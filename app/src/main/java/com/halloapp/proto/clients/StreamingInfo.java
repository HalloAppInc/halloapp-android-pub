// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: clients.proto

package com.halloapp.proto.clients;

/**
 * Protobuf type {@code clients.StreamingInfo}
 */
public  final class StreamingInfo extends
    com.google.protobuf.GeneratedMessageLite<
        StreamingInfo, StreamingInfo.Builder> implements
    // @@protoc_insertion_point(message_implements:clients.StreamingInfo)
    StreamingInfoOrBuilder {
  private StreamingInfo() {
  }
  public static final int BLOB_VERSION_FIELD_NUMBER = 1;
  private int blobVersion_;
  /**
   * <code>.clients.BlobVersion blob_version = 1;</code>
   * @return The enum numeric value on the wire for blobVersion.
   */
  @java.lang.Override
  public int getBlobVersionValue() {
    return blobVersion_;
  }
  /**
   * <code>.clients.BlobVersion blob_version = 1;</code>
   * @return The blobVersion.
   */
  @java.lang.Override
  public com.halloapp.proto.clients.BlobVersion getBlobVersion() {
    com.halloapp.proto.clients.BlobVersion result = com.halloapp.proto.clients.BlobVersion.forNumber(blobVersion_);
    return result == null ? com.halloapp.proto.clients.BlobVersion.UNRECOGNIZED : result;
  }
  /**
   * <code>.clients.BlobVersion blob_version = 1;</code>
   * @param value The enum numeric value on the wire for blobVersion to set.
   */
  private void setBlobVersionValue(int value) {
      blobVersion_ = value;
  }
  /**
   * <code>.clients.BlobVersion blob_version = 1;</code>
   * @param value The blobVersion to set.
   */
  private void setBlobVersion(com.halloapp.proto.clients.BlobVersion value) {
    blobVersion_ = value.getNumber();
    
  }
  /**
   * <code>.clients.BlobVersion blob_version = 1;</code>
   */
  private void clearBlobVersion() {
    
    blobVersion_ = 0;
  }

  public static final int CHUNK_SIZE_FIELD_NUMBER = 2;
  private int chunkSize_;
  /**
   * <code>int32 chunk_size = 2;</code>
   * @return The chunkSize.
   */
  @java.lang.Override
  public int getChunkSize() {
    return chunkSize_;
  }
  /**
   * <code>int32 chunk_size = 2;</code>
   * @param value The chunkSize to set.
   */
  private void setChunkSize(int value) {
    
    chunkSize_ = value;
  }
  /**
   * <code>int32 chunk_size = 2;</code>
   */
  private void clearChunkSize() {
    
    chunkSize_ = 0;
  }

  public static final int BLOB_SIZE_FIELD_NUMBER = 3;
  private long blobSize_;
  /**
   * <code>int64 blob_size = 3;</code>
   * @return The blobSize.
   */
  @java.lang.Override
  public long getBlobSize() {
    return blobSize_;
  }
  /**
   * <code>int64 blob_size = 3;</code>
   * @param value The blobSize to set.
   */
  private void setBlobSize(long value) {
    
    blobSize_ = value;
  }
  /**
   * <code>int64 blob_size = 3;</code>
   */
  private void clearBlobSize() {
    
    blobSize_ = 0L;
  }

  public static com.halloapp.proto.clients.StreamingInfo parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static com.halloapp.proto.clients.StreamingInfo parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static com.halloapp.proto.clients.StreamingInfo parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static com.halloapp.proto.clients.StreamingInfo parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static com.halloapp.proto.clients.StreamingInfo parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static com.halloapp.proto.clients.StreamingInfo parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static com.halloapp.proto.clients.StreamingInfo parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input);
  }
  public static com.halloapp.proto.clients.StreamingInfo parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input, extensionRegistry);
  }
  public static com.halloapp.proto.clients.StreamingInfo parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return parseDelimitedFrom(DEFAULT_INSTANCE, input);
  }
  public static com.halloapp.proto.clients.StreamingInfo parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return parseDelimitedFrom(DEFAULT_INSTANCE, input, extensionRegistry);
  }
  public static com.halloapp.proto.clients.StreamingInfo parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input);
  }
  public static com.halloapp.proto.clients.StreamingInfo parseFrom(
      com.google.protobuf.CodedInputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input, extensionRegistry);
  }

  public static Builder newBuilder() {
    return (Builder) DEFAULT_INSTANCE.createBuilder();
  }
  public static Builder newBuilder(com.halloapp.proto.clients.StreamingInfo prototype) {
    return (Builder) DEFAULT_INSTANCE.createBuilder(prototype);
  }

  /**
   * Protobuf type {@code clients.StreamingInfo}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessageLite.Builder<
        com.halloapp.proto.clients.StreamingInfo, Builder> implements
      // @@protoc_insertion_point(builder_implements:clients.StreamingInfo)
      com.halloapp.proto.clients.StreamingInfoOrBuilder {
    // Construct using com.halloapp.proto.clients.StreamingInfo.newBuilder()
    private Builder() {
      super(DEFAULT_INSTANCE);
    }


    /**
     * <code>.clients.BlobVersion blob_version = 1;</code>
     * @return The enum numeric value on the wire for blobVersion.
     */
    @java.lang.Override
    public int getBlobVersionValue() {
      return instance.getBlobVersionValue();
    }
    /**
     * <code>.clients.BlobVersion blob_version = 1;</code>
     * @param value The blobVersion to set.
     * @return This builder for chaining.
     */
    public Builder setBlobVersionValue(int value) {
      copyOnWrite();
      instance.setBlobVersionValue(value);
      return this;
    }
    /**
     * <code>.clients.BlobVersion blob_version = 1;</code>
     * @return The blobVersion.
     */
    @java.lang.Override
    public com.halloapp.proto.clients.BlobVersion getBlobVersion() {
      return instance.getBlobVersion();
    }
    /**
     * <code>.clients.BlobVersion blob_version = 1;</code>
     * @param value The enum numeric value on the wire for blobVersion to set.
     * @return This builder for chaining.
     */
    public Builder setBlobVersion(com.halloapp.proto.clients.BlobVersion value) {
      copyOnWrite();
      instance.setBlobVersion(value);
      return this;
    }
    /**
     * <code>.clients.BlobVersion blob_version = 1;</code>
     * @return This builder for chaining.
     */
    public Builder clearBlobVersion() {
      copyOnWrite();
      instance.clearBlobVersion();
      return this;
    }

    /**
     * <code>int32 chunk_size = 2;</code>
     * @return The chunkSize.
     */
    @java.lang.Override
    public int getChunkSize() {
      return instance.getChunkSize();
    }
    /**
     * <code>int32 chunk_size = 2;</code>
     * @param value The chunkSize to set.
     * @return This builder for chaining.
     */
    public Builder setChunkSize(int value) {
      copyOnWrite();
      instance.setChunkSize(value);
      return this;
    }
    /**
     * <code>int32 chunk_size = 2;</code>
     * @return This builder for chaining.
     */
    public Builder clearChunkSize() {
      copyOnWrite();
      instance.clearChunkSize();
      return this;
    }

    /**
     * <code>int64 blob_size = 3;</code>
     * @return The blobSize.
     */
    @java.lang.Override
    public long getBlobSize() {
      return instance.getBlobSize();
    }
    /**
     * <code>int64 blob_size = 3;</code>
     * @param value The blobSize to set.
     * @return This builder for chaining.
     */
    public Builder setBlobSize(long value) {
      copyOnWrite();
      instance.setBlobSize(value);
      return this;
    }
    /**
     * <code>int64 blob_size = 3;</code>
     * @return This builder for chaining.
     */
    public Builder clearBlobSize() {
      copyOnWrite();
      instance.clearBlobSize();
      return this;
    }

    // @@protoc_insertion_point(builder_scope:clients.StreamingInfo)
  }
  @java.lang.Override
  @java.lang.SuppressWarnings({"unchecked", "fallthrough"})
  protected final java.lang.Object dynamicMethod(
      com.google.protobuf.GeneratedMessageLite.MethodToInvoke method,
      java.lang.Object arg0, java.lang.Object arg1) {
    switch (method) {
      case NEW_MUTABLE_INSTANCE: {
        return new com.halloapp.proto.clients.StreamingInfo();
      }
      case NEW_BUILDER: {
        return new Builder();
      }
      case BUILD_MESSAGE_INFO: {
          java.lang.Object[] objects = new java.lang.Object[] {
            "blobVersion_",
            "chunkSize_",
            "blobSize_",
          };
          java.lang.String info =
              "\u0000\u0003\u0000\u0000\u0001\u0003\u0003\u0000\u0000\u0000\u0001\f\u0002\u0004" +
              "\u0003\u0002";
          return newMessageInfo(DEFAULT_INSTANCE, info, objects);
      }
      // fall through
      case GET_DEFAULT_INSTANCE: {
        return DEFAULT_INSTANCE;
      }
      case GET_PARSER: {
        com.google.protobuf.Parser<com.halloapp.proto.clients.StreamingInfo> parser = PARSER;
        if (parser == null) {
          synchronized (com.halloapp.proto.clients.StreamingInfo.class) {
            parser = PARSER;
            if (parser == null) {
              parser =
                  new DefaultInstanceBasedParser<com.halloapp.proto.clients.StreamingInfo>(
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


  // @@protoc_insertion_point(class_scope:clients.StreamingInfo)
  private static final com.halloapp.proto.clients.StreamingInfo DEFAULT_INSTANCE;
  static {
    StreamingInfo defaultInstance = new StreamingInfo();
    // New instances are implicitly immutable so no need to make
    // immutable.
    DEFAULT_INSTANCE = defaultInstance;
    com.google.protobuf.GeneratedMessageLite.registerDefaultInstance(
      StreamingInfo.class, defaultInstance);
  }

  public static com.halloapp.proto.clients.StreamingInfo getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static volatile com.google.protobuf.Parser<StreamingInfo> PARSER;

  public static com.google.protobuf.Parser<StreamingInfo> parser() {
    return DEFAULT_INSTANCE.getParserForType();
  }
}
