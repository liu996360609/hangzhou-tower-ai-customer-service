package com.hzmall.ai.model;

import lombok.Data;

@Data
public class RetrieveResult {
    private String question;
    private String answer;
    private double score;
    private String source;
    private String category;

    public RetrieveResult() {}

    public RetrieveResult(String question, String answer, double score, String source, String category) {
        this.question = question;
        this.answer = answer;
        this.score = score;
        this.source = source;
        this.category = category;
    }
}
