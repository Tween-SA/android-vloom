package com.tween.viacelular.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import com.squareup.picasso.Picasso;
import com.tween.viacelular.R;
import com.tween.viacelular.activities.BlockedActivity;
import com.tween.viacelular.models.Suscription;
import com.tween.viacelular.utils.Common;
import com.tween.viacelular.utils.StringUtils;
import io.realm.RealmResults;

/**
 * Created by david.figueroa on 8/7/15.
 */
public class BlockedAdapter extends RecyclerView.Adapter<BlockedAdapter.ViewHolder>
{
	private RealmResults<Suscription>	clients;
	private BlockedActivity				activity;

	public static class ViewHolder extends RecyclerView.ViewHolder
	{
		public int			HolderId;
		public ImageView	picture;
		public TextView		txtTitle;
		public TextView		txtContent;
		public ImageButton	ibUnLock;

		public ViewHolder(View itemView, int ViewType)
		{
			super(itemView);
			HolderId	= 0;
			picture		= (ImageView) itemView.findViewById(R.id.circleView);
			txtTitle	= (TextView) itemView.findViewById(R.id.rowText);
			txtContent	= (TextView) itemView.findViewById(R.id.rowSubText);
			ibUnLock	= (ImageButton) itemView.findViewById(R.id.ibUnLock);
		}
	}

	public BlockedAdapter(RealmResults<Suscription> clients, BlockedActivity activity)
	{
		this.clients	= clients;
		this.activity	= activity;
	}

	@Override
	public BlockedAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
	{
		//Agregado para capturar excepciones
		try
		{
			if(viewType == 0)
			{
				View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_blocked, parent, false);
				return new ViewHolder(v, viewType);
			}
		}
		catch(Exception e)
		{
			System.out.println("BlockedAdapter:onCreateViewHolder - Exception: " + e);
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
		//Agregado para capturar excepciones
		try
		{
			if(clients.size() > 0)
			{
				Suscription item = clients.get(position);

				if(item != null)
				{
					//Modificación para migrar a asynctask la descarga de imágenes
					if(StringUtils.isNotEmpty(item.getImage()))
					{
						//Modificación de librería para recargar imagenes a mientras se está viendo el listado y optimizar vista
						Picasso.with(activity).load(item.getImage()).placeholder(R.drawable.ic_launcher).into(holder.picture);
					}
					else
					{
						//Mostrar el logo de Vloom si no tiene logo
						Picasso.with(activity).load(Suscription.ICON_APP).placeholder(R.drawable.ic_launcher).into(holder.picture);
					}

					holder.txtTitle.setText(item.getName());
					holder.txtContent.setText(item.getIndustry());
					holder.ibUnLock.setOnClickListener(new View.OnClickListener()
					{
						@Override
						public void onClick(View v)
						{
							activity.unLockCompany(position);
						}
					});
				}
			}
		}
		catch(Exception e)
		{
			System.out.println("BlockedAdapter:onBindViewHolder - Exception: " + e);
			if(Common.DEBUG)
			{
				e.printStackTrace();
			}
		}
	}

	@Override
	public int getItemCount()
	{
		//Agregado para prevenir lista sin resultados
		if(clients != null)
		{
			return clients.size();
		}
		else
		{
			return 0;
		}
	}

	@Override
	public int getItemViewType(int position)
	{
		return 0;
	}
}