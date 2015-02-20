package edu.ucdavis.gwt.gis.client.layout;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

import edu.ucdavis.cstars.client.restful.RestfulDocumentInfo;
import edu.ucdavis.gwt.gis.client.GisClient;
import edu.ucdavis.gwt.gis.client.AppManager;
import edu.ucdavis.gwt.gis.client.config.HelpTopicConfig;
import edu.ucdavis.gwt.gis.client.extras.AdvancedLayerConfig;
import edu.ucdavis.gwt.gis.client.help.LayersHelpMenu;
import edu.ucdavis.gwt.gis.client.layers.DataLayer;
import edu.ucdavis.gwt.gis.client.layers.ImageServerDataLayer;
import edu.ucdavis.gwt.gis.client.layers.KmlDataLayer;
import edu.ucdavis.gwt.gis.client.layers.MapServerDataLayer;
import edu.ucdavis.gwt.gis.client.layers.DataLayer.DataLayerType;
import edu.ucdavis.gwt.gis.client.layout.modal.BootstrapModalLayout;
import edu.ucdavis.gwt.gis.client.resources.GadgetResources;

public class LayerPopup extends BootstrapModalLayout {
	
	public static AdvancedLayerConfig advancedConfig = new AdvancedLayerConfig();
	
	private SimplePanel panel = new SimplePanel();	
	private VerticalPanel vp = new VerticalPanel();
	private String name = "";
	private String url = "";
	private DescriptionPanel descriptionPanel = null;
	private DataLayer datalayer = null;
	
	private Anchor closeBtn = new Anchor("Close");
	
	public LayerPopup(DataLayer dl) {
		this.name = dl.getLabel();
		this.url = dl.getUrl().replaceAll("\\?.*", "");
		datalayer = dl;
		
		panel.add(vp);
		vp.setWidth("100%");
		
		closeBtn.addStyleName("btn");
		closeBtn.addClickHandler(new ClickHandler(){
			@Override
			public void onClick(ClickEvent event) {
				hide();
			}
		});

	}
	
	public DataLayer getDataLayer() {
		return datalayer;
	}
	
	public void setImageServerInfo(ImageServerDataLayer isdl) {
		if( isdl == null ) {
			setErrorLoading();
			return;
		} else if ( isdl.getInfo() == null ) {
			setErrorLoading();
			return;
		}
		
		vp.add(getInfo(isdl.getInfo().getDescription()));
		
		// add link url
		HorizontalPanel hp = new HorizontalPanel();
		hp.setStyleName("menuPopupItem");
		hp.getElement().getStyle().setMarginLeft(10, Unit.PX);
		hp.add(new Image(GadgetResources.INSTANCE.esriglobe()));
		Anchor a = new Anchor("ArcGIS Service Description", true, url, "_blank");
		a.setStyleName(GadgetResources.INSTANCE.css().legendPopupLink());
		hp.add(a);
		vp.add(hp);
		
		vp.add(getOpacityButton());
		
		// must have specified a geometry server
		if( !AppManager.INSTANCE.getConfig().getGeometryServer().isEmpty() ) {
			vp.add(getZoomToButton());
		}
		
		vp.add(getAdvancedButton());
		
		if( GisClient.getLayerMenuCreateHandler() != null ) {
			GisClient.getLayerMenuCreateHandler().onCreate(vp, datalayer);
		}
		
		vp.add(getRemoveButton());
		
		vp.add(getHelpButton());
	}

	public void setKmlInfo(String url) {
		vp.add(getInfo("A KML file<br /><br />NOTE:  Feature layers, such as KML layers, will always remain on" +
				"top of the map, reguardless of their order in this layer panel."));
		
		// add link url
		HorizontalPanel hp = new HorizontalPanel();
		hp.setStyleName("menuPopupItem");
		hp.getElement().getStyle().setMarginLeft(10, Unit.PX);
		hp.add(new Image(GadgetResources.INSTANCE.metadata()));
		Anchor a = new Anchor("Link to file", true, url, "_blank");
		a.setStyleName(GadgetResources.INSTANCE.css().legendPopupLink());
		hp.add(a);
		vp.add(hp);		
		
		vp.add(getOpacityButton());
		
		// must have specified a geometry server
		if( !AppManager.INSTANCE.getConfig().getGeometryServer().isEmpty() ) {
			vp.add(getZoomToButton());
		}
		
		vp.add(getRemoveButton());
		
		vp.add(getHelpButton());
	}
	
