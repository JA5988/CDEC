package anansi_utils;


import java.util.LinkedList;
import java.util.List;

public class LinguisticEventNode {


    private int linguisticEventId;
    private static List<Sentence> documentCorpus;
    private Sentence eventSentence;


    private List<Token> mainEvText;
    private List<Token>  Agent;
    private List<Token> LocatedIn;
    private List<Token> OccursOn;
    private List<Token>  Patient;
    private String sentence;
    private Token eventTrigger;


    private String fileName;


    public LinguisticEventNode(int linguisticEventId, Sentence eventSentence, List<Token> mainEvText, String sentence, Token eventTrigger, String fileName) {
        this.linguisticEventId = linguisticEventId;
        this.eventSentence = eventSentence;
        this.mainEvText = mainEvText;
        this.sentence = sentence;
        this.eventTrigger = eventTrigger;
        this.fileName = fileName;
        this.Agent = new LinkedList<Token>();
        this.Patient = new LinkedList<Token>();
        this.LocatedIn = new LinkedList<Token>();
        this.OccursOn = new LinkedList<Token>();
    }

    /* Inputs
        Id of linguistic event in graph
        Id (or index location in sentence) of the predicate node of the event trigger
        Sentence number in the anansi document. This is used to reference correct nlp sentence
        Assumption is that corenlp will provided one sentence at a time.
     */

    public List<Token> getLocatedIn() {
        return this.LocatedIn;
    }

    public List<Token> getOccursOn() {
        return this.OccursOn;
    }

    public String getFileName() {
        return fileName;
    }
    public int getLinguisticEventId() {
        return linguisticEventId;
    }

    public static List<Sentence> getDocumentCorpus() {
        return documentCorpus;
    }

    public Sentence getEventSentence() {
        return eventSentence;
    }

    public List<Token> getMainEvText() {
        return mainEvText;
    }

    public String getSentence() {
        return sentence;
    }

    public Token getEventTrigger() {
        return eventTrigger;
    }

    public void setLinguisticEventId(int linguisticEventId) {
        this.linguisticEventId = linguisticEventId;
    }

    public static void setDocumentCorpus(List<Sentence> documentCorpus) {
        LinguisticEventNode.documentCorpus = documentCorpus;
    }

    public void setEventSentence(Sentence eventSentence) {
        this.eventSentence = eventSentence;
    }

    public void setMainEvText(List<Token> mainEvText) {
        this.mainEvText = mainEvText;
    }

    public void setSentence(String sentence) {
        this.sentence = sentence;
    }

    public void setEventTrigger(Token eventTrigger) {
        this.eventTrigger = eventTrigger;
    }

    public boolean hasAgent(){
        return Agent.size() > 0;
    }

    public boolean hasPatient() {
        return Patient.size() > 0;
    }

    public List<Token> getPatient() {
        return this.Patient;
    }
    public List<Token> getAgent() {
        return this.Agent;
    }

}
