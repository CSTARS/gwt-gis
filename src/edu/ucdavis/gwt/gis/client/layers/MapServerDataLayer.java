package edu.ucdavis.gwt.gis.client.layers;


import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.http.client.URL;
import com.google.gwt.user.client.Window;

import edu.ucdavis.cstars.client.Error;
import edu.ucdavis.cstars.client.MapWidget;
import edu.ucdavis.cstars.client.callback.ProjectCallback;
import edu.ucdavis.cstars.client.event.ErrorHandler;
import edu.ucdavis.cstars.client.event.LayerLoadHandler;
import edu.ucdavis.cstars.client.geometry.Extent;
import edu.ucdavis.cstars.client.geometry.Geometry;
import edu.ucdavis.cstars.client.layers.ArcGISDynamicMapServiceLayer;
import edu.ucdavis.cstars.client.layers.ArcGISTiledMapServiceLayer;
import edu.ucdavis.cstars.client.layers.Layer;
import edu.ucdavis.cstars.client.layers.LayerInfo;
import edu.ucdavis.cstars.client.restful.RestfulDocumentInfo;
import edu.ucdavis.cstars.client.restful.RestfulServicesDirectory;
import edu.ucdavis.cstars.client.tasks.GeometryService;
import edu.ucdavis.gwt.gis.client.AppManager;
import edu.ucdavis.gwt.gis.client.Debugger;
import edu.ucdavis.gwt.gis.client.MapEventStatusHandler;
import edu.ucdavis.gwt.gis.client.ajax.RequestManager;
import edu.ucdavis.gwt.gis.client.ajax.RequestManager.Request;
import edu.ucdavis.gwt.gis.client.config.LayerConfig;
import edu.ucdavis.gwt.gis.client.state.overlays.MapServerOverlay;

public class MapServerDataLayer extends DataLayer {
	
	// are we using a tiled map layer
	private boolean isTiled = false;
	
	// defined max extent
	private Extent maxExtent = null;
	
	// is there an option to use a tiled map layer
	private boolean hasTileOption = false;

	// holds the popup window data
	private RestfulDocumentInfo docInfo = null;  
	
	// holds the icons
	private JsArray<LayerInfo> legendInfo = null;
	
	// holds other layer info
	private JsArray<LayerInfo> layersInfo = null;
	
	// layer data
	private JavaScriptObject subLayerData = null;
	
	// legend load error
	private boolean errorLoadingLegend = false;
	
	// config data;
	private boolean legendIsGradient = false;
	
	private Layer mapLayer = null;
	
	// map layer error message
	private Error error = null;
	
	// loading variables
	private int requestsLoaded = 0;
	private int totalRequests = 4;
	
	public MapServerDataLayer(MapServerOverlay overlay) {
		super(overlay.getUrl(), overlay.getLabel(), (int) Math.floor((overlay.getOpacity()*100)) );
		
		defaultVisible = overlay.isVisible();
		setLegendIsGradient(overlay.legendIsGradient());
		setLegendUrl(overlay.getLegendUrl());
		setShowLegendOnLoad(overlay.showLegendOnLoad());
		
		hasTileOption = overlay.hasTiledOption();
		isTiled = overlay.isTiled();
		
		setType(DataLayerType.MapServer);
		
		loadLegend();
		loadData();
		loadSubLayerData();		
	}
	
	public MapServerDataLayer(LayerConfig config) {
		super(config);
		setType(DataLayerType.MapServer);
		setLegendIsGradient(config.legendIsGradient());
		setShowLegendOnLoad(config.showLegend());
		loadLegend();
		loadData();
		loadSubLayerData();
	}
	
	public MapServerDataLayer(String url, String title, double opacity) {
		super(url, title, opacity);
		setLegendUrl(url+"/legend?f=json");
		setType(DataLayerType.MapServer);
		loadLegend();
		loadData();
		loadSubLayerData();
	}
	
	public void reload() {
		AppManager.INSTANCE.getClient().removeLayer(this);
		handlers.clear();
		isLoaded = false;
		errorLoadingData = false;
		errorLoadingLegend = false;
		requestsLoaded = 0;
		error = null;
		
		setDomainToken(AppManager.INSTANCE.getDomainAccess().getDomainToken(getUrl()));
		createLayer();
		loadLegend();
		loadData();
		loadSubLayerData();
		
		addLoadHandler(new DataLayerLoadHandler(){
			@Override
			public void onDataLoaded(DataLayer dataLayer) {
				for( DataLayerReloadHandler handler: reloadHandlers ) {
					handler.onDataReloaded(dataLayer);
				}
			}
		});
		
		AppManager.INSTANCE.getClient().addLayer(this);
	}

	
	protected void createLayer() {
		if( isTiled ) mapLayer = createTiledLayer();
		else mapLayer = createDynamicLayer();

		mapLayer.addLoadHandler(new LayerLoadHandler(){
			@Override
			public void onLoad(Layer layer) {
				checkLoaded();
			}
		});
		mapLayer.addErrorHandler(new ErrorHandler(){
			@Override
			public void onError(Error e) {
				error = e;
				checkLoaded();
				Debugger.INSTANCE.catchException(error.getName(), error.getMessage(), "MapServerDataLayer", "Error adding "+getLabel()+" layer to map");
			}
		});
		
		AppManager.INSTANCE.getMap().addLayer(mapLayer);
		MapEventStatusHandler.INSTANCE.addLayer(mapLayer, getLabel());
	}
	
