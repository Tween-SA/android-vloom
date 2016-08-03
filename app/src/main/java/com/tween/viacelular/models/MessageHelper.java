package com.tween.viacelular.models;

import android.os.Looper;
import com.tween.viacelular.data.Country;
import com.tween.viacelular.utils.Common;
import com.tween.viacelular.utils.StringUtils;
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
		}
		else
		{
			System.out.println("\nMessage - is null");
		}
	}

	/**
	 * Marca todos los mensajes de una Suscription como eliminados. (Vacía la Company)
	 * @param companyId
	 * @param flag
	 */
	public static void emptyCompany(String companyId, int flag)
	{
		class DeleteMessages extends Thread
		{
			private String	companyId;
			private int		flag;

			public DeleteMessages(String companyId, int flag)
			{
				this.companyId	= companyId;
				this.flag		= flag;
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
					System.out.println("MessageHelper:DeleteMessages:start - Exception: " + e);

					if(Common.DEBUG)
					{
						e.printStackTrace();
					}
				}
			}
		}

		DeleteMessages task = new DeleteMessages(companyId, flag);
		task.start();
	}

	/**
	 * Reagrupa mensajes bajo una Suscription (Company)
	 * @param companyId
	 * @param newCompanyId
	 */
	public static boolean groupMessages(String companyId, String newCompanyId)
	{
		class GroupMessages extends Thread
		{
			private String	companyId;
			private String	newCompanyId;
			private boolean	modify;

			public GroupMessages(String companyId, String newCompanyId)
			{
				this.companyId		= companyId;
				this.newCompanyId	= newCompanyId;
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
					System.out.println("MessageHelper:groupMessages:start - Exception: " + e);

					if(Common.DEBUG)
					{
						e.printStackTrace();
					}
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

		GroupMessages task = new GroupMessages(companyId, newCompanyId);
		task.start();
		return task.isModify();
	}

	/**
	 * Actualiza el countryCode de todos los mensajes que no tengan uno asignado
	 */
	public static void updateCountry(String country)
	{
		class UpdateCountry extends Thread
		{
			private String country;

			public UpdateCountry(String country)
			{
				this.country = country;
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
					System.out.println("MessageHelper:UpdateCountry:start - Exception: " + e);

					if(Common.DEBUG)
					{
						e.printStackTrace();
					}
				}
			}
		}

		//Agregado para evitar ejecución innecesaria de task
		Realm realm						= Realm.getDefaultInstance();
		RealmResults<Message> results	= realm.where(Message.class).equalTo(Land.KEY_API, "").or().isNull(Land.KEY_API).or().isEmpty(Land.KEY_API).or()
											.equalTo(Land.KEY_API, "'null'").or().equalTo(Land.KEY_API, "''").findAll();

		if(StringUtils.isNotEmpty(country) && results.size() > 0)
		{
			UpdateCountry task = new UpdateCountry(country);
			task.start();
		}
	}
}