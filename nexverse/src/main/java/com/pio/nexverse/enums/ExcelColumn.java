package com.pio.nexverse.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ExcelColumn {
    FIRST_NAME(0, "first name"),
    LAST_NAME(1, "last name"),
    EMAIL(2, "email"),
    PHONE_NUMBER(3, "phone number"),
    EMPLOYEE_CODE(4, "employee code"),
    JOB_TITLE(5, "job title"),
    DEPARTMENT_NAME(6, "department name");

    private final int columnIndex;
    private final String header;

    public static ExcelColumn formIndex(int index) {
        for (ExcelColumn column : values()) {
            if (column.columnIndex == index) {
                return column;
            }
        }
        throw new IllegalArgumentException("Unknown Column Index : " + index);
    }
}
