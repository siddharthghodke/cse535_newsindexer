package edu.buffalo.cse.irf14.document;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Posting implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2753197932831134893L;
	private int docId;
	private List<Integer> positions;
	
	/* term frequency and doc frequency for result posting */
	private List<Integer> termFrequencyList;
	private List<Integer> docFrequencyList;
	private List<List<Integer>> positionList;
	
	private double tfIdfWeight;
	private double relevancyScore;
	
	public Posting() {
		
	}
	
	public Posting(Posting p) {
		this.docId = p.getDocId();
		this.positions = p.getPositions();
		this.relevancyScore = p.getRelevancyScore();
		this.tfIdfWeight = p.getTfIdfWeight();
		
		//this(p.getDocId(), -1);
		if(p.getDocFrequencyList() != null)
			docFrequencyList = new ArrayList<Integer>(p.getDocFrequencyList());
		else
			docFrequencyList = new ArrayList<Integer>();
		
		if(p.getTermFrequencyList() != null)
			termFrequencyList = new ArrayList<Integer>(p.getTermFrequencyList());
		else
			termFrequencyList = new ArrayList<Integer>();
		
		if(p.getPostionList() != null)
			positionList = new ArrayList<List<Integer>>(p.getPostionList());
		else
			positionList = new ArrayList<List<Integer>>();
	}
	
	public Posting(int docId, int position) {
		positions = new ArrayList<Integer>();
		positions.add(position);
		this.docId = docId;
		relevancyScore = tfIdfWeight = 0;
	}
	
	public void add(int docId, int position) {
		this.docId = docId;
		positions.add(position);
	}
	
	public void addPosition(int position) {
		if(!positions.contains(position))
			positions.add(position);
	}
	
	public int getFrequency() {
		return positions.size();
	}

	public void setDocId(int id) {
		docId = id;
	}
	
	public int getDocId() {
		return docId;
	}
	
	public List<Integer> getPositions() {
		return positions;
	}

	public void addTermFrequency(int tf) {
		termFrequencyList.add(tf);
	}
	
	public void addDocFrequency(int df) {
		docFrequencyList.add(df);
	}
	
	public List<Integer> getTermFrequencyList() {
		return termFrequencyList;
	}
	
	public List<Integer> getDocFrequencyList() {
		return docFrequencyList;
	}
	
	public void setTermFrequencyList(List<Integer> tfList) {
		termFrequencyList = tfList;
	}
	
	public void setDocFrequencyList(List<Integer> dfList) {
		docFrequencyList = dfList;
	}
	
	public void addPositionList(List<Integer> posList) {
		positionList.add(posList);
	}
	
	public List<List<Integer>> getPostionList() {
		return positionList;
	}
	
	public void setPositionList(List<List<Integer>> posList) {
		positionList = posList;
	}
	
	public void appendTermFrequencyList(List<Integer> tfList, boolean append0) {
		if(termFrequencyList == null || termFrequencyList.size() == 0) {
			termFrequencyList = tfList;
			return;
		}
		for(int tf: tfList) {
			if(append0)
				termFrequencyList.add(0);
			else
				termFrequencyList.add(tf);
		}
	}
	
	public void appendDocFrequencyList(List<Integer> dfList) {
		if(docFrequencyList == null || docFrequencyList.size() == 0) {
			docFrequencyList = dfList;
			return;
		}
		for(int df: dfList) {
			docFrequencyList.add(df);
		}
	}
	
	public void appendPositionList(List<List<Integer>> posList, boolean appendEmpty) {
		if(positionList == null || positionList.size() == 0) {
			positionList = posList;
			return;
		}
		for(List<Integer> pos: posList) {
			if(appendEmpty)
				positionList.add(new ArrayList<Integer>());
			else
				positionList.add(pos);
		}
	}
	
	public void resetTermFrequencyList() {
		if(termFrequencyList == null) {
			return;
		}
		for(int i=0; i<termFrequencyList.size(); i++) {
			termFrequencyList.set(i, 0);
		}
	}
	
	public double getTfIdfWeight() {
		return tfIdfWeight;
	}

	public void setTfIdfWeight(double tfIdfWeight) {
		this.tfIdfWeight = tfIdfWeight;
	}
	
	public double getRelevancyScore() {
		return relevancyScore;
	}

	public void setRelevancyScore(double relevancyScore) {
		this.relevancyScore = relevancyScore;
	}
	
}
