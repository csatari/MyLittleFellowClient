package hu.jex.mylittlefellow.gui;

import hu.jex.mylittlefellow.R;
import hu.jex.mylittlefellow.model.Logger;
import hu.jex.mylittlefellow.model.Resource;
import hu.jex.mylittlefellow.model.ResourceStorage;
import hu.jex.mylittlefellow.model.Tax;
import hu.jex.mylittlefellow.model.Tax.OnTaxEvent;
import hu.jex.mylittlefellow.model.Tile;
import hu.jex.mylittlefellow.model.TimedAction;
import hu.jex.mylittlefellow.model.VisibleTileAdapter;
import hu.jex.mylittlefellow.storage.Storage;
import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.Toast;

/**
 * Az adót beállító oldal
 * @author Albert
 *
 */
public class TaxActivity extends BaseActivity implements OnTaxEvent {

	private static Activity context;
	private static GridView centerGridview;
	private static GridviewAdapter gridviewAdapter;
	
	private static ResourceStorage taxTypes;
	//dialog
	private static Dialog taxresourceDialog;
	private static EditText taxResourceAmount;
	private static Button taxResourceSend;
	
	private static Tax tax;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tax);
        context = this;
        
        tax = new Tax(context);
        tax.setCustomEventListener(this);
        
        centerGridview = (GridView)findViewById(R.id.centerGridview);
        gridviewAdapter = new GridviewAdapter();
        centerGridview.setGravity(Gravity.RIGHT);
        centerGridview.setAdapter(gridviewAdapter);
        centerGridview.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position,
					long arg3) {
				final Resource res = taxTypes.getByIndex(position);
				taxresourceDialog = new Dialog(context);
				taxresourceDialog.setContentView(R.layout.dialog_taxresource);
				taxresourceDialog.setTitle(Resource.getNameOfType(res.getId()));
				taxResourceAmount = (EditText) taxresourceDialog.findViewById(R.id.amount);
				taxResourceSend = (Button) taxresourceDialog.findViewById(R.id.send);
				
				taxResourceSend.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						try {
							taxTypes.set(res.getId(), Integer.parseInt(taxResourceAmount.getText().toString()));
							taxresourceDialog.cancel();
							refreshGridview();
							
							tax.setTax(res.getId(), res.getAmount());
						}
						catch(NumberFormatException e) {
							InformationDialog.errorToast(context, "Wrong number format", Toast.LENGTH_SHORT);
							taxResourceAmount.setText("");
						}
					}
				});
				taxresourceDialog.show();
			}
        	
		});
        try {
        	setBackground(tax.getBackground());
        }
        catch(Exception e) {
        	Logger.writeException(e);
        	goBackToRootActivity();
        }
        try {
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
		
		tax.loadTax();
        
	}
	/**
	 * A hátteret állítja be
	 * @param type
	 */
	private void setBackground(int type) {
		findViewById(R.id.background).setBackgroundResource(Tile.getTileBackground(type));
	}
	/**
	 * Frissíti az adót megjelenítõ vezérlõt
	 */
	private void refreshGridview() {
        gridviewAdapter.notifyDataSetChanged();
	}
	/**
	 * A nyersanyagokat megjelenítõ osztály
	 * @author Albert
	 *
	 */
	private class GridviewAdapter extends BaseAdapter {
		@Override
		public int getCount() {
			if(taxTypes == null) {
				return 0;
			}
			return taxTypes.size();
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
			if(taxTypes == null) {
				return null;
			}
			/*Resource res = taxTypes.getByIndex(position);
			RelativeLayout RL = new RelativeLayout(context);
			RL.setBackgroundColor(Color.argb(200,0, 181, 84));
			
			LayoutParams lpText = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			TextView textView = new TextView(context);
			textView.setText(Resource.getNameOfType(res.getId())+": "+res.getAmount());
			textView.setTextColor(Color.rgb(157, 255, 0));
			textView.setPadding(40, 30, 40, 30);
			textView.setTextSize(20);
			textView.setLayoutParams(lpText);
			
			RelativeLayout.LayoutParams lp2 = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
			
			RL.addView(textView,lp2);
			return RL;*/
			
			final Resource res = taxTypes.getByIndex(position);
			Button button = new Button(context);

			button.setText(res.getAmount()+"");
			int draw = Resource.getResourceDrawableByType(res.getId());
			//Logger.writeToLog("A kép kirajzolása, id-k: "+draw+" "+res.getId());
			button.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(draw), null, null, null);
			button.setGravity(Gravity.CENTER);

			button.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					taxresourceDialog = new Dialog(context);
					taxresourceDialog.setContentView(R.layout.dialog_taxresource);
					taxresourceDialog.setTitle(Resource.getNameOfType(res.getId()));
					taxResourceAmount = (EditText) taxresourceDialog.findViewById(R.id.amount);
					taxResourceSend = (Button) taxresourceDialog.findViewById(R.id.send);
					
					taxResourceSend.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							try {
								if(Integer.parseInt(taxResourceAmount.getText().toString()) > 5) {
									InformationDialog.errorToast(context, "The number is too high! Must be lower than 6", Toast.LENGTH_SHORT);
									taxResourceAmount.setText("");
									return;
								}
								taxTypes.set(res.getId(), Integer.parseInt(taxResourceAmount.getText().toString()));
								taxresourceDialog.cancel();
								refreshGridview();
								
								tax.setTax(res.getId(), res.getAmount());
							}
							catch(NumberFormatException e) {
								InformationDialog.errorToast(context, "Wrong number format", Toast.LENGTH_SHORT);
								taxResourceAmount.setText("");
							}
						}
					});
					taxresourceDialog.show();
				}
			});

			return button;
		}
	}
	@Override
	protected ActivityType getActivityType() {
		return ActivityType.TAX;
	}
	@Override
	protected void onTimedActionFinish(TimedAction timedAction) {
		InformationDialog.errorToast(context, "Finished...", Toast.LENGTH_SHORT);
	}
	@Override
	protected void onTimedActionTick(TimedAction timedAction, long secondsUntilFinished) { }
	@Override
	protected void onTimedActionStart(TimedAction timedAction) { }
	@Override
	protected void onTimedActionError(TimedAction timedAction) { }
	@Override
	protected void onTimedActionFinishUpload(TimedAction timedAction) { }
	@Override
	public void onTaxRefreshed(ResourceStorage taxes) {
		taxTypes = taxes;
		refreshGridview();
	}
}
