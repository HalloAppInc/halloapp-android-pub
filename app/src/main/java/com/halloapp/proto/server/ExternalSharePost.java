// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: server.proto

package com.halloapp.proto.server;

/**
 * Protobuf type {@code server.ExternalSharePost}
 */
public  final class ExternalSharePost extends
    com.google.protobuf.GeneratedMessageLite<
        ExternalSharePost, ExternalSharePost.Builder> implements
    // @@protoc_insertion_point(message_implements:server.ExternalSharePost)
    ExternalSharePostOrBuilder {
  private ExternalSharePost() {
    blobId_ = "";
    blob_ = com.google.protobuf.ByteString.EMPTY;
  }
  /**
   * Protobuf enum {@code server.ExternalSharePost.Action}
   */
  public enum Action
      implements com.google.protobuf.Internal.EnumLite {
    /**
     * <code>STORE = 0;</code>
     */
    STORE(0),
    /**
     * <code>DELETE = 1;</code>
     */
    DELETE(1),
    /**
     * <code>GET = 2;</code>
     */
    GET(2),
    UNRECOGNIZED(-1),
    ;

    /**
     * <code>STORE = 0;</code>
     */
    public static final int STORE_VALUE = 0;
    /**
     * <code>DELETE = 1;</code>
     */
    public static final int DELETE_VALUE = 1;
    /**
     * <code>GET = 2;</code>
     */
    public static final int GET_VALUE = 2;


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
        case 0: return STORE;
        case 1: return DELETE;
        case 2: return GET;
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

    // @@protoc_insertion_point(enum_scope:server.ExternalSharePost.Action)
  }

  public static final int ACTION_FIELD_NUMBER = 1;
  private int action_;
  /**
   * <code>.server.ExternalSharePost.Action action = 1;</code>
   * @return The enum numeric value on the wire for action.
   */
  @java.lang.Override
  public int getActionValue() {
    return action_;
  }
  /**
   * <code>.server.ExternalSharePost.Action action = 1;</code>
   * @return The action.
   */
  @java.lang.Override
  public com.halloapp.proto.server.ExternalSharePost.Action getAction() {
    com.halloapp.proto.server.ExternalSharePost.Action result = com.halloapp.proto.server.ExternalSharePost.Action.forNumber(action_);
    return result == null ? com.halloapp.proto.server.ExternalSharePost.Action.UNRECOGNIZED : result;
  }
  /**
   * <code>.server.ExternalSharePost.Action action = 1;</code>
   * @param value The enum numeric value on the wire for action to set.
   */
  private void setActionValue(int value) {
      action_ = value;
  }
  /**
   * <code>.server.ExternalSharePost.Action action = 1;</code>
   * @param value The action to set.
   */
  private void setAction(com.halloapp.proto.server.ExternalSharePost.Action value) {
    action_ = value.getNumber();
    
  }
  /**
   * <code>.server.ExternalSharePost.Action action = 1;</code>
   */
  private void clearAction() {
    
    action_ = 0;
  }

  public static final int BLOB_ID_FIELD_NUMBER = 2;
  private java.lang.String blobId_;
  /**
   * <code>string blob_id = 2;</code>
   * @return The blobId.
   */
  @java.lang.Override
  public java.lang.String getBlobId() {
    return blobId_;
  }
  /**
   * <code>string blob_id = 2;</code>
   * @return The bytes for blobId.
   */
  @java.lang.Override
  public com.google.protobuf.ByteString
      getBlobIdBytes() {
    return com.google.protobuf.ByteString.copyFromUtf8(blobId_);
  }
  /**
   * <code>string blob_id = 2;</code>
   * @param value The blobId to set.
   */
  private void setBlobId(
      java.lang.String value) {
    value.getClass();
  
    blobId_ = value;
  }
  /**
   * <code>string blob_id = 2;</code>
   */
  private void clearBlobId() {
    
    blobId_ = getDefaultInstance().getBlobId();
  }
  /**
   * <code>string blob_id = 2;</code>
   * @param value The bytes for blobId to set.
   */
  private void setBlobIdBytes(
      com.google.protobuf.ByteString value) {
    checkByteStringIsUtf8(value);
    blobId_ = value.toStringUtf8();
    
  }

  public static final int BLOB_FIELD_NUMBER = 3;
  private com.google.protobuf.ByteString blob_;
  /**
   * <code>bytes blob = 3;</code>
   * @return The blob.
   */
  @java.lang.Override
  public com.google.protobuf.ByteString getBlob() {
    return blob_;
  }
  /**
   * <code>bytes blob = 3;</code>
   * @param value The blob to set.
   */
  private void setBlob(com.google.protobuf.ByteString value) {
    value.getClass();
  
    blob_ = value;
  }
  /**
   * <code>bytes blob = 3;</code>
   */
  private void clearBlob() {
    
    blob_ = getDefaultInstance().getBlob();
  }

  public static final int EXPIRES_IN_SECONDS_FIELD_NUMBER = 4;
  private long expiresInSeconds_;
  /**
   * <code>int64 expires_in_seconds = 4;</code>
   * @return The expiresInSeconds.
   */
  @java.lang.Override
  public long getExpiresInSeconds() {
    return expiresInSeconds_;
  }
  /**
   * <code>int64 expires_in_seconds = 4;</code>
   * @param value The expiresInSeconds to set.
   */
  private void setExpiresInSeconds(long value) {
    
    expiresInSeconds_ = value;
  }
  /**
   * <code>int64 expires_in_seconds = 4;</code>
   */
  private void clearExpiresInSeconds() {
    
    expiresInSeconds_ = 0L;
  }

  public static final int OG_TAG_INFO_FIELD_NUMBER = 5;
  private com.halloapp.proto.server.OgTagInfo ogTagInfo_;
  /**
   * <code>.server.OgTagInfo og_tag_info = 5;</code>
   */
  @java.lang.Override
  public boolean hasOgTagInfo() {
    return ogTagInfo_ != null;
  }
  /**
   * <code>.server.OgTagInfo og_tag_info = 5;</code>
   */
  @java.lang.Override
  public com.halloapp.proto.server.OgTagInfo getOgTagInfo() {
    return ogTagInfo_ == null ? com.halloapp.proto.server.OgTagInfo.getDefaultInstance() : ogTagInfo_;
  }
  /**
   * <code>.server.OgTagInfo og_tag_info = 5;</code>
   */
  private void setOgTagInfo(com.halloapp.proto.server.OgTagInfo value) {
    value.getClass();
  ogTagInfo_ = value;
    
    }
  /**
   * <code>.server.OgTagInfo og_tag_info = 5;</code>
   */
  @java.lang.SuppressWarnings({"ReferenceEquality"})
  private void mergeOgTagInfo(com.halloapp.proto.server.OgTagInfo value) {
    value.getClass();
  if (ogTagInfo_ != null &&
        ogTagInfo_ != com.halloapp.proto.server.OgTagInfo.getDefaultInstance()) {
      ogTagInfo_ =
        com.halloapp.proto.server.OgTagInfo.newBuilder(ogTagInfo_).mergeFrom(value).buildPartial();
    } else {
      ogTagInfo_ = value;
    }
    
  }
  /**
   * <code>.server.OgTagInfo og_tag_info = 5;</code>
   */
  private void clearOgTagInfo() {  ogTagInfo_ = null;
    
  }

  public static com.halloapp.proto.server.ExternalSharePost parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static com.halloapp.proto.server.ExternalSharePost parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static com.halloapp.proto.server.ExternalSharePost parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static com.halloapp.proto.server.ExternalSharePost parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static com.halloapp.proto.server.ExternalSharePost parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static com.halloapp.proto.server.ExternalSharePost parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static com.halloapp.proto.server.ExternalSharePost parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input);
  }
  public static com.halloapp.proto.server.ExternalSharePost parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input, extensionRegistry);
  }
  public static com.halloapp.proto.server.ExternalSharePost parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return parseDelimitedFrom(DEFAULT_INSTANCE, input);
  }
  public static com.halloapp.proto.server.ExternalSharePost parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return parseDelimitedFrom(DEFAULT_INSTANCE, input, extensionRegistry);
  }
  public static com.halloapp.proto.server.ExternalSharePost parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input);
  }
  public static com.halloapp.proto.server.ExternalSharePost parseFrom(
      com.google.protobuf.CodedInputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input, extensionRegistry);
  }

  public static Builder newBuilder() {
    return (Builder) DEFAULT_INSTANCE.createBuilder();
  }
  public static Builder newBuilder(com.halloapp.proto.server.ExternalSharePost prototype) {
    return (Builder) DEFAULT_INSTANCE.createBuilder(prototype);
  }

  /**
   * Protobuf type {@code server.ExternalSharePost}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessageLite.Builder<
        com.halloapp.proto.server.ExternalSharePost, Builder> implements
      // @@protoc_insertion_point(builder_implements:server.ExternalSharePost)
      com.halloapp.proto.server.ExternalSharePostOrBuilder {
    // Construct using com.halloapp.proto.server.ExternalSharePost.newBuilder()
    private Builder() {
      super(DEFAULT_INSTANCE);
    }


    /**
     * <code>.server.ExternalSharePost.Action action = 1;</code>
     * @return The enum numeric value on the wire for action.
     */
    @java.lang.Override
    public int getActionValue() {
      return instance.getActionValue();
    }
    /**
     * <code>.server.ExternalSharePost.Action action = 1;</code>
     * @param value The action to set.
     * @return This builder for chaining.
     */
    public Builder setActionValue(int value) {
      copyOnWrite();
      instance.setActionValue(value);
      return this;
    }
    /**
     * <code>.server.ExternalSharePost.Action action = 1;</code>
     * @return The action.
     */
    @java.lang.Override
    public com.halloapp.proto.server.ExternalSharePost.Action getAction() {
      return instance.getAction();
    }
    /**
     * <code>.server.ExternalSharePost.Action action = 1;</code>
     * @param value The enum numeric value on the wire for action to set.
     * @return This builder for chaining.
     */
    public Builder setAction(com.halloapp.proto.server.ExternalSharePost.Action value) {
      copyOnWrite();
      instance.setAction(value);
      return this;
    }
    /**
     * <code>.server.ExternalSharePost.Action action = 1;</code>
     * @return This builder for chaining.
     */
    public Builder clearAction() {
      copyOnWrite();
      instance.clearAction();
      return this;
    }

    /**
     * <code>string blob_id = 2;</code>
     * @return The blobId.
     */
    @java.lang.Override
    public java.lang.String getBlobId() {
      return instance.getBlobId();
    }
    /**
     * <code>string blob_id = 2;</code>
     * @return The bytes for blobId.
     */
    @java.lang.Override
    public com.google.protobuf.ByteString
        getBlobIdBytes() {
      return instance.getBlobIdBytes();
    }
    /**
     * <code>string blob_id = 2;</code>
     * @param value The blobId to set.
     * @return This builder for chaining.
     */
    public Builder setBlobId(
        java.lang.String value) {
      copyOnWrite();
      instance.setBlobId(value);
      return this;
    }
    /**
     * <code>string blob_id = 2;</code>
     * @return This builder for chaining.
     */
    public Builder clearBlobId() {
      copyOnWrite();
      instance.clearBlobId();
      return this;
    }
    /**
     * <code>string blob_id = 2;</code>
     * @param value The bytes for blobId to set.
     * @return This builder for chaining.
     */
    public Builder setBlobIdBytes(
        com.google.protobuf.ByteString value) {
      copyOnWrite();
      instance.setBlobIdBytes(value);
      return this;
    }

    /**
     * <code>bytes blob = 3;</code>
     * @return The blob.
     */
    @java.lang.Override
    public com.google.protobuf.ByteString getBlob() {
      return instance.getBlob();
    }
    /**
     * <code>bytes blob = 3;</code>
     * @param value The blob to set.
     * @return This builder for chaining.
     */
    public Builder setBlob(com.google.protobuf.ByteString value) {
      copyOnWrite();
      instance.setBlob(value);
      return this;
    }
    /**
     * <code>bytes blob = 3;</code>
     * @return This builder for chaining.
     */
    public Builder clearBlob() {
      copyOnWrite();
      instance.clearBlob();
      return this;
    }

    /**
     * <code>int64 expires_in_seconds = 4;</code>
     * @return The expiresInSeconds.
     */
    @java.lang.Override
    public long getExpiresInSeconds() {
      return instance.getExpiresInSeconds();
    }
    /**
     * <code>int64 expires_in_seconds = 4;</code>
     * @param value The expiresInSeconds to set.
     * @return This builder for chaining.
     */
    public Builder setExpiresInSeconds(long value) {
      copyOnWrite();
      instance.setExpiresInSeconds(value);
      return this;
    }
    /**
     * <code>int64 expires_in_seconds = 4;</code>
     * @return This builder for chaining.
     */
    public Builder clearExpiresInSeconds() {
      copyOnWrite();
      instance.clearExpiresInSeconds();
      return this;
    }

    /**
     * <code>.server.OgTagInfo og_tag_info = 5;</code>
     */
    @java.lang.Override
    public boolean hasOgTagInfo() {
      return instance.hasOgTagInfo();
    }
    /**
     * <code>.server.OgTagInfo og_tag_info = 5;</code>
     */
    @java.lang.Override
    public com.halloapp.proto.server.OgTagInfo getOgTagInfo() {
      return instance.getOgTagInfo();
    }
    /**
     * <code>.server.OgTagInfo og_tag_info = 5;</code>
     */
    public Builder setOgTagInfo(com.halloapp.proto.server.OgTagInfo value) {
      copyOnWrite();
      instance.setOgTagInfo(value);
      return this;
      }
    /**
     * <code>.server.OgTagInfo og_tag_info = 5;</code>
     */
    public Builder setOgTagInfo(
        com.halloapp.proto.server.OgTagInfo.Builder builderForValue) {
      copyOnWrite();
      instance.setOgTagInfo(builderForValue.build());
      return this;
    }
    /**
     * <code>.server.OgTagInfo og_tag_info = 5;</code>
     */
    public Builder mergeOgTagInfo(com.halloapp.proto.server.OgTagInfo value) {
      copyOnWrite();
      instance.mergeOgTagInfo(value);
      return this;
    }
    /**
     * <code>.server.OgTagInfo og_tag_info = 5;</code>
     */
    public Builder clearOgTagInfo() {  copyOnWrite();
      instance.clearOgTagInfo();
      return this;
    }

    // @@protoc_insertion_point(builder_scope:server.ExternalSharePost)
  }
  @java.lang.Override
  @java.lang.SuppressWarnings({"unchecked", "fallthrough"})
  protected final java.lang.Object dynamicMethod(
      com.google.protobuf.GeneratedMessageLite.MethodToInvoke method,
      java.lang.Object arg0, java.lang.Object arg1) {
    switch (method) {
      case NEW_MUTABLE_INSTANCE: {
        return new com.halloapp.proto.server.ExternalSharePost();
      }
      case NEW_BUILDER: {
        return new Builder();
      }
      case BUILD_MESSAGE_INFO: {
          java.lang.Object[] objects = new java.lang.Object[] {
            "action_",
            "blobId_",
            "blob_",
            "expiresInSeconds_",
            "ogTagInfo_",
          };
          java.lang.String info =
              "\u0000\u0005\u0000\u0000\u0001\u0005\u0005\u0000\u0000\u0000\u0001\f\u0002\u0208" +
              "\u0003\n\u0004\u0002\u0005\t";
          return newMessageInfo(DEFAULT_INSTANCE, info, objects);
      }
      // fall through
      case GET_DEFAULT_INSTANCE: {
        return DEFAULT_INSTANCE;
      }
      case GET_PARSER: {
        com.google.protobuf.Parser<com.halloapp.proto.server.ExternalSharePost> parser = PARSER;
        if (parser == null) {
          synchronized (com.halloapp.proto.server.ExternalSharePost.class) {
            parser = PARSER;
            if (parser == null) {
              parser =
                  new DefaultInstanceBasedParser<com.halloapp.proto.server.ExternalSharePost>(
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


  // @@protoc_insertion_point(class_scope:server.ExternalSharePost)
  private static final com.halloapp.proto.server.ExternalSharePost DEFAULT_INSTANCE;
  static {
    ExternalSharePost defaultInstance = new ExternalSharePost();
    // New instances are implicitly immutable so no need to make
    // immutable.
    DEFAULT_INSTANCE = defaultInstance;
    com.google.protobuf.GeneratedMessageLite.registerDefaultInstance(
      ExternalSharePost.class, defaultInstance);
  }

  public static com.halloapp.proto.server.ExternalSharePost getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static volatile com.google.protobuf.Parser<ExternalSharePost> PARSER;

  public static com.google.protobuf.Parser<ExternalSharePost> parser() {
    return DEFAULT_INSTANCE.getParserForType();
  }
}

