// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: server.proto

package com.halloapp.proto.server;

public interface FeedItemsOrBuilder extends
    // @@protoc_insertion_point(interface_extends:server.FeedItems)
    com.google.protobuf.MessageLiteOrBuilder {

  /**
   * <code>int64 uid = 1;</code>
   * @return The uid.
   */
  long getUid();

  /**
   * <code>repeated .server.FeedItem items = 2;</code>
   */
  java.util.List<com.halloapp.proto.server.FeedItem> 
      getItemsList();
  /**
   * <code>repeated .server.FeedItem items = 2;</code>
   */
  com.halloapp.proto.server.FeedItem getItems(int index);
  /**
   * <code>repeated .server.FeedItem items = 2;</code>
   */
  int getItemsCount();
}
