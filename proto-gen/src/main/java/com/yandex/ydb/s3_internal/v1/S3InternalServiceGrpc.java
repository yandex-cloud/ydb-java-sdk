package com.yandex.ydb.s3_internal.v1;

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
    comments = "Source: kikimr/public/api/grpc/draft/ydb_s3_internal_v1.proto")
public final class S3InternalServiceGrpc {

  private S3InternalServiceGrpc() {}

  public static final String SERVICE_NAME = "Ydb.S3Internal.V1.S3InternalService";

  // Static method descriptors that strictly reflect the proto.
  private static volatile io.grpc.MethodDescriptor<com.yandex.ydb.s3_internal.S3InternalProtos.S3ListingRequest,
      com.yandex.ydb.s3_internal.S3InternalProtos.S3ListingResponse> getS3ListingMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "S3Listing",
      requestType = com.yandex.ydb.s3_internal.S3InternalProtos.S3ListingRequest.class,
      responseType = com.yandex.ydb.s3_internal.S3InternalProtos.S3ListingResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<com.yandex.ydb.s3_internal.S3InternalProtos.S3ListingRequest,
      com.yandex.ydb.s3_internal.S3InternalProtos.S3ListingResponse> getS3ListingMethod() {
    io.grpc.MethodDescriptor<com.yandex.ydb.s3_internal.S3InternalProtos.S3ListingRequest, com.yandex.ydb.s3_internal.S3InternalProtos.S3ListingResponse> getS3ListingMethod;
    if ((getS3ListingMethod = S3InternalServiceGrpc.getS3ListingMethod) == null) {
      synchronized (S3InternalServiceGrpc.class) {
        if ((getS3ListingMethod = S3InternalServiceGrpc.getS3ListingMethod) == null) {
          S3InternalServiceGrpc.getS3ListingMethod = getS3ListingMethod =
              io.grpc.MethodDescriptor.<com.yandex.ydb.s3_internal.S3InternalProtos.S3ListingRequest, com.yandex.ydb.s3_internal.S3InternalProtos.S3ListingResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "S3Listing"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.yandex.ydb.s3_internal.S3InternalProtos.S3ListingRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.yandex.ydb.s3_internal.S3InternalProtos.S3ListingResponse.getDefaultInstance()))
              .setSchemaDescriptor(new S3InternalServiceMethodDescriptorSupplier("S3Listing"))
              .build();
        }
      }
    }
    return getS3ListingMethod;
  }

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static S3InternalServiceStub newStub(io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<S3InternalServiceStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<S3InternalServiceStub>() {
        @java.lang.Override
        public S3InternalServiceStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new S3InternalServiceStub(channel, callOptions);
        }
      };
    return S3InternalServiceStub.newStub(factory, channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static S3InternalServiceBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<S3InternalServiceBlockingStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<S3InternalServiceBlockingStub>() {
        @java.lang.Override
        public S3InternalServiceBlockingStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new S3InternalServiceBlockingStub(channel, callOptions);
        }
      };
    return S3InternalServiceBlockingStub.newStub(factory, channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static S3InternalServiceFutureStub newFutureStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<S3InternalServiceFutureStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<S3InternalServiceFutureStub>() {
        @java.lang.Override
        public S3InternalServiceFutureStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new S3InternalServiceFutureStub(channel, callOptions);
        }
      };
    return S3InternalServiceFutureStub.newStub(factory, channel);
  }

  /**
   */
  public static abstract class S3InternalServiceImplBase implements io.grpc.BindableService {

    /**
     */
    public void s3Listing(com.yandex.ydb.s3_internal.S3InternalProtos.S3ListingRequest request,
        io.grpc.stub.StreamObserver<com.yandex.ydb.s3_internal.S3InternalProtos.S3ListingResponse> responseObserver) {
      asyncUnimplementedUnaryCall(getS3ListingMethod(), responseObserver);
    }

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
          .addMethod(
            getS3ListingMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                com.yandex.ydb.s3_internal.S3InternalProtos.S3ListingRequest,
                com.yandex.ydb.s3_internal.S3InternalProtos.S3ListingResponse>(
                  this, METHODID_S3LISTING)))
          .build();
    }
  }

  /**
   */
  public static final class S3InternalServiceStub extends io.grpc.stub.AbstractAsyncStub<S3InternalServiceStub> {
    private S3InternalServiceStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected S3InternalServiceStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new S3InternalServiceStub(channel, callOptions);
    }

    /**
     */
    public void s3Listing(com.yandex.ydb.s3_internal.S3InternalProtos.S3ListingRequest request,
        io.grpc.stub.StreamObserver<com.yandex.ydb.s3_internal.S3InternalProtos.S3ListingResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getS3ListingMethod(), getCallOptions()), request, responseObserver);
    }
  }

  /**
   */
  public static final class S3InternalServiceBlockingStub extends io.grpc.stub.AbstractBlockingStub<S3InternalServiceBlockingStub> {
    private S3InternalServiceBlockingStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected S3InternalServiceBlockingStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new S3InternalServiceBlockingStub(channel, callOptions);
    }

    /**
     */
    public com.yandex.ydb.s3_internal.S3InternalProtos.S3ListingResponse s3Listing(com.yandex.ydb.s3_internal.S3InternalProtos.S3ListingRequest request) {
      return blockingUnaryCall(
          getChannel(), getS3ListingMethod(), getCallOptions(), request);
    }
  }

  /**
   */
  public static final class S3InternalServiceFutureStub extends io.grpc.stub.AbstractFutureStub<S3InternalServiceFutureStub> {
    private S3InternalServiceFutureStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected S3InternalServiceFutureStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new S3InternalServiceFutureStub(channel, callOptions);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<com.yandex.ydb.s3_internal.S3InternalProtos.S3ListingResponse> s3Listing(
        com.yandex.ydb.s3_internal.S3InternalProtos.S3ListingRequest request) {
      return futureUnaryCall(
          getChannel().newCall(getS3ListingMethod(), getCallOptions()), request);
    }
  }

  private static final int METHODID_S3LISTING = 0;

  private static final class MethodHandlers<Req, Resp> implements
      io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
    private final S3InternalServiceImplBase serviceImpl;
    private final int methodId;

    MethodHandlers(S3InternalServiceImplBase serviceImpl, int methodId) {
      this.serviceImpl = serviceImpl;
      this.methodId = methodId;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_S3LISTING:
          serviceImpl.s3Listing((com.yandex.ydb.s3_internal.S3InternalProtos.S3ListingRequest) request,
              (io.grpc.stub.StreamObserver<com.yandex.ydb.s3_internal.S3InternalProtos.S3ListingResponse>) responseObserver);
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

  private static abstract class S3InternalServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
    S3InternalServiceBaseDescriptorSupplier() {}

    @java.lang.Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return com.yandex.ydb.s3_internal.v1.YdbS3InternalV1.getDescriptor();
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
      return getFileDescriptor().findServiceByName("S3InternalService");
    }
  }

  private static final class S3InternalServiceFileDescriptorSupplier
      extends S3InternalServiceBaseDescriptorSupplier {
    S3InternalServiceFileDescriptorSupplier() {}
  }

  private static final class S3InternalServiceMethodDescriptorSupplier
      extends S3InternalServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
    private final String methodName;

    S3InternalServiceMethodDescriptorSupplier(String methodName) {
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
      synchronized (S3InternalServiceGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .setSchemaDescriptor(new S3InternalServiceFileDescriptorSupplier())
              .addMethod(getS3ListingMethod())
              .build();
        }
      }
    }
    return result;
  }
}