	public void setErrorLoading() {
		//vp.clear();
		vp.add(new HTML("<div style='padding: 5px; color: red'>Error loading layer information. You can view the given ArcGIS Server URL " +
				"<a href='"+url+"' target='_blank' >here.</a></div>"));
		
		vp.add(getRemoveButton());
		
		vp.add(getHelpButton());
	}
	
	public void setErrorLoadingKml(String msg) {
		//vp.clear();
		if( msg.isEmpty() ) {
			vp.add(new HTML("<div style='padding: 5px; color: red'>Error loading KML file. You can view given URL " +
					"<a href='"+url+"' target='_blank' >here.</a></div>"));
		} else {
			vp.add(new HTML("<div style='padding: 5px; color: red'>Error loading KML file. "+msg+".  You can view given URL " +
					"<a href='"+url+"' target='_blank' >here.</a></div>"));
		}
		
		
		vp.add(getRemoveButton());
		
		vp.add(getHelpButton());
	}
	
	public void setDocInfo(RestfulDocumentInfo docInfo) {
		if( docInfo == null ) {
			setErrorLoading();
			return;
		}
		
		vp.add(getInfo(docInfo.getComments()));
		
		// add link url
		Anchor a = new Anchor("<i class='icon-globe menu-icon'></i>&nbsp;&nbsp;ArcGIS Service Description", true, url, "_blank");
		a.setStyleName("menu-link");
		vp.add(a);
		
		vp.add(getContact(docInfo.getAuthor()));
		
		if( datalayer.getType() == DataLayerType.MapServer ) vp.add(new HTML("<div style='color:#444444;font-size:14px;border-bottom:1px solid #cccccc;padding:3px;margin:5px'>Actions & Settings</div>"));
		
		vp.add(getOpacityButton());
		
		// must have specified a geometry server
		if( !AppManager.INSTANCE.getConfig().getGeometryServer().isEmpty() ) {
			vp.add(getZoomToButton());
		}
		
		vp.add(getAdvancedButton());
		
		if( GisClient.getLayerMenuCreateHandler() != null ) {
			GisClient.getLayerMenuCreateHandler().onCreate(vp, datalayer);
		}
		
		vp.add(getRemoveButton());
		
		vp.add(getHelpButton());
	}
	
	private Widget getInfo(String comments) {
		try {
			String[] parts = comments.split(" ");
	
			for( int i = 0; i < parts.length; i++ ){
				if( parts[i].startsWith("(http://") ) {
					 parts[i] = "<a href='"+parts[i].replace("(", "").replace(")", "")+"' target='_blank'>(Link)</a>";
				} else if ( parts[i].startsWith("http://") ) {
					 parts[i] = "<a href='"+parts[i]+"' target='_blank'>Link</a>";
				} else if ( parts[i].startsWith("www\\.") ) {
					 parts[i] = "<a href='http://"+parts[i]+"' target='_blank'>Link</a>";
				}
			}
			
			String info = "";
			for( String s: parts ) info += s+" ";
			descriptionPanel = new DescriptionPanel(info);
			return descriptionPanel;
		} catch (Exception e) {}
		
		return new HTML("<div style='padding: 5px'>Currently no metadata available</div>");
	}
	
	private Widget getContact(String author) {
		String[] parts = author.split(" ");
		String email = "";
		for( int i = 0; i < parts.length; i++ ){
			if( parts[i].contains("mailto:") ) {
				email = parts[i];
				break;
			}
		}
		

		if( email.length() > 0 ) {
			Anchor a = new Anchor("<i class='icon-envelope menu-icon'></i> Contact - "+author.replaceAll("mailto:",""), true, email);
			a.setStyleName("menu-link");
			return a;
		} else if( author.length() > 0 ) {
			return new HTML("<div style='padding: 0 0 5px 15px'><span style='color:#444444;font-weight:bold'>Author: </span>" +
				"<span style='color:#666666'>"+ author+"</span></div>");
		}
		
		return new HTML("<div style='padding: 0 0 5px 15px; color: #666666'>No author information provided</div>");
	}
	
