package edu.buffalo.cse.irf14.query;

import java.util.ArrayList;
import java.util.List;

import edu.buffalo.cse.irf14.analysis.Analyzer;
import edu.buffalo.cse.irf14.analysis.AnalyzerFactory;
import edu.buffalo.cse.irf14.analysis.TokenStream;
import edu.buffalo.cse.irf14.analysis.Tokenizer;
import edu.buffalo.cse.irf14.document.FieldNames;
import edu.buffalo.cse.irf14.document.Posting;
import edu.buffalo.cse.irf14.document.PostingsList;
import edu.buffalo.cse.irf14.index.IndexReader;
import edu.buffalo.cse.irf14.index.IndexType;
import edu.buffalo.cse.irf14.util.Constants;
import edu.buffalo.cse.irf14.util.StringPool;

public class QueryUtil {
	
	static IndexReader termIndexReader;
	static IndexReader categoryIndexReader;
	static IndexReader authorIndexReader;
	static IndexReader placeIndexReader;
	Analyzer termAnalyzer, authorAnalyzer, categoryAnalyzer, placeAnalyzer;
	AnalyzerFactory analyzerFactory;
	Tokenizer whiteSpaceTokenizer;
	TokenStream ts;
	
	static {
		
	}
	
	public QueryUtil(String indexDir) {
		termIndexReader = new IndexReader(indexDir, IndexType.TERM);
		categoryIndexReader = new IndexReader(indexDir, IndexType.CATEGORY);
		authorIndexReader = new IndexReader(indexDir, IndexType.AUTHOR);
		placeIndexReader = new IndexReader(indexDir, IndexType.PLACE);
		whiteSpaceTokenizer = new Tokenizer();
		analyzerFactory = AnalyzerFactory.getInstance();
	}
	
