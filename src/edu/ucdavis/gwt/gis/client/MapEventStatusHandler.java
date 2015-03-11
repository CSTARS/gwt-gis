package edu.ucdavis.gwt.gis.client;

import java.util.LinkedList;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style.Cursor;
import com.google.gwt.dom.client.Style.Overflow;
import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;

import edu.ucdavis.cstars.client.Error;
import edu.ucdavis.cstars.client.MapWidget;
import edu.ucdavis.cstars.client.control.Control;
import edu.ucdavis.cstars.client.event.ErrorHandler;
import edu.ucdavis.cstars.client.event.LayerLoadHandler;
import edu.ucdavis.cstars.client.event.MapLayerAddHandler;
import edu.ucdavis.cstars.client.event.MapLayerRemoveHandler;
import edu.ucdavis.cstars.client.event.UpdateEndHandler;
import edu.ucdavis.cstars.client.event.UpdateStartHandler;
import edu.ucdavis.cstars.client.layers.Layer;
import edu.ucdavis.gwt.gis.client.toolbar.Toolbar;
import edu.ucdavis.gwt.gis.client.toolbar.button.ToolbarItem;
import edu.ucdavis.gwt.gis.client.resources.GadgetResources;

/**
 * Keeps track of layer events as well as primary client loading.  Checks when layers are 
 * loading, updating, finished or in an error state. This class contains logic, event handlers, 
 * status icon and status popup display for map and status overlay for app loading.
 * 
 * TODO: need to update layer names when the are updated in app
 * 
 * @author jrmerz
 */
public class MapEventStatusHandler  {

	public final static MapEventStatusHandler INSTANCE = new MapEventStatusHandler();
	
	private MapWidget map = null;
	//private RootPanel rootPanel = null;
	
	private IconPanel iconPanel = new IconPanel();
	
	private LinkedList<StatusLayer> layers = new LinkedList<StatusLayer>();
	
	private String currentStatus = "ok";
	
	private static int layerCount = 0;
	private StatusPopup popup = new StatusPopup();
	
	private HTML loadingText = new HTML();
	
	private HandlerRegistration popupResizeRef = null;
	
	// for when things go bad
	public Timer errorTimer = new Timer() {
		@Override
		public void run() {
			loadingText.setHTML("There appears to be a network error.<br />Showing what I got.");
			Timer t = new Timer() {
				@Override
				public void run() {
					setDone();
				}
			};
			t.schedule(4000);
		}
	};
	
	// for when things are taking a bit to load
	public Timer showTimer = new Timer() {
		@Override
		public void run() {
			loadingText.setVisible(true);
		}
	};
	
	private GisClient client = null;
	protected MapEventStatusHandler() {}
	
	/**
	 * init the class and show the loading panel
	 * 
	 * @param panel panel loading popup is to overlay
	 */
	public void init(GisClient gc) {
		this.client = gc;
		client.getRootPanel().getElement().getStyle().setOpacity(0);
		setTransition(client.getRootPanel().getElement());
		
		VerticalPanel vp = new VerticalPanel();
		vp.setWidth("100%");
		vp.setSpacing(10);
		vp.getElement().getStyle().setMarginTop(55, Unit.PX);
		Image icon = new Image(GadgetResources.INSTANCE.mapLoading());
		vp.add(icon);
		vp.add(loadingText);
		vp.setCellHorizontalAlignment(icon, HorizontalPanel.ALIGN_CENTER);
		vp.setCellHorizontalAlignment(loadingText, HorizontalPanel.ALIGN_CENTER);
		
		loadingText.setHTML("Loading ESRI Libraries...");
		loadingText.setVisible(false);
		loadingText.getElement().getStyle().setPosition(Position.ABSOLUTE);
		loadingText.getElement().getStyle().setTop(client.getTop(), Unit.PX);
		loadingText.getElement().getStyle().setLeft(client.getLeft(), Unit.PX);
		RootPanel.get().add(loadingText);
		
		popupResizeRef = Window.addResizeHandler(new ResizeHandler() {
			@Override
			public void onResize(ResizeEvent event) {
				loadingText.getElement().getStyle().setTop(client.getTop(), Unit.PX);
				loadingText.getElement().getStyle().setLeft(client.getLeft(), Unit.PX);
			}
		});
		
		showTimer.schedule(3000);
		errorTimer.schedule(30000);
	}
	
	/**
	 * Set map so statushandler can listen to events
	 * 
	 * @param mw
	 */
	public void setMap(MapWidget mw){
		loadingText.setHTML("<i class='fa fa-spinner fa-spin'></i> Initializing Map...");
		
		map = mw;
		
		map.addLayerAddHandler(new MapLayerAddHandler(){
			@Override
			public void onLayerAdd(Layer layer) {
				addLayer(layer, layer.getId());
			}
		});
		
		map.addLayerRemoveHandler(new MapLayerRemoveHandler(){
			@Override
			public void onLayerRemove(Layer layer) {
				removeLayer(layer.getId());
			}
		});

	}
	
