// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: kikimr/public/api/grpc/draft/ydb_persqueue_v1.proto

package com.yandex.ydb.persqueue.v1;

public final class YdbPersqueueV1 {
  private YdbPersqueueV1() {}
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
      "\n3kikimr/public/api/grpc/draft/ydb_persq" +
      "ueue_v1.proto\022\020Ydb.PersQueue.V1\032>kikimr/" +
      "public/api/protos/ydb_persqueue_cluster_" +
      "discovery.proto\032/kikimr/public/api/proto" +
      "s/ydb_persqueue_v1.proto2\237\007\n\020PersQueueSe" +
      "rvice\022r\n\016StreamingWrite\022-.Ydb.PersQueue." +
      "V1.StreamingWriteClientMessage\032-.Ydb.Per" +
      "sQueue.V1.StreamingWriteServerMessage(\0010" +
      "\001\022\212\001\n\026MigrationStreamingRead\0225.Ydb.PersQ" +
      "ueue.V1.MigrationStreamingReadClientMess" +
      "age\0325.Ydb.PersQueue.V1.MigrationStreamin" +
      "gReadServerMessage(\0010\001\022\\\n\023GetReadSession" +
      "sInfo\022!.Ydb.PersQueue.V1.ReadInfoRequest" +
      "\032\".Ydb.PersQueue.V1.ReadInfoResponse\022`\n\r" +
      "DescribeTopic\022&.Ydb.PersQueue.V1.Describ" +
      "eTopicRequest\032\'.Ydb.PersQueue.V1.Describ" +
      "eTopicResponse\022T\n\tDropTopic\022\".Ydb.PersQu" +
      "eue.V1.DropTopicRequest\032#.Ydb.PersQueue." +
      "V1.DropTopicResponse\022Z\n\013CreateTopic\022$.Yd" +
      "b.PersQueue.V1.CreateTopicRequest\032%.Ydb." +
      "PersQueue.V1.CreateTopicResponse\022W\n\nAlte" +
      "rTopic\022#.Ydb.PersQueue.V1.AlterTopicRequ" +
      "est\032$.Ydb.PersQueue.V1.AlterTopicRespons" +
      "e\022Z\n\013AddReadRule\022$.Ydb.PersQueue.V1.AddR" +
      "eadRuleRequest\032%.Ydb.PersQueue.V1.AddRea" +
      "dRuleResponse\022c\n\016RemoveReadRule\022\'.Ydb.Pe" +
      "rsQueue.V1.RemoveReadRuleRequest\032(.Ydb.P" +
      "ersQueue.V1.RemoveReadRuleResponse2\241\001\n\027C" +
      "lusterDiscoveryService\022\205\001\n\020DiscoverClust" +
      "ers\0227.Ydb.PersQueue.ClusterDiscovery.Dis" +
      "coverClustersRequest\0328.Ydb.PersQueue.Clu" +
      "sterDiscovery.DiscoverClustersResponseB " +
      "\n\033com.yandex.ydb.persqueue.v1\370\001\001b\006proto3"
    };
    descriptor = com.google.protobuf.Descriptors.FileDescriptor
      .internalBuildGeneratedFileFrom(descriptorData,
        new com.google.protobuf.Descriptors.FileDescriptor[] {
          com.yandex.ydb.persqueue.cluster_discovery.YdbPersqueueClusterDiscovery.getDescriptor(),
          com.yandex.ydb.persqueue.YdbPersqueueV1.getDescriptor(),
        });
    com.yandex.ydb.persqueue.cluster_discovery.YdbPersqueueClusterDiscovery.getDescriptor();
    com.yandex.ydb.persqueue.YdbPersqueueV1.getDescriptor();
  }

  // @@protoc_insertion_point(outer_class_scope)
}
