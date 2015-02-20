package edu.ucdavis.gwt.gis.client.draw;

import java.util.LinkedList;


import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.dom.client.Style.Cursor;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

import edu.ucdavis.cstars.client.Graphic;
import edu.ucdavis.cstars.client.Graphic.Attributes;
import edu.ucdavis.cstars.client.dojo.Color;
import edu.ucdavis.cstars.client.geometry.Point;
import edu.ucdavis.cstars.client.geometry.Polygon;
import edu.ucdavis.cstars.client.geometry.Polyline;
import edu.ucdavis.cstars.client.geometry.Geometry.GeometryType;
import edu.ucdavis.cstars.client.layers.GraphicsLayer;
import edu.ucdavis.cstars.client.symbol.SimpleFillSymbol;
import edu.ucdavis.cstars.client.symbol.SimpleLineSymbol;
import edu.ucdavis.cstars.client.symbol.SimpleMarkerSymbol;
import edu.ucdavis.cstars.client.symbol.Symbol;
import edu.ucdavis.gwt.gis.client.AppManager;
import edu.ucdavis.gwt.gis.client.Debugger;
import edu.ucdavis.gwt.gis.client.GisClient;
import edu.ucdavis.gwt.gis.client.layers.DataLayer;
import edu.ucdavis.gwt.gis.client.layers.FeatureCollectionDataLayer;
import edu.ucdavis.gwt.gis.client.layers.DataLayer.DataLayerType;
import edu.ucdavis.gwt.gis.client.layout.OpacitySelector;
import edu.ucdavis.gwt.gis.client.layout.OpacitySelector.SelectHandler;
import edu.ucdavis.gwt.gis.client.layout.modal.BootstrapModalLayout;
import edu.ucdavis.gwt.gis.client.resources.GadgetResources;

/**
 * This panel allows for editing of a features attributes and style.
 * 
 * @author jrmerz
 */
public class EditFeaturePanel extends BootstrapModalLayout {

	private static EditFeaturePanelUiBinder uiBinder = GWT.create(EditFeaturePanelUiBinder.class);
	interface EditFeaturePanelUiBinder extends UiBinder<Widget, EditFeaturePanel> {}

	public static int[] WIDTH_OPTIONS = new int[] {1, 2, 3, 4, 5, 6};
	           
	public InfoPopup infoPopup = new InfoPopup();
	
	private Widget panel;
	private Anchor close = new Anchor("Close");

	@UiField VerticalPanel pointStylePanel;
	@UiField VerticalPanel lineStylePanel;
	@UiField VerticalPanel fillStylePanel;
	@UiField VerticalPanel attributes;
	@UiField HTML attributesButton;
	@UiField HTML styleButton;
	@UiField HorizontalPanel hidePanel;
	@UiField HTML addAttributeButton;
	@UiField TextBox newAttributeName;
	@UiField TextArea newAttributeValue;
	@UiField TextBox name;
	@UiField ListBox pointStyleInput;
	@UiField ListBox lineWidthInput;
	@UiField ListBox lineStyleInput;
	@UiField HTML lineOpacity;
	@UiField HTML fillOpacity;
	@UiField TextBox fillColorInput;
	@UiField TextBox lineColorInput;
	@UiField TextBox pointColorInput;
	@UiField TextBox pointSizeInput;
	@UiField HTML zoomTo;
	@UiField HTML collectionName;
	
	private OpacitySelector lineOpacitySelector = new OpacitySelector();
	private OpacitySelector fillOpacitySelector = new OpacitySelector();
	
	private static Graphic currentGraphic = null;
	// visibly show a point is selected
	public static Graphic pointOutline = null; 
	
	/**
	 * Create a new EditFeaturePanel
	 */
	public EditFeaturePanel() {
		panel = uiBinder.createAndBindUi(this);
		initHandlers();
		setShowingAttributesPanel(true);
	}

