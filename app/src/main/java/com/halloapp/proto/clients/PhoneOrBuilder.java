// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: clients.proto

package com.halloapp.proto.clients;

public interface PhoneOrBuilder extends
    // @@protoc_insertion_point(interface_extends:clients.Phone)
    com.google.protobuf.MessageLiteOrBuilder {

  /**
   * <code>.clients.PhoneType type = 1;</code>
   * @return The enum numeric value on the wire for type.
   */
  int getTypeValue();
  /**
   * <code>.clients.PhoneType type = 1;</code>
   * @return The type.
   */
  com.halloapp.proto.clients.PhoneType getType();

  /**
   * <code>string number = 2;</code>
   * @return The number.
   */
  java.lang.String getNumber();
  /**
   * <code>string number = 2;</code>
   * @return The bytes for number.
   */
  com.google.protobuf.ByteString
      getNumberBytes();
}
