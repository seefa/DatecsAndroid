package com.datecs.examples.PrinterSample.util;

public abstract class HexUtil {
	
	private static final char[] HEX = new char[] { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' }; 
	
	private HexUtil() { }
	
	public static final String byteArrayToHexString(byte[] scr, int off, int len) {
		final char[] buf = new char[len * 3];  
		
		for (int i = 0, j = 0; i < len; i++) {
			buf[j++] = HEX[((scr[off + i] >> 4) & 0xf)];
			buf[j++] = HEX[((scr[off + i]) & 0xf)];
			buf[j++] = ' ';
		}
		
		return new String(buf);
	}
	
	public static final String byteArrayToHexString(byte[] src) {
		return byteArrayToHexString(src, 0, src.length);
	}

	public static final byte[] hexStringToByteArray(char[] src, int off, int len) {
		if ((len & 1) != 0) throw new IllegalArgumentException("The argument 'len' can not be odd value");
		
		final byte[] buffer = new byte[len / 2];
			
		for (int i = 0; i < len; i++) {
			int nib = src[off + i];
			
			if ('0' <= nib && nib <= '9') {
				nib = nib - '0';
			} else if ('A' <= nib && nib <= 'F') {
				nib = nib - 'A' + 10;
			} else if ('a' <= nib && nib <= 'f') {
				nib = nib - 'a' + 10;
			} else {
				throw new IllegalArgumentException("The argument 'src' can contains only HEX characters");
			}
			
		    if ((i & 1) != 0) {
		    	buffer[i / 2] += nib; 
		    } else {
		    	buffer[i / 2] = (byte)(nib << 4);
		    }		    
		}
		
		return buffer;		
	}

	public static final byte[] hexStringToByteArray(char[] src) {
		return hexStringToByteArray(src, 0, src.length);
	}
	
	public static final byte[] hexStringToByteArray(String s) {
		char[] src = s.toCharArray();
		return hexStringToByteArray(src);
	}
	
	public static final byte[] hexStringToByteArray(String s, char delimiter) {
		char src[] = s.toCharArray();		
		int srcLen = 0;
		
		for (char c: src) {
			if (c != delimiter) src[srcLen++] = c;
		}
		
		return hexStringToByteArray(src, 0, srcLen);		
	}

}