	/**
	 * Setup clickhandlers and other various event handling.
	 */
	private void initHandlers() {
		zoomTo.getElement().getStyle().setFontSize(11, Unit.PX);
		zoomTo.addClickHandler(new ClickHandler(){
			@Override
			public void onClick(ClickEvent event) {
				if( currentGraphic != null ) {
					GeometryType type = currentGraphic.getGeometry().getType();
					if( type == GeometryType.POINT ) {
						AppManager.INSTANCE.getMap().centerAt(
								(Point) currentGraphic.getGeometry()
						);
					} else if ( type == GeometryType.POLYLINE ) {
						AppManager.INSTANCE.getMap().setExtent(
								((Polyline) currentGraphic.getGeometry()).getExtent()
						, true);
					} else if  ( type == GeometryType.POLYGON ) {
						AppManager.INSTANCE.getMap().setExtent(
								((Polygon) currentGraphic.getGeometry()).getExtent()
						, true);
					}
					hide();
				}
			}
		});
		
		close.addStyleName("btn");
		close.addClickHandler(new ClickHandler(){
			@Override
			public void onClick(ClickEvent event) {
				hide();
			}
		});
		
		styleButton.addClickHandler(new ClickHandler(){
			@Override
			public void onClick(ClickEvent event) {
				setShowingAttributesPanel(false);
			}
		});
		attributesButton.addClickHandler(new ClickHandler(){
			@Override
			public void onClick(ClickEvent event) {
				setShowingAttributesPanel(true);
			}
		});
		
		addAttributeButton.addClickHandler(new ClickHandler(){
			@Override
			public void onClick(ClickEvent event) {
				addAttribute();
			}
		});
		
		name.addKeyUpHandler(new KeyUpHandler(){
			@Override
			public void onKeyUp(KeyUpEvent event) {
				Attributes attrs = currentGraphic.getAttributes();
				if( attrs == null ) attrs = (Attributes) JavaScriptObject.createObject();		
				attrs.setString("name", name.getText());
				currentGraphic.setAttributes(attrs);
			}
		});

		// POINT STYLE
		SimpleMarkerSymbol.StyleType[] markerStyles = SimpleMarkerSymbol.StyleType.values();
		for( SimpleMarkerSymbol.StyleType style: markerStyles ) {
			if( style.getValue().length() > 0 ) 
				pointStyleInput.addItem(getNiceName(style.getValue()), style.getValue());
		}
		pointStyleInput.addChangeHandler(new ChangeHandler(){
			@Override
			public void onChange(ChangeEvent event) {
				GeometryType type = currentGraphic.getGeometry().getType();
				String style = pointStyleInput.getValue(pointStyleInput.getSelectedIndex());
				if( type == GeometryType.POINT ) {
					SimpleMarkerSymbol s = (SimpleMarkerSymbol) currentGraphic.getSymbol();
					s.setStyle(getMarkerStyle(style));
				}
				update();
				updatePointOutline();
			}
		});
		
		// POINT SIZE
		pointSizeInput.addBlurHandler(new BlurHandler(){
			@Override
			public void onBlur(BlurEvent event) {
				updatePointSize();
			}
		});
		pointSizeInput.addKeyUpHandler(new KeyUpHandler(){
			@Override
			public void onKeyUp(KeyUpEvent event) {
				if( event.getNativeKeyCode() == KeyCodes.KEY_ENTER ) {
					updatePointSize();
				}
			}
		});
		
		// POINT COLOR
		if( !GisClient.isIE7() && !GisClient.isIE8() ) { 
			pointColorInput.getElement().setAttribute("type", "color");
		}
		pointColorInput.addValueChangeHandler(new ValueChangeHandler<String>(){
			@Override
			public void onValueChange(ValueChangeEvent<String> event) {
				updatePointColor();
			}
		});

		// LINE WIDTH
		for( int w: WIDTH_OPTIONS ) {
			lineWidthInput.addItem(w+"");
		}
		lineWidthInput.addChangeHandler(new ChangeHandler(){
			@Override
			public void onChange(ChangeEvent event) {
				GeometryType type = currentGraphic.getGeometry().getType();
				String width = lineWidthInput.getValue(lineWidthInput.getSelectedIndex());
				if( type == GeometryType.POLYLINE ) {
					SimpleLineSymbol s = (SimpleLineSymbol) currentGraphic.getSymbol();
					s.setWidth(Integer.valueOf(width));
				} else if( type == GeometryType.POLYGON ) {
					SimpleFillSymbol s = (SimpleFillSymbol) currentGraphic.getSymbol();
					s.getOutline().setWidth(Integer.valueOf(width));
				}
				update();
			}
		});
		
		// LINE OPACITY
		lineOpacity.addClickHandler(new ClickHandler(){
			@Override
			public void onClick(ClickEvent event) {
				double o = 1;
				try {
					o = Double.parseDouble(lineOpacity.getText().replace("%", "")) / (double) 100;
				} catch(Exception e) {}
				lineOpacitySelector.show(lineOpacity, o);
			}
		});
		lineOpacitySelector.setSelectHandler(new SelectHandler(){
			@Override
			public void onSelect(double opacity) {
				lineOpacity.setText("%"+(int) Math.floor(opacity*100));
				GeometryType type = currentGraphic.getGeometry().getType();
				if( type == GeometryType.POLYLINE ) {
					SimpleLineSymbol s = (SimpleLineSymbol) currentGraphic.getSymbol();
					Color c = s.getColor();
					c.setColor(c.getRed(), c.getGreen(), c.getBlue(), opacity);
				} else if( type == GeometryType.POLYGON ) {
					SimpleFillSymbol s = (SimpleFillSymbol) currentGraphic.getSymbol();
					Color c = s.getOutline().getColor();
					c.setColor(c.getRed(), c.getGreen(), c.getBlue(), opacity);
				}
				update();
			}
		});

		// LINE COLOR
		if( !GisClient.isIE7() && !GisClient.isIE8() ) { 
			lineColorInput.getElement().setAttribute("type", "color");
		}
		lineColorInput.addValueChangeHandler(new ValueChangeHandler<String>(){
			@Override
			public void onValueChange(ValueChangeEvent<String> event) {
				updateLineColor();
			}
		});
		
		// LINE STYLE
		SimpleLineSymbol.StyleType[] lineStyles = SimpleLineSymbol.StyleType.values();
		for( SimpleLineSymbol.StyleType style: lineStyles ) {
			if( style.getValue().length() > 0 ) 
				lineStyleInput.addItem(getNiceName(style.getValue()), style.getValue());
		}
		lineStyleInput.addChangeHandler(new ChangeHandler(){
			@Override
			public void onChange(ChangeEvent event) {
				GeometryType type = currentGraphic.getGeometry().getType();
				String style = lineStyleInput.getValue(lineStyleInput.getSelectedIndex());
				if( type == GeometryType.POLYLINE ) {
					SimpleLineSymbol s = (SimpleLineSymbol) currentGraphic.getSymbol();
					s.setStyle(getLineStyle(style));
				} else if( type == GeometryType.POLYGON ) {
					SimpleFillSymbol s = (SimpleFillSymbol) currentGraphic.getSymbol();
					s.getOutline().setStyle(getLineStyle(style));
				}
				update();
			}
		});

		// FILL OPACITY
		fillOpacity.addClickHandler(new ClickHandler(){
			@Override
			public void onClick(ClickEvent event) {
				double o = 1;
				try {
					o = Double.parseDouble(fillOpacity.getText().replace("%", "")) / (double) 100;
				} catch(Exception e) {}
				fillOpacitySelector.show(fillOpacity, o);
			}
		});
		fillOpacitySelector.setSelectHandler(new SelectHandler(){
			@Override
			public void onSelect(double opacity) {
				fillOpacity.setText("%"+(int) Math.floor(opacity*100));
				GeometryType type = currentGraphic.getGeometry().getType();
				if( type == GeometryType.POLYGON ) {
					SimpleFillSymbol s = (SimpleFillSymbol) currentGraphic.getSymbol();
					Color c = s.getColor();
					c.setColor(c.getRed(), c.getGreen(), c.getBlue(), opacity);
				}
				update();
			}
		});	
		
		// FILL COLOR
		if( !GisClient.isIE7() && !GisClient.isIE8() ) { 
			fillColorInput.getElement().setAttribute("type", "color");
		}
		fillColorInput.addValueChangeHandler(new ValueChangeHandler<String>(){
			@Override
			public void onValueChange(ValueChangeEvent<String> event) {
				updateFillColor();
			}
		});
	}
	
