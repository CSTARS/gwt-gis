package edu.ucdavis.gwt.gis.client.auth;

import java.util.LinkedList;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.storage.client.Storage;

import edu.ucdavis.gwt.gis.client.AppManager;
import edu.ucdavis.gwt.gis.client.layers.DataLayer;
import edu.ucdavis.gwt.gis.client.layers.MapServerDataLayer;
import edu.ucdavis.gwt.gis.client.layers.DataLayer.DataLayerType;

public class DomainAccess {
	
	public static final int[] AUTH_ERROR_CODES = new int[] { 401, 403, 499, 498 };
	
	private static final String LOCALSTORE_TOKEN = "__gwtgis_auth_tokens_";
	private LinkedList<DomainToken> tokens = new LinkedList<DomainToken>();

	public static boolean isAuthErrorCode(int code) {
		for( int c: AUTH_ERROR_CODES ) {
			if( c == code ) return true;
		}
		return false;
	}
	
	public void loadFromLocalStore() {
		if( !Storage.isLocalStorageSupported() ) return;
		Storage datastore = Storage.getLocalStorageIfSupported();
		
		for( int i = 0; i < datastore.getLength(); i++ ) {
			if( datastore.key(i).contentEquals(LOCALSTORE_TOKEN) ) {
				JsArray<DomainTokenOverlay> overlays = _load(datastore.getItem(datastore.key(i)));
				for( int j = 0; j < overlays.length(); j++ ) {
					DomainTokenOverlay dto = overlays.get(j);
					tokens.add(new DomainToken(dto.getDomain(), dto.getToken(), dto.getUser()));
				}
				return;
			}
		}
	}
	
	private native JsArray<DomainTokenOverlay> _load(String array) /*-{
		return eval('('+array+')');
	}-*/;
	
	public LinkedList<DomainToken> getTokens() {
		return tokens;
	}
	
	public DomainToken getDomainToken(String domain) {
		if( domain == null ) return null;
		try {
			String[] parts = domain.split(":\\/\\/");
			if( parts.length <= 1 ) return null;
			domain = parts[1].replaceAll("\\/.*", "");
		} catch (Exception e) {}
		
		for( DomainToken dt: tokens ) {
			if( dt.getDomain().contentEquals(domain) ) return dt;
		}
		return null;
	}
	
	public void setDomainToken(DomainToken dt) {
		if( dt.getDomain().isEmpty() ) return;
		removeToken(dt);
		tokens.add(dt);
		save();
		reload(dt);
	}
	
	// for all services that are broken in this domain
	// attempt a reload
	private void reload(DomainToken dt) {
		LinkedList<DataLayer> layers = AppManager.INSTANCE.getDataLayers();
		LinkedList<MapServerDataLayer> reloadList = new LinkedList<MapServerDataLayer>();
		for( int i = 0; i < layers.size(); i++ ) {
			DataLayer layer = layers.get(i);
			if( layer.getType() == DataLayerType.MapServer && layer.getUrl().contains("://"+dt.getDomain()) ) {
				MapServerDataLayer msdl = (MapServerDataLayer) layer;
				if( msdl.hasError() ) {
					int c = msdl.getError().getCode();
					if ( c == 401 || c == 499 || c == 498 ) {
						reloadList.add(msdl);
					}
				}
			}
		}
		for( MapServerDataLayer msdl: reloadList ) msdl.reload();
	}
	
	private void save() {
		if( !Storage.isLocalStorageSupported() ) return;
		Storage datastore = Storage.getLocalStorageIfSupported();
		
		JavaScriptObject arr = JavaScriptObject.createArray();
		for( DomainToken dt: tokens ) {
			_push(arr, dt.getDomain(), dt.getToken(), dt.getUsername());
		}
		String jso = _toString(arr);
		
		datastore.setItem(LOCALSTORE_TOKEN, jso);
	}
	
	private native String _toString(JavaScriptObject arr) /*-{
		return JSON.stringify(arr);
	}-*/;
	
	private native void _push(JavaScriptObject arr, String domain, String token, String user) /*-{
		arr.push({"domain":domain,"token":token,"user":user});
	}-*/;
	
	public void removeDomainToken(DomainToken domainToken) {
		removeToken(domainToken);
		save();
	}
	
	private void removeToken(DomainToken domainToken) {
		for( DomainToken dt: tokens ) {
			if( dt.getDomain().contentEquals(domainToken.getDomain()) ) {
				tokens.remove(dt);
				return;
			}
		}
	}
	
	private static class DomainTokenOverlay extends JavaScriptObject {
		protected DomainTokenOverlay() {}
		public final native String getDomain() /*-{
			return this.domain;
		}-*/;
		public final native String getToken() /*-{
			return this.token;
		}-*/;
		public final native String getUser() /*-{
			return this.user;
		}-*/;
	}

}
