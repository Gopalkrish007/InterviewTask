package com.example.newtask.network;

import androidx.lifecycle.MutableLiveData;

import com.example.newtask.model.User;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class UserRepository {
    private Retrofit retrofit;
    private ApiService apiService;

    public UserRepository() {
        retrofit = new Retrofit.Builder()
                .baseUrl("https://reqres.in/api/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        apiService = retrofit.create(ApiService.class);

    }

    public MutableLiveData<List<User>> getUsers() {
        MutableLiveData<List<User>> users = new MutableLiveData<>();
        apiService.getUsers().enqueue(new Callback<UserResponse>() {
            @Override
            public void onResponse(Call<UserResponse> call, Response<UserResponse> response) {
                if (response.isSuccessful()) {
                    users.setValue(response.body().getData());
                }
            }

            @Override
            public void onFailure(Call<UserResponse> call, Throwable t) {
                users.setValue(null);
            }
        });
        return users;
    }
}

