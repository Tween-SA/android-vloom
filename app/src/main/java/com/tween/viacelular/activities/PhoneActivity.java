package com.tween.viacelular.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.afollestad.materialdialogs.MaterialDialog;
import com.facebook.FacebookSdk;
import com.tween.viacelular.R;
import com.tween.viacelular.asynctask.RegisterPhoneAsyncTask;
import com.tween.viacelular.models.Isp;
import com.tween.viacelular.models.Land;
import com.tween.viacelular.models.User;
import com.tween.viacelular.utils.Common;
import com.tween.viacelular.utils.StringUtils;
import com.tween.viacelular.utils.Utils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import io.realm.Realm;
import io.realm.RealmResults;

public class PhoneActivity extends AppCompatActivity
{
	private Button			btnCountry;
	private Button			btnContinue;
	private TextView		inputCountry;
	private EditText		editPhone;
	private TextInputLayout	inputPhone;
	private int				originalSoftInputMode;
	private int				maxLenght		= 0;
	private int				minLenght		= 0;
	private String			format			= "";
	private boolean			foundCountry	= false;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		//Agregado para capturar excepciones
		try
		{
			super.onCreate(savedInstanceState);
			setContentView(R.layout.activity_phone);
			setTitle(R.string.title_activity_phone);
			Toolbar toolBar	= (Toolbar) findViewById(R.id.toolbarRegister);
			btnCountry		= (Button) findViewById(R.id.btnCountry);
			btnContinue		= (Button) findViewById(R.id.btnContinue);
			inputCountry	= (TextView) findViewById(R.id.inputCountry);
			editPhone		= (EditText) findViewById(R.id.editPhone);
			inputPhone		= (TextInputLayout) findViewById(R.id.inputPhone);
			setSupportActionBar(toolBar);
			Utils.tintColorScreen(this, Common.COLOR_ACTION);

			if(Utils.checkSesion(this, Common.PHONE_SCREEN))
			{
				inputCountry.setText(Common.CODE_FORMAT);
				//La importación de Country desde API se traslado al onPostExecute de SplashAsyncTask
				inputPhone.setErrorEnabled(false);

				//Agregado para habilitar el botón luego de terminar de escribir en el input
				editPhone.addTextChangedListener(new TextWatcher()
				{
					@Override
					public void beforeTextChanged(CharSequence s, int start, int count, int after)
					{
						inputPhone.setErrorEnabled(false);
						btnContinue.setEnabled(true);
						btnContinue.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.accent));
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
				//Agregado para que el usuario no tenga que tocar el botón para continuar cuando el teclado se oculta
				editPhone.setOnEditorActionListener(new TextView.OnEditorActionListener()
				{
					public boolean onEditorAction(TextView v, int actionId, KeyEvent event)
					{
						if(actionId == EditorInfo.IME_ACTION_SEND)
						{
							register(v);
							return true;
						}

						return false;
					}
				});
			}
		}
		catch(Exception e)
		{
			System.out.println("PhoneActivity:OnCreate - Exception: " + e);

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

	public void register(View view)
	{
		registerMethod();
	}

	public boolean registerMethod()
	{
		try
		{
			if(inputCountry.getText().toString().equals(Common.CODE_FORMAT))
			{
				Toast.makeText(getApplicationContext(), getString(R.string.no_country), Toast.LENGTH_SHORT).show();
				return false;
			}

			String phone = editPhone.getText().toString().trim();

			if(StringUtils.isEmpty(phone))
			{
				inputPhone.setErrorEnabled(true);
				inputPhone.setError(getString(R.string.phone_blank));
				return false;
			}
			else
			{
				inputPhone.setErrorEnabled(false);
			}

			if(!StringUtils.isValidPhone(phone))
			{
				inputPhone.setErrorEnabled(true);
				inputPhone.setError(getString(R.string.phone_invalid));
				return false;
			}
			else
			{
				inputPhone.setErrorEnabled(false);
			}

			//Agregado para validar dinámicamente según país seleccionado
			if(minLenght != 0 && maxLenght != 0)
			{
				if(phone.length() > maxLenght || phone.length() < minLenght)
				{
					inputPhone.setErrorEnabled(true);
					inputPhone.setError(getString(R.string.phone_lenght));
					return false;
				}
				else
				{
					inputPhone.setErrorEnabled(false);
				}
			}

			phone = inputCountry.getText().toString() + phone;
			new RegisterPhoneAsyncTask(PhoneActivity.this, phone).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
		}
		catch(Exception e)
		{
			System.out.println("PhoneActivity:register - Exception: " + e);

			if(Common.DEBUG)
			{
				e.printStackTrace();
			}
		}

		return true;
	}

	public void showCountries(View view)
	{
		try
		{
			hideSoftKeyboard();
			Realm realm						= Realm.getDefaultInstance();
			RealmResults<Land> countries	= realm.where(Land.class).findAllSorted(Common.KEY_NAME);
			List<String> listItems			= new ArrayList<>();
			String[] arrayCountries			= getResources().getStringArray(R.array.countries);
			CharSequence items[];

			if(countries.size() > 0)
			{
				for(Land land: countries)
				{
					listItems.add(land.getName());
				}
			}
			else
			{
				Collections.addAll(listItems, arrayCountries);
			}

			items = listItems.toArray(new CharSequence[listItems.size()]);

			new MaterialDialog.Builder(this)
				.title(R.string.countries)
				.items(items)
				.itemsCallback(new MaterialDialog.ListCallback()
				{
					@Override
					public void onSelection(MaterialDialog dialog, View view, int which, CharSequence text)
					{
						view.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.action));
						selectCountry(text.toString(), false);
					}
				}).show();
		}
		catch(Exception e)
		{
			System.out.println("PhoneActivity:showCountries - Exception: " + e);

			if(Common.DEBUG)
			{
				e.printStackTrace();
			}
		}
	}

