package nl.cyberwizzard.repdroid;

import java.io.*;
import java.util.ArrayList;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import android.os.Environment;
import android.util.Log;

public class GCodeParser {
	static File file;						// File holding the Gcode
	static RandomAccessFile is = null;		// Input stream from the Gcode file
	static FileChannel channel = null;		// Direct channel access to the file
	static ByteBuffer bbuf = null;			// Buffer to fill with bytes from the file
	static byte bbuf_bytes[] = null;		// Handle to the content of bbuf
	static int ptr = 0;					// Pointer to the last byte used in bbuf_bytes
	static int ptr_offset = 0;				// Offset + ptr = position in file
	static int bbuf_size = 0;				// Maximum position in the current byte buffer
	static LayerIndex root = new LayerIndex();	// Root layer
	static LayerIndex currentLayer = root;	// Pointer to the current layer
	
	static ArrayList<LayerIndex> index = null;
	// Keep track of layer changes when the Z axis moves by holding on to that
	static float lastZ = -999.0f;
	
	/**
	 * Open a G-code file for printing. To preserve memory and be able to handle large
	 * jobs, we scan the file for errors and index it by layer.
	 * @param filename
	 */
	public static void openFile(String filename) throws Exception {
		// Probe the state of the external storage
		String state = Environment.getExternalStorageState();
		if (!Environment.MEDIA_MOUNTED.equals(state) && !Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
			System.out.println("Storage is not mounted\r\n");
			return;
		}
		
		file = new File(Environment.getExternalStorageDirectory(), filename);
		Log.i("OpenFile", "Attempting to open " + file.getAbsolutePath());
		if(!file.exists()) {
			Log.w("OpenFile", "File " + file.getAbsolutePath() + " does not exist");
			throw new Exception("File " + file.getAbsolutePath() + " does not exist");
		}
		if(!file.canRead()) {
			Log.w("OpenFile", "File " + file.getAbsolutePath() + " is unreadable");
			throw new Exception("File " + file.getAbsolutePath() + " is unreadable");
		}
		
		try {
			is = new RandomAccessFile(file, "r");
			channel = is.getChannel();
			// Create a 4k input buffer
			bbuf = ByteBuffer.allocate(512*1024);
			// Dump data from the input channel into the byte buffer
			bbuf_size = channel.read(bbuf);
			// Flip the buffer so the internal pointer starts at zero again
			bbuf.flip();
			// Fetch the bytes
			bbuf_bytes = bbuf.array();
		} catch (IOException e) {
			Log.w("OpenFile", "Error reading " + filename, e);
			throw e;
		}
	}
	
	/**
	 * Close the file and with it, all the buffers and streams.
	 * @throws IOException
	 */
	public static void closeFile() throws IOException {
		if(is!=null) {
			is.close();
			is = null;
		}
	}
	
	/**
	 * To validate the file, we scan it line by line and parse G and M codes to 
	 * test if we think it is a valid job. At the same time, we can index the layers
	 * in the file.
	 * @return True while valid lines are read or false upon errors or EOF
	 */
	private static boolean parseLine() {
		// Buffer to hold all bytes making up a single line
		byte buf[] = new byte[128];
		// Number of bytes in the current line
		int buflen = 0;
		// Preserve the current location so we know where this line started
		int ptr_history = ptr_offset+ptr;
		
		// Test if we reached EOF
		if(bbuf_size == -1) return false;	// No more bytes, no more lines
		
		try {
			for(int i=0;i<128;i++) {
				byte c = (byte)bbuf_bytes[ptr++];
				// If we used all bytes from the file block...
				if(ptr >= bbuf_size) {
					// ... load more bytes from the file
					fetchBlock();
					// Test if we reached EOF 
					if(bbuf_size <= 0) break;
				}
				if(c == '\r') continue;					// Swallow carriage return
				if(c == '\n') break;						// Break when the line ends
				if(c == ' ' && buflen == 0) continue;		// Swallow leading spaces
				buf[buflen++] = c;
				// When a line is too long, abort
				if(buflen == 128) return false;
			}
			
			// Determine the type of the command
			byte cmd = buf[0];
			switch(cmd) {
			case 'g':
			case 'G':
				// G-code
				try {
					GCommand.setData(buf,buflen);
					if(!GCommand.validCode()) {
						Log.e("parseLine","Unknown G-code: "+buf.toString());
						return false;
					}
					// Check for movement commands
					if((GCommand.code == 0 || GCommand.code == 1)) {
						// Trigger the parsing of the command to find if the Z axis moved
						GCommand.parseArguments();
						if(GCommand.has_Z && GCommand.arg_Z != lastZ) {
							// New layer
							LayerIndex i = new LayerIndex();
							i.prev = currentLayer;
							i.index = currentLayer.index+1;
							i.offset = ptr_history;
							currentLayer.next = i;
							// Update the pointer
							currentLayer = i;
							// Preserve whatever Z we just found
							lastZ = GCommand.arg_Z;
						}
					}
					//Log.i("parseLine", GCommand.explain());
				} catch (ArgumentInvalidException e) {
					Log.e("parseLine", "Invalid argument exception for: "+(new String(buf)));
					return false;
				} catch (ArgumentNotFoundException e) {
					Log.e("parseLine", "Missing argument for: "+(new String(buf)));
					return false;
				} catch (Exception e) {
					Log.e("parseLine", "Generic error:"+e.getMessage() + ":"+(new String(buf)));
					e.printStackTrace();
					return false;
				}
				break;
			case 'm':
			case 'M':
				// M-code
				break;
			default:
				// Unknown
				//Log.w("parseLine", "Unknown command ignored: " + (new String(buf)));
			}

			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		
	}
	
	private static void fetchBlock() throws IOException {
		// Load a new block of data from the file
		bbuf_size = channel.read(bbuf);
		// Reset the internal pointer in the byte buffer to position zero
		bbuf.flip();
		// Replace the current array with the new one
		bbuf_bytes = bbuf.array();
		// Reset the pointer
		ptr = 0;
		// Increase the block count
		ptr_offset += 512*1024;
	}

	public static void indexFile() {
		// Wipe the array list - if any
		index = new ArrayList<LayerIndex>();
		int lines = 0;
		
		Log.i("indexFile","Indexing file");
		while(parseLine()) {
			lines++;
		}
		Log.i("indexFile","Parsed "+lines+" lines");
		
		LayerIndex i = root;
		while(i.next != null) {
			Log.i("indexFile","Layer "+i.index+" @ "+i.offset);
			i = i.next;
		}
	}
}
