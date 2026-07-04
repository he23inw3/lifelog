package jp.he23inw3.asset.configuration;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import io.quarkus.arc.profile.IfBuildProfile;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Singleton;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;

@ApplicationScoped
public class GoogleCalendarProducer {

    /**
     * アプリケーション名定数。
     */
    private static final String APPLICATION_NAME = "LifeLog";

    /**
     * prod プロファイル時に優先してインジェクションされる Calendar インスタンスを生成します。
     * 
     * @return
     * @throws GeneralSecurityException
     * @throws IOException
     */
    @Produces
    @Singleton
    @IfBuildProfile("prod")
    public Calendar calendar() throws GeneralSecurityException, IOException {
        GoogleCredentials credentials = GoogleCredentials.getApplicationDefault()
                .createScoped(Collections.singletonList(CalendarScopes.CALENDAR));
        return new Calendar.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                GsonFactory.getDefaultInstance(),
                new HttpCredentialsAdapter(credentials))
                        .setApplicationName(APPLICATION_NAME)
                        .build();
    }
}
