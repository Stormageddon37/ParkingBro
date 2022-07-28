package com.bresler.parkingbro;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class MainActivity extends AppCompatActivity {


	private static final String LOCATION_PREFS_INDEX = "location";
	private static final String googleMaps = "https://www.google.com/maps/search/?api=1&query=";
	private static final int PERMISSION_REQUEST_LOCATION_SAVE = 99;
	private static final int PERMISSION_CODE = 1234;
	private static final int CAPTURE_CODE = 1001;
	private static boolean containsImage = false;
	private TextView actualLocation;
	private ImageView imageView;
	private Uri imageUri;

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

	public void openCamera() {
		ContentValues values = new ContentValues();
		values.put(MediaStore.Images.Media.TITLE, "new image");
		values.put(MediaStore.Images.Media.DESCRIPTION, "");
		imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

		Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
		startActivityForResult(cameraIntent, CAPTURE_CODE);
	}

	private String saveLocation() {
		Location currentLocation = getOptimalLocation();
		String locationString = stringifyLocation(currentLocation);
		if (locationString == null)
			Toaster.error(this, "Unable to get location");
		if (!savedLocationSuccessfully(locationString)) {
			Toaster.error(this, "Location saving failed, please try again later");
		}
		return locationString;
	}

	private void setupSaveButton() {
		Button button = findViewById(R.id.save);
		button.setOnClickListener(v -> {
			if (!checkLocationPermission()) {
				Toaster.error(this, "You must allow location service to use this app");
				return;
			}
			actualLocation.setText(saveLocation());
			Toaster.success(this, "Location saved!");
		});
	}

	private void setupNavigateButton() {
		Button button = findViewById(R.id.navigate);
		button.setOnClickListener(v -> {
			Intent browserIntent = new Intent(Intent.ACTION_VIEW, getNavigationURL());
			Toaster.info(this, "Launching navigation app!");
			startActivity(browserIntent);
		});
	}

	private void setupCameraButton() {
		FloatingActionButton cameraButton = findViewById(R.id.camera);
		cameraButton.setOnClickListener(view -> {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
				if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED || checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
					String[] permissions = {Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
					requestPermissions(permissions, PERMISSION_CODE);
				} else {
					openCamera();
				}
			} else {
				openCamera();
			}
		});
	}

	private void setupDeleteButton() {
		ImageButton imageButton = findViewById(R.id.delete);
		imageButton.setOnClickListener(view -> {
			if (containsImage) {
				imageView.setImageResource(R.drawable.ic_baseline_image_24);
				Toaster.success(this, "Latest image deleted!");
				containsImage = false;
			} else {
				Toaster.info(this, "No stashed images found!");
			}
		});
	}

	private void setupButtons() {
		setupNavigateButton();
		setupSaveButton();
		setupCameraButton();
		setupDeleteButton();
	}

	private void initializeLocation() {
		SharedPreferences sharedPreferences = getPreferences(MODE_PRIVATE);
		actualLocation.setText(sharedPreferences.getString(LOCATION_PREFS_INDEX, "Unknown"));
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

	private void findViews() {
		imageView = findViewById(R.id.image_view);
		actualLocation = findViewById(R.id.actualLocation);
	}

	private void setup() {
		findViews();
		setupButtons();
		setupActionBar();
		setupStatusBar();
		initializeLocation();
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);

		if (requestCode == PERMISSION_CODE) {
			if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
				openCamera();
			} else {
				Toaster.error(this, "Permission denied!");
			}
		}
	}

	@SuppressLint("MissingSuperCall")
	@Override
	protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
		if (resultCode == RESULT_OK) {
			imageView.setImageURI(imageUri);
			Toaster.success(this, "Saved image!");
			containsImage = true;
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		setup();
	}
}