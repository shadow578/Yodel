package io.github.shadow578.yodel.backup

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter
import java.io.IOException
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * gson adapter for [LocalDateTime]
 */
class LocalDateTimeAdapter : TypeAdapter<LocalDateTime?>() {
    @Throws(IOException::class)
    override fun write(writer: JsonWriter, value: LocalDateTime?) {
        if (value == null) {
            writer.nullValue()
        } else {
            writer.value(FORMAT.format(value))
        }
    }

    @Throws(IOException::class)
    override fun read(reader: JsonReader): LocalDateTime? {
        return if (reader.peek() == JsonToken.NULL) {
            reader.nextNull()
            null
        } else {
            LocalDateTime.parse(reader.nextString(), FORMAT)
        }
    }

    companion object {
        private val FORMAT = DateTimeFormatter.ISO_DATE_TIME
    }
}

/**
 * gson adapter for [LocalDate]
 */
class LocalDateAdapter : TypeAdapter<LocalDate?>() {
    @Throws(IOException::class)
    override fun write(writer: JsonWriter, value: LocalDate?) {
        if (value == null) {
            writer.nullValue()
        } else {
            writer.value(FORMAT.format(value))
        }
    }

    @Throws(IOException::class)
    override fun read(reader: JsonReader): LocalDate? {
        return if (reader.peek() == JsonToken.NULL) {
            reader.nextNull()
            null
        } else {
            LocalDate.parse(reader.nextString(), FORMAT)
        }
    }

    companion object {
        private val FORMAT = DateTimeFormatter.ISO_DATE
    }
}