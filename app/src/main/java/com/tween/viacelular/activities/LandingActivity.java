package com.tween.viacelular.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.method.LinkMovementMethod;
import android.view.Menu;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.squareup.picasso.Picasso;
import com.tween.viacelular.R;
import com.tween.viacelular.models.Suscription;
import com.tween.viacelular.utils.Common;
import com.tween.viacelular.utils.StringUtils;
import com.tween.viacelular.utils.Utils;
import de.hdodenhof.circleimageview.CircleImageView;
import io.realm.Realm;

public class LandingActivity extends AppCompatActivity implements AppBarLayout.OnOffsetChangedListener
{
	private static final float	PERCENTAGE_TO_SHOW_TITLE_AT_TOOLBAR	= 0.9f;
	private static final float	PERCENTAGE_TO_HIDE_TITLE_DETAILS	= 0.3f;
	private static final int	ALPHA_ANIMATIONS_DURATION			= 100;
	private boolean				mIsTheTitleVisible					= false;
	private boolean				mIsTheTitleContainerVisible			= true;
	private Suscription			suscription							= null;
	private String				companyId							= "";
	private String				section								= "";
	private String				color								= Common.COLOR_ACTION;
	private LinearLayout		mTitleContainer;
	private TextView			txtTitle, txtEmail, txtPhone, txtBigTitle, txtSubTitle, txtSubTitleCollapsed, txtAbout;
	private Toolbar				toolBar;
	private Context				context;
	private CircleImageView		circleView;
	private Button				btnSuscribe;
	private ImageView			logo;
	private float				scale;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		try
		{
			super.onCreate(savedInstanceState);
			setContentView(R.layout.activity_landing);

			toolBar											= (Toolbar) findViewById(R.id.toolBar);
			CollapsingToolbarLayout collapsingToolbarLayout	= (CollapsingToolbarLayout) findViewById(R.id.collapsingToolbarLayout);
			txtTitle										= (TextView) findViewById(R.id.txtTitle);
			txtBigTitle										= (TextView) findViewById(R.id.txtBigTitle);
			txtSubTitle										= (TextView) findViewById(R.id.txtSubTitle);
			txtSubTitleCollapsed							= (TextView) findViewById(R.id.txtSubTitleCollapsed);
			mTitleContainer									= (LinearLayout) findViewById(R.id.rlTitle);
			final AppBarLayout mAppBarLayout				= (AppBarLayout) findViewById(R.id.appBarLayout);
			txtAbout										= (TextView) findViewById(R.id.txtAbout);
			TextView txtUrl									= (TextView) findViewById(R.id.txtUrl);
			TextView txtExample								= (TextView) findViewById(R.id.txtExample);
			TextView txtContact								= (TextView) findViewById(R.id.txtContact);
			txtEmail										= (TextView) findViewById(R.id.txtEmail);
			txtPhone										= (TextView) findViewById(R.id.txtPhone);
			RecyclerView rcwCard							= (RecyclerView) findViewById(R.id.rcwCard);
			RelativeLayout rlExample						= (RelativeLayout) findViewById(R.id.rlExample);
			View contact									= findViewById(R.id.contentContact);
			btnSuscribe										= (Button) findViewById(R.id.btnSuscribe);
			circleView										= (CircleImageView) findViewById(R.id.circleView);
			ImageView ivPlaceholder							= (ImageView) findViewById(R.id.ivPlaceholder);
			final ImageView ibBack							= (ImageView) findViewById(R.id.ibBack);
			logo											= (ImageView) findViewById(R.id.logo);
			View dividerTitle								= findViewById(R.id.dividerTitle);
			ImageView iconShowNotif							= (ImageView) findViewById(R.id.iconShowNotif);
			TextView txtShowNotif							= (TextView) findViewById(R.id.txtShowNotif);
			TextView txtId									= (TextView) findViewById(R.id.txtId);
			LinearLayout llId								= (LinearLayout) findViewById(R.id.llId);
			scale											= getResources().getDisplayMetrics().density;
			toolBar.setTitle("");
			toolBar.setSubtitle("");

			if(mAppBarLayout != null)
			{
				mAppBarLayout.addOnOffsetChangedListener(this);
			}

			setSupportActionBar(toolBar);
			startAlphaAnimation(txtTitle, 0, View.INVISIBLE, this);

			if(txtUrl != null)
			{
				txtUrl.setMovementMethod(LinkMovementMethod.getInstance());
			}

			txtEmail.setTag(1);
			txtPhone.setTag(2);

			context						= getApplicationContext();
			final Intent intentRecive	= getIntent();

			if(intentRecive != null)
			{
				Realm realm		= Realm.getDefaultInstance();
				companyId	= intentRecive.getStringExtra(Common.KEY_ID);
				section		= intentRecive.getStringExtra(Common.KEY_SECTION);

				if(StringUtils.isNotEmpty(companyId))
				{
					suscription	= realm.where(Suscription.class).equalTo(Suscription.KEY_API, companyId).findFirst();

					if(suscription != null)
					{
						txtBigTitle.setText(suscription.getName());
						txtSubTitle.setText(suscription.getIndustry());
						txtTitle.setText(suscription.getName());
						txtSubTitleCollapsed.setText(suscription.getIndustry());

						//Validaciones para mostrar o no campos según disponibilidad de datos
						if(StringUtils.isNotEmpty(suscription.getAbout()))
						{
							txtAbout.setText(suscription.getAbout());
						}
						else
						{
							//Validar con PO para ver si cuando no hay texto no mostramos nada en vez del placeholder siguiente
							txtAbout.setText(getString(R.string.landing_title, suscription.getName()));
						}

						//Momentaneamente queda oculta la sección de Mensajes de Ejemplos por falta de definición, buscar código implementado hasta el momento en VC-954
						rlExample.setVisibility(RelativeLayout.GONE);
						rcwCard.setVisibility(RecyclerView.GONE);

						if(StringUtils.isEmpty(suscription.getUrl()) && StringUtils.isEmpty(suscription.getEmail()) && StringUtils.isEmpty(suscription.getPhone()))
						{
							contact.setVisibility(View.GONE);
						}
						else
						{
							contact.setVisibility(View.VISIBLE);
							ImageView iconUrl	= (ImageView) findViewById(R.id.iconUrl);
							ImageView iconEmail	= (ImageView) findViewById(R.id.iconEmail);
							ImageView iconPhone	= (ImageView) findViewById(R.id.iconPhone);

							if(StringUtils.isNotEmpty(suscription.getUrl()))
							{
								if(txtUrl != null)
								{
									txtUrl.setText(suscription.getUrl());
									iconUrl.setVisibility(ImageView.VISIBLE);
									txtUrl.setVisibility(TextView.VISIBLE);
									final Activity activity = this;
									txtUrl.setOnClickListener(new View.OnClickListener()
									{
										@Override
										public void onClick(final View view)
										{
											//Agregado para capturar evento en Google Analytics
											GoogleAnalytics.getInstance(activity).newTracker(Common.HASH_GOOGLEANALYTICS).send(	new HitBuilders.EventBuilder().setCategory("Company")
													.setAction("WebLanding").setLabel("AccionUser").build());
										}
									});
								}
							}
							else
							{
								iconUrl.setVisibility(ImageView.GONE);

								if(txtUrl != null)
								{
									txtUrl.setVisibility(TextView.GONE);
								}
							}

							if(StringUtils.isNotEmpty(suscription.getEmail()))
							{
								txtEmail.setText(suscription.getEmail());
								iconEmail.setVisibility(ImageView.VISIBLE);
								txtEmail.setVisibility(TextView.VISIBLE);
							}
							else
							{
								iconEmail.setVisibility(ImageView.GONE);
								txtEmail.setVisibility(TextView.GONE);
							}

							if(StringUtils.isNotEmpty(suscription.getPhone()))
							{
								txtPhone.setText(suscription.getPhone());
								iconPhone.setVisibility(ImageView.VISIBLE);
								txtPhone.setVisibility(TextView.VISIBLE);
							}
							else
							{
								iconPhone.setVisibility(ImageView.GONE);
								txtPhone.setVisibility(TextView.GONE);
							}
						}

						String image	= suscription.getImage();
						color			= suscription.getColorHex();

						if(StringUtils.isEmpty(color))
						{
							color = Common.COLOR_ACTION;
						}

						if(Utils.isLightColor(color, this))
						{
							toolBar.setTitleTextColor(Color.BLACK);
							collapsingToolbarLayout.setCollapsedTitleTextColor(Color.BLACK);
							collapsingToolbarLayout.setExpandedTitleColor(Color.BLACK);
							toolBar.setSubtitleTextColor(Color.DKGRAY);
							txtContact.setTextColor(Color.BLACK);
							txtExample.setTextColor(Color.BLACK);
							txtTitle.setTextColor(Color.BLACK);
							txtBigTitle.setTextColor(Color.BLACK);
							txtSubTitle.setTextColor(Color.DKGRAY);
							txtSubTitleCollapsed.setTextColor(Color.DKGRAY);
							ibBack.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_arrow_back_black_24dp));
						}
						else
						{
							ibBack.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.back));
							txtContact.setTextColor(Color.parseColor(color));
							txtExample.setTextColor(Color.parseColor(color));
						}

