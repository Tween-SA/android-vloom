package com.tween.viacelular.models;

import android.content.Context;
import android.content.SharedPreferences;
import com.tween.viacelular.activities.BlockedActivity;
import com.tween.viacelular.asynctask.CompanyAsyncTask;
import com.tween.viacelular.data.Country;
import com.tween.viacelular.utils.Common;
import com.tween.viacelular.utils.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;
import io.realm.Realm;
import io.realm.RealmResults;

/**
 * Created by davidfigueroa on 31/3/16.
 */
public abstract class UserHelper
{
	/**
	 * Convierte el User JSON recibido en User //TODO migrar a Realm
	 * @param json
	 * @param checkSubscriptions
	 * @param context
	 * @return
	 */
	public static User parseJSON(JSONObject json, boolean checkSubscriptions, Context context)
	{
		User user	= null;

		try
		{
			String jId			= "";
			String jGcmId		= "";
			String jPhone		= "";
			int jStatus			= User.STATUS_UNVERIFIED;
			String jFirstName	= "";
			String jLastName	= "";
			String jCountryCode	= "";
			String jEmail		= "";
			Realm realm			= Realm.getDefaultInstance();
			String jCode		= "";
			JSONArray jsonArray	= null;

			//Modificación por cambio en la estructura de los recursos en la Api
			if(json.has(Common.KEY_ID))
			{
				if(!json.isNull(Common.KEY_ID))
				{
					if(StringUtils.isNotEmpty(json.getString(Common.KEY_ID)))
					{
						jId = json.getString(Common.KEY_ID);
					}
				}
			}
			else
			{
				//Agregado para contemplar id de mongo
				if(json.has(Common.KEY_IDMONGO))
				{
					if(!json.isNull(Common.KEY_IDMONGO))
					{
						if(StringUtils.isNotEmpty(json.getString(Common.KEY_IDMONGO)))
						{
							jId = json.getString(Common.KEY_IDMONGO);
						}
					}
				}
			}

			if(json.has(User.KEY_GCMID))
			{
				if(!json.isNull(User.KEY_GCMID))
				{
					if(StringUtils.isNotEmpty(json.getString(User.KEY_GCMID)))
					{
						jGcmId = json.getString(User.KEY_GCMID);
					}
				}
			}

			if(json.has(User.KEY_PHONE))
			{
				if(!json.isNull(User.KEY_PHONE))
				{
					if(StringUtils.isNotEmpty(json.getString(User.KEY_PHONE)))
					{
						jPhone = json.getString(User.KEY_PHONE);
					}
				}
			}

			if(json.has(Common.KEY_STATUS))
			{
				if(!json.isNull(Common.KEY_STATUS))
				{
					if(StringUtils.isNotEmpty(json.getString(Common.KEY_STATUS)))
					{
						if(StringUtils.isNumber(json.getString(Common.KEY_STATUS)))
						{
							jStatus = json.getInt(Common.KEY_STATUS);
						}
					}
				}
			}

			if(json.has(User.KEY_FIRSTNAME))
			{
				if(StringUtils.isNotEmpty(json.getString(User.KEY_FIRSTNAME)))
				{
					jFirstName = json.getString(User.KEY_FIRSTNAME);
				}
			}

			if(json.has(User.KEY_LASTNAME))
			{
				if(StringUtils.isNotEmpty(json.getString(User.KEY_LASTNAME)))
				{
					jLastName = json.getString(User.KEY_LASTNAME);
				}
			}

			if(json.has(User.KEY_EMAIL))
			{
				if(StringUtils.isNotEmpty(json.getString(User.KEY_EMAIL)))
				{
					jEmail = json.getString(User.KEY_EMAIL);
				}
			}

			if(json.has(Country.KEY_API))
			{
				if(StringUtils.isNotEmpty(json.getString(Country.KEY_API)))
				{
					jCountryCode = json.getString(Country.KEY_API);
				}
			}

			if(json.has(Common.KEY_CODE))
			{
				if(StringUtils.isNotEmpty(json.getString(Common.KEY_CODE)))
				{
					jCode = json.getString(Common.KEY_CODE);
				}
			}

			if(json.has(Common.KEY_INFO))
			{
				//La estructura anterior alojaba más datos dentro de la key "info"
				json = json.getJSONObject(Common.KEY_INFO);

				if(json.has(Common.KEY_STATUS))
				{
					if(!json.isNull(Common.KEY_STATUS))
					{
						if(StringUtils.isNumber(json.getString(Common.KEY_STATUS)))
						{
							jStatus = json.getInt(Common.KEY_STATUS);
						}
					}
				}

				if(json.has(User.KEY_FIRSTNAME))
				{
					if(StringUtils.isNotEmpty(json.getString(User.KEY_FIRSTNAME)))
					{
						jFirstName = json.getString(User.KEY_FIRSTNAME);
					}
				}

				if(json.has(User.KEY_LASTNAME))
				{
					if(StringUtils.isNotEmpty(json.getString(User.KEY_LASTNAME)))
					{
						jLastName = json.getString(User.KEY_LASTNAME);
					}
				}

				if(json.has(User.KEY_EMAIL))
				{
					if(StringUtils.isNotEmpty(json.getString(User.KEY_EMAIL)))
					{
						jEmail = json.getString(User.KEY_EMAIL);
					}
				}

				if(json.has(Country.KEY_API))
				{
					if(StringUtils.isNotEmpty(json.getString(Country.KEY_API)))
					{
						jCountryCode = json.getString(Country.KEY_API);
					}
				}
			}

			//Modificación para persistir objeto parseado directamente, prevenir problema por userId sin validar
			user = realm.where(User.class).findFirst();

			if(user != null)
			{
				//Fix de usuario sin id luego de validar
				if(!jGcmId.equals(user.getGcmId()))
				{
					jGcmId = user.getGcmId();
				}

				if(user.getStatus() != User.STATUS_INACTIVE)
				{
					jStatus = User.STATUS_ACTIVE;
				}

				if(!user.getCountryCode().equals(jCountryCode))
				{
					jCountryCode = user.getCountryCode();
				}

				if(user.getUserId().equals("1"))
				{
					realm.beginTransaction();
					user.deleteFromRealm();
					realm.commitTransaction();
				}
			}

			if(StringUtils.isEmpty(jId))
			{
				jId = "1"; // Temporal hasta que se valide su número de celular
			}

			if(StringUtils.isEmpty(jCountryCode) && context != null)
			{
				SharedPreferences preferences	= context.getSharedPreferences(Common.KEY_PREF, Context.MODE_PRIVATE);
				jCountryCode					= preferences.getString(Country.KEY_API, "");
			}

			realm.beginTransaction();
			user = new User(jId, jFirstName, jLastName, "", "", jEmail, "", jPhone, jGcmId, jStatus, jCountryCode);
			realm.copyToRealmOrUpdate(user);
			realm.commitTransaction();

			debug(user);
			//Agregado para revisar las suscripciones del usuario
			if(checkSubscriptions && context != null)
			{
				if(json.has("subs"))
				{
					if(!json.isNull("subs"))
					{
						jsonArray = json.getJSONArray("subs");

						if(Common.DEBUG)
						{
							System.out.println("El usuario tiene suscripciones: "+jsonArray.toString());
						}

						if(jsonArray != null)
						{
							List<Suscription> blocked = new ArrayList<>();

							if(jsonArray.length() > 0)
							{
								for(int i = 0; i < jsonArray.length(); i++)
								{
									String companyId = jsonArray.getString(i);

									if(StringUtils.isIdMongo(companyId))
									{
										//Modificación para validar paso de contexto Realm
										Suscription suscription	= realm.where(Suscription.class).equalTo(Suscription.KEY_API, companyId).findFirst();
										String companyIdApi		= "";

										if(suscription == null)
										{
											final CompanyAsyncTask task	= new CompanyAsyncTask(context, false, companyId, jCountryCode);
											//TODO implementar callbacks para prevenir la suspención del UI por delay en la Asynctask
											companyIdApi				= task.execute().get();
										}

										suscription	= realm.where(Suscription.class).equalTo(Suscription.KEY_API, companyIdApi).findFirst();

										//Modificación para contemplar casos en que el usuario quitó companies añadidas
										if(suscription.getBlocked() == Common.BOOL_YES && suscription.getFollower() == Common.BOOL_NO)
										{
											BlockedActivity.modifySubscriptions(context, Common.BOOL_NO, false, suscription.getCompanyId());
											blocked.add(suscription);
										}
										else
										{
											//Agregado para contemplar auto suscripción por lista o campaña mediante web
											if(suscription.getBlocked() == Common.BOOL_NO && suscription.getFollower() == Common.BOOL_NO)
											{
												BlockedActivity.modifySubscriptions(context, Common.BOOL_YES, false, suscription.getCompanyId());
											}
										}
									}
								}
							}

							//Agregado para reportar companies que no habían sido reportadas
							RealmResults<Suscription> added		= realm.where(Suscription.class).equalTo(Suscription.KEY_FOLLOWER, Common.BOOL_YES).findAll();
							RealmResults<Suscription> locked	= realm.where(Suscription.class).equalTo(Suscription.KEY_FOLLOWER, Common.BOOL_NO).equalTo(Suscription.KEY_BLOCKED, Common.BOOL_YES).findAll();

							if(added.size() > jsonArray.length())
							{
								for(Suscription suscription : added)
								{
									if(!jsonArray.toString().contains(suscription.getCompanyId()))
									{
										BlockedActivity.modifySubscriptions(context, Common.BOOL_YES, false, suscription.getCompanyId());
									}
								}
							}

							if(locked.size() > blocked.size())
							{
								for(Suscription suscription : locked)
								{
									for(Suscription sucription2Block : blocked)
									{
										if(!sucription2Block.getCompanyId().equals(suscription.getCompanyId()))
										{
											BlockedActivity.modifySubscriptions(context, Common.BOOL_NO, false, suscription.getCompanyId());
											break;
										}
									}
								}
							}
						}
					}
				}
			}

			if(StringUtils.isNotEmpty(jCode))
			{
				realm.beginTransaction();
				user.setStatus(User.STATUS_ACTIVE);
				realm.commitTransaction();
				//Prever duplicación de users
				User first = realm.where(User.class).equalTo(User.KEY_API, "1").findFirst();

				if(first != null)
				{
					realm.beginTransaction();
					first.deleteFromRealm();
					realm.commitTransaction();
				}
			}
		}
		catch(Exception e)
		{
			System.out.println("UserHelper:parseJSON - Exception: " + e);

			if(Common.DEBUG)
			{
				e.printStackTrace();
			}
		}

		return user;
	}

	public static void debug(User user)
	{
		if(user != null)
		{
			System.out.println("\nUser - userId: " + user.getUserId());
			System.out.println("User - firstName: " + user.getFirstName());
			System.out.println("User - lastName: " + user.getLastName());
			System.out.println("User - facebookId: " + user.getFacebookId());
			System.out.println("User - googleId: " + user.getGoogleId());
			System.out.println("User - email: " + user.getEmail());
			System.out.println("User - password: " + user.getPassword());
			System.out.println("User - phone: " + user.getPhone());
			System.out.println("User - gcmId: " + user.getGcmId());
			System.out.println("User - status: " + user.getStatus());
			System.out.println("User - countryCode: " + user.getCountryCode());
		}
		else
		{
			System.out.println("\nUser: null");
		}
	}
}