// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: server.proto

package com.halloapp.proto.server;

public interface ShareStanzaOrBuilder extends
    // @@protoc_insertion_point(interface_extends:server.ShareStanza)
    com.google.protobuf.MessageLiteOrBuilder {

  /**
   * <code>int64 uid = 1;</code>
   * @return The uid.
   */
  long getUid();

  /**
   * <code>repeated string post_ids = 2;</code>
   * @return A list containing the postIds.
   */
  java.util.List<java.lang.String>
      getPostIdsList();
  /**
   * <code>repeated string post_ids = 2;</code>
   * @return The count of postIds.
   */
  int getPostIdsCount();
  /**
   * <code>repeated string post_ids = 2;</code>
   * @param index The index of the element to return.
   * @return The postIds at the given index.
   */
  java.lang.String getPostIds(int index);
  /**
   * <code>repeated string post_ids = 2;</code>
   * @param index The index of the element to return.
   * @return The postIds at the given index.
   */
  com.google.protobuf.ByteString
      getPostIdsBytes(int index);

  /**
   * <code>string result = 3;</code>
   * @return The result.
   */
  java.lang.String getResult();
  /**
   * <code>string result = 3;</code>
   * @return The bytes for result.
   */
  com.google.protobuf.ByteString
      getResultBytes();

  /**
   * <code>string reason = 4;</code>
   * @return The reason.
   */
  java.lang.String getReason();
  /**
   * <code>string reason = 4;</code>
   * @return The bytes for reason.
   */
  com.google.protobuf.ByteString
      getReasonBytes();
}
