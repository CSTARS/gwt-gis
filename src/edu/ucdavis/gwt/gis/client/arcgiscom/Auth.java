package edu.ucdavis.gwt.gis.client.arcgiscom;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.user.client.Window;

import edu.ucdavis.gwt.gis.client.ajax.RequestManager;
import edu.ucdavis.gwt.gis.client.ajax.RequestManager.Request;
import edu.ucdavis.gwt.gis.client.arcgiscom.overlay.AuthResponse;
import edu.ucdavis.gwt.gis.client.state.ClientStateManager;

public class Auth {
	
	private String token = "";
	private String username = "";
	private String loginUrl = "https://www.arcgis.com/sharing/generatetoken?f=json&expiration=120&referer="+Window.Location.getHost();
	
	public interface LoginHandler {
		public void onSuccess();
		public void onError(String message);
	};
	public LoginHandler handler = null;
	
	public void login(String user, String password, LoginHandler lhandler) {
		this.username = user;
		handler = lhandler;
		RequestManager.INSTANCE.makeRequest(new Request() {
			@Override
			public void onError() {
				handler.onError("Login Url Error");
			}
			@Override
			public void onSuccess(JavaScriptObject json) {
				AuthResponse resp = (AuthResponse) json;
				if( resp.isError() ) {
					handler.onError(resp.getError().getMessage());
				} else {
					token = resp.getToken();
					ClientStateManager.setArcGisToken(token, username);
					handler.onSuccess();
				}
			}
		}, loginUrl+"&username="+username+"&password="+password);
	}
	
	public void setUsername(String username) {
		this.username = username;
	}
	
	public String getUsername() {
		return username;
	}
	
	public String getToken() {
		return token;
	}
	
	public void setToken(String token) {
		this.token = token;
	}
	
	public void logout() {
		ClientStateManager.removeArcGisToken();
		username = "";
		token = "";
	}
	
}
