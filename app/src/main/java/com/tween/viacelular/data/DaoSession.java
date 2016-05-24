package com.tween.viacelular.data;

import android.database.sqlite.SQLiteDatabase;
import com.tween.viacelular.utils.Common;
import java.util.Map;
import de.greenrobot.dao.AbstractDao;
import de.greenrobot.dao.AbstractDaoSession;
import de.greenrobot.dao.identityscope.IdentityScopeType;
import de.greenrobot.dao.internal.DaoConfig;

/**
 * Created by davidfigueroa on 25/8/15.
 */
/**
 * {@inheritDoc}
 *
 * @see de.greenrobot.dao.AbstractDaoSession
 */
public class DaoSession extends AbstractDaoSession
{
	private DaoConfig				userConfig;
	private DaoConfig				registeredAccountConfig;
	private DaoConfig				messageConfig;
	private DaoConfig				countryConfig;
	private DaoConfig				companyConfig;
	private DaoConfig				ispConfig;
	private UserDao					userDao;
	private RegisteredAccountDao	registeredAccountDao;
	private MessageDao				messageDao;
	private CountryDao				countryDao;
	private CompanyDao				companyDao;
	private IspDao					ispDao;

	public DaoSession(SQLiteDatabase db, IdentityScopeType type, Map<Class<? extends AbstractDao<?, ?>>, DaoConfig> daoConfigMap)
	{
		super(db);
		//Agregado para prevenir excepciones
		try
		{
			userConfig				= daoConfigMap.get(UserDao.class).clone();
			userConfig.initIdentityScope(type);
			registeredAccountConfig	= daoConfigMap.get(RegisteredAccountDao.class).clone();
			registeredAccountConfig.initIdentityScope(type);
			messageConfig			= daoConfigMap.get(MessageDao.class).clone();
			messageConfig.initIdentityScope(type);
			countryConfig			= daoConfigMap.get(CountryDao.class).clone();
			countryConfig.initIdentityScope(type);
			companyConfig			= daoConfigMap.get(CompanyDao.class).clone();
			companyConfig.initIdentityScope(type);
			ispConfig				= daoConfigMap.get(IspDao.class).clone();
			ispConfig.initIdentityScope(type);

			userDao					= new UserDao(userConfig, this);
			registeredAccountDao	= new RegisteredAccountDao(registeredAccountConfig, this);
			messageDao				= new MessageDao(messageConfig, this);
			countryDao				= new CountryDao(countryConfig, this);
			companyDao				= new CompanyDao(companyConfig, this);
			ispDao					= new IspDao(ispConfig, this);

			registerDao(User.class, userDao);
			registerDao(RegisteredAccount.class, registeredAccountDao);
			registerDao(Message.class, messageDao);
			registerDao(Country.class, countryDao);
			registerDao(Company.class, companyDao);
			registerDao(Isp.class, ispDao);
		}
		catch(Exception e)
		{
			System.out.println("DaoSession:construct - Exception: " + e);
			if(Common.DEBUG)
			{
				e.printStackTrace();
			}
		}
	}

	public void clear()
	{
		//Agregado para prevenir excepciones
		try
		{
			userConfig.getIdentityScope().clear();
			registeredAccountConfig.getIdentityScope().clear();
			messageConfig.getIdentityScope().clear();
			countryConfig.getIdentityScope().clear();
			companyConfig.getIdentityScope().clear();
			ispConfig.getIdentityScope().clear();
		}
		catch(Exception e)
		{
			System.out.println("DaoSession:clear - Exception: " + e);
			if(Common.DEBUG)
			{
				e.printStackTrace();
			}
		}
	}

	public UserDao getUserDao()
	{
		return userDao;
	}

	public RegisteredAccountDao getRegisteredAccountDao()
	{
		return registeredAccountDao;
	}

	public MessageDao getMessageDao()
	{
		return messageDao;
	}

	public CountryDao getCountryDao()
	{
		return countryDao;
	}

	public CompanyDao getCompanyDao()
	{
		return companyDao;
	}

	public IspDao getIspDao()
	{
		return ispDao;
	}
}