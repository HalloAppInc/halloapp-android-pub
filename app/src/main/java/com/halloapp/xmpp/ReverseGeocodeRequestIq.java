package com.halloapp.xmpp;

import com.halloapp.proto.server.GpsLocation;
import com.halloapp.proto.server.Iq;
import com.halloapp.proto.server.ReverseGeocodeRequest;

public class ReverseGeocodeRequestIq extends HalloIq {

    private final double latitude;
    private final double longitude;

    public ReverseGeocodeRequestIq(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    @Override
    public Iq.Builder toProtoIq() {
        ReverseGeocodeRequest.Builder request = ReverseGeocodeRequest.newBuilder();
        final GpsLocation gpsLocation = GpsLocation.newBuilder().setLatitude(latitude).setLongitude(longitude).build();
        request.setLocation(gpsLocation);
        return Iq.newBuilder()
                .setType(Iq.Type.GET)
                .setReverseGeocodeRequest(request);
    }
}
