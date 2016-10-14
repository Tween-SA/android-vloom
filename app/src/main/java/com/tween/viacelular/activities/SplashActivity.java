package com.tween.viacelular.activities;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;
import com.newrelic.agent.android.NewRelic;
import com.tween.viacelular.R;
import com.tween.viacelular.asynctask.GetLocationByApiAsyncTask;
import com.tween.viacelular.models.Isp;
import com.tween.viacelular.models.MessageHelper;
import com.tween.viacelular.models.Migration;
import com.tween.viacelular.models.User;
import com.tween.viacelular.services.MyFirebaseInstanceIdService;
import com.tween.viacelular.services.MyFirebaseMessagingService;
import com.tween.viacelular.utils.Common;
import com.tween.viacelular.utils.DateUtils;
import com.tween.viacelular.utils.StringUtils;
import com.tween.viacelular.utils.Utils;
import java.util.ArrayList;
import java.util.List;
import io.realm.Realm;

public class SplashActivity extends AppCompatActivity
{
	private BroadcastReceiver		mRegistrationBroadcastReceiver;
	//Ordanamiento de permisos y agregado del permiso para enviar sms
	private String[]				permissionsNeed = {	Manifest.permission.ACCESS_NETWORK_STATE, Manifest.permission.ACCESS_WIFI_STATE, Manifest.permission.BROADCAST_SMS,
														Manifest.permission.GET_ACCOUNTS, Manifest.permission.INTERNET, Manifest.permission.READ_CONTACTS,
														Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.READ_SMS, Manifest.permission.SEND_SMS,
														Manifest.permission.RECEIVE_SMS, Manifest.permission.WAKE_LOCK,Manifest.permission.WRITE_EXTERNAL_STORAGE};
	public static GoogleAnalytics	analytics;
	public static Tracker			tracker;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		try
		{
			super.onCreate(savedInstanceState);

			if(!Common.DEBUG)
			{
				NewRelic.withApplicationToken(Common.HASH_NEWRELIC).start(this.getApplication());
				//Agregado para reportar a GoogleAnalytics
				analytics	= GoogleAnalytics.getInstance(this);
				analytics.setLocalDispatchPeriod(1800);
				tracker		= analytics.newTracker(Common.HASH_GOOGLEANALYTICS);
				tracker.enableExceptionReporting(true);
				tracker.enableAdvertisingIdCollection(true);
				tracker.enableAutoActivityTracking(true);
				tracker.setScreenName("Splash");
			}
			else
			{
				//Para saber qué tipo de pantalla es cuando estamos en debug
				System.out.println("Densidad de pantalla: " + getResources().getDisplayMetrics().density);
			}

			//Revisar si hay alguna preferencia que indique si estuvo logueado
			SharedPreferences preferences	= getSharedPreferences(Common.KEY_PREF, Context.MODE_PRIVATE);
			System.out.println("Prefencias: "+preferences.getAll());

			//Agregado para inicializar nueva base de datos Realm y migrar si es necesario
			if(StringUtils.isEmpty(preferences.getString(User.KEY_PHONE, "")))
			{
				//Clean install
				preferences.edit().putBoolean(Common.KEY_PREF_UPGRADED +"DB"+ Common.REALMDB_VERSION, true).apply();
			}

			Migration.getDB(this);
			Realm realm = Realm.getDefaultInstance();
			realm.setAutoRefresh(true);
			mRegistrationBroadcastReceiver = new BroadcastReceiver()
			{
				@Override
				public void onReceive(Context context, Intent intent)
				{
				}
			};

			checkPlayServices();//No hace falta levantar un IntentService para push con FCM
			setContentView(R.layout.activity_splash);

			//Agregado para actualizar coordenadas
			Isp isp = realm.where(Isp.class).findFirst();

			if(isp != null)
			{
				if(DateUtils.needUpdate(isp.getUpdated(), DateUtils.HIGH_FREQUENCY))
				{
					GetLocationByApiAsyncTask geoTask = new GetLocationByApiAsyncTask(this, false, true);
					geoTask.execute();
				}
			}
			else
			{
				GetLocationByApiAsyncTask geoTask = new GetLocationByApiAsyncTask(this, false, false);
				geoTask.execute();
			}

			//Agregado para solicitar permisos en Android 6.0
			if(Common.API_LEVEL >= Build.VERSION_CODES.M)
			{
				List<String> permissionsList = new ArrayList<>();

				for(String permission : permissionsNeed)
				{
					if(ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED)
					{
						//Agregado para evitar solicitar nuevamente permisos si el usuario ya los había negado
						if(!ActivityCompat.shouldShowRequestPermissionRationale(this, permission))
						{
							permissionsList.add(permission);
						}
					}
				}

				String[] permissions = new String[permissionsList.size()];
				permissionsList.toArray(permissions);

				if(permissions.length > 0)
				{
					int callBack = 0;
					ActivityCompat.requestPermissions(this, permissions, callBack);
				}
				else
				{
					Utils.tintColorScreen(this, Common.COLOR_ACTION);
					Utils.upgradeApp(SplashActivity.this);
				}
			}
			else
			{
				Utils.tintColorScreen(this, Common.COLOR_ACTION);
				Utils.upgradeApp(SplashActivity.this);
			}
		}
		catch(Exception e)
		{
			System.out.println("SplashActivity:onCreate - Exception: " + e);

			if(Common.DEBUG)
			{
				e.printStackTrace();
			}
		}
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults)
	{
		try
		{
			Utils.tintColorScreen(this, Common.COLOR_ACTION);
			Utils.upgradeApp(SplashActivity.this);
		}
		catch(Exception e)
		{
			System.out.println("SplashActivity:onRequestPermissionsResult - Exception: " + e);

			if(Common.DEBUG)
			{
				e.printStackTrace();
			}
		}
	}

	/**
	 * Check the device to make sure it has the Google Play Services APK. If it doesn't, display a dialog that allows users to download the APK from the Google Play Store or
	 * enable it in the device's system settings.
	 */
	private boolean checkPlayServices()
	{
		//Agregado para capturar excepciones
		try
		{
			//Nuevo registro en FCM
			FirebaseMessaging.getInstance().subscribeToTopic(MyFirebaseInstanceIdService.FRIENDLY_ENGAGE_TOPIC);
			String token = FirebaseInstanceId.getInstance().getToken();
			System.out.println("TOKEN NUEVO: "+token);

			if(StringUtils.isNotEmpty(token))
			{
				SharedPreferences.Editor preferences	= getSharedPreferences(Common.KEY_PREF, Context.MODE_PRIVATE).edit();
				preferences.putString(User.KEY_GCMID, token);
				preferences.apply();
			}

			//Actualización de métodos que estaban deprecados
			GoogleApiAvailability googleApiAvailability	= GoogleApiAvailability.getInstance();
			int resultCode								= googleApiAvailability.isGooglePlayServicesAvailable(this);

			if(resultCode != ConnectionResult.SUCCESS)
			{
				//Agregado para verificar sessión y redirigir según resultado
				final Activity activity = this;

				if(Common.DEBUG)
				{
					if(googleApiAvailability.isUserResolvableError(resultCode))
					{
						googleApiAvailability.getErrorDialog(this, resultCode, MyFirebaseMessagingService.PLAY_SERVICES_RESOLUTION_REQUEST).show();
						Utils.checkSesion(activity, Common.SPLASH_SCREEN);
					}
					else
					{
						//Modificación por AlerDialog MaterialDesign con compatibilidad extendida
						AlertDialog.Builder dialog = new AlertDialog.Builder(this);
						dialog.setCancelable(false);
						dialog.setMessage(R.string.app_name);
						dialog.setTitle(R.string.no_supported);
						dialog.setPositiveButton(R.string.agree, new DialogInterface.OnClickListener()
						{
							@Override
							public void onClick(final DialogInterface dialog, final int which)
							{
								dialog.dismiss();
								Utils.checkSesion(activity, Common.SPLASH_SCREEN);
							}
						});
						dialog.show();
					}
				}
				else
				{
					//Modificación por AlerDialog MaterialDesign con compatibilidad extendida
					AlertDialog.Builder dialog = new AlertDialog.Builder(this);
					dialog.setCancelable(false);
					dialog.setMessage(R.string.app_name);
					dialog.setTitle(R.string.no_play_services);
					dialog.setPositiveButton(R.string.agree, new DialogInterface.OnClickListener()
					{
						@Override
						public void onClick(final DialogInterface dialog, final int which)
						{
							dialog.dismiss();
							Utils.checkSesion(activity, Common.SPLASH_SCREEN);
						}
					});
					dialog.show();
				}

				return false;
			}
		}
		catch(Exception e)
		{
			System.out.println("SplashActivity:checkPlayServices - Exception: " + e);

			if(Common.DEBUG)
			{
				e.printStackTrace();
			}
		}

		return true;
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onResume()
	{
		super.onResume();

		try
		{
			LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver, new IntentFilter(MyFirebaseMessagingService.REGISTRATION_COMPLETE));

			//Agregado para redirigir cuando se recibe una push en background
			if(getIntent() != null)
			{
				if(getIntent().getExtras() != null)
				{
					MessageHelper.savePush(getIntent().getExtras(), this, "", true);
				}
			}
		}
		catch(Exception e)
		{
			System.out.println("SplashActivity:onResume - Exception: " + e);

			if(Common.DEBUG)
			{
				e.printStackTrace();
			}
		}
	}

	protected void onPause()
	{
		super.onPause();

		try
		{
			LocalBroadcastManager.getInstance(this).unregisterReceiver(mRegistrationBroadcastReceiver);
		}
		catch(Exception e)
		{
			System.out.println("SplashActivity:onPause - Exception: " + e);

			if(Common.DEBUG)
			{
				e.printStackTrace();
			}
		}
	}
}
