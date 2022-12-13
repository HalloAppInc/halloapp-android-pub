// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: server.proto

package com.halloapp.proto.server;

public interface PublicFeedItemOrBuilder extends
    // @@protoc_insertion_point(interface_extends:server.PublicFeedItem)
    com.google.protobuf.MessageLiteOrBuilder {

  /**
   * <code>.server.UserProfile user_profile = 1;</code>
   * @return Whether the userProfile field is set.
   */
  boolean hasUserProfile();
  /**
   * <code>.server.UserProfile user_profile = 1;</code>
   * @return The userProfile.
   */
  com.halloapp.proto.server.UserProfile getUserProfile();

  /**
   * <code>.server.Post post = 2;</code>
   * @return Whether the post field is set.
   */
  boolean hasPost();
  /**
   * <code>.server.Post post = 2;</code>
   * @return The post.
   */
  com.halloapp.proto.server.Post getPost();

  /**
   * <pre>
   * Comments will be capped - 20 per post for now.
   * </pre>
   *
   * <code>repeated .server.Comment comments = 3;</code>
   */
  java.util.List<com.halloapp.proto.server.Comment> 
      getCommentsList();
  /**
   * <pre>
   * Comments will be capped - 20 per post for now.
   * </pre>
   *
   * <code>repeated .server.Comment comments = 3;</code>
   */
  com.halloapp.proto.server.Comment getComments(int index);
  /**
   * <pre>
   * Comments will be capped - 20 per post for now.
   * </pre>
   *
   * <code>repeated .server.Comment comments = 3;</code>
   */
  int getCommentsCount();

  /**
   * <code>.server.PublicFeedItem.Reason reason = 4;</code>
   * @return The enum numeric value on the wire for reason.
   */
  int getReasonValue();
  /**
   * <code>.server.PublicFeedItem.Reason reason = 4;</code>
   * @return The reason.
   */
  com.halloapp.proto.server.PublicFeedItem.Reason getReason();
}