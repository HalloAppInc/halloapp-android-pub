// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: server.proto

package com.halloapp.proto.server;

public interface CountOrBuilder extends
    // @@protoc_insertion_point(interface_extends:server.Count)
    com.google.protobuf.MessageLiteOrBuilder {

  /**
   * <code>string namespace = 1;</code>
   * @return The namespace.
   */
  java.lang.String getNamespace();
  /**
   * <code>string namespace = 1;</code>
   * @return The bytes for namespace.
   */
  com.google.protobuf.ByteString
      getNamespaceBytes();

  /**
   * <code>string metric = 2;</code>
   * @return The metric.
   */
  java.lang.String getMetric();
  /**
   * <code>string metric = 2;</code>
   * @return The bytes for metric.
   */
  com.google.protobuf.ByteString
      getMetricBytes();

  /**
   * <code>int64 count = 3;</code>
   * @return The count.
   */
  long getCount();

  /**
   * <code>repeated .server.Dim dims = 4;</code>
   */
  java.util.List<com.halloapp.proto.server.Dim> 
      getDimsList();
  /**
   * <code>repeated .server.Dim dims = 4;</code>
   */
  com.halloapp.proto.server.Dim getDims(int index);
  /**
   * <code>repeated .server.Dim dims = 4;</code>
   */
  int getDimsCount();
}
