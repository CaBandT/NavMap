package com.cabandt.navmap;

import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconAllowOverlap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconIgnorePlacement;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconImage;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconOffset;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.cabandt.navmap.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.api.directions.v5.models.DirectionsResponse;
import com.mapbox.api.directions.v5.models.DirectionsRoute;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.location.LocationComponent;
import com.mapbox.mapboxsdk.location.modes.CameraMode;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import com.mapbox.mapboxsdk.utils.BitmapUtils;
import com.mapbox.services.android.navigation.ui.v5.NavigationLauncher;
import com.mapbox.services.android.navigation.ui.v5.NavigationLauncherOptions;
import com.mapbox.services.android.navigation.ui.v5.route.NavigationMapRoute;
import com.mapbox.services.android.navigation.v5.navigation.NavigationRoute;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BookmarkViewActivity extends AppCompatActivity implements OnMapReadyCallback, PermissionsListener {

    private MapView mapView;
    private MapboxMap mapboxMap;
    private PermissionsManager permissionsManager;
    private LocationComponent locationComponent;
    private DirectionsRoute currentRoute;
    private NavigationMapRoute navigationMapRoute;
    private String geoJsonSourceLayerId = "GeoJsonSourceLayerId";
    private String symbolIconId="SymbolIconId";

    private Button btnStartNavigation;
    private FloatingActionButton fabDeleteBookmark;

    private String id, name;
    private double lat, lng;
    private TextView tvName, tvLat, tvLng, tvDistance, tvDuration;
    private ProgressBar progressBar;
    private AlertDialog.Builder alertBuilder;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private String userUid;

    //shared prefs
    public SharedPreferences sharedPreferences;
    public static String userpref = "mypref";
    public static final String unitskey = "unitkey";
    public static final String landemarkkey = "landmarkpref";
    public static final String languagekey = "languagepref";

    private String landmarkPreference;
    private String measurementSystem;
    private String languagePreference;

    private boolean locationPerms = false;

    private final String TAG = "BookmarkViewActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Mapbox.getInstance(this,getString(R.string.mapbox_access_token));
        setContentView(R.layout.activity_bookmark_view);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        id = getIntent().getStringExtra("bId");
        name =  getIntent().getStringExtra("bName");
        lat =  Double.parseDouble(getIntent().getStringExtra("bLat"));
        lng =  Double.parseDouble(getIntent().getStringExtra("bLng"));

        tvName = findViewById(R.id.tvBookmarkTitle);
        tvLat = findViewById(R.id.tvBookmarkLat);
        tvLng = findViewById(R.id.tvBookmarkLng);
        tvDuration = findViewById(R.id.tvBookmarkDuration);
        tvDistance = findViewById(R.id.tvBookmarkDistance);
        fabDeleteBookmark = findViewById(R.id.fabDeleteBookmark);
        progressBar = findViewById(R.id.bookmarkViewProgressBar);

        //Firebase instantiations
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        userUid = mAuth.getCurrentUser().getUid();

        //shared prefs
        userpref = mAuth.getUid();
        sharedPreferences = getSharedPreferences(userpref, Context.MODE_PRIVATE);

        try {
            measurementSystem = sharedPreferences.getString(unitskey,"");
            landmarkPreference = sharedPreferences.getString(landemarkkey,"");
            languagePreference = sharedPreferences.getString(languagekey,"");
        } catch (Exception ex){
            Log.e(TAG, "Couldn't read ");
            ex.printStackTrace();
        }


        tvName.setText(name);
        tvLat.setText("" + lat);
        tvLng.setText("" + lng);

        //start map
        mapView = findViewById(R.id.mapView);
        mapView.getMapAsync(this);

        fabDeleteBookmark.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder deleteDialog =
                        new AlertDialog.Builder(BookmarkViewActivity.this);

                deleteDialog.setTitle("Delete Bookmark...");
                deleteDialog.setMessage("Are you sure you would like to delete this bookmark?");

                deleteDialog.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        progressBar.setVisibility(View.VISIBLE);

                        db.collection("users")
                                .document(userUid)
                                .collection("bookmarks")
                                .document(id)
                                .delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        progressBar.setVisibility(View.GONE);
                                        Toast.makeText(BookmarkViewActivity.this, "Bookmark Deleted", Toast.LENGTH_SHORT).show();

                                        Intent intent = new Intent(BookmarkViewActivity.this, NavMain.class);
                                        startActivity(intent);
                                        finish();
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        progressBar.setVisibility(View.GONE);
                                        simpleAlert("Delete Error", "Unable to delete bookmark, please try again later...");
                                    }
                                });
                    }
                });
                deleteDialog.setNegativeButton("Cancel", null);

                deleteDialog.show();
            }
        });
    }

    @Override
    public void onMapReady(@NonNull MapboxMap mapboxMap) {
        this.mapboxMap = mapboxMap;
        mapboxMap.setStyle(Style.MAPBOX_STREETS, style -> {
            enableLocationComponent(style);
            addDestinationIconSymbolLayer(style);

            generateRoute();

            btnStartNavigation = findViewById(R.id.btnStart);
            btnStartNavigation.setOnClickListener(v -> {
                boolean simulateRoute = false;
                NavigationLauncherOptions options = NavigationLauncherOptions.builder()
                        .directionsRoute(currentRoute)
                        .shouldSimulateRoute(simulateRoute)
                        .build();

                // Call this method with Context from within an activity
                NavigationLauncher.startNavigation(this, options);
           });

            setUpSource(style);

            setUpLayer(style);

            Drawable drawable = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_baseline_location_on_24, null);
            Bitmap bitmap = BitmapUtils.getBitmapFromDrawable(drawable);
            style.addImage(symbolIconId, bitmap);
        });
    }

    private void setUpLayer(Style loadedMapStyle) {
        loadedMapStyle.addLayer(new SymbolLayer("SYMBOL_LAYER_ID", geoJsonSourceLayerId)
                .withProperties(iconImage(symbolIconId), iconOffset(new Float[]{0f, -8f})));
    }

    private void setUpSource(Style loadedMapStyle) {
        loadedMapStyle.addSource(new GeoJsonSource(geoJsonSourceLayerId));
    }

    private void addDestinationIconSymbolLayer(Style loadedMapStyle) {
        loadedMapStyle.addImage("destination-icon-id", BitmapFactory.decodeResource(
                this.getResources(), com.mapbox.mapboxsdk.R.drawable.mapbox_marker_icon_default));
        GeoJsonSource geoJsonSource = new GeoJsonSource("destination-source-id");
        loadedMapStyle.addSource(geoJsonSource);

        SymbolLayer destinationSymbolLayer = new SymbolLayer("destination-symbol-layer-id", "destination-source-id");
        destinationSymbolLayer.withProperties(iconImage("destination-icon-id"),
                iconAllowOverlap(true),
                iconIgnorePlacement(true));
        loadedMapStyle.addLayer(destinationSymbolLayer);
    }

    private void generateRoute() {
        Point destinationPoint = Point.fromLngLat(lng, lat);
        Point originPoint = Point.fromLngLat(locationComponent.getLastKnownLocation().getLongitude(),
                locationComponent.getLastKnownLocation().getLatitude());

        GeoJsonSource source = mapboxMap.getStyle().getSourceAs("destination-source-id");
        if (source != null){
            source.setGeoJson(Feature.fromGeometry(destinationPoint));
        }

        NavigationRoute.builder(this)
                .accessToken(Mapbox.getAccessToken())
                .origin(originPoint)
                .destination(destinationPoint)
                .voiceUnits(measurementSystem)
                .build()
                .getRoute(new Callback<DirectionsResponse>() {
                    @Override
                    public void onResponse(Call<DirectionsResponse> call, Response<DirectionsResponse> response) {
                        // You can get the generic HTTP info about the response
                        Log.d(TAG, "Response code: " + response.code());
                        if (response.body() == null){
                            Log.d(TAG, "No routes found, make sure you set the right user and access token.");
                            return;
                        } else if (response.body().routes().size() < 1){
                            Toast.makeText(BookmarkViewActivity.this, "No Routes Found", Toast.LENGTH_SHORT).show();
                            Log.e(TAG, "No routes found");
                            return;
                        }

                        currentRoute = response.body().routes().get(0);

                        setDistanceAndTime();

                        // Draw the route on the map
                        if (navigationMapRoute != null){
                            navigationMapRoute.removeRoute();
                        }
                        else
                        {
                            navigationMapRoute = new NavigationMapRoute(
                                    null, mapView, mapboxMap,
                                    com.mapbox.services.android.navigation.ui.v5.R.style.NavigationMapRoute);
                        }
                        navigationMapRoute.addRoute(currentRoute);
                    }

                    @Override
                    public void onFailure(Call<DirectionsResponse> call, Throwable t) {
                        Log.e(TAG, "Error: " + t.getMessage());
                    }
                });
    }

    public void setDistanceAndTime(){
        Double distInMeters = currentRoute.distance();
        Double dist;
        if (measurementSystem.equals("metric"))
        {
            if (distInMeters > 10000){
                dist = distInMeters/1000;
                dist = Double.valueOf(Math.round(dist));
                tvDistance.setText(dist.intValue() + " km");
            } else {
                dist = distInMeters/10;
                dist = Double.valueOf(Math.round(dist));
                dist /= 100;
                tvDistance.setText(dist + " km");
            }
        } else {
            dist = distInMeters/1609.33;
            if (distInMeters > 16090){
                dist = Double.valueOf(Math.round(dist));
                tvDistance.setText(dist.intValue() + " miles");
            } else {
                dist *= 100;
                dist = Double.valueOf(Math.round(dist));
                dist /= 100;
                tvDistance.setText(dist + " miles");
            }
        }

        Double time = currentRoute.duration()/60;
        if (time % 10 >= 5){
            time ++;
        }
        time = Double.valueOf(Math.round(time));

        String duration;
        if (time > 60){
            int hours = (int)(time/60);
            int mins = time.intValue() - (hours * 60);

            duration = hours + "h " + mins + " m";
        } else {
            duration = time.intValue() + " mins";
        }
        tvDuration.setText(duration);
    }

    public void simpleAlert(String title, String message)
    {
        alertBuilder.setTitle(title).
                setMessage(message).
                setCancelable(false).
                setPositiveButton("Ok", null);

        alertBuilder.show();
    }

    @SuppressLint("MissingPermission")
    private void enableLocationComponent(Style loadedMapStyle) {

        if (PermissionsManager.areLocationPermissionsGranted(this))
        {
            locationPerms = true;
            Log.d(TAG, "Location should be showing");
            locationComponent = mapboxMap.getLocationComponent();
            locationComponent.activateLocationComponent(this, loadedMapStyle);
            locationComponent.setLocationComponentEnabled(true);

            //set the components camera
            locationComponent.setCameraMode(CameraMode.TRACKING);
        } else
        {
            permissionsManager = new PermissionsManager(this);
            permissionsManager.requestLocationPermissions(this);
            Log.d(TAG, "Location permission not granted");
        }
    }

    @Override
    public void onExplanationNeeded(List<String> permissionsToExplain) {
        Toast.makeText(this, R.string.user_location_permission_explanation, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onPermissionResult(boolean granted) {
        if (granted){
            enableLocationComponent(mapboxMap.getStyle());
        } else {
            Toast.makeText(this, R.string.user_location_permission_not_granted, Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    //region onStart, onResume, onPause etc
    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }
    //endregion
}