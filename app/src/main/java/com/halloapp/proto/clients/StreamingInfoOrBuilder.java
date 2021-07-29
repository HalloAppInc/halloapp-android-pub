// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: clients.proto

package com.halloapp.proto.clients;

public interface StreamingInfoOrBuilder extends
    // @@protoc_insertion_point(interface_extends:clients.StreamingInfo)
    com.google.protobuf.MessageLiteOrBuilder {

  /**
   * <code>.clients.BlobVersion blob_version = 1;</code>
   * @return The enum numeric value on the wire for blobVersion.
   */
  int getBlobVersionValue();
  /**
   * <code>.clients.BlobVersion blob_version = 1;</code>
   * @return The blobVersion.
   */
  com.halloapp.proto.clients.BlobVersion getBlobVersion();

  /**
   * <code>int32 chunk_size = 2;</code>
   * @return The chunkSize.
   */
  int getChunkSize();

  /**
   * <code>int64 blob_size = 3;</code>
   * @return The blobSize.
   */
  long getBlobSize();
}
