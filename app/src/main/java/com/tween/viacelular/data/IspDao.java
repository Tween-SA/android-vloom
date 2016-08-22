package com.tween.viacelular.data;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import com.tween.viacelular.models.Land;
import com.tween.viacelular.utils.Common;
import de.greenrobot.dao.AbstractDao;
import de.greenrobot.dao.Property;
import de.greenrobot.dao.internal.DaoConfig;

/**
 * Created by davidfigueroa on 25/8/15.
 */
public class IspDao extends AbstractDao<Isp, Long>
{
	public static final String TABLENAME		= "ISP";
	public static final String KEY_REGIONNAME	= "region_name";
	public static final String KEY_OPERATORNET	= "operator_network";
	public static final String KEY_OPERATORSIM	= "operator_sim";
	public static final String KEY_COUNTRYNET	= "country_network";
	public static final String KEY_COUNTRYSIM	= "country_sim";

	/**
	 * Properties of entity User.<br/> Can be used for QueryBuilder and for referencing column names.
	 */
	public static class Properties
	{
		public final static Property id				= new Property(0, Long.class, Common.KEY_ID, true, Common.KEY_ID);
		public final static Property as				= new Property(1, String.class, Isp.KEY_AS, false, Isp.KEY_AS);
		public final static Property status			= new Property(2, String.class, Common.KEY_STATUS, false, Common.KEY_STATUS);
		public final static Property country		= new Property(3, String.class, Isp.KEY_COUNTRY, false, Isp.KEY_COUNTRY);
		public final static Property countryCode	= new Property(4, String.class, Land.KEY_API, false, CountryDao.KEY_COUNTRYCODE);
		public final static Property region			= new Property(5, String.class, Isp.KEY_REGION, false, Isp.KEY_REGION);
		public final static Property regionName		= new Property(6, String.class, Isp.KEY_REGIONNAME, false, KEY_REGIONNAME);
		public final static Property city			= new Property(7, String.class, Isp.KEY_CITY, false, Isp.KEY_CITY);
		public final static Property zip			= new Property(8, String.class, Isp.KEY_ZIP, false, Isp.KEY_ZIP);
		public final static Property lat			= new Property(9, String.class, Isp.KEY_LAT, false, Isp.KEY_LAT);
		public final static Property lon			= new Property(10, String.class, Isp.KEY_LON, false, Isp.KEY_LON);
		public final static Property timezone		= new Property(11, String.class, Isp.KEY_TIMEZONE, false, Isp.KEY_TIMEZONE);
		public final static Property isp			= new Property(12, String.class, Isp.KEY_ISP, false, Isp.KEY_ISP);
		public final static Property org			= new Property(13, String.class, Isp.KEY_ORG, false, Isp.KEY_ORG);
		public final static Property query			= new Property(14, String.class, Isp.KEY_QUERY, false, Isp.KEY_QUERY);
		public final static Property operatorNet	= new Property(15, String.class, Isp.KEY_OPERATORNET, false, KEY_OPERATORNET);
		public final static Property opertatorSim	= new Property(16, String.class, Isp.KEY_OPERATORSIM, false, KEY_OPERATORSIM);
		public final static Property countryNet		= new Property(17, String.class, Isp.KEY_COUNTRYNET, false, KEY_COUNTRYNET);
		public final static Property countrySim		= new Property(18, String.class, Isp.KEY_COUNTRYSIM, false, KEY_COUNTRYSIM);
	}

	public IspDao(DaoConfig config)
	{
		super(config);
	}

	public IspDao(DaoConfig config, DaoSession daoSession)
	{
		super(config, daoSession);
	}

