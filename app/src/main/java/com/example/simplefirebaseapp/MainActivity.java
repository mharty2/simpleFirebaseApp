package com.example.simplefirebaseapp;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.api.Distribution;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

public class MainActivity extends AppCompatActivity {
    private final static String TAG = "MainActivity";

    private final static String KEY_TITLE = "title";
    private final static String KEY_DESCRIPTION = "description";

    private EditText editTextTitle;
    private EditText editTextDescription;
    private EditText editTextPriority;
    private TextView textViewData;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference collectionReference = db.collection("Notebook");
    private DocumentReference documentReference = db.collection("Notebook").document("My First Note");
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editTextTitle = findViewById(R.id.edit_text_title);
        editTextDescription = findViewById(R.id.edit_text_description);
        editTextPriority = findViewById(R.id.edit_text_priority);
        textViewData = findViewById(R.id.text_view_data);
    }

    public void saveNote(View v) {
        String title = editTextTitle.getText().toString();
        String description = editTextDescription.getText().toString();

        /**
        Map<String, Object> note = new HashMap<>();
        note.put(KEY_TITLE, title);
        note.put(KEY_DESCRIPTION, description);
         */
        Note note = new Note(title, description);
        documentReference.set(note)
        .addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(MainActivity.this, "note saved", Toast.LENGTH_LONG).show();
            }
        })
        .addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(MainActivity.this, "unable to save", Toast.LENGTH_LONG).show();
                Log.d(TAG, e.getMessage());
            }
        });
    }

    public void addNote(View v) {
        String title = editTextTitle.getText().toString();
        String description = editTextDescription.getText().toString();

        if(editTextPriority.length() == 0) {
            editTextPriority.setText("0");
        }

        int priority = Integer.parseInt(editTextPriority.getText().toString());
        Note note = new Note(title, description, priority);
        collectionReference.document(note.getTitle()).set(note);
    }

    @Override
    protected void onStart() {
        super.onStart();
        collectionReference.addSnapshotListener(this, new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    return;
                }
                String data = "";
                for (QueryDocumentSnapshot documentSnapshot: queryDocumentSnapshots) {
                    Note note = documentSnapshot.toObject(Note.class);
                    note.setId(documentSnapshot.getId());

                    String description = note.getDescription();
                    String title = note.getTitle();
                    int priority = note.getPriority();

                    data += "Title: " + title + "\nDescription: " + description + "\nPriority: "+ priority+ "\n\n";
                    textViewData.setText(data);
                }
            }
        });
        documentReference.addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Toast.makeText(MainActivity.this, "error while loading", Toast.LENGTH_LONG).show();
                    Log.d(TAG,e.toString());
                    return;
                }
                if (documentSnapshot.exists()) {
                    /**Map<String, Object> note = documentSnapshot.getData();
                    textViewData.setText("Title: " + note.get(KEY_TITLE) + "\n" + "Description: " + note.get(KEY_DESCRIPTION));
                     */
                    Note note = documentSnapshot.toObject(Note.class);

                    String title = note.getTitle();
                    String description = note.getDescription();

                    textViewData.setText("Title: " + title + "\n" + "Description: " + description);

                } else {
                    textViewData.setText("");
                }
            }
        });
    }
    /**
    public void updateDescription (View v) {
        String description = editTextDescription.getText().toString();
        //Map<String, Object> note = new HashMap<>();
        //note.put(KEY_DESCRIPTION, description);

        //documentReference.set(note, SetOptions.merge());
        //documentReference.update(note);
        documentReference.update(KEY_DESCRIPTION, description);
    }

    public void deleteNote(View v) {
        documentReference.delete();
    }

    public void deleteDescription(View v) {
        //Map<String, Object> note = new HashMap<>();
        //note.put(KEY_DESCRIPTION, FieldValue.delete());
        documentReference.update(KEY_DESCRIPTION, FieldValue.delete());
    }
     */
    public void loadNote(View v) {
        documentReference.get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            //String title = documentSnapshot.getString(KEY_TITLE);
                            //String description = documentSnapshot.getString(KEY_DESCRIPTION);

                            //other way:
                            //Map<String, Object> note = documentSnapshot.getData();
                            //textViewData.setText("Title: " + note.get(KEY_TITLE) + "\n" + "Description: " + note.get(KEY_DESCRIPTION));
                            Note note = documentSnapshot.toObject(Note.class);

                            String title = note.getTitle();
                            String description = note.getDescription();


                            textViewData.setText("Title: " + title + "\n" + "Description: " + description);

                        } else {
                            Toast.makeText(MainActivity.this, "document doesn't exist", Toast.LENGTH_LONG).show();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(MainActivity.this, "unable to save", Toast.LENGTH_LONG).show();
                        Log.d(TAG, e.getMessage());
                    }
                });
    }
    public void loadNotes(View v) {
        collectionReference.whereGreaterThanOrEqualTo("priority", 0)
                .orderBy("priority", Query.Direction.DESCENDING)
                .limit(3)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        String data = "";
                        for (QueryDocumentSnapshot documentSnapshot: queryDocumentSnapshots) {
                            Note note = documentSnapshot.toObject(Note.class);
                            note.setId(documentSnapshot.getId());

                            String description = note.getDescription();
                            String title = note.getTitle();
                            int priority = note.getPriority();

                            data += "Title: " + title + "\nDescription: " + description + "\nPriority: "+ priority+ "\n\n";
                            textViewData.setText(data);
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });
    }
}
