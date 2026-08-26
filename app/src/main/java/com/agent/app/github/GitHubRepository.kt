package com.agent.app.github

data class GitHubRepository(
    val name: String,
    val fullName: String,
    val description: String?,
    val private: Boolean,
    val defaultBranch: String
)
