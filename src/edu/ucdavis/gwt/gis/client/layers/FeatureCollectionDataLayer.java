package edu.ucdavis.gwt.gis.client.layers;

import java.util.Date;
import java.util.LinkedList;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;

import edu.ucdavis.cstars.client.Error;
import edu.ucdavis.cstars.client.Graphic;
import edu.ucdavis.cstars.client.InfoTemplate;
import edu.ucdavis.cstars.client.MapWidget;
import edu.ucdavis.cstars.client.event.ClickHandler;
import edu.ucdavis.cstars.client.event.ErrorHandler;
import edu.ucdavis.cstars.client.event.LayerLoadHandler;
import edu.ucdavis.cstars.client.layers.FeatureLayer;
import edu.ucdavis.cstars.client.layers.GraphicsLayer;
import edu.ucdavis.cstars.client.layers.Layer;
import edu.ucdavis.cstars.client.renderer.Renderer;
import edu.ucdavis.cstars.client.symbol.PictureMarkerSymbol;
import edu.ucdavis.cstars.client.symbol.SimpleFillSymbol;
import edu.ucdavis.cstars.client.symbol.SimpleLineSymbol;
import edu.ucdavis.cstars.client.symbol.SimpleMarkerSymbol;
import edu.ucdavis.cstars.client.symbol.Symbol;
import edu.ucdavis.gwt.gis.client.canvas.CanvasGeometry;
import edu.ucdavis.gwt.gis.client.canvas.CanvasMap;
import edu.ucdavis.gwt.gis.client.canvas.CanvasPoint;
import edu.ucdavis.gwt.gis.client.canvas.CanvasPolyline;
import edu.ucdavis.gwt.gis.client.canvas.markers.CanvasCircleMarker;
import edu.ucdavis.gwt.gis.client.canvas.markers.CanvasCrossMarker;
import edu.ucdavis.gwt.gis.client.canvas.markers.CanvasDiamondMarker;
import edu.ucdavis.gwt.gis.client.canvas.markers.CanvasSquareMarker;
import edu.ucdavis.gwt.gis.client.canvas.markers.CanvasXMarker;
import edu.ucdavis.gwt.gis.client.state.overlays.FeatureCollectionOverlay;
import edu.ucdavis.gwt.gis.client.state.overlays.GraphicsLayerOverlay;

public class FeatureCollectionDataLayer extends DataLayer {

	private String popupTitle = "name";
	private String popupContent = "description";
	private boolean showAllFields = false;
	
	private String id = "";
	private double opacity = 1;
	private boolean visible = true;
	
	private LinkedList<GraphicsLayer> mapLayers = new LinkedList<GraphicsLayer>();
	private GraphicsLayer gwtGisLayer = null;
	
	public FeatureCollectionDataLayer(String title) {
		super("", title, 1);
		id = title.replaceAll(" ", "_")+"_"+String.valueOf(new Date().getTime());
		setType(DataLayerType.FeatureCollection);
		createLayer();
	}
	
	public FeatureCollectionDataLayer(String title, FeatureLayer layer ) {
		super("", title, 1);
		setType(DataLayerType.FeatureCollection);
		
		addLayer(layer);
	}
	
	public FeatureCollectionDataLayer(FeatureCollectionOverlay data) {
		super("", data.getLabel(), 1);
		setType(DataLayerType.FeatureCollection);
		
		JsArray<GraphicsLayerOverlay> layers = data.getGraphicsLayers();
		for( int i = 0; i < layers.length(); i++ ) {
			GraphicsLayerOverlay layer = layers.get(i);
			GraphicsLayer glayer = GraphicsLayer.create();
			
			if( layer.hasRenderer() ) {
				glayer.setRenderer(Renderer.fromJson(layer.getRenderer()));
			}
			
			JsArray<JavaScriptObject> graphics = layer.getGraphics();
			for( int j = 0; j < graphics.length(); j++ ) {
				glayer.add(Graphic.create(graphics.get(j)));
			}
			addLayer(glayer);
			
			if( i == 0 ) gwtGisLayer = glayer;
		}
	}
	
	public void setId(String id) {
		this.id = id;
	}
	
	public void addLayer(GraphicsLayer layer) {
		mapLayers.add(layer);
	}
	
	public GraphicsLayer getMapLayer() {
		return gwtGisLayer;
	}
	
