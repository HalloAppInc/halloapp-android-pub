// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: clients.proto

package com.halloapp.proto.clients;

public interface EncryptedPayloadOrBuilder extends
    // @@protoc_insertion_point(interface_extends:clients.EncryptedPayload)
    com.google.protobuf.MessageLiteOrBuilder {

  /**
   * <code>bytes sender_state_encrypted_payload = 1;</code>
   * @return The senderStateEncryptedPayload.
   */
  com.google.protobuf.ByteString getSenderStateEncryptedPayload();

  /**
   * <code>bytes one_to_one_encrypted_payload = 2;</code>
   * @return The oneToOneEncryptedPayload.
   */
  com.google.protobuf.ByteString getOneToOneEncryptedPayload();

  /**
   * <code>bytes comment_key_encrypted_payload = 3;</code>
   * @return The commentKeyEncryptedPayload.
   */
  com.google.protobuf.ByteString getCommentKeyEncryptedPayload();

  public com.halloapp.proto.clients.EncryptedPayload.PayloadCase getPayloadCase();
}
