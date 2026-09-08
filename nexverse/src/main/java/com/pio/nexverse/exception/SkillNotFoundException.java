package com.pio.nexverse.exception;

import static com.pio.nexverse.constants.ExceptionMessages.SKILL_NOT_FOUND;

public class SkillNotFoundException extends RuntimeException {
    public SkillNotFoundException() {
        super(SKILL_NOT_FOUND);
    }
}
