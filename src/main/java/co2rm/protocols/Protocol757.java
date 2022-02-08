package co2rm.protocols;

import co2rm.Protocol;
import co2rm.packets.BlockChangePacket;
import co2rm.packets.TimeUpdatePacket;

//1.18.1
//https://wiki.vg/Protocol

public class Protocol757 extends Protocol {

	@Override
	public void registerPackets() {
		register(0x0C, BlockChangePacket.class);
		register(0x59, TimeUpdatePacket.class);
	}
}
