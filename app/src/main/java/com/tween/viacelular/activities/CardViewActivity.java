package com.tween.viacelular.activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.transition.Explode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.afollestad.materialdialogs.MaterialDialog;
import com.appboy.Appboy;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.squareup.picasso.Picasso;
import com.tween.viacelular.R;
import com.tween.viacelular.adapters.CardAdapter;
import com.tween.viacelular.asynctask.ConfirmReadingAsyncTask;
import com.tween.viacelular.asynctask.SendIdentificationKeyAsyncTask;
import com.tween.viacelular.data.ApiConnection;
import com.tween.viacelular.models.Message;
import com.tween.viacelular.models.MessageHelper;
import com.tween.viacelular.models.Migration;
import com.tween.viacelular.models.Suscription;
import com.tween.viacelular.models.SuscriptionHelper;
import com.tween.viacelular.utils.Common;
import com.tween.viacelular.utils.StringUtils;
import com.tween.viacelular.utils.Utils;
import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;

public class CardViewActivity extends AppCompatActivity
{
	private Suscription				suscription		= null;
	private MaterialDialog			list			= null;
	private RealmResults<Message>	notifications	= null;
	private int						colorTitle		= Color.WHITE;
	private int						colorSubTitle	= Color.LTGRAY;
	private RecyclerView			rcwCard;
	private CardAdapter				mAdapter;
	private CoordinatorLayout		Clayout;
	private CardView				cardPayout;
	private CardView				cardSuscribe;
	private CardView				cardForm;
	private CardView				cardOk;
	private CardView				cardRetry;
	private RelativeLayout			rlEmpty;
	private EditText				editCode;
	private TextInputLayout			inputCode;
	private int						originalSoftInputMode;
	private Button					btnContinueForm;
	private TextView				txtSubTitleForm;
	private String					companyId;
	private Toolbar					toolBar;
	private TextView				txtTitle;
	private TextView				txtSubTitleCollapsed;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		try
		{
			if(Common.API_LEVEL >= Build.VERSION_CODES.LOLLIPOP)
			{
				getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
				getWindow().setEnterTransition(new Explode());
				getWindow().setReenterTransition(new Explode());
				getWindow().setExitTransition(new Explode());
			}

			super.onCreate(savedInstanceState);
			setContentView(R.layout.activity_cardview);
			toolBar										= (Toolbar) findViewById(R.id.toolBarCardView);
			rcwCard										= (RecyclerView) findViewById(R.id.rcwCard);
			cardPayout									= (CardView) findViewById(R.id.cardPayout);
			cardForm									= (CardView) findViewById(R.id.cardForm);
			cardOk										= (CardView) findViewById(R.id.cardOk);
			cardRetry									= (CardView) findViewById(R.id.cardRetry);
			rlEmpty										= (RelativeLayout) findViewById(R.id.rlEmpty);
			editCode									= (EditText) findViewById(R.id.editCode);
			inputCode									= (TextInputLayout) findViewById(R.id.inputCode);
			btnContinueForm								= (Button) findViewById(R.id.btnContinueForm);
			txtSubTitleForm								= (TextView) findViewById(R.id.txtSubTitleForm);
			cardSuscribe								= (CardView) findViewById(R.id.cardSuscribe);
			TextView txtSubSuscribe						= (TextView) findViewById(R.id.txtSubSuscribe);
			ImageView ibBack							= (ImageView) findViewById(R.id.ibBack);
			ImageView circleView						= (ImageView) findViewById(R.id.circleView);
			txtTitle									= (TextView) findViewById(R.id.txtTitle);
			txtSubTitleCollapsed						= (TextView) findViewById(R.id.txtSubTitleCollapsed);
			rcwCard.setHasFixedSize(true);
			RecyclerView.LayoutManager mLayoutManager	= new LinearLayoutManager(this);
			rcwCard.setLayoutManager(mLayoutManager);
			setSupportActionBar(toolBar);
			toolBar.setTitle("");
			toolBar.setSubtitle("");
			setTitle("");

			if(Utils.checkSesion(this, Common.ANOTHER_SCREEN))
			{
				final Intent intentRecive			= getIntent();
				Realm realm							= Realm.getDefaultInstance();
				RealmResults<Message> notifications	= null;

				if(intentRecive != null)
				{
					//Modificaciones para migrar entidad Company completa a Realm
					companyId	= intentRecive.getStringExtra(Common.KEY_ID);
					suscription	= realm.where(Suscription.class).equalTo(Suscription.KEY_API, companyId).findFirst();

					if(suscription != null)
					{
						txtTitle.setText(suscription.getName());
						String image	= suscription.getImage();
						String color	= suscription.getColorHex();

						//Agregado para validar companies sin color
						if(StringUtils.isEmpty(color))
						{
							color = Common.COLOR_ACTION;
						}

						//Agregado para detectar si el color es claro
						if(Utils.isLightColor(color))
						{
							colorTitle		= Color.BLACK;
							colorSubTitle	= Color.DKGRAY;
							ibBack.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_arrow_back_black_24dp));
							toolBar.setOverflowIcon(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_more_vert_black_24dp));
						}
						else
						{
							ibBack.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.back));
						}

						//Agregado para destacar en gris los titulos si la company fue bloqueada y la card ignorada
						if(suscription.getGray() == Common.BOOL_YES)
						{
							txtTitle.setTextColor(Utils.adjustAlpha(colorTitle, Common.ALPHA_FOR_BLOCKS));
							txtSubTitleCollapsed.setTextColor(Utils.adjustAlpha(colorSubTitle, Common.ALPHA_FOR_BLOCKS));
							color = Common.COLOR_BLOCKED;
						}
						else
						{
							txtTitle.setTextColor(colorTitle);
							txtSubTitleCollapsed.setTextColor(Utils.adjustAlpha(colorSubTitle, Common.ALPHA_FOR_SUBTITLE));
						}

						//Modificación para migrar a asynctask la descarga de imágenes
						if(StringUtils.isNotEmpty(image))
						{
							//Modificación de librería para recargar imagenes a mientras se está viendo el listado y optimizar vista
							Picasso.with(getApplicationContext()).load(image).placeholder(R.drawable.ic_launcher).into(circleView);
						}

						txtSubTitleCollapsed.setText(suscription.getIndustry());
						toolBar.setBackgroundColor(Color.parseColor(color));
						Utils.tintColorScreen(this, color);

						//Agregado para corregir ARN por phantomCompany
						if(StringUtils.isNotEmpty(companyId))
						{
							notifications	= realm.where(Message.class).notEqualTo(Message.KEY_DELETED, Common.BOOL_YES).lessThan(Common.KEY_STATUS, Message.STATUS_SPAM)
												.equalTo(Suscription.KEY_API, suscription.getCompanyId()).findAllSorted(Message.KEY_CREATED, Sort.DESCENDING);
						}

						//Agregado como atajo para ir a la pantalla Configuración
						txtTitle.setOnClickListener(new View.OnClickListener()
						{
							@Override
							public void onClick(final View v)
							{
								goToSettings();
							}
						});

						txtSubTitleCollapsed.setOnClickListener(new View.OnClickListener()
						{
							@Override
							public void onClick(final View v)
							{
								goToSettings();
							}
						});

						mAdapter = new CardAdapter(this, suscription.getCompanyId());
						rcwCard.setAdapter(mAdapter);
					}
					else
					{
						txtTitle.setText(getString(R.string.app_name));
						Utils.tintColorScreen(this, Common.COLOR_ACTION);
					}
				}
				else
				{
					txtTitle.setText(getString(R.string.app_name));
					Utils.tintColorScreen(this, Common.COLOR_ACTION);
				}

				ibBack.setOnClickListener(new View.OnClickListener()
				{
					@Override
					public void onClick(final View v)
					{
						onBackPressed();
					}
				});

				Clayout = (CoordinatorLayout) findViewById(R.id.clSnack);
				cardForm.setVisibility(CardView.GONE);
				cardOk.setVisibility(CardView.GONE);
				cardRetry.setVisibility(CardView.GONE);
				cardPayout.setVisibility(CardView.GONE);
				cardSuscribe.setVisibility(CardView.GONE);
				rcwCard.setVisibility(RecyclerView.GONE);
				rlEmpty.setVisibility(RelativeLayout.GONE);

				if(suscription != null)
				{
					RealmResults<Message> unread = realm.where(Message.class).notEqualTo(Message.KEY_DELETED, Common.BOOL_YES).lessThan(Common.KEY_STATUS, Message.STATUS_READ)
														.equalTo(Suscription.KEY_API, suscription.getCompanyId()).findAll();

					if(unread.size() > 0)
					{
						ConfirmReadingAsyncTask task = new ConfirmReadingAsyncTask(getApplicationContext(), false, companyId, "", Message.STATUS_READ);
						task.execute();
					}

					//Validaciones para mostrar o no campos según disponibilidad de datos
					if(StringUtils.isNotEmpty(suscription.getAbout()))
					{
						txtSubSuscribe.setText(suscription.getAbout());
					}
					else
					{
						//Validar con PO para ver si cuando no hay texto no mostramos nada en vez del placeholder siguiente
						txtSubSuscribe.setText(getString(R.string.landing_title, " "+suscription.getName()));
					}

					if(notifications != null)
					{
						if(notifications.size() > 0)
						{
							rcwCard.setVisibility(RecyclerView.VISIBLE);
							rlEmpty.setVisibility(RelativeLayout.GONE);
						}
						else
						{
							rcwCard.setVisibility(RecyclerView.GONE);
							rlEmpty.setVisibility(RelativeLayout.VISIBLE);
						}
					}
					else
					{
						rcwCard.setVisibility(RecyclerView.GONE);
						rlEmpty.setVisibility(RelativeLayout.VISIBLE);
					}
				}
			}
		}
		catch(Exception e)
		{
			System.out.println("CardViewActivity:onCreate - Exception: " + e);

			if(Common.DEBUG)
			{
				e.printStackTrace();
			}
		}
	}

	public void goToSettings()
	{
		try
		{
			Realm realm		= Realm.getDefaultInstance();
			Intent intent	= new Intent(getApplicationContext(), LandingActivity.class); //Modificación para unificar en pantalla Landing
			suscription		= realm.where(Suscription.class).equalTo(Suscription.KEY_API, companyId).findFirst();
			intent.putExtra(Common.KEY_ID, companyId);
			intent.putExtra(Common.KEY_SECTION, "card");
			startActivity(intent);
			finish();
		}
		catch(Exception e)
		{
			System.out.println("CardViewActivity:goToSettings - Exception: " + e);

			if(Common.DEBUG)
			{
				e.printStackTrace();
			}
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		try
		{
			MenuInflater inflater = getMenuInflater();
			inflater.inflate(R.menu.menu_cardview, menu);
		}
		catch(Exception e)
		{
			System.out.println("CardViewActivity:onCreateOptionsMenu - Exception: " + e);

			if(Common.DEBUG)
			{
				e.printStackTrace();
			}
		}

		return super.onCreateOptionsMenu(menu);
	}

	/**
	 * Agregado para modificar acción de silenciar según si está silenciado o no
	 * @param menu
	 * @return
	 */
	@Override
	public boolean onPrepareOptionsMenu(Menu menu)
	{
		try
		{
			if(menu != null)
			{
				Migration.getDB(CardViewActivity.this);
				Realm realm	= Realm.getDefaultInstance();
				suscription	= realm.where(Suscription.class).equalTo(Suscription.KEY_API, companyId).findFirst();

				if(menu.getItem(1) != null)
				{
					MenuItem item	= menu.getItem(1);

					if(suscription != null)
					{
						if(suscription.getSilenced() == Common.BOOL_YES)
						{
							item.setTitle(R.string.activate_notif);
						}
						else
						{
							item.setTitle(R.string.silence);
						}
					}
					else
					{
						item.setTitle(R.string.silence);
					}
				}

				if(menu.getItem(3) != null)
				{
					MenuItem itemSuscribe	= menu.getItem(3);

					if(suscription != null)
					{
						if(suscription.getFollower() == Common.BOOL_YES)
						{
							itemSuscribe.setTitle(R.string.landing_unsuscribe);
						}
						else
						{
							itemSuscribe.setTitle(R.string.landing_suscribe);
						}
					}
					else
					{
						itemSuscribe.setTitle(R.string.landing_suscribe);
					}
				}
			}
		}
		catch(Exception e)
		{
			System.out.println("CardViewActivity:onPrepareOptionsMenu - Exception: " + e);

			if(Common.DEBUG)
			{
				e.printStackTrace();
			}
		}

		return super.onPrepareOptionsMenu(menu);
	}

	public void showOptionsCard(final int position)
	{
		try
		{
			list = new MaterialDialog.Builder(this)
					.items(R.array.optionsCard)
					.itemsCallback(new MaterialDialog.ListCallback()
					{
						@Override
						public void onSelection(MaterialDialog dialog, View view, int which, CharSequence text)
						{
							dispatchMenu(which, notifications.get(position).getMsgId());
						}
					}).show();
		}
		catch(Exception e)
		{
			System.out.println("CardViewActivity:showOptionsCard - Exception: " + e);

			if(Common.DEBUG)
			{
				e.printStackTrace();
			}
		}
	}

	public void becomeGray(View view)
	{
		Realm realm = null;

		try
		{
			realm		= Realm.getDefaultInstance();
			suscription	= realm.where(Suscription.class).equalTo(Suscription.KEY_API, companyId).findFirst();
			realm.beginTransaction();
			suscription.setGray(Common.BOOL_YES);
			realm.commitTransaction();
			Utils.hideCard(cardSuscribe);
			txtTitle.setTextColor(Utils.adjustAlpha(colorTitle, Common.ALPHA_FOR_BLOCKS));
			txtSubTitleCollapsed.setTextColor(Utils.adjustAlpha(colorSubTitle, Common.ALPHA_FOR_BLOCKS));
			toolBar.setBackgroundColor(Color.parseColor(Common.COLOR_BLOCKED));
			Utils.tintColorScreen(this, Common.COLOR_BLOCKED);

			RelativeLayout.LayoutParams p = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

			if(Common.API_LEVEL >= Build.VERSION_CODES.JELLY_BEAN_MR1)
			{
				p.removeRule(RelativeLayout.BELOW);
			}

			rcwCard.setLayoutParams(p);
		}
		catch(Exception e)
		{
			System.out.println("CardViewActivity:becomeGray - Exception: " + e);

			if(Common.DEBUG)
			{
				e.printStackTrace();
			}
		}
	}

	public void continueCompany(View view)
	{
		try
		{
			Realm realm	= Realm.getDefaultInstance();
			suscription	= realm.where(Suscription.class).equalTo(Suscription.KEY_API, companyId).findFirst();
			realm.beginTransaction();
			suscription.setReceive(Common.BOOL_YES);
			realm.commitTransaction();
			Utils.hideCard(cardPayout);

			RelativeLayout.LayoutParams p = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

			if(Common.API_LEVEL >= Build.VERSION_CODES.JELLY_BEAN_MR1)
			{
				p.removeRule(RelativeLayout.BELOW);
			}

			rcwCard.setLayoutParams(p);
		}
		catch(Exception e)
		{
			System.out.println("CardViewActivity:continueCompany - Exception: " + e);

			if(Common.DEBUG)
			{
				e.printStackTrace();
			}
		}
	}

	public void blockCompany(View view)
	{
		try
		{
			Realm realm	= Realm.getDefaultInstance();
			suscription	= realm.where(Suscription.class).equalTo(Suscription.KEY_API, companyId).findFirst();
			BlockedActivity.modifySubscriptions(getApplicationContext(), Common.BOOL_NO, true, companyId);
			Utils.hideCard(cardPayout);
			Utils.hideCard(cardSuscribe);

			RelativeLayout.LayoutParams p = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

			if(Common.API_LEVEL >= Build.VERSION_CODES.JELLY_BEAN_MR1)
			{
				p.removeRule(RelativeLayout.BELOW);
			}

			rcwCard.setLayoutParams(p);

			Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
			intent.putExtra(Common.KEY_ID, companyId);
			intent.putExtra(Suscription.KEY_BLOCKED, suscription.getBlocked());
			intent.putExtra(Common.KEY_REFRESH, true);
			startActivity(intent);
			finish();
		}
		catch(Exception e)
		{
			System.out.println("CardViewActivity:blockCompany - Exception: " + e);

			if(Common.DEBUG)
			{
				e.printStackTrace();
			}
		}
	}

	public void dispatchMenu(int position, final String msgId)
	{
		try
		{
			Realm realm					= Realm.getDefaultInstance();
			suscription					= realm.where(Suscription.class).equalTo(Suscription.KEY_API, companyId).findFirst();
			Snackbar snackBar			= null;
			final Message notification	= realm.where(Message.class).equalTo(Message.KEY_API, msgId).findFirst();

			switch(position)
			{
				case CardAdapter.OPTION_SHARE:
					//Agregado para capturar evento en Google Analytics
					GoogleAnalytics.getInstance(this).newTracker(Common.HASH_GOOGLEANALYTICS).send(	new HitBuilders.EventBuilder().setCategory("Mensajes").setAction("Compartir")
																									.setLabel("Accion_user").build());
					Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
					sharingIntent.setType("text/plain");
					sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, notification.getType());
					sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, notification.getMsg());
					startActivity(Intent.createChooser(sharingIntent, getResources().getString(R.string.share)));
				break;

				case CardAdapter.OPTION_BLOCK:
					//Agregado para capturar evento en Google Analytics
					GoogleAnalytics.getInstance(this).newTracker(Common.HASH_GOOGLEANALYTICS).send(	new HitBuilders.EventBuilder().setCategory("Mensajes").setAction("Marcarspam")
																									.setLabel("Accion_user").build());
					realm.beginTransaction();
					notification.setStatus(Message.STATUS_SPAM);
					realm.commitTransaction();
					ConfirmReadingAsyncTask task	= new ConfirmReadingAsyncTask(getApplicationContext(), false, companyId, notification.getMsgId(), Message.STATUS_SPAM);
					task.execute();

					snackBar						= Snackbar.make(Clayout, getString(R.string.snack_msg_spam), Snackbar.LENGTH_LONG).setAction(getString(R.string.undo), new View.OnClickListener()
					{
						@Override
						public void onClick(View v)
						{
							Realm realm						= Realm.getDefaultInstance();
							realm.beginTransaction();
							notification.setStatus(Message.STATUS_READ);
							realm.commitTransaction();
							refresh(false);
							ConfirmReadingAsyncTask task	= new ConfirmReadingAsyncTask(getApplicationContext(), false, companyId, notification.getMsgId(), Message.STATUS_READ);
							task.execute();
						}
					});
				break;

				case CardAdapter.OPTION_DELETE:
					//Agregado para capturar evento en Google Analytics
					GoogleAnalytics.getInstance(this).newTracker(Common.HASH_GOOGLEANALYTICS).send(	new HitBuilders.EventBuilder().setCategory("Mensajes").setAction("Borrar")
																									.setLabel("Accion_user").build());
					realm.beginTransaction();
					notification.setDeleted(Common.BOOL_YES);
					realm.commitTransaction();
					snackBar = Snackbar.make(Clayout, getString(R.string.snack_msg_deleted), Snackbar.LENGTH_LONG).setAction(getString(R.string.undo), new View.OnClickListener()
					{
						@Override
						public void onClick(View v)
						{
							Realm realm = Realm.getDefaultInstance();
							realm.beginTransaction();
							notification.setDeleted(Common.BOOL_NO);
							realm.commitTransaction();
							refresh(false);
						}
					});
				break;
			}

			refresh(false);
			list.dismiss();
			Utils.setStyleSnackBar(snackBar, getApplicationContext());
		}
		catch(Exception e)
		{
			System.out.println("CardViewActivity:dispatchMenu - Exception: " + e);

			if(Common.DEBUG)
			{
				e.printStackTrace();
			}
		}
	}

	private void refresh(final boolean erase)
	{
		try
		{
			Handler handler	= new android.os.Handler();
			handler.post(new Runnable()
			{
				public void run()
				{
					if(StringUtils.isNotEmpty(companyId))
					{
						Realm realm				= Realm.getDefaultInstance();
						Suscription suscription	= realm.where(Suscription.class).equalTo(Suscription.KEY_API, companyId).findFirst();

						if(erase)
						{
							notifications = null;
						}
						else
						{
							if(suscription != null)
							{
								if(StringUtils.isNotEmpty(companyId))
								{
									notifications = realm.where(Message.class).notEqualTo(Message.KEY_DELETED, Common.BOOL_YES).notEqualTo(Common.KEY_STATUS, Message.STATUS_SPAM)
														.equalTo(Suscription.KEY_API, suscription.getCompanyId()).findAllSorted(Message.KEY_CREATED, Sort.DESCENDING);
								}
							}
						}

						//Modificación para implementar placeholder picture en lugar de card vacía
						if(rcwCard != null)
						{
							if(rlEmpty != null)
							{
								cardForm.setVisibility(CardView.GONE);
								cardOk.setVisibility(CardView.GONE);
								cardRetry.setVisibility(CardView.GONE);
								cardPayout.setVisibility(CardView.GONE);
								cardSuscribe.setVisibility(CardView.GONE);
								rcwCard.setVisibility(RecyclerView.GONE);
								rlEmpty.setVisibility(RelativeLayout.GONE);

								int idViewFather = 0;

								if(suscription != null)
								{
									if(SuscriptionHelper.isRevenue(suscription.getCompanyId()))
									{
										if(suscription.getReceive() != Common.BOOL_YES)
										{
											idViewFather = cardPayout.getId();
										}
										else
										{
											if(suscription.getFollower() == Common.BOOL_NO && suscription.getGray() == Common.BOOL_NO)
											{
												idViewFather = cardSuscribe.getId();
											}
										}
									}
									else
									{
										if(suscription.getFollower() == Common.BOOL_NO && suscription.getGray() == Common.BOOL_NO)
										{
											idViewFather = cardSuscribe.getId();
										}
									}
								}

								RelativeLayout.LayoutParams p = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

								if(notifications == null)
								{
									rcwCard.setVisibility(RecyclerView.GONE);
									rlEmpty.setVisibility(RelativeLayout.VISIBLE);

									//Agregado para evitar que las cards se solapen con la card superior
									if(idViewFather != 0)
									{
										p.addRule(RelativeLayout.BELOW, idViewFather);
										rlEmpty.setLayoutParams(p);
									}
									else
									{
										if(Common.API_LEVEL >= Build.VERSION_CODES.JELLY_BEAN_MR1)
										{
											p.removeRule(RelativeLayout.BELOW);
											rlEmpty.setLayoutParams(p);
										}
									}
								}
								else
								{
									if(notifications.size() == 0)
									{
										rcwCard.setVisibility(RecyclerView.GONE);
										rlEmpty.setVisibility(RelativeLayout.VISIBLE);

										//Agregado para evitar que las cards se solapen con la card superior
										if(idViewFather != 0)
										{
											p.addRule(RelativeLayout.BELOW, idViewFather);
											rlEmpty.setLayoutParams(p);
										}
										else
										{
											if(Common.API_LEVEL >= Build.VERSION_CODES.JELLY_BEAN_MR1)
											{
												p.removeRule(RelativeLayout.BELOW);
												rlEmpty.setLayoutParams(p);
											}
										}
									}
									else
									{
										rcwCard.setVisibility(RecyclerView.VISIBLE);
										rlEmpty.setVisibility(RelativeLayout.GONE);

										//Agregado para evitar que las cards se solapen con la card superior
										if(idViewFather != 0)
										{
											p.addRule(RelativeLayout.BELOW, idViewFather);
											rcwCard.setLayoutParams(p);
										}
										else
										{
											if(Common.API_LEVEL >= Build.VERSION_CODES.JELLY_BEAN_MR1)
											{
												p.removeRule(RelativeLayout.BELOW);
												rcwCard.setLayoutParams(p);
											}
										}
									}
								}

								if(idViewFather == cardPayout.getId())
								{
									Utils.showCard(cardPayout);
								}
								else
								{
									if(idViewFather == cardSuscribe.getId())
									{
										Utils.showCard(cardSuscribe);
									}
								}
							}

							//Modificación para determinar si el mensaje es pago o no desde el número corto
							mAdapter = new CardAdapter(CardViewActivity.this, companyId);
							rcwCard.setAdapter(mAdapter);
						}

						if(suscription != null)
						{
							if(StringUtils.isNotEmpty(suscription.getIdentificationKey()) && suscription.getDataSent() == Common.BOOL_NO && suscription.getFollower() == Common.BOOL_YES)
							{
								rlEmpty.setVisibility(RelativeLayout.GONE);
								rcwCard.setVisibility(RecyclerView.GONE);
								Utils.showCard(cardForm);
								inputCode.setErrorEnabled(false);
								inputCode.setHint(suscription.getIdentificationKey());
								String title = getString(R.string.landing_card_form_text1) + " " + suscription.getIdentificationKey() + " " + getString(R.string.landing_card_form_text2);
								txtSubTitleForm.setText(title);

								editCode.addTextChangedListener(new TextWatcher()
								{
									@Override
									public void beforeTextChanged(CharSequence s, int start, int count, int after)
									{
										inputCode.setErrorEnabled(false);
										enableNextStep();
									}

									@Override
									public void onTextChanged(CharSequence s, int start, int before, int count)
									{
									}

									@Override
									public void afterTextChanged(Editable s)
									{
									}
								});
							}
						}
					}
				}
			});
		}
		catch(Exception e)
		{
			System.out.println("CardViewActivity:refresh - Exception: " + e);

			if(Common.DEBUG)
			{
				e.printStackTrace();
			}
		}
	}

	public void sendData(View view)
	{
		try
		{
			Utils.hideCard(cardForm);

			if(StringUtils.isAlphanumeric(editCode.getText().toString()))
			{
				inputCode.setErrorEnabled(false);
				SendIdentificationKeyAsyncTask task = new SendIdentificationKeyAsyncTask(this, true, editCode.getText().toString(), companyId);

				if(task.execute().get().equals(ApiConnection.OK))
				{
					Utils.showCard(cardOk);
				}
				else
				{
					Utils.showCard(cardRetry);
				}
			}
			else
			{
				Utils.showCard(cardRetry);
				inputCode.setErrorEnabled(true);
				inputCode.setError(getString(R.string.code_alphanumeric));
			}
		}
		catch(Exception e)
		{
			System.out.println("CardViewActivity:sendData - Exception: " + e);

			if(Common.DEBUG)
			{
				e.printStackTrace();
			}
		}
	}

	public void sendAgain(View view)
	{
		try
		{
			inputCode.setErrorEnabled(false);
			Utils.hideCard(cardRetry);
			sendData(view);
		}
		catch(Exception e)
		{
			System.out.println("CardViewActivity:sendAgain - Exception: " + e);

			if(Common.DEBUG)
			{
				e.printStackTrace();
			}
		}
	}

	public void byeCard(View view)
	{
		try
		{
			Utils.hideCard(cardForm);
			Utils.hideCard(cardOk);
			Utils.hideCard(cardRetry);
			Utils.hideCard(cardPayout);
			Utils.hideCard(cardSuscribe);
			RelativeLayout.LayoutParams p = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

			if(notifications != null)
			{
				if(notifications.size() > 0)
				{
					rcwCard.setVisibility(RecyclerView.VISIBLE);
					rlEmpty.setVisibility(RelativeLayout.GONE);
				}
				else
				{
					rcwCard.setVisibility(RecyclerView.GONE);
					cardForm.setVisibility(CardView.GONE);
					cardOk.setVisibility(CardView.GONE);
					cardPayout.setVisibility(CardView.GONE);
					cardSuscribe.setVisibility(CardView.GONE);
					cardRetry.setVisibility(CardView.GONE);
					rlEmpty.setVisibility(RelativeLayout.VISIBLE);
					p = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
				}
			}
			else
			{
				rcwCard.setVisibility(RecyclerView.GONE);
				cardForm.setVisibility(CardView.GONE);
				cardOk.setVisibility(CardView.GONE);
				cardPayout.setVisibility(CardView.GONE);
				cardSuscribe.setVisibility(CardView.GONE);
				cardRetry.setVisibility(CardView.GONE);
				rlEmpty.setVisibility(RelativeLayout.VISIBLE);
				p = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
			}

			if(Common.API_LEVEL >= Build.VERSION_CODES.JELLY_BEAN_MR1)
			{
				p.removeRule(RelativeLayout.BELOW);
			}

			rcwCard.setLayoutParams(p);
		}
		catch(Exception e)
		{
			System.out.println("CardViewActivity:byeCard - Exception: " + e);

			if(Common.DEBUG)
			{
				e.printStackTrace();
			}
		}
	}

	public void suscribeCompany(View v)
	{
		try
		{
			Realm realm	= Realm.getDefaultInstance();
			suscription	= realm.where(Suscription.class).equalTo(Suscription.KEY_API, companyId).findFirst();
			BlockedActivity.modifySubscriptions(getApplicationContext(), Common.BOOL_YES, false, companyId);
			Utils.hideCard(cardPayout);
			Utils.hideCard(cardSuscribe);
			txtTitle.setTextColor(colorTitle);
			txtSubTitleCollapsed.setTextColor(colorSubTitle);
			toolBar.setBackgroundColor(Color.parseColor(suscription.getColorHex()));
			Utils.tintColorScreen(this, suscription.getColorHex());

			RelativeLayout.LayoutParams p = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
			if(Common.API_LEVEL >= Build.VERSION_CODES.JELLY_BEAN_MR1)
			{
				p.removeRule(RelativeLayout.BELOW);
			}

			rcwCard.setLayoutParams(p);

			if(StringUtils.isNotEmpty(suscription.getIdentificationKey()) && suscription.getDataSent() == Common.BOOL_NO && suscription.getFollower() == Common.BOOL_YES)
			{
				rlEmpty.setVisibility(RelativeLayout.GONE);
				rcwCard.setVisibility(RecyclerView.GONE);
				Utils.showCard(cardForm);
				inputCode.setErrorEnabled(false);
				inputCode.setHint(suscription.getIdentificationKey());
				String title = getString(R.string.landing_card_form_text1) + " " + suscription.getIdentificationKey() + " " + getString(R.string.landing_card_form_text2);
				txtSubTitleForm.setText(title);

				editCode.addTextChangedListener(new TextWatcher()
				{
					@Override
					public void beforeTextChanged(CharSequence s, int start, int count, int after)
					{
						inputCode.setErrorEnabled(false);
						enableNextStep();
					}

					@Override
					public void onTextChanged(CharSequence s, int start, int before, int count)
					{
					}

					@Override
					public void afterTextChanged(Editable s)
					{
					}
				});
			}
		}
		catch(Exception e)
		{
			System.out.println("CardViewActivity:suscribeCompany - Exception: " + e);

			if(Common.DEBUG)
			{
				e.printStackTrace();
			}
		}
	}

	public void enableNextStep()
	{
		try
		{
			btnContinueForm.setEnabled(true);
			btnContinueForm.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.accent));
			btnContinueForm.setOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					sendData(v);
				}
			});
		}
		catch(Exception e)
		{
			System.out.println("CardViewActivity:enableNextStep - Exception: " + e);

			if(Common.DEBUG)
			{
				e.printStackTrace();
			}
		}
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		try
		{
			hideSoftKeyboard();
			Realm realm	= Realm.getDefaultInstance();
			suscription	= realm.where(Suscription.class).equalTo(Suscription.KEY_API, companyId).findFirst();

			if(suscription != null)
			{
				Snackbar snackBar	= null;

				if(item.toString().equals(getString(R.string.silence)))
				{
					//Agregado para capturar evento en Google Analytics
					GoogleAnalytics.getInstance(this).newTracker(Common.HASH_GOOGLEANALYTICS).send(	new HitBuilders.EventBuilder().setCategory("Company").setAction("SilenciarInCompany")
																									.setLabel("AccionUser").build());
					realm.beginTransaction();
					suscription.setSilenced(Utils.reverseBool(suscription.getSilenced()));
					realm.commitTransaction();
					refresh(false);
					snackBar = Snackbar.make(Clayout, getString(R.string.snack_silence), Snackbar.LENGTH_LONG).setAction(getString(R.string.undo), new View.OnClickListener()
					{
						@Override
						public void onClick(View v)
						{
							Realm realm	= Realm.getDefaultInstance();
							suscription	= realm.where(Suscription.class).equalTo(Suscription.KEY_API, companyId).findFirst();
							realm.beginTransaction();
							suscription.setSilenced(Utils.reverseBool(suscription.getSilenced()));
							realm.commitTransaction();
							refresh(false);
						}
					});
				}

				//Agregado para desactivar silencio
				if(item.toString().equals(getString(R.string.activate_notif)))
				{
					realm.beginTransaction();
					suscription.setSilenced(Utils.reverseBool(suscription.getSilenced()));
					realm.commitTransaction();
					refresh(false);
					snackBar = Snackbar.make(Clayout, getString(R.string.snack_unsilence), Snackbar.LENGTH_LONG).setAction(getString(R.string.undo), new View.OnClickListener()
					{
						@Override
						public void onClick(View v)
						{
							Realm realm	= Realm.getDefaultInstance();
							suscription	= realm.where(Suscription.class).equalTo(Suscription.KEY_API, companyId).findFirst();
							realm.beginTransaction();
							suscription.setSilenced(Utils.reverseBool(suscription.getSilenced()));
							realm.commitTransaction();
							refresh(false);
						}
					});
				}

				if(item.toString().equals(getString(R.string.empty_messages)))
				{
					//Agregado para capturar evento en Google Analytics
					GoogleAnalytics.getInstance(this).newTracker(Common.HASH_GOOGLEANALYTICS).send(	new HitBuilders.EventBuilder().setCategory("Company").setAction("VaciarInCompany")
																									.setLabel("AccionUser").build());

					//Modificación para ejecutar proceso en background
					if(StringUtils.isNotEmpty(companyId))
					{
						MessageHelper.emptyCompany(companyId, Common.BOOL_YES);
					}

					refresh(true);
					snackBar = Snackbar.make(Clayout, getString(R.string.snack_empty), Snackbar.LENGTH_LONG).setAction(getString(R.string.undo), new View.OnClickListener()
					{
						@Override
						public void onClick(View v)
						{
							//Modificación para ejecutar proceso en background
							if(StringUtils.isNotEmpty(companyId))
							{
								MessageHelper.emptyCompany(companyId, Common.BOOL_NO);
							}

							refresh(false);
						}
					});
				}

				//Se reemplaza Bloquear por Desuscribir
				if(item.toString().equals(getString(R.string.landing_unsuscribe)))
				{
					//Agregado para capturar evento en Google Analytics
					GoogleAnalytics.getInstance(this).newTracker(Common.HASH_GOOGLEANALYTICS).send(	new HitBuilders.EventBuilder().setCategory("Company").setAction("BloquearInCompany")
																									.setLabel("AccionUser").build());
					BlockedActivity.modifySubscriptions(getApplicationContext(), Common.BOOL_NO, true, companyId);
					Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
					intent.putExtra(Common.KEY_ID, companyId);
					intent.putExtra(Suscription.KEY_BLOCKED, Common.BOOL_YES);
					intent.putExtra(Common.KEY_REFRESH, false);
					startActivity(intent);
					finish();
				}

				//Al igual que Silenciar/Activar, esta es la opción para suscribir
				if(item.toString().equals(getString(R.string.landing_suscribe)))
				{
					BlockedActivity.modifySubscriptions(getApplicationContext(), Common.BOOL_YES, false, companyId);
					txtTitle.setTextColor(colorTitle);
					txtSubTitleCollapsed.setTextColor(colorSubTitle);
					toolBar.setBackgroundColor(Color.parseColor(suscription.getColorHex()));
					Utils.tintColorScreen(this, suscription.getColorHex());
				}

				//Agregado para redirigir a la landing de la company
				if(item.toString().equals(getString(R.string.settings)))
				{
					goToSettings();
				}

				Utils.setStyleSnackBar(snackBar, getApplicationContext());
			}
		}
		catch(Exception e)
		{
			System.out.println("CardViewActivity:onOptionsItemSelected - Exception: " + e);

			if(Common.DEBUG)
			{
				e.printStackTrace();
			}
		}

		return super.onOptionsItemSelected(item);
	}

	/**
	 * Agregado para prevenir dos tareas dentro de la pila para esta activity
	 */
	@Override
	public void onBackPressed()
	{
		try
		{
			hideSoftKeyboard();
			Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			intent.putExtra(Common.KEY_REFRESH, false);
			startActivity(intent);
			finish();
		}
		catch(Exception e)
		{
			System.out.println("CardViewActivity:onBackPressed - Exception: " + e);

			if(Common.DEBUG)
			{
				e.printStackTrace();
			}
		}
	}

	/**
	 * Agregado para esconder el teclado cuando se oprime back
	 */
	private void hideSoftKeyboard()
	{
		try
		{
			getWindow().setSoftInputMode(originalSoftInputMode);

			// Hide keyboard when paused.
			View currentFocusView = getCurrentFocus();

			if(currentFocusView != null)
			{
				InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.RESULT_UNCHANGED_SHOWN);
			}
		}
		catch(Exception e)
		{
			System.out.println("CardViewActivity:hideSoftKeyboard - Exception: " + e);

			if(Common.DEBUG)
			{
				e.printStackTrace();
			}
		}
	}

	/**
	 * Agregado para controlar estado del teclado
	 */
	protected void onResume()
	{
		super.onResume();

		try
		{
			if(Utils.checkSesion(this, Common.ANOTHER_SCREEN))
			{
				refresh(false);
			}

			Window window = getWindow();

			// Overriding the soft input mode of the Window so that the Send and Cancel buttons appear above the soft keyboard when either EditText field gains focus.
			// We cache the mode in order to set it back to the original value when the Fragment is paused.
			originalSoftInputMode = window.getAttributes().softInputMode;
			window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
		}
		catch(Exception e)
		{
			System.out.println("CardViewActivity:onResume - Exception: " + e);

			if(Common.DEBUG)
			{
				e.printStackTrace();
			}
		}
	}

	protected void onPause()
	{
		super.onPause();
	}

	public void onStart()
	{
		super.onStart();

		try
		{
			if(!Common.DEBUG)
			{
				Appboy.getInstance(CardViewActivity.this).openSession(CardViewActivity.this);
			}
		}
		catch(Exception e)
		{
			System.out.println("CardViewActivity:onStart - Exception: " + e);

			if(Common.DEBUG)
			{
				e.printStackTrace();
			}
		}
	}

	public void onStop()
	{
		super.onStop();

		try
		{
			if(!Common.DEBUG)
			{
				Appboy.getInstance(CardViewActivity.this).closeSession(CardViewActivity.this);
			}
		}
		catch(Exception e)
		{
			System.out.println("CardViewActivity:onStop - Exception: " + e);

			if(Common.DEBUG)
			{
				e.printStackTrace();
			}
		}
	}
}