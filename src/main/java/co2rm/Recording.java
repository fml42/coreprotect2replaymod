package co2rm;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.CRC32;
import java.util.zip.Checksum;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import org.json.JSONObject;
import org.json.JSONTokener;

public class Recording {
	
	List<PacketContainer> packets;
	JSONObject meta;
	File fileIn;
	Protocol prot;
	
	public Recording(File f) {
		
		this.fileIn = f;
		
		try {
			ZipFile zipFile = new ZipFile(f);
			
			ZipEntry recordingData = zipFile.getEntry("recording.tmcpr");
			
			DataInputStream dis = new DataInputStream((InputStream) zipFile.getInputStream(recordingData));
			
			packets = new LinkedList<PacketContainer>();
			
			try {
				for (;;) {
					
					int timestamp = dis.readInt();
				    int bytes = dis.readInt();
				    byte[] bb = new byte[bytes];
				    dis.readFully(bb);
				    
				    PacketContainer packet = new PacketContainer(timestamp, bytes, bb);
					
					packets.add(packet);
					
				}
			} catch (EOFException e) {
				dis.close();
			}
			
			meta = new JSONObject(new JSONTokener(zipFile.getInputStream(zipFile.getEntry("metaData.json"))));
			
			if (meta.has("protocol")) {
				int protocolVersion = meta.getInt("protocol");
				prot = Protocol.getProtocolForVersion(protocolVersion);
				if (prot == null) {
					System.out.println("warning: no parser registered for protocol version "+protocolVersion);
				}
			} else {
				System.out.println("warning: recording metadata does not specify protocol version");
			}
			
			zipFile.close();

			if (prot == null) {
				throw new Exception("no protocol");
			}
			
		} catch (Exception e) {
			System.err.println("recording could not be read.");
			e.printStackTrace();
			System.exit(1);
		}
		
	}
	
	public JSONObject getMeta() {
		return meta;
	}
	
