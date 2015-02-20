var express = require('express');
var request = require('request');
var csv = require('csv');
var fs = require('fs');
var exportProxy = require('./esri-export-proxy.js');
var app = express();

var validUrls = require('/etc/gwt-gis/whitelist');

process.on('uncaughtException', function(err) {
    // handle the error safely
    console.log(err);
});

var allowCrossDomain = function(req, res, next) {
    res.header('Access-Control-Allow-Origin', '*');
    res.header('Access-Control-Allow-Methods', 'GET,PUT,POST,DELETE,OPTIONS');
    res.header('Access-Control-Allow-Headers', 'Content-Type, Authorization, Content-Length, X-Requested-With');
    
    console.log("Request Received, routing through cross-domain middleware");
    
    // intercept OPTIONS method
    if ('OPTIONS' == req.method) {
    	
	    // make sure it's a valid proxy url
	    var url = getProxyUrl(req).replace(/.*:\/\//,'').replace(/\/.*/,'');
	    if( validUrls.list.indexOf(url) == -1 ) {
	    	console.log("Invalid proxy: "+url);
	    	return next(403);
	    }
    	
       res.end("ok");
    } else {
      next();
    }
};

app.configure(function () {
  app.use(allowCrossDomain);
  app.use(express.bodyParser({limit: '50mb'}));
  app.use(express.cookieParser());
  app.use(express.logger());
  app.use(express.methodOverride());
  app.use(express.session({secret: '1234567890QWERTY'}));
  app.use(app.router);
});

app.post('/export', function(req,res){
	var body = req.body;
	var type = body.type;

	if( type == "csv" ) {
		res.header('Content-Disposition', 'attachment; filename='+body.title.replace(/,/g,'-')+'.'+type);
		csv()
		.from( JSON.parse(body.data) )
		.to( function(data){
	  		res.send(data);
		});
	} else {
		// wkid comes in as a string, but needs to be an it or arcserver complains...
		try {
			body.geometry.spatialReference.wkid = parseInt(body.geometry.spatialReference.wkid);
		} catch (e) {}

		exportProxy.run({
			url  : body.url,
			body : {
				f              : "json",
				where          : body.where ? body.where : "",
				returnGeometry : "true",
				spatialRel     : "esriSpatialRelIntersects",
				geometry       : typeof body.geomerty == "object" ? JSON.stringify(body.geometry) : body.geometry,
				geometryType   : body.geometryType,
				inSR           : body.sr,
				outSR          : '4326',
				outFields      : body.outFields ? body.outFields : "*"
			},
			format  : type,
			callback : function(err, data){
				if( err ) return res.send(err);
		
				if( type == "KML" ) {
					res.set({
						'Content-Disposition': 'attachment; filename="'+body.title+'.kml"',
						'Content-Type': 'application/vnd.google-earth.kml+xml'
					});
				} else if ( type == "ESRI Shapefile" ) {
					res.set({
						'Content-Disposition': 'attachment; filename="'+body.title+'.zip"',
						'Content-Type': 'application/octet-stream'
					});
				}
				res.send(data);
			}
		});
	}
	
});

app.post('/proxy', function(req, res){	
    // make sure it's a valid proxy url
    var url = getProxyUrl(req).replace(/.*:\/\//,'').replace(/\/.*/,'');
    if( validUrls.list.indexOf(url) == -1 ) {
    	console.log("Invalid proxy: "+url);
    	return res.send(403)
    }
	
    console.log("Proxy request received: "+url);
    
	var body = req.body;
	var url = getProxyUrl(req);
 
	console.log(body);
   
	if( !body || url.length == "" ) {
		return res.send({error:true});
	}
	
	request.post(url, {form:body}, function (error, response, body) {
      console.log("Proxy response received: "+url);
	  if (!error && response.statusCode == 200) {
		  try {
			  return res.send(JSON.parse(body));
		  } catch (e) {
			  return res.send({error:true,message:"json format error"});
		  }
	  }
	  return res.send(error);
	});
});

app.post('/loadShapefile', function(req, res) {
    var respHeader = "<html><head><script type='text/javascript'>"+
		"window.parent.postMessage({msg:'createShapeFile',payload:";
    var respFooter = "},'*');</script></head><body></body></html>";

	function send(msg) {
		if( typeof msg == 'object' ) msg = JSON.stringify(msg);
		res.send(respHeader+msg+respFooter);
	}
	
    if( !req.files.file ) return send({error:true,message:"No file specified"});
    if( req.files.file.size > 10485760 ) return send({error:true,message:"File must be less the 10mb"});

    var r = request.post(
            'http://www.arcgis.com/sharing/rest/content/features/generate',
            function (error, resp, body) { 
                   if (!error && resp.statusCode == 200) {
                            send(body+",layerName:'"+req.files.file.name+"'");
                    } else {
                            send({error:true,message:"esri proxy error",errorObj:error});
                    }

            }
    );
    var form = r.form();
    form.append("f", "json");
    form.append("fileType", "shapefile");
    form.append("publishParameters", JSON.stringify({
            name: req.files.file.name,
            targetSR: {
                wkid:102100
            },
            maxRecordCount : 1000,
            enforceInputFileSizeLimit :true,
            enforceOutputJsonSizeLimit :true,
            generalize : true,
            maxAllowableOffset:10.583354500044093,
            reducePrecision:true,
            numberOfDigitsAfterDecimal:0
    }));
    form.append("file", fs.createReadStream(req.files.file.path));
});

// GOOGLE DRIVE
// currently there is no way to let a non-loggedin user to access a public map via xhr...
// using this proxy for now
app.get('/loadPublicMap', function(req, res) {
	var url = req.query.url;
	
	if( !url ) return res.send({error:true,message:"no download link provided"});
	 console.log("Loading public map: "+url);
	
	var host = url.replace(/^https:\/\//,'').split("/")[0];
	if( !url.match(/.*docs.google\.com/ ) ) return res.send({error:true,message:"invalid link"});
	
	request.get(url, function (error, response, body) {
		console.log("Public map update: "+url);
		
	  if (!error && response.statusCode == 200) {
		  try {
			  return res.send(JSON.parse(body));
		  } catch (e) {
			  return res.send({error:true,message:"json format error"});
		  }
	  }
	  return res.send(error);
	  
	  
	});
});


function getProxyUrl(req) {
	var url = req.originalUrl.split("?");
	
	if( url.length == 1 ) return "";
	return url[1]
}


app.listen(4002);
console.log("esri proxy running on port 4002");
