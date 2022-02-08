package co2rm.packets;

import co2rm.BlockMap;
import co2rm.IPacketInformation;
import co2rm.IReadablePacket;
import co2rm.IWriteablePacket;
import co2rm.Packet;
import co2rm.Position;

public class BlockChangePacket implements IWriteablePacket, IReadablePacket, IPacketInformation {
	
	Position position;
	int blockState;
	
	public BlockChangePacket() {}
	
	public BlockChangePacket(Position position, int blockState) {
		this.position = position;
		this.blockState = blockState;
	}


	@Override
	public void readPacketData(Packet p) {
		this.position = p.is().readPosition();
		this.blockState = p.is().readVarInt();
	}

	@Override
	public void writePacketData(Packet p) {
		p.os().writePosition(this.position);
		p.os().writeVarInt(this.blockState);
	}

	public Position getPosition() {
		return position;
	}

	public void setPosition(Position position) {
		this.position = position;
	}

	public int getBlockState() {
		return blockState;
	}

	public void setBlockState(int blockState) {
		this.blockState = blockState;
	}

	@Override
	public String getPacketName() {
		return "Block Change";
	}

	@Override
	public void printPacket() {
		String identifier = "<?>";
		if (BlockMap.getDefaultMap() != null) identifier = BlockMap.getDefaultMap().getFromStateId(blockState);
		System.out.println("block change: "+position.toString()+" : "+blockState+" : "+identifier);
	}

}
