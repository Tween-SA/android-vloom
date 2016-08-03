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
public class MessageDao extends AbstractDao<Message, Long>
{
	public static final String TABLENAME	= "MESSAGE";
	public static final String KEY_MSGID	= "msg_id";

	/**
	 * Properties of entity User.<br/> Can be used for QueryBuilder and for referencing column names.
	 */
	public static class Properties
	{
		public final static Property id				= new Property(0, Long.class, Common.KEY_ID, true, Common.KEY_ID);
		public final static Property msgId			= new Property(1, String.class, Message.KEY_API, false, KEY_MSGID);
		public final static Property type			= new Property(2, String.class, Common.KEY_TYPE, false, Common.KEY_TYPE);
		public final static Property msg			= new Property(3, String.class, Message.KEY_MSG, false, Message.KEY_MSG);
		public final static Property channel		= new Property(4, String.class, Message.KEY_CHANNEL, false, Message.KEY_CHANNEL);
		public final static Property status			= new Property(5, Integer.class, Common.KEY_STATUS, false, Common.KEY_STATUS);
		public final static Property companyId		= new Property(6, String.class, Company.KEY_API, false, CompanyDao.KEY_COMPANYID);
		public final static Property ttd			= new Property(7, Integer.class, Message.KEY_TTD, false, Message.KEY_TTD);
		public final static Property phone			= new Property(8, String.class, User.KEY_PHONE, false, User.KEY_PHONE);
		public final static Property countryCode	= new Property(9, String.class, Land.KEY_API, false, CountryDao.KEY_COUNTRYCODE);
		public final static Property flags			= new Property(10, String.class, Message.KEY_FLAGS, false, Message.KEY_FLAGS);
		public final static Property created		= new Property(11, Long.class, Message.KEY_CREATED, false, Message.KEY_CREATED);
		public final static Property processed		= new Property(12, Long.class, Message.KEY_PROCESSED, false, Message.KEY_PROCESSED);
		public final static Property delivered		= new Property(13, Long.class, Message.KEY_DELIVERED, false, Message.KEY_DELIVERED);
		public final static Property acknowladged	= new Property(14, Long.class, Message.KEY_ACKNOWLADGED, false, Message.KEY_ACKNOWLADGED);
		public final static Property deleted		= new Property(15, Integer.class, Message.KEY_DELETED, false, Message.KEY_DELETED);
	}

	public MessageDao(DaoConfig config)
	{
		super(config);
	}

