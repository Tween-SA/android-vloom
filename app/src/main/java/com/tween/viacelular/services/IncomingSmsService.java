package com.tween.viacelular.services;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.text.format.DateFormat;
import com.tween.viacelular.asynctask.CheckCodeAsyncTask;
import com.tween.viacelular.asynctask.ConnectApiSMSAsyncTask;
import com.tween.viacelular.data.Company;
import com.tween.viacelular.data.Country;
import com.tween.viacelular.data.User;
import com.tween.viacelular.models.Message;
import com.tween.viacelular.models.Suscription;
import com.tween.viacelular.models.SuscriptionHelper;
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
		Realm realm = null;

		try
		{
			final Bundle bundle				= intent.getExtras();
			SharedPreferences preferences	= context.getApplicationContext().getSharedPreferences(Common.KEY_PREF, Context.MODE_PRIVATE);
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
						SmsMessage currentMessage = SmsMessage.createFromPdu((byte[]) singlePdusObj, "3gpp");

						if(currentMessage == null)
						{
							currentMessage = SmsMessage.createFromPdu((byte[]) singlePdusObj, "3gpp2");
						}

						String address	= "";
						String message	= "";
						String date		= "";

						if(currentMessage != null)
						{
							address	= currentMessage.getDisplayOriginatingAddress().replace("+", "");
							message	= currentMessage.getDisplayMessageBody();

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
							realm = Realm.getDefaultInstance();
							RealmResults<Message> notifications = realm.where(Message.class).equalTo(Message.KEY_CHANNEL, address).equalTo(Common.KEY_TYPE, Message.TYPE_SMS)
																	.equalTo(Message.KEY_CREATED, Long.valueOf(date)).findAll();

							if(notifications != null)
							{
								if(notifications.size() == 0)
								{
									if(	StringUtils.isCompanyNumber(address) || message.contains(Message.SMS_CODE) || message.contains(Message.SMS_CODE_ES) || message.contains(Message.SMS_CODE_NEW) ||
										message.contains(Message.SMS_CODE_ES_NEW))
									{
										notification = new Message();
										notification.setType(Message.TYPE_SMS);
										notification.setMsg(message);
										notification.setCreated(Long.valueOf(date));
										notification.setChannel(address);
										notification.setStatus(Message.STATUS_RECEIVE);

										//Modificación para contemplar cambio en tratamiento de números cortos
										SharedPreferences.Editor editor	= preferences.edit();
										editor.putInt(Common.KEY_LAST_MSGID, preferences.getInt(Common.KEY_LAST_MSGID, 1) + 1);
										editor.apply();
										notification.setMsgId(String.valueOf(preferences.getInt(Common.KEY_LAST_MSGID, 1)));
										//Agregado para continuar numeración de msgId

										boolean coincidenceKeyword	= false;
										Suscription client			= null;
										String companyId			= "";

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
											companyId = Company.COMPANY_ID_VC_MONGO;
										}
										else
										{
											RealmResults<Suscription> companies	= SuscriptionHelper.getCompanyByNumber(address);
											Suscription phantomCompany			= realm.where(Suscription.class).equalTo(Common.KEY_NAME, address).findFirst();

											if(companies != null)
											{
												if(companies.size() > 1)
												{
													for(Suscription company: companies)
													{
														client = company;

														if(message.toUpperCase().contains(company.getName().toUpperCase()))
														{
															coincidenceKeyword = true;
															break;
														}
														else
														{
															if(StringUtils.containsKeywords(message, company.getKeywords()))
															{
																coincidenceKeyword = true;
																break;
															}
														}
													}

													if(!coincidenceKeyword)
													{
														//Buscamos si ya existe la company fantasma
														if(phantomCompany != null)
														{
															client		= phantomCompany;
															companyId	= client.getCompanyId();
														}
														else
														{
															client		= SuscriptionHelper.createPhantom(address, context, preferences.getString(Country.KEY_API, ""));
															companyId	= client.getCompanyId();
														}
													}
												}
												else
												{
													if(companies.size() == 1)
													{
														client		= companies.get(0);
														companyId	= client.getCompanyId();
													}
													else
													{
														if(phantomCompany != null)
														{
															client		= phantomCompany;
															companyId	= client.getCompanyId();
														}
														else
														{
															client		= SuscriptionHelper.createPhantom(address, context, preferences.getString(Country.KEY_API, ""));
															companyId	= client.getCompanyId();
														}
													}
												}
											}
											else
											{
												if(phantomCompany != null)
												{
													client		= phantomCompany;
													companyId	= client.getCompanyId();
												}
												else
												{
													client		= SuscriptionHelper.createPhantom(address, context, preferences.getString(Country.KEY_API, ""));
													companyId	= client.getCompanyId();
												}
											}
										}

										//Agregado para contemplar país y celular del usuario al recibir un sms
										notification.setCountryCode(preferences.getString(Country.KEY_API, ""));
										notification.setPhone(preferences.getString(User.KEY_PHONE, ""));
										notification.setCompanyId(companyId);
										notification.setFlags(Message.FLAGS_SMS);

										realm.beginTransaction();
										realm.copyToRealmOrUpdate(notification);
										realm.commitTransaction();

										//Agregado para mostrar notificación sin sonido
										Utils.showPush(context, preferences.getString(User.KEY_PHONE, ""), Common.BOOL_YES, notification);
									}
								}
							}
						}
					}

					if(StringUtils.isNotEmpty(code))
					{
						if(StringUtils.isValidCode(code))
						{
							final CheckCodeAsyncTask task = new CheckCodeAsyncTask(context, code, false);
							task.execute();
						}
					}
					else
					{
						//Agregado para enviar a la api sms que acaba de llegar
						if(notification != null)
						{
							final ConnectApiSMSAsyncTask task	= new ConnectApiSMSAsyncTask(context, false);
							task.setMessage(notification);
							task.execute();
						}
					}
				}
			}
		}
		catch(Exception e)
		{
			System.out.println("IncomingSmsService:onReceive - Exception: " + e);

			if(Common.DEBUG)
			{
				e.printStackTrace();
			}
		}
	}

	/**
	 * Agregado para enviar sms de baja al bloquear Company
	 */
	public static void sendSMS(Context context, String phoneNumber, String message)
	{
		try
		{
			PendingIntent sentPI		= PendingIntent.getBroadcast(context, 0, new Intent("SMS_SENT"), 0);
			PendingIntent deliveredPI	= PendingIntent.getBroadcast(context, 0, new Intent("SMS_DELIVERED"), 0);

			//---when the SMS has been sent---
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
					}
				}
			};

			context.registerReceiver(broadcastReceiver, new IntentFilter("SMS_DELIVERED"));
			SmsManager sms = SmsManager.getDefault();
			sms.sendTextMessage(phoneNumber, null, message, sentPI, deliveredPI);
			context.unregisterReceiver(broadcastReceiver);
		}
		catch(Exception e)
		{
			System.out.println("IncomingSmsService:sendSMS - Exception: " + e);

			if(Common.DEBUG)
			{
				e.printStackTrace();
			}
		}
	}
}