package com.example.mymp3cutter;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Environment;
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
import android.widget.TextView;

import java.io.File;
import java.io.IOException;

public class MyAudioActivity extends AppCompatActivity {

	private ListView listView;
	private File file;
	private Toolbar toolbar;
	private MediaPlayer mediaPlayer;
	private int prevTrack=-1,currentTrack=-1,playingSongPosition=-1;
	private boolean isCallExplicit=false;
	private View prevView;
	private final String extPath=Environment.getExternalStorageDirectory().getAbsolutePath()+"/Music/MyMp3Cutter/";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_my_audio);
		listView=(ListView)findViewById(R.id.lv_my_audio);
		toolbar=(Toolbar)findViewById(R.id.toolbar_my_audio);
		setSupportActionBar(toolbar);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		String readingPath=Environment.getExternalStorageDirectory().getAbsolutePath()+"/Music/MyMp3Cutter";
		file = new File(readingPath);
		if(!file.exists()){
			file.mkdir();
		}
		MyAudioAdapter myAudioAdapter=new MyAudioAdapter();
		//ArrayAdapter arrayAdapter =new ArrayAdapter(this,android.R.layout.simple_list_item_1,file.list());
		listView.setAdapter(myAudioAdapter);
	}
	class MyAudioAdapter extends BaseAdapter{

		@Override
		public int getCount() {
			return file.list().length;
		}

		@Override
		public String getItem(int position) {
			return file.list()[position];
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(final int position, View view, final ViewGroup parent) {
			if(view==null){
				view=getLayoutInflater().inflate(R.layout.layout_my_audio_item,null);
			}
			final String filePath=extPath+getItem(position);
			final ImageView imageView=(ImageView)view.findViewById(R.id.imageViewPlayPauseMyAudioItem);
			ConstraintLayout constraintLayout=(ConstraintLayout) view.findViewById(R.id.constraintLayoutContentMyAudio);
			TextView textView=(TextView)view.findViewById(R.id.textViewFileNameMyAudioItem);
			TextView textView1=(TextView)view.findViewById(R.id.textViewModificationDate);
			textView1.setText("");
			if(!isCallExplicit){
				textView.setText(getItem(position));
			}
			if(playingSongPosition !=position)
				imageView.setImageResource(R.drawable.icon_play);
			else
				imageView.setImageResource(R.drawable.icon_pause);

			constraintLayout.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent intent=new Intent(MyAudioActivity.this,PlayMusicActivity.class);
					intent.putExtra(getResources().getString(R.string.music_file_name),getItem(position));
					intent.putExtra(getResources().getString(R.string.selected_song_uri),getUri(position));
					startActivity(intent);
				}
			});
			final View finalView=view;
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
							mediaPlayer.setDataSource(MyAudioActivity.this, getUri(position));
							mediaPlayer.prepare();
						} catch (IOException e) {
							e.printStackTrace();
						}
						prevTrack=currentTrack;
						prevView= finalView;
					}
				}
			});
			return  view;
		}
	}
	public Uri getUri(int position){
		return Uri.fromFile(file.listFiles()[position]);
	}

	@Override
	protected void onStart() {
		super.onStart();
		mediaPlayer=new MediaPlayer();
	}

	@Override
	protected void onStop() {
		super.onStop();
//		mediaPlayer.stop();
		mediaPlayer.reset();
		mediaPlayer.release();
		playingSongPosition =-1;
	}

}
