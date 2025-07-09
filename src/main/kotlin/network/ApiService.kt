package org.YairElazar.network

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.http.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import io.ktor.serialization.kotlinx.json.*

object ApiService {

    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                isLenient = true
                ignoreUnknownKeys = true
            })
        }

        engine {
            requestTimeout = 30_000
            pipelining = true
        }
    }

    @Serializable
    data class LoginRequest(val username: String, val password: String)

    @Serializable
    data class LoginResponse(val token: String)

    suspend fun login(username: String, password: String): LoginResponse {
        return client.post("http://localhost:8080/api/users/login") {
            contentType(ContentType.Application.Json)
            setBody(LoginRequest(username, password))
        }.body()
    }

    @Serializable
    data class Task(
        val id: Long,
        val title: String,
        val description: String? = null,
        val dueDate: String? = null,
        val category: String? = null,
        val completed: Boolean,
        val users: List<String> = listOf()  // הוספתי את רשימת המשתמשים
    )

    suspend fun getTasks(token: String): List<Task> {
        println("Sending request to get tasks with token: $token")

        val response: HttpResponse = client.get("http://localhost:8080/api/tasks") {
            headers {
                append(HttpHeaders.Authorization, "Bearer $token")
                accept(ContentType.Application.Json)
            }
        }

        println("Response status: ${response.status}")
        val rawBody = response.bodyAsText()
        println("Raw body:\n$rawBody")

        if (!response.status.isSuccess()) {
            throw RuntimeException("Request failed: ${response.status}, body:\n$rawBody")
        }

        return Json.decodeFromString(rawBody)
    }

    @Serializable
    data class CreateTaskRequest(
        val title: String,
        val description: String? = null,
        val dueDate: String? = null,
        val category: String? = null,
        val completed: Boolean = false,
        val users: List<String> = listOf()  // הוספתי את רשימת המשתמשים כאן גם
    )

    suspend fun addTask(token: String, task: CreateTaskRequest) {
        val response = client.post("http://localhost:8080/api/tasks") {
            contentType(ContentType.Application.Json)
            headers {
                append(HttpHeaders.Authorization, "Bearer $token")
            }
            setBody(task)
        }

        if (!response.status.isSuccess()) {
            val errorBody = response.bodyAsText()
            throw RuntimeException("שמירת המשימה נכשלה: ${response.status}, תגובה: $errorBody")
        }
    }

    // חדש - הוספת מספר משימות במכה
    suspend fun addTasksBatch(token: String, tasks: List<CreateTaskRequest>) {
        val response = client.post("http://localhost:8080/api/tasks/batch") {
            contentType(ContentType.Application.Json)
            headers {
                append(HttpHeaders.Authorization, "Bearer $token")
            }
            setBody(tasks)
        }

        if (!response.status.isSuccess()) {
            val errorBody = response.bodyAsText()
            throw RuntimeException("שמירת המשימות נכשלה (batch): ${response.status}, תגובה: $errorBody")
        }
    }

    // עדכון משימה קיימת
    suspend fun updateTask(token: String, task: Task) {
        val response = client.put("http://localhost:8080/api/tasks/${task.id}") {
            contentType(ContentType.Application.Json)
            headers {
                append(HttpHeaders.Authorization, "Bearer $token")
            }
            setBody(task)
        }

        if (!response.status.isSuccess()) {
            val errorBody = response.bodyAsText()
            throw RuntimeException("עדכון המשימה נכשלה: ${response.status}, תגובה: $errorBody")
        }
    }

    // חדש - עדכון מספר משימות במכה
    suspend fun updateTasksBatch(token: String, tasks: List<Task>) {
        val response = client.put("http://localhost:8080/api/tasks/batch") {
            contentType(ContentType.Application.Json)
            headers {
                append(HttpHeaders.Authorization, "Bearer $token")
            }
            setBody(tasks)
        }

        if (!response.status.isSuccess()) {
            val errorBody = response.bodyAsText()
            throw RuntimeException("עדכון המשימות נכשל (batch): ${response.status}, תגובה: $errorBody")
        }
    }

    suspend fun deleteTask(token: String, taskId: Long) {
        val response = client.delete("http://localhost:8080/api/tasks/$taskId") {
            headers {
                append(HttpHeaders.Authorization, "Bearer $token")
            }
        }

        if (!response.status.isSuccess()) {
            val errorBody = response.bodyAsText()
            throw RuntimeException("מחיקת המשימה נכשלה: ${response.status}, תגובה: $errorBody")
        }
    }

    @Serializable
    data class RegisterRequest(val username: String, val password: String)

    suspend fun register(username: String, password: String): LoginResponse {
        val response: HttpResponse = client.post("http://localhost:8080/api/users/register") {
            contentType(ContentType.Application.Json)
            setBody(RegisterRequest(username, password))
        }

        if (!response.status.isSuccess()) {
            throw RuntimeException("הרשמה נכשלה: ${response.status}")
        }

        return response.body()
    }

    suspend fun addUserToTask(token: String, taskId: Long, usernameToAdd: String) {
        val response = client.post("http://localhost:8080/api/tasks/$taskId/addUser") {
            contentType(ContentType.Application.Json)
            headers {
                append(HttpHeaders.Authorization, "Bearer $token")
            }
            parameter("usernameToAdd", usernameToAdd)
        }

        if (!response.status.isSuccess()) {
            val errorBody = response.bodyAsText()
            throw RuntimeException("הוספת משתמש למשימה נכשלה: ${response.status}, תגובה: $errorBody")
        }
    }
}
