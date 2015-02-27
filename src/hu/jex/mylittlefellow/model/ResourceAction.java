package hu.jex.mylittlefellow.model;

import hu.jex.mylittlefellow.R;
import hu.jex.mylittlefellow.storage.Storage;
import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AbsListView.LayoutParams;
import android.widget.ImageButton;

/**
 * A nyersanyagokkal kapcsolatos gombokat kezeli
 * @author Albert
 *
 */
public class ResourceAction {
	public enum ActionButtonType {
		EXAMINE, WOODCUT, COLLECTTWIG, HUNT, COLLECTBERRIES, MINE, FISH, SETTLEDOWN, TRAVEL
	}
	
	//events define
	static OnActionEventListener mListener;
	public interface OnActionEventListener {
		/**
		 * Megvizsgálásra nyomott rá
		 */
		public void onExamineEvent();
		/**
		 * Letelepedésre nyomott rá
		 */
		public void onSettleDownEvent();
		/**
		 * Kitermelésre nyomott rá
		 * @param type A kitermelés típusa
		 */ 
		public void onActionEvent(ActionButtonType type);
		/**
		 * Utazásra nyomott rá
		 */
		public void onTravelEvent();
	}
	/**
	 * Az eseménykezelés beállítása
	 * @param eventListener
	 */
	public static void setCustomEventListener(OnActionEventListener eventListener) {
		mListener=eventListener;
	}
	
	
	public ResourceAction() {}
	/**
	 * Megadja, hogy hány gomb van
	 * @param homeid
	 * @param usertileid
	 * @param tile
	 * @return
	 */
	public static int getActionButtonCount(int homeid, int usertileid, Tile tile) {
		int count = 0;
		if(usertileid != tile.getId()) {
			count++;
			return count;
		}
		if(!tile.isExamined()) {
			count++;
		}
		switch(tile.getType()) {
			case 0: 
				count += 2;
				break;
			case 2: 
				count += 2;
				break;
			case 3:
				break;
			case 7: //város
				break;
			default: 
				count++;
				break;
		}
		if(homeid == 0) {
			count++;
		}
		return count;
	}
	/**
	 * Visszaadja, hogy milyen típusa van az adott sorszámon lévõ gombnak
	 * @param position A sorszám 
	 * @param tile A terület
	 * @param homeid az otthon azonosítója
	 * @param usertileid a karakter tartózkodási helye
	 * @return A gomb típusa
	 */
	private static ActionButtonType getActionButtonType(int position, Tile tile, int homeid, int usertileid) {
		ActionButtonType buttonType = null;
		switch(position) {
			case 0:
				if(usertileid != tile.getId()) {
					buttonType = ActionButtonType.TRAVEL;
					
					break;
				}
				else if(!tile.isExamined()) {
					buttonType = ActionButtonType.EXAMINE;
					break;
				}
				else {
					switch(tile.getType()) {
						case 0:
							buttonType = ActionButtonType.WOODCUT;
							break;
						case 1:
							buttonType = ActionButtonType.HUNT;
							break;
						case 2:
							buttonType = ActionButtonType.COLLECTBERRIES;
							break;
						case 4:
							buttonType = ActionButtonType.MINE;
							break;
						case 5:
							buttonType = ActionButtonType.MINE;
							break;
						case 6:
							buttonType = ActionButtonType.FISH;
							break;	
						case 8:
							buttonType = ActionButtonType.FISH;
							break;	
						default:
							break;
					}
				}
				break;
			case 1:
				if(!tile.isExamined()) {
					switch(tile.getType()) {
						case 0:
							buttonType = ActionButtonType.WOODCUT;
							break;
						case 1:
							buttonType = ActionButtonType.HUNT;
							break;
						case 2:
							buttonType = ActionButtonType.COLLECTBERRIES;
							break;
						case 4:
							buttonType = ActionButtonType.MINE;
							break;
						case 5:
							buttonType = ActionButtonType.MINE;
							break;
						case 6:
							buttonType = ActionButtonType.FISH;
							break;	
						case 8:
							buttonType = ActionButtonType.FISH;
							break;	
						default:
							break;
					}
				}
				else {
					switch(tile.getType()) {
						case 0:
							buttonType = ActionButtonType.COLLECTTWIG;
							//Logger.writeToLog("beállít COLLECTTWIG");
							break;
						case 2:
							buttonType = ActionButtonType.COLLECTTWIG;
							//Logger.writeToLog("beállít COLLECTTWIG2");
							break;	
						default: break;
					}
				}
				break;
			case 2:
				if(!tile.isExamined()) {
					switch(tile.getType()) {
						case 0:
							buttonType = ActionButtonType.COLLECTTWIG;
							//Logger.writeToLog("beállít COLLECTTWIG");
							break;
						case 2:
							buttonType = ActionButtonType.COLLECTTWIG;
							//Logger.writeToLog("beállít COLLECTTWIG2");
							break;	
						default: break;
					}
				}
				break;
			default: break;
		}
		int actionButtonCount = getActionButtonCount(homeid, usertileid,tile);
		if(position+1 == actionButtonCount && homeid == 0 && usertileid == tile.getId()) {
			buttonType = ActionButtonType.SETTLEDOWN;
		}
		return buttonType;
	}
	/**
	 * Visszaadja a gombot a megadott sorszámon
	 * @param context
	 * @param position A sorszám
	 * @param tile A terület
	 * @return A gomb
	 */
	public static ImageButton getActionButton(Context context, int position, Tile tile) {
		ImageButton button = new ImageButton(context);
		ActionButtonType type = getActionButtonType(position, tile, Storage.getHomeId(context), Storage.getUserTileId(context));
		button.setId(position);
		LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		button.setPadding(25, 25, 25, 25);
		button.setLayoutParams(params);
		ActionButtonClickListener l = new ResourceAction.ActionButtonClickListener(type);
		switch(type) {
			case EXAMINE:
				button.setImageResource(R.drawable.discovery_mode_icon32);
				break;
			case WOODCUT:
				button.setImageResource(R.drawable.axe_icon32);
				break;
			case TRAVEL:
				button.setImageResource(R.drawable.travel_icon32);
				break;
			case SETTLEDOWN:
				button.setImageResource(R.drawable.settledown_icon32);
				break;
			case COLLECTTWIG:
				button.setImageResource(R.drawable.twig_icon32);
				break;
			case HUNT:
				button.setImageResource(R.drawable.hunt_icon32);
				break;
			case MINE:
				button.setImageResource(R.drawable.mine_icon32);
				break;
			case COLLECTBERRIES:
				button.setImageResource(R.drawable.berry_icon_32);
				break;
			case FISH:
				button.setImageResource(R.drawable.fishing_icon_32);
				break;
			default:
				button.setImageResource(R.drawable.cancel);
				break;
		}
		button.setOnClickListener(l);
		return button;
	}
	
