// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: server.proto

package com.halloapp.proto.server;

/**
 * Protobuf type {@code server.HalloappUserProfile}
 */
public  final class HalloappUserProfile extends
    com.google.protobuf.GeneratedMessageLite<
        HalloappUserProfile, HalloappUserProfile.Builder> implements
    // @@protoc_insertion_point(message_implements:server.HalloappUserProfile)
    HalloappUserProfileOrBuilder {
  private HalloappUserProfile() {
    username_ = "";
    name_ = "";
    avatarId_ = "";
  }
  public static final int UID_FIELD_NUMBER = 1;
  private long uid_;
  /**
   * <code>int64 uid = 1;</code>
   * @return The uid.
   */
  @java.lang.Override
  public long getUid() {
    return uid_;
  }
  /**
   * <code>int64 uid = 1;</code>
   * @param value The uid to set.
   */
  private void setUid(long value) {
    
    uid_ = value;
  }
  /**
   * <code>int64 uid = 1;</code>
   */
  private void clearUid() {
    
    uid_ = 0L;
  }

  public static final int USERNAME_FIELD_NUMBER = 2;
  private java.lang.String username_;
  /**
   * <code>string username = 2;</code>
   * @return The username.
   */
  @java.lang.Override
  public java.lang.String getUsername() {
    return username_;
  }
  /**
   * <code>string username = 2;</code>
   * @return The bytes for username.
   */
  @java.lang.Override
  public com.google.protobuf.ByteString
      getUsernameBytes() {
    return com.google.protobuf.ByteString.copyFromUtf8(username_);
  }
  /**
   * <code>string username = 2;</code>
   * @param value The username to set.
   */
  private void setUsername(
      java.lang.String value) {
    value.getClass();
  
    username_ = value;
  }
  /**
   * <code>string username = 2;</code>
   */
  private void clearUsername() {
    
    username_ = getDefaultInstance().getUsername();
  }
  /**
   * <code>string username = 2;</code>
   * @param value The bytes for username to set.
   */
  private void setUsernameBytes(
      com.google.protobuf.ByteString value) {
    checkByteStringIsUtf8(value);
    username_ = value.toStringUtf8();
    
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

  public static final int STATUS_FIELD_NUMBER = 5;
  private int status_;
  /**
   * <code>.server.FriendshipStatus status = 5;</code>
   * @return The enum numeric value on the wire for status.
   */
  @java.lang.Override
  public int getStatusValue() {
    return status_;
  }
  /**
   * <code>.server.FriendshipStatus status = 5;</code>
   * @return The status.
   */
  @java.lang.Override
  public com.halloapp.proto.server.FriendshipStatus getStatus() {
    com.halloapp.proto.server.FriendshipStatus result = com.halloapp.proto.server.FriendshipStatus.forNumber(status_);
    return result == null ? com.halloapp.proto.server.FriendshipStatus.UNRECOGNIZED : result;
  }
  /**
   * <code>.server.FriendshipStatus status = 5;</code>
   * @param value The enum numeric value on the wire for status to set.
   */
  private void setStatusValue(int value) {
      status_ = value;
  }
  /**
   * <code>.server.FriendshipStatus status = 5;</code>
   * @param value The status to set.
   */
  private void setStatus(com.halloapp.proto.server.FriendshipStatus value) {
    status_ = value.getNumber();
    
  }
  /**
   * <code>.server.FriendshipStatus status = 5;</code>
   */
  private void clearStatus() {
    
    status_ = 0;
  }

  public static final int BLOCKED_FIELD_NUMBER = 6;
  private boolean blocked_;
  /**
   * <code>bool blocked = 6;</code>
   * @return The blocked.
   */
  @java.lang.Override
  public boolean getBlocked() {
    return blocked_;
  }
  /**
   * <code>bool blocked = 6;</code>
   * @param value The blocked to set.
   */
  private void setBlocked(boolean value) {
    
    blocked_ = value;
  }
  /**
   * <code>bool blocked = 6;</code>
   */
  private void clearBlocked() {
    
    blocked_ = false;
  }

  public static com.halloapp.proto.server.HalloappUserProfile parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static com.halloapp.proto.server.HalloappUserProfile parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static com.halloapp.proto.server.HalloappUserProfile parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static com.halloapp.proto.server.HalloappUserProfile parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static com.halloapp.proto.server.HalloappUserProfile parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static com.halloapp.proto.server.HalloappUserProfile parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static com.halloapp.proto.server.HalloappUserProfile parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input);
  }
  public static com.halloapp.proto.server.HalloappUserProfile parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input, extensionRegistry);
  }
  public static com.halloapp.proto.server.HalloappUserProfile parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return parseDelimitedFrom(DEFAULT_INSTANCE, input);
  }
  public static com.halloapp.proto.server.HalloappUserProfile parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return parseDelimitedFrom(DEFAULT_INSTANCE, input, extensionRegistry);
  }
  public static com.halloapp.proto.server.HalloappUserProfile parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input);
  }
  public static com.halloapp.proto.server.HalloappUserProfile parseFrom(
      com.google.protobuf.CodedInputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input, extensionRegistry);
  }

  public static Builder newBuilder() {
    return (Builder) DEFAULT_INSTANCE.createBuilder();
  }
  public static Builder newBuilder(com.halloapp.proto.server.HalloappUserProfile prototype) {
    return (Builder) DEFAULT_INSTANCE.createBuilder(prototype);
  }

  /**
   * Protobuf type {@code server.HalloappUserProfile}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessageLite.Builder<
        com.halloapp.proto.server.HalloappUserProfile, Builder> implements
      // @@protoc_insertion_point(builder_implements:server.HalloappUserProfile)
      com.halloapp.proto.server.HalloappUserProfileOrBuilder {
    // Construct using com.halloapp.proto.server.HalloappUserProfile.newBuilder()
    private Builder() {
      super(DEFAULT_INSTANCE);
    }


    /**
     * <code>int64 uid = 1;</code>
     * @return The uid.
     */
    @java.lang.Override
    public long getUid() {
      return instance.getUid();
    }
    /**
     * <code>int64 uid = 1;</code>
     * @param value The uid to set.
     * @return This builder for chaining.
     */
    public Builder setUid(long value) {
      copyOnWrite();
      instance.setUid(value);
      return this;
    }
    /**
     * <code>int64 uid = 1;</code>
     * @return This builder for chaining.
     */
    public Builder clearUid() {
      copyOnWrite();
      instance.clearUid();
      return this;
    }

    /**
     * <code>string username = 2;</code>
     * @return The username.
     */
    @java.lang.Override
    public java.lang.String getUsername() {
      return instance.getUsername();
    }
    /**
     * <code>string username = 2;</code>
     * @return The bytes for username.
     */
    @java.lang.Override
    public com.google.protobuf.ByteString
        getUsernameBytes() {
      return instance.getUsernameBytes();
    }
    /**
     * <code>string username = 2;</code>
     * @param value The username to set.
     * @return This builder for chaining.
     */
    public Builder setUsername(
        java.lang.String value) {
      copyOnWrite();
      instance.setUsername(value);
      return this;
    }
    /**
     * <code>string username = 2;</code>
     * @return This builder for chaining.
     */
    public Builder clearUsername() {
      copyOnWrite();
      instance.clearUsername();
      return this;
    }
    /**
     * <code>string username = 2;</code>
     * @param value The bytes for username to set.
     * @return This builder for chaining.
     */
    public Builder setUsernameBytes(
        com.google.protobuf.ByteString value) {
      copyOnWrite();
      instance.setUsernameBytes(value);
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
     * <code>.server.FriendshipStatus status = 5;</code>
     * @return The enum numeric value on the wire for status.
     */
    @java.lang.Override
    public int getStatusValue() {
      return instance.getStatusValue();
    }
    /**
     * <code>.server.FriendshipStatus status = 5;</code>
     * @param value The status to set.
     * @return This builder for chaining.
     */
    public Builder setStatusValue(int value) {
      copyOnWrite();
      instance.setStatusValue(value);
      return this;
    }
    /**
     * <code>.server.FriendshipStatus status = 5;</code>
     * @return The status.
     */
    @java.lang.Override
    public com.halloapp.proto.server.FriendshipStatus getStatus() {
      return instance.getStatus();
    }
    /**
     * <code>.server.FriendshipStatus status = 5;</code>
     * @param value The enum numeric value on the wire for status to set.
     * @return This builder for chaining.
     */
    public Builder setStatus(com.halloapp.proto.server.FriendshipStatus value) {
      copyOnWrite();
      instance.setStatus(value);
      return this;
    }
    /**
     * <code>.server.FriendshipStatus status = 5;</code>
     * @return This builder for chaining.
     */
    public Builder clearStatus() {
      copyOnWrite();
      instance.clearStatus();
      return this;
    }

    /**
     * <code>bool blocked = 6;</code>
     * @return The blocked.
     */
    @java.lang.Override
    public boolean getBlocked() {
      return instance.getBlocked();
    }
    /**
     * <code>bool blocked = 6;</code>
     * @param value The blocked to set.
     * @return This builder for chaining.
     */
    public Builder setBlocked(boolean value) {
      copyOnWrite();
      instance.setBlocked(value);
      return this;
    }
    /**
     * <code>bool blocked = 6;</code>
     * @return This builder for chaining.
     */
    public Builder clearBlocked() {
      copyOnWrite();
      instance.clearBlocked();
      return this;
    }

    // @@protoc_insertion_point(builder_scope:server.HalloappUserProfile)
  }
  @java.lang.Override
  @java.lang.SuppressWarnings({"unchecked", "fallthrough"})
  protected final java.lang.Object dynamicMethod(
      com.google.protobuf.GeneratedMessageLite.MethodToInvoke method,
      java.lang.Object arg0, java.lang.Object arg1) {
    switch (method) {
      case NEW_MUTABLE_INSTANCE: {
        return new com.halloapp.proto.server.HalloappUserProfile();
      }
      case NEW_BUILDER: {
        return new Builder();
      }
      case BUILD_MESSAGE_INFO: {
          java.lang.Object[] objects = new java.lang.Object[] {
            "uid_",
            "username_",
            "name_",
            "avatarId_",
            "status_",
            "blocked_",
          };
          java.lang.String info =
              "\u0000\u0006\u0000\u0000\u0001\u0006\u0006\u0000\u0000\u0000\u0001\u0002\u0002\u0208" +
              "\u0003\u0208\u0004\u0208\u0005\f\u0006\u0007";
          return newMessageInfo(DEFAULT_INSTANCE, info, objects);
      }
      // fall through
      case GET_DEFAULT_INSTANCE: {
        return DEFAULT_INSTANCE;
      }
      case GET_PARSER: {
        com.google.protobuf.Parser<com.halloapp.proto.server.HalloappUserProfile> parser = PARSER;
        if (parser == null) {
          synchronized (com.halloapp.proto.server.HalloappUserProfile.class) {
            parser = PARSER;
            if (parser == null) {
              parser =
                  new DefaultInstanceBasedParser<com.halloapp.proto.server.HalloappUserProfile>(
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


  // @@protoc_insertion_point(class_scope:server.HalloappUserProfile)
  private static final com.halloapp.proto.server.HalloappUserProfile DEFAULT_INSTANCE;
  static {
    HalloappUserProfile defaultInstance = new HalloappUserProfile();
    // New instances are implicitly immutable so no need to make
    // immutable.
    DEFAULT_INSTANCE = defaultInstance;
    com.google.protobuf.GeneratedMessageLite.registerDefaultInstance(
      HalloappUserProfile.class, defaultInstance);
  }

  public static com.halloapp.proto.server.HalloappUserProfile getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static volatile com.google.protobuf.Parser<HalloappUserProfile> PARSER;

  public static com.google.protobuf.Parser<HalloappUserProfile> parser() {
    return DEFAULT_INSTANCE.getParserForType();
  }
}
