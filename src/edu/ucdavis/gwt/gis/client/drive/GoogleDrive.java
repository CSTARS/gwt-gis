package edu.ucdavis.gwt.gis.client.drive;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

import edu.ucdavis.gwt.gis.client.AppManager;
import edu.ucdavis.gwt.gis.client.drive.Oauth.IsSignedInCallback;
import edu.ucdavis.gwt.gis.client.drive.Oauth.OauthCallback;
import edu.ucdavis.gwt.gis.client.layout.modal.BootstrapModalLayout;
import edu.ucdavis.gwt.gis.client.layout.modal.MainMenuFooter;
import edu.ucdavis.gwt.gis.client.resources.GadgetResources;
import edu.ucdavis.gwt.gis.client.state.ClientStateManager;

public class GoogleDrive extends BootstrapModalLayout {

	private static GoogleDriveUiBinder uiBinder = GWT.create(GoogleDriveUiBinder.class);
	interface GoogleDriveUiBinder extends UiBinder<Widget, GoogleDrive> {}
	
	public final static GoogleDrive INSTANCE = new GoogleDrive();
	
	interface CreateFolderCallback {
		public void onFolderCreated(String id);
	}
	
	interface HasMapsFolderCallback {
		public void onComplete(String id);
	}
	
	interface GetFileMetadataCallback {
		public void onComplete(String id, String downloadUrl, String webContentLink);
	}
	
	private static final String MIME_TYPE = "application/vnd.gwt-gis.map";
	private static final String MAPS_FOLDER = AppManager.INSTANCE.getConfig().getGoogleDriveConfig().getFolderName();
	private static final String API_KEY = AppManager.INSTANCE.getConfig().getGoogleDriveConfig().getApiKey();
	private static final String PUBLIC_MAP_PROXY = AppManager.INSTANCE.getConfig().getProxy().replace("/proxy","")+"/loadPublicMap";
	
	
	private Widget panel;
	private MainMenuFooter footer = new MainMenuFooter();
	
	// are we signed in
	private boolean signedIn = false;
	// have we done a 'non-popup' check for signing in.
	private boolean checkedSignedIn = false;
	// have we loaded the drive api
	private boolean apiLoaded = false;
	
	// an array of all the user's gwt-gis files
	private JsArray<JavaScriptObject> files = null;
	// folder id
	private String parentFolderId = "";
	// loaded map metadata
	private JavaScriptObject loadedMap;
	// share object
	private JavaScriptObject shareClient;
	// is the share client window open?
	// this is a giant HACK!!!
	private boolean isShareClientOpen = false;
	
	@UiField Button signinBtn;
	@UiField Element signin;
	@UiField Element content;
	@UiField VerticalPanel mapsList;
	@UiField Button savemapBtn;
	@UiField TextBox savemapInput;
	@UiField TextArea savemapDescription;
	@UiField Element outerLoadedMapPanel;
	@UiField FlowPanel loadedMapPanel;
	
	
	protected GoogleDrive() {
		panel = uiBinder.createAndBindUi(this);
		
		signinBtn.addClickHandler(new ClickHandler(){
			@Override
			public void onClick(ClickEvent event) {
				signIn();
			}
		});
		
		savemapBtn.addClickHandler(new ClickHandler(){
			@Override
			public void onClick(ClickEvent event) {
				String name = savemapInput.getText();
				
				if( name.length() == 0 ) return;
				
				saveFile(name, savemapDescription.getText());
				savemapInput.setText("");
				savemapDescription.setText("");
			}
		});
	}
	
	// use this to load a map from a google drive redirect url
	public void loadMapOnStart() {
		// get the drive object from 'state' parameter
		final State stateJso = State.create(Window.Location.getParameter("state"));
		
		if( stateJso.getIds().length() == 0 ) {
			Window.alert("Google Drive opened app but did not supply a file to load");
			return;
		}
		
		Oauth.INSTANCE.checkSignedIn(new IsSignedInCallback(){
			@Override
			public void isSignedIn(boolean signedIn) {
				if( signedIn ) {
					GoogleDrive.this.signedIn = true;
					hideSignInBtn();
					loadDriveApi(new OauthCallback(){
						@Override
						public void onCallComplete() {
							loadMapOnStart(stateJso.getIds().get(0), false);
						}
					});
				} else {
					
					// sign in
					Oauth.INSTANCE.signIn(new OauthCallback(){
						@Override
						public void onCallComplete() {
							GoogleDrive.this.signedIn = true;
							hideSignInBtn();
							
							// load drive api
							loadDriveApi(new OauthCallback(){
								@Override
								public void onCallComplete() {
									loadMapOnStart(stateJso.getIds().get(0), false);
								}
							});
							
						}
					});
					
				}
			}
		});
		
		checkedSignedIn = true;
	}
	
