package com.example.newtask.viewModel;

import android.app.Application;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.newtask.network.UserRepository;
import com.example.newtask.model.User;

import java.io.Closeable;
import java.util.List;

public class UserViewModel extends ViewModel {
    private MutableLiveData<List<User>> userList;
    private UserRepository userRepository;

        public UserViewModel() {

        userRepository = new UserRepository();
        userList = userRepository.getUsers();

    }



    public LiveData<List<User>> getUsers() {
        if (userList == null) {
            userList = userRepository.getUsers();
        }
        return userList;
    }
}

