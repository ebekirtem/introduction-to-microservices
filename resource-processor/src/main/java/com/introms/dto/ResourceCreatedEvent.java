package com.introms.dto;

import java.io.Serializable;

public record ResourceCreatedEvent(Integer resourceId) implements Serializable {
}
