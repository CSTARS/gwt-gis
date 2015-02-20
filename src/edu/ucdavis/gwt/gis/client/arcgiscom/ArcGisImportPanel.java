package edu.ucdavis.gwt.gis.client.arcgiscom;

import java.util.HashMap;
import java.util.LinkedList;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.dom.client.Style.TextDecoration;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

import edu.ucdavis.gwt.gis.client.ajax.RequestManager;
import edu.ucdavis.gwt.gis.client.ajax.RequestManager.Request;
import edu.ucdavis.gwt.gis.client.arcgiscom.Auth.LoginHandler;
import edu.ucdavis.gwt.gis.client.arcgiscom.overlay.MapDataResponse;
import edu.ucdavis.gwt.gis.client.arcgiscom.overlay.MapOverview;
import edu.ucdavis.gwt.gis.client.arcgiscom.overlay.UserInfoResponse;
import edu.ucdavis.gwt.gis.client.layout.modal.BootstrapModalLayout;
import edu.ucdavis.gwt.gis.client.layout.modal.MainMenuFooter;
import edu.ucdavis.gwt.gis.client.resources.GadgetResources;
import edu.ucdavis.gwt.gis.client.state.ClientStateManager;

public class ArcGisImportPanel extends BootstrapModalLayout {
	
	private static ArcGisImportPanelUiBinder uiBinder = GWT.create(ArcGisImportPanelUiBinder.class);
	interface ArcGisImportPanelUiBinder extends UiBinder<Widget, ArcGisImportPanel> {}

	private String userInfoUrl = "http://www.arcgis.com/sharing/content/users/";
	private static String mapDataUrl = "http://www.arcgisonline.com/sharing/content/items/";
	
	@UiField TextBox username;
	@UiField PasswordTextBox password;
	@UiField Anchor login;
	@UiField Anchor logout;
	@UiField Element loginPanel;
	@UiField Element selectPanel;
	@UiField VerticalPanel mapList;
	@UiField HTML message;
	@UiField HTML selectMapTitle;
	@UiField TextBox publicMapId;
	@UiField Anchor loadFromId;
	
	private Widget panel;
	private MainMenuFooter footer = new MainMenuFooter();
	
	private Auth auth = new Auth();
	
	private LinkedList<Anchor> maps = new LinkedList<Anchor>();
	private static HashMap<String, MapOverview> mapOverviews = new HashMap<String, MapOverview>();
	
	private static String currentId = "";
	
	public ArcGisImportPanel() {
		panel = uiBinder.createAndBindUi(this);
		
		login.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				message.setText("Logging in to ArcGIS.com ...");
				auth.login(username.getText(), password.getText(), loginHandler);
			}
		});
		
		password.addKeyUpHandler(new KeyUpHandler(){
			@Override
			public void onKeyUp(KeyUpEvent event) {
				if( event.getNativeKeyCode() == KeyCodes.KEY_ENTER ){
					message.setText("Logging in to ArcGIS.com ...");
					auth.login(username.getText(), password.getText(), loginHandler);
				}
			}
		});
		
		logout.addClickHandler(new ClickHandler(){
			@Override
			public void onClick(ClickEvent event) {
				auth.logout();
				showLogin();
			}
		});
		
		loadFromId.addClickHandler(new ClickHandler(){
			@Override
			public void onClick(ClickEvent event) {
				String id = publicMapId.getText();
				if( id.length() > 0 ) {
					id = id.replaceAll(".*=", "");
					publicMapId.setText(id);
					loadArcGisMap(id);
					hide();
				}
			}
		});
	}
	
	private LoginHandler loginHandler = new LoginHandler() {
		@Override
		public void onError(String m) {
			message.setHTML(m);
		}
		@Override
		public void onSuccess() {
			message.setHTML("Success. Loading Your Maps...");
			loadUserInfo();
		}
	};
	
	private void loadUserInfo() {
		show();
		message.setHTML("Loading maps...");
		RequestManager.INSTANCE.makeRequest(new Request() {
			@Override
			public void onError() {
				message.setHTML("Failed to load maps from ArcGis.com");
			}
			@Override
			public void onSuccess(JavaScriptObject json) {
				UserInfoResponse resp = (UserInfoResponse) json;
				if( resp.isError() ) {
					showLogin();
					message.setHTML(resp.getError().getMessage());
				} else {
					JsArray<MapOverview> items = resp.getItems();
					maps.clear();
					mapOverviews.clear();
					for( int i = 0; i < items.length(); i++ ) {
						MapOverview item = items.get(i);
						mapOverviews.put(item.getId(), item);
						Anchor a = new Anchor(item.getTitle());
						a.getElement().setAttribute("mid", item.getId());
						a.getElement().getStyle().setTextDecoration(TextDecoration.NONE);
						a.getElement().getStyle().setProperty("whiteSpace", "nowrap");
						a.addClickHandler(showMapClickHandler);
						maps.add(a);
					}
					showMaps();
					message.setHTML("");
				}
			}
		}, userInfoUrl+auth.getUsername()+"?f=json&token="+auth.getToken());
	}
	
	private ClickHandler showMapClickHandler = new ClickHandler() {
		@Override
		public void onClick(ClickEvent event) {
			String id = ((Anchor) event.getSource()).getElement().getAttribute("mid");
			loadArcGisMap(id);
			hide();
		}
	};
	

	public void run() {
		selectPanel.getStyle().setDisplay(Display.NONE);
		loginPanel.getStyle().setDisplay(Display.NONE);
		message.setHTML("");
		
		String token = ClientStateManager.getArcGisToken();
		String username = ClientStateManager.getArcGisUsername();
		if( auth.getToken().length() > 0 ) {
			showMaps();
		} else if (token.length() > 0 && username.length() > 0 ) {
			auth.setToken(token);
			auth.setUsername(username);
			loadUserInfo();
		} else {
			showLogin();
		}
	}
	
	private void showMaps() {
		selectPanel.getStyle().setDisplay(Display.BLOCK);
		loginPanel.getStyle().setDisplay(Display.NONE);

		selectMapTitle.setHTML(auth.getUsername()+"'s Maps");
		mapList.clear();
		
		for( Anchor a: maps ) {
			HorizontalPanel hp = new HorizontalPanel();
			hp.setSpacing(3);
			hp.add(new Image(GadgetResources.INSTANCE.map()));
			hp.add(a);
			mapList.add(hp);
		}
	}
	
	private void showLogin() {
		loginPanel.getStyle().setDisplay(Display.BLOCK);
		selectPanel.getStyle().setDisplay(Display.NONE);
	}
	
	
	public static void loadArcGisMap(String id) {
		currentId = id;
		RequestManager.INSTANCE.makeRequest(new Request(){
			@Override
			public void onError() {
				Window.alert("Failed to load map from ArcGIS.com");
			}
			@Override
			public void onSuccess(JavaScriptObject json) {
				MapDataResponse resp = (MapDataResponse) json.cast();
				if( resp.isError() ) {
					Window.alert(resp.getError().getMessage());
				} else {
					ClientStateManager.loadClientStateFromArcGis(currentId, mapOverviews.get(currentId), resp);
				}
			}
		}, mapDataUrl+id+"/data?f=json");
	}

	@Override
	public String getTitle() {
		return "Login to ArcGIS.com";
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
