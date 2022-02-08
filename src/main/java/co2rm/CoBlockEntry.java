package co2rm;

public class CoBlockEntry {
	
	int time;
	int user;
	int world;
	int x;
	int y;
	int z;
	int type;
	int data;
	String meta;
	String blockdata;
	int action;
	int rolled_back;
	
	public CoBlockEntry(int time, int user, int world, int x, int y, int z, int type, int data, String meta,
			String blockdata, int action, int rolled_back) {
		super();
		this.time = time;
		this.user = user;
		this.world = world;
		this.x = x;
		this.y = y;
		this.z = z;
		this.type = type;
		this.data = data;
		this.meta = meta; //blobToString(meta);
		this.blockdata = blockdata; //blobToString(blockdata);
		this.action = action;
		this.rolled_back = rolled_back;
	}

	/*private String blobToString(Blob val) {
		try {
			return val == null ? null : new String(val.getBytes(0, (int) val.length()));
		} catch (SQLException e) {
			e.printStackTrace();
		};
		return null;
	}*/

	public Position getPosition() {
		return new Position(x, y, z);
	}
	
}
