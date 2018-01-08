package com.mydomain.takehomeapp;
/*
 * Description: product list main activity
 * Created by jmonani on 12/21/17.
 *
 */

import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.mydomain.takehomeapp.ProductCatalogue.ProductListAdapter;
import com.mydomain.takehomeapp.services.ProductAPIService;
import com.mydomain.takehomeapp.services.apihelper.BaseApiResponse;
import com.mydomain.takehomeapp.services.apihelper.ProductDetailResponse;
import com.mydomain.takehomeapp.services.apihelper.ProductDetails;
import com.mydomain.takehomeapp.utility.AsyncCallbackInf;
import com.mydomain.takehomeapp.utility.ScrollListener;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private List<ProductDetails> mProductDetailsList;
    private static final int SCROLL_DOWN = 1;
    private static final int SUCCESS_RESPONSE = 200;
    private RecyclerView mProductListRecyclerView;
    private ProductListAdapter mProductListAdapter;
    private TextView mEmptyListMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mProductDetailsList = new ArrayList<>();
        setContentView(R.layout.activity_main);
        mProductListRecyclerView = findViewById(R.id.product_recycler_view);
        mEmptyListMessage = findViewById(R.id.product_list_empty_message);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        mProductListRecyclerView.setLayoutManager(layoutManager);
        mProductListAdapter = new ProductListAdapter((ArrayList<ProductDetails>)mProductDetailsList, this);
        mProductListRecyclerView.setAdapter(mProductListAdapter);

        ScrollListener mScrollListener = new ScrollListener(layoutManager) {
            @Override
            public boolean onLoadMore(int page, int totalItemsCount, RecyclerView recyclerView) {
                if (mProductListRecyclerView.canScrollVertically(SCROLL_DOWN)) {
                    ProductDetails product = mProductDetailsList.get(mProductDetailsList.size() - 1);
                    fetchProductList(product.getPageNumber() + 1);
                }
                return true;
            }
        };
        mScrollListener.resetState();
        mProductListRecyclerView.setOnScrollListener(mScrollListener);
        if(mProductDetailsList.size() <= 0)
            fetchProductList(1);
    }

    public void onResume() {
        super.onResume();
    }


    public void fetchProductList(int page) {

        new ProductAPIService().getProductListAsync(page, new AsyncCallbackInf() {
            @Override
            public void onResponseCallback(int result, BaseApiResponse response) {
                ProductDetailResponse detailResponse = (ProductDetailResponse) response;
                if(null != detailResponse && null != detailResponse.status && SUCCESS_RESPONSE == detailResponse.status) {
                    setProductList(detailResponse);
                    Log.i("MainActivity", "fetchProductList: List of products: " + mProductDetailsList.size());
                    mProductListAdapter.setData((ArrayList<ProductDetails>) mProductDetailsList);
                    mProductListAdapter.notifyDataSetChanged();
                    mEmptyListMessage.setVisibility(View.INVISIBLE);
                    mProductListRecyclerView.setVisibility(View.VISIBLE);
                } else {
                    // error scenario
                    assert detailResponse != null;
                    Log.e("MainActivity", "Error in fetching product list:" + detailResponse.error);
                    mEmptyListMessage.setText(detailResponse.error);
                    mEmptyListMessage.setVisibility(View.VISIBLE);
                    mProductListRecyclerView.setVisibility(View.INVISIBLE);
                    Snackbar.make(findViewById(android.R.id.content), detailResponse.error, Snackbar.LENGTH_LONG).show();
                }
            }
        });
    }

    private void setProductList(ProductDetailResponse response) {
        int pageNum = response.pageNumber;
        List<ProductDetails> products = response.products;
        if(null != products) {
            for (ProductDetails details: products) {
                details.setPageNumber(pageNum);
                mProductDetailsList.add(details);
            }
            //TODO
            HomeApplication.getInstance().setProductList(mProductDetailsList);
        }
    }
}
