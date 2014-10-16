package edu.buffalo.cse.irf14.dictionary;

import java.util.HashMap;
import java.util.Map;

public class PlaceDictionary {
	private static Map<String, Integer> placeDictionary;
	private static Map<Integer, String> reversePlaceDictionary;
	private static Integer placeId;
	
	static {
		placeDictionary = new HashMap<String, Integer>();
		reversePlaceDictionary = new HashMap<Integer, String>();
		placeId = -1;
	}

	public static Integer addPlace(String key) {
		placeDictionary.put(key, ++placeId);
		reversePlaceDictionary.put(placeId, key);
		return placeId;
	}
	
	public static Map<String, Integer> getDictionary() {
		return placeDictionary;
	}

	public static Integer getPlaceId(String key) {
		return placeDictionary.get(key);
	}
	
	public static String getPlace(Integer placeId) {
		return reversePlaceDictionary.get(placeId);
	}
	
	public static Map<Integer, String> getReverseDictionary() {
		return reversePlaceDictionary;
	}
	
	public static void setReverseDictionary(Map<Integer, String> reverseDictionary) {
		reversePlaceDictionary = reverseDictionary;
	}
	
	public static void setDictionary(Map<String, Integer> dictionary) {
		placeDictionary = dictionary;
	}
	
	public static int size() {
		return placeDictionary.size();
	}
}
