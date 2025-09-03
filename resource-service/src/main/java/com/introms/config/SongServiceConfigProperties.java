package com.introms.config;

import jakarta.validation.constraints.*;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

@Data
@Validated
@ConfigurationProperties(prefix = "song-service")
public class SongServiceConfigProperties {

    @NotNull(message = "Base URL must not be null.")
    @NotBlank(message = "Base URL must not be blank.")
    private String baseUrl;

    @NotNull(message = "Timeout configuration cannot be null.")
    private TimeoutProperties timeout;

    @NotNull(message = "Retry configuration cannot be null.")
    private RetryProperties retry;

    @Data
    public static class TimeoutProperties {
        @NotNull(message = "Connect timeout must not be null.")
        @Positive(message = "Connect timeout must be a positive value.")
        private Duration connect;

        @NotNull(message = "Read timeout must not be null.")
        @Positive(message = "Read timeout must be a positive value.")
        private Duration read;
    }

    @Data
    public static class RetryProperties {
        @Min(value = 1, message = "Retry attempts must be at least 1.")
        private int maxAttempts;

        @NotNull(message = "Backoff configuration cannot be null.")
        private BackoffProperties backoff;

        @Data
        public static class BackoffProperties {
            @NotNull(message = "Initial backoff duration cannot be null.")
            @Positive(message = "Initial backoff duration must be positive.")
            private Duration initial;

            @NotNull(message = "Max backoff duration cannot be null.")
            @Positive(message = "Max backoff duration must be positive.")
            private Duration max;
        }
    }
}