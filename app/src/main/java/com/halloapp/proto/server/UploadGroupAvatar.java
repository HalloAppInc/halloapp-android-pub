// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: server.proto

package com.halloapp.proto.server;

/**
 * Protobuf type {@code server.UploadGroupAvatar}
 */
public  final class UploadGroupAvatar extends
    com.google.protobuf.GeneratedMessageLite<
        UploadGroupAvatar, UploadGroupAvatar.Builder> implements
    // @@protoc_insertion_point(message_implements:server.UploadGroupAvatar)
    UploadGroupAvatarOrBuilder {
  private UploadGroupAvatar() {
    gid_ = "";
    data_ = com.google.protobuf.ByteString.EMPTY;
  }
  public static final int GID_FIELD_NUMBER = 1;
  private java.lang.String gid_;
  /**
   * <code>string gid = 1;</code>
   * @return The gid.
   */
  @java.lang.Override
  public java.lang.String getGid() {
    return gid_;
  }
  /**
   * <code>string gid = 1;</code>
   * @return The bytes for gid.
   */
  @java.lang.Override
  public com.google.protobuf.ByteString
      getGidBytes() {
    return com.google.protobuf.ByteString.copyFromUtf8(gid_);
  }
  /**
   * <code>string gid = 1;</code>
   * @param value The gid to set.
   */
  private void setGid(
      java.lang.String value) {
    value.getClass();
  
    gid_ = value;
  }
  /**
   * <code>string gid = 1;</code>
   */
  private void clearGid() {
    
    gid_ = getDefaultInstance().getGid();
  }
  /**
   * <code>string gid = 1;</code>
   * @param value The bytes for gid to set.
   */
  private void setGidBytes(
      com.google.protobuf.ByteString value) {
    checkByteStringIsUtf8(value);
    gid_ = value.toStringUtf8();
    
  }

  public static final int DATA_FIELD_NUMBER = 2;
  private com.google.protobuf.ByteString data_;
  /**
   * <code>bytes data = 2;</code>
   * @return The data.
   */
  @java.lang.Override
  public com.google.protobuf.ByteString getData() {
    return data_;
  }
  /**
   * <code>bytes data = 2;</code>
   * @param value The data to set.
   */
  private void setData(com.google.protobuf.ByteString value) {
    value.getClass();
  
    data_ = value;
  }
  /**
   * <code>bytes data = 2;</code>
   */
  private void clearData() {
    
    data_ = getDefaultInstance().getData();
  }

  public static com.halloapp.proto.server.UploadGroupAvatar parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static com.halloapp.proto.server.UploadGroupAvatar parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static com.halloapp.proto.server.UploadGroupAvatar parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static com.halloapp.proto.server.UploadGroupAvatar parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static com.halloapp.proto.server.UploadGroupAvatar parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static com.halloapp.proto.server.UploadGroupAvatar parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static com.halloapp.proto.server.UploadGroupAvatar parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input);
  }
  public static com.halloapp.proto.server.UploadGroupAvatar parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input, extensionRegistry);
  }
  public static com.halloapp.proto.server.UploadGroupAvatar parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return parseDelimitedFrom(DEFAULT_INSTANCE, input);
  }
  public static com.halloapp.proto.server.UploadGroupAvatar parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return parseDelimitedFrom(DEFAULT_INSTANCE, input, extensionRegistry);
  }
  public static com.halloapp.proto.server.UploadGroupAvatar parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input);
  }
  public static com.halloapp.proto.server.UploadGroupAvatar parseFrom(
      com.google.protobuf.CodedInputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input, extensionRegistry);
  }

  public static Builder newBuilder() {
    return (Builder) DEFAULT_INSTANCE.createBuilder();
  }
  public static Builder newBuilder(com.halloapp.proto.server.UploadGroupAvatar prototype) {
    return (Builder) DEFAULT_INSTANCE.createBuilder(prototype);
  }

  /**
   * Protobuf type {@code server.UploadGroupAvatar}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessageLite.Builder<
        com.halloapp.proto.server.UploadGroupAvatar, Builder> implements
      // @@protoc_insertion_point(builder_implements:server.UploadGroupAvatar)
      com.halloapp.proto.server.UploadGroupAvatarOrBuilder {
    // Construct using com.halloapp.proto.server.UploadGroupAvatar.newBuilder()
    private Builder() {
      super(DEFAULT_INSTANCE);
    }


    /**
     * <code>string gid = 1;</code>
     * @return The gid.
     */
    @java.lang.Override
    public java.lang.String getGid() {
      return instance.getGid();
    }
    /**
     * <code>string gid = 1;</code>
     * @return The bytes for gid.
     */
    @java.lang.Override
    public com.google.protobuf.ByteString
        getGidBytes() {
      return instance.getGidBytes();
    }
    /**
     * <code>string gid = 1;</code>
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
     * <code>string gid = 1;</code>
     * @return This builder for chaining.
     */
    public Builder clearGid() {
      copyOnWrite();
      instance.clearGid();
      return this;
    }
    /**
     * <code>string gid = 1;</code>
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
     * <code>bytes data = 2;</code>
     * @return The data.
     */
    @java.lang.Override
    public com.google.protobuf.ByteString getData() {
      return instance.getData();
    }
    /**
     * <code>bytes data = 2;</code>
     * @param value The data to set.
     * @return This builder for chaining.
     */
    public Builder setData(com.google.protobuf.ByteString value) {
      copyOnWrite();
      instance.setData(value);
      return this;
    }
    /**
     * <code>bytes data = 2;</code>
     * @return This builder for chaining.
     */
    public Builder clearData() {
      copyOnWrite();
      instance.clearData();
      return this;
    }

    // @@protoc_insertion_point(builder_scope:server.UploadGroupAvatar)
  }
  @java.lang.Override
  @java.lang.SuppressWarnings({"unchecked", "fallthrough"})
  protected final java.lang.Object dynamicMethod(
      com.google.protobuf.GeneratedMessageLite.MethodToInvoke method,
      java.lang.Object arg0, java.lang.Object arg1) {
    switch (method) {
      case NEW_MUTABLE_INSTANCE: {
        return new com.halloapp.proto.server.UploadGroupAvatar();
      }
      case NEW_BUILDER: {
        return new Builder();
      }
      case BUILD_MESSAGE_INFO: {
          java.lang.Object[] objects = new java.lang.Object[] {
            "gid_",
            "data_",
          };
          java.lang.String info =
              "\u0000\u0002\u0000\u0000\u0001\u0002\u0002\u0000\u0000\u0000\u0001\u0208\u0002\n" +
              "";
          return newMessageInfo(DEFAULT_INSTANCE, info, objects);
      }
      // fall through
      case GET_DEFAULT_INSTANCE: {
        return DEFAULT_INSTANCE;
      }
      case GET_PARSER: {
        com.google.protobuf.Parser<com.halloapp.proto.server.UploadGroupAvatar> parser = PARSER;
        if (parser == null) {
          synchronized (com.halloapp.proto.server.UploadGroupAvatar.class) {
            parser = PARSER;
            if (parser == null) {
              parser =
                  new DefaultInstanceBasedParser<com.halloapp.proto.server.UploadGroupAvatar>(
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


  // @@protoc_insertion_point(class_scope:server.UploadGroupAvatar)
  private static final com.halloapp.proto.server.UploadGroupAvatar DEFAULT_INSTANCE;
  static {
    UploadGroupAvatar defaultInstance = new UploadGroupAvatar();
    // New instances are implicitly immutable so no need to make
    // immutable.
    DEFAULT_INSTANCE = defaultInstance;
    com.google.protobuf.GeneratedMessageLite.registerDefaultInstance(
      UploadGroupAvatar.class, defaultInstance);
  }

  public static com.halloapp.proto.server.UploadGroupAvatar getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static volatile com.google.protobuf.Parser<UploadGroupAvatar> PARSER;

  public static com.google.protobuf.Parser<UploadGroupAvatar> parser() {
    return DEFAULT_INSTANCE.getParserForType();
  }
}

