package nl.cyberwizzard.repdroid;

import android.util.Log;

/**
 * Class to wrap a G-code.
 * 
 * Supported codes: see validCode()
 * 
 * @author Berend Dekens
 */

public final class GCommand extends Command {
	// Holders for the limited number of possible arguments (faster than an dynamic array of objects)
	static float arg_X = 0.0f, arg_Y = 0.0f, arg_Z = 0.0f, arg_E = 0.0f, arg_F = 0.0f, arg_P = 0.0f;
	static boolean has_X = false, has_Y = false, has_Z = false, has_E = false, has_F = false, has_P = false;
	
	// Private constructor - can not be instantiated
	private GCommand() {}
	
	public static void setData(byte[] cbuf, int len) throws Exception {
		// Reset for new command
		buflen = 0;
		code = -1;
		hasArgs = false;
		
		// Copy the content of the String to the character buffer
		buflen = len;
		if(buflen > 127) throw new Exception("String length exceeds 127");
		for(int i=0;i<buflen;i++) buf[i] = cbuf[i];
		
		// Set the last element to a space so each line will end in whitespace
		buf[buflen++] = ' ';
		
		// Extract the code following the 'g' or 'G'
		for(int i=1;i<buflen;i++) {
			byte c = buf[i];
			if(c < '0' || c > '9') {
				// Non-numeric character, must be the ending
				if(i==1) throw new Exception("Malformed G-code: " + cbuf.toString());
				// Convert into a value
				code = parseIntFromCharBuf(1, i);
				// Exit loop
				break;
			}
		}

		// Make sure the code is valid
		if(code == -1) throw new Exception("Invalid G-code: " + cbuf.toString());
	}
	
	/**
	 * Get the offset for the X axis in mm or inches. Depending on the opcode this is used differently.
	 * G0/G1: Move to position on X axis
	 * G28: Home X axis (value ignored)
	 * G92: Set the current X position
	 * @return
	 * @throws ArgumentInvalidException
	 * @throws ArgumentNotFoundException
	 */
	public static float getX() throws ArgumentInvalidException, ArgumentNotFoundException {
		// Make sure the arguments are parsed
		if(!hasArgs) parseArguments();
		
		// Filter: make sure this opcode should have this argument
		switch(code) {
		case 0:
		case 1:
		case 28:
		case 92:
			break;
		default:
			throw new ArgumentInvalidException(code, 'X');
		}
		
		if(!has_X) throw new ArgumentNotFoundException('X');
		return arg_X;
	}

	/**
	 * Get the offset for the Y axis in mm or inches. Depending on the opcode this is used differently.
	 * G0/G1: Move to position on Y axis
	 * G28: Home Y axis (value ignored)
	 * G92: Set the current Y position
	 * @return
	 * @throws ArgumentInvalidException
	 * @throws ArgumentNotFoundException
	 */
	public static float getY() throws ArgumentInvalidException, ArgumentNotFoundException {
		// Make sure the arguments are parsed
		if(!hasArgs) parseArguments();
		
		// Filter: make sure this opcode should have this argument
		switch(code) {
		case 0:
		case 1:
		case 28:
		case 92:
			break;
		default:
			throw new ArgumentInvalidException(code, 'Y');
		}
		
		if(!has_Y) throw new ArgumentNotFoundException('Y');
		return arg_Y;
	}
	
	/**
	 * Get the offset for the Z axis in mm or inches. Depending on the opcode this is used differently.
	 * G0/G1: Move to position on Z axis
	 * G28: Home Z axis (value ignored)
	 * G92: Set the current Z position
	 * @return
	 * @throws ArgumentInvalidException
	 * @throws ArgumentNotFoundException
	 */
	public static float getZ() throws ArgumentInvalidException, ArgumentNotFoundException {
		// Make sure the arguments are parsed
		if(!hasArgs) parseArguments();
		
		// Filter: make sure this opcode should have this argument
		switch(code) {
		case 0:
		case 1:
		case 28:
		case 92:
			break;
		default:
			throw new ArgumentInvalidException(code, 'Z');
		}
		
		if(!has_Z) throw new ArgumentNotFoundException('Z');
		return arg_Z;
	}
	
