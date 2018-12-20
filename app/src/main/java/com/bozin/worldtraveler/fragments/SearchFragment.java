package com.bozin.worldtraveler.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;

import com.bozin.worldtraveler.MainActivity;
import com.bozin.worldtraveler.R;
import com.bozin.worldtraveler.adapters.SearchResultAdapter;
import com.bozin.worldtraveler.model.RxSearchObservable;
import com.bozin.worldtraveler.model.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class SearchFragment extends Fragment {

    private static final String TAG = "SearchFragment";
    private ArrayList<User> userList;
    private DatabaseReference mDbReference;
    private CompositeDisposable compositeDisposable = new CompositeDisposable();


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_search, container, false);
        SearchView searchView = v.findViewById(R.id.sv_search);
        searchView.setIconifiedByDefault(false);

        RecyclerView searchResultsRv = v.findViewById(R.id.rv_search);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        searchResultsRv.setLayoutManager(layoutManager);
        userList = new ArrayList<>();
        SearchResultAdapter adapter = new SearchResultAdapter(getContext(), userList);
        searchResultsRv.setAdapter(adapter);

        //Init db Reference
        mDbReference = FirebaseDatabase.getInstance().getReference("users");


        ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userList.clear();
                adapter.updateUserList(userList);
                if (dataSnapshot.exists()) {
                    Log.d(TAG, "Datasnapshot exists");
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        User user = snapshot.getValue(User.class);
                        Log.d(TAG, "User added: " + user.getUserName());
                        userList.add(user);
                    }
                    adapter.updateUserList(userList);
                } else {
                    Log.d(TAG, "Datasnapshot is empty");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d(TAG, "database error occured" + databaseError.toString());
            }
        };


        compositeDisposable.add(RxSearchObservable.fromView(searchView)
                .debounce(400, TimeUnit.MILLISECONDS)
                .filter(text -> !text.isEmpty())
                .map(s -> {
                    s = s.toLowerCase();
                    Query userQuery = mDbReference.orderByChild("visibility").startAt("1_" + s ).endAt("1_" + s + "\uf8ff");
                    userQuery.addListenerForSingleValueEvent(valueEventListener);
                    Log.d(TAG, "userquery for query:  " + s);
                    return userList;
                })
                .distinctUntilChanged()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(users -> adapter.updateUserList(userList)));


        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        ((MainActivity) Objects.requireNonNull(getActivity())).setNavItemChecked(R.id.menu_item_friends);

    }

    @Override
    public void onPause() {
        super.onPause();
        ((MainActivity) Objects.requireNonNull(getActivity())).uncheckNavItem(R.id.menu_item_friends);
        compositeDisposable.dispose();
    }
}
