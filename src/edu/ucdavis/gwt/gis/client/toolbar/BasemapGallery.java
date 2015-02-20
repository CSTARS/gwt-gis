package edu.ucdavis.gwt.gis.client.toolbar;

import java.util.LinkedList;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style.Overflow;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;

import edu.ucdavis.cstars.client.MapWidget;
import edu.ucdavis.cstars.client.layers.ArcGISTiledMapServiceLayer;
import edu.ucdavis.cstars.client.layers.Layer;
import edu.ucdavis.cstars.client.layers.OpenStreetMapLayer;
import edu.ucdavis.gwt.gis.client.AppManager;
import edu.ucdavis.gwt.gis.client.GisClient;
import edu.ucdavis.gwt.gis.client.ajax.RequestManager;
import edu.ucdavis.gwt.gis.client.ajax.RequestManager.Request;
import edu.ucdavis.gwt.gis.client.layout.modal.BootstrapModalLayout;

/**
 * Faked basemap gallery 
 * 
 * @author Justin Merz
 */
public class BasemapGallery extends BootstrapModalLayout {
	
	public static BasemapGallery INSTANCE = new BasemapGallery();
	
	private String baseRequest = "http://www.arcgis.com/sharing/community/groups?q=title%3A\"ArcGIS Online Basemaps\" AND owner%3Aesri&f=json"; // base request to start this process
	private String mapRequest = "http://www.arcgis.com/sharing/search?sortField=name&num=50&f=json&q=";  // query map layer overviews
	private String baseIconUrl = "http://www.arcgis.com/sharing/content/items/"; 	// baseIconUrl + result.id + "/info/" + result.thumbnail
    private String layerRequest = "http://www.arcgis.com/sharing/content/items/"; // result.id + "/data?f=json"
	
    private FlowPanel panel = new FlowPanel();
    private LinkedList<MapIconPanel> icons = new LinkedList<MapIconPanel>();
    
    private MapIconPanel currentLayer = null;
    
    private MapWidget map = null;
    
    private boolean isLoaded = false;
    // if the gallery hasn't loaded yet, set this variabel
    private String waitingSelectionId = "";
    
    private CheckBox disableCheckBox = new CheckBox();
    private FocusPanel disableEventPanel = new FocusPanel();
    
    private Anchor menuBtn = new Anchor("Menu");
    private Anchor closeBtn = new Anchor("Close");
    private FlowPanel footer = new FlowPanel();
    
    public BasemapGallery() {
    	
    	RequestManager.INSTANCE.makeRequest(new Request() {
			@Override
			public void onError() {
				panel.add(new HTML("Failed to load basemaps from ESRI"));
			}
			@Override
			public void onSuccess(JavaScriptObject json) {
				JsArray<ResultOverlay> results = ((RequestOverlay) json).getResults();
				if( results.length() < 1 ){
					panel.add(new HTML("Failed to load basemaps from ESRI"));
				} else {
					loadBasemapLayers(results.get(0).getId());
				}
			}
    	}, baseRequest);
    	
    	
    	disableCheckBox.setValue(true);
    	disableCheckBox.addClickHandler(new ClickHandler(){
			@Override
			public void onClick(ClickEvent event) {
				hideShowLayer(disableCheckBox.getValue());
			}
    	});
    	disableEventPanel.getElement().getStyle().setProperty("outline", "none");
    	disableEventPanel.add(disableCheckBox);
    	
    	panel.getElement().getStyle().setOverflow(Overflow.VISIBLE);
    	
    	
    	// init footer
    	menuBtn.addClickHandler(new ClickHandler(){
			@Override
			public void onClick(ClickEvent event) {
				AppManager.INSTANCE.getClient().getToolbar().showMenu();
			}
    	});
    	closeBtn.addClickHandler(new ClickHandler(){
			@Override
			public void onClick(ClickEvent event) {
				hide();
			}
    	});
    	menuBtn.addStyleName("btn");
    	menuBtn.addStyleName("btn-primary");
    	closeBtn.addStyleName("btn");
    	
    	footer.add(menuBtn);
    	footer.add(closeBtn);
    	
    }
    
