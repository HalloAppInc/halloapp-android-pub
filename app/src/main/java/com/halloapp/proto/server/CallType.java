// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: server.proto

package com.halloapp.proto.server;

/**
 * <pre>
 * Calls
 * </pre>
 *
 * Protobuf enum {@code server.CallType}
 */
public enum CallType
    implements com.google.protobuf.Internal.EnumLite {
  /**
   * <code>UNKNOWN_TYPE = 0;</code>
   */
  UNKNOWN_TYPE(0),
  /**
   * <code>AUDIO = 1;</code>
   */
  AUDIO(1),
  /**
   * <code>VIDEO = 2;</code>
   */
  VIDEO(2),
  UNRECOGNIZED(-1),
  ;

  /**
   * <code>UNKNOWN_TYPE = 0;</code>
   */
  public static final int UNKNOWN_TYPE_VALUE = 0;
  /**
   * <code>AUDIO = 1;</code>
   */
  public static final int AUDIO_VALUE = 1;
  /**
   * <code>VIDEO = 2;</code>
   */
  public static final int VIDEO_VALUE = 2;


  @java.lang.Override
  public final int getNumber() {
    if (this == UNRECOGNIZED) {
      throw new java.lang.IllegalArgumentException(
          "Can't get the number of an unknown enum value.");
    }
    return value;
  }

  /**
   * @param value The number of the enum to look for.
   * @return The enum associated with the given number.
   * @deprecated Use {@link #forNumber(int)} instead.
   */
  @java.lang.Deprecated
  public static CallType valueOf(int value) {
    return forNumber(value);
  }

  public static CallType forNumber(int value) {
    switch (value) {
      case 0: return UNKNOWN_TYPE;
      case 1: return AUDIO;
      case 2: return VIDEO;
      default: return null;
    }
  }

  public static com.google.protobuf.Internal.EnumLiteMap<CallType>
      internalGetValueMap() {
    return internalValueMap;
  }
  private static final com.google.protobuf.Internal.EnumLiteMap<
      CallType> internalValueMap =
        new com.google.protobuf.Internal.EnumLiteMap<CallType>() {
          @java.lang.Override
          public CallType findValueByNumber(int number) {
            return CallType.forNumber(number);
          }
        };

  public static com.google.protobuf.Internal.EnumVerifier 
      internalGetVerifier() {
    return CallTypeVerifier.INSTANCE;
  }

  private static final class CallTypeVerifier implements 
       com.google.protobuf.Internal.EnumVerifier { 
          static final com.google.protobuf.Internal.EnumVerifier           INSTANCE = new CallTypeVerifier();
          @java.lang.Override
          public boolean isInRange(int number) {
            return CallType.forNumber(number) != null;
          }
        };

  private final int value;

  private CallType(int value) {
    this.value = value;
  }

  // @@protoc_insertion_point(enum_scope:server.CallType)
}

