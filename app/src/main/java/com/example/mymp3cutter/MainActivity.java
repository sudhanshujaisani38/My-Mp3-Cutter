package com.example.mymp3cutter;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;

public class MainActivity extends Activity {

	private static final int REQ_CODE=346;
	private AdView adView;
	private GridLayout gridLayout;
	private LinearLayout trimAudio,mergeAudio,mixAudio,contactsRingtone,myAudio,settings;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		trimAudio=(LinearLayout)findViewById(R.id.trim_audio);
		mergeAudio=(LinearLayout)findViewById(R.id.merge_audio);
		mixAudio=(LinearLayout)findViewById(R.id.mix_audio);
		contactsRingtone=(LinearLayout)findViewById(R.id.contacts_ringtone);
		myAudio=(LinearLayout)findViewById(R.id.my_audio);
		adView=(AdView)findViewById(R.id.adViewMainActivity);
		settings=(LinearLayout)findViewById(R.id.settings);
		AdRequest adRequest=new AdRequest.Builder().build();
		adView.loadAd(adRequest);
		Toast.makeText(this, "main activity running", Toast.LENGTH_SHORT).show();
		trimAudio.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent=new Intent(MainActivity.this,TrimAudioActivity.class);
				startActivity(intent);
			}
		});

		mergeAudio.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent=new Intent(MainActivity.this,MergeAudioActivity.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(intent);
			}
		});

		mixAudio.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				AlertDialog.Builder builder=new AlertDialog.Builder(MainActivity.this);
				builder.setTitle(getResources().getString(R.string.release_function_limit));
				builder.setIcon(R.drawable.icon_scissor);
				builder.setItems(R.array.mix_audio_item_list, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						Toast.makeText(MainActivity.this, "Yet to be implemented:"+which, Toast.LENGTH_SHORT).show();
					}
				});
				builder.create().show();
			}
		});

		contactsRingtone.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_CONTACTS)== PackageManager.PERMISSION_GRANTED){
					Intent intent=new Intent(MainActivity.this,ContactsRingtoneActivity.class);
					startActivity(intent);
				}else{
					ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.READ_CONTACTS,Manifest.permission.WRITE_CONTACTS},REQ_CODE);
				}
			}
		});

		myAudio.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent=new Intent(MainActivity.this,MyAudioActivity.class);
				startActivity(intent);
			}
		});

		settings.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent=new Intent(MainActivity.this,SettingsActivity.class);
				startActivity(intent);
			}
		});
	}
	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		if(requestCode==REQ_CODE){
			if(grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED && grantResults[1]==PackageManager.PERMISSION_GRANTED){
				Intent intent=new Intent(MainActivity.this,ContactsRingtoneActivity.class);
				startActivity(intent);
			}
		}
	}
}
