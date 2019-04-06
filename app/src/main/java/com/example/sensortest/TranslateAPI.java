package com.example.sensortest;


import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;

/*Source: https://www.wikihow.com/Execute-HTTP-POST-Requests-in-Android
Source: https://medium.com/@JasonCromer/android-asynctask-http-request-tutorial-6b429d833e28
Source: https://tech.yandex.com/translate/doc/dg/reference/translate-docpage/
*/

public class TranslateAPI extends AsyncTask<String, Void, String> {
    public static final String API_KEY = "trnsl.1.1.20190406T161250Z.d535ecb82d0bb929.0fb2ded77d67b60b2911527e20f72e3fe3b1e994";

    public static final String REQUEST_METHOD = "GET";
    public static final int READ_TIMEOUT = 15000;
    public static final int CONNECTION_TIMEOUT = 15000;
    @Override
    protected String doInBackground(String... params){
        String url = "https://translate.yandex.net/api/v1.5/tr.json/translate?key=" + API_KEY + "&text=" + params[0] + "&lang=" + params[1] + "-" + params[2] + "&format=plain";

        String result;
        String inputLine;
        try {
            //Create a URL object holding our url
            URL myUrl = new URL(url);
            //Create a connection
            HttpURLConnection connection =(HttpURLConnection)
                    myUrl.openConnection();
            //Set methods and timeouts
            connection.setRequestMethod(REQUEST_METHOD);
            connection.setReadTimeout(READ_TIMEOUT);
            connection.setConnectTimeout(CONNECTION_TIMEOUT);

            //Connect to our url
            connection.connect();
            //Create a new InputStreamReader
            InputStreamReader streamReader = new
                    InputStreamReader(connection.getInputStream());
            //Create a new buffered reader and String Builder
            BufferedReader reader = new BufferedReader(streamReader);
            StringBuilder stringBuilder = new StringBuilder();
            //Check if the line we are reading is not null
            while((inputLine = reader.readLine()) != null){
                stringBuilder.append(inputLine);
            }
            //Close our InputStream and Buffered reader
            reader.close();
            streamReader.close();
            //Set our result equal to our stringBuilder
            result = stringBuilder.toString();
        }
        catch(IOException e){
            e.printStackTrace();
            result = "test";
        }
        Log.d("Response: ", result);
        String[] resposeParts = result.split("\"");
        return resposeParts[resposeParts.length - 2];
    }
    protected void onPostExecute(String result){
        super.onPostExecute(result);
    }
}