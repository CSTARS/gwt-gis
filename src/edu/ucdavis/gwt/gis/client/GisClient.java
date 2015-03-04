package edu.ucdavis.gwt.gis.client;


import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style.Overflow;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

import edu.ucdavis.cstars.client.ESRI;
import edu.ucdavis.cstars.client.InfoWindowBase;
import edu.ucdavis.cstars.client.MapWidget;
import edu.ucdavis.cstars.client.SpatialReference;
import edu.ucdavis.cstars.client.Util;
import edu.ucdavis.cstars.client.control.Position;
import edu.ucdavis.cstars.client.dijits.Scalebar;
import edu.ucdavis.cstars.client.dijits.Scalebar.AttachTo;
import edu.ucdavis.cstars.client.dijits.Scalebar.ScalebarUnit;
import edu.ucdavis.cstars.client.event.MapLoadHandler;
import edu.ucdavis.cstars.client.geometry.Geometry;
import edu.ucdavis.cstars.client.geometry.Point;
import edu.ucdavis.cstars.client.layers.LOD;
import edu.ucdavis.gwt.gis.client.arcgiscom.ArcGisImportPanel;
import edu.ucdavis.gwt.gis.client.config.BasemapConfig;
import edu.ucdavis.gwt.gis.client.config.GadgetConfig;
import edu.ucdavis.gwt.gis.client.config.LayerConfig;
import edu.ucdavis.gwt.gis.client.draw.DrawControl;
import edu.ucdavis.gwt.gis.client.drive.GoogleDrive;
import edu.ucdavis.gwt.gis.client.layers.DataLayer;
import edu.ucdavis.gwt.gis.client.layers.FeatureCollectionDataLayer;
import edu.ucdavis.gwt.gis.client.layers.ImageServerDataLayer;
import edu.ucdavis.gwt.gis.client.layers.KmlDataLayer;
import edu.ucdavis.gwt.gis.client.layers.MapServerDataLayer;
import edu.ucdavis.gwt.gis.client.layers.DataLayer.DataLayerLoadHandler;
import edu.ucdavis.gwt.gis.client.layers.DataLayer.DataLayerType;
import edu.ucdavis.gwt.gis.client.layout.LayerMenuCreateHandler;
import edu.ucdavis.gwt.gis.client.layout.LayerMenuItem;
import edu.ucdavis.gwt.gis.client.layout.LayersPanel;
import edu.ucdavis.gwt.gis.client.layout.RightShadow;
import edu.ucdavis.gwt.gis.client.layout.RootLayout;
import edu.ucdavis.gwt.gis.client.state.ClientStateManager;
import edu.ucdavis.gwt.gis.client.toolbar.BasemapGallery;
import edu.ucdavis.gwt.gis.client.toolbar.Toolbar;
import edu.ucdavis.gwt.gis.client.toolbar.button.BaseMapButton;
import edu.ucdavis.gwt.gis.client.toolbar.menu.AddLayerMenu;
import edu.ucdavis.gwt.gis.client.resources.GadgetResources;


/**
 * Main class.  Create this, then add a loadhandler and customize as you wish (see examples in wiki).
 * 
 * @author Justin Merz
 */
public class GisClient {
	
	/**
	 * Load handler for when all standard functionality is loaded 
	 */
	public interface GisClientLoadHandler {
		public void onLoad();
	}
	private GisClientLoadHandler loadHandler = null;

	// old fashion are we using IE 7/8 flag
	private static boolean isIE7 = false;
	private static boolean isIE8 = false;
	private static boolean isIE9 = false;
	static {
		if( Window.Navigator.getUserAgent().matches(".*MSIE 7.*") ) isIE7 = true;
		if( Window.Navigator.getUserAgent().matches(".*MSIE 8.*") ) isIE8 = true;
		if( Window.Navigator.getUserAgent().matches(".*MSIE 9.*") ) isIE9 = true;
	}

	// website config file
	private GadgetConfig config = null;
	// main table map sits 
	private static RootLayout layout = new RootLayout();
	// main panel the application is attached to
	private SimplePanel rootPanel = null;
	// gwt-esri map object
	private MapWidget map = null;
	// top toolbar
	private Toolbar toolbar = null;
	// right hand layers (contains all the legends) panel
	protected LayersPanel layersPanel = new LayersPanel();
	
	
	// last known client width
	//private static  int lastWidth = 0;
	// last known client height
	//private static int lastHeight = 0;
	
