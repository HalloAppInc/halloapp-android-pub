// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: server.proto

package com.halloapp.proto.server;

public interface HomeFeedRerequestOrBuilder extends
    // @@protoc_insertion_point(interface_extends:server.HomeFeedRerequest)
    com.google.protobuf.MessageLiteOrBuilder {

  /**
   * <pre>
   * Post id or Comment id.
   * </pre>
   *
   * <code>string id = 1;</code>
   * @return The id.
   */
  java.lang.String getId();
  /**
   * <pre>
   * Post id or Comment id.
   * </pre>
   *
   * <code>string id = 1;</code>
   * @return The bytes for id.
   */
  com.google.protobuf.ByteString
      getIdBytes();

  /**
   * <code>.server.HomeFeedRerequest.RerequestType rerequest_type = 2;</code>
   * @return The enum numeric value on the wire for rerequestType.
   */
  int getRerequestTypeValue();
  /**
   * <code>.server.HomeFeedRerequest.RerequestType rerequest_type = 2;</code>
   * @return The rerequestType.
   */
  com.halloapp.proto.server.HomeFeedRerequest.RerequestType getRerequestType();
}