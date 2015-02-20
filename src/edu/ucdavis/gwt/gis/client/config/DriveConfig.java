package edu.ucdavis.gwt.gis.client.config;

import com.google.gwt.core.client.JavaScriptObject;

public class DriveConfig extends JavaScriptObject {
	
	protected DriveConfig() {}
	
	// name of folder where files for this app will be saved
	public final native String getFolderName() /*-{
		if( this.folderName ) return this.folderName;
		return "GwtGisApplication";
	}-*/;
	
	// google client id
	// visit here for more information: https://code.google.com/apis/console
	public final native String getClientId() /*-{
		if( this.clientId ) return this.clientId;
		return "";
	}-*/;
	
	// google drive app id (required for sharing)
	// visit here for more information: https://code.google.com/apis/console
	// then click 'Drive SDK', your 'App Id' is located in the top left of the center panel
	public final native String getAppId() /*-{
		if( this.appId ) return this.appId;
		return "";
	}-*/;
	
	// google 'Simple Api Access' Key
	// visit here for more information: https://code.google.com/apis/console
	// then 'Create new Browser key' and set your apps 'refers' in the appropriate box
	// this key is used when a 'non-logged-in' user loads a public map.  The request
	// is then seen as a generic api access and thus, needs a key
	public final native String getApiKey() /*-{
		if( this.apiKey ) return this.apiKey;
		return "";
	}-*/;
	

}