	public MessageDao(DaoConfig config, DaoSession daoSession)
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
			db.execSQL("CREATE TABLE " + constraint + "'" + TABLENAME + "' ('" + Common.KEY_ID + "' INTEGER PRIMARY KEY, '" + KEY_MSGID + "' TEXT, '" + Common.KEY_TYPE + "' TEXT, '" + Message.KEY_MSG + "' TEXT, '" + Message.KEY_CHANNEL + "' TEXT, '" + Common.KEY_STATUS + "' INTEGER, '" + CompanyDao.KEY_COMPANYID + "' TEXT, '" + Message.KEY_TTD + "' INTEGER, '" + User.KEY_PHONE + "' TEXT, '" + CountryDao.KEY_COUNTRYCODE + "' TEXT, '" + Message.KEY_FLAGS + "' TEXT, '" + Message.KEY_CREATED + "' INTEGER, '" + Message.KEY_PROCESSED + "' INTEGER, '" + Message.KEY_DELIVERED + "' INTEGER, '" + Message.KEY_ACKNOWLADGED + "' INTEGER, '" + Message.KEY_DELETED + "' INTEGER);");
			db.execSQL("CREATE INDEX idx_mcompany ON " + TABLENAME + "(" + CompanyDao.KEY_COMPANYID + ");");
		}
		catch(Exception e)
		{
			System.out.println("MessageDao:createTable - Exception: " + e);
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
			db.execSQL("DROP INDEX " + (ifExists ? "IF EXISTS " : "") + "idx_mcompany;");
		}
		catch(Exception e)
		{
			System.out.println("MessageDao:dropTable - Exception: " + e);
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
	protected void bindValues(SQLiteStatement stmt, Message entity)
	{
		//Agregado para capturar excepciones
		try
		{
			stmt.clearBindings();

			if(entity.getId() != null)
			{
				stmt.bindLong(1, entity.getId());
			}

			if(entity.getMsgId() != null)
			{
				stmt.bindString(2, entity.getMsgId());
			}

			if(entity.getType() != null)
			{
				stmt.bindString(3, entity.getType());
			}

			if(entity.getMsg() != null)
			{
				stmt.bindString(4, entity.getMsg());
			}

			if(entity.getChannel() != null)
			{
				stmt.bindString(5, entity.getChannel());
			}

			stmt.bindLong(6, entity.getStatus());

			if(entity.getCompanyId() != null)
			{
				stmt.bindString(7, entity.getCompanyId());
			}

			stmt.bindLong(8, entity.getTtd());

			if(entity.getPhone() != null)
			{
				stmt.bindString(9, entity.getPhone());
			}

			if(entity.getCountryCode() != null)
			{
				stmt.bindString(10, entity.getCountryCode());
			}

			if(entity.getFlags() != null)
			{
				stmt.bindString(11, entity.getFlags());
			}

			stmt.bindLong(12, entity.getCreated());
			stmt.bindLong(13, entity.getProcessed());
			stmt.bindLong(14, entity.getDelivered());
			stmt.bindLong(15, entity.getAcknowladged());
			stmt.bindLong(16, entity.getDeleted());
		}
		catch(Exception e)
		{
			System.out.println("MessageDao:bindValues - Exception: " + e);
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
	public Message readEntity(Cursor cursor, int offset)
	{
		return new Message(cursor.isNull(offset) ? null : cursor.getLong(offset),
				cursor.isNull(offset + 1) ? null : cursor.getString(offset + 1),
				cursor.isNull(offset + 2) ? null : cursor.getString(offset + 2),
				cursor.isNull(offset + 3) ? null : cursor.getString(offset + 3),
				cursor.isNull(offset + 4) ? null : cursor.getString(offset + 4),
				cursor.isNull(offset + 5) ? null : cursor.getInt(offset + 5),
				cursor.isNull(offset + 6) ? null : cursor.getString(offset + 6),
				cursor.isNull(offset + 7) ? null : cursor.getInt(offset + 7),
				cursor.isNull(offset + 8) ? null : cursor.getString(offset + 8),
				cursor.isNull(offset + 9) ? null : cursor.getString(offset + 9),
				cursor.isNull(offset + 10) ? null : cursor.getString(offset + 10),
				cursor.isNull(offset + 11) ? null : cursor.getLong(offset + 11),
				cursor.isNull(offset + 12) ? null : cursor.getLong(offset + 12),
				cursor.isNull(offset + 13) ? null : cursor.getLong(offset + 13),
				cursor.isNull(offset + 14) ? null : cursor.getLong(offset + 14),
				cursor.isNull(offset + 15) ? null : cursor.getInt(offset + 15));
	}

	/**
	 * @inheritdoc
	 */
	@Override
	public void readEntity(Cursor cursor, Message entity, int offset)
	{
		//Agregado para capturar excepciones
		try
		{
			entity.setId(cursor.isNull(offset) ? null : cursor.getLong(offset));
			entity.setMsgId(cursor.isNull(offset + 1) ? null : cursor.getString(offset + 1));
			entity.setType(cursor.isNull(offset + 2) ? null : cursor.getString(offset + 2));
			entity.setMsg(cursor.isNull(offset + 3) ? null : cursor.getString(offset + 3));
			entity.setChannel(cursor.isNull(offset + 4) ? null : cursor.getString(offset + 4));
			entity.setStatus(cursor.isNull(offset + 5) ? null : cursor.getInt(offset + 5));
			entity.setCompanyId(cursor.isNull(offset + 6) ? null : cursor.getString(offset + 6));
			entity.setTtd(cursor.isNull(offset + 7) ? null : cursor.getInt(offset + 7));
			entity.setPhone(cursor.isNull(offset + 8) ? null : cursor.getString(offset + 8));
			entity.setCountryCode(cursor.isNull(offset + 9) ? null : cursor.getString(offset + 9));
			entity.setFlags(cursor.isNull(offset + 10) ? null : cursor.getString(offset + 10));
			entity.setCreated(cursor.isNull(offset + 11) ? null : cursor.getLong(offset + 11));
			entity.setProcessed(cursor.isNull(offset + 12) ? null : cursor.getLong(offset + 12));
			entity.setDelivered(cursor.isNull(offset + 13) ? null : cursor.getLong(offset + 13));
			entity.setAcknowladged(cursor.isNull(offset + 14) ? null : cursor.getLong(offset + 14));
			entity.setDeleted(cursor.isNull(offset + 15) ? null : cursor.getInt(offset + 15));
		}
		catch(Exception e)
		{
			System.out.println("MessageDao:readEntity - Exception: " + e);
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
	protected Long updateKeyAfterInsert(Message entity, long rowId)
	{
		entity.setId(rowId);
		return rowId;
	}

	/**
	 * @inheritdoc
	 */
	@Override
	public Long getKey(Message entity)
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