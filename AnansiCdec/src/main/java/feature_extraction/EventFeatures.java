package feature_extraction;

//import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import anansi_utils.Sentence;
import anansi_utils.Token;
import common.Globals;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.pipeline.CoreSentence;


public class EventFeatures {
	
	public HashMap<String, Integer> dict;
	public HashMap<String, Integer> anansiDict;
	public final boolean lemmatize;
	public final HashSet<String> pos;
	public final int ngrams;
	public List<List<String>> corpus;
	public HashMap<String, Integer> freqs;
	public double numGrams;


//Anansi Utils
	public EventFeatures(List<CoreSentence> docs, boolean lemmatize, String[] pos, int ngrams) {
		this.lemmatize = lemmatize;
		this.pos = new HashSet<String>(Arrays.asList(pos));
		this.ngrams = ngrams;
		this.corpus = new ArrayList<List<String>>();
		this.dict = this.makeDict(docs); // this.corpus is populated in "filter" function, and contains ngrams
	}
	private HashMap<String, Integer> makeDict(List<CoreSentence> corpus) {
		HashMap<String, Integer> dict = new HashMap<String, Integer>();
		HashSet<String> vocab = this.filterAndNgram(corpus);
		int idx = 0;
		for(String word : vocab)
			dict.put(word, idx++);

		return dict;
	}
	public double[] makeEvVector(List<IndexedWord> evText, List<CoreSentence> doc) {
		/*
		 * document
		 */
		List<String> cleanDoc = new ArrayList<String>();
		for(CoreSentence s : doc) {
			// unigrams
			ArrayList<String> cleanSent = clean(s);
			cleanDoc.addAll(clean(s));

			// ngrams
			int n_copy = this.ngrams;
			while(n_copy > 1)
				cleanDoc.addAll(ngrams(cleanSent, n_copy--));
		}
		HashSet<String> docVocab = new HashSet<String>(cleanDoc);
		/*
		 * event
		 */
		HashMap<Integer, ArrayList<String>> sIdToEvText = new HashMap<Integer, ArrayList<String>>();
		List<String> evNGrams = new ArrayList<String>();
		// partition by sentence for ngrams
		for(IndexedWord tok : evText) {
			if(!sIdToEvText.containsKey(tok.sentIndex()))
				sIdToEvText.put(tok.sentIndex(), new ArrayList<String>());
			String word = cleanTok(new CoreLabel(tok), this.lemmatize, this.pos);
			if(word != null)
				sIdToEvText.get(tok.sentIndex()).add(word);
		}
		for(int s_id : sIdToEvText.keySet()) {
			evNGrams.addAll(sIdToEvText.get(s_id));
			int n_copy = this.ngrams;
			while(n_copy > 1) {
				ArrayList<String> grams = ngrams(sIdToEvText.get(s_id), n_copy--);
				for(String g : grams) {
					if(docVocab.contains(g)) // evText might have non-contiguous ngrams
						evNGrams.add(g);
				}
			}
		}
		double[] vector = new double[this.dict.size()];
		for(String gram : evNGrams)
			vector[this.dict.get(gram)] += 1.0/evNGrams.size();

		return vector;
	}
	public double[] makeSentVector(List<CoreSentence> sent, List<CoreSentence> docs) {
		List<String> words = new LinkedList<String>();
		for(CoreSentence s : sent) {
			words.addAll(clean(s));
		}
		List<List<String>> strDocs = new ArrayList<List<String>>();
		HashMap<String, Integer> vocabDict = new HashMap<String, Integer>();
		int i = 0;
		for(CoreSentence s : docs) {
			ArrayList<String> strSent = clean(s);
			for(String w : strSent) {
				if(!vocabDict.containsKey(w))
					vocabDict.put(w, i++);
			}
			strDocs.add(strSent);
			
			//System.out.println("Printing out strDocs within for loop for s : docs... " + strDocs);
			//System.out.println("Printing out in the same area, the strSent " + strSent);
		}
		double[] vec = new double[vocabDict.size()];
		for(String w : words) {
			//System.out.println("Printing w: " + w); //TESTING
			//System.out.println("Printing words: " + words); //TESTING
			//System.out.println("Printing strDocs: " + strDocs); //TESTING
			
			
			vec[vocabDict.get(w)] = tf(w, words)* idf(w, strDocs);
		}

		return vec;
	}
	private ArrayList<String> clean(CoreSentence doc){
		ArrayList<String> cleanSent = new ArrayList<String>();
		for(CoreLabel tok : doc.tokens()) {
			String word = cleanTok(tok, this.lemmatize, this.pos);
			if(word != null)
				cleanSent.add(word);
		}

		return cleanSent;
	}
	public static String cleanTok(CoreLabel tok, boolean lemmatize, HashSet<String> pos) {

		String clean = "";
		if(pos.isEmpty()) {
			if(lemmatize)
				clean = tok.lemma();
			else
				clean = tok.originalText();
		}
		else if(pos.contains(tok.tag().subSequence(0, 1))) {
			if(lemmatize)
				clean = tok.lemma();
			else
				clean = tok.originalText();
		}

		clean = clean.replaceAll("[^a-zA-Z0-9]", "");
		if(clean.length() == 0 || Globals.strIgnore.contains(clean) )
			return null;
		return clean.toLowerCase();
	}
	private HashSet<String> filterAndNgram(List<CoreSentence> corpus){
		ArrayList<ArrayList<String>> cleanCorpus = new ArrayList<ArrayList<String>>();
		for(CoreSentence doc : corpus)
			cleanCorpus.add(clean(doc));
		HashSet<String> vocab = new HashSet<String>();

		for(ArrayList<String> doc : cleanCorpus) {
			int n_copy = this.ngrams;
			// unigrams
			vocab.addAll(doc);
			this.corpus.add(doc);

			// ngrams
			while(n_copy > 1) {
				ArrayList<String> grams = ngrams(doc, n_copy--);
				vocab.addAll(grams);
				doc.addAll(grams);
			}
		}
		return vocab;
	}
	private HashMap<String, Integer> anansiMakeDict(List<Sentence> corpus,List<Token> ev1Tokens, List<Token> ev2Tokens ){
		HashMap<String, Integer> dict = new HashMap<String, Integer>();
		HashSet<String> vocab = this.anansiFilterAndNgram(corpus);
		int idx = 0;
		for(String word: vocab) {
			if(word != null)
				dict.put(word, idx++);

		}

		for(Token tkn : ev1Tokens){
			if (dict.containsKey(tkn.lemma)){
				continue;
			} else {
				dict.put(tkn.lemma, tkn.index);
			}
		}
		for(Token tkn : ev2Tokens){
			if (dict.containsKey(tkn.lemma)){
				continue;
			} else {
				dict.put(tkn.lemma, tkn.index);
			}
		}
		return dict;
	}
	private HashSet<String> anansiFilterAndNgram(List<Sentence> corpus){
		ArrayList<ArrayList<String>> cleanCorpus = new ArrayList<ArrayList<String>>();
		for(Sentence doc : corpus) {
			if(doc == null)
				System.out.println("Hello");
			cleanCorpus.add(anansiClean(doc));
		}
		HashSet<String> vocab = new HashSet<String>();

		for(ArrayList<String> doc : cleanCorpus) {
			int n_copy = this.ngrams;
			// unigrams
			vocab.addAll(doc);
			this.corpus.add(doc);

			// ngrams
			while(n_copy > 1) {
				ArrayList<String> grams = ngrams(doc, n_copy--);
				vocab.addAll(grams);
				doc.addAll(grams);
			}
		}
		return vocab;
	}

