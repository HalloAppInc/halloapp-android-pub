// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: server.proto

package com.halloapp.proto.server;

/**
 * Protobuf type {@code server.StunServer}
 */
public  final class StunServer extends
    com.google.protobuf.GeneratedMessageLite<
        StunServer, StunServer.Builder> implements
    // @@protoc_insertion_point(message_implements:server.StunServer)
    StunServerOrBuilder {
  private StunServer() {
    host_ = "";
  }
  public static final int HOST_FIELD_NUMBER = 1;
  private java.lang.String host_;
  /**
   * <code>string host = 1;</code>
   * @return The host.
   */
  @java.lang.Override
  public java.lang.String getHost() {
    return host_;
  }
  /**
   * <code>string host = 1;</code>
   * @return The bytes for host.
   */
  @java.lang.Override
  public com.google.protobuf.ByteString
      getHostBytes() {
    return com.google.protobuf.ByteString.copyFromUtf8(host_);
  }
  /**
   * <code>string host = 1;</code>
   * @param value The host to set.
   */
  private void setHost(
      java.lang.String value) {
    java.lang.Class<?> valueClass = value.getClass();
  
    host_ = value;
  }
  /**
   * <code>string host = 1;</code>
   */
  private void clearHost() {
    
    host_ = getDefaultInstance().getHost();
  }
  /**
   * <code>string host = 1;</code>
   * @param value The bytes for host to set.
   */
  private void setHostBytes(
      com.google.protobuf.ByteString value) {
    checkByteStringIsUtf8(value);
    host_ = value.toStringUtf8();
    
  }

  public static final int PORT_FIELD_NUMBER = 2;
  private int port_;
  /**
   * <code>uint32 port = 2;</code>
   * @return The port.
   */
  @java.lang.Override
  public int getPort() {
    return port_;
  }
  /**
   * <code>uint32 port = 2;</code>
   * @param value The port to set.
   */
  private void setPort(int value) {
    
    port_ = value;
  }
  /**
   * <code>uint32 port = 2;</code>
   */
  private void clearPort() {
    
    port_ = 0;
  }

  public static com.halloapp.proto.server.StunServer parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static com.halloapp.proto.server.StunServer parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static com.halloapp.proto.server.StunServer parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static com.halloapp.proto.server.StunServer parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static com.halloapp.proto.server.StunServer parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static com.halloapp.proto.server.StunServer parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static com.halloapp.proto.server.StunServer parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input);
  }
  public static com.halloapp.proto.server.StunServer parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input, extensionRegistry);
  }
  public static com.halloapp.proto.server.StunServer parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return parseDelimitedFrom(DEFAULT_INSTANCE, input);
  }
  public static com.halloapp.proto.server.StunServer parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return parseDelimitedFrom(DEFAULT_INSTANCE, input, extensionRegistry);
  }
  public static com.halloapp.proto.server.StunServer parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input);
  }
  public static com.halloapp.proto.server.StunServer parseFrom(
      com.google.protobuf.CodedInputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input, extensionRegistry);
  }

  public static Builder newBuilder() {
    return (Builder) DEFAULT_INSTANCE.createBuilder();
  }
  public static Builder newBuilder(com.halloapp.proto.server.StunServer prototype) {
    return (Builder) DEFAULT_INSTANCE.createBuilder(prototype);
  }

  /**
   * Protobuf type {@code server.StunServer}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessageLite.Builder<
        com.halloapp.proto.server.StunServer, Builder> implements
      // @@protoc_insertion_point(builder_implements:server.StunServer)
      com.halloapp.proto.server.StunServerOrBuilder {
    // Construct using com.halloapp.proto.server.StunServer.newBuilder()
    private Builder() {
      super(DEFAULT_INSTANCE);
    }


    /**
     * <code>string host = 1;</code>
     * @return The host.
     */
    @java.lang.Override
    public java.lang.String getHost() {
      return instance.getHost();
    }
    /**
     * <code>string host = 1;</code>
     * @return The bytes for host.
     */
    @java.lang.Override
    public com.google.protobuf.ByteString
        getHostBytes() {
      return instance.getHostBytes();
    }
    /**
     * <code>string host = 1;</code>
     * @param value The host to set.
     * @return This builder for chaining.
     */
    public Builder setHost(
        java.lang.String value) {
      copyOnWrite();
      instance.setHost(value);
      return this;
    }
    /**
     * <code>string host = 1;</code>
     * @return This builder for chaining.
     */
    public Builder clearHost() {
      copyOnWrite();
      instance.clearHost();
      return this;
    }
    /**
     * <code>string host = 1;</code>
     * @param value The bytes for host to set.
     * @return This builder for chaining.
     */
    public Builder setHostBytes(
        com.google.protobuf.ByteString value) {
      copyOnWrite();
      instance.setHostBytes(value);
      return this;
    }

    /**
     * <code>uint32 port = 2;</code>
     * @return The port.
     */
    @java.lang.Override
    public int getPort() {
      return instance.getPort();
    }
    /**
     * <code>uint32 port = 2;</code>
     * @param value The port to set.
     * @return This builder for chaining.
     */
    public Builder setPort(int value) {
      copyOnWrite();
      instance.setPort(value);
      return this;
    }
    /**
     * <code>uint32 port = 2;</code>
     * @return This builder for chaining.
     */
    public Builder clearPort() {
      copyOnWrite();
      instance.clearPort();
      return this;
    }

    // @@protoc_insertion_point(builder_scope:server.StunServer)
  }
  @java.lang.Override
  @java.lang.SuppressWarnings({"unchecked", "fallthrough"})
  protected final java.lang.Object dynamicMethod(
      com.google.protobuf.GeneratedMessageLite.MethodToInvoke method,
      java.lang.Object arg0, java.lang.Object arg1) {
    switch (method) {
      case NEW_MUTABLE_INSTANCE: {
        return new com.halloapp.proto.server.StunServer();
      }
      case NEW_BUILDER: {
        return new Builder();
      }
      case BUILD_MESSAGE_INFO: {
          java.lang.Object[] objects = new java.lang.Object[] {
            "host_",
            "port_",
          };
          java.lang.String info =
              "\u0000\u0002\u0000\u0000\u0001\u0002\u0002\u0000\u0000\u0000\u0001\u0208\u0002\u000b" +
              "";
          return newMessageInfo(DEFAULT_INSTANCE, info, objects);
      }
      // fall through
      case GET_DEFAULT_INSTANCE: {
        return DEFAULT_INSTANCE;
      }
      case GET_PARSER: {
        com.google.protobuf.Parser<com.halloapp.proto.server.StunServer> parser = PARSER;
        if (parser == null) {
          synchronized (com.halloapp.proto.server.StunServer.class) {
            parser = PARSER;
            if (parser == null) {
              parser =
                  new DefaultInstanceBasedParser<com.halloapp.proto.server.StunServer>(
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


  // @@protoc_insertion_point(class_scope:server.StunServer)
  private static final com.halloapp.proto.server.StunServer DEFAULT_INSTANCE;
  static {
    StunServer defaultInstance = new StunServer();
    // New instances are implicitly immutable so no need to make
    // immutable.
    DEFAULT_INSTANCE = defaultInstance;
    com.google.protobuf.GeneratedMessageLite.registerDefaultInstance(
      StunServer.class, defaultInstance);
  }

  public static com.halloapp.proto.server.StunServer getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static volatile com.google.protobuf.Parser<StunServer> PARSER;

  public static com.google.protobuf.Parser<StunServer> parser() {
    return DEFAULT_INSTANCE.getParserForType();
  }
}

