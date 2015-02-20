package edu.ucdavis.gwt.gis.client.state;

import java.util.Date;
import java.util.LinkedList;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsArrayNumber;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.storage.client.Storage;

import edu.ucdavis.cstars.client.MapWidget;
import edu.ucdavis.cstars.client.geometry.Extent;
import edu.ucdavis.cstars.client.geometry.Geometry;
import edu.ucdavis.cstars.client.geometry.Point;
import edu.ucdavis.cstars.client.layers.ArcGISTiledMapServiceLayer;
import edu.ucdavis.cstars.client.layers.FeatureLayer;
import edu.ucdavis.cstars.client.layers.FeatureLayer.FeatureCollectionObject;
import edu.ucdavis.gwt.gis.client.AppManager;
import edu.ucdavis.gwt.gis.client.Debugger;
import edu.ucdavis.gwt.gis.client.GisClient;
import edu.ucdavis.gwt.gis.client.arcgiscom.overlay.BaseMap;
import edu.ucdavis.gwt.gis.client.arcgiscom.overlay.BaseMapLayer;
import edu.ucdavis.gwt.gis.client.arcgiscom.overlay.MapDataResponse;
import edu.ucdavis.gwt.gis.client.arcgiscom.overlay.MapOverview;
import edu.ucdavis.gwt.gis.client.arcgiscom.overlay.OperationalLayer;
import edu.ucdavis.gwt.gis.client.arcgiscom.overlay.features.FeatureCollectionLayer;
import edu.ucdavis.gwt.gis.client.draw.DrawControl;
import edu.ucdavis.gwt.gis.client.layers.DataLayer;
import edu.ucdavis.gwt.gis.client.layers.FeatureCollectionDataLayer;
import edu.ucdavis.gwt.gis.client.layers.ImageServerDataLayer;
import edu.ucdavis.gwt.gis.client.layers.KmlDataLayer;
import edu.ucdavis.gwt.gis.client.layers.MapServerDataLayer;
import edu.ucdavis.gwt.gis.client.layers.DataLayer.DataLayerType;
import edu.ucdavis.gwt.gis.client.state.overlays.BasemapOverlay;
import edu.ucdavis.gwt.gis.client.state.overlays.FeatureCollectionOverlay;
import edu.ucdavis.gwt.gis.client.state.overlays.ImageServerOverlay;
import edu.ucdavis.gwt.gis.client.state.overlays.KmlOverlay;
import edu.ucdavis.gwt.gis.client.state.overlays.MapServerOverlay;
import edu.ucdavis.gwt.gis.client.state.overlays.MapStateOverlay;
import edu.ucdavis.gwt.gis.client.toolbar.BasemapGallery;

public class ClientStateManager {

	private static Storage storage = Storage.getLocalStorageIfSupported();
	private static final String NAME_TOKEN = "__name_";
	private static final String DATA_TOKEN = "__data_";
	private static final String DATE_TOKEN = "__date_";
	private static final String PRINT_TOKEN = "__print_";
	private static final String ARCGIS_TOKEN = "__arcgis_token";
	private static final String ARCGIS_USERNAME = "__arcgis_user";
	public static final String WARNING_MESSAGE = "Maps are saved in your browser.  Clearing your browsers cache will delete any saved maps.";
	
	private static String currentStateId = "";
	
	public static String getArcGisToken() {
		String token = storage.getItem(ARCGIS_TOKEN);
		if( token == null ) return "";
		return token;
	}
	
	public static String getArcGisUsername() {
		String token = storage.getItem(ARCGIS_USERNAME);
		if( token == null ) return "";
		return token;
	}
	
	public static void setArcGisToken(String token, String username) {
		storage.setItem(ARCGIS_TOKEN, token);
		storage.setItem(ARCGIS_USERNAME, username);
	}
	
	public static void removeArcGisToken() {
		storage.removeItem(ARCGIS_TOKEN);
		storage.removeItem(ARCGIS_USERNAME);
	}
	
