package com.telegram.menfess.service;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class ConfigWebhook implements ApplicationRunner {
    private final NgrokUtils ngrokUtils;

    public ConfigWebhook(NgrokUtils ngrokUtils) {
        this.ngrokUtils = ngrokUtils;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        ngrokUtils.setWebhook();
    }
}
