// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: kikimr/public/api/grpc/draft/ydb_logstore_v1.proto

package com.yandex.ydb.logstore.v1;

public final class YdbLogstoreV1 {
  private YdbLogstoreV1() {}
  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistryLite registry) {
  }

  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistry registry) {
    registerAllExtensions(
        (com.google.protobuf.ExtensionRegistryLite) registry);
  }

  public static com.google.protobuf.Descriptors.FileDescriptor
      getDescriptor() {
    return descriptor;
  }
  private static  com.google.protobuf.Descriptors.FileDescriptor
      descriptor;
  static {
    java.lang.String[] descriptorData = {
      "\n2kikimr/public/api/grpc/draft/ydb_logst" +
      "ore_v1.proto\022\017Ydb.LogStore.V1\0321kikimr/pu" +
      "blic/api/protos/draft/ydb_logstore.proto" +
      "2\231\005\n\017LogStoreService\022[\n\016CreateLogStore\022#" +
      ".Ydb.LogStore.CreateLogStoreRequest\032$.Yd" +
      "b.LogStore.CreateLogStoreResponse\022a\n\020Des" +
      "cribeLogStore\022%.Ydb.LogStore.DescribeLog" +
      "StoreRequest\032&.Ydb.LogStore.DescribeLogS" +
      "toreResponse\022U\n\014DropLogStore\022!.Ydb.LogSt" +
      "ore.DropLogStoreRequest\032\".Ydb.LogStore.D" +
      "ropLogStoreResponse\022[\n\016CreateLogTable\022#." +
      "Ydb.LogStore.CreateLogTableRequest\032$.Ydb" +
      ".LogStore.CreateLogTableResponse\022a\n\020Desc" +
      "ribeLogTable\022%.Ydb.LogStore.DescribeLogT" +
      "ableRequest\032&.Ydb.LogStore.DescribeLogTa" +
      "bleResponse\022U\n\014DropLogTable\022!.Ydb.LogSto" +
      "re.DropLogTableRequest\032\".Ydb.LogStore.Dr" +
      "opLogTableResponse\022X\n\rAlterLogTable\022\".Yd" +
      "b.LogStore.AlterLogTableRequest\032#.Ydb.Lo" +
      "gStore.AlterLogTableResponseB\034\n\032com.yand" +
      "ex.ydb.logstore.v1b\006proto3"
    };
    descriptor = com.google.protobuf.Descriptors.FileDescriptor
      .internalBuildGeneratedFileFrom(descriptorData,
        new com.google.protobuf.Descriptors.FileDescriptor[] {
          com.yandex.ydb.logstore.LogStoreProtos.getDescriptor(),
        });
    com.yandex.ydb.logstore.LogStoreProtos.getDescriptor();
  }

  // @@protoc_insertion_point(outer_class_scope)
}
