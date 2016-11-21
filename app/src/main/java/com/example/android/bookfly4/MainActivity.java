package com.example.android.bookfly4;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    //used to identify errors within the parsing process
    public static final String LOG_TAG = MainActivity.class.getName();
    //Google Books API URL
    public static final String GOOGLE_BOOKS_URL = "https://www.googleapis.com/books/v1/volumes?q=";

    //used for user searches
    private EditText userInputSearch;
    private Button searchBtn;

    //ListView variables created to populate the data
    private ListView listView;

    //variables for arrayList,array list adapter, and errorText
    private ArrayList<Book> bookArrayList;
    private BookListAdapter adapter;
    private TextView emptyJson;

    //Checks for internet connection availability
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        searchBtn = (Button) findViewById(R.id.btnView);
        userInputSearch = (EditText) findViewById(R.id.editView);
        emptyJson = (TextView) findViewById(R.id.error_text);

        listView = (ListView) findViewById(R.id.listView);
        bookArrayList = new ArrayList<Book>();
        adapter = new BookListAdapter(this, bookArrayList);
        listView.setAdapter(adapter);

        searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (!isNetworkAvailable()) {
                    Context context = getApplicationContext();
                    String text = "Internet Connection not found.";
                    int duration = Toast.LENGTH_SHORT;
                    Toast toast = Toast.makeText(context,text,duration);
                    toast.show();
                } else {
                    adapter.clear();
                    userInputSearch = (EditText) findViewById(R.id.editView);
                    String userInput = userInputSearch.getText().toString().replace(" ", "+");
                    if (userInput.isEmpty()) {
                        Context context = getApplicationContext();
                        String text = "Nothing entered. Please Enter.";
                        int duration = Toast.LENGTH_SHORT;
                        Toast toast = Toast.makeText(context, text, duration);
                        toast.show();

                    }
                    BookAsyncTask task = new BookAsyncTask();
                    task.execute(userInput);
                }
            }
        });

    }

    private void updateUI(List<Book> book) {
        //if JSON list is empty from search, set "No results found...
        if (book.isEmpty()) {
            emptyJson.setText("No results found. Try a different search.");
            //else, set text to empty string, add JSON data retrieved to adapter,
        } else {
            emptyJson.setText(" ");
            adapter.addAll(book);
            //Notifies the attached observers that the underlying
            // data has been changed and any View
            // reflecting the data set should refresh itself.
            adapter.notifyDataSetChanged();
        }

    }


    private String makeHttpRequest(URL url) throws IOException {
        String jsonResponse = null;
        if (url == null) {
            return jsonResponse;
        }
        //Create empty variables to establish connection and inputStream
        HttpURLConnection urlConnection = null;
        InputStream inputStream = null;
        try {
            //Open connection with URL
            urlConnection = (HttpURLConnection) url.openConnection();
            //Request connection with URL
            urlConnection.setRequestMethod("GET");
            //Connect to the URL
            urlConnection.connect();

            //Receive the response and make sense of it.
            //If connection is successfull read the Stream and stream it in inputStream variable

            if(urlConnection.getResponseCode() == 200) {
                inputStream = urlConnection.getInputStream();
                jsonResponse = readFromStream(inputStream);
            } else {
                Log.e(LOG_TAG,"Error Repsonse code");
            }
            // if no response is received then retrieve response code error

        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            } else {
                Log.e(LOG_TAG, "Error" + urlConnection.getResponseCode());
            }
            if (inputStream != null) {
                inputStream.close();
            }

        }
        return jsonResponse;
    }

    //converts input into stream
    private String readFromStream(InputStream inputStream) throws IOException {
        StringBuilder output = new StringBuilder();
        if (inputStream != null) {
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charset.forName("UTF-8"));
            BufferedReader reader = new BufferedReader(inputStreamReader);
            String line = reader.readLine();
            while (line != null) {
                output.append(line);
                line = reader.readLine();
            }
        }
        return output.toString();
    }

    private List<Book> extractFromJson(String bookJson) {
        List<Book> bookList = new ArrayList<>();
        if (TextUtils.isEmpty(bookJson)) {
            return null;
        }
        try {
            JSONObject baseJson = new JSONObject(bookJson);
            JSONArray itemsArray = baseJson.getJSONArray("items");

            for (int i = 0; i < itemsArray.length(); i++) {
                JSONObject responseObject = itemsArray.getJSONObject(i);
                JSONObject volumesInfo = responseObject.getJSONObject("volumeInfo");

                String actualAuthor = "N/A";
                if (volumesInfo.has("authors")) {
                    JSONArray authorsArray = volumesInfo.getJSONArray("authors");
                    actualAuthor = authorsArray.getString(0);

                }
                String bookTitle = volumesInfo.getString("title");
                Book book = new Book(bookTitle, actualAuthor);

                Log.d(LOG_TAG, "extractFromJson" + book.toString());

                bookList.add(book);

            }

        } catch (JSONException e) {
            Log.e(LOG_TAG, "Problem parsing JSON book", e);

        }
        return bookList;
    }

    //Create a URL from a string
    private URL createUrl(String stringUrl) {
        URL url = null;
        try {
            url = new URL(stringUrl);
        } catch (MalformedURLException exception) {
            Log.e(LOG_TAG, "Error with creating URL", exception);
            return null;
        }
        return url;
    }

    private class BookAsyncTask extends AsyncTask<String, Void, List<Book>> {

        @Override
        protected List<Book> doInBackground(String... strings) {
            URL url = createUrl(GOOGLE_BOOKS_URL + strings[0] + "&maxResults=15");
            String jsonResponse = " ";
            try {
                jsonResponse = makeHttpRequest(url);

            } catch (IOException e) {
                e.printStackTrace();
            }
            List<Book> book = extractFromJson(jsonResponse);
            return book;
        }

        @Override
        protected void onPostExecute(List<Book> books) {
            if (books == null) {
                return;
            }
            updateUI(books);
        }
    }


}
