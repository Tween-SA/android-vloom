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
public class RegisteredAccountDao extends AbstractDao<RegisteredAccount, Long>
{
	public static final String TABLENAME = "REGISTERED_ACCOUNT";

	/**
	 * Properties of entity User.<br/> Can be used for QueryBuilder and for referencing column names.
	 */
	public static class Properties
	{
		public final static Property id		= new Property(0, Long.class, Common.KEY_ID, true, Common.KEY_ID);
		public final static Property name	= new Property(1, String.class, Common.KEY_NAME, false, Common.KEY_NAME);
		public final static Property type	= new Property(2, String.class, Common.KEY_TYPE, false, Common.KEY_TYPE);
	}

	public RegisteredAccountDao(DaoConfig config)
	{
		super(config);
	}

	public RegisteredAccountDao(DaoConfig config, DaoSession daoSession)
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
			db.execSQL("CREATE TABLE " + constraint + "'" + TABLENAME + "' ('" + Common.KEY_ID + "' INTEGER PRIMARY KEY, '" + Common.KEY_NAME + "' TEXT, '" + Common.KEY_TYPE + "' TEXT);");
		}
		catch(Exception e)
		{
			System.out.println("RegisteredAccountDao:createTable - Exception: " + e);
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
			System.out.println("RegisteredAccountDao:dropTable - Exception: " + e);
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
	protected void bindValues(SQLiteStatement stmt, RegisteredAccount entity)
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

			if(entity.getType() != null)
			{
				stmt.bindString(3, entity.getType());
			}
		}
		catch(Exception e)
		{
			System.out.println("RegisteredAccountDao:bindValues - Exception: " + e);
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
	public RegisteredAccount readEntity(Cursor cursor, int offset)
	{
		return new RegisteredAccount(cursor.isNull(offset) ? null : cursor.getLong(offset), cursor.isNull(offset + 1) ? null : cursor.getString(offset + 1), cursor.isNull(offset + 2) ? null : cursor.getString(offset + 2));
	}

	/**
	 * @inheritdoc
	 */
	@Override
	public void readEntity(Cursor cursor, RegisteredAccount entity, int offset)
	{
		//Agregado para capturar excepciones
		try
		{
			entity.setId(cursor.isNull(offset) ? null : cursor.getLong(offset));
			entity.setName(cursor.isNull(offset + 1) ? null : cursor.getString(offset + 1));
			entity.setType(cursor.isNull(offset + 2) ? null : cursor.getString(offset + 2));
		}
		catch(Exception e)
		{
			System.out.println("RegisteredAccountDao:readEntity - Exception: " + e);
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
	protected Long updateKeyAfterInsert(RegisteredAccount entity, long rowId)
	{
		entity.setId(rowId);
		return rowId;
	}

	/**
	 * @inheritdoc
	 */
	@Override
	public Long getKey(RegisteredAccount entity)
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