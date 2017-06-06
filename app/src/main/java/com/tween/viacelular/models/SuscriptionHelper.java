package com.tween.viacelular.models;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import com.tween.viacelular.R;
import com.tween.viacelular.adapters.TimestampComparator;
import com.tween.viacelular.interfaces.CallBackListener;
import com.tween.viacelular.utils.ApiConnection;
import com.tween.viacelular.utils.Common;
import com.tween.viacelular.utils.DateUtils;
import com.tween.viacelular.utils.StringUtils;
import com.tween.viacelular.utils.Utils;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import io.realm.Case;
import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;

/**
 * Manejador para tratamiento y parseo de empresas
 * Created by Tween (David Figueroa davo.figueroa@tween.com.ar) on 26/02/2016
 */
public abstract class SuscriptionHelper
{
	public static String classifySubscription(String addressee, String message, Context context, String country)
	{
		String companyId = "";

		try
		{
			Realm realm	= Realm.getDefaultInstance();

			//Revisamos si el sms tiene las keywords de Vloom
			if(	message.toUpperCase().contains(context.getString(R.string.app_name).toUpperCase()) || message.toUpperCase().contains("VIACELULAR") ||
				addressee.toUpperCase().contains(context.getString(R.string.app_name).toUpperCase()) || addressee.toUpperCase().contains("VIACELULAR"))
			{
				companyId	= Suscription.COMPANY_ID_VC_MONGO;
			}
			else
			{
				//Buscamos companies con el número del que vino el sms
				RealmResults<Suscription> companiesWithNumber	= realm.where(Suscription.class).contains(Suscription.KEY_NUMBERS, "\""+addressee+"\"", Case.INSENSITIVE).findAll();
				//Verificamos coincidencias (companies que compartan el número)
				int count										= companiesWithNumber.size();
				Suscription client;

				switch(count)
				{
					case 0:
						//Buscamos si ya existia la company phantom para ese número
						client = realm.where(Suscription.class).equalTo(Common.KEY_NAME, addressee, Case.INSENSITIVE).findFirst();

						if(client != null)
						{
							//Existe se asocia
							companyId = client.getCompanyId();
						}
						else
						{
							//No existe este número corto en la db, generamos company fantasma
							client		= createPhantom(addressee, context, country, false);
							companyId	= client.getCompanyId();
						}
					break;

					case 1:
						//Encontró una única company
						companyId = companiesWithNumber.get(0).getCompanyId();
					break;

					default:
						//Hay ocurrencias, revisaremos las keywords
						for(Suscription company : companiesWithNumber)
						{
							//Verificamos si el mensaje contiene el nombre de la company
							if(message.toUpperCase().contains(company.getName().toUpperCase()))
							{
								companyId = company.getCompanyId();
								break;
							}
							else
							{
								//Verificamos si el mensaje contiene alguna de las keywords
								if(StringUtils.containsKeywords(message, company.getKeywords()))
								{
									companyId = company.getCompanyId();
									break;
								}
							}
						}

						//No hubo coincidencia, se genera company fantasma
						if(StringUtils.isEmpty(companyId))
						{
							//Buscamos si ya existia la company phantom para ese número
							client = realm.where(Suscription.class).equalTo(Common.KEY_NAME, addressee, Case.INSENSITIVE).findFirst();

							if(client != null)
							{
								//Existe se asocia
								companyId = client.getCompanyId();
							}
							else
							{
								//No existe este número corto en la db, generamos company fantasma
								client		= createPhantom(addressee, context, country, false);
								companyId	= client.getCompanyId();
							}
						}
					break;
				}

				//Desconfia de la coincidencia por el último
				boolean correct = false;
				client = realm.where(Suscription.class).equalTo(Suscription.KEY_API, companyId).findFirst();

				if(client != null)
				{
					if(hasNumber(client, addressee, context))
					{
						correct = true;
					}
				}

				if(!correct)
				{
					//Buscamos si ya existia la company phantom para ese número
					client = realm.where(Suscription.class).equalTo(Common.KEY_NAME, addressee, Case.INSENSITIVE).findFirst();

					if(client != null)
					{
						//Existe se asocia
						companyId = client.getCompanyId();
					}
					else
					{
						//No existe este número corto en la db, generamos company fantasma
						client		= createPhantom(addressee, context, country, false);
						companyId	= client.getCompanyId();
					}
				}
			}
		}
		catch(Exception e)
		{
			Utils.logError(context, "SuscriptionHelper:classifySubscription - Exception:", e);
		}

		return companyId;
	}

	/**
	 * Agregado para actualizar companies mediante pull update. Debe ser llamado desde una Asynctask únicamente
	 * @param activity
	 * @return
	 */
	public static List<String> updateCompanies(Activity activity, boolean forceByUser)
	{
		List<Suscription> companies	= new ArrayList<>();
		List<String> list			= new ArrayList<>();

		try
		{
			//Modificación para contemplar migración a Realm
			Migration.getDB(activity);
			Realm realm						= Realm.getDefaultInstance();
			SharedPreferences preferences	= activity.getApplicationContext().getSharedPreferences(Common.KEY_PREF, Context.MODE_PRIVATE);
			String country					= preferences.getString(Land.KEY_API, "");
			JSONObject jsonResult;
			String result;

			if(StringUtils.isEmpty(country))
			{
				User user = realm.where(User.class).findFirst();

				if(user != null)
				{
					country = user.getCountryCode();
				}
			}

			//Si el usuario hizo pull update o apreto en la opción de actualizar del menú en el home se hace directamente
			if(forceByUser && ApiConnection.checkInternet(activity))
			{
				jsonResult	= new JSONObject(ApiConnection.getRequest(ApiConnection.COMPANIES_BY_COUNTRY + "=" + country, activity, preferences.getString(Common.KEY_TOKEN, ""), "", Common.TIMEOUT_API));
				result		= ApiConnection.checkResponse(activity.getApplicationContext(), jsonResult);

				if(result.equals(ApiConnection.OK))
				{
					if(jsonResult.getString(Common.KEY_CONTENT).trim().startsWith("["))
					parseList(jsonResult.getJSONArray(Common.KEY_CONTENT), activity.getApplicationContext(), true);
				}
				else
				{
					parseList(null, activity.getApplicationContext(), true);
				}

				SharedPreferences.Editor editor	= preferences.edit();
				editor.putLong(Common.KEY_PREF_TSCOMPANIES, System.currentTimeMillis());
				editor.apply();
			}
			else
			{
				//Agregado para limitar frecuencia de actualización
				long tsUpated = preferences.getLong(Common.KEY_PREF_TSCOMPANIES, System.currentTimeMillis());

				if(DateUtils.needUpdate(tsUpated, DateUtils.VERYHIGH_FREQUENCY, activity) && ApiConnection.checkInternet(activity))
				{
					jsonResult	= new JSONObject(ApiConnection.getRequest(ApiConnection.COMPANIES_BY_COUNTRY + "=" + country, activity, preferences.getString(Common.KEY_TOKEN, ""), "", Common.TIMEOUT_API));
					result		= ApiConnection.checkResponse(activity.getApplicationContext(), jsonResult);

					if(result.equals(ApiConnection.OK))
					{
						if(jsonResult.getString(Common.KEY_CONTENT).trim().startsWith("[") && jsonResult.getString(Common.KEY_CONTENT).trim().endsWith("]"))
						{
							parseList(jsonResult.getJSONArray(Common.KEY_CONTENT), activity.getApplicationContext(), true);
						}
						else
						{
							parseList(null, activity.getApplicationContext(), true);
						}
					}
					else
					{
						parseList(null, activity.getApplicationContext(), true);
					}

					SharedPreferences.Editor editor	= preferences.edit();
					editor.putLong(Common.KEY_PREF_TSCOMPANIES, System.currentTimeMillis());
					editor.apply();
				}
			}

			companies = getList(activity);
		}
		catch(Exception e)
		{
			Utils.logError(activity, "SuscriptionHelper:updateCompanies - Exception:", e);
		}

		if(companies.size() > 0)
		{
			for(Suscription suscription : companies)
			{
				if(suscription != null)
				{
					list.add(suscription.getCompanyId());
				}
			}
		}

		return list;
	}

