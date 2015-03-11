package edu.ucdavis.gwt.gis.client.addlayer;

import java.util.LinkedList;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style.Cursor;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.dom.client.Style.TextDecoration;
import com.google.gwt.dom.client.Style.VerticalAlign;
import com.google.gwt.dom.client.Style.Visibility;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.jsonp.client.JsonpRequest;
import com.google.gwt.jsonp.client.JsonpRequestBuilder;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

import edu.ucdavis.cstars.client.Error;
import edu.ucdavis.cstars.client.restful.RestfulServiceInfo;
import edu.ucdavis.cstars.client.restful.RestfulServicesDirectory;
import edu.ucdavis.cstars.client.restful.RestfulServiceInfo.Service;
import edu.ucdavis.gwt.gis.client.AppManager;
import edu.ucdavis.gwt.gis.client.GisClient;
import edu.ucdavis.gwt.gis.client.auth.DomainToken;
import edu.ucdavis.gwt.gis.client.auth.TokenPopupPanel;
import edu.ucdavis.gwt.gis.client.extras.EsriPreview;
import edu.ucdavis.gwt.gis.client.extras.SimpleOpacitySelector;
import edu.ucdavis.gwt.gis.client.layers.DataLayer.DataLayerType;
import edu.ucdavis.gwt.gis.client.layout.modal.BootstrapModalLayout;
import edu.ucdavis.gwt.gis.client.layout.modal.MainMenuFooter;
import edu.ucdavis.gwt.gis.client.resources.GadgetResources;
import edu.ucdavis.gwt.gis.client.toolbar.Toolbar;


public class ServiceAdder extends BootstrapModalLayout {
	
	public final static String DEFAULT_SERVICE = "atlas.resources.ca.gov";

	private static ServiceAdderUiBinder uiBinder = GWT.create(ServiceAdderUiBinder.class);
	interface ServiceAdderUiBinder extends UiBinder<Widget, ServiceAdder> {}
	
	private static JsonpRequestBuilder xhr = new JsonpRequestBuilder(); 

	@SuppressWarnings("rawtypes")
	private JsonpRequest currentRequest = null;
	
	@UiField TextBox input;
	@UiField VerticalPanel navigationPanel;
	@UiField HorizontalPanel cLocationPanel;
	@UiField VerticalPanel directoryPanel;
	@UiField Element layerPanel;
	@UiField TextBox title;
	@UiField HTML description;
	@UiField SimplePanel preview;
	@UiField SimpleOpacitySelector opacityInput;
	@UiField Element loading;
	@UiField Anchor layerBackButton;
	@UiField Anchor addLayerButton;
	// TODO
	//@UiField CheckBox addBasemapCheckbox;
	@UiField Anchor search;
	@UiField HTML errorPanel;
	@UiField Anchor tokenManager;
	
	private Toolbar toolbar = null;
	private Widget panel;
	
	private LinkedList<String> currentLocation = new LinkedList<String>();
	private String currentUrl = "";
	private static final String SERVICE_BASE = "/services/";

	private GisClient client = null;
	
	private DataLayerType type = DataLayerType.MapServer;
	
	private int top = 50;
	private int left = 70;
	
	private MainMenuFooter footer = new MainMenuFooter();
	
	public ServiceAdder(GisClient client) {
		panel = uiBinder.createAndBindUi(this);
		
		errorPanel.setVisible(false);
		this.client = client;
		xhr.setTimeout(5000);
		input.setText(DEFAULT_SERVICE);
		
		initHandlers();

		
	}
	
	private void initHandlers() {
		
		addLayerButton.addClickHandler(new ClickHandler(){
			@Override
			public void onClick(ClickEvent event) {
				// TODO
				//if( addBasemapCheckbox.getValue() ) addBasemap(); 
				if( type == DataLayerType.KML ) addKmlLayer();
				else if( type == DataLayerType.ImageServer ) addImageLayer();
				else addMapServiceLayer();
				
			}
		});
		
		layerBackButton.addClickHandler(new ClickHandler(){
			@Override
			public void onClick(ClickEvent event) {
				String token = currentLocation.removeLast();
				if( token.contentEquals("MapServer") ) currentLocation.removeLast();
				else if( token.contentEquals("ImageServer") ) currentLocation.removeLast();
				loadDirectory();
			}
		});
		
		input.addKeyUpHandler(new KeyUpHandler(){
			@Override
			public void onKeyUp(KeyUpEvent event) {
				if( event.getNativeKeyCode() == KeyCodes.KEY_ENTER ) {
					setUrl(input.getText());
					load();	
				}
			}
		});
		
		search.setHTML("<i class='fa fa-search'></i>");
		search.addClickHandler(new ClickHandler(){
			@Override
			public void onClick(ClickEvent event) {
				setUrl(input.getText());
				load();
			}
		});

		
		tokenManager.getElement().getStyle().setCursor(Cursor.POINTER);
		tokenManager.addClickHandler(new ClickHandler(){
			@Override
			public void onClick(ClickEvent event) {
				TokenPopupPanel.INSTANCE.show();
			}
		});
	}
	
