package edu.ucdavis.gwt.gis.client;

import java.util.LinkedList;

import edu.ucdavis.cstars.client.MapWidget;
import edu.ucdavis.gwt.gis.client.auth.DomainAccess;
import edu.ucdavis.gwt.gis.client.config.GadgetConfig;
import edu.ucdavis.gwt.gis.client.layers.DataLayer;
import edu.ucdavis.gwt.gis.client.layers.DataLayerHandler;
import edu.ucdavis.gwt.gis.client.layers.DataLayer.DataLayerLoadHandler;
import edu.ucdavis.gwt.gis.client.mapcontroller.MapControllerHost;

/**
 * Stores global objects [map, datalayers, config, etc] in one place
 * 
 * @author Justin Merz
 */
public class AppManager {
	
	public final static AppManager INSTANCE = new AppManager();
	
	private LinkedList<DataLayer> layers = new LinkedList<DataLayer>();
	
	private LinkedList<DataLayerHandler> layerHandlers = new LinkedList<DataLayerHandler>();
		
	private GadgetConfig config = null;
	
	private MapWidget map = null;
	
	private GisClient client = null;
	
	private DomainAccess domainAccess = new DomainAccess();
	
	private MapControllerHost mapControllerHost = new MapControllerHost();
	
	public MapControllerHost getMapControllerHost() {
		return mapControllerHost;
	}

	protected AppManager() {
		domainAccess.loadFromLocalStore();
	}
	
	public DomainAccess getDomainAccess() {
		return domainAccess;
	}
	
	/**
	 * Set the GisClient object
	 * 
	 * @param client
	 */
	public void setClient(GisClient client) {
		this.client = client;
	}
	
	/**
	 * Get the GisClient object
	 * 
	 * @return GisClient
	 */
	public GisClient getClient() {
		return client;
	}
	
	/**
	 * Set the gwt-esri map object
	 * 
	 * @param map
	 */
	public void setMap(MapWidget map){
		this.map = map;
	}
	
	/**
	 * get the gwt-esri map object
	 * 
	 * @return MapWidget
	 */
	public MapWidget getMap() {
		return map;
	}
	
	/**
	 * Set the webpage's config file
	 * 
	 * @param config
	 */
	public void setConfig(GadgetConfig config) {
		this.config = config;
	}
	
	/**
	 * Get the webpages config file
	 * 
	 * @return GadgetConfig
	 */
	public GadgetConfig getConfig() {
		return config;
	}
	
	/**
	 * Add a datalayer
	 * 
	 * @param layer
	 */
	public void addDataLayer(DataLayer layer) {
		layers.add(layer);
		// make sure layer is loaded before we alert to world
		layer.addLoadHandler(new DataLayerLoadHandler(){
			@Override
			public void onDataLoaded(DataLayer dataLayer) {
				for( DataLayerHandler dlh: layerHandlers ) 
					dlh.onDataLayerAdd(dataLayer);
			}
		});
		
	}
	
	/**
	 * Remove a datalayer
	 * 
	 * @param layer
	 */
	public void removeDataLayer(DataLayer layer) {
		layers.remove(layer);
		for( DataLayerHandler dlh: layerHandlers ) 
			dlh.onDataLayerRemove(layer);
	}

	/**
	 * Get all datalayers in application
	 * 
	 * @return LinkedList<DataLayer>
	 */
	public LinkedList<DataLayer> getDataLayers() {
		return layers;
	}
	
	/**
	 * Get a datalayer by it's unique id
	 * 
	 * @param id
	 * @return DataLayer
	 */
	public DataLayer getLayerById(String id) {
		for( DataLayer dl: layers)
			if( dl.getId().contentEquals(id) ) return dl;
		return null;
	}
	
	public void addDataLayerHandler(DataLayerHandler handler) {
		layerHandlers.add(handler);
	}
	
	public void removeDataLayerHandler(DataLayerHandler handler) {
		layerHandlers.remove(handler);
	}
	
	public void fireDataLayerUpdate(DataLayer dl) {
		for( DataLayerHandler dlh: layerHandlers ) 
			dlh.onDataLayerUpdate(dl);
	}

}
