package com.tween.viacelular.adapters;

import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.amulyakhare.textdrawable.TextDrawable;
import com.squareup.picasso.Picasso;
import com.tween.viacelular.R;
import com.tween.viacelular.fragments.SwipeRefreshLayoutBasicFragment;
import com.tween.viacelular.models.Message;
import com.tween.viacelular.models.Suscription;
import com.tween.viacelular.models.SuscriptionHelper;
import com.tween.viacelular.utils.Common;
import com.tween.viacelular.utils.DateUtils;
import com.tween.viacelular.utils.StringUtils;
import com.tween.viacelular.utils.Utils;
import java.util.ArrayList;
import java.util.List;
import de.hdodenhof.circleimageview.CircleImageView;
import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;

/**
 * Adpatador para presentación de empresas en la pantalla principal
 * Created by Tween (David Figueroa davo.figueroa@tween.com.ar) on 08/07/2015
 */
public class HomeAdapter extends RecyclerView.Adapter<HomeAdapter.ViewHolder>
{
	private List<Suscription>				clients		= new ArrayList<>();
	private SwipeRefreshLayoutBasicFragment	activity;

	public static class ViewHolder extends RecyclerView.ViewHolder
	{
		public int				HolderId;
		private RelativeLayout	rlClient;
		private CircleImageView picture;
		private TextView		title, subTitle, rowTime;
		private ImageView		count, silence, bigSilence, price, bigPrice, block, bigBlock;

		public ViewHolder(View view, int ViewType)
		{
			super(view);
			HolderId	= 0;
			rlClient	= (RelativeLayout) view.findViewById(R.id.rlClient);
			picture		= (CircleImageView) view.findViewById(R.id.circleView);
			title		= (TextView) view.findViewById(R.id.rowText);
			subTitle	= (TextView) view.findViewById(R.id.rowSubText);
			rowTime		= (TextView) view.findViewById(R.id.rowTime);
			count		= (ImageView) view.findViewById(R.id.imgCount);
			silence		= (ImageView) view.findViewById(R.id.imgSilence);
			bigSilence	= (ImageView) view.findViewById(R.id.imgBigSilence);
			price		= (ImageView) view.findViewById(R.id.imgPrice);
			bigPrice	= (ImageView) view.findViewById(R.id.imgBigPrice);
			//Se agregan los iconos para destacar cuando la company está bloqueada y se hizo dismiss de la card para suscribir
			block		= (ImageView) view.findViewById(R.id.imgBlock);
			bigBlock	= (ImageView) view.findViewById(R.id.imgBigBlock);
		}
	}

	public HomeAdapter(SwipeRefreshLayoutBasicFragment activity)
	{
		this.clients	= SuscriptionHelper.getList(activity.getHomeActivity());
		this.activity	= activity;
	}

	@Override
	public HomeAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
	{
		//Agregado para capturar excepciones
		try
		{
			if(viewType == 0)
			{
				View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notification, parent, false);
				return new ViewHolder(v, viewType);
			}
		}
		catch(Exception e)
		{
			Utils.logError(activity.getContext(), "HomeAdapter:onCreateViewHolder - Exception:", e);
		}

