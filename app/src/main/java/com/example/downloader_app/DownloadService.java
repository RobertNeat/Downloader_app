package com.example.downloader_app;

import android.util.Log;
import android.os.Build;
import android.os.Environment;
import android.content.Intent;
import android.content.Context;
import android.app.Notification;
import android.app.IntentService;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.DataInputStream;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;
import java.net.URL;
import java.net.ProtocolException;
import java.net.MalformedURLException;
import javax.net.ssl.HttpsURLConnection;

//wykonywanie pobierania pliku w usłudze pierwszoplanowej
public class DownloadService extends IntentService {

    private static final String NAZWA_ZADANIA_USLUGI = "com.example.downloader_app.action.nazwa_zadania";
    private static final String KLUCZ_PARAMETRY_USLUGI = "com.example.downloader_app.extra.nazwa_klucza_parametru";

    private static final int ID_POWIADOMIENIA = 1;  //id powiadomienia dla usługi (każda usługa musi mieć powiadomienie

    public static final String POWIADOMIENIE = "com.example.downloader_app.odbiornik";
    public static final String INFO = "info";

    NotificationManager mManagerPowiadomien;

    Double a;
    Double b;
    Double loadingDouble;
    int loadingInt;

    boolean strumienDoPlikuAktywny = false;
    boolean strumienZSieciAktywny = false;

    //utworzenie nadawcy komunikatów
    private void wyslijBroadcast(ProgressInfo wartosc) {
        Intent zamiar = new Intent(POWIADOMIENIE);
        zamiar.putExtra(INFO, wartosc);
        LocalBroadcastManager.getInstance(this).sendBroadcast(zamiar);
    }


    //przekazanie parametrów do uruchomienia usługi (funkcja pomocnicza)
    public static void uruchomUsluge(Context context, String parametr, ProgressInfo startingValues) {
        Intent zamiar = new Intent(context, DownloadService.class);
        zamiar.setAction(NAZWA_ZADANIA_USLUGI);
        zamiar.putExtra(KLUCZ_PARAMETRY_USLUGI, parametr);
        zamiar.putExtra(INFO, startingValues);
        context.startService(zamiar);
    }

    //konstruktor
    public DownloadService() {
        super("DownloadService");
    }


    //metoda uruchamiająca długotrwałe zadanie
    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        //wyświetlenie powiadomienia i przejście w tryb pierwszoplanowy
        mManagerPowiadomien = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        przygotujKanalPowiadomien();
        startForeground(ID_POWIADOMIENIA, utworzPowiadomienie(intent.getParcelableExtra(INFO)));

