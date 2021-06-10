package com.vickysg.notesapp.adapters;

import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.makeramen.roundedimageview.RoundedImageView;
import com.vickysg.notesapp.R;
import com.vickysg.notesapp.entities.Note;
import com.vickysg.notesapp.listeners.NotesListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class NotesAdapter extends  RecyclerView.Adapter<NotesAdapter.NoteViewHolder>{

    private List<Note> notes;

    //  for using Noteslistener Interface
    private NotesListener notesListener;

    //   Adding for Search options
    private Timer timer;
    private List<Note> notesSource ;

    public NotesAdapter(List<Note> notes, NotesListener notesListener) {
        this.notes = notes;
        //  Adding this for interface
        this.notesListener = notesListener;

        //  Adding for Search Options
        notesSource = notes;
    }

    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_container_note,parent,false);
        return new NoteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NoteViewHolder holder, int position) {
        // Calling setNote function here
        holder.setNote(notes.get(position));
        //  Adding this for interface
        holder.layoutNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                notesListener.onNoteClicked(notes.get(position),position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return notes.size();
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    static class NoteViewHolder extends RecyclerView.ViewHolder{

        TextView textTitle , textSubtitle , textDateTime ;

        LinearLayout layoutNote;

        RoundedImageView imageNote;

        public NoteViewHolder(@NonNull View itemView) {
            super(itemView);
            textTitle = itemView.findViewById(R.id.textTitle);
            textSubtitle = itemView.findViewById(R.id.textSubitle);
            textDateTime = itemView.findViewById(R.id.textDateTime);

            layoutNote = itemView.findViewById(R.id.layoutNote);

            imageNote = itemView.findViewById(R.id.imageNote);

        }

        void setNote(Note note){
            textTitle.setText(note.getTitle());
            if (note.getSubtitle().trim().isEmpty()){
                textSubtitle.setVisibility(View.GONE);
            }else {
                textSubtitle.setText(note.getSubtitle());
            }
            textDateTime.setText(note.getDateTime());

            GradientDrawable gradientDrawable = (GradientDrawable) layoutNote.getBackground();
            if (note.getColor() != null){
                gradientDrawable.setColor(Color.parseColor(note.getColor()));
            }else {
                gradientDrawable.setColor(Color.parseColor("#333333"));
            }

            //  For Displaying Image
            if (note.getImagePath() != null){
                imageNote.setImageBitmap(BitmapFactory.decodeFile(note.getImagePath()));
                imageNote.setVisibility(View.VISIBLE);
            }else {
                imageNote.setVisibility(View.GONE);
            }
        }
    }

    //   Function for search from notes

    public void searchNotes(final String searchKeyword){
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
               if (searchKeyword.trim().isEmpty()){
                   notes = notesSource;
               }else{
                   ArrayList<Note> temp = new ArrayList<>();
                   for (Note note : notesSource){
                       if (note.getTitle().toLowerCase().contains(searchKeyword.toLowerCase())
                            || note.getSubtitle().toLowerCase().contains(searchKeyword.toLowerCase())
                            || note.getNoteText().toLowerCase().contains(searchKeyword.toLowerCase())){

                           temp.add(note);

                       }

                   }

                   notes = temp ;
               }

               new Handler(Looper.getMainLooper()).post(new Runnable() {
                   @Override
                   public void run() {
                       notifyDataSetChanged();
                   }
               });
            }
        },500);
    }

    public void cancelTimer(){
        if (timer != null){
            timer.cancel();
        }
    }
    //  End here Function for search from notes
}
