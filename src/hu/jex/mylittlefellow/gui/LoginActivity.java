package hu.jex.mylittlefellow.gui;

import hu.jex.mylittlefellow.R;
import hu.jex.mylittlefellow.model.Login;
import hu.jex.mylittlefellow.model.Login.OnLoginEvent;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

/**
 * A bejelentkezõablak oldala
 * @author Albert
 *
 */
public class LoginActivity extends Activity implements OnLoginEvent {
	private static Activity context;

	private static Login login;

	private static Dialog registerDialog;
	private static Button registerSend;
	private static EditText registerUsername;
	private static EditText registerPassword;
	private static EditText registerRePassword;
	private static ProgressDialog progressDialog;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		context = this;
		setContentView(R.layout.activity_login_new);
		int result = GooglePlayServicesUtil.isGooglePlayServicesAvailable(context);
		if(result != ConnectionResult.SUCCESS) {
			GooglePlayServicesUtil.getErrorDialog(result,context,16).show();
		}
		else {
			login = new Login(context);
			login.setCustomEventListener(this);
			login.startLogin();
		}
	}
	/**
	 * A fiókválasztás után fut le.
	 */
	protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
		if (requestCode == 15 && resultCode == RESULT_OK) {
			login.accountPicked(data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME));
		}
		else if(requestCode == 16 && resultCode == RESULT_OK) {
			login = new Login(context);
			login.setCustomEventListener(this);
			login.startLogin();
		}
		else {
			InformationDialog.errorToast(context, "You have to pick an account!", Toast.LENGTH_SHORT);
			this.finish();
		}
	}

	@Override
	public void onLogin() {
		progressDialog.dismiss();
		finish();
		Intent intent = new Intent(LoginActivity.this, MapActivity.class);
		startActivity(intent);
	}
	@Override
	public void onStartProgress() {
		progressDialog = new ProgressDialog(context);
		progressDialog.setTitle("Loading...");
		progressDialog.setMessage("Loading...");
		progressDialog.show();
		progressDialog.setCancelable(false);
		progressDialog.setCanceledOnTouchOutside(false);
	}



	@Override
	public void setProgressMessage(String message) {
		progressDialog.setMessage(message);
	}



	@Override
	public void showRegistrationDialog(String account) {
		Typeface tf = Typeface.SANS_SERIF;
		registerDialog = new Dialog(context);
		registerDialog.setContentView(R.layout.dialog_register);
		registerDialog.setTitle(account);
		registerDialog.setCancelable(false);
		registerDialog.setCanceledOnTouchOutside(false);
		registerUsername = (EditText)registerDialog.findViewById(R.id.username);
		registerPassword = (EditText)registerDialog.findViewById(R.id.password);
		registerRePassword = (EditText)registerDialog.findViewById(R.id.repassword);
		registerSend = (Button)registerDialog.findViewById(R.id.send);
		registerPassword.setTypeface(tf);
		registerRePassword.setTypeface(tf);
		registerSend.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				if(login.checkRegistrationData(registerUsername, registerPassword, registerRePassword)) {
					login.register(registerUsername.getText().toString(), registerPassword.getText().toString(), registerRePassword.getText().toString());
					registerDialog.dismiss();
				}
			}
		});
		registerDialog.show();
	}
}
