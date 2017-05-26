package com.tween.viacelular.adapters;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.SectionIndexer;
import android.widget.TextView;
import com.squareup.picasso.Picasso;
import com.tween.viacelular.R;
import com.tween.viacelular.activities.LandingActivity;
import com.tween.viacelular.models.Suscription;
import com.tween.viacelular.utils.Common;
import com.tween.viacelular.utils.StringUtils;
import com.tween.viacelular.utils.Utils;
import java.util.ArrayList;
import java.util.List;
import de.hdodenhof.circleimageview.CircleImageView;
import io.realm.Realm;
import se.emilsjolander.stickylistheaders.StickyListHeadersAdapter;

/**
 * Adaptador para presentación de datos sobre empresas diferenciando añadidas del total del país del usuario
 * Created by Tween (David Figueroa davo.figueroa@tween.com.ar) on 14/01/2016
 */
public class SuscriptionsAdapter extends BaseAdapter implements StickyListHeadersAdapter, SectionIndexer
{
	private List<Suscription>	suscriptions	= new ArrayList<>();
	private Activity			activityContext;
	private int[]				mSectionIndices;
	private Character[]			mSectionLetters;
	private LayoutInflater		mInflater;
	private String				backTo;

	public SuscriptionsAdapter(List<String> itemList, Activity activityContext, String backTo)
	{
		Realm realm = Realm.getDefaultInstance();

		if(itemList.size() > 0)
		{
			for(String id: itemList)
			{
				this.suscriptions.add(realm.where(Suscription.class).equalTo(Suscription.KEY_API, id).findFirst());
			}
		}

		this.activityContext	= activityContext;
		mSectionIndices			= getSectionIndices();
		mSectionLetters			= getSectionLetters();
		mInflater				= LayoutInflater.from(activityContext);
		this.backTo				= backTo;
	}

	private int[] getSectionIndices()
	{
		int[] sections = new int[0];

		try
		{
			ArrayList<Integer> sectionIndices	= new ArrayList<>();
			char lastFirstChar					= '\0';

			if(suscriptions != null)
			{
				if(suscriptions.size() > 0)
				{
					if(suscriptions.get(0) != null)
					{
						if(StringUtils.isNotEmpty(suscriptions.get(0).getName()))
						{
							lastFirstChar = suscriptions.get(0).getName().charAt(0);
							sectionIndices.add(0);
						}
					}

					for(int i = 1; i < suscriptions.size(); i++)
					{
						if(suscriptions.get(i) != null)
						{
							if(suscriptions.get(i).getName() != null)
							{
								if(suscriptions.get(i).getName().charAt(0) != lastFirstChar)
								{
									lastFirstChar = suscriptions.get(i).getName().charAt(0);
									sectionIndices.add(i);
								}
							}
						}
					}
				}
			}

			sections = new int[sectionIndices.size()];

			for(int i = 0; i < sectionIndices.size(); i++)
			{
				sections[i] = sectionIndices.get(i);
			}
		}
		catch(Exception e)
		{
			Utils.logError(activityContext, "SuscriptionsAdapter:getSectionLetters - Exception:", e);
		}

		return sections;
	}

	private Character[] getSectionLetters()
	{
		Character[] letters = new Character[0];

		try
		{
			letters = new Character[mSectionIndices.length];

			if(suscriptions != null)
			{
				for(int i = 0; i < mSectionIndices.length; i++)
				{
					letters[i] = suscriptions.get(mSectionIndices[i]).getName().charAt(0);
				}
			}
		}
		catch(Exception e)
		{
			Utils.logError(activityContext, "SuscriptionsAdapter:getSectionLetters - Exception:", e);
		}

		return letters;
	}

	@Override
	public int getCount()
	{
		if(suscriptions != null)
		{
			return suscriptions.size();
		}

		return 0;
	}