	/**
	 * update the fill color of the current graphic
	 */
	private void updateFillColor() {
		if( !fillColorInput.getText().startsWith("#") ) {
			fillColorInput.setText("#"+fillColorInput.getText());
		}
		
		GeometryType type = currentGraphic.getGeometry().getType();
		Color newColor = Color.create(fillColorInput.getText());
		if( type == GeometryType.POLYGON ) {
			SimpleFillSymbol s = (SimpleFillSymbol) currentGraphic.getSymbol();
			Color c = s.getColor();
			c.setColor(newColor.getRed(), newColor.getGreen(), newColor.getRed(), c.getAlpha());
		}
		update();
	}
	
	/**
	 * update the point size of the selected graphic
	 */
	private void updatePointSize() {
		GeometryType type = currentGraphic.getGeometry().getType();
		if( type == GeometryType.POINT ) {
			SimpleMarkerSymbol s = (SimpleMarkerSymbol) currentGraphic.getSymbol();
			try {
				s.setSize(Integer.parseInt(pointSizeInput.getText()));
			} catch (Exception e) {}
		}
		update();
		updatePointOutline();
	}
	
	/**
	 * Update the marker color of the selected graphic.
	 */
	private void updatePointColor() {
		if( !pointColorInput.getText().startsWith("#") ) {
			pointColorInput.setText("#"+pointColorInput.getText());
		}
		
		GeometryType type = currentGraphic.getGeometry().getType();
		Color newColor = Color.create(pointColorInput.getText());
		if( type == GeometryType.POINT ) {
			SimpleMarkerSymbol s = (SimpleMarkerSymbol) currentGraphic.getSymbol();
			Color c = s.getColor();
			c.setColor(newColor.getRed(), newColor.getGreen(), newColor.getRed(), c.getAlpha());
		}
		update();
	}
	
