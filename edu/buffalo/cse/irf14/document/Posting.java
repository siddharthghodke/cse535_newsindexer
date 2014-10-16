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

}