	/**
	 *	This function is intended to take a FULL service url (http://..../mapserver
	 *	and display the servers contents, starting at it's root
	 * 
	 **/
	public void browseServer(String url) {
		// find just service
		url = url.replaceAll("/services/.*", "")+"/services";
		setUrl(url);
		
		navigationPanel.setVisible(false);
		layerPanel.getStyle().setDisplay(Display.NONE);
		
		load();

		show();
	}
	
	// assume we are trying for mapserver unless type can be found
	private void setUrl(String url) {
		if( url.endsWith("kml") || url.endsWith("kmz") ) {
			currentLocation.clear();
			if( !url.matches("^(http|https|ftp)://.*") ) url  = "http://"+url;
			currentUrl = url;
			type = DataLayerType.KML;
			return;
		} else if ( url.endsWith("ImageServer") ) {
			type = DataLayerType.ImageServer;
		} else {
			type = DataLayerType.MapServer;
		}
		
		String tmp = "";
		
		
		// this is a case of giving ArcGIS/rest/services which we want as ArcGIS/rest/services/
		if( url.matches(".*/services$") ) url = url + "/";
		
		if( url.contains(SERVICE_BASE) ) {
			tmp = url.replaceAll(SERVICE_BASE+".*", "");
		} else {
			tmp = url.replaceAll("/$", "") + "/ArcGIS/rest";
		}
			
		if( !tmp.matches("^(http|https)://.*") ) tmp  = "http://"+tmp;
		
		currentLocation.clear();
		String[] parts = url.replaceAll("\\?.*", "").replaceAll(tmp, "").split(SERVICE_BASE);
		if( parts.length > 1) {
			parts[1] = parts[1].replaceAll("^/", "");
			String[] p = parts[1].split("/");
			for(String s: p) {
				if( s.length() > 0 ) currentLocation.add(s);
			}
		}
		currentUrl = tmp;
	}
	
	public void setToolbar(Toolbar toolbar) {
		this.toolbar = toolbar;
	}
	
	private String getUrl() {
		String url = currentUrl + SERVICE_BASE;
		for( int i = 0; i < currentLocation.size(); i++ ) {
			if( i > 0 ) url += "/";
			url += currentLocation.get(i);
		}
		url = url + "?f=json";
		
		// see if we have a token;
		try {
			String domain = url.split(":\\/\\/")[1].replaceAll("\\/.*", "");
			DomainToken dt = AppManager.INSTANCE.getDomainAccess().getDomainToken(domain);
			if( dt != null ) url = url + "&token=" +dt.getToken();
		} catch(Exception e) {}

		
		return url;
	}
	
	private void addKmlLayer() {
		client.addLayer(currentUrl, title.getText(), opacityInput.getValue());
		hide();
	}
	
	private void addMapServiceLayer() {
		String url = currentUrl + SERVICE_BASE.replaceAll("/$", "");
		for( String l: currentLocation ) url += "/"+l;
		client.addLayer(url, title.getText(), opacityInput.getValue());
		hide();
	}
	
	private void addImageLayer() {
		String url = currentUrl + SERVICE_BASE.replaceAll("/$", "");
		for( String l: currentLocation ) url += "/"+l;
		client.addLayer(url, title.getText(), opacityInput.getValue());
		hide();
	}
	
	private void addBasemap() {
		String url = currentUrl + SERVICE_BASE.replaceAll("/$", "");
		for( String l: currentLocation ) url += "/"+l;
		GisClient.basemapSelector.add(title.getText(), url, "");
		hide();
	}
	
	private void load() {
		if( type == DataLayerType.KML ) {
			renderKml();
			return;
		} else if( currentLocation.size() > 0 ) {
			if( currentLocation.get(currentLocation.size()-1).contentEquals("MapServer") ) {
				loadLayer();
				return;
			} else if( currentLocation.size() == 2 && !currentLocation.get(1).contentEquals("MapServer") ) {
				currentLocation.add("MapServer");
				loadLayer();
				return;
			} else if( currentLocation.get(currentLocation.size()-1).contentEquals("ImageServer") ) {
				loadLayer();
				return;
			} else if( currentLocation.size() == 2 && !currentLocation.get(1).contentEquals("ImageServer") ) {
				currentLocation.add("ImageServer");
				loadLayer();
				return;
			}
		}
		loadDirectory();
	}
	
