package za.co.we_party.weparty;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import za.co.we_party.weparty.Adapters.ClubAdapter;
import za.co.we_party.weparty.Adapters.ClubsAdapter;
import za.co.we_party.weparty.Models.Club;
import za.co.we_party.weparty.Models.ClubModel;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, ClubAdapter.OnClubSelectedListener {

    public GoogleMap wePartyMap;
    private boolean isWePartyMapReady = false;
    private boolean isClubMarkersAdded = false;
    SupportMapFragment mapFragment;
    public BottomNavigationView navigation;
    private static final String TAG = MainActivity.class.getSimpleName();

    //---- Bottom sheet ------
    BottomSheetBehavior sheetBehavior;
    LinearLayout bottomSheetLayout;

    //firestore
    private FirebaseFirestore mFirestore;
    private Query mQuery;

    private FilterDialogFragment mFilterDialog;
    private ClubAdapter mAdapter;

    //firebase
    FirebaseDatabase database;
    public DatabaseReference reference;

    //holds all the clubs from database
    ArrayList<ClubModel> allClubs = new ArrayList<>();
    //Collection of clubs
    public LinkedHashMap<Integer, Marker> ClubMarkers = new LinkedHashMap<>();
    ArrayList<Integer> ClubIDs = new ArrayList<>();
    //circle around club marker
    public static Circle clubMarkerCircle;

    //recycler view for user nearby clubs
    public RecyclerView userNearbyClubs;
    //nearby clubs adapter
    public ClubsAdapter nearbyAdapter;

    //Butter knife view binding
    @BindView(R.id.nearbyRV)
    RecyclerView mNearbyRecycler;

    @BindView(R.id.view_empty)
    ViewGroup mEmptyView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        supportMapFragment();
        setupUI();

        // Enable Firestore logging
        FirebaseFirestore.setLoggingEnabled(true);

        // Initialize Firestore and the main RecyclerView
        initFirestore();
        initRecyclerView();

        initFirebase();
    }

    private void initFirebase() {

        database = FirebaseDatabase.getInstance();
        reference = database.getReference("clubs/clubIDs/");//Todo: set reference to user location

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot != null) {

                    onDataChangeUpdater(dataSnapshot);

                } else {
                    Toast.makeText(getApplicationContext(), "Database Error!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

                Toast.makeText(getApplicationContext(), "Terminating Process!", Toast.LENGTH_SHORT).show();
            }
        });

        reference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                final ClubModel changedClubChild = dataSnapshot.getValue(ClubModel.class);
