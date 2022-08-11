// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: log_events.proto

package com.halloapp.proto.log_events;

public interface StreamStatsOrBuilder extends
    // @@protoc_insertion_point(interface_extends:server.StreamStats)
    com.google.protobuf.MessageLiteOrBuilder {

  /**
   * <pre>
   *outgoing
   * </pre>
   *
   * <code>uint64 packetsSent = 1;</code>
   * @return The packetsSent.
   */
  long getPacketsSent();

  /**
   * <pre>
   * incoming
   * </pre>
   *
   * <code>uint64 packetsLost = 2;</code>
   * @return The packetsLost.
   */
  long getPacketsLost();

  /**
   * <code>uint64 packetsReceived = 3;</code>
   * @return The packetsReceived.
   */
  long getPacketsReceived();

  /**
   * <code>uint64 bytesReceived = 4;</code>
   * @return The bytesReceived.
   */
  long getBytesReceived();

  /**
   * <pre>
   *jitter stats - incoming quality
   * </pre>
   *
   * <code>double jitter = 5;</code>
   * @return The jitter.
   */
  double getJitter();

  /**
   * <code>double jitterBufferDelay = 6;</code>
   * @return The jitterBufferDelay.
   */
  double getJitterBufferDelay();

  /**
   * <code>uint64 jitterBufferEmittedCount = 7;</code>
   * @return The jitterBufferEmittedCount.
   */
  long getJitterBufferEmittedCount();

  /**
   * <code>double jitterBufferMinimumDelay = 8;</code>
   * @return The jitterBufferMinimumDelay.
   */
  double getJitterBufferMinimumDelay();
}