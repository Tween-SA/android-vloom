package com.tween.viacelular.utils;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Rect;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Looper;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.telephony.TelephonyManager;
import android.view.TouchDelegate;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.readystatesoftware.systembartint.SystemBarTintManager;
import com.tween.viacelular.R;
import com.tween.viacelular.activities.CodeActivity;
import com.tween.viacelular.activities.HomeActivity;
import com.tween.viacelular.activities.PhoneActivity;
import com.tween.viacelular.activities.SettingsActivity;
import com.tween.viacelular.activities.SuscriptionsActivity;
import com.tween.viacelular.asynctask.GetLocationAsyncTask;
import com.tween.viacelular.asynctask.MigrationAsyncTask;
import com.tween.viacelular.asynctask.SplashAsyncTask;
import com.tween.viacelular.asynctask.UpdateUserAsyncTask;
import com.tween.viacelular.data.DaoMaster;
import com.tween.viacelular.interfaces.CallBackListener;
import com.tween.viacelular.models.Isp;
import com.tween.viacelular.models.Land;
import com.tween.viacelular.models.Message;
import com.tween.viacelular.models.Suscription;
import com.tween.viacelular.models.User;
import com.tween.viacelular.services.MyFirebaseMessagingService;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import io.realm.Realm;

/**
 * Created by Davo on 11/06/2015.
 */
public class Utils
{
	public static final String path2Copy = Environment.getExternalStorageDirectory().getPath()+"/".replace("//", "/");//"/sdcard/";

	//Cambio de contexto para redirigir desde el menú
	public static void redirectMenu(Activity activity, int position, int current)
	{
		try
		{
			if(position != 0)
			{
				Intent intent = null;

				switch(position)
				{
					case 1:
						intent = new Intent(activity, HomeActivity.class);
						intent.putExtra(Common.KEY_TITLE, activity.getString(R.string.title_notifications));
						intent.putExtra(Common.KEY_SECTION, position);
						intent.putExtra(Common.KEY_REFRESH, false);
					break;

					case 2:
						//Agregado para prevenir casos en que no se actualizaron las suscripciones
						Realm realm = Realm.getDefaultInstance();

						if(realm.where(Suscription.class).equalTo(Suscription.KEY_FOLLOWER, Common.BOOL_YES).count() == 0)
						{
							new UpdateUserAsyncTask(activity, Common.BOOL_YES, true, "", true, false).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
						}
						else
						{
							//Agregado para limitar frecuencia de actualización
							SharedPreferences preferences	= activity.getSharedPreferences(Common.KEY_PREF, Context.MODE_PRIVATE);
							long tsUpated					= preferences.getLong(Common.KEY_PREF_TSSUBSCRIPTIONS, System.currentTimeMillis());

							if(DateUtils.needUpdate(tsUpated, DateUtils.HIGH_FREQUENCY))
							{
								//Se modifica para reemplazar la pantalla Bloquedas por la pantalla Empresas con tab
								new UpdateUserAsyncTask(activity, Common.BOOL_YES, true, "", true, false).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
							}
							else
							{
								intent = new Intent(activity, SuscriptionsActivity.class);
								intent.putExtra(Common.KEY_TITLE, activity.getString(R.string.title_companies));
								intent.putExtra(Common.KEY_SECTION, position);
							}
						}
					break;

					case 3:
						//Agregado para capturar evento en Google Analytics, se incorpora la opción "no quiero ver más esto" que hace lo mismo que marcar como spam por el momento
						GoogleAnalytics.getInstance(activity).newTracker(Common.HASH_GOOGLEANALYTICS).send(	new HitBuilders.EventBuilder().setCategory("Ajustes").setAction("Entrar")
																											.setLabel("Accion_user").build());
						intent = new Intent(activity, SettingsActivity.class);
						intent.putExtra(Common.KEY_TITLE, activity.getString(R.string.title_settings));
						intent.putExtra(Common.KEY_SECTION, position);
					break;
				}

				if(position != current && intent != null)
				{
					intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					activity.startActivity(intent);
				}
			}
		}
		catch(Exception e)
		{
			System.out.println("Utils:redirectMenu - Exception: " + e);

			if(Common.DEBUG)
			{
				e.printStackTrace();
			}
		}
	}

