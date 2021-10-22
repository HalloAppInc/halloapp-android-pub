// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: log_events.proto

package com.halloapp.proto.log_events;

public interface GroupDecryptionReportOrBuilder extends
    // @@protoc_insertion_point(interface_extends:server.GroupDecryptionReport)
    com.google.protobuf.MessageLiteOrBuilder {

  /**
   * <code>.server.GroupDecryptionReport.Status result = 1;</code>
   * @return The enum numeric value on the wire for result.
   */
  int getResultValue();
  /**
   * <code>.server.GroupDecryptionReport.Status result = 1;</code>
   * @return The result.
   */
  com.halloapp.proto.log_events.GroupDecryptionReport.Status getResult();

  /**
   * <code>string reason = 2;</code>
   * @return The reason.
   */
  java.lang.String getReason();
  /**
   * <code>string reason = 2;</code>
   * @return The bytes for reason.
   */
  com.google.protobuf.ByteString
      getReasonBytes();

  /**
   * <code>string content_id = 3;</code>
   * @return The contentId.
   */
  java.lang.String getContentId();
  /**
   * <code>string content_id = 3;</code>
   * @return The bytes for contentId.
   */
  com.google.protobuf.ByteString
      getContentIdBytes();

  /**
   * <code>string gid = 4;</code>
   * @return The gid.
   */
  java.lang.String getGid();
  /**
   * <code>string gid = 4;</code>
   * @return The bytes for gid.
   */
  com.google.protobuf.ByteString
      getGidBytes();

  /**
   * <code>.server.GroupDecryptionReport.ItemType item_type = 5;</code>
   * @return The enum numeric value on the wire for itemType.
   */
  int getItemTypeValue();
  /**
   * <code>.server.GroupDecryptionReport.ItemType item_type = 5;</code>
   * @return The itemType.
   */
  com.halloapp.proto.log_events.GroupDecryptionReport.ItemType getItemType();

  /**
   * <pre>
   * at time msg id was first encountered
   * </pre>
   *
   * <code>string original_version = 6;</code>
   * @return The originalVersion.
   */
  java.lang.String getOriginalVersion();
  /**
   * <pre>
   * at time msg id was first encountered
   * </pre>
   *
   * <code>string original_version = 6;</code>
   * @return The bytes for originalVersion.
   */
  com.google.protobuf.ByteString
      getOriginalVersionBytes();

  /**
   * <code>uint32 rerequest_count = 7;</code>
   * @return The rerequestCount.
   */
  int getRerequestCount();

  /**
   * <code>uint32 time_taken_s = 8;</code>
   * @return The timeTakenS.
   */
  int getTimeTakenS();

  /**
   * <code>.server.Platform sender_platform = 9;</code>
   * @return The enum numeric value on the wire for senderPlatform.
   */
  int getSenderPlatformValue();
  /**
   * <code>.server.Platform sender_platform = 9;</code>
   * @return The senderPlatform.
   */
  com.halloapp.proto.log_events.Platform getSenderPlatform();

  /**
   * <code>string sender_version = 10;</code>
   * @return The senderVersion.
   */
  java.lang.String getSenderVersion();
  /**
   * <code>string sender_version = 10;</code>
   * @return The bytes for senderVersion.
   */
  com.google.protobuf.ByteString
      getSenderVersionBytes();
}
