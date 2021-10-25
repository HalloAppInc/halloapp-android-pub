// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: server.proto

package com.halloapp.proto.server;

public interface OtpRequestOrBuilder extends
    // @@protoc_insertion_point(interface_extends:server.OtpRequest)
    com.google.protobuf.MessageLiteOrBuilder {

  /**
   * <code>string phone = 1;</code>
   * @return The phone.
   */
  java.lang.String getPhone();
  /**
   * <code>string phone = 1;</code>
   * @return The bytes for phone.
   */
  com.google.protobuf.ByteString
      getPhoneBytes();

  /**
   * <code>.server.OtpRequest.Method method = 2;</code>
   * @return The enum numeric value on the wire for method.
   */
  int getMethodValue();
  /**
   * <code>.server.OtpRequest.Method method = 2;</code>
   * @return The method.
   */
  com.halloapp.proto.server.OtpRequest.Method getMethod();

  /**
   * <code>string lang_id = 3;</code>
   * @return The langId.
   */
  java.lang.String getLangId();
  /**
   * <code>string lang_id = 3;</code>
   * @return The bytes for langId.
   */
  com.google.protobuf.ByteString
      getLangIdBytes();

  /**
   * <code>string group_invite_token = 4;</code>
   * @return The groupInviteToken.
   */
  java.lang.String getGroupInviteToken();
  /**
   * <code>string group_invite_token = 4;</code>
   * @return The bytes for groupInviteToken.
   */
  com.google.protobuf.ByteString
      getGroupInviteTokenBytes();

  /**
   * <code>string user_agent = 5;</code>
   * @return The userAgent.
   */
  java.lang.String getUserAgent();
  /**
   * <code>string user_agent = 5;</code>
   * @return The bytes for userAgent.
   */
  com.google.protobuf.ByteString
      getUserAgentBytes();

  /**
   * <code>string hashcash_solution = 6;</code>
   * @return The hashcashSolution.
   */
  java.lang.String getHashcashSolution();
  /**
   * <code>string hashcash_solution = 6;</code>
   * @return The bytes for hashcashSolution.
   */
  com.google.protobuf.ByteString
      getHashcashSolutionBytes();

  /**
   * <code>int64 hashcash_solution_time_taken_ms = 7;</code>
   * @return The hashcashSolutionTimeTakenMs.
   */
  long getHashcashSolutionTimeTakenMs();
}
