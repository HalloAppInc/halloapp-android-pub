// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: server.proto

package com.halloapp.proto.server;

/**
 * Protobuf type {@code server.WebStanza}
 */
public  final class WebStanza extends
    com.google.protobuf.GeneratedMessageLite<
        WebStanza, WebStanza.Builder> implements
    // @@protoc_insertion_point(message_implements:server.WebStanza)
    WebStanzaOrBuilder {
  private WebStanza() {
    staticKey_ = com.google.protobuf.ByteString.EMPTY;
  }
  private int payloadCase_ = 0;
  private java.lang.Object payload_;
  public enum PayloadCase {
    CONTENT(2),
    NOISE_MESSAGE(3),
    PAYLOAD_NOT_SET(0);
    private final int value;
    private PayloadCase(int value) {
      this.value = value;
    }
    /**
     * @deprecated Use {@link #forNumber(int)} instead.
     */
    @java.lang.Deprecated
    public static PayloadCase valueOf(int value) {
      return forNumber(value);
    }

    public static PayloadCase forNumber(int value) {
      switch (value) {
        case 2: return CONTENT;
        case 3: return NOISE_MESSAGE;
        case 0: return PAYLOAD_NOT_SET;
        default: return null;
      }
    }
    public int getNumber() {
      return this.value;
    }
  };

  @java.lang.Override
  public PayloadCase
  getPayloadCase() {
    return PayloadCase.forNumber(
        payloadCase_);
  }

  private void clearPayload() {
    payloadCase_ = 0;
    payload_ = null;
  }

  public static final int STATIC_KEY_FIELD_NUMBER = 1;
  private com.google.protobuf.ByteString staticKey_;
  /**
   * <pre>
   * Used to identify the user. Must be set on every packet exchanged
   * </pre>
   *
   * <code>bytes static_key = 1;</code>
   * @return The staticKey.
   */
  @java.lang.Override
  public com.google.protobuf.ByteString getStaticKey() {
    return staticKey_;
  }
  /**
   * <pre>
   * Used to identify the user. Must be set on every packet exchanged
   * </pre>
   *
   * <code>bytes static_key = 1;</code>
   * @param value The staticKey to set.
   */
  private void setStaticKey(com.google.protobuf.ByteString value) {
    value.getClass();
  
    staticKey_ = value;
  }
  /**
   * <pre>
   * Used to identify the user. Must be set on every packet exchanged
   * </pre>
   *
   * <code>bytes static_key = 1;</code>
   */
  private void clearStaticKey() {
    
    staticKey_ = getDefaultInstance().getStaticKey();
  }

  public static final int CONTENT_FIELD_NUMBER = 2;
  /**
   * <pre>
   * Noise encrypted content
   * </pre>
   *
   * <code>bytes content = 2;</code>
   * @return The content.
   */
  @java.lang.Override
  public com.google.protobuf.ByteString getContent() {
    if (payloadCase_ == 2) {
      return (com.google.protobuf.ByteString) payload_;
    }
    return com.google.protobuf.ByteString.EMPTY;
  }
  /**
   * <pre>
   * Noise encrypted content
   * </pre>
   *
   * <code>bytes content = 2;</code>
   * @param value The content to set.
   */
  private void setContent(com.google.protobuf.ByteString value) {
    value.getClass();
  payloadCase_ = 2;
    payload_ = value;
  }
  /**
   * <pre>
   * Noise encrypted content
   * </pre>
   *
   * <code>bytes content = 2;</code>
   */
  private void clearContent() {
    if (payloadCase_ == 2) {
      payloadCase_ = 0;
      payload_ = null;
    }
  }

  public static final int NOISE_MESSAGE_FIELD_NUMBER = 3;
  /**
   * <code>.server.NoiseMessage noise_message = 3;</code>
   */
  @java.lang.Override
  public boolean hasNoiseMessage() {
    return payloadCase_ == 3;
  }
  /**
   * <code>.server.NoiseMessage noise_message = 3;</code>
   */
  @java.lang.Override
  public com.halloapp.proto.server.NoiseMessage getNoiseMessage() {
    if (payloadCase_ == 3) {
       return (com.halloapp.proto.server.NoiseMessage) payload_;
    }
    return com.halloapp.proto.server.NoiseMessage.getDefaultInstance();
  }
  /**
   * <code>.server.NoiseMessage noise_message = 3;</code>
   */
  private void setNoiseMessage(com.halloapp.proto.server.NoiseMessage value) {
    value.getClass();
  payload_ = value;
    payloadCase_ = 3;
  }
  /**
   * <code>.server.NoiseMessage noise_message = 3;</code>
   */
  private void mergeNoiseMessage(com.halloapp.proto.server.NoiseMessage value) {
    value.getClass();
  if (payloadCase_ == 3 &&
        payload_ != com.halloapp.proto.server.NoiseMessage.getDefaultInstance()) {
      payload_ = com.halloapp.proto.server.NoiseMessage.newBuilder((com.halloapp.proto.server.NoiseMessage) payload_)
          .mergeFrom(value).buildPartial();
    } else {
      payload_ = value;
    }
    payloadCase_ = 3;
  }
  /**
   * <code>.server.NoiseMessage noise_message = 3;</code>
   */
  private void clearNoiseMessage() {
    if (payloadCase_ == 3) {
      payloadCase_ = 0;
      payload_ = null;
    }
  }

  public static com.halloapp.proto.server.WebStanza parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static com.halloapp.proto.server.WebStanza parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static com.halloapp.proto.server.WebStanza parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static com.halloapp.proto.server.WebStanza parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static com.halloapp.proto.server.WebStanza parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static com.halloapp.proto.server.WebStanza parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static com.halloapp.proto.server.WebStanza parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input);
  }
  public static com.halloapp.proto.server.WebStanza parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input, extensionRegistry);
  }
  public static com.halloapp.proto.server.WebStanza parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return parseDelimitedFrom(DEFAULT_INSTANCE, input);
  }
  public static com.halloapp.proto.server.WebStanza parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return parseDelimitedFrom(DEFAULT_INSTANCE, input, extensionRegistry);
  }
  public static com.halloapp.proto.server.WebStanza parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input);
  }
  public static com.halloapp.proto.server.WebStanza parseFrom(
      com.google.protobuf.CodedInputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input, extensionRegistry);
  }

  public static Builder newBuilder() {
    return (Builder) DEFAULT_INSTANCE.createBuilder();
  }
  public static Builder newBuilder(com.halloapp.proto.server.WebStanza prototype) {
    return (Builder) DEFAULT_INSTANCE.createBuilder(prototype);
  }

  /**
   * Protobuf type {@code server.WebStanza}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessageLite.Builder<
        com.halloapp.proto.server.WebStanza, Builder> implements
      // @@protoc_insertion_point(builder_implements:server.WebStanza)
      com.halloapp.proto.server.WebStanzaOrBuilder {
    // Construct using com.halloapp.proto.server.WebStanza.newBuilder()
    private Builder() {
      super(DEFAULT_INSTANCE);
    }

    @java.lang.Override
    public PayloadCase
        getPayloadCase() {
      return instance.getPayloadCase();
    }

    public Builder clearPayload() {
      copyOnWrite();
      instance.clearPayload();
      return this;
    }


    /**
     * <pre>
     * Used to identify the user. Must be set on every packet exchanged
     * </pre>
     *
     * <code>bytes static_key = 1;</code>
     * @return The staticKey.
     */
    @java.lang.Override
    public com.google.protobuf.ByteString getStaticKey() {
      return instance.getStaticKey();
    }
    /**
     * <pre>
     * Used to identify the user. Must be set on every packet exchanged
     * </pre>
     *
     * <code>bytes static_key = 1;</code>
     * @param value The staticKey to set.
     * @return This builder for chaining.
     */
    public Builder setStaticKey(com.google.protobuf.ByteString value) {
      copyOnWrite();
      instance.setStaticKey(value);
      return this;
    }
    /**
     * <pre>
     * Used to identify the user. Must be set on every packet exchanged
     * </pre>
     *
     * <code>bytes static_key = 1;</code>
     * @return This builder for chaining.
     */
    public Builder clearStaticKey() {
      copyOnWrite();
      instance.clearStaticKey();
      return this;
    }

    /**
     * <pre>
     * Noise encrypted content
     * </pre>
     *
     * <code>bytes content = 2;</code>
     * @return The content.
     */
    @java.lang.Override
    public com.google.protobuf.ByteString getContent() {
      return instance.getContent();
    }
    /**
     * <pre>
     * Noise encrypted content
     * </pre>
     *
     * <code>bytes content = 2;</code>
     * @param value The content to set.
     * @return This builder for chaining.
     */
    public Builder setContent(com.google.protobuf.ByteString value) {
      copyOnWrite();
      instance.setContent(value);
      return this;
    }
    /**
     * <pre>
     * Noise encrypted content
     * </pre>
     *
     * <code>bytes content = 2;</code>
     * @return This builder for chaining.
     */
    public Builder clearContent() {
      copyOnWrite();
      instance.clearContent();
      return this;
    }

    /**
     * <code>.server.NoiseMessage noise_message = 3;</code>
     */
    @java.lang.Override
    public boolean hasNoiseMessage() {
      return instance.hasNoiseMessage();
    }
    /**
     * <code>.server.NoiseMessage noise_message = 3;</code>
     */
    @java.lang.Override
    public com.halloapp.proto.server.NoiseMessage getNoiseMessage() {
      return instance.getNoiseMessage();
    }
    /**
     * <code>.server.NoiseMessage noise_message = 3;</code>
     */
    public Builder setNoiseMessage(com.halloapp.proto.server.NoiseMessage value) {
      copyOnWrite();
      instance.setNoiseMessage(value);
      return this;
    }
    /**
     * <code>.server.NoiseMessage noise_message = 3;</code>
     */
    public Builder setNoiseMessage(
        com.halloapp.proto.server.NoiseMessage.Builder builderForValue) {
      copyOnWrite();
      instance.setNoiseMessage(builderForValue.build());
      return this;
    }
    /**
     * <code>.server.NoiseMessage noise_message = 3;</code>
     */
    public Builder mergeNoiseMessage(com.halloapp.proto.server.NoiseMessage value) {
      copyOnWrite();
      instance.mergeNoiseMessage(value);
      return this;
    }
    /**
     * <code>.server.NoiseMessage noise_message = 3;</code>
     */
    public Builder clearNoiseMessage() {
      copyOnWrite();
      instance.clearNoiseMessage();
      return this;
    }

    // @@protoc_insertion_point(builder_scope:server.WebStanza)
  }
  @java.lang.Override
  @java.lang.SuppressWarnings({"unchecked", "fallthrough"})
  protected final java.lang.Object dynamicMethod(
      com.google.protobuf.GeneratedMessageLite.MethodToInvoke method,
      java.lang.Object arg0, java.lang.Object arg1) {
    switch (method) {
      case NEW_MUTABLE_INSTANCE: {
        return new com.halloapp.proto.server.WebStanza();
      }
      case NEW_BUILDER: {
        return new Builder();
      }
      case BUILD_MESSAGE_INFO: {
          java.lang.Object[] objects = new java.lang.Object[] {
            "payload_",
            "payloadCase_",
            "staticKey_",
            com.halloapp.proto.server.NoiseMessage.class,
          };
          java.lang.String info =
              "\u0000\u0003\u0001\u0000\u0001\u0003\u0003\u0000\u0000\u0000\u0001\n\u0002=\u0000" +
              "\u0003<\u0000";
          return newMessageInfo(DEFAULT_INSTANCE, info, objects);
      }
      // fall through
      case GET_DEFAULT_INSTANCE: {
        return DEFAULT_INSTANCE;
      }
      case GET_PARSER: {
        com.google.protobuf.Parser<com.halloapp.proto.server.WebStanza> parser = PARSER;
        if (parser == null) {
          synchronized (com.halloapp.proto.server.WebStanza.class) {
            parser = PARSER;
            if (parser == null) {
              parser =
                  new DefaultInstanceBasedParser<com.halloapp.proto.server.WebStanza>(
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


  // @@protoc_insertion_point(class_scope:server.WebStanza)
  private static final com.halloapp.proto.server.WebStanza DEFAULT_INSTANCE;
  static {
    WebStanza defaultInstance = new WebStanza();
    // New instances are implicitly immutable so no need to make
    // immutable.
    DEFAULT_INSTANCE = defaultInstance;
    com.google.protobuf.GeneratedMessageLite.registerDefaultInstance(
      WebStanza.class, defaultInstance);
  }

  public static com.halloapp.proto.server.WebStanza getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static volatile com.google.protobuf.Parser<WebStanza> PARSER;

  public static com.google.protobuf.Parser<WebStanza> parser() {
    return DEFAULT_INSTANCE.getParserForType();
  }
}

