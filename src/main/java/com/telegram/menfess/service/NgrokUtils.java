package com.telegram.menfess.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Service
public class NgrokUtils {
    private static final Logger logger = LoggerFactory.getLogger(NgrokUtils.class);

    private final String botToken;

    private final RestTemplate restTemplate;

    @Value("${ngrok.api.url:http://localhost:4040/api/tunnels}")
    private String ngrokApiUrl;

    public NgrokUtils(@Value("${bot.token}") String botToken) {
        this.restTemplate = new RestTemplate();
        this.botToken = botToken;
    }
    public void setWebhook() {
        try {
            // Attempt to get URL from ngrok
            String tunnelJson = getTunnelInfo();
            if (tunnelJson == null) {
                logger.error("Failed to get tunnel information from ngrok");
                return;
            }

            String publicUrl = extractPublicUrl(tunnelJson);
            if (publicUrl != null) {
                setTelegramWebHook(publicUrl);
            }
        } catch (Exception e) {
            logger.error("Error in setWebhook: {}", e.getMessage(), e);
        }
    }

    private String getTunnelInfo() {
        try {
            return restTemplate.getForObject(ngrokApiUrl, String.class);
        } catch (RestClientException e) {
            logger.error("Failed to connect to ngrok API: {}", e.getMessage());
            try {
                String alternativeUrl = ngrokApiUrl.replace("4040", "3030");
                return restTemplate.getForObject(alternativeUrl, String.class);
            } catch (RestClientException ex) {
                logger.error("Failed to connect to ngrok API on alternative port: {}", ex.getMessage());
                return null;
            }
        }
    }

    private String extractPublicUrl(String tunnelJson) {
        try {
            JsonNode tunnelNode = new ObjectMapper().readTree(tunnelJson);
            JsonNode tunnels = tunnelNode.get("tunnels");

            if (tunnels != null && tunnels.isArray() && !tunnels.isEmpty()) {
                return tunnels.get(0).get("public_url").asText();
            } else {
                logger.error("No tunnels found in ngrok response");
                return null;
            }
        } catch (Exception e) {
            logger.error("Error extracting public URL: {}", e.getMessage());
            return null;
        }
    }

    private void setTelegramWebHook(String publicUrl) {
        try {
            String webHookPath = "/webhook";
            String fullWebHookUrl = publicUrl + webHookPath;
            String setWebHookUrl = String.format("https://api.telegram.org/bot%s/setWebhook?url=%s",
                    botToken, fullWebHookUrl);
            System.out.println(setWebHookUrl);

            String response = restTemplate.getForObject(setWebHookUrl, String.class);
            logger.info("Webhook set response: {}", response);
        } catch (Exception e) {
            logger.error("Error setting Telegram webhook: {}", e.getMessage());
        }
    }
}
