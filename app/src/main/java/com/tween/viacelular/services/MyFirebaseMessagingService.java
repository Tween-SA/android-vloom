package com.tween.viacelular.services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.tween.viacelular.R;
import com.tween.viacelular.activities.BlockedActivity;
import com.tween.viacelular.activities.CardViewActivity;
import com.tween.viacelular.asynctask.CompanyAsyncTask;
import com.tween.viacelular.asynctask.ConfirmReadingAsyncTask;
import com.tween.viacelular.asynctask.LogoAsyncTask;
import com.tween.viacelular.models.Land;
import com.tween.viacelular.models.MessageHelper;
import com.tween.viacelular.models.User;
import com.tween.viacelular.models.Message;
import com.tween.viacelular.models.Migration;
import com.tween.viacelular.models.Suscription;
import com.tween.viacelular.utils.Common;
import com.tween.viacelular.utils.StringUtils;

import org.json.JSONArray;

import java.util.Map;
import io.realm.Realm;

/**
 * Created by davidfigueroa on 1/8/16.
 */
public class MyFirebaseMessagingService extends FirebaseMessagingService
{
	public static final int		PLAY_SERVICES_RESOLUTION_REQUEST	= 8000;
	public static final int		PUSH_NORMAL							= 0;
	public static final int		PUSH_WITHOUT_SOUND					= 1;
	public static final int		PUSH_HIDDEN							= 2; //Preparar para recibir comandos invisibles para desencadenar alguna acción
	public static final String	SENT_TOKEN_TO_SERVER				= "sentTokenToServer";
	public static final String	REGISTRATION_COMPLETE				= "registrationComplete";
	private Context				context;

