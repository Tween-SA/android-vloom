package com.tween.viacelular.activities;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.transition.Explode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.getkeepsafe.taptargetview.TapTargetView;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.squareup.picasso.Picasso;
import com.tween.viacelular.R;
import com.tween.viacelular.adapters.CardAdapter;
import com.tween.viacelular.asynctask.AttachAsyncTask;
import com.tween.viacelular.asynctask.ConfirmReadingAsyncTask;
import com.tween.viacelular.asynctask.SendIdentificationKeyAsyncTask;
import com.tween.viacelular.interfaces.CallBackListener;
import com.tween.viacelular.models.Message;
import com.tween.viacelular.models.MessageHelper;
import com.tween.viacelular.models.Migration;
import com.tween.viacelular.models.Suscription;
import com.tween.viacelular.models.SuscriptionHelper;
import com.tween.viacelular.services.MyDownloadService;
import com.tween.viacelular.services.MyUploadService;
import com.tween.viacelular.utils.Common;
import com.tween.viacelular.utils.StringUtils;
import com.tween.viacelular.utils.Utils;
import org.json.JSONObject;
import java.io.ByteArrayOutputStream;
import java.io.File;
import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;

public class CardViewActivity extends AppCompatActivity
{
	private Suscription				suscription		= null;
	private MaterialDialog			list			= null;
	private RealmResults<Message>	notifications	= null;
	private int						colorTitle		= Color.WHITE;
	private int						colorSubTitle	= Color.LTGRAY;
	public String					msgId			= "";
	public String					idValue			= "";
	private Uri						mDownloadUrl	= null;
	private Boolean					isFabOpen		= false;
	private RecyclerView			rcwCard;
	private CardAdapter				mAdapter;
	private CoordinatorLayout		Clayout;
	private CardView				cardPayout, cardSuscribe;
	private RelativeLayout			rlEmpty, rlClientId;
	private int						originalSoftInputMode;
	private FloatingActionButton	fabOpen,fabNote,fabPhoto;
	private Animation				animOpen, animClose, animRotateForward, animRotateBackward;
	private TextView				txtTitle, txtSubTitleCollapsed, idTitle, idText;
	private String					companyId;
	private Toolbar					toolBar;
	private Uri						tempUri;
	private BroadcastReceiver		mBroadcastReceiver;
	private ImageView				ivHelp;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		try
		{
			if(Common.API_LEVEL >= Build.VERSION_CODES.LOLLIPOP)
			{
				getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
				getWindow().setEnterTransition(new Explode());
				getWindow().setReenterTransition(new Explode());
				getWindow().setExitTransition(new Explode());
			}

			super.onCreate(savedInstanceState);
			Migration.getDB(this);
			setContentView(R.layout.activity_cardview);
			SharedPreferences preferences				= getSharedPreferences(Common.KEY_PREF, Context.MODE_PRIVATE);
			toolBar										= (Toolbar) findViewById(R.id.toolBarCardView);
			rcwCard										= (RecyclerView) findViewById(R.id.rcwCard);
			cardPayout									= (CardView) findViewById(R.id.cardPayout);
			rlEmpty										= (RelativeLayout) findViewById(R.id.rlEmpty);
			cardSuscribe								= (CardView) findViewById(R.id.cardSuscribe);
			TextView txtSubSuscribe						= (TextView) findViewById(R.id.txtSubSuscribe);
			ImageView ibBack							= (ImageView) findViewById(R.id.ibBack);
			ImageView circleView						= (ImageView) findViewById(R.id.circleView);
			txtTitle									= (TextView) findViewById(R.id.txtTitle);
			rlClientId									= (RelativeLayout) findViewById(R.id.rlClientId);
			ivHelp										= (ImageView) findViewById(R.id.ivHelp);
			idTitle										= (TextView) findViewById(R.id.idTitle);
			idText										= (TextView) findViewById(R.id.idText);
			txtSubTitleCollapsed						= (TextView) findViewById(R.id.txtSubTitleCollapsed);
			fabOpen										= (FloatingActionButton) findViewById(R.id.fabOpen);
			fabNote										= (FloatingActionButton) findViewById(R.id.fabNote);
			fabPhoto									= (FloatingActionButton) findViewById(R.id.fabPhoto);
			animOpen									= AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_open);
			animClose									= AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_close);
			animRotateForward							= AnimationUtils.loadAnimation(getApplicationContext(), R.anim.rotate_forward);
			animRotateBackward							= AnimationUtils.loadAnimation(getApplicationContext(), R.anim.rotate_backward);
			rcwCard.setHasFixedSize(true);
			RecyclerView.LayoutManager mLayoutManager	= new LinearLayoutManager(this);
			FirebaseAuth mAuth							= FirebaseAuth.getInstance();
			rcwCard.setLayoutManager(mLayoutManager);
			setSupportActionBar(toolBar);
			toolBar.setTitle("");
			toolBar.setSubtitle("");
			setTitle("");

			if(mAuth != null)
			{
				mAuth.signOut();
				mAuth.signInAnonymously().addOnCompleteListener(this, new OnCompleteListener<AuthResult>()
				{
					@Override
					public void onComplete(@NonNull Task<AuthResult> task)
					{
						if(!task.isSuccessful())
						{
							System.out.println("OnCompleteListener-signInAnonymously:getException: ");
						}
					}
				});
			}

			if(Utils.checkSesion(this, Common.ANOTHER_SCREEN))
			{
				final Intent intentRecive			= getIntent();
				Realm realm							= Realm.getDefaultInstance();

				if(intentRecive != null)
				{
					//Modificaciones para migrar entidad Company completa a Realm
					companyId	= intentRecive.getStringExtra(Common.KEY_ID);
					suscription	= realm.where(Suscription.class).equalTo(Suscription.KEY_API, companyId).findFirst();

					if(suscription == null)
					{
						//Intentar con otra cosa
						if(StringUtils.isNotEmpty(intentRecive.getStringExtra(Suscription.KEY_API)))
						{
							JSONObject json	= new JSONObject(intentRecive.getStringExtra(Suscription.KEY_API));
							suscription		= SuscriptionHelper.parseEntity(json, companyId, "", this, false, Common.BOOL_YES, true);
						}
					}

					if(suscription != null)
					{
						txtTitle.setText(suscription.getName());
						String image	= suscription.getImage();
						String color	= suscription.getColorHex();

						//Agregado para validar companies sin color
						if(StringUtils.isEmpty(color))
						{
							color = Common.COLOR_ACTION;
						}

						//Agregado para detectar si el color es claro
						if(Utils.isLightColor(color, this))
						{
							colorTitle		= Color.BLACK;
							colorSubTitle	= Color.DKGRAY;
							ibBack.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_arrow_back_black_24dp));
							toolBar.setOverflowIcon(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_more_vert_black_24dp));
						}
						else
						{
							ibBack.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.back));
						}

						//Agregado para destacar en gris los titulos si la company fue bloqueada y la card ignorada
						if(suscription.getGray() == Common.BOOL_YES)
						{
							txtTitle.setTextColor(Utils.adjustAlpha(colorTitle, Common.ALPHA_FOR_BLOCKS));
							txtSubTitleCollapsed.setTextColor(Utils.adjustAlpha(colorSubTitle, Common.ALPHA_FOR_BLOCKS));
							color = Common.COLOR_BLOCKED;
						}
						else
						{
							txtTitle.setTextColor(colorTitle);
							txtSubTitleCollapsed.setTextColor(Utils.adjustAlpha(colorSubTitle, Common.ALPHA_FOR_SUBTITLE));
						}
						
						if(suscription.getType() == Suscription.TYPE_FOLDER)
						{
							//Mostramos icono default de carpeta
							Picasso.with(this).load(R.drawable.ic_folder).into(circleView);
						}
						else
						{
							//Mostramos el logo de la company
							if(StringUtils.isNotEmpty(image))
							{
								//Modificación de librería para recargar imagenes a mientras se está viendo el listado y optimizar vista
								Picasso.with(getApplicationContext()).load(image).placeholder(R.mipmap.ic_launcher).into(circleView);
							}
							else
							{
								//Mostrar el logo de Vloom si no tiene logo
								Picasso.with(getApplicationContext()).load(Suscription.ICON_APP).placeholder(R.mipmap.ic_launcher).into(circleView);
							}
						}
						
						refreshIdZone();

						txtSubTitleCollapsed.setText(suscription.getIndustry());
						toolBar.setBackgroundColor(Color.parseColor(color));
						Utils.tintColorScreen(this, color);
						//Agregado como atajo para ir a la pantalla Configuración
						txtTitle.setOnClickListener(new View.OnClickListener()
						{
							@Override
							public void onClick(final View v)
							{
								goToSettings();
							}
						});

						txtSubTitleCollapsed.setOnClickListener(new View.OnClickListener()
						{
							@Override
							public void onClick(final View v)
							{
								goToSettings();
							}
						});

						mAdapter = new CardAdapter(this, suscription.getCompanyId());
						rcwCard.setAdapter(mAdapter);
					}
					else
					{
						txtTitle.setText(getString(R.string.app_name));
						Utils.tintColorScreen(this, Common.COLOR_ACTION);
					}
				}
				else
				{
					txtTitle.setText(getString(R.string.app_name));
					Utils.tintColorScreen(this, Common.COLOR_ACTION);
				}

				Utils.ampliarAreaTouch(ibBack, 150);
				ibBack.setOnClickListener(new View.OnClickListener()
				{
					@Override
					public void onClick(final View v)
					{
						onBackPressed();
					}
				});

				Clayout = (CoordinatorLayout) findViewById(R.id.clSnack);

				if(suscription != null)
				{
					long unread = realm.where(Message.class).equalTo(Message.KEY_DELETED, Common.BOOL_NO).lessThan(Common.KEY_STATUS, Message.STATUS_READ)
									.equalTo(Suscription.KEY_API, suscription.getCompanyId()).count();

					if(unread > 0)
					{
						new ConfirmReadingAsyncTask(false, companyId, "", Message.STATUS_READ, this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
					}

					//Validaciones para mostrar o no campos según disponibilidad de datos
					if(StringUtils.isNotEmpty(suscription.getAbout()))
					{
						txtSubSuscribe.setText(suscription.getAbout());
					}
					else
					{
						//Validar con PO para ver si cuando no hay texto no mostramos nada en vez del placeholder siguiente
						txtSubSuscribe.setText(getString(R.string.landing_title, " "+suscription.getName()));
					}

					refresh(false);
				}
			}

			mBroadcastReceiver = new BroadcastReceiver()
			{
				@Override
				public void onReceive(Context context, Intent intent)
				{
					switch(intent.getAction())
					{
						case MyDownloadService.DOWNLOAD_COMPLETED:
							// Get number of bytes downloaded
							long numBytes = intent.getLongExtra(MyDownloadService.EXTRA_BYTES_DOWNLOADED, 0);
							// Alert success
							System.out.println(numBytes+" bytes downloaded complete in "+intent.getStringExtra(MyDownloadService.EXTRA_DOWNLOAD_PATH));
						break;

						case MyDownloadService.DOWNLOAD_ERROR:
							// Alert failure
							System.out.println("Failed to download from "+intent.getStringExtra(MyDownloadService.EXTRA_DOWNLOAD_PATH));
						break;

						case MyUploadService.UPLOAD_COMPLETED:
						case MyUploadService.UPLOAD_ERROR:
							System.out.println("upload:" + intent);
							onUploadResultIntent(intent);
						break;

						default:
							System.out.println("Default intent getAction: "+intent.getAction());
						break;
					}
				}
			};

			if(!preferences.getBoolean(Common.KEY_PREF_SHOWNOTE, false))
			{
				Utils.initShowCase(this, fabOpen, getString(R.string.showcase_note_title), getString(R.string.showcase_note_subtitle), new TapTargetView.Listener()
				{
					@Override
					public void onTargetClick(TapTargetView view)
					{
						super.onTargetClick(view);
						animateFab(view);
					}
				});
				preferences.edit().putBoolean(Common.KEY_PREF_SHOWNOTE, true).apply();
			}
		}
		catch(Exception e)
		{
			Utils.logError(this, getLocalClassName()+":onCreate - Exception:", e);
		}
	}
	
	public void showHelp(View view)
	{
		final Activity activity = this;
		try
		{
			new MaterialDialog.Builder(this).cancelable(true).content(R.string.id_help).neutralText(R.string.ok).positiveText(R.string.landing_suscribe)
				.onPositive(new MaterialDialog.SingleButtonCallback()
				{
					@Override
					public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which)
					{
						try
						{
							dialog.dismiss();
							modifyId(null);
						}
						catch(Exception e)
						{
							Utils.logError(activity, getLocalClassName()+":showHelp - Exception:", e);
						}
					}
				})
				.onNeutral(new MaterialDialog.SingleButtonCallback()
				{
					@Override
					public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which)
					{
						dialog.dismiss();
					}
				}).show();
		}
		catch(Exception e)
		{
			Utils.logError(this, getLocalClassName()+":showHelp - Exception:", e);
		}
	}
	
	public void retry()
	{
		try
		{
			new SendIdentificationKeyAsyncTask(this, true, idValue, companyId).executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
			refreshIdZone();
		}
		catch(Exception e)
		{
			Utils.logError(this, getLocalClassName()+":retry - Exception:", e);
		}
	}
	
	public void modifyId(View view)
	{
		final Activity activity = this;
		
		try
		{
			new MaterialDialog.Builder(this).title(getString(R.string.id_title)).inputType(InputType.TYPE_CLASS_TEXT)
				.positiveText(R.string.enrich_save).cancelable(true).inputRange(0, 40)
				.input(getString(R.string.enrich_notehint), idValue, new MaterialDialog.InputCallback()
				{
					@Override
					public void onInput(@NonNull MaterialDialog dialog, CharSequence input)
					{
						if(input != null)
						{
							if(input != "")
							{
								idValue = input.toString();
								
								if(StringUtils.isNotEmpty(idValue))
								{
									new SendIdentificationKeyAsyncTask(activity, true, idValue, companyId).executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
									refreshIdZone();
								}
							}
						}
					}
				}).show();
		}
		catch(Exception e)
		{
			Utils.logError(this, getLocalClassName()+":modifyId - Exception:", e);
		}
	}
	
	public void refreshIdZone()
	{
		try
		{
			if(StringUtils.isNotEmpty(companyId))
			{
				Realm realm				= Realm.getDefaultInstance();
				Suscription suscription	= realm.where(Suscription.class).equalTo(Suscription.KEY_API, companyId).findFirst();
				
				if(suscription != null)
				{
					if(StringUtils.isNotEmpty(suscription.getIdentificationKey()))
					{
						rlClientId.setVisibility(RelativeLayout.VISIBLE);
						
						if(StringUtils.isNotEmpty(suscription.getIdentificationValue()) && suscription.getDataSent() == Common.BOOL_YES)
						{
							idTitle.setText(getString(R.string.id_ok));
							idText.setText(getString(R.string.id_oktext)+" "+suscription.getIdentificationValue());
							ivHelp.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_edit_white_18dp));
							ivHelp.setOnClickListener(new View.OnClickListener()
							{
								@Override
								public void onClick(final View view)
								{
									modifyId(view);
								}
							});
							
							if(Common.API_LEVEL >= Build.VERSION_CODES.LOLLIPOP)
							{
								rlClientId.setBackground(getDrawable(R.drawable.idok));
							}
							else
							{
								rlClientId.setBackgroundDrawable(getResources().getDrawable(R.drawable.idok));
							}
						}
						else
						{
							if(StringUtils.isNotEmpty(suscription.getIdentificationValue()) && suscription.getDataSent() != Common.BOOL_YES)
							{
								idTitle.setText(getString(R.string.id_title));
								idText.setText(getString(R.string.id_oktext)+" "+suscription.getIdentificationValue());
								ivHelp.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_edit_white_18dp));
								ivHelp.setOnClickListener(new View.OnClickListener()
								{
									@Override
									public void onClick(final View view)
									{
										modifyId(view);
									}
								});
								idTitle.setOnClickListener(new View.OnClickListener()
								{
									@Override
									public void onClick(final View view)
									{
										retry();
									}
								});
								idText.setOnClickListener(new View.OnClickListener()
								{
									@Override
									public void onClick(final View view)
									{
										retry();
									}
								});
								if(Common.API_LEVEL >= Build.VERSION_CODES.LOLLIPOP)
								{
									rlClientId.setBackground(getDrawable(R.drawable.idfail));
								}
								else
								{
									rlClientId.setBackgroundDrawable(getResources().getDrawable(R.drawable.idfail));
								}
							}
							else
							{
								idTitle.setText(getString(R.string.id_title));
								idText.setText(getString(R.string.id_text));
								ivHelp.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_live_help_white_36dp));
								ivHelp.setOnClickListener(new View.OnClickListener()
								{
									@Override
									public void onClick(final View view)
									{
										showHelp(view);
									}
								});
								if(Common.API_LEVEL >= Build.VERSION_CODES.LOLLIPOP)
								{
									rlClientId.setBackground(getDrawable(R.drawable.freepass));
								}
								else
								{
									rlClientId.setBackgroundDrawable(getResources().getDrawable(R.drawable.freepass));
								}
							}
						}
					}
					else
					{
						rlClientId.setVisibility(RelativeLayout.GONE);
					}
				}
				
				realm.close();
			}
		}
		catch(Exception e)
		{
			Utils.logError(this, getLocalClassName()+":refreshIdZone - Exception:", e);
		}
	}

	public void onCreateNote(final String msgId)
	{
		try
		{
			final Activity activity	= this;
			String txtNote			= "";

			if(StringUtils.isNotEmpty(msgId))
			{
				final Realm realm	= Realm.getDefaultInstance();
				Message message		= realm.where(Message.class).equalTo(Message.KEY_API, msgId).findFirst();

				if(message != null)
				{
					txtNote = message.getMsg();
					new MaterialDialog.Builder(this).title(getString(R.string.enrich_noteheader)).inputType(InputType.TYPE_CLASS_TEXT)
						.positiveText(R.string.enrich_save).cancelable(true).inputRange(0, 160).positiveColor(Color.parseColor(Common.COLOR_COMMENT))
						.input(getString(R.string.enrich_notehint), txtNote, new MaterialDialog.InputCallback()
						{
							@Override
							public void onInput(@NonNull MaterialDialog dialog, CharSequence input)
							{
								if(input != null)
								{
									if(input != "")
									{
										final String comment = input.toString();

										if(StringUtils.isNotEmpty(comment))
										{
											final Realm realm = Realm.getDefaultInstance();
											realm.executeTransactionAsync(new Realm.Transaction()
											{
												@Override
												public void execute(Realm bgRealm)
												{
													Message message = bgRealm.where(Message.class).equalTo(Message.KEY_API, msgId).findFirst();
													message.setMsg(comment);
													message.setCreated(System.currentTimeMillis());
												}
											}, new Realm.Transaction.OnSuccess()
											{
												@Override
												public void onSuccess()
												{
													refresh(false);
												}
											});
											realm.close();
										}
									}
								}
							}
						}).show();
				}

				realm.close();
			}
			else
			{
				new MaterialDialog.Builder(this).title(getString(R.string.enrich_addnoteheader)).inputType(InputType.TYPE_CLASS_TEXT)
					.positiveText(R.string.enrich_save).cancelable(true).inputRange(0, 160).positiveColor(Color.parseColor(Common.COLOR_COMMENT))
					.input(getString(R.string.enrich_notehint), txtNote, new MaterialDialog.InputCallback()
					{
						@Override
						public void onInput(@NonNull MaterialDialog dialog, CharSequence input)
						{
							if(input != null)
							{
								if(input != "")
								{
									final String comment = input.toString();

									if(StringUtils.isNotEmpty(comment))
									{
										final Realm realm = Realm.getDefaultInstance();
										realm.executeTransactionAsync(new Realm.Transaction()
										{
											@Override
											public void execute(Realm bgRealm)
											{
												bgRealm.copyToRealmOrUpdate(MessageHelper.getNewNote(comment, companyId, activity));
											}
										}, new Realm.Transaction.OnSuccess()
										{
											@Override
											public void onSuccess()
											{
												refresh(false);
											}
										});
										realm.close();
									}
								}
							}
						}
					}).show();
			}
		}
		catch(Exception e)
		{
			Utils.logError(this, getLocalClassName()+":onCreateNote - Exception:", e);
		}
	}

	public void createNoteWithPhoto(View view)
	{
		try
		{
			callCamera(null);
		}
		catch(Exception e)
		{
			Utils.logError(this, getLocalClassName()+":createNoteWithPhoto - Exception:", e);
		}
	}

	public void createNote(View view)
	{
		try
		{
			animateFab(view);
			onCreateNote(null);
		}
		catch(Exception e)
		{
			Utils.logError(this, getLocalClassName()+":createNote - Exception:", e);
		}
	}

	public void animateFab(View view)
	{
		try
		{
			if(isFabOpen)
			{
				fabOpen.startAnimation(animRotateBackward);
				fabNote.startAnimation(animClose);
				fabPhoto.startAnimation(animClose);
				fabNote.setClickable(false);
				fabPhoto.setClickable(false);
				isFabOpen = false;
			}
			else
			{
				fabOpen.startAnimation(animRotateForward);
				fabNote.startAnimation(animOpen);
				fabPhoto.startAnimation(animOpen);
				fabNote.setClickable(true);
				fabPhoto.setClickable(true);
				isFabOpen = true;
			}
		}
		catch(Exception e)
		{
			Utils.logError(this, getLocalClassName()+":animateFab - Exception:", e);
		}
	}

	public void attach(String id, String comment, String linkOne, String linkTwo, String linkThree)
	{
		try
		{
			new AttachAsyncTask(this, false, id, comment, linkOne, linkTwo, linkThree, new CallBackListener()
			{
				@Override
				public void invoke()
				{
					runOnUiThread(new Runnable()
					{
						@Override
						public void run()
						{
							refresh(false);
						}
					});
				}
			}).executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
		}
		catch(Exception e)
		{
			Utils.logError(this, getLocalClassName()+":attach - Exception:", e);
		}
	}

	public void callCamera(String id)
	{
		try
		{
			msgId				= id;
			Intent cameraIntent	= new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
			File out			= Environment.getExternalStorageDirectory();

			if(StringUtils.isNotEmpty(id))
			{
				out = new File(out, id);
			}
			else
			{
				out = new File(out, String.valueOf(System.currentTimeMillis()));
			}

			tempUri = Uri.fromFile(out);
			cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, tempUri);
			startActivityForResult(cameraIntent, 0);
		}
		catch(Exception e)
		{
			Utils.logError(this, getLocalClassName()+":callCamera - Exception:", e);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent intent)
	{
		super.onActivityResult(requestCode, resultCode, intent);

		try
		{
			if(resultCode == RESULT_OK)
			{
				//Nueva forma para reducir procesamiento de imagen
				BitmapFactory.Options options	= new BitmapFactory.Options();
				options.inScaled				= false;
				options.inDither				= false;
				options.inPreferredConfig		= Bitmap.Config.ARGB_8888;
				Bitmap bitmap					= BitmapFactory.decodeFile(tempUri.getPath(), options);

				if(bitmap != null)
				{
					Realm realm					= Realm.getDefaultInstance();
					final Activity activity		= this;
					ByteArrayOutputStream baos	= new ByteArrayOutputStream();
					bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
					mDownloadUrl				= null;

					if(StringUtils.isNotEmpty(msgId))
					{
						Message message	= realm.where(Message.class).equalTo(Message.KEY_API, msgId).findFirst();

						if(message != null)
						{
							realm.executeTransactionAsync(new Realm.Transaction()
							{
								@Override
								public void execute(Realm bgRealm)
								{
									Message message	= bgRealm.where(Message.class).equalTo(Message.KEY_API, msgId).findFirst();

									//Actualizamos la uri primero para refrescar la vista mientras se sube la imagen
									if(StringUtils.isNotEmpty(message.getUri()) && StringUtils.isNotEmpty(message.getUriTwo()))
									{
										message.setUriThree(tempUri.toString());
									}
									else
									{
										if(StringUtils.isNotEmpty(message.getUri()))
										{
											message.setUriTwo(tempUri.toString());
										}
										else
										{
											message.setUri(tempUri.toString());
										}
									}
								}
							}, new Realm.Transaction.OnSuccess()
							{
								@Override
								public void onSuccess()
								{
									onUploadResultIntent(null);
									activity.startService(new Intent(activity, MyUploadService.class).putExtra(MyUploadService.EXTRA_FILE_URI, tempUri).putExtra(Common.KEY_ID, msgId)
											.setAction(MyUploadService.ACTION_UPLOAD));
								}
							});
						}
					}
					else
					{
						//Se trata de una nota con imagen
						realm.executeTransactionAsync(new Realm.Transaction()
						{
							@Override
							public void execute(Realm bgRealm)
							{
								Message message = MessageHelper.getNewNote("", companyId, activity);
								message.setUri(tempUri.toString());
								bgRealm.copyToRealmOrUpdate(message);
							}
						}, new Realm.Transaction.OnSuccess()
						{
							@Override
							public void onSuccess()
							{
								onUploadResultIntent(null);
							}
						});
					}

					realm.close();
				}
			}
		}
		catch(Exception e)
		{
			Utils.logError(this, getLocalClassName()+":onActivityResult - Exception:", e);
		}
	}

	private void onUploadResultIntent(Intent intent)
	{
		try
		{
			if(intent != null)
			{
				mDownloadUrl	= intent.getParcelableExtra(MyUploadService.EXTRA_DOWNLOAD_URL);
				tempUri			= intent.getParcelableExtra(MyUploadService.EXTRA_FILE_URI);
			}

			Intent intentRefresh = new Intent(this, CardViewActivity.class);
			intentRefresh.putExtra(Common.KEY_ID, companyId);
			startActivity(intentRefresh);
			finish();
		}
		catch(Exception e)
		{
			Utils.logError(this, getLocalClassName()+":onUploadResultIntent - Exception:", e);
		}
	}

	public void goToSettings()
	{
		try
		{
			Realm realm		= Realm.getDefaultInstance();
			Intent intent	= new Intent(getApplicationContext(), LandingActivity.class); //Modificación para unificar en pantalla Landing
			suscription		= realm.where(Suscription.class).equalTo(Suscription.KEY_API, companyId).findFirst();
			intent.putExtra(Common.KEY_ID, companyId);
			intent.putExtra(Common.KEY_SECTION, "card");
			startActivity(intent);
			finish();
		}
		catch(Exception e)
		{
			Utils.logError(this, getLocalClassName()+":goToSettings - Exception:", e);
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		try
		{
			MenuInflater inflater = getMenuInflater();
			Migration.getDB(CardViewActivity.this);
			Realm realm	= Realm.getDefaultInstance();
			suscription	= realm.where(Suscription.class).equalTo(Suscription.KEY_API, companyId).findFirst();
			
			if(suscription.getType() == Suscription.TYPE_FOLDER)
			{
				inflater.inflate(R.menu.menu_folder, menu);
			}
			else
			{
				inflater.inflate(R.menu.menu_cardview, menu);
			}
			
			realm.close();
		}
		catch(Exception e)
		{
			Utils.logError(this, getLocalClassName()+":onCreateOptionsMenu - Exception:", e);
		}

		return super.onCreateOptionsMenu(menu);
	}

	/**
	 * Agregado para modificar acción de silenciar según si está silenciado o no
	 * @param menu
	 * @return
	 */
	@Override
	public boolean onPrepareOptionsMenu(Menu menu)
	{
		try
		{
			if(menu != null)
			{
				Migration.getDB(CardViewActivity.this);
				Realm realm	= Realm.getDefaultInstance();
				suscription	= realm.where(Suscription.class).equalTo(Suscription.KEY_API, companyId).findFirst();
				
				if(suscription.getType() != Suscription.TYPE_FOLDER)
				{
					if(menu.getItem(1) != null)
					{
						MenuItem item	= menu.getItem(1);
						
						if(suscription != null)
						{
							if(suscription.getSilenced() == Common.BOOL_YES)
							{
								item.setTitle(R.string.activate_notif);
							}
							else
							{
								item.setTitle(R.string.silence);
							}
						}
						else
						{
							item.setTitle(R.string.silence);
						}
					}
					
					if(menu.getItem(3) != null)
					{
						MenuItem itemSuscribe	= menu.getItem(3);
						
						if(suscription != null)
						{
							if(suscription.getFollower() == Common.BOOL_YES)
							{
								itemSuscribe.setTitle(R.string.landing_unsuscribe);
							}
							else
							{
								itemSuscribe.setTitle(R.string.landing_suscribe);
							}
						}
						else
						{
							itemSuscribe.setTitle(R.string.landing_suscribe);
						}
					}
				}
				
				realm.close();
			}
		}
		catch(Exception e)
		{
			Utils.logError(this, getLocalClassName()+":onPrepareOptionsMenu - Exception:", e);
		}

		return super.onPrepareOptionsMenu(menu);
	}

	public void showOptionsCard(final int position, final String msgId)
	{
		try
		{
			int items		= R.array.optionsCard;
			Realm realm		= Realm.getDefaultInstance();
			Message message	= realm.where(Message.class).equalTo(Message.KEY_API, msgId).findFirst();

			if(message != null)
			{
				if(message.getKind() == Message.KIND_TWITTER)
				{
					items = R.array.optionsCardSocial;
				}
				else
				{
					if(message.getKind() == Message.KIND_NOTE)
					{
						items = R.array.optionsNote;
					}
				}
			}

			list = new MaterialDialog.Builder(this)
					.items(items)
					.itemsCallback(new MaterialDialog.ListCallback()
					{
						@Override
						public void onSelection(MaterialDialog dialog, View view, int which, CharSequence text)
						{
							dispatchMenu(which, notifications.get(position).getMsgId());
						}
					}).show();
		}
		catch(Exception e)
		{
			Utils.logError(this, getLocalClassName()+":showOptionsCard - Exception:", e);
		}
	}

	public void becomeGray(View view)
	{
		try
		{
			Realm realm	= Realm.getDefaultInstance();
			suscription	= realm.where(Suscription.class).equalTo(Suscription.KEY_API, companyId).findFirst();
			realm.executeTransaction(new Realm.Transaction()
			{
				@Override
				public void execute(Realm realm)
				{
					suscription.setGray(Common.BOOL_YES);
				}
			});
			Utils.hideViewWithFade(cardSuscribe, this);
			txtTitle.setTextColor(Utils.adjustAlpha(colorTitle, Common.ALPHA_FOR_BLOCKS));
			txtSubTitleCollapsed.setTextColor(Utils.adjustAlpha(colorSubTitle, Common.ALPHA_FOR_BLOCKS));
			toolBar.setBackgroundColor(Color.parseColor(Common.COLOR_BLOCKED));
			Utils.tintColorScreen(this, Common.COLOR_BLOCKED);
			RelativeLayout.LayoutParams p = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

			if(Common.API_LEVEL >= Build.VERSION_CODES.JELLY_BEAN_MR1)
			{
				p.removeRule(RelativeLayout.BELOW);
			}

			rcwCard.setLayoutParams(p);
			realm.close();
		}
		catch(Exception e)
		{
			Utils.logError(this, getLocalClassName()+":becomeGray - Exception:", e);
		}
	}

	public void continueCompany(View view)
	{
		try
		{
			Realm realm	= Realm.getDefaultInstance();
			suscription	= realm.where(Suscription.class).equalTo(Suscription.KEY_API, companyId).findFirst();
			realm.executeTransaction(new Realm.Transaction()
			{
				@Override
				public void execute(Realm realm)
				{
					suscription.setReceive(Common.BOOL_YES);
				}
			});
			Utils.hideViewWithFade(cardPayout, this);
			RelativeLayout.LayoutParams p = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

			if(Common.API_LEVEL >= Build.VERSION_CODES.JELLY_BEAN_MR1)
			{
				p.removeRule(RelativeLayout.BELOW);
			}

			rcwCard.setLayoutParams(p);
		}
		catch(Exception e)
		{
			Utils.logError(this, getLocalClassName()+":continueCompany - Exception:", e);
		}
	}

	public void blockCompany(View view)
	{
		try
		{
			//Agregado para capturar evento en Google Analytics
			GoogleAnalytics.getInstance(this).newTracker(Common.HASH_GOOGLEANALYTICS).send(	new HitBuilders.EventBuilder().setCategory("Company").setAction("BloquearInCompany")
																							.setLabel("AccionUser").build());
			Realm realm	= Realm.getDefaultInstance();
			suscription	= realm.where(Suscription.class).equalTo(Suscription.KEY_API, companyId).findFirst();
			HomeActivity.modifySubscriptions(CardViewActivity.this, Common.BOOL_NO, true, companyId, false);
			Utils.hideViewWithFade(cardPayout, this);
			Utils.hideViewWithFade(cardSuscribe, this);
			RelativeLayout.LayoutParams p = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

			if(Common.API_LEVEL >= Build.VERSION_CODES.JELLY_BEAN_MR1)
			{
				p.removeRule(RelativeLayout.BELOW);
			}

			rcwCard.setLayoutParams(p);
			Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
			intent.putExtra(Common.KEY_ID, companyId);
			intent.putExtra(Suscription.KEY_BLOCKED, suscription.getBlocked());
			intent.putExtra(Common.KEY_REFRESH, true);
			startActivity(intent);
			finish();
		}
		catch(Exception e)
		{
			Utils.logError(this, getLocalClassName()+":blockCompany - Exception:", e);
		}
	}

	public void dispatchMenu(int position, final String msgId)
	{
		try
		{
			final Realm realm			= Realm.getDefaultInstance();
			suscription					= realm.where(Suscription.class).equalTo(Suscription.KEY_API, companyId).findFirst();
			Snackbar snackBar			= null;
			final Message notification	= realm.where(Message.class).equalTo(Message.KEY_API, msgId).findFirst();
			final Activity activity		= this;

			switch(position)
			{
				case CardAdapter.OPTION_SHARE:
					//Agregado para capturar evento en Google Analytics
					GoogleAnalytics.getInstance(this).newTracker(Common.HASH_GOOGLEANALYTICS).send(new HitBuilders.EventBuilder().setCategory("Mensajes").setAction("Compartir")
						.setLabel("Accion_user").build());
					Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
					sharingIntent.setType("text/plain");
					sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, notification.getType());
					sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, notification.getMsg());
					startActivity(Intent.createChooser(sharingIntent, getResources().getString(R.string.share)));
				break;

				case CardAdapter.OPTION_BLOCK:
					//Diferenciamos si se trata de una nota para ir al editar
					if(notification.getKind() == Message.KIND_NOTE)
					{
						onCreateNote(notification.getMsgId());
					}
					else
					{
						//Agregado para capturar evento en Google Analytics, se incorpora la opción "no quiero ver más esto" que hace lo mismo que marcar como spam por el momento
						GoogleAnalytics.getInstance(this).newTracker(Common.HASH_GOOGLEANALYTICS).send(	new HitBuilders.EventBuilder().setCategory("Mensajes").setAction("Marcarspam")
								.setLabel("Accion_user").build());
						realm.executeTransaction(new Realm.Transaction()
						{
							@Override
							public void execute(Realm realm)
							{
								notification.setStatus(Message.STATUS_SPAM);
							}
						});
						new ConfirmReadingAsyncTask(false, companyId, notification.getMsgId(), Message.STATUS_SPAM, this).executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);

						snackBar = Snackbar.make(Clayout, getString(R.string.snack_msg_spam), Snackbar.LENGTH_LONG).setAction(getString(R.string.undo), new View.OnClickListener()
						{
							@Override
							public void onClick(View v)
							{
								Realm realm = Realm.getDefaultInstance();
								realm.executeTransaction(new Realm.Transaction()
								{
									@Override
									public void execute(Realm realm)
									{
										notification.setStatus(Message.STATUS_READ);
									}
								});
								refresh(false);
								new ConfirmReadingAsyncTask(false, companyId, notification.getMsgId(), Message.STATUS_READ, activity).executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
							}
						});
					}
				break;

				case CardAdapter.OPTION_DISMISS:
					//Agregado para capturar evento en Google Analytics, se incorpora la opción "no quiero ver más esto" que hace lo mismo que marcar como spam por el momento
					GoogleAnalytics.getInstance(this).newTracker(Common.HASH_GOOGLEANALYTICS).send(	new HitBuilders.EventBuilder().setCategory("Social").setAction("Marcarspam")
																									.setLabel("Accion_user").build());
					realm.executeTransaction(new Realm.Transaction()
					{
						@Override
						public void execute(Realm realm)
						{
							notification.setStatus(Message.STATUS_SPAM);
						}
					});
					new ConfirmReadingAsyncTask(false, companyId, notification.getMsgId(), Message.STATUS_SPAM, this).executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);

					snackBar = Snackbar.make(Clayout, getString(R.string.snack_msg_spam), Snackbar.LENGTH_LONG).setAction(getString(R.string.undo), new View.OnClickListener()
					{
						@Override
						public void onClick(View v)
						{
							Realm realm = Realm.getDefaultInstance();
							realm.executeTransaction(new Realm.Transaction()
							{
								@Override
								public void execute(Realm realm)
								{
									notification.setStatus(Message.STATUS_READ);
								}
							});
							refresh(false);
							new ConfirmReadingAsyncTask(false, companyId, notification.getMsgId(), Message.STATUS_READ, activity).executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
						}
					});
					break;

				case CardAdapter.OPTION_DELETE:
					//Agregado para capturar evento en Google Analytics
					GoogleAnalytics.getInstance(this).newTracker(Common.HASH_GOOGLEANALYTICS).send(	new HitBuilders.EventBuilder().setCategory("Mensajes").setAction("Borrar")
																									.setLabel("Accion_user").build());
					realm.executeTransaction(new Realm.Transaction()
					{
						@Override
						public void execute(Realm realm)
						{
							notification.setDeleted(Common.BOOL_YES);
						}
					});
					snackBar = Snackbar.make(Clayout, getString(R.string.snack_msg_deleted), Snackbar.LENGTH_LONG).setAction(getString(R.string.undo), new View.OnClickListener()
					{
						@Override
						public void onClick(View v)
						{
							Realm realm = Realm.getDefaultInstance();
							realm.executeTransaction(new Realm.Transaction()
							{
								@Override
								public void execute(Realm realm)
								{
									notification.setDeleted(Common.BOOL_NO);
								}
							});
							refresh(false);
						}
					});
				break;
			}

			refresh(false);
			list.dismiss();
			Utils.setStyleSnackBar(snackBar, getApplicationContext());
		}
		catch(Exception e)
		{
			Utils.logError(this, getLocalClassName()+":dispatchMenu - Exception:", e);
		}
	}

	private void refresh(final boolean erase)
	{
		try
		{
			final Activity context	= this;
			Handler handler			= new android.os.Handler();
			handler.post(new Runnable()
			{
				public void run()
				{
					if(StringUtils.isNotEmpty(companyId))
					{
						Realm realm				= Realm.getDefaultInstance();
						Suscription suscription	= realm.where(Suscription.class).equalTo(Suscription.KEY_API, companyId).findFirst();

						if(erase)
						{
							notifications = null;
						}
						else
						{
							if(suscription != null)
							{
								if(StringUtils.isNotEmpty(companyId))
								{
									notifications = realm.where(Message.class).notEqualTo(Message.KEY_DELETED, Common.BOOL_YES).notEqualTo(Common.KEY_STATUS, Message.STATUS_SPAM)
														.equalTo(Suscription.KEY_API, suscription.getCompanyId()).findAllSorted(Message.KEY_CREATED, Sort.DESCENDING);
								}
							}
						}

						//Modificación para implementar placeholder picture en lugar de card vacía
						if(rcwCard != null)
						{
							if(rlEmpty != null)
							{
								if(suscription != null)
								{
									boolean isSubscribe = true;

									if(suscription.getFollower() == Common.BOOL_NO && suscription.getGray() == Common.BOOL_NO)
									{
										Utils.showViewWithFade(cardSuscribe, context);
										isSubscribe = false;
									}
									else
									{
										Utils.hideViewWithFade(cardSuscribe, context);
									}

									if(notifications != null)
									{
										if(notifications.size() > 0)
										{
											if(SuscriptionHelper.isRevenue(suscription.getCompanyId(), context) && isSubscribe)
											{
												if(suscription.getReceive() != Common.BOOL_YES)
												{
													Utils.showViewWithFade(cardPayout, context);
												}
												else
												{
													Utils.hideViewWithFade(cardPayout, context);
												}
											}
											else
											{
												Utils.hideViewWithFade(cardPayout, context);
											}

											mAdapter = new CardAdapter(CardViewActivity.this, companyId);
											rcwCard.setAdapter(mAdapter);
											rlEmpty.setVisibility(RelativeLayout.GONE);
											Utils.showViewWithFade(rcwCard, context);
										}
										else
										{
											Utils.showViewWithFade(rlEmpty, context);
										}
									}
									else
									{
										Utils.showViewWithFade(rlEmpty, context);
									}
								}
							}
						}
					}
				}
			});
		}
		catch(Exception e)
		{
			Utils.logError(this, getLocalClassName()+":refresh - Exception:", e);
		}
	}

	public void suscribeCompany(View v)
	{
		try
		{
			//Agregado para capturar evento en Google Analytics
			GoogleAnalytics.getInstance(this).newTracker(Common.HASH_GOOGLEANALYTICS).send(	new HitBuilders.EventBuilder().setCategory("Company").setAction("AgregarInCompany")
																							.setLabel("AccionUser").build());
			Realm realm	= Realm.getDefaultInstance();
			suscription	= realm.where(Suscription.class).equalTo(Suscription.KEY_API, companyId).findFirst();
			HomeActivity.modifySubscriptions(CardViewActivity.this, Common.BOOL_YES, false, companyId, false);
			Utils.hideViewWithFade(cardPayout, this);
			Utils.hideViewWithFade(cardSuscribe, this);
			txtTitle.setTextColor(colorTitle);
			txtSubTitleCollapsed.setTextColor(colorSubTitle);
			toolBar.setBackgroundColor(Color.parseColor(suscription.getColorHex()));
			Utils.tintColorScreen(this, suscription.getColorHex());
			RelativeLayout.LayoutParams p = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

			if(Common.API_LEVEL >= Build.VERSION_CODES.JELLY_BEAN_MR1)
			{
				p.removeRule(RelativeLayout.BELOW);
			}

			rcwCard.setLayoutParams(p);
		}
		catch(Exception e)
		{
			Utils.logError(this, getLocalClassName()+":suscribeCompany - Exception:", e);
		}
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		try
		{
			hideSoftKeyboard();
			Realm realm				= Realm.getDefaultInstance();
			suscription				= realm.where(Suscription.class).equalTo(Suscription.KEY_API, companyId).findFirst();
			final Activity activity	= this;

			if(suscription != null)
			{
				Snackbar snackBar	= null;

				if(item.toString().equals(getString(R.string.silence)))
				{
					//Agregado para capturar evento en Google Analytics
					GoogleAnalytics.getInstance(this).newTracker(Common.HASH_GOOGLEANALYTICS).send(new HitBuilders.EventBuilder().setCategory("Company")
						.setAction("SilenciarInCompany").setLabel("AccionUser").build());
					realm.executeTransaction(new Realm.Transaction()
					{
						@Override
						public void execute(Realm realm)
						{
							suscription.setSilenced(Utils.reverseBool(suscription.getSilenced()));
						}
					});
					refresh(false);
					snackBar = Snackbar.make(Clayout, getString(R.string.snack_silence), Snackbar.LENGTH_LONG).setAction(getString(R.string.undo), new View.OnClickListener()
					{
						@Override
						public void onClick(View v)
						{
							Realm realm	= Realm.getDefaultInstance();
							suscription	= realm.where(Suscription.class).equalTo(Suscription.KEY_API, companyId).findFirst();
							realm.executeTransaction(new Realm.Transaction()
							{
								@Override
								public void execute(Realm realm)
								{
									suscription.setSilenced(Utils.reverseBool(suscription.getSilenced()));
								}
							});
							refresh(false);
						}
					});
				}

				//Agregado para desactivar silencio
				if(item.toString().equals(getString(R.string.activate_notif)))
				{
					realm.executeTransaction(new Realm.Transaction()
					{
						@Override
						public void execute(Realm realm)
						{
							suscription.setSilenced(Utils.reverseBool(suscription.getSilenced()));
						}
					});
					refresh(false);
					snackBar = Snackbar.make(Clayout, getString(R.string.snack_unsilence), Snackbar.LENGTH_LONG).setAction(getString(R.string.undo), new View.OnClickListener()
					{
						@Override
						public void onClick(View v)
						{
							Realm realm	= Realm.getDefaultInstance();
							suscription	= realm.where(Suscription.class).equalTo(Suscription.KEY_API, companyId).findFirst();
							realm.executeTransaction(new Realm.Transaction()
							{
								@Override
								public void execute(Realm realm)
								{
									suscription.setSilenced(Utils.reverseBool(suscription.getSilenced()));
								}
							});
							refresh(false);
						}
					});
				}

				if(item.toString().equals(getString(R.string.empty_messages)))
				{
					//Agregado para capturar evento en Google Analytics
					GoogleAnalytics.getInstance(this).newTracker(Common.HASH_GOOGLEANALYTICS).send(	new HitBuilders.EventBuilder().setCategory("Company").setAction("VaciarInCompany")
																									.setLabel("AccionUser").build());

					//Modificación para ejecutar proceso en background
					if(StringUtils.isNotEmpty(companyId))
					{
						MessageHelper.emptyCompany(companyId, Common.BOOL_YES, activity);
					}

					refresh(true);
					snackBar = Snackbar.make(Clayout, getString(R.string.snack_empty), Snackbar.LENGTH_LONG).setAction(getString(R.string.undo), new View.OnClickListener()
					{
						@Override
						public void onClick(View v)
						{
							//Modificación para ejecutar proceso en background
							if(StringUtils.isNotEmpty(companyId))
							{
								MessageHelper.emptyCompany(companyId, Common.BOOL_NO, activity);
							}

							refresh(false);
						}
					});
				}

				//Se reemplaza Bloquear por Desuscribir
				if(item.toString().equals(getString(R.string.landing_unsuscribe)))
				{
					//Agregado para capturar evento en Google Analytics
					GoogleAnalytics.getInstance(this).newTracker(Common.HASH_GOOGLEANALYTICS).send(	new HitBuilders.EventBuilder().setCategory("Company").setAction("BloquearInCompany")
																									.setLabel("AccionUser").build());
					HomeActivity.modifySubscriptions(CardViewActivity.this, Common.BOOL_NO, false, companyId, false);
					Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
					intent.putExtra(Common.KEY_ID, companyId);
					intent.putExtra(Suscription.KEY_BLOCKED, Common.BOOL_YES);
					intent.putExtra(Common.KEY_REFRESH, false);
					startActivity(intent);
					finish();
				}

				//Al igual que Silenciar/Activar, esta es la opción para suscribir
				if(item.toString().equals(getString(R.string.landing_suscribe)))
				{
					//Agregado para capturar evento en Google Analytics
					GoogleAnalytics.getInstance(this).newTracker(Common.HASH_GOOGLEANALYTICS).send(	new HitBuilders.EventBuilder().setCategory("Company").setAction("AgregarInCompany")
																									.setLabel("AccionUser").build());
					HomeActivity.modifySubscriptions(CardViewActivity.this, Common.BOOL_YES, false, companyId, false);
					txtTitle.setTextColor(colorTitle);
					txtSubTitleCollapsed.setTextColor(colorSubTitle);
					toolBar.setBackgroundColor(Color.parseColor(suscription.getColorHex()));
					Utils.tintColorScreen(this, suscription.getColorHex());
				}

				//Agregado para redirigir a la landing de la company
				if(item.toString().equals(getString(R.string.settings)))
				{
					goToSettings();
				}
				
				//Agregado para permitir cambiar nombre de la carpeta desde las opciones
				if(item.toString().equals(getString(R.string.folder_header)))
				{
					new MaterialDialog.Builder(this).title(getString(R.string.folder_header)).inputType(InputType.TYPE_CLASS_TEXT)
						.positiveText(R.string.enrich_save).cancelable(true).inputRange(0, 20).positiveColor(Color.parseColor(Common.COLOR_COMMENT))
						.input(getString(R.string.folder_hint), suscription.getName(), new MaterialDialog.InputCallback()
						{
							@Override
							public void onInput(@NonNull MaterialDialog dialog, CharSequence input)
							{
								if(input != null)
								{
									if(input != "")
									{
										final String name = input.toString().trim();
										
										if(StringUtils.isNotEmpty(name))
										{
											Realm realm = Realm.getDefaultInstance();
											realm.executeTransactionAsync(new Realm.Transaction()
											{
												@Override
												public void execute(Realm bgRealm)
												{
													Suscription suscription1 = bgRealm.where(Suscription.class).equalTo(Suscription.KEY_API, companyId).findFirst();
													suscription1.setName(name);
												}
											}, new Realm.Transaction.OnSuccess()
											{
												@Override
												public void onSuccess()
												{
													Handler handler = new android.os.Handler();
													handler.post(new Runnable()
													{
														public void run()
														{
															txtTitle.setText(name);
														}
													});
												}
											});
											
											realm.close();
										}
									}
								}
							}
						}).show();
				}

				Utils.setStyleSnackBar(snackBar, getApplicationContext());
			}
		}
		catch(Exception e)
		{
			Utils.logError(this, getLocalClassName()+":onOptionsItemSelected - Exception:", e);
		}

		return super.onOptionsItemSelected(item);
	}

	/**
	 * Agregado para prevenir dos tareas dentro de la pila para esta activity
	 */
	@Override
	public void onBackPressed()
	{
		try
		{
			hideSoftKeyboard();
			Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			intent.putExtra(Common.KEY_REFRESH, false);
			startActivity(intent);
			finish();
		}
		catch(Exception e)
		{
			Utils.logError(this, getLocalClassName()+":onBackPressed - Exception:", e);
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
			Utils.logError(this, getLocalClassName()+":hideSoftKeyboard - Exception:", e);
		}
	}

	/**
	 * Agregado para controlar estado del teclado
	 */
	protected void onResume()
	{
		super.onResume();

		try
		{
			if(Utils.checkSesion(this, Common.ANOTHER_SCREEN))
			{
				refresh(false);
			}

			Window window = getWindow();

			// Overriding the soft input mode of the Window so that the Send and Cancel buttons appear above the soft keyboard when either EditText field gains focus.
			// We cache the mode in order to set it back to the original value when the Fragment is paused.
			originalSoftInputMode = window.getAttributes().softInputMode;
			window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
		}
		catch(Exception e)
		{
			Utils.logError(this, getLocalClassName()+":onResume - Exception:", e);
		}
	}

	@Override
	public void onStart()
	{
		super.onStart();
		try
		{
			LocalBroadcastManager manager = LocalBroadcastManager.getInstance(this);
			manager.registerReceiver(mBroadcastReceiver, MyDownloadService.getIntentFilter());
			manager.registerReceiver(mBroadcastReceiver, MyUploadService.getIntentFilter());
		}
		catch(Exception e)
		{
			Utils.logError(this, getLocalClassName()+":onStart - Exception:", e);
		}
	}

	@Override
	public void onStop()
	{
		super.onStop();
		// Unregister download receiver
		try
		{
			LocalBroadcastManager.getInstance(this).unregisterReceiver(mBroadcastReceiver);
		}
		catch(Exception e)
		{
			Utils.logError(this, getLocalClassName()+":onStop - Exception:", e);
		}
	}
}