// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: log_events.proto

package com.halloapp.proto.log_events;

/**
 * Protobuf type {@code server.GroupDecryptionReport}
 */
public  final class GroupDecryptionReport extends
    com.google.protobuf.GeneratedMessageLite<
        GroupDecryptionReport, GroupDecryptionReport.Builder> implements
    // @@protoc_insertion_point(message_implements:server.GroupDecryptionReport)
    GroupDecryptionReportOrBuilder {
  private GroupDecryptionReport() {
    reason_ = "";
    contentId_ = "";
    gid_ = "";
    originalVersion_ = "";
  }
  /**
   * Protobuf enum {@code server.GroupDecryptionReport.Status}
   */
  public enum Status
      implements com.google.protobuf.Internal.EnumLite {
    /**
     * <code>UNKNOWN_STATUS = 0;</code>
     */
    UNKNOWN_STATUS(0),
    /**
     * <code>OK = 1;</code>
     */
    OK(1),
    /**
     * <code>FAIL = 2;</code>
     */
    FAIL(2),
    UNRECOGNIZED(-1),
    ;

    /**
     * <code>UNKNOWN_STATUS = 0;</code>
     */
    public static final int UNKNOWN_STATUS_VALUE = 0;
    /**
     * <code>OK = 1;</code>
     */
    public static final int OK_VALUE = 1;
    /**
     * <code>FAIL = 2;</code>
     */
    public static final int FAIL_VALUE = 2;


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
    public static Status valueOf(int value) {
      return forNumber(value);
    }

    public static Status forNumber(int value) {
      switch (value) {
        case 0: return UNKNOWN_STATUS;
        case 1: return OK;
        case 2: return FAIL;
        default: return null;
      }
    }

    public static com.google.protobuf.Internal.EnumLiteMap<Status>
        internalGetValueMap() {
      return internalValueMap;
    }
    private static final com.google.protobuf.Internal.EnumLiteMap<
        Status> internalValueMap =
          new com.google.protobuf.Internal.EnumLiteMap<Status>() {
            @java.lang.Override
            public Status findValueByNumber(int number) {
              return Status.forNumber(number);
            }
          };

    public static com.google.protobuf.Internal.EnumVerifier 
        internalGetVerifier() {
      return StatusVerifier.INSTANCE;
    }

    private static final class StatusVerifier implements 
         com.google.protobuf.Internal.EnumVerifier { 
            static final com.google.protobuf.Internal.EnumVerifier           INSTANCE = new StatusVerifier();
            @java.lang.Override
            public boolean isInRange(int number) {
              return Status.forNumber(number) != null;
            }
          };

    private final int value;

    private Status(int value) {
      this.value = value;
    }

    // @@protoc_insertion_point(enum_scope:server.GroupDecryptionReport.Status)
  }

  /**
   * Protobuf enum {@code server.GroupDecryptionReport.ItemType}
   */
  public enum ItemType
      implements com.google.protobuf.Internal.EnumLite {
    /**
     * <code>UNKNOWN_TYPE = 0;</code>
     */
    UNKNOWN_TYPE(0),
    /**
     * <code>POST = 1;</code>
     */
    POST(1),
    /**
     * <code>COMMENT = 2;</code>
     */
    COMMENT(2),
    UNRECOGNIZED(-1),
    ;

    /**
     * <code>UNKNOWN_TYPE = 0;</code>
     */
    public static final int UNKNOWN_TYPE_VALUE = 0;
    /**
     * <code>POST = 1;</code>
     */
    public static final int POST_VALUE = 1;
    /**
     * <code>COMMENT = 2;</code>
     */
    public static final int COMMENT_VALUE = 2;


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
    public static ItemType valueOf(int value) {
      return forNumber(value);
    }

    public static ItemType forNumber(int value) {
      switch (value) {
        case 0: return UNKNOWN_TYPE;
        case 1: return POST;
        case 2: return COMMENT;
        default: return null;
      }
    }

    public static com.google.protobuf.Internal.EnumLiteMap<ItemType>
        internalGetValueMap() {
      return internalValueMap;
    }
    private static final com.google.protobuf.Internal.EnumLiteMap<
        ItemType> internalValueMap =
          new com.google.protobuf.Internal.EnumLiteMap<ItemType>() {
            @java.lang.Override
            public ItemType findValueByNumber(int number) {
              return ItemType.forNumber(number);
            }
          };

    public static com.google.protobuf.Internal.EnumVerifier 
        internalGetVerifier() {
      return ItemTypeVerifier.INSTANCE;
    }

    private static final class ItemTypeVerifier implements 
         com.google.protobuf.Internal.EnumVerifier { 
            static final com.google.protobuf.Internal.EnumVerifier           INSTANCE = new ItemTypeVerifier();
            @java.lang.Override
            public boolean isInRange(int number) {
              return ItemType.forNumber(number) != null;
            }
          };

    private final int value;

    private ItemType(int value) {
      this.value = value;
    }

    // @@protoc_insertion_point(enum_scope:server.GroupDecryptionReport.ItemType)
  }

  public static final int RESULT_FIELD_NUMBER = 1;
  private int result_;
  /**
   * <code>.server.GroupDecryptionReport.Status result = 1;</code>
   * @return The enum numeric value on the wire for result.
   */
  @java.lang.Override
  public int getResultValue() {
    return result_;
  }
  /**
   * <code>.server.GroupDecryptionReport.Status result = 1;</code>
   * @return The result.
   */
  @java.lang.Override
  public com.halloapp.proto.log_events.GroupDecryptionReport.Status getResult() {
    com.halloapp.proto.log_events.GroupDecryptionReport.Status result = com.halloapp.proto.log_events.GroupDecryptionReport.Status.forNumber(result_);
    return result == null ? com.halloapp.proto.log_events.GroupDecryptionReport.Status.UNRECOGNIZED : result;
  }
  /**
   * <code>.server.GroupDecryptionReport.Status result = 1;</code>
   * @param value The enum numeric value on the wire for result to set.
   */
  private void setResultValue(int value) {
      result_ = value;
  }
  /**
   * <code>.server.GroupDecryptionReport.Status result = 1;</code>
   * @param value The result to set.
   */
  private void setResult(com.halloapp.proto.log_events.GroupDecryptionReport.Status value) {
    result_ = value.getNumber();
    
  }
  /**
   * <code>.server.GroupDecryptionReport.Status result = 1;</code>
   */
  private void clearResult() {
    
    result_ = 0;
  }

  public static final int REASON_FIELD_NUMBER = 2;
  private java.lang.String reason_;
  /**
   * <code>string reason = 2;</code>
   * @return The reason.
   */
  @java.lang.Override
  public java.lang.String getReason() {
    return reason_;
  }
  /**
   * <code>string reason = 2;</code>
   * @return The bytes for reason.
   */
  @java.lang.Override
  public com.google.protobuf.ByteString
      getReasonBytes() {
    return com.google.protobuf.ByteString.copyFromUtf8(reason_);
  }
  /**
   * <code>string reason = 2;</code>
   * @param value The reason to set.
   */
  private void setReason(
      java.lang.String value) {
    value.getClass();
  
    reason_ = value;
  }
  /**
   * <code>string reason = 2;</code>
   */
  private void clearReason() {
    
    reason_ = getDefaultInstance().getReason();
  }
  /**
   * <code>string reason = 2;</code>
   * @param value The bytes for reason to set.
   */
  private void setReasonBytes(
      com.google.protobuf.ByteString value) {
    checkByteStringIsUtf8(value);
    reason_ = value.toStringUtf8();
    
  }

  public static final int CONTENT_ID_FIELD_NUMBER = 3;
  private java.lang.String contentId_;
  /**
   * <code>string content_id = 3;</code>
   * @return The contentId.
   */
  @java.lang.Override
  public java.lang.String getContentId() {
    return contentId_;
  }
  /**
   * <code>string content_id = 3;</code>
   * @return The bytes for contentId.
   */
  @java.lang.Override
  public com.google.protobuf.ByteString
      getContentIdBytes() {
    return com.google.protobuf.ByteString.copyFromUtf8(contentId_);
  }
  /**
   * <code>string content_id = 3;</code>
   * @param value The contentId to set.
   */
  private void setContentId(
      java.lang.String value) {
    value.getClass();
  
    contentId_ = value;
  }
  /**
   * <code>string content_id = 3;</code>
   */
  private void clearContentId() {
    
    contentId_ = getDefaultInstance().getContentId();
  }
  /**
   * <code>string content_id = 3;</code>
   * @param value The bytes for contentId to set.
   */
  private void setContentIdBytes(
      com.google.protobuf.ByteString value) {
    checkByteStringIsUtf8(value);
    contentId_ = value.toStringUtf8();
    
  }

  public static final int GID_FIELD_NUMBER = 4;
  private java.lang.String gid_;
  /**
   * <code>string gid = 4;</code>
   * @return The gid.
   */
  @java.lang.Override
  public java.lang.String getGid() {
    return gid_;
  }
  /**
   * <code>string gid = 4;</code>
   * @return The bytes for gid.
   */
  @java.lang.Override
  public com.google.protobuf.ByteString
      getGidBytes() {
    return com.google.protobuf.ByteString.copyFromUtf8(gid_);
  }
  /**
   * <code>string gid = 4;</code>
   * @param value The gid to set.
   */
  private void setGid(
      java.lang.String value) {
    value.getClass();
  
    gid_ = value;
  }
  /**
   * <code>string gid = 4;</code>
   */
  private void clearGid() {
    
    gid_ = getDefaultInstance().getGid();
  }
  /**
   * <code>string gid = 4;</code>
   * @param value The bytes for gid to set.
   */
  private void setGidBytes(
      com.google.protobuf.ByteString value) {
    checkByteStringIsUtf8(value);
    gid_ = value.toStringUtf8();
    
  }

  public static final int ITEM_TYPE_FIELD_NUMBER = 5;
  private int itemType_;
  /**
   * <code>.server.GroupDecryptionReport.ItemType item_type = 5;</code>
   * @return The enum numeric value on the wire for itemType.
   */
  @java.lang.Override
  public int getItemTypeValue() {
    return itemType_;
  }
  /**
   * <code>.server.GroupDecryptionReport.ItemType item_type = 5;</code>
   * @return The itemType.
   */
  @java.lang.Override
  public com.halloapp.proto.log_events.GroupDecryptionReport.ItemType getItemType() {
    com.halloapp.proto.log_events.GroupDecryptionReport.ItemType result = com.halloapp.proto.log_events.GroupDecryptionReport.ItemType.forNumber(itemType_);
    return result == null ? com.halloapp.proto.log_events.GroupDecryptionReport.ItemType.UNRECOGNIZED : result;
  }
  /**
   * <code>.server.GroupDecryptionReport.ItemType item_type = 5;</code>
   * @param value The enum numeric value on the wire for itemType to set.
   */
  private void setItemTypeValue(int value) {
      itemType_ = value;
  }
  /**
   * <code>.server.GroupDecryptionReport.ItemType item_type = 5;</code>
   * @param value The itemType to set.
   */
  private void setItemType(com.halloapp.proto.log_events.GroupDecryptionReport.ItemType value) {
    itemType_ = value.getNumber();
    
  }
  /**
   * <code>.server.GroupDecryptionReport.ItemType item_type = 5;</code>
   */
  private void clearItemType() {
    
    itemType_ = 0;
  }

  public static final int ORIGINAL_VERSION_FIELD_NUMBER = 6;
  private java.lang.String originalVersion_;
  /**
   * <pre>
   * at time msg id was first encountered
   * </pre>
   *
   * <code>string original_version = 6;</code>
   * @return The originalVersion.
   */
  @java.lang.Override
  public java.lang.String getOriginalVersion() {
    return originalVersion_;
  }
  /**
   * <pre>
   * at time msg id was first encountered
   * </pre>
   *
   * <code>string original_version = 6;</code>
   * @return The bytes for originalVersion.
   */
  @java.lang.Override
  public com.google.protobuf.ByteString
      getOriginalVersionBytes() {
    return com.google.protobuf.ByteString.copyFromUtf8(originalVersion_);
  }
  /**
   * <pre>
   * at time msg id was first encountered
   * </pre>
   *
   * <code>string original_version = 6;</code>
   * @param value The originalVersion to set.
   */
  private void setOriginalVersion(
      java.lang.String value) {
    value.getClass();
  
    originalVersion_ = value;
  }
  /**
   * <pre>
   * at time msg id was first encountered
   * </pre>
   *
   * <code>string original_version = 6;</code>
   */
  private void clearOriginalVersion() {
    
    originalVersion_ = getDefaultInstance().getOriginalVersion();
  }
  /**
   * <pre>
   * at time msg id was first encountered
   * </pre>
   *
   * <code>string original_version = 6;</code>
   * @param value The bytes for originalVersion to set.
   */
  private void setOriginalVersionBytes(
      com.google.protobuf.ByteString value) {
    checkByteStringIsUtf8(value);
    originalVersion_ = value.toStringUtf8();
    
  }

  public static final int REREQUEST_COUNT_FIELD_NUMBER = 7;
  private int rerequestCount_;
  /**
   * <code>uint32 rerequest_count = 7;</code>
   * @return The rerequestCount.
   */
  @java.lang.Override
  public int getRerequestCount() {
    return rerequestCount_;
  }
  /**
   * <code>uint32 rerequest_count = 7;</code>
   * @param value The rerequestCount to set.
   */
  private void setRerequestCount(int value) {
    
    rerequestCount_ = value;
  }
  /**
   * <code>uint32 rerequest_count = 7;</code>
   */
  private void clearRerequestCount() {
    
    rerequestCount_ = 0;
  }

  public static final int TIME_TAKEN_S_FIELD_NUMBER = 8;
  private int timeTakenS_;
  /**
   * <code>uint32 time_taken_s = 8;</code>
   * @return The timeTakenS.
   */
  @java.lang.Override
  public int getTimeTakenS() {
    return timeTakenS_;
  }
  /**
   * <code>uint32 time_taken_s = 8;</code>
   * @param value The timeTakenS to set.
   */
  private void setTimeTakenS(int value) {
    
    timeTakenS_ = value;
  }
  /**
   * <code>uint32 time_taken_s = 8;</code>
   */
  private void clearTimeTakenS() {
    
    timeTakenS_ = 0;
  }

  public static com.halloapp.proto.log_events.GroupDecryptionReport parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static com.halloapp.proto.log_events.GroupDecryptionReport parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static com.halloapp.proto.log_events.GroupDecryptionReport parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static com.halloapp.proto.log_events.GroupDecryptionReport parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static com.halloapp.proto.log_events.GroupDecryptionReport parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static com.halloapp.proto.log_events.GroupDecryptionReport parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static com.halloapp.proto.log_events.GroupDecryptionReport parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input);
  }
  public static com.halloapp.proto.log_events.GroupDecryptionReport parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input, extensionRegistry);
  }
  public static com.halloapp.proto.log_events.GroupDecryptionReport parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return parseDelimitedFrom(DEFAULT_INSTANCE, input);
  }
  public static com.halloapp.proto.log_events.GroupDecryptionReport parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return parseDelimitedFrom(DEFAULT_INSTANCE, input, extensionRegistry);
  }
  public static com.halloapp.proto.log_events.GroupDecryptionReport parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input);
  }
  public static com.halloapp.proto.log_events.GroupDecryptionReport parseFrom(
      com.google.protobuf.CodedInputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input, extensionRegistry);
  }

  public static Builder newBuilder() {
    return (Builder) DEFAULT_INSTANCE.createBuilder();
  }
  public static Builder newBuilder(com.halloapp.proto.log_events.GroupDecryptionReport prototype) {
    return (Builder) DEFAULT_INSTANCE.createBuilder(prototype);
  }

  /**
   * Protobuf type {@code server.GroupDecryptionReport}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessageLite.Builder<
        com.halloapp.proto.log_events.GroupDecryptionReport, Builder> implements
      // @@protoc_insertion_point(builder_implements:server.GroupDecryptionReport)
      com.halloapp.proto.log_events.GroupDecryptionReportOrBuilder {
    // Construct using com.halloapp.proto.log_events.GroupDecryptionReport.newBuilder()
    private Builder() {
      super(DEFAULT_INSTANCE);
    }


    /**
     * <code>.server.GroupDecryptionReport.Status result = 1;</code>
     * @return The enum numeric value on the wire for result.
     */
    @java.lang.Override
    public int getResultValue() {
      return instance.getResultValue();
    }
    /**
     * <code>.server.GroupDecryptionReport.Status result = 1;</code>
     * @param value The result to set.
     * @return This builder for chaining.
     */
    public Builder setResultValue(int value) {
      copyOnWrite();
      instance.setResultValue(value);
      return this;
    }
    /**
     * <code>.server.GroupDecryptionReport.Status result = 1;</code>
     * @return The result.
     */
    @java.lang.Override
    public com.halloapp.proto.log_events.GroupDecryptionReport.Status getResult() {
      return instance.getResult();
    }
    /**
     * <code>.server.GroupDecryptionReport.Status result = 1;</code>
     * @param value The enum numeric value on the wire for result to set.
     * @return This builder for chaining.
     */
    public Builder setResult(com.halloapp.proto.log_events.GroupDecryptionReport.Status value) {
      copyOnWrite();
      instance.setResult(value);
      return this;
    }
    /**
     * <code>.server.GroupDecryptionReport.Status result = 1;</code>
     * @return This builder for chaining.
     */
    public Builder clearResult() {
      copyOnWrite();
      instance.clearResult();
      return this;
    }

    /**
     * <code>string reason = 2;</code>
     * @return The reason.
     */
    @java.lang.Override
    public java.lang.String getReason() {
      return instance.getReason();
    }
    /**
     * <code>string reason = 2;</code>
     * @return The bytes for reason.
     */
    @java.lang.Override
    public com.google.protobuf.ByteString
        getReasonBytes() {
      return instance.getReasonBytes();
    }
    /**
     * <code>string reason = 2;</code>
     * @param value The reason to set.
     * @return This builder for chaining.
     */
    public Builder setReason(
        java.lang.String value) {
      copyOnWrite();
      instance.setReason(value);
      return this;
    }
    /**
     * <code>string reason = 2;</code>
     * @return This builder for chaining.
     */
    public Builder clearReason() {
      copyOnWrite();
      instance.clearReason();
      return this;
    }
    /**
     * <code>string reason = 2;</code>
     * @param value The bytes for reason to set.
     * @return This builder for chaining.
     */
    public Builder setReasonBytes(
        com.google.protobuf.ByteString value) {
      copyOnWrite();
      instance.setReasonBytes(value);
      return this;
    }

    /**
     * <code>string content_id = 3;</code>
     * @return The contentId.
     */
    @java.lang.Override
    public java.lang.String getContentId() {
      return instance.getContentId();
    }
    /**
     * <code>string content_id = 3;</code>
     * @return The bytes for contentId.
     */
    @java.lang.Override
    public com.google.protobuf.ByteString
        getContentIdBytes() {
      return instance.getContentIdBytes();
    }
    /**
     * <code>string content_id = 3;</code>
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
     * <code>string content_id = 3;</code>
     * @return This builder for chaining.
     */
    public Builder clearContentId() {
      copyOnWrite();
      instance.clearContentId();
      return this;
    }
    /**
     * <code>string content_id = 3;</code>
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
     * <code>string gid = 4;</code>
     * @return The gid.
     */
    @java.lang.Override
    public java.lang.String getGid() {
      return instance.getGid();
    }
    /**
     * <code>string gid = 4;</code>
     * @return The bytes for gid.
     */
    @java.lang.Override
    public com.google.protobuf.ByteString
        getGidBytes() {
      return instance.getGidBytes();
    }
    /**
     * <code>string gid = 4;</code>
     * @param value The gid to set.
     * @return This builder for chaining.
     */
    public Builder setGid(
        java.lang.String value) {
      copyOnWrite();
      instance.setGid(value);
      return this;
    }
    /**
     * <code>string gid = 4;</code>
     * @return This builder for chaining.
     */
    public Builder clearGid() {
      copyOnWrite();
      instance.clearGid();
      return this;
    }
    /**
     * <code>string gid = 4;</code>
     * @param value The bytes for gid to set.
     * @return This builder for chaining.
     */
    public Builder setGidBytes(
        com.google.protobuf.ByteString value) {
      copyOnWrite();
      instance.setGidBytes(value);
      return this;
    }

    /**
     * <code>.server.GroupDecryptionReport.ItemType item_type = 5;</code>
     * @return The enum numeric value on the wire for itemType.
     */
    @java.lang.Override
    public int getItemTypeValue() {
      return instance.getItemTypeValue();
    }
    /**
     * <code>.server.GroupDecryptionReport.ItemType item_type = 5;</code>
     * @param value The itemType to set.
     * @return This builder for chaining.
     */
    public Builder setItemTypeValue(int value) {
      copyOnWrite();
      instance.setItemTypeValue(value);
      return this;
    }
    /**
     * <code>.server.GroupDecryptionReport.ItemType item_type = 5;</code>
     * @return The itemType.
     */
    @java.lang.Override
    public com.halloapp.proto.log_events.GroupDecryptionReport.ItemType getItemType() {
      return instance.getItemType();
    }
    /**
     * <code>.server.GroupDecryptionReport.ItemType item_type = 5;</code>
     * @param value The enum numeric value on the wire for itemType to set.
     * @return This builder for chaining.
     */
    public Builder setItemType(com.halloapp.proto.log_events.GroupDecryptionReport.ItemType value) {
      copyOnWrite();
      instance.setItemType(value);
      return this;
    }
    /**
     * <code>.server.GroupDecryptionReport.ItemType item_type = 5;</code>
     * @return This builder for chaining.
     */
    public Builder clearItemType() {
      copyOnWrite();
      instance.clearItemType();
      return this;
    }

    /**
     * <pre>
     * at time msg id was first encountered
     * </pre>
     *
     * <code>string original_version = 6;</code>
     * @return The originalVersion.
     */
    @java.lang.Override
    public java.lang.String getOriginalVersion() {
      return instance.getOriginalVersion();
    }
    /**
     * <pre>
     * at time msg id was first encountered
     * </pre>
     *
     * <code>string original_version = 6;</code>
     * @return The bytes for originalVersion.
     */
    @java.lang.Override
    public com.google.protobuf.ByteString
        getOriginalVersionBytes() {
      return instance.getOriginalVersionBytes();
    }
    /**
     * <pre>
     * at time msg id was first encountered
     * </pre>
     *
     * <code>string original_version = 6;</code>
     * @param value The originalVersion to set.
     * @return This builder for chaining.
     */
    public Builder setOriginalVersion(
        java.lang.String value) {
      copyOnWrite();
      instance.setOriginalVersion(value);
      return this;
    }
    /**
     * <pre>
     * at time msg id was first encountered
     * </pre>
     *
     * <code>string original_version = 6;</code>
     * @return This builder for chaining.
     */
    public Builder clearOriginalVersion() {
      copyOnWrite();
      instance.clearOriginalVersion();
      return this;
    }
    /**
     * <pre>
     * at time msg id was first encountered
     * </pre>
     *
     * <code>string original_version = 6;</code>
     * @param value The bytes for originalVersion to set.
     * @return This builder for chaining.
     */
    public Builder setOriginalVersionBytes(
        com.google.protobuf.ByteString value) {
      copyOnWrite();
      instance.setOriginalVersionBytes(value);
      return this;
    }

    /**
     * <code>uint32 rerequest_count = 7;</code>
     * @return The rerequestCount.
     */
    @java.lang.Override
    public int getRerequestCount() {
      return instance.getRerequestCount();
    }
    /**
     * <code>uint32 rerequest_count = 7;</code>
     * @param value The rerequestCount to set.
     * @return This builder for chaining.
     */
    public Builder setRerequestCount(int value) {
      copyOnWrite();
      instance.setRerequestCount(value);
      return this;
    }
    /**
     * <code>uint32 rerequest_count = 7;</code>
     * @return This builder for chaining.
     */
    public Builder clearRerequestCount() {
      copyOnWrite();
      instance.clearRerequestCount();
      return this;
    }

    /**
     * <code>uint32 time_taken_s = 8;</code>
     * @return The timeTakenS.
     */
    @java.lang.Override
    public int getTimeTakenS() {
      return instance.getTimeTakenS();
    }
    /**
     * <code>uint32 time_taken_s = 8;</code>
     * @param value The timeTakenS to set.
     * @return This builder for chaining.
     */
    public Builder setTimeTakenS(int value) {
      copyOnWrite();
      instance.setTimeTakenS(value);
      return this;
    }
    /**
     * <code>uint32 time_taken_s = 8;</code>
     * @return This builder for chaining.
     */
    public Builder clearTimeTakenS() {
      copyOnWrite();
      instance.clearTimeTakenS();
      return this;
    }

    // @@protoc_insertion_point(builder_scope:server.GroupDecryptionReport)
  }
  @java.lang.Override
  @java.lang.SuppressWarnings({"unchecked", "fallthrough"})
  protected final java.lang.Object dynamicMethod(
      com.google.protobuf.GeneratedMessageLite.MethodToInvoke method,
      java.lang.Object arg0, java.lang.Object arg1) {
    switch (method) {
      case NEW_MUTABLE_INSTANCE: {
        return new com.halloapp.proto.log_events.GroupDecryptionReport();
      }
      case NEW_BUILDER: {
        return new Builder();
      }
      case BUILD_MESSAGE_INFO: {
          java.lang.Object[] objects = new java.lang.Object[] {
            "result_",
            "reason_",
            "contentId_",
            "gid_",
            "itemType_",
            "originalVersion_",
            "rerequestCount_",
            "timeTakenS_",
          };
          java.lang.String info =
              "\u0000\b\u0000\u0000\u0001\b\b\u0000\u0000\u0000\u0001\f\u0002\u0208\u0003\u0208" +
              "\u0004\u0208\u0005\f\u0006\u0208\u0007\u000b\b\u000b";
          return newMessageInfo(DEFAULT_INSTANCE, info, objects);
      }
      // fall through
      case GET_DEFAULT_INSTANCE: {
        return DEFAULT_INSTANCE;
      }
      case GET_PARSER: {
        com.google.protobuf.Parser<com.halloapp.proto.log_events.GroupDecryptionReport> parser = PARSER;
        if (parser == null) {
          synchronized (com.halloapp.proto.log_events.GroupDecryptionReport.class) {
            parser = PARSER;
            if (parser == null) {
              parser =
                  new DefaultInstanceBasedParser<com.halloapp.proto.log_events.GroupDecryptionReport>(
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


  // @@protoc_insertion_point(class_scope:server.GroupDecryptionReport)
  private static final com.halloapp.proto.log_events.GroupDecryptionReport DEFAULT_INSTANCE;
  static {
    GroupDecryptionReport defaultInstance = new GroupDecryptionReport();
    // New instances are implicitly immutable so no need to make
    // immutable.
    DEFAULT_INSTANCE = defaultInstance;
    com.google.protobuf.GeneratedMessageLite.registerDefaultInstance(
      GroupDecryptionReport.class, defaultInstance);
  }

  public static com.halloapp.proto.log_events.GroupDecryptionReport getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static volatile com.google.protobuf.Parser<GroupDecryptionReport> PARSER;

  public static com.google.protobuf.Parser<GroupDecryptionReport> parser() {
    return DEFAULT_INSTANCE.getParserForType();
  }
}