        if (intent != null) {
            final String action = intent.getAction();
            if (NAZWA_ZADANIA_USLUGI.equals(action)) {  //dopasowanie akcji do odpowiedniej obsługi akcji
                final String param1 = intent.getStringExtra(KLUCZ_PARAMETRY_USLUGI);
                ProgressInfo startingValues = intent.getParcelableExtra(INFO); //wydobycie parametrów
                wykonajZadanie(param1, startingValues); //uruchomienie akcji z parametrami
            } else {
                Log.e("downloader_app", "nieznana akcja");
            }
        }
        Log.d("downloader_app", "usługa wykonała zadanie");
    }


    //metoda wykonująca zadanie pobierania (rozpoczyna się po otrzymaniu uprawnień)
    private void wykonajZadanie(String mAdres, ProgressInfo startingValues) {
        HttpsURLConnection polaczenie = null;
        int ROZMIAR_BLOKU = 2048;
        try{    //rozpoczęcie połączenia
            URL url = new URL(mAdres);
            polaczenie = (HttpsURLConnection) url.openConnection();
            polaczenie.setRequestMethod("GET");
            File plikRoboczy = new File(url.getFile());
            File plikWyjsciowy = new File(Environment.getExternalStorageDirectory() + File.separator+ plikRoboczy.getName());
            if(plikWyjsciowy.exists()) plikWyjsciowy.delete();
            InputStream strumienZSieci = null;
            FileOutputStream strumienDoPliku = null;

            strumienDoPlikuAktywny = true;
            strumienZSieciAktywny = true;
            //rozpoczęcie zapisywania do pliku z otwartego ze strumienia
            DataInputStream czytnik = new DataInputStream(polaczenie.getInputStream());
            strumienDoPliku = new FileOutputStream(plikWyjsciowy.getPath());
            byte bufor[] = new byte[ROZMIAR_BLOKU];
            int pobrano = czytnik.read(bufor, 0, ROZMIAR_BLOKU);
            while (pobrano != -1){
                strumienDoPliku.write(bufor, 0, pobrano);
                startingValues.pobranychBajtow += pobrano;
                pobrano = czytnik.read(bufor, 0, ROZMIAR_BLOKU);
                wyslijBroadcast(startingValues);
                mManagerPowiadomien.notify(ID_POWIADOMIENIA, utworzPowiadomienie(startingValues));
            }

            if (strumienZSieci != null) {
                try{
                    strumienZSieci.close();
                    strumienDoPlikuAktywny = false;
                }
                catch(IOException e){
                    e.printStackTrace();
                }
            }
            if (strumienDoPliku != null){
                try{
                    strumienDoPliku.close();
                    strumienZSieciAktywny = false;
                }
                catch(IOException e){
                    e.printStackTrace();
                }
            }
            if (polaczenie != null) polaczenie.disconnect();
        } catch (ProtocolException e) {
            throw new RuntimeException(e);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    //utworzenie kanału powiadomień dla wersji >OREO
    private void przygotujKanalPowiadomien(){
        NotificationManager mManagerPowiadomien = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            CharSequence name = getString(R.string.app_name);
            NotificationChannel kanal = new NotificationChannel("71", name, NotificationManager.IMPORTANCE_LOW);
            mManagerPowiadomien.createNotificationChannel(kanal);
        }
    }


    //utworzenie metody tworzącej powiadomienie (aktualizacja powiadomienia następuje poprzez ponowne utworzenie powiadomienia)
    private Notification utworzPowiadomienie(ProgressInfo ProgressInfo){
        Intent intencjaPowiadomienia = new Intent(this, MainActivity.class);
        intencjaPowiadomienia.putExtra(INFO, ProgressInfo); //dodanie aktualnego stanu pobierania

        TaskStackBuilder budowniczyStosu = TaskStackBuilder.create(this);   //odbudowanie stosu aktywności w aplikacji
        budowniczyStosu.addParentStack(MainActivity.class);
        budowniczyStosu.addNextIntent(intencjaPowiadomienia);
        PendingIntent intencjaOczekujaca = budowniczyStosu.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        //obliczenie procenta pobrania pliku
        a = Double.valueOf(ProgressInfo.pobranychBajtow);
        b = Double.valueOf(ProgressInfo.rozmiar);
        loadingDouble = (a/b)*100;
        loadingInt = (int) Math.round(loadingDouble);
        //zbudowanie powiadomienia
        Notification.Builder budowniczyPowiadomien = new Notification.Builder(this);
        budowniczyPowiadomien.setContentTitle("Pobrano " + Integer.toString(ProgressInfo.pobranychBajtow) + " bajtow, " + Integer.toString(loadingInt) + "%")
                .setProgress(100, loadingInt, false)
                .setContentIntent(intencjaOczekujaca)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setWhen(System.currentTimeMillis())
                .setPriority(Notification.PRIORITY_HIGH);
        //ustawienie trwania powiadomienia w wypadku trwania pobierania
        if (strumienDoPlikuAktywny && strumienZSieciAktywny){
            budowniczyPowiadomien.setOngoing(false);
        } else {
            budowniczyPowiadomien.setOngoing(true);
        }
        //ustawienie kanału powiadomienia dla >OREO
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            budowniczyPowiadomien.setChannelId("71");
        }
        return budowniczyPowiadomien.build();
    }


}
