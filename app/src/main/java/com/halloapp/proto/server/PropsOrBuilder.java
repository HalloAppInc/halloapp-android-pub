// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: server.proto

package com.halloapp.proto.server;

public interface PropsOrBuilder extends
    // @@protoc_insertion_point(interface_extends:server.Props)
    com.google.protobuf.MessageLiteOrBuilder {

  /**
   * <code>bytes hash = 1;</code>
   * @return The hash.
   */
  com.google.protobuf.ByteString getHash();

  /**
   * <code>repeated .server.Prop props = 2;</code>
   */
  java.util.List<com.halloapp.proto.server.Prop> 
      getPropsList();
  /**
   * <code>repeated .server.Prop props = 2;</code>
   */
  com.halloapp.proto.server.Prop getProps(int index);
  /**
   * <code>repeated .server.Prop props = 2;</code>
   */
  int getPropsCount();
}
