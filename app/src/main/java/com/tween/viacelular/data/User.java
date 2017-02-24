package com.tween.viacelular.data;

import android.app.Activity;
import android.os.AsyncTask;
import com.tween.viacelular.asynctask.CompanyAsyncTask;
import com.tween.viacelular.models.Land;
import com.tween.viacelular.models.Suscription;
import com.tween.viacelular.utils.Common;
import com.tween.viacelular.utils.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import io.realm.Realm;

public class User
{
	private Long	id;
	private String	firstName;
	private String	lastName;
	private String	facebookId;
	private String	googleId;
	private String	email;
	private String	password;
	private String	phone;
	private String	gcmId;
	private String	userId;
	private Integer	status;
	private String	countryCode;

	public static final int STATUS_UNVERIFIED		= 0;
	public static final int STATUS_ACTIVE			= 1;
	public static final int STATUS_INACTIVE			= 2;
	public static final String KEY_API				= "userId";
	public static final String KEY_GCMID			= "gcmId";
	public static final String KEY_PHONE			= "phone";
	public static final String KEY_EMAIL			= "email";
	public static final String KEY_FIRSTNAME		= "firstName";
	public static final String KEY_LASTNAME			= "lastName";
	public static final String KEY_USERSTATUS		= "userStatus";
	public static final String KEY_FACEBOOK			= "facebookId";
	public static final String KEY_GOOGLE			= "googleId";
	public static final String KEY_PASSWORD			= "password";
	public static final String KEY_CARRIER			= "carrier";
	public static final String USERID				= "55f30078c9a75d9abcc30207";
	public static final String FAKE_GCMID_EMULATOR	= "fT7ge95a-6k:APA91bFJWoP5dXfXFW-UEih-OppYAtMBnBnw1lVuiyCk8q-yFXtS1nI2UXqTtdCNU5gynjXGuFBUgX-hEdjbw_7xlcaH4gZWioWFCa_DjnDdHgUbtz267EEQ6tNKhcYU1Gw-HbCNpSIz";

	public User()
	{

	}

	public User(String firstName, String lastName, String facebookId, String googleId, String email, String password, String phone, String gcmId, String userId, int status, String countryCode)
	{
		this.firstName		= firstName;
		this.lastName		= lastName;
		this.facebookId		= facebookId;
		this.googleId		= googleId;
		this.email			= email;
		this.password		= password;
		this.phone			= phone;
		this.gcmId			= gcmId;
		this.userId			= userId;
		this.status			= status;
		this.countryCode	= countryCode;
	}

	public User(Long id, String firstName, String lastName, String phone, String email, String userId, String gcmId, Integer status, String countryCode, String facebookId, String googleId, String password)
	{
		this.id				= id;
		this.firstName		= firstName;
		this.lastName		= lastName;
		this.phone			= phone;
		this.email			= email;
		this.userId			= userId;
		this.gcmId			= gcmId;
		this.status			= status;
		this.countryCode	= countryCode;
		this.facebookId		= facebookId;
		this.googleId		= googleId;
		this.password		= password;
	}

	public Long getId()
	{
		return id;
	}

	public void setId(Long id)
	{
		this.id = id;
	}

	public String getFirstName()
	{
		if(firstName != null)
		{
			return firstName;
		}else
		{
			return "";
		}
	}

	public void setFirstName(String firstName)
	{
		this.firstName = firstName;
	}

	public String getLastName()
	{
		if(lastName != null)
		{
			return lastName;
		}
		else
		{
			return "";
		}
	}

	public void setLastName(String lastName)
	{
		this.lastName = lastName;
	}

	public String getFacebookId()
	{
		if(facebookId != null)
		{
			return facebookId;
		}
		else
		{
			return "";
		}
	}

	public void setFacebookId(String facebookId)
	{
		this.facebookId = facebookId;
	}

	public String getGoogleId()
	{
		if(googleId != null)
		{
			return googleId;
		}
		else
		{
			return "";
		}
	}

	public void setGoogleId(String googleId)
	{
		this.googleId = googleId;
	}

