package edu.ucdavis.gwt.gis.client.arcgiscom.overlay;

import com.google.gwt.core.client.JavaScriptObject;

public class AuthResponse extends JavaScriptObject {
	
	protected AuthResponse() {}
	
	public final native String getToken() /*-{
		if( this.token ) return this.token;
		return "";
	}-*/;
	
	public final native int getExpires() /*-{
		if( this.expires ) return this.expires;
		return 0;
	}-*/;
	
	public final native boolean isError() /*-{
		if( this.error ) return true;
		return false;
	}-*/;
	
	public final native Error getError() /*-{
		if( this.error ) return this.error;
		return {};
	}-*/;
	
}
