// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: server.proto

package com.halloapp.proto.server;

/**
 * Protobuf type {@code server.SearchRequest}
 */
public  final class SearchRequest extends
    com.google.protobuf.GeneratedMessageLite<
        SearchRequest, SearchRequest.Builder> implements
    // @@protoc_insertion_point(message_implements:server.SearchRequest)
    SearchRequestOrBuilder {
  private SearchRequest() {
    usernameString_ = "";
  }
  public static final int USERNAME_STRING_FIELD_NUMBER = 1;
  private java.lang.String usernameString_;
  /**
   * <code>string username_string = 1;</code>
   * @return The usernameString.
   */
  @java.lang.Override
  public java.lang.String getUsernameString() {
    return usernameString_;
  }
  /**
   * <code>string username_string = 1;</code>
   * @return The bytes for usernameString.
   */
  @java.lang.Override
  public com.google.protobuf.ByteString
      getUsernameStringBytes() {
    return com.google.protobuf.ByteString.copyFromUtf8(usernameString_);
  }
  /**
   * <code>string username_string = 1;</code>
   * @param value The usernameString to set.
   */
  private void setUsernameString(
      java.lang.String value) {
    value.getClass();
  
    usernameString_ = value;
  }
  /**
   * <code>string username_string = 1;</code>
   */
  private void clearUsernameString() {
    
    usernameString_ = getDefaultInstance().getUsernameString();
  }
  /**
   * <code>string username_string = 1;</code>
   * @param value The bytes for usernameString to set.
   */
  private void setUsernameStringBytes(
      com.google.protobuf.ByteString value) {
    checkByteStringIsUtf8(value);
    usernameString_ = value.toStringUtf8();
    
  }

  public static com.halloapp.proto.server.SearchRequest parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static com.halloapp.proto.server.SearchRequest parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static com.halloapp.proto.server.SearchRequest parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static com.halloapp.proto.server.SearchRequest parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static com.halloapp.proto.server.SearchRequest parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static com.halloapp.proto.server.SearchRequest parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static com.halloapp.proto.server.SearchRequest parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input);
  }
  public static com.halloapp.proto.server.SearchRequest parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input, extensionRegistry);
  }
  public static com.halloapp.proto.server.SearchRequest parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return parseDelimitedFrom(DEFAULT_INSTANCE, input);
  }
  public static com.halloapp.proto.server.SearchRequest parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return parseDelimitedFrom(DEFAULT_INSTANCE, input, extensionRegistry);
  }
  public static com.halloapp.proto.server.SearchRequest parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input);
  }
  public static com.halloapp.proto.server.SearchRequest parseFrom(
      com.google.protobuf.CodedInputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input, extensionRegistry);
  }

  public static Builder newBuilder() {
    return (Builder) DEFAULT_INSTANCE.createBuilder();
  }
  public static Builder newBuilder(com.halloapp.proto.server.SearchRequest prototype) {
    return (Builder) DEFAULT_INSTANCE.createBuilder(prototype);
  }

  /**
   * Protobuf type {@code server.SearchRequest}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessageLite.Builder<
        com.halloapp.proto.server.SearchRequest, Builder> implements
      // @@protoc_insertion_point(builder_implements:server.SearchRequest)
      com.halloapp.proto.server.SearchRequestOrBuilder {
    // Construct using com.halloapp.proto.server.SearchRequest.newBuilder()
    private Builder() {
      super(DEFAULT_INSTANCE);
    }


    /**
     * <code>string username_string = 1;</code>
     * @return The usernameString.
     */
    @java.lang.Override
    public java.lang.String getUsernameString() {
      return instance.getUsernameString();
    }
    /**
     * <code>string username_string = 1;</code>
     * @return The bytes for usernameString.
     */
    @java.lang.Override
    public com.google.protobuf.ByteString
        getUsernameStringBytes() {
      return instance.getUsernameStringBytes();
    }
    /**
     * <code>string username_string = 1;</code>
     * @param value The usernameString to set.
     * @return This builder for chaining.
     */
    public Builder setUsernameString(
        java.lang.String value) {
      copyOnWrite();
      instance.setUsernameString(value);
      return this;
    }
    /**
     * <code>string username_string = 1;</code>
     * @return This builder for chaining.
     */
    public Builder clearUsernameString() {
      copyOnWrite();
      instance.clearUsernameString();
      return this;
    }
    /**
     * <code>string username_string = 1;</code>
     * @param value The bytes for usernameString to set.
     * @return This builder for chaining.
     */
    public Builder setUsernameStringBytes(
        com.google.protobuf.ByteString value) {
      copyOnWrite();
      instance.setUsernameStringBytes(value);
      return this;
    }

    // @@protoc_insertion_point(builder_scope:server.SearchRequest)
  }
  @java.lang.Override
  @java.lang.SuppressWarnings({"unchecked", "fallthrough"})
  protected final java.lang.Object dynamicMethod(
      com.google.protobuf.GeneratedMessageLite.MethodToInvoke method,
      java.lang.Object arg0, java.lang.Object arg1) {
    switch (method) {
      case NEW_MUTABLE_INSTANCE: {
        return new com.halloapp.proto.server.SearchRequest();
      }
      case NEW_BUILDER: {
        return new Builder();
      }
      case BUILD_MESSAGE_INFO: {
          java.lang.Object[] objects = new java.lang.Object[] {
            "usernameString_",
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
        com.google.protobuf.Parser<com.halloapp.proto.server.SearchRequest> parser = PARSER;
        if (parser == null) {
          synchronized (com.halloapp.proto.server.SearchRequest.class) {
            parser = PARSER;
            if (parser == null) {
              parser =
                  new DefaultInstanceBasedParser<com.halloapp.proto.server.SearchRequest>(
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


  // @@protoc_insertion_point(class_scope:server.SearchRequest)
  private static final com.halloapp.proto.server.SearchRequest DEFAULT_INSTANCE;
  static {
    SearchRequest defaultInstance = new SearchRequest();
    // New instances are implicitly immutable so no need to make
    // immutable.
    DEFAULT_INSTANCE = defaultInstance;
    com.google.protobuf.GeneratedMessageLite.registerDefaultInstance(
      SearchRequest.class, defaultInstance);
  }

  public static com.halloapp.proto.server.SearchRequest getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static volatile com.google.protobuf.Parser<SearchRequest> PARSER;

  public static com.google.protobuf.Parser<SearchRequest> parser() {
    return DEFAULT_INSTANCE.getParserForType();
  }
}