	public String getEmail()
	{
		if(email != null)
		{
			return email;
		}
		else
		{
			return "";
		}
	}

	public void setEmail(String email)
	{
		this.email = email;
	}

	public String getPassword()
	{
		if(password != null)
		{
			return password;
		}
		else
		{
			return "";
		}
	}

	public void setPassword(String password)
	{
		this.password = password;
	}

	public String getPhone()
	{
		if(phone != null)
		{
			return phone;
		}
		else
		{
			return "";
		}
	}

	public void setPhone(String phone)
	{
		this.phone = phone;
	}

	public String getGcmId()
	{
		if(gcmId != null)
		{
			return gcmId;
		}
		else
		{
			return "";
		}
	}

	public void setGcmId(String gcmId)
	{
		this.gcmId = gcmId;
	}

	public String getUserId()
	{
		if(userId != null)
		{
			return userId;
		}
		else
		{
			return "";
		}
	}

	public void setUserId(final String userId)
	{
		this.userId = userId;
	}

	public int getStatus()
	{
		if(status != null)
		{
			return status;
		}
		else
		{
			return STATUS_UNVERIFIED;
		}
	}

	public void setStatus(final int status)
	{
		this.status = status;
	}

	public String getCountryCode()
	{
		if(countryCode != null)
		{
			return countryCode;
		}
		else
		{
			return "";
		}
	}

	public void setCountryCode(String countryCode)
	{
		this.countryCode = countryCode;
	}

