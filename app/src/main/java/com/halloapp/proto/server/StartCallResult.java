// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: server.proto

package com.halloapp.proto.server;

/**
 * Protobuf type {@code server.StartCallResult}
 */
public  final class StartCallResult extends
    com.google.protobuf.GeneratedMessageLite<
        StartCallResult, StartCallResult.Builder> implements
    // @@protoc_insertion_point(message_implements:server.StartCallResult)
    StartCallResultOrBuilder {
  private StartCallResult() {
    stunServers_ = emptyProtobufList();
    turnServers_ = emptyProtobufList();
  }
  /**
   * Protobuf enum {@code server.StartCallResult.Result}
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
     * <pre>
     * TODO:(nikola) we will likely have to add some sort of reasons here
     * </pre>
     *
     * <code>FAIL = 2;</code>
     */
    FAIL(2),
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
     * <pre>
     * TODO:(nikola) we will likely have to add some sort of reasons here
     * </pre>
     *
     * <code>FAIL = 2;</code>
     */
    public static final int FAIL_VALUE = 2;


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
        case 2: return FAIL;
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

    // @@protoc_insertion_point(enum_scope:server.StartCallResult.Result)
  }

  public static final int RESULT_FIELD_NUMBER = 1;
  private int result_;
  /**
   * <code>.server.StartCallResult.Result result = 1;</code>
   * @return The enum numeric value on the wire for result.
   */
  @java.lang.Override
  public int getResultValue() {
    return result_;
  }
  /**
   * <code>.server.StartCallResult.Result result = 1;</code>
   * @return The result.
   */
  @java.lang.Override
  public com.halloapp.proto.server.StartCallResult.Result getResult() {
    com.halloapp.proto.server.StartCallResult.Result result = com.halloapp.proto.server.StartCallResult.Result.forNumber(result_);
    return result == null ? com.halloapp.proto.server.StartCallResult.Result.UNRECOGNIZED : result;
  }
  /**
   * <code>.server.StartCallResult.Result result = 1;</code>
   * @param value The enum numeric value on the wire for result to set.
   */
  private void setResultValue(int value) {
      result_ = value;
  }
  /**
   * <code>.server.StartCallResult.Result result = 1;</code>
   * @param value The result to set.
   */
  private void setResult(com.halloapp.proto.server.StartCallResult.Result value) {
    result_ = value.getNumber();
    
  }
  /**
   * <code>.server.StartCallResult.Result result = 1;</code>
   */
  private void clearResult() {
    
    result_ = 0;
  }

  public static final int STUN_SERVERS_FIELD_NUMBER = 2;
  private com.google.protobuf.Internal.ProtobufList<com.halloapp.proto.server.StunServer> stunServers_;
  /**
   * <code>repeated .server.StunServer stun_servers = 2;</code>
   */
  @java.lang.Override
  public java.util.List<com.halloapp.proto.server.StunServer> getStunServersList() {
    return stunServers_;
  }
  /**
   * <code>repeated .server.StunServer stun_servers = 2;</code>
   */
  public java.util.List<? extends com.halloapp.proto.server.StunServerOrBuilder> 
      getStunServersOrBuilderList() {
    return stunServers_;
  }
  /**
   * <code>repeated .server.StunServer stun_servers = 2;</code>
   */
  @java.lang.Override
  public int getStunServersCount() {
    return stunServers_.size();
  }
  /**
   * <code>repeated .server.StunServer stun_servers = 2;</code>
   */
  @java.lang.Override
  public com.halloapp.proto.server.StunServer getStunServers(int index) {
    return stunServers_.get(index);
  }
  /**
   * <code>repeated .server.StunServer stun_servers = 2;</code>
   */
  public com.halloapp.proto.server.StunServerOrBuilder getStunServersOrBuilder(
      int index) {
    return stunServers_.get(index);
  }
  private void ensureStunServersIsMutable() {
    com.google.protobuf.Internal.ProtobufList<com.halloapp.proto.server.StunServer> tmp = stunServers_;
    if (!tmp.isModifiable()) {
      stunServers_ =
          com.google.protobuf.GeneratedMessageLite.mutableCopy(tmp);
     }
  }

  /**
   * <code>repeated .server.StunServer stun_servers = 2;</code>
   */
  private void setStunServers(
      int index, com.halloapp.proto.server.StunServer value) {
    value.getClass();
  ensureStunServersIsMutable();
    stunServers_.set(index, value);
  }
  /**
   * <code>repeated .server.StunServer stun_servers = 2;</code>
   */
  private void addStunServers(com.halloapp.proto.server.StunServer value) {
    value.getClass();
  ensureStunServersIsMutable();
    stunServers_.add(value);
  }
  /**
   * <code>repeated .server.StunServer stun_servers = 2;</code>
   */
  private void addStunServers(
      int index, com.halloapp.proto.server.StunServer value) {
    value.getClass();
  ensureStunServersIsMutable();
    stunServers_.add(index, value);
  }
  /**
   * <code>repeated .server.StunServer stun_servers = 2;</code>
   */
  private void addAllStunServers(
      java.lang.Iterable<? extends com.halloapp.proto.server.StunServer> values) {
    ensureStunServersIsMutable();
    com.google.protobuf.AbstractMessageLite.addAll(
        values, stunServers_);
  }
  /**
   * <code>repeated .server.StunServer stun_servers = 2;</code>
   */
  private void clearStunServers() {
    stunServers_ = emptyProtobufList();
  }
  /**
   * <code>repeated .server.StunServer stun_servers = 2;</code>
   */
  private void removeStunServers(int index) {
    ensureStunServersIsMutable();
    stunServers_.remove(index);
  }

  public static final int TURN_SERVERS_FIELD_NUMBER = 3;
  private com.google.protobuf.Internal.ProtobufList<com.halloapp.proto.server.TurnServer> turnServers_;
  /**
   * <code>repeated .server.TurnServer turn_servers = 3;</code>
   */
  @java.lang.Override
  public java.util.List<com.halloapp.proto.server.TurnServer> getTurnServersList() {
    return turnServers_;
  }
  /**
   * <code>repeated .server.TurnServer turn_servers = 3;</code>
   */
  public java.util.List<? extends com.halloapp.proto.server.TurnServerOrBuilder> 
      getTurnServersOrBuilderList() {
    return turnServers_;
  }
  /**
   * <code>repeated .server.TurnServer turn_servers = 3;</code>
   */
  @java.lang.Override
  public int getTurnServersCount() {
    return turnServers_.size();
  }
  /**
   * <code>repeated .server.TurnServer turn_servers = 3;</code>
   */
  @java.lang.Override
  public com.halloapp.proto.server.TurnServer getTurnServers(int index) {
    return turnServers_.get(index);
  }
  /**
   * <code>repeated .server.TurnServer turn_servers = 3;</code>
   */
  public com.halloapp.proto.server.TurnServerOrBuilder getTurnServersOrBuilder(
      int index) {
    return turnServers_.get(index);
  }
  private void ensureTurnServersIsMutable() {
    com.google.protobuf.Internal.ProtobufList<com.halloapp.proto.server.TurnServer> tmp = turnServers_;
    if (!tmp.isModifiable()) {
      turnServers_ =
          com.google.protobuf.GeneratedMessageLite.mutableCopy(tmp);
     }
  }

  /**
   * <code>repeated .server.TurnServer turn_servers = 3;</code>
   */
  private void setTurnServers(
      int index, com.halloapp.proto.server.TurnServer value) {
    value.getClass();
  ensureTurnServersIsMutable();
    turnServers_.set(index, value);
  }
  /**
   * <code>repeated .server.TurnServer turn_servers = 3;</code>
   */
  private void addTurnServers(com.halloapp.proto.server.TurnServer value) {
    value.getClass();
  ensureTurnServersIsMutable();
    turnServers_.add(value);
  }
  /**
   * <code>repeated .server.TurnServer turn_servers = 3;</code>
   */
  private void addTurnServers(
      int index, com.halloapp.proto.server.TurnServer value) {
    value.getClass();
  ensureTurnServersIsMutable();
    turnServers_.add(index, value);
  }
  /**
   * <code>repeated .server.TurnServer turn_servers = 3;</code>
   */
  private void addAllTurnServers(
      java.lang.Iterable<? extends com.halloapp.proto.server.TurnServer> values) {
    ensureTurnServersIsMutable();
    com.google.protobuf.AbstractMessageLite.addAll(
        values, turnServers_);
  }
  /**
   * <code>repeated .server.TurnServer turn_servers = 3;</code>
   */
  private void clearTurnServers() {
    turnServers_ = emptyProtobufList();
  }
  /**
   * <code>repeated .server.TurnServer turn_servers = 3;</code>
   */
  private void removeTurnServers(int index) {
    ensureTurnServersIsMutable();
    turnServers_.remove(index);
  }

  public static final int TIMESTAMP_MS_FIELD_NUMBER = 4;
  private long timestampMs_;
  /**
   * <code>int64 timestamp_ms = 4;</code>
   * @return The timestampMs.
   */
  @java.lang.Override
  public long getTimestampMs() {
    return timestampMs_;
  }
  /**
   * <code>int64 timestamp_ms = 4;</code>
   * @param value The timestampMs to set.
   */
  private void setTimestampMs(long value) {
    
    timestampMs_ = value;
  }
  /**
   * <code>int64 timestamp_ms = 4;</code>
   */
  private void clearTimestampMs() {
    
    timestampMs_ = 0L;
  }

  public static com.halloapp.proto.server.StartCallResult parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static com.halloapp.proto.server.StartCallResult parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static com.halloapp.proto.server.StartCallResult parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static com.halloapp.proto.server.StartCallResult parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static com.halloapp.proto.server.StartCallResult parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static com.halloapp.proto.server.StartCallResult parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static com.halloapp.proto.server.StartCallResult parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input);
  }
  public static com.halloapp.proto.server.StartCallResult parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input, extensionRegistry);
  }
  public static com.halloapp.proto.server.StartCallResult parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return parseDelimitedFrom(DEFAULT_INSTANCE, input);
  }
  public static com.halloapp.proto.server.StartCallResult parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return parseDelimitedFrom(DEFAULT_INSTANCE, input, extensionRegistry);
  }
  public static com.halloapp.proto.server.StartCallResult parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input);
  }
  public static com.halloapp.proto.server.StartCallResult parseFrom(
      com.google.protobuf.CodedInputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input, extensionRegistry);
  }

  public static Builder newBuilder() {
    return (Builder) DEFAULT_INSTANCE.createBuilder();
  }
  public static Builder newBuilder(com.halloapp.proto.server.StartCallResult prototype) {
    return (Builder) DEFAULT_INSTANCE.createBuilder(prototype);
  }

  /**
   * Protobuf type {@code server.StartCallResult}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessageLite.Builder<
        com.halloapp.proto.server.StartCallResult, Builder> implements
      // @@protoc_insertion_point(builder_implements:server.StartCallResult)
      com.halloapp.proto.server.StartCallResultOrBuilder {
    // Construct using com.halloapp.proto.server.StartCallResult.newBuilder()
    private Builder() {
      super(DEFAULT_INSTANCE);
    }


    /**
     * <code>.server.StartCallResult.Result result = 1;</code>
     * @return The enum numeric value on the wire for result.
     */
    @java.lang.Override
    public int getResultValue() {
      return instance.getResultValue();
    }
    /**
     * <code>.server.StartCallResult.Result result = 1;</code>
     * @param value The result to set.
     * @return This builder for chaining.
     */
    public Builder setResultValue(int value) {
      copyOnWrite();
      instance.setResultValue(value);
      return this;
    }
    /**
     * <code>.server.StartCallResult.Result result = 1;</code>
     * @return The result.
     */
    @java.lang.Override
    public com.halloapp.proto.server.StartCallResult.Result getResult() {
      return instance.getResult();
    }
    /**
     * <code>.server.StartCallResult.Result result = 1;</code>
     * @param value The enum numeric value on the wire for result to set.
     * @return This builder for chaining.
     */
    public Builder setResult(com.halloapp.proto.server.StartCallResult.Result value) {
      copyOnWrite();
      instance.setResult(value);
      return this;
    }
    /**
     * <code>.server.StartCallResult.Result result = 1;</code>
     * @return This builder for chaining.
     */
    public Builder clearResult() {
      copyOnWrite();
      instance.clearResult();
      return this;
    }

    /**
     * <code>repeated .server.StunServer stun_servers = 2;</code>
     */
    @java.lang.Override
    public java.util.List<com.halloapp.proto.server.StunServer> getStunServersList() {
      return java.util.Collections.unmodifiableList(
          instance.getStunServersList());
    }
    /**
     * <code>repeated .server.StunServer stun_servers = 2;</code>
     */
    @java.lang.Override
    public int getStunServersCount() {
      return instance.getStunServersCount();
    }/**
     * <code>repeated .server.StunServer stun_servers = 2;</code>
     */
    @java.lang.Override
    public com.halloapp.proto.server.StunServer getStunServers(int index) {
      return instance.getStunServers(index);
    }
    /**
     * <code>repeated .server.StunServer stun_servers = 2;</code>
     */
    public Builder setStunServers(
        int index, com.halloapp.proto.server.StunServer value) {
      copyOnWrite();
      instance.setStunServers(index, value);
      return this;
    }
    /**
     * <code>repeated .server.StunServer stun_servers = 2;</code>
     */
    public Builder setStunServers(
        int index, com.halloapp.proto.server.StunServer.Builder builderForValue) {
      copyOnWrite();
      instance.setStunServers(index,
          builderForValue.build());
      return this;
    }
    /**
     * <code>repeated .server.StunServer stun_servers = 2;</code>
     */
    public Builder addStunServers(com.halloapp.proto.server.StunServer value) {
      copyOnWrite();
      instance.addStunServers(value);
      return this;
    }
    /**
     * <code>repeated .server.StunServer stun_servers = 2;</code>
     */
    public Builder addStunServers(
        int index, com.halloapp.proto.server.StunServer value) {
      copyOnWrite();
      instance.addStunServers(index, value);
      return this;
    }
    /**
     * <code>repeated .server.StunServer stun_servers = 2;</code>
     */
    public Builder addStunServers(
        com.halloapp.proto.server.StunServer.Builder builderForValue) {
      copyOnWrite();
      instance.addStunServers(builderForValue.build());
      return this;
    }
    /**
     * <code>repeated .server.StunServer stun_servers = 2;</code>
     */
    public Builder addStunServers(
        int index, com.halloapp.proto.server.StunServer.Builder builderForValue) {
      copyOnWrite();
      instance.addStunServers(index,
          builderForValue.build());
      return this;
    }
    /**
     * <code>repeated .server.StunServer stun_servers = 2;</code>
     */
    public Builder addAllStunServers(
        java.lang.Iterable<? extends com.halloapp.proto.server.StunServer> values) {
      copyOnWrite();
      instance.addAllStunServers(values);
      return this;
    }
    /**
     * <code>repeated .server.StunServer stun_servers = 2;</code>
     */
    public Builder clearStunServers() {
      copyOnWrite();
      instance.clearStunServers();
      return this;
    }
    /**
     * <code>repeated .server.StunServer stun_servers = 2;</code>
     */
    public Builder removeStunServers(int index) {
      copyOnWrite();
      instance.removeStunServers(index);
      return this;
    }

    /**
     * <code>repeated .server.TurnServer turn_servers = 3;</code>
     */
    @java.lang.Override
    public java.util.List<com.halloapp.proto.server.TurnServer> getTurnServersList() {
      return java.util.Collections.unmodifiableList(
          instance.getTurnServersList());
    }
    /**
     * <code>repeated .server.TurnServer turn_servers = 3;</code>
     */
    @java.lang.Override
    public int getTurnServersCount() {
      return instance.getTurnServersCount();
    }/**
     * <code>repeated .server.TurnServer turn_servers = 3;</code>
     */
    @java.lang.Override
    public com.halloapp.proto.server.TurnServer getTurnServers(int index) {
      return instance.getTurnServers(index);
    }
    /**
     * <code>repeated .server.TurnServer turn_servers = 3;</code>
     */
    public Builder setTurnServers(
        int index, com.halloapp.proto.server.TurnServer value) {
      copyOnWrite();
      instance.setTurnServers(index, value);
      return this;
    }
    /**
     * <code>repeated .server.TurnServer turn_servers = 3;</code>
     */
    public Builder setTurnServers(
        int index, com.halloapp.proto.server.TurnServer.Builder builderForValue) {
      copyOnWrite();
      instance.setTurnServers(index,
          builderForValue.build());
      return this;
    }
    /**
     * <code>repeated .server.TurnServer turn_servers = 3;</code>
     */
    public Builder addTurnServers(com.halloapp.proto.server.TurnServer value) {
      copyOnWrite();
      instance.addTurnServers(value);
      return this;
    }
    /**
     * <code>repeated .server.TurnServer turn_servers = 3;</code>
     */
    public Builder addTurnServers(
        int index, com.halloapp.proto.server.TurnServer value) {
      copyOnWrite();
      instance.addTurnServers(index, value);
      return this;
    }
    /**
     * <code>repeated .server.TurnServer turn_servers = 3;</code>
     */
    public Builder addTurnServers(
        com.halloapp.proto.server.TurnServer.Builder builderForValue) {
      copyOnWrite();
      instance.addTurnServers(builderForValue.build());
      return this;
    }
    /**
     * <code>repeated .server.TurnServer turn_servers = 3;</code>
     */
    public Builder addTurnServers(
        int index, com.halloapp.proto.server.TurnServer.Builder builderForValue) {
      copyOnWrite();
      instance.addTurnServers(index,
          builderForValue.build());
      return this;
    }
    /**
     * <code>repeated .server.TurnServer turn_servers = 3;</code>
     */
    public Builder addAllTurnServers(
        java.lang.Iterable<? extends com.halloapp.proto.server.TurnServer> values) {
      copyOnWrite();
      instance.addAllTurnServers(values);
      return this;
    }
    /**
     * <code>repeated .server.TurnServer turn_servers = 3;</code>
     */
    public Builder clearTurnServers() {
      copyOnWrite();
      instance.clearTurnServers();
      return this;
    }
    /**
     * <code>repeated .server.TurnServer turn_servers = 3;</code>
     */
    public Builder removeTurnServers(int index) {
      copyOnWrite();
      instance.removeTurnServers(index);
      return this;
    }

    /**
     * <code>int64 timestamp_ms = 4;</code>
     * @return The timestampMs.
     */
    @java.lang.Override
    public long getTimestampMs() {
      return instance.getTimestampMs();
    }
    /**
     * <code>int64 timestamp_ms = 4;</code>
     * @param value The timestampMs to set.
     * @return This builder for chaining.
     */
    public Builder setTimestampMs(long value) {
      copyOnWrite();
      instance.setTimestampMs(value);
      return this;
    }
    /**
     * <code>int64 timestamp_ms = 4;</code>
     * @return This builder for chaining.
     */
    public Builder clearTimestampMs() {
      copyOnWrite();
      instance.clearTimestampMs();
      return this;
    }

    // @@protoc_insertion_point(builder_scope:server.StartCallResult)
  }
  @java.lang.Override
  @java.lang.SuppressWarnings({"unchecked", "fallthrough"})
  protected final java.lang.Object dynamicMethod(
      com.google.protobuf.GeneratedMessageLite.MethodToInvoke method,
      java.lang.Object arg0, java.lang.Object arg1) {
    switch (method) {
      case NEW_MUTABLE_INSTANCE: {
        return new com.halloapp.proto.server.StartCallResult();
      }
      case NEW_BUILDER: {
        return new Builder();
      }
      case BUILD_MESSAGE_INFO: {
          java.lang.Object[] objects = new java.lang.Object[] {
            "result_",
            "stunServers_",
            com.halloapp.proto.server.StunServer.class,
            "turnServers_",
            com.halloapp.proto.server.TurnServer.class,
            "timestampMs_",
          };
          java.lang.String info =
              "\u0000\u0004\u0000\u0000\u0001\u0004\u0004\u0000\u0002\u0000\u0001\f\u0002\u001b" +
              "\u0003\u001b\u0004\u0002";
          return newMessageInfo(DEFAULT_INSTANCE, info, objects);
      }
      // fall through
      case GET_DEFAULT_INSTANCE: {
        return DEFAULT_INSTANCE;
      }
      case GET_PARSER: {
        com.google.protobuf.Parser<com.halloapp.proto.server.StartCallResult> parser = PARSER;
        if (parser == null) {
          synchronized (com.halloapp.proto.server.StartCallResult.class) {
            parser = PARSER;
            if (parser == null) {
              parser =
                  new DefaultInstanceBasedParser<com.halloapp.proto.server.StartCallResult>(
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


  // @@protoc_insertion_point(class_scope:server.StartCallResult)
  private static final com.halloapp.proto.server.StartCallResult DEFAULT_INSTANCE;
  static {
    StartCallResult defaultInstance = new StartCallResult();
    // New instances are implicitly immutable so no need to make
    // immutable.
    DEFAULT_INSTANCE = defaultInstance;
    com.google.protobuf.GeneratedMessageLite.registerDefaultInstance(
      StartCallResult.class, defaultInstance);
  }

  public static com.halloapp.proto.server.StartCallResult getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static volatile com.google.protobuf.Parser<StartCallResult> PARSER;

  public static com.google.protobuf.Parser<StartCallResult> parser() {
    return DEFAULT_INSTANCE.getParserForType();
  }
}
