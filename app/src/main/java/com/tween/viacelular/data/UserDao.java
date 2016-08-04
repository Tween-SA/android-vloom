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
public class UserDao extends AbstractDao<User, Long>
{
	public static final String TABLENAME		= "USER";
	public static final String KEY_FIRSTNAME	= "first_name";
	public static final String KEY_LASTNAME		= "last_name";
	public static final String KEY_USERID		= "user_id";
	public static final String KEY_GCMID		= "gcm_id";
	public static final String KEY_FACEBOOK		= "facebook_id";
	public static final String KEY_GOOGLE		= "google_id";

	/**
	 * Properties of entity User.<br/> Can be used for QueryBuilder and for referencing column names.
	 */
	public static class Properties
	{
		public final static Property id				= new Property(0, Long.class, Common.KEY_ID, true, Common.KEY_ID);
		public final static Property firstName		= new Property(1, String.class, User.KEY_FIRSTNAME, false, KEY_FIRSTNAME);
		public final static Property lastName		= new Property(2, String.class, User.KEY_LASTNAME, false, KEY_LASTNAME);
		public final static Property phone			= new Property(3, String.class, User.KEY_PHONE, false, User.KEY_PHONE);
		public final static Property email			= new Property(4, String.class, User.KEY_EMAIL, false, User.KEY_EMAIL);
		public final static Property userId			= new Property(5, String.class, User.KEY_API, false, KEY_USERID);
		public final static Property gcmId			= new Property(6, String.class, User.KEY_GCMID, false, KEY_GCMID);
		public final static Property status			= new Property(7, Integer.class, Common.KEY_STATUS, false, Common.KEY_STATUS);
		public final static Property countryCode	= new Property(8, String.class, Land.KEY_API, false, CountryDao.KEY_COUNTRYCODE);
		public final static Property facebookId		= new Property(9, String.class, User.KEY_FACEBOOK, false, KEY_FACEBOOK);
		public final static Property googleId		= new Property(10, String.class, User.KEY_GOOGLE, false, KEY_GOOGLE);
		public final static Property password		= new Property(11, String.class, User.KEY_PASSWORD, false, User.KEY_PASSWORD);
	}

	public UserDao(DaoConfig config)
	{
		super(config);
	}

	public UserDao(DaoConfig config, DaoSession daoSession)
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
			db.execSQL("CREATE TABLE " + constraint + "'" + TABLENAME + "' ('" + Common.KEY_ID + "' INTEGER PRIMARY KEY, '" + KEY_FIRSTNAME + "' TEXT, '" + KEY_LASTNAME + "' TEXT, '" + User.KEY_PHONE + "' TEXT, '" + User.KEY_EMAIL + "' TEXT, '" + KEY_USERID + "' TEXT, '" + KEY_GCMID + "' TEXT, '" + Common.KEY_STATUS + "' INTEGER, '" + CountryDao.KEY_COUNTRYCODE + "' TEXT, '" + KEY_FACEBOOK + "' TEXT, '" + KEY_GOOGLE + "' TEXT, '" + User.KEY_PASSWORD + "' TEXT);");
		}
		catch(Exception e)
		{
			System.out.println("UserDao:createTable - Exception: " + e);
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
			System.out.println("UserDao:dropTable - Exception: " + e);
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
	protected void bindValues(SQLiteStatement stmt, User entity)
	{
		//Agregado para capturar excepciones
		try
		{
			stmt.clearBindings();

			if(entity.getId() != null)
			{
				stmt.bindLong(1, entity.getId());
			}

			if(entity.getFirstName() != null)
			{
				stmt.bindString(2, entity.getFirstName());
			}

			if(entity.getLastName() != null)
			{
				stmt.bindString(3, entity.getLastName());
			}

			if(entity.getPhone() != null)
			{
				stmt.bindString(4, entity.getPhone());
			}

			if(entity.getEmail() != null)
			{
				stmt.bindString(5, entity.getEmail());
			}

			if(entity.getUserId() != null)
			{
				stmt.bindString(6, entity.getUserId());
			}

			if(entity.getGcmId() != null)
			{
				stmt.bindString(7, entity.getGcmId());
			}

			stmt.bindLong(8, entity.getStatus());

			if(entity.getCountryCode() != null)
			{
				stmt.bindString(9, entity.getCountryCode());
			}

			if(entity.getFacebookId() != null)
			{
				stmt.bindString(10, entity.getFacebookId());
			}

			if(entity.getGoogleId() != null)
			{
				stmt.bindString(11, entity.getGoogleId());
			}

			if(entity.getPassword() != null)
			{
				stmt.bindString(12, entity.getPassword());
			}
		}
		catch(Exception e)
		{
			System.out.println("UserDao:bindValues - Exception: " + e);
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
	public User readEntity(Cursor cursor, int offset)
	{
		return new User(cursor.isNull(offset) ? null : cursor.getLong(offset),
				cursor.isNull(offset + 1) ? null : cursor.getString(offset + 1),
				cursor.isNull(offset + 2) ? null : cursor.getString(offset + 2),
				cursor.isNull(offset + 3) ? null : cursor.getString(offset + 3),
				cursor.isNull(offset + 4) ? null : cursor.getString(offset + 4),
				cursor.isNull(offset + 5) ? null : cursor.getString(offset + 5),
				cursor.isNull(offset + 6) ? null : cursor.getString(offset + 6),
				cursor.isNull(offset + 7) ? null : cursor.getInt(offset + 7),
				cursor.isNull(offset + 8) ? null : cursor.getString(offset + 8),
				cursor.isNull(offset + 9) ? null : cursor.getString(offset + 9),
				cursor.isNull(offset + 10) ? null : cursor.getString(offset + 10),
				cursor.isNull(offset + 11) ? null : cursor.getString(offset + 11));
	}

	/**
	 * @inheritdoc
	 */
	@Override
	public void readEntity(Cursor cursor, User entity, int offset)
	{
		//Agregado para capturar excepciones
		try
		{
			entity.setId(cursor.isNull(offset) ? null : cursor.getLong(offset));
			entity.setFirstName(cursor.isNull(offset + 1) ? null : cursor.getString(offset + 1));
			entity.setLastName(cursor.isNull(offset + 2) ? null : cursor.getString(offset + 2));
			entity.setPhone(cursor.isNull(offset + 3) ? null : cursor.getString(offset + 3));
			entity.setEmail(cursor.isNull(offset + 4) ? null : cursor.getString(offset + 4));
			entity.setUserId(cursor.isNull(offset + 5) ? null : cursor.getString(offset + 5));
			entity.setGcmId(cursor.isNull(offset + 6) ? null : cursor.getString(offset + 6));
			entity.setStatus(cursor.isNull(offset + 7) ? null : cursor.getInt(offset + 7));
			entity.setCountryCode(cursor.isNull(offset + 8) ? null : cursor.getString(offset + 8));
			entity.setFacebookId(cursor.isNull(offset + 9) ? null : cursor.getString(offset + 9));
			entity.setGoogleId(cursor.isNull(offset + 10) ? null : cursor.getString(offset + 10));
			entity.setPassword(cursor.isNull(offset + 11) ? null : cursor.getString(offset + 11));
		}
		catch(Exception e)
		{
			System.out.println("UserDao:readEntity - Exception: " + e);
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
	protected Long updateKeyAfterInsert(User entity, long rowId)
	{
		entity.setId(rowId);
		return rowId;
	}

	/**
	 * @inheritdoc
	 */
	@Override
	public Long getKey(User entity)
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
