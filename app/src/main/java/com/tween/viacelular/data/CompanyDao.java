package com.tween.viacelular.data;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import com.tween.viacelular.models.Migration;
import com.tween.viacelular.services.ApiConnection;
import com.tween.viacelular.utils.Common;
import com.tween.viacelular.utils.StringUtils;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;
import de.greenrobot.dao.AbstractDao;
import de.greenrobot.dao.Property;
import de.greenrobot.dao.internal.DaoConfig;
import io.realm.Realm;

/**
 * Created by davidfigueroa on 25/8/15.
 */
public class CompanyDao extends AbstractDao<Company, Long>
{
	public static final String TABLENAME				= "COMPANY";
	public static final String KEY_COMPANYID			= "company_id";
	public static final String KEY_INDUSTRYCODE			= "industry_code";
	public static final String KEY_COLOR				= "color_hex";
	public static final String KEY_NUMBERS				= "from_numbers";
	public static final String KEY_MSGEXAMPLES			= "msg_examples";
	public static final String KEY_IDENTIFICATIONKEY	= "identification_key";
	public static final String KEY_DATASENT				= "data_sent";
	public static final String KEY_IDENTIFICATIONVALUE	= "identification_value";
	public static final String SELECT_FIELDS			= TABLENAME + "." + Common.KEY_ID + ", " + TABLENAME + "." + KEY_COMPANYID + ", " + TABLENAME + "." + Common.KEY_NAME + ", " + TABLENAME + "." + CountryDao.KEY_COUNTRYCODE + ", " + TABLENAME + "." + KEY_INDUSTRYCODE + ", " + TABLENAME + "." + Company.KEY_INDUSTRY + ", " + TABLENAME + "." + Common.KEY_TYPE + ", " + TABLENAME + "." + Company.KEY_IMAGE + ", " + TABLENAME + "." + KEY_COLOR + ", " + TABLENAME + "." + KEY_NUMBERS + ", " + TABLENAME + "." + Company.KEY_KEYWORDS + ", " + TABLENAME + "." + Company.KEY_UNSUSCRIBE + ", " + TABLENAME + "." + Company.KEY_SIZE + ", " + TABLENAME + "." + Common.KEY_STATUS + ", " + TABLENAME + "." + Company.KEY_SILENCED + ", " + TABLENAME + "." + Company.KEY_BLOCKED + ", " + TABLENAME + "." + User.KEY_EMAIL + ", " + TABLENAME + "." + UserDao.KEY_FIRSTNAME + ", " + TABLENAME + "." + UserDao.KEY_LASTNAME + ", " + TABLENAME + "." + User.KEY_PASSWORD + ", " + TABLENAME + "." + Company.KEY_RECEIVE + ", " + TABLENAME + "." + Company.KEY_SUSCRIBE + ", " + TABLENAME + "." + Company.KEY_URL + ", " + TABLENAME + "." + User.KEY_PHONE + ", " + TABLENAME + "." + KEY_MSGEXAMPLES + ", " + TABLENAME + "." + Company.KEY_ABOUT + ", " + TABLENAME + "." + KEY_IDENTIFICATIONKEY + ", " + TABLENAME + "." + KEY_DATASENT + ", " + TABLENAME + "." + KEY_IDENTIFICATIONVALUE + ", " + TABLENAME + "." + Company.KEY_FOLLOWER + ", " + TABLENAME + "." + Company.KEY_GRAY;
	public static final String FROM_CLAUSE				= " FROM " + TABLENAME + " INNER JOIN " + MessageDao.TABLENAME + " ON " + MessageDao.TABLENAME + "." + KEY_COMPANYID + " = " + TABLENAME + "." + KEY_COMPANYID;

