package com.qboxus.musictok.ActivitesFragment.Accounts;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.qboxus.musictok.Models.UserRegisterModel;
import com.qboxus.musictok.R;
import com.ycuwq.datepicker.date.DatePicker;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import static java.util.Calendar.DATE;
import static java.util.Calendar.MONTH;
import static java.util.Calendar.YEAR;

public class DateOfBirthF extends Fragment implements View.OnClickListener {
    View view;
    DatePicker datePicker;
    Button btnDobNext;
    String currentDate, stYear;
    Date c;
    String fromWhere;
    UserRegisterModel userRegisterModel = new UserRegisterModel();

    public DateOfBirthF() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_dob_fragment, container, false);
        c = Calendar.getInstance().getTime();
        datePicker = view.findViewById(R.id.datePicker);
        btnDobNext = view.findViewById(R.id.btn_dob_next);
        datePicker.setMaxDate(System.currentTimeMillis() - 1000);

        Bundle bundle = getArguments();
        userRegisterModel = (UserRegisterModel) bundle.getSerializable("user_model");
        fromWhere = bundle.getString("fromWhere");
        datePicker.setOnDateSelectedListener(new DatePicker.OnDateSelectedListener() {
            @Override
            public void onDateSelected(int year, int month, int day) {
                // select the date from datepicker
                btnDobNext.setEnabled(true);
                btnDobNext.setClickable(true);
                stYear = String.valueOf(year);
                currentDate = year + "-" + month + "-" + day;
                userRegisterModel.dateOfBirth = currentDate;
            }
        });
        datePicker.getYearPicker().setEndYear(2020);

        view.findViewById(R.id.btn_dob_next).setOnClickListener(this::onClick);
        view.findViewById(R.id.goBack).setOnClickListener(this::onClick);
        return view;
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.goBack:
                getActivity().onBackPressed();
                break;

            case R.id.btn_dob_next:
                checkDobDate();
                break;

            default:
                break;
        }

    }


    public void checkDobDate() {
        SimpleDateFormat df = new SimpleDateFormat("yyy", Locale.ENGLISH);
        String formattedDate = df.format(c);
        Date dob = null;
        Date currentdate = null;
        try {
            dob = df.parse(formattedDate);
            currentdate = df.parse(currentDate);
        } catch (Exception e) {

        }

        int value = getDiffYears(currentdate, dob);
        // check that a user select the date greater then 17 year
        if (value > 17) {
            //get the email or phone if a user want to signup
            if (fromWhere.equals("signup")) {
                EmailPhoneF emailPhoneF = new EmailPhoneF(fromWhere);
                FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                transaction.setCustomAnimations(R.anim.in_from_right, R.anim.out_to_left, R.anim.in_from_left, R.anim.out_to_right);
                Bundle bundle = new Bundle();
                bundle.putSerializable("user_model", userRegisterModel);
                emailPhoneF.setArguments(bundle);
                transaction.addToBackStack(null);
                transaction.replace(R.id.dob_fragment, emailPhoneF).commit();
            }
            else if (fromWhere.equals("social") || fromWhere.equals("fromPhone")) {
                //if user from facebook or phone number the get the username
                CreateUsernameF signUp_fragment = new CreateUsernameF(fromWhere);
                FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                transaction.setCustomAnimations(R.anim.in_from_right, R.anim.out_to_left, R.anim.in_from_left, R.anim.out_to_right);
                Bundle bundle = new Bundle();
                bundle.putSerializable("user_model", userRegisterModel);
                signUp_fragment.setArguments(bundle);
                transaction.addToBackStack(null);
                transaction.replace(R.id.dob_fragment, signUp_fragment).commit();
            }
        } else {
            Toast.makeText(getActivity(), view.getContext().getString(R.string.age_must_be_over_eighteen), Toast.LENGTH_SHORT).show();
        }
    }

    // this method will return the years difference
    public int getDiffYears(Date first, Date last) {
        Calendar a = getCalendar(first);
        Calendar b = getCalendar(last);
        int diff = b.get(YEAR) - a.get(YEAR);
        if (a.get(MONTH) > b.get(MONTH) ||
                (a.get(MONTH) == b.get(MONTH) && a.get(DATE) > b.get(DATE))) {
            diff--;
        }
        return diff;
    }

    public static Calendar getCalendar(Date date) {
        Calendar cal = Calendar.getInstance(Locale.US);
        cal.setTime(date);
        return cal;
    }


}