package edu.ucdavis.gwt.gis.client.arcgiscom.overlay;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;

/**
 * This is the ArcGis.com map (data) object
 * 
 * http://www.arcgis.com/sharing/content/items/[id]/data
 * 
 * @author jrmerz
 */
public class MapDataResponse extends JavaScriptObject {
	
	protected MapDataResponse() {}
	
	public final native JsArray<OperationalLayer> getOperationalLayers() /*-{
		if( this.operationalLayers ) return this.operationalLayers;
		return [];
	}-*/;
	
	public final native BaseMap getBaseMap() /*-{
		if( this.baseMap ) return this.baseMap;
		return {};
	}-*/;
	
//	public final native String getVersion() /*-{
//		if( this.version ) return this.version;
//		return "";
//	}-*/;
	
	public final native boolean isError() /*-{
		if( this["error"] != null ) return true;
		return false;
	}-*/;
	
	public final native Error getError() /*-{
		if( this.error ) return this.error;
		return {};
	}-*/;
}
