package anansi_utils;

import java.util.LinkedList;
import java.util.List;

public class EventPair {
    private EventNode eventNodeOne;
    private EventNode eventNodeTwo;
    public double cdecScore;
    public List<EventNode> getLinguisticPair() {
        return linguisticPair;
    }

    private List<EventNode> linguisticPair = new LinkedList<>();

    public EventPair(EventNode eventNodeOne, EventNode eventNodeTwo) {
        this.eventNodeOne = eventNodeOne;
        this.eventNodeTwo = eventNodeTwo;
        this.linguisticPair.add(this.eventNodeOne);
        this.linguisticPair.add(this.eventNodeTwo);
    }

    public EventNode getEventNodeOne() {
        return this.eventNodeOne;
    }

    public EventNode getEventNodeTwo() {
        return this.eventNodeTwo;
    }

    public void setCdecScore(double cdecScore) {
        this.cdecScore = cdecScore;
    }
}