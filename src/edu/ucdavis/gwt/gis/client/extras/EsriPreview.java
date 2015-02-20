package edu.ucdavis.gwt.gis.client.extras;

import edu.ucdavis.cstars.client.geometry.Extent;
import edu.ucdavis.cstars.client.restful.RestfulDocumentInfo;
import edu.ucdavis.cstars.client.restful.RestfulLayerInfo;
import edu.ucdavis.cstars.client.restful.RestfulLayersInfo;
import edu.ucdavis.gwt.gis.client.AppManager;
import edu.ucdavis.gwt.gis.client.auth.DomainToken;
import edu.ucdavis.gwt.gis.client.resources.GadgetResources;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.dom.client.Style.Visibility;
import com.google.gwt.event.dom.client.LoadEvent;
import com.google.gwt.event.dom.client.LoadHandler;
import com.google.gwt.http.client.URL;
import com.google.gwt.jsonp.client.JsonpRequestBuilder;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.SimplePanel;

public class EsriPreview extends Composite {
	
	private static final JsonpRequestBuilder XHR = new JsonpRequestBuilder();
	private static String basemap = "http://services.arcgisonline.com/ArcGIS/rest/services/Canvas/World_Light_Gray_Base/MapServer";
	static {
		XHR.setTimeout(30000);
	}
	
	public interface DataLoadHandler {
		public void onLayerDataLoad(RestfulLayerInfo layer);
	}
	private DataLoadHandler handler = null;

	private String url = "";
	private SimplePanel panel = new SimplePanel();
	private Image img = null;
	private HTML loadingIcon = new HTML("<i class='icon-spinner icon-spin'></i>");
	private FlowPanel canvas = new FlowPanel();

	private int width = 0;
	private int height = 0;
	private int loadingOffsetX = 0;
	private int loadingOffsetY = 0;
	private DomainToken domainToken = null;
	private double scale = 1;
	private boolean isImageServer = false;
	private boolean noCss = false;
	
	public EsriPreview(String url, int width, int height) {
		this.url = url;
		this.width = width;
		this.height = height;
		loadingOffsetX = (int) Math.floor(width / 2);
		loadingOffsetY = (int) Math.floor(height / 2);
		
		initWidget(panel);
		
		panel.setStyleName("EsriPreview");
		panel.setSize(width+"px", height+"px");
		canvas.setSize(width+"px", height+"px");
		canvas.getElement().getStyle().setPosition(Position.RELATIVE);
		panel.add(canvas);
		setPosition(loadingIcon.getElement(), loadingOffsetX, loadingOffsetY);
		
		canvas.add(loadingIcon);
		
		if( url.contains("ImageServer") ) {
			isImageServer = true;
			loadImageLayer();
		} else {
			loadMapServerLayer();
		}
	}
	
	public void noCss() {
		noCss = true;
		if( img != null ) img.setStyleName("");
		panel.setStyleName("");
	}
	
	private void loadImageLayer() {
		String tmpUrl = url+"?f=json";
		domainToken = AppManager.INSTANCE.getDomainAccess().getDomainToken(url);
		if( domainToken != null ) tmpUrl = tmpUrl + "&token="+domainToken.getToken();
		
		XHR.requestObject(tmpUrl, new AsyncCallback<JavaScriptObject>() {
			@Override
			public void onFailure(Throwable caught) {
				setError("Preview Generation Failed.<br />Could not access service information.");
			}
			@Override
			public void onSuccess(JavaScriptObject result) {
				try {
					if( !checkError(result) ) {
						create((RestfulLayerInfo) result);
						if( handler != null ) handler.onLayerDataLoad(null);
					} else {
						//setError("Preview Generation Failed.<br />Could not access service information.");
						
						// a json response was returned but says invalid url
						// more than likely an old server, try this instead
						tryFirstLayer();
					}
				} catch (Exception e) {
					setError("Preview Generation Failed.<br />Unknown response from service.");
				}
			}
		});
	}
	
	private void loadMapServerLayer() {
		String tmpUrl = url+"/layers?f=json";
		domainToken = AppManager.INSTANCE.getDomainAccess().getDomainToken(url);
		if( domainToken != null ) tmpUrl = tmpUrl + "&token="+domainToken.getToken();
		
		XHR.requestObject(tmpUrl, new AsyncCallback<JavaScriptObject>() {
			@Override
			public void onFailure(Throwable caught) {
				setError("Preview Generation Failed.<br />Could not access service information.");
			}
			@Override
			public void onSuccess(JavaScriptObject result) {
				try {
					if( !checkError(result) ) {
						RestfulLayerInfo layer = create((RestfulLayersInfo) result);
						if( handler != null ) handler.onLayerDataLoad(layer);
					} else {
						//setError("Preview Generation Failed.<br />Could not access service information.");
						
						// a json response was returned but says invalid url
						// more than likely an old server, try this instead
						tryFirstLayer();
					}
				} catch (Exception e) {
					setError("Preview Generation Failed.<br />Unknown response from service.");
				}
			}
		});
	}
	
	private void tryFirstLayer() {
		String tmpUrl = url+"/0?f=json";
		if( domainToken != null ) tmpUrl = tmpUrl + "&token="+domainToken.getToken();
		
		XHR.requestObject(tmpUrl, new AsyncCallback<JavaScriptObject>() {
			@Override
			public void onFailure(Throwable caught) {
				setError("Preview Generation Failed.<br />Could not access service information.");
			}
			@Override
			public void onSuccess(JavaScriptObject result) {
				try {
					if( !checkError(result) ) {
						create((RestfulLayerInfo) result);
						if( handler != null ) handler.onLayerDataLoad((RestfulLayerInfo) result);
					} else {
						setError("Preview Generation Failed.<br />Could not access service information.");
					}
				} catch (Exception e) {
					setError("Preview Generation Failed.<br />Unknown response from service.");
				}
			}
		});
	}
	
