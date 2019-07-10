package com.time.table;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

public class lectureChangeDialog extends AppCompatDialogFragment {

    private EditText editTextSubject;
    private EditText editTextTeacher;
    private EditText time1;
    private EditText time2;
    String day;
    String time;

    Boolean add=false;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.change_dialog, null);

        day = getArguments().getStringArrayList("id").get(0);
        time = getArguments().getStringArrayList("id").get(1);

        if(time.equals(""))
            add = true;

        builder.setView(view)
                .setTitle("Update Lecture Details")
                .setNegativeButton("delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //lecture deleted
                        if(add == true)
                            return;

                        String newSubject = null;
                        String newTeacher = null;
                        CustomModel.getInstanceUpd().changeUpdate(true,-1,day,time,newSubject,newTeacher);

                    }
                })
                .setPositiveButton("save", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        String newtime1 = time1.getText().toString();
                        String newtime2 = time2.getText().toString();
                        String newSubject = editTextSubject.getText().toString();
                        String newTeacher = editTextTeacher.getText().toString();

                        if(newtime1.equals("") || newtime2.equals("")){
                            //show some error
                            return;
                        }

                        if(add==true){
                            if(newSubject.equals("") || newTeacher.equals(""))
                                return;;
                        }

                        System.out.println(newSubject);
                        System.out.println(newTeacher+" "+FileActivity.findByid(day,time));

                        if(newSubject.equals(""))
                            newSubject=FileActivity.getLectures().get(FileActivity.findByid(day,time)).subject;
                        if(newTeacher.equals(""))
                            newTeacher=FileActivity.getLectures().get(FileActivity.findByid(day,time)).teacher;

                        System.out.println("this isss magic\n"+ day + time + newSubject + newTeacher);
                        if(add==false)
                            CustomModel.getInstanceUpd().changeUpdate(true,0, day, newtime1+":"+newtime2, newSubject, newTeacher);
                        else
                            CustomModel.getInstanceUpd().changeUpdate(true,1, day, newtime1+":"+newtime2, newSubject, newTeacher);
                    }
                });

        time1 = view.findViewById(R.id.time1);
        time2 = view.findViewById(R.id.time2);
        editTextSubject = view.findViewById(R.id.editTextSubject);
        editTextTeacher = view.findViewById(R.id.editTextTeacher);

        if(add==false) {
            int q = FileActivity.findByid(day, time);
            String[] editTime = time.split(":");
            time1.setText(editTime[0]);
            time2.setText(editTime[1]);
            editTextSubject.setText(FileActivity.getLectures().get(q).subject);
            editTextTeacher.setText(FileActivity.getLectures().get(q).teacher);
        }
        return builder.create();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }
}
