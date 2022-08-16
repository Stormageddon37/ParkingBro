package com.bresler.parkingbro;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class FullScreenImageActivity extends AppCompatActivity {

	private ImageView imageView;
	private ImageButton deleteButton;
	private FloatingActionButton backButton;
	private int index;

	private void setupActionBar() {
		ActionBar actionBar = getSupportActionBar();
		ColorDrawable colorDrawable = new ColorDrawable(Color.parseColor("#00A651"));
		if (actionBar != null) {
			actionBar.setBackgroundDrawable(colorDrawable);
		}
	}

	private void findViews() {
		imageView = findViewById(R.id.full_image);
		backButton = findViewById(R.id.back);
		deleteButton = findViewById(R.id.delete);
	}

	private void setupButtons() {
		backButton.setOnClickListener(view -> {
			Intent resultIntent = new Intent();
			resultIntent.putExtra("delete", false);
			resultIntent.putExtra("index", index);
			setResult(Activity.RESULT_OK, resultIntent);
			finish();
		});
		deleteButton.setOnClickListener(view -> {
			Intent resultIntent = new Intent();
			resultIntent.putExtra("delete", true);
			resultIntent.putExtra("index", index);
			setResult(Activity.RESULT_OK, resultIntent);
			finish();
		});
	}

	private void displayImage() {
		Bundle extras = getIntent().getExtras();
		Uri imageUri = Uri.parse(extras.getString("imageUri"));
		imageView.setImageURI(imageUri);
	}

	private void setup() {
		findViews();
		setupActionBar();
		setupButtons();
		displayImage();
		index = getIntent().getExtras().getInt("index");
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_full_screen_image);
		setup();
	}

}