	// public maps in the url will not require login
	// will still require drive api...
	public void loadPublicMapOnStart() {
		// get the drive object from 'state' parameter
		final String fileId = Window.Location.getParameter("pubstate");
		
		loadDriveApi(new OauthCallback(){
			@Override
			public void onCallComplete() {
				loadMapOnStart(fileId, true);
			}
		});
	}
	
	// this should only be called by loadMapOnStart() 
	// after all auth has been taken care of.
	private void loadMapOnStart(String id, final boolean isPublic) {
		getFileMetadata(id, new GetFileMetadataCallback(){
			@Override
			public void onComplete(String id, String downloadUrl, String alternateLink) {
				// use the altLink through a proxy to get the file contents :/
				if( isPublic ) loadFile(alternateLink, id, isPublic);
				else loadFile(downloadUrl, id, isPublic);
			}
		});
	}
	
	public void show() {
		super.show();
		if( !checkedSignedIn ) {
			
			Oauth.INSTANCE.checkSignedIn(new IsSignedInCallback(){
				@Override
				public void isSignedIn(boolean signedIn) {
					if( signedIn ) {
						GoogleDrive.this.signedIn = true;
						hideSignInBtn();
						loadDriveApi();
					} else {
						showSignInBtn();
					}
				}
			});
			
			checkedSignedIn = true;
		} else {
			reloadMetadata();
			getFileList();
		}
	}
	
	private void loadDriveApi(final OauthCallback callback) {
		Oauth.INSTANCE.loadApi("drive", "v2", new OauthCallback(){
			@Override
			public void onCallComplete() {
				apiLoaded = true;
				getFileList();
				reloadMetadata();
				if( callback != null ) callback.onCallComplete();
			}
		});
	}
	
	private void loadDriveApi() {
		loadDriveApi(null);
	}
	
	private void signIn() {
		Oauth.INSTANCE.signIn(new OauthCallback(){
			@Override
			public void onCallComplete() {
				GoogleDrive.this.signedIn = true;
				hideSignInBtn();
				loadDriveApi();
			}
		});
	}
	
	private void hideSignInBtn() {
		content.getStyle().setDisplay(Display.BLOCK);
		signin.getStyle().setDisplay(Display.NONE);
	}
	
	private void showSignInBtn() {
		content.getStyle().setDisplay(Display.NONE);
		signin.getStyle().setDisplay(Display.BLOCK);
	}

	public void getFileList() {
		mapsList.clear();
		mapsList.add(new HTML("Loading maps from drive..."));
		_list("mimeType = '"+MIME_TYPE+"'", this);
	}
	
	private native void _list(String query, GoogleDrive gd) /*-{
		$wnd.gapi.client.drive.files
			.list({q: query+" and trashed = false"})
			.execute(function(resp) {
				gd.@edu.ucdavis.gwt.gis.client.drive.GoogleDrive::files = resp.items;
	      		gd.@edu.ucdavis.gwt.gis.client.drive.GoogleDrive::onFilesLoad()();
	    	});
	}-*/;
	
	private void onFilesLoad() {
		mapsList.clear();
		
		if( files == null ) {
			mapsList.add(new HTML("You are not logged into Google Drive"));
			return;
		}
		
		if( files.length() == 0 ) {
			mapsList.add(new HTML("You have no saved maps on Google Drive"));
			return;
		}
		
		for( int i = 0; i < files.length(); i++ ) {
			HTML btn = new HTML(_createFileBtn(files.get(i)));
			btn.addClickHandler(new ClickHandler(){
				@Override
				public void onClick(ClickEvent event) {
					HTML btn = (HTML) event.getSource();
					String durl = Element.as(btn.getElement().getFirstChild()).getAttribute("durl");
					String id = Element.as(btn.getElement().getFirstChild()).getAttribute("id");
					loadFile(durl, id.replace("maplink-", ""), false);
				}
			});
		
			mapsList.add(btn);
		}
	}
	
