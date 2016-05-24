package com.tween.viacelular.data;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import com.tween.viacelular.utils.Common;
import de.greenrobot.dao.AbstractDao;
import de.greenrobot.dao.Property;
import de.greenrobot.dao.internal.DaoConfig;

/**
 * Created by davidfigueroa on 25/8/15.
 */
public class CountryDao extends AbstractDao<Country, Long>
{
	public static final String TABLENAME		= "COUNTRY";
	public static final String KEY_COUNTRYCODE	= "country_code";
	public static final String KEY_ISOCODE		= "iso_code";
	public static final String KEY_MINLENGTH	= "min_length";
	public static final String KEY_MAXLENGHT	= "max_length";

	/**
	 * Properties of entity User.<br/> Can be used for QueryBuilder and for referencing column names.
	 */
	public static class Properties
	{
		public final static Property id			= new Property(0, Long.class, Common.KEY_ID, true, Common.KEY_ID);
		public final static Property name		= new Property(1, String.class, Common.KEY_NAME, false, Common.KEY_NAME);
		public final static Property code		= new Property(2, String.class, Country.KEY_CODE, false, Country.KEY_CODE);
		public final static Property isoCode	= new Property(3, String.class, Country.KEY_ISOCODE, false, KEY_ISOCODE);
		public final static Property format		= new Property(4, String.class, Country.KEY_FORMAT, false, Country.KEY_FORMAT);
		public final static Property minLength	= new Property(5, String.class, Country.KEY_MINLENGTH, false, KEY_MINLENGTH);
		public final static Property maxLength	= new Property(6, String.class, Country.KEY_MAXLENGHT, false, KEY_MAXLENGHT);
	}

	public CountryDao(DaoConfig config)
	{
		super(config);
	}

	public CountryDao(DaoConfig config, DaoSession daoSession)
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
			db.execSQL("CREATE TABLE " + constraint + "'" + TABLENAME + "' ('" + Common.KEY_ID + "' INTEGER PRIMARY KEY, '" + Common.KEY_NAME + "' TEXT, '" + Country.KEY_CODE + "' TEXT, '" + KEY_ISOCODE + "', '" + Country.KEY_FORMAT + "', '" + KEY_MINLENGTH + "', '" + KEY_MAXLENGHT + "' TEXT);");
			db.execSQL("CREATE INDEX idx_cname ON " + TABLENAME + "(" + Common.KEY_NAME + ");");
		}
		catch(Exception e)
		{
			System.out.println("CountryDao:createTable - Exception: " + e);
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
			db.execSQL("DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "'" + TABLENAME + "'");
			db.execSQL("DROP INDEX " + (ifExists ? "IF EXISTS " : "") + "idx_cname;");
		}
		catch(Exception e)
		{
			System.out.println("CountryDao:dropTable - Exception: " + e);
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
	protected void bindValues(SQLiteStatement stmt, Country entity)
	{
		//Agregado para capturar excepciones
		try
		{
			stmt.clearBindings();

			if(entity.getId() != null)
			{
				stmt.bindLong(1, entity.getId());
			}

			if(entity.getName() != null)
			{
				stmt.bindString(2, entity.getName());
			}

			if(entity.getCode() != null)
			{
				stmt.bindString(3, entity.getCode());
			}

			if(entity.getIsoCode() != null)
			{
				stmt.bindString(4, entity.getIsoCode());
			}

			if(entity.getFormat() != null)
			{
				stmt.bindString(5, entity.getFormat());
			}

			if(entity.getMinLength() != null)
			{
				stmt.bindString(6, entity.getMinLength());
			}

			if(entity.getMaxLength() != null)
			{
				stmt.bindString(7, entity.getMaxLength());
			}
		}
		catch(Exception e)
		{
			System.out.println("CountryDao:bindValues - Exception: " + e);
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
	public Country readEntity(Cursor cursor, int offset)
	{
		return new Country(	cursor.isNull(offset) ? null : cursor.getLong(offset), cursor.isNull(offset + 1) ? null : cursor.getString(offset + 1),
							cursor.isNull(offset + 2) ? null : cursor.getString(offset + 2), cursor.isNull(offset + 3) ? null : cursor.getString(offset + 3),
							cursor.isNull(offset + 4) ? null : cursor.getString(offset + 4), cursor.isNull(offset + 5) ? null : cursor.getString(offset + 5),
							cursor.isNull(offset + 6) ? null : cursor.getString(offset + 6));
	}

	/**
	 * @inheritdoc
	 */
	@Override
	public void readEntity(Cursor cursor, Country entity, int offset)
	{
		//Agregado para capturar excepciones
		try
		{
			entity.setId(cursor.isNull(offset) ? null : cursor.getLong(offset));
			entity.setName(cursor.isNull(offset + 1) ? null : cursor.getString(offset + 1));
			entity.setCode(cursor.isNull(offset + 2) ? null : cursor.getString(offset + 2));
			entity.setIsoCode(cursor.isNull(offset + 3) ? null : cursor.getString(offset + 3));
			entity.setFormat(cursor.isNull(offset + 4) ? null : cursor.getString(offset + 4));
			entity.setMinLength(cursor.isNull(offset + 5) ? null : cursor.getString(offset + 5));
			entity.setMaxLength(cursor.isNull(offset + 6) ? null : cursor.getString(offset + 6));
		}
		catch(Exception e)
		{
			System.out.println("CountryDao:readEntity - Exception: " + e);
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
	protected Long updateKeyAfterInsert(Country entity, long rowId)
	{
		entity.setId(rowId);
		return rowId;
	}

	/**
	 * @inheritdoc
	 */
	@Override
	public Long getKey(Country entity)
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