	/**
	 * Genera una notificación push local avisando la llegada de un sms o push simulada desde la pantalla Ajustes en modo de Appbeta
	 * @param context
	 * @param from
	 * @param sound
	 * @param message
	 */
	public static void showPush(Context context, String from, String sound, Message message)
	{
		try
		{
			MyFirebaseMessagingService push	= new MyFirebaseMessagingService();
			push.setContext(context);
			Bundle bundle				= new Bundle();
			bundle.putString(Common.KEY_SOUND, sound);
			bundle.putString(Common.KEY_TYPE, message.getType());
			bundle.putString(Message.KEY_PLAYLOAD, message.getMsg());
			bundle.putString(Message.KEY_TIMESTAMP, String.valueOf(message.getCreated()));
			bundle.putString(Message.KEY_CHANNEL, message.getChannel());
			bundle.putString(Common.KEY_STATUS, String.valueOf(message.getStatus()));
			bundle.putString(Suscription.KEY_API, message.getCompanyId());
			bundle.putString(User.KEY_PHONE, message.getPhone());
			bundle.putString(Message.KEY_TTD, String.valueOf(0));
			bundle.putString(Land.KEY_API, message.getCountryCode());
			bundle.putString(Message.KEY_FLAGS, message.getFlags());

			//Campos nuevos para push multimedia
			bundle.putInt(Message.KEY_KIND, message.getKind());
			bundle.putString(Message.KEY_LINK, message.getLink());
			bundle.putString(Message.KEY_LINKTHUMB, message.getLinkThumbnail());
			bundle.putString(Message.KEY_SUBMSG, message.getSubMsg());
			bundle.putString(Message.KEY_CAMPAIGNID, message.getCampaignId());
			bundle.putString(Message.KEY_LISTID, message.getListId());
			push.onOldPush(from, bundle);
		}
		catch(Exception e)
		{
			System.out.println("Utils:showPush - Exception: " + e);

			if(Common.DEBUG)
			{
				e.printStackTrace();
			}
		}
	}

