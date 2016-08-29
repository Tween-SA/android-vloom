package com.tween.viacelular.models;

import android.content.Context;
import android.content.SharedPreferences;
import com.tween.viacelular.data.Company;
import com.tween.viacelular.utils.Common;
import io.realm.DynamicRealm;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmMigration;
import io.realm.RealmObjectSchema;
import io.realm.RealmSchema;

/**
 * Created by davidfigueroa on 4/2/16.
 */
public class Migration implements RealmMigration
{
	/**
	 * Regera la db Realm y la setea como Default para ser usada posteriormente
	 * @param context
	 */
	public static void getDB(Context context)
	{
		try
		{
			String version					= context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
			SharedPreferences preferences	= context.getSharedPreferences(Common.KEY_PREF, Context.MODE_PRIVATE);
			boolean splashed				= preferences.getBoolean(Common.KEY_PREF_SPLASHED, false);
			boolean upgraded				= preferences.getBoolean(Common.KEY_PREF_UPGRADED + version, false);

			if(!splashed && !upgraded)
			{
				RealmConfiguration config	= new RealmConfiguration.Builder(context)
						.name(Common.REALMDB_NAME)
						.schemaVersion(Common.REALMDB_VERSION)
						.build();
				Realm.setDefaultConfiguration(config);
			}
			else
			{
				if(splashed)
				{
					//App actualizada
					RealmConfiguration config	= new RealmConfiguration.Builder(context)
							.name(Common.REALMDB_NAME)
							.schemaVersion(Common.REALMDB_VERSION)
							//.migration(new Migration())
							//.deleteRealmIfMigrationNeeded()
							.build();
					Realm.setDefaultConfiguration(config);
				}
			}
		}
		catch(Exception e)
		{
			System.out.println("Migration:getDB - Exception: " + e);

			if(Common.DEBUG)
			{
				e.printStackTrace();
			}
		}
	}

	public static Suscription companyToSuscription(Suscription suscription, Company company)
	{
		if(suscription == null)
		{
			suscription = new Suscription();
		}

		if(company == null)
		{
			company = new Company();
		}

		suscription.setCompanyId(company.getCompanyId());
		suscription.setName(company.getName());
		suscription.setCountryCode(company.getCountryCode());
		suscription.setIndustryCode(company.getIndustryCode());
		suscription.setIndustry(company.getIndustry());
		suscription.setType(company.getType());
		suscription.setImage(company.getImage());
		suscription.setColorHex(company.getColorHex());
		suscription.setFromNumbers(company.getFromNumbers());
		suscription.setKeywords(company.getKeywords());
		suscription.setUnsuscribe(company.getUnsuscribe());
		suscription.setUrl(company.getUrl());
		suscription.setPhone(company.getPhone());
		suscription.setMsgExamples(company.getMsgExamples());
		suscription.setIdentificationKey(company.getIdentificationKey());
		suscription.setDataSent(company.getDataSent());
		suscription.setIdentificationValue(company.getIdentificationValue());
		suscription.setAbout(company.getAbout());
		suscription.setStatus(company.getStatus());
		suscription.setSilenced(company.getSilenced());
		suscription.setBlocked(company.getBlocked());
		suscription.setEmail(company.getEmail());
		suscription.setReceive(company.getReceive());
		suscription.setSuscribe(company.getSuscribe());
		suscription.setFollower(company.getFollower());

		return suscription;
	}

	@Override
	public void migrate(final DynamicRealm realm, final long oldVersion, final long newVersion)
	{
		try
		{
			System.out.println("Migrando dbRealm: oldv:"+oldVersion+" newv:"+newVersion);

			if(oldVersion > newVersion)
			{
				RealmSchema schema = realm.getSchema();
				RealmObjectSchema subscription = schema.get(Suscription.class.getName());
				subscription.addField(Suscription.KEY_LASTSOCIALUPDATED, Long.class);
				RealmObjectSchema message = schema.get(Message.class.getName());
				message.addField(Message.KEY_SOCIALID, String.class);
				message.addField(Message.KEY_SOCIALDATE, String.class);
				message.addField(Message.KEY_SOCIALLIKES, int.class);
				message.addField(Message.KEY_SOCIALSHARES, int.class);
				message.addField(Message.KEY_SOCIALACCOUNT, String.class);
				message.addField(Message.KEY_SOCIALNAME, String.class);
			}
		}
		catch(Exception e)
		{
			System.out.println(Migration.class.getName()+":migrate - Exception: " + e);

			if(Common.DEBUG)
			{
				e.printStackTrace();
			}
		}
	}
}