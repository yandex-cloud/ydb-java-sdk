// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: kikimr/public/api/protos/ydb_common.proto

package ru.yandex.ydb.common;

public final class CommonProtos {
  private CommonProtos() {}
  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistryLite registry) {
  }

  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistry registry) {
    registerAllExtensions(
        (com.google.protobuf.ExtensionRegistryLite) registry);
  }
  public interface FeatureFlagOrBuilder extends
      // @@protoc_insertion_point(interface_extends:Ydb.FeatureFlag)
      com.google.protobuf.MessageOrBuilder {
  }
  /**
   * Protobuf type {@code Ydb.FeatureFlag}
   */
  public  static final class FeatureFlag extends
      com.google.protobuf.GeneratedMessageV3 implements
      // @@protoc_insertion_point(message_implements:Ydb.FeatureFlag)
      FeatureFlagOrBuilder {
  private static final long serialVersionUID = 0L;
    // Use FeatureFlag.newBuilder() to construct.
    private FeatureFlag(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
      super(builder);
    }
    private FeatureFlag() {
    }

    @java.lang.Override
    public final com.google.protobuf.UnknownFieldSet
    getUnknownFields() {
      return this.unknownFields;
    }
    private FeatureFlag(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      this();
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
              if (!parseUnknownFieldProto3(
                  input, unknownFields, extensionRegistry, tag)) {
                done = true;
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
      return ru.yandex.ydb.common.CommonProtos.internal_static_Ydb_FeatureFlag_descriptor;
    }

    protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return ru.yandex.ydb.common.CommonProtos.internal_static_Ydb_FeatureFlag_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              ru.yandex.ydb.common.CommonProtos.FeatureFlag.class, ru.yandex.ydb.common.CommonProtos.FeatureFlag.Builder.class);
    }

    /**
     * Protobuf enum {@code Ydb.FeatureFlag.Status}
     */
    public enum Status
        implements com.google.protobuf.ProtocolMessageEnum {
      /**
       * <code>STATUS_UNSPECIFIED = 0;</code>
       */
      STATUS_UNSPECIFIED(0),
      /**
       * <code>ENABLED = 1;</code>
       */
      ENABLED(1),
      /**
       * <code>DISABLED = 2;</code>
       */
      DISABLED(2),
      UNRECOGNIZED(-1),
      ;

      /**
       * <code>STATUS_UNSPECIFIED = 0;</code>
       */
      public static final int STATUS_UNSPECIFIED_VALUE = 0;
      /**
       * <code>ENABLED = 1;</code>
       */
      public static final int ENABLED_VALUE = 1;
      /**
       * <code>DISABLED = 2;</code>
       */
      public static final int DISABLED_VALUE = 2;


      public final int getNumber() {
        if (this == UNRECOGNIZED) {
          throw new java.lang.IllegalArgumentException(
              "Can't get the number of an unknown enum value.");
        }
        return value;
      }

      /**
       * @deprecated Use {@link #forNumber(int)} instead.
       */
      @java.lang.Deprecated
      public static Status valueOf(int value) {
        return forNumber(value);
      }

      public static Status forNumber(int value) {
        switch (value) {
          case 0: return STATUS_UNSPECIFIED;
          case 1: return ENABLED;
          case 2: return DISABLED;
          default: return null;
        }
      }

      public static com.google.protobuf.Internal.EnumLiteMap<Status>
          internalGetValueMap() {
        return internalValueMap;
      }
      private static final com.google.protobuf.Internal.EnumLiteMap<
          Status> internalValueMap =
            new com.google.protobuf.Internal.EnumLiteMap<Status>() {
              public Status findValueByNumber(int number) {
                return Status.forNumber(number);
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
        return ru.yandex.ydb.common.CommonProtos.FeatureFlag.getDescriptor().getEnumTypes().get(0);
      }

      private static final Status[] VALUES = values();

      public static Status valueOf(
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

      private Status(int value) {
        this.value = value;
      }

      // @@protoc_insertion_point(enum_scope:Ydb.FeatureFlag.Status)
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
      unknownFields.writeTo(output);
    }

    public int getSerializedSize() {
      int size = memoizedSize;
      if (size != -1) return size;

      size = 0;
      size += unknownFields.getSerializedSize();
      memoizedSize = size;
      return size;
    }

    @java.lang.Override
    public boolean equals(final java.lang.Object obj) {
      if (obj == this) {
       return true;
      }
      if (!(obj instanceof ru.yandex.ydb.common.CommonProtos.FeatureFlag)) {
        return super.equals(obj);
      }
      ru.yandex.ydb.common.CommonProtos.FeatureFlag other = (ru.yandex.ydb.common.CommonProtos.FeatureFlag) obj;

      boolean result = true;
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
      hash = (29 * hash) + unknownFields.hashCode();
      memoizedHashCode = hash;
      return hash;
    }

    public static ru.yandex.ydb.common.CommonProtos.FeatureFlag parseFrom(
        java.nio.ByteBuffer data)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data);
    }
    public static ru.yandex.ydb.common.CommonProtos.FeatureFlag parseFrom(
        java.nio.ByteBuffer data,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data, extensionRegistry);
    }
    public static ru.yandex.ydb.common.CommonProtos.FeatureFlag parseFrom(
        com.google.protobuf.ByteString data)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data);
    }
    public static ru.yandex.ydb.common.CommonProtos.FeatureFlag parseFrom(
        com.google.protobuf.ByteString data,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data, extensionRegistry);
    }
    public static ru.yandex.ydb.common.CommonProtos.FeatureFlag parseFrom(byte[] data)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data);
    }
    public static ru.yandex.ydb.common.CommonProtos.FeatureFlag parseFrom(
        byte[] data,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data, extensionRegistry);
    }
    public static ru.yandex.ydb.common.CommonProtos.FeatureFlag parseFrom(java.io.InputStream input)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseWithIOException(PARSER, input);
    }
    public static ru.yandex.ydb.common.CommonProtos.FeatureFlag parseFrom(
        java.io.InputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseWithIOException(PARSER, input, extensionRegistry);
    }
    public static ru.yandex.ydb.common.CommonProtos.FeatureFlag parseDelimitedFrom(java.io.InputStream input)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseDelimitedWithIOException(PARSER, input);
    }
    public static ru.yandex.ydb.common.CommonProtos.FeatureFlag parseDelimitedFrom(
        java.io.InputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
    }
    public static ru.yandex.ydb.common.CommonProtos.FeatureFlag parseFrom(
        com.google.protobuf.CodedInputStream input)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseWithIOException(PARSER, input);
    }
    public static ru.yandex.ydb.common.CommonProtos.FeatureFlag parseFrom(
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
    public static Builder newBuilder(ru.yandex.ydb.common.CommonProtos.FeatureFlag prototype) {
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
     * Protobuf type {@code Ydb.FeatureFlag}
     */
    public static final class Builder extends
        com.google.protobuf.GeneratedMessageV3.Builder<Builder> implements
        // @@protoc_insertion_point(builder_implements:Ydb.FeatureFlag)
        ru.yandex.ydb.common.CommonProtos.FeatureFlagOrBuilder {
      public static final com.google.protobuf.Descriptors.Descriptor
          getDescriptor() {
        return ru.yandex.ydb.common.CommonProtos.internal_static_Ydb_FeatureFlag_descriptor;
      }

      protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
          internalGetFieldAccessorTable() {
        return ru.yandex.ydb.common.CommonProtos.internal_static_Ydb_FeatureFlag_fieldAccessorTable
            .ensureFieldAccessorsInitialized(
                ru.yandex.ydb.common.CommonProtos.FeatureFlag.class, ru.yandex.ydb.common.CommonProtos.FeatureFlag.Builder.class);
      }

      // Construct using ru.yandex.ydb.common.CommonProtos.FeatureFlag.newBuilder()
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
        return this;
      }

      public com.google.protobuf.Descriptors.Descriptor
          getDescriptorForType() {
        return ru.yandex.ydb.common.CommonProtos.internal_static_Ydb_FeatureFlag_descriptor;
      }

      public ru.yandex.ydb.common.CommonProtos.FeatureFlag getDefaultInstanceForType() {
        return ru.yandex.ydb.common.CommonProtos.FeatureFlag.getDefaultInstance();
      }

      public ru.yandex.ydb.common.CommonProtos.FeatureFlag build() {
        ru.yandex.ydb.common.CommonProtos.FeatureFlag result = buildPartial();
        if (!result.isInitialized()) {
          throw newUninitializedMessageException(result);
        }
        return result;
      }

      public ru.yandex.ydb.common.CommonProtos.FeatureFlag buildPartial() {
        ru.yandex.ydb.common.CommonProtos.FeatureFlag result = new ru.yandex.ydb.common.CommonProtos.FeatureFlag(this);
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
        if (other instanceof ru.yandex.ydb.common.CommonProtos.FeatureFlag) {
          return mergeFrom((ru.yandex.ydb.common.CommonProtos.FeatureFlag)other);
        } else {
          super.mergeFrom(other);
          return this;
        }
      }

      public Builder mergeFrom(ru.yandex.ydb.common.CommonProtos.FeatureFlag other) {
        if (other == ru.yandex.ydb.common.CommonProtos.FeatureFlag.getDefaultInstance()) return this;
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
        ru.yandex.ydb.common.CommonProtos.FeatureFlag parsedMessage = null;
        try {
          parsedMessage = PARSER.parsePartialFrom(input, extensionRegistry);
        } catch (com.google.protobuf.InvalidProtocolBufferException e) {
          parsedMessage = (ru.yandex.ydb.common.CommonProtos.FeatureFlag) e.getUnfinishedMessage();
          throw e.unwrapIOException();
        } finally {
          if (parsedMessage != null) {
            mergeFrom(parsedMessage);
          }
        }
        return this;
      }
      public final Builder setUnknownFields(
          final com.google.protobuf.UnknownFieldSet unknownFields) {
        return super.setUnknownFieldsProto3(unknownFields);
      }

      public final Builder mergeUnknownFields(
          final com.google.protobuf.UnknownFieldSet unknownFields) {
        return super.mergeUnknownFields(unknownFields);
      }


      // @@protoc_insertion_point(builder_scope:Ydb.FeatureFlag)
    }

    // @@protoc_insertion_point(class_scope:Ydb.FeatureFlag)
    private static final ru.yandex.ydb.common.CommonProtos.FeatureFlag DEFAULT_INSTANCE;
    static {
      DEFAULT_INSTANCE = new ru.yandex.ydb.common.CommonProtos.FeatureFlag();
    }

    public static ru.yandex.ydb.common.CommonProtos.FeatureFlag getDefaultInstance() {
      return DEFAULT_INSTANCE;
    }

    private static final com.google.protobuf.Parser<FeatureFlag>
        PARSER = new com.google.protobuf.AbstractParser<FeatureFlag>() {
      public FeatureFlag parsePartialFrom(
          com.google.protobuf.CodedInputStream input,
          com.google.protobuf.ExtensionRegistryLite extensionRegistry)
          throws com.google.protobuf.InvalidProtocolBufferException {
          return new FeatureFlag(input, extensionRegistry);
      }
    };

    public static com.google.protobuf.Parser<FeatureFlag> parser() {
      return PARSER;
    }

    @java.lang.Override
    public com.google.protobuf.Parser<FeatureFlag> getParserForType() {
      return PARSER;
    }

    public ru.yandex.ydb.common.CommonProtos.FeatureFlag getDefaultInstanceForType() {
      return DEFAULT_INSTANCE;
    }

  }

  private static final com.google.protobuf.Descriptors.Descriptor
    internal_static_Ydb_FeatureFlag_descriptor;
  private static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_Ydb_FeatureFlag_fieldAccessorTable;

  public static com.google.protobuf.Descriptors.FileDescriptor
      getDescriptor() {
    return descriptor;
  }
  private static  com.google.protobuf.Descriptors.FileDescriptor
      descriptor;
  static {
    java.lang.String[] descriptorData = {
      "\n)kikimr/public/api/protos/ydb_common.pr" +
      "oto\022\003Ydb\"J\n\013FeatureFlag\";\n\006Status\022\026\n\022STA" +
      "TUS_UNSPECIFIED\020\000\022\013\n\007ENABLED\020\001\022\014\n\010DISABL" +
      "ED\020\002B\'\n\024ru.yandex.ydb.commonB\014CommonProt" +
      "os\370\001\001b\006proto3"
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
    internal_static_Ydb_FeatureFlag_descriptor =
      getDescriptor().getMessageTypes().get(0);
    internal_static_Ydb_FeatureFlag_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_Ydb_FeatureFlag_descriptor,
        new java.lang.String[] { });
  }

  // @@protoc_insertion_point(outer_class_scope)
}
