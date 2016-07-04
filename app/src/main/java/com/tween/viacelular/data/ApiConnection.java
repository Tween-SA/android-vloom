package com.tween.viacelular.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import com.tween.viacelular.R;
import com.tween.viacelular.utils.Common;
import com.tween.viacelular.utils.StringUtils;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.Locale;

/**
 * Created by davidfigueroa on 3/8/15.
 */
public class ApiConnection
{
	public static final String OK					= "OK";
	public static final String FAIL					= "FAIL";
	public static final String METHOD_GET			= "GET";
	public static final String METHOD_POST			= "POST";
	public static final String METHOD_PUT			= "PUT";
	public static final String TOKEN_AUTHORIZATION	= "Bearer d32f7a8d983b442f608bcdbef27e41c32bf0d9a8";
	public static final String CLOUDFRONT_S3		= "https://d1ads2zadze8sp.cloudfront.net/"; //Recuerdo que apunta al s3 https://s3-sa-east-1.amazonaws.com/vc-img/Logos/
	public static final String SERVERP				= "https://api.vloom.io/v1/"; //New Production - master
	//public static final String SERVERP				= "https://dev.vloom.io/v1/"; //Testing - develop
	//public static final String SERVER				= "https://private-16a42-viacelular.apiary-mock.com/v1.0/"; //Development Apiary
	//public static final String SERVER				= "https://private-29fe84-davidfigueroa.apiary-mock.com/v1/"; //Development Apiary Private
	public static final String IP_API				= "http://ip-api.com/json";
	public static final String COMPANIES			= SERVERP + "companies";
	public static final String COUNTRIES			= SERVERP + "countries?locale="+Locale.getDefault().getLanguage();
	public static final String MESSAGES				= SERVERP + "messages";
	public static final String USERS				= SERVERP + "users";
	public static final String COMPANIES_BY_COUNTRY	= COMPANIES+"/"+Country.KEY_API+"?code";
	public static final String SEND_SMS				= MESSAGES + "/lists";
	public static final String CALLME				= USERS + "/tts";
	public static final String MODIFY_COMPANIES		= USERS + "/" + User.KEY_API + "/subscriptions";

