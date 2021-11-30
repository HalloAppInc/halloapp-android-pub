// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: clients.proto

package com.halloapp.proto.clients;

/**
 * Protobuf type {@code clients.GroupHistoryPayload}
 */
public  final class GroupHistoryPayload extends
    com.google.protobuf.GeneratedMessageLite<
        GroupHistoryPayload, GroupHistoryPayload.Builder> implements
    // @@protoc_insertion_point(message_implements:clients.GroupHistoryPayload)
    GroupHistoryPayloadOrBuilder {
  private GroupHistoryPayload() {
    memberDetails_ = emptyProtobufList();
    contentDetails_ = emptyProtobufList();
  }
  public static final int MEMBER_DETAILS_FIELD_NUMBER = 1;
  private com.google.protobuf.Internal.ProtobufList<com.halloapp.proto.clients.MemberDetails> memberDetails_;
  /**
   * <code>repeated .clients.MemberDetails member_details = 1;</code>
   */
  @java.lang.Override
  public java.util.List<com.halloapp.proto.clients.MemberDetails> getMemberDetailsList() {
    return memberDetails_;
  }
  /**
   * <code>repeated .clients.MemberDetails member_details = 1;</code>
   */
  public java.util.List<? extends com.halloapp.proto.clients.MemberDetailsOrBuilder> 
      getMemberDetailsOrBuilderList() {
    return memberDetails_;
  }
  /**
   * <code>repeated .clients.MemberDetails member_details = 1;</code>
   */
  @java.lang.Override
  public int getMemberDetailsCount() {
    return memberDetails_.size();
  }
  /**
   * <code>repeated .clients.MemberDetails member_details = 1;</code>
   */
  @java.lang.Override
  public com.halloapp.proto.clients.MemberDetails getMemberDetails(int index) {
    return memberDetails_.get(index);
  }
  /**
   * <code>repeated .clients.MemberDetails member_details = 1;</code>
   */
  public com.halloapp.proto.clients.MemberDetailsOrBuilder getMemberDetailsOrBuilder(
      int index) {
    return memberDetails_.get(index);
  }
  private void ensureMemberDetailsIsMutable() {
    com.google.protobuf.Internal.ProtobufList<com.halloapp.proto.clients.MemberDetails> tmp = memberDetails_;
    if (!tmp.isModifiable()) {
      memberDetails_ =
          com.google.protobuf.GeneratedMessageLite.mutableCopy(tmp);
     }
  }

  /**
   * <code>repeated .clients.MemberDetails member_details = 1;</code>
   */
  private void setMemberDetails(
      int index, com.halloapp.proto.clients.MemberDetails value) {
    value.getClass();
  ensureMemberDetailsIsMutable();
    memberDetails_.set(index, value);
  }
  /**
   * <code>repeated .clients.MemberDetails member_details = 1;</code>
   */
  private void addMemberDetails(com.halloapp.proto.clients.MemberDetails value) {
    value.getClass();
  ensureMemberDetailsIsMutable();
    memberDetails_.add(value);
  }
  /**
   * <code>repeated .clients.MemberDetails member_details = 1;</code>
   */
  private void addMemberDetails(
      int index, com.halloapp.proto.clients.MemberDetails value) {
    value.getClass();
  ensureMemberDetailsIsMutable();
    memberDetails_.add(index, value);
  }
  /**
   * <code>repeated .clients.MemberDetails member_details = 1;</code>
   */
  private void addAllMemberDetails(
      java.lang.Iterable<? extends com.halloapp.proto.clients.MemberDetails> values) {
    ensureMemberDetailsIsMutable();
    com.google.protobuf.AbstractMessageLite.addAll(
        values, memberDetails_);
  }
  /**
   * <code>repeated .clients.MemberDetails member_details = 1;</code>
   */
  private void clearMemberDetails() {
    memberDetails_ = emptyProtobufList();
  }
  /**
   * <code>repeated .clients.MemberDetails member_details = 1;</code>
   */
  private void removeMemberDetails(int index) {
    ensureMemberDetailsIsMutable();
    memberDetails_.remove(index);
  }

  public static final int CONTENT_DETAILS_FIELD_NUMBER = 2;
  private com.google.protobuf.Internal.ProtobufList<com.halloapp.proto.clients.ContentDetails> contentDetails_;
  /**
   * <code>repeated .clients.ContentDetails content_details = 2;</code>
   */
  @java.lang.Override
  public java.util.List<com.halloapp.proto.clients.ContentDetails> getContentDetailsList() {
    return contentDetails_;
  }
  /**
   * <code>repeated .clients.ContentDetails content_details = 2;</code>
   */
  public java.util.List<? extends com.halloapp.proto.clients.ContentDetailsOrBuilder> 
      getContentDetailsOrBuilderList() {
    return contentDetails_;
  }
  /**
   * <code>repeated .clients.ContentDetails content_details = 2;</code>
   */
  @java.lang.Override
  public int getContentDetailsCount() {
    return contentDetails_.size();
  }
  /**
   * <code>repeated .clients.ContentDetails content_details = 2;</code>
   */
  @java.lang.Override
  public com.halloapp.proto.clients.ContentDetails getContentDetails(int index) {
    return contentDetails_.get(index);
  }
  /**
   * <code>repeated .clients.ContentDetails content_details = 2;</code>
   */
  public com.halloapp.proto.clients.ContentDetailsOrBuilder getContentDetailsOrBuilder(
      int index) {
    return contentDetails_.get(index);
  }
  private void ensureContentDetailsIsMutable() {
    com.google.protobuf.Internal.ProtobufList<com.halloapp.proto.clients.ContentDetails> tmp = contentDetails_;
    if (!tmp.isModifiable()) {
      contentDetails_ =
          com.google.protobuf.GeneratedMessageLite.mutableCopy(tmp);
     }
  }

  /**
   * <code>repeated .clients.ContentDetails content_details = 2;</code>
   */
  private void setContentDetails(
      int index, com.halloapp.proto.clients.ContentDetails value) {
    value.getClass();
  ensureContentDetailsIsMutable();
    contentDetails_.set(index, value);
  }
  /**
   * <code>repeated .clients.ContentDetails content_details = 2;</code>
   */
  private void addContentDetails(com.halloapp.proto.clients.ContentDetails value) {
    value.getClass();
  ensureContentDetailsIsMutable();
    contentDetails_.add(value);
  }
  /**
   * <code>repeated .clients.ContentDetails content_details = 2;</code>
   */
  private void addContentDetails(
      int index, com.halloapp.proto.clients.ContentDetails value) {
    value.getClass();
  ensureContentDetailsIsMutable();
    contentDetails_.add(index, value);
  }
  /**
   * <code>repeated .clients.ContentDetails content_details = 2;</code>
   */
  private void addAllContentDetails(
      java.lang.Iterable<? extends com.halloapp.proto.clients.ContentDetails> values) {
    ensureContentDetailsIsMutable();
    com.google.protobuf.AbstractMessageLite.addAll(
        values, contentDetails_);
  }
  /**
   * <code>repeated .clients.ContentDetails content_details = 2;</code>
   */
  private void clearContentDetails() {
    contentDetails_ = emptyProtobufList();
  }
  /**
   * <code>repeated .clients.ContentDetails content_details = 2;</code>
   */
  private void removeContentDetails(int index) {
    ensureContentDetailsIsMutable();
    contentDetails_.remove(index);
  }

  public static com.halloapp.proto.clients.GroupHistoryPayload parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static com.halloapp.proto.clients.GroupHistoryPayload parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static com.halloapp.proto.clients.GroupHistoryPayload parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static com.halloapp.proto.clients.GroupHistoryPayload parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static com.halloapp.proto.clients.GroupHistoryPayload parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static com.halloapp.proto.clients.GroupHistoryPayload parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static com.halloapp.proto.clients.GroupHistoryPayload parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input);
  }
  public static com.halloapp.proto.clients.GroupHistoryPayload parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input, extensionRegistry);
  }
  public static com.halloapp.proto.clients.GroupHistoryPayload parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return parseDelimitedFrom(DEFAULT_INSTANCE, input);
  }
  public static com.halloapp.proto.clients.GroupHistoryPayload parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return parseDelimitedFrom(DEFAULT_INSTANCE, input, extensionRegistry);
  }
  public static com.halloapp.proto.clients.GroupHistoryPayload parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input);
  }
  public static com.halloapp.proto.clients.GroupHistoryPayload parseFrom(
      com.google.protobuf.CodedInputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input, extensionRegistry);
  }

  public static Builder newBuilder() {
    return (Builder) DEFAULT_INSTANCE.createBuilder();
  }
  public static Builder newBuilder(com.halloapp.proto.clients.GroupHistoryPayload prototype) {
    return (Builder) DEFAULT_INSTANCE.createBuilder(prototype);
  }

  /**
   * Protobuf type {@code clients.GroupHistoryPayload}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessageLite.Builder<
        com.halloapp.proto.clients.GroupHistoryPayload, Builder> implements
      // @@protoc_insertion_point(builder_implements:clients.GroupHistoryPayload)
      com.halloapp.proto.clients.GroupHistoryPayloadOrBuilder {
    // Construct using com.halloapp.proto.clients.GroupHistoryPayload.newBuilder()
    private Builder() {
      super(DEFAULT_INSTANCE);
    }


    /**
     * <code>repeated .clients.MemberDetails member_details = 1;</code>
     */
    @java.lang.Override
    public java.util.List<com.halloapp.proto.clients.MemberDetails> getMemberDetailsList() {
      return java.util.Collections.unmodifiableList(
          instance.getMemberDetailsList());
    }
    /**
     * <code>repeated .clients.MemberDetails member_details = 1;</code>
     */
    @java.lang.Override
    public int getMemberDetailsCount() {
      return instance.getMemberDetailsCount();
    }/**
     * <code>repeated .clients.MemberDetails member_details = 1;</code>
     */
    @java.lang.Override
    public com.halloapp.proto.clients.MemberDetails getMemberDetails(int index) {
      return instance.getMemberDetails(index);
    }
    /**
     * <code>repeated .clients.MemberDetails member_details = 1;</code>
     */
    public Builder setMemberDetails(
        int index, com.halloapp.proto.clients.MemberDetails value) {
      copyOnWrite();
      instance.setMemberDetails(index, value);
      return this;
    }
    /**
     * <code>repeated .clients.MemberDetails member_details = 1;</code>
     */
    public Builder setMemberDetails(
        int index, com.halloapp.proto.clients.MemberDetails.Builder builderForValue) {
      copyOnWrite();
      instance.setMemberDetails(index,
          builderForValue.build());
      return this;
    }
    /**
     * <code>repeated .clients.MemberDetails member_details = 1;</code>
     */
    public Builder addMemberDetails(com.halloapp.proto.clients.MemberDetails value) {
      copyOnWrite();
      instance.addMemberDetails(value);
      return this;
    }
    /**
     * <code>repeated .clients.MemberDetails member_details = 1;</code>
     */
    public Builder addMemberDetails(
        int index, com.halloapp.proto.clients.MemberDetails value) {
      copyOnWrite();
      instance.addMemberDetails(index, value);
      return this;
    }
    /**
     * <code>repeated .clients.MemberDetails member_details = 1;</code>
     */
    public Builder addMemberDetails(
        com.halloapp.proto.clients.MemberDetails.Builder builderForValue) {
      copyOnWrite();
      instance.addMemberDetails(builderForValue.build());
      return this;
    }
    /**
     * <code>repeated .clients.MemberDetails member_details = 1;</code>
     */
    public Builder addMemberDetails(
        int index, com.halloapp.proto.clients.MemberDetails.Builder builderForValue) {
      copyOnWrite();
      instance.addMemberDetails(index,
          builderForValue.build());
      return this;
    }
    /**
     * <code>repeated .clients.MemberDetails member_details = 1;</code>
     */
    public Builder addAllMemberDetails(
        java.lang.Iterable<? extends com.halloapp.proto.clients.MemberDetails> values) {
      copyOnWrite();
      instance.addAllMemberDetails(values);
      return this;
    }
    /**
     * <code>repeated .clients.MemberDetails member_details = 1;</code>
     */
    public Builder clearMemberDetails() {
      copyOnWrite();
      instance.clearMemberDetails();
      return this;
    }
    /**
     * <code>repeated .clients.MemberDetails member_details = 1;</code>
     */
    public Builder removeMemberDetails(int index) {
      copyOnWrite();
      instance.removeMemberDetails(index);
      return this;
    }

    /**
     * <code>repeated .clients.ContentDetails content_details = 2;</code>
     */
    @java.lang.Override
    public java.util.List<com.halloapp.proto.clients.ContentDetails> getContentDetailsList() {
      return java.util.Collections.unmodifiableList(
          instance.getContentDetailsList());
    }
    /**
     * <code>repeated .clients.ContentDetails content_details = 2;</code>
     */
    @java.lang.Override
    public int getContentDetailsCount() {
      return instance.getContentDetailsCount();
    }/**
     * <code>repeated .clients.ContentDetails content_details = 2;</code>
     */
    @java.lang.Override
    public com.halloapp.proto.clients.ContentDetails getContentDetails(int index) {
      return instance.getContentDetails(index);
    }
    /**
     * <code>repeated .clients.ContentDetails content_details = 2;</code>
     */
    public Builder setContentDetails(
        int index, com.halloapp.proto.clients.ContentDetails value) {
      copyOnWrite();
      instance.setContentDetails(index, value);
      return this;
    }
    /**
     * <code>repeated .clients.ContentDetails content_details = 2;</code>
     */
    public Builder setContentDetails(
        int index, com.halloapp.proto.clients.ContentDetails.Builder builderForValue) {
      copyOnWrite();
      instance.setContentDetails(index,
          builderForValue.build());
      return this;
    }
    /**
     * <code>repeated .clients.ContentDetails content_details = 2;</code>
     */
    public Builder addContentDetails(com.halloapp.proto.clients.ContentDetails value) {
      copyOnWrite();
      instance.addContentDetails(value);
      return this;
    }
    /**
     * <code>repeated .clients.ContentDetails content_details = 2;</code>
     */
    public Builder addContentDetails(
        int index, com.halloapp.proto.clients.ContentDetails value) {
      copyOnWrite();
      instance.addContentDetails(index, value);
      return this;
    }
    /**
     * <code>repeated .clients.ContentDetails content_details = 2;</code>
     */
    public Builder addContentDetails(
        com.halloapp.proto.clients.ContentDetails.Builder builderForValue) {
      copyOnWrite();
      instance.addContentDetails(builderForValue.build());
      return this;
    }
    /**
     * <code>repeated .clients.ContentDetails content_details = 2;</code>
     */
    public Builder addContentDetails(
        int index, com.halloapp.proto.clients.ContentDetails.Builder builderForValue) {
      copyOnWrite();
      instance.addContentDetails(index,
          builderForValue.build());
      return this;
    }
    /**
     * <code>repeated .clients.ContentDetails content_details = 2;</code>
     */
    public Builder addAllContentDetails(
        java.lang.Iterable<? extends com.halloapp.proto.clients.ContentDetails> values) {
      copyOnWrite();
      instance.addAllContentDetails(values);
      return this;
    }
    /**
     * <code>repeated .clients.ContentDetails content_details = 2;</code>
     */
    public Builder clearContentDetails() {
      copyOnWrite();
      instance.clearContentDetails();
      return this;
    }
    /**
     * <code>repeated .clients.ContentDetails content_details = 2;</code>
     */
    public Builder removeContentDetails(int index) {
      copyOnWrite();
      instance.removeContentDetails(index);
      return this;
    }

    // @@protoc_insertion_point(builder_scope:clients.GroupHistoryPayload)
  }
  @java.lang.Override
  @java.lang.SuppressWarnings({"unchecked", "fallthrough"})
  protected final java.lang.Object dynamicMethod(
      com.google.protobuf.GeneratedMessageLite.MethodToInvoke method,
      java.lang.Object arg0, java.lang.Object arg1) {
    switch (method) {
      case NEW_MUTABLE_INSTANCE: {
        return new com.halloapp.proto.clients.GroupHistoryPayload();
      }
      case NEW_BUILDER: {
        return new Builder();
      }
      case BUILD_MESSAGE_INFO: {
          java.lang.Object[] objects = new java.lang.Object[] {
            "memberDetails_",
            com.halloapp.proto.clients.MemberDetails.class,
            "contentDetails_",
            com.halloapp.proto.clients.ContentDetails.class,
          };
          java.lang.String info =
              "\u0000\u0002\u0000\u0000\u0001\u0002\u0002\u0000\u0002\u0000\u0001\u001b\u0002\u001b" +
              "";
          return newMessageInfo(DEFAULT_INSTANCE, info, objects);
      }
      // fall through
      case GET_DEFAULT_INSTANCE: {
        return DEFAULT_INSTANCE;
      }
      case GET_PARSER: {
        com.google.protobuf.Parser<com.halloapp.proto.clients.GroupHistoryPayload> parser = PARSER;
        if (parser == null) {
          synchronized (com.halloapp.proto.clients.GroupHistoryPayload.class) {
            parser = PARSER;
            if (parser == null) {
              parser =
                  new DefaultInstanceBasedParser<com.halloapp.proto.clients.GroupHistoryPayload>(
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


  // @@protoc_insertion_point(class_scope:clients.GroupHistoryPayload)
  private static final com.halloapp.proto.clients.GroupHistoryPayload DEFAULT_INSTANCE;
  static {
    GroupHistoryPayload defaultInstance = new GroupHistoryPayload();
    // New instances are implicitly immutable so no need to make
    // immutable.
    DEFAULT_INSTANCE = defaultInstance;
    com.google.protobuf.GeneratedMessageLite.registerDefaultInstance(
      GroupHistoryPayload.class, defaultInstance);
  }

  public static com.halloapp.proto.clients.GroupHistoryPayload getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static volatile com.google.protobuf.Parser<GroupHistoryPayload> PARSER;

  public static com.google.protobuf.Parser<GroupHistoryPayload> parser() {
    return DEFAULT_INSTANCE.getParserForType();
  }
}

