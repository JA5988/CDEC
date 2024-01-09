package feature_extraction;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;
import anansi_utils.*;
import common.Utils;
import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.pipeline.CoreSentence;
import org.nd4j.linalg.ops.transforms.Transforms;
import org.nd4j.linalg.cpu.nativecpu.NDArray;
import common.Globals;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;

public class EvPairDataset {
	
	public final Attribute label = new Attribute("class", new ArrayList<String>(Arrays.asList(new String[] {"0", "1"})));
	static final Logger LOGGER = Logger.getLogger(EvPairDataset.class.getName());
	public Instances data;

	private Instances makeInstances(int size) {
		/*
		 * add attributes
		 */
		String[] realAttrs = {"word_distribution_sim", "ev_sentence_sim", "head_word_vec_sim"};
		ArrayList<Attribute> cols = new ArrayList<Attribute>();
		for(String key : Arrays.asList(realAttrs))
			cols.add(new Attribute(key));
		String[] boolAttrs = {"same_POS"};
		List<String> bool = Arrays.asList("0","1");
		for(String key : Arrays.asList(boolAttrs))
			cols.add(new Attribute(key, bool));
		cols.add(label);
		
		/*
		 * make dataset
		 */
		this.data = new Instances("ev_pairs", cols, size);
		data.setClass(label);
		
		return data;
	}
	public Instances makeDataset( List<EventPair> pairs, boolean lemmatize, String[] pos, List<CoreSentence> docCorpus) {
		LOGGER.info("Making dataset");
		Instances data = makeInstances(pairs.size());

		for(EventPair pair : pairs) {
			Instance vec = evPairVector(pair,docCorpus,lemmatize, pos);
			data.add(vec);
		}
		return data;
	}
/*
	public Instances makeAnansiDataset(List<LinguisticEventPair> pairs, boolean lemmatize, String[] pos){
		Instances data = makeInstances(pairs.size());
		for(LinguisticEventPair pair : pairs){
			Instance vec = anansiPairVector(pair, lemmatize, pos);
			data.add(vec);
		}

		return data;
	}
*/

