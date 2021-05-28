// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: clients.proto

package com.halloapp.proto.clients;

/**
 * Protobuf type {@code clients.SenderKey}
 */
public  final class SenderKey extends
    com.google.protobuf.GeneratedMessageLite<
        SenderKey, SenderKey.Builder> implements
    // @@protoc_insertion_point(message_implements:clients.SenderKey)
    SenderKeyOrBuilder {
  private SenderKey() {
    chainKey_ = com.google.protobuf.ByteString.EMPTY;
    publicSignatureKey_ = com.google.protobuf.ByteString.EMPTY;
  }
  public static final int CHAIN_KEY_FIELD_NUMBER = 1;
  private com.google.protobuf.ByteString chainKey_;
  /**
   * <code>bytes chain_key = 1;</code>
   * @return The chainKey.
   */
  @java.lang.Override
  public com.google.protobuf.ByteString getChainKey() {
    return chainKey_;
  }
  /**
   * <code>bytes chain_key = 1;</code>
   * @param value The chainKey to set.
   */
  private void setChainKey(com.google.protobuf.ByteString value) {
    value.getClass();
  
    chainKey_ = value;
  }
  /**
   * <code>bytes chain_key = 1;</code>
   */
  private void clearChainKey() {
    
    chainKey_ = getDefaultInstance().getChainKey();
  }

  public static final int PUBLIC_SIGNATURE_KEY_FIELD_NUMBER = 2;
  private com.google.protobuf.ByteString publicSignatureKey_;
  /**
   * <code>bytes public_signature_key = 2;</code>
   * @return The publicSignatureKey.
   */
  @java.lang.Override
  public com.google.protobuf.ByteString getPublicSignatureKey() {
    return publicSignatureKey_;
  }
  /**
   * <code>bytes public_signature_key = 2;</code>
   * @param value The publicSignatureKey to set.
   */
  private void setPublicSignatureKey(com.google.protobuf.ByteString value) {
    value.getClass();
  
    publicSignatureKey_ = value;
  }
  /**
   * <code>bytes public_signature_key = 2;</code>
   */
  private void clearPublicSignatureKey() {
    
    publicSignatureKey_ = getDefaultInstance().getPublicSignatureKey();
  }

  public static com.halloapp.proto.clients.SenderKey parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static com.halloapp.proto.clients.SenderKey parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static com.halloapp.proto.clients.SenderKey parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static com.halloapp.proto.clients.SenderKey parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static com.halloapp.proto.clients.SenderKey parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static com.halloapp.proto.clients.SenderKey parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static com.halloapp.proto.clients.SenderKey parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input);
  }
  public static com.halloapp.proto.clients.SenderKey parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input, extensionRegistry);
  }
  public static com.halloapp.proto.clients.SenderKey parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return parseDelimitedFrom(DEFAULT_INSTANCE, input);
  }
  public static com.halloapp.proto.clients.SenderKey parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return parseDelimitedFrom(DEFAULT_INSTANCE, input, extensionRegistry);
  }
  public static com.halloapp.proto.clients.SenderKey parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input);
  }
  public static com.halloapp.proto.clients.SenderKey parseFrom(
      com.google.protobuf.CodedInputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input, extensionRegistry);
  }

  public static Builder newBuilder() {
    return (Builder) DEFAULT_INSTANCE.createBuilder();
  }
  public static Builder newBuilder(com.halloapp.proto.clients.SenderKey prototype) {
    return (Builder) DEFAULT_INSTANCE.createBuilder(prototype);
  }

  /**
   * Protobuf type {@code clients.SenderKey}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessageLite.Builder<
        com.halloapp.proto.clients.SenderKey, Builder> implements
      // @@protoc_insertion_point(builder_implements:clients.SenderKey)
      com.halloapp.proto.clients.SenderKeyOrBuilder {
    // Construct using com.halloapp.proto.clients.SenderKey.newBuilder()
    private Builder() {
      super(DEFAULT_INSTANCE);
    }


    /**
     * <code>bytes chain_key = 1;</code>
     * @return The chainKey.
     */
    @java.lang.Override
    public com.google.protobuf.ByteString getChainKey() {
      return instance.getChainKey();
    }
    /**
     * <code>bytes chain_key = 1;</code>
     * @param value The chainKey to set.
     * @return This builder for chaining.
     */
    public Builder setChainKey(com.google.protobuf.ByteString value) {
      copyOnWrite();
      instance.setChainKey(value);
      return this;
    }
    /**
     * <code>bytes chain_key = 1;</code>
     * @return This builder for chaining.
     */
    public Builder clearChainKey() {
      copyOnWrite();
      instance.clearChainKey();
      return this;
    }

    /**
     * <code>bytes public_signature_key = 2;</code>
     * @return The publicSignatureKey.
     */
    @java.lang.Override
    public com.google.protobuf.ByteString getPublicSignatureKey() {
      return instance.getPublicSignatureKey();
    }
    /**
     * <code>bytes public_signature_key = 2;</code>
     * @param value The publicSignatureKey to set.
     * @return This builder for chaining.
     */
    public Builder setPublicSignatureKey(com.google.protobuf.ByteString value) {
      copyOnWrite();
      instance.setPublicSignatureKey(value);
      return this;
    }
    /**
     * <code>bytes public_signature_key = 2;</code>
     * @return This builder for chaining.
     */
    public Builder clearPublicSignatureKey() {
      copyOnWrite();
      instance.clearPublicSignatureKey();
      return this;
    }

    // @@protoc_insertion_point(builder_scope:clients.SenderKey)
  }
  @java.lang.Override
  @java.lang.SuppressWarnings({"unchecked", "fallthrough"})
  protected final java.lang.Object dynamicMethod(
      com.google.protobuf.GeneratedMessageLite.MethodToInvoke method,
      java.lang.Object arg0, java.lang.Object arg1) {
    switch (method) {
      case NEW_MUTABLE_INSTANCE: {
        return new com.halloapp.proto.clients.SenderKey();
      }
      case NEW_BUILDER: {
        return new Builder();
      }
      case BUILD_MESSAGE_INFO: {
          java.lang.Object[] objects = new java.lang.Object[] {
            "chainKey_",
            "publicSignatureKey_",
          };
          java.lang.String info =
              "\u0000\u0002\u0000\u0000\u0001\u0002\u0002\u0000\u0000\u0000\u0001\n\u0002\n";
          return newMessageInfo(DEFAULT_INSTANCE, info, objects);
      }
      // fall through
      case GET_DEFAULT_INSTANCE: {
        return DEFAULT_INSTANCE;
      }
      case GET_PARSER: {
        com.google.protobuf.Parser<com.halloapp.proto.clients.SenderKey> parser = PARSER;
        if (parser == null) {
          synchronized (com.halloapp.proto.clients.SenderKey.class) {
            parser = PARSER;
            if (parser == null) {
              parser =
                  new DefaultInstanceBasedParser<com.halloapp.proto.clients.SenderKey>(
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


  // @@protoc_insertion_point(class_scope:clients.SenderKey)
  private static final com.halloapp.proto.clients.SenderKey DEFAULT_INSTANCE;
  static {
    SenderKey defaultInstance = new SenderKey();
    // New instances are implicitly immutable so no need to make
    // immutable.
    DEFAULT_INSTANCE = defaultInstance;
    com.google.protobuf.GeneratedMessageLite.registerDefaultInstance(
      SenderKey.class, defaultInstance);
  }

  public static com.halloapp.proto.clients.SenderKey getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static volatile com.google.protobuf.Parser<SenderKey> PARSER;

  public static com.google.protobuf.Parser<SenderKey> parser() {
    return DEFAULT_INSTANCE.getParserForType();
  }
}

