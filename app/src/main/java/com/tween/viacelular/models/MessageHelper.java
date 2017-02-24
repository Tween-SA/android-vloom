package com.tween.viacelular.models;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Looper;
import com.tween.viacelular.R;
import com.tween.viacelular.activities.CardViewActivity;
import com.tween.viacelular.data.Country;
import com.tween.viacelular.services.MyFirebaseMessagingService;
import com.tween.viacelular.utils.Common;
import com.tween.viacelular.utils.StringUtils;
import com.tween.viacelular.utils.Utils;
import io.realm.Realm;
import io.realm.RealmResults;

/**
 * Created by davidfigueroa on 21/3/16.
 */
public abstract class MessageHelper
{
	/**
	 * Imprime los valores del objeto Realm Message
	 * @param message
	 */
	public static void debugMessage(Message message)
	{
		if(message != null)
		{
			System.out.println("\nMessage - msgId: " + message.getMsgId());
			System.out.println("Message - type: " + message.getType());
			System.out.println("Message - msg: " + message.getMsg());
			System.out.println("Message - channel: " + message.getChannel());
			System.out.println("Message - status: " + message.getStatus());
			System.out.println("Message - phone: " + message.getPhone());
			System.out.println("Message - countryCode: " + message.getCountryCode());
			System.out.println("Message - flags: " + message.getFlags());
			System.out.println("Message - created: " + message.getCreated());
			System.out.println("Message - deleted: " + message.getDeleted());
			System.out.println("Message - kind: " + message.getKind());
			System.out.println("Message - link: " + message.getLink());
			System.out.println("Message - linkThumbnail: " + message.getLinkThumbnail());
			System.out.println("Message - subMsg: " + message.getSubMsg());
			System.out.println("Message - campaignId: " + message.getCampaignId());
			System.out.println("Message - listId: " + message.getListId());
			System.out.println("Message - companyId: " + message.getCompanyId());
			System.out.println("Message - socialId: " + message.getSocialId());
			System.out.println("Message - socialDate: " + message.getSocialDate());
			System.out.println("Message - socialLikes: " + message.getSocialLikes());
			System.out.println("Message - socialShares: " + message.getSocialShares());
			System.out.println("Message - socialAccount: " + message.getSocialAccount());
			System.out.println("Message - socialName: " + message.getSocialName());
			System.out.println("Message - txid: " + message.getTxid());
			System.out.println("Message - note: " + message.getNote());
			System.out.println("Message - attached: " + message.getAttached());
			System.out.println("Message - attachedTwo: " + message.getAttachedTwo());
			System.out.println("Message - attachedThree: " + message.getAttachedThree());
			System.out.println("Message - uri: " + message.getUri());
			System.out.println("Message - uriTwo: " + message.getUriTwo());
			System.out.println("Message - uriThree: " + message.getUriThree());
		}
		else
		{
			System.out.println("\nMessage - is null");
		}
	}

	/**
	 * Genera un nuevo objeto de Mensaje para representar una nota propia del usuario
	 * @param note
	 * @param companyId
	 * @param context
	 * @return Message
	 */
	public static Message getNewNote(String note, String companyId, Context context)
	{
		SharedPreferences preferences	= context.getSharedPreferences(Common.KEY_PREF, Context.MODE_PRIVATE);
		int notificationId				= preferences.getInt(Common.KEY_LAST_MSGID, 0);
		notificationId					= notificationId+1;
		SharedPreferences.Editor editor	= preferences.edit();
		editor.putInt(Common.KEY_LAST_MSGID, notificationId);
		editor.apply();
		return new Message(	String.valueOf(notificationId), context.getString(R.string.enrich_notetype), note, "", Message.STATUS_READ, preferences.getString(User.KEY_API, ""),
							preferences.getString(Land.KEY_API, ""), Message.FLAGS_PUSHCAP, System.currentTimeMillis(), Common.BOOL_NO, Message.KIND_NOTE, "", "", "", "", "", companyId);
	}

	/**
	 * Marca todos los mensajes de una Suscription como eliminados. (Vacía la Company)
	 * @param companyId
	 * @param flag
	 */
	public static void emptyCompany(String companyId, int flag, Context context)
	{
		class DeleteMessages extends Thread
		{
			private String	companyId;
			private int		flag;
			private Context context;

			private DeleteMessages(String companyId, int flag, Context context)
			{
				this.companyId	= companyId;
				this.flag		= flag;
				this.context	= context;
			}

			public void start()
			{
				//Agregado para evitar excepciones Runtime
				if(Looper.myLooper() == null)
				{
					Looper.prepare();
				}

				try
				{
					Realm realm	= Realm.getDefaultInstance();
					realm.executeTransaction(new Realm.Transaction()
					{
						@Override
						public void execute(Realm bgRealm)
						{
							RealmResults<Message> results = bgRealm.where(Message.class).equalTo(Suscription.KEY_API, companyId).findAll();

							for(int i = results.size() -1; i >=0; i--)
							{
								results.get(i).setDeleted(flag);
							}
						}
					});
				}
				catch(Exception e)
				{
					Utils.logError(context, "MessageHelper:DeleteMessages:start - Exception:", e);
				}
			}
		}

		DeleteMessages task = new DeleteMessages(companyId, flag, context);
		task.start();
	}

