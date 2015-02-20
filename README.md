gwt-gis
=======

Online GIS Client created using the gwt-esri library

## Sample Implementation
```
public class MyMaps implements EntryPoint {

	private GisClient mapClient = null;

	public void onModuleLoad() {
		mapClient = new GisClient();		
		mapClient.load(new GisClientLoadHandler(){
			@Override
			public void onLoad() {
				onClientReady();
			}
		});
	}
		
	public void onClientReady() {
        // do stuff
   	}
}
```
[More complex example.](https://github.com/CSTARS/ceres-maps)

## GWT-GIS Config
The GisClient will look for a javascript object in the window scope call mapConfig.  You can access the config file in your app at any time by using:
```
GadgetConfig config = AppManager.INSTANCE.getConfig();
// or if you want to add in your own options ...
MyConfig config = (MyConfig) AppManager.INSTANCE.getConfig();
// with MyConfig extending GadgetConfig
```

### Config Options
```
var mapConfig = {
  // the defualt layers that will show on the right hand layer list
  // note: order here defines default order in list.
  dataLayers : [{
    // nice name for the layer
    label : '',
    // url to arcgis service or kml file
    url : '',
    // legend url... if you are using the load legend service
    legendUrl : '',
    // is your legend really a long list of gradient colors?  Set this flag to make the css fit
    legendIsGradient : false,
    // show the legend be expended (visible) by default
    showLegend : false,
    // should the layer be visible by default
    visible : true,
    // default opacity for the layer
    transparency : 0
  }, ...],
  
  // additional basemaps for the application
  basemaps : [{
    // name of basemap
    name : '',
    // url of basemap service
    url : '',
    // url of icon for basemap
    iconUrl : ''
  }, ...],
  
  // you can provide wfs layers that can be used to by the query tool for layer intersection.  Ex:
  // you provide a county layer, then a user can use select a county to see what visible layers
  // intersect that county
  intersectLayers : [{
    // same as dataLayer
  }, ...],
  
  // provide information about who you are, who is hosting the app
  provider : {
    // your homepage
    url : '',
    // HTML that will be set in the popup window
    popup : '',
    // should the info window show on start
    showOnStart : ''
  },
  
  // start center lng/lat for the app
  center : [-121.01, 38.22],
  
  // initial zoom level
  zoom : 7,
  
  // id of element client should be anchored to.  If not provided, the client will run
  // in fullscreen mode, appending to the body element
  anchor : '',
  
  // application title, will show in top nav bar
  title : 'MyMaps',
  
  // url of the NodeJS Proxy and Utility Server (see node dir in repo)
  // adds ability to export queries as shapefiles, kml or csv, allows query access cross-domain
  // access to non CORS enabled arcgis services.
  proxy : '',
  
  // many features require a geometry server to run, most arcgis servers have one
  // url of geomerty service
  geometryServer : '',
  
  // configure the arcgis print service so you can print from the client app
  print : {
    // url of arcgis print service
    server : '',
    defaultTemplate : '',
    // should users be allowed to select the print template, just leave off if no
    allowTemplateSelection : '',
    defaultFormat : '',
    // I think you see the pattern here...
    allowFormatSelection : '',
    defaultTitle : '',
    allowTitleSelection : '',
    allowLegendSelection : ''
  },
  
  // enable the app's idenitify tool, so user can make queries
  // will use intersect layers if provided above
  identifyTool : false,
  
  // enable users to upload shapefiles and add to map.  Will use the arcgis.com service to parse
  shapefileUpload : false,
  
  // enable Google Drive saving, loading and sharing of gwt-gis client maps.
  drive : {
    // name of folder that all users maps will be stored in, will create if it doesn't exist
    folderName : '',
    // google client id, visit here for more information: https://code.google.com/apis/console
    clientId : '',
    // google drive app id (required for sharing)
	  // visit here for more information: https://code.google.com/apis/console
	  // then click 'Drive SDK', your 'App Id' is located in the top left of the center panel
    appId : '',
    // google 'Simple Api Access' Key
  	// visit here for more information: https://code.google.com/apis/console
  	// then 'Create new Browser key' and set your apps 'refers' in the appropriate box
  	// this key is used when a 'non-logged-in' user loads a public map.  The request
  	// is then seen as a generic api access and thus, needs a key
    apiKey : '',
  }
  
  // services that should be used by the search box
  searchServices : [{
    // url of query service or arcgis geocoder
    url : "http://myserver.com/ArcGIS/rest/services/Boundaries/Areas/MapServer/0",
    // either "query" or "geocoder"
    type : "query",
    // for query service, what attribute do you want to search against
    parameter : "NAME",
    // option format function.  Name that service will be passed, you can format name as
    // you see fit here
    format : function(attr) {
      return attr.NAME+" (City)";
    }
  }, ...],
  
  // how should selected search polygons be drawn
  searchColor : {
    outline: {
        r: 255,
        g: 169,
        b: 0,
        a: 1
    },
    fill: {
        r: 255,
        g: 169,
        b: 0,
        a: .025
    }
  },
  
  // Should a layer panel allow you to browser the arcgis server?  This is great for discovery
  enableBrowseServer : true,
  
  // allow users to geolocate themselves using the browsers geolocation api
  enableGeolocate : true,
  
  // time in ms before the app gives up on a service url
  xhrTimeout : 20000,
}
```
