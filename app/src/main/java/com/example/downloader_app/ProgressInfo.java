package com.example.downloader_app;

import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.NonNull;

// klasa implementująca zapisanie całego obiektu do Bundle
public class ProgressInfo implements Parcelable {

    public int pobranychBajtow;
    public int rozmiar;
    public int status;


    public void set_pobranychBajtow(int pobranychBajtow) {
        this.pobranychBajtow = pobranychBajtow;
    }


    public int get_Rozmiar() {
        return rozmiar;
    }


    public void set_Rozmiar(int rozmiar) {
        this.rozmiar = rozmiar;
    }


    public void set_Status(int status) {
        this.status = status;
    }


    public ProgressInfo() {
        pobranychBajtow = 0;
        rozmiar = 0;
        status = 0;
    }


    //wydobycie wartośći pól z obiektu wpisanego w Parcel
    protected ProgressInfo(Parcel in) {
        pobranychBajtow = in.readInt();
        rozmiar = in.readInt();
        status = in.readInt();
    }



    public static final Creator<ProgressInfo> CREATOR = new Creator<ProgressInfo>() {
        @Override
        public ProgressInfo createFromParcel(Parcel in) {
            return new ProgressInfo(in);
        }
        @Override
        public ProgressInfo[] newArray(int size) {
            return new ProgressInfo[size];
        }
    };


    @Override
    public int describeContents() {
        return 0;
    }


    //zapisywanie stanu obiektu do paczki z parametru
    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeInt(pobranychBajtow);
        dest.writeInt(rozmiar);
        dest.writeInt(status);
    }
}
