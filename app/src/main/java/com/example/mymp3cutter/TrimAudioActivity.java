package com.example.mymp3cutter;

import android.content.Intent;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.provider.MediaStore;
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

import java.io.IOException;

public class TrimAudioActivity extends AppCompatActivity {

	private Toolbar toolbar;
	private ListView listView;
	private Cursor cursor;
	private MediaPlayer mediaPlayer;
	private int currentTrack=-1;
	private boolean isCallExplicit=false;
	private int playingSongPosition=-1;
	private int prevTrack=-1;
	private View prevView;
	private TrimSongAdapter trimSongAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_trim_audio);
		toolbar=(Toolbar)findViewById(R.id.toolbar_trim_song);
		listView=(ListView)findViewById(R.id.listViewTrimSong);
		setSupportActionBar(toolbar);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
	}

	@Override
	protected void onStart() {
		super.onStart();
		cursor=getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,null, MediaStore.Audio.Media.IS_MUSIC+"!=0",null, MediaStore.Audio.Media.DISPLAY_NAME);
		trimSongAdapter=new TrimSongAdapter();
		listView.setAdapter(trimSongAdapter);
		mediaPlayer=new MediaPlayer();
	}

	class TrimSongAdapter extends BaseAdapter{

		@Override
		public int getCount() {
			return cursor.getCount();
		}

		@Override
		public String getItem(int position) {
			cursor.moveToPosition(position);
			return cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME));
		}

		@Override
		public long getItemId(int position) {
			cursor.moveToPosition(position);
			return cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media._ID));
		}


		public Uri getUri(int position){
			cursor.moveToPosition(position);
			return Uri.parse(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA)));
		}

		public long getDuration(int position){
			cursor.moveToPosition(position);
			return cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION));
		}

		public String getArtistName(int position){
			cursor.moveToPosition(position);
			return cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Artists.ARTIST));
		}
		@Override
		public View getView(final int position, View view, final ViewGroup parent) {
			if(view==null){
				view=getLayoutInflater().inflate(R.layout.layout_my_audio_item,null);
			}
			final ImageView imageView=(ImageView)view.findViewById(R.id.imageViewPlayPauseMyAudioItem);
			ConstraintLayout constraintLayout=(ConstraintLayout)view.findViewById(R.id.constraintLayoutContentMyAudio);
			TextView textView=(TextView)view.findViewById(R.id.textViewFileNameMyAudioItem);
			TextView textView1=(TextView)view.findViewById(R.id.textViewModificationDate);
			if(!isCallExplicit){
				textView.setText(getItem(position));
				textView1.setText(getArtistName(position));
			}
			if(playingSongPosition !=position)
				imageView.setImageResource(R.drawable.icon_play);
			else
				imageView.setImageResource(R.drawable.icon_pause);

			constraintLayout.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent intent=new Intent(TrimAudioActivity.this,SelectSectionActivity.class);
					intent.putExtra(getResources().getString(R.string.music_file_name),getItem(position));
					intent.putExtra(getResources().getString(R.string.selected_song_uri),getUri(position));
					intent.putExtra(getResources().getString(R.string.duration),getDuration(position));
					startActivity(intent);
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
							mediaPlayer.setDataSource(TrimAudioActivity.this,getUri(position));
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
	protected void onStop() {
		super.onStop();
		mediaPlayer.stop();
		mediaPlayer.reset();
		mediaPlayer.release();
		playingSongPosition =-1;
	}
}