	/**
	 * Reagrupa mensajes bajo una Suscription (Company)
	 * @param companyId
	 * @param newCompanyId
	 */
	public static boolean groupMessages(String companyId, String newCompanyId, Context context)
	{
		class GroupMessages extends Thread
		{
			private String	companyId;
			private String	newCompanyId;
			private boolean	modify;
			private Context context;

			private GroupMessages(String companyId, String newCompanyId, Context context)
			{
				this.companyId		= companyId;
				this.newCompanyId	= newCompanyId;
				this.context		= context;
			}

			public void start()
			{
				//Agregado para evitar excepciones Runtime
				if(Looper.myLooper() == null)
				{
					Looper.prepare();
				}

				try
				{
					Realm realm	= Realm.getDefaultInstance();
					realm.executeTransaction(new Realm.Transaction()
					{
						@Override
						public void execute(Realm bgRealm)
						{
							RealmResults<Message> results = bgRealm.where(Message.class).equalTo(Suscription.KEY_API, companyId).findAll();

							if(results.size() > 0)
							{
								for(int i = results.size() -1; i >=0; i--)
								{
									results.get(i).setCompanyId(newCompanyId);
								}

								setModify(true);
							}
							else
							{
								setModify(false);
							}
						}
					});
				}
				catch(Exception e)
				{
					Utils.logError(context, "MessageHelper:groupMessages:start - Exception:", e);
				}
			}

			public boolean isModify()
			{
				return modify;
			}

			public void setModify(final boolean modify)
			{
				this.modify = modify;
			}
		}

		GroupMessages task = new GroupMessages(companyId, newCompanyId, context);
		task.start();
		return task.isModify();
	}

	/**
	 * Actualiza el countryCode de todos los mensajes que no tengan uno asignado
	 */
	public static void updateCountry(String country, Context context)
	{
		class UpdateCountry extends Thread
		{
			private String country;
			private Context context;

			private UpdateCountry(String country, Context context)
			{
				this.country = country;
				this.context = context;
			}

			public void start()
			{
				//Agregado para evitar excepciones Runtime
				if(Looper.myLooper() == null)
				{
					Looper.prepare();
				}

				try
				{
					Realm realm	= Realm.getDefaultInstance();
					realm.executeTransaction(new Realm.Transaction()
					{
						@Override
						public void execute(Realm bgRealm)
						{
							RealmResults<Message> results = bgRealm.where(Message.class).equalTo(Land.KEY_API, "").or().isNull(Land.KEY_API).or().isEmpty(Land.KEY_API)
																.or().equalTo(Land.KEY_API, "'null'").or().equalTo(Land.KEY_API, "''").findAll();

							for(int i = results.size() -1; i >=0; i--)
							{
								results.get(i).setCountryCode(country);
							}
						}
					});
				}
				catch(Exception e)
				{
					Utils.logError(context, "MessageHelper:UpdateCountry:start - Exception:", e);
				}
			}
		}

		//Agregado para evitar ejecución innecesaria de task
		Realm realm						= Realm.getDefaultInstance();
		RealmResults<Message> results	= realm.where(Message.class).equalTo(Land.KEY_API, "").or().isNull(Land.KEY_API).or().isEmpty(Land.KEY_API).or()
											.equalTo(Land.KEY_API, "'null'").or().equalTo(Land.KEY_API, "''").findAll();

		if(StringUtils.isNotEmpty(country) && results.size() > 0)
		{
			new UpdateCountry(country, context).start();
		}
	}

	/**
	 * Procesamiento de data recibida en push
	 * @param data
	 * @param context
	 * @param from
	 * @param isBackground
	 */
	public static void savePush(Bundle data, Context context, String from, boolean isBackground)
	{
		try
		{
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

			if(Common.DEBUG)
			{
				System.out.println("msgId: "+msgId);
				System.out.println("msgType: "+msgType);
				System.out.println("msg: "+msg);
				System.out.println("channel: "+channel);
				System.out.println("companyId: "+companyId);
				System.out.println("phone: "+phone);
				System.out.println("countryCode: "+countryCode);
				System.out.println("flags: "+flags);
				System.out.println("sound: "+sound);
				System.out.println("notificationId: "+notificationId);
				System.out.println("kind: "+kind);
				System.out.println("link: "+link);
				System.out.println("linkThumb: "+linkThumb);
				System.out.println("subMsg: "+subMsg);
				System.out.println("campaignId: "+campaignId);
				System.out.println("listId: "+listId);
				System.out.println("created: "+created);
			}

			//Agregado para contemplar campo sound de iOS
			if(StringUtils.isNumber(sound))
			{
				soundOn = Integer.valueOf(sound);
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
				countryCode = preferences.getString(Land.KEY_API, "");
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

			if(soundOn == MyFirebaseMessagingService.PUSH_NORMAL)
			{
				final Message messageToInsert = message;
				Realm realm = Realm.getDefaultInstance();
				realm.executeTransaction(new Realm.Transaction()
				{
					@Override
					public void execute(Realm realm)
					{
						realm.copyToRealmOrUpdate(messageToInsert);
					}
				});
				realm.close();
			}
			else
			{
				msgId = companyId;
			}

			if(isBackground)
			{
				//Redirect to CardViewActivity
				Intent intent = new Intent(context, CardViewActivity.class);
				intent.putExtra(Common.KEY_ID, companyId);
				intent.putExtra(Common.KEY_LAST_MSGID, msgId);
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				context.startActivity(intent);
			}
			else
			{
				//Show notification
				MyFirebaseMessagingService push	= new MyFirebaseMessagingService();
				push.setContext(context);
				/**
				 * Production applications would usually process the message here.
				 * Eg: - Syncing with server.
				 *	 - Store message in local database.
				 *	 - Update UI.
				 */

				/**
				 * In some cases it may be useful to show a notification indicating to the user that a message was received.
				 */
				push.sendNotification(msgId, preferences, soundOn, notificationId, countryCode, msg, context);
			}
		}
		catch(Exception e)
		{
			Utils.logError(context, "MessageHelper:savePush - Exception:", e);
		}
	}
}