package com.example.expoporgra2prompt;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> {
    private List<MensajeChat> mensajes;

    public ChatAdapter(List<MensajeChat> mensajes) {
        this.mensajes = mensajes;
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(viewType == 0 ? R.layout.item_mensaje_usuario : R.layout.item_mensaje_bot, parent, false);
        return new ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        holder.tvMensaje.setText(mensajes.get(position).getTexto());
    }

    @Override
    public int getItemCount() {
        return mensajes.size();
    }

    @Override
    public int getItemViewType(int position) {
        return mensajes.get(position).esUsuario() ? 0 : 1;
    }

    static class ChatViewHolder extends RecyclerView.ViewHolder {
        TextView tvMensaje;
        ChatViewHolder(View itemView) {
            super(itemView);
            tvMensaje = itemView.findViewById(R.id.tvMensajeChat);
        }
    }
}

