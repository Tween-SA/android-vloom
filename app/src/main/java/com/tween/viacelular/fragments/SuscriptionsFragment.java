package com.tween.viacelular.fragments;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import com.tween.viacelular.R;
import com.tween.viacelular.activities.SuscriptionsActivity;
import com.tween.viacelular.adapters.SuscriptionsAdapter;
import com.tween.viacelular.models.Suscription;
import com.tween.viacelular.utils.Common;
import com.tween.viacelular.utils.StringUtils;
import com.tween.viacelular.utils.Utils;
import java.util.ArrayList;
import java.util.List;
import io.realm.Realm;
import io.realm.RealmResults;
import se.emilsjolander.stickylistheaders.StickyListHeadersListView;

/**
 * Manejador para fragmentos y tabs de pantalla antigua Empresas
 * Created by Tween (David Figueroa davo.figueroa@tween.com.ar) on 13/01/2016
 */
public class SuscriptionsFragment extends Fragment implements	AdapterView.OnItemClickListener, StickyListHeadersListView.OnHeaderClickListener,
																StickyListHeadersListView.OnStickyHeaderOffsetChangedListener, StickyListHeadersListView.OnStickyHeaderChangedListener
{
	private static final String			ARG_SECTION_NUMBER	= "section_number";
	private SuscriptionsActivity		activityContext;
	private int							section;
	private StickyListHeadersListView	stickyList;

	public SuscriptionsFragment()
	{
	}

	public static SuscriptionsFragment createInstance(int section, SuscriptionsActivity activityContext)
	{
		SuscriptionsFragment suscriptionsFragment	= new SuscriptionsFragment();
		Bundle bundle								= new Bundle();
		bundle.putInt(ARG_SECTION_NUMBER, section);
		suscriptionsFragment.setArguments(bundle);
		suscriptionsFragment.setActivityContext(activityContext);
		suscriptionsFragment.setSection(section);
		return suscriptionsFragment;
	}

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		View view = inflater.inflate(R.layout.fragment_suscriptions, container, false);

		try
		{
			if(Utils.checkSesion(activityContext, Common.ANOTHER_SCREEN))
			{
				stickyList	= (StickyListHeadersListView) view.findViewById(R.id.list);
				section		= getArguments().getInt(ARG_SECTION_NUMBER);
				stickyList.setOnItemClickListener(this);
				stickyList.setOnHeaderClickListener(this);
				stickyList.setOnStickyHeaderChangedListener(this);
				stickyList.setOnStickyHeaderOffsetChangedListener(this);
				stickyList.setAreHeadersSticky(true);

				if(Common.API_LEVEL >= Build.VERSION_CODES.M)
				{
					stickyList.setOnScrollChangeListener(new View.OnScrollChangeListener()
					{
						@Override
						public void onScrollChange(final View v, final int scrollX, final int scrollY, final int oldScrollX, final int oldScrollY)
						{
							stickyList.notifyAll();
						}
					});
				}

				populateList();
			}
		}
		catch(Exception e)
		{
			Utils.logError(activityContext, "SuscriptionsFragment:onResume - Exception:", e);
		}

		return view;
	}

	public void populateList()
	{
		try
		{
			Realm realm						= Realm.getDefaultInstance();
			RealmResults<Suscription> suscriptions;
			List<String> listSuscriptions	= new ArrayList<>();

			if(section == 1)
			{
				//Tab Añadidas
				suscriptions = realm.where(Suscription.class).equalTo(Suscription.KEY_FOLLOWER, Common.BOOL_YES).findAllSorted(Common.KEY_NAME);
			}
			else
			{
				//Tab Todas
				suscriptions = realm.where(Suscription.class).findAllSorted(Common.KEY_NAME);
			}

			suscriptions.sort(Common.KEY_NAME);

			if(suscriptions.size() > 0)
			{
				for(Suscription suscription : suscriptions)
				{
					//Agregado para evitar mostrar phantoms companies
					if(StringUtils.isIdMongo(suscription.getCompanyId()))
					{
						listSuscriptions.add(suscription.getCompanyId());
					}
					else
					{
						//Mostrar las carpetas creadas
						if(!StringUtils.isIdMongo(suscription.getCompanyId()) && suscription.getType() == Suscription.TYPE_FOLDER)
						{
							listSuscriptions.add(suscription.getCompanyId());
						}
					}
				}
			}

			SuscriptionsAdapter adapter = new SuscriptionsAdapter(listSuscriptions, activityContext, "suscriptions");
			stickyList.setAdapter(adapter);
		}
		catch(Exception e)
		{
			Utils.logError(activityContext, "SuscriptionsFragment:populateList - Exception:", e);
		}
	}

	@Override
	public void onHeaderClick(final StickyListHeadersListView l, final View header, final int itemPosition, final long headerId, final boolean currentlySticky)
	{
	}

	@Override
	public void onItemClick(final AdapterView<?> parent, final View view, final int position, final long id)
	{
	}

	@Override
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public void onStickyHeaderChanged(final StickyListHeadersListView l, final View header, final int itemPosition, final long headerId)
	{
		header.setAlpha(1);
	}

	@Override
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public void onStickyHeaderOffsetChanged(final StickyListHeadersListView l, final View header, final int offset)
	{
		try
		{
			if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
			{
				header.setAlpha(1 - (offset / (float) header.getMeasuredHeight()));
			}
		}
		catch(Exception e)
		{
			Utils.logError(activityContext, "SuscriptionsFragment:onStickyHeaderOffsetChanged - Exception:", e);
		}
	}

	public void setActivityContext(final SuscriptionsActivity activityContext)
	{
		this.activityContext = activityContext;
	}

	public void setSection(final int section)
	{
		this.section = section;
	}
}