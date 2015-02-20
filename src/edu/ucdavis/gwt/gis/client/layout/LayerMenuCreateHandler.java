package edu.ucdavis.gwt.gis.client.layout;

import com.google.gwt.user.client.ui.VerticalPanel;

import edu.ucdavis.gwt.gis.client.layers.DataLayer;

public interface LayerMenuCreateHandler {
	public LayerMenuItem onCreate(VerticalPanel menu, DataLayer layer);
}
