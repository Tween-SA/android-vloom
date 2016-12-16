package com.tween.viacelular.asynctask;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.view.View;
import com.afollestad.materialdialogs.MaterialDialog;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.tween.viacelular.R;
import com.tween.viacelular.models.Suscription;
import com.tween.viacelular.utils.Common;
import com.tween.viacelular.utils.StringUtils;

public class LogoAsyncTask extends AsyncTask<Void, Void, Bitmap>
{
	private MaterialDialog	progress;
	private Context			context;
	private boolean			displayDialog	= false;
	private String			urlLogo			= "";
	private double			density			= Common.DENSITY_XXHDPI;
	private Bitmap			result			= null;

	public LogoAsyncTask(Context context, boolean displayDialog, String urlLogo, double density)
	{
		this.context		= context;
		this.displayDialog	= displayDialog;
		this.urlLogo		= urlLogo;
		this.density		= density;
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
			System.out.println("LogoAsyncTask:onPreExecute - Exception: " + e);
			if(Common.DEBUG)
			{
				e.printStackTrace();
			}
		}
	}

	@Override
	protected Bitmap doInBackground(Void... params)
	{
		try
		{
			//Agregado para prevenir error al levantar instancia cuando la app está cerrada
			Common.DEFAULT_OPTIONS			= new DisplayImageOptions.Builder().cacheInMemory(true).cacheOnDisk(true).build();
			ImageLoaderConfiguration config	= new ImageLoaderConfiguration.Builder(context).defaultDisplayImageOptions(Common.DEFAULT_OPTIONS).build();
			ImageLoader imageLoader			= null;

			//Modificación para implementar cache
			if(ImageLoader.getInstance() != null)
			{
				ImageLoader.getInstance().init(config);
				imageLoader = ImageLoader.getInstance();
			}

			if(imageLoader != null)
			{
				if(StringUtils.isEmpty(urlLogo))
				{
					urlLogo = Suscription.ICON_APP;
				}

				//Agregado para evitar errores por codificación del @
				if(density != -1)
				{
					if(density == Common.DENSITY_XHDPI)
					{
						urlLogo = urlLogo.replace("@3x.png", "@2x.png").replace("%403x.png", "%402x.png");
					}
					else
					{
						if(density == Common.DENSITY_HDPI)
						{
							urlLogo = urlLogo.replace("@3x.png", "@1,5x.png").replace("%403x.png", "%401,5x.png");
						}
						else
						{
							if(density <= Common.DENSITY_HDPI)
							{
								urlLogo = urlLogo.replace("@3x.png", "@1x.png").replace("%403x.png", "%401x.png");
							}
						}
					}

					urlLogo = urlLogo.replace("@", "%40");
				}

				if(Common.DEBUG)
				{
					System.out.println("Image to down profile: "+urlLogo);
				}

				//Test
				imageLoader.loadImage(urlLogo, new SimpleImageLoadingListener()
				{
					@Override
					public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage)
					{
						System.out.println("onLoadingComplete: "+urlLogo);
						result = loadedImage;
					}
				});
			}
			else
			{
				result = BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher);
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
			System.out.println("LogoAsyncTask:doInBackground - Exception: " + e);

			if(Common.DEBUG)
			{
				e.printStackTrace();
			}
		}

		return result;
	}
}
