package com.telegram.menfess.service;

import lombok.Data;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@Data
public class MenfessDataConfirmation {

    private Map<String, String> data;


}
