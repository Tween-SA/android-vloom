package com.tween.viacelular.asynctask;

import android.Manifest;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.support.v4.content.ContextCompat;
import com.afollestad.materialdialogs.MaterialDialog;
import com.tween.viacelular.R;
import com.tween.viacelular.models.ConnectedAccount;
import com.tween.viacelular.models.User;
import com.tween.viacelular.utils.Common;
import com.tween.viacelular.utils.StringUtils;
import io.realm.Realm;
import io.realm.RealmResults;

/**
 * Created by david.figueroa on 17/6/15.
 */
public class ReadAccountsAsyncTask extends AsyncTask<Void, Void, Boolean>
{
	private MaterialDialog	progress;
	private Activity		activity;
	private boolean			displayDialog	= false;

	public ReadAccountsAsyncTask(Activity activity, boolean displayDialog)
	{
		this.activity		= activity;
		this.displayDialog	= displayDialog;
	}

	protected void onPreExecute()
	{
		try
		{
			if(displayDialog)
			{
				if(progress != null)
				{
					if(progress.isShowing())
					{
						progress.cancel();
					}
				}

				progress = new MaterialDialog.Builder(activity)
					.title(R.string.progress_dialog)
					.cancelable(false)
					.content(R.string.please_wait)
					.progress(true, 0)
					.show();
			}
		}
		catch(Exception e)
		{
			System.out.println("ReadAccountsAsyncTask - Exception: " + e);

			if(Common.DEBUG)
			{
				e.printStackTrace();
			}
		}
	}

	@Override
	protected Boolean doInBackground(Void... params)
	{
		boolean result = false;

		try
		{
			if(ContextCompat.checkSelfPermission(activity, Manifest.permission.GET_ACCOUNTS) == PackageManager.PERMISSION_GRANTED)
			{
				//Modificaciones para contemplar migración a Realm
				Realm realm						= Realm.getDefaultInstance();
				AccountManager accountManager	= AccountManager.get(activity);
				Account[] accounts				= accountManager.getAccounts();

				if(accounts.length > 0)
				{
					SharedPreferences preferences			= activity.getSharedPreferences(Common.KEY_PREF, Context.MODE_PRIVATE);
					RealmResults<ConnectedAccount> results	= realm.where(ConnectedAccount.class).findAll();
					realm.beginTransaction();
					results.deleteAllFromRealm();
					realm.commitTransaction();


					for(Account account : accounts)
					{
						realm.beginTransaction();
						realm.copyToRealmOrUpdate(new ConnectedAccount(account.name, account.type));
						realm.commitTransaction();

						if(account.type.equals(ConnectedAccount.TYPE_GOOGLE) && StringUtils.isEmpty(preferences.getString(User.KEY_EMAIL, "")))
						{
							SharedPreferences.Editor editor = preferences.edit();
							editor.putString(User.KEY_EMAIL, account.name);
							editor.apply();
						}
					}
				}
			}

			if(displayDialog)
			{
				if(progress != null)
				{
					if(progress.isShowing())
					{
						progress.cancel();
					}
				}
			}
		}
		catch(Exception e)
		{
			System.out.println("ReadAccountsAsyncTask - Exception: " + e);

			if(Common.DEBUG)
			{
				e.printStackTrace();
			}
		}

		return result;
	}
}