package com.agent.app.github

import java.net.HttpURLConnection
import java.net.URL

class GitHubClient(
    private val token: String
) {

    private fun request(
        method: String,
        urlString: String
    ): String {
        val connection = URL(urlString).openConnection() as HttpURLConnection

        connection.requestMethod = method
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

        connection.connect()

        val responseCode = connection.responseCode

        val stream = if (responseCode in 200..299) {
            connection.inputStream
        } else {
            connection.errorStream
        }

        val response = stream?.bufferedReader()?.use {
            it.readText()
        } ?: ""

        connection.disconnect()

        if (responseCode !in 200..299) {
            throw Exception(
                "GitHub API error $responseCode: $response"
            )
        }

        return response
    }

    fun getRepository(
        owner: String,
        repository: String
    ): String {
        return request(
            "GET",
            "https://api.github.com/repos/$owner/$repository"
        )
    }
}
