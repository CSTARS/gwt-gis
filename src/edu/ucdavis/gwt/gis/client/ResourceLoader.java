package edu.ucdavis.gwt.gis.client;

import com.gargoylesoftware.htmlunit.javascript.host.Console;
import com.google.gwt.core.client.Callback;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.ScriptInjector;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.HeadElement;
import com.google.gwt.dom.client.LinkElement;
import com.google.gwt.dom.client.ScriptElement;
import com.google.gwt.user.client.Window;

public class ResourceLoader {
	
	private static int rootLoadCount = 0;
	private static String[] rootScript = {
		"http://js.arcgis.com/3.8/",
		"http://ajax.googleapis.com/ajax/libs/jquery/1.9.1/jquery.min.js"
	};
	
	private static String[] scripts = {
		"https://apis.google.com/js/client.js?onload=ginit",
		GWT.getModuleBaseForStaticFiles()+"js/bootstrap.min.js",
		GWT.getModuleBaseForStaticFiles()+"js/idangerous.swiper-1.9.min.js",
		GWT.getModuleBaseForStaticFiles()+"js/jquery.esriPreview.js"
	};
	
	private static String[] styles = {
		GWT.getModuleBaseForStaticFiles()+"css/bootstrap.min.css", 
		GWT.getModuleBaseForStaticFiles()+"css/bootstrap-responsive.min.css",
		"http://maxcdn.bootstrapcdn.com/font-awesome/4.3.0/css/font-awesome.min.css"
	};
	
	public static void inject() {
		injectScripts();
		injectStyles();
	}
	

	public static void injectRootScripts(final Runnable callback) {
		for( int i = 0; i < rootScript.length; i++ ) {
			
			ScriptInjector.fromUrl(rootScript[i]).setCallback(
		     new Callback<Void, Exception>() {
				@Override
				public void onFailure(Exception reason) {
					rootLoadCount++;
					if( rootLoadCount == rootScript.length ) callback.run();
				}
				@Override
				public void onSuccess(Void result) {
					rootLoadCount++;
					if( rootLoadCount == rootScript.length ) callback.run();
				}
		     }).setWindow(getWindow()).inject();
		}
	}
	
	private static native JavaScriptObject getWindow() /*-{
		return $wnd;
	}-*/;
	
	private static void injectScripts() {
		HeadElement head = Document.get().getHead();
		
		for( int i = 0; i < scripts.length; i++ ) {
			ScriptElement script = Document.get().createScriptElement();
			script.setSrc(scripts[i]);
			head.appendChild(script);
		}
	}
	
	private static void injectStyles() {
		HeadElement head = Document.get().getHead();
		
		for( int i = 0; i < styles.length; i++ ) {
			LinkElement link = Document.get().createLinkElement();
			link.setRel("stylesheet");
			link.setHref(styles[i]);
			head.appendChild(link);
		}
	}
	
	
	
}
