package com.tween.viacelular.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;
import com.tween.viacelular.R;
import com.tween.viacelular.adapters.BlockedAdapter;
import com.tween.viacelular.adapters.RecyclerAdapter;
import com.tween.viacelular.adapters.RecyclerItemClickListener;
import com.tween.viacelular.asynctask.UpdateSuscriptionsAsyncTask;
import com.tween.viacelular.models.Message;
import com.tween.viacelular.models.Suscription;
import com.tween.viacelular.models.SuscriptionHelper;
import com.tween.viacelular.services.IncomingSmsService;
import com.tween.viacelular.utils.Common;
import com.tween.viacelular.utils.StringUtils;
import com.tween.viacelular.utils.Utils;
import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;

public class BlockedActivity extends AppCompatActivity
{
	private RecyclerView				rcwBlocked;
	private BlockedAdapter				adapter;
	private RealmResults<Suscription>	clients	= null;
	private CoordinatorLayout			Clayout;
	private RelativeLayout				rlEmpty;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		try
		{
			super.onCreate(savedInstanceState);
			setContentView(R.layout.activity_blocked);

			if(Utils.checkSesion(this, Common.ANOTHER_SCREEN))
			{
				Realm realm						= Realm.getDefaultInstance();
				final Intent intentRecive		= getIntent();
				setTitle(intentRecive.getStringExtra(Common.KEY_TITLE));
				Toolbar toolbar					= (Toolbar) findViewById(R.id.toolBar);
				setSupportActionBar(toolbar);
				RecyclerView mRecyclerView		= (RecyclerView) findViewById(R.id.RecyclerView);
				mRecyclerView.setHasFixedSize(true);
				rcwBlocked						= (RecyclerView) findViewById(R.id.rcwBlocked);
				rlEmpty							= (RelativeLayout) findViewById(R.id.rlEmpty);
				RecyclerView.Adapter mAdapter	= new RecyclerAdapter(	Utils.getMenu(getApplicationContext()),
																		intentRecive.getIntExtra(Common.KEY_SECTION, 0), ContextCompat.getColor(getApplicationContext(), R.color.accent),
																		getApplicationContext());
				mRecyclerView.setAdapter(mAdapter);
				RecyclerView.LayoutManager mLayoutManager	= new LinearLayoutManager(this);
				mRecyclerView.setLayoutManager(mLayoutManager);
				DrawerLayout Drawer							= (DrawerLayout) findViewById(R.id.DrawerLayout);
				ActionBarDrawerToggle mDrawerToggle			= new ActionBarDrawerToggle(this, Drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
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
				//Cambio de contexto para redirigir desde el menú
				final Activity context = BlockedActivity.this;
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

				rcwBlocked.setHasFixedSize(true);
				mLayoutManager	= new LinearLayoutManager(this);
				rcwBlocked.setLayoutManager(mLayoutManager);
				clients			= realm.where(Suscription.class).equalTo(Common.KEY_STATUS, Suscription.STATUS_BLOCKED).or().equalTo(Suscription.KEY_BLOCKED, Common.BOOL_YES)
									.findAllSorted(Common.KEY_NAME);
				adapter			= new BlockedAdapter(clients, this);
				rcwBlocked.setAdapter(adapter);
				Clayout			= (CoordinatorLayout) findViewById(R.id.clSnack);
				Utils.tintColorScreen(this, Common.COLOR_ACTION);

				if(clients.size() > 0)
				{
					rcwBlocked.setVisibility(RecyclerView.VISIBLE);
					rlEmpty.setVisibility(RelativeLayout.GONE);
				}
				else
				{
					rcwBlocked.setVisibility(RecyclerView.GONE);
					rlEmpty.setVisibility(RelativeLayout.VISIBLE);
				}
			}
		}
		catch(Exception e)
		{
			System.out.println("BlockedActivity:onCreate - Exception: " + e);

			if(Common.DEBUG)
			{
				e.printStackTrace();
			}
		}
	}

	public void unLockCompany(final int position)
	{
		//Agregado para capturar excepciones
		try
		{
			//Validación de lista agregada
			if(clients != null)
			{
				final Suscription client = clients.get(position);

				if(client != null)
				{
					//Se quitó momentaneamente la llamda a la api hasta que esté disponible
					modifySubscriptions(this, Common.BOOL_YES, false, client.getCompanyId(), false);
					refresh();

					Snackbar snackBar = Snackbar.make(Clayout, getString(R.string.snack_unblocked), Snackbar.LENGTH_LONG).setAction(getString(R.string.undo), new View.OnClickListener()
					{
						@Override
						public void onClick(View v)
						{
							try
							{
								//Se quitó momentaneamente la llamda a la api hasta que esté disponible
								modifySubscriptions(getApplicationContext(), Common.BOOL_NO, false, client.getCompanyId(), false);
								refresh();
							}
							catch(Exception e)
							{
								System.out.println("BlockActivity:unLockCompany1 - Exception: " + e);

								if(Common.DEBUG)
								{
									e.printStackTrace();
								}
							}
						}
					});

					Utils.setStyleSnackBar(snackBar, getApplicationContext());
				}
			}
		}
		catch(Exception e)
		{
			System.out.println("BlockActivity:unLockCompany2 - Exception: " + e);

			if(Common.DEBUG)
			{
				e.printStackTrace();
			}
		}
	}

