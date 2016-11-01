package com.tween.viacelular.models;

import io.realm.RealmObject;
import io.realm.annotations.Ignore;
import io.realm.annotations.Index;
import io.realm.annotations.PrimaryKey;

/**
 * Created by davidfigueroa on 29/2/16.
 */
public class Message extends RealmObject
{
	@PrimaryKey
	@Index
	private String	msgId;
	private String	type;
	private String	msg;
	@Index
	private String	channel;
	private int		status;
	private String	phone;
	private String	countryCode;
	private String	flags;
	@Index
	private Long	created;
	private int		deleted;
	private int		kind;
	private String	link;
	private String	linkThumbnail;
	private String	subMsg;
	private String	campaignId;
	private String	listId;
	@Index
	private String	companyId;
	private String	socialId;
	private String	socialDate;
	private int		socialLikes;
	private int		socialShares;
	private String	socialAccount;
	private String	socialName;
	private String	txid;
	private String	note;
	private String	attached;
	private String	attachedTwo;
	private String	attachedThree;

	@Ignore
	public static final int KIND_TEXT				= 0; //La push contiene únicamente texto, clase de push por defecto
	@Ignore
	public static final int KIND_IMAGE				= 1; //La push contiene un link a una imagen
	@Ignore
	public static final int KIND_INVOICE			= 2; //La push contiene un link a un archivo descargable identificado como factura
	@Ignore
	public static final int KIND_FILE_DOWNLOADABLE	= 3; //La push contiene un link a un archivo descargable genérico
	@Ignore
	public static final int KIND_LINKWEB			= 4; //La push contiene un link a un sitio web
	@Ignore
	public static final int KIND_LINKMAP			= 5; //La push contiene un link a un mapa
	@Ignore
	public static final int KIND_VIDEO				= 6; //La push contiene un link a un vídeo
	@Ignore
	public static final int KIND_RATING				= 7; //La push contiene funcionalidad para calificar la Company
	@Ignore
	public static final int KIND_AUDIO				= 8; //La push contiene un link a un audio
	@Ignore
	public static final int KIND_TWITTER			= 9; //La push es un tweet de Twitter
	@Ignore
	public static final int KIND_TWITTER_IMAGE		= 10; //La push es un tweet de Twitter que contiene una imagen
	@Ignore
	public static final int KIND_FACEBOOK			= 11; //La push es un post de Facebook
	@Ignore
	public static final int KIND_FACEBOOK_IMAGE		= 12; //La push es un post de Facebook que contiene una imagen
	@Ignore
	public static final int STATUS_RECEIVE			= 3;
	@Ignore
	public static final int STATUS_READ				= 4;
	@Ignore
	public static final int STATUS_SPAM				= 5;
	@Ignore
	public static final int STATUS_PERSONAL			= 6;
	@Ignore
	public static final String KEY_COMPANYID		= "company_id";
	@Ignore
	public static final String KEY_TXID				= "txid";
	@Ignore
	public static final String FLAGS_PUSH			= "1";
	@Ignore
	public static final String FLAGS_SMS			= "2";
	@Ignore
	public static final String FLAGS_SMSCAP			= "4";
	@Ignore
	public static final String FLAGS_PUSHCAP		= "5";
	@Ignore
	public static final String KEY_API				= "msgId";
	@Ignore
	public static final String KEY_CAMPAIGNID		= "campaignId";
	@Ignore
	public static final String KEY_LISTID			= "listId";
	@Ignore
	public static final String KEY_MSG				= "msg";
	@Ignore
	public static final String KEY_NOTE				= "note";
	@Ignore
	public static final String KEY_ATTACHED			= "attached";
	@Ignore
	public static final String KEY_ATTACHEDTWO		= "attachedTwo";
	@Ignore
	public static final String KEY_ATTACHEDTHREE	= "attachedThree";
	@Ignore
	public static final String KEY_SUBMSG			= "subMsg";
	@Ignore
	public static final String KEY_CHANNEL			= "channel";
	@Ignore
	public static final String KEY_TTD				= "ttd";
	@Ignore
	public static final String KEY_FLAGS			= "flags";
	@Ignore
	public static final String KEY_CREATED			= "created";
	@Ignore
	public static final String KEY_KIND				= "kind";
	@Ignore
	public static final String KEY_LINK				= "link";
	@Ignore
	public static final String KEY_LINKTHUMB		= "linkThumb";
	@Ignore
	public static final String KEY_DELETED			= "deleted";
	@Ignore
	public static final String KEY_PLAYLOAD			= "payload";
	@Ignore
	public static final String KEY_TIMESTAMP		= "timestamp";
	@Ignore
	public static final String KEY_SOCIALID			= "socialId";
	@Ignore
	public static final String KEY_SOCIALDATE		= "socialDate";
	@Ignore
	public static final String KEY_SOCIALLIKES		= "socialLikes";
	@Ignore
	public static final String KEY_SOCIALSHARES		= "socialShares";
	@Ignore
	public static final String KEY_SOCIALACCOUNT	= "socialAccount";
	@Ignore
	public static final String KEY_SOCIALNAME		= "socialName";
	@Ignore
	public static final String SMS_CODE				= "ViaCelular code: ";
	@Ignore
	public static final String SMS_CODE_ES			= "Tu código ViaCelular es: ";
	@Ignore
	public static final String SMS_INVITE			= "Descargate la APP ViaCelular para tener mejor organizados y ordenados los mensajes de tus empresas ";
	@Ignore
	public static final String SMS_CODE_NEW			= "Vloom code: ";
	@Ignore
	public static final String SMS_CODE_ES_NEW		= "Tu código Vloom es: ";
	@Ignore
	public static final String SMS_INVITE_NEW		= "Descargate la APP Vloom para tener mejor organizados y ordenados los mensajes de tus empresas ";
	@Ignore
	public static final String TYPE_SMS				= "SMS";

