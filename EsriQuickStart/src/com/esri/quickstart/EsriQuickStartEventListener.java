package com.esri.quickstart;

import java.util.EventListener;

import android.location.Location;

import com.esri.core.tasks.ags.geocode.LocatorGeocodeResult;
import com.esri.core.tasks.ags.geocode.LocatorReverseGeocodeResult;

/**
 * Provides the listener methods for EsriQuickStartLib
 * @author Andy Gup
 * @version 1.0
 *
 */
public interface EsriQuickStartEventListener extends EventListener{
	/**
	 * Indicates that the address Locator service has sent a response payload back to the client.
	 * @param event 
	 * @param result the List<?> of all results returned by the address geocoder.
	 * @param exception If there was an error look for result to be null.
	 */
	public void onAddressResultEvent(EsriQuickStartEvent event,java.util.List<LocatorGeocodeResult> result,String exception);		
	/**
	 * Indicates that the address Locator service has a reverse geocode response payload back to the client.
	 * @param event
	 * @param result
	 * @param exception If there was an error look for result to be null.
	 */
	public void onAddressResultEvent(EsriQuickStartEvent event,LocatorReverseGeocodeResult result, String exception);
	/**
	 * Indicates there has been a location exception thrown by LocationService.
	 * @param event 
	 * @param message 
	 */
	public void onLocationExceptionEvent(EsriQuickStartEvent event,String message);		
	/**
	 * Indicates that the location change has been sent by the LocationService.
	 * @param event
	 * @param location
	 */
	public void onLocationChangedEvent(EsriQuickStartEvent event, Location location);		
	/*
	 * Indicates that the LocationService has been issued a stop() command. 
	 */
	public void onLocationShutdownEvent(EsriQuickStartEvent event, String reason);		
	/**
	 * Indicates that the base map has loaded. Equivalent to OnStatusChangedListener.STATUS.INITIALIZED.
	 * @param event
	 * @param status
	 * @param source
	 */
	public void onStatusEvent(EsriQuickStartEvent event,String status, Object source);
	/**
	 * Indicates that the base map has loaded. Equivalent to OnStatusChangedListener.STATUS.INITIALIZED.
	 * @param event
	 * @param status
	 */
	public void onStatusErrorEvent(EsriQuickStartEvent event,String status);
}