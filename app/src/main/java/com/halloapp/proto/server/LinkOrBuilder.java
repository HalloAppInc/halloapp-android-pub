// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: server.proto

package com.halloapp.proto.server;

public interface LinkOrBuilder extends
    // @@protoc_insertion_point(interface_extends:server.Link)
    com.google.protobuf.MessageLiteOrBuilder {

  /**
   * <code>.server.Link.Type type = 1;</code>
   * @return The enum numeric value on the wire for type.
   */
  int getTypeValue();
  /**
   * <code>.server.Link.Type type = 1;</code>
   * @return The type.
   */
  com.halloapp.proto.server.Link.Type getType();

  /**
   * <code>string text = 2;</code>
   * @return The text.
   */
  java.lang.String getText();
  /**
   * <code>string text = 2;</code>
   * @return The bytes for text.
   */
  com.google.protobuf.ByteString
      getTextBytes();
}
