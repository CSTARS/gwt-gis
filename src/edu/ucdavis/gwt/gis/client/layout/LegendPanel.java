package edu.ucdavis.gwt.gis.client.layout;

import java.util.HashMap;


import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsArrayInteger;
import com.google.gwt.dom.client.Style.Cursor;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

import edu.ucdavis.cstars.client.MapWidget;
import edu.ucdavis.cstars.client.Util;
import edu.ucdavis.cstars.client.geometry.Extent;
import edu.ucdavis.cstars.client.layers.ArcGISDynamicMapServiceLayer;
import edu.ucdavis.cstars.client.layers.LayerInfo;
import edu.ucdavis.cstars.client.restful.LegendInfo;
import edu.ucdavis.gwt.gis.client.AppManager;
import edu.ucdavis.gwt.gis.client.layers.MapServerDataLayer;

public class LegendPanel extends Composite {
	
	private VerticalPanel panel = new VerticalPanel();
	private boolean crunch = false;
	private double minScale = 0;
	private double maxScale = 0;
	private double currentScale = 0;

	
	private HashMap<String, String> layerDetails = new HashMap<String, String>();
	private HashMap<String, HTML> detailButtons = new HashMap<String, HTML>();
	
	private PopupPanel detailsPanel = new PopupPanel();
	private MapServerDataLayer datalayer = null;
	
	private CheckBox visibleCheckBox = new CheckBox();
	
	/**
	 * 
	 * @param crunch - should we crunch this panel?
	 */
	public LegendPanel(MapServerDataLayer dl, boolean crunch, double minScale, double maxScale) {
		this.crunch = crunch;
		datalayer = dl;
		panel.setWidth("96%");
		panel.getElement().getStyle().setMargin(5, Unit.PX);
		initWidget(panel);
		
		this.minScale = minScale;
		this.maxScale = maxScale;
		
		if( minScale < 0 ) this.minScale = 0;
		if( maxScale < 0 ) this.maxScale = 0;
		
		MapWidget map = AppManager.INSTANCE.getMap();
		currentScale = Extent.getScale(map);
		updateVisibility();
		
		detailsPanel.setAutoHideEnabled(true);
		detailsPanel.setStyleName("LayerDetailsPopup");
	}
	
	public void setCurrentScale(double cScale) {
		currentScale = cScale;
	}
	
	public void setSubLayerData(JavaScriptObject json) {
		if( json == null ) return;
		
		JsArray<JavaScriptObject> arr = getLayers(json);
		for( int i = 0; i < arr.length(); i++ ) {
			JavaScriptObject layer = arr.get(i);
			String groupName = getGroupName(layer);
			
			if( isSingleValue(layer) ) {
				addRenderer(getRenderer(layer), i+"-0", groupName);
			} else  {
				JsArray<JavaScriptObject> renderers = getRenderers(layer);
				if( renderers != null ) {
					for( int j = 0; j < renderers.length(); j++ ) {
						addRenderer(renderers.get(j), getId(layer)+"-"+j, groupName);
					}
				}
			}
		}
	}
	
	private native int getId(JavaScriptObject jso) /*-{
		return jso.id;
	}-*/;
	
	private void addRenderer(JavaScriptObject renderer, String id, String groupName) {
		String title = getAttributeName(renderer);
		if( title.length() == 0 ) title = groupName;
		String layerHTML = title+"<br />";
		
		String desc = getDescription(renderer);
		if( desc.length() > 0 ) {
			layerHTML += "<div style='padding:2px;margin:2px;font-size:11px;color:#888888'>"+desc+"</div>";
		} else {
			layerHTML += "<div style='padding:2px;margin:2px;font-size:11px;color:#888888'>No further layer description provided</div>";
		}
		
		if( detailButtons.containsKey(id) && desc.length() > 0 ){
			layerDetails.put(id, layerHTML);
			
			HTML button = detailButtons.get(id);
			addDetailMouseHandlers(button);
			
			// drop in a clickable link if a label was not given to the renderer
			if( button.getText().length() == 0 ) button.setText("Layer Description");
		} 
	}
	
	private native String getDescription(JavaScriptObject json) /*-{
		if( json.description ) return json.description;
		return "";
	}-*/;
	
