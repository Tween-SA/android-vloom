package com.tween.viacelular.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;
import com.appboy.Appboy;
import com.tween.viacelular.R;
import com.tween.viacelular.adapters.RecyclerAdapter;
import com.tween.viacelular.adapters.RecyclerItemClickListener;
import com.tween.viacelular.models.User;
import com.tween.viacelular.utils.Common;
import com.tween.viacelular.utils.StringUtils;
import com.tween.viacelular.utils.Utils;
import io.realm.Realm;

/**
 * Created by davidfigueroa on 11/11/15.
 * Integra módulo Feedback de Appboy
 */
public class FeedbackActivity extends AppCompatActivity
{
	private Button			btnSend;
	private CheckBox		chkIssue;
	private EditText		etMessage;
	private String			etEmail;
	private TextInputLayout	inputMessage;
	private int				originalSoftInputMode;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		try
		{
			super.onCreate(savedInstanceState);
			setContentView(R.layout.activity_feedback);

			if(Utils.checkSesion(this, Common.ANOTHER_SCREEN))
			{
				final Intent intentRecive		= getIntent();
				setTitle(intentRecive.getStringExtra(Common.KEY_TITLE));
				Toolbar toolBar					= (Toolbar) findViewById(R.id.toolBar);
				btnSend							= (Button) findViewById(R.id.btnSend);
				chkIssue						= (CheckBox) findViewById(R.id.chkIssue);
				etMessage						= (EditText) findViewById(R.id.etMessage);
				inputMessage					= (TextInputLayout) findViewById(R.id.inputMessage);
				setSupportActionBar(toolBar);
				RecyclerView mRecyclerView		= (RecyclerView) findViewById(R.id.RecyclerView);
				mRecyclerView.setHasFixedSize(true);
				inputMessage.setErrorEnabled(false);
				RecyclerView.Adapter mAdapter	= null;
				Realm realm						= Realm.getDefaultInstance();
				User user						= realm.where(User.class).findFirst();

				if(user != null)
				{
					if(StringUtils.isNotEmpty(user.getEmail()))
					{
						etEmail = user.getEmail();
					}
				}

				mAdapter = new RecyclerAdapter(	Utils.getMenu(getApplicationContext()), intentRecive.getIntExtra(Common.KEY_SECTION, RecyclerAdapter.FEEDBACK_SELECTED),
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
						hideSoftKeyboard();
					}

					@Override
					public void onDrawerClosed(View drawerView)
					{
						super.onDrawerClosed(drawerView);
					}
				};

				mDrawerToggle.syncState();
				final Context context = getApplicationContext();
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

				Utils.tintColorScreen(this, Common.COLOR_ACTION);

				//Agregado para habilitar el botón luego de terminar de escribir en el input
				etMessage.addTextChangedListener(new TextWatcher()
				{
					@Override
					public void beforeTextChanged(CharSequence s, int start, int count, int after)
					{
						inputMessage.setErrorEnabled(false);
						enableSend();
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
			System.out.println("FeedbackActivity:onCreate - Exception: " + e);

			if(Common.DEBUG)
			{
				e.printStackTrace();
			}
		}
	}

	public void enableSend()
	{
		try
		{
			btnSend.setEnabled(true);
			btnSend.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.accent));
			btnSend.setOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					send(v);
				}
			});
		}
		catch(Exception e)
		{
			System.out.println("FeedbackActivity:enableSend - Exception: " + e);

			if(Common.DEBUG)
			{
				e.printStackTrace();
			}
		}
	}

	public void send(View view)
	{
		try
		{
			boolean result = true;

			if(StringUtils.isEmpty(etMessage.getText().toString().trim()))
			{
				inputMessage.setErrorEnabled(true);
				inputMessage.setError(getString(R.string.feedback_error));
				result = false;
			}
			else
			{
				inputMessage.setErrorEnabled(false);
			}

			if(result)
			{
				hideSoftKeyboard();
				boolean isIssue	= chkIssue.isChecked();
				String message	= etMessage.getText().toString();

				if(Common.DEBUG)
				{
					System.out.println("FeedbackActivity:isIssue: " + isIssue);
					System.out.println("FeedbackActivity:message: " + etMessage.getText().toString());
					System.out.println("FeedbackActivity:email: " + etEmail);
				}

				result = Appboy.getInstance(this).submitFeedback(etEmail, message, isIssue);

				if(result)
				{
					Toast.makeText(FeedbackActivity.this, getString(R.string.feedback_ok), Toast.LENGTH_SHORT).show();
					Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
					intent.putExtra(Common.KEY_REFRESH, false);
					startActivity(intent);
					finish();
				}
				else
				{
					Toast.makeText(FeedbackActivity.this, getString(R.string.response_invalid), Toast.LENGTH_SHORT).show();
				}
			}
		}
		catch(Exception e)
		{
			System.out.println("FeedbackActivity:send - Exception: " + e);

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
			System.out.println("FeedbackActivity:hideSoftKeyboard - Exception: " + e);

			if(Common.DEBUG)
			{
				e.printStackTrace();
			}
		}
	}

	@Override
	public void onResume()
	{
		super.onResume();

		try
		{
			Appboy.getInstance(this).logFeedbackDisplayed();
			Window window			= getWindow();
			originalSoftInputMode	= window.getAttributes().softInputMode;
			window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
			Appboy.getInstance(this).logFeedbackDisplayed();
		}
		catch(Exception e)
		{
			System.out.println("FeedbackActivity:onResume - Exception: " + e);

			if(Common.DEBUG)
			{
				e.printStackTrace();
			}
		}
	}

	@Override
	public void onStart()
	{
		super.onStart();

		try
		{
			if(!Common.DEBUG)
			{
				Appboy.getInstance(FeedbackActivity.this).openSession(FeedbackActivity.this);
			}
		}
		catch(Exception e)
		{
			System.out.println("FeedbackActivity:onStart - Exception: " + e);

			if(Common.DEBUG)
			{
				e.printStackTrace();
			}
		}
	}

	@Override
	public void onStop()
	{
		super.onStop();

		try
		{
			if(!Common.DEBUG)
			{
				Appboy.getInstance(FeedbackActivity.this).closeSession(FeedbackActivity.this);
			}
		}
		catch(Exception e)
		{
			System.out.println("FeedbackActivity:onStop - Exception: " + e);

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
			hideSoftKeyboard();
			Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
			intent.putExtra(Common.KEY_REFRESH, false);
			startActivity(intent);
			finish();
		}
		catch(Exception e)
		{
			System.out.println("FeedbackActivity:onBackPressed - Exception: " + e);

			if(Common.DEBUG)
			{
				e.printStackTrace();
			}
		}
	}
}