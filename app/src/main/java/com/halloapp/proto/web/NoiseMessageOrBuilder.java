// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: web.proto

package com.halloapp.proto.web;

public interface NoiseMessageOrBuilder extends
    // @@protoc_insertion_point(interface_extends:web.NoiseMessage)
    com.google.protobuf.MessageLiteOrBuilder {

  /**
   * <code>.web.NoiseMessage.MessageType message_type = 1;</code>
   * @return The enum numeric value on the wire for messageType.
   */
  int getMessageTypeValue();
  /**
   * <code>.web.NoiseMessage.MessageType message_type = 1;</code>
   * @return The messageType.
   */
  com.halloapp.proto.web.NoiseMessage.MessageType getMessageType();

  /**
   * <code>bytes content = 2;</code>
   * @return The content.
   */
  com.google.protobuf.ByteString getContent();
}