	private Layer createTiledLayer() {
		ArcGISTiledMapServiceLayer.Options options =  ArcGISTiledMapServiceLayer.Options.create();
		options.setId("datalayer-"+baseId);
		options.setOpacity(getDefaultOpacity());
		options.setVisible(defaultVisible);
		return ArcGISTiledMapServiceLayer.create(getDataUrl(), options);
	}
	
	private Layer createDynamicLayer() {
		ArcGISDynamicMapServiceLayer.Options options =  ArcGISDynamicMapServiceLayer.Options.create();
		options.setId("datalayer-"+baseId);
		options.setOpacity(getDefaultOpacity());
		options.setVisible(defaultVisible);
		return ArcGISDynamicMapServiceLayer.create(getDataUrl(), options);
	}
	
	public boolean hasError() {
		if( error == null ) return false;
		return true;
	}
	
	public Error getError() {
		return error;
	}
	
	public RestfulDocumentInfo getDocInfo() {
		return docInfo;
	}
	
	public JavaScriptObject getSubLayerData() {
		return subLayerData;
	}
	
	protected void checkLoaded() {
		if( !isLoaded ) {
			requestsLoaded++;
			if( requestsLoaded >= totalRequests ) {
				isLoaded = true;
				for( DataLayerLoadHandler handler: handlers)  handler.onDataLoaded(this);
				handlers.clear();
			}
		}
	}
	
	public void setLegendIsGradient(boolean isGradient) {
		legendIsGradient = isGradient;
	}
	
	public boolean legendIsGradient() {
		return legendIsGradient;
	}
	
	public JsArray<LayerInfo> getLegendInfo() {
		return legendInfo;
	}
	
	public boolean isTiled() {
		return  isTiled;
	}
	
	public void setTiled(boolean tiled) {
		if( tiled && hasTileOption ) {
			isTiled = true;
			return;
		}
		isTiled = false;
	}
	
	public Extent getMaxExtent() {
		return maxExtent;
	}
	
	public boolean hasTimeInfo() {
		return _hasTimeInfo(mapLayer);
	}
	
	private native boolean _hasTimeInfo(Layer layer) /*-{
		if( layer.timeInfo != null ) return true;
		return false;
	}-*/;
	
	public boolean hasTileOption() {
		return hasTileOption;
	}
	
	public void zoomToLayerExtent() {
		if( getMaxExtent() != null && !AppManager.INSTANCE.getConfig().getGeometryServer().isEmpty() ) {
			MapWidget map = AppManager.INSTANCE.getMap();
			if( map.getSpatialReference().getWkid() == getMaxExtent().getSpatialReference().getWkid() ) {
				AppManager.INSTANCE.getMap().setExtent(getMaxExtent(), true);
			} else {
				GeometryService gs = GeometryService.create(AppManager.INSTANCE.getConfig().getGeometryServer());
				gs.project(
						new Geometry[] {getMaxExtent()}, 
						map.getSpatialReference(), 
						new ProjectCallback(){
							@Override
							public void onProjectComplete(JsArray<Geometry> geometries) {
								AppManager.INSTANCE.getMap().setExtent((Extent) geometries.get(0), true);
							}
							@Override
							public void onError(Error error) {
								Window.alert(error.getMessage());
							}
						}
				);
			}
		} else {
			Window.alert("This layer does not provide a max extent");
		}
	}
	
	public boolean errorLoadingLegend() {
		return errorLoadingLegend;
	}
	
	private native boolean checkError(JavaScriptObject json) /*-{
		if( json ) {
			if( json.error ) return true;
		}
		return false;
	}-*/;
	
	private void loadSubLayerData() {
		String url = getDataUrl().replaceAll("\\?.*", "")+"/layers?f=json";
		if( hasToken() ) url += "&token="+getDomainToken().getToken();
		
		RequestManager.INSTANCE.makeRequest(
				new Request() {
					@Override
					public void onError() {
						checkLoaded();
						Debugger.INSTANCE.catchException("Failed to load layer sublayer data - "+getLabel(), "", "MapServerDataLayer","");
					}
					@Override
					public void onSuccess(JavaScriptObject json) {
						if( !checkError(json) ) {
							subLayerData = json;
						}
						checkLoaded();
					}				
				}, url
			);
	}
	
