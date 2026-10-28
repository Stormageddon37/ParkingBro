package com.bresler.parkingbro.ui.main

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.flowWithLifecycle
import coil.compose.AsyncImage
import com.bresler.parkingbro.R
import com.bresler.parkingbro.ui.ParkingToast
import com.bresler.parkingbro.ui.theme.AppBlack
import com.bresler.parkingbro.ui.theme.AppWhite
import com.bresler.parkingbro.ui.theme.DarkBackground
import com.bresler.parkingbro.ui.theme.Green

private const val MAPS_SEARCH = "https://www.google.com/maps/search/?api=1&query="

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    onOpenImage: (Uri) -> Unit,
    viewModel: MainViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    var showDeleteDialog by remember { mutableStateOf(false) }
    var longPressIndex by remember { mutableStateOf<Int?>(null) }

    fun hasPermission(permission: String) =
        ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED

    // Camera capture: ACTION_IMAGE_CAPTURE into the pre-created MediaStore URI.
    val takePicture = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture(),
    ) { success -> viewModel.onImageCaptured(success) }

    val cameraPermission = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (granted) viewModel.onCameraRequested()
        else ParkingToast.show(context, ToastType.ERROR, "Permission denied!")
    }

    fun requestCamera() {
        if (hasPermission(Manifest.permission.CAMERA)) viewModel.onCameraRequested()
        else cameraPermission.launch(Manifest.permission.CAMERA)
    }

    val locationPermission = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (granted) viewModel.onSaveRequested()
        else ParkingToast.show(context, ToastType.ERROR, "You must allow location service to use this app")
    }

    fun requestSave() {
        if (hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)) viewModel.onSaveRequested()
        else locationPermission.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    // One-shot effects from the ViewModel.
    LaunchedEffectEvents(lifecycleOwner, viewModel) { event ->
        when (event) {
            is UiEvent.ShowToast -> ParkingToast.show(context, event.type, event.message)
            is UiEvent.OpenMaps -> context.startActivity(
                Intent(Intent.ACTION_VIEW, Uri.parse(MAPS_SEARCH + event.query)),
            )
            is UiEvent.OpenImage -> onOpenImage(event.uri)
            is UiEvent.LaunchCamera -> takePicture.launch(event.uri)
            UiEvent.ShowDeleteDialog -> showDeleteDialog = true
        }
    }

    Surface(modifier = Modifier.fillMaxSize(), color = DarkBackground) {
        Column(Modifier.fillMaxSize()) {
            // Black status-bar strip (edge-to-edge), matching the original window.
            Box(
                Modifier
                    .fillMaxWidth()
                    .windowInsetsTopHeight(WindowInsets.statusBars)
                    .background(AppBlack),
            )
            TopAppBar(
                title = { Text(stringResource(R.string.app_name), color = AppWhite, letterSpacing = 0.sp) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Green,
                    titleContentColor = AppWhite,
                ),
                // Status bar is handled by the black spacer above; don't add it again here.
                windowInsets = WindowInsets(0, 0, 0, 0),
                expandedHeight = 56.dp,
            )

            Box(Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Spacer(Modifier.height(40.dp))
                    BrandButton(
                        text = stringResource(R.string.save_current_location),
                        iconRes = R.drawable.ic_baseline_location_on_24,
                        onClick = ::requestSave,
                    )

                    Spacer(Modifier.height(40.dp))
                    Text(
                        text = stringResource(R.string.last_saved_location),
                        color = AppWhite,
                        fontSize = 18.sp,
                        letterSpacing = 0.sp,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = state.displayText,
                        color = AppWhite,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.sp,
                        textAlign = TextAlign.Center,
                    )

                    Spacer(Modifier.height(40.dp))
                    BrandButton(
                        text = stringResource(R.string.navigate_to_saved_location),
                        iconRes = R.drawable.ic_baseline_directions_walk_24,
                        onClick = { viewModel.onNavigateRequested() },
                    )

                    Spacer(Modifier.height(30.dp))
                    ImageRow(
                        images = state.images,
                        onClick = viewModel::onImageClicked,
                        onLongPress = { longPressIndex = it },
                    )
                }

                FloatingActionButton(
                    onClick = ::requestCamera,
                    containerColor = Green,
                    contentColor = AppWhite,
                    shape = CircleShape,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .navigationBarsPadding()
                        .padding(20.dp),
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_baseline_photo_camera_24),
                        contentDescription = stringResource(R.string.adds_images),
                        tint = AppWhite,
                        modifier = Modifier.size(32.dp),
                    )
                }
            }
        }
    }

    if (showDeleteDialog) {
        DeletePicturesDialog(
            onResult = { flush, remember ->
                showDeleteDialog = false
                viewModel.onDeleteDialogResult(flush, remember)
            },
            onDismiss = { showDeleteDialog = false },
        )
    }

    longPressIndex?.let { index ->
        val hasImage = state.images.getOrNull(index) != null
        ImageActionDialog(
            hasImage = hasImage,
            onConfirm = {
                longPressIndex = null
                if (hasImage) viewModel.onRemoveImage(index) else requestCamera()
            },
            onDismiss = { longPressIndex = null },
        )
    }
}

