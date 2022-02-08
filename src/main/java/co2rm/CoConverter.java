package co2rm;

import java.util.HashMap;
import java.util.List;

import org.json.JSONObject;

import co2rm.packets.BlockChangePacket;

public class CoConverter {

	BlockMap blockMap;
	CoDatabase coDatabase;
	

	public CoConverter(BlockMap blockMap, CoDatabase coDatabase) {
		this.blockMap = blockMap;
		this.coDatabase = coDatabase;
	}


	public void convert(Protocol pr, int tStart, int tEnd, String world, String user, Position bb1, Position bb2, int timelapseOffset,
			int timelapseDuration, List<PacketContainer> newPackets) {
		
		int worldId = coDatabase.findWorld(world);
		String where = "time>="+tStart+" and time<="+tEnd+" and wid="+worldId+" ";
		if (user != null) {
			int userId = coDatabase.findUser(user);
			where += "and user="+userId+" ";
		}
		where += CoDatabase.positionalWhere(bb1, bb2);
		System.out.println("[block lookup] "+where);
		List<CoBlockEntry> blockChanges = coDatabase.loadBlockHistory(where);
		System.out.println(blockChanges.size()+" block changes found");
		
		double waitTime = timelapseDuration / (double) blockChanges.size();
		
		HashMap<String, Block> blockCache = new HashMap<>();
		
		for (int i = 0; i<blockChanges.size(); i++) {
			CoBlockEntry be = blockChanges.get(i);
			
			String material = coDatabase.getMaterial(be.type);
			String[] blockData = coDatabase.getBlockData(be.blockdata);
			Block b = new Block(material, blockData);
			
			b.blockData = amendBlockData(b.material, b.blockData, be.data);
			boolean interaction = false;
			if (be.action == 2) {
				boolean s = interaction(blockCache, be, b, blockMap);
				if (s) interaction = true;
			}

			int mappedId = blockMap.getMappedId(b.material, b.blockData);
			
			String username = coDatabase.getUsername(be.user);
			String action = "<unknown>"; 
			if (be.action == 0) action = "destroyed";
			if (be.action == 1) action = "placed";
			if (be.action == 2) action = "interacted with"; //?
			String blockDataStr = (b.blockData.length>0) ? "["+String.join(",", b.blockData)+"]" : "";
			System.out.println("["+be.time+"] "+username+" "+action+" "+b.material+blockDataStr+" at "+be.getPosition()+" -> "+mappedId);
			
			if (be.action == 0) mappedId = 0; // destory action, 0 = air 
			
			boolean discard = true;
			if (be.action == 0) discard = false;
			if (be.action == 1) discard = false;
			if (interaction) discard = false;
			
			
			
			if (!discard) {
				blockCache.put(be.getPosition().toString(), b);
				
				try {
					int ts = timelapseOffset + (int)(i*waitTime); 
					BlockChangePacket pp = new BlockChangePacket(be.getPosition(), mappedId);
					PacketContainer pc = new PacketContainer(ts, pp, pr);
					newPackets.add(pc);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		
	}


	private boolean interaction(HashMap<String, Block> blockCache, CoBlockEntry be, Block b, BlockMap blockMap) {
		if (b.material.contains("_trapdoor")) {
			String posStr = be.getPosition().toString();
			if (blockCache.containsKey(posStr)) {
				Block bCached = blockCache.get(posStr);
				JSONObject properties = BlockMap.propertyListToJson(bCached.blockData);
				if (!properties.has("open")) {
					//properties.put("open", "false");
					JSONObject defaultProperties = blockMap.getDefaultProperties(b.material);
					System.out.println("[default properties] "+defaultProperties.toString());
					properties = defaultProperties;
				}
				if (properties.getString("open").equals("true")) properties.put("open", "false");
				else if (properties.getString("open").equals("false")) properties.put("open", "true");
				System.out.println("[trapdoor] "+properties.toString());				
				b.blockData = BlockMap.propertyListFromJson(properties);
				return true;
			}
		}
		
		return false;
	}


	private String[] amendBlockData(String material, String[] blockData, int data) {
		JSONObject properties = BlockMap.propertyListToJson(blockData);
		
		//https://github.com/PlayPro/CoreProtect/blob/70f74ced0fd5afead71ed540ddb4157d1d4f4f3c/src/main/java/net/coreprotect/database/Rollback.java#L813
		if (material.contains("_door")) {
			if (data >= 8) {
				properties.put("half", "upper");
				data = data - 8;
			} else {
				properties.put("half", "lower");
			}
			if (data >= 4) {
				properties.put("hinge", "right");
				data = data - 4;
			} else {
				properties.put("hinge", "left");
			}
			switch (data) {
			case 0:
				properties.put("facing", "east");
				break;
			case 1:
				properties.put("facing", "south");
				break;
			case 2:
				properties.put("facing", "west");
				break;
			case 3:
				properties.put("facing", "north");
				break;
			}
			
			System.out.println("[door] "+properties.toString());
		}
		
		return BlockMap.propertyListFromJson(properties);
	}

}
