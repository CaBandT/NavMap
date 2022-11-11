package com.example.navmap.ui;

import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconAllowOverlap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconIgnorePlacement;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconImage;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconOffset;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;

import com.example.navmap.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.api.directions.v5.models.DirectionsResponse;
import com.mapbox.api.directions.v5.models.DirectionsRoute;
import com.mapbox.api.geocoding.v5.models.CarmenFeature;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.location.LocationComponent;
import com.mapbox.mapboxsdk.location.modes.CameraMode;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.plugins.places.autocomplete.PlaceAutocomplete;
import com.mapbox.mapboxsdk.plugins.places.autocomplete.model.PlaceOptions;
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


public class ExploreFragment extends Fragment implements OnMapReadyCallback, PermissionsListener, MapboxMap.OnMapClickListener {

    private Activity activity;
    private Bundle _savedInstanceState;
    private static final int REQUEST_CODE_AUTOCOMPLETE = 7171;
    private MapView mapView;
    private MapboxMap mapboxMap;
    private PermissionsManager permissionsManager;
    private LocationComponent locationComponent;
    private DirectionsRoute currentRoute;
    private NavigationMapRoute navigationMapRoute;
    private Button btnStartNavigation;
    private FloatingActionButton fabLocationSearch, fabTrackUser, fabBookmarkLocation;

    private String geoJsonSourceLayerId = "GeoJsonSourceLayerId";
    private String symbolIconId = "SymbolIconId";

    //region Variables required for layers
    //fab buttons
    private FloatingActionButton fabMenu, fabStreet, fabSatellite, fabTraffic;

    //booleans to keep keep track of active map style
    private boolean clicked = false;
    private boolean street = true;
    private boolean satellite = false;
    private boolean traffic = false;
    private boolean locationPerms = false;

    //animations for opening and close fab menu
    private Animation rotateOpen, rotateClose, fromBottom, toBottom;
    //endregion

    private final String TAG = "fragment_explore";

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        activity = getActivity();
        _savedInstanceState = savedInstanceState;

        Mapbox.getInstance(activity, getString(R.string.mapbox_access_token));