	private native String _createFileBtn(JavaScriptObject metadata) /*-{
		var btn = "<a class='btn btn-link' id='maplink-"+metadata.id+"' durl='"+metadata.downloadUrl+"'>";
		if( metadata.iconLink ) btn += "<img src='"+metadata.iconLink+"' border='0' /> ";
		btn += metadata.title+"</a>";
		return btn;
	}-*/;
	
	// reload the metadata for the current file
	private void reloadMetadata() {
		if( loadedMap == null ) return;
		
		loadedMapPanel.clear();
		loadedMapPanel.add(new HTML("Reloading map data from Google Drive..."));
		
		_getFileMetadata(_getLoadedMapId(), Oauth.INSTANCE.getToken(), new GetFileMetadataCallback(){
			@Override
			public void onComplete(String id, String downloadUrl,
					String webContentLink) {
				
				loadedMap = getLoadedMap(id);
				loadedMapPanel.clear();
				loadedMapPanel.add(new HTML(_getLoadedMapHtml(loadedMap, Oauth.INSTANCE.getToken())));
				
				addLoadedMapPanelButtons(id);
			}
		}, this);
		
	}
	
	private void getFileMetadata(String id, GetFileMetadataCallback callback) {
		_getFileMetadata(id, Oauth.INSTANCE.getToken(), callback, this);
	}
	
	private native void _getFileMetadata(String id, JavaScriptObject token, GetFileMetadataCallback callback, GoogleDrive gd) /*-{
		
		// if no token is provided, send along the generic api key,
		// this is probably for loading a public map
		var key = @edu.ucdavis.gwt.gis.client.drive.GoogleDrive::API_KEY;
		if( token ) key = "";
		else if ( key.length > 0 ) key = "?key="+key;
		
		$wnd.$.ajax({
			type : "GET",
			url  : "https://www.googleapis.com/drive/v2/files/"+id+key,
			beforeSend: function (request) {
                if( token ) request.setRequestHeader("Authorization", 'Bearer ' + token.access_token);
            },
			success : function(data, status, xhr) {
				gd.@edu.ucdavis.gwt.gis.client.drive.GoogleDrive::loadedMap = data;
				callback.@edu.ucdavis.gwt.gis.client.drive.GoogleDrive.GetFileMetadataCallback::onComplete(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)(data.id, data.downloadUrl, data.webContentLink);
			},
			error : function() {
				alert("Failed to load file metadata from Google Drive");
			}
		});
	}-*/;
	
	private void loadFile(String url, String id, boolean isPublic) {
		outerLoadedMapPanel.getStyle().setDisplay(Display.BLOCK);
		loadedMapPanel.clear();
		loadedMapPanel.add(new HTML("Loading map from Google Drive..."));
		
		_loadFile(url, id, isPublic, Oauth.INSTANCE.getToken(), this);
	}
	
	private native void _loadFile(String url, String id, boolean isPublic, JavaScriptObject token, GoogleDrive gd) /*-{
		
		// if is public, run through proxy
		if( isPublic ) url = @edu.ucdavis.gwt.gis.client.drive.GoogleDrive::PUBLIC_MAP_PROXY+"?url="+encodeURIComponent(url);
		
		// if no token is provided, send along the generic api key,
		// this is probably for loading a public map
		//var key = @edu.ucdavis.gwt.gis.client.drive.GoogleDrive::API_KEY;
		//if( token ) key = "";
		//else if ( key.length > 0 ) key = "&key="+key;
		
		$wnd.$.ajax({
			type : "GET",
			url  : url,
			beforeSend: function (request) {
                if( !isPublic ) request.setRequestHeader("Authorization", 'Bearer ' + token.access_token);
            },
			success : function(data, status, xhr) {

				if( !isPublic ) {
					// WTF?
					try {
						data = JSON.parse(data);
					} catch (e) {}
				}
				
				gd.@edu.ucdavis.gwt.gis.client.drive.GoogleDrive::_loadMap(Ljava/lang/String;Ljava/lang/String;)(id, data);
			},
			error : function() {
				alert("Failed to load file from Google Drive");
			}
		});
	}-*/;
	
