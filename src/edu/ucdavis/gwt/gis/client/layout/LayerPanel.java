package edu.ucdavis.gwt.gis.client.layout;

import java.util.HashMap;


import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style.Cursor;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.dom.client.Style.Visibility;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

import edu.ucdavis.cstars.client.Error;
import edu.ucdavis.cstars.client.event.MapZoomEndHandler;
import edu.ucdavis.cstars.client.event.UpdateEndHandler;
import edu.ucdavis.cstars.client.event.UpdateStartHandler;
import edu.ucdavis.cstars.client.geometry.Extent;
import edu.ucdavis.cstars.client.geometry.Point;
import edu.ucdavis.cstars.client.layers.KMLLayer;
import edu.ucdavis.cstars.client.layers.LayerInfo;
import edu.ucdavis.cstars.client.restful.LegendInfo;
import edu.ucdavis.cstars.client.restful.RestfulDocumentInfo;
import edu.ucdavis.cstars.client.symbol.Symbol;
import edu.ucdavis.gwt.gis.client.AppManager;
import edu.ucdavis.gwt.gis.client.GisClient;
import edu.ucdavis.gwt.gis.client.auth.DomainAccess;
import edu.ucdavis.gwt.gis.client.dandd.EventPanel;
import edu.ucdavis.gwt.gis.client.extras.EsriPreview;
import edu.ucdavis.gwt.gis.client.layers.DataLayer;
import edu.ucdavis.gwt.gis.client.layers.ImageServerDataLayer;
import edu.ucdavis.gwt.gis.client.layers.KmlDataLayer;
import edu.ucdavis.gwt.gis.client.layers.MapServerDataLayer;
import edu.ucdavis.gwt.gis.client.layers.DataLayer.DataLayerLoadHandler;
import edu.ucdavis.gwt.gis.client.layers.DataLayer.DataLayerType;
import edu.ucdavis.gwt.gis.client.resources.GadgetResources;

/**
 * The right hand panels that contains all layer information.  Includes btn for
 * popup as well as legendpanel (expand control)
 * 
 * @author jrmerz
 *
 */
public class LayerPanel extends SimplePanel implements UpdateStartHandler, UpdateEndHandler {

	private static LayerPanelUiBinder uiBinder = GWT.create(LayerPanelUiBinder.class);
	interface LayerPanelUiBinder extends UiBinder<Widget, LayerPanel> {}

	private boolean open = false;

	@UiField EventPanel dragIcon;
	@UiField Anchor expandIcon;
	@UiField Anchor layerMenuBtn;
	@UiField Anchor visibilityCheckBox;
	
	@UiField HTML layerName;
	@UiField SimplePanel content;
	@UiField VerticalPanel contentList;
	
	@UiField Element header;

	
	private HTML noVisibleLegendsPanel = new HTML("<div style='padding:10px;color:#888888'>No legend data provided at this scale.</div>");
	private DataLayer datalayer = null;
	
	private boolean isError = false;
	private LayerPopup popup = null;
	private OpacitySelector opacitySelector = new OpacitySelector();
	
	private boolean loading = false;
	private boolean requestError = false;
	
	public OpacitySelector getOpacitySelector() {
		return this.opacitySelector;
	}
	
	public LayerPanel(DataLayer dl) {
		super();
		add(uiBinder.createAndBindUi(this));
		datalayer = dl;
		
		// IE hacks
		if( GisClient.isIE7() || GisClient.isIE8() ) {
			header.getStyle().setProperty("borderTop", "1px solid #cccccc");
			header.getStyle().setProperty("borderBottom", "1px solid #aaaaaa");
		}

		updateVisibilityCheckBox();
		
		HTML html = new HTML("<i class='icon-resize-vertical'></i>");
		dragIcon.add(html);
		layerMenuBtn.setHTML("<i class='icon-reorder'></i>");
		expandIcon.setHTML("<i class='icon-chevron-right'></i>");

		popup = new LayerPopup(datalayer);
		initIconEventHandlers();
		
		// set the title
		setLayerTitle(datalayer.getLabel());
		
		// once the layer is loaded, render the legends
		datalayer.addLoadHandler(new DataLayerLoadHandler(){
			@Override
			public void onDataLoaded(DataLayer dataLayer) {
				if( datalayer.getType() == DataLayerType.MapServer ) loadMapServerDataLayer();
				else if (datalayer.getType() == DataLayerType.ImageServer ) loadImageServerDataLayer();
				else if (datalayer.getType() == DataLayerType.KML ) renderKmlLegends();
				Timer t = new Timer() {
					@Override
					public void run() {
						if( datalayer.showLegendOnLoad() ) expand(true);
					}
				};
				t.schedule(1000);
			}
		});
		
		if( datalayer.getType() == DataLayerType.MapServer ) {
		    MapServerDataLayer msdl = (MapServerDataLayer) datalayer;
		    msdl.getMapLayer().addUpdateStartHandler(this);
		    msdl.getMapLayer().addUpdateEndHandler(this);
		}
		
		// after a zoom action, update the visibility icon
		AppManager.INSTANCE.getMap().addZoomEndHandler(new MapZoomEndHandler(){
			@Override
			public void onZoomEnd(Extent extent, float zoomFactor, Point anchor, int level) {
				updateVisibility(Extent.getScale(AppManager.INSTANCE.getMap()));
			}
		});
	}
	