	/**
	 * A gombok megnyomásának figyeléséért felelõs
	 * @author Albert
	 *
	 */
	static class ActionButtonClickListener implements OnClickListener {
		private ActionButtonType type;
		public ActionButtonClickListener(ActionButtonType type) {
			this.type = type;
		}
		@Override
		public void onClick(View v) {
			switch(type) {
			case EXAMINE:
				//Logger.writeToLog("examine?");
				if(mListener!=null) mListener.onExamineEvent();
				break;
			case WOODCUT:
				//Logger.writeToLog("woodcut");
				if(mListener!=null) mListener.onActionEvent(ActionButtonType.WOODCUT);
				break;
			case COLLECTTWIG:
				//Logger.writeToLog("COLLECTTWIG");
				if(mListener!=null) mListener.onActionEvent(ActionButtonType.COLLECTTWIG);
				break;
			case HUNT:
				//Logger.writeToLog("HUNT");
				if(mListener!=null) mListener.onActionEvent(ActionButtonType.HUNT);
				break;
			case COLLECTBERRIES:
				//Logger.writeToLog("COLLECTBERRIES");
				if(mListener!=null) mListener.onActionEvent(ActionButtonType.COLLECTBERRIES);
				break;
			case MINE:
				//Logger.writeToLog("MINE");
				if(mListener!=null) mListener.onActionEvent(ActionButtonType.MINE);
				break;
			case FISH:
				//Logger.writeToLog("FISH");
				if(mListener!=null) mListener.onActionEvent(ActionButtonType.FISH);
				break;
			case SETTLEDOWN:
				//Logger.writeToLog("SETTLEDOWN");
				if(mListener!=null) mListener.onSettleDownEvent();
				break;
			case TRAVEL:
				if(mListener!=null) mListener.onTravelEvent();
				break;
			default:
				//Logger.writeToLog("wat");
				break;
			}
		}
		
	}
}
