// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: server.proto

package com.halloapp.proto.server;

public interface AuthRequestOrBuilder extends
    // @@protoc_insertion_point(interface_extends:server.AuthRequest)
    com.google.protobuf.MessageLiteOrBuilder {

  /**
   * <code>int64 uid = 1;</code>
   * @return The uid.
   */
  long getUid();

  /**
   * <code>string pwd = 2;</code>
   * @return The pwd.
   */
  java.lang.String getPwd();
  /**
   * <code>string pwd = 2;</code>
   * @return The bytes for pwd.
   */
  com.google.protobuf.ByteString
      getPwdBytes();

  /**
   * <code>.server.ClientMode client_mode = 3;</code>
   * @return Whether the clientMode field is set.
   */
  boolean hasClientMode();
  /**
   * <code>.server.ClientMode client_mode = 3;</code>
   * @return The clientMode.
   */
  com.halloapp.proto.server.ClientMode getClientMode();

  /**
   * <code>.server.ClientVersion client_version = 4;</code>
   * @return Whether the clientVersion field is set.
   */
  boolean hasClientVersion();
  /**
   * <code>.server.ClientVersion client_version = 4;</code>
   * @return The clientVersion.
   */
  com.halloapp.proto.server.ClientVersion getClientVersion();

  /**
   * <code>string resource = 5;</code>
   * @return The resource.
   */
  java.lang.String getResource();
  /**
   * <code>string resource = 5;</code>
   * @return The bytes for resource.
   */
  com.google.protobuf.ByteString
      getResourceBytes();
}
