// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: log_events.proto

package com.halloapp.proto.log_events;

/**
 * Protobuf type {@code server.InviteRequestResult}
 */
public  final class InviteRequestResult extends
    com.google.protobuf.GeneratedMessageLite<
        InviteRequestResult, InviteRequestResult.Builder> implements
    // @@protoc_insertion_point(message_implements:server.InviteRequestResult)
    InviteRequestResultOrBuilder {
  private InviteRequestResult() {
    invitedPhone_ = "";
  }
  /**
   * Protobuf enum {@code server.InviteRequestResult.Type}
   */
  public enum Type
      implements com.google.protobuf.Internal.EnumLite {
    /**
     * <code>UNKNOWN = 0;</code>
     */
    UNKNOWN(0),
    /**
     * <code>CANCELLED = 1;</code>
     */
    CANCELLED(1),
    /**
     * <code>SENT = 2;</code>
     */
    SENT(2),
    /**
     * <code>FAILED = 3;</code>
     */
    FAILED(3),
    UNRECOGNIZED(-1),
    ;

    /**
     * <code>UNKNOWN = 0;</code>
     */
    public static final int UNKNOWN_VALUE = 0;
    /**
     * <code>CANCELLED = 1;</code>
     */
    public static final int CANCELLED_VALUE = 1;
    /**
     * <code>SENT = 2;</code>
     */
    public static final int SENT_VALUE = 2;
    /**
     * <code>FAILED = 3;</code>
     */
    public static final int FAILED_VALUE = 3;


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
    public static Type valueOf(int value) {
      return forNumber(value);
    }

    public static Type forNumber(int value) {
      switch (value) {
        case 0: return UNKNOWN;
        case 1: return CANCELLED;
        case 2: return SENT;
        case 3: return FAILED;
        default: return null;
      }
    }

    public static com.google.protobuf.Internal.EnumLiteMap<Type>
        internalGetValueMap() {
      return internalValueMap;
    }
    private static final com.google.protobuf.Internal.EnumLiteMap<
        Type> internalValueMap =
          new com.google.protobuf.Internal.EnumLiteMap<Type>() {
            @java.lang.Override
            public Type findValueByNumber(int number) {
              return Type.forNumber(number);
            }
          };

    public static com.google.protobuf.Internal.EnumVerifier 
        internalGetVerifier() {
      return TypeVerifier.INSTANCE;
    }

    private static final class TypeVerifier implements 
         com.google.protobuf.Internal.EnumVerifier { 
            static final com.google.protobuf.Internal.EnumVerifier           INSTANCE = new TypeVerifier();
            @java.lang.Override
            public boolean isInRange(int number) {
              return Type.forNumber(number) != null;
            }
          };

    private final int value;

    private Type(int value) {
      this.value = value;
    }

    // @@protoc_insertion_point(enum_scope:server.InviteRequestResult.Type)
  }

  public static final int TYPE_FIELD_NUMBER = 1;
  private int type_;
  /**
   * <code>.server.InviteRequestResult.Type type = 1;</code>
   * @return The enum numeric value on the wire for type.
   */
  @java.lang.Override
  public int getTypeValue() {
    return type_;
  }
  /**
   * <code>.server.InviteRequestResult.Type type = 1;</code>
   * @return The type.
   */
  @java.lang.Override
  public com.halloapp.proto.log_events.InviteRequestResult.Type getType() {
    com.halloapp.proto.log_events.InviteRequestResult.Type result = com.halloapp.proto.log_events.InviteRequestResult.Type.forNumber(type_);
    return result == null ? com.halloapp.proto.log_events.InviteRequestResult.Type.UNRECOGNIZED : result;
  }
  /**
   * <code>.server.InviteRequestResult.Type type = 1;</code>
   * @param value The enum numeric value on the wire for type to set.
   */
  private void setTypeValue(int value) {
      type_ = value;
  }
  /**
   * <code>.server.InviteRequestResult.Type type = 1;</code>
   * @param value The type to set.
   */
  private void setType(com.halloapp.proto.log_events.InviteRequestResult.Type value) {
    type_ = value.getNumber();
    
  }
  /**
   * <code>.server.InviteRequestResult.Type type = 1;</code>
   */
  private void clearType() {
    
    type_ = 0;
  }

  public static final int INVITED_PHONE_FIELD_NUMBER = 2;
  private java.lang.String invitedPhone_;
  /**
   * <code>string invited_phone = 2;</code>
   * @return The invitedPhone.
   */
  @java.lang.Override
  public java.lang.String getInvitedPhone() {
    return invitedPhone_;
  }
  /**
   * <code>string invited_phone = 2;</code>
   * @return The bytes for invitedPhone.
   */
  @java.lang.Override
  public com.google.protobuf.ByteString
      getInvitedPhoneBytes() {
    return com.google.protobuf.ByteString.copyFromUtf8(invitedPhone_);
  }
  /**
   * <code>string invited_phone = 2;</code>
   * @param value The invitedPhone to set.
   */
  private void setInvitedPhone(
      java.lang.String value) {
    value.getClass();
  
    invitedPhone_ = value;
  }
  /**
   * <code>string invited_phone = 2;</code>
   */
  private void clearInvitedPhone() {
    
    invitedPhone_ = getDefaultInstance().getInvitedPhone();
  }
  /**
   * <code>string invited_phone = 2;</code>
   * @param value The bytes for invitedPhone to set.
   */
  private void setInvitedPhoneBytes(
      com.google.protobuf.ByteString value) {
    checkByteStringIsUtf8(value);
    invitedPhone_ = value.toStringUtf8();
    
  }

  public static com.halloapp.proto.log_events.InviteRequestResult parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static com.halloapp.proto.log_events.InviteRequestResult parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static com.halloapp.proto.log_events.InviteRequestResult parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static com.halloapp.proto.log_events.InviteRequestResult parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static com.halloapp.proto.log_events.InviteRequestResult parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static com.halloapp.proto.log_events.InviteRequestResult parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static com.halloapp.proto.log_events.InviteRequestResult parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input);
  }
  public static com.halloapp.proto.log_events.InviteRequestResult parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input, extensionRegistry);
  }
  public static com.halloapp.proto.log_events.InviteRequestResult parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return parseDelimitedFrom(DEFAULT_INSTANCE, input);
  }
  public static com.halloapp.proto.log_events.InviteRequestResult parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return parseDelimitedFrom(DEFAULT_INSTANCE, input, extensionRegistry);
  }
  public static com.halloapp.proto.log_events.InviteRequestResult parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input);
  }
  public static com.halloapp.proto.log_events.InviteRequestResult parseFrom(
      com.google.protobuf.CodedInputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input, extensionRegistry);
  }

  public static Builder newBuilder() {
    return (Builder) DEFAULT_INSTANCE.createBuilder();
  }
  public static Builder newBuilder(com.halloapp.proto.log_events.InviteRequestResult prototype) {
    return (Builder) DEFAULT_INSTANCE.createBuilder(prototype);
  }

  /**
   * Protobuf type {@code server.InviteRequestResult}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessageLite.Builder<
        com.halloapp.proto.log_events.InviteRequestResult, Builder> implements
      // @@protoc_insertion_point(builder_implements:server.InviteRequestResult)
      com.halloapp.proto.log_events.InviteRequestResultOrBuilder {
    // Construct using com.halloapp.proto.log_events.InviteRequestResult.newBuilder()
    private Builder() {
      super(DEFAULT_INSTANCE);
    }


    /**
     * <code>.server.InviteRequestResult.Type type = 1;</code>
     * @return The enum numeric value on the wire for type.
     */
    @java.lang.Override
    public int getTypeValue() {
      return instance.getTypeValue();
    }
    /**
     * <code>.server.InviteRequestResult.Type type = 1;</code>
     * @param value The type to set.
     * @return This builder for chaining.
     */
    public Builder setTypeValue(int value) {
      copyOnWrite();
      instance.setTypeValue(value);
      return this;
    }
    /**
     * <code>.server.InviteRequestResult.Type type = 1;</code>
     * @return The type.
     */
    @java.lang.Override
    public com.halloapp.proto.log_events.InviteRequestResult.Type getType() {
      return instance.getType();
    }
    /**
     * <code>.server.InviteRequestResult.Type type = 1;</code>
     * @param value The enum numeric value on the wire for type to set.
     * @return This builder for chaining.
     */
    public Builder setType(com.halloapp.proto.log_events.InviteRequestResult.Type value) {
      copyOnWrite();
      instance.setType(value);
      return this;
    }
    /**
     * <code>.server.InviteRequestResult.Type type = 1;</code>
     * @return This builder for chaining.
     */
    public Builder clearType() {
      copyOnWrite();
      instance.clearType();
      return this;
    }

    /**
     * <code>string invited_phone = 2;</code>
     * @return The invitedPhone.
     */
    @java.lang.Override
    public java.lang.String getInvitedPhone() {
      return instance.getInvitedPhone();
    }
    /**
     * <code>string invited_phone = 2;</code>
     * @return The bytes for invitedPhone.
     */
    @java.lang.Override
    public com.google.protobuf.ByteString
        getInvitedPhoneBytes() {
      return instance.getInvitedPhoneBytes();
    }
    /**
     * <code>string invited_phone = 2;</code>
     * @param value The invitedPhone to set.
     * @return This builder for chaining.
     */
    public Builder setInvitedPhone(
        java.lang.String value) {
      copyOnWrite();
      instance.setInvitedPhone(value);
      return this;
    }
    /**
     * <code>string invited_phone = 2;</code>
     * @return This builder for chaining.
     */
    public Builder clearInvitedPhone() {
      copyOnWrite();
      instance.clearInvitedPhone();
      return this;
    }
    /**
     * <code>string invited_phone = 2;</code>
     * @param value The bytes for invitedPhone to set.
     * @return This builder for chaining.
     */
    public Builder setInvitedPhoneBytes(
        com.google.protobuf.ByteString value) {
      copyOnWrite();
      instance.setInvitedPhoneBytes(value);
      return this;
    }

    // @@protoc_insertion_point(builder_scope:server.InviteRequestResult)
  }
  @java.lang.Override
  @java.lang.SuppressWarnings({"unchecked", "fallthrough"})
  protected final java.lang.Object dynamicMethod(
      com.google.protobuf.GeneratedMessageLite.MethodToInvoke method,
      java.lang.Object arg0, java.lang.Object arg1) {
    switch (method) {
      case NEW_MUTABLE_INSTANCE: {
        return new com.halloapp.proto.log_events.InviteRequestResult();
      }
      case NEW_BUILDER: {
        return new Builder();
      }
      case BUILD_MESSAGE_INFO: {
          java.lang.Object[] objects = new java.lang.Object[] {
            "type_",
            "invitedPhone_",
          };
          java.lang.String info =
              "\u0000\u0002\u0000\u0000\u0001\u0002\u0002\u0000\u0000\u0000\u0001\f\u0002\u0208" +
              "";
          return newMessageInfo(DEFAULT_INSTANCE, info, objects);
      }
      // fall through
      case GET_DEFAULT_INSTANCE: {
        return DEFAULT_INSTANCE;
      }
      case GET_PARSER: {
        com.google.protobuf.Parser<com.halloapp.proto.log_events.InviteRequestResult> parser = PARSER;
        if (parser == null) {
          synchronized (com.halloapp.proto.log_events.InviteRequestResult.class) {
            parser = PARSER;
            if (parser == null) {
              parser =
                  new DefaultInstanceBasedParser<com.halloapp.proto.log_events.InviteRequestResult>(
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


  // @@protoc_insertion_point(class_scope:server.InviteRequestResult)
  private static final com.halloapp.proto.log_events.InviteRequestResult DEFAULT_INSTANCE;
  static {
    InviteRequestResult defaultInstance = new InviteRequestResult();
    // New instances are implicitly immutable so no need to make
    // immutable.
    DEFAULT_INSTANCE = defaultInstance;
    com.google.protobuf.GeneratedMessageLite.registerDefaultInstance(
      InviteRequestResult.class, defaultInstance);
  }

  public static com.halloapp.proto.log_events.InviteRequestResult getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static volatile com.google.protobuf.Parser<InviteRequestResult> PARSER;

  public static com.google.protobuf.Parser<InviteRequestResult> parser() {
    return DEFAULT_INSTANCE.getParserForType();
  }
}

