package common;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class Globals {

	/*
	 * shared constants
	 */

	public static final Path ROOT_DIR = Paths.get(System.getProperty("user.dir")).getParent();
	public static final String CLASSIFIERS_DIR = System.getProperty("user.dir") + "\\classifiers\\";
	public static final Path ECBPLUS_DIR = Paths.get(ROOT_DIR.toString(), "data", "ecb_aug");
	public static final Path RESULTS_DIR = Paths.get(ROOT_DIR.toString(), "data", "results");
	public static final Path PYTHON_DIR = Paths.get(ROOT_DIR.toString(), "python_assets");
	public static final Path CONLL_DIR = Paths.get(ROOT_DIR.toString(), "data", "conll_files");
	public static final Path CORE_NLP = Paths.get(ROOT_DIR.toString(), "cdec", "lib", "stanford-corenlp-full-2018-02-27");
	public static final Path CONLL_SCORER_PATH = Paths.get(ROOT_DIR.toString(), "perl_assets", "reference-coreference-scorers", "scorer.pl");
	public static final Path PY_DOC_CLUSTER_DIR = Paths.get(PYTHON_DIR.toString(), "doc_clustering");
	public static final Path CLEAN_SENT_PATH = Paths.get(ROOT_DIR.toString(), "data", "ECBplus_coreference_sentences.csv");
	public static final Path CACHED_CORE_DOCS = Paths.get(ROOT_DIR.toString(), "data", "cached_core_docs");
	public static final String W2V_SERVER = "http://localhost:8000";

	
	/*
	 * shared flags
	 */
	public static final boolean LEMMATIZE = true;
	public static boolean USED_CACHED_PARSES = true;
	public static boolean USE_TEST_PRED_EVS = false;
	
	
	/*
	 * shared data structures
	 */
	public static final String[] POS = {"N", "V", "R", "J", "CD"};
	public static final HashSet<String> strIgnore = new HashSet<String>(Arrays.asList("nt", "t"));

	

}