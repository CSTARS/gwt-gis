package edu.ucdavis.gwt.gis.client.extras;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style.Cursor;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.DisclosurePanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

import edu.ucdavis.cstars.client.layers.ArcGISDynamicMapServiceLayer;
import edu.ucdavis.cstars.client.layers.ArcGISImageServiceLayer;
import edu.ucdavis.cstars.client.layers.ArcGISDynamicMapServiceLayer.ImageFormat;
import edu.ucdavis.gwt.gis.client.AppManager;
import edu.ucdavis.gwt.gis.client.auth.DomainToken;
import edu.ucdavis.gwt.gis.client.auth.TokenPopupPanel;
import edu.ucdavis.gwt.gis.client.layers.DataLayer;
import edu.ucdavis.gwt.gis.client.layers.ImageServerDataLayer;
import edu.ucdavis.gwt.gis.client.layers.MapServerDataLayer;
import edu.ucdavis.gwt.gis.client.layers.DataLayer.DataLayerType;
import edu.ucdavis.gwt.gis.client.layout.LayerPopup;
import edu.ucdavis.gwt.gis.client.layout.modal.BootstrapModalLayout;

public class AdvancedLayerConfig extends BootstrapModalLayout {

	private static AdvancedLayerConfigUiBinder uiBinder = GWT.create(AdvancedLayerConfigUiBinder.class);
	interface AdvancedLayerConfigUiBinder extends UiBinder<Widget, AdvancedLayerConfig> {}

	private DataLayer datalayer = null;
	
	@UiField TextBox titleInput;
	@UiField RadioButton dynamicMapService;
	@UiField RadioButton tiledMapService;
	@UiField CheckBox legendIsGradient;
	@UiField Element mapTypeSelectPanel;
	@UiField TextBox token;
	@UiField TextBox tokenUsername;
	@UiField SimplePanel generateTokenHelp;
	@UiField Anchor tokenManager;
	@UiField ListBox imageFormat;
	@UiField Element imageFormatSelectPanel;
	@UiField Element legendSelectPanel;
	
	private SimplePanel tokenHelpContent = new SimplePanel();
	
	private Anchor saveButton = new Anchor("Save");
	private Anchor cancelButton = new Anchor("Cancel");
	private Anchor backButton = new Anchor("Back to Layer Menu");
	private LayerPopup layerPopup;
	
	private Widget body;
	private FlowPanel footer = new FlowPanel();
	
	
	public AdvancedLayerConfig() {
		body = uiBinder.createAndBindUi(this);
		
		saveButton.addStyleName("btn");
		saveButton.addStyleName("btn-primary");
		cancelButton.addStyleName("btn");
		backButton.addStyleName("btn");
		backButton.addStyleName("pull-left");
		footer.add(backButton);
		footer.add(saveButton);
		footer.add(cancelButton);
		
		initHandlers();
	}
	
	/// add back buttons
	public void editLayer(LayerPopup popup) {
		layerPopup = popup;
		datalayer = popup.getDataLayer();
		initEditor();
		show();
	}
	
	// no back button
	public void editLayer(DataLayer dl) {
		layerPopup = null;
		backButton.setVisible(false);
		datalayer = dl;
		initEditor();
		show();
	}
	
	private void initEditor() {
		titleInput.setText(datalayer.getLabel());
		
		String format = "";
		if( datalayer.getType() == DataLayerType.MapServer ) {
			MapServerDataLayer msdl = (MapServerDataLayer) datalayer;
			
			if( msdl.hasTileOption() ) {
				mapTypeSelectPanel.getStyle().setDisplay(Display.BLOCK);
				if( msdl.isTiled() ) {
					tiledMapService.setValue(true);
					imageFormatSelectPanel.getStyle().setDisplay(Display.NONE);
				} else {
					dynamicMapService.setValue(true);
					imageFormatSelectPanel.getStyle().setDisplay(Display.BLOCK);
					format = ((ArcGISDynamicMapServiceLayer) msdl.getMapLayer()).getImageFormat().getValue();
				}
			} else {
				mapTypeSelectPanel.getStyle().setDisplay(Display.NONE);
			}
			
			legendIsGradient.setValue(msdl.legendIsGradient());
			legendSelectPanel.getStyle().setDisplay(Display.BLOCK);
			
			DomainToken dt = AppManager.INSTANCE.getDomainAccess().getDomainToken(msdl.getUrl());
			if( dt != null ) {
				token.setText(dt.getToken());
				tokenUsername.setText(dt.getUsername());
			} 
			
		} else if( datalayer.getType() == DataLayerType.ImageServer ) {
			mapTypeSelectPanel.getStyle().setDisplay(Display.NONE);
			legendSelectPanel.getStyle().setDisplay(Display.NONE);
			imageFormatSelectPanel.getStyle().setDisplay(Display.BLOCK);
			ImageServerDataLayer isdl = (ImageServerDataLayer) datalayer;
			format = ((ArcGISImageServiceLayer) isdl.getMapLayer()).getFormat();
		}
		
		if( format == null ) format = "";
		
		// init image type
		imageFormat.clear();
		imageFormat.addItem("Layer Default", "");
		imageFormat.addItem(ImageFormat.PNG8.getValue());
		imageFormat.addItem(ImageFormat.PNG32.getValue());
		imageFormat.addItem(ImageFormat.JPG.getValue());
		boolean found = false;
		for( int i = 0; i < 3; i++ ) {
			if( imageFormat.getValue(i).contentEquals(format) ){
				imageFormat.setSelectedIndex(i);
				found = true;
				break;
			}
		}
		if( !found ) {
			imageFormat.addItem(format);
			imageFormat.setSelectedIndex(3);
		}
		
		setupHelp();
	}
	
