package com.tween.viacelular.asynctask;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import com.afollestad.materialdialogs.MaterialDialog;
import com.tween.viacelular.R;
import com.tween.viacelular.models.Isp;
import com.tween.viacelular.models.Land;
import com.tween.viacelular.models.Message;
import com.tween.viacelular.models.MessageHelper;
import com.tween.viacelular.models.Migration;
import com.tween.viacelular.models.Suscription;
import com.tween.viacelular.models.User;
import com.tween.viacelular.services.ApiConnection;
import com.tween.viacelular.utils.Common;
import com.tween.viacelular.utils.StringUtils;
import com.tween.viacelular.utils.Utils;
import io.realm.Realm;
import io.realm.RealmResults;

public class SplashAsyncTask extends AsyncTask<Void, Void, String>
{
	private MaterialDialog		progress;
	private Activity			activity;
	private boolean				splashed		= false;
	private SharedPreferences	preferences;
	private boolean				displayDialog	= false;

	public SplashAsyncTask(Activity activity, boolean displayDialog)
	{
		this.activity		= activity;
		this.displayDialog	= displayDialog;
	}

	protected void onPreExecute()
	{
		try
		{
			preferences	= activity.getApplicationContext().getSharedPreferences(Common.KEY_PREF, Context.MODE_PRIVATE);
			splashed	= preferences.getBoolean(Common.KEY_PREF_SPLASHED, false);

			if(displayDialog)
			{
				if(progress != null)
				{
					if(progress.isShowing())
					{
						progress.cancel();
					}
				}

				progress = new MaterialDialog.Builder(activity)
					.title(R.string.progress_dialog)
					.cancelable(false)
					.content(R.string.please_wait)
					.progress(true, 0)
					.show();
			}

			Migration.getDB(activity);

			if(!splashed)
			{
				new ReadAccountsAsyncTask(activity, false).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
			}
		}
		catch(Exception e)
		{
			System.out.println("SplashAsyncTask - Exception: " + e);

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
			if(!splashed)
			{
				//Modificaciones para contemplar migración a Realm
				SharedPreferences.Editor editor	= preferences.edit();
				editor.putBoolean(Common.KEY_PREF_SPLASHED, true);

				//Agregado para saltear la pantalla Welcome
				editor.putBoolean(Common.KEY_PREF_WELCOME, true);
				editor.putInt(Common.KEY_LAST_MSGID, 2);
				editor.apply();

				//Agregado para migrar a objeto Realm Message
				Realm realm								= Realm.getDefaultInstance();
				final RealmResults<Isp> ispRealmResults	= realm.where(Isp.class).findAll();
				User user								= realm.where(User.class).findFirst();
				String country							= preferences.getString(Land.KEY_API, "");

				//Agregado para incorporar país en "push" iniciales
				if(user != null)
				{
					if(StringUtils.isNotEmpty(user.getCountryCode()))
					{
						country = user.getCountryCode();
					}
				}

				final String finalCountry = country;
				realm.executeTransaction(new Realm.Transaction()
				{
					@Override
					public void execute(Realm realm)
					{
						Message messageRealm = new Message(	"1", activity.getString(R.string.welcome_notification), activity.getString(R.string.welcome_text),
															activity.getString(R.string.app_name), Message.STATUS_RECEIVE, "", finalCountry, Message.FLAGS_PUSH,
															System.currentTimeMillis(), Common.BOOL_NO, Message.KIND_TEXT, "", "", "", "", "", Suscription.COMPANY_ID_VC_MONGO);
						Message messageRealm1 = new Message("2", activity.getString(R.string.you_have), activity.getString(R.string.you_have_text),
															activity.getString(R.string.app_name), Message.STATUS_RECEIVE, "", finalCountry, Message.FLAGS_PUSH,
															System.currentTimeMillis(), Common.BOOL_NO, Message.KIND_TEXT, "", "", "", "", "", Suscription.COMPANY_ID_VC_MONGO);
						realm.copyToRealmOrUpdate(messageRealm);
						realm.copyToRealmOrUpdate(messageRealm1);
						ispRealmResults.deleteAllFromRealm();
						Suscription suscription = realm.where(Suscription.class).equalTo(Suscription.KEY_API, Suscription.COMPANY_ID_VC_MONGO).findFirst();

						if(suscription != null)
						{
							suscription.getMessages().add(messageRealm);
						}
					}
				});
			}
			else
			{
				MessageHelper.updateCountry(preferences.getString(Land.KEY_API, ""));
			}

			result = ApiConnection.OK;
		}
		catch(Exception e)
		{
			System.out.println("SplashAsyncTask - Exception: " + e);

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

			Utils.checkSesion(activity, Common.SPLASH_SCREEN);
		}
		catch(Exception e)
		{
			System.out.println("SplashAsyncTask - Exception: " + e);

			if(Common.DEBUG)
			{
				e.printStackTrace();
			}
		}

		super.onPostExecute(result);
	}
}