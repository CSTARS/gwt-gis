package edu.ucdavis.gwt.gis.client.layers;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.user.client.Window;

import edu.ucdavis.cstars.client.Error;
import edu.ucdavis.cstars.client.Graphic;
import edu.ucdavis.cstars.client.InfoTemplate;
import edu.ucdavis.cstars.client.MapWidget;
import edu.ucdavis.cstars.client.Util;
import edu.ucdavis.cstars.client.callback.ProjectCallback;
import edu.ucdavis.cstars.client.event.ErrorHandler;
import edu.ucdavis.cstars.client.event.LayerLoadHandler;
import edu.ucdavis.cstars.client.geometry.Extent;
import edu.ucdavis.cstars.client.geometry.Geometry;
import edu.ucdavis.cstars.client.layers.FeatureLayer;
import edu.ucdavis.cstars.client.layers.Field;
import edu.ucdavis.cstars.client.layers.KMLLayer;
import edu.ucdavis.cstars.client.layers.Layer;
import edu.ucdavis.cstars.client.symbol.Symbol;
import edu.ucdavis.cstars.client.tasks.GeometryService;
import edu.ucdavis.gwt.gis.client.AppManager;
import edu.ucdavis.gwt.gis.client.Debugger;
import edu.ucdavis.gwt.gis.client.MapEventStatusHandler;
import edu.ucdavis.gwt.gis.client.config.LayerConfig;
import edu.ucdavis.gwt.gis.client.state.ClientStateManager;
import edu.ucdavis.gwt.gis.client.state.overlays.KmlOverlay;

public class KmlDataLayer extends DataLayer {

	private String popupTitle = "name";
	private String popupContent = "description";
	private boolean showAllFields = false;
	private FeatureLayer fLayer = null;
	private HashMap<Symbol, String> layerInfos = new HashMap<Symbol, String>();
	private boolean isError = false;
	private String errorMessage = "";
	
	private KMLLayer mapLayer = null;
	
	private KmlOverlay stateInfo = null;
	
	public KmlDataLayer(KmlOverlay overlay) {
		super(overlay.getUrl(), overlay.getLabel(), (int) Math.floor((overlay.getOpacity()*100)) );
		
		defaultVisible = overlay.isVisible();
		setShowLegendOnLoad(overlay.showLegendOnLoad());
		
		stateInfo = overlay;
		
		setType(DataLayerType.KML);
		createLayer();
	}
	
	public KmlDataLayer(LayerConfig config) {
		super(config);
		setType(DataLayerType.KML);
		createLayer();
	}
	
	public KmlDataLayer(String url, String title, int opacity) {
		super(url, title, opacity);
		setType(DataLayerType.KML);
		createLayer();
	}

	
	private native boolean instanceOf(Layer l, String type) /*-{
		type = "esri.layers."+type;
		if( l.declaredClass ) {
			if( l.declaredClass == type ) return true; 
		}
		return false;
	}-*/;
	
	protected void createLayer() {
		KMLLayer.Options options = KMLLayer.Options.create();
		options.setVisible(defaultVisible);
		
		mapLayer = KMLLayer.create(getUrl());

		AppManager.INSTANCE.getMap().addLayer(mapLayer);
		MapEventStatusHandler.INSTANCE.addLayer(mapLayer, getLabel());
		
		mapLayer.addLoadHandler(new LayerLoadHandler(){
			@Override
			public void onLoad(Layer layer) {
				checkLoaded();
			}
		});
		mapLayer.addErrorHandler(new ErrorHandler(){
			@Override
			public void onError(Error error) {
				isError = true;
				errorMessage = error.getMessage();
				checkLoaded();
				Debugger.INSTANCE.catchException(error.getName(), error.getMessage(), "MapServerDataLayer","Failed to load kml - "+getLabel());
			}
		});
	}
	
	public String getErrorMessage() {
		return errorMessage;
	}
	
	private InfoTemplate createInfoTemplate() {
		String content = "${"+popupContent+"}";
		if( showAllFields ) {
			LinkedList<String> names = getFieldNames();
			content = "";
			for( String n: names ) {
				content += "<span style='color:#444444'>"+n+":</span> <span style='color:#888888'>${"+n+"}<br />";
			}
		}
		return InfoTemplate.create("${"+popupTitle+"}", content);
	}
	
