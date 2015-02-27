package hu.jex.mylittlefellow.gui;

import hu.jex.mylittlefellow.R;
import hu.jex.mylittlefellow.model.Resource;
import hu.jex.mylittlefellow.model.ResourceStorage;
import hu.jex.mylittlefellow.model.StorageModel;
import hu.jex.mylittlefellow.model.StorageModel.OnStorageEvent;
import hu.jex.mylittlefellow.model.TimedAction;
import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * A raktárat kezelõ oldal
 * @author Albert
 *
 */
public class StorageActivity extends BaseActivity implements OnStorageEvent {

	private static Activity context;
	private static GridView storageGridview;
	private static StorageAdapter storageAdapter;
	private static TextView storageLimitTextview;
	private static TextView ipoTextview;

	private static StorageModel storageModel;

	private static boolean frissitve;
	
	private static Dialog throwDialog;
	private static EditText throwAmount;
	private static Button throwSend;
	private static Button throwCancel;

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_storage);
		context = this;

		storageModel = new StorageModel(context);
		storageModel.setCustomEventListener(this);

		storageGridview = (GridView)findViewById(R.id.centerGridview);
		storageAdapter = new StorageAdapter();
		storageGridview.setGravity(Gravity.RIGHT);
		storageGridview.setAdapter(storageAdapter);

		storageLimitTextview = (TextView)findViewById(R.id.storageLimit);
		ipoTextview = (TextView)findViewById(R.id.ipoTextview);

		try {
			setBackground(storageModel.getBackground());
		}
		catch(Exception e) {
			goBackToRootActivity();
		}
	}
	@Override
	public void onPause() {
		super.onPause();
		frissitve = false;
	}
	@Override
	public void onResume() {
		super.onResume();
		frissitve = false;

		storageModel.storagePreview();
		storageModel.downloadStorage();

	}
	/**
	 * A háttérképet állítja be
	 * @param resid
	 */
	private void setBackground(int resid) {
		findViewById(R.id.background).setBackgroundResource(resid);
	}

	/**
	 * A raktárban lévõ nyersanyagok megjelenéséért felelõs
	 * @author Albert
	 *
	 */
	private class StorageAdapter extends BaseAdapter {
		@Override
		public int getCount() {
			return storageModel.getStorageCount();
		}
		@Override
		public Object getItem(int arg0) {
			return null;
		}
		@Override
		public long getItemId(int arg0) {
			return 0;
		}
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {


			Resource res = storageModel.getResourceByPosition(position);
			if(res == null) {
				return null;
			}

			Button button = new Button(context);

			button.setText(res.getAmount()+"");
			int draw = Resource.getResourceDrawableByType(res.getId());
			//Logger.writeToLog("A kép kirajzolása, id-k: "+draw+" "+res.getId());
			final int resourceId = res.getId();
			button.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(draw), null, null, null);
			button.setGravity(Gravity.CENTER);

			button.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					if(!frissitve) {
						InformationDialog.errorToast(context, "Please wait, while it's loading...",Toast.LENGTH_SHORT);
						return;
					}
					throwDialog = new Dialog(context);
					throwDialog.setContentView(R.layout.dialog_storagethrowaway);
					throwDialog.setTitle(getResources().getString(R.string.storage_title));
					throwAmount = (EditText) throwDialog.findViewById(R.id.amount);
					throwSend = (Button) throwDialog.findViewById(R.id.send);
					throwCancel = (Button) throwDialog.findViewById(R.id.cancel);
					
					throwSend.setOnClickListener(new OnClickListener() {
						
						@Override
						public void onClick(View v) {
							//Logger.writeToLog("Kattintott res: "+resourceId);
							
							storageModel.removeFromStorage(resourceId,Integer.parseInt(throwAmount.getText().toString()));
							throwDialog.cancel();
						}
					});
					
					throwCancel.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							throwDialog.cancel();
						}
					});
					throwDialog.show();
				}
			});

			return button;
		}
	}
	@Override
	protected ActivityType getActivityType() {
		return ActivityType.STORAGE;
	}
	@Override
	protected void onTimedActionFinish(TimedAction timedAction) {
		InformationDialog.errorToast(context, "Finished...", Toast.LENGTH_SHORT);
		if(timedAction.getType() == TimedAction.RESOURCE1 ||
				timedAction.getType() == TimedAction.RESOURCE2 ||
				timedAction.getType() == TimedAction.RESOURCE3) {
			storageModel.downloadStorage();
		}
	}
	@Override
	protected void onTimedActionTick(TimedAction timedAction,
			long secondsUntilFinished) {
		//Logger.writeToLog("tick..."+secondsUntilFinished);
	}
	@Override
	protected void onTimedActionStart(TimedAction timedAction) {
	}
	@Override
	protected void onTimedActionError(TimedAction timedAction) {
	}
	@Override
	protected void onTimedActionFinishUpload(TimedAction timedAction) {
	}

	@Override
	public void onStorageDownloaded(ResourceStorage storage, int ipo, int storageLimit) {
		storageLimitTextview.setText("Storage limit: "+storageLimit);
		ipoTextview.setText("Intelligence points: "+ipo);
		if(storage == null) { 
			return;
		}
		storageLimitTextview.setText("Storage limit: "+storage.getStorageResourceCount()+"/"+storageLimit);
		storageAdapter.notifyDataSetChanged();
		frissitve = true;
	}
}
