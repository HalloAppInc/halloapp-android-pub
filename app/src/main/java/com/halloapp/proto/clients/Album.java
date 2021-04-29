// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: clients.proto

package com.halloapp.proto.clients;

/**
 * Protobuf type {@code clients.Album}
 */
public  final class Album extends
    com.google.protobuf.GeneratedMessageLite<
        Album, Album.Builder> implements
    // @@protoc_insertion_point(message_implements:clients.Album)
    AlbumOrBuilder {
  private Album() {
    media_ = emptyProtobufList();
  }
  public static final int MEDIA_FIELD_NUMBER = 1;
  private com.google.protobuf.Internal.ProtobufList<com.halloapp.proto.clients.AlbumMedia> media_;
  /**
   * <code>repeated .clients.AlbumMedia media = 1;</code>
   */
  @java.lang.Override
  public java.util.List<com.halloapp.proto.clients.AlbumMedia> getMediaList() {
    return media_;
  }
  /**
   * <code>repeated .clients.AlbumMedia media = 1;</code>
   */
  public java.util.List<? extends com.halloapp.proto.clients.AlbumMediaOrBuilder> 
      getMediaOrBuilderList() {
    return media_;
  }
  /**
   * <code>repeated .clients.AlbumMedia media = 1;</code>
   */
  @java.lang.Override
  public int getMediaCount() {
    return media_.size();
  }
  /**
   * <code>repeated .clients.AlbumMedia media = 1;</code>
   */
  @java.lang.Override
  public com.halloapp.proto.clients.AlbumMedia getMedia(int index) {
    return media_.get(index);
  }
  /**
   * <code>repeated .clients.AlbumMedia media = 1;</code>
   */
  public com.halloapp.proto.clients.AlbumMediaOrBuilder getMediaOrBuilder(
      int index) {
    return media_.get(index);
  }
  private void ensureMediaIsMutable() {
    com.google.protobuf.Internal.ProtobufList<com.halloapp.proto.clients.AlbumMedia> tmp = media_;
    if (!tmp.isModifiable()) {
      media_ =
          com.google.protobuf.GeneratedMessageLite.mutableCopy(tmp);
     }
  }

  /**
   * <code>repeated .clients.AlbumMedia media = 1;</code>
   */
  private void setMedia(
      int index, com.halloapp.proto.clients.AlbumMedia value) {
    value.getClass();
  ensureMediaIsMutable();
    media_.set(index, value);
  }
  /**
   * <code>repeated .clients.AlbumMedia media = 1;</code>
   */
  private void addMedia(com.halloapp.proto.clients.AlbumMedia value) {
    value.getClass();
  ensureMediaIsMutable();
    media_.add(value);
  }
  /**
   * <code>repeated .clients.AlbumMedia media = 1;</code>
   */
  private void addMedia(
      int index, com.halloapp.proto.clients.AlbumMedia value) {
    value.getClass();
  ensureMediaIsMutable();
    media_.add(index, value);
  }
  /**
   * <code>repeated .clients.AlbumMedia media = 1;</code>
   */
  private void addAllMedia(
      java.lang.Iterable<? extends com.halloapp.proto.clients.AlbumMedia> values) {
    ensureMediaIsMutable();
    com.google.protobuf.AbstractMessageLite.addAll(
        values, media_);
  }
  /**
   * <code>repeated .clients.AlbumMedia media = 1;</code>
   */
  private void clearMedia() {
    media_ = emptyProtobufList();
  }
  /**
   * <code>repeated .clients.AlbumMedia media = 1;</code>
   */
  private void removeMedia(int index) {
    ensureMediaIsMutable();
    media_.remove(index);
  }

  public static final int TEXT_FIELD_NUMBER = 2;
  private com.halloapp.proto.clients.Text text_;
  /**
   * <code>.clients.Text text = 2;</code>
   */
  @java.lang.Override
  public boolean hasText() {
    return text_ != null;
  }
  /**
   * <code>.clients.Text text = 2;</code>
   */
  @java.lang.Override
  public com.halloapp.proto.clients.Text getText() {
    return text_ == null ? com.halloapp.proto.clients.Text.getDefaultInstance() : text_;
  }
  /**
   * <code>.clients.Text text = 2;</code>
   */
  private void setText(com.halloapp.proto.clients.Text value) {
    value.getClass();
  text_ = value;
    
    }
  /**
   * <code>.clients.Text text = 2;</code>
   */
  @java.lang.SuppressWarnings({"ReferenceEquality"})
  private void mergeText(com.halloapp.proto.clients.Text value) {
    value.getClass();
  if (text_ != null &&
        text_ != com.halloapp.proto.clients.Text.getDefaultInstance()) {
      text_ =
        com.halloapp.proto.clients.Text.newBuilder(text_).mergeFrom(value).buildPartial();
    } else {
      text_ = value;
    }
    
  }
  /**
   * <code>.clients.Text text = 2;</code>
   */
  private void clearText() {  text_ = null;
    
  }

  public static com.halloapp.proto.clients.Album parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static com.halloapp.proto.clients.Album parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static com.halloapp.proto.clients.Album parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static com.halloapp.proto.clients.Album parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static com.halloapp.proto.clients.Album parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static com.halloapp.proto.clients.Album parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static com.halloapp.proto.clients.Album parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input);
  }
  public static com.halloapp.proto.clients.Album parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input, extensionRegistry);
  }
  public static com.halloapp.proto.clients.Album parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return parseDelimitedFrom(DEFAULT_INSTANCE, input);
  }
  public static com.halloapp.proto.clients.Album parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return parseDelimitedFrom(DEFAULT_INSTANCE, input, extensionRegistry);
  }
  public static com.halloapp.proto.clients.Album parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input);
  }
  public static com.halloapp.proto.clients.Album parseFrom(
      com.google.protobuf.CodedInputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input, extensionRegistry);
  }

  public static Builder newBuilder() {
    return (Builder) DEFAULT_INSTANCE.createBuilder();
  }
  public static Builder newBuilder(com.halloapp.proto.clients.Album prototype) {
    return (Builder) DEFAULT_INSTANCE.createBuilder(prototype);
  }

  /**
   * Protobuf type {@code clients.Album}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessageLite.Builder<
        com.halloapp.proto.clients.Album, Builder> implements
      // @@protoc_insertion_point(builder_implements:clients.Album)
      com.halloapp.proto.clients.AlbumOrBuilder {
    // Construct using com.halloapp.proto.clients.Album.newBuilder()
    private Builder() {
      super(DEFAULT_INSTANCE);
    }


    /**
     * <code>repeated .clients.AlbumMedia media = 1;</code>
     */
    @java.lang.Override
    public java.util.List<com.halloapp.proto.clients.AlbumMedia> getMediaList() {
      return java.util.Collections.unmodifiableList(
          instance.getMediaList());
    }
    /**
     * <code>repeated .clients.AlbumMedia media = 1;</code>
     */
    @java.lang.Override
    public int getMediaCount() {
      return instance.getMediaCount();
    }/**
     * <code>repeated .clients.AlbumMedia media = 1;</code>
     */
    @java.lang.Override
    public com.halloapp.proto.clients.AlbumMedia getMedia(int index) {
      return instance.getMedia(index);
    }
    /**
     * <code>repeated .clients.AlbumMedia media = 1;</code>
     */
    public Builder setMedia(
        int index, com.halloapp.proto.clients.AlbumMedia value) {
      copyOnWrite();
      instance.setMedia(index, value);
      return this;
    }
    /**
     * <code>repeated .clients.AlbumMedia media = 1;</code>
     */
    public Builder setMedia(
        int index, com.halloapp.proto.clients.AlbumMedia.Builder builderForValue) {
      copyOnWrite();
      instance.setMedia(index,
          builderForValue.build());
      return this;
    }
    /**
     * <code>repeated .clients.AlbumMedia media = 1;</code>
     */
    public Builder addMedia(com.halloapp.proto.clients.AlbumMedia value) {
      copyOnWrite();
      instance.addMedia(value);
      return this;
    }
    /**
     * <code>repeated .clients.AlbumMedia media = 1;</code>
     */
    public Builder addMedia(
        int index, com.halloapp.proto.clients.AlbumMedia value) {
      copyOnWrite();
      instance.addMedia(index, value);
      return this;
    }
    /**
     * <code>repeated .clients.AlbumMedia media = 1;</code>
     */
    public Builder addMedia(
        com.halloapp.proto.clients.AlbumMedia.Builder builderForValue) {
      copyOnWrite();
      instance.addMedia(builderForValue.build());
      return this;
    }
    /**
     * <code>repeated .clients.AlbumMedia media = 1;</code>
     */
    public Builder addMedia(
        int index, com.halloapp.proto.clients.AlbumMedia.Builder builderForValue) {
      copyOnWrite();
      instance.addMedia(index,
          builderForValue.build());
      return this;
    }
    /**
     * <code>repeated .clients.AlbumMedia media = 1;</code>
     */
    public Builder addAllMedia(
        java.lang.Iterable<? extends com.halloapp.proto.clients.AlbumMedia> values) {
      copyOnWrite();
      instance.addAllMedia(values);
      return this;
    }
    /**
     * <code>repeated .clients.AlbumMedia media = 1;</code>
     */
    public Builder clearMedia() {
      copyOnWrite();
      instance.clearMedia();
      return this;
    }
    /**
     * <code>repeated .clients.AlbumMedia media = 1;</code>
     */
    public Builder removeMedia(int index) {
      copyOnWrite();
      instance.removeMedia(index);
      return this;
    }

    /**
     * <code>.clients.Text text = 2;</code>
     */
    @java.lang.Override
    public boolean hasText() {
      return instance.hasText();
    }
    /**
     * <code>.clients.Text text = 2;</code>
     */
    @java.lang.Override
    public com.halloapp.proto.clients.Text getText() {
      return instance.getText();
    }
    /**
     * <code>.clients.Text text = 2;</code>
     */
    public Builder setText(com.halloapp.proto.clients.Text value) {
      copyOnWrite();
      instance.setText(value);
      return this;
      }
    /**
     * <code>.clients.Text text = 2;</code>
     */
    public Builder setText(
        com.halloapp.proto.clients.Text.Builder builderForValue) {
      copyOnWrite();
      instance.setText(builderForValue.build());
      return this;
    }
    /**
     * <code>.clients.Text text = 2;</code>
     */
    public Builder mergeText(com.halloapp.proto.clients.Text value) {
      copyOnWrite();
      instance.mergeText(value);
      return this;
    }
    /**
     * <code>.clients.Text text = 2;</code>
     */
    public Builder clearText() {  copyOnWrite();
      instance.clearText();
      return this;
    }

    // @@protoc_insertion_point(builder_scope:clients.Album)
  }
  @java.lang.Override
  @java.lang.SuppressWarnings({"unchecked", "fallthrough"})
  protected final java.lang.Object dynamicMethod(
      com.google.protobuf.GeneratedMessageLite.MethodToInvoke method,
      java.lang.Object arg0, java.lang.Object arg1) {
    switch (method) {
      case NEW_MUTABLE_INSTANCE: {
        return new com.halloapp.proto.clients.Album();
      }
      case NEW_BUILDER: {
        return new Builder();
      }
      case BUILD_MESSAGE_INFO: {
          java.lang.Object[] objects = new java.lang.Object[] {
            "media_",
            com.halloapp.proto.clients.AlbumMedia.class,
            "text_",
          };
          java.lang.String info =
              "\u0000\u0002\u0000\u0000\u0001\u0002\u0002\u0000\u0001\u0000\u0001\u001b\u0002\t" +
              "";
          return newMessageInfo(DEFAULT_INSTANCE, info, objects);
      }
      // fall through
      case GET_DEFAULT_INSTANCE: {
        return DEFAULT_INSTANCE;
      }
      case GET_PARSER: {
        com.google.protobuf.Parser<com.halloapp.proto.clients.Album> parser = PARSER;
        if (parser == null) {
          synchronized (com.halloapp.proto.clients.Album.class) {
            parser = PARSER;
            if (parser == null) {
              parser =
                  new DefaultInstanceBasedParser<com.halloapp.proto.clients.Album>(
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


  // @@protoc_insertion_point(class_scope:clients.Album)
  private static final com.halloapp.proto.clients.Album DEFAULT_INSTANCE;
  static {
    Album defaultInstance = new Album();
    // New instances are implicitly immutable so no need to make
    // immutable.
    DEFAULT_INSTANCE = defaultInstance;
    com.google.protobuf.GeneratedMessageLite.registerDefaultInstance(
      Album.class, defaultInstance);
  }

  public static com.halloapp.proto.clients.Album getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static volatile com.google.protobuf.Parser<Album> PARSER;

  public static com.google.protobuf.Parser<Album> parser() {
    return DEFAULT_INSTANCE.getParserForType();
  }
}

