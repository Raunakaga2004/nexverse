package com.pio.nexverse.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * This enum is used in super admin dashboard to tell what growth period the chart of super admin dashboard show.
 */
@RequiredArgsConstructor
@Getter
public enum GrowthPeriod {
    LAST_12_MONTHS(12),
    LAST_3_YEARS(36),
    LAST_5_YEARS(60);

    private final int months;
}