	private ArrayList<String> anansiClean(Sentence doc){
		if(doc == null)
			System.out.println();
		ArrayList<String> cleanSent = new ArrayList<String>();
		for(Token tok : doc.tokens) {
			if(tok == null)
				continue;
			String word = anansicleanTok(tok, this.lemmatize, this.pos);

			if(word != null)
				cleanSent.add(word);
		}

		return cleanSent;
	}

	public static String anansicleanTok(Token tok, boolean lemmatize, HashSet<String> pos) {

		String clean = "";
		if(pos.isEmpty()) {
			if(lemmatize)
				clean = tok.lemma;
			else
				clean = tok.originalText;
		}
		else if(pos.contains(tok.pos.subSequence(0, 1))) {
			if(lemmatize)
				clean = tok.lemma;
			else
				clean = tok.originalText;
		}

		clean = clean.replaceAll("[^a-zA-Z0-9]", "");
		if(clean.length() == 0 || Globals.strIgnore.contains(clean) )
			return null;
		return clean.toLowerCase();
	}

	public double[] anansimakeEvVector(List<Token> evText, List<Sentence> doc, String eventSentenceNumber) {
		/*
		 * document
		 */
		List<String> cleanDoc = new ArrayList<String>();
		for (Sentence s : doc) {
			// unigrams
			ArrayList<String> cleanSent = anansiClean(s);
			cleanDoc.addAll(anansiClean(s));

			// ngrams
			int n_copy = this.ngrams;
			while (n_copy > 1)
				cleanDoc.addAll(ngrams(cleanSent, n_copy--));
		}
		HashSet<String> docVocab = new HashSet<String>(cleanDoc);
		/*
		 * event
		 */
		HashMap<String, ArrayList<String>> sIdToEvText = new HashMap<String, ArrayList<String>>();
		List<String> evNGrams = new ArrayList<String>();
		// partition by sentence for ngrams
		for (Token tok : evText) {
			if (!sIdToEvText.containsKey(eventSentenceNumber))
				sIdToEvText.put(eventSentenceNumber, new ArrayList<String>());
			String word = anansicleanTok(tok, this.lemmatize, this.pos);
			if (word != null)
				sIdToEvText.get(eventSentenceNumber).add(word);
		}
		for (String s_id : sIdToEvText.keySet()) {
			evNGrams.addAll(sIdToEvText.get(s_id));
			int n_copy = this.ngrams;
			while (n_copy > 1) {
				ArrayList<String> grams = ngrams(sIdToEvText.get(s_id), n_copy--);
				for (String g : grams) {
					if (docVocab.contains(g)) // evText might have non-contiguous ngrams
						evNGrams.add(g);
				}
			}
		}
		double[] vector = new double[this.dict.size()];
		for(String gram : evNGrams) {
			if(this.dict.get(gram) == null){
				continue;
			}
			System.out.println("GRAM: " + gram);
			System.out.println("INDEX: " + this.dict.get(gram));
			System.out.println("SIZE: " + vector.length);
			vector[this.dict.get(gram)] += 1.0 / evNGrams.size();
		}
		return vector;
	}

