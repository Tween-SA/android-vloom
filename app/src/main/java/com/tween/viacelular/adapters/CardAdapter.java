package com.tween.viacelular.adapters;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.kuassivi.view.ProgressProfileView;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.tween.viacelular.R;
import com.tween.viacelular.activities.CardViewActivity;
import com.tween.viacelular.activities.GalleryActivity;
import com.tween.viacelular.models.Message;
import com.tween.viacelular.models.Suscription;
import com.tween.viacelular.models.SuscriptionHelper;
import com.tween.viacelular.utils.Common;
import com.tween.viacelular.utils.DateUtils;
import com.tween.viacelular.utils.StringUtils;
import com.tween.viacelular.utils.Utils;
import de.hdodenhof.circleimageview.CircleImageView;
import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;

/**
 * Created by david.figueroa on 8/7/15.
 */
public class CardAdapter extends RecyclerView.Adapter<CardAdapter.ViewHolder>
{
	private RealmResults<Message>	notificationList;
	private CardViewActivity		activity;
	private Suscription				suscription; //Modificación para recibir la entidad completa
	public final static int			OPTION_SHARE	= 0;
	public final static int			OPTION_BLOCK	= 1;
	public final static int			OPTION_DELETE	= 2;
	public final static int			OPTION_DISMISS	= 3;

	public static class ViewHolder extends RecyclerView.ViewHolder
	{
		public int					HolderId;
		public ImageView			iconPrice, iconSMS, ibOptions, iconDown, ivPicture, iconSocial, ivChain, iconComent, iconAttach, ivEdit;
		public TextView				rowTime, txtTitle, txtContent, socialAccount, socialDate, txtComment, txtReceipt;
		private View				dividerTitle;
		private Button				btnDownload, btnView, btnShare;
		private RatingBar			ratingBar;
		private RelativeLayout		rlComment;
		private CircleImageView		imgOne, imgTwo, imgThree;
		private ProgressProfileView	animOne, animTwo, animThree;
		private CardView			cardReceipt;
		private CircleImageView		circleReceipt;
		private FrameLayout			line;

		public ViewHolder(View itemView, int viewType)
		{
			super(itemView);
			HolderId	= viewType;
			rowTime		= (TextView) itemView.findViewById(R.id.rowTime);
			txtTitle	= (TextView) itemView.findViewById(R.id.txtTitle);
			txtContent	= (TextView) itemView.findViewById(R.id.txtContent);
			line		= (FrameLayout) itemView.findViewById(R.id.line);
			txtContent.setMovementMethod(LinkMovementMethod.getInstance());

			//Procesar controles existentes para cada tipo de mensaje
			switch(viewType)
			{
				case Message.KIND_IMAGE:
				case Message.KIND_VIDEO:
					ivPicture		= (ImageView) itemView.findViewById(R.id.ivPicture);
					dividerTitle	= itemView.findViewById(R.id.dividerTitle);
					btnView			= (Button) itemView.findViewById(R.id.btnView);
					btnShare		= (Button) itemView.findViewById(R.id.btnShare);
				break;

				case Message.KIND_INVOICE:
					cardReceipt		= (CardView) itemView.findViewById(R.id.cardReceipt);
					circleReceipt	= (CircleImageView) itemView.findViewById(R.id.circleReceipt);
					txtReceipt		= (TextView) itemView.findViewById(R.id.txtReceipt);
				break;

				case Message.KIND_FILE_DOWNLOADABLE:
				case Message.KIND_AUDIO:
					btnDownload	= (Button) itemView.findViewById(R.id.btnDownload);
					iconDown	= (ImageView) itemView.findViewById(R.id.iconDown);
				break;

				case Message.KIND_RATING:
					ratingBar = (RatingBar) itemView.findViewById(R.id.ratingBar);
				break;

				case Message.KIND_FACEBOOK_IMAGE:
				case Message.KIND_TWITTER_IMAGE:
					ibOptions		= (ImageView) itemView.findViewById(R.id.ibOptions);
					iconSocial		= (ImageView) itemView.findViewById(R.id.iconSocial);
					socialAccount	= (TextView) itemView.findViewById(R.id.socialAccount);
					socialDate		= (TextView) itemView.findViewById(R.id.socialDate);
					ivPicture		= (ImageView) itemView.findViewById(R.id.ivPicture);
				break;

				case Message.KIND_FACEBOOK:
				case Message.KIND_TWITTER:
					ibOptions		= (ImageView) itemView.findViewById(R.id.ibOptions);
					iconSocial		= (ImageView) itemView.findViewById(R.id.iconSocial);
					socialAccount	= (TextView) itemView.findViewById(R.id.socialAccount);
					socialDate		= (TextView) itemView.findViewById(R.id.socialDate);
				break;

				default:
					iconPrice	= (ImageView) itemView.findViewById(R.id.iconPrice);
					iconSMS		= (ImageView) itemView.findViewById(R.id.iconSMS);
					ibOptions	= (ImageView) itemView.findViewById(R.id.ibOptions);
					ivChain		= (ImageView) itemView.findViewById(R.id.ivChain);
					iconComent	= (ImageView) itemView.findViewById(R.id.iconComent);
					iconAttach	= (ImageView) itemView.findViewById(R.id.iconAttach);
					rlComment	= (RelativeLayout) itemView.findViewById(R.id.rlComment);
					txtComment	= (TextView) itemView.findViewById(R.id.txtComment);
					ivEdit		= (ImageView) itemView.findViewById(R.id.ivEdit);
					imgOne		= (CircleImageView) itemView.findViewById(R.id.imgOne);
					imgTwo		= (CircleImageView) itemView.findViewById(R.id.imgTwo);
					imgThree	= (CircleImageView) itemView.findViewById(R.id.imgThree);
					animOne		= (ProgressProfileView) itemView.findViewById(R.id.animOne);
					animTwo		= (ProgressProfileView) itemView.findViewById(R.id.animTwo);
					animThree	= (ProgressProfileView) itemView.findViewById(R.id.animThree);
					cardReceipt	= (CardView) itemView.findViewById(R.id.cardView);
				break;
			}
		}
	}

