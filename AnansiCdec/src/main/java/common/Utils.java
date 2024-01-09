package common;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;
import edu.emory.clir.clearnlp.util.StringUtils;
import anansi_utils.*;
import com.google.gson.Gson;
import edu.cmu.lti.ws4j.WS4J;
import weka.classifiers.Classifier;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.SerializationHelper;

/**
 * Misc. procedures that could be in main but add too much visual clutter
 * @author acrem003
 *
 */
public class Utils {
	static AnansiDoc doc = null;
	static Gson GSON = new Gson();
	static String model = Globals.CLASSIFIERS_DIR + "classifier.model.bin";
	static File file = new File("C:\\CDEC\\anansi\\graph\\new_anansi\\jfk.json");
	static Map<Integer, Nodes> nodes = new HashMap<Integer, Nodes>();
	static Map<Integer, Nodes> unlabeledNodes = new HashMap<Integer, Nodes>();
	static Map<Integer, Nodes> compoundTerms = new HashMap<Integer, Nodes>();
	static Map<Integer, Nodes> linguisticEvents = new HashMap<Integer, Nodes>();
	static Map<Integer, Nodes> sentenceNodes = new HashMap<Integer, Nodes>();
	static Map<Integer, Nodes> eventPredicates = new HashMap<Integer, Nodes>();
	static Map<Integer, Set<Rels>> relsByStartNode = new HashMap<Integer,Set<Rels>>();
	static Map<Integer, Set<Rels>> relsByEndNode = new HashMap<Integer,Set<Rels>>();
	static Map<Integer, Nodes> terms = new HashMap<Integer, Nodes>();
	static Set<Integer> nodesInCompounds = new HashSet<Integer>();
	static boolean termChunking;
	static List<Sentence> docCorpus = new LinkedList<Sentence>();
	static Set<LinguisticEventNode> linguisticEventSet = new HashSet<LinguisticEventNode>();
	static String fileName = "";
	static Map<Nodes, Set<Nodes>> leAdjacent = new HashMap<Nodes, Set<Nodes>>();
	static Map<Nodes, Set<Nodes>> leArgumentsEntities = new HashMap<Nodes, Set<Nodes>>();

	private static Classifier wekaClassifier;
	private static final ArrayList<Attribute> ATTRIBUTES = new ArrayList<>();
	private static final ArrayList<String> CLASS_VALUES = new ArrayList<>();

	static
	{
		ATTRIBUTES.add(new Attribute("jcn"));
		ATTRIBUTES.add(new Attribute("hso"));
		ATTRIBUTES.add(new Attribute("lesk"));
		ATTRIBUTES.add(new Attribute("lin"));
		ATTRIBUTES.add(new Attribute("path"));
		ATTRIBUTES.add(new Attribute("res"));
		ATTRIBUTES.add(new Attribute("wup"));
		CLASS_VALUES.add("Yes");
		CLASS_VALUES.add("No");
		Attribute classAttribute = new Attribute("theClass", CLASS_VALUES);
		ATTRIBUTES.add(classAttribute);
	}

	static Map<String, Set<Nodes>> docSentences = new HashMap<String, Set<Nodes>>();
	public static void getAgents(Set<LinguisticEventNode> linguisticEvents){
		List<Token> agentSet = new LinkedList<>();
		for(LinguisticEventNode linguisticEvent : linguisticEvents){
			for(Rels rel :relsByEndNode.get(linguisticEvent.getLinguisticEventId())){
				if (rel.properties.SemanticCategory != null) {
					if (rel.properties.SemanticCategory.contains("Agent")) {
						Nodes agent = nodes.get(rel.startNodeId);
						Set<Nodes> agents;
						if(labelsAsSet(agent).contains("Salary")){
							agents = gatherSalaryTokens(agent);
						} else{
							agents = gatherArgEntityTokens(agent);
						}

						for (Nodes node : agents) {
							Token tkn = new Token(Integer.parseInt(node.properties.ElementId), (String) node.properties.Text,
									(String) node.properties.Lemma, (String) node.properties.PartOfSpeech);
							linguisticEvent.getAgent().add(tkn);
						}

					}
				}
			}
		}

	}

	public static void getPatients(Set<LinguisticEventNode> linguisticEvents){
		List<Token> patientSet = new LinkedList<>();
		boolean noIncoming;
		for(LinguisticEventNode linguisticEvent : linguisticEvents) {
			noIncoming= relsByEndNode.get(linguisticEvent.getLinguisticEventId()) == null;
			if (!noIncoming){
				for (Rels rel : relsByEndNode.get(linguisticEvent.getLinguisticEventId())) {
					if (rel.properties.SemanticCategory != null) {
						if (rel.properties.SemanticCategory.contains("Patient")) {
							Nodes agent = nodes.get(rel.startNodeId);
							Set<Nodes> agents = gatherArgEntityTokens(agent);
							for (Nodes node : agents) {
								Token tkn = new Token(Integer.parseInt(node.properties.ElementId), (String) node.properties.Text,
										(String) node.properties.Lemma, (String) node.properties.PartOfSpeech);
								linguisticEvent.getPatient().add(tkn);
							}
						}
					}

				}
			} else {
				continue;
			}
		}
	}

	public static void getLocations(Set<LinguisticEventNode> linguisticEvents){
		List<Token> locationSet = new LinkedList<>();
		boolean noOutgoing;
		for(LinguisticEventNode linguisticEvent : linguisticEvents){
			noOutgoing = relsByStartNode.get(linguisticEvent.getLinguisticEventId()) == null;
			if (!noOutgoing){
				for (Rels rel : relsByStartNode.get(linguisticEvent.getLinguisticEventId())){
					if (rel.type.contains("LocatedIn")){
						Nodes location = nodes.get(rel.endNodeId);
						Set<Nodes> locations = gatherLocationTokens(location);
						for (Nodes node : locations) {
							Token tkn = new Token(Integer.parseInt(node.properties.ElementId), (String) node.properties.Text,
									(String) node.properties.Lemma, (String) node.properties.PartOfSpeech);
							linguisticEvent.getLocatedIn().add(tkn);
						}
					}

				}
			} else{
				continue;
			}
		}
	}

