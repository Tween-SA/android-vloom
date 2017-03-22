package com.tween.viacelular.services;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.text.format.DateFormat;
import com.tween.viacelular.R;
import com.tween.viacelular.asynctask.CheckCodeAsyncTask;
import com.tween.viacelular.asynctask.ConnectApiSMSAsyncTask;
import com.tween.viacelular.models.Land;
import com.tween.viacelular.models.Message;
import com.tween.viacelular.models.Migration;
import com.tween.viacelular.models.Suscription;
import com.tween.viacelular.models.SuscriptionHelper;
import com.tween.viacelular.models.User;
import com.tween.viacelular.utils.Common;
import com.tween.viacelular.utils.StringUtils;
import com.tween.viacelular.utils.Utils;
import java.util.Calendar;
import java.util.Locale;
import io.realm.Realm;
import io.realm.RealmResults;

/**
 * Created by david.figueroa on 6/7/15.
 */
public class IncomingSmsService extends BroadcastReceiver
{
	@Override
	public void onReceive(final Context context, final Intent intent)
	{
		try
		{
			Migration.getDB(context);
			final Bundle bundle				= intent.getExtras();
			SharedPreferences preferences	= context.getSharedPreferences(Common.KEY_PREF, Context.MODE_PRIVATE);
			SharedPreferences.Editor editor	= preferences.edit();
			String code						= "";
			Message notification			= null;

			if(bundle != null)
			{
				final Object[] pdusObj = (Object[]) bundle.get("pdus");

				if(pdusObj != null)
				{
					//Mejora en la performance
					for(Object singlePdusObj : pdusObj)
					{
						SmsMessage currentMessage;

						if(Common.API_LEVEL >= Build.VERSION_CODES.M)
						{
							currentMessage = SmsMessage.createFromPdu((byte[]) singlePdusObj, "3gpp");

							if(currentMessage == null)
							{
								currentMessage = SmsMessage.createFromPdu((byte[]) singlePdusObj, "3gpp2");
							}
						}
						else
						{
							currentMessage = SmsMessage.createFromPdu((byte[]) singlePdusObj);
						}

						String address;
						String message;
						String date;

						if(currentMessage != null)
						{
							address	= currentMessage.getDisplayOriginatingAddress().replace("+", "");//Quitar carácteres que puedan romper el sms al procesarlo
							message	= currentMessage.getDisplayMessageBody().replace("\\t", " ").replace("\\n", " ").replace("\\r", " ").replace("\\u000A", " ").trim();

							if(currentMessage.getTimestampMillis() < System.currentTimeMillis())
							{
								date = String.valueOf(currentMessage.getTimestampMillis());
							}
							else
							{
								date = String.valueOf(System.currentTimeMillis());
							}

							if(Common.DEBUG)
							{
								Calendar cal			= Calendar.getInstance(Locale.ENGLISH);
								cal.setTimeInMillis(currentMessage.getTimestampMillis());
								String dateFormated		= DateFormat.format("yyyy-MM-dd HH:mm:ss", cal).toString();
								cal.setTimeInMillis(System.currentTimeMillis());
								String dateFormated2	= DateFormat.format("yyyy-MM-dd HH:mm:ss", cal).toString();
								System.out.println("IncomingSmsService - SenderNum: " + address + "; message: " + message+"; dateSMS: "+dateFormated+"; currentDate: "+dateFormated2);
							}

							//Agregado para prevenir las consultas si no se otorgo sessión para el dao
							Realm realm							= Realm.getDefaultInstance();
							RealmResults<Message> notifications	= realm.where(Message.class).equalTo(Message.KEY_CHANNEL, address).equalTo(Common.KEY_TYPE, Message.TYPE_SMS)
																	.equalTo(Message.KEY_CREATED, Long.valueOf(date)).findAll();

							if(notifications.size() == 0)
							{
								if(	StringUtils.isPhoneNumber(address) || message.contains(Message.SMS_CODE) || message.contains(Message.SMS_CODE_ES) ||
									message.contains(Message.SMS_CODE_NEW) || message.contains(Message.SMS_CODE_ES_NEW))
								{
									notification = new Message();
									notification.setType(Message.TYPE_SMS);
									notification.setMsg(message);
									notification.setCreated(System.currentTimeMillis());
									notification.setChannel(address);
									notification.setStatus(Message.STATUS_RECEIVE);
									//Modificación para contemplar cambio en tratamiento de números cortos
									editor.putInt(Common.KEY_LAST_MSGID, preferences.getInt(Common.KEY_LAST_MSGID, 1) + 1);
									editor.apply();
									notification.setMsgId(String.valueOf(preferences.getInt(Common.KEY_LAST_MSGID, 1)));
									//Agregado para continuar numeración de msgId

									Suscription client;
									String companyId	= "";

									if(message.contains(Message.SMS_CODE))
									{
										code	= message.replace(Message.SMS_CODE, "");
										code	= code.trim().substring(0, Common.CODE_LENGTH);
									}
									else
									{
										//Agregado para contemplar cuando el mensaje sea traducido al español
										if(message.contains(Message.SMS_CODE_ES))
										{
											code	= message.replace(Message.SMS_CODE_ES, "");
											code	= code.trim().substring(0, Common.CODE_LENGTH);
										}
									}

									if(message.contains(Message.SMS_CODE_NEW))
									{
										code	= message.replace(Message.SMS_CODE_NEW, "");
										code	= code.trim().substring(0, Common.CODE_LENGTH);
									}
									else
									{
										//Agregado para contemplar cuando el mensaje sea traducido al español
										if(message.contains(Message.SMS_CODE_ES_NEW))
										{
											code	= message.replace(Message.SMS_CODE_ES_NEW, "");
											code	= code.trim().substring(0, Common.CODE_LENGTH);
										}
									}

									//Modificaciones para contemplar números cortos de más de una company
									if(StringUtils.isNotEmpty(code))
									{
										companyId = Suscription.COMPANY_ID_VC_MONGO;
									}
									else
									{
										if(message.toUpperCase().contains(context.getString(R.string.app_name).toUpperCase()) || message.toUpperCase().contains("VIACELULAR"))
										{
											//Optimización para evitar bucle por un registro
											companyId = Suscription.COMPANY_ID_VC_MONGO;
										}
										else
										{
											User user		= realm.where(User.class).findFirst();
											String country	= preferences.getString(Land.KEY_API, "");

											if(user != null)
											{
												if(StringUtils.isNotEmpty(user.getCountryCode()))
												{
													country	= user.getCountryCode();
													editor.putString(Land.KEY_API, country);
													editor.apply();
												}
											}

											//Re-estructuración para mejorar clasificación de sms
											client = realm.where(Suscription.class).equalTo(Suscription.KEY_API, SuscriptionHelper.classifySubscription(address, message, context, country))
														.findFirst();

											if(client != null)
											{
												companyId = client.getCompanyId();
											}
										}
									}

									//Agregado para contemplar país y celular del usuario al recibir un sms
									notification.setCountryCode(preferences.getString(Land.KEY_API, ""));
									notification.setPhone(preferences.getString(User.KEY_PHONE, ""));
									notification.setCompanyId(companyId);
									notification.setFlags(Message.FLAGS_SMS);
									//Agregado para contemplar números largos
									notification.setKind(Message.KIND_TEXT);

									if(!StringUtils.isCompanyNumber(address))
									{
										notification.setStatus(Message.STATUS_PERSONAL);
									}

									realm.beginTransaction();
									realm.copyToRealmOrUpdate(notification);
									realm.commitTransaction();

									//Agregado para mostrar notificación sin sonido
									if(notification.getStatus() != Message.STATUS_PERSONAL || StringUtils.isNotEmpty(code))
									{
										Utils.showPush(context, preferences.getString(User.KEY_PHONE, ""), String.valueOf(Common.BOOL_YES), notification);
									}
								}
							}
						}
					}

					if(StringUtils.isNotEmpty(code))
					{
						if(StringUtils.isValidCode(code))
						{
							new CheckCodeAsyncTask(context, code, false).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
						}
					}
					else
					{
						//Agregado para enviar a la api sms que acaba de llegar
						if(notification != null)
						{
							final ConnectApiSMSAsyncTask task	= new ConnectApiSMSAsyncTask(context, false);
							task.setMessage(notification);
							task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
						}
					}
				}
			}
		}
		catch(Exception e)
		{
			Utils.logError(context, "IncomingSmsService:onReceive - Exception:", e);
		}
	}

