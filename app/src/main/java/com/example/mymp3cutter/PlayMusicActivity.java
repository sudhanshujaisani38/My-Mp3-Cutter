package com.example.mymp3cutter;

import android.Manifest;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.AlarmManagerCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;

import java.io.File;
import java.io.IOException;

public class PlayMusicActivity extends AppCompatActivity {

	private Toolbar toolbar;
	private AdView adView;
	private TextView textViewFileName,textViewDuration;
	private LinearLayout linearLayoutPlayPause;
	private ImageView imageViewPlayPause;
	private Button buttonRename,buttonDelete;
	private MediaPlayer mediaPlayer;
	private Uri uri;
	private String filePath;
	private File file;
	private ContentValues contentValues;
	private LinearLayout linearLayoutRingtone,linearLayoutContacts,linearLayoutNotifications,linearLayoutAlarm;
	private static final int CODE_RINGTONE=101,CODE_NOTIFY=102,CODE_ALARM=103,CODE_CONTACT=104;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_play_music);
		toolbar=(Toolbar)findViewById(R.id.toolbar_play_music);
		textViewFileName=(TextView)findViewById(R.id.textViewFileName);
		linearLayoutPlayPause=(LinearLayout)findViewById(R.id.linearLayoutPlayPause);
		imageViewPlayPause=(ImageView)findViewById(R.id.iv_PlayPause);
		textViewDuration=(TextView)findViewById(R.id.textViewDuration);
		buttonRename=(Button)findViewById(R.id.button_rename);
		buttonDelete=(Button)findViewById(R.id.button_delete);
		linearLayoutRingtone=(LinearLayout)findViewById(R.id.ringtone);
		linearLayoutAlarm=(LinearLayout)findViewById(R.id.alarm);
		linearLayoutContacts=(LinearLayout)findViewById(R.id.contacts);
		linearLayoutNotifications=(LinearLayout)findViewById(R.id.notification);
		adView=(AdView)findViewById(R.id.adViewPlayMusic);
		setSupportActionBar(toolbar);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		final String fileName=getIntent().getStringExtra(getResources().getString(R.string.music_file_name));
		textViewFileName.setText(fileName);
		uri=getIntent().getParcelableExtra(getResources().getString(R.string.selected_song_uri));

		filePath= Environment.getExternalStorageDirectory().getAbsolutePath()+"/Music/MyMp3Cutter/"+fileName;
		file=new File(filePath);
		contentValues=new ContentValues();
		contentValues.put(MediaStore.MediaColumns.DATA,file.getAbsolutePath());
		contentValues.put(MediaStore.MediaColumns.TITLE,fileName);
		contentValues.put(MediaStore.MediaColumns.MIME_TYPE,"audio/ogg");
		contentValues.put(MediaStore.Audio.Media.ARTIST,getResources().getString(R.string.app_name));
		contentValues.put(MediaStore.Audio.Media.IS_RINGTONE,true);
		contentValues.put(MediaStore.Audio.Media.IS_ALARM,true);
		contentValues.put(MediaStore.Audio.Media.IS_MUSIC,true);
		contentValues.put(MediaStore.Audio.Media.IS_RINGTONE,true);

		linearLayoutPlayPause.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(mediaPlayer.isPlaying()){
					mediaPlayer.pause();
					imageViewPlayPause.setImageResource(R.drawable.icon_play);

				}else{
					mediaPlayer.start();
					imageViewPlayPause.setImageResource(R.drawable.icon_pause);
				}
			}
		});
		buttonRename.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				final AlertDialog.Builder builder=new AlertDialog.Builder(PlayMusicActivity.this);
				builder.setTitle(getResources().getString(R.string.rename_file));
				final EditText editText=new EditText(PlayMusicActivity.this);
				editText.setText(fileName);
				builder.setView(editText);
				builder.setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {

						File newFile=new File(file.getParent()+"/"+editText.getText().toString());
						file.renameTo(newFile);
					}
				});
				builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});
				builder.create().show();
			}

		});
		buttonDelete.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				AlertDialog.Builder builder=new AlertDialog.Builder(PlayMusicActivity.this);
				builder.setMessage(R.string.are_you_sure);
				builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						file.delete();
						Intent intent=new Intent(PlayMusicActivity.this,MainActivity.class);
						intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
						startActivity(intent);
					}
				});
				builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});
				builder.create().show();
			}
		});
		linearLayoutContacts.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent =new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
				startActivityForResult(intent,CODE_CONTACT);
			}
		});
		linearLayoutAlarm.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				setRingtone(RingtoneManager.TYPE_ALARM);
			}
		});
		linearLayoutNotifications.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				setRingtone(RingtoneManager.TYPE_NOTIFICATION);
			}
		});
		linearLayoutRingtone.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				setRingtone(RingtoneManager.TYPE_RINGTONE);
			}
		});
	}
	void setRingtone(int type){
		Uri uri=MediaStore.Audio.Media.getContentUriForPath(file.getAbsolutePath());
		getContentResolver().delete(uri, MediaStore.MediaColumns.DATA+"=\""+file.getAbsolutePath()+"\"",null);
		Uri newUri1=getContentResolver().insert(uri,contentValues);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			if(Settings.System.canWrite(PlayMusicActivity.this)){

				RingtoneManager.setActualDefaultRingtoneUri(PlayMusicActivity.this,type, newUri1);
				Toast.makeText(this, getResources().getString(R.string.operation_success), Toast.LENGTH_SHORT).show();
			}
			else{
				Intent intent=new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
				startActivity(intent);
			}
		}
	}
	@Override
	protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode){
			case CODE_CONTACT:
				if(resultCode==RESULT_OK){
					Uri contactUri = data.getData();
					ContentValues contentValues=new ContentValues();
					contentValues.put(ContactsContract.Contacts.CUSTOM_RINGTONE,uri.toString());
					int rowsUpdated=getContentResolver().update(contactUri,contentValues,null,null);
					Toast.makeText(this, rowsUpdated+" contact updated", Toast.LENGTH_SHORT).show();
				}
		}
	}

	@Override
	protected void onStart() {
		super.onStart();
		AdRequest adRequest=new AdRequest.Builder().build();
		adView.loadAd(adRequest);
		mediaPlayer=new MediaPlayer();
		mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
			@Override
			public void onCompletion(MediaPlayer mp) {
				imageViewPlayPause.setImageResource(R.drawable.icon_play);
			}
		});
		try {
			mediaPlayer.setDataSource(filePath);
			mediaPlayer.prepare();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void onStop() {
		super.onStop();
//		mediaPlayer.stop();
		mediaPlayer.reset();
		mediaPlayer.release();
	}
}
