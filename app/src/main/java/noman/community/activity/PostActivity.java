package noman.community.activity;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.orhanobut.logger.BuildConfig;
import com.quranreading.helper.DBManager;
import com.quranreading.qibladirection.GlobalClass;
import com.quranreading.qibladirection.R;
import com.quranreading.sharedPreference.LocationPref;

import cn.pedant.SweetAlert.SweetAlertDialog;
import noman.Ads.AdIntegration;
import noman.CommunityGlobalClass;
import noman.community.adapter.PrayerAdapter;
import noman.community.model.PostPrayerRequest;
import noman.community.model.PostResponse;
import noman.community.prefrences.SavePreference;
import noman.community.utility.DebugInfo;
import retrofit.Call;
import retrofit.Response;
import retrofit.Retrofit;

/**
 * Created by Administrator on 11/17/2016.
 */

public class PostActivity extends AdIntegration {

    EditText textPrayer;
    TextView txtCounter, txtUserInfo;
    int limitMessage = 220;
    Button btnPost;
    CheckBox checkBoxLocation;
    SweetAlertDialog taskCompDialog;
    boolean showLocation = true;
    String country;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

      /*  country =  getCountryName(this);

        if(country.isEmpty() || country==null ||country.length()==0)
        {
            boolean isInternetAvaliable = CommunityGlobalClass.getInstance().isInternetOn();
            //Get Country Name while app start
            if (isInternetAvaliable) {
                country  =  CommunityGlobalClass.getInstance().getCountryName();
            }
        }

*/
        LocationPref locationPref = new LocationPref(this);
        country = locationPref.getCountryCode();


        // ********************************** Get Country name use pref *******************8
        DBManager dbObj = new DBManager(this);
        dbObj.open();
        Cursor cCode = dbObj.getCountrybyCodes(country);
        if (cCode.moveToFirst()) {
            country = cCode.getString(cCode.getColumnIndex(DBManager.FLD_COUNTRY_NAME));
            cCode.close();
            dbObj.close();
        } else {
            cCode.close();
            dbObj.close();
        }
       // *****************************************

        setContentView(R.layout.activity_post);
        if (!((GlobalClass) getApplication()).isPurchase) {
            super.showBannerAd(this, (LinearLayout) findViewById(R.id.linearAd));
        }

