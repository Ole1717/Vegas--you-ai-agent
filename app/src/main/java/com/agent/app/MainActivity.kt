package com.agent.app

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import com.agent.app.github.GitHubClient
import com.agent.app.security.SecureTokenStorage
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private lateinit var tokenStorage: SecureTokenStorage

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        tokenStorage = SecureTokenStorage(this)

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 32, 32, 32)
        }

        val title = TextView(this).apply {
            text = "Vegas Agent"
            textSize = 28f
        }

        val tokenInput = EditText(this).apply {
            hint = "GitHub Token"
            inputType =
                android.text.InputType.TYPE_CLASS_TEXT or
                android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
        }

        val ownerInput = EditText(this).apply {
            hint = "GitHub Username"
        }

        val repositoryInput = EditText(this).apply {
            hint = "Repository"
        }

        val saveButton = Button(this).apply {
            text = "Сохранить GitHub Token"
        }

        val checkButton = Button(this).apply {
            text = "Проверить GitHub"
        }

        val result = TextView(this).apply {
            text = "GitHub не подключён"
            textSize = 16f
        }

        layout.addView(title)
        layout.addView(tokenInput)
        layout.addView(ownerInput)
        layout.addView(repositoryInput)
        layout.addView(saveButton)
        layout.addView(checkButton)
        layout.addView(result)

        setContentView(layout)

        val savedToken = tokenStorage.getGitHubToken()

        if (!savedToken.isNullOrBlank()) {
            tokenInput.setText(savedToken)
            result.text = "Token найден в защищённом хранилище"
        }

        saveButton.setOnClickListener {
            val token = tokenInput.text.toString().trim()

            if (token.isEmpty()) {
                result.text = "Введите GitHub Token"
                return@setOnClickListener
            }

            try {
                tokenStorage.saveGitHubToken(token)
                tokenInput.setText("")
                result.text = "✓ GitHub Token сохранён"
            } catch (e: Exception) {
                result.text =
                    "Ошибка сохранения: ${e.message}"
            }
        }

        checkButton.setOnClickListener {

            val token = tokenStorage.getGitHubToken()

            if (token.isNullOrBlank()) {
                result.text =
                    "Сначала сохраните GitHub Token"
                return@setOnClickListener
            }

            val owner =
                ownerInput.text.toString().trim()

            val repository =
                repositoryInput.text.toString().trim()

            if (owner.isEmpty() || repository.isEmpty()) {
                result.text =
                    "Введите Username и Repository"
                return@setOnClickListener
            }

            result.text = "⟳ Проверяю GitHub..."

            lifecycleScope.launch {
                try {
                    val client =
                        GitHubClient(token)

                    val repo =
                        client.getRepository(
                            owner,
                            repository
                        )

                    result.text =
                        """
                        ✓ GitHub подключён

                        Репозиторий:
                        ${repo.fullName}

                        Ветка:
                        ${repo.defaultBranch}

                        Приватный:
                        ${if (repo.private) "да" else "нет"}
                        """.trimIndent()

                } catch (e: Exception) {

                    result.text =
                        "❌ Ошибка GitHub:\n${e.message}"
                }
            }
        }
    }
}
