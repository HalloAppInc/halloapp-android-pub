// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: server.proto

package com.halloapp.proto.server;

/**
 * Protobuf type {@code server.ContentMissing}
 */
public  final class ContentMissing extends
    com.google.protobuf.GeneratedMessageLite<
        ContentMissing, ContentMissing.Builder> implements
    // @@protoc_insertion_point(message_implements:server.ContentMissing)
    ContentMissingOrBuilder {
  private ContentMissing() {
    contentId_ = "";
    senderClientVersion_ = "";
  }
  /**
   * Protobuf enum {@code server.ContentMissing.ContentType}
   */
  public enum ContentType
      implements com.google.protobuf.Internal.EnumLite {
    /**
     * <code>UNKNOWN = 0;</code>
     */
    UNKNOWN(0),
    /**
     * <code>CHAT = 1;</code>
     */
    CHAT(1),
    /**
     * <pre>
     * For calls - WebRtcOffer and WebRtcAnswer packets include encrypted payloads.
     * Clients will respond back with content-missing and end-call packet if the call is not active.
     * </pre>
     *
     * <code>CALL = 2;</code>
     */
    CALL(2),
    /**
     * <code>GROUP_FEED_POST = 3;</code>
     */
    GROUP_FEED_POST(3),
    /**
     * <code>GROUP_FEED_COMMENT = 4;</code>
     */
    GROUP_FEED_COMMENT(4),
    /**
     * <code>HOME_FEED_POST = 5;</code>
     */
    HOME_FEED_POST(5),
    /**
     * <code>HOME_FEED_COMMENT = 6;</code>
     */
    HOME_FEED_COMMENT(6),
    /**
     * <code>HISTORY_RESEND = 7;</code>
     */
    HISTORY_RESEND(7),
    /**
     * <code>GROUP_HISTORY = 8;</code>
     */
    GROUP_HISTORY(8),
    /**
     * <code>CHAT_REACTION = 9;</code>
     */
    CHAT_REACTION(9),
    /**
     * <code>GROUP_COMMENT_REACTION = 10;</code>
     */
    GROUP_COMMENT_REACTION(10),
    /**
     * <code>GROUP_POST_REACTION = 11;</code>
     */
    GROUP_POST_REACTION(11),
    /**
     * <code>HOME_COMMENT_REACTION = 12;</code>
     */
    HOME_COMMENT_REACTION(12),
    /**
     * <code>HOME_POST_REACTION = 13;</code>
     */
    HOME_POST_REACTION(13),
    UNRECOGNIZED(-1),
    ;

    /**
     * <code>UNKNOWN = 0;</code>
     */
    public static final int UNKNOWN_VALUE = 0;
    /**
     * <code>CHAT = 1;</code>
     */
    public static final int CHAT_VALUE = 1;
    /**
     * <pre>
     * For calls - WebRtcOffer and WebRtcAnswer packets include encrypted payloads.
     * Clients will respond back with content-missing and end-call packet if the call is not active.
     * </pre>
     *
     * <code>CALL = 2;</code>
     */
    public static final int CALL_VALUE = 2;
    /**
     * <code>GROUP_FEED_POST = 3;</code>
     */
    public static final int GROUP_FEED_POST_VALUE = 3;
    /**
     * <code>GROUP_FEED_COMMENT = 4;</code>
     */
    public static final int GROUP_FEED_COMMENT_VALUE = 4;
    /**
     * <code>HOME_FEED_POST = 5;</code>
     */
    public static final int HOME_FEED_POST_VALUE = 5;
    /**
     * <code>HOME_FEED_COMMENT = 6;</code>
     */
    public static final int HOME_FEED_COMMENT_VALUE = 6;
    /**
     * <code>HISTORY_RESEND = 7;</code>
     */
    public static final int HISTORY_RESEND_VALUE = 7;
    /**
     * <code>GROUP_HISTORY = 8;</code>
     */
    public static final int GROUP_HISTORY_VALUE = 8;
    /**
     * <code>CHAT_REACTION = 9;</code>
     */
    public static final int CHAT_REACTION_VALUE = 9;
    /**
     * <code>GROUP_COMMENT_REACTION = 10;</code>
     */
    public static final int GROUP_COMMENT_REACTION_VALUE = 10;
    /**
     * <code>GROUP_POST_REACTION = 11;</code>
     */
    public static final int GROUP_POST_REACTION_VALUE = 11;
    /**
     * <code>HOME_COMMENT_REACTION = 12;</code>
     */
    public static final int HOME_COMMENT_REACTION_VALUE = 12;
    /**
     * <code>HOME_POST_REACTION = 13;</code>
     */
    public static final int HOME_POST_REACTION_VALUE = 13;


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
    public static ContentType valueOf(int value) {
      return forNumber(value);
    }

    public static ContentType forNumber(int value) {
      switch (value) {
        case 0: return UNKNOWN;
        case 1: return CHAT;
        case 2: return CALL;
        case 3: return GROUP_FEED_POST;
        case 4: return GROUP_FEED_COMMENT;
        case 5: return HOME_FEED_POST;
        case 6: return HOME_FEED_COMMENT;
        case 7: return HISTORY_RESEND;
        case 8: return GROUP_HISTORY;
        case 9: return CHAT_REACTION;
        case 10: return GROUP_COMMENT_REACTION;
        case 11: return GROUP_POST_REACTION;
        case 12: return HOME_COMMENT_REACTION;
        case 13: return HOME_POST_REACTION;
        default: return null;
      }
    }

    public static com.google.protobuf.Internal.EnumLiteMap<ContentType>
        internalGetValueMap() {
      return internalValueMap;
    }
    private static final com.google.protobuf.Internal.EnumLiteMap<
        ContentType> internalValueMap =
          new com.google.protobuf.Internal.EnumLiteMap<ContentType>() {
            @java.lang.Override
            public ContentType findValueByNumber(int number) {
              return ContentType.forNumber(number);
            }
          };

    public static com.google.protobuf.Internal.EnumVerifier 
        internalGetVerifier() {
      return ContentTypeVerifier.INSTANCE;
    }

    private static final class ContentTypeVerifier implements 
         com.google.protobuf.Internal.EnumVerifier { 
            static final com.google.protobuf.Internal.EnumVerifier           INSTANCE = new ContentTypeVerifier();
            @java.lang.Override
            public boolean isInRange(int number) {
              return ContentType.forNumber(number) != null;
            }
          };

    private final int value;

    private ContentType(int value) {
      this.value = value;
    }

    // @@protoc_insertion_point(enum_scope:server.ContentMissing.ContentType)
  }

  public static final int CONTENT_ID_FIELD_NUMBER = 1;
  private java.lang.String contentId_;
  /**
   * <code>string content_id = 1;</code>
   * @return The contentId.
   */
  @java.lang.Override
  public java.lang.String getContentId() {
    return contentId_;
  }
  /**
   * <code>string content_id = 1;</code>
   * @return The bytes for contentId.
   */
  @java.lang.Override
  public com.google.protobuf.ByteString
      getContentIdBytes() {
    return com.google.protobuf.ByteString.copyFromUtf8(contentId_);
  }
  /**
   * <code>string content_id = 1;</code>
   * @param value The contentId to set.
   */
  private void setContentId(
      java.lang.String value) {
    value.getClass();
  
    contentId_ = value;
  }
  /**
   * <code>string content_id = 1;</code>
   */
  private void clearContentId() {
    
    contentId_ = getDefaultInstance().getContentId();
  }
  /**
   * <code>string content_id = 1;</code>
   * @param value The bytes for contentId to set.
   */
  private void setContentIdBytes(
      com.google.protobuf.ByteString value) {
    checkByteStringIsUtf8(value);
    contentId_ = value.toStringUtf8();
    
  }

  public static final int CONTENT_TYPE_FIELD_NUMBER = 2;
  private int contentType_;
  /**
   * <code>.server.ContentMissing.ContentType content_type = 2;</code>
   * @return The enum numeric value on the wire for contentType.
   */
  @java.lang.Override
  public int getContentTypeValue() {
    return contentType_;
  }
  /**
   * <code>.server.ContentMissing.ContentType content_type = 2;</code>
   * @return The contentType.
   */
  @java.lang.Override
  public com.halloapp.proto.server.ContentMissing.ContentType getContentType() {
    com.halloapp.proto.server.ContentMissing.ContentType result = com.halloapp.proto.server.ContentMissing.ContentType.forNumber(contentType_);
    return result == null ? com.halloapp.proto.server.ContentMissing.ContentType.UNRECOGNIZED : result;
  }
  /**
   * <code>.server.ContentMissing.ContentType content_type = 2;</code>
   * @param value The enum numeric value on the wire for contentType to set.
   */
  private void setContentTypeValue(int value) {
      contentType_ = value;
  }
  /**
   * <code>.server.ContentMissing.ContentType content_type = 2;</code>
   * @param value The contentType to set.
   */
  private void setContentType(com.halloapp.proto.server.ContentMissing.ContentType value) {
    contentType_ = value.getNumber();
    
  }
  /**
   * <code>.server.ContentMissing.ContentType content_type = 2;</code>
   */
  private void clearContentType() {
    
    contentType_ = 0;
  }

  public static final int SENDER_CLIENT_VERSION_FIELD_NUMBER = 3;
  private java.lang.String senderClientVersion_;
  /**
   * <pre>
   * ex: "HalloApp/Android0.127"
   * </pre>
   *
   * <code>string sender_client_version = 3;</code>
   * @return The senderClientVersion.
   */
  @java.lang.Override
  public java.lang.String getSenderClientVersion() {
    return senderClientVersion_;
  }
  /**
   * <pre>
   * ex: "HalloApp/Android0.127"
   * </pre>
   *
   * <code>string sender_client_version = 3;</code>
   * @return The bytes for senderClientVersion.
   */
  @java.lang.Override
  public com.google.protobuf.ByteString
      getSenderClientVersionBytes() {
    return com.google.protobuf.ByteString.copyFromUtf8(senderClientVersion_);
  }
  /**
   * <pre>
   * ex: "HalloApp/Android0.127"
   * </pre>
   *
   * <code>string sender_client_version = 3;</code>
   * @param value The senderClientVersion to set.
   */
  private void setSenderClientVersion(
      java.lang.String value) {
    value.getClass();
  
    senderClientVersion_ = value;
  }
  /**
   * <pre>
   * ex: "HalloApp/Android0.127"
   * </pre>
   *
   * <code>string sender_client_version = 3;</code>
   */
  private void clearSenderClientVersion() {
    
    senderClientVersion_ = getDefaultInstance().getSenderClientVersion();
  }
  /**
   * <pre>
   * ex: "HalloApp/Android0.127"
   * </pre>
   *
   * <code>string sender_client_version = 3;</code>
   * @param value The bytes for senderClientVersion to set.
   */
  private void setSenderClientVersionBytes(
      com.google.protobuf.ByteString value) {
    checkByteStringIsUtf8(value);
    senderClientVersion_ = value.toStringUtf8();
    
  }

  public static com.halloapp.proto.server.ContentMissing parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static com.halloapp.proto.server.ContentMissing parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static com.halloapp.proto.server.ContentMissing parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static com.halloapp.proto.server.ContentMissing parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static com.halloapp.proto.server.ContentMissing parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static com.halloapp.proto.server.ContentMissing parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static com.halloapp.proto.server.ContentMissing parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input);
  }
  public static com.halloapp.proto.server.ContentMissing parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input, extensionRegistry);
  }
  public static com.halloapp.proto.server.ContentMissing parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return parseDelimitedFrom(DEFAULT_INSTANCE, input);
  }
  public static com.halloapp.proto.server.ContentMissing parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return parseDelimitedFrom(DEFAULT_INSTANCE, input, extensionRegistry);
  }
  public static com.halloapp.proto.server.ContentMissing parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input);
  }
  public static com.halloapp.proto.server.ContentMissing parseFrom(
      com.google.protobuf.CodedInputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input, extensionRegistry);
  }

  public static Builder newBuilder() {
    return (Builder) DEFAULT_INSTANCE.createBuilder();
  }
  public static Builder newBuilder(com.halloapp.proto.server.ContentMissing prototype) {
    return (Builder) DEFAULT_INSTANCE.createBuilder(prototype);
  }

  /**
   * Protobuf type {@code server.ContentMissing}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessageLite.Builder<
        com.halloapp.proto.server.ContentMissing, Builder> implements
      // @@protoc_insertion_point(builder_implements:server.ContentMissing)
      com.halloapp.proto.server.ContentMissingOrBuilder {
    // Construct using com.halloapp.proto.server.ContentMissing.newBuilder()
    private Builder() {
      super(DEFAULT_INSTANCE);
    }


    /**
     * <code>string content_id = 1;</code>
     * @return The contentId.
     */
    @java.lang.Override
    public java.lang.String getContentId() {
      return instance.getContentId();
    }
    /**
     * <code>string content_id = 1;</code>
     * @return The bytes for contentId.
     */
    @java.lang.Override
    public com.google.protobuf.ByteString
        getContentIdBytes() {
      return instance.getContentIdBytes();
    }
    /**
     * <code>string content_id = 1;</code>
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
     * <code>string content_id = 1;</code>
     * @return This builder for chaining.
     */
    public Builder clearContentId() {
      copyOnWrite();
      instance.clearContentId();
      return this;
    }
    /**
     * <code>string content_id = 1;</code>
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
     * <code>.server.ContentMissing.ContentType content_type = 2;</code>
     * @return The enum numeric value on the wire for contentType.
     */
    @java.lang.Override
    public int getContentTypeValue() {
      return instance.getContentTypeValue();
    }
    /**
     * <code>.server.ContentMissing.ContentType content_type = 2;</code>
     * @param value The contentType to set.
     * @return This builder for chaining.
     */
    public Builder setContentTypeValue(int value) {
      copyOnWrite();
      instance.setContentTypeValue(value);
      return this;
    }
    /**
     * <code>.server.ContentMissing.ContentType content_type = 2;</code>
     * @return The contentType.
     */
    @java.lang.Override
    public com.halloapp.proto.server.ContentMissing.ContentType getContentType() {
      return instance.getContentType();
    }
    /**
     * <code>.server.ContentMissing.ContentType content_type = 2;</code>
     * @param value The enum numeric value on the wire for contentType to set.
     * @return This builder for chaining.
     */
    public Builder setContentType(com.halloapp.proto.server.ContentMissing.ContentType value) {
      copyOnWrite();
      instance.setContentType(value);
      return this;
    }
    /**
     * <code>.server.ContentMissing.ContentType content_type = 2;</code>
     * @return This builder for chaining.
     */
    public Builder clearContentType() {
      copyOnWrite();
      instance.clearContentType();
      return this;
    }

    /**
     * <pre>
     * ex: "HalloApp/Android0.127"
     * </pre>
     *
     * <code>string sender_client_version = 3;</code>
     * @return The senderClientVersion.
     */
    @java.lang.Override
    public java.lang.String getSenderClientVersion() {
      return instance.getSenderClientVersion();
    }
    /**
     * <pre>
     * ex: "HalloApp/Android0.127"
     * </pre>
     *
     * <code>string sender_client_version = 3;</code>
     * @return The bytes for senderClientVersion.
     */
    @java.lang.Override
    public com.google.protobuf.ByteString
        getSenderClientVersionBytes() {
      return instance.getSenderClientVersionBytes();
    }
    /**
     * <pre>
     * ex: "HalloApp/Android0.127"
     * </pre>
     *
     * <code>string sender_client_version = 3;</code>
     * @param value The senderClientVersion to set.
     * @return This builder for chaining.
     */
    public Builder setSenderClientVersion(
        java.lang.String value) {
      copyOnWrite();
      instance.setSenderClientVersion(value);
      return this;
    }
    /**
     * <pre>
     * ex: "HalloApp/Android0.127"
     * </pre>
     *
     * <code>string sender_client_version = 3;</code>
     * @return This builder for chaining.
     */
    public Builder clearSenderClientVersion() {
      copyOnWrite();
      instance.clearSenderClientVersion();
      return this;
    }
    /**
     * <pre>
     * ex: "HalloApp/Android0.127"
     * </pre>
     *
     * <code>string sender_client_version = 3;</code>
     * @param value The bytes for senderClientVersion to set.
     * @return This builder for chaining.
     */
    public Builder setSenderClientVersionBytes(
        com.google.protobuf.ByteString value) {
      copyOnWrite();
      instance.setSenderClientVersionBytes(value);
      return this;
    }

    // @@protoc_insertion_point(builder_scope:server.ContentMissing)
  }
  @java.lang.Override
  @java.lang.SuppressWarnings({"unchecked", "fallthrough"})
  protected final java.lang.Object dynamicMethod(
      com.google.protobuf.GeneratedMessageLite.MethodToInvoke method,
      java.lang.Object arg0, java.lang.Object arg1) {
    switch (method) {
      case NEW_MUTABLE_INSTANCE: {
        return new com.halloapp.proto.server.ContentMissing();
      }
      case NEW_BUILDER: {
        return new Builder();
      }
      case BUILD_MESSAGE_INFO: {
          java.lang.Object[] objects = new java.lang.Object[] {
            "contentId_",
            "contentType_",
            "senderClientVersion_",
          };
          java.lang.String info =
              "\u0000\u0003\u0000\u0000\u0001\u0003\u0003\u0000\u0000\u0000\u0001\u0208\u0002\f" +
              "\u0003\u0208";
          return newMessageInfo(DEFAULT_INSTANCE, info, objects);
      }
      // fall through
      case GET_DEFAULT_INSTANCE: {
        return DEFAULT_INSTANCE;
      }
      case GET_PARSER: {
        com.google.protobuf.Parser<com.halloapp.proto.server.ContentMissing> parser = PARSER;
        if (parser == null) {
          synchronized (com.halloapp.proto.server.ContentMissing.class) {
            parser = PARSER;
            if (parser == null) {
              parser =
                  new DefaultInstanceBasedParser<com.halloapp.proto.server.ContentMissing>(
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


  // @@protoc_insertion_point(class_scope:server.ContentMissing)
  private static final com.halloapp.proto.server.ContentMissing DEFAULT_INSTANCE;
  static {
    ContentMissing defaultInstance = new ContentMissing();
    // New instances are implicitly immutable so no need to make
    // immutable.
    DEFAULT_INSTANCE = defaultInstance;
    com.google.protobuf.GeneratedMessageLite.registerDefaultInstance(
      ContentMissing.class, defaultInstance);
  }

  public static com.halloapp.proto.server.ContentMissing getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static volatile com.google.protobuf.Parser<ContentMissing> PARSER;

  public static com.google.protobuf.Parser<ContentMissing> parser() {
    return DEFAULT_INSTANCE.getParserForType();
  }
}

