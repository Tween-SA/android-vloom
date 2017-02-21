package com.tween.viacelular.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.ArrayRes;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.tween.viacelular.R;
import com.tween.viacelular.models.Suscription;
import com.tween.viacelular.utils.Common;
import com.tween.viacelular.utils.Utils;
import io.realm.Realm;

/**
 * Created by david.figueroa on 15/7/15.
 */
public class IconOptionAdapter extends BaseAdapter
{
	private final Context			mContext;
	private final CharSequence[]	mItems;
	private String					company; //Modificación para recargar Suscripción fuera de contexto Realm
	public final static int			OPTION_SILENCED	= 0;
	public final static int			OPTION_EMPTY	= 1;
	public final static int			OPTION_BLOCK	= 2;

	public IconOptionAdapter(Context context, @ArrayRes int arrayResId, String company)
	{
		this(context, context.getResources().getTextArray(arrayResId), company);
	}

	private IconOptionAdapter(Context context, CharSequence[] items, String company)
	{
		this.mContext	= context;
		this.mItems		= items;
		this.company	= company;
	}

	@Override
	public int getCount()
	{
		return mItems.length;
	}

	@Override
	public CharSequence getItem(int position)
	{
		return mItems[position];
	}

	@Override
	public long getItemId(int position)
	{
		return position;
	}

	@Override
	public boolean hasStableIds()
	{
		return true;
	}

	@SuppressLint("ViewHolder")
	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
		try
		{
			//Agregado para posterior migración a Realm
			Realm realm				= Realm.getDefaultInstance();
			Suscription suscription	= realm.where(Suscription.class).equalTo(Suscription.KEY_API, company).findFirst();

			if(convertView == null)
			{
				convertView = View.inflate(mContext, R.layout.item_dialog_icon, null);
			}

			switch(position)
			{
				case OPTION_SILENCED:
					if(suscription.getSilenced() == Common.BOOL_YES)
					{
						((ImageView) convertView.findViewById(R.id.iconOption)).setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_notifications_active_black_48dp));
						((TextView) convertView.findViewById(R.id.title)).setText(mContext.getString(R.string.activate_notif));
					}
					else
					{
						((ImageView) convertView.findViewById(R.id.iconOption)).setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_notifications_off_black_48dp));
						((TextView) convertView.findViewById(R.id.title)).setText(mItems[position]);
					}
				break;

				case OPTION_EMPTY:
					((ImageView) convertView.findViewById(R.id.iconOption)).setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_delete_black_48dp));
					((TextView) convertView.findViewById(R.id.title)).setText(mItems[position]);
				break;

				case OPTION_BLOCK:
					if(suscription.getFollower() == Common.BOOL_YES)
					{
						((ImageView) convertView.findViewById(R.id.iconOption)).setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_block_black_48dp));
						((TextView) convertView.findViewById(R.id.title)).setText(mItems[position]);
					}
					else
					{
						((ImageView) convertView.findViewById(R.id.iconOption)).setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_add_circle_black_24dp));
						((TextView) convertView.findViewById(R.id.title)).setText(mContext.getString(R.string.landing_suscribe));
					}
				break;
			}
		}
		catch(Exception e)
		{
			Utils.logError(mContext, "IconOptionAdapter:getView - Exception:", e);
		}

		return convertView;
	}
}