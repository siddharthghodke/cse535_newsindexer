package edu.buffalo.cse.irf14.dictionary;

import java.util.HashMap;
import java.util.Map;

public class DocumentDictionary {
	private static Map<String, Integer> docDictionary;
	private static Map<Integer, String> reverseDocDictionary;
	private static Map<Integer, Integer> docLengthMap;
	private static Integer docId;
	
	static {
		docDictionary = new HashMap<String, Integer>();
		reverseDocDictionary = new HashMap<Integer, String>();
		docLengthMap = new HashMap<Integer, Integer>();
		docId = -1;
	}

	public static Integer addDoc(String key) {
		docDictionary.put(key, ++docId);
		reverseDocDictionary.put(docId, key);
		return docId;
	}
	
	public static Map<String, Integer> getDictionary() {
		return docDictionary;
	}
	
	public static Map<Integer, String> getReverseDictionary() {
		return reverseDocDictionary;
	}
	
	public static Integer getDocId(String key) {
		return docDictionary.get(key);
	}
	
	public static String getFileName(int docId) {
		return reverseDocDictionary.get(docId);
	}
	
	public static int size() {
		return docDictionary.size();
	}
	
	public static void setReverseDictionary(Map<Integer, String> reverseDictionary) {
		reverseDocDictionary = reverseDictionary;
	}
	
	public static void setDictionary(Map<String, Integer> dictionary) {
		docDictionary = dictionary;
	}
	
	public static void addDocLength(int docId, int docLength) {
		docLengthMap.put(docId, docLength);
	}
	
	public static Integer getDocLength(int docId) {
		return docLengthMap.get(docId);
	}
	
	public static Map<Integer, Integer> getDocLengthMap() {
		return docLengthMap;
	}
	
	public static void setDocumentLengthMap(Map<Integer, Integer> documentLengthMap) {
		docLengthMap = documentLengthMap;
	}
}
