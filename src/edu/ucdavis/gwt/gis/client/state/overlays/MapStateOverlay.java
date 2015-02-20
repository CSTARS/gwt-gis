package edu.ucdavis.gwt.gis.client.state.overlays;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;

public class MapStateOverlay extends JavaScriptObject {

	protected MapStateOverlay() {}
	
	public final native String getName() /*-{
		if( this.name ) return this.name;
		return "";
	}-*/;
	
	public final native double getCenterX() /*-{
		if( this.centerX ) return this.centerX;
		return 0;
	}-*/;
	
	public final native double getCenterY() /*-{
		if( this.centerY ) return this.centerY;
		return 0;
	}-*/;
	
	public final native double getLevel() /*-{
		if( this.level ) return this.level;
		return 0;
	}-*/;
	
	public final native String getSelectedBasemap() /*-{
		if( this.selectedBasemap ) return this.selectedBasemap;
		return "";
	}-*/;
	
	public final native boolean isBasemapVisible() /*-{
		if( this.basemapVisible == null ) return true;
		if( this.basemapVisible ) return true;
		return false;
	}-*/;
	
	public final native JsArray<BasemapOverlay>  getBasemaps() /*-{
		if( this.basemaps ) return this.basemaps;
		return [];
	}-*/;
	
	public final native JsArray<JavaScriptObject>  getGraphics() /*-{
	if( this.graphics ) return this.graphics;
	return [];
}-*/;
	
	public final native JsArray<JavaScriptObject> getDataLayers() /*-{
		if( this.datalayers ) return this.datalayers;
		return [];
	}-*/;
	
}
