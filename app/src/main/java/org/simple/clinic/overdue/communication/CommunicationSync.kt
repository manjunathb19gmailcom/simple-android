package org.simple.clinic.overdue.communication

import com.f2prateek.rx.preferences2.Preference
import io.reactivex.Completable
import org.simple.clinic.sync.ModelSync
import org.simple.clinic.sync.SyncCoordinator
import org.simple.clinic.util.Optional
import javax.inject.Inject
import javax.inject.Named

class CommunicationSync @Inject constructor(
    private val syncCoordinator: SyncCoordinator,
    private val repository: CommunicationRepository,
    private val api: CommunicationSyncApiV2,
    @Named("last_communication_pull_token") private val lastPullToken: Preference<Optional<String>>
): ModelSync {

  override fun sync(): Completable {
    return Completable.mergeArrayDelayError(push(), pull())
  }

  override fun push(): Completable {
    return syncCoordinator.push(repository, pushNetworkCall = { api.push(toRequest(it)) })
  }

  override fun pull(): Completable {
    return syncCoordinator.pull(
        repository = repository,
        lastPullToken = lastPullToken,
        pullNetworkCall = api::pull)
  }

  private fun toRequest(schedules: List<Communication>): CommunicationPushRequest {
    val payloads = schedules
        .map {
          it.run {
            CommunicationPayload(
                uuid = uuid,
                appointmentUuid = appointmentUuid,
                userUuid = userUuid,
                type = type,
                result = result,
                createdAt = createdAt,
                updatedAt = updatedAt,
                deletedAt = deletedAt)
          }
        }
        .toList()
    return CommunicationPushRequest(payloads)
  }
}
