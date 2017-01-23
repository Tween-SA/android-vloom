package com.tween.viacelular.asynctask;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Looper;
import com.afollestad.materialdialogs.MaterialDialog;
import com.tween.viacelular.R;
import com.tween.viacelular.models.Isp;
import com.tween.viacelular.models.Message;
import com.tween.viacelular.models.Migration;
import com.tween.viacelular.models.Suscription;
import com.tween.viacelular.models.SuscriptionHelper;
import com.tween.viacelular.services.ApiConnection;
import com.tween.viacelular.utils.Common;
import com.tween.viacelular.utils.DateUtils;
import com.tween.viacelular.utils.StringUtils;
import com.tween.viacelular.utils.Utils;
import org.json.JSONArray;
import org.json.JSONObject;
import io.realm.Realm;
import io.realm.RealmResults;

/**
 * Created by david.figueroa on 17/6/15.
 */
public class ConfirmReadingAsyncTask extends AsyncTask<Void, Void, String>
{
	private MaterialDialog	progress;
	private Activity		activity;
	private Context			context;
	private boolean			displayDialog	= false;
	private String			companyId		= "";
	private String			msgId			= "";
	private int				status			= Message.STATUS_RECEIVE;

	public ConfirmReadingAsyncTask(Context context, boolean displayDialog, String companyId, String msgId, int status)
	{
		this.activity		= null;
		this.context		= context;
		this.displayDialog	= displayDialog;
		this.companyId		= companyId;
		this.msgId			= msgId;
		this.status			= status;
	}

	public ConfirmReadingAsyncTask(boolean displayDialog, String companyId, String msgId, int status, Activity activity)
	{
		this.activity		= activity;
		this.context		= null;
		this.displayDialog	= displayDialog;
		this.companyId		= companyId;
		this.msgId			= msgId;
		this.status			= status;
	}

	protected void onPreExecute()
	{
		try
		{
			if(context == null)
			{
				context = activity;
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
					.title(R.string.progress_dialog)
					.cancelable(false)
					.content(R.string.please_wait)
					.progress(true, 0)
					.show();
			}

			Migration.getDB(context);

			//Reportar coordenadas
			if(StringUtils.isIdMongo(msgId) && status < Message.STATUS_SPAM)
			{
				Realm realm	= Realm.getDefaultInstance();
				Isp isp		= realm.where(Isp.class).findFirst();

				if(isp != null && activity != null)
				{
					if(DateUtils.needUpdate(isp.getUpdated(), DateUtils.MEAN_FREQUENCY, context))
					{
						new GetLocationAsyncTask(activity, false, true, null).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
					}
				}
			}
		}
		catch(Exception e)
		{
			Utils.logError(context, "ConfirmReadingAsyncTask:onPreExecute - Exception:", e);
		}
	}

