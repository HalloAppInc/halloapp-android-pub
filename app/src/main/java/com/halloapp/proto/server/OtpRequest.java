// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: server.proto

package com.halloapp.proto.server;

/**
 * Protobuf type {@code server.OtpRequest}
 */
public  final class OtpRequest extends
    com.google.protobuf.GeneratedMessageLite<
        OtpRequest, OtpRequest.Builder> implements
    // @@protoc_insertion_point(message_implements:server.OtpRequest)
    OtpRequestOrBuilder {
  private OtpRequest() {
    phone_ = "";
    langId_ = "";
    groupInviteToken_ = "";
    userAgent_ = "";
  }
  /**
   * Protobuf enum {@code server.OtpRequest.Method}
   */
  public enum Method
      implements com.google.protobuf.Internal.EnumLite {
    /**
     * <code>SMS = 0;</code>
     */
    SMS(0),
    /**
     * <code>VOICE_CALL = 1;</code>
     */
    VOICE_CALL(1),
    UNRECOGNIZED(-1),
    ;

    /**
     * <code>SMS = 0;</code>
     */
    public static final int SMS_VALUE = 0;
    /**
     * <code>VOICE_CALL = 1;</code>
     */
    public static final int VOICE_CALL_VALUE = 1;


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
    public static Method valueOf(int value) {
      return forNumber(value);
    }

    public static Method forNumber(int value) {
      switch (value) {
        case 0: return SMS;
        case 1: return VOICE_CALL;
        default: return null;
      }
    }

    public static com.google.protobuf.Internal.EnumLiteMap<Method>
        internalGetValueMap() {
      return internalValueMap;
    }
    private static final com.google.protobuf.Internal.EnumLiteMap<
        Method> internalValueMap =
          new com.google.protobuf.Internal.EnumLiteMap<Method>() {
            @java.lang.Override
            public Method findValueByNumber(int number) {
              return Method.forNumber(number);
            }
          };

    public static com.google.protobuf.Internal.EnumVerifier 
        internalGetVerifier() {
      return MethodVerifier.INSTANCE;
    }

    private static final class MethodVerifier implements 
         com.google.protobuf.Internal.EnumVerifier { 
            static final com.google.protobuf.Internal.EnumVerifier           INSTANCE = new MethodVerifier();
            @java.lang.Override
            public boolean isInRange(int number) {
              return Method.forNumber(number) != null;
            }
          };

    private final int value;

    private Method(int value) {
      this.value = value;
    }

    // @@protoc_insertion_point(enum_scope:server.OtpRequest.Method)
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

  public static final int METHOD_FIELD_NUMBER = 2;
  private int method_;
  /**
   * <code>.server.OtpRequest.Method method = 2;</code>
   * @return The enum numeric value on the wire for method.
   */
  @java.lang.Override
  public int getMethodValue() {
    return method_;
  }
  /**
   * <code>.server.OtpRequest.Method method = 2;</code>
   * @return The method.
   */
  @java.lang.Override
  public com.halloapp.proto.server.OtpRequest.Method getMethod() {
    com.halloapp.proto.server.OtpRequest.Method result = com.halloapp.proto.server.OtpRequest.Method.forNumber(method_);
    return result == null ? com.halloapp.proto.server.OtpRequest.Method.UNRECOGNIZED : result;
  }
  /**
   * <code>.server.OtpRequest.Method method = 2;</code>
   * @param value The enum numeric value on the wire for method to set.
   */
  private void setMethodValue(int value) {
      method_ = value;
  }
  /**
   * <code>.server.OtpRequest.Method method = 2;</code>
   * @param value The method to set.
   */
  private void setMethod(com.halloapp.proto.server.OtpRequest.Method value) {
    method_ = value.getNumber();
    
  }
  /**
   * <code>.server.OtpRequest.Method method = 2;</code>
   */
  private void clearMethod() {
    
    method_ = 0;
  }

  public static final int LANG_ID_FIELD_NUMBER = 3;
  private java.lang.String langId_;
  /**
   * <code>string lang_id = 3;</code>
   * @return The langId.
   */
  @java.lang.Override
  public java.lang.String getLangId() {
    return langId_;
  }
  /**
   * <code>string lang_id = 3;</code>
   * @return The bytes for langId.
   */
  @java.lang.Override
  public com.google.protobuf.ByteString
      getLangIdBytes() {
    return com.google.protobuf.ByteString.copyFromUtf8(langId_);
  }
  /**
   * <code>string lang_id = 3;</code>
   * @param value The langId to set.
   */
  private void setLangId(
      java.lang.String value) {
    value.getClass();
  
    langId_ = value;
  }
  /**
   * <code>string lang_id = 3;</code>
   */
  private void clearLangId() {
    
    langId_ = getDefaultInstance().getLangId();
  }
  /**
   * <code>string lang_id = 3;</code>
   * @param value The bytes for langId to set.
   */
  private void setLangIdBytes(
      com.google.protobuf.ByteString value) {
    checkByteStringIsUtf8(value);
    langId_ = value.toStringUtf8();
    
  }

  public static final int GROUP_INVITE_TOKEN_FIELD_NUMBER = 4;
  private java.lang.String groupInviteToken_;
  /**
   * <code>string group_invite_token = 4;</code>
   * @return The groupInviteToken.
   */
  @java.lang.Override
  public java.lang.String getGroupInviteToken() {
    return groupInviteToken_;
  }
  /**
   * <code>string group_invite_token = 4;</code>
   * @return The bytes for groupInviteToken.
   */
  @java.lang.Override
  public com.google.protobuf.ByteString
      getGroupInviteTokenBytes() {
    return com.google.protobuf.ByteString.copyFromUtf8(groupInviteToken_);
  }
  /**
   * <code>string group_invite_token = 4;</code>
   * @param value The groupInviteToken to set.
   */
  private void setGroupInviteToken(
      java.lang.String value) {
    value.getClass();
  
    groupInviteToken_ = value;
  }
  /**
   * <code>string group_invite_token = 4;</code>
   */
  private void clearGroupInviteToken() {
    
    groupInviteToken_ = getDefaultInstance().getGroupInviteToken();
  }
  /**
   * <code>string group_invite_token = 4;</code>
   * @param value The bytes for groupInviteToken to set.
   */
  private void setGroupInviteTokenBytes(
      com.google.protobuf.ByteString value) {
    checkByteStringIsUtf8(value);
    groupInviteToken_ = value.toStringUtf8();
    
  }

  public static final int USER_AGENT_FIELD_NUMBER = 5;
  private java.lang.String userAgent_;
  /**
   * <code>string user_agent = 5;</code>
   * @return The userAgent.
   */
  @java.lang.Override
  public java.lang.String getUserAgent() {
    return userAgent_;
  }
  /**
   * <code>string user_agent = 5;</code>
   * @return The bytes for userAgent.
   */
  @java.lang.Override
  public com.google.protobuf.ByteString
      getUserAgentBytes() {
    return com.google.protobuf.ByteString.copyFromUtf8(userAgent_);
  }
  /**
   * <code>string user_agent = 5;</code>
   * @param value The userAgent to set.
   */
  private void setUserAgent(
      java.lang.String value) {
    value.getClass();
  
    userAgent_ = value;
  }
  /**
   * <code>string user_agent = 5;</code>
   */
  private void clearUserAgent() {
    
    userAgent_ = getDefaultInstance().getUserAgent();
  }
  /**
   * <code>string user_agent = 5;</code>
   * @param value The bytes for userAgent to set.
   */
  private void setUserAgentBytes(
      com.google.protobuf.ByteString value) {
    checkByteStringIsUtf8(value);
    userAgent_ = value.toStringUtf8();
    
  }

  public static com.halloapp.proto.server.OtpRequest parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static com.halloapp.proto.server.OtpRequest parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static com.halloapp.proto.server.OtpRequest parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static com.halloapp.proto.server.OtpRequest parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static com.halloapp.proto.server.OtpRequest parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static com.halloapp.proto.server.OtpRequest parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static com.halloapp.proto.server.OtpRequest parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input);
  }
  public static com.halloapp.proto.server.OtpRequest parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input, extensionRegistry);
  }
  public static com.halloapp.proto.server.OtpRequest parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return parseDelimitedFrom(DEFAULT_INSTANCE, input);
  }
  public static com.halloapp.proto.server.OtpRequest parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return parseDelimitedFrom(DEFAULT_INSTANCE, input, extensionRegistry);
  }
  public static com.halloapp.proto.server.OtpRequest parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input);
  }
  public static com.halloapp.proto.server.OtpRequest parseFrom(
      com.google.protobuf.CodedInputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input, extensionRegistry);
  }

  public static Builder newBuilder() {
    return (Builder) DEFAULT_INSTANCE.createBuilder();
  }
  public static Builder newBuilder(com.halloapp.proto.server.OtpRequest prototype) {
    return (Builder) DEFAULT_INSTANCE.createBuilder(prototype);
  }

  /**
   * Protobuf type {@code server.OtpRequest}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessageLite.Builder<
        com.halloapp.proto.server.OtpRequest, Builder> implements
      // @@protoc_insertion_point(builder_implements:server.OtpRequest)
      com.halloapp.proto.server.OtpRequestOrBuilder {
    // Construct using com.halloapp.proto.server.OtpRequest.newBuilder()
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
     * <code>.server.OtpRequest.Method method = 2;</code>
     * @return The enum numeric value on the wire for method.
     */
    @java.lang.Override
    public int getMethodValue() {
      return instance.getMethodValue();
    }
    /**
     * <code>.server.OtpRequest.Method method = 2;</code>
     * @param value The method to set.
     * @return This builder for chaining.
     */
    public Builder setMethodValue(int value) {
      copyOnWrite();
      instance.setMethodValue(value);
      return this;
    }
    /**
     * <code>.server.OtpRequest.Method method = 2;</code>
     * @return The method.
     */
    @java.lang.Override
    public com.halloapp.proto.server.OtpRequest.Method getMethod() {
      return instance.getMethod();
    }
    /**
     * <code>.server.OtpRequest.Method method = 2;</code>
     * @param value The enum numeric value on the wire for method to set.
     * @return This builder for chaining.
     */
    public Builder setMethod(com.halloapp.proto.server.OtpRequest.Method value) {
      copyOnWrite();
      instance.setMethod(value);
      return this;
    }
    /**
     * <code>.server.OtpRequest.Method method = 2;</code>
     * @return This builder for chaining.
     */
    public Builder clearMethod() {
      copyOnWrite();
      instance.clearMethod();
      return this;
    }

    /**
     * <code>string lang_id = 3;</code>
     * @return The langId.
     */
    @java.lang.Override
    public java.lang.String getLangId() {
      return instance.getLangId();
    }
    /**
     * <code>string lang_id = 3;</code>
     * @return The bytes for langId.
     */
    @java.lang.Override
    public com.google.protobuf.ByteString
        getLangIdBytes() {
      return instance.getLangIdBytes();
    }
    /**
     * <code>string lang_id = 3;</code>
     * @param value The langId to set.
     * @return This builder for chaining.
     */
    public Builder setLangId(
        java.lang.String value) {
      copyOnWrite();
      instance.setLangId(value);
      return this;
    }
    /**
     * <code>string lang_id = 3;</code>
     * @return This builder for chaining.
     */
    public Builder clearLangId() {
      copyOnWrite();
      instance.clearLangId();
      return this;
    }
    /**
     * <code>string lang_id = 3;</code>
     * @param value The bytes for langId to set.
     * @return This builder for chaining.
     */
    public Builder setLangIdBytes(
        com.google.protobuf.ByteString value) {
      copyOnWrite();
      instance.setLangIdBytes(value);
      return this;
    }

    /**
     * <code>string group_invite_token = 4;</code>
     * @return The groupInviteToken.
     */
    @java.lang.Override
    public java.lang.String getGroupInviteToken() {
      return instance.getGroupInviteToken();
    }
    /**
     * <code>string group_invite_token = 4;</code>
     * @return The bytes for groupInviteToken.
     */
    @java.lang.Override
    public com.google.protobuf.ByteString
        getGroupInviteTokenBytes() {
      return instance.getGroupInviteTokenBytes();
    }
    /**
     * <code>string group_invite_token = 4;</code>
     * @param value The groupInviteToken to set.
     * @return This builder for chaining.
     */
    public Builder setGroupInviteToken(
        java.lang.String value) {
      copyOnWrite();
      instance.setGroupInviteToken(value);
      return this;
    }
    /**
     * <code>string group_invite_token = 4;</code>
     * @return This builder for chaining.
     */
    public Builder clearGroupInviteToken() {
      copyOnWrite();
      instance.clearGroupInviteToken();
      return this;
    }
    /**
     * <code>string group_invite_token = 4;</code>
     * @param value The bytes for groupInviteToken to set.
     * @return This builder for chaining.
     */
    public Builder setGroupInviteTokenBytes(
        com.google.protobuf.ByteString value) {
      copyOnWrite();
      instance.setGroupInviteTokenBytes(value);
      return this;
    }

    /**
     * <code>string user_agent = 5;</code>
     * @return The userAgent.
     */
    @java.lang.Override
    public java.lang.String getUserAgent() {
      return instance.getUserAgent();
    }
    /**
     * <code>string user_agent = 5;</code>
     * @return The bytes for userAgent.
     */
    @java.lang.Override
    public com.google.protobuf.ByteString
        getUserAgentBytes() {
      return instance.getUserAgentBytes();
    }
    /**
     * <code>string user_agent = 5;</code>
     * @param value The userAgent to set.
     * @return This builder for chaining.
     */
    public Builder setUserAgent(
        java.lang.String value) {
      copyOnWrite();
      instance.setUserAgent(value);
      return this;
    }
    /**
     * <code>string user_agent = 5;</code>
     * @return This builder for chaining.
     */
    public Builder clearUserAgent() {
      copyOnWrite();
      instance.clearUserAgent();
      return this;
    }
    /**
     * <code>string user_agent = 5;</code>
     * @param value The bytes for userAgent to set.
     * @return This builder for chaining.
     */
    public Builder setUserAgentBytes(
        com.google.protobuf.ByteString value) {
      copyOnWrite();
      instance.setUserAgentBytes(value);
      return this;
    }

    // @@protoc_insertion_point(builder_scope:server.OtpRequest)
  }
  @java.lang.Override
  @java.lang.SuppressWarnings({"unchecked", "fallthrough"})
  protected final java.lang.Object dynamicMethod(
      com.google.protobuf.GeneratedMessageLite.MethodToInvoke method,
      java.lang.Object arg0, java.lang.Object arg1) {
    switch (method) {
      case NEW_MUTABLE_INSTANCE: {
        return new com.halloapp.proto.server.OtpRequest();
      }
      case NEW_BUILDER: {
        return new Builder();
      }
      case BUILD_MESSAGE_INFO: {
          java.lang.Object[] objects = new java.lang.Object[] {
            "phone_",
            "method_",
            "langId_",
            "groupInviteToken_",
            "userAgent_",
          };
          java.lang.String info =
              "\u0000\u0005\u0000\u0000\u0001\u0005\u0005\u0000\u0000\u0000\u0001\u0208\u0002\f" +
              "\u0003\u0208\u0004\u0208\u0005\u0208";
          return newMessageInfo(DEFAULT_INSTANCE, info, objects);
      }
      // fall through
      case GET_DEFAULT_INSTANCE: {
        return DEFAULT_INSTANCE;
      }
      case GET_PARSER: {
        com.google.protobuf.Parser<com.halloapp.proto.server.OtpRequest> parser = PARSER;
        if (parser == null) {
          synchronized (com.halloapp.proto.server.OtpRequest.class) {
            parser = PARSER;
            if (parser == null) {
              parser =
                  new DefaultInstanceBasedParser<com.halloapp.proto.server.OtpRequest>(
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


  // @@protoc_insertion_point(class_scope:server.OtpRequest)
  private static final com.halloapp.proto.server.OtpRequest DEFAULT_INSTANCE;
  static {
    OtpRequest defaultInstance = new OtpRequest();
    // New instances are implicitly immutable so no need to make
    // immutable.
    DEFAULT_INSTANCE = defaultInstance;
    com.google.protobuf.GeneratedMessageLite.registerDefaultInstance(
      OtpRequest.class, defaultInstance);
  }

  public static com.halloapp.proto.server.OtpRequest getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static volatile com.google.protobuf.Parser<OtpRequest> PARSER;

  public static com.google.protobuf.Parser<OtpRequest> parser() {
    return DEFAULT_INSTANCE.getParserForType();
  }
}
