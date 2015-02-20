package edu.ucdavis.gwt.gis.client.config;

import com.google.gwt.core.client.JavaScriptObject;

public class SearchServiceConfig extends JavaScriptObject {
    
    protected SearchServiceConfig() {}
    
    public final native String getUrl() /*-{
        if( this.url ) return this.url;
        return "";
    }-*/;
    
    public final native String getType() /*-{
        if( this.type ) return this.type;
        return "";
    }-*/;
    
    public final native String getParameter() /*-{
        if( this.parameter ) return this.parameter;
        return "NAME";
    }-*/;
    
    public final native JavaScriptObject getFormatter() /*-{
        return this.format;
    }-*/;

}
