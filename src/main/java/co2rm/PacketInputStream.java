package co2rm;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

public class PacketInputStream extends DataInputStream {
	
	
	
	public PacketInputStream(InputStream in) {
		super(in);
	}

	public int readVarInt() {
		try {
			 int value = 0;
			    int length = 0;
			    byte currentByte;

			    while (true) {
			        currentByte = readByte();
			        value |= (currentByte & 0x7F) << (length * 7);
			        
			        length += 1;
			        if (length > 5) {
			            throw new RuntimeException("VarInt is too big");
			        }

			        if ((currentByte & 0x80) != 0x80) {
			            break;
			        }
			    }
			    return value;
		} catch (Exception e) {
			e.printStackTrace();
			return -1;
		}
	}
	
	public long readVarLong() {
	    try {
	    	long value = 0;
		    int length = 0;
		    byte currentByte;

		    while (true) {
		        currentByte = readByte();
		        value |= (currentByte & 0x7F) << (length * 7);
		        
		        length += 1;
		        if (length > 10) {
		            throw new RuntimeException("VarLong is too big");
		        }

		        if ((currentByte & 0x80) != 0x80) {
		            break;
		        }
		    }
		    return value;
		} catch (Exception e) {
			e.printStackTrace();
			return -1;
		}
	}
	
	public Position readPosition() {
		try {
			return new Position(readLong());
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

}
