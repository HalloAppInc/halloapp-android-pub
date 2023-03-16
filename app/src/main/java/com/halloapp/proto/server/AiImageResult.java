// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: server.proto

package com.halloapp.proto.server;

/**
 * Protobuf type {@code server.AiImageResult}
 */
public  final class AiImageResult extends
    com.google.protobuf.GeneratedMessageLite<
        AiImageResult, AiImageResult.Builder> implements
    // @@protoc_insertion_point(message_implements:server.AiImageResult)
    AiImageResultOrBuilder {
  private AiImageResult() {
    id_ = "";
  }
  /**
   * Protobuf enum {@code server.AiImageResult.Result}
   */
  public enum Result
      implements com.google.protobuf.Internal.EnumLite {
    /**
     * <code>PENDING = 0;</code>
     */
    PENDING(0),
    /**
     * <code>FAIL = 1;</code>
     */
    FAIL(1),
    UNRECOGNIZED(-1),
    ;

    /**
     * <code>PENDING = 0;</code>
     */
    public static final int PENDING_VALUE = 0;
    /**
     * <code>FAIL = 1;</code>
     */
    public static final int FAIL_VALUE = 1;


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
        case 0: return PENDING;
        case 1: return FAIL;
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

    // @@protoc_insertion_point(enum_scope:server.AiImageResult.Result)
  }

  /**
   * Protobuf enum {@code server.AiImageResult.Reason}
   */
  public enum Reason
      implements com.google.protobuf.Internal.EnumLite {
    /**
     * <code>UNKNOWN = 0;</code>
     */
    UNKNOWN(0),
    /**
     * <code>TOO_SOON = 1;</code>
     */
    TOO_SOON(1),
    /**
     * <code>TOO_MANY_ATTEMPTS = 2;</code>
     */
    TOO_MANY_ATTEMPTS(2),
    UNRECOGNIZED(-1),
    ;

    /**
     * <code>UNKNOWN = 0;</code>
     */
    public static final int UNKNOWN_VALUE = 0;
    /**
     * <code>TOO_SOON = 1;</code>
     */
    public static final int TOO_SOON_VALUE = 1;
    /**
     * <code>TOO_MANY_ATTEMPTS = 2;</code>
     */
    public static final int TOO_MANY_ATTEMPTS_VALUE = 2;


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
        case 1: return TOO_SOON;
        case 2: return TOO_MANY_ATTEMPTS;
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

    // @@protoc_insertion_point(enum_scope:server.AiImageResult.Reason)
  }

  public static final int RESULT_FIELD_NUMBER = 1;
  private int result_;
  /**
   * <code>.server.AiImageResult.Result result = 1;</code>
   * @return The enum numeric value on the wire for result.
   */
  @java.lang.Override
  public int getResultValue() {
    return result_;
  }
  /**
   * <code>.server.AiImageResult.Result result = 1;</code>
   * @return The result.
   */
  @java.lang.Override
  public com.halloapp.proto.server.AiImageResult.Result getResult() {
    com.halloapp.proto.server.AiImageResult.Result result = com.halloapp.proto.server.AiImageResult.Result.forNumber(result_);
    return result == null ? com.halloapp.proto.server.AiImageResult.Result.UNRECOGNIZED : result;
  }
  /**
   * <code>.server.AiImageResult.Result result = 1;</code>
   * @param value The enum numeric value on the wire for result to set.
   */
  private void setResultValue(int value) {
      result_ = value;
  }
  /**
   * <code>.server.AiImageResult.Result result = 1;</code>
   * @param value The result to set.
   */
  private void setResult(com.halloapp.proto.server.AiImageResult.Result value) {
    result_ = value.getNumber();
    
  }
  /**
   * <code>.server.AiImageResult.Result result = 1;</code>
   */
  private void clearResult() {
    
    result_ = 0;
  }

  public static final int REASON_FIELD_NUMBER = 2;
  private int reason_;
  /**
   * <code>.server.AiImageResult.Reason reason = 2;</code>
   * @return The enum numeric value on the wire for reason.
   */
  @java.lang.Override
  public int getReasonValue() {
    return reason_;
  }
  /**
   * <code>.server.AiImageResult.Reason reason = 2;</code>
   * @return The reason.
   */
  @java.lang.Override
  public com.halloapp.proto.server.AiImageResult.Reason getReason() {
    com.halloapp.proto.server.AiImageResult.Reason result = com.halloapp.proto.server.AiImageResult.Reason.forNumber(reason_);
    return result == null ? com.halloapp.proto.server.AiImageResult.Reason.UNRECOGNIZED : result;
  }
  /**
   * <code>.server.AiImageResult.Reason reason = 2;</code>
   * @param value The enum numeric value on the wire for reason to set.
   */
  private void setReasonValue(int value) {
      reason_ = value;
  }
  /**
   * <code>.server.AiImageResult.Reason reason = 2;</code>
   * @param value The reason to set.
   */
  private void setReason(com.halloapp.proto.server.AiImageResult.Reason value) {
    reason_ = value.getNumber();
    
  }
  /**
   * <code>.server.AiImageResult.Reason reason = 2;</code>
   */
  private void clearReason() {
    
    reason_ = 0;
  }

  public static final int ID_FIELD_NUMBER = 3;
  private java.lang.String id_;
  /**
   * <code>string id = 3;</code>
   * @return The id.
   */
  @java.lang.Override
  public java.lang.String getId() {
    return id_;
  }
  /**
   * <code>string id = 3;</code>
   * @return The bytes for id.
   */
  @java.lang.Override
  public com.google.protobuf.ByteString
      getIdBytes() {
    return com.google.protobuf.ByteString.copyFromUtf8(id_);
  }
  /**
   * <code>string id = 3;</code>
   * @param value The id to set.
   */
  private void setId(
      java.lang.String value) {
    value.getClass();
  
    id_ = value;
  }
  /**
   * <code>string id = 3;</code>
   */
  private void clearId() {
    
    id_ = getDefaultInstance().getId();
  }
  /**
   * <code>string id = 3;</code>
   * @param value The bytes for id to set.
   */
  private void setIdBytes(
      com.google.protobuf.ByteString value) {
    checkByteStringIsUtf8(value);
    id_ = value.toStringUtf8();
    
  }

  public static final int BACKOFF_FIELD_NUMBER = 4;
  private long backoff_;
  /**
   * <code>int64 backoff = 4;</code>
   * @return The backoff.
   */
  @java.lang.Override
  public long getBackoff() {
    return backoff_;
  }
  /**
   * <code>int64 backoff = 4;</code>
   * @param value The backoff to set.
   */
  private void setBackoff(long value) {
    
    backoff_ = value;
  }
  /**
   * <code>int64 backoff = 4;</code>
   */
  private void clearBackoff() {
    
    backoff_ = 0L;
  }

  public static com.halloapp.proto.server.AiImageResult parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static com.halloapp.proto.server.AiImageResult parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static com.halloapp.proto.server.AiImageResult parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static com.halloapp.proto.server.AiImageResult parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static com.halloapp.proto.server.AiImageResult parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static com.halloapp.proto.server.AiImageResult parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static com.halloapp.proto.server.AiImageResult parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input);
  }
  public static com.halloapp.proto.server.AiImageResult parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input, extensionRegistry);
  }
  public static com.halloapp.proto.server.AiImageResult parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return parseDelimitedFrom(DEFAULT_INSTANCE, input);
  }
  public static com.halloapp.proto.server.AiImageResult parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return parseDelimitedFrom(DEFAULT_INSTANCE, input, extensionRegistry);
  }
  public static com.halloapp.proto.server.AiImageResult parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input);
  }
  public static com.halloapp.proto.server.AiImageResult parseFrom(
      com.google.protobuf.CodedInputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input, extensionRegistry);
  }

  public static Builder newBuilder() {
    return (Builder) DEFAULT_INSTANCE.createBuilder();
  }
  public static Builder newBuilder(com.halloapp.proto.server.AiImageResult prototype) {
    return (Builder) DEFAULT_INSTANCE.createBuilder(prototype);
  }

  /**
   * Protobuf type {@code server.AiImageResult}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessageLite.Builder<
        com.halloapp.proto.server.AiImageResult, Builder> implements
      // @@protoc_insertion_point(builder_implements:server.AiImageResult)
      com.halloapp.proto.server.AiImageResultOrBuilder {
    // Construct using com.halloapp.proto.server.AiImageResult.newBuilder()
    private Builder() {
      super(DEFAULT_INSTANCE);
    }


    /**
     * <code>.server.AiImageResult.Result result = 1;</code>
     * @return The enum numeric value on the wire for result.
     */
    @java.lang.Override
    public int getResultValue() {
      return instance.getResultValue();
    }
    /**
     * <code>.server.AiImageResult.Result result = 1;</code>
     * @param value The result to set.
     * @return This builder for chaining.
     */
    public Builder setResultValue(int value) {
      copyOnWrite();
      instance.setResultValue(value);
      return this;
    }
    /**
     * <code>.server.AiImageResult.Result result = 1;</code>
     * @return The result.
     */
    @java.lang.Override
    public com.halloapp.proto.server.AiImageResult.Result getResult() {
      return instance.getResult();
    }
    /**
     * <code>.server.AiImageResult.Result result = 1;</code>
     * @param value The enum numeric value on the wire for result to set.
     * @return This builder for chaining.
     */
    public Builder setResult(com.halloapp.proto.server.AiImageResult.Result value) {
      copyOnWrite();
      instance.setResult(value);
      return this;
    }
    /**
     * <code>.server.AiImageResult.Result result = 1;</code>
     * @return This builder for chaining.
     */
    public Builder clearResult() {
      copyOnWrite();
      instance.clearResult();
      return this;
    }

    /**
     * <code>.server.AiImageResult.Reason reason = 2;</code>
     * @return The enum numeric value on the wire for reason.
     */
    @java.lang.Override
    public int getReasonValue() {
      return instance.getReasonValue();
    }
    /**
     * <code>.server.AiImageResult.Reason reason = 2;</code>
     * @param value The reason to set.
     * @return This builder for chaining.
     */
    public Builder setReasonValue(int value) {
      copyOnWrite();
      instance.setReasonValue(value);
      return this;
    }
    /**
     * <code>.server.AiImageResult.Reason reason = 2;</code>
     * @return The reason.
     */
    @java.lang.Override
    public com.halloapp.proto.server.AiImageResult.Reason getReason() {
      return instance.getReason();
    }
    /**
     * <code>.server.AiImageResult.Reason reason = 2;</code>
     * @param value The enum numeric value on the wire for reason to set.
     * @return This builder for chaining.
     */
    public Builder setReason(com.halloapp.proto.server.AiImageResult.Reason value) {
      copyOnWrite();
      instance.setReason(value);
      return this;
    }
    /**
     * <code>.server.AiImageResult.Reason reason = 2;</code>
     * @return This builder for chaining.
     */
    public Builder clearReason() {
      copyOnWrite();
      instance.clearReason();
      return this;
    }

    /**
     * <code>string id = 3;</code>
     * @return The id.
     */
    @java.lang.Override
    public java.lang.String getId() {
      return instance.getId();
    }
    /**
     * <code>string id = 3;</code>
     * @return The bytes for id.
     */
    @java.lang.Override
    public com.google.protobuf.ByteString
        getIdBytes() {
      return instance.getIdBytes();
    }
    /**
     * <code>string id = 3;</code>
     * @param value The id to set.
     * @return This builder for chaining.
     */
    public Builder setId(
        java.lang.String value) {
      copyOnWrite();
      instance.setId(value);
      return this;
    }
    /**
     * <code>string id = 3;</code>
     * @return This builder for chaining.
     */
    public Builder clearId() {
      copyOnWrite();
      instance.clearId();
      return this;
    }
    /**
     * <code>string id = 3;</code>
     * @param value The bytes for id to set.
     * @return This builder for chaining.
     */
    public Builder setIdBytes(
        com.google.protobuf.ByteString value) {
      copyOnWrite();
      instance.setIdBytes(value);
      return this;
    }

    /**
     * <code>int64 backoff = 4;</code>
     * @return The backoff.
     */
    @java.lang.Override
    public long getBackoff() {
      return instance.getBackoff();
    }
    /**
     * <code>int64 backoff = 4;</code>
     * @param value The backoff to set.
     * @return This builder for chaining.
     */
    public Builder setBackoff(long value) {
      copyOnWrite();
      instance.setBackoff(value);
      return this;
    }
    /**
     * <code>int64 backoff = 4;</code>
     * @return This builder for chaining.
     */
    public Builder clearBackoff() {
      copyOnWrite();
      instance.clearBackoff();
      return this;
    }

    // @@protoc_insertion_point(builder_scope:server.AiImageResult)
  }
  @java.lang.Override
  @java.lang.SuppressWarnings({"unchecked", "fallthrough"})
  protected final java.lang.Object dynamicMethod(
      com.google.protobuf.GeneratedMessageLite.MethodToInvoke method,
      java.lang.Object arg0, java.lang.Object arg1) {
    switch (method) {
      case NEW_MUTABLE_INSTANCE: {
        return new com.halloapp.proto.server.AiImageResult();
      }
      case NEW_BUILDER: {
        return new Builder();
      }
      case BUILD_MESSAGE_INFO: {
          java.lang.Object[] objects = new java.lang.Object[] {
            "result_",
            "reason_",
            "id_",
            "backoff_",
          };
          java.lang.String info =
              "\u0000\u0004\u0000\u0000\u0001\u0004\u0004\u0000\u0000\u0000\u0001\f\u0002\f\u0003" +
              "\u0208\u0004\u0002";
          return newMessageInfo(DEFAULT_INSTANCE, info, objects);
      }
      // fall through
      case GET_DEFAULT_INSTANCE: {
        return DEFAULT_INSTANCE;
      }
      case GET_PARSER: {
        com.google.protobuf.Parser<com.halloapp.proto.server.AiImageResult> parser = PARSER;
        if (parser == null) {
          synchronized (com.halloapp.proto.server.AiImageResult.class) {
            parser = PARSER;
            if (parser == null) {
              parser =
                  new DefaultInstanceBasedParser<com.halloapp.proto.server.AiImageResult>(
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


  // @@protoc_insertion_point(class_scope:server.AiImageResult)
  private static final com.halloapp.proto.server.AiImageResult DEFAULT_INSTANCE;
  static {
    AiImageResult defaultInstance = new AiImageResult();
    // New instances are implicitly immutable so no need to make
    // immutable.
    DEFAULT_INSTANCE = defaultInstance;
    com.google.protobuf.GeneratedMessageLite.registerDefaultInstance(
      AiImageResult.class, defaultInstance);
  }

  public static com.halloapp.proto.server.AiImageResult getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static volatile com.google.protobuf.Parser<AiImageResult> PARSER;

  public static com.google.protobuf.Parser<AiImageResult> parser() {
    return DEFAULT_INSTANCE.getParserForType();
  }
}