	public static void getOccursOn(Set<LinguisticEventNode> linguisticEvents){
		List<Token> temporalSet = new LinkedList<>();
		boolean noOutgoing;
		for(LinguisticEventNode linguisticEvent : linguisticEvents){
			noOutgoing = relsByStartNode.get(linguisticEvent.getLinguisticEventId()) == null;
			if (!noOutgoing){
				for (Rels rel : relsByStartNode.get(linguisticEvent.getLinguisticEventId())){
					if (rel.type.contains("OccursOn")){
						Nodes temporal = nodes.get(rel.endNodeId);
						if(temporal.properties.SentenceId == null){
							//Temporal does not have a sentence, its probably the document date so we skip.
							continue;
						}
						Set<Nodes> locations =  gatherTemporalTokens(temporal);
						for (Nodes node : locations) {
							Token tkn = new Token(Integer.parseInt(node.properties.ElementId), (String) node.properties.Text,
									(String) node.properties.Lemma, (String) node.properties.PartOfSpeech);
							linguisticEvent.getOccursOn().add(tkn);
						}
					}

				}
			} else{
				continue;
			}
		}
	}

	public static double evaluateSimilarity(String term1, String term2){
		double[] similarityVector = new double[8];
		similarityVector[0]= WS4J.runJCN(term1,term2);
		similarityVector[1] = WS4J.runHSO(term1, term2);
		similarityVector[2] = WS4J.runLCH(term1, term2);
		similarityVector[3] = WS4J.runLESK(term1, term2);
		similarityVector[4] = WS4J.runLIN(term1,term2);
		similarityVector[5] = WS4J.runPATH(term1,term2);
		similarityVector[6] = WS4J.runRES(term1,term2);
		similarityVector[7] = WS4J.runWUP(term1,term2);
		Instances dataSet = new Instances("my-dataset", ATTRIBUTES, 9);
		dataSet.setClassIndex(7);
		dataSet.add(new DenseInstance(1,similarityVector));
		double[] result = null;
		try{
			result = wekaClassifier.distributionForInstance(dataSet.firstInstance());
		} catch (Exception ex){
			System.out.println("Error classifying");
			System.exit(-3);
		}
		return result[0];
 	}

	public static void classifyAdjacent(){

		for (Nodes node : linguisticEvents.values()){
			leArgumentsEntities.put(node,new HashSet<Nodes>());
		}
		 for (Nodes leNode : linguisticEvents.values()){


				if (relsByStartNode.get(leNode.id) != null) {
					for (Rels leRel : relsByStartNode.get(leNode.id)) {

						Nodes neighbor = nodes.get(leRel.endNodeId);
						if (labelsAsSet(neighbor).contains("Entity") && labelsAsSet(neighbor).contains("Temporal")) {
							if (isDocumentDate(neighbor)) {
								continue;
							}
								leArgumentsEntities.get(leNode).addAll(gatherArgEntityTokens(neighbor));

						}
						if (labelsAsSet(neighbor).contains("Entity") || labelsAsSet(neighbor).contains("Argument")){
							if(labelsAsSet(neighbor).contains("Salary")){
								leArgumentsEntities.get(leNode).addAll(gatherSalaryTokens(neighbor));
							} else {

								leArgumentsEntities.get(leNode).addAll(gatherArgEntityTokens(neighbor));
							}

						}
					}
				}
		 	for (Rels leRel : relsByEndNode.get(leNode.id) ){
				Nodes neighbor = nodes.get(leRel.startNodeId);
				if (labelsAsSet(neighbor).contains("Entity") && labelsAsSet(neighbor).contains("Temporal")){
					if (isDocumentDate(neighbor)){
						continue;
					}
					leArgumentsEntities.get(leNode).addAll(gatherArgEntityTokens(neighbor));

				}
				if (labelsAsSet(neighbor).contains("Entity") || labelsAsSet(neighbor).contains("Argument")){
					if(labelsAsSet(neighbor).contains("Salary")){
						leArgumentsEntities.get(leNode).addAll(gatherSalaryTokens(neighbor));
					} else {
						leArgumentsEntities.get(leNode).addAll(gatherArgEntityTokens(neighbor));
					}
				}
		 	}
		 }
	}

	public static Set<Nodes> gatherSalaryTokens(Nodes node ){
		Set<Nodes> entityNodes = new HashSet<Nodes>();
		Set<Nodes> tokens = new HashSet<Nodes>();
		Nodes associatedTerm = null;
		for (Rels rel : relsByEndNode.get(node.id)){
			if(rel.type.equals("ElementOf") ){
				if (labelsAsSet(nodes.get(rel.startNodeId)).contains("Entity")
				){
					entityNodes.add(nodes.get(rel.startNodeId));
				}

			}
		}
		for (Nodes elementNode : entityNodes){
			tokens.addAll(gatherArgEntityTokens(elementNode));
		}
		return tokens;
	}

	public static Set<Nodes> gatherTemporalTokens (Nodes node) {
		Set<Nodes> tokens = new HashSet<Nodes>();
		Nodes associatedTerm = null;
		for (Rels rel : relsByEndNode.get(node.id)){
			if(rel.type.equals("ElementOf") ){
				if (labelsAsSet(nodes.get(rel.startNodeId)).contains("Entity")  || labelsAsSet(nodes.get(rel.startNodeId)).contains("Location")){
					continue;
				}
				if(labelsAsSet(nodes.get(rel.startNodeId)).contains("Term") && labelsAsSet(nodes.get(rel.startNodeId)).size() == 1){
					tokens.add(nodes.get(rel.startNodeId));
				} else if (labelsAsSet(nodes.get(rel.startNodeId)).contains("CompoundTerm")){
					associatedTerm = nodes.get(rel.startNodeId);
					break;
				}
			}
		}
		if (associatedTerm != null) {
			if (labelsAsSet(associatedTerm).contains("CompoundTerm")) {
				tokens.addAll(grabOutgoingContains(associatedTerm));
			} else if (labelsAsSet(associatedTerm).contains("Term")) {
				tokens.add(associatedTerm);
			}
		}
		return tokens;
	}

