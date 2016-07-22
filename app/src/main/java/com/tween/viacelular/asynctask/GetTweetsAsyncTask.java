package com.tween.viacelular.asynctask;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;

import com.afollestad.materialdialogs.MaterialDialog;
import com.tween.viacelular.R;
import com.tween.viacelular.services.ApiConnection;
import com.tween.viacelular.models.Migration;
import com.tween.viacelular.models.Suscription;
import com.tween.viacelular.utils.Common;

import org.json.JSONObject;

/**
 * Created by davidfigueroa on 15/6/16.
 */
public class GetTweetsAsyncTask extends AsyncTask<Void, Void, String>
{
	private MaterialDialog	progress;
	private Context			context;
	private boolean			displayDialog	= false;
	private String			companyId		= "";

	public GetTweetsAsyncTask(final Context context, final boolean displayDialog, final String companyId)
	{
		this.context		= context;
		this.displayDialog	= displayDialog;
		this.companyId		= companyId;
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
					.title(R.string.landing_card_loading_header)
					.cancelable(false)
					.content(R.string.please_wait)//Podría ser "Estamos recibiendo nuevo contenido para ti"
					.progress(true, 0)
					.show();
			}

			Migration.getDB(context);
		}
		catch(Exception e)
		{
			System.out.println("GetTweetsAsyncTask:onPreExecute - Exception: " + e);

			if(Common.DEBUG)
			{
				e.printStackTrace();
			}
		}
	}

	@Override
	protected String doInBackground(Void... params)
	{
		String result = "";

		try
		{
			SharedPreferences preferences	= context.getSharedPreferences(Common.KEY_PREF, Context.MODE_PRIVATE);
			String url						= ApiConnection.COMPANIES_SOCIAL.replace(Suscription.KEY_API, companyId);
			JSONObject jsonResult			= new JSONObject(ApiConnection.request(url, context, ApiConnection.METHOD_GET, preferences.getString(Common.KEY_TOKEN, ""), ""));
			result							= ApiConnection.checkResponse(context, jsonResult);

			if(result.equals(ApiConnection.OK))
			{
				//IspHelper.parseJSON(jsonResult.getJSONObject(Common.KEY_DATA), context, update);
				/*Usar key_content para sacar la data original: {
  "success": true,
  "data": {
    "id": "755420575728885800",
    "tweet": "Contanos qué tema dedicarías y a quién. Nosotros lo reproduciremos mañana en https://t.co/sKDySpMT0N #DíadelAmigo 🎶📻 https://t.co/gFmExsdlJB",
    "retweets": 7,
    "favs": 9,
    "date": "Tue Jul 19 15:14:34 +0000 2016",
    "name": "Tarjeta Nevada",
    "twitter": "@Tarjeta_Nevada",
    "description": "Cuenta Oficial de Tarjeta Nevada. Nos encontrarás aquí de 09 a 21 hs. y en el 0810 333 9496 las 24 hs.",
    "followers": 14403,
    "background": "http://pbs.twimg.com/profile_background_images/755230285/a71175d7686e6efc6f8b42a47a0b119b.jpeg",
    "image": "http://pbs.twimg.com/profile_images/753188556949053447/b94WNtsb_normal.jpg"
  }
}*/
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
			System.out.println("GetTweetsAsyncTask:doInBackground - Exception: " + e);

			if(Common.DEBUG)
			{
				e.printStackTrace();
			}
		}

		return result;
	}
}