	/**
	 * <h1>Checkeo de Sesión</h1>
	 * <p>Verifica si hay usuario logueado y verificado para redireccionar</p>
	 *
	 * @param activity Contexto
	 * @param pantalla donde se realiza el control - 0 selección de tarjeta, 1 registro, 2 codigo, 3 slider
	 * @return void Redirecciona según verificación
	 */
	public static boolean checkSesion(Activity activity, int pantalla)
	{
		boolean result = true;

		try
		{
			SharedPreferences preferences	= activity.getApplicationContext().getSharedPreferences(Common.KEY_PREF, Context.MODE_PRIVATE);
			boolean logged					= preferences.getBoolean(Common.KEY_PREF_LOGGED, false);
			boolean checked					= preferences.getBoolean(Common.KEY_PREF_CHECKED, false);
			boolean freePassOn				= preferences.getBoolean(Common.KEY_PREF_FREEPASS, false);
			Intent intent;

			switch(pantalla)
			{
				case Common.SPLASH_SCREEN:
					if(logged && checked)
					{
						//Agregado para limitar frecuencia de actualización
						long tsUpated = preferences.getLong(Common.KEY_PREF_TSUSER, System.currentTimeMillis());

						if(DateUtils.needUpdate(tsUpated, DateUtils.LOW_FREQUENCY))
						{
							//Agregado para actualizar datos del usuario solamente cuando inicia la app
							new UpdateUserAsyncTask(activity, Common.BOOL_YES, false, "", true, true).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
						}

						intent	= new Intent(activity, HomeActivity.class);
						intent.putExtra(Common.KEY_REFRESH, false);
						activity.startActivity(intent);
						activity.finish();
						result	= false;
					}
					else
					{
						if(logged)
						{
							intent	= new Intent(activity, CodeActivity.class);
							activity.startActivity(intent);
							activity.finish();
							result	= false;
						}
						else
						{
							//Modificaciones para contemplar migración a Realm
							intent	= new Intent(activity, PhoneActivity.class);
							activity.startActivity(intent);
							activity.finish();
							result	= false;
						}
					}
				break;

				case Common.PHONE_SCREEN:
					if(logged && checked)
					{
						intent	= new Intent(activity, HomeActivity.class);
						intent.putExtra(Common.KEY_REFRESH, false);
						activity.startActivity(intent);
						activity.finish();
						result	= false;
					}
					else
					{
						if(logged)
						{
							intent	= new Intent(activity, CodeActivity.class);
							activity.startActivity(intent);
							activity.finish();
							result	= false;
						}
					}
				break;

				case Common.CODE_SCREEN:
					if(!freePassOn)
					{
						if(logged && checked)
						{
							intent	= new Intent(activity, HomeActivity.class);
							intent.putExtra(Common.KEY_REFRESH, false);
							activity.startActivity(intent);
							activity.finish();
							result	= false;
						}
						/*else
						{
							if(!logged)
							{
								intent	= new Intent(activity, PhoneActivity.class);
								activity.startActivity(intent);
								activity.finish();
								result	= false;
							}
						}*/
					}
					else
					{
						intent	= new Intent(activity, HomeActivity.class);
						activity.startActivity(intent);
						activity.finish();
						result	= false;
					}
				break;

				default:
					if(!freePassOn)
					{
						if(!logged && !checked)
						{
							//Modificaciones para contemplar migración a Realm
							intent	= new Intent(activity, PhoneActivity.class);
							activity.startActivity(intent);
							activity.finish();
							result	= false;
						}
						else
						{
							if(logged && !checked)
							{
								intent	= new Intent(activity, CodeActivity.class);
								activity.startActivity(intent);
								activity.finish();
								result	= false;
							}
						}
					}
				break;
			}
		}
		catch(Exception e)
		{
			System.out.println("Utils:checkSession - Exception: " + e);

			if(Common.DEBUG)
			{
				e.printStackTrace();
			}
		}

		return result;
	}

	public static String[] getMenu(Context context)
	{
		return new String[]{context.getString(R.string.title_notifications), context.getString(R.string.title_companies), context.getString(R.string.title_settings)};
	}

	public static boolean isLightColor(String colorHex)
	{
		boolean result	= false;

		try
		{
			colorHex		= colorHex.replace("#", "");
			int color		= Integer.parseInt(colorHex, 16);
			int[] rgb		= {Color.red(color), Color.green(color), Color.blue(color)};
			int brightness	= (int)Math.sqrt(rgb[0] * rgb[0] * .241 + rgb[1] * rgb[1] * .691 + rgb[2] * rgb[2] * .068);

			if(brightness >= 215)
			{
				//Si brightness <= 40 puede considerarse como color oscuro
				result = true;
			}
		}
		catch(Exception e)
		{
			System.out.println("Utils:isLightColor - Exception: " + e);

			if(Common.DEBUG)
			{
				e.printStackTrace();
			}
		}

		return result;
	}

	/**
	 * Devuelve el nombre de la operadora según disponibilidad, prefiere Red antes que SIM
	 *
	 * @param context
	 * @return String result
	 */
	public static String getCarrierName(Context context)
	{
		String result = "";
		try
		{
			TelephonyManager manager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

			if(manager != null)
			{
				result = manager.getNetworkOperatorName();

				if(StringUtils.isEmpty(result))
				{
					result = manager.getSimOperatorName();
				}
			}
		}
		catch(Exception e)
		{
			System.out.println("Utils:getCarrierName - Exception: " + e);

			if(Common.DEBUG)
			{
				e.printStackTrace();
			}
		}
		finally
		{
			//Agregado para probar en emuladores
			if(result.toUpperCase().equals("ANDROID"))
			{
				result = "PERSONAL";
			}
		}

		return result;
	}