	private native String getAttributeName(JavaScriptObject json) /*-{
			if( json.label ) return json.label;
			if( json.value ) return json.value;
			return "";
	}-*/;
	
	private native boolean isSingleValue(JavaScriptObject json) /*-{
		if( json.drawingInfo ) {
			if( json.drawingInfo.renderer ) {
				if( json.drawingInfo.renderer.description ) {
					return true;
				}
			}
		}
		return false;
	}-*/;
	
	private native JavaScriptObject getRenderer(JavaScriptObject json) /*-{
		if( json.drawingInfo ) {
			if( json.drawingInfo.renderer ) {
				if( json.drawingInfo.renderer.description ) {
					return json.drawingInfo.renderer;
				}
			}
		}
		return {};
	}-*/;
	
	private native JsArray<JavaScriptObject> getRenderers(JavaScriptObject json) /*-{
		if( json.drawingInfo ) {
			if( json.drawingInfo.renderer ) {
				return json.drawingInfo.renderer.uniqueValueInfos;
			}
		}
		return [];
	}-*/;
	
	
	private native JsArray<JavaScriptObject> getLayers(JavaScriptObject json) /*-{
		return json.layers;
	}-*/;
	
	private native String getGroupName(JavaScriptObject json) /*-{
		return json.name;
	}-*/;
	
	/**
	 * Update the check box to correct visibility for layer
	 */
	public void updateCheckBox() {
		if( datalayer.isTiled() ) return;
		
		String sVal = visibleCheckBox.getElement().getAttribute("lid");
		int val = Integer.parseInt(sVal);
		
		ArcGISDynamicMapServiceLayer layer = (ArcGISDynamicMapServiceLayer) datalayer.getMapLayer();
		JsArrayInteger visibleIds = layer.getVisibleLayers();

		for( int i = 0; i < visibleIds.length(); i++ ) {
			if( visibleIds.get(i) == val ) {
				visibleCheckBox.setValue(true);
				return;
			}
		}
		visibleCheckBox.setValue(false);
	}
	
	public boolean updateVisibility() {		
		boolean isVisible = false;
		
		if( minScale >= currentScale && maxScale <= currentScale) {
			isVisible = true;
		} else if ( minScale == 0 && maxScale == 0 ) {
			isVisible = true;
		}
		setVisible(isVisible);
		return isVisible;
	}
	
	public void setLayerTitle(LayerInfo layer, boolean defaultVisible){
		String title = layer.getName();
		//int layerId = getLegendLayerId(layer);
		int layerId = layer.getId();
		
		HorizontalPanel hp = new HorizontalPanel();
		hp.setWidth("100%");
		
		if( !datalayer.isTiled() ) {
			
			visibleCheckBox.setValue(defaultVisible);
			visibleCheckBox.getElement().setAttribute("lid", layerId+"");
			visibleCheckBox.addClickHandler(new ClickHandler(){
				@Override
				public void onClick(ClickEvent event) {
					String sVal = ((CheckBox) event.getSource()).getElement().getAttribute("lid");
					int val = Integer.parseInt(sVal);
					ArcGISDynamicMapServiceLayer dLayer = (ArcGISDynamicMapServiceLayer) datalayer.getMapLayer();
					
					JsArrayInteger ints = null;
					if( ((CheckBox) event.getSource()).getValue() ) ints = addLayer(dLayer.getVisibleLayers(), val);
					else ints = removeLayer(dLayer.getVisibleLayers(), val);
					
					int[] visible = new int[ints.length()];
					for( int i = 0; i < ints.length(); i++ ) {
						visible[i] = ints.get(i);
					}
					
					dLayer.setVisibleLayers(visible);
				}
			});
			hp.add(visibleCheckBox);
			hp.setCellVerticalAlignment(visibleCheckBox, VerticalPanel.ALIGN_MIDDLE);
			hp.setCellWidth(visibleCheckBox, "20px");
		}
		
		HTML html = new HTML(title.replaceAll("_", " "));
		html.setStyleName("LayerLegendPanelTitle-content");
		hp.add(html);
		hp.setStyleName("LayerLegendPanelTitle");
		
		panel.add(hp);
	}
	
	public void setLayerTitle(String title){
		HTML html = new HTML(title);
		html.setStyleName("LayerLegendPanelTitle");
		panel.add(html);
	}
	
