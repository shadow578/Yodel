package io.github.shadow578.music_dl.backup;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import io.github.shadow578.music_dl.KtPorted;

/**
 * gson adapters for backup data
 */
@KtPorted
public class BackupGSONAdapters {

    @KtPorted
    public static class LocalDateTimeAdapter extends TypeAdapter<LocalDateTime> {

        private static final DateTimeFormatter FORMAT = DateTimeFormatter.ISO_DATE_TIME;

        @Override
        public void write(JsonWriter out, LocalDateTime value) throws IOException {
            if (value == null) {
                out.nullValue();
            } else {
                out.value(FORMAT.format(value));
            }
        }

        @Override
        public LocalDateTime read(JsonReader in) throws IOException {
            if (in.peek().equals(JsonToken.NULL)) {
                in.nextNull();
                return null;
            } else {
                return LocalDateTime.parse(in.nextString(), FORMAT);
            }
        }
    }

    @KtPorted
    public static class LocalDateAdapter extends TypeAdapter<LocalDate> {

        private static final DateTimeFormatter FORMAT = DateTimeFormatter.ISO_DATE;

        @Override
        public void write(JsonWriter out, LocalDate value) throws IOException {
            if (value == null) {
                out.nullValue();
            } else {
                out.value(FORMAT.format(value));
            }
        }

        @Override
        public LocalDate read(JsonReader in) throws IOException {
            if (in.peek().equals(JsonToken.NULL)) {
                in.nextNull();
                return null;
            } else {
                return LocalDate.parse(in.nextString(), FORMAT);
            }
        }
    }
}
