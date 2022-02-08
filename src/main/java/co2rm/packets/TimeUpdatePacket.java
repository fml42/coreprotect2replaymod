package co2rm.packets;

import java.io.IOException;

import co2rm.IPacketInformation;
import co2rm.IReadablePacket;
import co2rm.IWriteablePacket;
import co2rm.Packet;

public class TimeUpdatePacket implements IReadablePacket, IWriteablePacket, IPacketInformation {
	
	long worldAge;
	long timeOfDay;
	
	public TimeUpdatePacket() {
	}

	public TimeUpdatePacket(long worldAge, long timeOfDay) {
		this.worldAge = worldAge;
		this.timeOfDay = timeOfDay;
	}

	
	@Override
	public void writePacketData(Packet p) {
		try {
			p.os().writeLong(this.worldAge);
			p.os().writeLong(this.timeOfDay);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void readPacketData(Packet p) {
		try {
			this.worldAge = p.is().readLong();
			this.timeOfDay = p.is().readLong();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public long getWorldAge() {
		return worldAge;
	}

	public void setWorldAge(long worldAge) {
		this.worldAge = worldAge;
	}

	public long getTimeOfDay() {
		return timeOfDay;
	}

	public void setTimeOfDay(long timeOfDay) {
		this.timeOfDay = timeOfDay;
	}

	@Override
	public String getPacketName() {
		return "Time Update";
	}

	@Override
	public void printPacket() {
		System.out.println("time update: worldAge="+getWorldAge()+" timeOfDay="+getTimeOfDay());
	}

	
}
