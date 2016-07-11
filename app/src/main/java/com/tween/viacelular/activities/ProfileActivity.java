package com.tween.viacelular.activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.squareup.picasso.Picasso;
import com.tween.viacelular.R;
import com.tween.viacelular.data.Company;
import com.tween.viacelular.models.Suscription;
import com.tween.viacelular.utils.Common;
import com.tween.viacelular.utils.StringUtils;
import com.tween.viacelular.utils.Utils;
import de.hdodenhof.circleimageview.CircleImageView;
import io.realm.Realm;

public class ProfileActivity extends AppCompatActivity
{
	private TextView	txtEmail;
	private TextView	txtPhone;
	private Suscription	client		= null;
	private String		companyId	= "";
	private String		page;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		Realm realm = null;

		try
		{
			super.onCreate(savedInstanceState);
			setContentView(R.layout.activity_profile);

			Toolbar toolBar				= (Toolbar) findViewById(R.id.toolBarProfile);
			TextView txtTitle			= (TextView) findViewById(R.id.txtTitle);
			TextView rowText			= (TextView) findViewById(R.id.rowText);
			TextView rowSubText			= (TextView) findViewById(R.id.rowSubText);
			TextView txtUrl				= (TextView) findViewById(R.id.txtUrl);
			TextView txtContact			= (TextView) findViewById(R.id.txtContact);
			CircleImageView circleView	= (CircleImageView) findViewById(R.id.circleView);
			View dividerContact			= findViewById(R.id.dividerContact);
			txtEmail					= (TextView) findViewById(R.id.txtEmail);
			txtPhone					= (TextView) findViewById(R.id.txtPhone);
			View contact				= findViewById(R.id.contentContact);

			txtUrl.setMovementMethod(LinkMovementMethod.getInstance());
			txtEmail.setTag(1);
			txtPhone.setTag(2);
			txtTitle.setText(getString(R.string.landing_title, getString(R.string.app_name)));
			final Context context		= getApplicationContext();
			setSupportActionBar(toolBar);
			setTitle(getString(R.string.settings));
			final Intent intentRecive	= getIntent();

			if(intentRecive != null)
			{
				companyId		= intentRecive.getStringExtra(Common.KEY_ID);
				String mongoId	= intentRecive.getStringExtra(Common.KEY_IDMONGO);
				page			= intentRecive.getStringExtra(Common.KEY_SECTION);

				if(StringUtils.isNotEmpty(companyId) || StringUtils.isNotEmpty(mongoId))
				{
					realm	= Realm.getDefaultInstance();
					client	= realm.where(Suscription.class).equalTo(Suscription.KEY_API, companyId).findFirst();

					if(client != null)
					{
						//Validaciones para mostrar o no campos según disponibilidad de datos
						if(StringUtils.isNotEmpty(client.getAbout()))
						{
							txtTitle.setText(client.getAbout());
						}
						else
						{
							//Validar con PO para ver si cuando no hay texto no mostramos nada en vez del placeholder siguiente
							txtTitle.setText(getString(R.string.landing_title, client.getName()));
						}

						if(StringUtils.isEmpty(client.getUrl()) && StringUtils.isEmpty(client.getEmail()) && StringUtils.isEmpty(client.getPhone()))
						{
							contact.setVisibility(View.GONE);
						}
						else
						{
							contact.setVisibility(View.VISIBLE);
							ImageView iconUrl	= (ImageView) findViewById(R.id.iconUrl);
							ImageView iconEmail	= (ImageView) findViewById(R.id.iconEmail);
							ImageView iconPhone	= (ImageView) findViewById(R.id.iconPhone);

							if(StringUtils.isNotEmpty(client.getUrl()))
							{
								txtUrl.setText(client.getUrl());
								iconUrl.setVisibility(ImageView.VISIBLE);
								txtUrl.setVisibility(TextView.VISIBLE);
							}
							else
							{
								iconUrl.setVisibility(ImageView.GONE);
								txtUrl.setVisibility(TextView.GONE);
							}

							if(StringUtils.isNotEmpty(client.getEmail()))
							{
								txtEmail.setText(client.getEmail());
								iconEmail.setVisibility(ImageView.VISIBLE);
								txtEmail.setVisibility(TextView.VISIBLE);
							}
							else
							{
								iconEmail.setVisibility(ImageView.GONE);
								txtEmail.setVisibility(TextView.GONE);
							}

							if(StringUtils.isNotEmpty(client.getPhone()))
							{
								txtPhone.setText(client.getPhone());
								iconPhone.setVisibility(ImageView.VISIBLE);
								txtPhone.setVisibility(TextView.VISIBLE);
							}
							else
							{
								iconPhone.setVisibility(ImageView.GONE);
								txtPhone.setVisibility(TextView.GONE);
							}
						}

						String image	= client.getImage();
						String color	= client.getColorHex();
						rowText.setText(client.getName());
						rowSubText.setText(client.getIndustry());

						if(StringUtils.isEmpty(color))
						{
							color = Common.COLOR_ACTION;
						}

						toolBar.setBackgroundColor(Color.parseColor(color));

						if(Utils.isLightColor(color))
						{
							toolBar.setTitleTextColor(Color.BLACK);
							toolBar.setSubtitleTextColor(Color.DKGRAY);
							toolBar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp);
							txtContact.setTextColor(Color.BLACK);
							toolBar.setOverflowIcon(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_more_vert_black_24dp));
						}
						else
						{
							toolBar.setNavigationIcon(R.drawable.back);
							txtContact.setTextColor(Color.parseColor(color));
						}

						//Modificación para migrar a asynctask la descarga de imágenes
						if(StringUtils.isNotEmpty(image))
						{
							//Modificación de librería para recargar imagenes a mientras se está viendo el listado y optimizar vista
							Picasso.with(getApplicationContext()).load(image).placeholder(R.drawable.ic_launcher).into(circleView);
						}

						Utils.tintColorScreen(this, color);

						toolBar.setNavigationOnClickListener(new View.OnClickListener()
						{
							@Override
							public void onClick(final View v)
							{
								onBackPressed();
							}
						});
					}
					else
					{
						setTitle(getString(R.string.app_name));
						Utils.tintColorScreen(this, Common.COLOR_ACTION);
					}
				}
				else
				{
					setTitle(getString(R.string.app_name));
					Utils.tintColorScreen(this, Common.COLOR_ACTION);
				}
			}
			else
			{
				setTitle(getString(R.string.app_name));
				Utils.tintColorScreen(this, Common.COLOR_ACTION);
			}
		}
		catch(Exception e)
		{
			System.out.println("ProfileActivity:OnCreate - Exception: " + e);
			if(Common.DEBUG)
			{
				e.printStackTrace();
			}
		}
	}

	@Override
	public void onBackPressed()
	{
		try
		{
			Intent intent = null;

			if(page != null)
			{
				if(page.equals("card"))
				{
					intent = new Intent(getApplicationContext(), CardViewActivity.class);
					intent.putExtra(Common.KEY_ID, companyId);
				}
				else
				{
					intent = new Intent(getApplicationContext(), SuscriptionsActivity.class);
				}
			}
			else
			{
				intent = new Intent(getApplicationContext(), SuscriptionsActivity.class);
			}

			startActivity(intent);
			finish();
		}
		catch(Exception e)
		{
			System.out.println("ProfileActivity:onBackPressed - Exception: " + e);
			if(Common.DEBUG)
			{
				e.printStackTrace();
			}
		}
	}

	public void unSuscribe(View view)
	{
		Realm realm = null;

		try
		{
			if(client == null)
			{
				realm	= Realm.getDefaultInstance();
				client	= realm.where(Suscription.class).equalTo(Suscription.KEY_API, companyId).findFirst();
			}

			BlockedActivity.modifySubscriptions(ProfileActivity.this, Common.BOOL_NO, false, companyId, true);
			Intent intent = new Intent(getApplicationContext(), LandingActivity.class);
			intent.putExtra(Common.KEY_ID, client.getCompanyId());
			startActivity(intent);
			finish();
		}
		catch(Exception e)
		{
			System.out.println("CardViewActivity:unSuscribe - Exception: " + e);
			if(Common.DEBUG)
			{
				e.printStackTrace();
			}
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
			System.out.println("CardViewActivity:unSuscribe - Exception: " + e);
			if(Common.DEBUG)
			{
				e.printStackTrace();
			}
		}
	}

	public void block(View view)
	{
		Realm realm = null;

		try
		{
			if(client == null)
			{
				realm	= Realm.getDefaultInstance();
				client	= realm.where(Suscription.class).equalTo(Suscription.KEY_API, companyId).findFirst();
			}

			//Se centralizó el comportamiento
			BlockedActivity.modifySubscriptions(ProfileActivity.this, Common.BOOL_NO, false, companyId, true);
			Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
			intent.putExtra(Common.KEY_REFRESH, false);
			intent.putExtra(Common.KEY_ID, companyId);
			intent.putExtra(Company.KEY_BLOCKED, client.getBlocked());
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

	public void goTo(View view)
	{
		try
		{
			String extraText	= "";
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
			System.out.println("LandingActivity:goTo - Exception: " + e);
			if(Common.DEBUG)
			{
				e.printStackTrace();
			}
		}
	}
}