// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: server.proto

package com.halloapp.proto.server;

public interface MomentInfoOrBuilder extends
    // @@protoc_insertion_point(interface_extends:server.MomentInfo)
    com.google.protobuf.MessageLiteOrBuilder {

  /**
   * <pre>
   * timestamp received from the server in the notification.
   * </pre>
   *
   * <code>int64 notification_timestamp = 1;</code>
   * @return The notificationTimestamp.
   */
  long getNotificationTimestamp();

  /**
   * <pre>
   * Time taken on the moment camera to capture the moment.
   * </pre>
   *
   * <code>int64 time_taken = 2;</code>
   * @return The timeTaken.
   */
  long getTimeTaken();

  /**
   * <pre>
   * Number of times a moment has been captured.
   * </pre>
   *
   * <code>int64 num_takes = 3;</code>
   * @return The numTakes.
   */
  long getNumTakes();

  /**
   * <pre>
   * Number of times the selfie view has been captured.
   * </pre>
   *
   * <code>int64 num_selfie_takes = 4;</code>
   * @return The numSelfieTakes.
   */
  long getNumSelfieTakes();

  /**
   * <pre>
   * Id of the notification that resulted in the user posting this moment.
   * </pre>
   *
   * <code>int64 notification_id = 5;</code>
   * @return The notificationId.
   */
  long getNotificationId();
}
