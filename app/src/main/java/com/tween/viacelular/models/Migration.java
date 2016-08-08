package com.tween.viacelular.models;

import android.content.Context;

import com.tween.viacelular.data.Company;
import com.tween.viacelular.utils.Common;

import io.realm.Realm;
import io.realm.RealmConfiguration;

/**
 * Created by davidfigueroa on 4/2/16.
 */
public class Migration //implements RealmMigration
{
	/**
	 * Regera la db Realm y la setea como Default para ser usada posteriormente
	 * @param context
	 */
	public static void getDB(Context context, int version)
	{
		try
		{
			RealmConfiguration config	= new RealmConfiguration.Builder(context)
				.name(Common.REALMDB_NAME)
				.schemaVersion(version)
				.build();

			/*if(version != Common.REALMDB_VERSION)
			{
				Realm.migrateRealm(config, new Migration());
				config	= new RealmConfiguration.Builder(context)
						.name(Common.REALMDB_NAME)
						.schemaVersion(Common.REALMDB_VERSION)
						.build();
			}*/

			Realm.setDefaultConfiguration(config);
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
		try
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
		}
		catch(Exception e)
		{
			System.out.println("Migration:migrate - Exception: " + e);

			if(Common.DEBUG)
			{
				e.printStackTrace();
			}
		}

		return suscription;
	}

	/**
	 * Actualización de cambios en modelos de la db
	 * @param realm
	 * @param oldVersion
	 * @param newVersion
	 */
	/*@Override
	public void migrate(final DynamicRealm realm, long oldVersion, final long newVersion)
	{
		try
		{
			RealmSchema schema = realm.getSchema();

			if(oldVersion == Common.REALMDB_VERSION-1)
			{
				RealmObjectSchema messages = schema.get("Message");
				messages.addField(Message.KEY_SOCIALID, String.class);
				messages.addField(Message.KEY_SOCIALDATE, Long.class);
				messages.addField(Message.KEY_SOCIALLIKES, int.class);
				messages.addField(Message.KEY_SOCIALSHARES, int.class);
				messages.addField(Message.KEY_SOCIALACCOUNT, String.class);
				messages.addField(Message.KEY_SOCIALNAME, String.class);
				oldVersion++;
			}
		}
		catch(Exception e)
		{
			System.out.println("Migration:migrate - Exception: " + e);

			if(Common.DEBUG)
			{
				e.printStackTrace();
			}
		}
	}*/
}