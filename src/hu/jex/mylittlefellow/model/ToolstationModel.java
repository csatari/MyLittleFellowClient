package hu.jex.mylittlefellow.model;

import hu.jex.mylittlefellow.communicator.CommunicatorTool;
import hu.jex.mylittlefellow.gui.InformationDialog;

import java.util.ArrayList;

import android.app.Activity;
import android.os.AsyncTask;
import android.widget.Toast;

/**
 * Az eszközkészítõ oldal modellje
 * @author Albert
 *
 */
public class ToolstationModel {
	private static Activity context;
	
	private static ArrayList<ToolReceipt> toolReceipts;
	
	static OnToolstationEvent mListener;
	public interface OnToolstationEvent {
		/**
		 * Betöltötte a kifejleszthetõ eszközöket
		 */
		public void onViewRefreshed();
		/**
		 * Elkezdi kifejleszteni az egyik eszközt
		 * @param id
		 */
		public void onDevelopStarts(int id);
	}
	/**
	 * Beállítja az eseménykezelõt
	 * @param eventListener
	 */
	public void setCustomEventListener(OnToolstationEvent eventListener) {
		mListener=eventListener;
	}
	
	private enum CommunicatorType {
		LOADTOOLSTATION_DEVELOPING,DEVELOP_TIMED
	}
	
	public ToolstationModel(Activity context) {
		ToolstationModel.context = context;
		
		toolReceipts = new ArrayList<ToolReceipt>();
	}
	/**
	 * Betölti a kifejleszthetõ eszközök listáját
	 */
	public void loadToolStation() {
		Communicator comm = new Communicator();
        comm.execute(CommunicatorType.LOADTOOLSTATION_DEVELOPING);
	}
	/**
	 * Elkezd kifejleszteni egy eszközt
	 * @param id
	 */
	public void startDeveloping(int id) {
		Communicator comm = new Communicator();
		comm.helper = id;
		comm.execute(CommunicatorType.DEVELOP_TIMED);
	}
	
	public ArrayList<ToolReceipt> getToolReceipts() {
		return toolReceipts;
	}
	
	public ToolReceipt getToolReceiptAtPosition(int position) {
		return toolReceipts.get(position);
	}
	/**
	 * A szerverrel való kommunikációt intézi
	 * @author Albert
	 *
	 */
	private class Communicator extends AsyncTask<CommunicatorType, Void, Void> {
		public int helper;
		@Override
		protected Void doInBackground(CommunicatorType... params) {
			if(params[0] == CommunicatorType.LOADTOOLSTATION_DEVELOPING) {
				CommunicatorTool comm = new CommunicatorTool(context);
				toolReceipts = comm.getAllDevelopableTools();
				if(toolReceipts != null) {
					//for(ToolReceipt b : toolReceipts) {
						//Logger.writeToLog(b.toString());
					//}
					if(toolReceipts.size() == 0) {
						InformationDialog.errorToast(context, "Nothing to develop", Toast.LENGTH_SHORT);
					}
				}
				else {
					InformationDialog.errorToast(context, "Nothing to develop", Toast.LENGTH_SHORT);
				}
				context.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						if(mListener!=null)mListener.onViewRefreshed();
					}
				});
			}
			else if(params[0] == CommunicatorType.DEVELOP_TIMED) {
				if(mListener!=null)mListener.onDevelopStarts(helper);
			}
			return null;
		}
	}
}
