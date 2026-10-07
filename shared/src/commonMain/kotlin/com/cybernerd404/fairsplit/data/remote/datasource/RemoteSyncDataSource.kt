package com.cybernerd404.fairsplit.data.remote.datasource

import com.cybernerd404.fairsplit.data.local.TokenStorage
import com.cybernerd404.fairsplit.data.remote.dto.*
import io.github.aakira.napier.Napier
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.*
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import kotlinx.serialization.Serializable

@Serializable
data class JoinEpicBody(
    val inviteCode: String,
    val member: MemberDto
)

@Serializable
data class SyncImageUploadRequest(
    val fileName: String,
    val base64Data: String,
    val mimeType: String = "image/jpeg"
)

@Serializable
data class SyncImageUploadResponse(
    val url: String,
    val fileName: String
)

interface RemoteSyncDataSource {
    suspend fun getEpics(): List<EpicDto>
    suspend fun getEpicById(id: String): EpicDto?
    suspend fun getEpicByInviteCode(code: String): EpicDto?
    suspend fun saveEpic(epic: EpicDto): EpicDto
    suspend fun updateEpic(epic: EpicDto): EpicDto
    suspend fun deleteEpic(id: String): Boolean
    suspend fun joinEpic(inviteCode: String, member: MemberDto): EpicDto?
    suspend fun getExpenses(epicId: String): List<ExpenseDto>
    suspend fun addExpense(expense: ExpenseDto): ExpenseDto
    suspend fun deleteExpense(epicId: String, expenseId: String): Boolean
    suspend fun getTransactions(): List<TransactionDto>
    suspend fun addTransaction(tx: TransactionDto): TransactionDto
    suspend fun updateTransaction(tx: TransactionDto): TransactionDto
    suspend fun deleteTransaction(transactionId: String): Boolean
    suspend fun getActivities(epicId: String? = null): List<ActivityDto>
    suspend fun addActivity(act: ActivityDto): ActivityDto
    suspend fun uploadImage(fileName: String, base64Data: String, mimeType: String = "image/jpeg"): String?
}

