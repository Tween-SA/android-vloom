package com.tween.viacelular.asynctask;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import com.afollestad.materialdialogs.MaterialDialog;
import com.tween.viacelular.R;
import com.tween.viacelular.activities.HomeActivity;
import com.tween.viacelular.activities.SuscriptionsActivity;
import com.tween.viacelular.models.Land;
import com.tween.viacelular.models.Migration;
import com.tween.viacelular.models.User;
import com.tween.viacelular.models.UserHelper;
import com.tween.viacelular.services.ApiConnection;
import com.tween.viacelular.utils.Common;
import com.tween.viacelular.utils.StringUtils;
import org.json.JSONObject;
import java.util.Locale;
import io.realm.Realm;

public class UpdateUserAsyncTask extends AsyncTask<Void, Void, String>
{
	private MaterialDialog	progress;
	private Context			context;
	private int				force			= Common.BOOL_NO;
	private boolean			displayDialog	= true;
	private String			token			= "";
	private boolean			useGet			= false;
	private boolean			usePut			= false;

	public UpdateUserAsyncTask(final Context context, final int force, final boolean displayDialog, final String token, final boolean useGet, final boolean usePut)
	{
		this.context		= context;
		this.force			= force;
		this.displayDialog	= displayDialog;
		this.token			= token;
		this.useGet			= useGet;
		this.usePut			= usePut;
	}

	protected void onPreExecute()
	{
		try
		{
			System.out.println("pre UpdateUserTask");

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
					.content(R.string.upgrade_text)
					.progress(true, 0)
					.show();
			}

			Migration.getDB(context);
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
			String userId					= preferences.getString(User.KEY_API, "");
			User user						= realm.where(User.class).equalTo(User.KEY_API, userId).findFirst();
			User userParsed					= null;
			JSONObject jsonSend				= new JSONObject();
			JSONObject jsonResult			= new JSONObject();
			JSONObject info					= new JSONObject();
			String gcmId					= preferences.getString(User.KEY_GCMID, token);
			String country					= preferences.getString(Land.KEY_API, "");
			String phone					= preferences.getString(User.KEY_PHONE, "");
			String email					= "";
			String firstName				= "";
			String lastName					= "";
			int status						= User.STATUS_UNVERIFIED;

			if(user != null)
			{
				user = realm.where(User.class).findFirst();
			}

			if(user != null)
			{
				//Agregado para reemplazar el gcmId con el nuevo token
				if(StringUtils.isNotEmpty(token))
				{
					realm.beginTransaction();
					user.setGcmId(token);
					realm.commitTransaction();
				}

				userId		= preferences.getString(User.KEY_API, user.getUserId());
				gcmId		= preferences.getString(User.KEY_GCMID, user.getGcmId());
				firstName	= user.getFirstName();
				lastName	= user.getLastName();
				status		= user.getStatus();
				email		= user.getEmail();
				country		= preferences.getString(Land.KEY_API, user.getCountryCode());
				phone		= preferences.getString(User.KEY_PHONE, user.getPhone());

				if(StringUtils.isIdMongo(userId))
				{
					//Modificación para solamente actualizar en api datos sin hacer el GET
					if(useGet)
					{
						//Modificación para refrescar suscripciones del usuario
						if(StringUtils.isIdMongo(userId))
						{
							jsonResult	= new JSONObject(ApiConnection.request(ApiConnection.USERS + "/" + userId, context, ApiConnection.METHOD_GET, preferences.getString(Common.KEY_TOKEN, ""), ""));
							result		= ApiConnection.checkResponse(context, jsonResult);
						}

						if(result.equals(ApiConnection.OK))
						{
							JSONObject jsonData = jsonResult.getJSONObject(Common.KEY_CONTENT);

							if(jsonData != null)
							{
								userParsed = UserHelper.parseJSON(jsonData, true, context);
							}
						}
					}
					else
					{
						userParsed = user;
					}

					if(usePut)
					{
						if((StringUtils.isNotEmpty(preferences.getString(User.KEY_GCMID, "")) && (!preferences.getString(User.KEY_GCMID, "").equals(gcmId)) || force == 1))
						{
							if(userParsed != null)
							{
								//Modificación para quitar campos no relevantes y forzar el envio de gsmId
								boolean modified = false;

								if(StringUtils.isNotEmpty(firstName))
								{
									if(!firstName.equals(userParsed.getFirstName()))
									{
										info.put(User.KEY_FIRSTNAME, firstName);
										modified = true;
									}
								}

								if(StringUtils.isNotEmpty(lastName))
								{
									if(!lastName.equals(userParsed.getLastName()))
									{
										info.put(User.KEY_LASTNAME, lastName);
										modified = true;
									}
								}

								if(StringUtils.isNotEmpty(email))
								{
									//Modificación para enviar siempre el email de User para forzar el update con PUT/{userId}
									info.put(User.KEY_EMAIL, email);
									modified = true;
								}

								if(StringUtils.isNotEmpty(country))
								{
									if(!country.equals(userParsed.getCountryCode()))
									{
										info.put(Land.KEY_API, country);
										modified = true;
									}
								}

								//Agregado para forzar actualización de User si se dio de baja de redis por gcmId no válido
								if(userParsed.getStatus() != status)
								{
									modified = true;
								}

								if(modified || force == 1)
								{
									jsonSend.put(User.KEY_PHONE, ("+"+phone).replace("++", "+"));
									jsonSend.put(User.KEY_API, user.getUserId());
									jsonSend.put(User.KEY_GCMID, preferences.getString(User.KEY_GCMID, user.getGcmId()));
									info.put("os", "android");
									info.put("countryLanguage", Locale.getDefault().getLanguage()+"-"+Locale.getDefault().getCountry());
									jsonSend.put(Common.KEY_INFO, info);
									jsonResult	= new JSONObject(	ApiConnection.request(ApiConnection.USERS + "/" + user.getUserId(), context, ApiConnection.METHOD_PUT,
											preferences.getString(Common.KEY_TOKEN, ""), jsonSend.toString()));
									result		= ApiConnection.checkResponse(context, jsonResult);
									//Guardar la fecha de última actualización
									SharedPreferences.Editor editor = preferences.edit();
									editor.putLong(Common.KEY_PREF_TSUSER, System.currentTimeMillis());
									editor.apply();
								}
							}
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

			System.out.println("do redirect");
			if(!displayDialog && useGet && !usePut && StringUtils.isEmpty(token) && StringUtils.isIdMongo(userId))
			{
				//Redirige a la pantalla home al terminar
				Intent intent = new Intent(context, HomeActivity.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				intent.putExtra(Common.KEY_REFRESH, false);
				context.startActivity(intent);
			}
			else
			{
				if(displayDialog && useGet && !usePut && StringUtils.isEmpty(token) && StringUtils.isIdMongo(userId))
				{
					//Redirige a la pantalla empresas al terminar
					Intent intent = new Intent(context, SuscriptionsActivity.class);
					intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					intent.putExtra(Common.KEY_TITLE, context.getString(R.string.title_companies));
					intent.putExtra(Common.KEY_SECTION, 2);
					intent.putExtra(Common.KEY_REFRESH, false);
					context.startActivity(intent);
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

		return result;
	}
}
