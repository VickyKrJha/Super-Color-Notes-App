package com.vickysg.notesapp.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.vickysg.notesapp.R;
import com.vickysg.notesapp.adapters.NotesAdapter;
import com.vickysg.notesapp.database.NotesDatabase;
import com.vickysg.notesapp.entities.Note;
import com.vickysg.notesapp.listeners.NotesListener;

import java.util.ArrayList;
import java.util.List;

// implements by NotesListener interface
public class MainActivity extends AppCompatActivity implements NotesListener {

    // This request code is used for adding a Note
    public static final int REQUEST_CODE_ADD_NOTE = 1;

    // This request code is used for Update a Note
    public static final int REQUEST_CODE_UPDATE_NOTE = 2;

    // This request code is used for Showing All  Notes
    public static final int REQUEST_CODE_SHOW_NOTES = 3;

    //  Function Started for Quick Actions
    public static final int REQUEST_CODE_SELECT_IMAGE = 4;
    private static final int REQUEST_CODE_STORAGE_PERMISSION = 5;

    private AlertDialog dialogAddURL;
    //  Function Ended  for Quick Actions


    private RecyclerView notesRecyclerView;

    private List<Note> noteList;
    private NotesAdapter notesAdapter;

    //
    private int noteClickedPosition = -1;

