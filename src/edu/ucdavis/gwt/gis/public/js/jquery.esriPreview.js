(function (window, document, $, undefined) {
	
	
	$.esriPreview = function(elem, options) {
		var defaults = {},
				plugin = this,
				$elem = $(elem),
				elem = elem,
				selector = elem.selector,
				$selector = $(selector);

			plugin.settings = {};

			plugin.ui = {
				//_basemap : "http://services.arcgisonline.com/ArcGIS/rest/services/Canvas/World_Light_Gray_Base/MapServer",
				_basemap : "http://server.arcgisonline.com/ArcGIS/rest/services/Canvas/World_Light_Gray_Base/MapServer",
				_url     : "",
				_root    : null,
				_panel   : $("<div style='position:relative' class='esriPreview' ></div>"),
				_map     : $("<img style='position:absolute;top:0;left:0;display:none' />"),
				_bg      : $("<img style='position:absolute;top:0;left:0;display:none;opacity:.7' />"),
				_loading : $("<div style='position:absolute;top:0;left:0' >loading...</div>"),
				_width   : 250,
				_height  : 250,
				_isImageServer : false,
				_layerInfo : null, // this will tell you if the layer has been loaded
				_onLoad    :null,
				_format    : "",
				_bbox      : "",
				_sr        : "",

				init : function(root, url, width, height, onLoad){
					this._onLoad = onLoad;
					this._root = root;
					this._url = url;
					if( width ) this._width = width;
					if( height ) this._height = height;
					
					this._map.height(this._height).width(this._width);
					this._bg.height(this._height).width(this._width);
					this._panel.height(this._height).width(this._width);
					
					this._panel.append(this._bg).append(this._map).append(this._loading);
					this._root.append(this._panel);
					
					if( this._url && this._url.match(/.*ImageServer.*/) ) {
						this._isImageServer = true;	
					}
					
					this.loadLayer();
				},
				
				loadLayer : function() {
					var $this = this;
					
					var rUrl = "";
					if( this._isImageServer ) rUrl = this._url+"?f=json";
					else rUrl = this._url+"/layers?f=json";
					
					$.ajax({
						url : rUrl,
						dataType : "jsonp",
						success : function(result){
							if( !result.error ) {
								$this._layerInfo = $this.create(result);
							} else {
								// a json response was returned but says invalid url
								// more than likely an old server, try this instead
								$this.tryFirstLayer();
							}
						},
						complete : function(xhr, status) {
							if( status != "success" ) $this.setError("Preview Generation Failed.<br />Could not access service information.");
						}
					});
				},
				
				tryFirstLayer : function() {
					var $this = this;
					
					$.ajax({
						url : this._url+"/0?f=json",
						dataType : "jsonp",
						success : function(result){
							if( !result.error ) {
								$this._layerInfo = $this.create(result);
							} else {
								$this.setError("Preview Generation Failed.<br />Could not access service information.");
							}
						},
						complete : function(xhr, status) {
							if( status != "success" ) $this.setError("Preview Generation Failed.<br />Could not access service information.");
						}
					});
				},
				
				create : function( info ) {
					var layer = null;
					var $this = this;
					
					if( !info ) {
						return this.setError("Preview Generation Failed.<br />Could not access service information.");
					} else if ( !info.layers ) {
						return this.setError("Preview Generation Failed.<br />Could not access service information.");
					}
					
					for( var i = 0; i < info.layers.length; i++ ) {
						if( info.layers[i].defaultVisibility ) {
							layer = info.layers[i];
							break;
						}
					}
					if( layer == null && info.layers.length() > 0 ) {
						layer = info.layers[0];
					} else if(layer == null && layers.getLayers().length() == 0 ) {
						this.setError("No layers to display");
						return null;
					}
					
					// get img format's, try highest res, png
					$.ajax({
						url : this._url+"?f=json",
						dataType : "jsonp",
						success : function(result){
							if( !result.error && result.supportedImageFormatTypes ) {
								var types = result.supportedImageFormatTypes.toLowerCase().split(",");
								for( var i = 0; i < types.length; i++ ) {
									if( types[i].match(/png\d+/) ) {
										$this._format = types[i];
										break;
									}
								}
							}
							$this.createImg(layer);
						},
						complete : function(xhr, status) {
							$this.createImg(layer);
						}
					});
					
					
					return layer;
				},
				
				createImg : function(layer) {
					var $this = this;
					var ext = null;
					
					// are we in gwt?
					if( window.$wnd && layer.extent ) ext = new $wnd.esri.geometry.Extent(layer.extent);
					else if( layer.extent ) ext = new esri.geometry.Extent(layer.extent);
					
					
					this._bbox = this.getBbox(layer, ext);
					var wkid = ext.spatialReference.wkid;
					if( wkid == null ) wkid = 0;
					
					this._sr = "";
					if( wkid > 0 ) this._sr = wkid+"";

					var exportEndPoint = "/export";
					if( this._isImageServer ) exportEndPoint = "/exportImage";
					
					// if we don't have an sr, the base map will most likely be off;
					if( this._sr.length > 0 ) {
						this._bg.attr('src', this._basemap+"/export?bbox="+this._bbox+"&format=png32&transparent=true&f=image&imageSR="+
								this._sr+"&bboxSR="+this._sr+"&size="+this._width+","+this._height);
						this._bg.show();
					}
					
					this._map.attr('src',this._url+exportEndPoint+"?bbox="+this._bbox+"&format="+this._format+"&transparent=true&f=image&imageSR="+
						this._sr+"&bboxSR="+this._sr+"&size="+(this._width)+","+(this._height));
					
					this._map.on('load', function(){
						$this._loading.hide();
						$this._map.show();
						if( $this._onLoad ) $this._onLoad($this._panel);
					});

				},
				
				resize : function(options) {
					if( this._layerInfo == null ) return; // not loaded 
					if( this._width == options.width && this._height == options.height ) return;
					
					this._width = options.width;
					this._height = options.height;
					
					var exportEndPoint = "/export";
					if( this._isImageServer ) exportEndPoint = "/exportImage";
					
					this._bg.height(this._height).width(this._width);
					this._map.height(this._height).width(this._width);
					this._panel.height(this._height).width(this._width);
					
					this._bg.attr('src', this._basemap+"/export?bbox="+this._bbox+"&format=png32&transparent=true&f=image&imageSR="+
							this._sr+"&bboxSR="+this._sr+"&size="+this._width+","+this._height);
					this._map.attr('src',this._url+exportEndPoint+"?bbox="+this._bbox+"&format="+this._format+"&transparent=true&f=image&imageSR="+
						this._sr+"&bboxSR="+this._sr+"&size="+(this._width)+","+(this._height));
				},
				
				getBbox : function(layer, extent) {
					if( extent == null ) return "";
						
					var w = this._width * 0.000254;
					var cScale = (extent.xmax - extent.xmin) / w;
					var h = this._height * 0.000254;
					var cScaleH = (extent.ymax - extent.ymin) / h;
					if( cScale > cScaleH ) cScale = cScaleH;
						
					if( cScale < layer.minScale || layer.minScale == 0 ) {
						return extent.xmin + "," + extent.ymin + "," + extent.xmax + "," + extent.ymax;
					}
						
					var x = extent.xmin + ((extent.xmax - extent.xmin) / 2);
					var y = extent.ymin + ((extent.ymax - extent.ymin) / 2);
						
					var newWidth = (layer.minScale-5) * (this._width * 0.000254);
					var newHeight = (layer.minScale-5) * (this._height * 0.000254);
						
					var xMin = x - (newWidth / 2);
					var xMax = x + (newWidth / 2);
					var yMin = y - (newHeight / 2);
					var yMax = y + (newHeight / 2);
						
					return xMin + "," + yMin + "," + xMax + "," + yMax;
				},
					
				setError: function( errorMsg) {
					this._loading.hide();
					this._panel.html("<div style='"+this._height+"px'>"+errorMsg+"</div>").css("color","white");
					this._panel.width(this._width).height(this._height);
					this._panel.addClass("error");
					this._panel.css("line-height","25px");
					if( this._onLoad ) this._onLoad(this._panel);
				}

			}
			
			plugin.ui.init($elem, options.url, options.width, options.height, options.loaded);
			
			//plugin.init();
			
			return plugin;
	}
	
	$.fn.esriPreview = function(options, value){
		return this.each(function(){
			var $this = $(this);
	        var data = $this.data('_esriPreview');

			if( !data && typeof options === 'object' ) $this.data('_esriPreview', new $.esriPreview(this, options) );
			else if(data && typeof options === 'string') data.ui[options](value);
		});	
	}
	
}(window, document, jQuery));