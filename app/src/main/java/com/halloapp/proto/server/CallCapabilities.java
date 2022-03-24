// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: server.proto

package com.halloapp.proto.server;

/**
 * Protobuf type {@code server.CallCapabilities}
 */
public  final class CallCapabilities extends
    com.google.protobuf.GeneratedMessageLite<
        CallCapabilities, CallCapabilities.Builder> implements
    // @@protoc_insertion_point(message_implements:server.CallCapabilities)
    CallCapabilitiesOrBuilder {
  private CallCapabilities() {
  }
  public static final int PRE_ANSWER_FIELD_NUMBER = 1;
  private boolean preAnswer_;
  /**
   * <pre>
   * true if the initiator supports pre-answering the call
   * </pre>
   *
   * <code>bool pre_answer = 1;</code>
   * @return The preAnswer.
   */
  @java.lang.Override
  public boolean getPreAnswer() {
    return preAnswer_;
  }
  /**
   * <pre>
   * true if the initiator supports pre-answering the call
   * </pre>
   *
   * <code>bool pre_answer = 1;</code>
   * @param value The preAnswer to set.
   */
  private void setPreAnswer(boolean value) {
    
    preAnswer_ = value;
  }
  /**
   * <pre>
   * true if the initiator supports pre-answering the call
   * </pre>
   *
   * <code>bool pre_answer = 1;</code>
   */
  private void clearPreAnswer() {
    
    preAnswer_ = false;
  }

  public static final int SDP_RESTART_FIELD_NUMBER = 2;
  private boolean sdpRestart_;
  /**
   * <pre>
   * true if the initiator supports CallSdp ice restart
   * </pre>
   *
   * <code>bool sdp_restart = 2;</code>
   * @return The sdpRestart.
   */
  @java.lang.Override
  public boolean getSdpRestart() {
    return sdpRestart_;
  }
  /**
   * <pre>
   * true if the initiator supports CallSdp ice restart
   * </pre>
   *
   * <code>bool sdp_restart = 2;</code>
   * @param value The sdpRestart to set.
   */
  private void setSdpRestart(boolean value) {
    
    sdpRestart_ = value;
  }
  /**
   * <pre>
   * true if the initiator supports CallSdp ice restart
   * </pre>
   *
   * <code>bool sdp_restart = 2;</code>
   */
  private void clearSdpRestart() {
    
    sdpRestart_ = false;
  }

  public static com.halloapp.proto.server.CallCapabilities parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static com.halloapp.proto.server.CallCapabilities parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static com.halloapp.proto.server.CallCapabilities parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static com.halloapp.proto.server.CallCapabilities parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static com.halloapp.proto.server.CallCapabilities parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static com.halloapp.proto.server.CallCapabilities parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static com.halloapp.proto.server.CallCapabilities parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input);
  }
  public static com.halloapp.proto.server.CallCapabilities parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input, extensionRegistry);
  }
  public static com.halloapp.proto.server.CallCapabilities parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return parseDelimitedFrom(DEFAULT_INSTANCE, input);
  }
  public static com.halloapp.proto.server.CallCapabilities parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return parseDelimitedFrom(DEFAULT_INSTANCE, input, extensionRegistry);
  }
  public static com.halloapp.proto.server.CallCapabilities parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input);
  }
  public static com.halloapp.proto.server.CallCapabilities parseFrom(
      com.google.protobuf.CodedInputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input, extensionRegistry);
  }

  public static Builder newBuilder() {
    return (Builder) DEFAULT_INSTANCE.createBuilder();
  }
  public static Builder newBuilder(com.halloapp.proto.server.CallCapabilities prototype) {
    return (Builder) DEFAULT_INSTANCE.createBuilder(prototype);
  }

  /**
   * Protobuf type {@code server.CallCapabilities}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessageLite.Builder<
        com.halloapp.proto.server.CallCapabilities, Builder> implements
      // @@protoc_insertion_point(builder_implements:server.CallCapabilities)
      com.halloapp.proto.server.CallCapabilitiesOrBuilder {
    // Construct using com.halloapp.proto.server.CallCapabilities.newBuilder()
    private Builder() {
      super(DEFAULT_INSTANCE);
    }


    /**
     * <pre>
     * true if the initiator supports pre-answering the call
     * </pre>
     *
     * <code>bool pre_answer = 1;</code>
     * @return The preAnswer.
     */
    @java.lang.Override
    public boolean getPreAnswer() {
      return instance.getPreAnswer();
    }
    /**
     * <pre>
     * true if the initiator supports pre-answering the call
     * </pre>
     *
     * <code>bool pre_answer = 1;</code>
     * @param value The preAnswer to set.
     * @return This builder for chaining.
     */
    public Builder setPreAnswer(boolean value) {
      copyOnWrite();
      instance.setPreAnswer(value);
      return this;
    }
    /**
     * <pre>
     * true if the initiator supports pre-answering the call
     * </pre>
     *
     * <code>bool pre_answer = 1;</code>
     * @return This builder for chaining.
     */
    public Builder clearPreAnswer() {
      copyOnWrite();
      instance.clearPreAnswer();
      return this;
    }

    /**
     * <pre>
     * true if the initiator supports CallSdp ice restart
     * </pre>
     *
     * <code>bool sdp_restart = 2;</code>
     * @return The sdpRestart.
     */
    @java.lang.Override
    public boolean getSdpRestart() {
      return instance.getSdpRestart();
    }
    /**
     * <pre>
     * true if the initiator supports CallSdp ice restart
     * </pre>
     *
     * <code>bool sdp_restart = 2;</code>
     * @param value The sdpRestart to set.
     * @return This builder for chaining.
     */
    public Builder setSdpRestart(boolean value) {
      copyOnWrite();
      instance.setSdpRestart(value);
      return this;
    }
    /**
     * <pre>
     * true if the initiator supports CallSdp ice restart
     * </pre>
     *
     * <code>bool sdp_restart = 2;</code>
     * @return This builder for chaining.
     */
    public Builder clearSdpRestart() {
      copyOnWrite();
      instance.clearSdpRestart();
      return this;
    }

    // @@protoc_insertion_point(builder_scope:server.CallCapabilities)
  }
  @java.lang.Override
  @java.lang.SuppressWarnings({"unchecked", "fallthrough"})
  protected final java.lang.Object dynamicMethod(
      com.google.protobuf.GeneratedMessageLite.MethodToInvoke method,
      java.lang.Object arg0, java.lang.Object arg1) {
    switch (method) {
      case NEW_MUTABLE_INSTANCE: {
        return new com.halloapp.proto.server.CallCapabilities();
      }
      case NEW_BUILDER: {
        return new Builder();
      }
      case BUILD_MESSAGE_INFO: {
          java.lang.Object[] objects = new java.lang.Object[] {
            "preAnswer_",
            "sdpRestart_",
          };
          java.lang.String info =
              "\u0000\u0002\u0000\u0000\u0001\u0002\u0002\u0000\u0000\u0000\u0001\u0007\u0002\u0007" +
              "";
          return newMessageInfo(DEFAULT_INSTANCE, info, objects);
      }
      // fall through
      case GET_DEFAULT_INSTANCE: {
        return DEFAULT_INSTANCE;
      }
      case GET_PARSER: {
        com.google.protobuf.Parser<com.halloapp.proto.server.CallCapabilities> parser = PARSER;
        if (parser == null) {
          synchronized (com.halloapp.proto.server.CallCapabilities.class) {
            parser = PARSER;
            if (parser == null) {
              parser =
                  new DefaultInstanceBasedParser<com.halloapp.proto.server.CallCapabilities>(
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


  // @@protoc_insertion_point(class_scope:server.CallCapabilities)
  private static final com.halloapp.proto.server.CallCapabilities DEFAULT_INSTANCE;
  static {
    CallCapabilities defaultInstance = new CallCapabilities();
    // New instances are implicitly immutable so no need to make
    // immutable.
    DEFAULT_INSTANCE = defaultInstance;
    com.google.protobuf.GeneratedMessageLite.registerDefaultInstance(
      CallCapabilities.class, defaultInstance);
  }

  public static com.halloapp.proto.server.CallCapabilities getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static volatile com.google.protobuf.Parser<CallCapabilities> PARSER;

  public static com.google.protobuf.Parser<CallCapabilities> parser() {
    return DEFAULT_INSTANCE.getParserForType();
  }
}
