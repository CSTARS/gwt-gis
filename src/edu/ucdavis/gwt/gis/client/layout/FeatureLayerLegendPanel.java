package edu.ucdavis.gwt.gis.client.layout;

import java.util.HashMap;
import java.util.Iterator;

import com.google.gwt.canvas.client.Canvas;
import com.google.gwt.dom.client.Style.Cursor;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TextBox;

import edu.ucdavis.cstars.client.symbol.Symbol;
import edu.ucdavis.gwt.gis.client.canvas.CanvasMap;
import edu.ucdavis.gwt.gis.client.layers.DataLayer;
import edu.ucdavis.gwt.gis.client.layers.FeatureCollectionDataLayer;
import edu.ucdavis.gwt.gis.client.layers.KmlDataLayer;
import edu.ucdavis.gwt.gis.client.layers.DataLayer.DataLayerType;

public class FeatureLayerLegendPanel extends Composite {
	
	private FlowPanel panel = new FlowPanel();
	private HashMap<Symbol, String> layerInfos = null;
	private DataLayer datalayer = null;
	
	public FeatureLayerLegendPanel(DataLayer dl) {
		initWidget(panel);
		datalayer = dl;
		
		if( datalayer.getType() == DataLayerType.KML ) {
			layerInfos = ((KmlDataLayer) datalayer).getLayerInfos();
		}
		
		renderItems();
	}	
		
	
	private void renderItems() {
		Iterator<Symbol> i = layerInfos.keySet().iterator();
		while( i.hasNext() ) {
			Symbol s = i.next();
			String name = layerInfos.get(s);
			
			CanvasMap canvasMap = FeatureCollectionDataLayer.renderLegendIcon(s);
			canvasMap.redraw();
			
			HorizontalPanel hp = new HorizontalPanel();
			hp.setWidth("100%");
			hp.setVerticalAlignment(HorizontalPanel.ALIGN_MIDDLE);
			hp.getElement().getStyle().setPadding(10, Unit.PX);
			
			QuickEditPanel editPanel = new QuickEditPanel(s, name);
			hp.add(editPanel);
			hp.setCellVerticalAlignment(editPanel, HorizontalPanel.ALIGN_MIDDLE);
			
			Canvas c = canvasMap.getCanvas();
			if( c != null ) hp.add(c);
			hp.setCellHorizontalAlignment(c, HorizontalPanel.ALIGN_RIGHT);
			panel.add(hp);
		}
	}
	
	private class QuickEditPanel extends Composite {
		private SimplePanel panel = new SimplePanel();
		private TextBox tb = new TextBox();
		private HTML html = new HTML();
		private Symbol symbol = null;
		private String name = "";
		
		public QuickEditPanel(Symbol symbol, String name) {
			setName(name);
			this.symbol = symbol;
			panel.add(html);
			
			html.addClickHandler(onClick);
			html.setTitle("Click to Edit");
			html.getElement().getStyle().setCursor(Cursor.POINTER);
			
			tb.setStyleName("editTextBox");
			
			tb.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					event.preventDefault();
					event.stopPropagation();
				}			
			});
			
			tb.addBlurHandler(new BlurHandler(){
				@Override
				public void onBlur(BlurEvent event) {
					unselect();
				}
			});
			
			tb.addKeyUpHandler(new KeyUpHandler(){
				@Override
				public void onKeyUp(KeyUpEvent event) {
					if( event.getNativeKeyCode() == KeyCodes.KEY_ENTER ) {
						unselect();
					}
				}
			});
			
			initWidget(panel);
		}
		
		private void unselect() {
			if(tb.getText().length() == 0) {
				panel.clear();
				panel.add(html);
				return;
			}
			
			setName(tb.getText());
			panel.clear();
			panel.add(html);
			
			if( datalayer.getType() == DataLayerType.KML ) {
				((KmlDataLayer) datalayer).updateInfoName(symbol, name);
			}
		}
		
		private ClickHandler onClick = new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				panel.clear();
				tb.setText("");
				panel.add(tb);
				tb.setFocus(true);
				tb.selectAll();
			}
		};
		
		private void setName(String text) {
			name = text;
			html.setHTML("<span style='color:#2278da;font-size:14px'>"+text+"</span>");
		}
		
	}
}
