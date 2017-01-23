package com.tween.viacelular.models;

import io.realm.RealmObject;
import io.realm.annotations.Ignore;
import io.realm.annotations.Index;
import io.realm.annotations.PrimaryKey;

/**
 * Created by davidfigueroa on 31/3/16.
 */
public class User extends RealmObject
{
	@PrimaryKey
	@Index
	private String	userId;
	private String	firstName;
	private String	lastName;
	private String	facebookId;
	private String	googleId;
	private String	email;
	private String	password;
	private String	phone;
	private String	gcmId;
	private int		status;
	private String	countryCode;

	@Ignore
	public static final int STATUS_UNVERIFIED		= 0;
	@Ignore
	public static final int STATUS_ACTIVE			= 1;
	@Ignore
	public static final int STATUS_INACTIVE			= 2;
	@Ignore
	public static final String KEY_API				= "userId";
	@Ignore
	public static final String KEY_GCMID			= "gcmId";
	@Ignore
	public static final String KEY_PHONE			= "phone";
	@Ignore
	public static final String KEY_EMAIL			= "email";
	@Ignore
	public static final String KEY_FIRSTNAME		= "firstName";
	@Ignore
	public static final String KEY_LASTNAME			= "lastName";
	@Ignore
	public static final String KEY_USERSTATUS		= "userStatus";
	@Ignore
	public static final String KEY_FACEBOOK			= "facebookId";
	@Ignore
	public static final String KEY_GOOGLE			= "googleId";
	@Ignore
	public static final String KEY_PASSWORD			= "password";
	@Ignore
	public static final String KEY_CARRIER			= "carrier";
	@Ignore
	public static final String USERID				= "55f30078c9a75d9abcc30207";
	@Ignore
	public static final String FAKE_GCMID_EMULATOR	= "fT7ge95a-6k:APA91bFJWoP5dXfXFW-UEih-OppYAtMBnBnw1lVuiyCk8q-yFXtS1nI2UXqTtdCNU5gynjXGuFBUgX-hEdjbw_7xlcaH4gZWioWFCa_DjnDdHgUbtz267EEQ6tNKhcYU1Gw-HbCNpSIz";
	@Ignore
	public static final String FAKE_USER			= "userFake";

	public User()
	{
	}

	public User(	final String userId, final String firstName, final String lastName, final String facebookId, final String googleId, final String email, final String password, final String phone,
					final String gcmId, final Integer status, final String countryCode)
	{
		this.userId			= userId;
		this.firstName		= firstName;
		this.lastName		= lastName;
		this.facebookId		= facebookId;
		this.googleId		= googleId;
		this.email			= email;
		this.password		= password;
		this.phone			= phone;
		this.gcmId			= gcmId;
		this.status			= status;
		this.countryCode	= countryCode;
	}

	public String getUserId()
	{
		return userId;
	}

	public void setUserId(final String userId)
	{
		this.userId = userId;
	}

	public String getFirstName()
	{
		return firstName;
	}

	public void setFirstName(final String firstName)
	{
		this.firstName = firstName;
	}

	public String getLastName()
	{
		return lastName;
	}

	public void setLastName(final String lastName)
	{
		this.lastName = lastName;
	}

	public String getFacebookId()
	{
		return facebookId;
	}

	public void setFacebookId(final String facebookId)
	{
		this.facebookId = facebookId;
	}

	public String getGoogleId()
	{
		return googleId;
	}

	public void setGoogleId(final String googleId)
	{
		this.googleId = googleId;
	}

	public String getEmail()
	{
		return email;
	}

	public void setEmail(final String email)
	{
		this.email = email;
	}

	public String getPassword()
	{
		return password;
	}

	public void setPassword(final String password)
	{
		this.password = password;
	}

	public String getPhone()
	{
		return phone;
	}

	public void setPhone(final String phone)
	{
		this.phone = phone;
	}

	public String getGcmId()
	{
		return gcmId;
	}

	public void setGcmId(final String gcmId)
	{
		this.gcmId = gcmId;
	}

	public int getStatus()
	{
		return status;
	}

	public void setStatus(final int status)
	{
		this.status = status;
	}

	public String getCountryCode()
	{
		return countryCode;
	}

	public void setCountryCode(final String countryCode)
	{
		this.countryCode = countryCode;
	}
}