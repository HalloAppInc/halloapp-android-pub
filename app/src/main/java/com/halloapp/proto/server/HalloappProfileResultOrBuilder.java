// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: server.proto

package com.halloapp.proto.server;

public interface HalloappProfileResultOrBuilder extends
    // @@protoc_insertion_point(interface_extends:server.HalloappProfileResult)
    com.google.protobuf.MessageLiteOrBuilder {

  /**
   * <code>.server.HalloappProfileResult.Result result = 1;</code>
   * @return The enum numeric value on the wire for result.
   */
  int getResultValue();
  /**
   * <code>.server.HalloappProfileResult.Result result = 1;</code>
   * @return The result.
   */
  com.halloapp.proto.server.HalloappProfileResult.Result getResult();

  /**
   * <code>.server.HalloappProfileResult.Reason reason = 2;</code>
   * @return The enum numeric value on the wire for reason.
   */
  int getReasonValue();
  /**
   * <code>.server.HalloappProfileResult.Reason reason = 2;</code>
   * @return The reason.
   */
  com.halloapp.proto.server.HalloappProfileResult.Reason getReason();

  /**
   * <code>.server.HalloappUserProfile profile = 3;</code>
   * @return Whether the profile field is set.
   */
  boolean hasProfile();
  /**
   * <code>.server.HalloappUserProfile profile = 3;</code>
   * @return The profile.
   */
  com.halloapp.proto.server.HalloappUserProfile getProfile();
}
