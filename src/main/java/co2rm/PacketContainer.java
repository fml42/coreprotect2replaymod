package co2rm;

import java.io.IOException;

public class PacketContainer {
	public int timestamp = 2;
	public int bytes = 0;
	public byte[] bb = new byte[bytes];
	
	public PacketContainer(int time, int bytecount, byte[] bytes) throws IOException{
		this.timestamp = time;
		this.bytes = bytecount;
		this.bb = bytes;
	}

	/*public PacketContainer(int t, Packet p) {
		this.timestamp = t;
		this.bb = p.getBytes();
		this.bytes = this.bb.length;
	}*/
	
	public PacketContainer(int t, IWriteablePacket wp, Protocol protocol) {
		this.timestamp = t;
		Packet p = protocol.writePacket(wp);
		this.bb = p.getBytes();
		this.bytes = this.bb.length;
	}
	
	public Packet getRawPacket() {
		return new Packet(bb);
	}
	
	public IReadablePacket getPacket(Protocol protocol) {
		return protocol.readPacket(new Packet(bb));
	}
}
