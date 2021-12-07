// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: server.proto

package com.halloapp.proto.server;

/**
 * Protobuf type {@code server.IncomingCall}
 */
public  final class IncomingCall extends
    com.google.protobuf.GeneratedMessageLite<
        IncomingCall, IncomingCall.Builder> implements
    // @@protoc_insertion_point(message_implements:server.IncomingCall)
    IncomingCallOrBuilder {
  private IncomingCall() {
    callId_ = "";
    stunServers_ = emptyProtobufList();
    turnServers_ = emptyProtobufList();
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

  public static final int CALL_TYPE_FIELD_NUMBER = 2;
  private int callType_;
  /**
   * <code>.server.CallType call_type = 2;</code>
   * @return The enum numeric value on the wire for callType.
   */
  @java.lang.Override
  public int getCallTypeValue() {
    return callType_;
  }
  /**
   * <code>.server.CallType call_type = 2;</code>
   * @return The callType.
   */
  @java.lang.Override
  public com.halloapp.proto.server.CallType getCallType() {
    com.halloapp.proto.server.CallType result = com.halloapp.proto.server.CallType.forNumber(callType_);
    return result == null ? com.halloapp.proto.server.CallType.UNRECOGNIZED : result;
  }
  /**
   * <code>.server.CallType call_type = 2;</code>
   * @param value The enum numeric value on the wire for callType to set.
   */
  private void setCallTypeValue(int value) {
      callType_ = value;
  }
  /**
   * <code>.server.CallType call_type = 2;</code>
   * @param value The callType to set.
   */
  private void setCallType(com.halloapp.proto.server.CallType value) {
    callType_ = value.getNumber();
    
  }
  /**
   * <code>.server.CallType call_type = 2;</code>
   */
  private void clearCallType() {
    
    callType_ = 0;
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

  public static final int STUN_SERVERS_FIELD_NUMBER = 4;
  private com.google.protobuf.Internal.ProtobufList<com.halloapp.proto.server.StunServer> stunServers_;
  /**
   * <code>repeated .server.StunServer stun_servers = 4;</code>
   */
  @java.lang.Override
  public java.util.List<com.halloapp.proto.server.StunServer> getStunServersList() {
    return stunServers_;
  }
  /**
   * <code>repeated .server.StunServer stun_servers = 4;</code>
   */
  public java.util.List<? extends com.halloapp.proto.server.StunServerOrBuilder> 
      getStunServersOrBuilderList() {
    return stunServers_;
  }
  /**
   * <code>repeated .server.StunServer stun_servers = 4;</code>
   */
  @java.lang.Override
  public int getStunServersCount() {
    return stunServers_.size();
  }
  /**
   * <code>repeated .server.StunServer stun_servers = 4;</code>
   */
  @java.lang.Override
  public com.halloapp.proto.server.StunServer getStunServers(int index) {
    return stunServers_.get(index);
  }
  /**
   * <code>repeated .server.StunServer stun_servers = 4;</code>
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
   * <code>repeated .server.StunServer stun_servers = 4;</code>
   */
  private void setStunServers(
      int index, com.halloapp.proto.server.StunServer value) {
    value.getClass();
  ensureStunServersIsMutable();
    stunServers_.set(index, value);
  }
  /**
   * <code>repeated .server.StunServer stun_servers = 4;</code>
   */
  private void addStunServers(com.halloapp.proto.server.StunServer value) {
    value.getClass();
  ensureStunServersIsMutable();
    stunServers_.add(value);
  }
  /**
   * <code>repeated .server.StunServer stun_servers = 4;</code>
   */
  private void addStunServers(
      int index, com.halloapp.proto.server.StunServer value) {
    value.getClass();
  ensureStunServersIsMutable();
    stunServers_.add(index, value);
  }
  /**
   * <code>repeated .server.StunServer stun_servers = 4;</code>
   */
  private void addAllStunServers(
      java.lang.Iterable<? extends com.halloapp.proto.server.StunServer> values) {
    ensureStunServersIsMutable();
    com.google.protobuf.AbstractMessageLite.addAll(
        values, stunServers_);
  }
  /**
   * <code>repeated .server.StunServer stun_servers = 4;</code>
   */
  private void clearStunServers() {
    stunServers_ = emptyProtobufList();
  }
  /**
   * <code>repeated .server.StunServer stun_servers = 4;</code>
   */
  private void removeStunServers(int index) {
    ensureStunServersIsMutable();
    stunServers_.remove(index);
  }

  public static final int TURN_SERVERS_FIELD_NUMBER = 5;
  private com.google.protobuf.Internal.ProtobufList<com.halloapp.proto.server.TurnServer> turnServers_;
  /**
   * <code>repeated .server.TurnServer turn_servers = 5;</code>
   */
  @java.lang.Override
  public java.util.List<com.halloapp.proto.server.TurnServer> getTurnServersList() {
    return turnServers_;
  }
  /**
   * <code>repeated .server.TurnServer turn_servers = 5;</code>
   */
  public java.util.List<? extends com.halloapp.proto.server.TurnServerOrBuilder> 
      getTurnServersOrBuilderList() {
    return turnServers_;
  }
  /**
   * <code>repeated .server.TurnServer turn_servers = 5;</code>
   */
  @java.lang.Override
  public int getTurnServersCount() {
    return turnServers_.size();
  }
  /**
   * <code>repeated .server.TurnServer turn_servers = 5;</code>
   */
  @java.lang.Override
  public com.halloapp.proto.server.TurnServer getTurnServers(int index) {
    return turnServers_.get(index);
  }
  /**
   * <code>repeated .server.TurnServer turn_servers = 5;</code>
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
   * <code>repeated .server.TurnServer turn_servers = 5;</code>
   */
  private void setTurnServers(
      int index, com.halloapp.proto.server.TurnServer value) {
    value.getClass();
  ensureTurnServersIsMutable();
    turnServers_.set(index, value);
  }
  /**
   * <code>repeated .server.TurnServer turn_servers = 5;</code>
   */
  private void addTurnServers(com.halloapp.proto.server.TurnServer value) {
    value.getClass();
  ensureTurnServersIsMutable();
    turnServers_.add(value);
  }
  /**
   * <code>repeated .server.TurnServer turn_servers = 5;</code>
   */
  private void addTurnServers(
      int index, com.halloapp.proto.server.TurnServer value) {
    value.getClass();
  ensureTurnServersIsMutable();
    turnServers_.add(index, value);
  }
  /**
   * <code>repeated .server.TurnServer turn_servers = 5;</code>
   */
  private void addAllTurnServers(
      java.lang.Iterable<? extends com.halloapp.proto.server.TurnServer> values) {
    ensureTurnServersIsMutable();
    com.google.protobuf.AbstractMessageLite.addAll(
        values, turnServers_);
  }
  /**
   * <code>repeated .server.TurnServer turn_servers = 5;</code>
   */
  private void clearTurnServers() {
    turnServers_ = emptyProtobufList();
  }
  /**
   * <code>repeated .server.TurnServer turn_servers = 5;</code>
   */
  private void removeTurnServers(int index) {
    ensureTurnServersIsMutable();
    turnServers_.remove(index);
  }

  public static final int TIMESTAMP_MS_FIELD_NUMBER = 6;
  private long timestampMs_;
  /**
   * <pre>
   * ts when message is generated by the server
   * </pre>
   *
   * <code>int64 timestamp_ms = 6;</code>
   * @return The timestampMs.
   */
  @java.lang.Override
  public long getTimestampMs() {
    return timestampMs_;
  }
  /**
   * <pre>
   * ts when message is generated by the server
   * </pre>
   *
   * <code>int64 timestamp_ms = 6;</code>
   * @param value The timestampMs to set.
   */
  private void setTimestampMs(long value) {
    
    timestampMs_ = value;
  }
  /**
   * <pre>
   * ts when message is generated by the server
   * </pre>
   *
   * <code>int64 timestamp_ms = 6;</code>
   */
  private void clearTimestampMs() {
    
    timestampMs_ = 0L;
  }

  public static final int SERVER_SENT_TS_MS_FIELD_NUMBER = 7;
  private long serverSentTsMs_;
  /**
   * <pre>
   * ts when the message is send to receiver
   * </pre>
   *
   * <code>int64 server_sent_ts_ms = 7;</code>
   * @return The serverSentTsMs.
   */
  @java.lang.Override
  public long getServerSentTsMs() {
    return serverSentTsMs_;
  }
  /**
   * <pre>
   * ts when the message is send to receiver
   * </pre>
   *
   * <code>int64 server_sent_ts_ms = 7;</code>
   * @param value The serverSentTsMs to set.
   */
  private void setServerSentTsMs(long value) {
    
    serverSentTsMs_ = value;
  }
  /**
   * <pre>
   * ts when the message is send to receiver
   * </pre>
   *
   * <code>int64 server_sent_ts_ms = 7;</code>
   */
  private void clearServerSentTsMs() {
    
    serverSentTsMs_ = 0L;
  }

  public static com.halloapp.proto.server.IncomingCall parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static com.halloapp.proto.server.IncomingCall parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static com.halloapp.proto.server.IncomingCall parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static com.halloapp.proto.server.IncomingCall parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static com.halloapp.proto.server.IncomingCall parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static com.halloapp.proto.server.IncomingCall parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static com.halloapp.proto.server.IncomingCall parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input);
  }
  public static com.halloapp.proto.server.IncomingCall parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input, extensionRegistry);
  }
  public static com.halloapp.proto.server.IncomingCall parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return parseDelimitedFrom(DEFAULT_INSTANCE, input);
  }
  public static com.halloapp.proto.server.IncomingCall parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return parseDelimitedFrom(DEFAULT_INSTANCE, input, extensionRegistry);
  }
  public static com.halloapp.proto.server.IncomingCall parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input);
  }
  public static com.halloapp.proto.server.IncomingCall parseFrom(
      com.google.protobuf.CodedInputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input, extensionRegistry);
  }

  public static Builder newBuilder() {
    return (Builder) DEFAULT_INSTANCE.createBuilder();
  }
  public static Builder newBuilder(com.halloapp.proto.server.IncomingCall prototype) {
    return (Builder) DEFAULT_INSTANCE.createBuilder(prototype);
  }

  /**
   * Protobuf type {@code server.IncomingCall}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessageLite.Builder<
        com.halloapp.proto.server.IncomingCall, Builder> implements
      // @@protoc_insertion_point(builder_implements:server.IncomingCall)
      com.halloapp.proto.server.IncomingCallOrBuilder {
    // Construct using com.halloapp.proto.server.IncomingCall.newBuilder()
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
     * <code>.server.CallType call_type = 2;</code>
     * @return The enum numeric value on the wire for callType.
     */
    @java.lang.Override
    public int getCallTypeValue() {
      return instance.getCallTypeValue();
    }
    /**
     * <code>.server.CallType call_type = 2;</code>
     * @param value The callType to set.
     * @return This builder for chaining.
     */
    public Builder setCallTypeValue(int value) {
      copyOnWrite();
      instance.setCallTypeValue(value);
      return this;
    }
    /**
     * <code>.server.CallType call_type = 2;</code>
     * @return The callType.
     */
    @java.lang.Override
    public com.halloapp.proto.server.CallType getCallType() {
      return instance.getCallType();
    }
    /**
     * <code>.server.CallType call_type = 2;</code>
     * @param value The enum numeric value on the wire for callType to set.
     * @return This builder for chaining.
     */
    public Builder setCallType(com.halloapp.proto.server.CallType value) {
      copyOnWrite();
      instance.setCallType(value);
      return this;
    }
    /**
     * <code>.server.CallType call_type = 2;</code>
     * @return This builder for chaining.
     */
    public Builder clearCallType() {
      copyOnWrite();
      instance.clearCallType();
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

    /**
     * <code>repeated .server.StunServer stun_servers = 4;</code>
     */
    @java.lang.Override
    public java.util.List<com.halloapp.proto.server.StunServer> getStunServersList() {
      return java.util.Collections.unmodifiableList(
          instance.getStunServersList());
    }
    /**
     * <code>repeated .server.StunServer stun_servers = 4;</code>
     */
    @java.lang.Override
    public int getStunServersCount() {
      return instance.getStunServersCount();
    }/**
     * <code>repeated .server.StunServer stun_servers = 4;</code>
     */
    @java.lang.Override
    public com.halloapp.proto.server.StunServer getStunServers(int index) {
      return instance.getStunServers(index);
    }
    /**
     * <code>repeated .server.StunServer stun_servers = 4;</code>
     */
    public Builder setStunServers(
        int index, com.halloapp.proto.server.StunServer value) {
      copyOnWrite();
      instance.setStunServers(index, value);
      return this;
    }
    /**
     * <code>repeated .server.StunServer stun_servers = 4;</code>
     */
    public Builder setStunServers(
        int index, com.halloapp.proto.server.StunServer.Builder builderForValue) {
      copyOnWrite();
      instance.setStunServers(index,
          builderForValue.build());
      return this;
    }
    /**
     * <code>repeated .server.StunServer stun_servers = 4;</code>
     */
    public Builder addStunServers(com.halloapp.proto.server.StunServer value) {
      copyOnWrite();
      instance.addStunServers(value);
      return this;
    }
    /**
     * <code>repeated .server.StunServer stun_servers = 4;</code>
     */
    public Builder addStunServers(
        int index, com.halloapp.proto.server.StunServer value) {
      copyOnWrite();
      instance.addStunServers(index, value);
      return this;
    }
    /**
     * <code>repeated .server.StunServer stun_servers = 4;</code>
     */
    public Builder addStunServers(
        com.halloapp.proto.server.StunServer.Builder builderForValue) {
      copyOnWrite();
      instance.addStunServers(builderForValue.build());
      return this;
    }
    /**
     * <code>repeated .server.StunServer stun_servers = 4;</code>
     */
    public Builder addStunServers(
        int index, com.halloapp.proto.server.StunServer.Builder builderForValue) {
      copyOnWrite();
      instance.addStunServers(index,
          builderForValue.build());
      return this;
    }
    /**
     * <code>repeated .server.StunServer stun_servers = 4;</code>
     */
    public Builder addAllStunServers(
        java.lang.Iterable<? extends com.halloapp.proto.server.StunServer> values) {
      copyOnWrite();
      instance.addAllStunServers(values);
      return this;
    }
    /**
     * <code>repeated .server.StunServer stun_servers = 4;</code>
     */
    public Builder clearStunServers() {
      copyOnWrite();
      instance.clearStunServers();
      return this;
    }
    /**
     * <code>repeated .server.StunServer stun_servers = 4;</code>
     */
    public Builder removeStunServers(int index) {
      copyOnWrite();
      instance.removeStunServers(index);
      return this;
    }

    /**
     * <code>repeated .server.TurnServer turn_servers = 5;</code>
     */
    @java.lang.Override
    public java.util.List<com.halloapp.proto.server.TurnServer> getTurnServersList() {
      return java.util.Collections.unmodifiableList(
          instance.getTurnServersList());
    }
    /**
     * <code>repeated .server.TurnServer turn_servers = 5;</code>
     */
    @java.lang.Override
    public int getTurnServersCount() {
      return instance.getTurnServersCount();
    }/**
     * <code>repeated .server.TurnServer turn_servers = 5;</code>
     */
    @java.lang.Override
    public com.halloapp.proto.server.TurnServer getTurnServers(int index) {
      return instance.getTurnServers(index);
    }
    /**
     * <code>repeated .server.TurnServer turn_servers = 5;</code>
     */
    public Builder setTurnServers(
        int index, com.halloapp.proto.server.TurnServer value) {
      copyOnWrite();
      instance.setTurnServers(index, value);
      return this;
    }
    /**
     * <code>repeated .server.TurnServer turn_servers = 5;</code>
     */
    public Builder setTurnServers(
        int index, com.halloapp.proto.server.TurnServer.Builder builderForValue) {
      copyOnWrite();
      instance.setTurnServers(index,
          builderForValue.build());
      return this;
    }
    /**
     * <code>repeated .server.TurnServer turn_servers = 5;</code>
     */
    public Builder addTurnServers(com.halloapp.proto.server.TurnServer value) {
      copyOnWrite();
      instance.addTurnServers(value);
      return this;
    }
    /**
     * <code>repeated .server.TurnServer turn_servers = 5;</code>
     */
    public Builder addTurnServers(
        int index, com.halloapp.proto.server.TurnServer value) {
      copyOnWrite();
      instance.addTurnServers(index, value);
      return this;
    }
    /**
     * <code>repeated .server.TurnServer turn_servers = 5;</code>
     */
    public Builder addTurnServers(
        com.halloapp.proto.server.TurnServer.Builder builderForValue) {
      copyOnWrite();
      instance.addTurnServers(builderForValue.build());
      return this;
    }
    /**
     * <code>repeated .server.TurnServer turn_servers = 5;</code>
     */
    public Builder addTurnServers(
        int index, com.halloapp.proto.server.TurnServer.Builder builderForValue) {
      copyOnWrite();
      instance.addTurnServers(index,
          builderForValue.build());
      return this;
    }
    /**
     * <code>repeated .server.TurnServer turn_servers = 5;</code>
     */
    public Builder addAllTurnServers(
        java.lang.Iterable<? extends com.halloapp.proto.server.TurnServer> values) {
      copyOnWrite();
      instance.addAllTurnServers(values);
      return this;
    }
    /**
     * <code>repeated .server.TurnServer turn_servers = 5;</code>
     */
    public Builder clearTurnServers() {
      copyOnWrite();
      instance.clearTurnServers();
      return this;
    }
    /**
     * <code>repeated .server.TurnServer turn_servers = 5;</code>
     */
    public Builder removeTurnServers(int index) {
      copyOnWrite();
      instance.removeTurnServers(index);
      return this;
    }

    /**
     * <pre>
     * ts when message is generated by the server
     * </pre>
     *
     * <code>int64 timestamp_ms = 6;</code>
     * @return The timestampMs.
     */
    @java.lang.Override
    public long getTimestampMs() {
      return instance.getTimestampMs();
    }
    /**
     * <pre>
     * ts when message is generated by the server
     * </pre>
     *
     * <code>int64 timestamp_ms = 6;</code>
     * @param value The timestampMs to set.
     * @return This builder for chaining.
     */
    public Builder setTimestampMs(long value) {
      copyOnWrite();
      instance.setTimestampMs(value);
      return this;
    }
    /**
     * <pre>
     * ts when message is generated by the server
     * </pre>
     *
     * <code>int64 timestamp_ms = 6;</code>
     * @return This builder for chaining.
     */
    public Builder clearTimestampMs() {
      copyOnWrite();
      instance.clearTimestampMs();
      return this;
    }

    /**
     * <pre>
     * ts when the message is send to receiver
     * </pre>
     *
     * <code>int64 server_sent_ts_ms = 7;</code>
     * @return The serverSentTsMs.
     */
    @java.lang.Override
    public long getServerSentTsMs() {
      return instance.getServerSentTsMs();
    }
    /**
     * <pre>
     * ts when the message is send to receiver
     * </pre>
     *
     * <code>int64 server_sent_ts_ms = 7;</code>
     * @param value The serverSentTsMs to set.
     * @return This builder for chaining.
     */
    public Builder setServerSentTsMs(long value) {
      copyOnWrite();
      instance.setServerSentTsMs(value);
      return this;
    }
    /**
     * <pre>
     * ts when the message is send to receiver
     * </pre>
     *
     * <code>int64 server_sent_ts_ms = 7;</code>
     * @return This builder for chaining.
     */
    public Builder clearServerSentTsMs() {
      copyOnWrite();
      instance.clearServerSentTsMs();
      return this;
    }

    // @@protoc_insertion_point(builder_scope:server.IncomingCall)
  }
  @java.lang.Override
  @java.lang.SuppressWarnings({"unchecked", "fallthrough"})
  protected final java.lang.Object dynamicMethod(
      com.google.protobuf.GeneratedMessageLite.MethodToInvoke method,
      java.lang.Object arg0, java.lang.Object arg1) {
    switch (method) {
      case NEW_MUTABLE_INSTANCE: {
        return new com.halloapp.proto.server.IncomingCall();
      }
      case NEW_BUILDER: {
        return new Builder();
      }
      case BUILD_MESSAGE_INFO: {
          java.lang.Object[] objects = new java.lang.Object[] {
            "callId_",
            "callType_",
            "webrtcOffer_",
            "stunServers_",
            com.halloapp.proto.server.StunServer.class,
            "turnServers_",
            com.halloapp.proto.server.TurnServer.class,
            "timestampMs_",
            "serverSentTsMs_",
          };
          java.lang.String info =
              "\u0000\u0007\u0000\u0000\u0001\u0007\u0007\u0000\u0002\u0000\u0001\u0208\u0002\f" +
              "\u0003\t\u0004\u001b\u0005\u001b\u0006\u0002\u0007\u0002";
          return newMessageInfo(DEFAULT_INSTANCE, info, objects);
      }
      // fall through
      case GET_DEFAULT_INSTANCE: {
        return DEFAULT_INSTANCE;
      }
      case GET_PARSER: {
        com.google.protobuf.Parser<com.halloapp.proto.server.IncomingCall> parser = PARSER;
        if (parser == null) {
          synchronized (com.halloapp.proto.server.IncomingCall.class) {
            parser = PARSER;
            if (parser == null) {
              parser =
                  new DefaultInstanceBasedParser<com.halloapp.proto.server.IncomingCall>(
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


  // @@protoc_insertion_point(class_scope:server.IncomingCall)
  private static final com.halloapp.proto.server.IncomingCall DEFAULT_INSTANCE;
  static {
    IncomingCall defaultInstance = new IncomingCall();
    // New instances are implicitly immutable so no need to make
    // immutable.
    DEFAULT_INSTANCE = defaultInstance;
    com.google.protobuf.GeneratedMessageLite.registerDefaultInstance(
      IncomingCall.class, defaultInstance);
  }

  public static com.halloapp.proto.server.IncomingCall getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static volatile com.google.protobuf.Parser<IncomingCall> PARSER;

  public static com.google.protobuf.Parser<IncomingCall> parser() {
    return DEFAULT_INSTANCE.getParserForType();
  }
}

