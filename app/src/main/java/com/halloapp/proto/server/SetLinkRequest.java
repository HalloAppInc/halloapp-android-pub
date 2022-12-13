// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: server.proto

package com.halloapp.proto.server;

/**
 * Protobuf type {@code server.SetLinkRequest}
 */
public  final class SetLinkRequest extends
    com.google.protobuf.GeneratedMessageLite<
        SetLinkRequest, SetLinkRequest.Builder> implements
    // @@protoc_insertion_point(message_implements:server.SetLinkRequest)
    SetLinkRequestOrBuilder {
  private SetLinkRequest() {
  }
  public static final int LINK_FIELD_NUMBER = 1;
  private com.halloapp.proto.server.Link link_;
  /**
   * <code>.server.Link link = 1;</code>
   */
  @java.lang.Override
  public boolean hasLink() {
    return link_ != null;
  }
  /**
   * <code>.server.Link link = 1;</code>
   */
  @java.lang.Override
  public com.halloapp.proto.server.Link getLink() {
    return link_ == null ? com.halloapp.proto.server.Link.getDefaultInstance() : link_;
  }
  /**
   * <code>.server.Link link = 1;</code>
   */
  private void setLink(com.halloapp.proto.server.Link value) {
    value.getClass();
  link_ = value;
    
    }
  /**
   * <code>.server.Link link = 1;</code>
   */
  @java.lang.SuppressWarnings({"ReferenceEquality"})
  private void mergeLink(com.halloapp.proto.server.Link value) {
    value.getClass();
  if (link_ != null &&
        link_ != com.halloapp.proto.server.Link.getDefaultInstance()) {
      link_ =
        com.halloapp.proto.server.Link.newBuilder(link_).mergeFrom(value).buildPartial();
    } else {
      link_ = value;
    }
    
  }
  /**
   * <code>.server.Link link = 1;</code>
   */
  private void clearLink() {  link_ = null;
    
  }

  public static com.halloapp.proto.server.SetLinkRequest parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static com.halloapp.proto.server.SetLinkRequest parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static com.halloapp.proto.server.SetLinkRequest parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static com.halloapp.proto.server.SetLinkRequest parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static com.halloapp.proto.server.SetLinkRequest parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static com.halloapp.proto.server.SetLinkRequest parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static com.halloapp.proto.server.SetLinkRequest parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input);
  }
  public static com.halloapp.proto.server.SetLinkRequest parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input, extensionRegistry);
  }
  public static com.halloapp.proto.server.SetLinkRequest parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return parseDelimitedFrom(DEFAULT_INSTANCE, input);
  }
  public static com.halloapp.proto.server.SetLinkRequest parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return parseDelimitedFrom(DEFAULT_INSTANCE, input, extensionRegistry);
  }
  public static com.halloapp.proto.server.SetLinkRequest parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input);
  }
  public static com.halloapp.proto.server.SetLinkRequest parseFrom(
      com.google.protobuf.CodedInputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input, extensionRegistry);
  }

  public static Builder newBuilder() {
    return (Builder) DEFAULT_INSTANCE.createBuilder();
  }
  public static Builder newBuilder(com.halloapp.proto.server.SetLinkRequest prototype) {
    return (Builder) DEFAULT_INSTANCE.createBuilder(prototype);
  }

  /**
   * Protobuf type {@code server.SetLinkRequest}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessageLite.Builder<
        com.halloapp.proto.server.SetLinkRequest, Builder> implements
      // @@protoc_insertion_point(builder_implements:server.SetLinkRequest)
      com.halloapp.proto.server.SetLinkRequestOrBuilder {
    // Construct using com.halloapp.proto.server.SetLinkRequest.newBuilder()
    private Builder() {
      super(DEFAULT_INSTANCE);
    }


    /**
     * <code>.server.Link link = 1;</code>
     */
    @java.lang.Override
    public boolean hasLink() {
      return instance.hasLink();
    }
    /**
     * <code>.server.Link link = 1;</code>
     */
    @java.lang.Override
    public com.halloapp.proto.server.Link getLink() {
      return instance.getLink();
    }
    /**
     * <code>.server.Link link = 1;</code>
     */
    public Builder setLink(com.halloapp.proto.server.Link value) {
      copyOnWrite();
      instance.setLink(value);
      return this;
      }
    /**
     * <code>.server.Link link = 1;</code>
     */
    public Builder setLink(
        com.halloapp.proto.server.Link.Builder builderForValue) {
      copyOnWrite();
      instance.setLink(builderForValue.build());
      return this;
    }
    /**
     * <code>.server.Link link = 1;</code>
     */
    public Builder mergeLink(com.halloapp.proto.server.Link value) {
      copyOnWrite();
      instance.mergeLink(value);
      return this;
    }
    /**
     * <code>.server.Link link = 1;</code>
     */
    public Builder clearLink() {  copyOnWrite();
      instance.clearLink();
      return this;
    }

    // @@protoc_insertion_point(builder_scope:server.SetLinkRequest)
  }
  @java.lang.Override
  @java.lang.SuppressWarnings({"unchecked", "fallthrough"})
  protected final java.lang.Object dynamicMethod(
      com.google.protobuf.GeneratedMessageLite.MethodToInvoke method,
      java.lang.Object arg0, java.lang.Object arg1) {
    switch (method) {
      case NEW_MUTABLE_INSTANCE: {
        return new com.halloapp.proto.server.SetLinkRequest();
      }
      case NEW_BUILDER: {
        return new Builder();
      }
      case BUILD_MESSAGE_INFO: {
          java.lang.Object[] objects = new java.lang.Object[] {
            "link_",
          };
          java.lang.String info =
              "\u0000\u0001\u0000\u0000\u0001\u0001\u0001\u0000\u0000\u0000\u0001\t";
          return newMessageInfo(DEFAULT_INSTANCE, info, objects);
      }
      // fall through
      case GET_DEFAULT_INSTANCE: {
        return DEFAULT_INSTANCE;
      }
      case GET_PARSER: {
        com.google.protobuf.Parser<com.halloapp.proto.server.SetLinkRequest> parser = PARSER;
        if (parser == null) {
          synchronized (com.halloapp.proto.server.SetLinkRequest.class) {
            parser = PARSER;
            if (parser == null) {
              parser =
                  new DefaultInstanceBasedParser<com.halloapp.proto.server.SetLinkRequest>(
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


  // @@protoc_insertion_point(class_scope:server.SetLinkRequest)
  private static final com.halloapp.proto.server.SetLinkRequest DEFAULT_INSTANCE;
  static {
    SetLinkRequest defaultInstance = new SetLinkRequest();
    // New instances are implicitly immutable so no need to make
    // immutable.
    DEFAULT_INSTANCE = defaultInstance;
    com.google.protobuf.GeneratedMessageLite.registerDefaultInstance(
      SetLinkRequest.class, defaultInstance);
  }

  public static com.halloapp.proto.server.SetLinkRequest getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static volatile com.google.protobuf.Parser<SetLinkRequest> PARSER;

  public static com.google.protobuf.Parser<SetLinkRequest> parser() {
    return DEFAULT_INSTANCE.getParserForType();
  }
}