	/**
	 * Formatea el Carrier para channel de los sms capturados, por defecto devuelve t (Clickatell)
	 * @return String result
	 */
	public static String getChannelSMS(Context context)
	{
		String result = "t";

		try
		{
			result = getCarrierName(context);

			if(StringUtils.isNotEmpty(result))
			{
				result = result.substring(0, 1).toLowerCase();
			}
		}
		catch(Exception e)
		{
			System.out.println("Utils:getChannelSMS - Exception: " + e);

			if(Common.DEBUG)
			{
				e.printStackTrace();
			}
		}

		return result;
	}

	public static void tintColorScreen(Activity activity, String color)
	{
		try
		{
			if(Common.API_LEVEL >= Build.VERSION_CODES.KITKAT)
			{
				activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
				SystemBarTintManager tintManager = new SystemBarTintManager(activity);
				tintManager.setStatusBarTintEnabled(true);

				if(StringUtils.isNotEmpty(color))
				{
					tintManager.setTintColor(Color.parseColor(color));
				}
				else
				{
					tintManager.setTintColor(Color.parseColor(Common.COLOR_ACTION));
				}
			}
		}
		catch(Exception e)
		{
			System.out.println("Utils:tintColorScreen - Exception: " + e);

			if(Common.DEBUG)
			{
				e.printStackTrace();
			}
		}
	}

	/**
	 * Rediregire con intent según parámetro action con el texto recibido
	 * @param activity
	 * @param action
	 * @param extraText
	 */
	public static void goTo(Activity activity, int action, String extraText)
	{
		try
		{
			if(StringUtils.isNotEmpty(extraText))
			{
				Intent intent;

				if(action == 1)
				{
					//Agregado para capturar evento en Google Analytics
					GoogleAnalytics.getInstance(activity).newTracker(Common.HASH_GOOGLEANALYTICS).send(	new HitBuilders.EventBuilder().setCategory("Company").setAction("EmailLanding")
																										.setLabel("AccionUser").build());
					//Envía hacia algún cliente de email la casilla recibida en extraText, se corrige para enviar la dirección al Para:
					intent = new Intent(Intent.ACTION_SEND);
					intent.setType("text/plain");
					intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
					intent.putExtra(Intent.EXTRA_EMAIL, new String[]{extraText});
					activity.startActivity(Intent.createChooser(intent, activity.getString(R.string.mail_chooser)));
				}
				else
				{
					//Agregado para capturar evento en Google Analytics
					GoogleAnalytics.getInstance(activity).newTracker(Common.HASH_GOOGLEANALYTICS).send(	new HitBuilders.EventBuilder().setCategory("Company").setAction("PhoneLanding")
																										.setLabel("AccionUser").build());
					//Envía hacia el cliente de Teléfono el número recibido en extraText
					intent = new Intent(Intent.ACTION_DIAL);
					intent.setData(Uri.parse("tel:" + extraText));
					intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					activity.startActivity(intent);
				}
			}
		}
		catch(Exception e)
		{
			System.out.println("Utils:goTo - Exception: " + e);

			if(Common.DEBUG)
			{
				e.printStackTrace();
			}
		}
	}

