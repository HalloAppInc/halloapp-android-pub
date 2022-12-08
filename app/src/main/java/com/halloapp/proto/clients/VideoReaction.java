// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: clients.proto

package com.halloapp.proto.clients;

/**
 * Protobuf type {@code clients.VideoReaction}
 */
public  final class VideoReaction extends
    com.google.protobuf.GeneratedMessageLite<
        VideoReaction, VideoReaction.Builder> implements
    // @@protoc_insertion_point(message_implements:clients.VideoReaction)
    VideoReactionOrBuilder {
  private VideoReaction() {
  }
  public static final int VIDEO_FIELD_NUMBER = 1;
  private com.halloapp.proto.clients.Video video_;
  /**
   * <code>.clients.Video video = 1;</code>
   */
  @java.lang.Override
  public boolean hasVideo() {
    return video_ != null;
  }
  /**
   * <code>.clients.Video video = 1;</code>
   */
  @java.lang.Override
  public com.halloapp.proto.clients.Video getVideo() {
    return video_ == null ? com.halloapp.proto.clients.Video.getDefaultInstance() : video_;
  }
  /**
   * <code>.clients.Video video = 1;</code>
   */
  private void setVideo(com.halloapp.proto.clients.Video value) {
    value.getClass();
  video_ = value;
    
    }
  /**
   * <code>.clients.Video video = 1;</code>
   */
  @java.lang.SuppressWarnings({"ReferenceEquality"})
  private void mergeVideo(com.halloapp.proto.clients.Video value) {
    value.getClass();
  if (video_ != null &&
        video_ != com.halloapp.proto.clients.Video.getDefaultInstance()) {
      video_ =
        com.halloapp.proto.clients.Video.newBuilder(video_).mergeFrom(value).buildPartial();
    } else {
      video_ = value;
    }
    
  }
  /**
   * <code>.clients.Video video = 1;</code>
   */
  private void clearVideo() {  video_ = null;
    
  }

  public static com.halloapp.proto.clients.VideoReaction parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static com.halloapp.proto.clients.VideoReaction parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static com.halloapp.proto.clients.VideoReaction parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static com.halloapp.proto.clients.VideoReaction parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static com.halloapp.proto.clients.VideoReaction parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static com.halloapp.proto.clients.VideoReaction parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static com.halloapp.proto.clients.VideoReaction parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input);
  }
  public static com.halloapp.proto.clients.VideoReaction parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input, extensionRegistry);
  }
  public static com.halloapp.proto.clients.VideoReaction parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return parseDelimitedFrom(DEFAULT_INSTANCE, input);
  }
  public static com.halloapp.proto.clients.VideoReaction parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return parseDelimitedFrom(DEFAULT_INSTANCE, input, extensionRegistry);
  }
  public static com.halloapp.proto.clients.VideoReaction parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input);
  }
  public static com.halloapp.proto.clients.VideoReaction parseFrom(
      com.google.protobuf.CodedInputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input, extensionRegistry);
  }

  public static Builder newBuilder() {
    return (Builder) DEFAULT_INSTANCE.createBuilder();
  }
  public static Builder newBuilder(com.halloapp.proto.clients.VideoReaction prototype) {
    return (Builder) DEFAULT_INSTANCE.createBuilder(prototype);
  }

  /**
   * Protobuf type {@code clients.VideoReaction}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessageLite.Builder<
        com.halloapp.proto.clients.VideoReaction, Builder> implements
      // @@protoc_insertion_point(builder_implements:clients.VideoReaction)
      com.halloapp.proto.clients.VideoReactionOrBuilder {
    // Construct using com.halloapp.proto.clients.VideoReaction.newBuilder()
    private Builder() {
      super(DEFAULT_INSTANCE);
    }


    /**
     * <code>.clients.Video video = 1;</code>
     */
    @java.lang.Override
    public boolean hasVideo() {
      return instance.hasVideo();
    }
    /**
     * <code>.clients.Video video = 1;</code>
     */
    @java.lang.Override
    public com.halloapp.proto.clients.Video getVideo() {
      return instance.getVideo();
    }
    /**
     * <code>.clients.Video video = 1;</code>
     */
    public Builder setVideo(com.halloapp.proto.clients.Video value) {
      copyOnWrite();
      instance.setVideo(value);
      return this;
      }
    /**
     * <code>.clients.Video video = 1;</code>
     */
    public Builder setVideo(
        com.halloapp.proto.clients.Video.Builder builderForValue) {
      copyOnWrite();
      instance.setVideo(builderForValue.build());
      return this;
    }
    /**
     * <code>.clients.Video video = 1;</code>
     */
    public Builder mergeVideo(com.halloapp.proto.clients.Video value) {
      copyOnWrite();
      instance.mergeVideo(value);
      return this;
    }
    /**
     * <code>.clients.Video video = 1;</code>
     */
    public Builder clearVideo() {  copyOnWrite();
      instance.clearVideo();
      return this;
    }

    // @@protoc_insertion_point(builder_scope:clients.VideoReaction)
  }
  @java.lang.Override
  @java.lang.SuppressWarnings({"unchecked", "fallthrough"})
  protected final java.lang.Object dynamicMethod(
      com.google.protobuf.GeneratedMessageLite.MethodToInvoke method,
      java.lang.Object arg0, java.lang.Object arg1) {
    switch (method) {
      case NEW_MUTABLE_INSTANCE: {
        return new com.halloapp.proto.clients.VideoReaction();
      }
      case NEW_BUILDER: {
        return new Builder();
      }
      case BUILD_MESSAGE_INFO: {
          java.lang.Object[] objects = new java.lang.Object[] {
            "video_",
          };
          java.lang.String info =
              "\u0000\u0001\u0000\u0000\u0001\u0001\u0001\u0000\u0000\u0000\u0001\t";
          return newMessageInfo(DEFAULT_INSTANCE, info, objects);
      }
      // fall through
      case GET_DEFAULT_INSTANCE: {
        return DEFAULT_INSTANCE;
      }
      case GET_PARSER: {
        com.google.protobuf.Parser<com.halloapp.proto.clients.VideoReaction> parser = PARSER;
        if (parser == null) {
          synchronized (com.halloapp.proto.clients.VideoReaction.class) {
            parser = PARSER;
            if (parser == null) {
              parser =
                  new DefaultInstanceBasedParser<com.halloapp.proto.clients.VideoReaction>(
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


  // @@protoc_insertion_point(class_scope:clients.VideoReaction)
  private static final com.halloapp.proto.clients.VideoReaction DEFAULT_INSTANCE;
  static {
    VideoReaction defaultInstance = new VideoReaction();
    // New instances are implicitly immutable so no need to make
    // immutable.
    DEFAULT_INSTANCE = defaultInstance;
    com.google.protobuf.GeneratedMessageLite.registerDefaultInstance(
      VideoReaction.class, defaultInstance);
  }

  public static com.halloapp.proto.clients.VideoReaction getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static volatile com.google.protobuf.Parser<VideoReaction> PARSER;

  public static com.google.protobuf.Parser<VideoReaction> parser() {
    return DEFAULT_INSTANCE.getParserForType();
  }
}
