// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: server.proto

package com.halloapp.proto.server;

/**
 * Protobuf type {@code server.PrivacyList}
 */
public  final class PrivacyList extends
    com.google.protobuf.GeneratedMessageLite<
        PrivacyList, PrivacyList.Builder> implements
    // @@protoc_insertion_point(message_implements:server.PrivacyList)
    PrivacyListOrBuilder {
  private PrivacyList() {
    uidElements_ = emptyProtobufList();
    hash_ = com.google.protobuf.ByteString.EMPTY;
    phoneElements_ = emptyProtobufList();
  }
  /**
   * Protobuf enum {@code server.PrivacyList.Type}
   */
  public enum Type
      implements com.google.protobuf.Internal.EnumLite {
    /**
     * <code>ALL = 0;</code>
     */
    ALL(0),
    /**
     * <code>BLOCK = 1;</code>
     */
    BLOCK(1),
    /**
     * <code>EXCEPT = 2;</code>
     */
    EXCEPT(2),
    /**
     * <code>MUTE = 3;</code>
     */
    MUTE(3),
    /**
     * <code>ONLY = 4;</code>
     */
    ONLY(4),
    UNRECOGNIZED(-1),
    ;

    /**
     * <code>ALL = 0;</code>
     */
    public static final int ALL_VALUE = 0;
    /**
     * <code>BLOCK = 1;</code>
     */
    public static final int BLOCK_VALUE = 1;
    /**
     * <code>EXCEPT = 2;</code>
     */
    public static final int EXCEPT_VALUE = 2;
    /**
     * <code>MUTE = 3;</code>
     */
    public static final int MUTE_VALUE = 3;
    /**
     * <code>ONLY = 4;</code>
     */
    public static final int ONLY_VALUE = 4;


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
        case 0: return ALL;
        case 1: return BLOCK;
        case 2: return EXCEPT;
        case 3: return MUTE;
        case 4: return ONLY;
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

    // @@protoc_insertion_point(enum_scope:server.PrivacyList.Type)
  }

  public static final int TYPE_FIELD_NUMBER = 1;
  private int type_;
  /**
   * <code>.server.PrivacyList.Type type = 1;</code>
   * @return The enum numeric value on the wire for type.
   */
  @java.lang.Override
  public int getTypeValue() {
    return type_;
  }
  /**
   * <code>.server.PrivacyList.Type type = 1;</code>
   * @return The type.
   */
  @java.lang.Override
  public com.halloapp.proto.server.PrivacyList.Type getType() {
    com.halloapp.proto.server.PrivacyList.Type result = com.halloapp.proto.server.PrivacyList.Type.forNumber(type_);
    return result == null ? com.halloapp.proto.server.PrivacyList.Type.UNRECOGNIZED : result;
  }
  /**
   * <code>.server.PrivacyList.Type type = 1;</code>
   * @param value The enum numeric value on the wire for type to set.
   */
  private void setTypeValue(int value) {
      type_ = value;
  }
  /**
   * <code>.server.PrivacyList.Type type = 1;</code>
   * @param value The type to set.
   */
  private void setType(com.halloapp.proto.server.PrivacyList.Type value) {
    type_ = value.getNumber();
    
  }
  /**
   * <code>.server.PrivacyList.Type type = 1;</code>
   */
  private void clearType() {
    
    type_ = 0;
  }

  public static final int UID_ELEMENTS_FIELD_NUMBER = 2;
  private com.google.protobuf.Internal.ProtobufList<com.halloapp.proto.server.UidElement> uidElements_;
  /**
   * <code>repeated .server.UidElement uid_elements = 2;</code>
   */
  @java.lang.Override
  public java.util.List<com.halloapp.proto.server.UidElement> getUidElementsList() {
    return uidElements_;
  }
  /**
   * <code>repeated .server.UidElement uid_elements = 2;</code>
   */
  public java.util.List<? extends com.halloapp.proto.server.UidElementOrBuilder> 
      getUidElementsOrBuilderList() {
    return uidElements_;
  }
  /**
   * <code>repeated .server.UidElement uid_elements = 2;</code>
   */
  @java.lang.Override
  public int getUidElementsCount() {
    return uidElements_.size();
  }
  /**
   * <code>repeated .server.UidElement uid_elements = 2;</code>
   */
  @java.lang.Override
  public com.halloapp.proto.server.UidElement getUidElements(int index) {
    return uidElements_.get(index);
  }
  /**
   * <code>repeated .server.UidElement uid_elements = 2;</code>
   */
  public com.halloapp.proto.server.UidElementOrBuilder getUidElementsOrBuilder(
      int index) {
    return uidElements_.get(index);
  }
  private void ensureUidElementsIsMutable() {
    com.google.protobuf.Internal.ProtobufList<com.halloapp.proto.server.UidElement> tmp = uidElements_;
    if (!tmp.isModifiable()) {
      uidElements_ =
          com.google.protobuf.GeneratedMessageLite.mutableCopy(tmp);
     }
  }

  /**
   * <code>repeated .server.UidElement uid_elements = 2;</code>
   */
  private void setUidElements(
      int index, com.halloapp.proto.server.UidElement value) {
    value.getClass();
  ensureUidElementsIsMutable();
    uidElements_.set(index, value);
  }
  /**
   * <code>repeated .server.UidElement uid_elements = 2;</code>
   */
  private void addUidElements(com.halloapp.proto.server.UidElement value) {
    value.getClass();
  ensureUidElementsIsMutable();
    uidElements_.add(value);
  }
  /**
   * <code>repeated .server.UidElement uid_elements = 2;</code>
   */
  private void addUidElements(
      int index, com.halloapp.proto.server.UidElement value) {
    value.getClass();
  ensureUidElementsIsMutable();
    uidElements_.add(index, value);
  }
  /**
   * <code>repeated .server.UidElement uid_elements = 2;</code>
   */
  private void addAllUidElements(
      java.lang.Iterable<? extends com.halloapp.proto.server.UidElement> values) {
    ensureUidElementsIsMutable();
    com.google.protobuf.AbstractMessageLite.addAll(
        values, uidElements_);
  }
  /**
   * <code>repeated .server.UidElement uid_elements = 2;</code>
   */
  private void clearUidElements() {
    uidElements_ = emptyProtobufList();
  }
  /**
   * <code>repeated .server.UidElement uid_elements = 2;</code>
   */
  private void removeUidElements(int index) {
    ensureUidElementsIsMutable();
    uidElements_.remove(index);
  }

  public static final int HASH_FIELD_NUMBER = 3;
  private com.google.protobuf.ByteString hash_;
  /**
   * <code>bytes hash = 3;</code>
   * @return The hash.
   */
  @java.lang.Override
  public com.google.protobuf.ByteString getHash() {
    return hash_;
  }
  /**
   * <code>bytes hash = 3;</code>
   * @param value The hash to set.
   */
  private void setHash(com.google.protobuf.ByteString value) {
    value.getClass();
  
    hash_ = value;
  }
  /**
   * <code>bytes hash = 3;</code>
   */
  private void clearHash() {
    
    hash_ = getDefaultInstance().getHash();
  }

  public static final int PHONE_ELEMENTS_FIELD_NUMBER = 4;
  private com.google.protobuf.Internal.ProtobufList<com.halloapp.proto.server.PhoneElement> phoneElements_;
  /**
   * <code>repeated .server.PhoneElement phone_elements = 4;</code>
   */
  @java.lang.Override
  public java.util.List<com.halloapp.proto.server.PhoneElement> getPhoneElementsList() {
    return phoneElements_;
  }
  /**
   * <code>repeated .server.PhoneElement phone_elements = 4;</code>
   */
  public java.util.List<? extends com.halloapp.proto.server.PhoneElementOrBuilder> 
      getPhoneElementsOrBuilderList() {
    return phoneElements_;
  }
  /**
   * <code>repeated .server.PhoneElement phone_elements = 4;</code>
   */
  @java.lang.Override
  public int getPhoneElementsCount() {
    return phoneElements_.size();
  }
  /**
   * <code>repeated .server.PhoneElement phone_elements = 4;</code>
   */
  @java.lang.Override
  public com.halloapp.proto.server.PhoneElement getPhoneElements(int index) {
    return phoneElements_.get(index);
  }
  /**
   * <code>repeated .server.PhoneElement phone_elements = 4;</code>
   */
  public com.halloapp.proto.server.PhoneElementOrBuilder getPhoneElementsOrBuilder(
      int index) {
    return phoneElements_.get(index);
  }
  private void ensurePhoneElementsIsMutable() {
    com.google.protobuf.Internal.ProtobufList<com.halloapp.proto.server.PhoneElement> tmp = phoneElements_;
    if (!tmp.isModifiable()) {
      phoneElements_ =
          com.google.protobuf.GeneratedMessageLite.mutableCopy(tmp);
     }
  }

  /**
   * <code>repeated .server.PhoneElement phone_elements = 4;</code>
   */
  private void setPhoneElements(
      int index, com.halloapp.proto.server.PhoneElement value) {
    value.getClass();
  ensurePhoneElementsIsMutable();
    phoneElements_.set(index, value);
  }
  /**
   * <code>repeated .server.PhoneElement phone_elements = 4;</code>
   */
  private void addPhoneElements(com.halloapp.proto.server.PhoneElement value) {
    value.getClass();
  ensurePhoneElementsIsMutable();
    phoneElements_.add(value);
  }
  /**
   * <code>repeated .server.PhoneElement phone_elements = 4;</code>
   */
  private void addPhoneElements(
      int index, com.halloapp.proto.server.PhoneElement value) {
    value.getClass();
  ensurePhoneElementsIsMutable();
    phoneElements_.add(index, value);
  }
  /**
   * <code>repeated .server.PhoneElement phone_elements = 4;</code>
   */
  private void addAllPhoneElements(
      java.lang.Iterable<? extends com.halloapp.proto.server.PhoneElement> values) {
    ensurePhoneElementsIsMutable();
    com.google.protobuf.AbstractMessageLite.addAll(
        values, phoneElements_);
  }
  /**
   * <code>repeated .server.PhoneElement phone_elements = 4;</code>
   */
  private void clearPhoneElements() {
    phoneElements_ = emptyProtobufList();
  }
  /**
   * <code>repeated .server.PhoneElement phone_elements = 4;</code>
   */
  private void removePhoneElements(int index) {
    ensurePhoneElementsIsMutable();
    phoneElements_.remove(index);
  }

  public static final int USING_PHONES_FIELD_NUMBER = 5;
  private boolean usingPhones_;
  /**
   * <code>bool using_phones = 5;</code>
   * @return The usingPhones.
   */
  @java.lang.Override
  public boolean getUsingPhones() {
    return usingPhones_;
  }
  /**
   * <code>bool using_phones = 5;</code>
   * @param value The usingPhones to set.
   */
  private void setUsingPhones(boolean value) {
    
    usingPhones_ = value;
  }
  /**
   * <code>bool using_phones = 5;</code>
   */
  private void clearUsingPhones() {
    
    usingPhones_ = false;
  }

  public static com.halloapp.proto.server.PrivacyList parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static com.halloapp.proto.server.PrivacyList parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static com.halloapp.proto.server.PrivacyList parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static com.halloapp.proto.server.PrivacyList parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static com.halloapp.proto.server.PrivacyList parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static com.halloapp.proto.server.PrivacyList parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static com.halloapp.proto.server.PrivacyList parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input);
  }
  public static com.halloapp.proto.server.PrivacyList parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input, extensionRegistry);
  }
  public static com.halloapp.proto.server.PrivacyList parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return parseDelimitedFrom(DEFAULT_INSTANCE, input);
  }
  public static com.halloapp.proto.server.PrivacyList parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return parseDelimitedFrom(DEFAULT_INSTANCE, input, extensionRegistry);
  }
  public static com.halloapp.proto.server.PrivacyList parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input);
  }
  public static com.halloapp.proto.server.PrivacyList parseFrom(
      com.google.protobuf.CodedInputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input, extensionRegistry);
  }

  public static Builder newBuilder() {
    return (Builder) DEFAULT_INSTANCE.createBuilder();
  }
  public static Builder newBuilder(com.halloapp.proto.server.PrivacyList prototype) {
    return (Builder) DEFAULT_INSTANCE.createBuilder(prototype);
  }

  /**
   * Protobuf type {@code server.PrivacyList}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessageLite.Builder<
        com.halloapp.proto.server.PrivacyList, Builder> implements
      // @@protoc_insertion_point(builder_implements:server.PrivacyList)
      com.halloapp.proto.server.PrivacyListOrBuilder {
    // Construct using com.halloapp.proto.server.PrivacyList.newBuilder()
    private Builder() {
      super(DEFAULT_INSTANCE);
    }


    /**
     * <code>.server.PrivacyList.Type type = 1;</code>
     * @return The enum numeric value on the wire for type.
     */
    @java.lang.Override
    public int getTypeValue() {
      return instance.getTypeValue();
    }
    /**
     * <code>.server.PrivacyList.Type type = 1;</code>
     * @param value The type to set.
     * @return This builder for chaining.
     */
    public Builder setTypeValue(int value) {
      copyOnWrite();
      instance.setTypeValue(value);
      return this;
    }
    /**
     * <code>.server.PrivacyList.Type type = 1;</code>
     * @return The type.
     */
    @java.lang.Override
    public com.halloapp.proto.server.PrivacyList.Type getType() {
      return instance.getType();
    }
    /**
     * <code>.server.PrivacyList.Type type = 1;</code>
     * @param value The enum numeric value on the wire for type to set.
     * @return This builder for chaining.
     */
    public Builder setType(com.halloapp.proto.server.PrivacyList.Type value) {
      copyOnWrite();
      instance.setType(value);
      return this;
    }
    /**
     * <code>.server.PrivacyList.Type type = 1;</code>
     * @return This builder for chaining.
     */
    public Builder clearType() {
      copyOnWrite();
      instance.clearType();
      return this;
    }

    /**
     * <code>repeated .server.UidElement uid_elements = 2;</code>
     */
    @java.lang.Override
    public java.util.List<com.halloapp.proto.server.UidElement> getUidElementsList() {
      return java.util.Collections.unmodifiableList(
          instance.getUidElementsList());
    }
    /**
     * <code>repeated .server.UidElement uid_elements = 2;</code>
     */
    @java.lang.Override
    public int getUidElementsCount() {
      return instance.getUidElementsCount();
    }/**
     * <code>repeated .server.UidElement uid_elements = 2;</code>
     */
    @java.lang.Override
    public com.halloapp.proto.server.UidElement getUidElements(int index) {
      return instance.getUidElements(index);
    }
    /**
     * <code>repeated .server.UidElement uid_elements = 2;</code>
     */
    public Builder setUidElements(
        int index, com.halloapp.proto.server.UidElement value) {
      copyOnWrite();
      instance.setUidElements(index, value);
      return this;
    }
    /**
     * <code>repeated .server.UidElement uid_elements = 2;</code>
     */
    public Builder setUidElements(
        int index, com.halloapp.proto.server.UidElement.Builder builderForValue) {
      copyOnWrite();
      instance.setUidElements(index,
          builderForValue.build());
      return this;
    }
    /**
     * <code>repeated .server.UidElement uid_elements = 2;</code>
     */
    public Builder addUidElements(com.halloapp.proto.server.UidElement value) {
      copyOnWrite();
      instance.addUidElements(value);
      return this;
    }
    /**
     * <code>repeated .server.UidElement uid_elements = 2;</code>
     */
    public Builder addUidElements(
        int index, com.halloapp.proto.server.UidElement value) {
      copyOnWrite();
      instance.addUidElements(index, value);
      return this;
    }
    /**
     * <code>repeated .server.UidElement uid_elements = 2;</code>
     */
    public Builder addUidElements(
        com.halloapp.proto.server.UidElement.Builder builderForValue) {
      copyOnWrite();
      instance.addUidElements(builderForValue.build());
      return this;
    }
    /**
     * <code>repeated .server.UidElement uid_elements = 2;</code>
     */
    public Builder addUidElements(
        int index, com.halloapp.proto.server.UidElement.Builder builderForValue) {
      copyOnWrite();
      instance.addUidElements(index,
          builderForValue.build());
      return this;
    }
    /**
     * <code>repeated .server.UidElement uid_elements = 2;</code>
     */
    public Builder addAllUidElements(
        java.lang.Iterable<? extends com.halloapp.proto.server.UidElement> values) {
      copyOnWrite();
      instance.addAllUidElements(values);
      return this;
    }
    /**
     * <code>repeated .server.UidElement uid_elements = 2;</code>
     */
    public Builder clearUidElements() {
      copyOnWrite();
      instance.clearUidElements();
      return this;
    }
    /**
     * <code>repeated .server.UidElement uid_elements = 2;</code>
     */
    public Builder removeUidElements(int index) {
      copyOnWrite();
      instance.removeUidElements(index);
      return this;
    }

    /**
     * <code>bytes hash = 3;</code>
     * @return The hash.
     */
    @java.lang.Override
    public com.google.protobuf.ByteString getHash() {
      return instance.getHash();
    }
    /**
     * <code>bytes hash = 3;</code>
     * @param value The hash to set.
     * @return This builder for chaining.
     */
    public Builder setHash(com.google.protobuf.ByteString value) {
      copyOnWrite();
      instance.setHash(value);
      return this;
    }
    /**
     * <code>bytes hash = 3;</code>
     * @return This builder for chaining.
     */
    public Builder clearHash() {
      copyOnWrite();
      instance.clearHash();
      return this;
    }

    /**
     * <code>repeated .server.PhoneElement phone_elements = 4;</code>
     */
    @java.lang.Override
    public java.util.List<com.halloapp.proto.server.PhoneElement> getPhoneElementsList() {
      return java.util.Collections.unmodifiableList(
          instance.getPhoneElementsList());
    }
    /**
     * <code>repeated .server.PhoneElement phone_elements = 4;</code>
     */
    @java.lang.Override
    public int getPhoneElementsCount() {
      return instance.getPhoneElementsCount();
    }/**
     * <code>repeated .server.PhoneElement phone_elements = 4;</code>
     */
    @java.lang.Override
    public com.halloapp.proto.server.PhoneElement getPhoneElements(int index) {
      return instance.getPhoneElements(index);
    }
    /**
     * <code>repeated .server.PhoneElement phone_elements = 4;</code>
     */
    public Builder setPhoneElements(
        int index, com.halloapp.proto.server.PhoneElement value) {
      copyOnWrite();
      instance.setPhoneElements(index, value);
      return this;
    }
    /**
     * <code>repeated .server.PhoneElement phone_elements = 4;</code>
     */
    public Builder setPhoneElements(
        int index, com.halloapp.proto.server.PhoneElement.Builder builderForValue) {
      copyOnWrite();
      instance.setPhoneElements(index,
          builderForValue.build());
      return this;
    }
    /**
     * <code>repeated .server.PhoneElement phone_elements = 4;</code>
     */
    public Builder addPhoneElements(com.halloapp.proto.server.PhoneElement value) {
      copyOnWrite();
      instance.addPhoneElements(value);
      return this;
    }
    /**
     * <code>repeated .server.PhoneElement phone_elements = 4;</code>
     */
    public Builder addPhoneElements(
        int index, com.halloapp.proto.server.PhoneElement value) {
      copyOnWrite();
      instance.addPhoneElements(index, value);
      return this;
    }
    /**
     * <code>repeated .server.PhoneElement phone_elements = 4;</code>
     */
    public Builder addPhoneElements(
        com.halloapp.proto.server.PhoneElement.Builder builderForValue) {
      copyOnWrite();
      instance.addPhoneElements(builderForValue.build());
      return this;
    }
    /**
     * <code>repeated .server.PhoneElement phone_elements = 4;</code>
     */
    public Builder addPhoneElements(
        int index, com.halloapp.proto.server.PhoneElement.Builder builderForValue) {
      copyOnWrite();
      instance.addPhoneElements(index,
          builderForValue.build());
      return this;
    }
    /**
     * <code>repeated .server.PhoneElement phone_elements = 4;</code>
     */
    public Builder addAllPhoneElements(
        java.lang.Iterable<? extends com.halloapp.proto.server.PhoneElement> values) {
      copyOnWrite();
      instance.addAllPhoneElements(values);
      return this;
    }
    /**
     * <code>repeated .server.PhoneElement phone_elements = 4;</code>
     */
    public Builder clearPhoneElements() {
      copyOnWrite();
      instance.clearPhoneElements();
      return this;
    }
    /**
     * <code>repeated .server.PhoneElement phone_elements = 4;</code>
     */
    public Builder removePhoneElements(int index) {
      copyOnWrite();
      instance.removePhoneElements(index);
      return this;
    }

    /**
     * <code>bool using_phones = 5;</code>
     * @return The usingPhones.
     */
    @java.lang.Override
    public boolean getUsingPhones() {
      return instance.getUsingPhones();
    }
    /**
     * <code>bool using_phones = 5;</code>
     * @param value The usingPhones to set.
     * @return This builder for chaining.
     */
    public Builder setUsingPhones(boolean value) {
      copyOnWrite();
      instance.setUsingPhones(value);
      return this;
    }
    /**
     * <code>bool using_phones = 5;</code>
     * @return This builder for chaining.
     */
    public Builder clearUsingPhones() {
      copyOnWrite();
      instance.clearUsingPhones();
      return this;
    }

    // @@protoc_insertion_point(builder_scope:server.PrivacyList)
  }
  @java.lang.Override
  @java.lang.SuppressWarnings({"unchecked", "fallthrough"})
  protected final java.lang.Object dynamicMethod(
      com.google.protobuf.GeneratedMessageLite.MethodToInvoke method,
      java.lang.Object arg0, java.lang.Object arg1) {
    switch (method) {
      case NEW_MUTABLE_INSTANCE: {
        return new com.halloapp.proto.server.PrivacyList();
      }
      case NEW_BUILDER: {
        return new Builder();
      }
      case BUILD_MESSAGE_INFO: {
          java.lang.Object[] objects = new java.lang.Object[] {
            "type_",
            "uidElements_",
            com.halloapp.proto.server.UidElement.class,
            "hash_",
            "phoneElements_",
            com.halloapp.proto.server.PhoneElement.class,
            "usingPhones_",
          };
          java.lang.String info =
              "\u0000\u0005\u0000\u0000\u0001\u0005\u0005\u0000\u0002\u0000\u0001\f\u0002\u001b" +
              "\u0003\n\u0004\u001b\u0005\u0007";
          return newMessageInfo(DEFAULT_INSTANCE, info, objects);
      }
      // fall through
      case GET_DEFAULT_INSTANCE: {
        return DEFAULT_INSTANCE;
      }
      case GET_PARSER: {
        com.google.protobuf.Parser<com.halloapp.proto.server.PrivacyList> parser = PARSER;
        if (parser == null) {
          synchronized (com.halloapp.proto.server.PrivacyList.class) {
            parser = PARSER;
            if (parser == null) {
              parser =
                  new DefaultInstanceBasedParser<com.halloapp.proto.server.PrivacyList>(
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


  // @@protoc_insertion_point(class_scope:server.PrivacyList)
  private static final com.halloapp.proto.server.PrivacyList DEFAULT_INSTANCE;
  static {
    PrivacyList defaultInstance = new PrivacyList();
    // New instances are implicitly immutable so no need to make
    // immutable.
    DEFAULT_INSTANCE = defaultInstance;
    com.google.protobuf.GeneratedMessageLite.registerDefaultInstance(
      PrivacyList.class, defaultInstance);
  }

  public static com.halloapp.proto.server.PrivacyList getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static volatile com.google.protobuf.Parser<PrivacyList> PARSER;

  public static com.google.protobuf.Parser<PrivacyList> parser() {
    return DEFAULT_INSTANCE.getParserForType();
  }
}

