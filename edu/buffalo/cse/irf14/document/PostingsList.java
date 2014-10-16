package edu.buffalo.cse.irf14.document;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import edu.buffalo.cse.irf14.util.StringPool;

public class PostingsList implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 4129344126034501649L;
	private List<Posting> postingsList;
	
	public PostingsList() {
		postingsList = new ArrayList<Posting>();
	}
	

	public void insert(int docId, int position) {
		/*
		 * using insertion sort with binary search 
		 * to insert the posting at appropriate position
		 */
		int postingsSize = postingsList.size();
		int low = 0, high = postingsSize - 1;
		int mid = (low + high)/2;
		while(low <= high) {
			mid = (low + high)/2;
			if(docId == postingsList.get(mid).getDocId()) {
				break;
			}
			else if (docId < postingsList.get(mid).getDocId()) {
				high = mid - 1;
			}
			else {
				low = mid + 1;
			}
		}
		
		if(low <= high) {
			// posting found. add the position to that posting
			postingsList.get(mid).addPosition(position);
		} else {
			// posting not found, create new posting and add
			postingsList.add(low, new Posting(docId, position));
		}
		
	}
	
	public int getDocFrequency() {
		return postingsList.size();
	}
	
	public List<Posting> getPostingsList() {
		return postingsList;
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		List<Integer> pos;
		for(int i=0; i<postingsList.size(); i++) {
			sb.append(StringPool.OPEN_SQUARE_BRACKETS);
			sb.append(postingsList.get(i).getDocId());
			sb.append(StringPool.COLON);
			pos = postingsList.get(i).getPositions();
			for(Integer p : pos) {
				sb.append(p + StringPool.SPACE);
			}
			sb.append(StringPool.CLOSE_SQUARE_BRACKETS + StringPool.SPACE);
		}
		return sb.toString();
	}
	

}
