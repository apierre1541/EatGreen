package com.example.eatgreen;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    private List<User> userList;
    private Context context;

    public UserAdapter(List<User> userList, Context context) {
        this.userList = userList;
        this.context = context;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // On lie le design de la ligne (item_user.xml)
        View view = LayoutInflater.from(context).inflate(R.layout.item_user, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = userList.get(position);

        // On affiche les vraies infos
        holder.txtName.setText(user.getPrenom() + " " + user.getNom());
        holder.txtRole.setText("Rôle : " + user.getRole());

        // CLIC SUR MODIFIER (Crayon)
        holder.btnEdit.setOnClickListener(v -> {
            Intent intent = new Intent(context, ModifierUserActivity.class);
            intent.putExtra("user_id", user.getId());
            intent.putExtra("user_nom", user.getNom());
            intent.putExtra("user_prenom", user.getPrenom());
            intent.putExtra("user_email", user.getEmail());
            intent.putExtra("user_role", user.getRole());
            context.startActivity(intent);
        });

        // CLIC SUR SUPPRIMER (Poubelle)
        holder.btnDelete.setOnClickListener(v -> {
            // On appelle une méthode dans GestionUtilisateursActivity pour supprimer
            if (context instanceof GestionUtilisateursActivity) {
                ((GestionUtilisateursActivity) context).supprimerUtilisateur(user.getId(), position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView txtName, txtRole;
        ImageButton btnEdit, btnDelete;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            // On récupère les ID de item_user.xml
            txtName = itemView.findViewById(R.id.txtName);
            txtRole = itemView.findViewById(R.id.txtRole);
            btnEdit = itemView.findViewById(R.id.btnEditUser);
            btnDelete = itemView.findViewById(R.id.btnDeleteUser);
        }
    }
}