	public static String saveClientState(String name) {
		String json = getMapAsJson(name);
		String id = md5(json);
		
		storage.setItem(NAME_TOKEN+id, name.replaceAll("___*", "_"));
		storage.setItem(DATA_TOKEN+id, json);
		storage.setItem(DATE_TOKEN+id, Long.toString(new Date().getTime()));
		
		currentStateId = id;
		return id;
	}
	
	/**
	 * Just like saveClientState, except is saves to a special token in localstore that is retrieved by
	 * popup window.
	 */
	public static void savePrintState() {
		storage.setItem(PRINT_TOKEN, getMapAsJson(""));
	}
	
	public static MapStateOverlay loadPrintState() {
		try {
			return load(storage.getItem(PRINT_TOKEN));
		} catch (Exception e) {
			Debugger.INSTANCE.catchException(e, "ClientStateManager", "loadPrintState()");
		}
		return null;
	}
	
	public static String getCurrentStateId() {
		return currentStateId;
	}
	
	public static String getMapName(String id) {
		return storage.getItem(NAME_TOKEN+id);
	}
	
	/**
	 * returns list in format [name]__[date]__[id]
	 * 
	 * @return
	 */
	public static LinkedList<String> getSavedMapNames() {
		LinkedList<String> list = new LinkedList<String>();
		
		for( int i = 0; i < storage.getLength(); i++ ) {
			String key = storage.key(i);
			if( key.startsWith(NAME_TOKEN) ) {
				String id = key.replaceFirst(NAME_TOKEN, "");
				list.add(storage.getItem(key)+"__"+storage.getItem(DATE_TOKEN+id)+"__"+id);
			}
		}
		
		return list;
	}
	
	public static void clearClientState() {
		MapWidget map = AppManager.INSTANCE.getMap();
		
		// remove all data layers
		LinkedList<DataLayer> tmp = AppManager.INSTANCE.getDataLayers();
		LinkedList<DataLayer> layers = new LinkedList<DataLayer>();
		for( DataLayer layer: tmp ) layers.add(layer);
		for( DataLayer layer: layers ) AppManager.INSTANCE.getClient().removeLayer(layer);
		
		// clear any other layers populating the map
		map.removeAllLayers();
		
		// remove all additional basemaps
		BasemapGallery.INSTANCE.clearAdditionalBasemaps();
		
		// remove all graphics
		if( map.getGraphics()  != null ) {
			map.getGraphics().clear();
		}
		
		// clear drawn graphics
		DrawControl.INSTANCE.clear();
	}
	
	public static void removeState(String id) {
		for( int i = 0; i < storage.getLength(); i++ ) {
			String key = storage.key(i);
			if( key.contains(id) ) {
				storage.removeItem(key);
			}
		}
	}
	
	public static void loadClientStateFromId(String id) {
		try {
			String json = storage.getItem(DATA_TOKEN+id);
			loadClientStateFromJson(id, json);
		} catch (Exception e) {
			Debugger.INSTANCE.catchException(e, "ClientStateManager", "loadClientStateFromId(id)");
		}	
	}
	
	
	public static void loadClientStateFromJson(String id, String json) {
		try {
			MapStateOverlay map = load(json);
			loadClientStateFromJson(id, map);
		} catch (Exception e) {
			Debugger.INSTANCE.catchException(e, "ClientStateManager", "loadClientStateFromJson(id, json)");
		}	
	}
	
