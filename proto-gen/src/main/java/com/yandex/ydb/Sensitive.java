// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: kikimr/public/api/protos/annotations/sensitive.proto

package com.yandex.ydb;

public final class Sensitive {
  private Sensitive() {}
  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistryLite registry) {
    registry.add(com.yandex.ydb.Sensitive.sensitive);
  }

  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistry registry) {
    registerAllExtensions(
        (com.google.protobuf.ExtensionRegistryLite) registry);
  }
  public static final int SENSITIVE_FIELD_NUMBER = 87655;
  /**
   * <pre>
   * do not print this field
   * </pre>
   *
   * <code>extend .google.protobuf.FieldOptions { ... }</code>
   */
  public static final
    com.google.protobuf.GeneratedMessage.GeneratedExtension<
      com.google.protobuf.DescriptorProtos.FieldOptions,
      java.lang.Boolean> sensitive = com.google.protobuf.GeneratedMessage
          .newFileScopedGeneratedExtension(
        java.lang.Boolean.class,
        null);

  public static com.google.protobuf.Descriptors.FileDescriptor
      getDescriptor() {
    return descriptor;
  }
  private static  com.google.protobuf.Descriptors.FileDescriptor
      descriptor;
  static {
    java.lang.String[] descriptorData = {
      "\n4kikimr/public/api/protos/annotations/s" +
      "ensitive.proto\022\003Ydb\032 google/protobuf/des" +
      "criptor.proto:2\n\tsensitive\022\035.google.prot" +
      "obuf.FieldOptions\030\347\254\005 \001(\010B\023\n\016com.yandex." +
      "ydb\370\001\001b\006proto3"
    };
    descriptor = com.google.protobuf.Descriptors.FileDescriptor
      .internalBuildGeneratedFileFrom(descriptorData,
        new com.google.protobuf.Descriptors.FileDescriptor[] {
          com.google.protobuf.DescriptorProtos.getDescriptor(),
        });
    sensitive.internalInit(descriptor.getExtensions().get(0));
    com.google.protobuf.DescriptorProtos.getDescriptor();
  }

  // @@protoc_insertion_point(outer_class_scope)
}
