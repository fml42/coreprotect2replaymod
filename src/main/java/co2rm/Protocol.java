package co2rm;

import java.util.HashMap;
import java.util.Map.Entry;

import co2rm.protocols.Protocol754;
import co2rm.protocols.Protocol757;

public abstract class Protocol {

	static HashMap<Integer, Class<?>> protocolMap;
	
	static {
		protocolMap = new HashMap<>();
		protocolMap.put(754, Protocol754.class);
		protocolMap.put(757, Protocol757.class);
	}
	
	HashMap<Integer, Class<?>> packetMap;
	
	public Protocol() {
		packetMap = new HashMap<>();
		registerPackets();
	}
	
	protected void register(int packetId, Class<?> packetClass) {
		packetMap.put(packetId, packetClass);
	}
	
	protected IReadablePacket defaultParsePacket(Packet p) {
		if (packetMap.containsKey(p.getPacketId())) {
			try {
				IReadablePacket rp = (IReadablePacket) packetMap.get(p.getPacketId()).newInstance();
				rp.readPacketData(p);
				return rp;
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		}
		return null;
	}
	
	private int getPacketId(Class<?> packetClass) {
		for (Entry<Integer, Class<?>> e: packetMap.entrySet()) {
			if (e.getValue() == packetClass) {
				return e.getKey();
			}
		}
		return -1;
	}
	
	protected Packet defaultWritePacket(IWriteablePacket wp) {
		int packetId = getPacketId(wp.getClass());
		
		if (packetId != -1) {
			Packet p = new Packet(packetId);
			wp.writePacketData(p);
			return p;
		}
		return null;
	}

	//protocal implementations can override this if necessary
	public IReadablePacket readPacket(Packet p) {
		return defaultParsePacket(p);
	}
	
	//protocal implementations can override this if necessary
	public Packet writePacket(IWriteablePacket wp) {
		return defaultWritePacket(wp);
	}

	public void registerPackets() {}

	public static Protocol getProtocolForVersion(int protocolVersion) {
		if (protocolMap.containsKey(protocolVersion)) {
			try {
				return (Protocol) protocolMap.get(protocolVersion).newInstance();
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

}
