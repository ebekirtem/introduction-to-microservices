package com.introms.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix="s3")
@Data
@Validated
public class S3ConfigProperties {
    private String region;
    private String endpoint;
    private String bucketName;
}
