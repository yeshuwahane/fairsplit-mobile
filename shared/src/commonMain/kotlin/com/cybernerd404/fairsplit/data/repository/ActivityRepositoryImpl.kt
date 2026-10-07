package com.cybernerd404.fairsplit.data.repository

import com.cybernerd404.fairsplit.data.local.MockDataProvider
import com.cybernerd404.fairsplit.data.remote.datasource.RemoteSyncDataSource
import com.cybernerd404.fairsplit.data.remote.dto.toDto
import com.cybernerd404.fairsplit.data.utils.DataResource
import com.cybernerd404.fairsplit.data.utils.safeApiCall
import com.cybernerd404.fairsplit.domain.model.ActivityEntry
import com.cybernerd404.fairsplit.domain.repository.ActivityRepository

class ActivityRepositoryImpl(
    private val syncDataSource: RemoteSyncDataSource? = null
) : ActivityRepository {

    override suspend fun getActivitiesForEpic(epicId: String): DataResource<List<ActivityEntry>> = safeApiCall {
        try {
            val remoteActs = syncDataSource?.getActivities(epicId) ?: emptyList()
            if (remoteActs.isNotEmpty()) {
                remoteActs.forEach { dto ->
                    val act = dto.toDomain()
                    val idx = MockDataProvider.allActivities.indexOfFirst { it.id == act.id }
                    if (idx >= 0) {
                        MockDataProvider.allActivities[idx] = act
                    } else {
                        MockDataProvider.allActivities.add(0, act)
                    }
                }
                MockDataProvider.persistToLocalDatabase()
            }
        } catch (_: Exception) {}
        MockDataProvider.allActivities.filter { it.epicId == epicId }
    }

    override suspend fun getAllActivities(): DataResource<List<ActivityEntry>> = safeApiCall {
        try {
            val remoteActs = syncDataSource?.getActivities()
            if (remoteActs != null) {
                MockDataProvider.allActivities.clear()
                MockDataProvider.allActivities.addAll(remoteActs.map { it.toDomain() })
                MockDataProvider.persistToLocalDatabase()
            }
        } catch (_: Exception) {}
        MockDataProvider.allActivities
    }

    override suspend fun logActivity(entry: ActivityEntry): DataResource<ActivityEntry> = safeApiCall {
        MockDataProvider.allActivities.add(0, entry)
        try {
            syncDataSource?.addActivity(entry.toDto())
        } catch (_: Exception) {}
        entry
    }
}
