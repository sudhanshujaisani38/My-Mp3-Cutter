package com.example.mymp3cutter;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.ContactsContract;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;

public class ContactsRingtoneActivity extends AppCompatActivity {

	private Cursor cursor;
	private ListView listView;
	private Toolbar toolbar;
	private static final int REQ_CODE=347;
	private static long CONTACT_ID;
	private static MediaPlayer mediaPlayer;
	private int prevTrack=-1;
	private View prevView=null;
	private int currentTrack=-1;
	private ContactsRingtoneAdapter contactsRingtoneAdapter;
	private int playingSongPosition =-1;
	private boolean isCallExplicit=false;
	private static ArrayList<MContact> contactArrayList=new ArrayList<>();
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_contacts_ringtone);
		listView=(ListView)findViewById(R.id.listViewContacts);
		toolbar=(Toolbar)findViewById(R.id.toolbar_contacts_ringtone);
		setSupportActionBar(toolbar);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
	}

	@Override
	protected void onStart() {
		super.onStart();
		mediaPlayer=new MediaPlayer();
//		cursor=getContentResolver().query(ContactsContract.Contacts.CONTENT_URI,null,null,null, ContactsContract.Contacts.DISPLAY_NAME);
		if(contactArrayList.size()==0)
		new LoadContactsTask().execute();
		contactsRingtoneAdapter=new ContactsRingtoneAdapter();
		listView.setAdapter(contactsRingtoneAdapter);
	}

	class ContactsRingtoneAdapter extends BaseAdapter{

		@Override
		public int getCount() {
			return contactArrayList.size();
		}

		@Override
		public String getItem(int position) {
			return contactArrayList.get(position).getName();

		}

		@Override
		public long getItemId(int position) {
			return contactArrayList.get(position).getId();
		}


		public String getRingtoneName(int position){
			return contactArrayList.get(position).getRingtoneName();

		}
		public Uri getRingtoneUri(int position){
			return contactArrayList.get(position).getRingtoneUri();
		}
		@Override
		public View getView(final int position, View view, final ViewGroup parent) {
			if(view==null){
				view=getLayoutInflater().inflate(R.layout.layout_my_audio_item,null);
			}
			final ImageView imageView=(ImageView)view.findViewById(R.id.imageViewPlayPauseMyAudioItem);
			ConstraintLayout constraintLayout=(ConstraintLayout) view.findViewById(R.id.constraintLayoutContentMyAudio);
			TextView textView=(TextView)view.findViewById(R.id.textViewFileNameMyAudioItem);
			TextView textView1=(TextView)view.findViewById(R.id.textViewModificationDate);
			if(!isCallExplicit){
				textView.setText(getItem(position));
				textView1.setText(getRingtoneName(position));
			}
			if(playingSongPosition !=position)
				imageView.setImageResource(R.drawable.icon_play);
			else
				imageView.setImageResource(R.drawable.icon_pause);

			constraintLayout.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					CONTACT_ID=getItemId(position);
					Intent intent=new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
					ContactsRingtoneActivity.this.startActivityForResult(intent,REQ_CODE);
				}
			});
			final View finalView = view;
			imageView.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					currentTrack=position;
					if(prevTrack==currentTrack){
						if(mediaPlayer.isPlaying()){
							mediaPlayer.pause();
							mediaPlayer.seekTo(0);
							imageView.setImageResource(R.drawable.icon_play);
							playingSongPosition =-1;

						}else{
							mediaPlayer.start();
							imageView.setImageResource(R.drawable.icon_pause);
						}
					}else{
						if(prevTrack!=-1) {
							isCallExplicit=true;
//							Toast.makeText(ContactsRingtoneActivity.this, "prev:"+prevTrack+" current:"+currentTrack, Toast.LENGTH_SHORT).show();
							ImageView prevImageView = (ImageView) getView(prevTrack, prevView, parent).findViewById(R.id.imageViewPlayPauseMyAudioItem);
							prevImageView.setImageResource(R.drawable.icon_play);
							isCallExplicit=false;
						}
						mediaPlayer.stop();
						mediaPlayer.reset();
						mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
							@Override
							public void onPrepared(MediaPlayer mp) {
								mediaPlayer.start();
								playingSongPosition =position;
								imageView.setImageResource(R.drawable.icon_pause);
							}
						});
						mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
							@Override
							public void onCompletion(MediaPlayer mp) {
								imageView.setImageResource(R.drawable.icon_play);
								mediaPlayer.seekTo(0);
								playingSongPosition =-1;
							}
						});
						try {
							mediaPlayer.setDataSource(ContactsRingtoneActivity.this,getRingtoneUri(position));
							mediaPlayer.prepare();
						} catch (IOException e) {
							e.printStackTrace();
						}
						prevTrack=currentTrack;
						prevView= finalView;
					}
				}
			});
			return view;
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(resultCode==RESULT_OK && requestCode==REQ_CODE){
			Uri uri=data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
			if(uri!=null){
				ContentValues contentValues=new ContentValues();
				contentValues.put(ContactsContract.Contacts.CUSTOM_RINGTONE,uri.toString());
				getContentResolver().update(ContactsContract.Contacts.CONTENT_URI,contentValues,ContactsContract.Contacts._ID+" = ?",new String[]{CONTACT_ID+""});
				contactsRingtoneAdapter.notifyDataSetChanged();
			}
		}

	}

	@Override
	protected void onStop() {
		super.onStop();
		mediaPlayer.stop();
		mediaPlayer.reset();
		mediaPlayer.release();
		playingSongPosition =-1;
	}

	class MContact{
		String name;
		long id;
		Uri ringtoneUri;
		String ringtoneName;

		public MContact(String name, long id, Uri ringtoneUri, String ringtoneName) {
			this.name = name;
			this.id = id;
			this.ringtoneUri = ringtoneUri;
			this.ringtoneName = ringtoneName;
		}

		public String getRingtoneName() {
			return ringtoneName;
		}

		public void setRingtoneName(String ringtoneName) {
			this.ringtoneName = ringtoneName;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public long getId() {
			return id;
		}

		public void setId(long id) {
			this.id = id;
		}

		public Uri getRingtoneUri() {
			return ringtoneUri;
		}

		public void setRingtoneUri(Uri ringtoneUri) {
			this.ringtoneUri = ringtoneUri;
		}
	}
	class LoadContactsTask extends AsyncTask<Void,Integer,ArrayList<MContact>>{
		private final static int noOfContactsPerRefresh=20;
		private boolean listHasEnded=false;
		private ProgressDialog progressDialog;
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			cursor=getContentResolver().query(ContactsContract.Contacts.CONTENT_URI,null,null,null, ContactsContract.Contacts.DISPLAY_NAME);
			progressDialog=new ProgressDialog(ContactsRingtoneActivity.this);
			progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			progressDialog.setTitle(R.string.loading_contacts);
			progressDialog.show();

		}

		@Override
		protected ArrayList<MContact> doInBackground(Void... voids) {
			ArrayList<MContact> tempList=new ArrayList<>();
			while (cursor.moveToNext()){
				String name=cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
				long id=cursor.getLong(cursor.getColumnIndex(ContactsContract.Contacts._ID));
				Uri ringtoneUri;
				String ringtoneName;
				String ringtoneUriString=cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.CUSTOM_RINGTONE));
				if(ringtoneUriString==null){
					ringtoneUri= RingtoneManager.getActualDefaultRingtoneUri(ContactsRingtoneActivity.this,RingtoneManager.TYPE_RINGTONE);
					ringtoneName="Default ("+ RingtoneManager.getRingtone(ContactsRingtoneActivity.this,RingtoneManager.getActualDefaultRingtoneUri(ContactsRingtoneActivity.this,RingtoneManager.TYPE_RINGTONE)).getTitle(ContactsRingtoneActivity.this)+")";
				}else{
					ringtoneUri= Uri.parse(ringtoneUriString);
					ringtoneName=RingtoneManager.getRingtone(ContactsRingtoneActivity.this, Uri.parse(ringtoneUriString)).getTitle(ContactsRingtoneActivity.this);
				}
				tempList.add(new MContact(name,id,ringtoneUri,ringtoneName));
				publishProgress(tempList.size());
			}
			return tempList;
		}

		@Override
		protected void onProgressUpdate(Integer... values) {
			super.onProgressUpdate(values);
			progressDialog.setProgress(values[0]);
			progressDialog.setMessage(values[0]+" of "+cursor.getCount());
		}

		@Override
		protected void onPostExecute(ArrayList<MContact> mContacts) {
			super.onPostExecute(mContacts);
			contactArrayList.addAll(mContacts);
			contactsRingtoneAdapter.notifyDataSetChanged();
			progressDialog.dismiss();
			cursor.close();
		}
	}
}
