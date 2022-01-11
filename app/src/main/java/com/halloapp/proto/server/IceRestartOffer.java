// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: server.proto

package com.halloapp.proto.server;

/**
 * Protobuf type {@code server.IceRestartOffer}
 */
public  final class IceRestartOffer extends
    com.google.protobuf.GeneratedMessageLite<
        IceRestartOffer, IceRestartOffer.Builder> implements
    // @@protoc_insertion_point(message_implements:server.IceRestartOffer)
    IceRestartOfferOrBuilder {
  private IceRestartOffer() {
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

  public static final int IDX_FIELD_NUMBER = 2;
  private int idx_;
  /**
   * <code>int32 idx = 2;</code>
   * @return The idx.
   */
  @java.lang.Override
  public int getIdx() {
    return idx_;
  }
  /**
   * <code>int32 idx = 2;</code>
   * @param value The idx to set.
   */
  private void setIdx(int value) {
    
    idx_ = value;
  }
  /**
   * <code>int32 idx = 2;</code>
   */
  private void clearIdx() {
    
    idx_ = 0;
  }

  public static final int WEBRTC_OFFER_FIELD_NUMBER = 3;
  private com.halloapp.proto.server.WebRtcSessionDescription webrtcOffer_;
  /**
   * <code>.server.WebRtcSessionDescription webrtc_offer = 3;</code>
   */
  @java.lang.Override
  public boolean hasWebrtcOffer() {
    return webrtcOffer_ != null;
  }
  /**
   * <code>.server.WebRtcSessionDescription webrtc_offer = 3;</code>
   */
  @java.lang.Override
  public com.halloapp.proto.server.WebRtcSessionDescription getWebrtcOffer() {
    return webrtcOffer_ == null ? com.halloapp.proto.server.WebRtcSessionDescription.getDefaultInstance() : webrtcOffer_;
  }
  /**
   * <code>.server.WebRtcSessionDescription webrtc_offer = 3;</code>
   */
  private void setWebrtcOffer(com.halloapp.proto.server.WebRtcSessionDescription value) {
    value.getClass();
  webrtcOffer_ = value;
    
    }
  /**
   * <code>.server.WebRtcSessionDescription webrtc_offer = 3;</code>
   */
  @java.lang.SuppressWarnings({"ReferenceEquality"})
  private void mergeWebrtcOffer(com.halloapp.proto.server.WebRtcSessionDescription value) {
    value.getClass();
  if (webrtcOffer_ != null &&
        webrtcOffer_ != com.halloapp.proto.server.WebRtcSessionDescription.getDefaultInstance()) {
      webrtcOffer_ =
        com.halloapp.proto.server.WebRtcSessionDescription.newBuilder(webrtcOffer_).mergeFrom(value).buildPartial();
    } else {
      webrtcOffer_ = value;
    }
    
  }
  /**
   * <code>.server.WebRtcSessionDescription webrtc_offer = 3;</code>
   */
  private void clearWebrtcOffer() {  webrtcOffer_ = null;
    
  }

  public static com.halloapp.proto.server.IceRestartOffer parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static com.halloapp.proto.server.IceRestartOffer parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static com.halloapp.proto.server.IceRestartOffer parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static com.halloapp.proto.server.IceRestartOffer parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static com.halloapp.proto.server.IceRestartOffer parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static com.halloapp.proto.server.IceRestartOffer parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static com.halloapp.proto.server.IceRestartOffer parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input);
  }
  public static com.halloapp.proto.server.IceRestartOffer parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input, extensionRegistry);
  }
  public static com.halloapp.proto.server.IceRestartOffer parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return parseDelimitedFrom(DEFAULT_INSTANCE, input);
  }
  public static com.halloapp.proto.server.IceRestartOffer parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return parseDelimitedFrom(DEFAULT_INSTANCE, input, extensionRegistry);
  }
  public static com.halloapp.proto.server.IceRestartOffer parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input);
  }
  public static com.halloapp.proto.server.IceRestartOffer parseFrom(
      com.google.protobuf.CodedInputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input, extensionRegistry);
  }

  public static Builder newBuilder() {
    return (Builder) DEFAULT_INSTANCE.createBuilder();
  }
  public static Builder newBuilder(com.halloapp.proto.server.IceRestartOffer prototype) {
    return (Builder) DEFAULT_INSTANCE.createBuilder(prototype);
  }

  /**
   * Protobuf type {@code server.IceRestartOffer}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessageLite.Builder<
        com.halloapp.proto.server.IceRestartOffer, Builder> implements
      // @@protoc_insertion_point(builder_implements:server.IceRestartOffer)
      com.halloapp.proto.server.IceRestartOfferOrBuilder {
    // Construct using com.halloapp.proto.server.IceRestartOffer.newBuilder()
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
     * <code>int32 idx = 2;</code>
     * @return The idx.
     */
    @java.lang.Override
    public int getIdx() {
      return instance.getIdx();
    }
    /**
     * <code>int32 idx = 2;</code>
     * @param value The idx to set.
     * @return This builder for chaining.
     */
    public Builder setIdx(int value) {
      copyOnWrite();
      instance.setIdx(value);
      return this;
    }
    /**
     * <code>int32 idx = 2;</code>
     * @return This builder for chaining.
     */
    public Builder clearIdx() {
      copyOnWrite();
      instance.clearIdx();
      return this;
    }

    /**
     * <code>.server.WebRtcSessionDescription webrtc_offer = 3;</code>
     */
    @java.lang.Override
    public boolean hasWebrtcOffer() {
      return instance.hasWebrtcOffer();
    }
    /**
     * <code>.server.WebRtcSessionDescription webrtc_offer = 3;</code>
     */
    @java.lang.Override
    public com.halloapp.proto.server.WebRtcSessionDescription getWebrtcOffer() {
      return instance.getWebrtcOffer();
    }
    /**
     * <code>.server.WebRtcSessionDescription webrtc_offer = 3;</code>
     */
    public Builder setWebrtcOffer(com.halloapp.proto.server.WebRtcSessionDescription value) {
      copyOnWrite();
      instance.setWebrtcOffer(value);
      return this;
      }
    /**
     * <code>.server.WebRtcSessionDescription webrtc_offer = 3;</code>
     */
    public Builder setWebrtcOffer(
        com.halloapp.proto.server.WebRtcSessionDescription.Builder builderForValue) {
      copyOnWrite();
      instance.setWebrtcOffer(builderForValue.build());
      return this;
    }
    /**
     * <code>.server.WebRtcSessionDescription webrtc_offer = 3;</code>
     */
    public Builder mergeWebrtcOffer(com.halloapp.proto.server.WebRtcSessionDescription value) {
      copyOnWrite();
      instance.mergeWebrtcOffer(value);
      return this;
    }
    /**
     * <code>.server.WebRtcSessionDescription webrtc_offer = 3;</code>
     */
    public Builder clearWebrtcOffer() {  copyOnWrite();
      instance.clearWebrtcOffer();
      return this;
    }

    // @@protoc_insertion_point(builder_scope:server.IceRestartOffer)
  }
  @java.lang.Override
  @java.lang.SuppressWarnings({"unchecked", "fallthrough"})
  protected final java.lang.Object dynamicMethod(
      com.google.protobuf.GeneratedMessageLite.MethodToInvoke method,
      java.lang.Object arg0, java.lang.Object arg1) {
    switch (method) {
      case NEW_MUTABLE_INSTANCE: {
        return new com.halloapp.proto.server.IceRestartOffer();
      }
      case NEW_BUILDER: {
        return new Builder();
      }
      case BUILD_MESSAGE_INFO: {
          java.lang.Object[] objects = new java.lang.Object[] {
            "callId_",
            "idx_",
            "webrtcOffer_",
          };
          java.lang.String info =
              "\u0000\u0003\u0000\u0000\u0001\u0003\u0003\u0000\u0000\u0000\u0001\u0208\u0002\u0004" +
              "\u0003\t";
          return newMessageInfo(DEFAULT_INSTANCE, info, objects);
      }
      // fall through
      case GET_DEFAULT_INSTANCE: {
        return DEFAULT_INSTANCE;
      }
      case GET_PARSER: {
        com.google.protobuf.Parser<com.halloapp.proto.server.IceRestartOffer> parser = PARSER;
        if (parser == null) {
          synchronized (com.halloapp.proto.server.IceRestartOffer.class) {
            parser = PARSER;
            if (parser == null) {
              parser =
                  new DefaultInstanceBasedParser<com.halloapp.proto.server.IceRestartOffer>(
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


  // @@protoc_insertion_point(class_scope:server.IceRestartOffer)
  private static final com.halloapp.proto.server.IceRestartOffer DEFAULT_INSTANCE;
  static {
    IceRestartOffer defaultInstance = new IceRestartOffer();
    // New instances are implicitly immutable so no need to make
    // immutable.
    DEFAULT_INSTANCE = defaultInstance;
    com.google.protobuf.GeneratedMessageLite.registerDefaultInstance(
      IceRestartOffer.class, defaultInstance);
  }

  public static com.halloapp.proto.server.IceRestartOffer getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static volatile com.google.protobuf.Parser<IceRestartOffer> PARSER;

  public static com.google.protobuf.Parser<IceRestartOffer> parser() {
    return DEFAULT_INSTANCE.getParserForType();
  }
}
