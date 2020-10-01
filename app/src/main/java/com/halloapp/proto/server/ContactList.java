// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: server.proto

package com.halloapp.proto.server;

/**
 * Protobuf type {@code server.ContactList}
 */
public  final class ContactList extends
    com.google.protobuf.GeneratedMessageLite<
        ContactList, ContactList.Builder> implements
    // @@protoc_insertion_point(message_implements:server.ContactList)
    ContactListOrBuilder {
  private ContactList() {
    syncId_ = "";
    contacts_ = emptyProtobufList();
  }
  /**
   * Protobuf enum {@code server.ContactList.Type}
   */
  public enum Type
      implements com.google.protobuf.Internal.EnumLite {
    /**
     * <code>FULL = 0;</code>
     */
    FULL(0),
    /**
     * <code>DELTA = 1;</code>
     */
    DELTA(1),
    /**
     * <code>NORMAL = 2;</code>
     */
    NORMAL(2),
    UNRECOGNIZED(-1),
    ;

    /**
     * <code>FULL = 0;</code>
     */
    public static final int FULL_VALUE = 0;
    /**
     * <code>DELTA = 1;</code>
     */
    public static final int DELTA_VALUE = 1;
    /**
     * <code>NORMAL = 2;</code>
     */
    public static final int NORMAL_VALUE = 2;


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
    public static Type valueOf(int value) {
      return forNumber(value);
    }

    public static Type forNumber(int value) {
      switch (value) {
        case 0: return FULL;
        case 1: return DELTA;
        case 2: return NORMAL;
        default: return null;
      }
    }

    public static com.google.protobuf.Internal.EnumLiteMap<Type>
        internalGetValueMap() {
      return internalValueMap;
    }
    private static final com.google.protobuf.Internal.EnumLiteMap<
        Type> internalValueMap =
          new com.google.protobuf.Internal.EnumLiteMap<Type>() {
            @java.lang.Override
            public Type findValueByNumber(int number) {
              return Type.forNumber(number);
            }
          };

    public static com.google.protobuf.Internal.EnumVerifier 
        internalGetVerifier() {
      return TypeVerifier.INSTANCE;
    }

    private static final class TypeVerifier implements 
         com.google.protobuf.Internal.EnumVerifier { 
            static final com.google.protobuf.Internal.EnumVerifier           INSTANCE = new TypeVerifier();
            @java.lang.Override
            public boolean isInRange(int number) {
              return Type.forNumber(number) != null;
            }
          };

    private final int value;

    private Type(int value) {
      this.value = value;
    }

    // @@protoc_insertion_point(enum_scope:server.ContactList.Type)
  }

  public static final int TYPE_FIELD_NUMBER = 1;
  private int type_;
  /**
   * <code>.server.ContactList.Type type = 1;</code>
   * @return The enum numeric value on the wire for type.
   */
  @java.lang.Override
  public int getTypeValue() {
    return type_;
  }
  /**
   * <code>.server.ContactList.Type type = 1;</code>
   * @return The type.
   */
  @java.lang.Override
  public com.halloapp.proto.server.ContactList.Type getType() {
    com.halloapp.proto.server.ContactList.Type result = com.halloapp.proto.server.ContactList.Type.forNumber(type_);
    return result == null ? com.halloapp.proto.server.ContactList.Type.UNRECOGNIZED : result;
  }
  /**
   * <code>.server.ContactList.Type type = 1;</code>
   * @param value The enum numeric value on the wire for type to set.
   */
  private void setTypeValue(int value) {
      type_ = value;
  }
  /**
   * <code>.server.ContactList.Type type = 1;</code>
   * @param value The type to set.
   */
  private void setType(com.halloapp.proto.server.ContactList.Type value) {
    type_ = value.getNumber();
    
  }
  /**
   * <code>.server.ContactList.Type type = 1;</code>
   */
  private void clearType() {
    
    type_ = 0;
  }

  public static final int SYNC_ID_FIELD_NUMBER = 2;
  private java.lang.String syncId_;
  /**
   * <code>string sync_id = 2;</code>
   * @return The syncId.
   */
  @java.lang.Override
  public java.lang.String getSyncId() {
    return syncId_;
  }
  /**
   * <code>string sync_id = 2;</code>
   * @return The bytes for syncId.
   */
  @java.lang.Override
  public com.google.protobuf.ByteString
      getSyncIdBytes() {
    return com.google.protobuf.ByteString.copyFromUtf8(syncId_);
  }
  /**
   * <code>string sync_id = 2;</code>
   * @param value The syncId to set.
   */
  private void setSyncId(
      java.lang.String value) {
    value.getClass();
  
    syncId_ = value;
  }
  /**
   * <code>string sync_id = 2;</code>
   */
  private void clearSyncId() {
    
    syncId_ = getDefaultInstance().getSyncId();
  }
  /**
   * <code>string sync_id = 2;</code>
   * @param value The bytes for syncId to set.
   */
  private void setSyncIdBytes(
      com.google.protobuf.ByteString value) {
    checkByteStringIsUtf8(value);
    syncId_ = value.toStringUtf8();
    
  }

  public static final int BATCH_INDEX_FIELD_NUMBER = 3;
  private int batchIndex_;
  /**
   * <code>int32 batch_index = 3;</code>
   * @return The batchIndex.
   */
  @java.lang.Override
  public int getBatchIndex() {
    return batchIndex_;
  }
  /**
   * <code>int32 batch_index = 3;</code>
   * @param value The batchIndex to set.
   */
  private void setBatchIndex(int value) {
    
    batchIndex_ = value;
  }
  /**
   * <code>int32 batch_index = 3;</code>
   */
  private void clearBatchIndex() {
    
    batchIndex_ = 0;
  }

  public static final int IS_LAST_FIELD_NUMBER = 4;
  private boolean isLast_;
  /**
   * <code>bool is_last = 4;</code>
   * @return The isLast.
   */
  @java.lang.Override
  public boolean getIsLast() {
    return isLast_;
  }
  /**
   * <code>bool is_last = 4;</code>
   * @param value The isLast to set.
   */
  private void setIsLast(boolean value) {
    
    isLast_ = value;
  }
  /**
   * <code>bool is_last = 4;</code>
   */
  private void clearIsLast() {
    
    isLast_ = false;
  }

  public static final int CONTACTS_FIELD_NUMBER = 5;
  private com.google.protobuf.Internal.ProtobufList<com.halloapp.proto.server.Contact> contacts_;
  /**
   * <code>repeated .server.Contact contacts = 5;</code>
   */
  @java.lang.Override
  public java.util.List<com.halloapp.proto.server.Contact> getContactsList() {
    return contacts_;
  }
  /**
   * <code>repeated .server.Contact contacts = 5;</code>
   */
  public java.util.List<? extends com.halloapp.proto.server.ContactOrBuilder> 
      getContactsOrBuilderList() {
    return contacts_;
  }
  /**
   * <code>repeated .server.Contact contacts = 5;</code>
   */
  @java.lang.Override
  public int getContactsCount() {
    return contacts_.size();
  }
  /**
   * <code>repeated .server.Contact contacts = 5;</code>
   */
  @java.lang.Override
  public com.halloapp.proto.server.Contact getContacts(int index) {
    return contacts_.get(index);
  }
  /**
   * <code>repeated .server.Contact contacts = 5;</code>
   */
  public com.halloapp.proto.server.ContactOrBuilder getContactsOrBuilder(
      int index) {
    return contacts_.get(index);
  }
  private void ensureContactsIsMutable() {
    com.google.protobuf.Internal.ProtobufList<com.halloapp.proto.server.Contact> tmp = contacts_;
    if (!tmp.isModifiable()) {
      contacts_ =
          com.google.protobuf.GeneratedMessageLite.mutableCopy(tmp);
     }
  }

  /**
   * <code>repeated .server.Contact contacts = 5;</code>
   */
  private void setContacts(
      int index, com.halloapp.proto.server.Contact value) {
    value.getClass();
  ensureContactsIsMutable();
    contacts_.set(index, value);
  }
  /**
   * <code>repeated .server.Contact contacts = 5;</code>
   */
  private void addContacts(com.halloapp.proto.server.Contact value) {
    value.getClass();
  ensureContactsIsMutable();
    contacts_.add(value);
  }
  /**
   * <code>repeated .server.Contact contacts = 5;</code>
   */
  private void addContacts(
      int index, com.halloapp.proto.server.Contact value) {
    value.getClass();
  ensureContactsIsMutable();
    contacts_.add(index, value);
  }
  /**
   * <code>repeated .server.Contact contacts = 5;</code>
   */
  private void addAllContacts(
      java.lang.Iterable<? extends com.halloapp.proto.server.Contact> values) {
    ensureContactsIsMutable();
    com.google.protobuf.AbstractMessageLite.addAll(
        values, contacts_);
  }
  /**
   * <code>repeated .server.Contact contacts = 5;</code>
   */
  private void clearContacts() {
    contacts_ = emptyProtobufList();
  }
  /**
   * <code>repeated .server.Contact contacts = 5;</code>
   */
  private void removeContacts(int index) {
    ensureContactsIsMutable();
    contacts_.remove(index);
  }

  public static com.halloapp.proto.server.ContactList parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static com.halloapp.proto.server.ContactList parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static com.halloapp.proto.server.ContactList parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static com.halloapp.proto.server.ContactList parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static com.halloapp.proto.server.ContactList parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static com.halloapp.proto.server.ContactList parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static com.halloapp.proto.server.ContactList parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input);
  }
  public static com.halloapp.proto.server.ContactList parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input, extensionRegistry);
  }
  public static com.halloapp.proto.server.ContactList parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return parseDelimitedFrom(DEFAULT_INSTANCE, input);
  }
  public static com.halloapp.proto.server.ContactList parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return parseDelimitedFrom(DEFAULT_INSTANCE, input, extensionRegistry);
  }
  public static com.halloapp.proto.server.ContactList parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input);
  }
  public static com.halloapp.proto.server.ContactList parseFrom(
      com.google.protobuf.CodedInputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input, extensionRegistry);
  }

  public static Builder newBuilder() {
    return (Builder) DEFAULT_INSTANCE.createBuilder();
  }
  public static Builder newBuilder(com.halloapp.proto.server.ContactList prototype) {
    return (Builder) DEFAULT_INSTANCE.createBuilder(prototype);
  }

  /**
   * Protobuf type {@code server.ContactList}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessageLite.Builder<
        com.halloapp.proto.server.ContactList, Builder> implements
      // @@protoc_insertion_point(builder_implements:server.ContactList)
      com.halloapp.proto.server.ContactListOrBuilder {
    // Construct using com.halloapp.proto.server.ContactList.newBuilder()
    private Builder() {
      super(DEFAULT_INSTANCE);
    }


    /**
     * <code>.server.ContactList.Type type = 1;</code>
     * @return The enum numeric value on the wire for type.
     */
    @java.lang.Override
    public int getTypeValue() {
      return instance.getTypeValue();
    }
    /**
     * <code>.server.ContactList.Type type = 1;</code>
     * @param value The type to set.
     * @return This builder for chaining.
     */
    public Builder setTypeValue(int value) {
      copyOnWrite();
      instance.setTypeValue(value);
      return this;
    }
    /**
     * <code>.server.ContactList.Type type = 1;</code>
     * @return The type.
     */
    @java.lang.Override
    public com.halloapp.proto.server.ContactList.Type getType() {
      return instance.getType();
    }
    /**
     * <code>.server.ContactList.Type type = 1;</code>
     * @param value The enum numeric value on the wire for type to set.
     * @return This builder for chaining.
     */
    public Builder setType(com.halloapp.proto.server.ContactList.Type value) {
      copyOnWrite();
      instance.setType(value);
      return this;
    }
    /**
     * <code>.server.ContactList.Type type = 1;</code>
     * @return This builder for chaining.
     */
    public Builder clearType() {
      copyOnWrite();
      instance.clearType();
      return this;
    }

    /**
     * <code>string sync_id = 2;</code>
     * @return The syncId.
     */
    @java.lang.Override
    public java.lang.String getSyncId() {
      return instance.getSyncId();
    }
    /**
     * <code>string sync_id = 2;</code>
     * @return The bytes for syncId.
     */
    @java.lang.Override
    public com.google.protobuf.ByteString
        getSyncIdBytes() {
      return instance.getSyncIdBytes();
    }
    /**
     * <code>string sync_id = 2;</code>
     * @param value The syncId to set.
     * @return This builder for chaining.
     */
    public Builder setSyncId(
        java.lang.String value) {
      copyOnWrite();
      instance.setSyncId(value);
      return this;
    }
    /**
     * <code>string sync_id = 2;</code>
     * @return This builder for chaining.
     */
    public Builder clearSyncId() {
      copyOnWrite();
      instance.clearSyncId();
      return this;
    }
    /**
     * <code>string sync_id = 2;</code>
     * @param value The bytes for syncId to set.
     * @return This builder for chaining.
     */
    public Builder setSyncIdBytes(
        com.google.protobuf.ByteString value) {
      copyOnWrite();
      instance.setSyncIdBytes(value);
      return this;
    }

    /**
     * <code>int32 batch_index = 3;</code>
     * @return The batchIndex.
     */
    @java.lang.Override
    public int getBatchIndex() {
      return instance.getBatchIndex();
    }
    /**
     * <code>int32 batch_index = 3;</code>
     * @param value The batchIndex to set.
     * @return This builder for chaining.
     */
    public Builder setBatchIndex(int value) {
      copyOnWrite();
      instance.setBatchIndex(value);
      return this;
    }
    /**
     * <code>int32 batch_index = 3;</code>
     * @return This builder for chaining.
     */
    public Builder clearBatchIndex() {
      copyOnWrite();
      instance.clearBatchIndex();
      return this;
    }

    /**
     * <code>bool is_last = 4;</code>
     * @return The isLast.
     */
    @java.lang.Override
    public boolean getIsLast() {
      return instance.getIsLast();
    }
    /**
     * <code>bool is_last = 4;</code>
     * @param value The isLast to set.
     * @return This builder for chaining.
     */
    public Builder setIsLast(boolean value) {
      copyOnWrite();
      instance.setIsLast(value);
      return this;
    }
    /**
     * <code>bool is_last = 4;</code>
     * @return This builder for chaining.
     */
    public Builder clearIsLast() {
      copyOnWrite();
      instance.clearIsLast();
      return this;
    }

    /**
     * <code>repeated .server.Contact contacts = 5;</code>
     */
    @java.lang.Override
    public java.util.List<com.halloapp.proto.server.Contact> getContactsList() {
      return java.util.Collections.unmodifiableList(
          instance.getContactsList());
    }
    /**
     * <code>repeated .server.Contact contacts = 5;</code>
     */
    @java.lang.Override
    public int getContactsCount() {
      return instance.getContactsCount();
    }/**
     * <code>repeated .server.Contact contacts = 5;</code>
     */
    @java.lang.Override
    public com.halloapp.proto.server.Contact getContacts(int index) {
      return instance.getContacts(index);
    }
    /**
     * <code>repeated .server.Contact contacts = 5;</code>
     */
    public Builder setContacts(
        int index, com.halloapp.proto.server.Contact value) {
      copyOnWrite();
      instance.setContacts(index, value);
      return this;
    }
    /**
     * <code>repeated .server.Contact contacts = 5;</code>
     */
    public Builder setContacts(
        int index, com.halloapp.proto.server.Contact.Builder builderForValue) {
      copyOnWrite();
      instance.setContacts(index,
          builderForValue.build());
      return this;
    }
    /**
     * <code>repeated .server.Contact contacts = 5;</code>
     */
    public Builder addContacts(com.halloapp.proto.server.Contact value) {
      copyOnWrite();
      instance.addContacts(value);
      return this;
    }
    /**
     * <code>repeated .server.Contact contacts = 5;</code>
     */
    public Builder addContacts(
        int index, com.halloapp.proto.server.Contact value) {
      copyOnWrite();
      instance.addContacts(index, value);
      return this;
    }
    /**
     * <code>repeated .server.Contact contacts = 5;</code>
     */
    public Builder addContacts(
        com.halloapp.proto.server.Contact.Builder builderForValue) {
      copyOnWrite();
      instance.addContacts(builderForValue.build());
      return this;
    }
    /**
     * <code>repeated .server.Contact contacts = 5;</code>
     */
    public Builder addContacts(
        int index, com.halloapp.proto.server.Contact.Builder builderForValue) {
      copyOnWrite();
      instance.addContacts(index,
          builderForValue.build());
      return this;
    }
    /**
     * <code>repeated .server.Contact contacts = 5;</code>
     */
    public Builder addAllContacts(
        java.lang.Iterable<? extends com.halloapp.proto.server.Contact> values) {
      copyOnWrite();
      instance.addAllContacts(values);
      return this;
    }
    /**
     * <code>repeated .server.Contact contacts = 5;</code>
     */
    public Builder clearContacts() {
      copyOnWrite();
      instance.clearContacts();
      return this;
    }
    /**
     * <code>repeated .server.Contact contacts = 5;</code>
     */
    public Builder removeContacts(int index) {
      copyOnWrite();
      instance.removeContacts(index);
      return this;
    }

    // @@protoc_insertion_point(builder_scope:server.ContactList)
  }
  @java.lang.Override
  @java.lang.SuppressWarnings({"unchecked", "fallthrough"})
  protected final java.lang.Object dynamicMethod(
      com.google.protobuf.GeneratedMessageLite.MethodToInvoke method,
      java.lang.Object arg0, java.lang.Object arg1) {
    switch (method) {
      case NEW_MUTABLE_INSTANCE: {
        return new com.halloapp.proto.server.ContactList();
      }
      case NEW_BUILDER: {
        return new Builder();
      }
      case BUILD_MESSAGE_INFO: {
          java.lang.Object[] objects = new java.lang.Object[] {
            "type_",
            "syncId_",
            "batchIndex_",
            "isLast_",
            "contacts_",
            com.halloapp.proto.server.Contact.class,
          };
          java.lang.String info =
              "\u0000\u0005\u0000\u0000\u0001\u0005\u0005\u0000\u0001\u0000\u0001\f\u0002\u0208" +
              "\u0003\u0004\u0004\u0007\u0005\u001b";
          return newMessageInfo(DEFAULT_INSTANCE, info, objects);
      }
      // fall through
      case GET_DEFAULT_INSTANCE: {
        return DEFAULT_INSTANCE;
      }
      case GET_PARSER: {
        com.google.protobuf.Parser<com.halloapp.proto.server.ContactList> parser = PARSER;
        if (parser == null) {
          synchronized (com.halloapp.proto.server.ContactList.class) {
            parser = PARSER;
            if (parser == null) {
              parser =
                  new DefaultInstanceBasedParser<com.halloapp.proto.server.ContactList>(
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


  // @@protoc_insertion_point(class_scope:server.ContactList)
  private static final com.halloapp.proto.server.ContactList DEFAULT_INSTANCE;
  static {
    ContactList defaultInstance = new ContactList();
    // New instances are implicitly immutable so no need to make
    // immutable.
    DEFAULT_INSTANCE = defaultInstance;
    com.google.protobuf.GeneratedMessageLite.registerDefaultInstance(
      ContactList.class, defaultInstance);
  }

  public static com.halloapp.proto.server.ContactList getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static volatile com.google.protobuf.Parser<ContactList> PARSER;

  public static com.google.protobuf.Parser<ContactList> parser() {
    return DEFAULT_INSTANCE.getParserForType();
  }
}

