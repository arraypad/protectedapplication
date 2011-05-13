package org.hexy.protectedapplication;

import java.util.Date;

import android.app.Activity;
import android.app.Application;
import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

/**
 * Provides password protection across the application
 */
public class ProtectedApplication extends Application {
	/**
	 * Period after onPause events until onResume in which a password
	 * prompt is not needed (milliseconds)
	 */
	protected final static int PASSWORD_CHECK_PERIOD = 5000;
	
	/**
	 * Whether the password prompt is currently showing
	 */
	protected boolean dialogShowing = false;
	
	/**
	 * The last time (in epoch milliseconds) that an activity was paused.
	 */
	protected long lastPause;
	
	protected String currentPassword;
	
	/**
	 * Initialiser
	 */
	@Override
	public void onCreate() {
		super.onCreate();
		this.lastPause = 0;
	}
	
	/**
	 * Resets the last paused time to now
	 */
	public void updatedPaused() {
		this.lastPause = new Date().getTime();
	}
	
	/**
	 * Prompts the user for a password if applicable
	 * 
	 * @param Context context
	 */
	public void checkPassword(final Activity context) {
		if (this.dialogShowing) {
			// already showing a prompt
			return;
		}
		
		if (new Date().getTime() - this.lastPause <= ProtectedApplication.PASSWORD_CHECK_PERIOD) {
			// was paused less than PASSWORD_CHECK_PERIOD ago
			return;
		}
		
		final Settings settings = new Settings(context);
		if (!settings.hasPassword()) {
			// no password is set
			return;
		}
		
		// inflate dialog
		final Dialog dialog = new Dialog(context);
		dialog.setContentView(R.layout.password_prompt);
		dialog.setTitle("Enter password");
		dialog.setCancelable(false);
		final EditText inputPassword = (EditText) dialog.findViewById(R.id.input_password);
		
		Button buttonOk = (Button) dialog.findViewById(R.id.password_set_ok);
		buttonOk.setOnClickListener(new OnClickListener() {
			public void onClick(View view) {
				String password = inputPassword.getText().toString();
				
				if (password.length() == 0) {
					Toast.makeText(context, "You must enter a password", Toast.LENGTH_LONG).show();
					return;
				}
				
				if (settings.checkPassword(password)) {
					// password was correct
					
					// store plaintext password for encryption
					ProtectedApplication.this.setPassword(password);
					
					dialog.dismiss();
					ProtectedApplication.this.dialogShowing = false;
				} else {
					inputPassword.setText("");
					Toast.makeText(context, "Incorrect password", Toast.LENGTH_LONG).show();
					return;
				}
			}
		});

		dialog.show();
		this.dialogShowing = true;
	}
	
	public void setPassword(String password) {
		this.currentPassword = password;
	}
	
	public String getPassword() {
		return this.currentPassword;
	}
}
