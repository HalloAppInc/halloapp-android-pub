// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: log_events.proto

package com.halloapp.proto.log_events;

public interface DecryptionReportOrBuilder extends
    // @@protoc_insertion_point(interface_extends:server.DecryptionReport)
    com.google.protobuf.MessageLiteOrBuilder {

  /**
   * <code>string result = 1;</code>
   * @return The result.
   */
  java.lang.String getResult();
  /**
   * <code>string result = 1;</code>
   * @return The bytes for result.
   */
  com.google.protobuf.ByteString
      getResultBytes();

  /**
   * <code>string msg_id = 2;</code>
   * @return The msgId.
   */
  java.lang.String getMsgId();
  /**
   * <code>string msg_id = 2;</code>
   * @return The bytes for msgId.
   */
  com.google.protobuf.ByteString
      getMsgIdBytes();

  /**
   * <pre>
   * at time message id was first encountered
   * </pre>
   *
   * <code>string original_version = 3;</code>
   * @return The originalVersion.
   */
  java.lang.String getOriginalVersion();
  /**
   * <pre>
   * at time message id was first encountered
   * </pre>
   *
   * <code>string original_version = 3;</code>
   * @return The bytes for originalVersion.
   */
  com.google.protobuf.ByteString
      getOriginalVersionBytes();

  /**
   * <code>string sender_version = 4;</code>
   * @return The senderVersion.
   */
  java.lang.String getSenderVersion();
  /**
   * <code>string sender_version = 4;</code>
   * @return The bytes for senderVersion.
   */
  com.google.protobuf.ByteString
      getSenderVersionBytes();

  /**
   * <code>.server.Platform sender_platform = 5;</code>
   * @return The enum numeric value on the wire for senderPlatform.
   */
  int getSenderPlatformValue();
  /**
   * <code>.server.Platform sender_platform = 5;</code>
   * @return The senderPlatform.
   */
  com.halloapp.proto.log_events.Platform getSenderPlatform();

  /**
   * <code>uint32 rerequest_count = 6;</code>
   * @return The rerequestCount.
   */
  int getRerequestCount();

  /**
   * <code>uint32 time_taken_s = 7;</code>
   * @return The timeTakenS.
   */
  int getTimeTakenS();
}
