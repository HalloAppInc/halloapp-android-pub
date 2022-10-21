// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: web.proto

package com.halloapp.proto.web;

/**
 * Protobuf type {@code web.ReceiptUpdate}
 */
public  final class ReceiptUpdate extends
    com.google.protobuf.GeneratedMessageLite<
        ReceiptUpdate, ReceiptUpdate.Builder> implements
    // @@protoc_insertion_point(message_implements:web.ReceiptUpdate)
    ReceiptUpdateOrBuilder {
  private ReceiptUpdate() {
    id_ = "";
    contentId_ = "";
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

  public static final int CONTENT_ID_FIELD_NUMBER = 2;
  private java.lang.String contentId_;
  /**
   * <code>string content_id = 2;</code>
   * @return The contentId.
   */
  @java.lang.Override
  public java.lang.String getContentId() {
    return contentId_;
  }
  /**
   * <code>string content_id = 2;</code>
   * @return The bytes for contentId.
   */
  @java.lang.Override
  public com.google.protobuf.ByteString
      getContentIdBytes() {
    return com.google.protobuf.ByteString.copyFromUtf8(contentId_);
  }
  /**
   * <code>string content_id = 2;</code>
   * @param value The contentId to set.
   */
  private void setContentId(
      java.lang.String value) {
    value.getClass();
  
    contentId_ = value;
  }
  /**
   * <code>string content_id = 2;</code>
   */
  private void clearContentId() {
    
    contentId_ = getDefaultInstance().getContentId();
  }
  /**
   * <code>string content_id = 2;</code>
   * @param value The bytes for contentId to set.
   */
  private void setContentIdBytes(
      com.google.protobuf.ByteString value) {
    checkByteStringIsUtf8(value);
    contentId_ = value.toStringUtf8();
    
  }

  public static final int RECEIPT_FIELD_NUMBER = 3;
  private com.halloapp.proto.web.ReceiptInfo receipt_;
  /**
   * <code>.web.ReceiptInfo receipt = 3;</code>
   */
  @java.lang.Override
  public boolean hasReceipt() {
    return receipt_ != null;
  }
  /**
   * <code>.web.ReceiptInfo receipt = 3;</code>
   */
  @java.lang.Override
  public com.halloapp.proto.web.ReceiptInfo getReceipt() {
    return receipt_ == null ? com.halloapp.proto.web.ReceiptInfo.getDefaultInstance() : receipt_;
  }
  /**
   * <code>.web.ReceiptInfo receipt = 3;</code>
   */
  private void setReceipt(com.halloapp.proto.web.ReceiptInfo value) {
    value.getClass();
  receipt_ = value;
    
    }
  /**
   * <code>.web.ReceiptInfo receipt = 3;</code>
   */
  @java.lang.SuppressWarnings({"ReferenceEquality"})
  private void mergeReceipt(com.halloapp.proto.web.ReceiptInfo value) {
    value.getClass();
  if (receipt_ != null &&
        receipt_ != com.halloapp.proto.web.ReceiptInfo.getDefaultInstance()) {
      receipt_ =
        com.halloapp.proto.web.ReceiptInfo.newBuilder(receipt_).mergeFrom(value).buildPartial();
    } else {
      receipt_ = value;
    }
    
  }
  /**
   * <code>.web.ReceiptInfo receipt = 3;</code>
   */
  private void clearReceipt() {  receipt_ = null;
    
  }

  public static com.halloapp.proto.web.ReceiptUpdate parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static com.halloapp.proto.web.ReceiptUpdate parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static com.halloapp.proto.web.ReceiptUpdate parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static com.halloapp.proto.web.ReceiptUpdate parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static com.halloapp.proto.web.ReceiptUpdate parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static com.halloapp.proto.web.ReceiptUpdate parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static com.halloapp.proto.web.ReceiptUpdate parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input);
  }
  public static com.halloapp.proto.web.ReceiptUpdate parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input, extensionRegistry);
  }
  public static com.halloapp.proto.web.ReceiptUpdate parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return parseDelimitedFrom(DEFAULT_INSTANCE, input);
  }
  public static com.halloapp.proto.web.ReceiptUpdate parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return parseDelimitedFrom(DEFAULT_INSTANCE, input, extensionRegistry);
  }
  public static com.halloapp.proto.web.ReceiptUpdate parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input);
  }
  public static com.halloapp.proto.web.ReceiptUpdate parseFrom(
      com.google.protobuf.CodedInputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input, extensionRegistry);
  }

  public static Builder newBuilder() {
    return (Builder) DEFAULT_INSTANCE.createBuilder();
  }
  public static Builder newBuilder(com.halloapp.proto.web.ReceiptUpdate prototype) {
    return (Builder) DEFAULT_INSTANCE.createBuilder(prototype);
  }

  /**
   * Protobuf type {@code web.ReceiptUpdate}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessageLite.Builder<
        com.halloapp.proto.web.ReceiptUpdate, Builder> implements
      // @@protoc_insertion_point(builder_implements:web.ReceiptUpdate)
      com.halloapp.proto.web.ReceiptUpdateOrBuilder {
    // Construct using com.halloapp.proto.web.ReceiptUpdate.newBuilder()
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
     * <code>string content_id = 2;</code>
     * @return The contentId.
     */
    @java.lang.Override
    public java.lang.String getContentId() {
      return instance.getContentId();
    }
    /**
     * <code>string content_id = 2;</code>
     * @return The bytes for contentId.
     */
    @java.lang.Override
    public com.google.protobuf.ByteString
        getContentIdBytes() {
      return instance.getContentIdBytes();
    }
    /**
     * <code>string content_id = 2;</code>
     * @param value The contentId to set.
     * @return This builder for chaining.
     */
    public Builder setContentId(
        java.lang.String value) {
      copyOnWrite();
      instance.setContentId(value);
      return this;
    }
    /**
     * <code>string content_id = 2;</code>
     * @return This builder for chaining.
     */
    public Builder clearContentId() {
      copyOnWrite();
      instance.clearContentId();
      return this;
    }
    /**
     * <code>string content_id = 2;</code>
     * @param value The bytes for contentId to set.
     * @return This builder for chaining.
     */
    public Builder setContentIdBytes(
        com.google.protobuf.ByteString value) {
      copyOnWrite();
      instance.setContentIdBytes(value);
      return this;
    }

    /**
     * <code>.web.ReceiptInfo receipt = 3;</code>
     */
    @java.lang.Override
    public boolean hasReceipt() {
      return instance.hasReceipt();
    }
    /**
     * <code>.web.ReceiptInfo receipt = 3;</code>
     */
    @java.lang.Override
    public com.halloapp.proto.web.ReceiptInfo getReceipt() {
      return instance.getReceipt();
    }
    /**
     * <code>.web.ReceiptInfo receipt = 3;</code>
     */
    public Builder setReceipt(com.halloapp.proto.web.ReceiptInfo value) {
      copyOnWrite();
      instance.setReceipt(value);
      return this;
      }
    /**
     * <code>.web.ReceiptInfo receipt = 3;</code>
     */
    public Builder setReceipt(
        com.halloapp.proto.web.ReceiptInfo.Builder builderForValue) {
      copyOnWrite();
      instance.setReceipt(builderForValue.build());
      return this;
    }
    /**
     * <code>.web.ReceiptInfo receipt = 3;</code>
     */
    public Builder mergeReceipt(com.halloapp.proto.web.ReceiptInfo value) {
      copyOnWrite();
      instance.mergeReceipt(value);
      return this;
    }
    /**
     * <code>.web.ReceiptInfo receipt = 3;</code>
     */
    public Builder clearReceipt() {  copyOnWrite();
      instance.clearReceipt();
      return this;
    }

    // @@protoc_insertion_point(builder_scope:web.ReceiptUpdate)
  }
  @java.lang.Override
  @java.lang.SuppressWarnings({"unchecked", "fallthrough"})
  protected final java.lang.Object dynamicMethod(
      com.google.protobuf.GeneratedMessageLite.MethodToInvoke method,
      java.lang.Object arg0, java.lang.Object arg1) {
    switch (method) {
      case NEW_MUTABLE_INSTANCE: {
        return new com.halloapp.proto.web.ReceiptUpdate();
      }
      case NEW_BUILDER: {
        return new Builder();
      }
      case BUILD_MESSAGE_INFO: {
          java.lang.Object[] objects = new java.lang.Object[] {
            "id_",
            "contentId_",
            "receipt_",
          };
          java.lang.String info =
              "\u0000\u0003\u0000\u0000\u0001\u0003\u0003\u0000\u0000\u0000\u0001\u0208\u0002\u0208" +
              "\u0003\t";
          return newMessageInfo(DEFAULT_INSTANCE, info, objects);
      }
      // fall through
      case GET_DEFAULT_INSTANCE: {
        return DEFAULT_INSTANCE;
      }
      case GET_PARSER: {
        com.google.protobuf.Parser<com.halloapp.proto.web.ReceiptUpdate> parser = PARSER;
        if (parser == null) {
          synchronized (com.halloapp.proto.web.ReceiptUpdate.class) {
            parser = PARSER;
            if (parser == null) {
              parser =
                  new DefaultInstanceBasedParser<com.halloapp.proto.web.ReceiptUpdate>(
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


  // @@protoc_insertion_point(class_scope:web.ReceiptUpdate)
  private static final com.halloapp.proto.web.ReceiptUpdate DEFAULT_INSTANCE;
  static {
    ReceiptUpdate defaultInstance = new ReceiptUpdate();
    // New instances are implicitly immutable so no need to make
    // immutable.
    DEFAULT_INSTANCE = defaultInstance;
    com.google.protobuf.GeneratedMessageLite.registerDefaultInstance(
      ReceiptUpdate.class, defaultInstance);
  }

  public static com.halloapp.proto.web.ReceiptUpdate getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static volatile com.google.protobuf.Parser<ReceiptUpdate> PARSER;

  public static com.google.protobuf.Parser<ReceiptUpdate> parser() {
    return DEFAULT_INSTANCE.getParserForType();
  }
}

