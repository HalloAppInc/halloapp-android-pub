// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: server.proto

package com.halloapp.proto.server;

public interface CallRingingOrBuilder extends
    // @@protoc_insertion_point(interface_extends:server.CallRinging)
    com.google.protobuf.MessageLiteOrBuilder {

  /**
   * <code>string call_id = 1;</code>
   * @return The callId.
   */
  java.lang.String getCallId();
  /**
   * <code>string call_id = 1;</code>
   * @return The bytes for callId.
   */
  com.google.protobuf.ByteString
      getCallIdBytes();

  /**
   * <code>int64 timestamp_ms = 2;</code>
   * @return The timestampMs.
   */
  long getTimestampMs();
}