	/**
	 * Properties of entity User.<br/> Can be used for QueryBuilder and for referencing column names.
	 */
	public static class Properties
	{
		public final static Property id					= new Property(0, Long.class, Common.KEY_ID, true, Common.KEY_ID);
		public final static Property companyId			= new Property(1, String.class, Company.KEY_API, false, KEY_COMPANYID);
		public final static Property name				= new Property(2, String.class, Common.KEY_NAME, false, Common.KEY_NAME);
		public final static Property countryCode		= new Property(3, String.class, Country.KEY_API, false, CountryDao.KEY_COUNTRYCODE);
		public final static Property industryCode		= new Property(4, String.class, Company.KEY_INDUSTRYCODE, false, KEY_INDUSTRYCODE);
		public final static Property industry			= new Property(5, String.class, Company.KEY_INDUSTRY, false, Company.KEY_INDUSTRY);
		public final static Property type				= new Property(6, Integer.class, Common.KEY_TYPE, false, Common.KEY_TYPE);
		public final static Property image				= new Property(7, Integer.class, Company.KEY_IMAGE, false, Company.KEY_IMAGE);
		public final static Property colorHex			= new Property(8, String.class, Company.KEY_COLOR, false, KEY_COLOR);
		public final static Property fromNumbers		= new Property(9, String.class, Company.KEY_NUMBERS, false, KEY_NUMBERS);
		public final static Property keywords			= new Property(10, String.class, Company.KEY_KEYWORDS, false, Company.KEY_KEYWORDS);
		public final static Property unsuscribe			= new Property(11, String.class, Company.KEY_UNSUSCRIBE, false, Company.KEY_UNSUSCRIBE);
		public final static Property size				= new Property(12, Integer.class, Company.KEY_SIZE, false, Company.KEY_SIZE);
		public final static Property status				= new Property(13, Integer.class, Common.KEY_STATUS, false, Common.KEY_STATUS);
		public final static Property silenced			= new Property(14, Integer.class, Company.KEY_SILENCED, false, Company.KEY_SILENCED);
		public final static Property blocked			= new Property(15, Integer.class, Company.KEY_BLOCKED, false, Company.KEY_BLOCKED);
		public final static Property email				= new Property(16, String.class, User.KEY_EMAIL, false, User.KEY_EMAIL);
		public final static Property firstName			= new Property(17, String.class, User.KEY_FIRSTNAME, false, UserDao.KEY_FIRSTNAME);
		public final static Property lastName			= new Property(18, String.class, User.KEY_LASTNAME, false, UserDao.KEY_LASTNAME);
		public final static Property password			= new Property(19, String.class, User.KEY_PASSWORD, false, User.KEY_PASSWORD);
		public final static Property receive			= new Property(20, Integer.class, Company.KEY_RECEIVE, false, Company.KEY_RECEIVE);
		public final static Property suscribe			= new Property(21, Integer.class, Company.KEY_SUSCRIBE, false, Company.KEY_SUSCRIBE);
		public final static Property url				= new Property(22, String.class, Company.KEY_URL, false, Company.KEY_URL);
		public final static Property phone				= new Property(23, String.class, User.KEY_PHONE, false, User.KEY_PHONE);
		public final static Property msgExamples		= new Property(24, String.class, Company.KEY_MSGEXAMPLES, false, KEY_MSGEXAMPLES);
		public final static Property about				= new Property(25, String.class, Company.KEY_ABOUT, false, Company.KEY_ABOUT);
		public final static Property identificationKey	= new Property(26, String.class, Company.KEY_IDENTIFICATIONKEY, false, KEY_IDENTIFICATIONKEY);
		public final static Property dataSent			= new Property(27, Integer.class, Company.KEY_DATASENT, false, KEY_DATASENT);
		public final static Property identificationVal	= new Property(28, String.class, Company.KEY_IDENTIFICATIONVALUE, false, KEY_IDENTIFICATIONVALUE);
		public final static Property follower			= new Property(29, Integer.class, Company.KEY_FOLLOWER, false, Company.KEY_FOLLOWER);
		public final static Property gray				= new Property(30, Integer.class, Company.KEY_GRAY, false, Company.KEY_GRAY);
	}

	public CompanyDao(DaoConfig config)
	{
		super(config);
	}