	private void loadLegend() {
		String url = getLegendUrl();
		if( hasToken() ) {
			if( url.contains("?") ) url += "&token="+getDomainToken().getToken();
			else url += "?token="+getDomainToken().getToken();
		}
		
		RequestManager.INSTANCE.makeRequest(new Request(){
			@Override
			public void onError() {
				errorLoadingLegend = true;
				checkLoaded();
				Debugger.INSTANCE.catchException("Failed to load layer legend data - "+getLabel(), "", "MapServerDataLayer","");
			}
			@Override
			public void onSuccess(JavaScriptObject json) {
				if( checkError(json) ) {
					loadSoapLegend();
				} else {
					legendInfo = ((RestfulServicesDirectory) json).getLayers();
					checkLoaded();
				}
			}
		}, url);
	}
	
	// try arcgis.com's soap to rest tool
	private void loadSoapLegend() {
		String url = getUrl();
		if( hasToken() ) {
			if( url.contains("?") ) url += "&token="+getDomainToken().getToken();
			else url += "?token="+getDomainToken().getToken();
		}
		
		RequestManager.INSTANCE.makeRequest(new Request(){
			@Override
			public void onError() {
				errorLoadingLegend = true;
				checkLoaded();
				Debugger.INSTANCE.catchException("Failed to load layer legend data - "+getLabel(), "", "MapServerDataLayer","");
			}
			@Override
			public void onSuccess(JavaScriptObject json) {
				if( checkError(json) ) {
					errorLoadingLegend = true;
				} else {
					legendInfo = ((RestfulServicesDirectory) json).getLayers();
				}
				checkLoaded();
			}
		}, "http://utility.arcgis.com/sharing/tools/legend?returnbytes=true&f=json&soapUrl="+URL.encode(url));
	}
	
	private void loadData() {
		RequestManager.INSTANCE.makeRequest(new Request(){
			@Override
			public void onError() {
				errorLoadingData = true;
				checkLoaded();
				createLayer();
				Debugger.INSTANCE.catchException("Failed to load layer data - "+getLabel(), "", "MapServerDataLayer","");
			}
			@Override
			public void onSuccess(JavaScriptObject json) {
				checkLoaded();
				RestfulServicesDirectory dir = (RestfulServicesDirectory) json;
				docInfo = dir.getDocumentInfo();
				if( dir.hasTileInfo() ) {
					hasTileOption = true;
					// only set to tiled is if the sr's match
					if( dir.getSpatialReference() != null ) {
						if( dir.getSpatialReference().getWkid() == AppManager.INSTANCE.getMap().getSpatialReference().getWkid() ) {
							isTiled = true;
						}
					}
				}
				
				layersInfo = dir.getLayers();
				
				maxExtent = dir.getFullExtent();
				if( maxExtent == null ) dir.getInitialExtent();
				createLayer();
			}
		}, getDataUrl());
	}
	
	public JsArray<LayerInfo> getLayersInfo() {
		return layersInfo;
	}
	
	private String getDataUrl() {
		String tmpUrl = getUrl();
		if( tmpUrl.contains("?") ) tmpUrl += "&f=json";
		else tmpUrl += "?f=json";
		
		if( hasToken() ) tmpUrl += "&token="+getDomainToken().getToken();
		
		return tmpUrl;
	}

	// switch from a map layer to a tiled layer
	public void switchLayerType() {
		// first, remove current layer
		AppManager.INSTANCE.getMap().removeLayer(mapLayer);
		MapEventStatusHandler.INSTANCE.removeLayer(mapLayer.getId());
		
		// create new layer
		if( isTiled ) mapLayer = createTiledLayer();
		else mapLayer = createDynamicLayer();

		// add layer to map and status handlers
		AppManager.INSTANCE.getMap().addLayer(mapLayer);
		MapEventStatusHandler.INSTANCE.addLayer(mapLayer, getLabel());
		
		// update legend
		getLayerPanel().clearAndUpdateLegends();
	}
	
	public String toJson() {
		String json = "{"+super.toJson()+",";
		
		json += getJsonKeyValue("opacity", getOpacity())+",";
		json += getJsonKeyValue("isTiled", isTiled())+",";
		json += getJsonKeyValue("hasTileOption", hasTileOption())+",";
		json += getJsonKeyValue("legendIsGradient", legendIsGradient())+"}";
		
		return json;
	}

	@Override
	public String getId() {
		return mapLayer.getId();
	}

	@Override
	public double getOpacity() {
		return mapLayer.getOpacity();
	}

	@Override
	public boolean isVisible() {
		return mapLayer.isVisible();
	}

	@Override
	public void removeFromMap(MapWidget map) {
		map.removeLayer(mapLayer);
	}

	@Override
	public void setOpacity(double opacity) {
		mapLayer.setOpacity(opacity);
	}

	@Override
	public void setVisible(boolean visible) {
		if( mapLayer != null ) {
			mapLayer.setVisibility(visible);
			if( getLayerPanel() != null ) {
				getLayerPanel().updateVisibilityCheckBox();
			}
		}
	}
	
	public Layer getMapLayer() {
		return mapLayer;
	}
	
}
