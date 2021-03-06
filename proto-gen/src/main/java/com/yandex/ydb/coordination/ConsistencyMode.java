// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: kikimr/public/api/protos/ydb_coordination.proto

package com.yandex.ydb.coordination;

/**
 * <pre>
 **
 * Consistency mode
 * </pre>
 *
 * Protobuf enum {@code Ydb.Coordination.ConsistencyMode}
 */
public enum ConsistencyMode
    implements com.google.protobuf.ProtocolMessageEnum {
  /**
   * <pre>
   * The default or current value
   * </pre>
   *
   * <code>CONSISTENCY_MODE_UNSET = 0;</code>
   */
  CONSISTENCY_MODE_UNSET(0),
  /**
   * <pre>
   * Strict mode makes sure operations may only complete on current master
   * </pre>
   *
   * <code>CONSISTENCY_MODE_STRICT = 1;</code>
   */
  CONSISTENCY_MODE_STRICT(1),
  /**
   * <pre>
   * Relaxed mode allows operations to complete on stale masters
   * </pre>
   *
   * <code>CONSISTENCY_MODE_RELAXED = 2;</code>
   */
  CONSISTENCY_MODE_RELAXED(2),
  UNRECOGNIZED(-1),
  ;

  /**
   * <pre>
   * The default or current value
   * </pre>
   *
   * <code>CONSISTENCY_MODE_UNSET = 0;</code>
   */
  public static final int CONSISTENCY_MODE_UNSET_VALUE = 0;
  /**
   * <pre>
   * Strict mode makes sure operations may only complete on current master
   * </pre>
   *
   * <code>CONSISTENCY_MODE_STRICT = 1;</code>
   */
  public static final int CONSISTENCY_MODE_STRICT_VALUE = 1;
  /**
   * <pre>
   * Relaxed mode allows operations to complete on stale masters
   * </pre>
   *
   * <code>CONSISTENCY_MODE_RELAXED = 2;</code>
   */
  public static final int CONSISTENCY_MODE_RELAXED_VALUE = 2;


  public final int getNumber() {
    if (this == UNRECOGNIZED) {
      throw new java.lang.IllegalArgumentException(
          "Can't get the number of an unknown enum value.");
    }
    return value;
  }

  /**
   * @param value The numeric wire value of the corresponding enum entry.
   * @return The enum associated with the given numeric wire value.
   * @deprecated Use {@link #forNumber(int)} instead.
   */
  @java.lang.Deprecated
  public static ConsistencyMode valueOf(int value) {
    return forNumber(value);
  }

  /**
   * @param value The numeric wire value of the corresponding enum entry.
   * @return The enum associated with the given numeric wire value.
   */
  public static ConsistencyMode forNumber(int value) {
    switch (value) {
      case 0: return CONSISTENCY_MODE_UNSET;
      case 1: return CONSISTENCY_MODE_STRICT;
      case 2: return CONSISTENCY_MODE_RELAXED;
      default: return null;
    }
  }

  public static com.google.protobuf.Internal.EnumLiteMap<ConsistencyMode>
      internalGetValueMap() {
    return internalValueMap;
  }
  private static final com.google.protobuf.Internal.EnumLiteMap<
      ConsistencyMode> internalValueMap =
        new com.google.protobuf.Internal.EnumLiteMap<ConsistencyMode>() {
          public ConsistencyMode findValueByNumber(int number) {
            return ConsistencyMode.forNumber(number);
          }
        };

  public final com.google.protobuf.Descriptors.EnumValueDescriptor
      getValueDescriptor() {
    if (this == UNRECOGNIZED) {
      throw new java.lang.IllegalStateException(
          "Can't get the descriptor of an unrecognized enum value.");
    }
    return getDescriptor().getValues().get(ordinal());
  }
  public final com.google.protobuf.Descriptors.EnumDescriptor
      getDescriptorForType() {
    return getDescriptor();
  }
  public static final com.google.protobuf.Descriptors.EnumDescriptor
      getDescriptor() {
    return com.yandex.ydb.coordination.CoordinationProtos.getDescriptor().getEnumTypes().get(0);
  }

  private static final ConsistencyMode[] VALUES = values();

  public static ConsistencyMode valueOf(
      com.google.protobuf.Descriptors.EnumValueDescriptor desc) {
    if (desc.getType() != getDescriptor()) {
      throw new java.lang.IllegalArgumentException(
        "EnumValueDescriptor is not for this type.");
    }
    if (desc.getIndex() == -1) {
      return UNRECOGNIZED;
    }
    return VALUES[desc.getIndex()];
  }

  private final int value;

  private ConsistencyMode(int value) {
    this.value = value;
  }

  // @@protoc_insertion_point(enum_scope:Ydb.Coordination.ConsistencyMode)
}

