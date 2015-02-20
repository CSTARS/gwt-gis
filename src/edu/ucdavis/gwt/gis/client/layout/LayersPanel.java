package edu.ucdavis.gwt.gis.client.layout;

import java.util.LinkedList;

import com.google.gwt.dom.client.Style.Overflow;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

import edu.ucdavis.cstars.client.MapWidget;
import edu.ucdavis.cstars.client.layers.Layer;
import edu.ucdavis.gwt.gis.client.AppManager;
import edu.ucdavis.gwt.gis.client.dandd.DragAndDropContainer;
import edu.ucdavis.gwt.gis.client.dandd.EventPanel;
import edu.ucdavis.gwt.gis.client.layers.DataLayer;
import edu.ucdavis.gwt.gis.client.layers.DataLayer.DataLayerLoadHandler;
import edu.ucdavis.gwt.gis.client.resources.GadgetResources;

public class LayersPanel extends Composite {
	
	private HTML loading = new HTML("Loading...");
	private DragDropPanel panel = new DragDropPanel();
	
	public int WIDTH = 275;

	private ScrollPanel sp = new ScrollPanel();
	
	private boolean isIE = false;
	
	private LinkedList<LoadingPanel> loadingPanels = new LinkedList<LoadingPanel>();
	
	public LayersPanel() {
		panel.getElement().setId("filters");
		panel.setWidth("100%");
		panel.setHeight("100%");
		
		panel.setStyleName("LayersPanel");

		sp.setStyleName("LegendGroupPanel");
		sp.setWidth(WIDTH+"px");
		sp.setHeight("100%");
		sp.add(panel);
		
		initWidget(sp);
	
		loading.getElement().getStyle().setMargin(5, Unit.PX);
	}
	
	public Widget createExpandAll() {
		AbsolutePanel sp = new AbsolutePanel();
		sp.getElement().getStyle().setOverflow(Overflow.VISIBLE);
		
		HorizontalPanel hp = new HorizontalPanel();
		HTML expand = new HTML("&nbsp;");
		expand.setStyleName(GadgetResources.INSTANCE.css().expandButton());
		expand.addClickHandler(new ClickHandler(){
			@Override
			public void onClick(ClickEvent event) {
				panel.expandAll();
			}
		});
		expand.addMouseOverHandler(new MouseOverHandler(){
			@Override
			public void onMouseOver(MouseOverEvent event) {
				((Label) event.getSource()).setStyleName(GadgetResources.INSTANCE.css().expandButtonMouseover());
			}
		});
		expand.addMouseOutHandler(new MouseOutHandler(){
			@Override
			public void onMouseOut(MouseOutEvent event) {
				((Label) event.getSource()).setStyleName(GadgetResources.INSTANCE.css().expandButton());
			}
		});
		
		
		HTML shrink = new HTML("&nbsp;");
		shrink.setStyleName(GadgetResources.INSTANCE.css().collapseButton());
		shrink.addClickHandler(new ClickHandler(){
			@Override
			public void onClick(ClickEvent event) {
				panel.shrinkAll();
			}
		});
		shrink.addMouseOverHandler(new MouseOverHandler(){
			@Override
			public void onMouseOver(MouseOverEvent event) {
				((Label) event.getSource()).setStyleName(GadgetResources.INSTANCE.css().collapseButtonMouseover());
			}
		});
		shrink.addMouseOutHandler(new MouseOutHandler(){
			@Override
			public void onMouseOut(MouseOutEvent event) {
				((Label) event.getSource()).setStyleName(GadgetResources.INSTANCE.css().collapseButton());
			}
		});
		
		hp.add(shrink);
		hp.add(expand);
	
		hp.setStyleName("expandCollapseBar");
		
		sp.add(hp, 1, 0);
		
		return sp;
	}
	
	public void setHeight( int height ){
		setHeight(height+"px");
		sp.setHeight(height+"PX");
		//HEIGHT = height;
	}
	
	public void add(DataLayer layer) {
		// add loading panel that is replaced when dl is loaded
		LoadingPanel lp = new LoadingPanel(layer);
		panel.add(lp);
		loadingPanels.add(lp);
		
		layer.addLoadHandler(new DataLayerLoadHandler(){
			@Override
			public void onDataLoaded(DataLayer dataLayer) {
				
				LayerPanel layerPanel = new LayerPanel(dataLayer);
				dataLayer.setLayerPanel(layerPanel);
				
				// swap out loading panel
				for( LoadingPanel lp: loadingPanels ) {
					if( lp.getDataLayer() == dataLayer ) {
						int index = panel.getIndex(lp);
						panel.insert(layerPanel, index+1);
						panel.remove(lp);
						loadingPanels.remove(lp);
						break;
					}
				}
				
				// update map
				updateMap();
			}
		});
	}
	
	public void remove(DataLayer layer) {
		panel.remove(layer.getLayerPanel());
		updateMap();
	}
	
	public void updateMap() {
		panel.update();
	}
	
	public void setExpanded(boolean expand){
		if( expand ) panel.expandAll();
		else panel.shrinkAll();
	}
	
	private class LoadingPanel extends EventPanel {
		private SimplePanel panel = new SimplePanel();
		private HorizontalPanel hp = new HorizontalPanel();
		private DataLayer dl = null;
		
		public LoadingPanel(DataLayer layer) {
			dl = layer;
			panel.setStyleName("LayerLoadingPanel");
			
			hp.setSpacing(5);
			hp.setVerticalAlignment(HorizontalPanel.ALIGN_MIDDLE);
			hp.add(new Image(GadgetResources.INSTANCE.loading()));
			hp.add(new HTML("&nbsp;&nbsp;&nbsp;"+layer.getLabel()));
			
			panel.add(hp);
			setDisabled(true);
			add(panel);
		}
		
		public DataLayer getDataLayer() {
			return dl;
		}
		
	}
	
	private class DragDropPanel extends DragAndDropContainer {
			
			public DragDropPanel() {
				setOrderChangeHandler(new OrderChangeHandler(){
					@Override
					public void onOrderChange() {
						update();
					}
				});
			}
			
			public void shrinkAll() {
				for( Widget w: getWidgets() ){
					if( w instanceof LayerPanel ) {
						LayerPanel lp = (LayerPanel) w;
						if( lp.isOpen() ) lp.expand(false);
					}
				}
			}
			
			public void expandAll() {
				for( Widget w: getWidgets() ){
					if( w instanceof LayerPanel ) {
						LayerPanel lp = (LayerPanel) w;
						if( !lp.isOpen() ) lp.expand(true);
					}
				}
			}

			public void update() {
				int count = 0;
				for( int i = getWidgets().size()-1; i >= 0; i-- ) {
					if( getWidgets().get(i) instanceof LayerPanel ) {
						LayerPanel lp = (LayerPanel) getWidgets().get(i);
						setHazardOrder(lp.getLayerId(), count);
						count++;
					}
				}
			}
			
			private void setHazardOrder(String id, int index) {
				MapWidget map = AppManager.INSTANCE.getMap();
				Layer l = map.getLayer(id);
				if( l != null ) map.reorderLayer(l, index+1);
			}
			
	}

}
