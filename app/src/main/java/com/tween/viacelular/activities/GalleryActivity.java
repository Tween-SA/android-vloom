package com.tween.viacelular.activities;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.synnapps.carouselview.CarouselView;
import com.synnapps.carouselview.ImageListener;
import com.tween.viacelular.R;
import com.tween.viacelular.models.Message;
import com.tween.viacelular.utils.Common;
import com.tween.viacelular.utils.StringUtils;
import io.realm.Realm;

/**
 * Created by davidfigueroa on 12/12/16.
 */
public class GalleryActivity extends AppCompatActivity
{
	CarouselView carouselView;
	private String msgId = "";

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_gallery);
		carouselView = (CarouselView) findViewById(R.id.carouselView);

		if(getIntent() != null)
		{
			msgId = getIntent().getStringExtra(Common.KEY_ID);
		}

		if(StringUtils.isNotEmpty(msgId))
		{
			Realm realm				= Realm.getDefaultInstance();
			final Message message	= realm.where(Message.class).equalTo(Message.KEY_API, msgId).findFirst();
			int pages				= 0;

			if(message != null)
			{
				if(StringUtils.isNotEmpty(message.getUri()) && StringUtils.isNotEmpty(message.getUriTwo()) && StringUtils.isNotEmpty(message.getUriThree()))
				{
					pages = 3;
				}
				else
				{
					if(StringUtils.isNotEmpty(message.getUri()) && StringUtils.isNotEmpty(message.getUriTwo()))
					{
						pages = 2;
					}
					else
					{
						if(StringUtils.isNotEmpty(message.getUri()))
						{
							pages = 1;
						}
					}
				}

				carouselView.setPageCount(pages);
				carouselView.setSlideInterval(1000000000);
				carouselView.stopCarousel();
				final Activity activity = this;

				if(pages > 0)
				{
					carouselView.setImageListener(new ImageListener()
					{
						@Override
						public void setImageForPosition(int position, final ImageView imageView)
						{
							switch(position)
							{
								case 0:
									Picasso.with(activity).load(message.getUri()).placeholder(R.drawable.splash).into(imageView, new Callback()
									{
										@Override
										public void onSuccess()
										{
										}

										@Override
										public void onError()
										{
											Picasso.with(activity).load(message.getAttached()).placeholder(R.drawable.splash).into(imageView);
										}
									});
								break;

								case 1:
									Picasso.with(activity).load(message.getUriTwo()).placeholder(R.drawable.splash).into(imageView, new Callback()
									{
										@Override
										public void onSuccess()
										{
										}

										@Override
										public void onError()
										{
											Picasso.with(activity).load(message.getAttachedTwo()).placeholder(R.drawable.splash).into(imageView);
										}
									});
								break;

								case 2:
									Picasso.with(activity).load(message.getUriThree()).placeholder(R.drawable.splash).into(imageView, new Callback()
									{
										@Override
										public void onSuccess()
										{
										}

										@Override
										public void onError()
										{
											Picasso.with(activity).load(message.getAttachedThree()).placeholder(R.drawable.splash).into(imageView);
										}
									});
								break;

								default:
									Picasso.with(activity).load(R.drawable.splash).placeholder(R.drawable.splash).into(imageView);
								break;
							}
						}
					});
				}
			}
		}
	}
}