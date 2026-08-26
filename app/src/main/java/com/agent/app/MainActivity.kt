package com.agent.app
import com.agent.app.agent.AgentCore
import com.agent.app.agent.AgentRequest
import com.agent.app.files.FileManager

import android.os.Bundle
import android.view.WindowInsets
import android.text.InputType
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import com.agent.app.github.GitHubClient
import com.agent.app.memory.MemoryManager
import com.agent.app.security.SecureTokenStorage
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private lateinit var tokenStorage: SecureTokenStorage
    private lateinit var memory: MemoryManager
    private lateinit var agentCore: AgentCore
    private lateinit var fileManager: FileManager

    private lateinit var chatOutput: TextView
    private lateinit var messageInput: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        tokenStorage = SecureTokenStorage(this)
        memory = MemoryManager(this)
        fileManager = FileManager(filesDir.resolve("projects"))
        agentCore = AgentCore(memory, fileManager)

        buildInterface()
        loadMemory()
    }

    private fun buildInterface() {

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24, 24, 24, 24)
        }

        val title = TextView(this).apply {
            text = "Vegas Agent"
            textSize = 26f
        }

        chatOutput = TextView(this).apply {
            textSize = 16f
            setPadding(0, 24, 0, 24)
        }

        val scroll = ScrollView(this).apply {
            addView(chatOutput)
        }

        val scrollParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            0
        ).apply {
            weight = 1f
        }

        messageInput = EditText(this).apply {
            hint = "Напишите сообщение..."
            inputType = InputType.TYPE_CLASS_TEXT or
                    InputType.TYPE_TEXT_FLAG_MULTI_LINE
            minLines = 1
            maxLines = 5
            isVerticalScrollBarEnabled = true
        }

        val sendButton = Button(this).apply {
            text = "Отправить"
        }

        val githubButton = Button(this).apply {
            text = "GitHub"
        }

        root.addView(title)
        root.addView(scroll, scrollParams)
        root.addView(messageInput)
        root.addView(sendButton)
        root.addView(githubButton)

        setContentView(root)
        root.setOnApplyWindowInsetsListener { view, insets ->
            val ime = insets.getInsets(WindowInsets.Type.ime())
            val system = insets.getInsets(WindowInsets.Type.systemBars())
            view.setPadding(24, 24, 24, maxOf(24, ime.bottom - system.bottom))
            insets
        }

        sendButton.setOnClickListener {
            processWithAgent()
        }

        githubButton.setOnClickListener {
            showGitHubTest()
        }
    }

    private fun processWithAgent() {
        val message = messageInput.text.toString().trim()

        if (message.isEmpty()) return

        messageInput.setText("")

        lifecycleScope.launch {
            val response = agentCore.process(
                AgentRequest(message)
            )

            appendMessage("👤 Ты:\n$message")
            appendMessage("🤖 Vegas:\n$response")
        }
    }

    private fun loadMemory() {

        lifecycleScope.launch {

            val messages =
                memory.getConversationContext(20)

            if (messages.isEmpty()) {
                chatOutput.text =
                    "Память пуста.\n\nНачните новый разговор."
                return@launch
            }

            val text = buildString {

                append("🧠 Последний контекст:\n\n")

                messages.forEach { message ->

                    val icon =
                        if (message.role == "user") {
                            "👤"
                        } else {
                            "🤖"
                        }

                    append(icon)
                    append(" ")
                    append(message.content)
                    append("\n\n")
                }
            }

            chatOutput.text = text
        }
    }

    private fun appendMessage(message: String) {

        chatOutput.append(
            "\n\n$message"
        )
    }

    private fun showGitHubTest() {

        lifecycleScope.launch {

            val token =
                tokenStorage.getGitHubToken()

            if (token.isNullOrBlank()) {

                appendMessage(
                    "❌ GitHub Token не сохранён."
                )

                return@launch
            }

            appendMessage(
                "🐙 GitHub подключён.\n" +
                "Token найден в защищённом хранилище."
            )

            val ownerInput = EditText(this@MainActivity).apply {
                hint = "GitHub Username"
            }

            val repositoryInput = EditText(this@MainActivity).apply {
                hint = "Repository"
            }

            val dialog = android.app.AlertDialog.Builder(
                this@MainActivity
            )
                .setTitle("Проверить репозиторий")
                .setView(
                    LinearLayout(this@MainActivity).apply {
                        orientation = LinearLayout.VERTICAL
                        setPadding(32, 16, 32, 0)
                        addView(ownerInput)
                        addView(repositoryInput)
                    }
                )
                .setPositiveButton("Проверить", null)
                .setNegativeButton("Отмена", null)
                .create()

            dialog.setOnShowListener {

                dialog.getButton(
                    android.app.AlertDialog.BUTTON_POSITIVE
                ).setOnClickListener {

                    val owner =
                        ownerInput.text.toString().trim()

                    val repository =
                        repositoryInput.text.toString().trim()

                    if (
                        owner.isEmpty() ||
                        repository.isEmpty()
                    ) {
                        appendMessage(
                            "❌ Введите Username и Repository."
                        )
                        return@setOnClickListener
                    }

                    appendMessage(
                        "⟳ Проверяю $owner/$repository..."
                    )

                    lifecycleScope.launch {

                        try {

                            val client =
                                GitHubClient(token)

                            val repo =
                                client.getRepository(
                                    owner,
                                    repository
                                )

                            appendMessage(
                                """
                                ✓ Репозиторий найден

                                ${repo.fullName}

                                Ветка: ${repo.defaultBranch}
                                Приватный: ${
                                    if (repo.isPrivate) "да"
                                    else "нет"
                                }
                                """.trimIndent()
                            )

                        } catch (e: Exception) {

                            appendMessage(
                                "❌ GitHub ошибка:\n${e.message}"
                            )
                        }
                    }

                    dialog.dismiss()
                }
            }

            dialog.show()
        }
    }
}
