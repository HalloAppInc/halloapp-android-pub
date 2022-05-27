// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: server.proto

package com.halloapp.proto.server;

public interface PostOrBuilder extends
    // @@protoc_insertion_point(interface_extends:server.Post)
    com.google.protobuf.MessageLiteOrBuilder {

  /**
   * <code>string id = 1;</code>
   * @return The id.
   */
  java.lang.String getId();
  /**
   * <code>string id = 1;</code>
   * @return The bytes for id.
   */
  com.google.protobuf.ByteString
      getIdBytes();

  /**
   * <code>int64 publisher_uid = 2;</code>
   * @return The publisherUid.
   */
  long getPublisherUid();

  /**
   * <code>bytes payload = 3;</code>
   * @return The payload.
   */
  com.google.protobuf.ByteString getPayload();

  /**
   * <code>.server.Audience audience = 4;</code>
   * @return Whether the audience field is set.
   */
  boolean hasAudience();
  /**
   * <code>.server.Audience audience = 4;</code>
   * @return The audience.
   */
  com.halloapp.proto.server.Audience getAudience();

  /**
   * <code>int64 timestamp = 5;</code>
   * @return The timestamp.
   */
  long getTimestamp();

  /**
   * <code>string publisher_name = 6;</code>
   * @return The publisherName.
   */
  java.lang.String getPublisherName();
  /**
   * <code>string publisher_name = 6;</code>
   * @return The bytes for publisherName.
   */
  com.google.protobuf.ByteString
      getPublisherNameBytes();

  /**
   * <pre>
   * Serialized EncryptedPayload (from client.proto).
   * </pre>
   *
   * <code>bytes enc_payload = 7;</code>
   * @return The encPayload.
   */
  com.google.protobuf.ByteString getEncPayload();

  /**
   * <code>.server.MediaCounters media_counters = 8;</code>
   * @return Whether the mediaCounters field is set.
   */
  boolean hasMediaCounters();
  /**
   * <code>.server.MediaCounters media_counters = 8;</code>
   * @return The mediaCounters.
   */
  com.halloapp.proto.server.MediaCounters getMediaCounters();

  /**
   * <code>.server.Post.Tag tag = 9;</code>
   * @return The enum numeric value on the wire for tag.
   */
  int getTagValue();
  /**
   * <code>.server.Post.Tag tag = 9;</code>
   * @return The tag.
   */
  com.halloapp.proto.server.Post.Tag getTag();

  /**
   * <code>string psa_tag = 10;</code>
   * @return The psaTag.
   */
  java.lang.String getPsaTag();
  /**
   * <code>string psa_tag = 10;</code>
   * @return The bytes for psaTag.
   */
  com.google.protobuf.ByteString
      getPsaTagBytes();
}
