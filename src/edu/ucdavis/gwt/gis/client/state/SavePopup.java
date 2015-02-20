package edu.ucdavis.gwt.gis.client.state;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

import edu.ucdavis.gwt.gis.client.layout.modal.BootstrapModalLayout;
import edu.ucdavis.gwt.gis.client.layout.modal.MainMenuFooter;

public class SavePopup extends BootstrapModalLayout {

	private static SavePopupUiBinder uiBinder = GWT.create(SavePopupUiBinder.class);
	interface SavePopupUiBinder extends UiBinder<Widget, SavePopup> {}

	public static SavePopup INSTANCE = new SavePopup();
	
	@UiField TextBox mapNameInput;
	@UiField HTML saveButton;
	@UiField HTML messagePanel;
	@UiField HTML warningMessage;
	@UiField VerticalPanel updateCurrentPanel;
	@UiField HTML updateButton;
	
	private Widget panel;
	private MainMenuFooter footer = new MainMenuFooter();
	
	public SavePopup() {
		panel = uiBinder.createAndBindUi(this);
		
		warningMessage.setHTML(ClientStateManager.WARNING_MESSAGE);
		
		saveButton.addClickHandler(new ClickHandler(){
			@Override
			public void onClick(ClickEvent event) {
				save(mapNameInput.getText());
			}
		});
		
		mapNameInput.addKeyUpHandler(new KeyUpHandler(){
			@Override
			public void onKeyUp(KeyUpEvent event) {
				if( event.getNativeKeyCode() == KeyCodes.KEY_ENTER ) {
					save(mapNameInput.getText());
				}
			}
		});
		
		updateButton.addClickHandler(new ClickHandler(){
			@Override
			public void onClick(ClickEvent event) {
				String id = ClientStateManager.getCurrentStateId();
				String name = ClientStateManager.getMapName(id);
				save(name);
				ClientStateManager.removeState(id);
			}
		});
	}
	
	public void show() {
		messagePanel.setHTML("");	
		String id = ClientStateManager.getCurrentStateId();
		String name = ClientStateManager.getMapName(id);
		if( name == null ) {
			updateCurrentPanel.setVisible(false);
		} else if( id.length() == 0 ) {
			updateCurrentPanel.setVisible(false);
		} else {
			updateCurrentPanel.setVisible(true);
			updateButton.setText("Update saved map: "+name);
		}
		mapNameInput.setFocus(true);
		mapNameInput.setText("");
		super.show();
	}
	
	private void save(String name) {
		if( name.length() == 0 ) {
			messagePanel.setHTML("<div style='padding:10px;color:red;text-align:center'>Please provide a name for the map</div>");
		} else {
			ClientStateManager.saveClientState(name);
			saveSuccess();
		}
	}
	
	private void saveSuccess() {
		messagePanel.setHTML("<div style='padding:10px;color: green;text-align:center'>Success!</div>");
		Timer t = new Timer(){
			@Override
			public void run() {
				hide();
			}
		};
		t.schedule(1000);
	}

	@Override
	public String getTitle() {
		return "Save Map in Browser";
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
