import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.pipeline.*;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations;
import edu.stanford.nlp.simple.*;
import edu.stanford.nlp.trees.GrammaticalRelation;
import edu.stanford.nlp.util.CoreMap;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

import anansi_utils.EventNode;



public class CdecSplitFiles {
	static String projectPath = System.getProperty("user.dir");
	static File projectDir = new File(projectPath);
	static ArrayList<EventNode> eventNodesFinal = new ArrayList<>();
	static ArrayList<CoreSentence> coreSentenceList = new ArrayList<>();
	
	public static Set<IndexedWord> getDescendants(IndexedWord word, SemanticGraph depParse, Set<IndexedWord> descendants){
		if (!depParse.getChildren(word).isEmpty()) {
			for(IndexedWord wordChild : depParse.getChildren(word)) {
				if(!descendants.contains(wordChild)) {
					descendants.add(wordChild);
					getDescendants(wordChild, depParse, descendants);
				}
			}
		}
		return descendants;
	}
	
	public static void fileProcessor() throws IOException {
		
		System.out.println(projectDir.toString());
		//Directed towards the specific folder created after splitting the scenes. Needs to be changed to a full eng_scenes folder
		File folderName = new File(projectDir.toString() + "/eng_scenes - Copy/export");
		System.out.println(folderName);
		File[] files = folderName.listFiles();
		
		try {
			if(files != null) {
				for(File file : files) {
					Scanner reader = new Scanner(file);
					textProcessor(reader, file);
				}
			}
		}catch (IOException e) {	
	}
		
}
	
	public static void textProcessor(Scanner reader, File file) {
        StanfordCoreNLP pipeline;

        // Set up pipeline properties
        Properties props = new Properties();
        props.setProperty("annotators", "tokenize,ssplit,pos,lemma,ner,parse");
        // Initialize StanfordCoreNLP with the properties
        pipeline = new StanfordCoreNLP(props);
        
        
        while(reader.hasNextLine()) {
        	String data = reader.nextLine();
        	CoreDocument doc = new CoreDocument(data);
            pipeline.annotate(doc);
            
            List<String> events = new ArrayList<>();
            Map<String, List<String>> verbChildren = new HashMap<>();
            
            for (CoreSentence sentence : doc.sentences()) {
                SemanticGraph dependencyParse = sentence.dependencyParse();
                
                for (IndexedWord word : dependencyParse.vertexSet()) {
                    if (word.tag().startsWith("VB")){ 
                    	String event = word.word();
                        events.add(event);
                        
                        List<String> children = new ArrayList<>();
                        Set<IndexedWord> allChildren = dependencyParse.getChildren(word);
                        
                        Set<IndexedWord> descendants = new HashSet<>();
                        
                        for (IndexedWord child : new ArrayList<>(allChildren)) {  
                           descendants = getDescendants(child, dependencyParse, descendants);
                           for(IndexedWord descendant : descendants) {
                        	   children.add(descendant.word());
                           }
                        }
                        List<IndexedWord> list = new ArrayList<>(descendants);
                        
                        verbChildren.put(event, children);
                        
                        EventNode eventNode = new EventNode(list, sentence, event); 
    					eventNodesFinal.add(eventNode);
    					
    					System.out.println(eventNodesFinal);
                    }
                }
                
                coreSentenceList.add(sentence);
            }
        }
    }
	
	public static ArrayList<CoreSentence> getCoreSentenceList(){
		return coreSentenceList;
	}
	
	public static ArrayList<EventNode> cdecSplitFilesInitializer() throws IOException{
		fileProcessor();
		return eventNodesFinal;
	}
	
}
