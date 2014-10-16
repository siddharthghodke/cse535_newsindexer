package edu.buffalo.cse.irf14.query;

import java.util.ArrayList;
import java.util.List;

import edu.buffalo.cse.irf14.document.Posting;
import edu.buffalo.cse.irf14.document.PostingsList;

public class QueryUtil {
	

	public List<Posting> getPostingsIntersection(List<PostingsList> ps, List<String> operators) {
		if(ps == null || operators == null) {
			return null;
		}
		if(ps.size() != operators.size() + 1) {
			return null;
		}
		if(ps.size() < 2) {
			return null;
		}
		
		int i = 0;
		List<Posting> resultList = new ArrayList<Posting>();
		resultList = ps.get(0).getPostingsList();
		while(i+1 < ps.size()) {
			if(operators.get(i).equals("OR")) {
				resultList = postingsOR(resultList, ps.get(i+1).getPostingsList());  
			} else if(operators.get(i).equals("AND")) {
				resultList = postingsAND(resultList, ps.get(i+1).getPostingsList());
			}
			i++;
		}
		return resultList;
	}
	
	private List<Posting> postingsAND(List<Posting> a, List<Posting> b) {
		if(a == null || b == null) {
			return null;
		}
		
		if(a.size() == 0) {
			return b;
		}
		if(b.size() == 0) {
			return a;
		}
		
		List<Posting> resultList = new ArrayList<Posting>();
		Posting aPosting, bPosting;
		
		int i = 0, j = 0;
		while(i != a.size() || j != b.size()) {
			aPosting = a.get(i);
			bPosting = b.get(j);
			
			if(aPosting.getDocId() == bPosting.getDocId()) {
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
		
		List<Posting> resultList = new ArrayList<Posting>();
		Posting aPosting, bPosting;
		
		int i = 0, j = 0;
		while(i != a.size() || j != b.size()) {
			aPosting = a.get(i);
			bPosting = b.get(j);
			int aDocId = aPosting.getDocId();
			int bDocId = bPosting.getDocId();
			
			if(aDocId == bDocId) {
				resultList.add(aPosting);
				i++;
				j++;
			} else if(aDocId < bDocId) {
				i++;
				resultList.add(aPosting);
			} else {
				j++;
				resultList.add(bPosting);
			}
		}
		
		while(i != a.size()) {
			aPosting = a.get(i++);
			resultList.add(aPosting);
		}
		while(j != b.size()) {
			bPosting = b.get(j++);
			resultList.add(bPosting);
		}
		
		return resultList;
	}
	


}
