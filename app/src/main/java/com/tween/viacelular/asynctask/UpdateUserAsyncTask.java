package com.tween.viacelular.asynctask;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import com.afollestad.materialdialogs.MaterialDialog;
import com.tween.viacelular.R;
import com.tween.viacelular.data.ApiConnection;
import com.tween.viacelular.models.Land;
import com.tween.viacelular.models.User;
import com.tween.viacelular.models.UserHelper;
import com.tween.viacelular.utils.Common;
import com.tween.viacelular.utils.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;
import io.realm.Realm;

public class UpdateUserAsyncTask extends AsyncTask<Void, Void, String>
{
	private MaterialDialog	progress;
	private Context			context;
	private int				force			= 0;
	private boolean			displayDialog	= true;
	private String			token			= "";
	private boolean			onlyGet			= false;

	public UpdateUserAsyncTask(final Context context, final int force, final boolean displayDialog, final String token, final boolean onlyGet)
	{
		this.context		= context;
		this.force			= force;
		this.displayDialog	= displayDialog;
		this.token			= token;
		this.onlyGet		= onlyGet;
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
			System.out.println("UpdateUserAsyncTask - Exception: " + e);

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
			SharedPreferences preferences	= context.getSharedPreferences(Common.KEY_PREF, Context.MODE_PRIVATE);
			Realm realm						= Realm.getDefaultInstance();
			User user						= realm.where(User.class).findFirst();
			User userParsed					= null;
			JSONObject jsonSend				= new JSONObject();
			JSONObject jsonResult			= new JSONObject();
			JSONObject info					= new JSONObject();

			if(user != null)
			{
				//Agregado para reemplazar el gcmId con el nuevo token
				if(StringUtils.isNotEmpty(getToken()))
				{
					realm.beginTransaction();
					user.setGcmId(getToken());
					realm.commitTransaction();
				}

				//Modificación para refrescar suscripciones del usuario
				if(StringUtils.isIdMongo(user.getUserId()))
				{
					jsonResult	= new JSONObject(	ApiConnection.request(ApiConnection.USERS + "/" + user.getUserId(), context, ApiConnection.METHOD_GET,
							preferences.getString(Common.KEY_TOKEN, ""), ""));
					result		= ApiConnection.checkResponse(context, jsonResult);
				}

				if(result.equals(ApiConnection.OK))
				{
					JSONObject jsonData = jsonResult.getJSONObject(Common.KEY_DATA);

					if(jsonData != null)
					{
						userParsed = UserHelper.parseJSON(jsonData, true, context);
					}
				}

				if(!onlyGet)
				{
					if((StringUtils.isNotEmpty(preferences.getString(User.KEY_GCMID, "")) && (!preferences.getString(User.KEY_GCMID, "").equals(user.getGcmId())) || force == 1))
					{
						if(userParsed != null)
						{
							//Modificación para quitar campos no relevantes y forzar el envio de gsmId
							boolean modified = false;

							if(StringUtils.isNotEmpty(user.getFirstName()))
							{
								if(!user.getFirstName().equals(userParsed.getFirstName()))
								{
									info.put(User.KEY_FIRSTNAME, user.getFirstName());
									modified = true;
								}
							}

							if(StringUtils.isNotEmpty(user.getLastName()))
							{
								if(!user.getLastName().equals(userParsed.getLastName()))
								{
									info.put(User.KEY_LASTNAME, user.getLastName());
									modified = true;
								}
							}

							if(StringUtils.isNotEmpty(user.getEmail()))
							{
								//Modificación para enviar siempre el email de User para forzar el update con PUT/{userId}
								info.put(User.KEY_EMAIL, user.getEmail());
								modified = true;
							}

							if(StringUtils.isNotEmpty(user.getCountryCode()))
							{
								if(!user.getCountryCode().equals(userParsed.getCountryCode()))
								{
									info.put(Land.KEY_API, user.getCountryCode());
									modified = true;
								}
							}

							//Agregado para forzar actualización de User si se dio de baja de redis por gcmId no válido
							if(userParsed.getStatus() != user.getStatus())
							{
								modified = true;
							}

							if(modified || force == 1)
							{
								jsonSend.put(User.KEY_PHONE, ("+"+user.getPhone()).replace("++", "+"));
								jsonSend.put(User.KEY_API, user.getUserId());
								jsonSend.put(User.KEY_GCMID, preferences.getString(User.KEY_GCMID, user.getGcmId()));
								jsonSend.put(Common.KEY_INFO, info);
								jsonResult	= new JSONObject(	ApiConnection.request(ApiConnection.USERS + "/" + user.getUserId(), context, ApiConnection.METHOD_PUT,
										preferences.getString(Common.KEY_TOKEN, ""), jsonSend.toString()));
								result		= ApiConnection.checkResponse(context, jsonResult);
							}
						}
					}
				}
			}
		}
		catch(JSONException e)
		{
			System.out.println("UpdateUserAsyncTask - JSONException: " + e);

			if(Common.DEBUG)
			{
				e.printStackTrace();
			}
		}
		catch(Exception e)
		{
			System.out.println("UpdateUserAsyncTask - Exception: " + e);

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
			System.out.println("UpdateUserAsyncTask - Exception: " + e);

			if(Common.DEBUG)
			{
				e.printStackTrace();
			}
		}

		super.onPostExecute(result);
	}
	
	public String getToken()
	{
		return token;
	}
	
	public void setToken(final String token)
	{
		this.token = token;
	}
}