	public static Set<Nodes> gatherLocationTokens (Nodes node) {
		Set<Nodes> tokens = new HashSet<Nodes>();
		Nodes associatedTerm = null;
		for (Rels rel : relsByEndNode.get(node.id)){
			if(rel.type.equals("ElementOf") ){
				if (labelsAsSet(nodes.get(rel.startNodeId)).contains("Entity")  && labelsAsSet(nodes.get(rel.startNodeId)).contains("Location")){
					continue;
				}
				associatedTerm = nodes.get(rel.startNodeId);
				break;
			}
		}
		if(associatedTerm == null){
			System.out.println();
		}
		if (labelsAsSet(associatedTerm).contains("CompoundTerm")){
			tokens.addAll(grabOutgoingContains(associatedTerm));
		} else if (labelsAsSet(associatedTerm).contains("Term")) {
			tokens.add(associatedTerm);
		}
		return tokens;
	}

	public static Set<Nodes> gatherArgEntityTokens(Nodes node){
		Set<Nodes> tokens = new HashSet<Nodes>();
		Nodes associatedTerm = null;
		for (Rels rel : relsByEndNode.get(node.id)){
			if(rel.type.equals("ElementOf") ){
				if (labelsAsSet(nodes.get(rel.startNodeId)).contains("Entity")  && !labelsAsSet(nodes.get(rel.startNodeId)).contains("Location")){
					continue;
				}
				associatedTerm = nodes.get(rel.startNodeId);
			}
		}
		if(associatedTerm == null){
			System.out.println();
		}
		if (labelsAsSet(associatedTerm).contains("CompoundTerm")){
			tokens.addAll(grabOutgoingContains(associatedTerm));
		} else if (labelsAsSet(associatedTerm).contains("Term")) {
			tokens.add(associatedTerm);
		}
		return tokens;
	}

	public static boolean isDocumentDate(Nodes node){
		boolean isDocDate = false;
		for (Rels leRel : relsByEndNode.get(node.id) ) {
			Nodes neighbor = nodes.get(leRel.startNodeId);
			if(labelsAsSet(neighbor).contains("DataSource")){
				isDocDate = true;
			}
		}
		return isDocDate;
	}
	public static Set<String> labelsAsSet(Nodes node){
		Set<String> labels = new HashSet<String>();
		if(node.labels == null){
			return labels;
		}
		for (String label : node.labels){
			labels.add(label);
		}

		return labels;
	}

	public static void classifyNodes(){
		for (Nodes node : doc.nodes) {
			nodes.put(node.id, node);
			if(node.labels.length == 0){
				unlabeledNodes.put(node.id, node);
			}
			for (String label : node.labels) {
				if (label.equals("DocumentText"))
					fileName = node.properties.Filename;
				if (label.equals("CompoundTerm")) {
					compoundTerms.put(node.id, node);
				}
				if (label.contains("Predicate")) {
					eventPredicates.put(node.id, node);
				}
				if ((label.equals("Term") && node.labels.length == 1)) {
					terms.put(node.id, node);
				}
				if (label.equals("VerbalNoun") && node.labels.length == 2) {
					terms.put(node.id, node);
				}
				if (label.equals("State") && node.labels.length == 2) {
					terms.put(node.id, node);
				}
				if (label.equals("LinguisticEvent")) {
					if (!node.properties.Lemma.contains("say") && !node.properties.Lemma.contains("report")
							&& !node.properties.Lemma.contains("tell") ) {
						linguisticEvents.put(node.id, node);

					} else {
						continue;
					}
				}
				if (label.contains("Sentence")) {
					sentenceNodes.put(node.id, node);
				}
			}
		}
	}

	public static void classifyRels() {
		for (Rels rel : doc.rels) {
			Set<Rels> setofStartRels = new HashSet<Rels>();
			relsByStartNode.put(rel.startNodeId, setofStartRels);
			Set<Rels> setofEndRels = new HashSet<Rels>();
			relsByEndNode.put(rel.endNodeId, setofEndRels);
		}
		for (Rels rel : doc.rels) {
			relsByStartNode.get(rel.startNodeId).add(rel);
		}
		for (Rels rel : doc.rels) {
			relsByEndNode.get(rel.endNodeId).add(rel);
		}
	}

	public static void compoundSentenceSets(){
		for (Nodes compound : compoundTerms.values()) {
			Set<Nodes> contained = grabOutgoingContains(compound);
			for (Nodes node : contained) {
				nodesInCompounds.add(node.id);
			}
		}

		for (Nodes node : terms.values()) {
			/*
			 * Go through all terms in the graph, if they exist in a compound term, skip them.
			 * Populate the map of (String of Sentence -> Set of Tokens in sentence)
			 * */
			if (nodesInCompounds.contains(node.id))
				continue;
			if (!docSentences.containsKey(node.properties.SentenceId)) {
				Set<Nodes> toks = new HashSet<Nodes>();
				docSentences.put(node.properties.SentenceId, toks);
			}
		}
	}

	public static void termSentenceSets(){
		for (Nodes compound : compoundTerms.values()) {
			Set<Nodes> contained = grabOutgoingContains(compound);
			for (Nodes node : contained) {
				nodesInCompounds.add(node.id);
			}
		}

		for (Nodes node : terms.values()) {
			/*
			 * Go through all terms in the graph, if they exist in a compound term, skip them.
			 * Populate the map of (String of Sentence -> Set of Tokens in sentence)
			 * */
			if (nodesInCompounds.contains(node.id))
				continue;
			if (!docSentences.containsKey(node.properties.SentenceId)) {
				Set<Nodes> toks = new HashSet<Nodes>();
				docSentences.put(node.properties.SentenceId, toks);
			}
		}
	}

