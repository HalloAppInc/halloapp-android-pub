// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: clients.proto

package com.halloapp.proto.clients;

/**
 * Protobuf type {@code clients.KMomentContainer}
 */
public  final class KMomentContainer extends
    com.google.protobuf.GeneratedMessageLite<
        KMomentContainer, KMomentContainer.Builder> implements
    // @@protoc_insertion_point(message_implements:clients.KMomentContainer)
    KMomentContainerOrBuilder {
  private KMomentContainer() {
    location_ = "";
  }
  private int momentCase_ = 0;
  private java.lang.Object moment_;
  public enum MomentCase {
    IMAGE(1),
    VIDEO(2),
    MOMENT_NOT_SET(0);
    private final int value;
    private MomentCase(int value) {
      this.value = value;
    }
    /**
     * @deprecated Use {@link #forNumber(int)} instead.
     */
    @java.lang.Deprecated
    public static MomentCase valueOf(int value) {
      return forNumber(value);
    }

    public static MomentCase forNumber(int value) {
      switch (value) {
        case 1: return IMAGE;
        case 2: return VIDEO;
        case 0: return MOMENT_NOT_SET;
        default: return null;
      }
    }
    public int getNumber() {
      return this.value;
    }
  };

  @java.lang.Override
  public MomentCase
  getMomentCase() {
    return MomentCase.forNumber(
        momentCase_);
  }

  private void clearMoment() {
    momentCase_ = 0;
    moment_ = null;
  }

  public static final int IMAGE_FIELD_NUMBER = 1;
  /**
   * <code>.clients.Image image = 1;</code>
   */
  @java.lang.Override
  public boolean hasImage() {
    return momentCase_ == 1;
  }
  /**
   * <code>.clients.Image image = 1;</code>
   */
  @java.lang.Override
  public com.halloapp.proto.clients.Image getImage() {
    if (momentCase_ == 1) {
       return (com.halloapp.proto.clients.Image) moment_;
    }
    return com.halloapp.proto.clients.Image.getDefaultInstance();
  }
  /**
   * <code>.clients.Image image = 1;</code>
   */
  private void setImage(com.halloapp.proto.clients.Image value) {
    value.getClass();
  moment_ = value;
    momentCase_ = 1;
  }
  /**
   * <code>.clients.Image image = 1;</code>
   */
  private void mergeImage(com.halloapp.proto.clients.Image value) {
    value.getClass();
  if (momentCase_ == 1 &&
        moment_ != com.halloapp.proto.clients.Image.getDefaultInstance()) {
      moment_ = com.halloapp.proto.clients.Image.newBuilder((com.halloapp.proto.clients.Image) moment_)
          .mergeFrom(value).buildPartial();
    } else {
      moment_ = value;
    }
    momentCase_ = 1;
  }
  /**
   * <code>.clients.Image image = 1;</code>
   */
  private void clearImage() {
    if (momentCase_ == 1) {
      momentCase_ = 0;
      moment_ = null;
    }
  }

  public static final int VIDEO_FIELD_NUMBER = 2;
  /**
   * <code>.clients.Video video = 2;</code>
   */
  @java.lang.Override
  public boolean hasVideo() {
    return momentCase_ == 2;
  }
  /**
   * <code>.clients.Video video = 2;</code>
   */
  @java.lang.Override
  public com.halloapp.proto.clients.Video getVideo() {
    if (momentCase_ == 2) {
       return (com.halloapp.proto.clients.Video) moment_;
    }
    return com.halloapp.proto.clients.Video.getDefaultInstance();
  }
  /**
   * <code>.clients.Video video = 2;</code>
   */
  private void setVideo(com.halloapp.proto.clients.Video value) {
    value.getClass();
  moment_ = value;
    momentCase_ = 2;
  }
  /**
   * <code>.clients.Video video = 2;</code>
   */
  private void mergeVideo(com.halloapp.proto.clients.Video value) {
    value.getClass();
  if (momentCase_ == 2 &&
        moment_ != com.halloapp.proto.clients.Video.getDefaultInstance()) {
      moment_ = com.halloapp.proto.clients.Video.newBuilder((com.halloapp.proto.clients.Video) moment_)
          .mergeFrom(value).buildPartial();
    } else {
      moment_ = value;
    }
    momentCase_ = 2;
  }
  /**
   * <code>.clients.Video video = 2;</code>
   */
  private void clearVideo() {
    if (momentCase_ == 2) {
      momentCase_ = 0;
      moment_ = null;
    }
  }

  public static final int LIVE_SELFIE_FIELD_NUMBER = 3;
  private com.halloapp.proto.clients.Video liveSelfie_;
  /**
   * <code>.clients.Video live_selfie = 3;</code>
   */
  @java.lang.Override
  public boolean hasLiveSelfie() {
    return liveSelfie_ != null;
  }
  /**
   * <code>.clients.Video live_selfie = 3;</code>
   */
  @java.lang.Override
  public com.halloapp.proto.clients.Video getLiveSelfie() {
    return liveSelfie_ == null ? com.halloapp.proto.clients.Video.getDefaultInstance() : liveSelfie_;
  }
  /**
   * <code>.clients.Video live_selfie = 3;</code>
   */
  private void setLiveSelfie(com.halloapp.proto.clients.Video value) {
    value.getClass();
  liveSelfie_ = value;
    
    }
  /**
   * <code>.clients.Video live_selfie = 3;</code>
   */
  @java.lang.SuppressWarnings({"ReferenceEquality"})
  private void mergeLiveSelfie(com.halloapp.proto.clients.Video value) {
    value.getClass();
  if (liveSelfie_ != null &&
        liveSelfie_ != com.halloapp.proto.clients.Video.getDefaultInstance()) {
      liveSelfie_ =
        com.halloapp.proto.clients.Video.newBuilder(liveSelfie_).mergeFrom(value).buildPartial();
    } else {
      liveSelfie_ = value;
    }
    
  }
  /**
   * <code>.clients.Video live_selfie = 3;</code>
   */
  private void clearLiveSelfie() {  liveSelfie_ = null;
    
  }

  public static final int SELFIE_POSITION_INFO_FIELD_NUMBER = 4;
  private com.halloapp.proto.clients.PositionInfo selfiePositionInfo_;
  /**
   * <code>.clients.PositionInfo selfie_position_info = 4;</code>
   */
  @java.lang.Override
  public boolean hasSelfiePositionInfo() {
    return selfiePositionInfo_ != null;
  }
  /**
   * <code>.clients.PositionInfo selfie_position_info = 4;</code>
   */
  @java.lang.Override
  public com.halloapp.proto.clients.PositionInfo getSelfiePositionInfo() {
    return selfiePositionInfo_ == null ? com.halloapp.proto.clients.PositionInfo.getDefaultInstance() : selfiePositionInfo_;
  }
  /**
   * <code>.clients.PositionInfo selfie_position_info = 4;</code>
   */
  private void setSelfiePositionInfo(com.halloapp.proto.clients.PositionInfo value) {
    value.getClass();
  selfiePositionInfo_ = value;
    
    }
  /**
   * <code>.clients.PositionInfo selfie_position_info = 4;</code>
   */
  @java.lang.SuppressWarnings({"ReferenceEquality"})
  private void mergeSelfiePositionInfo(com.halloapp.proto.clients.PositionInfo value) {
    value.getClass();
  if (selfiePositionInfo_ != null &&
        selfiePositionInfo_ != com.halloapp.proto.clients.PositionInfo.getDefaultInstance()) {
      selfiePositionInfo_ =
        com.halloapp.proto.clients.PositionInfo.newBuilder(selfiePositionInfo_).mergeFrom(value).buildPartial();
    } else {
      selfiePositionInfo_ = value;
    }
    
  }
  /**
   * <code>.clients.PositionInfo selfie_position_info = 4;</code>
   */
  private void clearSelfiePositionInfo() {  selfiePositionInfo_ = null;
    
  }

  public static final int LOCATION_FIELD_NUMBER = 5;
  private java.lang.String location_;
  /**
   * <code>string location = 5;</code>
   * @return The location.
   */
  @java.lang.Override
  public java.lang.String getLocation() {
    return location_;
  }
  /**
   * <code>string location = 5;</code>
   * @return The bytes for location.
   */
  @java.lang.Override
  public com.google.protobuf.ByteString
      getLocationBytes() {
    return com.google.protobuf.ByteString.copyFromUtf8(location_);
  }
  /**
   * <code>string location = 5;</code>
   * @param value The location to set.
   */
  private void setLocation(
      java.lang.String value) {
    java.lang.Class<?> valueClass = value.getClass();
  
    location_ = value;
  }
  /**
   * <code>string location = 5;</code>
   */
  private void clearLocation() {
    
    location_ = getDefaultInstance().getLocation();
  }
  /**
   * <code>string location = 5;</code>
   * @param value The bytes for location to set.
   */
  private void setLocationBytes(
      com.google.protobuf.ByteString value) {
    checkByteStringIsUtf8(value);
    location_ = value.toStringUtf8();
    
  }

  public static com.halloapp.proto.clients.KMomentContainer parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static com.halloapp.proto.clients.KMomentContainer parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static com.halloapp.proto.clients.KMomentContainer parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static com.halloapp.proto.clients.KMomentContainer parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static com.halloapp.proto.clients.KMomentContainer parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static com.halloapp.proto.clients.KMomentContainer parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static com.halloapp.proto.clients.KMomentContainer parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input);
  }
  public static com.halloapp.proto.clients.KMomentContainer parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input, extensionRegistry);
  }
  public static com.halloapp.proto.clients.KMomentContainer parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return parseDelimitedFrom(DEFAULT_INSTANCE, input);
  }
  public static com.halloapp.proto.clients.KMomentContainer parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return parseDelimitedFrom(DEFAULT_INSTANCE, input, extensionRegistry);
  }
  public static com.halloapp.proto.clients.KMomentContainer parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input);
  }
  public static com.halloapp.proto.clients.KMomentContainer parseFrom(
      com.google.protobuf.CodedInputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input, extensionRegistry);
  }

  public static Builder newBuilder() {
    return (Builder) DEFAULT_INSTANCE.createBuilder();
  }
  public static Builder newBuilder(com.halloapp.proto.clients.KMomentContainer prototype) {
    return (Builder) DEFAULT_INSTANCE.createBuilder(prototype);
  }

  /**
   * Protobuf type {@code clients.KMomentContainer}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessageLite.Builder<
        com.halloapp.proto.clients.KMomentContainer, Builder> implements
      // @@protoc_insertion_point(builder_implements:clients.KMomentContainer)
      com.halloapp.proto.clients.KMomentContainerOrBuilder {
    // Construct using com.halloapp.proto.clients.KMomentContainer.newBuilder()
    private Builder() {
      super(DEFAULT_INSTANCE);
    }

    @java.lang.Override
    public MomentCase
        getMomentCase() {
      return instance.getMomentCase();
    }

    public Builder clearMoment() {
      copyOnWrite();
      instance.clearMoment();
      return this;
    }


    /**
     * <code>.clients.Image image = 1;</code>
     */
    @java.lang.Override
    public boolean hasImage() {
      return instance.hasImage();
    }
    /**
     * <code>.clients.Image image = 1;</code>
     */
    @java.lang.Override
    public com.halloapp.proto.clients.Image getImage() {
      return instance.getImage();
    }
    /**
     * <code>.clients.Image image = 1;</code>
     */
    public Builder setImage(com.halloapp.proto.clients.Image value) {
      copyOnWrite();
      instance.setImage(value);
      return this;
    }
    /**
     * <code>.clients.Image image = 1;</code>
     */
    public Builder setImage(
        com.halloapp.proto.clients.Image.Builder builderForValue) {
      copyOnWrite();
      instance.setImage(builderForValue.build());
      return this;
    }
    /**
     * <code>.clients.Image image = 1;</code>
     */
    public Builder mergeImage(com.halloapp.proto.clients.Image value) {
      copyOnWrite();
      instance.mergeImage(value);
      return this;
    }
    /**
     * <code>.clients.Image image = 1;</code>
     */
    public Builder clearImage() {
      copyOnWrite();
      instance.clearImage();
      return this;
    }

    /**
     * <code>.clients.Video video = 2;</code>
     */
    @java.lang.Override
    public boolean hasVideo() {
      return instance.hasVideo();
    }
    /**
     * <code>.clients.Video video = 2;</code>
     */
    @java.lang.Override
    public com.halloapp.proto.clients.Video getVideo() {
      return instance.getVideo();
    }
    /**
     * <code>.clients.Video video = 2;</code>
     */
    public Builder setVideo(com.halloapp.proto.clients.Video value) {
      copyOnWrite();
      instance.setVideo(value);
      return this;
    }
    /**
     * <code>.clients.Video video = 2;</code>
     */
    public Builder setVideo(
        com.halloapp.proto.clients.Video.Builder builderForValue) {
      copyOnWrite();
      instance.setVideo(builderForValue.build());
      return this;
    }
    /**
     * <code>.clients.Video video = 2;</code>
     */
    public Builder mergeVideo(com.halloapp.proto.clients.Video value) {
      copyOnWrite();
      instance.mergeVideo(value);
      return this;
    }
    /**
     * <code>.clients.Video video = 2;</code>
     */
    public Builder clearVideo() {
      copyOnWrite();
      instance.clearVideo();
      return this;
    }

    /**
     * <code>.clients.Video live_selfie = 3;</code>
     */
    @java.lang.Override
    public boolean hasLiveSelfie() {
      return instance.hasLiveSelfie();
    }
    /**
     * <code>.clients.Video live_selfie = 3;</code>
     */
    @java.lang.Override
    public com.halloapp.proto.clients.Video getLiveSelfie() {
      return instance.getLiveSelfie();
    }
    /**
     * <code>.clients.Video live_selfie = 3;</code>
     */
    public Builder setLiveSelfie(com.halloapp.proto.clients.Video value) {
      copyOnWrite();
      instance.setLiveSelfie(value);
      return this;
      }
    /**
     * <code>.clients.Video live_selfie = 3;</code>
     */
    public Builder setLiveSelfie(
        com.halloapp.proto.clients.Video.Builder builderForValue) {
      copyOnWrite();
      instance.setLiveSelfie(builderForValue.build());
      return this;
    }
    /**
     * <code>.clients.Video live_selfie = 3;</code>
     */
    public Builder mergeLiveSelfie(com.halloapp.proto.clients.Video value) {
      copyOnWrite();
      instance.mergeLiveSelfie(value);
      return this;
    }
    /**
     * <code>.clients.Video live_selfie = 3;</code>
     */
    public Builder clearLiveSelfie() {  copyOnWrite();
      instance.clearLiveSelfie();
      return this;
    }

    /**
     * <code>.clients.PositionInfo selfie_position_info = 4;</code>
     */
    @java.lang.Override
    public boolean hasSelfiePositionInfo() {
      return instance.hasSelfiePositionInfo();
    }
    /**
     * <code>.clients.PositionInfo selfie_position_info = 4;</code>
     */
    @java.lang.Override
    public com.halloapp.proto.clients.PositionInfo getSelfiePositionInfo() {
      return instance.getSelfiePositionInfo();
    }
    /**
     * <code>.clients.PositionInfo selfie_position_info = 4;</code>
     */
    public Builder setSelfiePositionInfo(com.halloapp.proto.clients.PositionInfo value) {
      copyOnWrite();
      instance.setSelfiePositionInfo(value);
      return this;
      }
    /**
     * <code>.clients.PositionInfo selfie_position_info = 4;</code>
     */
    public Builder setSelfiePositionInfo(
        com.halloapp.proto.clients.PositionInfo.Builder builderForValue) {
      copyOnWrite();
      instance.setSelfiePositionInfo(builderForValue.build());
      return this;
    }
    /**
     * <code>.clients.PositionInfo selfie_position_info = 4;</code>
     */
    public Builder mergeSelfiePositionInfo(com.halloapp.proto.clients.PositionInfo value) {
      copyOnWrite();
      instance.mergeSelfiePositionInfo(value);
      return this;
    }
    /**
     * <code>.clients.PositionInfo selfie_position_info = 4;</code>
     */
    public Builder clearSelfiePositionInfo() {  copyOnWrite();
      instance.clearSelfiePositionInfo();
      return this;
    }

    /**
     * <code>string location = 5;</code>
     * @return The location.
     */
    @java.lang.Override
    public java.lang.String getLocation() {
      return instance.getLocation();
    }
    /**
     * <code>string location = 5;</code>
     * @return The bytes for location.
     */
    @java.lang.Override
    public com.google.protobuf.ByteString
        getLocationBytes() {
      return instance.getLocationBytes();
    }
    /**
     * <code>string location = 5;</code>
     * @param value The location to set.
     * @return This builder for chaining.
     */
    public Builder setLocation(
        java.lang.String value) {
      copyOnWrite();
      instance.setLocation(value);
      return this;
    }
    /**
     * <code>string location = 5;</code>
     * @return This builder for chaining.
     */
    public Builder clearLocation() {
      copyOnWrite();
      instance.clearLocation();
      return this;
    }
    /**
     * <code>string location = 5;</code>
     * @param value The bytes for location to set.
     * @return This builder for chaining.
     */
    public Builder setLocationBytes(
        com.google.protobuf.ByteString value) {
      copyOnWrite();
      instance.setLocationBytes(value);
      return this;
    }

    // @@protoc_insertion_point(builder_scope:clients.KMomentContainer)
  }
  @java.lang.Override
  @java.lang.SuppressWarnings({"unchecked", "fallthrough"})
  protected final java.lang.Object dynamicMethod(
      com.google.protobuf.GeneratedMessageLite.MethodToInvoke method,
      java.lang.Object arg0, java.lang.Object arg1) {
    switch (method) {
      case NEW_MUTABLE_INSTANCE: {
        return new com.halloapp.proto.clients.KMomentContainer();
      }
      case NEW_BUILDER: {
        return new Builder();
      }
      case BUILD_MESSAGE_INFO: {
          java.lang.Object[] objects = new java.lang.Object[] {
            "moment_",
            "momentCase_",
            com.halloapp.proto.clients.Image.class,
            com.halloapp.proto.clients.Video.class,
            "liveSelfie_",
            "selfiePositionInfo_",
            "location_",
          };
          java.lang.String info =
              "\u0000\u0005\u0001\u0000\u0001\u0005\u0005\u0000\u0000\u0000\u0001<\u0000\u0002<" +
              "\u0000\u0003\t\u0004\t\u0005\u0208";
          return newMessageInfo(DEFAULT_INSTANCE, info, objects);
      }
      // fall through
      case GET_DEFAULT_INSTANCE: {
        return DEFAULT_INSTANCE;
      }
      case GET_PARSER: {
        com.google.protobuf.Parser<com.halloapp.proto.clients.KMomentContainer> parser = PARSER;
        if (parser == null) {
          synchronized (com.halloapp.proto.clients.KMomentContainer.class) {
            parser = PARSER;
            if (parser == null) {
              parser =
                  new DefaultInstanceBasedParser<com.halloapp.proto.clients.KMomentContainer>(
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


  // @@protoc_insertion_point(class_scope:clients.KMomentContainer)
  private static final com.halloapp.proto.clients.KMomentContainer DEFAULT_INSTANCE;
  static {
    KMomentContainer defaultInstance = new KMomentContainer();
    // New instances are implicitly immutable so no need to make
    // immutable.
    DEFAULT_INSTANCE = defaultInstance;
    com.google.protobuf.GeneratedMessageLite.registerDefaultInstance(
      KMomentContainer.class, defaultInstance);
  }

  public static com.halloapp.proto.clients.KMomentContainer getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static volatile com.google.protobuf.Parser<KMomentContainer> PARSER;

  public static com.google.protobuf.Parser<KMomentContainer> parser() {
    return DEFAULT_INSTANCE.getParserForType();
  }
}

