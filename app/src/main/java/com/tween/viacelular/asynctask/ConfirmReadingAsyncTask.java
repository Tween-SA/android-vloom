package com.tween.viacelular.asynctask;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Looper;
import com.afollestad.materialdialogs.MaterialDialog;
import com.tween.viacelular.R;
import com.tween.viacelular.data.ApiConnection;
import com.tween.viacelular.models.Message;
import com.tween.viacelular.models.Suscription;
import com.tween.viacelular.utils.Common;
import com.tween.viacelular.utils.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;

/**
 * Created by david.figueroa on 17/6/15.
 */
public class ConfirmReadingAsyncTask extends AsyncTask<Void, Void, String>
{
	private MaterialDialog	progress;
	private Context			context;
	private boolean			displayDialog	= false;
	private String			companyId		= "";
	private String			msgId			= "";

	public ConfirmReadingAsyncTask(Context context, boolean displayDialog, String companyId, String msgId)
	{
		this.context		= context;
		this.displayDialog	= displayDialog;
		this.companyId		= companyId;
		this.msgId			= msgId;
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
		}
		catch(Exception e)
		{
			System.out.println("ConfirmReadingAsyncTask:onPreExecute - Exception: " + e);

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
			JSONObject jsonSend				= new JSONObject();
			SharedPreferences preferences	= context.getApplicationContext().getSharedPreferences(Common.KEY_PREF, Context.MODE_PRIVATE);
			JSONArray jsonArray				= new JSONArray();
			Realm realm						= Realm.getDefaultInstance();

			if(StringUtils.isIdMongo(msgId))
			{
				//Aquí entra cuando se recibe una push para acusar el recibo con estado 3 y para marcar como spam enviando el estado 5
				Message notification = realm.where(Message.class).equalTo(Message.KEY_API, msgId).findFirst();

				if(notification != null)
				{
					jsonSend.put(Common.KEY_STATUS, notification.getStatus());

					//Agregado para incluir campos de campaña y lista si están
					if(StringUtils.isNotEmpty(notification.getCampaignId()))
					{
						jsonSend.put(Message.KEY_CAMPAIGNID, notification.getCampaignId());
					}

					if(StringUtils.isNotEmpty(notification.getListId()))
					{
						jsonSend.put(Message.KEY_LISTID, notification.getListId());
					}

					//Agregado para contemplar mensajes dentro de listas
					if(StringUtils.isIdMongo(notification.getMsgId().replace("-", "")))
					{
						JSONObject jsonResult	= new JSONObject(	ApiConnection.request(ApiConnection.MESSAGES + "/" + notification.getMsgId(), context, ApiConnection.METHOD_PUT,
								preferences.getString(Common.KEY_TOKEN, ""), jsonSend.toString()));
						result					= ApiConnection.checkResponse(context.getApplicationContext(), jsonResult);
					}
				}
			}
			else
			{
				Suscription suscription = null;

				if(StringUtils.isIdMongo(companyId))
				{
					suscription	= realm.where(Suscription.class).equalTo(Suscription.KEY_API, companyId).findFirst();
				}

				if(suscription != null)
				{
					RealmResults<Message> notifications = realm.where(Message.class).equalTo(Message.KEY_DELETED, Common.BOOL_NO).equalTo(Common.KEY_STATUS, Message.STATUS_RECEIVE)
															.equalTo(Suscription.KEY_API, suscription.getCompanyId()).findAllSorted(Message.KEY_CREATED, Sort.DESCENDING);

					if(notifications.size() > 0)
					{
						for(Message notification : notifications)
						{
							//Agregado para confirmar lectura de varios mensajes contra la api, mejora para evitar enviar confirmación de objecto local que no está en mongo
							if(StringUtils.isIdMongo(suscription.getCompanyId()) && StringUtils.isIdMongo(notification.getMsgId().replace("-", "")))
							{
								JSONObject jsonObject = new JSONObject();
								jsonObject.put(Common.KEY_ID, notification.getMsgId());
								jsonObject.put(Common.KEY_STATUS, notification.getStatus());

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

						//Agregado para actualizar el status en thread aparte
						UpdateMessages task = new UpdateMessages(suscription.getCompanyId());
						task.start();

						if(jsonArray.length() > 0)
						{
							JSONObject jsonResult	= new JSONObject(	ApiConnection.request(ApiConnection.MESSAGES, context, ApiConnection.METHOD_PUT, preferences.getString(Common.KEY_TOKEN, ""),
																		jsonArray.toString()));
							result					= ApiConnection.checkResponse(context, jsonResult);
						}
					}
				}
			}

			result = ApiConnection.OK;
		}
		catch(Exception e)
		{
			System.out.println("ConfirmReadingAsyncTask:doInBackground - Exception: " + e);

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
		}
		catch(Exception e)
		{
			System.out.println("ConfirmReadingAsyncTask:onPostExecute - Exception: " + e);

			if(Common.DEBUG)
			{
				e.printStackTrace();
			}
		}

		super.onPostExecute(result);
	}

	public class UpdateMessages extends Thread
	{
		private String	id;

		public UpdateMessages(String id)
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
						RealmResults<Message> messages = bgRealm.where(Message.class).equalTo(Message.KEY_DELETED, Common.BOOL_NO).equalTo(Common.KEY_STATUS, Message.STATUS_RECEIVE)
															.equalTo(Suscription.KEY_API, id).findAllSorted(Message.KEY_CREATED, Sort.DESCENDING);

						for(int i = messages.size() -1; i >=0; i--)
						{
							messages.get(i).setStatus(Message.STATUS_READ);
						}
					}
				});
			}
			catch(Exception e)
			{
				System.out.println("ConfirmReadingAsyncTask:UpdateMessages:start - Exception: " + e);

				if(Common.DEBUG)
				{
					e.printStackTrace();
				}
			}
		}
	}
}