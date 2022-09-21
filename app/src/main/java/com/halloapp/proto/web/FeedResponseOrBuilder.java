// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: web.proto

package com.halloapp.proto.web;

public interface FeedResponseOrBuilder extends
    // @@protoc_insertion_point(interface_extends:web.FeedResponse)
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
   * <code>.web.FeedType type = 2;</code>
   * @return The enum numeric value on the wire for type.
   */
  int getTypeValue();
  /**
   * <code>.web.FeedType type = 2;</code>
   * @return The type.
   */
  com.halloapp.proto.web.FeedType getType();

  /**
   * <code>repeated .web.FeedItem items = 3;</code>
   */
  java.util.List<com.halloapp.proto.web.FeedItem> 
      getItemsList();
  /**
   * <code>repeated .web.FeedItem items = 3;</code>
   */
  com.halloapp.proto.web.FeedItem getItems(int index);
  /**
   * <code>repeated .web.FeedItem items = 3;</code>
   */
  int getItemsCount();

  /**
   * <code>repeated .web.UserDisplayInfo user_display_info = 4;</code>
   */
  java.util.List<com.halloapp.proto.web.UserDisplayInfo> 
      getUserDisplayInfoList();
  /**
   * <code>repeated .web.UserDisplayInfo user_display_info = 4;</code>
   */
  com.halloapp.proto.web.UserDisplayInfo getUserDisplayInfo(int index);
  /**
   * <code>repeated .web.UserDisplayInfo user_display_info = 4;</code>
   */
  int getUserDisplayInfoCount();

  /**
   * <code>repeated .web.PostDisplayInfo post_display_info = 5;</code>
   */
  java.util.List<com.halloapp.proto.web.PostDisplayInfo> 
      getPostDisplayInfoList();
  /**
   * <code>repeated .web.PostDisplayInfo post_display_info = 5;</code>
   */
  com.halloapp.proto.web.PostDisplayInfo getPostDisplayInfo(int index);
  /**
   * <code>repeated .web.PostDisplayInfo post_display_info = 5;</code>
   */
  int getPostDisplayInfoCount();

  /**
   * <code>string next_cursor = 6;</code>
   * @return The nextCursor.
   */
  java.lang.String getNextCursor();
  /**
   * <code>string next_cursor = 6;</code>
   * @return The bytes for nextCursor.
   */
  com.google.protobuf.ByteString
      getNextCursorBytes();

  /**
   * <code>.web.FeedResponse.Error error = 7;</code>
   * @return The enum numeric value on the wire for error.
   */
  int getErrorValue();
  /**
   * <code>.web.FeedResponse.Error error = 7;</code>
   * @return The error.
   */
  com.halloapp.proto.web.FeedResponse.Error getError();

  /**
   * <code>repeated .web.GroupDisplayInfo group_display_info = 8;</code>
   */
  java.util.List<com.halloapp.proto.web.GroupDisplayInfo> 
      getGroupDisplayInfoList();
  /**
   * <code>repeated .web.GroupDisplayInfo group_display_info = 8;</code>
   */
  com.halloapp.proto.web.GroupDisplayInfo getGroupDisplayInfo(int index);
  /**
   * <code>repeated .web.GroupDisplayInfo group_display_info = 8;</code>
   */
  int getGroupDisplayInfoCount();
}