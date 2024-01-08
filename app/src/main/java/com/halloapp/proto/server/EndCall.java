// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: server.proto

package com.halloapp.proto.server;

/**
 * Protobuf type {@code server.EndCall}
 */
public  final class EndCall extends
    com.google.protobuf.GeneratedMessageLite<
        EndCall, EndCall.Builder> implements
    // @@protoc_insertion_point(message_implements:server.EndCall)
    EndCallOrBuilder {
  private EndCall() {
    callId_ = "";
  }
  /**
   * Protobuf enum {@code server.EndCall.Reason}
   */
  public enum Reason
      implements com.google.protobuf.Internal.EnumLite {
    /**
     * <code>UNKNOWN = 0;</code>
     */
    UNKNOWN(0),
    /**
     * <pre>
     * receiver rejects the incoming call
     * </pre>
     *
     * <code>REJECT = 1;</code>
     */
    REJECT(1),
    /**
     * <pre>
     * receiver is in another call
     * </pre>
     *
     * <code>BUSY = 2;</code>
     */
    BUSY(2),
    /**
     * <pre>
     * sender or receiver times out the call after ringing for some time.
     * </pre>
     *
     * <code>TIMEOUT = 3;</code>
     */
    TIMEOUT(3),
    /**
     * <pre>
     * initiator or receiver end the call.
     * </pre>
     *
     * <code>CALL_END = 4;</code>
     */
    CALL_END(4),
    /**
     * <pre>
     * initiator hangups before the call connects.
     * </pre>
     *
     * <code>CANCEL = 5;</code>
     */
    CANCEL(5),
    /**
     * <pre>
     * receiver could not decrypt the content.
     * </pre>
     *
     * <code>DECRYPTION_FAILED = 6;</code>
     */
    DECRYPTION_FAILED(6),
    /**
     * <pre>
     * receiver could not encrypt the answer.
     * </pre>
     *
     * <code>ENCRYPTION_FAILED = 7;</code>
     */
    ENCRYPTION_FAILED(7),
    /**
     * <pre>
     * system errors or crashes.
     * </pre>
     *
     * <code>SYSTEM_ERROR = 8;</code>
     */
    SYSTEM_ERROR(8),
    /**
     * <pre>
     * unsupported video calls.
     * </pre>
     *
     * <code>VIDEO_UNSUPPORTED = 9;</code>
     */
    VIDEO_UNSUPPORTED(9),
    /**
     * <pre>
     * ice connection state error.
     * </pre>
     *
     * <code>CONNECTION_ERROR = 10;</code>
     */
    CONNECTION_ERROR(10),
    UNRECOGNIZED(-1),
    ;

    /**
     * <code>UNKNOWN = 0;</code>
     */
    public static final int UNKNOWN_VALUE = 0;
    /**
     * <pre>
     * receiver rejects the incoming call
     * </pre>
     *
     * <code>REJECT = 1;</code>
     */
    public static final int REJECT_VALUE = 1;
    /**
     * <pre>
     * receiver is in another call
     * </pre>
     *
     * <code>BUSY = 2;</code>
     */
    public static final int BUSY_VALUE = 2;
    /**
     * <pre>
     * sender or receiver times out the call after ringing for some time.
     * </pre>
     *
     * <code>TIMEOUT = 3;</code>
     */
    public static final int TIMEOUT_VALUE = 3;
    /**
     * <pre>
     * initiator or receiver end the call.
     * </pre>
     *
     * <code>CALL_END = 4;</code>
     */
    public static final int CALL_END_VALUE = 4;
    /**
     * <pre>
     * initiator hangups before the call connects.
     * </pre>
     *
     * <code>CANCEL = 5;</code>
     */
    public static final int CANCEL_VALUE = 5;
    /**
     * <pre>
     * receiver could not decrypt the content.
     * </pre>
     *
     * <code>DECRYPTION_FAILED = 6;</code>
     */
    public static final int DECRYPTION_FAILED_VALUE = 6;
    /**
     * <pre>
     * receiver could not encrypt the answer.
     * </pre>
     *
     * <code>ENCRYPTION_FAILED = 7;</code>
     */
    public static final int ENCRYPTION_FAILED_VALUE = 7;
    /**
     * <pre>
     * system errors or crashes.
     * </pre>
     *
     * <code>SYSTEM_ERROR = 8;</code>
     */
    public static final int SYSTEM_ERROR_VALUE = 8;
    /**
     * <pre>
     * unsupported video calls.
     * </pre>
     *
     * <code>VIDEO_UNSUPPORTED = 9;</code>
     */
    public static final int VIDEO_UNSUPPORTED_VALUE = 9;
    /**
     * <pre>
     * ice connection state error.
     * </pre>
     *
     * <code>CONNECTION_ERROR = 10;</code>
     */
    public static final int CONNECTION_ERROR_VALUE = 10;


    @java.lang.Override
    public final int getNumber() {
      if (this == UNRECOGNIZED) {
        throw new java.lang.IllegalArgumentException(
            "Can't get the number of an unknown enum value.");
      }
      return value;
    }

    /**
     * @param value The number of the enum to look for.
     * @return The enum associated with the given number.
     * @deprecated Use {@link #forNumber(int)} instead.
     */
    @java.lang.Deprecated
    public static Reason valueOf(int value) {
      return forNumber(value);
    }

    public static Reason forNumber(int value) {
      switch (value) {
        case 0: return UNKNOWN;
        case 1: return REJECT;
        case 2: return BUSY;
        case 3: return TIMEOUT;
        case 4: return CALL_END;
        case 5: return CANCEL;
        case 6: return DECRYPTION_FAILED;
        case 7: return ENCRYPTION_FAILED;
        case 8: return SYSTEM_ERROR;
        case 9: return VIDEO_UNSUPPORTED;
        case 10: return CONNECTION_ERROR;
        default: return null;
      }
    }

    public static com.google.protobuf.Internal.EnumLiteMap<Reason>
        internalGetValueMap() {
      return internalValueMap;
    }
    private static final com.google.protobuf.Internal.EnumLiteMap<
        Reason> internalValueMap =
          new com.google.protobuf.Internal.EnumLiteMap<Reason>() {
            @java.lang.Override
            public Reason findValueByNumber(int number) {
              return Reason.forNumber(number);
            }
          };

    public static com.google.protobuf.Internal.EnumVerifier 
        internalGetVerifier() {
      return ReasonVerifier.INSTANCE;
    }

    private static final class ReasonVerifier implements 
         com.google.protobuf.Internal.EnumVerifier { 
            static final com.google.protobuf.Internal.EnumVerifier           INSTANCE = new ReasonVerifier();
            @java.lang.Override
            public boolean isInRange(int number) {
              return Reason.forNumber(number) != null;
            }
          };

    private final int value;

    private Reason(int value) {
      this.value = value;
    }

    // @@protoc_insertion_point(enum_scope:server.EndCall.Reason)
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
    java.lang.Class<?> valueClass = value.getClass();
  
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

  public static final int REASON_FIELD_NUMBER = 2;
  private int reason_;
  /**
   * <code>.server.EndCall.Reason reason = 2;</code>
   * @return The enum numeric value on the wire for reason.
   */
  @java.lang.Override
  public int getReasonValue() {
    return reason_;
  }
  /**
   * <code>.server.EndCall.Reason reason = 2;</code>
   * @return The reason.
   */
  @java.lang.Override
  public com.halloapp.proto.server.EndCall.Reason getReason() {
    com.halloapp.proto.server.EndCall.Reason result = com.halloapp.proto.server.EndCall.Reason.forNumber(reason_);
    return result == null ? com.halloapp.proto.server.EndCall.Reason.UNRECOGNIZED : result;
  }
  /**
   * <code>.server.EndCall.Reason reason = 2;</code>
   * @param value The enum numeric value on the wire for reason to set.
   */
  private void setReasonValue(int value) {
      reason_ = value;
  }
  /**
   * <code>.server.EndCall.Reason reason = 2;</code>
   * @param value The reason to set.
   */
  private void setReason(com.halloapp.proto.server.EndCall.Reason value) {
    reason_ = value.getNumber();
    
  }
  /**
   * <code>.server.EndCall.Reason reason = 2;</code>
   */
  private void clearReason() {
    
    reason_ = 0;
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

  public static com.halloapp.proto.server.EndCall parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static com.halloapp.proto.server.EndCall parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static com.halloapp.proto.server.EndCall parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static com.halloapp.proto.server.EndCall parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static com.halloapp.proto.server.EndCall parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static com.halloapp.proto.server.EndCall parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static com.halloapp.proto.server.EndCall parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input);
  }
  public static com.halloapp.proto.server.EndCall parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input, extensionRegistry);
  }
  public static com.halloapp.proto.server.EndCall parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return parseDelimitedFrom(DEFAULT_INSTANCE, input);
  }
  public static com.halloapp.proto.server.EndCall parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return parseDelimitedFrom(DEFAULT_INSTANCE, input, extensionRegistry);
  }
  public static com.halloapp.proto.server.EndCall parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input);
  }
  public static com.halloapp.proto.server.EndCall parseFrom(
      com.google.protobuf.CodedInputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input, extensionRegistry);
  }

  public static Builder newBuilder() {
    return (Builder) DEFAULT_INSTANCE.createBuilder();
  }
  public static Builder newBuilder(com.halloapp.proto.server.EndCall prototype) {
    return (Builder) DEFAULT_INSTANCE.createBuilder(prototype);
  }

  /**
   * Protobuf type {@code server.EndCall}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessageLite.Builder<
        com.halloapp.proto.server.EndCall, Builder> implements
      // @@protoc_insertion_point(builder_implements:server.EndCall)
      com.halloapp.proto.server.EndCallOrBuilder {
    // Construct using com.halloapp.proto.server.EndCall.newBuilder()
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
     * <code>.server.EndCall.Reason reason = 2;</code>
     * @return The enum numeric value on the wire for reason.
     */
    @java.lang.Override
    public int getReasonValue() {
      return instance.getReasonValue();
    }
    /**
     * <code>.server.EndCall.Reason reason = 2;</code>
     * @param value The reason to set.
     * @return This builder for chaining.
     */
    public Builder setReasonValue(int value) {
      copyOnWrite();
      instance.setReasonValue(value);
      return this;
    }
    /**
     * <code>.server.EndCall.Reason reason = 2;</code>
     * @return The reason.
     */
    @java.lang.Override
    public com.halloapp.proto.server.EndCall.Reason getReason() {
      return instance.getReason();
    }
    /**
     * <code>.server.EndCall.Reason reason = 2;</code>
     * @param value The enum numeric value on the wire for reason to set.
     * @return This builder for chaining.
     */
    public Builder setReason(com.halloapp.proto.server.EndCall.Reason value) {
      copyOnWrite();
      instance.setReason(value);
      return this;
    }
    /**
     * <code>.server.EndCall.Reason reason = 2;</code>
     * @return This builder for chaining.
     */
    public Builder clearReason() {
      copyOnWrite();
      instance.clearReason();
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

    // @@protoc_insertion_point(builder_scope:server.EndCall)
  }
  @java.lang.Override
  @java.lang.SuppressWarnings({"unchecked", "fallthrough"})
  protected final java.lang.Object dynamicMethod(
      com.google.protobuf.GeneratedMessageLite.MethodToInvoke method,
      java.lang.Object arg0, java.lang.Object arg1) {
    switch (method) {
      case NEW_MUTABLE_INSTANCE: {
        return new com.halloapp.proto.server.EndCall();
      }
      case NEW_BUILDER: {
        return new Builder();
      }
      case BUILD_MESSAGE_INFO: {
          java.lang.Object[] objects = new java.lang.Object[] {
            "callId_",
            "reason_",
            "timestampMs_",
          };
          java.lang.String info =
              "\u0000\u0003\u0000\u0000\u0001\u0003\u0003\u0000\u0000\u0000\u0001\u0208\u0002\f" +
              "\u0003\u0002";
          return newMessageInfo(DEFAULT_INSTANCE, info, objects);
      }
      // fall through
      case GET_DEFAULT_INSTANCE: {
        return DEFAULT_INSTANCE;
      }
      case GET_PARSER: {
        com.google.protobuf.Parser<com.halloapp.proto.server.EndCall> parser = PARSER;
        if (parser == null) {
          synchronized (com.halloapp.proto.server.EndCall.class) {
            parser = PARSER;
            if (parser == null) {
              parser =
                  new DefaultInstanceBasedParser<com.halloapp.proto.server.EndCall>(
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


  // @@protoc_insertion_point(class_scope:server.EndCall)
  private static final com.halloapp.proto.server.EndCall DEFAULT_INSTANCE;
  static {
    EndCall defaultInstance = new EndCall();
    // New instances are implicitly immutable so no need to make
    // immutable.
    DEFAULT_INSTANCE = defaultInstance;
    com.google.protobuf.GeneratedMessageLite.registerDefaultInstance(
      EndCall.class, defaultInstance);
  }

  public static com.halloapp.proto.server.EndCall getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static volatile com.google.protobuf.Parser<EndCall> PARSER;

  public static com.google.protobuf.Parser<EndCall> parser() {
    return DEFAULT_INSTANCE.getParserForType();
  }
}