	public static AnansiDoc buildDocument(String fileName){
		file = new File(fileName);
		try {
			wekaClassifier = (Classifier) SerializationHelper.read(model);
		} catch(Exception e){
			System.out.println("Error!");
			System.exit(-2);
		}
		try {
			doc = GSON.fromJson(new String(java.nio.file.Files.readAllBytes(file.toPath())), AnansiDoc.class);
		} catch (IOException e) {
			System.out.println("Failed to read from file.");
		}
		classifyNodes();
		classifyRels();
		compoundSentenceSets();
		classifyAdjacent();


		for (Nodes node :leArgumentsEntities.keySet()){
			System.out.println("Linguistic Event: " + node.properties.Text + " ID: " + node.id);
			for(Nodes nde: leArgumentsEntities.get(node)){
				System.out.println("Argument: " + nde.properties.Text);
			}
		}

		for (Nodes node : terms.values()) {
			docSentences.get(node.properties.SentenceId).add(node);
		}

		for (Nodes node : unlabeledNodes.values()) {
			docSentences.get(node.properties.SentenceId).add(node);
		}

		for (Set<Nodes> setofNodes : docSentences.values()) {
			Token[] toks = new Token[setofNodes.size()];
			String sentenceId = "";
			for (Nodes node : setofNodes){
				System.out.println(node.properties.ElementId);
			}
			for (Nodes node : setofNodes) {

				toks[Integer.parseInt(node.properties.ElementId) - 1] = new Token(Integer.parseInt(node.properties.ElementId), (String) node.properties.Text,
						(String) node.properties.Lemma, (String) node.properties.PartOfSpeech);
				sentenceId = node.properties.SentenceId;
			}

			Sentence sent;
			for (Nodes node : sentenceNodes.values()) {
				if (sentenceId.equals(node.properties.SentenceId)) {
					sent = new Sentence(toks, sentenceId, node.properties.Begin, node.properties.End, (String) node.properties.Text);
					docCorpus.add(sent);
					break;
				}
			}
		}

		for (Nodes linguisticEvent: leArgumentsEntities.keySet()){
			List<Token> evMainText = new LinkedList<Token>();
			Nodes eventpredicate = null;
			Nodes predicate = null;
			Token eventTrigger = null;
			int linguisticEventId = linguisticEvent.id;
			Set<Nodes> eventMainText = new HashSet<Nodes>();
			Set<Nodes> eventMainTextCompounded = new HashSet<Nodes>();
			for (Rels rel : relsByEndNode.get(linguisticEvent.id)) {
				if (rel.type.equals("EventPredicate")) {
					eventpredicate = nodes.get(rel.startNodeId);
					break;
				}
			}
			for (Rels rel : relsByEndNode.get(eventpredicate.id)) {
				if (rel.type.equals("Predicate")) {
					predicate = nodes.get(rel.startNodeId);
					break;
				}
			}
			if (predicate == null) {
				predicate = eventpredicate;
			}
			if (labelsAsSet(predicate).contains("LinguisticSubEvent"))
				continue;
			Token token = new Token(Integer.parseInt(predicate.properties.ElementId), (String) predicate.properties.Text,
					(String) predicate.properties.Lemma, (String) predicate.properties.PartOfSpeech);
			eventTrigger = token;
			if (eventpredicate.properties.Lemma.equals("be")) {
				boolean copRelExists = false;
				Nodes copNode = null;
				for (Rels rel : relsByEndNode.get(eventpredicate.id)) {
					if (rel.properties.Dependency != null) {
						if (rel.properties.Dependency.equals("cop")) {
							eventMainTextCompounded = getBeDeps(eventpredicate);
							for (Nodes node : eventMainTextCompounded){
								boolean compound = false;
								for(String label : node.labels){
									if (label.contains("Compound")){
										compound = true;
									}
								}
								if (compound){
									eventMainText.addAll(grabOutgoingContains(node));
								} else {
									eventMainText.add(node);
								}
							}
							copNode = nodes.get(rel.startNodeId);
							copRelExists = true;
						}
						/*
							if the COP node has a predicate, make it the new event predicate
							Otherwise, the COP node becomes the predicate.
						 */
					}
				}
				if (copRelExists) {
					boolean predicateCOP = false;
					for (String label : copNode.labels) {
						if (label.equals("Predicate")) {
							predicateCOP = true;
						}
					}
					if (predicateCOP) {
						for (Rels relation : relsByEndNode.get(copNode.id)) {
							if (relation.type.equals("Predicate")) {
								predicate = nodes.get(relation.startNodeId);
								break;
							}
						}
						if (predicate == null) {
							predicate = eventpredicate;
						}
						Token tkn = new Token(Integer.parseInt(predicate.properties.ElementId), (String) predicate.properties.Text,
								(String) predicate.properties.Lemma, (String) predicate.properties.PartOfSpeech);
						eventTrigger = tkn;
					} else {
						Token tkn = new Token(Integer.parseInt(copNode.properties.ElementId), (String) copNode.properties.Text,
								(String) copNode.properties.Lemma, (String) copNode.properties.PartOfSpeech);
						eventTrigger = tkn;
					}
				}
				if (!copRelExists) {
					eventMainText = getDeps(eventpredicate);
					eventMainText.addAll(leArgumentsEntities.get(linguisticEvent));
				}
			} else {
				eventMainText.addAll(leArgumentsEntities.get(linguisticEvent));
			}

			for (Nodes node : eventMainText) {
				if (node.labels.length >= 1) {
					if (!StringUtils.containsPunctuation((String) node.properties.Text)) {
						//String originalText, String lemma, String pos
						Token tkn = new Token(Integer.parseInt(node.properties.ElementId), (String) node.properties.Text,
								(String) node.properties.Lemma, (String) node.properties.PartOfSpeech);
						evMainText.add(tkn);

					} else
						continue;
				} else {
					for (Nodes childNode : grabOutgoingContains(node)) {
						if (!StringUtils.containsPunctuation((String) childNode.properties.Text) && childNode.labels.length == 1) {
							//String originalText, String lemma, String pos
							//Token token = new Token(Integer.parseInt(childNode.properties.ElementId), (String) childNode.properties.Text,
							//	(String) childNode.properties.Lemma, (String) childNode.properties.PartOfSpeech);
							//evMainText.add(token);

						} else
							continue;
					}
				}
			}
			evMainText.add(eventTrigger);
			Sentence evSentence = null;
			for (Sentence docSentence : docCorpus) {

				if (docSentence.Begin <= linguisticEvent.properties.Begin && docSentence.End >= linguisticEvent.properties.End) {
					evSentence = docSentence;
					break;
				}
			}


			LinguisticEventNode linguisticEventNode = new LinguisticEventNode(linguisticEventId, evSentence, evMainText, evSentence.text, eventTrigger, fileName);
			linguisticEventSet.add(linguisticEventNode);
		}
		LinguisticEventNode.setDocumentCorpus(docCorpus);
		getAgents(linguisticEventSet);
		getPatients(linguisticEventSet);
		getLocations(linguisticEventSet);
		getOccursOn(linguisticEventSet);
		doc.setLinguisticEvents(linguisticEventSet);

		return doc;
	}
	public static AnansiDoc tetDocument(){

		termChunking = true;
		if (termChunking) {
			try {
				doc = GSON.fromJson(new String(java.nio.file.Files.readAllBytes(file.toPath())), AnansiDoc.class);
			} catch (IOException e) {
				System.out.println("Failed to read from file.");
			}

			/*
				Unbox all the nodes and place them in their respective classifications.
			 */
			classifyNodes();

			/*
			*
			* Unbox relationships and classify them according to their start/end nodes.
			*
			* */
			classifyRels();

			/*
			* Grab all the terms contained in compound terms.
			*
			* */
			compoundSentenceSets();
/*
		for(Nodes node: terms.values()){
			if(nodesInCompounds.contains(node.id))
				continue;
			if(!docSentences.containsKey(node.properties.SentenceId)) {
				Set<Nodes> toks = new HashSet<Nodes>();
				docSentences.put(node.properties.SentenceId, toks);
			}
		}*/

			/*
			*
			* */
			for (Nodes node : terms.values()) {
				docSentences.get(node.properties.SentenceId).add(node);
			}

			for (Nodes node : unlabeledNodes.values()) {
				docSentences.get(node.properties.SentenceId).add(node);
			}


/*
		for(Nodes node: compoundTerms.values()){
			if(node.properties.SentenceId != null)
				docSentences.get(node.properties.SentenceId).add(node);
			else {
				for(Nodes sentence : sentenceNodes.values()){
					if(node.properties.Begin >= sentence.properties.Begin && node.properties.End <= sentence.properties.End){
						docSentences.get(sentence.properties.SentenceId).add(node);
					}
				}
			}
		}
*/
			for (Set<Nodes> setofNodes : docSentences.values()) {
				Token[] toks = new Token[setofNodes.size()];
				String sentenceId = "";

				for (Nodes node : setofNodes) {
					toks[Integer.parseInt(node.properties.ElementId) - 1] = new Token(Integer.parseInt(node.properties.ElementId), (String) node.properties.Text,
							(String) node.properties.Lemma, (String) node.properties.PartOfSpeech);
					sentenceId = node.properties.SentenceId;
				}

				Sentence sent;
				for (Nodes node : sentenceNodes.values()) {
					if (sentenceId.equals(node.properties.SentenceId)) {
						sent = new Sentence(toks, sentenceId, node.properties.Begin, node.properties.End, (String) node.properties.Text);
						docCorpus.add(sent);
						break;
					}
				}


			}

			for (Nodes lingEvent : linguisticEvents.values()) {

				List<Token> evMainText = new LinkedList<Token>();
				Nodes eventpredicate = null;
				Nodes sentence = null;
				Nodes predicate = null;
				Token eventTrigger = null;
				int linguisticEventId = lingEvent.id;
				//(int linguisticEventId, Sentence eventSentence, List<Token> mainEvText, String sentence, Token eventTrigger)
				for (Rels rel : relsByEndNode.get(lingEvent.id)) {
					if (rel.type.equals("EventPredicate")) {
						eventpredicate = nodes.get(rel.startNodeId);
						break;
					}
				}
				for (Rels rel : relsByEndNode.get(eventpredicate.id)) {
					if (rel.type.equals("Predicate")) {
						predicate = nodes.get(rel.startNodeId);
						break;
					}
				}
				if (predicate == null) {
					predicate = eventpredicate;
				}
				Token token = new Token(Integer.parseInt(predicate.properties.ElementId), (String) predicate.properties.Text,
						(String) predicate.properties.Lemma, (String) predicate.properties.PartOfSpeech);
				eventTrigger = token;


/*			for (Nodes node : grabIncoming(linguisticEvents.get(lingEvent.id))) {
				Set<String> labels = new HashSet<String>();


				for (String label : node.labels) {
					labels.add(label);

				}
			if (labels.contains("CompoundTerm") && labels.contains("Predicate")){
					for(Rels rel : relsByEndNode.get(node.id)){
						if (rel.type.equals("EventPredicate") ){
							Nodes compoundeventPredicate = terms.get(rel.startNodeId);
							eventpredicate = compoundeventPredicate;
							Token tkn = new Token(compoundeventPredicate.id, (String) compoundeventPredicate .properties.Text,
									(String) compoundeventPredicate.properties.Lemma, (String) compoundeventPredicate.properties.PartOfSpeech);
							eventTrigger = tkn;
							eventTrigger.pos = compoundeventPredicate.properties.PartOfSpeech;
							lingEvent.properties.PartOfSpeech = compoundeventPredicate.properties.PartOfSpeech;
						}
					}
				}
				else if (labels.contains("Predicate") && labels.contains("Term")) {
					eventpredicate = node;
					Token tkn = new Token(node.id, (String) node.properties.Text,
							(String) node.properties.Lemma, (String) node.properties.PartOfSpeech);
					eventTrigger = tkn;
					eventTrigger.pos = eventpredicate.properties.PartOfSpeech;
					lingEvent.properties.PartOfSpeech = node.properties.PartOfSpeech;
				} else if (labels.contains("State") && labels.contains("Term")) {
				eventpredicate = node;
				Token tkn = new Token(node.id, (String) node.properties.Text,
						(String) node.properties.Lemma, (String) node.properties.PartOfSpeech);
				eventTrigger = tkn;
				eventTrigger.pos = eventpredicate.properties.PartOfSpeech;
				lingEvent.properties.PartOfSpeech = node.properties.PartOfSpeech;


				} else if (labels.contains("Term")){
				eventpredicate = node;
				Token tkn = new Token(node.id, (String) node.properties.Text,
						(String) node.properties.Lemma, (String) node.properties.PartOfSpeech);
				eventTrigger = tkn;
				eventTrigger.pos = eventpredicate.properties.PartOfSpeech;
				lingEvent.properties.PartOfSpeech = node.properties.PartOfSpeech;
				}
			}*/

				//in the case of negated linguistic events


				//for (Rels rel : relsByEndNode.get(eventpredicate.id)) {
				//	if (rel.type.equals("Predicate")) {
				/*	predicate = nodes.get(rel.startNodeId);
					Token token = new Token(Integer.parseInt(nodes.get(rel.startNodeId).properties.ElementId), (String) nodes.get(rel.startNodeId).properties.Text,
							(String) nodes.get(rel.startNodeId).properties.Lemma, (String) nodes.get(rel.startNodeId).properties.PartOfSpeech);
					eventTrigger = token;*/
				//	}
				//}

				Set<Nodes> eventMainText = new HashSet<Nodes>();
				Nodes compoundTerm = null;


				if (eventpredicate.properties.Lemma.equals("be")) {
					boolean copRelExists = false;
					Nodes copNode = null;
					for (Rels rel : relsByEndNode.get(eventpredicate.id)) {
						if (rel.properties.Dependency != null) {
							if (rel.properties.Dependency.equals("cop")) {
								eventMainText = getBeDeps(eventpredicate);
								copNode = nodes.get(rel.startNodeId);
								copRelExists = true;
							}
						/*
							if the COP node has a predicate, make it the new event predicate
							Otherwise, the COP node becomes the predicate.
						 */
						}
					}
					if (copRelExists) {
						boolean predicateCOP = false;
						for (String label : copNode.labels) {
							if (label.equals("Predicate")) {
								predicateCOP = true;
							}
						}
						if (predicateCOP) {
							for (Rels relation : relsByEndNode.get(copNode.id)) {
								if (relation.type.equals("Predicate")) {
									predicate = nodes.get(relation.startNodeId);
									break;
								}
							}
							if (predicate == null) {
								predicate = eventpredicate;
							}
							Token tkn = new Token(Integer.parseInt(predicate.properties.ElementId), (String) predicate.properties.Text,
									(String) predicate.properties.Lemma, (String) predicate.properties.PartOfSpeech);
							eventTrigger = tkn;
						} else {
							Token tkn = new Token(Integer.parseInt(copNode.properties.ElementId), (String) copNode.properties.Text,
									(String) copNode.properties.Lemma, (String) copNode.properties.PartOfSpeech);
							eventTrigger = tkn;
						}
					}
					if (!copRelExists) {
						eventMainText = getDeps(eventpredicate);
					}
				} else {
					eventMainText = getDeps(eventpredicate, predicate);
				}


				for (Nodes node : eventMainText) {
					if (node.labels.length >= 1) {
						if (!StringUtils.containsPunctuation((String) node.properties.Text)) {
							//String originalText, String lemma, String pos
							Token tkn = new Token(Integer.parseInt(node.properties.ElementId), (String) node.properties.Text,
									(String) node.properties.Lemma, (String) node.properties.PartOfSpeech);
							evMainText.add(tkn);

						} else
							continue;
					} else {
						for (Nodes childNode : grabOutgoingContains(node)) {
							if (!StringUtils.containsPunctuation((String) childNode.properties.Text) && childNode.labels.length == 1) {
								//String originalText, String lemma, String pos
								//Token token = new Token(Integer.parseInt(childNode.properties.ElementId), (String) childNode.properties.Text,
								//	(String) childNode.properties.Lemma, (String) childNode.properties.PartOfSpeech);
								//evMainText.add(token);

							} else
								continue;
						}
					}
				}
				evMainText.add(eventTrigger);
				Sentence evSentence = null;
				for (Sentence docSentence : docCorpus) {

					if (docSentence.Begin <= lingEvent.properties.Begin && docSentence.End >= lingEvent.properties.End) {
						evSentence = docSentence;
						break;
					}
				}


				LinguisticEventNode linguisticEvent = new LinguisticEventNode(linguisticEventId, evSentence, evMainText, evSentence.text, eventTrigger, fileName);
				linguisticEventSet.add(linguisticEvent);


			}
			LinguisticEventNode.setDocumentCorpus(docCorpus);
			doc.setLinguisticEvents(linguisticEventSet);
		} else {
			try {
				doc = GSON.fromJson(new String(java.nio.file.Files.readAllBytes(file.toPath())), AnansiDoc.class);
			} catch (IOException e) {
				System.out.println("Failed to read from file.");
			}
			classifyNodes();
			classifyRels();
			compoundSentenceSets();
			for (Nodes node : terms.values()) {
				docSentences.get(node.properties.SentenceId).add(node);
			}

			for (Nodes node : unlabeledNodes.values()) {
				docSentences.get(node.properties.SentenceId).add(node);
			}

			for (Set<Nodes> setofNodes : docSentences.values()) {
				Token[] toks = new Token[setofNodes.size()];
				String sentenceId = "";
				for (Nodes node : setofNodes){
					System.out.println(node.properties.ElementId);
				}
				for (Nodes node : setofNodes) {

					toks[Integer.parseInt(node.properties.ElementId) - 1] = new Token(Integer.parseInt(node.properties.ElementId), (String) node.properties.Text,
							(String) node.properties.Lemma, (String) node.properties.PartOfSpeech);
					sentenceId = node.properties.SentenceId;
				}

				Sentence sent;
				for (Nodes node : sentenceNodes.values()) {
					if (sentenceId.equals(node.properties.SentenceId)) {
						sent = new Sentence(toks, sentenceId, node.properties.Begin, node.properties.End, (String) node.properties.Text);
						docCorpus.add(sent);
						break;
					}
				}
			}

			for (Nodes lingEvent : linguisticEvents.values()) {

				List<Token> evMainText = new LinkedList<Token>();
				Nodes eventpredicate = null;
				Nodes sentence = null;
				Nodes predicate = null;
				Token eventTrigger = null;
				int linguisticEventId = lingEvent.id;
				//(int linguisticEventId, Sentence eventSentence, List<Token> mainEvText, String sentence, Token eventTrigger)
				for (Rels rel : relsByEndNode.get(lingEvent.id)) {
					if (rel.type.equals("EventPredicate")) {
						eventpredicate = nodes.get(rel.startNodeId);
						break;
					}
				}
				for (Rels rel : relsByEndNode.get(eventpredicate.id)) {
					if (rel.type.equals("Predicate")) {
						predicate = nodes.get(rel.startNodeId);
						break;
					}
				}
				if (predicate == null) {
					predicate = eventpredicate;
				}
				Token token = new Token(Integer.parseInt(predicate.properties.ElementId), (String) predicate.properties.Text,
						(String) predicate.properties.Lemma, (String) predicate.properties.PartOfSpeech);
				eventTrigger = token;

				Set<Nodes> eventMainTextCompounded = new HashSet<Nodes>();
				Set<Nodes> eventMainText = new HashSet<Nodes>();
				Nodes compoundTerm = null;


				if (eventpredicate.properties.Lemma.equals("be")) {
					boolean copRelExists = false;
					Nodes copNode = null;
					for (Rels rel : relsByEndNode.get(eventpredicate.id)) {
						if (rel.properties.Dependency != null) {
							if (rel.properties.Dependency.equals("cop")) {
								eventMainTextCompounded = getBeDeps(eventpredicate);
								for (Nodes node : eventMainTextCompounded){
									boolean compound = false;
									for(String label : node.labels){
										if (label.contains("Compound")){
											compound = true;
										}
									}
									if (compound){
										eventMainText.addAll(grabOutgoingContains(node));
									} else {
										eventMainText.add(node);
									}
								}
								copNode = nodes.get(rel.startNodeId);
								copRelExists = true;
							}
						/*
							if the COP node has a predicate, make it the new event predicate
							Otherwise, the COP node becomes the predicate.
						 */
						}
					}
					if (copRelExists) {
						boolean predicateCOP = false;
						for (String label : copNode.labels) {
							if (label.equals("Predicate")) {
								predicateCOP = true;
							}
						}
						if (predicateCOP) {
							for (Rels relation : relsByEndNode.get(copNode.id)) {
								if (relation.type.equals("Predicate")) {
									predicate = nodes.get(relation.startNodeId);
									break;
								}
							}
							if (predicate == null) {
								predicate = eventpredicate;
							}
							Token tkn = new Token(Integer.parseInt(predicate.properties.ElementId), (String) predicate.properties.Text,
									(String) predicate.properties.Lemma, (String) predicate.properties.PartOfSpeech);
							eventTrigger = tkn;
						} else {
							Token tkn = new Token(Integer.parseInt(copNode.properties.ElementId), (String) copNode.properties.Text,
									(String) copNode.properties.Lemma, (String) copNode.properties.PartOfSpeech);
							eventTrigger = tkn;
						}
					}
					if (!copRelExists) {
						eventMainText = getDeps(eventpredicate);
					}
				}/**/ else {
					eventMainTextCompounded = getDeps(eventpredicate);
					for (Nodes node : eventMainTextCompounded){
						boolean compound = false;
						for(String label : node.labels){
							if (label.contains("Compound")){
								compound = true;
							}
						}
						if (compound){
							eventMainText.addAll(grabOutgoingContains(node));
						} else {
							eventMainText.add(node);
						}
					}
				}


				for (Nodes node : eventMainText) {
					if (node.labels.length >= 1) {
						if (!StringUtils.containsPunctuation((String) node.properties.Text)) {
							//String originalText, String lemma, String pos
							Token tkn = new Token(Integer.parseInt(node.properties.ElementId), (String) node.properties.Text,
									(String) node.properties.Lemma, (String) node.properties.PartOfSpeech);
							evMainText.add(tkn);

						} else
							continue;
					} else {
						for (Nodes childNode : grabOutgoingContains(node)) {
							if (!StringUtils.containsPunctuation((String) childNode.properties.Text) && childNode.labels.length == 1) {
								//String originalText, String lemma, String pos
								//Token token = new Token(Integer.parseInt(childNode.properties.ElementId), (String) childNode.properties.Text,
								//	(String) childNode.properties.Lemma, (String) childNode.properties.PartOfSpeech);
								//evMainText.add(token);

							} else
								continue;
						}
					}
				}
				evMainText.add(eventTrigger);
				Sentence evSentence = null;
				for (Sentence docSentence : docCorpus) {

					if (docSentence.Begin <= lingEvent.properties.Begin && docSentence.End >= lingEvent.properties.End) {
						evSentence = docSentence;
						break;
					}
				}


				LinguisticEventNode linguisticEvent = new LinguisticEventNode(linguisticEventId, evSentence, evMainText, evSentence.text, eventTrigger, fileName);
				linguisticEventSet.add(linguisticEvent);


			}
			LinguisticEventNode.setDocumentCorpus(docCorpus);
			doc.setLinguisticEvents(linguisticEventSet);


		}
		return doc;
	}
	private static Set<Nodes> grabOutgoing (Nodes node) {
		Set<Nodes> adjacent = new HashSet<Nodes>();
		for(Rels rel : relsByStartNode.get(node.id)){
			adjacent.add(nodes.get(rel.endNodeId));
		}
		return adjacent;
	}
	private static Set<Nodes> grabOutgoingContains (Nodes node) {
		Set<Nodes> adjacent = new HashSet<Nodes>();
		if(relsByStartNode.get(node.id) != null) {
			for (Rels rel : relsByStartNode.get(node.id)) {
				if (rel.type.contains("Contains"))
					adjacent.add(nodes.get(rel.endNodeId));
			}
		}
		return adjacent;
	}

