package com.example.mymp3cutter;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.util.ArrayList;

public class MergingActivity extends AppCompatActivity {

	private Toolbar toolbar;
	private TextView textViewSongName, textViewProgress;
	private ProgressBar progressBar;
	private ArrayList<Uri>selectedSongsUri;
	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_merging);
		toolbar=(Toolbar)findViewById(R.id.toolbar_merging);
		setSupportActionBar(toolbar);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		textViewSongName =(TextView)findViewById(R.id.textViewSongNameMerging);
		textViewProgress=(TextView)findViewById(R.id.textViewProgress);
		progressBar=(ProgressBar)findViewById(R.id.progressBarMerging);
		progressBar.setProgress(progressBar.getMax()/2,true);
		textViewSongName.setText(getIntent().getStringExtra(getResources().getString(R.string.music_file_name)));
		selectedSongsUri=(ArrayList<Uri>)getIntent().getSerializableExtra(getResources().getString(R.string.selected_song_uri));
		new MergeTask().execute(selectedSongsUri);
	}

	class MergeTask extends AsyncTask<ArrayList<Uri>,String,Void>{
		private ProgressDialog progressDialog;
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			progressDialog=new ProgressDialog(MergingActivity.this);
			progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			progressDialog.setTitle(R.string.merging);
			progressDialog.show();
		}

		@Override
		protected Void doInBackground(ArrayList<Uri>... arrayLists) {
			ArrayList<Uri>uriArrayList=arrayLists[0];
			String path= Environment.getExternalStorageDirectory().getAbsolutePath()+"/Music/MyMp3Cutter/"+textViewSongName.getText().toString()+".mp3";
			File file=new File(path);
			try {
				file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
			BufferedOutputStream bufferedOutputStream=null;
			try {
				bufferedOutputStream=new BufferedOutputStream(new FileOutputStream(file));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			for(int i=0;i<uriArrayList.size();i++){
				publishProgress("File:"+(i+1));
				//Log.d("---------------------","Next Song:"+(i+1)+"----------");
				File temp=new File(uriArrayList.get(i).toString());
				byte[] tempBytes=new byte[2048];
				try {
					BufferedInputStream bufferedInputStream=new BufferedInputStream(new FileInputStream(temp));
					while (bufferedInputStream.read(tempBytes)!=-1){
						//Log.d("Merging","Song");
						bufferedOutputStream.write(tempBytes);
					}
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}

			}
			return null;
		}

		@Override
		protected void onProgressUpdate(String... values) {
			super.onProgressUpdate(values);
			progressDialog.setMessage(values[0]);
		}

		@Override
		protected void onPostExecute(Void aVoid) {
			super.onPostExecute(aVoid);
			progressDialog.dismiss();
			Intent intent=new Intent(MergingActivity.this,PlayMusicActivity.class);
			intent.putExtra(getResources().getString(R.string.music_file_name),textViewSongName.getText().toString());
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
			startActivity(intent);
		}
	}

}
