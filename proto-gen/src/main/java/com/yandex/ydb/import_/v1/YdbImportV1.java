// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: kikimr/public/api/grpc/ydb_import_v1.proto

package com.yandex.ydb.import_.v1;

public final class YdbImportV1 {
  private YdbImportV1() {}
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
      "\n*kikimr/public/api/grpc/ydb_import_v1.p" +
      "roto\022\rYdb.Import.V1\032)kikimr/public/api/p" +
      "rotos/ydb_import.proto2b\n\rImportService\022" +
      "Q\n\014ImportFromS3\022\037.Ydb.Import.ImportFromS" +
      "3Request\032 .Ydb.Import.ImportFromS3Respon" +
      "seB\033\n\031com.yandex.ydb.import_.v1b\006proto3"
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
          com.yandex.ydb.import_.YdbImport.getDescriptor(),
        }, assigner);
    com.yandex.ydb.import_.YdbImport.getDescriptor();
  }

  // @@protoc_insertion_point(outer_class_scope)
}
