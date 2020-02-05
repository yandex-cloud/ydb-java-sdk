package com.yandex.ydb.clickhouse.v1;

import static io.grpc.MethodDescriptor.generateFullMethodName;
import static io.grpc.stub.ClientCalls.asyncBidiStreamingCall;
import static io.grpc.stub.ClientCalls.asyncClientStreamingCall;
import static io.grpc.stub.ClientCalls.asyncServerStreamingCall;
import static io.grpc.stub.ClientCalls.asyncUnaryCall;
import static io.grpc.stub.ClientCalls.blockingServerStreamingCall;
import static io.grpc.stub.ClientCalls.blockingUnaryCall;
import static io.grpc.stub.ClientCalls.futureUnaryCall;
import static io.grpc.stub.ServerCalls.asyncBidiStreamingCall;
import static io.grpc.stub.ServerCalls.asyncClientStreamingCall;
import static io.grpc.stub.ServerCalls.asyncServerStreamingCall;
import static io.grpc.stub.ServerCalls.asyncUnaryCall;
import static io.grpc.stub.ServerCalls.asyncUnimplementedStreamingCall;
import static io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall;

/**
 */
@javax.annotation.Generated(
    value = "by gRPC proto compiler",
    comments = "Source: kikimr/public/api/grpc/draft/ydb_clickhouse_internal_v1.proto")
public final class ClickhouseInternalServiceGrpc {

  private ClickhouseInternalServiceGrpc() {}

  public static final String SERVICE_NAME = "Ydb.ClickhouseInternal.V1.ClickhouseInternalService";