    public Widget getDisableCheckbox() {
    	return disableEventPanel;
    }
    
    public boolean isBasemapShowing() {
    	return disableCheckBox.getValue();
    }
    
    public void setBasemapShowing(boolean showing) {
    	disableCheckBox.setValue(showing);
    	hideShowLayer(showing);
    }
    
    protected void hideShowLayer(boolean show) {
		if( currentLayer != null ) {
			currentLayer.getMainLayer().setVisibility(show);
			if( currentLayer.getTopLayer() != null ) {
				currentLayer.getTopLayer().setVisibility(show);
			}
		} else {
			JsArrayString ids = map.getLayerIds();
			for( int i = 0; i < ids.length(); i++ ) {
				if( ids.get(i).startsWith(GisClient.BASEMAP_TOKEN) ) {
					map.getLayer(ids.get(i)).setVisibility(show);
				}
			}
		}
    }
    
    public void setMap(MapWidget map) {
    	this.map = map;
    }
    
    private void loadBasemapLayers(String id) {
    	RequestManager.INSTANCE.makeRequest(new Request(){
			@Override
			public void onError() {
				panel.add(new HTML("Failed to load basemaps from ESRI"));
			}
			@Override
			public void onSuccess(JavaScriptObject json) {
				initBasemaps((RequestOverlay) json);
				isLoaded = true;
				if( waitingSelectionId.length() > 0 ) {
					selectBasemap(waitingSelectionId);
				}
			}
    	}, mapRequest+createMapRequestQuery(id) );
    	
    }
    
    private void initBasemaps(RequestOverlay request) {
    	JsArray<ResultOverlay> results = request.getResults();
    	for( int i = 0; i < results.length(); i++ ){
    		ResultOverlay result = results.get(i);
    		if( !result.getTitle().contains("Bing") ){
    			
    			MapIconPanel icon = new MapIconPanel(createIconUrl(result.getId(), result.getThumbnail()), result.getTitle(), result.getId());
    			icon.setArcId(result.getId());
    			icons.add(icon);
    			panel.add(icon);
    			
    		}
    	}
    	
    }
    
    public void addBasemap(String name, String url, String iconUrl) {
    	int id = icons.size()+1;
    	String sid = GisClient.BASEMAP_TOKEN+id+"-additional";
		
		
		ArcGISTiledMapServiceLayer.Options layerOptions = ArcGISTiledMapServiceLayer.Options.create();
		layerOptions.setId(sid);
		
    	MapIconPanel icon = new MapIconPanel(iconUrl, name, url);
    	icon.setMainLayer(ArcGISTiledMapServiceLayer.create(url, layerOptions));
		icons.add(icon);
		panel.add(icon);
		
		icon.select();
    }
    
    /**
     * String[] has order [name, id, iconUrl]
     * 
     * @return
     */
    public LinkedList<String[]> getAdditionalBasemaps() {
    	LinkedList<String[]> list = new LinkedList<String[]>();
    	for( MapIconPanel icon: icons) {
    		String lid = icon.getLayerId();
    		if( lid != null ) {
    			if( lid.matches(GisClient.BASEMAP_TOKEN+"\\d*-additional") ) {
    				list.add(new String[] {icon.getName(), icon.getUid(), icon.getIconUrl()});
    			}
    		}
    	}
    	return list;
    }
	
    public void clearAdditionalBasemaps() {
    	LinkedList<MapIconPanel> tmp = new LinkedList<MapIconPanel>();
    	for( MapIconPanel icon: icons) {
    		String lid = icon.getLayerId();
    		if( lid != null ) {
	    		if( lid.matches(GisClient.BASEMAP_TOKEN+"\\d*-additional") ) {
	    			tmp.add(icon);
	    		}
    		}
    	}
    	
    	for( MapIconPanel icon: tmp) {
    		icon.removeFromParent();
    		icons.remove(icon);
    	}
    }
    
    /**
     * Returns the id of the selected basemap
     * 
     * @return String
     */
    public String getSelectedBasemap() {
    	if( currentLayer != null ) {
    		return currentLayer.getUid();
    	}
    	return "";
    }
    
