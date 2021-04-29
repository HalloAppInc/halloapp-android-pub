// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: clients.proto

package com.halloapp.proto.clients;

/**
 * Protobuf type {@code clients.PostContainer}
 */
public  final class PostContainer extends
    com.google.protobuf.GeneratedMessageLite<
        PostContainer, PostContainer.Builder> implements
    // @@protoc_insertion_point(message_implements:clients.PostContainer)
    PostContainerOrBuilder {
  private PostContainer() {
  }
  private int postCase_ = 0;
  private java.lang.Object post_;
  public enum PostCase {
    TEXT(1),
    ALBUM(2),
    POST_NOT_SET(0);
    private final int value;
    private PostCase(int value) {
      this.value = value;
    }
    /**
     * @deprecated Use {@link #forNumber(int)} instead.
     */
    @java.lang.Deprecated
    public static PostCase valueOf(int value) {
      return forNumber(value);
    }

    public static PostCase forNumber(int value) {
      switch (value) {
        case 1: return TEXT;
        case 2: return ALBUM;
        case 0: return POST_NOT_SET;
        default: return null;
      }
    }
    public int getNumber() {
      return this.value;
    }
  };

  @java.lang.Override
  public PostCase
  getPostCase() {
    return PostCase.forNumber(
        postCase_);
  }

  private void clearPost() {
    postCase_ = 0;
    post_ = null;
  }

  public static final int TEXT_FIELD_NUMBER = 1;
  /**
   * <code>.clients.Text text = 1;</code>
   */
  @java.lang.Override
  public boolean hasText() {
    return postCase_ == 1;
  }
  /**
   * <code>.clients.Text text = 1;</code>
   */
  @java.lang.Override
  public com.halloapp.proto.clients.Text getText() {
    if (postCase_ == 1) {
       return (com.halloapp.proto.clients.Text) post_;
    }
    return com.halloapp.proto.clients.Text.getDefaultInstance();
  }
  /**
   * <code>.clients.Text text = 1;</code>
   */
  private void setText(com.halloapp.proto.clients.Text value) {
    value.getClass();
  post_ = value;
    postCase_ = 1;
  }
  /**
   * <code>.clients.Text text = 1;</code>
   */
  private void mergeText(com.halloapp.proto.clients.Text value) {
    value.getClass();
  if (postCase_ == 1 &&
        post_ != com.halloapp.proto.clients.Text.getDefaultInstance()) {
      post_ = com.halloapp.proto.clients.Text.newBuilder((com.halloapp.proto.clients.Text) post_)
          .mergeFrom(value).buildPartial();
    } else {
      post_ = value;
    }
    postCase_ = 1;
  }
  /**
   * <code>.clients.Text text = 1;</code>
   */
  private void clearText() {
    if (postCase_ == 1) {
      postCase_ = 0;
      post_ = null;
    }
  }

  public static final int ALBUM_FIELD_NUMBER = 2;
  /**
   * <code>.clients.Album album = 2;</code>
   */
  @java.lang.Override
  public boolean hasAlbum() {
    return postCase_ == 2;
  }
  /**
   * <code>.clients.Album album = 2;</code>
   */
  @java.lang.Override
  public com.halloapp.proto.clients.Album getAlbum() {
    if (postCase_ == 2) {
       return (com.halloapp.proto.clients.Album) post_;
    }
    return com.halloapp.proto.clients.Album.getDefaultInstance();
  }
  /**
   * <code>.clients.Album album = 2;</code>
   */
  private void setAlbum(com.halloapp.proto.clients.Album value) {
    value.getClass();
  post_ = value;
    postCase_ = 2;
  }
  /**
   * <code>.clients.Album album = 2;</code>
   */
  private void mergeAlbum(com.halloapp.proto.clients.Album value) {
    value.getClass();
  if (postCase_ == 2 &&
        post_ != com.halloapp.proto.clients.Album.getDefaultInstance()) {
      post_ = com.halloapp.proto.clients.Album.newBuilder((com.halloapp.proto.clients.Album) post_)
          .mergeFrom(value).buildPartial();
    } else {
      post_ = value;
    }
    postCase_ = 2;
  }
  /**
   * <code>.clients.Album album = 2;</code>
   */
  private void clearAlbum() {
    if (postCase_ == 2) {
      postCase_ = 0;
      post_ = null;
    }
  }

  public static com.halloapp.proto.clients.PostContainer parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static com.halloapp.proto.clients.PostContainer parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static com.halloapp.proto.clients.PostContainer parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static com.halloapp.proto.clients.PostContainer parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static com.halloapp.proto.clients.PostContainer parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static com.halloapp.proto.clients.PostContainer parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static com.halloapp.proto.clients.PostContainer parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input);
  }
  public static com.halloapp.proto.clients.PostContainer parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input, extensionRegistry);
  }
  public static com.halloapp.proto.clients.PostContainer parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return parseDelimitedFrom(DEFAULT_INSTANCE, input);
  }
  public static com.halloapp.proto.clients.PostContainer parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return parseDelimitedFrom(DEFAULT_INSTANCE, input, extensionRegistry);
  }
  public static com.halloapp.proto.clients.PostContainer parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input);
  }
  public static com.halloapp.proto.clients.PostContainer parseFrom(
      com.google.protobuf.CodedInputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input, extensionRegistry);
  }

  public static Builder newBuilder() {
    return (Builder) DEFAULT_INSTANCE.createBuilder();
  }
  public static Builder newBuilder(com.halloapp.proto.clients.PostContainer prototype) {
    return (Builder) DEFAULT_INSTANCE.createBuilder(prototype);
  }

  /**
   * Protobuf type {@code clients.PostContainer}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessageLite.Builder<
        com.halloapp.proto.clients.PostContainer, Builder> implements
      // @@protoc_insertion_point(builder_implements:clients.PostContainer)
      com.halloapp.proto.clients.PostContainerOrBuilder {
    // Construct using com.halloapp.proto.clients.PostContainer.newBuilder()
    private Builder() {
      super(DEFAULT_INSTANCE);
    }

    @java.lang.Override
    public PostCase
        getPostCase() {
      return instance.getPostCase();
    }

    public Builder clearPost() {
      copyOnWrite();
      instance.clearPost();
      return this;
    }


    /**
     * <code>.clients.Text text = 1;</code>
     */
    @java.lang.Override
    public boolean hasText() {
      return instance.hasText();
    }
    /**
     * <code>.clients.Text text = 1;</code>
     */
    @java.lang.Override
    public com.halloapp.proto.clients.Text getText() {
      return instance.getText();
    }
    /**
     * <code>.clients.Text text = 1;</code>
     */
    public Builder setText(com.halloapp.proto.clients.Text value) {
      copyOnWrite();
      instance.setText(value);
      return this;
    }
    /**
     * <code>.clients.Text text = 1;</code>
     */
    public Builder setText(
        com.halloapp.proto.clients.Text.Builder builderForValue) {
      copyOnWrite();
      instance.setText(builderForValue.build());
      return this;
    }
    /**
     * <code>.clients.Text text = 1;</code>
     */
    public Builder mergeText(com.halloapp.proto.clients.Text value) {
      copyOnWrite();
      instance.mergeText(value);
      return this;
    }
    /**
     * <code>.clients.Text text = 1;</code>
     */
    public Builder clearText() {
      copyOnWrite();
      instance.clearText();
      return this;
    }

    /**
     * <code>.clients.Album album = 2;</code>
     */
    @java.lang.Override
    public boolean hasAlbum() {
      return instance.hasAlbum();
    }
    /**
     * <code>.clients.Album album = 2;</code>
     */
    @java.lang.Override
    public com.halloapp.proto.clients.Album getAlbum() {
      return instance.getAlbum();
    }
    /**
     * <code>.clients.Album album = 2;</code>
     */
    public Builder setAlbum(com.halloapp.proto.clients.Album value) {
      copyOnWrite();
      instance.setAlbum(value);
      return this;
    }
    /**
     * <code>.clients.Album album = 2;</code>
     */
    public Builder setAlbum(
        com.halloapp.proto.clients.Album.Builder builderForValue) {
      copyOnWrite();
      instance.setAlbum(builderForValue.build());
      return this;
    }
    /**
     * <code>.clients.Album album = 2;</code>
     */
    public Builder mergeAlbum(com.halloapp.proto.clients.Album value) {
      copyOnWrite();
      instance.mergeAlbum(value);
      return this;
    }
    /**
     * <code>.clients.Album album = 2;</code>
     */
    public Builder clearAlbum() {
      copyOnWrite();
      instance.clearAlbum();
      return this;
    }

    // @@protoc_insertion_point(builder_scope:clients.PostContainer)
  }
  @java.lang.Override
  @java.lang.SuppressWarnings({"unchecked", "fallthrough"})
  protected final java.lang.Object dynamicMethod(
      com.google.protobuf.GeneratedMessageLite.MethodToInvoke method,
      java.lang.Object arg0, java.lang.Object arg1) {
    switch (method) {
      case NEW_MUTABLE_INSTANCE: {
        return new com.halloapp.proto.clients.PostContainer();
      }
      case NEW_BUILDER: {
        return new Builder();
      }
      case BUILD_MESSAGE_INFO: {
          java.lang.Object[] objects = new java.lang.Object[] {
            "post_",
            "postCase_",
            com.halloapp.proto.clients.Text.class,
            com.halloapp.proto.clients.Album.class,
          };
          java.lang.String info =
              "\u0000\u0002\u0001\u0000\u0001\u0002\u0002\u0000\u0000\u0000\u0001<\u0000\u0002<" +
              "\u0000";
          return newMessageInfo(DEFAULT_INSTANCE, info, objects);
      }
      // fall through
      case GET_DEFAULT_INSTANCE: {
        return DEFAULT_INSTANCE;
      }
      case GET_PARSER: {
        com.google.protobuf.Parser<com.halloapp.proto.clients.PostContainer> parser = PARSER;
        if (parser == null) {
          synchronized (com.halloapp.proto.clients.PostContainer.class) {
            parser = PARSER;
            if (parser == null) {
              parser =
                  new DefaultInstanceBasedParser<com.halloapp.proto.clients.PostContainer>(
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


  // @@protoc_insertion_point(class_scope:clients.PostContainer)
  private static final com.halloapp.proto.clients.PostContainer DEFAULT_INSTANCE;
  static {
    PostContainer defaultInstance = new PostContainer();
    // New instances are implicitly immutable so no need to make
    // immutable.
    DEFAULT_INSTANCE = defaultInstance;
    com.google.protobuf.GeneratedMessageLite.registerDefaultInstance(
      PostContainer.class, defaultInstance);
  }

  public static com.halloapp.proto.clients.PostContainer getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static volatile com.google.protobuf.Parser<PostContainer> PARSER;

  public static com.google.protobuf.Parser<PostContainer> parser() {
    return DEFAULT_INSTANCE.getParserForType();
  }
}

