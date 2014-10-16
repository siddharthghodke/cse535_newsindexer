package edu.buffalo.cse.irf14.index;

public interface Index {

	public void add(String token, int docId, int position);
	
	public void writeIndexToDisk(String rootIndexDir) throws Exception;
}
