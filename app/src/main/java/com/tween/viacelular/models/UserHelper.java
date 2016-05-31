package com.tween.viacelular.models;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Looper;
import com.tween.viacelular.asynctask.CompanyAsyncTask;
import com.tween.viacelular.data.Country;
import com.tween.viacelular.utils.Common;
import com.tween.viacelular.utils.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
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
			JSONArray subs		= null;
			JSONArray blocked	= null;

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

			//Modificación para tomar suscripciones
			if(json.has("subs"))
			{
				if(!json.isNull("subs"))
				{
					subs = json.getJSONArray("subs");
				}
			}

			if(json.has("blocked"))
			{
				if(!json.isNull("blocked"))
				{
					blocked = json.getJSONArray("blocked");
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

			//Agregado para revisar las suscripciones del usuario
			if(checkSubscriptions && context != null)
			{
				String ids2Add		= "";
				String ids2Remove	= "";

				if(subs != null)
				{
					if(Common.DEBUG)
					{
						System.out.println("El usuario tiene "+subs.length()+" suscripciones: "+subs.toString());
					}

					if(subs.length() > 0)
					{
						for(int i = 0; i < subs.length(); i++)
						{
							String companyId = subs.getString(i);

							if(StringUtils.isIdMongo(companyId))
							{
								//Modificación para validar paso de contexto Realm
								Suscription suscription	= realm.where(Suscription.class).equalTo(Suscription.KEY_API, companyId).findFirst();

								if(suscription == null)
								{
									final CompanyAsyncTask task	= new CompanyAsyncTask(context, false, companyId, jCountryCode);
									task.setFlag(Common.BOOL_YES);
									task.execute();
								}
								else
								{
									//Modificación para contemplar casos en que el usuario quitó companies añadidas
									if(suscription.getBlocked() == Common.BOOL_YES && suscription.getFollower() == Common.BOOL_NO)
									{
										ids2Remove = ids2Remove+"'"+suscription.getCompanyId()+"',";
									}
									else
									{
										//Agregado para contemplar auto suscripción por lista o campaña mediante web
										if(suscription.getBlocked() == Common.BOOL_NO && suscription.getFollower() == Common.BOOL_NO)
										{
											ids2Add = ids2Add+"'"+suscription.getCompanyId()+"',";
										}
									}
								}
							}
						}
					}

					//Agregado para reportar companies que no habían sido reportadas
					RealmResults<Suscription> added	= realm.where(Suscription.class).equalTo(Suscription.KEY_FOLLOWER, Common.BOOL_YES).findAll();

					if(added.size() > subs.length())
					{
						for(Suscription suscription : added)
						{
							if(!subs.toString().contains(suscription.getCompanyId()))
							{
								ids2Add = ids2Add+"'"+suscription.getCompanyId()+"',";
							}
						}
					}
				}

				if(blocked != null)
				{
					if(Common.DEBUG)
					{
						System.out.println("El usuario tiene quitadas: " + blocked.toString());
					}

					if(blocked.length() > 0)
					{
						for(int i = 0; i < blocked.length(); i++)
						{
							String companyId = blocked.getString(i);

							if(StringUtils.isIdMongo(companyId))
							{
								Suscription suscription	= realm.where(Suscription.class).equalTo(Suscription.KEY_API, companyId).findFirst();

								if(suscription == null)
								{
									final CompanyAsyncTask task	= new CompanyAsyncTask(context, false, companyId, jCountryCode);
									task.setFlag(Common.BOOL_NO);
									task.execute();
								}
								else
								{
									ids2Remove = ids2Remove+"'"+suscription.getCompanyId()+"',";
								}
							}
						}
					}
				}

				//Procesar actualización de subscriptions
				if(StringUtils.isNotEmpty(ids2Add))
				{
					ids2Add = ids2Add.substring(0, ids2Add.length() - 1);
				}

				if(StringUtils.isNotEmpty(ids2Remove))
				{
					ids2Remove = ids2Remove.substring(0, ids2Remove.length() - 1);
				}

				if(StringUtils.isNotEmpty(ids2Add) || StringUtils.isNotEmpty(ids2Remove))
				{
					UpdateSubscriptions task = new UpdateSubscriptions(ids2Add, ids2Remove);
					task.start();
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

	public static class UpdateSubscriptions extends Thread
	{
		private String	added;
		private String	removed;

		public UpdateSubscriptions(String added, String removed)
		{
			this.added		= added;
			this.removed	= removed;
		}

		public void start()
		{
			try
			{
				if(Looper.myLooper() == null)
				{
					Looper.prepare();
				}

				Realm realm	= Realm.getDefaultInstance();
				realm.executeTransaction(new Realm.Transaction()
				{
					@Override
					public void execute(Realm bgRealm)
					{
						RealmResults<Suscription> results = bgRealm.where(Suscription.class).findAll();

						for(int i = results.size() -1; i >=0; i--)
						{
							if(added.contains(results.get(i).getCompanyId()))
							{
								if(results.get(i).getFollower() == Common.BOOL_NO)
								{
									results.get(i).setFollower(Common.BOOL_YES);
									results.get(i).setBlocked(Common.BOOL_NO);
									results.get(i).setGray(Common.BOOL_NO);
									results.get(i).setDataSent(Common.BOOL_NO);
									results.get(i).setIdentificationValue("");
								}
							}

							if(removed.contains(results.get(i).getCompanyId()))
							{
								if(results.get(i).getFollower() == Common.BOOL_YES)
								{
									results.get(i).setFollower(Common.BOOL_NO);
									results.get(i).setBlocked(Common.BOOL_YES);
									results.get(i).setDataSent(Common.BOOL_NO);
									results.get(i).setIdentificationValue("");
								}
							}
						}
					}
				});
			}
			catch(Exception e)
			{
				System.out.println("UpdateSuscriptionsAsyncTask:UpdateCompany:start - Exception: " + e);

				if(Common.DEBUG)
				{
					e.printStackTrace();
				}
			}
		}
	}
}