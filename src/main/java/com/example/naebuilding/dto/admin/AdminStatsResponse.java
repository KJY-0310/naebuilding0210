package com.example.naebuilding.dto.admin;

import java.util.List;
import java.util.Map;

public record AdminStatsResponse(
        List<MonthlyCountDto> monthly,
        Map<String, Long> status,
        int completionRate,
        int rejectRate
) {}
