package com.tween.viacelular.asynctask;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import com.afollestad.materialdialogs.MaterialDialog;
import com.tween.viacelular.R;
import com.tween.viacelular.data.ApiConnection;
import com.tween.viacelular.models.Message;
import com.tween.viacelular.models.Suscription;
import com.tween.viacelular.models.User;
import com.tween.viacelular.utils.Common;
import com.tween.viacelular.utils.StringUtils;
import com.tween.viacelular.utils.Utils;
import org.json.JSONArray;
import org.json.JSONObject;
import io.realm.Realm;
import io.realm.RealmResults;

public class ConnectApiSMSAsyncTask extends AsyncTask<Void, Void, String>
{
	private MaterialDialog	progress;
	private Context			context;
	private boolean			displayDialog	= true;
	private Message			message			= null;

	public ConnectApiSMSAsyncTask(Context context, boolean displayDialog)
	{
		this.context		= context;
		this.displayDialog	= displayDialog;
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
			System.out.println("ConnectApiSMSAsyncTask:onPreExecute - Exception: " + e);

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
			//Modificaciones para contemplar migración a Realm
			Realm realm						= Realm.getDefaultInstance();
			SharedPreferences preferences	= context.getSharedPreferences(Common.KEY_PREF, Context.MODE_PRIVATE);
			JSONArray jsonArray				= new JSONArray();
			User user						= realm.where(User.class).findFirst();
			boolean send					= false;

			//Modificación para reducir if innecesarios y corrección de CountryCode incorrecto, se tomará desde el Usuario que es el correcto
			if(user != null)
			{
				String countryCode = user.getCountryCode();

				//Agregado para enviar a la api sms que acaba de llegar
				if(message != null)
				{
					String companyId = "";

					if(StringUtils.isIdMongo(message.getCompanyId()))
					{
						companyId = message.getCompanyId();
					}

					JSONObject jsonObject	= new JSONObject();
					jsonObject.put(Common.KEY_TYPE, message.getType());
					jsonObject.put(Message.KEY_MSG, StringUtils.sanitizeText(message.getMsg()));
					jsonObject.put(Message.KEY_CHANNEL, Utils.getChannelSMS(context));
					jsonObject.put(Common.KEY_STATUS, message.getStatus());
					jsonObject.put(Suscription.KEY_API, companyId);
					jsonObject.put(Message.KEY_CREATED, message.getCreated());
					jsonObject.put(Message.KEY_DELETED, message.getDeleted());
					jsonObject.put(Suscription.KEY_FROM, message.getChannel());
					jsonObject.put(Message.KEY_TTD, 0);
					jsonObject.put(Message.KEY_FLAGS, Message.FLAGS_SMSCAP);
					JSONArray phones		= new JSONArray();
					phones.put(user.getPhone().replace("+", ""));
					jsonObject.put("phones", phones);
					jsonArray.put(jsonObject);
					send = true;
				}
				else
				{
					RealmResults<Message> messages	= realm.where(Message.class).equalTo(Common.KEY_TYPE, Message.TYPE_SMS).findAll();

					if(messages.size() > 0)
					{
						//Armado de array en Json con los sms interpretados
						for(int i = 0; i < messages.size(); i++)
						{
							if(i <= 300)
							{
								String companyId = "";

								if(StringUtils.isIdMongo(messages.get(i).getCompanyId()))
								{
									companyId = messages.get(i).getCompanyId();
								}

								//Reestructuración de api
								JSONObject jsonObject	= new JSONObject();
								jsonObject.put(Common.KEY_TYPE, messages.get(i).getType());
								jsonObject.put(Message.KEY_MSG, StringUtils.sanitizeText(messages.get(i).getMsg()));
								jsonObject.put(Message.KEY_CHANNEL, Utils.getChannelSMS(context));
								jsonObject.put(Common.KEY_STATUS, messages.get(i).getStatus());
								jsonObject.put(Suscription.KEY_API, companyId);
								jsonObject.put(Message.KEY_CREATED, messages.get(i).getCreated());
								jsonObject.put(Message.KEY_DELETED, messages.get(i).getDeleted());
								jsonObject.put(Suscription.KEY_FROM, messages.get(i).getChannel());
								jsonObject.put(Message.KEY_TTD, 0);
								jsonObject.put(Message.KEY_FLAGS, Message.FLAGS_SMSCAP);
								JSONArray phones		= new JSONArray();
								phones.put(user.getPhone().replace("+", ""));
								jsonObject.put("phones", phones);
								jsonArray.put(jsonObject);
							}
						}

						send = true;
					}
				}
			}

			if(send)
			{
				//Agregado para no enviar mis mensajes cada vez que hago debug
				if(!user.getPhone().replace("+", "").equals("5492616333888"))
				{
					JSONObject jsonResult	= new JSONObject(	ApiConnection.request(ApiConnection.SEND_SMS, context, ApiConnection.METHOD_POST, preferences.getString(Common.KEY_TOKEN, ""),
																jsonArray.toString()));
					result					= ApiConnection.checkResponse(context, jsonResult);
				}
				else
				{
					if(Common.DEBUG)
					{
						System.out.println("Call to Api-Storage will be with: "+jsonArray.toString());
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
			System.out.println("ConnectApiSMSAsyncTask:doInBackground - Exception: " + e);

			if(Common.DEBUG)
			{
				e.printStackTrace();
			}
		}

		return result;
	}

	public Message getMessage()
	{
		return message;
	}

	public void setMessage(final Message message)
	{
		this.message = message;
	}
}