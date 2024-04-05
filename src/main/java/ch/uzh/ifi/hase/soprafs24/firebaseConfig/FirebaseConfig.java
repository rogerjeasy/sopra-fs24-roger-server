package ch.uzh.ifi.hase.soprafs24.firebaseConfig;

import java.io.FileInputStream;
import javax.annotation.PostConstruct;
import io.github.cdimascio.dotenv.Dotenv;

import org.springframework.stereotype.Service;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;

@Service
public class FirebaseConfig {
    
    @PostConstruct
    public void configureFirebase() {
        try {
            Dotenv dotenv = Dotenv.load();
            String firebaseConfig = dotenv.get("FIREBASE_CONFIG");
            FileInputStream serviceAccount = new FileInputStream(firebaseConfig);

            FirebaseOptions options = new FirebaseOptions.Builder()

            .setCredentials(GoogleCredentials.fromStream(serviceAccount))

            // .setDatabaseUrl("https://roger-spring-default-rtdb.europe-west1.firebasedatabase.app")

            .build();

            FirebaseApp.initializeApp(options);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
