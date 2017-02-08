package com.tween.viacelular.models;

import android.content.Context;
import android.content.SharedPreferences;

import com.tween.viacelular.data.Company;
import com.tween.viacelular.utils.Common;
import com.tween.viacelular.utils.Utils;

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
			SharedPreferences preferences	= context.getSharedPreferences(Common.KEY_PREF, Context.MODE_PRIVATE);//Agregar algo para saber cuando es la primera vez que levanta la app
			boolean upgradedDB				= preferences.getBoolean(Common.KEY_PREF_UPGRADED +"DB"+ Common.REALMDB_VERSION, false);

			if(upgradedDB)
			{
				//No necesita migrarse porque es instalación nueva
				RealmConfiguration config	= new RealmConfiguration.Builder(context)
						.name(Common.REALMDB_NAME)
						.schemaVersion(Common.REALMDB_VERSION)
						.deleteRealmIfMigrationNeeded()
						.build();
				Realm.setDefaultConfiguration(config);
			}
			else
			{
				//Necesita migrarse porque es un update en caliente
				RealmConfiguration config	= new RealmConfiguration.Builder(context)
						.name(Common.REALMDB_NAME)
						.schemaVersion(Common.REALMDB_VERSION)
						.migration(new Migration())
						.build();
				Realm.setDefaultConfiguration(config);
			}
		}
		catch(Exception e)
		{
			Utils.logError(context, Migration.class.getName()+":getDB - Exception:", e);

			if(Common.DEBUG)
			{
				Utils.writeStringInFile(Migration.class.getName()+":getDB - Exception: " + e, "", context);
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
			System.out.println("Migrando dbRealm: oldVersion:"+oldVersion+" newVersion:"+newVersion);

			if(realm != null)
			{
				if(oldVersion < newVersion)
				{
					RealmSchema schema = realm.getSchema();

					if(schema != null)
					{
						RealmObjectSchema message = schema.get(Message.class.getSimpleName());

						if(message != null)
						{
							message.addField(Message.KEY_ATTACHEDTWO, String.class);
							message.addField(Message.KEY_ATTACHEDTHREE, String.class);
							message.addField(Message.KEY_URI, String.class);
							message.addField(Message.KEY_URITWO, String.class);
							message.addField(Message.KEY_URITHREE, String.class);
						}
						else
						{
							System.out.println("message is null");
						}
					}
					else
					{
						System.out.println("schema is null");
					}
				}
				else
				{
					System.out.println("db version is updated");
				}
			}
			else
			{
				System.out.println("realm is null");
			}
		}
		catch(Exception e)
		{
			System.out.println(Migration.class.getName()+":migrate - Exception: " + e);

			if(Common.DEBUG)
			{
				e.printStackTrace();
				Utils.writeStringInFile(Migration.class.getName()+":migrate - Exception: " + e, "", null);
			}
		}
	}

	@Override
	public int hashCode()
	{
		return 37;
	}

	@Override
	public boolean equals(Object o)
	{
		return (o instanceof Migration);
	}
}