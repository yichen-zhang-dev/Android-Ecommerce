package com.example.ecommerce;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.media.Image;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.squareup.picasso.Picasso;


public class Favorite_Movies extends AppCompatActivity {

    private static FirebaseFirestore db;

    private FirestoreRecyclerAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorite_movies);

        db = FirebaseFirestore.getInstance();

        configureAdapter();

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.fav_recycler_view);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
    }

    @Override
    protected void onStart() {
        super.onStart();
        adapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening();
    }

    private void configureAdapter() {
        Query query = db.collection("favorite_movies").orderBy("title");
        // Configure RecylcerView adapter
        FirestoreRecyclerOptions<FavoriteMovies> options = new FirestoreRecyclerOptions.Builder<FavoriteMovies>()
                .setQuery(query, FavoriteMovies.class)
                .build();

        adapter = new FirestoreRecyclerAdapter<FavoriteMovies, FavoriteMoviesHolder>(options) {
            @Override
            public FavoriteMoviesHolder onCreateViewHolder(ViewGroup group, int i) {
                View view = LayoutInflater.from(group.getContext())
                        .inflate(R.layout.favorite_movie_item, group, false);
                return new FavoriteMoviesHolder(view);
            }

            @Override
            public void onBindViewHolder(FavoriteMoviesHolder movieHolder, int position, FavoriteMovies movie) {
                movieHolder.getTitleView().setText(movie.getTitle());
                movieHolder.getYearView().setText("\t(" + movie.getYear() + ")");
            }
        };
    }
}

class FavoriteMoviesHolder extends RecyclerView.ViewHolder {

    private final TextView titleView;
    private final TextView yearView;
    private final ImageButton removeButton;
    private static FirebaseFirestore db;

    private final String REMOVE_FAV = "Remove a fav";

    public FavoriteMoviesHolder(View view) {
        super(view);

        db = FirebaseFirestore.getInstance();

        titleView = (TextView) view.findViewById(R.id.fav_movie_title);
        yearView = (TextView) view.findViewById(R.id.fav_movie_year);
        removeButton = (ImageButton) view.findViewById(R.id.remove_fav_button);

        removeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                db.collection("favorite_movies").document((String) titleView.getText())
                        .delete()
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                Log.d(REMOVE_FAV, "Successful!");
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.w(REMOVE_FAV, "Error removing a favorite movie!", e);
                            }
                        });
            }
        });

    }

    public TextView getTitleView() {
        return titleView;
    }

    public TextView getYearView() {
        return yearView;
    }

    public ImageButton getRemoveButton() {
        return removeButton;
    }
}

class FavoriteMovies {

    private String title;
    private String year;

    public FavoriteMovies() { }

    public String getTitle() { return title; }

    public String getYear() { return year; }
}