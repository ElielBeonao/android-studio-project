package ca.uottawa.testnovigrad.fwk;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import ca.uottawa.testnovigrad.R;
import ca.uottawa.testnovigrad.models.User;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {
    private static List<User> userList = new ArrayList<>();

    private static OnUserEditButtonClickListener editButtonClickListener;
    private static OnUserEditButtonClickListener deleteButtonClickListener;

    public UserAdapter(List<User> userList) {
        this.userList = userList;
    }

    @Override
    public UserViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(UserViewHolder holder, int position) {
        User user = userList.get(position);
        holder.bind(user);
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder {
        private TextView userEmailAddressTextView, userFirstNameTextView, userLastNameTextView, userCompanyTextView, userRoleTextView;
        private Button btnModifier, btnSupprimer;

        public UserViewHolder(View itemView) {
            super(itemView);
            userEmailAddressTextView = itemView.findViewById(R.id.userEmailAddressTextView);
            userFirstNameTextView = itemView.findViewById(R.id.userFirstNameTextView);
            userLastNameTextView = itemView.findViewById(R.id.userLastNameTextView);
            userRoleTextView = itemView.findViewById(R.id.userRoleTextView);
            userCompanyTextView = itemView.findViewById(R.id.userCompanyTextView);

            btnModifier = itemView.findViewById(R.id.btn_user_modifier);
            btnSupprimer = itemView.findViewById(R.id.btn_user_supprimer);
        }

        public void bind(User user) {
            userEmailAddressTextView.setText(user.getEmail());
            userFirstNameTextView.setText(user.getFirstName());
            userLastNameTextView.setText(user.getLastName());
            userCompanyTextView.setText(user.getUserAuthority());

            btnModifier.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (editButtonClickListener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            editButtonClickListener.onEditButtonClick(userList.get(position));
                        }
                    }
                }
            });

            btnSupprimer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (deleteButtonClickListener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            deleteButtonClickListener.onDeleteButtonClick(userList.get(position));
                        }
                    }
                }
            });
        }
    }

    public void setOnEditButtonClickListener(OnUserEditButtonClickListener listener) {
        this.editButtonClickListener = listener;
    }

    public void setOnDeleteButtonClickListener(OnUserEditButtonClickListener listener) {
        this.deleteButtonClickListener = listener;
    }
}


