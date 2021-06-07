// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: server.proto

package com.halloapp.proto.server;

/**
 * Protobuf type {@code server.GroupStanza}
 */
public  final class GroupStanza extends
    com.google.protobuf.GeneratedMessageLite<
        GroupStanza, GroupStanza.Builder> implements
    // @@protoc_insertion_point(message_implements:server.GroupStanza)
    GroupStanzaOrBuilder {
  private GroupStanza() {
    gid_ = "";
    name_ = "";
    avatarId_ = "";
    senderName_ = "";
    members_ = emptyProtobufList();
    background_ = "";
    audienceHash_ = com.google.protobuf.ByteString.EMPTY;
  }
  /**
   * Protobuf enum {@code server.GroupStanza.Action}
   */
  public enum Action
      implements com.google.protobuf.Internal.EnumLite {
    /**
     * <code>SET = 0;</code>
     */
    SET(0),
    /**
     * <code>GET = 1;</code>
     */
    GET(1),
    /**
     * <code>CREATE = 2;</code>
     */
    CREATE(2),
    /**
     * <code>DELETE = 3;</code>
     */
    DELETE(3),
    /**
     * <code>LEAVE = 4;</code>
     */
    LEAVE(4),
    /**
     * <code>CHANGE_AVATAR = 5;</code>
     */
    CHANGE_AVATAR(5),
    /**
     * <code>CHANGE_NAME = 6;</code>
     */
    CHANGE_NAME(6),
    /**
     * <code>MODIFY_ADMINS = 7;</code>
     */
    MODIFY_ADMINS(7),
    /**
     * <code>MODIFY_MEMBERS = 8;</code>
     */
    MODIFY_MEMBERS(8),
    /**
     * <code>AUTO_PROMOTE_ADMINS = 9;</code>
     */
    AUTO_PROMOTE_ADMINS(9),
    /**
     * <code>SET_NAME = 10;</code>
     */
    SET_NAME(10),
    /**
     * <code>JOIN = 11;</code>
     */
    JOIN(11),
    /**
     * <code>PREVIEW = 12;</code>
     */
    PREVIEW(12),
    /**
     * <code>SET_BACKGROUND = 13;</code>
     */
    SET_BACKGROUND(13),
    /**
     * <code>GET_MEMBER_IDENTITY_KEYS = 14;</code>
     */
    GET_MEMBER_IDENTITY_KEYS(14),
    UNRECOGNIZED(-1),
    ;

    /**
     * <code>SET = 0;</code>
     */
    public static final int SET_VALUE = 0;
    /**
     * <code>GET = 1;</code>
     */
    public static final int GET_VALUE = 1;
    /**
     * <code>CREATE = 2;</code>
     */
    public static final int CREATE_VALUE = 2;
    /**
     * <code>DELETE = 3;</code>
     */
    public static final int DELETE_VALUE = 3;
    /**
     * <code>LEAVE = 4;</code>
     */
    public static final int LEAVE_VALUE = 4;
    /**
     * <code>CHANGE_AVATAR = 5;</code>
     */
    public static final int CHANGE_AVATAR_VALUE = 5;
    /**
     * <code>CHANGE_NAME = 6;</code>
     */
    public static final int CHANGE_NAME_VALUE = 6;
    /**
     * <code>MODIFY_ADMINS = 7;</code>
     */
    public static final int MODIFY_ADMINS_VALUE = 7;
    /**
     * <code>MODIFY_MEMBERS = 8;</code>
     */
    public static final int MODIFY_MEMBERS_VALUE = 8;
    /**
     * <code>AUTO_PROMOTE_ADMINS = 9;</code>
     */
    public static final int AUTO_PROMOTE_ADMINS_VALUE = 9;
    /**
     * <code>SET_NAME = 10;</code>
     */
    public static final int SET_NAME_VALUE = 10;
    /**
     * <code>JOIN = 11;</code>
     */
    public static final int JOIN_VALUE = 11;
    /**
     * <code>PREVIEW = 12;</code>
     */
    public static final int PREVIEW_VALUE = 12;
    /**
     * <code>SET_BACKGROUND = 13;</code>
     */
    public static final int SET_BACKGROUND_VALUE = 13;
    /**
     * <code>GET_MEMBER_IDENTITY_KEYS = 14;</code>
     */
    public static final int GET_MEMBER_IDENTITY_KEYS_VALUE = 14;


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
        case 0: return SET;
        case 1: return GET;
        case 2: return CREATE;
        case 3: return DELETE;
        case 4: return LEAVE;
        case 5: return CHANGE_AVATAR;
        case 6: return CHANGE_NAME;
        case 7: return MODIFY_ADMINS;
        case 8: return MODIFY_MEMBERS;
        case 9: return AUTO_PROMOTE_ADMINS;
        case 10: return SET_NAME;
        case 11: return JOIN;
        case 12: return PREVIEW;
        case 13: return SET_BACKGROUND;
        case 14: return GET_MEMBER_IDENTITY_KEYS;
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

    // @@protoc_insertion_point(enum_scope:server.GroupStanza.Action)
  }

  public static final int ACTION_FIELD_NUMBER = 1;
  private int action_;
  /**
   * <code>.server.GroupStanza.Action action = 1;</code>
   * @return The enum numeric value on the wire for action.
   */
  @java.lang.Override
  public int getActionValue() {
    return action_;
  }
  /**
   * <code>.server.GroupStanza.Action action = 1;</code>
   * @return The action.
   */
  @java.lang.Override
  public com.halloapp.proto.server.GroupStanza.Action getAction() {
    com.halloapp.proto.server.GroupStanza.Action result = com.halloapp.proto.server.GroupStanza.Action.forNumber(action_);
    return result == null ? com.halloapp.proto.server.GroupStanza.Action.UNRECOGNIZED : result;
  }
  /**
   * <code>.server.GroupStanza.Action action = 1;</code>
   * @param value The enum numeric value on the wire for action to set.
   */
  private void setActionValue(int value) {
      action_ = value;
  }
  /**
   * <code>.server.GroupStanza.Action action = 1;</code>
   * @param value The action to set.
   */
  private void setAction(com.halloapp.proto.server.GroupStanza.Action value) {
    action_ = value.getNumber();
    
  }
  /**
   * <code>.server.GroupStanza.Action action = 1;</code>
   */
  private void clearAction() {
    
    action_ = 0;
  }

  public static final int GID_FIELD_NUMBER = 2;
  private java.lang.String gid_;
  /**
   * <code>string gid = 2;</code>
   * @return The gid.
   */
  @java.lang.Override
  public java.lang.String getGid() {
    return gid_;
  }
  /**
   * <code>string gid = 2;</code>
   * @return The bytes for gid.
   */
  @java.lang.Override
  public com.google.protobuf.ByteString
      getGidBytes() {
    return com.google.protobuf.ByteString.copyFromUtf8(gid_);
  }
  /**
   * <code>string gid = 2;</code>
   * @param value The gid to set.
   */
  private void setGid(
      java.lang.String value) {
    value.getClass();
  
    gid_ = value;
  }
  /**
   * <code>string gid = 2;</code>
   */
  private void clearGid() {
    
    gid_ = getDefaultInstance().getGid();
  }
  /**
   * <code>string gid = 2;</code>
   * @param value The bytes for gid to set.
   */
  private void setGidBytes(
      com.google.protobuf.ByteString value) {
    checkByteStringIsUtf8(value);
    gid_ = value.toStringUtf8();
    
  }

  public static final int NAME_FIELD_NUMBER = 3;
  private java.lang.String name_;
  /**
   * <code>string name = 3;</code>
   * @return The name.
   */
  @java.lang.Override
  public java.lang.String getName() {
    return name_;
  }
  /**
   * <code>string name = 3;</code>
   * @return The bytes for name.
   */
  @java.lang.Override
  public com.google.protobuf.ByteString
      getNameBytes() {
    return com.google.protobuf.ByteString.copyFromUtf8(name_);
  }
  /**
   * <code>string name = 3;</code>
   * @param value The name to set.
   */
  private void setName(
      java.lang.String value) {
    value.getClass();
  
    name_ = value;
  }
  /**
   * <code>string name = 3;</code>
   */
  private void clearName() {
    
    name_ = getDefaultInstance().getName();
  }
  /**
   * <code>string name = 3;</code>
   * @param value The bytes for name to set.
   */
  private void setNameBytes(
      com.google.protobuf.ByteString value) {
    checkByteStringIsUtf8(value);
    name_ = value.toStringUtf8();
    
  }

  public static final int AVATAR_ID_FIELD_NUMBER = 4;
  private java.lang.String avatarId_;
  /**
   * <code>string avatar_id = 4;</code>
   * @return The avatarId.
   */
  @java.lang.Override
  public java.lang.String getAvatarId() {
    return avatarId_;
  }
  /**
   * <code>string avatar_id = 4;</code>
   * @return The bytes for avatarId.
   */
  @java.lang.Override
  public com.google.protobuf.ByteString
      getAvatarIdBytes() {
    return com.google.protobuf.ByteString.copyFromUtf8(avatarId_);
  }
  /**
   * <code>string avatar_id = 4;</code>
   * @param value The avatarId to set.
   */
  private void setAvatarId(
      java.lang.String value) {
    value.getClass();
  
    avatarId_ = value;
  }
  /**
   * <code>string avatar_id = 4;</code>
   */
  private void clearAvatarId() {
    
    avatarId_ = getDefaultInstance().getAvatarId();
  }
  /**
   * <code>string avatar_id = 4;</code>
   * @param value The bytes for avatarId to set.
   */
  private void setAvatarIdBytes(
      com.google.protobuf.ByteString value) {
    checkByteStringIsUtf8(value);
    avatarId_ = value.toStringUtf8();
    
  }

  public static final int SENDER_UID_FIELD_NUMBER = 5;
  private long senderUid_;
  /**
   * <code>int64 sender_uid = 5;</code>
   * @return The senderUid.
   */
  @java.lang.Override
  public long getSenderUid() {
    return senderUid_;
  }
  /**
   * <code>int64 sender_uid = 5;</code>
   * @param value The senderUid to set.
   */
  private void setSenderUid(long value) {
    
    senderUid_ = value;
  }
  /**
   * <code>int64 sender_uid = 5;</code>
   */
  private void clearSenderUid() {
    
    senderUid_ = 0L;
  }

  public static final int SENDER_NAME_FIELD_NUMBER = 6;
  private java.lang.String senderName_;
  /**
   * <code>string sender_name = 6;</code>
   * @return The senderName.
   */
  @java.lang.Override
  public java.lang.String getSenderName() {
    return senderName_;
  }
  /**
   * <code>string sender_name = 6;</code>
   * @return The bytes for senderName.
   */
  @java.lang.Override
  public com.google.protobuf.ByteString
      getSenderNameBytes() {
    return com.google.protobuf.ByteString.copyFromUtf8(senderName_);
  }
  /**
   * <code>string sender_name = 6;</code>
   * @param value The senderName to set.
   */
  private void setSenderName(
      java.lang.String value) {
    value.getClass();
  
    senderName_ = value;
  }
  /**
   * <code>string sender_name = 6;</code>
   */
  private void clearSenderName() {
    
    senderName_ = getDefaultInstance().getSenderName();
  }
  /**
   * <code>string sender_name = 6;</code>
   * @param value The bytes for senderName to set.
   */
  private void setSenderNameBytes(
      com.google.protobuf.ByteString value) {
    checkByteStringIsUtf8(value);
    senderName_ = value.toStringUtf8();
    
  }

  public static final int MEMBERS_FIELD_NUMBER = 7;
  private com.google.protobuf.Internal.ProtobufList<com.halloapp.proto.server.GroupMember> members_;
  /**
   * <code>repeated .server.GroupMember members = 7;</code>
   */
  @java.lang.Override
  public java.util.List<com.halloapp.proto.server.GroupMember> getMembersList() {
    return members_;
  }
  /**
   * <code>repeated .server.GroupMember members = 7;</code>
   */
  public java.util.List<? extends com.halloapp.proto.server.GroupMemberOrBuilder> 
      getMembersOrBuilderList() {
    return members_;
  }
  /**
   * <code>repeated .server.GroupMember members = 7;</code>
   */
  @java.lang.Override
  public int getMembersCount() {
    return members_.size();
  }
  /**
   * <code>repeated .server.GroupMember members = 7;</code>
   */
  @java.lang.Override
  public com.halloapp.proto.server.GroupMember getMembers(int index) {
    return members_.get(index);
  }
  /**
   * <code>repeated .server.GroupMember members = 7;</code>
   */
  public com.halloapp.proto.server.GroupMemberOrBuilder getMembersOrBuilder(
      int index) {
    return members_.get(index);
  }
  private void ensureMembersIsMutable() {
    com.google.protobuf.Internal.ProtobufList<com.halloapp.proto.server.GroupMember> tmp = members_;
    if (!tmp.isModifiable()) {
      members_ =
          com.google.protobuf.GeneratedMessageLite.mutableCopy(tmp);
     }
  }

  /**
   * <code>repeated .server.GroupMember members = 7;</code>
   */
  private void setMembers(
      int index, com.halloapp.proto.server.GroupMember value) {
    value.getClass();
  ensureMembersIsMutable();
    members_.set(index, value);
  }
  /**
   * <code>repeated .server.GroupMember members = 7;</code>
   */
  private void addMembers(com.halloapp.proto.server.GroupMember value) {
    value.getClass();
  ensureMembersIsMutable();
    members_.add(value);
  }
  /**
   * <code>repeated .server.GroupMember members = 7;</code>
   */
  private void addMembers(
      int index, com.halloapp.proto.server.GroupMember value) {
    value.getClass();
  ensureMembersIsMutable();
    members_.add(index, value);
  }
  /**
   * <code>repeated .server.GroupMember members = 7;</code>
   */
  private void addAllMembers(
      java.lang.Iterable<? extends com.halloapp.proto.server.GroupMember> values) {
    ensureMembersIsMutable();
    com.google.protobuf.AbstractMessageLite.addAll(
        values, members_);
  }
  /**
   * <code>repeated .server.GroupMember members = 7;</code>
   */
  private void clearMembers() {
    members_ = emptyProtobufList();
  }
  /**
   * <code>repeated .server.GroupMember members = 7;</code>
   */
  private void removeMembers(int index) {
    ensureMembersIsMutable();
    members_.remove(index);
  }

  public static final int BACKGROUND_FIELD_NUMBER = 8;
  private java.lang.String background_;
  /**
   * <code>string background = 8;</code>
   * @return The background.
   */
  @java.lang.Override
  public java.lang.String getBackground() {
    return background_;
  }
  /**
   * <code>string background = 8;</code>
   * @return The bytes for background.
   */
  @java.lang.Override
  public com.google.protobuf.ByteString
      getBackgroundBytes() {
    return com.google.protobuf.ByteString.copyFromUtf8(background_);
  }
  /**
   * <code>string background = 8;</code>
   * @param value The background to set.
   */
  private void setBackground(
      java.lang.String value) {
    value.getClass();
  
    background_ = value;
  }
  /**
   * <code>string background = 8;</code>
   */
  private void clearBackground() {
    
    background_ = getDefaultInstance().getBackground();
  }
  /**
   * <code>string background = 8;</code>
   * @param value The bytes for background to set.
   */
  private void setBackgroundBytes(
      com.google.protobuf.ByteString value) {
    checkByteStringIsUtf8(value);
    background_ = value.toStringUtf8();
    
  }

  public static final int AUDIENCE_HASH_FIELD_NUMBER = 9;
  private com.google.protobuf.ByteString audienceHash_;
  /**
   * <code>bytes audience_hash = 9;</code>
   * @return The audienceHash.
   */
  @java.lang.Override
  public com.google.protobuf.ByteString getAudienceHash() {
    return audienceHash_;
  }
  /**
   * <code>bytes audience_hash = 9;</code>
   * @param value The audienceHash to set.
   */
  private void setAudienceHash(com.google.protobuf.ByteString value) {
    value.getClass();
  
    audienceHash_ = value;
  }
  /**
   * <code>bytes audience_hash = 9;</code>
   */
  private void clearAudienceHash() {
    
    audienceHash_ = getDefaultInstance().getAudienceHash();
  }

  public static com.halloapp.proto.server.GroupStanza parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static com.halloapp.proto.server.GroupStanza parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static com.halloapp.proto.server.GroupStanza parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static com.halloapp.proto.server.GroupStanza parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static com.halloapp.proto.server.GroupStanza parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static com.halloapp.proto.server.GroupStanza parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static com.halloapp.proto.server.GroupStanza parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input);
  }
  public static com.halloapp.proto.server.GroupStanza parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input, extensionRegistry);
  }
  public static com.halloapp.proto.server.GroupStanza parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return parseDelimitedFrom(DEFAULT_INSTANCE, input);
  }
  public static com.halloapp.proto.server.GroupStanza parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return parseDelimitedFrom(DEFAULT_INSTANCE, input, extensionRegistry);
  }
  public static com.halloapp.proto.server.GroupStanza parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input);
  }
  public static com.halloapp.proto.server.GroupStanza parseFrom(
      com.google.protobuf.CodedInputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input, extensionRegistry);
  }

  public static Builder newBuilder() {
    return (Builder) DEFAULT_INSTANCE.createBuilder();
  }
  public static Builder newBuilder(com.halloapp.proto.server.GroupStanza prototype) {
    return (Builder) DEFAULT_INSTANCE.createBuilder(prototype);
  }

  /**
   * Protobuf type {@code server.GroupStanza}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessageLite.Builder<
        com.halloapp.proto.server.GroupStanza, Builder> implements
      // @@protoc_insertion_point(builder_implements:server.GroupStanza)
      com.halloapp.proto.server.GroupStanzaOrBuilder {
    // Construct using com.halloapp.proto.server.GroupStanza.newBuilder()
    private Builder() {
      super(DEFAULT_INSTANCE);
    }


    /**
     * <code>.server.GroupStanza.Action action = 1;</code>
     * @return The enum numeric value on the wire for action.
     */
    @java.lang.Override
    public int getActionValue() {
      return instance.getActionValue();
    }
    /**
     * <code>.server.GroupStanza.Action action = 1;</code>
     * @param value The action to set.
     * @return This builder for chaining.
     */
    public Builder setActionValue(int value) {
      copyOnWrite();
      instance.setActionValue(value);
      return this;
    }
    /**
     * <code>.server.GroupStanza.Action action = 1;</code>
     * @return The action.
     */
    @java.lang.Override
    public com.halloapp.proto.server.GroupStanza.Action getAction() {
      return instance.getAction();
    }
    /**
     * <code>.server.GroupStanza.Action action = 1;</code>
     * @param value The enum numeric value on the wire for action to set.
     * @return This builder for chaining.
     */
    public Builder setAction(com.halloapp.proto.server.GroupStanza.Action value) {
      copyOnWrite();
      instance.setAction(value);
      return this;
    }
    /**
     * <code>.server.GroupStanza.Action action = 1;</code>
     * @return This builder for chaining.
     */
    public Builder clearAction() {
      copyOnWrite();
      instance.clearAction();
      return this;
    }

    /**
     * <code>string gid = 2;</code>
     * @return The gid.
     */
    @java.lang.Override
    public java.lang.String getGid() {
      return instance.getGid();
    }
    /**
     * <code>string gid = 2;</code>
     * @return The bytes for gid.
     */
    @java.lang.Override
    public com.google.protobuf.ByteString
        getGidBytes() {
      return instance.getGidBytes();
    }
    /**
     * <code>string gid = 2;</code>
     * @param value The gid to set.
     * @return This builder for chaining.
     */
    public Builder setGid(
        java.lang.String value) {
      copyOnWrite();
      instance.setGid(value);
      return this;
    }
    /**
     * <code>string gid = 2;</code>
     * @return This builder for chaining.
     */
    public Builder clearGid() {
      copyOnWrite();
      instance.clearGid();
      return this;
    }
    /**
     * <code>string gid = 2;</code>
     * @param value The bytes for gid to set.
     * @return This builder for chaining.
     */
    public Builder setGidBytes(
        com.google.protobuf.ByteString value) {
      copyOnWrite();
      instance.setGidBytes(value);
      return this;
    }

    /**
     * <code>string name = 3;</code>
     * @return The name.
     */
    @java.lang.Override
    public java.lang.String getName() {
      return instance.getName();
    }
    /**
     * <code>string name = 3;</code>
     * @return The bytes for name.
     */
    @java.lang.Override
    public com.google.protobuf.ByteString
        getNameBytes() {
      return instance.getNameBytes();
    }
    /**
     * <code>string name = 3;</code>
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
     * <code>string name = 3;</code>
     * @return This builder for chaining.
     */
    public Builder clearName() {
      copyOnWrite();
      instance.clearName();
      return this;
    }
    /**
     * <code>string name = 3;</code>
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
     * <code>string avatar_id = 4;</code>
     * @return The avatarId.
     */
    @java.lang.Override
    public java.lang.String getAvatarId() {
      return instance.getAvatarId();
    }
    /**
     * <code>string avatar_id = 4;</code>
     * @return The bytes for avatarId.
     */
    @java.lang.Override
    public com.google.protobuf.ByteString
        getAvatarIdBytes() {
      return instance.getAvatarIdBytes();
    }
    /**
     * <code>string avatar_id = 4;</code>
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
     * <code>string avatar_id = 4;</code>
     * @return This builder for chaining.
     */
    public Builder clearAvatarId() {
      copyOnWrite();
      instance.clearAvatarId();
      return this;
    }
    /**
     * <code>string avatar_id = 4;</code>
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
     * <code>int64 sender_uid = 5;</code>
     * @return The senderUid.
     */
    @java.lang.Override
    public long getSenderUid() {
      return instance.getSenderUid();
    }
    /**
     * <code>int64 sender_uid = 5;</code>
     * @param value The senderUid to set.
     * @return This builder for chaining.
     */
    public Builder setSenderUid(long value) {
      copyOnWrite();
      instance.setSenderUid(value);
      return this;
    }
    /**
     * <code>int64 sender_uid = 5;</code>
     * @return This builder for chaining.
     */
    public Builder clearSenderUid() {
      copyOnWrite();
      instance.clearSenderUid();
      return this;
    }

    /**
     * <code>string sender_name = 6;</code>
     * @return The senderName.
     */
    @java.lang.Override
    public java.lang.String getSenderName() {
      return instance.getSenderName();
    }
    /**
     * <code>string sender_name = 6;</code>
     * @return The bytes for senderName.
     */
    @java.lang.Override
    public com.google.protobuf.ByteString
        getSenderNameBytes() {
      return instance.getSenderNameBytes();
    }
    /**
     * <code>string sender_name = 6;</code>
     * @param value The senderName to set.
     * @return This builder for chaining.
     */
    public Builder setSenderName(
        java.lang.String value) {
      copyOnWrite();
      instance.setSenderName(value);
      return this;
    }
    /**
     * <code>string sender_name = 6;</code>
     * @return This builder for chaining.
     */
    public Builder clearSenderName() {
      copyOnWrite();
      instance.clearSenderName();
      return this;
    }
    /**
     * <code>string sender_name = 6;</code>
     * @param value The bytes for senderName to set.
     * @return This builder for chaining.
     */
    public Builder setSenderNameBytes(
        com.google.protobuf.ByteString value) {
      copyOnWrite();
      instance.setSenderNameBytes(value);
      return this;
    }

    /**
     * <code>repeated .server.GroupMember members = 7;</code>
     */
    @java.lang.Override
    public java.util.List<com.halloapp.proto.server.GroupMember> getMembersList() {
      return java.util.Collections.unmodifiableList(
          instance.getMembersList());
    }
    /**
     * <code>repeated .server.GroupMember members = 7;</code>
     */
    @java.lang.Override
    public int getMembersCount() {
      return instance.getMembersCount();
    }/**
     * <code>repeated .server.GroupMember members = 7;</code>
     */
    @java.lang.Override
    public com.halloapp.proto.server.GroupMember getMembers(int index) {
      return instance.getMembers(index);
    }
    /**
     * <code>repeated .server.GroupMember members = 7;</code>
     */
    public Builder setMembers(
        int index, com.halloapp.proto.server.GroupMember value) {
      copyOnWrite();
      instance.setMembers(index, value);
      return this;
    }
    /**
     * <code>repeated .server.GroupMember members = 7;</code>
     */
    public Builder setMembers(
        int index, com.halloapp.proto.server.GroupMember.Builder builderForValue) {
      copyOnWrite();
      instance.setMembers(index,
          builderForValue.build());
      return this;
    }
    /**
     * <code>repeated .server.GroupMember members = 7;</code>
     */
    public Builder addMembers(com.halloapp.proto.server.GroupMember value) {
      copyOnWrite();
      instance.addMembers(value);
      return this;
    }
    /**
     * <code>repeated .server.GroupMember members = 7;</code>
     */
    public Builder addMembers(
        int index, com.halloapp.proto.server.GroupMember value) {
      copyOnWrite();
      instance.addMembers(index, value);
      return this;
    }
    /**
     * <code>repeated .server.GroupMember members = 7;</code>
     */
    public Builder addMembers(
        com.halloapp.proto.server.GroupMember.Builder builderForValue) {
      copyOnWrite();
      instance.addMembers(builderForValue.build());
      return this;
    }
    /**
     * <code>repeated .server.GroupMember members = 7;</code>
     */
    public Builder addMembers(
        int index, com.halloapp.proto.server.GroupMember.Builder builderForValue) {
      copyOnWrite();
      instance.addMembers(index,
          builderForValue.build());
      return this;
    }
    /**
     * <code>repeated .server.GroupMember members = 7;</code>
     */
    public Builder addAllMembers(
        java.lang.Iterable<? extends com.halloapp.proto.server.GroupMember> values) {
      copyOnWrite();
      instance.addAllMembers(values);
      return this;
    }
    /**
     * <code>repeated .server.GroupMember members = 7;</code>
     */
    public Builder clearMembers() {
      copyOnWrite();
      instance.clearMembers();
      return this;
    }
    /**
     * <code>repeated .server.GroupMember members = 7;</code>
     */
    public Builder removeMembers(int index) {
      copyOnWrite();
      instance.removeMembers(index);
      return this;
    }

    /**
     * <code>string background = 8;</code>
     * @return The background.
     */
    @java.lang.Override
    public java.lang.String getBackground() {
      return instance.getBackground();
    }
    /**
     * <code>string background = 8;</code>
     * @return The bytes for background.
     */
    @java.lang.Override
    public com.google.protobuf.ByteString
        getBackgroundBytes() {
      return instance.getBackgroundBytes();
    }
    /**
     * <code>string background = 8;</code>
     * @param value The background to set.
     * @return This builder for chaining.
     */
    public Builder setBackground(
        java.lang.String value) {
      copyOnWrite();
      instance.setBackground(value);
      return this;
    }
    /**
     * <code>string background = 8;</code>
     * @return This builder for chaining.
     */
    public Builder clearBackground() {
      copyOnWrite();
      instance.clearBackground();
      return this;
    }
    /**
     * <code>string background = 8;</code>
     * @param value The bytes for background to set.
     * @return This builder for chaining.
     */
    public Builder setBackgroundBytes(
        com.google.protobuf.ByteString value) {
      copyOnWrite();
      instance.setBackgroundBytes(value);
      return this;
    }

    /**
     * <code>bytes audience_hash = 9;</code>
     * @return The audienceHash.
     */
    @java.lang.Override
    public com.google.protobuf.ByteString getAudienceHash() {
      return instance.getAudienceHash();
    }
    /**
     * <code>bytes audience_hash = 9;</code>
     * @param value The audienceHash to set.
     * @return This builder for chaining.
     */
    public Builder setAudienceHash(com.google.protobuf.ByteString value) {
      copyOnWrite();
      instance.setAudienceHash(value);
      return this;
    }
    /**
     * <code>bytes audience_hash = 9;</code>
     * @return This builder for chaining.
     */
    public Builder clearAudienceHash() {
      copyOnWrite();
      instance.clearAudienceHash();
      return this;
    }

    // @@protoc_insertion_point(builder_scope:server.GroupStanza)
  }
  @java.lang.Override
  @java.lang.SuppressWarnings({"unchecked", "fallthrough"})
  protected final java.lang.Object dynamicMethod(
      com.google.protobuf.GeneratedMessageLite.MethodToInvoke method,
      java.lang.Object arg0, java.lang.Object arg1) {
    switch (method) {
      case NEW_MUTABLE_INSTANCE: {
        return new com.halloapp.proto.server.GroupStanza();
      }
      case NEW_BUILDER: {
        return new Builder();
      }
      case BUILD_MESSAGE_INFO: {
          java.lang.Object[] objects = new java.lang.Object[] {
            "action_",
            "gid_",
            "name_",
            "avatarId_",
            "senderUid_",
            "senderName_",
            "members_",
            com.halloapp.proto.server.GroupMember.class,
            "background_",
            "audienceHash_",
          };
          java.lang.String info =
              "\u0000\t\u0000\u0000\u0001\t\t\u0000\u0001\u0000\u0001\f\u0002\u0208\u0003\u0208" +
              "\u0004\u0208\u0005\u0002\u0006\u0208\u0007\u001b\b\u0208\t\n";
          return newMessageInfo(DEFAULT_INSTANCE, info, objects);
      }
      // fall through
      case GET_DEFAULT_INSTANCE: {
        return DEFAULT_INSTANCE;
      }
      case GET_PARSER: {
        com.google.protobuf.Parser<com.halloapp.proto.server.GroupStanza> parser = PARSER;
        if (parser == null) {
          synchronized (com.halloapp.proto.server.GroupStanza.class) {
            parser = PARSER;
            if (parser == null) {
              parser =
                  new DefaultInstanceBasedParser<com.halloapp.proto.server.GroupStanza>(
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


  // @@protoc_insertion_point(class_scope:server.GroupStanza)
  private static final com.halloapp.proto.server.GroupStanza DEFAULT_INSTANCE;
  static {
    GroupStanza defaultInstance = new GroupStanza();
    // New instances are implicitly immutable so no need to make
    // immutable.
    DEFAULT_INSTANCE = defaultInstance;
    com.google.protobuf.GeneratedMessageLite.registerDefaultInstance(
      GroupStanza.class, defaultInstance);
  }

  public static com.halloapp.proto.server.GroupStanza getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static volatile com.google.protobuf.Parser<GroupStanza> PARSER;

  public static com.google.protobuf.Parser<GroupStanza> parser() {
    return DEFAULT_INSTANCE.getParserForType();
  }
}

