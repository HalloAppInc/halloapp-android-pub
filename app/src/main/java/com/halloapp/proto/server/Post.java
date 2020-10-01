// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: server.proto

package com.halloapp.proto.server;

/**
 * Protobuf type {@code server.Post}
 */
public  final class Post extends
    com.google.protobuf.GeneratedMessageLite<
        Post, Post.Builder> implements
    // @@protoc_insertion_point(message_implements:server.Post)
    PostOrBuilder {
  private Post() {
    id_ = "";
    payload_ = com.google.protobuf.ByteString.EMPTY;
    publisherName_ = "";
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

  public static final int PUBLISHER_UID_FIELD_NUMBER = 2;
  private long publisherUid_;
  /**
   * <code>int64 publisher_uid = 2;</code>
   * @return The publisherUid.
   */
  @java.lang.Override
  public long getPublisherUid() {
    return publisherUid_;
  }
  /**
   * <code>int64 publisher_uid = 2;</code>
   * @param value The publisherUid to set.
   */
  private void setPublisherUid(long value) {
    
    publisherUid_ = value;
  }
  /**
   * <code>int64 publisher_uid = 2;</code>
   */
  private void clearPublisherUid() {
    
    publisherUid_ = 0L;
  }

  public static final int PAYLOAD_FIELD_NUMBER = 3;
  private com.google.protobuf.ByteString payload_;
  /**
   * <code>bytes payload = 3;</code>
   * @return The payload.
   */
  @java.lang.Override
  public com.google.protobuf.ByteString getPayload() {
    return payload_;
  }
  /**
   * <code>bytes payload = 3;</code>
   * @param value The payload to set.
   */
  private void setPayload(com.google.protobuf.ByteString value) {
    value.getClass();
  
    payload_ = value;
  }
  /**
   * <code>bytes payload = 3;</code>
   */
  private void clearPayload() {
    
    payload_ = getDefaultInstance().getPayload();
  }

  public static final int AUDIENCE_FIELD_NUMBER = 4;
  private com.halloapp.proto.server.Audience audience_;
  /**
   * <code>.server.Audience audience = 4;</code>
   */
  @java.lang.Override
  public boolean hasAudience() {
    return audience_ != null;
  }
  /**
   * <code>.server.Audience audience = 4;</code>
   */
  @java.lang.Override
  public com.halloapp.proto.server.Audience getAudience() {
    return audience_ == null ? com.halloapp.proto.server.Audience.getDefaultInstance() : audience_;
  }
  /**
   * <code>.server.Audience audience = 4;</code>
   */
  private void setAudience(com.halloapp.proto.server.Audience value) {
    value.getClass();
  audience_ = value;
    
    }
  /**
   * <code>.server.Audience audience = 4;</code>
   */
  @java.lang.SuppressWarnings({"ReferenceEquality"})
  private void mergeAudience(com.halloapp.proto.server.Audience value) {
    value.getClass();
  if (audience_ != null &&
        audience_ != com.halloapp.proto.server.Audience.getDefaultInstance()) {
      audience_ =
        com.halloapp.proto.server.Audience.newBuilder(audience_).mergeFrom(value).buildPartial();
    } else {
      audience_ = value;
    }
    
  }
  /**
   * <code>.server.Audience audience = 4;</code>
   */
  private void clearAudience() {  audience_ = null;
    
  }

  public static final int TIMESTAMP_FIELD_NUMBER = 5;
  private long timestamp_;
  /**
   * <code>int64 timestamp = 5;</code>
   * @return The timestamp.
   */
  @java.lang.Override
  public long getTimestamp() {
    return timestamp_;
  }
  /**
   * <code>int64 timestamp = 5;</code>
   * @param value The timestamp to set.
   */
  private void setTimestamp(long value) {
    
    timestamp_ = value;
  }
  /**
   * <code>int64 timestamp = 5;</code>
   */
  private void clearTimestamp() {
    
    timestamp_ = 0L;
  }

  public static final int PUBLISHER_NAME_FIELD_NUMBER = 6;
  private java.lang.String publisherName_;
  /**
   * <code>string publisher_name = 6;</code>
   * @return The publisherName.
   */
  @java.lang.Override
  public java.lang.String getPublisherName() {
    return publisherName_;
  }
  /**
   * <code>string publisher_name = 6;</code>
   * @return The bytes for publisherName.
   */
  @java.lang.Override
  public com.google.protobuf.ByteString
      getPublisherNameBytes() {
    return com.google.protobuf.ByteString.copyFromUtf8(publisherName_);
  }
  /**
   * <code>string publisher_name = 6;</code>
   * @param value The publisherName to set.
   */
  private void setPublisherName(
      java.lang.String value) {
    value.getClass();
  
    publisherName_ = value;
  }
  /**
   * <code>string publisher_name = 6;</code>
   */
  private void clearPublisherName() {
    
    publisherName_ = getDefaultInstance().getPublisherName();
  }
  /**
   * <code>string publisher_name = 6;</code>
   * @param value The bytes for publisherName to set.
   */
  private void setPublisherNameBytes(
      com.google.protobuf.ByteString value) {
    checkByteStringIsUtf8(value);
    publisherName_ = value.toStringUtf8();
    
  }

  public static com.halloapp.proto.server.Post parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static com.halloapp.proto.server.Post parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static com.halloapp.proto.server.Post parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static com.halloapp.proto.server.Post parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static com.halloapp.proto.server.Post parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static com.halloapp.proto.server.Post parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static com.halloapp.proto.server.Post parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input);
  }
  public static com.halloapp.proto.server.Post parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input, extensionRegistry);
  }
  public static com.halloapp.proto.server.Post parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return parseDelimitedFrom(DEFAULT_INSTANCE, input);
  }
  public static com.halloapp.proto.server.Post parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return parseDelimitedFrom(DEFAULT_INSTANCE, input, extensionRegistry);
  }
  public static com.halloapp.proto.server.Post parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input);
  }
  public static com.halloapp.proto.server.Post parseFrom(
      com.google.protobuf.CodedInputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input, extensionRegistry);
  }

  public static Builder newBuilder() {
    return (Builder) DEFAULT_INSTANCE.createBuilder();
  }
  public static Builder newBuilder(com.halloapp.proto.server.Post prototype) {
    return (Builder) DEFAULT_INSTANCE.createBuilder(prototype);
  }

  /**
   * Protobuf type {@code server.Post}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessageLite.Builder<
        com.halloapp.proto.server.Post, Builder> implements
      // @@protoc_insertion_point(builder_implements:server.Post)
      com.halloapp.proto.server.PostOrBuilder {
    // Construct using com.halloapp.proto.server.Post.newBuilder()
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
     * <code>int64 publisher_uid = 2;</code>
     * @return The publisherUid.
     */
    @java.lang.Override
    public long getPublisherUid() {
      return instance.getPublisherUid();
    }
    /**
     * <code>int64 publisher_uid = 2;</code>
     * @param value The publisherUid to set.
     * @return This builder for chaining.
     */
    public Builder setPublisherUid(long value) {
      copyOnWrite();
      instance.setPublisherUid(value);
      return this;
    }
    /**
     * <code>int64 publisher_uid = 2;</code>
     * @return This builder for chaining.
     */
    public Builder clearPublisherUid() {
      copyOnWrite();
      instance.clearPublisherUid();
      return this;
    }

    /**
     * <code>bytes payload = 3;</code>
     * @return The payload.
     */
    @java.lang.Override
    public com.google.protobuf.ByteString getPayload() {
      return instance.getPayload();
    }
    /**
     * <code>bytes payload = 3;</code>
     * @param value The payload to set.
     * @return This builder for chaining.
     */
    public Builder setPayload(com.google.protobuf.ByteString value) {
      copyOnWrite();
      instance.setPayload(value);
      return this;
    }
    /**
     * <code>bytes payload = 3;</code>
     * @return This builder for chaining.
     */
    public Builder clearPayload() {
      copyOnWrite();
      instance.clearPayload();
      return this;
    }

    /**
     * <code>.server.Audience audience = 4;</code>
     */
    @java.lang.Override
    public boolean hasAudience() {
      return instance.hasAudience();
    }
    /**
     * <code>.server.Audience audience = 4;</code>
     */
    @java.lang.Override
    public com.halloapp.proto.server.Audience getAudience() {
      return instance.getAudience();
    }
    /**
     * <code>.server.Audience audience = 4;</code>
     */
    public Builder setAudience(com.halloapp.proto.server.Audience value) {
      copyOnWrite();
      instance.setAudience(value);
      return this;
      }
    /**
     * <code>.server.Audience audience = 4;</code>
     */
    public Builder setAudience(
        com.halloapp.proto.server.Audience.Builder builderForValue) {
      copyOnWrite();
      instance.setAudience(builderForValue.build());
      return this;
    }
    /**
     * <code>.server.Audience audience = 4;</code>
     */
    public Builder mergeAudience(com.halloapp.proto.server.Audience value) {
      copyOnWrite();
      instance.mergeAudience(value);
      return this;
    }
    /**
     * <code>.server.Audience audience = 4;</code>
     */
    public Builder clearAudience() {  copyOnWrite();
      instance.clearAudience();
      return this;
    }

    /**
     * <code>int64 timestamp = 5;</code>
     * @return The timestamp.
     */
    @java.lang.Override
    public long getTimestamp() {
      return instance.getTimestamp();
    }
    /**
     * <code>int64 timestamp = 5;</code>
     * @param value The timestamp to set.
     * @return This builder for chaining.
     */
    public Builder setTimestamp(long value) {
      copyOnWrite();
      instance.setTimestamp(value);
      return this;
    }
    /**
     * <code>int64 timestamp = 5;</code>
     * @return This builder for chaining.
     */
    public Builder clearTimestamp() {
      copyOnWrite();
      instance.clearTimestamp();
      return this;
    }

    /**
     * <code>string publisher_name = 6;</code>
     * @return The publisherName.
     */
    @java.lang.Override
    public java.lang.String getPublisherName() {
      return instance.getPublisherName();
    }
    /**
     * <code>string publisher_name = 6;</code>
     * @return The bytes for publisherName.
     */
    @java.lang.Override
    public com.google.protobuf.ByteString
        getPublisherNameBytes() {
      return instance.getPublisherNameBytes();
    }
    /**
     * <code>string publisher_name = 6;</code>
     * @param value The publisherName to set.
     * @return This builder for chaining.
     */
    public Builder setPublisherName(
        java.lang.String value) {
      copyOnWrite();
      instance.setPublisherName(value);
      return this;
    }
    /**
     * <code>string publisher_name = 6;</code>
     * @return This builder for chaining.
     */
    public Builder clearPublisherName() {
      copyOnWrite();
      instance.clearPublisherName();
      return this;
    }
    /**
     * <code>string publisher_name = 6;</code>
     * @param value The bytes for publisherName to set.
     * @return This builder for chaining.
     */
    public Builder setPublisherNameBytes(
        com.google.protobuf.ByteString value) {
      copyOnWrite();
      instance.setPublisherNameBytes(value);
      return this;
    }

    // @@protoc_insertion_point(builder_scope:server.Post)
  }
  @java.lang.Override
  @java.lang.SuppressWarnings({"unchecked", "fallthrough"})
  protected final java.lang.Object dynamicMethod(
      com.google.protobuf.GeneratedMessageLite.MethodToInvoke method,
      java.lang.Object arg0, java.lang.Object arg1) {
    switch (method) {
      case NEW_MUTABLE_INSTANCE: {
        return new com.halloapp.proto.server.Post();
      }
      case NEW_BUILDER: {
        return new Builder();
      }
      case BUILD_MESSAGE_INFO: {
          java.lang.Object[] objects = new java.lang.Object[] {
            "id_",
            "publisherUid_",
            "payload_",
            "audience_",
            "timestamp_",
            "publisherName_",
          };
          java.lang.String info =
              "\u0000\u0006\u0000\u0000\u0001\u0006\u0006\u0000\u0000\u0000\u0001\u0208\u0002\u0002" +
              "\u0003\n\u0004\t\u0005\u0002\u0006\u0208";
          return newMessageInfo(DEFAULT_INSTANCE, info, objects);
      }
      // fall through
      case GET_DEFAULT_INSTANCE: {
        return DEFAULT_INSTANCE;
      }
      case GET_PARSER: {
        com.google.protobuf.Parser<com.halloapp.proto.server.Post> parser = PARSER;
        if (parser == null) {
          synchronized (com.halloapp.proto.server.Post.class) {
            parser = PARSER;
            if (parser == null) {
              parser =
                  new DefaultInstanceBasedParser<com.halloapp.proto.server.Post>(
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


  // @@protoc_insertion_point(class_scope:server.Post)
  private static final com.halloapp.proto.server.Post DEFAULT_INSTANCE;
  static {
    Post defaultInstance = new Post();
    // New instances are implicitly immutable so no need to make
    // immutable.
    DEFAULT_INSTANCE = defaultInstance;
    com.google.protobuf.GeneratedMessageLite.registerDefaultInstance(
      Post.class, defaultInstance);
  }

  public static com.halloapp.proto.server.Post getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static volatile com.google.protobuf.Parser<Post> PARSER;

  public static com.google.protobuf.Parser<Post> parser() {
    return DEFAULT_INSTANCE.getParserForType();
  }
}