	private void _loadMap(final String id, String json) {
		ClientStateManager.clearClientState();
		ClientStateManager.loadClientStateFromJson(id, json);
		
		loadedMap = getLoadedMap(id);
		loadedMapPanel.clear();
		loadedMapPanel.add(new HTML(_getLoadedMapHtml(loadedMap, Oauth.INSTANCE.getToken())));
		
		addLoadedMapPanelButtons(id);
	};
	
	private void addLoadedMapPanelButtons(final String id) {
		FlowPanel buttonPanel = new FlowPanel();
		
		Button share = new Button("<i class='icon-share-sign'></i> Share Map");
		share.addStyleName("btn");
		share.addStyleName("btn-primary");
		share.addStyleName("pull-right");
		share.addClickHandler(new ClickHandler(){
			@Override
			public void onClick(ClickEvent event) {
				share();
			}
		});
		buttonPanel.add(share);
		
		Button save = new Button("<i class='icon-save'></i> Save Map");
		save.addStyleName("btn");
		save.addStyleName("btn-primary");
		save.addClickHandler(new ClickHandler(){
			@Override
			public void onClick(ClickEvent event) {
				_updateFile(id, ClientStateManager.getMapAsJson(_getLoadedMapName()), GoogleDrive.this);
			}
		});
		buttonPanel.add(save);
		loadedMapPanel.add(buttonPanel);
	}
	
	private native String _getLoadedMapId() /*-{
		var map = this.@edu.ucdavis.gwt.gis.client.drive.GoogleDrive::loadedMap;
		if( map.id ) return map.id;
		return "";
	}-*/;
	
	private native String _getLoadedMapName() /*-{
		var map = this.@edu.ucdavis.gwt.gis.client.drive.GoogleDrive::loadedMap;
		if( map.title ) return map.title;
		return "gwt-gis map";
	}-*/;
	
	private native String _getLoadedMapHtml(JavaScriptObject map, JavaScriptObject token) /*-{
		var html = "<b>Currently Loaded Map: </b> "+map.title+"<br /><div class='well well-small' style='color:#666; font-size:11px'>";
		if( map.description && map.description.length > 0 ) html += "<b>Description:</b> "+map.description+"<br />";
		html += "<b>Created:</b> "+map.createdDate.split("T")[0]+"<br />";
		html += "<b>Last Modified:</b> "+map.modifiedDate.split("T")[0]+" by "+map.lastModifyingUserName+"<br />";
		if( map.ownerNames ) html += "<b>Owner(s):</b> "+map.ownerNames.join(' ')+"<br />";
		html += "<b>Drive Link:</b> <a href='https://docs.google.com/file/d/"+map.id+"/edit' target='_blank'>Open in Google Drive</a><br />";
		
		if( token ) {
			html += "<div id='gdrive-publiclink-resp'><b>Public Link:</b> checking...</div>";
			$wnd.$.ajax({
				type : "GET",
				url  : "https://www.googleapis.com/drive/v2/files/"+map.id+"/permissions/anyone",
				beforeSend: function (request) {
	                request.setRequestHeader("Authorization", 'Bearer ' + token.access_token);
	            },
				success : function(data, status, xhr) {
					if( typeof data === 'string' ) data = JSON.parse(data);
					if( data.type == "anyone" && data.role == "reader" ) {
						var href = window.location.href.replace(/\?.*$/,'')+"?pubstate="+map.id;
						$wnd.$("#gdrive-publiclink-resp").html("<b>Public Link:</b> <a href='"+href+"' target='_blank'>"+href+"</a>");
					} else {
						$wnd.$("#gdrive-publiclink-resp").html("<b>Public Link:</b> This map is private.  Click the "+
																"'Share' button to create a public link to this map");
					}
					
				},
				error : function() {
					$wnd.$("#gdrive-publiclink-resp").html("<b>Public Link:</b> This map is private.  Click the "+
																"'Share' button to create a public link to this map");
				}
			});
		}
		

		// see if the map is shared with the world
		return html+"</div>";
	}-*/;
	
