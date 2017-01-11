package com.tween.viacelular.asynctask;

import android.Manifest;
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
import com.tween.viacelular.services.ApiConnection;
import com.tween.viacelular.utils.Common;
import com.tween.viacelular.utils.StringUtils;
import org.json.JSONObject;
import java.util.List;
import io.realm.Realm;

/**
 * Created by davidfigueroa on 15/6/16.
 */
public class GetLocationAsyncTask extends AsyncTask<Void, Void, String> implements LocationListener
{
	private MaterialDialog		progress;
	private Context				context;
	private boolean				displayDialog	= false;
	private boolean				update			= false;
	private CallBackListener	listener;

	public GetLocationAsyncTask(final Context context, final boolean displayDialog, final boolean update, CallBackListener listener)
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
			System.out.println("GetLocationAsyncTask:onPreExecute - Exception: " + e);

			if(Common.DEBUG)
			{
				e.printStackTrace();
			}
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
				jsonResult	= new JSONObject(ApiConnection.request(ApiConnection.IP_API, context, ApiConnection.METHOD_GET, "", ""));
				result		= ApiConnection.checkResponse(context, jsonResult);
				jsonResult	= jsonResult.getJSONObject(Common.KEY_CONTENT);
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

					System.out.print("Before gps Lat: "+jLat+" Lon: "+jLon);
					//Mejora para obtener la ubicación en base al gps
					Location location				= null;
					String bestProvider				= null;
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
								System.out.println("location is null with getBestProvider:getLastKnownLocation");
							}
						}
						else
						{
							List<String> providers = locationManager.getAllProviders();

							System.out.println("providers: "+providers.size());

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
									else
									{
										System.out.println("Provider: "+provider+" is null");
									}
								}
							}
						}
					}
					else
					{
						System.out.println("locationManager is null");
					}

					System.out.print("After gps Lat: "+jLat+" Lon: "+jLon);

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

							System.out.println("currentStreet: "+currentStreet);

							jsonResult.put(Land.KEY_API, address.getCountryCode());
							jsonResult.put(Isp.KEY_COUNTRY, address.getCountryName());
							jsonResult.put(Isp.KEY_CITY, address.getAdminArea());
							SharedPreferences.Editor editor	= preferences.edit();
							editor.putString(Common.KEY_PREF_ADDRESS, currentStreet);
							editor.putString(Land.KEY_API, address.getCountryName());
							editor.apply();
						}
					}
				}
				else
				{
					System.out.println("No se dieron permisos");
				}

				System.out.println("JSON 2 parser: "+jsonResult.toString());
				IspHelper.parseJSON(jsonResult, context, update);
			}
		}
		catch(Exception e)
		{
			System.out.println("GetLocationAsyncTask:doInBackground - Exception: " + e);

			if(Common.DEBUG)
			{
				e.printStackTrace();
			}
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
				listener.callBack();
			}
		}
		catch(Exception e)
		{
			System.out.println("GetLocationAsyncTask:onPostExecute - Exception: " + e);

			if(Common.DEBUG)
			{
				e.printStackTrace();
			}
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

	public static Address useGeoCoder(final Context context, final LatLng latLng, final String street)
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

				System.out.println("Matches: "+matches.toString());

				if(!matches.isEmpty())
				{
					bestMatch = matches.get(0);
					System.out.println("Best Match: "+bestMatch.toString());
					System.out.println("Lat: "+bestMatch.getLatitude()+" Lon: "+bestMatch.getLongitude());
				}
			}
		}
		catch(Exception e)
		{
			System.out.println("GetLocationAsyncTask:useGeoCoder - Exception: " + e);

			if(Common.DEBUG)
			{
				e.printStackTrace();
			}
		}

		return bestMatch;
	}
}