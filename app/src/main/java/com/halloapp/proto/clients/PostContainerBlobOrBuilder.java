// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: clients.proto

package com.halloapp.proto.clients;

public interface PostContainerBlobOrBuilder extends
    // @@protoc_insertion_point(interface_extends:clients.PostContainerBlob)
    com.google.protobuf.MessageLiteOrBuilder {

  /**
   * <code>.clients.PostContainer post_container = 1;</code>
   * @return Whether the postContainer field is set.
   */
  boolean hasPostContainer();
  /**
   * <code>.clients.PostContainer post_container = 1;</code>
   * @return The postContainer.
   */
  com.halloapp.proto.clients.PostContainer getPostContainer();

  /**
   * <code>int64 uid = 2;</code>
   * @return The uid.
   */
  long getUid();

  /**
   * <code>string post_id = 3;</code>
   * @return The postId.
   */
  java.lang.String getPostId();
  /**
   * <code>string post_id = 3;</code>
   * @return The bytes for postId.
   */
  com.google.protobuf.ByteString
      getPostIdBytes();
}