package com.agent.app.github

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class GitHubClient(
    private val token: String
) {

    suspend fun getRepository(
        owner: String,
        repository: String
    ): GitHubRepository = withContext(Dispatchers.IO) {

        val url = URL(
            "https://api.github.com/repos/$owner/$repository"
        )

        val connection =
            url.openConnection() as HttpURLConnection

        try {
            connection.requestMethod = "GET"

            connection.setRequestProperty(
                "Authorization",
                "Bearer $token"
            )

            connection.setRequestProperty(
                "Accept",
                "application/vnd.github+json"
            )

            connection.setRequestProperty(
                "X-GitHub-Api-Version",
                "2022-11-28"
            )

            connection.connectTimeout = 15_000
            connection.readTimeout = 15_000

            val responseCode = connection.responseCode

            val stream =
                if (responseCode in 200..299) {
                    connection.inputStream
                } else {
                    connection.errorStream
                }

            val response = stream
                ?.bufferedReader()
                ?.use { it.readText() }
                ?: ""

            if (responseCode !in 200..299) {
                throw GitHubException(
                    responseCode,
                    response
                )
            }

            val json = JSONObject(response)

            GitHubRepository(
                name = json.optString("name"),
                fullName = json.optString("full_name"),
                description = json.optString("description")
                    .takeIf { it.isNotBlank() },
                isPrivate = json.optBoolean("private"),
                defaultBranch = json.optString(
                    "default_branch",
                    "main"
                )
            )

        } finally {
            connection.disconnect()
        }
    }
}

class GitHubException(
    val code: Int,
    val response: String
) : Exception(
    "GitHub API error $code: $response"
)
