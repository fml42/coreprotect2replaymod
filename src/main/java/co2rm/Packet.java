package co2rm;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

public class Packet {
	
	private int packetId;
	
	public Packet(int packetId) {
		baos = new ByteArrayOutputStream();
		os = new PacketOutputStream(baos);
		os.writeVarInt(packetId);
	}
	
	private PacketInputStream is;
	private PacketOutputStream os;
	private ByteArrayOutputStream baos;
	
	public PacketInputStream is() {
		return is;
	}
	
	public PacketOutputStream os() {
		return os;
	}
	
	public int getPacketId() {
		return packetId;
	}
	
	public Packet(byte[] bytes) {
		is = new PacketInputStream(new ByteArrayInputStream(bytes));
		this.packetId = is.readVarInt();
	}
	
	byte[] getBytes() {
		return baos.toByteArray();
	}

}
