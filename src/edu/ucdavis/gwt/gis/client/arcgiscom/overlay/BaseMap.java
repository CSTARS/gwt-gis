package edu.ucdavis.gwt.gis.client.arcgiscom.overlay;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;

public class BaseMap extends JavaScriptObject {
	
	protected BaseMap() {}
	
	public final native String getTitle() /*-{
		if( this.title ) return this.title;
		return "";
	}-*/;
	
	public final native JsArray<BaseMapLayer> getBaseMapLayers() /*-{
		if( this.baseMapLayers ) return this.baseMapLayers;
		return [];
	}-*/;

}
