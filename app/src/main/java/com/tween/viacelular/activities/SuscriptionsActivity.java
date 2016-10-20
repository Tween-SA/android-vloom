package com.tween.viacelular.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import com.tween.viacelular.R;
import com.tween.viacelular.adapters.RecyclerAdapter;
import com.tween.viacelular.adapters.RecyclerItemClickListener;
import com.tween.viacelular.fragments.SuscriptionsFragment;
import com.tween.viacelular.models.Suscription;
import com.tween.viacelular.utils.Common;
import com.tween.viacelular.utils.Utils;
import java.util.ArrayList;
import java.util.List;
import java.util.WeakHashMap;
import io.realm.Realm;

/**
 * Created by davidfigueroa on 13/1/16.
 */
public class SuscriptionsActivity extends AppCompatActivity
{
	public RecyclerView					mRecyclerView;
	public Intent						intentRecive;
	private Toolbar						toolBar;
	private Activity					context;
	public RecyclerView.Adapter			mAdapter				= null;
	public WeakHashMap<View,Integer>	mOriginalViewHeightPool	= new WeakHashMap<>();

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		try
		{
			super.onCreate(savedInstanceState);
			setContentView(R.layout.activity_suscriptions);
			toolBar						= (Toolbar) findViewById(R.id.toolbar);
			mRecyclerView				= (RecyclerView) findViewById(R.id.menu);
			mRecyclerView.setHasFixedSize(true);
			intentRecive				= getIntent();
			setSupportActionBar(toolBar);
			ViewPager viewPager			= (ViewPager) findViewById(R.id.viewPager);
			PagerAdapter pagerAdapter	= new PagerAdapter(getSupportFragmentManager());
			pagerAdapter.addFragment(SuscriptionsFragment.createInstance(1, SuscriptionsActivity.this), getString(R.string.title_companies_subscriptions));
			pagerAdapter.addFragment(SuscriptionsFragment.createInstance(2, SuscriptionsActivity.this), getString(R.string.title_companies_all));
			viewPager.setAdapter(pagerAdapter);
			TabLayout tabLayout			= (TabLayout) findViewById(R.id.tabLayout);
			tabLayout.setupWithViewPager(viewPager);
			context						= SuscriptionsActivity.this;
			updateMenu();
		}
		catch(Exception e)
		{
			System.out.println("SuscriptionsActivity:onCreate - Exception: " + e);

			if(Common.DEBUG)
			{
				e.printStackTrace();
			}
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onResume()
	{
		//Agregado para capturar excepciones
		try
		{
			super.onResume();

			if(mRecyclerView != null && mAdapter != null)
			{
				updateMenu();
			}
		}
		catch(Exception e)
		{
			System.out.println("SuscriptionsActivity:onResume - Exception: " + e);

			if(Common.DEBUG)
			{
				e.printStackTrace();
			}
		}
	}

	/**
	 * Separación de código para generar el menú lateral
	 */
	public void updateMenu()
	{
		try
		{
			mAdapter = new RecyclerAdapter(Utils.getMenu(	getApplicationContext()), intentRecive.getIntExtra(Common.KEY_SECTION, RecyclerAdapter.SUSCRIPTION_SELECTED),
															ContextCompat.getColor(getApplicationContext(), R.color.accent), getApplicationContext());
			mRecyclerView.setAdapter(mAdapter);
			RecyclerView.LayoutManager mLayoutManager	= new LinearLayoutManager(this);
			mRecyclerView.setLayoutManager(mLayoutManager);
			DrawerLayout Drawer							= (DrawerLayout) findViewById(R.id.DrawerLayout);

			ActionBarDrawerToggle mDrawerToggle = new ActionBarDrawerToggle(this, Drawer, toolBar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
			{
				@Override
				public void onDrawerOpened(View drawerView)
				{
					super.onDrawerOpened(drawerView);
				}

				@Override
				public void onDrawerClosed(View drawerView)
				{
					super.onDrawerClosed(drawerView);
				}
			};

			mDrawerToggle.syncState();
			mRecyclerView.addOnItemTouchListener(new RecyclerItemClickListener(getApplicationContext(),
				new RecyclerItemClickListener.OnItemClickListener()
				{
					@Override
					public void onItemClick(View view, int position)
					{
						Utils.redirectMenu(context, position, intentRecive.getIntExtra(Common.KEY_SECTION, 0));
					}
				})
			);
		}
		catch(Exception e)
		{
			System.out.println("SuscriptionsActivity:updateMenu - Exception: " + e);

			if(Common.DEBUG)
			{
				e.printStackTrace();
			}
		}
	}

	/**
	 * Redirige a la configuración o landing de una company según si está suscripto o no
	 * @param activity
	 */
	public static void redirectLanding(Activity activity, String companyId)
	{
		try
		{
			//Unificación de landing activity
			Intent intent		= new Intent(activity, LandingActivity.class);
			Realm realm				= Realm.getDefaultInstance();
			Suscription client	= realm.where(Suscription.class).equalTo(Suscription.KEY_API, companyId).findFirst();
			intent.putExtra(Common.KEY_ID, client.getCompanyId());
			intent.putExtra(Common.KEY_SECTION, "companies");
			activity.startActivity(intent);
			activity.finish();
		}
		catch(Exception e)
		{
			System.out.println("SuscriptionsActivity:redirectLanding - Exception: " + e);

			if(Common.DEBUG)
			{
				e.printStackTrace();
			}
		}
	}

	public static class PagerAdapter extends FragmentPagerAdapter
	{
		private final List<Fragment> fragmentList		= new ArrayList<>();
		private final List<String> fragmentTitleList	= new ArrayList<>();

		public PagerAdapter(FragmentManager fragmentManager)
		{
			super(fragmentManager);
		}

		public void addFragment(Fragment fragment, String title)
		{
			fragmentList.add(fragment);
			fragmentTitleList.add(title);
		}

		@Override
		public Fragment getItem(int position)
		{
			return fragmentList.get(position);
		}

		@Override
		public int getCount()
		{
			return fragmentList.size();
		}

		@Override
		public CharSequence getPageTitle(int position)
		{
			return fragmentTitleList.get(position);
		}
	}
}