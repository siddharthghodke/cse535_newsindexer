package edu.buffalo.cse.irf14.query;


import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import edu.buffalo.cse.irf14.SearchRunner.ScoringModel;
import edu.buffalo.cse.irf14.analysis.TokenStream;
import edu.buffalo.cse.irf14.analysis.Tokenizer;
import edu.buffalo.cse.irf14.dictionary.DocumentDictionary;
import edu.buffalo.cse.irf14.document.Document;
import edu.buffalo.cse.irf14.document.FieldNames;
import edu.buffalo.cse.irf14.document.Parser;
import edu.buffalo.cse.irf14.document.ParserException;
import edu.buffalo.cse.irf14.document.Posting;
import edu.buffalo.cse.irf14.util.ResultDocument;
import edu.buffalo.cse.irf14.util.StringPool;

/**
 * Class that represents a parsed query
 * @author nikhillo
 *
 */
public class Query {
	
	private String userQuery;
	//private String defOp;
	private String parsedQuery;
	private List<Posting> resultList;
	private List<Posting> tempList ;
	private String corpusDir;
	private QueryUtil queryUtil;
	private long runTime;
	private Map<String, List<String>> wcExpandedTerms;
	
	public Query(String query, String defOp) {
		this.userQuery = query;
		//this.defOp = defOp;
		if(query.trim().equals(StringPool.BLANK)) {
			parsedQuery = StringPool.BLANK;
		} else {
			QueryBuilder qb = new QueryBuilder(query, defOp);
			parsedQuery = qb.buildQuery();
			wcExpandedTerms = qb.getWCExpandedTerms(); 
		}
	}
	
	
	public List<ResultDocument> getResultSet(ScoringModel model){
		//QueryUtil queryUtil = new QueryUtil("/home/IR/newTestIndex");
		List<Posting> resultPostings;
		List<ResultDocument> resultDocumentList = new ArrayList<ResultDocument>();
		if(userQuery.trim().equals(StringPool.BLANK)) {
			return resultDocumentList;
		}
		
		try {
			long startTime = System.currentTimeMillis();
			resultPostings = queryUtil.getResult(parsedQuery);
			
			if(resultPostings != null && resultPostings.size() > 0) {
				if(model.toString().equalsIgnoreCase("TFIDF"))
					getRankedlistForTfIdf(resultPostings);
				else
					getRankedlistForOkapi(resultPostings);
			}
			else {
				return resultDocumentList;
			}
			
			if(resultList == null || resultList.size() == 0) {
				return resultDocumentList;
			}
			int rank = 0;
			double previousScore = 0;
			for(int i=0; i<resultList.size(); i++) {
				Posting posting = resultList.get(i);
				double relevancyScore = posting.getRelevancyScore();
				if(relevancyScore != previousScore) {
					previousScore = relevancyScore;
					rank++;
				}
				ResultDocument resultDocument = new ResultDocument();
				List<String> idTitleSnippet = generateSnippet(posting);
				
				if(idTitleSnippet != null) {
					resultDocument.setResultDocId(idTitleSnippet.get(0));
					resultDocument.setResultTitle(idTitleSnippet.get(1));
					resultDocument.setResultSnippet(idTitleSnippet.get(2));
				} else {
					resultDocument.setResultDocId(StringPool.BLANK);
					resultDocument.setResultTitle(StringPool.BLANK);
					resultDocument.setResultSnippet(StringPool.BLANK);
				}
				resultDocument.setResultRank(rank);
				resultDocument.setRelavanceScore(relevancyScore);
				
				resultDocumentList.add(resultDocument);
			}
			
			long endTime = System.currentTimeMillis();
			
			runTime = endTime - startTime;
			return resultDocumentList;
			
		} catch (Exception e) {
			return resultDocumentList;
		}
		
	}
	
	
	public void getRankedlistForTfIdf(List<Posting> postingList){
		int numberOfPostings = postingList.size();
		List<Double> idfList = null ; 
		double normalisingTerm = 0;
		resultList = postingList;		
		
		tempList = new ArrayList<Posting>();
		
		idfList = new ArrayList<Double>();
		
		if(resultList != null){
			if(!resultList.isEmpty()){
				
				for(int k=0; k<resultList.get(0).getDocFrequencyList().size(); k++)
				{
					idfList.add(k, (Math.log(DocumentDictionary.size() / resultList.get(0).getDocFrequencyList().get(k))));
				}
				
				// setting tf-idfs for all documents in postings list.
				for(int i=0; i<numberOfPostings; i++){
					double tf_idf = (double)0;
					for(int j=0; j<resultList.get(i).getTermFrequencyList().size(); j++){
						double tf = Math.log(1 + resultList.get(i).getTermFrequencyList().get(j));						
						tf_idf = tf_idf + (tf * idfList.get(j));						
					}//inner for	
					
					if(tf_idf>normalisingTerm)
					{
						normalisingTerm = tf_idf;
					}
				//	normalisingTerm += Math.pow(tf_idf, 2);
					resultList.get(i).setTfIdfWeight(tf_idf);					
					
				}//outer for
				
			//	normalisingTerm = Math.sqrt(normalisingTerm);
				
				// setting relevancy rank for each document
				for(int i=0;i<numberOfPostings;i++)
				{
					resultList.get(i).setRelevancyScore(resultList.get(i).getTfIdfWeight() / normalisingTerm);
				}
										
				for(int count=0; count<resultList.size();count++)
				{					
					tempList.add(resultList.get(count)); 
				}
				
				mergeSort(0, resultList.size() - 1);				
				
			}
		}	// if resultlist != null
		
	}
	
