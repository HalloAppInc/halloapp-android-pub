// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: clients.proto

package com.halloapp.proto.clients;

/**
 * Protobuf type {@code clients.OneTimePreKey}
 */
public  final class OneTimePreKey extends
    com.google.protobuf.GeneratedMessageLite<
        OneTimePreKey, OneTimePreKey.Builder> implements
    // @@protoc_insertion_point(message_implements:clients.OneTimePreKey)
    OneTimePreKeyOrBuilder {
  private OneTimePreKey() {
    publicKey_ = com.google.protobuf.ByteString.EMPTY;
  }
  public static final int ID_FIELD_NUMBER = 1;
  private int id_;
  /**
   * <code>int32 id = 1;</code>
   * @return The id.
   */
  @java.lang.Override
  public int getId() {
    return id_;
  }
  /**
   * <code>int32 id = 1;</code>
   * @param value The id to set.
   */
  private void setId(int value) {
    
    id_ = value;
  }
  /**
   * <code>int32 id = 1;</code>
   */
  private void clearId() {
    
    id_ = 0;
  }

  public static final int PUBLIC_KEY_FIELD_NUMBER = 2;
  private com.google.protobuf.ByteString publicKey_;
  /**
   * <code>bytes public_key = 2;</code>
   * @return The publicKey.
   */
  @java.lang.Override
  public com.google.protobuf.ByteString getPublicKey() {
    return publicKey_;
  }
  /**
   * <code>bytes public_key = 2;</code>
   * @param value The publicKey to set.
   */
  private void setPublicKey(com.google.protobuf.ByteString value) {
    value.getClass();
  
    publicKey_ = value;
  }
  /**
   * <code>bytes public_key = 2;</code>
   */
  private void clearPublicKey() {
    
    publicKey_ = getDefaultInstance().getPublicKey();
  }

  public static com.halloapp.proto.clients.OneTimePreKey parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static com.halloapp.proto.clients.OneTimePreKey parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static com.halloapp.proto.clients.OneTimePreKey parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static com.halloapp.proto.clients.OneTimePreKey parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static com.halloapp.proto.clients.OneTimePreKey parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static com.halloapp.proto.clients.OneTimePreKey parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static com.halloapp.proto.clients.OneTimePreKey parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input);
  }
  public static com.halloapp.proto.clients.OneTimePreKey parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input, extensionRegistry);
  }
  public static com.halloapp.proto.clients.OneTimePreKey parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return parseDelimitedFrom(DEFAULT_INSTANCE, input);
  }
  public static com.halloapp.proto.clients.OneTimePreKey parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return parseDelimitedFrom(DEFAULT_INSTANCE, input, extensionRegistry);
  }
  public static com.halloapp.proto.clients.OneTimePreKey parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input);
  }
  public static com.halloapp.proto.clients.OneTimePreKey parseFrom(
      com.google.protobuf.CodedInputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input, extensionRegistry);
  }

  public static Builder newBuilder() {
    return (Builder) DEFAULT_INSTANCE.createBuilder();
  }
  public static Builder newBuilder(com.halloapp.proto.clients.OneTimePreKey prototype) {
    return (Builder) DEFAULT_INSTANCE.createBuilder(prototype);
  }

  /**
   * Protobuf type {@code clients.OneTimePreKey}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessageLite.Builder<
        com.halloapp.proto.clients.OneTimePreKey, Builder> implements
      // @@protoc_insertion_point(builder_implements:clients.OneTimePreKey)
      com.halloapp.proto.clients.OneTimePreKeyOrBuilder {
    // Construct using com.halloapp.proto.clients.OneTimePreKey.newBuilder()
    private Builder() {
      super(DEFAULT_INSTANCE);
    }


    /**
     * <code>int32 id = 1;</code>
     * @return The id.
     */
    @java.lang.Override
    public int getId() {
      return instance.getId();
    }
    /**
     * <code>int32 id = 1;</code>
     * @param value The id to set.
     * @return This builder for chaining.
     */
    public Builder setId(int value) {
      copyOnWrite();
      instance.setId(value);
      return this;
    }
    /**
     * <code>int32 id = 1;</code>
     * @return This builder for chaining.
     */
    public Builder clearId() {
      copyOnWrite();
      instance.clearId();
      return this;
    }

    /**
     * <code>bytes public_key = 2;</code>
     * @return The publicKey.
     */
    @java.lang.Override
    public com.google.protobuf.ByteString getPublicKey() {
      return instance.getPublicKey();
    }
    /**
     * <code>bytes public_key = 2;</code>
     * @param value The publicKey to set.
     * @return This builder for chaining.
     */
    public Builder setPublicKey(com.google.protobuf.ByteString value) {
      copyOnWrite();
      instance.setPublicKey(value);
      return this;
    }
    /**
     * <code>bytes public_key = 2;</code>
     * @return This builder for chaining.
     */
    public Builder clearPublicKey() {
      copyOnWrite();
      instance.clearPublicKey();
      return this;
    }

    // @@protoc_insertion_point(builder_scope:clients.OneTimePreKey)
  }
  @java.lang.Override
  @java.lang.SuppressWarnings({"unchecked", "fallthrough"})
  protected final java.lang.Object dynamicMethod(
      com.google.protobuf.GeneratedMessageLite.MethodToInvoke method,
      java.lang.Object arg0, java.lang.Object arg1) {
    switch (method) {
      case NEW_MUTABLE_INSTANCE: {
        return new com.halloapp.proto.clients.OneTimePreKey();
      }
      case NEW_BUILDER: {
        return new Builder();
      }
      case BUILD_MESSAGE_INFO: {
          java.lang.Object[] objects = new java.lang.Object[] {
            "id_",
            "publicKey_",
          };
          java.lang.String info =
              "\u0000\u0002\u0000\u0000\u0001\u0002\u0002\u0000\u0000\u0000\u0001\u0004\u0002\n" +
              "";
          return newMessageInfo(DEFAULT_INSTANCE, info, objects);
      }
      // fall through
      case GET_DEFAULT_INSTANCE: {
        return DEFAULT_INSTANCE;
      }
      case GET_PARSER: {
        com.google.protobuf.Parser<com.halloapp.proto.clients.OneTimePreKey> parser = PARSER;
        if (parser == null) {
          synchronized (com.halloapp.proto.clients.OneTimePreKey.class) {
            parser = PARSER;
            if (parser == null) {
              parser =
                  new DefaultInstanceBasedParser<com.halloapp.proto.clients.OneTimePreKey>(
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


  // @@protoc_insertion_point(class_scope:clients.OneTimePreKey)
  private static final com.halloapp.proto.clients.OneTimePreKey DEFAULT_INSTANCE;
  static {
    OneTimePreKey defaultInstance = new OneTimePreKey();
    // New instances are implicitly immutable so no need to make
    // immutable.
    DEFAULT_INSTANCE = defaultInstance;
    com.google.protobuf.GeneratedMessageLite.registerDefaultInstance(
      OneTimePreKey.class, defaultInstance);
  }

  public static com.halloapp.proto.clients.OneTimePreKey getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static volatile com.google.protobuf.Parser<OneTimePreKey> PARSER;

  public static com.google.protobuf.Parser<OneTimePreKey> parser() {
    return DEFAULT_INSTANCE.getParserForType();
  }
}

