package com.example.mymp3cutter;

import android.content.Intent;
import android.net.MailTo;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

public class SettingsActivity extends AppCompatActivity {

	private Toolbar toolbar;
	private ListView listView;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);
		toolbar=(Toolbar)findViewById(R.id.toolbar_settings);
		listView=(ListView)findViewById(R.id.listViewSettings);
		setSupportActionBar(toolbar);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				switch (position){
					case 0:
						Toast.makeText(SettingsActivity.this, getResources().getString(R.string.toast_msg_not_implemented), Toast.LENGTH_SHORT).show();
						break;
					case 1:
						Toast.makeText(SettingsActivity.this, getResources().getString(R.string.toast_msg_not_implemented), Toast.LENGTH_SHORT).show();
						break;
					case 2:
						Intent intent=new Intent(Intent.ACTION_SENDTO);
						intent.setData(Uri.fromParts("mailto",getResources().getString(R.string.feedback_email_id),null));
						startActivity(intent);
						break;
					case 3:
						Intent intent1=new Intent(Intent.ACTION_SEND);
						intent1.setType("text/plain");
						intent1.putExtra(Intent.EXTRA_TEXT,getResources().getString(R.string.app_link));
						startActivity(intent1);
						break;
					case 4:
						Intent intent2=new Intent(SettingsActivity.this,AboutActivity.class);
						startActivity(intent2);
				}
			}
		});
	}
}