	// default basemap for application
	public static String DEFAULT_BASEMAP = "http://services.arcgisonline.com/ArcGIS/rest/services/Canvas/World_Light_Gray_Base/MapServer?app=gwtgis";
	// token to be added to all basemap layer ids'
	public static String BASEMAP_TOKEN = "__base_";
	// token give to by esri
	public static String DEFAULT_BASEMAP_TOKEN = "layer";
	
	// handler for adding application specific menu's to each layer menu (on right hand side)
	public static LayerMenuCreateHandler layerMenuCreateHandler = null;
	
	// curstom info window
	private static InfoWindowBase customInfoWindow = null;
	
	// url config variables
	private DataLayer zoomToLayer = null;
	private int urlZoomLevel = -1;
	private float urlCenterX = 0;
	private float urlCenterY = 0;
	
	private static boolean fullScreen = true;
	private RootPanel gwtAnchor = null;
	
	private AddLayerMenu addLayerMenu = null;
	
	public static BaseMapButton basemapSelector = new BaseMapButton(); 
	
	private boolean debugging = false;
	
	public GisClient() {
	    checkDebug();
	    Debugger.INSTANCE.log("Creating GisClient");
	    
	    config = getConfig();
	    AppManager.INSTANCE.setConfig(config);
	    Debugger.INSTANCE.log("Config file set");
	}
	
	public GisClient(SimplePanel rootPanel) {
	    checkDebug();
	    
	    Debugger.INSTANCE.log("Creating GisClient with RootPanel");
	    
		this.rootPanel = rootPanel;
		fullScreen = false;
		
	    config = getConfig();
	    AppManager.INSTANCE.setConfig(config);
	    Debugger.INSTANCE.log("Config file set");
	}

	public void checkDebug() {
	    String debug = Window.Location.getParameter("debug");
	    if( debug != null  && debug.contentEquals("true") ) {
	        debugging = true;
	        Debugger.INSTANCE.setLogging(debugging);
	    }
	}
	
	
	/**
	 * Set load handler for client.  This fires when the client is ready for developer to customize.
	 * Customizations included adding items to layer menu as well as adding functionality to the
	 * map itself.
	 * 
	 * @param loadHandler - handler to fire when client is ready
	 */
	public void load(final GisClientLoadHandler loadHandler) {
		ResourceLoader.injectRootScripts(new Runnable(){
			@Override
			public void run() {
				// add 3rd party js and css
				ResourceLoader.inject();
				
				// IE sucks so much, delay to load, make sure the page is ready
			    if( isIE7 || isIE8 ) {
			        new Timer() {
		                @Override
		                public void run() {
		                    _load(loadHandler);
		                }
			        }.schedule(2000);
			    } else {
			        _load(loadHandler);
			    }
			}
		});
	}
	
