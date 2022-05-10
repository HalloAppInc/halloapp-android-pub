// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: server.proto

package com.halloapp.proto.server;

/**
 * Protobuf type {@code server.WebClientInfo}
 */
public  final class WebClientInfo extends
    com.google.protobuf.GeneratedMessageLite<
        WebClientInfo, WebClientInfo.Builder> implements
    // @@protoc_insertion_point(message_implements:server.WebClientInfo)
    WebClientInfoOrBuilder {
  private WebClientInfo() {
    staticKey_ = com.google.protobuf.ByteString.EMPTY;
  }
  /**
   * Protobuf enum {@code server.WebClientInfo.Action}
   */
  public enum Action
      implements com.google.protobuf.Internal.EnumLite {
    /**
     * <code>UNKNOWN_ACTION = 0;</code>
     */
    UNKNOWN_ACTION(0),
    /**
     * <pre>
     * Used by web client
     * </pre>
     *
     * <code>ADD_KEY = 1;</code>
     */
    ADD_KEY(1),
    /**
     * <pre>
     * Used by web client
     * </pre>
     *
     * <code>IS_KEY_AUTHENTICATED = 2;</code>
     */
    IS_KEY_AUTHENTICATED(2),
    /**
     * <pre>
     * Used by mobile client
     * </pre>
     *
     * <code>AUTHENTICATE_KEY = 3;</code>
     */
    AUTHENTICATE_KEY(3),
    /**
     * <pre>
     * Used by web client and mobile client
     * </pre>
     *
     * <code>REMOVE_KEY = 4;</code>
     */
    REMOVE_KEY(4),
    UNRECOGNIZED(-1),
    ;

    /**
     * <code>UNKNOWN_ACTION = 0;</code>
     */
    public static final int UNKNOWN_ACTION_VALUE = 0;
    /**
     * <pre>
     * Used by web client
     * </pre>
     *
     * <code>ADD_KEY = 1;</code>
     */
    public static final int ADD_KEY_VALUE = 1;
    /**
     * <pre>
     * Used by web client
     * </pre>
     *
     * <code>IS_KEY_AUTHENTICATED = 2;</code>
     */
    public static final int IS_KEY_AUTHENTICATED_VALUE = 2;
    /**
     * <pre>
     * Used by mobile client
     * </pre>
     *
     * <code>AUTHENTICATE_KEY = 3;</code>
     */
    public static final int AUTHENTICATE_KEY_VALUE = 3;
    /**
     * <pre>
     * Used by web client and mobile client
     * </pre>
     *
     * <code>REMOVE_KEY = 4;</code>
     */
    public static final int REMOVE_KEY_VALUE = 4;


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
    public static Action valueOf(int value) {
      return forNumber(value);
    }

    public static Action forNumber(int value) {
      switch (value) {
        case 0: return UNKNOWN_ACTION;
        case 1: return ADD_KEY;
        case 2: return IS_KEY_AUTHENTICATED;
        case 3: return AUTHENTICATE_KEY;
        case 4: return REMOVE_KEY;
        default: return null;
      }
    }

    public static com.google.protobuf.Internal.EnumLiteMap<Action>
        internalGetValueMap() {
      return internalValueMap;
    }
    private static final com.google.protobuf.Internal.EnumLiteMap<
        Action> internalValueMap =
          new com.google.protobuf.Internal.EnumLiteMap<Action>() {
            @java.lang.Override
            public Action findValueByNumber(int number) {
              return Action.forNumber(number);
            }
          };

    public static com.google.protobuf.Internal.EnumVerifier 
        internalGetVerifier() {
      return ActionVerifier.INSTANCE;
    }

    private static final class ActionVerifier implements 
         com.google.protobuf.Internal.EnumVerifier { 
            static final com.google.protobuf.Internal.EnumVerifier           INSTANCE = new ActionVerifier();
            @java.lang.Override
            public boolean isInRange(int number) {
              return Action.forNumber(number) != null;
            }
          };

    private final int value;

    private Action(int value) {
      this.value = value;
    }

    // @@protoc_insertion_point(enum_scope:server.WebClientInfo.Action)
  }

  /**
   * Protobuf enum {@code server.WebClientInfo.Result}
   */
  public enum Result
      implements com.google.protobuf.Internal.EnumLite {
    /**
     * <code>UNKNOWN = 0;</code>
     */
    UNKNOWN(0),
    /**
     * <code>OK = 1;</code>
     */
    OK(1),
    /**
     * <code>AUTHENTICATED = 2;</code>
     */
    AUTHENTICATED(2),
    /**
     * <code>NOT_AUTHENTICATED = 3;</code>
     */
    NOT_AUTHENTICATED(3),
    UNRECOGNIZED(-1),
    ;

    /**
     * <code>UNKNOWN = 0;</code>
     */
    public static final int UNKNOWN_VALUE = 0;
    /**
     * <code>OK = 1;</code>
     */
    public static final int OK_VALUE = 1;
    /**
     * <code>AUTHENTICATED = 2;</code>
     */
    public static final int AUTHENTICATED_VALUE = 2;
    /**
     * <code>NOT_AUTHENTICATED = 3;</code>
     */
    public static final int NOT_AUTHENTICATED_VALUE = 3;


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
        case 0: return UNKNOWN;
        case 1: return OK;
        case 2: return AUTHENTICATED;
        case 3: return NOT_AUTHENTICATED;
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

    // @@protoc_insertion_point(enum_scope:server.WebClientInfo.Result)
  }

  public static final int ACTION_FIELD_NUMBER = 1;
  private int action_;
  /**
   * <code>.server.WebClientInfo.Action action = 1;</code>
   * @return The enum numeric value on the wire for action.
   */
  @java.lang.Override
  public int getActionValue() {
    return action_;
  }
  /**
   * <code>.server.WebClientInfo.Action action = 1;</code>
   * @return The action.
   */
  @java.lang.Override
  public com.halloapp.proto.server.WebClientInfo.Action getAction() {
    com.halloapp.proto.server.WebClientInfo.Action result = com.halloapp.proto.server.WebClientInfo.Action.forNumber(action_);
    return result == null ? com.halloapp.proto.server.WebClientInfo.Action.UNRECOGNIZED : result;
  }
  /**
   * <code>.server.WebClientInfo.Action action = 1;</code>
   * @param value The enum numeric value on the wire for action to set.
   */
  private void setActionValue(int value) {
      action_ = value;
  }
  /**
   * <code>.server.WebClientInfo.Action action = 1;</code>
   * @param value The action to set.
   */
  private void setAction(com.halloapp.proto.server.WebClientInfo.Action value) {
    action_ = value.getNumber();
    
  }
  /**
   * <code>.server.WebClientInfo.Action action = 1;</code>
   */
  private void clearAction() {
    
    action_ = 0;
  }

  public static final int STATIC_KEY_FIELD_NUMBER = 2;
  private com.google.protobuf.ByteString staticKey_;
  /**
   * <code>bytes static_key = 2;</code>
   * @return The staticKey.
   */
  @java.lang.Override
  public com.google.protobuf.ByteString getStaticKey() {
    return staticKey_;
  }
  /**
   * <code>bytes static_key = 2;</code>
   * @param value The staticKey to set.
   */
  private void setStaticKey(com.google.protobuf.ByteString value) {
    value.getClass();
  
    staticKey_ = value;
  }
  /**
   * <code>bytes static_key = 2;</code>
   */
  private void clearStaticKey() {
    
    staticKey_ = getDefaultInstance().getStaticKey();
  }

  public static final int RESULT_FIELD_NUMBER = 3;
  private int result_;
  /**
   * <code>.server.WebClientInfo.Result result = 3;</code>
   * @return The enum numeric value on the wire for result.
   */
  @java.lang.Override
  public int getResultValue() {
    return result_;
  }
  /**
   * <code>.server.WebClientInfo.Result result = 3;</code>
   * @return The result.
   */
  @java.lang.Override
  public com.halloapp.proto.server.WebClientInfo.Result getResult() {
    com.halloapp.proto.server.WebClientInfo.Result result = com.halloapp.proto.server.WebClientInfo.Result.forNumber(result_);
    return result == null ? com.halloapp.proto.server.WebClientInfo.Result.UNRECOGNIZED : result;
  }
  /**
   * <code>.server.WebClientInfo.Result result = 3;</code>
   * @param value The enum numeric value on the wire for result to set.
   */
  private void setResultValue(int value) {
      result_ = value;
  }
  /**
   * <code>.server.WebClientInfo.Result result = 3;</code>
   * @param value The result to set.
   */
  private void setResult(com.halloapp.proto.server.WebClientInfo.Result value) {
    result_ = value.getNumber();
    
  }
  /**
   * <code>.server.WebClientInfo.Result result = 3;</code>
   */
  private void clearResult() {
    
    result_ = 0;
  }

  public static com.halloapp.proto.server.WebClientInfo parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static com.halloapp.proto.server.WebClientInfo parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static com.halloapp.proto.server.WebClientInfo parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static com.halloapp.proto.server.WebClientInfo parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static com.halloapp.proto.server.WebClientInfo parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static com.halloapp.proto.server.WebClientInfo parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static com.halloapp.proto.server.WebClientInfo parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input);
  }
  public static com.halloapp.proto.server.WebClientInfo parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input, extensionRegistry);
  }
  public static com.halloapp.proto.server.WebClientInfo parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return parseDelimitedFrom(DEFAULT_INSTANCE, input);
  }
  public static com.halloapp.proto.server.WebClientInfo parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return parseDelimitedFrom(DEFAULT_INSTANCE, input, extensionRegistry);
  }
  public static com.halloapp.proto.server.WebClientInfo parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input);
  }
  public static com.halloapp.proto.server.WebClientInfo parseFrom(
      com.google.protobuf.CodedInputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input, extensionRegistry);
  }

  public static Builder newBuilder() {
    return (Builder) DEFAULT_INSTANCE.createBuilder();
  }
  public static Builder newBuilder(com.halloapp.proto.server.WebClientInfo prototype) {
    return (Builder) DEFAULT_INSTANCE.createBuilder(prototype);
  }

  /**
   * Protobuf type {@code server.WebClientInfo}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessageLite.Builder<
        com.halloapp.proto.server.WebClientInfo, Builder> implements
      // @@protoc_insertion_point(builder_implements:server.WebClientInfo)
      com.halloapp.proto.server.WebClientInfoOrBuilder {
    // Construct using com.halloapp.proto.server.WebClientInfo.newBuilder()
    private Builder() {
      super(DEFAULT_INSTANCE);
    }


    /**
     * <code>.server.WebClientInfo.Action action = 1;</code>
     * @return The enum numeric value on the wire for action.
     */
    @java.lang.Override
    public int getActionValue() {
      return instance.getActionValue();
    }
    /**
     * <code>.server.WebClientInfo.Action action = 1;</code>
     * @param value The action to set.
     * @return This builder for chaining.
     */
    public Builder setActionValue(int value) {
      copyOnWrite();
      instance.setActionValue(value);
      return this;
    }
    /**
     * <code>.server.WebClientInfo.Action action = 1;</code>
     * @return The action.
     */
    @java.lang.Override
    public com.halloapp.proto.server.WebClientInfo.Action getAction() {
      return instance.getAction();
    }
    /**
     * <code>.server.WebClientInfo.Action action = 1;</code>
     * @param value The enum numeric value on the wire for action to set.
     * @return This builder for chaining.
     */
    public Builder setAction(com.halloapp.proto.server.WebClientInfo.Action value) {
      copyOnWrite();
      instance.setAction(value);
      return this;
    }
    /**
     * <code>.server.WebClientInfo.Action action = 1;</code>
     * @return This builder for chaining.
     */
    public Builder clearAction() {
      copyOnWrite();
      instance.clearAction();
      return this;
    }

    /**
     * <code>bytes static_key = 2;</code>
     * @return The staticKey.
     */
    @java.lang.Override
    public com.google.protobuf.ByteString getStaticKey() {
      return instance.getStaticKey();
    }
    /**
     * <code>bytes static_key = 2;</code>
     * @param value The staticKey to set.
     * @return This builder for chaining.
     */
    public Builder setStaticKey(com.google.protobuf.ByteString value) {
      copyOnWrite();
      instance.setStaticKey(value);
      return this;
    }
    /**
     * <code>bytes static_key = 2;</code>
     * @return This builder for chaining.
     */
    public Builder clearStaticKey() {
      copyOnWrite();
      instance.clearStaticKey();
      return this;
    }

    /**
     * <code>.server.WebClientInfo.Result result = 3;</code>
     * @return The enum numeric value on the wire for result.
     */
    @java.lang.Override
    public int getResultValue() {
      return instance.getResultValue();
    }
    /**
     * <code>.server.WebClientInfo.Result result = 3;</code>
     * @param value The result to set.
     * @return This builder for chaining.
     */
    public Builder setResultValue(int value) {
      copyOnWrite();
      instance.setResultValue(value);
      return this;
    }
    /**
     * <code>.server.WebClientInfo.Result result = 3;</code>
     * @return The result.
     */
    @java.lang.Override
    public com.halloapp.proto.server.WebClientInfo.Result getResult() {
      return instance.getResult();
    }
    /**
     * <code>.server.WebClientInfo.Result result = 3;</code>
     * @param value The enum numeric value on the wire for result to set.
     * @return This builder for chaining.
     */
    public Builder setResult(com.halloapp.proto.server.WebClientInfo.Result value) {
      copyOnWrite();
      instance.setResult(value);
      return this;
    }
    /**
     * <code>.server.WebClientInfo.Result result = 3;</code>
     * @return This builder for chaining.
     */
    public Builder clearResult() {
      copyOnWrite();
      instance.clearResult();
      return this;
    }

    // @@protoc_insertion_point(builder_scope:server.WebClientInfo)
  }
  @java.lang.Override
  @java.lang.SuppressWarnings({"unchecked", "fallthrough"})
  protected final java.lang.Object dynamicMethod(
      com.google.protobuf.GeneratedMessageLite.MethodToInvoke method,
      java.lang.Object arg0, java.lang.Object arg1) {
    switch (method) {
      case NEW_MUTABLE_INSTANCE: {
        return new com.halloapp.proto.server.WebClientInfo();
      }
      case NEW_BUILDER: {
        return new Builder();
      }
      case BUILD_MESSAGE_INFO: {
          java.lang.Object[] objects = new java.lang.Object[] {
            "action_",
            "staticKey_",
            "result_",
          };
          java.lang.String info =
              "\u0000\u0003\u0000\u0000\u0001\u0003\u0003\u0000\u0000\u0000\u0001\f\u0002\n\u0003" +
              "\f";
          return newMessageInfo(DEFAULT_INSTANCE, info, objects);
      }
      // fall through
      case GET_DEFAULT_INSTANCE: {
        return DEFAULT_INSTANCE;
      }
      case GET_PARSER: {
        com.google.protobuf.Parser<com.halloapp.proto.server.WebClientInfo> parser = PARSER;
        if (parser == null) {
          synchronized (com.halloapp.proto.server.WebClientInfo.class) {
            parser = PARSER;
            if (parser == null) {
              parser =
                  new DefaultInstanceBasedParser<com.halloapp.proto.server.WebClientInfo>(
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


  // @@protoc_insertion_point(class_scope:server.WebClientInfo)
  private static final com.halloapp.proto.server.WebClientInfo DEFAULT_INSTANCE;
  static {
    WebClientInfo defaultInstance = new WebClientInfo();
    // New instances are implicitly immutable so no need to make
    // immutable.
    DEFAULT_INSTANCE = defaultInstance;
    com.google.protobuf.GeneratedMessageLite.registerDefaultInstance(
      WebClientInfo.class, defaultInstance);
  }

  public static com.halloapp.proto.server.WebClientInfo getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static volatile com.google.protobuf.Parser<WebClientInfo> PARSER;

  public static com.google.protobuf.Parser<WebClientInfo> parser() {
    return DEFAULT_INSTANCE.getParserForType();
  }
}

