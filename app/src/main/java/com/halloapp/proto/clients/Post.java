// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: clients.proto

package com.halloapp.proto.clients;

/**
 * Protobuf type {@code clients.Post}
 */
public  final class Post extends
    com.google.protobuf.GeneratedMessageLite<
        Post, Post.Builder> implements
    // @@protoc_insertion_point(message_implements:clients.Post)
    PostOrBuilder {
  private Post() {
    media_ = emptyProtobufList();
    text_ = "";
    mentions_ = emptyProtobufList();
  }
  public static final int MEDIA_FIELD_NUMBER = 1;
  private com.google.protobuf.Internal.ProtobufList<com.halloapp.proto.clients.Media> media_;
  /**
   * <code>repeated .clients.Media media = 1;</code>
   */
  @java.lang.Override
  public java.util.List<com.halloapp.proto.clients.Media> getMediaList() {
    return media_;
  }
  /**
   * <code>repeated .clients.Media media = 1;</code>
   */
  public java.util.List<? extends com.halloapp.proto.clients.MediaOrBuilder> 
      getMediaOrBuilderList() {
    return media_;
  }
  /**
   * <code>repeated .clients.Media media = 1;</code>
   */
  @java.lang.Override
  public int getMediaCount() {
    return media_.size();
  }
  /**
   * <code>repeated .clients.Media media = 1;</code>
   */
  @java.lang.Override
  public com.halloapp.proto.clients.Media getMedia(int index) {
    return media_.get(index);
  }
  /**
   * <code>repeated .clients.Media media = 1;</code>
   */
  public com.halloapp.proto.clients.MediaOrBuilder getMediaOrBuilder(
      int index) {
    return media_.get(index);
  }
  private void ensureMediaIsMutable() {
    com.google.protobuf.Internal.ProtobufList<com.halloapp.proto.clients.Media> tmp = media_;
    if (!tmp.isModifiable()) {
      media_ =
          com.google.protobuf.GeneratedMessageLite.mutableCopy(tmp);
     }
  }

  /**
   * <code>repeated .clients.Media media = 1;</code>
   */
  private void setMedia(
      int index, com.halloapp.proto.clients.Media value) {
    value.getClass();
  ensureMediaIsMutable();
    media_.set(index, value);
  }
  /**
   * <code>repeated .clients.Media media = 1;</code>
   */
  private void addMedia(com.halloapp.proto.clients.Media value) {
    value.getClass();
  ensureMediaIsMutable();
    media_.add(value);
  }
  /**
   * <code>repeated .clients.Media media = 1;</code>
   */
  private void addMedia(
      int index, com.halloapp.proto.clients.Media value) {
    value.getClass();
  ensureMediaIsMutable();
    media_.add(index, value);
  }
  /**
   * <code>repeated .clients.Media media = 1;</code>
   */
  private void addAllMedia(
      java.lang.Iterable<? extends com.halloapp.proto.clients.Media> values) {
    ensureMediaIsMutable();
    com.google.protobuf.AbstractMessageLite.addAll(
        values, media_);
  }
  /**
   * <code>repeated .clients.Media media = 1;</code>
   */
  private void clearMedia() {
    media_ = emptyProtobufList();
  }
  /**
   * <code>repeated .clients.Media media = 1;</code>
   */
  private void removeMedia(int index) {
    ensureMediaIsMutable();
    media_.remove(index);
  }

  public static final int TEXT_FIELD_NUMBER = 2;
  private java.lang.String text_;
  /**
   * <code>string text = 2;</code>
   * @return The text.
   */
  @java.lang.Override
  public java.lang.String getText() {
    return text_;
  }
  /**
   * <code>string text = 2;</code>
   * @return The bytes for text.
   */
  @java.lang.Override
  public com.google.protobuf.ByteString
      getTextBytes() {
    return com.google.protobuf.ByteString.copyFromUtf8(text_);
  }
  /**
   * <code>string text = 2;</code>
   * @param value The text to set.
   */
  private void setText(
      java.lang.String value) {
    value.getClass();
  
    text_ = value;
  }
  /**
   * <code>string text = 2;</code>
   */
  private void clearText() {
    
    text_ = getDefaultInstance().getText();
  }
  /**
   * <code>string text = 2;</code>
   * @param value The bytes for text to set.
   */
  private void setTextBytes(
      com.google.protobuf.ByteString value) {
    checkByteStringIsUtf8(value);
    text_ = value.toStringUtf8();
    
  }

  public static final int MENTIONS_FIELD_NUMBER = 3;
  private com.google.protobuf.Internal.ProtobufList<com.halloapp.proto.clients.Mention> mentions_;
  /**
   * <code>repeated .clients.Mention mentions = 3;</code>
   */
  @java.lang.Override
  public java.util.List<com.halloapp.proto.clients.Mention> getMentionsList() {
    return mentions_;
  }
  /**
   * <code>repeated .clients.Mention mentions = 3;</code>
   */
  public java.util.List<? extends com.halloapp.proto.clients.MentionOrBuilder> 
      getMentionsOrBuilderList() {
    return mentions_;
  }
  /**
   * <code>repeated .clients.Mention mentions = 3;</code>
   */
  @java.lang.Override
  public int getMentionsCount() {
    return mentions_.size();
  }
  /**
   * <code>repeated .clients.Mention mentions = 3;</code>
   */
  @java.lang.Override
  public com.halloapp.proto.clients.Mention getMentions(int index) {
    return mentions_.get(index);
  }
  /**
   * <code>repeated .clients.Mention mentions = 3;</code>
   */
  public com.halloapp.proto.clients.MentionOrBuilder getMentionsOrBuilder(
      int index) {
    return mentions_.get(index);
  }
  private void ensureMentionsIsMutable() {
    com.google.protobuf.Internal.ProtobufList<com.halloapp.proto.clients.Mention> tmp = mentions_;
    if (!tmp.isModifiable()) {
      mentions_ =
          com.google.protobuf.GeneratedMessageLite.mutableCopy(tmp);
     }
  }

  /**
   * <code>repeated .clients.Mention mentions = 3;</code>
   */
  private void setMentions(
      int index, com.halloapp.proto.clients.Mention value) {
    value.getClass();
  ensureMentionsIsMutable();
    mentions_.set(index, value);
  }
  /**
   * <code>repeated .clients.Mention mentions = 3;</code>
   */
  private void addMentions(com.halloapp.proto.clients.Mention value) {
    value.getClass();
  ensureMentionsIsMutable();
    mentions_.add(value);
  }
  /**
   * <code>repeated .clients.Mention mentions = 3;</code>
   */
  private void addMentions(
      int index, com.halloapp.proto.clients.Mention value) {
    value.getClass();
  ensureMentionsIsMutable();
    mentions_.add(index, value);
  }
  /**
   * <code>repeated .clients.Mention mentions = 3;</code>
   */
  private void addAllMentions(
      java.lang.Iterable<? extends com.halloapp.proto.clients.Mention> values) {
    ensureMentionsIsMutable();
    com.google.protobuf.AbstractMessageLite.addAll(
        values, mentions_);
  }
  /**
   * <code>repeated .clients.Mention mentions = 3;</code>
   */
  private void clearMentions() {
    mentions_ = emptyProtobufList();
  }
  /**
   * <code>repeated .clients.Mention mentions = 3;</code>
   */
  private void removeMentions(int index) {
    ensureMentionsIsMutable();
    mentions_.remove(index);
  }

  public static com.halloapp.proto.clients.Post parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static com.halloapp.proto.clients.Post parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static com.halloapp.proto.clients.Post parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static com.halloapp.proto.clients.Post parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static com.halloapp.proto.clients.Post parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static com.halloapp.proto.clients.Post parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static com.halloapp.proto.clients.Post parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input);
  }
  public static com.halloapp.proto.clients.Post parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input, extensionRegistry);
  }
  public static com.halloapp.proto.clients.Post parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return parseDelimitedFrom(DEFAULT_INSTANCE, input);
  }
  public static com.halloapp.proto.clients.Post parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return parseDelimitedFrom(DEFAULT_INSTANCE, input, extensionRegistry);
  }
  public static com.halloapp.proto.clients.Post parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input);
  }
  public static com.halloapp.proto.clients.Post parseFrom(
      com.google.protobuf.CodedInputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input, extensionRegistry);
  }

  public static Builder newBuilder() {
    return (Builder) DEFAULT_INSTANCE.createBuilder();
  }
  public static Builder newBuilder(com.halloapp.proto.clients.Post prototype) {
    return (Builder) DEFAULT_INSTANCE.createBuilder(prototype);
  }

  /**
   * Protobuf type {@code clients.Post}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessageLite.Builder<
        com.halloapp.proto.clients.Post, Builder> implements
      // @@protoc_insertion_point(builder_implements:clients.Post)
      com.halloapp.proto.clients.PostOrBuilder {
    // Construct using com.halloapp.proto.clients.Post.newBuilder()
    private Builder() {
      super(DEFAULT_INSTANCE);
    }


    /**
     * <code>repeated .clients.Media media = 1;</code>
     */
    @java.lang.Override
    public java.util.List<com.halloapp.proto.clients.Media> getMediaList() {
      return java.util.Collections.unmodifiableList(
          instance.getMediaList());
    }
    /**
     * <code>repeated .clients.Media media = 1;</code>
     */
    @java.lang.Override
    public int getMediaCount() {
      return instance.getMediaCount();
    }/**
     * <code>repeated .clients.Media media = 1;</code>
     */
    @java.lang.Override
    public com.halloapp.proto.clients.Media getMedia(int index) {
      return instance.getMedia(index);
    }
    /**
     * <code>repeated .clients.Media media = 1;</code>
     */
    public Builder setMedia(
        int index, com.halloapp.proto.clients.Media value) {
      copyOnWrite();
      instance.setMedia(index, value);
      return this;
    }
    /**
     * <code>repeated .clients.Media media = 1;</code>
     */
    public Builder setMedia(
        int index, com.halloapp.proto.clients.Media.Builder builderForValue) {
      copyOnWrite();
      instance.setMedia(index,
          builderForValue.build());
      return this;
    }
    /**
     * <code>repeated .clients.Media media = 1;</code>
     */
    public Builder addMedia(com.halloapp.proto.clients.Media value) {
      copyOnWrite();
      instance.addMedia(value);
      return this;
    }
    /**
     * <code>repeated .clients.Media media = 1;</code>
     */
    public Builder addMedia(
        int index, com.halloapp.proto.clients.Media value) {
      copyOnWrite();
      instance.addMedia(index, value);
      return this;
    }
    /**
     * <code>repeated .clients.Media media = 1;</code>
     */
    public Builder addMedia(
        com.halloapp.proto.clients.Media.Builder builderForValue) {
      copyOnWrite();
      instance.addMedia(builderForValue.build());
      return this;
    }
    /**
     * <code>repeated .clients.Media media = 1;</code>
     */
    public Builder addMedia(
        int index, com.halloapp.proto.clients.Media.Builder builderForValue) {
      copyOnWrite();
      instance.addMedia(index,
          builderForValue.build());
      return this;
    }
    /**
     * <code>repeated .clients.Media media = 1;</code>
     */
    public Builder addAllMedia(
        java.lang.Iterable<? extends com.halloapp.proto.clients.Media> values) {
      copyOnWrite();
      instance.addAllMedia(values);
      return this;
    }
    /**
     * <code>repeated .clients.Media media = 1;</code>
     */
    public Builder clearMedia() {
      copyOnWrite();
      instance.clearMedia();
      return this;
    }
    /**
     * <code>repeated .clients.Media media = 1;</code>
     */
    public Builder removeMedia(int index) {
      copyOnWrite();
      instance.removeMedia(index);
      return this;
    }

    /**
     * <code>string text = 2;</code>
     * @return The text.
     */
    @java.lang.Override
    public java.lang.String getText() {
      return instance.getText();
    }
    /**
     * <code>string text = 2;</code>
     * @return The bytes for text.
     */
    @java.lang.Override
    public com.google.protobuf.ByteString
        getTextBytes() {
      return instance.getTextBytes();
    }
    /**
     * <code>string text = 2;</code>
     * @param value The text to set.
     * @return This builder for chaining.
     */
    public Builder setText(
        java.lang.String value) {
      copyOnWrite();
      instance.setText(value);
      return this;
    }
    /**
     * <code>string text = 2;</code>
     * @return This builder for chaining.
     */
    public Builder clearText() {
      copyOnWrite();
      instance.clearText();
      return this;
    }
    /**
     * <code>string text = 2;</code>
     * @param value The bytes for text to set.
     * @return This builder for chaining.
     */
    public Builder setTextBytes(
        com.google.protobuf.ByteString value) {
      copyOnWrite();
      instance.setTextBytes(value);
      return this;
    }

    /**
     * <code>repeated .clients.Mention mentions = 3;</code>
     */
    @java.lang.Override
    public java.util.List<com.halloapp.proto.clients.Mention> getMentionsList() {
      return java.util.Collections.unmodifiableList(
          instance.getMentionsList());
    }
    /**
     * <code>repeated .clients.Mention mentions = 3;</code>
     */
    @java.lang.Override
    public int getMentionsCount() {
      return instance.getMentionsCount();
    }/**
     * <code>repeated .clients.Mention mentions = 3;</code>
     */
    @java.lang.Override
    public com.halloapp.proto.clients.Mention getMentions(int index) {
      return instance.getMentions(index);
    }
    /**
     * <code>repeated .clients.Mention mentions = 3;</code>
     */
    public Builder setMentions(
        int index, com.halloapp.proto.clients.Mention value) {
      copyOnWrite();
      instance.setMentions(index, value);
      return this;
    }
    /**
     * <code>repeated .clients.Mention mentions = 3;</code>
     */
    public Builder setMentions(
        int index, com.halloapp.proto.clients.Mention.Builder builderForValue) {
      copyOnWrite();
      instance.setMentions(index,
          builderForValue.build());
      return this;
    }
    /**
     * <code>repeated .clients.Mention mentions = 3;</code>
     */
    public Builder addMentions(com.halloapp.proto.clients.Mention value) {
      copyOnWrite();
      instance.addMentions(value);
      return this;
    }
    /**
     * <code>repeated .clients.Mention mentions = 3;</code>
     */
    public Builder addMentions(
        int index, com.halloapp.proto.clients.Mention value) {
      copyOnWrite();
      instance.addMentions(index, value);
      return this;
    }
    /**
     * <code>repeated .clients.Mention mentions = 3;</code>
     */
    public Builder addMentions(
        com.halloapp.proto.clients.Mention.Builder builderForValue) {
      copyOnWrite();
      instance.addMentions(builderForValue.build());
      return this;
    }
    /**
     * <code>repeated .clients.Mention mentions = 3;</code>
     */
    public Builder addMentions(
        int index, com.halloapp.proto.clients.Mention.Builder builderForValue) {
      copyOnWrite();
      instance.addMentions(index,
          builderForValue.build());
      return this;
    }
    /**
     * <code>repeated .clients.Mention mentions = 3;</code>
     */
    public Builder addAllMentions(
        java.lang.Iterable<? extends com.halloapp.proto.clients.Mention> values) {
      copyOnWrite();
      instance.addAllMentions(values);
      return this;
    }
    /**
     * <code>repeated .clients.Mention mentions = 3;</code>
     */
    public Builder clearMentions() {
      copyOnWrite();
      instance.clearMentions();
      return this;
    }
    /**
     * <code>repeated .clients.Mention mentions = 3;</code>
     */
    public Builder removeMentions(int index) {
      copyOnWrite();
      instance.removeMentions(index);
      return this;
    }

    // @@protoc_insertion_point(builder_scope:clients.Post)
  }
  @java.lang.Override
  @java.lang.SuppressWarnings({"unchecked", "fallthrough"})
  protected final java.lang.Object dynamicMethod(
      com.google.protobuf.GeneratedMessageLite.MethodToInvoke method,
      java.lang.Object arg0, java.lang.Object arg1) {
    switch (method) {
      case NEW_MUTABLE_INSTANCE: {
        return new com.halloapp.proto.clients.Post();
      }
      case NEW_BUILDER: {
        return new Builder();
      }
      case BUILD_MESSAGE_INFO: {
          java.lang.Object[] objects = new java.lang.Object[] {
            "media_",
            com.halloapp.proto.clients.Media.class,
            "text_",
            "mentions_",
            com.halloapp.proto.clients.Mention.class,
          };
          java.lang.String info =
              "\u0000\u0003\u0000\u0000\u0001\u0003\u0003\u0000\u0002\u0000\u0001\u001b\u0002\u0208" +
              "\u0003\u001b";
          return newMessageInfo(DEFAULT_INSTANCE, info, objects);
      }
      // fall through
      case GET_DEFAULT_INSTANCE: {
        return DEFAULT_INSTANCE;
      }
      case GET_PARSER: {
        com.google.protobuf.Parser<com.halloapp.proto.clients.Post> parser = PARSER;
        if (parser == null) {
          synchronized (com.halloapp.proto.clients.Post.class) {
            parser = PARSER;
            if (parser == null) {
              parser =
                  new DefaultInstanceBasedParser<com.halloapp.proto.clients.Post>(
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


  // @@protoc_insertion_point(class_scope:clients.Post)
  private static final com.halloapp.proto.clients.Post DEFAULT_INSTANCE;
  static {
    Post defaultInstance = new Post();
    // New instances are implicitly immutable so no need to make
    // immutable.
    DEFAULT_INSTANCE = defaultInstance;
    com.google.protobuf.GeneratedMessageLite.registerDefaultInstance(
      Post.class, defaultInstance);
  }

  public static com.halloapp.proto.clients.Post getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static volatile com.google.protobuf.Parser<Post> PARSER;

  public static com.google.protobuf.Parser<Post> parser() {
    return DEFAULT_INSTANCE.getParserForType();
  }
}

