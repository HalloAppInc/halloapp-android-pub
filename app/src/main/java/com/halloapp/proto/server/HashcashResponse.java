// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: server.proto

package com.halloapp.proto.server;

/**
 * Protobuf type {@code server.HashcashResponse}
 */
public  final class HashcashResponse extends
    com.google.protobuf.GeneratedMessageLite<
        HashcashResponse, HashcashResponse.Builder> implements
    // @@protoc_insertion_point(message_implements:server.HashcashResponse)
    HashcashResponseOrBuilder {
  private HashcashResponse() {
    hashcashChallenge_ = "";
  }
  public static final int HASHCASH_CHALLENGE_FIELD_NUMBER = 1;
  private java.lang.String hashcashChallenge_;
  /**
   * <code>string hashcash_challenge = 1;</code>
   * @return The hashcashChallenge.
   */
  @java.lang.Override
  public java.lang.String getHashcashChallenge() {
    return hashcashChallenge_;
  }
  /**
   * <code>string hashcash_challenge = 1;</code>
   * @return The bytes for hashcashChallenge.
   */
  @java.lang.Override
  public com.google.protobuf.ByteString
      getHashcashChallengeBytes() {
    return com.google.protobuf.ByteString.copyFromUtf8(hashcashChallenge_);
  }
  /**
   * <code>string hashcash_challenge = 1;</code>
   * @param value The hashcashChallenge to set.
   */
  private void setHashcashChallenge(
      java.lang.String value) {
    value.getClass();
  
    hashcashChallenge_ = value;
  }
  /**
   * <code>string hashcash_challenge = 1;</code>
   */
  private void clearHashcashChallenge() {
    
    hashcashChallenge_ = getDefaultInstance().getHashcashChallenge();
  }
  /**
   * <code>string hashcash_challenge = 1;</code>
   * @param value The bytes for hashcashChallenge to set.
   */
  private void setHashcashChallengeBytes(
      com.google.protobuf.ByteString value) {
    checkByteStringIsUtf8(value);
    hashcashChallenge_ = value.toStringUtf8();
    
  }

  public static com.halloapp.proto.server.HashcashResponse parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static com.halloapp.proto.server.HashcashResponse parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static com.halloapp.proto.server.HashcashResponse parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static com.halloapp.proto.server.HashcashResponse parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static com.halloapp.proto.server.HashcashResponse parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static com.halloapp.proto.server.HashcashResponse parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static com.halloapp.proto.server.HashcashResponse parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input);
  }
  public static com.halloapp.proto.server.HashcashResponse parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input, extensionRegistry);
  }
  public static com.halloapp.proto.server.HashcashResponse parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return parseDelimitedFrom(DEFAULT_INSTANCE, input);
  }
  public static com.halloapp.proto.server.HashcashResponse parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return parseDelimitedFrom(DEFAULT_INSTANCE, input, extensionRegistry);
  }
  public static com.halloapp.proto.server.HashcashResponse parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input);
  }
  public static com.halloapp.proto.server.HashcashResponse parseFrom(
      com.google.protobuf.CodedInputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input, extensionRegistry);
  }

  public static Builder newBuilder() {
    return (Builder) DEFAULT_INSTANCE.createBuilder();
  }
  public static Builder newBuilder(com.halloapp.proto.server.HashcashResponse prototype) {
    return (Builder) DEFAULT_INSTANCE.createBuilder(prototype);
  }

  /**
   * Protobuf type {@code server.HashcashResponse}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessageLite.Builder<
        com.halloapp.proto.server.HashcashResponse, Builder> implements
      // @@protoc_insertion_point(builder_implements:server.HashcashResponse)
      com.halloapp.proto.server.HashcashResponseOrBuilder {
    // Construct using com.halloapp.proto.server.HashcashResponse.newBuilder()
    private Builder() {
      super(DEFAULT_INSTANCE);
    }


    /**
     * <code>string hashcash_challenge = 1;</code>
     * @return The hashcashChallenge.
     */
    @java.lang.Override
    public java.lang.String getHashcashChallenge() {
      return instance.getHashcashChallenge();
    }
    /**
     * <code>string hashcash_challenge = 1;</code>
     * @return The bytes for hashcashChallenge.
     */
    @java.lang.Override
    public com.google.protobuf.ByteString
        getHashcashChallengeBytes() {
      return instance.getHashcashChallengeBytes();
    }
    /**
     * <code>string hashcash_challenge = 1;</code>
     * @param value The hashcashChallenge to set.
     * @return This builder for chaining.
     */
    public Builder setHashcashChallenge(
        java.lang.String value) {
      copyOnWrite();
      instance.setHashcashChallenge(value);
      return this;
    }
    /**
     * <code>string hashcash_challenge = 1;</code>
     * @return This builder for chaining.
     */
    public Builder clearHashcashChallenge() {
      copyOnWrite();
      instance.clearHashcashChallenge();
      return this;
    }
    /**
     * <code>string hashcash_challenge = 1;</code>
     * @param value The bytes for hashcashChallenge to set.
     * @return This builder for chaining.
     */
    public Builder setHashcashChallengeBytes(
        com.google.protobuf.ByteString value) {
      copyOnWrite();
      instance.setHashcashChallengeBytes(value);
      return this;
    }

    // @@protoc_insertion_point(builder_scope:server.HashcashResponse)
  }
  @java.lang.Override
  @java.lang.SuppressWarnings({"unchecked", "fallthrough"})
  protected final java.lang.Object dynamicMethod(
      com.google.protobuf.GeneratedMessageLite.MethodToInvoke method,
      java.lang.Object arg0, java.lang.Object arg1) {
    switch (method) {
      case NEW_MUTABLE_INSTANCE: {
        return new com.halloapp.proto.server.HashcashResponse();
      }
      case NEW_BUILDER: {
        return new Builder();
      }
      case BUILD_MESSAGE_INFO: {
          java.lang.Object[] objects = new java.lang.Object[] {
            "hashcashChallenge_",
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
        com.google.protobuf.Parser<com.halloapp.proto.server.HashcashResponse> parser = PARSER;
        if (parser == null) {
          synchronized (com.halloapp.proto.server.HashcashResponse.class) {
            parser = PARSER;
            if (parser == null) {
              parser =
                  new DefaultInstanceBasedParser<com.halloapp.proto.server.HashcashResponse>(
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


  // @@protoc_insertion_point(class_scope:server.HashcashResponse)
  private static final com.halloapp.proto.server.HashcashResponse DEFAULT_INSTANCE;
  static {
    HashcashResponse defaultInstance = new HashcashResponse();
    // New instances are implicitly immutable so no need to make
    // immutable.
    DEFAULT_INSTANCE = defaultInstance;
    com.google.protobuf.GeneratedMessageLite.registerDefaultInstance(
      HashcashResponse.class, defaultInstance);
  }

  public static com.halloapp.proto.server.HashcashResponse getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static volatile com.google.protobuf.Parser<HashcashResponse> PARSER;

  public static com.google.protobuf.Parser<HashcashResponse> parser() {
    return DEFAULT_INSTANCE.getParserForType();
  }
}
