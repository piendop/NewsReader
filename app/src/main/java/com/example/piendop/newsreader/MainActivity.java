package com.example.piendop.newsreader;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import javax.net.ssl.HttpsURLConnection;

public class MainActivity extends AppCompatActivity {

    //global object database
    SQLiteDatabase myDatabase;
    ArrayAdapter<String> arrayAdapter;
    ArrayList<String> titles= new ArrayList<>();
    ArrayList<String> contents = new ArrayList<>();

    /**CLASS GET WEB CONTENT CONVERT IT INTO WEB API**/
    public class WebContent extends AsyncTask<String,Void,Void>{

        @Override
        protected Void doInBackground(String... urls) {
            try {
                //get content of all ids in top stories
                String result = getUrlContent(urls[0]);
                //now we get api web id of each new from result
                JSONArray jsonArray = new JSONArray(result);
                //web new api, for speed purpose we just show 20 news each
                // time so we just store to database 20 news
                int numberOfItems=20;
                //rarely, we have number of news less than 20, but we still have to check
                if(numberOfItems>jsonArray.length()){
                    numberOfItems=jsonArray.length();
                }
                /**SINCE EVERY TIME WE OPEN THE APP TOP NEWS CHANGE SO WE NEED TO UPDATE DATABASE **/
                myDatabase.execSQL("DELETE FROM articles");
                //get news' info from 20 top news from its web api
                for(int i=0;i<numberOfItems;++i){
                    //get id of news from json array
                    String id = jsonArray.getString(i);
                    //get url's content of each id
                    result=getUrlContent("https://hacker-news.firebaseio.com/v0/item/"
                            +id+".json?print=pretty");
                    //insert specific info to database
                    JSONObject jsonObject = new JSONObject(result);
                    //check if info we need is null or not
                    if (!jsonObject.isNull("title") && !jsonObject.isNull("url")){
                        String title = jsonObject.getString("title");
                        String urlNews = jsonObject.getString("url");
                        insertToDatabaseByString(id,title,urlNews);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            //after store all info to database we update list view
            updateListView();
        }

        /**HOW TO INSERT STRING TO DATABASE 101*/
        private void insertToDatabaseByString(String id, String title, String content) {
            String sql = "INSERT INTO articles (id, title, content) VALUES (?, ?, ?)";

            SQLiteStatement statement = myDatabase.compileStatement(sql);

            statement.bindString(1, id);
            statement.bindString(2, title);
            statement.bindString(3, content);

            statement.execute();
        }

        /**METHOD TO GET URL'S CONTENT OF SPECIFIC URL*/
        private String getUrlContent(String urls) {

            //urls[0] can be empty ==> need catch and try
            try {
                URL url = new URL(urls);
                HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();//we might
                // not open the connection ==> need catch and try
                InputStream inputStream = urlConnection.getInputStream();
                InputStreamReader reader = new InputStreamReader(inputStream);
                int data = reader.read();
                String result="";
                while(data!=-1){
                    result+=(char) data;
                    data=reader.read();
                }
                return result;
            } catch (Exception e) {
                e.printStackTrace();
                return "Failed successfully";
            }
        }
    }

    /**UPDATE LIST VIEW EVERY TIME OPEN THE APP*/
    public void updateListView() {
        Cursor c = myDatabase.rawQuery("SELECT * FROM articles", null);

        int contentIndex = c.getColumnIndex("content");
        int titleIndex = c.getColumnIndex("title");

        //clear the previous title and content 's list
        if (c.moveToFirst()) {
            titles.clear();
            contents.clear();
            do {
                titles.add(c.getString(titleIndex));
                contents.add(c.getString(contentIndex));
            } while (c.moveToNext());
            //update changes in data
            arrayAdapter.notifyDataSetChanged();
        }
    }

    /**CLASS CREATE DATABASE NEWS BASED ON WEB API HACKER NEWS**/
    /*public class Database extends AsyncTask<String,Void,String>{
        @Override
        protected String doInBackground(String... urls) {
            try {
                URL url = new URL(urls[0]);//urls[0] can be empty ==> need catch and try
                HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();//we might
                // not open the connection ==> need catch and try
                InputStream inputStream = urlConnection.getInputStream();
                InputStreamReader reader = new InputStreamReader(inputStream);
                int data = reader.read();
                String result="";
                while(data!=-1){
                    result+=(char) data;
                    data=reader.read();
                }
                return result;
            } catch (Exception e) {

                e.printStackTrace();
                return "Failed successfully";
            }
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            //using JSON object to parse the info
            try {
                JSONObject jsonObject = new JSONObject(result);//get the content as json file
                String title = jsonObject.getString("title");
                String url = jsonObject.getString("url");
                insertToDatabase(title,url);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        private void insertToDatabase(String title, String url) {
            try{
                myDatabase = MainActivity.this.
                        openOrCreateDatabase("News",MODE_PRIVATE,null);

                myDatabase.execSQL("CREATE TABLE IF NOT EXISTS " +
                        "news (id INTEGER PRIMARY KEY,title VARCHAR, url VARCHAR)");

                myDatabase.execSQL("INSERT INTO news (id,title,age) VALUES(title,url)");

                Cursor c = myDatabase.rawQuery("SELECT * FROM news", null);

                int nameIndex = c.getColumnIndex("title");
                int ageIndex = c.getColumnIndex("url");
                int idIndex = c.getColumnIndex("id");

                c.moveToFirst();

                while (c != null) {

                    Log.i("News-Title", c.getString(nameIndex));
                    Log.i("News-Url", Integer.toString(c.getInt(ageIndex)));
                    Log.i("News-Id", Integer.toString(c.getInt(idIndex)));

                    c.moveToNext();
                }
            }catch(Exception e){
                e.printStackTrace();
            }
        }
    }*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        /***NEEDING OBJECTS ACROSS FUNCTION****/
        ListView listView = findViewById(R.id.listView);
        /****CREATE NEWS READER LIST VIEW WITH ARRAY ADAPTER****/
        createListView(listView);
        /******CREATE/UPDATE DATABASE NEWS INFO*******/
        initDatabaseNews();
        /**UPDATE LIST VIEW*/
        updateListView();
    }

    /******CREATE DATABASE NEWS INFO*******/
    private void initDatabaseNews() {
        /**create database news*/
        myDatabase = MainActivity.this.
                openOrCreateDatabase("News",MODE_PRIVATE,null);

        myDatabase.execSQL("CREATE TABLE IF NOT EXISTS " +
                "articles (id VARCHAR PRIMARY KEY,title VARCHAR, content VARCHAR)");
        /***get web ids, now it store in webIds array list**/
        WebContent webContent = new WebContent();
        webContent.execute("https://hacker-news.firebaseio.com/v0/topstories.json?print=pretty");
    }

    /****CREATE NEWS READER TITLE LIST VIEW WITH ARRAY ADAPTER****/
    private void createListView(ListView listView) {
        //array adapter display news data in list view with custom layout
        arrayAdapter = new ArrayAdapter<>(getApplicationContext(),
                R.layout.black_text, R.id.textView, titles);
        //set arrayAdapter as adapter of listView
        listView.setAdapter(arrayAdapter);
        //open a news url when click on titles
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                //i is a row index, we use as to get content
                Intent intent = new Intent(getApplicationContext(),ContentActivity.class);
                //put string html content to content activity
                intent.putExtra("content",contents.get(i));
                startActivity(intent);
            }
        });
    }
}
