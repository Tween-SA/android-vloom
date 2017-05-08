package com.tween.viacelular.asynctask;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import com.afollestad.materialdialogs.MaterialDialog;
import com.tween.viacelular.R;
import com.tween.viacelular.data.Message;
import com.tween.viacelular.utils.ApiConnection;
import com.tween.viacelular.utils.Common;
import com.tween.viacelular.utils.StringUtils;
import com.tween.viacelular.utils.Utils;

/**
 * Manejador para reportar mensajes capturados en el dispositivo para backup
 * Created by Tween (David Figueroa davo.figueroa@tween.com.ar)
 */
public class CertificateAsyncTask extends AsyncTask<Void, Void, String>
{
	private MaterialDialog	progress;
	private Context			context;
	private boolean			displayDialog	= true;
	private String			id				= "";
	
	public CertificateAsyncTask(final Context context, final boolean displayDialog, final String id)
	{
		this.context		= context;
		this.displayDialog	= displayDialog;
		this.id				= id;
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

				progress = new MaterialDialog.Builder(context)
							.title(R.string.progress_dialog)
							.cancelable(false)
							.content(R.string.please_wait)
							.progress(true, 0)
							.show();
			}
		}
		catch(Exception e)
		{
			Utils.logError(context, "CertificateAsyncTask:onPreExecute - Exception:", e);
		}
	}

	@Override
	protected String doInBackground(Void... params)
	{
		try
		{
			if(StringUtils.isIdMongo(id))
			{
				SharedPreferences preferences	= context.getSharedPreferences(Common.KEY_PREF, Context.MODE_PRIVATE);
				ApiConnection.request(ApiConnection.CERTIFICATE_MESSAGES.replace(Message.KEY_API, id), context, ApiConnection.METHOD_PUT, preferences.getString(Common.KEY_TOKEN, ""), "");
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
			Utils.logError(context, "CertificateAsyncTask:doInBackground - Exception:", e);
		}

		return "";
	}
}