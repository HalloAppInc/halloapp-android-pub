// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: server.proto

package com.halloapp.proto.server;

/**
 * Protobuf type {@code server.SuggestedProfile}
 */
public  final class SuggestedProfile extends
    com.google.protobuf.GeneratedMessageLite<
        SuggestedProfile, SuggestedProfile.Builder> implements
    // @@protoc_insertion_point(message_implements:server.SuggestedProfile)
    SuggestedProfileOrBuilder {
  private SuggestedProfile() {
  }
  /**
   * Protobuf enum {@code server.SuggestedProfile.Reason}
   */
  public enum Reason
      implements com.google.protobuf.Internal.EnumLite {
    /**
     * <code>UNKNOWN_REASON = 0;</code>
     */
    UNKNOWN_REASON(0),
    /**
     * <code>DIRECT_CONTACT = 1;</code>
     */
    DIRECT_CONTACT(1),
    /**
     * <code>FOF = 2;</code>
     */
    FOF(2),
    /**
     * <code>CAMPUS = 3;</code>
     */
    CAMPUS(3),
    UNRECOGNIZED(-1),
    ;

    /**
     * <code>UNKNOWN_REASON = 0;</code>
     */
    public static final int UNKNOWN_REASON_VALUE = 0;
    /**
     * <code>DIRECT_CONTACT = 1;</code>
     */
    public static final int DIRECT_CONTACT_VALUE = 1;
    /**
     * <code>FOF = 2;</code>
     */
    public static final int FOF_VALUE = 2;
    /**
     * <code>CAMPUS = 3;</code>
     */
    public static final int CAMPUS_VALUE = 3;


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
    public static Reason valueOf(int value) {
      return forNumber(value);
    }

    public static Reason forNumber(int value) {
      switch (value) {
        case 0: return UNKNOWN_REASON;
        case 1: return DIRECT_CONTACT;
        case 2: return FOF;
        case 3: return CAMPUS;
        default: return null;
      }
    }

    public static com.google.protobuf.Internal.EnumLiteMap<Reason>
        internalGetValueMap() {
      return internalValueMap;
    }
    private static final com.google.protobuf.Internal.EnumLiteMap<
        Reason> internalValueMap =
          new com.google.protobuf.Internal.EnumLiteMap<Reason>() {
            @java.lang.Override
            public Reason findValueByNumber(int number) {
              return Reason.forNumber(number);
            }
          };

    public static com.google.protobuf.Internal.EnumVerifier 
        internalGetVerifier() {
      return ReasonVerifier.INSTANCE;
    }

    private static final class ReasonVerifier implements 
         com.google.protobuf.Internal.EnumVerifier { 
            static final com.google.protobuf.Internal.EnumVerifier           INSTANCE = new ReasonVerifier();
            @java.lang.Override
            public boolean isInRange(int number) {
              return Reason.forNumber(number) != null;
            }
          };

    private final int value;

    private Reason(int value) {
      this.value = value;
    }

    // @@protoc_insertion_point(enum_scope:server.SuggestedProfile.Reason)
  }

  public static final int USER_PROFILE_FIELD_NUMBER = 1;
  private com.halloapp.proto.server.BasicUserProfile userProfile_;
  /**
   * <code>.server.BasicUserProfile user_profile = 1;</code>
   */
  @java.lang.Override
  public boolean hasUserProfile() {
    return userProfile_ != null;
  }
  /**
   * <code>.server.BasicUserProfile user_profile = 1;</code>
   */
  @java.lang.Override
  public com.halloapp.proto.server.BasicUserProfile getUserProfile() {
    return userProfile_ == null ? com.halloapp.proto.server.BasicUserProfile.getDefaultInstance() : userProfile_;
  }
  /**
   * <code>.server.BasicUserProfile user_profile = 1;</code>
   */
  private void setUserProfile(com.halloapp.proto.server.BasicUserProfile value) {
    value.getClass();
  userProfile_ = value;
    
    }
  /**
   * <code>.server.BasicUserProfile user_profile = 1;</code>
   */
  @java.lang.SuppressWarnings({"ReferenceEquality"})
  private void mergeUserProfile(com.halloapp.proto.server.BasicUserProfile value) {
    value.getClass();
  if (userProfile_ != null &&
        userProfile_ != com.halloapp.proto.server.BasicUserProfile.getDefaultInstance()) {
      userProfile_ =
        com.halloapp.proto.server.BasicUserProfile.newBuilder(userProfile_).mergeFrom(value).buildPartial();
    } else {
      userProfile_ = value;
    }
    
  }
  /**
   * <code>.server.BasicUserProfile user_profile = 1;</code>
   */
  private void clearUserProfile() {  userProfile_ = null;
    
  }

  public static final int REASON_FIELD_NUMBER = 2;
  private int reason_;
  /**
   * <code>.server.SuggestedProfile.Reason reason = 2;</code>
   * @return The enum numeric value on the wire for reason.
   */
  @java.lang.Override
  public int getReasonValue() {
    return reason_;
  }
  /**
   * <code>.server.SuggestedProfile.Reason reason = 2;</code>
   * @return The reason.
   */
  @java.lang.Override
  public com.halloapp.proto.server.SuggestedProfile.Reason getReason() {
    com.halloapp.proto.server.SuggestedProfile.Reason result = com.halloapp.proto.server.SuggestedProfile.Reason.forNumber(reason_);
    return result == null ? com.halloapp.proto.server.SuggestedProfile.Reason.UNRECOGNIZED : result;
  }
  /**
   * <code>.server.SuggestedProfile.Reason reason = 2;</code>
   * @param value The enum numeric value on the wire for reason to set.
   */
  private void setReasonValue(int value) {
      reason_ = value;
  }
  /**
   * <code>.server.SuggestedProfile.Reason reason = 2;</code>
   * @param value The reason to set.
   */
  private void setReason(com.halloapp.proto.server.SuggestedProfile.Reason value) {
    reason_ = value.getNumber();
    
  }
  /**
   * <code>.server.SuggestedProfile.Reason reason = 2;</code>
   */
  private void clearReason() {
    
    reason_ = 0;
  }

  public static final int RANK_FIELD_NUMBER = 3;
  private int rank_;
  /**
   * <pre>
   * Lower rank is better.
   * </pre>
   *
   * <code>int32 rank = 3;</code>
   * @return The rank.
   */
  @java.lang.Override
  public int getRank() {
    return rank_;
  }
  /**
   * <pre>
   * Lower rank is better.
   * </pre>
   *
   * <code>int32 rank = 3;</code>
   * @param value The rank to set.
   */
  private void setRank(int value) {
    
    rank_ = value;
  }
  /**
   * <pre>
   * Lower rank is better.
   * </pre>
   *
   * <code>int32 rank = 3;</code>
   */
  private void clearRank() {
    
    rank_ = 0;
  }

  public static com.halloapp.proto.server.SuggestedProfile parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static com.halloapp.proto.server.SuggestedProfile parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static com.halloapp.proto.server.SuggestedProfile parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static com.halloapp.proto.server.SuggestedProfile parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static com.halloapp.proto.server.SuggestedProfile parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static com.halloapp.proto.server.SuggestedProfile parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static com.halloapp.proto.server.SuggestedProfile parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input);
  }
  public static com.halloapp.proto.server.SuggestedProfile parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input, extensionRegistry);
  }
  public static com.halloapp.proto.server.SuggestedProfile parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return parseDelimitedFrom(DEFAULT_INSTANCE, input);
  }
  public static com.halloapp.proto.server.SuggestedProfile parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return parseDelimitedFrom(DEFAULT_INSTANCE, input, extensionRegistry);
  }
  public static com.halloapp.proto.server.SuggestedProfile parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input);
  }
  public static com.halloapp.proto.server.SuggestedProfile parseFrom(
      com.google.protobuf.CodedInputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input, extensionRegistry);
  }

  public static Builder newBuilder() {
    return (Builder) DEFAULT_INSTANCE.createBuilder();
  }
  public static Builder newBuilder(com.halloapp.proto.server.SuggestedProfile prototype) {
    return (Builder) DEFAULT_INSTANCE.createBuilder(prototype);
  }

  /**
   * Protobuf type {@code server.SuggestedProfile}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessageLite.Builder<
        com.halloapp.proto.server.SuggestedProfile, Builder> implements
      // @@protoc_insertion_point(builder_implements:server.SuggestedProfile)
      com.halloapp.proto.server.SuggestedProfileOrBuilder {
    // Construct using com.halloapp.proto.server.SuggestedProfile.newBuilder()
    private Builder() {
      super(DEFAULT_INSTANCE);
    }


    /**
     * <code>.server.BasicUserProfile user_profile = 1;</code>
     */
    @java.lang.Override
    public boolean hasUserProfile() {
      return instance.hasUserProfile();
    }
    /**
     * <code>.server.BasicUserProfile user_profile = 1;</code>
     */
    @java.lang.Override
    public com.halloapp.proto.server.BasicUserProfile getUserProfile() {
      return instance.getUserProfile();
    }
    /**
     * <code>.server.BasicUserProfile user_profile = 1;</code>
     */
    public Builder setUserProfile(com.halloapp.proto.server.BasicUserProfile value) {
      copyOnWrite();
      instance.setUserProfile(value);
      return this;
      }
    /**
     * <code>.server.BasicUserProfile user_profile = 1;</code>
     */
    public Builder setUserProfile(
        com.halloapp.proto.server.BasicUserProfile.Builder builderForValue) {
      copyOnWrite();
      instance.setUserProfile(builderForValue.build());
      return this;
    }
    /**
     * <code>.server.BasicUserProfile user_profile = 1;</code>
     */
    public Builder mergeUserProfile(com.halloapp.proto.server.BasicUserProfile value) {
      copyOnWrite();
      instance.mergeUserProfile(value);
      return this;
    }
    /**
     * <code>.server.BasicUserProfile user_profile = 1;</code>
     */
    public Builder clearUserProfile() {  copyOnWrite();
      instance.clearUserProfile();
      return this;
    }

    /**
     * <code>.server.SuggestedProfile.Reason reason = 2;</code>
     * @return The enum numeric value on the wire for reason.
     */
    @java.lang.Override
    public int getReasonValue() {
      return instance.getReasonValue();
    }
    /**
     * <code>.server.SuggestedProfile.Reason reason = 2;</code>
     * @param value The reason to set.
     * @return This builder for chaining.
     */
    public Builder setReasonValue(int value) {
      copyOnWrite();
      instance.setReasonValue(value);
      return this;
    }
    /**
     * <code>.server.SuggestedProfile.Reason reason = 2;</code>
     * @return The reason.
     */
    @java.lang.Override
    public com.halloapp.proto.server.SuggestedProfile.Reason getReason() {
      return instance.getReason();
    }
    /**
     * <code>.server.SuggestedProfile.Reason reason = 2;</code>
     * @param value The enum numeric value on the wire for reason to set.
     * @return This builder for chaining.
     */
    public Builder setReason(com.halloapp.proto.server.SuggestedProfile.Reason value) {
      copyOnWrite();
      instance.setReason(value);
      return this;
    }
    /**
     * <code>.server.SuggestedProfile.Reason reason = 2;</code>
     * @return This builder for chaining.
     */
    public Builder clearReason() {
      copyOnWrite();
      instance.clearReason();
      return this;
    }

    /**
     * <pre>
     * Lower rank is better.
     * </pre>
     *
     * <code>int32 rank = 3;</code>
     * @return The rank.
     */
    @java.lang.Override
    public int getRank() {
      return instance.getRank();
    }
    /**
     * <pre>
     * Lower rank is better.
     * </pre>
     *
     * <code>int32 rank = 3;</code>
     * @param value The rank to set.
     * @return This builder for chaining.
     */
    public Builder setRank(int value) {
      copyOnWrite();
      instance.setRank(value);
      return this;
    }
    /**
     * <pre>
     * Lower rank is better.
     * </pre>
     *
     * <code>int32 rank = 3;</code>
     * @return This builder for chaining.
     */
    public Builder clearRank() {
      copyOnWrite();
      instance.clearRank();
      return this;
    }

    // @@protoc_insertion_point(builder_scope:server.SuggestedProfile)
  }
  @java.lang.Override
  @java.lang.SuppressWarnings({"unchecked", "fallthrough"})
  protected final java.lang.Object dynamicMethod(
      com.google.protobuf.GeneratedMessageLite.MethodToInvoke method,
      java.lang.Object arg0, java.lang.Object arg1) {
    switch (method) {
      case NEW_MUTABLE_INSTANCE: {
        return new com.halloapp.proto.server.SuggestedProfile();
      }
      case NEW_BUILDER: {
        return new Builder();
      }
      case BUILD_MESSAGE_INFO: {
          java.lang.Object[] objects = new java.lang.Object[] {
            "userProfile_",
            "reason_",
            "rank_",
          };
          java.lang.String info =
              "\u0000\u0003\u0000\u0000\u0001\u0003\u0003\u0000\u0000\u0000\u0001\t\u0002\f\u0003" +
              "\u0004";
          return newMessageInfo(DEFAULT_INSTANCE, info, objects);
      }
      // fall through
      case GET_DEFAULT_INSTANCE: {
        return DEFAULT_INSTANCE;
      }
      case GET_PARSER: {
        com.google.protobuf.Parser<com.halloapp.proto.server.SuggestedProfile> parser = PARSER;
        if (parser == null) {
          synchronized (com.halloapp.proto.server.SuggestedProfile.class) {
            parser = PARSER;
            if (parser == null) {
              parser =
                  new DefaultInstanceBasedParser<com.halloapp.proto.server.SuggestedProfile>(
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


  // @@protoc_insertion_point(class_scope:server.SuggestedProfile)
  private static final com.halloapp.proto.server.SuggestedProfile DEFAULT_INSTANCE;
  static {
    SuggestedProfile defaultInstance = new SuggestedProfile();
    // New instances are implicitly immutable so no need to make
    // immutable.
    DEFAULT_INSTANCE = defaultInstance;
    com.google.protobuf.GeneratedMessageLite.registerDefaultInstance(
      SuggestedProfile.class, defaultInstance);
  }

  public static com.halloapp.proto.server.SuggestedProfile getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static volatile com.google.protobuf.Parser<SuggestedProfile> PARSER;

  public static com.google.protobuf.Parser<SuggestedProfile> parser() {
    return DEFAULT_INSTANCE.getParserForType();
  }
}