	/**
	 * Update the line color of the selected graphic
	 */
	private void updateLineColor() {
		if( !lineColorInput.getText().startsWith("#") ) {
			lineColorInput.setText("#"+lineColorInput.getText());
		}
		
		GeometryType type = currentGraphic.getGeometry().getType();
		Color newColor = Color.create(lineColorInput.getText());
		if( type == GeometryType.POLYLINE ) {
			SimpleLineSymbol s = (SimpleLineSymbol) currentGraphic.getSymbol();
			Color c = s.getColor();
			c.setColor(newColor.getRed(), newColor.getGreen(), newColor.getRed(), c.getAlpha());
		} else if( type == GeometryType.POLYGON ) {
			SimpleFillSymbol s = (SimpleFillSymbol) currentGraphic.getSymbol();
			Color c = s.getOutline().getColor();
			c.setColor(newColor.getRed(), newColor.getGreen(), newColor.getRed(), c.getAlpha());
		}
		update();
	}
	
	/**
	 * update the outline of a selected marker
	 */
	private void updatePointOutline() {
		
		if( currentGraphic == null ) return;
		if( currentGraphic.getGeometry().getType() != GeometryType.POINT ) {
			AppManager.INSTANCE.getMap().getGraphics().remove(pointOutline);
			return;
		}
		
		if( pointOutline != null )
			AppManager.INSTANCE.getMap().getGraphics().remove(pointOutline);
		
		SimpleMarkerSymbol point = (SimpleMarkerSymbol) currentGraphic.getSymbol();
		
		SimpleMarkerSymbol.StyleType style = point.getStyle();
		if( style == SimpleMarkerSymbol.StyleType.STYLE_X || style == SimpleMarkerSymbol.StyleType.STYLE_CROSS ) {
			style = SimpleMarkerSymbol.StyleType.STYLE_CIRCLE;
		}
		
		Symbol s = SimpleMarkerSymbol.create(
				style, 
				point.getSize()+14, 
				SimpleLineSymbol.create(
						SimpleLineSymbol.StyleType.STYLE_DOT,
						Color.create(255, 0, 0, .7),
						2),
				Color.create(0, 0, 0, 0)
		);
		pointOutline = Graphic.create(currentGraphic.getGeometry(), s);
		AppManager.INSTANCE.getMap().getGraphics().add(pointOutline);
	}
	