	/**
	 * Revisamos si hay companies que contengan los mensajes no reconocidos anteriormente
	 * @param companyPhantom
	 * @param context
     */
	public static void killPhantoms(List<Suscription> companyPhantom, final Context context, CallBackListener callBackListener)
	{
		Realm realm = Realm.getDefaultInstance();

		if(companyPhantom == null)
		{
			companyPhantom					= new ArrayList<>();
			List<Suscription> suscriptions	= getList(context);

			if(suscriptions.size() > 0)
			{
				for(Suscription suscription : suscriptions)
				{
					if(suscription != null)
					{
						if(!StringUtils.isIdMongo(suscription.getCompanyId()) && suscription.getType() != Suscription.TYPE_FOLDER)
						{
							companyPhantom.add(suscription);
						}
					}
				}
			}
		}

		if(companyPhantom.size() > 0)
		{
			SharedPreferences preferences	= context.getSharedPreferences(Common.KEY_PREF, Context.MODE_PRIVATE);
			SharedPreferences.Editor editor	= preferences.edit();
			User user						= realm.where(User.class).findFirst();
			String country					= preferences.getString(Land.KEY_API, "");
			String companyId;

			if(user != null)
			{
				if(StringUtils.isNotEmpty(user.getCountryCode()))
				{
					country	= user.getCountryCode();
					editor.putString(Land.KEY_API, country);
					editor.apply();
				}
			}

			for(Suscription phantom : companyPhantom)
			{
				RealmResults<Message> messages	= realm.where(Message.class).equalTo(Suscription.KEY_API, phantom.getCompanyId()).equalTo(Message.KEY_DELETED, Common.BOOL_NO)
													.lessThan(Common.KEY_STATUS, Message.STATUS_SPAM).findAll().distinct(Message.KEY_CHANNEL);

				if(messages.size() > 0)
				{
					for(Message message : messages)
					{
						Suscription client = realm.where(Suscription.class).equalTo(Suscription.KEY_API, SuscriptionHelper.classifySubscription(message.getChannel(), message.getMsg(),
																																				context, country)).findFirst();

						if(client != null)
						{
							companyId = client.getCompanyId();

							if(!companyId.equals(phantom.getCompanyId()) && StringUtils.isIdMongo(companyId))
							{
								//Actualizar los mensajes
								MessageHelper.groupMessages(phantom.getCompanyId(), companyId, context);
							}
						}
					}
				}
			}
		}

		if(callBackListener != null)
		{
			callBackListener.invoke();
		}
	}

	/**
	 * Retorna la lista de suscripciones a mostrar en el home
	 * @param context
	 * @return
	 */
	public static List<Suscription> getList(Context context)
	{
		List<Suscription> companies	= new ArrayList<>();

		try
		{
			Migration.getDB(context);
			Realm realm							= Realm.getDefaultInstance();//No mostrar mensajes personales
			RealmResults<Message> realmResults	= realm.where(Message.class).notEqualTo(Message.KEY_DELETED, Common.BOOL_YES).lessThan(Common.KEY_STATUS, Message.STATUS_SPAM)
													.findAllSorted(Message.KEY_CREATED, Sort.DESCENDING);
			String[] sortKey					= {Message.KEY_CREATED};
			Sort[] sortVal						= {Sort.DESCENDING};
			realmResults.sort(sortKey, sortVal);

			if(realmResults.size() > 0)
			{
				//Agregado para corregir que las companies se orden por creación de último mensaje
				List<Message> messages = new LinkedList<>(realmResults);
				Collections.sort(messages, new TimestampComparator());

				for(Message message: messages)
				{
					if(StringUtils.isIdMongo(message.getCompanyId()))
					{
						Suscription company = realm.where(Suscription.class).equalTo(Suscription.KEY_API, message.getCompanyId()).findFirst();

						if(company != null && !companies.contains(company))
						{
							companies.add(company);
						}
					}
					else
					{
						if(StringUtils.isCompanyNumber(message.getChannel()))
						{
							Suscription company = realm.where(Suscription.class).equalTo(Suscription.KEY_API, message.getCompanyId()).findFirst();

							if(company != null && !companies.contains(company))
							{
								if(company.getName().equals(message.getChannel()))
								{
									companies.add(company);
								}
							}
						}
					}
				}
			}

			//Agregado para mostrar companies añadidas por más de que no tengan mensajes
			RealmResults<Suscription> suscriptions = realm.where(Suscription.class).equalTo(Suscription.KEY_FOLLOWER, Common.BOOL_YES).findAll();

			if(suscriptions.size() > 0)
			{
				for(Suscription suscription : suscriptions)
				{
					if(!companies.contains(suscription) && StringUtils.isIdMongo(suscription.getCompanyId()))
					{
						companies.add(suscription);
					}
					else
					{
						//Mostrar las carpetas creadas
						if(!companies.contains(suscription) && !StringUtils.isIdMongo(suscription.getCompanyId()) && suscription.getType() == Suscription.TYPE_FOLDER)
						{
							companies.add(suscription);
						}
					}
				}
			}
		}
		catch(Exception e)
		{
			Utils.logError(context, "SuscriptionHelper:getList - Exception:", e);
		}

		return companies;
	}

