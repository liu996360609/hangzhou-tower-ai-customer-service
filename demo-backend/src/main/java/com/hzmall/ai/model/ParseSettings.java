package com.hzmall.ai.model;

import lombok.Data;
import java.util.Map;

@Data
public class ParseSettings {
    private String segmentMode; // automatic / custom
    private int segmentLength;
    private int segmentOverlap;
    private String separator;
    private boolean removeExtraWhitespace;
    private boolean removeEmptyLines;
    private Map<String, String> preprocessingRules;

    public static ParseSettings defaultSettings() {
        ParseSettings settings = new ParseSettings();
        settings.setSegmentMode("automatic");
        settings.setSegmentLength(500);
        settings.setSegmentOverlap(50);
        settings.setSeparator("\n");
        settings.setRemoveExtraWhitespace(true);
        settings.setRemoveEmptyLines(true);
        return settings;
    }
}