	/**
	 * Convierte el User JSON recibido en User //TODO migrar a Realm
	 * @param json
	 * @param checkSubscriptions
	 * @param activity
	 * @return
	 */
	public static User parseJSON(JSONObject json, boolean checkSubscriptions, Activity activity)
	{
		User user	= null;
		Realm realm	= null;

		try
		{
			String jId			= "";
			String jGcmId		= "";
			String jPhone		= "";
			int jStatus			= User.STATUS_UNVERIFIED;
			String jFirstName	= "";
			String jLastName	= "";
			String jCountryCode	= "";
			String jEmail		= "";

			//Modificación por cambio en la estructura de los recursos en la Api
			if(json.has(Common.KEY_ID))
			{
				if(!json.isNull(Common.KEY_ID))
				{
					if(StringUtils.isNotEmpty(json.getString(Common.KEY_ID)))
					{
						jId = json.getString(Common.KEY_ID);
					}
				}
			}
			else
			{
				//Agregado para contemplar id de mongo
				if(json.has(Common.KEY_IDMONGO))
				{
					if(!json.isNull(Common.KEY_IDMONGO))
					{
						if(StringUtils.isNotEmpty(json.getString(Common.KEY_IDMONGO)))
						{
							jId = json.getString(Common.KEY_IDMONGO);
						}
					}
				}
			}

			if(json.has(KEY_GCMID))
			{
				if(!json.isNull(KEY_GCMID))
				{
					if(StringUtils.isNotEmpty(json.getString(KEY_GCMID)))
					{
						jGcmId = json.getString(KEY_GCMID);
					}
				}
			}

			if(json.has(KEY_PHONE))
			{
				if(!json.isNull(KEY_PHONE))
				{
					if(StringUtils.isNotEmpty(json.getString(KEY_PHONE)))
					{
						jPhone = json.getString(KEY_PHONE);
					}
				}
			}

			if(json.has(Common.KEY_STATUS))
			{
				if(!json.isNull(Common.KEY_STATUS))
				{
					if(StringUtils.isNotEmpty(json.getString(Common.KEY_STATUS)))
					{
						if(StringUtils.isNumber(json.getString(Common.KEY_STATUS)))
						{
							jStatus = json.getInt(Common.KEY_STATUS);
						}
					}
				}
			}

			if(json.has(KEY_FIRSTNAME))
			{
				if(StringUtils.isNotEmpty(json.getString(KEY_FIRSTNAME)))
				{
					jFirstName = json.getString(KEY_FIRSTNAME);
				}
			}

			if(json.has(KEY_LASTNAME))
			{
				if(StringUtils.isNotEmpty(json.getString(KEY_LASTNAME)))
				{
					jLastName = json.getString(KEY_LASTNAME);
				}
			}

			if(json.has(KEY_EMAIL))
			{
				if(StringUtils.isNotEmpty(json.getString(KEY_EMAIL)))
				{
					jEmail = json.getString(KEY_EMAIL);
				}
			}

			if(json.has(Land.KEY_API))
			{
				if(StringUtils.isNotEmpty(json.getString(Land.KEY_API)))
				{
					jCountryCode = json.getString(Land.KEY_API);
				}
			}

			if(json.has(Common.KEY_INFO))
			{
				//La estructura anterior alojaba más datos dentro de la key "info"
				json = json.getJSONObject(Common.KEY_INFO);
				if(json.has(Common.KEY_STATUS))
				{
					if(!json.isNull(Common.KEY_STATUS))
					{
						if(StringUtils.isNumber(json.getString(Common.KEY_STATUS)))
						{
							jStatus = json.getInt(Common.KEY_STATUS);
						}
					}
				}

				if(json.has(KEY_FIRSTNAME))
				{
					if(StringUtils.isNotEmpty(json.getString(KEY_FIRSTNAME)))
					{
						jFirstName = json.getString(KEY_FIRSTNAME);
					}
				}

				if(json.has(KEY_LASTNAME))
				{
					if(StringUtils.isNotEmpty(json.getString(KEY_LASTNAME)))
					{
						jLastName = json.getString(KEY_LASTNAME);
					}
				}

				if(json.has(KEY_EMAIL))
				{
					if(StringUtils.isNotEmpty(json.getString(KEY_EMAIL)))
					{
						jEmail = json.getString(KEY_EMAIL);
					}
				}

				if(json.has(Land.KEY_API))
				{
					if(StringUtils.isNotEmpty(json.getString(Land.KEY_API)))
					{
						jCountryCode = json.getString(Land.KEY_API);
					}
				}
			}

			//Agregado para revisar las suscripciones del usuario
			if(checkSubscriptions && activity != null)
			{
				if(json.has("subs"))
				{
					if(!json.isNull("subs"))
					{
						JSONArray jsonArray = json.getJSONArray("subs");

						if(Common.DEBUG)
						{
							System.out.println("El usuario tiene suscripciones: "+jsonArray.toString());
						}

						if(jsonArray != null)
						{
							if(jsonArray.length() > 0)
							{
								for(int i = 0; i < jsonArray.length(); i++)
								{
									String companyId = jsonArray.getString(i);

									if(StringUtils.isIdMongo(companyId))
									{
										//Modificación para validar paso de contexto Realm
										Suscription suscription	= realm.where(Suscription.class).equalTo(Suscription.KEY_API, companyId).findFirst();

										if(suscription == null)
										{
											final CompanyAsyncTask task	= new CompanyAsyncTask(activity, false, companyId, jCountryCode);
											task.setFlag(Common.BOOL_YES);
											task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
										}
									}
								}
							}
						}
					}
				}
			}

			user = new User();
			user.setUserId(jId);
			user.setGcmId(jGcmId);
			user.setPhone(jPhone);
			user.setStatus(jStatus);
			user.setFirstName(jFirstName);
			user.setLastName(jLastName);
			user.setCountryCode(jCountryCode);
			user.setEmail(jEmail);
		}
		catch(Exception e)
		{
			System.out.println("User:parseJSON - Exception: " + e);
			if(Common.DEBUG)
			{
				e.printStackTrace();
			}
		}

		return user;
	}

	public void debug()
	{
		System.out.println("User - id: " + id);
		System.out.println("User - firstName: " + firstName);
		System.out.println("User - lastName: " + lastName);
		System.out.println("User - facebookId: " + facebookId);
		System.out.println("User - googleId: " + googleId);
		System.out.println("User - email: " + email);
		System.out.println("User - password: " + password);
		System.out.println("User - phone: " + phone);
		System.out.println("User - gcmId: " + gcmId);
		System.out.println("User - userId: " + userId);
		System.out.println("User - status: " + status);
		System.out.println("User - countryCode: " + countryCode);
	}
}