	/**
	 * Convierte una lista de Companies JSON recibidas en Suscription Realm
	 * @param jsonArray
	 * @param context
	 * @param update
	 */
	public static void parseList(JSONArray jsonArray, Context context, boolean update)
	{
		try
		{
			Realm realm = Realm.getDefaultInstance();

			if(jsonArray != null)
			{
				if(jsonArray.length() > 0)
				{
					for(int i = 0; i < jsonArray.length(); i++)
					{
						parseEntity(jsonArray.getJSONObject(i), "", "", context, update, 2, false);
					}
				}
				else
				{
					if(!update)
					{
						final Suscription vc = new Suscription(	Suscription.COMPANY_ID_VC_MONGO, context.getString(R.string.app_name), Land.DEFAULT_VALUE, "2",context.getString(R.string.app),
															Suscription.TYPE_FREE_REGISTERED, Suscription.ICON_APP, Common.COLOR_ACTION,
															"[\"from\":\""+Suscription.DEFAULT_SENDER+"\", \"type\":\"free\"]", context.getString(R.string.app_name) + ",", "",
															context.getString(R.string.url), "2614239139",
															"[{“title”:”Favorite Road Trips”,”msg”:”Tu credit con Falabella cumple 180 días de mora el 02-12-2015…”,”created”:”1450370433000”}]",
															"", Common.BOOL_NO, "", "Recibe notificaciones de vencimiento, promociones y novedades de "+context.getString(R.string.app_name),
															Suscription.STATUS_ACTIVE, Common.BOOL_NO, Common.BOOL_NO, Common.MAIL_TWEEN, Common.BOOL_YES, Common.BOOL_YES, Common.BOOL_YES,
															Common.BOOL_NO, Suscription.KEY_DEFAULTTWITTER);
						realm.executeTransaction(new Realm.Transaction()
						{
							@Override
							public void execute(Realm realm)
							{
								realm.copyToRealmOrUpdate(vc);
							}
						});
					}
				}
			}
			else
			{
				if(!update)
				{
					final Suscription vc = new Suscription(	Suscription.COMPANY_ID_VC_MONGO, context.getString(R.string.app_name), Land.DEFAULT_VALUE, "2", context.getString(R.string.app),
							Suscription.TYPE_FREE_REGISTERED, Suscription.ICON_APP, Common.COLOR_ACTION, "[\"from\":\""+Suscription.DEFAULT_SENDER+"\", \"type\":\"free\"]",
							context.getString(R.string.app_name) + ",", "", context.getString(R.string.url), "2614239139",
							"[{“title”:”Favorite Road Trips”,”msg”:”Tu credit con Falabella cumple 180 días de mora el 02-12-2015…”,”created”:”1450370433000”}]",
							"", Common.BOOL_NO, "", "Recibe notificaciones de vencimiento, promociones y novedades de "+context.getString(R.string.app_name),
							Suscription.STATUS_ACTIVE, Common.BOOL_NO, Common.BOOL_NO, Common.MAIL_TWEEN, Common.BOOL_YES, Common.BOOL_YES, Common.BOOL_YES, Common.BOOL_NO,
							Suscription.KEY_DEFAULTTWITTER);
					realm.executeTransaction(new Realm.Transaction()
					{
						@Override
						public void execute(Realm realm)
						{
							realm.copyToRealmOrUpdate(vc);
						}
					});
				}
			}
		}
		catch(Exception e)
		{
			Utils.logError(context, "SuscriptionHelper:parseList - Exception:", e);
		}
	}

	public static String toJSON(Suscription suscription, Context context)
	{
		String json = "";
		try
		{
			JSONObject jsonObject = new JSONObject();
			jsonObject.put(Suscription.KEY_API, suscription.getCompanyId());
			jsonObject.put(Common.KEY_NAME, suscription.getName());
			jsonObject.put(Land.KEY_API, suscription.getCountryCode());
			jsonObject.put(Suscription.KEY_INDUSTRYCODE, suscription.getIndustryCode());
			jsonObject.put(Suscription.KEY_INDUSTRY, suscription.getIndustry());
			jsonObject.put(Common.KEY_TYPE, suscription.getType());
			jsonObject.put(Suscription.KEY_IMAGE, suscription.getImage());
			jsonObject.put(Suscription.KEY_COLOR, suscription.getColorHex());
			jsonObject.put(Suscription.KEY_NUMBERS, suscription.getFromNumbers());
			jsonObject.put(Suscription.KEY_KEYWORDS, suscription.getKeywords());
			jsonObject.put(Suscription.KEY_UNSUSCRIBE, suscription.getUnsuscribe());
			jsonObject.put(Suscription.KEY_URL, suscription.getUrl());
			jsonObject.put(User.KEY_PHONE, suscription.getPhone());
			jsonObject.put(Suscription.KEY_MSGEXAMPLES, suscription.getMsgExamples());
			jsonObject.put(Suscription.KEY_IDENTIFICATIONKEY, suscription.getIdentificationKey());
			jsonObject.put(Suscription.KEY_DATASENT, suscription.getDataSent());
			jsonObject.put(Suscription.KEY_IDENTIFICATIONVALUE, suscription.getIdentificationValue());
			jsonObject.put(Suscription.KEY_ABOUT, suscription.getAbout());
			jsonObject.put(Common.KEY_STATUS, suscription.getStatus());
			jsonObject.put(Suscription.KEY_SILENCED, suscription.getSilenced());
			jsonObject.put(Suscription.KEY_BLOCKED, suscription.getBlocked());
			jsonObject.put(User.KEY_EMAIL, suscription.getEmail());
			jsonObject.put(Suscription.KEY_RECEIVE, suscription.getReceive());
			jsonObject.put(Suscription.KEY_SUSCRIBE, suscription.getSuscribe());
			jsonObject.put(Suscription.KEY_FOLLOWER, suscription.getFollower());
			jsonObject.put(Suscription.KEY_GRAY, suscription.getGray());
			jsonObject.put(Suscription.KEY_LASTSOCIALUPDATED, suscription.getLastSocialUpdated());
			jsonObject.put(Suscription.KEY_TWITTER, suscription.getTwitter());
			json = jsonObject.toString();
		}
		catch(Exception e)
		{
			Utils.logError(context, "SuscriptionHelper:toJSON - Exception:", e);
		}

		return json;
	}

