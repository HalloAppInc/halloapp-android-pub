// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: server.proto

package com.halloapp.proto.server;

/**
 * Protobuf type {@code server.HashcashRequest}
 */
public  final class HashcashRequest extends
    com.google.protobuf.GeneratedMessageLite<
        HashcashRequest, HashcashRequest.Builder> implements
    // @@protoc_insertion_point(message_implements:server.HashcashRequest)
    HashcashRequestOrBuilder {
  private HashcashRequest() {
    countryCode_ = "";
  }
  public static final int COUNTRY_CODE_FIELD_NUMBER = 1;
  private java.lang.String countryCode_;
  /**
   * <code>string country_code = 1;</code>
   * @return The countryCode.
   */
  @java.lang.Override
  public java.lang.String getCountryCode() {
    return countryCode_;
  }
  /**
   * <code>string country_code = 1;</code>
   * @return The bytes for countryCode.
   */
  @java.lang.Override
  public com.google.protobuf.ByteString
      getCountryCodeBytes() {
    return com.google.protobuf.ByteString.copyFromUtf8(countryCode_);
  }
  /**
   * <code>string country_code = 1;</code>
   * @param value The countryCode to set.
   */
  private void setCountryCode(
      java.lang.String value) {
    java.lang.Class<?> valueClass = value.getClass();
  
    countryCode_ = value;
  }
  /**
   * <code>string country_code = 1;</code>
   */
  private void clearCountryCode() {
    
    countryCode_ = getDefaultInstance().getCountryCode();
  }
  /**
   * <code>string country_code = 1;</code>
   * @param value The bytes for countryCode to set.
   */
  private void setCountryCodeBytes(
      com.google.protobuf.ByteString value) {
    checkByteStringIsUtf8(value);
    countryCode_ = value.toStringUtf8();
    
  }

  public static com.halloapp.proto.server.HashcashRequest parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static com.halloapp.proto.server.HashcashRequest parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static com.halloapp.proto.server.HashcashRequest parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static com.halloapp.proto.server.HashcashRequest parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static com.halloapp.proto.server.HashcashRequest parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static com.halloapp.proto.server.HashcashRequest parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static com.halloapp.proto.server.HashcashRequest parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input);
  }
  public static com.halloapp.proto.server.HashcashRequest parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input, extensionRegistry);
  }
  public static com.halloapp.proto.server.HashcashRequest parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return parseDelimitedFrom(DEFAULT_INSTANCE, input);
  }
  public static com.halloapp.proto.server.HashcashRequest parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return parseDelimitedFrom(DEFAULT_INSTANCE, input, extensionRegistry);
  }
  public static com.halloapp.proto.server.HashcashRequest parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input);
  }
  public static com.halloapp.proto.server.HashcashRequest parseFrom(
      com.google.protobuf.CodedInputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input, extensionRegistry);
  }

  public static Builder newBuilder() {
    return (Builder) DEFAULT_INSTANCE.createBuilder();
  }
  public static Builder newBuilder(com.halloapp.proto.server.HashcashRequest prototype) {
    return (Builder) DEFAULT_INSTANCE.createBuilder(prototype);
  }

  /**
   * Protobuf type {@code server.HashcashRequest}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessageLite.Builder<
        com.halloapp.proto.server.HashcashRequest, Builder> implements
      // @@protoc_insertion_point(builder_implements:server.HashcashRequest)
      com.halloapp.proto.server.HashcashRequestOrBuilder {
    // Construct using com.halloapp.proto.server.HashcashRequest.newBuilder()
    private Builder() {
      super(DEFAULT_INSTANCE);
    }


    /**
     * <code>string country_code = 1;</code>
     * @return The countryCode.
     */
    @java.lang.Override
    public java.lang.String getCountryCode() {
      return instance.getCountryCode();
    }
    /**
     * <code>string country_code = 1;</code>
     * @return The bytes for countryCode.
     */
    @java.lang.Override
    public com.google.protobuf.ByteString
        getCountryCodeBytes() {
      return instance.getCountryCodeBytes();
    }
    /**
     * <code>string country_code = 1;</code>
     * @param value The countryCode to set.
     * @return This builder for chaining.
     */
    public Builder setCountryCode(
        java.lang.String value) {
      copyOnWrite();
      instance.setCountryCode(value);
      return this;
    }
    /**
     * <code>string country_code = 1;</code>
     * @return This builder for chaining.
     */
    public Builder clearCountryCode() {
      copyOnWrite();
      instance.clearCountryCode();
      return this;
    }
    /**
     * <code>string country_code = 1;</code>
     * @param value The bytes for countryCode to set.
     * @return This builder for chaining.
     */
    public Builder setCountryCodeBytes(
        com.google.protobuf.ByteString value) {
      copyOnWrite();
      instance.setCountryCodeBytes(value);
      return this;
    }

    // @@protoc_insertion_point(builder_scope:server.HashcashRequest)
  }
  @java.lang.Override
  @java.lang.SuppressWarnings({"unchecked", "fallthrough"})
  protected final java.lang.Object dynamicMethod(
      com.google.protobuf.GeneratedMessageLite.MethodToInvoke method,
      java.lang.Object arg0, java.lang.Object arg1) {
    switch (method) {
      case NEW_MUTABLE_INSTANCE: {
        return new com.halloapp.proto.server.HashcashRequest();
      }
      case NEW_BUILDER: {
        return new Builder();
      }
      case BUILD_MESSAGE_INFO: {
          java.lang.Object[] objects = new java.lang.Object[] {
            "countryCode_",
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
        com.google.protobuf.Parser<com.halloapp.proto.server.HashcashRequest> parser = PARSER;
        if (parser == null) {
          synchronized (com.halloapp.proto.server.HashcashRequest.class) {
            parser = PARSER;
            if (parser == null) {
              parser =
                  new DefaultInstanceBasedParser<com.halloapp.proto.server.HashcashRequest>(
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


  // @@protoc_insertion_point(class_scope:server.HashcashRequest)
  private static final com.halloapp.proto.server.HashcashRequest DEFAULT_INSTANCE;
  static {
    HashcashRequest defaultInstance = new HashcashRequest();
    // New instances are implicitly immutable so no need to make
    // immutable.
    DEFAULT_INSTANCE = defaultInstance;
    com.google.protobuf.GeneratedMessageLite.registerDefaultInstance(
      HashcashRequest.class, defaultInstance);
  }

  public static com.halloapp.proto.server.HashcashRequest getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static volatile com.google.protobuf.Parser<HashcashRequest> PARSER;

  public static com.google.protobuf.Parser<HashcashRequest> parser() {
    return DEFAULT_INSTANCE.getParserForType();
  }
}

