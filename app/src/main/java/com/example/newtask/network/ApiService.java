package com.example.newtask.network;

import retrofit2.Call;
import retrofit2.http.GET;

public interface ApiService {
    @GET("users?page=2")
    Call<UserResponse> getUsers();
}

