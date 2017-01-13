package com.tween.viacelular.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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
import com.tween.viacelular.adapters.HomeAdapter;
import com.tween.viacelular.adapters.IconOptionAdapter;
import com.tween.viacelular.asynctask.GetTweetsAsyncTask;
import com.tween.viacelular.models.Land;
import com.tween.viacelular.models.Message;
import com.tween.viacelular.models.MessageHelper;
import com.tween.viacelular.models.Suscription;
import com.tween.viacelular.models.SuscriptionHelper;
import com.tween.viacelular.models.User;
import com.tween.viacelular.utils.Common;
import com.tween.viacelular.utils.DateUtils;
import com.tween.viacelular.utils.StringUtils;
import com.tween.viacelular.utils.Utils;

import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmResults;

/**
 * A basic sample that shows how to use {@link android.support.v4.widget.SwipeRefreshLayout} to add the 'swipe-to-refresh' gesture to a layout. In this sample, SwipeRefreshLayout contains a
 * scrollable {@link android.widget.ListView} as its only child.
 *
 * <p>To provide an accessible way to trigger the refresh, this app also provides a refresh action item. <p>In this sample app, the refresh updates the ListView with a random set of new items.
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
	private HomeActivity		homeActivity;
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
			System.out.println("SwipeRefreshBasicFragment:onCreate - Exception: " + e);

			if(Common.DEBUG)
			{
				e.printStackTrace();
			}
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		View view = inflater.inflate(R.layout.fragment_home, container, false);

		try
		{
			mSwipeRefreshLayout							= (SwipeRefreshLayout) view.findViewById(R.id.swiperefresh);
			mSwipeRefreshLayout.setColorSchemeResources(R.color.swipe_color_1, R.color.swipe_color_2, R.color.swipe_color_3, R.color.accent);
			rcwHome										= (RecyclerView) view.findViewById(R.id.rcwHome);
			rcwHome.setHasFixedSize(true);
			RecyclerView.LayoutManager mLayoutManager	= new LinearLayoutManager(getActivity());
			rcwHome.setLayoutManager(mLayoutManager);
			rlEmpty										= (RelativeLayout) view.findViewById(R.id.rlEmpty);
		}
		catch(Exception e)
		{
			System.out.println("SwipeRefreshBasicFragment:onCreateView - Exception: " + e);

			if(Common.DEBUG)
			{
				e.printStackTrace();
			}
		}

		return view;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState)
	{
		try
		{
			super.onViewCreated(view, savedInstanceState);

			if(homeActivity != null)
			{
				companies = SuscriptionHelper.getList(homeActivity);
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
			System.out.println("SwipeRefreshBasicFragment:onViewCreated - Exception: " + e);

			if(Common.DEBUG)
			{
				e.printStackTrace();
			}
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
				case R.id.menu_refresh:
					Handler handler = new android.os.Handler();
					handler.post(new Runnable()
					{
						public void run()
						{
							initiateRefresh(true, true);
						}
					});
					return true;
			}
		}
		catch(Exception e)
		{
			System.out.println("SwipeRefreshLayoutBasicFragment:onOptionsItemSelected - Exception: " + e);

			if(Common.DEBUG)
			{
				e.printStackTrace();
			}
		}

		return super.onOptionsItemSelected(item);
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
					new RefreshCompanyTask(homeActivity, showDialog).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
				}
				else
				{
					onRefreshComplete();
				}
			}
		}
		catch(Exception e)
		{
			System.out.println("SwipeRefreshBasicFragment:initiateRefresh - Exception: " + e);

			if(Common.DEBUG)
			{
				e.printStackTrace();
			}
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
			System.out.println("SwipeRefreshLayoutBasicFragment:showMenu - Exception: " + e);

			if(Common.DEBUG)
			{
				e.printStackTrace();
			}
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
								if(DateUtils.needUpdate(suscription.getLastSocialUpdated(), DateUtils.DAY_MILLIS))
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
			System.out.println("SwipeRefreshLayoutBasicFragment:redirectCard - Exception: " + e);

			if(Common.DEBUG)
			{
				e.printStackTrace();
			}
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
					Suscription suscription = realm.where(Suscription.class).equalTo(Suscription.KEY_API, company.getCompanyId()).findFirst();
					realm.beginTransaction();

					if(suscription.getSilenced() == Common.BOOL_YES)
					{
						suscription.setSilenced(Common.BOOL_NO);
					}
					else
					{
						//Agregado para capturar evento en Google Analytics
						GoogleAnalytics.getInstance(getHomeActivity()).newTracker(Common.HASH_GOOGLEANALYTICS).send(	new HitBuilders.EventBuilder().setCategory("Company").setAction("Silenciar")
																														.setLabel("AccionUser").build());
						suscription.setSilenced(Common.BOOL_YES);
					}

					realm.commitTransaction();

					snackBar = Snackbar.make(clayout, context.getString(R.string.snack_silence), Snackbar.LENGTH_LONG).setAction(getString(R.string.undo), new View.OnClickListener()
					{
						@Override
						public void onClick(View v)
						{
							Realm realm	= Realm.getDefaultInstance();
							Suscription suscription = realm.where(Suscription.class).equalTo(Suscription.KEY_API, company.getCompanyId()).findFirst();
							realm.beginTransaction();

							if(suscription.getSilenced() == Common.BOOL_YES)
							{
								suscription.setSilenced(Common.BOOL_NO);
							}
							else
							{
								suscription.setSilenced(Common.BOOL_YES);
							}

							realm.commitTransaction();
							refresh(false, false);
						}
					});
				break;

				case IconOptionAdapter.OPTION_EMPTY:
					//Agregado para capturar evento en Google Analytics
					GoogleAnalytics.getInstance(getHomeActivity()).newTracker(Common.HASH_GOOGLEANALYTICS).send(	new HitBuilders.EventBuilder().setCategory("Company").setAction("Vaciar")
																													.setLabel("AccionUser").build());
					//Modificación para ejecutar proceso en background
					if(StringUtils.isNotEmpty(companyId))
					{
						MessageHelper.emptyCompany(companyId, Common.BOOL_YES);
					}

					snackBar = Snackbar.make(clayout, context.getString(R.string.snack_empty), Snackbar.LENGTH_LONG).setAction(getString(R.string.undo), new View.OnClickListener()
					{
						@Override
						public void onClick(View v)
						{
							//Modificación para ejecutar proceso en background
							if(StringUtils.isNotEmpty(companyId))
							{
								MessageHelper.emptyCompany(companyId, Common.BOOL_NO);
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
																													.setAction("Bloquear")
																													.setLabel("AccionUser").build());
						snackBarText = context.getString(R.string.snack_blocked);
					}
					else
					{
						//Agregado para capturar evento en Google Analytics
						GoogleAnalytics.getInstance(getHomeActivity()).newTracker(Common.HASH_GOOGLEANALYTICS).send(new HitBuilders.EventBuilder().setCategory("Company")
																													.setAction("Agregar")
																													.setLabel("AccionUser").build());
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
			System.out.println("SwipeRefreshLayoutBasicFragment:dispatchMenu - Exception: " + e);

			if(Common.DEBUG)
			{
				e.printStackTrace();
			}
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
			System.out.println("SwipeRefreshLayoutBasicFragment:refresh - Exception: " + e);

			if(Common.DEBUG)
			{
				e.printStackTrace();
			}
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
			System.out.println("SwipeRefreshBasicFragment:initiateRefresh - Exception: " + e);

			if(Common.DEBUG)
			{
				e.printStackTrace();
			}
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

				if(companyPhantom.size() > 0)
				{
					SharedPreferences preferences	= homeActivity.getApplicationContext().getSharedPreferences(Common.KEY_PREF, Context.MODE_PRIVATE);
					SharedPreferences.Editor editor	= preferences.edit();
					User user						= realm.where(User.class).findFirst();
					String country					= preferences.getString(Land.KEY_API, "");
					String companyId;

					if(user != null)
					{
						if(StringUtils.isNotEmpty(user.getCountryCode()))
						{
							country	= user.getCountryCode();
							editor.putString(Land.KEY_API, country);
							editor.apply();
						}
					}

					for(Suscription phantom : companyPhantom)
					{
						RealmResults<Message> messages	= realm.where(Message.class).equalTo(Suscription.KEY_API, phantom.getCompanyId()).equalTo(Message.KEY_DELETED, Common.BOOL_NO)
															.lessThan(Common.KEY_STATUS, Message.STATUS_SPAM).findAll().distinct(Message.KEY_CHANNEL);

						if(messages.size() > 0)
						{
							for(Message message : messages)
							{
								Suscription client = realm.where(Suscription.class).equalTo(Suscription.KEY_API, SuscriptionHelper.classifySubscription(message.getChannel(), message.getMsg(),
														homeActivity, country)).findFirst();

								if(client != null)
								{
									companyId = client.getCompanyId();

									if(!companyId.equals(phantom.getCompanyId()) && StringUtils.isIdMongo(companyId))
									{
										//Actualizar los mensajes
										MessageHelper.groupMessages(phantom.getCompanyId(), companyId);
									}
								}
							}
						}
					}
				}
			}
			catch(Exception e)
			{
				System.out.println("SwipeRefreshLayoutBasicFragment:RefreshCompanyTask:doInBackground - Exception: " + e);

				if(Common.DEBUG)
				{
					e.printStackTrace();
				}
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
				System.out.println("SwipeRefreshLayoutBasicFragment:RefreshCompanyTask:onPostExecute - Exception: " + e);

				if(Common.DEBUG)
				{
					e.printStackTrace();
				}
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

	public HomeActivity getHomeActivity()
	{
		return homeActivity;
	}

	public void setHomeActivity(final HomeActivity homeActivity)
	{
		this.homeActivity = homeActivity;
	}
}
