package edu.ucdavis.gwt.gis.client.config;

import com.google.gwt.core.client.JavaScriptObject;

/**
 * Config object for basemaps
 * 
 * @author jrmerz
 */
public class BasemapConfig extends JavaScriptObject {

	protected BasemapConfig() {}
	
	/**
	 * Name of basemap
	 * 
	 * @return String
	 */
	public final native String getName() /*-{
		if( this.name ) return this.name;
		return "";
	}-*/;
	
	/**
	 * Url of basemap
	 * 
	 * @return String
	 */
	public final native String getUrl() /*-{
		if( this.url ) return this.url;
		return "";
	}-*/;
	
	/**
	 * Icon url of basemap
	 * 
	 * @return String
	 */
	public final native String getIconUrl() /*-{
		if( this.iconUrl ) return this.iconUrl;
		return "";
	}-*/;
	
}