	public View getView(final int position, View convertView, final ViewGroup parent)
	{
		try
		{
			ViewHolder holder;

			if(convertView == null)
			{
				holder					= new ViewHolder();
				convertView				= mInflater.inflate(R.layout.item_suscription, parent, false);
				holder.picture			= (CircleImageView) convertView.findViewById(R.id.circleView);
				holder.txtTitle			= (TextView) convertView.findViewById(R.id.rowText);
				holder.rlSuscription	= (RelativeLayout) convertView.findViewById(R.id.rlSuscription);
				convertView.setTag(holder);
			}
			else
			{
				holder = (ViewHolder) convertView.getTag();
			}

			if(suscriptions != null)
			{
				if(suscriptions.size() > 0)
				{
					Suscription item = suscriptions.get(position);

					if(item != null)
					{
						if(item.getType() == Suscription.TYPE_FOLDER)
						{
							//Mostramos icono default de carpeta
							Picasso.with(activityContext).load(R.drawable.ic_folder).into(holder.picture);
						}
						else
						{
							//Mostramos el logo de la company
							if(StringUtils.isNotEmpty(item.getImage()))
							{
								//Modificación de librería para recargar imagenes a mientras se está viendo el listado y optimizar vista
								Picasso.with(activityContext).load(item.getImage()).placeholder(R.mipmap.ic_launcher).into(holder.picture);
							}
							else
							{
								//Mostrar el logo de Vloom si no tiene logo
								Picasso.with(activityContext).load(Suscription.ICON_APP).placeholder(R.mipmap.ic_launcher).into(holder.picture);
							}
						}

						holder.txtTitle.setText(item.getName());
						final String companyId = item.getCompanyId();

						holder.picture.setOnClickListener(new View.OnClickListener()
						{
							@Override
							public void onClick(View v)
							{
								Intent intent		= new Intent(activityContext, LandingActivity.class);
								intent.putExtra(Common.KEY_ID, companyId);
								intent.putExtra(Common.KEY_SECTION, backTo);
								activityContext.startActivity(intent);
								activityContext.finish();
							}
						});

						holder.txtTitle.setOnClickListener(new View.OnClickListener()
						{
							@Override
							public void onClick(View v)
							{
								Intent intent		= new Intent(activityContext, LandingActivity.class);
								intent.putExtra(Common.KEY_ID, companyId);
								intent.putExtra(Common.KEY_SECTION, backTo);
								activityContext.startActivity(intent);
								activityContext.finish();
							}
						});

						//Agregado para extender comportamiento a celda completa
						holder.rlSuscription.setOnClickListener(new View.OnClickListener()
						{
							@Override
							public void onClick(View v)
							{
								Intent intent		= new Intent(activityContext, LandingActivity.class);
								intent.putExtra(Common.KEY_ID, companyId);
								intent.putExtra(Common.KEY_SECTION, backTo);
								activityContext.startActivity(intent);
								activityContext.finish();
							}
						});

						//Se modifican los colores para destacar cuando la company está bloqueada y se hizo dismiss de la card para suscribir
						if(item.getGray() == Common.BOOL_YES)
						{
							holder.txtTitle.setTextColor(Utils.adjustAlpha(Color.BLACK, Common.ALPHA_FOR_BLOCKS));
						}
					}
				}
			}
		}
		catch(Exception e)
		{
			Utils.logError(activityContext, "SuscriptionsAdapter:getView - Exception:", e);
		}

		return convertView;
	}

	public View getHeaderView(int position, View convertView, ViewGroup parent)
	{
		try
		{
			HeaderViewHolder holder;

			if(convertView == null)
			{
				holder		= new HeaderViewHolder();
				convertView	= mInflater.inflate(R.layout.header_letter, parent, false);
				holder.text	= (TextView) convertView.findViewById(R.id.text);
				convertView.setTag(holder);
			}
			else
			{
				holder = (HeaderViewHolder) convertView.getTag();
			}

			if(suscriptions != null)
			{
				if(suscriptions.size() > 0)
				{
					Suscription item = suscriptions.get(position);

					if(item != null)
					{
						if(StringUtils.isNotEmpty(item.getName()))
						{
							if(item.getName().length() >= 2)
							{
								String headerText = "" + item.getName().subSequence(0, 1).charAt(0);
								holder.text.setText(headerText);
							}
						}
					}
				}
			}
		}
		catch(Exception e)
		{
			Utils.logError(activityContext, "SuscriptionsAdapter:getHeaderView - Exception:", e);
		}

		return convertView;
	}

