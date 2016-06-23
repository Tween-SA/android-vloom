package com.tween.viacelular.models;

import io.realm.RealmObject;
import io.realm.annotations.Ignore;
import io.realm.annotations.Index;
import io.realm.annotations.PrimaryKey;

/**
 * Created by davidfigueroa on 31/3/16.
 * Reference by http://ip-api.com/
 */
public class Isp extends RealmObject
{
	@PrimaryKey
	@Index
	private String	query;
	private String	as;
	private String	status;
	@Index
	private String	country;
	private String	countryCode;
	private String	region;
	private String	regionName;
	private String	city;
	private String	zip;
	private String	lat;
	private String	lon;
	private String	timezone;
	private String	isp;
	private String	org;
	private String	operatorNet;
	private String	operatorSim;
	@Index
	private String	countryNet;
	@Index
	private String	countrySim;
	@Index
	private Long	updated;

	@Ignore
	public static final String KEY_COUNTRY		= "country";
	@Ignore
	public static final String KEY_REGION		= "region";
	@Ignore
	public static final String KEY_REGIONNAME	= "regionName";
	@Ignore
	public static final String KEY_CITY			= "city";
	@Ignore
	public static final String KEY_ZIP			= "zip";
	@Ignore
	public static final String KEY_LAT			= "lat";
	@Ignore
	public static final String KEY_LON			= "lon";
	@Ignore
	public static final String KEY_TIMEZONE		= "timezone";
	@Ignore
	public static final String KEY_ISP			= "isp";
	@Ignore
	public static final String KEY_ORG			= "org";
	@Ignore
	public static final String KEY_AS			= "as";
	@Ignore
	public static final String KEY_QUERY		= "query";
	@Ignore
	public static final String KEY_UPDATED		= "updated";

	public Isp()
	{
	}

	public Isp(	final String query, final String as, final String status, final String country, final String countryCode, final String region, final String regionName, final String city,
				final String zip, final String lat, final String lon, final String timezone, final String isp, final String org, final String operatorNet, final String operatorSim,
				final String countryNet, final String countrySim, final Long updated)
	{
		this.query			= query;
		this.as				= as;
		this.status			= status;
		this.country		= country;
		this.countryCode	= countryCode;
		this.region			= region;
		this.regionName		= regionName;
		this.city			= city;
		this.zip			= zip;
		this.lat			= lat;
		this.lon			= lon;
		this.timezone		= timezone;
		this.isp			= isp;
		this.org			= org;
		this.operatorNet	= operatorNet;
		this.operatorSim	= operatorSim;
		this.countryNet		= countryNet;
		this.countrySim		= countrySim;
		this.updated		= updated;
	}

	public String getQuery()
	{
		return query;
	}

	public void setQuery(final String query)
	{
		this.query = query;
	}

	public String getAs()
	{
		return as;
	}

	public void setAs(final String as)
	{
		this.as = as;
	}

	public String getStatus()
	{
		return status;
	}

	public void setStatus(final String status)
	{
		this.status = status;
	}

	public String getCountry()
	{
		return country;
	}

	public void setCountry(final String country)
	{
		this.country = country;
	}

	public String getCountryCode()
	{
		return countryCode;
	}

	public void setCountryCode(final String countryCode)
	{
		this.countryCode = countryCode;
	}

	public String getRegion()
	{
		return region;
	}

	public void setRegion(final String region)
	{
		this.region = region;
	}

	public String getRegionName()
	{
		return regionName;
	}

	public void setRegionName(final String regionName)
	{
		this.regionName = regionName;
	}

	public String getCity()
	{
		return city;
	}

	public void setCity(final String city)
	{
		this.city = city;
	}

	public String getZip()
	{
		return zip;
	}

	public void setZip(final String zip)
	{
		this.zip = zip;
	}

	public String getLat()
	{
		return lat;
	}

	public void setLat(final String lat)
	{
		this.lat = lat;
	}

	public String getLon()
	{
		return lon;
	}

	public void setLon(final String lon)
	{
		this.lon = lon;
	}

	public String getTimezone()
	{
		return timezone;
	}

	public void setTimezone(final String timezone)
	{
		this.timezone = timezone;
	}

	public String getIsp()
	{
		return isp;
	}

	public void setIsp(final String isp)
	{
		this.isp = isp;
	}

	public String getOrg()
	{
		return org;
	}

	public void setOrg(final String org)
	{
		this.org = org;
	}

	public String getOperatorNet()
	{
		return operatorNet;
	}

	public void setOperatorNet(final String operatorNet)
	{
		this.operatorNet = operatorNet;
	}

	public String getOperatorSim()
	{
		return operatorSim;
	}

	public void setOperatorSim(final String operatorSim)
	{
		this.operatorSim = operatorSim;
	}

	public String getCountryNet()
	{
		return countryNet;
	}

	public void setCountryNet(final String countryNet)
	{
		this.countryNet = countryNet;
	}

	public String getCountrySim()
	{
		return countrySim;
	}

	public void setCountrySim(final String countrySim)
	{
		this.countrySim = countrySim;
	}

	public Long getUpdated()
	{
		return updated;
	}

	public void setUpdated(final Long updated)
	{
		this.updated = updated;
	}
}