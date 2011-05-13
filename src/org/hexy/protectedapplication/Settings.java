package org.hexy.protectedapplication;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Cipher;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import android.util.Base64;
import android.util.Log;

public class Settings {
	protected Activity context;
	protected static String encryptionKey;
	
	public Settings(Activity context) {
		this.context = context;
	}
	
	public String getEncryptionKey() {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this.context);
		String key = prefs.getString("encryption_key", null);
		
		if (key == null) {
			// first use, set random key
			key = java.util.UUID.randomUUID().toString();
			this.setEncryptionKey(key);
			return key;
		}
		
		if (this.hasPassword()) {
			// decrypt key using password
				
			byte[] encBytes = Base64.decode(key.getBytes(), Base64.DEFAULT);
			
			Cipher cipher;
			byte[] plainBytes;
			try {
				cipher = CipherFactory.getCipher((Activity) this.context, false, false);
				plainBytes = cipher.doFinal(encBytes);
			} catch (Exception e) {
				Log.e("BodyChecker", "Crypto failure " + e.getMessage());
				return null;
			}
			
			return new String(plainBytes);
		} else {
			// key is plaintext
			return key;
		}
	}
	
	public void setEncryptionKey(String key) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this.context);
		Editor editor = prefs.edit();
		
		if (this.hasPassword()) {
			byte[] plainBytes = key.getBytes();
			
			Cipher cipher;
			byte[] encBytes;
			try {
				cipher = CipherFactory.getCipher((Activity) this.context, true, false);
				encBytes = cipher.doFinal(plainBytes);
			} catch (Exception e) {
				Log.e("BodyChecker", "Crypto failure " + e.getMessage());
				return;
			}
			
			byte[] encoded = Base64.encode(encBytes, Base64.DEFAULT);
			editor.putString("encryption_key", new String(encoded));
		} else {
			editor.putString("encryption_key", key);
		}
		
		editor.commit();
	}
	
	public boolean checkPassword(String password) {
		String storedHash = this.getPasswordHash();
		return this.hashPassword(password).equals(storedHash);
	}
	
	public boolean hasPassword() {
		return !this.getPasswordHash().equals("");
	}
	
	public void setPassword(String password) {
		SharedPreferences customPreferences = this.context.getSharedPreferences("customPreferences", Activity.MODE_PRIVATE);
		Editor editor = customPreferences.edit();

		String key = this.getEncryptionKey();
		((ProtectedApplication) this.context.getApplication()).setPassword(password);
		String hash = password == null ? "" : this.hashPassword(password);
		editor.putString("password", hash);
		editor.commit();
		this.setEncryptionKey(key);
	}
	
	protected String hashPassword(String password) {
		MessageDigest digest;
		try {
			digest = MessageDigest.getInstance("SHA-1");
		} catch (NoSuchAlgorithmException e) {
			return "";
		}
		byte[] hash = digest.digest(password.getBytes());
		byte[] encoded = Base64.encode(hash, Base64.DEFAULT);
		return new String(encoded);
	}
	
	protected String getPasswordHash() {
		SharedPreferences prefs = this.context.getSharedPreferences("customPreferences", Activity.MODE_PRIVATE);
		return prefs.getString("password", "");
	}
}