	/**
	 * Get the offset for the E axis in mm or inches. Depending on the opcode this is used differently.
	 * G0/G1: Move to position on E axis
	 * G92: Set the current E position
	 * @return
	 * @throws ArgumentInvalidException
	 * @throws ArgumentNotFoundException
	 */
	public static float getE() throws ArgumentInvalidException, ArgumentNotFoundException {
		// Make sure the arguments are parsed
		if(!hasArgs) parseArguments();
		
		// Filter: make sure this opcode should have this argument
		switch(code) {
		case 0:
		case 1:
		case 92:
			break;
		default:
			throw new ArgumentInvalidException(code, 'E');
		}
		
		if(!has_E) throw new ArgumentNotFoundException('E');
		return arg_E;
	}
	
	/**
	 * Get the feed rate, in mm/min
	 * G0/G1: Set feed rate
	 * @return
	 * @throws ArgumentInvalidException
	 * @throws ArgumentNotFoundException
	 */
	public static float getF() throws ArgumentInvalidException, ArgumentNotFoundException {
		// Make sure the arguments are parsed
		if(!hasArgs) parseArguments();
		
		// Filter: make sure this opcode should have this argument
		switch(code) {
		case 0:
		case 1:
			break;
		default:
			throw new ArgumentInvalidException(code, 'F');
		}
		
		if(!has_F) throw new ArgumentNotFoundException('F');
		return arg_F;
	}
	
	/**
	 * Get the delay from G4: Dwell, in ms
	 * @return
	 * @throws ArgumentInvalidException
	 * @throws ArgumentNotFoundException
	 */
	public static float getP() throws ArgumentInvalidException, ArgumentNotFoundException {
		// Make sure the arguments are parsed
		if(!hasArgs) parseArguments();
		
		// Filter: make sure this opcode should have this argument
		switch(code) {
		case 4:
			break;
		default:
			throw new ArgumentInvalidException(code, 'P');
		}
		
		if(!has_P) throw new ArgumentNotFoundException('P');
		return arg_P;
	}
	
	public static String explain() throws ArgumentInvalidException, ArgumentNotFoundException {
		String str = "";
		switch(code) {
		case 0:
		case 1:
			str = "G" + code + " - Move to ";
			try {
				float f = getX();
				str += "X:"+f+" ";
			} catch(ArgumentNotFoundException e) {}
			try {
				float f = getY();
				str += "Y:"+f+" ";
			} catch(ArgumentNotFoundException e) {}
			try {
				float f = getZ();
				str += "Z:"+f+" ";
			} catch(ArgumentNotFoundException e) {}
			try {
				float f = getE();
				str += "E:"+f+" ";
			} catch(ArgumentNotFoundException e) {}
			try {
				float f = getF();
				str += "using feedrate:"+f;
			} catch(ArgumentNotFoundException e) {}
			return str;
		case 4:
			return "G4 - Dwell "+getP()+"ms";
		case 20:
			return "G22 - Use inches";
		case 21:
			return "G21 - Use mm";
		case 28:
			str = "G28 - Home ";
			try {
				getX();
				str += "X;";
			} catch(ArgumentNotFoundException e) {}
			try {
				getY();
				str += "Y;";
			} catch(ArgumentNotFoundException e) {}
			try {
				getZ();
				str += "Z;";
			} catch(ArgumentNotFoundException e) {}
			return str;
		case 90:
			return "G90 - Use absolute positioning";
		case 91:
			return "G91 - Use relative positioning";
		case 92:
			str = "G92 - Set axis to ";
			try {
				float f = getX();
				str += "X="+f+" ";
			} catch(ArgumentNotFoundException e) {}
			try {
				float f = getY();
				str += "Y="+f+" ";
			} catch(ArgumentNotFoundException e) {}
			try {
				float f = getZ();
				str += "Z="+f+" ";
			} catch(ArgumentNotFoundException e) {}
			try {
				float f = getE();
				str += "E="+f+" ";
			} catch(ArgumentNotFoundException e) {}
			return str;
		default:
			return "Unknown opcode "+code;
		}
	}
	
