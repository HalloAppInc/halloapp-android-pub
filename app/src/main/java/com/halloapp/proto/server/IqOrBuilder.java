// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: server.proto

package com.halloapp.proto.server;

public interface IqOrBuilder extends
    // @@protoc_insertion_point(interface_extends:server.Iq)
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
   * <code>.server.Iq.Type type = 2;</code>
   * @return The enum numeric value on the wire for type.
   */
  int getTypeValue();
  /**
   * <code>.server.Iq.Type type = 2;</code>
   * @return The type.
   */
  com.halloapp.proto.server.Iq.Type getType();

  /**
   * <code>.server.UploadMedia upload_media = 3;</code>
   * @return Whether the uploadMedia field is set.
   */
  boolean hasUploadMedia();
  /**
   * <code>.server.UploadMedia upload_media = 3;</code>
   * @return The uploadMedia.
   */
  com.halloapp.proto.server.UploadMedia getUploadMedia();

  /**
   * <code>.server.ContactList contact_list = 4;</code>
   * @return Whether the contactList field is set.
   */
  boolean hasContactList();
  /**
   * <code>.server.ContactList contact_list = 4;</code>
   * @return The contactList.
   */
  com.halloapp.proto.server.ContactList getContactList();

  /**
   * <code>.server.UploadAvatar upload_avatar = 5;</code>
   * @return Whether the uploadAvatar field is set.
   */
  boolean hasUploadAvatar();
  /**
   * <code>.server.UploadAvatar upload_avatar = 5;</code>
   * @return The uploadAvatar.
   */
  com.halloapp.proto.server.UploadAvatar getUploadAvatar();

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
   * <code>.server.Avatars avatars = 7;</code>
   * @return Whether the avatars field is set.
   */
  boolean hasAvatars();
  /**
   * <code>.server.Avatars avatars = 7;</code>
   * @return The avatars.
   */
  com.halloapp.proto.server.Avatars getAvatars();

  /**
   * <code>.server.ClientMode client_mode = 8;</code>
   * @return Whether the clientMode field is set.
   */
  boolean hasClientMode();
  /**
   * <code>.server.ClientMode client_mode = 8;</code>
   * @return The clientMode.
   */
  com.halloapp.proto.server.ClientMode getClientMode();

  /**
   * <code>.server.ClientVersion client_version = 9;</code>
   * @return Whether the clientVersion field is set.
   */
  boolean hasClientVersion();
  /**
   * <code>.server.ClientVersion client_version = 9;</code>
   * @return The clientVersion.
   */
  com.halloapp.proto.server.ClientVersion getClientVersion();

  /**
   * <code>.server.PushRegister push_register = 10;</code>
   * @return Whether the pushRegister field is set.
   */
  boolean hasPushRegister();
  /**
   * <code>.server.PushRegister push_register = 10;</code>
   * @return The pushRegister.
   */
  com.halloapp.proto.server.PushRegister getPushRegister();

  /**
   * <code>.server.WhisperKeys whisper_keys = 11;</code>
   * @return Whether the whisperKeys field is set.
   */
  boolean hasWhisperKeys();
  /**
   * <code>.server.WhisperKeys whisper_keys = 11;</code>
   * @return The whisperKeys.
   */
  com.halloapp.proto.server.WhisperKeys getWhisperKeys();

  /**
   * <code>.server.Ping ping = 12;</code>
   * @return Whether the ping field is set.
   */
  boolean hasPing();
  /**
   * <code>.server.Ping ping = 12;</code>
   * @return The ping.
   */
  com.halloapp.proto.server.Ping getPing();

  /**
   * <code>.server.FeedItem feed_item = 13;</code>
   * @return Whether the feedItem field is set.
   */
  boolean hasFeedItem();
  /**
   * <code>.server.FeedItem feed_item = 13;</code>
   * @return The feedItem.
   */
  com.halloapp.proto.server.FeedItem getFeedItem();

  /**
   * <code>.server.PrivacyList privacy_list = 14;</code>
   * @return Whether the privacyList field is set.
   */
  boolean hasPrivacyList();
  /**
   * <code>.server.PrivacyList privacy_list = 14;</code>
   * @return The privacyList.
   */
  com.halloapp.proto.server.PrivacyList getPrivacyList();

  /**
   * <code>.server.PrivacyLists privacy_lists = 16;</code>
   * @return Whether the privacyLists field is set.
   */
  boolean hasPrivacyLists();
  /**
   * <code>.server.PrivacyLists privacy_lists = 16;</code>
   * @return The privacyLists.
   */
  com.halloapp.proto.server.PrivacyLists getPrivacyLists();

  /**
   * <code>.server.GroupStanza group_stanza = 17;</code>
   * @return Whether the groupStanza field is set.
   */
  boolean hasGroupStanza();
  /**
   * <code>.server.GroupStanza group_stanza = 17;</code>
   * @return The groupStanza.
   */
  com.halloapp.proto.server.GroupStanza getGroupStanza();

  /**
   * <code>.server.GroupsStanza groups_stanza = 18;</code>
   * @return Whether the groupsStanza field is set.
   */
  boolean hasGroupsStanza();
  /**
   * <code>.server.GroupsStanza groups_stanza = 18;</code>
   * @return The groupsStanza.
   */
  com.halloapp.proto.server.GroupsStanza getGroupsStanza();

  /**
   * <code>.server.ClientLog client_log = 19;</code>
   * @return Whether the clientLog field is set.
   */
  boolean hasClientLog();
  /**
   * <code>.server.ClientLog client_log = 19;</code>
   * @return The clientLog.
   */
  com.halloapp.proto.server.ClientLog getClientLog();

  /**
   * <code>.server.Name name = 20;</code>
   * @return Whether the name field is set.
   */
  boolean hasName();
  /**
   * <code>.server.Name name = 20;</code>
   * @return The name.
   */
  com.halloapp.proto.server.Name getName();

  /**
   * <code>.server.ErrorStanza error_stanza = 21;</code>
   * @return Whether the errorStanza field is set.
   */
  boolean hasErrorStanza();
  /**
   * <code>.server.ErrorStanza error_stanza = 21;</code>
   * @return The errorStanza.
   */
  com.halloapp.proto.server.ErrorStanza getErrorStanza();

  /**
   * <code>.server.Props props = 22;</code>
   * @return Whether the props field is set.
   */
  boolean hasProps();
  /**
   * <code>.server.Props props = 22;</code>
   * @return The props.
   */
  com.halloapp.proto.server.Props getProps();

  /**
   * <code>.server.InvitesRequest invites_request = 23;</code>
   * @return Whether the invitesRequest field is set.
   */
  boolean hasInvitesRequest();
  /**
   * <code>.server.InvitesRequest invites_request = 23;</code>
   * @return The invitesRequest.
   */
  com.halloapp.proto.server.InvitesRequest getInvitesRequest();

  /**
   * <code>.server.InvitesResponse invites_response = 24;</code>
   * @return Whether the invitesResponse field is set.
   */
  boolean hasInvitesResponse();
  /**
   * <code>.server.InvitesResponse invites_response = 24;</code>
   * @return The invitesResponse.
   */
  com.halloapp.proto.server.InvitesResponse getInvitesResponse();

  /**
   * <code>.server.NotificationPrefs notification_prefs = 25;</code>
   * @return Whether the notificationPrefs field is set.
   */
  boolean hasNotificationPrefs();
  /**
   * <code>.server.NotificationPrefs notification_prefs = 25;</code>
   * @return The notificationPrefs.
   */
  com.halloapp.proto.server.NotificationPrefs getNotificationPrefs();

  /**
   * <code>.server.GroupFeedItem group_feed_item = 26;</code>
   * @return Whether the groupFeedItem field is set.
   */
  boolean hasGroupFeedItem();
  /**
   * <code>.server.GroupFeedItem group_feed_item = 26;</code>
   * @return The groupFeedItem.
   */
  com.halloapp.proto.server.GroupFeedItem getGroupFeedItem();

  /**
   * <code>.server.UploadGroupAvatar group_avatar = 27;</code>
   * @return Whether the groupAvatar field is set.
   */
  boolean hasGroupAvatar();
  /**
   * <code>.server.UploadGroupAvatar group_avatar = 27;</code>
   * @return The groupAvatar.
   */
  com.halloapp.proto.server.UploadGroupAvatar getGroupAvatar();

  /**
   * <code>.server.DeleteAccount delete_account = 28;</code>
   * @return Whether the deleteAccount field is set.
   */
  boolean hasDeleteAccount();
  /**
   * <code>.server.DeleteAccount delete_account = 28;</code>
   * @return The deleteAccount.
   */
  com.halloapp.proto.server.DeleteAccount getDeleteAccount();

  /**
   * <code>.server.GroupInviteLink group_invite_link = 31;</code>
   * @return Whether the groupInviteLink field is set.
   */
  boolean hasGroupInviteLink();
  /**
   * <code>.server.GroupInviteLink group_invite_link = 31;</code>
   * @return The groupInviteLink.
   */
  com.halloapp.proto.server.GroupInviteLink getGroupInviteLink();

  /**
   * <code>.server.HistoryResend history_resend = 32;</code>
   * @return Whether the historyResend field is set.
   */
  boolean hasHistoryResend();
  /**
   * <code>.server.HistoryResend history_resend = 32;</code>
   * @return The historyResend.
   */
  com.halloapp.proto.server.HistoryResend getHistoryResend();

  /**
   * <code>.server.ExportData export_data = 33;</code>
   * @return Whether the exportData field is set.
   */
  boolean hasExportData();
  /**
   * <code>.server.ExportData export_data = 33;</code>
   * @return The exportData.
   */
  com.halloapp.proto.server.ExportData getExportData();

  /**
   * <code>.server.ContactSyncError contact_sync_error = 34;</code>
   * @return Whether the contactSyncError field is set.
   */
  boolean hasContactSyncError();
  /**
   * <code>.server.ContactSyncError contact_sync_error = 34;</code>
   * @return The contactSyncError.
   */
  com.halloapp.proto.server.ContactSyncError getContactSyncError();

  /**
   * <pre>
   * only for sms_app gateway use
   * </pre>
   *
   * <code>.server.ClientOtpRequest client_otp_request = 35;</code>
   * @return Whether the clientOtpRequest field is set.
   */
  boolean hasClientOtpRequest();
  /**
   * <pre>
   * only for sms_app gateway use
   * </pre>
   *
   * <code>.server.ClientOtpRequest client_otp_request = 35;</code>
   * @return The clientOtpRequest.
   */
  com.halloapp.proto.server.ClientOtpRequest getClientOtpRequest();

  /**
   * <pre>
   * only for sms_app gateway use
   * </pre>
   *
   * <code>.server.ClientOtpResponse client_otp_response = 36;</code>
   * @return Whether the clientOtpResponse field is set.
   */
  boolean hasClientOtpResponse();
  /**
   * <pre>
   * only for sms_app gateway use
   * </pre>
   *
   * <code>.server.ClientOtpResponse client_otp_response = 36;</code>
   * @return The clientOtpResponse.
   */
  com.halloapp.proto.server.ClientOtpResponse getClientOtpResponse();

  /**
   * <code>.server.WhisperKeysCollection whisper_keys_collection = 37;</code>
   * @return Whether the whisperKeysCollection field is set.
   */
  boolean hasWhisperKeysCollection();
  /**
   * <code>.server.WhisperKeysCollection whisper_keys_collection = 37;</code>
   * @return The whisperKeysCollection.
   */
  com.halloapp.proto.server.WhisperKeysCollection getWhisperKeysCollection();

  /**
   * <code>.server.GetCallServers get_call_servers = 38;</code>
   * @return Whether the getCallServers field is set.
   */
  boolean hasGetCallServers();
  /**
   * <code>.server.GetCallServers get_call_servers = 38;</code>
   * @return The getCallServers.
   */
  com.halloapp.proto.server.GetCallServers getGetCallServers();

  /**
   * <code>.server.GetCallServersResult get_call_servers_result = 39;</code>
   * @return Whether the getCallServersResult field is set.
   */
  boolean hasGetCallServersResult();
  /**
   * <code>.server.GetCallServersResult get_call_servers_result = 39;</code>
   * @return The getCallServersResult.
   */
  com.halloapp.proto.server.GetCallServersResult getGetCallServersResult();

  /**
   * <code>.server.StartCall start_call = 40;</code>
   * @return Whether the startCall field is set.
   */
  boolean hasStartCall();
  /**
   * <code>.server.StartCall start_call = 40;</code>
   * @return The startCall.
   */
  com.halloapp.proto.server.StartCall getStartCall();

  /**
   * <code>.server.StartCallResult start_call_result = 41;</code>
   * @return Whether the startCallResult field is set.
   */
  boolean hasStartCallResult();
  /**
   * <code>.server.StartCallResult start_call_result = 41;</code>
   * @return The startCallResult.
   */
  com.halloapp.proto.server.StartCallResult getStartCallResult();

  /**
   * <code>.server.TruncWhisperKeysCollection trunc_whisper_keys_collection = 42;</code>
   * @return Whether the truncWhisperKeysCollection field is set.
   */
  boolean hasTruncWhisperKeysCollection();
  /**
   * <code>.server.TruncWhisperKeysCollection trunc_whisper_keys_collection = 42;</code>
   * @return The truncWhisperKeysCollection.
   */
  com.halloapp.proto.server.TruncWhisperKeysCollection getTruncWhisperKeysCollection();

  /**
   * <code>.server.ExternalSharePost external_share_post = 43;</code>
   * @return Whether the externalSharePost field is set.
   */
  boolean hasExternalSharePost();
  /**
   * <code>.server.ExternalSharePost external_share_post = 43;</code>
   * @return The externalSharePost.
   */
  com.halloapp.proto.server.ExternalSharePost getExternalSharePost();

  /**
   * <code>.server.ExternalSharePostContainer external_share_post_container = 44;</code>
   * @return Whether the externalSharePostContainer field is set.
   */
  boolean hasExternalSharePostContainer();
  /**
   * <code>.server.ExternalSharePostContainer external_share_post_container = 44;</code>
   * @return The externalSharePostContainer.
   */
  com.halloapp.proto.server.ExternalSharePostContainer getExternalSharePostContainer();

  /**
   * <code>.server.WebClientInfo web_client_info = 45;</code>
   * @return Whether the webClientInfo field is set.
   */
  boolean hasWebClientInfo();
  /**
   * <code>.server.WebClientInfo web_client_info = 45;</code>
   * @return The webClientInfo.
   */
  com.halloapp.proto.server.WebClientInfo getWebClientInfo();

  /**
   * <code>.server.ReportUserContent report_user_content = 46;</code>
   * @return Whether the reportUserContent field is set.
   */
  boolean hasReportUserContent();
  /**
   * <code>.server.ReportUserContent report_user_content = 46;</code>
   * @return The reportUserContent.
   */
  com.halloapp.proto.server.ReportUserContent getReportUserContent();

  /**
   * <code>.server.PublicFeedRequest public_feed_request = 47;</code>
   * @return Whether the publicFeedRequest field is set.
   */
  boolean hasPublicFeedRequest();
  /**
   * <code>.server.PublicFeedRequest public_feed_request = 47;</code>
   * @return The publicFeedRequest.
   */
  com.halloapp.proto.server.PublicFeedRequest getPublicFeedRequest();

  /**
   * <code>.server.PublicFeedResponse public_feed_response = 48;</code>
   * @return Whether the publicFeedResponse field is set.
   */
  boolean hasPublicFeedResponse();
  /**
   * <code>.server.PublicFeedResponse public_feed_response = 48;</code>
   * @return The publicFeedResponse.
   */
  com.halloapp.proto.server.PublicFeedResponse getPublicFeedResponse();

  /**
   * <code>.server.RelationshipRequest relationship_request = 49;</code>
   * @return Whether the relationshipRequest field is set.
   */
  boolean hasRelationshipRequest();
  /**
   * <code>.server.RelationshipRequest relationship_request = 49;</code>
   * @return The relationshipRequest.
   */
  com.halloapp.proto.server.RelationshipRequest getRelationshipRequest();

  /**
   * <code>.server.RelationshipResponse relationship_response = 50;</code>
   * @return Whether the relationshipResponse field is set.
   */
  boolean hasRelationshipResponse();
  /**
   * <code>.server.RelationshipResponse relationship_response = 50;</code>
   * @return The relationshipResponse.
   */
  com.halloapp.proto.server.RelationshipResponse getRelationshipResponse();

  /**
   * <code>.server.RelationshipList relationship_list = 51;</code>
   * @return Whether the relationshipList field is set.
   */
  boolean hasRelationshipList();
  /**
   * <code>.server.RelationshipList relationship_list = 51;</code>
   * @return The relationshipList.
   */
  com.halloapp.proto.server.RelationshipList getRelationshipList();

  /**
   * <code>.server.UsernameRequest username_request = 52;</code>
   * @return Whether the usernameRequest field is set.
   */
  boolean hasUsernameRequest();
  /**
   * <code>.server.UsernameRequest username_request = 52;</code>
   * @return The usernameRequest.
   */
  com.halloapp.proto.server.UsernameRequest getUsernameRequest();

  /**
   * <code>.server.UsernameResponse username_response = 53;</code>
   * @return Whether the usernameResponse field is set.
   */
  boolean hasUsernameResponse();
  /**
   * <code>.server.UsernameResponse username_response = 53;</code>
   * @return The usernameResponse.
   */
  com.halloapp.proto.server.UsernameResponse getUsernameResponse();

  /**
   * <code>.server.SearchRequest search_request = 54;</code>
   * @return Whether the searchRequest field is set.
   */
  boolean hasSearchRequest();
  /**
   * <code>.server.SearchRequest search_request = 54;</code>
   * @return The searchRequest.
   */
  com.halloapp.proto.server.SearchRequest getSearchRequest();

  /**
   * <code>.server.SearchResponse search_response = 55;</code>
   * @return Whether the searchResponse field is set.
   */
  boolean hasSearchResponse();
  /**
   * <code>.server.SearchResponse search_response = 55;</code>
   * @return The searchResponse.
   */
  com.halloapp.proto.server.SearchResponse getSearchResponse();

  /**
   * <code>.server.FollowSuggestionsRequest follow_suggestions_request = 56;</code>
   * @return Whether the followSuggestionsRequest field is set.
   */
  boolean hasFollowSuggestionsRequest();
  /**
   * <code>.server.FollowSuggestionsRequest follow_suggestions_request = 56;</code>
   * @return The followSuggestionsRequest.
   */
  com.halloapp.proto.server.FollowSuggestionsRequest getFollowSuggestionsRequest();

  /**
   * <code>.server.FollowSuggestionsResponse follow_suggestions_response = 57;</code>
   * @return Whether the followSuggestionsResponse field is set.
   */
  boolean hasFollowSuggestionsResponse();
  /**
   * <code>.server.FollowSuggestionsResponse follow_suggestions_response = 57;</code>
   * @return The followSuggestionsResponse.
   */
  com.halloapp.proto.server.FollowSuggestionsResponse getFollowSuggestionsResponse();

  /**
   * <code>.server.SetLinkRequest set_link_request = 58;</code>
   * @return Whether the setLinkRequest field is set.
   */
  boolean hasSetLinkRequest();
  /**
   * <code>.server.SetLinkRequest set_link_request = 58;</code>
   * @return The setLinkRequest.
   */
  com.halloapp.proto.server.SetLinkRequest getSetLinkRequest();

  /**
   * <code>.server.SetLinkResult set_link_result = 59;</code>
   * @return Whether the setLinkResult field is set.
   */
  boolean hasSetLinkResult();
  /**
   * <code>.server.SetLinkResult set_link_result = 59;</code>
   * @return The setLinkResult.
   */
  com.halloapp.proto.server.SetLinkResult getSetLinkResult();

  /**
   * <code>.server.SetBioRequest set_bio_request = 60;</code>
   * @return Whether the setBioRequest field is set.
   */
  boolean hasSetBioRequest();
  /**
   * <code>.server.SetBioRequest set_bio_request = 60;</code>
   * @return The setBioRequest.
   */
  com.halloapp.proto.server.SetBioRequest getSetBioRequest();

  /**
   * <code>.server.SetBioResult set_bio_result = 61;</code>
   * @return Whether the setBioResult field is set.
   */
  boolean hasSetBioResult();
  /**
   * <code>.server.SetBioResult set_bio_result = 61;</code>
   * @return The setBioResult.
   */
  com.halloapp.proto.server.SetBioResult getSetBioResult();

  /**
   * <code>.server.UserProfileRequest user_profile_request = 62;</code>
   * @return Whether the userProfileRequest field is set.
   */
  boolean hasUserProfileRequest();
  /**
   * <code>.server.UserProfileRequest user_profile_request = 62;</code>
   * @return The userProfileRequest.
   */
  com.halloapp.proto.server.UserProfileRequest getUserProfileRequest();

  /**
   * <code>.server.UserProfileResult user_profile_result = 63;</code>
   * @return Whether the userProfileResult field is set.
   */
  boolean hasUserProfileResult();
  /**
   * <code>.server.UserProfileResult user_profile_result = 63;</code>
   * @return The userProfileResult.
   */
  com.halloapp.proto.server.UserProfileResult getUserProfileResult();

  /**
   * <code>.server.PostMetricsRequest post_metrics_request = 64;</code>
   * @return Whether the postMetricsRequest field is set.
   */
  boolean hasPostMetricsRequest();
  /**
   * <code>.server.PostMetricsRequest post_metrics_request = 64;</code>
   * @return The postMetricsRequest.
   */
  com.halloapp.proto.server.PostMetricsRequest getPostMetricsRequest();

  /**
   * <code>.server.PostMetricsResult post_metrics_result = 65;</code>
   * @return Whether the postMetricsResult field is set.
   */
  boolean hasPostMetricsResult();
  /**
   * <code>.server.PostMetricsResult post_metrics_result = 65;</code>
   * @return The postMetricsResult.
   */
  com.halloapp.proto.server.PostMetricsResult getPostMetricsResult();

  /**
   * <code>.server.AiImageRequest ai_image_request = 66;</code>
   * @return Whether the aiImageRequest field is set.
   */
  boolean hasAiImageRequest();
  /**
   * <code>.server.AiImageRequest ai_image_request = 66;</code>
   * @return The aiImageRequest.
   */
  com.halloapp.proto.server.AiImageRequest getAiImageRequest();

  /**
   * <code>.server.AiImageResult ai_image_result = 67;</code>
   * @return Whether the aiImageResult field is set.
   */
  boolean hasAiImageResult();
  /**
   * <code>.server.AiImageResult ai_image_result = 67;</code>
   * @return The aiImageResult.
   */
  com.halloapp.proto.server.AiImageResult getAiImageResult();

  /**
   * <code>.server.ArchiveRequest archive_request = 68;</code>
   * @return Whether the archiveRequest field is set.
   */
  boolean hasArchiveRequest();
  /**
   * <code>.server.ArchiveRequest archive_request = 68;</code>
   * @return The archiveRequest.
   */
  com.halloapp.proto.server.ArchiveRequest getArchiveRequest();

  /**
   * <code>.server.ArchiveResult archive_result = 69;</code>
   * @return Whether the archiveResult field is set.
   */
  boolean hasArchiveResult();
  /**
   * <code>.server.ArchiveResult archive_result = 69;</code>
   * @return The archiveResult.
   */
  com.halloapp.proto.server.ArchiveResult getArchiveResult();

  /**
   * <code>.server.PostSubscriptionRequest post_subscription_request = 70;</code>
   * @return Whether the postSubscriptionRequest field is set.
   */
  boolean hasPostSubscriptionRequest();
  /**
   * <code>.server.PostSubscriptionRequest post_subscription_request = 70;</code>
   * @return The postSubscriptionRequest.
   */
  com.halloapp.proto.server.PostSubscriptionRequest getPostSubscriptionRequest();

  /**
   * <code>.server.PostSubscriptionResponse post_subscription_response = 71;</code>
   * @return Whether the postSubscriptionResponse field is set.
   */
  boolean hasPostSubscriptionResponse();
  /**
   * <code>.server.PostSubscriptionResponse post_subscription_response = 71;</code>
   * @return The postSubscriptionResponse.
   */
  com.halloapp.proto.server.PostSubscriptionResponse getPostSubscriptionResponse();

  /**
   * <code>.server.GeoTagRequest geo_tag_request = 72;</code>
   * @return Whether the geoTagRequest field is set.
   */
  boolean hasGeoTagRequest();
  /**
   * <code>.server.GeoTagRequest geo_tag_request = 72;</code>
   * @return The geoTagRequest.
   */
  com.halloapp.proto.server.GeoTagRequest getGeoTagRequest();

  /**
   * <code>.server.GeoTagResponse geo_tag_response = 73;</code>
   * @return Whether the geoTagResponse field is set.
   */
  boolean hasGeoTagResponse();
  /**
   * <code>.server.GeoTagResponse geo_tag_response = 73;</code>
   * @return The geoTagResponse.
   */
  com.halloapp.proto.server.GeoTagResponse getGeoTagResponse();

  /**
   * <code>.server.RegisterRequest register_request = 74;</code>
   * @return Whether the registerRequest field is set.
   */
  boolean hasRegisterRequest();
  /**
   * <code>.server.RegisterRequest register_request = 74;</code>
   * @return The registerRequest.
   */
  com.halloapp.proto.server.RegisterRequest getRegisterRequest();

  /**
   * <code>.server.RegisterResponse register_response = 75;</code>
   * @return Whether the registerResponse field is set.
   */
  boolean hasRegisterResponse();
  /**
   * <code>.server.RegisterResponse register_response = 75;</code>
   * @return The registerResponse.
   */
  com.halloapp.proto.server.RegisterResponse getRegisterResponse();

  /**
   * <code>.server.HalloappSearchRequest halloapp_search_request = 76;</code>
   * @return Whether the halloappSearchRequest field is set.
   */
  boolean hasHalloappSearchRequest();
  /**
   * <code>.server.HalloappSearchRequest halloapp_search_request = 76;</code>
   * @return The halloappSearchRequest.
   */
  com.halloapp.proto.server.HalloappSearchRequest getHalloappSearchRequest();

  /**
   * <code>.server.HalloappSearchResponse halloapp_search_response = 77;</code>
   * @return Whether the halloappSearchResponse field is set.
   */
  boolean hasHalloappSearchResponse();
  /**
   * <code>.server.HalloappSearchResponse halloapp_search_response = 77;</code>
   * @return The halloappSearchResponse.
   */
  com.halloapp.proto.server.HalloappSearchResponse getHalloappSearchResponse();

  /**
   * <code>.server.FriendshipRequest friendship_request = 78;</code>
   * @return Whether the friendshipRequest field is set.
   */
  boolean hasFriendshipRequest();
  /**
   * <code>.server.FriendshipRequest friendship_request = 78;</code>
   * @return The friendshipRequest.
   */
  com.halloapp.proto.server.FriendshipRequest getFriendshipRequest();

  /**
   * <code>.server.FriendshipResponse friendship_response = 79;</code>
   * @return Whether the friendshipResponse field is set.
   */
  boolean hasFriendshipResponse();
  /**
   * <code>.server.FriendshipResponse friendship_response = 79;</code>
   * @return The friendshipResponse.
   */
  com.halloapp.proto.server.FriendshipResponse getFriendshipResponse();

  /**
   * <code>.server.FriendListRequest friend_list_request = 80;</code>
   * @return Whether the friendListRequest field is set.
   */
  boolean hasFriendListRequest();
  /**
   * <code>.server.FriendListRequest friend_list_request = 80;</code>
   * @return The friendListRequest.
   */
  com.halloapp.proto.server.FriendListRequest getFriendListRequest();

  /**
   * <code>.server.FriendListResponse friend_list_response = 81;</code>
   * @return Whether the friendListResponse field is set.
   */
  boolean hasFriendListResponse();
  /**
   * <code>.server.FriendListResponse friend_list_response = 81;</code>
   * @return The friendListResponse.
   */
  com.halloapp.proto.server.FriendListResponse getFriendListResponse();

  /**
   * <code>.server.Album album = 82;</code>
   * @return Whether the album field is set.
   */
  boolean hasAlbum();
  /**
   * <code>.server.Album album = 82;</code>
   * @return The album.
   */
  com.halloapp.proto.server.Album getAlbum();

  /**
   * <code>.server.AlbumResult album_result = 83;</code>
   * @return Whether the albumResult field is set.
   */
  boolean hasAlbumResult();
  /**
   * <code>.server.AlbumResult album_result = 83;</code>
   * @return The albumResult.
   */
  com.halloapp.proto.server.AlbumResult getAlbumResult();

  /**
   * <code>.server.GetAlbums get_albums = 84;</code>
   * @return Whether the getAlbums field is set.
   */
  boolean hasGetAlbums();
  /**
   * <code>.server.GetAlbums get_albums = 84;</code>
   * @return The getAlbums.
   */
  com.halloapp.proto.server.GetAlbums getGetAlbums();

  public com.halloapp.proto.server.Iq.PayloadCase getPayloadCase();
}