	private void _load(GisClientLoadHandler loadHandler) {
		AppManager.INSTANCE.setClient(GisClient.this);
		
		Debugger.INSTANCE.log("Checking for IE");

		if( isIE7 || isIE8 ) { // inject IE 7/8 css
			addIECss();
		}
		
		// are we letting the user browse the arcgis server for a layer
		if( config.enableBrowseServer() ) {
		    Debugger.INSTANCE.log("Browser server in layer menu enabled");
		    
            setLayerMenuCreateHandler(new LayerMenuCreateHandler(){
                @Override
                public LayerMenuItem onCreate(VerticalPanel menu, DataLayer dl) {
                        BrowseMenuItem item = new BrowseMenuItem();
                        item.init(dl);
                        menu.add(item);
                        return item;
                }
            });
		}
		
		Debugger.INSTANCE.log("Config file set");
		
		// are we using cors?
		if( config.getProxy().length() > 0 ) {
		    Debugger.INSTANCE.log("Setting esri proxy: "+config.getProxy());
		    ESRI.setProxyUrl(config.getProxy());
		}
		
		// did config supply a parent panel?
		if( rootPanel == null ) {
		    Debugger.INSTANCE.log("No rootPanel supplied");
		    
			if( config.getAnchorDiv().length() > 0 ) {
			    Debugger.INSTANCE.log("Using anchor id: "+config.getAnchorDiv());
			    
				rootPanel = new SimplePanel();
				gwtAnchor = RootPanel.get(config.getAnchorDiv());
				rootPanel.setSize(gwtAnchor.getOffsetWidth()+"px", gwtAnchor.getOffsetHeight()+"px");
				gwtAnchor.add(rootPanel);
				fullScreen = false;
			} else {
	             Debugger.INSTANCE.log("Setting fullscreen mode with rootPanel id: gwt-gis-root");
			    
				rootPanel = new SimplePanel();
				rootPanel.getElement().setId("gwt-gis-root");
				RootPanel.get().add(rootPanel);
				fullScreen = true;
				if( isIE7 ) rootPanel.getElement().setAttribute("scroll", "no");
				RootPanel.get().getElement().getStyle().setMargin(0, Unit.PX);
				RootPanel.get().getElement().getStyle().setPadding(0, Unit.PX);
			}
		}
		rootPanel.getElement().getStyle().setOverflow(Overflow.HIDDEN);
		
		// add the layout to the root panel (whatever that might be)
		rootPanel.add(layout);
		
		
		if( debugging ) {
			Debugger.INSTANCE.show();
		}
		
		this.loadHandler = loadHandler;
		
		Debugger.INSTANCE.log("Injecting css resources");
		
		// load gadget css
		GadgetResources.INSTANCE.css().ensureInjected();
		
		Debugger.INSTANCE.log("Initializing map status handler");
		
		// let the map event handler setup it's loading screen
		MapEventStatusHandler.INSTANCE.init(this);
		
		Debugger.INSTANCE.log("Loading ESRI libraries");
		
		// request esri packages and wait for them to load
		Util.addRequiredPackage(Util.Package.ESRI_LAYERS_OSM);
		Util.addRequiredPackage(Util.Package.ESRI_DIJIT_BASEMAPGALLERY);
		Util.addRequiredPackage(Util.Package.ESRI_INFOWINDOWBASE);
		Util.addRequiredPackage(Util.Package.ESRI_DIJIT_SCALEBAR);
		Util.addRequiredPackage(Util.Package.ESRI_LAYERS_KMLLAYER);
		Util.addRequiredPackage(Util.Package.ESRI_TOOLBARS_DRAW);
		Util.addRequiredPackage(Util.Package.ESRI_TOOLBARS_EDIT);
		Util.addRequiredPackage(Util.Package.ESRI_TASKS_PRINTTASK);
		Util.addEsriLoadHandler(onEsriLoad);
	}
	
	// Runs after the esri libraries are loaded
	private Runnable onEsriLoad = new Runnable() {
		@Override
		public void run() {
		    Debugger.INSTANCE.log("ESRI Libraries loaded");
		    
			// first pass at getting size correct
			updateAppSize(true);
			
			Debugger.INSTANCE.log("Setting ajax timeouts to: "+20+"s");
			
			// set the default time in which esri timesout
			if( config.hasXhrTimeout() ) ESRI.setTimeout(config.getXhrTimeout());
			else ESRI.setTimeout(30000);
			
			Debugger.INSTANCE.log("Creating map");
			
			// create the map
			map = createMap(onMapLoad);
			
			Debugger.INSTANCE.log("Assigning map to AppManager & StatusHandler");
			
			// set the map for the map status handler
			MapEventStatusHandler.INSTANCE.setMap(map);
			AppManager.INSTANCE.setMap(map);
			
			Debugger.INSTANCE.log("Waiting for map to load...");
		}
	};
	