	/**
	 * add layer status display to map
	 */
	public void addControl() {
		popup.setPosition(0, 60, edu.ucdavis.cstars.client.control.Position.BOTTOM_RIGHT);
		map.addControl(popup);
	}
	
	/**
	 * stop listening to layers events.  remove it from displays.
	 * 
	 * @param id - layer id to remove
	 */
	public void removeLayer(String id){
		StatusLayer sLayer = null;
		for( StatusLayer sl: layers ){
			if( id.contentEquals(sl.getLayer().getId()) ){
				sLayer = sl;
				break;
			}
		}
		if( sLayer != null ) {
			layers.remove(sLayer);
			popup.removeLayer(sLayer.getId());
			for( int i = sLayer.getId(); i < layers.size(); i++ ) {
				layers.get(i).setId(i);
			}
			layerCount--;
		}
	}

	
	/**
	 * Start listening to a layers events.  Show it in the display panel
	 * 
	 * @param layer - layer to add
	 * @param name - name of layer
	 */
	public void addLayer(Layer layer, String name) {
		boolean found = false;
		for( StatusLayer sl: layers ){
			if( layer.getId().contentEquals(sl.getLayer().getId()) ){
				found = true;
				break;
			}
		}
		if( !found ) {
			popup.addLayer(name, layerCount);
			layers.add(new StatusLayer(layer));
		}
	}
	
	/**
	 * Let primary load screen know the layers are loading
	 */
	public void setLoadingLayers() {
		loadingText.setHTML("Loading Map Layers...");
	}
	
	/**
	 * The client is loaded and the main loading panel should be removed
	 */
	public void setDone() {
		errorTimer.cancel();
		showTimer.cancel();
		RootPanel.get().remove(loadingText);
		client.getRootPanel().getElement().getStyle().setOpacity(1);
		if( popupResizeRef != null ) popupResizeRef.removeHandler();
	}
	
	/**
	 * Get the status icon for the toolbar.
	 * 
	 * @return
	 */
	public ToolbarItem getStatusButton() {
		return iconPanel;
	}
	
	/**
	 * Set the toolbar status icon to loading
	 */
	private void setLoading() {
		if( !currentStatus.contains("loading") ){
			iconPanel.setLoading();
		}
	}
	
	/**
	 * update layer panel status
	 */
	private void updateStatus() {
		boolean isError = false;
		
		for( StatusLayer layer: layers ){
			if( layer.getStatus().contentEquals("loading") ) {
				setLoading();
				return;
			}
			if( layer.getStatus().contentEquals("error") ){
				isError = true;
				break;
			}
		}
		
		if( isError ) {
			iconPanel.setError();
		} else {
			iconPanel.setOk();
		}
		
	}
	
	private native void setTransition(Element ele) /*-{
		ele.style['MozTransition'] = "opacity 1500ms linear";
		ele.style['WebkitTransition'] = "opacity 1500ms linear";
		ele.style['transition'] = "opacity 1500ms linear";
		ele.style['msTransition'] = "opacity 1500ms linear";
		ele.style['OTransition'] = "opacity 1500ms linear";
	}-*/;
	
	/**
	 * Private class for the layer panel.
	 * 
	 * @author jrmerz
	 */
	private class StatusLayer {
		private int statusPopupId = 0;
		private String status = "loading";
		public Layer layer;
		private boolean cancelLoadHandler = false;
		
		public StatusLayer(Layer l) {
			setLoading();
			statusPopupId = layerCount;
			layerCount++;
			
			layer = l;
			

			l.addLoadHandler(new LayerLoadHandler(){
				@Override
				public void onLoad(Layer layer) {
					if( !cancelLoadHandler ) {
						status = "ok";
						updateStatus();
						popup.setLoadedOk(statusPopupId);
					}
				}					
			});
			l.addErrorHandler(new ErrorHandler(){
				@Override
				public void onError(Error error) {
					status = "error";
					updateStatus();
					popup.setLoadedError(statusPopupId);
				}
			});

			
			l.addUpdateStartHandler(new UpdateStartHandler() {
				@Override
				public void onUpdateStart() {
					cancelLoadHandler = true;
					setLoading();
					status = "loading";
					popup.setUpdating(statusPopupId);
				}
			});
			
			l.addUpdateEndHandler(new UpdateEndHandler(){
				@Override
				public void onError(Error error) {
					status = "error";
					updateStatus();
					popup.setError(statusPopupId);
				}
				@Override
				public void onUpdateEnd() {
					status = "ok";
					updateStatus();
					popup.setOk(statusPopupId);
				}
			});
		}
		
		/**
		 * get the uid for this panel
		 * 
		 * @return
		 */
		public int getId() {
			return statusPopupId;
		}
		
		/**
		 * set a uid for this panel
		 * 
		 * @param id
		 */
		public void setId(int id) {
			statusPopupId = id;
		}
		
		/**
		 * get this panel's layer
		 * 
		 * @return
		 */
		public Layer getLayer() {
			return layer;
		}
		
		/**
		 * get this panel's current status
		 * 
		 * @return
		 */
		public String getStatus() {
			return status;
		}
		
	}
	
