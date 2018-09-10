package com.bozin.worldtraveler.viewModels;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.util.Log;

import com.bozin.worldtraveler.R;
import com.bozin.worldtraveler.data.PlacesRepository;
import com.bozin.worldtraveler.model.Place;
import com.bozin.worldtraveler.model.User;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;

import io.reactivex.Completable;
import io.reactivex.CompletableEmitter;
import io.reactivex.CompletableOnSubscribe;


public class MainViewModel extends AndroidViewModel {
    private static final String TAG = "viewModel";
    private PlacesRepository mPlacesRepository;
    private LiveData<List<Place>> placesList;
    private LinkedHashMap<Integer, String> countriesVisited;
    private LinkedHashMap<Integer, MarkerOptions> placesHashMap;
    private FirebaseAuth mAuth;

    private FirebaseUser mFireBaseUser;


    public MainViewModel(@NonNull Application application) {
        super(application);
        mPlacesRepository = new PlacesRepository(application);
        placesList = mPlacesRepository.getAllPlaces();
        countriesVisited = new LinkedHashMap<>();
        placesHashMap = new LinkedHashMap<>();
        mAuth = FirebaseAuth.getInstance();
    }

    public LiveData<List<Place>> getPlacesList() {
        if (placesList != null) {
            return placesList;
        } else {
            return mPlacesRepository.getAllPlaces();
        }

    }

    public List<Place> getPlaces() {
        if (placesList != null) {
            return placesList.getValue();
        } else {
            return mPlacesRepository.getAllPlaces().getValue();
        }
    }

    public void insertPlace(Place place) {
        mPlacesRepository.insertPlace(place);
    }

    public void deletePlaceById(int id) {
        mPlacesRepository.deletePlaceByID(id);
    }

    public LinkedHashMap<Integer, MarkerOptions> getPlacesHashMap(List<Place> places, Object... objects) {
        createMarkersFromPlaces(places, objects);
        return placesHashMap;
    }


    //NEED TO CALL THIS FIRST TO INIT PLACES HASHMAP
    public void createMarkersFromPlaces(List<Place> list, Object... objects) {
        if(list != null){
            for (Place place : list) {

                String city = place.getCity_name();
                String country = place.getCountry_name();
                double latitude = place.getLatitude();
                double longitude = place.getLongitude();
                int markerId = place.getPlaceID();
                LatLng latLng = new LatLng(latitude, longitude);

                if (!countriesVisited.containsValue(country)) {
                    countriesVisited.put(markerId, country);
                }

                MarkerOptions markerOptions;

                int customBitmap = objects.length > 0 ? (int) objects[0] : 0;
                Context context = objects.length > 1 ? (Context) objects[1] : null;

                if (customBitmap == 1 && context != null) {
                    Bitmap b = getBitmapFromVectorDrawable(context, R.drawable.ic_location_on_black_24dp);
                    Bitmap bHalfSize = Bitmap.createScaledBitmap(b, b.getWidth() / 2, b.getHeight() / 2, false);

                    markerOptions = new MarkerOptions().title(city)
                            .snippet(country).position(latLng).draggable(true)
                            .icon(BitmapDescriptorFactory.fromBitmap(bHalfSize));
                } else {
                    markerOptions = new MarkerOptions().title(city)
                            .snippet("" + markerId).position(latLng).draggable(true)
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
                }


                placesHashMap.put(place.getPlaceID(), markerOptions);
            }
        }
    }


    @SuppressLint("ObsoleteSdkInt")
    private static Bitmap getBitmapFromVectorDrawable(Context context, int drawableId) {
        Drawable drawable = ContextCompat.getDrawable(context, drawableId);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            drawable = (DrawableCompat.wrap(Objects.requireNonNull(drawable))).mutate();
        }

        Bitmap bitmap = Bitmap.createBitmap(Objects.requireNonNull(drawable).getIntrinsicWidth(),
                drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }

    public LinkedHashMap<Integer, String> getCountriesVisited() {
        return countriesVisited;
    }

    public void removeCountry(int id) {
        countriesVisited.remove(id);
    }

    public void removeMarker(int id) {
        placesHashMap.remove(id);
    }

    public static MainViewModel getViewModel(FragmentActivity activity) {
        MainViewModelFactory factory = new MainViewModelFactory(activity.getApplication());
        return ViewModelProviders.of(activity, factory).get(MainViewModel.class);
    }


    public FirebaseUser getFireBaseUser() {
        return mFireBaseUser;
    }


    public Completable signInWithEmail(String email, String password, Activity activity) {
        return Completable.create(emitter -> mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(Objects.requireNonNull(activity), task -> {
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in currentUser's information
                        mFireBaseUser = mAuth.getCurrentUser();
                        emitter.onComplete();
                    } else {
                        emitter.onError(task.getException());
                    }
                }));
    }


    public Completable firebaseAuthWithGoogle(GoogleSignInAccount acct, Activity activity) {
        return Completable.create(new CompletableOnSubscribe() {
            @Override
            public void subscribe(CompletableEmitter emitter) throws Exception {
                Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());
                AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
                mAuth.signInWithCredential(credential)
                        .addOnCompleteListener(Objects.requireNonNull(activity), task -> {
                            if (task.isSuccessful()) {
                                // Sign in success, update UI with the signed-in currentUser's information
                                Log.d(TAG, "signInWithCredential:success");
                                mFireBaseUser = mAuth.getCurrentUser();
                                emitter.onComplete();
                            } else {
                                // If sign in fails, display a message to the currentUser.
                                Log.w(TAG, "signInWithCredential:failure", task.getException());
                                emitter.onError(task.getException());
                            }
                        });
            }
        });

    }

    public void resetPassword(String email) {
        mAuth.sendPasswordResetEmail(email);
    }

    public void addUserToDatabase() {
        //Add currentUser to firebase database
        DatabaseReference mdatabaseReference = FirebaseDatabase.getInstance().getReference("users");
        String _uuid = mFireBaseUser.getUid();
        String _uname = mFireBaseUser.getDisplayName();
        String _upicture = mFireBaseUser.getPhotoUrl().toString();
        User currentUser = new User(_uuid, _uname, _upicture, "");
        mdatabaseReference.child(_uuid).setValue(currentUser);
    }


}