						ivPlaceholder.setBackgroundColor(Color.parseColor(color));

						//Modificación para migrar a asynctask la descarga de imágenes
						if(StringUtils.isNotEmpty(image))
						{
							//Modificación de librería para recargar imagenes a mientras se está viendo el listado y optimizar vista
							Picasso.with(getApplicationContext()).load(image).placeholder(R.mipmap.ic_launcher).into(circleView);
							Picasso.with(getApplicationContext()).load(image).placeholder(R.mipmap.ic_launcher).into(logo);
						}
						else
						{
							//Mostrar el logo de Vloom si no tiene logo
							Picasso.with(getApplicationContext()).load(Suscription.ICON_APP).placeholder(R.mipmap.ic_launcher).into(circleView);
							Picasso.with(getApplicationContext()).load(Suscription.ICON_APP).placeholder(R.mipmap.ic_launcher).into(logo);
						}

						Utils.tintColorScreen(this, color);
						collapsingToolbarLayout.setStatusBarScrimColor(Color.parseColor(color));
						Utils.ampliarAreaTouch(ibBack, 150);
						ibBack.setOnClickListener(new View.OnClickListener()
						{
							@Override
							public void onClick(final View v)
							{
								onBackPressed();
							}
						});

						//Agregado para diferenciar vista cuando la company está añadida
						if(suscription.getFollower() == Common.BOOL_YES)
						{
							btnSuscribe.setText(getString(R.string.landing_suscribed));
							btnSuscribe.setTextColor(ContextCompat.getColor(context, R.color.accent));
						}
						else
						{
							btnSuscribe.setText(getString(R.string.landing_suscribe));
							btnSuscribe.setTextColor(ContextCompat.getColor(context, android.R.color.black));
						}
					}
				}
			}

			if(Common.API_LEVEL >= Build.VERSION_CODES.LOLLIPOP)
			{
				if(circleView != null)
				{
					circleView.setElevation((float) 4);
				}

				if(mAppBarLayout != null)
				{
					mAppBarLayout.setElevation((float) 4);
				}
			}

			//Agregado para replicar función de ir Cards como estaba en profile
			if(suscription != null)
			{
				//Se deja siempre visible para ir a la pantalla cards
				dividerTitle.setVisibility(View.VISIBLE);
				iconShowNotif.setVisibility(ImageView.VISIBLE);
				txtShowNotif.setVisibility(TextView.VISIBLE);

				if(StringUtils.isNotEmpty(suscription.getIdentificationKey()))
				{
					txtId.setVisibility(TextView.VISIBLE);
					//TODO Cuando terminemos de definir esta funcionalidad mostramos llId y seguimos desarrollando el popup para editar el dato
				}
				else
				{
					txtId.setVisibility(TextView.GONE);
					llId.setVisibility(LinearLayout.GONE);
				}
			}
			else
			{
				dividerTitle.setVisibility(View.GONE);
				iconShowNotif.setVisibility(ImageView.GONE);
				txtShowNotif.setVisibility(TextView.GONE);
				txtId.setVisibility(TextView.GONE);
				llId.setVisibility(LinearLayout.GONE);
			}
		}
		catch(Exception e)
		{
			Utils.logError(this, getLocalClassName()+":onCreate - Exception:", e);
		}
	}

	public void goBack(View view)
	{
		onBackPressed();
	}

	public void goTo(View view)
	{
		try
		{
			String extraText;
			Integer action		= (Integer) view.getTag();

			if(action == 1)
			{
				extraText = txtEmail.getText().toString();
			}
			else
			{
				extraText = txtPhone.getText().toString();
			}

			Utils.goTo(this, action, extraText);
		}
		catch(Exception e)
		{
			Utils.logError(this, getLocalClassName()+":goTo - Exception:", e);
		}
	}

	public void viewCards(View view)
	{
		try
		{
			Intent intent = new Intent(getApplicationContext(), CardViewActivity.class);
			intent.putExtra(Common.KEY_ID, companyId);
			startActivity(intent);
			finish();
		}
		catch(Exception e)
		{
			Utils.logError(this, getLocalClassName()+":viewCards - Exception:", e);
		}
	}

	@Override
	public void onBackPressed()
	{
		try
		{
			Intent intent;

			//Agregado para volver a la activity que corresponda
			if(section.equals("card"))
			{
				intent = new Intent(getApplicationContext(), CardViewActivity.class);
				intent.putExtra(Common.KEY_ID, companyId);
			}
			else
			{
				if(section.equals("suscriptions"))
				{
					intent = new Intent(getApplicationContext(), SuscriptionsActivity.class);
				}
				else
				{
					if(section.equals("searchHome"))
					{
						intent = new Intent(getApplicationContext(), SearchActivity.class);
						intent.putExtra(Common.KEY_SECTION, "home");
					}
					else
					{
						intent = new Intent(getApplicationContext(), SearchActivity.class);
						intent.putExtra(Common.KEY_SECTION, "suscriptions");
					}
				}
			}

			startActivity(intent);
			finish();
		}
		catch(Exception e)
		{
			Utils.logError(this, getLocalClassName()+":onBackPressed - Exception:", e);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		return true;
	}

	@Override
	public void onOffsetChanged(AppBarLayout appBarLayout, int offset)
	{
		try
		{
			int maxScroll		= appBarLayout.getTotalScrollRange();
			float percentage	= (float) Math.abs(offset) / (float) maxScroll;
			handleAlphaOnTitle(percentage);
			handleToolbarTitleVisibility(percentage);
		}
		catch(Exception e)
		{
			Utils.logError(this, getLocalClassName()+":onOffsetChanged - Exception:", e);
		}
	}

	private void handleToolbarTitleVisibility(float percentage)
	{
		try
		{
			if(percentage >= PERCENTAGE_TO_SHOW_TITLE_AT_TOOLBAR)
			{
				if(!mIsTheTitleVisible)
				{
					startAlphaAnimation(txtTitle, ALPHA_ANIMATIONS_DURATION, View.VISIBLE, this);
					mIsTheTitleVisible = true;
				}
			}
			else
			{
				if(mIsTheTitleVisible)
				{
					startAlphaAnimation(txtTitle, ALPHA_ANIMATIONS_DURATION, View.INVISIBLE, this);
					mIsTheTitleVisible = false;
				}
			}
		}
		catch(Exception e)
		{
			Utils.logError(this, getLocalClassName()+":handleToolbarTitleVisibility - Exception:", e);
		}
	}

	private void handleAlphaOnTitle(float percentage)
	{
		try
		{
			int dpAsPixels;

			if(percentage >= PERCENTAGE_TO_HIDE_TITLE_DETAILS)
			{
				if(mIsTheTitleContainerVisible)
				{
					//Al achicar la barra
					startAlphaAnimation(mTitleContainer, ALPHA_ANIMATIONS_DURATION, View.INVISIBLE, this);
					mIsTheTitleContainerVisible = false;
					//Mostrar toolBar
					circleView.setVisibility(CircleImageView.VISIBLE);
					txtTitle.setVisibility(TextView.VISIBLE);
					txtSubTitleCollapsed.setVisibility(TextView.VISIBLE);
					//Ocultar collapsed view
					logo.setVisibility(CircleImageView.GONE);
					txtBigTitle.setVisibility(TextView.GONE);
					txtSubTitle.setVisibility(TextView.GONE);
					btnSuscribe.setVisibility(Button.GONE);
					toolBar.setBackgroundColor(Color.parseColor(color));
					dpAsPixels	= (int) (20*scale + 0.5f);
					txtAbout.setPadding(0, dpAsPixels, 0, 0);

					if(Common.API_LEVEL >= Build.VERSION_CODES.LOLLIPOP)
					{
						toolBar.setElevation((float) 4);
					}
				}
			}
			else
			{
				if(!mIsTheTitleContainerVisible)
				{
					//Al expandir la barra
					startAlphaAnimation(mTitleContainer, ALPHA_ANIMATIONS_DURATION, View.VISIBLE, this);
					mIsTheTitleContainerVisible = true;
					//Ocultar toolBar
					circleView.setVisibility(CircleImageView.GONE);
					txtTitle.setVisibility(TextView.GONE);
					txtSubTitleCollapsed.setVisibility(TextView.GONE);
					//Mostrar collapsed view
					logo.setVisibility(CircleImageView.VISIBLE);
					txtBigTitle.setVisibility(TextView.VISIBLE);
					txtSubTitle.setVisibility(TextView.VISIBLE);
					btnSuscribe.setVisibility(Button.VISIBLE);
					toolBar.setBackgroundColor(Color.TRANSPARENT);
					dpAsPixels	= (int) (10*scale + 0.5f);
					txtAbout.setPadding(0, dpAsPixels, 0, 0);

					if(Common.API_LEVEL >= Build.VERSION_CODES.LOLLIPOP)
					{
						toolBar.setElevation((float) 0);
					}
				}
			}
		}
		catch(Exception e)
		{
			Utils.logError(this, getLocalClassName()+":handleAlphaOnTitle - Exception:", e);
		}
	}

	public static void startAlphaAnimation(View v, long duration, int visibility, Context context)
	{
		try
		{
			AlphaAnimation alphaAnimation = (visibility == View.VISIBLE) ? new AlphaAnimation(0f, 1f) : new AlphaAnimation(1f, 0f);
			alphaAnimation.setDuration(duration);
			alphaAnimation.setFillAfter(true);
			v.startAnimation(alphaAnimation);
		}
		catch(Exception e)
		{
			Utils.logError(context, "LandingActivity:onOffsetChanged - Exception:", e);
		}
	}

	public void suscribe(View view)
	{
		try
		{
			//TODO: Contemplar caso de añadir una empresa sugerida y medir con Analytics: (Category:Company - Action:AgregarSugerencia - Label:AccionUser)
			//Agregado para verificar si la company ya estaba o no suscripta
			Realm realm				= Realm.getDefaultInstance();
			Suscription suscription	= realm.where(Suscription.class).equalTo(Suscription.KEY_API, companyId).findFirst();

			if(suscription != null)
			{
				if(Utils.reverseBool(suscription.getFollower()) == Common.BOOL_YES)
				{
					//Agregado para capturar evento en Google Analytics
					GoogleAnalytics.getInstance(this).newTracker(Common.HASH_GOOGLEANALYTICS).send(	new HitBuilders.EventBuilder().setCategory("Company").setAction("AgregarLanding")
																									.setLabel("AccionUser").build());
				}
				else
				{
					//Agregado para capturar evento en Google Analytics
					GoogleAnalytics.getInstance(this).newTracker(Common.HASH_GOOGLEANALYTICS).send(	new HitBuilders.EventBuilder().setCategory("Company").setAction("BloquearLanding")
																									.setLabel("AccionUser").build());
				}

				HomeActivity.modifySubscriptions(LandingActivity.this, Utils.reverseBool(suscription.getFollower()), false, companyId, false);

				//Agregado para redirigir a la pantallas cards para pedir la identificación del cliente si es necesario
				if(StringUtils.isNotEmpty(suscription.getIdentificationKey()) && Utils.reverseBool(suscription.getFollower()) == Common.BOOL_YES)
				{
					Intent intent = new Intent(context, CardViewActivity.class);
					intent.putExtra(Common.KEY_ID, companyId);
					startActivity(intent);
				}
				else
				{
					if(Utils.reverseBool(suscription.getFollower()) == Common.BOOL_YES)
					{
						//Modificación para no redirigir al suscribir si no se necesita la identificación, en este caso actualizamos el layout
						btnSuscribe.setText(getString(R.string.landing_suscribed));
						btnSuscribe.setTextColor(ContextCompat.getColor(context, R.color.accent));
					}
					else
					{
						//Modificación para no redirigir al suscribir si no se necesita la identificación, en este caso actualizamos el layout
						btnSuscribe.setText(getString(R.string.landing_suscribe));

						if(Utils.isLightColor(color, this))
						{
							btnSuscribe.setTextColor(ContextCompat.getColor(context, android.R.color.white));
						}
						else
						{
							btnSuscribe.setTextColor(ContextCompat.getColor(context, R.color.black));
						}
					}
				}
			}
		}
		catch(Exception e)
		{
			Utils.logError(this, getLocalClassName()+":onOffsetChanged - Exception:", e);
		}
	}
}