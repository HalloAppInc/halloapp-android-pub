// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: web.proto

package com.halloapp.proto.web;

public interface PostDisplayInfoOrBuilder extends
    // @@protoc_insertion_point(interface_extends:web.PostDisplayInfo)
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
   * <code>.web.PostDisplayInfo.TransferState transferState = 2;</code>
   * @return The enum numeric value on the wire for transferState.
   */
  int getTransferStateValue();
  /**
   * <code>.web.PostDisplayInfo.TransferState transferState = 2;</code>
   * @return The transferState.
   */
  com.halloapp.proto.web.PostDisplayInfo.TransferState getTransferState();

  /**
   * <code>.web.PostDisplayInfo.SeenState seenState = 3;</code>
   * @return The enum numeric value on the wire for seenState.
   */
  int getSeenStateValue();
  /**
   * <code>.web.PostDisplayInfo.SeenState seenState = 3;</code>
   * @return The seenState.
   */
  com.halloapp.proto.web.PostDisplayInfo.SeenState getSeenState();

  /**
   * <code>.web.PostDisplayInfo.RetractState retractState = 4;</code>
   * @return The enum numeric value on the wire for retractState.
   */
  int getRetractStateValue();
  /**
   * <code>.web.PostDisplayInfo.RetractState retractState = 4;</code>
   * @return The retractState.
   */
  com.halloapp.proto.web.PostDisplayInfo.RetractState getRetractState();

  /**
   * <code>bool is_unsupported = 5;</code>
   * @return The isUnsupported.
   */
  boolean getIsUnsupported();

  /**
   * <code>int32 unread_comments = 6;</code>
   * @return The unreadComments.
   */
  int getUnreadComments();

  /**
   * <code>repeated .web.ReceiptInfo user_receipts = 7;</code>
   */
  java.util.List<com.halloapp.proto.web.ReceiptInfo> 
      getUserReceiptsList();
  /**
   * <code>repeated .web.ReceiptInfo user_receipts = 7;</code>
   */
  com.halloapp.proto.web.ReceiptInfo getUserReceipts(int index);
  /**
   * <code>repeated .web.ReceiptInfo user_receipts = 7;</code>
   */
  int getUserReceiptsCount();
}
