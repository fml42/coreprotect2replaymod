package co2rm;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

public class CoDatabase {
	
	Connection conn;

	HashMap<Integer, String> materialMap;
	HashMap<Integer, String> blockDataMap;
	HashMap<Integer, String> userMap;
	HashMap<Integer, String> userMapUUID;
	HashMap<Integer, String> worldMap;
	
	public CoDatabase(File coDatabaseFile) {
	
		try {
			conn = DriverManager.getConnection("jdbc:sqlite:"+coDatabaseFile.getAbsolutePath().replace("\\", "/"));
			
			materialMap = new HashMap<>();
			blockDataMap = new HashMap<>();
			userMap = new HashMap<>();
			userMapUUID = new HashMap<>();
			worldMap = new HashMap<>();
			
			//loadMaterialMap();
			loadIntStrMap("co_material_map", "material", materialMap);
			
			//loadBlockData();
			loadIntStrMap("co_blockdata_map", "data", blockDataMap);
			
			//loadUserMap();
			loadIntStrMap("co_user", "user", userMap);
			loadIntStrMap("co_user", "uuid", userMapUUID);
			
			//loadWorldMap();
			loadIntStrMap("co_world", "world", worldMap);
			
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
		
	}
	
	private void loadIntStrMap(String table, String key, HashMap<Integer, String> map) {
		try {
			Statement stat = conn.createStatement();
			ResultSet rs = stat.executeQuery("select * from "+table+";");
	        while (rs.next()) {
	        	System.out.println("co map ["+table+"] : "+rs.getInt("id")+" -> "+rs.getString(key));
	        	map.put(rs.getInt("id"), rs.getString(key));
	        }
	        rs.close();
		} catch (Exception e) {
			e.printStackTrace();
		}	
	}
	
	
	static final String BLOCK_TABLE = "co_block";
	List<CoBlockEntry> loadBlockHistory(String where) {
		List<CoBlockEntry> entries = new ArrayList<CoBlockEntry>();
		
		try {
			Statement stat = conn.createStatement();
			ResultSet rs = stat.executeQuery("select * from "+BLOCK_TABLE+(where!=null?" where "+where:"")+" order by time asc;");
	        while (rs.next()) {
	        	CoBlockEntry be = new CoBlockEntry(
	        			rs.getInt("time"), 
	        			rs.getInt("user"), 
	        			rs.getInt("wid"), 
	        			rs.getInt("x"), 
	        			rs.getInt("y"), 
	        			rs.getInt("z"), 
	        			rs.getInt("type"), 
	        			rs.getInt("data"), 
	        			rs.getString("meta"), 
	        			rs.getString("blockdata"), 
	        			rs.getInt("action"), 
	        			rs.getInt("rolled_back")
        			);
	        	entries.add(be);
	        }
	        rs.close();
		} catch (Exception e) {
			e.printStackTrace();
		}	
		
		return entries;
	}
	

	void close() {
		try {
			conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public static String positionalWhereAxis(int a, int b, String coord) {
		int min = Math.min(a,b);
		int max = Math.max(a,b);
		return "and "+coord+">="+min+" and "+coord+"<="+max+" ";
	}
	public static String positionalWhere(Position bb1, Position bb2) {
		String where = "";
		where += positionalWhereAxis(bb1.x, bb2.x, "x");
		where += positionalWhereAxis(bb1.y, bb2.y, "y");
		where += positionalWhereAxis(bb1.z, bb2.z, "z");
		return where;
	}

	public int findWorld(String world) {
		return findId(worldMap, world);
	}
	
	public int findUser(String user) {
		return findId(userMap, user);
	}
	
	public int findId(HashMap<Integer, String> map, String search) {
		int id = -1;
		for (Entry<Integer, String> entry: map.entrySet()) {
			if (entry.getValue().equals(search)) {
				id = entry.getKey();
				break;
			}
		}
		return id;
	}

	public String getMaterial(int type) {
		return materialMap.containsKey(type) ? materialMap.get(type) : null;
	}
	
	public String getBlockData(int id) {
		return blockDataMap.containsKey(id) ? blockDataMap.get(id) : null;
	}
	
	public String getUsername(int user) {
		return userMap.containsKey(user) ? userMap.get(user) : null;
	}
	
	public String[] getBlockData(String blockdata) {
		if (blockdata != null && blockdata != "") {
			String[] split = blockdata.split(",");
			List<String> blockdatalist = new ArrayList<>(split.length);
			for (String splitId: split) {
				int bdId = Integer.parseInt(splitId);
				String bd = getBlockData(bdId);
				if (bd != null) {
					blockdatalist.add(bd);
				}
			}
			String[] arr = new String[blockdatalist.size()];
			for (int i = 0; i<arr.length; i++) arr[i] = blockdatalist.get(i);
			return arr;
		}
		
		return new String[]{};
	}

}
