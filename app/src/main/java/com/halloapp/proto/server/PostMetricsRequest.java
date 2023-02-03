// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: server.proto

package com.halloapp.proto.server;

/**
 * Protobuf type {@code server.PostMetricsRequest}
 */
public  final class PostMetricsRequest extends
    com.google.protobuf.GeneratedMessageLite<
        PostMetricsRequest, PostMetricsRequest.Builder> implements
    // @@protoc_insertion_point(message_implements:server.PostMetricsRequest)
    PostMetricsRequestOrBuilder {
  private PostMetricsRequest() {
    postId_ = "";
  }
  public static final int POST_ID_FIELD_NUMBER = 1;
  private java.lang.String postId_;
  /**
   * <code>string post_id = 1;</code>
   * @return The postId.
   */
  @java.lang.Override
  public java.lang.String getPostId() {
    return postId_;
  }
  /**
   * <code>string post_id = 1;</code>
   * @return The bytes for postId.
   */
  @java.lang.Override
  public com.google.protobuf.ByteString
      getPostIdBytes() {
    return com.google.protobuf.ByteString.copyFromUtf8(postId_);
  }
  /**
   * <code>string post_id = 1;</code>
   * @param value The postId to set.
   */
  private void setPostId(
      java.lang.String value) {
    value.getClass();
  
    postId_ = value;
  }
  /**
   * <code>string post_id = 1;</code>
   */
  private void clearPostId() {
    
    postId_ = getDefaultInstance().getPostId();
  }
  /**
   * <code>string post_id = 1;</code>
   * @param value The bytes for postId to set.
   */
  private void setPostIdBytes(
      com.google.protobuf.ByteString value) {
    checkByteStringIsUtf8(value);
    postId_ = value.toStringUtf8();
    
  }

  public static com.halloapp.proto.server.PostMetricsRequest parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static com.halloapp.proto.server.PostMetricsRequest parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static com.halloapp.proto.server.PostMetricsRequest parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static com.halloapp.proto.server.PostMetricsRequest parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static com.halloapp.proto.server.PostMetricsRequest parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static com.halloapp.proto.server.PostMetricsRequest parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static com.halloapp.proto.server.PostMetricsRequest parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input);
  }
  public static com.halloapp.proto.server.PostMetricsRequest parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input, extensionRegistry);
  }
  public static com.halloapp.proto.server.PostMetricsRequest parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return parseDelimitedFrom(DEFAULT_INSTANCE, input);
  }
  public static com.halloapp.proto.server.PostMetricsRequest parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return parseDelimitedFrom(DEFAULT_INSTANCE, input, extensionRegistry);
  }
  public static com.halloapp.proto.server.PostMetricsRequest parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input);
  }
  public static com.halloapp.proto.server.PostMetricsRequest parseFrom(
      com.google.protobuf.CodedInputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input, extensionRegistry);
  }

  public static Builder newBuilder() {
    return (Builder) DEFAULT_INSTANCE.createBuilder();
  }
  public static Builder newBuilder(com.halloapp.proto.server.PostMetricsRequest prototype) {
    return (Builder) DEFAULT_INSTANCE.createBuilder(prototype);
  }

  /**
   * Protobuf type {@code server.PostMetricsRequest}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessageLite.Builder<
        com.halloapp.proto.server.PostMetricsRequest, Builder> implements
      // @@protoc_insertion_point(builder_implements:server.PostMetricsRequest)
      com.halloapp.proto.server.PostMetricsRequestOrBuilder {
    // Construct using com.halloapp.proto.server.PostMetricsRequest.newBuilder()
    private Builder() {
      super(DEFAULT_INSTANCE);
    }


    /**
     * <code>string post_id = 1;</code>
     * @return The postId.
     */
    @java.lang.Override
    public java.lang.String getPostId() {
      return instance.getPostId();
    }
    /**
     * <code>string post_id = 1;</code>
     * @return The bytes for postId.
     */
    @java.lang.Override
    public com.google.protobuf.ByteString
        getPostIdBytes() {
      return instance.getPostIdBytes();
    }
    /**
     * <code>string post_id = 1;</code>
     * @param value The postId to set.
     * @return This builder for chaining.
     */
    public Builder setPostId(
        java.lang.String value) {
      copyOnWrite();
      instance.setPostId(value);
      return this;
    }
    /**
     * <code>string post_id = 1;</code>
     * @return This builder for chaining.
     */
    public Builder clearPostId() {
      copyOnWrite();
      instance.clearPostId();
      return this;
    }
    /**
     * <code>string post_id = 1;</code>
     * @param value The bytes for postId to set.
     * @return This builder for chaining.
     */
    public Builder setPostIdBytes(
        com.google.protobuf.ByteString value) {
      copyOnWrite();
      instance.setPostIdBytes(value);
      return this;
    }

    // @@protoc_insertion_point(builder_scope:server.PostMetricsRequest)
  }
  @java.lang.Override
  @java.lang.SuppressWarnings({"unchecked", "fallthrough"})
  protected final java.lang.Object dynamicMethod(
      com.google.protobuf.GeneratedMessageLite.MethodToInvoke method,
      java.lang.Object arg0, java.lang.Object arg1) {
    switch (method) {
      case NEW_MUTABLE_INSTANCE: {
        return new com.halloapp.proto.server.PostMetricsRequest();
      }
      case NEW_BUILDER: {
        return new Builder();
      }
      case BUILD_MESSAGE_INFO: {
          java.lang.Object[] objects = new java.lang.Object[] {
            "postId_",
          };
          java.lang.String info =
              "\u0000\u0001\u0000\u0000\u0001\u0001\u0001\u0000\u0000\u0000\u0001\u0208";
          return newMessageInfo(DEFAULT_INSTANCE, info, objects);
      }
      // fall through
      case GET_DEFAULT_INSTANCE: {
        return DEFAULT_INSTANCE;
      }
      case GET_PARSER: {
        com.google.protobuf.Parser<com.halloapp.proto.server.PostMetricsRequest> parser = PARSER;
        if (parser == null) {
          synchronized (com.halloapp.proto.server.PostMetricsRequest.class) {
            parser = PARSER;
            if (parser == null) {
              parser =
                  new DefaultInstanceBasedParser<com.halloapp.proto.server.PostMetricsRequest>(
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


  // @@protoc_insertion_point(class_scope:server.PostMetricsRequest)
  private static final com.halloapp.proto.server.PostMetricsRequest DEFAULT_INSTANCE;
  static {
    PostMetricsRequest defaultInstance = new PostMetricsRequest();
    // New instances are implicitly immutable so no need to make
    // immutable.
    DEFAULT_INSTANCE = defaultInstance;
    com.google.protobuf.GeneratedMessageLite.registerDefaultInstance(
      PostMetricsRequest.class, defaultInstance);
  }

  public static com.halloapp.proto.server.PostMetricsRequest getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static volatile com.google.protobuf.Parser<PostMetricsRequest> PARSER;

  public static com.google.protobuf.Parser<PostMetricsRequest> parser() {
    return DEFAULT_INSTANCE.getParserForType();
  }
}
