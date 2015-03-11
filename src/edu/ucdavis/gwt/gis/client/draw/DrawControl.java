package edu.ucdavis.gwt.gis.client.draw;

import java.util.LinkedList;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

import edu.ucdavis.cstars.client.Graphic;
import edu.ucdavis.cstars.client.MapWidget;
import edu.ucdavis.cstars.client.control.Control;
import edu.ucdavis.cstars.client.control.Position;
import edu.ucdavis.cstars.client.dojo.Color;
import edu.ucdavis.cstars.client.event.DrawEndHandler;
import edu.ucdavis.cstars.client.event.MouseEvent;
import edu.ucdavis.cstars.client.geometry.Geometry;
import edu.ucdavis.cstars.client.layers.GraphicsLayer;
import edu.ucdavis.cstars.client.symbol.SimpleFillSymbol;
import edu.ucdavis.cstars.client.symbol.SimpleLineSymbol;
import edu.ucdavis.cstars.client.symbol.SimpleMarkerSymbol;
import edu.ucdavis.cstars.client.symbol.Symbol;
import edu.ucdavis.cstars.client.toolbars.Draw;
import edu.ucdavis.cstars.client.toolbars.Edit;
import edu.ucdavis.cstars.client.toolbars.Draw.GeometryType;
import edu.ucdavis.gwt.gis.client.AppManager;
import edu.ucdavis.gwt.gis.client.layers.DataLayer;
import edu.ucdavis.gwt.gis.client.layers.FeatureCollectionDataLayer;
import edu.ucdavis.gwt.gis.client.layers.DataLayer.DataLayerType;
import edu.ucdavis.gwt.gis.client.mapcontroller.MapController;

/**
 * Main control for editing and creating features
 * 
 * @author jrmerz
 */
public class DrawControl extends Control implements MapController {
	
	private static DrawControlUiBinder uiBinder = GWT.create(DrawControlUiBinder.class);
	interface DrawControlUiBinder extends UiBinder<Widget, DrawControl> {}
	
	private FeatureCollectionDataLayer currentLayer = null;
	private LinkedList<FeatureCollectionDataLayer> layers = new LinkedList<FeatureCollectionDataLayer>();
	
	public static final DrawControl INSTANCE = new DrawControl();

	private Draw draw = null;
	private Edit edit = null;
	private boolean isDrawing = false;
	private boolean enabled = false;
	
	private Graphic selectedGraphic = null;

	private EditFeaturePanel editFeaturePanel = null;
	private FeatureCollectionSelectPanel collectionSelectPanel = null;
	
	@UiField SimplePanel doneAnchor;
	
	@UiField SimplePanel drawControlsAnchor;
	@UiField SimplePanel actionsAnchor;
	
	DrawTypeControl drawTypeControl = new DrawTypeControl(this);
	ActionControl actionControl = new ActionControl(this);
	DoneControl doneControl = new DoneControl(this);
	
	/**
	 * Create the draw control
	 */
	protected DrawControl() {
		initWidget(uiBinder.createAndBindUi(this));
		editFeaturePanel = new EditFeaturePanel();
		collectionSelectPanel = new FeatureCollectionSelectPanel(this);
		
		
		actionsAnchor.setVisible(false);
		
		drawControlsAnchor.getElement().appendChild(drawTypeControl.getElement());
		actionsAnchor.getElement().appendChild(actionControl.getElement());
		doneAnchor.getElement().appendChild(doneControl.getElement());
	}
	
	/**
	 * Enable the control by showing it on the map and setting the tool to a ready state.
	 * 
	 * @param enable - show the control be enabled
	 */
	public void enable(boolean enable) {
		if( enable ) activate();
		else deactivate();
	}
	
	public void activate() {
		AppManager.INSTANCE.getMapControllerHost().setActive(this);
		
		enabled = true;
		
		drawControlsAnchor.setVisible(isEnabled());
		doneAnchor.setVisible(isEnabled());
		
		draw.deactivate();
		isDrawing = false;
		finishEdits();
		
		actionsAnchor.setVisible(false);
		
		collectionSelectPanel.update();
	}
	
