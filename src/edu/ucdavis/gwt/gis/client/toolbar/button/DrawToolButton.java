package edu.ucdavis.gwt.gis.client.toolbar.button;

import edu.ucdavis.gwt.gis.client.AppManager;
import edu.ucdavis.gwt.gis.client.draw.DrawControl;
import edu.ucdavis.gwt.gis.client.toolbar.Toolbar;

public class DrawToolButton extends ToolbarItem {
	
	public DrawToolButton(){
		AppManager.INSTANCE.getMap().addControl(DrawControl.INSTANCE);
	}

	@Override
	public String getIcon() {
		return "<i class='fa fa-pencil'></i>";
	}

	@Override
	public void onAdd(Toolbar toolbar) {}


	public void onClick() {
		DrawControl.INSTANCE.enable(!DrawControl.INSTANCE.isEnabled());
		AppManager.INSTANCE.getClient().getToolbar().hideMenu();
	}

	@Override
	public String getText() {
		return "Draw Tool";
	}

}
