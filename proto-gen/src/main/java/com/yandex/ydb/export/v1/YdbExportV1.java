// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: kikimr/public/api/grpc/ydb_export_v1.proto

package com.yandex.ydb.export.v1;

public final class YdbExportV1 {
  private YdbExportV1() {}
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
      "\n*kikimr/public/api/grpc/ydb_export_v1.p" +
      "roto\022\rYdb.Export.V1\032)kikimr/public/api/p" +
      "rotos/ydb_export.proto2\251\001\n\rExportService" +
      "\022K\n\nExportToYt\022\035.Ydb.Export.ExportToYtRe" +
      "quest\032\036.Ydb.Export.ExportToYtResponse\022K\n" +
      "\nExportToS3\022\035.Ydb.Export.ExportToS3Reque" +
      "st\032\036.Ydb.Export.ExportToS3ResponseB\032\n\030co" +
      "m.yandex.ydb.export.v1b\006proto3"
    };
    descriptor = com.google.protobuf.Descriptors.FileDescriptor
      .internalBuildGeneratedFileFrom(descriptorData,
        new com.google.protobuf.Descriptors.FileDescriptor[] {
          com.yandex.ydb.export.YdbExport.getDescriptor(),
        });
    com.yandex.ydb.export.YdbExport.getDescriptor();
  }

  // @@protoc_insertion_point(outer_class_scope)
}
