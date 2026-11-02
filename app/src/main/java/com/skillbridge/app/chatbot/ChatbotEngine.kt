package com.skillbridge.app.chatbot

import android.content.Context
import com.google.gson.Gson

/**
 * Offline "pretend" chatbot. All answers live in `assets/chatbot/answers.json`; this class only
 * scores the user's message against each entry's keywords/phrases and returns the best answer.
 * No network, no AI service - swap [reply] for a real model later without touching the UI.
 */
class ChatbotEngine(private val kb: KnowledgeBase) {

    data class Entry(
        val id: String = "",
        val q: List<String> = emptyList(),
        val a: String = "",
        val f: List<String> = emptyList(),
        val go: String? = null,
        val role: String? = null
    )

    data class Suggestions(val student: List<String> = emptyList(), val client: List<String> = emptyList())

    data class KnowledgeBase(
        val botName: String = "Bridgey",
        val welcome: String = "",
        val fallbacks: List<String> = emptyList(),
        val suggestions: Suggestions = Suggestions(),
        val entries: List<Entry> = emptyList()
    )

    /** [action] is a screen hint ("nearby", "explore", ...) the UI can turn into a button. */
    data class Reply(
        val text: String,
        val followUps: List<String> = emptyList(),
        val action: String? = null,
        val entryId: String? = null
    )

    val botName get() = kb.botName
    fun suggestions(isClient: Boolean) = if (isClient) kb.suggestions.client else kb.suggestions.student
    fun welcome(name: String) = fill(kb.welcome, name)

    private val indexed = kb.entries.map { e -> e to e.q.map { normalize(it) }.filter { it.isNotEmpty() } }
    private var fallbackIdx = 0

    fun reply(message: String, isClient: Boolean, name: String): Reply {
        val padded = " ${normalize(message)} "
        val tokens = padded.trim().split(' ').filter { it.length >= 4 }

        var best: Entry? = null
        var bestScore = 0.0
        for ((entry, keys) in indexed) {
            // Client-only entries are hidden from students; students-only wording is fine for clients.
            if (entry.role == "client" && !isClient) continue
            var score = 0.0
            for (k in keys) {
                if (padded.contains(" $k ")) {
                    val words = k.count { it == ' ' } + 1
                    score += words * 3.0 + k.length / 4.0
                    if (padded.trim() == k) score += 4.0
                } else if (!k.contains(' ') && k.length >= 5) {
                    // loose single-word match: "applications" ~ "application", "projects" ~ "project"
                    if (tokens.any { t -> t.length >= 5 && (t.startsWith(k.dropLast(1)) || k.startsWith(t.dropLast(1))) }) score += 1.5
                }
            }
            if (score > 0 && entry.role == "client" && isClient) score += 2.0
            if (score > bestScore) { bestScore = score; best = entry }
        }

        val hit = best
        if (hit == null || bestScore < 2.0) {
            val fb = kb.fallbacks.getOrElse(fallbackIdx++ % kb.fallbacks.size.coerceAtLeast(1)) { "Sorry, I don't know that yet." }
            return Reply(fb, suggestions(isClient).take(3))
        }
        return Reply(fill(hit.a, name), hit.f, hit.go, hit.id)
    }

    private fun fill(text: String, name: String) = text.replace("{name}", name.ifBlank { "there" })

    companion object {
        fun normalize(s: String): String =
            s.lowercase().replace("'", "").replace("’", "")
                .replace(Regex("[^a-z0-9₹ ]+"), " ").replace(Regex("\\s+"), " ").trim()

        fun parse(json: String): ChatbotEngine = ChatbotEngine(Gson().fromJson(json, KnowledgeBase::class.java))

        @Volatile private var cached: ChatbotEngine? = null

        fun get(context: Context): ChatbotEngine = cached ?: synchronized(this) {
            cached ?: parse(
                context.applicationContext.assets.open("chatbot/answers.json").bufferedReader().use { it.readText() }
            ).also { cached = it }
        }
    }
}
