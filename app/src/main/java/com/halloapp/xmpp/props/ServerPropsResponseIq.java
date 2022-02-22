package com.halloapp.xmpp.props;

import com.google.android.gms.common.util.Hex;
import com.halloapp.proto.server.Iq;
import com.halloapp.proto.server.Prop;
import com.halloapp.proto.server.Props;
import com.halloapp.xmpp.HalloIq;

import java.util.HashMap;
import java.util.Map;

public class ServerPropsResponseIq extends HalloIq {

    private final Map<String, String> propMap;
    private final String hash;

    private ServerPropsResponseIq(Map<String, String> propMap, String hash) {
        this.propMap = propMap;
        this.hash = hash;
    }

    public Map<String, String> getProps() {
        return propMap;
    }

    public String getHash() {
        return hash;
    }

    public static ServerPropsResponseIq fromProto(Props props) {
        final Map<String, String> propMap = new HashMap<>();
        final String hash = Hex.bytesToStringLowercase(props.getHash().toByteArray());
        for (Prop prop : props.getPropsList()) {
            propMap.put(prop.getName(), prop.getValue());
        }
        return new ServerPropsResponseIq(propMap, hash);
    }

    @Override
    public Iq toProtoIq() {
        return null;
    }
}