	public EventPanel getDragIcon() {
	    return dragIcon;
	}
	
	public void setLayerTitle(String title) {
		layerName.setHTML(title);
		layerName.setTitle(title);
	}
	
	private void loadMapServerDataLayer() {
		MapServerDataLayer msdl = (MapServerDataLayer) datalayer;
		if( msdl.errorLoadingLegend() ) setErrorLoadingLegend(); 
		else renderMapServerLegends();
		
		RestfulDocumentInfo docInfo = msdl.getDocInfo();
		if( docInfo == null || msdl.errorLoadingData() || msdl.hasError() ) {
			setErrorLoadingLayer(msdl); 
		} else {
			popup.setDocInfo(docInfo);
		}
	}
	
	private void loadImageServerDataLayer() {
		ImageServerDataLayer isdl = (ImageServerDataLayer) datalayer;
		
		if( isdl.errorLoadingData() || isdl.hasError() ) {
			setErrorLoadingLayer(isdl); 
		} else {
			popup.setImageServerInfo(isdl);
			VerticalPanel vp = new VerticalPanel();
			vp.setWidth("100%");
			vp.add(new HTML("<div class='LayerLegendPanelTitle' style='padding:5px 0 0 5px'>Imagery Service</div>"));
			EsriPreview preview = new EsriPreview(isdl.getUrl(), 200, 200);
			preview.noCss();
			vp.add(preview);
			vp.setCellHorizontalAlignment(preview, HorizontalPanel.ALIGN_RIGHT);
			vp.getElement().getStyle().setPaddingBottom(10, Unit.PX);
			contentList.add(vp);
		}
	}
	
	// update the sub layer checkboxes
	public void updateCheckBoxs() {
		if( !isError ) {
			for( int i = 0; i < contentList.getWidgetCount(); i++ ) {
				Widget w = contentList.getWidget(i);
				if( w instanceof LegendPanel ) {
					LegendPanel lp = (LegendPanel) w;
					lp.updateCheckBox();
				}
			}
		}
	}
	
	// based on the current map scale, should we be showing the sublayers
	private void updateVisibility(double scale) {
		if( !isError ) {
			int count = 0;
			for( int i = 0; i < contentList.getWidgetCount(); i++ ) {
				Widget w = contentList.getWidget(i);
				if( w instanceof LegendPanel ) {
					LegendPanel lp = (LegendPanel) w;
					lp.setCurrentScale(scale);
					if( lp.updateVisibility() ) count++;
				}
			}
			
	
			if( count > 0 ) noVisibleLegendsPanel.setVisible(false);
			else noVisibleLegendsPanel.setVisible(true);
			
			if( isOpen() ) updateContentSize();
		}
	}
	
	public void clearAndUpdateLegends() {
		if( datalayer.getType() == DataLayerType.MapServer ) {
			contentList.clear();
			renderMapServerLegends();
		}
	}
	
	private void renderKmlLegends() {
		KMLLayer layer = ((KmlDataLayer) datalayer).getMapLayer();

		if( ((KmlDataLayer) datalayer).isError() ) {
			setErrorLoadingKml(((KmlDataLayer) datalayer).getErrorMessage());
		} else {
			// set popup
			popup.setKmlInfo(layer.getUrl());

			HashMap<Symbol, String> layerInfos = ((KmlDataLayer) datalayer).getLayerInfos();
			if( !layerInfos.isEmpty() ) contentList.add(new FeatureLayerLegendPanel(datalayer));
			else contentList.add(new HTML("<div style='padding:10px;color:#888888'>No graphics to display for KML file</div>"));
		}
	}

