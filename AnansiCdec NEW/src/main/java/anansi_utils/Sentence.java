package anansi_utils;

import java.util.*;

public class Sentence {
    public int index;
    public int Begin;
    public int End;
    public String parse;
    public String text;
    public String binaryParse;
    public Dependency[] basicDependencies;
    public Dependency[] enhancedDependencies;
    public Dependency[] enhancedPlusPlusDependencies;
    public int sentimentValue;
    public String sentiment;
    public double[] sentimentDistribution;
    public String sentimentTree;
    public EntityMention[] entityMentions;
    public Token[] tokens;
    public Map<Integer, Dependency> idToDependency;
    public Map<Integer, Set<Integer>> gobToDeps;
    public String sentenceId;



    public Sentence (Token[] tokens, String sentenceId, int Begin, int End,String text){
        this.tokens = tokens;
        this.sentenceId = sentenceId;
        this.Begin = Begin;
        this.End = End;
        this.text = text;
    }

    public List<Token> getDependents(int tokenIndex){
        Set<Token> visitedTokens = new HashSet<Token>();
        visitedTokens.add(tokens[tokenIndex - 1]);
        List<Token> tokenList = new LinkedList<>();
        tokenList.add(tokens[tokenIndex - 1]); //token count starts at 1 in the json file.
        for(int i : gobToDeps.get(tokenIndex))
            recursiveDependents(tokenList, visitedTokens, tokens[i - 1]);

        tokenList.add(tokens[tokenIndex - 1]);
        return tokenList;
    }

    private void recursiveDependents(List<Token> tokenList, Set<Token> visited, Token token){
        if (!visited.contains(token)){
          visited.add(token);
          if(!token.originalText.contains(".") && !token.originalText.contains("!") && !token.originalText.contains(","))
            tokenList.add(token);

          for(int i : gobToDeps.get(token.index))
              recursiveDependents(tokenList, visited, tokens[i - 1]);
        }
    }
    //This function creates a graph of which gob is attached to which dep
    public void initializegobToDeps() {
        gobToDeps = new HashMap<Integer, Set<Integer>>();
        for(Token tok : tokens){
            Set<Integer> list = new HashSet<>();
            gobToDeps.put(tok.index,list);
        }
        for(Dependency dep : enhancedPlusPlusDependencies){
            if(dep.governor == 0)
                continue;//prevent nullpointer exception
            gobToDeps.get(dep.governor).add(dep.dependent);
        }
        idToDependency = new HashMap<Integer, Dependency>();

        for (Dependency dep : enhancedPlusPlusDependencies){
            idToDependency.put(dep.governor,dep);
        }
    }

}
