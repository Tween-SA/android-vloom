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
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.gcm.GcmListenerService;
import com.tween.viacelular.R;
import com.tween.viacelular.activities.BlockedActivity;
import com.tween.viacelular.activities.CardViewActivity;
import com.tween.viacelular.asynctask.CompanyAsyncTask;
import com.tween.viacelular.asynctask.ConfirmReadingAsyncTask;
import com.tween.viacelular.asynctask.LogoAsyncTask;
import com.tween.viacelular.data.Country;
import com.tween.viacelular.data.User;
import com.tween.viacelular.models.Message;
import com.tween.viacelular.models.Suscription;
import com.tween.viacelular.utils.Common;
import com.tween.viacelular.utils.StringUtils;
import io.realm.Realm;

/**
 * Created by david.figueroa on 16/6/15.
 */
public class MyGcmListenerService extends GcmListenerService
{
	public static final int		PLAY_SERVICES_RESOLUTION_REQUEST	= 8000;
	public static final int		PUSH_NORMAL							= 0;
	public static final int		PUSH_WITHOUT_SOUND					= 1;
	public static final int		PUSH_HIDDEN							= 2; //Preparar para recibir comandos invisibles para desencadenar alguna acción
	public static final String	SENT_TOKEN_TO_SERVER				= "sentTokenToServer";
	public static final String	REGISTRATION_COMPLETE				= "registrationComplete";
	private Context				context;
	private Bitmap				bmp;
	private String				image;

