// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: clients.proto

package com.halloapp.proto.clients;

public interface ContactOrBuilder extends
    // @@protoc_insertion_point(interface_extends:clients.Contact)
    com.google.protobuf.MessageLiteOrBuilder {

  /**
   * <code>string name = 1;</code>
   * @return The name.
   */
  java.lang.String getName();
  /**
   * <code>string name = 1;</code>
   * @return The bytes for name.
   */
  com.google.protobuf.ByteString
      getNameBytes();

  /**
   * <code>repeated .clients.Phone phones = 2;</code>
   */
  java.util.List<com.halloapp.proto.clients.Phone> 
      getPhonesList();
  /**
   * <code>repeated .clients.Phone phones = 2;</code>
   */
  com.halloapp.proto.clients.Phone getPhones(int index);
  /**
   * <code>repeated .clients.Phone phones = 2;</code>
   */
  int getPhonesCount();
}
