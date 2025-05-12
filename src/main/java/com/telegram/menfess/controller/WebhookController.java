package com.telegram.menfess.controller;

import com.telegram.menfess.bot.BotGateway;
import org.springframework.web.bind.annotation.*;
import org.telegram.telegrambots.meta.api.objects.Update;


@RestController
public class WebhookController {

    private final BotGateway botGateway;

    public WebhookController(BotGateway botGateway) {
        this.botGateway = botGateway;
    }

    @PostMapping("/webhook")
    public String webhook(@RequestBody Update update) {
        botGateway.process(update);
        return "ok";
    }

    @GetMapping("/health")
    public String health() {
        return "ok";
    }
}