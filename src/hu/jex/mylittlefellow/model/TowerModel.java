package hu.jex.mylittlefellow.model;

import hu.jex.mylittlefellow.communicator.CommunicatorBuilding;
import hu.jex.mylittlefellow.gui.InformationDialog;

import java.util.ArrayList;

import android.app.Activity;
import android.os.AsyncTask;
import android.widget.Toast;

public class TowerModel {
	private static Activity context;
	
	private static ArrayList<BuildingReceipt> buildingReceipts;
	
	static OnTowerEvent mListener;
	public interface OnTowerEvent {
		/**
		 * Let�lt�tte a fejleszthet� �p�leteket
		 */
		public void onRefreshed();
		/**
		 * Elkezd fejleszteni egy �p�letet
		 * @param type Az �p�let t�pusa
		 * @param level az �p�let szintje
		 */
		public void onStartDeveloping(int type, int level);
	}
	/**
	 * Be�ll�tja az esem�nykezel�t
	 * @param eventListener
	 */
	public void setCustomEventListener(OnTowerEvent eventListener) {
		mListener=eventListener;
	}
	
	private enum CommunicatorType {
		LOADTOWER_DEVELOPING,DEVELOP_TIMED
	}
	
	public TowerModel(Activity context) {
		TowerModel.context = context;
		
		buildingReceipts = new ArrayList<BuildingReceipt>();
	}
	/**
	 * Elkezdi let�lteni az �p�letek list�j�t
	 */
	public void loadTowerDeveloping() {
		Communicator comm = new Communicator();
        comm.execute(CommunicatorType.LOADTOWER_DEVELOPING);
	}
	/**
	 * Elkezdi kifejleszteni az �p�letet
	 * @param type az �p�let t�pusa
	 * @param level az �p�let szintje
	 */
	public void startBuildingDevelop(int type, int level) {
		Communicator comm = new Communicator();
		comm.helper = type;
		comm.helper2 = level;
		comm.execute(CommunicatorType.DEVELOP_TIMED);
	}
	
	public BuildingReceipt getBuildingReceiptAtPosition(int position) {
		return buildingReceipts.get(position);
	}
	public ArrayList<BuildingReceipt> getBuildingReceipts() {
		return buildingReceipts;
	}
	/**
	 * A szerverrel val� kommunik�ci�t val�s�tja meg m�sik sz�lon
	 * @author Albert
	 *
	 */
	private class Communicator extends AsyncTask<CommunicatorType, Void, Void> {
		public int helper;
		public int helper2;
		@Override
		protected Void doInBackground(CommunicatorType... params) {
			if(params[0] == CommunicatorType.LOADTOWER_DEVELOPING) {
				CommunicatorBuilding comm = new CommunicatorBuilding(context);
				buildingReceipts = comm.getAllDevelopableBuildings();
				if(buildingReceipts != null) {
					//for(BuildingReceipt b : buildingReceipts) {
						//Logger.writeToLog(b.toString());
					//}
					if(buildingReceipts.size() == 0) {
						InformationDialog.errorToast(context, "Nothing to develop", Toast.LENGTH_SHORT);
					}
				}
				else {
					InformationDialog.errorToast(context, "Nothing to develop", Toast.LENGTH_SHORT);
				}
				context.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						if(mListener!=null)mListener.onRefreshed();
					}
				});
			}
			else if(params[0] == CommunicatorType.DEVELOP_TIMED) {
				if(mListener!=null)mListener.onStartDeveloping(helper, helper2);
			}
			return null;
		}
	}
}
