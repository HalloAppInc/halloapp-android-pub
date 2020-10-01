// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: server.proto

package com.halloapp.proto.server;

/**
 * <pre>
 * Left them to be string for now, will update these later as necessary.
 * </pre>
 *
 * Protobuf type {@code server.AuthResult}
 */
public  final class AuthResult extends
    com.google.protobuf.GeneratedMessageLite<
        AuthResult, AuthResult.Builder> implements
    // @@protoc_insertion_point(message_implements:server.AuthResult)
    AuthResultOrBuilder {
  private AuthResult() {
    result_ = "";
    reason_ = "";
    propsHash_ = com.google.protobuf.ByteString.EMPTY;
  }
  public static final int RESULT_FIELD_NUMBER = 1;
  private java.lang.String result_;
  /**
   * <code>string result = 1;</code>
   * @return The result.
   */
  @java.lang.Override
  public java.lang.String getResult() {
    return result_;
  }
  /**
   * <code>string result = 1;</code>
   * @return The bytes for result.
   */
  @java.lang.Override
  public com.google.protobuf.ByteString
      getResultBytes() {
    return com.google.protobuf.ByteString.copyFromUtf8(result_);
  }
  /**
   * <code>string result = 1;</code>
   * @param value The result to set.
   */
  private void setResult(
      java.lang.String value) {
    value.getClass();
  
    result_ = value;
  }
  /**
   * <code>string result = 1;</code>
   */
  private void clearResult() {
    
    result_ = getDefaultInstance().getResult();
  }
  /**
   * <code>string result = 1;</code>
   * @param value The bytes for result to set.
   */
  private void setResultBytes(
      com.google.protobuf.ByteString value) {
    checkByteStringIsUtf8(value);
    result_ = value.toStringUtf8();
    
  }

  public static final int REASON_FIELD_NUMBER = 2;
  private java.lang.String reason_;
  /**
   * <code>string reason = 2;</code>
   * @return The reason.
   */
  @java.lang.Override
  public java.lang.String getReason() {
    return reason_;
  }
  /**
   * <code>string reason = 2;</code>
   * @return The bytes for reason.
   */
  @java.lang.Override
  public com.google.protobuf.ByteString
      getReasonBytes() {
    return com.google.protobuf.ByteString.copyFromUtf8(reason_);
  }
  /**
   * <code>string reason = 2;</code>
   * @param value The reason to set.
   */
  private void setReason(
      java.lang.String value) {
    value.getClass();
  
    reason_ = value;
  }
  /**
   * <code>string reason = 2;</code>
   */
  private void clearReason() {
    
    reason_ = getDefaultInstance().getReason();
  }
  /**
   * <code>string reason = 2;</code>
   * @param value The bytes for reason to set.
   */
  private void setReasonBytes(
      com.google.protobuf.ByteString value) {
    checkByteStringIsUtf8(value);
    reason_ = value.toStringUtf8();
    
  }

  public static final int PROPS_HASH_FIELD_NUMBER = 3;
  private com.google.protobuf.ByteString propsHash_;
  /**
   * <code>bytes props_hash = 3;</code>
   * @return The propsHash.
   */
  @java.lang.Override
  public com.google.protobuf.ByteString getPropsHash() {
    return propsHash_;
  }
  /**
   * <code>bytes props_hash = 3;</code>
   * @param value The propsHash to set.
   */
  private void setPropsHash(com.google.protobuf.ByteString value) {
    value.getClass();
  
    propsHash_ = value;
  }
  /**
   * <code>bytes props_hash = 3;</code>
   */
  private void clearPropsHash() {
    
    propsHash_ = getDefaultInstance().getPropsHash();
  }

  public static com.halloapp.proto.server.AuthResult parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static com.halloapp.proto.server.AuthResult parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static com.halloapp.proto.server.AuthResult parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static com.halloapp.proto.server.AuthResult parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static com.halloapp.proto.server.AuthResult parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static com.halloapp.proto.server.AuthResult parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static com.halloapp.proto.server.AuthResult parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input);
  }
  public static com.halloapp.proto.server.AuthResult parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input, extensionRegistry);
  }
  public static com.halloapp.proto.server.AuthResult parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return parseDelimitedFrom(DEFAULT_INSTANCE, input);
  }
  public static com.halloapp.proto.server.AuthResult parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return parseDelimitedFrom(DEFAULT_INSTANCE, input, extensionRegistry);
  }
  public static com.halloapp.proto.server.AuthResult parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input);
  }
  public static com.halloapp.proto.server.AuthResult parseFrom(
      com.google.protobuf.CodedInputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input, extensionRegistry);
  }

  public static Builder newBuilder() {
    return (Builder) DEFAULT_INSTANCE.createBuilder();
  }
  public static Builder newBuilder(com.halloapp.proto.server.AuthResult prototype) {
    return (Builder) DEFAULT_INSTANCE.createBuilder(prototype);
  }

  /**
   * <pre>
   * Left them to be string for now, will update these later as necessary.
   * </pre>
   *
   * Protobuf type {@code server.AuthResult}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessageLite.Builder<
        com.halloapp.proto.server.AuthResult, Builder> implements
      // @@protoc_insertion_point(builder_implements:server.AuthResult)
      com.halloapp.proto.server.AuthResultOrBuilder {
    // Construct using com.halloapp.proto.server.AuthResult.newBuilder()
    private Builder() {
      super(DEFAULT_INSTANCE);
    }


    /**
     * <code>string result = 1;</code>
     * @return The result.
     */
    @java.lang.Override
    public java.lang.String getResult() {
      return instance.getResult();
    }
    /**
     * <code>string result = 1;</code>
     * @return The bytes for result.
     */
    @java.lang.Override
    public com.google.protobuf.ByteString
        getResultBytes() {
      return instance.getResultBytes();
    }
    /**
     * <code>string result = 1;</code>
     * @param value The result to set.
     * @return This builder for chaining.
     */
    public Builder setResult(
        java.lang.String value) {
      copyOnWrite();
      instance.setResult(value);
      return this;
    }
    /**
     * <code>string result = 1;</code>
     * @return This builder for chaining.
     */
    public Builder clearResult() {
      copyOnWrite();
      instance.clearResult();
      return this;
    }
    /**
     * <code>string result = 1;</code>
     * @param value The bytes for result to set.
     * @return This builder for chaining.
     */
    public Builder setResultBytes(
        com.google.protobuf.ByteString value) {
      copyOnWrite();
      instance.setResultBytes(value);
      return this;
    }

    /**
     * <code>string reason = 2;</code>
     * @return The reason.
     */
    @java.lang.Override
    public java.lang.String getReason() {
      return instance.getReason();
    }
    /**
     * <code>string reason = 2;</code>
     * @return The bytes for reason.
     */
    @java.lang.Override
    public com.google.protobuf.ByteString
        getReasonBytes() {
      return instance.getReasonBytes();
    }
    /**
     * <code>string reason = 2;</code>
     * @param value The reason to set.
     * @return This builder for chaining.
     */
    public Builder setReason(
        java.lang.String value) {
      copyOnWrite();
      instance.setReason(value);
      return this;
    }
    /**
     * <code>string reason = 2;</code>
     * @return This builder for chaining.
     */
    public Builder clearReason() {
      copyOnWrite();
      instance.clearReason();
      return this;
    }
    /**
     * <code>string reason = 2;</code>
     * @param value The bytes for reason to set.
     * @return This builder for chaining.
     */
    public Builder setReasonBytes(
        com.google.protobuf.ByteString value) {
      copyOnWrite();
      instance.setReasonBytes(value);
      return this;
    }

    /**
     * <code>bytes props_hash = 3;</code>
     * @return The propsHash.
     */
    @java.lang.Override
    public com.google.protobuf.ByteString getPropsHash() {
      return instance.getPropsHash();
    }
    /**
     * <code>bytes props_hash = 3;</code>
     * @param value The propsHash to set.
     * @return This builder for chaining.
     */
    public Builder setPropsHash(com.google.protobuf.ByteString value) {
      copyOnWrite();
      instance.setPropsHash(value);
      return this;
    }
    /**
     * <code>bytes props_hash = 3;</code>
     * @return This builder for chaining.
     */
    public Builder clearPropsHash() {
      copyOnWrite();
      instance.clearPropsHash();
      return this;
    }

    // @@protoc_insertion_point(builder_scope:server.AuthResult)
  }
  @java.lang.Override
  @java.lang.SuppressWarnings({"unchecked", "fallthrough"})
  protected final java.lang.Object dynamicMethod(
      com.google.protobuf.GeneratedMessageLite.MethodToInvoke method,
      java.lang.Object arg0, java.lang.Object arg1) {
    switch (method) {
      case NEW_MUTABLE_INSTANCE: {
        return new com.halloapp.proto.server.AuthResult();
      }
      case NEW_BUILDER: {
        return new Builder();
      }
      case BUILD_MESSAGE_INFO: {
          java.lang.Object[] objects = new java.lang.Object[] {
            "result_",
            "reason_",
            "propsHash_",
          };
          java.lang.String info =
              "\u0000\u0003\u0000\u0000\u0001\u0003\u0003\u0000\u0000\u0000\u0001\u0208\u0002\u0208" +
              "\u0003\n";
          return newMessageInfo(DEFAULT_INSTANCE, info, objects);
      }
      // fall through
      case GET_DEFAULT_INSTANCE: {
        return DEFAULT_INSTANCE;
      }
      case GET_PARSER: {
        com.google.protobuf.Parser<com.halloapp.proto.server.AuthResult> parser = PARSER;
        if (parser == null) {
          synchronized (com.halloapp.proto.server.AuthResult.class) {
            parser = PARSER;
            if (parser == null) {
              parser =
                  new DefaultInstanceBasedParser<com.halloapp.proto.server.AuthResult>(
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


  // @@protoc_insertion_point(class_scope:server.AuthResult)
  private static final com.halloapp.proto.server.AuthResult DEFAULT_INSTANCE;
  static {
    AuthResult defaultInstance = new AuthResult();
    // New instances are implicitly immutable so no need to make
    // immutable.
    DEFAULT_INSTANCE = defaultInstance;
    com.google.protobuf.GeneratedMessageLite.registerDefaultInstance(
      AuthResult.class, defaultInstance);
  }

  public static com.halloapp.proto.server.AuthResult getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static volatile com.google.protobuf.Parser<AuthResult> PARSER;

  public static com.google.protobuf.Parser<AuthResult> parser() {
    return DEFAULT_INSTANCE.getParserForType();
  }
}