	private void renderMapServerLegends() {
		MapServerDataLayer msdl = (MapServerDataLayer) datalayer;
		JsArray<LayerInfo> layers = msdl.getLegendInfo();
		for( int i = 0; i < layers.length(); i++ ){
			
			boolean crunch = false;
			if( msdl.legendIsGradient() ) crunch = true;
			LegendPanel legendPanel = new LegendPanel(msdl, crunch, layers.get(i).getMinScale(), layers.get(i).getMaxScale());

			boolean visible = false;
			if( msdl.getLayersInfo().length() > i ) {
				visible = msdl.getLayersInfo().get(i).hasDefaultVisibility();
			}
			
			if ( layers.length() > 1 ) {
				legendPanel.setLayerTitle(layers.get(i), visible);
			} else {
				// TODO: single layers that are default off may not be able to be turned on!
				legendPanel.setLayerTitle(layers.get(i).getName());
			}
			
			if( layers.get(i).hasLegendInfo() ) {
				JsArray<LegendInfo> legendInfo = layers.get(i).getLegendInfo();
				for( int j = 0; j < legendInfo.length(); j++ ){
					LegendInfo li = legendInfo.get(j);
					legendPanel.addLayerElement(li,layers.get(i), j);
				}
			}
			
			legendPanel.setSubLayerData( msdl.getSubLayerData() );
			
			contentList.add(legendPanel);
		}
		
		// add no visible legends panel
		noVisibleLegendsPanel.setVisible(false);
		contentList.add(noVisibleLegendsPanel);
		
		updateVisibility(Extent.getScale(AppManager.INSTANCE.getMap()));
	}

	
	public boolean isOpen() {
		return open;
	}
	
	// ui binder doesn't seem to be setting this....
	private void setIconStyle(Image img){
		img.getElement().getStyle().setCursor(Cursor.POINTER);
		img.getElement().getStyle().setProperty("margin", "0px 1px 0px 1px");
	}
	
	public String getLayerId() {
		return datalayer.getId();
	}

	private void initIconEventHandlers() {
		
		expandIcon.addClickHandler(new ClickHandler(){
			@Override
			public void onClick(ClickEvent event) {
				cancelEventProp(event);
				expand(!open);
			}
		});
		
		layerName.addClickHandler(new ClickHandler(){
            @Override
            public void onClick(ClickEvent event) {
                cancelEventProp(event);
                expand(!open);
            }		
		});
		
		layerMenuBtn.addClickHandler(new ClickHandler(){
			@Override
			public void onClick(ClickEvent event) {
				cancelEventProp(event);
				popup.show();
			}
		});

		visibilityCheckBox.addClickHandler(new ClickHandler(){
			@Override
			public void onClick(ClickEvent event) {
				// if the btn-info style is set, the layer is active.
				boolean show = !visibilityCheckBox.getStyleName().contains("on");
				datalayer.setVisible(show);
				updateVisibilityCheckBox();
			}
		});
		
	}
	
	public void expand(boolean expand){
		if( expand ) {
			content.setHeight(contentList.getOffsetHeight()+"px");
			content.getElement().getStyle().setOpacity(1);
			if( GisClient.isIE7() || GisClient.isIE8() ) {
				expandIcon.setHTML("<i class='icon-chevron-down'></i>");
			} else {
				expandIcon.addStyleName("open");
			}
		} else {
			content.setHeight("0px");
			content.getElement().getStyle().setOpacity(0);
			if( GisClient.isIE7() || GisClient.isIE8() ) {
				expandIcon.setHTML("<i class='icon-chevron-down'></i>");
			} else {
				expandIcon.removeStyleName("open");
			}
		}
		open = expand;
	}
	
	private String niceOpacity(double o){
		int i = (int) (o*100);
		if( i == 100 ) return "<span style='margin-right:-5px'>"+i+"%</span>";
		return i+"%";
	}
	
	private void cancelEventProp(ClickEvent event){
		event.stopPropagation();
		event.preventDefault();
	}
	
	private void setErrorLoadingLegend() {
		isError = true;
		contentList.clear();
		contentList.add(new HTML("<div style='color:red;text-align:center;padding:10px;'>Failed to load legend data.<br />The ArcGIS Server may not have restful legend support.</div>"));
	}
	
	private void setErrorLoadingLayer(ImageServerDataLayer isdl) {
		setErrorLoadingLayer(isdl.hasError(), isdl.getError());
	}
	
