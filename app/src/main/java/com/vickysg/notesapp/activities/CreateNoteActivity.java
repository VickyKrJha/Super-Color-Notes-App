package com.vickysg.notesapp.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.vickysg.notesapp.R;
import com.vickysg.notesapp.database.NotesDatabase;
import com.vickysg.notesapp.entities.Note;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class CreateNoteActivity extends AppCompatActivity {

    private EditText inputNoteTitle , inputNoteSubtitle , inputNoteText;
    private TextView textDateTime;

    private View viewSubtitleIndicator;
    private String selectedNotecolor;

    private ImageView imageNote;
    private String selectedImagePath;

    private TextView textWebURL;
    private LinearLayout layoutWebUrl;

    private static final int REQUEST_CODE_STORAGE_PERMISSION = 1;
    private static final int REQUEST_CODE_SELECT_IMAGE = 2;

    // Dialog for Url
    private AlertDialog dialogAddURL;

    // Dialog for Delete the Note
    private AlertDialog dialogDeleteNote;

    //
    private Note alreadyAvailableNote;

    private AdView mAdView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_note);

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

        ImageView imageBack = findViewById(R.id.imageBack);
        imageBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        inputNoteTitle = findViewById(R.id.inputNoteTitle);
        inputNoteSubtitle = findViewById(R.id.inputNoteSubtitle);
        inputNoteText = findViewById(R.id.inputNote);
        textDateTime = findViewById(R.id.textDateTime);

        viewSubtitleIndicator = findViewById(R.id.viewSubtitileIndicator);

        imageNote = findViewById(R.id.imageNote);

        textWebURL = findViewById(R.id.textWebURL);
        layoutWebUrl = findViewById(R.id.layoutWebURL);

        // Java saving unknown keywords to project dictionary , so we have to put cursor on EEEE and select this
        textDateTime.setText(new SimpleDateFormat("EEEE, dd MMMM yyyy HH:mm a", Locale.getDefault())
                .format(new Date())
        );

        ImageView imageSave = findViewById(R.id.imageSave);
        imageSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Calling saveNote method or function
                saveNote();
            }
        });

        selectedNotecolor = "#333333" ;

        selectedImagePath = "" ;


        //  getting from intent.putExtra
        if (getIntent().getBooleanExtra("isViewOrUpdate",false)){
            alreadyAvailableNote = (Note) getIntent().getSerializableExtra("note");
            // Calling method or function
            setViewOrUpdateNote();
        }

        //  for deleting Url
        findViewById(R.id.imageRemoveWebURL).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                textWebURL.setText(null);
                layoutWebUrl.setVisibility(View.GONE);
            }
        });

        //  for deleting Image
        findViewById(R.id.imageRemoveImage).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               imageNote.setImageBitmap(null);
               imageNote.setVisibility(View.GONE);
               findViewById(R.id.imageRemoveImage).setVisibility(View.GONE);
               selectedImagePath = "";
            }
        });

        //  Function Started for Quick Actions

        if (getIntent().getBooleanExtra("isFromQuickActions" , false)){
            String type = getIntent().getStringExtra("quickActionType");
            if (type != null){
                if (type.equals("image")){
                    selectedImagePath = getIntent().getStringExtra("imagePath");
                    imageNote.setImageBitmap(BitmapFactory.decodeFile(selectedImagePath));
                    imageNote.setVisibility(View.VISIBLE);
                    findViewById(R.id.imageRemoveImage).setVisibility(View.VISIBLE);
                }else if (type.equals("URL")){
                    textWebURL.setText(getIntent().getStringExtra("URL"));
                    layoutWebUrl.setVisibility(View.VISIBLE);
                }
            }
        }

        //  Function Ended for Quick Actions

        initMiscellaneous();
        setSubtitleIndicatorColor();


    }

    //

    private void setViewOrUpdateNote(){

        inputNoteTitle.setText(alreadyAvailableNote.getTitle());
        inputNoteSubtitle.setText(alreadyAvailableNote.getSubtitle());
        inputNoteText.setText(alreadyAvailableNote.getNoteText());
        textDateTime.setText(alreadyAvailableNote.getDateTime());

        if (alreadyAvailableNote.getImagePath() != null && !alreadyAvailableNote.getImagePath().trim().isEmpty()){
            imageNote.setImageBitmap(BitmapFactory.decodeFile(alreadyAvailableNote.getImagePath()));
            imageNote.setVisibility(View.VISIBLE);
            // For Image Delete Icon
            findViewById(R.id.imageRemoveImage).setVisibility(View.VISIBLE);

            selectedImagePath = alreadyAvailableNote.getImagePath();
        }

        if (alreadyAvailableNote.getWebLink() != null && !alreadyAvailableNote.getWebLink().trim().isEmpty()){
            textWebURL.setText(alreadyAvailableNote.getWebLink());
            layoutWebUrl.setVisibility(View.VISIBLE);
        }

    }

    private void saveNote(){
        if(inputNoteTitle.getText().toString().trim().isEmpty()){
            Toast.makeText(this, "Note title can't be empty!", Toast.LENGTH_SHORT).show();
            return;
        }else if (inputNoteSubtitle.getText().toString().trim().isEmpty() && inputNoteText.getText().toString().trim().isEmpty()){
            Toast.makeText(this, "Note can't be empty!", Toast.LENGTH_SHORT).show();
            return;
        }

        //   Process for Saving Notes Start Here

        final Note note = new Note();
        note.setTitle(inputNoteTitle.getText().toString());
        note.setSubtitle(inputNoteSubtitle.getText().toString());
        note.setNoteText(inputNoteText.getText().toString());
        note.setDateTime(textDateTime.getText().toString());


        note.setColor(selectedNotecolor);

        //  for Saving Images
        note.setImagePath(selectedImagePath);


        //  here we have checked if layoutWebURL is visible or not
        // if it is visible means Web url is added else not added
        if (layoutWebUrl.getVisibility() == View.VISIBLE){
            note.setWebLink(textWebURL.getText().toString());
        }

        //

        if (alreadyAvailableNote != null ){
            note.setId(alreadyAvailableNote.getId());
        }

        //  Room doesn't allow database operation on the main thread that's why we are using async task to save note
        //  Also add this SuppressLint

        @SuppressLint("StaticFieldLeak")
        class SaveNoteTask extends AsyncTask<Void,Void,Void>{
            @Override
            protected Void doInBackground(Void... voids) {
                NotesDatabase.getDatabse(getApplicationContext()).noteDao().insertNote(note);
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);

                Intent intent = new Intent();
                setResult(RESULT_OK,intent);
                finish();
            }
        }

        new SaveNoteTask().execute();

    }


    private void initMiscellaneous(){
        final LinearLayout layoutMiscellaneous = findViewById(R.id.layoutMiscellaneous);
        final BottomSheetBehavior<LinearLayout> bottomSheetBehavior = BottomSheetBehavior.from(layoutMiscellaneous);
        layoutMiscellaneous.findViewById(R.id.textMiscellaneous).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (bottomSheetBehavior.getState() != BottomSheetBehavior.STATE_EXPANDED){
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                }else{
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                }
            }
        });

        final ImageView imageColor1 =layoutMiscellaneous.findViewById(R.id.imageColor1);
        final ImageView imageColor2 =layoutMiscellaneous.findViewById(R.id.imageColor2);
        final ImageView imageColor3 =layoutMiscellaneous.findViewById(R.id.imageColor3);
        final ImageView imageColor4 =layoutMiscellaneous.findViewById(R.id.imageColor4);
        final ImageView imageColor5=layoutMiscellaneous.findViewById(R.id.imageColor5);

        layoutMiscellaneous.findViewById(R.id.viewColor1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedNotecolor = "#333333";
                imageColor1.setImageResource(R.drawable.ic_done);
                imageColor2.setImageResource(0);
                imageColor3.setImageResource(0);
                imageColor4.setImageResource(0);
                imageColor5.setImageResource(0);
                setSubtitleIndicatorColor();
            }
        });

        layoutMiscellaneous.findViewById(R.id.viewColor2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedNotecolor = "#fdbe3b";
                imageColor1.setImageResource(0);
                imageColor2.setImageResource(R.drawable.ic_done);
                imageColor3.setImageResource(0);
                imageColor4.setImageResource(0);
                imageColor5.setImageResource(0);
                setSubtitleIndicatorColor();
            }
        });

        layoutMiscellaneous.findViewById(R.id.viewColor3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedNotecolor = "#ff4842";
                imageColor1.setImageResource(0);
                imageColor2.setImageResource(0);
                imageColor3.setImageResource(R.drawable.ic_done);
                imageColor4.setImageResource(0);
                imageColor5.setImageResource(0);
                setSubtitleIndicatorColor();
            }
        });

        layoutMiscellaneous.findViewById(R.id.viewColor4).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedNotecolor = "#3a52fc";
                imageColor1.setImageResource(0);
                imageColor2.setImageResource(0);
                imageColor3.setImageResource(0);
                imageColor4.setImageResource(R.drawable.ic_done);
                imageColor5.setImageResource(0);
                setSubtitleIndicatorColor();
            }
        });

        layoutMiscellaneous.findViewById(R.id.viewColor5).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedNotecolor = "#000000";
                imageColor1.setImageResource(0);
                imageColor2.setImageResource(0);
                imageColor3.setImageResource(0);
                imageColor4.setImageResource(0);
                imageColor5.setImageResource(R.drawable.ic_done);
                setSubtitleIndicatorColor();
            }
        });

        //   for updating we use this
        if (alreadyAvailableNote != null && alreadyAvailableNote.getColor() != null && !alreadyAvailableNote.getColor().trim().isEmpty()){

            switch (alreadyAvailableNote.getColor()){
                case "#fdbe3b":
                    layoutMiscellaneous.findViewById(R.id.viewColor2).performClick();
                    break;

                case "#ff4842":
                    layoutMiscellaneous.findViewById(R.id.viewColor3).performClick();
                    break;
                case "#3a52fc":
                    layoutMiscellaneous.findViewById(R.id.viewColor4).performClick();
                    break;
                case "#000000":
                    layoutMiscellaneous.findViewById(R.id.viewColor5).performClick();
                    break;


            }
        }


        layoutMiscellaneous.findViewById(R.id.layoutAddImage).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                // Request Run time Storage Permission
                if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){

                    ActivityCompat.requestPermissions(CreateNoteActivity.this,
                            new String[] {Manifest.permission.READ_EXTERNAL_STORAGE},REQUEST_CODE_STORAGE_PERMISSION);
                }
                else{
                    // Calling Image Function
                    selectImage();
                }
            }
        });

        //  for Adding and Dispaly Url
        layoutMiscellaneous.findViewById(R.id.layoutAddUrl).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                //  Calling the function for showing Url
                showAddURLDialog();
            }
        });


        //  For Displaying delete option and perform to delete
        if (alreadyAvailableNote != null){
            // Set visibility to visible here
            layoutMiscellaneous.findViewById(R.id.layoutDeleteNote).setVisibility(View.VISIBLE);
            layoutMiscellaneous.findViewById(R.id.layoutDeleteNote).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                    // Calling Show Delete Note Dialog function or method
                    showDeleteNoteDialog();

                }
            });
        }

    }

    //  Create and Showing Delete Note Dialog
    private void showDeleteNoteDialog(){
        if (dialogDeleteNote == null){
            AlertDialog.Builder builder = new AlertDialog.Builder(CreateNoteActivity.this);
            View view = LayoutInflater.from(this).inflate(R.layout.layout_delete_note,
                    (ViewGroup) findViewById(R.id.layoutDeleteNoteContainer));

            builder.setView(view);
            dialogDeleteNote = builder.create();
            if (dialogDeleteNote.getWindow() != null) {

                dialogDeleteNote.getWindow().setBackgroundDrawable(new ColorDrawable(0));
            }
            view.findViewById(R.id.textDeleteNote).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    @SuppressLint("StaticFieldLeak")
                    class DeleteNoteTask extends AsyncTask<Void,Void,Void>{

                        @Override
                        protected Void doInBackground(Void... voids) {
                            NotesDatabase.getDatabse(getApplicationContext()).noteDao()
                                    .deleteNote(alreadyAvailableNote);
                            return null;
                        }

                        @Override
                        protected void onPostExecute(Void aVoid) {
                            super.onPostExecute(aVoid);
                            Intent intent = new Intent();
                            intent.putExtra("isNotDeleted",true);
                            setResult(RESULT_OK,intent);
                            finish();
                        }
                    }
                  //  executing Delete Note method
                  new DeleteNoteTask().execute();
                }
            });

            view.findViewById(R.id.textCancel).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialogDeleteNote.dismiss();
                }
            });
        }

        dialogDeleteNote.show();
    }


    private void setSubtitleIndicatorColor(){
        GradientDrawable gradientDrawable = (GradientDrawable) viewSubtitleIndicator.getBackground();
        gradientDrawable.setColor(Color.parseColor(selectedNotecolor));
    }

    private void selectImage(){

        Intent intent = new Intent(Intent.ACTION_PICK , MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        if (intent.resolveActivity(getPackageManager())  != null){
            startActivityForResult(intent,REQUEST_CODE_SELECT_IMAGE);
        }
    }

    //  Handling Permission Result

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

    //  Handling Result for Selected Image

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_SELECT_IMAGE && resultCode == RESULT_OK){
            if (data != null){
                Uri selectImageUri = data.getData();
                if (selectImageUri != null){
                    try {
                        InputStream inputStream = getContentResolver().openInputStream(selectImageUri);
                        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                        imageNote.setImageBitmap(bitmap);
                        imageNote.setVisibility(View.VISIBLE);
                        // Image Delete Icon
                        findViewById(R.id.imageRemoveImage).setVisibility(View.VISIBLE);

                        // Calling getPathFromUri() function
                        selectedImagePath = getPathFromUri(selectImageUri);
                    }catch (Exception exception){
                        Toast.makeText(this, exception.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
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

    //  For Create or showing Url Dialog
    private void showAddURLDialog(){
        if (dialogAddURL == null){
            AlertDialog.Builder builder = new AlertDialog.Builder(CreateNoteActivity.this);
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
                        Toast.makeText(CreateNoteActivity.this, "Enter URL", Toast.LENGTH_SHORT).show();
                    }else if (!Patterns.WEB_URL.matcher(inputURL.getText().toString()).matches()){
                        Toast.makeText(CreateNoteActivity.this, "Enter Valid URL", Toast.LENGTH_SHORT).show();
                    }else {
                        textWebURL.setText(inputURL.getText().toString());
                        layoutWebUrl.setVisibility(View.VISIBLE);
                        dialogAddURL.dismiss();
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
}