	private native JsArrayInteger removeLayer(JsArrayInteger arr, int layerId ) /*-{
		var vals = [];
		for( var i in arr ) {
			if( arr[i] != layerId ) vals.push(arr[i]);
		}
		if( vals.length == 0 ) vals.push(-1);
		return vals;
	}-*/;
	
	private native JsArrayInteger addLayer(JsArrayInteger arr, int layerId ) /*-{
		arr.push(layerId);
		return arr;
	}-*/;
	
	public void addLayerElement(LegendInfo li, LayerInfo layerInfo, int index){
		String name = li.getLabel();
		Image icon = getIcon(li);
		//String id = getLegendLayerId(layerInfo)+"-"+index;
		String id = layerInfo.getId()+"-"+index;
		
		HorizontalPanel hp = new HorizontalPanel();
		hp.setWidth("98%");
		
		HTML html = new HTML(name);
		html.getElement().setAttribute("value", id);

		if( detailButtons.containsKey(id) ){
			addDetailMouseHandlers(html);
		} else {
			detailButtons.put(id, html);
		}
		
		if( !crunch ) {
			hp.setHeight("30px");	
		}
		
		hp.add(html);
		hp.setCellVerticalAlignment(html, HorizontalPanel.ALIGN_MIDDLE);
		
		if( crunch ) {
			icon.getElement().getStyle().setMarginBottom(-16, Unit.PX);
		} else {
			icon.setStyleName("LegendIcon");
		}
		
		hp.add(icon);
		hp.setCellHorizontalAlignment(icon, HorizontalPanel.ALIGN_RIGHT);
		
		panel.add(hp);
	}
	
	private void addDetailMouseHandlers(HTML html) {
		html.getElement().getStyle().setCursor(Cursor.POINTER);
		html.getElement().getStyle().setColor("#2278DA");
		detailsPanel.addAutoHidePartner(html.getElement());
		html.addClickHandler(new ClickHandler(){
			@Override
			public void onClick(ClickEvent event) {
				if( !detailsPanel.isShowing() ) {
					HTML icon = (HTML) event.getSource();
					String key = icon.getElement().getAttribute("value");		
					
					String layerDetail = "No layer description provided.";
					if( layerDetails.containsKey(key) ) {
						 layerDetail = layerDetails.get(key);
					} 
					
					detailsPanel.clear();
					detailsPanel.setWidth("300px");
					
					detailsPanel.add(new HTML(layerDetail));
					
					detailsPanel.setVisible(false);
					detailsPanel.show();
					
					// if the height of the popup is more then half the height of the screen add an extra 100px to width
					if( detailsPanel.getOffsetHeight() > (int)  Math.floor(Window.getClientHeight() / 2) ) {
						detailsPanel.setWidth("400px");
					}
					
					// calc top and left for popup
					int left = icon.getAbsoluteLeft()-detailsPanel.getOffsetWidth();
					int top = icon.getAbsoluteTop();
					
					// now make sure it is all inside screen
					if( top + detailsPanel.getOffsetHeight() > Window.getClientHeight() ) {
						top = top - ((top + detailsPanel.getOffsetHeight()) - Window.getClientHeight()) - 3;
					}
					
					detailsPanel.setPopupPosition(left, top);
					detailsPanel.setVisible(true);
				}
			}
		});
		html.addMouseOutHandler(new MouseOutHandler() {
			@Override
			public void onMouseOut(MouseOutEvent event) {
				if( detailsPanel.isShowing() ) detailsPanel.hide();
			}
		});
	}
	
	private native int getLegendLayerId(LayerInfo jso) /*-{
		if( jso.layerInfo ) return jso.layerId;
		return -1;
	}-*/;
	
	private Image getIcon( LegendInfo legend ){
		if( legend.getImageData().length() > 0 ) {
			return new LegendIcon(  legend.getImageData(),  legend.getContentType(), 26, 26);
		} 
		return new Image(legend.getUrl());
	}
	
	private class LegendIcon extends Image {
        public LegendIcon(String data, String contentType, int height, int width ){
                super();
                getElement().setAttribute("border", "0");
                if( contentType.isEmpty() ) contentType = "image/png";
                setUrl("data:"+contentType+";base64,"+data);
        }
	}
	
	
}
