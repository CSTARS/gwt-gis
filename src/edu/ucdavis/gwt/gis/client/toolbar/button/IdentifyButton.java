package edu.ucdavis.gwt.gis.client.toolbar.button;

import edu.ucdavis.gwt.gis.client.identify.IdentifyTool;
import edu.ucdavis.gwt.gis.client.toolbar.Toolbar;

public class IdentifyButton extends ToolbarItem {
		
	public IdentifyButton() {}
		
	@Override
	public String getIcon() {
		return "<i class='fa fa-info'></i>";
	}

	@Override
	public void onAdd(Toolbar toolbar) {}

	public void onClick() {
		IdentifyTool.INSTANCE.show();
	}

	@Override
	public String getText() {
		return "Identify Tool";
	}
}
