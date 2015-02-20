package edu.ucdavis.gwt.gis.client.arcgiscom.overlay.features;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;


public class FeatureCollection extends JavaScriptObject {

	protected FeatureCollection() {}
	
	public final native double getOpacity() /*-{
		if( this.opacity ) return opacity;
		return 1;
	}-*/;
	
	public final native boolean isVisible() /*-{
		if( this.visibility == null ) return true;
		return this.visibility;
	}-*/;
	
	public final native JsArray<FeatureCollectionLayer> getLayers() /*-{
		if( this.layers ) return this.layers;
		return [];
	}-*/;
	
}
