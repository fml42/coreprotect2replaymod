package co2rm;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import co2rm.packets.TimeUpdatePacket;

public class Co2Rm {
	
	public static void start(String cmdWorld, Position cmdPos1, Position cmdPos2, int cmdTime1, int cmdTime2, int cmdLength, 
			File recIn, File recOut, File blockMapFile, File coDatabaseFile) {
		
		BlockMap blockMap = new BlockMap(blockMapFile);
		BlockMap.setDefaultBlockMap(blockMap);
		
		Recording rec = new Recording(recIn);
		rec.listPackets(blockMap); //prints packet list
		System.out.println("[read recording] "+rec.getPackets().size()+" packets");
		Protocol pr = rec.getProtocol();
		
		//System.exit(0);
		
		List<PacketContainer> packets = rec.getPackets();
		List<PacketContainer> newPackets = new ArrayList<PacketContainer>();
		
		//pad recording
		int padding = 1000; //additional length
		int durationOg = rec.getMeta().getInt("duration");
		int newDuration = durationOg + cmdLength + padding;
		rec.getMeta().put("duration", newDuration);
		int interval = 1000;
		int ticks = 20;
		addTimePackets(pr, packets, newPackets, interval, ticks, newDuration);
		
		int timelapseOffset = durationOg;
		CoDatabase coDatabase = new CoDatabase(coDatabaseFile);
		CoConverter coc = new CoConverter(blockMap, coDatabase);
		coc.convert(pr, cmdTime1, cmdTime2, cmdWorld, null, cmdPos1, cmdPos2, timelapseOffset, cmdLength, newPackets);
		coDatabase.close();
		
		orderPackets(newPackets);
		List<PacketContainer> allPackets = concatPackets(packets, newPackets);
		rec.setPacketList(allPackets);
		rec.writeFile(recOut);
	}

	private static void addTimePackets(Protocol pr, List<PacketContainer> packets, List<PacketContainer> newPackets, int interval, int ticks, int newDuration) {
		try {
			long tLastWorldAge = 0;
			long tLastTimeOfDay = 0;
			int tsLastTimePacket = 0;
			
			for (PacketContainer pc: packets) {
				IReadablePacket p = pc.getPacket(pr);
				if (p instanceof TimeUpdatePacket) {
					TimeUpdatePacket tup = (TimeUpdatePacket) p;
					tLastWorldAge = tup.getWorldAge();
					tLastTimeOfDay = tup.getTimeOfDay();
					tsLastTimePacket = pc.timestamp;
				}
			}
			
			for (int t = tsLastTimePacket+interval; t<=newDuration; t+=interval) {
				tLastTimeOfDay += ticks;
				tLastWorldAge += ticks;
				
				TimeUpdatePacket tup = new TimeUpdatePacket(tLastWorldAge, tLastTimeOfDay);
				PacketContainer timePacket = new PacketContainer(t, tup, pr);
				newPackets.add(timePacket);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

	@SafeVarargs
	private static List<PacketContainer> concatPackets(List<PacketContainer>... packetLists) {
		List<PacketContainer> newList = new ArrayList<>();
		for (List<PacketContainer> list: packetLists) {
			for (PacketContainer pc: list) newList.add(pc);
		}
		return newList;
	}

	private static void orderPackets(List<PacketContainer> newPackets) {
		Collections.sort(newPackets, new Comparator<PacketContainer>() {
			@Override
			public int compare(PacketContainer a, PacketContainer b) {
				return a.timestamp - b.timestamp;
			}
		});
	}

}