@Composable
private fun BrandButton(text: String, iconRes: Int, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = Green, contentColor = AppWhite),
        shape = RoundedCornerShape(32.dp),
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = null,
            tint = AppWhite,
            modifier = Modifier.size(32.dp),
        )
        Spacer(Modifier.width(8.dp))
        Text(text = text, color = AppWhite, fontSize = 18.sp, letterSpacing = 0.sp, textAlign = TextAlign.Center)
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ImageRow(
    images: List<Uri?>,
    onClick: (Int) -> Unit,
    onLongPress: (Int) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 48.dp),
        horizontalArrangement = Arrangement.spacedBy(0.dp),
    ) {
        images.forEachIndexed { index, uri ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(90.dp)
                    .combinedClickable(
                        onClick = { onClick(index) },
                        onLongClick = { onLongPress(index) },
                    ),
                contentAlignment = Alignment.Center,
            ) {
                if (uri != null) {
                    AsyncImage(
                        model = uri,
                        contentDescription = "Image",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.fillMaxSize(),
                    )
                } else {
                    Image(
                        painter = painterResource(R.drawable.ic_baseline_image_24),
                        contentDescription = "Image",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }
        }
    }
}

@Composable
private fun DeletePicturesDialog(
    onResult: (flush: Boolean, remember: Boolean) -> Unit,
    onDismiss: () -> Unit,
) {
    var rememberChoice by remember { mutableStateOf(false) }
    AlertDialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(dismissOnClickOutside = false),
        title = { Text("Delete pictures?") },
        text = {
            Column {
                Text("Delete your saved pictures?")
                Spacer(Modifier.height(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = rememberChoice,
                        onCheckedChange = { rememberChoice = it },
                        colors = CheckboxDefaults.colors(checkedColor = Green),
                    )
                    Text(stringResource(R.string.remember_this_decision), fontSize = 16.sp)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onResult(true, rememberChoice) }) {
                Text("YES", color = Green)
            }
        },
        dismissButton = {
            TextButton(onClick = { onResult(false, rememberChoice) }) {
                Text("NO", color = Green)
            }
        },
    )
}

/** Collects the ViewModel's one-shot events only while the screen is started. */
@Composable
private fun LaunchedEffectEvents(
    lifecycleOwner: LifecycleOwner,
    viewModel: MainViewModel,
    onEvent: (UiEvent) -> Unit,
) {
    LaunchedEffect(viewModel, lifecycleOwner) {
        viewModel.events
            .flowWithLifecycle(lifecycleOwner.lifecycle)
            .collect { onEvent(it) }
    }
}

@Composable
private fun ImageActionDialog(
    hasImage: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (hasImage) "Remove image?" else "Add image?") },
        confirmButton = {
            TextButton(onClick = onConfirm) { Text("Yes", color = Green) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = Green) }
        },
    )
}
