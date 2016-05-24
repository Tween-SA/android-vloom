package com.tween.viacelular.data;

import com.tween.viacelular.utils.Common;

public class Message
{
	private Long	id;
	private String	msgId;
	private String	type;
	private String	msg;
	private String	channel;
	private Integer	status;
	private String	companyId;
	private Integer	ttd;
	private String	phone;
	private String	countryCode;
	private String	flags;
	private Long	created;
	private Long	processed;
	private Long	delivered;
	private Long	acknowladged;
	private Integer	deleted;

	public static final int STATUS_RECEIVE		= 3;
	public static final int STATUS_READ			= 4;
	public static final int STATUS_SPAM			= 5;
	public static final String FLAGS_PUSH		= "1";
	public static final String FLAGS_SMS		= "2";
	public static final String FLAGS_SMSCAP		= "4";
	public static final String FLAGS_PUSHCAP	= "5";
	public static final String KEY_API			= "msgId";
	public static final String KEY_MSG			= "msg";
	public static final String KEY_CHANNEL		= "channel";
	public static final String KEY_TTD			= "ttd";
	public static final String KEY_FLAGS		= "flags";
	public static final String KEY_CREATED		= "created";
	public static final String KEY_PROCESSED	= "processed";
	public static final String KEY_DELIVERED	= "delivered";
	public static final String KEY_ACKNOWLADGED	= "acknowladged";
	public static final String KEY_DELETED		= "deleted";
	public static final String KEY_PLAYLOAD		= "payload";
	public static final String KEY_TIMESTAMP	= "timestamp";
	public static final String SMS_CODE			= "ViaCelular code: ";
	public static final String SMS_CODE_ES		= "Tu código ViaCelular es: ";
	public static final String SMS_INVITE		= "Descargate la APP ViaCelular para tener mejor organizados y ordenados los mensajes de tus empresas ";
	public static final String SMS_CODE_NEW		= "Vloom code: ";
	public static final String SMS_CODE_ES_NEW	= "Tu código Vloom es: ";
	public static final String SMS_INVITE_NEW	= "Descargate la APP Vloom para tener mejor organizados y ordenados los mensajes de tus empresas ";
	public static final String TYPE_SMS			= "SMS";

	public Message()
	{
	}

	public Message(String type, String msg)
	{
		this.type	= type;
		this.msg	= msg;
	}

	public Message(	String msgId, String type, String msg, String channel, Integer status, String companyId, Integer ttd, String phone, String countryCode, String flags, Long created, Long processed,
					Long delivered, Long acknowladged, Integer deleted)
	{
		this.msgId			= msgId;
		this.type			= type;
		this.msg			= msg;
		this.channel		= channel;
		this.status			= status;
		this.companyId		= companyId;
		this.ttd			= ttd;
		this.phone			= phone;
		this.countryCode	= countryCode;
		this.flags			= flags;
		this.created		= created;
		this.processed		= processed;
		this.delivered		= delivered;
		this.acknowladged	= acknowladged;
		this.deleted		= deleted;
	}

	public Message(	Long id, String msgId, String type, String msg, String channel, Integer status, String companyId, Integer ttd, String phone, String countryCode, String flags, Long created,
					Long processed, Long delivered, Long acknowladged, Integer deleted)
	{
		this.id				= id;
		this.msgId			= msgId;
		this.type			= type;
		this.msg			= msg;
		this.channel		= channel;
		this.status			= status;
		this.companyId		= companyId;
		this.ttd			= ttd;
		this.phone			= phone;
		this.countryCode	= countryCode;
		this.flags			= flags;
		this.created		= created;
		this.processed		= processed;
		this.delivered		= delivered;
		this.acknowladged	= acknowladged;
		this.deleted		= deleted;
	}

	public Long getId()
	{
		return id;
	}

	public void setId(Long id)
	{
		this.id = id;
	}

	public String getMsgId()
	{
		if(msgId != null)
		{
			return msgId;
		}
		else
		{
			return "";
		}
	}

