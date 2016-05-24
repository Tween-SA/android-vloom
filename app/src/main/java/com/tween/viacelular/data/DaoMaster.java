package com.tween.viacelular.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import com.tween.viacelular.utils.Common;
import de.greenrobot.dao.AbstractDaoMaster;
import de.greenrobot.dao.identityscope.IdentityScopeType;

/**
 * Created by davidfigueroa on 25/8/15.
 */
public class DaoMaster extends AbstractDaoMaster
{
	public static final int SCHEMA_VERSION	= 1010;
	public static final String DB_NAME		= "viacelular-db";
	public static final String DB_PATH		= "/data/data/com.tween.viacelular/databases/";

	/**
	 * Creates underlying database table using DAOs.
	 */
	public static void createAllTables(SQLiteDatabase db, boolean ifNotExists)
	{
		//Agregado para prevenir excepciones
		try
		{
			UserDao.createTable(db, ifNotExists);
			RegisteredAccountDao.createTable(db, ifNotExists);
			MessageDao.createTable(db, ifNotExists);
			CountryDao.createTable(db, ifNotExists);
			CompanyDao.createTable(db, ifNotExists);
			IspDao.createTable(db, ifNotExists);
		}
		catch(Exception e)
		{
			System.out.println("DaoMaster:createAllTables - Exception: " + e);
			if(Common.DEBUG)
			{
				e.printStackTrace();
			}
		}
	}

	/**
	 * Drops underlying database table using DAOs.
	 */
	public static void dropAllTables(SQLiteDatabase db, boolean ifExists)
	{
		//Agregado para prevenir excepciones
		try
		{
			UserDao.dropTable(db, ifExists);
			RegisteredAccountDao.dropTable(db, ifExists);
			MessageDao.dropTable(db, ifExists);
			CountryDao.dropTable(db, ifExists);
			CompanyDao.dropTable(db, ifExists);
			IspDao.dropTable(db, ifExists);
		}
		catch(Exception e)
		{
			System.out.println("DaoMaster:dropAllTables - Exception: " + e);
			if(Common.DEBUG)
			{
				e.printStackTrace();
			}
		}
	}

