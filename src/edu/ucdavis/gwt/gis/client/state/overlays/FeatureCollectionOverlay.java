package edu.ucdavis.gwt.gis.client.state.overlays;

import com.google.gwt.core.client.JsArray;

public class FeatureCollectionOverlay extends DataLayerOverlay {
	
	protected FeatureCollectionOverlay() {}	
	
	public final native JsArray<GraphicsLayerOverlay> getGraphicsLayers() /*-{
		if( this.graphicsLayers ) return this.graphicsLayers;
		return [];
	}-*/;

}
