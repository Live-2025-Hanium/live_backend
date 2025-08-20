package com.example.live_backend.domain.mission.my.Enum;

import java.time.DayOfWeek;

public enum RepeatType {
    EVERYDAY,
    WEEKDAYS,
    WEEKENDS;

    public boolean includes(DayOfWeek day) {
        return switch (this) {
            case EVERYDAY -> true;
            case WEEKDAYS -> day != DayOfWeek.SATURDAY && day != DayOfWeek.SUNDAY;
            case WEEKENDS -> day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY;
        };
    }
}