	public void deactivate() {		
		this.enabled = false;
		
		drawControlsAnchor.setVisible(isEnabled());
		doneAnchor.setVisible(isEnabled());
		
		draw.deactivate();
		isDrawing = false;
		finishEdits();
		
		actionsAnchor.setVisible(false);
		
		collectionSelectPanel.update();
	}
	
	
	/**
	 * Add datalayer to the draw control
	 * 
	 * @param layer - layer to be added
	 */
	public void addLayer(FeatureCollectionDataLayer layer) {
		layers.add(layer);
		layer.addClickHandler(
				new edu.ucdavis.cstars.client.event.ClickHandler(){
					@Override
					public void onClick(MouseEvent event) {
					      edit(event.getGraphic());
					}
			});
	}
	
	public void edit(Graphic g) {
	    removePointOutline();

        if( !isDrawing && isEnabled() ) {
            
            selectedGraphic = g;
            
            actionsAnchor.setVisible(true);
            actionControl.showControls();
            
            Geometry.GeometryType type = selectedGraphic.getGeometry().getType();
            if( type == Geometry.GeometryType.POLYLINE || type == Geometry.GeometryType.POLYGON ) {
                edit.activate(Edit.ToolType.EDIT_VERTICES, selectedGraphic);
                editFeaturePanel.editGraphic(selectedGraphic);
            } else if( type == Geometry.GeometryType.POINT ){
                edit.deactivate();
                editFeaturePanel.editGraphic(selectedGraphic);
            }
        }
	}
	
	public void removePointOutline() {
		if( EditFeaturePanel.pointOutline == null ) return;
		AppManager.INSTANCE.getMap().getGraphics().remove(EditFeaturePanel.pointOutline);
		EditFeaturePanel.pointOutline = null;
	}
	
	public void setCurrentLayer(FeatureCollectionDataLayer layer) {
		currentLayer = layer;
		setCollectionText(layer.getLabel());
	}
	
	private void setCollectionText(String label) {
		actionControl.setAddingTo("Adding to Collection: <span style='color: #777777'>"+label+"</span>");
	}
	
	public FeatureCollectionDataLayer getCurrentLayer() {
		return currentLayer;
	}
	
	/**
	 * Clear the control removing all layers from the map.  Then create
	 * a new blank layer as a default drawing layer.
	 */
	public void clear() {
		for( FeatureCollectionDataLayer layer: layers) {
			layer.removeFromMap(AppManager.INSTANCE.getMap());
		}
		
		layers.clear();
		//currentLayer.clear();
		currentLayer = null;	
	}
	
	// check and see if there is a collection in manager, if so, set if.
	public void update() {
		LinkedList<DataLayer> layers = AppManager.INSTANCE.getDataLayers();
		for( DataLayer dl: layers) {
			if( dl.getType() == DataLayerType.FeatureCollection ) {
				currentLayer = (FeatureCollectionDataLayer) dl;
				break;
			}
		}
		if( currentLayer == null ) {
			currentLayer = new FeatureCollectionDataLayer("DrawLayer");
			AppManager.INSTANCE.getClient().addLayer(currentLayer);
		}
		setCollectionText(currentLayer.getLabel());
		
		collectionSelectPanel.update();
	}
	
	/**
	 * Add a graphic to the currently selected datalayer.
	 * 
	 * @param g - graphics to add.
	 */
	public void add(Graphic g) {
		currentLayer.add(g);
	}
	
	/**
	 * Remove a graphic from the currently selected datalayer.
	 * 
	 * @param g - graphic to remove.
	 */
	public void remove(Graphic g) {
		//currentLayer.remove(g);
		GraphicsLayer gl = g.getLayer();
		if( gl != null ) gl.remove(g);
	}
	
	/**
	 * is this control enabled.
	 * 
	 * @return boolean
	 */
	public boolean isEnabled() {
		return enabled;
	}
	
	@SuppressWarnings("unused")
	private void enableSelector() {
		draw.deactivate();
		isDrawing = false;
		
		actionsAnchor.setVisible(false);
	}
	
	@SuppressWarnings("unused")
	private void enableDrawPoint() {
		finishEdits();
		if( isEnabled() ) {
			draw.activate(GeometryType.POINT);
			isDrawing = true;
		}
		
		actionsAnchor.setVisible(true);
		actionControl.showDescription();
	}
	
