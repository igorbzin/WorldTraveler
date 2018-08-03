package com.bozin.worldtraveler;

import android.app.Activity;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import com.bozin.worldtraveler.databinding.FragmentRegisterBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseUser;

import java.util.Objects;


public class RegisterFragment extends Fragment {

    private EditText firstName;
    private EditText name;
    private EditText email;
    private EditText password;
    private EditText passwordRepeat;
    private FirebaseAuth mAuth;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        FragmentRegisterBinding registerBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_register, container, false);
        mAuth = FirebaseAuth.getInstance();
        email =registerBinding.etRegisterEmail;
        password = registerBinding.etRegisterPwd1;
        passwordRepeat = registerBinding.etRegisterPwd2;
        name = registerBinding.etRegisterName;
        firstName = registerBinding.etRegisterFirstName;
        registerBinding.btnEmailRegister.setOnClickListener(view -> {
            if(!validateForm()){
                return;
            }

            String emailStr = email.getText().toString();
            String passwordStr = password.getText().toString();
            String passwordReprßeatStr = passwordRepeat.getText().toString();
            if(passwordStr.equals(passwordReprßeatStr)){
                mAuth.createUserWithEmailAndPassword(emailStr, passwordStr)
                        .addOnCompleteListener(Objects.requireNonNull(getActivity()), new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    // Sign in success, update UI with the signed-in user's information
                                    Log.d("Register", "createUserWithEmail:success");
                                    FirebaseUser user = mAuth.getCurrentUser();
                                } else {
                                    // If sign in fails, display a message to the user.
                                    Log.w("Register", "createUserWithEmail:failure", task.getException());
                                    try {
                                        throw Objects.requireNonNull(task.getException());

                                    } catch (FirebaseAuthException e) {
                                        Log.w("Register", "RegisterInWithEmail:failure", task.getException());
                                        String errorCode = e.getErrorCode();
                                        switch (errorCode) {
                                            case "ERROR_INVALID_EMAIL":
                                                email.requestFocus();
                                                email.setError(getString(R.string.error_invalid_email));
                                                break;
                                            case "ERROR_WRONG_PASSWORD":
                                                passwordRepeat.requestFocus();
                                                passwordRepeat.setError(getString(R.string.error_invalid_pwd));
                                                break;
                                            case "ERROR_USER_NOT_FOUND":
                                                passwordRepeat.requestFocus();
                                                passwordRepeat.setError(getString(R.string.error_invalid_user));
                                                break;
                                            case "ERROR_EMAIL_ALREADY_IN_USE":
                                                email.requestFocus();
                                                email.setError(getString(R.string.error_email_exists));
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
                                }
                            }
                        });

            } else {
                passwordRepeat.requestFocus();
                passwordRepeat.setError(getString(R.string.error_pwd_mismatch));
            }

        });
        return registerBinding.getRoot();
    }


    private boolean validateForm() {
        boolean valid = true;
        if (TextUtils.isEmpty(email.getText().toString())) {
            email.setError("Required.");
            valid = false;
        } else {
            email.setError(null);
        }

        if (TextUtils.isEmpty(password.getText().toString())) {
            password.setError("Required.");
            valid = false;
        } else {
            password.setError(null);
        }

        if (TextUtils.isEmpty(passwordRepeat.getText().toString())) {
            passwordRepeat.setError("Required.");
            valid = false;
        } else {
            passwordRepeat.setError(null);
        }

        if (TextUtils.isEmpty(name.getText().toString())) {
            name.setError("Required.");
            valid = false;
        } else {
            name.setError(null);
        }

        if (TextUtils.isEmpty(firstName.getText().toString())) {
            firstName.setError("Required.");
            valid = false;
        } else {
            firstName.setError(null);
        }


        return valid;
    }

    private void hideKeyboard() {

        InputMethodManager imm = (InputMethodManager) Objects.requireNonNull(getActivity()).getSystemService(Activity.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(getActivity().findViewById(R.id.cl_register_fragment).getWindowToken(), 0);
        }
    }


    private Snackbar makeSnackBar(String text) {
        Snackbar sb_error = Snackbar.make(Objects.requireNonNull(getActivity()).findViewById(R.id.cl_register_fragment), text, Snackbar.LENGTH_LONG);
        View sb_errorView = sb_error.getView();
        TextView tv_sb_error = sb_errorView.findViewById(android.support.design.R.id.snackbar_text);
        tv_sb_error.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        sb_errorView.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.colorAccent));
        return sb_error;
    }



}
