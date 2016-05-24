package com.tween.viacelular.data;

import android.content.Context;
import android.telephony.TelephonyManager;
import com.tween.viacelular.utils.Common;
import com.tween.viacelular.utils.StringUtils;
import org.json.JSONObject;

/**
 * Created by davidfigueroa on 3/8/15.
 * Reference by http://ip-api.com/
 */
public class Isp
{
	private Long	id;
	private String	as;
	private String	status;
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
	private String	query;
	private String	operatorNet;
	private String	operatorSim;
	private String	countryNet;
	private String	countrySim;

	public static final String KEY_COUNTRY		= "country";
	public static final String KEY_REGION		= "region";
	public static final String KEY_REGIONNAME	= "regionName";
	public static final String KEY_CITY			= "city";
	public static final String KEY_ZIP			= "zip";
	public static final String KEY_LAT			= "lat";
	public static final String KEY_LON			= "lon";
	public static final String KEY_TIMEZONE		= "timezone";
	public static final String KEY_ISP			= "isp";
	public static final String KEY_ORG			= "org";
	public static final String KEY_AS			= "as";
	public static final String KEY_QUERY		= "query";
	public static final String KEY_OPERATORNET	= "operatorNetwork";
	public static final String KEY_OPERATORSIM	= "operatorSim";
	public static final String KEY_COUNTRYNET	= "countryNetwork";
	public static final String KEY_COUNTRYSIM	= "countrySim";

	public Isp()
	{
	}

	public Isp(long id)
	{
		this.id = id;
	}

	public Isp(	String as, String status, String country, String countryCode, String region, String regionName, String city, String zip, String lat, String lon, String timezone, String isp,
				String org, String query, String operatorNet, String operatorSim, String countryNet, String countrySim)
	{
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
		this.query			= query;
		this.operatorNet	= operatorNet;
		this.operatorSim	= operatorSim;
		this.countryNet		= countryNet;
		this.countrySim		= countrySim;
	}

	public Isp(	Long id, String as, String status, String country, String countryCode, String region, String regionName, String city, String zip, String lat, String lon, String timezone, String isp,
				String org, String query, String operatorNet, String operatorSim, String countryNet, String countrySim)
	{
		this.id				= id;
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
		this.query			= query;
		this.operatorNet	= operatorNet;
		this.operatorSim	= operatorSim;
		this.countryNet		= countryNet;
		this.countrySim		= countrySim;
	}

	public Long getId()
	{
		return id;
	}

	public void setId(Long id)
	{
		this.id = id;
	}

	public String getAs()
	{
		if(as != null)
		{
			return as;
		}
		else
		{
			return "";
		}
	}

	public void setAs(String as)
	{
		this.as = as;
	}

	public String getStatus()
	{
		if(status != null)
		{
			return status;
		}
		else
		{
			return "";
		}
	}

	public void setStatus(String status)
	{
		this.status = status;
	}

	public String getCountry()
	{
		if(country != null)
		{
			return country;
		}
		else
		{
			return "";
		}
	}

