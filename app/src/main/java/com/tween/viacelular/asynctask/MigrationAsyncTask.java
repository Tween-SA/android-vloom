package com.tween.viacelular.asynctask;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import com.afollestad.materialdialogs.MaterialDialog;
import com.tween.viacelular.R;
import com.tween.viacelular.activities.HomeActivity;
import com.tween.viacelular.data.Company;
import com.tween.viacelular.data.CompanyDao;
import com.tween.viacelular.data.DaoMaster;
import com.tween.viacelular.data.DaoSession;
import com.tween.viacelular.data.Isp;
import com.tween.viacelular.data.IspDao;
import com.tween.viacelular.data.Message;
import com.tween.viacelular.data.MessageDao;
import com.tween.viacelular.data.User;
import com.tween.viacelular.data.UserDao;
import com.tween.viacelular.models.Migration;
import com.tween.viacelular.models.Suscription;
import com.tween.viacelular.models.SuscriptionHelper;
import com.tween.viacelular.utils.Common;
import com.tween.viacelular.utils.StringUtils;
import com.tween.viacelular.utils.Utils;
import java.util.List;
import io.realm.Realm;

/**
 * Manejador para actualización antigua de base de datos para replicar los datos que estaban en sqlite
 * Created by Tween (David Figueroa davo.figueroa@tween.com.ar)
 */
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
				
				if(!activity.isFinishing() && !activity.isDestroyed())
				{
					progress = new MaterialDialog.Builder(activity)
								.title(R.string.landing_card_loading_header)
								.cancelable(false)
								.content(R.string.upgrade_text)
								.progress(true, 0)
								.show();
				}

				Migration.getDB(activity);
				new ReadAccountsAsyncTask(activity, false).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
			}
		}
		catch(Exception e)
		{
			Utils.logError(activity, "MigrationAsyncTask:onPreExecute - Exception:", e);
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
			session						= DaoMaster.openRdb(activity);
			List<Company> companyList	= CompanyDao.getList(activity, false);
			Realm realm					= Realm.getDefaultInstance();

			if(session != null)
			{
				MessageDao messageDao	= session.getMessageDao();
				UserDao userDao			= session.getUserDao();
				IspDao ispDao			= session.getIspDao();
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
							final com.tween.viacelular.models.User user = new com.tween.viacelular.models.User();
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
							realm.executeTransaction(new Realm.Transaction()
							{
								@Override
								public void execute(Realm realm)
								{
									realm.copyToRealmOrUpdate(user);
								}
							});
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
							final com.tween.viacelular.models.Isp isp = new com.tween.viacelular.models.Isp();
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
							realm.executeTransaction(new Realm.Transaction()
							{
								@Override
								public void execute(Realm realm)
								{
									realm.copyToRealmOrUpdate(isp);
								}
							});
						}
					}
				}

				//Merge de company in suscription
				if(companyList != null)
				{
					if(companyList.size() > 0)
					{
						for(final Company existingCompany : companyList)
						{
							realm.executeTransaction(new Realm.Transaction()
							{
								@Override
								public void execute(Realm realm)
								{
									Suscription suscription = new Suscription(	existingCompany.getCompanyId(), existingCompany.getName(), existingCompany.getCountryCode(),
											existingCompany.getIndustryCode(), existingCompany.getIndustry(), existingCompany.getType(),
											existingCompany.getImage(), existingCompany.getColorHex(), existingCompany.getFromNumbers(),
											existingCompany.getKeywords(), existingCompany.getUnsuscribe(), existingCompany.getUrl(),
											existingCompany.getPhone(), existingCompany.getMsgExamples(), existingCompany.getIdentificationKey(),
											existingCompany.getDataSent(), existingCompany.getIdentificationValue(), existingCompany.getAbout(),
											existingCompany.getStatus(), existingCompany.getSilenced(), existingCompany.getBlocked(),
											existingCompany.getEmail(), existingCompany.getReceive(), existingCompany.getSuscribe(),
											existingCompany.getFollower(), existingCompany.getGray(), "");
									realm.copyToRealmOrUpdate(suscription);
								}
							});
						}
					}
				}

				//Actualizar datos de suscriptions
				SuscriptionHelper.updateCompanies(activity, false);

				//Importar mensajes
				if(messageDao != null)
				{
					List<Message> allMessages = messageDao.queryBuilder().list();

					if(allMessages != null)
					{
						if(allMessages.size() > 0)
						{
							for(final Message message : allMessages)
							{
								String companyId		= Suscription.COMPANY_ID_VC_MONGO;
								Suscription suscription	= realm.where(Suscription.class)
															.equalTo(Suscription.KEY_API, SuscriptionHelper.classifySubscription(message.getChannel(), message.getMsg(), activity, countryCode))
															.findFirst();

								if(suscription != null)
								{
									companyId = suscription.getCompanyId();
								}

								final String finalPhone = phone;
								final String finalCountry = countryCode;
								final String finalCompany = companyId;
								realm.executeTransaction(new Realm.Transaction()
								{
									@Override
									public void execute(Realm realm)
									{
										com.tween.viacelular.models.Message messageRealm = new com.tween.viacelular.models.Message();
										messageRealm.setChannel(message.getChannel());
										messageRealm.setCreated(message.getCreated());
										messageRealm.setDeleted(message.getDeleted());
										messageRealm.setFlags(message.getFlags());
										messageRealm.setKind(com.tween.viacelular.models.Message.KIND_TEXT);
										messageRealm.setCampaignId("");
										messageRealm.setLink("");
										messageRealm.setLinkThumbnail("");
										messageRealm.setMsg(message.getMsg());
										messageRealm.setMsgId(message.getMsgId());
										messageRealm.setStatus(message.getStatus());
										messageRealm.setSubMsg("");
										messageRealm.setType(message.getType());
										messageRealm.setCompanyId(finalCompany);

										if(StringUtils.isNotEmpty(message.getPhone()))
										{
											messageRealm.setPhone(message.getPhone());
										}
										else
										{
											messageRealm.setPhone(finalPhone);
										}

										if(StringUtils.isNotEmpty(message.getCountryCode()))
										{
											messageRealm.setCountryCode(message.getCountryCode());
										}
										else
										{
											messageRealm.setCountryCode(finalCountry);
										}

										realm.copyToRealmOrUpdate(messageRealm);
									}
								});
							}
						}
					}
				}

				//No se migran los países para optimizar proceso
			}
		}
		catch(Exception e)
		{
			Utils.logError(activity, "MigrationAsyncTask:doInBackground - Exception:", e);
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
			HomeActivity.modifySubscriptions(activity, Common.BOOL_YES, true, "", false);

			//TODO Revisar y actualizar esto con cada nueva versión
			SharedPreferences preferences	= activity.getSharedPreferences(Common.KEY_PREF, Context.MODE_PRIVATE);
			SharedPreferences.Editor editor	= preferences.edit();
			editor.remove(Common.KEY_PREF_UPGRADED + "1.0.6.2");
			editor.remove(Common.KEY_PREF_UPGRADED + "1.0.6.3");
			editor.remove(Common.KEY_PREF_UPGRADED + "1.0.6.4");
			editor.remove(Common.KEY_PREF_UPGRADED + "1.0.6.5");
			editor.putBoolean(Common.KEY_PREF_UPGRADED + activity.getPackageManager().getPackageInfo(activity.getPackageName(), 0).versionName, true);
			//Reiniciar la fecha para mostrar el popup tras cada update de la app
			editor.putLong(Common.KEY_PREF_DATE_1STLAUNCH, System.currentTimeMillis());
			editor.apply();

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
			Utils.logError(activity, "MigrationAsyncTask:onPostExecute - Exception:", e);
		}

		super.onPostExecute(result);
	}
}