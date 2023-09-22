package org.example.configuration.initializer;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import io.quarkus.runtime.Startup;

import java.io.FileInputStream;
import java.io.IOException;

@Startup
public class FirebaseInitializer {

    public FirebaseInitializer() {
        try {
            // Load the Firebase service account JSON file
            FileInputStream serviceAccount = new FileInputStream("path/to/your/serviceAccountKey.json");

            // Initialize Firebase with the service account credentials
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .setDatabaseUrl("https://newstorageforuplodapp.firebaseio.com")
                    .build();

            FirebaseApp.initializeApp(options);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