    public void selectBasemap(String id) {
    	if( !isLoaded ) {
    		waitingSelectionId = id;
    		return;
    	}
    	
    	for( MapIconPanel mip: icons ) {
    		if( mip.getUid().contentEquals(id) ) {
    			mip.select();
    			return;
    		}
    	}
    }
	
	private String createMapRequestQuery(String id){
		return "group%3A"+id+"%20AND%20type%3A%22web%20map%22";
	}
	
	private String createIconUrl(String id, String thumbnail){
		return baseIconUrl + id + "/info/" + thumbnail;
	}
	
	protected void setBasemap(String id) {
		for( MapIconPanel icon: icons ){
			if( icon.getUid().contentEquals(id) ){
				clearBasemap();
				if( icon.isLoaded() ){
					addBasemap(icon);
				} else {
					icon.loadBasemap();
				}
				return;
			}
		}
	}
	
	private void addBasemap(MapIconPanel icon){
		icon.getMainLayer().setVisibility(disableCheckBox.getValue());
		try {
			map.addLayer(icon.getMainLayer(), 0);
		} catch (Exception e) {
			Window.alert(e.getMessage());
		}
		if( icon.getTopLayer() != null ){
			icon.getTopLayer().setVisibility(disableCheckBox.getValue());
			map.addLayer(icon.getTopLayer());
		}
		currentLayer = icon;
	}
	
	public void clearBasemap() {
		if( currentLayer == null ) {
			JsArrayString ids = map.getLayerIds();
			for( int i = 0; i < ids.length(); i++ ){
				if( ids.get(i).startsWith(GisClient.BASEMAP_TOKEN) ){
					map.removeLayer(map.getLayer(ids.get(i)));
				} else if ( ids.get(i).startsWith(GisClient.DEFAULT_BASEMAP_TOKEN) ) {
					map.removeLayer(map.getLayer(ids.get(i)));
					// TODO: HACK!  this is removing the top base layer, some reason isn't showing up in
					// layer ids.  Proly cause esri is shit.
					Element ele = DOM.getElementById(map.getMapPanel().getElement().getId()+"_"+GisClient.DEFAULT_BASEMAP_TOKEN+"1");
					if( ele != null ) ele.removeFromParent();
				}
			}
		} else {
			map.removeLayer(currentLayer.getMainLayer());
			if( currentLayer.getTopLayer() != null ) map.removeLayer(currentLayer.getTopLayer());
		}
	}
	
	private void unselectLayers() {
		for( MapIconPanel icon: icons ){
			icon.setUnselected();
		}
	}
	
	public class MapIconPanel extends Composite {
		
		private FocusPanel eventPanel = new FocusPanel();
		private FlowPanel panel = new FlowPanel();
		private String uid = "";
		private Image icon = null;
		private String name = "";
		private String iconUrl = "";
		public String arcId = "";
		
		public Layer layer = null;
		public Layer topLayer = null;
		
