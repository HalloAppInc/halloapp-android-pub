// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: log_events.proto

package com.halloapp.proto.log_events;

public interface InviteRequestResultOrBuilder extends
    // @@protoc_insertion_point(interface_extends:server.InviteRequestResult)
    com.google.protobuf.MessageLiteOrBuilder {

  /**
   * <code>.server.InviteRequestResult.Type type = 1;</code>
   * @return The enum numeric value on the wire for type.
   */
  int getTypeValue();
  /**
   * <code>.server.InviteRequestResult.Type type = 1;</code>
   * @return The type.
   */
  com.halloapp.proto.log_events.InviteRequestResult.Type getType();

  /**
   * <code>string invited_phone = 2;</code>
   * @return The invitedPhone.
   */
  java.lang.String getInvitedPhone();
  /**
   * <code>string invited_phone = 2;</code>
   * @return The bytes for invitedPhone.
   */
  com.google.protobuf.ByteString
      getInvitedPhoneBytes();
}