	public List<Posting> getResult(String query) {
		
		try {
			// base case
			if(!query.contains(StringPool.OPEN_SQUARE_BRACKETS)) {
				List<Posting> resultList;
				List<String> operatorList = new ArrayList<String>();
				List<List<Posting>> ps = new ArrayList<List<Posting>>();
				String[] tokens = query.split(StringPool.SPACE);
				String[] tokenParts;
				String indexType, term;
				PostingsList postingList;
				for(String token: tokens) {
					if(isOperator(token)) {
						operatorList.add(token);
					} else {
						// TODO use pattern matcher
						if(token.contains(StringPool.LESS_THAN)) {
							token = token.replaceAll(StringPool.LESS_THAN, StringPool.BLANK);
							token = token.replaceAll(StringPool.GREATER_THAN, StringPool.BLANK);
							operatorList.remove(operatorList.size() - 1);
							operatorList.add(Constants.NOT);
						}
						tokenParts = token.split(StringPool.COLON);
						if(tokenParts.length == 2) {
							indexType = tokenParts[0];
							term = tokenParts[1];
							ts = whiteSpaceTokenizer.consume(term);
							if(indexType.equalsIgnoreCase(IndexType.AUTHOR.toString())) {
								authorAnalyzer = analyzerFactory.getAnalyzerForField(FieldNames.AUTHOR, ts);
								ts = authorAnalyzer.getStream();
								authorAnalyzer.increment();
								if(ts.getCurrent() != null)
									postingList = authorIndexReader.getPostingsList(ts.getCurrent().toString());
								else
									postingList = authorIndexReader.getPostingsList(term);
							} else if(indexType.equalsIgnoreCase(IndexType.CATEGORY.toString())) {
								postingList = categoryIndexReader.getPostingsList(term);
							} else if(indexType.equalsIgnoreCase(IndexType.PLACE.toString())) {
								placeAnalyzer = analyzerFactory.getAnalyzerForField(FieldNames.PLACE, ts);
								ts = placeAnalyzer.getStream();
								placeAnalyzer.increment();
								if(ts.getCurrent() != null)
									postingList = placeIndexReader.getPostingsList(ts.getCurrent().toString());
								else
									postingList = placeIndexReader.getPostingsList(term);
							} else {
								termAnalyzer = analyzerFactory.getAnalyzerForField(FieldNames.CONTENT, ts);
								ts = termAnalyzer.getStream();
								termAnalyzer.increment();
								if(ts.getCurrent() != null)
									postingList = termIndexReader.getPostingsList(ts.getCurrent().toString());
								else
									postingList = termIndexReader.getPostingsList(term);
							}
							if(postingList != null)
								ps.add(postingList.getPostingsList());
							else
								ps.add(new ArrayList<Posting>());
						}
					}
				}
				
				resultList = getPostingsIntersection(ps, operatorList);
				return resultList;
			}
			else {
				//recursion
				List<Posting> resultList;
				List<String> operatorList = new ArrayList<String>();
				List<List<Posting>> ps = new ArrayList<List<Posting>>();
				String[] tokens, tokenParts;
				String indexType, term;
				PostingsList postingList;
				int indexOfOpenSquareBracket = query.indexOf(StringPool.OPEN_SQUARE_BRACKETS);
				int startIndex = 0;
				do {
					// add the terms before the square bracket (if present)
					String sub1 = query.substring(startIndex, indexOfOpenSquareBracket);
					sub1 = sub1.trim();
					if(!sub1.isEmpty()) {
						tokens = sub1.split(StringPool.SPACE);
						for(String token: tokens) {
							if(isOperator(token)) {
								operatorList.add(token);
							} else {
								if(token.contains(StringPool.LESS_THAN)) {
									token = token.replaceAll(StringPool.LESS_THAN, StringPool.BLANK);
									token = token.replaceAll(StringPool.GREATER_THAN, StringPool.BLANK);
									operatorList.remove(operatorList.size() - 1);
									operatorList.add(Constants.NOT);
								}
								tokenParts = token.split(StringPool.COLON);
								if(tokenParts.length == 2) {
									indexType = tokenParts[0];
									term = tokenParts[1];
									ts = whiteSpaceTokenizer.consume(term);
									if(indexType.equalsIgnoreCase(IndexType.AUTHOR.toString())) {
										authorAnalyzer = analyzerFactory.getAnalyzerForField(FieldNames.AUTHOR, ts);
										ts = authorAnalyzer.getStream();
										authorAnalyzer.increment();
										if(ts.getCurrent() != null)
											postingList = authorIndexReader.getPostingsList(ts.getCurrent().toString());
										else
											postingList = authorIndexReader.getPostingsList(term);
									} else if(indexType.equalsIgnoreCase(IndexType.CATEGORY.toString())) {
										postingList = categoryIndexReader.getPostingsList(term);
									} else if(indexType.equalsIgnoreCase(IndexType.PLACE.toString())) {
										placeAnalyzer = analyzerFactory.getAnalyzerForField(FieldNames.PLACE, ts);
										ts = placeAnalyzer.getStream();
										placeAnalyzer.increment();
										if(ts.getCurrent() != null)
											postingList = placeIndexReader.getPostingsList(ts.getCurrent().toString());
										else
											postingList = placeIndexReader.getPostingsList(term);
									} else {
										termAnalyzer = analyzerFactory.getAnalyzerForField(FieldNames.CONTENT, ts);
										ts = termAnalyzer.getStream();
										termAnalyzer.increment();
										if(ts.getCurrent() != null)
											postingList = termIndexReader.getPostingsList(ts.getCurrent().toString());
										else
											postingList = termIndexReader.getPostingsList(term);
									}
									if(postingList != null)
										ps.add(postingList.getPostingsList());
									else
										ps.add(new ArrayList<Posting>());
								}
							}
						}
					}
					
					// add everything inside this pair of square bracket by using recursion
					int openBr = 0;
					int i = indexOfOpenSquareBracket;
					do {
						char ch = query.charAt(i);
						if(ch == '[') {
							openBr++;
						} else if(ch == ']') {
							openBr--;
						}
						i++;
					} while (openBr != 0);
					
					String queryWithinSquareBracket = query.substring(indexOfOpenSquareBracket + 2, i - 2);
					List<Posting> posList = getResult(queryWithinSquareBracket);
					ps.add(posList);
					indexOfOpenSquareBracket = query.indexOf(StringPool.OPEN_SQUARE_BRACKETS, i+2);
					startIndex = i;
				} while (indexOfOpenSquareBracket != -1);
				
				// add the terms after the square bracket (if present);
				String sub2 = query.substring(startIndex);
				sub2 = sub2.trim();
				if(!sub2.isEmpty()) {
					tokens = sub2.split(StringPool.SPACE);
					for(String token: tokens) {
						if(isOperator(token)) {
							operatorList.add(token);
						} else {
							if(token.contains(StringPool.LESS_THAN)) {
								token = token.replaceAll(StringPool.LESS_THAN, StringPool.BLANK);
								token = token.replaceAll(StringPool.GREATER_THAN, StringPool.BLANK);
								operatorList.remove(operatorList.size() - 1);
								operatorList.add(Constants.NOT);
							}
							tokenParts = token.split(StringPool.COLON);
							if(tokenParts.length == 2) {
								indexType = tokenParts[0];
								term = tokenParts[1];
								ts = whiteSpaceTokenizer.consume(term);
								if(indexType.equalsIgnoreCase(IndexType.AUTHOR.toString())) {
									authorAnalyzer = analyzerFactory.getAnalyzerForField(FieldNames.AUTHOR, ts);
									ts = authorAnalyzer.getStream();
									authorAnalyzer.increment();
									if(ts.getCurrent() != null)
										postingList = authorIndexReader.getPostingsList(ts.getCurrent().toString());
									else
										postingList = authorIndexReader.getPostingsList(term);
								} else if(indexType.equalsIgnoreCase(IndexType.CATEGORY.toString())) {
									postingList = categoryIndexReader.getPostingsList(term);
								} else if(indexType.equalsIgnoreCase(IndexType.PLACE.toString())) {
									placeAnalyzer = analyzerFactory.getAnalyzerForField(FieldNames.PLACE, ts);
									ts = placeAnalyzer.getStream();
									placeAnalyzer.increment();
									if(ts.getCurrent() != null)
										postingList = placeIndexReader.getPostingsList(ts.getCurrent().toString());
									else
										postingList = placeIndexReader.getPostingsList(term);
								} else {
									termAnalyzer = analyzerFactory.getAnalyzerForField(FieldNames.CONTENT, ts);
									ts = termAnalyzer.getStream();
									termAnalyzer.increment();
									if(ts.getCurrent() != null)
										postingList = termIndexReader.getPostingsList(ts.getCurrent().toString());
									else
										postingList = termIndexReader.getPostingsList(term);
								}
								if(postingList != null)
									ps.add(postingList.getPostingsList());
								else
									ps.add(new ArrayList<Posting>());
							}
						}
					}
				}
				
				resultList = getPostingsIntersection(ps, operatorList);
				return resultList;
			}
		} catch(Exception e) {
			e.printStackTrace();
			return new ArrayList<Posting>();
		}
	}
	
	

