package org.hexy.protectedapplication;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;

import android.app.Activity;

public class CipherFactory {
	protected final static byte[] FILE_SALT = new byte[] { (byte) 0xef, (byte) 0xe9, (byte) 0xe7, (byte) 0x6f, (byte) 0x5c, (byte) 0xdd, (byte) 0x9b, (byte) 0x95 }; 
	protected final static byte[] KEY_SALT = new byte[] { (byte) 0xb1, (byte) 0x56, (byte) 0x93, (byte) 0xc7, (byte) 0xac, (byte) 0x27, (byte) 0x8d, (byte) 0x74 };
	
	public static Cipher getCipher(Activity context, boolean isEncrypting, boolean isFile) throws
			InvalidKeySpecException, NoSuchAlgorithmException, NoSuchPaddingException,
			InvalidKeyException, InvalidAlgorithmParameterException {
		
		String password;
		if (isFile) {
			Settings settings = new Settings(context);
			password = settings.getEncryptionKey();
		} else {
			password = ((ProtectedApplication) context.getApplication()).getPassword();
		}
		
		/*
		  	Cipher performance:
		  	
			PBEWITHMD5AND128BITAES-CBC-OPENSSL	write 2.6s read 4.9s
			PBEWITHMD5ANDRC2					write 12s  read 14s
			PBEWITHMD5ANDDES					write 3.8s read 6.4s
		*/
		
		PBEKeySpec keySpec = new PBEKeySpec(password.toCharArray());
		SecretKeyFactory keyFactory;
		keyFactory = SecretKeyFactory.getInstance("PBEWITHMD5AND128BITAES-CBC-OPENSSL");
		SecretKey passwordKey = keyFactory.generateSecret(keySpec);

		int iterationCount = 2048;

		Cipher cipher = Cipher.getInstance("PBEWITHMD5AND128BITAES-CBC-OPENSSL");
		int mode = isEncrypting ? Cipher.ENCRYPT_MODE : Cipher.DECRYPT_MODE;
		byte[] salt = isFile ? CipherFactory.FILE_SALT : CipherFactory.KEY_SALT;
		cipher.init(mode, passwordKey, new PBEParameterSpec(salt, iterationCount));
		return cipher;
	}
	
	public static Cipher getInputCipher(Activity context) throws
			InvalidKeySpecException, NoSuchAlgorithmException, NoSuchPaddingException,
			InvalidKeyException, InvalidAlgorithmParameterException {
		return CipherFactory.getCipher(context, false, true);
	}

	public static Cipher getOutputCipher(Activity context) throws
			InvalidKeySpecException, NoSuchAlgorithmException, NoSuchPaddingException,
			InvalidKeyException, InvalidAlgorithmParameterException {
		return CipherFactory.getCipher(context, true, true);
	}
}