	public CompanyDao(DaoConfig config, DaoSession daoSession)
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
			db.execSQL("CREATE TABLE " + constraint + "'" + TABLENAME + "' ('" + Common.KEY_ID + "' INTEGER PRIMARY KEY, '" + KEY_COMPANYID + "' TEXT, '" + Common.KEY_NAME + "' TEXT, '" + CountryDao.KEY_COUNTRYCODE + "' TEXT, '" + KEY_INDUSTRYCODE + "' TEXT, '" + Company.KEY_INDUSTRY + "' TEXT, '" + Common.KEY_TYPE + "' INTEGER, '" + Company.KEY_IMAGE + "' TEXT, '" + KEY_COLOR + "' TEXT, '" + KEY_NUMBERS + "' TEXT, '" + Company.KEY_KEYWORDS + "' TEXT, '" + Company.KEY_UNSUSCRIBE + "' TEXT, '" + Company.KEY_SIZE + "' INTEGER, '" + Common.KEY_STATUS + "' INTEGER, '" + Company.KEY_SILENCED + "' INTEGER, '" + Company.KEY_BLOCKED + "' INTEGER, '" + User.KEY_EMAIL + "' TEXT, '" + UserDao.KEY_FIRSTNAME + "' TEXT, '" + UserDao.KEY_LASTNAME + "' TEXT, '" + User.KEY_PASSWORD + "' TEXT, '" + Company.KEY_RECEIVE + "' INTEGER, '" + Company.KEY_SUSCRIBE + "' INTEGER, '" + Company.KEY_URL + "' TEXT, '" + User.KEY_PHONE + "' TEXT, '" + KEY_MSGEXAMPLES + "' TEXT, '"+Company.KEY_ABOUT+"' TEXT, '"+KEY_IDENTIFICATIONKEY+"' TEXT, '"+KEY_DATASENT+"' TEXT, '"+KEY_IDENTIFICATIONVALUE+"' TEXT, '"+Company.KEY_FOLLOWER+"' INTEGER, '"+Company.KEY_GRAY+"' INTEGER);");
			db.execSQL("CREATE INDEX idx_ccompany ON " + TABLENAME + "(" + CompanyDao.KEY_COMPANYID + ");");
		}
		catch(Exception e)
		{
			System.out.println("CompanyDao:createTable - Exception: " + e);
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
			db.execSQL("DROP INDEX " + (ifExists ? "IF EXISTS " : "") + "idx_ccompany;");
		}
		catch(Exception e)
		{
			System.out.println("CompanyDao:dropTable - Exception: " + e);
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
	protected void bindValues(SQLiteStatement stmt, Company entity)
	{
		//Agregado para capturar excepciones
		try
		{
			stmt.clearBindings();

			if(entity.getId() != null)
			{
				stmt.bindLong(1, entity.getId());
			}

			if(entity.getCompanyId() != null)
			{
				stmt.bindString(2, entity.getCompanyId());
			}

			if(entity.getName() != null)
			{
				stmt.bindString(3, entity.getName());
			}

			if(entity.getCountryCode() != null)
			{
				stmt.bindString(4, entity.getCountryCode());
			}

			if(entity.getIndustryCode() != null)
			{
				stmt.bindString(5, entity.getIndustryCode());
			}

			if(entity.getIndustry() != null)
			{
				stmt.bindString(6, entity.getIndustry());
			}

			stmt.bindLong(7, entity.getType());

			if(entity.getImage() != null)
			{
				stmt.bindString(8, entity.getImage());
			}

			if(entity.getColorHex() != null)
			{
				stmt.bindString(9, entity.getColorHex());
			}

			if(entity.getFromNumbers() != null)
			{
				stmt.bindString(10, entity.getFromNumbers());
			}

			if(entity.getKeywords() != null)
			{
				stmt.bindString(11, entity.getKeywords());
			}

			if(entity.getUnsuscribe() != null)
			{
				stmt.bindString(12, entity.getUnsuscribe());
			}

			if(entity.getSize() != null)
			{
				stmt.bindLong(13, entity.getSize());
			}

			stmt.bindLong(14, entity.getStatus());
			stmt.bindLong(15, entity.getSilenced());
			stmt.bindLong(16, entity.getBlocked());

			if(entity.getEmail() != null)
			{
				stmt.bindString(17, entity.getEmail());
			}

			if(entity.getFirstName() != null)
			{
				stmt.bindString(18, entity.getFirstName());
			}

			if(entity.getLastName() != null)
			{
				stmt.bindString(19, entity.getLastName());
			}

			if(entity.getPassword() != null)
			{
				stmt.bindString(20, entity.getPassword());
			}

			stmt.bindLong(21, entity.getReceive());
			stmt.bindLong(22, entity.getSuscribe());

			if(entity.getUrl() != null)
			{
				stmt.bindString(23, entity.getUrl());
			}

			if(entity.getPhone() != null)
			{
				stmt.bindString(24, entity.getPhone());
			}

			if(entity.getMsgExamples() != null)
			{
				stmt.bindString(25, entity.getMsgExamples());
			}

			if(entity.getAbout() != null)
			{
				stmt.bindString(26, entity.getAbout());
			}

			if(entity.getIdentificationKey() != null)
			{
				stmt.bindString(27, entity.getIdentificationKey());
			}

			stmt.bindLong(28, entity.getDataSent());

			if(entity.getIdentificationValue() != null)
			{
				stmt.bindString(29, entity.getIdentificationValue());
			}

			stmt.bindLong(30, entity.getFollower());
			stmt.bindLong(31, entity.getGray());
		}
		catch(Exception e)
		{
			System.out.println("CompanyDao:bindValues - Exception: " + e);
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
	public Company readEntity(Cursor cursor, int offset)
	{
		return new Company(cursor.isNull(offset) ? null : cursor.getLong(offset),
				cursor.isNull(offset + 1) ? null : cursor.getString(offset + 1),
				cursor.isNull(offset + 2) ? null : cursor.getString(offset + 2),
				cursor.isNull(offset + 3) ? null : cursor.getString(offset + 3),
				cursor.isNull(offset + 4) ? null : cursor.getString(offset + 4),
				cursor.isNull(offset + 5) ? null : cursor.getString(offset + 5),
				cursor.isNull(offset + 6) ? null : cursor.getInt(offset + 6),
				cursor.isNull(offset + 7) ? null : cursor.getString(offset + 7),
				cursor.isNull(offset + 8) ? null : cursor.getString(offset + 8),
				cursor.isNull(offset + 9) ? null : cursor.getString(offset + 9),
				cursor.isNull(offset + 10) ? null : cursor.getString(offset + 10),
				cursor.isNull(offset + 11) ? null : cursor.getString(offset + 11),
				cursor.isNull(offset + 12) ? null : cursor.getInt(offset + 12),
				cursor.isNull(offset + 13) ? null : cursor.getInt(offset + 13),
				cursor.isNull(offset + 14) ? null : cursor.getInt(offset + 14),
				cursor.isNull(offset + 15) ? null : cursor.getInt(offset + 15),
				cursor.isNull(offset + 16) ? null : cursor.getString(offset + 16),
				cursor.isNull(offset + 17) ? null : cursor.getString(offset + 17),
				cursor.isNull(offset + 18) ? null : cursor.getString(offset + 18),
				cursor.isNull(offset + 19) ? null : cursor.getString(offset + 19),
				cursor.isNull(offset + 20) ? null : cursor.getInt(offset + 20),
				cursor.isNull(offset + 21) ? null : cursor.getInt(offset + 21),
				cursor.isNull(offset + 22) ? null : cursor.getString(offset + 22),
				cursor.isNull(offset + 23) ? null : cursor.getString(offset + 23),
				cursor.isNull(offset + 24) ? null : cursor.getString(offset + 24),
				cursor.isNull(offset + 25) ? null : cursor.getString(offset + 25),
				cursor.isNull(offset + 26) ? null : cursor.getString(offset + 26),
				cursor.isNull(offset + 27) ? null : cursor.getInt(offset + 27),
				cursor.isNull(offset + 28) ? null : cursor.getString(offset + 28),
				cursor.isNull(offset + 29) ? null : cursor.getInt(offset + 29),
				cursor.isNull(offset + 30) ? null : cursor.getInt(offset + 30));
	}

	/**
	 * @inheritdoc
	 */
	@Override
	public void readEntity(Cursor cursor, Company entity, int offset)
	{
		//Agregado para capturar excepciones
		try
		{
			entity.setId(cursor.isNull(offset) ? null : cursor.getLong(offset));
			entity.setCompanyId(cursor.isNull(offset + 1) ? null : cursor.getString(offset + 1));
			entity.setName(cursor.isNull(offset + 2) ? null : cursor.getString(offset + 2));
			entity.setCountryCode(cursor.isNull(offset + 3) ? null : cursor.getString(offset + 3));
			entity.setIndustryCode(cursor.isNull(offset + 4) ? null : cursor.getString(offset + 4));
			entity.setIndustry(cursor.isNull(offset + 5) ? null : cursor.getString(offset + 5));
			entity.setType(cursor.isNull(offset + 6) ? null : cursor.getInt(offset + 6));
			entity.setImage(cursor.isNull(offset + 7) ? null : cursor.getString(offset + 7));
			entity.setColorHex(cursor.isNull(offset + 8) ? null : cursor.getString(offset + 8));
			entity.setFromNumbers(cursor.isNull(offset + 9) ? null : cursor.getString(offset + 9));
			entity.setKeywords(cursor.isNull(offset + 10) ? null : cursor.getString(offset + 10));
			entity.setUnsuscribe(cursor.isNull(offset + 11) ? null : cursor.getString(offset + 11));
			entity.setSize(cursor.isNull(offset + 12) ? null : cursor.getInt(offset + 12));
			entity.setStatus(cursor.isNull(offset + 13) ? null : cursor.getInt(offset + 13));
			entity.setSilenced(cursor.isNull(offset + 14) ? null : cursor.getInt(offset + 14));
			entity.setBlocked(cursor.isNull(offset + 15) ? null : cursor.getInt(offset + 15));
			entity.setEmail(cursor.isNull(offset + 16) ? null : cursor.getString(offset + 16));
			entity.setFirstName(cursor.isNull(offset + 17) ? null : cursor.getString(offset + 17));
			entity.setLastName(cursor.isNull(offset + 18) ? null : cursor.getString(offset + 18));
			entity.setPassword(cursor.isNull(offset + 19) ? null : cursor.getString(offset + 19));
			entity.setSize(cursor.isNull(offset + 20) ? null : cursor.getInt(offset + 20));
			entity.setReceive(cursor.isNull(offset + 21) ? null : cursor.getInt(offset + 21));
			entity.setSuscribe(cursor.isNull(offset + 22) ? null : cursor.getInt(offset + 22));
			entity.setUrl(cursor.isNull(offset + 23) ? null : cursor.getString(offset + 23));
			entity.setPhone(cursor.isNull(offset + 24) ? null : cursor.getString(offset + 24));
			entity.setMsgExamples(cursor.isNull(offset + 25) ? null : cursor.getString(offset + 25));
			entity.setAbout(cursor.isNull(offset + 26) ? null : cursor.getString(offset + 26));
			entity.setIdentificationKey(cursor.isNull(offset + 27) ? null : cursor.getString(offset + 27));
			entity.setDataSent(cursor.isNull(offset + 28) ? null : cursor.getInt(offset + 28));
			entity.setIdentificationValue(cursor.isNull(offset + 28) ? null : cursor.getString(offset + 28));
			entity.setFollower(cursor.isNull(offset + 29) ? null : cursor.getInt(offset + 29));
			entity.setFollower(cursor.isNull(offset + 30) ? null : cursor.getInt(offset + 30));
		}
		catch(Exception e)
		{
			System.out.println("CompanyDao:readEntity - Exception: " + e);
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
	protected Long updateKeyAfterInsert(Company entity, long rowId)
	{
		entity.setId(rowId);
		return rowId;
	}

	/**
	 * @inheritdoc
	 */
	@Override
	public Long getKey(Company entity)
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

	/**
	 * Obtener companies por número corto
	 * @param number Número corto a buscar
	 * @param companyDao Dao para consultar
	 * @return List≤Company> Lista de companies
	 */
	public static List<Company> getCompanyByNumber(String number, CompanyDao companyDao, boolean uncached)
	{
		List<Company> companies	= new ArrayList<>();
		try
		{
			if(uncached)
			{
				companies = companyDao.queryBuilder().where(CompanyDao.Properties.fromNumbers.like("%\"" + number + "\"%")).listLazyUncached();
			}
			else
			{
				companies = companyDao.queryBuilder().where(CompanyDao.Properties.fromNumbers.like("%\"" + number + "\"%")).list();
			}
		}
		catch(Exception e)
		{
			System.out.println("CompanyDao:getCompanyByNumber - Exception: " + e);
			if(Common.DEBUG)
			{
				e.printStackTrace();
			}
		}

		return companies;
	}

	/**
	 * Devuelve la lista de companies a mostrar en el home
	 * @param context
	 * @return
	 */
	public static List<Company> getList(Context context, boolean forHome)
	{
		DaoSession session				= null;
		ArrayList<Company> companies	= new ArrayList<>();

		try
		{
			session = DaoMaster.openRdb(context);

			//Agregado para evitar bloqueos en la db
			if(session != null)
			{
				List<String> usedNames	= new ArrayList<>();
				//Modificación para corregir orden por mensaje más nuevo
				String filterHome		= "";

				if(forHome)
				{
					//Se quito filtro por no bloqueadas para que el usuario pueda continuar los mensajes de una company por más que haya removido su suscripción
					filterHome = " WHERE " + MessageDao.TABLENAME + "." + Message.KEY_DELETED + " != " + Common.BOOL_YES;
				}

				String sql = "SELECT " + SELECT_FIELDS + FROM_CLAUSE + filterHome + " ORDER BY " + MessageDao.TABLENAME + "." + Message.KEY_CREATED + " DESC;";

				if(session.getDatabase().isOpen())
				{
					if(!Common.DEBUGDB && Common.DEBUG)
					{
						System.out.println("SQL: " + sql);
					}

					Cursor c = session.getDatabase().rawQuery(sql, null);

					if(c != null)
					{
						if(c.getCount() > 0)
						{
							for(c.moveToFirst(); !(c.isAfterLast()); c.moveToNext())
							{
								//Agregado para evitar duplicados en la lista
								if(!usedNames.contains(c.getString(2)))
								{
									Company company = new Company(	c.getLong(0), c.getString(1), c.getString(2), c.getString(3), c.getString(4), c.getString(5), c.getInt(6), c.getString(7),
																	c.getString(8), c.getString(9), c.getString(10), c.getString(11), c.getInt(12), c.getInt(13), c.getInt(14), c.getInt(15),
																	c.getString(16), c.getString(17), c.getString(18), c.getString(19), c.getInt(20), c.getInt(21), c.getString(22), c.getString(23),
																	c.getString(24), c.getString(25), c.getString(26), c.getInt(27), c.getString(28), c.getInt(29), c.getInt(30));
									companies.add(company);
									usedNames.add(company.getName());
								}
							}

							c.close();
						}
					}
				}
			}
		}
		catch(Exception e)
		{
			System.out.println("CompanyDao:getListHome - Exception: " + e);
			if(Common.DEBUG)
			{
				e.printStackTrace();
			}
		}
		finally
		{
			//Modificación para evitar bloqueos en la db
			DaoMaster.closeDB(session);
		}

		DaoMaster.closeDB(session);
		return companies;
	}

	/**
	 * Agregado para actualizar companies mediante pull update. Debe ser llamado desde una Asynctask únicamente
	 * @param activity
	 * @return
	 */
	public static List<Company> updateCompanies(Activity activity)
	{
		Realm realm				= null;
		DaoSession session		= null;
		List<Company> companies	= null;

		try
		{
			//Modificación para evitar bloqueos en la db
			session							= DaoMaster.openDB(activity.getApplicationContext());
			Migration.getDB(activity);
			realm							= Realm.getDefaultInstance();
			CompanyDao companyDao			= null;
			UserDao userDao					= null;
			MessageDao messageDao			= null;
			SharedPreferences preferences	= activity.getApplicationContext().getSharedPreferences(Common.KEY_PREF, Context.MODE_PRIVATE);
			String country					= preferences.getString(Country.KEY_API, "");
			JSONObject jsonResult			= null;
			String result					= "";

			//Agregado para evitar bloqueos en la db
			if(session != null)
			{
				companyDao	= session.getCompanyDao();
				userDao		= session.getUserDao();
			}

			if(StringUtils.isEmpty(country))
			{
				List<User> userList	= null;

				if(userDao != null)
				{
					userList = userDao.queryBuilder().limit(1).listLazyUncached();
				}

				if(userList != null)
				{
					if(userList.size() > 0)
					{
						if(StringUtils.isNotEmpty(userList.get(0).getCountryCode()))
						{
							country = userList.get(0).getCountryCode();
						}
					}
				}
			}

			jsonResult	= new JSONObject(ApiConnection.request(ApiConnection.COMPANIES_BY_COUNTRY + "=" + country, activity, ApiConnection.METHOD_GET, preferences.getString(Common.KEY_TOKEN, ""), ""));
			result		= ApiConnection.checkResponse(activity.getApplicationContext(), jsonResult);

			if(result.equals(ApiConnection.OK))
			{
				Company.parseList(jsonResult.getJSONArray(Common.KEY_DATA), activity.getApplicationContext(), companyDao, true, realm, messageDao);
			}
			else
			{
				Company.parseList(null, activity.getApplicationContext(), companyDao, true, realm, messageDao);
			}

			DaoMaster.closeDB(session);
			companies = getList(activity, true);
		}
		catch(Exception e)
		{
			System.out.println("CompanyDao:updateCompanies - Exception: " + e);
			if(Common.DEBUG)
			{
				e.printStackTrace();
			}
		}
		finally
		{
			DaoMaster.closeDB(session);
		}

		DaoMaster.closeDB(session);
		return companies;
	}
}