package com.bresler.parkingbro;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {


	public static final String LOCATION_PREFS_INDEX = "location";
	public static final String googleMaps = "https://www.google.com/maps/search/?api=1&query=";
	public static final int PERMISSION_REQUEST_LOCATION_SAVE = 99;

	private void failToast(String text) {
		Toast.makeText(MainActivity.this, text, Toast.LENGTH_LONG).show();
	}

	private void addLocationPermission() {
		ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_LOCATION_SAVE);
	}

	private void showPermissionsDialog() {
		new AlertDialog.Builder(this)
				.setTitle(R.string.title_location_permission)
				.setMessage(R.string.text_location_permission)
				.setPositiveButton(R.string.ok, (dialogInterface, i) -> addLocationPermission())
				.create()
				.show();
	}

	public boolean checkLocationPermission() {
		if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
			if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
				showPermissionsDialog();
			} else {
				addLocationPermission();
				return true;
			}
			return false;
		} else {
			return true;
		}
	}

	private Location getOptimalLocation() {
		LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
				ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
			return null;
		}
		Location optimalLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
		if (optimalLocation == null) {
			optimalLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
		}
		if (optimalLocation == null) {
			optimalLocation = locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
		}
		return optimalLocation;
	}

	private String stringifyLocation(Location location) {
		if (location == null) return null;
		return location.getLatitude() + ", " + location.getLongitude();
	}

	private boolean savedLocationSuccessfully(String locationString) {
		return MainActivity.this.getPreferences(MODE_PRIVATE).edit().putString(LOCATION_PREFS_INDEX, locationString).commit();
	}

	private Uri getNavigationURL() {
		SharedPreferences sharedPreferences = MainActivity.this.getPreferences(MODE_PRIVATE);
		String url = googleMaps + sharedPreferences.getString(LOCATION_PREFS_INDEX, "0.0,0.0");
		return Uri.parse(url);
	}

	private void setupSaveButton() {
		Button button = findViewById(R.id.save);
		button.setOnClickListener(v -> {
			if (!checkLocationPermission()) {
				failToast("You must allow location service to use this app");
				return;
			}
			saveLocation();
		});
	}

	private void saveLocation() {
		Location currentLocation = getOptimalLocation();
		String locationString = stringifyLocation(currentLocation);
		if (locationString == null)
			failToast("Unable to get location");
		if (!savedLocationSuccessfully(locationString)) {
			failToast("Location saving failed, please try again later");
		}
	}

	private void setupNavigateButton() {
		Button button = findViewById(R.id.navigate);
		button.setOnClickListener(v -> {
			Intent browserIntent = new Intent(Intent.ACTION_VIEW, getNavigationURL());
			startActivity(browserIntent);
		});
	}

	private void setupButtons() {
		setupNavigateButton();
		setupSaveButton();
	}

	private void initializeLocation() {
		TextView textView = findViewById(R.id.actualLocation);
		SharedPreferences sharedPreferences = getPreferences(MODE_PRIVATE);
		textView.setText(sharedPreferences.getString(LOCATION_PREFS_INDEX, "Unknown"));
	}

	private void setupActionBar() {
		ActionBar actionBar = getSupportActionBar();
		ColorDrawable colorDrawable = new ColorDrawable(Color.parseColor("#00A651"));
		if (actionBar != null) {
			actionBar.setBackgroundDrawable(colorDrawable);
		}
	}

	private void setupStatusBar() {
		Window window = this.getWindow();
		window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
		window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
		window.setStatusBarColor(ContextCompat.getColor(this, R.color.black));
	}

	private void setupColors() {
		setupActionBar();
		setupStatusBar();
	}


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		setupButtons();
		initializeLocation();
		setupColors();
	}
}