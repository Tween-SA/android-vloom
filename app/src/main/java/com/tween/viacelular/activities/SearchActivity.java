package com.tween.viacelular.activities;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;

import com.miguelcatalan.materialsearchview.MaterialSearchView;
import com.tween.viacelular.R;
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
 * Created by davidfigueroa on 7/2/17.
 */

public class SearchActivity extends AppCompatActivity implements	AdapterView.OnItemClickListener, StickyListHeadersListView.OnHeaderClickListener,
																	StickyListHeadersListView.OnStickyHeaderOffsetChangedListener, StickyListHeadersListView.OnStickyHeaderChangedListener
{
	private MaterialSearchView			searchView;
	private String						section	= "";
	private StickyListHeadersListView	stickyList;
	private Toolbar						toolBar;
	private int							originalSoftInputMode;

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState)
	{
		try
		{
			super.onCreate(savedInstanceState);
			setContentView(R.layout.activity_search);
			toolBar = (Toolbar) findViewById(R.id.toolBar);
			setSupportActionBar(toolBar);
			toolBar.setNavigationIcon(R.drawable.back);
			toolBar.setNavigationOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick(final View v)
				{
					onBackPressed();
				}
			});
			stickyList	= (StickyListHeadersListView) findViewById(R.id.list);
			stickyList.setOnItemClickListener(this);
			stickyList.setOnHeaderClickListener(this);
			stickyList.setOnStickyHeaderChangedListener(this);
			stickyList.setOnStickyHeaderOffsetChangedListener(this);
			stickyList.setAreHeadersSticky(true);
			searchView	= (MaterialSearchView) findViewById(R.id.search_view);
			searchView.setVoiceSearch(false);
			searchView.setCursorDrawable(R.drawable.custom_cursor);
			searchView.setEllipsize(true);
			searchView.setOnQueryTextListener(new MaterialSearchView.OnQueryTextListener()
			{
				@Override
				public boolean onQueryTextSubmit(String query)
				{
					System.out.println("onQueryTextSubmit: "+query);
					populateList(query);
					return false;
				}

				@Override
				public boolean onQueryTextChange(String newText)
				{
					System.out.println("onQueryTextChange: "+newText);
					populateList(newText);
					return false;
				}
			});
			searchView.setOnSearchViewListener(new MaterialSearchView.SearchViewListener()
			{
				@Override
				public void onSearchViewShown()
				{
					toolBar.setVisibility(Toolbar.GONE);
					System.out.println("onSearchViewShown");
					showSoftKeyboard();
				}

				@Override
				public void onSearchViewClosed()
				{
					hideSoftKeyboard();
					toolBar.setVisibility(Toolbar.VISIBLE);
					System.out.println("onSearchViewClosed");
				}
			});
			final Intent intentRecive = getIntent();

			if(intentRecive != null)
			{
				section = intentRecive.getStringExtra(Common.KEY_SECTION);
			}

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

			populateList("");
		}
		catch(Exception e)
		{
			Utils.logError(this, getLocalClassName()+":onCreate - Exception:", e);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		toolBar.setVisibility(Toolbar.GONE);
		getMenuInflater().inflate(R.menu.menu_search, menu);
		MenuItem item = menu.findItem(R.id.action_search);
		searchView.setMenuItem(item);
		searchView.showSearch(true);
		return true;
	}

	public void populateList(String filter)
	{
		try
		{
			Realm realm						= Realm.getDefaultInstance();
			RealmResults<Suscription> suscriptions;
			List<String> listSuscriptions	= new ArrayList<>();

			if(StringUtils.isNotEmpty(filter))
			{
				//Tab Añadidas
				suscriptions = realm.where(Suscription.class).contains(Common.KEY_NAME, filter, Case.INSENSITIVE).findAllSorted(Common.KEY_NAME);
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
					if(StringUtils.isIdMongo(suscription.getCompanyId()))
					{
						listSuscriptions.add(suscription.getCompanyId());
					}
					else
					{
						if(StringUtils.isCompanyNumber(suscription.getName()))
						{
							listSuscriptions.add(suscription.getCompanyId());
						}
					}
				}
			}

			SuscriptionsAdapter adapter = new SuscriptionsAdapter(listSuscriptions, this);
			stickyList.setAdapter(adapter);
		}
		catch(Exception e)
		{
			Utils.logError(this, getLocalClassName()+":populateList - Exception:", e);
		}
	}

	@Override
	public void onBackPressed()
	{
		if(searchView.isSearchOpen())
		{
			searchView.closeSearch();
		}
		else
		{
			Intent intent;

			if(section.equals("suscriptions"))
			{
				intent = new Intent(getApplicationContext(), SuscriptionsActivity.class);
			}
			else
			{
				intent = new Intent(getApplicationContext(), HomeActivity.class);
			}

			startActivity(intent);
			finish();
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		System.out.println("onActivityResult: "+requestCode+" resultCode: "+resultCode);
		if(requestCode == MaterialSearchView.REQUEST_VOICE && resultCode == RESULT_OK)
		{
			ArrayList<String> matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
			System.out.println("matches: "+matches);

			if(matches != null && matches.size() > 0)
			{
				String searchWrd = matches.get(0);

				if(!TextUtils.isEmpty(searchWrd))
				{
					searchView.setQuery(searchWrd, false);
				}
			}

			return;
		}

		super.onActivityResult(requestCode, resultCode, data);
	}

	private void hideSoftKeyboard()
	{
		try
		{
			getWindow().setSoftInputMode(originalSoftInputMode);
			View currentFocusView = getCurrentFocus();

			if(currentFocusView != null)
			{
				InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.RESULT_UNCHANGED_SHOWN);
			}
		}
		catch(Exception e)
		{
			Utils.logError(this, getLocalClassName()+":hideSoftKeyboard - Exception:", e);
		}
	}

	/**
	 * Agregado para mostrar el teclado
	 */
	private void showSoftKeyboard()
	{
		try
		{
			getWindow().setSoftInputMode(originalSoftInputMode);

			// Hide keyboard when paused.
			View currentFocusView = getCurrentFocus();

			if(currentFocusView != null)
			{
				InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				inputMethodManager.showSoftInput(getCurrentFocus(), InputMethodManager.RESULT_UNCHANGED_SHOWN);
			}
		}
		catch(Exception e)
		{
			Utils.logError(this, getLocalClassName()+":showSoftKeyboard - Exception:", e);
		}
	}

	@Override
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public void onStickyHeaderChanged(final StickyListHeadersListView l, final View header, final int itemPosition, final long headerId)
	{
		header.setAlpha(1);
	}

	@Override
	public void onItemClick(AdapterView<?> adapterView, View view, int i, long l)
	{
	}

	@Override
	public void onHeaderClick(StickyListHeadersListView l, View header, int itemPosition, long headerId, boolean currentlySticky)
	{
	}

	@Override
	public void onStickyHeaderOffsetChanged(StickyListHeadersListView l, View header, int offset)
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
			Utils.logError(this, getLocalClassName()+":onStickyHeaderOffsetChanged - Exception:", e);
		}
	}
}