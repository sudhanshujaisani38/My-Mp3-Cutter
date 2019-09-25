package com.example.mymp3cutter;

import android.content.Intent;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;

public class MergeAudioActivity extends AppCompatActivity {

	private Toolbar toolbar;
	private ListView listView;
	private Cursor cursor;
	private MergeAudioAdapter mergeAudioAdapter;
	private MediaPlayer mediaPlayer;
	private int currentTrack=-1,prevTrack=-1, playingSongPosition =-1;
	private View prevView;
	private FloatingActionButton floatingActionButton;
	private boolean isCallExplicit=false;


	ArrayList<String> selectedSongsIds;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_merge_audio);
		toolbar=(Toolbar)findViewById(R.id.toolbar_merge_audio);
		listView=(ListView)findViewById(R.id.listViewMergeAudio);
		floatingActionButton=(FloatingActionButton)findViewById(R.id.fabMergeAudio);
		setSupportActionBar(toolbar);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		floatingActionButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(selectedSongsIds.size()>=2){
					Intent intent=new Intent(MergeAudioActivity.this,ReorderSongsActivity.class);
					intent.putExtra(getResources().getString(R.string.selected_songs), selectedSongsIds);
					startActivity(intent);
				}else{
					Toast.makeText(MergeAudioActivity.this, getResources().getString(R.string.atleast_2_songs), Toast.LENGTH_SHORT).show();
				}
			}
		});

	}

	@Override
	protected void onStart() {
		super.onStart();
		cursor=getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,null, MediaStore.Audio.Media.IS_MUSIC+"!=0",null, MediaStore.Audio.Media.DISPLAY_NAME);
		mergeAudioAdapter=new MergeAudioAdapter();
		listView.setAdapter(mergeAudioAdapter);
		mediaPlayer=new MediaPlayer();
		selectedSongsIds =new ArrayList<>();
	}

	class MergeAudioAdapter extends BaseAdapter{

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

		public String getArtistName(int position){
			cursor.moveToPosition(position);
			return cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Artists.ARTIST));
		}

		public String getDuration(int position){
			cursor.moveToPosition(position);
			long msec=cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION));
			if(msec==0)
				return "00:00";
			long sec=msec/1000;
			long min=sec/60;
			sec=sec%60;
			String minstr=min+"";
			String secstr=sec+"";
			if(min<10)
				minstr="0"+min;
			if(sec<10)
				secstr="0"+sec;
			return  minstr+":"+secstr;
		}
		public Uri getUri(int position){
			cursor.moveToPosition(position);
			return Uri.parse(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA)));
		}

		@Override
		public View getView(final int position, View view, final ViewGroup parent) {
			if(view==null){
				view=getLayoutInflater().inflate(R.layout.layout_merge_item,null);
			}
			final ImageView imageView=(ImageView) view.findViewById(R.id.imageViewPlayPauseMergeItem);
			ConstraintLayout constraintLayout=(ConstraintLayout)view.findViewById(R.id.constraintLayoutMergeItem);
			TextView textViewFileName=(TextView)view.findViewById(R.id.textViewFileNameMergeItem);
			TextView textViewArtist=(TextView)view.findViewById(R.id.textViewArtistMergeItem);
			TextView textViewDuration=(TextView)view.findViewById(R.id.textViewDuration);
			final CheckBox checkBox=(CheckBox)view.findViewById(R.id.checkBoxMergeItem);
			if(selectedSongsIds.contains(getItemId(position)+"")){
				checkBox.setChecked(true);
			}else
				checkBox.setChecked(false);
			if(!isCallExplicit){
				textViewFileName.setText(getItem(position));
				textViewArtist.setText(getArtistName(position));
				textViewDuration.setText(getDuration(position));
			}

			if(playingSongPosition !=position)
				imageView.setImageResource(R.drawable.icon_play);
			else
				imageView.setImageResource(R.drawable.icon_pause);
			checkBox.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					boolean isChecked=checkBox.isChecked();
					if(isChecked){
						if(selectedSongsIds.size()==4){
							Toast.makeText(MergeAudioActivity.this, "Max 4 songs allowed", Toast.LENGTH_SHORT).show();
							checkBox.setChecked(false);
						}else{
							if(!selectedSongsIds.contains(getItemId(position)));
							selectedSongsIds.add(getItemId(position)+"");
						}
					}else{
						if(selectedSongsIds.contains(getItemId(position)+"")){
							selectedSongsIds.remove(getItemId(position)+"");
						}
					}
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

						}else{
							mediaPlayer.start();
							imageView.setImageResource(R.drawable.icon_pause);
						}
					}else{
						if(prevTrack!=-1) {
							isCallExplicit=true;
							ImageView prevImageView = (ImageView) getView(prevTrack, prevView, parent).findViewById(R.id.imageViewPlayPauseMergeItem);
							prevImageView.setImageResource(R.drawable.icon_play);
							isCallExplicit=false;
						}
						mediaPlayer.stop();
						mediaPlayer.reset();
						mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
							@Override
							public void onPrepared(MediaPlayer mp) {
								mediaPlayer.start();
								imageView.setImageResource(R.drawable.icon_pause);
								playingSongPosition =position;
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
							mediaPlayer.setDataSource(MergeAudioActivity.this,getUri(position));
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