	/**
	 * Called when message is received.
	 *
	 * @param from SenderID of the sender.
	 * @param data Data bundle containing message data as key/value pairs. For Set of keys use data.keySet().
	 */
	@Override
	public void onMessageReceived(String from, Bundle data)
	{
		try
		{
			if(data != null)
			{
				if(context == null)
				{
					context = getApplicationContext();
				}

				String msgId					= data.getString(Message.KEY_API, "");
				String msgType					= data.getString(Common.KEY_TYPE, "");
				String msg						= data.getString(Message.KEY_PLAYLOAD, "");
				String channel					= data.getString(Message.KEY_CHANNEL, "");
				String companyId				= data.getString(Suscription.KEY_API, "");
				String phone					= data.getString(User.KEY_PHONE, "");
				String countryCode				= data.getString(Country.KEY_API, "");
				String flags					= data.getString(Message.KEY_FLAGS, "");
				SharedPreferences preferences	= context.getSharedPreferences(Common.KEY_PREF, Context.MODE_PRIVATE);
				String sound					= data.getString(Common.KEY_SOUND, "0");
				int notificationId				= preferences.getInt(Common.KEY_LAST_MSGID, 0);
				int soundOn						= 0; //Agregado para silenciar la push de sms
				//Agregado para contemplar campos nuevos de push
				int kind						= data.getInt(Message.KEY_KIND, Message.KIND_TEXT);
				String link						= data.getString(Message.KEY_LINK, "");
				String linkThumb				= data.getString(Message.KEY_LINKTHUMB, "");
				String subMsg					= data.getString(Message.KEY_SUBMSG, "");
				String campaignId				= data.getString(Message.KEY_CAMPAIGNID, "");
				String listId					= data.getString(Message.KEY_LISTID, "");
				String created					= data.getString(Message.KEY_TIMESTAMP, String.valueOf(System.currentTimeMillis()));

				//Agregado para contemplar campo sound de iOS
				if(StringUtils.isNumber(sound))
				{
					soundOn = Integer.valueOf(sound);
				}

				if(Common.DEBUG)
				{
					System.out.println("Bundle: "+data.toString());
					System.out.println("From: " + from);
				}

				if(StringUtils.isEmpty(msgId))
				{
					msgId = String.valueOf(notificationId + 1);
				}

				if(StringUtils.isEmpty(msgType))
				{
					//Agregado para tomar el campo type desde Appboy
					/**
					 * Estructura de mensajes Appboy
					 * a -> Cuerpo
					 * p -> TTL
					 * t -> Título
					 * _ab
					 * cid
					 * collapse_key Tipo de mensaje. Por ejemplo: campaign
					 */
					if(StringUtils.isNotEmpty(data.getString("t", "")))
					{
						if(data.getString("t", "").equals(Common.VALUE_FEEDBACKAPPBOY))
						{
							msgType = context.getString(R.string.feedback_typepush);
						}
						else
						{
							//Agregado para contemplar title de campañas Appboy
							msgType = data.getString("t", "");
						}
					}
					else
					{
						//Agregado para contemplar key estándar de título para la push
						if(StringUtils.isNotEmpty(data.getString("title", "")))
						{
							msgType = data.getString("title", "");
						}
						else
						{
							msgType = context.getString(R.string.notification);
						}
					}
				}

				if(StringUtils.isEmpty(msg))
				{
					//Agregado para tomar el campo msg desde Appboy
					if(StringUtils.isNotEmpty(data.getString("a", "")))
					{
						msg = data.getString("a", "");
					}
					else
					{
						//Agregado para contemplar key estándar de mensaje
						if(StringUtils.isNotEmpty(data.getString("message", "")))
						{
							msg = data.getString("message", "");
						}
						else
						{
							msg = context.getString(R.string.notification_new);
						}
					}
				}

				if(StringUtils.isEmpty(channel))
				{
					channel = context.getString(R.string.app_name);
				}

				if(StringUtils.isEmpty(companyId))
				{
					companyId = Suscription.COMPANY_ID_VC_MONGO;
				}
				else
				{
					if(	companyId.equals(Suscription.COMPANY_ID_VC) || companyId.equals(Suscription.COMPANY_ID_VC_LONG) || companyId.equals(Suscription.COMPANY_ID_VC_MONGOOLD) ||
						companyId.equals(Suscription.COMPANY_ID_WEBVC))
					{
						companyId = Suscription.COMPANY_ID_VC_MONGO;
					}
				}

				if(StringUtils.isEmpty(countryCode))
				{
					countryCode = preferences.getString(Country.KEY_API, "");
				}

				if(StringUtils.isEmpty(phone))
				{
					//Agregado para no dejar vacío este campo
					if(StringUtils.isNotEmpty(from))
					{
						phone = from;
					}
					else
					{
						phone = preferences.getString(User.KEY_PHONE, "");
					}
				}

				if(StringUtils.isEmpty(flags))
				{
					flags = Message.FLAGS_PUSH;
				}

				//Agregado para contemplar key estándar de imagen
				if(StringUtils.isEmpty(link) && kind == Message.KIND_TEXT)
				{
					if(StringUtils.isNotEmpty(data.getString("image", "")))
					{
						link	= data.getString("image", "");
						kind	= Message.KIND_IMAGE;
					}
					else
					{
						if(StringUtils.isNotEmpty(data.getString("img", "")))
						{
							link	= data.getString("img", "");
							kind	= Message.KIND_IMAGE;
						}
					}
				}
				else
				{
					//Se envía link pero no se especifica si es imagen, audio o vídeo por ende lo interpretamos como archivo común
					if(StringUtils.isNotEmpty(link) && kind == Message.KIND_TEXT)
					{
						kind	= Message.KIND_FILE_DOWNLOADABLE;
					}
				}

				if(StringUtils.isNotEmpty(link) && StringUtils.isEmpty(linkThumb))
				{
					linkThumb	= link;
				}

				Long time = System.currentTimeMillis();

				if(StringUtils.isLong(created))
				{
					time = Long.valueOf(created);

					if(time < System.currentTimeMillis())
					{
						time = System.currentTimeMillis();
					}
				}

				//TODO: Agregar autodetección de tipo de mensaje por extensión de url

				//Creamos el objeto inicial en Realm
				Message message = new Message();
				message.setMsgId(msgId);
				message.setType(msgType);
				message.setMsg(msg);
				message.setChannel(channel);
				message.setStatus(Message.STATUS_RECEIVE);
				message.setCountryCode(countryCode);
				message.setFlags(flags);
				message.setCreated(time);//Se modificó para tomar el ts de la push siempre que no sea más viejo que el actual del device
				message.setDeleted(Common.BOOL_NO);
				message.setKind(kind);
				message.setLink(link);
				message.setLinkThumbnail(linkThumb);
				message.setSubMsg(subMsg);
				message.setCampaignId(campaignId);
				message.setListId(listId);
				message.setCompanyId(companyId);
				message.setPhone(phone);

				if(soundOn == PUSH_NORMAL)
				{
					Realm realm = Realm.getDefaultInstance();
					realm.beginTransaction();
					realm.copyToRealmOrUpdate(message);
					realm.commitTransaction();
				}
				else
				{
					msgId = companyId;
				}

				/**
				 * Production applications would usually process the message here.
				 * Eg: - Syncing with server.
				 *     - Store message in local database.
				 *     - Update UI.
				 */

				/**
				 * In some cases it may be useful to show a notification indicating to the user that a message was received.
				 */
				sendNotification(msgId, preferences, soundOn, notificationId, countryCode, msg);
			}
		}
		catch(Exception e)
		{
			System.out.println("MyGcmListenerService:onMessageReceived - Exception: " + e);

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
	private void sendNotification(String msgId, SharedPreferences preferences, int sound, int from, String countryCode, String msg)
	{
		try
		{
			boolean silenced	= preferences.getBoolean(Suscription.KEY_SILENCED, false);
			int silencedChannel	= Common.BOOL_YES;
			int blocked			= Common.BOOL_YES;
			int statusP			= Suscription.STATUS_BLOCKED;
			String title		= context.getString(R.string.app_name);
			Suscription clientP	= null;
			image				= "";
			bmp					= BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher);
			Realm realm			= Realm.getDefaultInstance();
			Message message		= realm.where(Message.class).equalTo(Message.KEY_API, msgId).findFirst();
			String companyIdApi	= "";
			String contentText	= "";
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
					task.execute();
				}
				catch(Exception e)
				{
					System.out.println("MyGcmListenerService:sendNotification:getCompanyByApi - Exception: " + e);
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
					}
				}

				if(!silenced && silencedChannel == Common.BOOL_NO)
				{
					Intent intent = new Intent(context, CardViewActivity.class);

					if(clientP != null)
					{
						if(StringUtils.isIdMongo(clientP.getCompanyId()))
						{
							intent.putExtra(Common.KEY_ID, clientP.getCompanyId());
							image = clientP.getImage();
							//Rollback para autoañadir companies si no está añadida
							if(!newClient && clientP.getFollower() == Common.BOOL_NO && clientP.getBlocked() == Common.BOOL_NO)
							{
								BlockedActivity.modifySubscriptions(context, Common.BOOL_YES, false, clientP.getCompanyId());
							}
						}
						else
						{
							intent.putExtra(Common.KEY_ID, "");
						}
					}

					intent.putExtra(Common.KEY_LAST_MSGID, msgId);
					intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					PendingIntent pendingIntent						= PendingIntent.getActivity(context, from, intent, PendingIntent.FLAG_ONE_SHOT);
					NotificationCompat.Builder notificationBuilder	= null;

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
								System.out.println("MyGcmListenerService:sendNotification:getImageWithPicasso - Exception: " + e);
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
							LogoAsyncTask task	= new LogoAsyncTask(context, false, image, context.getResources().getDisplayMetrics().density);
							//TODO implementar callbacks para prevenir la suspención del UI por delay en la Asynctask
							bmp					= task.execute().get();
						}
						catch(Exception e)
						{
							System.out.println("MyGcmListenerService:sendNotification:getImageWithPicasso - Exception: " + e);
						}
					}
				}

				//Primero mostramos la notificación y después confirmamos lectura y reportamos posición
				if(StringUtils.isIdMongo(msgId) && sound == PUSH_NORMAL)
				{
					try
					{
						ConfirmReadingAsyncTask task = new ConfirmReadingAsyncTask(context, false, "", msgId, Message.STATUS_RECEIVE);
						task.execute();
					}
					catch(Exception e)
					{
						System.out.println("MyGcmListenerService:sendNotification:ConfirmReading - Exception: " + e);
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
					ConfirmReadingAsyncTask task	= new ConfirmReadingAsyncTask(getApplicationContext(), false, "", id, Message.STATUS_SPAM);
					task.execute();
				}
			}
		}
		catch(Exception e)
		{
			System.out.println("MyGcmListenerService:sendNotification - Exception: " + e);

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