	public void setMsgId(String msgId)
	{
		this.msgId = msgId;
	}

	public String getType()
	{
		if(type != null)
		{
			return type;
		}
		else
		{
			return "";
		}
	}

	public void setType(String type)
	{
		this.type = type;
	}

	public String getMsg()
	{
		if(msg != null)
		{
			return msg;
		}
		else
		{
			return "";
		}
	}

	public void setMsg(String msg)
	{
		this.msg = msg;
	}

	public String getChannel()
	{
		if(channel != null)
		{
			return channel;
		}
		else
		{
			return "";
		}
	}

	public void setChannel(final String channel)
	{
		this.channel = channel;
	}

	public int getStatus()
	{
		if(status != null)
		{
			return status;
		}else
		{
			return STATUS_RECEIVE;
		}
	}

	public void setStatus(final int status)
	{
		this.status = status;
	}

	public String getCompanyId()
	{
		if(companyId != null)
		{
			return companyId;
		}
		else
		{
			return "";
		}
	}

	public void setCompanyId(String companyId)
	{
		this.companyId = companyId;
	}

	public int getDeleted()
	{
		if(deleted != null)
		{
			return deleted;
		}
		else
		{
			return Common.BOOL_NO;
		}
	}

	public void setDeleted(int deleted)
	{
		this.deleted = deleted;
	}

	public int getTtd()
	{
		if(ttd != null)
		{
			return ttd;
		}
		else
		{
			return Common.BOOL_NO;
		}
	}

	public void setTtd(int ttd)
	{
		this.ttd = ttd;
	}

	public String getPhone()
	{
		if(phone != null)
		{
			return phone;
		}else
		{
			return "";
		}
	}

	public void setPhone(String phone)
	{
		this.phone = phone;
	}

	public String getCountryCode()
	{
		if(countryCode != null)
		{
			return countryCode;
		}
		else
		{
			return "";
		}
	}

	public void setCountryCode(String countryCode)
	{
		this.countryCode = countryCode;
	}

	public String getFlags()
	{
		if(flags != null)
		{
			return flags;
		}
		else
		{
			return "";
		}
	}

	public void setFlags(String flags)
	{
		this.flags = flags;
	}

	public long getCreated()
	{
		if(created != null)
		{
			return created;
		}
		else
		{
			return System.currentTimeMillis();
		}
	}

	public void setCreated(long created)
	{
		this.created = created;
	}

	public long getProcessed()
	{
		if(processed != null)
		{
			return processed;
		}
		else
		{
			return System.currentTimeMillis();
		}
	}

	public void setProcessed(long processed)
	{
		this.processed = processed;
	}

	public long getDelivered()
	{
		if(delivered != null)
		{
			return delivered;
		}
		else
		{
			return System.currentTimeMillis();
		}
	}

	public void setDelivered(long delivered)
	{
		this.delivered = delivered;
	}

	public long getAcknowladged()
	{
		if(acknowladged != null)
		{
			return acknowladged;
		}
		else
		{
			return System.currentTimeMillis();
		}
	}

	public void setAcknowladged(long acknowladged)
	{
		this.acknowladged = acknowladged;
	}

	public void debug()
	{
		System.out.println("Message - id: " + id);
		System.out.println("Message - msgId: " + msgId);
		System.out.println("Message - type: " + type);
		System.out.println("Message - msg: " + msg);
		System.out.println("Message - channel: " + channel);
		System.out.println("Message - status: " + status);
		System.out.println("Message - companyId: " + companyId);
		System.out.println("Message - ttd: " + ttd);
		System.out.println("Message - phone: " + phone);
		System.out.println("Message - countryCode: " + countryCode);
		System.out.println("Message - flags: " + flags);
		System.out.println("Message - created: " + created);
		System.out.println("Message - processed: " + processed);
		System.out.println("Message - delivered: " + delivered);
		System.out.println("Message - acknowladged: " + acknowladged);
		System.out.println("Message - deleted: " + deleted);
	}
}