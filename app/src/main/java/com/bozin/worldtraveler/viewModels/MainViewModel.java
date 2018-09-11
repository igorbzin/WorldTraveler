package com.bozin.worldtraveler.viewModels;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.SharedPreferences;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;

import io.reactivex.Completable;


public class MainViewModel extends AndroidViewModel {
    private static final String TAG = "viewModel";
    private PlacesRepository mPlacesRepository;
    private LiveData<List<Place>> placesList;
    private LinkedHashMap<Integer, String> countriesVisited;
    private LinkedHashMap<Integer, MarkerOptions> placesHashMap;
    private FirebaseAuth mAuth ;
    private FirebaseUser mFireBaseUser;
    private DatabaseReference mDatabaseReference;


    public MainViewModel(@NonNull Application application) {
        super(application);
        mPlacesRepository = new PlacesRepository(application);
        placesList = mPlacesRepository.getAllPlaces();
        countriesVisited = new LinkedHashMap<>();
        placesHashMap = new LinkedHashMap<>();
        mDatabaseReference = FirebaseDatabase.getInstance().getReference("users");
        mAuth = FirebaseAuth.getInstance();
        mFireBaseUser = mAuth.getCurrentUser();
    }

    public LiveData<List<Place>> getPlacesList() {
        if (placesList != null) {
            return placesList;
        } else {
            return mPlacesRepository.getAllPlaces();
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


    public User getCurrentUser(){
        String _uid = mFireBaseUser.getUid();
        String _uname = mFireBaseUser.getDisplayName();
        String _uphoto = mFireBaseUser.getPhotoUrl().toString();
        String _friends = "";
        return new User(_uid, _uname, "", _friends);
    }

    //NEED TO CALL THIS FIRST TO INIT PLACES HASHMAP
    private void createMarkersFromPlaces(List<Place> list, Object... objects) {
        if (list != null) {
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

    public void setFirebaseAuth(FirebaseAuth auth){
        mAuth = auth;
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

    public void setFireBaseUser(FirebaseUser fireBaseUser){
        mFireBaseUser = fireBaseUser;
    }


    public Completable signInWithEmail(String email, String password, Activity activity) {
        initFireBase();
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
        initFireBase();
        return Completable.create(emitter -> {
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
        });

    }

    public void resetPassword(String email) {
        mAuth.sendPasswordResetEmail(email);
    }

    public void addUserToDatabase() {
        //Add currentUser to firebase database
        String _uuid = mFireBaseUser.getUid();
        String _uname = mFireBaseUser.getDisplayName();
        String _upicture = "";
        if (mFireBaseUser.getPhotoUrl() != null) {
          _upicture = Objects.requireNonNull(mFireBaseUser.getPhotoUrl()).toString();
        }
        User currentUser = new User(_uuid, _uname, _upicture, "");
        mDatabaseReference.child(_uuid).setValue(currentUser);
    }


    public void updateDbUserData(Context context){
        SharedPreferences sharedPreferences = android.support.v7.preference.PreferenceManager.getDefaultSharedPreferences(context);
        boolean visibility = sharedPreferences.getBoolean(context.getString(R.string.pref_search_key), false);
        int visible = visibility ? 1 : 0;
        try {
            mDatabaseReference.child(mFireBaseUser.getUid()).child("visibility").setValue(visible);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public String setMapStyle(Context context) {
        SharedPreferences sharedPreferences = android.support.v7.preference.PreferenceManager.getDefaultSharedPreferences(context);
        boolean mShowCityNames = sharedPreferences.getBoolean(context.getString(R.string.settings_show_cities_key), false);
        boolean mShowCountryNames = sharedPreferences.getBoolean(context.getString(R.string.settings_show_countries_key), true);

        String mapStyle = sharedPreferences.getString(context.getString(R.string.sp_mapstyle_key), "0");
        JSONArray jsonArray = null;
        try {
            jsonArray = new JSONArray(mapStyle);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (jsonArray != null) {
            jsonArray = setPreferenceValue(jsonArray, "administrative.country", mShowCountryNames);
            jsonArray = setPreferenceValue(jsonArray, "administrative.locality", mShowCityNames);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(context.getString(R.string.sp_mapstyle_key), jsonArray.toString());
            editor.putBoolean(context.getString(R.string.settings_show_cities_key), mShowCityNames);
            editor.putBoolean(context.getString(R.string.settings_show_countries_key), mShowCountryNames);
            editor.apply();
            return jsonArray.toString();
        } else {
            return null;
        }
    }


    private JSONArray setPreferenceValue(JSONArray jsonArray, String preference, boolean visibility) {
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonObject = null;
            try {
                jsonObject = new JSONObject(jsonArray.get(i).toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                if (jsonObject != null && jsonObject.has("featureType")) {
                    if (jsonObject.get("featureType").equals(preference)) {
                        JSONArray stylers = jsonObject.getJSONArray("stylers");
                        JSONObject secondJSONObject;
                        for (int j = 0; j < stylers.length(); j++) {
                            secondJSONObject = new JSONObject(stylers.get(j).toString());
                            if (!visibility) {
                                secondJSONObject.put("visibility", "off");
                            } else {
                                secondJSONObject.put("visibility", "on");
                            }
                            stylers.put(j, secondJSONObject);
                        }
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                jsonArray.put(i, jsonObject);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return jsonArray;
    }

    private void initFireBase(){
        mAuth = FirebaseAuth.getInstance();
        mFireBaseUser = mAuth.getCurrentUser();
    }

}
