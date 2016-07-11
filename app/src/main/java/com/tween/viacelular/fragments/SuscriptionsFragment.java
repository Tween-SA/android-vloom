package com.tween.viacelular.fragments;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.TextView;
import com.tween.viacelular.R;
import com.tween.viacelular.activities.SuscriptionsActivity;
import com.tween.viacelular.adapters.SuscriptionsAdapter;
import com.tween.viacelular.models.Suscription;
import com.tween.viacelular.utils.Common;
import com.tween.viacelular.utils.StringUtils;
import com.tween.viacelular.utils.Utils;
import java.util.ArrayList;
import java.util.List;
import io.realm.Case;
import io.realm.Realm;
import io.realm.RealmResults;
import se.emilsjolander.stickylistheaders.StickyListHeadersListView;

/**
 * Created by davidfigueroa on 13/1/16.
 */
public class SuscriptionsFragment extends Fragment implements	AdapterView.OnItemClickListener, StickyListHeadersListView.OnHeaderClickListener,
																StickyListHeadersListView.OnStickyHeaderOffsetChangedListener, StickyListHeadersListView.OnStickyHeaderChangedListener
{
	private static final String			ARG_SECTION_NUMBER	= "section_number";
	private SuscriptionsActivity		activityContext;
	private int							section;
	private SuscriptionsAdapter			adapter				= null;
	private StickyListHeadersListView	stickyList;
	private TextInputLayout				inputFilter;
	private FloatingActionButton		fab;
	private int							originalSoftInputMode;
	private EditText					editFilter;

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
				inputFilter	= (TextInputLayout) view.findViewById(R.id.inputFilter);
				editFilter	= (EditText) view.findViewById(R.id.editFilter);
				fab			= (FloatingActionButton) view.findViewById(R.id.fab);
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

				//Agregado para activar búsqueda
				fab.setOnClickListener(new View.OnClickListener()
				{
					@Override
					public void onClick(View view)
					{
						enableFilter();
					}
				});

				editFilter.setOnEditorActionListener(new TextView.OnEditorActionListener()
				{
					@Override
					public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent)
					{
						//Ejecutar consulta filtrada
						populateList();
						hideSoftKeyboard();
						return true;
					}
				});

				disableFilter();
				populateList();
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

	public void populateList()
	{
		try
		{
			Realm realm								= Realm.getDefaultInstance();
			RealmResults<Suscription> suscriptions	= null;
			List<String> listSuscriptions			= new ArrayList<>();

			if(section == 1)
			{
				//Tab Añadidas
				if(editFilter != null)
				{
					if(StringUtils.isNotEmpty(editFilter.getText().toString()))
					{
						suscriptions = realm.where(Suscription.class).equalTo(Suscription.KEY_FOLLOWER, Common.BOOL_YES).contains(Common.KEY_NAME, editFilter.getText().toString(), Case.INSENSITIVE)
								.findAllSorted(Common.KEY_NAME);
					}
					else
					{
						suscriptions = realm.where(Suscription.class).equalTo(Suscription.KEY_FOLLOWER, Common.BOOL_YES).findAllSorted(Common.KEY_NAME);
					}
				}
				else
				{
					suscriptions = realm.where(Suscription.class).equalTo(Suscription.KEY_FOLLOWER, Common.BOOL_YES).findAllSorted(Common.KEY_NAME);
				}
			}
			else
			{
				//Tab Todas
				if(editFilter != null)
				{
					if(StringUtils.isNotEmpty(editFilter.getText().toString()))
					{
						suscriptions = realm.where(Suscription.class).contains(Common.KEY_NAME, editFilter.getText().toString(), Case.INSENSITIVE).findAllSorted(Common.KEY_NAME);
					}
					else
					{
						suscriptions = realm.where(Suscription.class).findAllSorted(Common.KEY_NAME);
					}
				}
				else
				{
					suscriptions = realm.where(Suscription.class).findAllSorted(Common.KEY_NAME);
				}
			}

			if(suscriptions != null)
			{
				suscriptions.sort(Common.KEY_NAME);

				if(suscriptions.size() > 0)
				{
					for(Suscription suscription: suscriptions)
					{
						//Agregado para evitar mostrar phantoms companies
						if(StringUtils.isIdMongo(suscription.getCompanyId()))
						{
							listSuscriptions.add(suscription.getCompanyId());
						}
					}
				}
			}

			adapter = new SuscriptionsAdapter(listSuscriptions, activityContext);
			stickyList.setAdapter(adapter);
		}
		catch(Exception e)
		{
			System.out.println("SuscriptionsFragment:onStickyHeaderOffsetChanged - Exception: " + e);

			if(Common.DEBUG)
			{
				e.printStackTrace();
			}
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
			System.out.println("SuscriptionsFragment:onStickyHeaderOffsetChanged - Exception: " + e);

			if(Common.DEBUG)
			{
				e.printStackTrace();
			}
		}
	}

	public void enableFilter()
	{
		try
		{
			if(fab != null)
			{
				fab.setImageResource(R.drawable.ic_clear_white_36dp);
				fab.setOnClickListener(new View.OnClickListener()
				{
					@Override
					public void onClick(View view)
					{
						disableFilter();
					}
				});
			}

			if(inputFilter != null)
			{
				inputFilter.setVisibility(TextInputLayout.VISIBLE);
				editFilter.requestFocus();
				inputFilter.requestFocus();
				showSoftKeyboard();
			}
		}
		catch(Exception e)
		{
			System.out.println("SuscriptionsFragment:enableFilter - Exception: " + e);

			if(Common.DEBUG)
			{
				e.printStackTrace();
			}
		}
	}

	public void disableFilter()
	{
		try
		{
			if(fab != null)
			{
				fab.setImageResource(R.drawable.ic_search_white_36dp);
				fab.setOnClickListener(new View.OnClickListener()
				{
					@Override
					public void onClick(View view)
					{
						enableFilter();
					}
				});
			}

			if(inputFilter != null)
			{
				inputFilter.setVisibility(TextInputLayout.GONE);

				if(editFilter != null)
				{
					if(StringUtils.isNotEmpty(editFilter.getText().toString()))
					{
						editFilter.setText("");
						populateList();
					}
				}
			}

			hideSoftKeyboard();
		}
		catch(Exception e)
		{
			System.out.println("SuscriptionsFragment:disableFilter - Exception: " + e);

			if(Common.DEBUG)
			{
				e.printStackTrace();
			}
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

	/**
	 * Agregado para esconder el teclado cuando se oprime back
	 */
	private void hideSoftKeyboard()
	{
		try
		{
			activityContext.getWindow().setSoftInputMode(originalSoftInputMode);

			// Hide keyboard when paused.
			View currentFocusView = activityContext.getCurrentFocus();

			if(currentFocusView != null)
			{
				InputMethodManager inputMethodManager = (InputMethodManager) activityContext.getSystemService(Context.INPUT_METHOD_SERVICE);
				inputMethodManager.hideSoftInputFromWindow(activityContext.getCurrentFocus().getWindowToken(), InputMethodManager.RESULT_UNCHANGED_SHOWN);
			}
		}
		catch(Exception e)
		{
			System.out.println("FeedbackActivity:hideSoftKeyboard - Exception: " + e);

			if(Common.DEBUG)
			{
				e.printStackTrace();
			}
		}
	}

	/**
	 * Agregado para mostrar el teclado
	 */
	private void showSoftKeyboard()
	{
		try
		{
			activityContext.getWindow().setSoftInputMode(originalSoftInputMode);

			// Hide keyboard when paused.
			View currentFocusView = activityContext.getCurrentFocus();

			if(currentFocusView != null)
			{
				InputMethodManager inputMethodManager = (InputMethodManager) activityContext.getSystemService(Context.INPUT_METHOD_SERVICE);
				inputMethodManager.showSoftInput(activityContext.getCurrentFocus(), InputMethodManager.RESULT_UNCHANGED_SHOWN);
			}
		}
		catch(Exception e)
		{
			System.out.println("FeedbackActivity:hideSoftKeyboard - Exception: " + e);

			if(Common.DEBUG)
			{
				e.printStackTrace();
			}
		}
	}
}