        return inflater.inflate(R.layout.fragment_explore, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();

        //start map
        mapView = activity.findViewById(R.id.mapView);
        mapView.onCreate(_savedInstanceState);
        mapView.getMapAsync(this);

        //region instantiations and set up required for layers
        //fab animations
        rotateOpen = AnimationUtils.loadAnimation(activity, R.anim.rotate_open_anim);
        rotateClose = AnimationUtils.loadAnimation(activity, R.anim.rotate_close_anim);
        fromBottom = AnimationUtils.loadAnimation(activity, R.anim.from_bottom_anim);
        toBottom = AnimationUtils.loadAnimation(activity, R.anim.to_bottom_anim);

        fabMenu = activity.findViewById(R.id.fabLayerMenu);
        fabStreet = activity.findViewById(R.id.fabStreet);
        fabSatellite = activity.findViewById(R.id.fabSatellite);
        fabTraffic = activity.findViewById(R.id.fabTraffic);

        fabTrackUser = activity.findViewById(R.id.fabGoToLocation);
        fabBookmarkLocation = activity.findViewById(R.id.fabBookmarkLocation);

        fabStreet.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(activity, R.color.white)));
        fabSatellite.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(activity, R.color.grey)));
        fabTraffic.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(activity, R.color.grey)));
        fabBookmarkLocation.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(activity, R.color.semiTransparentGrey)));
        //endregion

        //region layer Fab onClickListeners
        fabMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onMenuButtonClicked();
            }
        });
        fabStreet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (street)
                    streetOff();
                else
                    streetOn();
            }
        });
        fabSatellite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (satellite)
                    satelliteOff();
                else
                    satelliteOn();
            }
        });
        fabTraffic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (traffic)
                    trafficOff();
                else
                    trafficOn();
            }
        });
        //endregion

        //region Track Location onClickListener
        fabTrackUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (locationPerms) {
                    //set the components camera
                    locationComponent.setCameraMode(CameraMode.TRACKING_GPS_NORTH);
                } else {
                    Toast.makeText(activity, "Unable to Track location", Toast.LENGTH_SHORT).show();
                }
            }
        });
        //endregion
        
        fabBookmarkLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(activity, "Save Location Here", Toast.LENGTH_SHORT).show();
            }
        });
    }

    //region layer methods
    private void onMenuButtonClicked()
    {
        setVisibility(clicked);
        setAnimation(clicked);
        clicked = !clicked;
    }

    private void setVisibility(boolean clicked) {
        if (!clicked)
        {
            fabStreet.setVisibility(View.VISIBLE);
            fabSatellite.setVisibility(View.VISIBLE);
            fabTraffic.setVisibility(View.VISIBLE);

            fabStreet.setEnabled(true);
            fabSatellite.setEnabled(true);
            fabTraffic.setEnabled(true);
        } else
        {
            fabStreet.setVisibility(View.INVISIBLE);
            fabSatellite.setVisibility(View.INVISIBLE);
            fabTraffic.setVisibility(View.INVISIBLE);

            fabStreet.setEnabled(false);
            fabSatellite.setEnabled(false);
            fabTraffic.setEnabled(false);
        }
    }

    private void setAnimation(boolean clicked) {
        if (!clicked)
        {
            fabStreet.startAnimation(fromBottom);
            fabSatellite.startAnimation(fromBottom);
            fabTraffic.startAnimation(fromBottom);
            fabMenu.startAnimation(rotateOpen);
        } else
        {
            fabStreet.startAnimation(toBottom);
            fabSatellite.startAnimation(toBottom);
            fabTraffic.startAnimation(toBottom);
            fabMenu.startAnimation(rotateClose);
        }
    }

    private void changeMapStyle(int val)
    {
        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(@NonNull MapboxMap mapboxMap) {
                switch (val)
                {
                    case 1: mapboxMap.setStyle(Style.SATELLITE_STREETS); break;
                    case 2: mapboxMap.setStyle(Style.SATELLITE); break;
                    case 3: mapboxMap.setStyle(Style.TRAFFIC_DAY); break;
                    default: mapboxMap.setStyle(Style.MAPBOX_STREETS); break;
                }
            }
        });
    }

    //region methods to enable and disable layers
    private void streetOff()
    {
        if (satellite) {
            street = false;
            fabStreet.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(activity, R.color.grey)));
            changeMapStyle(2);
        }
    }

    private void streetOn()
    {
        street = true;
        fabStreet.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(activity, R.color.white)));

        if (satellite)
            changeMapStyle(1);
        else
            changeMapStyle(0);
    }

    private void satelliteOff()
    {
        satellite = false;
        fabSatellite.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(activity, R.color.grey)));

        street = true;
        fabStreet.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(activity, R.color.white)));

        changeMapStyle(0);
    }

    private void satelliteOn()
    {
        satellite = true;
        fabSatellite.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(activity, R.color.white)));

        street = true;
        fabStreet.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(activity, R.color.white)));

        traffic = false;
        fabTraffic.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(activity, R.color.grey)));

        changeMapStyle(1);
    }

    private void trafficOff()
    {
        traffic = false;
        fabTraffic.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(activity, R.color.grey)));
        changeMapStyle(0);
    }

    private void trafficOn()
    {
        traffic = true;
        fabTraffic.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(activity, R.color.white)));

        street = true;
        fabStreet.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(activity, R.color.white)));

        satellite = false;
        fabSatellite.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(activity, R.color.grey)));

        changeMapStyle(3);
    }
    //endregion
    //endregion

    @Override
    public void onMapReady(@NonNull MapboxMap mapboxMap) {
        this.mapboxMap = mapboxMap;
        mapboxMap.setStyle(Style.MAPBOX_STREETS, style -> {
            enableLocationComponent(style);
            addDestinationIconSymbolLayer(style);
            mapboxMap.addOnMapClickListener(this);

            btnStartNavigation = activity.findViewById(R.id.btnStart);
            btnStartNavigation.setOnClickListener(v -> {
                boolean simulateRoute = false;
                NavigationLauncherOptions options = NavigationLauncherOptions.builder()
                        .directionsRoute(currentRoute)
                        .shouldSimulateRoute(simulateRoute)
                        .build();

                // Call this method with Context from within an activity
                NavigationLauncher.startNavigation(activity, options);
            });

            initSearchFab();

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

    private void initSearchFab() {
        fabLocationSearch = activity.findViewById(R.id.fab_location_search);
        fabLocationSearch.setOnClickListener(view -> {
            Intent intent = new PlaceAutocomplete.IntentBuilder()
                    .accessToken(Mapbox.getAccessToken() != null ? Mapbox.getAccessToken() : getString(R.string.mapbox_access_token))
                    .placeOptions(PlaceOptions.builder()
                            .backgroundColor(Color.parseColor("#EEEEEE"))
                            .limit(10)
                            .build(PlaceOptions.MODE_CARDS))
                    .build(activity);

            startActivityForResult(intent, REQUEST_CODE_AUTOCOMPLETE);
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_CODE_AUTOCOMPLETE){
            // Retrieve selected location's CarmenFeature
            CarmenFeature selectedCarmenFeature = PlaceAutocomplete.getPlace(data);

            // Create a new FeatureCollection and add a new Feature to it using selectedCarmenFeature
            // Then retrieve and update the source designated for showing a selected location's symbol layer icon
            if (mapboxMap != null){
                Style style = mapboxMap.getStyle();
                if (style != null){
                    GeoJsonSource source = style.getSourceAs(geoJsonSourceLayerId);
                    if (source != null){
                        source.setGeoJson(FeatureCollection.fromFeatures(
                                new Feature[]{Feature.fromJson(selectedCarmenFeature.toJson())}));
                    }

                    // Move the camera to the selected location
                    mapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition.Builder()
                            .target(new LatLng(
                                    ((Point) selectedCarmenFeature.geometry()).latitude(),
                                    ((Point) selectedCarmenFeature.geometry()).longitude()))
                            .zoom(14)
                            .build()), 4000);
                }
            }
        }
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

    @Override
    public boolean onMapClick(@NonNull LatLng point) {
        Point destinationPoint = Point.fromLngLat(point.getLongitude(), point.getLatitude());
        Point originPoint = Point.fromLngLat(locationComponent.getLastKnownLocation().getLongitude(),
                locationComponent.getLastKnownLocation().getLatitude());

        GeoJsonSource source = mapboxMap.getStyle().getSourceAs("destination-source-id");
        if (source != null){
            source.setGeoJson(Feature.fromGeometry(destinationPoint));
        }

        getRoute(originPoint, destinationPoint);
        //enable nav btn
        btnStartNavigation.setEnabled(true);
        btnStartNavigation.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(activity, R.color.darkGreen)));
        //enable
        fabBookmarkLocation.setEnabled(true);
        fabBookmarkLocation.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(activity, R.color.white)));
        return true;
    }

    private void getRoute(Point origin, Point destination) {
        NavigationRoute.builder(activity)
                .accessToken(Mapbox.getAccessToken())
                .origin(origin)
                .destination(destination)
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
                            Log.e(TAG, "No routes found");
                            return;
                        }

                        currentRoute = response.body().routes().get(0);

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

    @SuppressLint("MissingPermission")
    private void enableLocationComponent(Style loadedMapStyle) {
        if (PermissionsManager.areLocationPermissionsGranted(activity)) {
            locationPerms = true;
            Log.d(TAG, "Location should be showing");
            locationComponent = mapboxMap.getLocationComponent();
            locationComponent.activateLocationComponent(activity, loadedMapStyle);
            locationComponent.setLocationComponentEnabled(true);

            //set the components camera
            locationComponent.setCameraMode(CameraMode.TRACKING);
        } else
        {
            permissionsManager = new PermissionsManager(this);
            permissionsManager.requestLocationPermissions(activity);
            Log.d(TAG, "Location permission not granted");
        }
    }

    @Override
    public void onExplanationNeeded(List<String> list) {
        Toast.makeText(activity,R.string.user_location_permission_explanation, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onPermissionResult(boolean granted) {
        if (granted){
            enableLocationComponent(mapboxMap.getStyle());
        } else {
            Toast.makeText(activity, R.string.user_location_permission_not_granted, Toast.LENGTH_SHORT).show();
            //finish();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public  void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public  void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    public  void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    public  void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }



}