	private void setupHelp() {
		tokenHelpContent.clear();
		String[] parts = datalayer.getUrl().split(":\\/\\/");
		parts[1] = parts[1].replaceAll("\\/.*", "");
		
		VerticalPanel vp = new VerticalPanel();
		vp.setSpacing(3);
		vp.add(new HTML("1) <a href='https://" +parts[1]+"/arcgis/tokens/gettoken.html' target='_blank'>Click here</a> " +
						"to generate a server token."));
		
		vp.add(new HTML("2) Enter your username and password"));
		
		vp.add(new HTML("3) Paste in the following domain into the <b>Identifier</b> field: "));
		vp.add(new HTML("<div style='padding:3px;font-weight:bold;text-align:center'>"+Window.Location.getHost()+"</div>"));
		
		vp.add(new HTML("4) Select a expire time, then click 'Generate Token'.  Copy and paste token below."));
		
		vp.getElement().getStyle().setProperty("borderBottom", "1px solid #eeeeee");
		
		tokenHelpContent.add(vp);
	}
	
	private void save() {
		if( !datalayer.getLabel().contentEquals(titleInput.getText()) ) {
			datalayer.setLabel(titleInput.getText());
			datalayer.getLayerPanel().setLayerTitle(datalayer.getLabel());
			AppManager.INSTANCE.fireDataLayerUpdate(datalayer);
		}
		
		String format = imageFormat.getValue(imageFormat.getSelectedIndex());
		
		// update layer type
		if( datalayer.getType() == DataLayerType.MapServer ) {
			
			MapServerDataLayer msdl = (MapServerDataLayer) datalayer;
			if( msdl.isTiled() && dynamicMapService.getValue() ) {
				msdl.setTiled(false);
				msdl.switchLayerType();
			} else if ( !msdl.isTiled() && tiledMapService.getValue() ) {
				msdl.setTiled(true);
				msdl.switchLayerType();
			}
			
			if( !msdl.isTiled() ) {
				((ArcGISDynamicMapServiceLayer) msdl.getMapLayer()).setImageFormat(ImageFormat.get(format));
			}
			
			if( (!legendIsGradient.getValue() && msdl.legendIsGradient()) ||
				(legendIsGradient.getValue() && !msdl.legendIsGradient()) ) {
					msdl.setLegendIsGradient(legendIsGradient.getValue());
					msdl.getLayerPanel().clearAndUpdateLegends();
			}

			
			if( !token.getText().isEmpty() ) {
				AppManager.INSTANCE.getDomainAccess().setDomainToken(
					new DomainToken(msdl.getUrl(), token.getText(), tokenUsername.getText())
				);
			}

		} else if(  datalayer.getType() == DataLayerType.ImageServer ) {
			ImageServerDataLayer isdl = (ImageServerDataLayer) datalayer;
			
			((ArcGISImageServiceLayer) isdl.getMapLayer()).setImageFormat(format);
		
			if( !token.getText().isEmpty() ) {
				AppManager.INSTANCE.getDomainAccess().setDomainToken(
					new DomainToken(isdl.getUrl(), token.getText(), tokenUsername.getText())
				);
			}
		}
	}
	
	private void initHandlers() {
		
		saveButton.addClickHandler(new ClickHandler(){
			@Override
			public void onClick(ClickEvent event) {
				save();
				hide();
			}
		});
		
		dynamicMapService.addClickHandler(new ClickHandler(){
			@Override
			public void onClick(ClickEvent event) {
				if( ((MapServerDataLayer) datalayer).hasTileOption() ) 
					tiledMapService.setValue(!dynamicMapService.getValue());
				
				if( dynamicMapService.getValue() ) imageFormatSelectPanel.getStyle().setDisplay(Display.BLOCK);
				else imageFormatSelectPanel.getStyle().setDisplay(Display.NONE);
			}
		});
		
		tiledMapService.addClickHandler(new ClickHandler(){
			@Override
			public void onClick(ClickEvent event) {
				if( ((MapServerDataLayer) datalayer).hasTileOption() ) 
					dynamicMapService.setValue(!tiledMapService.getValue());
				if( tiledMapService.getValue() ) imageFormatSelectPanel.getStyle().setDisplay(Display.BLOCK);
				else imageFormatSelectPanel.getStyle().setDisplay(Display.NONE);
			}
		});

		cancelButton.addClickHandler(closeHandler);
		
		backButton.addClickHandler(new ClickHandler(){
			@Override
			public void onClick(ClickEvent event) {
				layerPopup.show();
			}
		});
		
		DisclosurePanel dp = new DisclosurePanel();
		dp.setWidth("440px");
		dp.setAnimationEnabled(true);
		dp.setHeader(new HTML("Generate Token Help"));
		dp.setContent(tokenHelpContent);
		
		generateTokenHelp.add(dp);
		
		tokenManager.getElement().getStyle().setCursor(Cursor.POINTER);
		tokenManager.addClickHandler(new ClickHandler(){
			@Override
			public void onClick(ClickEvent event) {
				TokenPopupPanel.INSTANCE.show();
			}
		});
	}

	private ClickHandler closeHandler = new ClickHandler(){
		@Override
		public void onClick(ClickEvent event) {
			hide();
		}
	};

	@Override
	public String getTitle() {
		return "Advanced Config - "+datalayer.getLabel();
	}

	@Override
	public Widget getBody() {
		return body;
	}

	@Override
	public Widget getFooter() {
		return footer;
	}

}
