package com.tween.viacelular.data;

import android.app.Activity;
import com.tween.viacelular.R;
import com.tween.viacelular.utils.Common;
import org.json.JSONArray;
import org.json.JSONObject;

import io.realm.RealmObject;

/**
 * Created by david.figueroa on 30/6/15.
 */
public class Country extends RealmObject
{
	private Long	id;
	private String	name;
	private String	code;
	private String	isoCode;
	private String	format;
	private String	minLength;
	private String	maxLength;

	public static final String KEY_API			= "countryCode";
	public static final String KEY_CODE			= "code";
	public static final String KEY_ISOCODE		= "isoCode";
	public static final String KEY_FORMAT		= "format";
	public static final String KEY_MINLENGTH	= "minLength";
	public static final String KEY_MAXLENGHT	= "maxLength";
	public static final String DEFAULT_VALUE	= "AR";

	public Country()
	{

	}

	public Country(String name, String code, String isoCode, String format, String minLength, String maxLength)
	{
		this.name		= name;
		this.code		= code;
		this.isoCode	= isoCode;
		this.format		= format;
		this.minLength	= minLength;
		this.maxLength	= maxLength;
	}

	public Country(Long id, String name, String code, String isoCode, String format, String minLength, String maxLength)
	{
		this.id			= id;
		this.name		= name;
		this.code		= code;
		this.isoCode	= isoCode;
		this.format		= format;
		this.minLength	= minLength;
		this.maxLength	= maxLength;
	}

	public Long getId()
	{
		return id;
	}

	public void setId(Long id)
	{
		this.id = id;
	}

	public String getName()
	{
		if(name != null)
		{
			return name;
		}
		else
		{
			return "";
		}
	}

	public void setName(final String name)
	{
		this.name = name;
	}

	public String getCode()
	{
		if(code != null)
		{
			return code;
		}
		else
		{
			return "";
		}
	}

	public void setCode(final String code)
	{
		this.code = code;
	}

	public String getIsoCode()
	{
		if(isoCode != null)
		{
			return isoCode;
		}
		else
		{
			return "";
		}
	}

	public void setIsoCode(String isoCode)
	{
		this.isoCode = isoCode;
	}

	public String getFormat()
	{
		if(format != null)
		{
			return format;
		}
		else
		{
			return "";
		}
	}

	public void setFormat(final String format)
	{
		this.format = format;
	}

	public String getMinLength()
	{
		if(minLength != null)
		{
			return minLength;
		}
		else
		{
			return "";
		}
	}

	public void setMinLength(final String minLength)
	{
		this.minLength = minLength;
	}

	public String getMaxLength()
	{
		if(maxLength != null)
		{
			return maxLength;
		}
		else
		{
			return "";
		}
	}

	public void setMaxLength(final String maxLength)
	{
		this.maxLength = maxLength;
	}

	public void debug()
	{
		System.out.println("Country - id: " + id);
		System.out.println("Country - name: " + name);
		System.out.println("Country - code: " + code);
		System.out.println("Country - isoCode: " + isoCode);
		System.out.println("Country - format: " + format);
		System.out.println("Country - minLength: " + minLength);
		System.out.println("Country - maxLength: " + maxLength);
	}

	public static void parseList(JSONArray jsonArray, CountryDao countryDao)
	{
		try
		{
			//Agregado para validar daos con sessión
			if(countryDao != null)
			{
				countryDao.getDatabase().beginTransaction();
			}

			//Modificación para contemplar nuevo estándar y reestructuración de api
			for(int i = 0; i < jsonArray.length(); i++)
			{
				JSONObject jsonObject	= jsonArray.getJSONObject(i);
				String name				= "";
				String code				= "";
				String isoCode			= "";
				String format			= "";
				String minLength		= "";
				String maxLength		= "";

				if(jsonObject.has(Common.KEY_NAME))
				{
					if(!jsonObject.isNull(Common.KEY_NAME))
					{
						name = jsonObject.getString(Common.KEY_NAME);
					}
				}
				else
				{
					if(jsonObject.has(Common.KEY_DISPLAYNAME))
					{
						if(!jsonObject.isNull(Common.KEY_DISPLAYNAME))
						{
							name = jsonObject.getString(Common.KEY_DISPLAYNAME);
						}
					}
				}

				if(jsonObject.has(KEY_CODE))
				{
					if(!jsonObject.isNull(KEY_CODE))
					{
						code = jsonObject.getString(KEY_CODE);
					}
				}

				if(jsonObject.has(KEY_ISOCODE))
				{
					if(!jsonObject.isNull(KEY_ISOCODE))
					{
						isoCode = jsonObject.getString(KEY_ISOCODE);
					}
				}

				if(jsonObject.has(KEY_FORMAT))
				{
					if(!jsonObject.isNull(KEY_FORMAT))
					{
						format = jsonObject.getString(KEY_FORMAT);
					}
				}

				if(jsonObject.has(KEY_MINLENGTH))
				{
					if(!jsonObject.isNull(KEY_MINLENGTH))
					{
						minLength = jsonObject.getString(KEY_MINLENGTH);
					}
				}

				if(jsonObject.has(KEY_MAXLENGHT))
				{
					if(!jsonObject.isNull(KEY_MAXLENGHT))
					{
						maxLength = jsonObject.getString(KEY_MAXLENGHT);
					}
				}

				Country country = new Country(name, code, isoCode, format, minLength, maxLength);

				if(countryDao != null)
				{
					countryDao.insert(country);
				}
			}

			if(countryDao != null)
			{
				countryDao.getDatabase().setTransactionSuccessful();
				countryDao.getDatabase().endTransaction();
			}
		}
		catch(Exception e)
		{
			System.out.println("Country - Exception: " + e);
			if(Common.DEBUG)
			{
				e.printStackTrace();
			}
		}
	}

	public static void parseArray(Activity activity, CountryDao countryDao)
	{
		String[] arrayCountries	= activity.getResources().getStringArray(R.array.countries);
		String[] arrayCodes		= activity.getResources().getStringArray(R.array.codes);
		String[] arrayIsoCodes	= activity.getResources().getStringArray(R.array.isoCodes);
		String[] formats		= activity.getResources().getStringArray(R.array.formats);
		String[] minLength		= activity.getResources().getStringArray(R.array.minLength);
		String[] maxLength		= activity.getResources().getStringArray(R.array.maxLength);

		countryDao.getDatabase().beginTransaction();
		for(int i = 0; i < arrayCountries.length; i++)
		{
			Country country = new Country(arrayCountries[i], arrayCodes[i], arrayIsoCodes[i], formats[i], minLength[i], maxLength[i]);
			countryDao.insert(country);
		}

		countryDao.getDatabase().setTransactionSuccessful();
		countryDao.getDatabase().endTransaction();
	}
}