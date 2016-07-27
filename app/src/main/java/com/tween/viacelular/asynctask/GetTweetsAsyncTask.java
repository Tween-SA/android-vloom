package com.tween.viacelular.asynctask;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;

import com.afollestad.materialdialogs.MaterialDialog;
import com.tween.viacelular.R;
import com.tween.viacelular.data.User;
import com.tween.viacelular.models.Land;
import com.tween.viacelular.models.Message;
import com.tween.viacelular.models.Migration;
import com.tween.viacelular.models.Suscription;
import com.tween.viacelular.services.ApiConnection;
import com.tween.viacelular.utils.Common;

import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import io.realm.Realm;

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
					.content(R.string.social)
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
			Realm realm						= Realm.getDefaultInstance();
			Suscription suscription			= realm.where(Suscription.class).equalTo(Suscription.KEY_API, companyId).findFirst();
			int notificationId				= preferences.getInt(Common.KEY_LAST_MSGID, 0);
			String dateText					= context.getString(R.string.social_date);
			System.out.println("response: "+result+" to work: "+jsonResult.toString());

			//Message message	= new Message(	"msgid", "type", msg, "channel", Message.STATUS_RECEIVE, "phone", "countryCode", Message.FLAGS_PUSH, System.currentTimeMillis(),
			//		Common.BOOL_NO, Message.KIND_TWITTER, link, image, submsg, "", "", companyId);

			if(result.equals(ApiConnection.OK))
			{
				if(!jsonResult.isNull(Common.KEY_CONTENT))
				{
					if(!jsonResult.getJSONObject(Common.KEY_CONTENT).isNull(Common.KEY_DATA))
					{
						String date		= jsonResult.getJSONObject(Common.KEY_CONTENT).getJSONObject(Common.KEY_DATA).getString("date");
						notificationId	= notificationId+1;
						Message message	= new Message();
						message.setMsgId(String.valueOf(notificationId));
						message.setMsg(jsonResult.getJSONObject(Common.KEY_CONTENT).getJSONObject(Common.KEY_DATA).getString("tweet"));
						message.setCompanyId(suscription.getCompanyId());
						message.setChannel(suscription.getName());
						message.setType(context.getString(R.string.social_high));
						message.setStatus(Message.STATUS_RECEIVE);
						message.setPhone(preferences.getString(User.KEY_PHONE, ""));
						message.setCountryCode(preferences.getString(Land.KEY_API, ""));
						message.setFlags(Message.FLAGS_PUSH);
						message.setCreated(System.currentTimeMillis());
						message.setDeleted(Common.BOOL_NO);
						message.setKind(Message.KIND_TWITTER);
						message.setLink(jsonResult.getJSONObject(Common.KEY_CONTENT).getJSONObject(Common.KEY_DATA).getString("background"));//Momentaneo hasta tener link-preview
						message.setSubMsg(jsonResult.getJSONObject(Common.KEY_CONTENT).getJSONObject(Common.KEY_DATA).getString("description"));
						message.setSocialId(jsonResult.getJSONObject(Common.KEY_CONTENT).getJSONObject(Common.KEY_DATA).getString("id"));
						message.setSocialName(jsonResult.getJSONObject(Common.KEY_CONTENT).getJSONObject(Common.KEY_DATA).getString("name"));
						message.setSocialAccount(jsonResult.getJSONObject(Common.KEY_CONTENT).getJSONObject(Common.KEY_DATA).getString("twitter"));
						message.setSocialShares(jsonResult.getJSONObject(Common.KEY_CONTENT).getJSONObject(Common.KEY_DATA).getInt("retweets"));
						message.setSocialLikes(jsonResult.getJSONObject(Common.KEY_CONTENT).getJSONObject(Common.KEY_DATA).getInt("favs"));
						SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
						SimpleDateFormat old = new SimpleDateFormat("EEE MMM dd HH:mm:ss ZZZZZ yyyy",Locale.ENGLISH);
						old.setLenient(true);

						Date date2 = null;
						try
						{
							date2 = old.parse(date);
						}
						catch(ParseException e)
						{
							System.out.println("GetTweetsAsyncTask:doInBackground:parseDate - Exception: " + e);

							if(Common.DEBUG)
							{
								e.printStackTrace();
							}
						}

						message.setSocialDate(dateText.replace("dd/mm/yyyy", sdf.format(date2)));
						realm.beginTransaction();
						realm.copyToRealmOrUpdate(message);
						realm.commitTransaction();
						SharedPreferences.Editor editor = preferences.edit();
						editor.putInt(Common.KEY_LAST_MSGID, notificationId);
						editor.apply();
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
			System.out.println("GetTweetsAsyncTask:doInBackground - Exception: " + e);

			if(Common.DEBUG)
			{
				e.printStackTrace();
			}
		}

		return result;
	}
}