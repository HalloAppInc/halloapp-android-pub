// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: server.proto

package com.halloapp.proto.server;

/**
 * Protobuf type {@code server.HoldCall}
 */
public  final class HoldCall extends
    com.google.protobuf.GeneratedMessageLite<
        HoldCall, HoldCall.Builder> implements
    // @@protoc_insertion_point(message_implements:server.HoldCall)
    HoldCallOrBuilder {
  private HoldCall() {
    callId_ = "";
  }
  public static final int CALL_ID_FIELD_NUMBER = 1;
  private java.lang.String callId_;
  /**
   * <code>string call_id = 1;</code>
   * @return The callId.
   */
  @java.lang.Override
  public java.lang.String getCallId() {
    return callId_;
  }
  /**
   * <code>string call_id = 1;</code>
   * @return The bytes for callId.
   */
  @java.lang.Override
  public com.google.protobuf.ByteString
      getCallIdBytes() {
    return com.google.protobuf.ByteString.copyFromUtf8(callId_);
  }
  /**
   * <code>string call_id = 1;</code>
   * @param value The callId to set.
   */
  private void setCallId(
      java.lang.String value) {
    value.getClass();
  
    callId_ = value;
  }
  /**
   * <code>string call_id = 1;</code>
   */
  private void clearCallId() {
    
    callId_ = getDefaultInstance().getCallId();
  }
  /**
   * <code>string call_id = 1;</code>
   * @param value The bytes for callId to set.
   */
  private void setCallIdBytes(
      com.google.protobuf.ByteString value) {
    checkByteStringIsUtf8(value);
    callId_ = value.toStringUtf8();
    
  }

  public static final int HOLD_FIELD_NUMBER = 2;
  private boolean hold_;
  /**
   * <code>bool hold = 2;</code>
   * @return The hold.
   */
  @java.lang.Override
  public boolean getHold() {
    return hold_;
  }
  /**
   * <code>bool hold = 2;</code>
   * @param value The hold to set.
   */
  private void setHold(boolean value) {
    
    hold_ = value;
  }
  /**
   * <code>bool hold = 2;</code>
   */
  private void clearHold() {
    
    hold_ = false;
  }

  public static final int TIMESTAMP_MS_FIELD_NUMBER = 3;
  private long timestampMs_;
  /**
   * <code>int64 timestamp_ms = 3;</code>
   * @return The timestampMs.
   */
  @java.lang.Override
  public long getTimestampMs() {
    return timestampMs_;
  }
  /**
   * <code>int64 timestamp_ms = 3;</code>
   * @param value The timestampMs to set.
   */
  private void setTimestampMs(long value) {
    
    timestampMs_ = value;
  }
  /**
   * <code>int64 timestamp_ms = 3;</code>
   */
  private void clearTimestampMs() {
    
    timestampMs_ = 0L;
  }

  public static com.halloapp.proto.server.HoldCall parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static com.halloapp.proto.server.HoldCall parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static com.halloapp.proto.server.HoldCall parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static com.halloapp.proto.server.HoldCall parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static com.halloapp.proto.server.HoldCall parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static com.halloapp.proto.server.HoldCall parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static com.halloapp.proto.server.HoldCall parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input);
  }
  public static com.halloapp.proto.server.HoldCall parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input, extensionRegistry);
  }
  public static com.halloapp.proto.server.HoldCall parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return parseDelimitedFrom(DEFAULT_INSTANCE, input);
  }
  public static com.halloapp.proto.server.HoldCall parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return parseDelimitedFrom(DEFAULT_INSTANCE, input, extensionRegistry);
  }
  public static com.halloapp.proto.server.HoldCall parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input);
  }
  public static com.halloapp.proto.server.HoldCall parseFrom(
      com.google.protobuf.CodedInputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input, extensionRegistry);
  }

  public static Builder newBuilder() {
    return (Builder) DEFAULT_INSTANCE.createBuilder();
  }
  public static Builder newBuilder(com.halloapp.proto.server.HoldCall prototype) {
    return (Builder) DEFAULT_INSTANCE.createBuilder(prototype);
  }

  /**
   * Protobuf type {@code server.HoldCall}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessageLite.Builder<
        com.halloapp.proto.server.HoldCall, Builder> implements
      // @@protoc_insertion_point(builder_implements:server.HoldCall)
      com.halloapp.proto.server.HoldCallOrBuilder {
    // Construct using com.halloapp.proto.server.HoldCall.newBuilder()
    private Builder() {
      super(DEFAULT_INSTANCE);
    }


    /**
     * <code>string call_id = 1;</code>
     * @return The callId.
     */
    @java.lang.Override
    public java.lang.String getCallId() {
      return instance.getCallId();
    }
    /**
     * <code>string call_id = 1;</code>
     * @return The bytes for callId.
     */
    @java.lang.Override
    public com.google.protobuf.ByteString
        getCallIdBytes() {
      return instance.getCallIdBytes();
    }
    /**
     * <code>string call_id = 1;</code>
     * @param value The callId to set.
     * @return This builder for chaining.
     */
    public Builder setCallId(
        java.lang.String value) {
      copyOnWrite();
      instance.setCallId(value);
      return this;
    }
    /**
     * <code>string call_id = 1;</code>
     * @return This builder for chaining.
     */
    public Builder clearCallId() {
      copyOnWrite();
      instance.clearCallId();
      return this;
    }
    /**
     * <code>string call_id = 1;</code>
     * @param value The bytes for callId to set.
     * @return This builder for chaining.
     */
    public Builder setCallIdBytes(
        com.google.protobuf.ByteString value) {
      copyOnWrite();
      instance.setCallIdBytes(value);
      return this;
    }

    /**
     * <code>bool hold = 2;</code>
     * @return The hold.
     */
    @java.lang.Override
    public boolean getHold() {
      return instance.getHold();
    }
    /**
     * <code>bool hold = 2;</code>
     * @param value The hold to set.
     * @return This builder for chaining.
     */
    public Builder setHold(boolean value) {
      copyOnWrite();
      instance.setHold(value);
      return this;
    }
    /**
     * <code>bool hold = 2;</code>
     * @return This builder for chaining.
     */
    public Builder clearHold() {
      copyOnWrite();
      instance.clearHold();
      return this;
    }

    /**
     * <code>int64 timestamp_ms = 3;</code>
     * @return The timestampMs.
     */
    @java.lang.Override
    public long getTimestampMs() {
      return instance.getTimestampMs();
    }
    /**
     * <code>int64 timestamp_ms = 3;</code>
     * @param value The timestampMs to set.
     * @return This builder for chaining.
     */
    public Builder setTimestampMs(long value) {
      copyOnWrite();
      instance.setTimestampMs(value);
      return this;
    }
    /**
     * <code>int64 timestamp_ms = 3;</code>
     * @return This builder for chaining.
     */
    public Builder clearTimestampMs() {
      copyOnWrite();
      instance.clearTimestampMs();
      return this;
    }

    // @@protoc_insertion_point(builder_scope:server.HoldCall)
  }
  @java.lang.Override
  @java.lang.SuppressWarnings({"unchecked", "fallthrough"})
  protected final java.lang.Object dynamicMethod(
      com.google.protobuf.GeneratedMessageLite.MethodToInvoke method,
      java.lang.Object arg0, java.lang.Object arg1) {
    switch (method) {
      case NEW_MUTABLE_INSTANCE: {
        return new com.halloapp.proto.server.HoldCall();
      }
      case NEW_BUILDER: {
        return new Builder();
      }
      case BUILD_MESSAGE_INFO: {
          java.lang.Object[] objects = new java.lang.Object[] {
            "callId_",
            "hold_",
            "timestampMs_",
          };
          java.lang.String info =
              "\u0000\u0003\u0000\u0000\u0001\u0003\u0003\u0000\u0000\u0000\u0001\u0208\u0002\u0007" +
              "\u0003\u0002";
          return newMessageInfo(DEFAULT_INSTANCE, info, objects);
      }
      // fall through
      case GET_DEFAULT_INSTANCE: {
        return DEFAULT_INSTANCE;
      }
      case GET_PARSER: {
        com.google.protobuf.Parser<com.halloapp.proto.server.HoldCall> parser = PARSER;
        if (parser == null) {
          synchronized (com.halloapp.proto.server.HoldCall.class) {
            parser = PARSER;
            if (parser == null) {
              parser =
                  new DefaultInstanceBasedParser<com.halloapp.proto.server.HoldCall>(
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


  // @@protoc_insertion_point(class_scope:server.HoldCall)
  private static final com.halloapp.proto.server.HoldCall DEFAULT_INSTANCE;
  static {
    HoldCall defaultInstance = new HoldCall();
    // New instances are implicitly immutable so no need to make
    // immutable.
    DEFAULT_INSTANCE = defaultInstance;
    com.google.protobuf.GeneratedMessageLite.registerDefaultInstance(
      HoldCall.class, defaultInstance);
  }

  public static com.halloapp.proto.server.HoldCall getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static volatile com.google.protobuf.Parser<HoldCall> PARSER;

  public static com.google.protobuf.Parser<HoldCall> parser() {
    return DEFAULT_INSTANCE.getParserForType();
  }
}