	/**
	 * Agregado para enviar sms de baja al bloquear Company
	 */
	public static void sendSMS(Context context, String phoneNumber, String message)
	{
		try
		{
			if(context != null && StringUtils.isNotEmpty(phoneNumber) && StringUtils.isNotEmpty(message))
			{
				PendingIntent sentPI		= PendingIntent.getBroadcast(context, 0, new Intent("SMS_SENT"), 0);
				PendingIntent deliveredPI	= PendingIntent.getBroadcast(context, 0, new Intent("SMS_DELIVERED"), 0);

				if(sentPI != null && deliveredPI != null)
				{
					context.registerReceiver(new BroadcastReceiver()
					{
						@Override
						public void onReceive(Context arg0, Intent arg1)
						{
							switch(getResultCode())
							{
								case Activity.RESULT_OK:
									System.out.println("SMS sent");
								break;

								case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
									System.out.println("Generic failure");
								break;

								case SmsManager.RESULT_ERROR_NO_SERVICE:
									System.out.println("No service");
								break;

								case SmsManager.RESULT_ERROR_NULL_PDU:
									System.out.println("Null PDU");
								break;

								case SmsManager.RESULT_ERROR_RADIO_OFF:
									System.out.println("Radio off");
								break;

								default:
									System.out.println("Result Code not found");
								break;
							}
						}
					}, new IntentFilter("SMS_SENT"));

					//---when the SMS has been delivered---
					BroadcastReceiver broadcastReceiver = new BroadcastReceiver()
					{
						@Override
						public void onReceive(Context arg0, Intent arg1)
						{
							switch(getResultCode())
							{
								case Activity.RESULT_OK:
									System.out.println("SMS delivered");
								break;

								case Activity.RESULT_CANCELED:
									System.out.println("SMS not delivered");
								break;

								default:
									System.out.println("Result Code2 not found");
								break;
							}
						}
					};

					context.registerReceiver(broadcastReceiver, new IntentFilter("SMS_DELIVERED"));
					SmsManager sms = SmsManager.getDefault();

					if(sms != null)
					{
						sms.sendTextMessage(phoneNumber, null, message, sentPI, deliveredPI);
					}

					context.unregisterReceiver(broadcastReceiver);
				}
			}
		}
		catch(Exception e)
		{
			Utils.logError(context, "IncomingSmsService:sendSMS - Exception:", e);
		}
	}
}