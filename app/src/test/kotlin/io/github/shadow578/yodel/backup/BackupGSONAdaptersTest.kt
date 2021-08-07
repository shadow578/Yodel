package io.github.shadow578.yodel.backup

import com.google.gson.GsonBuilder
import io.kotest.assertions.withClue
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldBeEqualIgnoringCase
import io.kotest.matchers.string.shouldNotBeBlank
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * [LocalDateTimeAdapter]
 * [LocalDateAdapter]
 */
class BackupGSONAdaptersTest {
    /**
     * [LocalDateTimeAdapter]
     */
    @Test
    fun shouldSerializeLocalDateTime() {
        val gson = GsonBuilder()
                .registerTypeAdapter(LocalDateTime::class.java, LocalDateTimeAdapter())
                .create()

        withClue("non- null value")
        {
            val originalValue = TestDateTime(LocalDateTime.of(2021, 8, 7, 13, 0, 0))
            val json = gson.toJson(originalValue)
            json.shouldNotBeNull()
            json.shouldNotBeBlank()

            gson.fromJson(json, TestDateTime::class.java) shouldBe originalValue
        }

        withClue("serialize null value")
        {
            val originalValue = TestDateTime(null)
            val json = gson.toJson(originalValue)
            json.shouldNotBeNull()

            json.replace(" ", "") shouldBeEqualIgnoringCase """{}"""
        }

        withClue("deserialize null value")
        {
            gson.fromJson("""{ "value": null }""", TestDateTime::class.java) shouldBe TestDateTime(null)
        }
    }

    /**
     * [LocalDateAdapter]
     */
    @Test
    fun shouldSerializeLocalDate() {
        val gson = GsonBuilder()
                .registerTypeAdapter(LocalDate::class.java, LocalDateAdapter())
                .create()

        withClue("non- null value")
        {
            val originalValue = TestDate(LocalDate.of(2021, 8, 7))
            val json = gson.toJson(originalValue)
            json.shouldNotBeNull()
            json.shouldNotBeBlank()

            gson.fromJson(json, TestDate::class.java) shouldBe originalValue
        }

        withClue("serialize null value")
        {
            val originalValue = TestDate(null)
            val json = gson.toJson(originalValue)
            json.shouldNotBeNull()

            json.replace(" ", "") shouldBeEqualIgnoringCase """{}"""
        }

        withClue("deserialize null value")
        {
            gson.fromJson("""{ "value": null }""", TestDate::class.java) shouldBe TestDate(null)
        }
    }

    data class TestDateTime(
            val value: LocalDateTime?
    )

    data class TestDate(
            val value: LocalDate?
    )
}