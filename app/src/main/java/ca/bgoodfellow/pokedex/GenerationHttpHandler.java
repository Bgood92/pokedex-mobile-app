package ca.bgoodfellow.pokedex;

import android.os.AsyncTask;

import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by goodf on 2018-03-21.
 */

public class GenerationHttpHandler extends AsyncTask {
    OkHttpClient client = new OkHttpClient();

    private static final int NUMBER_OF_GENERATIONS = 7;

    @Override
    protected String doInBackground(Object[] params) {
        Request.Builder builder = new Request.Builder();
        builder.url(params[0].toString());
        Request request = builder.build();

        try {
            Response response = client.newCall(request).execute();
            return response.body().string();
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(Object o) {
        super.onPostExecute(o);

        parseRegionResponse(o.toString());
    }

    private String[] parseRegionResponse(String response) {
        try{
            JSONObject json = new JSONObject(response);

            String[] regionList = new String[NUMBER_OF_GENERATIONS];

            for (int i = 0; i < regionList.length; i++) {
                regionList[i] = json.getJSONArray("results").getJSONObject(i).getString("name");
            }

            return regionList;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }
}