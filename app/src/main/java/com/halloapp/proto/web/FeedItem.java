// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: web.proto

package com.halloapp.proto.web;

/**
 * Protobuf type {@code web.FeedItem}
 */
public  final class FeedItem extends
    com.google.protobuf.GeneratedMessageLite<
        FeedItem, FeedItem.Builder> implements
    // @@protoc_insertion_point(message_implements:web.FeedItem)
    FeedItemOrBuilder {
  private FeedItem() {
    groupId_ = "";
  }
  private int contentCase_ = 0;
  private java.lang.Object content_;
  public enum ContentCase {
    POST(1),
    COMMENT(2),
    CONTENT_NOT_SET(0);
    private final int value;
    private ContentCase(int value) {
      this.value = value;
    }
    /**
     * @deprecated Use {@link #forNumber(int)} instead.
     */
    @java.lang.Deprecated
    public static ContentCase valueOf(int value) {
      return forNumber(value);
    }

    public static ContentCase forNumber(int value) {
      switch (value) {
        case 1: return POST;
        case 2: return COMMENT;
        case 0: return CONTENT_NOT_SET;
        default: return null;
      }
    }
    public int getNumber() {
      return this.value;
    }
  };

  @java.lang.Override
  public ContentCase
  getContentCase() {
    return ContentCase.forNumber(
        contentCase_);
  }

  private void clearContent() {
    contentCase_ = 0;
    content_ = null;
  }

  public static final int POST_FIELD_NUMBER = 1;
  /**
   * <code>.server.Post post = 1;</code>
   */
  @java.lang.Override
  public boolean hasPost() {
    return contentCase_ == 1;
  }
  /**
   * <code>.server.Post post = 1;</code>
   */
  @java.lang.Override
  public com.halloapp.proto.server.Post getPost() {
    if (contentCase_ == 1) {
       return (com.halloapp.proto.server.Post) content_;
    }
    return com.halloapp.proto.server.Post.getDefaultInstance();
  }
  /**
   * <code>.server.Post post = 1;</code>
   */
  private void setPost(com.halloapp.proto.server.Post value) {
    value.getClass();
  content_ = value;
    contentCase_ = 1;
  }
  /**
   * <code>.server.Post post = 1;</code>
   */
  private void mergePost(com.halloapp.proto.server.Post value) {
    value.getClass();
  if (contentCase_ == 1 &&
        content_ != com.halloapp.proto.server.Post.getDefaultInstance()) {
      content_ = com.halloapp.proto.server.Post.newBuilder((com.halloapp.proto.server.Post) content_)
          .mergeFrom(value).buildPartial();
    } else {
      content_ = value;
    }
    contentCase_ = 1;
  }
  /**
   * <code>.server.Post post = 1;</code>
   */
  private void clearPost() {
    if (contentCase_ == 1) {
      contentCase_ = 0;
      content_ = null;
    }
  }

  public static final int COMMENT_FIELD_NUMBER = 2;
  /**
   * <code>.server.Comment comment = 2;</code>
   */
  @java.lang.Override
  public boolean hasComment() {
    return contentCase_ == 2;
  }
  /**
   * <code>.server.Comment comment = 2;</code>
   */
  @java.lang.Override
  public com.halloapp.proto.server.Comment getComment() {
    if (contentCase_ == 2) {
       return (com.halloapp.proto.server.Comment) content_;
    }
    return com.halloapp.proto.server.Comment.getDefaultInstance();
  }
  /**
   * <code>.server.Comment comment = 2;</code>
   */
  private void setComment(com.halloapp.proto.server.Comment value) {
    value.getClass();
  content_ = value;
    contentCase_ = 2;
  }
  /**
   * <code>.server.Comment comment = 2;</code>
   */
  private void mergeComment(com.halloapp.proto.server.Comment value) {
    value.getClass();
  if (contentCase_ == 2 &&
        content_ != com.halloapp.proto.server.Comment.getDefaultInstance()) {
      content_ = com.halloapp.proto.server.Comment.newBuilder((com.halloapp.proto.server.Comment) content_)
          .mergeFrom(value).buildPartial();
    } else {
      content_ = value;
    }
    contentCase_ = 2;
  }
  /**
   * <code>.server.Comment comment = 2;</code>
   */
  private void clearComment() {
    if (contentCase_ == 2) {
      contentCase_ = 0;
      content_ = null;
    }
  }

  public static final int GROUP_ID_FIELD_NUMBER = 3;
  private java.lang.String groupId_;
  /**
   * <code>string group_id = 3;</code>
   * @return The groupId.
   */
  @java.lang.Override
  public java.lang.String getGroupId() {
    return groupId_;
  }
  /**
   * <code>string group_id = 3;</code>
   * @return The bytes for groupId.
   */
  @java.lang.Override
  public com.google.protobuf.ByteString
      getGroupIdBytes() {
    return com.google.protobuf.ByteString.copyFromUtf8(groupId_);
  }
  /**
   * <code>string group_id = 3;</code>
   * @param value The groupId to set.
   */
  private void setGroupId(
      java.lang.String value) {
    value.getClass();
  
    groupId_ = value;
  }
  /**
   * <code>string group_id = 3;</code>
   */
  private void clearGroupId() {
    
    groupId_ = getDefaultInstance().getGroupId();
  }
  /**
   * <code>string group_id = 3;</code>
   * @param value The bytes for groupId to set.
   */
  private void setGroupIdBytes(
      com.google.protobuf.ByteString value) {
    checkByteStringIsUtf8(value);
    groupId_ = value.toStringUtf8();
    
  }

  public static final int EXPIRY_TIMESTAMP_FIELD_NUMBER = 4;
  private long expiryTimestamp_;
  /**
   * <pre>
   * Set only for post items. `-1` if item should never expire.
   * </pre>
   *
   * <code>int64 expiry_timestamp = 4;</code>
   * @return The expiryTimestamp.
   */
  @java.lang.Override
  public long getExpiryTimestamp() {
    return expiryTimestamp_;
  }
  /**
   * <pre>
   * Set only for post items. `-1` if item should never expire.
   * </pre>
   *
   * <code>int64 expiry_timestamp = 4;</code>
   * @param value The expiryTimestamp to set.
   */
  private void setExpiryTimestamp(long value) {
    
    expiryTimestamp_ = value;
  }
  /**
   * <pre>
   * Set only for post items. `-1` if item should never expire.
   * </pre>
   *
   * <code>int64 expiry_timestamp = 4;</code>
   */
  private void clearExpiryTimestamp() {
    
    expiryTimestamp_ = 0L;
  }

  public static com.halloapp.proto.web.FeedItem parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static com.halloapp.proto.web.FeedItem parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static com.halloapp.proto.web.FeedItem parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static com.halloapp.proto.web.FeedItem parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static com.halloapp.proto.web.FeedItem parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static com.halloapp.proto.web.FeedItem parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static com.halloapp.proto.web.FeedItem parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input);
  }
  public static com.halloapp.proto.web.FeedItem parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input, extensionRegistry);
  }
  public static com.halloapp.proto.web.FeedItem parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return parseDelimitedFrom(DEFAULT_INSTANCE, input);
  }
  public static com.halloapp.proto.web.FeedItem parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return parseDelimitedFrom(DEFAULT_INSTANCE, input, extensionRegistry);
  }
  public static com.halloapp.proto.web.FeedItem parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input);
  }
  public static com.halloapp.proto.web.FeedItem parseFrom(
      com.google.protobuf.CodedInputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input, extensionRegistry);
  }

  public static Builder newBuilder() {
    return (Builder) DEFAULT_INSTANCE.createBuilder();
  }
  public static Builder newBuilder(com.halloapp.proto.web.FeedItem prototype) {
    return (Builder) DEFAULT_INSTANCE.createBuilder(prototype);
  }

  /**
   * Protobuf type {@code web.FeedItem}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessageLite.Builder<
        com.halloapp.proto.web.FeedItem, Builder> implements
      // @@protoc_insertion_point(builder_implements:web.FeedItem)
      com.halloapp.proto.web.FeedItemOrBuilder {
    // Construct using com.halloapp.proto.web.FeedItem.newBuilder()
    private Builder() {
      super(DEFAULT_INSTANCE);
    }

    @java.lang.Override
    public ContentCase
        getContentCase() {
      return instance.getContentCase();
    }

    public Builder clearContent() {
      copyOnWrite();
      instance.clearContent();
      return this;
    }


    /**
     * <code>.server.Post post = 1;</code>
     */
    @java.lang.Override
    public boolean hasPost() {
      return instance.hasPost();
    }
    /**
     * <code>.server.Post post = 1;</code>
     */
    @java.lang.Override
    public com.halloapp.proto.server.Post getPost() {
      return instance.getPost();
    }
    /**
     * <code>.server.Post post = 1;</code>
     */
    public Builder setPost(com.halloapp.proto.server.Post value) {
      copyOnWrite();
      instance.setPost(value);
      return this;
    }
    /**
     * <code>.server.Post post = 1;</code>
     */
    public Builder setPost(
        com.halloapp.proto.server.Post.Builder builderForValue) {
      copyOnWrite();
      instance.setPost(builderForValue.build());
      return this;
    }
    /**
     * <code>.server.Post post = 1;</code>
     */
    public Builder mergePost(com.halloapp.proto.server.Post value) {
      copyOnWrite();
      instance.mergePost(value);
      return this;
    }
    /**
     * <code>.server.Post post = 1;</code>
     */
    public Builder clearPost() {
      copyOnWrite();
      instance.clearPost();
      return this;
    }

    /**
     * <code>.server.Comment comment = 2;</code>
     */
    @java.lang.Override
    public boolean hasComment() {
      return instance.hasComment();
    }
    /**
     * <code>.server.Comment comment = 2;</code>
     */
    @java.lang.Override
    public com.halloapp.proto.server.Comment getComment() {
      return instance.getComment();
    }
    /**
     * <code>.server.Comment comment = 2;</code>
     */
    public Builder setComment(com.halloapp.proto.server.Comment value) {
      copyOnWrite();
      instance.setComment(value);
      return this;
    }
    /**
     * <code>.server.Comment comment = 2;</code>
     */
    public Builder setComment(
        com.halloapp.proto.server.Comment.Builder builderForValue) {
      copyOnWrite();
      instance.setComment(builderForValue.build());
      return this;
    }
    /**
     * <code>.server.Comment comment = 2;</code>
     */
    public Builder mergeComment(com.halloapp.proto.server.Comment value) {
      copyOnWrite();
      instance.mergeComment(value);
      return this;
    }
    /**
     * <code>.server.Comment comment = 2;</code>
     */
    public Builder clearComment() {
      copyOnWrite();
      instance.clearComment();
      return this;
    }

    /**
     * <code>string group_id = 3;</code>
     * @return The groupId.
     */
    @java.lang.Override
    public java.lang.String getGroupId() {
      return instance.getGroupId();
    }
    /**
     * <code>string group_id = 3;</code>
     * @return The bytes for groupId.
     */
    @java.lang.Override
    public com.google.protobuf.ByteString
        getGroupIdBytes() {
      return instance.getGroupIdBytes();
    }
    /**
     * <code>string group_id = 3;</code>
     * @param value The groupId to set.
     * @return This builder for chaining.
     */
    public Builder setGroupId(
        java.lang.String value) {
      copyOnWrite();
      instance.setGroupId(value);
      return this;
    }
    /**
     * <code>string group_id = 3;</code>
     * @return This builder for chaining.
     */
    public Builder clearGroupId() {
      copyOnWrite();
      instance.clearGroupId();
      return this;
    }
    /**
     * <code>string group_id = 3;</code>
     * @param value The bytes for groupId to set.
     * @return This builder for chaining.
     */
    public Builder setGroupIdBytes(
        com.google.protobuf.ByteString value) {
      copyOnWrite();
      instance.setGroupIdBytes(value);
      return this;
    }

    /**
     * <pre>
     * Set only for post items. `-1` if item should never expire.
     * </pre>
     *
     * <code>int64 expiry_timestamp = 4;</code>
     * @return The expiryTimestamp.
     */
    @java.lang.Override
    public long getExpiryTimestamp() {
      return instance.getExpiryTimestamp();
    }
    /**
     * <pre>
     * Set only for post items. `-1` if item should never expire.
     * </pre>
     *
     * <code>int64 expiry_timestamp = 4;</code>
     * @param value The expiryTimestamp to set.
     * @return This builder for chaining.
     */
    public Builder setExpiryTimestamp(long value) {
      copyOnWrite();
      instance.setExpiryTimestamp(value);
      return this;
    }
    /**
     * <pre>
     * Set only for post items. `-1` if item should never expire.
     * </pre>
     *
     * <code>int64 expiry_timestamp = 4;</code>
     * @return This builder for chaining.
     */
    public Builder clearExpiryTimestamp() {
      copyOnWrite();
      instance.clearExpiryTimestamp();
      return this;
    }

    // @@protoc_insertion_point(builder_scope:web.FeedItem)
  }
  @java.lang.Override
  @java.lang.SuppressWarnings({"unchecked", "fallthrough"})
  protected final java.lang.Object dynamicMethod(
      com.google.protobuf.GeneratedMessageLite.MethodToInvoke method,
      java.lang.Object arg0, java.lang.Object arg1) {
    switch (method) {
      case NEW_MUTABLE_INSTANCE: {
        return new com.halloapp.proto.web.FeedItem();
      }
      case NEW_BUILDER: {
        return new Builder();
      }
      case BUILD_MESSAGE_INFO: {
          java.lang.Object[] objects = new java.lang.Object[] {
            "content_",
            "contentCase_",
            com.halloapp.proto.server.Post.class,
            com.halloapp.proto.server.Comment.class,
            "groupId_",
            "expiryTimestamp_",
          };
          java.lang.String info =
              "\u0000\u0004\u0001\u0000\u0001\u0004\u0004\u0000\u0000\u0000\u0001<\u0000\u0002<" +
              "\u0000\u0003\u0208\u0004\u0002";
          return newMessageInfo(DEFAULT_INSTANCE, info, objects);
      }
      // fall through
      case GET_DEFAULT_INSTANCE: {
        return DEFAULT_INSTANCE;
      }
      case GET_PARSER: {
        com.google.protobuf.Parser<com.halloapp.proto.web.FeedItem> parser = PARSER;
        if (parser == null) {
          synchronized (com.halloapp.proto.web.FeedItem.class) {
            parser = PARSER;
            if (parser == null) {
              parser =
                  new DefaultInstanceBasedParser<com.halloapp.proto.web.FeedItem>(
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


  // @@protoc_insertion_point(class_scope:web.FeedItem)
  private static final com.halloapp.proto.web.FeedItem DEFAULT_INSTANCE;
  static {
    FeedItem defaultInstance = new FeedItem();
    // New instances are implicitly immutable so no need to make
    // immutable.
    DEFAULT_INSTANCE = defaultInstance;
    com.google.protobuf.GeneratedMessageLite.registerDefaultInstance(
      FeedItem.class, defaultInstance);
  }

  public static com.halloapp.proto.web.FeedItem getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static volatile com.google.protobuf.Parser<FeedItem> PARSER;

  public static com.google.protobuf.Parser<FeedItem> parser() {
    return DEFAULT_INSTANCE.getParserForType();
  }
}