	public void selectCountry(String lblCountry, boolean isCode)
	{
		try
		{
			if(StringUtils.isNotEmpty(lblCountry))
			{
				Realm realm						= Realm.getDefaultInstance();
				Land country					= realm.where(Land.class).equalTo(Common.KEY_NAME, lblCountry.trim()).findFirst();
				SharedPreferences preferences	= getApplicationContext().getSharedPreferences(Common.KEY_PREF, Context.MODE_PRIVATE);
				SharedPreferences.Editor editor	= preferences.edit();

				if(country != null)
				{
					btnCountry.setText(lblCountry);
					//Modificación para evitar retratamiento del 9 en AR y CL
					inputCountry.setText(country.getCode());
					//Agregado para actualizar el país seleccionado

					editor.putString(Land.KEY_API, country.getIsoCode());
					editor.apply();

					//Agregado para obtener dinámicamente la longitud a validar
					minLenght		= Integer.valueOf(country.getMinLength());
					maxLenght		= Integer.valueOf(country.getMaxLength());
					format			= country.getFormat();
					foundCountry	= true;
				}
				else
				{
					//Agregado para buscar país por isoCode en caso de que la api ip responda en otro idioma el país. Por ej: Spain
					if(isCode)
					{
						country = realm.where(Land.class).equalTo(Land.KEY_ISOCODE, lblCountry.trim()).findFirst();

						if(country != null)
						{
							btnCountry.setText(country.getName());
							//Modificación para evitar retratamiento del 9 en AR y CL
							inputCountry.setText(country.getCode());
							//Agregado para actualizar el país seleccionado

							editor.putString(Land.KEY_API, country.getIsoCode());
							editor.apply();

							//Agregado para obtener dinámicamente la longitud a validar
							minLenght		= Integer.valueOf(country.getMinLength());
							maxLenght		= Integer.valueOf(country.getMaxLength());
							format			= country.getFormat();
							foundCountry	= true;
						}
					}
					else
					{
						String[] arrayCountries	= getResources().getStringArray(R.array.countries);
						String[] arrayCodes		= getResources().getStringArray(R.array.codes);
						String[] arrayIsoCodes	= getResources().getStringArray(R.array.isoCodes);
						//Placeholder agregado por si no se pudo procesar el json y la api tampoco devolvió data
						String[] arrayFormats	= getResources().getStringArray(R.array.formats);
						String[] arrayMinLength	= getResources().getStringArray(R.array.minLength);
						String[] arrayMaxLength	= getResources().getStringArray(R.array.maxLength);

						for(int i = 0; i < arrayCountries.length; i++)
						{
							if(arrayCountries[i].equals(lblCountry))
							{
								btnCountry.setText(lblCountry);
								//Modificación para evitar retratamiento del 9 en AR y CL
								inputCountry.setText(arrayCodes[i]);
								preferences		= getApplicationContext().getSharedPreferences(Common.KEY_PREF, Context.MODE_PRIVATE);
								String phone	= preferences.getString(User.KEY_PHONE, "");

								if(StringUtils.isNotEmpty(phone))
								{
									editPhone.setText(phone.replace(arrayCodes[i], ""));
								}

								editor.putString(Land.KEY_API, arrayIsoCodes[i]);
								editor.apply();

								//Agregado para obtener dinámicamente la longitud a validar
								minLenght		= Integer.valueOf(arrayMinLength[i]);
								maxLenght		= Integer.valueOf(arrayMaxLength[i]);
								format			= arrayFormats[i];
								foundCountry	= true;
								break;
							}
						}
					}
				}

				//Agregado para completar el hint con el ejemplo del formato
				if(StringUtils.isNotEmpty(format))
				{
					inputPhone.setHint(getString(R.string.phone)+". "+getString(R.string.phone_example)+" "+format);
				}
			}
			else
			{
				btnCountry.setText(getString(R.string.country_hint));
				inputCountry.setText(Common.CODE_FORMAT);
				inputPhone.setHint(getString(R.string.phone));
			}
		}
		catch(Exception e)
		{
			System.out.println("PhoneActivity:selectCountry - Exception: " + e);

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
			View currentFocusView = getCurrentFocus();

			if(currentFocusView != null)
			{
				InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.RESULT_UNCHANGED_SHOWN);
			}
		}
		catch(Exception e)
		{
			System.out.println("PhoneActivity:hideSoftKeyboard - Exception: " + e);

			if(Common.DEBUG)
			{
				e.printStackTrace();
			}
		}
	}

	/**
	 * Agregado para realizar checkeos al reanudar la pantalla
	 */
	@Override
	public void onResume()
	{
		try
		{
			super.onResume();
			Window window			= getWindow();
			originalSoftInputMode	= window.getAttributes().softInputMode;
			window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

			//Se movío este código aquí para ejectuarse al efectuar back desde CodeActivity y al reingresar a esta activity
			SharedPreferences preferences	= getApplicationContext().getSharedPreferences(Common.KEY_PREF, Context.MODE_PRIVATE);

			//Preselección de Country en base a la api IP
			Realm realm			= Realm.getDefaultInstance();
			Isp isp				= realm.where(Isp.class).findFirst();
			String country		= "";
			String countryCode	= "";

			//Agregado por si no se pudo obtener el país desde api, probamos obteniendo desde el TelephonyManager
			if(isp != null)
			{
				if(StringUtils.isNotEmpty(isp.getCountry()))
				{
					country = isp.getCountry();
				}
				else
				{
					if(StringUtils.isNotEmpty(isp.getCountryNet()))
					{
						country = isp.getCountryNet();
					}
					else
					{
						if(StringUtils.isNotEmpty(isp.getCountrySim()))
						{
							country = isp.getCountrySim();
						}
						else
						{
							//Agregado para recargar el país si el usuario vuelve a esta pantalla
							if(StringUtils.isNotEmpty(preferences.getString(Land.KEY_API, "")))
							{
								country = preferences.getString(Land.KEY_API, "");
							}
						}
					}
				}

				//Agregado para mover el selector si hay diferencia de ubicación
				if(!isp.getCountryCode().equals(isp.getCountryNet()) && !isp.getCountryCode().equals(isp.getCountrySim()))
				{
					if(StringUtils.isNotEmpty(isp.getCountryNet()))
					{
						country		= realm.where(Land.class).equalTo(Land.KEY_ISOCODE, isp.getCountryNet()).findFirst().getName();
						countryCode	= isp.getCountryNet();
					}
					else
					{
						if(StringUtils.isNotEmpty(isp.getCountrySim()))
						{
							country		= realm.where(Land.class).equalTo(Land.KEY_ISOCODE, isp.getCountrySim()).findFirst().getName();
							countryCode	= isp.getCountrySim();
						}
						else
						{
							if(StringUtils.isNotEmpty(isp.getCountryCode()))
							{
								countryCode = isp.getCountryCode();
							}
						}
					}
				}
			}

			//Agregado para corregir falla en preselección de país
			if(StringUtils.isNotEmpty(country))
			{
				selectCountry(country, false);
			}
			else
			{
				Intent intent = getIntent();

				if(intent != null)
				{
					if(intent.hasExtra(Land.KEY_API))
					{
						if(StringUtils.isNotEmpty(intent.getStringExtra(Land.KEY_API)))
						{
							country = intent.getStringExtra(Land.KEY_API);
							selectCountry(country, false);
						}
					}
				}
			}

			if(!foundCountry)
			{
				selectCountry(countryCode, true);
			}

			if(StringUtils.isNotEmpty(preferences.getString(User.KEY_PHONE, "")) && StringUtils.isNotEmpty(inputCountry.getText().toString()) && StringUtils.isEmpty(editPhone.getText().toString()))
			{
				editPhone.setText(preferences.getString(User.KEY_PHONE, "").substring(inputCountry.getText().toString().length()));
			}

			//Agregado para medición de descargas por Facebook
			if(!Common.DEBUG)
			{
				FacebookSdk.sdkInitialize(getApplicationContext());
			}
		}
		catch(Exception e)
		{
			System.out.println("PhoneActivity:onResume - Exception: " + e);

			if(Common.DEBUG)
			{
				e.printStackTrace();
			}
		}
	}

	//Agregado para medición de descargas por Facebook
	protected void onPause()
	{
		super.onPause();
	}
}