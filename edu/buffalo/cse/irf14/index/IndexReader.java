/**
 * 
 */
package edu.buffalo.cse.irf14.index;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.hamcrest.core.IsEqual;

import edu.buffalo.cse.irf14.dictionary.AuthorDictionary;
import edu.buffalo.cse.irf14.dictionary.CategoryDictionary;
import edu.buffalo.cse.irf14.dictionary.DocumentDictionary;
import edu.buffalo.cse.irf14.dictionary.PlaceDictionary;
import edu.buffalo.cse.irf14.dictionary.TermDictionary;
import edu.buffalo.cse.irf14.document.Posting;
import edu.buffalo.cse.irf14.document.PostingsList;
import edu.buffalo.cse.irf14.util.Constants;
import edu.buffalo.cse.irf14.util.StringPool;

/**
 * Class that emulates reading data back from a written index
 * @author nikhillo, sghodke, amitpuru
 */
public class IndexReader {
	
	private String rootIndexDir;
	private String indexType;
	private Map<Integer, PostingsList> index;
	private Map<String, Integer> dictionary;
	private Map<Integer, String> reverseDictionary;
	
	// TODO remove this method
	public Map<Integer, PostingsList> getTermIndex() {
		return index;
	}
	/**
	 * Default constructor
	 * @param indexDir : The root directory from which the index is to be read.
	 * This will be exactly the same directory as passed on IndexWriter. In case 
	 * you make subdirectories etc., you will have to handle it accordingly.
	 * @param type The {@link IndexType} to read from
	 */
	@SuppressWarnings("unchecked")
	public IndexReader(String indexDir, IndexType type) {
		//TODO
		rootIndexDir = indexDir;
		indexType = type.toString().toLowerCase();
		try {
			// read the index
			FileInputStream fileInStream= new FileInputStream(rootIndexDir + File.separator + indexType + File.separator + indexType);
			BufferedInputStream bufferedInStream = new BufferedInputStream(fileInStream);
			ObjectInputStream objectInStream = new ObjectInputStream(bufferedInStream);
			index = (HashMap<Integer, PostingsList>) objectInStream.readObject();
			objectInStream.close(); 
			fileInStream.close();
			
			// read the dictionary associated with this index
			fileInStream = new FileInputStream(rootIndexDir + File.separator + Constants.DICTIONARY + File.separator + indexType);
			bufferedInStream = new BufferedInputStream(fileInStream);
			objectInStream = new ObjectInputStream(bufferedInStream);
			dictionary = (Map<String, Integer>) objectInStream.readObject();
			if(indexType.equals(IndexType.TERM.toString().toLowerCase())) {
				TermDictionary.setDictionary(dictionary);
			} else if(indexType.equals(IndexType.AUTHOR.toString().toLowerCase())) {
				AuthorDictionary.setDictionary(dictionary);
			} else if(indexType.equals(IndexType.CATEGORY.toString().toLowerCase())) {
				CategoryDictionary.setDictionary(dictionary);
			} else if(indexType.equals(IndexType.PLACE.toString().toLowerCase())) {
				PlaceDictionary.setDictionary(dictionary);
			}
			objectInStream.close(); 
			fileInStream.close();
			
			// read the reverse dictionary associated with this index
			fileInStream = new FileInputStream(rootIndexDir + File.separator + Constants.DICTIONARY + File.separator + Constants.REVERSE + StringPool.UNDERSCORE + indexType);
			bufferedInStream = new BufferedInputStream(fileInStream);
			objectInStream = new ObjectInputStream(bufferedInStream);
			reverseDictionary = (Map<Integer, String>) objectInStream.readObject();
			if(indexType.equals(IndexType.TERM.toString().toLowerCase())) {
				TermDictionary.setReverseDictionary(reverseDictionary);
			} else if(indexType.equals(IndexType.AUTHOR.toString().toLowerCase())) {
				AuthorDictionary.setReverseDictionary(reverseDictionary);
			} else if(indexType.equals(IndexType.CATEGORY.toString().toLowerCase())) {
				CategoryDictionary.setReverseDictionary(reverseDictionary);
			} else if(indexType.equals(IndexType.PLACE.toString().toLowerCase())) {
				PlaceDictionary.setReverseDictionary(reverseDictionary);
			}
			objectInStream.close(); 
			fileInStream.close();
			
			// read the document dictionary
			fileInStream = new FileInputStream(rootIndexDir + File.separator + Constants.DICTIONARY + File.separator + Constants.DOCUMENT);
			bufferedInStream = new BufferedInputStream(fileInStream);
			objectInStream = new ObjectInputStream(bufferedInStream);
			dictionary = (Map<String, Integer>) objectInStream.readObject();
			DocumentDictionary.setDictionary(dictionary);
			objectInStream.close(); 
			fileInStream.close();
			
			// read the reverse document dictionary
			fileInStream = new FileInputStream(rootIndexDir + File.separator + Constants.DICTIONARY + File.separator + Constants.REVERSE + StringPool.UNDERSCORE + Constants.DOCUMENT);
			bufferedInStream = new BufferedInputStream(fileInStream);
			objectInStream = new ObjectInputStream(bufferedInStream);
			reverseDictionary = (Map<Integer, String>) objectInStream.readObject();
			DocumentDictionary.setReverseDictionary(reverseDictionary);
			objectInStream.close(); 
			fileInStream.close();
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Get total number of terms from the "key" dictionary associated with this 
	 * index. A postings list is always created against the "key" dictionary
	 * @return The total number of terms
	 */
	public int getTotalKeyTerms() {
		if(indexType.equals(IndexType.TERM.toString().toLowerCase())) {
			return TermDictionary.size();
		} else if(indexType.equals(IndexType.AUTHOR.toString().toLowerCase())) {
			return AuthorDictionary.size();
		} else if(indexType.equals(IndexType.CATEGORY.toString().toLowerCase())) {
			return CategoryDictionary.size();
		} else if(indexType.equals(IndexType.PLACE.toString().toLowerCase())) {
			return PlaceDictionary.size();
		}
		return -1;
	}
	
	/**
	 * Get total number of terms from the "value" dictionary associated with this 
	 * index. A postings list is always created with the "value" dictionary
	 * @return The total number of terms
	 */
	public int getTotalValueTerms() {
		return DocumentDictionary.size();
	}
	
	/**
	 * Method to get the postings for a given term. You can assume that
	 * the raw string that is used to query would be passed through the same
	 * Analyzer as the original field would have been.
	 * @param term : The "analyzed" term to get postings for
	 * @return A Map containing the corresponding fileid as the key and the 
	 * number of occurrences as values if the given term was found, null otherwise.
	 */
	public Map<String, Integer> getPostings(String term) {
		Integer termId;
		if(indexType.equals(IndexType.TERM.toString().toLowerCase())) {
			termId = TermDictionary.getTermId(term);
		} else if(indexType.equals(IndexType.AUTHOR.toString().toLowerCase())) {
			termId = AuthorDictionary.getAuthorId(term);
		} else if(indexType.equals(IndexType.CATEGORY.toString().toLowerCase())) {
			termId = CategoryDictionary.getCatId(term);
		} else if(indexType.equals(IndexType.PLACE.toString().toLowerCase())) {
			termId = PlaceDictionary.getPlaceId(term);
		} else {
			return null;
		}
		if(termId == null) {
			return null;
		}
		
		List<Posting> postings = index.get(termId).getPostingsList();
		if(postings == null) {
			return null;
		}
		
		Map<String, Integer> postingMap = new HashMap<String, Integer>();
		for(Posting posting: postings) {
			String fileId = DocumentDictionary.getFileName(posting.getDocId());
			int occurences = posting.getFrequency();
			postingMap.put(fileId, occurences);
		}
		
		return postingMap;
	}
	
	/**
	 * Method to get the top k terms from the index in terms of the total number
	 * of occurrences.
	 * @param k : The number of terms to fetch
	 * @return : An ordered list of results. Must be <=k fr valid k values
	 * null for invalid k values
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public List<String> getTopK(int k) {
		if(k <= 0)
			return null;
		
		List<Posting> postings;
		String term;
		int count, low, mid, high;
		Map<String, Integer> termCount = new HashMap<String, Integer>();
		List<String> termList = new ArrayList<String>();
		Iterator it = index.entrySet().iterator();
		while(it.hasNext()) {
			count = 0;
			Map.Entry<Integer, PostingsList> entry = (Entry<Integer, PostingsList>) it.next();
			if(indexType.equals(IndexType.TERM.toString().toLowerCase())) {
				term = TermDictionary.getTerm(entry.getKey());
			} else if(indexType.equals(IndexType.AUTHOR.toString().toLowerCase())) {
				term = AuthorDictionary.getAuthor(entry.getKey());
			} else if(indexType.equals(IndexType.CATEGORY.toString().toLowerCase())) {
				term = CategoryDictionary.getCategory(entry.getKey());
			} else if(indexType.equals(IndexType.PLACE.toString().toLowerCase())) {
				term = PlaceDictionary.getPlace(entry.getKey());
			} else {
				return null;
			}
			postings = entry.getValue().getPostingsList();
			for(Posting posting: postings) {
				count += posting.getFrequency(); 
			}
			termCount.put(term, count);
			
			low = 0;
			high = termList.size() - 1;
			mid = 0;
			while(low <= high) {
				mid = (low + high)/2;
				if(termCount.get(termList.get(mid)) == count) {
					break;
				} else if(termCount.get(termList.get(mid)) > count) {
					low = mid + 1;
				} else {
					high = mid - 1;
				}
			}
			if(low <= high) {
				termList.add(mid, term);
			} else {
				termList.add(low, term);
			}
		}
		
		List<String> subListToReturn = new ArrayList<String>();
		int elementsToReturn = (termList.size() > k) ? k : termList.size();
		for(int i=0; i<elementsToReturn; i++) {
			subListToReturn.add(i, termList.get(i));
		}
		return subListToReturn;
	}
	
	/**
	 * Method to implement a simple boolean AND query on the given index
	 * @param terms The ordered set of terms to AND, similar to getPostings()
	 * the terms would be passed through the necessary Analyzer.
	 * @return A Map (if all terms are found) containing FileId as the key 
	 * and number of occurrences as the value, the number of occurrences 
	 * would be the sum of occurrences for each participating term. return null
	 * if the given term list returns no results
	 * BONUS ONLY
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Map<String, Integer> query(String...terms) {
		//TODO : BONUS ONLY
		if(terms == null) {
			return null;
		}
		if(!indexType.equals(IndexType.TERM.toString().toLowerCase())) {
			return null;
		}
		
		int totalTerms = terms.length;
		if(totalTerms == 0) {
			return null;
		}
	
		// construct a term vs doc matrix for the given terms
		// i.e. term against the docs in which it appears
		// also construct a term vs termFreqInDoc matrix
		int[][] termDocumentMatrix =  new int[totalTerms][];	
		int[][] termFreqMatrix = new int[totalTerms][];
		List<Posting> postings;
	
		for(int i=0; i<totalTerms; i++) {
			String term = terms[i];
			int termId = TermDictionary.getTermId(term);
			postings = index.get(termId).getPostingsList();
			
			termDocumentMatrix[i] = new int[postings.size()];
			termFreqMatrix[i] = new int[postings.size()];
			for(int j=0; j<postings.size(); j++) {
				Posting posting = postings.get(j);
				termDocumentMatrix[i][j] = posting.getDocId();
				termFreqMatrix[i][j] = posting.getFrequency();
			}
		}
		/*
		// sort the matrix according to increasing number of doc freq
		int[] tempArray;
		int min = 1000, swapPos;
		for(int i=0; i<totalTerms; i++) {
			min = termDocumentMatrix[i].length;
			swapPos = -1;
			for(int j=i+1; j<totalTerms; j++) {
				if(termDocumentMatrix[j].length < min) {
					swapPos = j;
					min = termDocumentMatrix.length;
				}
			}
			if(swapPos != -1) {
				tempArray = termDocumentMatrix[i];
				termDocumentMatrix[i] = termDocumentMatrix[swapPos];
				termDocumentMatrix[swapPos] = tempArray;
			}
		}
		*/

		// add all docs of 1st term to the map of common docs against term frequency
		// and then check if the docs of successive terms appear in this list
		Map<Integer, Integer> commonDocAndFreq = new HashMap<Integer, Integer>();
		for(int i=0; i<termDocumentMatrix[0].length; i++) {
			commonDocAndFreq.put(termDocumentMatrix[0][i], termFreqMatrix[0][i]);
		}
		
		int freq;
		boolean removeTerm;
		for(int k=0; k<termDocumentMatrix[0].length; k++) {
			if(commonDocAndFreq.size() == 0) {
				break;
			}
			freq = commonDocAndFreq.get(termDocumentMatrix[0][k]);
			removeTerm = false;
			for(int i=1; i<totalTerms; i++) {
				int j;
				for(j=0; j<termDocumentMatrix[i].length; j++) {
					if(termDocumentMatrix[0][k] < termDocumentMatrix[i][j]) {
						removeTerm = true;
						break;
					} else if((termDocumentMatrix[0][k] == termDocumentMatrix[i][j])) {
						removeTerm = false;
						freq += termFreqMatrix[i][j];
						break;
					}
				}
				if(removeTerm || j==termDocumentMatrix[i].length) {
					removeTerm = true;
					break;
				}
			}
			commonDocAndFreq.remove(termDocumentMatrix[0][k]);
			if(!removeTerm) {
				commonDocAndFreq.put(termDocumentMatrix[0][k], freq);
			}
		}
		if(commonDocAndFreq.size() == 0) {
			return null;
		}
		Map<String, Integer> resultMatrix = new HashMap<String, Integer>(); 
		Iterator it = commonDocAndFreq.entrySet().iterator();
		String fileId;
		while(it.hasNext()) {
			Map.Entry<Integer, Integer> entry = (Entry<Integer, Integer>) it.next();
			fileId = DocumentDictionary.getFileName(entry.getKey());
			resultMatrix.put(fileId, entry.getValue());
		}
		return resultMatrix;
	}
}
