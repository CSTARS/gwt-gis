package edu.ucdavis.gwt.gis.client.toolbar.menu;

import com.google.gwt.user.client.ui.Image;

import edu.ucdavis.gwt.gis.client.AppManager;
import edu.ucdavis.gwt.gis.client.GisClient;
import edu.ucdavis.gwt.gis.client.toolbar.button.GoogleDriveButton;
import edu.ucdavis.gwt.gis.client.toolbar.button.LoadArcGisButton;
import edu.ucdavis.gwt.gis.client.toolbar.button.LoadButton;
import edu.ucdavis.gwt.gis.client.toolbar.button.SaveButton;

public class SaveLoadMenu extends ToolbarMenuItem {

	private LoadArcGisButton loadArcGis = new LoadArcGisButton(); 
	
	public SaveLoadMenu() {
		addItem(loadArcGis);
		
		if( !GisClient.isIE7() && !GisClient.isIE8() ) {
			addItem(new SaveButton());
			addItem( new LoadButton());
		}
		
		if( AppManager.INSTANCE.getConfig().hasGoogleDriveConfig() ) {
		    addItem(new GoogleDriveButton());
		}
	}
	
	@Override
	public Image getIcon() {
		return null;
	}

	@Override
	public String getText() {
		return "Save/Load";
	}


}
