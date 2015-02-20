package edu.ucdavis.gwt.gis.client.arcgiscom.overlay;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsArrayString;

/**
 * Overlay for http://www.arcgis.com/sharing/content/users/[username] response
 * 
 * @author jrmerz
 */
public class UserInfoResponse extends JavaScriptObject {

	protected UserInfoResponse() {}
	
	public final native String getUsername() /*-{
		if( this.username ) return this.username;
		return "";
	}-*/;
	
	public final native String getCurrentFolder() /*-{
		if( this.currentFolder ) return this.currentFolder;
		return "";
	}-*/;
	
	public final native JsArray<MapOverview> getItems() /*-{
		if( this.items ) return this.items;
		return [];
	}-*/;
	
	public final native JsArrayString getFolders() /*-{
		if( this.folders ) return this.folders;
		return [];
	}-*/;
	
	public final native Error getError() /*-{
		if( this.error ) return this.error;
		return {};
	}-*/;
	
	public final native boolean isError() /*-{
		if( this.error ) return true;
		return false;
	}-*/;
	
}
