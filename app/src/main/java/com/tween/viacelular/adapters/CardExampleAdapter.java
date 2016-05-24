package com.tween.viacelular.adapters;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.tween.viacelular.R;
import com.tween.viacelular.data.Company;
import com.tween.viacelular.data.Message;
import com.tween.viacelular.utils.Common;
import com.tween.viacelular.utils.DateUtils;
import com.tween.viacelular.utils.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by david.figueroa on 30/12/15.
 */
public class CardExampleAdapter extends RecyclerView.Adapter<CardExampleAdapter.ViewHolder>
{
	public List<Message>	notificationList;
	private Activity activity;

	public static class ViewHolder extends RecyclerView.ViewHolder
	{
		public TextView		rowTime;
		public TextView		txtTitle;
		public TextView		txtContent;

		public ViewHolder(View itemView)
		{
			super(itemView);
			rowTime		= (TextView) itemView.findViewById(R.id.rowTime);
			txtTitle	= (TextView) itemView.findViewById(R.id.txtTitle);
			txtContent	= (TextView) itemView.findViewById(R.id.txtContent);
			txtContent.setMovementMethod(LinkMovementMethod.getInstance());
		}
	}

	public CardExampleAdapter(Activity activity, Company client)
	{
		this.activity			= activity;
		this.notificationList	= new ArrayList<>();

		try
		{
			this.notificationList = CardExampleAdapter.parseMsgExamples(client);
		}
		catch(Exception e)
		{
			System.out.println("CardAdapterExample:construct - Exception: " + e);
			if(Common.DEBUG)
			{
				e.printStackTrace();
			}
		}
	}

	public static List<Message> parseMsgExamples(Company client)
	{
		List<Message> messages = new ArrayList<>();

		try
		{
			if(client != null)
			{
				if(StringUtils.isNotEmpty(client.getMsgExamples()))
				{
					String msgExamples = StringUtils.removeSpacesJSON(client.getMsgExamples());

					if(msgExamples.startsWith("[") && msgExamples.endsWith("]"))
					{
						JSONArray jsonArray = new JSONArray(msgExamples);

						if(jsonArray != null)
						{
							if(jsonArray.length() > 0)
							{
								for(int i = 0; i < jsonArray.length(); i++)
								{
									JSONObject json = jsonArray.getJSONObject(i);

									if(json != null)
									{
										String title	= "";
										String msg		= "";
										Long created	= System.currentTimeMillis();

										if(json.has(Common.KEY_TITLE))
										{
											if(!json.isNull(Common.KEY_TITLE))
											{
												if(StringUtils.isNotEmpty(json.getString(Common.KEY_TITLE)))
												{
													title = json.getString(Common.KEY_TITLE);
												}
											}
										}

										if(json.has(Message.KEY_MSG))
										{
											if(!json.isNull(Message.KEY_MSG))
											{
												if(StringUtils.isNotEmpty(json.getString(Message.KEY_MSG)))
												{
													msg = json.getString(Message.KEY_MSG);
												}
											}
										}

										if(json.has(Message.KEY_CREATED))
										{
											if(!json.isNull(Message.KEY_CREATED))
											{
												if(StringUtils.isNotEmpty(json.getString(Message.KEY_CREATED)))
												{
													created = json.getLong(Message.KEY_CREATED);
												}
											}
										}

										if(StringUtils.isNotEmpty(title) && StringUtils.isNotEmpty(msg))
										{
											Message message = new Message(	"", title, msg, "", Message.STATUS_RECEIVE, client.getCompanyId(), 0, "", client.getCountryCode(), "2", created,
																			created, created, created, Common.BOOL_NO);
											messages.add(message);
										}
									}
								}
							}
						}
					}
				}
			}
		}
		catch(Exception e)
		{
			System.out.println("CardAdapterExample:parseMsgExamples - Exception: " + e);

			if(Common.DEBUG)
			{
				e.printStackTrace();
			}
		}

		return messages;
	}

	@Override
	public CardExampleAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
	{
		try
		{
			LayoutInflater mInflater	= LayoutInflater.from(parent.getContext());
			ViewGroup mainGroup			= (ViewGroup) mInflater.inflate(R.layout.item_card_example, parent, false);
			return new ViewHolder(mainGroup);
		}
		catch(Exception e)
		{
			System.out.println("CardAdapterExample:onCreateViewHolder - Exception: " + e);

			if(Common.DEBUG)
			{
				e.printStackTrace();
			}
		}

		return null;
	}

	@Override
	public void onBindViewHolder(final ViewHolder holder, final int position)
	{
		try
		{
			if(notificationList != null)
			{
				if(notificationList.size() > 0)
				{
					Message item = notificationList.get(position);

					if(item != null)
					{
						if(item.getMsg().equals(activity.getString(R.string.no_messages_text)))
						{
							holder.rowTime.setVisibility(TextView.GONE);
							holder.txtTitle.setText(item.getType());
							holder.txtContent.setText(item.getMsg());
						}
						else
						{
							String time = DateUtils.getTimeFromTs(item.getCreated(), activity.getApplicationContext());

							if(StringUtils.isNotEmpty(time))
							{
								holder.rowTime.setText(time);
							}
							else
							{
								holder.rowTime.setVisibility(TextView.GONE);
							}

							holder.txtTitle.setText(item.getType());
							holder.txtContent.setText(item.getMsg());
						}
					}
				}
			}
		}
		catch(Exception e)
		{
			System.out.println("CardAdapterExample:onBindViewHolder - Exception: " + e);

			if(Common.DEBUG)
			{
				e.printStackTrace();
			}
		}
	}

	@Override
	public int getItemCount()
	{
		if(notificationList != null)
		{
			return notificationList.size();
		}
		else
		{
			return 0;
		}
	}
}