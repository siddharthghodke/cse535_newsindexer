package edu.buffalo.cse.irf14.index;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;

import edu.buffalo.cse.irf14.dictionary.PlaceDictionary;
import edu.buffalo.cse.irf14.document.PostingsList;
import edu.buffalo.cse.irf14.util.Constants;
import edu.buffalo.cse.irf14.util.StringPool;

public class PlaceIndex implements Index {

	private Map<Integer, PostingsList> index;
	private static final String TYPE = IndexType.PLACE.toString();
		
	public PlaceIndex() {
		index = new HashMap<Integer, PostingsList>();
	}
	
	@Override
	public void add(String token, int docId, int position) {
		PostingsList ps;
		String tokenLowerCase = token.toLowerCase();
		Integer placeId = PlaceDictionary.getPlaceId(tokenLowerCase);
		if(placeId == null) {
			placeId = PlaceDictionary.addPlace(tokenLowerCase);
			ps = new PostingsList();
			ps.insert(docId, position);
			index.put(placeId, ps);
		} else {
			index.get(placeId).insert(docId, position);
		}
	}

	@Override
	public void writeIndexToDisk(String rootIndexDir) throws Exception {
		FileOutputStream fileOutStream;
		ObjectOutputStream objectOutStream;
		BufferedOutputStream bufferOutStream;
		String fileName = rootIndexDir + File.separator + TYPE.toLowerCase() + File.separator + TYPE.toLowerCase();
		
		fileOutStream = new FileOutputStream(fileName);
		bufferOutStream = new BufferedOutputStream(fileOutStream);
		objectOutStream = new ObjectOutputStream(bufferOutStream);
		objectOutStream.writeObject(index);
		objectOutStream.close();
		fileOutStream.close();
		
		// write place dictionary
		fileName = rootIndexDir + File.separator + Constants.DICTIONARY + File.separator + TYPE.toLowerCase();
		fileOutStream = new FileOutputStream(fileName);
		bufferOutStream = new BufferedOutputStream(fileOutStream);
		objectOutStream = new ObjectOutputStream(bufferOutStream);
		objectOutStream.writeObject(PlaceDictionary.getDictionary());
		objectOutStream.close();
		fileOutStream.close();
		
		// write place dictionary
		fileName = rootIndexDir + File.separator + Constants.DICTIONARY + File.separator + Constants.REVERSE  + StringPool.UNDERSCORE + TYPE.toLowerCase();
		fileOutStream = new FileOutputStream(fileName);
		bufferOutStream = new BufferedOutputStream(fileOutStream);
		objectOutStream = new ObjectOutputStream(bufferOutStream);
		objectOutStream.writeObject(PlaceDictionary.getReverseDictionary());
		objectOutStream.close();
		fileOutStream.close();
	}

}
