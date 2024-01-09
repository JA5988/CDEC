
/*
import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.pipeline.*;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations;
import edu.stanford.nlp.simple.*;
import edu.stanford.nlp.trees.GrammaticalRelation;
import edu.stanford.nlp.util.CoreMap;

import java.util.*;
 */
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import anansi_utils.EventNode;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.pipeline.CoreDocument;
import edu.stanford.nlp.pipeline.CoreSentence;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.semgraph.SemanticGraph;

public class newTestingCoreNLP_IndividualSentence {
	
	/*public static Set<IndexedWord> getDescendants(IndexedWord word, SemanticGraph depParse, Set<IndexedWord> descendants){
		
		try {
				FileWriter fileWriter = new FileWriter("output2.txt", true); // true to append to existing file
				PrintWriter printWriter = new PrintWriter(fileWriter);

				printWriter.println(depParse);

				printWriter.close();
			} catch (IOException e) {
				System.out.println("An error occurred.");
				e.printStackTrace();
			}
		
		
			if (!depParse.getChildren(word).isEmpty()) {
				for(IndexedWord wordChild : depParse.getChildren(word)) {
					if(descendants.contains(wordChild)) {
						
					}else {
						descendants.add(wordChild);
						getDescendants(wordChild, depParse, descendants);
					}
					
				}
			}
			return descendants;
		} 
*/
	
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
	
