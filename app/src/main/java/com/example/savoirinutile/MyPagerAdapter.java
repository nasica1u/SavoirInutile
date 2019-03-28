package com.example.savoirinutile;

import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

public class MyPagerAdapter extends PagerAdapter {

    private List<SavoirItem> mItems;

    public MyPagerAdapter(List<SavoirItem> items){
        mItems = items;
    }

    @NonNull
    @Override
    public View instantiateItem(@NonNull ViewGroup container, int position) {
        LayoutInflater inflater = LayoutInflater.from(container.getContext());
        View v = inflater.inflate(R.layout.page_cell, container, false);
        container.addView(v);
        update(mItems.get(position), v);
        return v;
    }

    public void update(SavoirItem item, View v){
        TextView title = v.findViewById(R.id.view_title);
        TextView date = v.findViewById(R.id.view_date);
        TextView description = v.findViewById(R.id.view_description);

        title.setText(item.getTitle());
        date.setText(item.getDate());
        description.setText(item.getDescription());
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);
    }

    @Override
    public int getCount() {
        return mItems.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object o) {
        return view == o;
    }
}
