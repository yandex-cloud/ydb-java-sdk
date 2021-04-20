// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: yql/protos/common.proto

package ru.yandex.yql.proto;

public final class Common {
  private Common() {}
  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistryLite registry) {
  }

  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistry registry) {
    registerAllExtensions(
        (com.google.protobuf.ExtensionRegistryLite) registry);
  }
  public interface TTimeOrBuilder extends
      // @@protoc_insertion_point(interface_extends:NYql.NProto.TTime)
      com.google.protobuf.MessageOrBuilder {

    /**
     * <code>optional uint64 Value = 1;</code>
     */
    boolean hasValue();
    /**
     * <code>optional uint64 Value = 1;</code>
     */
    long getValue();

    /**
     * <code>optional .NYql.NProto.TTime.TUnit Unit = 2;</code>
     */
    boolean hasUnit();
    /**
     * <code>optional .NYql.NProto.TTime.TUnit Unit = 2;</code>
     */
    ru.yandex.yql.proto.Common.TTime.TUnit getUnit();
  }
  /**
   * Protobuf type {@code NYql.NProto.TTime}
   */
  public  static final class TTime extends
      com.google.protobuf.GeneratedMessageV3 implements
      // @@protoc_insertion_point(message_implements:NYql.NProto.TTime)
      TTimeOrBuilder {
  private static final long serialVersionUID = 0L;
    // Use TTime.newBuilder() to construct.
    private TTime(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
      super(builder);
    }
    private TTime() {
      value_ = 0L;
      unit_ = 1;
    }

    @java.lang.Override
    public final com.google.protobuf.UnknownFieldSet
    getUnknownFields() {
      return this.unknownFields;
    }
    private TTime(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      this();
      int mutable_bitField0_ = 0;
      com.google.protobuf.UnknownFieldSet.Builder unknownFields =
          com.google.protobuf.UnknownFieldSet.newBuilder();
      try {
        boolean done = false;
        while (!done) {
          int tag = input.readTag();
          switch (tag) {
            case 0:
              done = true;
              break;
            default: {
              if (!parseUnknownField(
                  input, unknownFields, extensionRegistry, tag)) {
                done = true;
              }
              break;
            }
            case 8: {
              bitField0_ |= 0x00000001;
              value_ = input.readUInt64();
              break;
            }
            case 16: {
              int rawValue = input.readEnum();
              ru.yandex.yql.proto.Common.TTime.TUnit value = ru.yandex.yql.proto.Common.TTime.TUnit.valueOf(rawValue);
              if (value == null) {
                unknownFields.mergeVarintField(2, rawValue);
              } else {
                bitField0_ |= 0x00000002;
                unit_ = rawValue;
              }
              break;
            }
          }
        }
      } catch (com.google.protobuf.InvalidProtocolBufferException e) {
        throw e.setUnfinishedMessage(this);
      } catch (java.io.IOException e) {
        throw new com.google.protobuf.InvalidProtocolBufferException(
            e).setUnfinishedMessage(this);
      } finally {
        this.unknownFields = unknownFields.build();
        makeExtensionsImmutable();
      }
    }
    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return ru.yandex.yql.proto.Common.internal_static_NYql_NProto_TTime_descriptor;
    }

    protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return ru.yandex.yql.proto.Common.internal_static_NYql_NProto_TTime_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              ru.yandex.yql.proto.Common.TTime.class, ru.yandex.yql.proto.Common.TTime.Builder.class);
    }

    /**
     * Protobuf enum {@code NYql.NProto.TTime.TUnit}
     */
    public enum TUnit
        implements com.google.protobuf.ProtocolMessageEnum {
      /**
       * <code>NANOSECONDS = 1;</code>
       */
      NANOSECONDS(1),
      /**
       * <code>MICROSECONDS = 2;</code>
       */
      MICROSECONDS(2),
      /**
       * <code>MILLISECONDS = 3;</code>
       */
      MILLISECONDS(3),
      /**
       * <code>SECONDS = 4;</code>
       */
      SECONDS(4),
      /**
       * <code>MINUTES = 5;</code>
       */
      MINUTES(5),
      /**
       * <code>HOURS = 6;</code>
       */
      HOURS(6),
      /**
       * <code>DAYS = 7;</code>
       */
      DAYS(7),
      ;

      /**
       * <code>NANOSECONDS = 1;</code>
       */
      public static final int NANOSECONDS_VALUE = 1;
      /**
       * <code>MICROSECONDS = 2;</code>
       */
      public static final int MICROSECONDS_VALUE = 2;
      /**
       * <code>MILLISECONDS = 3;</code>
       */
      public static final int MILLISECONDS_VALUE = 3;
      /**
       * <code>SECONDS = 4;</code>
       */
      public static final int SECONDS_VALUE = 4;
      /**
       * <code>MINUTES = 5;</code>
       */
      public static final int MINUTES_VALUE = 5;
      /**
       * <code>HOURS = 6;</code>
       */
      public static final int HOURS_VALUE = 6;
      /**
       * <code>DAYS = 7;</code>
       */
      public static final int DAYS_VALUE = 7;


      public final int getNumber() {
        return value;
      }

      /**
       * @deprecated Use {@link #forNumber(int)} instead.
       */
      @java.lang.Deprecated
      public static TUnit valueOf(int value) {
        return forNumber(value);
      }

      public static TUnit forNumber(int value) {
        switch (value) {
          case 1: return NANOSECONDS;
          case 2: return MICROSECONDS;
          case 3: return MILLISECONDS;
          case 4: return SECONDS;
          case 5: return MINUTES;
          case 6: return HOURS;
          case 7: return DAYS;
          default: return null;
        }
      }

      public static com.google.protobuf.Internal.EnumLiteMap<TUnit>
          internalGetValueMap() {
        return internalValueMap;
      }
      private static final com.google.protobuf.Internal.EnumLiteMap<
          TUnit> internalValueMap =
            new com.google.protobuf.Internal.EnumLiteMap<TUnit>() {
              public TUnit findValueByNumber(int number) {
                return TUnit.forNumber(number);
              }
            };

      public final com.google.protobuf.Descriptors.EnumValueDescriptor
          getValueDescriptor() {
        return getDescriptor().getValues().get(ordinal());
      }
      public final com.google.protobuf.Descriptors.EnumDescriptor
          getDescriptorForType() {
        return getDescriptor();
      }
      public static final com.google.protobuf.Descriptors.EnumDescriptor
          getDescriptor() {
        return ru.yandex.yql.proto.Common.TTime.getDescriptor().getEnumTypes().get(0);
      }

      private static final TUnit[] VALUES = values();

      public static TUnit valueOf(
          com.google.protobuf.Descriptors.EnumValueDescriptor desc) {
        if (desc.getType() != getDescriptor()) {
          throw new java.lang.IllegalArgumentException(
            "EnumValueDescriptor is not for this type.");
        }
        return VALUES[desc.getIndex()];
      }

      private final int value;

      private TUnit(int value) {
        this.value = value;
      }

      // @@protoc_insertion_point(enum_scope:NYql.NProto.TTime.TUnit)
    }

    private int bitField0_;
    public static final int VALUE_FIELD_NUMBER = 1;
    private long value_;
    /**
     * <code>optional uint64 Value = 1;</code>
     */
    public boolean hasValue() {
      return ((bitField0_ & 0x00000001) == 0x00000001);
    }
    /**
     * <code>optional uint64 Value = 1;</code>
     */
    public long getValue() {
      return value_;
    }

    public static final int UNIT_FIELD_NUMBER = 2;
    private int unit_;
    /**
     * <code>optional .NYql.NProto.TTime.TUnit Unit = 2;</code>
     */
    public boolean hasUnit() {
      return ((bitField0_ & 0x00000002) == 0x00000002);
    }
    /**
     * <code>optional .NYql.NProto.TTime.TUnit Unit = 2;</code>
     */
    public ru.yandex.yql.proto.Common.TTime.TUnit getUnit() {
      ru.yandex.yql.proto.Common.TTime.TUnit result = ru.yandex.yql.proto.Common.TTime.TUnit.valueOf(unit_);
      return result == null ? ru.yandex.yql.proto.Common.TTime.TUnit.NANOSECONDS : result;
    }

    private byte memoizedIsInitialized = -1;
    public final boolean isInitialized() {
      byte isInitialized = memoizedIsInitialized;
      if (isInitialized == 1) return true;
      if (isInitialized == 0) return false;

      memoizedIsInitialized = 1;
      return true;
    }

    public void writeTo(com.google.protobuf.CodedOutputStream output)
                        throws java.io.IOException {
      if (((bitField0_ & 0x00000001) == 0x00000001)) {
        output.writeUInt64(1, value_);
      }
      if (((bitField0_ & 0x00000002) == 0x00000002)) {
        output.writeEnum(2, unit_);
      }
      unknownFields.writeTo(output);
    }

    public int getSerializedSize() {
      int size = memoizedSize;
      if (size != -1) return size;

      size = 0;
      if (((bitField0_ & 0x00000001) == 0x00000001)) {
        size += com.google.protobuf.CodedOutputStream
          .computeUInt64Size(1, value_);
      }
      if (((bitField0_ & 0x00000002) == 0x00000002)) {
        size += com.google.protobuf.CodedOutputStream
          .computeEnumSize(2, unit_);
      }
      size += unknownFields.getSerializedSize();
      memoizedSize = size;
      return size;
    }

    @java.lang.Override
    public boolean equals(final java.lang.Object obj) {
      if (obj == this) {
       return true;
      }
      if (!(obj instanceof ru.yandex.yql.proto.Common.TTime)) {
        return super.equals(obj);
      }
      ru.yandex.yql.proto.Common.TTime other = (ru.yandex.yql.proto.Common.TTime) obj;

      boolean result = true;
      result = result && (hasValue() == other.hasValue());
      if (hasValue()) {
        result = result && (getValue()
            == other.getValue());
      }
      result = result && (hasUnit() == other.hasUnit());
      if (hasUnit()) {
        result = result && unit_ == other.unit_;
      }
      result = result && unknownFields.equals(other.unknownFields);
      return result;
    }

    @java.lang.Override
    public int hashCode() {
      if (memoizedHashCode != 0) {
        return memoizedHashCode;
      }
      int hash = 41;
      hash = (19 * hash) + getDescriptor().hashCode();
      if (hasValue()) {
        hash = (37 * hash) + VALUE_FIELD_NUMBER;
        hash = (53 * hash) + com.google.protobuf.Internal.hashLong(
            getValue());
      }
      if (hasUnit()) {
        hash = (37 * hash) + UNIT_FIELD_NUMBER;
        hash = (53 * hash) + unit_;
      }
      hash = (29 * hash) + unknownFields.hashCode();
      memoizedHashCode = hash;
      return hash;
    }

    public static ru.yandex.yql.proto.Common.TTime parseFrom(
        java.nio.ByteBuffer data)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data);
    }
    public static ru.yandex.yql.proto.Common.TTime parseFrom(
        java.nio.ByteBuffer data,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data, extensionRegistry);
    }
    public static ru.yandex.yql.proto.Common.TTime parseFrom(
        com.google.protobuf.ByteString data)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data);
    }
    public static ru.yandex.yql.proto.Common.TTime parseFrom(
        com.google.protobuf.ByteString data,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data, extensionRegistry);
    }
    public static ru.yandex.yql.proto.Common.TTime parseFrom(byte[] data)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data);
    }
    public static ru.yandex.yql.proto.Common.TTime parseFrom(
        byte[] data,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data, extensionRegistry);
    }
    public static ru.yandex.yql.proto.Common.TTime parseFrom(java.io.InputStream input)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseWithIOException(PARSER, input);
    }
    public static ru.yandex.yql.proto.Common.TTime parseFrom(
        java.io.InputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseWithIOException(PARSER, input, extensionRegistry);
    }
    public static ru.yandex.yql.proto.Common.TTime parseDelimitedFrom(java.io.InputStream input)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseDelimitedWithIOException(PARSER, input);
    }
    public static ru.yandex.yql.proto.Common.TTime parseDelimitedFrom(
        java.io.InputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
    }
    public static ru.yandex.yql.proto.Common.TTime parseFrom(
        com.google.protobuf.CodedInputStream input)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseWithIOException(PARSER, input);
    }
    public static ru.yandex.yql.proto.Common.TTime parseFrom(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseWithIOException(PARSER, input, extensionRegistry);
    }

    public Builder newBuilderForType() { return newBuilder(); }
    public static Builder newBuilder() {
      return DEFAULT_INSTANCE.toBuilder();
    }
    public static Builder newBuilder(ru.yandex.yql.proto.Common.TTime prototype) {
      return DEFAULT_INSTANCE.toBuilder().mergeFrom(prototype);
    }
    public Builder toBuilder() {
      return this == DEFAULT_INSTANCE
          ? new Builder() : new Builder().mergeFrom(this);
    }

    @java.lang.Override
    protected Builder newBuilderForType(
        com.google.protobuf.GeneratedMessageV3.BuilderParent parent) {
      Builder builder = new Builder(parent);
      return builder;
    }
    /**
     * Protobuf type {@code NYql.NProto.TTime}
     */
    public static final class Builder extends
        com.google.protobuf.GeneratedMessageV3.Builder<Builder> implements
        // @@protoc_insertion_point(builder_implements:NYql.NProto.TTime)
        ru.yandex.yql.proto.Common.TTimeOrBuilder {
      public static final com.google.protobuf.Descriptors.Descriptor
          getDescriptor() {
        return ru.yandex.yql.proto.Common.internal_static_NYql_NProto_TTime_descriptor;
      }

      protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
          internalGetFieldAccessorTable() {
        return ru.yandex.yql.proto.Common.internal_static_NYql_NProto_TTime_fieldAccessorTable
            .ensureFieldAccessorsInitialized(
                ru.yandex.yql.proto.Common.TTime.class, ru.yandex.yql.proto.Common.TTime.Builder.class);
      }

      // Construct using ru.yandex.yql.proto.Common.TTime.newBuilder()
      private Builder() {
        maybeForceBuilderInitialization();
      }

      private Builder(
          com.google.protobuf.GeneratedMessageV3.BuilderParent parent) {
        super(parent);
        maybeForceBuilderInitialization();
      }
      private void maybeForceBuilderInitialization() {
        if (com.google.protobuf.GeneratedMessageV3
                .alwaysUseFieldBuilders) {
        }
      }
      public Builder clear() {
        super.clear();
        value_ = 0L;
        bitField0_ = (bitField0_ & ~0x00000001);
        unit_ = 1;
        bitField0_ = (bitField0_ & ~0x00000002);
        return this;
      }

      public com.google.protobuf.Descriptors.Descriptor
          getDescriptorForType() {
        return ru.yandex.yql.proto.Common.internal_static_NYql_NProto_TTime_descriptor;
      }

      public ru.yandex.yql.proto.Common.TTime getDefaultInstanceForType() {
        return ru.yandex.yql.proto.Common.TTime.getDefaultInstance();
      }

      public ru.yandex.yql.proto.Common.TTime build() {
        ru.yandex.yql.proto.Common.TTime result = buildPartial();
        if (!result.isInitialized()) {
          throw newUninitializedMessageException(result);
        }
        return result;
      }

      public ru.yandex.yql.proto.Common.TTime buildPartial() {
        ru.yandex.yql.proto.Common.TTime result = new ru.yandex.yql.proto.Common.TTime(this);
        int from_bitField0_ = bitField0_;
        int to_bitField0_ = 0;
        if (((from_bitField0_ & 0x00000001) == 0x00000001)) {
          to_bitField0_ |= 0x00000001;
        }
        result.value_ = value_;
        if (((from_bitField0_ & 0x00000002) == 0x00000002)) {
          to_bitField0_ |= 0x00000002;
        }
        result.unit_ = unit_;
        result.bitField0_ = to_bitField0_;
        onBuilt();
        return result;
      }

      public Builder clone() {
        return (Builder) super.clone();
      }
      public Builder setField(
          com.google.protobuf.Descriptors.FieldDescriptor field,
          java.lang.Object value) {
        return (Builder) super.setField(field, value);
      }
      public Builder clearField(
          com.google.protobuf.Descriptors.FieldDescriptor field) {
        return (Builder) super.clearField(field);
      }
      public Builder clearOneof(
          com.google.protobuf.Descriptors.OneofDescriptor oneof) {
        return (Builder) super.clearOneof(oneof);
      }
      public Builder setRepeatedField(
          com.google.protobuf.Descriptors.FieldDescriptor field,
          int index, java.lang.Object value) {
        return (Builder) super.setRepeatedField(field, index, value);
      }
      public Builder addRepeatedField(
          com.google.protobuf.Descriptors.FieldDescriptor field,
          java.lang.Object value) {
        return (Builder) super.addRepeatedField(field, value);
      }
      public Builder mergeFrom(com.google.protobuf.Message other) {
        if (other instanceof ru.yandex.yql.proto.Common.TTime) {
          return mergeFrom((ru.yandex.yql.proto.Common.TTime)other);
        } else {
          super.mergeFrom(other);
          return this;
        }
      }

      public Builder mergeFrom(ru.yandex.yql.proto.Common.TTime other) {
        if (other == ru.yandex.yql.proto.Common.TTime.getDefaultInstance()) return this;
        if (other.hasValue()) {
          setValue(other.getValue());
        }
        if (other.hasUnit()) {
          setUnit(other.getUnit());
        }
        this.mergeUnknownFields(other.unknownFields);
        onChanged();
        return this;
      }

      public final boolean isInitialized() {
        return true;
      }

      public Builder mergeFrom(
          com.google.protobuf.CodedInputStream input,
          com.google.protobuf.ExtensionRegistryLite extensionRegistry)
          throws java.io.IOException {
        ru.yandex.yql.proto.Common.TTime parsedMessage = null;
        try {
          parsedMessage = PARSER.parsePartialFrom(input, extensionRegistry);
        } catch (com.google.protobuf.InvalidProtocolBufferException e) {
          parsedMessage = (ru.yandex.yql.proto.Common.TTime) e.getUnfinishedMessage();
          throw e.unwrapIOException();
        } finally {
          if (parsedMessage != null) {
            mergeFrom(parsedMessage);
          }
        }
        return this;
      }
      private int bitField0_;

      private long value_ ;
      /**
       * <code>optional uint64 Value = 1;</code>
       */
      public boolean hasValue() {
        return ((bitField0_ & 0x00000001) == 0x00000001);
      }
      /**
       * <code>optional uint64 Value = 1;</code>
       */
      public long getValue() {
        return value_;
      }
      /**
       * <code>optional uint64 Value = 1;</code>
       */
      public Builder setValue(long value) {
        bitField0_ |= 0x00000001;
        value_ = value;
        onChanged();
        return this;
      }
      /**
       * <code>optional uint64 Value = 1;</code>
       */
      public Builder clearValue() {
        bitField0_ = (bitField0_ & ~0x00000001);
        value_ = 0L;
        onChanged();
        return this;
      }

      private int unit_ = 1;
      /**
       * <code>optional .NYql.NProto.TTime.TUnit Unit = 2;</code>
       */
      public boolean hasUnit() {
        return ((bitField0_ & 0x00000002) == 0x00000002);
      }
      /**
       * <code>optional .NYql.NProto.TTime.TUnit Unit = 2;</code>
       */
      public ru.yandex.yql.proto.Common.TTime.TUnit getUnit() {
        ru.yandex.yql.proto.Common.TTime.TUnit result = ru.yandex.yql.proto.Common.TTime.TUnit.valueOf(unit_);
        return result == null ? ru.yandex.yql.proto.Common.TTime.TUnit.NANOSECONDS : result;
      }
      /**
       * <code>optional .NYql.NProto.TTime.TUnit Unit = 2;</code>
       */
      public Builder setUnit(ru.yandex.yql.proto.Common.TTime.TUnit value) {
        if (value == null) {
          throw new NullPointerException();
        }
        bitField0_ |= 0x00000002;
        unit_ = value.getNumber();
        onChanged();
        return this;
      }
      /**
       * <code>optional .NYql.NProto.TTime.TUnit Unit = 2;</code>
       */
      public Builder clearUnit() {
        bitField0_ = (bitField0_ & ~0x00000002);
        unit_ = 1;
        onChanged();
        return this;
      }
      public final Builder setUnknownFields(
          final com.google.protobuf.UnknownFieldSet unknownFields) {
        return super.setUnknownFields(unknownFields);
      }

      public final Builder mergeUnknownFields(
          final com.google.protobuf.UnknownFieldSet unknownFields) {
        return super.mergeUnknownFields(unknownFields);
      }


      // @@protoc_insertion_point(builder_scope:NYql.NProto.TTime)
    }

    // @@protoc_insertion_point(class_scope:NYql.NProto.TTime)
    private static final ru.yandex.yql.proto.Common.TTime DEFAULT_INSTANCE;
    static {
      DEFAULT_INSTANCE = new ru.yandex.yql.proto.Common.TTime();
    }

    public static ru.yandex.yql.proto.Common.TTime getDefaultInstance() {
      return DEFAULT_INSTANCE;
    }

    @java.lang.Deprecated public static final com.google.protobuf.Parser<TTime>
        PARSER = new com.google.protobuf.AbstractParser<TTime>() {
      public TTime parsePartialFrom(
          com.google.protobuf.CodedInputStream input,
          com.google.protobuf.ExtensionRegistryLite extensionRegistry)
          throws com.google.protobuf.InvalidProtocolBufferException {
          return new TTime(input, extensionRegistry);
      }
    };

    public static com.google.protobuf.Parser<TTime> parser() {
      return PARSER;
    }

    @java.lang.Override
    public com.google.protobuf.Parser<TTime> getParserForType() {
      return PARSER;
    }

    public ru.yandex.yql.proto.Common.TTime getDefaultInstanceForType() {
      return DEFAULT_INSTANCE;
    }

  }

  private static final com.google.protobuf.Descriptors.Descriptor
    internal_static_NYql_NProto_TTime_descriptor;
  private static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_NYql_NProto_TTime_fieldAccessorTable;

  public static com.google.protobuf.Descriptors.FileDescriptor
      getDescriptor() {
    return descriptor;
  }
  private static  com.google.protobuf.Descriptors.FileDescriptor
      descriptor;
  static {
    java.lang.String[] descriptorData = {
      "\n\027yql/protos/common.proto\022\013NYql.NProto\"\253" +
      "\001\n\005TTime\022\r\n\005Value\030\001 \001(\004\022&\n\004Unit\030\002 \001(\0162\030." +
      "NYql.NProto.TTime.TUnit\"k\n\005TUnit\022\017\n\013NANO" +
      "SECONDS\020\001\022\020\n\014MICROSECONDS\020\002\022\020\n\014MILLISECO" +
      "NDS\020\003\022\013\n\007SECONDS\020\004\022\013\n\007MINUTES\020\005\022\t\n\005HOURS" +
      "\020\006\022\010\n\004DAYS\020\007B\025\n\023ru.yandex.yql.proto"
    };
    com.google.protobuf.Descriptors.FileDescriptor.InternalDescriptorAssigner assigner =
        new com.google.protobuf.Descriptors.FileDescriptor.    InternalDescriptorAssigner() {
          public com.google.protobuf.ExtensionRegistry assignDescriptors(
              com.google.protobuf.Descriptors.FileDescriptor root) {
            descriptor = root;
            return null;
          }
        };
    com.google.protobuf.Descriptors.FileDescriptor
      .internalBuildGeneratedFileFrom(descriptorData,
        new com.google.protobuf.Descriptors.FileDescriptor[] {
        }, assigner);
    internal_static_NYql_NProto_TTime_descriptor =
      getDescriptor().getMessageTypes().get(0);
    internal_static_NYql_NProto_TTime_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_NYql_NProto_TTime_descriptor,
        new java.lang.String[] { "Value", "Unit", });
  }

  // @@protoc_insertion_point(outer_class_scope)
}
