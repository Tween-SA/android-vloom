package com.tween.viacelular.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import com.tween.viacelular.R;
import com.tween.viacelular.models.Land;
import com.tween.viacelular.models.Message;
import com.tween.viacelular.models.Suscription;
import com.tween.viacelular.models.User;
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
 * Manejador de conexión contra apis REST bajo JSON
 * Created by Tween (David Figueroa davo.figueroa@tween.com.ar) on 03/08/15
 */
public class ApiConnection
{
	public static final String OK						= "OK";
	private static final String FAIL					= "FAIL";
	public static final String METHOD_GET				= "GET";
	public static final String METHOD_POST				= "POST";
	public static final String METHOD_PUT				= "PUT";
	private static final String TOKEN_AUTHORIZATION		= "Bearer d32f7a8d983b442f608bcdbef27e41c32bf0d9a8";
	public static final String FIREBASE_STORAGE			= "gs://tween-viacelular.appspot.com"; //Cubeta en Firebase para adjuntar imagenes a los mensajes
	public static final String FIREBASE_CHILD			= "messages_attached"; //Cubeta en Firebase para adjuntar imagenes a los mensajes
	public static final String CLOUDFRONT_S3			= "https://dfp5lnxq5eoj6.cloudfront.net/"; //Recuerdo que apunta al s3 https://s3-sa-east-1.amazonaws.com/vc-img/Logos/
	/**
	 * Url para redirigir a la web business
	 * "https://business.vloom.io/register"; //Production
	 * "https://stagging-business.vloom.io/register"; //Stagging
	 * "https://dev-business.vloom.io/register"; //Testing
	 */
	public static final String BUSINESS					= "https://business.vloom.io/register";
	/**
	 * Url para redirigir a la web de wechain precisamente al explorer
	 * "http://chain.vloom.io/"; //Production
	 * "http://chain.vloom.io/"; //Stagging
	 * "http://chain.vloom.io/"; //Testing
	 */
	public static final String CHAIN					= "http://chain.vloom.io/";
	public static final String CHAIN_ITEM				= CHAIN+"#/item/";
	/**
	 * Url base del server
	 * "https://api.vloom.io/v1/"; //Production - master
	 * "https://stagging.vloom.io/v1/"; //Stagging - stagging
	 * "https://dev.vloom.io/v1/"; //Testing - develop
	 * "https://private-16a42-viacelular.apiary-mock.com/v1.0/"; //Development Apiary
	 * "https://private-29fe84-davidfigueroa.apiary-mock.com/v1/"; //Development Apiary Private
	 */
	private static final String SERVERP					= "https://api.vloom.io/v1/";
	public static final String IP_API					= "http://ip-api.com/json";
	public static final String COMPANIES				= SERVERP+"companies";
	public static final String COUNTRIES				= SERVERP+"countries?locale="+Locale.getDefault().getLanguage();
	public static final String MESSAGES					= SERVERP+"messages";
	public static final String USERS					= SERVERP+"users";
	public static final String COMPANIES_BY_COUNTRY		= COMPANIES+"/"+ Land.KEY_API+"?code";
	public static final String COMPANIES_SOCIAL			= COMPANIES+"/"+Suscription.KEY_API+"/social";
	public static final String USERS_MESSAGES			= USERS+"/"+User.KEY_API+"/messages";
	public static final String CERTIFICATE_MESSAGES		= MESSAGES+"/"+ Message.KEY_API+"/certificate";
	public static final String SEND_SMS					= MESSAGES+"/lists";
	public static final String CALLME					= USERS+"/tts";
	public static final String MODIFY_COMPANIES			= USERS+"/"+ User.KEY_API+"/subscriptions";
	public static final String SUGGESTIONS				= MODIFY_COMPANIES+"?country="+Land.KEY_API;

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
						if(Common.DEBUG)
						{
							System.out.println("Red: "+networkInfo.getTypeName()+" - "+networkInfo.toString());
						}

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
			Utils.logError(context, "ApiConnection:checkInternet - Exception: ", e);
		}

		return result;
	}

	public static String getNetwork(Context context)
	{
		String network = "";

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
						network = networkInfo.getTypeName();
					}
				}
			}
		}
		catch(Exception e)
		{
			Utils.logError(context, "ApiConnection:getNetwork - Exception: ", e);
		}

		return network;
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
			Utils.logError(context, "ApiConnection:loadJSONFromAsset - Exception: ", e);
		}

		return json;
	}

	public static String request(String urlStr, Context context, String method, String authorization, String jsonParams)
	{
		String result		= "{}";
		InputStream stream	= null;
		String message		= "";
		String headers		= "";
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
				connection.setConnectTimeout(10500);
				connection.setReadTimeout(10500);
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

				try
				{
					httpConnection.connect();

					if(httpConnection.getHeaderFields() != null)
					{
						headers	= httpConnection.getHeaderFields().toString();
					}

					code	= httpConnection.getResponseCode();
					message	= httpConnection.getResponseMessage();
				}
				catch(Exception e)
				{
					Utils.logError(context, "ApiConnection:request:getResponseCode() - Exception:", e);
				}

				if(Common.DEBUG)
				{
					System.out.println("Headers: " + headers);
					System.out.println("Response Code: " + code);
					System.out.println("Response Message: " + message);
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
					result = convertInputStreamToString(stream, context);
				}

				result = StringUtils.removeSpacesJSON(result);

				if(Common.DEBUG)
				{
					System.out.println("Original Result: " + result);
					System.out.println(	"Original !ifJson: " + (!result.startsWith("{") && !result.endsWith("}") && !result.startsWith("[") && !result.endsWith("]"))+
										" isHTML: " +(result.contains("Service Temporarily Unavailable") || result.contains("html") || result.contains("HTML")));
				}
			}
			catch(Exception e)
			{
				Utils.logError(context, "ApiConnection:request - Exception: ", e);
			}
		}

		if(StringUtils.isNotEmpty(result))
		{
			if(!result.startsWith("{") && !result.endsWith("}") && !result.startsWith("[") && !result.endsWith("]"))
			{
				if(result.contains("Service Temporarily Unavailable") || result.contains("html") || result.contains("HTML"))
				{
					result = "{\"status\":\"FAIL\",\"statusMessage\":\"" + context.getString(R.string.service_unavailable) + "\",\"statusCode\":" + HttpURLConnection.HTTP_UNAVAILABLE
								+ ",\"content\":null}";
				}
				else
				{
					result = "{\"status\":\"FAIL\",\"statusMessage\":\"" + context.getString(R.string.service_unavailable) + "\",\"statusCode\":" + HttpURLConnection.HTTP_INTERNAL_ERROR
								+ ",\"content\":null}";
				}
			}
			else
			{
				if(!connected)
				{
					result = "{\"status\":\"FAIL\",\"statusMessage\":\"" + context.getString(R.string.no_internet) + "\",\"statusCode\":" + HttpURLConnection.HTTP_INTERNAL_ERROR
								+ ",\"content\":null}";
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
							result = "{\"status\":\"OK\",\"statusMessage\":\"" + message + "\",\"statusCode\":" + code + ",\"content\":" + result + "}";
						break;

						case HttpURLConnection.HTTP_BAD_REQUEST:
							result = "{\"status\":\"FAIL\",\"statusMessage\":\"" + context.getString(R.string.bad_request) + "\",\"statusCode\":" + code + ",\"content\":" + result + "}";
						break;

						default:
							result = "{\"status\":\"FAIL\",\"statusMessage\":\"" + context.getString(R.string.service_unavailable) + "\",\"statusCode\":" + code + ",\"content\":null}";
						break;
					}
				}
			}
		}
		else
		{
			result = "{\"status\":\"FAIL\",\"statusMessage\":\"" + context.getString(R.string.service_unavailable) + "\",\"statusCode\":" + code + ",\"content\":null}";
		}

		if(Common.DEBUG)
		{
			System.out.println("Final Response: " + result);
		}

		return result;
	}

	private static String convertInputStreamToString(InputStream inputStream, Context context)
	{
		String result = "";
		try
		{
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
			String line;

			while((line = bufferedReader.readLine()) != null)
			{
				result += line;
			}

			inputStream.close();
		}
		catch(Exception e)
		{
			Utils.logError(context, "ApiConnection:convertInputStreamToString - Exception: ", e);
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
						if(json.has(Common.KEY_CONTENT))
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
							if(json.has(Common.KEY_CONTENT))
							{
								if(!json.isNull(Common.KEY_CONTENT))
								{
									JSONObject jsonData = json.getJSONObject(Common.KEY_CONTENT);
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
			Utils.logError(context, "ApiConnection:checkResponse - Exception: ", e);
		}

		return result;
	}
}