package edu.ucdavis.gwt.gis.client.toolbar.button;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.ui.Image;

import edu.ucdavis.gwt.gis.client.arcgiscom.ArcGisImportPanel;
import edu.ucdavis.gwt.gis.client.resources.GadgetResources;
import edu.ucdavis.gwt.gis.client.toolbar.Toolbar;

public class LoadArcGisButton extends ToolbarItem {

	private ArcGisImportPanel popup = new ArcGisImportPanel();
	
	public LoadArcGisButton() {}

	@Override
	public String getIcon() {	
		return "<i class='fa fa-cloud-download'></i>";
	}

	@Override
	public void onAdd(Toolbar toolbar) {}


	public void onClick() {
		popup.show();
	}

	@Override
	public String getText() {
		return "Load ArcGIS.com Map";
	}

}
