package com.esri.quickstart;

import java.util.EventObject;

import android.location.Location;

import com.esri.core.tasks.ags.geocode.LocatorGeocodeResult;
import com.esri.core.tasks.ags.geocode.LocatorReverseGeocodeResult;

////////////////////////////
////	
//// CUSTOM EVENT BUS
////
////////////////////////////

/**
 * This Class provides a custom event bus for EsriQuickStartLib
 * 
 * @author Andy gup
 * @version 1.0
 *
 */
public class EsriQuickStartEvent extends EventObject{

	private static final long serialVersionUID = 1L;

	/**
	 * Public Event Bus for EsriQuickStartLib
	 * @param source MapViewEvent
	 */
	public EsriQuickStartEvent(Object source){
		super(source);
	}

	public EventListenerList eventListenerList = new EventListenerList();
	
	/**
	 * Adds the eventListenerList for EsriQuickStartLib
	 * @param listener
	 */
	public void addEventListener(EsriQuickStartEventListener listener){
		eventListenerList.add(EsriQuickStartEventListener.class, listener);
	}
	
	/**
	 * Removes the eventListenerList for EsriQuickStartLib
	 * @param listener
	 */
	public void removeEventListener(EsriQuickStartEventListener listener){
		eventListenerList.remove(EsriQuickStartEventListener.class, listener);
	}	
	
	/**
	 * Dispatches ADDRESS_? events
	 * @param event
	 * @param geocodeResult
	 * @param exception Any errors encountered by the geocoder. GeocodeResult will be <code>null</code> and an error message
	 * will be sent.
	 */
	public void dispatchAddressEvent(EsriQuickStartEvent event,java.util.List<LocatorGeocodeResult> geocodeResult,String exception){
		Object[] listeners = eventListenerList.getListenerList();
		Object eventObj = event.getSource();
		String eventName = eventObj.toString();
		for(int i=0; i<listeners.length;i+=2){
			if(listeners[i] == EsriQuickStartEventListener.class){				
				if(eventName.contains("ADDRESS"))
				{
						((EsriQuickStartEventListener) listeners[i+1]).onAddressResultEvent(event, geocodeResult, exception);
				}
			}
		}
	}
	
	/**
	 * Dispatches REVERSEGEOCODE_ADDRESS_? events
	 * @param event
	 * @param geocodeResult
	 * @param exception Any errors encountered by the geocoder. GeocodeResult will be <code>null</code> and an error message
	 * will be sent.
	 */
	public void dispatchGeocodeEvent(EsriQuickStartEvent event,LocatorReverseGeocodeResult geocodeResult,String exception){
		Object[] listeners = eventListenerList.getListenerList();
		Object eventObj = event.getSource();
		String eventName = eventObj.toString();
		for(int i=0; i<listeners.length;i+=2){
			if(listeners[i] == EsriQuickStartEventListener.class){				
				if(eventName.contains("REVERSE"))
				{
						((EsriQuickStartEventListener) listeners[i+1]).onAddressResultEvent(event, geocodeResult, exception);
				}
			}
		}
	}
	
	/**
	 * Dispatches LOCATION_EXCEPTION events 
	 * @param event
	 * @param message
	 */
	public void dispatchLocationExceptionEvent(EsriQuickStartEvent event,String message){
		Object[] listeners = eventListenerList.getListenerList();
		Object eventObj = event.getSource();
		String eventName = eventObj.toString();
		for(int i=0; i<listeners.length;i+=2){
			if(listeners[i] == EsriQuickStartEventListener.class){				
				if(eventName.contains("LOCATION")){
					((EsriQuickStartEventListener) listeners[i+1]).onLocationExceptionEvent(event, message);
				}				
			}
		}
	}
	
	/**
	 * Dispatches LOCATION_UPDATE events only 
	 * @param event
	 * @param location
	 */
	public void dispatchLocationChangedEvent(EsriQuickStartEvent event,Location location){
		Object[] listeners = eventListenerList.getListenerList();
		Object eventObj = event.getSource();
		String eventName = eventObj.toString();
		for(int i=0; i<listeners.length;i+=2){
			if(listeners[i] == EsriQuickStartEventListener.class){				
				if(eventName.contains("LOCATION"))
				{
					((EsriQuickStartEventListener) listeners[i+1]).onLocationChangedEvent(event, location);
				}			
			}
		}
	}
	
	/**
	 * Dispatches LOCATION_SHUTDOWN events only 
	 * @param event
	 * @param reason
	 */
	public void dispatchLocationShutdownEvent(EsriQuickStartEvent event,String reason){
		Object[] listeners = eventListenerList.getListenerList();
		Object eventObj = event.getSource();
		String eventName = eventObj.toString();
		for(int i=0; i<listeners.length;i+=2){
			if(listeners[i] == EsriQuickStartEventListener.class){				
				if(eventName.contains("LOCATION"))
				{
					((EsriQuickStartEventListener) listeners[i+1]).onLocationShutdownEvent(event, reason);
				}			
			}
		}
	}
	
	/**
	 * Dispatches STATUS_? events
	 * @param event
	 * @param status
	 */
	public void dispatchStatusEvent(EsriQuickStartEvent event,String status,Object source){
		Object[] listeners = eventListenerList.getListenerList();
		Object eventObj = event.getSource();
		String eventName = eventObj.toString();
		for(int i=0; i<listeners.length;i+=2){
			if(listeners[i] == EsriQuickStartEventListener.class){				
				if(eventName.contains("STATUS"))
				{
						((EsriQuickStartEventListener) listeners[i+1]).onStatusEvent(event, status, source);
				}
			}
		}
	}
	
	/**
	 * Dispatches STATUS_? error and failure events
	 * @param event
	 * @param status
	 */
	public void dispatchStatusErrorEvent(EsriQuickStartEvent event,String status){
		Object[] listeners = eventListenerList.getListenerList();
		Object eventObj = event.getSource();
		String eventName = eventObj.toString();
		for(int i=0; i<listeners.length;i+=2){
			if(listeners[i] == EsriQuickStartEventListener.class){				
				if(eventName.contains("STATUS"))
				{
						((EsriQuickStartEventListener) listeners[i+1]).onStatusErrorEvent(event, status);
				}
			}
		}
	}	
}
