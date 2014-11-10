package edu.buffalo.cse.irf14;

import java.io.File;
import java.io.PrintStream;
import java.util.List;
import java.util.Map;

import edu.buffalo.cse.irf14.query.Query;
import edu.buffalo.cse.irf14.query.QueryParser;
import edu.buffalo.cse.irf14.query.QueryUtil;
import edu.buffalo.cse.irf14.util.Constants;
import edu.buffalo.cse.irf14.util.ResultDocument;

/**
 * Main class to run the searcher.
 * As before implement all TODO methods unless marked for bonus
 * @author nikhillo
 *
 */
public class SearchRunner {
	public enum ScoringModel {TFIDF, OKAPI};
	
	private QueryUtil queryUtil;
	private String indexDirectory, corpusDirectory;
	PrintStream stream;
	char queryMode;
	Query lastQuery = null;
	
	/**
	 * Default (and only public) constuctor
	 * @param indexDir : The directory where the index resides
	 * @param corpusDir : Directory where the (flattened) corpus resides
	 * @param mode : Mode, one of Q or E
	 * @param stream: Stream to write output to
	 */
	public SearchRunner(String indexDir, String corpusDir, 
			char mode, PrintStream stream) {
		
		queryUtil = new QueryUtil(indexDir);
		indexDirectory = indexDir;
		corpusDirectory = corpusDir;
		queryMode = mode;
		this.stream = stream;
	}

	/**
	 * Method to execute given query in the Q mode
	 * @param userQuery : Query to be parsed and executed
	 * @param model : Scoring Model to use for ranking results
	 */
	public void query(String userQuery, ScoringModel model) {
		try {
			Query query = QueryParser.parse(userQuery, Constants.OR);
			lastQuery = query;
			query.setQueryUtil(queryUtil);
			query.setCorpusDir(corpusDirectory);
			
			stream.println("User query: " + userQuery);
			
			List<ResultDocument> queryResults = query.getResultSet(model);
			if(queryResults.size() == 0) {
				stream.println("\nNo docs matched the given query\n");
				return;
			}
	
			stream.println("Runtime: " + query.getQueryRuntime() + "ms\tTotal Results:" + queryResults.size());
			int i = 1;
			for(ResultDocument result: queryResults) {
				if(i > 10)
					break;
				stream.println();
				stream.println("RANK: " + result.getResultRank());
				stream.println("FileId: " + result.getResultDocId());
				stream.println("RELEVANCY SCORE:" + String.format("%.2f", result.getRelavanceScore()));
				stream.println("TITLE: " + result.getResultTitle());
				stream.println("SNIPPET: " + result.getResultSnippet());
				stream.println("\n-------------------------------------------------------------------------------------------------");
				i++;
			}
			stream.println();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Method to execute queries in E mode
	 * @param queryFile : The file from which queries are to be read and executed
	 */
	public void query(File queryFile) {
		queryUtil.generateResultsInEvalMode(queryFile, stream, indexDirectory, corpusDirectory);
	}
	
	/**
	 * General cleanup method
	 */
	public void close() {
		if(stream != null) 
			stream.close();
	}
	
	/**
	 * Method to indicate if wildcard queries are supported
	 * @return true if supported, false otherwise
	 */
	public static boolean wildcardSupported() {
		//TODO: CHANGE THIS TO TRUE ONLY IF WILDCARD BONUS ATTEMPTED
		return true;
	}
	
	/**
	 * Method to get substituted query terms for a given term with wildcards
	 * @return A Map containing the original query term as key and list of
	 * possible expansions as values if exist, null otherwise
	 */
	public Map<String, List<String>> getQueryTerms() {
		//TODO:IMPLEMENT THIS METHOD IFF WILDCARD BONUS ATTEMPTED
		if(lastQuery != null) {
			return lastQuery.getWildCardTerms();
		}
		return null;
		
	}
	
	/**
	 * Method to indicate if speel correct queries are supported
	 * @return true if supported, false otherwise
	 */
	public static boolean spellCorrectSupported() {
		//TODO: CHANGE THIS TO TRUE ONLY IF SPELLCHECK BONUS ATTEMPTED
		return false;
	}
	
	/**
	 * Method to get ordered "full query" substitutions for a given misspelt query
	 * @return : Ordered list of full corrections (null if none present) for the given query
	 */
	public List<String> getCorrections() {
		//TODO: IMPLEMENT THIS METHOD IFF SPELLCHECK EXECUTED
		return null;
	}
	/*
	public static void main(String[] args) {
		SearchRunner sr = new SearchRunner("/home/IR/newTestIndex/phase2index", "/home/IR/corpus/", 'q', System.out);
		//File inFile = new File("/home/IR/phase2/queries");
		//sr.query(inFile);
		Scanner sc = new Scanner(System.in);
		while(true) {
			String userQuery = sc.nextLine();
			sr.query(userQuery, ScoringModel.TFIDF);
			System.out.println("\n");
			System.out.println("WILDCARD EXPANDED QUERY TERMS:");
			Set<String> wcTerms = sr.getQueryTerms().keySet();
			if(wcTerms != null && wcTerms.size() > 0) {
				for(String wcTerm: wcTerms) {
					System.out.print(wcTerm + ": ");
					for(String expandedTerm: sr.getQueryTerms().get(wcTerm)) {
						System.out.print(expandedTerm + " ");
					}
					System.out.println();
				}
			} else {
				System.out.println("No wildcard terms found!");
			}
			
			
			System.out.println("\n\n\n");
		}
	}
	*/
}
