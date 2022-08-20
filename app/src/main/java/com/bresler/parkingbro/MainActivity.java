package com.bresler.parkingbro;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.icu.text.SimpleDateFormat;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Date;
import java.util.Locale;

import es.dmoral.toasty.Toasty;

public class MainActivity extends AppCompatActivity {


	private static final String googleMaps = "https://www.google.com/maps/search/?api=1&query=";
	private static final int MAXIMUM_IMAGES_ALLOWED = Config.MAXIMUM_IMAGES_ALLOWED;
	private static final String TIMESTAMP_PREFS_INDEX = "timestamp";
	private static final int PERMISSION_REQUEST_LOCATION_SAVE = 99;
	private static final int PERMISSION_REQUEST_OPEN_CAMERA = 1234;
	private static final String LOCATION_PREFS_INDEX = "location";
	private TextView actualLocation;
	private ImageView[] imageViews;
	private int currentImages = 0;
	private Uri[] imageUris;

	final ActivityResultLauncher<Intent> cameraActivityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
		if (result.getResultCode() == Activity.RESULT_OK) {
			imageViews[currentImages].setImageURI(imageUris[currentImages]);
			Toaster.success(this, "Saved image!");
			currentImages++;
		}
	});

	final ActivityResultLauncher<Intent> displayImageActivityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
		if (result.getResultCode() == Activity.RESULT_OK) {
			Intent intentData = result.getData();
			if (intentData == null)
				return;
			Bundle bundle = intentData.getExtras();
			boolean delete = bundle.getBoolean("delete");
			int index = bundle.getInt("index");
			if (delete) {
				removeImage(index);
			}
		}
	});

	private static Object[] pushNullsToEnd(Object[] array) {
		int j = 0;
		for (int i = 0; i < array.length; i++) {
			if (array[i] != null) {
				Object temp = array[j];
				array[j] = array[i];
				array[i] = temp;
				j++;
			}
		}
		return array;
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

	private boolean checkLocationPermission() {
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

	private boolean savedTimestampSuccessfully(String timestamp) {
		return MainActivity.this.getPreferences(MODE_PRIVATE).edit().putString(TIMESTAMP_PREFS_INDEX, timestamp).commit();
	}

	private boolean savedDataSuccessfully(String locationString, String timestamp) {
		if (locationString == null) return false;
		return savedLocationSuccessfully(locationString) && savedTimestampSuccessfully(timestamp);
	}

	private Uri getNavigationURL() {
		SharedPreferences sharedPreferences = MainActivity.this.getPreferences(MODE_PRIVATE);
		String url = googleMaps + sharedPreferences.getString(LOCATION_PREFS_INDEX, "0.0,0.0");
		return Uri.parse(url);
	}

	private void openCamera() {
		if (currentImages == MAXIMUM_IMAGES_ALLOWED) {
			Toaster.error(this, "Maximum images reached!");
			return;
		}
		ContentValues values = new ContentValues();
		values.put(MediaStore.Images.Media.TITLE, "");
		values.put(MediaStore.Images.Media.DESCRIPTION, "");
		imageUris[currentImages] = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
		Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUris[currentImages]);
		cameraActivityResultLauncher.launch(cameraIntent);
	}

	private String getTimestamp() {
		String currentDate = "";
		String currentTime = "";
		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
			currentDate = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date()).replace('-', '/');
			currentTime = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());
		}
		return currentDate + " @ " + currentTime;
	}

	private String saveLocation() {
		String locationString = stringifyLocation(getOptimalLocation());
		String timestampString = getTimestamp();
		if (locationString == null)
			Toaster.error(this, "Could not get location");
		if (!savedDataSuccessfully(locationString, timestampString))
			Toaster.error(this, "Location saving failed, please try again later");
		return locationString;
	}

	@SuppressLint("SetTextI18n")
	private void setupSaveButton() {
		Button button = findViewById(R.id.save);
		button.setOnClickListener(v -> {
			if (!checkLocationPermission()) {
				Toaster.error(this, "You must allow location service to use this app");
				return;
			}
			String locationString = saveLocation();
			if (locationString != null) {
				actualLocation.setText(locationString + '\n' + getTimestamp());
				Toaster.success(this, "Location saved!");
			} else {
				Toaster.error(this, "Could not get location");
			}
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
					requestPermissions(permissions, PERMISSION_REQUEST_OPEN_CAMERA);
				} else {
					openCamera();
				}
			} else {
				openCamera();
			}
		});
	}

	private void setupButtons() {
		setupNavigateButton();
		setupSaveButton();
		setupCameraButton();
		setupImageButtons();
	}

	private void initializeLocation() {
		SharedPreferences sharedPreferences = getPreferences(MODE_PRIVATE);
		String fullLocation = sharedPreferences.getString(LOCATION_PREFS_INDEX, "Unknown") + '\n' + sharedPreferences.getString(TIMESTAMP_PREFS_INDEX, "Unknown");
		actualLocation.setText(fullLocation);
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
		imageViews[0] = findViewById(R.id.image_view_0);
		imageViews[1] = findViewById(R.id.image_view_1);
		imageViews[2] = findViewById(R.id.image_view_2);
		actualLocation = findViewById(R.id.savedLocation);
	}

	private void setupImageButtons() {
		for (int i = 0; i < imageViews.length; i++) {
			int finalI = i;

			imageViews[i].setOnClickListener(view -> {
				if (imageUris[finalI] == null) {
					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
						if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED || checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
							String[] permissions = {Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
							requestPermissions(permissions, PERMISSION_REQUEST_OPEN_CAMERA);
						} else {
							openCamera();
						}
					} else {
						openCamera();
					}
				} else {
					Intent intent = new Intent(MainActivity.this, FullScreenImageActivity.class);
					intent.putExtra("imageUri", imageUris[finalI].toString());
					intent.putExtra("index", finalI);
					displayImageActivityResultLauncher.launch(intent);
				}
			});

			imageViews[i].setOnLongClickListener(view -> {
				if (imageUris[finalI] != null) {
					AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
					builder.setCancelable(true);
					builder.setTitle("Remove image?");
					builder.setPositiveButton("Yes", (dialog, id) -> removeImage(finalI));
					builder.setNegativeButton("Cancel", (dialog, id) -> dialog.cancel());
					AlertDialog alertDialog = builder.create();
					alertDialog.show();
					alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(getResources().getColor(R.color.green));
					alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.green));
				}
				return false;
			});
		}
	}

	private void removeImage(int index) {
		if (imageUris[index] != null) {
			imageViews[index].setImageResource(R.drawable.ic_baseline_image_24);
			Toaster.success(this, "Removed image!");
		} else {
			Toaster.error(this, "No image here!");
		}
		imageUris[index] = null;
		updateImageArray();
		currentImages--;
	}

	private void redrawImages() {
		for (int i = 0; i < imageViews.length; i++) {
			if (imageUris[i] != null)
				imageViews[i].setImageURI(imageUris[i]);
			else
				imageViews[i].setImageResource(R.drawable.ic_baseline_image_24);
		}
	}

	private void updateImageArray() {
		imageUris = (Uri[]) pushNullsToEnd(imageUris);
		redrawImages();
	}

	private void setup() {
		imageUris = new Uri[MAXIMUM_IMAGES_ALLOWED];
		imageViews = new ImageView[MAXIMUM_IMAGES_ALLOWED];
		findViews();
		setupButtons();
		setupActionBar();
		setupStatusBar();
		initializeLocation();
		Toasty.Config.getInstance().allowQueue(false).apply();
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		if (requestCode == PERMISSION_REQUEST_OPEN_CAMERA) {
			if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
				openCamera();
			} else {
				Toaster.error(this, "Permission denied!");
			}
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		setup();
	}
}
