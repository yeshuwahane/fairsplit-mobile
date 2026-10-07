package com.cybernerd404.fairsplit.data.utils

data class DataResource<out T>(
    var resourceState: ResourceState,
    val data: T?,
    val error: Throwable?,
    val loadingPercentage: Int? = null,
) {
    companion object {
        fun <T> initial(data: T? = null): DataResource<T> {
            return DataResource(ResourceState.INITIAL, data, null)
        }

        fun <T> loading(percentage: Int? = null, data: T? = null): DataResource<T> {
            return DataResource(ResourceState.LOADING, data, null, percentage)
        }

        fun <T> success(data: T): DataResource<T> {
            return DataResource(ResourceState.SUCCESS, data, null)
        }

        fun <T> error(error: Throwable?, data: T? = null): DataResource<T> {
            return DataResource(ResourceState.ERROR, data, error)
        }
    }

    fun isLoading() = resourceState == ResourceState.LOADING
    fun isSuccess() = resourceState == ResourceState.SUCCESS
    fun isError() = resourceState == ResourceState.ERROR
}

enum class ResourceState {
    INITIAL,
    LOADING,
    SUCCESS,
    ERROR,
}
