package com.tween.viacelular.asynctask;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Looper;
import com.afollestad.materialdialogs.MaterialDialog;
import com.tween.viacelular.R;
import com.tween.viacelular.activities.HomeActivity;
import com.tween.viacelular.models.Message;
import com.tween.viacelular.models.Suscription;
import com.tween.viacelular.models.User;
import com.tween.viacelular.services.ApiConnection;
import com.tween.viacelular.utils.Common;
import com.tween.viacelular.utils.StringUtils;
import com.tween.viacelular.utils.Utils;
import org.json.JSONArray;
import org.json.JSONObject;
import io.realm.Realm;
import io.realm.RealmResults;

public class UpdateSuscriptionsAsyncTask extends AsyncTask<Void, Void, String>
{
	private MaterialDialog	progress;
	private Activity		activity;
	private Context			context;
	private boolean			displayDialog	= false;
	private int				suscribe		= Common.BOOL_YES;
	private boolean			needRedirect	= false;
	private UpdateCompany	task;
	private String			companyId		= "";

	public UpdateSuscriptionsAsyncTask(Activity activity, boolean displayDialog, int suscribe, boolean needRedirect, String companyId)
	{
		this.activity		= activity;
		this.displayDialog	= displayDialog;
		this.suscribe		= suscribe;
		this.needRedirect	= needRedirect;
		this.companyId		= companyId;
	}

	public UpdateSuscriptionsAsyncTask(Context context, boolean displayDialog, int suscribe, boolean needRedirect, String companyId)
	{
		this.context		= context;
		this.displayDialog	= displayDialog;
		this.suscribe		= suscribe;
		this.needRedirect	= needRedirect;
		this.companyId		= companyId;
	}

