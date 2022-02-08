package co2rm.protocols;

import co2rm.Protocol;
import co2rm.packets.BlockChangePacket;
import co2rm.packets.TimeUpdatePacket;	

//1.16.5
//https://wiki.vg/index.php?title=Protocol&oldid=16681

public class Protocol754 extends Protocol {

	@Override
	public void registerPackets() {
		register(0x0B, BlockChangePacket.class);
		register(0x4E, TimeUpdatePacket.class);
	}

}
