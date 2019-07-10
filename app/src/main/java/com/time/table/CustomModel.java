package com.time.table;

public class CustomModel {

    public interface lectureChangeListener {
        //void stateChanged();
        void loadNewLectures();
        //void updateDisplayName();
    }

    public interface detailsChangeListener {
        //void stateChanged();
        //void loadNewLectures();
        void updateDisplayName();
    }

    public interface lectureUpdateListener {
        //void stateChanged();
        //void loadNewLectures();
        void updateLecture(String day,  String time, String subject, String teacher);
        void addLecture(String day,  String time, String subject, String teacher);
        void deleteLecture(String day,  String time);
    }

    public interface syncListener {
        //void stateChanged();
        //void loadNewLectures();
        void backgroundRefresh();
    }


    private static CustomModel lecInstance;
    private static CustomModel detInstance;
    private static CustomModel updInstance;
    private static CustomModel refInstance;
    private lectureChangeListener lecListener;
    private detailsChangeListener detListener;
    private lectureUpdateListener updListener;
    private syncListener refListener;
    private boolean lecState;
    private boolean detState;
    private boolean updState;
    private boolean refState;

    private CustomModel() {}

    public static CustomModel getInstanceLec() {
        if(lecInstance == null) {
            lecInstance = new CustomModel();
        }
        return lecInstance;
    }

    public static CustomModel getInstanceDet() {
        if(detInstance == null) {
            detInstance = new CustomModel();
        }
        return detInstance;
    }

    public static CustomModel getInstanceUpd() {
        if(updInstance == null) {
            updInstance = new CustomModel();
        }
        return updInstance;
    }

    public static CustomModel getInstanceRef() {
        if(refInstance == null) {
            refInstance = new CustomModel();
        }
        return refInstance;
    }


    public void setLecListener(lectureChangeListener listener) {
        lecListener = listener;
    }

    public void setDetListener(detailsChangeListener listener) {
        detListener = listener;
    }

    public void setUpdListener(lectureUpdateListener listener) {
        updListener = listener;
    }

    public void setRefListener(syncListener listener) {
        refListener = listener;
    }

    public void changeLecture(boolean state) {
        if(lecListener != null) {
            lecState = state;
            System.out.println("mlistener chnage");
            notifyNewChange();
        }
    }

    public void changeName(boolean state) {
        if(detListener != null) {
            detState = state;
            System.out.println("mlistener name");
            notifyDetailsChange();
        }
    }

    public void changeUpdate(boolean state, int addNew ,String day,  String time, String subject, String teacher) {
        if(updListener != null) {
            updState = state;
            System.out.println("mlistener lect");
            notifyLectureChange(addNew,day,time,subject,teacher);
        }
    }

    public void changeRefresh(boolean state) {
        if(refListener != null) {
            refState = state;
            System.out.println("mlistener ref");
            notifySyncChange();
        }
    }

    public boolean getState() {
        return lecState;
    }

    private void notifyNewChange() {
        //lecListener.stateChanged();
        lecListener.loadNewLectures();
    }

    private void notifyDetailsChange() {
        //lecListener.stateChanged();
        detListener.updateDisplayName();
    }

    private void notifyLectureChange(int j,String day,  String time, String subject, String teacher) {
        //lecListener.stateChanged();
        if(j==0)
            updListener.updateLecture(day,time,subject,teacher);
        else if(j==1)
            updListener.addLecture(day,time,subject,teacher);
        else if(j==-1)
            updListener.deleteLecture(day,time);
        else {}
    }

    private void notifySyncChange() {
        //lecListener.stateChanged();
        refListener.backgroundRefresh();
    }


}