	/**
	 * Convierte la Company JSON recibida en Suscription Realm
	 * @param jsonObject
	 * @param companyId
	 * @param countryCode
	 * @param context
	 * @param update
	 * @return Suscription
	 */
	public static Suscription parseEntity(JSONObject jsonObject, String companyId, String countryCode, Context context, boolean update, int flag, boolean justParser)
	{
		Suscription company	= null;

		try
		{
			String jCompanyId			= "";
			String jName				= "";
			String jIndustry			= "";
			String jIndustryCode		= "";
			String jEmail				= "";
			String jFromNumbers			= "[]";
			Integer jSilenced			= Common.BOOL_NO;
			Integer jBlocked			= Common.BOOL_NO;
			String jImage				= Suscription.ICON_APP;
			String jColorHex			= Common.COLOR_ACTION;
			Integer jType				= Suscription.TYPE_AUTOGENERATED;
			String jKeywords			= "";
			String jUnsuscribe			= "";
			Integer jStatus				= Suscription.STATUS_ACTIVE;
			String jCountryCode			= "";
			Integer jReceive			= Common.BOOL_YES;
			Integer jSuscribe			= Common.BOOL_NO;
			String jUrl					= "";
			String jPhone				= "";
			String jMsgExamples			= "[]";
			boolean findOutType			= false;
			String jAbout				= "";
			String jIdentificationKey	= "";
			Integer jDataSent			= Common.BOOL_NO;
			String jIdentificationValue	= "";
			Integer jFollower			= Common.BOOL_NO;
			Integer jGray				= Common.BOOL_NO;
			String twitter				= "";
			Realm realm					= Realm.getDefaultInstance();

			//Agregado para preañadir company nueva sin tener que esperar el get de la task
			if(flag == Common.BOOL_YES)
			{
				jFollower	= Common.BOOL_YES;
				jBlocked	= Common.BOOL_NO;
			}
			else
			{
				if(flag == Common.BOOL_NO)
				{
					jFollower	= Common.BOOL_NO;
					jBlocked	= Common.BOOL_YES;
				}
			}

			if(jsonObject != null)
			{
				if(jsonObject.has(Common.KEY_ID))
				{
					if(!jsonObject.isNull(Common.KEY_ID))
					{
						if(StringUtils.isNotEmpty(jsonObject.getString(Common.KEY_ID)))
						{
							jCompanyId = jsonObject.getString(Common.KEY_ID);
						}
					}
				}
				else
				{
					//Agregado para contemplar campo _id como id en el GET companies/companyId
					if(jsonObject.has(Common.KEY_IDMONGO))
					{
						if(!jsonObject.isNull(Common.KEY_IDMONGO))
						{
							if(StringUtils.isNotEmpty(jsonObject.getString(Common.KEY_IDMONGO)))
							{
								jCompanyId = jsonObject.getString(Common.KEY_IDMONGO);
							}
						}
					}
				}

				if(jsonObject.has(Common.KEY_NAME))
				{
					if(!jsonObject.isNull(Common.KEY_NAME))
					{
						if(StringUtils.isNotEmpty(jsonObject.getString(Common.KEY_NAME)))
						{
							jName = jsonObject.getString(Common.KEY_NAME);
						}
					}
				}

				if(jsonObject.has(Suscription.KEY_INDUSTRY))
				{
					if(!jsonObject.isNull(Suscription.KEY_INDUSTRY))
					{
						if(StringUtils.isNotEmpty(jsonObject.getString(Suscription.KEY_INDUSTRY)))
						{
							jIndustry = jsonObject.getString(Suscription.KEY_INDUSTRY);
						}
					}
				}

				if(jsonObject.has(Suscription.KEY_INDUSTRYCODE))
				{
					if(!jsonObject.isNull(Suscription.KEY_INDUSTRYCODE))
					{
						if(StringUtils.isNotEmpty(jsonObject.getString(Suscription.KEY_INDUSTRYCODE)))
						{
							jIndustryCode = jsonObject.getString(Suscription.KEY_INDUSTRYCODE);
						}
					}
				}

				if(jsonObject.has(Suscription.KEY_IMAGE))
				{
					if(!jsonObject.isNull(Suscription.KEY_IMAGE))
					{
						if(StringUtils.isNotEmpty(jsonObject.getString(Suscription.KEY_IMAGE)))
						{
							jImage = jsonObject.getString(Suscription.KEY_IMAGE);
						}
					}
				}

				if(jsonObject.has(Suscription.KEY_COLOR))
				{
					if(!jsonObject.isNull(Suscription.KEY_COLOR))
					{
						if(StringUtils.isNotEmpty(jsonObject.getString(Suscription.KEY_COLOR)))
						{
							jColorHex = "#" + jsonObject.getString(Suscription.KEY_COLOR);
							jColorHex = jColorHex.replace("##", "#");
						}
					}
				}

				if(jsonObject.has(Common.KEY_TYPE))
				{
					if(!jsonObject.isNull(Common.KEY_TYPE))
					{
						if(StringUtils.isNumber(jsonObject.getString(Common.KEY_TYPE)))
						{
							jType		= Integer.valueOf(jsonObject.getString(Common.KEY_TYPE));
							findOutType	= true;
						}
					}
				}

				if(jsonObject.has(Common.KEY_STATUS))
				{
					if(!jsonObject.isNull(Common.KEY_STATUS))
					{
						if(StringUtils.isNumber(jsonObject.getString(Common.KEY_STATUS)))
						{
							jStatus = jsonObject.getInt(Common.KEY_STATUS);
						}
					}
				}

				if(jsonObject.has(Land.KEY_API))
				{
					if(!jsonObject.isNull(Land.KEY_API))
					{
						if(StringUtils.isNotEmpty(jsonObject.getString(Land.KEY_API)))
						{
							jCountryCode = jsonObject.getString(Land.KEY_API);
						}
					}
				}

				//Agregado para mostrar en LandingActivity
				if(jsonObject.has(Suscription.KEY_ABOUT))
				{
					if(!jsonObject.isNull(Suscription.KEY_ABOUT))
					{
						if(StringUtils.isNotEmpty(jsonObject.getString(Suscription.KEY_ABOUT)))
						{
							jAbout = jsonObject.getString(Suscription.KEY_ABOUT);
						}
					}
				}

				if(jsonObject.has(Suscription.KEY_EMPLOYEES))
				{
					if(!jsonObject.isNull(Suscription.KEY_EMPLOYEES))
					{
						JSONArray jsonArrEmployees = jsonObject.getJSONArray(Suscription.KEY_EMPLOYEES);

						if(jsonArrEmployees != null)
						{
							if(!jsonArrEmployees.isNull(0))
							{
								JSONObject jsonEmployees = jsonArrEmployees.getJSONObject(0);

								if(jsonEmployees.has(User.KEY_EMAIL))
								{
									if(!jsonEmployees.isNull(User.KEY_EMAIL))
									{
										if(StringUtils.isNotEmpty(jsonEmployees.getString(User.KEY_EMAIL)))
										{
											jEmail = jsonEmployees.getString(User.KEY_EMAIL);
										}
									}
								}
							}
						}
					}
				}

				//Agregado para contemplar campos nuevos de la Company por si vienen fuera de info
				if(jsonObject.has(Suscription.KEY_SUSCRIBE))
				{
					if(!jsonObject.isNull(Suscription.KEY_SUSCRIBE))
					{
						if(StringUtils.isNotEmpty(jsonObject.getString(Suscription.KEY_SUSCRIBE)))
						{
							jSuscribe = jsonObject.getInt(Suscription.KEY_SUSCRIBE);
						}
					}
				}

				if(jsonObject.has(Suscription.KEY_URL))
				{
					if(!jsonObject.isNull(Suscription.KEY_URL))
					{
						if(StringUtils.isNotEmpty(jsonObject.getString(Suscription.KEY_URL)))
						{
							jUrl = jsonObject.getString(Suscription.KEY_URL);
						}
					}
				}

				if(jsonObject.has(User.KEY_PHONE))
				{
					if(!jsonObject.isNull(User.KEY_PHONE))
					{
						if(StringUtils.isNotEmpty(jsonObject.getString(User.KEY_PHONE)))
						{
							jPhone = jsonObject.getString(User.KEY_PHONE);
						}
					}
				}

				if(jsonObject.has(Suscription.KEY_MSGEXAMPLES))
				{
					if(!jsonObject.isNull(Suscription.KEY_MSGEXAMPLES))
					{
						if(StringUtils.isNotEmpty(jsonObject.getString(Suscription.KEY_MSGEXAMPLES)))
						{
							jMsgExamples = jsonObject.getString(Suscription.KEY_MSGEXAMPLES);
						}
					}
				}

				if(jsonObject.has(User.KEY_EMAIL))
				{
					if(!jsonObject.isNull(User.KEY_EMAIL))
					{
						if(StringUtils.isNotEmpty(jsonObject.getString(User.KEY_EMAIL)))
						{
							jEmail = jsonObject.getString(User.KEY_EMAIL);
						}
					}
				}

				if(jsonObject.has(Common.KEY_INFO))
				{
					if(!jsonObject.isNull(Common.KEY_INFO))
					{
						jsonObject = jsonObject.getJSONObject(Common.KEY_INFO);
						if(jsonObject != null)
						{
							if(jsonObject.has(Suscription.KEY_NUMBERS))
							{
								if(!jsonObject.isNull(Suscription.KEY_NUMBERS))
								{
									if(StringUtils.isNotEmpty(jsonObject.getString(Suscription.KEY_NUMBERS)))
									{
										//Modificación para recibir nuevo formato de jsonArray
										jFromNumbers = StringUtils.removeSpacesJSON(jsonObject.getString(Suscription.KEY_NUMBERS));
									}
								}
							}

							if(jsonObject.has(Suscription.KEY_KEYWORDS))
							{
								if(!jsonObject.isNull(Suscription.KEY_KEYWORDS))
								{
									if(StringUtils.isNotEmpty(jsonObject.getString(Suscription.KEY_KEYWORDS)))
									{
										jKeywords = StringUtils.fixListFields(jsonObject.getString(Suscription.KEY_KEYWORDS) + ",");
									}
								}
							}

							if(jsonObject.has(Suscription.KEY_UNSUSCRIBE))
							{
								if(!jsonObject.isNull(Suscription.KEY_UNSUSCRIBE))
								{
									if(StringUtils.isNotEmpty(jsonObject.getString(Suscription.KEY_UNSUSCRIBE)))
									{
										jUnsuscribe = jsonObject.getString(Suscription.KEY_UNSUSCRIBE);
									}
								}
							}

							if(jsonObject.has(Common.KEY_NAME))
							{
								if(!jsonObject.isNull(Common.KEY_NAME))
								{
									if(StringUtils.isNotEmpty(jsonObject.getString(Common.KEY_NAME)))
									{
										jName = jsonObject.getString(Common.KEY_NAME);
									}
								}
							}

							if(jsonObject.has(Suscription.KEY_INDUSTRY))
							{
								if(!jsonObject.isNull(Suscription.KEY_INDUSTRY))
								{
									if(StringUtils.isNotEmpty(jsonObject.getString(Suscription.KEY_INDUSTRY)))
									{
										jIndustry = jsonObject.getString(Suscription.KEY_INDUSTRY);
									}
								}
							}

							if(jsonObject.has(Suscription.KEY_INDUSTRYCODE))
							{
								if(!jsonObject.isNull(Suscription.KEY_INDUSTRYCODE))
								{
									if(StringUtils.isNotEmpty(jsonObject.getString(Suscription.KEY_INDUSTRYCODE)))
									{
										jIndustryCode = jsonObject.getString(Suscription.KEY_INDUSTRYCODE);
									}
								}
							}

							if(jsonObject.has(Suscription.KEY_IMAGE))
							{
								if(!jsonObject.isNull(Suscription.KEY_IMAGE))
								{
									if(StringUtils.isNotEmpty(jsonObject.getString(Suscription.KEY_IMAGE)))
									{
										jImage = jsonObject.getString(Suscription.KEY_IMAGE);
									}
								}
							}

							if(jsonObject.has(Suscription.KEY_COLOR))
							{
								if(!jsonObject.isNull(Suscription.KEY_COLOR))
								{
									if(StringUtils.isNotEmpty(jsonObject.getString(Suscription.KEY_COLOR)))
									{
										jColorHex	= "#" + jsonObject.getString(Suscription.KEY_COLOR);
										jColorHex	= jColorHex.replace("##", "#");
									}
								}
							}

							if(jsonObject.has(Common.KEY_TYPE))
							{
								if(!jsonObject.isNull(Common.KEY_TYPE))
								{
									if(StringUtils.isNumber(jsonObject.getString(Common.KEY_TYPE)) && !findOutType)
									{
										jType = Integer.valueOf(jsonObject.getString(Common.KEY_TYPE));
									}
								}
							}

							if(jsonObject.has(Common.KEY_STATUS))
							{
								if(!jsonObject.isNull(Common.KEY_STATUS))
								{
									if(StringUtils.isNumber(jsonObject.getString(Common.KEY_STATUS)))
									{
										jStatus = jsonObject.getInt(Common.KEY_STATUS);
									}
								}
							}

							if(jsonObject.has(Land.KEY_API))
							{
								if(!jsonObject.isNull(Land.KEY_API))
								{
									if(StringUtils.isNotEmpty(jsonObject.getString(Land.KEY_API)))
									{
										jCountryCode = jsonObject.getString(Land.KEY_API);
									}
								}
							}

							if(jsonObject.has(Suscription.KEY_SUSCRIBE))
							{
								if(!jsonObject.isNull(Suscription.KEY_SUSCRIBE))
								{
									if(StringUtils.isNotEmpty(jsonObject.getString(Suscription.KEY_SUSCRIBE)))
									{
										jSuscribe = jsonObject.getInt(Suscription.KEY_SUSCRIBE);
									}
								}
							}

							if(jsonObject.has(Suscription.KEY_URL))
							{
								if(!jsonObject.isNull(Suscription.KEY_URL))
								{
									if(StringUtils.isNotEmpty(jsonObject.getString(Suscription.KEY_URL)))
									{
										jUrl = jsonObject.getString(Suscription.KEY_URL);
									}
								}
							}

							if(jsonObject.has(User.KEY_PHONE))
							{
								if(!jsonObject.isNull(User.KEY_PHONE))
								{
									if(StringUtils.isNotEmpty(jsonObject.getString(User.KEY_PHONE)))
									{
										jPhone = jsonObject.getString(User.KEY_PHONE);
									}
								}
							}

							if(jsonObject.has(Suscription.KEY_MSGEXAMPLES))
							{
								if(!jsonObject.isNull(Suscription.KEY_MSGEXAMPLES))
								{
									if(StringUtils.isNotEmpty(jsonObject.getString(Suscription.KEY_MSGEXAMPLES)))
									{
										jMsgExamples = jsonObject.getString(Suscription.KEY_MSGEXAMPLES);
									}
								}
							}

							if(jsonObject.has(User.KEY_EMAIL))
							{
								if(!jsonObject.isNull(User.KEY_EMAIL))
								{
									if(StringUtils.isNotEmpty(jsonObject.getString(User.KEY_EMAIL)))
									{
										jEmail = jsonObject.getString(User.KEY_EMAIL);
									}
								}
							}

							//Agregado para contemplar campo de identificación en empresa
							if(jsonObject.has(Suscription.KEY_IDENTIFICATIONKEY))
							{
								if(!jsonObject.isNull(Suscription.KEY_IDENTIFICATIONKEY))
								{
									if(StringUtils.isNotEmpty(jsonObject.getString(Suscription.KEY_IDENTIFICATIONKEY)))
									{
										jIdentificationKey = jsonObject.getString(Suscription.KEY_IDENTIFICATIONKEY);
									}
								}
							}

							if(jsonObject.has(Suscription.KEY_DATASENT))
							{
								if(!jsonObject.isNull(Suscription.KEY_DATASENT))
								{
									if(StringUtils.isNotEmpty(jsonObject.getString(Suscription.KEY_DATASENT)))
									{
										jDataSent = jsonObject.getInt(Suscription.KEY_DATASENT);
									}
								}
							}

							//Agregado para contemplar valor de id de identificación en empresa
							if(jsonObject.has(Suscription.KEY_IDENTIFICATIONVALUE))
							{
								if(!jsonObject.isNull(Suscription.KEY_IDENTIFICATIONVALUE))
								{
									if(StringUtils.isNotEmpty(jsonObject.getString(Suscription.KEY_IDENTIFICATIONVALUE)))
									{
										jIdentificationValue = jsonObject.getString(Suscription.KEY_IDENTIFICATIONVALUE);
									}
								}
							}

							if(jsonObject.has(Suscription.KEY_GRAY))
							{
								if(!jsonObject.isNull(Suscription.KEY_GRAY))
								{
									if(StringUtils.isNotEmpty(jsonObject.getString(Suscription.KEY_GRAY)))
									{
										jGray = jsonObject.getInt(Suscription.KEY_GRAY);
									}
								}
							}

							if(jsonObject.has(Suscription.KEY_TWITTER))
							{
								if(!jsonObject.isNull(Suscription.KEY_TWITTER))
								{
									if(StringUtils.isNotEmpty(jsonObject.getString(Suscription.KEY_TWITTER)))
									{
										twitter = jsonObject.getString(Suscription.KEY_TWITTER);
									}
								}
							}
						}
					}
				}

				if(!jKeywords.toUpperCase().contains(jName.toUpperCase()))
				{
					jKeywords = jName + "," + jKeywords;
				}
			}
			else
			{
				//Agregado para empresas no identificadas mediante push
				Suscription lastUnamed				= null;
				int number							= 1;
				RealmResults<Suscription> clients	= realm.where(Suscription.class).beginsWith(Common.KEY_NAME, context.getString(R.string.company_unamed) + " ")
														.findAllSorted(Common.KEY_NAME, Sort.DESCENDING);

				//Agregado para prevenir lista sin resultados
				if(clients != null)
				{
					if(clients.size() > 0)
					{
						lastUnamed = clients.get(0);
					}

					if(lastUnamed != null)
					{
						String[] newName = lastUnamed.getName().split(" ");

						if(newName.length == 3)
						{
							if(StringUtils.isNumber(newName[2]))
							{
								number = Integer.parseInt(newName[2])+1;
							}
						}
						else
						{
							if(StringUtils.isNumber(newName[1]))
							{
								number = Integer.parseInt(newName[1])+1;
							}
						}
					}
				}

				company			= new Suscription();
				jName			= context.getString(R.string.company_unamed)+" "+number;
				jIndustry		= context.getString(R.string.app);
				jIndustryCode	= "2";

				if(StringUtils.isIdMongo(companyId))
				{
					jCompanyId = companyId;
				}

				if(StringUtils.isNotEmpty(countryCode))
				{
					jCountryCode = countryCode;
				}
			}

			company = new Suscription(	jCompanyId, jName, jCountryCode, jIndustryCode, jIndustry, jType, jImage, jColorHex, jFromNumbers, jKeywords, jUnsuscribe, jUrl, jPhone,
										jMsgExamples, jIdentificationKey, jDataSent, jIdentificationValue, jAbout, jStatus, jSilenced, jBlocked, jEmail, jReceive, jSuscribe,
										jFollower, jGray, twitter);
			company.setReceive(Common.BOOL_YES);

			//Agregado para actualizar companies mediante pull update
			if(!justParser)
			{
				if(update)
				{
					Suscription existingCompany	= realm.where(Suscription.class).equalTo(Suscription.KEY_API, company.getCompanyId()).findFirst();
					realm.beginTransaction();

					if(existingCompany != null)
					{
						existingCompany.setName(company.getName());
						existingCompany.setIndustry(company.getIndustry());
						existingCompany.setIndustryCode(company.getIndustryCode());
						existingCompany.setEmail(company.getEmail());
						existingCompany.setFromNumbers(company.getFromNumbers());
						existingCompany.setImage(company.getImage());
						existingCompany.setColorHex(company.getColorHex());
						existingCompany.setType(company.getType());
						existingCompany.setKeywords(company.getKeywords());
						existingCompany.setUnsuscribe(company.getUnsuscribe());
						existingCompany.setStatus(company.getStatus());
						existingCompany.setCountryCode(company.getCountryCode());
						existingCompany.setUrl(company.getUrl());
						existingCompany.setPhone(company.getPhone());
						existingCompany.setMsgExamples(company.getMsgExamples());
						existingCompany.setAbout(company.getAbout());
						existingCompany.setIdentificationKey(company.getIdentificationKey());
						existingCompany.setTwitter(company.getTwitter());
						//Los campos internos no se actualizan para no perder la configuración local: silenced, blocked, receive, suscribe, dataSent, identificationValue, follower, gray
						realm.copyToRealmOrUpdate(existingCompany);
					}
					else
					{
						realm.copyToRealmOrUpdate(company);
					}

					realm.commitTransaction();
				}
				else
				{
					realm.beginTransaction();
					realm.copyToRealmOrUpdate(company);
					realm.commitTransaction();
				}
			}
		}
		catch(Exception e)
		{
			Utils.logError(context, "SuscriptionHelper:parseEntity - Exception:", e);
		}

		return company;
	}