	/**
	 * Get the Marker StyleType from a string
	 * 
	 * @param style - style string
	 * @return StyleType
	 */
	private SimpleMarkerSymbol.StyleType getMarkerStyle(String style) {
		for( int i = 0; i < SimpleMarkerSymbol.StyleType.values().length; i++) {
			if( SimpleMarkerSymbol.StyleType.values()[i].getValue().contentEquals(style) ) {
				return SimpleMarkerSymbol.StyleType.values()[i];
			}
		}
		return SimpleMarkerSymbol.StyleType.STYLE_DIAMOND;
	}
	
	/**
	 * Get the Line StyleType from a string
	 * 
	 * @param style - style string
	 * @return StyleType
	 */
	private SimpleLineSymbol.StyleType getLineStyle(String style) {
		for( int i = 0; i < SimpleLineSymbol.StyleType.values().length; i++) {
			if( SimpleLineSymbol.StyleType.values()[i].getValue().contentEquals(style) ) {
				return SimpleLineSymbol.StyleType.values()[i];
			}
		}
		return SimpleLineSymbol.StyleType.STYLE_SOLID;
	}
	
	/**
	 * update the current graphics symbology
	 */
	private void update() {
		if( currentGraphic.getGeometry().getType() == GeometryType.POINT ){
			DrawControl.INSTANCE.remove(currentGraphic);
			currentGraphic = Graphic.create(
					currentGraphic.getGeometry(),
					currentGraphic.getSymbol());
			DrawControl.INSTANCE.add(currentGraphic);
		} else {
			Symbol s = currentGraphic.getSymbol();
			currentGraphic.setSymbol(s);
		}
	}
	
	/**
	 * Add a user provided attribute to a feature
	 */
	private void addAttribute() {
		String name = newAttributeName.getText();
		String value = newAttributeValue.getText();
		if( name.length() == 0 || value.length() == 0 ) {
			return;
		}
		
		Attributes attrs = currentGraphic.getAttributes();
		if( attrs == null ) attrs = (Attributes) JavaScriptObject.createObject();
		
		attrs.setString(name, value);
		currentGraphic.setAttributes(attrs);
		
		// remove no attributes label if there
		if( attributes.getWidgetCount() == 1 ) {
			if( attributes.getWidget(0) instanceof HTML ) {
				attributes.clear();
			}
		}
		attributes.add(new CurrentAttributePanel(name, value));
		
		newAttributeName.setText("");
		newAttributeValue.setText("");
	}
	
	private void setShowingAttributesPanel(boolean showing) {
		if( showing ) {
			hidePanel.setStyleName("EditFeaturesPanel-attributes");
			styleButton.getElement().getStyle().setBackgroundColor("white");
			attributesButton.getElement().getStyle().setBackgroundColor("#f8f8f8");
		} else {
			hidePanel.setStyleName("EditFeaturesPanel-style");
			styleButton.getElement().getStyle().setBackgroundColor("#f8f8f8");
			attributesButton.getElement().getStyle().setBackgroundColor("white");

		}
	}
	
	/**
	 * start editing a graphic
	 * 
	 * @param g - graphic to edit
	 */
	public void editGraphic(Graphic g) {
		currentGraphic = g;
		updatePointOutline();
		GeometryType type = currentGraphic.getGeometry().getType();
		
		name.setText("");

		// get feature layer name
		collectionName.setText("");
		GraphicsLayer layer = currentGraphic.getLayer();
		LinkedList<DataLayer> layers = AppManager.INSTANCE.getDataLayers();
		for( DataLayer l: layers ) {
			if( l.getType() == DataLayerType.FeatureCollection ) {
				if( ((FeatureCollectionDataLayer) l).hasLayer(layer) ) {
					collectionName.setText("Collection: "+((FeatureCollectionDataLayer) l).getLabel());
				}
			}
		}
		
		
		// fill attribute table
		attributes.clear();
		Attributes attrs = currentGraphic.getAttributes();
		if( attrs != null ) {
			JsArrayString keys = currentGraphic.getAttributes().getKeys();
			for( int i = 0; i < keys.length(); i++ ) {
				String key = keys.get(i);
				
				if( key.contentEquals("name") ) {
					name.setText(getValue(key, attrs));
				} else {
					attributes.add(new CurrentAttributePanel(key, getValue(key, attrs)));
				}
			}
		}
		if( attributes.getWidgetCount() == 0 ) {
			attributes.add(new HTML("<div style='padding:5px;color:#444444'>This feature currently has no attributes.</div>"));
		}
		
		if( type == GeometryType.POINT ) {
			editPoint();
		} else if ( type == GeometryType.POLYLINE ) {
			editPolyline();
		} else if ( type == GeometryType.POLYGON ) {
			editPolygon();
		}
		
	}

