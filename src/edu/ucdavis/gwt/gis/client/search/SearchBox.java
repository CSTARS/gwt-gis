package edu.ucdavis.gwt.gis.client.search;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.TextBox;

import edu.ucdavis.cstars.client.Error;
import edu.ucdavis.cstars.client.Graphic;
import edu.ucdavis.cstars.client.MapWidget;
import edu.ucdavis.cstars.client.Graphic.Attributes;
import edu.ucdavis.cstars.client.callback.AddressToLocationsCallback;
import edu.ucdavis.cstars.client.callback.BufferCallback;
import edu.ucdavis.cstars.client.callback.QueryTaskCallback;
import edu.ucdavis.cstars.client.callback.SimplifyCallback;
import edu.ucdavis.cstars.client.dojo.Color;
import edu.ucdavis.cstars.client.event.MouseEvent;
import edu.ucdavis.cstars.client.geometry.Geometry;
import edu.ucdavis.cstars.client.geometry.Point;
import edu.ucdavis.cstars.client.geometry.Polygon;
import edu.ucdavis.cstars.client.layers.GraphicsLayer;
import edu.ucdavis.cstars.client.symbol.SimpleFillSymbol;
import edu.ucdavis.cstars.client.symbol.SimpleLineSymbol;
import edu.ucdavis.cstars.client.tasks.Address;
import edu.ucdavis.cstars.client.tasks.AddressCandidate;
import edu.ucdavis.cstars.client.tasks.BufferParameters;
import edu.ucdavis.cstars.client.tasks.FeatureSet;
import edu.ucdavis.cstars.client.tasks.GeometryService;
import edu.ucdavis.cstars.client.tasks.Locator;
import edu.ucdavis.cstars.client.tasks.Query;
import edu.ucdavis.cstars.client.tasks.QueryTask;
import edu.ucdavis.cstars.client.tasks.GeometryService.UnitType;
import edu.ucdavis.gwt.gis.client.AppManager;
import edu.ucdavis.gwt.gis.client.config.PolyStyleConfig;
import edu.ucdavis.gwt.gis.client.config.SearchServiceConfig;
import edu.ucdavis.gwt.gis.client.draw.DrawControl;
import edu.ucdavis.gwt.gis.client.layers.FeatureCollectionDataLayer;
import edu.ucdavis.gwt.gis.client.toolbar.GeocodeResultsPanel;

public class SearchBox extends TextBox {
    
    private GeometryService gService;
    private static LinkedList<Locator> locators = new LinkedList<Locator>();
    private static LinkedList<GwtGisQueryTask> queries = new LinkedList<GwtGisQueryTask>();
    
    private GraphicsLayer graphicsLayer;
    
    private GeocodeResultsPanel resultsPopup = new GeocodeResultsPanel(this);        
    private LinkedList<AddressCandidate> locs = null;
    
    private AddressCandidate currentPlace = null;
    private QueryAll currentQuery = null;
    private MapWidget map = null;
    private GraphicMenu graphicMenu = new GraphicMenu();
    
    public SearchBox() {
        
        JsArray<SearchServiceConfig> searchServices = AppManager.INSTANCE.getConfig().getSearchServices();
        for( int i = 0; i < searchServices.length(); i++ ) {
            if( searchServices.get(i).getType().equals("geocoder") ) {
                locators.add(Locator.create(searchServices.get(i).getUrl()));
            } else {
                GwtGisQueryTask cqt = (GwtGisQueryTask) QueryTask.create(searchServices.get(i).getUrl());
                cqt.setParameter(searchServices.get(i).getParameter());
                cqt.setFormatter(searchServices.get(i).getFormatter());
                queries.add(cqt);
            }
        }
        
        getElement().setAttribute("placeholder", "Search");
        setStyleName("search-query");

        addKeyUpHandler(new KeyUpHandler(){
            public void onKeyUp(KeyUpEvent event) {
                if( event.getNativeKeyCode() == KeyCodes.KEY_ENTER ){
                    String searchTxt = getText().toLowerCase();
                    resultsPopup.clear();
                    search(searchTxt);
                }
            }
        });
        
        Window.addResizeHandler(new ResizeHandler(){
            @Override
            public void onResize(ResizeEvent event) {
                resize();
            }
        });
        

    }
    
