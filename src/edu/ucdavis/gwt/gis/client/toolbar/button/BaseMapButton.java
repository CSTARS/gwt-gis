package edu.ucdavis.gwt.gis.client.toolbar.button;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.ui.Image;

import edu.ucdavis.cstars.client.MapWidget;
import edu.ucdavis.gwt.gis.client.resources.GadgetResources;
import edu.ucdavis.gwt.gis.client.toolbar.BasemapGallery;
import edu.ucdavis.gwt.gis.client.toolbar.Toolbar;

public class BaseMapButton extends ToolbarItem {
	
	private MapWidget map = null;
	
	public BaseMapButton() {}
	

	public void add(String name, String url, String iconUrl ) {
		if( iconUrl.length() == 0 ) iconUrl = url+"/export?bbox=-20037507.0671618%2C-19971868.8804086%2C20037507.0671618%2C" +
				"19971868.8804086&bboxSR=&layers=&layerdefs=&size=100%2C+67&imageSR=&format=png&transparent=true&dpi=&time=" +
				"&layerTimeOptions=&f=image";
		
		BasemapGallery.INSTANCE.addBasemap(name, url, iconUrl);
	}
	
	
	public void init(MapWidget mapWidget) {
		map = mapWidget;
	}


	@Override
	public String getIcon() {
		return "<i class='icon-picture'></i>";
	}


	@Override
	public String getText() {
		return "Select Basemap";
	}


	@Override
	public void onAdd(Toolbar toolbar) {}

	public void onClick() {
		BasemapGallery.INSTANCE.show();
	}

}
