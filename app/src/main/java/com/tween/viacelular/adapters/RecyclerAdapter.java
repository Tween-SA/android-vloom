package com.tween.viacelular.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.tween.viacelular.R;
import com.tween.viacelular.models.ConnectedAccount;
import com.tween.viacelular.models.Migration;
import com.tween.viacelular.models.User;
import com.tween.viacelular.utils.Common;
import com.tween.viacelular.utils.StringUtils;
import com.tween.viacelular.utils.Utils;

import io.realm.Realm;

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.ViewHolder>
{
	private static final int	TYPE_HEADER				= 0;
	private static final int	TYPE_ITEM				= 1;
	public static final int		HOME_SELECTED			= 1;
	public static final int		SUSCRIPTION_SELECTED	= 2;
	public static final int		SETTINGS_SELECTED		= 3;
	private int					mIcons[]				= {R.drawable.notificaciones, R.drawable.empresas, 0};//Quitamos la pantalla Feedback
	private String				mNavTitles[];
	private String				name, email, phone;
	private int					profile, selected, color;
	private Context				context;

	public static class ViewHolder extends RecyclerView.ViewHolder
	{
		public int				HolderId;
		public TextView			textView, name, email;
		public ImageView		imageView, profile;
		public View				div;
		public RelativeLayout	rlItem;

		public ViewHolder(View itemView, int ViewType)
		{
			super(itemView);

			if(ViewType == TYPE_ITEM)
			{
				textView	= (TextView) itemView.findViewById(R.id.rowText);
				imageView	= (ImageView) itemView.findViewById(R.id.rowIcon);
				div			= itemView.findViewById(R.id.div);
				rlItem		= (RelativeLayout) itemView.findViewById(R.id.rlItem);
				HolderId	= 1;
				textView.setClickable(true);
			}
			else
			{
				name		= (TextView) itemView.findViewById(R.id.name);
				email		= (TextView) itemView.findViewById(R.id.email);
				profile		= (ImageView) itemView.findViewById(R.id.circleView);
				HolderId	= 0;
			}
		}
	}

	public RecyclerAdapter(String[] title, int selected, int color, Context context)
	{
		try
		{
			Migration.getDB(context);
			//Modificaciones para contemplar migración a Realm
			Realm realm		= Realm.getDefaultInstance();
			User user		= realm.where(User.class).findFirst();
			String name		= "";
			String email	= "";
			String phone	= "";

			if(user != null)
			{
				if(StringUtils.isNotEmpty(user.getFirstName()))
				{
					name = user.getFirstName() + " ";
				}

				if(StringUtils.isNotEmpty(user.getLastName()))
				{
					name = name + user.getLastName();
				}

				if(StringUtils.isNotEmpty(user.getEmail()))
				{
					email = user.getEmail();
				}
				else
				{
					//Por si no trajo email
					ConnectedAccount connectedAccount = realm.where(ConnectedAccount.class).equalTo(Common.KEY_TYPE, ConnectedAccount.TYPE_GOOGLE).findFirst();

					if(connectedAccount != null)
					{
						email = connectedAccount.getName();
					}
				}

				if(StringUtils.isNotEmpty(user.getPhone()))
				{
					phone = user.getPhone();
				}
			}

			this.name		= name;
			this.email		= email;
			this.profile	= R.drawable.ic_account_circle_white_48dp;
			this.mNavTitles	= title;
			this.selected	= selected;
			this.color		= color;
			this.phone		= phone;
			this.context	= context;
		}
		catch(Exception e)
		{
			Utils.logError(context, "RecyclerAdapter.ViewHolder:constructor - Exception:", e);
		}
	}

	@Override
	public RecyclerAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
	{
		try
		{
			if(viewType == TYPE_ITEM)
			{
				View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_row, parent, false);
				return new ViewHolder(v, viewType);
			}
			else
			{
				if(viewType == TYPE_HEADER)
				{
					View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.header, parent, false);
					return new ViewHolder(v, viewType);
				}
			}
		}
		catch(Exception e)
		{
			Utils.logError(context, "RecyclerAdapter.ViewHolder:onCreateViewHolder - Exception:", e);
		}

		return null;
	}

	@Override
	public void onBindViewHolder(RecyclerAdapter.ViewHolder holder, final int position)
	{
		try
		{
			if(holder.HolderId == 1)
			{
				holder.textView.setText(mNavTitles[position - 1]);

				if(mIcons[position - 1] != 0)
				{
					holder.imageView.setImageResource(mIcons[position - 1]);
				}
				else
				{
					holder.imageView.setVisibility(android.widget.ImageView.GONE);
				}

				if(position != 2)
				{
					holder.div.setVisibility(android.view.View.GONE);
				}

				if(position == selected)
				{
					holder.textView.setSelected(true);
					holder.imageView.setSelected(true);
					holder.textView.setTextColor(color);
					holder.imageView.setColorFilter(color);
				}
				else
				{
					holder.textView.setSelected(false);
					holder.imageView.setSelected(false);
				}
			}
			else
			{
				//Agregado para mostrar siempre el celular debajo del nombre o email según de cual dato dispongamos

				if(StringUtils.isNotEmpty(name))
				{
					holder.name.setText(name);
					holder.name.setVisibility(android.widget.TextView.VISIBLE);
				}
				else
				{
					if(StringUtils.isNotEmpty(email))
					{
						holder.name.setText(email);
						holder.name.setVisibility(android.widget.TextView.VISIBLE);
					}
					else
					{
						holder.name.setVisibility(android.widget.TextView.GONE);
					}
				}
				
				holder.email.setText(phone);
			}
		}
		catch(Exception e)
		{
			Utils.logError(context, "RecyclerAdapter:onBindViewHolder - Exception:", e);
		}
	}

	@Override
	public int getItemCount()
	{
		//Agregado para evitar excepciones por referencia de lista null
		if(mNavTitles != null)
		{
			return mNavTitles.length + 1;
		}
		else
		{
			return 1;
		}
	}

	@Override
	public int getItemViewType(int position)
	{
		if(isPositionHeader(position))
		{
			return TYPE_HEADER;
		}

		return TYPE_ITEM;
	}

	private boolean isPositionHeader(int position)
	{
		return position == 0;
	}
}