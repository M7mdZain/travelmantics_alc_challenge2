package com.example.android.travelmantics_firebase;

import java.io.Serializable;

public interface FirebaseImageUploadListener extends Serializable {
    void onImageChanged(int position);
}
