package com.proyecto.droidnotes.retrofit;

import com.proyecto.droidnotes.models.FCMBody;
import com.proyecto.droidnotes.models.FCMResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface IFCMApi {
    @Headers({
            "Content-Type:application/json",
            "Authorization:key=AAAAw1qIvMw:APA91bFZ-O2-cm4eo5eZPbv277xyRgq75BfyzwakexpBUlq3eiLpgOeGudpl5BQZM4-Gwsi6k8k1ak7nPYyvPdlesXUw9Oz8_OMT_xBhlUObev4H-7Rii4HWFBPpuYOvgs8NtdHtCitG"
    })
    @POST("fcm/send")

    Call<FCMResponse> send(@Body FCMBody body);
}