    public void setMap(MapWidget map) {
        gService = GeometryService.create("http://atlas.resources.ca.gov/ArcGIS/rest/services/Geometry/GeometryServer");

        this.map = map;
        resize();
        
        final FeatureCollectionDataLayer layer = new FeatureCollectionDataLayer("Search Features");
        AppManager.INSTANCE.getClient().addLayer(layer);
        DrawControl.INSTANCE.setCurrentLayer(layer);
        
        map.addClickHandler(new edu.ucdavis.cstars.client.event.ClickHandler(){
            @Override
            public void onClick(MouseEvent event) {
                if( event.getGraphic() == null ) return;
                if( !layer.getGraphics().contains(event.getGraphic()) ) return;
                
                if( !DrawControl.INSTANCE.isEnabled() ) {
                    DrawControl.INSTANCE.enable(true);
                    DrawControl.INSTANCE.edit(event.getGraphic());
                }
            }
        });
    }
    
    
    public native void debug(JavaScriptObject jso) /*-{
        console.log(jso);
    }-*/;
    
    public native void debug(String jso) /*-{
        console.log(jso);
    }-*/;
    
    private void resize() {
        if( AppManager.INSTANCE.getClient().isPhoneMode() ) setWidth("100px");
        else setWidth("150px");
    }

    public void init(MapWidget mapWidget) {     
        map = mapWidget;
        graphicsLayer = map.getGraphics();
    }

    private void search(String searchTxt) {
        resultsPopup.add(new HTML("<i class='icon-spinner icon-spin'></i> Searching '"+searchTxt+"'... "));
        resultsPopup.show();
        if( currentQuery != null ) currentQuery.cancel();
        
        currentQuery = new QueryAll(searchTxt, new QueryAll.QueryComplete(){
            public void onComplete(LinkedList<SearchResult> results) {
                currentQuery = null;
                
                // sort alphabetically
                Collections.sort(results, new Comparator<SearchResult>(){
                    @Override
                    public int compare(SearchResult o1, SearchResult o2) {
                        return o1.name.compareTo(o2.name);
                    }
                });
                
                resultsPopup.clear();
                for( SearchResult r: results ) {
                    resultsPopup.add(createResultBtn(r)); 
                }
                
            }
        });
    }
    
    private HTML createResultBtn(final SearchResult r) {
        HTML btn = new HTML("<div style='cursor:pointer'>"+
                (r.center == null ? "<i class='icon-check-empty' style='color:blue'></i> " : "<i class='icon-map-marker' style='color:blue'></i> ")+
                r.name+" <span>"+
                (r.center == null ? "Boundary" : "Point")
                +"</span></div>"); 
        
        btn.addClickHandler(new ClickHandler(){
            @Override
            public void onClick(ClickEvent event) {
                if( r.center != null ) {
                    getBuffer(r);
                    map.centerAndZoom(r.center, 12);
                } else {
                    getGeometry(r);
                    resultsPopup.hide();
                }
            }
        });
        
        return btn;
    }
    
    private void getBuffer(final SearchResult sr) {
        BufferParameters params = BufferParameters.create();
        params.setDistances(new int[] {1000});
        params.setUnit(UnitType.UNIT_FOOT);
        params.setGeometries(new Geometry[] { sr.center });
        params.setOutSpatialRefernce(map.getSpatialReference());
        
        // get a buffered geometry
        gService.buffer(params, new BufferCallback(){
                @Override
                public void onBufferComplete(JsArray<Geometry> geometries) {
                    Graphic g = createSmallRadiusGraphic((Polygon) geometries.get(0));
                    Attributes attrs = Attributes.create();
                    attrs.setString("name", sr.name);
                    g.setAttributes(attrs);
                    DrawControl.INSTANCE.add(g);
                }
                @Override
                public void onError(Error error) {
                    Window.alert("Error getting the buffered geometry");
                }
        });
    }
    
