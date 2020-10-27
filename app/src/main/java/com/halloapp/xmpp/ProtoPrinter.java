package com.halloapp.xmpp;

import android.text.TextUtils;
import android.util.Pair;

import androidx.annotation.NonNull;

import com.google.protobuf.GeneratedMessageLite;
import com.halloapp.BuildConfig;
import com.halloapp.util.Preconditions;
import com.halloapp.util.logs.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Class for turning protobufs into cleaner human-readable strings
 *
 * This approach takes the default Protobuf toString() and makes some modifications to it. The main
 * drawback of this is that the toString() function does not have a spec and the output format
 * could change with future Protobuf updates. Additionally, the default toString() uses reflection
 * to inspect the names of the methods in order to traverse the structure; if Proguard changes
 * these names (i.e. like it does in release) then incorrect results will be printed.
 *
 * So how can we print these in production? One option is to tell proguard not to change
 * anything on classes extending GeneratedMessageLite (currently only fields are preserved),
 * but this leaks a lot of API information to anybody reverse-engineering the app. Probably
 * the best long-term solution is to store the raw protobuf contents in the logs with some
 * marker that tells a post-processor that it represents a protobuf; that post-processor then
 * looks at the included version number in order to fetch the relevant protobuf definitions
 * for conversion to string. We also could avoid all that server-side processing by writing
 * a manual toString() function, but it would have to be updated with each schema change.
 */
public class ProtoPrinter {

    public static String toString(@NonNull GeneratedMessageLite<?, ?> message) {
        return xml(message);
    }

    public static String simplified(@NonNull GeneratedMessageLite<?, ?> message) {
        return maybePrependWarning(simplifiedInternal(message));
    }

    public static String xml(@NonNull GeneratedMessageLite<?, ?> message) {
        String name = camelCaseToKebabCase(message.getClass().getSimpleName());
        ProtoNode node = parse(tokenize(name + " { " + simplifiedInternal(message) + " }"));
        return maybePrependWarning(node.toXml());
    }

    private static String maybePrependWarning(String s) {
        return BuildConfig.DEBUG ? s : "R!" + s;
    }

    private static String simplifiedInternal(@NonNull GeneratedMessageLite<?, ?> message) {
        String original = message.toString().replace("\"\\}", "\" \\}");
        String[] parts = original.split("\n");

        StringBuilder uncommented = new StringBuilder();
        // Skip first which is comment
        for (int i = 1; i < parts.length; i++) {
            uncommented.append(parts[i]).append(" ");
        }
        return uncommented.toString().replaceAll("  *", " ");
    }

    private static class ProtoNode {
        private static final int MAX_ATTR_LEN = 100;
        private static final int PEEK_BEFORE = 40;
        private static final int PEEK_AFTER = 40;

        @NonNull public String name;
        public List<Pair<String, String>> attributes;
        public List<ProtoNode> children;

        public ProtoNode(@NonNull String name, @NonNull List<Pair<String, String>> attributes, @NonNull List<ProtoNode> children) {
            List<Pair<String, String>> selected = new ArrayList<>();
            for (Pair<String, String> attribute : attributes) {
                if (!attribute.first.endsWith("_value")) {
                    selected.add(attribute);
                }
            }
            this.name = name;
            this.attributes = selected;
            this.children = children;
        }

        @NonNull
        public String toXml() {
            StringBuilder sb = new StringBuilder();

            sb.append("<").append(name);
            if (!attributes.isEmpty()) {
                sb.append(" ");
            }
            for (int i=0; i<attributes.size(); i++) {
                Pair<String, String> attribute = attributes.get(i);
                String second = attribute.second;
                if (second.length() > MAX_ATTR_LEN) {
                    second = second.substring(0, PEEK_BEFORE) + "…" + second.substring(second.length() - PEEK_AFTER);
                }
                sb.append(attribute.first).append("=").append(second);
                if (i != attributes.size() - 1) {
                    sb.append(" ");
                }
            }
            if (children.isEmpty()) {
                sb.append("/>");
            } else {
                sb.append(">");
                for (ProtoNode child : children) {
                    sb.append(child.toXml());
                }
                sb.append("</").append(name).append(">");
            }

            return sb.toString();
        }
    }

    private static String[] tokenize(@NonNull String s) {
        List<String> tokens = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        char[] chars = s.toCharArray();
        int i = 0;

        boolean quoted = false;
        boolean slashed = false;
        while (i < chars.length) {
            char c = chars[i];
            if (c == ' ' && !quoted) {
                String str = sb.toString();
                if (!TextUtils.isEmpty(str)) {
                    tokens.add(str);
                }
                sb = new StringBuilder();
                i++;
                continue;
            }
            if (c == '"' && !slashed) {
                quoted = !quoted;
            }
            if (c == '\\') {
                slashed = !slashed;
            } else {
                slashed = false;
            }
            sb.append(c);
            i++;
        }
        String str = sb.toString();
        if (!TextUtils.isEmpty(str)) {
            tokens.add(str);
        }

        String[] ret = new String[tokens.size()];
        tokens.toArray(ret);
        return ret;
    }

    private static ProtoNode parse(@NonNull String[] tokens) {
        List<Pair<String, String>> attributes = new ArrayList<>();
        List<ProtoNode> children = new ArrayList<>();

        if (tokens.length == 0) {
            throw new IllegalArgumentException("cannot parse token list of length 0");
        }

        String name = tokens[0];
        int i=0;
        while (i<tokens.length) {
            String token = tokens[i];
            if (token.endsWith(":")) {
                String key = token.substring(0, token.length() - 1);
                String value = tokens[i+1];
                attributes.add(new Pair<>(key, value));
                i += 2;
            } else if (!token.equals("{") && !token.equals("}") && i > 1) {
                int tokensForRecursiveCall = tokensFromNameToClose(tokens, i);
                ProtoNode child = parse(Arrays.copyOfRange(tokens, i, i + tokensForRecursiveCall));
                children.add(child);
                i += tokensForRecursiveCall;
            } else {
                i++;
            }
        }
        return new ProtoNode(name, attributes, children);
    }

    private static int tokensFromNameToClose(String[] tokens, int start) {
        int depth = 0;
        Preconditions.checkArgument(tokens[start + 1].equals("{")); // Starts ON the name
        for (int i=start; i<tokens.length; i++) {
            if (tokens[i].equals("{")) {
                depth++;
            } else if (tokens[i].equals("}")) {
                depth--;
                if (depth <= 0) {
                    return i - start;
                }
            }
        }
        return -1;
    }

    private static String camelCaseToKebabCase(String s) {
        StringBuilder sb = new StringBuilder();
        for (int i=0; i<s.length(); i++) {
            char c = s.charAt(i);
            if (Character.isUpperCase(c) && i != 0 && (i != s.length() - 1 && Character.isLowerCase(s.charAt(i + 1)) || Character.isLowerCase(s.charAt(i - 1)))) {
                sb.append('_');
            }
            sb.append(Character.toLowerCase(c));
        }

        return sb.toString();
    }
}