	@Override
	protected String doInBackground(Void... params)
	{
		String result = "";

		try
		{
			JSONObject jsonSend				= new JSONObject();
			SharedPreferences preferences	= context.getApplicationContext().getSharedPreferences(Common.KEY_PREF, Context.MODE_PRIVATE);
			JSONArray jsonArray				= new JSONArray();
			Realm realm						= Realm.getDefaultInstance();

			if(StringUtils.isIdMongo(msgId))
			{
				//Modificación para reportar por más de que por algo no se encuentré en la db
				jsonSend.put(Common.KEY_STATUS, status);

				//Reportar coordenadas

				if(status < Message.STATUS_SPAM)
				{
					Isp isp	= realm.where(Isp.class).findFirst();

					if(isp != null)
					{
						if(StringUtils.isNotEmpty(isp.getLat()) && StringUtils.isNotEmpty(isp.getLon()))
						{
							JSONObject geoJSON = new JSONObject();
							geoJSON.put(Common.KEY_GEO_LAT, isp.getLat());
							geoJSON.put(Common.KEY_GEO_LON, isp.getLon());
							geoJSON.put(Common.KEY_GEO_SOURCE, ApiConnection.getNetwork(context));
							jsonSend.put(Common.KEY_GEO, geoJSON);
						}
					}

					//TODO agregar integración para confirmación de visita
				}

				//Aquí entra cuando se recibe una push para acusar el recibo con estado 3 y para marcar como spam enviando el estado 5
				Message notification = realm.where(Message.class).equalTo(Message.KEY_API, msgId).findFirst();

				if(notification != null)
				{
					//Agregado para incluir campos de campaña y lista si están
					if(StringUtils.isNotEmpty(notification.getCampaignId()))
					{
						jsonSend.put(Message.KEY_CAMPAIGNID, notification.getCampaignId());
					}

					if(StringUtils.isNotEmpty(notification.getListId()))
					{
						jsonSend.put(Message.KEY_LISTID, notification.getListId());
					}
				}

				//Agregado para contemplar mensajes dentro de listas
				if(StringUtils.isIdMongo(msgId.replace("-", "")))
				{
					JSONObject jsonResult	= new JSONObject(	ApiConnection.request(ApiConnection.MESSAGES + "/" + msgId, context, ApiConnection.METHOD_PUT,
																preferences.getString(Common.KEY_TOKEN, ""), jsonSend.toString()));
					result					= ApiConnection.checkResponse(context, jsonResult);
				}
			}
			else
			{
				Suscription suscription = realm.where(Suscription.class).equalTo(Suscription.KEY_API, companyId).findFirst();
				SuscriptionHelper.debugSuscription(suscription);

				if(suscription != null)
				{
					//Agregado para actualizar el status en thread aparte
					RealmResults<Message> notifications = realm.where(Message.class).notEqualTo(Message.KEY_DELETED, Common.BOOL_YES).lessThan(Common.KEY_STATUS, Message.STATUS_READ)
															.equalTo(Suscription.KEY_API, suscription.getCompanyId()).findAll();

					if(notifications.size() > 0)
					{
						UpdateMessages task = new UpdateMessages(suscription.getCompanyId());
						task.start();

						//Solamente lo que es real se reporta a la api
						if(StringUtils.isIdMongo(companyId))
						{
							for(Message notification : notifications)
							{
								//Agregado para confirmar lectura de varios mensajes contra la api, mejora para evitar enviar confirmación de objecto local que no está en mongo
								if(StringUtils.isIdMongo(suscription.getCompanyId()) && StringUtils.isIdMongo(notification.getMsgId().replace("-", "")))
								{
									JSONObject jsonObject = new JSONObject();
									jsonObject.put(Common.KEY_ID, notification.getMsgId());
									jsonObject.put(Common.KEY_STATUS, Message.STATUS_READ);

									//Agregado para incluir campos de campaña y lista si están
									if(StringUtils.isNotEmpty(notification.getCampaignId()))
									{
										jsonObject.put(Message.KEY_CAMPAIGNID, notification.getCampaignId());
									}

									if(StringUtils.isNotEmpty(notification.getListId()))
									{
										jsonObject.put(Message.KEY_LISTID, notification.getListId());
									}

									jsonArray.put(jsonObject);
								}
							}
						}

						if(jsonArray.length() > 0)
						{
							JSONObject jsonResult	= new JSONObject(	ApiConnection.request(ApiConnection.MESSAGES, context, ApiConnection.METHOD_PUT,
																		preferences.getString(Common.KEY_TOKEN, ""), jsonArray.toString()));
							result					= ApiConnection.checkResponse(context, jsonResult);
						}
					}
				}
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
			}
		}
		catch(Exception e)
		{
			Utils.logError(context, "ConfirmReadingAsyncTask:doInBackground - Exception:", e);
		}

		return result;
	}

	private class UpdateMessages extends Thread
	{
		private String	id;

		private UpdateMessages(String id)
		{
			this.id	= id;
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
						RealmResults<Message> messages = bgRealm.where(Message.class).notEqualTo(Message.KEY_DELETED, Common.BOOL_YES).lessThan(Common.KEY_STATUS, Message.STATUS_READ)
															.equalTo(Suscription.KEY_API, id).findAll();

						for(int i = messages.size() -1; i >=0; i--)
						{
							messages.get(i).setStatus(Message.STATUS_READ);
						}
					}
				});
			}
			catch(Exception e)
			{
				Utils.logError(context, "ConfirmReadingAsyncTask:UpdateMessages:start - Exception:", e);
			}
		}
	}
}