	/**
	 * Test the current opcode if it is known in this program.
	 * @return True if we know how to handle it, false if we don't
	 */
	public static boolean validCode() {
		switch(code) {
		case 0:		// G0 - Move
		case 1:		// G1 - Move
		case 4:		// G4 - Dwell
		case 20:	// G20 - Use inches
		case 21:	// G21 - Use mm
		case 28:	// G28 - Home
		case 90:	// G90 - Use absolute positioning
		case 91:	// G91 - Use relative positioning
		case 92:	// G92 - Set axis position
			return true;
		default:
			return false;
		}
	}
	
	/**
	 * Process a G-code string which has the format: "A1 B2 C3.0 D4"
	 * @param str A single line of G-code
	 * @return
	 */
	protected static void parseArguments() {
		// Reset arguments
		arg_X = arg_Y = arg_Z = arg_E = arg_F = arg_P = 0.0f;
		has_X = has_Y = has_Z = has_E = has_F = has_P = false;
		
		state s = state.FIND_ARG;
		
		// Holders for the decoding of all commands
		byte arg = '#';
		int arg_start = -1;
		float val;
		
		for(int i=0;i<buflen;i++) {
			switch(s) {
			case FIND_ARG:
				// Searching for a command...
				if(buf[i] != ' ' && buf[i] != '\t') {
					// End of white space, capture the command char and fetch the argument
					arg = buf[i];
					arg_start = i+1;
					s = state.FIND_VAL;
				}
				break;
			case FIND_VAL:
				// Found a command, search for the arguments...
				if(buf[i] == ' ' || buf[i] == '\t') {
					// Found white space - thats the end of the argument
					if(i == arg_start)
						// No argument found
						val = 0.0f;
					else
						// Parse whatever we have - can throw NumberFormatException
						val = parseFloatFromCharBuf(arg_start, i);
					
					// Store the argument
					switch(arg) {
					case 'x': case 'X': has_X = true; arg_X = val; break;
					case 'y': case 'Y': has_Y = true; arg_Y = val; break;
					case 'z': case 'Z': has_Z = true; arg_Z = val; break;
					case 'e': case 'E': has_E = true; arg_E = val; break;
					case 'f': case 'F': has_F = true; arg_F = val; break;
					case 'p': case 'P': has_P = true; arg_P = val; break;
					case 'g': case 'G': /* The G-code itself - ignore it */; break;
					default:
						Log.e("parseArguments", "Invalid argument for G-code found: "+(char)arg);
						break;
					}
					
					// Invalidate the mark
					arg_start = -1;
					s = state.FIND_ARG;
				}
				break;
			}
		}
		
		// If arg_start is valid, we still have to process a part of the string
		if(arg_start >= 0) {
			val = parseFloatFromCharBuf(arg_start, buflen);
			// Store the argument
			switch(arg) {
			case 'x': case 'X': has_X = true; arg_X = val; break;
			case 'y': case 'Y': has_Y = true; arg_Y = val; break;
			case 'z': case 'Z': has_Z = true; arg_Z = val; break;
			case 'e': case 'E': has_E = true; arg_E = val; break;
			case 'f': case 'F': has_F = true; arg_F = val; break;
			case 'p': case 'P': has_P = true; arg_P = val; break;
			default:
				Log.e("parseArguments", "Invalid argument for G-code found: "+arg);
				break;
			}
		}
		
		hasArgs = true;
	}
}