		return null;
	}

	@Override
	public void onBindViewHolder(final ViewHolder holder, final int position)
	{
		try
		{
			Realm realm	= Realm.getDefaultInstance();

			if(clients.size() > 0)
			{
				Suscription suscription = null;

				if(clients.get(position) != null)
				{
					suscription	= realm.where(Suscription.class).equalTo(Suscription.KEY_API, clients.get(position).getCompanyId()).findFirst();
				}

				final Suscription item = suscription;

				if(item != null)
				{
					RealmResults<Message> countNotif = realm.where(Message.class).notEqualTo(Message.KEY_DELETED, Common.BOOL_YES).lessThan(Common.KEY_STATUS, Message.STATUS_SPAM)
														.equalTo(Suscription.KEY_API, item.getCompanyId()).findAllSorted(Message.KEY_CREATED, Sort.DESCENDING);
					
					if(item.getType() == Suscription.TYPE_FOLDER)
					{
						//Mostramos icono default de carpeta
						Picasso.with(activity.getHomeActivity()).load(R.drawable.ic_folder).into(holder.picture);
					}
					else
					{
						//Mostramos el logo de la company
						if(StringUtils.isNotEmpty(item.getImage()))
						{
							//Modificación de librería para recargar imagenes a mientras se está viendo el listado y optimizar vista
							Picasso.with(activity.getHomeActivity()).load(item.getImage()).placeholder(R.mipmap.ic_launcher).into(holder.picture);
						}
						else
						{
							//Mostrar el logo de Vloom si no tiene logo
							Picasso.with(activity.getHomeActivity()).load(Suscription.ICON_APP).placeholder(R.mipmap.ic_launcher).into(holder.picture);
						}
					}

					String name		= item.getName();
					String industry	= item.getIndustry();

					if(activity.getResources().getDisplayMetrics().density == Common.DENSITY_XHDPI)
					{
						//XHDPI
						if(name.length() > 25)
						{
							name = name.substring(0, 20).trim() + "...";
						}

						if(industry.length() > 28)
						{
							industry = industry.substring(0, 20).trim() + "...";
						}
					}
					else
					{
						//Agregado para nombres de company largos en fullhd
						if(activity.getResources().getDisplayMetrics().density == Common.DENSITY_XXHDPI)
						{
							//XXHDPI
							if(name.length() > 32)
							{
								name = name.substring(0, 30).trim() + "...";
							}
						}
						else
						{
							//Agregado para nombres de company largos en resoluciones menores a HD
							if(activity.getResources().getDisplayMetrics().density <= Common.DENSITY_HDPI)
							{
								//HDPI
								if(name.length() > 25)
								{
									name = name.substring(0, 14).trim() + "...";
								}

								if(industry.length() > 28)
								{
									industry = industry.substring(0, 14).trim() + "...";
								}
							}
						}
					}

					holder.title.setText(name);
					holder.subTitle.setText(industry);
					holder.rowTime.setText("");
					holder.bigSilence.setVisibility(ImageView.GONE);
					holder.bigPrice.setVisibility(ImageView.GONE);
					int unread = 0;
					
					//Se agregan los iconos para destacar cuando la company está bloqueada y se hizo dismiss de la card para suscribir
					if(item.getGray() == Common.BOOL_YES)
					{
						holder.title.setTextColor(Utils.adjustAlpha(Color.BLACK, Common.ALPHA_FOR_BLOCKS));
						holder.subTitle.setTextColor(Utils.adjustAlpha(Color.GRAY, Common.ALPHA_FOR_BLOCKS));
						holder.bigBlock.setVisibility(ImageView.VISIBLE);
						holder.price.setVisibility(ImageView.GONE);
						holder.silence.setVisibility(ImageView.GONE);
					}

					holder.rlClient.setOnClickListener(new View.OnClickListener()
					{
						@Override
						public void onClick(View v)
						{
							activity.redirectCard(item.getCompanyId());
						}
					});

					holder.rlClient.setOnLongClickListener(new View.OnLongClickListener()
					{
						@Override
						public boolean onLongClick(View v)
						{
							if(item.getType() == Suscription.TYPE_FOLDER)
							{
								activity.generateFolder(item.getCompanyId());
							}
							else
							{
								activity.showMenu(item, item.getCompanyId());
							}
							return true;
						}
					});

					if(countNotif.size() > 0)
					{
						//Agregado para mejora en la performance
						for(Message message : countNotif)
						{
							if(message.getStatus() == Message.STATUS_RECEIVE)
							{
								unread++;
							}
						}

						String time = DateUtils.getTimeFromTs(countNotif.get(0).getCreated(), activity.getActivity());

						if(StringUtils.isNotEmpty(time))
						{
							holder.rowTime.setText(time);
						}

						if(StringUtils.isNotEmpty(countNotif.get(0).getType()))
						{
							holder.subTitle.setText(StringUtils.capitaliseString(countNotif.get(0).getType()));
						}

						if(unread > 0)
						{
							holder.count.setVisibility(ImageView.VISIBLE);
							TextDrawable drawableCount = TextDrawable
								.builder()
								.beginConfig()
								.endConfig()
								.buildRound(String.valueOf(unread), Color.parseColor(Common.COLOR_ACCENT));
							holder.count.setImageDrawable(drawableCount);

							if(SuscriptionHelper.getTypeNumber(item, countNotif.get(0).getChannel(), activity.getContext()).equals(Suscription.NUMBER_PAYOUT))
							{
								holder.price.setVisibility(ImageView.VISIBLE);
								holder.silence.setVisibility(ImageView.GONE);
							}
							else
							{
								holder.price.setVisibility(ImageView.GONE);
							}

							if(item.getSilenced() == Common.BOOL_YES)
							{
								holder.silence.setVisibility(ImageView.VISIBLE);
								holder.price.setVisibility(ImageView.GONE);
							}
							else
							{
								holder.silence.setVisibility(ImageView.GONE);
							}
						}
						else
						{
							holder.count.setVisibility(ImageView.GONE);
							holder.silence.setVisibility(ImageView.GONE);
							holder.price.setVisibility(ImageView.GONE);

							if(SuscriptionHelper.getTypeNumber(item, countNotif.get(0).getChannel(), activity.getContext()).equals(Suscription.NUMBER_PAYOUT))
							{
								holder.bigPrice.setVisibility(ImageView.VISIBLE);
								holder.bigSilence.setVisibility(ImageView.GONE);
							}
							else
							{
								holder.bigPrice.setVisibility(ImageView.GONE);
							}

							if(item.getSilenced() == Common.BOOL_YES)
							{
								holder.bigSilence.setVisibility(ImageView.VISIBLE);
								holder.bigPrice.setVisibility(ImageView.GONE);
							}
							else
							{
								holder.bigSilence.setVisibility(ImageView.GONE);
							}
						}
					}
				}
			}
		}
		catch(Exception e)
		{
			Utils.logError(activity.getContext(), "HomeAdapter:onBindViewHolder - Exception:", e);
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