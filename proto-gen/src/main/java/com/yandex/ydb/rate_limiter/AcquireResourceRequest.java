// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: kikimr/public/api/protos/ydb_rate_limiter.proto

package com.yandex.ydb.rate_limiter;

/**
 * Protobuf type {@code Ydb.RateLimiter.AcquireResourceRequest}
 */
public final class AcquireResourceRequest extends
    com.google.protobuf.GeneratedMessageV3 implements
    // @@protoc_insertion_point(message_implements:Ydb.RateLimiter.AcquireResourceRequest)
    AcquireResourceRequestOrBuilder {
private static final long serialVersionUID = 0L;
  // Use AcquireResourceRequest.newBuilder() to construct.
  private AcquireResourceRequest(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
    super(builder);
  }
  private AcquireResourceRequest() {
    coordinationNodePath_ = "";
    resourcePath_ = "";
  }

  @java.lang.Override
  @SuppressWarnings({"unused"})
  protected java.lang.Object newInstance(
      UnusedPrivateParameter unused) {
    return new AcquireResourceRequest();
  }

  @java.lang.Override
  public final com.google.protobuf.UnknownFieldSet
  getUnknownFields() {
    return this.unknownFields;
  }
  private AcquireResourceRequest(
      com.google.protobuf.CodedInputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    this();
    if (extensionRegistry == null) {
      throw new java.lang.NullPointerException();
    }
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
          case 10: {
            com.yandex.ydb.OperationProtos.OperationParams.Builder subBuilder = null;
            if (operationParams_ != null) {
              subBuilder = operationParams_.toBuilder();
            }
            operationParams_ = input.readMessage(com.yandex.ydb.OperationProtos.OperationParams.parser(), extensionRegistry);
            if (subBuilder != null) {
              subBuilder.mergeFrom(operationParams_);
              operationParams_ = subBuilder.buildPartial();
            }

            break;
          }
          case 18: {
            java.lang.String s = input.readStringRequireUtf8();

            coordinationNodePath_ = s;
            break;
          }
          case 26: {
            java.lang.String s = input.readStringRequireUtf8();

            resourcePath_ = s;
            break;
          }
          case 32: {
            unitsCase_ = 4;
            units_ = input.readUInt64();
            break;
          }
          case 40: {
            unitsCase_ = 5;
            units_ = input.readUInt64();
            break;
          }
          default: {
            if (!parseUnknownField(
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
    return com.yandex.ydb.rate_limiter.RateLimiterProtos.internal_static_Ydb_RateLimiter_AcquireResourceRequest_descriptor;
  }

  @java.lang.Override
  protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internalGetFieldAccessorTable() {
    return com.yandex.ydb.rate_limiter.RateLimiterProtos.internal_static_Ydb_RateLimiter_AcquireResourceRequest_fieldAccessorTable
        .ensureFieldAccessorsInitialized(
            com.yandex.ydb.rate_limiter.AcquireResourceRequest.class, com.yandex.ydb.rate_limiter.AcquireResourceRequest.Builder.class);
  }

  private int unitsCase_ = 0;
  private java.lang.Object units_;
  public enum UnitsCase
      implements com.google.protobuf.Internal.EnumLite,
          com.google.protobuf.AbstractMessage.InternalOneOfEnum {
    REQUIRED(4),
    USED(5),
    UNITS_NOT_SET(0);
    private final int value;
    private UnitsCase(int value) {
      this.value = value;
    }
    /**
     * @param value The number of the enum to look for.
     * @return The enum associated with the given number.
     * @deprecated Use {@link #forNumber(int)} instead.
     */
    @java.lang.Deprecated
    public static UnitsCase valueOf(int value) {
      return forNumber(value);
    }

    public static UnitsCase forNumber(int value) {
      switch (value) {
        case 4: return REQUIRED;
        case 5: return USED;
        case 0: return UNITS_NOT_SET;
        default: return null;
      }
    }
    public int getNumber() {
      return this.value;
    }
  };

  public UnitsCase
  getUnitsCase() {
    return UnitsCase.forNumber(
        unitsCase_);
  }

  public static final int OPERATION_PARAMS_FIELD_NUMBER = 1;
  private com.yandex.ydb.OperationProtos.OperationParams operationParams_;
  /**
   * <code>.Ydb.Operations.OperationParams operation_params = 1;</code>
   * @return Whether the operationParams field is set.
   */
  @java.lang.Override
  public boolean hasOperationParams() {
    return operationParams_ != null;
  }
  /**
   * <code>.Ydb.Operations.OperationParams operation_params = 1;</code>
   * @return The operationParams.
   */
  @java.lang.Override
  public com.yandex.ydb.OperationProtos.OperationParams getOperationParams() {
    return operationParams_ == null ? com.yandex.ydb.OperationProtos.OperationParams.getDefaultInstance() : operationParams_;
  }
  /**
   * <code>.Ydb.Operations.OperationParams operation_params = 1;</code>
   */
  @java.lang.Override
  public com.yandex.ydb.OperationProtos.OperationParamsOrBuilder getOperationParamsOrBuilder() {
    return getOperationParams();
  }

  public static final int COORDINATION_NODE_PATH_FIELD_NUMBER = 2;
  private volatile java.lang.Object coordinationNodePath_;
  /**
   * <pre>
   * Path of a coordination node.
   * </pre>
   *
   * <code>string coordination_node_path = 2;</code>
   * @return The coordinationNodePath.
   */
  @java.lang.Override
  public java.lang.String getCoordinationNodePath() {
    java.lang.Object ref = coordinationNodePath_;
    if (ref instanceof java.lang.String) {
      return (java.lang.String) ref;
    } else {
      com.google.protobuf.ByteString bs = 
          (com.google.protobuf.ByteString) ref;
      java.lang.String s = bs.toStringUtf8();
      coordinationNodePath_ = s;
      return s;
    }
  }
  /**
   * <pre>
   * Path of a coordination node.
   * </pre>
   *
   * <code>string coordination_node_path = 2;</code>
   * @return The bytes for coordinationNodePath.
   */
  @java.lang.Override
  public com.google.protobuf.ByteString
      getCoordinationNodePathBytes() {
    java.lang.Object ref = coordinationNodePath_;
    if (ref instanceof java.lang.String) {
      com.google.protobuf.ByteString b = 
          com.google.protobuf.ByteString.copyFromUtf8(
              (java.lang.String) ref);
      coordinationNodePath_ = b;
      return b;
    } else {
      return (com.google.protobuf.ByteString) ref;
    }
  }

  public static final int RESOURCE_PATH_FIELD_NUMBER = 3;
  private volatile java.lang.Object resourcePath_;
  /**
   * <pre>
   * Path of resource inside a coordination node.
   * </pre>
   *
   * <code>string resource_path = 3;</code>
   * @return The resourcePath.
   */
  @java.lang.Override
  public java.lang.String getResourcePath() {
    java.lang.Object ref = resourcePath_;
    if (ref instanceof java.lang.String) {
      return (java.lang.String) ref;
    } else {
      com.google.protobuf.ByteString bs = 
          (com.google.protobuf.ByteString) ref;
      java.lang.String s = bs.toStringUtf8();
      resourcePath_ = s;
      return s;
    }
  }
  /**
   * <pre>
   * Path of resource inside a coordination node.
   * </pre>
   *
   * <code>string resource_path = 3;</code>
   * @return The bytes for resourcePath.
   */
  @java.lang.Override
  public com.google.protobuf.ByteString
      getResourcePathBytes() {
    java.lang.Object ref = resourcePath_;
    if (ref instanceof java.lang.String) {
      com.google.protobuf.ByteString b = 
          com.google.protobuf.ByteString.copyFromUtf8(
              (java.lang.String) ref);
      resourcePath_ = b;
      return b;
    } else {
      return (com.google.protobuf.ByteString) ref;
    }
  }

  public static final int REQUIRED_FIELD_NUMBER = 4;
  /**
   * <pre>
   * Request resource's units for usage.
   * </pre>
   *
   * <code>uint64 required = 4;</code>
   * @return Whether the required field is set.
   */
  @java.lang.Override
  public boolean hasRequired() {
    return unitsCase_ == 4;
  }
  /**
   * <pre>
   * Request resource's units for usage.
   * </pre>
   *
   * <code>uint64 required = 4;</code>
   * @return The required.
   */
  @java.lang.Override
  public long getRequired() {
    if (unitsCase_ == 4) {
      return (java.lang.Long) units_;
    }
    return 0L;
  }

  public static final int USED_FIELD_NUMBER = 5;
  /**
   * <pre>
   * Actually used resource's units by client.
   * </pre>
   *
   * <code>uint64 used = 5;</code>
   * @return Whether the used field is set.
   */
  @java.lang.Override
  public boolean hasUsed() {
    return unitsCase_ == 5;
  }
  /**
   * <pre>
   * Actually used resource's units by client.
   * </pre>
   *
   * <code>uint64 used = 5;</code>
   * @return The used.
   */
  @java.lang.Override
  public long getUsed() {
    if (unitsCase_ == 5) {
      return (java.lang.Long) units_;
    }
    return 0L;
  }

  private byte memoizedIsInitialized = -1;
  @java.lang.Override
  public final boolean isInitialized() {
    byte isInitialized = memoizedIsInitialized;
    if (isInitialized == 1) return true;
    if (isInitialized == 0) return false;

    memoizedIsInitialized = 1;
    return true;
  }

  @java.lang.Override
  public void writeTo(com.google.protobuf.CodedOutputStream output)
                      throws java.io.IOException {
    if (operationParams_ != null) {
      output.writeMessage(1, getOperationParams());
    }
    if (!getCoordinationNodePathBytes().isEmpty()) {
      com.google.protobuf.GeneratedMessageV3.writeString(output, 2, coordinationNodePath_);
    }
    if (!getResourcePathBytes().isEmpty()) {
      com.google.protobuf.GeneratedMessageV3.writeString(output, 3, resourcePath_);
    }
    if (unitsCase_ == 4) {
      output.writeUInt64(
          4, (long)((java.lang.Long) units_));
    }
    if (unitsCase_ == 5) {
      output.writeUInt64(
          5, (long)((java.lang.Long) units_));
    }
    unknownFields.writeTo(output);
  }

  @java.lang.Override
  public int getSerializedSize() {
    int size = memoizedSize;
    if (size != -1) return size;

    size = 0;
    if (operationParams_ != null) {
      size += com.google.protobuf.CodedOutputStream
        .computeMessageSize(1, getOperationParams());
    }
    if (!getCoordinationNodePathBytes().isEmpty()) {
      size += com.google.protobuf.GeneratedMessageV3.computeStringSize(2, coordinationNodePath_);
    }
    if (!getResourcePathBytes().isEmpty()) {
      size += com.google.protobuf.GeneratedMessageV3.computeStringSize(3, resourcePath_);
    }
    if (unitsCase_ == 4) {
      size += com.google.protobuf.CodedOutputStream
        .computeUInt64Size(
            4, (long)((java.lang.Long) units_));
    }
    if (unitsCase_ == 5) {
      size += com.google.protobuf.CodedOutputStream
        .computeUInt64Size(
            5, (long)((java.lang.Long) units_));
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
    if (!(obj instanceof com.yandex.ydb.rate_limiter.AcquireResourceRequest)) {
      return super.equals(obj);
    }
    com.yandex.ydb.rate_limiter.AcquireResourceRequest other = (com.yandex.ydb.rate_limiter.AcquireResourceRequest) obj;

    if (hasOperationParams() != other.hasOperationParams()) return false;
    if (hasOperationParams()) {
      if (!getOperationParams()
          .equals(other.getOperationParams())) return false;
    }
    if (!getCoordinationNodePath()
        .equals(other.getCoordinationNodePath())) return false;
    if (!getResourcePath()
        .equals(other.getResourcePath())) return false;
    if (!getUnitsCase().equals(other.getUnitsCase())) return false;
    switch (unitsCase_) {
      case 4:
        if (getRequired()
            != other.getRequired()) return false;
        break;
      case 5:
        if (getUsed()
            != other.getUsed()) return false;
        break;
      case 0:
      default:
    }
    if (!unknownFields.equals(other.unknownFields)) return false;
    return true;
  }

  @java.lang.Override
  public int hashCode() {
    if (memoizedHashCode != 0) {
      return memoizedHashCode;
    }
    int hash = 41;
    hash = (19 * hash) + getDescriptor().hashCode();
    if (hasOperationParams()) {
      hash = (37 * hash) + OPERATION_PARAMS_FIELD_NUMBER;
      hash = (53 * hash) + getOperationParams().hashCode();
    }
    hash = (37 * hash) + COORDINATION_NODE_PATH_FIELD_NUMBER;
    hash = (53 * hash) + getCoordinationNodePath().hashCode();
    hash = (37 * hash) + RESOURCE_PATH_FIELD_NUMBER;
    hash = (53 * hash) + getResourcePath().hashCode();
    switch (unitsCase_) {
      case 4:
        hash = (37 * hash) + REQUIRED_FIELD_NUMBER;
        hash = (53 * hash) + com.google.protobuf.Internal.hashLong(
            getRequired());
        break;
      case 5:
        hash = (37 * hash) + USED_FIELD_NUMBER;
        hash = (53 * hash) + com.google.protobuf.Internal.hashLong(
            getUsed());
        break;
      case 0:
      default:
    }
    hash = (29 * hash) + unknownFields.hashCode();
    memoizedHashCode = hash;
    return hash;
  }

  public static com.yandex.ydb.rate_limiter.AcquireResourceRequest parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static com.yandex.ydb.rate_limiter.AcquireResourceRequest parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static com.yandex.ydb.rate_limiter.AcquireResourceRequest parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static com.yandex.ydb.rate_limiter.AcquireResourceRequest parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static com.yandex.ydb.rate_limiter.AcquireResourceRequest parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static com.yandex.ydb.rate_limiter.AcquireResourceRequest parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static com.yandex.ydb.rate_limiter.AcquireResourceRequest parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static com.yandex.ydb.rate_limiter.AcquireResourceRequest parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input, extensionRegistry);
  }
  public static com.yandex.ydb.rate_limiter.AcquireResourceRequest parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input);
  }
  public static com.yandex.ydb.rate_limiter.AcquireResourceRequest parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
  }
  public static com.yandex.ydb.rate_limiter.AcquireResourceRequest parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static com.yandex.ydb.rate_limiter.AcquireResourceRequest parseFrom(
      com.google.protobuf.CodedInputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input, extensionRegistry);
  }

  @java.lang.Override
  public Builder newBuilderForType() { return newBuilder(); }
  public static Builder newBuilder() {
    return DEFAULT_INSTANCE.toBuilder();
  }
  public static Builder newBuilder(com.yandex.ydb.rate_limiter.AcquireResourceRequest prototype) {
    return DEFAULT_INSTANCE.toBuilder().mergeFrom(prototype);
  }
  @java.lang.Override
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
   * Protobuf type {@code Ydb.RateLimiter.AcquireResourceRequest}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessageV3.Builder<Builder> implements
      // @@protoc_insertion_point(builder_implements:Ydb.RateLimiter.AcquireResourceRequest)
      com.yandex.ydb.rate_limiter.AcquireResourceRequestOrBuilder {
    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return com.yandex.ydb.rate_limiter.RateLimiterProtos.internal_static_Ydb_RateLimiter_AcquireResourceRequest_descriptor;
    }

    @java.lang.Override
    protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return com.yandex.ydb.rate_limiter.RateLimiterProtos.internal_static_Ydb_RateLimiter_AcquireResourceRequest_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              com.yandex.ydb.rate_limiter.AcquireResourceRequest.class, com.yandex.ydb.rate_limiter.AcquireResourceRequest.Builder.class);
    }

    // Construct using com.yandex.ydb.rate_limiter.AcquireResourceRequest.newBuilder()
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
    @java.lang.Override
    public Builder clear() {
      super.clear();
      if (operationParamsBuilder_ == null) {
        operationParams_ = null;
      } else {
        operationParams_ = null;
        operationParamsBuilder_ = null;
      }
      coordinationNodePath_ = "";

      resourcePath_ = "";

      unitsCase_ = 0;
      units_ = null;
      return this;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.Descriptor
        getDescriptorForType() {
      return com.yandex.ydb.rate_limiter.RateLimiterProtos.internal_static_Ydb_RateLimiter_AcquireResourceRequest_descriptor;
    }

    @java.lang.Override
    public com.yandex.ydb.rate_limiter.AcquireResourceRequest getDefaultInstanceForType() {
      return com.yandex.ydb.rate_limiter.AcquireResourceRequest.getDefaultInstance();
    }

    @java.lang.Override
    public com.yandex.ydb.rate_limiter.AcquireResourceRequest build() {
      com.yandex.ydb.rate_limiter.AcquireResourceRequest result = buildPartial();
      if (!result.isInitialized()) {
        throw newUninitializedMessageException(result);
      }
      return result;
    }

    @java.lang.Override
    public com.yandex.ydb.rate_limiter.AcquireResourceRequest buildPartial() {
      com.yandex.ydb.rate_limiter.AcquireResourceRequest result = new com.yandex.ydb.rate_limiter.AcquireResourceRequest(this);
      if (operationParamsBuilder_ == null) {
        result.operationParams_ = operationParams_;
      } else {
        result.operationParams_ = operationParamsBuilder_.build();
      }
      result.coordinationNodePath_ = coordinationNodePath_;
      result.resourcePath_ = resourcePath_;
      if (unitsCase_ == 4) {
        result.units_ = units_;
      }
      if (unitsCase_ == 5) {
        result.units_ = units_;
      }
      result.unitsCase_ = unitsCase_;
      onBuilt();
      return result;
    }

    @java.lang.Override
    public Builder clone() {
      return super.clone();
    }
    @java.lang.Override
    public Builder setField(
        com.google.protobuf.Descriptors.FieldDescriptor field,
        java.lang.Object value) {
      return super.setField(field, value);
    }
    @java.lang.Override
    public Builder clearField(
        com.google.protobuf.Descriptors.FieldDescriptor field) {
      return super.clearField(field);
    }
    @java.lang.Override
    public Builder clearOneof(
        com.google.protobuf.Descriptors.OneofDescriptor oneof) {
      return super.clearOneof(oneof);
    }
    @java.lang.Override
    public Builder setRepeatedField(
        com.google.protobuf.Descriptors.FieldDescriptor field,
        int index, java.lang.Object value) {
      return super.setRepeatedField(field, index, value);
    }
    @java.lang.Override
    public Builder addRepeatedField(
        com.google.protobuf.Descriptors.FieldDescriptor field,
        java.lang.Object value) {
      return super.addRepeatedField(field, value);
    }
    @java.lang.Override
    public Builder mergeFrom(com.google.protobuf.Message other) {
      if (other instanceof com.yandex.ydb.rate_limiter.AcquireResourceRequest) {
        return mergeFrom((com.yandex.ydb.rate_limiter.AcquireResourceRequest)other);
      } else {
        super.mergeFrom(other);
        return this;
      }
    }

    public Builder mergeFrom(com.yandex.ydb.rate_limiter.AcquireResourceRequest other) {
      if (other == com.yandex.ydb.rate_limiter.AcquireResourceRequest.getDefaultInstance()) return this;
      if (other.hasOperationParams()) {
        mergeOperationParams(other.getOperationParams());
      }
      if (!other.getCoordinationNodePath().isEmpty()) {
        coordinationNodePath_ = other.coordinationNodePath_;
        onChanged();
      }
      if (!other.getResourcePath().isEmpty()) {
        resourcePath_ = other.resourcePath_;
        onChanged();
      }
      switch (other.getUnitsCase()) {
        case REQUIRED: {
          setRequired(other.getRequired());
          break;
        }
        case USED: {
          setUsed(other.getUsed());
          break;
        }
        case UNITS_NOT_SET: {
          break;
        }
      }
      this.mergeUnknownFields(other.unknownFields);
      onChanged();
      return this;
    }

    @java.lang.Override
    public final boolean isInitialized() {
      return true;
    }

    @java.lang.Override
    public Builder mergeFrom(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      com.yandex.ydb.rate_limiter.AcquireResourceRequest parsedMessage = null;
      try {
        parsedMessage = PARSER.parsePartialFrom(input, extensionRegistry);
      } catch (com.google.protobuf.InvalidProtocolBufferException e) {
        parsedMessage = (com.yandex.ydb.rate_limiter.AcquireResourceRequest) e.getUnfinishedMessage();
        throw e.unwrapIOException();
      } finally {
        if (parsedMessage != null) {
          mergeFrom(parsedMessage);
        }
      }
      return this;
    }
    private int unitsCase_ = 0;
    private java.lang.Object units_;
    public UnitsCase
        getUnitsCase() {
      return UnitsCase.forNumber(
          unitsCase_);
    }

    public Builder clearUnits() {
      unitsCase_ = 0;
      units_ = null;
      onChanged();
      return this;
    }


    private com.yandex.ydb.OperationProtos.OperationParams operationParams_;
    private com.google.protobuf.SingleFieldBuilderV3<
        com.yandex.ydb.OperationProtos.OperationParams, com.yandex.ydb.OperationProtos.OperationParams.Builder, com.yandex.ydb.OperationProtos.OperationParamsOrBuilder> operationParamsBuilder_;
    /**
     * <code>.Ydb.Operations.OperationParams operation_params = 1;</code>
     * @return Whether the operationParams field is set.
     */
    public boolean hasOperationParams() {
      return operationParamsBuilder_ != null || operationParams_ != null;
    }
    /**
     * <code>.Ydb.Operations.OperationParams operation_params = 1;</code>
     * @return The operationParams.
     */
    public com.yandex.ydb.OperationProtos.OperationParams getOperationParams() {
      if (operationParamsBuilder_ == null) {
        return operationParams_ == null ? com.yandex.ydb.OperationProtos.OperationParams.getDefaultInstance() : operationParams_;
      } else {
        return operationParamsBuilder_.getMessage();
      }
    }
    /**
     * <code>.Ydb.Operations.OperationParams operation_params = 1;</code>
     */
    public Builder setOperationParams(com.yandex.ydb.OperationProtos.OperationParams value) {
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
     * <code>.Ydb.Operations.OperationParams operation_params = 1;</code>
     */
    public Builder setOperationParams(
        com.yandex.ydb.OperationProtos.OperationParams.Builder builderForValue) {
      if (operationParamsBuilder_ == null) {
        operationParams_ = builderForValue.build();
        onChanged();
      } else {
        operationParamsBuilder_.setMessage(builderForValue.build());
      }

      return this;
    }
    /**
     * <code>.Ydb.Operations.OperationParams operation_params = 1;</code>
     */
    public Builder mergeOperationParams(com.yandex.ydb.OperationProtos.OperationParams value) {
      if (operationParamsBuilder_ == null) {
        if (operationParams_ != null) {
          operationParams_ =
            com.yandex.ydb.OperationProtos.OperationParams.newBuilder(operationParams_).mergeFrom(value).buildPartial();
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
     * <code>.Ydb.Operations.OperationParams operation_params = 1;</code>
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
     * <code>.Ydb.Operations.OperationParams operation_params = 1;</code>
     */
    public com.yandex.ydb.OperationProtos.OperationParams.Builder getOperationParamsBuilder() {
      
      onChanged();
      return getOperationParamsFieldBuilder().getBuilder();
    }
    /**
     * <code>.Ydb.Operations.OperationParams operation_params = 1;</code>
     */
    public com.yandex.ydb.OperationProtos.OperationParamsOrBuilder getOperationParamsOrBuilder() {
      if (operationParamsBuilder_ != null) {
        return operationParamsBuilder_.getMessageOrBuilder();
      } else {
        return operationParams_ == null ?
            com.yandex.ydb.OperationProtos.OperationParams.getDefaultInstance() : operationParams_;
      }
    }
    /**
     * <code>.Ydb.Operations.OperationParams operation_params = 1;</code>
     */
    private com.google.protobuf.SingleFieldBuilderV3<
        com.yandex.ydb.OperationProtos.OperationParams, com.yandex.ydb.OperationProtos.OperationParams.Builder, com.yandex.ydb.OperationProtos.OperationParamsOrBuilder> 
        getOperationParamsFieldBuilder() {
      if (operationParamsBuilder_ == null) {
        operationParamsBuilder_ = new com.google.protobuf.SingleFieldBuilderV3<
            com.yandex.ydb.OperationProtos.OperationParams, com.yandex.ydb.OperationProtos.OperationParams.Builder, com.yandex.ydb.OperationProtos.OperationParamsOrBuilder>(
                getOperationParams(),
                getParentForChildren(),
                isClean());
        operationParams_ = null;
      }
      return operationParamsBuilder_;
    }

    private java.lang.Object coordinationNodePath_ = "";
    /**
     * <pre>
     * Path of a coordination node.
     * </pre>
     *
     * <code>string coordination_node_path = 2;</code>
     * @return The coordinationNodePath.
     */
    public java.lang.String getCoordinationNodePath() {
      java.lang.Object ref = coordinationNodePath_;
      if (!(ref instanceof java.lang.String)) {
        com.google.protobuf.ByteString bs =
            (com.google.protobuf.ByteString) ref;
        java.lang.String s = bs.toStringUtf8();
        coordinationNodePath_ = s;
        return s;
      } else {
        return (java.lang.String) ref;
      }
    }
    /**
     * <pre>
     * Path of a coordination node.
     * </pre>
     *
     * <code>string coordination_node_path = 2;</code>
     * @return The bytes for coordinationNodePath.
     */
    public com.google.protobuf.ByteString
        getCoordinationNodePathBytes() {
      java.lang.Object ref = coordinationNodePath_;
      if (ref instanceof String) {
        com.google.protobuf.ByteString b = 
            com.google.protobuf.ByteString.copyFromUtf8(
                (java.lang.String) ref);
        coordinationNodePath_ = b;
        return b;
      } else {
        return (com.google.protobuf.ByteString) ref;
      }
    }
    /**
     * <pre>
     * Path of a coordination node.
     * </pre>
     *
     * <code>string coordination_node_path = 2;</code>
     * @param value The coordinationNodePath to set.
     * @return This builder for chaining.
     */
    public Builder setCoordinationNodePath(
        java.lang.String value) {
      if (value == null) {
    throw new NullPointerException();
  }
  
      coordinationNodePath_ = value;
      onChanged();
      return this;
    }
    /**
     * <pre>
     * Path of a coordination node.
     * </pre>
     *
     * <code>string coordination_node_path = 2;</code>
     * @return This builder for chaining.
     */
    public Builder clearCoordinationNodePath() {
      
      coordinationNodePath_ = getDefaultInstance().getCoordinationNodePath();
      onChanged();
      return this;
    }
    /**
     * <pre>
     * Path of a coordination node.
     * </pre>
     *
     * <code>string coordination_node_path = 2;</code>
     * @param value The bytes for coordinationNodePath to set.
     * @return This builder for chaining.
     */
    public Builder setCoordinationNodePathBytes(
        com.google.protobuf.ByteString value) {
      if (value == null) {
    throw new NullPointerException();
  }
  checkByteStringIsUtf8(value);
      
      coordinationNodePath_ = value;
      onChanged();
      return this;
    }

    private java.lang.Object resourcePath_ = "";
    /**
     * <pre>
     * Path of resource inside a coordination node.
     * </pre>
     *
     * <code>string resource_path = 3;</code>
     * @return The resourcePath.
     */
    public java.lang.String getResourcePath() {
      java.lang.Object ref = resourcePath_;
      if (!(ref instanceof java.lang.String)) {
        com.google.protobuf.ByteString bs =
            (com.google.protobuf.ByteString) ref;
        java.lang.String s = bs.toStringUtf8();
        resourcePath_ = s;
        return s;
      } else {
        return (java.lang.String) ref;
      }
    }
    /**
     * <pre>
     * Path of resource inside a coordination node.
     * </pre>
     *
     * <code>string resource_path = 3;</code>
     * @return The bytes for resourcePath.
     */
    public com.google.protobuf.ByteString
        getResourcePathBytes() {
      java.lang.Object ref = resourcePath_;
      if (ref instanceof String) {
        com.google.protobuf.ByteString b = 
            com.google.protobuf.ByteString.copyFromUtf8(
                (java.lang.String) ref);
        resourcePath_ = b;
        return b;
      } else {
        return (com.google.protobuf.ByteString) ref;
      }
    }
    /**
     * <pre>
     * Path of resource inside a coordination node.
     * </pre>
     *
     * <code>string resource_path = 3;</code>
     * @param value The resourcePath to set.
     * @return This builder for chaining.
     */
    public Builder setResourcePath(
        java.lang.String value) {
      if (value == null) {
    throw new NullPointerException();
  }
  
      resourcePath_ = value;
      onChanged();
      return this;
    }
    /**
     * <pre>
     * Path of resource inside a coordination node.
     * </pre>
     *
     * <code>string resource_path = 3;</code>
     * @return This builder for chaining.
     */
    public Builder clearResourcePath() {
      
      resourcePath_ = getDefaultInstance().getResourcePath();
      onChanged();
      return this;
    }
    /**
     * <pre>
     * Path of resource inside a coordination node.
     * </pre>
     *
     * <code>string resource_path = 3;</code>
     * @param value The bytes for resourcePath to set.
     * @return This builder for chaining.
     */
    public Builder setResourcePathBytes(
        com.google.protobuf.ByteString value) {
      if (value == null) {
    throw new NullPointerException();
  }
  checkByteStringIsUtf8(value);
      
      resourcePath_ = value;
      onChanged();
      return this;
    }

    /**
     * <pre>
     * Request resource's units for usage.
     * </pre>
     *
     * <code>uint64 required = 4;</code>
     * @return Whether the required field is set.
     */
    public boolean hasRequired() {
      return unitsCase_ == 4;
    }
    /**
     * <pre>
     * Request resource's units for usage.
     * </pre>
     *
     * <code>uint64 required = 4;</code>
     * @return The required.
     */
    public long getRequired() {
      if (unitsCase_ == 4) {
        return (java.lang.Long) units_;
      }
      return 0L;
    }
    /**
     * <pre>
     * Request resource's units for usage.
     * </pre>
     *
     * <code>uint64 required = 4;</code>
     * @param value The required to set.
     * @return This builder for chaining.
     */
    public Builder setRequired(long value) {
      unitsCase_ = 4;
      units_ = value;
      onChanged();
      return this;
    }
    /**
     * <pre>
     * Request resource's units for usage.
     * </pre>
     *
     * <code>uint64 required = 4;</code>
     * @return This builder for chaining.
     */
    public Builder clearRequired() {
      if (unitsCase_ == 4) {
        unitsCase_ = 0;
        units_ = null;
        onChanged();
      }
      return this;
    }

    /**
     * <pre>
     * Actually used resource's units by client.
     * </pre>
     *
     * <code>uint64 used = 5;</code>
     * @return Whether the used field is set.
     */
    public boolean hasUsed() {
      return unitsCase_ == 5;
    }
    /**
     * <pre>
     * Actually used resource's units by client.
     * </pre>
     *
     * <code>uint64 used = 5;</code>
     * @return The used.
     */
    public long getUsed() {
      if (unitsCase_ == 5) {
        return (java.lang.Long) units_;
      }
      return 0L;
    }
    /**
     * <pre>
     * Actually used resource's units by client.
     * </pre>
     *
     * <code>uint64 used = 5;</code>
     * @param value The used to set.
     * @return This builder for chaining.
     */
    public Builder setUsed(long value) {
      unitsCase_ = 5;
      units_ = value;
      onChanged();
      return this;
    }
    /**
     * <pre>
     * Actually used resource's units by client.
     * </pre>
     *
     * <code>uint64 used = 5;</code>
     * @return This builder for chaining.
     */
    public Builder clearUsed() {
      if (unitsCase_ == 5) {
        unitsCase_ = 0;
        units_ = null;
        onChanged();
      }
      return this;
    }
    @java.lang.Override
    public final Builder setUnknownFields(
        final com.google.protobuf.UnknownFieldSet unknownFields) {
      return super.setUnknownFields(unknownFields);
    }

    @java.lang.Override
    public final Builder mergeUnknownFields(
        final com.google.protobuf.UnknownFieldSet unknownFields) {
      return super.mergeUnknownFields(unknownFields);
    }


    // @@protoc_insertion_point(builder_scope:Ydb.RateLimiter.AcquireResourceRequest)
  }

  // @@protoc_insertion_point(class_scope:Ydb.RateLimiter.AcquireResourceRequest)
  private static final com.yandex.ydb.rate_limiter.AcquireResourceRequest DEFAULT_INSTANCE;
  static {
    DEFAULT_INSTANCE = new com.yandex.ydb.rate_limiter.AcquireResourceRequest();
  }

  public static com.yandex.ydb.rate_limiter.AcquireResourceRequest getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static final com.google.protobuf.Parser<AcquireResourceRequest>
      PARSER = new com.google.protobuf.AbstractParser<AcquireResourceRequest>() {
    @java.lang.Override
    public AcquireResourceRequest parsePartialFrom(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return new AcquireResourceRequest(input, extensionRegistry);
    }
  };

  public static com.google.protobuf.Parser<AcquireResourceRequest> parser() {
    return PARSER;
  }

  @java.lang.Override
  public com.google.protobuf.Parser<AcquireResourceRequest> getParserForType() {
    return PARSER;
  }

  @java.lang.Override
  public com.yandex.ydb.rate_limiter.AcquireResourceRequest getDefaultInstanceForType() {
    return DEFAULT_INSTANCE;
  }

}

