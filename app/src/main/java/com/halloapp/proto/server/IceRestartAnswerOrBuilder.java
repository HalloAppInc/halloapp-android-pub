// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: server.proto

package com.halloapp.proto.server;

public interface IceRestartAnswerOrBuilder extends
    // @@protoc_insertion_point(interface_extends:server.IceRestartAnswer)
    com.google.protobuf.MessageLiteOrBuilder {

  /**
   * <code>string call_id = 1;</code>
   * @return The callId.
   */
  java.lang.String getCallId();
  /**
   * <code>string call_id = 1;</code>
   * @return The bytes for callId.
   */
  com.google.protobuf.ByteString
      getCallIdBytes();

  /**
   * <code>int32 idx = 2;</code>
   * @return The idx.
   */
  int getIdx();

  /**
   * <code>.server.WebRtcSessionDescription webrtc_answer = 3;</code>
   * @return Whether the webrtcAnswer field is set.
   */
  boolean hasWebrtcAnswer();
  /**
   * <code>.server.WebRtcSessionDescription webrtc_answer = 3;</code>
   * @return The webrtcAnswer.
   */
  com.halloapp.proto.server.WebRtcSessionDescription getWebrtcAnswer();
}
