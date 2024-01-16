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


public class coreNLP_FullFile {
	static String projectPath = System.getProperty("user.dir");
	static File projectDir = new File(projectPath);
	static ArrayList<EventNode> eventNodesFinal = new ArrayList<>();
	static ArrayList<CoreSentence> coreSentenceList = new ArrayList<>();
	static HashSet<String> auxiliaryVerbsSet = new HashSet<String>(Arrays.asList("am", "is", "are", "was", "were", "being", "been", "have", "has", "had", "can", "could", "may", "might", "must", "shall", "should", "will", "would", "do", "does", "did"));
	
	//private static File folderName = new File(projectDir.toString() + "/eng_scenes/export");
	//private static File[] files = folderName.listFiles();
	
	
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
	
	
	// Added fileProcessor method for iterating through the files
	public static void fileProcessor() throws IOException {
		System.out.println(projectDir.toString());
		
		//File folderName = new File(projectDir.toString() + "/eng_scenes/export");
		
		//SEP 6th: Modification to split the files
		File folderName = new File(projectDir.toString() + "/eng_scenes - Copy/export");
		
		
		System.out.println(folderName);
		
		File[] files = folderName.listFiles();

		if (files != null) {
			for (File file : files) {
				try {
					String strippedFileName = file.getName();
					// strippedFileName = strippedFileName.replace(".tml", ".txt");
					
					
					//AUG 28TH: MODIFY THE FILE PATH
					//File outputFile = new File("D:\\Cognac Lab\\Project - Scene Ordering\\NEW_output_eng_scenes\\NEW_output_eng_scenes" + strippedFileName);
					File outputFile = new File("C:\\Users\\jalon\\OneDrive\\Desktop\\Cognac Laboratory\\Scene Ordering\\NEW_NEW_output_eng_scenes" + strippedFileName);
					
					
					//File outputFile = new File("C:\\Users\\jalon\\Desktop\\Cognac Laboratory\\Scene Ordering\\NEW_NEW_output_eng_scenes" + strippedFileName);
					if (outputFile.createNewFile()) {
						System.out.println("File created: " + outputFile.getName());
						Scanner reader = new Scanner(file);
						FileWriter writer = new FileWriter(outputFile);
						writeToFile(reader, writer, file);
						writer.close(); 
						reader.close();
					} else {
						System.out.println("File already exists.");

					}
				} catch (IOException e) {
					System.out.println("An error with the file creating?");
					e.printStackTrace();
				}
			}

		}
	}

	public static void writeToFile(Scanner reader, FileWriter writer, File file)throws IOException{
		//Map<String, List<String>> verbChildren = new HashMap<>(); 
		
		StanfordCoreNLP pipeline;
    	// Set up pipeline properties
    	Properties props = new Properties();
    	props.setProperty("annotators", "tokenize,ssplit,pos,lemma,ner,parse");
    	// Initialize StanfordCoreNLP with the properties
    	pipeline = new StanfordCoreNLP(props);

    		while(reader.hasNextLine()) {
        		String data = reader.nextLine();
        		CoreDocument coreDocument = new CoreDocument(data);
        		pipeline.annotate(coreDocument);
        		
        		//		MODIFY FROM THIS POINT FORWARD. GRAB CHILDREN.
        		List<String> events = new ArrayList<>();
        		Map<String, List<String>> verbChildren = new HashMap<>(); 
        		
        		for(CoreSentence sentence: coreDocument.sentences()) {
        			SemanticGraph dependencyParse = sentence.dependencyParse();
        			//System.out.println(sentence);
        			for(IndexedWord word: dependencyParse.vertexSet()) {
        				if(word.tag().startsWith("VB")) {
        					if(!auxiliaryVerbsSet.contains(word.word().toLowerCase())) {
                           		String event = word.word();
                                events.add(event);
                            
       
                                List<String> children = new ArrayList<>();
                                Set<IndexedWord> allChildren = dependencyParse.getChildren(word);
        					
                                Set<IndexedWord> descendants = new HashSet<>();
        					
                                for(IndexedWord child: new ArrayList<>(allChildren)) {
        						//Converting from set to List 08/28
        						//Set<IndexedWord> descendants = new HashSet<>();
                                	descendants = getDescendants(child, dependencyParse, descendants);
                                	for(IndexedWord descendant: descendants) {
                                		children.add(descendant.word());
                                	}
                                }
        					
                                List<IndexedWord> list = new ArrayList<>(descendants);
        					
                                verbChildren.put(event, children);
        					//Passing the descendants, the sentence itself, and the event
                                EventNode eventNode = new EventNode(list, sentence, event); 
        					// ^ Place all of these in a global variable list, every time one of these get created you add it to the list
                                eventNodesFinal.add(eventNode);
        					
                                System.out.println(eventNodesFinal);
        					
        				}
        			}
        			
        		}
        		coreSentenceList.add(sentence);  	
        		
        	}
        		
        		for (String event : events) {
                	System.out.println("NEW EVENT:");
                    System.out.println(event + "\n");
                    
                    List<String> dependencies = verbChildren.get(event);
                    
                    if(dependencies != null) {
                    	System.out.println("DEPENDENCIES");
                    	for(String dependency : dependencies) {
                    		System.out.println(dependency);
                    	}
                    	System.out.println();
                    }
        		}
        		/*
        		//Commenting out because don't want to create files?
        		// Print out events and their children
        		writer.write("Events:\n");
        		for (String event : events) {
        			writer.write(event + "\n");
        		}

        		writer.write("\nChildren of each event:\n");
        		for(String event: events) {
        			List<String> dependencies = verbChildren.get(event);
            		if(dependencies != null) {
            			for(String dependency: dependencies) {
            				writer.write(dependency + "\n");
            			}
            		}
        			
        		}
        		
        		*/
        	}
    		
    		
    	
    	
    }
    
	
	
