package com.tween.viacelular.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.tween.viacelular.R;
import com.tween.viacelular.activities.CardViewActivity;
import com.tween.viacelular.activities.HomeActivity;
import com.tween.viacelular.activities.SearchActivity;
import com.tween.viacelular.activities.VerifyPhoneActivity;
import com.tween.viacelular.adapters.HomeAdapter;
import com.tween.viacelular.adapters.IconOptionAdapter;
import com.tween.viacelular.asynctask.GetTweetsAsyncTask;
import com.tween.viacelular.models.MessageHelper;
import com.tween.viacelular.models.Suscription;
import com.tween.viacelular.models.SuscriptionHelper;
import com.tween.viacelular.utils.Common;
import com.tween.viacelular.utils.DateUtils;
import com.tween.viacelular.utils.StringUtils;
import com.tween.viacelular.utils.Utils;

import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;

/**
 * A basic sample that shows how to use {@link android.support.v4.widget.SwipeRefreshLayout} to add the 'swipe-to-refresh' gesture to a layout.
 * In this sample, SwipeRefreshLayout contains a scrollable {@link android.widget.ListView} as its only child.
 *
 * <p>To provide an accessible way to trigger the refresh, this app also provides a refresh action item.
 * <p>In this sample app, the refresh updates the ListView with a random set of new items.
 * Created by davidfigueroa on 17/11/15.
 */
public class SwipeRefreshLayoutBasicFragment extends Fragment
{
	private SwipeRefreshLayout	mSwipeRefreshLayout;
	private RecyclerView		rcwHome;
	private boolean				clicked		= false;
	private boolean				started		= false;
	private MaterialDialog		list		= null;
	private List<Suscription>	companies	= new ArrayList<>();
	private CoordinatorLayout	clayout;
	private HomeAdapter			adapter;
	private Activity			activity;
	private RelativeLayout		rlEmpty;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		try
		{
			super.onCreate(savedInstanceState);
			setHasOptionsMenu(true);
		}
		catch(Exception e)
		{
			Utils.logError(getHomeActivity(), "SwipeRefreshLayoutBasicFragment:onCreate - Exception:", e);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		View view = inflater.inflate(R.layout.fragment_home, container, false);

		try
		{
			RelativeLayout rlFreePass					= (RelativeLayout) view.findViewById(R.id.rlFreePass);
			mSwipeRefreshLayout							= (SwipeRefreshLayout) view.findViewById(R.id.swiperefresh);
			mSwipeRefreshLayout.setColorSchemeResources(R.color.swipe_color_1, R.color.swipe_color_2, R.color.swipe_color_3, R.color.accent);
			rcwHome										= (RecyclerView) view.findViewById(R.id.rcwHome);
			rcwHome.setHasFixedSize(true);
			RecyclerView.LayoutManager mLayoutManager	= new LinearLayoutManager(getActivity());
			rcwHome.setLayoutManager(mLayoutManager);
			rlEmpty										= (RelativeLayout) view.findViewById(R.id.rlEmpty);
			SharedPreferences preferences				= getHomeActivity().getSharedPreferences(Common.KEY_PREF, Context.MODE_PRIVATE);

			if(preferences.getBoolean(Common.KEY_PREF_FREEPASS, false))
			{
				rlFreePass.setVisibility(RelativeLayout.VISIBLE);
				rlFreePass.setOnClickListener(new View.OnClickListener()
				{
					@Override
					public void onClick(View view)
					{
						Intent intent = new Intent(getHomeActivity(), VerifyPhoneActivity.class);
						getHomeActivity().startActivity(intent);
					}
				});
			}
			else
			{
				rlFreePass.setVisibility(RelativeLayout.GONE);
			}
		}
		catch(Exception e)
		{
			Utils.logError(getHomeActivity(), "SwipeRefreshLayoutBasicFragment:onCreateView - Exception:", e);
		}

		return view;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState)
	{
		try
		{
			super.onViewCreated(view, savedInstanceState);

			if(getHomeActivity() != null)
			{
				companies = SuscriptionHelper.getList(getHomeActivity());
			}

			if(companies != null)
			{
				if(companies.size() > 0)
				{
					adapter			= new HomeAdapter(this);
					Handler handler	= new android.os.Handler();
					handler.post(new Runnable()
					{
						public void run()
						{
							rcwHome.setHasFixedSize(true);
							RecyclerView.LayoutManager mLayoutManager	= new LinearLayoutManager(getActivity());
							rcwHome.setLayoutManager(mLayoutManager);
							rcwHome.setAdapter(adapter);
							rlEmpty.setVisibility(RelativeLayout.GONE);
							rcwHome.setVisibility(RecyclerView.VISIBLE);
						}
					});
				}
			}

			/**
			 * Implement {@link SwipeRefreshLayout.OnRefreshListener}. When users do the "swipe to refresh" gesture,
			 * SwipeRefreshLayout invokes {@link SwipeRefreshLayout.OnRefreshListener#onRefresh onRefresh()}.
			 * In {@link SwipeRefreshLayout.OnRefreshListener#onRefresh onRefresh()}, call a method that refreshes the content.
			 * Call the same method in response to the Refresh action from the action bar.
			 */
			mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener()
			{
				@Override
				public void onRefresh()
				{
					Handler handler = new android.os.Handler();
					handler.post(new Runnable()
					{
						public void run()
						{
							initiateRefresh(true, true);
						}
					});
				}
			});
		}
		catch(Exception e)
		{
			Utils.logError(getHomeActivity(), "SwipeRefreshLayoutBasicFragment:onViewCreated - Exception:", e);
		}
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
	{
		inflater.inflate(R.menu.menu_swipe, menu);
	}