	public Widget getHelpButton() {
	    // see if we have a flagged help item for the menu
	    JsArray<HelpTopicConfig> topics = AppManager.INSTANCE.getConfig().getHelpTopics();
        for( int i = 0; i < topics.length(); i++ ) {
            if( topics.get(i).isLayerMenuHelp() ) {
                Anchor a = new Anchor("<i class='icon-question menu-icon'></i> Help", true, topics.get(i).getUrl(), "_blank");
                a.setStyleName("menu-link");
                return a;
            }
        }
        
        // no layer menu help
        return new SimplePanel();
	}

	public Widget getAdvancedButton() {
		Anchor a = new Anchor("<i class='icon-cogs menu-icon'></i> Advanced Layer Settings", true);
		a.setStyleName("menu-link");
		a.addClickHandler(new ClickHandler(){
			@Override
			public void onClick(ClickEvent event) {
				advancedConfig.editLayer(LayerPopup.this);
			}	
		});
		
		return a;
	}
	
	public Widget getOpacityButton() {
		HorizontalPanel panel = new HorizontalPanel();
		
		panel.add(new HTML("<div class='menu-link' style='cursor:pointer'><i class='icon-th-large menu-icon'></i>&nbsp;&nbsp;Opacity</div>"));
		
		
		final ListBox list = new ListBox();
		list.setWidth("70px");
		list.getElement().getStyle().setProperty("margin", "0 0 0 15px");
		for( int i = 0; i <= 10; i++ ) {
			list.addItem((i*10)+"%", (i*10)+"");

			if( ((double) i / 10) == datalayer.getOpacity() ) list.setSelectedIndex(i);
		}
		
		list.addChangeHandler(new ChangeHandler(){
			@Override
			public void onChange(ChangeEvent event) {
				String val = list.getValue(list.getSelectedIndex());
				datalayer.setOpacity(Double.parseDouble(val) / 100);
			}
		});
		
		panel.add(list);
		
		return panel;
	}
	
	public Widget getZoomToButton() {
		Anchor a = new Anchor("<i class='icon-resize-small menu-icon'></i>&nbsp;&nbsp;Zoom to Layer Extent", true);
		a.setStyleName("menu-link");
		a.addClickHandler(new ClickHandler(){
			@Override
			public void onClick(ClickEvent event) {
				if( datalayer.getType() == DataLayerType.MapServer ) {
					MapServerDataLayer msdl = (MapServerDataLayer) datalayer;
					msdl.zoomToLayerExtent();
				} else if ( datalayer.getType() == DataLayerType.ImageServer ) {
					ImageServerDataLayer isdl = (ImageServerDataLayer) datalayer;
					isdl.zoomToLayerExtent();	
				} else if ( datalayer.getType() == DataLayerType.KML ) {
					KmlDataLayer kdl = (KmlDataLayer) datalayer;
					kdl.zoomToLayerExtent();
				}
				hide();
			}	
		});
		
		return a;
	}
	
	public Widget getRemoveButton() {
		Anchor a = new Anchor("<i class='icon-remove-circle menu-icon'></i> Remove Layer", true);
		a.setStyleName("menu-link");
		a.addClickHandler(new ClickHandler(){
			@Override
			public void onClick(ClickEvent event) {
				if( Window.confirm("Are your sure?\n\nThis will permanently remove '"+name+"' from the client.") ){
					AppManager.INSTANCE.getClient().removeLayer(datalayer);
					hide();
				}
			}	
		});
		
		return a;
	}
	
	private class DescriptionPanel extends Composite {
		private SimplePanel panel = new SimplePanel();
		private ScrollPanel sp = new ScrollPanel();
		
		public DescriptionPanel(String txt) {
			panel.setStyleName("layerPopup-description well");
			panel.add(sp);
			if( txt.length() <= 3 ) txt = "No layer description provided.";
			sp.add(new HTML(txt));
			initWidget(panel);
		}
		
	}

	@Override
	public String getTitle() {
		return name;
	}

	@Override
	public Widget getBody() {
		return panel;
	}

	@Override
	public Widget getFooter() {
		return closeBtn;
	}
	
}
