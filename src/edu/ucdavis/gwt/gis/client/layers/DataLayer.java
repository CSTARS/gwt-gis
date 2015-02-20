package edu.ucdavis.gwt.gis.client.layers;

import java.util.LinkedList;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.user.client.Window;

import edu.ucdavis.cstars.client.MapWidget;
import edu.ucdavis.gwt.gis.client.AppManager;
import edu.ucdavis.gwt.gis.client.auth.DomainToken;
import edu.ucdavis.gwt.gis.client.config.LayerConfig;
import edu.ucdavis.gwt.gis.client.layout.LayerPanel;

public abstract class DataLayer {
	
	private static int count = 0;
	protected int baseId = 0;
	
	// datalayer type
	public enum DataLayerType {
		MapServer,
		KML,
		FeatureCollection,
		ImageServer
	}
	protected DataLayerType type = null;
	
	// current layer on the map
	//protected Layer mapLayer = null;
	
	// need reference for removal / editing
	private LayerPanel layerPanel = null;
	
	// layer data load error
	protected boolean errorLoadingData = false;
	
	// config data;
	private String label = "";

	// url to layer
	private String url = "";
	
	private String legendUrl = "";
	
	protected boolean defaultVisible = false;
	
	protected double defaultOpacity = 80;
	
	private boolean showLegendOnLoad = false;
	
	private boolean displayLayer = true;
	
	protected boolean isLoaded = false;
	
	private DomainToken token = null;
	
	public interface DataLayerLoadHandler {
		public void onDataLoaded(DataLayer dataLayer);
	}
	public interface DataLayerReloadHandler {
		public void onDataReloaded(DataLayer dataLayer);
	}
	protected LinkedList<DataLayerLoadHandler> handlers = new LinkedList<DataLayerLoadHandler>();
	protected LinkedList<DataLayerReloadHandler> reloadHandlers = new LinkedList<DataLayerReloadHandler>();
	
	public DataLayer(LayerConfig config) {
		label = config.getLabel();
		url = config.getUrl();
		legendUrl = config.getLegendUrl();
		defaultVisible = config.isVisible();
		defaultOpacity = config.getOpacity();
		
		token = AppManager.INSTANCE.getDomainAccess().getDomainToken(getUrl());
		
		baseId = count;
		count++;
	}
	
	public DataLayer(String url, String title, double opacity) {
		label = title;
		this.url = url;
		defaultVisible = true;
		defaultOpacity = opacity;
		
		token = AppManager.INSTANCE.getDomainAccess().getDomainToken(getUrl());
		
		baseId = count;
		count++;
	}
	
	protected abstract void createLayer();
	
	protected abstract void checkLoaded();
	
	public boolean hasToken() {
		if( token != null ) return true;
		return false;
	}
	
	public DomainToken getDomainToken() {
		return token;
	}
	
	public void setDomainToken(DomainToken domainToken) {
		this.token = domainToken;
	}
	
	public void setType(DataLayerType type) {
		this.type = type;
	}
	
	public DataLayerType getType() {
		return type;
	}
	
	public void setLegendUrl(String legendUrl) {
		this.legendUrl = legendUrl;
	}
	
	public String getLegendUrl() {
		return legendUrl;
	}
	
	public void setLayerPanel(LayerPanel layerPanel) {
		this.layerPanel = layerPanel;
	}
	
	public LayerPanel getLayerPanel() {
		return layerPanel;
	}
	
	public String getUrl() {
		return url;
	}
	
	public void addLoadHandler(DataLayerLoadHandler handler) {
		if( isLoaded ) {
			handler.onDataLoaded(this);
		} else {
			handlers.add(handler);
		}	
	}
	
	public void addReloadHandler(DataLayerReloadHandler handler) {
		reloadHandlers.add(handler);	
	}
	
	public String getLabel() {
		return label;
	}
	
	public void setLabel(String label) {
		this.label = label;
	}
	
	public void setDisplayLayer(boolean b) {
		this.displayLayer = b;
	}

	public boolean getDisplayLayer() {
		return this.displayLayer;
	}

	//public Layer getMapLayer() {
	//	return mapLayer;
	//}
	
	/*
	 * Abstract layer functions
	 */
	public abstract String getId();
	
	public abstract void setVisible(boolean visible);
	
	public abstract boolean isVisible();
	
	public abstract void setOpacity(double opacity);
	
	public abstract double getOpacity();
	
	public abstract void removeFromMap(MapWidget map);
	
	public void setShowLegendOnLoad(boolean showLegend) {
		showLegendOnLoad = showLegend;
	}
	
	public boolean showLegendOnLoad() {
		return showLegendOnLoad;
	}

	public boolean errorLoadingData() {
		return errorLoadingData;
	}
	
	protected double getDefaultOpacity() {
		double o = defaultOpacity / 100;
		if( o < 0 ) o = 0;
		if( o > 1 ) o = 1;
		return o;
	}
	
	// for saving layer data
	public String toJson() {
		String json = "";
		
		if( getType() == DataLayerType.KML ) {
			json += getJsonKeyValue("type", "kml")+",";
		} else if( getType() == DataLayerType.FeatureCollection ) {
			json += getJsonKeyValue("type", "featurecollection")+",";
		} else {
			json += json += getJsonKeyValue("type", "mapserver")+",";
		}
		
		json += getJsonKeyValue("label", getLabel())+",";
		json += getJsonKeyValue("url", getUrl())+",";
		json += getJsonKeyValue("legendUrl", getLegendUrl())+",";
		
		if( getLayerPanel() != null ) {
			json += getJsonKeyValue("showLegendOnLoad", getLayerPanel().isOpen())+",";
		}
		
		json += getJsonKeyValue("visible", isVisible());

		return json;
	}
	
	protected native String getJsonKeyObject(String key, JavaScriptObject value) /*-{
		var str = JSON.stringify(value);
		return "\""+key+"\":"+str;
	}-*/;
	
	protected String getJsonKeyValue(String key, String value) {
		return "\""+key+"\":\""+value+"\"";
	}

	protected String getJsonKeyValue(String key, boolean value) {
		return "\""+key+"\":"+Boolean.toString(value);
	}
	
	protected String getJsonKeyValue(String key, double value) {
		return "\""+key+"\":"+value;
	}
	
	
}