	public static void textProcessor(String sentenceText) {
		//String text = "Rosemarie von Salten was here in the company of her mother, and Fred Rittner was in the company of his brother, who was several years older than him and with whom he had undertaken a major trip. He wanted to recover from the strain of his doctoral exam, and his brother Magnus had taken some time off from his business to be able to accompany him. The brothers loved each other very much. Magnus Rittner had become head of the large Rittner paint works after the death of his father, and Fred had studied chemistry in order to be able to devote his energy equally to the work. Although the brothers loved each other warmly, they were of very different characters. Magnus was a serious, reliable man who made high demands on himself and others. Fred Rittner was more light-blooded, he loved the joyful enjoyment of life, unscrupulously took all the good and pleasant things that life offered him, without being particularly concerned with serious duties. More amiable than his serious brother, all hearts flew to him, and he accepted it with a sunny victoriousness, as if it could not be otherwise. Rosemarie von Salten, with fine instinct, had immediately recognized in Magnus Rittner the more valuable. She looked up to him in shy admiration, but did not dare to believe that he could ever have any interest in her. At first, of course, he met her with a strangely gentle courtesy that touched her strangely. But then the closer she got to know him, the more reserved he became toward her. Fred Rittner was at first dear and sympathetic to her only because he was Magnus' brother. In order to be able to stay near him a lot, she put up with Fred's company.";
        //String text = "Rosemarie von Salten was here in the company of her mother, and Fred Rittner was in the company of his brother, who was several years older than him and with whom he had undertaken a major trip. He wanted to recover from the strain of his doctoral exam, and his brother Magnus had taken some time off from his business to be able to accompany him. The brothers loved each other very much. Magnus Rittner had become head of the large Rittner paint works after the death of his father, and Fred had studied chemistry in order to be able to devote his energy equally to the work. Although the brothers loved each other warmly, they were of very different characters. Magnus was a serious, reliable man who made high demands on himself and others. Fred Rittner was more light-blooded, he loved the joyful enjoyment of life, unscrupulously took all the good and pleasant things that life offered him, without being particularly concerned with serious duties.";
        
        StanfordCoreNLP pipeline;

        // Set up pipeline properties
        Properties props = new Properties();
        props.setProperty("annotators", "tokenize,ssplit,pos,lemma,ner,parse");
        // Initialize StanfordCoreNLP with the properties
        pipeline = new StanfordCoreNLP(props);
        CoreDocument doc = new CoreDocument(sentenceText);
        pipeline.annotate(doc);

        // A list to store the detected verbs (events)
        List<String> events = new ArrayList<>();
        // A map to store the children of each verb
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

        for (String event : events) {
        	System.out.println("NEW EVENT:");
            System.out.println(event + "\n");
            
            List<String> dependencies = verbChildren.get(event);
            
            if(dependencies != null) {
            	System.out.println("DEPENDENCIES");
            	for(String dependency : dependencies) {
            		System.out.println(dependency);
            	}
            	System.out.println();
 	
            }
        }
    }
	
    
	public static ArrayList<EventNode> initializer() throws IOException  { //Return ArrayList
		fileProcessor();
		System.out.println("We are finished :) Sort of");
		return eventNodesFinal;
		//System.out.println(projectDir);
		
	}
}