	public void setCountry(String country)
	{
		this.country = country;
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

	public String getRegion()
	{
		if(region != null)
		{
			return region;
		}
		else
		{
			return "";
		}
	}

	public void setRegion(String region)
	{
		this.region = region;
	}

	public String getRegionName()
	{
		if(regionName != null)
		{
			return regionName;
		}
		else
		{
			return "";
		}
	}

	public void setRegionName(String regionName)
	{
		this.regionName = regionName;
	}

	public String getCity()
	{
		if(city != null)
		{
			return city;
		}
		else
		{
			return "";
		}
	}

	public void setCity(String city)
	{
		this.city = city;
	}

	public String getZip()
	{
		if(zip != null)
		{
			return zip;
		}
		else
		{
			return "";
		}
	}

	public void setZip(String zip)
	{
		this.zip = zip;
	}

	public String getLat()
	{
		if(lat != null)
		{
			return lat;
		}
		else
		{
			return "";
		}
	}

	public void setLat(String lat)
	{
		this.lat = lat;
	}

	public String getLon()
	{
		if(lon != null)
		{
			return lon;
		}
		else
		{
			return "";
		}
	}

	public void setLon(String lon)
	{
		this.lon = lon;
	}

	public String getTimezone()
	{
		if(timezone != null)
		{
			return timezone;
		}
		else
		{
			return "";
		}
	}

	public void setTimezone(String timezone)
	{
		this.timezone = timezone;
	}

	public String getIsp()
	{
		if(isp != null)
		{
			return isp;
		}
		else
		{
			return "";
		}
	}

	public void setIsp(String isp)
	{
		this.isp = isp;
	}

	public String getOrg()
	{
		if(org != null)
		{
			return org;
		}
		else
		{
			return "";
		}
	}

	public void setOrg(String org)
	{
		this.org = org;
	}

	public String getQuery()
	{
		if(query != null)
		{
			return query;
		}
		else
		{
			return "";
		}
	}

	public void setQuery(String query)
	{
		this.query = query;
	}

	public String getOperatorNet()
	{
		if(operatorNet != null)
		{
			return operatorNet;
		}
		else
		{
			return "";
		}
	}

	public void setOperatorNet(String operatorNet)
	{
		this.operatorNet = operatorNet;
	}

	public String getOperatorSim()
	{
		if(operatorSim != null)
		{
			return operatorSim;
		}
		else
		{
			return "";
		}
	}

	public void setOperatorSim(String operatorSim)
	{
		this.operatorSim = operatorSim;
	}

	public String getCountryNet()
	{
		if(countryNet != null)
		{
			return countryNet;
		}
		else
		{
			return "";
		}
	}

	public void setCountryNet(String countryNet)
	{
		this.countryNet = countryNet;
	}

	public String getCountrySim()
	{
		if(countrySim != null)
		{
			return countrySim;
		}
		else
		{
			return "";
		}
	}

	public void setCountrySim(String countrySim)
	{
		this.countrySim = countrySim;
	}

	public void debug()
	{
		System.out.println("Isp - id: " + id);
		System.out.println("Isp - as: " + as);
		System.out.println("Isp - status: " + status);
		System.out.println("Isp - country: " + country);
		System.out.println("Isp - countryCode: " + countryCode);
		System.out.println("Isp - region: " + region);
		System.out.println("Isp - regionName: " + regionName);
		System.out.println("Isp - city: " + city);
		System.out.println("Isp - zip: " + zip);
		System.out.println("Isp - lat: " + lat);
		System.out.println("Isp - lon: " + lon);
		System.out.println("Isp - timezone: " + timezone);
		System.out.println("Isp - isp: " + isp);
		System.out.println("Isp - org: " + org);
		System.out.println("Isp - query: " + query);
		System.out.println("Isp - operatorNet: " + operatorNet);
		System.out.println("Isp - operatorSim: " + operatorSim);
		System.out.println("Isp - countryNet: " + countryNet);
		System.out.println("Isp - countrySim: " + countrySim);
	}

	//Agregado para contemplar la operadora móvil
	public static void parseJSON(JSONObject json, IspDao ispDao, Context context)
	{
		try
		{
			String jAs			= "";
			String jStatus		= "";
			String jCountry		= "";
			String jCountryCode	= "";
			String jRegion		= "";
			String jRegionName	= "";
			String jCity		= "";
			String jZip			= "";
			String jLat			= "";
			String jLon			= "";
			String jTimezone	= "";
			String jIsp			= "";
			String jOrg			= "";
			String jQuery		= "";
			String jOpNet		= "";
			String jOpSim		= "";
			String jCoNet		= "";
			String jCoSim		= "";

			if(json != null)
			{
				if(json.has(KEY_AS))
				{
					if(StringUtils.isNotEmpty(json.getString(KEY_AS)))
					{
						jAs = json.getString(KEY_AS);
					}
				}

				if(json.has(Common.KEY_STATUS))
				{
					if(StringUtils.isNotEmpty(json.getString(Common.KEY_STATUS)))
					{
						jStatus = json.getString(Common.KEY_STATUS);
					}
				}

				if(json.has(KEY_COUNTRY))
				{
					if(StringUtils.isNotEmpty(json.getString(KEY_COUNTRY)))
					{
						jCountry = json.getString(KEY_COUNTRY);
					}
				}

				if(json.has(Country.KEY_API))
				{
					if(StringUtils.isNotEmpty(json.getString(Country.KEY_API)))
					{
						jCountryCode = json.getString(Country.KEY_API);
					}
				}

				if(json.has(KEY_REGION))
				{
					if(StringUtils.isNotEmpty(json.getString(KEY_REGION)))
					{
						jRegion = json.getString(KEY_REGION);
					}
				}

				if(json.has(KEY_REGIONNAME))
				{
					if(StringUtils.isNotEmpty(json.getString(KEY_REGIONNAME)))
					{
						jRegionName = json.getString(KEY_REGIONNAME);
					}
				}

				if(json.has(KEY_CITY))
				{
					if(StringUtils.isNotEmpty(json.getString(KEY_CITY)))
					{
						jCity = json.getString(KEY_CITY);
					}
				}

				if(json.has(KEY_ZIP))
				{
					if(StringUtils.isNotEmpty(json.getString(KEY_ZIP)))
					{
						jZip = json.getString(KEY_ZIP);
					}
				}

				if(json.has(KEY_LAT))
				{
					if(StringUtils.isNotEmpty(json.getString(KEY_LAT)))
					{
						jLat = json.getString(KEY_LAT);
					}
				}

				if(json.has(KEY_LON))
				{
					if(StringUtils.isNotEmpty(json.getString(KEY_LON)))
					{
						jLon = json.getString(KEY_LON);
					}
				}

				if(json.has(KEY_TIMEZONE))
				{
					if(StringUtils.isNotEmpty(json.getString(KEY_TIMEZONE)))
					{
						jTimezone = json.getString(KEY_TIMEZONE);
					}
				}

				if(json.has(KEY_ISP))
				{
					if(StringUtils.isNotEmpty(json.getString(KEY_ISP)))
					{
						jIsp = json.getString(KEY_ISP);
					}
				}

				if(json.has(KEY_ORG))
				{
					if(StringUtils.isNotEmpty(json.getString(KEY_ORG)))
					{
						jOrg = json.getString(KEY_ORG);
					}
				}

				if(json.has(KEY_QUERY))
				{
					if(StringUtils.isNotEmpty(json.getString(KEY_QUERY)))
					{
						jQuery = json.getString(KEY_QUERY);
					}
				}
			}

			//Obtenemos la operadora
			TelephonyManager manager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
			if(manager != null)
			{
				jOpNet	= manager.getNetworkOperatorName().toUpperCase();
				jOpSim	= manager.getSimOperatorName().toUpperCase();
				jCoNet	= manager.getNetworkCountryIso().toUpperCase();
				jCoSim	= manager.getSimCountryIso().toUpperCase();
			}

			//Agregado para probar en emuladores
			if(jOpNet.equals("ANDROID") || jOpSim.equals("ANDROID"))
			{
				jOpNet	= "PERSONAL";
				jOpSim	= "PERSONAL";
			}

			ispDao.insert(new Isp(jAs, jStatus, jCountry, jCountryCode, jRegion, jRegionName, jCity, jZip, jLat, jLon, jTimezone, jIsp, jOrg, jQuery, jOpNet, jOpSim, jCoNet, jCoSim));
		}
		catch(Exception e)
		{
			System.out.println("Isp:parseJSON - Exception: " + e);
			if(Common.DEBUG)
			{
				e.printStackTrace();
			}
		}
	}
}