	/**
	 * Creates the underlying database table.
	 */
	public static void createTable(SQLiteDatabase db, boolean ifNotExists)
	{
		//Agregado para capturar excepciones
		try
		{
			String constraint = ifNotExists ? "IF NOT EXISTS " : "";
			db.execSQL("CREATE TABLE " + constraint + "'" + TABLENAME + "' ('" + Common.KEY_ID + "' INTEGER PRIMARY KEY, '" + Isp.KEY_AS + "' TEXT, '" + Common.KEY_STATUS + "' TEXT, '" + Isp.KEY_COUNTRY + "' TEXT, '" + CountryDao.KEY_COUNTRYCODE + "' TEXT, '" + Isp.KEY_REGION + "' TEXT, '" + KEY_REGIONNAME + "' TEXT, '" + Isp.KEY_CITY + "' TEXT, '" + Isp.KEY_ZIP + "' TEXT, '" + Isp.KEY_LAT + "' TEXT, '" + Isp.KEY_LON + "' TEXT, '" + Isp.KEY_TIMEZONE + "' TEXT, '" + Isp.KEY_ISP + "' TEXT, '" + Isp.KEY_ORG + "' TEXT, '" + Isp.KEY_QUERY + "' TEXT, '" + KEY_OPERATORNET + "' TEXT, '" + KEY_OPERATORSIM + "' TEXT, '" + KEY_COUNTRYNET + "' TEXT, '" + KEY_COUNTRYSIM + "' TEXT);");
		}
		catch(Exception e)
		{
			System.out.println("IspDao:createTable - Exception: " + e);
			if(Common.DEBUG)
			{
				e.printStackTrace();
			}
		}
	}

	/**
	 * Drops the underlying database table.
	 */
	public static void dropTable(SQLiteDatabase db, boolean ifExists)
	{
		//Agregado para capturar excepciones
		try
		{
			String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "'" + TABLENAME + "'";
			db.execSQL(sql);
		}
		catch(Exception e)
		{
			System.out.println("IspDao:dropTable - Exception: " + e);
			if(Common.DEBUG)
			{
				e.printStackTrace();
			}
		}
	}

	/**
	 * @inheritdoc
	 */
	@Override
	protected void bindValues(SQLiteStatement stmt, Isp entity)
	{
		//Agregado para capturar excepciones
		try
		{
			stmt.clearBindings();

			if(entity.getId() != null)
			{
				stmt.bindLong(1, entity.getId());
			}

			if(entity.getAs() != null)
			{
				stmt.bindString(2, entity.getAs());
			}

			if(entity.getStatus() != null)
			{
				stmt.bindString(3, entity.getStatus());
			}

			if(entity.getCountry() != null)
			{
				stmt.bindString(4, entity.getCountry());
			}

			if(entity.getCountryCode() != null)
			{
				stmt.bindString(5, entity.getCountryCode());
			}

			if(entity.getRegion() != null)
			{
				stmt.bindString(6, entity.getRegion());
			}

			if(entity.getRegionName() != null)
			{
				stmt.bindString(7, entity.getRegionName());
			}

			if(entity.getCity() != null)
			{
				stmt.bindString(8, entity.getCity());
			}

			if(entity.getZip() != null)
			{
				stmt.bindString(9, entity.getZip());
			}

			if(entity.getLat() != null)
			{
				stmt.bindString(10, entity.getLat());
			}

			if(entity.getLon() != null)
			{
				stmt.bindString(11, entity.getLon());
			}

			if(entity.getTimezone() != null)
			{
				stmt.bindString(12, entity.getTimezone());
			}

			if(entity.getIsp() != null)
			{
				stmt.bindString(13, entity.getIsp());
			}

			if(entity.getOrg() != null)
			{
				stmt.bindString(14, entity.getOrg());
			}

			if(entity.getQuery() != null)
			{
				stmt.bindString(15, entity.getQuery());
			}

			if(entity.getQuery() != null)
			{
				stmt.bindString(16, entity.getOperatorNet());
			}

			if(entity.getQuery() != null)
			{
				stmt.bindString(17, entity.getOperatorSim());
			}

			if(entity.getQuery() != null)
			{
				stmt.bindString(18, entity.getCountryNet());
			}

			if(entity.getQuery() != null)
			{
				stmt.bindString(19, entity.getCountrySim());
			}
		}
		catch(Exception e)
		{
			System.out.println("IspDao:bindValues - Exception: " + e);
			if(Common.DEBUG)
			{
				e.printStackTrace();
			}
		}
	}

	/**
	 * @inheritdoc
	 */
	@Override
	public Long readKey(Cursor cursor, int offset)
	{
		return cursor.isNull(offset) ? null : cursor.getLong(offset);
	}

