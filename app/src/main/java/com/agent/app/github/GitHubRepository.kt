package com.agent.app.github

data class GitHubRepository(
    val name: String,
    val fullName: String,
    val description: String?,
    val isPrivate: Boolean,
    val defaultBranch: String
)