	/**
	 * Idicator icon for the toolbar
	 * 
	 * @author jrmerz
	 */
	private class IconPanel extends ToolbarItem {

		private Image statusImage = new Image(GadgetResources.INSTANCE.ok());
		
		@Override
		public String getIcon() {
			return "<i class='fa fa-question-circle'></i>";
		}

		@Override
		public void onAdd(Toolbar toolbar) {

		}

		public void onClick() {
			popup.show();
		}
		
		public void setError() {
			statusImage.setResource(GadgetResources.INSTANCE.error());
			currentStatus = "error";
		}
		
		public void setLoading() {
			statusImage.setResource(GadgetResources.INSTANCE.loading());
			currentStatus = "loading";
		}
		
		public void setOk() {
			statusImage.setResource(GadgetResources.INSTANCE.ok());
			currentStatus = "ok";
		}


		@Override
		public String getText() {
			return "";
		}
		
	}
	
	/**
	 * Popup control for the map which shows the status of layers
	 * 
	 * @author jrmerz
	 */
	public class StatusPopup extends Control {
		
		private FlexTable statusTable = new FlexTable();
		private SimplePanel panel = new SimplePanel();
		
		public StatusPopup() {
			panel.setStyleName(GadgetResources.INSTANCE.css().statusPopup());
			if( GisClient.isIE7() || GisClient.isIE8() ) {
				panel.getElement().getStyle().setProperty("border", "1px solid #888888");
			}
			
			VerticalPanel vp = new VerticalPanel();
			vp.setWidth("100%");
			
			HorizontalPanel hp = new HorizontalPanel();
			hp.add(new HTML("<div style='padding: 5px; color: #666666; font-size: 14px'>Layer Status</div>"));
			
			Image closeButton = new Image(GadgetResources.INSTANCE.icon_close());
			closeButton.getElement().getStyle().setMargin(8, Unit.PX);
			closeButton.getElement().getStyle().setCursor(Cursor.POINTER);
			closeButton.addMouseOverHandler(new MouseOverHandler(){
				@Override
				public void onMouseOver(MouseOverEvent event) {
					((Image) event.getSource()).setResource(GadgetResources.INSTANCE.icon_close_u());
				}
			});
			closeButton.addMouseOutHandler(new MouseOutHandler(){
				@Override
				public void onMouseOut(MouseOutEvent event) {
					((Image) event.getSource()).setResource(GadgetResources.INSTANCE.icon_close());
				}
			});
			closeButton.addClickHandler(new ClickHandler(){
				@Override
				public void onClick(ClickEvent event) {
					setVisible(false);
				}
			});
			
			hp.add(closeButton);
			hp.setWidth("100%");
			hp.setCellHorizontalAlignment(closeButton, HorizontalPanel.ALIGN_RIGHT);
			
			vp.add(hp);
			
			
			vp.add(statusTable);
			statusTable.setCellSpacing(5);
			
			panel.add(vp);
			initWidget(panel);
		}
		
		public void addLayer(String name, int id) {
			statusTable.setWidget(id, 0, new Image(GadgetResources.INSTANCE.loading()));
			HTML html = new HTML(name);
			html.setSize("80px", "20px");
			html.getElement().getStyle().setOverflow(Overflow.HIDDEN);
			html.getElement().getStyle().setProperty("whiteSpace", "nowrap");
			statusTable.setWidget(id, 1, html);
			statusTable.setHTML(id, 2, "<span style='white-space:nowrap'>Updating...</span>");
			updatePosition();
		}
		
		public void removeLayer(int id) {
			statusTable.removeRow(id);
			updatePosition();
		}
		
		public void setLoadedError(int id) {
			statusTable.setWidget(id, 0, new Image(GadgetResources.INSTANCE.error()));
			statusTable.setHTML(id, 2, "<span style='white-space:nowrap'>Error loading</span>");
		}
		
		public void setError(int id) {
			statusTable.setWidget(id, 0, new Image(GadgetResources.INSTANCE.error()));
			statusTable.setHTML(id, 2, "<span style='white-space:nowrap'>Error updating</span>");
		}
		
		public void setLoadedOk(int id) {
			statusTable.setWidget(id, 0, new Image(GadgetResources.INSTANCE.ok()));
			statusTable.setHTML(id, 2, "<span style='white-space:nowrap'>Loaded successfully</span>");
		}
		
		public void setOk(int id) {
			statusTable.setWidget(id, 0, new Image(GadgetResources.INSTANCE.ok()));
			statusTable.setHTML(id, 2, "<span style='white-space:nowrap'>Updated successfully</span>");
		}
		
		public void setUpdating(int id) {
			statusTable.setWidget(id, 0, new Image(GadgetResources.INSTANCE.loading()));
			statusTable.setHTML(id, 2, "<span style='white-space:nowrap'>Updating...</span>");
		}

		@Override
		public void init(MapWidget mapWidget) {
			setVisible(false);
		}
		
		public void show() {
			setVisible(true);
		}
		
	}
}
