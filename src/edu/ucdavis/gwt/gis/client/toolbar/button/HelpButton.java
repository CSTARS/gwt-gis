package edu.ucdavis.gwt.gis.client.toolbar.button;

import edu.ucdavis.gwt.gis.client.help.MainHelpMenu;
import edu.ucdavis.gwt.gis.client.toolbar.Toolbar;

public class HelpButton extends ToolbarItem {

	public HelpButton() {}
	
	@Override
	public String getIcon() {	
		return "<i class='fa fa-question-circle'></i>";
	}

	@Override
	public void onAdd(Toolbar toolbar) {}

	public void onClick() {
		MainHelpMenu.INSTANCE.show();
	}

	@Override
	public String getText() {
		return "Application Help";
	}

}
