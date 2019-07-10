package com.time.table;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Scanner;

import static com.time.table.DBHelper.TABLE_NAME;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, CustomModel.lectureChangeListener , CustomModel.detailsChangeListener , CustomModel.lectureUpdateListener ,CustomModel.syncListener{

    public static TextView username;
    public static String dbip = "192.168.1.5";
    static Calendar calendar = Calendar.getInstance();
    static int presentDay = calendar.get(Calendar.DAY_OF_WEEK);
    static int selectedDay = presentDay;
    int lecturePointer = 0;

    private String SERVER_URL = "http://"+dbip+"/time/UploadToServer.php";
    private static final String TAG = MainActivity.class.getSimpleName();

    DBHelper db = new DBHelper(this);

    // Constants
    // The authority for the sync adapter's content provider
    public static final String AUTHORITY = "com.example.android.datasync.provider";
    // An account type, in the form of a domain name
    public static final String ACCOUNT_TYPE = "example.com";
    // The account name
    public static final String ACCOUNT = "dummyaccount";
    // Instance fields
    Account mAccount;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        //mAccount = CreateSyncAccount(this);


        CustomModel.getInstanceLec().setLecListener(this);
        CustomModel.getInstanceDet().setDetListener(this);
        CustomModel.getInstanceUpd().setUpdListener(this);
        CustomModel.getInstanceUpd().setRefListener(this);

        Spinner daysSpinner = findViewById(R.id.daysSpinner);
        daysSpinner.setSelection(presentDay - 1);
        daysSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View v, int position, long arg3) {
                // TODO Auto-generated method stub
                selectedDay=position ;
                loadIntoCardView();
            }
            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                // TODO Auto-generated method stub
            }
        });

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openDialog(Integer.toString(selectedDay),"");
            }
        });

        //updateDisplayName();


        //listenForLectChanges();

        //loadIntoCardView();
        loadFromSQL();
        refresh();

    }


    public static Account CreateSyncAccount(Context context) {
        // Create the account type and default account
        Account newAccount = new Account(
                ACCOUNT, ACCOUNT_TYPE);
        // Get an instance of the Android account manager
        AccountManager accountManager =
                (AccountManager) context.getSystemService(
                        ACCOUNT_SERVICE);
        /*
         * Add the account and account type, no password or user data
         * If successful, return the Account object, otherwise report an error.
         */
        if (accountManager.addAccountExplicitly(newAccount, null, null)) {
            /*
             * If you don't set android:syncable="true" in
             * in your <provider> element in the manifest,
             * then call context.setIsSyncable(account, AUTHORITY, 1)
             * here.
             */
            return newAccount;
        } else {
            /*
             * The account exists or some other error occurred. Log this, report it,
             * or handle it internally.
             */
        }
        return null;
    }



    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.


        getMenuInflater().inflate(R.menu.main, menu);
        menu.getItem(0).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                try {
                    sync();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return false;
            }
        });
        menu.getItem(1).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                refresh();
                return false;
            }
        });


        /*SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        username = (TextView) findViewById(R.id.username);
        username.setText(sharedPref.getString("example_text",""));*/

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_gallery) {

        }  else if (id == R.id.nav_manage) {

            Intent intent = new Intent(this,SettingsActivity.class);
            startActivity(intent);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    @SuppressLint("ResourceAsColor")
    public void loadIntoCardView() {

        final ScrollView root = findViewById(R.id.lectScroll);
        root.removeAllViews();
        float dp = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, getResources().getDisplayMetrics());
        
        int noOfLectures = 0;
        System.out.println(FileActivity.getLectures());
        System.out.println(FileActivity.getLectures().size());
        int i=0;
        for(i = 0 ; i< FileActivity.getLectures().size() ; i++){
            System.out.println(noOfLectures);
            try {
                if(  Integer.parseInt(FileActivity.getLectures().get(i).day) != selectedDay )
                    continue;

            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
            lecturePointer = i;
            noOfLectures++;
            System.out.println(noOfLectures);
        }
        lecturePointer = lecturePointer - noOfLectures + 1;

        if( noOfLectures == 0 ){
            TextView nothing = new TextView(getApplicationContext());
            nothing.setText("No lectures");
            nothing.setTextColor(getResources().getColor(android.R.color.darker_gray));
            nothing.setTextSize(7*dp);
            nothing.setPaddingRelative(0, (int) (120*dp),0,0);
            nothing.setGravity(Gravity.CENTER);
            root.addView(nothing);
            return;
        }


        LinearLayout a = new LinearLayout(this);
        a.setOrientation(LinearLayout.VERTICAL);
        a.setPaddingRelative(20,40,20,20);

        System.out.println("dshadasgbd");
        final CardView[] cards = new CardView[noOfLectures];
        //LinearLayout[] cardsLinear = new LinearLayout[newl.length];
        //RelativeLayout[] cardsRelative = new RelativeLayout[newl.length];

        int il = lecturePointer;
        for (i = 0; i< noOfLectures ; i++){

            cards[i] = new CardView(getApplicationContext());

            //cardsLinear[i] = new LinearLayout(getApplicationContext());
            //cardsRelative[i] = new RelativeLayout(getApplicationContext());
            //cardsLinear[i].setOrientation(LinearLayout.HORIZONTAL);

            System.out.println("load cards\n"+FileActivity.getLectures().get(il));

            TextView time = new TextView(getApplicationContext());
            time.setText(FileActivity.getLectures().get(il).time);
            time.setTextSize(6*dp);
            time.setTextColor(getResources().getColor(android.R.color.darker_gray));
            time.setPadding( (int) (20*dp),(int) (22*dp),(int) (20*dp), (int) (5*dp));

            TextView subject = new TextView(getApplicationContext());
            subject.setText(FileActivity.getLectures().get(il).subject);
            subject.setTextSize(6*dp);
            subject.setTextColor(getResources().getColor(android.R.color.black));
            subject.setPaddingRelative((int) (80*dp),(int) (22*dp),(int) (20*dp), (int) (5*dp));
            //subject.setPaddingRelative(R.dimen.activity_horizontal_margin/10,50,15,50);

            TextView teacher = new TextView(getApplicationContext());
            teacher.setText(FileActivity.getLectures().get(il++).teacher);
            teacher.setTextSize(6*dp);
            teacher.setTextColor(getResources().getColor(android.R.color.holo_blue_dark));
            teacher.setPaddingRelative((int) (180*dp),(int) (22*dp),(int) (20*dp), (int) (5*dp));
            //teacher.setPaddingRelative(R.dimen.activity_horizontal_margin-teacher.length()*20,50,30,50);

            TextView deleted = new TextView(getApplicationContext());
            String strike = "______________________________________________________________________________________________________________________________________________________________________________";
            deleted.setText(strike);
            deleted.setTextSize(6*dp);
            deleted.setTextColor(getResources().getColor(android.R.color.darker_gray));
            deleted.setAlpha(0.6f);
            deleted.setPadding( (int) (16*dp),(int) (16*dp),(int) (16*dp), (int) (20*dp));
            deleted.setVisibility(View.INVISIBLE);

           /* cardsLinear[i].addView(time);
            cardsLinear[i].addView(subject);
            cardsLinear[i].addView(teacher);*/

           /* cardsRelative[i].addView(time);
            cardsRelative[i].addView(subject);
            cardsRelative[i].addView(teacher);*/

            cards[i].addView(time);
            cards[i].addView(subject);
            cards[i].addView(teacher);
            cards[i].addView(deleted);

            //  cards[i].addView(cardsRelative[i]);



            //cards[i].setMinimumWidth(R.dimen.activity_horizontal_margin);
            //cards[i].setMinimumHeight(8);
            cards[i].setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) (76*dp)));
            cards[i].setPaddingRelative(0,0,0,0);
            //cards[i].setForegroundGravity(Gravity.CENTER_HORIZONTAL);
            cards[i].setUseCompatPadding(true);
            cards[i].setCardElevation(3.0f);
            cards[i].setRadius(20.0f);
            cards[i].setClickable(true);
            cards[i].setCardBackgroundColor(getResources().getColor(android.R.color.white));
            //cards[i].set

            //add theme for buttons
            int[] attrs = new int[] {
                    android.R.attr.selectableItemBackground,
                    android.R.attr.dividerHorizontal
            };

            TypedArray ta = obtainStyledAttributes(attrs);
            Drawable drawableTheme = ta.getDrawable(0 /* index */);
            ta.recycle();
            cards[i].setForeground(drawableTheme);
            //cards[i].addView(cardsLinear[i]);

            a.addView(cards[i]);
        }
        System.out.println("121`23`12dsadas");

        root.addView(a);
        root.setForegroundGravity(Gravity.CENTER_HORIZONTAL);

        System.out.println("121`23`12dsadas");

        for (int j = 0 ; j < noOfLectures ; j++) {
            final CardView view = (CardView) a.getChildAt(j);
            System.out.println(root.getChildCount()+" "+a.getChildCount()+" "+view.getChildCount());
            view.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    TextView t = (TextView) view.getChildAt(0);
                    System.out.println(Integer.toString(selectedDay)+"  "+ t.getText().toString());
                    openDialog(Integer.toString(selectedDay) , t.getText().toString());
                    return false;
                }
            });

            /*{
                @Override
                public void onClick(View v) {
                    TextView t = (TextView) view.getChildAt(0);
                    System.out.println(Integer.toString(selectedDay)+"  "+ t.getText().toString());
                    openDialog(Integer.toString(selectedDay) , t.getText().toString());
                }
            });*/
        }

    }

    @Override
    public void deleteLecture(String day,  String time){
        deleteStrike(day,time);
        //db.deleteData(day,time);
        //loadFromSQL();
    }

    public void deleteStrike(String day, String time){
        ScrollView root = findViewById(R.id.lectScroll);
        LinearLayout a = (LinearLayout) root.getChildAt(0);
        System.out.println(FileActivity.positionInDay(day,time) + "<------------1-1--1-1-1-1-1-1-1-1-1-1-1-1-1-1-1");
        CardView card = (CardView) a.getChildAt(FileActivity.positionInDay(day,time));
        TextView deleted = (TextView) card.getChildAt(3);
        deleted.setVisibility(View.VISIBLE);
    }

    public void openDialog(String day, String time) {
        ArrayList id = new ArrayList();
        id.add(day);
        id.add(time);
        Bundle bundl = new Bundle();
        bundl.putStringArrayList("id",id);

        System.out.println("bundl dialog"+bundl);
        lectureChangeDialog exampleDialog = new lectureChangeDialog();
        exampleDialog.setArguments(bundl);
        exampleDialog.show(getSupportFragmentManager(), "example dialog");
    }


    @Override
    public void loadNewLectures(){

        loadIntoSQL();
        loadIntoCardView();
    }

    @Override
    public void backgroundRefresh(){
        refresh();
    }


    public void refresh(){
        //updateFromURL("http://"+dbip+"/time/change.php");
        downloadFromURL("http://"+dbip+"/time/export.sql");
        //parseSQLtoInternalDB();
    }

    @Override
    public void updateDisplayName(){
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        System.out.println("do u understando?????");
        username = (TextView) findViewById(R.id.username);
        System.out.println(sharedPref.getString("username","User"));
        username.setText(""+sharedPref.getString("username","User"));
    }

    @Override
    public void updateLecture(String day,  String time, String subject, String teacher){
        db.updateData(day,time,subject,teacher);
        System.out.println("Changed \n"+day+" "+time+" "+subject+" "+teacher);
        loadFromSQL();
        loadIntoCardView();
    }

    public void addLecture(String day,  String time, String subject, String teacher){
        db.insertData(day,time,subject,teacher);
        System.out.println("Changed \n"+day+" "+time+" "+subject+" "+teacher);
        loadFromSQL();
        loadIntoCardView();
    }


    public void loadFromSQL(){
        Cursor res = db.getAllData();

        FileActivity.lectures.clear();
        System.out.println("yooooo");
        System.out.println(FileActivity.getLectures());
        //System.out.println(FileActivity.getLectures().size() + "loaded from SQL");

        int i = 0;
        System.out.println(res.getCount() + " loaded from SQL  <----------------------");
        while (res.moveToNext()) {
            FileActivity.lectures.add(new Entry(res.getString(0),res.getString(1),res.getString(2),res.getString(3)));
            /*FileActivity.lectures.set(i).time = res.getString(1);
            FileActivity.lectures.set(i).teacher = res.getString(2);
            FileActivity.lectures.set(i).subject = res.getString(3);*/
            System.out.println(FileActivity.getLectures().get(i));
            i++;
        }
    }


    public void loadIntoSQL(){

        db.deleteData(null,null);
        db.execSQL("delete from "+ TABLE_NAME);

        for (int i = 0 ; i < FileActivity.getLectures().size() ; i++) {
            String dayT = FileActivity.getLectures().get(i).day;
            String timeT = FileActivity.getLectures().get(i).time;
            String subjectT = FileActivity.getLectures().get(i).subject;
            String teacherT = FileActivity.getLectures().get(i).teacher;

            db.insertData(dayT,timeT,subjectT,teacherT);
        }

    }

    private void downloadFromURL(final String urlWebService){

        class DownloadSQL extends AsyncTask<Void, Void, String> {

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                try {
                    parseSQLtoInternalDB();
                    loadFromSQL();
                    loadIntoCardView();
                }
                catch (Exception e) {
                    System.out.println("null file/server error");
                    e.printStackTrace();
                }
            }

            @Override
            protected String doInBackground(Void... voids) {
                try {
                    //set the download URL, a url that points to a file on the internet
                    //this is the file to be downloaded
                    //URL url = new URL("http://"+dbip+"/time/export.sql");

                    URL url = new URL(urlWebService);

                    //create the new connection
                    HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

                    //set up some things on the connection
                    urlConnection.setRequestMethod("GET");
                    urlConnection.setDoOutput(true);

                    //and connect!
                    urlConnection.connect();

                    //set the path where we want to save the file
                    //in this case, going to save it on the root directory of the
                    //sd card.
                    File SDCardRoot = Environment.getExternalStorageDirectory();
                    //create a new file, specifying the path, and the filename
                    //which we want to save the file as.
                    File file = new File(SDCardRoot,"export.sql");

                    //this will be used to write the downloaded data into the file we created
                    FileOutputStream fileOutput = new FileOutputStream(file);

                    //this will be used in reading the data from the internet
                    InputStream inputStream = urlConnection.getInputStream();

                    //this is the total size of the file
                    int totalSize = urlConnection.getContentLength();
                    //variable to store total downloaded bytes
                    int downloadedSize = 0;

                    //create a buffer...
                    byte[] buffer = new byte[1024];
                    int bufferLength = 0; //used to store a temporary size of the buffer

                    //now, read through the input buffer and write the contents to the file
                    while ( (bufferLength = inputStream.read(buffer)) > 0 ) {
                        //add the data in the buffer to the file in the file output stream (the file on the sd card
                        fileOutput.write(buffer, 0, bufferLength);
                        //add up the size so we know how much is downloaded
                        downloadedSize += bufferLength;
                        //this is where you would do something to report the prgress, like this maybe
                        System.out.println("Downloaded export file");
                        //updateProgress(downloadedSize, totalSize);

                    }
                    //close the output stream when done
                    fileOutput.close();

//catch some possible errors...
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }
        }
        DownloadSQL getSQL = new DownloadSQL();
        getSQL.execute();

    }

    private void parseSQLtoInternalDB(){
        File file = new File(Environment.getExternalStorageDirectory().getPath()+"/export.sql");
        Scanner input = null;
        try {
            input = new Scanner(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        input.nextLine();
        db.execSQL("delete from "+ TABLE_NAME);
        while(input.hasNext()) {
            //String nextToken = input.next();
            //or to process line by line
            String nextLine = input.nextLine();
            nextLine = nextLine.substring(19);
            System.out.println(nextLine+"<..................WHAT YOU WANNA SEE");
            nextLine = nextLine.replace("("," ");
            nextLine = nextLine.replace(")"," ");
            nextLine = nextLine.trim();
            String[] entry = nextLine.split(", ");
            db.insertData(entry[0],entry[1],entry[2],entry[3]);

        }

        input.close();
        loadFromSQL();
        loadIntoCardView();
    }

    private void updateFromURL(final String urlWebService) {

        class DownloadJSON extends AsyncTask<Void, Void, String> {

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                try {
                    loadJSON(s);
                }
                catch (JSONException e) {
                    System.out.println("null JSON/server error");
                    e.printStackTrace();
                }
            }

            @Override
            protected String doInBackground(Void... voids) {
                try {
                    URL url = new URL(urlWebService);
                    HttpURLConnection con = (HttpURLConnection) url.openConnection();
                    StringBuilder sb = new StringBuilder();
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(con.getInputStream()));
                    String json;
                    while ((json = bufferedReader.readLine()) != null) {
                        sb.append(json + "\n");
                    }
                    return sb.toString().trim();
                } catch (Exception e) {
                    return null;
                }
            }
        }
        DownloadJSON getJSON = new DownloadJSON();
        getJSON.execute();
    }


    private void loadJSON(String json ) throws JSONException {

        try{
            JSONArray jsonArray = new JSONArray(json);
            //newl = new lectureChange[jsonArray.length()];
            int notif = 0, u = 0;
            for (int i = 0; i < jsonArray.length(); i++) {

                JSONObject obj = jsonArray.getJSONObject(i);

                String day = obj.getString("day");
                String time = obj.getString("time");
                String subject = obj.getString("subject");
                String teacher = obj.getString("teacher");

                //newl[i] = new lectureChange(dayArray,time,subject,teacher);

                //System.out.println(newl[i]);

                int q = FileActivity.findByid(day,time);
                System.out.println("lec "+q + "<<<<----------------------------findByid reeturns");

                if(q!=-1){
                    FileActivity.lectures.set(q, new Entry(day,time, subject , teacher));
                    db.updateData(day,time,subject,teacher);

                    //MainActivity.dayArray[dayArray].setLecture( q, time, subject, teacher);
                    System.out.println("Updating..."+ u++);
                    System.out.println(FileActivity.lectures.get(q));
                    notif = 1; // show notif listener

                }
                else {
                    System.out.println("loop "+i);
                    FileActivity.lectures.add(new Entry(day,time,subject,teacher));
                    db.insertData(day,time,subject,teacher);

                    //code to add lect if it doesn't already exist
                }
            }

            /*for(int d=0; d<7; d++){

                for(int i=0 ; i<newl.length ; i++ ){

                    if(exists(newl[i])!=0){
                        dayArray[newl[i].dayArray].setLecture( exists(newl[i]), newl[i].time, newl[i].subject, newl[i].teacher);
                        System.out.println(dayArray[newl[i].dayArray].lecture[i]);
                    }
                    else {
                        //code to add lect if it doesn't already exist
                    }
                }
            }*/
            //if(notif==1)
                //showNotif();;

            Toast.makeText(getApplicationContext(), "Refreshed", Toast.LENGTH_SHORT).show();
            loadFromSQL();
            loadIntoCardView();
        }
        catch (NullPointerException e)
        {
            if(json == null)
                Toast.makeText(getApplicationContext(), "Couldn't connect", Toast.LENGTH_SHORT).show();
        }
    }

    private void sync() throws FileNotFoundException, UnsupportedEncodingException {
        exportToSQL();
        uploadClick(Environment.getExternalStorageDirectory().getPath()+"/export.sql");
    }

    public void exportToSQL() throws FileNotFoundException, UnsupportedEncodingException {
        Cursor res = db.getAllData();

        PrintWriter writer = new PrintWriter(Environment.getExternalStorageDirectory().getPath()+"/export.sql", "UTF-8");

        writer.println("CREATE TABLE time (DAY TEXT, TIME TEXT , SUBJECT TEXT , TEACHER TEXT)");
        while (res.moveToNext()) {
            writer.println("INSERT INTO change ("+res.getString(0)+", "+res.getString(1)+", "+res.getString(2)+", "+res.getString(3)+")");
        }
        System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!written");
        writer.close();
    }

    public void uploadClick(String Path){
        final String selectedFilePath = Path;
        if(selectedFilePath != null){
            System.out.println("Uploading File...");

            new Thread(new Runnable() {
                @Override
                public void run() {
                    //creating new thread to handle Http Operations
                    uploadFile(selectedFilePath);
                }
            }).start();
        }else{
            Toast.makeText(MainActivity.this,"Please choose a File First",Toast.LENGTH_SHORT).show();
        }
    }

    public int uploadFile(final String selectedFilePath){

        int serverResponseCode = 0;

        HttpURLConnection connection;
        DataOutputStream dataOutputStream;
        String lineEnd = "\r\n";
        String twoHyphens = "--";
        String boundary = "*****";


        int bytesRead,bytesAvailable,bufferSize;
        byte[] buffer;
        int maxBufferSize = 1 * 1024 * 1024;
        File selectedFile = new File(selectedFilePath);


        String[] parts = selectedFilePath.split("/");
        final String fileName = parts[parts.length-1];

        if (!selectedFile.isFile()){

            runOnUiThread(new Runnable() {
                @Override
                public void run() {

                }
            });
            return 0;
        }else{
            try{
                FileInputStream fileInputStream = new FileInputStream(selectedFile);
                URL url = new URL(SERVER_URL);
                connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);//Allow Inputs
                connection.setDoOutput(true);//Allow Outputs
                connection.setUseCaches(false);//Don't use a cached Copy
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Connection", "Keep-Alive");
                connection.setRequestProperty("ENCTYPE", "multipart/form-data");
                connection.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
                connection.setRequestProperty("uploaded_file",selectedFilePath);

                //creating new dataoutputstream
                dataOutputStream = new DataOutputStream(connection.getOutputStream());

                //writing bytes to data outputstream
                dataOutputStream.writeBytes(twoHyphens + boundary + lineEnd);
                dataOutputStream.writeBytes("Content-Disposition: form-data; name=\"uploaded_file\";filename=\""
                        + selectedFilePath + "\"" + lineEnd);

                dataOutputStream.writeBytes(lineEnd);

                //returns no. of bytes present in fileInputStream
                bytesAvailable = fileInputStream.available();
                //selecting the buffer size as minimum of available bytes or 1 MB
                bufferSize = Math.min(bytesAvailable,maxBufferSize);
                //setting the buffer as byte array of size of bufferSize
                buffer = new byte[bufferSize];

                //reads bytes from FileInputStream(from 0th index of buffer to buffersize)
                bytesRead = fileInputStream.read(buffer,0,bufferSize);

                //loop repeats till bytesRead = -1, i.e., no bytes are left to read
                while (bytesRead > 0){
                    //write the bytes read from inputstream
                    dataOutputStream.write(buffer,0,bufferSize);
                    bytesAvailable = fileInputStream.available();
                    bufferSize = Math.min(bytesAvailable,maxBufferSize);
                    bytesRead = fileInputStream.read(buffer,0,bufferSize);
                }

                dataOutputStream.writeBytes(lineEnd);
                dataOutputStream.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

                serverResponseCode = connection.getResponseCode();
                String serverResponseMessage = connection.getResponseMessage();

                Log.i(TAG, "Server Response is: " + serverResponseMessage + ": " + serverResponseCode);

                //response code of 200 indicates the server status OK
                if(serverResponseCode == 200){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            System.out.println("File Upload completed.\n\n You can see the uploaded file here: \n\n");
                            //+ "http://coderefer.com/extras/uploads/"+ fileName);
                        }
                    });
                }

                //closing the input and output streams
                fileInputStream.close();
                dataOutputStream.flush();
                dataOutputStream.close();



            } catch (FileNotFoundException e) {
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this,"File Not Found",Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (MalformedURLException e) {
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, "URL error!", Toast.LENGTH_SHORT).show();
                    }
                });
                //Toast.makeText(MainActivity.this, "URL error!", Toast.LENGTH_SHORT).show();

            } catch (IOException e) {
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, "Cannot Read/Write File!", Toast.LENGTH_SHORT).show();
                    }
                });

                //Toast.makeText(MainActivity.this, "Cannot Read/Write File!", Toast.LENGTH_SHORT).show();

            }

            return serverResponseCode;
        }

    }

    public static class SyncService extends Service {
        // Storage for an instance of the sync adapter
        private static SyncAdapter sSyncAdapter = null;
        // Object to use as a thread-safe lock
        private static final Object sSyncAdapterLock = new Object();
        /*
         * Instantiate the sync adapter object.
         */
        @Override
        public void onCreate() {
            /*
             * Create the sync adapter as a singleton.
             * Set the sync adapter as syncable
             * Disallow parallel syncs
             */
            synchronized (sSyncAdapterLock) {
                if (sSyncAdapter == null) {
                    sSyncAdapter = new SyncAdapter(getApplicationContext(), true);
                }
            }
        }
        /**
         * Return an object that allows the system to invoke
         * the sync adapter.
         *
         */
        @Override
        public IBinder onBind(Intent intent) {
            /*
             * Get the object that allows external processes
             * to call onPerformSync(). The object is created
             * in the base class code when the SyncAdapter
             * constructors call super()
             */
            return sSyncAdapter.getSyncAdapterBinder();
        }
    }


}
