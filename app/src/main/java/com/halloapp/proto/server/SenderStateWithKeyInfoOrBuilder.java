// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: server.proto

package com.halloapp.proto.server;

public interface SenderStateWithKeyInfoOrBuilder extends
    // @@protoc_insertion_point(interface_extends:server.SenderStateWithKeyInfo)
    com.google.protobuf.MessageLiteOrBuilder {

  /**
   * <code>bytes public_key = 1;</code>
   * @return The publicKey.
   */
  com.google.protobuf.ByteString getPublicKey();

  /**
   * <code>int64 one_time_pre_key_id = 2;</code>
   * @return The oneTimePreKeyId.
   */
  long getOneTimePreKeyId();

  /**
   * <code>bytes enc_sender_state = 3;</code>
   * @return The encSenderState.
   */
  com.google.protobuf.ByteString getEncSenderState();
}