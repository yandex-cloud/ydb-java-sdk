// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: kikimr/public/api/protos/ydb_coordination.proto

package ru.yandex.ydb.coordination;

/**
 * Protobuf type {@code Ydb.Coordination.DropNodeRequest}
 */
public  final class DropNodeRequest extends
    com.google.protobuf.GeneratedMessageV3 implements
    // @@protoc_insertion_point(message_implements:Ydb.Coordination.DropNodeRequest)
    DropNodeRequestOrBuilder {
private static final long serialVersionUID = 0L;
  // Use DropNodeRequest.newBuilder() to construct.
  private DropNodeRequest(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
    super(builder);
  }
  private DropNodeRequest() {
    path_ = "";
  }

  @java.lang.Override
  public final com.google.protobuf.UnknownFieldSet
  getUnknownFields() {
    return this.unknownFields;
  }
  private DropNodeRequest(
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
            if (!parseUnknownFieldProto3(
                input, unknownFields, extensionRegistry, tag)) {
              done = true;
            }
            break;
          }
          case 10: {
            java.lang.String s = input.readStringRequireUtf8();

            path_ = s;
            break;
          }
          case 18: {
            ru.yandex.ydb.OperationProtos.OperationParams.Builder subBuilder = null;
            if (operationParams_ != null) {
              subBuilder = operationParams_.toBuilder();
            }
            operationParams_ = input.readMessage(ru.yandex.ydb.OperationProtos.OperationParams.parser(), extensionRegistry);
            if (subBuilder != null) {
              subBuilder.mergeFrom(operationParams_);
              operationParams_ = subBuilder.buildPartial();
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
    return ru.yandex.ydb.coordination.CoordinationProtos.internal_static_Ydb_Coordination_DropNodeRequest_descriptor;
  }

  protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internalGetFieldAccessorTable() {
    return ru.yandex.ydb.coordination.CoordinationProtos.internal_static_Ydb_Coordination_DropNodeRequest_fieldAccessorTable
        .ensureFieldAccessorsInitialized(
            ru.yandex.ydb.coordination.DropNodeRequest.class, ru.yandex.ydb.coordination.DropNodeRequest.Builder.class);
  }

  public static final int PATH_FIELD_NUMBER = 1;
  private volatile java.lang.Object path_;
  /**
   * <code>string path = 1;</code>
   */
  public java.lang.String getPath() {
    java.lang.Object ref = path_;
    if (ref instanceof java.lang.String) {
      return (java.lang.String) ref;
    } else {
      com.google.protobuf.ByteString bs = 
          (com.google.protobuf.ByteString) ref;
      java.lang.String s = bs.toStringUtf8();
      path_ = s;
      return s;
    }
  }
  /**
   * <code>string path = 1;</code>
   */
  public com.google.protobuf.ByteString
      getPathBytes() {
    java.lang.Object ref = path_;
    if (ref instanceof java.lang.String) {
      com.google.protobuf.ByteString b = 
          com.google.protobuf.ByteString.copyFromUtf8(
              (java.lang.String) ref);
      path_ = b;
      return b;
    } else {
      return (com.google.protobuf.ByteString) ref;
    }
  }

  public static final int OPERATION_PARAMS_FIELD_NUMBER = 2;
  private ru.yandex.ydb.OperationProtos.OperationParams operationParams_;
  /**
   * <code>.Ydb.Operations.OperationParams operation_params = 2;</code>
   */
  public boolean hasOperationParams() {
    return operationParams_ != null;
  }
  /**
   * <code>.Ydb.Operations.OperationParams operation_params = 2;</code>
   */
  public ru.yandex.ydb.OperationProtos.OperationParams getOperationParams() {
    return operationParams_ == null ? ru.yandex.ydb.OperationProtos.OperationParams.getDefaultInstance() : operationParams_;
  }
  /**
   * <code>.Ydb.Operations.OperationParams operation_params = 2;</code>
   */
  public ru.yandex.ydb.OperationProtos.OperationParamsOrBuilder getOperationParamsOrBuilder() {
    return getOperationParams();
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
    if (!getPathBytes().isEmpty()) {
      com.google.protobuf.GeneratedMessageV3.writeString(output, 1, path_);
    }
    if (operationParams_ != null) {
      output.writeMessage(2, getOperationParams());
    }
    unknownFields.writeTo(output);
  }

  public int getSerializedSize() {
    int size = memoizedSize;
    if (size != -1) return size;

    size = 0;
    if (!getPathBytes().isEmpty()) {
      size += com.google.protobuf.GeneratedMessageV3.computeStringSize(1, path_);
    }
    if (operationParams_ != null) {
      size += com.google.protobuf.CodedOutputStream
        .computeMessageSize(2, getOperationParams());
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
    if (!(obj instanceof ru.yandex.ydb.coordination.DropNodeRequest)) {
      return super.equals(obj);
    }
    ru.yandex.ydb.coordination.DropNodeRequest other = (ru.yandex.ydb.coordination.DropNodeRequest) obj;

    boolean result = true;
    result = result && getPath()
        .equals(other.getPath());
    result = result && (hasOperationParams() == other.hasOperationParams());
    if (hasOperationParams()) {
      result = result && getOperationParams()
          .equals(other.getOperationParams());
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
    hash = (37 * hash) + PATH_FIELD_NUMBER;
    hash = (53 * hash) + getPath().hashCode();
    if (hasOperationParams()) {
      hash = (37 * hash) + OPERATION_PARAMS_FIELD_NUMBER;
      hash = (53 * hash) + getOperationParams().hashCode();
    }
    hash = (29 * hash) + unknownFields.hashCode();
    memoizedHashCode = hash;
    return hash;
  }

  public static ru.yandex.ydb.coordination.DropNodeRequest parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static ru.yandex.ydb.coordination.DropNodeRequest parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static ru.yandex.ydb.coordination.DropNodeRequest parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static ru.yandex.ydb.coordination.DropNodeRequest parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static ru.yandex.ydb.coordination.DropNodeRequest parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static ru.yandex.ydb.coordination.DropNodeRequest parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static ru.yandex.ydb.coordination.DropNodeRequest parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static ru.yandex.ydb.coordination.DropNodeRequest parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input, extensionRegistry);
  }
  public static ru.yandex.ydb.coordination.DropNodeRequest parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input);
  }
  public static ru.yandex.ydb.coordination.DropNodeRequest parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
  }
  public static ru.yandex.ydb.coordination.DropNodeRequest parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static ru.yandex.ydb.coordination.DropNodeRequest parseFrom(
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
  public static Builder newBuilder(ru.yandex.ydb.coordination.DropNodeRequest prototype) {
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
   * Protobuf type {@code Ydb.Coordination.DropNodeRequest}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessageV3.Builder<Builder> implements
      // @@protoc_insertion_point(builder_implements:Ydb.Coordination.DropNodeRequest)
      ru.yandex.ydb.coordination.DropNodeRequestOrBuilder {
    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return ru.yandex.ydb.coordination.CoordinationProtos.internal_static_Ydb_Coordination_DropNodeRequest_descriptor;
    }

    protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return ru.yandex.ydb.coordination.CoordinationProtos.internal_static_Ydb_Coordination_DropNodeRequest_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              ru.yandex.ydb.coordination.DropNodeRequest.class, ru.yandex.ydb.coordination.DropNodeRequest.Builder.class);
    }

    // Construct using ru.yandex.ydb.coordination.DropNodeRequest.newBuilder()
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
      path_ = "";

      if (operationParamsBuilder_ == null) {
        operationParams_ = null;
      } else {
        operationParams_ = null;
        operationParamsBuilder_ = null;
      }
      return this;
    }

    public com.google.protobuf.Descriptors.Descriptor
        getDescriptorForType() {
      return ru.yandex.ydb.coordination.CoordinationProtos.internal_static_Ydb_Coordination_DropNodeRequest_descriptor;
    }

    public ru.yandex.ydb.coordination.DropNodeRequest getDefaultInstanceForType() {
      return ru.yandex.ydb.coordination.DropNodeRequest.getDefaultInstance();
    }

    public ru.yandex.ydb.coordination.DropNodeRequest build() {
      ru.yandex.ydb.coordination.DropNodeRequest result = buildPartial();
      if (!result.isInitialized()) {
        throw newUninitializedMessageException(result);
      }
      return result;
    }

    public ru.yandex.ydb.coordination.DropNodeRequest buildPartial() {
      ru.yandex.ydb.coordination.DropNodeRequest result = new ru.yandex.ydb.coordination.DropNodeRequest(this);
      result.path_ = path_;
      if (operationParamsBuilder_ == null) {
        result.operationParams_ = operationParams_;
      } else {
        result.operationParams_ = operationParamsBuilder_.build();
      }
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
      if (other instanceof ru.yandex.ydb.coordination.DropNodeRequest) {
        return mergeFrom((ru.yandex.ydb.coordination.DropNodeRequest)other);
      } else {
        super.mergeFrom(other);
        return this;
      }
    }

    public Builder mergeFrom(ru.yandex.ydb.coordination.DropNodeRequest other) {
      if (other == ru.yandex.ydb.coordination.DropNodeRequest.getDefaultInstance()) return this;
      if (!other.getPath().isEmpty()) {
        path_ = other.path_;
        onChanged();
      }
      if (other.hasOperationParams()) {
        mergeOperationParams(other.getOperationParams());
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
      ru.yandex.ydb.coordination.DropNodeRequest parsedMessage = null;
      try {
        parsedMessage = PARSER.parsePartialFrom(input, extensionRegistry);
      } catch (com.google.protobuf.InvalidProtocolBufferException e) {
        parsedMessage = (ru.yandex.ydb.coordination.DropNodeRequest) e.getUnfinishedMessage();
        throw e.unwrapIOException();
      } finally {
        if (parsedMessage != null) {
          mergeFrom(parsedMessage);
        }
      }
      return this;
    }

    private java.lang.Object path_ = "";
    /**
     * <code>string path = 1;</code>
     */
    public java.lang.String getPath() {
      java.lang.Object ref = path_;
      if (!(ref instanceof java.lang.String)) {
        com.google.protobuf.ByteString bs =
            (com.google.protobuf.ByteString) ref;
        java.lang.String s = bs.toStringUtf8();
        path_ = s;
        return s;
      } else {
        return (java.lang.String) ref;
      }
    }
    /**
     * <code>string path = 1;</code>
     */
    public com.google.protobuf.ByteString
        getPathBytes() {
      java.lang.Object ref = path_;
      if (ref instanceof String) {
        com.google.protobuf.ByteString b = 
            com.google.protobuf.ByteString.copyFromUtf8(
                (java.lang.String) ref);
        path_ = b;
        return b;
      } else {
        return (com.google.protobuf.ByteString) ref;
      }
    }
    /**
     * <code>string path = 1;</code>
     */
    public Builder setPath(
        java.lang.String value) {
      if (value == null) {
    throw new NullPointerException();
  }
  
      path_ = value;
      onChanged();
      return this;
    }
    /**
     * <code>string path = 1;</code>
     */
    public Builder clearPath() {
      
      path_ = getDefaultInstance().getPath();
      onChanged();
      return this;
    }
    /**
     * <code>string path = 1;</code>
     */
    public Builder setPathBytes(
        com.google.protobuf.ByteString value) {
      if (value == null) {
    throw new NullPointerException();
  }
  checkByteStringIsUtf8(value);
      
      path_ = value;
      onChanged();
      return this;
    }

    private ru.yandex.ydb.OperationProtos.OperationParams operationParams_ = null;
    private com.google.protobuf.SingleFieldBuilderV3<
        ru.yandex.ydb.OperationProtos.OperationParams, ru.yandex.ydb.OperationProtos.OperationParams.Builder, ru.yandex.ydb.OperationProtos.OperationParamsOrBuilder> operationParamsBuilder_;
    /**
     * <code>.Ydb.Operations.OperationParams operation_params = 2;</code>
     */
    public boolean hasOperationParams() {
      return operationParamsBuilder_ != null || operationParams_ != null;
    }
    /**
     * <code>.Ydb.Operations.OperationParams operation_params = 2;</code>
     */
    public ru.yandex.ydb.OperationProtos.OperationParams getOperationParams() {
      if (operationParamsBuilder_ == null) {
        return operationParams_ == null ? ru.yandex.ydb.OperationProtos.OperationParams.getDefaultInstance() : operationParams_;
      } else {
        return operationParamsBuilder_.getMessage();
      }
    }
    /**
     * <code>.Ydb.Operations.OperationParams operation_params = 2;</code>
     */
    public Builder setOperationParams(ru.yandex.ydb.OperationProtos.OperationParams value) {
      if (operationParamsBuilder_ == null) {
        if (value == null) {
          throw new NullPointerException();
        }
        operationParams_ = value;
        onChanged();
      } else {
        operationParamsBuilder_.setMessage(value);
      }

      return this;
    }
    /**
     * <code>.Ydb.Operations.OperationParams operation_params = 2;</code>
     */
    public Builder setOperationParams(
        ru.yandex.ydb.OperationProtos.OperationParams.Builder builderForValue) {
      if (operationParamsBuilder_ == null) {
        operationParams_ = builderForValue.build();
        onChanged();
      } else {
        operationParamsBuilder_.setMessage(builderForValue.build());
      }

      return this;
    }
    /**
     * <code>.Ydb.Operations.OperationParams operation_params = 2;</code>
     */
    public Builder mergeOperationParams(ru.yandex.ydb.OperationProtos.OperationParams value) {
      if (operationParamsBuilder_ == null) {
        if (operationParams_ != null) {
          operationParams_ =
            ru.yandex.ydb.OperationProtos.OperationParams.newBuilder(operationParams_).mergeFrom(value).buildPartial();
        } else {
          operationParams_ = value;
        }
        onChanged();
      } else {
        operationParamsBuilder_.mergeFrom(value);
      }

      return this;
    }
    /**
     * <code>.Ydb.Operations.OperationParams operation_params = 2;</code>
     */
    public Builder clearOperationParams() {
      if (operationParamsBuilder_ == null) {
        operationParams_ = null;
        onChanged();
      } else {
        operationParams_ = null;
        operationParamsBuilder_ = null;
      }

      return this;
    }
    /**
     * <code>.Ydb.Operations.OperationParams operation_params = 2;</code>
     */
    public ru.yandex.ydb.OperationProtos.OperationParams.Builder getOperationParamsBuilder() {
      
      onChanged();
      return getOperationParamsFieldBuilder().getBuilder();
    }
    /**
     * <code>.Ydb.Operations.OperationParams operation_params = 2;</code>
     */
    public ru.yandex.ydb.OperationProtos.OperationParamsOrBuilder getOperationParamsOrBuilder() {
      if (operationParamsBuilder_ != null) {
        return operationParamsBuilder_.getMessageOrBuilder();
      } else {
        return operationParams_ == null ?
            ru.yandex.ydb.OperationProtos.OperationParams.getDefaultInstance() : operationParams_;
      }
    }
    /**
     * <code>.Ydb.Operations.OperationParams operation_params = 2;</code>
     */
    private com.google.protobuf.SingleFieldBuilderV3<
        ru.yandex.ydb.OperationProtos.OperationParams, ru.yandex.ydb.OperationProtos.OperationParams.Builder, ru.yandex.ydb.OperationProtos.OperationParamsOrBuilder> 
        getOperationParamsFieldBuilder() {
      if (operationParamsBuilder_ == null) {
        operationParamsBuilder_ = new com.google.protobuf.SingleFieldBuilderV3<
            ru.yandex.ydb.OperationProtos.OperationParams, ru.yandex.ydb.OperationProtos.OperationParams.Builder, ru.yandex.ydb.OperationProtos.OperationParamsOrBuilder>(
                getOperationParams(),
                getParentForChildren(),
                isClean());
        operationParams_ = null;
      }
      return operationParamsBuilder_;
    }
    public final Builder setUnknownFields(
        final com.google.protobuf.UnknownFieldSet unknownFields) {
      return super.setUnknownFieldsProto3(unknownFields);
    }

    public final Builder mergeUnknownFields(
        final com.google.protobuf.UnknownFieldSet unknownFields) {
      return super.mergeUnknownFields(unknownFields);
    }


    // @@protoc_insertion_point(builder_scope:Ydb.Coordination.DropNodeRequest)
  }

  // @@protoc_insertion_point(class_scope:Ydb.Coordination.DropNodeRequest)
  private static final ru.yandex.ydb.coordination.DropNodeRequest DEFAULT_INSTANCE;
  static {
    DEFAULT_INSTANCE = new ru.yandex.ydb.coordination.DropNodeRequest();
  }

  public static ru.yandex.ydb.coordination.DropNodeRequest getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static final com.google.protobuf.Parser<DropNodeRequest>
      PARSER = new com.google.protobuf.AbstractParser<DropNodeRequest>() {
    public DropNodeRequest parsePartialFrom(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
        return new DropNodeRequest(input, extensionRegistry);
    }
  };

  public static com.google.protobuf.Parser<DropNodeRequest> parser() {
    return PARSER;
  }

  @java.lang.Override
  public com.google.protobuf.Parser<DropNodeRequest> getParserForType() {
    return PARSER;
  }

  public ru.yandex.ydb.coordination.DropNodeRequest getDefaultInstanceForType() {
    return DEFAULT_INSTANCE;
  }

}