	public static void debugSuscription(Suscription suscription)
	{
		if(suscription != null)
		{
			System.out.println("\nSuscription - companyId: " + suscription.getCompanyId());
			System.out.println("Suscription - name: " + suscription.getName());
			System.out.println("Suscription - countryCode: " + suscription.getCountryCode());
			System.out.println("Suscription - industryCode: " + suscription.getIndustryCode());
			System.out.println("Suscription - industry: " + suscription.getIndustry());
			System.out.println("Suscription - type: " + suscription.getType());
			System.out.println("Suscription - image: " + suscription.getImage());
			System.out.println("Suscription - colorHex: " + suscription.getColorHex());
			System.out.println("Suscription - fromNumbers: " + suscription.getFromNumbers());
			System.out.println("Suscription - keywords: " + suscription.getKeywords());
			System.out.println("Suscription - unsuscribe: " + suscription.getUnsuscribe());
			System.out.println("Suscription - url: " + suscription.getUrl());
			System.out.println("Suscription - phone: " + suscription.getPhone());
			System.out.println("Suscription - msgExamples: " + suscription.getMsgExamples());
			System.out.println("Suscription - identificationKey: " + suscription.getIdentificationKey());
			System.out.println("Suscription - dataSent: " + suscription.getDataSent());
			System.out.println("Suscription - IdentificationValue: " + suscription.getIdentificationValue());
			System.out.println("Suscription - about: " + suscription.getAbout());
			System.out.println("Suscription - status: " + suscription.getStatus());
			System.out.println("Suscription - silenced: " + suscription.getSilenced());
			System.out.println("Suscription - blocked: " + suscription.getBlocked());
			System.out.println("Suscription - email: " + suscription.getEmail());
			System.out.println("Suscription - receive: " + suscription.getReceive());
			System.out.println("Suscription - suscribe: " + suscription.getSuscribe());
			System.out.println("Suscription - follower: " + suscription.getFollower());
			System.out.println("Suscription - gray: " + suscription.getGray());
			System.out.println("Suscription - lastSocialUpdated: " + suscription.getLastSocialUpdated());
			System.out.println("Suscription - twitter: " + suscription.getTwitter());
		}
		else
		{
			System.out.println("\nSuscription: null");
		}
	}

