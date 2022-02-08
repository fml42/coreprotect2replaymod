package co2rm;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

public class BlockMap {

	JSONObject blockMap ;
	
	public BlockMap(File blockMapFile) {
		try {
			blockMap = new JSONObject(new JSONTokener(new FileInputStream(blockMapFile)));
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	public int getMappedId(String material, String[] properties) {
		if (blockMap.has(material)) {
			JSONObject mat = blockMap.getJSONObject(material);
			JSONArray states = mat.getJSONArray("states");
			int anyId = -1;
			int defaultId = -1;
			for (Object blockStateObj : states) {
				JSONObject state = (JSONObject) blockStateObj;
				JSONObject stateProperties = null;
				if (state.has("properties")) {
					stateProperties = state.getJSONObject("properties");
				}
				boolean matchedProperties = true;
				if (properties != null) {
					for (String p : properties) {
						String[] propertySplit = p.split("=");
						if (propertySplit.length >= 2) {
							String propertyKey = propertySplit[0].toLowerCase();
							String propertyVal = propertySplit[1].toLowerCase();
							if (stateProperties == null || !stateProperties.has(propertyKey)) {
								matchedProperties = false;
								break;
							} else {
								String statePropertyValue = stateProperties.getString(propertyKey);
								if (!statePropertyValue.equalsIgnoreCase(propertyVal)) {
									matchedProperties = false;
									break;
								}
							}
						}
					}
				}
				if (matchedProperties && state.has("id")) {
					int id = state.getInt("id");
					anyId = id;
					if (state.has("default") && state.getBoolean("default") == true) {
						defaultId = id;
					}
				}
			}
			if (defaultId != -1) return defaultId;
			else if (anyId != -1) return anyId;
			
		}
		return -1;
	}

	public String getFromStateId(int blockId) {
		Iterator<String> keys = blockMap.keys();
		while(keys.hasNext()) {
		    String key = keys.next();
		    JSONObject block = blockMap.getJSONObject(key);
		    JSONArray states = block.getJSONArray("states");
		    for (Object o: states) {
		    	JSONObject state = (JSONObject) o;
		    	if (state.getInt("id") == blockId) {
		    		List<String> propertyList = new ArrayList<>();
		    		if (state.has("properties")) {
		    			JSONObject properties = state.getJSONObject("properties");
		    			Iterator<String> propertiesIterator = properties.keys();
			    		while(propertiesIterator.hasNext()) {
			    			String propertyKey = propertiesIterator.next();
			    			String propertyVal = properties.getString(propertyKey);
			    			propertyList.add(propertyKey+"="+propertyVal);
			    		}
		    		}
		    		String proptertiesStr = "";
		    		if (propertyList.size() > 0) {
		    			proptertiesStr = "["+String.join(",", propertyList)+"]";
		    		}
		    		return key+proptertiesStr;
		    	}
		    }
		}
		return "???";
	}

	public static String[] propertyListFromJson(JSONObject properties) {
		List<String> blockDataNew = new ArrayList<>();
		Iterator<String> propertiesIterator = properties.keys();
		while(propertiesIterator.hasNext()) {
			String propertyKey = propertiesIterator.next();
			String propertyVal = properties.getString(propertyKey);
			blockDataNew.add(propertyKey+"="+propertyVal);
		}
		return blockDataNew.toArray(new String[0]);
	}

	public static JSONObject propertyListToJson(String[] properties) {
		JSONObject propertiesJson = new JSONObject();
		for (String p : properties) {
			String[] propertySplit = p.split("=");
			if (propertySplit.length >= 2) {
				String propertyKey = propertySplit[0].toLowerCase();
				String propertyVal = propertySplit[1].toLowerCase();
				propertiesJson.put(propertyKey, propertyVal);
			}
		}
		return propertiesJson;
	}

	public JSONObject getDefaultProperties(String materialId) {
		if (blockMap.has(materialId)) {
			JSONObject material = blockMap.getJSONObject(materialId);
			JSONArray states = material.getJSONArray("states");
			for (Object o : states) {
				JSONObject state = (JSONObject) o;
				if (state.has("default") && state.getBoolean("default") == true) {
					if (state.has("properties")) {
						return new JSONObject(new JSONTokener(state.getJSONObject("properties").toString()));
					}
				}
			}
		}
		return null;
	}

	static BlockMap defaultBlockMap;
	public static BlockMap getDefaultMap() {
		return defaultBlockMap;
	}
	public static void setDefaultBlockMap(BlockMap defaultBlockMap) {
		BlockMap.defaultBlockMap = defaultBlockMap;
	}

}
