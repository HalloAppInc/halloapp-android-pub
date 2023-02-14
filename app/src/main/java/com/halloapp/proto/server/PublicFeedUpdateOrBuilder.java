// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: server.proto

package com.halloapp.proto.server;

public interface PublicFeedUpdateOrBuilder extends
    // @@protoc_insertion_point(interface_extends:server.PublicFeedUpdate)
    com.google.protobuf.MessageLiteOrBuilder {

  /**
   * <code>string cursor = 1;</code>
   * @return The cursor.
   */
  java.lang.String getCursor();
  /**
   * <code>string cursor = 1;</code>
   * @return The bytes for cursor.
   */
  com.google.protobuf.ByteString
      getCursorBytes();

  /**
   * <code>.server.PublicFeedContentType public_feed_content_type = 2;</code>
   * @return The enum numeric value on the wire for publicFeedContentType.
   */
  int getPublicFeedContentTypeValue();
  /**
   * <code>.server.PublicFeedContentType public_feed_content_type = 2;</code>
   * @return The publicFeedContentType.
   */
  com.halloapp.proto.server.PublicFeedContentType getPublicFeedContentType();

  /**
   * <code>repeated .server.PublicFeedItem items = 3;</code>
   */
  java.util.List<com.halloapp.proto.server.PublicFeedItem> 
      getItemsList();
  /**
   * <code>repeated .server.PublicFeedItem items = 3;</code>
   */
  com.halloapp.proto.server.PublicFeedItem getItems(int index);
  /**
   * <code>repeated .server.PublicFeedItem items = 3;</code>
   */
  int getItemsCount();
}