		public MapIconPanel(String iconUrl, String title, String id) {
			eventPanel.setStyleName("BaseMapIconPanel");
			eventPanel.add(panel);
			
			name = title;
			this.iconUrl = iconUrl;
			uid = id;
			icon = new Image(iconUrl);
			
			eventPanel.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					select();
				}
			});
			panel.add(icon);
			
			HTML html = new HTML(title);
			html.setHeight("32px");
			html.getElement().getStyle().setFontSize(12, Unit.PX);
			html.getElement().getStyle().setLineHeight(16, Unit.PX);
			html.getElement().getStyle().setProperty("textAlign", "center");
			html.getElement().getStyle().setOverflow(Overflow.HIDDEN);
			panel.add(html);
			
			initWidget(eventPanel);
		}
		
		public void setMainLayer(Layer layer) {
			this.layer = layer;
		}
		
		public boolean isLoaded() {
			if( layer == null ) return false;
			return true;
		}
		
		public String getIconUrl() {
			return iconUrl;
		}
		
		public String getName() {
			return name;
		}
		
		public String getLayerId() {
			if( layer != null ) return layer.getId();
			return "";
		}
		
		public String getUid() {
			return uid;
		}
		
		public void setArcId(String arcId) {
			this.arcId = arcId;
		}
		
		public Layer getMainLayer() {
			return layer;
		}
		
		public Layer getTopLayer() {
			return topLayer;
		}
		
		public void setUnselected() {
			icon.getElement().getStyle().setProperty("margin", "0px");
			icon.getElement().getStyle().setProperty("border", "1px solid #cccccc");
		}
		
		public void setSelected() {
			icon.getElement().getStyle().setProperty("margin", "-1px");
			icon.getElement().getStyle().setProperty("border", "2px solid #FF9999");
		}
		
		public void select() {
			if( currentLayer == this ) return;
			
			unselectLayers();
			setSelected();
			setBasemap(uid);
			hide();
		}
		
		public void loadBasemap(){
			RequestManager.INSTANCE.makeRequest(new Request(){
				@Override
				public void onError() {}
				@Override
				public void onSuccess(JavaScriptObject json) {
					
					MapLayerRequestOverlay request = (MapLayerRequestOverlay) json;
					JsArray<BasemapLayersOverlay> layers = request.getBaseMap();
					for( int i = 0; i < layers.length(); i++ ){
						Layer l;
						
						if( layers.get(i).getType().contentEquals("OpenStreetMap") ){
							OpenStreetMapLayer.Options layerOptions = OpenStreetMapLayer.Options.create();
							layerOptions.setId(GisClient.BASEMAP_TOKEN+arcId);
							l = OpenStreetMapLayer.create(layerOptions);
						} else {
							ArcGISTiledMapServiceLayer.Options layerOptions = ArcGISTiledMapServiceLayer.Options.create();
							String sid = GisClient.BASEMAP_TOKEN+arcId;
							if( layers.get(i).isReference() ) sid += "-labels";
							layerOptions.setId(sid);
							l = ArcGISTiledMapServiceLayer.create(layers.get(i).getUrl(), layerOptions);
						}
						
						if( layers.get(i).isReference() ){
							topLayer = l;
						} else {
							layer = l;
						}
					}
					addBasemap(MapIconPanel.this);
					
				}
			},  layerRequest + arcId + "/data?f=json");
		}
		
	}
	

	public static class RequestOverlay extends JavaScriptObject {
		
		protected RequestOverlay() {}
		
		public final native int getStart() /*-{
			return this.start;
		}-*/;
		
		public final native int getTotal() /*-{
			return this.total;
		}-*/;
		
		public final native JsArray<ResultOverlay> getResults() /*-{
			return this.results;
		}-*/;
		
	}
	
	public static class MapLayerRequestOverlay extends JavaScriptObject {
		
		protected MapLayerRequestOverlay() {}
		
		public final native JsArray<BasemapLayersOverlay> getBaseMap() /*-{
			if( this.baseMap ){
				if( this.baseMap.baseMapLayers ) {
					return this.baseMap.baseMapLayers;
				} 
			}
			return [];
		}-*/;
		
	}
	
	public static class BasemapLayersOverlay extends JavaScriptObject {
	
		protected BasemapLayersOverlay() {}
		
		public final native String getType() /*-{
			if( this.type ) return this.type;
			return "";
		}-*/;
		
		public final native String getUrl() /*-{
			return this.url;
		}-*/;
		
		public final native boolean isReference() /*-{
			if( this.isReference ) return true;
			return false;
		}-*/;
		
	}
	
	public static class ResultOverlay extends JavaScriptObject {
		
		protected ResultOverlay() {}
		
		public final native String getId() /*-{
			return this.id;
		}-*/;
		
		public final native String getThumbnail() /*-{
			return this.thumbnail;
		}-*/;
		
		public final native String getTitle() /*-{
			return this.title;
		}-*/;

		
	}

	@Override
	public String getTitle() {
		return "Select Basemap";
	}

	@Override
	public Widget getBody() {
		return panel;
	}

	@Override
	public Widget getFooter() {
		return footer;
	}
	
}
