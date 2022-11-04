// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: web.proto

package com.halloapp.proto.web;

/**
 * Protobuf type {@code web.GroupDisplayInfo}
 */
public  final class GroupDisplayInfo extends
    com.google.protobuf.GeneratedMessageLite<
        GroupDisplayInfo, GroupDisplayInfo.Builder> implements
    // @@protoc_insertion_point(message_implements:web.GroupDisplayInfo)
    GroupDisplayInfoOrBuilder {
  private GroupDisplayInfo() {
    id_ = "";
    name_ = "";
    avatarId_ = "";
    description_ = "";
    background_ = "";
  }
  /**
   * Protobuf enum {@code web.GroupDisplayInfo.MembershipStatus}
   */
  public enum MembershipStatus
      implements com.google.protobuf.Internal.EnumLite {
    /**
     * <code>UNKNOWN = 0;</code>
     */
    UNKNOWN(0),
    /**
     * <code>NOT_MEMBER = 1;</code>
     */
    NOT_MEMBER(1),
    /**
     * <code>MEMBER = 2;</code>
     */
    MEMBER(2),
    /**
     * <code>ADMIN = 3;</code>
     */
    ADMIN(3),
    UNRECOGNIZED(-1),
    ;

    /**
     * <code>UNKNOWN = 0;</code>
     */
    public static final int UNKNOWN_VALUE = 0;
    /**
     * <code>NOT_MEMBER = 1;</code>
     */
    public static final int NOT_MEMBER_VALUE = 1;
    /**
     * <code>MEMBER = 2;</code>
     */
    public static final int MEMBER_VALUE = 2;
    /**
     * <code>ADMIN = 3;</code>
     */
    public static final int ADMIN_VALUE = 3;


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
    public static MembershipStatus valueOf(int value) {
      return forNumber(value);
    }

    public static MembershipStatus forNumber(int value) {
      switch (value) {
        case 0: return UNKNOWN;
        case 1: return NOT_MEMBER;
        case 2: return MEMBER;
        case 3: return ADMIN;
        default: return null;
      }
    }

    public static com.google.protobuf.Internal.EnumLiteMap<MembershipStatus>
        internalGetValueMap() {
      return internalValueMap;
    }
    private static final com.google.protobuf.Internal.EnumLiteMap<
        MembershipStatus> internalValueMap =
          new com.google.protobuf.Internal.EnumLiteMap<MembershipStatus>() {
            @java.lang.Override
            public MembershipStatus findValueByNumber(int number) {
              return MembershipStatus.forNumber(number);
            }
          };

    public static com.google.protobuf.Internal.EnumVerifier 
        internalGetVerifier() {
      return MembershipStatusVerifier.INSTANCE;
    }

    private static final class MembershipStatusVerifier implements 
         com.google.protobuf.Internal.EnumVerifier { 
            static final com.google.protobuf.Internal.EnumVerifier           INSTANCE = new MembershipStatusVerifier();
            @java.lang.Override
            public boolean isInRange(int number) {
              return MembershipStatus.forNumber(number) != null;
            }
          };

    private final int value;

    private MembershipStatus(int value) {
      this.value = value;
    }

    // @@protoc_insertion_point(enum_scope:web.GroupDisplayInfo.MembershipStatus)
  }

  public static final int ID_FIELD_NUMBER = 1;
  private java.lang.String id_;
  /**
   * <code>string id = 1;</code>
   * @return The id.
   */
  @java.lang.Override
  public java.lang.String getId() {
    return id_;
  }
  /**
   * <code>string id = 1;</code>
   * @return The bytes for id.
   */
  @java.lang.Override
  public com.google.protobuf.ByteString
      getIdBytes() {
    return com.google.protobuf.ByteString.copyFromUtf8(id_);
  }
  /**
   * <code>string id = 1;</code>
   * @param value The id to set.
   */
  private void setId(
      java.lang.String value) {
    value.getClass();
  
    id_ = value;
  }
  /**
   * <code>string id = 1;</code>
   */
  private void clearId() {
    
    id_ = getDefaultInstance().getId();
  }
  /**
   * <code>string id = 1;</code>
   * @param value The bytes for id to set.
   */
  private void setIdBytes(
      com.google.protobuf.ByteString value) {
    checkByteStringIsUtf8(value);
    id_ = value.toStringUtf8();
    
  }

  public static final int NAME_FIELD_NUMBER = 2;
  private java.lang.String name_;
  /**
   * <code>string name = 2;</code>
   * @return The name.
   */
  @java.lang.Override
  public java.lang.String getName() {
    return name_;
  }
  /**
   * <code>string name = 2;</code>
   * @return The bytes for name.
   */
  @java.lang.Override
  public com.google.protobuf.ByteString
      getNameBytes() {
    return com.google.protobuf.ByteString.copyFromUtf8(name_);
  }
  /**
   * <code>string name = 2;</code>
   * @param value The name to set.
   */
  private void setName(
      java.lang.String value) {
    value.getClass();
  
    name_ = value;
  }
  /**
   * <code>string name = 2;</code>
   */
  private void clearName() {
    
    name_ = getDefaultInstance().getName();
  }
  /**
   * <code>string name = 2;</code>
   * @param value The bytes for name to set.
   */
  private void setNameBytes(
      com.google.protobuf.ByteString value) {
    checkByteStringIsUtf8(value);
    name_ = value.toStringUtf8();
    
  }

  public static final int AVATAR_ID_FIELD_NUMBER = 3;
  private java.lang.String avatarId_;
  /**
   * <code>string avatar_id = 3;</code>
   * @return The avatarId.
   */
  @java.lang.Override
  public java.lang.String getAvatarId() {
    return avatarId_;
  }
  /**
   * <code>string avatar_id = 3;</code>
   * @return The bytes for avatarId.
   */
  @java.lang.Override
  public com.google.protobuf.ByteString
      getAvatarIdBytes() {
    return com.google.protobuf.ByteString.copyFromUtf8(avatarId_);
  }
  /**
   * <code>string avatar_id = 3;</code>
   * @param value The avatarId to set.
   */
  private void setAvatarId(
      java.lang.String value) {
    value.getClass();
  
    avatarId_ = value;
  }
  /**
   * <code>string avatar_id = 3;</code>
   */
  private void clearAvatarId() {
    
    avatarId_ = getDefaultInstance().getAvatarId();
  }
  /**
   * <code>string avatar_id = 3;</code>
   * @param value The bytes for avatarId to set.
   */
  private void setAvatarIdBytes(
      com.google.protobuf.ByteString value) {
    checkByteStringIsUtf8(value);
    avatarId_ = value.toStringUtf8();
    
  }

  public static final int DESCRIPTION_FIELD_NUMBER = 4;
  private java.lang.String description_;
  /**
   * <code>string description = 4;</code>
   * @return The description.
   */
  @java.lang.Override
  public java.lang.String getDescription() {
    return description_;
  }
  /**
   * <code>string description = 4;</code>
   * @return The bytes for description.
   */
  @java.lang.Override
  public com.google.protobuf.ByteString
      getDescriptionBytes() {
    return com.google.protobuf.ByteString.copyFromUtf8(description_);
  }
  /**
   * <code>string description = 4;</code>
   * @param value The description to set.
   */
  private void setDescription(
      java.lang.String value) {
    value.getClass();
  
    description_ = value;
  }
  /**
   * <code>string description = 4;</code>
   */
  private void clearDescription() {
    
    description_ = getDefaultInstance().getDescription();
  }
  /**
   * <code>string description = 4;</code>
   * @param value The bytes for description to set.
   */
  private void setDescriptionBytes(
      com.google.protobuf.ByteString value) {
    checkByteStringIsUtf8(value);
    description_ = value.toStringUtf8();
    
  }

  public static final int BACKGROUND_FIELD_NUMBER = 5;
  private java.lang.String background_;
  /**
   * <code>string background = 5;</code>
   * @return The background.
   */
  @java.lang.Override
  public java.lang.String getBackground() {
    return background_;
  }
  /**
   * <code>string background = 5;</code>
   * @return The bytes for background.
   */
  @java.lang.Override
  public com.google.protobuf.ByteString
      getBackgroundBytes() {
    return com.google.protobuf.ByteString.copyFromUtf8(background_);
  }
  /**
   * <code>string background = 5;</code>
   * @param value The background to set.
   */
  private void setBackground(
      java.lang.String value) {
    value.getClass();
  
    background_ = value;
  }
  /**
   * <code>string background = 5;</code>
   */
  private void clearBackground() {
    
    background_ = getDefaultInstance().getBackground();
  }
  /**
   * <code>string background = 5;</code>
   * @param value The bytes for background to set.
   */
  private void setBackgroundBytes(
      com.google.protobuf.ByteString value) {
    checkByteStringIsUtf8(value);
    background_ = value.toStringUtf8();
    
  }

  public static final int EXPIRY_INFO_FIELD_NUMBER = 6;
  private com.halloapp.proto.server.ExpiryInfo expiryInfo_;
  /**
   * <code>.server.ExpiryInfo expiry_info = 6;</code>
   */
  @java.lang.Override
  public boolean hasExpiryInfo() {
    return expiryInfo_ != null;
  }
  /**
   * <code>.server.ExpiryInfo expiry_info = 6;</code>
   */
  @java.lang.Override
  public com.halloapp.proto.server.ExpiryInfo getExpiryInfo() {
    return expiryInfo_ == null ? com.halloapp.proto.server.ExpiryInfo.getDefaultInstance() : expiryInfo_;
  }
  /**
   * <code>.server.ExpiryInfo expiry_info = 6;</code>
   */
  private void setExpiryInfo(com.halloapp.proto.server.ExpiryInfo value) {
    value.getClass();
  expiryInfo_ = value;
    
    }
  /**
   * <code>.server.ExpiryInfo expiry_info = 6;</code>
   */
  @java.lang.SuppressWarnings({"ReferenceEquality"})
  private void mergeExpiryInfo(com.halloapp.proto.server.ExpiryInfo value) {
    value.getClass();
  if (expiryInfo_ != null &&
        expiryInfo_ != com.halloapp.proto.server.ExpiryInfo.getDefaultInstance()) {
      expiryInfo_ =
        com.halloapp.proto.server.ExpiryInfo.newBuilder(expiryInfo_).mergeFrom(value).buildPartial();
    } else {
      expiryInfo_ = value;
    }
    
  }
  /**
   * <code>.server.ExpiryInfo expiry_info = 6;</code>
   */
  private void clearExpiryInfo() {  expiryInfo_ = null;
    
  }

  public static final int MEMBERSHIP_STATUS_FIELD_NUMBER = 7;
  private int membershipStatus_;
  /**
   * <code>.web.GroupDisplayInfo.MembershipStatus membership_status = 7;</code>
   * @return The enum numeric value on the wire for membershipStatus.
   */
  @java.lang.Override
  public int getMembershipStatusValue() {
    return membershipStatus_;
  }
  /**
   * <code>.web.GroupDisplayInfo.MembershipStatus membership_status = 7;</code>
   * @return The membershipStatus.
   */
  @java.lang.Override
  public com.halloapp.proto.web.GroupDisplayInfo.MembershipStatus getMembershipStatus() {
    com.halloapp.proto.web.GroupDisplayInfo.MembershipStatus result = com.halloapp.proto.web.GroupDisplayInfo.MembershipStatus.forNumber(membershipStatus_);
    return result == null ? com.halloapp.proto.web.GroupDisplayInfo.MembershipStatus.UNRECOGNIZED : result;
  }
  /**
   * <code>.web.GroupDisplayInfo.MembershipStatus membership_status = 7;</code>
   * @param value The enum numeric value on the wire for membershipStatus to set.
   */
  private void setMembershipStatusValue(int value) {
      membershipStatus_ = value;
  }
  /**
   * <code>.web.GroupDisplayInfo.MembershipStatus membership_status = 7;</code>
   * @param value The membershipStatus to set.
   */
  private void setMembershipStatus(com.halloapp.proto.web.GroupDisplayInfo.MembershipStatus value) {
    membershipStatus_ = value.getNumber();
    
  }
  /**
   * <code>.web.GroupDisplayInfo.MembershipStatus membership_status = 7;</code>
   */
  private void clearMembershipStatus() {
    
    membershipStatus_ = 0;
  }

  public static com.halloapp.proto.web.GroupDisplayInfo parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static com.halloapp.proto.web.GroupDisplayInfo parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static com.halloapp.proto.web.GroupDisplayInfo parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static com.halloapp.proto.web.GroupDisplayInfo parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static com.halloapp.proto.web.GroupDisplayInfo parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static com.halloapp.proto.web.GroupDisplayInfo parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static com.halloapp.proto.web.GroupDisplayInfo parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input);
  }
  public static com.halloapp.proto.web.GroupDisplayInfo parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input, extensionRegistry);
  }
  public static com.halloapp.proto.web.GroupDisplayInfo parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return parseDelimitedFrom(DEFAULT_INSTANCE, input);
  }
  public static com.halloapp.proto.web.GroupDisplayInfo parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return parseDelimitedFrom(DEFAULT_INSTANCE, input, extensionRegistry);
  }
  public static com.halloapp.proto.web.GroupDisplayInfo parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input);
  }
  public static com.halloapp.proto.web.GroupDisplayInfo parseFrom(
      com.google.protobuf.CodedInputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input, extensionRegistry);
  }

  public static Builder newBuilder() {
    return (Builder) DEFAULT_INSTANCE.createBuilder();
  }
  public static Builder newBuilder(com.halloapp.proto.web.GroupDisplayInfo prototype) {
    return (Builder) DEFAULT_INSTANCE.createBuilder(prototype);
  }

  /**
   * Protobuf type {@code web.GroupDisplayInfo}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessageLite.Builder<
        com.halloapp.proto.web.GroupDisplayInfo, Builder> implements
      // @@protoc_insertion_point(builder_implements:web.GroupDisplayInfo)
      com.halloapp.proto.web.GroupDisplayInfoOrBuilder {
    // Construct using com.halloapp.proto.web.GroupDisplayInfo.newBuilder()
    private Builder() {
      super(DEFAULT_INSTANCE);
    }


    /**
     * <code>string id = 1;</code>
     * @return The id.
     */
    @java.lang.Override
    public java.lang.String getId() {
      return instance.getId();
    }
    /**
     * <code>string id = 1;</code>
     * @return The bytes for id.
     */
    @java.lang.Override
    public com.google.protobuf.ByteString
        getIdBytes() {
      return instance.getIdBytes();
    }
    /**
     * <code>string id = 1;</code>
     * @param value The id to set.
     * @return This builder for chaining.
     */
    public Builder setId(
        java.lang.String value) {
      copyOnWrite();
      instance.setId(value);
      return this;
    }
    /**
     * <code>string id = 1;</code>
     * @return This builder for chaining.
     */
    public Builder clearId() {
      copyOnWrite();
      instance.clearId();
      return this;
    }
    /**
     * <code>string id = 1;</code>
     * @param value The bytes for id to set.
     * @return This builder for chaining.
     */
    public Builder setIdBytes(
        com.google.protobuf.ByteString value) {
      copyOnWrite();
      instance.setIdBytes(value);
      return this;
    }

    /**
     * <code>string name = 2;</code>
     * @return The name.
     */
    @java.lang.Override
    public java.lang.String getName() {
      return instance.getName();
    }
    /**
     * <code>string name = 2;</code>
     * @return The bytes for name.
     */
    @java.lang.Override
    public com.google.protobuf.ByteString
        getNameBytes() {
      return instance.getNameBytes();
    }
    /**
     * <code>string name = 2;</code>
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
     * <code>string name = 2;</code>
     * @return This builder for chaining.
     */
    public Builder clearName() {
      copyOnWrite();
      instance.clearName();
      return this;
    }
    /**
     * <code>string name = 2;</code>
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
     * <code>string avatar_id = 3;</code>
     * @return The avatarId.
     */
    @java.lang.Override
    public java.lang.String getAvatarId() {
      return instance.getAvatarId();
    }
    /**
     * <code>string avatar_id = 3;</code>
     * @return The bytes for avatarId.
     */
    @java.lang.Override
    public com.google.protobuf.ByteString
        getAvatarIdBytes() {
      return instance.getAvatarIdBytes();
    }
    /**
     * <code>string avatar_id = 3;</code>
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
     * <code>string avatar_id = 3;</code>
     * @return This builder for chaining.
     */
    public Builder clearAvatarId() {
      copyOnWrite();
      instance.clearAvatarId();
      return this;
    }
    /**
     * <code>string avatar_id = 3;</code>
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
     * <code>string description = 4;</code>
     * @return The description.
     */
    @java.lang.Override
    public java.lang.String getDescription() {
      return instance.getDescription();
    }
    /**
     * <code>string description = 4;</code>
     * @return The bytes for description.
     */
    @java.lang.Override
    public com.google.protobuf.ByteString
        getDescriptionBytes() {
      return instance.getDescriptionBytes();
    }
    /**
     * <code>string description = 4;</code>
     * @param value The description to set.
     * @return This builder for chaining.
     */
    public Builder setDescription(
        java.lang.String value) {
      copyOnWrite();
      instance.setDescription(value);
      return this;
    }
    /**
     * <code>string description = 4;</code>
     * @return This builder for chaining.
     */
    public Builder clearDescription() {
      copyOnWrite();
      instance.clearDescription();
      return this;
    }
    /**
     * <code>string description = 4;</code>
     * @param value The bytes for description to set.
     * @return This builder for chaining.
     */
    public Builder setDescriptionBytes(
        com.google.protobuf.ByteString value) {
      copyOnWrite();
      instance.setDescriptionBytes(value);
      return this;
    }

    /**
     * <code>string background = 5;</code>
     * @return The background.
     */
    @java.lang.Override
    public java.lang.String getBackground() {
      return instance.getBackground();
    }
    /**
     * <code>string background = 5;</code>
     * @return The bytes for background.
     */
    @java.lang.Override
    public com.google.protobuf.ByteString
        getBackgroundBytes() {
      return instance.getBackgroundBytes();
    }
    /**
     * <code>string background = 5;</code>
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
     * <code>string background = 5;</code>
     * @return This builder for chaining.
     */
    public Builder clearBackground() {
      copyOnWrite();
      instance.clearBackground();
      return this;
    }
    /**
     * <code>string background = 5;</code>
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
     * <code>.server.ExpiryInfo expiry_info = 6;</code>
     */
    @java.lang.Override
    public boolean hasExpiryInfo() {
      return instance.hasExpiryInfo();
    }
    /**
     * <code>.server.ExpiryInfo expiry_info = 6;</code>
     */
    @java.lang.Override
    public com.halloapp.proto.server.ExpiryInfo getExpiryInfo() {
      return instance.getExpiryInfo();
    }
    /**
     * <code>.server.ExpiryInfo expiry_info = 6;</code>
     */
    public Builder setExpiryInfo(com.halloapp.proto.server.ExpiryInfo value) {
      copyOnWrite();
      instance.setExpiryInfo(value);
      return this;
      }
    /**
     * <code>.server.ExpiryInfo expiry_info = 6;</code>
     */
    public Builder setExpiryInfo(
        com.halloapp.proto.server.ExpiryInfo.Builder builderForValue) {
      copyOnWrite();
      instance.setExpiryInfo(builderForValue.build());
      return this;
    }
    /**
     * <code>.server.ExpiryInfo expiry_info = 6;</code>
     */
    public Builder mergeExpiryInfo(com.halloapp.proto.server.ExpiryInfo value) {
      copyOnWrite();
      instance.mergeExpiryInfo(value);
      return this;
    }
    /**
     * <code>.server.ExpiryInfo expiry_info = 6;</code>
     */
    public Builder clearExpiryInfo() {  copyOnWrite();
      instance.clearExpiryInfo();
      return this;
    }

    /**
     * <code>.web.GroupDisplayInfo.MembershipStatus membership_status = 7;</code>
     * @return The enum numeric value on the wire for membershipStatus.
     */
    @java.lang.Override
    public int getMembershipStatusValue() {
      return instance.getMembershipStatusValue();
    }
    /**
     * <code>.web.GroupDisplayInfo.MembershipStatus membership_status = 7;</code>
     * @param value The membershipStatus to set.
     * @return This builder for chaining.
     */
    public Builder setMembershipStatusValue(int value) {
      copyOnWrite();
      instance.setMembershipStatusValue(value);
      return this;
    }
    /**
     * <code>.web.GroupDisplayInfo.MembershipStatus membership_status = 7;</code>
     * @return The membershipStatus.
     */
    @java.lang.Override
    public com.halloapp.proto.web.GroupDisplayInfo.MembershipStatus getMembershipStatus() {
      return instance.getMembershipStatus();
    }
    /**
     * <code>.web.GroupDisplayInfo.MembershipStatus membership_status = 7;</code>
     * @param value The enum numeric value on the wire for membershipStatus to set.
     * @return This builder for chaining.
     */
    public Builder setMembershipStatus(com.halloapp.proto.web.GroupDisplayInfo.MembershipStatus value) {
      copyOnWrite();
      instance.setMembershipStatus(value);
      return this;
    }
    /**
     * <code>.web.GroupDisplayInfo.MembershipStatus membership_status = 7;</code>
     * @return This builder for chaining.
     */
    public Builder clearMembershipStatus() {
      copyOnWrite();
      instance.clearMembershipStatus();
      return this;
    }

    // @@protoc_insertion_point(builder_scope:web.GroupDisplayInfo)
  }
  @java.lang.Override
  @java.lang.SuppressWarnings({"unchecked", "fallthrough"})
  protected final java.lang.Object dynamicMethod(
      com.google.protobuf.GeneratedMessageLite.MethodToInvoke method,
      java.lang.Object arg0, java.lang.Object arg1) {
    switch (method) {
      case NEW_MUTABLE_INSTANCE: {
        return new com.halloapp.proto.web.GroupDisplayInfo();
      }
      case NEW_BUILDER: {
        return new Builder();
      }
      case BUILD_MESSAGE_INFO: {
          java.lang.Object[] objects = new java.lang.Object[] {
            "id_",
            "name_",
            "avatarId_",
            "description_",
            "background_",
            "expiryInfo_",
            "membershipStatus_",
          };
          java.lang.String info =
              "\u0000\u0007\u0000\u0000\u0001\u0007\u0007\u0000\u0000\u0000\u0001\u0208\u0002\u0208" +
              "\u0003\u0208\u0004\u0208\u0005\u0208\u0006\t\u0007\f";
          return newMessageInfo(DEFAULT_INSTANCE, info, objects);
      }
      // fall through
      case GET_DEFAULT_INSTANCE: {
        return DEFAULT_INSTANCE;
      }
      case GET_PARSER: {
        com.google.protobuf.Parser<com.halloapp.proto.web.GroupDisplayInfo> parser = PARSER;
        if (parser == null) {
          synchronized (com.halloapp.proto.web.GroupDisplayInfo.class) {
            parser = PARSER;
            if (parser == null) {
              parser =
                  new DefaultInstanceBasedParser<com.halloapp.proto.web.GroupDisplayInfo>(
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


  // @@protoc_insertion_point(class_scope:web.GroupDisplayInfo)
  private static final com.halloapp.proto.web.GroupDisplayInfo DEFAULT_INSTANCE;
  static {
    GroupDisplayInfo defaultInstance = new GroupDisplayInfo();
    // New instances are implicitly immutable so no need to make
    // immutable.
    DEFAULT_INSTANCE = defaultInstance;
    com.google.protobuf.GeneratedMessageLite.registerDefaultInstance(
      GroupDisplayInfo.class, defaultInstance);
  }

  public static com.halloapp.proto.web.GroupDisplayInfo getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static volatile com.google.protobuf.Parser<GroupDisplayInfo> PARSER;

  public static com.google.protobuf.Parser<GroupDisplayInfo> parser() {
    return DEFAULT_INSTANCE.getParserForType();
  }
}