class RemoteSyncDataSourceImpl(
    private val client: HttpClient,
    private val tokenStorage: TokenStorage? = null
) : RemoteSyncDataSource {

    private fun HttpRequestBuilder.attachAuthAndUser() {
        contentType(ContentType.Application.Json)
        val token = tokenStorage?.getAccessToken()
        if (!token.isNullOrBlank()) {
            header(HttpHeaders.Authorization, "Bearer $token")
        }
        val profile = com.cybernerd404.fairsplit.data.local.LocalDatabase.getUserProfile()
        if (profile != null && profile.id.isNotBlank()) {
            header("X-User-Id", profile.id)
            if (profile.name.isNotBlank()) {
                header("X-User-Name", profile.name)
            }
        }
    }

    override suspend fun getEpics(): List<EpicDto> {
        return try {
            val res = client.get("/api/v1/sync/epics") {
                attachAuthAndUser()
            }.body<ApiResponse<List<EpicDto>>>()
            res.data ?: emptyList()
        } catch (e: Exception) {
            Napier.d(tag = "FAIRSPLIT_SYNC", message = "getEpics failed: ${e.message}")
            emptyList()
        }
    }

    override suspend fun getEpicById(id: String): EpicDto? {
        return try {
            val res = client.get("/api/v1/sync/epics/$id") {
                attachAuthAndUser()
            }.body<ApiResponse<EpicDto>>()
            res.data
        } catch (e: Exception) {
            Napier.d(tag = "FAIRSPLIT_SYNC", message = "getEpicById failed: ${e.message}")
            null
        }
    }

    override suspend fun getEpicByInviteCode(code: String): EpicDto? {
        return try {
            val res = client.get("/api/v1/sync/epics/invite/$code") {
                attachAuthAndUser()
            }.body<ApiResponse<EpicDto>>()
            res.data
        } catch (e: Exception) {
            Napier.d(tag = "FAIRSPLIT_SYNC", message = "getEpicByInviteCode failed: ${e.message}")
            null
        }
    }

    override suspend fun saveEpic(epic: EpicDto): EpicDto {
        val res = client.post("/api/v1/sync/epics") {
            attachAuthAndUser()
            setBody(epic)
        }.body<ApiResponse<EpicDto>>()
        return res.data ?: throw Exception(res.error?.message ?: "Failed to save Epic on server")
    }

    override suspend fun updateEpic(epic: EpicDto): EpicDto {
        return try {
            val res = client.put("/api/v1/sync/epics/${epic.id}") {
                attachAuthAndUser()
                setBody(epic)
            }.body<ApiResponse<EpicDto>>()
            res.data ?: epic
        } catch (e: Exception) {
            Napier.d(tag = "FAIRSPLIT_SYNC", message = "updateEpic failed: ${e.message}")
            epic
        }
    }

    override suspend fun deleteEpic(id: String): Boolean {
        return try {
            val res = client.delete("/api/v1/sync/epics/$id") {
                attachAuthAndUser()
            }.body<ApiResponse<Boolean>>()
            res.data ?: true
        } catch (e: Exception) {
            Napier.d(tag = "FAIRSPLIT_SYNC", message = "deleteEpic failed: ${e.message}")
            false
        }
    }

    override suspend fun joinEpic(inviteCode: String, member: MemberDto): EpicDto? {
        return try {
            val res = client.post("/api/v1/sync/epics/join") {
                attachAuthAndUser()
                setBody(JoinEpicBody(inviteCode = inviteCode, member = member))
            }.body<ApiResponse<EpicDto>>()
            res.data
        } catch (e: Exception) {
            Napier.d(tag = "FAIRSPLIT_SYNC", message = "joinEpic failed: ${e.message}")
            null
        }
    }
    override suspend fun getExpenses(epicId: String): List<ExpenseDto> {
        return try {
            val res = client.get("/api/v1/sync/epics/$epicId/expenses") {
                attachAuthAndUser()
            }.body<ApiResponse<List<ExpenseDto>>>()
            res.data ?: emptyList()
        } catch (e: Exception) {
            Napier.d(tag = "FAIRSPLIT_SYNC", message = "getExpenses failed: ${e.message}")
            emptyList()
        }
    }

    override suspend fun addExpense(expense: ExpenseDto): ExpenseDto {
        return try {
            val res = client.post("/api/v1/sync/epics/${expense.epicId}/expenses") {
                attachAuthAndUser()
                setBody(expense)
            }.body<ApiResponse<ExpenseDto>>()
            res.data ?: expense
        } catch (e: Exception) {
            Napier.d(tag = "FAIRSPLIT_SYNC", message = "addExpense failed: ${e.message}")
            expense
        }
    }

    override suspend fun deleteExpense(epicId: String, expenseId: String): Boolean {
        return try {
            val res = client.delete("/api/v1/sync/epics/$epicId/expenses/$expenseId") {
                attachAuthAndUser()
            }.body<ApiResponse<Boolean>>()
            res.data ?: false
        } catch (e: Exception) {
            Napier.d(tag = "FAIRSPLIT_SYNC", message = "deleteExpense failed: ${e.message}")
            false
        }
    }

    override suspend fun getTransactions(): List<TransactionDto> {
        return try {
            val res = client.get("/api/v1/sync/transactions") {
                attachAuthAndUser()
            }.body<ApiResponse<List<TransactionDto>>>()
            res.data ?: emptyList()
        } catch (e: Exception) {
            Napier.d(tag = "FAIRSPLIT_SYNC", message = "getTransactions failed: ${e.message}")
            emptyList()
        }
    }

    override suspend fun addTransaction(tx: TransactionDto): TransactionDto {
        return try {
            val res = client.post("/api/v1/sync/transactions") {
                attachAuthAndUser()
                setBody(tx)
            }.body<ApiResponse<TransactionDto>>()
            res.data ?: tx
        } catch (e: Exception) {
            Napier.d(tag = "FAIRSPLIT_SYNC", message = "addTransaction failed: ${e.message}")
            tx
        }
    }

    override suspend fun updateTransaction(tx: TransactionDto): TransactionDto {
        return try {
            val res = client.put("/api/v1/sync/transactions/${tx.id}") {
                attachAuthAndUser()
                setBody(tx)
            }.body<ApiResponse<TransactionDto>>()
            res.data ?: tx
        } catch (e: Exception) {
            Napier.d(tag = "FAIRSPLIT_SYNC", message = "updateTransaction failed: ${e.message}")
            tx
        }
    }

    override suspend fun deleteTransaction(transactionId: String): Boolean {
        return try {
            val res = client.delete("/api/v1/sync/transactions/$transactionId") {
                attachAuthAndUser()
            }.body<ApiResponse<Boolean>>()
            res.data ?: false
        } catch (e: Exception) {
            Napier.d(tag = "FAIRSPLIT_SYNC", message = "deleteTransaction failed: ${e.message}")
            false
        }
    }

    override suspend fun getActivities(epicId: String?): List<ActivityDto> {
        return try {
            val res = client.get("/api/v1/sync/activities") {
                attachAuthAndUser()
                if (!epicId.isNullOrBlank()) {
                    parameter("epicId", epicId)
                }
            }.body<ApiResponse<List<ActivityDto>>>()
            res.data ?: emptyList()
        } catch (e: Exception) {
            Napier.d(tag = "FAIRSPLIT_SYNC", message = "getActivities failed: ${e.message}")
            emptyList()
        }
    }

    override suspend fun addActivity(act: ActivityDto): ActivityDto {
        return try {
            val res = client.post("/api/v1/sync/activities") {
                attachAuthAndUser()
                setBody(act)
            }.body<ApiResponse<ActivityDto>>()
            res.data ?: act
        } catch (e: Exception) {
            Napier.d(tag = "FAIRSPLIT_SYNC", message = "addActivity failed: ${e.message}")
            act
        }
    }

    override suspend fun uploadImage(fileName: String, base64Data: String, mimeType: String): String? {
        return try {
            val res = client.post("/api/v1/sync/upload") {
                contentType(ContentType.Application.Json)
                setBody(SyncImageUploadRequest(fileName = fileName, base64Data = base64Data, mimeType = mimeType))
            }.body<ApiResponse<SyncImageUploadResponse>>()
            val partialUrl = res.data?.url ?: return null
            if (partialUrl.startsWith("http://") || partialUrl.startsWith("https://")) {
                partialUrl
            } else {
                "${com.cybernerd404.fairsplit.data.remote.ApiConfig.baseUrl}$partialUrl"
            }
        } catch (e: Exception) {
            Napier.w(tag = "FAIRSPLIT_SYNC", message = "uploadImage failed: ${e.message}")
            null
        }
    }
}