	public CardAdapter(CardViewActivity activity, String companyId)
	{
		Realm realm = Realm.getDefaultInstance();
		this.notificationList	= realm.where(Message.class).notEqualTo(Message.KEY_DELETED, Common.BOOL_YES).lessThan(Common.KEY_STATUS, Message.STATUS_SPAM)
									.equalTo(Suscription.KEY_API, companyId).findAllSorted(Message.KEY_CREATED, Sort.DESCENDING);
		this.activity			= activity;
		this.suscription		= realm.where(Suscription.class).equalTo(Suscription.KEY_API, companyId).findFirst();
	}

	@Override
	public CardAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
	{
		//Agregado para capturar excepciones
		try
		{
			View view;

			switch(viewType)
			{
				case Message.KIND_IMAGE:
					view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_card_image, parent, false);
				break;

				case Message.KIND_INVOICE:
					view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_card_receipt, parent, false);
				break;

				case Message.KIND_FILE_DOWNLOADABLE:
					view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_card_download, parent, false);
				break;

				case Message.KIND_LINKWEB:
					view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_card, parent, false);
				break;

				case Message.KIND_LINKMAP:
					view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_card, parent, false);
				break;

				case Message.KIND_VIDEO:
					view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_card_image, parent, false);
				break;

				case Message.KIND_RATING:
					view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_card_rating, parent, false);
				break;

				case Message.KIND_AUDIO:
					view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_card_download, parent, false);
				break;

				case Message.KIND_TWITTER:
				case Message.KIND_FACEBOOK:
					view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_card_social, parent, false);
				break;

				case Message.KIND_TWITTER_IMAGE:
				case Message.KIND_FACEBOOK_IMAGE:
					view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_card_social_image, parent, false);
				break;

				case Message.KIND_NOTE:
					view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_note, parent, false);
				break;

				default:
					view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_card, parent, false);
				break;
			}

			return new ViewHolder(view, viewType);
		}
		catch(Exception e)
		{
			Utils.logError(activity, "CardAdapter:onCreateViewHolder - Exception:", e);
		}

		return null;
	}

	@Override
	public void onBindViewHolder(final ViewHolder holder, final int position)
	{
		//Agregado para capturar excepciones
		try
		{
			if(notificationList.size() > 0)
			{
				final Message item = notificationList.get(position);

				if(item != null)
				{
					final String id = item.getMsgId();

					if(holder.line != null)
					{
						if(position == 0)
						{
							holder.line.setVisibility(FrameLayout.VISIBLE);
						}
						else
						{
							holder.line.setVisibility(FrameLayout.GONE);
						}
					}

					//Agregado para prevenir companies sin color
					String color = Common.COLOR_ACTION;

					if(StringUtils.isNotEmpty(suscription.getColorHex()))
					{
						color = suscription.getColorHex();
					}

					//Agregado para detectar si el color es claro
					if(Utils.isLightColor(color, activity))
					{
						holder.txtTitle.setTextColor(Color.BLACK);
					}
					else
					{
						//Agregado para evitar excepciones de tipo unknown color
						try
						{
							holder.txtTitle.setTextColor(Color.parseColor(suscription.getColorHex()));
						}
						catch(Exception e)
						{
							holder.txtTitle.setTextColor(Color.parseColor(Common.COLOR_ACTION));
						}
					}

					if(item.getMsg().equals(activity.getString(R.string.no_messages_text)))
					{
						holder.rowTime.setVisibility(TextView.GONE);
						holder.txtTitle.setText(item.getType());
						holder.txtContent.setText(item.getMsg());

						if(holder.ibOptions != null)
						{
							holder.ibOptions.setVisibility(ImageButton.GONE);
						}
					}
					else
					{
						//Modificación para determinar si el mensaje es pago o no desde el número corto
						String time = DateUtils.getTimeFromTs(item.getCreated(), activity.getApplicationContext());

						if(StringUtils.isNotEmpty(time))
						{
							holder.rowTime.setText(time);
						}
						else
						{
							holder.rowTime.setVisibility(TextView.GONE);
						}

						holder.txtTitle.setText(item.getType());
						holder.txtContent.setText(item.getMsg());

						if(holder.ibOptions != null)
						{
							Utils.ampliarAreaTouch(holder.ibOptions, 100);
							final String msgId = item.getMsgId();
							holder.ibOptions.setOnClickListener(new View.OnClickListener()
							{
								@Override
								public void onClick(View v)
								{
									activity.showOptionsCard(position, msgId);
								}
							});
						}

						if(item.getKind() == Message.KIND_TEXT)
						{
							if(item.getType().equals(Message.TYPE_SMS))
							{
								if(SuscriptionHelper.getTypeNumber(suscription, item.getChannel(), activity).equals(Suscription.NUMBER_PAYOUT))
								{
									holder.iconPrice.setVisibility(ImageView.VISIBLE);
									holder.iconSMS.setVisibility(ImageView.GONE);
								}
								else
								{
									holder.iconPrice.setVisibility(ImageView.GONE);
									holder.iconSMS.setVisibility(ImageView.VISIBLE);
								}
							}
							else
							{
								holder.iconPrice.setVisibility(ImageView.GONE);
								holder.iconSMS.setVisibility(ImageView.GONE);
							}
						}
					}

					String thumb					= "";
					String link						= "";
					final Activity activityContext	= activity;

					//Tratamiento de imagen si viene
					if(StringUtils.isNotEmpty(item.getLink()))
					{
						link	= item.getLink();
						thumb	= item.getLink();

						if(StringUtils.isNotEmpty(item.getLinkThumbnail()))
						{
							thumb = item.getLinkThumbnail();
						}
					}
					else
					{
						if(StringUtils.isNotEmpty(item.getLinkThumbnail()))
						{
							thumb	= item.getLinkThumbnail();
							link	= item.getLinkThumbnail();
						}
					}

					final Uri uri = Uri.parse(link);

					if(StringUtils.isNotEmpty(thumb) && holder.ivPicture != null)
					{
						Picasso.with(activity).load(thumb).placeholder(R.drawable.step1_idkey).into(holder.ivPicture);
					}

					if(item.getKind() != Message.KIND_IMAGE && item.getKind() != Message.KIND_VIDEO)
					{
						if(holder.dividerTitle != null)
						{
							holder.dividerTitle.setVisibility(View.GONE);
						}

						if(holder.btnView != null)
						{
							holder.btnView.setVisibility(Button.GONE);
						}

						if(holder.btnShare != null)
						{
							holder.btnShare.setVisibility(Button.GONE);
						}
					}
					else
					{
						holder.dividerTitle.setVisibility(View.VISIBLE);
						holder.btnView.setVisibility(Button.VISIBLE);
						holder.btnShare.setVisibility(Button.VISIBLE);

						if(item.getKind() == Message.KIND_IMAGE)
						{
							holder.btnView.setText(activity.getString(R.string.special_card_view));
						}
						else
						{
							if(item.getKind() == Message.KIND_VIDEO)
							{
								holder.btnView.setText(activity.getString(R.string.special_card_video));
							}
						}


						//Los botones irán al browser salvo Compartir que hace lo mismo que el Compartir una card normal
						holder.btnView.setOnClickListener(new View.OnClickListener()
						{
							@Override
							public void onClick(final View v)
							{
								Intent intent = new Intent(Intent.ACTION_VIEW, uri);
								activityContext.startActivity(intent);
							}
						});

						final String type	= item.getType();
						final String text	= item.getMsg()+" "+item.getLink();
						holder.btnShare.setOnClickListener(new View.OnClickListener()
						{
							@Override
							public void onClick(final View v)
							{
								Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
								//TODO ver más adelante posibilidad de compartir contenido directamente
								sharingIntent.setType("text/plain");
								sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, type);
								sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, text);
								activityContext.startActivity(Intent.createChooser(sharingIntent, activity.getResources().getString(R.string.share)));
							}
						});
					}

					if(holder.iconDown != null && holder.btnDownload != null)
					{
						holder.iconDown.setOnClickListener(new View.OnClickListener()
						{
							@Override
							public void onClick(final View v)
							{
								Intent intent = new Intent(Intent.ACTION_VIEW, uri);
								activityContext.startActivity(intent);
							}
						});

						holder.btnDownload.setOnClickListener(new View.OnClickListener()
						{
							@Override
							public void onClick(final View v)
							{
								Intent intent = new Intent(Intent.ACTION_VIEW, uri);
								activityContext.startActivity(intent);
							}
						});
					}

					if(item.getKind() == Message.KIND_TWITTER)
					{
						//Redesign by me for social's cards
						String time				= DateUtils.getTimeFromTs(item.getCreated(), activity.getApplicationContext());
						String socialAccount	= item.getSocialAccount();

						if(StringUtils.isNotEmpty(time))
						{
							socialAccount += " - "+time;
						}

						holder.socialAccount.setText(socialAccount);
						holder.socialDate.setText(item.getSocialDate());
						holder.txtContent.setOnClickListener(new View.OnClickListener()
						{
							@Override
							public void onClick(final View v)
							{
								//Indica a Analytics que se uso el contenido social
								GoogleAnalytics.getInstance(activityContext).newTracker(Common.HASH_GOOGLEANALYTICS)
									.send(new HitBuilders.EventBuilder().setCategory("Social").setAction("VerContenido").setLabel("AccionUser").build());
							}
						});
					}

					if(holder.iconComent != null && holder.rlComment != null && item.getKind() != Message.KIND_NOTE)
					{
						holder.ivEdit.setColorFilter(Color.parseColor(Common.COLOR_COMMENT));
						holder.ivEdit.setOnClickListener(new View.OnClickListener()
						{
							@Override
							public void onClick(final View view)
							{
								new MaterialDialog.Builder(activity).title(activity.getString(R.string.enrich_commentheader)).inputType(InputType.TYPE_CLASS_TEXT)
								.positiveText(R.string.enrich_save).cancelable(true).inputRange(0, 160).positiveColor(Color.parseColor(Common.COLOR_COMMENT))
								.input(activity.getString(R.string.enrich_commenthint), item.getNote(), new MaterialDialog.InputCallback()
								{
									@Override
									public void onInput(MaterialDialog dialog, CharSequence input)
									{
										if(input != null)
										{
											if(input != "")
											{
												final String comment = input.toString();

												if(StringUtils.isNotEmpty(comment))
												{
													Realm realm = Realm.getDefaultInstance();
													realm.executeTransactionAsync(new Realm.Transaction()
													{
														@Override
														public void execute(Realm bgRealm)
														{
															Message message = bgRealm.where(Message.class).equalTo(Message.KEY_API, id).findFirst();

															if(message != null)
															{
																message.setNote(comment);
															}
														}
													}, new Realm.Transaction.OnSuccess()
													{
														@Override
														public void onSuccess()
														{
															holder.txtComment.setText(item.getNote());
															Utils.showViewWithFade(holder.rlComment, activity);
															activity.attach(id, comment, item.getAttached(), item.getAttachedTwo(), item.getAttachedThree());
														}
													});

												}
											}
										}
									}
								}).show();
							}
						});

						if(StringUtils.isNotEmpty(item.getNote()))
						{
							holder.txtComment.setText(item.getNote());
							holder.rlComment.setVisibility(LinearLayout.VISIBLE);
						}
						else
						{
							holder.txtComment.setText("");
							holder.rlComment.setVisibility(LinearLayout.GONE);
						}

						holder.iconComent.setOnClickListener(new View.OnClickListener()
						{
							@Override
							public void onClick(final View v)
							{
								if(StringUtils.isNotEmpty(item.getNote()))
								{
									new MaterialDialog.Builder(activity).title(activity.getString(R.string.enrich_commentheader)).inputType(InputType.TYPE_CLASS_TEXT)
									.positiveText(R.string.enrich_save).cancelable(true).inputRange(0, 160).positiveColor(Color.parseColor(Common.COLOR_COMMENT))
									.input(activity.getString(R.string.enrich_commenthint), item.getNote(), new MaterialDialog.InputCallback()
									{
										@Override
										public void onInput(MaterialDialog dialog, CharSequence input)
										{
											if(input != null)
											{
												if(input != "")
												{
													final String comment = input.toString();

													if(StringUtils.isNotEmpty(comment))
													{
														Realm realm = Realm.getDefaultInstance();
														realm.executeTransactionAsync(new Realm.Transaction()
														{
															@Override
															public void execute(Realm bgRealm)
															{
																Message message = bgRealm.where(Message.class).equalTo(Message.KEY_API, id).findFirst();

																if(message != null)
																{
																	message.setNote(comment);
																}
															}
														}, new Realm.Transaction.OnSuccess()
														{
															@Override
															public void onSuccess()
															{
																holder.txtComment.setText(item.getNote());
																Utils.showViewWithFade(holder.rlComment, activity);
																activity.attach(id, comment, item.getAttached(), item.getAttachedTwo(), item.getAttachedThree());
															}
														});
													}
												}
											}
										}
									}).show();
								}
								else
								{
									new MaterialDialog.Builder(activity).title(activity.getString(R.string.enrich_addcommentheader)).inputType(InputType.TYPE_CLASS_TEXT)
									.positiveText(R.string.enrich_save).cancelable(true).inputRange(0, 160).positiveColor(Color.parseColor(Common.COLOR_COMMENT))
									.input(activity.getString(R.string.enrich_commenthint), item.getNote(), new MaterialDialog.InputCallback()
									{
										@Override
										public void onInput(MaterialDialog dialog, CharSequence input)
										{
											if(input != null)
											{
												if(input != "")
												{
													final String comment = input.toString();

													if(StringUtils.isNotEmpty(comment))
													{
														Realm realm = Realm.getDefaultInstance();
														realm.executeTransactionAsync(new Realm.Transaction()
														{
															@Override
															public void execute(Realm bgRealm)
															{
																Message message = bgRealm.where(Message.class).equalTo(Message.KEY_API, id).findFirst();

																if(message != null)
																{
																	message.setNote(comment);
																}
															}
														}, new Realm.Transaction.OnSuccess()
														{
															@Override
															public void onSuccess()
															{
																holder.txtComment.setText(item.getNote());
																Utils.showViewWithFade(holder.rlComment, activity);
																activity.attach(id, comment, item.getAttached(), item.getAttachedTwo(), item.getAttachedThree());
															}
														});
													}
												}
											}
										}
									}).show();
								}
							}
						});
					}
					else
					{
						if(holder.iconComent != null && item.getKind() == Message.KIND_NOTE)
						{
							holder.iconComent.setOnClickListener(new View.OnClickListener()
							{
								@Override
								public void onClick(View view)
								{
									activity.onCreateNote(item.getMsgId());
								}
							});
						}
					}

					if(holder.iconAttach != null)
					{
						if(StringUtils.isNotEmpty(item.getUri()) && StringUtils.isNotEmpty(item.getUriTwo()) && StringUtils.isNotEmpty(item.getUriThree()))
						{
							holder.iconAttach.setImageDrawable(ContextCompat.getDrawable(activity, R.drawable.photo_disabled));
							holder.iconAttach.setOnClickListener(new View.OnClickListener()
							{
								@Override
								public void onClick(View view)
								{
									Toast.makeText(activity, activity.getString(R.string.attach_limit), Toast.LENGTH_SHORT).show();
								}
							});
						}
						else
						{
							holder.iconAttach.setOnClickListener(new View.OnClickListener()
							{
								@Override
								public void onClick(final View v)
								{
									if(StringUtils.isNotEmpty(item.getUri()) && StringUtils.isNotEmpty(item.getUriTwo()) && StringUtils.isNotEmpty(item.getUriThree()))
									{
										Toast.makeText(activity, activity.getString(R.string.attach_limit), Toast.LENGTH_SHORT).show();
										holder.iconAttach.setImageDrawable(ContextCompat.getDrawable(activity, R.drawable.photo_disabled));
										holder.iconAttach.setOnClickListener(new View.OnClickListener()
										{
											@Override
											public void onClick(View view)
											{
											}
										});
									}
									else
									{
										activity.callCamera(item.getMsgId());
									}
								}
							});
						}
					}

					if(holder.imgOne != null && holder.animOne != null && StringUtils.isNotEmpty(item.getUri()))
					{
						Utils.showViewWithFade(holder.animOne, activity);
						holder.animOne.setProgress(0);
						holder.animOne.setProgress(20);
						Picasso.with(activity).load(item.getUri()).resize(100, 100).into(holder.imgOne, new Callback()
						{
							@Override
							public void onSuccess()
							{
								holder.animOne.getAnimator().setInterpolator(new AccelerateDecelerateInterpolator());
								holder.animOne.setProgress(100);
								holder.animOne.startAnimation();
								Utils.hideViewWithFade(holder.animOne, activity);
								Utils.showViewWithFade(holder.imgOne, activity);
								holder.imgOne.setOnClickListener(new View.OnClickListener()
								{
									@Override
									public void onClick(View view)
									{
										Intent intent = new Intent(activity, GalleryActivity.class);
										intent.putExtra(Common.KEY_ID, item.getMsgId());
										activity.startActivity(intent);
									}
								});
							}

							@Override
							public void onError()
							{
								Utils.hideViewWithFade(holder.animOne, activity);
								Utils.showViewWithFade(holder.imgOne, activity);
								//Reintentamos con el link de la imagen
								Utils.showViewWithFade(holder.animOne, activity);
								holder.animOne.setProgress(0);
								holder.animOne.setProgress(20);
								Picasso.with(activity).load(item.getAttached()).resize(100, 100).into(holder.imgOne, new Callback()
								{
									@Override
									public void onSuccess()
									{
										holder.animOne.getAnimator().setInterpolator(new AccelerateDecelerateInterpolator());
										holder.animOne.setProgress(100);
										holder.animOne.startAnimation();
										Utils.hideViewWithFade(holder.animOne, activity);
										Utils.showViewWithFade(holder.imgOne, activity);
										holder.imgOne.setOnClickListener(new View.OnClickListener()
										{
											@Override
											public void onClick(View view)
											{
												Intent intent = new Intent(activity, GalleryActivity.class);
												intent.putExtra(Common.KEY_ID, item.getMsgId());
												activity.startActivity(intent);
											}
										});
									}

									@Override
									public void onError()
									{
										Utils.hideViewWithFade(holder.animOne, activity);
										Utils.showViewWithFade(holder.imgOne, activity);
									}
								});
							}
						});
					}

					if(holder.imgTwo != null && holder.animTwo != null && StringUtils.isNotEmpty(item.getAttachedTwo()))
					{
						Utils.showViewWithFade(holder.animOne, activity);
						Utils.showViewWithFade(holder.animTwo, activity);
						holder.animOne.getAnimator().setInterpolator(new AccelerateDecelerateInterpolator());
						holder.animOne.setProgress(100);
						holder.animOne.startAnimation();
						holder.animTwo.setProgress(0);
						holder.animTwo.setProgress(20);
						Picasso.with(activity).load(item.getUriTwo()).resize(100, 100).into(holder.imgTwo, new Callback()
						{
							@Override
							public void onSuccess()
							{
								holder.animTwo.getAnimator().setInterpolator(new AccelerateDecelerateInterpolator());
								holder.animTwo.setProgress(100);
								holder.animTwo.startAnimation();
								Utils.hideViewWithFade(holder.animTwo, activity);
								Utils.hideViewWithFade(holder.animOne, activity);
								Utils.showViewWithFade(holder.imgTwo, activity);
								holder.imgTwo.setOnClickListener(new View.OnClickListener()
								{
									@Override
									public void onClick(View view)
									{
										Intent intent = new Intent(activity, GalleryActivity.class);
										intent.putExtra(Common.KEY_ID, item.getMsgId());
										activity.startActivity(intent);
									}
								});
							}

							@Override
							public void onError()
							{
								Utils.hideViewWithFade(holder.animTwo, activity);
								Utils.hideViewWithFade(holder.animOne, activity);
								Utils.showViewWithFade(holder.imgTwo, activity);
								//Reintentamos con el link de la imagen
								Utils.showViewWithFade(holder.animOne, activity);
								Utils.showViewWithFade(holder.animTwo, activity);
								holder.animOne.getAnimator().setInterpolator(new AccelerateDecelerateInterpolator());
								holder.animOne.setProgress(100);
								holder.animOne.startAnimation();
								holder.animTwo.setProgress(0);
								holder.animTwo.setProgress(20);
								Picasso.with(activity).load(item.getAttachedTwo()).resize(100, 100).into(holder.imgTwo, new Callback()
								{
									@Override
									public void onSuccess()
									{
										holder.animTwo.getAnimator().setInterpolator(new AccelerateDecelerateInterpolator());
										holder.animTwo.setProgress(100);
										holder.animTwo.startAnimation();
										Utils.hideViewWithFade(holder.animTwo, activity);
										Utils.hideViewWithFade(holder.animOne, activity);
										Utils.showViewWithFade(holder.imgTwo, activity);
										holder.imgTwo.setOnClickListener(new View.OnClickListener()
										{
											@Override
											public void onClick(View view)
											{
												Intent intent = new Intent(activity, GalleryActivity.class);
												intent.putExtra(Common.KEY_ID, item.getMsgId());
												activity.startActivity(intent);
											}
										});
									}

									@Override
									public void onError()
									{
										Utils.hideViewWithFade(holder.animTwo, activity);
										Utils.hideViewWithFade(holder.animOne, activity);
										Utils.showViewWithFade(holder.imgTwo, activity);
									}
								});
							}
						});
					}

					if(holder.imgThree != null && holder.animThree != null && StringUtils.isNotEmpty(item.getAttachedThree()))
					{
						Utils.showViewWithFade(holder.animOne, activity);
						Utils.showViewWithFade(holder.animTwo, activity);
						Utils.showViewWithFade(holder.animThree, activity);
						holder.animOne.getAnimator().setInterpolator(new AccelerateDecelerateInterpolator());
						holder.animOne.setProgress(100);
						holder.animOne.startAnimation();
						holder.animTwo.getAnimator().setInterpolator(new AccelerateDecelerateInterpolator());
						holder.animTwo.setProgress(100);
						holder.animTwo.startAnimation();
						holder.animThree.setProgress(0);
						holder.animThree.setProgress(20);
						Picasso.with(activity).load(item.getUriThree()).resize(100, 100).into(holder.imgThree, new Callback()
						{
							@Override
							public void onSuccess()
							{
								holder.animThree.getAnimator().setInterpolator(new AccelerateDecelerateInterpolator());
								holder.animThree.setProgress(100);
								holder.animThree.startAnimation();
								Utils.hideViewWithFade(holder.animThree, activity);
								Utils.hideViewWithFade(holder.animTwo, activity);
								Utils.hideViewWithFade(holder.animOne, activity);
								Utils.showViewWithFade(holder.imgThree, activity);
								holder.imgThree.setOnClickListener(new View.OnClickListener()
								{
									@Override
									public void onClick(View view)
									{
										Intent intent = new Intent(activity, GalleryActivity.class);
										intent.putExtra(Common.KEY_ID, item.getMsgId());
										activity.startActivity(intent);
									}
								});
							}

							@Override
							public void onError()
							{
								Utils.hideViewWithFade(holder.animThree, activity);
								Utils.hideViewWithFade(holder.animTwo, activity);
								Utils.hideViewWithFade(holder.animOne, activity);
								Utils.showViewWithFade(holder.imgThree, activity);
								//Reintentamos con el link de la imagen
								Utils.showViewWithFade(holder.animOne, activity);
								Utils.showViewWithFade(holder.animTwo, activity);
								Utils.showViewWithFade(holder.animThree, activity);
								holder.animOne.getAnimator().setInterpolator(new AccelerateDecelerateInterpolator());
								holder.animOne.setProgress(100);
								holder.animOne.startAnimation();
								holder.animTwo.getAnimator().setInterpolator(new AccelerateDecelerateInterpolator());
								holder.animTwo.setProgress(100);
								holder.animTwo.startAnimation();
								holder.animThree.setProgress(0);
								holder.animThree.setProgress(20);
								Picasso.with(activity).load(item.getAttachedThree()).resize(100, 100).into(holder.imgThree, new Callback()
								{
									@Override
									public void onSuccess()
									{
										holder.animThree.getAnimator().setInterpolator(new AccelerateDecelerateInterpolator());
										holder.animThree.setProgress(100);
										holder.animThree.startAnimation();
										Utils.hideViewWithFade(holder.animThree, activity);
										Utils.hideViewWithFade(holder.animTwo, activity);
										Utils.hideViewWithFade(holder.animOne, activity);
										Utils.showViewWithFade(holder.imgThree, activity);
										holder.imgThree.setOnClickListener(new View.OnClickListener()
										{
											@Override
											public void onClick(View view)
											{
												Intent intent = new Intent(activity, GalleryActivity.class);
												intent.putExtra(Common.KEY_ID, item.getMsgId());
												activity.startActivity(intent);
											}
										});
									}

									@Override
									public void onError()
									{
										Utils.hideViewWithFade(holder.animThree, activity);
										Utils.hideViewWithFade(holder.animTwo, activity);
										Utils.hideViewWithFade(holder.animOne, activity);
										Utils.showViewWithFade(holder.imgThree, activity);
									}
								});
							}
						});
					}

					if(holder.ivChain != null)
					{
						holder.ivChain.setOnClickListener(new View.OnClickListener()
						{
							@Override
							public void onClick(View view)
							{
								activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://chain.vloom.io/")));
							}
						});
					}

					//Nueva card para recepción de facturas
					if(holder.cardReceipt != null && item.getKind() == Message.KIND_INVOICE)
					{
						holder.cardReceipt.setOnClickListener(new View.OnClickListener()
						{
							@Override
							public void onClick(View view)
							{
								activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(item.getLink())));
							}
						});

						if(holder.circleReceipt != null && holder.txtReceipt != null)
						{
							//Agregado para detectar si el color es claro
							if(Utils.isLightColor(color, activity))
							{
								holder.circleReceipt.setColorFilter(Color.parseColor(Common.COLOR_ACCENT));
								holder.txtReceipt.setTextColor(Color.parseColor(Common.COLOR_ACCENT));
							}
							else
							{
								//Agregado para evitar excepciones de tipo unknown color
								try
								{
									holder.circleReceipt.setColorFilter(Color.parseColor(suscription.getColorHex()));
									holder.txtReceipt.setTextColor(Color.parseColor(suscription.getColorHex()));
								}
								catch(Exception e)
								{
									holder.circleReceipt.setColorFilter(Color.parseColor(Common.COLOR_ACTION));
									holder.txtReceipt.setTextColor(Color.parseColor(Common.COLOR_ACTION));
								}
							}
						}
					}
					else
					{
						//Para nuevo editar con longpress para las notas
						if(holder.cardReceipt != null && item.getKind() == Message.KIND_NOTE)
						{
							holder.cardReceipt.setOnLongClickListener(new View.OnLongClickListener()
							{
								@Override
								public boolean onLongClick(View view)
								{
									activity.onCreateNote(item.getMsgId());
									return false;
								}
							});
						}
					}
				}
			}
		}
		catch(Exception e)
		{
			Utils.logError(activity, "CardAdapter:onBindViewHolder - Exception:", e);
		}
	}

	@Override
	public int getItemCount()
	{
		//Agregado para prevenir lista sin resultados
		if(notificationList != null)
		{
			return notificationList.size();
		}
		else
		{
			return 0;
		}
	}


	@Override
	public int getItemViewType(int position)
	{
		int type = 0;

		if(notificationList != null)
		{
			if(notificationList.size() > 0)
			{
				if(notificationList.get(position) != null)
				{
					type = notificationList.get(position).getKind();
				}
			}
		}

		return type;
	}
}