	public Message()
	{
	}

	public Message(	final String msgId, final String type, final String msg, final String channel, final int status, final String phone, final String countryCode, final String flags,
					final Long created, final int deleted, final int kind, final String link, final String linkThumbnail, final String subMsg, final String campaignId, final String listId,
					final String companyId)
	{
		this.msgId			= msgId;
		this.type			= type;
		this.msg			= msg;
		this.channel		= channel;
		this.status			= status;
		this.phone			= phone;
		this.countryCode	= countryCode;
		this.flags			= flags;
		this.created		= created;
		this.deleted		= deleted;
		this.kind			= kind;
		this.link			= link;
		this.linkThumbnail	= linkThumbnail;
		this.subMsg			= subMsg;
		this.campaignId		= campaignId;
		this.listId			= listId;
		this.companyId		= companyId;
	}

	public String getMsgId()
	{
		return msgId;
	}

	public void setMsgId(final String msgId)
	{
		this.msgId = msgId;
	}

	public String getType()
	{
		return type;
	}

	public void setType(final String type)
	{
		this.type = type;
	}

	public String getMsg()
	{
		return msg;
	}

	public void setMsg(final String msg)
	{
		this.msg = msg;
	}

	public String getChannel()
	{
		return channel;
	}

	public void setChannel(final String channel)
	{
		this.channel = channel;
	}

	public int getStatus()
	{
		return status;
	}

	public void setStatus(final int status)
	{
		this.status = status;
	}

	public String getPhone()
	{
		return phone;
	}

	public void setPhone(final String phone)
	{
		this.phone = phone;
	}

	public String getCountryCode()
	{
		return countryCode;
	}

	public void setCountryCode(final String countryCode)
	{
		this.countryCode = countryCode;
	}

	public String getFlags()
	{
		return flags;
	}

	public void setFlags(final String flags)
	{
		this.flags = flags;
	}

	public Long getCreated()
	{
		return created;
	}

	public void setCreated(final Long created)
	{
		this.created = created;
	}

	public int getDeleted()
	{
		return deleted;
	}

	public void setDeleted(final int deleted)
	{
		this.deleted = deleted;
	}

	public int getKind()
	{
		return kind;
	}

	public void setKind(final int kind)
	{
		this.kind = kind;
	}

	public String getLink()
	{
		return link;
	}

	public void setLink(final String link)
	{
		this.link = link;
	}

	public String getLinkThumbnail()
	{
		return linkThumbnail;
	}

	public void setLinkThumbnail(final String linkThumbnail)
	{
		this.linkThumbnail = linkThumbnail;
	}

	public String getSubMsg()
	{
		return subMsg;
	}

	public void setSubMsg(final String subMsg)
	{
		this.subMsg = subMsg;
	}

	public String getCampaignId()
	{
		return campaignId;
	}

	public void setCampaignId(final String campaignId)
	{
		this.campaignId = campaignId;
	}

	public String getListId()
	{
		return listId;
	}

	public void setListId(final String listId)
	{
		this.listId = listId;
	}

	public String getCompanyId()
	{
		return companyId;
	}

	public void setCompanyId(final String companyId)
	{
		this.companyId = companyId;
	}

	public String getSocialId()
	{
		return socialId;
	}

	public void setSocialId(final String socialId)
	{
		this.socialId = socialId;
	}

	public String getSocialDate()
	{
		return socialDate;
	}

	public void setSocialDate(final String socialDate)
	{
		this.socialDate = socialDate;
	}

	public int getSocialLikes()
	{
		return socialLikes;
	}

	public void setSocialLikes(final int socialLikes)
	{
		this.socialLikes = socialLikes;
	}

	public int getSocialShares()
	{
		return socialShares;
	}

	public void setSocialShares(final int socialShares)
	{
		this.socialShares = socialShares;
	}

	public String getSocialAccount()
	{
		return socialAccount;
	}

	public void setSocialAccount(final String socialAccount)
	{
		this.socialAccount = socialAccount;
	}

	public String getSocialName()
	{
		return socialName;
	}

	public void setSocialName(final String socialName)
	{
		this.socialName = socialName;
	}

	public String getTxid()
	{
		return txid;
	}

	public void setTxid(final String txid)
	{
		this.txid = txid;
	}

	public String getNote()
	{
		return note;
	}

	public void setNote(final String note)
	{
		this.note = note;
	}

	public String getAttached()
	{
		return attached;
	}

	public void setAttached(final String attached)
	{
		this.attached = attached;
	}

	public String getAttachedTwo()
	{
		return attachedTwo;
	}

	public void setAttachedTwo(final String attachedTwo)
	{
		this.attachedTwo = attachedTwo;
	}

	public String getAttachedThree()
	{
		return attachedThree;
	}

	public void setAttachedThree(final String attachedThree)
	{
		this.attachedThree = attachedThree;
	}
}