	public static void sendContactMail(Activity activity)
	{
		try
		{
			Intent sendIntent	= new Intent(Intent.ACTION_SEND);
			sendIntent.setType("text/plain");
			sendIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
			sendIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{Common.MAIL_TWEEN});
			activity.startActivity(Intent.createChooser(sendIntent, activity.getString(R.string.mail_chooser)));
		}
		catch(Exception e)
		{
			System.out.println("Utils:sendContactMail - Exception: " + e);

			if(Common.DEBUG)
			{
				e.printStackTrace();
			}
		}
	}

	public static void sendMail(Activity activity, boolean send)
	{
		try
		{
			Intent sendIntent	= new Intent(Intent.ACTION_SEND_MULTIPLE);
			sendIntent.setType("*/*");
			sendIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
			sendIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{Common.MAIL_ADDRESSEE});
			copyDb(activity);
			ArrayList<Uri> uris	= new ArrayList<>();
			File file			= new File(path2Copy+"vloomdb.zip");

			if(file.exists())
			{
				uris.add(Uri.parse("file:///sdcard/vloomdb.zip"));
			}

			if(send)
			{
				sendIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
				sendIntent.putExtra(Intent.EXTRA_SUBJECT, createSubject(activity));
				sendIntent.putExtra(Intent.EXTRA_TEXT, createBody(activity));
				sendIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				sendIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
				sendIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				activity.startActivity(Intent.createChooser(sendIntent, activity.getString(R.string.mail_chooser)));
			}
			else
			{
				Toast.makeText(activity, "File is ready", Toast.LENGTH_SHORT).show();
				System.out.println("File is ready");
			}
		}
		catch(Exception e)
		{
			System.out.println("Utils:sendMail - Exception: " + e);

			if(Common.DEBUG)
			{
				e.printStackTrace();
			}
		}
	}

	/**
	 * Escribe la variable convertida a String en un archivo con posibilidad de renombrarlo
	 * @param string
	 */
	public static void writeStringInFile(String string, String fileName)
	{
		try
		{
			if(StringUtils.isEmpty(fileName))
			{
				fileName = "VloomDebug.txt";
			}

			File root = new File(Environment.getExternalStorageDirectory(), "VloomDebug");
			root.mkdirs();
			File gpxfile = new File(root, fileName);
			FileWriter writer = new FileWriter(gpxfile);
			writer.append(System.getProperty("line.separator")+DateUtils.getDateTimePhone()+": "+string);
			writer.flush();
			writer.close();
		}
		catch(Exception e)
		{
			System.out.println("Utils:writeStringInFile - Exception: " + e);

			if(Common.DEBUG)
			{
				e.printStackTrace();
			}
		}
	}

	private static String createSubject(Context context)
	{
		String subject = "";

		try
		{
			String version	= context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
			subject			= context.getString(R.string.send_statistics) + " " + context.getString(R.string.app_name) + " v" + version;
		}
		catch(Exception e)
		{
			System.out.println("Utils:createSubject - Exception: " + e);

			if(Common.DEBUG)
			{
				e.printStackTrace();
			}
		}

		return subject;
	}

	private static String createBody(Context context)
	{
		String body = "";

		try
		{
			String version			= context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
			String db				= "SQLite: "+DaoMaster.SCHEMA_VERSION+" Realm: "+Common.REALMDB_VERSION;
			String androidVersion	= Build.VERSION.RELEASE + " (" + android.os.Build.VERSION.SDK_INT + ")";
			String device			= Build.MANUFACTURER + " " + Build.MODEL;
			String lang				= Locale.getDefault().getDisplayLanguage() + " (" + Locale.getDefault().getLanguage() + ")";
			body					= context.getString(R.string.sub_send_stadistics) + " " + context.getString(R.string.send_mail_text)+context.getString(R.string.mail_date)
										+ " " + DateUtils.getDateTimePhone() + context.getString(R.string.mail_version) + " " + version + "\n* DB: "+ db +
										context.getString(R.string.mail_android) + " " + androidVersion + context.getString(R.string.mail_device) + " " + device
										+context.getString(R.string.mail_lang) + " " + lang;
		}
		catch(Exception e)
		{
			System.out.println("Utils:createBody - Exception: " + e);

			if(Common.DEBUG)
			{
				e.printStackTrace();
			}
		}

		return body;
	}

	private static class PrepareDB extends Thread
	{
		private Activity activity;

		private PrepareDB(final Activity activity)
		{
			this.activity = activity;
		}

		public void start()
		{
			//Agregado para evitar excepciones Runtime
			if(Looper.myLooper() == null)
			{
				Looper.prepare();
			}

			try
			{
				File f = new File(Common.REALMDB_PATH);
				File file[] = f.listFiles();
				for(int i=0; i < file.length; i++)
				{
					System.out.println("Files #"+i + ": "+ file[i].toString());
				}

				f = new File(DaoMaster.DB_PATH);
				File data[] = f.listFiles();
				for(int i=0; i < data.length; i++)
				{
					System.out.println("Data #"+i + ": "+ data[i].toString());
				}

				System.out.println("Path 2 Copy: "+path2Copy);

				Realm realm				= Realm.getDefaultInstance();
				//Agregado para enviar la db nueva en Realm, dejar de considerar la db SQLite
				ArrayList<String> files	= new ArrayList<>();
				String currentDBPath	= realm.getPath();
				String backupDBPath		= path2Copy + Common.REALMDB_NAME;
				File currentDB			= new File(currentDBPath);
				File backupDB			= new File(backupDBPath);
				FileChannel src			= new FileInputStream(currentDB).getChannel();
				FileChannel dst			= new FileOutputStream(backupDB).getChannel();
				dst.transferFrom(src, 0, src.size());
				src.close();
				dst.close();
				System.out.println("Adding file 1: "+backupDBPath);
				files.add(backupDBPath);

				//Agregado para enviar el segundo archivo de la db nueva
				currentDBPath	= realm.getPath()+".lock";
				backupDBPath	= path2Copy + Common.REALMDB_NAME+".lock";
				currentDB		= new File(currentDBPath);
				backupDB		= new File(backupDBPath);
				src				= new FileInputStream(currentDB).getChannel();
				dst				= new FileOutputStream(backupDB).getChannel();
				dst.transferFrom(src, 0, src.size());
				src.close();
				dst.close();
				System.out.println("Adding file 2: "+backupDBPath);
				files.add(backupDBPath);

				//TODO ver si es necesario agregar el archivo /data/data/com.tween.viacelular/files/viacelular.realm.management para eso hay que pasar esto a thread con callback

				System.out.println("Archivos a adjuntar: "+files.size()+" "+files.toString());
				//Agregado para comprimir archivos de db
				if(files.size() > 0)
				{
					BufferedInputStream origin;
					FileOutputStream dest	= new FileOutputStream(path2Copy+"vloomdb.zip");
					ZipOutputStream out		= new ZipOutputStream(new BufferedOutputStream(dest));
					byte dataEmail[]		= new byte[2048];

					for(int i = 0; i < files.size(); i++)
					{
						System.out.println("Comprimiendo: "+files.get(i));
						FileInputStream fi	= new FileInputStream(files.get(i));
						origin				= new BufferedInputStream(fi, 2048);
						ZipEntry entry = new ZipEntry(files.get(i).substring(files.get(i).lastIndexOf("/") + 1));
						out.putNextEntry(entry);
						int count;

						while((count = origin.read(dataEmail, 0, 2048)) != -1)
						{
							out.write(dataEmail, 0, count);
						}

						origin.close();
					}

					out.close();
				}

				//Agregado para copiar a la carpeta Descargas del cel
				currentDBPath	= path2Copy+"vloomdb.zip";
				backupDBPath	= path2Copy+ "Download/vloomdb"+DateUtils.getDateTimePhone().replace("/","").replace(":","").replace(" ", "")+".zip";
				System.out.println("Copiar de: "+currentDBPath+" a: "+backupDBPath);
				currentDB		= new File(currentDBPath);
				backupDB		= new File(backupDBPath);
				src				= new FileInputStream(currentDB).getChannel();
				dst				= new FileOutputStream(backupDB).getChannel();
				dst.transferFrom(src, 0, src.size());
				src.close();
				dst.close();
			}
			catch(Exception e)
			{
				FileWriter fichero;
				PrintWriter pw;

				try
				{
					fichero	= new FileWriter(path2Copy+"LogVloom.txt");
					pw		= new PrintWriter(fichero);
					pw.println(DateUtils.getDatePhone() + " - (thread) ");
				}
				catch(Exception d)
				{
					e.printStackTrace();
					d.printStackTrace();
				}
			}
		}
	}

	private static void copyDb(Activity activity)
	{
		try
		{
			if(ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
			{
				PrepareDB task = new PrepareDB(activity);
				task.start();
			}
		}
		catch(Exception e)
		{
			FileWriter fichero;
			PrintWriter pw;

			try
			{
				fichero	= new FileWriter(path2Copy+"LogVloom.txt");
				pw		= new PrintWriter(fichero);
				pw.println(DateUtils.getDatePhone() + " - (sendMail) ");
			}
			catch(Exception d)
			{
				e.printStackTrace();
				d.printStackTrace();
			}
		}
	}

	/**
	 * Revierte boolean convertido a int
	 * @param bool
	 * @return int
	 */
	public static int reverseBool(int bool)
	{
		if(Common.BOOL_YES == bool)
		{
			return Common.BOOL_NO;
		}
		else
		{
			return Common.BOOL_YES;
		}
	}

	/***
	 * Método para forzar ejecuciones al iniciar un update nuevo, se modifica si es necesario sino se omite
	 * @param activity
	 */
	private static void upgradeApp(Activity activity)
	{
		try
		{
			String version					= activity.getPackageManager().getPackageInfo(activity.getPackageName(), 0).versionName;
			SharedPreferences preferences	= activity.getSharedPreferences(Common.KEY_PREF, Context.MODE_PRIVATE);
			boolean splashed				= preferences.getBoolean(Common.KEY_PREF_SPLASHED, false);
			boolean upgraded				= preferences.getBoolean(Common.KEY_PREF_UPGRADED + version, false);

			//Se quitó if de más ya que siempre se realizará este checkeo de upgrade
			if(!upgraded)
			{
				if(splashed)
				{
					//Si la versión es reciente no hace falta migración de db vieja pero si actualización de Realm
					if(version.equals("1.2.9"))
					{
						SharedPreferences.Editor editor = preferences.edit();
						editor.putBoolean(Common.KEY_PREF_UPGRADED + version, true);
						editor.apply();
						Intent intent = new Intent(activity, HomeActivity.class);
						intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						intent.putExtra(Common.KEY_REFRESH, true);
						intent.putExtra(Common.KEY_PREF_WELCOME, true);
						activity.startActivity(intent);
					}
					else
					{
						//Para apps viejas si es necesaria la migración
						new MigrationAsyncTask(activity, true).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
					}
				}
				else
				{
					//Se movió la llamada de esta task acá para optimizar código
					SharedPreferences.Editor editor = preferences.edit();
					editor.remove(Common.KEY_PREF_UPGRADED + "1.0.6.2");
					editor.remove(Common.KEY_PREF_UPGRADED + "1.0.6.3");
					editor.remove(Common.KEY_PREF_UPGRADED + "1.0.6.4");
					editor.remove(Common.KEY_PREF_UPGRADED + "1.0.6.5");
					editor.putBoolean(Common.KEY_PREF_UPGRADED + version, true);
					//Reiniciar la fecha para mostrar el popup tras cada update de la app
					editor.putLong(Common.KEY_PREF_DATE_1STLAUNCH, System.currentTimeMillis());
					int delayTimes = preferences.getInt(Common.KEY_PREF_DELAY_RATE, 0);

					if(delayTimes == 0)
					{
						editor.putInt(Common.KEY_PREF_DELAY_RATE, 1);
					}

					editor.apply();
					new SplashAsyncTask(activity, false).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
				}
			}
			else
			{
				if(!splashed)
				{
					new SplashAsyncTask(activity, false).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
				}
				else
				{
					Utils.checkSesion(activity, Common.SPLASH_SCREEN);
				}
			}
		}
		catch(Exception e)
		{
			System.out.println("Utils:upgradeApp - Exception: " + e);

			if(Common.DEBUG)
			{
				e.printStackTrace();
			}
		}
	}

	/***
	 * Método para actualizar ubicación de usuario y países
	 * @param activity
	 */
	public static void getLocation(final Activity activity)
	{
		try
		{
			//Agregado para actualizar coordenadas
			Realm realm	= Realm.getDefaultInstance();
			Isp isp		= realm.where(Isp.class).findFirst();

			if(isp != null)
			{
				if(DateUtils.needUpdate(isp.getUpdated(), DateUtils.HIGH_FREQUENCY))
				{
					new GetLocationAsyncTask(activity, false, true, new CallBackListener()
					{
						@Override
						public void callBack()
						{
							activity.runOnUiThread(new Runnable()
							{
								@Override
								public void run()
								{
									upgradeApp(activity);
								}
							});
						}
					}).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
				}
			}
			else
			{
				new GetLocationAsyncTask(activity, false, false, new CallBackListener()
				{
					@Override
					public void callBack()
					{
						activity.runOnUiThread(new Runnable()
						{
							@Override
							public void run()
							{
								upgradeApp(activity);
							}
						});
					}
				}).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
			}
		}
		catch(Exception e)
		{
			System.out.println("Utils:getLocation - Exception: " + e);

			if(Common.DEBUG)
			{
				e.printStackTrace();
			}
		}
	}

	public static void ampliarAreaTouch(final View btnMenu, final int value)
	{
		final View parent = (View) btnMenu.getParent();  // button: the view you want to enlarge hit area
		parent.post(new Runnable()
		{
			public void run()
			{
				final Rect rect = new Rect();
				btnMenu.getHitRect(rect);
				rect.top -= value; // increase top hit area
				rect.left -= value; // increase left hit area
				rect.bottom += value; // increase bottom hit area
				rect.right += value; // increase right hit area
				parent.setTouchDelegate(new TouchDelegate(rect, btnMenu));
			}
		});
	}

	public static void setStyleSnackBar(Snackbar snackBar, Context context)
	{
		if(snackBar != null)
		{
			View snackbarView = snackBar.getView();
			snackbarView.setBackgroundColor(ContextCompat.getColor(context, R.color.snack_gray));
			TextView textView = (TextView) snackbarView.findViewById(android.support.design.R.id.snackbar_text);
			textView.setTextColor(ContextCompat.getColor(context, android.R.color.white));
			snackBar.setActionTextColor(ContextCompat.getColor(context, R.color.action));
			snackBar.show();
		}
	}

	public static void showViewWithFade(View view)
	{
		try
		{
			if(view.getVisibility() != View.VISIBLE)
			{
				view.setVisibility(View.VISIBLE);
				view.setAlpha(0.0f);
				view.animate().translationY(view.getHeight()).alpha(1.0f);
			}
		}
		catch(Exception e)
		{
			System.out.println("Utils:showViewWithFade - Exception: " + e);

			if(Common.DEBUG)
			{
				e.printStackTrace();
			}
		}
	}

	/**
	 * Agrega alpha al color recibido
	 * @param color
	 * @param factor
	 * @return color con alpha
	 */
	public static int adjustAlpha(int color, float factor)
	{
		int alpha	= Math.round(Color.alpha(color) * factor);
		int red		= Color.red(color);
		int green	= Color.green(color);
		int blue	= Color.blue(color);
		return Color.argb(alpha, red, green, blue);
	}

	public static void hideViewWithFade(final View view)
	{
		try
		{
			if(view != null)
			{
				if(view.getVisibility() == View.VISIBLE)
				{
					view.animate().translationY(0).alpha(0.0f).setListener(new AnimatorListenerAdapter()
					{
						@Override
						public void onAnimationEnd(Animator animation)
						{
							super.onAnimationEnd(animation);
							view.setVisibility(View.GONE);
						}
					});
				}
				else
				{
					view.setVisibility(View.GONE);
				}
			}
		}
		catch(Exception e)
		{
			System.out.println("Utils:hideViewWithFade - Exception: " + e);

			if(Common.DEBUG)
			{
				e.printStackTrace();
			}
		}
	}

	public static byte reverseByte(byte b)
	{
		return (byte) ((b & 0xF0) >> 4 | (b & 0x0F) << 4);
	}
}