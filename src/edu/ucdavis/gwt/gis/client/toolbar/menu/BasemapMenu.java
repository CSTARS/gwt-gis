package edu.ucdavis.gwt.gis.client.toolbar.menu;

import com.google.gwt.user.client.ui.Image;

import edu.ucdavis.gwt.gis.client.GisClient;
import edu.ucdavis.gwt.gis.client.extras.InfoPopup;
import edu.ucdavis.gwt.gis.client.toolbar.button.BaseMapButton;
import edu.ucdavis.gwt.gis.client.toolbar.button.InfoButton;

public class BasemapMenu extends ToolbarMenuItem {
	
	public BasemapMenu() {
		addItem(GisClient.basemapSelector);
	}
	
	@Override
	public Image getIcon() {
		return null;
	}

	@Override
	public String getText() {
		return "Basemap";
	}
	
	/*public InfoPopup getInfoPopup() {
		return mapInfo.getPopup();
	}*/

}