	/**
	 * @inheritdoc
	 */
	@Override
	public Isp readEntity(Cursor cursor, int offset)
	{
		return new Isp(cursor.isNull(offset) ? null : cursor.getLong(offset),
				cursor.isNull(offset + 1) ? null : cursor.getString(offset + 1),
				cursor.isNull(offset + 2) ? null : cursor.getString(offset + 2),
				cursor.isNull(offset + 3) ? null : cursor.getString(offset + 3),
				cursor.isNull(offset + 4) ? null : cursor.getString(offset + 4),
				cursor.isNull(offset + 5) ? null : cursor.getString(offset + 5),
				cursor.isNull(offset + 6) ? null : cursor.getString(offset + 6),
				cursor.isNull(offset + 7) ? null : cursor.getString(offset + 7),
				cursor.isNull(offset + 8) ? null : cursor.getString(offset + 8),
				cursor.isNull(offset + 9) ? null : cursor.getString(offset + 9),
				cursor.isNull(offset + 10) ? null : cursor.getString(offset + 10),
				cursor.isNull(offset + 11) ? null : cursor.getString(offset + 11),
				cursor.isNull(offset + 12) ? null : cursor.getString(offset + 12),
				cursor.isNull(offset + 13) ? null : cursor.getString(offset + 13),
				cursor.isNull(offset + 14) ? null : cursor.getString(offset + 14),
				cursor.isNull(offset + 15) ? null : cursor.getString(offset + 15),
				cursor.isNull(offset + 16) ? null : cursor.getString(offset + 16),
				cursor.isNull(offset + 17) ? null : cursor.getString(offset + 17),
				cursor.isNull(offset + 18) ? null : cursor.getString(offset + 18));
	}

	/**
	 * @inheritdoc
	 */
	@Override
	public void readEntity(Cursor cursor, Isp entity, int offset)
	{
		//Agregado para capturar excepciones
		try
		{
			entity.setId(cursor.isNull(offset) ? null : cursor.getLong(offset));
			entity.setAs(cursor.isNull(offset + 1) ? null : cursor.getString(offset + 1));
			entity.setStatus(cursor.isNull(offset + 2) ? null : cursor.getString(offset + 2));
			entity.setCountry(cursor.isNull(offset + 3) ? null : cursor.getString(offset + 3));
			entity.setCountryCode(cursor.isNull(offset + 4) ? null : cursor.getString(offset + 4));
			entity.setRegion(cursor.isNull(offset + 5) ? null : cursor.getString(offset + 5));
			entity.setRegionName(cursor.isNull(offset + 6) ? null : cursor.getString(offset + 6));
			entity.setCity(cursor.isNull(offset + 7) ? null : cursor.getString(offset + 7));
			entity.setZip(cursor.isNull(offset + 8) ? null : cursor.getString(offset + 8));
			entity.setLat(cursor.isNull(offset + 9) ? null : cursor.getString(offset + 9));
			entity.setLon(cursor.isNull(offset + 10) ? null : cursor.getString(offset + 10));
			entity.setTimezone(cursor.isNull(offset + 11) ? null : cursor.getString(offset + 11));
			entity.setIsp(cursor.isNull(offset + 12) ? null : cursor.getString(offset + 12));
			entity.setOrg(cursor.isNull(offset + 13) ? null : cursor.getString(offset + 13));
			entity.setQuery(cursor.isNull(offset + 14) ? null : cursor.getString(offset + 14));
			entity.setOperatorNet(cursor.isNull(offset + 15) ? null : cursor.getString(offset + 15));
			entity.setOperatorSim(cursor.isNull(offset + 16) ? null : cursor.getString(offset + 16));
			entity.setCountryNet(cursor.isNull(offset + 17) ? null : cursor.getString(offset + 17));
			entity.setCountrySim(cursor.isNull(offset + 18) ? null : cursor.getString(offset + 18));
		}
		catch(Exception e)
		{
			System.out.println("IspDao:readEntity - Exception: " + e);
			if(Common.DEBUG)
			{
				e.printStackTrace();
			}
		}
	}

	/**
	 * @inheritdoc
	 */
	@Override
	protected Long updateKeyAfterInsert(Isp entity, long rowId)
	{
		entity.setId(rowId);
		return rowId;
	}

	/**
	 * @inheritdoc
	 */
	@Override
	public Long getKey(Isp entity)
	{
		if(entity != null)
		{
			return entity.getId();
		}
		else
		{
			return null;
		}
	}

	/**
	 * @inheritdoc
	 */
	@Override
	protected boolean isEntityUpdateable()
	{
		return true;
	}
}