	/**
	 * Start editing a point
	 */
	private void editPoint() {
		pointStylePanel.setVisible(true);
		lineStylePanel.setVisible(false);
		fillStylePanel.setVisible(false);
		
		try {
			SimpleMarkerSymbol symbol = (SimpleMarkerSymbol) currentGraphic.getSymbol();
			setBoxValue(pointStyleInput, symbol.getStyle().getValue());
			pointColorInput.setText(symbol.getColor().toHex());
			pointSizeInput.setText(symbol.getSize()+"");
		} catch(Exception e) {
			Debugger.INSTANCE.catchException(e, "EditFeaturePanel", "editPolygone");
		}
	}
	
	/**
	 * Start editing a line
	 */
	private void editPolyline() {
		pointStylePanel.setVisible(false);
		lineStylePanel.setVisible(true);
		fillStylePanel.setVisible(false);
		
		try {
			SimpleLineSymbol lineSymbol = (SimpleLineSymbol) currentGraphic.getSymbol();
			setBoxValue(lineWidthInput, lineSymbol.getWidth()+"");
			setBoxValue(lineStyleInput, lineSymbol.getStyle().getValue());
			int o = (int) Math.floor(lineSymbol.getColor().getAlpha() * (double) 100);
			lineOpacity.setText("%"+ o);
			lineColorInput.setText(lineSymbol.getColor().toHex());	
		} catch(Exception e) {
			Debugger.INSTANCE.catchException(e, "EditFeaturePanel", "editPolygone");
		}
	}
	
	/**
	 * Start editing a polygon
	 */
	private void editPolygon() {
		pointStylePanel.setVisible(false);
		lineStylePanel.setVisible(true);
		fillStylePanel.setVisible(true);
		
		try {
			SimpleFillSymbol symbol = (SimpleFillSymbol) currentGraphic.getSymbol();
			SimpleLineSymbol lineSymbol = symbol.getOutline();
			setBoxValue(lineWidthInput, lineSymbol.getWidth()+"");
			setBoxValue(lineStyleInput, lineSymbol.getStyle().getValue());
			
			int o = (int) Math.floor(lineSymbol.getColor().getAlpha() * (double) 100);
			lineOpacity.setText("%"+ Integer.toString(o));
			lineColorInput.setText(lineSymbol.getColor().toHex());
			
			o = (int) Math.floor(symbol.getColor().getAlpha() * (double) 100);
			fillOpacity.setText("%"+ Integer.toString(o));
			fillColorInput.setText(symbol.getColor().toHex());
			
		} catch(Exception e) {
			Debugger.INSTANCE.catchException(e, "EditFeaturePanel", "editPolygone");
		}
	}
	
	/**
	 * Set the selected index of a list box based on a given value
	 * 
	 * @param lb - listbox
	 * @param value - value to match
	 */
	private void setBoxValue(ListBox lb, String value) {
		for( int i = 0; i < lb.getItemCount(); i++) {
			if( lb.getValue(i).contentEquals(value) ) {
				lb.setSelectedIndex(i);
				return;
			}
		}
		lb.setSelectedIndex(0);
	}
	
	/**
	 * Get a javascript objects value
	 * 
	 * @param key - key of value
	 * @param jso - object
	 * @return String
	 */
	private native String getValue(String key, JavaScriptObject jso) /*-{
		if( jso[key] ) return jso[key]+"";
		return "";
	}-*/;

	/**
	 * Remove an attribute from a javascript object.
	 * 
	 * @param key - key of attribute to remove.
	 * @param attributes - object
	 */
	private static native void deleteAttribute(String key, JavaScriptObject attributes) /*-{
		if( attributes[key] != null ) delete attributes[key]
	}-*/;
	
