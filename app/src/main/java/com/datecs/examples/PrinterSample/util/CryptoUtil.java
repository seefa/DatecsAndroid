package com.datecs.examples.PrinterSample.util;

import android.annotation.SuppressLint;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.RSAPrivateKeySpec;

public class CryptoUtil {

	@SuppressLint("TrulyRandom")
    public static byte[] encrypt3DESCBC(byte[] keyBytes, byte[] ivBytes, byte[] dataBytes) {
		try {
			AlgorithmParameterSpec ivSpec = new IvParameterSpec(ivBytes);
			SecretKeySpec newKey = new SecretKeySpec(keyBytes, "DESede");
			Cipher cipher = Cipher.getInstance("DESede/CBC/NoPadding");
			cipher.init(Cipher.ENCRYPT_MODE, newKey, ivSpec);
			return cipher.doFinal(dataBytes);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return null;
	}
		   
    public static byte[] decrypt3DESCBC(byte[] keyBytes, byte[] ivBytes, byte[] dataBytes) {
		try {
			AlgorithmParameterSpec ivSpec = new IvParameterSpec(ivBytes);
			SecretKeySpec newKey = new SecretKeySpec(keyBytes, "DESede");
			Cipher cipher = Cipher.getInstance("DESede/CBC/NoPadding");
			cipher.init(Cipher.DECRYPT_MODE, newKey, ivSpec);
			return cipher.doFinal(dataBytes);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return null;
	}
    
    public static byte[] decrypt3DESCBC(byte[] desKey, byte[] data) {
        byte[] decrypted = null; 
        
        try {                       
            final SecretKey key = new SecretKeySpec(desKey, "DESede");
            final IvParameterSpec iv = new IvParameterSpec(new byte[8]);
            final Cipher cipher = Cipher.getInstance("DESede/CBC/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, key, iv);
            decrypted = cipher.doFinal(data);           
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        
        return decrypted;
    }
    
    public static byte[] decryptRSABlock(byte[] modBytes, byte[] expBytes, byte[] data) {
        try {
            BigInteger modulus = new BigInteger(1, modBytes);
            BigInteger exponent = new BigInteger(1, expBytes);
            KeyFactory factory = KeyFactory.getInstance("RSA");
            Cipher cipher = Cipher.getInstance("RSA");
            
            RSAPrivateKeySpec privateSpec = new RSAPrivateKeySpec(modulus, exponent);
            PrivateKey privateKey = factory.generatePrivate(privateSpec);
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
            return cipher.doFinal(data);       
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return null;
    }
    
    public static byte[] decryptAESCBC(byte[] keyBytes, byte[] ivBytes, byte[] dataBytes) {
        try {
            AlgorithmParameterSpec ivSpec = new IvParameterSpec(ivBytes);
            SecretKeySpec newKey = new SecretKeySpec(keyBytes, "AES");
            Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, newKey, ivSpec);
            return cipher.doFinal(dataBytes);                   
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return null;
    }

    public static byte[] decryptAESCBC(byte[] keyBytes, byte[] dataBytes) {
        return decryptAESCBC(keyBytes, new byte[16], dataBytes);
    }
	
    public static byte[] encryptAES256CBC(byte[] keyValue, byte[] data) {
        try {                       
            final SecretKey key = new SecretKeySpec(keyValue, "AES256");
            final IvParameterSpec iv = new IvParameterSpec(new byte[16]);
            final Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, key, iv);
            final byte[] cipherData = cipher.doFinal(data);
            return cipherData;    
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return null;
    }
    
	public static byte[] calculateSHA256(byte[]src, int srcOffset, int len) {
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			md.update(src, srcOffset, len);			
			return  md.digest();
		} catch (Exception e) {
			e.printStackTrace();			
		}
		
		return null;
	}
	
	public static byte[] calculateSHA256(ByteBuffer buffer) {
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			md.update(buffer);			
			return  md.digest();
		} catch (Exception e) {
			e.printStackTrace();			
		}
		
		return null;
	}

	public static byte[] calculateSHA1(byte[] input, int offset, int len) {
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("SHA-1");
            md.update(input, offset, len);
        }
        catch(NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        } 
        return md.digest();
    }
    	
	private static void memxor(byte[] output, int outPos, byte[] a, int aPos, byte[] b, int bPos, int len) {
		for (int i = 0; i < len; i++) {
			output[outPos + i] = (byte)((a[aPos + i] & 0xff) ^ (b[bPos + i] & 0xff));
		}
	}
	
	private static void memcpy(byte[] dst, int dstOffset, byte[] src, int srcOffset, int length) {
		System.arraycopy(src, srcOffset, dst, dstOffset, length);
	}
	
	private static void memset(byte[] dst, int dstOffset, int value, int length) {
		for (int i = 0; i < length; i++) {
			dst[dstOffset + i] = (byte)value;
		}
	}
	
	public static final void encryptDES(byte[] output, int outputOffset, byte[] input, int inputOffset, int length, byte[] desKey, int desKeyOffset) {
		try {				    	
	        final SecretKey key = new SecretKeySpec(desKey, desKeyOffset, 8, "DES");
	        final IvParameterSpec iv = new IvParameterSpec(new byte[8]);
	        final Cipher cipher = Cipher.getInstance("DES/CBC/NoPadding");
	        cipher.init(Cipher.ENCRYPT_MODE, key, iv);	 
	        cipher.doFinal(input, inputOffset, length, output, outputOffset);	        
	    } catch (Exception e) {
	    	e.printStackTrace();
	    }		   
	}
		
	public static final void encrypt3DESECB(byte[] output, int outputOffset, byte[] input, int inputOffset, int length, byte[] desKey, int desKeyOffset) {
		final byte[] keyValue = new byte[24];
		System.arraycopy(desKey, desKeyOffset, keyValue, 0, 16);
		System.arraycopy(desKey, desKeyOffset, keyValue, 16, 8);
		
		try {			
	        final SecretKey key = new SecretKeySpec(keyValue, "DESede");	        
	        final Cipher cipher = Cipher.getInstance("DESede/ECB/NoPadding");
	        cipher.init(Cipher.ENCRYPT_MODE, key);	 
	        cipher.doFinal(input, inputOffset, length, output, outputOffset);		        
	    } catch (Exception e) {
	    	e.printStackTrace();	    	
	    }		   
	}
		
	public static byte[] calculateDerivedKey(byte[] ksn, byte[] ipek) {
		byte[] r8 = new byte[8];		
		byte[] r8a = new byte[8];		
		byte[] r8b = new byte[8];
		byte[] key = new byte[16];
		
		memcpy(key, 0, ipek, 0, 16);
		memcpy(r8, 0, ksn, 2, 8 - 2);
		r8[5] &= ~0x1F;
		
		int ec = ((ksn[ksn.length - 3] & 0x1F) << 16) | ((ksn[ksn.length - 2] & 0xFF) << 8) | (ksn[ksn.length - 1] & 0xFF);  
		int sr = 0x100000;
		
		byte[] pattern = new byte[] { (byte)0xC0, (byte)0xC0, (byte)0xC0, (byte)0xC0, 0x00, 0x00, 0x00, 0x00, (byte)0xC0, (byte)0xC0, (byte)0xC0, (byte)0xC0, 0x00, 0x00, 0x00, 0x00 };
			
		while (sr != 0) {
			if ((sr & ec) != 0) {
				r8[5] |= sr >> 16; 
				r8[6] |= sr >> 8; 
				r8[7] |= sr;
				
				memxor(r8a, 0, key, 8, r8, 0, 8);
				encryptDES(r8a, 0, r8a, 0, 8, key, 0);
				memxor(r8a, 0, r8a, 0, key, 8, 8);
				memxor(key, 0, key, 0, pattern, 0, 16);
				memxor(r8b, 0, key, 8, r8, 0, 8);
				encryptDES(r8b, 0, r8b, 0, 8, key, 0);
				memxor(r8b, 0, r8b, 0, key, 8, 8);				
				memcpy(key, 8, r8a, 0, 8);
				memcpy(key, 0, r8b, 0, 8);
			}
			
			sr>>= 1;
		}
		
		memset(r8, 0, 0, r8.length);
		memset(r8a, 0, 0, r8a.length);
		memset(r8b, 0, 0, r8b.length);
		 		
		return key;
	}	
	
	public static byte[] calculateDataKey(byte[] ksn, byte[] ipek) {
		byte[] dataKey = calculateDerivedKey(ksn, ipek);
		
		dataKey[5]^= 0xFF;
		dataKey[13]^= 0xFF;
		encrypt3DESECB(dataKey, 0, dataKey, 0, dataKey.length, dataKey, 0);		
		return dataKey;
	}
	
	public static byte[] signSHA256withRSA(byte[] modBytes, byte[] expBytes, byte[] data) {
	    try {
            BigInteger modulus = new BigInteger(1, modBytes);
            BigInteger exponent = new BigInteger(1, expBytes);
            KeyFactory factory = KeyFactory.getInstance("RSA");
            RSAPrivateKeySpec privateSpec = new RSAPrivateKeySpec(modulus, exponent);
            PrivateKey privateKey = factory.generatePrivate(privateSpec);
            
            Signature rsaSignature = Signature.getInstance("SHA256withRSA");
            rsaSignature.initSign(privateKey);
            rsaSignature.update(data);
            return rsaSignature.sign();                   
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return null;        
	}
		
    public static int crcccit(int crc, int b) {
        crc = ((crc >> 8) & 0xff) | ((crc << 8) & 0xffff);
        crc ^= (int) b & 0xff;
        crc ^= (crc & 0xff) >> 4;
        crc ^= ((crc << 8) << 4) & 0xffff;
        crc ^= ((crc & 0xff) << 4) << 1;
        return crc & 0xffff;
    }
    
    public static int crcccit(byte[] buf, int offset, int length) {
        int crc = 0;

        for (int i = 0; i < length; i++) {
            crc = crcccit(crc, (int) buf[offset + i] & 0xff);
        }

        return crc;
    }

}
