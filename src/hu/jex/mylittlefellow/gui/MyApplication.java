package hu.jex.mylittlefellow.gui;

import hu.jex.mylittlefellow.R;

import org.acra.ACRA;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;

import android.app.Application;

@ReportsCrashes(formKey = "", 
//mailTo = "csatari2864@gmail.com",
mode = ReportingInteractionMode.TOAST,
formUri = "http://csatari64.web.elte.hu/mlf/log.php",
resToastText = R.string.crash_send)
public class MyApplication extends Application {
	@Override
	public void onCreate() {
		super.onCreate();
		// The following line triggers the initialization of ACRA
		ACRA.init(this);
	}
}