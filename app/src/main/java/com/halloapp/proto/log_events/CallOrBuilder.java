// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: log_events.proto

package com.halloapp.proto.log_events;

public interface CallOrBuilder extends
    // @@protoc_insertion_point(interface_extends:server.Call)
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
   * <code>uint64 peer_uid = 2;</code>
   * @return The peerUid.
   */
  long getPeerUid();

  /**
   * <code>.server.Call.CallType type = 3;</code>
   * @return The enum numeric value on the wire for type.
   */
  int getTypeValue();
  /**
   * <code>.server.Call.CallType type = 3;</code>
   * @return The type.
   */
  com.halloapp.proto.log_events.Call.CallType getType();

  /**
   * <code>.server.Call.CallDirection direction = 4;</code>
   * @return The enum numeric value on the wire for direction.
   */
  int getDirectionValue();
  /**
   * <code>.server.Call.CallDirection direction = 4;</code>
   * @return The direction.
   */
  com.halloapp.proto.log_events.Call.CallDirection getDirection();

  /**
   * <pre>
   * true if the call was answered
   * </pre>
   *
   * <code>bool answered = 5;</code>
   * @return The answered.
   */
  boolean getAnswered();

  /**
   * <pre>
   * true if the webrtc connects successful
   * </pre>
   *
   * <code>bool connected = 6;</code>
   * @return The connected.
   */
  boolean getConnected();

  /**
   * <pre>
   * number of ms this call was in the in-call state
   * </pre>
   *
   * <code>uint64 duration_ms = 7;</code>
   * @return The durationMs.
   */
  long getDurationMs();

  /**
   * <pre>
   * string representation of the end call reason as defined in the EndCall.Reason
   * </pre>
   *
   * <code>string end_call_reason = 9;</code>
   * @return The endCallReason.
   */
  java.lang.String getEndCallReason();
  /**
   * <pre>
   * string representation of the end call reason as defined in the EndCall.Reason
   * </pre>
   *
   * <code>string end_call_reason = 9;</code>
   * @return The bytes for endCallReason.
   */
  com.google.protobuf.ByteString
      getEndCallReasonBytes();

  /**
   * <pre>
   * true if the end call happened locally, or false if the end call was received remotely
   * </pre>
   *
   * <code>bool local_end_call = 10;</code>
   * @return The localEndCall.
   */
  boolean getLocalEndCall();

  /**
   * <pre>
   * wifi or cellular
   * </pre>
   *
   * <code>.server.Call.NetworkType network_type = 11;</code>
   * @return The enum numeric value on the wire for networkType.
   */
  int getNetworkTypeValue();
  /**
   * <pre>
   * wifi or cellular
   * </pre>
   *
   * <code>.server.Call.NetworkType network_type = 11;</code>
   * @return The networkType.
   */
  com.halloapp.proto.log_events.Call.NetworkType getNetworkType();

  /**
   * <pre>
   * json serialized version of the result of peer_connection.get_stats at the end of
   * </pre>
   *
   * <code>string webrtc_stats = 20;</code>
   * @return The webrtcStats.
   */
  java.lang.String getWebrtcStats();
  /**
   * <pre>
   * json serialized version of the result of peer_connection.get_stats at the end of
   * </pre>
   *
   * <code>string webrtc_stats = 20;</code>
   * @return The bytes for webrtcStats.
   */
  com.google.protobuf.ByteString
      getWebrtcStatsBytes();
}
