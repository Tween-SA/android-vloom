package com.tween.viacelular.models;

import io.realm.RealmObject;
import io.realm.annotations.Ignore;
import io.realm.annotations.Index;
import io.realm.annotations.PrimaryKey;

/**
 * Modelo para persistir países
 * Created by Tween (David Figueroa davo.figueroa@tween.com.ar) on 31/03/2016
 */
public class Land extends RealmObject
{
	@PrimaryKey
	@Index
	private String	code;
	@Index
	private String	name;
	@Index
	private String	isoCode;
	private String	format;
	private String	minLength;
	private String	maxLength;

	@Ignore
	public static final String KEY_API			= "countryCode";
	@Ignore
	public static final String KEY_CODE			= "code";
	@Ignore
	public static final String KEY_ISOCODE		= "isoCode";
	@Ignore
	public static final String KEY_FORMAT		= "format";
	@Ignore
	public static final String KEY_MINLENGTH	= "minLength";
	@Ignore
	public static final String KEY_MAXLENGHT	= "maxLength";
	@Ignore
	public static final String DEFAULT_VALUE	= "AR";

	public Land()
	{
	}

	public Land(final String code, final String name, final String isoCode, final String format, final String minLength, final String maxLength)
	{
		this.code		= code;
		this.name		= name;
		this.isoCode	= isoCode;
		this.format		= format;
		this.minLength	= minLength;
		this.maxLength	= maxLength;
	}

	public String getCode()
	{
		return code;
	}

	public void setCode(final String code)
	{
		this.code = code;
	}

	public String getName()
	{
		return name;
	}

	public void setName(final String name)
	{
		this.name = name;
	}

	public String getIsoCode()
	{
		return isoCode;
	}

	public void setIsoCode(final String isoCode)
	{
		this.isoCode = isoCode;
	}

	public String getFormat()
	{
		return format;
	}

	public void setFormat(final String format)
	{
		this.format = format;
	}

	public String getMinLength()
	{
		return minLength;
	}

	public void setMinLength(final String minLength)
	{
		this.minLength = minLength;
	}

	public String getMaxLength()
	{
		return maxLength;
	}

	public void setMaxLength(final String maxLength)
	{
		this.maxLength = maxLength;
	}
}