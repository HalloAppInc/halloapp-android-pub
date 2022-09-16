// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: clients.proto

package com.halloapp.proto.clients;

/**
 * Protobuf type {@code clients.ChatContext}
 */
public  final class ChatContext extends
    com.google.protobuf.GeneratedMessageLite<
        ChatContext, ChatContext.Builder> implements
    // @@protoc_insertion_point(message_implements:clients.ChatContext)
    ChatContextOrBuilder {
  private ChatContext() {
    feedPostId_ = "";
    chatReplyMessageId_ = "";
    chatReplyMessageSenderId_ = "";
  }
  public static final int FEED_POST_ID_FIELD_NUMBER = 1;
  private java.lang.String feedPostId_;
  /**
   * <code>string feed_post_id = 1;</code>
   * @return The feedPostId.
   */
  @java.lang.Override
  public java.lang.String getFeedPostId() {
    return feedPostId_;
  }
  /**
   * <code>string feed_post_id = 1;</code>
   * @return The bytes for feedPostId.
   */
  @java.lang.Override
  public com.google.protobuf.ByteString
      getFeedPostIdBytes() {
    return com.google.protobuf.ByteString.copyFromUtf8(feedPostId_);
  }
  /**
   * <code>string feed_post_id = 1;</code>
   * @param value The feedPostId to set.
   */
  private void setFeedPostId(
      java.lang.String value) {
    value.getClass();
  
    feedPostId_ = value;
  }
  /**
   * <code>string feed_post_id = 1;</code>
   */
  private void clearFeedPostId() {
    
    feedPostId_ = getDefaultInstance().getFeedPostId();
  }
  /**
   * <code>string feed_post_id = 1;</code>
   * @param value The bytes for feedPostId to set.
   */
  private void setFeedPostIdBytes(
      com.google.protobuf.ByteString value) {
    checkByteStringIsUtf8(value);
    feedPostId_ = value.toStringUtf8();
    
  }

  public static final int FEED_POST_MEDIA_INDEX_FIELD_NUMBER = 2;
  private int feedPostMediaIndex_;
  /**
   * <code>int32 feed_post_media_index = 2;</code>
   * @return The feedPostMediaIndex.
   */
  @java.lang.Override
  public int getFeedPostMediaIndex() {
    return feedPostMediaIndex_;
  }
  /**
   * <code>int32 feed_post_media_index = 2;</code>
   * @param value The feedPostMediaIndex to set.
   */
  private void setFeedPostMediaIndex(int value) {
    
    feedPostMediaIndex_ = value;
  }
  /**
   * <code>int32 feed_post_media_index = 2;</code>
   */
  private void clearFeedPostMediaIndex() {
    
    feedPostMediaIndex_ = 0;
  }

  public static final int CHAT_REPLY_MESSAGE_ID_FIELD_NUMBER = 3;
  private java.lang.String chatReplyMessageId_;
  /**
   * <code>string chat_reply_message_id = 3;</code>
   * @return The chatReplyMessageId.
   */
  @java.lang.Override
  public java.lang.String getChatReplyMessageId() {
    return chatReplyMessageId_;
  }
  /**
   * <code>string chat_reply_message_id = 3;</code>
   * @return The bytes for chatReplyMessageId.
   */
  @java.lang.Override
  public com.google.protobuf.ByteString
      getChatReplyMessageIdBytes() {
    return com.google.protobuf.ByteString.copyFromUtf8(chatReplyMessageId_);
  }
  /**
   * <code>string chat_reply_message_id = 3;</code>
   * @param value The chatReplyMessageId to set.
   */
  private void setChatReplyMessageId(
      java.lang.String value) {
    value.getClass();
  
    chatReplyMessageId_ = value;
  }
  /**
   * <code>string chat_reply_message_id = 3;</code>
   */
  private void clearChatReplyMessageId() {
    
    chatReplyMessageId_ = getDefaultInstance().getChatReplyMessageId();
  }
  /**
   * <code>string chat_reply_message_id = 3;</code>
   * @param value The bytes for chatReplyMessageId to set.
   */
  private void setChatReplyMessageIdBytes(
      com.google.protobuf.ByteString value) {
    checkByteStringIsUtf8(value);
    chatReplyMessageId_ = value.toStringUtf8();
    
  }

  public static final int CHAT_REPLY_MESSAGE_MEDIA_INDEX_FIELD_NUMBER = 4;
  private int chatReplyMessageMediaIndex_;
  /**
   * <code>int32 chat_reply_message_media_index = 4;</code>
   * @return The chatReplyMessageMediaIndex.
   */
  @java.lang.Override
  public int getChatReplyMessageMediaIndex() {
    return chatReplyMessageMediaIndex_;
  }
  /**
   * <code>int32 chat_reply_message_media_index = 4;</code>
   * @param value The chatReplyMessageMediaIndex to set.
   */
  private void setChatReplyMessageMediaIndex(int value) {
    
    chatReplyMessageMediaIndex_ = value;
  }
  /**
   * <code>int32 chat_reply_message_media_index = 4;</code>
   */
  private void clearChatReplyMessageMediaIndex() {
    
    chatReplyMessageMediaIndex_ = 0;
  }

  public static final int CHAT_REPLY_MESSAGE_SENDER_ID_FIELD_NUMBER = 5;
  private java.lang.String chatReplyMessageSenderId_;
  /**
   * <code>string chat_reply_message_sender_id = 5;</code>
   * @return The chatReplyMessageSenderId.
   */
  @java.lang.Override
  public java.lang.String getChatReplyMessageSenderId() {
    return chatReplyMessageSenderId_;
  }
  /**
   * <code>string chat_reply_message_sender_id = 5;</code>
   * @return The bytes for chatReplyMessageSenderId.
   */
  @java.lang.Override
  public com.google.protobuf.ByteString
      getChatReplyMessageSenderIdBytes() {
    return com.google.protobuf.ByteString.copyFromUtf8(chatReplyMessageSenderId_);
  }
  /**
   * <code>string chat_reply_message_sender_id = 5;</code>
   * @param value The chatReplyMessageSenderId to set.
   */
  private void setChatReplyMessageSenderId(
      java.lang.String value) {
    value.getClass();
  
    chatReplyMessageSenderId_ = value;
  }
  /**
   * <code>string chat_reply_message_sender_id = 5;</code>
   */
  private void clearChatReplyMessageSenderId() {
    
    chatReplyMessageSenderId_ = getDefaultInstance().getChatReplyMessageSenderId();
  }
  /**
   * <code>string chat_reply_message_sender_id = 5;</code>
   * @param value The bytes for chatReplyMessageSenderId to set.
   */
  private void setChatReplyMessageSenderIdBytes(
      com.google.protobuf.ByteString value) {
    checkByteStringIsUtf8(value);
    chatReplyMessageSenderId_ = value.toStringUtf8();
    
  }

  public static final int FORWARD_COUNT_FIELD_NUMBER = 6;
  private int forwardCount_;
  /**
   * <code>uint32 forward_count = 6;</code>
   * @return The forwardCount.
   */
  @java.lang.Override
  public int getForwardCount() {
    return forwardCount_;
  }
  /**
   * <code>uint32 forward_count = 6;</code>
   * @param value The forwardCount to set.
   */
  private void setForwardCount(int value) {
    
    forwardCount_ = value;
  }
  /**
   * <code>uint32 forward_count = 6;</code>
   */
  private void clearForwardCount() {
    
    forwardCount_ = 0;
  }

  public static com.halloapp.proto.clients.ChatContext parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static com.halloapp.proto.clients.ChatContext parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static com.halloapp.proto.clients.ChatContext parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static com.halloapp.proto.clients.ChatContext parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static com.halloapp.proto.clients.ChatContext parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static com.halloapp.proto.clients.ChatContext parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static com.halloapp.proto.clients.ChatContext parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input);
  }
  public static com.halloapp.proto.clients.ChatContext parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input, extensionRegistry);
  }
  public static com.halloapp.proto.clients.ChatContext parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return parseDelimitedFrom(DEFAULT_INSTANCE, input);
  }
  public static com.halloapp.proto.clients.ChatContext parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return parseDelimitedFrom(DEFAULT_INSTANCE, input, extensionRegistry);
  }
  public static com.halloapp.proto.clients.ChatContext parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input);
  }
  public static com.halloapp.proto.clients.ChatContext parseFrom(
      com.google.protobuf.CodedInputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input, extensionRegistry);
  }

  public static Builder newBuilder() {
    return (Builder) DEFAULT_INSTANCE.createBuilder();
  }
  public static Builder newBuilder(com.halloapp.proto.clients.ChatContext prototype) {
    return (Builder) DEFAULT_INSTANCE.createBuilder(prototype);
  }

  /**
   * Protobuf type {@code clients.ChatContext}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessageLite.Builder<
        com.halloapp.proto.clients.ChatContext, Builder> implements
      // @@protoc_insertion_point(builder_implements:clients.ChatContext)
      com.halloapp.proto.clients.ChatContextOrBuilder {
    // Construct using com.halloapp.proto.clients.ChatContext.newBuilder()
    private Builder() {
      super(DEFAULT_INSTANCE);
    }


    /**
     * <code>string feed_post_id = 1;</code>
     * @return The feedPostId.
     */
    @java.lang.Override
    public java.lang.String getFeedPostId() {
      return instance.getFeedPostId();
    }
    /**
     * <code>string feed_post_id = 1;</code>
     * @return The bytes for feedPostId.
     */
    @java.lang.Override
    public com.google.protobuf.ByteString
        getFeedPostIdBytes() {
      return instance.getFeedPostIdBytes();
    }
    /**
     * <code>string feed_post_id = 1;</code>
     * @param value The feedPostId to set.
     * @return This builder for chaining.
     */
    public Builder setFeedPostId(
        java.lang.String value) {
      copyOnWrite();
      instance.setFeedPostId(value);
      return this;
    }
    /**
     * <code>string feed_post_id = 1;</code>
     * @return This builder for chaining.
     */
    public Builder clearFeedPostId() {
      copyOnWrite();
      instance.clearFeedPostId();
      return this;
    }
    /**
     * <code>string feed_post_id = 1;</code>
     * @param value The bytes for feedPostId to set.
     * @return This builder for chaining.
     */
    public Builder setFeedPostIdBytes(
        com.google.protobuf.ByteString value) {
      copyOnWrite();
      instance.setFeedPostIdBytes(value);
      return this;
    }

    /**
     * <code>int32 feed_post_media_index = 2;</code>
     * @return The feedPostMediaIndex.
     */
    @java.lang.Override
    public int getFeedPostMediaIndex() {
      return instance.getFeedPostMediaIndex();
    }
    /**
     * <code>int32 feed_post_media_index = 2;</code>
     * @param value The feedPostMediaIndex to set.
     * @return This builder for chaining.
     */
    public Builder setFeedPostMediaIndex(int value) {
      copyOnWrite();
      instance.setFeedPostMediaIndex(value);
      return this;
    }
    /**
     * <code>int32 feed_post_media_index = 2;</code>
     * @return This builder for chaining.
     */
    public Builder clearFeedPostMediaIndex() {
      copyOnWrite();
      instance.clearFeedPostMediaIndex();
      return this;
    }

    /**
     * <code>string chat_reply_message_id = 3;</code>
     * @return The chatReplyMessageId.
     */
    @java.lang.Override
    public java.lang.String getChatReplyMessageId() {
      return instance.getChatReplyMessageId();
    }
    /**
     * <code>string chat_reply_message_id = 3;</code>
     * @return The bytes for chatReplyMessageId.
     */
    @java.lang.Override
    public com.google.protobuf.ByteString
        getChatReplyMessageIdBytes() {
      return instance.getChatReplyMessageIdBytes();
    }
    /**
     * <code>string chat_reply_message_id = 3;</code>
     * @param value The chatReplyMessageId to set.
     * @return This builder for chaining.
     */
    public Builder setChatReplyMessageId(
        java.lang.String value) {
      copyOnWrite();
      instance.setChatReplyMessageId(value);
      return this;
    }
    /**
     * <code>string chat_reply_message_id = 3;</code>
     * @return This builder for chaining.
     */
    public Builder clearChatReplyMessageId() {
      copyOnWrite();
      instance.clearChatReplyMessageId();
      return this;
    }
    /**
     * <code>string chat_reply_message_id = 3;</code>
     * @param value The bytes for chatReplyMessageId to set.
     * @return This builder for chaining.
     */
    public Builder setChatReplyMessageIdBytes(
        com.google.protobuf.ByteString value) {
      copyOnWrite();
      instance.setChatReplyMessageIdBytes(value);
      return this;
    }

    /**
     * <code>int32 chat_reply_message_media_index = 4;</code>
     * @return The chatReplyMessageMediaIndex.
     */
    @java.lang.Override
    public int getChatReplyMessageMediaIndex() {
      return instance.getChatReplyMessageMediaIndex();
    }
    /**
     * <code>int32 chat_reply_message_media_index = 4;</code>
     * @param value The chatReplyMessageMediaIndex to set.
     * @return This builder for chaining.
     */
    public Builder setChatReplyMessageMediaIndex(int value) {
      copyOnWrite();
      instance.setChatReplyMessageMediaIndex(value);
      return this;
    }
    /**
     * <code>int32 chat_reply_message_media_index = 4;</code>
     * @return This builder for chaining.
     */
    public Builder clearChatReplyMessageMediaIndex() {
      copyOnWrite();
      instance.clearChatReplyMessageMediaIndex();
      return this;
    }

    /**
     * <code>string chat_reply_message_sender_id = 5;</code>
     * @return The chatReplyMessageSenderId.
     */
    @java.lang.Override
    public java.lang.String getChatReplyMessageSenderId() {
      return instance.getChatReplyMessageSenderId();
    }
    /**
     * <code>string chat_reply_message_sender_id = 5;</code>
     * @return The bytes for chatReplyMessageSenderId.
     */
    @java.lang.Override
    public com.google.protobuf.ByteString
        getChatReplyMessageSenderIdBytes() {
      return instance.getChatReplyMessageSenderIdBytes();
    }
    /**
     * <code>string chat_reply_message_sender_id = 5;</code>
     * @param value The chatReplyMessageSenderId to set.
     * @return This builder for chaining.
     */
    public Builder setChatReplyMessageSenderId(
        java.lang.String value) {
      copyOnWrite();
      instance.setChatReplyMessageSenderId(value);
      return this;
    }
    /**
     * <code>string chat_reply_message_sender_id = 5;</code>
     * @return This builder for chaining.
     */
    public Builder clearChatReplyMessageSenderId() {
      copyOnWrite();
      instance.clearChatReplyMessageSenderId();
      return this;
    }
    /**
     * <code>string chat_reply_message_sender_id = 5;</code>
     * @param value The bytes for chatReplyMessageSenderId to set.
     * @return This builder for chaining.
     */
    public Builder setChatReplyMessageSenderIdBytes(
        com.google.protobuf.ByteString value) {
      copyOnWrite();
      instance.setChatReplyMessageSenderIdBytes(value);
      return this;
    }

    /**
     * <code>uint32 forward_count = 6;</code>
     * @return The forwardCount.
     */
    @java.lang.Override
    public int getForwardCount() {
      return instance.getForwardCount();
    }
    /**
     * <code>uint32 forward_count = 6;</code>
     * @param value The forwardCount to set.
     * @return This builder for chaining.
     */
    public Builder setForwardCount(int value) {
      copyOnWrite();
      instance.setForwardCount(value);
      return this;
    }
    /**
     * <code>uint32 forward_count = 6;</code>
     * @return This builder for chaining.
     */
    public Builder clearForwardCount() {
      copyOnWrite();
      instance.clearForwardCount();
      return this;
    }

    // @@protoc_insertion_point(builder_scope:clients.ChatContext)
  }
  @java.lang.Override
  @java.lang.SuppressWarnings({"unchecked", "fallthrough"})
  protected final java.lang.Object dynamicMethod(
      com.google.protobuf.GeneratedMessageLite.MethodToInvoke method,
      java.lang.Object arg0, java.lang.Object arg1) {
    switch (method) {
      case NEW_MUTABLE_INSTANCE: {
        return new com.halloapp.proto.clients.ChatContext();
      }
      case NEW_BUILDER: {
        return new Builder();
      }
      case BUILD_MESSAGE_INFO: {
          java.lang.Object[] objects = new java.lang.Object[] {
            "feedPostId_",
            "feedPostMediaIndex_",
            "chatReplyMessageId_",
            "chatReplyMessageMediaIndex_",
            "chatReplyMessageSenderId_",
            "forwardCount_",
          };
          java.lang.String info =
              "\u0000\u0006\u0000\u0000\u0001\u0006\u0006\u0000\u0000\u0000\u0001\u0208\u0002\u0004" +
              "\u0003\u0208\u0004\u0004\u0005\u0208\u0006\u000b";
          return newMessageInfo(DEFAULT_INSTANCE, info, objects);
      }
      // fall through
      case GET_DEFAULT_INSTANCE: {
        return DEFAULT_INSTANCE;
      }
      case GET_PARSER: {
        com.google.protobuf.Parser<com.halloapp.proto.clients.ChatContext> parser = PARSER;
        if (parser == null) {
          synchronized (com.halloapp.proto.clients.ChatContext.class) {
            parser = PARSER;
            if (parser == null) {
              parser =
                  new DefaultInstanceBasedParser<com.halloapp.proto.clients.ChatContext>(
                      DEFAULT_INSTANCE);
              PARSER = parser;
            }
          }
        }
        return parser;
    }
    case GET_MEMOIZED_IS_INITIALIZED: {
      return (byte) 1;
    }
    case SET_MEMOIZED_IS_INITIALIZED: {
      return null;
    }
    }
    throw new UnsupportedOperationException();
  }


  // @@protoc_insertion_point(class_scope:clients.ChatContext)
  private static final com.halloapp.proto.clients.ChatContext DEFAULT_INSTANCE;
  static {
    ChatContext defaultInstance = new ChatContext();
    // New instances are implicitly immutable so no need to make
    // immutable.
    DEFAULT_INSTANCE = defaultInstance;
    com.google.protobuf.GeneratedMessageLite.registerDefaultInstance(
      ChatContext.class, defaultInstance);
  }

  public static com.halloapp.proto.clients.ChatContext getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static volatile com.google.protobuf.Parser<ChatContext> PARSER;

  public static com.google.protobuf.Parser<ChatContext> parser() {
    return DEFAULT_INSTANCE.getParserForType();
  }
}

