package edu.ucdavis.gwt.gis.client.toolbar.button;

import edu.ucdavis.gwt.gis.client.drive.GoogleDrive;
import edu.ucdavis.gwt.gis.client.toolbar.Toolbar;

public class GoogleDriveButton extends ToolbarItem {
	
	public GoogleDriveButton() {}

	@Override
	public String getIcon() {	
		return "<i class='fa fa-cloud'></i>";
	}

	@Override
	public void onAdd(Toolbar toolbar) {}


	public void onClick() {
		GoogleDrive.INSTANCE.show();
	}

	@Override
	public String getText() {
		return "Google Drive";
	}

}
