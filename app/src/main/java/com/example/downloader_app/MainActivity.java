package com.example.downloader_app;

import android.Manifest;
import android.os.Bundle;
import android.os.AsyncTask;
import android.view.View;
import android.widget.Toast;
import android.widget.Button;
import android.widget.TextView;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.BroadcastReceiver;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import java.net.URL;
import java.util.HashMap;
import javax.net.ssl.HttpsURLConnection;

public class MainActivity extends AppCompatActivity implements View.OnFocusChangeListener{

    Button btnInfo;
    Button btnFile;
    EditText address;
    TextView fileSize;
    TextView fileType;
    TextView bytesDownloaded;
    ProgressBar downloadingBar;
    int loadingInt;

    //utworzenie klasy odbiorcy (niezbędne do odbierania rozgłoszeń)
    private BroadcastReceiver mOdbiorcaRozgloszen = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle paczka = intent.getExtras();
            ProgressInfo ProgressInfo = paczka.getParcelable(DownloadService.INFO);
            setBytesDownloaded(ProgressInfo.pobranychBajtow);
            loadingInt = (int) Math.round((new Double(ProgressInfo.pobranychBajtow) / new Double(ProgressInfo.rozmiar)) * 100);
            setProgressBar(loadingInt);
        }
    };

    //rejestracja odbiorcy komunikatów
    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(
                mOdbiorcaRozgloszen, new IntentFilter(DownloadService.POWIADOMIENIE));
    }


    //wyrejestrowanie odbiorcy komunikatów
    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mOdbiorcaRozgloszen);
    }


    //wykonywanie zadań w wątku pobocznym
    private class PolaczenieAsync extends AsyncTask<String, Integer, HashMap<Integer, String>> {
        //stworzenie zadania do wykonania w wątku pobocznym
        @Override
        protected HashMap<Integer, String> doInBackground(String... params) {
            HttpsURLConnection polaczenie = null;
            String adres_url = params[0];
            Integer rozmiar = 0;
            String mType = "0";
            try {
                URL url = new URL(adres_url);
                polaczenie = (HttpsURLConnection) url.openConnection();
                polaczenie.setRequestMethod("GET");
                rozmiar = polaczenie.getContentLength();
                mType = polaczenie.getContentType();
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            finally {
                if (polaczenie != null) polaczenie.disconnect();
            }
            HashMap<Integer, String> wynik = new HashMap<>();
            wynik.put(rozmiar, mType);
            publishProgress();
            return wynik;
        }

        //opcjonalna - aktualizacja wątku GUI
        @Override
        protected void onProgressUpdate(Integer... value) {     }

        //aktualizacja wyników po wykonaniu zadania w wątku pobocznym
        protected void onPostExecute(HashMap<Integer, String> result) {
            for ( HashMap.Entry<Integer, String> entry : result.entrySet()) {
                Integer key = entry.getKey();
                String value = entry.getValue();
                setFileSize(key);
                setFileType(value);
            }
        }
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        btnInfo = findViewById(R.id.info_BTN);
        btnFile = findViewById(R.id.download_BTN);
        address = findViewById(R.id.url_ET);
        fileSize = findViewById(R.id.out_file_size_TV);
        fileType = findViewById(R.id.out_file_type_TV);
        bytesDownloaded = findViewById(R.id.out_bytes_downloaded_TV);
        downloadingBar = findViewById(R.id.downloading_PB);

        if(getIntent().hasExtra("pobraneBajty")) {
            setBytesDownloaded(getIntent().getIntExtra("pobraneBajty", 0));
        }

        //wywołanie metody asynchronicznej pod przyciskiem
        btnInfo.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                PolaczenieAsync task = new PolaczenieAsync();
                String value = address.getText().toString();
                task.execute(value);
            }
        });

        btnFile.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                //poproszenie o prawo zapisu do pamięci urządzenia
                if (ActivityCompat.checkSelfPermission(v.getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    ProgressInfo downloadMgr = new ProgressInfo();  //zainicjalizowanie pobierania
                    downloadMgr.set_Rozmiar(Integer.parseInt(fileSize.getText().toString()));
                    downloadMgr.set_pobranychBajtow(0);
                    downloadMgr.set_Status(0);
                    DownloadService.uruchomUsluge(MainActivity.this, address.getText().toString(), downloadMgr);
                    //uruchomienie usługi pobierania pliku
                }
                else {
                    //brak prawa zapisu ... prosimy o prawo zapisu po naciśnięciu przycisku
                    ActivityCompat.requestPermissions(
                            MainActivity.this,
                            new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            50
                    );
                }
            }
        });
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString("size", fileSize.getText().toString());
        outState.putString("type", fileType.getText().toString());
        super.onSaveInstanceState(outState);
    }


    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        fileSize.setText(savedInstanceState.getString("size"));
        fileType.setText(savedInstanceState.getString("type"));
    }


    //obsługa decyzji po wyświetleniu zapytania o uprawnienia (można wywołać pobieranie)
    @Override
    public void onRequestPermissionsResult(int kodZadania, @NonNull String[] uprawnienia, @NonNull int[] decyzje) {
        super.onRequestPermissionsResult(kodZadania, uprawnienia, decyzje);
        switch (kodZadania)
        {
            case 50:
                if (uprawnienia.length > 0 && uprawnienia[0].equals(Manifest.permission.WRITE_EXTERNAL_STORAGE) && decyzje[0] == PackageManager.PERMISSION_GRANTED)
                {   Toast.makeText(MainActivity.this, "Uprawnienia zapisu na karcie przyznane prawidłowo", Toast.LENGTH_SHORT).show();  }
                else
                {   Toast.makeText(MainActivity.this, "Uprawnienia zapisu na karcie zostały odrzucone", Toast.LENGTH_SHORT).show(); }
                break;
            default:
            {   Toast.makeText(MainActivity.this, "Otrzymano nieprawidłowy kod uprawnień:"+kodZadania, Toast.LENGTH_SHORT).show();  }
            break;
        }
    }


    public void setBytesDownloaded(Integer bytesDownloaded) {
        this.bytesDownloaded.setText(bytesDownloaded.toString());
    }


    public void setProgressBar(Integer loadingInt)
    {
        this.downloadingBar.setProgress(loadingInt);
    }


    public void setFileSize(Integer fileSize) {
        this.fileSize.setText(fileSize.toString());
    }


    public void setFileType(String fileType) {
        this.fileType.setText(fileType);
    }


    @Override
    public void onFocusChange(View view, boolean b) {}
}