package com.tween.viacelular.activities;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.RelativeLayout;

import com.afollestad.materialdialogs.MaterialDialog;
import com.miguelcatalan.materialsearchview.MaterialSearchView;
import com.tween.viacelular.R;
import com.tween.viacelular.adapters.SuscriptionsAdapter;
import com.tween.viacelular.models.Land;
import com.tween.viacelular.models.Suscription;
import com.tween.viacelular.models.SuscriptionHelper;
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
	private StickyListHeadersListView	stickyList;
	private Toolbar						toolBar;
	private RelativeLayout				rlEmpty;
	private int							originalSoftInputMode;
	private String						section	= "";
	private String						filter	= "";
	private boolean						enabled	= false;

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState)
	{
		try
		{
			super.onCreate(savedInstanceState);
			setContentView(R.layout.activity_search);
			toolBar	= (Toolbar) findViewById(R.id.toolBar);
			rlEmpty	= (RelativeLayout) findViewById(R.id.rlEmpty);
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
					populateList();
					enabled = false;
					searchView.clearFocus();
					searchView.setVisibility(View.VISIBLE);
					return true;
				}

				@Override
				public boolean onQueryTextChange(String newText)
				{
					if(enabled)
					{
						filter = newText;
						populateList();
					}
					else
					{
						if(searchView.isSearchOpen())
						{
							filter = "";
							populateList();
						}
					}

					return false;
				}
			});
			searchView.setOnSearchViewListener(new MaterialSearchView.SearchViewListener()
			{
				@Override
				public void onSearchViewShown()
				{
					populateList();
					toolBar.setVisibility(Toolbar.INVISIBLE);
					showSoftKeyboard();
					enabled = true;
				}

				@Override
				public void onSearchViewClosed()
				{
					hideSoftKeyboard();
					onBackPressed();
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
			
			Utils.tintColorScreen(this, Common.COLOR_SEARCH);
		}
		catch(Exception e)
		{
			Utils.logError(this, getLocalClassName()+":onCreate - Exception:", e);
		}
	}
	
	public void generateFolder(View view)
	{
		try
		{
			final Activity activity = this;
			new MaterialDialog.Builder(this).title(getString(R.string.folder_btn)).inputType(InputType.TYPE_CLASS_TEXT)
				.positiveText(R.string.enrich_save).cancelable(true).inputRange(0, 20).positiveColor(Color.parseColor(Common.COLOR_COMMENT))
				.input(getString(R.string.folder_hint), "", new MaterialDialog.InputCallback()
				{
					@Override
					public void onInput(@NonNull MaterialDialog dialog, CharSequence input)
					{
						if(input != null)
						{
							if(input != "")
							{
								final String name = input.toString().trim();
								
								if(StringUtils.isNotEmpty(name))
								{
									SharedPreferences preferences = activity.getSharedPreferences(Common.KEY_PREF, Context.MODE_PRIVATE);
									SuscriptionHelper.createPhantom(name, activity, preferences.getString(Land.KEY_API, ""), true);
									Handler handler = new android.os.Handler();
									handler.post(new Runnable()
									{
										public void run()
										{
											populateList();
										}
									});
								}
							}
						}
					}
				}).show();
		}
		catch(Exception e)
		{
			Utils.logError(this, getLocalClassName()+":generateFolder - Exception:", e);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		toolBar.setVisibility(Toolbar.INVISIBLE);
		getMenuInflater().inflate(R.menu.menu_search, menu);
		MenuItem item = menu.findItem(R.id.action_search);
		searchView.setMenuItem(item);
		searchView.showSearch(true);
		return true;
	}

	public void populateList()
	{
		try
		{
			Realm realm						= Realm.getDefaultInstance();
			RealmResults<Suscription> suscriptions;
			List<String> listSuscriptions	= new ArrayList<>();

			if(StringUtils.isNotEmpty(filter))
			{
				//Filtradas
				suscriptions = realm.where(Suscription.class).contains(Common.KEY_NAME, filter, Case.INSENSITIVE).findAllSorted(Common.KEY_NAME);
			}
			else
			{
				//Todas
				suscriptions = realm.where(Suscription.class).findAllSorted(Common.KEY_NAME);
			}

			suscriptions.sort(Common.KEY_NAME);

			if(suscriptions.size() > 0)
			{
				rlEmpty.setVisibility(RelativeLayout.GONE);
				
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
			}
			else
			{
				rlEmpty.setVisibility(RelativeLayout.VISIBLE);
			}

			String backTo;

			if(section.equals("suscriptions"))
			{
				backTo = "search";
			}
			else
			{
				backTo = "searchHome";
			}

			SuscriptionsAdapter adapter = new SuscriptionsAdapter(listSuscriptions, this, backTo);
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

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
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