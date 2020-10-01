// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: server.proto

package com.halloapp.proto.server;

public interface GroupStanzaOrBuilder extends
    // @@protoc_insertion_point(interface_extends:server.GroupStanza)
    com.google.protobuf.MessageLiteOrBuilder {

  /**
   * <code>.server.GroupStanza.Action action = 1;</code>
   * @return The enum numeric value on the wire for action.
   */
  int getActionValue();
  /**
   * <code>.server.GroupStanza.Action action = 1;</code>
   * @return The action.
   */
  com.halloapp.proto.server.GroupStanza.Action getAction();

  /**
   * <code>string gid = 2;</code>
   * @return The gid.
   */
  java.lang.String getGid();
  /**
   * <code>string gid = 2;</code>
   * @return The bytes for gid.
   */
  com.google.protobuf.ByteString
      getGidBytes();

  /**
   * <code>string name = 3;</code>
   * @return The name.
   */
  java.lang.String getName();
  /**
   * <code>string name = 3;</code>
   * @return The bytes for name.
   */
  com.google.protobuf.ByteString
      getNameBytes();

  /**
   * <code>string avatar_id = 4;</code>
   * @return The avatarId.
   */
  java.lang.String getAvatarId();
  /**
   * <code>string avatar_id = 4;</code>
   * @return The bytes for avatarId.
   */
  com.google.protobuf.ByteString
      getAvatarIdBytes();

  /**
   * <code>int64 sender_uid = 5;</code>
   * @return The senderUid.
   */
  long getSenderUid();

  /**
   * <code>string sender_name = 6;</code>
   * @return The senderName.
   */
  java.lang.String getSenderName();
  /**
   * <code>string sender_name = 6;</code>
   * @return The bytes for senderName.
   */
  com.google.protobuf.ByteString
      getSenderNameBytes();

  /**
   * <code>repeated .server.GroupMember members = 7;</code>
   */
  java.util.List<com.halloapp.proto.server.GroupMember> 
      getMembersList();
  /**
   * <code>repeated .server.GroupMember members = 7;</code>
   */
  com.halloapp.proto.server.GroupMember getMembers(int index);
  /**
   * <code>repeated .server.GroupMember members = 7;</code>
   */
  int getMembersCount();
}
