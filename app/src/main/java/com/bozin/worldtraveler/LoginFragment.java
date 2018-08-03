package com.bozin.worldtraveler;

import android.app.Activity;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.bozin.worldtraveler.databinding.FragmentProfileBinding;
import com.bozin.worldtraveler.databinding.FragmentUserBinding;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.squareup.picasso.Picasso;

import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;


public class LoginFragment extends Fragment implements
        View.OnClickListener {

    private FirebaseAuth mAuth;
    private EditText etEmail;
    private EditText etPassword;
    private String email;
    private String password;
    private final String TAG = "LOGIN_PROCESS";
    private GoogleSignInClient mGoogleSignInClient;
    private FirebaseUser currentUser;

    private static final int RC_SIGN_IN = 9001;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            FragmentProfileBinding profileBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_profile, container, false);
            //Update NavView
            NavigationView navigationView = Objects.requireNonNull(getActivity()).findViewById(R.id.nav_view);
            navigationView.getMenu().clear();
            navigationView.inflateMenu(R.menu.drawermenu_logged_in);
            View headerLayout = navigationView.getHeaderView(0);
            navigationView.removeHeaderView(headerLayout);
            navigationView.inflateHeaderView(R.layout.drawer_header_logged_in);
            CircleImageView profilePicture = getActivity().findViewById(R.id.iv_profile_picture);
            Uri photoUrl = currentUser.getPhotoUrl();
            if (photoUrl != null) {
                Picasso.get().load(photoUrl).into(profilePicture);
            } else {
                profilePicture.setImageResource(R.drawable.earth_icon);
            }
            String profile_name = currentUser.getDisplayName();
            TextView profile_nameTV = getActivity().findViewById(R.id.tv_profile_name);
            if (profile_name != null) {
                profile_nameTV.setText(profile_name);
            } else {
                profile_nameTV.setVisibility(View.GONE);
            }


            Picasso.get().load(currentUser.getPhotoUrl()).into(profileBinding.imageView);
            profileBinding.textView2.setText(currentUser.getDisplayName());
            profileBinding.btnSignOut.setOnClickListener(view -> {
                navigationView.getMenu().clear();
                navigationView.inflateMenu(R.menu.drawermenu);
                View headerLayout2 = navigationView.getHeaderView(0);
                navigationView.removeHeaderView(headerLayout2);
                navigationView.inflateHeaderView(R.layout.drawer_header);
                signOut();
                LoginFragment userFragment = new LoginFragment();
                FragmentTransaction fragmentTransaction = Objects.requireNonNull(getActivity().getSupportFragmentManager()).beginTransaction();
                fragmentTransaction.setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
                        .replace(R.id.container, userFragment)
                        .commit();
            });
            return profileBinding.getRoot();
        } else {

            FragmentUserBinding loginBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_user, container, false);
            // [START config_signin]
            // Configure Google Sign In
            GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(getString(R.string.default_web_client_id))
                    .requestEmail()
                    .build();
            // [END config_signin]

            mGoogleSignInClient = GoogleSignIn.getClient(Objects.requireNonNull(getActivity()), gso);

            View view = loginBinding.getRoot();
            etEmail = loginBinding.etEmail;
            etPassword = loginBinding.etPassword;
            loginBinding.btnSignIn.setText(getString(R.string.btn_login));
            loginBinding.btnRegister.setText(getString(R.string.btn_user_register));
            //loginBinding.btnSignOut.setText(getString(R.string.btn_login_sign_out));
            etEmail.setHint(getString(R.string.tv_login_email));
            etPassword.setHint(getString(R.string.tv_login_password));
            SignInButton googleBtn = loginBinding.btnGoogleSignIn;
            googleBtn.setOnClickListener(this);


            // Initialize Facebook Login button
            CallbackManager mCallbackManager = CallbackManager.Factory.create();
            LoginButton loginButton = loginBinding.loginSocialFacebook;
            loginButton.setReadPermissions("email", "public_profile");
            loginButton.setFragment(this);
            loginButton.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
                @Override
                public void onSuccess(LoginResult loginResult) {
                    Log.d(TAG, "facebook:onSuccess:" + loginResult);
                    handleFacebookAccessToken(loginResult.getAccessToken());
                }

                @Override
                public void onCancel() {
                    Log.d(TAG, "facebook:onCancel");
                    // ...
                }

                @Override
                public void onError(FacebookException error) {
                    Log.d(TAG, "facebook:onError", error);
                    // ...
                }
            });


            loginBinding.btnRegister.setOnClickListener(view1 -> {
                RegisterFragment registerFragment = new RegisterFragment();
                FragmentTransaction fragmentTransaction = Objects.requireNonNull(getActivity().getSupportFragmentManager()).beginTransaction();
                fragmentTransaction.setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
                        .replace(R.id.container, registerFragment)
                        .addToBackStack(getString(R.string.fragment_user))
                        .commit();
            });

            loginBinding.btnSignIn.setOnClickListener(v -> {
                if (!validateForm()) {
                    return;
                }

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

                                try {
                                    throw Objects.requireNonNull(task.getException());

                                } catch (FirebaseAuthException e) {
                                    Log.w(TAG, "signInWithEmail:failure", task.getException());
                                    String errorCode = e.getErrorCode();
                                    switch (errorCode) {
                                        case "ERROR_INVALID_EMAIL":
                                            etEmail.requestFocus();
                                            etEmail.setError(getString(R.string.error_invalid_email));
                                            break;
                                        case "ERROR_WRONG_PASSWORD":
                                            etPassword.requestFocus();
                                            etPassword.setError(getString(R.string.error_invalid_pwd));
                                            break;
                                        case "ERROR_USER_NOT_FOUND":
                                            etEmail.requestFocus();
                                            etEmail.setError(getString(R.string.error_invalid_user));
                                            break;
                                        default:
                                            break;
                                    }

                                } catch (FirebaseNetworkException e) {
                                    Snackbar error = makeSnackBar(getString(R.string.error_no_internet_connection));
                                    hideKeyboard();
                                    error.show();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                updateUI(null);
                            }
                        });

            });

            return view;
        }


        
    }


    private Snackbar makeSnackBar(String text) {
        Snackbar sb_error = Snackbar.make(Objects.requireNonNull(getActivity()).findViewById(R.id.cl_user_fragment), text, Snackbar.LENGTH_LONG);
        View sb_errorView = sb_error.getView();
        TextView tv_sb_error = sb_errorView.findViewById(android.support.design.R.id.snackbar_text);
        tv_sb_error.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        sb_errorView.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.colorAccent));
        return sb_error;
    }

    @Override
    public void onStart() {
        super.onStart();
        currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            updateUI(currentUser);
        }
    }


    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(Objects.requireNonNull(getActivity()), task -> {
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG, "signInWithCredential:success");
                        FirebaseUser user = mAuth.getCurrentUser();
                        updateUI(user);
                        refreshFragment();
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w(TAG, "signInWithCredential:failure", task.getException());
                        updateUI(null);
                    }

                });
    }


    private void signOut() {
        mAuth.signOut();

        if (mGoogleSignInClient != null) {
            // Google sign out
            mGoogleSignInClient.signOut().addOnCompleteListener(Objects.requireNonNull(getActivity()),
                    task -> updateUI(null));
        }
    }

    private void updateUI(FirebaseUser firebaseUser) {

        if (firebaseUser != null) {
            Toast.makeText(getActivity(), firebaseUser.getEmail() + firebaseUser.getDisplayName(), Toast.LENGTH_LONG).show();
        }

    }

    private void hideKeyboard() {

        InputMethodManager imm = (InputMethodManager) Objects.requireNonNull(getActivity()).getSystemService(Activity.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(getActivity().findViewById(R.id.cl_user_fragment).getWindowToken(), 0);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Log.w(TAG, "Google sign in failed", e);
                // [START_EXCLUDE]
                updateUI(null);
                // [END_EXCLUDE]
            }
        }
    }


    private void handleFacebookAccessToken(AccessToken token) {
        Log.d(TAG, "handleFacebookAccessToken:" + token);

        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(Objects.requireNonNull(getActivity()), task -> {
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG, "signInWithCredential:success");
                        FirebaseUser user = mAuth.getCurrentUser();
                        updateUI(user);
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w(TAG, "signInWithCredential:failure", task.getException());
                        Toast.makeText(getActivity(), "Authentication failed.",
                                Toast.LENGTH_SHORT).show();
                        updateUI(null);
                    }

                    // ...
                });
    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.btn_google_sign_in) {
            signIn();
        }
    }

    private boolean validateForm() {
        boolean valid = true;
        if (TextUtils.isEmpty(etEmail.getText().toString())) {
            etEmail.setError("Required.");
            valid = false;
        } else {
            etEmail.setError(null);
        }


        if (TextUtils.isEmpty(etPassword.getText().toString())) {
            etPassword.setError("Required.");
            valid = false;
        } else {
            etPassword.setError(null);
        }

        return valid;
    }

    private void refreshFragment() {
        LoginFragment loginFragment = new LoginFragment();
        FragmentTransaction fragmentTransaction = Objects.requireNonNull(getActivity().getSupportFragmentManager()).beginTransaction();
        fragmentTransaction.setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
                .replace(R.id.container, loginFragment)
                .commit();
    }

    @Override
    public void onResume() {
        super.onResume();
        Objects.requireNonNull(getActivity()).setTitle(R.string.fragment_user);
    }


}
