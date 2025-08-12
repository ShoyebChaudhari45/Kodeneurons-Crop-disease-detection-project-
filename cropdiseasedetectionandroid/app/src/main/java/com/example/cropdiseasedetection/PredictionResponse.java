package com.example.cropdiseasedetection;

public class PredictionResponse {
    private String disease;
    private String description;
    private String treatment;
    private String image_url;

    public String getDisease() { return disease; }
    public String getDescription() { return description; }
    public String getTreatment() { return treatment; }
    public String getImage_url() { return image_url; }
}