    private void getGeometry(final SearchResult sr) {
        GwtGisQueryTask qt = null;
        for( GwtGisQueryTask cqt: queries ) {
            if( cqt.getUrl().contentEquals(sr.url) ) {
                qt = cqt;
                break;
            }
        }
        
        Query q = Query.create();
        q.setOutFields(new String[]{"*"});
        q.setReturnGeometry(true);
        q.setWhere(qt.getParameter()+" = '"+sr.id+"'");
        qt.execute(q, new QueryTaskCallback(){
            @Override
            public void onComplete(FeatureSet featureSet) {
                if( featureSet.getFeatures().length() == 0 ) {
                    Window.alert("Failed to fetch intersect geometry");
                    return;
                }
                
                gService.simplify(new Geometry[] {(Polygon) featureSet.getFeatures().get(0).getGeometry()}, 
                        new SimplifyCallback(){
                            @Override
                            public void onSimplifyComplete(
                                    JsArray<Geometry> geometries) {
                                Graphic g = createSmallRadiusGraphic((Polygon) geometries.get(0));
                                Attributes attrs = Attributes.create();
                                attrs.setString("name", sr.name);
                                g.setAttributes(attrs);
                                
                                
                                DrawControl.INSTANCE.add(g);
                            }

                            @Override
                            public void onError(Error error) {
                                debug(error);
                            }
                        }
                );

                
                map.setExtent(((Polygon) featureSet.getFeatures().get(0).getGeometry()).getExtent(), true);
            }
            @Override
            public void onError(Error error) {
                debug(error);
                //Window.alert("Failed to fetch intersect geometry");
            }
        });
        
    }
    
    private static class QueryAll {
        int queriesRemaining = 0;
        String searchTxt = "";
        LinkedList<SearchResult> resultList = new LinkedList<SearchResult>();
        boolean cancelled = false;
        
        interface QueryComplete {
            public void onComplete(LinkedList<SearchResult> results);
        }
        private QueryComplete callback = null;
        
        public QueryAll(String searchTxt, QueryComplete callback) {
            queriesRemaining = locators.size()+queries.size();
            this.searchTxt = searchTxt;
            this.callback = callback;
            
            
            for( Locator l: locators ) locate(l);
            for( GwtGisQueryTask qt: queries ) query(qt);
        }
        
        private void locate(final Locator l) {
            
            Address addr = Address.create();
            
            // add california if not already there

            
            addr.setSingleLineInput(searchTxt);
            
            Locator.Parameters params = Locator.Parameters.create();
            params.setAddress(addr);
            params.setOutFields(new String[] {"*"});
            
            l.addressToLocations(params, new AddressToLocationsCallback(){
                @Override
                public void onAddressToLocationsComplete(JsArray<AddressCandidate> candidates) {
                    LinkedList<AddressCandidate> topLocs = getTopLocations(candidates);
                    for( AddressCandidate loc: topLocs ) {
                        SearchResult sr = new SearchResult(loc.getAddressAsString(), loc.getLocation(), l.getUrl(), "");
                        resultList.add(sr);
                    }
                    checkDone();
                }
                @Override
                public void onError(Error error) {
                    checkDone();
                }
            });
        }
        
        private void query(final GwtGisQueryTask qt) {
            
            Query q = Query.create();
            q.setOutFields(new String[]{"*"});
            q.setReturnGeometry(false);
            q.setWhere("UPPER("+qt.getParameter()+") like '"+searchTxt.toUpperCase()+"%'");
            qt.execute(q, new QueryTaskCallback(){
                @Override
                public void onComplete(FeatureSet featureSet) {
                    int c = 0;
                    for( int i = 0; i < featureSet.getFeatures().length(); i++ ) {
                        Graphic.Attributes attrs = featureSet.getFeatures().get(i).getAttributes();
                        resultList.add(new SearchResult(qt.format(attrs), null, qt.getUrl(), attrs.getStringForced(qt.getParameter())));
                        c++;
                        if( c == 5 ) break;
                    }
                    checkDone();
                }
                @Override
                public void onError(Error error) {
                    checkDone();
                }
            });
        }
        
