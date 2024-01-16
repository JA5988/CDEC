/*
 * Includes both full file processing and single text/String processing methods. 
 */

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
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.List;
import java.util.*;

import anansi_utils.EventNode;

public class FullFileCoreNLP {
	static String projectPath = System.getProperty("user.dir");
	static File projectDir = new File(projectPath);
	static ArrayList<EventNode> eventNodesFinal = new ArrayList<>();
	static ArrayList<CoreSentence> coreSentenceList = new ArrayList<>();
	
	//static String[] auxiliaryVerbs = {"am", "is", "are", "was", "were", "being", "been", "have", "has", "had", "can", "could", "may", "might", "must", "shall", "should", "will", "would", "do", "does", "did"};
	static HashSet<String> auxiliaryVerbsSet = new HashSet<String>(Arrays.asList("am", "is", "are", "was", "were", "being", "been", "have", "has", "had", "can", "could", "may", "might", "must", "shall", "should", "will", "would", "do", "does", "did"));
	
	//Oct Modification:
	//Q: Could I just make this an indexedWord? What's the difference if I make it an arraylist of indexedWords?
	static ArrayList<String> eventPreAnnotated = new ArrayList<>();
	
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
		File folderName = new File(projectDir.toString() + "/eng_scenes - Copy/export");
		File[] files = folderName.listFiles();
		if (files != null) {
			for (File file : files) {
				try {
					String strippedFileName = file.getName();
					// strippedFileName = strippedFileName.replace(".tml", ".txt");
					
					//File outputFile = new File("D:\\Cognac Lab\\Project - Scene Ordering\\NEW_output_eng_scenes\\NEW_output_eng_scenes" + strippedFileName);
					//File outputFile = new File("C:\\Users\\jalon\\OneDrive\\Desktop\\Cognac Laboratory\\Scene Ordering\\New Output\\NEW_NEW_output_eng_scenes" + strippedFileName);
					File outputFile = new File("C:\\Users\\jalon\\OneDrive\\Desktop\\Cognac Laboratory\\Scene Ordering\\New AnansiCdec\\AnansiCdec NEW\\New Output" + strippedFileName);
					
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
		StanfordCoreNLP pipeline;
    	Properties props = new Properties(); // Set up pipeline properties
    	props.setProperty("annotators", "tokenize,ssplit,pos,lemma,ner,parse");
    	pipeline = new StanfordCoreNLP(props); // Initialize StanfordCoreNLP with the properties

    	List<String> events = new ArrayList<>();
    	Map<String, List<String>> verbChildren = new HashMap<>(); 
    	
    	while(reader.hasNextLine()) {
        	String data = reader.nextLine();
        	CoreDocument coreDocument = new CoreDocument(data);
        	pipeline.annotate(coreDocument);
        		
        	for(CoreSentence sentence: coreDocument.sentences()) {
        		SemanticGraph dependencyParse = sentence.dependencyParse();
        		for (IndexedWord word : dependencyParse.vertexSet()) {
               		if (word.tag().startsWith("VB")){
                       	if(!auxiliaryVerbsSet.contains(word.word().toLowerCase())) {
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
                            //System.out.println(eventNodesFinal); //TESTING
                       	}
                	}
                		
        		}
        			
        		coreSentenceList.add(sentence); 
        			
        	}
        		//Print out events and their children
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
        } //End of while loop for reading files

        
    }
        		
	/*
	 * *************************** CAEVO FILE PROCESSING METHODS **************************
	 */
	public static void caevoFileProcessor(File folderName) throws IOException {
		//File folderName = new File(projectDir.toString() + "/eng_scenes_new"); 
		//^ Commented out to allow the recursion. Should be passed when it is called.
		File[] files = folderName.listFiles();
		if(files != null) {
			for(File file: files) {
				try {
					//if file.isFile(), do the reading of the txt and 
					if(file.isFile()) {
						Scanner reader = new Scanner(file);
						if(file.getName().endsWith(".xml")) { //POSSIBLY MAKE THIS MY ONLY IF CONDITION IF IM GOING TO JUST GRAB THE RAW TEXT WITHIN THIS XML FUNCTION
							//xml function to grab annotated events, regex:
							
							//TEST PRINT STATEMENT, DELETE LATER:
							System.out.println(file);
							
							xmlEventProcessor(reader, file.toString(), file.getPath().toString());
						}
						
					}else if(file.isDirectory()) {
						//TEST PRINT STATEMENT, DELETE LATER:
						System.out.println(file + " is not an actual file. Calling caevoFileProcessor function again");
						
						caevoFileProcessor(file);
					}
				}catch(IOException e) {
					System.out.println("An error with file handling/creation");
					e.printStackTrace();
				}
			}
		}
	}
	
	public void txtRawText(Scanner reader) {
		
	}
	
	public static void xmlEventProcessor(Scanner reader, String fileName, String filePath) {
		//final String EVENT_REGEX = "<EVENT eid=\"(.*?)\" class=\"(.*?)\">(.*?)</EVENT>";
		final String EVENT_REGEX = "string=\"(.*?)\"";
		//final String SENTENCE_REGEX = "<sentence>(.*?)</sentence>";
		ArrayList<String> caevoEvent = new ArrayList<>();
		
		Pattern eventPattern = Pattern.compile(EVENT_REGEX);
		//Pattern sentencePattern = Pattern.compile(SENTENCE_REGEX);
		
		while(reader.hasNextLine()) {
			String line = reader.nextLine();
			Matcher matcher = eventPattern.matcher(line);
			
			while(matcher.find()) {
				String eventText = matcher.group(1);
				
				//TEST PRINT STATEMENT, DELETE LATER:
				System.out.println("Event text: " + eventText);
				
				caevoEvent.add(eventText); 
				/*
				 * Resolved Q: Maybe I wouldn't add to the static eventPreAnnotated? Maybe I could instead
				 * create a new one that gets passed into the caevoCoreNLP function to THEN add?
				 */
			}
			
		}
		
		caevoCoreNLP(caevoEvent, fileName, filePath);
		
	}
	
	public static void caevoCoreNLP(ArrayList<String> caevoEvent, String fileName, String filePath) {
		StanfordCoreNLP pipeline;
		Properties props = new Properties();
		props.setProperty("annotators", "tokenize,ssplit,pos,lemma,ner,parse");
		pipeline = new StanfordCoreNLP(props);
		
		List<String> eventList = new ArrayList<>();
		Map<String, List<String>> verbChildren = new HashMap<>();
		
		//Match the txt file to the xml fileName passed
		//filepath of fileName to find txt with matching xml name 
		String filePathNew = filePath.replace(".txt.info.xml", ".txt");
		
		//TEST PRINT STATEMENT, DELETE LATER:
		System.out.println("filePathNew is called: " + filePathNew);
		
		File rawTextFile = new File(filePathNew);
		
		try {
			Scanner reader = new Scanner(rawTextFile);
			//Open and read the txt file
			while(reader.hasNextLine()) {
				String data = reader.nextLine();
				
				//TEST PRINT STATEMENT, DELETE LATER:
				System.out.println("Data/Sentence is: " + data);
				
				CoreDocument doc = new CoreDocument(data);
				pipeline.annotate(doc);
				
				for(CoreSentence sentence: doc.sentences()) {
					SemanticGraph dependencyParse = sentence.dependencyParse();
					for(IndexedWord word: dependencyParse.vertexSet()) {
						//caevoevent
						for(String event: caevoEvent) {
							if(word.word().equals(event)) {
								if(!auxiliaryVerbsSet.contains(word.word().toLowerCase())) {
									eventList.add(word.word());
									List<String> children = new ArrayList<>();
									Set<IndexedWord> allChildren = dependencyParse.getChildren(word);
									Set<IndexedWord> descendants = new HashSet<>();
									
									for(IndexedWord child: new ArrayList<>(allChildren)) {
										descendants = getDescendants(child, dependencyParse, descendants);
										for(IndexedWord descendant: descendants) {
											children.add(descendant.word());
										}
									}
									List<IndexedWord> list = new ArrayList<>(descendants);
									verbChildren.put(word.word(), children); //should just be word.word(). It's event in other places bc String event = word.word()
									EventNode eventNode = new EventNode(list, sentence, word.word());
									eventNodesFinal.add(eventNode);
								}
							}
						}
						coreSentenceList.add(sentence); 
					}
				}
			}
			reader.close();
			
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		
		
	}
	
	
	
	public static ArrayList<CoreSentence> getCoreSentenceList(){
		return coreSentenceList;
	}
	
	//The main method getting called by CDEC
	public static ArrayList<EventNode> initializer() throws IOException  { //Return ArrayList
		//fileProcessor();
		File sceneFolderName = new File(projectDir.toString() + "/eng_scenes_new"); 
		caevoFileProcessor(sceneFolderName);
		System.out.println("We are finished with FullFileCoreNLP. . .");
		return eventNodesFinal;
		//System.out.println(projectDir);
		
	}
	
	
	/*
	 * ************************** SINGLE TEXT METHODS *******************
	 */
	//other method that can get called by CDEC
	public static ArrayList<EventNode> oneSentenceInitializer() throws IOException  { 
		//String firstText = "A man who was seen over the weekend dangling from a ride at a Utah amusement park before falling nearly 50 feet to the ground has died, authorities said Monday. The 32-year-old man had been airlifted in critical condition to the University of Utah Hospital in Salt Lake City after falling from the Sky Ride at Lagoon Amusement Park in Farmington on Saturday, FOX13 Salt Lake City reported. Farmington police told local news outlets that they were notified Monday morning of the man’s death. Police have yet to release the man’s name. A witness recorded cellphone video of the man dangling from the chair of the park's Sky Ride. Witness cellphone video of the incident shared with FOX13 shows the man hanging onto the Sky Ride’s safety bar by his hands as he dangled outside the chair. He appeared to be alone on the ride and looked calm as the ride travels high above the park. Police said the incident remains under investigation, releasing few details other than that the fall was not intentional. \"We don't know why he did that or what was going on,\" Farmington Police Chief Wayne Hansen told KSL.com. \"We just don't know.\" Lagoon Amusement Park told FOX13 that the ride did not appear to have malfunctioned at the time of the incident. Police also said the ride appeared to be working correctly, according to KSL. The slow-moving Sky Ride is mainly used as a means of transport by park-goers to get from one end of the park to the other, similar to a chairlift, according to the station.";
		
		//Testing new aux verb removal, 000 and 001
		//String firstText = "Rosemarie von Salten was here in the company of her mother, and Fred Rittner was in the company of his brother, who was several years older than him and with whom he had undertaken a major trip. He wanted to recover from the strain of his doctoral exam, and his brother Magnus had taken some time off from his business to be able to accompany him. The brothers loved each other very much. Magnus Rittner had become head of the large Rittner paint works after the death of his father, and Fred had studied chemistry in order to be able to devote his energy equally to the work. Although the brothers loved each other warmly, they were of very different characters. Magnus was a serious, reliable man who made high demands on himself and others. Fred Rittner was more light-blooded, he loved the joyful enjoyment of life, unscrupulously took all the good and pleasant things that life offered him, without being particularly concerned with serious duties. More amiable than his serious brother, all hearts flew to him, and he accepted it with a sunny victoriousness, as if it could not be otherwise. Rosemarie von Salten, with fine instinct, had immediately recognized in Magnus Rittner the more valuable. She looked up to him in shy admiration, but did not dare to believe that he could ever have any interest in her. At first, of course, he met her with a strangely gentle courtesy that touched her strangely. But then the closer she got to know him, the more reserved he became toward her. Fred Rittner was at first dear and sympathetic to her only because he was Magnus' brother. In order to be able to stay near him a lot, she put up with Fred's company.";
		//String secondText = "But since Fred made an eager effort for her and his sunny kindness ingratiated itself into her heart as well, she thought she loved him. More and more he influenced her thinking, especially as Magnus withdrew from her more and more. When the brothers were with Frau von Salten and her daughter, which happened daily, Magnus occupied himself almost exclusively with the mother, while Fred did not leave Rosemary's side. They made trips together to Helouan, to the pyramids and the nearby fellah villages. Always Fred was next to Rosemarie. So she was convinced that she was indifferent to Magnus, while Fred openly showed her that he loved her. And earlier, in there in the hall, he had whispered to her that his heart beat longingly toward hers. As if in hot fright, Rosemarie had first looked into the unmoving face of Magnus Rittner, as if he had to tell her what she should do. And then she had fled out, as if she was not allowed to listen to what Fred Rittner whispered to her. And now she had become Fred Rittner's bride after all. For a moment she was overcome by a hot anxiety; she didn't know why. But then she said to herself, \"It's all right the way it's come. I will now always be able to stay close to Magnus Rittner. And mamma will now at last be pleased with me, because Fred is a rich man and I am bound to make a brilliant match. How good it is that I love Fred - yes, I love him dearly. If I should have married a man I did not love - I would rather have died. It's all right the way things have turned out.\" Rosemarie was still very young, nineteen years, and she was still an inexperienced child at heart who did not know herself. She now willingly let Fred kiss her, yes, she kissed him again and snuggled comfortably in his arms. For a while they stood intimately embraced. Then Rosemarie suddenly straightened up. \"Fred, I have something to tell you.\" He looked at her in love. \"What do you want to tell me, my sweet Rosemarie?\" She looked at him tall. \"That I'm a poor girl, Fred. I always thought we had a great fortune. But it isn't so. Mama just told me recently.\" Fred smiled, but then asked in surprise, \"Isn't your mother wealthy?\" \"No, Fred, she has nothing but her pension and then admittedly her very precious jewelry, but she will not part with it. Is it very bad that I am poor?\" Fred wondered how Frau von Salten could make such expensive trips if she was poor. But the question was not important to him. He kissed Rosemarie laughing, \"Don't worry, little girl, I have enough for both of us!\" She heaved a satisfied sigh. \"Then it's all right, Fred. What does one need money to be happy!\" This came out of her mouth so naively that he first kissed her again until she was breathless. Then he said, \"Well, after all, without money it's hard to be happy. But I don't want my little girl to think about that at all.\" There was a look in his eyes that caused her to say hastily, \"I must go back in now, Fred, mamma will miss me.\" Carelessly, he shrugged. \"Well - what if she does? Do you think she'll refuse to let me be her son-in-law?\" A faint shadow flitted across her face. \"Oh, Fred!\" \"What is it, Rosemarie? Are you really afraid that your mother will not give us her consent?\" \"Well, Rosemarie, why are you silent?\" asked Fred. She shrank up and smiled. \"Oh, Fred, mamma will be very pleased, of course. You must have noticed that she is very sympathetic to you and your brother.\" Fred nodded in amusement. \"It has not escaped my notice, and has been very dear to me, as I fell violently in love with you at once.\" There was something more in his tone than in his words that displeased Rosemarie. There was a soft twitch in her face, and she put her beautiful little hand to his mouth. \"Don't say in love, Fred, it sounds so-I don't say you love me, with all my heart!\" He kissed her palm. \"Is that something else, Mouse?\" She nodded. \"Being in love, that's such a shallow, dallying thing. I don't want you to be in love with me, I want you to love me.\" He kissed her eyes and mouth. \"If you didn't look so cute doing it, I might be afraid of your seriousness. But you shall not complain, sweetheart! Do you think I'd give up my golden freedom if I didn't love you so madly? You can boast that you have tamed the wild bird.\" \"Are you sorry?\" she asked with charming mischievousness. He pressed her tightly against him. \"Sweet Rosemarie, you are so hold and beautiful that there was no wavering for me. Now you have caught me. - But there is a break in there now, and we really must go in now, or everyone will guess our sweet secret. Tomorrow I'll speak to your mother, and this very evening I want to tell my brother that we're engaged.\" Rosemarie looked up at him questioningly. \"What will your brother say to that? Will he give us his consent?\" \"He will give it, Rosemarie, for he likes you very much.\" Her heart beat rapidly and loudly. \"Is that true?\" \"Certainly. Though he has not spoken of it, yet I know him well enough to know that he thinks much of you. And that is dear to me, Rosemarie, for though I am my free master, I never wish to do anything that displeases my brother. I respect in him the head of the family, the head of our house. And besides, I love him very much and do not wish to grieve him.\" Her eyes shone. \"How wonderful it is that you should speak of your brother in this way. He is a delightful person!\" \"Yes, Rosemarie, he is, and we must always harmonize with him, or I would not be able to be happy. My brother has been above all things to me.\" She nodded, beaming. \"What is in me, I want to do to please him.\" He pulled her to him and kissed her. And then he got cocky and said passionately, \"I would prefer not to let you back into the hall now. There are so many men's eyes on my sweet girl. And Herr von Sellin is courting you dangerously. Don't make me jealous, Rosemarie!\" \"Oh, Fred, how can you say such a thing! Wasn't I extremely reserved towards all gentlemen?\" \"A real little glacier virgin you are in intercourse with the others. Wasn't that just coquetry, though?\" Quite startled, she looked at him. \"But Fred, you mustn't speak to me like that.\" Laughing, he kissed her wonderful eyes. \"Little Rosemarie, can't you take a joke? You don't have to take everything so hard!\" She sighed a little. \"Yes, I'm a bit of a heavy character, Mama often bickers at me about it. I can't go over things easily, and I have to think everything through seriously.\" \"Well, you can do that with my brother, he's so thorough and serious too. But with me you have to laugh, darling.\" \"And if I can't do that all the time? Will you love me less then?\" \"Don't worry, mouse, as long as you are so sweet and lovely, I will always love you.\" \"And when I get old and ugly?\" He laughed boisterously. \"Then I will have become an old man, too, and we will have to be lenient with each other.\" Now she had to laugh, too, and laughing and blissful they strode back into the hall.";
		
		//042 and 043, DONE
		//String firstText = "When she was alone in her room, she took out the notebook and thought about whom she should return the money to, besides Magnus Rittner. She picked out Herr von Schlieben and a Consul Dreyhaupt. Fortunately, she knew the addresses of these two gentlemen. Schlieben got two thousand marks, Dreyhaupt three thousand. A deep, liberating breath lifted her chest. Gottlob gottlob - Magnus Rittner got his money back, and towards him she could feel freer again. All these years the thought had weighed heavily on her that she would probably never be able to free herself from this debt. Now, suddenly, a kind fortune threw this sum into her lap. A hot prayer of thanks flew up to heaven.";
		//String secondText = "And without thinking about it for long, she took leave the next day, drove over to Gotha with Herr and Frau von Schwarzburg, and went to the bank. She deposited the check and gave instructions that five thousand marks be transferred to Magnus Rittner, three thousand to Consul Dreyhaupt, and two thousand to Herr von Schlieben, in the name of her mother, to compensate for a loan received. To Consul Dreyhaupt and to Herr von Schlieben she wanted to write a few more lines of explanation. Magnus Rittner, however, was not to hear from her. She could not write to him. It was enough for him to hear that her mother's debt had been repaid. But now she could think of nothing else but how Magnus Rittner would take this money. Always she saw him before her, as clearly as if she had seen him only yesterday. It was strange - Fred Rittner's image had completely blurred in her memory. But his brother's striking personality had etched itself firmly in her memory. Nor did she think how Fred Rittner would take it that the five thousand marks had been repaid. Only how Magnus Rittner thought about it occupied her incessantly. Would he now think a little more mildly of her mother - and of herself? How would he feel? Had he been at the front, had he fought and suffered? And suddenly a thought made her heart cramp. Was he even still alive? Hadn't the war claimed him as a victim? She trembled, and a strange cold shiver ran over her body.";
		
		//Widows Connections, 009 
		String firstText = "If Adam Leoni. . . worry about who had murdered him.";
		String secondText = "No one would hear the shot that was about to be fired."; 
		
		//Original CoreNLP
		textProcessor(firstText);
		textProcessor(secondText);
		
		
		//OCT: CAEVO CoreNLP
		//caevoTextProcessor(firstText);
		//caevoTextProcessor(secondText);
		
		//NOV: CAEVO Changes
		//newCaevoTextProcessor(firstText);
		//newCaevoTextProcessor(secondText);
		
		System.out.println("We are finished :) Sort of");
		return eventNodesFinal;
		//System.out.println(projectDir);
		
	}
	
	//Method related to oneSentenceInitializer
	public static void textProcessor(String sentenceText) {
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
                       	if(!auxiliaryVerbsSet.contains(word.word().toLowerCase())) {
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

	
	/*
	 * Q: Could I process it even if it has the event tags in it? Would it change anything?
	 */
	public static void caevoTextProcessor(String sentenceText) {
		
		StanfordCoreNLP pipeline;
		Properties props = new Properties();
		props.setProperty("annotators", "tokenize,ssplit,pos,lemma,ner,parse");
		pipeline = new StanfordCoreNLP(props);
		CoreDocument doc = new CoreDocument(sentenceText);
		pipeline.annotate(doc);
		
		List<String> events = new ArrayList<>();
		Map<String, List<String>> verbChildren = new HashMap<>();
		
		for(CoreSentence sentence : doc.sentences()) {
			SemanticGraph dependencyParse = sentence.dependencyParse();
			for(IndexedWord word: dependencyParse.vertexSet()) {
				//Q: Could I pass the sentence or would the pipeline remove any additional information needed to extract the events?
				extractEventWords(sentenceText); 
				for(String event : eventPreAnnotated) {
					//Q: Double check that this is properly taking into consideration the aux verbs we want to ignore
					if(!auxiliaryVerbsSet.contains(event.toLowerCase())) {
						events.add(event);
						
						List<String> children = new ArrayList<>();
						//word or event? Shouldn't cause an issue with word if we're already within the for loop for eventPreAnnotated
						Set<IndexedWord> allChildren = dependencyParse.getChildren(word);
						Set<IndexedWord> descendants = new HashSet<>();
						
						for(IndexedWord child : new ArrayList<>(allChildren)) {
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
			}
			//OCT: REMOVE PRINT AFTER TESTING
			System.out.println("TEST (Sentence being added to coreSentenceList): " + sentence);
			
			coreSentenceList.add(sentence);
		}
		
		
	}
	
	
	/*
	 * Processes the new input from CAEVO by looking for and removing anything between  
	 * "<EVENT>" and "</EVENT>". Adds the new event to a static ArrayList called 
	 * eventPreAnnotated. Returns the string without the tags. 
	 */
	public static String extractEventWords(String line) {
		final String EVENT_REGEX = "<EVENT eid=\"(.*?)\" class=\"(.*?)\">(.*?)</EVENT>";
		final String REMOVE_TAGS_REGEX = "<EVENT[^>]*>(.*?)</EVENT>";
		
		
		String rawText = line.replaceAll(REMOVE_TAGS_REGEX, "$1");
		
		//NOV TEST
		System.out.println("Raw text is: " + rawText);
		
		Pattern pattern = Pattern.compile(EVENT_REGEX);
		Matcher matcher = pattern.matcher(line);
		
		//List<Event> events = new ArrayList<>();
		
		while(matcher.find()) {
			String eventText = matcher.group(3); //1 is the eid and 2 is the class
			
			eventPreAnnotated.add(eventText);
		}
		
		//NOV TEST:
		System.out.println("Events: \n");
		for(String event: eventPreAnnotated) {
			System.out.println(event);
		}
		
		return rawText;
	}	
	
	
	
	
}
