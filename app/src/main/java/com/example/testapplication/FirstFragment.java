package com.example.testapplication;

import static com.example.testapplication.Constants.SERVICE_SEND_SMS;
import static com.example.testapplication.Constants.SERVICE_SUBSCRIBER_INFO;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.text.Editable;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import com.example.testapplication.databinding.FragmentFirstBinding;
import com.example.testapplication.simplehttp.SimpleHttp;
import com.example.testapplication.simplehttp.SimpleHttpResponseHandler;
import com.google.android.gms.auth.api.phone.SmsRetriever;
import com.google.android.gms.auth.api.phone.SmsRetrieverClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.hbb20.CountryCodePicker;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Random;


public class FirstFragment extends Fragment {

    private FragmentFirstBinding binding;
    private final String TAG = "FirstFragment";

    TextView title;
    TextView subtitle;
    EditText editTextPhone;
    Button buttonValidate;

    View codeContainer;
    EditText code1;
    EditText code2;
    EditText code3;
    EditText code4;


    View messageBarContainer;
    TextView messageBarLabel;
    ImageView messageImageView;
    ProgressBar progressBar;

    TextView newCodeText;

    View bottomBarContainer;
    ImageButton backButton;

    String code;
    View timerContainer;
    TextView timerText;
    CountDownTimer cTimer;

    CountryCodePicker ccp;
    View phoneContainer;