        private void checkDone() {
            queriesRemaining--;
            if( queriesRemaining <= 0 && !cancelled ) callback.onComplete(resultList);
        }
        
        private LinkedList<AddressCandidate> getTopLocations(JsArray<AddressCandidate> list ){
            LinkedList<AddressCandidate> tmpList = new LinkedList<AddressCandidate>();
            LinkedList<AddressCandidate> top = new LinkedList<AddressCandidate>();
            
            // remove all duplicates, but keep the highest score
            for( int i = 0; i < list.length(); i++ ){
                if( !list.get(i).getAddressAsString().matches(".*California.*") &&
                    !list.get(i).getAddressAsString().matches(".*CA.*") ) continue;
                
                AddressCandidate addr = list.get(i);
                boolean found = false;
                for( int j = 0; j < tmpList.size(); j++ ){
                    if( addr.getAddressAsString().contentEquals(tmpList.get(j).getAddressAsString()) ){
                        if( addr.getScore() > tmpList.get(j).getScore() ){
                            tmpList.remove(j);
                            tmpList.add(j, addr);
                        }
                        found = true;
                        break;
                    }
                }
                if( !found ) tmpList.add(addr);
            }
            
            // now grab the top 5 scores
            for( int i = 0; i < tmpList.size(); i++ ){
                AddressCandidate addr = tmpList.get(i);
                boolean found = false;
                for( int j = 0; j < top.size(); j++ ){
                    if( addr.getScore() > top.get(j).getScore() ){
                        top.add(j, addr);
                        found = true;
                        break;
                    }
                }
                if( !found && top.size() <= 5 ) top.add(addr);
                if( top.size() > 5 ) top.removeLast();
            }
            
            return top;
        }
        
        public void cancel() {
            cancelled = true;
        }
    }
    
    private Graphic createSmallRadiusGraphic(Polygon p){
        PolyStyleConfig style = AppManager.INSTANCE.getConfig().getSearchColor();
        SimpleFillSymbol fill = SimpleFillSymbol.create(
                        SimpleFillSymbol.StyleType.STYLE_SOLID,
                        SimpleLineSymbol.create(
                                        SimpleLineSymbol.StyleType.STYLE_SOLID,
                                        Color.create(style.getOutlineColor()),
                                        2
                        ),
                        Color.create(style.getFillColor())
        );              
        return Graphic.create(p, fill);
    }
    
    public static class SearchResult {
        String name = "";
        Point center = null;
        String url = "";
        String id = "";
        public SearchResult(String name, Point center, String url, String id){
            this.name = name;
            this.center = center;
            this.url = url;
            this.id = id;
        }
    }
    
    public static class GwtGisQueryTask extends QueryTask {
        
        protected GwtGisQueryTask() {}
        
        public final native boolean draw() /*-{
            if( this.__cqt_draw ) return this.__cqt_draw;
            return false;
        }-*/;
        
        public final native void setDraw(boolean draw) /*-{
            this.__cqt_draw = draw;
        }-*/;
        
        public final native String getParameter() /*-{
            return this.__cqt_parameter;
        }-*/;
        
        public final native void setParameter(String param) /*-{
            this.__cqt_parameter = param;
        }-*/;
        
        public final String format(Graphic.Attributes attrs) {
            if( hasFormatter() ) return _format(attrs);
            return attrs.getStringForced(getParameter());
        }
        
        private final native String _format(JavaScriptObject attrs) /*-{
            return this.__cqt_formatter(attrs);
        }-*/;
        
        private final native boolean hasFormatter() /*-{
            if( this.__cqt_formatter != null ) return true;
            return false;
        }-*/;
        
        public final native void setFormatter(JavaScriptObject formatter) /*-{
            this.__cqt_formatter = formatter;
        }-*/;
       
    }

}