	/**
	 * This panel controls and displays information about a graphics attributes
	 * 
	 * @author jrmerz
	 */
	private class CurrentAttributePanel extends Composite {
		private HorizontalPanel panel = new HorizontalPanel();
		private HTML name = new HTML();
		private Image remove = new Image(GadgetResources.INSTANCE.remove());
		private String value = "";
		
		/**
		 * Create a new row to display and control an attribute
		 * 
		 * @param key - attribute key
		 * @param val - attribute value
		 */
		public CurrentAttributePanel(String key, String val) {
			name.setHTML(key);
			this.value = val;
			
			remove.setStyleName("AnimatedRemoveIcon");
			remove.setTitle("Delete Attribute");
			remove.getElement().getStyle().setMargin(0, Unit.PX);
			remove.addClickHandler(new ClickHandler(){
				@Override
				public void onClick(ClickEvent event) {
					if( attributes.getWidgetCount() == 0 ) {
						attributes.add(new HTML("<div style='padding:5px;color:#444444'>This feature currently has no attributes.</div>"));
					}
					deleteAttribute(name.getText(), currentGraphic.getAttributes());
					removeFromParent();
				}
			});
			
			name.getElement().getStyle().setColor("#2278DA");
			name.getElement().getStyle().setCursor(Cursor.POINTER);
			name.addClickHandler(new ClickHandler(){
				@Override
				public void onClick(ClickEvent event) {
					infoPopup.setValue(value);
					infoPopup.setPopupPosition(
							name.getAbsoluteLeft()+name.getOffsetWidth()+10, 
							name.getAbsoluteTop());
					infoPopup.show();
				}
			});
			
			panel.setVerticalAlignment(HorizontalPanel.ALIGN_MIDDLE);
			panel.add(name);
			panel.add(remove);
			panel.setWidth("100%");
			panel.setCellWidth(remove, "20px");
			panel.setHorizontalAlignment(HorizontalPanel.ALIGN_RIGHT);
			panel.getElement().getStyle().setPadding(5, Unit.PX);
			
			initWidget(panel);
		}
	}
	
	/**
	 * Popup for displaying attribute values.
	 * 
	 * @author jrmerz
	 */
	private class InfoPopup extends PopupPanel {
		private HTML panel = new HTML();
		public InfoPopup() {
			super();
			panel.setWidth("200px");
			panel.setStyleName("GwtGisPopup");
			panel.getElement().getStyle().setPaddingBottom(10, Unit.PX);
			setAutoHideEnabled(true);
			setWidget(panel);
			getElement().getStyle().setZIndex(3000);
		}
		public void setValue(String val) {
			panel.setHTML(val);
		}
	}
	
	/**
	 * Get nice name for listbox
	 * 
	 * @param key - ugly name
	 * @return String
	 */
	private String getNiceName(String key) {
		if( key.contentEquals("dash") ) return "Dash";
		else if ( key.contentEquals("dashdot") ) return "Dash Dot";
		else if ( key.contentEquals("longdashdotdot") ) return "Long Dash Dot";
		else if ( key.contentEquals("dot") ) return "Dot";
		else if ( key.contentEquals("none") ) return "None";
		else if ( key.contentEquals("solid") ) return "Solid";
		else if ( key.contentEquals("backwarddiagonal") ) return "Backward Diagonal";
		else if ( key.contentEquals("cross") ) return "Cross";
		else if ( key.contentEquals("diagonalcross") ) return "Diagonal Cross";
		else if ( key.contentEquals("forwarddiagonal") ) return "Forward Diagonal";
		else if ( key.contentEquals("vertical") ) return "Vertical";
		else if ( key.contentEquals("horizontal") ) return "Horizontal";
		else if ( key.contentEquals("x") ) return "X";
		else if ( key.contentEquals("cross") ) return "Cross";
		else if ( key.contentEquals("circle") ) return "Circle";
		else if ( key.contentEquals("diamond") ) return "Diamond";
		else if ( key.contentEquals("square") ) return "Square";
		return key;
	}

	@Override
	public String getTitle() {
		return "Feature Style & Attributes";
	}

	@Override
	public Widget getBody() {
		return panel;
	}

	@Override
	public Widget getFooter() {
		return close;
	}

}
