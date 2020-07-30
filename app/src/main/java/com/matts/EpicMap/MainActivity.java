package com.matts.EpicMap;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.api.directions.v5.models.DirectionsResponse;
import com.mapbox.api.directions.v5.models.DirectionsRoute;
import com.mapbox.api.geocoding.v5.GeocodingCriteria;
import com.mapbox.api.geocoding.v5.MapboxGeocoding;
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
import com.mapbox.services.android.navigation.ui.v5.NavigationLauncher;
import com.mapbox.services.android.navigation.ui.v5.NavigationLauncherOptions;
import com.mapbox.services.android.navigation.ui.v5.route.NavigationMapRoute;
import com.mapbox.services.android.navigation.v5.navigation.NavigationRoute;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.annotation.Nullable;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconAllowOverlap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconIgnorePlacement;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconImage;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, MapboxMap.OnMapClickListener, PermissionsListener {
    // variables for adding location layer
    private MapView mapView;
    private MapboxMap mapboxMap;
    // variables for adding location layer
    private PermissionsManager permissionsManager;
    private LocationComponent locationComponent;
    // variables for calculating and drawing a route
    private DirectionsRoute currentRoute;
    private String destination;
    private static final String TAG = "DirectionsActivity";
    private NavigationMapRoute navigationMapRoute;
    // variables needed to initialize navigation
    private Button button, buttonProfile, btnSearch, btnHistory;

    FirebaseAuth firebaseAuth;
    FirebaseFirestore FStore;
    MapHelper mh;
    DBHelper db;

    private String geojsonSourceLayerId = "geojsonSourceLayerId";

    public static String system, transMethod, userID;
    private static Point originPoint, destinationPoint;
    private GeoJsonSource source;
    private static LatLng currentPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Mapbox.getInstance(this, "pk.eyJ1IjoibWF0dHNwZW5jZXIiLCJhIjoiY2s2MHFsaXowMDl3OTNtbnhic2h4bzRqdiJ9.I3Lh1asF_BAtkWyyRm41xA");

        setContentView(R.layout.activity_main);
        buttonProfile = findViewById(R.id.btnProfile);
        btnSearch = findViewById(R.id.btnSearch);
        mapView = findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        mh = new MapHelper();
        db = new DBHelper(this);

        btnHistory = findViewById(R.id.btnHistory);

        firebaseAuth = FirebaseAuth.getInstance();
        FStore = FirebaseFirestore.getInstance();

        userID = firebaseAuth.getCurrentUser().getUid();

        DocumentReference documentReference = FStore.collection("users").document(userID);
        documentReference.addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
            @SuppressLint("LogNotTimber")
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException exception) {

                if (exception != null) {
                    Log.d(TAG, "Error: " + exception.getMessage());
                } else {
                    try {
                        String unit = documentSnapshot.getString("sysPref");
                        String methodOfTransport = documentSnapshot.getString("transPref");

                        system = mh.getSystemMethod(unit);
                        transMethod = mh.getTransportationMethod(methodOfTransport);
                    } catch (Exception ex) {
                        Log.d(TAG, "Error: " + ex.getMessage());
                    }
                }
            }
        });

        buttonProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), ProfileActivity.class));

            }
        });

        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = searchLocation();
                startActivityForResult(intent, 1);
            }
        });

        btnHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent history = new Intent(MainActivity.this, RouteActivity.class);
                startActivity(history);

            }
        });

    }

    public void reverseGeocode() {

        MapboxGeocoding reverseGeocodes = MapboxGeocoding.builder()
                .accessToken("pk.eyJ1IjoibWF0dHNwZW5jZXIiLCJhIjoiY2s2MHFsaXowMDl3OTNtbnhic2h4bzRqdiJ9.I3Lh1asF_BAtkWyyRm41xA")
                .query(String.valueOf(destinationPoint))
                .geocodingTypes(GeocodingCriteria.TYPE_ADDRESS)
                .build();

    }


    private void getCurrentLocation() {

        // Set the current (last known) position of the user, and store it in currentPosition LatLng variable
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        currentPosition = new LatLng(mapboxMap.getLocationComponent().getLastKnownLocation().getLatitude(),
                mapboxMap.getLocationComponent().getLastKnownLocation().getLongitude());

        // Gets the origin point and sets the value of originPoint variable equal to it
        originPoint = Point.fromLngLat(currentPosition.getLongitude(), currentPosition.getLatitude());

    }

    @Override
    public void onMapReady(@NonNull final MapboxMap mapboxMap) {
        this.mapboxMap = mapboxMap;
        mapboxMap.setStyle(getString(R.string.navigation_guidance_day), new Style.OnStyleLoaded() {
            @Override
            public void onStyleLoaded(@NonNull Style style) {
                enableLocationComponent(style);

                addDestinationIconSymbolLayer(style);

                currentPosition = new LatLng(mapboxMap.getCameraPosition().target.getLatitude(),
                        mapboxMap.getCameraPosition().target.getLongitude());

                mapboxMap.addOnMapClickListener(MainActivity.this);
                button = findViewById(R.id.startButton);
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        boolean simulateRoute = true;
                        NavigationLauncherOptions options = NavigationLauncherOptions.builder()
                                .directionsRoute(currentRoute)
                                .shouldSimulateRoute(simulateRoute)
                                .build();
                        // Call this method with Context from within an Activity
                        NavigationLauncher.startNavigation(MainActivity.this, options);

                        insertRoute();

                    }
                });
            }
        });
    }

    private void addDestinationIconSymbolLayer(@NonNull Style loadedMapStyle) {
        loadedMapStyle.addImage("destination-icon-id",
                BitmapFactory.decodeResource(this.getResources(), R.drawable.mapbox_marker_icon_default));
                GeoJsonSource geoJsonSource = new GeoJsonSource("destination-source-id");
                loadedMapStyle.addSource(geoJsonSource);
                SymbolLayer destinationSymbolLayer = new SymbolLayer("destination-symbol-layer-id", "destination-source-id");
                destinationSymbolLayer.withProperties(
                    iconImage("destination-icon-id"),
                    iconAllowOverlap(true),
                    iconIgnorePlacement(true)
                );
        loadedMapStyle.addLayer(destinationSymbolLayer);
    }

    @SuppressWarnings( {"MissingPermission"})
    @Override
    public boolean onMapClick(@NonNull LatLng point) {

        destinationPoint = Point.fromLngLat(point.getLongitude(), point.getLatitude());
        originPoint = Point.fromLngLat(locationComponent.getLastKnownLocation().getLongitude(),
        locationComponent.getLastKnownLocation().getLatitude());
        GeoJsonSource source = mapboxMap.getStyle().getSourceAs("destination-source-id");
         if (source != null) {
            source.setGeoJson(Feature.fromGeometry(destinationPoint));
        }
        //gets the users current location
        getCurrentLocation();
        getRoute(originPoint, destinationPoint);

        button.setEnabled(true);
        return true;
    }

    private void getRoute(Point origin, Point destination) {

        NavigationRoute.builder(this)
                .accessToken("pk.eyJ1IjoibWF0dHNwZW5jZXIiLCJhIjoiY2s2MHFsaXowMDl3OTNtbnhic2h4bzRqdiJ9.I3Lh1asF_BAtkWyyRm41xA")
                .origin(origin)
                .profile(transMethod)
                .destination(destination)
                .voiceUnits(system)
                .build()
                .getRoute(new Callback<DirectionsResponse>() {
                    @Override
                    public void onResponse(Call<DirectionsResponse> call, Response<DirectionsResponse> response) {
                        // You can get the generic HTTP info about the response
                        Log.d(TAG, "Response code: " + response.code());
                        if (response.body() == null) {
                            Log.e(TAG, "No routes found");
                            return;
                        } else if (response.body().routes().size() < 1) {
                            Log.e(TAG, "No routes found");
                            return;
                        }

                        currentRoute = response.body().routes().get(0);

                        // Draw the route on the map
                        if (navigationMapRoute != null) {
                            navigationMapRoute.removeRoute();
                        } else {
                            navigationMapRoute = new NavigationMapRoute(null, mapView, mapboxMap, R.style.NavigationMapRoute);
                        }
                        navigationMapRoute.addRoute(currentRoute);
                    }

                    @Override
                    public void onFailure(Call<DirectionsResponse> call, Throwable throwable) {
                        Log.e(TAG, "Error: " + throwable.getMessage());
                    }
                });
    }

    private void insertRoute(){
        boolean save;

        Date date = Calendar.getInstance().getTime();
        SimpleDateFormat ft= new SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy");
        String currentDateTime = ft.format(date);

        userID = firebaseAuth.getCurrentUser().getUid();

        LatLng originLatLng = new LatLng(originPoint.latitude(), originPoint.longitude());
        LatLng destinationLatLng = new LatLng(destinationPoint.latitude(), destinationPoint.longitude());

        Log.d(TAG, userID);
        Log.d(TAG, currentDateTime);
        Log.d(TAG, String.valueOf(originLatLng));
        Log.d(TAG, String.valueOf(destinationLatLng));

        save = db.tripInsert(userID, currentDateTime, transMethod,  originLatLng, destinationLatLng);

        if (save) {
            Log.d(TAG, "route saved");
            Toast.makeText(this, "Route was saved successfully!", Toast.LENGTH_SHORT).show();
        } else {
            Log.d(TAG, "route was not saved!");
            Toast.makeText(this, "Route was not saved!", Toast.LENGTH_SHORT).show();
        }
    }

    @SuppressLint("WrongConstant")
    @SuppressWarnings( {"MissingPermission"})
    private void enableLocationComponent(@NonNull Style loadedMapStyle) {
        // Check if permissions are enabled and if not request
        if (PermissionsManager.areLocationPermissionsGranted(this)) {
            // Activate the MapboxMap LocationComponent to show user location
            // Adding in LocationComponentOptions is also an optional parameter
            locationComponent = mapboxMap.getLocationComponent();
            locationComponent.activateLocationComponent(this, loadedMapStyle);
            locationComponent.setLocationComponentEnabled(true);
            // Set the component's camera mode
            locationComponent.setCameraMode(CameraMode.TRACKING);
        } else {
            permissionsManager = new PermissionsManager(this);
            permissionsManager.requestLocationPermissions(this);
        }
    }

    // Method for searching via address
    private Intent searchLocation()
    {
        // Gets the current location of the user
        Point pointOfProximity = Point.fromLngLat(currentPosition.getLongitude(), currentPosition.getLatitude());

        // Creates an intent which displays search functionality
        Intent i = new PlaceAutocomplete.IntentBuilder()
                .accessToken(Mapbox.getAccessToken())
                .placeOptions(PlaceOptions.builder()
                        .backgroundColor(Color.parseColor("#ffffff"))
                        .hint("Enter Address/Place")
                        .country(Locale.getDefault())
                        .proximity(pointOfProximity)
                        .geocodingTypes(GeocodingCriteria.TYPE_ADDRESS,
                                GeocodingCriteria.TYPE_POI,
                                GeocodingCriteria.TYPE_PLACE)
                        .limit(5)
                        .build(PlaceOptions.MODE_CARDS))
                .build(MainActivity.this);

        return i;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);


        if (resultCode == MainActivity.RESULT_OK && requestCode == 1) {
            // Gets the location of the chosen search item
            CarmenFeature selectedCarmenFeature = PlaceAutocomplete.getPlace(data);

            // Locates the new destination from the selected search address, and moves the camera to the new destination
            if (mapboxMap != null) {
                Style style = mapboxMap.getStyle();
                if (style != null) {

                    GeoJsonSource geoJsonSource = style.getSourceAs(geojsonSourceLayerId);
                    if (geoJsonSource != null) {
                        geoJsonSource.setGeoJson(FeatureCollection.fromFeatures(
                                new Feature[]{Feature.fromJson(selectedCarmenFeature.toJson())}));
                    }

                    // Gets the current user location
                    getCurrentLocation();

                    // Gets the destination address
                    destinationPoint = (Point) selectedCarmenFeature.geometry();
                    LatLng destination = new LatLng(destinationPoint.latitude(), destinationPoint.longitude());

                    mapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(
                            new CameraPosition.Builder()
                                    .target(destination)
                                    .zoom(15)
                                    .build()), 2000);

                    source = mapboxMap.getStyle().getSourceAs("destination-source-id");

                    if (source != null) {
                        source.setGeoJson(Feature.fromGeometry(destinationPoint));
                    }

                    // Finds the route to the destination
                    getRoute(originPoint, destinationPoint);

                    //enables the start navigation button
                    button.setEnabled(true);

                }
            }
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onExplanationNeeded(List<String> permissionsToExplain) {
        Toast.makeText(this, "", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onPermissionResult(boolean granted) {
        if (granted) {
            enableLocationComponent(mapboxMap.getStyle());
        } else {
            Toast.makeText(this, "", Toast.LENGTH_LONG).show();
            finish();
        }
    }

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
    protected void onSaveInstanceState(Bundle outState) {
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
}
