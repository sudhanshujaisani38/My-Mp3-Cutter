package com.example.mymp3cutter;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

public class RequestPermissionActivity extends Activity {

	private ImageView imageViewCross;
	private Button buttonEnableNow;
	private static final int REQ_CODE=345;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_request_permission);
		buttonEnableNow=(Button)findViewById(R.id.btn_enable_now);
		imageViewCross=(ImageView)findViewById(R.id.imageViewCross);
		imageViewCross.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		buttonEnableNow.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				ActivityCompat.requestPermissions(RequestPermissionActivity.this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_EXTERNAL_STORAGE},REQ_CODE);
			}
		});
	}

	@Override
	public void onRequestPermissionsResult(int requestCode,  String[] permissions,  int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		if(requestCode==REQ_CODE){
			if(grantResults.length>0 && grantResults[0]== PackageManager.PERMISSION_GRANTED && grantResults[1]==PackageManager.PERMISSION_GRANTED){
				Intent intent = new Intent(RequestPermissionActivity.this,MainActivity.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
				startActivity(intent);
			}
		}
	}
}
