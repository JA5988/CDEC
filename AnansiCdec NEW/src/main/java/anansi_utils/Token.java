/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package anansi_utils;

/**
 *
 * @author chorowitz
 */
public class Token
{
    public int index;
    public String word;
    public String originalText;
    public String lemma;
    public int characterOffsetBegin;
    public int characterOffsetEnd;
    public String pos;
    public String ner;
    public String normalizedNER;
    public String speaker;
    public String before;
    public String after;
    public Timex timex;

    public Token(int index,String originalText, String lemma, String pos) {
        this.index = index;
        this.originalText = originalText;
        this.lemma = lemma;
        this.pos = pos;
    }
}
