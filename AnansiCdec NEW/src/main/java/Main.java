import anansi_utils.*;
import common.*;
import java.io.IOException;
import java.io.FileWriter;
import java.nio.file.Paths;
import java.util.*;

import edu.stanford.nlp.pipeline.CoreSentence;
import feature_extraction.*;
import weka.classifiers.functions.MultilayerPerceptron;
import weka.core.Instance;
import weka.core.Instances;

public class Main {
    //arg[0] directory for graph to process
    public static void main(String[] args){
    	
        List<EventNode> eventNodes = new ArrayList<>();;
		
        try{
			eventNodes = FullFileCoreNLP.initializer(); //CHANGES
		}catch (IOException e) {
			e.printStackTrace();
		}
        
        //System.out.println("Printing out the size of the eventNodes to test: " + eventNodes.size()); //TESTING
        
		List<EventPair> evNodePairs = new ArrayList<>();
        List<CoreSentence> docCorpus = new ArrayList<>();
        
        docCorpus = FullFileCoreNLP.getCoreSentenceList();
        
        //System.out.println("Printing out the docCorpus to test: " + docCorpus); //TESTING 
        
        //evNodePairs gets initialized when the program calls testPairs function from Utils. Grabs the eventNodes initialized earlier which come from coreNLP_FullFile
        List<List<EventNode>> testPairs = Utils.testPairs(eventNodes);
        
        //System.out.println("Printing out the testPairs to test: " + testPairs); //TESTING
        
        //List<LinguisticEventPair> linguisticEventPairs = new LinkedList<LinguisticEventPair>();
        
        for(List<EventNode> pairs : testPairs){
        	//System.out.println("Printing out the eventNode pairs to test: " + pairs); //TESTING
        	
            evNodePairs.add(new EventPair(pairs.get(0), pairs.get(1)));
        }
        EvPairDataset dataMaker = new EvPairDataset();
        //makeDataSet calls the evPairVector function in EvPairDataset class where docCorpus gets initialized based on the coreSentences for each event node
        Instances instncs =  dataMaker.makeDataset(evNodePairs, true, Globals.POS, docCorpus);

        LinkedList<GeneralTuple<Instance, EventPair>> pairTest
                = new LinkedList<>();

        for (EventPair pair : evNodePairs) {
            //evPairVector(EventPair pair,List<CoreSentence> docCorpus , boolean lemmatize, String[] pos)
            Instance inst = dataMaker.evPairVector(pair, docCorpus, true, Globals.POS);
            inst.setDataset(instncs);
            pairTest.add(new GeneralTuple<Instance, EventPair>(inst, pair));
        }
        MultilayerPerceptron clf = new MultilayerPerceptron();
        try {
            clf = (MultilayerPerceptron) weka.core.SerializationHelper.read(Globals.CLASSIFIERS_DIR + "cdec.model.bin");
            //clf.buildClassifier(train);
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        GeneralTuple<Double, HashMap<HashSet<EventNode>, Double>> anansiRes = Utils.testClassifier(clf,pairTest);

        double predictionCutoff = anansiRes.first;
        HashMap<HashSet<EventNode>, Double> predLog = anansiRes.second;
        try {
            writeResults(evNodePairs, predictionCutoff);
        } catch (IOException x){

        }


    }

    //Sept 2: Changed from Lingui.... to EventPair evNodePairs
    public static void writeResults(List<EventPair> evNodePairs, double cdecCutoff) throws IOException{
        System.out.println("Writing to file. . .");
        
        //Finished processing 012_013 SEP 27
    	//FileWriter myWriter = new FileWriter("012_013_Output.txt", true);
    	FileWriter myWriter = new FileWriter("singleSentence.txt", true);
        //System.out.println("Inside the writeResults function"); //TESTING
        
        //Sept 2: Changed from Lingui.... to EventPair and evNodePairs
        for (EventPair pair : evNodePairs){
        	
        	//System.out.println("Inside the for loop in the writeResults function"); //TESTING
        	
            if (pair.cdecScore > cdecCutoff) {
                EventNode event1 = pair.getEventNodeOne();
                EventNode event2 = pair.getEventNodeTwo();
                myWriter.write("TRIGGER WORDS: " + event1.getEventHeadWord() + " " + event2.getEventHeadWord() + "\n");
                myWriter.write("Event One Trigger: " + event1.getEventHeadWord()+"\n");
                myWriter.write("Event One Sentence: " + event1.getCoreSentence().text() +"\n");
                myWriter.write("Event Two Trigger: " + event2.getEventHeadWord()+"\n");
                myWriter.write("Event Two Sentence: " + event2.getCoreSentence().text()+"\n");
                myWriter.write("Score: " + pair.cdecScore + "\n");
                
                
                myWriter.write("\n");
            }
        }
        myWriter.close();
    }
}