	public void getRankedlistForOkapi(List<Posting> postingList){
		int numberOfTerms = postingList.get(0).getTermFrequencyList().size();
		int numberOfPostings = postingList.size();
		List<Double> idfList = null ; 
		double normalisingTerm = 0;
		int totalLength = 0;
		int avgLength;
		resultList = postingList;		
		double k1 = 1.5;
		double b = 0.75;
		
		tempList = new ArrayList<Posting>();
		
		idfList = new ArrayList<Double>();
		
				
		for (int t : DocumentDictionary.getDocLengthMap().values()) {		    
			totalLength += t; 
		}
		avgLength = totalLength/DocumentDictionary.size();
		
		if(resultList != null){
			if(!resultList.isEmpty()){
				
				for(int k=0; k<numberOfTerms; k++)
				{
					idfList.add(k, (Math.log(DocumentDictionary.size() / resultList.get(0).getDocFrequencyList().get(k))));
				}
				
				
				// setting tf-idfs for all documents in postings list.
				for(int i=0; i<numberOfPostings; i++){
					double tf_idf = (double)0;
					int lengthOfDoc = DocumentDictionary.getDocLength(resultList.get(i).getDocId());
					for(int j=0; j<resultList.get(i).getTermFrequencyList().size(); j++){
						double tf = Math.log(1 + resultList.get(i).getTermFrequencyList().get(j));						
						double numerator = ((1+k1)*tf);
						double term2 = k1 * (1 - b + (b*lengthOfDoc/avgLength));
						double denominator = tf+term2;
						tf_idf = tf_idf + ( (numerator/denominator) * idfList.get(j));						
					}//inner for			
					//normalisingTerm += Math.pow(tf_idf, 2);
					if(tf_idf>normalisingTerm)
					{
						normalisingTerm = tf_idf;
					}
					resultList.get(i).setRelevancyScore(tf_idf);
				}//outer for
				
				//normalisingTerm = Math.sqrt(normalisingTerm);
				// setting relevancy rank for each document
				for(int i=0;i<numberOfPostings;i++)
				{
					resultList.get(i).setRelevancyScore(resultList.get(i).getRelevancyScore() / normalisingTerm);
				}
										
				for(int count=0; count<resultList.size();count++)
				{					
					tempList.add(resultList.get(count)); 
				}
				
				mergeSort(0, resultList.size() - 1);				
				
			}
		}	// if resultlist != null
		
	}
	
		
	
