package com.cybernerd404.fairsplit.data.utils

suspend fun <T> safeApiCall(apiCall: suspend () -> T): DataResource<T> {
    return try {
        val result = apiCall()
        DataResource.success(result)
    } catch (throwable: Throwable) {
        DataResource.error(throwable)
    }
}
