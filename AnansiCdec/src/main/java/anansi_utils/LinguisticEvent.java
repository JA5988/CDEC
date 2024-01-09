package anansi_utils;


import java.util.List;

public class LinguisticEvent {

    private static String docCorpus;
    private int linguisticEventId;
    private List<Token> mainEvText;
    private String sentence;
    private String eventTrigger;


    public LinguisticEvent(int linguisticEventId, List<Token> mainEvText, String sentence, String eventTrigger) {
        this.linguisticEventId = linguisticEventId;
        this.mainEvText = mainEvText;
        this.sentence = sentence;
        this.eventTrigger = eventTrigger;
    }

    public int getLinguisticEventId() {
        return linguisticEventId;
    }

    public static String getDocCorpus() {
        return docCorpus;
    }

    public List<Token> getMainEvText() {
        return mainEvText;
    }

    public String getSentence() {
        return sentence;
    }

    public String getEventTrigger() {
        return eventTrigger;
    }


}
