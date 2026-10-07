package com.cybernerd404.fairsplit.domain.usecase

import com.cybernerd404.fairsplit.data.utils.DataResource
import com.cybernerd404.fairsplit.domain.model.ActivityEntry
import com.cybernerd404.fairsplit.domain.repository.ActivityRepository

class GetEpicActivitiesUseCase(private val repository: ActivityRepository) {
    suspend operator fun invoke(epicId: String): DataResource<List<ActivityEntry>> {
        return repository.getActivitiesForEpic(epicId)
    }
}

class GetAllActivitiesUseCase(private val repository: ActivityRepository) {
    suspend operator fun invoke(): DataResource<List<ActivityEntry>> {
        return repository.getAllActivities()
    }
}
