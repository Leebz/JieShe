package com.whut.JieShe.fragments;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.whut.JieShe.OrganizationDetailActivity;
import com.whut.JieShe.R;
import com.whut.JieShe.adapter.QuickOrganizationAdapter;
import com.whut.JieShe.bean.MyUser;
import com.whut.JieShe.bean.Organization;
import com.tapadoo.alerter.Alerter;

import java.util.ArrayList;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.datatype.BmobQueryResult;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.GetListener;
import cn.bmob.v3.listener.SQLQueryListener;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * Use the {@link OrganizationCollectionFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class OrganizationCollectionFragment extends Fragment implements BaseQuickAdapter.OnItemClickListener {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER

    private OnFragmentInteractionListener mListener;

    private RecyclerView mOrganizationList;
    private SwipeRefreshLayout mRefreshLayout;
    private QuickOrganizationAdapter mOrganizationAdapter;
    private BmobUser mUser;
    private MyUser mMyUser;

    public OrganizationCollectionFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     */
    // TODO: Rename and change types and number of parameters
    public static OrganizationCollectionFragment newInstance(BmobUser user) {
        OrganizationCollectionFragment fragment = new OrganizationCollectionFragment();
        fragment.mUser = user;
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_organization_collection, container, false);

        mRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipe_refresh);
        mRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refresh();
            }
        });
        mOrganizationList = (RecyclerView) rootView.findViewById(R.id.my_organization_collection_list);
        mOrganizationList.setLayoutManager(new LinearLayoutManager(getContext(),LinearLayoutManager.VERTICAL,false));
        mOrganizationAdapter = new QuickOrganizationAdapter(getContext(),new ArrayList<Organization>());
        mOrganizationList.setAdapter(mOrganizationAdapter);
        mOrganizationAdapter.setOnItemClickListener(this);

        refresh();

        return rootView;
    }

    private void refresh() {
        BmobQuery<MyUser> query = new BmobQuery<MyUser>();
        query.getObject(getContext(), mUser.getObjectId(), new GetListener<MyUser>() {
            @Override
            public void onSuccess(MyUser myUser) {
                mMyUser = myUser;
                loadData();
            }

            @Override
            public void onFailure(int i, String s) {
                Alerter.create(getActivity()).setTitle("获取信息失败").setText(s).show();
            }
        });
    }

    private void loadData() {
        if(mMyUser.getOrganCollection()==null||mMyUser.getOrganCollection().size()==0){
            return;
        }
        StringBuffer sb = new StringBuffer();
        sb.append("select * from Organization where ");
        for(String id:mMyUser.getOrganCollection()){
            sb.append("objectId='");
            sb.append(id);
            sb.append("' ");
            sb.append("or ");
        }
        sb.delete(sb.length()-4,sb.length()-1);

        BmobQuery<Organization> query = new BmobQuery<>();
        query.setSQL(sb.toString());
        query.doSQLQuery(getContext(), new SQLQueryListener<Organization>(){
            @Override
            public void done(BmobQueryResult<Organization> bmobQueryResult, BmobException e) {
                if(e ==null){
                    mOrganizationAdapter.addData(bmobQueryResult.getResults());
                    mRefreshLayout.setRefreshing(false);
                }else{
                    Alerter.create(getActivity())
                            .setTitle("刷新失败")
                            .setText(e.getMessage())
                            .show();
                    mRefreshLayout.setRefreshing(false);
                }
            }
        });
    }

    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(null);
        }
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
        intent.putExtra("organization", mOrganizationAdapter.getData().get(position));
        startActivity(intent);
    }
}