	/**
	 * Verifica si la company tiene al menos un número considerado como pago
	 * @param companyId
	 * @return boolean
	 */
	public static boolean isRevenue(String companyId, Context context)
	{
		try
		{
			Realm realm				= Realm.getDefaultInstance();
			Suscription suscription	= realm.where(Suscription.class).equalTo(Suscription.KEY_API, companyId).findFirst();
			RealmResults<Message> messages	= realm.where(Message.class).notEqualTo(Message.KEY_DELETED, Common.BOOL_YES).lessThan(Common.KEY_STATUS, Message.STATUS_SPAM)
					.equalTo(Suscription.KEY_API, suscription.getCompanyId()).findAll();

			if(messages.size() > 0)
			{
				//Modificación para mejorar rendimiento
				for(com.tween.viacelular.models.Message message : messages)
				{
					//Agregado para corregir formato de campo
					if(StringUtils.removeSpacesJSON(suscription.getFromNumbers()).contains("\"" + message.getChannel().replace("+", "") + "\""))
					{
						if(getTypeNumber(suscription, "\"" + message.getChannel().replace("+", "") + "\"", context).equals(Suscription.NUMBER_PAYOUT))
						{
							return true;
						}
					}
				}
			}
		}
		catch(Exception e)
		{
			Utils.logError(context, "SuscriptionHelper:isRevenue - Exception:", e);
		}

		return false;
	}