	protected void onPreExecute()
	{
		try
		{
			//Modificación para contemplar contexto sino se llamada desde una activity
			if(activity != null)
			{
				context = activity.getApplicationContext();
			}

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
					.title(R.string.landing_card_loading_header)
					.cancelable(false)
					.content(R.string.landing_card_loading_text)
					.progress(true, 0)
					.show();
			}
		}
		catch(Exception e)
		{
			Utils.logError(activity, "UpdateSuscriptionsAsyncTask:onPreExecute - Exception:", e);
		}
	}

	@Override
	protected String doInBackground(Void... params)
	{
		String result = ApiConnection.OK;

		try
		{
			SharedPreferences preferences	= context.getSharedPreferences(Common.KEY_PREF, Context.MODE_PRIVATE);
			Realm realm						= Realm.getDefaultInstance();
			User user						= realm.where(User.class).equalTo(User.KEY_API, preferences.getString(User.KEY_API, "")).findFirst();

			if(user != null)
			{
				if(StringUtils.isIdMongo(user.getUserId()))
				{
					String url			= ApiConnection.MODIFY_COMPANIES;
					url					= url.replace(User.KEY_API, user.getUserId());
					JSONArray jsonArray	= new JSONArray();
					String ids			= "";
					String localIds		= "";

					if(StringUtils.isNotEmpty(companyId))
					{
						Suscription suscription			= realm.where(Suscription.class).equalTo(Suscription.KEY_API, companyId).findFirst();

						if(suscription != null)
						{
							if(StringUtils.isIdMongo(suscription.getCompanyId()))
							{
								JSONObject jsonObject	= new JSONObject();
								jsonObject.put(Suscription.KEY_API, suscription.getCompanyId());
								jsonObject.put(Suscription.KEY_SUSCRIBE, suscribe);
								jsonArray.put(jsonObject);
								ids						= ids+"'"+suscription.getCompanyId()+"',";
							}
							else
							{
								localIds = localIds+"'"+suscription.getCompanyId()+"',";
							}
						}
						else
						{
							System.out.println("company is null");
						}
					}
					else
					{
						//Modificación por migración a Realm
						RealmResults<Message> messages = realm.where(Message.class).distinct(Suscription.KEY_API);

						if(messages.size() > 0)
						{
							for(Message message : messages)
							{
								//Agregado para validar que no se envien las companies fantasmas desconocidas
								if(StringUtils.isIdMongo(message.getCompanyId()))
								{
									JSONObject jsonObject	= new JSONObject();
									jsonObject.put(Suscription.KEY_API, message.getCompanyId());
									jsonObject.put(Suscription.KEY_SUSCRIBE, suscribe);
									jsonArray.put(jsonObject);
									ids = ids+"'"+message.getCompanyId()+"',";
								}
								else
								{
									localIds = localIds+"'"+message.getCompanyId()+"',";
								}
							}
						}
					}

					if(jsonArray.length() > 0)
					{
						ids						= ids.substring(0, ids.length() - 1);
						modifySubscriptions(ids, suscribe);
						JSONObject jsonResult	= new JSONObject(ApiConnection.request(url, context, ApiConnection.METHOD_PUT, preferences.getString(Common.KEY_TOKEN, ""), jsonArray.toString()));
						result					= ApiConnection.checkResponse(context, jsonResult);
						result					= ApiConnection.OK;

						if(!result.equals(ApiConnection.OK))
						{
							modifySubscriptions(ids, Utils.reverseBool(suscribe));
						}
					}
					else
					{
						if(!needRedirect && suscribe == Common.BOOL_YES)
						{
							JSONObject jsonObject	= new JSONObject();
							jsonObject.put(Suscription.KEY_API, Suscription.COMPANY_ID_VC_MONGO);
							jsonObject.put(Suscription.KEY_SUSCRIBE, suscribe);
							jsonArray.put(jsonObject);
							modifySubscriptions(Suscription.COMPANY_ID_VC_MONGO, suscribe);
							JSONObject jsonResult	= new JSONObject(ApiConnection.request(url, context, ApiConnection.METHOD_PUT, preferences.getString(Common.KEY_TOKEN, ""), jsonArray.toString()));
							result					= ApiConnection.checkResponse(context, jsonResult);
							result					= ApiConnection.OK;
						}
						else
						{
							//Agregado para suscribir companies desconocidas sin reportar a api
							if(localIds.length() > 0)
							{
								modifySubscriptions(localIds, suscribe);
							}
						}
					}

					//Guardar la fecha de última actualización
					SharedPreferences.Editor editor = preferences.edit();
					editor.putLong(Common.KEY_PREF_TSSUBSCRIPTIONS, System.currentTimeMillis());
					editor.apply();
				}
			}
		}
		catch(Exception e)
		{
			Utils.logError(activity, "UpdateSuscriptionsAsyncTask:doInBackground - Exception:", e);
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
						if(task != null)
						{
							if(!task.isAlive())
							{
								progress.cancel();
							}
						}
					}
				}
			}

			if(result.equals(ApiConnection.OK))
			{
				if(needRedirect)
				{
					Intent intent = new Intent(context, HomeActivity.class);
					intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

					if(StringUtils.isEmpty(companyId))
					{
						intent.putExtra(Common.KEY_REFRESH, true);
						intent.putExtra(Common.KEY_PREF_WELCOME, true);
					}
					else
					{
						intent.putExtra(Common.KEY_REFRESH, false);
					}

					context.startActivity(intent);

					if(activity != null)
					{
						activity.finish();
					}
				}
			}
		}
		catch(Exception e)
		{
			Utils.logError(activity, "UpdateSuscriptionsAsyncTask:onPostExecute - Exception:", e);
		}
	}

	private boolean modifySubscriptions(final String ids, final int flag)
	{
		boolean result	= true;

		try
		{
			if(StringUtils.isNotEmpty(ids))
			{
				task = new UpdateCompany(ids, flag);
				task.start();
			}
		}
		catch(Exception e)
		{
			Utils.logError(activity, "UpdateSuscriptionsAsyncTask:modifySubscriptions - Exception:", e);
			result = false;
		}

		return result;
	}

	public class UpdateCompany extends Thread
	{
		private String	ids;
		private int		flag;

		public UpdateCompany(String ids, int flag)
		{
			this.ids	= ids;
			this.flag	= flag;
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
				Realm realm	= Realm.getDefaultInstance();
				realm.executeTransaction(new Realm.Transaction()
				{
					@Override
					public void execute(Realm bgRealm)
					{
						RealmResults<Suscription> results = bgRealm.where(Suscription.class).findAll();

						for(int i = results.size() -1; i >=0; i--)
						{
							if(ids.contains(results.get(i).getCompanyId()))
							{
								results.get(i).setFollower(flag);
								results.get(i).setBlocked(Utils.reverseBool(flag));

								if(flag == Common.BOOL_NO)
								{
									results.get(i).setDataSent(flag);
								}
								else
								{
									results.get(i).setGray(Common.BOOL_NO);
								}
							}
						}
					}
				});
			}
			catch(Exception e)
			{
				Utils.logError(activity, "UpdateSuscriptionsAsyncTask:UpdateCompany:start - Exception:", e);
			}
		}
	}
}