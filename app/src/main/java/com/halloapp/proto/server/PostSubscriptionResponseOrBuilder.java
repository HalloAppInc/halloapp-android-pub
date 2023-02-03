// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: server.proto

package com.halloapp.proto.server;

public interface PostSubscriptionResponseOrBuilder extends
    // @@protoc_insertion_point(interface_extends:server.PostSubscriptionResponse)
    com.google.protobuf.MessageLiteOrBuilder {

  /**
   * <code>.server.PostSubscriptionResponse.Result result = 1;</code>
   * @return The enum numeric value on the wire for result.
   */
  int getResultValue();
  /**
   * <code>.server.PostSubscriptionResponse.Result result = 1;</code>
   * @return The result.
   */
  com.halloapp.proto.server.PostSubscriptionResponse.Result getResult();

  /**
   * <code>.server.PostSubscriptionResponse.Reason reason = 2;</code>
   * @return The enum numeric value on the wire for reason.
   */
  int getReasonValue();
  /**
   * <code>.server.PostSubscriptionResponse.Reason reason = 2;</code>
   * @return The reason.
   */
  com.halloapp.proto.server.PostSubscriptionResponse.Reason getReason();

  /**
   * <code>repeated .server.FeedItem items = 3;</code>
   */
  java.util.List<com.halloapp.proto.server.FeedItem> 
      getItemsList();
  /**
   * <code>repeated .server.FeedItem items = 3;</code>
   */
  com.halloapp.proto.server.FeedItem getItems(int index);
  /**
   * <code>repeated .server.FeedItem items = 3;</code>
   */
  int getItemsCount();
}