	public static void modifySubscriptions(Context context, int flag, boolean goToHome, String companyId, boolean blockUI)
	{
		//Unificación de comportamiento en Asynctask para funcionalidad de Bloquear, Desuscribir y viceversa
		try
		{
			System.out.println("BlockActivity:modifySubscriptions - flag: " + flag+" gotohome: "+goToHome+" companyId: "+companyId+" blockui: "+blockUI);
			Realm realm				= Realm.getDefaultInstance();
			Suscription suscription	= realm.where(Suscription.class).equalTo(Suscription.KEY_API, companyId).findFirst();
			boolean sendSMS			= false;

			if(suscription != null)
			{
				if(flag == Common.BOOL_NO && suscription.getType() == Suscription.TYPE_AUTOGENERATED && suscription.getBlocked() == Common.BOOL_NO)
				{
					sendSMS = true;
				}
			}

			new UpdateSuscriptionsAsyncTask(context, blockUI, flag, goToHome, companyId).execute();

			if(sendSMS)
			{
				//Modificación para leer mensajes desde Realm
				RealmResults<Message> messages = realm.where(Message.class).equalTo(Common.KEY_TYPE, Message.TYPE_SMS).equalTo(Suscription.KEY_API, companyId)
													.lessThan(Common.KEY_STATUS, Message.STATUS_SPAM).findAllSorted(Message.KEY_CREATED, Sort.DESCENDING);

				if(messages.size() > 0)
				{
					String number			= SuscriptionHelper.searchUnsuscribeNumber(suscription, messages);

					if(StringUtils.isNotEmpty(number))
					{
						IncomingSmsService.sendSMS(context, number, SuscriptionHelper.searchUnsuscribeMessage(suscription, context));
					}
				}
			}
		}
		catch(Exception e)
		{
			System.out.println("BlockActivity:modifySubscriptions - Exception: " + e);

			if(Common.DEBUG)
			{
				e.printStackTrace();
			}
		}
	}

	private void refresh()
	{
		//Agregado para capturar excepciones
		try
		{
			Handler handler = new Handler();
			handler.post(new Runnable()
			{
				public void run()
				{
					try
					{
						Realm realm	= Realm.getDefaultInstance();
						clients		= realm.where(Suscription.class).equalTo(Common.KEY_STATUS, Suscription.STATUS_BLOCKED).or().equalTo(Suscription.KEY_BLOCKED, Common.BOOL_YES)
										.findAllSorted(Common.KEY_NAME);

						if(clients.size() > 0)
						{
							adapter = new BlockedAdapter(clients, BlockedActivity.this);
							rcwBlocked.setAdapter(adapter);

							if(clients.size() > 0)
							{
								rcwBlocked.setVisibility(RecyclerView.VISIBLE);
								rlEmpty.setVisibility(RelativeLayout.GONE);
							}
							else
							{
								rcwBlocked.setVisibility(RecyclerView.GONE);
								rlEmpty.setVisibility(RelativeLayout.VISIBLE);
							}
						}
						else
						{
							rcwBlocked.setVisibility(RecyclerView.GONE);
							rlEmpty.setVisibility(RelativeLayout.VISIBLE);
						}
					}
					catch(Exception e)
					{
						System.out.println("BlockedActivity:refresh - Exception: " + e);

						if(Common.DEBUG)
						{
							e.printStackTrace();
						}
					}
				}
			});
		}
		catch(Exception e)
		{
			System.out.println("BlockedActivity:refresh - Exception: " + e);

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
	public void onBackPressed()
	{
		try
		{
			Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
			intent.putExtra(Common.KEY_REFRESH, false);
			startActivity(intent);
			finish();
		}
		catch(Exception e)
		{
			System.out.println("BlockedActivity:onBackPressed - Exception: " + e);

			if(Common.DEBUG)
			{
				e.printStackTrace();
			}
		}
	}

	protected void onResume()
	{
		super.onResume();
		//Agregado para recargar listado en caso de salir de la app
		try
		{
			if(Utils.checkSesion(this, Common.ANOTHER_SCREEN))
			{
				refresh();
			}
		}
		catch(Exception e)
		{
			System.out.println("BlockedActivity:onResume - Exception: " + e);

			if(Common.DEBUG)
			{
				e.printStackTrace();
			}
		}
	}
}