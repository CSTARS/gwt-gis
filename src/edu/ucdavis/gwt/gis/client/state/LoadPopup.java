package edu.ucdavis.gwt.gis.client.state;

import java.util.Date;
import java.util.LinkedList;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Cursor;
import com.google.gwt.dom.client.Style.VerticalAlign;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

import edu.ucdavis.gwt.gis.client.layout.modal.BootstrapModalLayout;
import edu.ucdavis.gwt.gis.client.layout.modal.MainMenuFooter;
import edu.ucdavis.gwt.gis.client.resources.GadgetResources;

public class LoadPopup extends BootstrapModalLayout {

	private static LoadPopupUiBinder uiBinder = GWT.create(LoadPopupUiBinder.class);
	interface LoadPopupUiBinder extends UiBinder<Widget, LoadPopup> {}

	public static LoadPopup INSTANCE = new LoadPopup();
	
	@UiField FlowPanel mapsPanel;
	@UiField HTML messagePanel;
	@UiField HTML warningMessage;
	
	private Widget panel;
	private MainMenuFooter footer = new MainMenuFooter();
	
	public LoadPopup() {
		panel = uiBinder.createAndBindUi(this);
		
		warningMessage.setHTML(ClientStateManager.WARNING_MESSAGE);
	}
	
	public void show() {
		mapsPanel.clear();
		messagePanel.setHTML("");
		loadMaps();
		super.show();
	}
	
	private void loadMaps() {
		LinkedList<String> maps = ClientStateManager.getSavedMapNames();
		for(String map: maps) {
			mapsPanel.add(new MapSelectPanel(map));
		}
		if( maps.size() == 0 ) mapsPanel.add(new HTML("<span style='No saved maps to load.'></span>"));
	}
	
	private void load(String id) {
		Image loading = new Image(GadgetResources.INSTANCE.loading());
		loading.getElement().getStyle().setVerticalAlign(VerticalAlign.TOP);
		messagePanel.setHTML("<div style='color:green;padding:10px;text-align:center'>"+loading.toString()+" Loading Map...</div>");
		
		ClientStateManager.clearClientState();
		ClientStateManager.loadClientStateFromId(id);
		
		Timer t = new Timer() {
			@Override
			public void run() {
				hide();
			}
		};
		t.schedule(1500);
	}

	private class MapSelectPanel extends Composite {
		private SimplePanel panel = new SimplePanel();
		private String id = "";
		private String name = "";
		
		
		public MapSelectPanel(String mapName) {
			initWidget(panel);
			String[] parts = mapName.split("__");
			
			String date = "";
			if( parts.length >= 3 ) {
				name = parts[0];
				date = niceDate(parts[1]);
				id = parts[2];
			}
			
			HorizontalPanel hp = new HorizontalPanel();
			hp.setWidth("100%");
			
			Image map = new Image(GadgetResources.INSTANCE.map());
			map.getElement().getStyle().setVerticalAlign(VerticalAlign.TOP);
			HTML select = new HTML(map.toString()+" "+name+date);
			select.setStyleName("MapSelectPanel");
			hp.add(select);
			hp.setCellWidth(select, "85%");
			
			Image remove = new Image(GadgetResources.INSTANCE.remove());
			remove.setStyleName("AnimatedRemoveIcon");
			remove.setTitle("Delete Map");
			remove.getElement().getStyle().setCursor(Cursor.POINTER);
			hp.add(remove);
			hp.setCellHorizontalAlignment(remove, HorizontalPanel.ALIGN_RIGHT);
			hp.setCellVerticalAlignment(remove, HorizontalPanel.ALIGN_MIDDLE);
			remove.addClickHandler(new ClickHandler(){
				@Override
				public void onClick(ClickEvent event) {
					if( Window.confirm("Are you sure you want to delete: "+name) ) {
						ClientStateManager.removeState(id);
						mapsPanel.clear();
						loadMaps();
					}
				}
			});
			
			panel.add(hp);
			
			select.addClickHandler(new ClickHandler(){
				@Override
				public void onClick(ClickEvent event) {
					load(id);
				}
			});
		}
		
		@SuppressWarnings("deprecation")
		public String niceDate(String time) {
			try {
				Date d = new Date(Long.parseLong(time));
				String noonTime = "am";
				int hours = d.getHours();
				if( hours > 12 ) {
					hours = hours - 12;
					noonTime = "pm";
				}
				
				String min = String.valueOf(d.getMinutes());
				if( min.length() == 1 ) min = "0"+min; 
				
				return " - <span style='font-style:italic;color:#bbbbbb'>"+(d.getMonth()+1)+"/"+d.getDate()+"/"+
						(d.getYear()+1900)+" "+hours+":"+min+noonTime+"</span>";
			} catch(Exception e) {}
			return "";
		}
		
	}

	@Override
	public String getTitle() {
		return "Load Map from Browser";
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
