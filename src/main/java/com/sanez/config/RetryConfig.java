package com.sanez.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;


@Configuration
@EnableRetry //habilita el soporte para @Retryable en toda la aplicación.
public class RetryConfig {
}