	// Runs after the map is loaded with a baselayer
	private MapLoadHandler onMapLoad = new MapLoadHandler(){
		@Override
		public void onLoad(MapWidget map) {
		    Debugger.INSTANCE.log("Map loaded");
		    
			// Update the status of the loading screen
			MapEventStatusHandler.INSTANCE.setLoadingLayers();
			
			Debugger.INSTANCE.log("Adding controls to map");
			
			// start adding controls to the map
			addControls();		
			
			Debugger.INSTANCE.log("Setting layers panel");
			
			layout.setLayersPanel(layersPanel);
			
			Debugger.INSTANCE.log("Attaching map to BasemapGallery");
			
			// make sure the basemap gallery is aware of the map
			BasemapGallery.INSTANCE.setMap(map);
			
			// make sure we are not loading from the 'cloud' 
			if( Window.Location.getParameter("state") == null &&
				Window.Location.getParameter("pubstate") == null ) {
			    
				// add layers to map 
				addLayers();
	
				// add additional basemaps
				addBasemaps();
			}
			
			Debugger.INSTANCE.log("Creating addLayerMenu");
			addLayerMenu = new AddLayerMenu();
			
            if( config.enableBrowseServer() ) {
                Debugger.INSTANCE.log("Adding AddLayerMenu to main menu");
                getToolbar().addToolbarMenu(addLayerMenu);
            }
			
            Debugger.INSTANCE.log("Calling onLayersLoaded");
			// case where we have no pre-loaded layers
			onLayersLoaded();
			
			Debugger.INSTANCE.log("Setting done status");
			MapEventStatusHandler.INSTANCE.setDone();
			
			// update the map size to fill the layout
			updateAppSize(true);
			
			// if we are in embed mode, we might need to poke this again
			// yes, this is a major hack
			if( gwtAnchor != null ) {
			    Debugger.INSTANCE.log("No gwtAnchor, resizing in 5s");
				new Timer() {
					@Override
					public void run() {
						updateAppSize(true);
					}
				}.schedule(5000);
			}
			
	        // TODO: move to config
	        //CompatibilityTester.showDetails(false);
	        //CompatibilityTester.test();
		}
	};
	
	public AddLayerMenu getAddLayerMenu() {
	    return addLayerMenu;
	}
	
	/**
	 * Expand all layer panels to show their legends 
	 **/
	public void expandLegends(boolean expand) {
		layersPanel.setExpanded(expand);
	}
	
	/**
	 * Set handler to fire each time a new layer menu is created.
	 * (optional)
	 * 
	 * @param handler
	 */
	public static void setLayerMenuCreateHandler(LayerMenuCreateHandler handler) {
		layerMenuCreateHandler = handler;
	}
	
	/**
	 * Get the handler that should be fired each time a menu is created.
	 * (optional)
	 * 
	 * @return LayerMenuCreateHandler
	 **/
	public static LayerMenuCreateHandler getLayerMenuCreateHandler() {
		return layerMenuCreateHandler;
	}
	
	/**
	 * Fires when the map is almost ready to go
	 **/
	private void onLayersLoaded() {
	    Debugger.INSTANCE.log("Firing onLayersLoaded");
		
		// set listeners for window is resized. we will need to resize the map.
		Window.addResizeHandler(new ResizeHandler(){
			@Override
			public void onResize(ResizeEvent event) {
				resizeTimer.cancel();
				resizeTimer.schedule(200);
			}
		});
	
		Debugger.INSTANCE.log("Calling GisClient onLoad handler");
		if( loadHandler != null ) loadHandler.onLoad();
		
		
		if( Window.Location.getParameter("map") != null ) {
		    Debugger.INSTANCE.log("Setting map from url parameter");
			
			centerMap();
			Timer t = new Timer() {
				public void run() {
					String map = Window.Location.getParameter("map");
					ClientStateManager.loadClientStateFromJson("", map);
				}
			};
			t.schedule(1000);
		
		} else if ( Window.Location.getParameter("arcmap") != null ) {
		    Debugger.INSTANCE.log("Setting map from arcmap");
			
			centerMap();
			Timer t = new Timer() {
				public void run() {
					String id = Window.Location.getParameter("arcmap");
					ArcGisImportPanel.loadArcGisMap(id);
				}
			};
			t.schedule(1000);
			
		// loading from google drive
		} else if ( Window.Location.getParameter("state") != null ) {
		    Debugger.INSTANCE.log("Loading data from google drive");
			
			Timer t = new Timer() {
				public void run() {
					ClientStateManager.clearClientState();
					GoogleDrive.INSTANCE.loadMapOnStart();
				}
			};
			t.schedule(1000);
		
		// load a public file from google drive
		} else if ( Window.Location.getParameter("pubstate") != null ) {
		    Debugger.INSTANCE.log("Loading public data from google drive");
			
			Timer t = new Timer() {
				public void run() {
					ClientStateManager.clearClientState();
					GoogleDrive.INSTANCE.loadPublicMapOnStart();
				}
			};
			t.schedule(1000);
			
			
		} else {
			
			centerMap();
			
			if( zoomToLayer != null ) {
			    Debugger.INSTANCE.log("ZoomToLayer found");
			    
				new Timer() {
					 public void run() {
						zoomToLayer.addLoadHandler(new DataLayerLoadHandler(){
							@Override
							public void onDataLoaded(final DataLayer dataLayer) {
								if( dataLayer.getType() == DataLayerType.MapServer ) {
									((MapServerDataLayer) dataLayer).zoomToLayerExtent();
								} else if( dataLayer.getType() == DataLayerType.KML ) {
									((KmlDataLayer) dataLayer).zoomToLayerExtent();
								}
							}
						});
						
					 }
				 }.schedule(500);
			} else {
				// double check
				new Timer() {
					 public void run() {
						 centerMap();
					 }
				 }.schedule(500);
			}
		}
	}
	
