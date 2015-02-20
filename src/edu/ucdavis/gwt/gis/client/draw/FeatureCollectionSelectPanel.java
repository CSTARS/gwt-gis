package edu.ucdavis.gwt.gis.client.draw;

import java.util.LinkedList;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

import edu.ucdavis.cstars.client.MapWidget;
import edu.ucdavis.cstars.client.control.Control;
import edu.ucdavis.cstars.client.control.Position;
import edu.ucdavis.gwt.gis.client.AppManager;
import edu.ucdavis.gwt.gis.client.layers.DataLayer;
import edu.ucdavis.gwt.gis.client.layers.FeatureCollectionDataLayer;
import edu.ucdavis.gwt.gis.client.layers.DataLayer.DataLayerType;
import edu.ucdavis.gwt.gis.client.layout.modal.BootstrapModalLayout;
import edu.ucdavis.gwt.gis.client.resources.GadgetResources;

public class FeatureCollectionSelectPanel extends BootstrapModalLayout {

	private static FeatureCollectionSelectPanelUiBinder uiBinder = GWT.create(FeatureCollectionSelectPanelUiBinder.class);
	interface FeatureCollectionSelectPanelUiBinder extends UiBinder<Widget, FeatureCollectionSelectPanel> {}

	@UiField Anchor showAll;
	@UiField FlowPanel collections; 
	@UiField FocusPanel add;
	
	private DrawControl drawControl = null;
	private FeaturePanel currentPanel = null;
	
	private Widget panel;
	
	private Anchor close = new Anchor("Close");
	
	public FeatureCollectionSelectPanel(DrawControl drawControl) {
		this.drawControl = drawControl;
		panel = uiBinder.createAndBindUi(this);
		
		add.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				FeatureCollectionDataLayer layer = new FeatureCollectionDataLayer("DrawLayer");
				AppManager.INSTANCE.getClient().addLayer(layer);
				update();
			}
		});
		
		close.addStyleName("btn");
		close.addClickHandler(new ClickHandler(){
			@Override
			public void onClick(ClickEvent event) {
				hide();
			}
		});
	}
	
	public void update() {
		collections.clear();
		LinkedList<DataLayer> layers = AppManager.INSTANCE.getDataLayers();
		for( DataLayer dl: layers ) {
			if( dl.getType() == DataLayerType.FeatureCollection ) {
				boolean isCurrent = false;
				if( drawControl.getCurrentLayer() == dl ) isCurrent = true;
				FeaturePanel fp = new FeaturePanel((FeatureCollectionDataLayer) dl, isCurrent);
				collections.add(fp);
				if( isCurrent ) currentPanel = fp;
			}
		}
	}

	private void updateEditor(FeaturePanel panel) {
		currentPanel.setInactive();
		currentPanel = panel;
		drawControl.setCurrentLayer(panel.getDataLayer());
	}
	
	private class FeaturePanel extends Composite {
		private FeatureCollectionDataLayer datalayer = null;
		private Image editingIcon = new Image(GadgetResources.INSTANCE.edit());
		private CheckBox visibleCheckBox = new CheckBox();
		
		// name panels
		private SimplePanel outerNamePanel = new SimplePanel();
		private TextBox nameInput = new TextBox();
		private HTML name = new HTML();
		
		public FeaturePanel(FeatureCollectionDataLayer dl, boolean isCurrent) {
			datalayer = dl;
			
			HorizontalPanel panel = new HorizontalPanel();
			
			if( isCurrent ) editingIcon.setStyleName("featureCollectionEdit-active");
			else editingIcon.setStyleName("featureCollectionEdit-inactive");
			
			editingIcon.addClickHandler(editIconClickHandler);
			panel.add(editingIcon);
			
			panel.add(outerNamePanel);
			outerNamePanel.add(name);
			name.setText(dl.getLabel());
			name.addClickHandler(nameClickHandler);
			name.setStyleName("featureCollectionName");
			nameInput.addBlurHandler(nameInputBlurHandler);
			nameInput.addKeyUpHandler(nameInputKeyHandler);
			nameInput.setWidth("140px");
			nameInput.setStyleName("searchBoxInput");
			
			visibleCheckBox.setValue(dl.isVisible());
			visibleCheckBox.addClickHandler(visibleClickHandler);
			panel.add(visibleCheckBox);
			
			initWidget(panel);
		}
		
		private FeatureCollectionDataLayer getDataLayer() {
			return datalayer;
		}
		
		private void updateName() {
			if( !nameInput.getText().isEmpty() ) {
				name.setText(nameInput.getText());
				datalayer.setLabel(nameInput.getText());
			}
			outerNamePanel.clear();
			outerNamePanel.add(name);
		}
		
		private ClickHandler visibleClickHandler = new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				datalayer.setVisible(visibleCheckBox.getValue());
			}
		};
		
		private BlurHandler nameInputBlurHandler = new BlurHandler() {
			@Override
			public void onBlur(BlurEvent event) {
				updateName();
			}
		};
		
		private KeyUpHandler nameInputKeyHandler = new KeyUpHandler() {
			@Override
			public void onKeyUp(KeyUpEvent event) {
				if( event.getNativeKeyCode() == KeyCodes.KEY_ENTER ) {
					updateName();
				}
			}
		};
		
		private ClickHandler nameClickHandler = new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				outerNamePanel.clear();
				nameInput.setText("");
				outerNamePanel.add(nameInput);
				nameInput.setFocus(true);
			}
		};
		
		private ClickHandler editIconClickHandler = new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				
				if( editingIcon.getStyleName().contentEquals("featureCollectionEdit-inactive") ) {
					editingIcon.setStyleName("featureCollectionEdit-active");
					updateEditor(FeaturePanel.this);
				}
			}
		};
		
		private void setInactive() {
			editingIcon.setStyleName("featureCollectionEdit-inactive");
		}
		
	}

	@Override
	public String getTitle() {
		return "Feature Collections";
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
