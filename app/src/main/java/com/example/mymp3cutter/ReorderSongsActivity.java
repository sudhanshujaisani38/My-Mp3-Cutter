package com.example.mymp3cutter;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.DragEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;

public class ReorderSongsActivity extends AppCompatActivity {

	private ArrayList<String> selectedSongsStringIds;
	private ArrayList<Song>reorderableSongs;
	private ArrayList<Uri>selectedSongsUri;
	private Cursor cursor;
	private ReorderSongsAdapter reorderSongsAdapter;
	private Toolbar toolbar;
	private FloatingActionButton floatingActionButton;
	private ListView listView;
	private MediaPlayer mediaPlayer;
	private int prevTrack=-1;
	private View prevView=null;
	private int currentTrack=-1;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_reorder_songs);
		toolbar=(Toolbar)findViewById(R.id.toolbar_reorder_songs);
		setSupportActionBar(toolbar);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		listView=(ListView)findViewById(R.id.listViewReorderSongs);
		floatingActionButton=(FloatingActionButton)findViewById(R.id.fabReorder);
		selectedSongsStringIds =(ArrayList<String>) getIntent().getSerializableExtra(getResources().getString(R.string.selected_songs));
		floatingActionButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				selectedSongsUri.clear();
				for(int i = 0; i< reorderableSongs.size(); i++){
					selectedSongsUri.add(reorderSongsAdapter.getUri(i));
				}
				final AlertDialog.Builder builder = new AlertDialog.Builder(ReorderSongsActivity.this);
				builder.setTitle("Save as");
				final EditText editTextNewSongName=new EditText(ReorderSongsActivity.this);
				String name="";
				for(int i = 0; i< reorderableSongs.size(); i++){
					name+=reorderSongsAdapter.getItem(i);
				}
				editTextNewSongName.setText(name);
				builder.setView(editTextNewSongName);
				builder.setIcon(android.R.drawable.ic_menu_edit);

				builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						Intent intent=new Intent(ReorderSongsActivity.this,MergingActivity.class);
						intent.putExtra(getResources().getString(R.string.music_file_name),editTextNewSongName.getText().toString());
						intent.putExtra(getResources().getString(R.string.selected_song_uri),selectedSongsUri);
						startActivity(intent);
					}
				});
				builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});
				builder.create().show();
			}
		});
	}

	@Override
	protected void onStart() {
		super.onStart();
		selectedSongsUri=new ArrayList<>();
		reorderableSongs=new ArrayList<>();
		mediaPlayer=new MediaPlayer();
		reorderSongsAdapter=new ReorderSongsAdapter();
		String id= MediaStore.Audio.Media._ID;
		String selection=id+"=?";
		String ids[]=null;
		if(selectedSongsStringIds.size()>=1){
			for(int i = 0; i< selectedSongsStringIds.size()-1; i++){
				selection=selection+" OR "+id+"=?";
			}
			ids=new String[selectedSongsStringIds.size()];
			for(int i = 0; i< selectedSongsStringIds.size(); i++){
				ids[i]= selectedSongsStringIds.get(i);
			}
		}
		if(selectedSongsStringIds.size()==0){
			selection="1=0";
		}
		cursor=getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,null,selection,ids,null);
		while (cursor.moveToNext()){
			String songName=cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME));
			Uri uri=Uri.parse(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA)));
			String artistName=cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Artists.ARTIST));
			long _id=cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media._ID));
			long msec=cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION));
			String duration=getDurationString(msec);
			reorderableSongs.add(new Song(songName,uri,duration,artistName,_id));
		}
		listView.setAdapter(reorderSongsAdapter);
	}
	String getDurationString(long msec){

		if(msec==0)
			return "00:00";
		else {
			long sec = msec / 1000;
			long min = sec / 60;
			sec = sec % 60;
			String minstr = min + "";
			String secstr = sec + "";
			if (min < 10)
				minstr = "0" + min;
			if (sec < 10)
				secstr = "0" + sec;
			return minstr + ":" + secstr;
		}
	}

	class ReorderSongsAdapter extends BaseAdapter{

		@Override
		public int getCount() {
			return reorderableSongs.size();
		}

		@Override
		public String getItem(int position) {
			return reorderableSongs.get(position).getSongName();
		}

		@Override
		public long getItemId(int position) {
			return reorderableSongs.get(position).getId();
		}


		public Uri getUri(int position){
			return reorderableSongs.get(position).getUri();
		}
		@Override
		public View getView(final int position, View view, final ViewGroup parent) {
			if(view==null){
				view=getLayoutInflater().inflate(R.layout.layout_reorder_songs_item,null);
			}
			view.setTag(R.string.original_position,position);
			view.setTag(R.string.last_crossed,position);
			final ImageView imageView=(ImageView)view.findViewById(R.id.imageViewPlayPauseReorder);
			TextView textViewFileName=(TextView)view.findViewById(R.id.textViewFileNameReorder);
			TextView textViewArtist=(TextView)view.findViewById(R.id.textViewArtistReorder);
			TextView textViewDuration=(TextView)view.findViewById(R.id.textViewDurationReorder);
			ImageView imageViewClear=(ImageView)view.findViewById(R.id.imageViewClearReorder);
			textViewFileName.setText(getItem(position));
			textViewArtist.setText(getArtistName(position));
			textViewDuration.setText(getDuration(position));
			imageViewClear.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					reorderableSongs.remove(reorderableSongs.get(position));
					reorderSongsAdapter.notifyDataSetChanged();
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
						if(prevTrack!=-1 && reorderableSongs.size()>1) {
							ImageView prevImageView = (ImageView) getView(prevTrack, prevView, parent).findViewById(R.id.imageViewPlayPauseReorder);
							prevImageView.setImageResource(R.drawable.icon_play);
						}
						mediaPlayer.stop();
						mediaPlayer.reset();
						mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
							@Override
							public void onPrepared(MediaPlayer mp) {
								mediaPlayer.start();
								imageView.setImageResource(R.drawable.icon_pause);
							}
						});
						mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
							@Override
							public void onCompletion(MediaPlayer mp) {
								imageView.setImageResource(R.drawable.icon_play);
								mediaPlayer.seekTo(0);
							}
						});
						try {
							mediaPlayer.setDataSource(ReorderSongsActivity.this,getUri(position));
							mediaPlayer.prepare();
						} catch (IOException e) {
							e.printStackTrace();
						}
						prevTrack=currentTrack;
						prevView= finalView;
					}
				}
			});
			final View finalView1 = view;
			view.setOnDragListener(new View.OnDragListener() {
				@Override
				public boolean onDrag(View v, DragEvent event) {
					View draggingView=(View)event.getLocalState();

					switch (event.getAction()){
						case DragEvent.ACTION_DROP:
							Toast.makeText(ReorderSongsActivity.this, "Dropped on"+position, Toast.LENGTH_SHORT).show();
							draggingView.setVisibility(View.VISIBLE);
							int draggedSongPosition=(int)draggingView.getTag(R.string.original_position);
							Song draggedSong=reorderableSongs.get(draggedSongPosition);
							if(reorderableSongs.remove(reorderableSongs.get(draggedSongPosition)))
//								Toast.makeText(ReorderSongsActivity.this, "working", Toast.LENGTH_SHORT).show();
							reorderableSongs.add(position,draggedSong);
							reorderSongsAdapter.notifyDataSetChanged();
							break;

						case DragEvent.ACTION_DRAG_ENDED:
							draggingView.setVisibility(View.VISIBLE);
							break;

						case DragEvent.ACTION_DRAG_ENTERED:
							int pos=(int)draggingView.getTag(R.string.last_crossed);
							View view1=reorderSongsAdapter.getView(pos,null,parent);
							float x=view1.getX();
							float y=view1.getY();
							finalView1.animate().translationX(x);
							finalView1.animate().translationY(y);

						case DragEvent.ACTION_DRAG_EXITED:
							draggingView.setTag(R.string.last_crossed,position);
					}
					return true;
				}
			});
			view.setOnLongClickListener(new View.OnLongClickListener() {
				@Override
				public boolean onLongClick(View v) {
					View.DragShadowBuilder shadowBuilder=new View.DragShadowBuilder(v);
					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
						v.startDragAndDrop(null,shadowBuilder,v,View.DRAG_FLAG_OPAQUE);

					}
					v.setTag(R.string.last_crossed,position);
					v.setTag(R.string.original_position,position);
					v.setVisibility(View.INVISIBLE);
					return true;
				}
			});
			return view;
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
	}

	@Override
	protected void onStop() {
		super.onStop();
		cursor.close();
		mediaPlayer.stop();
		mediaPlayer.reset();
		mediaPlayer.release();
	}
	class Song{
		String songName;
		Uri uri;
		String duration;
		String artistName;
		long id;

		public Song(String songName, Uri uri, String duration, String artistName, long id) {
			this.songName = songName;
			this.uri = uri;
			this.duration = duration;
			this.artistName = artistName;
			this.id = id;
		}

		public String getSongName() {
			return songName;
		}

		public void setSongName(String songName) {
			this.songName = songName;
		}

		public Uri getUri() {
			return uri;
		}

		public void setUri(Uri uri) {
			this.uri = uri;
		}

		public String getDuration() {
			return duration;
		}

		public void setDuration(String duration) {
			this.duration = duration;
		}

		public String getArtistName() {
			return artistName;
		}

		public void setArtistName(String artistName) {
			this.artistName = artistName;
		}

		public long getId() {
			return id;
		}

		public void setId(long id) {
			this.id = id;
		}
	}
}
