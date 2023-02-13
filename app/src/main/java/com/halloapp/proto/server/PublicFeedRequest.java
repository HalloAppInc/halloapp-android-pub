// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: server.proto

package com.halloapp.proto.server;

/**
 * Protobuf type {@code server.PublicFeedRequest}
 */
public  final class PublicFeedRequest extends
    com.google.protobuf.GeneratedMessageLite<
        PublicFeedRequest, PublicFeedRequest.Builder> implements
    // @@protoc_insertion_point(message_implements:server.PublicFeedRequest)
    PublicFeedRequestOrBuilder {
  private PublicFeedRequest() {
    cursor_ = "";
  }
  public static final int CURSOR_FIELD_NUMBER = 1;
  private java.lang.String cursor_;
  /**
   * <code>string cursor = 1;</code>
   * @return The cursor.
   */
  @java.lang.Override
  public java.lang.String getCursor() {
    return cursor_;
  }
  /**
   * <code>string cursor = 1;</code>
   * @return The bytes for cursor.
   */
  @java.lang.Override
  public com.google.protobuf.ByteString
      getCursorBytes() {
    return com.google.protobuf.ByteString.copyFromUtf8(cursor_);
  }
  /**
   * <code>string cursor = 1;</code>
   * @param value The cursor to set.
   */
  private void setCursor(
      java.lang.String value) {
    value.getClass();
  
    cursor_ = value;
  }
  /**
   * <code>string cursor = 1;</code>
   */
  private void clearCursor() {
    
    cursor_ = getDefaultInstance().getCursor();
  }
  /**
   * <code>string cursor = 1;</code>
   * @param value The bytes for cursor to set.
   */
  private void setCursorBytes(
      com.google.protobuf.ByteString value) {
    checkByteStringIsUtf8(value);
    cursor_ = value.toStringUtf8();
    
  }

  public static final int PUBLIC_FEED_CONTENT_TYPE_FIELD_NUMBER = 2;
  private int publicFeedContentType_;
  /**
   * <code>.server.PublicFeedContentType public_feed_content_type = 2;</code>
   * @return The enum numeric value on the wire for publicFeedContentType.
   */
  @java.lang.Override
  public int getPublicFeedContentTypeValue() {
    return publicFeedContentType_;
  }
  /**
   * <code>.server.PublicFeedContentType public_feed_content_type = 2;</code>
   * @return The publicFeedContentType.
   */
  @java.lang.Override
  public com.halloapp.proto.server.PublicFeedContentType getPublicFeedContentType() {
    com.halloapp.proto.server.PublicFeedContentType result = com.halloapp.proto.server.PublicFeedContentType.forNumber(publicFeedContentType_);
    return result == null ? com.halloapp.proto.server.PublicFeedContentType.UNRECOGNIZED : result;
  }
  /**
   * <code>.server.PublicFeedContentType public_feed_content_type = 2;</code>
   * @param value The enum numeric value on the wire for publicFeedContentType to set.
   */
  private void setPublicFeedContentTypeValue(int value) {
      publicFeedContentType_ = value;
  }
  /**
   * <code>.server.PublicFeedContentType public_feed_content_type = 2;</code>
   * @param value The publicFeedContentType to set.
   */
  private void setPublicFeedContentType(com.halloapp.proto.server.PublicFeedContentType value) {
    publicFeedContentType_ = value.getNumber();
    
  }
  /**
   * <code>.server.PublicFeedContentType public_feed_content_type = 2;</code>
   */
  private void clearPublicFeedContentType() {
    
    publicFeedContentType_ = 0;
  }

  public static final int GPS_LOCATION_FIELD_NUMBER = 3;
  private com.halloapp.proto.server.GpsLocation gpsLocation_;
  /**
   * <code>.server.GpsLocation gps_location = 3;</code>
   */
  @java.lang.Override
  public boolean hasGpsLocation() {
    return gpsLocation_ != null;
  }
  /**
   * <code>.server.GpsLocation gps_location = 3;</code>
   */
  @java.lang.Override
  public com.halloapp.proto.server.GpsLocation getGpsLocation() {
    return gpsLocation_ == null ? com.halloapp.proto.server.GpsLocation.getDefaultInstance() : gpsLocation_;
  }
  /**
   * <code>.server.GpsLocation gps_location = 3;</code>
   */
  private void setGpsLocation(com.halloapp.proto.server.GpsLocation value) {
    value.getClass();
  gpsLocation_ = value;
    
    }
  /**
   * <code>.server.GpsLocation gps_location = 3;</code>
   */
  @java.lang.SuppressWarnings({"ReferenceEquality"})
  private void mergeGpsLocation(com.halloapp.proto.server.GpsLocation value) {
    value.getClass();
  if (gpsLocation_ != null &&
        gpsLocation_ != com.halloapp.proto.server.GpsLocation.getDefaultInstance()) {
      gpsLocation_ =
        com.halloapp.proto.server.GpsLocation.newBuilder(gpsLocation_).mergeFrom(value).buildPartial();
    } else {
      gpsLocation_ = value;
    }
    
  }
  /**
   * <code>.server.GpsLocation gps_location = 3;</code>
   */
  private void clearGpsLocation() {  gpsLocation_ = null;
    
  }

  public static final int SHOW_DEV_CONTENT_FIELD_NUMBER = 4;
  private boolean showDevContent_;
  /**
   * <code>bool show_dev_content = 4;</code>
   * @return The showDevContent.
   */
  @java.lang.Override
  public boolean getShowDevContent() {
    return showDevContent_;
  }
  /**
   * <code>bool show_dev_content = 4;</code>
   * @param value The showDevContent to set.
   */
  private void setShowDevContent(boolean value) {
    
    showDevContent_ = value;
  }
  /**
   * <code>bool show_dev_content = 4;</code>
   */
  private void clearShowDevContent() {
    
    showDevContent_ = false;
  }

  public static com.halloapp.proto.server.PublicFeedRequest parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static com.halloapp.proto.server.PublicFeedRequest parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static com.halloapp.proto.server.PublicFeedRequest parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static com.halloapp.proto.server.PublicFeedRequest parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static com.halloapp.proto.server.PublicFeedRequest parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static com.halloapp.proto.server.PublicFeedRequest parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static com.halloapp.proto.server.PublicFeedRequest parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input);
  }
  public static com.halloapp.proto.server.PublicFeedRequest parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input, extensionRegistry);
  }
  public static com.halloapp.proto.server.PublicFeedRequest parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return parseDelimitedFrom(DEFAULT_INSTANCE, input);
  }
  public static com.halloapp.proto.server.PublicFeedRequest parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return parseDelimitedFrom(DEFAULT_INSTANCE, input, extensionRegistry);
  }
  public static com.halloapp.proto.server.PublicFeedRequest parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input);
  }
  public static com.halloapp.proto.server.PublicFeedRequest parseFrom(
      com.google.protobuf.CodedInputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input, extensionRegistry);
  }

  public static Builder newBuilder() {
    return (Builder) DEFAULT_INSTANCE.createBuilder();
  }
  public static Builder newBuilder(com.halloapp.proto.server.PublicFeedRequest prototype) {
    return (Builder) DEFAULT_INSTANCE.createBuilder(prototype);
  }

  /**
   * Protobuf type {@code server.PublicFeedRequest}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessageLite.Builder<
        com.halloapp.proto.server.PublicFeedRequest, Builder> implements
      // @@protoc_insertion_point(builder_implements:server.PublicFeedRequest)
      com.halloapp.proto.server.PublicFeedRequestOrBuilder {
    // Construct using com.halloapp.proto.server.PublicFeedRequest.newBuilder()
    private Builder() {
      super(DEFAULT_INSTANCE);
    }


    /**
     * <code>string cursor = 1;</code>
     * @return The cursor.
     */
    @java.lang.Override
    public java.lang.String getCursor() {
      return instance.getCursor();
    }
    /**
     * <code>string cursor = 1;</code>
     * @return The bytes for cursor.
     */
    @java.lang.Override
    public com.google.protobuf.ByteString
        getCursorBytes() {
      return instance.getCursorBytes();
    }
    /**
     * <code>string cursor = 1;</code>
     * @param value The cursor to set.
     * @return This builder for chaining.
     */
    public Builder setCursor(
        java.lang.String value) {
      copyOnWrite();
      instance.setCursor(value);
      return this;
    }
    /**
     * <code>string cursor = 1;</code>
     * @return This builder for chaining.
     */
    public Builder clearCursor() {
      copyOnWrite();
      instance.clearCursor();
      return this;
    }
    /**
     * <code>string cursor = 1;</code>
     * @param value The bytes for cursor to set.
     * @return This builder for chaining.
     */
    public Builder setCursorBytes(
        com.google.protobuf.ByteString value) {
      copyOnWrite();
      instance.setCursorBytes(value);
      return this;
    }

    /**
     * <code>.server.PublicFeedContentType public_feed_content_type = 2;</code>
     * @return The enum numeric value on the wire for publicFeedContentType.
     */
    @java.lang.Override
    public int getPublicFeedContentTypeValue() {
      return instance.getPublicFeedContentTypeValue();
    }
    /**
     * <code>.server.PublicFeedContentType public_feed_content_type = 2;</code>
     * @param value The publicFeedContentType to set.
     * @return This builder for chaining.
     */
    public Builder setPublicFeedContentTypeValue(int value) {
      copyOnWrite();
      instance.setPublicFeedContentTypeValue(value);
      return this;
    }
    /**
     * <code>.server.PublicFeedContentType public_feed_content_type = 2;</code>
     * @return The publicFeedContentType.
     */
    @java.lang.Override
    public com.halloapp.proto.server.PublicFeedContentType getPublicFeedContentType() {
      return instance.getPublicFeedContentType();
    }
    /**
     * <code>.server.PublicFeedContentType public_feed_content_type = 2;</code>
     * @param value The enum numeric value on the wire for publicFeedContentType to set.
     * @return This builder for chaining.
     */
    public Builder setPublicFeedContentType(com.halloapp.proto.server.PublicFeedContentType value) {
      copyOnWrite();
      instance.setPublicFeedContentType(value);
      return this;
    }
    /**
     * <code>.server.PublicFeedContentType public_feed_content_type = 2;</code>
     * @return This builder for chaining.
     */
    public Builder clearPublicFeedContentType() {
      copyOnWrite();
      instance.clearPublicFeedContentType();
      return this;
    }

    /**
     * <code>.server.GpsLocation gps_location = 3;</code>
     */
    @java.lang.Override
    public boolean hasGpsLocation() {
      return instance.hasGpsLocation();
    }
    /**
     * <code>.server.GpsLocation gps_location = 3;</code>
     */
    @java.lang.Override
    public com.halloapp.proto.server.GpsLocation getGpsLocation() {
      return instance.getGpsLocation();
    }
    /**
     * <code>.server.GpsLocation gps_location = 3;</code>
     */
    public Builder setGpsLocation(com.halloapp.proto.server.GpsLocation value) {
      copyOnWrite();
      instance.setGpsLocation(value);
      return this;
      }
    /**
     * <code>.server.GpsLocation gps_location = 3;</code>
     */
    public Builder setGpsLocation(
        com.halloapp.proto.server.GpsLocation.Builder builderForValue) {
      copyOnWrite();
      instance.setGpsLocation(builderForValue.build());
      return this;
    }
    /**
     * <code>.server.GpsLocation gps_location = 3;</code>
     */
    public Builder mergeGpsLocation(com.halloapp.proto.server.GpsLocation value) {
      copyOnWrite();
      instance.mergeGpsLocation(value);
      return this;
    }
    /**
     * <code>.server.GpsLocation gps_location = 3;</code>
     */
    public Builder clearGpsLocation() {  copyOnWrite();
      instance.clearGpsLocation();
      return this;
    }

    /**
     * <code>bool show_dev_content = 4;</code>
     * @return The showDevContent.
     */
    @java.lang.Override
    public boolean getShowDevContent() {
      return instance.getShowDevContent();
    }
    /**
     * <code>bool show_dev_content = 4;</code>
     * @param value The showDevContent to set.
     * @return This builder for chaining.
     */
    public Builder setShowDevContent(boolean value) {
      copyOnWrite();
      instance.setShowDevContent(value);
      return this;
    }
    /**
     * <code>bool show_dev_content = 4;</code>
     * @return This builder for chaining.
     */
    public Builder clearShowDevContent() {
      copyOnWrite();
      instance.clearShowDevContent();
      return this;
    }

    // @@protoc_insertion_point(builder_scope:server.PublicFeedRequest)
  }
  @java.lang.Override
  @java.lang.SuppressWarnings({"unchecked", "fallthrough"})
  protected final java.lang.Object dynamicMethod(
      com.google.protobuf.GeneratedMessageLite.MethodToInvoke method,
      java.lang.Object arg0, java.lang.Object arg1) {
    switch (method) {
      case NEW_MUTABLE_INSTANCE: {
        return new com.halloapp.proto.server.PublicFeedRequest();
      }
      case NEW_BUILDER: {
        return new Builder();
      }
      case BUILD_MESSAGE_INFO: {
          java.lang.Object[] objects = new java.lang.Object[] {
            "cursor_",
            "publicFeedContentType_",
            "gpsLocation_",
            "showDevContent_",
          };
          java.lang.String info =
              "\u0000\u0004\u0000\u0000\u0001\u0004\u0004\u0000\u0000\u0000\u0001\u0208\u0002\f" +
              "\u0003\t\u0004\u0007";
          return newMessageInfo(DEFAULT_INSTANCE, info, objects);
      }
      // fall through
      case GET_DEFAULT_INSTANCE: {
        return DEFAULT_INSTANCE;
      }
      case GET_PARSER: {
        com.google.protobuf.Parser<com.halloapp.proto.server.PublicFeedRequest> parser = PARSER;
        if (parser == null) {
          synchronized (com.halloapp.proto.server.PublicFeedRequest.class) {
            parser = PARSER;
            if (parser == null) {
              parser =
                  new DefaultInstanceBasedParser<com.halloapp.proto.server.PublicFeedRequest>(
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


  // @@protoc_insertion_point(class_scope:server.PublicFeedRequest)
  private static final com.halloapp.proto.server.PublicFeedRequest DEFAULT_INSTANCE;
  static {
    PublicFeedRequest defaultInstance = new PublicFeedRequest();
    // New instances are implicitly immutable so no need to make
    // immutable.
    DEFAULT_INSTANCE = defaultInstance;
    com.google.protobuf.GeneratedMessageLite.registerDefaultInstance(
      PublicFeedRequest.class, defaultInstance);
  }

  public static com.halloapp.proto.server.PublicFeedRequest getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static volatile com.google.protobuf.Parser<PublicFeedRequest> PARSER;

  public static com.google.protobuf.Parser<PublicFeedRequest> parser() {
    return DEFAULT_INSTANCE.getParserForType();
  }
}

