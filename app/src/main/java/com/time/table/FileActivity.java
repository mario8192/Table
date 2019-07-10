package com.time.table;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.opencsv.CSVReader;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FileActivity extends AppCompatActivity {

    static String PathHolder;
    static ArrayList<Entry> lectures = new ArrayList<Entry>();
    //private lectureChangeListener listener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file);

        final Button button = (Button) findViewById(R.id.csvSelect);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Perform action on click
                //code for what you want it to do

                Toast.makeText(getApplicationContext(), "ok", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("*/*");
                String[] mimetypes = {"text/csv", "text/comma-separated-values", "application/csv"};
                intent.putExtra(Intent.EXTRA_MIME_TYPES, mimetypes);
                System.out.println("ooooookkkkkkk");
                startActivityForResult(intent, 7);
                System.out.println("ooooookkkkkkk11111111");


            }
        });

        button.callOnClick();

    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        if(data == null)
        {
            finish();
            return;
        }
        PathHolder = data.getData().getPath();

        System.out.println("ooooookkkkkkk2222222");
        Toast.makeText(getApplicationContext(), PathHolder, Toast.LENGTH_LONG).show();
        try {
            populateFromCSV();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //listener.loadNewLectures();
        System.out.println("do something naaa");
        CustomModel.getInstanceLec().changeLecture(true);

        finish();
    }

    public static String getCSVPath(){
        return PathHolder;
    }

    public void populateFromCSV() throws IOException {

        String csvpath = FileActivity.getCSVPath();
        String a = "\\\\";
        csvpath.replaceAll(a,"/");
        csvpath = csvpath.substring(18);                //lololololololol
        csvpath = "/sdcard/" + csvpath;
        System.out.println(csvpath);
        File f = new File(csvpath);

        CSVReader reader = new CSVReader(new FileReader(f));
        List<String[]> lectureList = reader.readAll();

        int c = lectureList.size();
        //System.out.println(lectureList.toArray());

        /*for (int td = 0; td<7 ; td++){

            dayArray[td] = new Day(td);
            dayArray[td].lecture = new Lecture[lectureList.size()];

            for(int i=0; i<lectureList.size() ; i++)
            {
                String[] t;
                t = lectureList.get(i);

                //System.out.println(t);
                if(t[0].length()>=5)

                    try{
                        dayArray[td].lecture[i] = new Lecture(t[0+3*td],t[1+3*td],t[2+3*td]);
                    }

                    catch (NullPointerException e){
                        Toast.makeText(getApplicationContext(), "Nothing to show", Toast.LENGTH_SHORT).show();
                        return;
                    }
                //dayArray[1].setLecture(i,t[0],t[1],t[2]);

                //System.out.println(dayArray[d].lecture[i]);
            }

        }*/

        lectures.clear();
        int i = 0;
        while (i<c){

            String[] t;
            t = lectureList.get(i);
            try{
                int currentDay=i/3;
                String dayT = Integer.toString(currentDay + 1);
                String timeT = t[0+3*currentDay];
                String subjectT = t[1+3*currentDay];
                String teacherT = t[2+3*currentDay];

                Entry tempEntry = new Entry(dayT,timeT,subjectT,teacherT);

                System.out.println(tempEntry);
                lectures.add(tempEntry);

            }

            catch (NullPointerException e){
                Toast.makeText(getApplicationContext(), "Nothing to show", Toast.LENGTH_SHORT).show();
                return;
            }

            i++;
        }


    }


    public static ArrayList<Entry> getLectures(){
        return lectures;
    }


    public static int findByid(String day, String time){
        int i;
        for (i=0 ; i<getLectures().size() ; i++){
            if(lectures.get(i).day.equals(day))
                if(lectures.get(i).time.equals(time))
                    return i;
        }
        return -1;
    }

    public static int positionInDay(String day, String time){
        int q = findByid(day,time);
        System.out.println(day+" --] "+time);
        int p=0;
        while (Integer.parseInt(day) != Integer.parseInt(lectures.get(p).day)){
            p++;
        }

        return q - p ;
    }

    /*public interface lectureChangeListener{
        void loadNewLectures();
    }*/



}
