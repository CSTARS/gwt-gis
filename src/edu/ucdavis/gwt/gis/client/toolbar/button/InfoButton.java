package edu.ucdavis.gwt.gis.client.toolbar.button;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.ui.Image;

import edu.ucdavis.gwt.gis.client.AppManager;
import edu.ucdavis.gwt.gis.client.Debugger;
import edu.ucdavis.gwt.gis.client.config.GadgetConfig;
import edu.ucdavis.gwt.gis.client.config.ProviderConfig;
import edu.ucdavis.gwt.gis.client.extras.InfoPopup;
import edu.ucdavis.gwt.gis.client.resources.GadgetResources;
import edu.ucdavis.gwt.gis.client.toolbar.Toolbar;

public class InfoButton extends ToolbarItem {
	
	private InfoPopup popup = new InfoPopup();
	private Image img = new Image();
	
	public InfoButton() {
		try {
			GadgetConfig gConfig = AppManager.INSTANCE.getConfig();
			if( gConfig != null ) {
				ProviderConfig config = gConfig.getProvider();
				popup.setDescription(config.getPopupContent());
				popup.setLinkUrl(config.getUrl());
			}
		} catch (Exception e) {
			Debugger.INSTANCE.catchException(e,"InfoButton", "setting popup info");
		}
	}

	public InfoPopup getPopup() {
		return popup;
	}
	
	@Override
	public String getIcon() {	
		return "<i class='fa fa-info-circle'></i>";
	}

	@Override
	public void onAdd(Toolbar toolbar) {}

	public void onClick() {
		popup.show();
	}

	@Override
	public String getText() {
		return "About";
	}

}