	/**
	 * Center the map for the first time
	 **/
	private void centerMap() {
	    Debugger.INSTANCE.log("Setting map center");
	    
		float x = 0;
		float y = 0;
		int zoom = 0;
		
		// check config file
		if( config.hasCenterPoint() ){
			x = (float) config.getCenterx();
			y = (float) config.getCentery();
			zoom = config.getZoomLevel();
		}
		
		// override any config file center w/ url center
		if( urlCenterX != 0 && urlCenterY != 0) {
			x = urlCenterX;
			y = urlCenterY;
		}
		
		if( urlZoomLevel != -1 ) {
			zoom = urlZoomLevel;
		}
		
		if( x != 0 && y != 0 ) {
			Point centerPoint = Point.create(x, y, SpatialReference.create(4269));
			centerPoint = (Point) Geometry.geographicToWebMercator(centerPoint);
			try {
			    Debugger.INSTANCE.log(centerPoint);
				map.centerAndZoom(centerPoint, zoom);
			} catch (Exception e) {
				Debugger.INSTANCE.catchException(e, "GisClient", "centerMap()");
			}
		}
		
	}
	
	/**
	 * Add custom basemaps to client
	 **/
	private void addBasemaps() {
		JsArray<BasemapConfig> bmaps = config.getAdditionalBasemaps();
		basemapSelector.init(map);
		for( int i = 0; i < bmaps.length(); i++ ) {
			BasemapConfig bc = bmaps.get(i);
			basemapSelector.add(bc.getName(), bc.getUrl(), bc.getIconUrl());
		}
	}
	
	/**
	 * Add website config layers to the client.  This also adds any layers passed in the ?url=
	 * parameter
	 **/
	private int addLayers() {
	    Debugger.INSTANCE.log("Adding local (config) layers");
	    
		int hcount = 0;
		JsArray<LayerConfig> dls = config.getDataLayers();
		for( int i = dls.length()-1; i >=0; i-- ) {
			String url = dls.get(i).getUrl();
			DataLayer dl = null;
			
			if( url.endsWith("kml") || url.endsWith("kmz") ) {
			    Debugger.INSTANCE.log("Adding kml layer: "+dls.get(i).getLabel());
				dl = new KmlDataLayer(dls.get(i));
			} else if ( url.contains("MapServer") ) {
			    Debugger.INSTANCE.log("Adding MapServer layer: "+dls.get(i).getLabel());
				dl = new MapServerDataLayer(dls.get(i));
			}
			
			if( dl != null ) {
				AppManager.INSTANCE.addDataLayer(dl);
				//dl.addLoadHandler(layerLoadHandler);
				layersPanel.add(dl);
				hcount++;
			}
		}
		
		// check for url parameters
		try {
			
			DataLayer firstLayer = null;
			
			// add single url
			String url = Window.Location.getParameter("url");
			if( url != null ){
			    Debugger.INSTANCE.log("Adding layer from url: "+url);
				firstLayer = addUrlDatalayer(url);
			}
			
			// add multiple urls
			String urls = Window.Location.getParameter("urls");
			if( urls != null ){
				String[] parts = urls.split(";");
				for( int i = 0; i < parts.length; i++ ) {
					DataLayer dl = addUrlDatalayer(parts[i]);
					Debugger.INSTANCE.log("Adding layer from url: "+dl.getUrl());
					if( firstLayer == null ) firstLayer = dl;
				}
			};
			
			// do we want to zoom to a layer extent?
			String zoom = Window.Location.getParameter("zoom");
			if( firstLayer != null && zoom != null ) {
				if( zoom.equals("true") ) {
				    Debugger.INSTANCE.log("Zooming to first layer");
					zoomToLayer = firstLayer;
				}
			}
			
			// do we want to set a zoomlevel?
			// this will overwrite the 'zoom' parameter
			String center = Window.Location.getParameter("center");
			if( center != null ) {
				try {
					String[] parts = center.split(",");
					urlCenterY = Float.parseFloat(parts[0]);
					urlCenterX = Float.parseFloat(parts[1]);
					Debugger.INSTANCE.log("Setting center: "+urlCenterY+", "+urlCenterX);
					if( parts.length > 2 ) urlZoomLevel = Integer.parseInt(parts[2]);
				} catch (Exception e) {}
			}
			
			
		} catch (Exception e) {
			Debugger.INSTANCE.catchException(e, "GisClient", "addLayers()");
		}
		
		return hcount;
	}
	