	public long getHeaderId(int position)
	{
		long id = 0;

		try
		{
			if(suscriptions != null)
			{
				if(suscriptions.size() > 0)
				{
					Suscription item = suscriptions.get(position);

					if(item != null)
					{
						if(StringUtils.isNotEmpty(item.getName()))
						{
							if(item.getName().length() >= 2)
							{
								id = item.getName().subSequence(0, 1).charAt(0);
							}
						}
					}
				}
			}
		}
		catch(Exception e)
		{
			Utils.logError(activityContext, "SuscriptionsAdapter:getHeaderId - Exception:", e);
		}

		return id;
	}

	@Override
	public int getPositionForSection(int sectionIndex)
	{
		if(mSectionIndices.length == 0)
		{
			return 0;
		}

		if(sectionIndex >= mSectionIndices.length)
		{
			sectionIndex = mSectionIndices.length - 1;
		}
		else
		{
			if(sectionIndex < 0)
			{
				sectionIndex = 0;
			}
		}

		return mSectionIndices[sectionIndex];
	}

	@Override
	public int getSectionForPosition(int position)
	{
		try
		{
			if(suscriptions != null)
			{
				Suscription company = suscriptions.get(position);

				if(company != null)
				{
					for(int i=0; i < mSectionIndices.length; i++)
					{
						if(position < mSectionIndices[i])
						{
							return i - 1;
						}
					}

					return mSectionIndices.length - 1;
				}
			}
		}
		catch(Exception e)
		{
			Utils.logError(activityContext, "SuscriptionsAdapter:getSectionForPosition - Exception:", e);
		}

		return 0;
	}

	private class HeaderViewHolder
	{
		TextView text;
	}

	private class ViewHolder
	{
		private CircleImageView	picture;
		public TextView			txtTitle;
		private RelativeLayout	rlSuscription;
	}

	@Override
	public Character[] getSections()
	{
		return mSectionLetters;
	}

	@Override
	public int getItemViewType(int position)
	{
		return 0;
	}

	public long getItemId(final int position)
	{
		return position;
	}

	public Suscription getItem(final int position)
	{
		if(suscriptions != null)
		{
			return suscriptions.get(position);
		}

		return null;
	}

	//Agregados para implementar funcionalidad de búsqueda
	public void animateTo(List<Suscription> models)
	{
		applyAndAnimateRemovals(models);
		applyAndAnimateAdditions(models);
		applyAndAnimateMovedItems(models);
	}

	private void applyAndAnimateRemovals(List<Suscription> newModels)
	{
		for(int i = suscriptions.size() - 1; i >= 0; i--)
		{
			final Suscription model = suscriptions.get(i);

			if(!newModels.contains(model))
			{
				removeItem(i);
			}
		}
	}

	private void applyAndAnimateAdditions(List<Suscription> newModels)
	{
		for(int i = 0, count = newModels.size(); i < count; i++)
		{
			final Suscription model = newModels.get(i);

			if(!suscriptions.contains(model))
			{
				addItem(i, model);
			}
		}
	}

	private void applyAndAnimateMovedItems(List<Suscription> newModels)
	{
		for(int toPosition = newModels.size() - 1; toPosition >= 0; toPosition--)
		{
			final Suscription model	= newModels.get(toPosition);
			final int fromPosition	= suscriptions.indexOf(model);

			if(fromPosition >= 0 && fromPosition != toPosition)
			{
				moveItem(fromPosition, toPosition);
			}
		}
	}

	private Suscription removeItem(int position)
	{
		return suscriptions.remove(position);
	}

	private void addItem(int position, Suscription model)
	{
		suscriptions.add(position, model);
	}

	private void moveItem(int fromPosition, int toPosition)
	{
		final Suscription model = suscriptions.remove(fromPosition);
		suscriptions.add(toPosition, model);
	}
}