	private void setErrorLoadingLayer(MapServerDataLayer msdl) {
		setErrorLoadingLayer(msdl.hasError(), msdl.getError());
	}
	
	private void setErrorLoadingLayer(boolean hasError, Error error) {
		isError = true;
		contentList.clear();
		
		if( hasError ) {
			if ( DomainAccess.isAuthErrorCode(error.getCode()) ) {
				contentList.add(new HTML("<div style='color:red;padding:10px;'>" +
						error.getMessage()+".  You may need to generate a new token to access this service."));		
				HTML html = new HTML("<div style='padding:10px;'>Generate/Set Token</div>");
				html.getElement().getStyle().setColor("#2278DA");
				html.getElement().getStyle().setCursor(Cursor.POINTER);
				html.addClickHandler(new ClickHandler(){
					@Override
					public void onClick(ClickEvent event) {
						LayerPopup.advancedConfig.editLayer(datalayer);
					}
				});
				contentList.add(html);
				//setDisabled(true);
			} else {
				contentList.add(new HTML("<div style='color:red;text-align:center;padding:10px;'>Failed to load layer.</div>"));
			}
		} else {
			contentList.add(new HTML("<div style='color:red;text-align:center;padding:10px;'>Failed to load layer.</div>"));
		}
		
		popup.setErrorLoading();
		visibilityCheckBox.setEnabled(false);
		visibilityCheckBox.getElement().getStyle().setVisibility(Visibility.HIDDEN);
		header.setClassName(GadgetResources.INSTANCE.css().legendPanelHeaderError());
	}
	
	private void setErrorLoadingKml(String msg) {
		isError = true;
		contentList.clear();
		if( msg.isEmpty() ) {
			contentList.add(new HTML("<div style='color:red;text-align:center;padding:10px;'>Failed to load layer.</div>"));
		} else {
			String[] parts = msg.split(" ");
			for( int i = 0; i < parts.length; i++ ){
				if( parts[i].startsWith("(http://") ) {
					 parts[i] = "<a href='"+parts[i].replace("(", "").replace(")", "")+"' target='_blank'>(Link)</a>";
				} else if ( parts[i].startsWith("http://") ) {
					 parts[i] = "<a href='"+parts[i]+"' target='_blank'>Link</a>";
				} else if ( parts[i].startsWith("www\\.") ) {
					 parts[i] = "<a href='http://"+parts[i]+"' target='_blank'>Link</a>";
				}
			}
			msg = "";
			for( String s: parts ) msg += s+" ";
			contentList.add(new HTML("<div style='color:red;text-align:center;padding:10px;'>"+msg+"</div>"));
		}
		popup.setErrorLoadingKml(msg);
		visibilityCheckBox.setEnabled(false);
		visibilityCheckBox.getElement().getStyle().setVisibility(Visibility.HIDDEN);
		header.setClassName(GadgetResources.INSTANCE.css().legendPanelHeaderError());
	}
	
	public void updateContentSize() {
		content.setHeight((contentList.getOffsetHeight()+5)+"px");
	}

	public void updateVisibilityCheckBox() {
	    visibilityCheckBox.setWidth("15px");
	    
		if( datalayer.isVisible() ) {
		    if( this.requestError ) {
	            visibilityCheckBox.setTitle("Failed to load image");
	            visibilityCheckBox.setHTML("<i class='icon-exclamation' style='color:red'></i>");
	            return;
		    } else if( this.loading ) {
		        visibilityCheckBox.setTitle("Loading layer...");
		        visibilityCheckBox.setHTML("<i class='icon-refresh icon-spin'></i>");
		    } else {
		        visibilityCheckBox.setTitle("Show layer");
		        visibilityCheckBox.setHTML("<i class='icon-eye-open'></i>");
		    }
		    
			visibilityCheckBox.addStyleName("on");
		} else {
		    visibilityCheckBox.setTitle("Hide layer");
			visibilityCheckBox.setHTML("<i class='icon-eye-close'></i>");
			visibilityCheckBox.removeStyleName("on");
		}
		
	}

    @Override
    public void onUpdateStart() {
        loading = true;
        requestError = false;
        updateVisibilityCheckBox();
    }

    @Override
    public void onError(Error error) {
        requestError = true;
        loading = false;
        updateVisibilityCheckBox();
    }

    @Override
    public void onUpdateEnd() {
        loading = false;
        requestError = false;
        updateVisibilityCheckBox();
    }
}
