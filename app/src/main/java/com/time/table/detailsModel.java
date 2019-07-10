package com.time.table;

public class detailsModel {
    public interface detailChangeListener {
        //void stateChanged();
        //void loadNewLectures();
        void updateDisplayName();
    }

    private static detailsModel mInstance;
    private detailChangeListener mListener;
    private boolean mState;

    private detailsModel() {}

    public static detailsModel getInstance() {
        if(mInstance == null) {
            mInstance = new detailsModel();
        }
        return mInstance;
    }

    public void setListener(detailChangeListener listener) {
        mListener = listener;
    }


    public void changeName(boolean state) {
        if(mListener != null) {
            mState = state;
            System.out.println("mlistener name");
            notifyStateChange1();
        }
    }

    public boolean getState() {
        return mState;
    }

    private void notifyStateChange1() {
        //mListener.stateChanged();
        mListener.updateDisplayName();
    }

}
