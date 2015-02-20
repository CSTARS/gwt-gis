/**
 *  ESRI Export Proxy
 *   - converts any esri query task into a support ogr2ogr format
 */
var sys = require('sys');
var exec = require('child_process').exec;
var fs = require('fs');
var request = require('request');
var uuid = require('node-uuid');
var TMP = __dirname+"/tmp";

if( !fs.existsSync(TMP) ) {
	fs.mkdir(TMP);
}

/**
 * Options:
 *  - format   : ogr2ogr format
 *  - url      : url to request
 *  - body     : post data object (pass this if you want to post)
 *  - callback : function called when all is said and done
 */
exports.run = function(options) {
	if( !options.callback ) return console.log("ESRI Export Proxy Error: no callback provided");
	if( !options.format ) return options.callback({error:true,message:"missing format parameter"});
	if( !options.url ) return options.callback({error:true,message:"missing url parameter"});
	getData(options);
}


function getData(options) {
	// this is a POST request
	if( options.body ) {
		request.post(options.url, {form:options.body}, function(error, response, body){
			handleResponse(error, response, body, options);
		});
	// this is a GET request
	} else {
		request.get(options.url, function(error, response, body){
			handleResponse(error, response, body, options);
		});
	}
}

function handleResponse(error, response, body, options) {
	if( !error && response.statusCode == 200 ) {
		try {
			options.inputFile = uuid.v4()+"_input";
			fs.writeFile(TMP+"/"+options.inputFile, body, function(err){
				if( err ) return cleanup(options, err);
				runOgr2Ogr(options);
			});
		} catch (e) {
			options.callback(e);
		}
	} else {
		options.callback(error);
	}
}

function runOgr2Ogr(options) {
	options.outputFile = uuid.v4()+"_output";
	exec(
		"ogr2ogr -f \""+options.format+"\" "+TMP+"/"+options.outputFile+" "+TMP+"/"+options.inputFile+" OGRGeoJSON",
		function(err, stdout, stderr) {
			if( err ) return cleanup(options, err);
			onOutputReady(options);
		}
	);
}

function onOutputReady(options) {
	fs.lstat(TMP+"/"+options.outputFile, function(err, stats) {
		if( err ) cleanup(options, err);
		
		// if output is dir, zip it first
		if( stats.isDirectory() ) {
			options.outputDir = options.outputFile;
			options.outputFile += ".zip";
			options.outputFormat = null;		

			exec(
				'cd '+TMP+'; zip -r '+options.outputFile+' '+options.outputDir,
				function(err, stdout, stderr) {
					if( err ) return cleanup(options, err);
					readOutput(options);
				}
			);
		// else read as UTF8 String
		} else {
			options.outputFormat = "utf8";
			readOutput(options);
		}
	});
}

function readOutput(options) {
	fs.readFile(TMP+"/"+options.outputFile, options.outputFormat, function(err, data) {
		if( err ) return cleanup(options, err);

		cleanup(options);
		options.callback(null, data);
	});
}


// if err, something bad happend, send error after file cleanup
function cleanup(options, err) {

	deleteFile(TMP+"/"+options.inputFile);
	deleteFile(TMP+"/"+options.outputFile);
	if( fs.existsSync(TMP+"/"+options.outputDir) ) {
		exec('rm -rf '+TMP+'/'+options.outputDir,function(err,out) {
			if( err ) {
				console.log("ESRI Export Proxy Error: unlink folder failed ("+TMP+"/"+outputs.outputDir+")");
				console.log(err); 
			}
		});
	}

	if( err ) options.callback(err);
}

function deleteFile(file) {
	if( fs.existsSync(file) ) {
		fs.unlink(file, function(err) {
			if( err ) {
				console.log("ESRI Export Proxy Error: unlink file failed ("+file+")");
				console.log(err);
			}
		});
	}
}

