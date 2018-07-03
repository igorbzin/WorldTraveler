package com.bozin.worldtraveler;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;

import java.util.Objects;


public class LoginFragment extends Fragment {

    private FirebaseAuth mAuth;
    private EditText etEmail;
    private EditText etPassword;
    private String email;
    private String password;
    private final String TAG = "LOGIN_PROCESS";


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login, container, false);
        mAuth = FirebaseAuth.getInstance();
        Button mLoginBtn = view.findViewById(R.id.btn_login);
        Button mSignInBtn = view.findViewById(R.id.btn_sign_in);
        etEmail = view.findViewById(R.id.editText);
        etPassword = view.findViewById(R.id.editText2);
        mLoginBtn.setOnClickListener(view1 -> {
            email = etEmail.getText().toString();
            password = etPassword.getText().toString();
            final View callingView = view1;

            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(Objects.requireNonNull(getActivity()), task -> {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("LOGIN_PROCESS", "createUserWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            updateUI(user);
                        } else {
                            try {
                                throw Objects.requireNonNull(task.getException());
                            } catch (FirebaseAuthUserCollisionException e) {
                                e.printStackTrace();
                                hideKeyboardFrom(Objects.requireNonNull(getContext()),callingView);
                                Snackbar sb_EmailUsed = Snackbar.make(getActivity().findViewById(R.id.cl_login_fragment), "This Email is already in use!", Snackbar.LENGTH_LONG);
                                sb_EmailUsed.show();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            // If sign in fails, display a message to the user.
                            Log.w("LOGIN_PROCESS", "createUserWithEmail:failure", task.getException());
                            //Toast.makeText(getActivity(), "Authentication failed.",
                            //        Toast.LENGTH_SHORT).show();
                            updateUI(null);
                        }
                    });
        });

        mSignInBtn.setOnClickListener( v -> {
            email = etEmail.getText().toString();
            password = etPassword.getText().toString();
            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(Objects.requireNonNull(getActivity()), task -> {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                            Toast.makeText(getActivity(), "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            updateUI(null);
                        }

                        // ...
                    });
        });


        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            updateUI(currentUser);
        }
    }


    void updateUI(FirebaseUser firebaseUser) {

        if (firebaseUser != null) {
            Toast.makeText(getActivity(), firebaseUser.getEmail() + firebaseUser.getDisplayName(), Toast.LENGTH_LONG).show();
        }
    }


    public static void hideKeyboardFrom(Context context, View view) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }



}
