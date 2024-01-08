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
   * <pre>
   * deprecated, use group_chat_stanza
   * </pre>
   *
   * <code>.server.GroupChat group_chat = 15 [deprecated = true];</code>
   * @deprecated server.Msg.group_chat is deprecated.
   *     See server.proto;l=1216
   * @return Whether the groupChat field is set.
   */
  @java.lang.Deprecated boolean hasGroupChat();
  /**
   * <pre>
   * deprecated, use group_chat_stanza
   * </pre>
   *
   * <code>.server.GroupChat group_chat = 15 [deprecated = true];</code>
   * @deprecated server.Msg.group_chat is deprecated.
   *     See server.proto;l=1216
   * @return The groupChat.
   */
  @java.lang.Deprecated com.halloapp.proto.server.GroupChat getGroupChat();

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
   * <code>.server.InviteeNotice invitee_notice = 27;</code>
   * @return Whether the inviteeNotice field is set.
   */
  boolean hasInviteeNotice();
  /**
   * <code>.server.InviteeNotice invitee_notice = 27;</code>
   * @return The inviteeNotice.
   */
  com.halloapp.proto.server.InviteeNotice getInviteeNotice();

  /**
   * <code>.server.GroupFeedRerequest group_feed_rerequest = 28;</code>
   * @return Whether the groupFeedRerequest field is set.
   */
  boolean hasGroupFeedRerequest();
  /**
   * <code>.server.GroupFeedRerequest group_feed_rerequest = 28;</code>
   * @return The groupFeedRerequest.
   */
  com.halloapp.proto.server.GroupFeedRerequest getGroupFeedRerequest();

  /**
   * <code>.server.HistoryResend history_resend = 29;</code>
   * @return Whether the historyResend field is set.
   */
  boolean hasHistoryResend();
  /**
   * <code>.server.HistoryResend history_resend = 29;</code>
   * @return The historyResend.
   */
  com.halloapp.proto.server.HistoryResend getHistoryResend();

  /**
   * <code>.server.PlayedReceipt played_receipt = 30;</code>
   * @return Whether the playedReceipt field is set.
   */
  boolean hasPlayedReceipt();
  /**
   * <code>.server.PlayedReceipt played_receipt = 30;</code>
   * @return The playedReceipt.
   */
  com.halloapp.proto.server.PlayedReceipt getPlayedReceipt();

  /**
   * <code>.server.RequestLogs request_logs = 31;</code>
   * @return Whether the requestLogs field is set.
   */
  boolean hasRequestLogs();
  /**
   * <code>.server.RequestLogs request_logs = 31;</code>
   * @return The requestLogs.
   */
  com.halloapp.proto.server.RequestLogs getRequestLogs();

  /**
   * <pre>
   * only for use with SMSApp clients
   * </pre>
   *
   * <code>.server.WakeUp wakeup = 32;</code>
   * @return Whether the wakeup field is set.
   */
  boolean hasWakeup();
  /**
   * <pre>
   * only for use with SMSApp clients
   * </pre>
   *
   * <code>.server.WakeUp wakeup = 32;</code>
   * @return The wakeup.
   */
  com.halloapp.proto.server.WakeUp getWakeup();

  /**
   * <code>.server.HomeFeedRerequest home_feed_rerequest = 33;</code>
   * @return Whether the homeFeedRerequest field is set.
   */
  boolean hasHomeFeedRerequest();
  /**
   * <code>.server.HomeFeedRerequest home_feed_rerequest = 33;</code>
   * @return The homeFeedRerequest.
   */
  com.halloapp.proto.server.HomeFeedRerequest getHomeFeedRerequest();

  /**
   * <code>.server.IncomingCall incoming_call = 34;</code>
   * @return Whether the incomingCall field is set.
   */
  boolean hasIncomingCall();
  /**
   * <code>.server.IncomingCall incoming_call = 34;</code>
   * @return The incomingCall.
   */
  com.halloapp.proto.server.IncomingCall getIncomingCall();

  /**
   * <code>.server.CallRinging call_ringing = 35;</code>
   * @return Whether the callRinging field is set.
   */
  boolean hasCallRinging();
  /**
   * <code>.server.CallRinging call_ringing = 35;</code>
   * @return The callRinging.
   */
  com.halloapp.proto.server.CallRinging getCallRinging();

  /**
   * <code>.server.AnswerCall answer_call = 36;</code>
   * @return Whether the answerCall field is set.
   */
  boolean hasAnswerCall();
  /**
   * <code>.server.AnswerCall answer_call = 36;</code>
   * @return The answerCall.
   */
  com.halloapp.proto.server.AnswerCall getAnswerCall();

  /**
   * <code>.server.EndCall end_call = 37;</code>
   * @return Whether the endCall field is set.
   */
  boolean hasEndCall();
  /**
   * <code>.server.EndCall end_call = 37;</code>
   * @return The endCall.
   */
  com.halloapp.proto.server.EndCall getEndCall();

  /**
   * <code>.server.IceCandidate ice_candidate = 38;</code>
   * @return Whether the iceCandidate field is set.
   */
  boolean hasIceCandidate();
  /**
   * <code>.server.IceCandidate ice_candidate = 38;</code>
   * @return The iceCandidate.
   */
  com.halloapp.proto.server.IceCandidate getIceCandidate();

  /**
   * <pre>
   * only for server use
   * </pre>
   *
   * <code>.server.MarketingAlert marketing_alert = 39;</code>
   * @return Whether the marketingAlert field is set.
   */
  boolean hasMarketingAlert();
  /**
   * <pre>
   * only for server use
   * </pre>
   *
   * <code>.server.MarketingAlert marketing_alert = 39;</code>
   * @return The marketingAlert.
   */
  com.halloapp.proto.server.MarketingAlert getMarketingAlert();

  /**
   * <pre>
   * deprecated, use call_sdp
   * </pre>
   *
   * <code>.server.IceRestartOffer ice_restart_offer = 40;</code>
   * @return Whether the iceRestartOffer field is set.
   */
  boolean hasIceRestartOffer();
  /**
   * <pre>
   * deprecated, use call_sdp
   * </pre>
   *
   * <code>.server.IceRestartOffer ice_restart_offer = 40;</code>
   * @return The iceRestartOffer.
   */
  com.halloapp.proto.server.IceRestartOffer getIceRestartOffer();

  /**
   * <pre>
   * deprecated, use call_sdp
   * </pre>
   *
   * <code>.server.IceRestartAnswer ice_restart_answer = 41;</code>
   * @return Whether the iceRestartAnswer field is set.
   */
  boolean hasIceRestartAnswer();
  /**
   * <pre>
   * deprecated, use call_sdp
   * </pre>
   *
   * <code>.server.IceRestartAnswer ice_restart_answer = 41;</code>
   * @return The iceRestartAnswer.
   */
  com.halloapp.proto.server.IceRestartAnswer getIceRestartAnswer();

  /**
   * <code>.server.GroupFeedHistory group_feed_history = 42;</code>
   * @return Whether the groupFeedHistory field is set.
   */
  boolean hasGroupFeedHistory();
  /**
   * <code>.server.GroupFeedHistory group_feed_history = 42;</code>
   * @return The groupFeedHistory.
   */
  com.halloapp.proto.server.GroupFeedHistory getGroupFeedHistory();

  /**
   * <pre>
   * deprecated, set answer in ringing
   * </pre>
   *
   * <code>.server.PreAnswerCall pre_answer_call = 43;</code>
   * @return Whether the preAnswerCall field is set.
   */
  boolean hasPreAnswerCall();
  /**
   * <pre>
   * deprecated, set answer in ringing
   * </pre>
   *
   * <code>.server.PreAnswerCall pre_answer_call = 43;</code>
   * @return The preAnswerCall.
   */
  com.halloapp.proto.server.PreAnswerCall getPreAnswerCall();

  /**
   * <code>.server.HoldCall hold_call = 44;</code>
   * @return Whether the holdCall field is set.
   */
  boolean hasHoldCall();
  /**
   * <code>.server.HoldCall hold_call = 44;</code>
   * @return The holdCall.
   */
  com.halloapp.proto.server.HoldCall getHoldCall();

  /**
   * <code>.server.MuteCall mute_call = 45;</code>
   * @return Whether the muteCall field is set.
   */
  boolean hasMuteCall();
  /**
   * <code>.server.MuteCall mute_call = 45;</code>
   * @return The muteCall.
   */
  com.halloapp.proto.server.MuteCall getMuteCall();

  /**
   * <code>.server.IncomingCallPush incoming_call_push = 46;</code>
   * @return Whether the incomingCallPush field is set.
   */
  boolean hasIncomingCallPush();
  /**
   * <code>.server.IncomingCallPush incoming_call_push = 46;</code>
   * @return The incomingCallPush.
   */
  com.halloapp.proto.server.IncomingCallPush getIncomingCallPush();

  /**
   * <code>.server.CallSdp call_sdp = 47;</code>
   * @return Whether the callSdp field is set.
   */
  boolean hasCallSdp();
  /**
   * <code>.server.CallSdp call_sdp = 47;</code>
   * @return The callSdp.
   */
  com.halloapp.proto.server.CallSdp getCallSdp();

  /**
   * <code>.server.WebStanza web_stanza = 48;</code>
   * @return Whether the webStanza field is set.
   */
  boolean hasWebStanza();
  /**
   * <code>.server.WebStanza web_stanza = 48;</code>
   * @return The webStanza.
   */
  com.halloapp.proto.server.WebStanza getWebStanza();

  /**
   * <code>.server.ContentMissing content_missing = 49;</code>
   * @return Whether the contentMissing field is set.
   */
  boolean hasContentMissing();
  /**
   * <code>.server.ContentMissing content_missing = 49;</code>
   * @return The contentMissing.
   */
  com.halloapp.proto.server.ContentMissing getContentMissing();

  /**
   * <code>.server.ScreenshotReceipt screenshot_receipt = 50;</code>
   * @return Whether the screenshotReceipt field is set.
   */
  boolean hasScreenshotReceipt();
  /**
   * <code>.server.ScreenshotReceipt screenshot_receipt = 50;</code>
   * @return The screenshotReceipt.
   */
  com.halloapp.proto.server.ScreenshotReceipt getScreenshotReceipt();

  /**
   * <code>.server.SavedReceipt saved_receipt = 51;</code>
   * @return Whether the savedReceipt field is set.
   */
  boolean hasSavedReceipt();
  /**
   * <code>.server.SavedReceipt saved_receipt = 51;</code>
   * @return The savedReceipt.
   */
  com.halloapp.proto.server.SavedReceipt getSavedReceipt();

  /**
   * <code>.server.GroupChatStanza group_chat_stanza = 52;</code>
   * @return Whether the groupChatStanza field is set.
   */
  boolean hasGroupChatStanza();
  /**
   * <code>.server.GroupChatStanza group_chat_stanza = 52;</code>
   * @return The groupChatStanza.
   */
  com.halloapp.proto.server.GroupChatStanza getGroupChatStanza();

  /**
   * <code>.server.MomentNotification moment_notification = 53;</code>
   * @return Whether the momentNotification field is set.
   */
  boolean hasMomentNotification();
  /**
   * <code>.server.MomentNotification moment_notification = 53;</code>
   * @return The momentNotification.
   */
  com.halloapp.proto.server.MomentNotification getMomentNotification();

  /**
   * <code>.server.ProfileUpdate profile_update = 54;</code>
   * @return Whether the profileUpdate field is set.
   */
  boolean hasProfileUpdate();
  /**
   * <code>.server.ProfileUpdate profile_update = 54;</code>
   * @return The profileUpdate.
   */
  com.halloapp.proto.server.ProfileUpdate getProfileUpdate();

  /**
   * <code>.server.PublicFeedUpdate public_feed_update = 55;</code>
   * @return Whether the publicFeedUpdate field is set.
   */
  boolean hasPublicFeedUpdate();
  /**
   * <code>.server.PublicFeedUpdate public_feed_update = 55;</code>
   * @return The publicFeedUpdate.
   */
  com.halloapp.proto.server.PublicFeedUpdate getPublicFeedUpdate();

  /**
   * <code>.server.AiImage ai_image = 56;</code>
   * @return Whether the aiImage field is set.
   */
  boolean hasAiImage();
  /**
   * <code>.server.AiImage ai_image = 56;</code>
   * @return The aiImage.
   */
  com.halloapp.proto.server.AiImage getAiImage();

  /**
   * <code>.server.HalloappProfileUpdate halloapp_profile_update = 57;</code>
   * @return Whether the halloappProfileUpdate field is set.
   */
  boolean hasHalloappProfileUpdate();
  /**
   * <code>.server.HalloappProfileUpdate halloapp_profile_update = 57;</code>
   * @return The halloappProfileUpdate.
   */
  com.halloapp.proto.server.HalloappProfileUpdate getHalloappProfileUpdate();

  /**
   * <code>.server.Album album = 58;</code>
   * @return Whether the album field is set.
   */
  boolean hasAlbum();
  /**
   * <code>.server.Album album = 58;</code>
   * @return The album.
   */
  com.halloapp.proto.server.Album getAlbum();

  /**
   * <code>.server.FriendListRequest friend_list_request = 59;</code>
   * @return Whether the friendListRequest field is set.
   */
  boolean hasFriendListRequest();
  /**
   * <code>.server.FriendListRequest friend_list_request = 59;</code>
   * @return The friendListRequest.
   */
  com.halloapp.proto.server.FriendListRequest getFriendListRequest();

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