	public void listPackets(BlockMap blockMap) {
		System.out.println(packets.size()+" packets");
		for (PacketContainer pc: packets) {
			try {
				IReadablePacket p = pc.getPacket(prot);
				if (p == null) continue;
				int rPacketId = pc.getRawPacket().getPacketId();
				String name = "<unknown>";
				IPacketInformation pi = null;
				if (p instanceof IPacketInformation) {
					pi = (IPacketInformation) p;
					name = pi.getPacketName();
				}
				System.out.println(pc.timestamp+"ms | PACKET 0x"+Integer.toHexString(rPacketId)+" | "+name);
				if (pi != null) pi.printPacket();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	/*public void listPackets(BlockMap blockMap) {
		PacketMap pMap = new PacketMap();
		
		System.out.println(packets.size()+" packets");
		for (PacketContainer pc: packets) {
			try {
				IReadablePacket p = pc.getPacket(prot);
				
				if (pc.timestamp == 0) continue; //skip init
				if (packetId == 0x46) continue; //Entity Velocity
				if (packetId == 0x28) continue; //Entity Position and Rotation
				if (packetId == 0x27) continue; //Entity Position
				if (packetId == 0x3a) continue; //Entity Head Look
				if (packetId == 0x4e) continue; //Time Update
				if (packetId == 0x21) continue; //Effect
				if (packetId == 0x05) continue; //Entity Animation (clientbound)
				if (packetId == 0x47) continue; //Entity Equipment
				if (packetId == 0x56) continue; //Entity Teleport
				if (packetId == 0x51) continue; //Sound Effect
				
				if (p == null) continue;
				
				int rPacketId = pc.getRawPacket().getPacketId();
				//String name = pMap.getPacketNameClientbound(rPacketId);
				System.out.println(pc.timestamp+"ms | PACKET 0x"+Integer.toHexString(rPacketId)+" | "+name);
				
				if (p instanceof TimeUpdatePacket) { //Time Update
					TimeUpdatePacket tup = (TimeUpdatePacket) p;
					System.out.println("time update: worldAge="+tup.getWorldAge()+" timeOfDay="+tup.getTimeOfDay());
				}
				
				if (p instanceof BlockChangePacket) { //block change
					BlockChangePacket pp = (BlockChangePacket) p;
					Position pos = pp.getPosition();
					int blockId = pp.getBlockState();
					System.out.println("block change: "+pos.toString()+" : "+blockId+" : "+blockMap.getFromStateId(blockId));
				}
				
				if (packetId == 0x3B) { //multi block change
					long chunkSection = pis.readLong();
					int sectionX = (int) (chunkSection >> 42);
					int sectionY = (int) ((chunkSection << 44) >> 44);
					int sectionZ = (int) ((chunkSection << 22) >> 42);
					boolean trustEdges = pis.readByte() == 1;
					int num = pis.readVarInt();
					System.out.println("multi block change: count="+num+", trustEdges="+trustEdges+", chunk="+sectionX+","+sectionY+","+sectionZ);
					for (int i = 0; i<num; i++) {
						long blockEncoding = pis.readVarLong();
						//blockStateId << 12 | (blockLocalX << 8 | blockLocalZ << 4 | blockLocalY)
						int blockstate = (int) (blockEncoding>>12);
						int localX = (int) ((blockEncoding >> 8) & 0xf);
						int localZ = (int) ((blockEncoding >> 4) & 0xf);
						int localY = (int) ((blockEncoding) & 0xf);
						Position pos = new Position(sectionX*16+localX, sectionY*16+localY, sectionZ*16+localZ);
						System.out.println("  ["+i+"]: "+pos.toString()+" : "+blockstate+" : "+blockMap.getFromStateId(blockstate));
					}
				}
				
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}*/

	public List<PacketContainer> getPackets() {
		return packets;
	}

	public void setPacketList(List<PacketContainer> allPackets) {
		this.packets = allPackets;
	}

	public void writeFile(File recOut) {
		
		if (fileIn == null) {
			System.err.println("cannot write replay, no input file");
			return;
		}
		
		try {

			//tmcpr
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			DataOutputStream os = new DataOutputStream(baos);
			for (PacketContainer pc : this.packets) {
				//System.out.println("[writepacket] time="+pc.timestamp+" bytes="+pc.bytes);
				os.writeInt(pc.timestamp);
				os.writeInt(pc.bytes);
				os.write(pc.bb);
			}
			System.out.println("[writepacket] wrote "+this.packets.size()+" packets");
			
			//checksum
			byte bytes[] = baos.toByteArray();
	        Checksum checksum = new CRC32();
	        checksum.update(bytes, 0, bytes.length);
	        long checksumValue = checksum.getValue();
	        String checksumStr = String.valueOf(checksumValue);
	        
	        //metadata
	        String newMetaStr = meta.toString();
			
			
			ZipFile zipFile = new ZipFile(fileIn);
			
			ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(recOut));
			for( Enumeration<?> e = zipFile.entries(); e.hasMoreElements(); )
			{   
			    ZipEntry entry = (ZipEntry) e.nextElement();
			    System.out.println( "[zip] "+ entry.getName() );
			    InputStream is = null;
			    
			    if (entry.getName().equalsIgnoreCase("recording.tmcpr")) {
			    	is = new ByteArrayInputStream(baos.toByteArray());
			    	entry = new ZipEntry("recording.tmcpr");
			    } else if (entry.getName().equalsIgnoreCase("recording.tmcpr.crc32")) {
			    	is = new ByteArrayInputStream(checksumStr.getBytes());
			    	entry = new ZipEntry("recording.tmcpr.crc32");
			    } else if (entry.getName().equalsIgnoreCase("metaData.json")) {
			    	is = new ByteArrayInputStream(newMetaStr.getBytes());
			    	entry = new ZipEntry("metaData.json");
			    } else {
			    	is = zipFile.getInputStream(entry);
			    }
			    
			    zos.putNextEntry(entry);
			    byte [] buf = new byte[1024];
	            int len;
	            while((len = (is.read(buf))) > 0) {            
	                zos.write(buf, 0, len);
	            }
			}
			zipFile.close();
			zos.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

	public Protocol getProtocol() {
		return prot;
	}

}
