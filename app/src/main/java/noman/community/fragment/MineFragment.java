package noman.community.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.orhanobut.logger.BuildConfig;
import com.quranreading.qibladirection.R;

import java.util.ArrayList;
import java.util.List;

import noman.CommunityGlobalClass;
import noman.community.activity.ComunityActivity;
import noman.community.adapter.MinePrayerAdapter;
import noman.community.model.Prayer;
import noman.community.model.SignInRequest;
import noman.community.model.SignUpResponse;
import noman.community.utility.DebugInfo;
import retrofit.Call;
import retrofit.Response;
import retrofit.Retrofit;

/**
 * Created by Administrator on 11/18/2016.
 */

public class MineFragment extends Fragment {

    SwipeRefreshLayout mSwipeRefreshLayout;
    RecyclerView mRecyclerView;
    MinePrayerAdapter mPrayerAdapter;
    boolean isInternetAvailable = false;

    FloatingActionButton fabMine;
    ComunityActivity mComunityActivity;
    TextView noData;

    List<Prayer> mineList=new ArrayList<>();

    public static MineFragment newInstance(ComunityActivity mComunityActivity) {
        MineFragment myFragment = new MineFragment();

        myFragment.mComunityActivity = mComunityActivity;
        CommunityGlobalClass.mMineFragment = myFragment;
        return myFragment;

    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.layout_prayer_tab, container, false);
        isInternetAvailable = CommunityGlobalClass.getInstance().isInternetOn();
        noData = (TextView) rootView.findViewById(R.id.txt_no);
        noData.setVisibility(View.VISIBLE);
        mSwipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipeRefreshLayout);
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_prayer);
        mRecyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(linearLayoutManager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Refresh items
                mSwipeRefreshLayout.setRefreshing(false);

            }
        });
        mComunityActivity.fab.attachToRecyclerView(mRecyclerView);
        //Atach fab with recycler view


        //  CommunityGlobalClass.getInstance().sendAnalyticsScreen("Community Mine");
        CommunityGlobalClass.mSignInRequests.setModule_id(1);
        minePrayer(CommunityGlobalClass.mSignInRequests);


        return rootView;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);


    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        //  menu.findItem(R.id.action_filter).setVisible(false);

    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);

        mComunityActivity.menu_filter.setVisibility(View.GONE);
        if (isVisibleToUser) {
           // if (getMineListAfterPosting() == null || getMineListAfterPosting().size() == 0) {
                if (mineList.size() != 0) {
                  //  onItemsLoadComplete();
                    noData.setVisibility(View.GONE);
                } else {
                    noData.setVisibility(View.VISIBLE);
                }
            }

    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //Because it show nullpointer in scrollview in recycler due to in tab fragment


    }

    public void onLoadMineList() {
        // Update the adapter and notify data set changed
        // Update the adapter and notify data set changed

        if (  CommunityGlobalClass.minePrayerModel.size() > 0) {
            noData.setVisibility(View.GONE);
        } else {
            noData.setVisibility(View.VISIBLE);
        }
        mPrayerAdapter = new MinePrayerAdapter(  CommunityGlobalClass.minePrayerModel, getActivity());
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setAdapter(mPrayerAdapter);
        if (mSwipeRefreshLayout.isRefreshing()) {
            if (mSwipeRefreshLayout != null) {
                mSwipeRefreshLayout.setRefreshing(false);
                mSwipeRefreshLayout.destroyDrawingCache();
                mSwipeRefreshLayout.clearAnimation();
            }

        }


    }


    public List<Prayer> minePrayer(final SignInRequest mSignUpRequest) {

     //   CommunityGlobalClass.getInstance().showLoading(getActivity());
        Call<SignUpResponse> call = CommunityGlobalClass.getRestApi().signInUser(mSignUpRequest);
        call.enqueue(new retrofit.Callback<SignUpResponse>() {

            @Override
            public void onResponse(Response<SignUpResponse> response, Retrofit retrofit) {

             //   CommunityGlobalClass.getInstance().cancelDialog();
                mineList= response.body().getPrayers();
                mPrayerAdapter = new MinePrayerAdapter(mineList, getActivity());
                mRecyclerView.setAdapter(mPrayerAdapter);
                if (mSwipeRefreshLayout.isRefreshing()) {
                    if (mSwipeRefreshLayout != null) {
                        mSwipeRefreshLayout.setRefreshing(false);
                        mSwipeRefreshLayout.destroyDrawingCache();
                        mSwipeRefreshLayout.clearAnimation();
                    }

                }
            }

            @Override
            public void onFailure(Throwable t) {
              //  CommunityGlobalClass.getInstance().cancelDialog();
                if (BuildConfig.DEBUG) DebugInfo.loggerException("SignUp-Failure" + t.getMessage());
                CommunityGlobalClass.getInstance().showServerFailureDialog(getActivity());
            }
        });

        return mineList;
    }
}
