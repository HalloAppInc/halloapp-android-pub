package com.halloapp.xmpp;

import androidx.annotation.NonNull;

import com.halloapp.Me;
import com.halloapp.noise.HANoiseSocket;
import com.halloapp.util.logs.Log;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class SocketConnector {
    private static final int INITIAL_CONNECTION_COUNT = 2;
    private static final long INITIAL_NEXT_CONNECT_TIMEOUT_MS = 250;
    private static final long MAX_WAIT_TIME_MS = 10_000;
    private static final int CONNECT_IP_ATTEMPTS = 4;
    private static final int CONNECT_WAIT_TIME = 30_000;

    private CompletionService<HANoiseSocket> socketCompletionService;
    private List<HANoiseSocket> connectingSockets;
    private int openAttempts;

    public SocketConnector(@NonNull Executor executor) {
        socketCompletionService = new ExecutorCompletionService<>(executor);
    }

    @NonNull
    public HANoiseSocket connect(@NonNull Me me, @NonNull String host, int port) throws IOException {
        cleanup();

        List<InetAddress> addresses = getInterleavedAddresses(host);
        connectingSockets = new ArrayList<>();
        ListIterator<InetAddress> addressIterator = addresses.listIterator();
        int connectCount = 0;
        HANoiseSocket socket = null;
        try {
            for (int i = 0; i < INITIAL_CONNECTION_COUNT && addressIterator.hasNext(); i++) {
                attemptConnect(me, addressIterator.next(), port);
                connectCount++;
            }

            long waitTimeMs = INITIAL_NEXT_CONNECT_TIMEOUT_MS;
            while (addressIterator.hasNext() && connectCount < CONNECT_IP_ATTEMPTS) {
                try {
                    socket = getNextSocketResult(waitTimeMs);
                    if (socket != null) {
                        break;
                    }
                } catch (InterruptedException e) {
                    Log.e("SocketConnector/connect interrupted", e);
                    break;
                }
                waitTimeMs = Math.min(MAX_WAIT_TIME_MS, waitTimeMs * 2);
                attemptConnect(me, addressIterator.next(), port);
                connectCount++;
            }

            if (socket == null && openAttempts > 0) {
                socket = getNextSocketResult(CONNECT_WAIT_TIME);
            }
        } catch (InterruptedException e) {
            Log.e("SocketConnector/connect final wait interrupted", e);
        } finally {
            cleanup();
        }
        if (socket == null) {
            throw new IOException("SocketConnector failed to connect to address " + host);
        }
        return socket;
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
        openAttempts = 0;
    }

    private void attemptConnect(@NonNull Me me, InetAddress address, int port) {
        Log.i("SocketConnector/attemptConnect queuing up " + address);
        HANoiseSocket nextSocket = new HANoiseSocket(me, address, port);
        connectingSockets.add(nextSocket);
        Future<HANoiseSocket> future = socketCompletionService.submit(new ConnectAttempt(nextSocket));
        if (future != null) {
            openAttempts++;
        }
    }

    private HANoiseSocket getNextSocketResult(long waitTimeMs) throws InterruptedException {
        Future<HANoiseSocket> future;
        future = socketCompletionService.poll(waitTimeMs, TimeUnit.MILLISECONDS);
        HANoiseSocket socket = null;
        if (future != null) {
            openAttempts--;
            try {
                socket = future.get();
                if (socket != null) {
                    connectingSockets.remove(socket);
                }
            } catch (ExecutionException e) {
                Log.e("Connection Attempt failed", e);
            }
        }
        return socket;
    }

    private List<InetAddress> getInterleavedAddresses(@NonNull String host) throws UnknownHostException {
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

        List<InetAddress> allAddresses = new LinkedList<>();
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

    private static class ConnectAttempt implements Callable<HANoiseSocket> {

        private final HANoiseSocket socket;

        ConnectAttempt(@NonNull HANoiseSocket socket) {
            this.socket = socket;
        }

        @Override
        public HANoiseSocket call() throws Exception {
            socket.connect();
            return socket;
        }
    }
}
