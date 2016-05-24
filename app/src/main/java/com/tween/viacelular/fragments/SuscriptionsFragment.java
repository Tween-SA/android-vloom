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
import com.tween.viacelular.utils.Utils;
import java.util.ArrayList;
import java.util.List;
import io.realm.Realm;
import io.realm.RealmResults;
import se.emilsjolander.stickylistheaders.StickyListHeadersListView;

/**
 * Created by davidfigueroa on 13/1/16.
 */
public class SuscriptionsFragment extends Fragment implements	AdapterView.OnItemClickListener, StickyListHeadersListView.OnHeaderClickListener,
																StickyListHeadersListView.OnStickyHeaderOffsetChangedListener, StickyListHeadersListView.OnStickyHeaderChangedListener
{
	private static final String		ARG_SECTION_NUMBER	= "section_number";
	private SuscriptionsActivity	activityContext;
	private int						section;

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
		View view			= inflater.inflate(R.layout.fragment_suscriptions, container, false);
		Realm realm			= null;

		try
		{
			if(Utils.checkSesion(activityContext, Common.ANOTHER_SCREEN))
			{
				realm										= Realm.getDefaultInstance();
				final StickyListHeadersListView stickyList	= (StickyListHeadersListView) view.findViewById(R.id.list);
				SuscriptionsAdapter adapter					= null;
				RealmResults<Suscription> allCompanies		= realm.where(Suscription.class).findAllSorted(Common.KEY_NAME);
				RealmResults<Suscription> suscriptions		= realm.where(Suscription.class).equalTo(Suscription.KEY_FOLLOWER, Common.BOOL_YES).findAllSorted(Common.KEY_NAME);
				List<String> listSuscriptions				= new ArrayList<>();
				List<String> listAll						= new ArrayList<>();

				if(suscriptions != null)
				{
					suscriptions.sort(Common.KEY_NAME);

					if(suscriptions.size() > 0)
					{
						for(Suscription suscription: suscriptions)
						{
							listSuscriptions.add(suscription.getCompanyId());
						}
					}
				}

				if(allCompanies != null)
				{
					allCompanies.sort(Common.KEY_NAME);

					if(allCompanies.size() > 0)
					{
						for(Suscription suscription: allCompanies)
						{
							listAll.add(suscription.getCompanyId());
						}
					}
				}

				if(getArguments().getInt(ARG_SECTION_NUMBER) == 1)
				{
					adapter = new SuscriptionsAdapter(listSuscriptions, activityContext);
				}
				else
				{
					adapter = new SuscriptionsAdapter(listAll, activityContext);
				}

				stickyList.setOnItemClickListener(this);
				stickyList.setOnHeaderClickListener(this);
				stickyList.setOnStickyHeaderChangedListener(this);
				stickyList.setOnStickyHeaderOffsetChangedListener(this);
				stickyList.setAreHeadersSticky(true);
				//Se quitó el seteo en true de las propiedades FastScrollAlwaysVisible, FastScrollEnabled, DrawingListUnderStickyHeader para mejorar performance y quitar el restaltado
				stickyList.setAdapter(adapter);

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
			}
		}
		catch(Exception e)
		{
			System.out.println("SuscriptionsFragment:onResume - Exception: " + e);
			if(Common.DEBUG)
			{
				e.printStackTrace();
			}
		}

		return view;
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
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
		{
			header.setAlpha(1 - (offset / (float) header.getMeasuredHeight()));
		}
	}

	public SuscriptionsActivity getActivityContext()
	{
		return activityContext;
	}

	public void setActivityContext(final SuscriptionsActivity activityContext)
	{
		this.activityContext = activityContext;
	}

	public int getSection()
	{
		return section;
	}

	public void setSection(final int section)
	{
		this.section = section;
	}
}