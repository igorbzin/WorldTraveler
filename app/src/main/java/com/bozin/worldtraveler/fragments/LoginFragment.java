package com.bozin.worldtraveler.fragments;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.databinding.DataBindingUtil;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bozin.worldtraveler.MainActivity;
import com.bozin.worldtraveler.R;
import com.bozin.worldtraveler.databinding.FragmentUserBinding;
import com.bozin.worldtraveler.viewModels.MainViewModel;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseUser;

import java.util.Objects;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableCompletableObserver;
import io.reactivex.schedulers.Schedulers;


public class LoginFragment extends BaseFragment implements
        View.OnClickListener {

    private EditText etEmail;
    private EditText etPassword;
    private String email;
    private String password;
    private final String TAG = "LOGIN_PROCESS";
    private GoogleSignInClient mGoogleSignInClient;
    private onLoggedInHandler mOnLoggedInHandler;
    private AlertDialog loadingDialog;
    private CompositeDisposable disposable = new CompositeDisposable();
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;


    private static final int RC_SIGN_IN = 9001;
    private MainViewModel mainViewModel;


    public static LoginFragment newInstance(int signingOut) {
        LoginFragment loginFragment = new LoginFragment();
        Bundle args = new Bundle();
        args.putInt("signing_out", signingOut);
        loginFragment.setArguments(args);
        return loginFragment;
    }

    public interface onLoggedInHandler {
        void onLoginUpdate();
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        FragmentUserBinding loginBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_user, container, false);

        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(Objects.requireNonNull(getActivity()), gso);


        //View initialisation
        View view = loginBinding.getRoot();
        etEmail = loginBinding.etEmail;
        etPassword = loginBinding.etPassword;
        loginBinding.btnSignIn.setText(getString(R.string.btn_login));
        loginBinding.btnRegister.setText(getString(R.string.btn_user_register));
        etEmail.setHint(getString(R.string.tv_login_email));
        loadingDialog = createLoadingDialog();

        //underline for textviews, set onclicklisteners
        TextView pwdForgotten = loginBinding.loginForgottenPwd;
        pwdForgotten.setPaintFlags(pwdForgotten.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        pwdForgotten.setOnClickListener(view13 -> onCreateDialogFragment());

        TextView skipLogin = loginBinding.tvSkipLogin;
        skipLogin.setPaintFlags(skipLogin.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        skipLogin.setOnClickListener(view12 -> {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(Objects.requireNonNull(getContext()));
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt("skip_login", 1);
            editor.apply();

            //Switch fragment to map
            NavigationView navigationView = getActivity().findViewById(R.id.nav_view);
            navigationView.getMenu().getItem(0).setChecked(true);
            MapFragment mapFragment = new MapFragment();
            FragmentTransaction fragmentTransaction = Objects.requireNonNull(getActivity().getSupportFragmentManager()).beginTransaction();
            fragmentTransaction.setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
                    .replace(R.id.container, mapFragment)
                    .addToBackStack(getString(R.string.fragment_user))
                    .commit();
        });


        etPassword.setHint(getString(R.string.tv_login_password));
        SignInButton googleBtn = loginBinding.btnGoogleSignIn;
        googleBtn.setOnClickListener(this);


        loginBinding.btnRegister.setOnClickListener(view1 -> {
            RegisterFragment registerFragment = new RegisterFragment();
            FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
            fragmentTransaction.setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
                    .replace(R.id.container, registerFragment)
                    .addToBackStack(getString(R.string.fragment_user))
                    .commit();
        });

        loginBinding.btnSignIn.setOnClickListener(v -> {
            if (!validateForm()) {
                return;
            }
            loadingDialog.show();
            email = etEmail.getText().toString();
            password = etPassword.getText().toString();


            disposable.add(mainViewModel.signInWithEmail(email, password, getActivity())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeWith(new DisposableCompletableObserver() {
                        @Override
                        public void onComplete() {
                            Log.d(TAG, "OnComplete called");
                            loadingDialog.dismiss();
                            updateUI(mainViewModel.getFireBaseUser());
                        }

                        @Override
                        public void onError(Throwable e) {
                            Log.d(TAG, "OnError called with exception: " + e.toString());
                            loadingDialog.dismiss();
                            if (e instanceof FirebaseAuthException) {
                                Log.w(TAG, "signInWithEmail:failure", e);
                                String errorCode = ((FirebaseAuthException) e).getErrorCode();
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
                            } else if (e instanceof FirebaseNetworkException) {
                                Snackbar error = makeSnackBar(getString(R.string.error_no_internet_connection));
                                hideKeyboard();
                                error.show();
                            }
                            updateUI(null);
                        }


                    }));
        });

        return view;
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
        mainViewModel = MainViewModel.getViewModel(Objects.requireNonNull(getActivity()));
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mOnLoggedInHandler = (onLoggedInHandler) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + "must implement onLoggedInHandler!");
        }
    }


    public void signOut() {
        FirebaseAuth.getInstance().signOut();
    }

    private void updateUI(FirebaseUser firebaseUser) {
        if (firebaseUser != null) {
            mOnLoggedInHandler.onLoginUpdate();
        } else {
            NavigationView mNavigationView = Objects.requireNonNull(getActivity()).findViewById(R.id.nav_view);
            mNavigationView.getMenu().clear();
            mNavigationView.inflateMenu(R.menu.drawermenu);
            View headerLayout = mNavigationView.getHeaderView(0);
            mNavigationView.removeHeaderView(headerLayout);
            mNavigationView.inflateHeaderView(R.layout.drawer_header);
            mNavigationView.getMenu().getItem(3).setChecked(true);
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                // [END_EXCLUDE]
                GoogleSignInAccount account = task.getResult(ApiException.class);
                disposable.add(mainViewModel.firebaseAuthWithGoogle(account, getActivity())
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeWith(new DisposableCompletableObserver() {
                            @Override
                            public void onComplete() {
                                loadingDialog.dismiss();
                                updateUI(mainViewModel.getFireBaseUser());
                            }

                            @Override
                            public void onError(Throwable e) {
                                loadingDialog.dismiss();
                                Log.d(TAG, "Error signing in with Google: " + e.toString());
                            }
                        }));
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Log.w(TAG, "Google sign in failed", e);
                Toast.makeText(getActivity(), e.toString(), Toast.LENGTH_LONG).show();
                updateUI(null);
            }
        }
    }


    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.btn_google_sign_in) {
            loadingDialog.show();
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


    @Override
    public void onResume() {
        super.onResume();
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        mainViewModel.setFirebaseAuth(mAuth);
        mainViewModel.setFireBaseUser(currentUser);
        updateUI(currentUser);
        Objects.requireNonNull(getActivity()).setTitle(R.string.fragment_user);
        if (currentUser == null) {
            ((MainActivity) Objects.requireNonNull(getActivity())).setNavItemChecked(R.id.menu_item_login);
        }

    }

    @SuppressLint("InflateParams")
    public void onCreateDialogFragment() {

        AlertDialog.Builder builder = new AlertDialog.Builder(Objects.requireNonNull(getActivity()));
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();


        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        builder.setView(inflater.inflate(R.layout.dialog_pwd_reset, null))
                // Add action buttons
                .setPositiveButton(R.string.btn_positive, (dialog, id) -> {

                })
                .setNegativeButton(R.string.btn_negative, (dialog, id) -> dialog.cancel());
        AlertDialog dialog = builder.create();
        dialog.show();

        dialog.getButton(dialog.BUTTON_NEGATIVE).setTextColor(getResources().getColor(R.color.colorAccent));
        dialog.getButton(dialog.BUTTON_POSITIVE).setOnClickListener(view -> {
            EditText email = ((AlertDialog) dialog).findViewById(R.id.et_pwd_reset_mail);
            String sEmail = Objects.requireNonNull(email).getText().toString();
            if (!sEmail.isEmpty()) {
                mainViewModel.resetPassword(sEmail);
                dialog.dismiss();
            } else {
                email.setError("Can't be empty!");
            }
        });
        dialog.getButton(dialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.colorAccent));
    }


    public AlertDialog createLoadingDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(Objects.requireNonNull(getActivity()));
        // Get the layout inflater
        LayoutInflater inflater = this.getLayoutInflater();
        // Inflate and set the layout for the dialog
        @SuppressLint("InflateParams") final View view = inflater.inflate(R.layout.dialog_loading, null);
        Drawable drawable = getResources().getDrawable(R.drawable.progress_loading);
        ProgressBar progressBar = view.findViewById(R.id.pb_loading_user);
        progressBar.setProgressDrawable(drawable);
        builder.setView(view);
        return builder.create();
    }

    @Override
    public void onPause() {
        super.onPause();
        disposable.clear();
        if (currentUser == null) {
            ((MainActivity) Objects.requireNonNull(getActivity())).uncheckNavItem(R.id.menu_item_login);
        }
    }
}
