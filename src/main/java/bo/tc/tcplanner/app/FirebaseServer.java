package bo.tc.tcplanner.app;

import bo.tc.tcplanner.datastructure.Timeline;
import bo.tc.tcplanner.datastructure.ValueHierarchyMap;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.database.*;
import com.google.gson.Gson;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import static bo.tc.tcplanner.app.TCSchedulingApp.valueEntryMap;

public class FirebaseServer extends Thread {
    public static void main(String[] args) throws InterruptedException, IOException {
        FirebaseServer firebaseServer = new FirebaseServer();
        firebaseServer.createServer();
    }

    public FirebaseServer createServer() throws InterruptedException, IOException {
        Timeline timeline;
        FileInputStream serviceAccount = new FileInputStream(
                "C:\\Users\\bobob\\Downloads\\tcplanner-4bbab-firebase-adminsdk-95h9b-f77d329628.json");
        FirebaseOptions options = new FirebaseOptions.Builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .setDatabaseUrl("https://tcplanner-4bbab.firebaseio.com")
                .build();
        FirebaseApp.initializeApp(options);


        Thread.currentThread().join();
        return this;
    }

    public Timeline fullUpload(String dataName, Object data) {
        FirebaseDatabase.getInstance().getReference(dataName).getRoot().setValue(data, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                System.out.println(dataName + " Completed");
            }
        });
        return null;
    }
}