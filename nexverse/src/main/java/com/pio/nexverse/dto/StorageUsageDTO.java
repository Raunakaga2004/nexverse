package com.pio.nexverse.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StorageUsageDTO {
    private long usedStorage;
    private long totalStorage;
}