	public static abstract class OpenHelper extends SQLiteOpenHelper
	{
		public OpenHelper(Context context, String name, CursorFactory factory)
		{
			super(context, name, factory, SCHEMA_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db)
		{
			//Agregado para prevenir excepciones
			try
			{
				if(Common.DEBUG)
				{
					System.out.println("greenDAO - Creating tables for schema version " + SCHEMA_VERSION);
				}

				createAllTables(db, false);
			}
			catch(Exception e)
			{
				System.out.println("DaoMaster:OpenHelper:onCreate - Exception: " + e);
				if(Common.DEBUG)
				{
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * WARNING: Drops all table on Upgrade! Use only during development.
	 */
	public static class DevOpenHelper extends OpenHelper
	{
		public DevOpenHelper(Context context, String name, CursorFactory factory)
		{
			super(context, name, factory);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
		{
			//Agregado para prevenir excepciones
			try
			{
				if(Common.DEBUG)
				{
					System.out.println("greenDAO - Actualizando desde versión " + oldVersion + " a " + newVersion + " ejecutando scripts de sql necesarios");
				}

				//Se quitaron las sentencias dropAllTables(db, true); y onCreate(db); para que posteriormente sean reemplazadas por script para de alter tables
				//Actualización de campos en Company, quitar en futura versión
				db.execSQL("ALTER TABLE " + CompanyDao.TABLENAME + " ADD COLUMN '"+Company.KEY_SUSCRIBE+"' INTEGER DEFAULT 0;");
				db.execSQL("ALTER TABLE " + CompanyDao.TABLENAME + " ADD COLUMN '"+Company.KEY_URL+"' TEXT;");
				db.execSQL("ALTER TABLE " + CompanyDao.TABLENAME + " ADD COLUMN '"+User.KEY_PHONE+"' TEXT;");
				db.execSQL("ALTER TABLE " + CompanyDao.TABLENAME + " ADD COLUMN '"+CompanyDao.KEY_MSGEXAMPLES+"' TEXT DEFAULT '[]';");
				db.execSQL("ALTER TABLE " + CompanyDao.TABLENAME + " ADD COLUMN '"+Company.KEY_ABOUT+"' TEXT;");
				db.execSQL("ALTER TABLE " + CompanyDao.TABLENAME + " ADD COLUMN '"+CompanyDao.KEY_IDENTIFICATIONKEY+"' TEXT;");
				db.execSQL("ALTER TABLE " + CompanyDao.TABLENAME + " ADD COLUMN '"+CompanyDao.KEY_DATASENT+"' TEXT DEFAULT 0;");
				db.execSQL("ALTER TABLE " + CompanyDao.TABLENAME + " ADD COLUMN '"+CompanyDao.KEY_IDENTIFICATIONVALUE+"' TEXT;");
				db.execSQL("ALTER TABLE " + CompanyDao.TABLENAME + " ADD COLUMN '"+Company.KEY_FOLLOWER+"' INTEGER DEFAULT 0;");
				db.execSQL("ALTER TABLE " + CompanyDao.TABLENAME + " ADD COLUMN '"+Company.KEY_GRAY+"' INTEGER DEFAULT 0;");
			}
			catch(Exception e)
			{
				System.out.println("DaoMaster:DevOpenHelper:onUpgrade - Exception: " + e);
				if(Common.DEBUG)
				{
					e.printStackTrace();
				}
			}
		}
	}

	public DaoMaster(SQLiteDatabase db)
	{
		super(db, SCHEMA_VERSION);
		//Agregado para prevenir excepciones
		try
		{
			registerDaoClass(UserDao.class);
			registerDaoClass(RegisteredAccountDao.class);
			registerDaoClass(MessageDao.class);
			registerDaoClass(CountryDao.class);
			registerDaoClass(CompanyDao.class);
			registerDaoClass(IspDao.class);
		}
		catch(Exception e)
		{
			System.out.println("DaoMaster:construct - Exception: " + e);
			if(Common.DEBUG)
			{
				e.printStackTrace();
			}
		}
	}

	public DaoSession newSession()
	{
		DaoSession session = null;
		//Agregado para prevenir excepciones
		try
		{
			session = new DaoSession(db, IdentityScopeType.Session, daoConfigMap);
		}
		catch(Exception e)
		{
			System.out.println("DaoMaster:newSession - Exception: " + e);
			if(Common.DEBUG)
			{
				e.printStackTrace();
			}
		}

		return session;
	}

	public DaoSession newSession(IdentityScopeType type)
	{
		DaoSession session = null;
		//Agregado para prevenir excepciones
		try
		{
			session = new DaoSession(db, type, daoConfigMap);
		}
		catch(Exception e)
		{
			System.out.println("DaoMaster:newSession - Exception: " + e);
			if(Common.DEBUG)
			{
				e.printStackTrace();
			}
		}

		return session;
	}

	public static DaoSession openDB(Context context)
	{
		DaoSession session = null;
		//Agregado para prevenir excepciones
		try
		{
			DevOpenHelper helper	= new DaoMaster.DevOpenHelper(context, DB_NAME, null);
			SQLiteDatabase db		= helper.getWritableDatabase();
			DaoMaster daoMaster		= new DaoMaster(db);
			session					= daoMaster.newSession();
		}
		catch(Exception e)
		{
			System.out.println("DaoMaster:openDB - Exception: " + e);
			if(Common.DEBUG)
			{
				e.printStackTrace();
			}
		}

		return session;
	}

	public static DaoSession openRdb(Context context)
	{
		DaoSession session = null;
		//Agregado para prevenir excepciones
		try
		{
			DevOpenHelper helper	= new DaoMaster.DevOpenHelper(context, DB_NAME, null);
			SQLiteDatabase db		= helper.getReadableDatabase();
			DaoMaster daoMaster		= new DaoMaster(db);
			session					= daoMaster.newSession();
		}
		catch(Exception e)
		{
			System.out.println("DaoMaster:openRdb - Exception: " + e);
			if(Common.DEBUG)
			{
				e.printStackTrace();
			}
		}

		return session;
	}

	public static void closeDB(DaoSession daoSession)
	{
		//Agregado para prevenir excepciones
		try
		{
			if(daoSession != null)
			{
				if(daoSession.getDatabase() != null)
				{
					if(daoSession.getDatabase().isOpen())
					{
						daoSession.getDatabase().close();
					}
				}

				daoSession.clear();
			}
		}
		catch(Exception e)
		{
			System.out.println("DaoMaster:closeDB - Exception: " + e);
			if(Common.DEBUG)
			{
				e.printStackTrace();
			}
		}
	}
}