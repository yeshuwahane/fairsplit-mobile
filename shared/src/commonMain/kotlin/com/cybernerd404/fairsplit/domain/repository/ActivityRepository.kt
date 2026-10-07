package com.cybernerd404.fairsplit.domain.repository

import com.cybernerd404.fairsplit.data.utils.DataResource
import com.cybernerd404.fairsplit.domain.model.ActivityEntry

interface ActivityRepository {
    suspend fun getActivitiesForEpic(epicId: String): DataResource<List<ActivityEntry>>
    suspend fun getAllActivities(): DataResource<List<ActivityEntry>>
    suspend fun logActivity(entry: ActivityEntry): DataResource<ActivityEntry>
}
