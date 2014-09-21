/**
 * 
 */
package edu.buffalo.cse.irf14.index;

import edu.buffalo.cse.irf14.analysis.Analyzer;
import edu.buffalo.cse.irf14.analysis.TitleAnalyzer;
import edu.buffalo.cse.irf14.analysis.Token;
import edu.buffalo.cse.irf14.analysis.TokenStream;
import edu.buffalo.cse.irf14.analysis.Tokenizer;
import edu.buffalo.cse.irf14.analysis.TokenizerException;
import edu.buffalo.cse.irf14.document.Document;
import edu.buffalo.cse.irf14.document.FieldNames;

/**
 * @author nikhillo
 * Class responsible for writing indexes to disk
 */
public class IndexWriter {
	/**
	 * Default constructor
	 * @param indexDir : The root directory to be sued for indexing
	 */
	Analyzer an;
	TokenStream ts;
	
	public IndexWriter(String indexDir) {
		//TODO : YOU MUST IMPLEMENT THIS
	}
	
	/**
	 * Method to add the given Document to the index
	 * This method should take care of reading the filed values, passing
	 * them through corresponding analyzers and then indexing the results
	 * for each indexable field within the document. 
	 * @param d : The Document to be added
	 * @throws IndexerException : In case any error occurs
	 */
	public void addDocument(Document d) throws IndexerException {
		
		//String title = d.getField(FieldNames.TITLE)[0];
		String title = "The city San Francisco is in California. Is your's IP address 192.168.10.124?";
		System.out.print("in INDEXWRITER#addDocument(): ");
		System.out.println(title);
		try {
			ts = new Tokenizer().consume(title);
		} catch (TokenizerException e) {
			e.printStackTrace();
		}
		an = new TitleAnalyzer(ts);
		ts = an.getStream();
		Token token;
		try {
			while(an.increment()) {
				token = ts.getCurrent();
				if(token != null) {
					System.out.print("[" + token.toString() + "] ");
				}
			}
		} catch (TokenizerException e) {
			e.printStackTrace();
		}
		
	}
	
	/**
	 * Method that indicates that all open resources must be closed
	 * and cleaned and that the entire indexing operation has been completed.
	 * @throws IndexerException : In case any error occurs
	 */
	public void close() throws IndexerException {
		//TODO
	}
}
