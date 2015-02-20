package edu.ucdavis.gwt.gis.client.layers;


import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.user.client.Window;

import edu.ucdavis.cstars.client.Error;
import edu.ucdavis.cstars.client.MapWidget;
import edu.ucdavis.cstars.client.callback.ProjectCallback;
import edu.ucdavis.cstars.client.event.ErrorHandler;
import edu.ucdavis.cstars.client.event.LayerLoadHandler;
import edu.ucdavis.cstars.client.geometry.Extent;
import edu.ucdavis.cstars.client.geometry.Geometry;
import edu.ucdavis.cstars.client.layers.ArcGISImageServiceLayer;
import edu.ucdavis.cstars.client.layers.Layer;
import edu.ucdavis.cstars.client.restful.RestfulDocumentInfo;
import edu.ucdavis.cstars.client.restful.RestfulLayerInfo;
import edu.ucdavis.cstars.client.restful.RestfulServicesDirectory;
import edu.ucdavis.cstars.client.tasks.GeometryService;
import edu.ucdavis.gwt.gis.client.AppManager;
import edu.ucdavis.gwt.gis.client.Debugger;
import edu.ucdavis.gwt.gis.client.MapEventStatusHandler;
import edu.ucdavis.gwt.gis.client.ajax.RequestManager;
import edu.ucdavis.gwt.gis.client.ajax.RequestManager.Request;
import edu.ucdavis.gwt.gis.client.config.LayerConfig;
import edu.ucdavis.gwt.gis.client.state.overlays.ImageServerOverlay;

public class ImageServerDataLayer extends DataLayer {
	
	// defined max extent
	private Extent maxExtent = null;

	// holds the popup window data
	private RestfulLayerInfo info = null;  
	
	private Layer mapLayer = null;
	
	// map layer error message
	private Error error = null;
	
	// loading variables
	private int requestsLoaded = 0;
	private int totalRequests = 2;
	
	public ImageServerDataLayer(ImageServerOverlay overlay) {
		super(overlay.getUrl(), overlay.getLabel(), (int) Math.floor((overlay.getOpacity()*100)) );
		
		defaultVisible = overlay.isVisible();
		
		setType(DataLayerType.ImageServer);
		
		loadData();
	}
	
	public ImageServerDataLayer(LayerConfig config) {
		super(config);
		setType(DataLayerType.ImageServer);
		loadData();
	}
	
	public ImageServerDataLayer(String url, String title, double opacity) {
		super(url, title, opacity);
		setType(DataLayerType.ImageServer);
		loadData();
	}
	
	public void reload() {
		AppManager.INSTANCE.getClient().removeLayer(this);
		handlers.clear();
		isLoaded = false;
		errorLoadingData = false;
		requestsLoaded = 0;
		error = null;
		
		setDomainToken(AppManager.INSTANCE.getDomainAccess().getDomainToken(getUrl()));
		createLayer();
		loadData();
		
		AppManager.INSTANCE.getClient().addLayer(this);
		
		addLoadHandler(new DataLayerLoadHandler(){
			@Override
			public void onDataLoaded(DataLayer dataLayer) {
				for( DataLayerReloadHandler handler: reloadHandlers ) {
					handler.onDataReloaded(dataLayer);
				}
			}
		});
		
	}

	
	protected void createLayer() {
		ArcGISImageServiceLayer.Options options =  ArcGISImageServiceLayer.Options.create();
		options.setId("datalayer-"+baseId);
		options.setOpacity(getDefaultOpacity());
		options.setVisible(defaultVisible);
		mapLayer = ArcGISImageServiceLayer.create(getDataUrl(), options);

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
	
	public boolean hasError() {
		if( error == null ) return false;
		return true;
	}
	
	public Error getError() {
		return error;
	}
	
	public RestfulLayerInfo getInfo() {
		return info;
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
	
	public Extent getMaxExtent() {
		return maxExtent;
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
								AppManager.INSTANCE.getMap().setExtent((Extent) geometries.get(0));
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
	
	private native boolean checkError(JavaScriptObject json) /*-{
		if( json ) {
			if( json.error ) return true;
		}
		return false;
	}-*/;

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
				RestfulLayerInfo layerInfo = (RestfulLayerInfo) json;
				info = layerInfo;
				maxExtent = info.getExtent();
				if( getLabel().contentEquals("") || 
						getLabel().contentEquals("ImageServer") ) setLabel(info.getName());
				createLayer();
				checkLoaded();
			}
		}, getDataUrl());
	}
	
	private String getDataUrl() {
		String tmpUrl = getUrl();
		if( tmpUrl.contains("?") ) tmpUrl += "&f=json";
		else tmpUrl += "?f=json";
		
		if( hasToken() ) tmpUrl += "&token="+getDomainToken().getToken();
		
		return tmpUrl;
	}
	
	public String toJson() {
		String json = "{"+super.toJson()+",";
		
		json += getJsonKeyValue("opacity", getOpacity())+",";
		
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
		mapLayer.setVisibility(visible);
	}
	
	public Layer getMapLayer() {
		return mapLayer;
	}
	
}