	@Override
	public void onMessageReceived(RemoteMessage remoteMessage)
	{
		try
		{
			if(remoteMessage != null)
			{
				context		= getApplicationContext();
				Migration.getDB(context);
				// Handle data payload of FCM messages.
				String from	= remoteMessage.getFrom();
				Map data	= remoteMessage.getData();

				if(Common.DEBUG)
				{
					System.out.println("FCM Id: "+remoteMessage.getMessageId());
					System.out.println("FCM FROM: "+from);

					if(remoteMessage.getNotification() != null)
					{
						System.out.println("FCM Notification Body: "+remoteMessage.getNotification().getBody());
						System.out.println("FCM Notification LocalizationKey: "+remoteMessage.getNotification().getBodyLocalizationKey());
						System.out.println("FCM Notification ClickAction: "+remoteMessage.getNotification().getClickAction());
						System.out.println("FCM Notification Color: "+remoteMessage.getNotification().getColor());
						System.out.println("FCM Notification Icon: "+remoteMessage.getNotification().getIcon());
						System.out.println("FCM Notification Sound: "+remoteMessage.getNotification().getSound());
						System.out.println("FCM Notification Tag: "+remoteMessage.getNotification().getTag());
						System.out.println("FCM Notification Title: "+remoteMessage.getNotification().getTitle());
						System.out.println("FCM Notification TitleLocalizationKey: "+remoteMessage.getNotification().getTitleLocalizationKey());
					}

					System.out.println("FCM Data: "+remoteMessage.getData());
				}

				if(data != null)
				{
					Bundle push = new Bundle();

					//Leyendo desde push
					if(data.get(Message.KEY_API) != null)
					{
						push.putString(Message.KEY_API, data.get(Message.KEY_API).toString());
					}

					if(data.get(Common.KEY_TYPE) != null)
					{
						if(StringUtils.isEmpty(data.get(Common.KEY_TYPE).toString()))
						{
							if(StringUtils.isNotEmpty(remoteMessage.getNotification().getTitle()))
							{
								push.putString(Common.KEY_TYPE, remoteMessage.getNotification().getTitle());
							}
							else
							{
								push.putString(Common.KEY_TYPE, context.getString(R.string.notification));
							}
						}
						else
						{
							push.putString(Common.KEY_TYPE, data.get(Common.KEY_TYPE).toString());
						}
					}
					else
					{
						if(StringUtils.isNotEmpty(remoteMessage.getNotification().getTitle()))
						{
							push.putString(Common.KEY_TYPE, remoteMessage.getNotification().getTitle());
						}
						else
						{
							push.putString(Common.KEY_TYPE, context.getString(R.string.notification));
						}
					}

					if(data.get(Message.KEY_PLAYLOAD) != null)
					{
						if(StringUtils.isEmpty(data.get(Message.KEY_PLAYLOAD).toString()))
						{
							if(StringUtils.isNotEmpty(remoteMessage.getNotification().getBody()))
							{
								push.putString(Message.KEY_PLAYLOAD, remoteMessage.getNotification().getBody());
							}
							else
							{
								push.putString(Message.KEY_PLAYLOAD, context.getString(R.string.notification_new));
							}
						}
						else
						{
							push.putString(Message.KEY_PLAYLOAD, data.get(Message.KEY_PLAYLOAD).toString());
						}
					}
					else
					{
						if(StringUtils.isNotEmpty(remoteMessage.getNotification().getBody()))
						{
							push.putString(Message.KEY_PLAYLOAD, remoteMessage.getNotification().getBody());
						}
						else
						{
							push.putString(Message.KEY_PLAYLOAD, context.getString(R.string.notification_new));
						}
					}

					if(data.get(Message.KEY_CHANNEL) != null)
					{
						push.putString(Message.KEY_CHANNEL, data.get(Message.KEY_CHANNEL).toString());
					}

					if(data.get(Suscription.KEY_API) != null)
					{
						push.putString(Suscription.KEY_API, data.get(Suscription.KEY_API).toString());
					}

					if(data.get(User.KEY_PHONE) != null)
					{
						push.putString(User.KEY_PHONE, data.get(User.KEY_PHONE).toString());
					}
					else
					{
						push.putString(User.KEY_PHONE, from);
					}

					if(data.get(Land.KEY_API) != null)
					{
						push.putString(Land.KEY_API, data.get(Land.KEY_API).toString());
					}

					if(data.get(Message.KEY_FLAGS) != null)
					{
						push.putString(Message.KEY_FLAGS, data.get(Message.KEY_FLAGS).toString());
					}

					if(data.get(Common.KEY_SOUND) != null)
					{
						push.putString(Common.KEY_SOUND, data.get(Common.KEY_SOUND).toString());
					}

					//TODO rever si no llega a venir como JSONArray
					if(data.get(Message.KEY_ATTACHMENTS) != null)
					{
						JSONArray attachments = new JSONArray(data.get(Message.KEY_ATTACHMENTS).toString());

						if(attachments.length() > 0)
						{
							if(attachments.getJSONObject(0) != null)
							{
								if(attachments.getJSONObject(0).has(Common.KEY_TYPE))
								{
									if(StringUtils.isNotEmpty(attachments.getJSONObject(0).getString(Common.KEY_TYPE)))
									{
										push.putInt(Message.KEY_KIND, attachments.getJSONObject(0).getInt(Common.KEY_TYPE));
									}
								}

								if(attachments.getJSONObject(0).has(Message.KEY_LINK))
								{
									if(StringUtils.isNotEmpty(attachments.getJSONObject(0).getString(Message.KEY_LINK)))
									{
										push.putString(Message.KEY_LINK, attachments.getJSONObject(0).getString(Message.KEY_LINK));
									}
								}

								if(attachments.getJSONObject(0).has(Message.KEY_LINKTHUMB))
								{
									if(StringUtils.isNotEmpty(attachments.getJSONObject(0).getString(Message.KEY_LINKTHUMB)))
									{
										push.putString(Message.KEY_LINKTHUMB, attachments.getJSONObject(0).getString(Message.KEY_LINKTHUMB));
									}
								}

								if(attachments.getJSONObject(0).has(Message.KEY_SUBMSG))
								{
									if(StringUtils.isNotEmpty(attachments.getJSONObject(0).getString(Message.KEY_SUBMSG)))
									{
										push.putString(Message.KEY_SUBMSG, attachments.getJSONObject(0).getString(Message.KEY_SUBMSG));
									}
								}
							}
						}
					}
					else
					{
						if(data.get(Message.KEY_KIND) != null)
						{
							push.putInt(Message.KEY_KIND, (int) data.get(Message.KEY_KIND));
						}

						if(data.get(Message.KEY_LINK) != null)
						{
							push.putString(Message.KEY_LINK, data.get(Message.KEY_LINK).toString());
						}

						if(data.get(Message.KEY_LINKTHUMB) != null)
						{
							push.putString(Message.KEY_LINKTHUMB, data.get(Message.KEY_LINKTHUMB).toString());
						}

						if(data.get(Message.KEY_SUBMSG) != null)
						{
							push.putString(Message.KEY_SUBMSG, data.get(Message.KEY_SUBMSG).toString());
						}
					}

					if(data.get(Message.KEY_CAMPAIGNID) != null)
					{
						push.putString(Message.KEY_CAMPAIGNID, data.get(Message.KEY_CAMPAIGNID).toString());
					}

					if(data.get(Message.KEY_LISTID) != null)
					{
						push.putString(Message.KEY_LISTID, data.get(Message.KEY_LISTID).toString());
					}

					if(data.get(Message.KEY_TIMESTAMP) != null)
					{
						push.putString(Message.KEY_TIMESTAMP, data.get(Message.KEY_TIMESTAMP).toString());
					}

					MessageHelper.savePush(push, context, from, false);
				}
			}
		}
		catch(Exception e)
		{
			System.out.println("MyFirebaseMessagingService:onMessageReceived - Exception: "+e);

			if(Common.DEBUG)
			{
				e.printStackTrace();
			}
		}
	}