	private DataLayer addUrlDatalayer(String url) {
		if( !url.matches("^(http|https|ftp)://.*") ) url  = "http://"+url;
		
		String title = url.replaceAll("/MapServer.*", "").replaceAll(".*/", "").replaceAll("\\.kml", "").replaceAll("\\.kmz", "");
		return addLayer(url, title, 80, false);
	}

	/**
	 * Add a datalayer to the client
	 * 
	 * @param dl - layer to add
	 * @return DataLayer
	 */
	public DataLayer addLayer(DataLayer dl) {	
		try {
			if( dl != null ) {
			    Debugger.INSTANCE.log("Adding layer: "+dl.getLabel());
				AppManager.INSTANCE.addDataLayer(dl);
				
				if( dl.getType() == DataLayerType.FeatureCollection ) {
					FeatureCollectionDataLayer fcl = (FeatureCollectionDataLayer) dl;
					DrawControl.INSTANCE.addLayer(fcl);
					fcl.addToMap(map);
				} else { 
					layersPanel.add(dl);
				}
			}
		} catch (Exception e) {
			Debugger.INSTANCE.catchException(e, "GisClient", "addLayer(DataLayer)");
		}
		return dl;
	}
	
	public DataLayer addLayer(String url, String title, int opacity) {
		return addLayer(url, title, opacity, true);
	}
	
	/**
	 * Add a new layer to the client
	 * 
	 * @param url - url of layer
	 * @param title - name of layer (for menu)
	 * @param opacity - default opacity
	 * @return DataLayer
	 */
	public DataLayer addLayer(String url, String title, int opacity, boolean showLegend) {
		DataLayer dl = null;
		Debugger.INSTANCE.log("Adding layer: "+title);
		
		try {
			if( url.endsWith("kml") || url.endsWith("kmz") ) {
				dl = new KmlDataLayer(url, title, opacity);
			} else if ( url.contains("MapServer") ) {
				dl = new MapServerDataLayer(url, title, opacity);
			} else if ( url.contains("ImageServer") ) {
				dl = new ImageServerDataLayer(url, title, opacity);
			}
			
			if( dl != null ) {
				dl.setShowLegendOnLoad(showLegend);
				AppManager.INSTANCE.addDataLayer(dl);
				layersPanel.add(dl);
			}
		} catch (Exception e) {
			Debugger.INSTANCE.catchException(e, "GisClient", "addLayer(url, title, opacity)");
		}
		
		return dl;
	}
	
	/**
	 * Remove a layer from the client
	 * 
	 * @param dl - layer to be removed
	 */
	public void removeLayer(DataLayer dl) {
		dl.removeFromMap(map);
		AppManager.INSTANCE.removeDataLayer(dl);
		MapEventStatusHandler.INSTANCE.removeLayer(dl.getId());
		layersPanel.remove(dl);
	}
	