	private native JavaScriptObject getLoadedMap(String id) /*-{
		var current = this.@edu.ucdavis.gwt.gis.client.drive.GoogleDrive::loadedMap;
		if( current && current.id == id ) return current;
		
		var files = this.@edu.ucdavis.gwt.gis.client.drive.GoogleDrive::files;
		for( var i = 0; i < files.length; i++ ) {
			if( files[i].id = id ) return files[i];
		}
		
		// bad
		return {};
	}-*/;
	
	private void share() {
		_share(AppManager.INSTANCE.getConfig().getGoogleDriveConfig().getAppId(), this);
	}

	private native void _share(String appId, GoogleDrive gd) /*-{
		var client = gd.@edu.ucdavis.gwt.gis.client.drive.GoogleDrive::shareClient;
		var id = gd.@edu.ucdavis.gwt.gis.client.drive.GoogleDrive::loadedMap.id;
		if( client == null ) {
			
			 $wnd.gapi.load('drive-share', function(){
			 	client = new $wnd.gapi.drive.share.ShareClient(appId);
        		client.setItemIds([id]);
			 	client.showSettingsDialog();
			 	gd.@edu.ucdavis.gwt.gis.client.drive.GoogleDrive::isShareClientOpen = true;
			 	gd.@edu.ucdavis.gwt.gis.client.drive.GoogleDrive::shareClient = client;
			 });
			 
			 // we never know when the share window has gone away
			 $wnd.$(window).on('mouseup', function() {
			 	var isOpen = gd.@edu.ucdavis.gwt.gis.client.drive.GoogleDrive::isShareClientOpen;
			 	if( !isOpen ) return;
			 	
			 	setTimeout(function(){
			 		// if it's open and 
			 		if( $wnd.$("dcs-a-dcs-b-dcs-r").length == 0 ) {
			 			gd.@edu.ucdavis.gwt.gis.client.drive.GoogleDrive::reloadMetadata()();
			 			gd.@edu.ucdavis.gwt.gis.client.drive.GoogleDrive::isShareClientOpen = false;
			 		}
			 	}, 200);
			 	
			 	
			 });
			 
		} else {
			client.setItemIds([id]);
			client.showSettingsDialog();
			gd.@edu.ucdavis.gwt.gis.client.drive.GoogleDrive::isShareClientOpen = true;
		}
	}-*/;
	
	
	private void saveFile(final String name, final String description) {
		final String json = ClientStateManager.getMapAsJson(name);
		
		if( parentFolderId.length() == 0 ) {
			// see if folder exsits
			_hasMapsFolder("title = '"+MAPS_FOLDER+"' and mimeType = 'application/vnd.google-apps.folder'", new HasMapsFolderCallback(){
				@Override
				public void onComplete(String id) {
					
					// no folder, must create
					if( id.length() == 0 ) {
						_createFolder(MAPS_FOLDER, new CreateFolderCallback(){
							@Override
							public void onFolderCreated(String id) {
								
								parentFolderId = id;
								_saveFile(parentFolderId, name, description, json, GoogleDrive.this);
								
							}
						});
					} else {
						
						parentFolderId = id;
						_saveFile(parentFolderId, name, description, json, GoogleDrive.this);
					}
					
				}
			});
			
		} else {
			_saveFile(parentFolderId, name, description, json, this);
		}
	}
	
	private native void _hasMapsFolder(String query, HasMapsFolderCallback callback) /*-{
		$wnd.gapi.client.drive.files
			.list({q: query+" and trashed = false"})
			.execute(function(resp) {
				console.log(resp);
				if( resp.items && resp.items.length > 0 ) {
					callback.@edu.ucdavis.gwt.gis.client.drive.GoogleDrive.HasMapsFolderCallback::onComplete(Ljava/lang/String;)(resp.items[0].id);
				} else {
					callback.@edu.ucdavis.gwt.gis.client.drive.GoogleDrive.HasMapsFolderCallback::onComplete(Ljava/lang/String;)("");
				}
	    	});
	}-*/;
	