	public void mergeSort( int lowerIndex, int higherIndex)
	{
		if(lowerIndex<higherIndex)
		{
			int middleIndex = (lowerIndex+higherIndex)/2;
			mergeSort(lowerIndex,middleIndex);
			mergeSort(middleIndex+1,higherIndex);
			merge(lowerIndex,middleIndex,higherIndex);

		}
	}
	
	public void merge( int lowerIndex, int middleIndex, int higherIndex)
	{		

		int i=lowerIndex;
		int j = middleIndex+1;
		int k= lowerIndex;
		
		
		for(int index=lowerIndex;index<=higherIndex;index++)
		{
			tempList.set(index, resultList.get(index));
			tempList.get(index).setRelevancyScore(resultList.get(index).getRelevancyScore());
		}

		while((i <= middleIndex) && (j<=higherIndex))
		{
			if(tempList.get(i).getRelevancyScore() < tempList.get(j).getRelevancyScore())
			{
				resultList.set(k++, tempList.get(j++));				
			}
			else
			{				
				resultList.set(k++, tempList.get(i++));
			}
		}

		while(i<=middleIndex)
		{
			resultList.set(k++, tempList.get(i++));
		}	
		
	}

	
	private List<String> generateSnippet(Posting posting) {
		final int SNIPPET_SIZE = 100; 
		int startOffset = 99999, endOffset = -1;
		String fileId = DocumentDictionary.getFileName(posting.getDocId());
		if(fileId == null) {
			return null;
		}
		
		List<String> idTitleSnippet = new ArrayList<String>();
		try {
			idTitleSnippet.add(fileId);
			// TODO comment this
			//String fileName = "/home/IR/corpus/" + fileId;
			String fileName = corpusDir + fileId;
			
			try {
				for(List<Integer> positions: posting.getPostionList()) {
					if(positions.get(0) < startOffset) {
						startOffset = positions.get(0);
					}
					if(positions.get(0) > endOffset) {
						endOffset = positions.get(0);
					}
				}
			} catch (Exception e){
				// do nothing
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
			
			if(startOffset > endOffset) {
				startOffset = 0;
				endOffset = SNIPPET_SIZE;
			}
			try {
				Document d = Parser.parse(fileName);
				StringBuilder sb = new StringBuilder();
				if(d != null) {
					String[] titleArray = d.getField(FieldNames.TITLE);
					String[] newsDateArray = d.getField(FieldNames.NEWSDATE);
					String[] placeArray = d.getField(FieldNames.PLACE);
	
					String title = null;
					if(titleArray != null)
						title = titleArray[0];
					if(title != null)
						idTitleSnippet.add(title);
					else {
						idTitleSnippet.add(StringPool.BLANK);
					}
					
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
					
					idTitleSnippet.add(sb.toString());
					
					return idTitleSnippet;
				}
			} catch (ParserException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
		} catch (Exception e) {
			if(idTitleSnippet.size() == 1) {
				idTitleSnippet.add(StringPool.BLANK);
			}
			if(idTitleSnippet.size() == 2) {
				idTitleSnippet.add(StringPool.BLANK);
				idTitleSnippet.add(StringPool.BLANK);
			}
			return idTitleSnippet;
		}
		
		return null;
	}
	
	
	/**
	 * Method to convert given parsed query into string
	 */
	public String toString() {
		
		if(parsedQuery.charAt(parsedQuery.length()-1) != ' ')
			return "{ " + parsedQuery + " }";
		else
			return "{ " + parsedQuery + "}";
	}
	
	public void setCorpusDir(String corpusDir) {
		if(File.separator.charAt(0) != corpusDir.charAt(corpusDir.length() - 1)) {
			corpusDir = corpusDir + File.separator;
		}
		this.corpusDir = corpusDir;
	}
	
	public void setQueryUtil(QueryUtil queryUtil) {
		this.queryUtil = queryUtil;
	}
	
	public String getUserQuery() {
		return userQuery;
	}
	
	public String getParsedQuery() {
		return parsedQuery;
	}
	
	public long getQueryRuntime() {
		return runTime;
	}
	
	public Map<String, List<String>> getWildCardTerms() {
		return wcExpandedTerms;
	}
}
