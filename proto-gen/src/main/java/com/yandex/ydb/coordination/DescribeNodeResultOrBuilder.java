// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: kikimr/public/api/protos/ydb_coordination.proto

package com.yandex.ydb.coordination;

public interface DescribeNodeResultOrBuilder extends
    // @@protoc_insertion_point(interface_extends:Ydb.Coordination.DescribeNodeResult)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <code>.Ydb.Scheme.Entry self = 1;</code>
   * @return Whether the self field is set.
   */
  boolean hasSelf();
  /**
   * <code>.Ydb.Scheme.Entry self = 1;</code>
   * @return The self.
   */
  com.yandex.ydb.scheme.SchemeOperationProtos.Entry getSelf();
  /**
   * <code>.Ydb.Scheme.Entry self = 1;</code>
   */
  com.yandex.ydb.scheme.SchemeOperationProtos.EntryOrBuilder getSelfOrBuilder();

  /**
   * <code>.Ydb.Coordination.Config config = 2;</code>
   * @return Whether the config field is set.
   */
  boolean hasConfig();
  /**
   * <code>.Ydb.Coordination.Config config = 2;</code>
   * @return The config.
   */
  com.yandex.ydb.coordination.Config getConfig();
  /**
   * <code>.Ydb.Coordination.Config config = 2;</code>
   */
  com.yandex.ydb.coordination.ConfigOrBuilder getConfigOrBuilder();
}
