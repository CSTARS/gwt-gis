package edu.ucdavis.gwt.gis.client.ajax;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.jsonp.client.JsonpRequestBuilder;
import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * This was uses to help make multiple request for a single layer.  We don't do that anymore so this can
 * go away.  TODO
 * 
 * @author jrmerz
 */
public class RequestManager {

	public final static RequestManager INSTANCE = new RequestManager();
	private JsonpRequestBuilder xhr = new JsonpRequestBuilder(); 
	
	protected RequestManager() {
		xhr.setTimeout(30000);
	}
	
	public void makeRequest(Request request, String url) {
		RequestWrapper wrapper = new RequestWrapper(request, url);
		xhr.requestObject(url, wrapper.callback);
	}

	public class RequestWrapper {
		public AsyncCallback<JavaScriptObject> callback;
		private Request responseObject;
		private String url = "";
		private int requestCount = 0;
		
		public RequestWrapper(Request response, String urlString) {
			responseObject = response;
			url = urlString;
			
			callback = new AsyncCallback<JavaScriptObject>() {
				@Override
				public void onFailure(Throwable caught) {
					responseObject.onError();
				}
				@Override
				public void onSuccess(JavaScriptObject result) {
					responseObject.onSuccess(result);
				}				
			};
		}
	}
	
	public interface Request {
		public void onSuccess(JavaScriptObject json);
		public void onError();
	}
	
}
