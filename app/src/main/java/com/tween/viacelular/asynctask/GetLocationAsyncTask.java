package com.tween.viacelular.asynctask;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.maps.model.LatLng;
import com.tween.viacelular.R;
import com.tween.viacelular.interfaces.CallBackListener;
import com.tween.viacelular.models.Isp;
import com.tween.viacelular.models.IspHelper;
import com.tween.viacelular.models.Land;
import com.tween.viacelular.models.Migration;
import com.tween.viacelular.models.User;
import com.tween.viacelular.utils.ApiConnection;
import com.tween.viacelular.utils.Common;
import com.tween.viacelular.utils.StringUtils;
import com.tween.viacelular.utils.Utils;
import org.json.JSONObject;
import java.util.List;
import io.realm.Realm;

/**
 * Manejador para refrescar la ubicación actual del usuario que reportaremos al confirmar llegada de mensajes
 * Created by Tween (David Figueroa davo.figueroa@tween.com.ar) on 15/06/2016
 */
public class GetLocationAsyncTask extends AsyncTask<Void, Void, String> implements LocationListener
{
	private MaterialDialog		progress;
	private Activity			context;
	private boolean				displayDialog	= false;
	private boolean				update			= false;
	private CallBackListener	listener;

	public GetLocationAsyncTask(final Activity context, final boolean displayDialog, final boolean update, CallBackListener listener)
	{
		this.context		= context;
		this.displayDialog	= displayDialog;
		this.update			= update;
		this.listener		= listener;
	}

	protected void onPreExecute()
	{
		try
		{
			if(displayDialog)
			{
				if(progress != null)
				{
					if(progress.isShowing())
					{
						progress.cancel();
					}
				}

				progress = new MaterialDialog.Builder(context)
					.title(R.string.progress_dialog)
					.cancelable(false)
					.content(R.string.please_wait)
					.progress(true, 0)
					.show();
			}

			Migration.getDB(context);
		}
		catch(Exception e)
		{
			Utils.logError(context, "GetLocationAsyncTask:onPreExecute - Exception:", e);
		}
	}

