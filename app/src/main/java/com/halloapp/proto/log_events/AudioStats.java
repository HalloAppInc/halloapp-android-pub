// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: log_events.proto

package com.halloapp.proto.log_events;

/**
 * Protobuf type {@code server.AudioStats}
 */
public  final class AudioStats extends
    com.google.protobuf.GeneratedMessageLite<
        AudioStats, AudioStats.Builder> implements
    // @@protoc_insertion_point(message_implements:server.AudioStats)
    AudioStatsOrBuilder {
  private AudioStats() {
  }
  public static final int INSERTEDSAMPLESFORDECELERATION_FIELD_NUMBER = 1;
  private long insertedSamplesForDeceleration_;
  /**
   * <pre>
   * tracks quality of incoming audio
   * </pre>
   *
   * <code>uint64 insertedSamplesForDeceleration = 1;</code>
   * @return The insertedSamplesForDeceleration.
   */
  @java.lang.Override
  public long getInsertedSamplesForDeceleration() {
    return insertedSamplesForDeceleration_;
  }
  /**
   * <pre>
   * tracks quality of incoming audio
   * </pre>
   *
   * <code>uint64 insertedSamplesForDeceleration = 1;</code>
   * @param value The insertedSamplesForDeceleration to set.
   */
  private void setInsertedSamplesForDeceleration(long value) {
    
    insertedSamplesForDeceleration_ = value;
  }
  /**
   * <pre>
   * tracks quality of incoming audio
   * </pre>
   *
   * <code>uint64 insertedSamplesForDeceleration = 1;</code>
   */
  private void clearInsertedSamplesForDeceleration() {
    
    insertedSamplesForDeceleration_ = 0L;
  }

  public static final int REMOVEDSAMPLESFORACCELERATION_FIELD_NUMBER = 2;
  private long removedSamplesForAcceleration_;
  /**
   * <code>uint64 removedSamplesForAcceleration = 2;</code>
   * @return The removedSamplesForAcceleration.
   */
  @java.lang.Override
  public long getRemovedSamplesForAcceleration() {
    return removedSamplesForAcceleration_;
  }
  /**
   * <code>uint64 removedSamplesForAcceleration = 2;</code>
   * @param value The removedSamplesForAcceleration to set.
   */
  private void setRemovedSamplesForAcceleration(long value) {
    
    removedSamplesForAcceleration_ = value;
  }
  /**
   * <code>uint64 removedSamplesForAcceleration = 2;</code>
   */
  private void clearRemovedSamplesForAcceleration() {
    
    removedSamplesForAcceleration_ = 0L;
  }

  public static final int PACKETSDISCARDED_FIELD_NUMBER = 3;
  private long packetsDiscarded_;
  /**
   * <code>uint64 packetsDiscarded = 3;</code>
   * @return The packetsDiscarded.
   */
  @java.lang.Override
  public long getPacketsDiscarded() {
    return packetsDiscarded_;
  }
  /**
   * <code>uint64 packetsDiscarded = 3;</code>
   * @param value The packetsDiscarded to set.
   */
  private void setPacketsDiscarded(long value) {
    
    packetsDiscarded_ = value;
  }
  /**
   * <code>uint64 packetsDiscarded = 3;</code>
   */
  private void clearPacketsDiscarded() {
    
    packetsDiscarded_ = 0L;
  }

  public static com.halloapp.proto.log_events.AudioStats parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static com.halloapp.proto.log_events.AudioStats parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static com.halloapp.proto.log_events.AudioStats parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static com.halloapp.proto.log_events.AudioStats parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static com.halloapp.proto.log_events.AudioStats parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static com.halloapp.proto.log_events.AudioStats parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static com.halloapp.proto.log_events.AudioStats parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input);
  }
  public static com.halloapp.proto.log_events.AudioStats parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input, extensionRegistry);
  }
  public static com.halloapp.proto.log_events.AudioStats parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return parseDelimitedFrom(DEFAULT_INSTANCE, input);
  }
  public static com.halloapp.proto.log_events.AudioStats parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return parseDelimitedFrom(DEFAULT_INSTANCE, input, extensionRegistry);
  }
  public static com.halloapp.proto.log_events.AudioStats parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input);
  }
  public static com.halloapp.proto.log_events.AudioStats parseFrom(
      com.google.protobuf.CodedInputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input, extensionRegistry);
  }

  public static Builder newBuilder() {
    return (Builder) DEFAULT_INSTANCE.createBuilder();
  }
  public static Builder newBuilder(com.halloapp.proto.log_events.AudioStats prototype) {
    return (Builder) DEFAULT_INSTANCE.createBuilder(prototype);
  }

  /**
   * Protobuf type {@code server.AudioStats}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessageLite.Builder<
        com.halloapp.proto.log_events.AudioStats, Builder> implements
      // @@protoc_insertion_point(builder_implements:server.AudioStats)
      com.halloapp.proto.log_events.AudioStatsOrBuilder {
    // Construct using com.halloapp.proto.log_events.AudioStats.newBuilder()
    private Builder() {
      super(DEFAULT_INSTANCE);
    }


    /**
     * <pre>
     * tracks quality of incoming audio
     * </pre>
     *
     * <code>uint64 insertedSamplesForDeceleration = 1;</code>
     * @return The insertedSamplesForDeceleration.
     */
    @java.lang.Override
    public long getInsertedSamplesForDeceleration() {
      return instance.getInsertedSamplesForDeceleration();
    }
    /**
     * <pre>
     * tracks quality of incoming audio
     * </pre>
     *
     * <code>uint64 insertedSamplesForDeceleration = 1;</code>
     * @param value The insertedSamplesForDeceleration to set.
     * @return This builder for chaining.
     */
    public Builder setInsertedSamplesForDeceleration(long value) {
      copyOnWrite();
      instance.setInsertedSamplesForDeceleration(value);
      return this;
    }
    /**
     * <pre>
     * tracks quality of incoming audio
     * </pre>
     *
     * <code>uint64 insertedSamplesForDeceleration = 1;</code>
     * @return This builder for chaining.
     */
    public Builder clearInsertedSamplesForDeceleration() {
      copyOnWrite();
      instance.clearInsertedSamplesForDeceleration();
      return this;
    }

    /**
     * <code>uint64 removedSamplesForAcceleration = 2;</code>
     * @return The removedSamplesForAcceleration.
     */
    @java.lang.Override
    public long getRemovedSamplesForAcceleration() {
      return instance.getRemovedSamplesForAcceleration();
    }
    /**
     * <code>uint64 removedSamplesForAcceleration = 2;</code>
     * @param value The removedSamplesForAcceleration to set.
     * @return This builder for chaining.
     */
    public Builder setRemovedSamplesForAcceleration(long value) {
      copyOnWrite();
      instance.setRemovedSamplesForAcceleration(value);
      return this;
    }
    /**
     * <code>uint64 removedSamplesForAcceleration = 2;</code>
     * @return This builder for chaining.
     */
    public Builder clearRemovedSamplesForAcceleration() {
      copyOnWrite();
      instance.clearRemovedSamplesForAcceleration();
      return this;
    }

    /**
     * <code>uint64 packetsDiscarded = 3;</code>
     * @return The packetsDiscarded.
     */
    @java.lang.Override
    public long getPacketsDiscarded() {
      return instance.getPacketsDiscarded();
    }
    /**
     * <code>uint64 packetsDiscarded = 3;</code>
     * @param value The packetsDiscarded to set.
     * @return This builder for chaining.
     */
    public Builder setPacketsDiscarded(long value) {
      copyOnWrite();
      instance.setPacketsDiscarded(value);
      return this;
    }
    /**
     * <code>uint64 packetsDiscarded = 3;</code>
     * @return This builder for chaining.
     */
    public Builder clearPacketsDiscarded() {
      copyOnWrite();
      instance.clearPacketsDiscarded();
      return this;
    }

    // @@protoc_insertion_point(builder_scope:server.AudioStats)
  }
  @java.lang.Override
  @java.lang.SuppressWarnings({"unchecked", "fallthrough"})
  protected final java.lang.Object dynamicMethod(
      com.google.protobuf.GeneratedMessageLite.MethodToInvoke method,
      java.lang.Object arg0, java.lang.Object arg1) {
    switch (method) {
      case NEW_MUTABLE_INSTANCE: {
        return new com.halloapp.proto.log_events.AudioStats();
      }
      case NEW_BUILDER: {
        return new Builder();
      }
      case BUILD_MESSAGE_INFO: {
          java.lang.Object[] objects = new java.lang.Object[] {
            "insertedSamplesForDeceleration_",
            "removedSamplesForAcceleration_",
            "packetsDiscarded_",
          };
          java.lang.String info =
              "\u0000\u0003\u0000\u0000\u0001\u0003\u0003\u0000\u0000\u0000\u0001\u0003\u0002\u0003" +
              "\u0003\u0003";
          return newMessageInfo(DEFAULT_INSTANCE, info, objects);
      }
      // fall through
      case GET_DEFAULT_INSTANCE: {
        return DEFAULT_INSTANCE;
      }
      case GET_PARSER: {
        com.google.protobuf.Parser<com.halloapp.proto.log_events.AudioStats> parser = PARSER;
        if (parser == null) {
          synchronized (com.halloapp.proto.log_events.AudioStats.class) {
            parser = PARSER;
            if (parser == null) {
              parser =
                  new DefaultInstanceBasedParser<com.halloapp.proto.log_events.AudioStats>(
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


  // @@protoc_insertion_point(class_scope:server.AudioStats)
  private static final com.halloapp.proto.log_events.AudioStats DEFAULT_INSTANCE;
  static {
    AudioStats defaultInstance = new AudioStats();
    // New instances are implicitly immutable so no need to make
    // immutable.
    DEFAULT_INSTANCE = defaultInstance;
    com.google.protobuf.GeneratedMessageLite.registerDefaultInstance(
      AudioStats.class, defaultInstance);
  }

  public static com.halloapp.proto.log_events.AudioStats getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static volatile com.google.protobuf.Parser<AudioStats> PARSER;

  public static com.google.protobuf.Parser<AudioStats> parser() {
    return DEFAULT_INSTANCE.getParserForType();
  }
}
