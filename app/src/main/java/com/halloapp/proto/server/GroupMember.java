// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: server.proto

package com.halloapp.proto.server;

/**
 * Protobuf type {@code server.GroupMember}
 */
public  final class GroupMember extends
    com.google.protobuf.GeneratedMessageLite<
        GroupMember, GroupMember.Builder> implements
    // @@protoc_insertion_point(message_implements:server.GroupMember)
    GroupMemberOrBuilder {
  private GroupMember() {
    name_ = "";
    avatarId_ = "";
    result_ = "";
    reason_ = "";
    identityKey_ = com.google.protobuf.ByteString.EMPTY;
  }
  /**
   * Protobuf enum {@code server.GroupMember.Action}
   */
  public enum Action
      implements com.google.protobuf.Internal.EnumLite {
    /**
     * <code>ADD = 0;</code>
     */
    ADD(0),
    /**
     * <code>REMOVE = 1;</code>
     */
    REMOVE(1),
    /**
     * <code>PROMOTE = 2;</code>
     */
    PROMOTE(2),
    /**
     * <code>DEMOTE = 3;</code>
     */
    DEMOTE(3),
    /**
     * <code>LEAVE = 4;</code>
     */
    LEAVE(4),
    /**
     * <code>JOIN = 5;</code>
     */
    JOIN(5),
    UNRECOGNIZED(-1),
    ;

    /**
     * <code>ADD = 0;</code>
     */
    public static final int ADD_VALUE = 0;
    /**
     * <code>REMOVE = 1;</code>
     */
    public static final int REMOVE_VALUE = 1;
    /**
     * <code>PROMOTE = 2;</code>
     */
    public static final int PROMOTE_VALUE = 2;
    /**
     * <code>DEMOTE = 3;</code>
     */
    public static final int DEMOTE_VALUE = 3;
    /**
     * <code>LEAVE = 4;</code>
     */
    public static final int LEAVE_VALUE = 4;
    /**
     * <code>JOIN = 5;</code>
     */
    public static final int JOIN_VALUE = 5;


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
        case 0: return ADD;
        case 1: return REMOVE;
        case 2: return PROMOTE;
        case 3: return DEMOTE;
        case 4: return LEAVE;
        case 5: return JOIN;
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

    // @@protoc_insertion_point(enum_scope:server.GroupMember.Action)
  }

  /**
   * Protobuf enum {@code server.GroupMember.Type}
   */
  public enum Type
      implements com.google.protobuf.Internal.EnumLite {
    /**
     * <code>MEMBER = 0;</code>
     */
    MEMBER(0),
    /**
     * <code>ADMIN = 1;</code>
     */
    ADMIN(1),
    UNRECOGNIZED(-1),
    ;

    /**
     * <code>MEMBER = 0;</code>
     */
    public static final int MEMBER_VALUE = 0;
    /**
     * <code>ADMIN = 1;</code>
     */
    public static final int ADMIN_VALUE = 1;


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
        case 0: return MEMBER;
        case 1: return ADMIN;
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

    // @@protoc_insertion_point(enum_scope:server.GroupMember.Type)
  }

  public static final int ACTION_FIELD_NUMBER = 1;
  private int action_;
  /**
   * <code>.server.GroupMember.Action action = 1;</code>
   * @return The enum numeric value on the wire for action.
   */
  @java.lang.Override
  public int getActionValue() {
    return action_;
  }
  /**
   * <code>.server.GroupMember.Action action = 1;</code>
   * @return The action.
   */
  @java.lang.Override
  public com.halloapp.proto.server.GroupMember.Action getAction() {
    com.halloapp.proto.server.GroupMember.Action result = com.halloapp.proto.server.GroupMember.Action.forNumber(action_);
    return result == null ? com.halloapp.proto.server.GroupMember.Action.UNRECOGNIZED : result;
  }
  /**
   * <code>.server.GroupMember.Action action = 1;</code>
   * @param value The enum numeric value on the wire for action to set.
   */
  private void setActionValue(int value) {
      action_ = value;
  }
  /**
   * <code>.server.GroupMember.Action action = 1;</code>
   * @param value The action to set.
   */
  private void setAction(com.halloapp.proto.server.GroupMember.Action value) {
    action_ = value.getNumber();
    
  }
  /**
   * <code>.server.GroupMember.Action action = 1;</code>
   */
  private void clearAction() {
    
    action_ = 0;
  }

  public static final int UID_FIELD_NUMBER = 2;
  private long uid_;
  /**
   * <code>int64 uid = 2;</code>
   * @return The uid.
   */
  @java.lang.Override
  public long getUid() {
    return uid_;
  }
  /**
   * <code>int64 uid = 2;</code>
   * @param value The uid to set.
   */
  private void setUid(long value) {
    
    uid_ = value;
  }
  /**
   * <code>int64 uid = 2;</code>
   */
  private void clearUid() {
    
    uid_ = 0L;
  }

  public static final int TYPE_FIELD_NUMBER = 3;
  private int type_;
  /**
   * <code>.server.GroupMember.Type type = 3;</code>
   * @return The enum numeric value on the wire for type.
   */
  @java.lang.Override
  public int getTypeValue() {
    return type_;
  }
  /**
   * <code>.server.GroupMember.Type type = 3;</code>
   * @return The type.
   */
  @java.lang.Override
  public com.halloapp.proto.server.GroupMember.Type getType() {
    com.halloapp.proto.server.GroupMember.Type result = com.halloapp.proto.server.GroupMember.Type.forNumber(type_);
    return result == null ? com.halloapp.proto.server.GroupMember.Type.UNRECOGNIZED : result;
  }
  /**
   * <code>.server.GroupMember.Type type = 3;</code>
   * @param value The enum numeric value on the wire for type to set.
   */
  private void setTypeValue(int value) {
      type_ = value;
  }
  /**
   * <code>.server.GroupMember.Type type = 3;</code>
   * @param value The type to set.
   */
  private void setType(com.halloapp.proto.server.GroupMember.Type value) {
    type_ = value.getNumber();
    
  }
  /**
   * <code>.server.GroupMember.Type type = 3;</code>
   */
  private void clearType() {
    
    type_ = 0;
  }

  public static final int NAME_FIELD_NUMBER = 4;
  private java.lang.String name_;
  /**
   * <code>string name = 4;</code>
   * @return The name.
   */
  @java.lang.Override
  public java.lang.String getName() {
    return name_;
  }
  /**
   * <code>string name = 4;</code>
   * @return The bytes for name.
   */
  @java.lang.Override
  public com.google.protobuf.ByteString
      getNameBytes() {
    return com.google.protobuf.ByteString.copyFromUtf8(name_);
  }
  /**
   * <code>string name = 4;</code>
   * @param value The name to set.
   */
  private void setName(
      java.lang.String value) {
    java.lang.Class<?> valueClass = value.getClass();
  
    name_ = value;
  }
  /**
   * <code>string name = 4;</code>
   */
  private void clearName() {
    
    name_ = getDefaultInstance().getName();
  }
  /**
   * <code>string name = 4;</code>
   * @param value The bytes for name to set.
   */
  private void setNameBytes(
      com.google.protobuf.ByteString value) {
    checkByteStringIsUtf8(value);
    name_ = value.toStringUtf8();
    
  }

  public static final int AVATAR_ID_FIELD_NUMBER = 5;
  private java.lang.String avatarId_;
  /**
   * <code>string avatar_id = 5;</code>
   * @return The avatarId.
   */
  @java.lang.Override
  public java.lang.String getAvatarId() {
    return avatarId_;
  }
  /**
   * <code>string avatar_id = 5;</code>
   * @return The bytes for avatarId.
   */
  @java.lang.Override
  public com.google.protobuf.ByteString
      getAvatarIdBytes() {
    return com.google.protobuf.ByteString.copyFromUtf8(avatarId_);
  }
  /**
   * <code>string avatar_id = 5;</code>
   * @param value The avatarId to set.
   */
  private void setAvatarId(
      java.lang.String value) {
    java.lang.Class<?> valueClass = value.getClass();
  
    avatarId_ = value;
  }
  /**
   * <code>string avatar_id = 5;</code>
   */
  private void clearAvatarId() {
    
    avatarId_ = getDefaultInstance().getAvatarId();
  }
  /**
   * <code>string avatar_id = 5;</code>
   * @param value The bytes for avatarId to set.
   */
  private void setAvatarIdBytes(
      com.google.protobuf.ByteString value) {
    checkByteStringIsUtf8(value);
    avatarId_ = value.toStringUtf8();
    
  }

  public static final int RESULT_FIELD_NUMBER = 6;
  private java.lang.String result_;
  /**
   * <code>string result = 6;</code>
   * @return The result.
   */
  @java.lang.Override
  public java.lang.String getResult() {
    return result_;
  }
  /**
   * <code>string result = 6;</code>
   * @return The bytes for result.
   */
  @java.lang.Override
  public com.google.protobuf.ByteString
      getResultBytes() {
    return com.google.protobuf.ByteString.copyFromUtf8(result_);
  }
  /**
   * <code>string result = 6;</code>
   * @param value The result to set.
   */
  private void setResult(
      java.lang.String value) {
    java.lang.Class<?> valueClass = value.getClass();
  
    result_ = value;
  }
  /**
   * <code>string result = 6;</code>
   */
  private void clearResult() {
    
    result_ = getDefaultInstance().getResult();
  }
  /**
   * <code>string result = 6;</code>
   * @param value The bytes for result to set.
   */
  private void setResultBytes(
      com.google.protobuf.ByteString value) {
    checkByteStringIsUtf8(value);
    result_ = value.toStringUtf8();
    
  }

  public static final int REASON_FIELD_NUMBER = 7;
  private java.lang.String reason_;
  /**
   * <code>string reason = 7;</code>
   * @return The reason.
   */
  @java.lang.Override
  public java.lang.String getReason() {
    return reason_;
  }
  /**
   * <code>string reason = 7;</code>
   * @return The bytes for reason.
   */
  @java.lang.Override
  public com.google.protobuf.ByteString
      getReasonBytes() {
    return com.google.protobuf.ByteString.copyFromUtf8(reason_);
  }
  /**
   * <code>string reason = 7;</code>
   * @param value The reason to set.
   */
  private void setReason(
      java.lang.String value) {
    java.lang.Class<?> valueClass = value.getClass();
  
    reason_ = value;
  }
  /**
   * <code>string reason = 7;</code>
   */
  private void clearReason() {
    
    reason_ = getDefaultInstance().getReason();
  }
  /**
   * <code>string reason = 7;</code>
   * @param value The bytes for reason to set.
   */
  private void setReasonBytes(
      com.google.protobuf.ByteString value) {
    checkByteStringIsUtf8(value);
    reason_ = value.toStringUtf8();
    
  }

  public static final int IDENTITY_KEY_FIELD_NUMBER = 8;
  private com.google.protobuf.ByteString identityKey_;
  /**
   * <pre>
   * Identity key to be returned on `GET_MEMBER_IDENTITY_KEYS` IQ.
   * </pre>
   *
   * <code>bytes identity_key = 8;</code>
   * @return The identityKey.
   */
  @java.lang.Override
  public com.google.protobuf.ByteString getIdentityKey() {
    return identityKey_;
  }
  /**
   * <pre>
   * Identity key to be returned on `GET_MEMBER_IDENTITY_KEYS` IQ.
   * </pre>
   *
   * <code>bytes identity_key = 8;</code>
   * @param value The identityKey to set.
   */
  private void setIdentityKey(com.google.protobuf.ByteString value) {
    java.lang.Class<?> valueClass = value.getClass();
  
    identityKey_ = value;
  }
  /**
   * <pre>
   * Identity key to be returned on `GET_MEMBER_IDENTITY_KEYS` IQ.
   * </pre>
   *
   * <code>bytes identity_key = 8;</code>
   */
  private void clearIdentityKey() {
    
    identityKey_ = getDefaultInstance().getIdentityKey();
  }

  public static com.halloapp.proto.server.GroupMember parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static com.halloapp.proto.server.GroupMember parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static com.halloapp.proto.server.GroupMember parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static com.halloapp.proto.server.GroupMember parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static com.halloapp.proto.server.GroupMember parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static com.halloapp.proto.server.GroupMember parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static com.halloapp.proto.server.GroupMember parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input);
  }
  public static com.halloapp.proto.server.GroupMember parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input, extensionRegistry);
  }
  public static com.halloapp.proto.server.GroupMember parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return parseDelimitedFrom(DEFAULT_INSTANCE, input);
  }
  public static com.halloapp.proto.server.GroupMember parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return parseDelimitedFrom(DEFAULT_INSTANCE, input, extensionRegistry);
  }
  public static com.halloapp.proto.server.GroupMember parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input);
  }
  public static com.halloapp.proto.server.GroupMember parseFrom(
      com.google.protobuf.CodedInputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input, extensionRegistry);
  }

  public static Builder newBuilder() {
    return (Builder) DEFAULT_INSTANCE.createBuilder();
  }
  public static Builder newBuilder(com.halloapp.proto.server.GroupMember prototype) {
    return (Builder) DEFAULT_INSTANCE.createBuilder(prototype);
  }

  /**
   * Protobuf type {@code server.GroupMember}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessageLite.Builder<
        com.halloapp.proto.server.GroupMember, Builder> implements
      // @@protoc_insertion_point(builder_implements:server.GroupMember)
      com.halloapp.proto.server.GroupMemberOrBuilder {
    // Construct using com.halloapp.proto.server.GroupMember.newBuilder()
    private Builder() {
      super(DEFAULT_INSTANCE);
    }


    /**
     * <code>.server.GroupMember.Action action = 1;</code>
     * @return The enum numeric value on the wire for action.
     */
    @java.lang.Override
    public int getActionValue() {
      return instance.getActionValue();
    }
    /**
     * <code>.server.GroupMember.Action action = 1;</code>
     * @param value The action to set.
     * @return This builder for chaining.
     */
    public Builder setActionValue(int value) {
      copyOnWrite();
      instance.setActionValue(value);
      return this;
    }
    /**
     * <code>.server.GroupMember.Action action = 1;</code>
     * @return The action.
     */
    @java.lang.Override
    public com.halloapp.proto.server.GroupMember.Action getAction() {
      return instance.getAction();
    }
    /**
     * <code>.server.GroupMember.Action action = 1;</code>
     * @param value The enum numeric value on the wire for action to set.
     * @return This builder for chaining.
     */
    public Builder setAction(com.halloapp.proto.server.GroupMember.Action value) {
      copyOnWrite();
      instance.setAction(value);
      return this;
    }
    /**
     * <code>.server.GroupMember.Action action = 1;</code>
     * @return This builder for chaining.
     */
    public Builder clearAction() {
      copyOnWrite();
      instance.clearAction();
      return this;
    }

    /**
     * <code>int64 uid = 2;</code>
     * @return The uid.
     */
    @java.lang.Override
    public long getUid() {
      return instance.getUid();
    }
    /**
     * <code>int64 uid = 2;</code>
     * @param value The uid to set.
     * @return This builder for chaining.
     */
    public Builder setUid(long value) {
      copyOnWrite();
      instance.setUid(value);
      return this;
    }
    /**
     * <code>int64 uid = 2;</code>
     * @return This builder for chaining.
     */
    public Builder clearUid() {
      copyOnWrite();
      instance.clearUid();
      return this;
    }

    /**
     * <code>.server.GroupMember.Type type = 3;</code>
     * @return The enum numeric value on the wire for type.
     */
    @java.lang.Override
    public int getTypeValue() {
      return instance.getTypeValue();
    }
    /**
     * <code>.server.GroupMember.Type type = 3;</code>
     * @param value The type to set.
     * @return This builder for chaining.
     */
    public Builder setTypeValue(int value) {
      copyOnWrite();
      instance.setTypeValue(value);
      return this;
    }
    /**
     * <code>.server.GroupMember.Type type = 3;</code>
     * @return The type.
     */
    @java.lang.Override
    public com.halloapp.proto.server.GroupMember.Type getType() {
      return instance.getType();
    }
    /**
     * <code>.server.GroupMember.Type type = 3;</code>
     * @param value The enum numeric value on the wire for type to set.
     * @return This builder for chaining.
     */
    public Builder setType(com.halloapp.proto.server.GroupMember.Type value) {
      copyOnWrite();
      instance.setType(value);
      return this;
    }
    /**
     * <code>.server.GroupMember.Type type = 3;</code>
     * @return This builder for chaining.
     */
    public Builder clearType() {
      copyOnWrite();
      instance.clearType();
      return this;
    }

    /**
     * <code>string name = 4;</code>
     * @return The name.
     */
    @java.lang.Override
    public java.lang.String getName() {
      return instance.getName();
    }
    /**
     * <code>string name = 4;</code>
     * @return The bytes for name.
     */
    @java.lang.Override
    public com.google.protobuf.ByteString
        getNameBytes() {
      return instance.getNameBytes();
    }
    /**
     * <code>string name = 4;</code>
     * @param value The name to set.
     * @return This builder for chaining.
     */
    public Builder setName(
        java.lang.String value) {
      copyOnWrite();
      instance.setName(value);
      return this;
    }
    /**
     * <code>string name = 4;</code>
     * @return This builder for chaining.
     */
    public Builder clearName() {
      copyOnWrite();
      instance.clearName();
      return this;
    }
    /**
     * <code>string name = 4;</code>
     * @param value The bytes for name to set.
     * @return This builder for chaining.
     */
    public Builder setNameBytes(
        com.google.protobuf.ByteString value) {
      copyOnWrite();
      instance.setNameBytes(value);
      return this;
    }

    /**
     * <code>string avatar_id = 5;</code>
     * @return The avatarId.
     */
    @java.lang.Override
    public java.lang.String getAvatarId() {
      return instance.getAvatarId();
    }
    /**
     * <code>string avatar_id = 5;</code>
     * @return The bytes for avatarId.
     */
    @java.lang.Override
    public com.google.protobuf.ByteString
        getAvatarIdBytes() {
      return instance.getAvatarIdBytes();
    }
    /**
     * <code>string avatar_id = 5;</code>
     * @param value The avatarId to set.
     * @return This builder for chaining.
     */
    public Builder setAvatarId(
        java.lang.String value) {
      copyOnWrite();
      instance.setAvatarId(value);
      return this;
    }
    /**
     * <code>string avatar_id = 5;</code>
     * @return This builder for chaining.
     */
    public Builder clearAvatarId() {
      copyOnWrite();
      instance.clearAvatarId();
      return this;
    }
    /**
     * <code>string avatar_id = 5;</code>
     * @param value The bytes for avatarId to set.
     * @return This builder for chaining.
     */
    public Builder setAvatarIdBytes(
        com.google.protobuf.ByteString value) {
      copyOnWrite();
      instance.setAvatarIdBytes(value);
      return this;
    }

    /**
     * <code>string result = 6;</code>
     * @return The result.
     */
    @java.lang.Override
    public java.lang.String getResult() {
      return instance.getResult();
    }
    /**
     * <code>string result = 6;</code>
     * @return The bytes for result.
     */
    @java.lang.Override
    public com.google.protobuf.ByteString
        getResultBytes() {
      return instance.getResultBytes();
    }
    /**
     * <code>string result = 6;</code>
     * @param value The result to set.
     * @return This builder for chaining.
     */
    public Builder setResult(
        java.lang.String value) {
      copyOnWrite();
      instance.setResult(value);
      return this;
    }
    /**
     * <code>string result = 6;</code>
     * @return This builder for chaining.
     */
    public Builder clearResult() {
      copyOnWrite();
      instance.clearResult();
      return this;
    }
    /**
     * <code>string result = 6;</code>
     * @param value The bytes for result to set.
     * @return This builder for chaining.
     */
    public Builder setResultBytes(
        com.google.protobuf.ByteString value) {
      copyOnWrite();
      instance.setResultBytes(value);
      return this;
    }

    /**
     * <code>string reason = 7;</code>
     * @return The reason.
     */
    @java.lang.Override
    public java.lang.String getReason() {
      return instance.getReason();
    }
    /**
     * <code>string reason = 7;</code>
     * @return The bytes for reason.
     */
    @java.lang.Override
    public com.google.protobuf.ByteString
        getReasonBytes() {
      return instance.getReasonBytes();
    }
    /**
     * <code>string reason = 7;</code>
     * @param value The reason to set.
     * @return This builder for chaining.
     */
    public Builder setReason(
        java.lang.String value) {
      copyOnWrite();
      instance.setReason(value);
      return this;
    }
    /**
     * <code>string reason = 7;</code>
     * @return This builder for chaining.
     */
    public Builder clearReason() {
      copyOnWrite();
      instance.clearReason();
      return this;
    }
    /**
     * <code>string reason = 7;</code>
     * @param value The bytes for reason to set.
     * @return This builder for chaining.
     */
    public Builder setReasonBytes(
        com.google.protobuf.ByteString value) {
      copyOnWrite();
      instance.setReasonBytes(value);
      return this;
    }

    /**
     * <pre>
     * Identity key to be returned on `GET_MEMBER_IDENTITY_KEYS` IQ.
     * </pre>
     *
     * <code>bytes identity_key = 8;</code>
     * @return The identityKey.
     */
    @java.lang.Override
    public com.google.protobuf.ByteString getIdentityKey() {
      return instance.getIdentityKey();
    }
    /**
     * <pre>
     * Identity key to be returned on `GET_MEMBER_IDENTITY_KEYS` IQ.
     * </pre>
     *
     * <code>bytes identity_key = 8;</code>
     * @param value The identityKey to set.
     * @return This builder for chaining.
     */
    public Builder setIdentityKey(com.google.protobuf.ByteString value) {
      copyOnWrite();
      instance.setIdentityKey(value);
      return this;
    }
    /**
     * <pre>
     * Identity key to be returned on `GET_MEMBER_IDENTITY_KEYS` IQ.
     * </pre>
     *
     * <code>bytes identity_key = 8;</code>
     * @return This builder for chaining.
     */
    public Builder clearIdentityKey() {
      copyOnWrite();
      instance.clearIdentityKey();
      return this;
    }

    // @@protoc_insertion_point(builder_scope:server.GroupMember)
  }
  @java.lang.Override
  @java.lang.SuppressWarnings({"unchecked", "fallthrough"})
  protected final java.lang.Object dynamicMethod(
      com.google.protobuf.GeneratedMessageLite.MethodToInvoke method,
      java.lang.Object arg0, java.lang.Object arg1) {
    switch (method) {
      case NEW_MUTABLE_INSTANCE: {
        return new com.halloapp.proto.server.GroupMember();
      }
      case NEW_BUILDER: {
        return new Builder();
      }
      case BUILD_MESSAGE_INFO: {
          java.lang.Object[] objects = new java.lang.Object[] {
            "action_",
            "uid_",
            "type_",
            "name_",
            "avatarId_",
            "result_",
            "reason_",
            "identityKey_",
          };
          java.lang.String info =
              "\u0000\b\u0000\u0000\u0001\b\b\u0000\u0000\u0000\u0001\f\u0002\u0002\u0003\f\u0004" +
              "\u0208\u0005\u0208\u0006\u0208\u0007\u0208\b\n";
          return newMessageInfo(DEFAULT_INSTANCE, info, objects);
      }
      // fall through
      case GET_DEFAULT_INSTANCE: {
        return DEFAULT_INSTANCE;
      }
      case GET_PARSER: {
        com.google.protobuf.Parser<com.halloapp.proto.server.GroupMember> parser = PARSER;
        if (parser == null) {
          synchronized (com.halloapp.proto.server.GroupMember.class) {
            parser = PARSER;
            if (parser == null) {
              parser =
                  new DefaultInstanceBasedParser<com.halloapp.proto.server.GroupMember>(
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


  // @@protoc_insertion_point(class_scope:server.GroupMember)
  private static final com.halloapp.proto.server.GroupMember DEFAULT_INSTANCE;
  static {
    GroupMember defaultInstance = new GroupMember();
    // New instances are implicitly immutable so no need to make
    // immutable.
    DEFAULT_INSTANCE = defaultInstance;
    com.google.protobuf.GeneratedMessageLite.registerDefaultInstance(
      GroupMember.class, defaultInstance);
  }

  public static com.halloapp.proto.server.GroupMember getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static volatile com.google.protobuf.Parser<GroupMember> PARSER;

  public static com.google.protobuf.Parser<GroupMember> parser() {
    return DEFAULT_INSTANCE.getParserForType();
  }
}

