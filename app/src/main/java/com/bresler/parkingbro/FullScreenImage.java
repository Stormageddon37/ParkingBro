package com.bresler.parkingbro;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class FullScreenImage extends AppCompatActivity {

	ImageView imageView;
	FloatingActionButton button;

	private void setupActionBar() {
		ActionBar actionBar = getSupportActionBar();
		ColorDrawable colorDrawable = new ColorDrawable(Color.parseColor("#00A651"));
		if (actionBar != null) {
			actionBar.setBackgroundDrawable(colorDrawable);
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_full_screen_image);
		setupActionBar();
		Bundle extras = getIntent().getExtras();
		Uri myUri = Uri.parse(extras.getString("imageUri"));

		imageView = findViewById(R.id.full_image);
		button = findViewById(R.id.back);
		button.setOnClickListener(v -> FullScreenImage.this.finish());
		imageView.setImageURI(myUri);
	}

}
