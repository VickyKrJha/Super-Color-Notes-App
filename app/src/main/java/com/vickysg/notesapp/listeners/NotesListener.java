package com.vickysg.notesapp.listeners;

import com.vickysg.notesapp.entities.Note;

public interface NotesListener {

    void onNoteClicked(Note note , int position);
}
