// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: schema.proto

package com.halloapp.proto;

public interface ChatMessageOrBuilder extends
    // @@protoc_insertion_point(interface_extends:proto.ChatMessage)
    com.google.protobuf.MessageLiteOrBuilder {

  /**
   * <code>repeated .proto.Media media = 1;</code>
   */
  java.util.List<com.halloapp.proto.Media> 
      getMediaList();
  /**
   * <code>repeated .proto.Media media = 1;</code>
   */
  com.halloapp.proto.Media getMedia(int index);
  /**
   * <code>repeated .proto.Media media = 1;</code>
   */
  int getMediaCount();

  /**
   * <code>string text = 2;</code>
   * @return The text.
   */
  java.lang.String getText();
  /**
   * <code>string text = 2;</code>
   * @return The bytes for text.
   */
  com.google.protobuf.ByteString
      getTextBytes();

  /**
   * <code>string feed_post_id = 3;</code>
   * @return The feedPostId.
   */
  java.lang.String getFeedPostId();
  /**
   * <code>string feed_post_id = 3;</code>
   * @return The bytes for feedPostId.
   */
  com.google.protobuf.ByteString
      getFeedPostIdBytes();

  /**
   * <code>int32 feed_post_media_index = 4;</code>
   * @return The feedPostMediaIndex.
   */
  int getFeedPostMediaIndex();

  /**
   * <code>repeated .proto.Mention mentions = 5;</code>
   */
  java.util.List<com.halloapp.proto.Mention> 
      getMentionsList();
  /**
   * <code>repeated .proto.Mention mentions = 5;</code>
   */
  com.halloapp.proto.Mention getMentions(int index);
  /**
   * <code>repeated .proto.Mention mentions = 5;</code>
   */
  int getMentionsCount();
}
