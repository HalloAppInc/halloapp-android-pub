// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: server.proto

package com.halloapp.proto.server;

public interface ScreenshotReceiptOrBuilder extends
    // @@protoc_insertion_point(interface_extends:server.ScreenshotReceipt)
    com.google.protobuf.MessageLiteOrBuilder {

  /**
   * <code>string id = 1;</code>
   * @return The id.
   */
  java.lang.String getId();
  /**
   * <code>string id = 1;</code>
   * @return The bytes for id.
   */
  com.google.protobuf.ByteString
      getIdBytes();

  /**
   * <code>string thread_id = 2;</code>
   * @return The threadId.
   */
  java.lang.String getThreadId();
  /**
   * <code>string thread_id = 2;</code>
   * @return The bytes for threadId.
   */
  com.google.protobuf.ByteString
      getThreadIdBytes();

  /**
   * <code>int64 timestamp = 3;</code>
   * @return The timestamp.
   */
  long getTimestamp();
}
