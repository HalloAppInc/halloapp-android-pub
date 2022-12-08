// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: server.proto

package com.halloapp.proto.server;

/**
 * Protobuf type {@code server.RelationshipRequest}
 */
public  final class RelationshipRequest extends
    com.google.protobuf.GeneratedMessageLite<
        RelationshipRequest, RelationshipRequest.Builder> implements
    // @@protoc_insertion_point(message_implements:server.RelationshipRequest)
    RelationshipRequestOrBuilder {
  private RelationshipRequest() {
  }
  /**
   * Protobuf enum {@code server.RelationshipRequest.Action}
   */
  public enum Action
      implements com.google.protobuf.Internal.EnumLite {
    /**
     * <code>FOLLOW = 0;</code>
     */
    FOLLOW(0),
    /**
     * <code>UNFOLLOW = 1;</code>
     */
    UNFOLLOW(1),
    /**
     * <code>ACCEPT_FOLLOW = 2;</code>
     */
    ACCEPT_FOLLOW(2),
    /**
     * <code>IGNORE_FOLLOW = 3;</code>
     */
    IGNORE_FOLLOW(3),
    /**
     * <code>REMOVE_FOLLOWER = 4;</code>
     */
    REMOVE_FOLLOWER(4),
    /**
     * <code>BLOCK = 5;</code>
     */
    BLOCK(5),
    /**
     * <code>UNBLOCK = 6;</code>
     */
    UNBLOCK(6),
    UNRECOGNIZED(-1),
    ;

    /**
     * <code>FOLLOW = 0;</code>
     */
    public static final int FOLLOW_VALUE = 0;
    /**
     * <code>UNFOLLOW = 1;</code>
     */
    public static final int UNFOLLOW_VALUE = 1;
    /**
     * <code>ACCEPT_FOLLOW = 2;</code>
     */
    public static final int ACCEPT_FOLLOW_VALUE = 2;
    /**
     * <code>IGNORE_FOLLOW = 3;</code>
     */
    public static final int IGNORE_FOLLOW_VALUE = 3;
    /**
     * <code>REMOVE_FOLLOWER = 4;</code>
     */
    public static final int REMOVE_FOLLOWER_VALUE = 4;
    /**
     * <code>BLOCK = 5;</code>
     */
    public static final int BLOCK_VALUE = 5;
    /**
     * <code>UNBLOCK = 6;</code>
     */
    public static final int UNBLOCK_VALUE = 6;


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
        case 0: return FOLLOW;
        case 1: return UNFOLLOW;
        case 2: return ACCEPT_FOLLOW;
        case 3: return IGNORE_FOLLOW;
        case 4: return REMOVE_FOLLOWER;
        case 5: return BLOCK;
        case 6: return UNBLOCK;
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

    // @@protoc_insertion_point(enum_scope:server.RelationshipRequest.Action)
  }

  public static final int ACTION_FIELD_NUMBER = 1;
  private int action_;
  /**
   * <code>.server.RelationshipRequest.Action action = 1;</code>
   * @return The enum numeric value on the wire for action.
   */
  @java.lang.Override
  public int getActionValue() {
    return action_;
  }
  /**
   * <code>.server.RelationshipRequest.Action action = 1;</code>
   * @return The action.
   */
  @java.lang.Override
  public com.halloapp.proto.server.RelationshipRequest.Action getAction() {
    com.halloapp.proto.server.RelationshipRequest.Action result = com.halloapp.proto.server.RelationshipRequest.Action.forNumber(action_);
    return result == null ? com.halloapp.proto.server.RelationshipRequest.Action.UNRECOGNIZED : result;
  }
  /**
   * <code>.server.RelationshipRequest.Action action = 1;</code>
   * @param value The enum numeric value on the wire for action to set.
   */
  private void setActionValue(int value) {
      action_ = value;
  }
  /**
   * <code>.server.RelationshipRequest.Action action = 1;</code>
   * @param value The action to set.
   */
  private void setAction(com.halloapp.proto.server.RelationshipRequest.Action value) {
    action_ = value.getNumber();
    
  }
  /**
   * <code>.server.RelationshipRequest.Action action = 1;</code>
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

  public static com.halloapp.proto.server.RelationshipRequest parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static com.halloapp.proto.server.RelationshipRequest parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static com.halloapp.proto.server.RelationshipRequest parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static com.halloapp.proto.server.RelationshipRequest parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static com.halloapp.proto.server.RelationshipRequest parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static com.halloapp.proto.server.RelationshipRequest parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static com.halloapp.proto.server.RelationshipRequest parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input);
  }
  public static com.halloapp.proto.server.RelationshipRequest parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input, extensionRegistry);
  }
  public static com.halloapp.proto.server.RelationshipRequest parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return parseDelimitedFrom(DEFAULT_INSTANCE, input);
  }
  public static com.halloapp.proto.server.RelationshipRequest parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return parseDelimitedFrom(DEFAULT_INSTANCE, input, extensionRegistry);
  }
  public static com.halloapp.proto.server.RelationshipRequest parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input);
  }
  public static com.halloapp.proto.server.RelationshipRequest parseFrom(
      com.google.protobuf.CodedInputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input, extensionRegistry);
  }

  public static Builder newBuilder() {
    return (Builder) DEFAULT_INSTANCE.createBuilder();
  }
  public static Builder newBuilder(com.halloapp.proto.server.RelationshipRequest prototype) {
    return (Builder) DEFAULT_INSTANCE.createBuilder(prototype);
  }

  /**
   * Protobuf type {@code server.RelationshipRequest}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessageLite.Builder<
        com.halloapp.proto.server.RelationshipRequest, Builder> implements
      // @@protoc_insertion_point(builder_implements:server.RelationshipRequest)
      com.halloapp.proto.server.RelationshipRequestOrBuilder {
    // Construct using com.halloapp.proto.server.RelationshipRequest.newBuilder()
    private Builder() {
      super(DEFAULT_INSTANCE);
    }


    /**
     * <code>.server.RelationshipRequest.Action action = 1;</code>
     * @return The enum numeric value on the wire for action.
     */
    @java.lang.Override
    public int getActionValue() {
      return instance.getActionValue();
    }
    /**
     * <code>.server.RelationshipRequest.Action action = 1;</code>
     * @param value The action to set.
     * @return This builder for chaining.
     */
    public Builder setActionValue(int value) {
      copyOnWrite();
      instance.setActionValue(value);
      return this;
    }
    /**
     * <code>.server.RelationshipRequest.Action action = 1;</code>
     * @return The action.
     */
    @java.lang.Override
    public com.halloapp.proto.server.RelationshipRequest.Action getAction() {
      return instance.getAction();
    }
    /**
     * <code>.server.RelationshipRequest.Action action = 1;</code>
     * @param value The enum numeric value on the wire for action to set.
     * @return This builder for chaining.
     */
    public Builder setAction(com.halloapp.proto.server.RelationshipRequest.Action value) {
      copyOnWrite();
      instance.setAction(value);
      return this;
    }
    /**
     * <code>.server.RelationshipRequest.Action action = 1;</code>
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

    // @@protoc_insertion_point(builder_scope:server.RelationshipRequest)
  }
  @java.lang.Override
  @java.lang.SuppressWarnings({"unchecked", "fallthrough"})
  protected final java.lang.Object dynamicMethod(
      com.google.protobuf.GeneratedMessageLite.MethodToInvoke method,
      java.lang.Object arg0, java.lang.Object arg1) {
    switch (method) {
      case NEW_MUTABLE_INSTANCE: {
        return new com.halloapp.proto.server.RelationshipRequest();
      }
      case NEW_BUILDER: {
        return new Builder();
      }
      case BUILD_MESSAGE_INFO: {
          java.lang.Object[] objects = new java.lang.Object[] {
            "action_",
            "uid_",
          };
          java.lang.String info =
              "\u0000\u0002\u0000\u0000\u0001\u0002\u0002\u0000\u0000\u0000\u0001\f\u0002\u0002" +
              "";
          return newMessageInfo(DEFAULT_INSTANCE, info, objects);
      }
      // fall through
      case GET_DEFAULT_INSTANCE: {
        return DEFAULT_INSTANCE;
      }
      case GET_PARSER: {
        com.google.protobuf.Parser<com.halloapp.proto.server.RelationshipRequest> parser = PARSER;
        if (parser == null) {
          synchronized (com.halloapp.proto.server.RelationshipRequest.class) {
            parser = PARSER;
            if (parser == null) {
              parser =
                  new DefaultInstanceBasedParser<com.halloapp.proto.server.RelationshipRequest>(
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


  // @@protoc_insertion_point(class_scope:server.RelationshipRequest)
  private static final com.halloapp.proto.server.RelationshipRequest DEFAULT_INSTANCE;
  static {
    RelationshipRequest defaultInstance = new RelationshipRequest();
    // New instances are implicitly immutable so no need to make
    // immutable.
    DEFAULT_INSTANCE = defaultInstance;
    com.google.protobuf.GeneratedMessageLite.registerDefaultInstance(
      RelationshipRequest.class, defaultInstance);
  }

  public static com.halloapp.proto.server.RelationshipRequest getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static volatile com.google.protobuf.Parser<RelationshipRequest> PARSER;

  public static com.google.protobuf.Parser<RelationshipRequest> parser() {
    return DEFAULT_INSTANCE.getParserForType();
  }
}