	private static void loadClientStateFromJson(String id, MapStateOverlay mapState) {
		try {
			
			MapWidget map = AppManager.INSTANCE.getMap();
			GisClient client = AppManager.INSTANCE.getClient();
			
			// center and zoom
			Point p = Point.create(mapState.getCenterX(), mapState.getCenterY(), map.getSpatialReference());
			map.centerAndZoom(p, mapState.getLevel());
			
			// add datalayers
			for( int i = mapState.getDataLayers().length()-1; i >= 0; i-- ) {
				JavaScriptObject jso =  mapState.getDataLayers().get(i);
				if( isDatalayerKml(jso) ) {
					client.addLayer( new KmlDataLayer((KmlOverlay) jso) );
				} else if ( isDatalayerMapServer(jso) ) {
					client.addLayer( new MapServerDataLayer( (MapServerOverlay) jso) );
				} else if ( isDatalayerImageServer(jso) ) {
					client.addLayer( new ImageServerDataLayer( (ImageServerOverlay) jso) );
				} else if ( isDataLayerFeatureCollection(jso) ) {
					client.addLayer( new FeatureCollectionDataLayer( (FeatureCollectionOverlay) jso) );
				}
			}
			
			// add additional basemaps 
			for( int i = mapState.getBasemaps().length()-1; i >= 0 ; i-- ) {
				BasemapOverlay basemap = mapState.getBasemaps().get(i);
				BasemapGallery.INSTANCE.addBasemap(basemap.getLabel(), basemap.getId(), basemap.getIconUrl());
			}
			
			// select base map
			if( mapState.getSelectedBasemap().startsWith("http://") ) {
				ArcGISTiledMapServiceLayer.Options layerOptions = ArcGISTiledMapServiceLayer.Options.create();
				layerOptions.setId(GisClient.BASEMAP_TOKEN+"loaded");
				map.addLayer(ArcGISTiledMapServiceLayer.create(mapState.getSelectedBasemap(), layerOptions),0);
			} else {
				BasemapGallery.INSTANCE.selectBasemap(mapState.getSelectedBasemap());
			}
			
			// set basemap enabled?
			BasemapGallery.INSTANCE.setBasemapShowing(mapState.isBasemapVisible());
			
			// add graphics to graphics layer
			//JsArray<JavaScriptObject> graphics = mapState.getGraphics();
			//for( int i = 0; i < graphics.length(); i++ ) {				
			//	DrawControl.GRAPHICS_LAYER.add(Graphic.create(graphics.get(i)));
			//}
			
			DrawControl.INSTANCE.update();
			
			currentStateId = id;
		} catch (Exception e) {
			Debugger.INSTANCE.catchException(e, "ClientStateManager", "loadClientStateFromJson(id, mapState)");
		}	
	}
	
	public static void loadClientStateFromArcGis(String id, MapOverview mapOverview, MapDataResponse mapData) {
		try {
			
			MapWidget map = AppManager.INSTANCE.getMap();
			GisClient client = AppManager.INSTANCE.getClient();
			
			// set extent
			if( mapOverview != null ) {
				JsArrayNumber coords = mapOverview.getExtent();
				if( coords.length() >= 4 ) {		
					Extent ext = Extent.create(coords.get(0), coords.get(1), coords.get(2), coords.get(3), null);
					ext = (Extent) Geometry.geographicToWebMercator(ext);
					map.setExtent(ext, true);
				}
			}
	
			
			// add datalayers
			JsArray<OperationalLayer> layers = mapData.getOperationalLayers();
			for( int i = 0; i < layers.length(); i++ ) {
				OperationalLayer layer = layers.get(i);
				if( layer.hasFeatureCollection() ) {
					JsArray<FeatureCollectionLayer> fLayers = layer.getFeatureCollection().getLayers();
					FeatureCollectionDataLayer fcl = new FeatureCollectionDataLayer("arcgis_"+i);
					for( int j = 0; j < fLayers.length(); j++ ) {
						FeatureCollectionLayer fcLayer = fLayers.get(j);
						
						FeatureCollectionObject json = FeatureCollectionObject.create();
						json.setFeatureSet(fcLayer.getFeatureSet());
						json.setLayerDefinition(fcLayer.getLayerDefinition());
						fcl.addLayer(FeatureLayer.create(json));
						
					}
					client.addLayer(fcl);
				} else {
					client.addLayer(layer.getUrl(), layer.getTitle(), (int) Math.floor(layer.getOpacity()*100));
				}
			}
			
			// add additional basemaps 
			// TODO: is this even supported in arcgis.com?
			
			// select base map
			// TODO: overlay layers, bingmaps and openstreetmaps
			BaseMap basemap = mapData.getBaseMap();
			if( basemap != null ) {
				JsArray<BaseMapLayer> blayers = basemap.getBaseMapLayers();
				if( blayers.length() > 0 ) {
					BasemapGallery.INSTANCE.clearBasemap();
					ArcGISTiledMapServiceLayer.Options layerOptions = ArcGISTiledMapServiceLayer.Options.create();
					layerOptions.setId(GisClient.BASEMAP_TOKEN+"loaded");
					map.addLayer(ArcGISTiledMapServiceLayer.create(blayers.get(0).getUrl(), layerOptions),0);
				}
			}
			
			// set basemap enabled?
			BasemapGallery.INSTANCE.setBasemapShowing(true);
			
			currentStateId = id;
		} catch (Exception e) {
			Debugger.INSTANCE.catchException(e, "ClientStateManager", "loadClientStateFromArcGis(id, mapOverview, mapData)");
		}	
	}
	

	
	//private static native void setGraphicsLayer(Graphic g, GraphicsLayer gl) /*-{	
	//}-*/;
	
