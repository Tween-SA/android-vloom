package com.tween.viacelular.asynctask;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import com.afollestad.materialdialogs.MaterialDialog;
import com.tween.viacelular.R;
import com.tween.viacelular.activities.BlockedActivity;
import com.tween.viacelular.data.Company;
import com.tween.viacelular.data.CompanyDao;
import com.tween.viacelular.data.Country;
import com.tween.viacelular.data.CountryDao;
import com.tween.viacelular.data.DaoMaster;
import com.tween.viacelular.data.DaoSession;
import com.tween.viacelular.data.Isp;
import com.tween.viacelular.data.IspDao;
import com.tween.viacelular.data.Message;
import com.tween.viacelular.data.MessageDao;
import com.tween.viacelular.data.User;
import com.tween.viacelular.data.UserDao;
import com.tween.viacelular.models.Land;
import com.tween.viacelular.models.Migration;
import com.tween.viacelular.models.Suscription;
import com.tween.viacelular.utils.Common;
import com.tween.viacelular.utils.StringUtils;
import java.util.List;
import io.realm.Realm;

public class MigrationAsyncTask extends AsyncTask<Void, Void, String>
{
	private MaterialDialog	progress;
	private Activity		activity;
	private boolean			displayDialog	= false;

	public MigrationAsyncTask(Activity activity, boolean displayDialog)
	{
		this.activity		= activity;
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

				progress = new MaterialDialog.Builder(activity)
					.title(R.string.landing_card_loading_header)
					.cancelable(false)
					.content(R.string.landing_card_loading_text)
					.progress(true, 0)
					.show();

				Migration.getDB(activity);
				final ReadAccountsAsyncTask task	= new ReadAccountsAsyncTask(activity, false);
				task.execute();
			}
		}
		catch(Exception e)
		{
			System.out.println("MigrationAsyncTask:onPreExecute - Exception: " + e);

			if(Common.DEBUG)
			{
				e.printStackTrace();
			}
		}
	}

	@Override
	protected String doInBackground(Void... params)
	{
		String result		= "";
		//Modificación para evitar bloqueos en la db
		DaoSession session	= null;

		try
		{
			session						= DaoMaster.openDB(activity);
			List<Company> companyList	= CompanyDao.updateCompanies(activity);
			Realm realm					= Realm.getDefaultInstance();

			if(session != null)
			{
				CompanyDao companyDao	= session.getCompanyDao();
				MessageDao messageDao	= session.getMessageDao();
				UserDao userDao			= session.getUserDao();
				IspDao ispDao			= session.getIspDao();
				CountryDao countryDao	= session.getCountryDao();
				String countryCode		= "";
				String phone			= "";

				//Agregado para migrar entidades Isp y User a Realm
				if(userDao != null)
				{
					List<User> users = userDao.queryBuilder().limit(1).listLazyUncached();

					if(users != null)
					{
						if(users.size() > 0)
						{
							com.tween.viacelular.models.User user = new com.tween.viacelular.models.User();
							user.setUserId(users.get(0).getUserId());
							user.setFirstName(users.get(0).getFirstName());
							user.setLastName(users.get(0).getLastName());
							user.setFacebookId(users.get(0).getFacebookId());
							user.setGoogleId(users.get(0).getGoogleId());
							user.setEmail(users.get(0).getEmail());
							user.setPassword(users.get(0).getPassword());
							user.setPhone(users.get(0).getPhone());
							user.setGcmId(users.get(0).getGcmId());
							user.setStatus(users.get(0).getStatus());
							user.setCountryCode(users.get(0).getCountryCode());
							realm.beginTransaction();
							realm.copyToRealmOrUpdate(user);
							realm.commitTransaction();
							countryCode	= users.get(0).getCountryCode();
							phone		= users.get(0).getPhone().replace("+", "");
						}
					}
				}

				if(ispDao != null)
				{
					List<Isp> isps = ispDao.queryBuilder().limit(1).listLazyUncached();

					if(isps != null)
					{
						if(isps.size() > 0)
						{
							com.tween.viacelular.models.Isp isp = new com.tween.viacelular.models.Isp();
							isp.setQuery(isps.get(0).getQuery());
							isp.setAs(isps.get(0).getAs());
							isp.setStatus(isps.get(0).getStatus());
							isp.setCountry(isps.get(0).getCountry());
							isp.setCountryCode(isps.get(0).getCountryCode());
							isp.setRegion(isps.get(0).getRegion());
							isp.setRegionName(isps.get(0).getRegionName());
							isp.setCity(isps.get(0).getCity());
							isp.setZip(isps.get(0).getZip());
							isp.setLat(isps.get(0).getLat());
							isp.setLon(isps.get(0).getLon());
							isp.setTimezone(isps.get(0).getTimezone());
							isp.setIsp(isps.get(0).getIsp());
							isp.setOrg(isps.get(0).getOrg());
							isp.setOperatorNet(isps.get(0).getOperatorNet());
							isp.setOperatorSim(isps.get(0).getOperatorSim());
							isp.setCountryNet(isps.get(0).getCountryNet());
							isp.setCountrySim(isps.get(0).getCountrySim());
							realm.beginTransaction();
							realm.copyToRealmOrUpdate(isp);
							realm.commitTransaction();
						}
					}
				}

				if(companyList != null)
				{
					if(companyList.size() > 0)
					{
						for(Company existingCompany : companyList)
						{
							if(!StringUtils.isIdMongo(existingCompany.getCompanyId()))
							{
								List<Company> clients = CompanyDao.getCompanyByNumber(existingCompany.getName(), companyDao, false);

								if(clients.size() > 0)
								{
									for(Company companyFounded : clients)
									{
										//Actualizar los mensajes
										List<Message> messages = null;

										if(messageDao != null && StringUtils.isNotEmpty(existingCompany.getCompanyId()))
										{
											messages = messageDao.queryBuilder().where(MessageDao.Properties.companyId.eq(existingCompany.getCompanyId())).list();
										}

										if(messages != null)
										{
											if(messages.size() > 0)
											{
												Suscription suscription = realm.where(Suscription.class).equalTo(Suscription.KEY_API, existingCompany.getCompanyId()).findFirst();
												messageDao.getDatabase().beginTransaction();

												for(Message message : messages)
												{
													message.setCompanyId(companyFounded.getCompanyId());
													messageDao.update(message);
													realm.beginTransaction();
													com.tween.viacelular.models.Message messageRealm = new com.tween.viacelular.models.Message();
													messageRealm.setChannel(message.getChannel());
													messageRealm.setCountryCode(message.getCountryCode());
													messageRealm.setCreated(message.getCreated());
													messageRealm.setDeleted(message.getDeleted());
													messageRealm.setFlags(message.getFlags());
													messageRealm.setKind(com.tween.viacelular.models.Message.KIND_TEXT);
													messageRealm.setCampaignId("");
													messageRealm.setLink("");
													messageRealm.setLinkThumbnail("");
													messageRealm.setMsg(message.getMsg());
													messageRealm.setMsgId(message.getMsgId());
													messageRealm.setPhone(message.getPhone());
													messageRealm.setStatus(message.getStatus());
													messageRealm.setSubMsg("");
													messageRealm.setType(message.getType());
													messageRealm.setCompanyId(message.getCompanyId());
													realm.copyToRealmOrUpdate(messageRealm);
													//suscription.getMessages().add(messageRealm);
													realm.commitTransaction();
												}

												messageDao.getDatabase().setTransactionSuccessful();
												messageDao.getDatabase().endTransaction();
											}
										}
									}
								}
							}
						}
					}
				}

				//Agregado para migrar a Realm los países
				if(countryDao != null)
				{
					List<Country> countries = countryDao.queryBuilder().orderAsc(CountryDao.Properties.name).listLazyUncached();

					if(countries.size() > 0)
					{
						for(Country country: countries)
						{
							Land land = new Land(country.getCode(), country.getName(), country.getIsoCode(), country.getFormat(), country.getMinLength(), country.getMaxLength());
							realm.beginTransaction();
							realm.copyToRealmOrUpdate(land);
							realm.commitTransaction();
						}
					}
				}

				//Agregado para migrar a Realm los mensajes
				if(messageDao != null)
				{
					List<Message> messages = messageDao.queryBuilder().orderDesc(MessageDao.Properties.created).listLazyUncached();

					if(messages.size() > 0)
					{
						for(Message message: messages)
						{
							com.tween.viacelular.models.Message notification = new com.tween.viacelular.models.Message();

							if(StringUtils.isNotEmpty(message.getCountryCode()))
							{
								notification.setCountryCode(message.getCountryCode());
							}
							else
							{
								notification.setCountryCode(countryCode);
							}

							if(StringUtils.isNotEmpty(message.getPhone()))
							{
								notification.setPhone(message.getPhone());
							}
							else
							{
								notification.setPhone(phone);
							}

							//Agregado para pasar de ViaCelular a Vloom
							if(message.getCompanyId().equals(Suscription.COMPANY_ID_VC_MONGOOLD))
							{
								notification.setCompanyId(Suscription.COMPANY_ID_VC_MONGO);
							}
							else
							{
								notification.setCompanyId(message.getCompanyId());
							}

							notification.setChannel(message.getChannel());
							notification.setCreated(message.getCreated());
							notification.setDeleted(message.getDeleted());
							notification.setFlags(message.getFlags());
							notification.setKind(com.tween.viacelular.models.Message.KIND_TEXT);
							notification.setCampaignId("");
							notification.setLink("");
							notification.setLinkThumbnail("");
							notification.setMsg(message.getMsg());
							notification.setMsgId(message.getMsgId());
							notification.setStatus(message.getStatus());
							notification.setSubMsg("");
							notification.setType(message.getType());
							notification.setCampaignId("");
							notification.setListId("");
							realm.beginTransaction();
							realm.copyToRealmOrUpdate(notification);
							realm.commitTransaction();
						}
					}
				}
			}
		}
		catch(Exception e)
		{
			System.out.println("MigrationAsyncTask:doInBackground - Exception: " + e);

			if(Common.DEBUG)
			{
				e.printStackTrace();
			}
		}
		finally
		{
			DaoMaster.closeDB(session);
		}

		DaoMaster.closeDB(session);
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

			BlockedActivity.modifySubscriptions(activity, Common.BOOL_YES, true, "");

			//TODO Revisar y actualizar esto con cada nueva versión
			SharedPreferences preferences	= activity.getSharedPreferences(Common.KEY_PREF, Context.MODE_PRIVATE);
			SharedPreferences.Editor editor	= preferences.edit();
			editor.remove(Common.KEY_PREF_UPGRADED + "1.0.6.2");
			editor.remove(Common.KEY_PREF_UPGRADED + "1.0.6.3");
			editor.remove(Common.KEY_PREF_UPGRADED + "1.0.6.4");
			editor.remove(Common.KEY_PREF_UPGRADED + "1.0.6.5");
			editor.remove(Common.KEY_PREF_UPGRADED + "1.0.6.6");
			editor.remove(Common.KEY_PREF_UPGRADED + "1.0.6.7");
			editor.remove(Common.KEY_PREF_UPGRADED + "1.0.6.8");
			editor.putBoolean(Common.KEY_PREF_UPGRADED + "1.2", true);
			//Reiniciar la fecha para mostrar el popup tras cada update de la app
			editor.putLong(Common.KEY_PREF_DATE_1STLAUNCH, System.currentTimeMillis());
			editor.apply();
		}
		catch(Exception e)
		{
			System.out.println("MigrationAsyncTask:onPostExecute - Exception: " + e);

			if(Common.DEBUG)
			{
				e.printStackTrace();
			}
		}

		super.onPostExecute(result);
	}
}