package edu.ucdavis.gwt.gis.client.toolbar.menu;

import com.google.gwt.user.client.ui.Image;

import edu.ucdavis.gwt.gis.client.extras.InfoPopup;
import edu.ucdavis.gwt.gis.client.help.MainHelpMenu;
import edu.ucdavis.gwt.gis.client.toolbar.button.HelpButton;
import edu.ucdavis.gwt.gis.client.toolbar.button.InfoButton;

public class HelpMenu extends ToolbarMenuItem {

	private InfoButton mapInfo = new InfoButton(); 
	private HelpButton helpButton = new HelpButton(); 
	
	public HelpMenu() {
	    if( MainHelpMenu.INSTANCE.hasTopics() ) {
	        addItem(helpButton);
	    }
		addItem(mapInfo);
	}
	
	@Override
	public Image getIcon() {
		return null;
	}

	@Override
	public String getText() {
		return "Help";
	}
	
	public InfoPopup getInfoPopup() {
		return mapInfo.getPopup();
	}

}
