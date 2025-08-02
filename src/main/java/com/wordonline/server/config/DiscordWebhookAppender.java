package com.wordonline.server.config;

import ch.qos.logback.core.AppenderBase;
import ch.qos.logback.classic.spi.ILoggingEvent;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;

import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class DiscordWebhookAppender extends AppenderBase<ILoggingEvent> {

    @Setter
    private String webhookUrl;

    @Override
    protected void append(ILoggingEvent eventObject) {
        try {
            String message = eventObject.getFormattedMessage();

            URL url = new URL(webhookUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            String payload = "{ \"content\": \"" + escapeJson(message) + "\" }";

            try (OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream())) {
                writer.write(payload);
            }

            int responseCode = conn.getResponseCode();
            if (responseCode != 204) {
                System.err.println("Discord Webhook Error: HTTP " + responseCode);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String escapeJson(String text) {
        return text.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n");
    }
}
