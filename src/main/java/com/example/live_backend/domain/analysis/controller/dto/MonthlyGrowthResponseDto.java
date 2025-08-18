package com.example.live_backend.domain.analysis.controller.dto;

import com.example.live_backend.domain.mission.clover.Enum.MissionCategory;
import lombok.Builder;
import lombok.Getter;

import java.time.YearMonth;
import java.util.*;

@Getter
@Builder
public class MonthlyGrowthResponseDto {
    private int previousMonth;
    private int currentMonth;
    private List<GrowthSummary> growthSummary;

    @Getter
    @Builder
    public static class GrowthSummary {
        private int rank;
        private String categoryName;
        private long previousMonthCount;
        private long currentMonthCount;
        private double growthPercentage;
    }

    public static MonthlyGrowthResponseDto from(YearMonth ym,
                                                   Map<MissionCategory, Long> prevMap,
                                                   Map<MissionCategory, Long> currMap) {

        Set<MissionCategory> allCats = new HashSet<>();
        allCats.addAll(prevMap.keySet());
        allCats.addAll(currMap.keySet());

        List<GrowthSummary> list = new ArrayList<>();
        for (MissionCategory cat : allCats) {
            long prev = prevMap.getOrDefault(cat, 0L);
            long curr = currMap.getOrDefault(cat, 0L);

            double pct;

            if (prev == 0L) {
                pct = (curr > 0L) ? 100.0 : 0.0;
            } else {
                pct = ((curr - prev) * 100.0) / prev;
            }
            double rounded = Math.round(pct * 10.0) / 10.0;

            list.add(GrowthSummary.builder()
                    .rank(0)
                    .categoryName(cat.getInKr())
                    .previousMonthCount(prev)
                    .currentMonthCount(curr)
                    .growthPercentage(rounded)
                    .build());
        }

        Collections.sort(list, (a, b) -> {
            int byPct = Double.compare(b.getGrowthPercentage(), a.getGrowthPercentage());
            if (byPct != 0) return byPct;
            long aInc = a.getCurrentMonthCount() - a.getPreviousMonthCount();
            long bInc = b.getCurrentMonthCount() - b.getPreviousMonthCount();
            return Long.compare(bInc, aInc);
        });

        if (list.size() > 3) {
            list = new ArrayList<>(list.subList(0, 3));
        }

        for (int i = 0; i < list.size(); i++) {
            GrowthSummary gs = list.get(i);
            list.set(i, GrowthSummary.builder()
                    .rank(i + 1)
                    .categoryName(gs.getCategoryName())
                    .previousMonthCount(gs.getPreviousMonthCount())
                    .currentMonthCount(gs.getCurrentMonthCount())
                    .growthPercentage(gs.getGrowthPercentage())
                    .build());
        }

        YearMonth prevYm = ym.minusMonths(1);

        return MonthlyGrowthResponseDto.builder()
                .previousMonth(prevYm.getMonthValue())
                .currentMonth(ym.getMonthValue())
                .growthSummary(list)
                .build();
    }
}
