// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: server.proto

package com.halloapp.proto.server;

/**
 * Protobuf type {@code server.EndOfQueue}
 */
public  final class EndOfQueue extends
    com.google.protobuf.GeneratedMessageLite<
        EndOfQueue, EndOfQueue.Builder> implements
    // @@protoc_insertion_point(message_implements:server.EndOfQueue)
    EndOfQueueOrBuilder {
  private EndOfQueue() {
  }
  public static final int TRIMMED_FIELD_NUMBER = 1;
  private boolean trimmed_;
  /**
   * <code>bool trimmed = 1;</code>
   * @return The trimmed.
   */
  @java.lang.Override
  public boolean getTrimmed() {
    return trimmed_;
  }
  /**
   * <code>bool trimmed = 1;</code>
   * @param value The trimmed to set.
   */
  private void setTrimmed(boolean value) {
    
    trimmed_ = value;
  }
  /**
   * <code>bool trimmed = 1;</code>
   */
  private void clearTrimmed() {
    
    trimmed_ = false;
  }

  public static com.halloapp.proto.server.EndOfQueue parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static com.halloapp.proto.server.EndOfQueue parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static com.halloapp.proto.server.EndOfQueue parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static com.halloapp.proto.server.EndOfQueue parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static com.halloapp.proto.server.EndOfQueue parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static com.halloapp.proto.server.EndOfQueue parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static com.halloapp.proto.server.EndOfQueue parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input);
  }
  public static com.halloapp.proto.server.EndOfQueue parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input, extensionRegistry);
  }
  public static com.halloapp.proto.server.EndOfQueue parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return parseDelimitedFrom(DEFAULT_INSTANCE, input);
  }
  public static com.halloapp.proto.server.EndOfQueue parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return parseDelimitedFrom(DEFAULT_INSTANCE, input, extensionRegistry);
  }
  public static com.halloapp.proto.server.EndOfQueue parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input);
  }
  public static com.halloapp.proto.server.EndOfQueue parseFrom(
      com.google.protobuf.CodedInputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input, extensionRegistry);
  }

  public static Builder newBuilder() {
    return (Builder) DEFAULT_INSTANCE.createBuilder();
  }
  public static Builder newBuilder(com.halloapp.proto.server.EndOfQueue prototype) {
    return (Builder) DEFAULT_INSTANCE.createBuilder(prototype);
  }

  /**
   * Protobuf type {@code server.EndOfQueue}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessageLite.Builder<
        com.halloapp.proto.server.EndOfQueue, Builder> implements
      // @@protoc_insertion_point(builder_implements:server.EndOfQueue)
      com.halloapp.proto.server.EndOfQueueOrBuilder {
    // Construct using com.halloapp.proto.server.EndOfQueue.newBuilder()
    private Builder() {
      super(DEFAULT_INSTANCE);
    }


    /**
     * <code>bool trimmed = 1;</code>
     * @return The trimmed.
     */
    @java.lang.Override
    public boolean getTrimmed() {
      return instance.getTrimmed();
    }
    /**
     * <code>bool trimmed = 1;</code>
     * @param value The trimmed to set.
     * @return This builder for chaining.
     */
    public Builder setTrimmed(boolean value) {
      copyOnWrite();
      instance.setTrimmed(value);
      return this;
    }
    /**
     * <code>bool trimmed = 1;</code>
     * @return This builder for chaining.
     */
    public Builder clearTrimmed() {
      copyOnWrite();
      instance.clearTrimmed();
      return this;
    }

    // @@protoc_insertion_point(builder_scope:server.EndOfQueue)
  }
  @java.lang.Override
  @java.lang.SuppressWarnings({"unchecked", "fallthrough"})
  protected final java.lang.Object dynamicMethod(
      com.google.protobuf.GeneratedMessageLite.MethodToInvoke method,
      java.lang.Object arg0, java.lang.Object arg1) {
    switch (method) {
      case NEW_MUTABLE_INSTANCE: {
        return new com.halloapp.proto.server.EndOfQueue();
      }
      case NEW_BUILDER: {
        return new Builder();
      }
      case BUILD_MESSAGE_INFO: {
          java.lang.Object[] objects = new java.lang.Object[] {
            "trimmed_",
          };
          java.lang.String info =
              "\u0000\u0001\u0000\u0000\u0001\u0001\u0001\u0000\u0000\u0000\u0001\u0007";
          return newMessageInfo(DEFAULT_INSTANCE, info, objects);
      }
      // fall through
      case GET_DEFAULT_INSTANCE: {
        return DEFAULT_INSTANCE;
      }
      case GET_PARSER: {
        com.google.protobuf.Parser<com.halloapp.proto.server.EndOfQueue> parser = PARSER;
        if (parser == null) {
          synchronized (com.halloapp.proto.server.EndOfQueue.class) {
            parser = PARSER;
            if (parser == null) {
              parser =
                  new DefaultInstanceBasedParser<com.halloapp.proto.server.EndOfQueue>(
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


  // @@protoc_insertion_point(class_scope:server.EndOfQueue)
  private static final com.halloapp.proto.server.EndOfQueue DEFAULT_INSTANCE;
  static {
    EndOfQueue defaultInstance = new EndOfQueue();
    // New instances are implicitly immutable so no need to make
    // immutable.
    DEFAULT_INSTANCE = defaultInstance;
    com.google.protobuf.GeneratedMessageLite.registerDefaultInstance(
      EndOfQueue.class, defaultInstance);
  }

  public static com.halloapp.proto.server.EndOfQueue getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static volatile com.google.protobuf.Parser<EndOfQueue> PARSER;

  public static com.google.protobuf.Parser<EndOfQueue> parser() {
    return DEFAULT_INSTANCE.getParserForType();
  }
}

