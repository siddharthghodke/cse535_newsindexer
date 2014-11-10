package edu.buffalo.cse.irf14.util;

public class ResultDocument {
	
	private String resultDocId;
	private int resultRank;
	private String resultTitle;
	private double resultRelavanceScore;
	private String resultSnippet;
	
	
	public String getResultDocId() {
		return resultDocId;
	}


	public void setResultDocId(String resultDocId) {
		this.resultDocId = resultDocId;
	}
	
	public String getResultSnippet() {
		return resultSnippet;
	}



	public void setResultSnippet(String resultSnippet) {
		this.resultSnippet = resultSnippet;
	}	
	
	public int getResultRank() {
		return resultRank;
	}



	public void setResultRank(int resultRank) {
		this.resultRank = resultRank;
	}



	public String getResultTitle() {
		return resultTitle;
	}



	public void setResultTitle(String resultTitle) {
		this.resultTitle = resultTitle;
	}



	public double getRelavanceScore() {
		return resultRelavanceScore;
	}



	public void setRelavanceScore(double relavanceScore) {
		this.resultRelavanceScore = relavanceScore;
	}



	public ResultDocument() {
		// TODO Auto-generated constructor stub
	}

	

}
