/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package anansi_utils;

import java.util.Map;

/**
 *
 * @author chorowitz
 */
public class EntityMention
{
    public int docTokenBegin;
    public int docTokenEnd;
    public int tokenBegin;
    public int tokenEnd;
    public String text;
    public int characterOffsetBegin;
    public int characterOffsetEnd;
    public String ner;
    public String normalizedNER;
    public Map<String, Double> nerConfidences;
    public Timex timex;
}
