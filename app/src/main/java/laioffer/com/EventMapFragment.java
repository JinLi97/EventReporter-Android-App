package laioffer.com;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class EventMapFragment extends Fragment implements OnMapReadyCallback, GoogleMap.OnInfoWindowClickListener,GoogleMap.OnMarkerClickListener {

    private MapView mMapView;
    private View mView;
    private DatabaseReference databaseReference;
    private List<Event> events;
    private GoogleMap mGoogleMap;
    private Marker lastClicked;
    private Marker curLocationMarker;


    public EventMapFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_event_map, container, false);
        databaseReference = FirebaseDatabase.getInstance().getReference();
        events = new ArrayList<>();
        return mView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mMapView = (MapView)mView.findViewById(R.id.event_map_view);
        if (mMapView != null) {
            mMapView.onCreate(null);
            mMapView.onResume();
            mMapView.getMapAsync(this);
        }
    }


    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    public void onPause(){
        super.onPause();
        mMapView.onPause();
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        mMapView.onDestroy();
    }

    @Override
    public void onLowMemory(){
        super.onLowMemory();
        mMapView.onLowMemory();
    }

    @Override
    public void onMapReady(GoogleMap googleMap){
        MapsInitializer.initialize(getContext());

        mGoogleMap = googleMap;
        mGoogleMap.setOnInfoWindowClickListener(this);
        mGoogleMap.setOnMarkerClickListener(this);
        final LocationTracker locationTracker = new LocationTracker(getActivity());
        locationTracker.getLocation();
        double curlatitude = locationTracker.getLatitude();
        double curlongitude = locationTracker.getLongitude();

        MarkerOptions marker = new MarkerOptions().position(new LatLng(curlatitude, curlongitude)).title("your location");
        marker.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
        curLocationMarker = googleMap.addMarker(marker);
        //set up camera configuration
        CameraPosition cameraPosition = new CameraPosition.Builder().target(
                new LatLng(curlatitude, curlongitude)).zoom(12).build();

        //Animate the zoom process
        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

        setUpMarkersCloseToCurLocation(googleMap, curlatitude, curlongitude);
    }

    private void setUpMarkersCloseToCurLocation(final GoogleMap googleMap, final double curLatitude, final double curLongitude) {
        events.clear();
        databaseReference.child("events").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Event event = snapshot.getValue(Event.class);
                    double desLatitude = event.getLatitude();
                    double desLongitude = event.getLongitude();
                    int distance = Utils.distanceBetweenTwoLocations(curLatitude,curLongitude,desLatitude,desLongitude);
                    if (distance <= 10) {
                        events.add(event);
                    }
                }

                //Set up every events
                for (Event event :events) {
                    //create marker
                    MarkerOptions marker = new MarkerOptions().position(
                            new LatLng(event.getLatitude(), event.getLongitude())).title(event.getTitle());

                    //Changing marker icon
                    marker.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ROSE));

                    //add marker
                    Marker mker = googleMap.addMarker(marker);
                    mker.setTag(event);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        Event event = (Event)marker.getTag();
        Intent intent = new Intent(getContext(), CommentActivity.class);
        String eventId = event.getId();
        intent.putExtra("EventID", eventId);
        getContext().startActivity(intent);
    }

    @Override
    public boolean onMarkerClick(final Marker marker){
        if (marker.equals(curLocationMarker)){
            marker.showInfoWindow();
            return true;
        }
        final Event event = (Event)marker.getTag();
        if (lastClicked != null && lastClicked.equals(marker)) {
            lastClicked = null;
            marker.hideInfoWindow();
            marker.setIcon(null);
            return true;
        }
        else {
            lastClicked = marker;
            new AsyncTask<Void, Void, Bitmap>() {
                @Override
                protected Bitmap doInBackground(Void... voids) {
                    Bitmap bitmap = Utils.getBitmapFromURL(event.getImgUri());
                    return bitmap;
                }

                @Override
                protected void onPostExecute(Bitmap bitmap) {
                    super.onPostExecute(bitmap);
                    if (bitmap != null) {
                        marker.setIcon(BitmapDescriptorFactory.fromBitmap(bitmap));
                        marker.setTitle(event.getTitle());
                    }
                }
            }.execute();
            return false;
        }
    }
}