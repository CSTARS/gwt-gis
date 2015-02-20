package edu.ucdavis.gwt.gis.client.drive;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.user.client.Timer;

import edu.ucdavis.gwt.gis.client.AppManager;

public class Oauth {
	
	public static final Oauth INSTANCE = new Oauth();

	private JavaScriptObject token = null;
	private String clientId = AppManager.INSTANCE.getConfig().getGoogleDriveConfig().getClientId();
	
	public interface IsSignedInCallback {
		public void isSignedIn(boolean signedIn);
	}
	
	public interface OauthCallback {
		public void onCallComplete();
	}
	
	private Oauth() {
		init();
	}
	
	private void init() {
		new Timer() {
			@Override
			public void run() {
				if( token == null ) return;
				_checkToken(Oauth.this);
			}
		}.scheduleRepeating(1000*5*60);
	};
	
	public JavaScriptObject getToken() {
		return token;
	}
	
	private native void _checkToken(Oauth oauth) /*-{
		var token = this.@edu.ucdavis.gwt.gis.client.drive.Oauth::token;
		var client_id = this.@edu.ucdavis.gwt.gis.client.drive.Oauth::clientId;
		if( ((parseInt(token.expires_at)*1000) - new Date().getTime()) < 1000*60*20 ) {
			 $wnd.gapi.auth.authorize(
		 		{
		 			client_id: client_id,
			    	scope: 'https://www.googleapis.com/auth/drive.file '+
			    			'https://www.googleapis.com/auth/drive.install '+
			    			'https://www.googleapis.com/auth/userinfo.profile', 
			    	// don't force the popup 
			    	immediate: true
			    },
			    function(){
			    	oauth.@edu.ucdavis.gwt.gis.client.drive.Oauth::token = $wnd.gapi.auth.getToken();
			    }
			);
		}
	}-*/;
	
	public void checkSignedIn(IsSignedInCallback callback) {
		if( token != null ) callback.isSignedIn(true);
		_checkSignedIn(callback, this);
	}
	
	private native void _checkSignedIn(IsSignedInCallback callback, Oauth oauth) /*-{
		var client_id = this.@edu.ucdavis.gwt.gis.client.drive.Oauth::clientId;
		 $wnd.gapi.auth.authorize(
		 		{
		 			client_id: client_id,
			    	scope: 'https://www.googleapis.com/auth/drive.file '+
			    			'https://www.googleapis.com/auth/drive.install '+
			    			'https://www.googleapis.com/auth/userinfo.profile', 
			    	// don't force the popup 
			    	immediate: true
			    },
			    function(){
			    	oauth.@edu.ucdavis.gwt.gis.client.drive.Oauth::token = $wnd.gapi.auth.getToken();
					if( oauth.@edu.ucdavis.gwt.gis.client.drive.Oauth::token != null ) {
						callback.@edu.ucdavis.gwt.gis.client.drive.Oauth.IsSignedInCallback::isSignedIn(Z)(true);
					} else {
						callback.@edu.ucdavis.gwt.gis.client.drive.Oauth.IsSignedInCallback::isSignedIn(Z)(false);
					}
			    }
		);
	 }-*/;
	
	public void signIn(OauthCallback callback) {
		_signIn(callback, this);
	}
	
	private native void _signIn(OauthCallback callback, Oauth oauth) /*-{
		var client_id = this.@edu.ucdavis.gwt.gis.client.drive.Oauth::clientId;
		 $wnd.gapi.auth.authorize(
		 		{
		 			client_id: client_id,
			    	scope: 'https://www.googleapis.com/auth/drive.file '+
			    			'https://www.googleapis.com/auth/drive.install '+
			    			'https://www.googleapis.com/auth/userinfo.profile', 
			    	// force the popup 
			    	immediate: false
			    },
			    function(){
			    	oauth.@edu.ucdavis.gwt.gis.client.drive.Oauth::token = $wnd.gapi.auth.getToken();
			
					if( oauth.@edu.ucdavis.gwt.gis.client.drive.Oauth::token != null ) {
						callback.@edu.ucdavis.gwt.gis.client.drive.Oauth.OauthCallback::onCallComplete()();
					}
			    }
		);
	}-*/;
	
	public void loadApi(String api, String version, OauthCallback callback) {
		_loadApi(api, version, callback);
	}
	
	private native void _loadApi(String api, String version, OauthCallback callback) /*-{
		$wnd.gapi.client.load(api, version, function(){
			callback.@edu.ucdavis.gwt.gis.client.drive.Oauth.OauthCallback::onCallComplete()();
		});
	}-*/;
	
	
}