	private static Set<Nodes> grabOutgoingGoverns (Nodes node) {
		Set<Nodes> adjacent = new HashSet<Nodes>();
		if(relsByStartNode.get(node.id) != null){
			for(Rels rel : relsByStartNode.get(node.id)){
				if(rel.type.contains("Governs")) {
					boolean predicate = false;
					for (String label : nodes.get(rel.endNodeId).labels){
						if(label.equals( "Predicate"))
							predicate = true;
					}
					if (!predicate)
						adjacent.add(nodes.get(rel.endNodeId));
				}
			}
		}

		return adjacent;
	}

	private static Set<Nodes> grabIncoming (Nodes node) {
		Set<Nodes> adjacent = new HashSet<Nodes>();
		for(Rels rel : relsByEndNode.get(node.id)){
			adjacent.add(nodes.get(rel.startNodeId));
		}
		return adjacent;
	}
	private static Set<Nodes> getDeps(Nodes node) {
		Set<Nodes> dependents = new HashSet<Nodes>();
		Set<Nodes> adjacentNodes = new HashSet<Nodes>();

		dependents.add(node);
		adjacentNodes = grabOutgoingGoverns(node);

		for(Nodes depNode: adjacentNodes){
			recDependents(dependents, depNode);
		}

		return dependents;
	}
/*
This is overloaded because in some cases we want to keep the predicate instead since it has an elementID that makes sense.
 */
	private static Set<Nodes> getDeps(Nodes node, Nodes predicate) {
		Set<Nodes> dependents = new HashSet<Nodes>();
		Set<Nodes> adjacentNodes = new HashSet<Nodes>();

		dependents.add(predicate);
		adjacentNodes = grabOutgoingGoverns(node);

		for(Nodes depNode: adjacentNodes){
			recDependents(dependents, depNode);
		}

		return dependents;
	}
	/*
		Method to handle dependents
	 */
	private static Set<Nodes> getBeDeps(Nodes node) {
		Nodes copRel = null;

		Set<Rels> incomingRels = relsByEndNode.get(node.id);

		for (Rels rel : incomingRels){
			if(rel.properties.Dependency != null) {
				if (rel.properties.Dependency.equals("cop")) {
					copRel = nodes.get(rel.startNodeId);
				}
			}
		}

		return getDeps(copRel);
	}

