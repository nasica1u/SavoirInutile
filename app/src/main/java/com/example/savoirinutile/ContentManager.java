package com.example.savoirinutile;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class ContentManager
{
    private static final String TAG       = ContentManager.class.getSimpleName();
    private static final String DATA_JSON = "data_json";
    private static final String FILE_NAME = "SavoirInutile";

    private static ContentManager   mInstance;
    private        List<SavoirItem> itemsList;
    private        DiskCache        dataCache;

    private HandlerThread mHandlerThread;
    private Handler mWorker;
    private Handler mReporter;

    public interface DataListener
    {
        void notifyRetrieved(List<SavoirItem> savoirItem);
        void notifyNotRetrieved();
    }

    private ContentManager(Context context)
    {
        dataCache = new DiskCache(context, "items");
        mHandlerThread = new HandlerThread("CThread");
        mHandlerThread.start();
        mWorker = new Handler(mHandlerThread.getLooper());
    }

    public void startThread(){
        if(mHandlerThread != null && !mHandlerThread.isAlive())
            mHandlerThread.start();
    }

    public void quitThread(){
        if(mHandlerThread != null && mHandlerThread.isAlive())
            mHandlerThread.quit();
    }

    public void retrieveData(final DataListener listener)
    {
        mReporter = new Handler(Looper.getMainLooper()){
            @Override
            public void handleMessage(Message msg) {
                if(msg.obj == null)
                {
                    listener.notifyNotRetrieved();
                    return;
                }

                List<SavoirItem> items = (List<SavoirItem>) msg.obj;
                if(items.isEmpty())
                    listener.notifyNotRetrieved();
                else
                    listener.notifyRetrieved(items);
            }
        };
        mWorker.post(getDataHandlerThread());
    }

    private Runnable getDataHandlerThread(){
        return new Runnable() {
            @Override
            public void run() {
                Message reporterMessage = mReporter.obtainMessage();
                URL urlObject = null;
                try
                {
                    //CHECK IF CACHE
                    String cache = dataCache.getText(FILE_NAME);
                    if(cache != null && !cache.isEmpty() && !Utils.isNetworkAvailable(MainActivity.getAppContext()))
                    {
                        reporterMessage.obj = retrieveDataFromJson(cache);
                    }
                    Log.i(TAG, "ici");
                    urlObject = new URL("https://serginho.goodbarber.com/front/get_items/939101/26902416/?local=1");
                    HttpURLConnection conn = (HttpURLConnection) urlObject.openConnection();
                    conn.setReadTimeout(7000);
                    conn.setConnectTimeout(7000);
                    conn.setRequestMethod("GET");
                    conn.setDoInput(true);
                    conn.connect();
                    Log.i(TAG, "là");
                    int status = conn.getResponseCode();
                    if (status != 200 && reporterMessage.obj == null)
                    {
                        Log.e(TAG, "Nothing found!");
                    }
                    InputStream is = conn.getInputStream();
                    String jsonAsString = Utils.getTextFromStream(is);
                    if (Utils.isStringValid(jsonAsString))
                    {
                        //SAVE JSON IN CACHE
                        dataCache.saveText(jsonAsString, FILE_NAME);
                        JSONObject responseJSON = new JSONObject(jsonAsString);
                        JSONArray itemsArray = responseJSON.optJSONArray("items");
                        List<SavoirItem> items = new ArrayList<>();
                        for (int i = 0; i < itemsArray.length(); i++) {
                            JSONObject object = itemsArray.getJSONObject(i);
                            items.add(new SavoirItem(object));
                        }
                        reporterMessage.obj = items;
                    }
                    Log.i(TAG, jsonAsString);
                }
                catch (Exception e)
                {
                    Log.i(TAG, "c'est cassé");
                }
                finally {
                    mReporter.sendMessage(reporterMessage);
                }
            }
        };
    }

    public static ContentManager getInstance(Context c)
    {
        if (mInstance == null)
            mInstance = new ContentManager(c);

        return mInstance;
    }

    private static List<SavoirItem> retrieveDataFromJson(String jsonString)
    {
        List<SavoirItem> itemsList = new ArrayList<>();
        try
        {
            JSONObject jsonItems = new JSONObject(jsonString);
            JSONArray itemsArray = jsonItems.getJSONArray("items");
            int nbPlaces = itemsArray.length();
            for (int i = 0; i < nbPlaces; i++)
            {
                itemsList.add(new SavoirItem(itemsArray.getJSONObject(i)));
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
        return itemsList;
    }

    public void getRequestData(DataListener listener)
    {
        new SavoirItemsAsynctask(listener).execute();
    }

    public class SavoirItemsAsynctask extends AsyncTask<Void, Void, List<SavoirItem>>
    {

        private WeakReference<DataListener> mListenerRef;

        public SavoirItemsAsynctask(DataListener listener)
        {
            mListenerRef = new WeakReference<>(listener);
        }

        @Override
        protected List<SavoirItem> doInBackground(Void... voids)
        {
            URL urlObject = null;
            try
            {
                //CHECK IF CACHE
                String cache = dataCache.getText(FILE_NAME);
                if(cache != null && !cache.isEmpty() && !Utils.isNetworkAvailable(MainActivity.getAppContext()))
                {
                    return retrieveDataFromJson(cache);
                }
                Log.i(TAG, "ici");
                urlObject = new URL("https://serginho.goodbarber.com/front/get_items/939101/26902416/?local=1");
                HttpURLConnection conn = (HttpURLConnection) urlObject.openConnection();
                conn.setReadTimeout(7000);
                conn.setConnectTimeout(7000);
                conn.setRequestMethod("GET");
                conn.setDoInput(true);
                conn.connect();
                Log.i(TAG, "là");
                int status = conn.getResponseCode();
                if (status != 200)
                {
                    return null;
                }
                InputStream is = conn.getInputStream();
                String jsonAsString = Utils.getTextFromStream(is);
                if (Utils.isStringValid(jsonAsString))
                {
                    //SAVE JSON IN CACHE
                    dataCache.saveText(jsonAsString, FILE_NAME);
                    JSONObject responseJSON = new JSONObject(jsonAsString);
                    JSONArray itemsArray = responseJSON.optJSONArray("items");
                    List<SavoirItem> items = new ArrayList<>();
                    for (int i = 0; i < itemsArray.length(); i++) {
                        JSONObject object = itemsArray.getJSONObject(i);
                        items.add(new SavoirItem(object));
                    }
                    return items;
                }
                Log.i(TAG, jsonAsString);
            }
            catch (Exception e)
            {
                Log.i(TAG, "c'est cassé");
            }
            return null;
        }

        @Override
        protected void onPostExecute(List<SavoirItem> savoirItem)
        {
            super.onPostExecute(savoirItem);

            if (mListenerRef.get() != null)
            {
                if (savoirItem != null)
                {
                    mListenerRef.get().notifyRetrieved(savoirItem);
                }
                else
                {
                    mListenerRef.get().notifyNotRetrieved();
                }
            }
        }
    }
}
