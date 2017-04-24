package com.tween.viacelular.models;

import io.realm.RealmObject;
import io.realm.annotations.Ignore;
import io.realm.annotations.Index;
import io.realm.annotations.PrimaryKey;

/**
 * Modelo para persistir cuentas asociadas al dispositivo
 * Created by Tween (David Figueroa davo.figueroa@tween.com.ar) on 03/02/2016
 */
public class ConnectedAccount extends RealmObject
{
	@PrimaryKey
	@Index
	private String	name;
	@Index
	private String	type;

	@Ignore
	public static final String	TYPE_GOOGLE	= "com.google";

	public ConnectedAccount()
	{
	}

	public ConnectedAccount(final String name, final String type)
	{
		this.name	= name;
		this.type	= type;
	}

	public String getName()
	{
		return name;
	}

	public void setName(final String name)
	{
		this.name = name;
	}

	public String getType()
	{
		return type;
	}

	public void setType(final String type)
	{
		this.type = type;
	}
}