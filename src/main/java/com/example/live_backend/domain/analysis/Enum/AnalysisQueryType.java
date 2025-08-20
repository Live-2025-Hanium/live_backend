package com.example.live_backend.domain.analysis.Enum;

import com.example.live_backend.global.error.exception.CustomException;
import com.example.live_backend.global.error.exception.ErrorCode;

public enum AnalysisQueryType {

    WEEKLY, DAILY;

    public static AnalysisQueryType from(String value) {
        if (value == null) throw new CustomException(ErrorCode.QUERY_TYPE_REQUIRED);
        switch (value.trim().toUpperCase()) {
            case "WEEKLY": return WEEKLY;
            case "DAILY": return DAILY;
            default: throw new CustomException(ErrorCode.INVALID_QUERY_TYPE);
        }
    }
}