	@Override
	protected String doInBackground(Void... params)
	{
		String result = "";

		try
		{
			SharedPreferences preferences	= context.getSharedPreferences(Common.KEY_PREF, Context.MODE_PRIVATE);
			JSONObject jsonResult			= new JSONObject();
			Realm realm						= Realm.getDefaultInstance();

			if(realm.where(Isp.class).count() == 0)
			{
				jsonResult	= new JSONObject(ApiConnection.getRequest(ApiConnection.IP_API, context, "", "", Common.TIMEOUT_API));
				result		= ApiConnection.checkResponse(context, jsonResult);
				
				if(!jsonResult.isNull(Common.KEY_CONTENT))
				{
					jsonResult = jsonResult.getJSONObject(Common.KEY_CONTENT);
				}
				else
				{
					jsonResult = null;
				}
			}
			else
			{
				result = ApiConnection.OK;
			}

			if(result.equals(ApiConnection.OK))
			{
				if(	ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
					ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
				{
					boolean foundCoords	= false;
					String jLat			= "";
					String jLon			= "";
					
					if(jsonResult != null)
					{
						if(jsonResult.has(Isp.KEY_LAT))
						{
							if(StringUtils.isNotEmpty(jsonResult.getString(Isp.KEY_LAT)))
							{
								jLat = jsonResult.getString(Isp.KEY_LAT);
							}
						}
						
						if(jsonResult.has(Isp.KEY_LON))
						{
							if(StringUtils.isNotEmpty(jsonResult.getString(Isp.KEY_LON)))
							{
								jLon = jsonResult.getString(Isp.KEY_LON);
							}
						}
					}
					else
					{
						jsonResult = new JSONObject();
					}

					//Mejora para obtener la ubicación en base al gps
					Location location;
					String bestProvider;
					Criteria criteria				= new Criteria();
					LocationManager locationManager	= (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

					if(locationManager != null)
					{
						bestProvider = locationManager.getBestProvider(criteria, false);

						if(bestProvider != null)
						{
							location = locationManager.getLastKnownLocation(bestProvider);

							if(location != null)
							{
								jLat		= String.valueOf(location.getLatitude());
								jLon		= String.valueOf(location.getLongitude());
								foundCoords	= true;
							}
							else
							{
								//Intentamos con los demás providers
								List<String> providers = locationManager.getAllProviders();

								if(providers.size() > 0)
								{
									for(String provider : providers)
									{
										location = locationManager.getLastKnownLocation(provider);

										if(location != null)
										{
											if(Common.DEBUG)
											{
												System.out.println("Provider: "+provider+" Current Location Latitude: "+location.getLatitude()+" Longitude: "+location.getLongitude());
											}

											jLat		= String.valueOf(location.getLatitude());
											jLon		= String.valueOf(location.getLongitude());
											foundCoords	= true;
											break;
										}
									}
								}
							}
						}
						else
						{
							//Intentamos con los demás providers
							List<String> providers = locationManager.getAllProviders();

							if(providers.size() > 0)
							{
								for(String provider : providers)
								{
									location = locationManager.getLastKnownLocation(provider);

									if(location != null)
									{
										if(Common.DEBUG)
										{
											System.out.println("Provider: "+provider+" Current Location Latitude: "+location.getLatitude()+" Longitude: "+location.getLongitude());
										}

										jLat		= String.valueOf(location.getLatitude());
										jLon		= String.valueOf(location.getLongitude());
										foundCoords	= true;
										break;
									}
								}
							}
						}
					}

					jsonResult.put(Isp.KEY_LAT, jLat);
					jsonResult.put(Isp.KEY_LON, jLon);

					if(foundCoords)
					{
						Address address = useGeoCoder(context, new LatLng(Double.valueOf(jLat), Double.valueOf(jLon)), "");

						if(address != null)
						{
							String currentStreet = "";

							if(StringUtils.isNotEmpty(address.getThoroughfare()))
							{
								currentStreet = address.getThoroughfare();
							}

							if(StringUtils.isNotEmpty(address.getFeatureName()))
							{
								currentStreet += " "+address.getFeatureName();
							}

							if(StringUtils.isNotEmpty(address.getPostalCode()))
							{
								currentStreet += ", "+address.getPostalCode();
							}

							if(StringUtils.isNotEmpty(address.getAdminArea()))
							{
								currentStreet += " "+address.getAdminArea();
							}

							if(StringUtils.isNotEmpty(address.getCountryName()))
							{
								currentStreet += ", "+address.getCountryName();
							}

							jsonResult.put(Land.KEY_API, address.getCountryCode());
							jsonResult.put(Isp.KEY_COUNTRY, address.getCountryName());
							jsonResult.put(Isp.KEY_CITY, address.getAdminArea());
							SharedPreferences.Editor editor	= preferences.edit();
							editor.putString(Common.KEY_PREF_ADDRESS, currentStreet);
							editor.putString(Common.KEY_GEO_LAT, jLat);
							editor.putString(Common.KEY_GEO_LON, jLon);
							User user = realm.where(User.class).findFirst();

							if(user != null)
							{
								editor.putString(Land.KEY_API, address.getCountryCode());
							}
							else
							{
								editor.putString(Land.KEY_API, address.getCountryName());
							}

							editor.apply();
						}
					}
				}

				IspHelper.parseJSON(jsonResult, context, update);
			}
		}
		catch(Exception e)
		{
			Utils.logError(context, "GetLocationAsyncTask:doInBackground - Exception:", e);
		}

		return result;
	}

	@Override
	protected void onPostExecute(String result)
	{
		try
		{
			if(displayDialog)
			{
				if(progress != null)
				{
					if(progress.isShowing())
					{
						progress.cancel();
					}
				}
			}

			if(listener != null)
			{
				listener.invoke();
			}
		}
		catch(Exception e)
		{
			Utils.logError(context, "GetLocationAsyncTask:onPostExecute - Exception:", e);
		}

		super.onPostExecute(result);
	}

	@Override
	public void onLocationChanged(Location location)
	{
		if(Common.DEBUG)
		{
			System.out.println("GetLocationAsyncTask:onLocationChanged: " + location);
		}
	}

	@Override
	public void onStatusChanged(String s, int i, Bundle bundle)
	{
		if(Common.DEBUG)
		{
			System.out.println("GetLocationAsyncTask:onStatusChanged: " + s+" i: "+i+" bundle: "+bundle);
		}
	}

	@Override
	public void onProviderEnabled(String s)
	{
		if(Common.DEBUG)
		{
			System.out.println("GetLocationAsyncTask:onProviderEnabled: " + s);
		}
	}

	@Override
	public void onProviderDisabled(String s)
	{
		if(Common.DEBUG)
		{
			System.out.println("GetLocationAsyncTask:onProviderDisabled: " + s);
		}
	}

	private static Address useGeoCoder(final Activity context, final LatLng latLng, final String street)
	{
		Address bestMatch	= null;

		try
		{
			if(ApiConnection.checkInternet(context))
			{
				Geocoder geoCoder = new Geocoder(context);
				List<Address> matches;

				if(StringUtils.isNotEmpty(street))
				{
					matches = geoCoder.getFromLocationName(street, 1);
				}
				else
				{
					matches = geoCoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
				}

				if(!matches.isEmpty())
				{
					bestMatch = matches.get(0);
				}
			}
		}
		catch(Exception e)
		{
			Utils.logError(context, "GetLocationAsyncTask:useGeoCoder - Exception:", e);
		}

		return bestMatch;
	}
}