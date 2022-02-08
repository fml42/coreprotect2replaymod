package co2rm;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class PacketOutputStream extends DataOutputStream {

	public PacketOutputStream(OutputStream out) {
		super(out);
	}
	
	public void writeVarInt(int value) {
		   try {
			   while (true) {
			        if ((value & ~0x7F) == 0) {
			        	writeByte(value);
			          return;
			        }

			        writeByte((value & 0x7F) | 0x80);
			        // Note: >>> means that the sign bit is shifted with the rest of the number rather than being left alone
			        value >>>= 7;
			    }
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	
	public void writePosition(Position p) {
		try {
			writeLong(p.encode());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
