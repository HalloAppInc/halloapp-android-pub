// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: server.proto

package com.halloapp.proto.server;

public interface AudienceOrBuilder extends
    // @@protoc_insertion_point(interface_extends:server.Audience)
    com.google.protobuf.MessageLiteOrBuilder {

  /**
   * <code>.server.Audience.Type type = 1;</code>
   * @return The enum numeric value on the wire for type.
   */
  int getTypeValue();
  /**
   * <code>.server.Audience.Type type = 1;</code>
   * @return The type.
   */
  com.halloapp.proto.server.Audience.Type getType();

  /**
   * <code>repeated int64 uids = 2;</code>
   * @return A list containing the uids.
   */
  java.util.List<java.lang.Long> getUidsList();
  /**
   * <code>repeated int64 uids = 2;</code>
   * @return The count of uids.
   */
  int getUidsCount();
  /**
   * <code>repeated int64 uids = 2;</code>
   * @param index The index of the element to return.
   * @return The uids at the given index.
   */
  long getUids(int index);
}