	private static void recDependents(Set<Nodes> dependents, Nodes nodetoExtract){
		Set<Nodes> adjacentNodes = new HashSet<Nodes>();
		if(!dependents.contains(nodetoExtract)) {
			dependents.add(nodetoExtract);
			adjacentNodes = grabOutgoingGoverns(nodetoExtract);

			for (Nodes node : adjacentNodes) {
				recDependents(dependents, node);
			}
		}
	}
	private static final Logger LOGGER = Logger.getLogger(Utils.class.getName());
	public static List<List<EventNode>> testPairs(List<EventNode> eventNodes){
		List<List<EventNode>> pairs = new ArrayList<>();

		for (int i = 0;  i < eventNodes.size() - 1; i++){
			for(int j = i + 1; j < eventNodes.size() ; j++){
				List<EventNode> pair = new ArrayList<>();

				pair.add(eventNodes.get(i));
				pair.add(eventNodes.get(j));
				pairs.add(pair);
			}
		}
		return pairs;
	}
	public static List<List<LinguisticEventNode>> anansiTestPairs(List<LinguisticEventNode> anansiEvents){
		List<List<LinguisticEventNode>> pairs = new LinkedList<List<LinguisticEventNode>>();

		for (int i = 0;  i < anansiEvents.size() - 1; i++){
			for(int j = i + 1; j < anansiEvents.size() ; j++){
				List<LinguisticEventNode> pair = new LinkedList<LinguisticEventNode>();

				pair.add(anansiEvents.get(i));
				pair.add(anansiEvents.get(j));
				pairs.add(pair);
			}
		}
		return pairs;
	}






