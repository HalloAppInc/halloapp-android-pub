// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: server.proto

package com.halloapp.proto.server;

/**
 * Protobuf type {@code server.Contact}
 */
public  final class Contact extends
    com.google.protobuf.GeneratedMessageLite<
        Contact, Contact.Builder> implements
    // @@protoc_insertion_point(message_implements:server.Contact)
    ContactOrBuilder {
  private Contact() {
    raw_ = "";
    normalized_ = "";
    avatarId_ = "";
    name_ = "";
  }
  /**
   * Protobuf enum {@code server.Contact.Action}
   */
  public enum Action
      implements com.google.protobuf.Internal.EnumLite {
    /**
     * <code>ADD = 0;</code>
     */
    ADD(0),
    /**
     * <code>DELETE = 1;</code>
     */
    DELETE(1),
    UNRECOGNIZED(-1),
    ;

    /**
     * <code>ADD = 0;</code>
     */
    public static final int ADD_VALUE = 0;
    /**
     * <code>DELETE = 1;</code>
     */
    public static final int DELETE_VALUE = 1;


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
        case 1: return DELETE;
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

    // @@protoc_insertion_point(enum_scope:server.Contact.Action)
  }

  public static final int ACTION_FIELD_NUMBER = 1;
  private int action_;
  /**
   * <code>.server.Contact.Action action = 1;</code>
   * @return The enum numeric value on the wire for action.
   */
  @java.lang.Override
  public int getActionValue() {
    return action_;
  }
  /**
   * <code>.server.Contact.Action action = 1;</code>
   * @return The action.
   */
  @java.lang.Override
  public com.halloapp.proto.server.Contact.Action getAction() {
    com.halloapp.proto.server.Contact.Action result = com.halloapp.proto.server.Contact.Action.forNumber(action_);
    return result == null ? com.halloapp.proto.server.Contact.Action.UNRECOGNIZED : result;
  }
  /**
   * <code>.server.Contact.Action action = 1;</code>
   * @param value The enum numeric value on the wire for action to set.
   */
  private void setActionValue(int value) {
      action_ = value;
  }
  /**
   * <code>.server.Contact.Action action = 1;</code>
   * @param value The action to set.
   */
  private void setAction(com.halloapp.proto.server.Contact.Action value) {
    action_ = value.getNumber();
    
  }
  /**
   * <code>.server.Contact.Action action = 1;</code>
   */
  private void clearAction() {
    
    action_ = 0;
  }

  public static final int RAW_FIELD_NUMBER = 2;
  private java.lang.String raw_;
  /**
   * <code>string raw = 2;</code>
   * @return The raw.
   */
  @java.lang.Override
  public java.lang.String getRaw() {
    return raw_;
  }
  /**
   * <code>string raw = 2;</code>
   * @return The bytes for raw.
   */
  @java.lang.Override
  public com.google.protobuf.ByteString
      getRawBytes() {
    return com.google.protobuf.ByteString.copyFromUtf8(raw_);
  }
  /**
   * <code>string raw = 2;</code>
   * @param value The raw to set.
   */
  private void setRaw(
      java.lang.String value) {
    java.lang.Class<?> valueClass = value.getClass();
  
    raw_ = value;
  }
  /**
   * <code>string raw = 2;</code>
   */
  private void clearRaw() {
    
    raw_ = getDefaultInstance().getRaw();
  }
  /**
   * <code>string raw = 2;</code>
   * @param value The bytes for raw to set.
   */
  private void setRawBytes(
      com.google.protobuf.ByteString value) {
    checkByteStringIsUtf8(value);
    raw_ = value.toStringUtf8();
    
  }

  public static final int NORMALIZED_FIELD_NUMBER = 3;
  private java.lang.String normalized_;
  /**
   * <code>string normalized = 3;</code>
   * @return The normalized.
   */
  @java.lang.Override
  public java.lang.String getNormalized() {
    return normalized_;
  }
  /**
   * <code>string normalized = 3;</code>
   * @return The bytes for normalized.
   */
  @java.lang.Override
  public com.google.protobuf.ByteString
      getNormalizedBytes() {
    return com.google.protobuf.ByteString.copyFromUtf8(normalized_);
  }
  /**
   * <code>string normalized = 3;</code>
   * @param value The normalized to set.
   */
  private void setNormalized(
      java.lang.String value) {
    java.lang.Class<?> valueClass = value.getClass();
  
    normalized_ = value;
  }
  /**
   * <code>string normalized = 3;</code>
   */
  private void clearNormalized() {
    
    normalized_ = getDefaultInstance().getNormalized();
  }
  /**
   * <code>string normalized = 3;</code>
   * @param value The bytes for normalized to set.
   */
  private void setNormalizedBytes(
      com.google.protobuf.ByteString value) {
    checkByteStringIsUtf8(value);
    normalized_ = value.toStringUtf8();
    
  }

  public static final int UID_FIELD_NUMBER = 4;
  private long uid_;
  /**
   * <code>int64 uid = 4;</code>
   * @return The uid.
   */
  @java.lang.Override
  public long getUid() {
    return uid_;
  }
  /**
   * <code>int64 uid = 4;</code>
   * @param value The uid to set.
   */
  private void setUid(long value) {
    
    uid_ = value;
  }
  /**
   * <code>int64 uid = 4;</code>
   */
  private void clearUid() {
    
    uid_ = 0L;
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

  public static final int NAME_FIELD_NUMBER = 7;
  private java.lang.String name_;
  /**
   * <code>string name = 7;</code>
   * @return The name.
   */
  @java.lang.Override
  public java.lang.String getName() {
    return name_;
  }
  /**
   * <code>string name = 7;</code>
   * @return The bytes for name.
   */
  @java.lang.Override
  public com.google.protobuf.ByteString
      getNameBytes() {
    return com.google.protobuf.ByteString.copyFromUtf8(name_);
  }
  /**
   * <code>string name = 7;</code>
   * @param value The name to set.
   */
  private void setName(
      java.lang.String value) {
    java.lang.Class<?> valueClass = value.getClass();
  
    name_ = value;
  }
  /**
   * <code>string name = 7;</code>
   */
  private void clearName() {
    
    name_ = getDefaultInstance().getName();
  }
  /**
   * <code>string name = 7;</code>
   * @param value The bytes for name to set.
   */
  private void setNameBytes(
      com.google.protobuf.ByteString value) {
    checkByteStringIsUtf8(value);
    name_ = value.toStringUtf8();
    
  }

  public static final int NUM_POTENTIAL_FRIENDS_FIELD_NUMBER = 8;
  private long numPotentialFriends_;
  /**
   * <code>int64 num_potential_friends = 8;</code>
   * @return The numPotentialFriends.
   */
  @java.lang.Override
  public long getNumPotentialFriends() {
    return numPotentialFriends_;
  }
  /**
   * <code>int64 num_potential_friends = 8;</code>
   * @param value The numPotentialFriends to set.
   */
  private void setNumPotentialFriends(long value) {
    
    numPotentialFriends_ = value;
  }
  /**
   * <code>int64 num_potential_friends = 8;</code>
   */
  private void clearNumPotentialFriends() {
    
    numPotentialFriends_ = 0L;
  }

  public static final int NUM_POTENTIAL_CLOSE_FRIENDS_FIELD_NUMBER = 9;
  private long numPotentialCloseFriends_;
  /**
   * <code>int64 num_potential_close_friends = 9;</code>
   * @return The numPotentialCloseFriends.
   */
  @java.lang.Override
  public long getNumPotentialCloseFriends() {
    return numPotentialCloseFriends_;
  }
  /**
   * <code>int64 num_potential_close_friends = 9;</code>
   * @param value The numPotentialCloseFriends to set.
   */
  private void setNumPotentialCloseFriends(long value) {
    
    numPotentialCloseFriends_ = value;
  }
  /**
   * <code>int64 num_potential_close_friends = 9;</code>
   */
  private void clearNumPotentialCloseFriends() {
    
    numPotentialCloseFriends_ = 0L;
  }

  public static final int INVITATION_RANK_FIELD_NUMBER = 10;
  private long invitationRank_;
  /**
   * <code>int64 invitation_rank = 10;</code>
   * @return The invitationRank.
   */
  @java.lang.Override
  public long getInvitationRank() {
    return invitationRank_;
  }
  /**
   * <code>int64 invitation_rank = 10;</code>
   * @param value The invitationRank to set.
   */
  private void setInvitationRank(long value) {
    
    invitationRank_ = value;
  }
  /**
   * <code>int64 invitation_rank = 10;</code>
   */
  private void clearInvitationRank() {
    
    invitationRank_ = 0L;
  }

  public static com.halloapp.proto.server.Contact parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static com.halloapp.proto.server.Contact parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static com.halloapp.proto.server.Contact parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static com.halloapp.proto.server.Contact parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static com.halloapp.proto.server.Contact parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static com.halloapp.proto.server.Contact parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static com.halloapp.proto.server.Contact parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input);
  }
  public static com.halloapp.proto.server.Contact parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input, extensionRegistry);
  }
  public static com.halloapp.proto.server.Contact parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return parseDelimitedFrom(DEFAULT_INSTANCE, input);
  }
  public static com.halloapp.proto.server.Contact parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return parseDelimitedFrom(DEFAULT_INSTANCE, input, extensionRegistry);
  }
  public static com.halloapp.proto.server.Contact parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input);
  }
  public static com.halloapp.proto.server.Contact parseFrom(
      com.google.protobuf.CodedInputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input, extensionRegistry);
  }

  public static Builder newBuilder() {
    return (Builder) DEFAULT_INSTANCE.createBuilder();
  }
  public static Builder newBuilder(com.halloapp.proto.server.Contact prototype) {
    return (Builder) DEFAULT_INSTANCE.createBuilder(prototype);
  }

  /**
   * Protobuf type {@code server.Contact}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessageLite.Builder<
        com.halloapp.proto.server.Contact, Builder> implements
      // @@protoc_insertion_point(builder_implements:server.Contact)
      com.halloapp.proto.server.ContactOrBuilder {
    // Construct using com.halloapp.proto.server.Contact.newBuilder()
    private Builder() {
      super(DEFAULT_INSTANCE);
    }


    /**
     * <code>.server.Contact.Action action = 1;</code>
     * @return The enum numeric value on the wire for action.
     */
    @java.lang.Override
    public int getActionValue() {
      return instance.getActionValue();
    }
    /**
     * <code>.server.Contact.Action action = 1;</code>
     * @param value The action to set.
     * @return This builder for chaining.
     */
    public Builder setActionValue(int value) {
      copyOnWrite();
      instance.setActionValue(value);
      return this;
    }
    /**
     * <code>.server.Contact.Action action = 1;</code>
     * @return The action.
     */
    @java.lang.Override
    public com.halloapp.proto.server.Contact.Action getAction() {
      return instance.getAction();
    }
    /**
     * <code>.server.Contact.Action action = 1;</code>
     * @param value The enum numeric value on the wire for action to set.
     * @return This builder for chaining.
     */
    public Builder setAction(com.halloapp.proto.server.Contact.Action value) {
      copyOnWrite();
      instance.setAction(value);
      return this;
    }
    /**
     * <code>.server.Contact.Action action = 1;</code>
     * @return This builder for chaining.
     */
    public Builder clearAction() {
      copyOnWrite();
      instance.clearAction();
      return this;
    }

    /**
     * <code>string raw = 2;</code>
     * @return The raw.
     */
    @java.lang.Override
    public java.lang.String getRaw() {
      return instance.getRaw();
    }
    /**
     * <code>string raw = 2;</code>
     * @return The bytes for raw.
     */
    @java.lang.Override
    public com.google.protobuf.ByteString
        getRawBytes() {
      return instance.getRawBytes();
    }
    /**
     * <code>string raw = 2;</code>
     * @param value The raw to set.
     * @return This builder for chaining.
     */
    public Builder setRaw(
        java.lang.String value) {
      copyOnWrite();
      instance.setRaw(value);
      return this;
    }
    /**
     * <code>string raw = 2;</code>
     * @return This builder for chaining.
     */
    public Builder clearRaw() {
      copyOnWrite();
      instance.clearRaw();
      return this;
    }
    /**
     * <code>string raw = 2;</code>
     * @param value The bytes for raw to set.
     * @return This builder for chaining.
     */
    public Builder setRawBytes(
        com.google.protobuf.ByteString value) {
      copyOnWrite();
      instance.setRawBytes(value);
      return this;
    }

    /**
     * <code>string normalized = 3;</code>
     * @return The normalized.
     */
    @java.lang.Override
    public java.lang.String getNormalized() {
      return instance.getNormalized();
    }
    /**
     * <code>string normalized = 3;</code>
     * @return The bytes for normalized.
     */
    @java.lang.Override
    public com.google.protobuf.ByteString
        getNormalizedBytes() {
      return instance.getNormalizedBytes();
    }
    /**
     * <code>string normalized = 3;</code>
     * @param value The normalized to set.
     * @return This builder for chaining.
     */
    public Builder setNormalized(
        java.lang.String value) {
      copyOnWrite();
      instance.setNormalized(value);
      return this;
    }
    /**
     * <code>string normalized = 3;</code>
     * @return This builder for chaining.
     */
    public Builder clearNormalized() {
      copyOnWrite();
      instance.clearNormalized();
      return this;
    }
    /**
     * <code>string normalized = 3;</code>
     * @param value The bytes for normalized to set.
     * @return This builder for chaining.
     */
    public Builder setNormalizedBytes(
        com.google.protobuf.ByteString value) {
      copyOnWrite();
      instance.setNormalizedBytes(value);
      return this;
    }

    /**
     * <code>int64 uid = 4;</code>
     * @return The uid.
     */
    @java.lang.Override
    public long getUid() {
      return instance.getUid();
    }
    /**
     * <code>int64 uid = 4;</code>
     * @param value The uid to set.
     * @return This builder for chaining.
     */
    public Builder setUid(long value) {
      copyOnWrite();
      instance.setUid(value);
      return this;
    }
    /**
     * <code>int64 uid = 4;</code>
     * @return This builder for chaining.
     */
    public Builder clearUid() {
      copyOnWrite();
      instance.clearUid();
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
     * <code>string name = 7;</code>
     * @return The name.
     */
    @java.lang.Override
    public java.lang.String getName() {
      return instance.getName();
    }
    /**
     * <code>string name = 7;</code>
     * @return The bytes for name.
     */
    @java.lang.Override
    public com.google.protobuf.ByteString
        getNameBytes() {
      return instance.getNameBytes();
    }
    /**
     * <code>string name = 7;</code>
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
     * <code>string name = 7;</code>
     * @return This builder for chaining.
     */
    public Builder clearName() {
      copyOnWrite();
      instance.clearName();
      return this;
    }
    /**
     * <code>string name = 7;</code>
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
     * <code>int64 num_potential_friends = 8;</code>
     * @return The numPotentialFriends.
     */
    @java.lang.Override
    public long getNumPotentialFriends() {
      return instance.getNumPotentialFriends();
    }
    /**
     * <code>int64 num_potential_friends = 8;</code>
     * @param value The numPotentialFriends to set.
     * @return This builder for chaining.
     */
    public Builder setNumPotentialFriends(long value) {
      copyOnWrite();
      instance.setNumPotentialFriends(value);
      return this;
    }
    /**
     * <code>int64 num_potential_friends = 8;</code>
     * @return This builder for chaining.
     */
    public Builder clearNumPotentialFriends() {
      copyOnWrite();
      instance.clearNumPotentialFriends();
      return this;
    }

    /**
     * <code>int64 num_potential_close_friends = 9;</code>
     * @return The numPotentialCloseFriends.
     */
    @java.lang.Override
    public long getNumPotentialCloseFriends() {
      return instance.getNumPotentialCloseFriends();
    }
    /**
     * <code>int64 num_potential_close_friends = 9;</code>
     * @param value The numPotentialCloseFriends to set.
     * @return This builder for chaining.
     */
    public Builder setNumPotentialCloseFriends(long value) {
      copyOnWrite();
      instance.setNumPotentialCloseFriends(value);
      return this;
    }
    /**
     * <code>int64 num_potential_close_friends = 9;</code>
     * @return This builder for chaining.
     */
    public Builder clearNumPotentialCloseFriends() {
      copyOnWrite();
      instance.clearNumPotentialCloseFriends();
      return this;
    }

    /**
     * <code>int64 invitation_rank = 10;</code>
     * @return The invitationRank.
     */
    @java.lang.Override
    public long getInvitationRank() {
      return instance.getInvitationRank();
    }
    /**
     * <code>int64 invitation_rank = 10;</code>
     * @param value The invitationRank to set.
     * @return This builder for chaining.
     */
    public Builder setInvitationRank(long value) {
      copyOnWrite();
      instance.setInvitationRank(value);
      return this;
    }
    /**
     * <code>int64 invitation_rank = 10;</code>
     * @return This builder for chaining.
     */
    public Builder clearInvitationRank() {
      copyOnWrite();
      instance.clearInvitationRank();
      return this;
    }

    // @@protoc_insertion_point(builder_scope:server.Contact)
  }
  @java.lang.Override
  @java.lang.SuppressWarnings({"unchecked", "fallthrough"})
  protected final java.lang.Object dynamicMethod(
      com.google.protobuf.GeneratedMessageLite.MethodToInvoke method,
      java.lang.Object arg0, java.lang.Object arg1) {
    switch (method) {
      case NEW_MUTABLE_INSTANCE: {
        return new com.halloapp.proto.server.Contact();
      }
      case NEW_BUILDER: {
        return new Builder();
      }
      case BUILD_MESSAGE_INFO: {
          java.lang.Object[] objects = new java.lang.Object[] {
            "action_",
            "raw_",
            "normalized_",
            "uid_",
            "avatarId_",
            "name_",
            "numPotentialFriends_",
            "numPotentialCloseFriends_",
            "invitationRank_",
          };
          java.lang.String info =
              "\u0000\t\u0000\u0000\u0001\n\t\u0000\u0000\u0000\u0001\f\u0002\u0208\u0003\u0208" +
              "\u0004\u0002\u0005\u0208\u0007\u0208\b\u0002\t\u0002\n\u0002";
          return newMessageInfo(DEFAULT_INSTANCE, info, objects);
      }
      // fall through
      case GET_DEFAULT_INSTANCE: {
        return DEFAULT_INSTANCE;
      }
      case GET_PARSER: {
        com.google.protobuf.Parser<com.halloapp.proto.server.Contact> parser = PARSER;
        if (parser == null) {
          synchronized (com.halloapp.proto.server.Contact.class) {
            parser = PARSER;
            if (parser == null) {
              parser =
                  new DefaultInstanceBasedParser<com.halloapp.proto.server.Contact>(
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


  // @@protoc_insertion_point(class_scope:server.Contact)
  private static final com.halloapp.proto.server.Contact DEFAULT_INSTANCE;
  static {
    Contact defaultInstance = new Contact();
    // New instances are implicitly immutable so no need to make
    // immutable.
    DEFAULT_INSTANCE = defaultInstance;
    com.google.protobuf.GeneratedMessageLite.registerDefaultInstance(
      Contact.class, defaultInstance);
  }

  public static com.halloapp.proto.server.Contact getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static volatile com.google.protobuf.Parser<Contact> PARSER;

  public static com.google.protobuf.Parser<Contact> parser() {
    return DEFAULT_INSTANCE.getParserForType();
  }
}

