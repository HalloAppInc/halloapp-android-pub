// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: server.proto

package com.halloapp.proto.server;

public interface GetCallServersResultOrBuilder extends
    // @@protoc_insertion_point(interface_extends:server.GetCallServersResult)
    com.google.protobuf.MessageLiteOrBuilder {

  /**
   * <code>.server.GetCallServersResult.Result result = 1;</code>
   * @return The enum numeric value on the wire for result.
   */
  int getResultValue();
  /**
   * <code>.server.GetCallServersResult.Result result = 1;</code>
   * @return The result.
   */
  com.halloapp.proto.server.GetCallServersResult.Result getResult();

  /**
   * <code>repeated .server.StunServer stun_servers = 2;</code>
   */
  java.util.List<com.halloapp.proto.server.StunServer> 
      getStunServersList();
  /**
   * <code>repeated .server.StunServer stun_servers = 2;</code>
   */
  com.halloapp.proto.server.StunServer getStunServers(int index);
  /**
   * <code>repeated .server.StunServer stun_servers = 2;</code>
   */
  int getStunServersCount();

  /**
   * <code>repeated .server.TurnServer turn_servers = 3;</code>
   */
  java.util.List<com.halloapp.proto.server.TurnServer> 
      getTurnServersList();
  /**
   * <code>repeated .server.TurnServer turn_servers = 3;</code>
   */
  com.halloapp.proto.server.TurnServer getTurnServers(int index);
  /**
   * <code>repeated .server.TurnServer turn_servers = 3;</code>
   */
  int getTurnServersCount();
}
