// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: server.proto

package com.halloapp.proto.server;

public interface HashcashResponseOrBuilder extends
    // @@protoc_insertion_point(interface_extends:server.HashcashResponse)
    com.google.protobuf.MessageLiteOrBuilder {

  /**
   * <code>string hashcash_challenge = 1;</code>
   * @return The hashcashChallenge.
   */
  java.lang.String getHashcashChallenge();
  /**
   * <code>string hashcash_challenge = 1;</code>
   * @return The bytes for hashcashChallenge.
   */
  com.google.protobuf.ByteString
      getHashcashChallengeBytes();

  /**
   * <pre>
   * default is false
   * </pre>
   *
   * <code>bool is_phone_not_needed = 2;</code>
   * @return The isPhoneNotNeeded.
   */
  boolean getIsPhoneNotNeeded();
}
