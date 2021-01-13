// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: server.proto

package com.halloapp.proto.server;

public interface MsgOrBuilder extends
    // @@protoc_insertion_point(interface_extends:server.Msg)
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
   * <code>.server.Msg.Type type = 2;</code>
   * @return The enum numeric value on the wire for type.
   */
  int getTypeValue();
  /**
   * <code>.server.Msg.Type type = 2;</code>
   * @return The type.
   */
  com.halloapp.proto.server.Msg.Type getType();

  /**
   * <code>int64 to_uid = 3;</code>
   * @return The toUid.
   */
  long getToUid();

  /**
   * <code>int64 from_uid = 4;</code>
   * @return The fromUid.
   */
  long getFromUid();

  /**
   * <code>.server.ContactList contact_list = 5;</code>
   * @return Whether the contactList field is set.
   */
  boolean hasContactList();
  /**
   * <code>.server.ContactList contact_list = 5;</code>
   * @return The contactList.
   */
  com.halloapp.proto.server.ContactList getContactList();

  /**
   * <code>.server.Avatar avatar = 6;</code>
   * @return Whether the avatar field is set.
   */
  boolean hasAvatar();
  /**
   * <code>.server.Avatar avatar = 6;</code>
   * @return The avatar.
   */
  com.halloapp.proto.server.Avatar getAvatar();

  /**
   * <code>.server.WhisperKeys whisper_keys = 7;</code>
   * @return Whether the whisperKeys field is set.
   */
  boolean hasWhisperKeys();
  /**
   * <code>.server.WhisperKeys whisper_keys = 7;</code>
   * @return The whisperKeys.
   */
  com.halloapp.proto.server.WhisperKeys getWhisperKeys();

  /**
   * <code>.server.SeenReceipt seen_receipt = 8;</code>
   * @return Whether the seenReceipt field is set.
   */
  boolean hasSeenReceipt();
  /**
   * <code>.server.SeenReceipt seen_receipt = 8;</code>
   * @return The seenReceipt.
   */
  com.halloapp.proto.server.SeenReceipt getSeenReceipt();

  /**
   * <code>.server.DeliveryReceipt delivery_receipt = 9;</code>
   * @return Whether the deliveryReceipt field is set.
   */
  boolean hasDeliveryReceipt();
  /**
   * <code>.server.DeliveryReceipt delivery_receipt = 9;</code>
   * @return The deliveryReceipt.
   */
  com.halloapp.proto.server.DeliveryReceipt getDeliveryReceipt();

  /**
   * <code>.server.ChatStanza chat_stanza = 10;</code>
   * @return Whether the chatStanza field is set.
   */
  boolean hasChatStanza();
  /**
   * <code>.server.ChatStanza chat_stanza = 10;</code>
   * @return The chatStanza.
   */
  com.halloapp.proto.server.ChatStanza getChatStanza();

  /**
   * <code>.server.FeedItem feed_item = 11;</code>
   * @return Whether the feedItem field is set.
   */
  boolean hasFeedItem();
  /**
   * <code>.server.FeedItem feed_item = 11;</code>
   * @return The feedItem.
   */
  com.halloapp.proto.server.FeedItem getFeedItem();

  /**
   * <code>.server.FeedItems feed_items = 12;</code>
   * @return Whether the feedItems field is set.
   */
  boolean hasFeedItems();
  /**
   * <code>.server.FeedItems feed_items = 12;</code>
   * @return The feedItems.
   */
  com.halloapp.proto.server.FeedItems getFeedItems();

  /**
   * <code>.server.ContactHash contact_hash = 13;</code>
   * @return Whether the contactHash field is set.
   */
  boolean hasContactHash();
  /**
   * <code>.server.ContactHash contact_hash = 13;</code>
   * @return The contactHash.
   */
  com.halloapp.proto.server.ContactHash getContactHash();

  /**
   * <code>.server.GroupStanza group_stanza = 14;</code>
   * @return Whether the groupStanza field is set.
   */
  boolean hasGroupStanza();
  /**
   * <code>.server.GroupStanza group_stanza = 14;</code>
   * @return The groupStanza.
   */
  com.halloapp.proto.server.GroupStanza getGroupStanza();

  /**
   * <code>.server.GroupChat group_chat = 15;</code>
   * @return Whether the groupChat field is set.
   */
  boolean hasGroupChat();
  /**
   * <code>.server.GroupChat group_chat = 15;</code>
   * @return The groupChat.
   */
  com.halloapp.proto.server.GroupChat getGroupChat();

  /**
   * <code>.server.Name name = 16;</code>
   * @return Whether the name field is set.
   */
  boolean hasName();
  /**
   * <code>.server.Name name = 16;</code>
   * @return The name.
   */
  com.halloapp.proto.server.Name getName();

  /**
   * <code>.server.ErrorStanza error_stanza = 17;</code>
   * @return Whether the errorStanza field is set.
   */
  boolean hasErrorStanza();
  /**
   * <code>.server.ErrorStanza error_stanza = 17;</code>
   * @return The errorStanza.
   */
  com.halloapp.proto.server.ErrorStanza getErrorStanza();

  /**
   * <code>.server.GroupChatRetract groupchat_retract = 18;</code>
   * @return Whether the groupchatRetract field is set.
   */
  boolean hasGroupchatRetract();
  /**
   * <code>.server.GroupChatRetract groupchat_retract = 18;</code>
   * @return The groupchatRetract.
   */
  com.halloapp.proto.server.GroupChatRetract getGroupchatRetract();

  /**
   * <code>.server.ChatRetract chat_retract = 19;</code>
   * @return Whether the chatRetract field is set.
   */
  boolean hasChatRetract();
  /**
   * <code>.server.ChatRetract chat_retract = 19;</code>
   * @return The chatRetract.
   */
  com.halloapp.proto.server.ChatRetract getChatRetract();

  /**
   * <code>.server.GroupFeedItem group_feed_item = 20;</code>
   * @return Whether the groupFeedItem field is set.
   */
  boolean hasGroupFeedItem();
  /**
   * <code>.server.GroupFeedItem group_feed_item = 20;</code>
   * @return The groupFeedItem.
   */
  com.halloapp.proto.server.GroupFeedItem getGroupFeedItem();

  /**
   * <code>.server.Rerequest rerequest = 22;</code>
   * @return Whether the rerequest field is set.
   */
  boolean hasRerequest();
  /**
   * <code>.server.Rerequest rerequest = 22;</code>
   * @return The rerequest.
   */
  com.halloapp.proto.server.Rerequest getRerequest();

  /**
   * <code>.server.SilentChatStanza silent_chat_stanza = 23;</code>
   * @return Whether the silentChatStanza field is set.
   */
  boolean hasSilentChatStanza();
  /**
   * <code>.server.SilentChatStanza silent_chat_stanza = 23;</code>
   * @return The silentChatStanza.
   */
  com.halloapp.proto.server.SilentChatStanza getSilentChatStanza();

  /**
   * <code>.server.GroupFeedItems group_feed_items = 24;</code>
   * @return Whether the groupFeedItems field is set.
   */
  boolean hasGroupFeedItems();
  /**
   * <code>.server.GroupFeedItems group_feed_items = 24;</code>
   * @return The groupFeedItems.
   */
  com.halloapp.proto.server.GroupFeedItems getGroupFeedItems();

  /**
   * <code>.server.EndOfQueue end_of_queue = 26;</code>
   * @return Whether the endOfQueue field is set.
   */
  boolean hasEndOfQueue();
  /**
   * <code>.server.EndOfQueue end_of_queue = 26;</code>
   * @return The endOfQueue.
   */
  com.halloapp.proto.server.EndOfQueue getEndOfQueue();

  /**
   * <code>int32 retry_count = 21;</code>
   * @return The retryCount.
   */
  int getRetryCount();

  /**
   * <code>int32 rerequest_count = 25;</code>
   * @return The rerequestCount.
   */
  int getRerequestCount();

  public com.halloapp.proto.server.Msg.PayloadCase getPayloadCase();
}
