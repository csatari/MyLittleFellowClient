package hu.jex.mylittlefellow.gui;

import hu.jex.mylittlefellow.R;
import hu.jex.mylittlefellow.gui.BaseActivity.ActivityType;
import hu.jex.mylittlefellow.model.Logger;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface.OnClickListener;
import android.widget.TextView;
import android.widget.Toast;

/**
 * A program által képernyõre írt dialog és toast kezelõosztálya
 * @author Albert
 *
 */
public class InformationDialog {
	/**
	 * Kiír egy hibaüzenetet dialog formájában
	 * @param context
	 * @param szoveg A kiírandó szöveg
	 * @param l A visszatérõ esemény
	 */
	public static void errorDialog(Context context, String szoveg, OnClickListener l) {
		try {
			AlertDialog.Builder builder = new AlertDialog.Builder(context);
			builder.setMessage(szoveg)
			.setTitle("Error");
			builder.setPositiveButton(context.getResources().getString(R.string.informationdialog_ok), l);
			AlertDialog dialog = builder.create();
			dialog.setCanceledOnTouchOutside(false);
			dialog.setCancelable(false);
			dialog.show();
		}
		catch(Exception e){
			Logger.writeToLog("Nem sikerült kirajzolni a dialogot, mert: ");
			Logger.writeException(e);
		}
	}
	/**
	 * Kiír egy dialogot, kényszerítve a UI szálra
	 * @param context
	 * @param szoveg A kiírandó szöveg
	 * @param l A visszatérõ esemény
	 */
	public static void errorDialogUIThread(final Activity context, final String szoveg, final OnClickListener l) {
		try {
			BaseActivity.context.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					InformationDialog.errorDialog(BaseActivity.context, szoveg, l);
				}
			});
		}
		catch(Exception e) {
			Logger.writeToLog("Nem lehet kiíratni UI threadre, mert:");
			Logger.writeException(e);
		}
	}
	/**
	 * Kiír egy Toastot
	 * @param context
	 * @param szoveg A kiírandó szöveg
	 * @param type A Toast típusa: Toast.LENGTH_LONG vagy Toast.LENGTH_SHORT
	 */
	public static void errorToast(final Activity context, final String szoveg, final int type) {
		try {
			context.runOnUiThread( new Runnable() {
				@Override
				public void run() {
					Toast.makeText(context, szoveg, type).show();
				}
			});
		}
		catch(Exception e) {
			Logger.writeToLog("Nem lehet kiírni a Toast-ot, mert: ");
			Logger.writeException(e);
		}
	}
	/**
	 * Kiírja az oldalhoz tartozó segítséget
	 * @param context
	 * @param type Az oldal típusa
	 */
	public static void showHelpDialog(final Activity context, ActivityType type) {
		String szoveg = "";
		switch(type) {
		case MAP:
			szoveg = "Here you can see the map where everything is happening. The 3 buttons in the top right" +
					" of the screen means:\n- Toggle Google maps\n- Toggle discovery mode\n- Go to character\n" +
					"When discovery mode is turned on, the GPS turns on too, and when you are on an undiscovered tile " +
					"of the map, it becomes discovered. The same goes with intelligence points, although you have to " +
					"be in the radius, or in 15 meters.\n" +
					"To enter a tile just click on it, and then click on the information window.\n" +
					"To get some information of an intelligence point just click on it, and it will show its name. " +
					"When you click on the information window, you will be redirected to the point's page.";
			break;
		case PLACE:
			szoveg = "On the top left corner you can see the actions with the resources. First, you have to examine " +
					"the tile and then you can harvest the resources. (You can harvest without examining)\n" +
					"Almost everything needs time to finish, on the top right corner there will be a counter.\n" +
					"On the top right, there are the actions what you can do with the buildings built on the tile. You " +
					"can build, when you are settled down on the tile. (Tent icon, top left)";
			break;
		case STORAGE:
			szoveg = "Here, you can see the storage of your character. There is a limit, what you can extend by building" +
					" storages. The intelligence points means the number of the found intelligence points.\n" +
					"With the red icon next to each resource, you can throw away one resource.";
			break;
		case TAX:
			szoveg = "Here, you can set tax for every living person on the tile. The tax is payed every week, once the" +
					" character enters the tile.";
			break;
		case TILESLICECHOOSE:
			szoveg = "You can build here by clicking on a square of the tile. The ideal is to build some storages and" +
					" build only one of other buildings.\n" +
					"You can only build the selected building, if you have every resource and intelligence point and" +
					" you have developed it in the Tower of Knowledge.";
			break;
		case TOOLSTATION:
			szoveg = "You can develop new tools here. Every tool speeds up the harvesting process.";
			break;
		case TOWER:
			szoveg = "You can develop new buildings here, what you can build. The goal is to build the level 5 Town Center";
			break;
		}
		try {
			AlertDialog.Builder builder = new AlertDialog.Builder(context);

			builder.setMessage(szoveg)
			.setTitle("Help");
			builder.setPositiveButton(context.getResources().getString(R.string.informationdialog_ok), null);

			AlertDialog dialog = builder.create();
			dialog.show();
			TextView msgTxt = (TextView) dialog.findViewById(android.R.id.message);
			msgTxt.setTextSize(15.0f);
		}
		catch(Exception e) {
			Logger.writeToLog("Nem lehet kiírni a helpDialogot, mert: ");
			Logger.writeException(e);
		}

	}
}
