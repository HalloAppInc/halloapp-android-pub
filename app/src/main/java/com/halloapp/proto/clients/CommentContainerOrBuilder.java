// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: clients.proto

package com.halloapp.proto.clients;

public interface CommentContainerOrBuilder extends
    // @@protoc_insertion_point(interface_extends:clients.CommentContainer)
    com.google.protobuf.MessageLiteOrBuilder {

  /**
   * <code>.clients.CommentContext context = 1;</code>
   * @return Whether the context field is set.
   */
  boolean hasContext();
  /**
   * <code>.clients.CommentContext context = 1;</code>
   * @return The context.
   */
  com.halloapp.proto.clients.CommentContext getContext();

  /**
   * <code>.clients.Text text = 2;</code>
   * @return Whether the text field is set.
   */
  boolean hasText();
  /**
   * <code>.clients.Text text = 2;</code>
   * @return The text.
   */
  com.halloapp.proto.clients.Text getText();

  /**
   * <code>.clients.Album album = 3;</code>
   * @return Whether the album field is set.
   */
  boolean hasAlbum();
  /**
   * <code>.clients.Album album = 3;</code>
   * @return The album.
   */
  com.halloapp.proto.clients.Album getAlbum();

  /**
   * <code>.clients.VoiceNote voice_note = 4;</code>
   * @return Whether the voiceNote field is set.
   */
  boolean hasVoiceNote();
  /**
   * <code>.clients.VoiceNote voice_note = 4;</code>
   * @return The voiceNote.
   */
  com.halloapp.proto.clients.VoiceNote getVoiceNote();

  /**
   * <code>.clients.Reaction reaction = 5;</code>
   * @return Whether the reaction field is set.
   */
  boolean hasReaction();
  /**
   * <code>.clients.Reaction reaction = 5;</code>
   * @return The reaction.
   */
  com.halloapp.proto.clients.Reaction getReaction();

  /**
   * <code>.clients.Sticker sticker = 6;</code>
   * @return Whether the sticker field is set.
   */
  boolean hasSticker();
  /**
   * <code>.clients.Sticker sticker = 6;</code>
   * @return The sticker.
   */
  com.halloapp.proto.clients.Sticker getSticker();

  public com.halloapp.proto.clients.CommentContainer.CommentCase getCommentCase();
}
