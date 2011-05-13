package org.hexy.protectedapplication;

import android.app.Activity;

public class ProtectedActivity extends Activity {
	@Override
	protected void onPause() {
		super.onPause();
		((ProtectedApplication) this.getApplication()).updatedPaused();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		((ProtectedApplication) this.getApplication()).checkPassword(this);
	}
}
