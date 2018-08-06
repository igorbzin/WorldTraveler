package com.bozin.worldtraveler;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bozin.worldtraveler.databinding.FragmentProfileBinding;
import com.facebook.login.Login;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.squareup.picasso.Picasso;

import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileFragment extends Fragment {

    private onSignedOutHandler mOnSignedOutHandler;

    public interface onSignedOutHandler{
        void onSignedOut();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        FragmentProfileBinding profileBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_profile, container, false);

        //Picasso.get().load(user.getPhotoUrl()).into(profileBinding.imageView);
        //profileBinding.textView2.setText(currentUser.getDisplayName());

        profileBinding.btnSignOut.setOnClickListener(view -> mOnSignedOutHandler.onSignedOut());
        return  profileBinding.getRoot();

    }



    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            mOnSignedOutHandler = (onSignedOutHandler) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement onSignedOutHandler");
        }
    }

}
