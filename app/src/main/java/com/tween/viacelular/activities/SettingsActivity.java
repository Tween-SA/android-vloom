package com.tween.viacelular.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.telephony.PhoneNumberUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.tween.viacelular.R;
import com.tween.viacelular.adapters.RecyclerAdapter;
import com.tween.viacelular.adapters.RecyclerItemClickListener;
import com.tween.viacelular.asynctask.CaptureSMSAsyncTask;
import com.tween.viacelular.models.Land;
import com.tween.viacelular.models.Message;
import com.tween.viacelular.models.Suscription;
import com.tween.viacelular.models.User;
import com.tween.viacelular.services.ApiConnection;
import com.tween.viacelular.services.MyFirebaseMessagingService;
import com.tween.viacelular.utils.Common;
import com.tween.viacelular.utils.StringUtils;
import com.tween.viacelular.utils.Utils;
import java.io.ByteArrayOutputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import io.realm.Realm;
import io.realm.RealmResults;

public class SettingsActivity extends AppCompatActivity
{
	private CheckBox			chkSilence, chkStatistics;
	public boolean				silenced		= false;
	public boolean				sendStatistics	= false;
	public Timer				timer;
	public TimerTask			timerTask;
	public final Handler		handler			= new android.os.Handler();
	private SharedPreferences	preferences;
	public int					forEightHours	= 28800000;
	private CoordinatorLayout	Clayout;
	private UpdateSilence		task;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		//Agregado para capturar excepciones
		try
		{
			super.onCreate(savedInstanceState);
			setContentView(R.layout.activity_settings);

			if(Utils.checkSesion(this, Common.ANOTHER_SCREEN))
			{
				final Intent intentRecive		= getIntent();
				setTitle(intentRecive.getStringExtra(Common.KEY_TITLE));
				Toolbar toolBar					= (Toolbar) findViewById(R.id.toolBar);
				Button btnPlayStore				= (Button) findViewById(R.id.btnPlayStore);
				Button btnBackup				= (Button) findViewById(R.id.btnBackup);
				Button btnSMS					= (Button) findViewById(R.id.btnSMS);
				Button btnPush					= (Button) findViewById(R.id.btnPush);
				Button btnPushSMS				= (Button) findViewById(R.id.btnPushSMS);
				Button btnSimilSMS				= (Button) findViewById(R.id.btnSimilSMS);
				Button btnMediaPush				= (Button) findViewById(R.id.btnMediaPush);
				setSupportActionBar(toolBar);
				RecyclerView mRecyclerView		= (RecyclerView) findViewById(R.id.RecyclerView);
				mRecyclerView.setHasFixedSize(true);
				RecyclerView.Adapter mAdapter	= new RecyclerAdapter(	Utils.getMenu(getApplicationContext()),
																		intentRecive.getIntExtra(Common.KEY_SECTION, RecyclerAdapter.SETTINGS_SELECTED),
																		ContextCompat.getColor(getApplicationContext(), R.color.accent), getApplicationContext());

				mRecyclerView.setAdapter(mAdapter);
				RecyclerView.LayoutManager mLayoutManager	= new LinearLayoutManager(this);
				mRecyclerView.setLayoutManager(mLayoutManager);
				DrawerLayout Drawer							= (DrawerLayout) findViewById(R.id.DrawerLayout);

				ActionBarDrawerToggle mDrawerToggle = new ActionBarDrawerToggle(this, Drawer, toolBar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
				{
					@Override
					public void onDrawerOpened(View drawerView)
					{
						super.onDrawerOpened(drawerView);
					}

					@Override
					public void onDrawerClosed(View drawerView)
					{
						super.onDrawerClosed(drawerView);
					}
				};

				mDrawerToggle.syncState();
				//Cambio de contexto para redirigir desde el menú
				final Activity context = SettingsActivity.this;
				mRecyclerView.addOnItemTouchListener(new RecyclerItemClickListener(getApplicationContext(),
					new RecyclerItemClickListener.OnItemClickListener()
					{
						@Override
						public void onItemClick(View view, int position)
						{
							Utils.redirectMenu(context, position, intentRecive.getIntExtra(Common.KEY_SECTION, 0));
						}
					})
				);

				chkSilence		= (CheckBox) findViewById(R.id.chkSilence);
				chkStatistics	= (CheckBox) findViewById(R.id.chkStatistics);
				preferences		= getSharedPreferences(Common.KEY_PREF, Context.MODE_PRIVATE);
				chkSilence.setChecked(preferences.getBoolean(Suscription.KEY_SILENCED, silenced));
				chkStatistics.setChecked(preferences.getBoolean(Common.KEY_SEND_STATISTICS, sendStatistics));
				Utils.tintColorScreen(this, Common.COLOR_ACTION);
				Clayout			= (CoordinatorLayout) findViewById(R.id.clSnack);

				chkSilence.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
				{
					@Override
					public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
					{
						checkSilence(buttonView);
					}
				});

				if(Common.APP_TEST)
				{
					btnBackup.setVisibility(Button.VISIBLE);
					btnSMS.setVisibility(Button.VISIBLE);
					btnPush.setVisibility(Button.VISIBLE);
					btnPushSMS.setVisibility(Button.VISIBLE);
					btnSimilSMS.setVisibility(Button.VISIBLE);
					btnMediaPush.setVisibility(Button.VISIBLE);
				}
				else
				{
					btnBackup.setVisibility(Button.GONE);
					btnSMS.setVisibility(Button.GONE);
					btnPush.setVisibility(Button.GONE);
					btnPushSMS.setVisibility(Button.GONE);
					btnSimilSMS.setVisibility(Button.GONE);
					btnMediaPush.setVisibility(Button.GONE);
				}

				//Agregado para visualizar la versión actual de la app con link a play store
				String version = getString(R.string.app_name)+ " "+getString(R.string.version_settins)
									+getApplicationContext().getPackageManager().getPackageInfo(getApplicationContext().getPackageName(), 0).versionName;

				if(btnPlayStore != null && StringUtils.isNotEmpty(version))
				{
					btnPlayStore.setText(version);
				}
			}
		}
		catch(Exception e)
		{
			Utils.logError(this, getLocalClassName()+":onResume - Exception:", e);
		}
	}

	public void goPlayStore(View v)
	{
		//Agregado para capturar evento en Google Analytics
		GoogleAnalytics.getInstance(this).newTracker(Common.HASH_GOOGLEANALYTICS).send(	new HitBuilders.EventBuilder().setCategory("Ajustes").setAction("Playstore")
																						.setLabel("AccionUser").build());

		try
		{
			startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + getPackageName())));
		}
		catch(Exception e)
		{
			startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + getPackageName())));
		}

		finish();
	}

	public void goBusiness(View v)
	{
		try
		{
			//Agregado para capturar evento en Google Analytics
			GoogleAnalytics.getInstance(this).newTracker(Common.HASH_GOOGLEANALYTICS).send(	new HitBuilders.EventBuilder().setCategory("Ajustes").setAction("Business")
																							.setLabel("AccionUser").build());
			startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(ApiConnection.BUSINESS)));
		}
		catch(Exception e)
		{
			Utils.logError(this, getLocalClassName()+":goBusiness - Exception:", e);
		}

		finish();
	}

	public void clickTxtSilence(View v)
	{
		chkSilence.setChecked(!chkSilence.isChecked());
	}

	public void checkSilence(View view)
	{
		try
		{
			final SharedPreferences.Editor editor	= preferences.edit();
			final String active						= getString(R.string.silence_actived);
			final String desactive					= getString(R.string.silence_desactived);
			Snackbar snackBar;

			if(chkSilence.isChecked())
			{
				//Agregado para capturar evento en Google Analytics
				GoogleAnalytics.getInstance(this).newTracker(Common.HASH_GOOGLEANALYTICS).send(	new HitBuilders.EventBuilder().setCategory("Ajustes").setAction("SilenciarOn")
																								.setLabel("AccionUser").build());
				editor.putBoolean(Suscription.KEY_SILENCED, true);
				editor.apply();
				task = new UpdateSilence(Common.BOOL_YES);
				task.start();

				snackBar = Snackbar.make(Clayout, active, Snackbar.LENGTH_LONG).setAction(getString(R.string.undo), new View.OnClickListener()
				{
					@Override
					public void onClick(View v)
					{
						editor.putBoolean(Suscription.KEY_SILENCED, false);
						editor.apply();
						task = new UpdateSilence(Common.BOOL_NO);
						task.start();
						clickTxtSilence(v);
					}
				});
			}
			else
			{
				//Agregado para capturar evento en Google Analytics
				GoogleAnalytics.getInstance(this).newTracker(Common.HASH_GOOGLEANALYTICS).send(	new HitBuilders.EventBuilder().setCategory("Ajustes").setAction("SilenciarOff")
																								.setLabel("AccionUser").build());
				editor.putBoolean(Suscription.KEY_SILENCED, false);
				editor.apply();
				task = new UpdateSilence(Common.BOOL_NO);
				task.start();

				snackBar = Snackbar.make(Clayout, desactive, Snackbar.LENGTH_LONG).setAction(getString(R.string.undo), new View.OnClickListener()
				{
					@Override
					public void onClick(View v)
					{
						editor.putBoolean(Suscription.KEY_SILENCED, true);
						editor.apply();
						task = new UpdateSilence(Common.BOOL_YES);
						task.start();
						clickTxtSilence(v);
					}
				});
			}

			Utils.setStyleSnackBar(snackBar, getApplicationContext());
		}
		catch(Exception e)
		{
			Utils.logError(this, getLocalClassName()+":checkSilence - Exception:", e);
		}
	}

	public void checkStatistics(View view)
	{
		try
		{
			SharedPreferences.Editor editor = preferences.edit();

			if(chkStatistics.isChecked())
			{
				chkStatistics.setChecked(false);
				editor.putBoolean(Common.KEY_SEND_STATISTICS, false);
			}
			else
			{
				chkStatistics.setChecked(true);
				editor.putBoolean(Common.KEY_SEND_STATISTICS, true);
			}

			editor.apply();
		}
		catch(Exception e)
		{
			Utils.logError(this, getLocalClassName()+":checkStatistics - Exception:", e);
		}
	}

	public void backup(View view)
	{
		try
		{
			//Envía email con interacción del usuario y la db adjuntada
			Utils.sendMail(SettingsActivity.this, false);
		}
		catch(Exception e)
		{
			Utils.logError(this, getLocalClassName()+":backup - Exception:", e);
		}
	}

	public void reImport(View view)
	{
		try
		{
			SharedPreferences.Editor editor	= preferences.edit();
			editor.putBoolean(Common.KEY_PREF_CAPTURED, false);
			editor.apply();
			new CaptureSMSAsyncTask(SettingsActivity.this, false).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
		}
		catch(Exception e)
		{
			Utils.logError(this, getLocalClassName()+":reImport - Exception:", e);
		}
	}

	public void sendPush(View view)
	{
		try
		{
			//Modificación se traslado a Utils para invocarlo también desde IncomingSmsService
			Message message = new Message();
			message.setCompanyId("561e659f34dea37a1dc7389f");
			message.setCountryCode(preferences.getString(Land.KEY_API, ""));
			message.setCreated(System.currentTimeMillis());
			Utils.showPush(getApplicationContext(), preferences.getString(User.KEY_PHONE, ""), String.valueOf(MyFirebaseMessagingService.PUSH_NORMAL), message);
		}
		catch(Exception e)
		{
			Utils.logError(this, getLocalClassName()+":sendPush - Exception:", e);
		}
	}

	//Agregado para pruebas de notificación sobre recepción de sms
	public void sendPushSMS(View view)
	{
		try
		{
			Message message = new Message();
			message.setCompanyId("561e659f34dea37a1dc7389f");
			message.setCreated(System.currentTimeMillis());
			Utils.showPush(getApplicationContext(), preferences.getString(User.KEY_PHONE, ""), String.valueOf(MyFirebaseMessagingService.PUSH_WITHOUT_SOUND), message);
		}
		catch(Exception e)
		{
			Utils.logError(this, getLocalClassName()+":sendPushSMS - Exception:", e);
		}
	}

	public void sendMediaPush(View view)
	{
		try
		{
			List<String> listItems = new ArrayList<>();
			listItems.add("Texto");
			listItems.add("Imagen");
			listItems.add("Factura");
			listItems.add("Descarga");
			listItems.add("Web");
			listItems.add("Mapa");
			listItems.add("Video");
			listItems.add("Rating");
			listItems.add("Audio");
			listItems.add("Twitter");
			listItems.add("Twitter with Image");
			listItems.add("Facebook");
			listItems.add("Facebook with Image");
			CharSequence items[];
			items = listItems.toArray(new CharSequence[listItems.size()]);
			new MaterialDialog.Builder(this)
				.title(R.string.send_push)
				.items(items)
				.itemsCallback(new MaterialDialog.ListCallback()
				{
					@Override
					public void onSelection(MaterialDialog dialog, View view, int which, CharSequence text)
					{
						Message message = new Message();
						message.setCompanyId("561e659f34dea37a1dc7389f");
						message.setKind(which);
						message.setCreated(System.currentTimeMillis());

						switch(which)
						{
							case Message.KIND_TEXT:
								message.setMsg("COMPRA en VOYENBUS.COM/BUS TICKETS U$S49,56 Ctas.1 28/11/2016.Disp. 1/P $ 42.377,79 Neva $ 42.377,79. Descarga la app: https://goo.gl/9W6jN4");
							break;

							case Message.KIND_IMAGE:
								message.setLink("http://i.blogs.es/d63bea/8538679708_906ab6a815_o/original.jpg");
							break;

							case Message.KIND_INVOICE:
								message.setMsg("Servicio SMS de Tarjeta Nevada. Resumen a Pagar con vto. 10/12/2016. 1 P. $17731,76. Neva $0. SEUO");
								message.setLink("https://apps.tarjetanevada.com.ar/ImpresionResumen/resumen?titular=28844711&periodo=1&server=resujava");
							break;

							case Message.KIND_FILE_DOWNLOADABLE:
								message.setLink("http://www.fiat.com.ar/Download.ashx?t=ModelDownload&i=2802");
							break;

							case Message.KIND_LINKWEB:
								message.setLink("http://tween.com.ar/?lang=en");
							break;

							case Message.KIND_LINKMAP:
								message.setLink("https://www.google.com.ar/maps/place/TweenCo+Work/@-32.867278,-68.853549,15z/data=!4m5!3m4!1s0x0:0xeee579b0091db087!8m2!3d-32.867278!4d-68.853549");
							break;

							case Message.KIND_VIDEO:
								message.setLink("https://www.youtube.com/watch?v=YXsHuj6SIVQ");
								message.setLinkThumbnail("http://i.blogs.es/d63bea/8538679708_906ab6a815_o/original.jpg");
							break;

							case Message.KIND_TWITTER:
							case Message.KIND_FACEBOOK:
								message.setSubMsg("Publicado el 19/05/2016");
							break;

							case Message.KIND_TWITTER_IMAGE:
							case Message.KIND_FACEBOOK_IMAGE:
								message.setLink("http://i.blogs.es/d63bea/8538679708_906ab6a815_o/original.jpg");
								message.setSubMsg("Publicado el 19/05/2016");
							break;
						}

						Utils.showPush(getApplicationContext(), preferences.getString(User.KEY_PHONE, ""), String.valueOf(MyFirebaseMessagingService.PUSH_NORMAL), message);
					}
				}).show();
		}
		catch(Exception e)
		{
			Utils.logError(this, getLocalClassName()+":sendMediaPush - Exception:", e);
		}
	}

	//Agregado para pruebas de notificación sobre recepción de sms
	@SuppressWarnings("unchecked")
	public void sendSimilSMS(View view)
	{
		//Agregado para capturar excepciones
		try
		{
			Context context					= getApplicationContext();
			String sender					= "22626";
			String body						= "Nuevo mensaje de ICBC";
			byte[] scBytes					= PhoneNumberUtils.networkPortionToCalledPartyBCD("0000000000");
			byte[] senderBytes				= PhoneNumberUtils.networkPortionToCalledPartyBCD(sender);
			int lsmcs						= scBytes.length;
			byte[] dateBytes				= new byte[7];
			Calendar calendar				= new GregorianCalendar();
			dateBytes[0]					= Utils.reverseByte((byte) (calendar.get(Calendar.YEAR)));
			dateBytes[1]					= Utils.reverseByte((byte) (calendar.get(Calendar.MONTH) + 1));
			dateBytes[2]					= Utils.reverseByte((byte) (calendar.get(Calendar.DAY_OF_MONTH)));
			dateBytes[3]					= Utils.reverseByte((byte) (calendar.get(Calendar.HOUR_OF_DAY)));
			dateBytes[4]					= Utils.reverseByte((byte) (calendar.get(Calendar.MINUTE)));
			dateBytes[5]					= Utils.reverseByte((byte) (calendar.get(Calendar.SECOND)));
			dateBytes[6]					= Utils.reverseByte((byte) ((calendar.get(Calendar.ZONE_OFFSET) + calendar.get(Calendar.DST_OFFSET)) / (60 * 1000 * 15)));
			ByteArrayOutputStream bo		= new ByteArrayOutputStream();
			bo.write(lsmcs);
			bo.write(scBytes);
			bo.write(0x04);
			bo.write((byte) sender.length());
			bo.write(senderBytes);
			bo.write(0x00);
			bo.write(0x00);
			bo.write(dateBytes);
			String sReflectedClassName		= "com.android.internal.telephony.GsmAlphabet";
			Class cReflectedNFCExtras		= Class.forName(sReflectedClassName);
			Method stringToGsm7BitPacked	= cReflectedNFCExtras.getMethod("stringToGsm7BitPacked", String.class);
			stringToGsm7BitPacked.setAccessible(true);
			byte[] bodybytes				= (byte[]) stringToGsm7BitPacked.invoke(null, body);
			bo.write(bodybytes);
			byte[] pdu						= bo.toByteArray();
			Intent intent					= new Intent();
			intent.setAction("android.provider.Telephony.SMS_RECEIVED");
			intent.putExtra("pdus", new Object[] { pdu });
			context.sendBroadcast(intent);
		}
		catch(Exception e)
		{
			Utils.logError(this, getLocalClassName()+":sendSimilSMS - Exception:", e);
		}
	}

	public void logout(View view)
	{
		try
		{
			SharedPreferences.Editor editor	= preferences.edit();
			editor.putString(User.KEY_EMAIL, "");
			editor.putBoolean(Common.KEY_PREF_LOGGED, false);
			editor.putBoolean(Common.KEY_PREF_CHECKED, false);
			editor.putString(Common.KEY_TOKEN, "");
			editor.putBoolean(Suscription.KEY_SILENCED, false);
			editor.putBoolean(Common.KEY_SEND_STATISTICS, false);
			editor.apply();
			Intent intent					= new Intent(getApplicationContext(), PhoneActivity.class);
			startActivity(intent);
			finish();
		}
		catch(Exception e)
		{
			Utils.logError(this, getLocalClassName()+":logout - Exception:", e);
		}
	}

	public void silenceUp()
	{
		try
		{
			timer		= new Timer();
			timerTask	= new TimerTask()
			{
				public void run()
				{
					handler.post(new Runnable()
					{
						public void run()
						{
							SharedPreferences.Editor editor = preferences.edit();
							editor.putBoolean(Suscription.KEY_SILENCED, false);
							editor.apply();
							silenceDown();
						}
					});
				}
			};

			if(Common.DEBUG)
			{
				timer.schedule(timerTask, 20000, 1000);
			}
			else
			{
				timer.schedule(timerTask, forEightHours, 1000);
			}
		}
		catch(Exception e)
		{
			Utils.logError(this, getLocalClassName()+":silenceUp - Exception:", e);
		}
	}

	public void silenceDown()
	{
		if(timer != null)
		{
			timer.cancel();
			timer = null;
		}
	}

	public void goContact(View view)
	{
		try
		{
			//Agregado para capturar evento en Google Analytics
			GoogleAnalytics.getInstance(this).newTracker(Common.HASH_GOOGLEANALYTICS).send(	new HitBuilders.EventBuilder().setCategory("Ajustes").setAction("Contacto")
																							.setLabel("AccionUser").build());
			//Envía email con interacción del usuario y la db adjuntada
			Utils.sendContactMail(SettingsActivity.this);
		}
		catch(Exception e)
		{
			Utils.logError(this, getLocalClassName()+":goContact - Exception:", e);
		}
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
	public void onBackPressed()
	{
		try
		{
			Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
			intent.putExtra(Common.KEY_REFRESH, false);
			startActivity(intent);
			finish();
		}
		catch(Exception e)
		{
			Utils.logError(this, getLocalClassName()+":onBackPressed - Exception:", e);
		}
	}

	/**
	 * Proceso para silenciar o quitar silencio a Companies en Realm
	 */
	public class UpdateSilence extends Thread
	{
		private int	flag;

		public UpdateSilence(int flag)
		{
			this.flag	= flag;
		}

		public void start()
		{
			if(Looper.myLooper() == null)
			{
				Looper.prepare();
			}

			try
			{
				Realm realm	= Realm.getDefaultInstance();
				realm.executeTransaction(new Realm.Transaction()
				{
					@Override
					public void execute(Realm bgRealm)
					{
						RealmResults<Suscription> results = bgRealm.where(Suscription.class).findAll();

						for(int i = results.size() - 1; i >= 0; i--)
						{
							results.get(i).setSilenced(flag);
						}
					}
				});
			}
			catch(Exception e)
			{
				Utils.logError(getApplicationContext(), "SettingsActivity:UpdateSilence:start - Exception:", e);
			}
		}
	}
}