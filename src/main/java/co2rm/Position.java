package co2rm;

public class Position {

	int x;
	int y;
	int z;
	
	public Position(long encodedPos) {
		this.x = posX(encodedPos);
		this.y = posY(encodedPos);
		this.z =  posZ(encodedPos);
	}
	
	@Override
	public String toString() {
		return "["+x+","+y+","+z+"]";
	}
	
	public long encode() {
		return ((x & 0x3FFFFFFL) << 38) | ((z & 0x3FFFFFFL) << 12) | (y & 0xFFFL);
	}
	
	private static int posX(long pos) { return (int) (pos >> 38); }
	private static int posY(long pos) { return (int) (pos & 0xFFF); }
	private static int posZ(long pos) { return (int) ((pos >> 12) & 0x3FFFFFF); }

	public Position(int x, int y, int z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}



	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getY() {
		return y;
	}

	public void setY(int y) {
		this.y = y;
	}

	public int getZ() {
		return z;
	}

	public void setZ(int z) {
		this.z = z;
	}
	
	
	
}
