// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: clients.proto

package com.halloapp.proto.clients;

public interface CommentIdContextOrBuilder extends
    // @@protoc_insertion_point(interface_extends:clients.CommentIdContext)
    com.google.protobuf.MessageLiteOrBuilder {

  /**
   * <code>string comment_id = 1;</code>
   * @return The commentId.
   */
  java.lang.String getCommentId();
  /**
   * <code>string comment_id = 1;</code>
   * @return The bytes for commentId.
   */
  com.google.protobuf.ByteString
      getCommentIdBytes();

  /**
   * <code>string feed_post_id = 2;</code>
   * @return The feedPostId.
   */
  java.lang.String getFeedPostId();
  /**
   * <code>string feed_post_id = 2;</code>
   * @return The bytes for feedPostId.
   */
  com.google.protobuf.ByteString
      getFeedPostIdBytes();

  /**
   * <code>string parent_comment_id = 3;</code>
   * @return The parentCommentId.
   */
  java.lang.String getParentCommentId();
  /**
   * <code>string parent_comment_id = 3;</code>
   * @return The bytes for parentCommentId.
   */
  com.google.protobuf.ByteString
      getParentCommentIdBytes();

  /**
   * <code>int64 sender_uid = 4;</code>
   * @return The senderUid.
   */
  long getSenderUid();

  /**
   * <code>int64 timestamp = 5;</code>
   * @return The timestamp.
   */
  long getTimestamp();
}
