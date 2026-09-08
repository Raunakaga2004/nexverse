package com.pio.nexverse.exception;

import static com.pio.nexverse.constants.ExceptionMessages.SKILL_ICON_NOT_FOUND;

public class SkillIconNotFoundException extends RuntimeException {
    public SkillIconNotFoundException() {
        super(SKILL_ICON_NOT_FOUND);
    }
}