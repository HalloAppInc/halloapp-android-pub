// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: server.proto

package com.halloapp.proto.server;

public interface InviterOrBuilder extends
    // @@protoc_insertion_point(interface_extends:server.Inviter)
    com.google.protobuf.MessageLiteOrBuilder {

  /**
   * <code>int64 uid = 1;</code>
   * @return The uid.
   */
  long getUid();

  /**
   * <code>string name = 2;</code>
   * @return The name.
   */
  java.lang.String getName();
  /**
   * <code>string name = 2;</code>
   * @return The bytes for name.
   */
  com.google.protobuf.ByteString
      getNameBytes();

  /**
   * <code>string phone = 3;</code>
   * @return The phone.
   */
  java.lang.String getPhone();
  /**
   * <code>string phone = 3;</code>
   * @return The bytes for phone.
   */
  com.google.protobuf.ByteString
      getPhoneBytes();

  /**
   * <code>int64 timestamp = 4;</code>
   * @return The timestamp.
   */
  long getTimestamp();
}
