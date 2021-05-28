// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: clients.proto

package com.halloapp.proto.clients;

/**
 * Protobuf type {@code clients.SenderState}
 */
public  final class SenderState extends
    com.google.protobuf.GeneratedMessageLite<
        SenderState, SenderState.Builder> implements
    // @@protoc_insertion_point(message_implements:clients.SenderState)
    SenderStateOrBuilder {
  private SenderState() {
  }
  public static final int SENDER_KEY_FIELD_NUMBER = 1;
  private com.halloapp.proto.clients.SenderKey senderKey_;
  /**
   * <code>.clients.SenderKey sender_key = 1;</code>
   */
  @java.lang.Override
  public boolean hasSenderKey() {
    return senderKey_ != null;
  }
  /**
   * <code>.clients.SenderKey sender_key = 1;</code>
   */
  @java.lang.Override
  public com.halloapp.proto.clients.SenderKey getSenderKey() {
    return senderKey_ == null ? com.halloapp.proto.clients.SenderKey.getDefaultInstance() : senderKey_;
  }
  /**
   * <code>.clients.SenderKey sender_key = 1;</code>
   */
  private void setSenderKey(com.halloapp.proto.clients.SenderKey value) {
    value.getClass();
  senderKey_ = value;
    
    }
  /**
   * <code>.clients.SenderKey sender_key = 1;</code>
   */
  @java.lang.SuppressWarnings({"ReferenceEquality"})
  private void mergeSenderKey(com.halloapp.proto.clients.SenderKey value) {
    value.getClass();
  if (senderKey_ != null &&
        senderKey_ != com.halloapp.proto.clients.SenderKey.getDefaultInstance()) {
      senderKey_ =
        com.halloapp.proto.clients.SenderKey.newBuilder(senderKey_).mergeFrom(value).buildPartial();
    } else {
      senderKey_ = value;
    }
    
  }
  /**
   * <code>.clients.SenderKey sender_key = 1;</code>
   */
  private void clearSenderKey() {  senderKey_ = null;
    
  }

  public static final int CURRENT_CHAIN_INDEX_FIELD_NUMBER = 2;
  private int currentChainIndex_;
  /**
   * <code>int32 current_chain_index = 2;</code>
   * @return The currentChainIndex.
   */
  @java.lang.Override
  public int getCurrentChainIndex() {
    return currentChainIndex_;
  }
  /**
   * <code>int32 current_chain_index = 2;</code>
   * @param value The currentChainIndex to set.
   */
  private void setCurrentChainIndex(int value) {
    
    currentChainIndex_ = value;
  }
  /**
   * <code>int32 current_chain_index = 2;</code>
   */
  private void clearCurrentChainIndex() {
    
    currentChainIndex_ = 0;
  }

  public static com.halloapp.proto.clients.SenderState parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static com.halloapp.proto.clients.SenderState parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static com.halloapp.proto.clients.SenderState parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static com.halloapp.proto.clients.SenderState parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static com.halloapp.proto.clients.SenderState parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static com.halloapp.proto.clients.SenderState parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static com.halloapp.proto.clients.SenderState parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input);
  }
  public static com.halloapp.proto.clients.SenderState parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input, extensionRegistry);
  }
  public static com.halloapp.proto.clients.SenderState parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return parseDelimitedFrom(DEFAULT_INSTANCE, input);
  }
  public static com.halloapp.proto.clients.SenderState parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return parseDelimitedFrom(DEFAULT_INSTANCE, input, extensionRegistry);
  }
  public static com.halloapp.proto.clients.SenderState parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input);
  }
  public static com.halloapp.proto.clients.SenderState parseFrom(
      com.google.protobuf.CodedInputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input, extensionRegistry);
  }

  public static Builder newBuilder() {
    return (Builder) DEFAULT_INSTANCE.createBuilder();
  }
  public static Builder newBuilder(com.halloapp.proto.clients.SenderState prototype) {
    return (Builder) DEFAULT_INSTANCE.createBuilder(prototype);
  }

  /**
   * Protobuf type {@code clients.SenderState}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessageLite.Builder<
        com.halloapp.proto.clients.SenderState, Builder> implements
      // @@protoc_insertion_point(builder_implements:clients.SenderState)
      com.halloapp.proto.clients.SenderStateOrBuilder {
    // Construct using com.halloapp.proto.clients.SenderState.newBuilder()
    private Builder() {
      super(DEFAULT_INSTANCE);
    }


    /**
     * <code>.clients.SenderKey sender_key = 1;</code>
     */
    @java.lang.Override
    public boolean hasSenderKey() {
      return instance.hasSenderKey();
    }
    /**
     * <code>.clients.SenderKey sender_key = 1;</code>
     */
    @java.lang.Override
    public com.halloapp.proto.clients.SenderKey getSenderKey() {
      return instance.getSenderKey();
    }
    /**
     * <code>.clients.SenderKey sender_key = 1;</code>
     */
    public Builder setSenderKey(com.halloapp.proto.clients.SenderKey value) {
      copyOnWrite();
      instance.setSenderKey(value);
      return this;
      }
    /**
     * <code>.clients.SenderKey sender_key = 1;</code>
     */
    public Builder setSenderKey(
        com.halloapp.proto.clients.SenderKey.Builder builderForValue) {
      copyOnWrite();
      instance.setSenderKey(builderForValue.build());
      return this;
    }
    /**
     * <code>.clients.SenderKey sender_key = 1;</code>
     */
    public Builder mergeSenderKey(com.halloapp.proto.clients.SenderKey value) {
      copyOnWrite();
      instance.mergeSenderKey(value);
      return this;
    }
    /**
     * <code>.clients.SenderKey sender_key = 1;</code>
     */
    public Builder clearSenderKey() {  copyOnWrite();
      instance.clearSenderKey();
      return this;
    }

    /**
     * <code>int32 current_chain_index = 2;</code>
     * @return The currentChainIndex.
     */
    @java.lang.Override
    public int getCurrentChainIndex() {
      return instance.getCurrentChainIndex();
    }
    /**
     * <code>int32 current_chain_index = 2;</code>
     * @param value The currentChainIndex to set.
     * @return This builder for chaining.
     */
    public Builder setCurrentChainIndex(int value) {
      copyOnWrite();
      instance.setCurrentChainIndex(value);
      return this;
    }
    /**
     * <code>int32 current_chain_index = 2;</code>
     * @return This builder for chaining.
     */
    public Builder clearCurrentChainIndex() {
      copyOnWrite();
      instance.clearCurrentChainIndex();
      return this;
    }

    // @@protoc_insertion_point(builder_scope:clients.SenderState)
  }
  @java.lang.Override
  @java.lang.SuppressWarnings({"unchecked", "fallthrough"})
  protected final java.lang.Object dynamicMethod(
      com.google.protobuf.GeneratedMessageLite.MethodToInvoke method,
      java.lang.Object arg0, java.lang.Object arg1) {
    switch (method) {
      case NEW_MUTABLE_INSTANCE: {
        return new com.halloapp.proto.clients.SenderState();
      }
      case NEW_BUILDER: {
        return new Builder();
      }
      case BUILD_MESSAGE_INFO: {
          java.lang.Object[] objects = new java.lang.Object[] {
            "senderKey_",
            "currentChainIndex_",
          };
          java.lang.String info =
              "\u0000\u0002\u0000\u0000\u0001\u0002\u0002\u0000\u0000\u0000\u0001\t\u0002\u0004" +
              "";
          return newMessageInfo(DEFAULT_INSTANCE, info, objects);
      }
      // fall through
      case GET_DEFAULT_INSTANCE: {
        return DEFAULT_INSTANCE;
      }
      case GET_PARSER: {
        com.google.protobuf.Parser<com.halloapp.proto.clients.SenderState> parser = PARSER;
        if (parser == null) {
          synchronized (com.halloapp.proto.clients.SenderState.class) {
            parser = PARSER;
            if (parser == null) {
              parser =
                  new DefaultInstanceBasedParser<com.halloapp.proto.clients.SenderState>(
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


  // @@protoc_insertion_point(class_scope:clients.SenderState)
  private static final com.halloapp.proto.clients.SenderState DEFAULT_INSTANCE;
  static {
    SenderState defaultInstance = new SenderState();
    // New instances are implicitly immutable so no need to make
    // immutable.
    DEFAULT_INSTANCE = defaultInstance;
    com.google.protobuf.GeneratedMessageLite.registerDefaultInstance(
      SenderState.class, defaultInstance);
  }

  public static com.halloapp.proto.clients.SenderState getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static volatile com.google.protobuf.Parser<SenderState> PARSER;

  public static com.google.protobuf.Parser<SenderState> parser() {
    return DEFAULT_INSTANCE.getParserForType();
  }
}

