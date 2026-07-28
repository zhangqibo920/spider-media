package com.spider.media.hotmonitor.ai.dto;

public class AnalysisResult {

    private int relevance;
    private String summary;
    private int importance;
    private String isFake;

    public int getRelevance() { return relevance; }
    public void setRelevance(int relevance) { this.relevance = relevance; }

    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }

    public int getImportance() { return importance; }
    public void setImportance(int importance) { this.importance = importance; }

    public String getIsFake() { return isFake; }
    public void setIsFake(String isFake) { this.isFake = isFake; }
}