	public List<Posting> getPostingsIntersection(List<List<Posting>> ps, List<String> operators) {
		if(ps == null || operators == null) {
			return null;
		}
		if(ps.size() != operators.size() + 1) {
			return null;
		}
		if(ps.size() == 0) {
			return null;
		}
		if(ps.size() == 1) {
			return ps.get(0);
		}
		
		int i = 0;
		List<Posting> resultList = new ArrayList<Posting>();
		List<Posting> firstTermPostings = ps.get(0);
		Posting resultPosting;
		
		/* calculate the tf and df for postings of first term, 
		 * and update it in its postings
		 * And then add the updated list to the resultList
		 */
		for(Posting pos: firstTermPostings) {
			resultPosting = new Posting(pos);
			if(pos.getDocFrequencyList() == null) {
				resultPosting.addDocFrequency(firstTermPostings.size());
				resultPosting.addTermFrequency(pos.getFrequency());
				resultPosting.addPositionList(pos.getPositions());
			} else {
				resultPosting.setDocFrequencyList(pos.getDocFrequencyList());
				resultPosting.setTermFrequencyList(pos.getTermFrequencyList());
				resultPosting.setPositionList(pos.getPostionList());
			}
			resultList.add(resultPosting);
		}
		
		while(i+1 < ps.size()) {
			if(operators.get(i).equals(Constants.OR)) {
				resultList = postingsOR(resultList, ps.get(i+1));  
			} else if(operators.get(i).equals(Constants.AND)) {
				resultList = postingsAND(resultList, ps.get(i+1));
			} else if(operators.get(i).equals(Constants.NOT)) {
				resultList = postingsNOT(resultList, ps.get(i + 1));
			}
			i++;
		}
		return resultList;
	}
	