	private void cancelPending() {
		if( currentRequest != null ) {
			currentRequest.cancel();
		}
	}
	
	private void loadDirectory() {	
		setLoading(true);
		
		cancelPending();
		currentRequest = xhr.requestObject(getUrl(), 
			new AsyncCallback<JavaScriptObject>() {
				@Override
				public void onFailure(Throwable caught) {
					currentLocation.add("MapServer");
					loadLayer();
				}
				@Override
				public void onSuccess(JavaScriptObject result) {
					navigationPanel.setVisible(false);
					setLoading(false);
					if( checkError(result) ) {
						Error error = getError(result);
						navigationPanel.setVisible(false);
						layerPanel.getStyle().setDisplay(Display.NONE);
						errorPanel.setVisible(true);
						errorPanel.setHTML("Error: "+error.getMessage());
					} else {
						navigationPanel.setVisible(true);
						layerPanel.getStyle().setDisplay(Display.NONE);
						setLoading(false);
						renderInfo((RestfulServiceInfo) result);
						errorPanel.setVisible(false);
					}
				}
			}
		);
	}
	
	private void renderKml() {
		setLoading(false);
		cancelPending();
		navigationPanel.setVisible(false);
		layerPanel.getStyle().setDisplay(Display.BLOCK);
		errorPanel.setVisible(false);
		// TODO
		//addBasemapCheckbox.setVisible(false);
		preview.setVisible(false);
		layerBackButton.setVisible(false);
		
		title.setText(currentUrl.replaceAll(".*/", "").replaceAll("\\.kml", "").replaceAll("\\.kmz", ""));
		description.setHTML("KML file.");
	}
	
	private void loadLayer() {
		setLoading(true);
		
		cancelPending();
		currentRequest = xhr.requestObject(getUrl(), 
			new AsyncCallback<JavaScriptObject>() {
				@Override
				public void onFailure(Throwable caught) {
					setLoading(false);
					navigationPanel.setVisible(false);
					layerPanel.getStyle().setDisplay(Display.NONE);
					errorPanel.setVisible(true);
					errorPanel.setHTML("Timeout Loading: "+getUrl().replaceAll("^(http|https)://", "").replaceAll("/.*", ""));
				}
				@Override
				public void onSuccess(JavaScriptObject result) {
					navigationPanel.setVisible(false);
					setLoading(false);
					if( checkError(result) ) {
						Error error = getError(result);
						navigationPanel.setVisible(false);
						layerPanel.getStyle().setDisplay(Display.NONE);
						errorPanel.setVisible(true);
						errorPanel.setHTML("Error: "+error.getMessage());
					} else {
						layerPanel.getStyle().setDisplay(Display.BLOCK);
						renderLayer((RestfulServicesDirectory) result);
						errorPanel.setVisible(false);
					}
				}
			}
		);
	}
	
	private native boolean checkError(JavaScriptObject json) /*-{
		if( json ) {
			if( json.error ) return true;
		}
		return false;
	}-*/;
	
	private native Error getError(JavaScriptObject json) /*-{
		if( json ) {
			if( json.error ) return json.error;
		}
		return {};
	}-*/;
	
	private void renderLayer(RestfulServicesDirectory info) {
		// TODO
		//addBasemapCheckbox.setValue(false);
		//addBasemapCheckbox.setVisible(true);
		preview.setVisible(true);
		layerBackButton.setVisible(true);
		
		String title = info.getMapName();
		if( title.isEmpty() ) {
			title = info.getName();
		}
		if( title.toLowerCase().contentEquals("layers") ) {
			if( currentLocation.size() > 1 ) title = currentLocation.get(currentLocation.size()-2);
		}
		this.title.setText(title);
		
		String description = info.getDescription();
		if( description.length() == 0 ) description = info.getServiceDescription();
		if( description.length() > 300 ) description = description.substring(0, 300)+"...";
		if( description.length() > 0 ) description += "<br />";
		description += "<a href='"+getUrl().replaceAll("\\?.*","")+"' target='_blank'>Full ArcGIS Description</a>";
		this.description.setHTML(description);
		
		
		this.preview.clear();
		this.preview.add(new EsriPreview(getUrl().replaceAll("\\?.*",""), 200, 200));
		
	}
	
