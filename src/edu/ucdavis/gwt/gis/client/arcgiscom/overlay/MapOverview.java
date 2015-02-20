package edu.ucdavis.gwt.gis.client.arcgiscom.overlay;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArrayNumber;
import com.google.gwt.core.client.JsArrayString;

public class MapOverview extends JavaScriptObject {
	
	protected MapOverview() {}
	
	public final native String getId() /*-{
		if( this.id ) return this.id;
		return "";
	}-*/;
	
	public final native String getTitle() /*-{
		if( this.title ) return this.title;
		return "";
	}-*/;
	
	public final native String getOwner() /*-{
		if( this.owner ) return this.owner;
		return "";
	}-*/;
	
	public final native int getUploaded() /*-{
		if( this.uploaded ) return this.uploaded;
		return 0;
	}-*/;
	
	public final native int getModified() /*-{
		if( this.modified ) return this.modified;
		return 0;
	}-*/;

	public final native String getType() /*-{
		if( this.type ) return this.type;
		return "";
	}-*/;
	
	public final native JsArrayString getTypeKeywords() /*-{
		if( this.typeKeywords ) return this.typeKeywords;
		return [];
	}-*/;
	
	public final native JsArrayString getTags() /*-{
		if( this.tags ) return this.tags;
		return [];
	}-*/;
	
	public final native String getSnippet() /*-{
		if( this.snippet ) return this.snippet;
		return "";
	}-*/;
	
	public final native JsArrayNumber getExtent() /*-{
		if( this.extent ) {
			 return [ this.extent[0][0], this.extent[0][1], this.extent[1][0], this.extent[1][1] ];
		}
		return [];
	}-*/;
	
}
