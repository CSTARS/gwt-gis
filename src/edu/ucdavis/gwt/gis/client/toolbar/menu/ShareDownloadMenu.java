package edu.ucdavis.gwt.gis.client.toolbar.menu;

import com.google.gwt.user.client.ui.Image;

import edu.ucdavis.gwt.gis.client.AppManager;
import edu.ucdavis.gwt.gis.client.CompatibilityTester;
import edu.ucdavis.gwt.gis.client.Debugger;
import edu.ucdavis.gwt.gis.client.toolbar.button.PrintMapButton;
import edu.ucdavis.gwt.gis.client.toolbar.button.GoogleDriveButton;


public class ShareDownloadMenu extends ToolbarMenuItem {

	public ShareDownloadMenu() {
	    Debugger.INSTANCE.log("ShareDownloadMenu: Creating shareDownloadMenu");
	    
	    
		if( CompatibilityTester.isCorsSupported() && AppManager.INSTANCE.getConfig().hasPrintConfig() ) {
		    Debugger.INSTANCE.log("ShareDownloadMenu: Cors is supported, adding print button");
			addItem(new PrintMapButton());
		}
		
		if( AppManager.INSTANCE.getConfig().hasGoogleDriveConfig() ) {
		    Debugger.INSTANCE.log("ShareDownloadMenu: adding google drive button");
		    addItem(new GoogleDriveButton());
		}
	}
	
	@Override
	public Image getIcon() {
		return null;
	}

	@Override
	public String getText() {
		return "Share/Print";
	}

}