	// TODO: is this saving order?  Or are we losing it somewhere else?
	public static String getDataLayersAsJson() {
		String json = "[";
		
		// get layers in order
		JsArrayString layerIds = AppManager.INSTANCE.getMap().getLayerIds();
		for( int i = 0; i < layerIds.length(); i++ ){
			DataLayer dl = AppManager.INSTANCE.getLayerById(layerIds.get(i));
			if( dl != null ) {
				if( dl.getType() == DataLayerType.KML ) json += ((KmlDataLayer) dl).toJson();
				else if( dl.getType() == DataLayerType.MapServer ) json += ((MapServerDataLayer) dl).toJson();
				json += ",";
			}
		}
		
		// get feature layers
		LinkedList<DataLayer> layers = AppManager.INSTANCE.getDataLayers();
		for( DataLayer layer: layers ) {
			if( layer.getType() == DataLayerType.FeatureCollection ) {
				json += ((FeatureCollectionDataLayer) layer).toJson()+",";
			}
		}
		
		return json.replaceAll(",$", "")+"]";
	}
	
	public static String getMapAsJson(String name) {
		String json = "{";
		json += "\"name\":\""+name+"\",";

		Point p = AppManager.INSTANCE.getMap().getExtent().getCenter();
		json += "\"centerX\":"+p.getX()+",";
		json += "\"centerY\":"+p.getY()+",";
		json += "\"level\":"+AppManager.INSTANCE.getMap().getLevel()+",";
		
		// get selected basemap
		String basemap = BasemapGallery.INSTANCE.getSelectedBasemap();
		if( basemap.length() == 0 ) basemap = GisClient.DEFAULT_BASEMAP;
		json += "\"selectedBasemap\":\""+basemap+"\",";
		json += "\"basemapVisible\":"+Boolean.toString(BasemapGallery.INSTANCE.isBasemapShowing())+",";
		
		// get additional basemaps
		LinkedList<String[]> list = BasemapGallery.INSTANCE.getAdditionalBasemaps();
		json += "\"basemaps\":[";
		for( String[] arr: list ) {
			json += "{\"label\":\""+arr[0]+"\"," +
					"\"id\":\""+arr[1]+"\"," +
					"\"iconUrl\":\""+arr[2]+"\"},";
		}
		json = json.replaceAll(",$", "");
		json += "],";
		//json += "\"graphics\":"+getGraphicsAsJson()+",";
		json += "\"datalayers\":"+getDataLayersAsJson()+"}";

		return json;
	}
	
	
	public static native boolean isDatalayerKml(JavaScriptObject jso) /*-{
		if( jso ) {
			if( jso.type == "kml" ) return true;
		}
		return false;
	}-*/;
	
	public static native boolean isDatalayerMapServer(JavaScriptObject jso) /*-{
		if( jso ) {
			if( jso.type == "mapserver" ) return true;
		}
		return false;
	}-*/;
	
	public static native boolean isDatalayerImageServer(JavaScriptObject jso) /*-{
		if( jso ) {
			if( jso.type == "imageserver" ) return true;
		}
		return false;
	}-*/;
	
