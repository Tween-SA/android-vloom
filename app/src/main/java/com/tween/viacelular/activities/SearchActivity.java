package com.tween.viacelular.activities;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import com.miguelcatalan.materialsearchview.MaterialSearchView;
import com.tween.viacelular.R;
import com.tween.viacelular.utils.Common;
import java.util.ArrayList;

public class SearchActivity extends AppCompatActivity
{
	private SearchView searchView;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		try
		{
			super.onCreate(savedInstanceState);
			setContentView(R.layout.activity_search);

			Toolbar toolBar = (Toolbar) findViewById(R.id.toolBar);
			setSupportActionBar(toolBar);
			handleIntent(getIntent());

			searchView = (SearchView) findViewById(R.id.searchView);
			SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
			searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
			searchView.setIconifiedByDefault(true);
			searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener()
			{
				@Override
				public boolean onQueryTextSubmit(String query)
				{
					filter(query);
					return false;
				}

				@Override
				public boolean onQueryTextChange(String newText)
				{
					return false;
				}
			});

			toolBar.setNavigationOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick(final View v)
				{
					onBackPressed();
				}
			});
		}
		catch(Exception e)
		{
			System.out.println("SearchActivity:OnCreate - Exception: " + e);

			if(Common.DEBUG)
			{
				e.printStackTrace();
			}
		}
	}

	@Override
	protected void onNewIntent(Intent intent)
	{
		setIntent(intent);
		handleIntent(intent);
	}

	private void handleIntent(Intent intent)
	{
		if(intent != null)
		{
			if(Intent.ACTION_SEARCH.equals(intent.getAction()))
			{
				String query = intent.getStringExtra(SearchManager.QUERY);
				filter(query);
			}
		}
	}

	public void filter(String text)
	{
		System.out.println("filter: " + text);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		try
		{
			getMenuInflater().inflate(R.menu.menu_subscriptions, menu);
			MenuItem item = menu.findItem(R.id.menuSearch);
		}
		catch(Exception e)
		{
			System.out.println("SearchActivity:onCreateOptionsMenu - Exception: " + e);

			if(Common.DEBUG)
			{
				e.printStackTrace();
			}
		}

		return true;
	}

	@Override
	public void onBackPressed()
	{
		try
		{
			Intent intent = new Intent(getApplicationContext(), SuscriptionsActivity.class);
			intent.putExtra(Common.KEY_TITLE, getString(R.string.title_companies));
			intent.putExtra(Common.KEY_SECTION, 2);
			startActivity(intent);
		}
		catch(Exception e)
		{
			System.out.println("SearchActivity:onBackPressed - Exception: " + e);

			if(Common.DEBUG)
			{
				e.printStackTrace();
			}
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		try
		{
			if(requestCode == MaterialSearchView.REQUEST_VOICE && resultCode == RESULT_OK)
			{
				ArrayList<String> matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

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
		catch(Exception e)
		{
			System.out.println("SearchActivity:onActivityResult - Exception: " + e);

			if(Common.DEBUG)
			{
				e.printStackTrace();
			}
		}
	}
}