	private native void _createFolder(String name, CreateFolderCallback callback) /*-{

	    var metadata = {
	      'title': name,
	      'mimeType': 'application/vnd.google-apps.folder',
	    };


	    var request = $wnd.gapi.client.request({
	        'path': '/drive/v2/files',
	        'method': 'POST',
	        'headers': {
	          'Content-Type': 'application/json'
	        },
	        'body': JSON.stringify(metadata)
	    });
	    
	    request.execute(function(resp){
	    	if( resp.id ) {
	    		callback.@edu.ucdavis.gwt.gis.client.drive.GoogleDrive.CreateFolderCallback::onFolderCreated(Ljava/lang/String;)(resp.id);
	    	} else {
	    		alert("Failed to create folder");
	    	}
	    });
	}-*/;
	
	private native void _saveFile(String folderId, String name, String description, String json, GoogleDrive gd) /*-{
		var boundary = '-------314159265358979323846';
		var delimiter = "\r\n--" + boundary + "\r\n";
		var close_delim = "\r\n--" + boundary + "--";

	    var metadata = {
	      'title': name,
	      'description':description,
	      'mimeType': @edu.ucdavis.gwt.gis.client.drive.GoogleDrive::MIME_TYPE,
	      parents : [{id: folderId}]
	    };

	    var base64Data = btoa(JSON.stringify(json));
	    var multipartRequestBody =
	        delimiter +
	        'Content-Type: application/json\r\n\r\n' +
	        JSON.stringify(metadata) +
	        delimiter +
	        'Content-Type: ' + @edu.ucdavis.gwt.gis.client.drive.GoogleDrive::MIME_TYPE + '\r\n' +
	        'Content-Transfer-Encoding: base64\r\n' +
	        '\r\n' +
	        base64Data +
	        close_delim;

	    var request = $wnd.gapi.client.request({
	        'path': '/upload/drive/v2/files',
	        'method': 'POST',
	        'params': {'uploadType': 'multipart'},
	        'headers': {
	          'Content-Type': 'multipart/mixed; boundary="' + boundary + '"'
	        },
	        'body': multipartRequestBody});
	    
	    request.execute(function(resp){
	    	if( resp.id ) {
	    		gd.@edu.ucdavis.gwt.gis.client.drive.GoogleDrive::getFileList()();
	    		alert("Map Saved!");
	    	} else {
	    		alert("Failed to save map");
	    	}
	    });
	}-*/;
	
	private native void _updateFile(String fileId, String json, GoogleDrive gd) /*-{
		var boundary = '-------314159265358979323846';
		var delimiter = "\r\n--" + boundary + "\r\n";
		var close_delim = "\r\n--" + boundary + "--";
	
		 var metadata = {};
	
	    var base64Data = btoa(JSON.stringify(json));
	    var multipartRequestBody =
	    	delimiter +
	        'Content-Type: application/json\r\n\r\n' +
	        JSON.stringify(metadata) +
	        delimiter +
	        'Content-Type: ' + @edu.ucdavis.gwt.gis.client.drive.GoogleDrive::MIME_TYPE + '\r\n' +
	        'Content-Transfer-Encoding: base64\r\n' +
	        '\r\n' +
	        base64Data +
	        close_delim;
	
	    var request = $wnd.gapi.client.request({
	        'path': '/upload/drive/v2/files/'+fileId,
	        'method': 'PUT',
	        'params': {'uploadType': 'multipart'},
	        'headers': {
	          'Content-Type': 'multipart/mixed; boundary="' + boundary + '"'
	        },
	        'body': multipartRequestBody});
	    
	    request.execute(function(resp){
	    	if( resp.id ) {
	    		alert("Map Updated!");
	    	} else {
	    		alert("Failed to update map");
	    	}
	    });
	}-*/;
	
	@Override
	public String getTitle() {
		return new Image(GadgetResources.INSTANCE.driveIcon()).toString()+" Google Drive";
	}

	@Override
	public Widget getBody() {
		return panel;
	}

	@Override
	public Widget getFooter() {
		return footer;
	}
	
	private static class State extends JavaScriptObject {
		
		protected State() {}
		
		public static final native State create(String json) /*-{
			return JSON.parse(json);
		}-*/;
		
		public final native JsArrayString getIds() /*-{
			if( this.ids ) return this.ids;
			return [];
		}-*/;
		
		public final native String getAction() /*-{
			if( this.action ) return this.action;
			return "";
		}-*/;
		
		public final native String getUserId() /*-{
			if( this.userId ) return this.userId;
			return "";
		}-*/;
		
	}


}
