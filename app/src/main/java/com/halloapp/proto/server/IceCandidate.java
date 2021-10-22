// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: server.proto

package com.halloapp.proto.server;

/**
 * <pre>
 *RFC 4566 https://datatracker.ietf.org/doc/html/rfc4566#section-5.14
 * </pre>
 *
 * Protobuf type {@code server.IceCandidate}
 */
public  final class IceCandidate extends
    com.google.protobuf.GeneratedMessageLite<
        IceCandidate, IceCandidate.Builder> implements
    // @@protoc_insertion_point(message_implements:server.IceCandidate)
    IceCandidateOrBuilder {
  private IceCandidate() {
    callId_ = "";
    sdpMediaId_ = "";
    sdp_ = "";
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

  public static final int SDP_MEDIA_ID_FIELD_NUMBER = 2;
  private java.lang.String sdpMediaId_;
  /**
   * <code>string sdp_media_id = 2;</code>
   * @return The sdpMediaId.
   */
  @java.lang.Override
  public java.lang.String getSdpMediaId() {
    return sdpMediaId_;
  }
  /**
   * <code>string sdp_media_id = 2;</code>
   * @return The bytes for sdpMediaId.
   */
  @java.lang.Override
  public com.google.protobuf.ByteString
      getSdpMediaIdBytes() {
    return com.google.protobuf.ByteString.copyFromUtf8(sdpMediaId_);
  }
  /**
   * <code>string sdp_media_id = 2;</code>
   * @param value The sdpMediaId to set.
   */
  private void setSdpMediaId(
      java.lang.String value) {
    value.getClass();
  
    sdpMediaId_ = value;
  }
  /**
   * <code>string sdp_media_id = 2;</code>
   */
  private void clearSdpMediaId() {
    
    sdpMediaId_ = getDefaultInstance().getSdpMediaId();
  }
  /**
   * <code>string sdp_media_id = 2;</code>
   * @param value The bytes for sdpMediaId to set.
   */
  private void setSdpMediaIdBytes(
      com.google.protobuf.ByteString value) {
    checkByteStringIsUtf8(value);
    sdpMediaId_ = value.toStringUtf8();
    
  }

  public static final int SDP_MEDIA_LINE_INDEX_FIELD_NUMBER = 3;
  private int sdpMediaLineIndex_;
  /**
   * <code>int32 sdp_media_line_index = 3;</code>
   * @return The sdpMediaLineIndex.
   */
  @java.lang.Override
  public int getSdpMediaLineIndex() {
    return sdpMediaLineIndex_;
  }
  /**
   * <code>int32 sdp_media_line_index = 3;</code>
   * @param value The sdpMediaLineIndex to set.
   */
  private void setSdpMediaLineIndex(int value) {
    
    sdpMediaLineIndex_ = value;
  }
  /**
   * <code>int32 sdp_media_line_index = 3;</code>
   */
  private void clearSdpMediaLineIndex() {
    
    sdpMediaLineIndex_ = 0;
  }

  public static final int SDP_FIELD_NUMBER = 4;
  private java.lang.String sdp_;
  /**
   * <code>string sdp = 4;</code>
   * @return The sdp.
   */
  @java.lang.Override
  public java.lang.String getSdp() {
    return sdp_;
  }
  /**
   * <code>string sdp = 4;</code>
   * @return The bytes for sdp.
   */
  @java.lang.Override
  public com.google.protobuf.ByteString
      getSdpBytes() {
    return com.google.protobuf.ByteString.copyFromUtf8(sdp_);
  }
  /**
   * <code>string sdp = 4;</code>
   * @param value The sdp to set.
   */
  private void setSdp(
      java.lang.String value) {
    value.getClass();
  
    sdp_ = value;
  }
  /**
   * <code>string sdp = 4;</code>
   */
  private void clearSdp() {
    
    sdp_ = getDefaultInstance().getSdp();
  }
  /**
   * <code>string sdp = 4;</code>
   * @param value The bytes for sdp to set.
   */
  private void setSdpBytes(
      com.google.protobuf.ByteString value) {
    checkByteStringIsUtf8(value);
    sdp_ = value.toStringUtf8();
    
  }

  public static com.halloapp.proto.server.IceCandidate parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static com.halloapp.proto.server.IceCandidate parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static com.halloapp.proto.server.IceCandidate parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static com.halloapp.proto.server.IceCandidate parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static com.halloapp.proto.server.IceCandidate parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static com.halloapp.proto.server.IceCandidate parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static com.halloapp.proto.server.IceCandidate parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input);
  }
  public static com.halloapp.proto.server.IceCandidate parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input, extensionRegistry);
  }
  public static com.halloapp.proto.server.IceCandidate parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return parseDelimitedFrom(DEFAULT_INSTANCE, input);
  }
  public static com.halloapp.proto.server.IceCandidate parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return parseDelimitedFrom(DEFAULT_INSTANCE, input, extensionRegistry);
  }
  public static com.halloapp.proto.server.IceCandidate parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input);
  }
  public static com.halloapp.proto.server.IceCandidate parseFrom(
      com.google.protobuf.CodedInputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input, extensionRegistry);
  }

  public static Builder newBuilder() {
    return (Builder) DEFAULT_INSTANCE.createBuilder();
  }
  public static Builder newBuilder(com.halloapp.proto.server.IceCandidate prototype) {
    return (Builder) DEFAULT_INSTANCE.createBuilder(prototype);
  }

  /**
   * <pre>
   *RFC 4566 https://datatracker.ietf.org/doc/html/rfc4566#section-5.14
   * </pre>
   *
   * Protobuf type {@code server.IceCandidate}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessageLite.Builder<
        com.halloapp.proto.server.IceCandidate, Builder> implements
      // @@protoc_insertion_point(builder_implements:server.IceCandidate)
      com.halloapp.proto.server.IceCandidateOrBuilder {
    // Construct using com.halloapp.proto.server.IceCandidate.newBuilder()
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
     * <code>string sdp_media_id = 2;</code>
     * @return The sdpMediaId.
     */
    @java.lang.Override
    public java.lang.String getSdpMediaId() {
      return instance.getSdpMediaId();
    }
    /**
     * <code>string sdp_media_id = 2;</code>
     * @return The bytes for sdpMediaId.
     */
    @java.lang.Override
    public com.google.protobuf.ByteString
        getSdpMediaIdBytes() {
      return instance.getSdpMediaIdBytes();
    }
    /**
     * <code>string sdp_media_id = 2;</code>
     * @param value The sdpMediaId to set.
     * @return This builder for chaining.
     */
    public Builder setSdpMediaId(
        java.lang.String value) {
      copyOnWrite();
      instance.setSdpMediaId(value);
      return this;
    }
    /**
     * <code>string sdp_media_id = 2;</code>
     * @return This builder for chaining.
     */
    public Builder clearSdpMediaId() {
      copyOnWrite();
      instance.clearSdpMediaId();
      return this;
    }
    /**
     * <code>string sdp_media_id = 2;</code>
     * @param value The bytes for sdpMediaId to set.
     * @return This builder for chaining.
     */
    public Builder setSdpMediaIdBytes(
        com.google.protobuf.ByteString value) {
      copyOnWrite();
      instance.setSdpMediaIdBytes(value);
      return this;
    }

    /**
     * <code>int32 sdp_media_line_index = 3;</code>
     * @return The sdpMediaLineIndex.
     */
    @java.lang.Override
    public int getSdpMediaLineIndex() {
      return instance.getSdpMediaLineIndex();
    }
    /**
     * <code>int32 sdp_media_line_index = 3;</code>
     * @param value The sdpMediaLineIndex to set.
     * @return This builder for chaining.
     */
    public Builder setSdpMediaLineIndex(int value) {
      copyOnWrite();
      instance.setSdpMediaLineIndex(value);
      return this;
    }
    /**
     * <code>int32 sdp_media_line_index = 3;</code>
     * @return This builder for chaining.
     */
    public Builder clearSdpMediaLineIndex() {
      copyOnWrite();
      instance.clearSdpMediaLineIndex();
      return this;
    }

    /**
     * <code>string sdp = 4;</code>
     * @return The sdp.
     */
    @java.lang.Override
    public java.lang.String getSdp() {
      return instance.getSdp();
    }
    /**
     * <code>string sdp = 4;</code>
     * @return The bytes for sdp.
     */
    @java.lang.Override
    public com.google.protobuf.ByteString
        getSdpBytes() {
      return instance.getSdpBytes();
    }
    /**
     * <code>string sdp = 4;</code>
     * @param value The sdp to set.
     * @return This builder for chaining.
     */
    public Builder setSdp(
        java.lang.String value) {
      copyOnWrite();
      instance.setSdp(value);
      return this;
    }
    /**
     * <code>string sdp = 4;</code>
     * @return This builder for chaining.
     */
    public Builder clearSdp() {
      copyOnWrite();
      instance.clearSdp();
      return this;
    }
    /**
     * <code>string sdp = 4;</code>
     * @param value The bytes for sdp to set.
     * @return This builder for chaining.
     */
    public Builder setSdpBytes(
        com.google.protobuf.ByteString value) {
      copyOnWrite();
      instance.setSdpBytes(value);
      return this;
    }

    // @@protoc_insertion_point(builder_scope:server.IceCandidate)
  }
  @java.lang.Override
  @java.lang.SuppressWarnings({"unchecked", "fallthrough"})
  protected final java.lang.Object dynamicMethod(
      com.google.protobuf.GeneratedMessageLite.MethodToInvoke method,
      java.lang.Object arg0, java.lang.Object arg1) {
    switch (method) {
      case NEW_MUTABLE_INSTANCE: {
        return new com.halloapp.proto.server.IceCandidate();
      }
      case NEW_BUILDER: {
        return new Builder();
      }
      case BUILD_MESSAGE_INFO: {
          java.lang.Object[] objects = new java.lang.Object[] {
            "callId_",
            "sdpMediaId_",
            "sdpMediaLineIndex_",
            "sdp_",
          };
          java.lang.String info =
              "\u0000\u0004\u0000\u0000\u0001\u0004\u0004\u0000\u0000\u0000\u0001\u0208\u0002\u0208" +
              "\u0003\u0004\u0004\u0208";
          return newMessageInfo(DEFAULT_INSTANCE, info, objects);
      }
      // fall through
      case GET_DEFAULT_INSTANCE: {
        return DEFAULT_INSTANCE;
      }
      case GET_PARSER: {
        com.google.protobuf.Parser<com.halloapp.proto.server.IceCandidate> parser = PARSER;
        if (parser == null) {
          synchronized (com.halloapp.proto.server.IceCandidate.class) {
            parser = PARSER;
            if (parser == null) {
              parser =
                  new DefaultInstanceBasedParser<com.halloapp.proto.server.IceCandidate>(
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


  // @@protoc_insertion_point(class_scope:server.IceCandidate)
  private static final com.halloapp.proto.server.IceCandidate DEFAULT_INSTANCE;
  static {
    IceCandidate defaultInstance = new IceCandidate();
    // New instances are implicitly immutable so no need to make
    // immutable.
    DEFAULT_INSTANCE = defaultInstance;
    com.google.protobuf.GeneratedMessageLite.registerDefaultInstance(
      IceCandidate.class, defaultInstance);
  }

  public static com.halloapp.proto.server.IceCandidate getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static volatile com.google.protobuf.Parser<IceCandidate> PARSER;

  public static com.google.protobuf.Parser<IceCandidate> parser() {
    return DEFAULT_INSTANCE.getParserForType();
  }
}