	@SuppressWarnings("unused")
	private void enableDrawLine() {
		finishEdits();
		if( isEnabled() ) {
			draw.activate(GeometryType.POLYLINE);
			isDrawing = true;
		}
		
		actionsAnchor.setVisible(true);
		actionControl.showDescription();
	}
	
	@SuppressWarnings("unused")
	private void enableDrawPolygon() {
		finishEdits();
		if( isEnabled() ) {
			draw.activate(GeometryType.POLYGON);
			isDrawing = true;
		}
		
		actionsAnchor.setVisible(true);
		actionControl.showDescription();
	}
	
	@SuppressWarnings("unused")
	private void deleteGeometry() {
		remove(selectedGraphic);
		edit.deactivate();
		
		actionsAnchor.setVisible(false);
		
		removePointOutline();
		selectedGraphic = null;
		
	}
	
	/**
	 * Finish any edits currently being made to a graphic.
	 */
	private void finishEdits() {
		if( selectedGraphic != null ){
			//resetPoint();
			edit.deactivate();
			
			actionsAnchor.setVisible(false);
			
			editFeaturePanel.hide();
			selectedGraphic = null;
			
			removePointOutline();
		}
	}
	
	@SuppressWarnings("unused")
	private void showEditFeaturePanel() {
		editFeaturePanel.show();
	}
	
	private void done() {
		enable(false);
	}
	
	private void showCollections() {
		collectionSelectPanel.show();
	}
	
	/**
	 * Init all the click handlers
	 */
	private void initHandlers() {

		draw.addDrawEndHandler(new DrawEndHandler() {
			@Override
			public void onDrawEnd(Geometry geometry) {
				Symbol s = null;
				
				if( geometry.getType() == Geometry.GeometryType.POLYLINE ) {
					s = SimpleLineSymbol.create(
						SimpleLineSymbol.StyleType.STYLE_SOLID, 
						Color.create(34, 120, 218, 1), 
						2);
				} else if( geometry.getType() == Geometry.GeometryType.POINT ) {
					s = createStandardMarker();
				} else if ( geometry.getType() == Geometry.GeometryType.POLYGON ) {
					s = SimpleFillSymbol.create(
							SimpleFillSymbol.StyleType.STYLE_SOLID, 
							SimpleLineSymbol.create(
									SimpleLineSymbol.StyleType.STYLE_SOLID,
									Color.create(34, 120, 218, 1),
									1), 
							Color.create(34, 120, 218, .6));
				}
				
				if( s != null ) {
					currentLayer.add(Graphic.create(geometry, s));
				}
			}
		});

	}
	
	/**
	 * Create the standard marker.
	 * 
	 * @return SimpleMarkerSymbol
	 */
	private SimpleMarkerSymbol createStandardMarker() {
		return SimpleMarkerSymbol.create(
				SimpleMarkerSymbol.StyleType.STYLE_CIRCLE, 
				14, 
				SimpleLineSymbol.create(
						SimpleLineSymbol.StyleType.STYLE_SOLID,
						Color.create(60, 60, 60, 1),
						1),
				Color.create(34, 120, 218, 1)
		);
	}
	

	/**
	 * Init the control when it is added to the map.
	 */
	@Override
	public void init(MapWidget map) {
		drawControlsAnchor.setVisible(false);
		doneAnchor.setVisible(false);
		actionsAnchor.setVisible(false);
		
		setPosition(50, 20, Position.TOP_LEFT);
		//outerEditFeaturePanel.add(editFeaturePanel);
		
		Draw.Options options = Draw.Options.create();
		options.showTooltips(true);
		draw = Draw.create(map, options);
		
		Edit.Options eOptions = Edit.Options.create();
		eOptions.allowDeleteVertices(true);
		edit = Edit.create(map);
		
		initHandlers();
		
		update();
	}
	
	
	private static class DoneControl {
		
		private Element root;
		
		public DoneControl(DrawControl control) {
			root = createButtons(control);
		}
		
		public Element getElement() {
			return root;
		}
		
		private native Element createButtons(DrawControl control) /*-{
			var btns = $wnd.$(
				"<div>" +
					"<div class='btn-group'>" +
						"<button type='button' style='width:40px;text-align:center' class='btn collections'><i class='fa fa-th'></i></button>"+
						"<button type='button' style='text-align:center' class='btn done'>Done</button>"+
					"</div>" +
				"</div>"
			);
			
			btns.find(".btn.collections").on('click', function(){
				control.@edu.ucdavis.gwt.gis.client.draw.DrawControl::showCollections()();
			});
			
			btns.find(".btn.done").on('click', function(){
				control.@edu.ucdavis.gwt.gis.client.draw.DrawControl::done()();
			});
			
			return btns[0];
		}-*/;
		
	}
	
