package com.tween.viacelular.asynctask;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import com.afollestad.materialdialogs.MaterialDialog;
import com.tween.viacelular.R;
import com.tween.viacelular.interfaces.CallBackListener;
import com.tween.viacelular.models.Isp;
import com.tween.viacelular.models.Message;
import com.tween.viacelular.models.Suscription;
import com.tween.viacelular.models.User;
import com.tween.viacelular.utils.ApiConnection;
import com.tween.viacelular.utils.Common;
import com.tween.viacelular.utils.StringUtils;
import com.tween.viacelular.utils.Utils;
import org.json.JSONArray;
import org.json.JSONObject;
import io.realm.Realm;

/**
 * Manejador para adjuntar fotos a un mensaje recibido
 * Created by Tween (David Figueroa davo.figueroa@tween.com.ar)
 */
public class AttachAsyncTask extends AsyncTask<Void, Void, String>
{
	private MaterialDialog		progress;
	private Context				context;
	private CallBackListener	listener;
	private String				msgId;
	private boolean				displayDialog	= true;
	private String				comment			= "";
	private String				linkOne			= "";
	private String				linkTwo			= "";
	private String				linkThree		= "";

	public AttachAsyncTask(Context context, boolean displayDialog, String msgId, String comment, String linkOne, String linkTwo, String linkThree, CallBackListener listener)
	{
		this.context		= context;
		this.displayDialog	= displayDialog;
		this.listener		= listener;
		this.msgId			= msgId;
		this.comment		= comment;
		this.linkOne		= linkOne;
		this.linkTwo		= linkTwo;
		this.linkThree		= linkThree;
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
			Utils.logError(context, "AttachAsyncTask:onPreExecute - Exception:", e);
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

							if(StringUtils.isNotEmpty(message.getNote()))
							{
								enrichJSON.put(Message.KEY_NOTE, message.getNote());
							}

							if(StringUtils.isNotEmpty(message.getAttached()))
							{
								enrichJSON.put(Message.KEY_ATTACHED, message.getAttached());
							}

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
						else
						{
							//Doble checkeo por acceso a datos de distinto hilo
							if(StringUtils.isNotEmpty(comment) || StringUtils.isNotEmpty(linkOne))
							{
								JSONObject enrichJSON = new JSONObject();

								if(StringUtils.isNotEmpty(comment))
								{
									enrichJSON.put(Message.KEY_NOTE, comment);
								}

								if(StringUtils.isNotEmpty(linkOne))
								{
									enrichJSON.put(Message.KEY_ATTACHED, linkOne);
								}

								if(StringUtils.isNotEmpty(linkTwo))
								{
									enrichJSON.put(Message.KEY_ATTACHEDTWO, linkTwo);
								}

								if(StringUtils.isNotEmpty(linkThree))
								{
									enrichJSON.put(Message.KEY_ATTACHEDTHREE, linkThree);
								}

								jsonObject.put(Common.KEY_ENRICH, enrichJSON);
							}
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

						ApiConnection.request(ApiConnection.MESSAGES+"/"+Common.KEY_ENRICH, context, ApiConnection.METHOD_POST, preferences.getString(Common.KEY_TOKEN, ""), jsonObject.toString());
					}
				}
			}
		}
		catch(Exception e)
		{
			Utils.logError(context, "AttachAsyncTask:doInBackground - Exception:", e);
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
			listener.invoke();
		}
		catch(Exception e)
		{
			Utils.logError(context, "AttachAsyncTask:onPostExecute - Exception:", e);
		}

		super.onPostExecute(result);
	}
}
