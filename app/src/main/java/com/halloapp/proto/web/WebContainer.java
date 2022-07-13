// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: web.proto

package com.halloapp.proto.web;

/**
 * Protobuf type {@code web.WebContainer}
 */
public  final class WebContainer extends
    com.google.protobuf.GeneratedMessageLite<
        WebContainer, WebContainer.Builder> implements
    // @@protoc_insertion_point(message_implements:web.WebContainer)
    WebContainerOrBuilder {
  private WebContainer() {
  }
  private int payloadCase_ = 0;
  private java.lang.Object payload_;
  public enum PayloadCase {
    NOISE_MESSAGE(1),
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
        case 1: return NOISE_MESSAGE;
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

  public static final int NOISE_MESSAGE_FIELD_NUMBER = 1;
  /**
   * <code>.web.NoiseMessage noise_message = 1;</code>
   */
  @java.lang.Override
  public boolean hasNoiseMessage() {
    return payloadCase_ == 1;
  }
  /**
   * <code>.web.NoiseMessage noise_message = 1;</code>
   */
  @java.lang.Override
  public com.halloapp.proto.web.NoiseMessage getNoiseMessage() {
    if (payloadCase_ == 1) {
       return (com.halloapp.proto.web.NoiseMessage) payload_;
    }
    return com.halloapp.proto.web.NoiseMessage.getDefaultInstance();
  }
  /**
   * <code>.web.NoiseMessage noise_message = 1;</code>
   */
  private void setNoiseMessage(com.halloapp.proto.web.NoiseMessage value) {
    value.getClass();
  payload_ = value;
    payloadCase_ = 1;
  }
  /**
   * <code>.web.NoiseMessage noise_message = 1;</code>
   */
  private void mergeNoiseMessage(com.halloapp.proto.web.NoiseMessage value) {
    value.getClass();
  if (payloadCase_ == 1 &&
        payload_ != com.halloapp.proto.web.NoiseMessage.getDefaultInstance()) {
      payload_ = com.halloapp.proto.web.NoiseMessage.newBuilder((com.halloapp.proto.web.NoiseMessage) payload_)
          .mergeFrom(value).buildPartial();
    } else {
      payload_ = value;
    }
    payloadCase_ = 1;
  }
  /**
   * <code>.web.NoiseMessage noise_message = 1;</code>
   */
  private void clearNoiseMessage() {
    if (payloadCase_ == 1) {
      payloadCase_ = 0;
      payload_ = null;
    }
  }

  public static com.halloapp.proto.web.WebContainer parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static com.halloapp.proto.web.WebContainer parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static com.halloapp.proto.web.WebContainer parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static com.halloapp.proto.web.WebContainer parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static com.halloapp.proto.web.WebContainer parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static com.halloapp.proto.web.WebContainer parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static com.halloapp.proto.web.WebContainer parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input);
  }
  public static com.halloapp.proto.web.WebContainer parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input, extensionRegistry);
  }
  public static com.halloapp.proto.web.WebContainer parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return parseDelimitedFrom(DEFAULT_INSTANCE, input);
  }
  public static com.halloapp.proto.web.WebContainer parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return parseDelimitedFrom(DEFAULT_INSTANCE, input, extensionRegistry);
  }
  public static com.halloapp.proto.web.WebContainer parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input);
  }
  public static com.halloapp.proto.web.WebContainer parseFrom(
      com.google.protobuf.CodedInputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input, extensionRegistry);
  }

  public static Builder newBuilder() {
    return (Builder) DEFAULT_INSTANCE.createBuilder();
  }
  public static Builder newBuilder(com.halloapp.proto.web.WebContainer prototype) {
    return (Builder) DEFAULT_INSTANCE.createBuilder(prototype);
  }

  /**
   * Protobuf type {@code web.WebContainer}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessageLite.Builder<
        com.halloapp.proto.web.WebContainer, Builder> implements
      // @@protoc_insertion_point(builder_implements:web.WebContainer)
      com.halloapp.proto.web.WebContainerOrBuilder {
    // Construct using com.halloapp.proto.web.WebContainer.newBuilder()
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
     * <code>.web.NoiseMessage noise_message = 1;</code>
     */
    @java.lang.Override
    public boolean hasNoiseMessage() {
      return instance.hasNoiseMessage();
    }
    /**
     * <code>.web.NoiseMessage noise_message = 1;</code>
     */
    @java.lang.Override
    public com.halloapp.proto.web.NoiseMessage getNoiseMessage() {
      return instance.getNoiseMessage();
    }
    /**
     * <code>.web.NoiseMessage noise_message = 1;</code>
     */
    public Builder setNoiseMessage(com.halloapp.proto.web.NoiseMessage value) {
      copyOnWrite();
      instance.setNoiseMessage(value);
      return this;
    }
    /**
     * <code>.web.NoiseMessage noise_message = 1;</code>
     */
    public Builder setNoiseMessage(
        com.halloapp.proto.web.NoiseMessage.Builder builderForValue) {
      copyOnWrite();
      instance.setNoiseMessage(builderForValue.build());
      return this;
    }
    /**
     * <code>.web.NoiseMessage noise_message = 1;</code>
     */
    public Builder mergeNoiseMessage(com.halloapp.proto.web.NoiseMessage value) {
      copyOnWrite();
      instance.mergeNoiseMessage(value);
      return this;
    }
    /**
     * <code>.web.NoiseMessage noise_message = 1;</code>
     */
    public Builder clearNoiseMessage() {
      copyOnWrite();
      instance.clearNoiseMessage();
      return this;
    }

    // @@protoc_insertion_point(builder_scope:web.WebContainer)
  }
  @java.lang.Override
  @java.lang.SuppressWarnings({"unchecked", "fallthrough"})
  protected final java.lang.Object dynamicMethod(
      com.google.protobuf.GeneratedMessageLite.MethodToInvoke method,
      java.lang.Object arg0, java.lang.Object arg1) {
    switch (method) {
      case NEW_MUTABLE_INSTANCE: {
        return new com.halloapp.proto.web.WebContainer();
      }
      case NEW_BUILDER: {
        return new Builder();
      }
      case BUILD_MESSAGE_INFO: {
          java.lang.Object[] objects = new java.lang.Object[] {
            "payload_",
            "payloadCase_",
            com.halloapp.proto.web.NoiseMessage.class,
          };
          java.lang.String info =
              "\u0000\u0001\u0001\u0000\u0001\u0001\u0001\u0000\u0000\u0000\u0001<\u0000";
          return newMessageInfo(DEFAULT_INSTANCE, info, objects);
      }
      // fall through
      case GET_DEFAULT_INSTANCE: {
        return DEFAULT_INSTANCE;
      }
      case GET_PARSER: {
        com.google.protobuf.Parser<com.halloapp.proto.web.WebContainer> parser = PARSER;
        if (parser == null) {
          synchronized (com.halloapp.proto.web.WebContainer.class) {
            parser = PARSER;
            if (parser == null) {
              parser =
                  new DefaultInstanceBasedParser<com.halloapp.proto.web.WebContainer>(
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


  // @@protoc_insertion_point(class_scope:web.WebContainer)
  private static final com.halloapp.proto.web.WebContainer DEFAULT_INSTANCE;
  static {
    WebContainer defaultInstance = new WebContainer();
    // New instances are implicitly immutable so no need to make
    // immutable.
    DEFAULT_INSTANCE = defaultInstance;
    com.google.protobuf.GeneratedMessageLite.registerDefaultInstance(
      WebContainer.class, defaultInstance);
  }

  public static com.halloapp.proto.web.WebContainer getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static volatile com.google.protobuf.Parser<WebContainer> PARSER;

  public static com.google.protobuf.Parser<WebContainer> parser() {
    return DEFAULT_INSTANCE.getParserForType();
  }
}
