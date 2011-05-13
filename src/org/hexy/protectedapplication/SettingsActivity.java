package org.hexy.protectedapplication;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.Preference.OnPreferenceClickListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class SettingsActivity extends PreferenceActivity {
	protected boolean removedPassword = false;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}
	
	protected void addPasswordPreferences() {
		this.addPreferencesFromResource(R.xml.password_preferences);
		
		Preference passwordPreference = (Preference) this.findPreference("password");
		passwordPreference.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			public boolean onPreferenceClick(final Preference preference) {
				final Dialog dialog = new Dialog(SettingsActivity.this);
				dialog.setContentView(R.layout.password_set);
				dialog.setTitle("Set password");

				final EditText inputPassword = (EditText) dialog.findViewById(R.id.input_password);
				final EditText inputConfirm = (EditText) dialog.findViewById(R.id.input_confirm);
				
				Button buttonOk = (Button) dialog.findViewById(R.id.password_set_ok);
				buttonOk.setOnClickListener(new OnClickListener() {
					public void onClick(View view) {
						String password = inputPassword.getText().toString();
						String confirm = inputConfirm.getText().toString();
						
						if (password.length() == 0) {
							Toast.makeText(SettingsActivity.this, "You must enter a password", Toast.LENGTH_LONG).show();
							return;
						}
						
						if (!password.equals(confirm)) {
							inputPassword.setText("");
							inputConfirm.setText("");
							Toast.makeText(SettingsActivity.this, "The passwords you entered do not match, please try again", Toast.LENGTH_LONG).show();
							return;
						}
						
						Settings settings = new Settings(SettingsActivity.this);
						settings.setPassword(password);
						
						Toast.makeText(SettingsActivity.this, "Password updated", Toast.LENGTH_LONG).show();
						SettingsActivity.this.checkRemovedPassword();
						dialog.dismiss();
					}
				});
				
				Button buttonCancel = (Button) dialog.findViewById(R.id.password_set_cancel);
				buttonCancel.setOnClickListener(new OnClickListener() {
					public void onClick(View view) {
						dialog.dismiss();
					}
				});

				dialog.show();
				
				return true;
			}
		});
		
		Preference removePasswordPreference = (Preference) findPreference("remove_password");
		removePasswordPreference.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			public boolean onPreferenceClick(final Preference preference) {
				AlertDialog.Builder builder = new AlertDialog.Builder(SettingsActivity.this);
				builder.setMessage("Are you sure you want to remove your password?")
					.setCancelable(false)
					.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							Settings settings = new Settings(SettingsActivity.this);
							settings.setPassword(null);
							Toast.makeText(SettingsActivity.this, "Password removed", Toast.LENGTH_LONG).show();
							SettingsActivity.this.checkRemovedPassword();
						}
					})
					.setNegativeButton("No", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							dialog.cancel();
						}
					});
				AlertDialog alert = builder.create();
				alert.show();
				return true;
			}
		});
	}
	
	@Override
	public void onPause() {
		super.onPause();
		((ProtectedApplication) this.getApplication()).updatedPaused();
	}
	
	@Override
	public void onResume() {
		super.onResume();
		((ProtectedApplication) this.getApplication()).checkPassword(this);
		this.checkRemovedPassword();
	}
	
	protected void checkRemovedPassword() {
		Preference removePasswordPreference = this.findPreference("remove_password");
		Settings settings = new Settings(this);
		removePasswordPreference.setEnabled(settings.hasPassword());
	}
}
