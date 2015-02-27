package hu.jex.mylittlefellow.gui;

import hu.jex.mylittlefellow.R;
import hu.jex.mylittlefellow.model.Logger;
import hu.jex.mylittlefellow.model.Tile;
import hu.jex.mylittlefellow.model.TimedAction;
import hu.jex.mylittlefellow.model.ToolReceipt;
import hu.jex.mylittlefellow.model.ToolstationModel;
import hu.jex.mylittlefellow.model.VisibleTileAdapter;
import hu.jex.mylittlefellow.model.ToolstationModel.OnToolstationEvent;
import hu.jex.mylittlefellow.storage.Storage;
import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Az eszközök fejlesztését mutató oldal
 * @author Albert
 *
 */
public class ToolstationActivity extends BaseActivity implements OnToolstationEvent {

	private static Activity context;
	private static GridView developGridview;
	private static DevelopAdapter developAdapter;
	
	private static TextView countdownTextView;
	
	private static ToolstationModel model;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tower);
        context = this;
        
        model = new ToolstationModel(context);
        model.setCustomEventListener(this);
        
        model.loadToolStation();
        
        developGridview = (GridView)findViewById(R.id.centerGridview);
        developAdapter = new DevelopAdapter();
        developGridview.setGravity(Gravity.RIGHT);
        developGridview.setAdapter(developAdapter);
        developGridview.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position,
					long arg3) {
				ToolReceipt br = model.getToolReceiptAtPosition(position);
				//Logger.writeToLog(br.toString());
				
				model.startDeveloping(br.getId());
				
			}
        });
        countdownTextView = (TextView)findViewById(R.id.countdownText);
        try {
        	refreshDevelopReceipts();

        	setBackground(Tile.getTileFromAllTilesById(VisibleTileAdapter.getAllTiles(), Storage.getUserTileId(context)).getType());
        }
        catch(Exception e) {
        	Logger.writeException(e);
        	goBackToRootActivity();
        }
	}
	@Override
	public void onPause() {
		super.onPause();
	}
	@Override
	public void onResume() {
		super.onResume();
	}
	/**
	 * A hátteret állítja be
	 * @param type
	 */
	private void setBackground(int type) {
		findViewById(R.id.background).setBackgroundResource(Tile.getTileBackground(type));
	}
	/**
	 * Frissíti a recepteket megjelenítõ vezérlõt
	 */
	private void refreshDevelopReceipts() {
		developAdapter.notifyDataSetChanged();
	}
	
	/**
	 * A receptek megjelenítéséért felelõs osztály
	 * @author Albert
	 *
	 */
	private class DevelopAdapter extends BaseAdapter {
		@Override
		public int getCount() {
			return model.getToolReceipts().size();
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
			LinearLayout LL = new LinearLayout(context);
			//LayoutParams lp = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
			LL.setOrientation(LinearLayout.VERTICAL);
			//LL.setLayoutParams(lp);
			TextView textView = new TextView(context);
			ToolReceipt br = model.getToolReceiptAtPosition(position);
			
			StringBuilder sb = new StringBuilder();
			sb.append("Type: ");
			sb.append(br.getName());
			sb.append("\n");
			sb.append(br.getResources().getResourcesInText());
			sb.append("Intelligence points: ");
			sb.append(br.getIpo());
			
			android.view.ViewGroup.LayoutParams lpv = new android.view.ViewGroup.LayoutParams(android.view.ViewGroup.LayoutParams.FILL_PARENT, android.view.ViewGroup.LayoutParams.FILL_PARENT);
			textView.setText(sb.toString());
			textView.setLayoutParams(lpv);
			textView.setBackgroundColor(Color.argb(200,0, 181, 84));
			textView.setTextColor(Color.WHITE);
			textView.setPadding(40, 10, 40, 10);
			textView.setTextSize(15);
			textView.setMinLines(7);
			textView.setBackgroundResource(R.drawable.background_blur);
			LL.addView(textView);
			return LL;
		}
	}

	@Override
	protected ActivityType getActivityType() {
		return ActivityType.TOOLSTATION;
	}
	@Override
	protected void onTimedActionFinish(TimedAction timedAction) {
		InformationDialog.errorToast(context, "finished...", Toast.LENGTH_LONG);
		countdownTextView.setVisibility(TextView.GONE);
		model.loadToolStation();
	}
	@Override
	protected void onTimedActionTick(TimedAction timedAction, long secondsUntilFinished) {
		countdownTextView.setText(timedAction.getTimedActionType()+" "+(int)Math.ceil((long)secondsUntilFinished/1000)+"");
	}
	@Override
	protected void onTimedActionStart(TimedAction timedAction) {
		Logger.writeToLog("ONSTART");
		countdownTextView.setVisibility(TextView.VISIBLE);
	}
	@Override
	protected void onTimedActionError(TimedAction timedAction) {
		countdownTextView.setVisibility(TextView.GONE);
	}
	@Override
	protected void onTimedActionFinishUpload(TimedAction timedAction) {
		countdownTextView.setText("Uploading...");
	}
	@Override
	public void onViewRefreshed() {
		refreshDevelopReceipts();
	}
	@Override
	public void onDevelopStarts(int id) {
		startTimedAction(TimedAction.DEVELOP_TOOL, id);
	}
}
