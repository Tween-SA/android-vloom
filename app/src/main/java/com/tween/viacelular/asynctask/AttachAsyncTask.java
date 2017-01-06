package com.tween.viacelular.asynctask;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.widget.Toast;
import com.afollestad.materialdialogs.MaterialDialog;
import com.tween.viacelular.R;
import com.tween.viacelular.interfaces.CallBackListener;
import com.tween.viacelular.models.Isp;
import com.tween.viacelular.models.Message;
import com.tween.viacelular.models.MessageHelper;
import com.tween.viacelular.models.Suscription;
import com.tween.viacelular.models.User;
import com.tween.viacelular.services.ApiConnection;
import com.tween.viacelular.utils.Common;
import com.tween.viacelular.utils.StringUtils;
import com.tween.viacelular.utils.Utils;
import org.json.JSONArray;
import org.json.JSONObject;
import io.realm.Realm;

public class AttachAsyncTask extends AsyncTask<Void, Void, String>
{
	private MaterialDialog		progress;
	private Context				context;
	private boolean				displayDialog	= true;
	private CallBackListener	listener;
	private String				msgId;

	public AttachAsyncTask(Context context, boolean displayDialog, String msgId, CallBackListener listener)
	{
		this.context		= context;
		this.displayDialog	= displayDialog;
		this.listener		= listener;
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
			System.out.println("AttachAsyncTask:onPreExecute - Exception: " + e);
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
			//Modificaciones para contemplar migraciónd de db
			Realm realm							= Realm.getDefaultInstance();
			SharedPreferences preferences		= context.getSharedPreferences(Common.KEY_PREF, Context.MODE_PRIVATE);
			User user							= realm.where(User.class).findFirst();

			if(user != null)
			{
				if(StringUtils.isNotEmpty(msgId))
				{
					Message message = realm.where(Message.class).equalTo(Message.KEY_API, msgId).findFirst();
					MessageHelper.debugMessage(message);

					if(message != null)
					{
						String companyId	= "";
						String flags		= Message.FLAGS_SMSCAP;

						if(StringUtils.isIdMongo(message.getCompanyId()))
						{
							companyId	= message.getCompanyId();
							flags		= message.getFlags();
						}
						else
						{
							if(message.getFlags().equals(Message.FLAGS_PUSH) || message.getFlags().equals(Message.FLAGS_PUSH_AND_SMS))
							{
								flags = Message.FLAGS_PUSHCAP;
							}
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
						jsonObject.put(Message.KEY_FLAGS, flags);
						jsonObject.put(User.KEY_PHONE, user.getPhone().replace("+", ""));
						jsonObject.put(Message.KEY_CAMPAIGNID, message.getCampaignId());
						jsonObject.put(Message.KEY_LISTID, message.getListId());
						Isp isp = realm.where(Isp.class).findFirst();

						if(StringUtils.isIdMongo(message.getMsgId()))
						{
							jsonObject.put(Message.KEY_API, msgId);
						}
						else
						{
							jsonObject.put(Message.KEY_API, "");
						}

						//Geo info que hoy solamente se manda cuando se marca la recepción del mensaje
						if(isp != null)
						{
							JSONObject geoJSON = new JSONObject();
							geoJSON.put(Common.KEY_GEO_LAT, isp.getLat());
							geoJSON.put(Common.KEY_GEO_LON, isp.getLon());
							geoJSON.put(Common.KEY_GEO_SOURCE, ApiConnection.getNetwork(context));
							jsonObject.put(Common.KEY_GEO, geoJSON);
						}

						//Enrich content generado por el usuario de la app
						if(StringUtils.isNotEmpty(message.getNote()) || StringUtils.isNotEmpty(message.getAttached()))
						{
							JSONObject enrichJSON = new JSONObject();
							enrichJSON.put(Message.KEY_NOTE, message.getNote());
							enrichJSON.put(Message.KEY_ATTACHED, message.getAttached());

							if(StringUtils.isNotEmpty(message.getAttachedTwo()))
							{
								enrichJSON.put(Message.KEY_ATTACHEDTWO, message.getAttachedTwo());
							}

							if(StringUtils.isNotEmpty(message.getAttachedThree()))
							{
								enrichJSON.put(Message.KEY_ATTACHEDTHREE, message.getAttachedThree());
							}

							jsonObject.put(Common.KEY_ENRICH, enrichJSON);
						}

						//Adjuntos provinientes de la push
						if(message.getKind() != Message.KIND_TEXT && StringUtils.isNotEmpty(message.getLink()))
						{
							JSONObject attachJSON = new JSONObject();
							attachJSON.put(Common.KEY_TYPE, message.getKind());
							attachJSON.put(Message.KEY_LINK, message.getLink());

							if(StringUtils.isNotEmpty(message.getLinkThumbnail()))
							{
								attachJSON.put(Message.KEY_LINKTHUMB, message.getLinkThumbnail());
							}

							JSONArray array = new JSONArray();
							array.put(attachJSON);
							jsonObject.put(Message.KEY_ATTACHMENTS, array);
						}

						JSONObject jsonResult	= new JSONObject(	ApiConnection.request(ApiConnection.MESSAGES+"/"+Common.KEY_ENRICH, context,
																	ApiConnection.METHOD_POST, preferences.getString(Common.KEY_TOKEN, ""), jsonObject.toString()));
						result					= ApiConnection.checkResponse(context, jsonResult);
					}
				}
			}
		}
		catch(Exception e)
		{
			System.out.println("AttachAsyncTask:doInBackground - Exception: " + e);

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

			//Llamar al callback
			if(StringUtils.isNotEmpty(result) && !result.equals(ApiConnection.OK))
			{
				Toast.makeText(context, result, Toast.LENGTH_SHORT).show();
			}

			listener.callBack();
		}
		catch(Exception e)
		{
			System.out.println("AttachAsyncTask:onPostExecute - Exception: " + e);

			if(Common.DEBUG)
			{
				e.printStackTrace();
			}
		}

		super.onPostExecute(result);
	}
}