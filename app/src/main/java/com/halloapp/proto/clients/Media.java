// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: clients.proto

package com.halloapp.proto.clients;

/**
 * Protobuf type {@code clients.Media}
 */
public  final class Media extends
    com.google.protobuf.GeneratedMessageLite<
        Media, Media.Builder> implements
    // @@protoc_insertion_point(message_implements:clients.Media)
    MediaOrBuilder {
  private Media() {
    encryptionKey_ = com.google.protobuf.ByteString.EMPTY;
    plaintextHash_ = com.google.protobuf.ByteString.EMPTY;
    downloadUrl_ = "";
  }
  public static final int TYPE_FIELD_NUMBER = 1;
  private int type_;
  /**
   * <code>.clients.MediaType type = 1;</code>
   * @return The enum numeric value on the wire for type.
   */
  @java.lang.Override
  public int getTypeValue() {
    return type_;
  }
  /**
   * <code>.clients.MediaType type = 1;</code>
   * @return The type.
   */
  @java.lang.Override
  public com.halloapp.proto.clients.MediaType getType() {
    com.halloapp.proto.clients.MediaType result = com.halloapp.proto.clients.MediaType.forNumber(type_);
    return result == null ? com.halloapp.proto.clients.MediaType.UNRECOGNIZED : result;
  }
  /**
   * <code>.clients.MediaType type = 1;</code>
   * @param value The enum numeric value on the wire for type to set.
   */
  private void setTypeValue(int value) {
      type_ = value;
  }
  /**
   * <code>.clients.MediaType type = 1;</code>
   * @param value The type to set.
   */
  private void setType(com.halloapp.proto.clients.MediaType value) {
    type_ = value.getNumber();
    
  }
  /**
   * <code>.clients.MediaType type = 1;</code>
   */
  private void clearType() {
    
    type_ = 0;
  }

  public static final int WIDTH_FIELD_NUMBER = 2;
  private int width_;
  /**
   * <code>int32 width = 2;</code>
   * @return The width.
   */
  @java.lang.Override
  public int getWidth() {
    return width_;
  }
  /**
   * <code>int32 width = 2;</code>
   * @param value The width to set.
   */
  private void setWidth(int value) {
    
    width_ = value;
  }
  /**
   * <code>int32 width = 2;</code>
   */
  private void clearWidth() {
    
    width_ = 0;
  }

  public static final int HEIGHT_FIELD_NUMBER = 3;
  private int height_;
  /**
   * <code>int32 height = 3;</code>
   * @return The height.
   */
  @java.lang.Override
  public int getHeight() {
    return height_;
  }
  /**
   * <code>int32 height = 3;</code>
   * @param value The height to set.
   */
  private void setHeight(int value) {
    
    height_ = value;
  }
  /**
   * <code>int32 height = 3;</code>
   */
  private void clearHeight() {
    
    height_ = 0;
  }

  public static final int ENCRYPTION_KEY_FIELD_NUMBER = 4;
  private com.google.protobuf.ByteString encryptionKey_;
  /**
   * <code>bytes encryption_key = 4;</code>
   * @return The encryptionKey.
   */
  @java.lang.Override
  public com.google.protobuf.ByteString getEncryptionKey() {
    return encryptionKey_;
  }
  /**
   * <code>bytes encryption_key = 4;</code>
   * @param value The encryptionKey to set.
   */
  private void setEncryptionKey(com.google.protobuf.ByteString value) {
    value.getClass();
  
    encryptionKey_ = value;
  }
  /**
   * <code>bytes encryption_key = 4;</code>
   */
  private void clearEncryptionKey() {
    
    encryptionKey_ = getDefaultInstance().getEncryptionKey();
  }

  public static final int PLAINTEXT_HASH_FIELD_NUMBER = 5;
  private com.google.protobuf.ByteString plaintextHash_;
  /**
   * <code>bytes plaintext_hash = 5;</code>
   * @return The plaintextHash.
   */
  @java.lang.Override
  public com.google.protobuf.ByteString getPlaintextHash() {
    return plaintextHash_;
  }
  /**
   * <code>bytes plaintext_hash = 5;</code>
   * @param value The plaintextHash to set.
   */
  private void setPlaintextHash(com.google.protobuf.ByteString value) {
    value.getClass();
  
    plaintextHash_ = value;
  }
  /**
   * <code>bytes plaintext_hash = 5;</code>
   */
  private void clearPlaintextHash() {
    
    plaintextHash_ = getDefaultInstance().getPlaintextHash();
  }

  public static final int DOWNLOAD_URL_FIELD_NUMBER = 6;
  private java.lang.String downloadUrl_;
  /**
   * <code>string download_url = 6;</code>
   * @return The downloadUrl.
   */
  @java.lang.Override
  public java.lang.String getDownloadUrl() {
    return downloadUrl_;
  }
  /**
   * <code>string download_url = 6;</code>
   * @return The bytes for downloadUrl.
   */
  @java.lang.Override
  public com.google.protobuf.ByteString
      getDownloadUrlBytes() {
    return com.google.protobuf.ByteString.copyFromUtf8(downloadUrl_);
  }
  /**
   * <code>string download_url = 6;</code>
   * @param value The downloadUrl to set.
   */
  private void setDownloadUrl(
      java.lang.String value) {
    value.getClass();
  
    downloadUrl_ = value;
  }
  /**
   * <code>string download_url = 6;</code>
   */
  private void clearDownloadUrl() {
    
    downloadUrl_ = getDefaultInstance().getDownloadUrl();
  }
  /**
   * <code>string download_url = 6;</code>
   * @param value The bytes for downloadUrl to set.
   */
  private void setDownloadUrlBytes(
      com.google.protobuf.ByteString value) {
    checkByteStringIsUtf8(value);
    downloadUrl_ = value.toStringUtf8();
    
  }

  public static com.halloapp.proto.clients.Media parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static com.halloapp.proto.clients.Media parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static com.halloapp.proto.clients.Media parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static com.halloapp.proto.clients.Media parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static com.halloapp.proto.clients.Media parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static com.halloapp.proto.clients.Media parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static com.halloapp.proto.clients.Media parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input);
  }
  public static com.halloapp.proto.clients.Media parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input, extensionRegistry);
  }
  public static com.halloapp.proto.clients.Media parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return parseDelimitedFrom(DEFAULT_INSTANCE, input);
  }
  public static com.halloapp.proto.clients.Media parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return parseDelimitedFrom(DEFAULT_INSTANCE, input, extensionRegistry);
  }
  public static com.halloapp.proto.clients.Media parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input);
  }
  public static com.halloapp.proto.clients.Media parseFrom(
      com.google.protobuf.CodedInputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input, extensionRegistry);
  }

  public static Builder newBuilder() {
    return (Builder) DEFAULT_INSTANCE.createBuilder();
  }
  public static Builder newBuilder(com.halloapp.proto.clients.Media prototype) {
    return (Builder) DEFAULT_INSTANCE.createBuilder(prototype);
  }

  /**
   * Protobuf type {@code clients.Media}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessageLite.Builder<
        com.halloapp.proto.clients.Media, Builder> implements
      // @@protoc_insertion_point(builder_implements:clients.Media)
      com.halloapp.proto.clients.MediaOrBuilder {
    // Construct using com.halloapp.proto.clients.Media.newBuilder()
    private Builder() {
      super(DEFAULT_INSTANCE);
    }


    /**
     * <code>.clients.MediaType type = 1;</code>
     * @return The enum numeric value on the wire for type.
     */
    @java.lang.Override
    public int getTypeValue() {
      return instance.getTypeValue();
    }
    /**
     * <code>.clients.MediaType type = 1;</code>
     * @param value The type to set.
     * @return This builder for chaining.
     */
    public Builder setTypeValue(int value) {
      copyOnWrite();
      instance.setTypeValue(value);
      return this;
    }
    /**
     * <code>.clients.MediaType type = 1;</code>
     * @return The type.
     */
    @java.lang.Override
    public com.halloapp.proto.clients.MediaType getType() {
      return instance.getType();
    }
    /**
     * <code>.clients.MediaType type = 1;</code>
     * @param value The enum numeric value on the wire for type to set.
     * @return This builder for chaining.
     */
    public Builder setType(com.halloapp.proto.clients.MediaType value) {
      copyOnWrite();
      instance.setType(value);
      return this;
    }
    /**
     * <code>.clients.MediaType type = 1;</code>
     * @return This builder for chaining.
     */
    public Builder clearType() {
      copyOnWrite();
      instance.clearType();
      return this;
    }

    /**
     * <code>int32 width = 2;</code>
     * @return The width.
     */
    @java.lang.Override
    public int getWidth() {
      return instance.getWidth();
    }
    /**
     * <code>int32 width = 2;</code>
     * @param value The width to set.
     * @return This builder for chaining.
     */
    public Builder setWidth(int value) {
      copyOnWrite();
      instance.setWidth(value);
      return this;
    }
    /**
     * <code>int32 width = 2;</code>
     * @return This builder for chaining.
     */
    public Builder clearWidth() {
      copyOnWrite();
      instance.clearWidth();
      return this;
    }

    /**
     * <code>int32 height = 3;</code>
     * @return The height.
     */
    @java.lang.Override
    public int getHeight() {
      return instance.getHeight();
    }
    /**
     * <code>int32 height = 3;</code>
     * @param value The height to set.
     * @return This builder for chaining.
     */
    public Builder setHeight(int value) {
      copyOnWrite();
      instance.setHeight(value);
      return this;
    }
    /**
     * <code>int32 height = 3;</code>
     * @return This builder for chaining.
     */
    public Builder clearHeight() {
      copyOnWrite();
      instance.clearHeight();
      return this;
    }

    /**
     * <code>bytes encryption_key = 4;</code>
     * @return The encryptionKey.
     */
    @java.lang.Override
    public com.google.protobuf.ByteString getEncryptionKey() {
      return instance.getEncryptionKey();
    }
    /**
     * <code>bytes encryption_key = 4;</code>
     * @param value The encryptionKey to set.
     * @return This builder for chaining.
     */
    public Builder setEncryptionKey(com.google.protobuf.ByteString value) {
      copyOnWrite();
      instance.setEncryptionKey(value);
      return this;
    }
    /**
     * <code>bytes encryption_key = 4;</code>
     * @return This builder for chaining.
     */
    public Builder clearEncryptionKey() {
      copyOnWrite();
      instance.clearEncryptionKey();
      return this;
    }

    /**
     * <code>bytes plaintext_hash = 5;</code>
     * @return The plaintextHash.
     */
    @java.lang.Override
    public com.google.protobuf.ByteString getPlaintextHash() {
      return instance.getPlaintextHash();
    }
    /**
     * <code>bytes plaintext_hash = 5;</code>
     * @param value The plaintextHash to set.
     * @return This builder for chaining.
     */
    public Builder setPlaintextHash(com.google.protobuf.ByteString value) {
      copyOnWrite();
      instance.setPlaintextHash(value);
      return this;
    }
    /**
     * <code>bytes plaintext_hash = 5;</code>
     * @return This builder for chaining.
     */
    public Builder clearPlaintextHash() {
      copyOnWrite();
      instance.clearPlaintextHash();
      return this;
    }

    /**
     * <code>string download_url = 6;</code>
     * @return The downloadUrl.
     */
    @java.lang.Override
    public java.lang.String getDownloadUrl() {
      return instance.getDownloadUrl();
    }
    /**
     * <code>string download_url = 6;</code>
     * @return The bytes for downloadUrl.
     */
    @java.lang.Override
    public com.google.protobuf.ByteString
        getDownloadUrlBytes() {
      return instance.getDownloadUrlBytes();
    }
    /**
     * <code>string download_url = 6;</code>
     * @param value The downloadUrl to set.
     * @return This builder for chaining.
     */
    public Builder setDownloadUrl(
        java.lang.String value) {
      copyOnWrite();
      instance.setDownloadUrl(value);
      return this;
    }
    /**
     * <code>string download_url = 6;</code>
     * @return This builder for chaining.
     */
    public Builder clearDownloadUrl() {
      copyOnWrite();
      instance.clearDownloadUrl();
      return this;
    }
    /**
     * <code>string download_url = 6;</code>
     * @param value The bytes for downloadUrl to set.
     * @return This builder for chaining.
     */
    public Builder setDownloadUrlBytes(
        com.google.protobuf.ByteString value) {
      copyOnWrite();
      instance.setDownloadUrlBytes(value);
      return this;
    }

    // @@protoc_insertion_point(builder_scope:clients.Media)
  }
  @java.lang.Override
  @java.lang.SuppressWarnings({"unchecked", "fallthrough"})
  protected final java.lang.Object dynamicMethod(
      com.google.protobuf.GeneratedMessageLite.MethodToInvoke method,
      java.lang.Object arg0, java.lang.Object arg1) {
    switch (method) {
      case NEW_MUTABLE_INSTANCE: {
        return new com.halloapp.proto.clients.Media();
      }
      case NEW_BUILDER: {
        return new Builder();
      }
      case BUILD_MESSAGE_INFO: {
          java.lang.Object[] objects = new java.lang.Object[] {
            "type_",
            "width_",
            "height_",
            "encryptionKey_",
            "plaintextHash_",
            "downloadUrl_",
          };
          java.lang.String info =
              "\u0000\u0006\u0000\u0000\u0001\u0006\u0006\u0000\u0000\u0000\u0001\f\u0002\u0004" +
              "\u0003\u0004\u0004\n\u0005\n\u0006\u0208";
          return newMessageInfo(DEFAULT_INSTANCE, info, objects);
      }
      // fall through
      case GET_DEFAULT_INSTANCE: {
        return DEFAULT_INSTANCE;
      }
      case GET_PARSER: {
        com.google.protobuf.Parser<com.halloapp.proto.clients.Media> parser = PARSER;
        if (parser == null) {
          synchronized (com.halloapp.proto.clients.Media.class) {
            parser = PARSER;
            if (parser == null) {
              parser =
                  new DefaultInstanceBasedParser<com.halloapp.proto.clients.Media>(
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


  // @@protoc_insertion_point(class_scope:clients.Media)
  private static final com.halloapp.proto.clients.Media DEFAULT_INSTANCE;
  static {
    Media defaultInstance = new Media();
    // New instances are implicitly immutable so no need to make
    // immutable.
    DEFAULT_INSTANCE = defaultInstance;
    com.google.protobuf.GeneratedMessageLite.registerDefaultInstance(
      Media.class, defaultInstance);
  }

  public static com.halloapp.proto.clients.Media getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static volatile com.google.protobuf.Parser<Media> PARSER;

  public static com.google.protobuf.Parser<Media> parser() {
    return DEFAULT_INSTANCE.getParserForType();
  }
}