	/**
	 * Add controls to the map
	 */
	private void addControls() {
		// Main toolbar
		toolbar = new Toolbar();
		toolbar.setPosition(0, 0, Position.TOP_LEFT);
		if( config.getTitle().length() > 0 ) toolbar.setTitle(config.getTitle());
		map.addControl(toolbar);
		
		// decor
		if( !GisClient.isIE7 ) {
			map.addControl(new RightShadow());
		}
		
		// scale bar
		// this is failing in IE 8
		try {
			Scalebar.Parameters params = Scalebar.Parameters.create();
			params.setAttachTo(AttachTo.BOTTOM_LEFT);
			params.setScalebarUnit(ScalebarUnit.ENGLISH);
			params.setMap(map);
			new Scalebar(params);
		} catch (Exception e){}	
		
		// add map event status control to the map
		MapEventStatusHandler.INSTANCE.addControl();
	}
	
	public int getWidth() {
	    // there are lots of issues w/ calculating size and which situation to use which
	    // panel, show all and say which we are using.
	    String msg = "getWidth(): window="+Window.getClientWidth()+", rootPanel="+rootPanel.getOffsetWidth()+
	                 ", layout="+layout.getOffsetWidth();
	    if( gwtAnchor != null ) msg += ", gwtAnchor="+gwtAnchor.getOffsetWidth();
	    
		if( fullScreen ) {
		    Debugger.INSTANCE.log(msg+" (using window)");
			return Window.getClientWidth();
		} else if ( gwtAnchor != null ) {
		    Debugger.INSTANCE.log(msg+" (using gwtAnchor: "+gwtAnchor.getElement().getId()+", "+gwtAnchor.getElement().getStyle().getWidth()+")");
			return gwtAnchor.getOffsetWidth();
		}
		Debugger.INSTANCE.log(msg+" (using rootPanel)");
		return rootPanel.getOffsetWidth();
	}
	
	public int getHeight() {
       String msg = "getHeight(): window="+Window.getClientHeight()+", rootPanel="+rootPanel.getOffsetHeight()+
               ", layout="+layout.getOffsetHeight();
      if( gwtAnchor != null ) msg += ", gwtAnchor="+gwtAnchor.getOffsetHeight();
	    
		if( fullScreen ) {
		    Debugger.INSTANCE.log(msg+" (using window)");
			return Window.getClientHeight();
		} else if ( gwtAnchor != null ) {
		    Debugger.INSTANCE.log(msg+" (using gwtAnchor "+gwtAnchor.getElement().getId()+")");
			return gwtAnchor.getOffsetHeight();
		}
		Debugger.INSTANCE.log(msg+" (using rootPanel)");
		return rootPanel.getOffsetHeight();
	}
	
	public int getLeft() {
		if( fullScreen ) return 0;
		return rootPanel.getAbsoluteLeft();
	}
	
	public int getTop() {
		if( fullScreen ) return 0;
		return rootPanel.getAbsoluteTop();
	}
	
	public SimplePanel getRootPanel() {
		return rootPanel;
	}
	
	public boolean isPhoneMode() {
		return layout.isPhoneMode();
	}
	
	/**
	 * Update the application size
	 * 
	 * @param force - by default if current width & height are equal to lastWidth
	 * and lastHeight nothing is done.  The force flag pushes on anyway.
	 */
	private int uasCount = 0;

	
	public void updateAppSize(boolean force) {
	    Debugger.INSTANCE.log("Updating app size, count="+uasCount);
	    
		uasCount++;
		int width = getWidth();
		int height = getHeight();
		
		if( GisClient.isIE7 || GisClient.isIE8 ) {
			width -= 2;
			height -= 2;
		}
		
		
		// resize panel as well
		if ( gwtAnchor != null ) {
			rootPanel.setSize(width+"px", height+"px");
			Debugger.INSTANCE.log("Setting rootPanel to: "+width+", "+height);
		} 
		
		if( map != null ) {
			if( isPhoneMode() ) {
			    Debugger.INSTANCE.log("Setting map to: "+width+", "+height);
			    map.setSize(width+"px", height+"px");
			} else {
			    Debugger.INSTANCE.log("Setting map to: "+(width-276)+", "+height);
			    map.setSize((width-276)+"px", height+"px");
			}
		}
		
		// always double check
		if( force ) {
		    Debugger.INSTANCE.log("Forcing resize in 500ms");
			new Timer() {
				@Override
				public void run() {
					updateAppSize(false);
				}
			}.schedule(500);
		}
	}
	
