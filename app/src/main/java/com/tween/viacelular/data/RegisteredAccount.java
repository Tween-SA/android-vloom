package com.tween.viacelular.data;

public class RegisteredAccount
{
	private Long	id;
	/**
	 * Not-null value.
	 */
	private String	name;
	/**
	 * Not-null value.
	 */
	private String	type;

	public static final String TYPE_GOOGLE		= "com.google";
	public static final String TYPE_TELEGRAM	= "org.telegram.messenger";

	public RegisteredAccount()
	{

	}

	public RegisteredAccount(long id)
	{
		this.id = id;
	}

	public RegisteredAccount(long id, String name, String type)
	{
		this.id = id;
		this.name = name;
		this.type = type;
	}

	public Long getId()
	{
		return id;
	}

	public void setId(Long id)
	{
		this.id = id;
	}

	/**
	 * Not-null value.
	 */
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

	/**
	 * Not-null value; ensure this value is available before it is saved to the database.
	 */
	public void setName(String name)
	{
		this.name = name;
	}

	/**
	 * Not-null value.
	 */
	public String getType()
	{
		if(type != null)
		{
			return type;
		}
		else
		{
			return TYPE_GOOGLE;
		}
	}

	/**
	 * Not-null value; ensure this value is available before it is saved to the database.
	 */
	public void setType(String type)
	{
		this.type = type;
	}
}