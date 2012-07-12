package nl.cyberwizzard.repdroid;


enum state {FIND_VAL, FIND_ARG};

public abstract class Command {
	protected static int code = -1;
	protected static boolean hasArgs = false;
	
	protected static byte[] buf = new byte[128];
	protected static int buflen = 0;
	
	public static int getCode() {
		return code;
	}
	
	public static int parseIntFromCharBuf(int start, int end) {
		if(start >= end) return -1;
		int res = 0;
		for(int i=start;i<end;i++) {
			if(buf[i] < '0' || buf[i] > '9')
				// Invalid char - bail
				return res;
			res += (buf[i] - '0');
			if(i<end-1) res *= 10;
		}
		return res;
	}
	
	public static float parseFloatFromCharBuf(int start, int end) {
		if(start >= end) return -1.0f;
		float res = 0.0f, divider = 1.0f;
		boolean hasFrac = false;
		for(int i=start;i<end;i++) {
			if((buf[i] < '0' || buf[i] > '9') && buf[i] != '.') {
				// Invalid char, bail
				return res / divider;
			}
			if(buf[i] == '.') hasFrac = true;
			else {
				res += (float)(buf[i] - '0');
				if(hasFrac) divider *= 10.0f;
				if(i<end-1) {
					res *= 10.0f;
				}
			}
		}
		return res / divider;
	}
	
}