    private AdView mAdView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        //   Starting for Ads

        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });

        mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);


        //  Ending for Ads

        ImageView imageAddNoteMain = findViewById(R.id.imageAddNoteMain);
        imageAddNoteMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(getApplicationContext(), CreateNoteActivity.class),REQUEST_CODE_ADD_NOTE);
            }
        });

        notesRecyclerView = findViewById(R.id.notesRecyclerView);
        notesRecyclerView.setLayoutManager(
                new StaggeredGridLayoutManager(2,StaggeredGridLayoutManager.VERTICAL));

        noteList = new ArrayList<>();
        notesAdapter = new NotesAdapter(noteList,this);
        notesRecyclerView.setAdapter(notesAdapter);

        //  Calling Function for getting All Notes
        getNotes(REQUEST_CODE_SHOW_NOTES , false);


        //   For Search method or function
        EditText inputSearch = findViewById(R.id.inputSearch);
        inputSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Calling Method of Cancel Timer
                notesAdapter.cancelTimer();
            }

            @Override
            public void afterTextChanged(Editable s) {
            //  Calling Method of searchNotes
                if (noteList.size() != 0){
                    notesAdapter.searchNotes(s.toString());
                }
            }
        });
        //   End here Search method or function

        //  Function Started for Quick Actions
        findViewById(R.id.imageAddNote).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(getApplicationContext(), CreateNoteActivity.class),REQUEST_CODE_ADD_NOTE);

            }
        });

        findViewById(R.id.imageAddImage).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){

                    ActivityCompat.requestPermissions(MainActivity.this,
                            new String[] {Manifest.permission.READ_EXTERNAL_STORAGE},REQUEST_CODE_STORAGE_PERMISSION);
                }
                else{
                    // Calling Image Function
                    selectImage();
                }
            }
        });

        findViewById(R.id.imageAddWebLink).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Calling function for Add or Show Url Dialog
                showAddURLDialog();

            }
        });

        //  Function Ended for Quick Actions

    }

    //  Function Started for Quick Actions

    private void selectImage(){

        Intent intent = new Intent(Intent.ACTION_PICK , MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        if (intent.resolveActivity(getPackageManager())  != null){
            startActivityForResult(intent,REQUEST_CODE_SELECT_IMAGE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_STORAGE_PERMISSION && grantResults.length > 0 ){
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED){
                selectImage();
            }else {
                Toast.makeText(this, "Permission Denied!", Toast.LENGTH_SHORT).show();
            }
        }
    }
    private String getPathFromUri (Uri contentUri){
        String filePath;
        Cursor cursor = getContentResolver().query(contentUri,null,null,null,null);
        if (cursor == null){
            filePath = contentUri.getPath();
        }else {
            cursor.moveToFirst();
            int index = cursor.getColumnIndex("_data");
            filePath = cursor.getString(index);
            cursor.close();
        }
        return filePath;
    }

    //  Function Ended for Quick Actions



    @Override
    public void onNoteClicked(Note note, int position) {
        noteClickedPosition = position;
        Intent intent = new Intent(getApplicationContext(),CreateNoteActivity.class);
        intent.putExtra("isViewOrUpdate",true);
        intent.putExtra("note",note);
        startActivityForResult(intent,REQUEST_CODE_UPDATE_NOTE);
    }

    //  Just as You need an Async task to save a note , you will also need it to get notes from the database

    private void getNotes( final int requestCode , final boolean isNoteDeleted){

        //  Also add this SuppressLint

        @SuppressLint("StaticFieldLeak")
        class GetNotesTask extends AsyncTask<Void,Void, List<Note>> {
            @Override
            protected List<Note> doInBackground(Void... voids) {
               return NotesDatabase.getDatabse(getApplicationContext()).noteDao().getAllNotes();
            }

            @Override
            protected void onPostExecute(List<Note> notes) {
                super.onPostExecute(notes);
                // print values in console
//                Log.d("MY_NOTES",notes.toString());

                if (requestCode == REQUEST_CODE_SHOW_NOTES){
                    noteList.addAll(notes);
                    notesAdapter.notifyDataSetChanged();
                }else if (requestCode == REQUEST_CODE_ADD_NOTE){
                    noteList.add(0,notes.get(0));
                    notesAdapter.notifyItemInserted(0);
                    notesRecyclerView.smoothScrollToPosition(0);
                }else if (requestCode == REQUEST_CODE_UPDATE_NOTE){
                    noteList.remove(noteClickedPosition);
//                    noteList.add(noteClickedPosition,notes.get(noteClickedPosition));
//                    notesAdapter.notifyItemChanged(noteClickedPosition);

                    if (isNoteDeleted){
                        notesAdapter.notifyItemRemoved(noteClickedPosition);
                    }else{
                        noteList.add(noteClickedPosition,notes.get(noteClickedPosition));
                        notesAdapter.notifyItemChanged(noteClickedPosition);
                    }
                }

            }
        }

        new GetNotesTask().execute();

    }

//   This Function or method is used for Auto Update Notes
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_ADD_NOTE && resultCode == RESULT_OK){
            getNotes(REQUEST_CODE_ADD_NOTE , false);
        }else if (requestCode == REQUEST_CODE_UPDATE_NOTE && resultCode == RESULT_OK){
            if (data != null){
                getNotes(REQUEST_CODE_UPDATE_NOTE , data.getBooleanExtra("isNotDeleted" , false));
            }
        }  //  Function Started for Quick Actions

            else if (requestCode == REQUEST_CODE_SELECT_IMAGE && resultCode == RESULT_OK){
                if (data != null){
                    Uri selectImageUri = data.getData();
                    if (selectImageUri != null){
                        try{
                            String selectedImagePath = getPathFromUri(selectImageUri);
                            Intent intent = new Intent(getApplicationContext(),CreateNoteActivity.class);
                            intent.putExtra("isFromQuickActions",true);
                            intent.putExtra("quickActionType","image");
                            intent.putExtra("imagePath",selectedImagePath);
                            startActivityForResult(intent,REQUEST_CODE_ADD_NOTE);
                        }catch (Exception exception){
                            Toast.makeText(this, exception.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        }
        //  Function Ended for Quick Actions
    }

    //  Function Started for Quick Actions

    //  For Create or showing Url Dialog
    private void showAddURLDialog(){
        if (dialogAddURL == null){
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            View view = LayoutInflater.from(this).inflate(R.layout.layout_add_url,
                    (ViewGroup) findViewById(R.id.layoutAddUrlContainer));

            builder.setView(view);

            dialogAddURL = builder.create();
            if (dialogAddURL.getWindow() != null) {

                dialogAddURL.getWindow().setBackgroundDrawable(new ColorDrawable(0));
            }

            final EditText inputURL = view.findViewById(R.id.inputURL);
            inputURL.requestFocus();

            view.findViewById(R.id.textAdd).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (inputURL.getText().toString().trim().isEmpty()){
                        Toast.makeText(MainActivity.this, "Enter URL", Toast.LENGTH_SHORT).show();
                    }else if (!Patterns.WEB_URL.matcher(inputURL.getText().toString()).matches()){
                        Toast.makeText(MainActivity.this, "Enter Valid URL", Toast.LENGTH_SHORT).show();
                    }else {
                        dialogAddURL.dismiss();

                        Intent intent = new Intent(getApplicationContext(),CreateNoteActivity.class);
                        intent.putExtra("isFromQuickActions",true);
                        intent.putExtra("quickActionType","URL");
                        intent.putExtra("URL",inputURL.getText().toString());
                        startActivityForResult(intent,REQUEST_CODE_ADD_NOTE);
                    }
                }
            });

            view.findViewById(R.id.textCancel).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialogAddURL.dismiss();
                }
            });
        }
        dialogAddURL.show();
    }

    //  Function Ended for Quick Actions
}