/**
 * 
 */
package edu.buffalo.cse.irf14.index;

import java.io.File;

import edu.buffalo.cse.irf14.analysis.Analyzer;
import edu.buffalo.cse.irf14.analysis.AnalyzerFactory;
import edu.buffalo.cse.irf14.analysis.Token;
import edu.buffalo.cse.irf14.analysis.TokenStream;
import edu.buffalo.cse.irf14.analysis.Tokenizer;
import edu.buffalo.cse.irf14.analysis.TokenizerException;
import edu.buffalo.cse.irf14.dictionary.DocumentDictionary;
import edu.buffalo.cse.irf14.document.Document;
import edu.buffalo.cse.irf14.document.FieldNames;
import edu.buffalo.cse.irf14.util.Constants;
import edu.buffalo.cse.irf14.util.StringPool;

/**
 * Class responsible for writing indexes to disk
 * @author nikhillo, sghodke, amitpuru
 */
public class IndexWriter {

	Analyzer an;
	AnalyzerFactory analyzerFactory;
	TokenStream ts;
	TermIndex termIndex;
	CategoryIndex categoryIndex;
	PlaceIndex placeIndex;
	AuthorIndex authorIndex;
	String rootIndexDir;
	Tokenizer whiteSpaceTokenizer;
	
	/**
	 * Default constructor
	 * @param indexDir : The root directory to be sued for indexing
	 */
	
	public IndexWriter(String indexDir) {
		termIndex = new TermIndex();
		categoryIndex = new CategoryIndex();
		authorIndex = new AuthorIndex();
		placeIndex = new PlaceIndex();
		rootIndexDir = indexDir;
		whiteSpaceTokenizer = new Tokenizer();
		analyzerFactory = AnalyzerFactory.getInstance();

		// create folders for all index types:
		File dir;
		IndexType[] values = IndexType.values();
		for(IndexType val: values) {
			dir = new File(indexDir + File.separator + val.toString().toLowerCase());
			if(!dir.exists()) {
				if(!dir.mkdirs()) {
					System.err.println("Failed to create the requested directory.\nPlease check the permissions.");
				}
			}
		}
		
		// create folder for dictionary
		dir = new File(rootIndexDir + File.separator + Constants.DICTIONARY);
		if(!dir.exists()) {
			if(!dir.mkdir()) {
				System.out.println("Failed to create the requested directory.\nPlease check the permissions.");
			}
		}
		System.setProperty("INDEX.DIR", indexDir);
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
		
		Integer docId;
		String fileId;
		String[] titleArray = d.getField(FieldNames.TITLE);
		String[] categoryArray = d.getField(FieldNames.CATEGORY);
		String[] authorArray = d.getField(FieldNames.AUTHOR);
		String[] orgArray = d.getField(FieldNames.AUTHORORG);
		String[] placeArray = d.getField(FieldNames.PLACE);
		String[] contentArray = d.getField(FieldNames.CONTENT);
		String[] newsDateArray = d.getField(FieldNames.NEWSDATE);
		String title = StringPool.BLANK, category = StringPool.BLANK;
		String author = StringPool.BLANK, place = StringPool.BLANK;
		String content = StringPool.BLANK, org = StringPool.BLANK;
		String newsDate = StringPool.BLANK;
		if(titleArray != null) 
			title = titleArray[0];
		if(categoryArray != null)
			category = categoryArray[0];
		if(authorArray != null)
			author = authorArray[0];
		if(placeArray != null)
			place = placeArray[0];
		if(contentArray != null) 
			content = contentArray[0];
		if(orgArray != null)
			org = orgArray[0];
		if(newsDateArray != null) {
			newsDate = newsDateArray[0];
		}
		
		// flag to check if the current doc has already been processed and indexed
		boolean isNewDoc = true;
		fileId = d.getField(FieldNames.FILEID)[0];
		
		try {
			docId = DocumentDictionary.getDocId(fileId);
			if(docId == null) {
				docId = DocumentDictionary.addDoc(fileId);
			} else {
				isNewDoc = false;
			}
			
			
			// analyze TITLE
			{
				if(isNewDoc && !title.isEmpty() && !StringPool.BLANK.equals(title)) {
					ts = whiteSpaceTokenizer.consume(title);
					an = analyzerFactory.getAnalyzerForField(FieldNames.TITLE, ts);
					ts = an.getStream();
					Token token;
					while(an.increment()) {
						token = ts.getCurrent();
						if(token != null) {
							termIndex.add(token.toString(), docId, token.getPos());
						}
					}
				}
			}
			
			// analyzer CATEGORY
			{
				if(!category.isEmpty() && !StringPool.BLANK.equals(category)) {
					categoryIndex.add(category, docId, -1);
				}
			}
			
			// analyze CONTENT
			{
				if(isNewDoc && !content.isEmpty() && !StringPool.BLANK.equals(content)) {
					ts = whiteSpaceTokenizer.consume(content);
					an = analyzerFactory.getAnalyzerForField(FieldNames.CONTENT, ts);
					ts = an.getStream();
					Token token;
					while(an.increment()) {
						token = ts.getCurrent();
						if(token != null) {
							termIndex.add(token.toString(), docId, token.getPos());
							
						}
					}
				}
			}
			
			// analyze AUTHOR and ORG together
			{
				if(isNewDoc && !author.isEmpty() && !StringPool.BLANK.equals(author)) {
					ts = whiteSpaceTokenizer.consume(author + StringPool.SPACE + org);
					an = analyzerFactory.getAnalyzerForField(FieldNames.AUTHOR, ts);
					ts = an.getStream();
					Token token;
					while(an.increment()) {
						token = ts.getCurrent();
						if(token != null) {
							authorIndex.add(token.toString(), docId, -1);
						}
					}
				}
			}

			// analyze PLACE
			{
				if(!place.isEmpty() && !StringPool.BLANK.equals(place)) {
					ts = whiteSpaceTokenizer.consume(place);
					an = analyzerFactory.getAnalyzerForField(FieldNames.PLACE, ts);
					ts = an.getStream();
					Token token;
					while(an.increment()) {
						token = ts.getCurrent();
						if(token != null) {
							placeIndex.add(token.toString(), docId, -1);

						}
					}
				}
			}
			
			// analyze NEWSDATE
			{
				if(isNewDoc && !newsDate.isEmpty() && !StringPool.BLANK.equals(newsDate)) {
					ts = whiteSpaceTokenizer.consume(newsDate);
					an = analyzerFactory.getAnalyzerForField(FieldNames.NEWSDATE, ts);
					ts = an.getStream();
					Token token;
					if(ts.hasNext()) {
						token = ts.next();
						if(token != null) {
							termIndex.add(token.toString(), docId, -1);

						}
					}
				}
			}

			
		} catch (TokenizerException e) {
			throw new IndexerException(e.getMessage());
		} catch (Exception e) {
			throw new IndexerException(e.getMessage());
		}
	}
	
	/**
	 * Method that indicates that all open resources must be closed
	 * and cleaned and that the entire indexing operation has been completed.
	 * @throws IndexerException : In case any error occurs
	 */
	public void close() throws IndexerException {
		try {
			termIndex.writeIndexToDisk(rootIndexDir);
			categoryIndex.writeIndexToDisk(rootIndexDir);
			authorIndex.writeIndexToDisk(rootIndexDir);
			placeIndex.writeIndexToDisk(rootIndexDir);
		} catch (Exception e) {
			throw new IndexerException(e.getMessage());
		}
	}
}
