// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: kikimr/public/api/grpc/ydb_cms_v1.proto

package ru.yandex.ydb.cms.v1;

public final class YdbCmsV1 {
  private YdbCmsV1() {}
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
      "\n\'kikimr/public/api/grpc/ydb_cms_v1.prot" +
      "o\022\nYdb.Cms.V1\032&kikimr/public/api/protos/" +
      "ydb_cms.proto2\234\004\n\nCmsService\022Q\n\016CreateDa" +
      "tabase\022\036.Ydb.Cms.CreateDatabaseRequest\032\037" +
      ".Ydb.Cms.CreateDatabaseResponse\022Z\n\021GetDa" +
      "tabaseStatus\022!.Ydb.Cms.GetDatabaseStatus" +
      "Request\032\".Ydb.Cms.GetDatabaseStatusRespo" +
      "nse\022N\n\rAlterDatabase\022\035.Ydb.Cms.AlterData" +
      "baseRequest\032\036.Ydb.Cms.AlterDatabaseRespo" +
      "nse\022N\n\rListDatabases\022\035.Ydb.Cms.ListDatab",
      "asesRequest\032\036.Ydb.Cms.ListDatabasesRespo" +
      "nse\022Q\n\016RemoveDatabase\022\036.Ydb.Cms.RemoveDa" +
      "tabaseRequest\032\037.Ydb.Cms.RemoveDatabaseRe" +
      "sponse\022l\n\027DescribeDatabaseOptions\022\'.Ydb." +
      "Cms.DescribeDatabaseOptionsRequest\032(.Ydb" +
      ".Cms.DescribeDatabaseOptionsResponseB\026\n\024" +
      "ru.yandex.ydb.cms.v1b\006proto3"
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
          ru.yandex.ydb.cms.YdbCms.getDescriptor(),
        }, assigner);
    ru.yandex.ydb.cms.YdbCms.getDescriptor();
  }

  // @@protoc_insertion_point(outer_class_scope)
}
