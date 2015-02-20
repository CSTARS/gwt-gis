package edu.ucdavis.gwt.gis.client.config;

import com.google.gwt.core.client.JavaScriptObject;

public class PrintConfig extends JavaScriptObject {
    
    protected PrintConfig() {}
    
    public final native String getServer() /*-{
        if( this.server ) return this.server;
    }-*/;
    
    public final native String getDefaultTemplate() /*-{
        if( this.defaultTemplate ) return this.defaultTemplate;
        return "";
    }-*/;

    public final native boolean allowTemplateSelection() /*-{
        if( this.allowTemplateSelection != null ) return this.allowTemplateSelection;
        return true;
    }-*/;
    
    public final native String getDefaultFormat() /*-{
        if( this.defaultFormat ) return this.defaultFormat;
        return "";
    }-*/;

    public final native boolean allowFormatSelection() /*-{
        if( this.allowFormatSelection != null ) return this.allowFormatSelection;
        return true;
    }-*/;
    
    public final native String getDefaultTitle() /*-{
        if( this.defaultTitle ) return this.defaultTitle;
        return "";
    }-*/;
    
    public final native boolean allowTitleSelection() /*-{
        if( this.allowTitleSelection != null ) return this.allowTitleSelection;
        return true;
    }-*/;
    
    public final native boolean allowLegendSelection() /*-{
        if( this.allowLegendSelection != null ) return this.allowLegendSelection;
        return true;
    }-*/;
}
