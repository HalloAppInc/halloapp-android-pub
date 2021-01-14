// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: server.proto

package com.halloapp.proto.server;

/**
 * Protobuf type {@code server.Presence}
 */
public  final class Presence extends
    com.google.protobuf.GeneratedMessageLite<
        Presence, Presence.Builder> implements
    // @@protoc_insertion_point(message_implements:server.Presence)
    PresenceOrBuilder {
  private Presence() {
    id_ = "";
  }
  /**
   * Protobuf enum {@code server.Presence.Type}
   */
  public enum Type
      implements com.google.protobuf.Internal.EnumLite {
    /**
     * <code>AVAILABLE = 0;</code>
     */
    AVAILABLE(0),
    /**
     * <code>AWAY = 1;</code>
     */
    AWAY(1),
    /**
     * <code>SUBSCRIBE = 2;</code>
     */
    SUBSCRIBE(2),
    /**
     * <code>UNSUBSCRIBE = 3;</code>
     */
    UNSUBSCRIBE(3),
    UNRECOGNIZED(-1),
    ;

    /**
     * <code>AVAILABLE = 0;</code>
     */
    public static final int AVAILABLE_VALUE = 0;
    /**
     * <code>AWAY = 1;</code>
     */
    public static final int AWAY_VALUE = 1;
    /**
     * <code>SUBSCRIBE = 2;</code>
     */
    public static final int SUBSCRIBE_VALUE = 2;
    /**
     * <code>UNSUBSCRIBE = 3;</code>
     */
    public static final int UNSUBSCRIBE_VALUE = 3;


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
        case 0: return AVAILABLE;
        case 1: return AWAY;
        case 2: return SUBSCRIBE;
        case 3: return UNSUBSCRIBE;
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

    // @@protoc_insertion_point(enum_scope:server.Presence.Type)
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

  public static final int TYPE_FIELD_NUMBER = 2;
  private int type_;
  /**
   * <code>.server.Presence.Type type = 2;</code>
   * @return The enum numeric value on the wire for type.
   */
  @java.lang.Override
  public int getTypeValue() {
    return type_;
  }
  /**
   * <code>.server.Presence.Type type = 2;</code>
   * @return The type.
   */
  @java.lang.Override
  public com.halloapp.proto.server.Presence.Type getType() {
    com.halloapp.proto.server.Presence.Type result = com.halloapp.proto.server.Presence.Type.forNumber(type_);
    return result == null ? com.halloapp.proto.server.Presence.Type.UNRECOGNIZED : result;
  }
  /**
   * <code>.server.Presence.Type type = 2;</code>
   * @param value The enum numeric value on the wire for type to set.
   */
  private void setTypeValue(int value) {
      type_ = value;
  }
  /**
   * <code>.server.Presence.Type type = 2;</code>
   * @param value The type to set.
   */
  private void setType(com.halloapp.proto.server.Presence.Type value) {
    type_ = value.getNumber();
    
  }
  /**
   * <code>.server.Presence.Type type = 2;</code>
   */
  private void clearType() {
    
    type_ = 0;
  }

  public static final int UID_FIELD_NUMBER = 3;
  private long uid_;
  /**
   * <pre>
   * Clients must stop using this field.
   * </pre>
   *
   * <code>int64 uid = 3 [deprecated = true];</code>
   * @return The uid.
   */
  @java.lang.Override
  @java.lang.Deprecated public long getUid() {
    return uid_;
  }
  /**
   * <pre>
   * Clients must stop using this field.
   * </pre>
   *
   * <code>int64 uid = 3 [deprecated = true];</code>
   * @param value The uid to set.
   */
  private void setUid(long value) {
    
    uid_ = value;
  }
  /**
   * <pre>
   * Clients must stop using this field.
   * </pre>
   *
   * <code>int64 uid = 3 [deprecated = true];</code>
   */
  private void clearUid() {
    
    uid_ = 0L;
  }

  public static final int LAST_SEEN_FIELD_NUMBER = 4;
  private long lastSeen_;
  /**
   * <code>int64 last_seen = 4;</code>
   * @return The lastSeen.
   */
  @java.lang.Override
  public long getLastSeen() {
    return lastSeen_;
  }
  /**
   * <code>int64 last_seen = 4;</code>
   * @param value The lastSeen to set.
   */
  private void setLastSeen(long value) {
    
    lastSeen_ = value;
  }
  /**
   * <code>int64 last_seen = 4;</code>
   */
  private void clearLastSeen() {
    
    lastSeen_ = 0L;
  }

  public static final int TO_UID_FIELD_NUMBER = 5;
  private long toUid_;
  /**
   * <code>int64 to_uid = 5;</code>
   * @return The toUid.
   */
  @java.lang.Override
  public long getToUid() {
    return toUid_;
  }
  /**
   * <code>int64 to_uid = 5;</code>
   * @param value The toUid to set.
   */
  private void setToUid(long value) {
    
    toUid_ = value;
  }
  /**
   * <code>int64 to_uid = 5;</code>
   */
  private void clearToUid() {
    
    toUid_ = 0L;
  }

  public static final int FROM_UID_FIELD_NUMBER = 6;
  private long fromUid_;
  /**
   * <code>int64 from_uid = 6;</code>
   * @return The fromUid.
   */
  @java.lang.Override
  public long getFromUid() {
    return fromUid_;
  }
  /**
   * <code>int64 from_uid = 6;</code>
   * @param value The fromUid to set.
   */
  private void setFromUid(long value) {
    
    fromUid_ = value;
  }
  /**
   * <code>int64 from_uid = 6;</code>
   */
  private void clearFromUid() {
    
    fromUid_ = 0L;
  }

  public static com.halloapp.proto.server.Presence parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static com.halloapp.proto.server.Presence parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static com.halloapp.proto.server.Presence parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static com.halloapp.proto.server.Presence parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static com.halloapp.proto.server.Presence parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static com.halloapp.proto.server.Presence parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static com.halloapp.proto.server.Presence parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input);
  }
  public static com.halloapp.proto.server.Presence parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input, extensionRegistry);
  }
  public static com.halloapp.proto.server.Presence parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return parseDelimitedFrom(DEFAULT_INSTANCE, input);
  }
  public static com.halloapp.proto.server.Presence parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return parseDelimitedFrom(DEFAULT_INSTANCE, input, extensionRegistry);
  }
  public static com.halloapp.proto.server.Presence parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input);
  }
  public static com.halloapp.proto.server.Presence parseFrom(
      com.google.protobuf.CodedInputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input, extensionRegistry);
  }

  public static Builder newBuilder() {
    return (Builder) DEFAULT_INSTANCE.createBuilder();
  }
  public static Builder newBuilder(com.halloapp.proto.server.Presence prototype) {
    return (Builder) DEFAULT_INSTANCE.createBuilder(prototype);
  }

  /**
   * Protobuf type {@code server.Presence}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessageLite.Builder<
        com.halloapp.proto.server.Presence, Builder> implements
      // @@protoc_insertion_point(builder_implements:server.Presence)
      com.halloapp.proto.server.PresenceOrBuilder {
    // Construct using com.halloapp.proto.server.Presence.newBuilder()
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
     * <code>.server.Presence.Type type = 2;</code>
     * @return The enum numeric value on the wire for type.
     */
    @java.lang.Override
    public int getTypeValue() {
      return instance.getTypeValue();
    }
    /**
     * <code>.server.Presence.Type type = 2;</code>
     * @param value The type to set.
     * @return This builder for chaining.
     */
    public Builder setTypeValue(int value) {
      copyOnWrite();
      instance.setTypeValue(value);
      return this;
    }
    /**
     * <code>.server.Presence.Type type = 2;</code>
     * @return The type.
     */
    @java.lang.Override
    public com.halloapp.proto.server.Presence.Type getType() {
      return instance.getType();
    }
    /**
     * <code>.server.Presence.Type type = 2;</code>
     * @param value The enum numeric value on the wire for type to set.
     * @return This builder for chaining.
     */
    public Builder setType(com.halloapp.proto.server.Presence.Type value) {
      copyOnWrite();
      instance.setType(value);
      return this;
    }
    /**
     * <code>.server.Presence.Type type = 2;</code>
     * @return This builder for chaining.
     */
    public Builder clearType() {
      copyOnWrite();
      instance.clearType();
      return this;
    }

    /**
     * <pre>
     * Clients must stop using this field.
     * </pre>
     *
     * <code>int64 uid = 3 [deprecated = true];</code>
     * @return The uid.
     */
    @java.lang.Override
    @java.lang.Deprecated public long getUid() {
      return instance.getUid();
    }
    /**
     * <pre>
     * Clients must stop using this field.
     * </pre>
     *
     * <code>int64 uid = 3 [deprecated = true];</code>
     * @param value The uid to set.
     * @return This builder for chaining.
     */
    @java.lang.Deprecated public Builder setUid(long value) {
      copyOnWrite();
      instance.setUid(value);
      return this;
    }
    /**
     * <pre>
     * Clients must stop using this field.
     * </pre>
     *
     * <code>int64 uid = 3 [deprecated = true];</code>
     * @return This builder for chaining.
     */
    @java.lang.Deprecated public Builder clearUid() {
      copyOnWrite();
      instance.clearUid();
      return this;
    }

    /**
     * <code>int64 last_seen = 4;</code>
     * @return The lastSeen.
     */
    @java.lang.Override
    public long getLastSeen() {
      return instance.getLastSeen();
    }
    /**
     * <code>int64 last_seen = 4;</code>
     * @param value The lastSeen to set.
     * @return This builder for chaining.
     */
    public Builder setLastSeen(long value) {
      copyOnWrite();
      instance.setLastSeen(value);
      return this;
    }
    /**
     * <code>int64 last_seen = 4;</code>
     * @return This builder for chaining.
     */
    public Builder clearLastSeen() {
      copyOnWrite();
      instance.clearLastSeen();
      return this;
    }

    /**
     * <code>int64 to_uid = 5;</code>
     * @return The toUid.
     */
    @java.lang.Override
    public long getToUid() {
      return instance.getToUid();
    }
    /**
     * <code>int64 to_uid = 5;</code>
     * @param value The toUid to set.
     * @return This builder for chaining.
     */
    public Builder setToUid(long value) {
      copyOnWrite();
      instance.setToUid(value);
      return this;
    }
    /**
     * <code>int64 to_uid = 5;</code>
     * @return This builder for chaining.
     */
    public Builder clearToUid() {
      copyOnWrite();
      instance.clearToUid();
      return this;
    }

    /**
     * <code>int64 from_uid = 6;</code>
     * @return The fromUid.
     */
    @java.lang.Override
    public long getFromUid() {
      return instance.getFromUid();
    }
    /**
     * <code>int64 from_uid = 6;</code>
     * @param value The fromUid to set.
     * @return This builder for chaining.
     */
    public Builder setFromUid(long value) {
      copyOnWrite();
      instance.setFromUid(value);
      return this;
    }
    /**
     * <code>int64 from_uid = 6;</code>
     * @return This builder for chaining.
     */
    public Builder clearFromUid() {
      copyOnWrite();
      instance.clearFromUid();
      return this;
    }

    // @@protoc_insertion_point(builder_scope:server.Presence)
  }
  @java.lang.Override
  @java.lang.SuppressWarnings({"unchecked", "fallthrough"})
  protected final java.lang.Object dynamicMethod(
      com.google.protobuf.GeneratedMessageLite.MethodToInvoke method,
      java.lang.Object arg0, java.lang.Object arg1) {
    switch (method) {
      case NEW_MUTABLE_INSTANCE: {
        return new com.halloapp.proto.server.Presence();
      }
      case NEW_BUILDER: {
        return new Builder();
      }
      case BUILD_MESSAGE_INFO: {
          java.lang.Object[] objects = new java.lang.Object[] {
            "id_",
            "type_",
            "uid_",
            "lastSeen_",
            "toUid_",
            "fromUid_",
          };
          java.lang.String info =
              "\u0000\u0006\u0000\u0000\u0001\u0006\u0006\u0000\u0000\u0000\u0001\u0208\u0002\f" +
              "\u0003\u0002\u0004\u0002\u0005\u0002\u0006\u0002";
          return newMessageInfo(DEFAULT_INSTANCE, info, objects);
      }
      // fall through
      case GET_DEFAULT_INSTANCE: {
        return DEFAULT_INSTANCE;
      }
      case GET_PARSER: {
        com.google.protobuf.Parser<com.halloapp.proto.server.Presence> parser = PARSER;
        if (parser == null) {
          synchronized (com.halloapp.proto.server.Presence.class) {
            parser = PARSER;
            if (parser == null) {
              parser =
                  new DefaultInstanceBasedParser<com.halloapp.proto.server.Presence>(
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


  // @@protoc_insertion_point(class_scope:server.Presence)
  private static final com.halloapp.proto.server.Presence DEFAULT_INSTANCE;
  static {
    Presence defaultInstance = new Presence();
    // New instances are implicitly immutable so no need to make
    // immutable.
    DEFAULT_INSTANCE = defaultInstance;
    com.google.protobuf.GeneratedMessageLite.registerDefaultInstance(
      Presence.class, defaultInstance);
  }

  public static com.halloapp.proto.server.Presence getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static volatile com.google.protobuf.Parser<Presence> PARSER;

  public static com.google.protobuf.Parser<Presence> parser() {
    return DEFAULT_INSTANCE.getParserForType();
  }
}

