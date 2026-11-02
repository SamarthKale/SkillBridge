package com.skillbridge.app.chatbot

import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ChatbotEngineTest {
    private val engine = ChatbotEngine.parse(File("src/main/assets/chatbot/answers.json").readText())

    private fun id(q: String, client: Boolean = false) = engine.reply(q, client, "Aarav").entryId

    @Test fun routesCommonQuestions() {
        assertEquals("nearby", id("How do I find jobs near me?"))
        assertEquals("apply", id("how do i apply"))
        assertEquals("status-overview", id("What do the statuses mean?"))
        assertEquals("status-shortlisted", id("I got shortlisted!"))
        assertEquals("messaging-locked", id("why can't I message the client"))
        assertEquals("scam", id("Is it safe to work with clients?"))
        assertEquals("greeting", id("hi"))
        assertEquals("thanks", id("thanks!"))
    }

    @Test fun clientQuestionsOnlyForClients() {
        assertEquals("c-post-project", id("How do I post a project?", client = true))
        assertTrue(id("How do I post a project?", client = false) != "c-post-project")
    }

    @Test fun everySuggestionAndFollowUpGetsRealAnswer() {
        val all = engine.suggestions(false).map { it to false } + engine.suggestions(true).map { it to true } +
            engine.javaClass.getDeclaredField("indexed").let { f ->
                f.isAccessible = true
                @Suppress("UNCHECKED_CAST")
                (f.get(engine) as List<Pair<ChatbotEngine.Entry, List<String>>>).flatMap { (e, _) -> e.f.map { it to (e.role == "client") } }
            }
        assertTrue(all.size > 10)
        all.forEach { (q, client) -> assertNotNull("No answer for '$q'", id(q, client)) }
    }

    @Test fun unknownGetsFallback() {
        val r = engine.reply("zxqv blorp wibble", false, "A")
        assertEquals(null, r.entryId)
        assertTrue(r.text.isNotBlank())
    }

    @Test fun fillsName() {
        assertTrue(engine.welcome("Aarav").contains("Aarav"))
    }
}
