// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: server.proto

package com.halloapp.proto.server;

public interface PrivacyListOrBuilder extends
    // @@protoc_insertion_point(interface_extends:server.PrivacyList)
    com.google.protobuf.MessageLiteOrBuilder {

  /**
   * <code>.server.PrivacyList.Type type = 1;</code>
   * @return The enum numeric value on the wire for type.
   */
  int getTypeValue();
  /**
   * <code>.server.PrivacyList.Type type = 1;</code>
   * @return The type.
   */
  com.halloapp.proto.server.PrivacyList.Type getType();

  /**
   * <code>repeated .server.UidElement uid_elements = 2;</code>
   */
  java.util.List<com.halloapp.proto.server.UidElement> 
      getUidElementsList();
  /**
   * <code>repeated .server.UidElement uid_elements = 2;</code>
   */
  com.halloapp.proto.server.UidElement getUidElements(int index);
  /**
   * <code>repeated .server.UidElement uid_elements = 2;</code>
   */
  int getUidElementsCount();

  /**
   * <code>bytes hash = 3;</code>
   * @return The hash.
   */
  com.google.protobuf.ByteString getHash();
}