	public static native boolean isDataLayerFeatureCollection(JavaScriptObject jso) /*-{
		if( jso ) {
			if( jso.type == "featurecollection" ) return true;
		}
		return false;
	}-*/;
	
	private static native MapStateOverlay load(String json) /*-{
		return $wnd.eval('('+json+')');
	}-*/;
	
	
    public static native String md5(String string) /*-{ 
	    function RotateLeft(lValue, iShiftBits) { 
	    	return (lValue<<iShiftBits) | (lValue>>>(32-iShiftBits)); 
	    } 
	    function AddUnsigned(lX,lY) { 
	    var lX4,lY4,lX8,lY8,lResult; 
	    lX8 = (lX & 0x80000000); 
	    lY8 = (lY & 0x80000000); 
	    lX4 = (lX & 0x40000000); 
	    lY4 = (lY & 0x40000000); 
	    lResult = (lX & 0x3FFFFFFF)+(lY & 0x3FFFFFFF); 
	    if (lX4 & lY4) { 
	    return (lResult ^ 0x80000000 ^ lX8 ^ lY8); 
	    } 
	    if (lX4 | lY4) { 
	    if (lResult & 0x40000000) { 
	    return (lResult ^ 0xC0000000 ^ lX8 ^ lY8); 
	    } else { 
	    return (lResult ^ 0x40000000 ^ lX8 ^ lY8); 
	    } 
	    } else { 
	    return (lResult ^ lX8 ^ lY8); 
	    } 
	    } 
	    function F(x,y,z) { return (x & y) | ((~x) & z); } 
	    function G(x,y,z) { return (x & z) | (y & (~z)); } 
	    function H(x,y,z) { return (x ^ y ^ z); } 
	    function I(x,y,z) { return (y ^ (x | (~z))); } 
	    function FF(a,b,c,d,x,s,ac) { 
	    a = AddUnsigned(a, AddUnsigned(AddUnsigned(F(b, c, d), x), ac)); 
	    return AddUnsigned(RotateLeft(a, s), b); 
	    }; 
	    function GG(a,b,c,d,x,s,ac) { 
	    a = AddUnsigned(a, AddUnsigned(AddUnsigned(G(b, c, d), x), ac)); 
	    return AddUnsigned(RotateLeft(a, s), b); 
	    }; 
	    function HH(a,b,c,d,x,s,ac) { 
	    a = AddUnsigned(a, AddUnsigned(AddUnsigned(H(b, c, d), x), ac)); 
	    return AddUnsigned(RotateLeft(a, s), b); 
	    }; 
	    function II(a,b,c,d,x,s,ac) { 
	    a = AddUnsigned(a, AddUnsigned(AddUnsigned(I(b, c, d), x), ac)); 
	    return AddUnsigned(RotateLeft(a, s), b); 
	    }; 
	    function ConvertToWordArray(string) { 
	    var lWordCount; 
	    var lMessageLength = string.length; 
	    var lNumberOfWords_temp1=lMessageLength + 8; 
	    var lNumberOfWords_temp2=(lNumberOfWords_temp1-(lNumberOfWords_temp1 
	% 64))/64; 
	    var lNumberOfWords = (lNumberOfWords_temp2+1)*16; 
	    var lWordArray=Array(lNumberOfWords-1); 
	    var lBytePosition = 0; 
	    var lByteCount = 0; 
	    while ( lByteCount < lMessageLength ) { 
	    lWordCount = (lByteCount-(lByteCount % 4))/4; 
	    lBytePosition = (lByteCount % 4)*8; 
	    lWordArray[lWordCount] = (lWordArray[lWordCount] | 
	(string.charCodeAt(lByteCount)<<lBytePosition)); 
	    lByteCount++; 
	    } 
	    lWordCount = (lByteCount-(lByteCount % 4))/4; 
	    lBytePosition = (lByteCount % 4)*8; 
	    lWordArray[lWordCount] = lWordArray[lWordCount] | 
	(0x80<<lBytePosition); 
	    lWordArray[lNumberOfWords-2] = lMessageLength<<3; 
	    lWordArray[lNumberOfWords-1] = lMessageLength>>>29; 
	    return lWordArray; 
	    }; 
	    function WordToHex(lValue) { 
	    var WordToHexValue="",WordToHexValue_temp="",lByte,lCount; 
	    for (lCount = 0;lCount<=3;lCount++) { 
	    lByte = (lValue>>>(lCount*8)) & 255; 
	    WordToHexValue_temp = "0" + lByte.toString(16); 
	    WordToHexValue = WordToHexValue + 
	WordToHexValue_temp.substr(WordToHexValue_temp.length-2,2); 
	    } 
	    return WordToHexValue; 
	    }; 
	    function Utf8Encode(string) { 
	    string = string.replace(/\r\n/g,"\n"); 
	    var utftext = ""; 
	    for (var n = 0; n < string.length; n++) { 
	    var c = string.charCodeAt(n); 
	    if (c < 128) { 
	    utftext += String.fromCharCode(c); 
	    } 
	    else if((c > 127) && (c < 2048)) { 
	    utftext += String.fromCharCode((c >> 6) | 192); 
	    utftext += String.fromCharCode((c & 63) | 128); 
	    } 
	    else { 
	    utftext += String.fromCharCode((c >> 12) | 224); 
	    utftext += String.fromCharCode(((c >> 6) & 63) | 128); 
	    utftext += String.fromCharCode((c & 63) | 128); 
	    } 
	    } 
	    return utftext; 
	    }; 
	    var x=Array(); 
	    var k,AA,BB,CC,DD,a,b,c,d; 
	    var S11=7, S12=12, S13=17, S14=22; 
	    var S21=5, S22=9 , S23=14, S24=20; 
	    var S31=4, S32=11, S33=16, S34=23; 
	    var S41=6, S42=10, S43=15, S44=21; 
	    string = Utf8Encode(string); 
	    x = ConvertToWordArray(string); 
	    a = 0x67452301; b = 0xEFCDAB89; c = 0x98BADCFE; d = 0x10325476; 
	    for (k=0;k<x.length;k+=16) { 
	    AA=a; BB=b; CC=c; DD=d; 
	    a=FF(a,b,c,d,x[k+0], S11,0xD76AA478); 
	    d=FF(d,a,b,c,x[k+1], S12,0xE8C7B756); 
	    c=FF(c,d,a,b,x[k+2], S13,0x242070DB); 
	    b=FF(b,c,d,a,x[k+3], S14,0xC1BDCEEE); 
	    a=FF(a,b,c,d,x[k+4], S11,0xF57C0FAF); 
	    d=FF(d,a,b,c,x[k+5], S12,0x4787C62A); 
	    c=FF(c,d,a,b,x[k+6], S13,0xA8304613); 
	    b=FF(b,c,d,a,x[k+7], S14,0xFD469501); 
	    a=FF(a,b,c,d,x[k+8], S11,0x698098D8); 
	    d=FF(d,a,b,c,x[k+9], S12,0x8B44F7AF); 
	    c=FF(c,d,a,b,x[k+10],S13,0xFFFF5BB1); 
	    b=FF(b,c,d,a,x[k+11],S14,0x895CD7BE); 
	    a=FF(a,b,c,d,x[k+12],S11,0x6B901122); 
	    d=FF(d,a,b,c,x[k+13],S12,0xFD987193); 
	    c=FF(c,d,a,b,x[k+14],S13,0xA679438E); 
	    b=FF(b,c,d,a,x[k+15],S14,0x49B40821); 
	    a=GG(a,b,c,d,x[k+1], S21,0xF61E2562); 
	    d=GG(d,a,b,c,x[k+6], S22,0xC040B340); 
	    c=GG(c,d,a,b,x[k+11],S23,0x265E5A51); 
	    b=GG(b,c,d,a,x[k+0], S24,0xE9B6C7AA); 
	    a=GG(a,b,c,d,x[k+5], S21,0xD62F105D); 
	    d=GG(d,a,b,c,x[k+10],S22,0x2441453); 
	    c=GG(c,d,a,b,x[k+15],S23,0xD8A1E681); 
	    b=GG(b,c,d,a,x[k+4], S24,0xE7D3FBC8); 
	    a=GG(a,b,c,d,x[k+9], S21,0x21E1CDE6); 
	    d=GG(d,a,b,c,x[k+14],S22,0xC33707D6); 
	    c=GG(c,d,a,b,x[k+3], S23,0xF4D50D87); 
	    b=GG(b,c,d,a,x[k+8], S24,0x455A14ED); 
	    a=GG(a,b,c,d,x[k+13],S21,0xA9E3E905); 
	    d=GG(d,a,b,c,x[k+2], S22,0xFCEFA3F8); 
	    c=GG(c,d,a,b,x[k+7], S23,0x676F02D9); 
	    b=GG(b,c,d,a,x[k+12],S24,0x8D2A4C8A); 
	    a=HH(a,b,c,d,x[k+5], S31,0xFFFA3942); 
	    d=HH(d,a,b,c,x[k+8], S32,0x8771F681); 
	    c=HH(c,d,a,b,x[k+11],S33,0x6D9D6122); 
	    b=HH(b,c,d,a,x[k+14],S34,0xFDE5380C); 
	    a=HH(a,b,c,d,x[k+1], S31,0xA4BEEA44); 
	    d=HH(d,a,b,c,x[k+4], S32,0x4BDECFA9); 
	    c=HH(c,d,a,b,x[k+7], S33,0xF6BB4B60); 
	    b=HH(b,c,d,a,x[k+10],S34,0xBEBFBC70); 
	    a=HH(a,b,c,d,x[k+13],S31,0x289B7EC6); 
	    d=HH(d,a,b,c,x[k+0], S32,0xEAA127FA); 
	    c=HH(c,d,a,b,x[k+3], S33,0xD4EF3085); 
	    b=HH(b,c,d,a,x[k+6], S34,0x4881D05); 
	    a=HH(a,b,c,d,x[k+9], S31,0xD9D4D039); 
	    d=HH(d,a,b,c,x[k+12],S32,0xE6DB99E5); 
	    c=HH(c,d,a,b,x[k+15],S33,0x1FA27CF8); 
	    b=HH(b,c,d,a,x[k+2], S34,0xC4AC5665); 
	    a=II(a,b,c,d,x[k+0], S41,0xF4292244); 
	    d=II(d,a,b,c,x[k+7], S42,0x432AFF97); 
	    c=II(c,d,a,b,x[k+14],S43,0xAB9423A7); 
	    b=II(b,c,d,a,x[k+5], S44,0xFC93A039); 
	    a=II(a,b,c,d,x[k+12],S41,0x655B59C3); 
	    d=II(d,a,b,c,x[k+3], S42,0x8F0CCC92); 
	    c=II(c,d,a,b,x[k+10],S43,0xFFEFF47D); 
	    b=II(b,c,d,a,x[k+1], S44,0x85845DD1); 
	    a=II(a,b,c,d,x[k+8], S41,0x6FA87E4F); 
	    d=II(d,a,b,c,x[k+15],S42,0xFE2CE6E0); 
	    c=II(c,d,a,b,x[k+6], S43,0xA3014314); 
	    b=II(b,c,d,a,x[k+13],S44,0x4E0811A1); 
	    a=II(a,b,c,d,x[k+4], S41,0xF7537E82); 
	    d=II(d,a,b,c,x[k+11],S42,0xBD3AF235); 
	    c=II(c,d,a,b,x[k+2], S43,0x2AD7D2BB); 
	    b=II(b,c,d,a,x[k+9], S44,0xEB86D391); 
	    a=AddUnsigned(a,AA); 
	    b=AddUnsigned(b,BB); 
	    c=AddUnsigned(c,CC); 
	    d=AddUnsigned(d,DD); 
	    } 
	    var temp = WordToHex(a)+WordToHex(b)+WordToHex(c)+WordToHex(d); 
	    return temp.toLowerCase(); 
    }-*/; 
}
