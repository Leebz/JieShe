package com.whut.JieShe.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.beardedhen.androidbootstrap.BootstrapDropDown;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.whut.JieShe.OrganizationDetailActivity;
import com.whut.JieShe.R;
import com.whut.JieShe.adapter.QuickOrganizationAdapter;
import com.whut.JieShe.bean.Organization;
import com.tapadoo.alerter.Alerter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.listener.CountListener;
import cn.bmob.v3.listener.FindListener;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link MainColumnFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MainColumnFragment extends Fragment implements BaseQuickAdapter.OnItemClickListener {

    private OnFragmentInteractionListener mListener;

    private BootstrapDropDown mSortDropDown;
    private BootstrapDropDown mTagDropDown;
    private RecyclerView mOrganizationListView;

    private QuickOrganizationAdapter mOrganizationAdapter;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    private int mCountEveryTime = 10; //每次加载条数
    private int mCurrentCount = 0;    //已加载条数
    private int mTotalCount = -1;    //全部数据条数

    public MainColumnFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     */
    public static MainColumnFragment newInstance(OnFragmentInteractionListener listener) {
        MainColumnFragment fragment = new MainColumnFragment();
        fragment.mListener = listener;
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main_column, container, false);
        mSwipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipe_refresh);
        mSortDropDown = (BootstrapDropDown) rootView.findViewById(R.id.sort_drop_down);
        mTagDropDown = (BootstrapDropDown) rootView.findViewById(R.id.type_drop_down);
        mSortDropDown.setOnDropDownItemClickListener(new BootstrapDropDown.OnDropDownItemClickListener() {
            @Override
            public void onItemClick(ViewGroup parent, View v, int id) {
                String sortData[] = getActivity().getResources().getStringArray(R.array.dropdown_organization_sort_data);
                mSortDropDown.setText(sortData[id]);
                refresh();
            }
        });
        mTagDropDown.setOnDropDownItemClickListener(new BootstrapDropDown.OnDropDownItemClickListener() {
            @Override
            public void onItemClick(ViewGroup parent, View v, int id) {
                String typeData[] = getActivity().getResources().getStringArray(R.array.dropdown_organization_type_data);
                mTagDropDown.setText(typeData[id]);
                refresh();
            }
        });

        mOrganizationListView = (RecyclerView) rootView.findViewById(R.id.organization_list);
        mOrganizationListView.setLayoutManager(new LinearLayoutManager(getContext(),LinearLayoutManager.VERTICAL,false));
        mOrganizationAdapter = new QuickOrganizationAdapter(getContext(),new ArrayList<Organization>());
        mOrganizationListView.setAdapter(mOrganizationAdapter);
        mOrganizationAdapter.setOnItemClickListener(this);

        mOrganizationAdapter.setOnLoadMoreListener(new BaseQuickAdapter.RequestLoadMoreListener() {
            @Override
            public void onLoadMoreRequested() {
                if(mTotalCount==-1){
                    BmobQuery<Organization> query = new BmobQuery<>();
                    query.count(getContext(),Organization.class,new CountListener() {
                        @Override
                        public void onSuccess(int i) {
                            mTotalCount = i;
                            loadMore();
                        }

                        @Override
                        public void onFailure(int i, String s) {

                        }
                    });
                }else{
                    loadMore();
                }
            }
        }, mOrganizationListView);

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refresh();
            }
        });

        refresh();

        return rootView;
    }

    private void refresh() {
        mCurrentCount = 0;
        filterData(new FindListener<Organization>() {
            @Override
            public void onSuccess(List<Organization> list) {
                mOrganizationAdapter.getData().clear();
                mOrganizationAdapter.addData(list);
                mCurrentCount = mOrganizationAdapter.getData().size();
                mSwipeRefreshLayout.setRefreshing(false);
            }

            @Override
            public void onError(int i, String s) {
                mOrganizationAdapter.loadMoreFail();
                Alerter.create(getActivity())
                        .setTitle("刷新失败")
                        .setText(s)
                        .show();
                mSwipeRefreshLayout.setRefreshing(false);
            }
        });

        //顺便刷新一下mTotalCount
        BmobQuery<Organization> query = new BmobQuery<>();
        query.count(getContext(),Organization.class,new CountListener() {
            @Override
            public void onSuccess(int i) {
                mTotalCount = i;
            }

            @Override
            public void onFailure(int i, String s) {

            }
        });
    }

    private void loadMore() {
        if(mCurrentCount >= mTotalCount){
            mOrganizationAdapter.loadMoreEnd();//数据全部加载完毕
        }else{
            filterData(new FindListener<Organization>() {
                @Override
                public void onSuccess(List<Organization> list) {
                    mOrganizationAdapter.addData(list);
                    mCurrentCount = mOrganizationAdapter.getData().size();
                    mOrganizationAdapter.loadMoreComplete();
                }

                @Override
                public void onError(int i, String s) {
                    mOrganizationAdapter.loadMoreFail();
                    Alerter.create(getActivity())
                            .setTitle("加载失败")
                            .setText(s)
                            .show();
                    mOrganizationAdapter.loadMoreFail();
                }
            });
        }
    }

    /**
     * 根据当前的条件筛选社团列表的显示
     */
    private void filterData(FindListener<Organization> listener){
        BmobQuery<Organization> query = new BmobQuery<>();
        query.setSkip(mCurrentCount);
        query.setLimit(mCountEveryTime);

        String tag = mTagDropDown.getText().toString();
        if(!(tag.startsWith("类型")||tag.startsWith("全部"))){
            String [] tags = {tag};
            query.addWhereContainsAll("tags", Arrays.asList(tags));
        }

        String sort = mSortDropDown.getText().toString();
        if(sort.equals("活动时间")){
            query.order("-recentActivityTime");
        } else {//默认以热度排序
            query.order("-totalJoinedUser");
        }

        query.findObjects(getContext(), listener);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
        Intent intent = new Intent(getContext(),OrganizationDetailActivity.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable("organization", mOrganizationAdapter.getData().get(position));
        intent.putExtras(bundle);
        startActivity(intent);
    }
}