	/**
	 * Entrega el tipo de un número corto. Puede ser FREE o PAYOUT
	 * @param suscription
	 * @param number
	 * @return String
	 */
	public static String getTypeNumber(Suscription suscription, String number, Context context)
	{
		try
		{
			if(hasNumber(suscription, number, context))
			{
				JSONArray jsonArray = new JSONArray(StringUtils.removeSpacesJSON(suscription.getFromNumbers()));

				if(jsonArray.length() > 0)
				{
					for(int i = 0; i < jsonArray.length(); i++)
					{
						//Validación para búsqueda de número en el home
						if(!jsonArray.isNull(i))
						{
							JSONObject jsonObject = jsonArray.getJSONObject(i);

							if(jsonObject != null)
							{
								if(StringUtils.isNotEmpty(jsonObject.toString()))
								{
									if(jsonObject.getString(Suscription.KEY_FROM).equals(number.replace("+", "")))
									{
										return jsonObject.getString(Common.KEY_TYPE);
									}
								}
							}
						}
					}
				}
			}
		}
		catch(Exception e)
		{
			Utils.logError(context, "SuscriptionHelper:getTypeNumber - Exception:", e);
		}

		return "";
	}

	/**
	 * Verifica si existe el número corto enviado por parámetro
	 * @param suscription
	 * @param number
	 * @return boolean
	 */
	private static boolean hasNumber(Suscription suscription, String number, Context context)
	{
		try
		{
			//Agregado para buscar Subscription en el momento
			if(suscription != null)
			{
				if(StringUtils.removeSpacesJSON(suscription.getFromNumbers()).replace("+", "").contains("\"" + number.replace("+", "") + "\""))
				{
					return true;
				}
			}
			else
			{
				Realm realm	= Realm.getDefaultInstance();
				suscription	= realm.where(Suscription.class).contains(Suscription.KEY_NUMBERS, "\""+number+"\"", Case.INSENSITIVE).findFirst();

				if(suscription != null)
				{
					return true;
				}
			}
		}
		catch(Exception e)
		{
			Utils.logError(context, "SuscriptionHelper:hasNumber - Exception:", e);
		}

		return false;
	}

