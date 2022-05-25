package com.qboxus.musictok.ActivitesFragment.Accounts;

import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.PasswordTransformationMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.qboxus.musictok.Models.UserRegisterModel;
import com.qboxus.musictok.R;
import com.qboxus.musictok.SimpleClasses.Functions;

public class CreatePasswordF extends Fragment implements View.OnClickListener{
    View view;
    EditText etPass;
    Button btnPass;
    ImageView ivHide;
    RelativeLayout tabShowPassword;
    UserRegisterModel userRegisterModel;
    String fromWhere, stEmail;
    private Boolean oldCheck = true;

    public CreatePasswordF(String fromWhere) {
        this.fromWhere = fromWhere;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_create_password, container, false);
        Bundle bundle = getArguments();
        userRegisterModel = (UserRegisterModel) bundle.getSerializable("user_model");
        stEmail = bundle.getString("email");
        ivHide =view.findViewById(R.id.iv_old_hide);
        tabShowPassword=view.findViewById(R.id.ll_old_hide);
        tabShowPassword.setOnClickListener(this);
        etPass = view.findViewById(R.id.edtPassword);
        etPass.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        view.findViewById(R.id.goBack).setOnClickListener(this);
        btnPass = view.findViewById(R.id.btn_pass);
        btnPass.setOnClickListener(this);

        etPass.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int count) {
                //check the password lenght
                String txtName = etPass.getText().toString();
                if (txtName.length() > 0) {
                    btnPass.setEnabled(true);
                    btnPass.setClickable(true);
                } else {
                    btnPass.setEnabled(false);
                    btnPass.setClickable(false);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        return view;
    }

    // this will check the validations like none of the field can be the empty
    public boolean checkValidation() {

        String password = etPass.getText().toString();

        if (TextUtils.isEmpty(password) || password.length() < 2 || password.length() > 12) {
            etPass.setError(view.getContext().getString(R.string.enter_valid_new_password));
            etPass.setFocusable(true);
            return false;
        }

        if (password.length() <= 5 || password.length() >= 12) {
            etPass.setError(view.getContext().getString(R.string.valid_password_length));
            etPass.setFocusable(true);
            return false;
        }


        return true;
    }


    @Override
    public void onClick(View view) {
        switch (view.getId())
        {
            case R.id.ll_old_hide:
                {
                    if (oldCheck){
                        etPass.setTransformationMethod(null);
                        ivHide.setImageDrawable(ContextCompat.getDrawable(view.getContext(),R.drawable.ic_show));
                        oldCheck = false;
                        etPass.setSelection(etPass.length());
                    }else {
                        etPass.setTransformationMethod(new PasswordTransformationMethod());
                        ivHide.setImageDrawable(ContextCompat.getDrawable(view.getContext(),R.drawable.ic_hide));
                        oldCheck = true;
                        etPass.setSelection(etPass.length());
                    }
                }
                break;
            case R.id.goBack:
            {
                getActivity().onBackPressed();
            }
            break;
            case R.id.btn_pass:
            {
                if (checkValidation()) {
                    CreateUsernameF user_name_f = new CreateUsernameF(fromWhere);
                    FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                    Bundle bundle2 = new Bundle();
                    userRegisterModel.password = etPass.getText().toString();
                    bundle2.putSerializable("user_model", userRegisterModel);
                    user_name_f.setArguments(bundle2);
                    transaction.setCustomAnimations(R.anim.in_from_right, R.anim.out_to_left, R.anim.in_from_left, R.anim.out_to_right);
                    transaction.addToBackStack(null);
                    transaction.replace(R.id.pass_f, user_name_f).commit();
                }
            }
            break;
        }
    }
}