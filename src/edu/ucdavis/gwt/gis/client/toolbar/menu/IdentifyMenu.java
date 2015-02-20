package edu.ucdavis.gwt.gis.client.toolbar.menu;

import com.google.gwt.user.client.ui.Image;

import edu.ucdavis.gwt.gis.client.toolbar.button.IdentifyButton;

public class IdentifyMenu extends ToolbarMenuItem {
	
	private IdentifyButton button = new IdentifyButton();
	
	public IdentifyMenu() {
		addItem(button);
	}
	
	@Override
	public Image getIcon() {
		return null;
	}

	@Override
	public String getText() {
		return "Identify";
	}
}