	private native boolean checkError(JavaScriptObject jso) /*-{
		if( jso ) {
			if( jso.error ) return true;
		}
		return false;
	}-*/;
	
	public void setDataLoadHandler(DataLoadHandler handler) {
		this.handler = handler;
	}
	
	private void setPosition(Element ele, int left, int top) {
		ele.getStyle().setPosition(Position.ABSOLUTE);
		ele.getStyle().setTop(top, Unit.PX);
		ele.getStyle().setLeft(left, Unit.PX);
	}
	
	private RestfulLayerInfo create(RestfulLayersInfo layers) {
		RestfulLayerInfo layer = null;
		for( int i = 0; i < layers.getLayers().length(); i++ ) {
			if( layers.getLayers().get(i).hasDefaultVisibility() ) {
				layer = layers.getLayers().get(i);
				break;
			}
		}
		if( layer == null && layers.getLayers().length() > 0 ) {
			layer = layers.getLayers().get(0);
		} else if(layer == null && layers.getLayers().length() == 0 ) {
			setError("No layers to display");
			return null;
		}
		
		create(layer);
		
		return layer;
	}
	
	private void create(RestfulLayerInfo layer) {
		Extent ext = layer.getExtent();
		String bbox = getBbox(layer);
		int wkid = ext.getSpatialReference().getWkid();
		String sr = "";
		if( wkid > 0 ) sr = String.valueOf(wkid);
		//if( sr.isEmpty() ) sr = URL.encodeQueryString(ext.getSpatialReference().getWkt());

		String exportEndPoint = "/export";
		if( isImageServer ) exportEndPoint = "/exportImage";
		
		// if we don't have an sr, the base map will most likely be off;
		if( sr.length() > 0 ) {
			Image base = new Image(basemap+"/export?bbox="+bbox+"&format=png&transparent=true&f=image&imageSR="+sr+"&bboxSR="+sr+"&size="+(width*2)+","+(height*2));
			base.setSize(width+"px", height+"px");
			setPosition(base.getElement(), 0, 0);
			base.setStyleName("EsriPreviewImage");
			canvas.add(base);
		}
		
		String tmpUrl = url+exportEndPoint+"?bbox="+bbox+"&format=png&transparent=true&f=image&imageSR="+sr+"&bboxSR="+sr+"&size="+(width*2)+","+(height*2);
		if( domainToken != null ) tmpUrl = tmpUrl + "&token="+domainToken.getToken();
		
		img = new Image(tmpUrl);
		img.setSize(width+"px", height+"px");
		setPosition(img.getElement(), 0, 0);
		if( !noCss ) img.setStyleName("EsriPreviewImage");
		img.getElement().getStyle().setVisibility(Visibility.HIDDEN);


		canvas.remove(loadingIcon);

		canvas.add(img);
		canvas.add(loadingIcon);
		
		img.addLoadHandler(new LoadHandler(){
			@Override
			public void onLoad(LoadEvent event) {
				canvas.remove(loadingIcon);
				img.getElement().getStyle().setVisibility(Visibility.VISIBLE);
			}
		});
		
		// make sure we check scale
		scaleImage(scale);
	}
	
	public void scaleImage(double scale) {
		this.scale = scale;
		
		int w = (int) Math.floor((double) width * scale);
		int h = (int) Math.floor((double) height * scale);
		
		for( int i = 0; i < canvas.getWidgetCount(); i++ ) {
			if( canvas.getWidget(i) instanceof Image ) {
				canvas.getWidget(i).setSize(w+"px", h+"px");
			}
		}
		panel.setSize(w+"px", h+"px");
		canvas.setSize(w+"px", h+"px");
	}
	
	public String getBbox(RestfulLayerInfo layer) {
		Extent extent = layer.getExtent();
		
		double w = (double) width * 0.000254;
		double cScale = (extent.getXMax() - extent.getXMin()) / w;
		double h = (double) height * 0.000254;
		double cScaleH = (extent.getYMax() - extent.getYMin()) / h;
		if( cScale > cScaleH ) cScale = cScaleH;
		
		if( cScale < layer.getMinScale() || layer.getMinScale() == 0 ) {
			return extent.getXMin() + "," + extent.getYMin() + "," + extent.getXMax() + "," + extent.getYMax();
		}
		
		double x = extent.getXMin() + ((extent.getXMax() - extent.getXMin()) / 2);
		double y = extent.getYMin() + ((extent.getYMax() - extent.getYMin()) / 2);
		
		double newWidth = (layer.getMinScale()-5) * (width * 0.000254);
		double newHeight = (layer.getMinScale()-5) * (height * 0.000254);
		
		double xMin = x - (newWidth / 2);
		double xMax = x + (newWidth / 2);
		double yMin = y - (newHeight / 2);
		double yMax = y + (newHeight / 2);
		
		return xMin + "," + yMin + "," + xMax + "," + yMax;
	}
	
	private void setError(String errorMsg) {
		panel.clear();
		panel.setSize(width+"px", "auto");
		HTML error = new HTML(errorMsg);
		error.setStyleName("ErrorMessage");
		panel.setStyleName("");
		panel.getElement().getStyle().setMarginRight(10, Unit.PX);
		panel.add(error);
	}
	



}
