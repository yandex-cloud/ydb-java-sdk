// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: kikimr/public/api/protos/ydb_coordination.proto

package com.yandex.ydb.coordination;

public interface SessionResponseOrBuilder extends
    // @@protoc_insertion_point(interface_extends:Ydb.Coordination.SessionResponse)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <code>.Ydb.Coordination.SessionResponse.PingPong ping = 1;</code>
   * @return Whether the ping field is set.
   */
  boolean hasPing();
  /**
   * <code>.Ydb.Coordination.SessionResponse.PingPong ping = 1;</code>
   * @return The ping.
   */
  com.yandex.ydb.coordination.SessionResponse.PingPong getPing();
  /**
   * <code>.Ydb.Coordination.SessionResponse.PingPong ping = 1;</code>
   */
  com.yandex.ydb.coordination.SessionResponse.PingPongOrBuilder getPingOrBuilder();

  /**
   * <code>.Ydb.Coordination.SessionResponse.PingPong pong = 2;</code>
   * @return Whether the pong field is set.
   */
  boolean hasPong();
  /**
   * <code>.Ydb.Coordination.SessionResponse.PingPong pong = 2;</code>
   * @return The pong.
   */
  com.yandex.ydb.coordination.SessionResponse.PingPong getPong();
  /**
   * <code>.Ydb.Coordination.SessionResponse.PingPong pong = 2;</code>
   */
  com.yandex.ydb.coordination.SessionResponse.PingPongOrBuilder getPongOrBuilder();

  /**
   * <code>.Ydb.Coordination.SessionResponse.Failure failure = 3;</code>
   * @return Whether the failure field is set.
   */
  boolean hasFailure();
  /**
   * <code>.Ydb.Coordination.SessionResponse.Failure failure = 3;</code>
   * @return The failure.
   */
  com.yandex.ydb.coordination.SessionResponse.Failure getFailure();
  /**
   * <code>.Ydb.Coordination.SessionResponse.Failure failure = 3;</code>
   */
  com.yandex.ydb.coordination.SessionResponse.FailureOrBuilder getFailureOrBuilder();

  /**
   * <code>.Ydb.Coordination.SessionResponse.SessionStarted session_started = 4;</code>
   * @return Whether the sessionStarted field is set.
   */
  boolean hasSessionStarted();
  /**
   * <code>.Ydb.Coordination.SessionResponse.SessionStarted session_started = 4;</code>
   * @return The sessionStarted.
   */
  com.yandex.ydb.coordination.SessionResponse.SessionStarted getSessionStarted();
  /**
   * <code>.Ydb.Coordination.SessionResponse.SessionStarted session_started = 4;</code>
   */
  com.yandex.ydb.coordination.SessionResponse.SessionStartedOrBuilder getSessionStartedOrBuilder();

  /**
   * <code>.Ydb.Coordination.SessionResponse.SessionStopped session_stopped = 5;</code>
   * @return Whether the sessionStopped field is set.
   */
  boolean hasSessionStopped();
  /**
   * <code>.Ydb.Coordination.SessionResponse.SessionStopped session_stopped = 5;</code>
   * @return The sessionStopped.
   */
  com.yandex.ydb.coordination.SessionResponse.SessionStopped getSessionStopped();
  /**
   * <code>.Ydb.Coordination.SessionResponse.SessionStopped session_stopped = 5;</code>
   */
  com.yandex.ydb.coordination.SessionResponse.SessionStoppedOrBuilder getSessionStoppedOrBuilder();

  /**
   * <code>.Ydb.Coordination.Unsupported unsupported_6 = 6;</code>
   * @return Whether the unsupported6 field is set.
   */
  boolean hasUnsupported6();
  /**
   * <code>.Ydb.Coordination.Unsupported unsupported_6 = 6;</code>
   * @return The unsupported6.
   */
  com.yandex.ydb.coordination.Unsupported getUnsupported6();
  /**
   * <code>.Ydb.Coordination.Unsupported unsupported_6 = 6;</code>
   */
  com.yandex.ydb.coordination.UnsupportedOrBuilder getUnsupported6OrBuilder();

  /**
   * <code>.Ydb.Coordination.Unsupported unsupported_7 = 7;</code>
   * @return Whether the unsupported7 field is set.
   */
  boolean hasUnsupported7();
  /**
   * <code>.Ydb.Coordination.Unsupported unsupported_7 = 7;</code>
   * @return The unsupported7.
   */
  com.yandex.ydb.coordination.Unsupported getUnsupported7();
  /**
   * <code>.Ydb.Coordination.Unsupported unsupported_7 = 7;</code>
   */
  com.yandex.ydb.coordination.UnsupportedOrBuilder getUnsupported7OrBuilder();

  /**
   * <code>.Ydb.Coordination.SessionResponse.AcquireSemaphorePending acquire_semaphore_pending = 8;</code>
   * @return Whether the acquireSemaphorePending field is set.
   */
  boolean hasAcquireSemaphorePending();
  /**
   * <code>.Ydb.Coordination.SessionResponse.AcquireSemaphorePending acquire_semaphore_pending = 8;</code>
   * @return The acquireSemaphorePending.
   */
  com.yandex.ydb.coordination.SessionResponse.AcquireSemaphorePending getAcquireSemaphorePending();
  /**
   * <code>.Ydb.Coordination.SessionResponse.AcquireSemaphorePending acquire_semaphore_pending = 8;</code>
   */
  com.yandex.ydb.coordination.SessionResponse.AcquireSemaphorePendingOrBuilder getAcquireSemaphorePendingOrBuilder();

  /**
   * <code>.Ydb.Coordination.SessionResponse.AcquireSemaphoreResult acquire_semaphore_result = 9;</code>
   * @return Whether the acquireSemaphoreResult field is set.
   */
  boolean hasAcquireSemaphoreResult();
  /**
   * <code>.Ydb.Coordination.SessionResponse.AcquireSemaphoreResult acquire_semaphore_result = 9;</code>
   * @return The acquireSemaphoreResult.
   */
  com.yandex.ydb.coordination.SessionResponse.AcquireSemaphoreResult getAcquireSemaphoreResult();
  /**
   * <code>.Ydb.Coordination.SessionResponse.AcquireSemaphoreResult acquire_semaphore_result = 9;</code>
   */
  com.yandex.ydb.coordination.SessionResponse.AcquireSemaphoreResultOrBuilder getAcquireSemaphoreResultOrBuilder();

  /**
   * <code>.Ydb.Coordination.SessionResponse.ReleaseSemaphoreResult release_semaphore_result = 10;</code>
   * @return Whether the releaseSemaphoreResult field is set.
   */
  boolean hasReleaseSemaphoreResult();
  /**
   * <code>.Ydb.Coordination.SessionResponse.ReleaseSemaphoreResult release_semaphore_result = 10;</code>
   * @return The releaseSemaphoreResult.
   */
  com.yandex.ydb.coordination.SessionResponse.ReleaseSemaphoreResult getReleaseSemaphoreResult();
  /**
   * <code>.Ydb.Coordination.SessionResponse.ReleaseSemaphoreResult release_semaphore_result = 10;</code>
   */
  com.yandex.ydb.coordination.SessionResponse.ReleaseSemaphoreResultOrBuilder getReleaseSemaphoreResultOrBuilder();

  /**
   * <code>.Ydb.Coordination.SessionResponse.DescribeSemaphoreResult describe_semaphore_result = 11;</code>
   * @return Whether the describeSemaphoreResult field is set.
   */
  boolean hasDescribeSemaphoreResult();
  /**
   * <code>.Ydb.Coordination.SessionResponse.DescribeSemaphoreResult describe_semaphore_result = 11;</code>
   * @return The describeSemaphoreResult.
   */
  com.yandex.ydb.coordination.SessionResponse.DescribeSemaphoreResult getDescribeSemaphoreResult();
  /**
   * <code>.Ydb.Coordination.SessionResponse.DescribeSemaphoreResult describe_semaphore_result = 11;</code>
   */
  com.yandex.ydb.coordination.SessionResponse.DescribeSemaphoreResultOrBuilder getDescribeSemaphoreResultOrBuilder();

  /**
   * <code>.Ydb.Coordination.SessionResponse.DescribeSemaphoreChanged describe_semaphore_changed = 12;</code>
   * @return Whether the describeSemaphoreChanged field is set.
   */
  boolean hasDescribeSemaphoreChanged();
  /**
   * <code>.Ydb.Coordination.SessionResponse.DescribeSemaphoreChanged describe_semaphore_changed = 12;</code>
   * @return The describeSemaphoreChanged.
   */
  com.yandex.ydb.coordination.SessionResponse.DescribeSemaphoreChanged getDescribeSemaphoreChanged();
  /**
   * <code>.Ydb.Coordination.SessionResponse.DescribeSemaphoreChanged describe_semaphore_changed = 12;</code>
   */
  com.yandex.ydb.coordination.SessionResponse.DescribeSemaphoreChangedOrBuilder getDescribeSemaphoreChangedOrBuilder();

  /**
   * <code>.Ydb.Coordination.SessionResponse.CreateSemaphoreResult create_semaphore_result = 13;</code>
   * @return Whether the createSemaphoreResult field is set.
   */
  boolean hasCreateSemaphoreResult();
  /**
   * <code>.Ydb.Coordination.SessionResponse.CreateSemaphoreResult create_semaphore_result = 13;</code>
   * @return The createSemaphoreResult.
   */
  com.yandex.ydb.coordination.SessionResponse.CreateSemaphoreResult getCreateSemaphoreResult();
  /**
   * <code>.Ydb.Coordination.SessionResponse.CreateSemaphoreResult create_semaphore_result = 13;</code>
   */
  com.yandex.ydb.coordination.SessionResponse.CreateSemaphoreResultOrBuilder getCreateSemaphoreResultOrBuilder();

  /**
   * <code>.Ydb.Coordination.SessionResponse.UpdateSemaphoreResult update_semaphore_result = 14;</code>
   * @return Whether the updateSemaphoreResult field is set.
   */
  boolean hasUpdateSemaphoreResult();
  /**
   * <code>.Ydb.Coordination.SessionResponse.UpdateSemaphoreResult update_semaphore_result = 14;</code>
   * @return The updateSemaphoreResult.
   */
  com.yandex.ydb.coordination.SessionResponse.UpdateSemaphoreResult getUpdateSemaphoreResult();
  /**
   * <code>.Ydb.Coordination.SessionResponse.UpdateSemaphoreResult update_semaphore_result = 14;</code>
   */
  com.yandex.ydb.coordination.SessionResponse.UpdateSemaphoreResultOrBuilder getUpdateSemaphoreResultOrBuilder();

  /**
   * <code>.Ydb.Coordination.SessionResponse.DeleteSemaphoreResult delete_semaphore_result = 15;</code>
   * @return Whether the deleteSemaphoreResult field is set.
   */
  boolean hasDeleteSemaphoreResult();
  /**
   * <code>.Ydb.Coordination.SessionResponse.DeleteSemaphoreResult delete_semaphore_result = 15;</code>
   * @return The deleteSemaphoreResult.
   */
  com.yandex.ydb.coordination.SessionResponse.DeleteSemaphoreResult getDeleteSemaphoreResult();
  /**
   * <code>.Ydb.Coordination.SessionResponse.DeleteSemaphoreResult delete_semaphore_result = 15;</code>
   */
  com.yandex.ydb.coordination.SessionResponse.DeleteSemaphoreResultOrBuilder getDeleteSemaphoreResultOrBuilder();

  /**
   * <code>.Ydb.Coordination.Unsupported unsupported_16 = 16;</code>
   * @return Whether the unsupported16 field is set.
   */
  boolean hasUnsupported16();
  /**
   * <code>.Ydb.Coordination.Unsupported unsupported_16 = 16;</code>
   * @return The unsupported16.
   */
  com.yandex.ydb.coordination.Unsupported getUnsupported16();
  /**
   * <code>.Ydb.Coordination.Unsupported unsupported_16 = 16;</code>
   */
  com.yandex.ydb.coordination.UnsupportedOrBuilder getUnsupported16OrBuilder();

  /**
   * <code>.Ydb.Coordination.Unsupported unsupported_17 = 17;</code>
   * @return Whether the unsupported17 field is set.
   */
  boolean hasUnsupported17();
  /**
   * <code>.Ydb.Coordination.Unsupported unsupported_17 = 17;</code>
   * @return The unsupported17.
   */
  com.yandex.ydb.coordination.Unsupported getUnsupported17();
  /**
   * <code>.Ydb.Coordination.Unsupported unsupported_17 = 17;</code>
   */
  com.yandex.ydb.coordination.UnsupportedOrBuilder getUnsupported17OrBuilder();

  /**
   * <code>.Ydb.Coordination.Unsupported unsupported_18 = 18;</code>
   * @return Whether the unsupported18 field is set.
   */
  boolean hasUnsupported18();
  /**
   * <code>.Ydb.Coordination.Unsupported unsupported_18 = 18;</code>
   * @return The unsupported18.
   */
  com.yandex.ydb.coordination.Unsupported getUnsupported18();
  /**
   * <code>.Ydb.Coordination.Unsupported unsupported_18 = 18;</code>
   */
  com.yandex.ydb.coordination.UnsupportedOrBuilder getUnsupported18OrBuilder();

  public com.yandex.ydb.coordination.SessionResponse.ResponseCase getResponseCase();
}
