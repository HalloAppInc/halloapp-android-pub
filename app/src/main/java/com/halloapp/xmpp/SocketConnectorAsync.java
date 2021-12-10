package com.halloapp.xmpp;

import android.text.format.DateUtils;

import androidx.annotation.NonNull;

import com.halloapp.ForegroundObserver;
import com.halloapp.Me;
import com.halloapp.noise.HANoiseSocket;
import com.halloapp.util.BgWorkers;
import com.halloapp.util.ThreadUtils;
import com.halloapp.util.logs.Log;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class SocketConnectorAsync {
    private static final int INITIAL_CONNECTION_COUNT = 2;
    private static final long INITIAL_NEXT_CONNECT_TIMEOUT_MS = 250;

    private static final long MAX_WAIT_TIME_FOREGROUND_MS = 10 * DateUtils.SECOND_IN_MILLIS;
    private static final long MAX_WAIT_TIME_BACKGROUND_MS = 2 * DateUtils.HOUR_IN_MILLIS;

    private CompletionService<HANoiseSocket> socketCompletionService;

    private final Me me;
    private final ForegroundObserver foregroundObserver;

    private final List<HANoiseSocket> connectingSockets = new ArrayList<>();

    private Thread connectorThread;
    private ConnectRunnable connectRunnable;

    private SocketListener socketListener;

    private Deque<InetAddress> addresses;

    private long waitTimeMs;

    public interface SocketListener {
        void onConnected(@NonNull HANoiseSocket socket);
        boolean isConnected();
    }

    public SocketConnectorAsync(@NonNull Me me, @NonNull BgWorkers bgWorkers, @NonNull ForegroundObserver foregroundObserver, @NonNull SocketListener socketListener) {
        this.me = me;
        this.foregroundObserver = foregroundObserver;
        this.socketListener = socketListener;

        socketCompletionService = new ExecutorCompletionService<>(bgWorkers.getExecutor());
    }

    public synchronized void connect(@NonNull String host, int port) {
        if (socketListener.isConnected()) {
            return;
        }
        if (connectRunnable != null && !connectRunnable.isDone()) {
            return;
        }
        connectRunnable = new ConnectRunnable(host, port);
        connectorThread = ThreadUtils.go(connectRunnable, "connector-thread");
    }

    public void resetConnectionBackoff() {
        if (connectorThread != null && connectRunnable != null && !connectRunnable.isDone()) {
            waitTimeMs = INITIAL_NEXT_CONNECT_TIMEOUT_MS;
            connectorThread.interrupt();
        }
    }

    public synchronized boolean isConnecting() {
        if (connectRunnable != null) {
            return !connectRunnable.isDone();
        }
        return false;
    }

    private void attemptConnect(@NonNull String host, int port) throws IOException {
        if (addresses == null || addresses.isEmpty()) {
            addresses = getInterleavedAddresses(host);
        }
        if (addresses.isEmpty()) {
            throw new IOException("No available addresses to connect to");
        }
        attemptConnect(me, addresses.remove(), port);
    }

    private void cleanup() {
        for (HANoiseSocket connectingSocket : connectingSockets) {
            try {
                connectingSocket.close();
            } catch (IOException e) {
                Log.e("SocketConnector/cleanup failed to close", e);
            }
        }
        connectingSockets.clear();
        while (socketCompletionService.poll() != null) {}
    }

    private void attemptConnect(@NonNull Me me, InetAddress address, int port) {
        Log.i("SocketConnector/attemptConnect queuing up " + address);
        HANoiseSocket nextSocket = new HANoiseSocket(me, address, port);
        connectingSockets.add(nextSocket);
        socketCompletionService.submit(new ConnectAttempt(nextSocket));
    }

    private HANoiseSocket getNextSocketResult(long waitTimeMs) throws InterruptedException {
        HANoiseSocket socket = null;
        Future<HANoiseSocket> future = socketCompletionService.poll();

        while (future != null) {
            socket = getSocket(future);
            if (socket != null && !socket.isClosed()) {
                return socket;
            }
            future = socketCompletionService.poll();
        }

        future = socketCompletionService.poll(waitTimeMs, TimeUnit.MILLISECONDS);
        if (future != null) {
            socket = getSocket(future);
        }
        return socket;
    }

    private HANoiseSocket getSocket(@NonNull Future<HANoiseSocket> completedFuture) throws InterruptedException {
        HANoiseSocket socket = null;
        try {
            socket = completedFuture.get();
        } catch (ExecutionException e) {
            Log.e("Connection Attempt failed", e);
        }
        if (socket != null) {
            connectingSockets.remove(socket);
        }
        return socket;
    }

    private Deque<InetAddress> getInterleavedAddresses(@NonNull String host) throws UnknownHostException {
        InetAddress[] addresses = InetAddress.getAllByName(host);

        List<InetAddress> v6addresses = new ArrayList<>();
        List<InetAddress> v4addresses = new ArrayList<>();
        for (InetAddress address : addresses) {
            if (address instanceof Inet4Address) {
                v4addresses.add(address);
            } else {
                v6addresses.add(address);
            }
        }
        Collections.shuffle(v4addresses);
        Collections.shuffle(v6addresses);

        LinkedList<InetAddress> allAddresses = new LinkedList<>();
        Iterator<InetAddress> v4iterator = v4addresses.iterator();
        Iterator<InetAddress> v6iterator = v6addresses.iterator();

        while (v4iterator.hasNext() || v6iterator.hasNext()) {
            if (v4iterator.hasNext()) {
                allAddresses.add(v4iterator.next());
            }
            if (v6iterator.hasNext()) {
                allAddresses.add(v6iterator.next());
            }
        }

        return allAddresses;
    }

    private class ConnectRunnable implements Runnable {

        private final String host;
        private final int port;

        private boolean done = false;

        ConnectRunnable(@NonNull String host, int port) {
            this.host = host;
            this.port = port;
        }

        @Override
        public void run() {
            while (!done) {
                try {
                    attemptConnect(host, port);
                } catch (IOException e) {
                    Log.e("Failed to fetch IP addresses", e);
                    done = true;
                    break;
                }
                if (waitTimeMs < INITIAL_NEXT_CONNECT_TIMEOUT_MS) {
                    waitTimeMs = INITIAL_NEXT_CONNECT_TIMEOUT_MS;
                } else {
                    if (foregroundObserver.isInForeground()) {
                        waitTimeMs = Math.min(waitTimeMs, MAX_WAIT_TIME_FOREGROUND_MS);
                    } else {
                        waitTimeMs = Math.min(waitTimeMs, MAX_WAIT_TIME_BACKGROUND_MS);
                    }
                }
                HANoiseSocket socket = null;
                try {
                    socket = getNextSocketResult(waitTimeMs);
                } catch (InterruptedException e) {
                    Log.e("Wait time interrupted", e);
                }
                if (socket == null || socket.isClosed()) {
                    waitTimeMs = Math.min(foregroundObserver.isInForeground() ? MAX_WAIT_TIME_FOREGROUND_MS : MAX_WAIT_TIME_BACKGROUND_MS, waitTimeMs * 2);
                } else {
                    waitTimeMs = INITIAL_NEXT_CONNECT_TIMEOUT_MS;
                    done = true;
                    cleanup();
                    if (socketListener != null) {
                        socketListener.onConnected(socket);
                    }
                }
            }
        }

        public boolean isDone() {
            return done;
        }
    }

    private static class ConnectAttempt implements Callable<HANoiseSocket> {

        private final HANoiseSocket socket;

        ConnectAttempt(@NonNull HANoiseSocket socket) {
            this.socket = socket;
        }

        @Override
        public HANoiseSocket call() throws Exception {
            ThreadUtils.setSocketTag();
            socket.connect();
            if (socket.isClosed()) {
                throw new SocketException("socket closed");
            }
            return socket;
        }
    }
}
