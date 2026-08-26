package com.agent.app

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.text.InputType
import android.view.Gravity
import android.view.View
import android.view.WindowInsets
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import com.agent.app.agent.AgentCore
import com.agent.app.agent.AgentRequest
import com.agent.app.files.FileManager
import com.agent.app.github.GitHubClient
import com.agent.app.memory.MemoryManager
import com.agent.app.security.SecureTokenStorage
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private lateinit var tokenStorage: SecureTokenStorage
    private lateinit var memory: MemoryManager
    private lateinit var agentCore: AgentCore
    private lateinit var fileManager: FileManager

    private lateinit var chatOutput: LinearLayout
    private lateinit var messageInput: EditText
    private lateinit var scrollView: ScrollView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        tokenStorage = SecureTokenStorage(this)
        memory = MemoryManager(this)
        fileManager = FileManager(filesDir.resolve("projects"))
        agentCore = AgentCore(memory, fileManager)

        buildInterface()
        loadMemory()
    }

    private fun dp(value: Int): Int =
        (value * resources.displayMetrics.density).toInt()

    private fun roundedBackground(
        color: Int,
        radius: Int
    ): GradientDrawable {
        return GradientDrawable().apply {
            setColor(color)
            cornerRadius = dp(radius).toFloat()
        }
    }

    private fun buildInterface() {

        val background = Color.rgb(18, 18, 22)
        val surface = Color.rgb(28, 28, 34)
        val surfaceLight = Color.rgb(38, 38, 46)
        val accent = Color.rgb(80, 125, 255)
        val white = Color.WHITE
        val secondary = Color.rgb(165, 165, 175)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(background)
            setPadding(dp(16), dp(12), dp(16), dp(12))
        }

        // HEADER
        val header = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(4), dp(4), dp(4), dp(14))
        }

        val title = TextView(this).apply {
            text = "🤖 Vegas"
            textSize = 28f
            setTextColor(white)
            setTypeface(null, android.graphics.Typeface.BOLD)
        }

        val status = TextView(this).apply {
            text = "●  Готов к работе"
            textSize = 13f
            setTextColor(Color.rgb(100, 220, 130))
            setPadding(0, dp(3), 0, 0)
        }

        header.addView(title)
        header.addView(status)

        // CHAT
        chatOutput = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, dp(8), 0, dp(16))
        }

        scrollView = ScrollView(this).apply {
            isFillViewport = true
            setBackgroundColor(background)
            addView(chatOutput)
        }

        val chatParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            0
        ).apply {
            weight = 1f
        }

        // INPUT AREA
        val inputContainer = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(10), dp(6), dp(6), dp(6))
            background = roundedBackground(surface, 22)
        }

        messageInput = EditText(this).apply {
            hint = "Напишите Vegas..."
            hintTextColor = Color.rgb(120, 120, 130)
            setTextColor(white)
            textSize = 16f
            background = null
            inputType = InputType.TYPE_CLASS_TEXT or
                    InputType.TYPE_TEXT_FLAG_MULTI_LINE
            minLines = 1
            maxLines = 4
            setPadding(0, 0, 0, 0)
        }

        val inputParams = LinearLayout.LayoutParams(
            0,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            weight = 1f
            marginEnd = dp(8)
        }

        val sendButton = TextView(this).apply {
            text = "➤"
            textSize = 22f
            gravity = Gravity.CENTER
            setTextColor(white)
            background = roundedBackground(accent, 18)
            setPadding(dp(12), dp(8), dp(12), dp(8))
            isClickable = true
            isFocusable = true
        }

        inputContainer.addView(messageInput, inputParams)
        inputContainer.addView(
            sendButton,
            LinearLayout.LayoutParams(dp(52), dp(52))
        )

        // GITHUB
        val githubButton = Button(this).apply {
            text = "🐙  GitHub"
            textSize = 14f
            setTextColor(white)
            background = roundedBackground(surfaceLight, 16)
            setAllCaps(false)
        }

        val githubParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            dp(48)
        ).apply {
            topMargin = dp(8)
        }

        root.addView(
            header,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        )

        root.addView(scrollView, chatParams)
        root.addView(inputContainer)
        root.addView(githubButton, githubParams)

        setContentView(root)

        root.setOnApplyWindowInsetsListener { view, insets ->
            val ime = insets.getInsets(WindowInsets.Type.ime())
            val system = insets.getInsets(WindowInsets.Type.systemBars())

            view.setPadding(
                dp(16),
                dp(12),
                dp(16),
                maxOf(
                    dp(12),
                    ime.bottom - system.bottom + dp(8)
                )
            )

            insets
        }

        sendButton.setOnClickListener {
            processWithAgent()
        }

        messageInput.setOnEditorActionListener { _, _, _ ->
            processWithAgent()
            true
        }

        githubButton.setOnClickListener {
            showGitHubTest()
        }
    }

    private fun processWithAgent() {

        val message = messageInput.text
            .toString()
            .trim()

        if (message.isEmpty()) return

        messageInput.setText("")

        addUserMessage(message)

        lifecycleScope.launch {

            addVegasMessage("Думаю...")

            try {
                val response = agentCore.process(
                    AgentRequest(message)
                )

                removeLastMessage()

                addVegasMessage(response)

            } catch (e: Exception) {

                removeLastMessage()

                addVegasMessage(
                    "Произошла ошибка:\n${e.message ?: "Неизвестная ошибка"}"
                )
            }
        }
    }

    private fun addUserMessage(message: String) {

        val bubble = createBubble(
            "Ты",
            message,
            Color.rgb(45, 85, 170),
            Gravity.END
        )

        chatOutput.addView(bubble)

        scrollToBottom()
    }

    private fun addVegasMessage(message: String) {

        val bubble = createBubble(
            "🤖 Vegas",
            message,
            Color.rgb(38, 38, 46),
            Gravity.START
        )

        chatOutput.addView(bubble)

        scrollToBottom()
    }

    private fun createBubble(
        name: String,
        message: String,
        color: Int,
        gravity: Int
    ): View {

        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(14), dp(10), dp(14), dp(10))
            background = roundedBackground(color, 18)
        }

        val nameView = TextView(this).apply {
            text = name
            textSize = 13f
            setTextColor(Color.rgb(180, 185, 200))
        }

        val messageView = TextView(this).apply {
            text = message
            textSize = 16f
            setTextColor(Color.WHITE)
            setPadding(0, dp(4), 0, 0)
        }

        container.addView(nameView)
        container.addView(messageView)

        val params = LinearLayout.LayoutParams(
            dp(310),
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            gravity = gravity
            topMargin = dp(6)
            bottomMargin = dp(6)
        }

        container.layoutParams = params

        return container
    }

    private fun removeLastMessage() {

        if (chatOutput.childCount > 0) {
            chatOutput.removeViewAt(
                chatOutput.childCount - 1
            )
        }
    }

    private fun scrollToBottom() {

        scrollView.post {
            scrollView.fullScroll(
                ScrollView.FOCUS_DOWN
            )
        }
    }

    private fun loadMemory() {

        lifecycleScope.launch {

            val messages =
                memory.getConversationContext(20)

            if (messages.isEmpty()) {

                addVegasMessage(
                    "Привет 👋\nЯ Vegas. Готов к работе."
                )

                return@launch
            }

            addVegasMessage(
                "Восстановил последний разговор:"
            )

            messages.forEach { message ->

                if (message.role == "user") {
                    addUserMessage(message.content)
                } else {
                    addVegasMessage(message.content)
                }
            }
        }
    }

    private fun appendMessage(message: String) {

        addVegasMessage(message)
    }

    private fun showGitHubTest() {

        lifecycleScope.launch {

            val token =
                tokenStorage.getGitHubToken()

            if (token.isNullOrBlank()) {

                addVegasMessage(
                    "❌ GitHub Token не сохранён."
                )

                return@launch
            }

            addVegasMessage(
                "🐙 GitHub подключён.\nToken найден в защищённом хранилище."
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
                        setPadding(dp(32), dp(16), dp(32), 0)
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
                        addVegasMessage(
                            "❌ Введите Username и Repository."
                        )
                        return@setOnClickListener
                    }

                    addVegasMessage(
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

                            addVegasMessage(
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

                            addVegasMessage(
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