	/**
	 * Create and show a simple notification containing the received GCM message.
	 *
	 * @param msgId Id form GCM message received.
	 * @param preferences
	 * @param sound
	 * @param from
	 */
	public void sendNotification(String msgId, SharedPreferences preferences, int sound, int from, String countryCode, String msg, Context context)
	{
		try
		{
			boolean silenced	= preferences.getBoolean(Suscription.KEY_SILENCED, false);
			int silencedChannel	= Common.BOOL_YES;
			int blocked			= Common.BOOL_YES;
			int statusP			= Suscription.STATUS_BLOCKED;
			String title		= context.getString(R.string.app_name);
			Suscription clientP;
			String image		= "";
			Bitmap bmp			= BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher);
			Realm realm			= Realm.getDefaultInstance();
			Message message		= realm.where(Message.class).equalTo(Message.KEY_API, msgId).findFirst();
			String companyIdApi;
			String contentText;
			boolean newClient	= false;

			if(message != null)
			{
				contentText		= message.getMsg();
				companyIdApi	= message.getCompanyId();
			}
			else
			{
				contentText		= context.getString(R.string.notification_new);
				companyIdApi	= msgId;
			}

			if(StringUtils.isNotEmpty(msg))
			{
				contentText = msg;
			}

			//Modificación para contemplar companiesPhantom
			if(sound == PUSH_NORMAL)
			{
				clientP = realm.where(Suscription.class).equalTo(Suscription.KEY_API, companyIdApi).findFirst();
			}
			else
			{
				//Agregado para contemplar notificación por sms
				clientP = realm.where(Suscription.class).equalTo(Suscription.KEY_API, msgId).findFirst();
			}

			if(clientP == null && StringUtils.isIdMongo(companyIdApi))
			{
				newClient = true;

				try
				{
					CompanyAsyncTask task	= new CompanyAsyncTask(context, false, companyIdApi, countryCode);
					task.setFlag(Common.BOOL_YES);
					task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
				}
				catch(Exception e)
				{
					System.out.println("MyFirebaseMessagingService:sendNotification:getCompanyByApi - Exception: " + e);
				}
			}

			//Se vuelve a consultar el objeto para tomar el devuelto por la api
			if(StringUtils.isIdMongo(companyIdApi))
			{
				clientP = realm.where(Suscription.class).equalTo(Suscription.KEY_API, companyIdApi).findFirst();
			}

			if(clientP != null)
			{
				if(StringUtils.isNotEmpty(clientP.getName()))
				{
					title = clientP.getName();
				}

				silencedChannel	= clientP.getSilenced();
				blocked			= clientP.getBlocked();
				statusP			= clientP.getStatus();
			}

			//Rollback solamente se restringe la recepción de push si el usuario expresamente puso que no, caso contrario la recibe y suscribe
			if(blocked == Common.BOOL_NO && statusP != Suscription.STATUS_BLOCKED)
			{
				if(sound == PUSH_NORMAL)
				{
					if(message != null)
					{
						realm.beginTransaction();
						realm.copyToRealmOrUpdate(message);
						realm.commitTransaction();
					}
				}
				else
				{
					if(sound == PUSH_HIDDEN)
					{
						//TODO en algún momento se prepararán comandos para forzar a la app a realizar algún procedimiento
						System.out.println("ejecutar comando a definir");
					}
				}

				if(!silenced && silencedChannel == Common.BOOL_NO)
				{
					Intent intent = new Intent(context, CardViewActivity.class);

					if(StringUtils.isIdMongo(clientP.getCompanyId()))
					{
						intent.putExtra(Common.KEY_ID, clientP.getCompanyId());
						image = clientP.getImage();
						//Rollback para autoañadir companies si no está añadida
						if(!newClient && clientP.getFollower() == Common.BOOL_NO && clientP.getBlocked() == Common.BOOL_NO)
						{
							BlockedActivity.modifySubscriptions(context, Common.BOOL_YES, false, clientP.getCompanyId(), false);
						}
					}
					else
					{
						intent.putExtra(Common.KEY_ID, "");
					}

					intent.putExtra(Common.KEY_LAST_MSGID, msgId);
					intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					PendingIntent pendingIntent						= PendingIntent.getActivity(context, from, intent, PendingIntent.FLAG_ONE_SHOT);
					NotificationCompat.Builder notificationBuilder;

					if(clientP.getCompanyId().equals(Suscription.COMPANY_ID_VC_MONGO))
					{
						notificationBuilder = new NotificationCompat.Builder(context).setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher));
					}
					else
					{
						if(StringUtils.isNotEmpty(image))
						{
							try
							{
								//Modificación para delay en la descarga del logo de la Company
								LogoAsyncTask task	= new LogoAsyncTask(context, false, image, context.getResources().getDisplayMetrics().density);
								//TODO implementar callbacks para prevenir la suspención del UI por delay en la Asynctask
								bmp					= task.execute().get();
							}
							catch(Exception e)
							{
								System.out.println("MyFirebaseMessagingService:sendNotification:getImageWithPicasso - Exception: " + e);
							}
						}

						notificationBuilder	= new NotificationCompat.Builder(context).setLargeIcon(bmp);
					}

					notificationBuilder
							.setSmallIcon(R.drawable.vc)
							.setContentTitle(title)
							.setContentText(contentText)
							.setAutoCancel(true)
							.setContentIntent(pendingIntent);

					if(Common.API_LEVEL >= Build.VERSION_CODES.LOLLIPOP)
					{
						notificationBuilder.setCategory(Notification.CATEGORY_MESSAGE)
								.setVisibility(Notification.VISIBILITY_PUBLIC);
					}

					if(Common.API_LEVEL >= Build.VERSION_CODES.JELLY_BEAN)
					{
						notificationBuilder.setPriority(Notification.PRIORITY_HIGH);
					}

					if(sound == PUSH_NORMAL)
					{
						Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
						notificationBuilder.setSound(defaultSoundUri);
					}

					//TODO: Revisar qué funcionalidad extra brinda: .setStyle(new NotificationCompat.BigTextStyle().bigText(msg))

					NotificationManager notificationManager	= (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
					notificationManager.notify(from, notificationBuilder.build());
					SharedPreferences.Editor editor			= preferences.edit();
					editor.putInt(Common.KEY_LAST_MSGID, from + 1);
					editor.apply();
				}
				else
				{
					//Agregado para preparga de logo
					if(StringUtils.isNotEmpty(image))
					{
						try
						{
							new LogoAsyncTask(context, false, image, context.getResources().getDisplayMetrics().density).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
							//TODO implementar callbacks para prevenir la suspención del UI por delay en la Asynctask
						}
						catch(Exception e)
						{
							System.out.println("MyFirebaseMessagingService:sendNotification:getImageWithPicasso - Exception: " + e);
						}
					}
				}

				//Primero mostramos la notificación y después confirmamos lectura y reportamos posición
				if(StringUtils.isIdMongo(msgId) && sound == PUSH_NORMAL)
				{
					try
					{
						new ConfirmReadingAsyncTask(context, false, "", msgId, Message.STATUS_RECEIVE).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
					}
					catch(Exception e)
					{
						System.out.println("MyFirebaseMessagingService:sendNotification:ConfirmReading - Exception: " + e);
					}
				}
				//Reload Home if it's running
			}
			else
			{
				if(sound == PUSH_NORMAL && message != null)
				{
					String id = message.getMsgId();
					//Agregado para no mostrar mensajes descartados por bloqueo
					realm.beginTransaction();
					message.setStatus(Message.STATUS_SPAM);
					realm.commitTransaction();

					//Agregado para notificar como spam al ser descartado
					GoogleAnalytics.getInstance(this).newTracker(Common.HASH_GOOGLEANALYTICS).send(	new HitBuilders.EventBuilder().setCategory("Mensajes").setAction("Marcarspam")
							.setLabel("Accion_user").build());
					new ConfirmReadingAsyncTask(context, false, "", id, Message.STATUS_SPAM).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
				}
			}
		}
		catch(Exception e)
		{
			System.out.println("MyFirebaseMessagingService:sendNotification - Exception: " + e);

			if(Common.DEBUG)
			{
				e.printStackTrace();
			}
		}
	}

	/**
	 * Called when message is received.
	 *
	 * @param from SenderID of the sender.
	 * @param data Data bundle containing message data as key/value pairs. For Set of keys use data.keySet().
	 */
	public void onOldPush(String from, Bundle data)
	{
		try
		{
			if(data != null)
			{
				if(context == null)
				{
					context = getApplicationContext();
				}

				MessageHelper.savePush(data, context, from, false);
				//Se agrupo el código para procesar la push dentro de MessageHelper
			}
		}
		catch(Exception e)
		{
			System.out.println("MyFirebaseMessagingService:onOldPush - Exception: " + e);

			if(Common.DEBUG)
			{
				e.printStackTrace();
			}
		}
	}

	public Context getContext()
	{
		return context;
	}

	public void setContext(Context context)
	{
		this.context = context;
	}
}
