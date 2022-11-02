// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: web.proto

package com.halloapp.proto.web;

/**
 * Protobuf type {@code web.GroupResponse}
 */
public  final class GroupResponse extends
    com.google.protobuf.GeneratedMessageLite<
        GroupResponse, GroupResponse.Builder> implements
    // @@protoc_insertion_point(message_implements:web.GroupResponse)
    GroupResponseOrBuilder {
  private GroupResponse() {
    id_ = "";
    groups_ = emptyProtobufList();
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

  public static final int GROUPS_FIELD_NUMBER = 2;
  private com.google.protobuf.Internal.ProtobufList<com.halloapp.proto.web.GroupDisplayInfo> groups_;
  /**
   * <code>repeated .web.GroupDisplayInfo groups = 2;</code>
   */
  @java.lang.Override
  public java.util.List<com.halloapp.proto.web.GroupDisplayInfo> getGroupsList() {
    return groups_;
  }
  /**
   * <code>repeated .web.GroupDisplayInfo groups = 2;</code>
   */
  public java.util.List<? extends com.halloapp.proto.web.GroupDisplayInfoOrBuilder> 
      getGroupsOrBuilderList() {
    return groups_;
  }
  /**
   * <code>repeated .web.GroupDisplayInfo groups = 2;</code>
   */
  @java.lang.Override
  public int getGroupsCount() {
    return groups_.size();
  }
  /**
   * <code>repeated .web.GroupDisplayInfo groups = 2;</code>
   */
  @java.lang.Override
  public com.halloapp.proto.web.GroupDisplayInfo getGroups(int index) {
    return groups_.get(index);
  }
  /**
   * <code>repeated .web.GroupDisplayInfo groups = 2;</code>
   */
  public com.halloapp.proto.web.GroupDisplayInfoOrBuilder getGroupsOrBuilder(
      int index) {
    return groups_.get(index);
  }
  private void ensureGroupsIsMutable() {
    com.google.protobuf.Internal.ProtobufList<com.halloapp.proto.web.GroupDisplayInfo> tmp = groups_;
    if (!tmp.isModifiable()) {
      groups_ =
          com.google.protobuf.GeneratedMessageLite.mutableCopy(tmp);
     }
  }

  /**
   * <code>repeated .web.GroupDisplayInfo groups = 2;</code>
   */
  private void setGroups(
      int index, com.halloapp.proto.web.GroupDisplayInfo value) {
    value.getClass();
  ensureGroupsIsMutable();
    groups_.set(index, value);
  }
  /**
   * <code>repeated .web.GroupDisplayInfo groups = 2;</code>
   */
  private void addGroups(com.halloapp.proto.web.GroupDisplayInfo value) {
    value.getClass();
  ensureGroupsIsMutable();
    groups_.add(value);
  }
  /**
   * <code>repeated .web.GroupDisplayInfo groups = 2;</code>
   */
  private void addGroups(
      int index, com.halloapp.proto.web.GroupDisplayInfo value) {
    value.getClass();
  ensureGroupsIsMutable();
    groups_.add(index, value);
  }
  /**
   * <code>repeated .web.GroupDisplayInfo groups = 2;</code>
   */
  private void addAllGroups(
      java.lang.Iterable<? extends com.halloapp.proto.web.GroupDisplayInfo> values) {
    ensureGroupsIsMutable();
    com.google.protobuf.AbstractMessageLite.addAll(
        values, groups_);
  }
  /**
   * <code>repeated .web.GroupDisplayInfo groups = 2;</code>
   */
  private void clearGroups() {
    groups_ = emptyProtobufList();
  }
  /**
   * <code>repeated .web.GroupDisplayInfo groups = 2;</code>
   */
  private void removeGroups(int index) {
    ensureGroupsIsMutable();
    groups_.remove(index);
  }

  public static com.halloapp.proto.web.GroupResponse parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static com.halloapp.proto.web.GroupResponse parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static com.halloapp.proto.web.GroupResponse parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static com.halloapp.proto.web.GroupResponse parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static com.halloapp.proto.web.GroupResponse parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static com.halloapp.proto.web.GroupResponse parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static com.halloapp.proto.web.GroupResponse parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input);
  }
  public static com.halloapp.proto.web.GroupResponse parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input, extensionRegistry);
  }
  public static com.halloapp.proto.web.GroupResponse parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return parseDelimitedFrom(DEFAULT_INSTANCE, input);
  }
  public static com.halloapp.proto.web.GroupResponse parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return parseDelimitedFrom(DEFAULT_INSTANCE, input, extensionRegistry);
  }
  public static com.halloapp.proto.web.GroupResponse parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input);
  }
  public static com.halloapp.proto.web.GroupResponse parseFrom(
      com.google.protobuf.CodedInputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input, extensionRegistry);
  }

  public static Builder newBuilder() {
    return (Builder) DEFAULT_INSTANCE.createBuilder();
  }
  public static Builder newBuilder(com.halloapp.proto.web.GroupResponse prototype) {
    return (Builder) DEFAULT_INSTANCE.createBuilder(prototype);
  }

  /**
   * Protobuf type {@code web.GroupResponse}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessageLite.Builder<
        com.halloapp.proto.web.GroupResponse, Builder> implements
      // @@protoc_insertion_point(builder_implements:web.GroupResponse)
      com.halloapp.proto.web.GroupResponseOrBuilder {
    // Construct using com.halloapp.proto.web.GroupResponse.newBuilder()
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
     * <code>repeated .web.GroupDisplayInfo groups = 2;</code>
     */
    @java.lang.Override
    public java.util.List<com.halloapp.proto.web.GroupDisplayInfo> getGroupsList() {
      return java.util.Collections.unmodifiableList(
          instance.getGroupsList());
    }
    /**
     * <code>repeated .web.GroupDisplayInfo groups = 2;</code>
     */
    @java.lang.Override
    public int getGroupsCount() {
      return instance.getGroupsCount();
    }/**
     * <code>repeated .web.GroupDisplayInfo groups = 2;</code>
     */
    @java.lang.Override
    public com.halloapp.proto.web.GroupDisplayInfo getGroups(int index) {
      return instance.getGroups(index);
    }
    /**
     * <code>repeated .web.GroupDisplayInfo groups = 2;</code>
     */
    public Builder setGroups(
        int index, com.halloapp.proto.web.GroupDisplayInfo value) {
      copyOnWrite();
      instance.setGroups(index, value);
      return this;
    }
    /**
     * <code>repeated .web.GroupDisplayInfo groups = 2;</code>
     */
    public Builder setGroups(
        int index, com.halloapp.proto.web.GroupDisplayInfo.Builder builderForValue) {
      copyOnWrite();
      instance.setGroups(index,
          builderForValue.build());
      return this;
    }
    /**
     * <code>repeated .web.GroupDisplayInfo groups = 2;</code>
     */
    public Builder addGroups(com.halloapp.proto.web.GroupDisplayInfo value) {
      copyOnWrite();
      instance.addGroups(value);
      return this;
    }
    /**
     * <code>repeated .web.GroupDisplayInfo groups = 2;</code>
     */
    public Builder addGroups(
        int index, com.halloapp.proto.web.GroupDisplayInfo value) {
      copyOnWrite();
      instance.addGroups(index, value);
      return this;
    }
    /**
     * <code>repeated .web.GroupDisplayInfo groups = 2;</code>
     */
    public Builder addGroups(
        com.halloapp.proto.web.GroupDisplayInfo.Builder builderForValue) {
      copyOnWrite();
      instance.addGroups(builderForValue.build());
      return this;
    }
    /**
     * <code>repeated .web.GroupDisplayInfo groups = 2;</code>
     */
    public Builder addGroups(
        int index, com.halloapp.proto.web.GroupDisplayInfo.Builder builderForValue) {
      copyOnWrite();
      instance.addGroups(index,
          builderForValue.build());
      return this;
    }
    /**
     * <code>repeated .web.GroupDisplayInfo groups = 2;</code>
     */
    public Builder addAllGroups(
        java.lang.Iterable<? extends com.halloapp.proto.web.GroupDisplayInfo> values) {
      copyOnWrite();
      instance.addAllGroups(values);
      return this;
    }
    /**
     * <code>repeated .web.GroupDisplayInfo groups = 2;</code>
     */
    public Builder clearGroups() {
      copyOnWrite();
      instance.clearGroups();
      return this;
    }
    /**
     * <code>repeated .web.GroupDisplayInfo groups = 2;</code>
     */
    public Builder removeGroups(int index) {
      copyOnWrite();
      instance.removeGroups(index);
      return this;
    }

    // @@protoc_insertion_point(builder_scope:web.GroupResponse)
  }
  @java.lang.Override
  @java.lang.SuppressWarnings({"unchecked", "fallthrough"})
  protected final java.lang.Object dynamicMethod(
      com.google.protobuf.GeneratedMessageLite.MethodToInvoke method,
      java.lang.Object arg0, java.lang.Object arg1) {
    switch (method) {
      case NEW_MUTABLE_INSTANCE: {
        return new com.halloapp.proto.web.GroupResponse();
      }
      case NEW_BUILDER: {
        return new Builder();
      }
      case BUILD_MESSAGE_INFO: {
          java.lang.Object[] objects = new java.lang.Object[] {
            "id_",
            "groups_",
            com.halloapp.proto.web.GroupDisplayInfo.class,
          };
          java.lang.String info =
              "\u0000\u0002\u0000\u0000\u0001\u0002\u0002\u0000\u0001\u0000\u0001\u0208\u0002\u001b" +
              "";
          return newMessageInfo(DEFAULT_INSTANCE, info, objects);
      }
      // fall through
      case GET_DEFAULT_INSTANCE: {
        return DEFAULT_INSTANCE;
      }
      case GET_PARSER: {
        com.google.protobuf.Parser<com.halloapp.proto.web.GroupResponse> parser = PARSER;
        if (parser == null) {
          synchronized (com.halloapp.proto.web.GroupResponse.class) {
            parser = PARSER;
            if (parser == null) {
              parser =
                  new DefaultInstanceBasedParser<com.halloapp.proto.web.GroupResponse>(
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


  // @@protoc_insertion_point(class_scope:web.GroupResponse)
  private static final com.halloapp.proto.web.GroupResponse DEFAULT_INSTANCE;
  static {
    GroupResponse defaultInstance = new GroupResponse();
    // New instances are implicitly immutable so no need to make
    // immutable.
    DEFAULT_INSTANCE = defaultInstance;
    com.google.protobuf.GeneratedMessageLite.registerDefaultInstance(
      GroupResponse.class, defaultInstance);
  }

  public static com.halloapp.proto.web.GroupResponse getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static volatile com.google.protobuf.Parser<GroupResponse> PARSER;

  public static com.google.protobuf.Parser<GroupResponse> parser() {
    return DEFAULT_INSTANCE.getParserForType();
  }
}