	/**
	 * Detecta si hay conexión a internet.
	 * @param context
	 * @return
	 */
	public static boolean checkInternet(Context context)
	{
		//Refactor
		boolean result = false;

		try
		{
			if(context != null)
			{
				ConnectivityManager connectivityManager	= (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

				if(connectivityManager != null)
				{
					NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

					if(networkInfo != null)
					{
						if(networkInfo.isConnected())
						{
							result = true;
						}
					}
				}
			}
		}
		catch(Exception e)
		{
			System.out.println("ApiConnection:checkInternet - Exception: " + e);
			if(Common.DEBUG)
			{
				e.printStackTrace();
			}
		}

		return result;
	}

	/**
	 * Carga de json mockup hasta que se disponga de la api correspondiente
	 * @param context
	 * @param file
	 * @return String
	 */
	public static String loadJSONFromAsset(Context context, String file)
	{
		String json = "";
		try
		{
			InputStream is	= context.getAssets().open(file);
			int size		= is.available();
			byte[] buffer	= new byte[size];
			is.read(buffer);
			is.close();
			json			= new String(buffer, "UTF-8");
		}
		catch(Exception e)
		{
			System.out.println("ApiConnection:loadJSONFromAsset - Exception: " + e);
			if(Common.DEBUG)
			{
				e.printStackTrace();
			}
		}

		return json;
	}

	public static String request(String urlStr, Context context, String method, String authorization, String jsonParams)
	{
		String result		= "{}";
		InputStream stream	= null;
		String message		= "";
		int code			= 0;
		boolean connected	= checkInternet(context);

		if(Common.DEBUG)
		{
			System.out.println("isConnected: " + connected);
		}

		if(connected)
		{
			try
			{
				if(Common.DEBUG)
				{
					System.out.println("Url: " + urlStr + " Method: " + method + " Authorization: " + authorization + " Lang: " + Locale.getDefault().getLanguage()+"-"+Locale.getDefault().getCountry());
					System.out.println("Body: " + jsonParams);
				}

				URL url								= new URL(urlStr);
				URLConnection connection			= url.openConnection();
				connection.setConnectTimeout(10000);
				connection.setReadTimeout(10000);
				HttpURLConnection httpConnection	= (HttpURLConnection) connection;
				httpConnection.setRequestMethod(method);
				httpConnection.setRequestProperty("Accept", "application/json");
				httpConnection.setRequestProperty("Content-type", "application/json");
				httpConnection.setRequestProperty("Accept-Language", Locale.getDefault().getLanguage()+"-"+Locale.getDefault().getCountry());

				if(StringUtils.isEmpty(authorization))
				{
					authorization = TOKEN_AUTHORIZATION;
				}

				httpConnection.setRequestProperty("Authorization", authorization);
				SharedPreferences preferences	= context.getApplicationContext().getSharedPreferences(Common.KEY_PREF, Context.MODE_PRIVATE);
				SharedPreferences.Editor editor	= preferences.edit();
				editor.putString(Common.KEY_TOKEN, authorization);
				editor.apply();

				httpConnection.setUseCaches(false);

				if(StringUtils.isNotEmpty(jsonParams))
				{
					byte[] outputInBytes	= jsonParams.getBytes("UTF-8");
					OutputStream os			= httpConnection.getOutputStream();
					os.write(outputInBytes);
					os.close();
				}

				httpConnection.connect();

				if(httpConnection != null)
				{
					try
					{
						code	= httpConnection.getResponseCode();
						message	= httpConnection.getResponseMessage();
					}
					catch(Exception e)
					{
						System.out.println("ApiConnection:request:getResponseCode() - Exception: " + e);
						if(Common.DEBUG)
						{
							e.printStackTrace();
						}
					}
				}

				if(Common.DEBUG)
				{
					System.out.println("Headers: " + httpConnection.getHeaderFields());
					System.out.println("Response Code: " + httpConnection.getResponseCode());
					System.out.println("Response Message: " + httpConnection.getResponseMessage());
				}

				if(code == HttpURLConnection.HTTP_OK || code == HttpURLConnection.HTTP_CREATED || code == HttpURLConnection.HTTP_ACCEPTED)
				{
					stream = httpConnection.getInputStream();
				}
				else
				{
					if(code < HttpURLConnection.HTTP_NO_CONTENT)
					{
						stream = httpConnection.getErrorStream();
					}
				}

				if(stream != null)
				{
					result = convertInputStreamToString(stream);
				}

				result = StringUtils.removeSpacesJSON(result);

				if(Common.DEBUG)
				{
					System.out.println("Original Result: " + result);
					System.out.println("Original !ifJson: " + (!result.startsWith("{") && !result.endsWith("}") && !result.startsWith("[") && !result.endsWith("]")));
					System.out.println("Original isHTML: " + (result.contains("Service Temporarily Unavailable") || result.contains("html") || result.contains("HTML")));
				}
			}
			catch(Exception e)
			{
				System.out.println("ApiConnection:request - Exception: " + e);
				if(Common.DEBUG)
				{
					e.printStackTrace();
				}
			}
		}

		if(StringUtils.isNotEmpty(result))
		{
			if(!result.startsWith("{") && !result.endsWith("}") && !result.startsWith("[") && !result.endsWith("]"))
			{
				if(result.contains("Service Temporarily Unavailable") || result.contains("html") || result.contains("HTML"))
				{
					result = "{\"status\":\"FAIL\",\"statusMessage\":\"" + context.getString(R.string.service_unavailable) + "\",\"statusCode\":" + HttpURLConnection.HTTP_UNAVAILABLE + ",\"data\":null}";
				}
				else
				{
					result = "{\"status\":\"FAIL\",\"statusMessage\":\"" + context.getString(R.string.service_unavailable) + "\",\"statusCode\":" + HttpURLConnection.HTTP_INTERNAL_ERROR + ",\"data\":null}";
				}
			}
			else
			{
				if(!connected)
				{
					result = "{\"status\":\"FAIL\",\"statusMessage\":\"" + context.getString(R.string.no_internet) + "\",\"statusCode\":" + HttpURLConnection.HTTP_INTERNAL_ERROR + ",\"data\":null}";
				}
				else
				{
					//Se arma una cabecera json en la que se inserta el response que viene del server
					switch(code)
					{
						case HttpURLConnection.HTTP_OK:
						case HttpURLConnection.HTTP_CREATED:
						case HttpURLConnection.HTTP_ACCEPTED:
						case HttpURLConnection.HTTP_NO_CONTENT:
							result = "{\"status\":\"OK\",\"statusMessage\":\"" + message + "\",\"statusCode\":" + code + ",\"data\":" + result + "}";
						break;

						case HttpURLConnection.HTTP_BAD_REQUEST:
							result = "{\"status\":\"FAIL\",\"statusMessage\":\"" + context.getString(R.string.bad_request) + "\",\"statusCode\":" + code + ",\"data\":" + result + "}";
						break;

						default:
							result = "{\"status\":\"FAIL\",\"statusMessage\":\"" + context.getString(R.string.service_unavailable) + "\",\"statusCode\":" + code + ",\"data\":null}";
						break;
					}
				}
			}
		}
		else
		{
			result = "{\"status\":\"FAIL\",\"statusMessage\":\"" + context.getString(R.string.service_unavailable) + "\",\"statusCode\":" + code + ",\"data\":null}";
		}

		if(Common.DEBUG)
		{
			System.out.println("Final Response: " + result);
		}

		return result;
	}

	public static String convertInputStreamToString(InputStream inputStream)
	{
		String result = "";
		try
		{
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

			if(bufferedReader != null)
			{
				String line = "";
				while((line = bufferedReader.readLine()) != null)
				{
					result += line;
				}
			}

			inputStream.close();
		}
		catch(Exception e)
		{
			System.out.println("ApiConnection:convertInputStreamToString - Exception: " + e);
			if(Common.DEBUG)
			{
				e.printStackTrace();
			}
		}

		return result;
	}

	//Optimización combinando aquí lo que también se hacía en getStatusMessage
	public static String checkResponse(Context context, JSONObject json)
	{
		String result = "";
		try
		{
			if(json != null)
			{
				if(json.has(Common.KEY_STATUS))
				{
					if(json.getString(Common.KEY_STATUS).equals(OK))
					{
						if(json.has(Common.KEY_DATA))
						{
							result = OK;
						}
						else
						{
							result = context.getString(R.string.response_invalid);
						}
					}
					else
					{
						if(json.getString(Common.KEY_STATUS).equals(FAIL))
						{
							if(json.has(Common.KEY_DATA))
							{
								if(!json.isNull(Common.KEY_DATA))
								{
									JSONObject jsonData = json.getJSONObject(Common.KEY_DATA);
									if(jsonData != null)
									{
										//El error es HTTP_BAD_REQUEST (400)
										if(jsonData.has(Common.KEY_DESCRIPTION))
										{
											result = jsonData.getString(Common.KEY_DESCRIPTION);
										}

										if(jsonData.has(Common.KEY_RESPONSE))
										{
											result = jsonData.getString(Common.KEY_RESPONSE);
										}

										if(StringUtils.isEmpty(result))
										{
											result = context.getString(R.string.response_invalid);
										}
									}
									else
									{
										//El error es mayor a HTTP_BAD_REQUEST (400) y no tiene json en el response
										result = context.getString(R.string.response_invalid);
									}
								}
								else
								{
									result = context.getString(R.string.response_invalid);
								}
							}
							else
							{
								result = context.getString(R.string.response_invalid);
							}
						}
						else
						{
							result = context.getString(R.string.response_invalid);
						}
					}
				}
				else
				{
					result = context.getString(R.string.response_invalid);
				}
			}
			else
			{
				result = context.getString(R.string.no_content);
			}
		}
		catch(Exception e)
		{
			System.out.println("ApiConnection:checkResponse - Exception: " + e);
			if(Common.DEBUG)
			{
				e.printStackTrace();
			}
		}

		return result;
	}
}