        RelativeLayout img_back=(RelativeLayout)findViewById(R.id.layout_drawer_menu_ic) ;
        img_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideKeyBoard();
                onBackPressed();
            }
        });
        TextView titleheader = (TextView) findViewById(R.id.txt_header_title);
        titleheader.setText(getResources().getString(R.string.activity_post));

        textPrayer = (EditText) findViewById(R.id.edit_prayer);
        txtCounter = (TextView) findViewById(R.id.txt_limit);
        txtUserInfo = (TextView) findViewById(R.id.txt_info_user);
        btnPost = (Button) findViewById(R.id.btnPost);
        checkBoxLocation = (CheckBox) findViewById(R.id.check_location);

        textPrayer.addTextChangedListener(mTextEditorWatcher);
        checkBoxLocation.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                showLocation = isChecked;
                if (showLocation) { //Add user Info
                    txtUserInfo.setText(CommunityGlobalClass.mSignInRequests.getName() + " , " + country);
                } else {
                    txtUserInfo.setText(CommunityGlobalClass.mSignInRequests.getName());
                }

            }
        });
        btnPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String content = textPrayer.getText().toString().trim();
                hideKeyBoard();
                if (content.length() > 0 || !content.isEmpty()) {
                    if (content.length() < 10) {
                        CommunityGlobalClass.getInstance().showToast("Please enter atleast 10 characters.");
                        return;
                    }
                    PostPrayerRequest mRequset = new PostPrayerRequest();
                    mRequset.setUser_id(CommunityGlobalClass.mSignInRequests.getUser_id());
                    if (showLocation) {
                        mRequset.setLocation_status("0");
                    } else {
                        mRequset.setLocation_status("1");
                    }
                    mRequset.setPrayer(content);
                    mRequset.setDevice_date_time(CommunityGlobalClass.getInstance().getCurrentDate());

                    callToLoadPrayer(mRequset);
                } else {
                    CommunityGlobalClass.getInstance().showToast("Please enter your prayer request");
                }
            }
        });

        //Add user Info

        txtUserInfo.setText(CommunityGlobalClass.mSignInRequests.getName() + " , " + country);

        setupUI(findViewById(R.id.parent));

    }

    @Override
    public void onPause() {
        super.onPause();
        hideKeyBoard();
    }

    private final TextWatcher mTextEditorWatcher = new TextWatcher() {
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        public void onTextChanged(CharSequence s, int start, int before, int count) {
            //This sets a textview to the current length
            txtCounter.setText(String.valueOf(s.length()) + "/" + limitMessage);
        }

        public void afterTextChanged(Editable s) {

        }
    };



        public String getCountryName(Context mContext)
        {
            String countryCode2="";

            LocationPref locationPref = new LocationPref(mContext);
            DBManager dbObj = new DBManager(mContext);
            dbObj.open();
            //Split string to get the only point before value lat and long
            Cursor c = dbObj.getCountry(locationPref.getLatitude().split("\\.")[0] + ".", locationPref.getLongitude().split("\\.")[0] + ".");
            if (c != null) {
                if (c.moveToFirst()) {
                  countryCode2 = c.getString(c.getColumnIndex(DBManager.FLD_COUNTRY));
                }
                c.close();
                dbObj.close();


            } else {
                c.close();
                dbObj.close();
            }

            return countryCode2;
        }
    //Call web
    public void callToLoadPrayer(final PostPrayerRequest mPostPrayerRequest) {
        CommunityGlobalClass.getInstance().showLoading(PostActivity.this);
        Call<PostResponse> call = CommunityGlobalClass.getRestApi().postPrayer(mPostPrayerRequest);
        call.enqueue(new retrofit.Callback<PostResponse>() {


            @Override
            public void onResponse(Response<PostResponse> response, Retrofit retrofit) {
                CommunityGlobalClass.getInstance().cancelDialog();
                if (response.body().getState() == true) {
                    showPostDialog(response.body().getResponse().getMessage(), false);
                    CommunityGlobalClass.minePrayerModel = response.body().getPrayers();
                    //Refresh the Mine Tab
                    CommunityGlobalClass.mMineFragment.onLoadMineList();
                    PostActivity.this.finish();


                    CommunityGlobalClass.prayerCounter=0;

                } else {
                    showPostDialog("Some issue in server", true);
                }

            }

            @Override
            public void onFailure(Throwable t) {
                CommunityGlobalClass.getInstance().cancelDialog();
                if (BuildConfig.DEBUG) DebugInfo.loggerException("Post-Failure" + t.getMessage());
                CommunityGlobalClass.getInstance().showServerFailureDialog(PostActivity.this);
            }
        });


    }


    public void showPostDialog(String message, boolean isError) {
        if (isError) {
            new SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
                    .setTitleText("Error")
                    .setContentText(message)
                    .show();
        } else {
            new SweetAlertDialog(this, SweetAlertDialog.SUCCESS_TYPE)
                    .setTitleText("Posted!")
                    .setContentText(message)
                    .show();
        }
    }

    public void hideKeyBoard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    public void setupUI(View view) {
        // Set up touch listener for non-text box views to hide keyboard.
        if (!(view instanceof CheckBox)) {
            view.setOnTouchListener(new View.OnTouchListener() {
                public boolean onTouch(View v, MotionEvent event) {
                    hideKeyBoard();
                    return false;
                }
            });
        }
        //If a layout container, iterate over children and seed recursion.
        if (view instanceof ViewGroup) {
            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                View innerView = ((ViewGroup) view).getChildAt(i);
                setupUI(innerView);
            }
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handle arrow click here
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}