	/**
	 * Respond to the user's selection of the Refresh action item. Start the SwipeRefreshLayout progress bar, then initiate the background task that refreshes the content.
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		try
		{
			switch(item.getItemId())
			{
				case R.id.action_search:
					GoogleAnalytics.getInstance(getHomeActivity()).newTracker(Common.HASH_GOOGLEANALYTICS)
							.send(new HitBuilders.EventBuilder().setCategory("Company").setAction("Filtro").setLabel("AccionUser").build());
					Intent intent = new Intent(getHomeActivity(), SearchActivity.class);
					intent.putExtra(Common.KEY_SECTION, "home");
					getHomeActivity().startActivity(intent);
					getHomeActivity().finish();
				break;
				
				case R.id.action_folder:
					new MaterialDialog.Builder(getHomeActivity()).title(getString(R.string.folder_btn)).inputType(InputType.TYPE_CLASS_TEXT)
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
										final String name = input.toString();
										
										if(StringUtils.isNotEmpty(name))
										{
											final Realm realm = Realm.getDefaultInstance();
											realm.executeTransactionAsync(new Realm.Transaction()
											{
												@Override
												public void execute(Realm bgRealm)
												{
													Suscription suscription = new Suscription();
													suscription.setName(name);
													bgRealm.copyToRealmOrUpdate(suscription);
												}
											}, new Realm.Transaction.OnSuccess()
											{
												@Override
												public void onSuccess()
												{
													refresh();
												}
											});
											realm.close();
										}
									}
								}
							}
						}).show();
				break;
				
				default:
					refresh();
				break;
			}
			
			return true;
		}
		catch(Exception e)
		{
			Utils.logError(getHomeActivity(), "SwipeRefreshLayoutBasicFragment:onOptionsItemSelected - Exception:", e);
		}

		return super.onOptionsItemSelected(item);
	}
	
	public void refresh()
	{
		Handler handler = new android.os.Handler();
		handler.post(new Runnable()
		{
			public void run()
			{
				initiateRefresh(true, true);
			}
		});
	}

	/**
	 * By abstracting the refresh process to a single method, the app allows both the SwipeGestureLayout onRefresh() method and the Refresh action item to refresh the content.
	 */
	private void initiateRefresh(final boolean bringOut, final boolean showDialog)
	{
		try
		{
			if(!started)
			{
				if(showDialog)
				{
					if(!mSwipeRefreshLayout.isRefreshing())
					{
						mSwipeRefreshLayout.setRefreshing(true);
					}
				}
				else
				{
					mSwipeRefreshLayout.setRefreshing(false);
				}

				started = true;

				if(bringOut)
				{
					new RefreshCompanyTask(getHomeActivity(), showDialog).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
				}
				else
				{
					onRefreshComplete();
				}
			}
		}
		catch(Exception e)
		{
			Utils.logError(getHomeActivity(), "SwipeRefreshLayoutBasicFragment:initiateRefresh - Exception:", e);
		}
	}

