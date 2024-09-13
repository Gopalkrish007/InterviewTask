package com.example.newtask.view;

import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.newtask.R;
import com.example.newtask.UserImage;
import com.example.newtask.UserImageDao;
import com.example.newtask.activity.LaunchActivity;
import com.example.newtask.model.User;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {
    private List<User> userList;

    private OnUploadImageClickListener uploadImageListener;
    private UserImageDao userImageDao;

    private Executor executor;


    public interface OnUploadImageClickListener {
        void onUploadImageClicked(int userId);
    }

    public UserAdapter(List<User> users, OnUploadImageClickListener listener, UserImageDao userImageDao) {
        this.userList = users;
        this.uploadImageListener = listener;
        this.userImageDao = userImageDao;
        this.executor = Executors.newSingleThreadExecutor(); // Single thread executor for background tasks


    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_item, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = userList.get(position);
        holder.name.setText(user.getFirst_name() + " " + user.getLast_name());
        holder.email.setText(user.getEmail());

        executor.execute(() -> {
            UserImage userImage = userImageDao.getUserImage(user.getId());
            holder.itemView.post(() -> {
                if (userImage != null) {
                    // Display the uploaded image
                    Glide.with(holder.itemView.getContext())
                            .load(userImage.getImageUri())
                            .into(holder.avatar);
                    Log.d("Iguru", "Displaying uploaded image for UserID: " + user.getId());
                } else {
                    Glide.with(holder.itemView.getContext())
                            .load(user.getAvatar())
                            .into(holder.avatar);
                    Log.d("Iguru", "Displaying API avatar for UserID: " + user.getId());

                }
            });
        });
        // Set up the upload icon click listener
        holder.uploadIcon.setOnClickListener(v -> {
            if (uploadImageListener != null) {
                uploadImageListener.onUploadImageClicked(user.getId());
            }
        });
    }


    @Override
    public int getItemCount() {
        return userList.size();
    }

    public void updateUsers(List<User> users) {
        this.userList = users;
        notifyDataSetChanged();
    }

    class UserViewHolder extends RecyclerView.ViewHolder {
        TextView name, email;
        ImageView avatar, uploadIcon, deleteIcon;

        public UserViewHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.user_name);
            email = itemView.findViewById(R.id.user_email);
            avatar = itemView.findViewById(R.id.user_avatar);
            uploadIcon = itemView.findViewById(R.id.upload_icon);
            deleteIcon = itemView.findViewById(R.id.delete_icon); // Find the delete icon
            deleteIcon.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    User user = userList.get(position);
                    // Call delete method, assuming imageUri is available
                    ((LaunchActivity) itemView.getContext()).deleteImage(user.getId(), Uri.parse(user.getAvatar())); // Use appropriate Uri for deletion
                }
            });

        }
    }
}

