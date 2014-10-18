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
	private List<Integer> termFrequency;
	private List<Integer> docFrequency;
	private List<List<Integer>> positionList;
	
	public Posting(Posting p) {
		this.docId = p.getDocId();
		this.positions = p.getPositions();
		termFrequency = new ArrayList<Integer>();
		docFrequency = new ArrayList<Integer>();
		positionList = new ArrayList<List<Integer>>();
	}
	
	public Posting(int docId, int position) {
		positions = new ArrayList<Integer>();
		positions.add(position);
		this.docId = docId;
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

	public int getDocId() {
		return docId;
	}
	
	public List<Integer> getPositions() {
		return positions;
	}

	public void addTermFrequency(int tf) {
		termFrequency.add(tf);
	}
	
	public void addDocFrequency(int df) {
		docFrequency.add(df);
	}
	
	public List<Integer> getTermFrequencyList() {
		return termFrequency;
	}
	
	public List<Integer> getDocFrequencyList() {
		return docFrequency;
	}
	
	public void setTermFrequencyList(List<Integer> tfList) {
		termFrequency = tfList;
	}
	
	public void setDocFrequencyList(List<Integer> dfList) {
		docFrequency = dfList;
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
	
}