	public void showMenu(final Suscription client, final String companyId)
	{
		try
		{
			final Context context	= getHomeActivity();
			list					= new MaterialDialog.Builder(context)
										.adapter(new IconOptionAdapter(context, R.array.optionsChannel, companyId),
												new MaterialDialog.ListCallback()
												{
													@Override
													public void onSelection(MaterialDialog dialog, View itemView, int which, CharSequence text)
													{
														dispatchMenu(which, client, companyId);
													}
												})
										.show();
			clicked					= true;
		}
		catch(Exception e)
		{
			Utils.logError(getHomeActivity(), "SwipeRefreshLayoutBasicFragment:showMenu - Exception:", e);
		}
	}

	public void redirectCard(String companyId)
	{
		try
		{
			if(!clicked)
			{
				Realm realm				= Realm.getDefaultInstance();
				Suscription suscription	= realm.where(Suscription.class).equalTo(Suscription.KEY_API, companyId).findFirst();

				if(suscription != null)
				{
					if(StringUtils.isIdMongo(suscription.getCompanyId()))
					{
						//Se quita el loader y se previene la posibilidad de que la company no tenga Twitter asociado
						if(StringUtils.isNotEmpty(suscription.getTwitter()))
						{
							if(suscription.getLastSocialUpdated() != null)
							{
								//Solamente se pide una vez al día
								if(DateUtils.needUpdate(suscription.getLastSocialUpdated(), DateUtils.DAY_MILLIS, getHomeActivity()))
								{
									new GetTweetsAsyncTask(getActivity(), false, companyId).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
								}
								else
								{
									Intent intent = new Intent(getActivity(), CardViewActivity.class);
									intent.putExtra(Common.KEY_ID, companyId);
									startActivity(intent);
								}
							}
							else
							{
								new GetTweetsAsyncTask(getActivity(), false, companyId).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
							}
						}
						else
						{
							Intent intent = new Intent(getActivity(), CardViewActivity.class);
							intent.putExtra(Common.KEY_ID, companyId);
							startActivity(intent);
						}
					}
					else
					{
						Intent intent = new Intent(getActivity(), CardViewActivity.class);
						intent.putExtra(Common.KEY_ID, companyId);
						startActivity(intent);
					}
				}
				else
				{
					Intent intent = new Intent(getActivity(), CardViewActivity.class);
					intent.putExtra(Common.KEY_ID, companyId);
					startActivity(intent);
				}
			}
			else
			{
				clicked = false;
			}
		}
		catch(Exception e)
		{
			Utils.logError(getHomeActivity(), "SwipeRefreshLayoutBasicFragment:redirectCard - Exception:", e);
		}
	}

