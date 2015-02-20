package edu.ucdavis.gwt.gis.client.toolbar;


import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

import edu.ucdavis.cstars.client.MapWidget;
import edu.ucdavis.cstars.client.control.Control;
import edu.ucdavis.cstars.client.event.MapResizeHandler;
import edu.ucdavis.cstars.client.geometry.Extent;
import edu.ucdavis.gwt.gis.client.AppManager;
import edu.ucdavis.gwt.gis.client.GisClient;
import edu.ucdavis.gwt.gis.client.config.GadgetConfig;
import edu.ucdavis.gwt.gis.client.extras.GeolocateWidget;
import edu.ucdavis.gwt.gis.client.identify.IdentifyTool;
import edu.ucdavis.gwt.gis.client.search.SearchBox;
import edu.ucdavis.gwt.gis.client.toolbar.button.ToolbarItem;
import edu.ucdavis.gwt.gis.client.toolbar.menu.ToolbarPopupMenu;
import edu.ucdavis.gwt.gis.client.toolbar.menu.ToolbarMenuItem;

public class Toolbar extends Control {

	private static UpperToolbarUiBinder uiBinder = GWT.create(UpperToolbarUiBinder.class);
	interface UpperToolbarUiBinder extends UiBinder<Widget, Toolbar> {}
	
	
	private SearchBox searchBox = null;
	private SimplePanel searchBoxPanel = new SimplePanel();
	
	private ToolbarPopupMenu popupMenu = new ToolbarPopupMenu("Actions");
	private Anchor popupMenuButton = new Anchor("<i class='icon-reorder'></i><span class='hidden-phone'> Menu</span>", true);
	
	@UiField FlowPanel menuPanel;
	@UiField FlowPanel statusPanel;
	@UiField SimplePanel messagePanel;
	@UiField SimplePanel searchPanel;
	@UiField HTMLPanel root;
	@UiField HTML title;
	@UiField Anchor navExpandBtn;
	
	public Toolbar() {
		initWidget(uiBinder.createAndBindUi(this));
		setStyleName("toolbar");

		popupMenuButton.addStyleName("btn");
		
		title.setHTML("");
		
		if( GisClient.isIE7() || GisClient.isIE8() ) {
			getElement().getStyle().setProperty("borderBottom", "1px solid #aaaaaa");
		}
		
		navExpandBtn.setHTML("<i class='icon-double-angle-left'></i>");
		navExpandBtn.addClickHandler(new ClickHandler(){
			@Override
			public void onClick(ClickEvent event) {
				AppManager.INSTANCE.getClient().getLayout().toggleNav(navExpandBtn);
			}
		});
		
		this.getElement().getStyle().setZIndex(100);
	}
	
	public void setTitle(String titleHtml) {
		title.setHTML(titleHtml);
	}
	
	public void showMenu() {
		popupMenu.show();
	}
	
	public void hideMenu() {
		popupMenu.hide();
	}
	
	public void addToolbarMenu(ToolbarMenuItem menu) {
		menu.init(popupMenu);
		popupMenu.addMenu(menu);
		menu.onAdd(this);
	}
	
	public void addToolbarItem(Widget w) {
        menuPanel.add(w);
    }
	
	public void addToolbarItem(ToolbarItem item) {
		item.init();
		menuPanel.add(item);
		item.onAdd(this);
	}
	
	public void addStatusItem(ToolbarItem item) {
		item.init();
		statusPanel.add(item);
		item.onAdd(this);
	}

	@Override
	public void init(MapWidget mapWidget) {
		setWidth(mapWidget.getWidth()+"px");
		mapWidget.addResizeHandler(new MapResizeHandler(){
			@Override
			public void onMapResize(Extent extent, int width, int height) {
				setWidth(width+"px");
			}
		});
		
		Window.addResizeHandler(new ResizeHandler(){
			@Override
			public void onResize(ResizeEvent event) {
				title.setVisible(event.getWidth() > 960);
			}
		});
		
		
		popupMenuButton.addClickHandler(new ClickHandler(){
			@Override
			public void onClick(ClickEvent event) {
				showMenu();
			}
		});
		menuPanel.add(popupMenuButton);
		
		//addSpacer();
		
		GadgetConfig config = AppManager.INSTANCE.getConfig();
		
		// add geolocate widget
		if( config.enableGeolocate() ) {
		    menuPanel.add(new GeolocateWidget());
		}
		
		// setup query tool
		if( config.enableIdentifyTool() ) {
			//QueryToolSettings.INSTANCE.setMap(mapWidget);
			//InspectManager.INSTANCE.setMap(mapWidget);
			menuPanel.add(IdentifyTool.INSTANCE.getToolbarIcon());
			//addSpacer();
		}
		
		// SEARCH CONTROL 
		searchBox = new SearchBox();
		searchBoxPanel.add(searchBox);
        searchPanel.add(searchBoxPanel);
        searchBox.setMap(mapWidget);
		
		moveZoomSlider(AppManager.INSTANCE.getMap().getId());
	}
	
	public void setSearchBox(Widget sb){
	    searchBoxPanel.clear();
	    searchBoxPanel.add(sb);
	}
	
	public void addSpacer() {
		HTML spacer = new HTML("&nbsp;");
		spacer.setStyleName("toolbarSpacer");
		menuPanel.add(spacer);
	}
	
	public void setMessage(Widget message) {
		messagePanel.clear();
		messagePanel.add(message);
	}
	
	private native void moveZoomSlider(String id) /*-{
		$wnd.document.getElementById(id+"_zoom_slider").style.top = "50px";
	}-*/;

}
