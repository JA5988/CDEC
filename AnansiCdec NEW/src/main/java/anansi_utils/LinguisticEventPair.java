package anansi_utils;

import java.util.List;

public class LinguisticEventPair {
    private List<LinguisticEventNode> linguisticPair;
    private double agentCompatibility;
    private double patientCompatibility;
    private double temporalCompatibility;
    private double locationCompatibility;
    private double cdecScore;



    public LinguisticEventPair(List<LinguisticEventNode> linguisticPair) {
        this.linguisticPair = linguisticPair;
    }

    public List<LinguisticEventNode> getLinguisticPair() {
        return linguisticPair;
    }

    public double getCdecScore() {
        return cdecScore;
    }

    public void setCdecScore(double cdecScore) {
        this.cdecScore = cdecScore;
    }

    public void setLinguisticPair(List<LinguisticEventNode> linguisticPair) {
        this.linguisticPair = linguisticPair;
    }

    public double getAgentCompatibility() {
        return agentCompatibility;
    }

    public void setAgentCompatibility(double agentCompatibility) {
        this.agentCompatibility = agentCompatibility;
    }

    public double getPatientCompatibility() {
        return patientCompatibility;
    }

    public void setPatientCompatibility(double patientCompatibility) {
        this.patientCompatibility = patientCompatibility;
    }

    public double getTemporalCompatibility() {
        return temporalCompatibility;
    }

    public void setTemporalCompatibility(double temporalCompatibility) {
        this.temporalCompatibility = temporalCompatibility;
    }

    public double getLocationCompatibility() {
        return locationCompatibility;
    }

    public void setLocationCompatibility(double locationCompatibility) {
        this.locationCompatibility = locationCompatibility;
    }
}
