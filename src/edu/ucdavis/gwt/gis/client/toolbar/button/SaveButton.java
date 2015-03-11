package edu.ucdavis.gwt.gis.client.toolbar.button;

import edu.ucdavis.gwt.gis.client.state.SavePopup;
import edu.ucdavis.gwt.gis.client.toolbar.Toolbar;

public class SaveButton extends ToolbarItem {

	
	public SaveButton() {}

	@Override
	public String getIcon() {	
		return "<i class='fa fa-save'></i>";
	}

	@Override
	public void onAdd(Toolbar toolbar) {}

	public void onClick() {
		SavePopup.INSTANCE.show();
	}

	@Override
	public String getText() {
		return "Save Map Locally";
	}

}