	public LinkedList<String> getFieldNames() {
		JsArray<Field> fields = fLayer.getFields();
		LinkedList<String> names = new LinkedList<String>();
		for( int i = 0; i < fields.length(); i++ ) {
			names.add(fields.get(i).getName());	
		}
		return names;
	}
	
	public void updateInfoName(Symbol s, String name) {
		if( layerInfos.containsKey(s) ) {
			layerInfos.put(s, name);
		}
	}
	
	public HashMap<Symbol, String> getLayerInfos() {
		return layerInfos;
	}
	
	public void updateInfoTemplate() {
		fLayer.setInfoTemplate(createInfoTemplate());
	}
	
	public void updateInfoTemplate(String titleField, String contentField, boolean showAll) {
		popupTitle = titleField;
		popupContent = contentField;
		showAllFields = showAll;
		updateInfoTemplate();
	}
	
	public boolean isError() {
		return isError;
	}
	
	protected void checkLoaded() {
		if( !isLoaded ) {
			isLoaded = true;
			if( !isError ) {
				KMLLayer kml = (KMLLayer) mapLayer;
				JsArray<Layer> layers = kml.getLayers();
				for( int i = 0; i < layers.length(); i++ ) {
					if( instanceOf(layers.get(i), "FeatureLayer") ){
						fLayer = (FeatureLayer) layers.get(i);
						populateLayerInfo();
						break;
					}
				}
				updateInfoTemplate();
			} 
			
			for( DataLayerLoadHandler handler: handlers)  handler.onDataLoaded(this);
			handlers.clear();
		}
		
	}

	
	private void populateLayerInfo() {
		for( int i = 0; i < fLayer.getGraphics().length(); i++ ) {

			Graphic g = fLayer.getGraphics().get(i);
			String name = getNiceName(g.getGeometry().getType().getValue());
			Symbol s = fLayer.getRenderer().getSymbol(g);
			
			
			if( s != null ) {
				
				// are we loading the kml from a saved map?
				if( stateInfo != null ) {
					String id = ClientStateManager.md5(new JSONObject(s.toJson()).toString());
					for( int j = 0; j < stateInfo.getNumLayerInfos(); j++ ) {
						if( stateInfo.getSymbolKey(j).contentEquals(id) ) {
							name = stateInfo.getSymbolName(j);
							break;
						}
					}
				}
			
				if( !layerInfos.containsKey(s) ) {
					layerInfos.put(s, name);
				}
			}
		}
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
	
	public Extent getMaxExtent() {
		if( mapLayer.getLayers().length() > 0 ) {
			return _getMaxExtent(mapLayer.getLayers().get(0));
		}
		return null;
	}
	private native Extent _getMaxExtent(Layer l) /*-{
		return l.fullExtent;
	}-*/;
	
	private String getNiceName(String type) {
		if( type.contentEquals("point") ) return "Point Feature(s)";
		if( type.contentEquals("multipoint") ) return "Point Feature(s)";
		if( type.contentEquals("polyline") ) return "Line Feature(s)";
		if( type.contentEquals("polygon") ) return "Polygon(s)";
		return "Feature(s)";
	}
	
	public String toJson() {
		String json = "{"+super.toJson()+",";
		
		json += getJsonKeyValue("popupTitle", popupTitle)+",";
		json += getJsonKeyValue("popupContent", popupContent)+",";
		json += getJsonKeyValue("showAllFields", showAllFields)+",";
		
		
		Iterator<Symbol> i = layerInfos.keySet().iterator();
		json += "\"layerInfos\":[";
		while( i.hasNext() ) {
			Symbol symbol = i.next();
			json += "{\"symbol\":\""+ClientStateManager.md5(new JSONObject(symbol.toJson()).toString())+"\",";
			json += getJsonKeyValue("name", layerInfos.get(symbol))+"}";
			
			if( i.hasNext() ) json += ",";
		}
		
		return json+"]}";
	}
	
	public KMLLayer getMapLayer() {
		return mapLayer;
	}

	@Override
	public String getId() {
		return mapLayer.getId();
	}

	@Override
	public double getOpacity() {
		return 1;
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
		// TODO not implemented yet
	}

	@Override
	public void setVisible(boolean visible) {
		mapLayer.setVisibility(visible);
	}
	
	
}