	public Instance evPairVector(EventPair pair,List<CoreSentence> docCorpus , boolean lemmatize, String[] pos) {
		Instance instance = new DenseInstance(this.data.numAttributes());

		// Retrieve EventNode objects from EventPair
		EventNode ev1 = pair.getEventNodeOne();
		EventNode ev2 = pair.getEventNodeTwo();

		List<IndexedWord> ev1Text = ev1.getIndexedWords();
		List<IndexedWord> ev2Text = ev2.getIndexedWords();
		
		List<CoreSentence> ev1Sentences = new ArrayList<>();
		List<CoreSentence> ev2Sentences = new ArrayList<>();
		
		ev1Sentences.add(ev1.getCoreSentence());
		ev2Sentences.add(ev2.getCoreSentence());

		List<CoreSentence> sentenceCorpus = new ArrayList<>();
		sentenceCorpus.add(ev1Sentences.get(0));
		sentenceCorpus.add(ev2Sentences.get(0));

		// Adjust this part according to how you can access the sentences from the EventNode objects



		/*
		 * event text distribution sim
		 */
		int ngrams = 1;
		EventFeatures feats = new EventFeatures(sentenceCorpus, lemmatize, pos, ngrams);
		double sim = Transforms.cosineSim(this.doubleArrToNDArr(feats.makeEvVector(ev1Text, ev1Sentences)),
				this.doubleArrToNDArr(feats.makeEvVector(ev2Text, ev2Sentences)));
		if(Double.isNaN(sim))
			sim = 0.16; // set to avg
		instance.setValue(this.data.attribute("word_distribution_sim"), sim);

		/*
		 * relative sentence sim
		 */

		if(ev1.getCoreSentence().equals(ev2.getCoreSentence())) {
			instance.setValue(this.data.attribute("ev_sentence_sim"), 1.0);
		}
		else {
			sim = Transforms.cosineSim(this.doubleArrToNDArr(feats.makeSentVector(ev1Sentences, docCorpus)),
					this.doubleArrToNDArr(feats.makeSentVector(ev2Sentences, docCorpus)));
			instance.setValue(this.data.attribute("ev_sentence_sim"), sim);
		}

		/*
		 * avg. head lemma w2v distance
		 */
		String delim = "MAKE_LIST";
		String request = String.format("%1$s/?f=%2$s&p1=%3$s&p2=%4$s", Globals.W2V_SERVER,
				"n_similarity",
				delim + ev1.getEventHeadWord(),
				delim + ev2.getEventHeadWord());
		instance.setValue(this.data.attribute("head_word_vec_sim"), Double.parseDouble(getHTML(request)));

		/*
		 * pos
		 */
		HashSet<String> pos1 = posSet(ev1Text);
		HashSet<String> pos2 = posSet(ev2Text);
		boolean NN = pos1.contains("N") && pos2.contains("N");
		boolean VV = pos1.contains("V") && pos2.contains("V");
		instance.setValue(this.data.attribute("same_POS"), (NN || VV) ? "1" : "0");




		return instance;
	}
/*
	public Instance anansiPairVector(LinguisticEventPair pair, boolean lemmatize, String[] pos){

		Instance instance = new DenseInstance(this.data.numAttributes());
		LinguisticEventNode ev1 = pair.getLinguisticPair().get(0);
		LinguisticEventNode ev2 = pair.getLinguisticPair().get(1);

		List<Token> ev1Text = ev1.getMainEvText();
		List<Token> ev2Text = ev2.getMainEvText();

		List<Sentence> sentenceCorpus = new LinkedList<Sentence>();

		sentenceCorpus.add(ev1.getEventSentence());
		sentenceCorpus.add(ev2.getEventSentence());

		List<Sentence> docCorpus = LinguisticEventNode.getDocumentCorpus();



		int ngrams = 1;

		EventFeatures feats = new EventFeatures(sentenceCorpus, lemmatize, pos, ngrams, true, ev1Text, ev2Text);
		List<Sentence> ev1Sentences = new LinkedList<Sentence>();
		List<Sentence> ev2Sentences = new LinkedList<Sentence>();
		ev1Sentences.add(ev1.getEventSentence());
		ev2Sentences.add(ev2.getEventSentence());

		double wsim = Transforms.cosineSim(this.doubleArrToNDArr(feats.anansimakeEvVector(ev1Text, ev1Sentences,(String) ev1.getEventSentence().sentenceId)),
				this.doubleArrToNDArr(feats.anansimakeEvVector(ev2Text, ev2Sentences,(String) ev2.getEventSentence().sentenceId)));
		if (Double.isNaN(wsim))
			wsim = 0.16; // set to avg
		instance.setValue(this.data.attribute("word_distribution_sim"), wsim);
		instance.setValue(this.data.attribute("word_distribution_sim"), wsim);

		if (ev1.getEventSentence().sentenceId == ev2.getEventSentence().sentenceId) {
			instance.setValue(this.data.attribute("ev_sentence_sim"), 1.0);
		} else {
			double sim;
			sim = Transforms.cosineSim(this.doubleArrToNDArr(feats.anansiMakeSentVector(ev1Sentences, docCorpus)),
					this.doubleArrToNDArr(feats.anansiMakeSentVector(ev2Sentences, docCorpus)));
			instance.setValue(this.data.attribute("ev_sentence_sim"), sim);
		}

		String delim = "MAKE_LIST";
		String request = String.format("%1$s/?f=%2$s&p1=%3$s&p2=%4$s", Globals.W2V_SERVER,
				"n_similarity",
				delim + ev1.getEventTrigger().lemma,
				delim + ev2.getEventTrigger().lemma);
		double wvsimlarity = Double.parseDouble(getHTML(request));
		if (wsim > 0) {
			wvsimlarity = Double.parseDouble(getHTML(request));
		} else {
			wvsimlarity = 0;
		}



		instance.setValue(this.data.attribute("head_word_vec_sim"), wvsimlarity);

		HashSet<String> pos1 = anansiPosSet(ev1Text);
		HashSet<String> pos2 = anansiPosSet(ev2Text);
		boolean NN = pos1.contains("N") && pos2.contains("N");
		boolean VV = pos1.contains("V") && pos2.contains("V");
		instance.setValue(this.data.attribute("same_POS"), (NN || VV) ? "1" : "0");

		instance.setValue(this.data.attribute("class"), false ? "1" : "0");

		return instance;
	}
*/


	private NDArray doubleArrToNDArr(double[] v) {
		float[] vec = new float[v.length];
		for(int i =0 ; i < v.length; i++)
			vec[i] = (float) v[i];
		return new NDArray(vec);
	}
	private HashSet<String> posSet(List<IndexedWord> evText) {
		HashSet<String> pos = new HashSet<String>();
		for(IndexedWord w : evText)
			pos.add(w.tag().substring(0, 1));
		return pos;
	}
	private HashSet<String> anansiPosSet(List<Token> evText) {
		HashSet<String> pos = new HashSet<String>();
		for(Token w : evText)
			pos.add(w.pos.substring(0, 1));
		return pos;
	}

	
	public static String getHTML(String urlToRead) {
		StringBuilder result = new StringBuilder();
		URL url = null;
		HttpURLConnection conn = null;
		BufferedReader rd = null;
		String line;

		try {
			url = new URL(urlToRead);
			conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			while ((line = rd.readLine()) != null) {
				result.append(line);
			}
			rd.close();
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
		
		return result.toString();
	}



}
