// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: clients.proto

package com.halloapp.proto.clients;

public interface ImageOrBuilder extends
    // @@protoc_insertion_point(interface_extends:clients.Image)
    com.google.protobuf.MessageLiteOrBuilder {

  /**
   * <code>.clients.EncryptedResource img = 1;</code>
   * @return Whether the img field is set.
   */
  boolean hasImg();
  /**
   * <code>.clients.EncryptedResource img = 1;</code>
   * @return The img.
   */
  com.halloapp.proto.clients.EncryptedResource getImg();

  /**
   * <code>int32 width = 2;</code>
   * @return The width.
   */
  int getWidth();

  /**
   * <code>int32 height = 3;</code>
   * @return The height.
   */
  int getHeight();
}
