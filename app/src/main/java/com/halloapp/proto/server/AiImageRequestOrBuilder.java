// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: server.proto

package com.halloapp.proto.server;

public interface AiImageRequestOrBuilder extends
    // @@protoc_insertion_point(interface_extends:server.AiImageRequest)
    com.google.protobuf.MessageLiteOrBuilder {

  /**
   * <code>string text = 1;</code>
   * @return The text.
   */
  java.lang.String getText();
  /**
   * <code>string text = 1;</code>
   * @return The bytes for text.
   */
  com.google.protobuf.ByteString
      getTextBytes();

  /**
   * <code>int64 num_images = 2;</code>
   * @return The numImages.
   */
  long getNumImages();
}