	private List<Posting> postingsAND(List<Posting> a, List<Posting> b) {
		if(a == null || b == null) {
			return null;
		}
		
		if(a.size() == 0 || b.size() == 0) {
			return new ArrayList<Posting>();
		}
		
		List<Posting> resultList = new ArrayList<Posting>();
		Posting aPosting, bPosting;
		
		int i = 0, j = 0;
		while(i < a.size() && j < b.size()) {
			aPosting = a.get(i);
			bPosting = b.get(j);
			
			if(aPosting.getDocId() == bPosting.getDocId()) {
				aPosting.addDocFrequency(b.size());
				aPosting.addTermFrequency(bPosting.getFrequency());
				aPosting.addPositionList(bPosting.getPositions());
				resultList.add(aPosting);
				i++;
				j++;
			} else if(aPosting.getDocId() < bPosting.getDocId()) {
				i++;
			} else {
				j++;
			}
		}
		return resultList;
	}
	
	private List<Posting> postingsOR(List<Posting> a, List<Posting> b) {
		if(a == null || b == null) {
			return null;
		}
		
		if(a.size() == 0) {
			return b;
		}
		if(b.size() == 0) {
			return a;
		}
		
		System.out.println(a.size() + StringPool.SPACE + b.size());
		List<Posting> resultList = new ArrayList<Posting>();
		Posting aPosting, bPosting, newPosting;
		
		int i = 0, j = 0;
		while(i < a.size() && j < b.size()) {
			aPosting = a.get(i);
			bPosting = b.get(j);
			int aDocId = aPosting.getDocId();
			int bDocId = bPosting.getDocId();
			
			if(aDocId == bDocId) {
				aPosting.addDocFrequency(b.size());
				aPosting.addTermFrequency(bPosting.getFrequency());
				aPosting.addPositionList(bPosting.getPositions());
				resultList.add(aPosting);
				i++;
				j++;
			} else if(aDocId < bDocId) {
				i++;
				resultList.add(aPosting);
			} else {
				j++;
				newPosting = new Posting(bPosting);
				if(bPosting.getDocFrequencyList() != null) {
					newPosting.setDocFrequencyList(bPosting.getDocFrequencyList());
					newPosting.setTermFrequencyList(bPosting.getTermFrequencyList());
					newPosting.setPositionList(bPosting.getPostionList());
				} else {
					newPosting.addDocFrequency(b.size());
					newPosting.addTermFrequency(bPosting.getFrequency());
					newPosting.addPositionList(bPosting.getPositions());
				}
				resultList.add(newPosting);
			}
		}
		
		while(i < a.size()) {
			aPosting = a.get(i++);
			resultList.add(aPosting);
		}
		while(j < b.size()) {
			bPosting = b.get(j++);
			newPosting = new Posting(bPosting);
			if(bPosting.getDocFrequencyList() != null) {
				newPosting.setDocFrequencyList(bPosting.getDocFrequencyList());
				newPosting.setTermFrequencyList(bPosting.getTermFrequencyList());
				newPosting.setPositionList(bPosting.getPostionList());
			} else {
				newPosting.addDocFrequency(b.size());
				newPosting.addTermFrequency(bPosting.getFrequency());
				newPosting.addPositionList(bPosting.getPositions());
			}
			resultList.add(newPosting);
		}
		
		return resultList;
	}
	
	private List<Posting> postingsNOT(List<Posting> a, List<Posting> b) {
		if(a == null || b == null) {
			return null;
		}
		
		if(a.size() == 0) {
			return new ArrayList<Posting>();
		}
		if(b.size() == 0) {
			return a;
		}
		
		List<Posting> resultList = new ArrayList<Posting>();
		Posting aPosting, bPosting;
		
		int i = 0, j = 0;
		while(i < a.size() && j < b.size()) {
			aPosting = a.get(i);
			bPosting = b.get(j);
			
			if(aPosting.getDocId() == bPosting.getDocId()) {
				i++;
				j++;
				continue;
			} else if(aPosting.getDocId() < bPosting.getDocId()) {
				i++;
				resultList.add(aPosting);
			} else {
				j++;
			}
		}
		
		while(i < a.size()) {
			aPosting = a.get(i++);
			resultList.add(aPosting);
		}
		return resultList;
	}
	
	
	static boolean isOperator(String str) {
		if(str.equals(Constants.OR) || str.equals(Constants.AND) || str.equals(Constants.NOT)) {
			return true;
		}
		return false;
	}


}
