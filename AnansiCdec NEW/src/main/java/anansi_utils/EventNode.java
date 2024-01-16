package anansi_utils;

import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.pipeline.CoreSentence;

import java.util.List;

public class EventNode {

    private List<IndexedWord> indexedWords;
    private CoreSentence coreSentence;



    // ... any other properties you might need
    private String eventHeadWord;
    public EventNode( List<IndexedWord> indexedWords, CoreSentence coreSentence, String eventHeadWord) {

        this.indexedWords = indexedWords;
        this.coreSentence = coreSentence;
        this.eventHeadWord = eventHeadWord;
        // Initialize any other properties you might need
    }

    public List<IndexedWord> getIndexedWords() {
        return this.indexedWords;
    }

    public CoreSentence getCoreSentence() {
        return this.coreSentence;
    }
    public String getEventHeadWord() {
        return eventHeadWord;
    }
    // ... any other methods you might need
}