package edu.ucdavis.gwt.gis.client.config;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;

/**
 * Main config object for application
 * 
 * @author jrmerz
 */
public class GadgetConfig extends JavaScriptObject {
	
	protected GadgetConfig() {}

	/**
	 * Get the list of datalayers
	 * 
	 * @return JsArray<LayerConfig>
	 */
	public final native JsArray<LayerConfig> getDataLayers() /*-{
		if( this.datalayers ) return this.datalayers;
		return [];
	}-*/;

	/**
	 * Get the list of additional basemaps
	 * 
	 * @return JsArray<BasemapConfig>
	 */
	public final native JsArray<BasemapConfig> getAdditionalBasemaps() /*-{
		if( this.basemaps ) return this.basemaps;
		return [];
	}-*/;
	
	public final native JsArray<LayerConfig> getIntersectLayers() /*-{
		if( this.intersectLayers ) return this.intersectLayers;
		return [];
	}-*/;
	
	/**
	 * Get the provider config object
	 * 
	 * @return Provider Config
	 */
	public final native ProviderConfig getProvider() /*-{
		if( this.provider ) return this.provider;
		return {};
	}-*/;

	/**
	 * Is a center point provided in the config object
	 * 
	 * @return boolean
	 */
	public final native boolean hasCenterPoint() /*-{
		if( this.center ) return true;
		return false;
	}-*/;
	
	/**
	 * get the center x coordinate
	 * 
	 * @return double
	 */
	public final native double getCenterx() /*-{
		if( this.center ) return this.center[0];
		return 0; 
	}-*/;
	
	/**
	 * get the center y coordinate
	 * 
	 * @return double
	 */
	public final native double getCentery() /*-{
		if( this.center ) return this.center[1];
		return 0; 
	}-*/;
	
	/**
	 * get the zoom level
	 * 
	 * @return int
	 */
	public final native int getZoomLevel() /*-{
		if( this.zoom ) return this.zoom;
		return 7;
	}-*/;

	/**
	 * get the div tag this entire application is to be anchored to
	 * 
	 * @return String
	 */
	public final native String getAnchorDiv() /*-{
		if( this.anchor ) return this.anchor;
		return "";
	}-*/;
	
	/**
	 * Get the string that will be appended on to the end of
	 * every geocoder search
	 * 
	 * @return String
	 */
	public final native String getGeocoderBias() /*-{
		if( this.geocoderBias ) return this.geocoderBias;
		return "";
	}-*/;

	/**
	 * Title of the application
	 * 
	 * @return String
	 */
	public final native String getTitle() /*-{
		if( this.title ) return this.title;
		return "";
	}-*/;
	
	/**
	 * Set a ESRI proxy server
	 * 
	 * @return String
	 */
	public final native String getProxy() /*-{
		if( this.proxy ) return this.proxy;
		return "";
	}-*/;
	
	
	public final native String getGeometryServer() /*-{
		if( this.geometryServer ) return this.geometryServer;
		return "";
	}-*/;
	
	public final native boolean hasPrintConfig() /*-{
        if( this.print ) return true;
        return false;
     }-*/;
	
	public final native PrintConfig getPrintConfig() /*-{
       if( this.print ) return this.print;
       return {};
    }-*/;
	
	public final native boolean enableIdentifyTool() /*-{
		if( this.identifyTool ) return this.identifyTool;
		return false;
	}-*/;
	
	public final native boolean enableShapefileUpload() /*-{
        if( this.shapefileUpload != null ) return this.shapefileUpload;
        return true;
    }-*/;

	
	public final native DriveConfig getGoogleDriveConfig() /*-{
		if( this.drive ) return this.drive;
		return {};
	}-*/;
	
	public final native boolean hasGoogleDriveConfig() /*-{
        if( this.drive != null ) return true;
        return false;
    }-*/;
	
	/**
	 * Services that should be used in a search box search
	 * 
	 * @return
	 */
	public final native JsArray<SearchServiceConfig> getSearchServices() /*-{
       if( this.searchServices ) return this.searchServices;
       return [];
    }-*/;
	
	public final native PolyStyleConfig getSearchColor() /*-{
	    if( this.searchColor ) return this.searchColor;
	    return {};
	}-*/;
	
	/**
	 * Should a layer panel allow you to browser the arcgis server?
	 */
	public final native boolean enableBrowseServer() /*-{
	    if( this.enableBrowseServer != null ) return this.enableBrowseServer;
	    return true;
	}-*/;
	
	public final native boolean enableGeolocate() /*-{
        if( this.enableGeolocate != null ) return this.enableGeolocate;
        return true;
    }-*/;
	
	public final native JsArray<HelpTopicConfig> getHelpTopics() /*-{
	    if( this.help ) return this.help
	    return [];
	}-*/;
	
	public final native boolean hasXhrTimeout() /*-{
	    if( this.xhrTimeout != null ) return true;
	    return false;
	}-*/;
	
	public final native int getXhrTimeout() /*-{
	    return this.xhrTimeout;
	}-*/;
	
	
}
