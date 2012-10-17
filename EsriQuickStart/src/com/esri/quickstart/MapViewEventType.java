package com.esri.quickstart;

/**
 * Specifies the event type used in EsriQuickStart library.
 * @author Andy Gup
 *
 */
public enum MapViewEventType{
	/**
	 * Indicates that an address has been returned from a Locator request.
	 */
	ADDRESS_COMPLETE("Locator job complete. Result object java.util.List<LocatorGeocodeResult> is included in response."),
	/**
	 * Indicates that a reverse geocode job was completed: lat/lon to address.
	 */
	REVERSEGEOCODE_ADDRESS_COMPLETE("Reverse geocode job complete."),
	/**
	 * The Locator was unable to complete the request. Check for messages.
	 */
	ADDRESS_EXCEPTION("There was a problem with the Locator job."),		
	/**
	 * Indicates a stop() command has been issued on the LocationService.
	 */
	LOCATION_SHUTDOWN("A request has been sent to issue a stop() command on the LocationService"),
	/**
	 * General exception notice for the location service. Check logcat for errors.
	 */
	LOCATION_EXCEPTION("There was an unknown error related to the LocationService"),
	/**
	 * Indicates the LocationService is initialized and ready.
	 */		
	LOCATION_INITIALIZED("The LocationService has been initialized. NOTE: it still may fail after a start() attempt."),
	/**
	 * LocationService failed after 4 attempts to restart.
	 */		
	LOCATION_DELAYEDSTART_EXCEPTION("There was a problem with trying to start the LocationService. Check logcat."),		
	/**
	 * Indicates an update sent by the LocationService.
	 */
	LOCATION_UPDATE("The LocationService has sent an update."),
	/**
	 * Indicates same as OnStatusChangedListener.STATUS.INITIALIZED. When you use this library
	 * you have to use this to listen to the map initialization event. If you override the listener
	 * elsewhere in your app this will not get called and will cause errors within this library.
	 */
	STATUS_INITIALIZED("Same as OnStatusChangedListener.STATUS.INITIALIZED"),
	/**
	 * Indicates same as OnStatusChangedListener.STATUS.LAYER_LOADING_FAILED. 
	 * @see STATUS_INITIALIZED		
	 */
	STATUS_LAYER_LOADING_FAILED("" +
			"An error occurred while attempting to intialize a layer. Equivalent to OnStatusChangedListener.STATUS.LAYER_LOADING_FAILED");


	private String description;
	private MapViewEventType(String description){
		this.description = description;
	}
	
	public String getDescription(){
		return description;
	}
}
