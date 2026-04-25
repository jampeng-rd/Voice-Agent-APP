package com.jam.voiceagent.data.repository

data class RepositoryResult<T>(
    val data: T? = null,
    val errorMessage: String? = null
) {
    val isSuccess: Boolean = data != null && errorMessage == null
}