	public RootLayout getLayout() {
		return layout;
	}
	
	/**
	 * A timer for double checking the app is at the correct size.  This is important when scroll bars
	 * popup up and then disappear.  Scheduling a second resize event makes sure the application fills
	 * the entire screen at all times.
	 */
	private Timer resizeTimer = new Timer() {
		@Override
		public void run() {
			updateAppSize(false);
		}
	};

	/**
	 * Get the toolbar control.
	 * 
	 * @return Toolbare
	 */
	public Toolbar getToolbar() {
		return toolbar;
	}
	
	/**
	 * Get the gwt-esri map object.
	 * 
	 * @return MapWidget
	 */
	public MapWidget getMapWidget() {
		return map;
	}
	
	public LayersPanel getLayersPanel() {
		return layersPanel;
	}

	
	/**
	 * Check the website for a config variable
	 * 
	 * @return GadgetConfig
	 */
	private final native GadgetConfig getConfig() /*-{
		if( typeof mapConfig != 'undefined' ) {
			return mapConfig;
		} else if ( $wnd.mapConfig ) {
			return $wnd.mapConfig;
		}
		return null;
	}-*/;
	
	/**
	 * Static, crap, IE flag.  enough said.
	 * 
	 * @return
	 */
	public static boolean isIE7() {
		return isIE7;
	}
	
	public static boolean isIE8() {
		return isIE8;
	}
	
	public static boolean isIE9() {
		return isIE9;
	}
	
	/**
	 * Set a custom info window to be used.  See the ESRI docs for more information on custom info windows.
	 * 
	 * @param customWindow
	 */
	public static void setCustomInfoWindow(InfoWindowBase customWindow) {
		customInfoWindow = customWindow;
	}
	
	/**
	 * Create a new map.
	 * 
	 * @param initLayer - layer map is to initialize to
	 * @param handler - load handler
	 * @return MapWidget
	 */
	public static MapWidget createMap(MapLoadHandler handler) {
		return createMap(handler, null);
	}
	
	public static MapWidget createMap(MapLoadHandler handler, LOD[] lods) {
	    Debugger.INSTANCE.log("Creating gray basemap");
		MapWidget.Options options = MapWidget.Options.create(MapWidget.BaseMap.GRAY);
		if( lods != null ) {
		    Debugger.INSTANCE.log("Setting levels of detail");
		    options.setLods(lods);	
		}

		if( isIE7 || isIE8 ) {
		    Debugger.INSTANCE.log("Crappy IE version, disabling graphics on pan");
			options.setDisplayGraphicsOnPan(false);
		} else {
			options.setFadeOnZoom(true);
		}

		Debugger.INSTANCE.log("Creating map object");
		MapWidget map = new MapWidget(layout.getMapPanel(), handler, options);
		Debugger.INSTANCE.log("Map object created");
		
		return map; 
	}
	
	private void addIECss() {
		Element style = DOM.createElement("link");
		style.setAttribute("rel", "stylesheet");
		style.setAttribute("href", GWT.getModuleBaseURL()+"/IE.css");
		Document.get().getElementsByTagName("head").getItem(0).appendChild(style);
		if( isIE7 ) {
			style = DOM.createElement("link");
			style.setAttribute("rel", "stylesheet");
			style.setAttribute("href", GWT.getModuleBaseURL()+"/IE7.css");
			Document.get().getElementsByTagName("head").getItem(0).appendChild(style);
		}
	}
	
	private class BrowseMenuItem extends LayerMenuItem {
        
        @Override
        public String getIcon() {
            return "<i class='icon-cloud'></i>";
        }
        @Override
        public String getText() {
            return "&nbsp;Browse ArcGIS Server";
        }
        @Override
        public void onClick(DataLayer dataLayer) {
            addLayerMenu.getServiceAdder().browseServer(dataLayer.getUrl());
        }
    }
	
	
}
