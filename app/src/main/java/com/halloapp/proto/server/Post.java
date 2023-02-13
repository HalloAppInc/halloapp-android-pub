// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: server.proto

package com.halloapp.proto.server;

/**
 * Protobuf type {@code server.Post}
 */
public  final class Post extends
    com.google.protobuf.GeneratedMessageLite<
        Post, Post.Builder> implements
    // @@protoc_insertion_point(message_implements:server.Post)
    PostOrBuilder {
  private Post() {
    id_ = "";
    payload_ = com.google.protobuf.ByteString.EMPTY;
    publisherName_ = "";
    encPayload_ = com.google.protobuf.ByteString.EMPTY;
    psaTag_ = "";
  }
  /**
   * Protobuf enum {@code server.Post.Tag}
   */
  public enum Tag
      implements com.google.protobuf.Internal.EnumLite {
    /**
     * <code>EMPTY = 0;</code>
     */
    EMPTY(0),
    /**
     * <code>MOMENT = 1;</code>
     */
    MOMENT(1),
    /**
     * <code>PUBLIC_MOMENT = 2;</code>
     */
    PUBLIC_MOMENT(2),
    /**
     * <code>PUBLIC_POST = 3;</code>
     */
    PUBLIC_POST(3),
    UNRECOGNIZED(-1),
    ;

    /**
     * <code>EMPTY = 0;</code>
     */
    public static final int EMPTY_VALUE = 0;
    /**
     * <code>MOMENT = 1;</code>
     */
    public static final int MOMENT_VALUE = 1;
    /**
     * <code>PUBLIC_MOMENT = 2;</code>
     */
    public static final int PUBLIC_MOMENT_VALUE = 2;
    /**
     * <code>PUBLIC_POST = 3;</code>
     */
    public static final int PUBLIC_POST_VALUE = 3;


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
    public static Tag valueOf(int value) {
      return forNumber(value);
    }

    public static Tag forNumber(int value) {
      switch (value) {
        case 0: return EMPTY;
        case 1: return MOMENT;
        case 2: return PUBLIC_MOMENT;
        case 3: return PUBLIC_POST;
        default: return null;
      }
    }

    public static com.google.protobuf.Internal.EnumLiteMap<Tag>
        internalGetValueMap() {
      return internalValueMap;
    }
    private static final com.google.protobuf.Internal.EnumLiteMap<
        Tag> internalValueMap =
          new com.google.protobuf.Internal.EnumLiteMap<Tag>() {
            @java.lang.Override
            public Tag findValueByNumber(int number) {
              return Tag.forNumber(number);
            }
          };

    public static com.google.protobuf.Internal.EnumVerifier 
        internalGetVerifier() {
      return TagVerifier.INSTANCE;
    }

    private static final class TagVerifier implements 
         com.google.protobuf.Internal.EnumVerifier { 
            static final com.google.protobuf.Internal.EnumVerifier           INSTANCE = new TagVerifier();
            @java.lang.Override
            public boolean isInRange(int number) {
              return Tag.forNumber(number) != null;
            }
          };

    private final int value;

    private Tag(int value) {
      this.value = value;
    }

    // @@protoc_insertion_point(enum_scope:server.Post.Tag)
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

  public static final int PUBLISHER_UID_FIELD_NUMBER = 2;
  private long publisherUid_;
  /**
   * <code>int64 publisher_uid = 2;</code>
   * @return The publisherUid.
   */
  @java.lang.Override
  public long getPublisherUid() {
    return publisherUid_;
  }
  /**
   * <code>int64 publisher_uid = 2;</code>
   * @param value The publisherUid to set.
   */
  private void setPublisherUid(long value) {
    
    publisherUid_ = value;
  }
  /**
   * <code>int64 publisher_uid = 2;</code>
   */
  private void clearPublisherUid() {
    
    publisherUid_ = 0L;
  }

  public static final int PAYLOAD_FIELD_NUMBER = 3;
  private com.google.protobuf.ByteString payload_;
  /**
   * <code>bytes payload = 3;</code>
   * @return The payload.
   */
  @java.lang.Override
  public com.google.protobuf.ByteString getPayload() {
    return payload_;
  }
  /**
   * <code>bytes payload = 3;</code>
   * @param value The payload to set.
   */
  private void setPayload(com.google.protobuf.ByteString value) {
    value.getClass();
  
    payload_ = value;
  }
  /**
   * <code>bytes payload = 3;</code>
   */
  private void clearPayload() {
    
    payload_ = getDefaultInstance().getPayload();
  }

  public static final int AUDIENCE_FIELD_NUMBER = 4;
  private com.halloapp.proto.server.Audience audience_;
  /**
   * <code>.server.Audience audience = 4;</code>
   */
  @java.lang.Override
  public boolean hasAudience() {
    return audience_ != null;
  }
  /**
   * <code>.server.Audience audience = 4;</code>
   */
  @java.lang.Override
  public com.halloapp.proto.server.Audience getAudience() {
    return audience_ == null ? com.halloapp.proto.server.Audience.getDefaultInstance() : audience_;
  }
  /**
   * <code>.server.Audience audience = 4;</code>
   */
  private void setAudience(com.halloapp.proto.server.Audience value) {
    value.getClass();
  audience_ = value;
    
    }
  /**
   * <code>.server.Audience audience = 4;</code>
   */
  @java.lang.SuppressWarnings({"ReferenceEquality"})
  private void mergeAudience(com.halloapp.proto.server.Audience value) {
    value.getClass();
  if (audience_ != null &&
        audience_ != com.halloapp.proto.server.Audience.getDefaultInstance()) {
      audience_ =
        com.halloapp.proto.server.Audience.newBuilder(audience_).mergeFrom(value).buildPartial();
    } else {
      audience_ = value;
    }
    
  }
  /**
   * <code>.server.Audience audience = 4;</code>
   */
  private void clearAudience() {  audience_ = null;
    
  }

  public static final int TIMESTAMP_FIELD_NUMBER = 5;
  private long timestamp_;
  /**
   * <code>int64 timestamp = 5;</code>
   * @return The timestamp.
   */
  @java.lang.Override
  public long getTimestamp() {
    return timestamp_;
  }
  /**
   * <code>int64 timestamp = 5;</code>
   * @param value The timestamp to set.
   */
  private void setTimestamp(long value) {
    
    timestamp_ = value;
  }
  /**
   * <code>int64 timestamp = 5;</code>
   */
  private void clearTimestamp() {
    
    timestamp_ = 0L;
  }

  public static final int PUBLISHER_NAME_FIELD_NUMBER = 6;
  private java.lang.String publisherName_;
  /**
   * <code>string publisher_name = 6;</code>
   * @return The publisherName.
   */
  @java.lang.Override
  public java.lang.String getPublisherName() {
    return publisherName_;
  }
  /**
   * <code>string publisher_name = 6;</code>
   * @return The bytes for publisherName.
   */
  @java.lang.Override
  public com.google.protobuf.ByteString
      getPublisherNameBytes() {
    return com.google.protobuf.ByteString.copyFromUtf8(publisherName_);
  }
  /**
   * <code>string publisher_name = 6;</code>
   * @param value The publisherName to set.
   */
  private void setPublisherName(
      java.lang.String value) {
    value.getClass();
  
    publisherName_ = value;
  }
  /**
   * <code>string publisher_name = 6;</code>
   */
  private void clearPublisherName() {
    
    publisherName_ = getDefaultInstance().getPublisherName();
  }
  /**
   * <code>string publisher_name = 6;</code>
   * @param value The bytes for publisherName to set.
   */
  private void setPublisherNameBytes(
      com.google.protobuf.ByteString value) {
    checkByteStringIsUtf8(value);
    publisherName_ = value.toStringUtf8();
    
  }

  public static final int ENC_PAYLOAD_FIELD_NUMBER = 7;
  private com.google.protobuf.ByteString encPayload_;
  /**
   * <pre>
   * Serialized EncryptedPayload (from client.proto).
   * </pre>
   *
   * <code>bytes enc_payload = 7;</code>
   * @return The encPayload.
   */
  @java.lang.Override
  public com.google.protobuf.ByteString getEncPayload() {
    return encPayload_;
  }
  /**
   * <pre>
   * Serialized EncryptedPayload (from client.proto).
   * </pre>
   *
   * <code>bytes enc_payload = 7;</code>
   * @param value The encPayload to set.
   */
  private void setEncPayload(com.google.protobuf.ByteString value) {
    value.getClass();
  
    encPayload_ = value;
  }
  /**
   * <pre>
   * Serialized EncryptedPayload (from client.proto).
   * </pre>
   *
   * <code>bytes enc_payload = 7;</code>
   */
  private void clearEncPayload() {
    
    encPayload_ = getDefaultInstance().getEncPayload();
  }

  public static final int MEDIA_COUNTERS_FIELD_NUMBER = 8;
  private com.halloapp.proto.server.MediaCounters mediaCounters_;
  /**
   * <code>.server.MediaCounters media_counters = 8;</code>
   */
  @java.lang.Override
  public boolean hasMediaCounters() {
    return mediaCounters_ != null;
  }
  /**
   * <code>.server.MediaCounters media_counters = 8;</code>
   */
  @java.lang.Override
  public com.halloapp.proto.server.MediaCounters getMediaCounters() {
    return mediaCounters_ == null ? com.halloapp.proto.server.MediaCounters.getDefaultInstance() : mediaCounters_;
  }
  /**
   * <code>.server.MediaCounters media_counters = 8;</code>
   */
  private void setMediaCounters(com.halloapp.proto.server.MediaCounters value) {
    value.getClass();
  mediaCounters_ = value;
    
    }
  /**
   * <code>.server.MediaCounters media_counters = 8;</code>
   */
  @java.lang.SuppressWarnings({"ReferenceEquality"})
  private void mergeMediaCounters(com.halloapp.proto.server.MediaCounters value) {
    value.getClass();
  if (mediaCounters_ != null &&
        mediaCounters_ != com.halloapp.proto.server.MediaCounters.getDefaultInstance()) {
      mediaCounters_ =
        com.halloapp.proto.server.MediaCounters.newBuilder(mediaCounters_).mergeFrom(value).buildPartial();
    } else {
      mediaCounters_ = value;
    }
    
  }
  /**
   * <code>.server.MediaCounters media_counters = 8;</code>
   */
  private void clearMediaCounters() {  mediaCounters_ = null;
    
  }

  public static final int TAG_FIELD_NUMBER = 9;
  private int tag_;
  /**
   * <code>.server.Post.Tag tag = 9;</code>
   * @return The enum numeric value on the wire for tag.
   */
  @java.lang.Override
  public int getTagValue() {
    return tag_;
  }
  /**
   * <code>.server.Post.Tag tag = 9;</code>
   * @return The tag.
   */
  @java.lang.Override
  public com.halloapp.proto.server.Post.Tag getTag() {
    com.halloapp.proto.server.Post.Tag result = com.halloapp.proto.server.Post.Tag.forNumber(tag_);
    return result == null ? com.halloapp.proto.server.Post.Tag.UNRECOGNIZED : result;
  }
  /**
   * <code>.server.Post.Tag tag = 9;</code>
   * @param value The enum numeric value on the wire for tag to set.
   */
  private void setTagValue(int value) {
      tag_ = value;
  }
  /**
   * <code>.server.Post.Tag tag = 9;</code>
   * @param value The tag to set.
   */
  private void setTag(com.halloapp.proto.server.Post.Tag value) {
    tag_ = value.getNumber();
    
  }
  /**
   * <code>.server.Post.Tag tag = 9;</code>
   */
  private void clearTag() {
    
    tag_ = 0;
  }

  public static final int PSA_TAG_FIELD_NUMBER = 10;
  private java.lang.String psaTag_;
  /**
   * <code>string psa_tag = 10;</code>
   * @return The psaTag.
   */
  @java.lang.Override
  public java.lang.String getPsaTag() {
    return psaTag_;
  }
  /**
   * <code>string psa_tag = 10;</code>
   * @return The bytes for psaTag.
   */
  @java.lang.Override
  public com.google.protobuf.ByteString
      getPsaTagBytes() {
    return com.google.protobuf.ByteString.copyFromUtf8(psaTag_);
  }
  /**
   * <code>string psa_tag = 10;</code>
   * @param value The psaTag to set.
   */
  private void setPsaTag(
      java.lang.String value) {
    value.getClass();
  
    psaTag_ = value;
  }
  /**
   * <code>string psa_tag = 10;</code>
   */
  private void clearPsaTag() {
    
    psaTag_ = getDefaultInstance().getPsaTag();
  }
  /**
   * <code>string psa_tag = 10;</code>
   * @param value The bytes for psaTag to set.
   */
  private void setPsaTagBytes(
      com.google.protobuf.ByteString value) {
    checkByteStringIsUtf8(value);
    psaTag_ = value.toStringUtf8();
    
  }

  public static final int MOMENT_UNLOCK_UID_FIELD_NUMBER = 11;
  private long momentUnlockUid_;
  /**
   * <code>int64 moment_unlock_uid = 11;</code>
   * @return The momentUnlockUid.
   */
  @java.lang.Override
  public long getMomentUnlockUid() {
    return momentUnlockUid_;
  }
  /**
   * <code>int64 moment_unlock_uid = 11;</code>
   * @param value The momentUnlockUid to set.
   */
  private void setMomentUnlockUid(long value) {
    
    momentUnlockUid_ = value;
  }
  /**
   * <code>int64 moment_unlock_uid = 11;</code>
   */
  private void clearMomentUnlockUid() {
    
    momentUnlockUid_ = 0L;
  }

  public static final int SHOW_POST_SHARE_SCREEN_FIELD_NUMBER = 12;
  private boolean showPostShareScreen_;
  /**
   * <pre>
   * If set to true, the client will try and show ui design to
   * share the recently composed post externally.
   * </pre>
   *
   * <code>bool show_post_share_screen = 12;</code>
   * @return The showPostShareScreen.
   */
  @java.lang.Override
  public boolean getShowPostShareScreen() {
    return showPostShareScreen_;
  }
  /**
   * <pre>
   * If set to true, the client will try and show ui design to
   * share the recently composed post externally.
   * </pre>
   *
   * <code>bool show_post_share_screen = 12;</code>
   * @param value The showPostShareScreen to set.
   */
  private void setShowPostShareScreen(boolean value) {
    
    showPostShareScreen_ = value;
  }
  /**
   * <pre>
   * If set to true, the client will try and show ui design to
   * share the recently composed post externally.
   * </pre>
   *
   * <code>bool show_post_share_screen = 12;</code>
   */
  private void clearShowPostShareScreen() {
    
    showPostShareScreen_ = false;
  }

  public static final int MOMENT_INFO_FIELD_NUMBER = 13;
  private com.halloapp.proto.server.MomentInfo momentInfo_;
  /**
   * <pre>
   * Must be set only for moments.
   * </pre>
   *
   * <code>.server.MomentInfo moment_info = 13;</code>
   */
  @java.lang.Override
  public boolean hasMomentInfo() {
    return momentInfo_ != null;
  }
  /**
   * <pre>
   * Must be set only for moments.
   * </pre>
   *
   * <code>.server.MomentInfo moment_info = 13;</code>
   */
  @java.lang.Override
  public com.halloapp.proto.server.MomentInfo getMomentInfo() {
    return momentInfo_ == null ? com.halloapp.proto.server.MomentInfo.getDefaultInstance() : momentInfo_;
  }
  /**
   * <pre>
   * Must be set only for moments.
   * </pre>
   *
   * <code>.server.MomentInfo moment_info = 13;</code>
   */
  private void setMomentInfo(com.halloapp.proto.server.MomentInfo value) {
    value.getClass();
  momentInfo_ = value;
    
    }
  /**
   * <pre>
   * Must be set only for moments.
   * </pre>
   *
   * <code>.server.MomentInfo moment_info = 13;</code>
   */
  @java.lang.SuppressWarnings({"ReferenceEquality"})
  private void mergeMomentInfo(com.halloapp.proto.server.MomentInfo value) {
    value.getClass();
  if (momentInfo_ != null &&
        momentInfo_ != com.halloapp.proto.server.MomentInfo.getDefaultInstance()) {
      momentInfo_ =
        com.halloapp.proto.server.MomentInfo.newBuilder(momentInfo_).mergeFrom(value).buildPartial();
    } else {
      momentInfo_ = value;
    }
    
  }
  /**
   * <pre>
   * Must be set only for moments.
   * </pre>
   *
   * <code>.server.MomentInfo moment_info = 13;</code>
   */
  private void clearMomentInfo() {  momentInfo_ = null;
    
  }

  public static final int IS_EXPIRED_FIELD_NUMBER = 14;
  private boolean isExpired_;
  /**
   * <pre>
   * Set by the server.
   * </pre>
   *
   * <code>bool is_expired = 14;</code>
   * @return The isExpired.
   */
  @java.lang.Override
  public boolean getIsExpired() {
    return isExpired_;
  }
  /**
   * <pre>
   * Set by the server.
   * </pre>
   *
   * <code>bool is_expired = 14;</code>
   * @param value The isExpired to set.
   */
  private void setIsExpired(boolean value) {
    
    isExpired_ = value;
  }
  /**
   * <pre>
   * Set by the server.
   * </pre>
   *
   * <code>bool is_expired = 14;</code>
   */
  private void clearIsExpired() {
    
    isExpired_ = false;
  }

  public static com.halloapp.proto.server.Post parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static com.halloapp.proto.server.Post parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static com.halloapp.proto.server.Post parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static com.halloapp.proto.server.Post parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static com.halloapp.proto.server.Post parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static com.halloapp.proto.server.Post parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static com.halloapp.proto.server.Post parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input);
  }
  public static com.halloapp.proto.server.Post parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input, extensionRegistry);
  }
  public static com.halloapp.proto.server.Post parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return parseDelimitedFrom(DEFAULT_INSTANCE, input);
  }
  public static com.halloapp.proto.server.Post parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return parseDelimitedFrom(DEFAULT_INSTANCE, input, extensionRegistry);
  }
  public static com.halloapp.proto.server.Post parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input);
  }
  public static com.halloapp.proto.server.Post parseFrom(
      com.google.protobuf.CodedInputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input, extensionRegistry);
  }

  public static Builder newBuilder() {
    return (Builder) DEFAULT_INSTANCE.createBuilder();
  }
  public static Builder newBuilder(com.halloapp.proto.server.Post prototype) {
    return (Builder) DEFAULT_INSTANCE.createBuilder(prototype);
  }

  /**
   * Protobuf type {@code server.Post}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessageLite.Builder<
        com.halloapp.proto.server.Post, Builder> implements
      // @@protoc_insertion_point(builder_implements:server.Post)
      com.halloapp.proto.server.PostOrBuilder {
    // Construct using com.halloapp.proto.server.Post.newBuilder()
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
     * <code>int64 publisher_uid = 2;</code>
     * @return The publisherUid.
     */
    @java.lang.Override
    public long getPublisherUid() {
      return instance.getPublisherUid();
    }
    /**
     * <code>int64 publisher_uid = 2;</code>
     * @param value The publisherUid to set.
     * @return This builder for chaining.
     */
    public Builder setPublisherUid(long value) {
      copyOnWrite();
      instance.setPublisherUid(value);
      return this;
    }
    /**
     * <code>int64 publisher_uid = 2;</code>
     * @return This builder for chaining.
     */
    public Builder clearPublisherUid() {
      copyOnWrite();
      instance.clearPublisherUid();
      return this;
    }

    /**
     * <code>bytes payload = 3;</code>
     * @return The payload.
     */
    @java.lang.Override
    public com.google.protobuf.ByteString getPayload() {
      return instance.getPayload();
    }
    /**
     * <code>bytes payload = 3;</code>
     * @param value The payload to set.
     * @return This builder for chaining.
     */
    public Builder setPayload(com.google.protobuf.ByteString value) {
      copyOnWrite();
      instance.setPayload(value);
      return this;
    }
    /**
     * <code>bytes payload = 3;</code>
     * @return This builder for chaining.
     */
    public Builder clearPayload() {
      copyOnWrite();
      instance.clearPayload();
      return this;
    }

    /**
     * <code>.server.Audience audience = 4;</code>
     */
    @java.lang.Override
    public boolean hasAudience() {
      return instance.hasAudience();
    }
    /**
     * <code>.server.Audience audience = 4;</code>
     */
    @java.lang.Override
    public com.halloapp.proto.server.Audience getAudience() {
      return instance.getAudience();
    }
    /**
     * <code>.server.Audience audience = 4;</code>
     */
    public Builder setAudience(com.halloapp.proto.server.Audience value) {
      copyOnWrite();
      instance.setAudience(value);
      return this;
      }
    /**
     * <code>.server.Audience audience = 4;</code>
     */
    public Builder setAudience(
        com.halloapp.proto.server.Audience.Builder builderForValue) {
      copyOnWrite();
      instance.setAudience(builderForValue.build());
      return this;
    }
    /**
     * <code>.server.Audience audience = 4;</code>
     */
    public Builder mergeAudience(com.halloapp.proto.server.Audience value) {
      copyOnWrite();
      instance.mergeAudience(value);
      return this;
    }
    /**
     * <code>.server.Audience audience = 4;</code>
     */
    public Builder clearAudience() {  copyOnWrite();
      instance.clearAudience();
      return this;
    }

    /**
     * <code>int64 timestamp = 5;</code>
     * @return The timestamp.
     */
    @java.lang.Override
    public long getTimestamp() {
      return instance.getTimestamp();
    }
    /**
     * <code>int64 timestamp = 5;</code>
     * @param value The timestamp to set.
     * @return This builder for chaining.
     */
    public Builder setTimestamp(long value) {
      copyOnWrite();
      instance.setTimestamp(value);
      return this;
    }
    /**
     * <code>int64 timestamp = 5;</code>
     * @return This builder for chaining.
     */
    public Builder clearTimestamp() {
      copyOnWrite();
      instance.clearTimestamp();
      return this;
    }

    /**
     * <code>string publisher_name = 6;</code>
     * @return The publisherName.
     */
    @java.lang.Override
    public java.lang.String getPublisherName() {
      return instance.getPublisherName();
    }
    /**
     * <code>string publisher_name = 6;</code>
     * @return The bytes for publisherName.
     */
    @java.lang.Override
    public com.google.protobuf.ByteString
        getPublisherNameBytes() {
      return instance.getPublisherNameBytes();
    }
    /**
     * <code>string publisher_name = 6;</code>
     * @param value The publisherName to set.
     * @return This builder for chaining.
     */
    public Builder setPublisherName(
        java.lang.String value) {
      copyOnWrite();
      instance.setPublisherName(value);
      return this;
    }
    /**
     * <code>string publisher_name = 6;</code>
     * @return This builder for chaining.
     */
    public Builder clearPublisherName() {
      copyOnWrite();
      instance.clearPublisherName();
      return this;
    }
    /**
     * <code>string publisher_name = 6;</code>
     * @param value The bytes for publisherName to set.
     * @return This builder for chaining.
     */
    public Builder setPublisherNameBytes(
        com.google.protobuf.ByteString value) {
      copyOnWrite();
      instance.setPublisherNameBytes(value);
      return this;
    }

    /**
     * <pre>
     * Serialized EncryptedPayload (from client.proto).
     * </pre>
     *
     * <code>bytes enc_payload = 7;</code>
     * @return The encPayload.
     */
    @java.lang.Override
    public com.google.protobuf.ByteString getEncPayload() {
      return instance.getEncPayload();
    }
    /**
     * <pre>
     * Serialized EncryptedPayload (from client.proto).
     * </pre>
     *
     * <code>bytes enc_payload = 7;</code>
     * @param value The encPayload to set.
     * @return This builder for chaining.
     */
    public Builder setEncPayload(com.google.protobuf.ByteString value) {
      copyOnWrite();
      instance.setEncPayload(value);
      return this;
    }
    /**
     * <pre>
     * Serialized EncryptedPayload (from client.proto).
     * </pre>
     *
     * <code>bytes enc_payload = 7;</code>
     * @return This builder for chaining.
     */
    public Builder clearEncPayload() {
      copyOnWrite();
      instance.clearEncPayload();
      return this;
    }

    /**
     * <code>.server.MediaCounters media_counters = 8;</code>
     */
    @java.lang.Override
    public boolean hasMediaCounters() {
      return instance.hasMediaCounters();
    }
    /**
     * <code>.server.MediaCounters media_counters = 8;</code>
     */
    @java.lang.Override
    public com.halloapp.proto.server.MediaCounters getMediaCounters() {
      return instance.getMediaCounters();
    }
    /**
     * <code>.server.MediaCounters media_counters = 8;</code>
     */
    public Builder setMediaCounters(com.halloapp.proto.server.MediaCounters value) {
      copyOnWrite();
      instance.setMediaCounters(value);
      return this;
      }
    /**
     * <code>.server.MediaCounters media_counters = 8;</code>
     */
    public Builder setMediaCounters(
        com.halloapp.proto.server.MediaCounters.Builder builderForValue) {
      copyOnWrite();
      instance.setMediaCounters(builderForValue.build());
      return this;
    }
    /**
     * <code>.server.MediaCounters media_counters = 8;</code>
     */
    public Builder mergeMediaCounters(com.halloapp.proto.server.MediaCounters value) {
      copyOnWrite();
      instance.mergeMediaCounters(value);
      return this;
    }
    /**
     * <code>.server.MediaCounters media_counters = 8;</code>
     */
    public Builder clearMediaCounters() {  copyOnWrite();
      instance.clearMediaCounters();
      return this;
    }

    /**
     * <code>.server.Post.Tag tag = 9;</code>
     * @return The enum numeric value on the wire for tag.
     */
    @java.lang.Override
    public int getTagValue() {
      return instance.getTagValue();
    }
    /**
     * <code>.server.Post.Tag tag = 9;</code>
     * @param value The tag to set.
     * @return This builder for chaining.
     */
    public Builder setTagValue(int value) {
      copyOnWrite();
      instance.setTagValue(value);
      return this;
    }
    /**
     * <code>.server.Post.Tag tag = 9;</code>
     * @return The tag.
     */
    @java.lang.Override
    public com.halloapp.proto.server.Post.Tag getTag() {
      return instance.getTag();
    }
    /**
     * <code>.server.Post.Tag tag = 9;</code>
     * @param value The enum numeric value on the wire for tag to set.
     * @return This builder for chaining.
     */
    public Builder setTag(com.halloapp.proto.server.Post.Tag value) {
      copyOnWrite();
      instance.setTag(value);
      return this;
    }
    /**
     * <code>.server.Post.Tag tag = 9;</code>
     * @return This builder for chaining.
     */
    public Builder clearTag() {
      copyOnWrite();
      instance.clearTag();
      return this;
    }

    /**
     * <code>string psa_tag = 10;</code>
     * @return The psaTag.
     */
    @java.lang.Override
    public java.lang.String getPsaTag() {
      return instance.getPsaTag();
    }
    /**
     * <code>string psa_tag = 10;</code>
     * @return The bytes for psaTag.
     */
    @java.lang.Override
    public com.google.protobuf.ByteString
        getPsaTagBytes() {
      return instance.getPsaTagBytes();
    }
    /**
     * <code>string psa_tag = 10;</code>
     * @param value The psaTag to set.
     * @return This builder for chaining.
     */
    public Builder setPsaTag(
        java.lang.String value) {
      copyOnWrite();
      instance.setPsaTag(value);
      return this;
    }
    /**
     * <code>string psa_tag = 10;</code>
     * @return This builder for chaining.
     */
    public Builder clearPsaTag() {
      copyOnWrite();
      instance.clearPsaTag();
      return this;
    }
    /**
     * <code>string psa_tag = 10;</code>
     * @param value The bytes for psaTag to set.
     * @return This builder for chaining.
     */
    public Builder setPsaTagBytes(
        com.google.protobuf.ByteString value) {
      copyOnWrite();
      instance.setPsaTagBytes(value);
      return this;
    }

    /**
     * <code>int64 moment_unlock_uid = 11;</code>
     * @return The momentUnlockUid.
     */
    @java.lang.Override
    public long getMomentUnlockUid() {
      return instance.getMomentUnlockUid();
    }
    /**
     * <code>int64 moment_unlock_uid = 11;</code>
     * @param value The momentUnlockUid to set.
     * @return This builder for chaining.
     */
    public Builder setMomentUnlockUid(long value) {
      copyOnWrite();
      instance.setMomentUnlockUid(value);
      return this;
    }
    /**
     * <code>int64 moment_unlock_uid = 11;</code>
     * @return This builder for chaining.
     */
    public Builder clearMomentUnlockUid() {
      copyOnWrite();
      instance.clearMomentUnlockUid();
      return this;
    }

    /**
     * <pre>
     * If set to true, the client will try and show ui design to
     * share the recently composed post externally.
     * </pre>
     *
     * <code>bool show_post_share_screen = 12;</code>
     * @return The showPostShareScreen.
     */
    @java.lang.Override
    public boolean getShowPostShareScreen() {
      return instance.getShowPostShareScreen();
    }
    /**
     * <pre>
     * If set to true, the client will try and show ui design to
     * share the recently composed post externally.
     * </pre>
     *
     * <code>bool show_post_share_screen = 12;</code>
     * @param value The showPostShareScreen to set.
     * @return This builder for chaining.
     */
    public Builder setShowPostShareScreen(boolean value) {
      copyOnWrite();
      instance.setShowPostShareScreen(value);
      return this;
    }
    /**
     * <pre>
     * If set to true, the client will try and show ui design to
     * share the recently composed post externally.
     * </pre>
     *
     * <code>bool show_post_share_screen = 12;</code>
     * @return This builder for chaining.
     */
    public Builder clearShowPostShareScreen() {
      copyOnWrite();
      instance.clearShowPostShareScreen();
      return this;
    }

    /**
     * <pre>
     * Must be set only for moments.
     * </pre>
     *
     * <code>.server.MomentInfo moment_info = 13;</code>
     */
    @java.lang.Override
    public boolean hasMomentInfo() {
      return instance.hasMomentInfo();
    }
    /**
     * <pre>
     * Must be set only for moments.
     * </pre>
     *
     * <code>.server.MomentInfo moment_info = 13;</code>
     */
    @java.lang.Override
    public com.halloapp.proto.server.MomentInfo getMomentInfo() {
      return instance.getMomentInfo();
    }
    /**
     * <pre>
     * Must be set only for moments.
     * </pre>
     *
     * <code>.server.MomentInfo moment_info = 13;</code>
     */
    public Builder setMomentInfo(com.halloapp.proto.server.MomentInfo value) {
      copyOnWrite();
      instance.setMomentInfo(value);
      return this;
      }
    /**
     * <pre>
     * Must be set only for moments.
     * </pre>
     *
     * <code>.server.MomentInfo moment_info = 13;</code>
     */
    public Builder setMomentInfo(
        com.halloapp.proto.server.MomentInfo.Builder builderForValue) {
      copyOnWrite();
      instance.setMomentInfo(builderForValue.build());
      return this;
    }
    /**
     * <pre>
     * Must be set only for moments.
     * </pre>
     *
     * <code>.server.MomentInfo moment_info = 13;</code>
     */
    public Builder mergeMomentInfo(com.halloapp.proto.server.MomentInfo value) {
      copyOnWrite();
      instance.mergeMomentInfo(value);
      return this;
    }
    /**
     * <pre>
     * Must be set only for moments.
     * </pre>
     *
     * <code>.server.MomentInfo moment_info = 13;</code>
     */
    public Builder clearMomentInfo() {  copyOnWrite();
      instance.clearMomentInfo();
      return this;
    }

    /**
     * <pre>
     * Set by the server.
     * </pre>
     *
     * <code>bool is_expired = 14;</code>
     * @return The isExpired.
     */
    @java.lang.Override
    public boolean getIsExpired() {
      return instance.getIsExpired();
    }
    /**
     * <pre>
     * Set by the server.
     * </pre>
     *
     * <code>bool is_expired = 14;</code>
     * @param value The isExpired to set.
     * @return This builder for chaining.
     */
    public Builder setIsExpired(boolean value) {
      copyOnWrite();
      instance.setIsExpired(value);
      return this;
    }
    /**
     * <pre>
     * Set by the server.
     * </pre>
     *
     * <code>bool is_expired = 14;</code>
     * @return This builder for chaining.
     */
    public Builder clearIsExpired() {
      copyOnWrite();
      instance.clearIsExpired();
      return this;
    }

    // @@protoc_insertion_point(builder_scope:server.Post)
  }
  @java.lang.Override
  @java.lang.SuppressWarnings({"unchecked", "fallthrough"})
  protected final java.lang.Object dynamicMethod(
      com.google.protobuf.GeneratedMessageLite.MethodToInvoke method,
      java.lang.Object arg0, java.lang.Object arg1) {
    switch (method) {
      case NEW_MUTABLE_INSTANCE: {
        return new com.halloapp.proto.server.Post();
      }
      case NEW_BUILDER: {
        return new Builder();
      }
      case BUILD_MESSAGE_INFO: {
          java.lang.Object[] objects = new java.lang.Object[] {
            "id_",
            "publisherUid_",
            "payload_",
            "audience_",
            "timestamp_",
            "publisherName_",
            "encPayload_",
            "mediaCounters_",
            "tag_",
            "psaTag_",
            "momentUnlockUid_",
            "showPostShareScreen_",
            "momentInfo_",
            "isExpired_",
          };
          java.lang.String info =
              "\u0000\u000e\u0000\u0000\u0001\u000e\u000e\u0000\u0000\u0000\u0001\u0208\u0002\u0002" +
              "\u0003\n\u0004\t\u0005\u0002\u0006\u0208\u0007\n\b\t\t\f\n\u0208\u000b\u0002\f\u0007" +
              "\r\t\u000e\u0007";
          return newMessageInfo(DEFAULT_INSTANCE, info, objects);
      }
      // fall through
      case GET_DEFAULT_INSTANCE: {
        return DEFAULT_INSTANCE;
      }
      case GET_PARSER: {
        com.google.protobuf.Parser<com.halloapp.proto.server.Post> parser = PARSER;
        if (parser == null) {
          synchronized (com.halloapp.proto.server.Post.class) {
            parser = PARSER;
            if (parser == null) {
              parser =
                  new DefaultInstanceBasedParser<com.halloapp.proto.server.Post>(
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


  // @@protoc_insertion_point(class_scope:server.Post)
  private static final com.halloapp.proto.server.Post DEFAULT_INSTANCE;
  static {
    Post defaultInstance = new Post();
    // New instances are implicitly immutable so no need to make
    // immutable.
    DEFAULT_INSTANCE = defaultInstance;
    com.google.protobuf.GeneratedMessageLite.registerDefaultInstance(
      Post.class, defaultInstance);
  }

  public static com.halloapp.proto.server.Post getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static volatile com.google.protobuf.Parser<Post> PARSER;

  public static com.google.protobuf.Parser<Post> parser() {
    return DEFAULT_INSTANCE.getParserForType();
  }
}