    View termsContainer;
    CheckBox termsCheckBox;
    TextView termsText;
    Boolean termsChecked = false;


    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {

        binding = FragmentFirstBinding.inflate(inflater, container, false);
        return binding.getRoot();

    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d(TAG, Locale.getDefault().getCountry());

        title = binding.title;
        subtitle = binding.subtitle;
        editTextPhone = binding.editTextPhone;
        buttonValidate = binding.buttonValidate;

        codeContainer = binding.codeContainer;
        code1 = binding.code1;
        code2 = binding.code2;
        code3 = binding.code3;
        code4 = binding.code4;

        progressBar = binding.progressBar;

        messageBarContainer = binding.messageBarContainer;
        messageImageView = binding.messageImageView;
        messageBarLabel = binding.messageBarLabel;

        newCodeText = binding.newCodeText;

        SpannableString content0 = new SpannableString(getString(R.string.signin_dialog_button_code));
        content0.setSpan(new UnderlineSpan(), 0, content0.length(), 0);
        newCodeText.setText(content0);

        bottomBarContainer = binding.bottomBarContainer;
        backButton = binding.backButton;

        timerContainer = binding.timerContainer;
        timerText = binding.timerText;

        ccp = binding.ccp;
        phoneContainer = binding.phoneContainer;

        termsContainer = binding.termsContainer;
        termsCheckBox = binding.termsCheckBox;
        termsText = binding.termsText;

        SpannableString content = new SpannableString(getString(R.string.signin_dialog_terms_link));
        content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
        termsText.setText(content);

        initViews();

        buttonValidate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(editTextPhone.getText().length()<8){
                    Toast.makeText(getContext(), R.string.signin_dialog_validation, Toast.LENGTH_LONG).show();
                    return;
                }
                editTextPhone.clearFocus();
                hideKeyBoard(view);
                sendCode();
            }
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                initViews();
            }
        });

        newCodeText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendCode();
            }
        });

        termsCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                termsChecked = isChecked;
                buttonValidate.setEnabled(termsChecked);
            }
        });

        termsText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://viva.com.bo"));
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                getContext().startActivity(intent);
            }
        });

       initValidationForm();

    }

    private void initViews(){
        phoneContainer.setVisibility(View.VISIBLE);
        buttonValidate.setVisibility(View.VISIBLE);
        title.setText(R.string.signin_dialog_title);
        subtitle.setText(R.string.signin_dialog_subtitle);
        messageBarContainer.setVisibility(View.GONE);
        newCodeText.setVisibility(View.GONE);
        codeContainer.setVisibility(View.GONE);
        bottomBarContainer.setVisibility(View.GONE);
        buttonValidate.setEnabled(true);
        timerContainer.setVisibility(View.GONE);
        if(cTimer!=null){
            cTimer.cancel();
        }
        termsContainer.setVisibility(View.VISIBLE);
        buttonValidate.setEnabled(termsChecked);
    }

    private void changeViews(){
        phoneContainer.setVisibility(View.GONE);
        codeContainer.setVisibility(View.VISIBLE);
        buttonValidate.setVisibility(View.GONE);
        title.setText(R.string.signin_dialog_title1);
        subtitle.setText(R.string.signin_dialog_subtitle1);
        newCodeText.setVisibility(View.VISIBLE);
        bottomBarContainer.setVisibility(View.VISIBLE);
        messageBarContainer.setVisibility(View.GONE);
        termsContainer.setVisibility(View.GONE);
    }

    private void initValidationForm(){
        code1.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable e) {
                if(e.toString().isEmpty()) {
                    hideMessageBar();
                } else {
                    code2.requestFocus();
                    validateCode();
                }
            }
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                //nothing needed here...
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //nothing needed here...
            }
        });

        code2.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable e) {
                if(e.toString().isEmpty()) {
                    code1.requestFocus();
                    hideMessageBar();
                } else {
                    code3.requestFocus();
                    validateCode();
                }
            }
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                //nothing needed here...
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //nothing needed here...
            }
        });

        code3.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable e) {
                if(e.toString().isEmpty()) {
                    code2.requestFocus();
                    hideMessageBar();
                } else {
                    code4.requestFocus();
                    validateCode();
                }
            }
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                //nothing needed here...
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //nothing needed here...
            }
        });

        code4.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable e) {
                if(e.toString().isEmpty()) {
                    code3.requestFocus();
                    hideMessageBar();
                } else {
                    validateCode();
                }
            }
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                //nothing needed here...
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //nothing needed here...
            }
        });
    }

    private boolean isTestPhone(){
        String phone = ccp.getSelectedCountryCode() + editTextPhone.getText().toString();
        if(phone.equalsIgnoreCase("59170700000")) return true;
        else return false;
    }

    private void validateCode(){
        Log.d(TAG, "validateCode");
        if (code1.getText().length()==1 && code2.getText().length()==1 && code3.getText().length()==1 && code4.getText().length()==1 ){
            String codeEntered = code1.getText().toString() + code2.getText().toString() + code3.getText().toString() + code4.getText().toString();

            code1.clearFocus();
            code2.clearFocus();
            code3.clearFocus();
            code4.clearFocus();
            hideKeyBoard(code4);
            showMessageBar(R.string.signin_dialog_loading2, true, false, -1);

            if(codeEntered.equalsIgnoreCase(code) || isTestPhone()){

                showMessageBar(R.string.signin_dialog_success, false, true, R.drawable.nt_ok_icon);

                Map<String, String> params = new HashMap<>();
                params.put("entityCode", "ADS_SCRIPTS");
                SimpleHttp.post(SERVICE_SUBSCRIBER_INFO, params, new SimpleHttpResponseHandler() {
                    @Override
                    public void onResponse(int responseCode, String responseBody) {
                        Log.i(TAG, String.valueOf(responseCode));
                        Log.i(TAG, responseBody);
                        if(responseCode==200){
                            NavHostFragment.findNavController(FirstFragment.this)
                                    .navigate(R.id.action_FirstFragment_to_SecondFragment);
                        }
                    }
                });
            } else {
                showMessageBar(R.string.signin_dialog_error1, false, true, R.drawable.nt_error_icon);
            }
        } else {
            hideMessageBar();
        }
    }


    private void sendCode(){
        Random rand = new Random();
        code = String.format(Locale.getDefault(),"%04d", rand.nextInt(10000));
        Log.d(TAG, "code: " + code);

        showMessageBar(R.string.signin_dialog_loading1, true, false, -1);
        buttonValidate.setEnabled(false);

        Map<String, String> params = new HashMap<>();
        params.put("deviceId", "1");
        params.put("number", ccp.getSelectedCountryCode() + editTextPhone.getText().toString());
        String message = getResources().getString(R.string.signin_dialog_sms) + " " + code;
        params.put("message", message);
        SimpleHttp.post(SERVICE_SEND_SMS, params, new SimpleHttpResponseHandler() {
            @Override
            public void onResponse(int responseCode, String responseBody) {
                Log.i(TAG, String.valueOf(responseCode));
                Log.i(TAG, responseBody);
                hideMessageBar();
                if(responseCode==200){
                    changeViews();
                    startTimerNewCode();
                    // TODO delete this, only for test
                    Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void startTimerNewCode(){
        newCodeText.setEnabled(false);
        newCodeText.setTextColor(Color.parseColor("#494949"));
        timerContainer.setVisibility(View.VISIBLE);
        String time = "00:60";
        timerText.setText(time);
        if(cTimer!=null){
            cTimer.cancel();
        }
        cTimer = new CountDownTimer(60000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                long s = millisUntilFinished / 1000;
                String t = s>=10?String.valueOf(s):"0"+String.valueOf(s);
                String r = "00:" + t;
                timerText.setText(r);
            }
            @Override
            public void onFinish() {
                newCodeText.setEnabled(true);
                newCodeText.setTextColor(Color.parseColor("#FFFFFF"));
                timerContainer.setVisibility(View.GONE);
            }
        };
        cTimer.start();
    }

    private void showMessageBar(int stringId, boolean showProgressBar, boolean showImage, int imageId){
        messageBarContainer.setVisibility(View.VISIBLE);
        messageImageView.setVisibility(showImage?View.VISIBLE:View.GONE);
        progressBar.setVisibility(showProgressBar?View.VISIBLE:View.GONE);
        messageBarLabel.setText(stringId);
        if(showImage && imageId!=-1) messageImageView.setImageResource(imageId);
    }

    private void hideMessageBar(){
        messageBarContainer.setVisibility(View.GONE);
    }

    private void hideKeyBoard(View view){
        InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }


    private void readSMS(){
        // Get an instance of SmsRetrieverClient, used to start listening for a matching
        // SMS message.
        SmsRetrieverClient client = SmsRetriever.getClient(getContext());

        // Starts SmsRetriever, which waits for ONE matching SMS message until timeout
        // (5 minutes). The matching SMS message will be sent via a Broadcast Intent with
        // action SmsRetriever#SMS_RETRIEVED_ACTION.
        Task<Void> task = client.startSmsRetriever();

        // Listen for success/failure of the start Task. If in a background thread, this
        // can be made blocking using Tasks.await(task, [timeout]);
        task.addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                // Successfully started retriever, expect broadcast intent
                // ...
            }
        });

        task.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // Failed to start retriever, inspect Exception for more details
                // ...
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}