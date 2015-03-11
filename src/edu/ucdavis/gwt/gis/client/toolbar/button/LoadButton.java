package edu.ucdavis.gwt.gis.client.toolbar.button;

import edu.ucdavis.gwt.gis.client.state.LoadPopup;
import edu.ucdavis.gwt.gis.client.toolbar.Toolbar;

public class LoadButton extends ToolbarItem {

	
	public LoadButton() {}

	@Override
	public String getIcon() {	
		return "<i class='fa fa-hdd-o'></i>";
	}

	@Override
	public void onAdd(Toolbar toolbar) {}

	public void onClick() {
		LoadPopup.INSTANCE.show();
	}

	@Override
	public String getText() {
		return "Load Local Map";
	}

}