/*                if (TrainMarkers.containsKey(changedGeoTrain.getTrain_id())) {

                    System.out.println("--------------The marker exists----------------");

                    //Todo: make a smooth transition from one point to another
                    final Marker updatedTrain = TrainMarkers.get(changedGeoTrain.getTrain_id());
                    updatedTrain.setPosition(new LatLng(changedGeoTrain.getCurrent_latitude(), changedGeoTrain.getCurrent_longitude()));
                    geoQuery = geoFire.queryAtLocation(new GeoLocation(changedGeoTrain.getCurrent_latitude(), changedGeoTrain.getCurrent_longitude()), 0.5f);
                    geoQueryListener(geoQuery);

                    //TODO: WRITE A FOR LOOP TO ITERATE THROUGH geoTrainsList AND CHECK IF THE NEW POSITIONS
                    // TODO ARE NOT GEO QUERIED FOR STATION NOTIFICATION

                    if (TrackingMarkerIsAnimating && clickedGeoTrain.getTrain_id() == changedGeoTrain.getTrain_id()) {
                        isNewPosition = true;
                        clickedGeoTrain = changedGeoTrain;
                    }

                } else {
                    //Todo : write a function to update the hash map with the newly added train
                }*/

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void onDataChangeUpdater(DataSnapshot dataSnapshot) {

        //places club marker whe the map is ready
        if (isWePartyMapReady) {

            if (!isClubMarkersAdded) {
                //place the markers

                for (DataSnapshot Trainsnapshot : dataSnapshot.getChildren()) {

                    final ClubModel club = Trainsnapshot.getValue(ClubModel.class);
                    allClubs.add(club);

                    final LatLng latLng = new LatLng(club.getLatitude(), club.getLongitude());

                    Marker geoClubMarker = wePartyMap.addMarker(new MarkerOptions()
                            .position(latLng)
                            .title(club.getClubName())
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_club_marker)));

                    clubMarkerCircle = wePartyMap.addCircle(new CircleOptions()
                            .center(geoClubMarker.getPosition())
                            .radius(450)
                            .strokeWidth(6)
                            .strokeColor(Color.rgb(35, 40, 58))
                            .fillColor(Color.argb(128, 35, 40, 58))
                            .clickable(true));

                    //add the club in to the clubs hash map.
                    ClubMarkers.put(club.getClubID(), geoClubMarker);
                    //Add the club ID so we can navigate, show data about different clubs later. or search by club ID
                    ClubIDs.add(club.getClubID());
                }

                isClubMarkersAdded = true;

                nearbyAdapter.populateclubList(allClubs);


            } else {

                //update the makers
            }

        } else {
            //todo toast map not ready
        }
        System.out.println(dataSnapshot);
        System.out.println("---------------------");
    }

    private void initFirestore() {

        mFirestore = FirebaseFirestore.getInstance();

/*        //sets the firestore recommended time stamp
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setTimestampsInSnapshotsEnabled(true)
                .build();
        mFirestore.setFirestoreSettings(settings);*/

        // Get the 50 highest rated restaurants
        mQuery = mFirestore.collection("restaurants")
                .orderBy("avgRating", Query.Direction.DESCENDING)
                .limit(50);
    }

    private void initRecyclerView() {
/*
        if (mQuery == null) {
            Log.w(TAG, "No query, not initializing RecyclerView");
        }

        mAdapter = new ClubAdapter(mQuery, this) {

            @Override
            protected void onDataChanged() {
                // Show/hide content if the query returns empty.
                if (getItemCount() == 0) {
                   // mNearbyRecycler.setVisibility(View.GONE);
                   // mEmptyView.setVisibility(View.VISIBLE);
                } else {
                   // mNearbyRecycler.setVisibility(View.VISIBLE);
                   // mEmptyView.setVisibility(View.GONE);
                }
            }

            @Override
            protected void onError(FirebaseFirestoreException e) {
                // Show a snackbar on errors
                Snackbar.make(findViewById(android.R.id.content),
                        "Error: check logs for info.", Snackbar.LENGTH_LONG).show();
            }
        };

        mNearbyRecycler.setLayoutManager(new LinearLayoutManager(this));
        mNearbyRecycler.setAdapter(mAdapter);*/
    }

    @Override
    public void onClubSelected(DocumentSnapshot restaurant) {
        // Go to the details page for the selected club
        Intent intent = new Intent(this, ClubDetailActivity.class);
        intent.putExtra(ClubDetailActivity.KEY_RESTAURANT_ID, restaurant.getId());

        startActivity(intent);
    }

    @Override
    public void onStart() {
        super.onStart();

        // Start sign in if necessary
/*        if (shouldStartSignIn()) {
            startSignIn();
            return;
        }*/

        // Apply filters
        // onFilter(mViewModel.getFilters());

        // Start listening for Firestore updates
        if (mAdapter != null) {
            mAdapter.startListening();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAdapter != null) {
            mAdapter.stopListening();
        }
    }

    private void setupUI() {

        bottomSheetLayout = findViewById(R.id.bottom_sheet);
        sheetBehavior = BottomSheetBehavior.from(bottomSheetLayout);
        sheetBehavior.setPeekHeight(180);
        bottomsheetcallback();
        sheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);



        nearbyAdapter = new ClubsAdapter();
       // nearbyAdapter.setItemsOnClickListener(this);
        mNearbyRecycler.setAdapter(nearbyAdapter);
        mNearbyRecycler.setHasFixedSize(true);
        LinearLayoutManager layoutManager
                = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        mNearbyRecycler.setLayoutManager(layoutManager);
      //  mNearbyRecycler.setVisibility(View.GONE); only gone when user location is not defined
    }


    private void bottomsheetcallback() {

        sheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                switch (newState) {
                    case BottomSheetBehavior.STATE_HIDDEN: {


                    }
                    break;
                    case BottomSheetBehavior.STATE_EXPANDED: {


                    }
                    break;
                    case BottomSheetBehavior.STATE_COLLAPSED: {


                    }
                    break;
                    case BottomSheetBehavior.STATE_DRAGGING:
                        break;
                    case BottomSheetBehavior.STATE_SETTLING:
                        break;
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {

            }
        });
    }

    //Bottom navigation
    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.map:

                    return true;

                case R.id.updates:


                    return false;

                case R.id.profile:


                    return false;

                case R.id.free_geo_taxi_ride:


                    return false;

                case R.id.main_manu:

                    return false;
            }

            return false;
        }
    };

    private void supportMapFragment() {

        /* Get the SupportMapFragment and request notification
           when the map is ready to be used. */
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        wePartyMap = googleMap;
        isWePartyMapReady = true;

        try {
            // Customise the styling of the base map using a JSON object defined
            // in a raw resource file.
            boolean success = googleMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                            this, R.raw.styles_json));

            if (!success) {
                Log.e(TAG, "Style parsing failed.");
            }
        } catch (Resources.NotFoundException e) {
            Log.e(TAG, "Can't find style. Error: ", e);
        }


        wePartyMap.getUiSettings().setCompassEnabled(false);
        wePartyMap.getUiSettings().setZoomGesturesEnabled(true);
        wePartyMap.getUiSettings().setMyLocationButtonEnabled(true);
        wePartyMap.getUiSettings().setTiltGesturesEnabled(false);
        wePartyMap.setMinZoomPreference(9.9f);

        LatLng userLocation = new LatLng(-26.00584, 28.24866);
        wePartyMap.moveCamera(CameraUpdateFactory.newLatLng(userLocation));
    }
}