	private void renderInfo(RestfulServiceInfo info) {
		cLocationPanel.clear();
		
		String loc = "Services";
		for( String l: currentLocation ) loc += "/"+l;
		cLocationPanel.add(new HTML(loc));
		
		if( currentLocation.size() > 0 ) {
			Anchor back = new Anchor("Back");
			back.getElement().getStyle().setTextDecoration(TextDecoration.NONE);
			back.addClickHandler(new ClickHandler(){
				@Override
				public void onClick(ClickEvent event) {
					currentLocation.removeLast();
					loadDirectory();
				}
			});
			cLocationPanel.add(back);
			cLocationPanel.setCellHorizontalAlignment(back, HorizontalPanel.ALIGN_RIGHT);
		}
		
		int count = 0;
		
		directoryPanel.clear();
		JsArrayString folders = info.getFolders();
		for( int i = 0;  i < folders.length(); i++ ) {
			Anchor link = new Anchor("<i class='fa fa-folder-open-o'></i> "+folders.get(i),true);
			link.getElement().setAttribute("value", folders.get(i));
			link.setStyleName("menu-link");
			link.addClickHandler(browseLinkHandler);
			
			directoryPanel.add(link);
			count++;
		}
		
		JsArray<Service> services = info.getServices();
		for( int i = 0; i < services.length(); i++ ) {
			if( services.get(i).getType().contentEquals("MapServer") ) {
				//Image icon = new Image(GadgetResources.INSTANCE.layers());
				//icon.getElement().getStyle().setVerticalAlign(VerticalAlign.TOP);
				
				
				
				
				//HTML link = new HTML(icon.toString()+" "+services.get(i).getName().replaceAll(".*/", ""));
				//link.getElement().setAttribute("value", services.get(i).getName().replaceAll(".*/", ""));
				/*link.setStyleName("folderLink");
				link.addClickHandler(new ClickHandler(){
					@Override
					public void onClick(ClickEvent event) {
						 currentLocation.add( ((HTML) event.getSource()).getElement().getAttribute("value") );
						 currentLocation.add("MapServer");
						loadLayer();
					}
				});*/
				
				Anchor link = new Anchor("<i class='fa fa-square-o'></i> "+services.get(i).getName().replaceAll(".*/", ""),true);
				link.getElement().setAttribute("value", services.get(i).getName().replaceAll(".*/", ""));
				link.setStyleName("menu-link");
				link.addClickHandler(new ClickHandler(){
					@Override
					public void onClick(ClickEvent event) {
						 currentLocation.add( ((Anchor) event.getSource()).getElement().getAttribute("value") );
						 currentLocation.add("MapServer");
						loadLayer();
					}
				});
				
				
				directoryPanel.add(link);
				count++;
			} else if( services.get(i).getType().contentEquals("ImageServer") ) {
				Image icon = new Image(GadgetResources.INSTANCE.map());
				icon.getElement().getStyle().setVerticalAlign(VerticalAlign.TOP);
				HTML link = new HTML(icon.toString()+" "+services.get(i).getName().replaceAll(".*/", ""));
				link.getElement().setAttribute("value", services.get(i).getName().replaceAll(".*/", ""));
				link.setStyleName("folderLink");
				link.addClickHandler(new ClickHandler(){
					@Override
					public void onClick(ClickEvent event) {
						 currentLocation.add( ((HTML) event.getSource()).getElement().getAttribute("value") );
						 currentLocation.add("ImageServer");
						loadLayer();
					}
				});
				directoryPanel.add(link);
				count++;
			}
		}
		
		if( count == 0 ) {
			directoryPanel.add(new HTML("This folder contains no sub-folders or map services."));
		}

	}
	
	public void run() {
		navigationPanel.setVisible(false);
		layerPanel.getStyle().setDisplay(Display.NONE);
		currentLocation.clear();
		setLoading(false);

		show();
	}
	
	private void setLoading(boolean loading) {
		if( loading ) this.loading.getStyle().setVisibility(Visibility.VISIBLE);
		else this.loading.getStyle().setVisibility(Visibility.HIDDEN);
	}
	
	private String niceOpacity(double o){
		int i = (int) (o*100);
		if( i == 100 ) return i+"%";
		return i+"%";
	}
	
	private ClickHandler browseLinkHandler = new ClickHandler() {
		@Override
		public void onClick(ClickEvent event) {
			currentLocation.add( ((Anchor) event.getSource()).getElement().getAttribute("value") );
			loadDirectory();
		}
	};

	@Override
	public String getTitle() {
		return "Browser ArcGIS Server";
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
