package com.tween.viacelular.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.design.widget.TextInputLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import com.afollestad.materialdialogs.MaterialDialog;
import com.tween.viacelular.R;
import com.tween.viacelular.asynctask.CaptureSMSAsyncTask;
import com.tween.viacelular.asynctask.CheckCodeAsyncTask;
import com.tween.viacelular.asynctask.RegisterPhoneAsyncTask;
import com.tween.viacelular.models.Land;
import com.tween.viacelular.models.User;
import com.tween.viacelular.utils.Common;
import com.tween.viacelular.utils.StringUtils;
import com.tween.viacelular.utils.Utils;
import java.util.concurrent.TimeUnit;
import io.realm.Realm;

public class CodeActivity extends AppCompatActivity
{
	private EditText			editCode;
	private TextInputLayout		inputCode;
	private TextView			txtCount;
	private Button				btnRegister;
	private Button				btnFreePass;
	private SharedPreferences	preferences;
	private CountDownTimer		countDownTimer;
	private int					originalSoftInputMode;
	private boolean				firstRound				= false;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		try
		{
			super.onCreate(savedInstanceState);
			setContentView(R.layout.activity_code);
			preferences = getApplicationContext().getSharedPreferences(Common.KEY_PREF, Context.MODE_PRIVATE);

			if(Utils.checkSesion(this, Common.CODE_SCREEN))
			{
				Toolbar toolBar		= (Toolbar) findViewById(R.id.toolbarRegister);
				editCode			= (EditText) findViewById(R.id.editCode);
				inputCode			= (TextInputLayout) findViewById(R.id.inputCode);
				TextView txtRecive	= (TextView) findViewById(R.id.txtRecive);
				txtCount			= (TextView) findViewById(R.id.txtCount);
				btnRegister			= (Button) findViewById(R.id.btnRegister);
				btnFreePass			= (Button) findViewById(R.id.btnFreePass);
				setSupportActionBar(toolBar);
				toolBar.setNavigationIcon(R.drawable.back);
				btnFreePass.setVisibility(Button.GONE);

				toolBar.setNavigationOnClickListener(new View.OnClickListener()
				{
					@Override
					public void onClick(final View v)
					{
						logout();
					}
				});

				inputCode.setErrorEnabled(false);
				Utils.tintColorScreen(this, Common.COLOR_ACTION);
				txtRecive.setText(Html.fromHtml(txtRecive.getText().toString().replace("+000000000000", "<b>" + preferences.getString(User.KEY_PHONE, "") + "</b>")));
				String code = preferences.getString(Common.KEY_CODE, "");

				if(StringUtils.isNotEmpty(code))
				{
					if(StringUtils.isValidCode(code))
					{
						final CheckCodeAsyncTask task = new CheckCodeAsyncTask(CodeActivity.this, code, true);
						task.execute();
					}
				}
				else
				{
					if(!preferences.getBoolean(Common.KEY_PREF_CAPTURED, false))
					{
						final CaptureSMSAsyncTask task = new CaptureSMSAsyncTask(CodeActivity.this, false);
						task.execute();
					}
				}

				//Modificación para que el contador sea por 2 minutos, y que a los 3 minutos se aparezca el botón para pasar sin validar
				countDownTimer = new CountDownTimer(120000, 1000)
				{
					public void onTick(long millisUntilFinished)
					{
						String timer	=	getString(R.string.timer_count);
						String format	=	"" + String.format("%d:%02d", TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished),
											TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished)));
						timer			=	timer.replace("0:00", format);
						txtCount.setText(timer);
						//Agregado para mostrar botón para saltar la verificación de código
						if(firstRound && (TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished) == 0))
						{
							btnFreePass.setVisibility(Button.VISIBLE);
						}
					}

					public void onFinish()
					{
						txtCount.setText(getString(R.string.timer_count));
						enableRetry();
						firstRound = true;
					}
				}.start();

				//Agregado para habilitar el botón luego de terminar de escribir en el input
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

				SharedPreferences.Editor editor = preferences.edit();
				editor.putBoolean(Common.KEY_PREF_CALLME, true);
				editor.apply();
			}
		}
		catch(Exception e)
		{
			System.out.println("CodeActivity:onCreate - Exception: " + e);

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
			btnRegister.setEnabled(true);
			btnRegister.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.accent));
			btnRegister.setOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					login(v);
				}
			});
		}
		catch(Exception e)
		{
			System.out.println("CodeActivity:enableNextStep - Exception: " + e);

			if(Common.DEBUG)
			{
				e.printStackTrace();
			}
		}
	}

	public void enableRetry()
	{
		try
		{
			boolean callme = preferences.getBoolean(Common.KEY_PREF_CALLME, true);

			if(callme)
			{
				//Modificación para una vez completado el timer llamar directamente a la Api de llamada y quitar el cambio de botón
				if(preferences.getInt(Common.KEY_PREF_CALLME_TIMES, 0) < 2)
				{
					RegisterPhoneAsyncTask task = new RegisterPhoneAsyncTask(CodeActivity.this, preferences.getString(User.KEY_PHONE, ""), false);
					task.execute();
					countDownTimer.start();
				}
			}
		}
		catch(Exception e)
		{
			System.out.println("CodeActivity:enableRetry - Exception: " + e);

			if(Common.DEBUG)
			{
				e.printStackTrace();
			}
		}
	}

	/**
	 * Agregado para permitir el ingreso del usuario sin validar
	 * @param view
	 */
	public void getFreePass(View view)
	{
		try
		{
			MaterialDialog progress	= new MaterialDialog.Builder(this)
					.title(R.string.progress_dialog)
					.cancelable(false)
					.content(R.string.please_wait)
					.progress(true, 0)
					.show();

			//Modificación para contemplar migración a Realm
			Realm realm	= Realm.getDefaultInstance();
			User user	= realm.where(User.class).findFirst();

			if(user != null)
			{
				realm.beginTransaction();

				if(user.getPhone().replace("+", "").equals("5492616333888"))
				{
					user.setUserId(User.USERID);
				}

				user.setStatus(User.STATUS_INACTIVE);
				realm.commitTransaction();
				SharedPreferences.Editor editor	= preferences.edit();
				editor.putBoolean(Common.KEY_PREF_CALLME, false);
				editor.putBoolean(Common.KEY_PREF_LOGGED, true);
				editor.putBoolean(Common.KEY_PREF_CHECKED, true);
				editor.putBoolean(Common.KEY_PREF_FREEPASS, true);
				editor.apply();
			}

			if(progress.isShowing())
			{
				progress.cancel();
			}

			Intent intent = new Intent(this, HomeActivity.class);
			intent.putExtra(Common.KEY_REFRESH, false);
			startActivity(intent);
			finish();
		}
		catch(Exception e)
		{
			System.out.println("CodeActivity:getFreePass - Exception: " + e);

			if(Common.DEBUG)
			{
				e.printStackTrace();
			}
		}
	}

	public void login(View view)
	{
		try
		{
			boolean result = true;
			if(StringUtils.isEmpty(editCode.getText().toString().trim()))
			{
				inputCode.setErrorEnabled(true);
				inputCode.setError(getString(R.string.code_blank));
				result = false;
			}
			else
			{
				inputCode.setErrorEnabled(false);
			}

			if(!StringUtils.isValidCode(editCode.getText().toString().trim()))
			{
				inputCode.setErrorEnabled(true);
				inputCode.setError(getString(R.string.code_wrong));
				result = false;
			}
			else
			{
				inputCode.setErrorEnabled(false);
			}

			if(result)
			{
				//Suspender autollamado luego de ingresar el código
				SharedPreferences.Editor editor = preferences.edit();
				editor.putBoolean(Common.KEY_PREF_CALLME, false);
				editor.apply();
				//TODO Agregar transitions o delay para indicar actividad luego de oprimir el botón hasta la redirección al home
				final CheckCodeAsyncTask task = new CheckCodeAsyncTask(CodeActivity.this, editCode.getText().toString().trim(), true);
				task.execute();
			}
		}
		catch(Exception e)
		{
			System.out.println("CodeActivity:login - Exception: " + e);

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
			hideSoftKeyboard();
			logout();
		}
		catch(Exception e)
		{
			System.out.println("CodeActivity:onBackPressed - Exception: " + e);

			if(Common.DEBUG)
			{
				e.printStackTrace();
			}
		}
	}

	public void logout()
	{
		try
		{
			SharedPreferences.Editor editor	= preferences.edit();
			editor.putBoolean(Common.KEY_PREF_LOGGED, false);
			editor.putBoolean(Common.KEY_PREF_CHECKED, false);
			//Agregado para cortar llamadas si oprimio back
			editor.putBoolean(Common.KEY_PREF_CALLME, false);
			editor.apply();
			Intent intent					= new Intent(getApplicationContext(), PhoneActivity.class);
			//Agregado para reiniciar el selector de país
			intent.putExtra(Land.KEY_API, preferences.getString(Land.KEY_API, ""));
			startActivity(intent);
			finish();
		}
		catch(Exception e)
		{
			System.out.println("CodeActivity:logout - Exception: " + e);

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
			System.out.println("CodeActivity:hideSoftKeyboard - Exception: " + e);

			if(Common.DEBUG)
			{
				e.printStackTrace();
			}
		}
	}

	/**
	 * Agregado para controlar estado del teclado
	 */
	@Override
	public void onResume()
	{
		super.onResume();
		try
		{
			Window window = getWindow();

			// Overriding the soft input mode of the Window so that the Send and Cancel buttons appear above the soft keyboard when either EditText field gains focus. We cache the mode in order to set it
			// back to the original value when the Fragment is paused.
			originalSoftInputMode = window.getAttributes().softInputMode;
			window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
		}
		catch(Exception e)
		{
			System.out.println("CodeActivity:onResume - Exception: " + e);

			if(Common.DEBUG)
			{
				e.printStackTrace();
			}
		}
	}
}