// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: server.proto

package com.halloapp.proto.server;

public interface ProfileUpdateOrBuilder extends
    // @@protoc_insertion_point(interface_extends:server.ProfileUpdate)
    com.google.protobuf.MessageLiteOrBuilder {

  /**
   * <code>.server.ProfileUpdate.Type type = 1;</code>
   * @return The enum numeric value on the wire for type.
   */
  int getTypeValue();
  /**
   * <code>.server.ProfileUpdate.Type type = 1;</code>
   * @return The type.
   */
  com.halloapp.proto.server.ProfileUpdate.Type getType();

  /**
   * <code>.server.BasicUserProfile profile = 2;</code>
   * @return Whether the profile field is set.
   */
  boolean hasProfile();
  /**
   * <code>.server.BasicUserProfile profile = 2;</code>
   * @return The profile.
   */
  com.halloapp.proto.server.BasicUserProfile getProfile();
}