	public void dispatchMenu(int position, Suscription client, final String companyId)
	{
		final Context context	= getHomeActivity();

		try
		{
			Snackbar snackBar			= null;
			Realm realm					= Realm.getDefaultInstance();
			final Suscription company	= realm.where(Suscription.class).equalTo(Suscription.KEY_API, companyId).findFirst();

			switch(position)
			{
				case IconOptionAdapter.OPTION_SILENCED:
					final Suscription suscription = realm.where(Suscription.class).equalTo(Suscription.KEY_API, company.getCompanyId()).findFirst();
					realm.executeTransaction(new Realm.Transaction()
					{
						@Override
						public void execute(Realm realm)
						{
							if(suscription.getSilenced() == Common.BOOL_YES)
							{
								suscription.setSilenced(Common.BOOL_NO);
							}
							else
							{
								//Agregado para capturar evento en Google Analytics
								GoogleAnalytics.getInstance(getHomeActivity()).newTracker(Common.HASH_GOOGLEANALYTICS).send(new HitBuilders.EventBuilder().setCategory("Company")
										.setAction("Silenciar").setLabel("AccionUser").build());
								suscription.setSilenced(Common.BOOL_YES);
							}
						}
					});

					snackBar = Snackbar.make(clayout, context.getString(R.string.snack_silence), Snackbar.LENGTH_LONG).setAction(getString(R.string.undo), new View.OnClickListener()
					{
						@Override
						public void onClick(View v)
						{
							Realm realm	= Realm.getDefaultInstance();
							final Suscription suscription = realm.where(Suscription.class).equalTo(Suscription.KEY_API, company.getCompanyId()).findFirst();
							realm.executeTransaction(new Realm.Transaction()
							{
								@Override
								public void execute(Realm realm)
								{
									if(suscription.getSilenced() == Common.BOOL_YES)
									{
										suscription.setSilenced(Common.BOOL_NO);
									}
									else
									{
										suscription.setSilenced(Common.BOOL_YES);
									}
								}
							});

							refresh(false, false);
						}
					});
				break;

				case IconOptionAdapter.OPTION_EMPTY:
					//Agregado para capturar evento en Google Analytics
					GoogleAnalytics.getInstance(getHomeActivity()).newTracker(Common.HASH_GOOGLEANALYTICS).send(	new HitBuilders.EventBuilder().setCategory("Company")
																													.setAction("Vaciar").setLabel("AccionUser").build());
					//Modificación para ejecutar proceso en background
					if(StringUtils.isNotEmpty(companyId))
					{
						MessageHelper.emptyCompany(companyId, Common.BOOL_YES, getHomeActivity());
					}

					snackBar = Snackbar.make(clayout, context.getString(R.string.snack_empty), Snackbar.LENGTH_LONG).setAction(getString(R.string.undo), new View.OnClickListener()
					{
						@Override
						public void onClick(View v)
						{
							//Modificación para ejecutar proceso en background
							if(StringUtils.isNotEmpty(companyId))
							{
								MessageHelper.emptyCompany(companyId, Common.BOOL_NO, getHomeActivity());
							}
							refresh(false, false);
						}
					});
				break;

				case IconOptionAdapter.OPTION_BLOCK:
					String snackBarText = context.getString(R.string.snack_unblocked);

					if(Utils.reverseBool(company.getFollower()) == Common.BOOL_NO)
					{
						//Agregado para capturar evento en Google Analytics
						GoogleAnalytics.getInstance(getHomeActivity()).newTracker(Common.HASH_GOOGLEANALYTICS).send(new HitBuilders.EventBuilder().setCategory("Company")
																													.setAction("Bloquear").setLabel("AccionUser").build());
						snackBarText = context.getString(R.string.snack_blocked);
					}
					else
					{
						//Agregado para capturar evento en Google Analytics
						GoogleAnalytics.getInstance(getHomeActivity()).newTracker(Common.HASH_GOOGLEANALYTICS).send(new HitBuilders.EventBuilder().setCategory("Company")
																													.setAction("Agregar").setLabel("AccionUser").build());
					}

					HomeActivity.modifySubscriptions(getHomeActivity(), Utils.reverseBool(client.getFollower()), false, company.getCompanyId(), false);
					snackBar = Snackbar.make(clayout, snackBarText, Snackbar.LENGTH_LONG).setAction(getString(R.string.undo), new View.OnClickListener()
					{
						@Override
						public void onClick(View v)
						{
							HomeActivity.modifySubscriptions(getHomeActivity(), Utils.reverseBool(company.getFollower()), false, companyId, false);
							refresh(false, false);
						}
					});
				break;
			}

			refresh(false, false);
			list.dismiss();
			Utils.setStyleSnackBar(snackBar, context);
		}
		catch(Exception e)
		{
			Utils.logError(getHomeActivity(), "SwipeRefreshLayoutBasicFragment:dispatchMenu - Exception:", e);
		}
	}

