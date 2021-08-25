// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: server.proto

package com.halloapp.proto.server;

/**
 * Protobuf type {@code server.OtpResponse}
 */
public  final class OtpResponse extends
    com.google.protobuf.GeneratedMessageLite<
        OtpResponse, OtpResponse.Builder> implements
    // @@protoc_insertion_point(message_implements:server.OtpResponse)
    OtpResponseOrBuilder {
  private OtpResponse() {
    phone_ = "";
  }
  /**
   * Protobuf enum {@code server.OtpResponse.Result}
   */
  public enum Result
      implements com.google.protobuf.Internal.EnumLite {
    /**
     * <code>UNKNOWN_RESULT = 0;</code>
     */
    UNKNOWN_RESULT(0),
    /**
     * <code>SUCCESS = 1;</code>
     */
    SUCCESS(1),
    /**
     * <code>FAILURE = 2;</code>
     */
    FAILURE(2),
    UNRECOGNIZED(-1),
    ;

    /**
     * <code>UNKNOWN_RESULT = 0;</code>
     */
    public static final int UNKNOWN_RESULT_VALUE = 0;
    /**
     * <code>SUCCESS = 1;</code>
     */
    public static final int SUCCESS_VALUE = 1;
    /**
     * <code>FAILURE = 2;</code>
     */
    public static final int FAILURE_VALUE = 2;


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
    public static Result valueOf(int value) {
      return forNumber(value);
    }

    public static Result forNumber(int value) {
      switch (value) {
        case 0: return UNKNOWN_RESULT;
        case 1: return SUCCESS;
        case 2: return FAILURE;
        default: return null;
      }
    }

    public static com.google.protobuf.Internal.EnumLiteMap<Result>
        internalGetValueMap() {
      return internalValueMap;
    }
    private static final com.google.protobuf.Internal.EnumLiteMap<
        Result> internalValueMap =
          new com.google.protobuf.Internal.EnumLiteMap<Result>() {
            @java.lang.Override
            public Result findValueByNumber(int number) {
              return Result.forNumber(number);
            }
          };

    public static com.google.protobuf.Internal.EnumVerifier 
        internalGetVerifier() {
      return ResultVerifier.INSTANCE;
    }

    private static final class ResultVerifier implements 
         com.google.protobuf.Internal.EnumVerifier { 
            static final com.google.protobuf.Internal.EnumVerifier           INSTANCE = new ResultVerifier();
            @java.lang.Override
            public boolean isInRange(int number) {
              return Result.forNumber(number) != null;
            }
          };

    private final int value;

    private Result(int value) {
      this.value = value;
    }

    // @@protoc_insertion_point(enum_scope:server.OtpResponse.Result)
  }

  /**
   * Protobuf enum {@code server.OtpResponse.Reason}
   */
  public enum Reason
      implements com.google.protobuf.Internal.EnumLite {
    /**
     * <code>UNKNOWN_REASON = 0;</code>
     */
    UNKNOWN_REASON(0),
    /**
     * <code>INVALID_PHONE_NUMBER = 1;</code>
     */
    INVALID_PHONE_NUMBER(1),
    /**
     * <code>INVALID_CLIENT_VERSION = 2;</code>
     */
    INVALID_CLIENT_VERSION(2),
    /**
     * <code>BAD_METHOD = 3;</code>
     */
    BAD_METHOD(3),
    /**
     * <code>OTP_FAIL = 4;</code>
     */
    OTP_FAIL(4),
    /**
     * <code>NOT_INVITED = 5;</code>
     */
    NOT_INVITED(5),
    /**
     * <code>INVALID_GROUP_INVITE_TOKEN = 6;</code>
     */
    INVALID_GROUP_INVITE_TOKEN(6),
    /**
     * <code>RETRIED_TOO_SOON = 7;</code>
     */
    RETRIED_TOO_SOON(7),
    /**
     * <code>BAD_REQUEST = 8;</code>
     */
    BAD_REQUEST(8),
    /**
     * <code>INTERNAL_SERVER_ERROR = 9;</code>
     */
    INTERNAL_SERVER_ERROR(9),
    UNRECOGNIZED(-1),
    ;

    /**
     * <code>UNKNOWN_REASON = 0;</code>
     */
    public static final int UNKNOWN_REASON_VALUE = 0;
    /**
     * <code>INVALID_PHONE_NUMBER = 1;</code>
     */
    public static final int INVALID_PHONE_NUMBER_VALUE = 1;
    /**
     * <code>INVALID_CLIENT_VERSION = 2;</code>
     */
    public static final int INVALID_CLIENT_VERSION_VALUE = 2;
    /**
     * <code>BAD_METHOD = 3;</code>
     */
    public static final int BAD_METHOD_VALUE = 3;
    /**
     * <code>OTP_FAIL = 4;</code>
     */
    public static final int OTP_FAIL_VALUE = 4;
    /**
     * <code>NOT_INVITED = 5;</code>
     */
    public static final int NOT_INVITED_VALUE = 5;
    /**
     * <code>INVALID_GROUP_INVITE_TOKEN = 6;</code>
     */
    public static final int INVALID_GROUP_INVITE_TOKEN_VALUE = 6;
    /**
     * <code>RETRIED_TOO_SOON = 7;</code>
     */
    public static final int RETRIED_TOO_SOON_VALUE = 7;
    /**
     * <code>BAD_REQUEST = 8;</code>
     */
    public static final int BAD_REQUEST_VALUE = 8;
    /**
     * <code>INTERNAL_SERVER_ERROR = 9;</code>
     */
    public static final int INTERNAL_SERVER_ERROR_VALUE = 9;


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
        case 0: return UNKNOWN_REASON;
        case 1: return INVALID_PHONE_NUMBER;
        case 2: return INVALID_CLIENT_VERSION;
        case 3: return BAD_METHOD;
        case 4: return OTP_FAIL;
        case 5: return NOT_INVITED;
        case 6: return INVALID_GROUP_INVITE_TOKEN;
        case 7: return RETRIED_TOO_SOON;
        case 8: return BAD_REQUEST;
        case 9: return INTERNAL_SERVER_ERROR;
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

    // @@protoc_insertion_point(enum_scope:server.OtpResponse.Reason)
  }

  public static final int PHONE_FIELD_NUMBER = 1;
  private java.lang.String phone_;
  /**
   * <code>string phone = 1;</code>
   * @return The phone.
   */
  @java.lang.Override
  public java.lang.String getPhone() {
    return phone_;
  }
  /**
   * <code>string phone = 1;</code>
   * @return The bytes for phone.
   */
  @java.lang.Override
  public com.google.protobuf.ByteString
      getPhoneBytes() {
    return com.google.protobuf.ByteString.copyFromUtf8(phone_);
  }
  /**
   * <code>string phone = 1;</code>
   * @param value The phone to set.
   */
  private void setPhone(
      java.lang.String value) {
    value.getClass();
  
    phone_ = value;
  }
  /**
   * <code>string phone = 1;</code>
   */
  private void clearPhone() {
    
    phone_ = getDefaultInstance().getPhone();
  }
  /**
   * <code>string phone = 1;</code>
   * @param value The bytes for phone to set.
   */
  private void setPhoneBytes(
      com.google.protobuf.ByteString value) {
    checkByteStringIsUtf8(value);
    phone_ = value.toStringUtf8();
    
  }

  public static final int RESULT_FIELD_NUMBER = 2;
  private int result_;
  /**
   * <code>.server.OtpResponse.Result result = 2;</code>
   * @return The enum numeric value on the wire for result.
   */
  @java.lang.Override
  public int getResultValue() {
    return result_;
  }
  /**
   * <code>.server.OtpResponse.Result result = 2;</code>
   * @return The result.
   */
  @java.lang.Override
  public com.halloapp.proto.server.OtpResponse.Result getResult() {
    com.halloapp.proto.server.OtpResponse.Result result = com.halloapp.proto.server.OtpResponse.Result.forNumber(result_);
    return result == null ? com.halloapp.proto.server.OtpResponse.Result.UNRECOGNIZED : result;
  }
  /**
   * <code>.server.OtpResponse.Result result = 2;</code>
   * @param value The enum numeric value on the wire for result to set.
   */
  private void setResultValue(int value) {
      result_ = value;
  }
  /**
   * <code>.server.OtpResponse.Result result = 2;</code>
   * @param value The result to set.
   */
  private void setResult(com.halloapp.proto.server.OtpResponse.Result value) {
    result_ = value.getNumber();
    
  }
  /**
   * <code>.server.OtpResponse.Result result = 2;</code>
   */
  private void clearResult() {
    
    result_ = 0;
  }

  public static final int REASON_FIELD_NUMBER = 3;
  private int reason_;
  /**
   * <code>.server.OtpResponse.Reason reason = 3;</code>
   * @return The enum numeric value on the wire for reason.
   */
  @java.lang.Override
  public int getReasonValue() {
    return reason_;
  }
  /**
   * <code>.server.OtpResponse.Reason reason = 3;</code>
   * @return The reason.
   */
  @java.lang.Override
  public com.halloapp.proto.server.OtpResponse.Reason getReason() {
    com.halloapp.proto.server.OtpResponse.Reason result = com.halloapp.proto.server.OtpResponse.Reason.forNumber(reason_);
    return result == null ? com.halloapp.proto.server.OtpResponse.Reason.UNRECOGNIZED : result;
  }
  /**
   * <code>.server.OtpResponse.Reason reason = 3;</code>
   * @param value The enum numeric value on the wire for reason to set.
   */
  private void setReasonValue(int value) {
      reason_ = value;
  }
  /**
   * <code>.server.OtpResponse.Reason reason = 3;</code>
   * @param value The reason to set.
   */
  private void setReason(com.halloapp.proto.server.OtpResponse.Reason value) {
    reason_ = value.getNumber();
    
  }
  /**
   * <code>.server.OtpResponse.Reason reason = 3;</code>
   */
  private void clearReason() {
    
    reason_ = 0;
  }

  public static final int RETRY_AFTER_SECS_FIELD_NUMBER = 4;
  private long retryAfterSecs_;
  /**
   * <code>int64 retry_after_secs = 4;</code>
   * @return The retryAfterSecs.
   */
  @java.lang.Override
  public long getRetryAfterSecs() {
    return retryAfterSecs_;
  }
  /**
   * <code>int64 retry_after_secs = 4;</code>
   * @param value The retryAfterSecs to set.
   */
  private void setRetryAfterSecs(long value) {
    
    retryAfterSecs_ = value;
  }
  /**
   * <code>int64 retry_after_secs = 4;</code>
   */
  private void clearRetryAfterSecs() {
    
    retryAfterSecs_ = 0L;
  }

  public static com.halloapp.proto.server.OtpResponse parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static com.halloapp.proto.server.OtpResponse parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static com.halloapp.proto.server.OtpResponse parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static com.halloapp.proto.server.OtpResponse parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static com.halloapp.proto.server.OtpResponse parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static com.halloapp.proto.server.OtpResponse parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static com.halloapp.proto.server.OtpResponse parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input);
  }
  public static com.halloapp.proto.server.OtpResponse parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input, extensionRegistry);
  }
  public static com.halloapp.proto.server.OtpResponse parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return parseDelimitedFrom(DEFAULT_INSTANCE, input);
  }
  public static com.halloapp.proto.server.OtpResponse parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return parseDelimitedFrom(DEFAULT_INSTANCE, input, extensionRegistry);
  }
  public static com.halloapp.proto.server.OtpResponse parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input);
  }
  public static com.halloapp.proto.server.OtpResponse parseFrom(
      com.google.protobuf.CodedInputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input, extensionRegistry);
  }

  public static Builder newBuilder() {
    return (Builder) DEFAULT_INSTANCE.createBuilder();
  }
  public static Builder newBuilder(com.halloapp.proto.server.OtpResponse prototype) {
    return (Builder) DEFAULT_INSTANCE.createBuilder(prototype);
  }

  /**
   * Protobuf type {@code server.OtpResponse}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessageLite.Builder<
        com.halloapp.proto.server.OtpResponse, Builder> implements
      // @@protoc_insertion_point(builder_implements:server.OtpResponse)
      com.halloapp.proto.server.OtpResponseOrBuilder {
    // Construct using com.halloapp.proto.server.OtpResponse.newBuilder()
    private Builder() {
      super(DEFAULT_INSTANCE);
    }


    /**
     * <code>string phone = 1;</code>
     * @return The phone.
     */
    @java.lang.Override
    public java.lang.String getPhone() {
      return instance.getPhone();
    }
    /**
     * <code>string phone = 1;</code>
     * @return The bytes for phone.
     */
    @java.lang.Override
    public com.google.protobuf.ByteString
        getPhoneBytes() {
      return instance.getPhoneBytes();
    }
    /**
     * <code>string phone = 1;</code>
     * @param value The phone to set.
     * @return This builder for chaining.
     */
    public Builder setPhone(
        java.lang.String value) {
      copyOnWrite();
      instance.setPhone(value);
      return this;
    }
    /**
     * <code>string phone = 1;</code>
     * @return This builder for chaining.
     */
    public Builder clearPhone() {
      copyOnWrite();
      instance.clearPhone();
      return this;
    }
    /**
     * <code>string phone = 1;</code>
     * @param value The bytes for phone to set.
     * @return This builder for chaining.
     */
    public Builder setPhoneBytes(
        com.google.protobuf.ByteString value) {
      copyOnWrite();
      instance.setPhoneBytes(value);
      return this;
    }

    /**
     * <code>.server.OtpResponse.Result result = 2;</code>
     * @return The enum numeric value on the wire for result.
     */
    @java.lang.Override
    public int getResultValue() {
      return instance.getResultValue();
    }
    /**
     * <code>.server.OtpResponse.Result result = 2;</code>
     * @param value The result to set.
     * @return This builder for chaining.
     */
    public Builder setResultValue(int value) {
      copyOnWrite();
      instance.setResultValue(value);
      return this;
    }
    /**
     * <code>.server.OtpResponse.Result result = 2;</code>
     * @return The result.
     */
    @java.lang.Override
    public com.halloapp.proto.server.OtpResponse.Result getResult() {
      return instance.getResult();
    }
    /**
     * <code>.server.OtpResponse.Result result = 2;</code>
     * @param value The enum numeric value on the wire for result to set.
     * @return This builder for chaining.
     */
    public Builder setResult(com.halloapp.proto.server.OtpResponse.Result value) {
      copyOnWrite();
      instance.setResult(value);
      return this;
    }
    /**
     * <code>.server.OtpResponse.Result result = 2;</code>
     * @return This builder for chaining.
     */
    public Builder clearResult() {
      copyOnWrite();
      instance.clearResult();
      return this;
    }

    /**
     * <code>.server.OtpResponse.Reason reason = 3;</code>
     * @return The enum numeric value on the wire for reason.
     */
    @java.lang.Override
    public int getReasonValue() {
      return instance.getReasonValue();
    }
    /**
     * <code>.server.OtpResponse.Reason reason = 3;</code>
     * @param value The reason to set.
     * @return This builder for chaining.
     */
    public Builder setReasonValue(int value) {
      copyOnWrite();
      instance.setReasonValue(value);
      return this;
    }
    /**
     * <code>.server.OtpResponse.Reason reason = 3;</code>
     * @return The reason.
     */
    @java.lang.Override
    public com.halloapp.proto.server.OtpResponse.Reason getReason() {
      return instance.getReason();
    }
    /**
     * <code>.server.OtpResponse.Reason reason = 3;</code>
     * @param value The enum numeric value on the wire for reason to set.
     * @return This builder for chaining.
     */
    public Builder setReason(com.halloapp.proto.server.OtpResponse.Reason value) {
      copyOnWrite();
      instance.setReason(value);
      return this;
    }
    /**
     * <code>.server.OtpResponse.Reason reason = 3;</code>
     * @return This builder for chaining.
     */
    public Builder clearReason() {
      copyOnWrite();
      instance.clearReason();
      return this;
    }

    /**
     * <code>int64 retry_after_secs = 4;</code>
     * @return The retryAfterSecs.
     */
    @java.lang.Override
    public long getRetryAfterSecs() {
      return instance.getRetryAfterSecs();
    }
    /**
     * <code>int64 retry_after_secs = 4;</code>
     * @param value The retryAfterSecs to set.
     * @return This builder for chaining.
     */
    public Builder setRetryAfterSecs(long value) {
      copyOnWrite();
      instance.setRetryAfterSecs(value);
      return this;
    }
    /**
     * <code>int64 retry_after_secs = 4;</code>
     * @return This builder for chaining.
     */
    public Builder clearRetryAfterSecs() {
      copyOnWrite();
      instance.clearRetryAfterSecs();
      return this;
    }

    // @@protoc_insertion_point(builder_scope:server.OtpResponse)
  }
  @java.lang.Override
  @java.lang.SuppressWarnings({"unchecked", "fallthrough"})
  protected final java.lang.Object dynamicMethod(
      com.google.protobuf.GeneratedMessageLite.MethodToInvoke method,
      java.lang.Object arg0, java.lang.Object arg1) {
    switch (method) {
      case NEW_MUTABLE_INSTANCE: {
        return new com.halloapp.proto.server.OtpResponse();
      }
      case NEW_BUILDER: {
        return new Builder();
      }
      case BUILD_MESSAGE_INFO: {
          java.lang.Object[] objects = new java.lang.Object[] {
            "phone_",
            "result_",
            "reason_",
            "retryAfterSecs_",
          };
          java.lang.String info =
              "\u0000\u0004\u0000\u0000\u0001\u0004\u0004\u0000\u0000\u0000\u0001\u0208\u0002\f" +
              "\u0003\f\u0004\u0002";
          return newMessageInfo(DEFAULT_INSTANCE, info, objects);
      }
      // fall through
      case GET_DEFAULT_INSTANCE: {
        return DEFAULT_INSTANCE;
      }
      case GET_PARSER: {
        com.google.protobuf.Parser<com.halloapp.proto.server.OtpResponse> parser = PARSER;
        if (parser == null) {
          synchronized (com.halloapp.proto.server.OtpResponse.class) {
            parser = PARSER;
            if (parser == null) {
              parser =
                  new DefaultInstanceBasedParser<com.halloapp.proto.server.OtpResponse>(
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


  // @@protoc_insertion_point(class_scope:server.OtpResponse)
  private static final com.halloapp.proto.server.OtpResponse DEFAULT_INSTANCE;
  static {
    OtpResponse defaultInstance = new OtpResponse();
    // New instances are implicitly immutable so no need to make
    // immutable.
    DEFAULT_INSTANCE = defaultInstance;
    com.google.protobuf.GeneratedMessageLite.registerDefaultInstance(
      OtpResponse.class, defaultInstance);
  }

  public static com.halloapp.proto.server.OtpResponse getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static volatile com.google.protobuf.Parser<OtpResponse> PARSER;

  public static com.google.protobuf.Parser<OtpResponse> parser() {
    return DEFAULT_INSTANCE.getParserForType();
  }
}