	/**
	 * Busca el número corto al que enviar el SMS de baja
	 * @param suscription
	 * @param messages
	 * @return String
	 */
	public static String searchUnsuscribeNumber(Suscription suscription, RealmResults<Message> messages, Context context)
	{
		String result = "";
		//En primer instancia nos fijamos el fromNumbers
		try
		{
			if(messages != null)
			{
				if(messages.size() > 0)
				{
					boolean noFoundInMessage	= true;
					Message message				= messages.get(0);

					if(message != null)
					{
						if(StringUtils.isNotEmpty(message.getChannel()))
						{
							if(StringUtils.isCompanyNumber(message.getChannel()))
							{
								noFoundInMessage	= false;
								result				= message.getChannel();
							}
						}
					}

					if(noFoundInMessage)
					{
						String number		= "";
						JSONArray jsonArray	= new JSONArray(StringUtils.removeSpacesJSON(suscription.getFromNumbers()));

						if(jsonArray.length() > 0)
						{
							for(int i = 0; i < jsonArray.length(); i++)
							{
								//Validación para búsqueda de número en el home
								if(!jsonArray.isNull(i))
								{
									JSONObject jsonObject = jsonArray.getJSONObject(i);

									if(jsonObject != null)
									{
										if(StringUtils.isNotEmpty(jsonObject.toString()))
										{
											if(jsonObject.has(Suscription.KEY_FROM) && jsonObject.has(Common.KEY_TYPE))
											{
												number = jsonObject.getString(Suscription.KEY_FROM).trim();

												if(jsonObject.getString(Common.KEY_TYPE).trim().equals(Suscription.NUMBER_PAYOUT))
												{
													break;
												}
											}
										}
									}
								}
							}

							result = number;
						}
					}

					//Por si no encontramos nos fijamos en el método Unsuscribe con formato csv por ej: SMS,BAJA,60700
					if(StringUtils.isNotEmpty(suscription.getUnsuscribe().trim()))
					{
						String[] method = suscription.getUnsuscribe().trim().split(",");

						if(StringUtils.isValidUnsuscribe(method))
						{
							result = method[2];
						}
					}
				}
			}
		}
		catch(Exception e)
		{
			Utils.logError(context, "SuscriptionHelper:searchUnsuscribeNumber - Exception:", e);
		}

		return result;
	}

	/**
	 * Busca el mensaje para incluir en el cuerpo del SMS de baja
	 * @param suscription
	 * @param context
	 * @return
	 */
	public static String searchUnsuscribeMessage(Suscription suscription, Context context)
	{
		String result = context.getString(android.R.string.cancel).toUpperCase();

		try
		{
			if(StringUtils.isNotEmpty(suscription.getUnsuscribe().trim()))
			{
				String[] method = suscription.getUnsuscribe().trim().split(",");

				if(StringUtils.isValidUnsuscribe(method))
				{
					if(StringUtils.isNotEmpty(method[1].trim()))
					{
						result = method[1].trim();
					}
				}
			}
		}
		catch(Exception e)
		{
			Utils.logError(context, "SuscriptionHelper:searchUnsuscribeMessage - Exception:", e);
		}

		return result;
	}

	/**
	 * Obtiene las companies por número corto
	 * @param number
	 * @return RealmResults<Suscription> Lista de companies
	 */
	public static RealmResults<Suscription> getCompanyByNumber(String number, Context context)
	{
		RealmResults<Suscription> companies	= null;

		try
		{
			Realm realm	= Realm.getDefaultInstance();
			companies	= realm.where(Suscription.class).contains(Suscription.KEY_NUMBERS, number).findAll();
		}
		catch(Exception e)
		{
			Utils.logError(context, "SuscriptionHelper:getCompanyByNumber - Exception:", e);
		}

		return companies;
	}

	/**
	 * Genera una company vacía para empresas no detectadas
	 * @param fewness
	 * @param context
	 * @param countryCode
	 * @return Suscription
	 */
	public static Suscription createPhantom(String fewness, Context context, String countryCode, boolean isFolder)
	{
		Suscription client	= null;

		try
		{
			Realm realm	= Realm.getDefaultInstance();
			client		= new Suscription();
			client.setName(fewness);
			client.setCountryCode(countryCode);
			client.setSuscribe(Common.BOOL_YES);
			client.setUrl("");
			client.setPhone("");
			client.setFromNumbers("[]");
			client.setMsgExamples("[]");
			client.setAbout("");
			client.setUnsuscribe("");
			client.setSilenced(Common.BOOL_NO);
			client.setBlocked(Common.BOOL_NO);
			client.setIdentificationKey("");
			client.setIdentificationValue("");
			client.setFollower(Common.BOOL_YES);
			client.setGray(Common.BOOL_NO);
			client.setDataSent(Common.BOOL_NO);
			client.setImage(Suscription.ICON_APP);
			client.setColorHex(Common.COLOR_ACTION);
			
			if(isFolder)
			{
				client.setIndustry(context.getString(R.string.folder_industry));
				client.setIndustryCode("0");
				client.setType(Suscription.TYPE_FOLDER);
				client.setReceive(Common.BOOL_YES);
				client.setKeywords("");
			}
			else
			{
				client.setIndustry(context.getString(R.string.app));
				client.setIndustryCode("2");
				client.setType(Suscription.TYPE_AUTOGENERATED);
				client.setReceive(Common.BOOL_NO);
				
				if(!StringUtils.isNumber(fewness) && !StringUtils.isLong(fewness))
				{
					client.setKeywords(fewness + ",");
				}
				
				client.setFromNumbers(addNumber(fewness, Suscription.NUMBER_FREE, client, context));
			}

			client.setCompanyId(String.valueOf(System.currentTimeMillis())); //Generamos el nuevo id con timestamp para evitar duplicados
			final Suscription suscription = client;
			realm.executeTransaction(new Realm.Transaction()
			{
				@Override
				public void execute(Realm realm)
				{
					realm.insert(suscription);
				}
			});
		}
		catch(Exception e)
		{
			Utils.logError(context, "SuscriptionHelper:createPhantom - Exception:", e);
		}

		return client;
	}

	/**
	 * Agrega el nuevo número corto recibido por parámetros y devuelve el JSON final a setear en el atributo fromNumbers
	 * @param number
	 * @param type
	 * @param suscription
	 * @return String
	 */
	private static String addNumber(String number, String type, Suscription suscription, Context context)
	{
		String fromNumbers = StringUtils.removeSpacesJSON(suscription.getFromNumbers());

		try
		{
			if(fromNumbers.equals("[]"))
			{
				fromNumbers = "["+"{\"from\":\"" + number.replace("+", "") + "\",\"type\":\"" + type + "\"}]";
			}
			else
			{
				if(!hasNumber(suscription, number, context))
				{
					fromNumbers = fromNumbers.replace("]", ",")+ "{\"from\":\"" + number.replace("+", "") + "\",\"type\":\"" + type + "\"}]";
				}
			}
		}
		catch(Exception e)
		{
			Utils.logError(context, "SuscriptionHelper:addNumber - Exception:", e);
		}

		return fromNumbers;
	}
}