  // Static method descriptors that strictly reflect the proto.
  private static volatile io.grpc.MethodDescriptor<com.yandex.ydb.clickhouse.ClickhouseInternalProtos.ScanRequest,
      com.yandex.ydb.clickhouse.ClickhouseInternalProtos.ScanResponse> getScanMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "Scan",
      requestType = com.yandex.ydb.clickhouse.ClickhouseInternalProtos.ScanRequest.class,
      responseType = com.yandex.ydb.clickhouse.ClickhouseInternalProtos.ScanResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<com.yandex.ydb.clickhouse.ClickhouseInternalProtos.ScanRequest,
      com.yandex.ydb.clickhouse.ClickhouseInternalProtos.ScanResponse> getScanMethod() {
    io.grpc.MethodDescriptor<com.yandex.ydb.clickhouse.ClickhouseInternalProtos.ScanRequest, com.yandex.ydb.clickhouse.ClickhouseInternalProtos.ScanResponse> getScanMethod;
    if ((getScanMethod = ClickhouseInternalServiceGrpc.getScanMethod) == null) {
      synchronized (ClickhouseInternalServiceGrpc.class) {
        if ((getScanMethod = ClickhouseInternalServiceGrpc.getScanMethod) == null) {
          ClickhouseInternalServiceGrpc.getScanMethod = getScanMethod =
              io.grpc.MethodDescriptor.<com.yandex.ydb.clickhouse.ClickhouseInternalProtos.ScanRequest, com.yandex.ydb.clickhouse.ClickhouseInternalProtos.ScanResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "Scan"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.yandex.ydb.clickhouse.ClickhouseInternalProtos.ScanRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.yandex.ydb.clickhouse.ClickhouseInternalProtos.ScanResponse.getDefaultInstance()))
              .setSchemaDescriptor(new ClickhouseInternalServiceMethodDescriptorSupplier("Scan"))
              .build();
        }
      }
    }
    return getScanMethod;
  }

  private static volatile io.grpc.MethodDescriptor<com.yandex.ydb.clickhouse.ClickhouseInternalProtos.GetShardLocationsRequest,
      com.yandex.ydb.clickhouse.ClickhouseInternalProtos.GetShardLocationsResponse> getGetShardLocationsMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "GetShardLocations",
      requestType = com.yandex.ydb.clickhouse.ClickhouseInternalProtos.GetShardLocationsRequest.class,
      responseType = com.yandex.ydb.clickhouse.ClickhouseInternalProtos.GetShardLocationsResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<com.yandex.ydb.clickhouse.ClickhouseInternalProtos.GetShardLocationsRequest,
      com.yandex.ydb.clickhouse.ClickhouseInternalProtos.GetShardLocationsResponse> getGetShardLocationsMethod() {
    io.grpc.MethodDescriptor<com.yandex.ydb.clickhouse.ClickhouseInternalProtos.GetShardLocationsRequest, com.yandex.ydb.clickhouse.ClickhouseInternalProtos.GetShardLocationsResponse> getGetShardLocationsMethod;
    if ((getGetShardLocationsMethod = ClickhouseInternalServiceGrpc.getGetShardLocationsMethod) == null) {
      synchronized (ClickhouseInternalServiceGrpc.class) {
        if ((getGetShardLocationsMethod = ClickhouseInternalServiceGrpc.getGetShardLocationsMethod) == null) {
          ClickhouseInternalServiceGrpc.getGetShardLocationsMethod = getGetShardLocationsMethod =
              io.grpc.MethodDescriptor.<com.yandex.ydb.clickhouse.ClickhouseInternalProtos.GetShardLocationsRequest, com.yandex.ydb.clickhouse.ClickhouseInternalProtos.GetShardLocationsResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "GetShardLocations"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.yandex.ydb.clickhouse.ClickhouseInternalProtos.GetShardLocationsRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.yandex.ydb.clickhouse.ClickhouseInternalProtos.GetShardLocationsResponse.getDefaultInstance()))
              .setSchemaDescriptor(new ClickhouseInternalServiceMethodDescriptorSupplier("GetShardLocations"))
              .build();
        }
      }
    }
    return getGetShardLocationsMethod;
  }

  private static volatile io.grpc.MethodDescriptor<com.yandex.ydb.clickhouse.ClickhouseInternalProtos.DescribeTableRequest,
      com.yandex.ydb.clickhouse.ClickhouseInternalProtos.DescribeTableResponse> getDescribeTableMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "DescribeTable",
      requestType = com.yandex.ydb.clickhouse.ClickhouseInternalProtos.DescribeTableRequest.class,
      responseType = com.yandex.ydb.clickhouse.ClickhouseInternalProtos.DescribeTableResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<com.yandex.ydb.clickhouse.ClickhouseInternalProtos.DescribeTableRequest,
      com.yandex.ydb.clickhouse.ClickhouseInternalProtos.DescribeTableResponse> getDescribeTableMethod() {
    io.grpc.MethodDescriptor<com.yandex.ydb.clickhouse.ClickhouseInternalProtos.DescribeTableRequest, com.yandex.ydb.clickhouse.ClickhouseInternalProtos.DescribeTableResponse> getDescribeTableMethod;
    if ((getDescribeTableMethod = ClickhouseInternalServiceGrpc.getDescribeTableMethod) == null) {
      synchronized (ClickhouseInternalServiceGrpc.class) {
        if ((getDescribeTableMethod = ClickhouseInternalServiceGrpc.getDescribeTableMethod) == null) {
          ClickhouseInternalServiceGrpc.getDescribeTableMethod = getDescribeTableMethod =
              io.grpc.MethodDescriptor.<com.yandex.ydb.clickhouse.ClickhouseInternalProtos.DescribeTableRequest, com.yandex.ydb.clickhouse.ClickhouseInternalProtos.DescribeTableResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "DescribeTable"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.yandex.ydb.clickhouse.ClickhouseInternalProtos.DescribeTableRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.yandex.ydb.clickhouse.ClickhouseInternalProtos.DescribeTableResponse.getDefaultInstance()))
              .setSchemaDescriptor(new ClickhouseInternalServiceMethodDescriptorSupplier("DescribeTable"))
              .build();
        }
      }
    }
    return getDescribeTableMethod;
  }

  private static volatile io.grpc.MethodDescriptor<com.yandex.ydb.clickhouse.ClickhouseInternalProtos.CreateSnapshotRequest,
      com.yandex.ydb.clickhouse.ClickhouseInternalProtos.CreateSnapshotResponse> getCreateSnapshotMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "CreateSnapshot",
      requestType = com.yandex.ydb.clickhouse.ClickhouseInternalProtos.CreateSnapshotRequest.class,
      responseType = com.yandex.ydb.clickhouse.ClickhouseInternalProtos.CreateSnapshotResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<com.yandex.ydb.clickhouse.ClickhouseInternalProtos.CreateSnapshotRequest,
      com.yandex.ydb.clickhouse.ClickhouseInternalProtos.CreateSnapshotResponse> getCreateSnapshotMethod() {
    io.grpc.MethodDescriptor<com.yandex.ydb.clickhouse.ClickhouseInternalProtos.CreateSnapshotRequest, com.yandex.ydb.clickhouse.ClickhouseInternalProtos.CreateSnapshotResponse> getCreateSnapshotMethod;
    if ((getCreateSnapshotMethod = ClickhouseInternalServiceGrpc.getCreateSnapshotMethod) == null) {
      synchronized (ClickhouseInternalServiceGrpc.class) {
        if ((getCreateSnapshotMethod = ClickhouseInternalServiceGrpc.getCreateSnapshotMethod) == null) {
          ClickhouseInternalServiceGrpc.getCreateSnapshotMethod = getCreateSnapshotMethod =
              io.grpc.MethodDescriptor.<com.yandex.ydb.clickhouse.ClickhouseInternalProtos.CreateSnapshotRequest, com.yandex.ydb.clickhouse.ClickhouseInternalProtos.CreateSnapshotResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "CreateSnapshot"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.yandex.ydb.clickhouse.ClickhouseInternalProtos.CreateSnapshotRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.yandex.ydb.clickhouse.ClickhouseInternalProtos.CreateSnapshotResponse.getDefaultInstance()))
              .setSchemaDescriptor(new ClickhouseInternalServiceMethodDescriptorSupplier("CreateSnapshot"))
              .build();
        }
      }
    }
    return getCreateSnapshotMethod;
  }

  private static volatile io.grpc.MethodDescriptor<com.yandex.ydb.clickhouse.ClickhouseInternalProtos.RefreshSnapshotRequest,
      com.yandex.ydb.clickhouse.ClickhouseInternalProtos.RefreshSnapshotResponse> getRefreshSnapshotMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "RefreshSnapshot",
      requestType = com.yandex.ydb.clickhouse.ClickhouseInternalProtos.RefreshSnapshotRequest.class,
      responseType = com.yandex.ydb.clickhouse.ClickhouseInternalProtos.RefreshSnapshotResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<com.yandex.ydb.clickhouse.ClickhouseInternalProtos.RefreshSnapshotRequest,
      com.yandex.ydb.clickhouse.ClickhouseInternalProtos.RefreshSnapshotResponse> getRefreshSnapshotMethod() {
    io.grpc.MethodDescriptor<com.yandex.ydb.clickhouse.ClickhouseInternalProtos.RefreshSnapshotRequest, com.yandex.ydb.clickhouse.ClickhouseInternalProtos.RefreshSnapshotResponse> getRefreshSnapshotMethod;
    if ((getRefreshSnapshotMethod = ClickhouseInternalServiceGrpc.getRefreshSnapshotMethod) == null) {
      synchronized (ClickhouseInternalServiceGrpc.class) {
        if ((getRefreshSnapshotMethod = ClickhouseInternalServiceGrpc.getRefreshSnapshotMethod) == null) {
          ClickhouseInternalServiceGrpc.getRefreshSnapshotMethod = getRefreshSnapshotMethod =
              io.grpc.MethodDescriptor.<com.yandex.ydb.clickhouse.ClickhouseInternalProtos.RefreshSnapshotRequest, com.yandex.ydb.clickhouse.ClickhouseInternalProtos.RefreshSnapshotResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "RefreshSnapshot"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.yandex.ydb.clickhouse.ClickhouseInternalProtos.RefreshSnapshotRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.yandex.ydb.clickhouse.ClickhouseInternalProtos.RefreshSnapshotResponse.getDefaultInstance()))
              .setSchemaDescriptor(new ClickhouseInternalServiceMethodDescriptorSupplier("RefreshSnapshot"))
              .build();
        }
      }
    }
    return getRefreshSnapshotMethod;
  }

  private static volatile io.grpc.MethodDescriptor<com.yandex.ydb.clickhouse.ClickhouseInternalProtos.DiscardSnapshotRequest,
      com.yandex.ydb.clickhouse.ClickhouseInternalProtos.DiscardSnapshotResponse> getDiscardSnapshotMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "DiscardSnapshot",
      requestType = com.yandex.ydb.clickhouse.ClickhouseInternalProtos.DiscardSnapshotRequest.class,
      responseType = com.yandex.ydb.clickhouse.ClickhouseInternalProtos.DiscardSnapshotResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<com.yandex.ydb.clickhouse.ClickhouseInternalProtos.DiscardSnapshotRequest,
      com.yandex.ydb.clickhouse.ClickhouseInternalProtos.DiscardSnapshotResponse> getDiscardSnapshotMethod() {
    io.grpc.MethodDescriptor<com.yandex.ydb.clickhouse.ClickhouseInternalProtos.DiscardSnapshotRequest, com.yandex.ydb.clickhouse.ClickhouseInternalProtos.DiscardSnapshotResponse> getDiscardSnapshotMethod;
    if ((getDiscardSnapshotMethod = ClickhouseInternalServiceGrpc.getDiscardSnapshotMethod) == null) {
      synchronized (ClickhouseInternalServiceGrpc.class) {
        if ((getDiscardSnapshotMethod = ClickhouseInternalServiceGrpc.getDiscardSnapshotMethod) == null) {
          ClickhouseInternalServiceGrpc.getDiscardSnapshotMethod = getDiscardSnapshotMethod =
              io.grpc.MethodDescriptor.<com.yandex.ydb.clickhouse.ClickhouseInternalProtos.DiscardSnapshotRequest, com.yandex.ydb.clickhouse.ClickhouseInternalProtos.DiscardSnapshotResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "DiscardSnapshot"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.yandex.ydb.clickhouse.ClickhouseInternalProtos.DiscardSnapshotRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.yandex.ydb.clickhouse.ClickhouseInternalProtos.DiscardSnapshotResponse.getDefaultInstance()))
              .setSchemaDescriptor(new ClickhouseInternalServiceMethodDescriptorSupplier("DiscardSnapshot"))
              .build();
        }
      }
    }
    return getDiscardSnapshotMethod;
  }

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static ClickhouseInternalServiceStub newStub(io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<ClickhouseInternalServiceStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<ClickhouseInternalServiceStub>() {
        @java.lang.Override
        public ClickhouseInternalServiceStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new ClickhouseInternalServiceStub(channel, callOptions);
        }
      };
    return ClickhouseInternalServiceStub.newStub(factory, channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static ClickhouseInternalServiceBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<ClickhouseInternalServiceBlockingStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<ClickhouseInternalServiceBlockingStub>() {
        @java.lang.Override
        public ClickhouseInternalServiceBlockingStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new ClickhouseInternalServiceBlockingStub(channel, callOptions);
        }
      };
    return ClickhouseInternalServiceBlockingStub.newStub(factory, channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static ClickhouseInternalServiceFutureStub newFutureStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<ClickhouseInternalServiceFutureStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<ClickhouseInternalServiceFutureStub>() {
        @java.lang.Override
        public ClickhouseInternalServiceFutureStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new ClickhouseInternalServiceFutureStub(channel, callOptions);
        }
      };
    return ClickhouseInternalServiceFutureStub.newStub(factory, channel);
  }

  /**
   */
  public static abstract class ClickhouseInternalServiceImplBase implements io.grpc.BindableService {

    /**
     */
    public void scan(com.yandex.ydb.clickhouse.ClickhouseInternalProtos.ScanRequest request,
        io.grpc.stub.StreamObserver<com.yandex.ydb.clickhouse.ClickhouseInternalProtos.ScanResponse> responseObserver) {
      asyncUnimplementedUnaryCall(getScanMethod(), responseObserver);
    }

    /**
     */
    public void getShardLocations(com.yandex.ydb.clickhouse.ClickhouseInternalProtos.GetShardLocationsRequest request,
        io.grpc.stub.StreamObserver<com.yandex.ydb.clickhouse.ClickhouseInternalProtos.GetShardLocationsResponse> responseObserver) {
      asyncUnimplementedUnaryCall(getGetShardLocationsMethod(), responseObserver);
    }

    /**
     */
    public void describeTable(com.yandex.ydb.clickhouse.ClickhouseInternalProtos.DescribeTableRequest request,
        io.grpc.stub.StreamObserver<com.yandex.ydb.clickhouse.ClickhouseInternalProtos.DescribeTableResponse> responseObserver) {
      asyncUnimplementedUnaryCall(getDescribeTableMethod(), responseObserver);
    }

    /**
     * <pre>
     **
     * CreateSnapshot creates a temporary consistent snapshot of one or more
     * tables, which may later be used in requests. Created snapshot will have
     * an opaque id and a server defined timeout, after which it may become
     * expired. For prolonged use it must be refreshed before it expires.
     * </pre>
     */
    public void createSnapshot(com.yandex.ydb.clickhouse.ClickhouseInternalProtos.CreateSnapshotRequest request,
        io.grpc.stub.StreamObserver<com.yandex.ydb.clickhouse.ClickhouseInternalProtos.CreateSnapshotResponse> responseObserver) {
      asyncUnimplementedUnaryCall(getCreateSnapshotMethod(), responseObserver);
    }

    /**
     * <pre>
     **
     * RefreshSnapshot will attempt to refresh a previously created snapshot,
     * extending expiration time in specified tables.
     * </pre>
     */
    public void refreshSnapshot(com.yandex.ydb.clickhouse.ClickhouseInternalProtos.RefreshSnapshotRequest request,
        io.grpc.stub.StreamObserver<com.yandex.ydb.clickhouse.ClickhouseInternalProtos.RefreshSnapshotResponse> responseObserver) {
      asyncUnimplementedUnaryCall(getRefreshSnapshotMethod(), responseObserver);
    }

    /**
     * <pre>
     **
     * DiscardSnapshot will attempt to discard a previously created snapshot,
     * so resources may be freed earlier than its expiration time.
     * </pre>
     */
    public void discardSnapshot(com.yandex.ydb.clickhouse.ClickhouseInternalProtos.DiscardSnapshotRequest request,
        io.grpc.stub.StreamObserver<com.yandex.ydb.clickhouse.ClickhouseInternalProtos.DiscardSnapshotResponse> responseObserver) {
      asyncUnimplementedUnaryCall(getDiscardSnapshotMethod(), responseObserver);
    }

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
          .addMethod(
            getScanMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                com.yandex.ydb.clickhouse.ClickhouseInternalProtos.ScanRequest,
                com.yandex.ydb.clickhouse.ClickhouseInternalProtos.ScanResponse>(
                  this, METHODID_SCAN)))
          .addMethod(
            getGetShardLocationsMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                com.yandex.ydb.clickhouse.ClickhouseInternalProtos.GetShardLocationsRequest,
                com.yandex.ydb.clickhouse.ClickhouseInternalProtos.GetShardLocationsResponse>(
                  this, METHODID_GET_SHARD_LOCATIONS)))
          .addMethod(
            getDescribeTableMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                com.yandex.ydb.clickhouse.ClickhouseInternalProtos.DescribeTableRequest,
                com.yandex.ydb.clickhouse.ClickhouseInternalProtos.DescribeTableResponse>(
                  this, METHODID_DESCRIBE_TABLE)))
          .addMethod(
            getCreateSnapshotMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                com.yandex.ydb.clickhouse.ClickhouseInternalProtos.CreateSnapshotRequest,
                com.yandex.ydb.clickhouse.ClickhouseInternalProtos.CreateSnapshotResponse>(
                  this, METHODID_CREATE_SNAPSHOT)))
          .addMethod(
            getRefreshSnapshotMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                com.yandex.ydb.clickhouse.ClickhouseInternalProtos.RefreshSnapshotRequest,
                com.yandex.ydb.clickhouse.ClickhouseInternalProtos.RefreshSnapshotResponse>(
                  this, METHODID_REFRESH_SNAPSHOT)))
          .addMethod(
            getDiscardSnapshotMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                com.yandex.ydb.clickhouse.ClickhouseInternalProtos.DiscardSnapshotRequest,
                com.yandex.ydb.clickhouse.ClickhouseInternalProtos.DiscardSnapshotResponse>(
                  this, METHODID_DISCARD_SNAPSHOT)))
          .build();
    }
  }

  /**
   */
  public static final class ClickhouseInternalServiceStub extends io.grpc.stub.AbstractAsyncStub<ClickhouseInternalServiceStub> {
    private ClickhouseInternalServiceStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected ClickhouseInternalServiceStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new ClickhouseInternalServiceStub(channel, callOptions);
    }

    /**
     */
    public void scan(com.yandex.ydb.clickhouse.ClickhouseInternalProtos.ScanRequest request,
        io.grpc.stub.StreamObserver<com.yandex.ydb.clickhouse.ClickhouseInternalProtos.ScanResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getScanMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void getShardLocations(com.yandex.ydb.clickhouse.ClickhouseInternalProtos.GetShardLocationsRequest request,
        io.grpc.stub.StreamObserver<com.yandex.ydb.clickhouse.ClickhouseInternalProtos.GetShardLocationsResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getGetShardLocationsMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void describeTable(com.yandex.ydb.clickhouse.ClickhouseInternalProtos.DescribeTableRequest request,
        io.grpc.stub.StreamObserver<com.yandex.ydb.clickhouse.ClickhouseInternalProtos.DescribeTableResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getDescribeTableMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     **
     * CreateSnapshot creates a temporary consistent snapshot of one or more
     * tables, which may later be used in requests. Created snapshot will have
     * an opaque id and a server defined timeout, after which it may become
     * expired. For prolonged use it must be refreshed before it expires.
     * </pre>
     */
    public void createSnapshot(com.yandex.ydb.clickhouse.ClickhouseInternalProtos.CreateSnapshotRequest request,
        io.grpc.stub.StreamObserver<com.yandex.ydb.clickhouse.ClickhouseInternalProtos.CreateSnapshotResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getCreateSnapshotMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     **
     * RefreshSnapshot will attempt to refresh a previously created snapshot,
     * extending expiration time in specified tables.
     * </pre>
     */
    public void refreshSnapshot(com.yandex.ydb.clickhouse.ClickhouseInternalProtos.RefreshSnapshotRequest request,
        io.grpc.stub.StreamObserver<com.yandex.ydb.clickhouse.ClickhouseInternalProtos.RefreshSnapshotResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getRefreshSnapshotMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     **
     * DiscardSnapshot will attempt to discard a previously created snapshot,
     * so resources may be freed earlier than its expiration time.
     * </pre>
     */
    public void discardSnapshot(com.yandex.ydb.clickhouse.ClickhouseInternalProtos.DiscardSnapshotRequest request,
        io.grpc.stub.StreamObserver<com.yandex.ydb.clickhouse.ClickhouseInternalProtos.DiscardSnapshotResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getDiscardSnapshotMethod(), getCallOptions()), request, responseObserver);
    }
  }

  /**
   */
  public static final class ClickhouseInternalServiceBlockingStub extends io.grpc.stub.AbstractBlockingStub<ClickhouseInternalServiceBlockingStub> {
    private ClickhouseInternalServiceBlockingStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected ClickhouseInternalServiceBlockingStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new ClickhouseInternalServiceBlockingStub(channel, callOptions);
    }

    /**
     */
    public com.yandex.ydb.clickhouse.ClickhouseInternalProtos.ScanResponse scan(com.yandex.ydb.clickhouse.ClickhouseInternalProtos.ScanRequest request) {
      return blockingUnaryCall(
          getChannel(), getScanMethod(), getCallOptions(), request);
    }

    /**
     */
    public com.yandex.ydb.clickhouse.ClickhouseInternalProtos.GetShardLocationsResponse getShardLocations(com.yandex.ydb.clickhouse.ClickhouseInternalProtos.GetShardLocationsRequest request) {
      return blockingUnaryCall(
          getChannel(), getGetShardLocationsMethod(), getCallOptions(), request);
    }

    /**
     */
    public com.yandex.ydb.clickhouse.ClickhouseInternalProtos.DescribeTableResponse describeTable(com.yandex.ydb.clickhouse.ClickhouseInternalProtos.DescribeTableRequest request) {
      return blockingUnaryCall(
          getChannel(), getDescribeTableMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     **
     * CreateSnapshot creates a temporary consistent snapshot of one or more
     * tables, which may later be used in requests. Created snapshot will have
     * an opaque id and a server defined timeout, after which it may become
     * expired. For prolonged use it must be refreshed before it expires.
     * </pre>
     */
    public com.yandex.ydb.clickhouse.ClickhouseInternalProtos.CreateSnapshotResponse createSnapshot(com.yandex.ydb.clickhouse.ClickhouseInternalProtos.CreateSnapshotRequest request) {
      return blockingUnaryCall(
          getChannel(), getCreateSnapshotMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     **
     * RefreshSnapshot will attempt to refresh a previously created snapshot,
     * extending expiration time in specified tables.
     * </pre>
     */
    public com.yandex.ydb.clickhouse.ClickhouseInternalProtos.RefreshSnapshotResponse refreshSnapshot(com.yandex.ydb.clickhouse.ClickhouseInternalProtos.RefreshSnapshotRequest request) {
      return blockingUnaryCall(
          getChannel(), getRefreshSnapshotMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     **
     * DiscardSnapshot will attempt to discard a previously created snapshot,
     * so resources may be freed earlier than its expiration time.
     * </pre>
     */
    public com.yandex.ydb.clickhouse.ClickhouseInternalProtos.DiscardSnapshotResponse discardSnapshot(com.yandex.ydb.clickhouse.ClickhouseInternalProtos.DiscardSnapshotRequest request) {
      return blockingUnaryCall(
          getChannel(), getDiscardSnapshotMethod(), getCallOptions(), request);
    }
  }

  /**
   */
  public static final class ClickhouseInternalServiceFutureStub extends io.grpc.stub.AbstractFutureStub<ClickhouseInternalServiceFutureStub> {
    private ClickhouseInternalServiceFutureStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected ClickhouseInternalServiceFutureStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new ClickhouseInternalServiceFutureStub(channel, callOptions);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<com.yandex.ydb.clickhouse.ClickhouseInternalProtos.ScanResponse> scan(
        com.yandex.ydb.clickhouse.ClickhouseInternalProtos.ScanRequest request) {
      return futureUnaryCall(
          getChannel().newCall(getScanMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<com.yandex.ydb.clickhouse.ClickhouseInternalProtos.GetShardLocationsResponse> getShardLocations(
        com.yandex.ydb.clickhouse.ClickhouseInternalProtos.GetShardLocationsRequest request) {
      return futureUnaryCall(
          getChannel().newCall(getGetShardLocationsMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<com.yandex.ydb.clickhouse.ClickhouseInternalProtos.DescribeTableResponse> describeTable(
        com.yandex.ydb.clickhouse.ClickhouseInternalProtos.DescribeTableRequest request) {
      return futureUnaryCall(
          getChannel().newCall(getDescribeTableMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     **
     * CreateSnapshot creates a temporary consistent snapshot of one or more
     * tables, which may later be used in requests. Created snapshot will have
     * an opaque id and a server defined timeout, after which it may become
     * expired. For prolonged use it must be refreshed before it expires.
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<com.yandex.ydb.clickhouse.ClickhouseInternalProtos.CreateSnapshotResponse> createSnapshot(
        com.yandex.ydb.clickhouse.ClickhouseInternalProtos.CreateSnapshotRequest request) {
      return futureUnaryCall(
          getChannel().newCall(getCreateSnapshotMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     **
     * RefreshSnapshot will attempt to refresh a previously created snapshot,
     * extending expiration time in specified tables.
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<com.yandex.ydb.clickhouse.ClickhouseInternalProtos.RefreshSnapshotResponse> refreshSnapshot(
        com.yandex.ydb.clickhouse.ClickhouseInternalProtos.RefreshSnapshotRequest request) {
      return futureUnaryCall(
          getChannel().newCall(getRefreshSnapshotMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     **
     * DiscardSnapshot will attempt to discard a previously created snapshot,
     * so resources may be freed earlier than its expiration time.
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<com.yandex.ydb.clickhouse.ClickhouseInternalProtos.DiscardSnapshotResponse> discardSnapshot(
        com.yandex.ydb.clickhouse.ClickhouseInternalProtos.DiscardSnapshotRequest request) {
      return futureUnaryCall(
          getChannel().newCall(getDiscardSnapshotMethod(), getCallOptions()), request);
    }
  }

  private static final int METHODID_SCAN = 0;
  private static final int METHODID_GET_SHARD_LOCATIONS = 1;
  private static final int METHODID_DESCRIBE_TABLE = 2;
  private static final int METHODID_CREATE_SNAPSHOT = 3;
  private static final int METHODID_REFRESH_SNAPSHOT = 4;
  private static final int METHODID_DISCARD_SNAPSHOT = 5;

  private static final class MethodHandlers<Req, Resp> implements
      io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
    private final ClickhouseInternalServiceImplBase serviceImpl;
    private final int methodId;

    MethodHandlers(ClickhouseInternalServiceImplBase serviceImpl, int methodId) {
      this.serviceImpl = serviceImpl;
      this.methodId = methodId;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_SCAN:
          serviceImpl.scan((com.yandex.ydb.clickhouse.ClickhouseInternalProtos.ScanRequest) request,
              (io.grpc.stub.StreamObserver<com.yandex.ydb.clickhouse.ClickhouseInternalProtos.ScanResponse>) responseObserver);
          break;
        case METHODID_GET_SHARD_LOCATIONS:
          serviceImpl.getShardLocations((com.yandex.ydb.clickhouse.ClickhouseInternalProtos.GetShardLocationsRequest) request,
              (io.grpc.stub.StreamObserver<com.yandex.ydb.clickhouse.ClickhouseInternalProtos.GetShardLocationsResponse>) responseObserver);
          break;
        case METHODID_DESCRIBE_TABLE:
          serviceImpl.describeTable((com.yandex.ydb.clickhouse.ClickhouseInternalProtos.DescribeTableRequest) request,
              (io.grpc.stub.StreamObserver<com.yandex.ydb.clickhouse.ClickhouseInternalProtos.DescribeTableResponse>) responseObserver);
          break;
        case METHODID_CREATE_SNAPSHOT:
          serviceImpl.createSnapshot((com.yandex.ydb.clickhouse.ClickhouseInternalProtos.CreateSnapshotRequest) request,
              (io.grpc.stub.StreamObserver<com.yandex.ydb.clickhouse.ClickhouseInternalProtos.CreateSnapshotResponse>) responseObserver);
          break;
        case METHODID_REFRESH_SNAPSHOT:
          serviceImpl.refreshSnapshot((com.yandex.ydb.clickhouse.ClickhouseInternalProtos.RefreshSnapshotRequest) request,
              (io.grpc.stub.StreamObserver<com.yandex.ydb.clickhouse.ClickhouseInternalProtos.RefreshSnapshotResponse>) responseObserver);
          break;
        case METHODID_DISCARD_SNAPSHOT:
          serviceImpl.discardSnapshot((com.yandex.ydb.clickhouse.ClickhouseInternalProtos.DiscardSnapshotRequest) request,
              (io.grpc.stub.StreamObserver<com.yandex.ydb.clickhouse.ClickhouseInternalProtos.DiscardSnapshotResponse>) responseObserver);
          break;
        default:
          throw new AssertionError();
      }
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public io.grpc.stub.StreamObserver<Req> invoke(
        io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        default:
          throw new AssertionError();
      }
    }
  }

  private static abstract class ClickhouseInternalServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
    ClickhouseInternalServiceBaseDescriptorSupplier() {}

    @java.lang.Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return com.yandex.ydb.clickhouse.v1.YdbClickhouseInternalV1.getDescriptor();
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
      return getFileDescriptor().findServiceByName("ClickhouseInternalService");
    }
  }

  private static final class ClickhouseInternalServiceFileDescriptorSupplier
      extends ClickhouseInternalServiceBaseDescriptorSupplier {
    ClickhouseInternalServiceFileDescriptorSupplier() {}
  }

  private static final class ClickhouseInternalServiceMethodDescriptorSupplier
      extends ClickhouseInternalServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
    private final String methodName;

    ClickhouseInternalServiceMethodDescriptorSupplier(String methodName) {
      this.methodName = methodName;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.MethodDescriptor getMethodDescriptor() {
      return getServiceDescriptor().findMethodByName(methodName);
    }
  }

  private static volatile io.grpc.ServiceDescriptor serviceDescriptor;

  public static io.grpc.ServiceDescriptor getServiceDescriptor() {
    io.grpc.ServiceDescriptor result = serviceDescriptor;
    if (result == null) {
      synchronized (ClickhouseInternalServiceGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .setSchemaDescriptor(new ClickhouseInternalServiceFileDescriptorSupplier())
              .addMethod(getScanMethod())
              .addMethod(getGetShardLocationsMethod())
              .addMethod(getDescribeTableMethod())
              .addMethod(getCreateSnapshotMethod())
              .addMethod(getRefreshSnapshotMethod())
              .addMethod(getDiscardSnapshotMethod())
              .build();
        }
      }
    }
    return result;
  }
}
