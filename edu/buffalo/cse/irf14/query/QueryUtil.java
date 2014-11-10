package edu.buffalo.cse.irf14.query;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import edu.buffalo.cse.irf14.SearchRunner.ScoringModel;
import edu.buffalo.cse.irf14.analysis.Analyzer;
import edu.buffalo.cse.irf14.analysis.AnalyzerFactory;
import edu.buffalo.cse.irf14.analysis.TokenStream;
import edu.buffalo.cse.irf14.analysis.Tokenizer;
import edu.buffalo.cse.irf14.dictionary.TermDictionary;
import edu.buffalo.cse.irf14.document.FieldNames;
import edu.buffalo.cse.irf14.document.Posting;
import edu.buffalo.cse.irf14.document.PostingsList;
import edu.buffalo.cse.irf14.index.IndexReader;
import edu.buffalo.cse.irf14.index.IndexType;
import edu.buffalo.cse.irf14.util.Constants;
import edu.buffalo.cse.irf14.util.ResultDocument;
import edu.buffalo.cse.irf14.util.StringPool;

public class QueryUtil {
	
	static IndexReader termIndexReader;
	static IndexReader categoryIndexReader;
	static IndexReader authorIndexReader;
	static IndexReader placeIndexReader;
	static AnalyzerFactory analyzerFactory;
	static Tokenizer whiteSpaceTokenizer;
	
	static {
		whiteSpaceTokenizer = new Tokenizer();
		analyzerFactory = AnalyzerFactory.getInstance();		
	}
	
	public QueryUtil(String indexDir) {
		termIndexReader = new IndexReader(indexDir, IndexType.TERM);
		categoryIndexReader = new IndexReader(indexDir, IndexType.CATEGORY);
		authorIndexReader = new IndexReader(indexDir, IndexType.AUTHOR);
		placeIndexReader = new IndexReader(indexDir, IndexType.PLACE);
	}
	
