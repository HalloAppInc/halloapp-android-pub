// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: clients.proto

package com.halloapp.proto.clients;

/**
 * Protobuf type {@code clients.ContactEmail}
 */
public  final class ContactEmail extends
    com.google.protobuf.GeneratedMessageLite<
        ContactEmail, ContactEmail.Builder> implements
    // @@protoc_insertion_point(message_implements:clients.ContactEmail)
    ContactEmailOrBuilder {
  private ContactEmail() {
    label_ = "";
    address_ = "";
  }
  public static final int LABEL_FIELD_NUMBER = 1;
  private java.lang.String label_;
  /**
   * <code>string label = 1;</code>
   * @return The label.
   */
  @java.lang.Override
  public java.lang.String getLabel() {
    return label_;
  }
  /**
   * <code>string label = 1;</code>
   * @return The bytes for label.
   */
  @java.lang.Override
  public com.google.protobuf.ByteString
      getLabelBytes() {
    return com.google.protobuf.ByteString.copyFromUtf8(label_);
  }
  /**
   * <code>string label = 1;</code>
   * @param value The label to set.
   */
  private void setLabel(
      java.lang.String value) {
    java.lang.Class<?> valueClass = value.getClass();
  
    label_ = value;
  }
  /**
   * <code>string label = 1;</code>
   */
  private void clearLabel() {
    
    label_ = getDefaultInstance().getLabel();
  }
  /**
   * <code>string label = 1;</code>
   * @param value The bytes for label to set.
   */
  private void setLabelBytes(
      com.google.protobuf.ByteString value) {
    checkByteStringIsUtf8(value);
    label_ = value.toStringUtf8();
    
  }

  public static final int ADDRESS_FIELD_NUMBER = 2;
  private java.lang.String address_;
  /**
   * <code>string address = 2;</code>
   * @return The address.
   */
  @java.lang.Override
  public java.lang.String getAddress() {
    return address_;
  }
  /**
   * <code>string address = 2;</code>
   * @return The bytes for address.
   */
  @java.lang.Override
  public com.google.protobuf.ByteString
      getAddressBytes() {
    return com.google.protobuf.ByteString.copyFromUtf8(address_);
  }
  /**
   * <code>string address = 2;</code>
   * @param value The address to set.
   */
  private void setAddress(
      java.lang.String value) {
    java.lang.Class<?> valueClass = value.getClass();
  
    address_ = value;
  }
  /**
   * <code>string address = 2;</code>
   */
  private void clearAddress() {
    
    address_ = getDefaultInstance().getAddress();
  }
  /**
   * <code>string address = 2;</code>
   * @param value The bytes for address to set.
   */
  private void setAddressBytes(
      com.google.protobuf.ByteString value) {
    checkByteStringIsUtf8(value);
    address_ = value.toStringUtf8();
    
  }

  public static com.halloapp.proto.clients.ContactEmail parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static com.halloapp.proto.clients.ContactEmail parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static com.halloapp.proto.clients.ContactEmail parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static com.halloapp.proto.clients.ContactEmail parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static com.halloapp.proto.clients.ContactEmail parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static com.halloapp.proto.clients.ContactEmail parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static com.halloapp.proto.clients.ContactEmail parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input);
  }
  public static com.halloapp.proto.clients.ContactEmail parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input, extensionRegistry);
  }
  public static com.halloapp.proto.clients.ContactEmail parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return parseDelimitedFrom(DEFAULT_INSTANCE, input);
  }
  public static com.halloapp.proto.clients.ContactEmail parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return parseDelimitedFrom(DEFAULT_INSTANCE, input, extensionRegistry);
  }
  public static com.halloapp.proto.clients.ContactEmail parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input);
  }
  public static com.halloapp.proto.clients.ContactEmail parseFrom(
      com.google.protobuf.CodedInputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input, extensionRegistry);
  }

  public static Builder newBuilder() {
    return (Builder) DEFAULT_INSTANCE.createBuilder();
  }
  public static Builder newBuilder(com.halloapp.proto.clients.ContactEmail prototype) {
    return (Builder) DEFAULT_INSTANCE.createBuilder(prototype);
  }

  /**
   * Protobuf type {@code clients.ContactEmail}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessageLite.Builder<
        com.halloapp.proto.clients.ContactEmail, Builder> implements
      // @@protoc_insertion_point(builder_implements:clients.ContactEmail)
      com.halloapp.proto.clients.ContactEmailOrBuilder {
    // Construct using com.halloapp.proto.clients.ContactEmail.newBuilder()
    private Builder() {
      super(DEFAULT_INSTANCE);
    }


    /**
     * <code>string label = 1;</code>
     * @return The label.
     */
    @java.lang.Override
    public java.lang.String getLabel() {
      return instance.getLabel();
    }
    /**
     * <code>string label = 1;</code>
     * @return The bytes for label.
     */
    @java.lang.Override
    public com.google.protobuf.ByteString
        getLabelBytes() {
      return instance.getLabelBytes();
    }
    /**
     * <code>string label = 1;</code>
     * @param value The label to set.
     * @return This builder for chaining.
     */
    public Builder setLabel(
        java.lang.String value) {
      copyOnWrite();
      instance.setLabel(value);
      return this;
    }
    /**
     * <code>string label = 1;</code>
     * @return This builder for chaining.
     */
    public Builder clearLabel() {
      copyOnWrite();
      instance.clearLabel();
      return this;
    }
    /**
     * <code>string label = 1;</code>
     * @param value The bytes for label to set.
     * @return This builder for chaining.
     */
    public Builder setLabelBytes(
        com.google.protobuf.ByteString value) {
      copyOnWrite();
      instance.setLabelBytes(value);
      return this;
    }

    /**
     * <code>string address = 2;</code>
     * @return The address.
     */
    @java.lang.Override
    public java.lang.String getAddress() {
      return instance.getAddress();
    }
    /**
     * <code>string address = 2;</code>
     * @return The bytes for address.
     */
    @java.lang.Override
    public com.google.protobuf.ByteString
        getAddressBytes() {
      return instance.getAddressBytes();
    }
    /**
     * <code>string address = 2;</code>
     * @param value The address to set.
     * @return This builder for chaining.
     */
    public Builder setAddress(
        java.lang.String value) {
      copyOnWrite();
      instance.setAddress(value);
      return this;
    }
    /**
     * <code>string address = 2;</code>
     * @return This builder for chaining.
     */
    public Builder clearAddress() {
      copyOnWrite();
      instance.clearAddress();
      return this;
    }
    /**
     * <code>string address = 2;</code>
     * @param value The bytes for address to set.
     * @return This builder for chaining.
     */
    public Builder setAddressBytes(
        com.google.protobuf.ByteString value) {
      copyOnWrite();
      instance.setAddressBytes(value);
      return this;
    }

    // @@protoc_insertion_point(builder_scope:clients.ContactEmail)
  }
  @java.lang.Override
  @java.lang.SuppressWarnings({"unchecked", "fallthrough"})
  protected final java.lang.Object dynamicMethod(
      com.google.protobuf.GeneratedMessageLite.MethodToInvoke method,
      java.lang.Object arg0, java.lang.Object arg1) {
    switch (method) {
      case NEW_MUTABLE_INSTANCE: {
        return new com.halloapp.proto.clients.ContactEmail();
      }
      case NEW_BUILDER: {
        return new Builder();
      }
      case BUILD_MESSAGE_INFO: {
          java.lang.Object[] objects = new java.lang.Object[] {
            "label_",
            "address_",
          };
          java.lang.String info =
              "\u0000\u0002\u0000\u0000\u0001\u0002\u0002\u0000\u0000\u0000\u0001\u0208\u0002\u0208" +
              "";
          return newMessageInfo(DEFAULT_INSTANCE, info, objects);
      }
      // fall through
      case GET_DEFAULT_INSTANCE: {
        return DEFAULT_INSTANCE;
      }
      case GET_PARSER: {
        com.google.protobuf.Parser<com.halloapp.proto.clients.ContactEmail> parser = PARSER;
        if (parser == null) {
          synchronized (com.halloapp.proto.clients.ContactEmail.class) {
            parser = PARSER;
            if (parser == null) {
              parser =
                  new DefaultInstanceBasedParser<com.halloapp.proto.clients.ContactEmail>(
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


  // @@protoc_insertion_point(class_scope:clients.ContactEmail)
  private static final com.halloapp.proto.clients.ContactEmail DEFAULT_INSTANCE;
  static {
    ContactEmail defaultInstance = new ContactEmail();
    // New instances are implicitly immutable so no need to make
    // immutable.
    DEFAULT_INSTANCE = defaultInstance;
    com.google.protobuf.GeneratedMessageLite.registerDefaultInstance(
      ContactEmail.class, defaultInstance);
  }

  public static com.halloapp.proto.clients.ContactEmail getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static volatile com.google.protobuf.Parser<ContactEmail> PARSER;

  public static com.google.protobuf.Parser<ContactEmail> parser() {
    return DEFAULT_INSTANCE.getParserForType();
  }
}

