package edu.ucdavis.gwt.gis.client.arcgiscom.overlay.features;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;

public class FeatureSet extends JavaScriptObject {
	
	protected FeatureSet() {}
	
	public final native String getGeometryType() /*-{
		if( this.geometryType ) return this.geometryType;
		return "";
	}-*/;
	
	public final native JsArray<JavaScriptObject> getGraphics() /*-{
		if( this.features ) return this.features;
		return [];
	}-*/;
	


}
