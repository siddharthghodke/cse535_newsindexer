package edu.buffalo.cse.irf14.dictionary;

import java.util.HashMap;
import java.util.Map;

public class CategoryDictionary {


	private static Map<String, Integer> catDictionary;
	private static Map<Integer, String> reverseCatDictionary;
	
	private static Integer catId;
	
	static {
		catDictionary = new HashMap<String, Integer>();
		reverseCatDictionary = new HashMap<Integer, String>();
		catId = -1;
	}

	public static Integer addCategory(String key) {
		catDictionary.put(key, ++catId);
		reverseCatDictionary.put(catId, key);
		return catId;
	}
	
	public static Map<String, Integer> getDictionary() {
		return catDictionary;
	}
	
	public static Map<Integer, String> getReverseCatDictionary() {
		return reverseCatDictionary;
	}

	public static Integer getCatId(String key) {
		return catDictionary.get(key);
	}
	
	public static String getCategory(Integer catId) {
		return reverseCatDictionary.get(catId);
	}
	
	public static void setReverseDictionary(Map<Integer, String> reverseDictionary) {
		reverseCatDictionary = reverseDictionary;
	}
	
	public static void setDictionary(Map<String, Integer> dictionary) {
		catDictionary = dictionary;
	}
	
	public static int size() {
		return catDictionary.size();
	}
}
