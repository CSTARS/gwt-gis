package edu.ucdavis.gwt.gis.client.toolbar.button;

import edu.ucdavis.gwt.gis.client.export.PrintMapPopup;
import edu.ucdavis.gwt.gis.client.toolbar.Toolbar;

public class PrintMapButton extends ToolbarItem {
	
	private PrintMapPopup popup = new PrintMapPopup();
	
	@Override
	public String getIcon() {
		return "<i class='fa fa-print'></i>";
	}

	@Override
	public void onAdd(Toolbar toolbar) {}

	public void onClick() {
		popup.initAndShow();
	}

	@Override
	public String getText() {
		return "Download/Print Map";
	}

}