	public void refresh(final boolean bringOut, final boolean showDialog)
	{
		try
		{
			Handler handler = new android.os.Handler();
			handler.post(new Runnable()
			{
				public void run()
				{
					initiateRefresh(bringOut, showDialog);
				}
			});
		}
		catch(Exception e)
		{
			Utils.logError(getHomeActivity(), "SwipeRefreshLayoutBasicFragment:refresh - Exception:", e);
		}
	}

	/**
	 * When the AsyncTask finishes, it calls onRefreshComplete(), which updates the data in the ListAdapter and turns off the progress bar.
	 */
	private void onRefreshComplete()
	{
		try
		{
			companies = SuscriptionHelper.getList(getHomeActivity());

			if(companies != null)
			{
				if(companies.size() > 0)
				{
					adapter			= new HomeAdapter(this);
					Handler handler	= new android.os.Handler();
					handler.post(new Runnable()
					{
						public void run()
						{
							rcwHome.setHasFixedSize(true);
							RecyclerView.LayoutManager mLayoutManager	= new LinearLayoutManager(getActivity());
							rcwHome.setLayoutManager(mLayoutManager);
							rcwHome.setAdapter(adapter);
							rlEmpty.setVisibility(RelativeLayout.GONE);
							rcwHome.setVisibility(RecyclerView.VISIBLE);
						}
					});
				}
				else
				{
					rlEmpty.setVisibility(RelativeLayout.VISIBLE);
					rcwHome.setVisibility(RecyclerView.GONE);
				}
			}
			else
			{
				rlEmpty.setVisibility(RelativeLayout.VISIBLE);
				rcwHome.setVisibility(RecyclerView.GONE);
			}
		}
		catch(Exception e)
		{
			Utils.logError(getHomeActivity(), "SwipeRefreshLayoutBasicFragment:initiateRefresh - Exception:", e);
		}

		mSwipeRefreshLayout.setRefreshing(false);
		started = false;
	}

	public class RefreshCompanyTask extends AsyncTask<Void, Void, List<String>>
	{
		private Activity	homeActivity;
		private boolean		forceByUser;

		private RefreshCompanyTask(Activity homeActivity, boolean forceByUser)
		{
			this.homeActivity	= homeActivity;
			this.forceByUser	= forceByUser;
		}

		@Override
		protected List<String> doInBackground(Void... params)
		{
			List<String> idsList				= new ArrayList<>();
			List<Suscription> companyPhantom	= new ArrayList<>();

			try
			{
				Realm realm	= Realm.getDefaultInstance();
				idsList.clear();
				idsList		= SuscriptionHelper.updateCompanies(homeActivity, forceByUser);

				if(idsList.size() > 0)
				{
					for(String id : idsList)
					{
						Suscription suscription = realm.where(Suscription.class).equalTo(Suscription.KEY_API, id).findFirst();

						if(!StringUtils.isIdMongo(suscription.getCompanyId()))
						{
							companyPhantom.add(suscription);
						}
					}
				}

				//Simplificamos para re-utilizar desde asyntask
				SuscriptionHelper.killPhantoms(companyPhantom, getHomeActivity(), null);
			}
			catch(Exception e)
			{
				Utils.logError(getHomeActivity(), "SwipeRefreshLayoutBasicFragment:RefreshCompanyTask:doInBackground - Exception:", e);
			}

			return idsList;
		}

		@Override
		protected void onPostExecute(List<String> result)
		{
			super.onPostExecute(result);

			try
			{
				onRefreshComplete();
			}
			catch(Exception e)
			{
				Utils.logError(getHomeActivity(), "SwipeRefreshLayoutBasicFragment:RefreshCompanyTask:onPostExecute - Exception:", e);
			}
		}
	}

	public CoordinatorLayout getClayout()
	{
		return clayout;
	}

	public void setClayout(final CoordinatorLayout clayout)
	{
		this.clayout = clayout;
	}

	public Activity getHomeActivity()
	{
		if(activity == null)
		{
			activity = getActivity();
		}

		return activity;
	}

	public void setHomeActivity(final Activity homeActivity)
	{
		this.activity = homeActivity;
	}
}