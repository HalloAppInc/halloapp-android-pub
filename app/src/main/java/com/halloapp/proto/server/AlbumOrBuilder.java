// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: server.proto

package com.halloapp.proto.server;

public interface AlbumOrBuilder extends
    // @@protoc_insertion_point(interface_extends:server.Album)
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
   * <code>.server.Album.Action action = 2;</code>
   * @return The enum numeric value on the wire for action.
   */
  int getActionValue();
  /**
   * <code>.server.Album.Action action = 2;</code>
   * @return The action.
   */
  com.halloapp.proto.server.Album.Action getAction();

  /**
   * <code>string name = 3;</code>
   * @return The name.
   */
  java.lang.String getName();
  /**
   * <code>string name = 3;</code>
   * @return The bytes for name.
   */
  com.google.protobuf.ByteString
      getNameBytes();

  /**
   * <code>int64 owner = 4;</code>
   * @return The owner.
   */
  long getOwner();

  /**
   * <code>.server.TimeRange time_range = 5;</code>
   * @return Whether the timeRange field is set.
   */
  boolean hasTimeRange();
  /**
   * <code>.server.TimeRange time_range = 5;</code>
   * @return The timeRange.
   */
  com.halloapp.proto.server.TimeRange getTimeRange();

  /**
   * <code>.server.GpsLocation location = 6;</code>
   * @return Whether the location field is set.
   */
  boolean hasLocation();
  /**
   * <code>.server.GpsLocation location = 6;</code>
   * @return The location.
   */
  com.halloapp.proto.server.GpsLocation getLocation();

  /**
   * <code>.server.AlbumAccess can_view = 7;</code>
   * @return The enum numeric value on the wire for canView.
   */
  int getCanViewValue();
  /**
   * <code>.server.AlbumAccess can_view = 7;</code>
   * @return The canView.
   */
  com.halloapp.proto.server.AlbumAccess getCanView();

  /**
   * <code>.server.AlbumAccess can_contribute = 8;</code>
   * @return The enum numeric value on the wire for canContribute.
   */
  int getCanContributeValue();
  /**
   * <code>.server.AlbumAccess can_contribute = 8;</code>
   * @return The canContribute.
   */
  com.halloapp.proto.server.AlbumAccess getCanContribute();

  /**
   * <code>repeated .server.AlbumMember members = 9;</code>
   */
  java.util.List<com.halloapp.proto.server.AlbumMember> 
      getMembersList();
  /**
   * <code>repeated .server.AlbumMember members = 9;</code>
   */
  com.halloapp.proto.server.AlbumMember getMembers(int index);
  /**
   * <code>repeated .server.AlbumMember members = 9;</code>
   */
  int getMembersCount();

  /**
   * <code>repeated .server.MediaItem media_items = 10;</code>
   */
  java.util.List<com.halloapp.proto.server.MediaItem> 
      getMediaItemsList();
  /**
   * <code>repeated .server.MediaItem media_items = 10;</code>
   */
  com.halloapp.proto.server.MediaItem getMediaItems(int index);
  /**
   * <code>repeated .server.MediaItem media_items = 10;</code>
   */
  int getMediaItemsCount();

  /**
   * <code>string cursor = 11;</code>
   * @return The cursor.
   */
  java.lang.String getCursor();
  /**
   * <code>string cursor = 11;</code>
   * @return The bytes for cursor.
   */
  com.google.protobuf.ByteString
      getCursorBytes();
}