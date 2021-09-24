// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: server.proto

package com.halloapp.proto.server;

/**
 * Protobuf type {@code server.FeedItem}
 */
public  final class FeedItem extends
    com.google.protobuf.GeneratedMessageLite<
        FeedItem, FeedItem.Builder> implements
    // @@protoc_insertion_point(message_implements:server.FeedItem)
    FeedItemOrBuilder {
  private FeedItem() {
    shareStanzas_ = emptyProtobufList();
    senderStateBundles_ = emptyProtobufList();
  }
  /**
   * Protobuf enum {@code server.FeedItem.Action}
   */
  public enum Action
      implements com.google.protobuf.Internal.EnumLite {
    /**
     * <code>PUBLISH = 0;</code>
     */
    PUBLISH(0),
    /**
     * <code>RETRACT = 1;</code>
     */
    RETRACT(1),
    /**
     * <code>SHARE = 2;</code>
     */
    SHARE(2),
    UNRECOGNIZED(-1),
    ;

    /**
     * <code>PUBLISH = 0;</code>
     */
    public static final int PUBLISH_VALUE = 0;
    /**
     * <code>RETRACT = 1;</code>
     */
    public static final int RETRACT_VALUE = 1;
    /**
     * <code>SHARE = 2;</code>
     */
    public static final int SHARE_VALUE = 2;


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
    public static Action valueOf(int value) {
      return forNumber(value);
    }

    public static Action forNumber(int value) {
      switch (value) {
        case 0: return PUBLISH;
        case 1: return RETRACT;
        case 2: return SHARE;
        default: return null;
      }
    }

    public static com.google.protobuf.Internal.EnumLiteMap<Action>
        internalGetValueMap() {
      return internalValueMap;
    }
    private static final com.google.protobuf.Internal.EnumLiteMap<
        Action> internalValueMap =
          new com.google.protobuf.Internal.EnumLiteMap<Action>() {
            @java.lang.Override
            public Action findValueByNumber(int number) {
              return Action.forNumber(number);
            }
          };

    public static com.google.protobuf.Internal.EnumVerifier 
        internalGetVerifier() {
      return ActionVerifier.INSTANCE;
    }

    private static final class ActionVerifier implements 
         com.google.protobuf.Internal.EnumVerifier { 
            static final com.google.protobuf.Internal.EnumVerifier           INSTANCE = new ActionVerifier();
            @java.lang.Override
            public boolean isInRange(int number) {
              return Action.forNumber(number) != null;
            }
          };

    private final int value;

    private Action(int value) {
      this.value = value;
    }

    // @@protoc_insertion_point(enum_scope:server.FeedItem.Action)
  }

  private int itemCase_ = 0;
  private java.lang.Object item_;
  public enum ItemCase {
    POST(2),
    COMMENT(3),
    ITEM_NOT_SET(0);
    private final int value;
    private ItemCase(int value) {
      this.value = value;
    }
    /**
     * @deprecated Use {@link #forNumber(int)} instead.
     */
    @java.lang.Deprecated
    public static ItemCase valueOf(int value) {
      return forNumber(value);
    }

    public static ItemCase forNumber(int value) {
      switch (value) {
        case 2: return POST;
        case 3: return COMMENT;
        case 0: return ITEM_NOT_SET;
        default: return null;
      }
    }
    public int getNumber() {
      return this.value;
    }
  };

  @java.lang.Override
  public ItemCase
  getItemCase() {
    return ItemCase.forNumber(
        itemCase_);
  }

  private void clearItem() {
    itemCase_ = 0;
    item_ = null;
  }

  public static final int ACTION_FIELD_NUMBER = 1;
  private int action_;
  /**
   * <code>.server.FeedItem.Action action = 1;</code>
   * @return The enum numeric value on the wire for action.
   */
  @java.lang.Override
  public int getActionValue() {
    return action_;
  }
  /**
   * <code>.server.FeedItem.Action action = 1;</code>
   * @return The action.
   */
  @java.lang.Override
  public com.halloapp.proto.server.FeedItem.Action getAction() {
    com.halloapp.proto.server.FeedItem.Action result = com.halloapp.proto.server.FeedItem.Action.forNumber(action_);
    return result == null ? com.halloapp.proto.server.FeedItem.Action.UNRECOGNIZED : result;
  }
  /**
   * <code>.server.FeedItem.Action action = 1;</code>
   * @param value The enum numeric value on the wire for action to set.
   */
  private void setActionValue(int value) {
      action_ = value;
  }
  /**
   * <code>.server.FeedItem.Action action = 1;</code>
   * @param value The action to set.
   */
  private void setAction(com.halloapp.proto.server.FeedItem.Action value) {
    action_ = value.getNumber();
    
  }
  /**
   * <code>.server.FeedItem.Action action = 1;</code>
   */
  private void clearAction() {
    
    action_ = 0;
  }

  public static final int POST_FIELD_NUMBER = 2;
  /**
   * <code>.server.Post post = 2;</code>
   */
  @java.lang.Override
  public boolean hasPost() {
    return itemCase_ == 2;
  }
  /**
   * <code>.server.Post post = 2;</code>
   */
  @java.lang.Override
  public com.halloapp.proto.server.Post getPost() {
    if (itemCase_ == 2) {
       return (com.halloapp.proto.server.Post) item_;
    }
    return com.halloapp.proto.server.Post.getDefaultInstance();
  }
  /**
   * <code>.server.Post post = 2;</code>
   */
  private void setPost(com.halloapp.proto.server.Post value) {
    value.getClass();
  item_ = value;
    itemCase_ = 2;
  }
  /**
   * <code>.server.Post post = 2;</code>
   */
  private void mergePost(com.halloapp.proto.server.Post value) {
    value.getClass();
  if (itemCase_ == 2 &&
        item_ != com.halloapp.proto.server.Post.getDefaultInstance()) {
      item_ = com.halloapp.proto.server.Post.newBuilder((com.halloapp.proto.server.Post) item_)
          .mergeFrom(value).buildPartial();
    } else {
      item_ = value;
    }
    itemCase_ = 2;
  }
  /**
   * <code>.server.Post post = 2;</code>
   */
  private void clearPost() {
    if (itemCase_ == 2) {
      itemCase_ = 0;
      item_ = null;
    }
  }

  public static final int COMMENT_FIELD_NUMBER = 3;
  /**
   * <code>.server.Comment comment = 3;</code>
   */
  @java.lang.Override
  public boolean hasComment() {
    return itemCase_ == 3;
  }
  /**
   * <code>.server.Comment comment = 3;</code>
   */
  @java.lang.Override
  public com.halloapp.proto.server.Comment getComment() {
    if (itemCase_ == 3) {
       return (com.halloapp.proto.server.Comment) item_;
    }
    return com.halloapp.proto.server.Comment.getDefaultInstance();
  }
  /**
   * <code>.server.Comment comment = 3;</code>
   */
  private void setComment(com.halloapp.proto.server.Comment value) {
    value.getClass();
  item_ = value;
    itemCase_ = 3;
  }
  /**
   * <code>.server.Comment comment = 3;</code>
   */
  private void mergeComment(com.halloapp.proto.server.Comment value) {
    value.getClass();
  if (itemCase_ == 3 &&
        item_ != com.halloapp.proto.server.Comment.getDefaultInstance()) {
      item_ = com.halloapp.proto.server.Comment.newBuilder((com.halloapp.proto.server.Comment) item_)
          .mergeFrom(value).buildPartial();
    } else {
      item_ = value;
    }
    itemCase_ = 3;
  }
  /**
   * <code>.server.Comment comment = 3;</code>
   */
  private void clearComment() {
    if (itemCase_ == 3) {
      itemCase_ = 0;
      item_ = null;
    }
  }

  public static final int SHARE_STANZAS_FIELD_NUMBER = 4;
  private com.google.protobuf.Internal.ProtobufList<com.halloapp.proto.server.ShareStanza> shareStanzas_;
  /**
   * <code>repeated .server.ShareStanza share_stanzas = 4;</code>
   */
  @java.lang.Override
  public java.util.List<com.halloapp.proto.server.ShareStanza> getShareStanzasList() {
    return shareStanzas_;
  }
  /**
   * <code>repeated .server.ShareStanza share_stanzas = 4;</code>
   */
  public java.util.List<? extends com.halloapp.proto.server.ShareStanzaOrBuilder> 
      getShareStanzasOrBuilderList() {
    return shareStanzas_;
  }
  /**
   * <code>repeated .server.ShareStanza share_stanzas = 4;</code>
   */
  @java.lang.Override
  public int getShareStanzasCount() {
    return shareStanzas_.size();
  }
  /**
   * <code>repeated .server.ShareStanza share_stanzas = 4;</code>
   */
  @java.lang.Override
  public com.halloapp.proto.server.ShareStanza getShareStanzas(int index) {
    return shareStanzas_.get(index);
  }
  /**
   * <code>repeated .server.ShareStanza share_stanzas = 4;</code>
   */
  public com.halloapp.proto.server.ShareStanzaOrBuilder getShareStanzasOrBuilder(
      int index) {
    return shareStanzas_.get(index);
  }
  private void ensureShareStanzasIsMutable() {
    com.google.protobuf.Internal.ProtobufList<com.halloapp.proto.server.ShareStanza> tmp = shareStanzas_;
    if (!tmp.isModifiable()) {
      shareStanzas_ =
          com.google.protobuf.GeneratedMessageLite.mutableCopy(tmp);
     }
  }

  /**
   * <code>repeated .server.ShareStanza share_stanzas = 4;</code>
   */
  private void setShareStanzas(
      int index, com.halloapp.proto.server.ShareStanza value) {
    value.getClass();
  ensureShareStanzasIsMutable();
    shareStanzas_.set(index, value);
  }
  /**
   * <code>repeated .server.ShareStanza share_stanzas = 4;</code>
   */
  private void addShareStanzas(com.halloapp.proto.server.ShareStanza value) {
    value.getClass();
  ensureShareStanzasIsMutable();
    shareStanzas_.add(value);
  }
  /**
   * <code>repeated .server.ShareStanza share_stanzas = 4;</code>
   */
  private void addShareStanzas(
      int index, com.halloapp.proto.server.ShareStanza value) {
    value.getClass();
  ensureShareStanzasIsMutable();
    shareStanzas_.add(index, value);
  }
  /**
   * <code>repeated .server.ShareStanza share_stanzas = 4;</code>
   */
  private void addAllShareStanzas(
      java.lang.Iterable<? extends com.halloapp.proto.server.ShareStanza> values) {
    ensureShareStanzasIsMutable();
    com.google.protobuf.AbstractMessageLite.addAll(
        values, shareStanzas_);
  }
  /**
   * <code>repeated .server.ShareStanza share_stanzas = 4;</code>
   */
  private void clearShareStanzas() {
    shareStanzas_ = emptyProtobufList();
  }
  /**
   * <code>repeated .server.ShareStanza share_stanzas = 4;</code>
   */
  private void removeShareStanzas(int index) {
    ensureShareStanzasIsMutable();
    shareStanzas_.remove(index);
  }

  public static final int SENDER_STATE_BUNDLES_FIELD_NUMBER = 7;
  private com.google.protobuf.Internal.ProtobufList<com.halloapp.proto.server.SenderStateBundle> senderStateBundles_;
  /**
   * <pre>
   * Sent by the publisher.
   * </pre>
   *
   * <code>repeated .server.SenderStateBundle sender_state_bundles = 7;</code>
   */
  @java.lang.Override
  public java.util.List<com.halloapp.proto.server.SenderStateBundle> getSenderStateBundlesList() {
    return senderStateBundles_;
  }
  /**
   * <pre>
   * Sent by the publisher.
   * </pre>
   *
   * <code>repeated .server.SenderStateBundle sender_state_bundles = 7;</code>
   */
  public java.util.List<? extends com.halloapp.proto.server.SenderStateBundleOrBuilder> 
      getSenderStateBundlesOrBuilderList() {
    return senderStateBundles_;
  }
  /**
   * <pre>
   * Sent by the publisher.
   * </pre>
   *
   * <code>repeated .server.SenderStateBundle sender_state_bundles = 7;</code>
   */
  @java.lang.Override
  public int getSenderStateBundlesCount() {
    return senderStateBundles_.size();
  }
  /**
   * <pre>
   * Sent by the publisher.
   * </pre>
   *
   * <code>repeated .server.SenderStateBundle sender_state_bundles = 7;</code>
   */
  @java.lang.Override
  public com.halloapp.proto.server.SenderStateBundle getSenderStateBundles(int index) {
    return senderStateBundles_.get(index);
  }
  /**
   * <pre>
   * Sent by the publisher.
   * </pre>
   *
   * <code>repeated .server.SenderStateBundle sender_state_bundles = 7;</code>
   */
  public com.halloapp.proto.server.SenderStateBundleOrBuilder getSenderStateBundlesOrBuilder(
      int index) {
    return senderStateBundles_.get(index);
  }
  private void ensureSenderStateBundlesIsMutable() {
    com.google.protobuf.Internal.ProtobufList<com.halloapp.proto.server.SenderStateBundle> tmp = senderStateBundles_;
    if (!tmp.isModifiable()) {
      senderStateBundles_ =
          com.google.protobuf.GeneratedMessageLite.mutableCopy(tmp);
     }
  }

  /**
   * <pre>
   * Sent by the publisher.
   * </pre>
   *
   * <code>repeated .server.SenderStateBundle sender_state_bundles = 7;</code>
   */
  private void setSenderStateBundles(
      int index, com.halloapp.proto.server.SenderStateBundle value) {
    value.getClass();
  ensureSenderStateBundlesIsMutable();
    senderStateBundles_.set(index, value);
  }
  /**
   * <pre>
   * Sent by the publisher.
   * </pre>
   *
   * <code>repeated .server.SenderStateBundle sender_state_bundles = 7;</code>
   */
  private void addSenderStateBundles(com.halloapp.proto.server.SenderStateBundle value) {
    value.getClass();
  ensureSenderStateBundlesIsMutable();
    senderStateBundles_.add(value);
  }
  /**
   * <pre>
   * Sent by the publisher.
   * </pre>
   *
   * <code>repeated .server.SenderStateBundle sender_state_bundles = 7;</code>
   */
  private void addSenderStateBundles(
      int index, com.halloapp.proto.server.SenderStateBundle value) {
    value.getClass();
  ensureSenderStateBundlesIsMutable();
    senderStateBundles_.add(index, value);
  }
  /**
   * <pre>
   * Sent by the publisher.
   * </pre>
   *
   * <code>repeated .server.SenderStateBundle sender_state_bundles = 7;</code>
   */
  private void addAllSenderStateBundles(
      java.lang.Iterable<? extends com.halloapp.proto.server.SenderStateBundle> values) {
    ensureSenderStateBundlesIsMutable();
    com.google.protobuf.AbstractMessageLite.addAll(
        values, senderStateBundles_);
  }
  /**
   * <pre>
   * Sent by the publisher.
   * </pre>
   *
   * <code>repeated .server.SenderStateBundle sender_state_bundles = 7;</code>
   */
  private void clearSenderStateBundles() {
    senderStateBundles_ = emptyProtobufList();
  }
  /**
   * <pre>
   * Sent by the publisher.
   * </pre>
   *
   * <code>repeated .server.SenderStateBundle sender_state_bundles = 7;</code>
   */
  private void removeSenderStateBundles(int index) {
    ensureSenderStateBundlesIsMutable();
    senderStateBundles_.remove(index);
  }

  public static final int SENDER_STATE_FIELD_NUMBER = 8;
  private com.halloapp.proto.server.SenderStateWithKeyInfo senderState_;
  /**
   * <pre>
   * Meant for the receiver, computed by the server using `sender_state_bundles`.
   * </pre>
   *
   * <code>.server.SenderStateWithKeyInfo sender_state = 8;</code>
   */
  @java.lang.Override
  public boolean hasSenderState() {
    return senderState_ != null;
  }
  /**
   * <pre>
   * Meant for the receiver, computed by the server using `sender_state_bundles`.
   * </pre>
   *
   * <code>.server.SenderStateWithKeyInfo sender_state = 8;</code>
   */
  @java.lang.Override
  public com.halloapp.proto.server.SenderStateWithKeyInfo getSenderState() {
    return senderState_ == null ? com.halloapp.proto.server.SenderStateWithKeyInfo.getDefaultInstance() : senderState_;
  }
  /**
   * <pre>
   * Meant for the receiver, computed by the server using `sender_state_bundles`.
   * </pre>
   *
   * <code>.server.SenderStateWithKeyInfo sender_state = 8;</code>
   */
  private void setSenderState(com.halloapp.proto.server.SenderStateWithKeyInfo value) {
    value.getClass();
  senderState_ = value;
    
    }
  /**
   * <pre>
   * Meant for the receiver, computed by the server using `sender_state_bundles`.
   * </pre>
   *
   * <code>.server.SenderStateWithKeyInfo sender_state = 8;</code>
   */
  @java.lang.SuppressWarnings({"ReferenceEquality"})
  private void mergeSenderState(com.halloapp.proto.server.SenderStateWithKeyInfo value) {
    value.getClass();
  if (senderState_ != null &&
        senderState_ != com.halloapp.proto.server.SenderStateWithKeyInfo.getDefaultInstance()) {
      senderState_ =
        com.halloapp.proto.server.SenderStateWithKeyInfo.newBuilder(senderState_).mergeFrom(value).buildPartial();
    } else {
      senderState_ = value;
    }
    
  }
  /**
   * <pre>
   * Meant for the receiver, computed by the server using `sender_state_bundles`.
   * </pre>
   *
   * <code>.server.SenderStateWithKeyInfo sender_state = 8;</code>
   */
  private void clearSenderState() {  senderState_ = null;
    
  }

  public static com.halloapp.proto.server.FeedItem parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static com.halloapp.proto.server.FeedItem parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static com.halloapp.proto.server.FeedItem parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static com.halloapp.proto.server.FeedItem parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static com.halloapp.proto.server.FeedItem parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static com.halloapp.proto.server.FeedItem parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static com.halloapp.proto.server.FeedItem parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input);
  }
  public static com.halloapp.proto.server.FeedItem parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input, extensionRegistry);
  }
  public static com.halloapp.proto.server.FeedItem parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return parseDelimitedFrom(DEFAULT_INSTANCE, input);
  }
  public static com.halloapp.proto.server.FeedItem parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return parseDelimitedFrom(DEFAULT_INSTANCE, input, extensionRegistry);
  }
  public static com.halloapp.proto.server.FeedItem parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input);
  }
  public static com.halloapp.proto.server.FeedItem parseFrom(
      com.google.protobuf.CodedInputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input, extensionRegistry);
  }

  public static Builder newBuilder() {
    return (Builder) DEFAULT_INSTANCE.createBuilder();
  }
  public static Builder newBuilder(com.halloapp.proto.server.FeedItem prototype) {
    return (Builder) DEFAULT_INSTANCE.createBuilder(prototype);
  }

  /**
   * Protobuf type {@code server.FeedItem}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessageLite.Builder<
        com.halloapp.proto.server.FeedItem, Builder> implements
      // @@protoc_insertion_point(builder_implements:server.FeedItem)
      com.halloapp.proto.server.FeedItemOrBuilder {
    // Construct using com.halloapp.proto.server.FeedItem.newBuilder()
    private Builder() {
      super(DEFAULT_INSTANCE);
    }

    @java.lang.Override
    public ItemCase
        getItemCase() {
      return instance.getItemCase();
    }

    public Builder clearItem() {
      copyOnWrite();
      instance.clearItem();
      return this;
    }


    /**
     * <code>.server.FeedItem.Action action = 1;</code>
     * @return The enum numeric value on the wire for action.
     */
    @java.lang.Override
    public int getActionValue() {
      return instance.getActionValue();
    }
    /**
     * <code>.server.FeedItem.Action action = 1;</code>
     * @param value The action to set.
     * @return This builder for chaining.
     */
    public Builder setActionValue(int value) {
      copyOnWrite();
      instance.setActionValue(value);
      return this;
    }
    /**
     * <code>.server.FeedItem.Action action = 1;</code>
     * @return The action.
     */
    @java.lang.Override
    public com.halloapp.proto.server.FeedItem.Action getAction() {
      return instance.getAction();
    }
    /**
     * <code>.server.FeedItem.Action action = 1;</code>
     * @param value The enum numeric value on the wire for action to set.
     * @return This builder for chaining.
     */
    public Builder setAction(com.halloapp.proto.server.FeedItem.Action value) {
      copyOnWrite();
      instance.setAction(value);
      return this;
    }
    /**
     * <code>.server.FeedItem.Action action = 1;</code>
     * @return This builder for chaining.
     */
    public Builder clearAction() {
      copyOnWrite();
      instance.clearAction();
      return this;
    }

    /**
     * <code>.server.Post post = 2;</code>
     */
    @java.lang.Override
    public boolean hasPost() {
      return instance.hasPost();
    }
    /**
     * <code>.server.Post post = 2;</code>
     */
    @java.lang.Override
    public com.halloapp.proto.server.Post getPost() {
      return instance.getPost();
    }
    /**
     * <code>.server.Post post = 2;</code>
     */
    public Builder setPost(com.halloapp.proto.server.Post value) {
      copyOnWrite();
      instance.setPost(value);
      return this;
    }
    /**
     * <code>.server.Post post = 2;</code>
     */
    public Builder setPost(
        com.halloapp.proto.server.Post.Builder builderForValue) {
      copyOnWrite();
      instance.setPost(builderForValue.build());
      return this;
    }
    /**
     * <code>.server.Post post = 2;</code>
     */
    public Builder mergePost(com.halloapp.proto.server.Post value) {
      copyOnWrite();
      instance.mergePost(value);
      return this;
    }
    /**
     * <code>.server.Post post = 2;</code>
     */
    public Builder clearPost() {
      copyOnWrite();
      instance.clearPost();
      return this;
    }

    /**
     * <code>.server.Comment comment = 3;</code>
     */
    @java.lang.Override
    public boolean hasComment() {
      return instance.hasComment();
    }
    /**
     * <code>.server.Comment comment = 3;</code>
     */
    @java.lang.Override
    public com.halloapp.proto.server.Comment getComment() {
      return instance.getComment();
    }
    /**
     * <code>.server.Comment comment = 3;</code>
     */
    public Builder setComment(com.halloapp.proto.server.Comment value) {
      copyOnWrite();
      instance.setComment(value);
      return this;
    }
    /**
     * <code>.server.Comment comment = 3;</code>
     */
    public Builder setComment(
        com.halloapp.proto.server.Comment.Builder builderForValue) {
      copyOnWrite();
      instance.setComment(builderForValue.build());
      return this;
    }
    /**
     * <code>.server.Comment comment = 3;</code>
     */
    public Builder mergeComment(com.halloapp.proto.server.Comment value) {
      copyOnWrite();
      instance.mergeComment(value);
      return this;
    }
    /**
     * <code>.server.Comment comment = 3;</code>
     */
    public Builder clearComment() {
      copyOnWrite();
      instance.clearComment();
      return this;
    }

    /**
     * <code>repeated .server.ShareStanza share_stanzas = 4;</code>
     */
    @java.lang.Override
    public java.util.List<com.halloapp.proto.server.ShareStanza> getShareStanzasList() {
      return java.util.Collections.unmodifiableList(
          instance.getShareStanzasList());
    }
    /**
     * <code>repeated .server.ShareStanza share_stanzas = 4;</code>
     */
    @java.lang.Override
    public int getShareStanzasCount() {
      return instance.getShareStanzasCount();
    }/**
     * <code>repeated .server.ShareStanza share_stanzas = 4;</code>
     */
    @java.lang.Override
    public com.halloapp.proto.server.ShareStanza getShareStanzas(int index) {
      return instance.getShareStanzas(index);
    }
    /**
     * <code>repeated .server.ShareStanza share_stanzas = 4;</code>
     */
    public Builder setShareStanzas(
        int index, com.halloapp.proto.server.ShareStanza value) {
      copyOnWrite();
      instance.setShareStanzas(index, value);
      return this;
    }
    /**
     * <code>repeated .server.ShareStanza share_stanzas = 4;</code>
     */
    public Builder setShareStanzas(
        int index, com.halloapp.proto.server.ShareStanza.Builder builderForValue) {
      copyOnWrite();
      instance.setShareStanzas(index,
          builderForValue.build());
      return this;
    }
    /**
     * <code>repeated .server.ShareStanza share_stanzas = 4;</code>
     */
    public Builder addShareStanzas(com.halloapp.proto.server.ShareStanza value) {
      copyOnWrite();
      instance.addShareStanzas(value);
      return this;
    }
    /**
     * <code>repeated .server.ShareStanza share_stanzas = 4;</code>
     */
    public Builder addShareStanzas(
        int index, com.halloapp.proto.server.ShareStanza value) {
      copyOnWrite();
      instance.addShareStanzas(index, value);
      return this;
    }
    /**
     * <code>repeated .server.ShareStanza share_stanzas = 4;</code>
     */
    public Builder addShareStanzas(
        com.halloapp.proto.server.ShareStanza.Builder builderForValue) {
      copyOnWrite();
      instance.addShareStanzas(builderForValue.build());
      return this;
    }
    /**
     * <code>repeated .server.ShareStanza share_stanzas = 4;</code>
     */
    public Builder addShareStanzas(
        int index, com.halloapp.proto.server.ShareStanza.Builder builderForValue) {
      copyOnWrite();
      instance.addShareStanzas(index,
          builderForValue.build());
      return this;
    }
    /**
     * <code>repeated .server.ShareStanza share_stanzas = 4;</code>
     */
    public Builder addAllShareStanzas(
        java.lang.Iterable<? extends com.halloapp.proto.server.ShareStanza> values) {
      copyOnWrite();
      instance.addAllShareStanzas(values);
      return this;
    }
    /**
     * <code>repeated .server.ShareStanza share_stanzas = 4;</code>
     */
    public Builder clearShareStanzas() {
      copyOnWrite();
      instance.clearShareStanzas();
      return this;
    }
    /**
     * <code>repeated .server.ShareStanza share_stanzas = 4;</code>
     */
    public Builder removeShareStanzas(int index) {
      copyOnWrite();
      instance.removeShareStanzas(index);
      return this;
    }

    /**
     * <pre>
     * Sent by the publisher.
     * </pre>
     *
     * <code>repeated .server.SenderStateBundle sender_state_bundles = 7;</code>
     */
    @java.lang.Override
    public java.util.List<com.halloapp.proto.server.SenderStateBundle> getSenderStateBundlesList() {
      return java.util.Collections.unmodifiableList(
          instance.getSenderStateBundlesList());
    }
    /**
     * <pre>
     * Sent by the publisher.
     * </pre>
     *
     * <code>repeated .server.SenderStateBundle sender_state_bundles = 7;</code>
     */
    @java.lang.Override
    public int getSenderStateBundlesCount() {
      return instance.getSenderStateBundlesCount();
    }/**
     * <pre>
     * Sent by the publisher.
     * </pre>
     *
     * <code>repeated .server.SenderStateBundle sender_state_bundles = 7;</code>
     */
    @java.lang.Override
    public com.halloapp.proto.server.SenderStateBundle getSenderStateBundles(int index) {
      return instance.getSenderStateBundles(index);
    }
    /**
     * <pre>
     * Sent by the publisher.
     * </pre>
     *
     * <code>repeated .server.SenderStateBundle sender_state_bundles = 7;</code>
     */
    public Builder setSenderStateBundles(
        int index, com.halloapp.proto.server.SenderStateBundle value) {
      copyOnWrite();
      instance.setSenderStateBundles(index, value);
      return this;
    }
    /**
     * <pre>
     * Sent by the publisher.
     * </pre>
     *
     * <code>repeated .server.SenderStateBundle sender_state_bundles = 7;</code>
     */
    public Builder setSenderStateBundles(
        int index, com.halloapp.proto.server.SenderStateBundle.Builder builderForValue) {
      copyOnWrite();
      instance.setSenderStateBundles(index,
          builderForValue.build());
      return this;
    }
    /**
     * <pre>
     * Sent by the publisher.
     * </pre>
     *
     * <code>repeated .server.SenderStateBundle sender_state_bundles = 7;</code>
     */
    public Builder addSenderStateBundles(com.halloapp.proto.server.SenderStateBundle value) {
      copyOnWrite();
      instance.addSenderStateBundles(value);
      return this;
    }
    /**
     * <pre>
     * Sent by the publisher.
     * </pre>
     *
     * <code>repeated .server.SenderStateBundle sender_state_bundles = 7;</code>
     */
    public Builder addSenderStateBundles(
        int index, com.halloapp.proto.server.SenderStateBundle value) {
      copyOnWrite();
      instance.addSenderStateBundles(index, value);
      return this;
    }
    /**
     * <pre>
     * Sent by the publisher.
     * </pre>
     *
     * <code>repeated .server.SenderStateBundle sender_state_bundles = 7;</code>
     */
    public Builder addSenderStateBundles(
        com.halloapp.proto.server.SenderStateBundle.Builder builderForValue) {
      copyOnWrite();
      instance.addSenderStateBundles(builderForValue.build());
      return this;
    }
    /**
     * <pre>
     * Sent by the publisher.
     * </pre>
     *
     * <code>repeated .server.SenderStateBundle sender_state_bundles = 7;</code>
     */
    public Builder addSenderStateBundles(
        int index, com.halloapp.proto.server.SenderStateBundle.Builder builderForValue) {
      copyOnWrite();
      instance.addSenderStateBundles(index,
          builderForValue.build());
      return this;
    }
    /**
     * <pre>
     * Sent by the publisher.
     * </pre>
     *
     * <code>repeated .server.SenderStateBundle sender_state_bundles = 7;</code>
     */
    public Builder addAllSenderStateBundles(
        java.lang.Iterable<? extends com.halloapp.proto.server.SenderStateBundle> values) {
      copyOnWrite();
      instance.addAllSenderStateBundles(values);
      return this;
    }
    /**
     * <pre>
     * Sent by the publisher.
     * </pre>
     *
     * <code>repeated .server.SenderStateBundle sender_state_bundles = 7;</code>
     */
    public Builder clearSenderStateBundles() {
      copyOnWrite();
      instance.clearSenderStateBundles();
      return this;
    }
    /**
     * <pre>
     * Sent by the publisher.
     * </pre>
     *
     * <code>repeated .server.SenderStateBundle sender_state_bundles = 7;</code>
     */
    public Builder removeSenderStateBundles(int index) {
      copyOnWrite();
      instance.removeSenderStateBundles(index);
      return this;
    }

    /**
     * <pre>
     * Meant for the receiver, computed by the server using `sender_state_bundles`.
     * </pre>
     *
     * <code>.server.SenderStateWithKeyInfo sender_state = 8;</code>
     */
    @java.lang.Override
    public boolean hasSenderState() {
      return instance.hasSenderState();
    }
    /**
     * <pre>
     * Meant for the receiver, computed by the server using `sender_state_bundles`.
     * </pre>
     *
     * <code>.server.SenderStateWithKeyInfo sender_state = 8;</code>
     */
    @java.lang.Override
    public com.halloapp.proto.server.SenderStateWithKeyInfo getSenderState() {
      return instance.getSenderState();
    }
    /**
     * <pre>
     * Meant for the receiver, computed by the server using `sender_state_bundles`.
     * </pre>
     *
     * <code>.server.SenderStateWithKeyInfo sender_state = 8;</code>
     */
    public Builder setSenderState(com.halloapp.proto.server.SenderStateWithKeyInfo value) {
      copyOnWrite();
      instance.setSenderState(value);
      return this;
      }
    /**
     * <pre>
     * Meant for the receiver, computed by the server using `sender_state_bundles`.
     * </pre>
     *
     * <code>.server.SenderStateWithKeyInfo sender_state = 8;</code>
     */
    public Builder setSenderState(
        com.halloapp.proto.server.SenderStateWithKeyInfo.Builder builderForValue) {
      copyOnWrite();
      instance.setSenderState(builderForValue.build());
      return this;
    }
    /**
     * <pre>
     * Meant for the receiver, computed by the server using `sender_state_bundles`.
     * </pre>
     *
     * <code>.server.SenderStateWithKeyInfo sender_state = 8;</code>
     */
    public Builder mergeSenderState(com.halloapp.proto.server.SenderStateWithKeyInfo value) {
      copyOnWrite();
      instance.mergeSenderState(value);
      return this;
    }
    /**
     * <pre>
     * Meant for the receiver, computed by the server using `sender_state_bundles`.
     * </pre>
     *
     * <code>.server.SenderStateWithKeyInfo sender_state = 8;</code>
     */
    public Builder clearSenderState() {  copyOnWrite();
      instance.clearSenderState();
      return this;
    }

    // @@protoc_insertion_point(builder_scope:server.FeedItem)
  }
  @java.lang.Override
  @java.lang.SuppressWarnings({"unchecked", "fallthrough"})
  protected final java.lang.Object dynamicMethod(
      com.google.protobuf.GeneratedMessageLite.MethodToInvoke method,
      java.lang.Object arg0, java.lang.Object arg1) {
    switch (method) {
      case NEW_MUTABLE_INSTANCE: {
        return new com.halloapp.proto.server.FeedItem();
      }
      case NEW_BUILDER: {
        return new Builder();
      }
      case BUILD_MESSAGE_INFO: {
          java.lang.Object[] objects = new java.lang.Object[] {
            "item_",
            "itemCase_",
            "action_",
            com.halloapp.proto.server.Post.class,
            com.halloapp.proto.server.Comment.class,
            "shareStanzas_",
            com.halloapp.proto.server.ShareStanza.class,
            "senderStateBundles_",
            com.halloapp.proto.server.SenderStateBundle.class,
            "senderState_",
          };
          java.lang.String info =
              "\u0000\u0006\u0001\u0000\u0001\b\u0006\u0000\u0002\u0000\u0001\f\u0002<\u0000\u0003" +
              "<\u0000\u0004\u001b\u0007\u001b\b\t";
          return newMessageInfo(DEFAULT_INSTANCE, info, objects);
      }
      // fall through
      case GET_DEFAULT_INSTANCE: {
        return DEFAULT_INSTANCE;
      }
      case GET_PARSER: {
        com.google.protobuf.Parser<com.halloapp.proto.server.FeedItem> parser = PARSER;
        if (parser == null) {
          synchronized (com.halloapp.proto.server.FeedItem.class) {
            parser = PARSER;
            if (parser == null) {
              parser =
                  new DefaultInstanceBasedParser<com.halloapp.proto.server.FeedItem>(
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


  // @@protoc_insertion_point(class_scope:server.FeedItem)
  private static final com.halloapp.proto.server.FeedItem DEFAULT_INSTANCE;
  static {
    FeedItem defaultInstance = new FeedItem();
    // New instances are implicitly immutable so no need to make
    // immutable.
    DEFAULT_INSTANCE = defaultInstance;
    com.google.protobuf.GeneratedMessageLite.registerDefaultInstance(
      FeedItem.class, defaultInstance);
  }

  public static com.halloapp.proto.server.FeedItem getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static volatile com.google.protobuf.Parser<FeedItem> PARSER;

  public static com.google.protobuf.Parser<FeedItem> parser() {
    return DEFAULT_INSTANCE.getParserForType();
  }
}

