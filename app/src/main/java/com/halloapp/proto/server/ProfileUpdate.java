// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: server.proto

package com.halloapp.proto.server;

/**
 * Protobuf type {@code server.ProfileUpdate}
 */
public  final class ProfileUpdate extends
    com.google.protobuf.GeneratedMessageLite<
        ProfileUpdate, ProfileUpdate.Builder> implements
    // @@protoc_insertion_point(message_implements:server.ProfileUpdate)
    ProfileUpdateOrBuilder {
  private ProfileUpdate() {
  }
  /**
   * Protobuf enum {@code server.ProfileUpdate.Type}
   */
  public enum Type
      implements com.google.protobuf.Internal.EnumLite {
    /**
     * <code>NORMAL = 0;</code>
     */
    NORMAL(0),
    /**
     * <code>DELETE = 1;</code>
     */
    DELETE(1),
    /**
     * <pre>
     * When the uid follows me.
     * </pre>
     *
     * <code>FOLLOWER_NOTICE = 2;</code>
     */
    FOLLOWER_NOTICE(2),
    /**
     * <pre>
     * When my contact joins katchup.
     * </pre>
     *
     * <code>CONTACT_NOTICE = 3;</code>
     */
    CONTACT_NOTICE(3),
    UNRECOGNIZED(-1),
    ;

    /**
     * <code>NORMAL = 0;</code>
     */
    public static final int NORMAL_VALUE = 0;
    /**
     * <code>DELETE = 1;</code>
     */
    public static final int DELETE_VALUE = 1;
    /**
     * <pre>
     * When the uid follows me.
     * </pre>
     *
     * <code>FOLLOWER_NOTICE = 2;</code>
     */
    public static final int FOLLOWER_NOTICE_VALUE = 2;
    /**
     * <pre>
     * When my contact joins katchup.
     * </pre>
     *
     * <code>CONTACT_NOTICE = 3;</code>
     */
    public static final int CONTACT_NOTICE_VALUE = 3;


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
        case 0: return NORMAL;
        case 1: return DELETE;
        case 2: return FOLLOWER_NOTICE;
        case 3: return CONTACT_NOTICE;
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

    // @@protoc_insertion_point(enum_scope:server.ProfileUpdate.Type)
  }

  public static final int TYPE_FIELD_NUMBER = 1;
  private int type_;
  /**
   * <code>.server.ProfileUpdate.Type type = 1;</code>
   * @return The enum numeric value on the wire for type.
   */
  @java.lang.Override
  public int getTypeValue() {
    return type_;
  }
  /**
   * <code>.server.ProfileUpdate.Type type = 1;</code>
   * @return The type.
   */
  @java.lang.Override
  public com.halloapp.proto.server.ProfileUpdate.Type getType() {
    com.halloapp.proto.server.ProfileUpdate.Type result = com.halloapp.proto.server.ProfileUpdate.Type.forNumber(type_);
    return result == null ? com.halloapp.proto.server.ProfileUpdate.Type.UNRECOGNIZED : result;
  }
  /**
   * <code>.server.ProfileUpdate.Type type = 1;</code>
   * @param value The enum numeric value on the wire for type to set.
   */
  private void setTypeValue(int value) {
      type_ = value;
  }
  /**
   * <code>.server.ProfileUpdate.Type type = 1;</code>
   * @param value The type to set.
   */
  private void setType(com.halloapp.proto.server.ProfileUpdate.Type value) {
    type_ = value.getNumber();
    
  }
  /**
   * <code>.server.ProfileUpdate.Type type = 1;</code>
   */
  private void clearType() {
    
    type_ = 0;
  }

  public static final int PROFILE_FIELD_NUMBER = 2;
  private com.halloapp.proto.server.BasicUserProfile profile_;
  /**
   * <code>.server.BasicUserProfile profile = 2;</code>
   */
  @java.lang.Override
  public boolean hasProfile() {
    return profile_ != null;
  }
  /**
   * <code>.server.BasicUserProfile profile = 2;</code>
   */
  @java.lang.Override
  public com.halloapp.proto.server.BasicUserProfile getProfile() {
    return profile_ == null ? com.halloapp.proto.server.BasicUserProfile.getDefaultInstance() : profile_;
  }
  /**
   * <code>.server.BasicUserProfile profile = 2;</code>
   */
  private void setProfile(com.halloapp.proto.server.BasicUserProfile value) {
    value.getClass();
  profile_ = value;
    
    }
  /**
   * <code>.server.BasicUserProfile profile = 2;</code>
   */
  @java.lang.SuppressWarnings({"ReferenceEquality"})
  private void mergeProfile(com.halloapp.proto.server.BasicUserProfile value) {
    value.getClass();
  if (profile_ != null &&
        profile_ != com.halloapp.proto.server.BasicUserProfile.getDefaultInstance()) {
      profile_ =
        com.halloapp.proto.server.BasicUserProfile.newBuilder(profile_).mergeFrom(value).buildPartial();
    } else {
      profile_ = value;
    }
    
  }
  /**
   * <code>.server.BasicUserProfile profile = 2;</code>
   */
  private void clearProfile() {  profile_ = null;
    
  }

  public static com.halloapp.proto.server.ProfileUpdate parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static com.halloapp.proto.server.ProfileUpdate parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static com.halloapp.proto.server.ProfileUpdate parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static com.halloapp.proto.server.ProfileUpdate parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static com.halloapp.proto.server.ProfileUpdate parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static com.halloapp.proto.server.ProfileUpdate parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static com.halloapp.proto.server.ProfileUpdate parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input);
  }
  public static com.halloapp.proto.server.ProfileUpdate parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input, extensionRegistry);
  }
  public static com.halloapp.proto.server.ProfileUpdate parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return parseDelimitedFrom(DEFAULT_INSTANCE, input);
  }
  public static com.halloapp.proto.server.ProfileUpdate parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return parseDelimitedFrom(DEFAULT_INSTANCE, input, extensionRegistry);
  }
  public static com.halloapp.proto.server.ProfileUpdate parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input);
  }
  public static com.halloapp.proto.server.ProfileUpdate parseFrom(
      com.google.protobuf.CodedInputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input, extensionRegistry);
  }

  public static Builder newBuilder() {
    return (Builder) DEFAULT_INSTANCE.createBuilder();
  }
  public static Builder newBuilder(com.halloapp.proto.server.ProfileUpdate prototype) {
    return (Builder) DEFAULT_INSTANCE.createBuilder(prototype);
  }

  /**
   * Protobuf type {@code server.ProfileUpdate}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessageLite.Builder<
        com.halloapp.proto.server.ProfileUpdate, Builder> implements
      // @@protoc_insertion_point(builder_implements:server.ProfileUpdate)
      com.halloapp.proto.server.ProfileUpdateOrBuilder {
    // Construct using com.halloapp.proto.server.ProfileUpdate.newBuilder()
    private Builder() {
      super(DEFAULT_INSTANCE);
    }


    /**
     * <code>.server.ProfileUpdate.Type type = 1;</code>
     * @return The enum numeric value on the wire for type.
     */
    @java.lang.Override
    public int getTypeValue() {
      return instance.getTypeValue();
    }
    /**
     * <code>.server.ProfileUpdate.Type type = 1;</code>
     * @param value The type to set.
     * @return This builder for chaining.
     */
    public Builder setTypeValue(int value) {
      copyOnWrite();
      instance.setTypeValue(value);
      return this;
    }
    /**
     * <code>.server.ProfileUpdate.Type type = 1;</code>
     * @return The type.
     */
    @java.lang.Override
    public com.halloapp.proto.server.ProfileUpdate.Type getType() {
      return instance.getType();
    }
    /**
     * <code>.server.ProfileUpdate.Type type = 1;</code>
     * @param value The enum numeric value on the wire for type to set.
     * @return This builder for chaining.
     */
    public Builder setType(com.halloapp.proto.server.ProfileUpdate.Type value) {
      copyOnWrite();
      instance.setType(value);
      return this;
    }
    /**
     * <code>.server.ProfileUpdate.Type type = 1;</code>
     * @return This builder for chaining.
     */
    public Builder clearType() {
      copyOnWrite();
      instance.clearType();
      return this;
    }

    /**
     * <code>.server.BasicUserProfile profile = 2;</code>
     */
    @java.lang.Override
    public boolean hasProfile() {
      return instance.hasProfile();
    }
    /**
     * <code>.server.BasicUserProfile profile = 2;</code>
     */
    @java.lang.Override
    public com.halloapp.proto.server.BasicUserProfile getProfile() {
      return instance.getProfile();
    }
    /**
     * <code>.server.BasicUserProfile profile = 2;</code>
     */
    public Builder setProfile(com.halloapp.proto.server.BasicUserProfile value) {
      copyOnWrite();
      instance.setProfile(value);
      return this;
      }
    /**
     * <code>.server.BasicUserProfile profile = 2;</code>
     */
    public Builder setProfile(
        com.halloapp.proto.server.BasicUserProfile.Builder builderForValue) {
      copyOnWrite();
      instance.setProfile(builderForValue.build());
      return this;
    }
    /**
     * <code>.server.BasicUserProfile profile = 2;</code>
     */
    public Builder mergeProfile(com.halloapp.proto.server.BasicUserProfile value) {
      copyOnWrite();
      instance.mergeProfile(value);
      return this;
    }
    /**
     * <code>.server.BasicUserProfile profile = 2;</code>
     */
    public Builder clearProfile() {  copyOnWrite();
      instance.clearProfile();
      return this;
    }

    // @@protoc_insertion_point(builder_scope:server.ProfileUpdate)
  }
  @java.lang.Override
  @java.lang.SuppressWarnings({"unchecked", "fallthrough"})
  protected final java.lang.Object dynamicMethod(
      com.google.protobuf.GeneratedMessageLite.MethodToInvoke method,
      java.lang.Object arg0, java.lang.Object arg1) {
    switch (method) {
      case NEW_MUTABLE_INSTANCE: {
        return new com.halloapp.proto.server.ProfileUpdate();
      }
      case NEW_BUILDER: {
        return new Builder();
      }
      case BUILD_MESSAGE_INFO: {
          java.lang.Object[] objects = new java.lang.Object[] {
            "type_",
            "profile_",
          };
          java.lang.String info =
              "\u0000\u0002\u0000\u0000\u0001\u0002\u0002\u0000\u0000\u0000\u0001\f\u0002\t";
          return newMessageInfo(DEFAULT_INSTANCE, info, objects);
      }
      // fall through
      case GET_DEFAULT_INSTANCE: {
        return DEFAULT_INSTANCE;
      }
      case GET_PARSER: {
        com.google.protobuf.Parser<com.halloapp.proto.server.ProfileUpdate> parser = PARSER;
        if (parser == null) {
          synchronized (com.halloapp.proto.server.ProfileUpdate.class) {
            parser = PARSER;
            if (parser == null) {
              parser =
                  new DefaultInstanceBasedParser<com.halloapp.proto.server.ProfileUpdate>(
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


  // @@protoc_insertion_point(class_scope:server.ProfileUpdate)
  private static final com.halloapp.proto.server.ProfileUpdate DEFAULT_INSTANCE;
  static {
    ProfileUpdate defaultInstance = new ProfileUpdate();
    // New instances are implicitly immutable so no need to make
    // immutable.
    DEFAULT_INSTANCE = defaultInstance;
    com.google.protobuf.GeneratedMessageLite.registerDefaultInstance(
      ProfileUpdate.class, defaultInstance);
  }

  public static com.halloapp.proto.server.ProfileUpdate getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static volatile com.google.protobuf.Parser<ProfileUpdate> PARSER;

  public static com.google.protobuf.Parser<ProfileUpdate> parser() {
    return DEFAULT_INSTANCE.getParserForType();
  }
}