	public boolean hasLayer(GraphicsLayer layer) {
		for( GraphicsLayer l: mapLayers ) {
			if( l == layer ) return true;
		}
		return false;
	}

	@Override
	protected void createLayer() {
		GraphicsLayer.Options options = GraphicsLayer.Options.create();
		options.setVisible(visible);
		GraphicsLayer mapLayer = GraphicsLayer.create(options);

		mapLayer.addLoadHandler(new LayerLoadHandler(){
			@Override
			public void onLoad(Layer layer) {
				checkLoaded();
			}
		});
		mapLayer.addErrorHandler(new ErrorHandler(){
			@Override
			public void onError(Error error) {
				checkLoaded();
			}
		}); 
		
		mapLayers.add(mapLayer);
		gwtGisLayer = mapLayer;
	}
	
	protected InfoTemplate createInfoTemplate() {
		String content = "${"+popupContent+"}";
		return InfoTemplate.create("${"+popupTitle+"}", content);
	}
	
	public void updateInfoTemplate() {
		for( GraphicsLayer layer: mapLayers ) {
			layer.setInfoTemplate(createInfoTemplate());
		}
	}
	
	public void updateInfoTemplate(String titleField, String contentField, boolean showAll) {
		popupTitle = titleField;
		popupContent = contentField;
		showAllFields = showAll;
		updateInfoTemplate();
	}
	
	protected void checkLoaded() {
		isLoaded = true;
		for( DataLayerLoadHandler handler: handlers)  handler.onDataLoaded(this);
		handlers.clear();

		updateInfoTemplate();
	}
	
	// STATIC FUNCTIONS FOR RENDERING LEGENDS 
	public static CanvasMap renderLegendIcon(Symbol symbol) {
		CanvasMap canvasMap = new CanvasMap(24, 24);
		canvasMap.getCanvas().setStyleName("LegendIcon");
		return renderSymbolOnCanvas(symbol, canvasMap, 0, 0);
	}
	
	public static CanvasMap renderSymbolOnCanvas(Symbol symbol, CanvasMap canvasMap, double top, double left) {
		if( symbol.getType().contentEquals("picturemarkersymbol") ) return createPointSymbolOnCanvas((PictureMarkerSymbol) symbol, canvasMap, top, left);
		else if (symbol.getType().contentEquals("simplelinesymbol") ) return createPolylineSymbolOnCanvas((SimpleLineSymbol) symbol, canvasMap, top, left);
		else if (symbol.getType().contentEquals("simplefillsymbol") ) return createPolygonSymbolOnCanvas((SimpleFillSymbol) symbol, canvasMap, top, left);
		else if (symbol.getType().contentEquals("simplemarkersymbol") ) return createMarkerSymbolOnCanvas((SimpleMarkerSymbol) symbol, canvasMap, top, left);
		return canvasMap;
	}
	
	public static CanvasMap createPolygonSymbolOnCanvas(SimpleFillSymbol sfs, CanvasMap canvasMap, double top, double left) {
		String fill = sfs.getColor().toCss(true);
		String stroke = null;
		if( sfs.getOutline() != null ) stroke = sfs.getOutline().getColor().toCss(true);
		CanvasSquareMarker marker = new CanvasSquareMarker(left+4, top+4, 12, stroke, fill);
		if( sfs.getOutline() != null ) marker.setLineWidth(sfs.getOutline().getWidth());
		canvasMap.addGeometry(marker);	
		return canvasMap;
	}
	
	public static CanvasMap createPolylineSymbolOnCanvas(SimpleLineSymbol sls, CanvasMap canvasMap, double top, double left) {
		CanvasPolyline polyline = new CanvasPolyline(sls.getColor().toCss(true));
		polyline.setLineWidth(sls.getWidth());
		polyline.addPoint(left+4, top+4);
		polyline.addPoint(left+12, top+12);
		canvasMap.addGeometry(polyline);
		return canvasMap;
	}
	
	public static CanvasMap createPointSymbolOnCanvas(PictureMarkerSymbol pms, CanvasMap canvasMap, double top, double left) {
		CanvasPoint point = new CanvasPoint(left, top, 24, 24, pms.getUrl());
		canvasMap.addGeometry(point);
		return canvasMap;
	}
	
