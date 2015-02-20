package edu.ucdavis.gwt.gis.client.config;

import com.google.gwt.core.client.JavaScriptObject;

/**
 * initial config of the provider information (who is hosting the app)
 * 
 * @author jrmerz
 */
public class ProviderConfig extends JavaScriptObject {
	
	protected ProviderConfig() {}
	
	/**
	 * Url to homepage.
	 * 
	 * @return String
	 */
	public final native String getUrl() /*-{
		if( this.url ) return this.url;
		return "";
	}-*/;

//	public final native String getIconUrl() /*-{
//		if( this.icon ) return this.icon;
//		return "";
//	}-*/;
	
	/**
	 * Text for info popup window.
	 * 
	 * @return String
	 */
	public final native String getPopupContent() /*-{
		if( this.popup ) return this.popup;
		return "";
	}-*/;

	/**
	 * Should the info popup show on startup?
	 * 
	 * @return boolean
	 */
	public final native boolean showOnStart() /*-{
		if( this.showOnStart ) return this.showOnStart;
		return false; 
	}-*/;
	
}
