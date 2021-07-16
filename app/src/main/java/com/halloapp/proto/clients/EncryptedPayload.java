// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: clients.proto

package com.halloapp.proto.clients;

/**
 * Protobuf type {@code clients.EncryptedPayload}
 */
public  final class EncryptedPayload extends
    com.google.protobuf.GeneratedMessageLite<
        EncryptedPayload, EncryptedPayload.Builder> implements
    // @@protoc_insertion_point(message_implements:clients.EncryptedPayload)
    EncryptedPayloadOrBuilder {
  private EncryptedPayload() {
  }
  private int payloadCase_ = 0;
  private java.lang.Object payload_;
  public enum PayloadCase {
    SENDER_STATE_ENCRYPTED_PAYLOAD(1),
    ONE_TO_ONE_ENCRYPTED_PAYLOAD(2),
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
        case 1: return SENDER_STATE_ENCRYPTED_PAYLOAD;
        case 2: return ONE_TO_ONE_ENCRYPTED_PAYLOAD;
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

  public static final int SENDER_STATE_ENCRYPTED_PAYLOAD_FIELD_NUMBER = 1;
  /**
   * <code>bytes sender_state_encrypted_payload = 1;</code>
   * @return Whether the senderStateEncryptedPayload field is set.
   */
  @java.lang.Override
  public boolean hasSenderStateEncryptedPayload() {
    return payloadCase_ == 1;
  }
  /**
   * <code>bytes sender_state_encrypted_payload = 1;</code>
   * @return The senderStateEncryptedPayload.
   */
  @java.lang.Override
  public com.google.protobuf.ByteString getSenderStateEncryptedPayload() {
    if (payloadCase_ == 1) {
      return (com.google.protobuf.ByteString) payload_;
    }
    return com.google.protobuf.ByteString.EMPTY;
  }
  /**
   * <code>bytes sender_state_encrypted_payload = 1;</code>
   * @param value The senderStateEncryptedPayload to set.
   */
  private void setSenderStateEncryptedPayload(com.google.protobuf.ByteString value) {
    value.getClass();
  payloadCase_ = 1;
    payload_ = value;
  }
  /**
   * <code>bytes sender_state_encrypted_payload = 1;</code>
   */
  private void clearSenderStateEncryptedPayload() {
    if (payloadCase_ == 1) {
      payloadCase_ = 0;
      payload_ = null;
    }
  }

  public static final int ONE_TO_ONE_ENCRYPTED_PAYLOAD_FIELD_NUMBER = 2;
  /**
   * <code>bytes one_to_one_encrypted_payload = 2;</code>
   * @return Whether the oneToOneEncryptedPayload field is set.
   */
  @java.lang.Override
  public boolean hasOneToOneEncryptedPayload() {
    return payloadCase_ == 2;
  }
  /**
   * <code>bytes one_to_one_encrypted_payload = 2;</code>
   * @return The oneToOneEncryptedPayload.
   */
  @java.lang.Override
  public com.google.protobuf.ByteString getOneToOneEncryptedPayload() {
    if (payloadCase_ == 2) {
      return (com.google.protobuf.ByteString) payload_;
    }
    return com.google.protobuf.ByteString.EMPTY;
  }
  /**
   * <code>bytes one_to_one_encrypted_payload = 2;</code>
   * @param value The oneToOneEncryptedPayload to set.
   */
  private void setOneToOneEncryptedPayload(com.google.protobuf.ByteString value) {
    value.getClass();
  payloadCase_ = 2;
    payload_ = value;
  }
  /**
   * <code>bytes one_to_one_encrypted_payload = 2;</code>
   */
  private void clearOneToOneEncryptedPayload() {
    if (payloadCase_ == 2) {
      payloadCase_ = 0;
      payload_ = null;
    }
  }

  public static com.halloapp.proto.clients.EncryptedPayload parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static com.halloapp.proto.clients.EncryptedPayload parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static com.halloapp.proto.clients.EncryptedPayload parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static com.halloapp.proto.clients.EncryptedPayload parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static com.halloapp.proto.clients.EncryptedPayload parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static com.halloapp.proto.clients.EncryptedPayload parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static com.halloapp.proto.clients.EncryptedPayload parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input);
  }
  public static com.halloapp.proto.clients.EncryptedPayload parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input, extensionRegistry);
  }
  public static com.halloapp.proto.clients.EncryptedPayload parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return parseDelimitedFrom(DEFAULT_INSTANCE, input);
  }
  public static com.halloapp.proto.clients.EncryptedPayload parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return parseDelimitedFrom(DEFAULT_INSTANCE, input, extensionRegistry);
  }
  public static com.halloapp.proto.clients.EncryptedPayload parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input);
  }
  public static com.halloapp.proto.clients.EncryptedPayload parseFrom(
      com.google.protobuf.CodedInputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input, extensionRegistry);
  }

  public static Builder newBuilder() {
    return (Builder) DEFAULT_INSTANCE.createBuilder();
  }
  public static Builder newBuilder(com.halloapp.proto.clients.EncryptedPayload prototype) {
    return (Builder) DEFAULT_INSTANCE.createBuilder(prototype);
  }

  /**
   * Protobuf type {@code clients.EncryptedPayload}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessageLite.Builder<
        com.halloapp.proto.clients.EncryptedPayload, Builder> implements
      // @@protoc_insertion_point(builder_implements:clients.EncryptedPayload)
      com.halloapp.proto.clients.EncryptedPayloadOrBuilder {
    // Construct using com.halloapp.proto.clients.EncryptedPayload.newBuilder()
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
     * <code>bytes sender_state_encrypted_payload = 1;</code>
     * @return Whether the senderStateEncryptedPayload field is set.
     */
    @java.lang.Override
    public boolean hasSenderStateEncryptedPayload() {
      return instance.hasSenderStateEncryptedPayload();
    }
    /**
     * <code>bytes sender_state_encrypted_payload = 1;</code>
     * @return The senderStateEncryptedPayload.
     */
    @java.lang.Override
    public com.google.protobuf.ByteString getSenderStateEncryptedPayload() {
      return instance.getSenderStateEncryptedPayload();
    }
    /**
     * <code>bytes sender_state_encrypted_payload = 1;</code>
     * @param value The senderStateEncryptedPayload to set.
     * @return This builder for chaining.
     */
    public Builder setSenderStateEncryptedPayload(com.google.protobuf.ByteString value) {
      copyOnWrite();
      instance.setSenderStateEncryptedPayload(value);
      return this;
    }
    /**
     * <code>bytes sender_state_encrypted_payload = 1;</code>
     * @return This builder for chaining.
     */
    public Builder clearSenderStateEncryptedPayload() {
      copyOnWrite();
      instance.clearSenderStateEncryptedPayload();
      return this;
    }

    /**
     * <code>bytes one_to_one_encrypted_payload = 2;</code>
     * @return Whether the oneToOneEncryptedPayload field is set.
     */
    @java.lang.Override
    public boolean hasOneToOneEncryptedPayload() {
      return instance.hasOneToOneEncryptedPayload();
    }
    /**
     * <code>bytes one_to_one_encrypted_payload = 2;</code>
     * @return The oneToOneEncryptedPayload.
     */
    @java.lang.Override
    public com.google.protobuf.ByteString getOneToOneEncryptedPayload() {
      return instance.getOneToOneEncryptedPayload();
    }
    /**
     * <code>bytes one_to_one_encrypted_payload = 2;</code>
     * @param value The oneToOneEncryptedPayload to set.
     * @return This builder for chaining.
     */
    public Builder setOneToOneEncryptedPayload(com.google.protobuf.ByteString value) {
      copyOnWrite();
      instance.setOneToOneEncryptedPayload(value);
      return this;
    }
    /**
     * <code>bytes one_to_one_encrypted_payload = 2;</code>
     * @return This builder for chaining.
     */
    public Builder clearOneToOneEncryptedPayload() {
      copyOnWrite();
      instance.clearOneToOneEncryptedPayload();
      return this;
    }

    // @@protoc_insertion_point(builder_scope:clients.EncryptedPayload)
  }
  @java.lang.Override
  @java.lang.SuppressWarnings({"unchecked", "fallthrough"})
  protected final java.lang.Object dynamicMethod(
      com.google.protobuf.GeneratedMessageLite.MethodToInvoke method,
      java.lang.Object arg0, java.lang.Object arg1) {
    switch (method) {
      case NEW_MUTABLE_INSTANCE: {
        return new com.halloapp.proto.clients.EncryptedPayload();
      }
      case NEW_BUILDER: {
        return new Builder();
      }
      case BUILD_MESSAGE_INFO: {
          java.lang.Object[] objects = new java.lang.Object[] {
            "payload_",
            "payloadCase_",
          };
          java.lang.String info =
              "\u0000\u0002\u0001\u0000\u0001\u0002\u0002\u0000\u0000\u0000\u0001=\u0000\u0002=" +
              "\u0000";
          return newMessageInfo(DEFAULT_INSTANCE, info, objects);
      }
      // fall through
      case GET_DEFAULT_INSTANCE: {
        return DEFAULT_INSTANCE;
      }
      case GET_PARSER: {
        com.google.protobuf.Parser<com.halloapp.proto.clients.EncryptedPayload> parser = PARSER;
        if (parser == null) {
          synchronized (com.halloapp.proto.clients.EncryptedPayload.class) {
            parser = PARSER;
            if (parser == null) {
              parser =
                  new DefaultInstanceBasedParser<com.halloapp.proto.clients.EncryptedPayload>(
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


  // @@protoc_insertion_point(class_scope:clients.EncryptedPayload)
  private static final com.halloapp.proto.clients.EncryptedPayload DEFAULT_INSTANCE;
  static {
    EncryptedPayload defaultInstance = new EncryptedPayload();
    // New instances are implicitly immutable so no need to make
    // immutable.
    DEFAULT_INSTANCE = defaultInstance;
    com.google.protobuf.GeneratedMessageLite.registerDefaultInstance(
      EncryptedPayload.class, defaultInstance);
  }

  public static com.halloapp.proto.clients.EncryptedPayload getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static volatile com.google.protobuf.Parser<EncryptedPayload> PARSER;

  public static com.google.protobuf.Parser<EncryptedPayload> parser() {
    return DEFAULT_INSTANCE.getParserForType();
  }
}

