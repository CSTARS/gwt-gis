package edu.ucdavis.gwt.gis.client.extras;

import com.google.gwt.core.client.Callback;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.dom.client.Style.Cursor;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.geolocation.client.Geolocation;
import com.google.gwt.geolocation.client.Position;
import com.google.gwt.geolocation.client.PositionError;
import com.google.gwt.geolocation.client.Geolocation.PositionOptions;
import com.google.gwt.geolocation.client.Position.Coordinates;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.SimplePanel;

import edu.ucdavis.cstars.client.Graphic;
import edu.ucdavis.cstars.client.SpatialReference;
import edu.ucdavis.cstars.client.dojo.Color;
import edu.ucdavis.cstars.client.geometry.Extent;
import edu.ucdavis.cstars.client.geometry.Geometry;
import edu.ucdavis.cstars.client.geometry.Point;
import edu.ucdavis.cstars.client.geometry.Polygon;
import edu.ucdavis.cstars.client.symbol.PictureMarkerSymbol;
import edu.ucdavis.cstars.client.symbol.SimpleFillSymbol;
import edu.ucdavis.cstars.client.symbol.SimpleLineSymbol;
import edu.ucdavis.cstars.client.symbol.SimpleFillSymbol.StyleType;
import edu.ucdavis.gwt.gis.client.AppManager;
import edu.ucdavis.gwt.gis.client.resources.GadgetResources;

public class GeolocateWidget extends Composite {
	
	private Anchor icon = new Anchor("<i class='icon-screenshot'></i>",true);
	private Geolocation geolocater = Geolocation.getIfSupported();
	
	private Graphic marker = null;
	private Graphic markerBuffer = null;
	
	private PictureMarkerSymbol markerSymbol = null;
	private SimpleFillSymbol markerBufferSymbol = null; 
	
	private int watchId = -1;
	private boolean error = false;

	public GeolocateWidget() {
		if( !Geolocation.isSupported() ) {
			initWidget(new SimplePanel());
			return;
		}
		
		markerSymbol = PictureMarkerSymbol.create(GadgetResources.INSTANCE.bullet_blue().getSafeUri().asString(), 16, 16);
		markerBufferSymbol = SimpleFillSymbol.create(
				SimpleFillSymbol.StyleType.STYLE_SOLID, 
				SimpleLineSymbol.create(
						SimpleLineSymbol.StyleType.STYLE_SOLID,
						Color.create(34, 120, 218, .9),
						1), 
				Color.create(34, 120, 218, .4)
		);
		
		
		icon.addStyleName("btn");
		
		icon.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				
				if( watchId == -1 ) {
					icon.addStyleName("btn-warning");
					icon.removeStyleName("btn-success");
					icon.removeStyleName("btn-danger");
					
					error = false;
					watchId = watchPosition(new Callback<Position, PositionError>(){
						@Override
						public void onFailure(PositionError reason) {
							icon.removeStyleName("btn-warning");
							icon.removeStyleName("btn-success");
							icon.addStyleName("btn-danger");
							error = true;
						}
						@Override
						public void onSuccess(Position result) {
							icon.removeStyleName("btn-warning");
							icon.removeStyleName("btn-danger");
							icon.addStyleName("btn-success");
							
							double a = result.getCoordinates().getAccuracy();
							
							Coordinates point = result.getCoordinates();
							Point centerPoint = Point.create(
									point.getLongitude(), 
									point.getLatitude(), 
									SpatialReference.create(4269)
							);
							centerPoint = (Point) Geometry.geographicToWebMercator(centerPoint);
							Extent ext = Extent.create(centerPoint.getX()-a, centerPoint.getY()-a, 
									centerPoint.getX()+a, centerPoint.getY()+a, 
									AppManager.INSTANCE.getMap().getSpatialReference());
							AppManager.INSTANCE.getMap().setExtent(ext, true);						
							
							setMarker(centerPoint, a);
						}
					});
				} else {
					geolocater.clearWatch(watchId);
					watchId = -1;
					removeMarker();
					
					icon.removeStyleName("btn-warning");
					icon.removeStyleName("btn-success");
					icon.removeStyleName("btn-danger");
				}
			}
		});
		initWidget(icon);
	}
	
	private void setMarker(Point centerPoint, double r) {
		removeMarker();
		
		marker = Graphic.create(centerPoint, markerSymbol);
		markerBuffer = Graphic.create(createBuffer(centerPoint, r), markerBufferSymbol);
		AppManager.INSTANCE.getMap().getGraphics().add(markerBuffer);
		AppManager.INSTANCE.getMap().getGraphics().add(marker);
	}
	
	private void removeMarker() {
		if( marker != null ) {
			AppManager.INSTANCE.getMap().getGraphics().remove(marker);
			marker = null;
		}
		if( markerBuffer != null ) {
			AppManager.INSTANCE.getMap().getGraphics().remove(markerBuffer);
			markerBuffer = null;
		}
	}
	
	private Polygon createBuffer(Point p, double r) {
		JavaScriptObject arr = JavaScriptObject.createArray();
		double x = 0;
		double y = 0;
		for( int i = 0; i < 360; i++ ) {
			x = (r * Math.sin(Math.toRadians(i))) + p.getX();
			y = (r * Math.cos(Math.toRadians(i))) + p.getY();
			push(arr, x, y);
		}
		return Polygon.create(createPolygonJson(arr, AppManager.INSTANCE.getMap().getSpatialReference().getWkid()));
	}
	
	private native JavaScriptObject createPolygonJson(JavaScriptObject points, int wkid) /*-{
		return {"rings":[points],"spatialReference":{"wkid": wkid }};
	}-*/;
	
	private native void push(JavaScriptObject arr, double x, double y) /*-{
		arr.push([x, y]);
	}-*/;
	
	// hacking this bug: http://code.google.com/p/google-web-toolkit/issues/detail?id=6834
	  public native int watchPosition(Callback<Position, PositionError> callback) /*-{

		    var success = $entry(function(pos) {
		      @com.google.gwt.geolocation.client.Geolocation::handleSuccess(*)(callback, pos);
		    });

		    var failure = $entry(function(err) {
		      @com.google.gwt.geolocation.client.Geolocation::handleFailure(*)
		      (callback, err.code, err.message);
		    });

			var id = -1;
		    if (@com.google.gwt.geolocation.client.Geolocation::isSupported()) {
		      id = $wnd.navigator.geolocation.watchPosition(success, failure, null);
		    }
		    return id;
	}-*/;
	
}