	private static class DrawTypeControl {
		
		private Element root;
		
		public DrawTypeControl(DrawControl control) {
			root = createButtons(control);
		}
		
		public Element getElement() {
			return root;
		}
		
		private native Element createButtons(DrawControl control) /*-{
			var btns = $wnd.$(
				"<div>" +
					"<div class='btn-group' data-toggle='buttons-radio'>" +
						"<button type='button' style='width:40px;text-align:center' class='btn select active'><i class='fa fa-hand-o-up'></i></button>"+
						"<button type='button' style='width:40px;text-align:center' class='btn point'><i class='fa fa-map-marker'></i></button>"+
						"<button type='button' style='width:40px;text-align:center' class='btn line'><i class='fa fa-angle-down' style='margin-top:1px'></i><i class='fa fa-angle-up' style='margin-left:-6px'></i></button>"+
						"<button type='button' style='width:40px;text-align:center' class='btn polygon'><i class='fa fa-square-o'></i></button>"+
					"</div>" +
				"</div>"
			);
			
			btns.find(".btn.select").on('click', function(){
				control.@edu.ucdavis.gwt.gis.client.draw.DrawControl::enableSelector()();
			});
			
			btns.find(".btn.point").on('click', function(){
				control.@edu.ucdavis.gwt.gis.client.draw.DrawControl::enableDrawPoint()();
			});
			
			btns.find(".btn.line").on('click', function(){
				control.@edu.ucdavis.gwt.gis.client.draw.DrawControl::enableDrawLine()();
			});
			
			btns.find(".btn.polygon").on('click', function(){
				control.@edu.ucdavis.gwt.gis.client.draw.DrawControl::enableDrawPolygon()();
			});
			
			return btns[0];
		}-*/;
		
	}
	
	private static class ActionControl {
		
		private Element root;
		
		public ActionControl(DrawControl control) {
			root = createButtons(control);
		}
		
		public Element getElement() {
			return root;
		}

		public native void setAddingTo(String text) /*-{
			var ele = this.@edu.ucdavis.gwt.gis.client.draw.DrawControl.ActionControl::root;
			$wnd.$(ele).find(".description").html(text);
		}-*/;
		
		public native void showDescription() /*-{
			var ele = $wnd.$(this.@edu.ucdavis.gwt.gis.client.draw.DrawControl.ActionControl::root);
			ele.find(".description").show();
			ele.find(".btn-group").hide();
		}-*/;
		
		public native void showControls() /*-{
			var ele = $wnd.$(this.@edu.ucdavis.gwt.gis.client.draw.DrawControl.ActionControl::root);
			ele.find(".description").hide();
			ele.find(".btn-group").show();
		}-*/;
		
		private native Element createButtons(DrawControl control) /*-{
			var btns = $wnd.$(
				"<div>" +
					"<div class='description' style='padding:5px;background-color:rgba(255,255,255,.7);white-space:nowrap;border-radius:4px'></div>"+
					"<div class='btn-group' style='display:none'>" +
						"<button type='button' style='width:40px;text-align:center' class='btn info'><i class='fa fa-info-circle'></i></button>"+
						"<button type='button' style='text-align:center' class='btn finished' value='point'>Finished</button>"+
						"<button type='button' style='width:40px;text-align:center' class='btn trash' value='point'><i class='fa fa-trash'></i></button>"+
					"</div>" +
				"</div>"
			);
			
			btns.find(".btn.info").on('click', function(){
				control.@edu.ucdavis.gwt.gis.client.draw.DrawControl::showEditFeaturePanel()();
			});
			
			btns.find(".btn.finished").on('click', function(){
				control.@edu.ucdavis.gwt.gis.client.draw.DrawControl::finishEdits()();
			});
			
			btns.find(".btn.trash").on('click', function(){
				control.@edu.ucdavis.gwt.gis.client.draw.DrawControl::deleteGeometry()();
			});
			
			return btns[0];
		}-*/;
		
	}

}