	public static CanvasMap createMarkerSymbolOnCanvas(SimpleMarkerSymbol sms, CanvasMap canvasMap, double top, double left) {
		String fill = null;
		String stroke = null;
		double lineWidth = 1;
		if( sms.getColor() != null ) fill = sms.getColor().toCss(true);
		if( sms.getOutline() != null ) {
			stroke = sms.getOutline().getColor().toCss(true);
			lineWidth = sms.getOutline().getWidth();
		}
		
		left += 4;
		top += 4;
		
		CanvasGeometry geo = null;
		if( sms.getStyle() == SimpleMarkerSymbol.StyleType.STYLE_DIAMOND ) {
			geo = new CanvasDiamondMarker(left, top, sms.getSize(), stroke, fill);
		} else if( sms.getStyle() == SimpleMarkerSymbol.StyleType.STYLE_CIRCLE ) {
			geo = new CanvasCircleMarker(left, top, sms.getSize(), stroke, fill);
		} else if( sms.getStyle() == SimpleMarkerSymbol.StyleType.STYLE_CROSS ) {
			geo = new CanvasCrossMarker(left, top, sms.getSize(), stroke);
		} else if( sms.getStyle() == SimpleMarkerSymbol.StyleType.STYLE_SQUARE ) {
			geo = new CanvasSquareMarker(left, top, sms.getSize(), stroke, fill);
		} else if( sms.getStyle() == SimpleMarkerSymbol.StyleType.STYLE_X ) {
			geo = new CanvasXMarker(left, top, sms.getSize(), stroke);	
		}
		
		if( geo != null ) {
			geo.setLineWidth(lineWidth);
			canvasMap.addGeometry(geo);
		}
		
		return canvasMap;
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public double getOpacity() {
		return opacity;
	}

	@Override
	public boolean isVisible() {
		return visible;
	}

	@Override
	public void removeFromMap(MapWidget map) {
		for( GraphicsLayer layer: mapLayers ) {
			map.removeLayer(layer);
		}
	}
	
	public void addToMap(MapWidget map) {
		for( GraphicsLayer layer: mapLayers ) {
			map.addLayer(layer);
		}
	}

	@Override
	public void setOpacity(double opacity) {
		this.opacity = opacity;
		for( GraphicsLayer layer: mapLayers ) {
			layer.setOpacity(opacity);
		}
	}

	@Override
	public void setVisible(boolean visible) {
		this.visible = visible;
		for( GraphicsLayer layer: mapLayers ) {
			layer.setVisibility(visible);
		}
	}

	public void remove(Graphic graphic) {
		for( GraphicsLayer layer: mapLayers ) {
			JsArray<Graphic> graphics = layer.getGraphics();
			for( int i = 0; i < graphics.length(); i++ ) {
				if( graphic == graphics.get(i) ) {
					layer.remove(graphic);
					return;
				}
			}
		}
	}
	
	public void add(Graphic graphic) {
		gwtGisLayer.add(graphic);
	}
	
	public void addClickHandler(ClickHandler handler) {
		for( GraphicsLayer layer: mapLayers ) {
			layer.addClickHandler(handler);
		}
	}
	
	public LinkedList<Graphic> getGraphics() {
		LinkedList<Graphic> graphics = new LinkedList<Graphic>();
		for( GraphicsLayer layer: mapLayers ) {
			JsArray<Graphic> lgraphics = layer.getGraphics();
			for( int i = 0; i < lgraphics.length(); i++ ) {
				graphics.add(lgraphics.get(i));
			}
		}
		return graphics;
	}
	
	public String toJson() {
		String json = "{"+super.toJson()+",";
		json += getGraphicsLayersJson();
		json += "}";
		return json;
	}
	
	public String getGraphicsLayersJson() {
		String json = "\"graphicsLayers\":[";
		for( GraphicsLayer layer: mapLayers ) {
			json += "{";
			
			
			
			if( layer.getRenderer() != null ) {
				json += getJsonKeyObject("renderer", layer.getRenderer().toJson())+",";
			}
			
			JsArray<Graphic> graphics = layer.getGraphics();
			json += "\"graphics\":[";
			for( int i = 0; i < graphics.length(); i++ ){
				json += stringify(graphics.get(i).toJson());
				if( i < graphics.length() - 1 ) json += ",";
			}
			json += "]},";
		}
		return json.replaceAll(",$", "")+"]";
	}
	
	private native String stringify(JavaScriptObject jso) /*-{
		return JSON.stringify(jso);
	}-*/;
	
	public void clear() {
		for( GraphicsLayer layer: mapLayers ) {
			layer.clear();
		}
	}
	
	
}
