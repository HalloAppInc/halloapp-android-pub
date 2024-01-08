// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: clients.proto

package com.halloapp.proto.clients;

/**
 * Protobuf type {@code clients.Text}
 */
public  final class Text extends
    com.google.protobuf.GeneratedMessageLite<
        Text, Text.Builder> implements
    // @@protoc_insertion_point(message_implements:clients.Text)
    TextOrBuilder {
  private Text() {
    text_ = "";
    mentions_ = emptyProtobufList();
  }
  public static final int TEXT_FIELD_NUMBER = 1;
  private java.lang.String text_;
  /**
   * <code>string text = 1;</code>
   * @return The text.
   */
  @java.lang.Override
  public java.lang.String getText() {
    return text_;
  }
  /**
   * <code>string text = 1;</code>
   * @return The bytes for text.
   */
  @java.lang.Override
  public com.google.protobuf.ByteString
      getTextBytes() {
    return com.google.protobuf.ByteString.copyFromUtf8(text_);
  }
  /**
   * <code>string text = 1;</code>
   * @param value The text to set.
   */
  private void setText(
      java.lang.String value) {
    java.lang.Class<?> valueClass = value.getClass();
  
    text_ = value;
  }
  /**
   * <code>string text = 1;</code>
   */
  private void clearText() {
    
    text_ = getDefaultInstance().getText();
  }
  /**
   * <code>string text = 1;</code>
   * @param value The bytes for text to set.
   */
  private void setTextBytes(
      com.google.protobuf.ByteString value) {
    checkByteStringIsUtf8(value);
    text_ = value.toStringUtf8();
    
  }

  public static final int MENTIONS_FIELD_NUMBER = 2;
  private com.google.protobuf.Internal.ProtobufList<com.halloapp.proto.clients.Mention> mentions_;
  /**
   * <code>repeated .clients.Mention mentions = 2;</code>
   */
  @java.lang.Override
  public java.util.List<com.halloapp.proto.clients.Mention> getMentionsList() {
    return mentions_;
  }
  /**
   * <code>repeated .clients.Mention mentions = 2;</code>
   */
  public java.util.List<? extends com.halloapp.proto.clients.MentionOrBuilder> 
      getMentionsOrBuilderList() {
    return mentions_;
  }
  /**
   * <code>repeated .clients.Mention mentions = 2;</code>
   */
  @java.lang.Override
  public int getMentionsCount() {
    return mentions_.size();
  }
  /**
   * <code>repeated .clients.Mention mentions = 2;</code>
   */
  @java.lang.Override
  public com.halloapp.proto.clients.Mention getMentions(int index) {
    return mentions_.get(index);
  }
  /**
   * <code>repeated .clients.Mention mentions = 2;</code>
   */
  public com.halloapp.proto.clients.MentionOrBuilder getMentionsOrBuilder(
      int index) {
    return mentions_.get(index);
  }
  private void ensureMentionsIsMutable() {
    com.google.protobuf.Internal.ProtobufList<com.halloapp.proto.clients.Mention> tmp = mentions_;
    if (!tmp.isModifiable()) {
      mentions_ =
          com.google.protobuf.GeneratedMessageLite.mutableCopy(tmp);
     }
  }

  /**
   * <code>repeated .clients.Mention mentions = 2;</code>
   */
  private void setMentions(
      int index, com.halloapp.proto.clients.Mention value) {
    value.getClass();
  ensureMentionsIsMutable();
    mentions_.set(index, value);
  }
  /**
   * <code>repeated .clients.Mention mentions = 2;</code>
   */
  private void addMentions(com.halloapp.proto.clients.Mention value) {
    value.getClass();
  ensureMentionsIsMutable();
    mentions_.add(value);
  }
  /**
   * <code>repeated .clients.Mention mentions = 2;</code>
   */
  private void addMentions(
      int index, com.halloapp.proto.clients.Mention value) {
    value.getClass();
  ensureMentionsIsMutable();
    mentions_.add(index, value);
  }
  /**
   * <code>repeated .clients.Mention mentions = 2;</code>
   */
  private void addAllMentions(
      java.lang.Iterable<? extends com.halloapp.proto.clients.Mention> values) {
    ensureMentionsIsMutable();
    com.google.protobuf.AbstractMessageLite.addAll(
        values, mentions_);
  }
  /**
   * <code>repeated .clients.Mention mentions = 2;</code>
   */
  private void clearMentions() {
    mentions_ = emptyProtobufList();
  }
  /**
   * <code>repeated .clients.Mention mentions = 2;</code>
   */
  private void removeMentions(int index) {
    ensureMentionsIsMutable();
    mentions_.remove(index);
  }

  public static final int LINK_FIELD_NUMBER = 3;
  private com.halloapp.proto.clients.Link link_;
  /**
   * <code>.clients.Link link = 3;</code>
   */
  @java.lang.Override
  public boolean hasLink() {
    return link_ != null;
  }
  /**
   * <code>.clients.Link link = 3;</code>
   */
  @java.lang.Override
  public com.halloapp.proto.clients.Link getLink() {
    return link_ == null ? com.halloapp.proto.clients.Link.getDefaultInstance() : link_;
  }
  /**
   * <code>.clients.Link link = 3;</code>
   */
  private void setLink(com.halloapp.proto.clients.Link value) {
    value.getClass();
  link_ = value;
    
    }
  /**
   * <code>.clients.Link link = 3;</code>
   */
  @java.lang.SuppressWarnings({"ReferenceEquality"})
  private void mergeLink(com.halloapp.proto.clients.Link value) {
    value.getClass();
  if (link_ != null &&
        link_ != com.halloapp.proto.clients.Link.getDefaultInstance()) {
      link_ =
        com.halloapp.proto.clients.Link.newBuilder(link_).mergeFrom(value).buildPartial();
    } else {
      link_ = value;
    }
    
  }
  /**
   * <code>.clients.Link link = 3;</code>
   */
  private void clearLink() {  link_ = null;
    
  }

  public static com.halloapp.proto.clients.Text parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static com.halloapp.proto.clients.Text parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static com.halloapp.proto.clients.Text parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static com.halloapp.proto.clients.Text parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static com.halloapp.proto.clients.Text parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static com.halloapp.proto.clients.Text parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static com.halloapp.proto.clients.Text parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input);
  }
  public static com.halloapp.proto.clients.Text parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input, extensionRegistry);
  }
  public static com.halloapp.proto.clients.Text parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return parseDelimitedFrom(DEFAULT_INSTANCE, input);
  }
  public static com.halloapp.proto.clients.Text parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return parseDelimitedFrom(DEFAULT_INSTANCE, input, extensionRegistry);
  }
  public static com.halloapp.proto.clients.Text parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input);
  }
  public static com.halloapp.proto.clients.Text parseFrom(
      com.google.protobuf.CodedInputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input, extensionRegistry);
  }

  public static Builder newBuilder() {
    return (Builder) DEFAULT_INSTANCE.createBuilder();
  }
  public static Builder newBuilder(com.halloapp.proto.clients.Text prototype) {
    return (Builder) DEFAULT_INSTANCE.createBuilder(prototype);
  }

  /**
   * Protobuf type {@code clients.Text}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessageLite.Builder<
        com.halloapp.proto.clients.Text, Builder> implements
      // @@protoc_insertion_point(builder_implements:clients.Text)
      com.halloapp.proto.clients.TextOrBuilder {
    // Construct using com.halloapp.proto.clients.Text.newBuilder()
    private Builder() {
      super(DEFAULT_INSTANCE);
    }


    /**
     * <code>string text = 1;</code>
     * @return The text.
     */
    @java.lang.Override
    public java.lang.String getText() {
      return instance.getText();
    }
    /**
     * <code>string text = 1;</code>
     * @return The bytes for text.
     */
    @java.lang.Override
    public com.google.protobuf.ByteString
        getTextBytes() {
      return instance.getTextBytes();
    }
    /**
     * <code>string text = 1;</code>
     * @param value The text to set.
     * @return This builder for chaining.
     */
    public Builder setText(
        java.lang.String value) {
      copyOnWrite();
      instance.setText(value);
      return this;
    }
    /**
     * <code>string text = 1;</code>
     * @return This builder for chaining.
     */
    public Builder clearText() {
      copyOnWrite();
      instance.clearText();
      return this;
    }
    /**
     * <code>string text = 1;</code>
     * @param value The bytes for text to set.
     * @return This builder for chaining.
     */
    public Builder setTextBytes(
        com.google.protobuf.ByteString value) {
      copyOnWrite();
      instance.setTextBytes(value);
      return this;
    }

    /**
     * <code>repeated .clients.Mention mentions = 2;</code>
     */
    @java.lang.Override
    public java.util.List<com.halloapp.proto.clients.Mention> getMentionsList() {
      return java.util.Collections.unmodifiableList(
          instance.getMentionsList());
    }
    /**
     * <code>repeated .clients.Mention mentions = 2;</code>
     */
    @java.lang.Override
    public int getMentionsCount() {
      return instance.getMentionsCount();
    }/**
     * <code>repeated .clients.Mention mentions = 2;</code>
     */
    @java.lang.Override
    public com.halloapp.proto.clients.Mention getMentions(int index) {
      return instance.getMentions(index);
    }
    /**
     * <code>repeated .clients.Mention mentions = 2;</code>
     */
    public Builder setMentions(
        int index, com.halloapp.proto.clients.Mention value) {
      copyOnWrite();
      instance.setMentions(index, value);
      return this;
    }
    /**
     * <code>repeated .clients.Mention mentions = 2;</code>
     */
    public Builder setMentions(
        int index, com.halloapp.proto.clients.Mention.Builder builderForValue) {
      copyOnWrite();
      instance.setMentions(index,
          builderForValue.build());
      return this;
    }
    /**
     * <code>repeated .clients.Mention mentions = 2;</code>
     */
    public Builder addMentions(com.halloapp.proto.clients.Mention value) {
      copyOnWrite();
      instance.addMentions(value);
      return this;
    }
    /**
     * <code>repeated .clients.Mention mentions = 2;</code>
     */
    public Builder addMentions(
        int index, com.halloapp.proto.clients.Mention value) {
      copyOnWrite();
      instance.addMentions(index, value);
      return this;
    }
    /**
     * <code>repeated .clients.Mention mentions = 2;</code>
     */
    public Builder addMentions(
        com.halloapp.proto.clients.Mention.Builder builderForValue) {
      copyOnWrite();
      instance.addMentions(builderForValue.build());
      return this;
    }
    /**
     * <code>repeated .clients.Mention mentions = 2;</code>
     */
    public Builder addMentions(
        int index, com.halloapp.proto.clients.Mention.Builder builderForValue) {
      copyOnWrite();
      instance.addMentions(index,
          builderForValue.build());
      return this;
    }
    /**
     * <code>repeated .clients.Mention mentions = 2;</code>
     */
    public Builder addAllMentions(
        java.lang.Iterable<? extends com.halloapp.proto.clients.Mention> values) {
      copyOnWrite();
      instance.addAllMentions(values);
      return this;
    }
    /**
     * <code>repeated .clients.Mention mentions = 2;</code>
     */
    public Builder clearMentions() {
      copyOnWrite();
      instance.clearMentions();
      return this;
    }
    /**
     * <code>repeated .clients.Mention mentions = 2;</code>
     */
    public Builder removeMentions(int index) {
      copyOnWrite();
      instance.removeMentions(index);
      return this;
    }

    /**
     * <code>.clients.Link link = 3;</code>
     */
    @java.lang.Override
    public boolean hasLink() {
      return instance.hasLink();
    }
    /**
     * <code>.clients.Link link = 3;</code>
     */
    @java.lang.Override
    public com.halloapp.proto.clients.Link getLink() {
      return instance.getLink();
    }
    /**
     * <code>.clients.Link link = 3;</code>
     */
    public Builder setLink(com.halloapp.proto.clients.Link value) {
      copyOnWrite();
      instance.setLink(value);
      return this;
      }
    /**
     * <code>.clients.Link link = 3;</code>
     */
    public Builder setLink(
        com.halloapp.proto.clients.Link.Builder builderForValue) {
      copyOnWrite();
      instance.setLink(builderForValue.build());
      return this;
    }
    /**
     * <code>.clients.Link link = 3;</code>
     */
    public Builder mergeLink(com.halloapp.proto.clients.Link value) {
      copyOnWrite();
      instance.mergeLink(value);
      return this;
    }
    /**
     * <code>.clients.Link link = 3;</code>
     */
    public Builder clearLink() {  copyOnWrite();
      instance.clearLink();
      return this;
    }

    // @@protoc_insertion_point(builder_scope:clients.Text)
  }
  @java.lang.Override
  @java.lang.SuppressWarnings({"unchecked", "fallthrough"})
  protected final java.lang.Object dynamicMethod(
      com.google.protobuf.GeneratedMessageLite.MethodToInvoke method,
      java.lang.Object arg0, java.lang.Object arg1) {
    switch (method) {
      case NEW_MUTABLE_INSTANCE: {
        return new com.halloapp.proto.clients.Text();
      }
      case NEW_BUILDER: {
        return new Builder();
      }
      case BUILD_MESSAGE_INFO: {
          java.lang.Object[] objects = new java.lang.Object[] {
            "text_",
            "mentions_",
            com.halloapp.proto.clients.Mention.class,
            "link_",
          };
          java.lang.String info =
              "\u0000\u0003\u0000\u0000\u0001\u0003\u0003\u0000\u0001\u0000\u0001\u0208\u0002\u001b" +
              "\u0003\t";
          return newMessageInfo(DEFAULT_INSTANCE, info, objects);
      }
      // fall through
      case GET_DEFAULT_INSTANCE: {
        return DEFAULT_INSTANCE;
      }
      case GET_PARSER: {
        com.google.protobuf.Parser<com.halloapp.proto.clients.Text> parser = PARSER;
        if (parser == null) {
          synchronized (com.halloapp.proto.clients.Text.class) {
            parser = PARSER;
            if (parser == null) {
              parser =
                  new DefaultInstanceBasedParser<com.halloapp.proto.clients.Text>(
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


  // @@protoc_insertion_point(class_scope:clients.Text)
  private static final com.halloapp.proto.clients.Text DEFAULT_INSTANCE;
  static {
    Text defaultInstance = new Text();
    // New instances are implicitly immutable so no need to make
    // immutable.
    DEFAULT_INSTANCE = defaultInstance;
    com.google.protobuf.GeneratedMessageLite.registerDefaultInstance(
      Text.class, defaultInstance);
  }

  public static com.halloapp.proto.clients.Text getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static volatile com.google.protobuf.Parser<Text> PARSER;

  public static com.google.protobuf.Parser<Text> parser() {
    return DEFAULT_INSTANCE.getParserForType();
  }
}

