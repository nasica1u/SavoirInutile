package com.example.savoirinutile;

import android.app.ActionBar;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements ContentManager.DataListener
{
    private ViewPager mPager;
    private List<SavoirItem> mItems;
    private static Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = getApplicationContext();

        mPager = findViewById(R.id.pager);
        doRequest(false);
    }

    @Override
    public void notifyRetrieved(List<SavoirItem> savoirItem)
    {
        mItems = savoirItem;
        mPager.setAdapter(new MyPagerAdapter(savoirItem));
        Toast.makeText(this, "Contenu chargé.", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void notifyNotRetrieved()
    {
        Toast.makeText(this, "Echec de chargement.", Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.custom_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.refreshButton:
                item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        doRequest(false);
                        return true;
                    }
                });
        }
        return false;
    }

    /*
        useAsync : if true request will be done via AsyncTask, else it is done with HandlerThread
     */
    public void doRequest(boolean useAsync){
        if(useAsync)
            ContentManager.getInstance(this).getRequestData(this);
        else
            ContentManager.getInstance(this).retrieveData(this);
    }

    public static Context getAppContext()
    {
        return mContext;
    }
}