	public double[] anansiMakeSentVector(List<Sentence> sent, List<Sentence> docs) {
		List<String> words = new LinkedList<String>();
		for(Sentence s : sent) {
			words.addAll(anansiClean(s));
		}
		List<List<String>> strDocs = new ArrayList<List<String>>();
		HashMap<String, Integer> vocabDict = new HashMap<String, Integer>();
		int i = 0;
		for(Sentence s : docs) {
			ArrayList<String> strSent = anansiClean(s);
			for(String w : strSent) {
				if(!vocabDict.containsKey(w))
					vocabDict.put(w, i++);
			}
			strDocs.add(strSent);
		}
		double[] vec = new double[vocabDict.size()];
		for(String w : words) {
			vec[vocabDict.get(w)] = tf(w, words)* idf(w, strDocs);
		}

		return vec;
	}

	//Anansi Utils

	
	/**
	 * @param doc list of strings
	 * @param term String represents a term
	 * @return term frequency of term in document
	 */
	public double tf(String term, List<String> doc) {
		double result = 0;
		for (String word : doc) {
			if (term != null) {
				if (term.equals(word))
					result++;
			}
		}
//		assertNotEquals(result, 0);
		return result / doc.size();
	}

	
	/**
	 * @param docs list of list of strings represents the dataset
	 * @param term String represents a term
	 * @return the inverse term frequency of term in documents
	 */
	public double idf(String term, List<List<String>> docs) {
		double n = 0;
		for (List<String> doc : docs) {
			for (String word : doc) {
				if(term != null) {
					if (term.equals(word)) {
						n++;
						break;
					}
				}
			}
		}
		double idf = Math.log(docs.size() / n);
//		assertNotEquals(n, 0.0);
		return idf;
	}

	
	public static ArrayList<String> ngrams(ArrayList<String> words, int n) {
		if(n <= 1) {
			return new ArrayList<String>(words);
		}
		
		ArrayList<String> ngrams = new ArrayList<String>();
		
		int c = words.size();
		for(int i = 0; i < c; i++) {
			if((i + n - 1) < c) {
				int stop = i + n;
				String ngramWords = words.get(i);
				
				for(int j = i + 1; j < stop; j++) {
					ngramWords +=" "+ words.get(j);
				}
				
				ngrams.add(ngramWords);
			}
		}
		
		return ngrams;
	}



}
