// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: server.proto

package com.halloapp.proto.server;

public interface SearchResponseOrBuilder extends
    // @@protoc_insertion_point(interface_extends:server.SearchResponse)
    com.google.protobuf.MessageLiteOrBuilder {

  /**
   * <code>.server.SearchResponse.Result result = 1;</code>
   * @return The enum numeric value on the wire for result.
   */
  int getResultValue();
  /**
   * <code>.server.SearchResponse.Result result = 1;</code>
   * @return The result.
   */
  com.halloapp.proto.server.SearchResponse.Result getResult();

  /**
   * <code>repeated .server.BasicUserProfile search_result = 2;</code>
   */
  java.util.List<com.halloapp.proto.server.BasicUserProfile> 
      getSearchResultList();
  /**
   * <code>repeated .server.BasicUserProfile search_result = 2;</code>
   */
  com.halloapp.proto.server.BasicUserProfile getSearchResult(int index);
  /**
   * <code>repeated .server.BasicUserProfile search_result = 2;</code>
   */
  int getSearchResultCount();
}
