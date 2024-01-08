// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: server.proto

package com.halloapp.proto.server;

/**
 * Protobuf type {@code server.FriendListRequest}
 */
public  final class FriendListRequest extends
    com.google.protobuf.GeneratedMessageLite<
        FriendListRequest, FriendListRequest.Builder> implements
    // @@protoc_insertion_point(message_implements:server.FriendListRequest)
    FriendListRequestOrBuilder {
  private FriendListRequest() {
    cursor_ = "";
  }
  /**
   * Protobuf enum {@code server.FriendListRequest.Action}
   */
  public enum Action
      implements com.google.protobuf.Internal.EnumLite {
    /**
     * <pre>
     * Fetch all friends of the user.
     * </pre>
     *
     * <code>GET_FRIENDS = 0;</code>
     */
    GET_FRIENDS(0),
    /**
     * <pre>
     * Fetch all incoming pending friend requests to the user.
     * </pre>
     *
     * <code>GET_INCOMING_PENDING = 1;</code>
     */
    GET_INCOMING_PENDING(1),
    /**
     * <pre>
     * Fetch all outgoing friend requests from the user.
     * </pre>
     *
     * <code>GET_OUTGOING_PENDING = 2;</code>
     */
    GET_OUTGOING_PENDING(2),
    /**
     * <pre>
     * Fetch all friend suggestions from the user.
     * </pre>
     *
     * <code>GET_SUGGESTIONS = 3;</code>
     */
    GET_SUGGESTIONS(3),
    /**
     * <pre>
     * Fetch blocked users of this user.
     * </pre>
     *
     * <code>GET_BLOCKED = 4;</code>
     */
    GET_BLOCKED(4),
    /**
     * <pre>
     * Set by server_only. Clients should sync all their lists with the server.
     * </pre>
     *
     * <code>SYNC_ALL = 5;</code>
     */
    SYNC_ALL(5),
    UNRECOGNIZED(-1),
    ;

    /**
     * <pre>
     * Fetch all friends of the user.
     * </pre>
     *
     * <code>GET_FRIENDS = 0;</code>
     */
    public static final int GET_FRIENDS_VALUE = 0;
    /**
     * <pre>
     * Fetch all incoming pending friend requests to the user.
     * </pre>
     *
     * <code>GET_INCOMING_PENDING = 1;</code>
     */
    public static final int GET_INCOMING_PENDING_VALUE = 1;
    /**
     * <pre>
     * Fetch all outgoing friend requests from the user.
     * </pre>
     *
     * <code>GET_OUTGOING_PENDING = 2;</code>
     */
    public static final int GET_OUTGOING_PENDING_VALUE = 2;
    /**
     * <pre>
     * Fetch all friend suggestions from the user.
     * </pre>
     *
     * <code>GET_SUGGESTIONS = 3;</code>
     */
    public static final int GET_SUGGESTIONS_VALUE = 3;
    /**
     * <pre>
     * Fetch blocked users of this user.
     * </pre>
     *
     * <code>GET_BLOCKED = 4;</code>
     */
    public static final int GET_BLOCKED_VALUE = 4;
    /**
     * <pre>
     * Set by server_only. Clients should sync all their lists with the server.
     * </pre>
     *
     * <code>SYNC_ALL = 5;</code>
     */
    public static final int SYNC_ALL_VALUE = 5;


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
        case 0: return GET_FRIENDS;
        case 1: return GET_INCOMING_PENDING;
        case 2: return GET_OUTGOING_PENDING;
        case 3: return GET_SUGGESTIONS;
        case 4: return GET_BLOCKED;
        case 5: return SYNC_ALL;
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

    // @@protoc_insertion_point(enum_scope:server.FriendListRequest.Action)
  }

  public static final int ACTION_FIELD_NUMBER = 1;
  private int action_;
  /**
   * <code>.server.FriendListRequest.Action action = 1;</code>
   * @return The enum numeric value on the wire for action.
   */
  @java.lang.Override
  public int getActionValue() {
    return action_;
  }
  /**
   * <code>.server.FriendListRequest.Action action = 1;</code>
   * @return The action.
   */
  @java.lang.Override
  public com.halloapp.proto.server.FriendListRequest.Action getAction() {
    com.halloapp.proto.server.FriendListRequest.Action result = com.halloapp.proto.server.FriendListRequest.Action.forNumber(action_);
    return result == null ? com.halloapp.proto.server.FriendListRequest.Action.UNRECOGNIZED : result;
  }
  /**
   * <code>.server.FriendListRequest.Action action = 1;</code>
   * @param value The enum numeric value on the wire for action to set.
   */
  private void setActionValue(int value) {
      action_ = value;
  }
  /**
   * <code>.server.FriendListRequest.Action action = 1;</code>
   * @param value The action to set.
   */
  private void setAction(com.halloapp.proto.server.FriendListRequest.Action value) {
    action_ = value.getNumber();
    
  }
  /**
   * <code>.server.FriendListRequest.Action action = 1;</code>
   */
  private void clearAction() {
    
    action_ = 0;
  }

  public static final int CURSOR_FIELD_NUMBER = 2;
  private java.lang.String cursor_;
  /**
   * <code>string cursor = 2;</code>
   * @return The cursor.
   */
  @java.lang.Override
  public java.lang.String getCursor() {
    return cursor_;
  }
  /**
   * <code>string cursor = 2;</code>
   * @return The bytes for cursor.
   */
  @java.lang.Override
  public com.google.protobuf.ByteString
      getCursorBytes() {
    return com.google.protobuf.ByteString.copyFromUtf8(cursor_);
  }
  /**
   * <code>string cursor = 2;</code>
   * @param value The cursor to set.
   */
  private void setCursor(
      java.lang.String value) {
    java.lang.Class<?> valueClass = value.getClass();
  
    cursor_ = value;
  }
  /**
   * <code>string cursor = 2;</code>
   */
  private void clearCursor() {
    
    cursor_ = getDefaultInstance().getCursor();
  }
  /**
   * <code>string cursor = 2;</code>
   * @param value The bytes for cursor to set.
   */
  private void setCursorBytes(
      com.google.protobuf.ByteString value) {
    checkByteStringIsUtf8(value);
    cursor_ = value.toStringUtf8();
    
  }

  public static com.halloapp.proto.server.FriendListRequest parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static com.halloapp.proto.server.FriendListRequest parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static com.halloapp.proto.server.FriendListRequest parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static com.halloapp.proto.server.FriendListRequest parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static com.halloapp.proto.server.FriendListRequest parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static com.halloapp.proto.server.FriendListRequest parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static com.halloapp.proto.server.FriendListRequest parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input);
  }
  public static com.halloapp.proto.server.FriendListRequest parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input, extensionRegistry);
  }
  public static com.halloapp.proto.server.FriendListRequest parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return parseDelimitedFrom(DEFAULT_INSTANCE, input);
  }
  public static com.halloapp.proto.server.FriendListRequest parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return parseDelimitedFrom(DEFAULT_INSTANCE, input, extensionRegistry);
  }
  public static com.halloapp.proto.server.FriendListRequest parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input);
  }
  public static com.halloapp.proto.server.FriendListRequest parseFrom(
      com.google.protobuf.CodedInputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input, extensionRegistry);
  }

  public static Builder newBuilder() {
    return (Builder) DEFAULT_INSTANCE.createBuilder();
  }
  public static Builder newBuilder(com.halloapp.proto.server.FriendListRequest prototype) {
    return (Builder) DEFAULT_INSTANCE.createBuilder(prototype);
  }

  /**
   * Protobuf type {@code server.FriendListRequest}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessageLite.Builder<
        com.halloapp.proto.server.FriendListRequest, Builder> implements
      // @@protoc_insertion_point(builder_implements:server.FriendListRequest)
      com.halloapp.proto.server.FriendListRequestOrBuilder {
    // Construct using com.halloapp.proto.server.FriendListRequest.newBuilder()
    private Builder() {
      super(DEFAULT_INSTANCE);
    }


    /**
     * <code>.server.FriendListRequest.Action action = 1;</code>
     * @return The enum numeric value on the wire for action.
     */
    @java.lang.Override
    public int getActionValue() {
      return instance.getActionValue();
    }
    /**
     * <code>.server.FriendListRequest.Action action = 1;</code>
     * @param value The action to set.
     * @return This builder for chaining.
     */
    public Builder setActionValue(int value) {
      copyOnWrite();
      instance.setActionValue(value);
      return this;
    }
    /**
     * <code>.server.FriendListRequest.Action action = 1;</code>
     * @return The action.
     */
    @java.lang.Override
    public com.halloapp.proto.server.FriendListRequest.Action getAction() {
      return instance.getAction();
    }
    /**
     * <code>.server.FriendListRequest.Action action = 1;</code>
     * @param value The enum numeric value on the wire for action to set.
     * @return This builder for chaining.
     */
    public Builder setAction(com.halloapp.proto.server.FriendListRequest.Action value) {
      copyOnWrite();
      instance.setAction(value);
      return this;
    }
    /**
     * <code>.server.FriendListRequest.Action action = 1;</code>
     * @return This builder for chaining.
     */
    public Builder clearAction() {
      copyOnWrite();
      instance.clearAction();
      return this;
    }

    /**
     * <code>string cursor = 2;</code>
     * @return The cursor.
     */
    @java.lang.Override
    public java.lang.String getCursor() {
      return instance.getCursor();
    }
    /**
     * <code>string cursor = 2;</code>
     * @return The bytes for cursor.
     */
    @java.lang.Override
    public com.google.protobuf.ByteString
        getCursorBytes() {
      return instance.getCursorBytes();
    }
    /**
     * <code>string cursor = 2;</code>
     * @param value The cursor to set.
     * @return This builder for chaining.
     */
    public Builder setCursor(
        java.lang.String value) {
      copyOnWrite();
      instance.setCursor(value);
      return this;
    }
    /**
     * <code>string cursor = 2;</code>
     * @return This builder for chaining.
     */
    public Builder clearCursor() {
      copyOnWrite();
      instance.clearCursor();
      return this;
    }
    /**
     * <code>string cursor = 2;</code>
     * @param value The bytes for cursor to set.
     * @return This builder for chaining.
     */
    public Builder setCursorBytes(
        com.google.protobuf.ByteString value) {
      copyOnWrite();
      instance.setCursorBytes(value);
      return this;
    }

    // @@protoc_insertion_point(builder_scope:server.FriendListRequest)
  }
  @java.lang.Override
  @java.lang.SuppressWarnings({"unchecked", "fallthrough"})
  protected final java.lang.Object dynamicMethod(
      com.google.protobuf.GeneratedMessageLite.MethodToInvoke method,
      java.lang.Object arg0, java.lang.Object arg1) {
    switch (method) {
      case NEW_MUTABLE_INSTANCE: {
        return new com.halloapp.proto.server.FriendListRequest();
      }
      case NEW_BUILDER: {
        return new Builder();
      }
      case BUILD_MESSAGE_INFO: {
          java.lang.Object[] objects = new java.lang.Object[] {
            "action_",
            "cursor_",
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
        com.google.protobuf.Parser<com.halloapp.proto.server.FriendListRequest> parser = PARSER;
        if (parser == null) {
          synchronized (com.halloapp.proto.server.FriendListRequest.class) {
            parser = PARSER;
            if (parser == null) {
              parser =
                  new DefaultInstanceBasedParser<com.halloapp.proto.server.FriendListRequest>(
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


  // @@protoc_insertion_point(class_scope:server.FriendListRequest)
  private static final com.halloapp.proto.server.FriendListRequest DEFAULT_INSTANCE;
  static {
    FriendListRequest defaultInstance = new FriendListRequest();
    // New instances are implicitly immutable so no need to make
    // immutable.
    DEFAULT_INSTANCE = defaultInstance;
    com.google.protobuf.GeneratedMessageLite.registerDefaultInstance(
      FriendListRequest.class, defaultInstance);
  }

  public static com.halloapp.proto.server.FriendListRequest getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static volatile com.google.protobuf.Parser<FriendListRequest> PARSER;

  public static com.google.protobuf.Parser<FriendListRequest> parser() {
    return DEFAULT_INSTANCE.getParserForType();
  }
}

