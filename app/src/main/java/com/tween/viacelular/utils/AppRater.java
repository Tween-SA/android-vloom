package com.tween.viacelular.utils;

import android.content.Context;
import android.content.SharedPreferences;
import java.util.Calendar;

/**
 * Implementación de dialog para calificar app
 * Created by davidfigueroa on 14/11/15.
 */
public class AppRater
{
	public static long daysUntilPrompt	=(Common.DAYS_UNTIL_PROMPT*DateUtils.oneDayTs);

	/**
	 * Se invoca al entrar en el home para contabilizar días y ejecuciones desde la instalación del update
	 * @param context
	 * @return boolean
	 */
	public static boolean launchApp(final Context context)
	{
		boolean result = false;
		//Agregado para prevenir excepciones
		try
		{
			SharedPreferences preferences	= context.getSharedPreferences(Common.KEY_PREF, Context.MODE_PRIVATE);
			int delayTimes					= preferences.getInt(Common.KEY_PREF_DELAY_RATE, 0);
			boolean dontShowAgain			= preferences.getBoolean(Common.KEY_PREF_NO_RATE, false);
			long dateFirstLaunch			= preferences.getLong(Common.KEY_PREF_DATE_1STLAUNCH, 0);
			SharedPreferences.Editor editor	= preferences.edit();
			long delayTs					= (delayTimes*DateUtils.oneDayTs);
			Calendar dateResult				= Calendar.getInstance();
			Calendar dateNow				= Calendar.getInstance();

			if(dateFirstLaunch == 0)
			{
				dateFirstLaunch = System.currentTimeMillis();
				editor.putLong(Common.KEY_PREF_DATE_1STLAUNCH, dateFirstLaunch);
			}

			dateResult.setTimeInMillis((dateFirstLaunch + (daysUntilPrompt + delayTs)));

			if(Common.DEBUG)
			{
				System.out.println("dateNow: " + dateNow.get(Calendar.DATE) + "/" + (dateNow.get(Calendar.MONTH) + 1) + "/" + dateNow.get(Calendar.YEAR) + " " + dateNow.get(Calendar.HOUR) + ":" + dateNow.get(Calendar.MINUTE) + ":" + dateNow.get(Calendar.SECOND));
				System.out.println("dateResult: " + dateResult.get(Calendar.DATE) + "/" + (dateResult.get(Calendar.MONTH) + 1) + "/" + dateResult.get(Calendar.YEAR) + " " + dateResult.get(Calendar.HOUR) + ":" + dateResult.get(Calendar.MINUTE) + ":" + dateResult.get(Calendar.SECOND));
				System.out.println("date FirstLaunch: " + DateUtils.getDateFromTs(dateFirstLaunch, context) + " delay: " + delayTimes);
				System.out.println("condition: " + (dateNow.compareTo(dateResult) >= 0));
			}

			if(!dontShowAgain)
			{
				//Corrección del calculo para postergar la aparición del popup para calificar
				if(dateNow.compareTo(dateResult) >= 0)
				{
					//La fecha es menor o igual al momento actual
					result = true;
				}
			}

			editor.apply();
		}
		catch(Exception e)
		{
			Utils.logError(context, "AppRater:launchApp - Exception:", e);
		}

		return result;
	}

	/**
	 * Redirige a Play Store para que el usuario califique la app
	 * @param context
	 */
	public static void rateApp(final Context context)
	{
		//Agregado para prevenir excepciones
		try
		{
			SharedPreferences preferences	= context.getSharedPreferences(Common.KEY_PREF, Context.MODE_PRIVATE);
			SharedPreferences.Editor editor	= preferences.edit();
			editor.putBoolean(Common.KEY_PREF_NO_RATE, true);
			editor.apply();
		}
		catch(Exception e)
		{
			Utils.logError(context, "AppRater:rateApp - Exception:", e);
		}
	}

	/**
	 * Contabiliza la postergación de esta sugerencia al precionar en "Más tarde"
	 * @param context
	 */
	public static void delayRateApp(final Context context)
	{
		//Agregado para prevenir excepciones
		try
		{
			SharedPreferences preferences	= context.getSharedPreferences(Common.KEY_PREF, Context.MODE_PRIVATE);
			int delayTimes					= preferences.getInt(Common.KEY_PREF_DELAY_RATE, 0);
			long dateFirstLaunch			= preferences.getLong(Common.KEY_PREF_DATE_1STLAUNCH, 0);
			SharedPreferences.Editor editor	= preferences.edit();

			//Agregado para incrementar los días de delay
			delayTimes = delayTimes+1;

			//Agregado para evitar reiteraciones innecesarias por fecha vieja, tiempo de reiteración más prolongado
			if(dateFirstLaunch != 0)
			{
				Calendar dateResult	= Calendar.getInstance();
				Calendar dateNow	= Calendar.getInstance();
				dateResult.setTimeInMillis((dateFirstLaunch + (daysUntilPrompt + (delayTimes * DateUtils.oneDayTs))));

				if(dateNow.compareTo(dateResult) <= 0)
				{
					//La fecha calculada es menor o igual a la actual se ajustará para que sea una fecha futura
					int count = 2;
					dateResult.add(Calendar.DATE, count);

					if(dateNow.compareTo(dateResult) <= 0)
					{
						while(dateNow.compareTo(dateResult) <= 0)
						{
							count = count+2;
							dateResult.add(Calendar.DATE, count);

							if(dateNow.compareTo(dateResult) > 0)
							{
								break;
							}
						}
					}

					delayTimes = delayTimes + count;
				}
			}

			editor.putInt(Common.KEY_PREF_DELAY_RATE, delayTimes);
			editor.apply();
		}
		catch(Exception e)
		{
			Utils.logError(context, "AppRater:delayRateApp - Exception:", e);
		}
	}
}