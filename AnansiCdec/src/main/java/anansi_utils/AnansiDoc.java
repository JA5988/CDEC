package anansi_utils;


import java.util.Set;

public class AnansiDoc {

    public Nodes[] nodes;
    public Rels[] rels;
    public Set<LinguisticEventNode> linguisticEvents;

    public Set<LinguisticEventNode> getLinguisticEvents() {
        return linguisticEvents;
    }

    public void setLinguisticEvents(Set<LinguisticEventNode> linguisticEvents) {
        this.linguisticEvents = linguisticEvents;
    }
}
