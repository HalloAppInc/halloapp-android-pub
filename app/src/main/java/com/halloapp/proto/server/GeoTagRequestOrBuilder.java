// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: server.proto

package com.halloapp.proto.server;

public interface GeoTagRequestOrBuilder extends
    // @@protoc_insertion_point(interface_extends:server.GeoTagRequest)
    com.google.protobuf.MessageLiteOrBuilder {

  /**
   * <code>.server.GeoTagRequest.Action action = 1;</code>
   * @return The enum numeric value on the wire for action.
   */
  int getActionValue();
  /**
   * <code>.server.GeoTagRequest.Action action = 1;</code>
   * @return The action.
   */
  com.halloapp.proto.server.GeoTagRequest.Action getAction();

  /**
   * <code>.server.GpsLocation gps_location = 2;</code>
   * @return Whether the gpsLocation field is set.
   */
  boolean hasGpsLocation();
  /**
   * <code>.server.GpsLocation gps_location = 2;</code>
   * @return The gpsLocation.
   */
  com.halloapp.proto.server.GpsLocation getGpsLocation();

  /**
   * <code>string geo_tag = 3;</code>
   * @return The geoTag.
   */
  java.lang.String getGeoTag();
  /**
   * <code>string geo_tag = 3;</code>
   * @return The bytes for geoTag.
   */
  com.google.protobuf.ByteString
      getGeoTagBytes();
}
