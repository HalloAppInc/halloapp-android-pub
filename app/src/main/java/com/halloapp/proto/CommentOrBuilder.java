// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: schema.proto

package com.halloapp.proto;

public interface CommentOrBuilder extends
    // @@protoc_insertion_point(interface_extends:proto.Comment)
    com.google.protobuf.MessageLiteOrBuilder {

  /**
   * <code>string feed_post_id = 1;</code>
   * @return The feedPostId.
   */
  java.lang.String getFeedPostId();
  /**
   * <code>string feed_post_id = 1;</code>
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
   * <code>string text = 4;</code>
   * @return The text.
   */
  java.lang.String getText();
  /**
   * <code>string text = 4;</code>
   * @return The bytes for text.
   */
  com.google.protobuf.ByteString
      getTextBytes();
}