	public List<Posting> getResult(String query) {
		TokenStream ts;
		Analyzer termAnalyzer, authorAnalyzer, placeAnalyzer;
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
						token = token.replaceAll(StringPool.DOUBLE_QUOTES, StringPool.BLANK);
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
						} else {
							operatorList.add(Constants.OR);
							ts = whiteSpaceTokenizer.consume(tokenParts[0]);
							termAnalyzer = analyzerFactory.getAnalyzerForField(FieldNames.CONTENT, ts);
							ts = termAnalyzer.getStream();
							termAnalyzer.increment();
							if(ts.getCurrent() != null)
								postingList = termIndexReader.getPostingsList(ts.getCurrent().toString());
							else
								postingList = termIndexReader.getPostingsList(tokenParts[1]);	
						}
						if(postingList != null)
							ps.add(postingList.getPostingsList());
						else
							ps.add(new ArrayList<Posting>());
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
								token = token.replaceAll(StringPool.DOUBLE_QUOTES, StringPool.BLANK);
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
								} else {
									operatorList.add(Constants.OR);
									ts = whiteSpaceTokenizer.consume(tokenParts[0]);
									termAnalyzer = analyzerFactory.getAnalyzerForField(FieldNames.CONTENT, ts);
									ts = termAnalyzer.getStream();
									termAnalyzer.increment();
									if(ts.getCurrent() != null)
										postingList = termIndexReader.getPostingsList(ts.getCurrent().toString());
									else
										postingList = termIndexReader.getPostingsList(tokenParts[1]);	
								}
								if(postingList != null)
									ps.add(postingList.getPostingsList());
								else
									ps.add(new ArrayList<Posting>());
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
							token = token.replaceAll(StringPool.DOUBLE_QUOTES, StringPool.BLANK);
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
							} else {
								operatorList.add(Constants.OR);
								ts = whiteSpaceTokenizer.consume(tokenParts[0]);
								termAnalyzer = analyzerFactory.getAnalyzerForField(FieldNames.CONTENT, ts);
								ts = termAnalyzer.getStream();
								termAnalyzer.increment();
								if(ts.getCurrent() != null)
									postingList = termIndexReader.getPostingsList(ts.getCurrent().toString());
								else
									postingList = termIndexReader.getPostingsList(tokenParts[1]);	
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
		} catch(Exception e) {
			e.printStackTrace();
			return new ArrayList<Posting>();
		}
	}
	
	

	private static List<Posting> getPostingsIntersection(List<List<Posting>> ps, List<String> operators) {
		if(ps == null || operators == null) {
			return null;
		}
		if(ps.size() != operators.size() + 1) {
			return null;
		}
		if(ps.size() == 0) {
			return null;
		}
		
		int i = 0;
		List<Posting> resultList = new ArrayList<Posting>();
		List<Posting> firstTermPostings = ps.get(0);
		Posting resultPosting;
		
		/* 
		 * calculate the tf and df for postings of first term, 
		 * and update it in its postings
		 * And then add the updated list to the resultList
		 */
		for(Posting pos: firstTermPostings) {
			resultPosting = new Posting(pos);
			if(pos.getDocFrequencyList() != null) {
				resultPosting.setDocFrequencyList(pos.getDocFrequencyList());
				resultPosting.setTermFrequencyList(pos.getTermFrequencyList());
				resultPosting.setPositionList(pos.getPostionList());				
			} else {
				resultPosting.addDocFrequency(firstTermPostings.size());
				resultPosting.addTermFrequency(pos.getFrequency());
				resultPosting.addPositionList(pos.getPositions());
			}
			resultList.add(resultPosting);
		}
		if(ps.size() == 1) {
			return resultList;
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
	
	private static List<Posting> postingsAND(List<Posting> a, List<Posting> b) {
		if(a == null || b == null) {
			return null;
		}
		
		if(a.size() == 0 || b.size() == 0) {
			return new ArrayList<Posting>();
		}
		
		List<Posting> resultList = new ArrayList<Posting>();
		Posting aPosting, bPosting, newPosting;
		
		int i = 0, j = 0;
		while(i < a.size() && j < b.size()) {
			aPosting = a.get(i);
			bPosting = b.get(j);
			
			if(aPosting.getDocId() == bPosting.getDocId()) {
				newPosting = new Posting(aPosting);
				
				if(bPosting.getDocFrequencyList() != null) {
					newPosting.appendDocFrequencyList(bPosting.getDocFrequencyList());
					newPosting.appendTermFrequencyList(bPosting.getTermFrequencyList(), false);
					newPosting.appendPositionList(bPosting.getPostionList(), false);
				} else {
					newPosting.addDocFrequency(b.size());
					newPosting.addTermFrequency(bPosting.getFrequency());
					newPosting.addPositionList(bPosting.getPositions());
				}
				
				if(newPosting.getRelevancyScore() != 1d) {
					newPosting.setRelevancyScore(aPosting.getRelevancyScore() + bPosting.getRelevancyScore());
				}
				resultList.add(newPosting);
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
	
	private static List<Posting> postingsOR(List<Posting> a, List<Posting> b) {
		if(a == null || b == null) {
			return null;
		}
		
		List<Posting> resultList = new ArrayList<Posting>();
		
		if(a.size() == 0 && b.size() == 0) {
			return resultList;
		}

		Posting newPosting;
		if(a.size() == 0) {
			for(Posting pos: b) {
				newPosting = new Posting(pos);
				if(pos.getDocFrequencyList() == null) {
					newPosting.addDocFrequency(b.size());
					newPosting.addTermFrequency(pos.getFrequency());
					newPosting.addPositionList(pos.getPositions());
				}
				resultList.add(newPosting);
			}
			return resultList;
		}
		if(b.size() == 0) {
			for(Posting pos: a) {
				newPosting = new Posting(pos);
				if(pos.getDocFrequencyList() == null) {
					newPosting.addDocFrequency(a.size());
					newPosting.addTermFrequency(pos.getFrequency());
					newPosting.addPositionList(pos.getPositions());
				}
				resultList.add(newPosting);
			}
			return resultList;
		}
		
		Posting aPosting, bPosting;
		aPosting = a.get(0);
		bPosting = b.get(0);
		int i = 0, j = 0;
		while(i < a.size() && j < b.size()) {
			aPosting = a.get(i);
			bPosting = b.get(j);
			int aDocId = aPosting.getDocId();
			int bDocId = bPosting.getDocId();
			
			if(aDocId == bDocId) {
				newPosting = new Posting(aPosting);
				if(bPosting.getDocFrequencyList() != null) {
					newPosting.appendDocFrequencyList(bPosting.getDocFrequencyList());
					newPosting.appendTermFrequencyList(bPosting.getTermFrequencyList(), false);
					newPosting.appendPositionList(bPosting.getPostionList(), false);
				} else {
					newPosting.addDocFrequency(b.size());
					newPosting.addTermFrequency(bPosting.getFrequency());
					newPosting.addPositionList(bPosting.getPositions());
				}
				if(newPosting.getRelevancyScore() != 1d) {
					newPosting.setRelevancyScore(aPosting.getRelevancyScore() + bPosting.getRelevancyScore());
				}
				i++;
				j++;
				resultList.add(newPosting);
			} else if(aDocId < bDocId) {
				i++;
				newPosting = new Posting(aPosting);
				
				if(bPosting.getDocFrequencyList() != null) {
					newPosting.appendDocFrequencyList(bPosting.getDocFrequencyList());
					newPosting.appendTermFrequencyList(bPosting.getTermFrequencyList(), true);
					newPosting.appendPositionList(bPosting.getPostionList(), true);
				} else {
					newPosting.addDocFrequency(b.size());
					newPosting.addTermFrequency(0);
					newPosting.addPositionList(new ArrayList<Integer>());
				}
				resultList.add(newPosting);
			} else {
				j++;
				newPosting = new Posting(aPosting);
				newPosting.setDocId(bDocId);
				newPosting.resetTermFrequencyList();
				if(bPosting.getDocFrequencyList() != null) {
					newPosting.appendDocFrequencyList(bPosting.getDocFrequencyList());
					newPosting.appendTermFrequencyList(bPosting.getTermFrequencyList(), false);
					newPosting.appendPositionList(bPosting.getPostionList(), false);
				} else {
					newPosting.addDocFrequency(b.size());
					newPosting.addTermFrequency(bPosting.getFrequency());
					newPosting.addPositionList(bPosting.getPositions());
				}
				newPosting.setRelevancyScore(bPosting.getRelevancyScore());
				resultList.add(newPosting);
			}
		}
		
		while(i < a.size()) {
			aPosting = a.get(i++);
			newPosting = new Posting(aPosting);
			
			if(bPosting.getDocFrequencyList() != null) {
				newPosting.appendDocFrequencyList(bPosting.getDocFrequencyList());
				newPosting.appendTermFrequencyList(bPosting.getTermFrequencyList(), true);
				newPosting.appendPositionList(bPosting.getPostionList(), true);
			} else {
				newPosting.addDocFrequency(b.size());
				newPosting.addTermFrequency(0);
				newPosting.addPositionList(new ArrayList<Integer>());
			}
			resultList.add(aPosting);
		}
		while(j < b.size()) {
			bPosting = b.get(j++);
			newPosting = new Posting(aPosting);
			newPosting.setDocId(bPosting.getDocId());
			newPosting.resetTermFrequencyList();
			if(bPosting.getDocFrequencyList() != null) {
				newPosting.appendDocFrequencyList(bPosting.getDocFrequencyList());
				newPosting.appendTermFrequencyList(bPosting.getTermFrequencyList(), false);
				newPosting.appendPositionList(bPosting.getPostionList(), false);
			} else {
				newPosting.addDocFrequency(b.size());
				newPosting.addTermFrequency(bPosting.getFrequency());
				newPosting.addPositionList(bPosting.getPositions());
			}
			resultList.add(newPosting);
		}
		
		return resultList;
	}
	
	private static List<Posting> postingsNOT(List<Posting> a, List<Posting> b) {
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
		Posting aPosting, bPosting, newPosting;
		bPosting = b.get(0);
		
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
				newPosting = new Posting(aPosting);
				
				if(bPosting.getDocFrequencyList() != null) {
					newPosting.appendDocFrequencyList(bPosting.getDocFrequencyList());
					newPosting.appendTermFrequencyList(bPosting.getTermFrequencyList(), true);
					newPosting.appendPositionList(bPosting.getPostionList(), true);
				} else {
					newPosting.addDocFrequency(b.size());
					newPosting.addTermFrequency(0);
					newPosting.addPositionList(new ArrayList<Integer>());
				}
				resultList.add(newPosting);
			} else {
				j++;
			}
		}
		
		while(i < a.size()) {
			aPosting = a.get(i++);
			newPosting = new Posting(aPosting);
			
			if(bPosting.getDocFrequencyList() != null) {
				newPosting.appendDocFrequencyList(bPosting.getDocFrequencyList());
				newPosting.appendTermFrequencyList(bPosting.getTermFrequencyList(), true);
				newPosting.appendPositionList(bPosting.getPostionList(), true);
			} else {
				newPosting.addDocFrequency(b.size());
				newPosting.addTermFrequency(0);
				newPosting.addPositionList(new ArrayList<Integer>());
			}
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
	
	public Map<String, List<String>> getQueryTerms(String term) {
		
		String newTerm = term.replaceAll("\\*", ".*");
		newTerm = newTerm.replaceAll("\\?", ".?");
		//Pattern pattern = Pattern.compile(term);
		
		Map<String, List<String>> resultMap;
		List<String> expansionTerms = new ArrayList<String>();
		
		//String[] termDictionary = new String[]{"mln", "mla", "mleaesa", "msqla"};
		Set<String> termDictionary = TermDictionary.getDictionary().keySet();
		
		for(String dictTerm: termDictionary) {
			if(Pattern.matches(newTerm, dictTerm)) {
				expansionTerms.add(dictTerm);
			}
		}
		
		if(expansionTerms.size() > 0) {
			resultMap = new HashMap<String, List<String>>();
			resultMap.put(term, expansionTerms);
			return resultMap;
		}
		
		return null;
		
	}
	
	public void generateResultsInEvalMode(File file, PrintStream stream, String indexDir, String corpusDir) {
		
		try {
			//File outFile = new File("/home/IR/phase2/queryOutput");
			BufferedReader br = new BufferedReader(new FileReader(file));
			//BufferedOutputStream bout = new BufferedOutputStream(new FileOutputStream(outFile));
			//writer = new PrintWriter(outFile);
			String line = br.readLine();
			String queryId, queryString;
			Query query;
			List<ResultDocument> results;
			int numOfResults = 0;
			
			int numberOfQueries = Integer.parseInt(line.split("numQueries=")[1]);
			StringBuilder queryResult = new StringBuilder();
			for(int i=0; i<numberOfQueries; i++) {
				line = br.readLine();
				String[] parts = line.split(StringPool.COLON);
				if(parts.length < 2) {
					br.close();
					return;
				}
				queryId = parts[0];
				queryString = parts[1];
				queryString = queryString.replaceAll("\\{", StringPool.BLANK);
				queryString = queryString.replaceAll("\\}", StringPool.BLANK);
				queryString = queryString.trim();
				
				query = new Query(queryString, Constants.OR);
				query.setCorpusDir(corpusDir);
				query.setQueryUtil(this);
				
				results = query.getResultSet(ScoringModel.TFIDF);
				
				//writer.println("numResults=" + results.size());
				if(results.size() > 0) {
					numOfResults++;
					queryResult.append(StringPool.NEW_LINE + queryId + StringPool.COLON);
					queryResult.append("{");
					int j=0;
					for(ResultDocument res: results) {
						if(++j > 10)
							break;
						queryResult.append(res.getResultDocId());
						queryResult.append(StringPool.HASH);
						queryResult.append(String.format("%.5f", res.getRelavanceScore()));
						queryResult.append(StringPool.COMMA);
						queryResult.append(StringPool.SPACE);
					}
					queryResult.deleteCharAt(queryResult.length()-1);
					queryResult.deleteCharAt(queryResult.length()-1);
					queryResult.append("}");
					//writer.println(queryResult.toString());
				}
			}
			br.close();
			if(numOfResults > 0) {
				stream.println("numResults=" + numOfResults + queryResult.toString());
			} else {
				stream.println("numResults=0");
			}
			
			br.close();
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


}