	public static void main(String[] args){
        //String text = "On Monday, the victims’ relatives went to the Jiangshan Municipal Funeral Parlor. Gun supporters hold that people, not guns, are to blame for the shootings.";
		//String text = "Rosemarie von Salten was here in the company of her mother, and Fred Rittner was in the company of his brother, who was several years older than him and with whom he had undertaken a major trip. He wanted to recover from the strain of his doctoral exam, and his brother Magnus had taken some time off from his business to be able to accompany him. The brothers loved each other very much. Magnus Rittner had become head of the large Rittner paint works after the death of his father, and Fred had studied chemistry in order to be able to devote his energy equally to the work. Although the brothers loved each other warmly, they were of very different characters. Magnus was a serious, reliable man who made high demands on himself and others. Fred Rittner was more light-blooded, he loved the joyful enjoyment of life, unscrupulously took all the good and pleasant things that life offered him, without being particularly concerned with serious duties. More amiable than his serious brother, all hearts flew to him, and he accepted it with a sunny victoriousness, as if it could not be otherwise. Rosemarie von Salten, with fine instinct, had immediately recognized in Magnus Rittner the more valuable. She looked up to him in shy admiration, but did not dare to believe that he could ever have any interest in her. At first, of course, he met her with a strangely gentle courtesy that touched her strangely. But then the closer she got to know him, the more reserved he became toward her. Fred Rittner was at first dear and sympathetic to her only because he was Magnus' brother. In order to be able to stay near him a lot, she put up with Fred's company.";
        //String text = "Rosemarie von Salten was here in the company of her mother, and Fred Rittner was in the company of his brother, who was several years older than him and with whom he had undertaken a major trip. He wanted to recover from the strain of his doctoral exam, and his brother Magnus had taken some time off from his business to be able to accompany him. The brothers loved each other very much. Magnus Rittner had become head of the large Rittner paint works after the death of his father, and Fred had studied chemistry in order to be able to devote his energy equally to the work. Although the brothers loved each other warmly, they were of very different characters. Magnus was a serious, reliable man who made high demands on himself and others. Fred Rittner was more light-blooded, he loved the joyful enjoyment of life, unscrupulously took all the good and pleasant things that life offered him, without being particularly concerned with serious duties.";
        //String text = "But since Fred made an eager effort for her and his sunny kindness ingratiated itself into her heart as well, she thought she loved him. More and more he influenced her thinking, especially as Magnus withdrew from her more and more. When the brothers were with Frau von Salten and her daughter, which happened daily, Magnus occupied himself almost exclusively with the mother, while Fred did not leave Rosemary's side. They made trips together to Helouan, to the pyramids and the nearby fellah villages. Always Fred was next to Rosemarie. So she was convinced that she was indifferent to Magnus, while Fred openly showed her that he loved her. And earlier, in there in the hall, he had whispered to her that his heart beat longingly toward hers. As if in hot fright, Rosemarie had first looked into the unmoving face of Magnus Rittner, as if he had to tell her what she should do. And then she had fled out, as if she was not allowed to listen to what Fred Rittner whispered to her. And now she had become Fred Rittner's bride after all. For a moment she was overcome by a hot anxiety; she didn't know why. But then she said to herself, \"It's all right the way it's come. I will now always be able to stay close to Magnus Rittner. And mamma will now at last be pleased with me, because Fred is a rich man and I am bound to make a brilliant match. How good it is that I love Fred - yes, I love him dearly. If I should have married a man I did not love - I would rather have died. It's all right the way things have turned out.\" Rosemarie was still very young, nineteen years, and she was still an inexperienced child at heart who did not know herself. She now willingly let Fred kiss her, yes, she kissed him again and snuggled comfortably in his arms. For a while they stood intimately embraced. Then Rosemarie suddenly straightened up. \"Fred, I have something to tell you.\" He looked at her in love. \"What do you want to tell me, my sweet Rosemarie?\" She looked at him tall. \"That I'm a poor girl, Fred. I always thought we had a great fortune. But it isn't so. Mama just told me recently.\" Fred smiled, but then asked in surprise, \"Isn't your mother wealthy?\" \"No, Fred, she has nothing but her pension and then admittedly her very precious jewelry, but she will not part with it. Is it very bad that I am poor?\" Fred wondered how Frau von Salten could make such expensive trips if she was poor. But the question was not important to him. He kissed Rosemarie laughing, \"Don't worry, little girl, I have enough for both of us!\" She heaved a satisfied sigh. \"Then it's all right, Fred. What does one need money to be happy!\" This came out of her mouth so naively that he first kissed her again until she was breathless. Then he said, \"Well, after all, without money it's hard to be happy. But I don't want my little girl to think about that at all.\" There was a look in his eyes that caused her to say hastily, \"I must go back in now, Fred, mamma will miss me.\" Carelessly, he shrugged. \"Well - what if she does? Do you think she'll refuse to let me be her son-in-law?\" A faint shadow flitted across her face. \"Oh, Fred!\" \"What is it, Rosemarie? Are you really afraid that your mother will not give us her consent?\" \"Well, Rosemarie, why are you silent?\" asked Fred. She shrank up and smiled. \"Oh, Fred, mamma will be very pleased, of course. You must have noticed that she is very sympathetic to you and your brother.\" Fred nodded in amusement. \"It has not escaped my notice, and has been very dear to me, as I fell violently in love with you at once.\" There was something more in his tone than in his words that displeased Rosemarie. There was a soft twitch in her face, and she put her beautiful little hand to his mouth. \"Don't say in love, Fred, it sounds so-I don't say you love me, with all my heart!\" He kissed her palm. \"Is that something else, Mouse?\" She nodded. \"Being in love, that's such a shallow, dallying thing. I don't want you to be in love with me, I want you to love me.\" He kissed her eyes and mouth. \"If you didn't look so cute doing it, I might be afraid of your seriousness. But you shall not complain, sweetheart! Do you think I'd give up my golden freedom if I didn't love you so madly? You can boast that you have tamed the wild bird.\" \"Are you sorry?\" she asked with charming mischievousness. He pressed her tightly against him. \"Sweet Rosemarie, you are so hold and beautiful that there was no wavering for me. Now you have caught me. - But there is a break in there now, and we really must go in now, or everyone will guess our sweet secret. Tomorrow I'll speak to your mother, and this very evening I want to tell my brother that we're engaged.\" Rosemarie looked up at him questioningly. \"What will your brother say to that? Will he give us his consent?\" \"He will give it, Rosemarie, for he likes you very much.\" Her heart beat rapidly and loudly. \"Is that true?\" \"Certainly. Though he has not spoken of it, yet I know him well enough to know that he thinks much of you. And that is dear to me, Rosemarie, for though I am my free master, I never wish to do anything that displeases my brother. I respect in him the head of the family, the head of our house. And besides, I love him very much and do not wish to grieve him.\" Her eyes shone. \"How wonderful it is that you should speak of your brother in this way. He is a delightful person!\" \"Yes, Rosemarie, he is, and we must always harmonize with him, or I would not be able to be happy. My brother has been above all things to me.\" She nodded, beaming. \"What is in me, I want to do to please him.\" He pulled her to him and kissed her. And then he got cocky and said passionately, \"I would prefer not to let you back into the hall now. There are so many men's eyes on my sweet girl. And Herr von Sellin is courting you dangerously. Don't make me jealous, Rosemarie!\" \"Oh, Fred, how can you say such a thing! Wasn't I extremely reserved towards all gentlemen?\" \"A real little glacier virgin you are in intercourse with the others. Wasn't that just coquetry, though?\" Quite startled, she looked at him. \"But Fred, you mustn't speak to me like that.\" Laughing, he kissed her wonderful eyes. \"Little Rosemarie, can't you take a joke? You don't have to take everything so hard!\" She sighed a little. \"Yes, I'm a bit of a heavy character, Mama often bickers at me about it. I can't go over things easily, and I have to think everything through seriously.\" \"Well, you can do that with my brother, he's so thorough and serious too. But with me you have to laugh, darling.\" \"And if I can't do that all the time? Will you love me less then?\" \"Don't worry, mouse, as long as you are so sweet and lovely, I will always love you.\" \"And when I get old and ugly?\" He laughed boisterously. \"Then I will have become an old man, too, and we will have to be lenient with each other.\" Now she had to laugh, too, and laughing and blissful they strode back into the hall.";
        String text = "They squeezed hands, and Fred hurried across the terrace to another entrance. Magnus walked slowly back into the hall. His eyes searched for Rosemarie. She was still sitting next to her mother, and her eyes were shining blissfully. Her sweet, lovely face seemed to him more beautiful and lovely than ever. \"She is blameless and she will have to suffer blamelessly. If one day she has to learn who her mother is, she will be in despair. If I could help her! How gladly I would.\" Thus spoke his heart. And he hardly let her out of his sight and memorized her fair features as if it were for life.";
        StanfordCoreNLP pipeline;

        // Set up pipeline properties
        Properties props = new Properties();
        props.setProperty("annotators", "tokenize,ssplit,pos,lemma,ner,parse");

        // Initialize StanfordCoreNLP with the properties
        pipeline = new StanfordCoreNLP(props);
        CoreDocument doc = new CoreDocument(text);
        pipeline.annotate(doc);

        // A list to store the detected verbs (events)
        List<String> events = new ArrayList<>();
        // A map to store the children of each verb
        //MODIFICATION: String to Integer, back to String
        Map<String, List<String>> verbChildren = new HashMap<>();

        
        for (CoreSentence sentence : doc.sentences()) {
            SemanticGraph dependencyParse = sentence.dependencyParse();
            for (IndexedWord word : dependencyParse.vertexSet()) {
            	//MODIFICATION: from VB.equals to word.tag().starts with
                if (word.tag().startsWith("VB")){ 
                	//MODIFICATION: assinging word.word to events variable
                	String event = word.word();
                    events.add(event);
                    
                    //MODIFICATION: From String to Integer, to back to String
                    List<String> children = new ArrayList<>();
                    //MODIFICATION:New line created a List
                    Set<IndexedWord> allChildren = dependencyParse.getChildren(word);
                    //MODIFICATION: IndexedWord iw to IndexedWord child
                    //MODIFICATION: changing the : to allChildren
                    for (IndexedWord child : new ArrayList<>(allChildren)) {
                        //07/19 REMOVED WHILE FIGURING OUT getDescendants method
                    	//children.add(child.word());
                        
                        //MODIFICATION OF GRANDCHILDREN, later removed?
                        /*for(IndexedWord grandChild : dependencyParse.getChildren(iw)){
                        	children.add(grandChild);
                        }*/
                        
                       //MODIFICATION 07/19, getting the descendants:
                    
                      // Set<IndexedWord> descendants = dependencyParse.descendants(child);
                       Set<IndexedWord> descendants = new HashSet<>();
                       descendants = getDescendants(child, dependencyParse, descendants);
                       for(IndexedWord descendant : descendants) {
                    	   //MODIFICATION 07/19: Was checking if repeated, removed the conditional.
                    	   children.add(descendant.word());
                    	   
                       }
                        
                       /* Set<IndexedWord> grandChildren = dependencyParse.getChildren(child);
                        for(IndexedWord grandChild : new ArrayList<>(grandChildren)) {
                        	children.add(grandChild.word());
                        } */
                    }
                    //MODIFICATION: from word.word to word.index
                    verbChildren.put(event, children);
                }
            }
        }

        // Print out events and their children
        //System.out.println("Events:");
        for (String event : events) {
        	System.out.println("NEW EVENT:"); //TESTING
            System.out.println(event);
            
            //MODIFICATION: Adding the printing of children of events/dependencies here:
            List<String> dependencies = verbChildren.get(event);
            if(dependencies != null) {
            	System.out.println("DEPENDENCIES");
            	for(String dependency : dependencies) {
            		System.out.println(dependency);
            	}
            	System.out.println(); //TESTING
            }
        }

        //MODIFICATION: Removing this?
        /*
        System.out.println("\nChildren of each event:");
        
        for (Map.Entry<Integer, List<IndexedWord>> entry : verbChildren.entrySet()) {
            //MODIFICATION: From entry.getKey() only to this whole beast of a line
        	//
        	System.out.print(doc.sentences().get(0).tokens().get(entry.getKey() - 1).word() + " -> ");
        	
            for (IndexedWord child : entry.getValue()) {
                System.out.print(child.word() + ", ");
            }
            System.out.println();
        }
        */
    }
}