	public static GeneralTuple<Double, HashMap<HashSet<EventNode>, Double>>
	testClassifier(Classifier clf,
						 LinkedList<GeneralTuple<Instance, EventPair>> test
	) {
		LOGGER.info("Testing classifier and finding optimal prediction cutoff");

		HashMap<HashSet<EventNode>, Double> testPredLog = new HashMap<HashSet<EventNode>, Double>();
		for(GeneralTuple<Instance, EventPair> tup : test) {
			Instance inst = tup.first;
			List<EventNode> pair = tup.second.getLinguisticPair();
			double pred;
			try {
				pred = clf.distributionForInstance(inst)[1];
				tup.second.setCdecScore(pred);
				testPredLog.put(new HashSet<EventNode>(pair), pred);
			} catch (Exception e) {
				e.printStackTrace();
			}

		}

		return new GeneralTuple<Double, HashMap<HashSet<EventNode>, Double>>(0.80, testPredLog);

	}


	public static GeneralTuple<Double, HashMap<HashSet<LinguisticEventNode>, Double>>
	anansitestClassifier(Classifier clf,
						 LinkedList<GeneralTuple<Instance, LinguisticEventPair>> test
						 ) {
		LOGGER.info("Testing classifier and finding optimal prediction cutoff");

		HashMap<HashSet<LinguisticEventNode>, Double> testPredLog = new HashMap<HashSet<LinguisticEventNode>, Double>();
		for(GeneralTuple<Instance, LinguisticEventPair> tup : test) {
			Instance inst = tup.first;
			List<LinguisticEventNode> pair = tup.second.getLinguisticPair();
			double pred;
			try {
				pred = clf.distributionForInstance(inst)[1];
				tup.second.setCdecScore(pred);
				testPredLog.put(new HashSet<LinguisticEventNode>(pair), pred);
			} catch (Exception e) {
				e.printStackTrace();
			}

		}

		return new GeneralTuple<Double, HashMap<HashSet<LinguisticEventNode>, Double>>(0.80, testPredLog);

	}
}
