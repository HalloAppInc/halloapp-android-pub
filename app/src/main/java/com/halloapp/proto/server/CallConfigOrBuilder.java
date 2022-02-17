// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: server.proto

package com.halloapp.proto.server;

public interface CallConfigOrBuilder extends
    // @@protoc_insertion_point(interface_extends:server.CallConfig)
    com.google.protobuf.MessageLiteOrBuilder {

  /**
   * <code>int32 audio_bitrate_max = 1;</code>
   * @return The audioBitrateMax.
   */
  int getAudioBitrateMax();

  /**
   * <code>int32 video_bitrate_max = 2;</code>
   * @return The videoBitrateMax.
   */
  int getVideoBitrateMax();

  /**
   * <code>int32 audio_codec = 3;</code>
   * @return The audioCodec.
   */
  int getAudioCodec();

  /**
   * <code>int32 video_codec = 4;</code>
   * @return The videoCodec.
   */
  int getVideoCodec();

  /**
   * <code>int32 video_width = 5;</code>
   * @return The videoWidth.
   */
  int getVideoWidth();

  /**
   * <code>int32 video_height = 6;</code>
   * @return The videoHeight.
   */
  int getVideoHeight();

  /**
   * <code>int32 video_fps = 7;</code>
   * @return The videoFps.
   */
  int getVideoFps();

  /**
   * <code>int32 audio_jitter_buffer_max_packets = 8;</code>
   * @return The audioJitterBufferMaxPackets.
   */
  int getAudioJitterBufferMaxPackets();

  /**
   * <code>bool audio_jitter_buffer_fast_accelerate = 9;</code>
   * @return The audioJitterBufferFastAccelerate.
   */
  boolean getAudioJitterBufferFastAccelerate();
}