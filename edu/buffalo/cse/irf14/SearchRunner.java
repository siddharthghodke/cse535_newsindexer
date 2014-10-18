package edu.buffalo.cse.irf14;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import edu.buffalo.cse.irf14.analysis.TokenStream;
import edu.buffalo.cse.irf14.analysis.Tokenizer;
import edu.buffalo.cse.irf14.dictionary.DocumentDictionary;
import edu.buffalo.cse.irf14.document.Document;
import edu.buffalo.cse.irf14.document.FieldNames;
import edu.buffalo.cse.irf14.document.Parser;
import edu.buffalo.cse.irf14.document.ParserException;
import edu.buffalo.cse.irf14.document.Posting;
import edu.buffalo.cse.irf14.index.IndexReader;
import edu.buffalo.cse.irf14.index.IndexType;
import edu.buffalo.cse.irf14.query.Query;
import edu.buffalo.cse.irf14.query.QueryParser;
import edu.buffalo.cse.irf14.query.QueryUtil;
import edu.buffalo.cse.irf14.util.StringPool;

/**
 * Main class to run the searcher.
 * As before implement all TODO methods unless marked for bonus
 * @author nikhillo
 *
 */
public class SearchRunner {
	public enum ScoringModel {TFIDF, OKAPI};
	
	/**
	 * Default (and only public) constuctor
	 * @param indexDir : The directory where the index resides
	 * @param corpusDir : Directory where the (flattened) corpus resides
	 * @param mode : Mode, one of Q or E
	 * @param stream: Stream to write output to
	 */
	public SearchRunner(String indexDir, String corpusDir, 
			char mode, PrintStream stream) {
		//TODO: IMPLEMENT THIS METHOD
	}
	
	/**
	 * Method to execute given query in the Q mode
	 * @param userQuery : Query to be parsed and executed
	 * @param model : Scoring Model to use for ranking results
	 */
	public void query(String userQuery, ScoringModel model) {
		//TODO: IMPLEMENT THIS METHOD
	}
	
	/**
	 * Method to execute queries in E mode
	 * @param queryFile : The file from which queries are to be read and executed
	 */
	public void query(File queryFile) {
		//TODO: IMPLEMENT THIS METHOD
	}
	
	/**
	 * General cleanup method
	 */
	public void close() {
		//TODO : IMPLEMENT THIS METHOD
	}
	
	/**
	 * Method to indicate if wildcard queries are supported
	 * @return true if supported, false otherwise
	 */
	public static boolean wildcardSupported() {
		//TODO: CHANGE THIS TO TRUE ONLY IF WILDCARD BONUS ATTEMPTED
		return false;
	}
	
	/**
	 * Method to get substituted query terms for a given term with wildcards
	 * @return A Map containing the original query term as key and list of
	 * possible expansions as values if exist, null otherwise
	 */
	public Map<String, List<String>> getQueryTerms() {
		//TODO:IMPLEMENT THIS METHOD IFF WILDCARD BONUS ATTEMPTED
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
	
	// TODO remove this method
	public static void main(String[] args) {
		//String userQuery = "Category:War AND Author:Dutt AND Place:Baghdad AND prisoners detainees rebels";
		//String userQuery = "pct mln";
		
		
		//String userQuery = "((MLN NOT Pct) OR dlr) OR U.S.";
		String userQuery = "Place:Washington AND January";
		Query query = QueryParser.parse(userQuery, "OR");
		System.out.println(query.toString());
		//System.out.println("{ Category:War AND Author:Dutt AND Place:Baghdad AND [ Term:prisoners OR Term:detainees OR Term:rebels ] }");
		
		QueryUtil queryUtil = new QueryUtil("/home/IR/newTestIndex");
		List<Posting> resultList = queryUtil.getResult(query.getParsedQuery());
		System.out.println(resultList.size());
		
		List<String> resultSnippets = new ArrayList<String>();
		int numberOfSnippets = resultList.size() > 10 ? 10 : resultList.size();
		for(int i=0; i<numberOfSnippets; i++) {
			String snippet = generateSnippet(resultList.get(i));
			resultSnippets.add(snippet);
		}
		
		for(String snippet: resultSnippets) {
			System.out.println(snippet + StringPool.NEW_LINE + StringPool.NEW_LINE);
		}
		
		/*for(Posting p: resultList) {
			System.out.println(p.getDocFrequencyList().size() + " " + p.getTermFrequencyList().size() + " " + p.getPostionList().size());
		}*/
		/*IndexReader reader = new IndexReader("/home/IR/newTestIndex", IndexType.TERM);
		String queryTerm = "us.*";
		List<String> termMatches = reader.getQueryTerms(queryTerm);
		
		for(String s: termMatches) {
			System.out.println(s);
		}*/
		
	}
	
	// TODO shift this method to appropriate class
	private static String generateSnippet(Posting posting) {
		final int SNIPPET_SIZE = 100; 
		int startOffset = 99999, endOffset = -1;
		String fileId = DocumentDictionary.getFileName(posting.getDocId());
		if(fileId == null) {
			return StringPool.BLANK;
		}
		String fileName = "/home/IR/corpus/" + fileId;
		
		for(List<Integer> positions: posting.getPostionList()) {
			if(positions.get(0) < startOffset) {
				startOffset = positions.get(0);
			}
			if(positions.get(0) > endOffset) {
				endOffset = positions.get(0);
			}
		}
		
		int offsetDiff = endOffset - startOffset;
		if(offsetDiff > SNIPPET_SIZE) {
			startOffset = startOffset - 50;
			endOffset = startOffset + 100;
		} else {
			startOffset -= (SNIPPET_SIZE - offsetDiff) / 2;
			endOffset += (SNIPPET_SIZE - offsetDiff) / 2;
		}
		
		if(startOffset < 0)
			startOffset = 0;
		
		try {
			Document d = Parser.parse(fileName);
			StringBuilder sb = new StringBuilder();
			if(d != null) {
				sb.append(fileId + StringPool.NEW_LINE);
				String[] titleArray = d.getField(FieldNames.TITLE);
				String[] newsDateArray = d.getField(FieldNames.NEWSDATE);
				String[] placeArray = d.getField(FieldNames.PLACE);

				String title = null;
				if(titleArray != null)
					title = titleArray[0];
				if(title != null)
					sb.append(title + StringPool.NEW_LINE);
				String newsDate = null, place = null;
				if(newsDateArray != null)
					newsDate = newsDateArray[0];
				if(placeArray != null)
					place = placeArray[0];
				if(newsDate != null)
					sb.append(newsDate + StringPool.SPACE);
				if(place != null)
					sb.append(place + StringPool.SPACE);
				if(startOffset != 0) {
					sb.append(".....");
				}
				String content = d.getField(FieldNames.CONTENT)[0];
				TokenStream ts = new Tokenizer().consume(content);
				ts.reset();
				int counter = 0;
				while(ts.hasNext()) {
					counter++;
					String token = ts.next().toString();
					if(counter >= startOffset && counter <= endOffset) {
						sb.append(token + StringPool.SPACE);
					}
					if(counter > endOffset) {
						break;
					}
				}
				
				if(counter > endOffset) {
					sb.append(".....");
				}
				
				return sb.toString().trim();
			}
		} catch (ParserException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
		return StringPool.BLANK;
	}
}
