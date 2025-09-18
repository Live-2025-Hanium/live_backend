package com.example.live_backend.domain.place.mapper;

import com.example.live_backend.domain.place.constant.PlaceCategory;
import org.springframework.stereotype.Component;

@Component
public class PlaceCategoryMapper {
    
    public String toKakaoCategory(String internalCategory) {
        PlaceCategory category = PlaceCategory.fromCode(internalCategory);
        return category != null ? category.getKakaoCode() : null;
    }
    
    public String getCategoryLabel(String categoryCode) {
        PlaceCategory category = PlaceCategory.fromCode(categoryCode);
        return category != null ? category.getLabel() : null;
    }

    public boolean isPsychiatryCategory(String categoryCode) {
        PlaceCategory category = PlaceCategory.fromCode(categoryCode);
        return category != null && category.isPsychiatry();
    }

    public boolean isValidCategory(String categoryCode) {
        return PlaceCategory.isValidCode(categoryCode);
    }
}