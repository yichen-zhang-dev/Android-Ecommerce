package com.example.ecommerce;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Text;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

import com.example.ecommerce.Movie;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "Firebase Tutorial";
    private static final String ECOMMERCE_TAG = "Ecommerce";
    private static final String MOVIE_TAG = "IMDb Movies";
    private static FirebaseFirestore db;

    private static final String HOST_KEY = "x-rapidapi-host";
    private static final String HOST = "imdb8.p.rapidapi.com";
    private static final String API_KEY = "x-rapidapi-key";
    private static final String API_TOKEN = "caad3fa983msh669d994564f9bf7p1eacf8jsn22fa56a73701";

    private static final String GET_MOST_POPULAR_MOVIES = "https://imdb8.p.rapidapi.com/title/get-most-popular-movies?";
    private static final String GET_MOVIE_DETAILS = "https://imdb8.p.rapidapi.com/title/get-details?tconst=";

    private FirestoreRecyclerAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d(ECOMMERCE_TAG, "======================Firing up Firebase======================");
        db = FirebaseFirestore.getInstance();
//        firebaseExample();
//        populateCities();
//        IMDbAPI();
        db.collection("movies").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                Log.d(MOVIE_TAG, "Number of movies in Firebase: " + task.getResult().size());
            }
        });

        configureAdapter();

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.main_recycler_view);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);

        // Start Cart activity
        final ImageButton cart_button = (ImageButton) findViewById(R.id.cart_button);
        cart_button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, Cart.class);
                startActivity(intent);
            }
        });
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
        Query query = db.collection("movies").whereGreaterThan("year", 2000);
        // Configure RecylcerView adapter
        FirestoreRecyclerOptions<Movie> options = new FirestoreRecyclerOptions.Builder<Movie>()
                .setQuery(query, Movie.class)
                .build();

        adapter = new FirestoreRecyclerAdapter<Movie, MovieHolder>(options) {
            @Override
            public MovieHolder onCreateViewHolder(ViewGroup group, int i) {
                View view = LayoutInflater.from(group.getContext())
                        .inflate(R.layout.movie_item, group, false);
                return new MovieHolder(view);
            }

            @Override
            public void onBindViewHolder(MovieHolder movieHolder, int position, Movie movie) {
                movieHolder.getTitleView().setText(movie.getTitle());
                movieHolder.getYearView().setText("\t(" + movie.getYear() + ")");
//                int imageID =
//                movieHolder.getImageView().set;
            }
        };
    }

    private void IMDbAPI() {
        OkHttpClient client = new OkHttpClient();
        // Get tconsts of the 100 most popular movies
        Request request = new Request.Builder()
                .url(GET_MOST_POPULAR_MOVIES + "homeCountry=US&purchaseCountry=US&currentCountry=US")
                .get()
                .addHeader(HOST_KEY, HOST)
                .addHeader(API_KEY, API_TOKEN)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    ArrayList<String> movieTCONSTS = new ArrayList<>();
                    String popularMovieTCONSTs = response.body().string();

                    Pattern tconstPattern = Pattern.compile("/title/tt[0-9]{7}");
                    Matcher tconstMatcher = tconstPattern.matcher(popularMovieTCONSTs);
                    while (tconstMatcher.find())  {
                        movieTCONSTS.add(tconstMatcher.group(0));
                    }
                    assert(movieTCONSTS.size() == 100);
                    response.close();

                    for (int i = 0; i < movieTCONSTS.size(); i++) {
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        Log.d(MOVIE_TAG, "LOOP: \t\t\t\t" + i + "\t\t" + movieTCONSTS.get(i));
                        // Get details of a single movie
                        Request movieDetailsRequest = new Request.Builder()
                                .url(GET_MOVIE_DETAILS + movieTCONSTS.get(i).substring(7))
                                .get()
                                .addHeader(HOST_KEY, HOST)
                                .addHeader(API_KEY, API_TOKEN)
                                .build();

                        OkHttpClient client = new OkHttpClient();
                        client.newCall(movieDetailsRequest).enqueue(new Callback() {
                            @Override
                            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                                // e.printStackTrace();
                                Log.w(MOVIE_TAG, "\t\t\tGet details error!");
                            }

                            @Override
                            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                                if (response.isSuccessful()) {
                                    String movieDetails = response.body().string();
//                                Log.d(MOVIE_TAG, movieDetails);
                                    try {
                                        JSONObject detail = new JSONObject(movieDetails);
                                        Log.d(MOVIE_TAG, "\t\t" + detail.get("title"));
                                        JSONObject image = null;
                                        try {
                                            image = new JSONObject(detail.get("image").toString());
                                            Map<String, Object> movieRecord = new HashMap<>();
                                            String[] keys = new String[] {"id", "title", "titleType", "year"};
                                            for (String key : keys) {
                                                movieRecord.put(key, detail.get(key));
                                            }
                                            movieRecord.put("imageURL", image.get("url"));
                                            String title = (String) detail.get("title");

                                            DocumentReference movieReference = db.collection("movies").document((String) detail.get("title"));
                                            movieReference.set(movieRecord)
                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void unused) {
                                                            Log.d(MOVIE_TAG, "DocumentSnapshot successfully written: " + title);
                                                        }
                                                    })
                                                    .addOnFailureListener(new OnFailureListener() {
                                                        @Override
                                                        public void onFailure(@NonNull Exception e) {
                                                            Log.w(MOVIE_TAG, "Error adding movie", e);
                                                        }
                                                    });
                                        } catch (Exception e) {
                                            Log.w(MOVIE_TAG, "There is no field: image.");
                                        } finally {
                                            response.close();
                                        }
                                    } catch (JSONException e) {
                                        Log.d(MOVIE_TAG, "SOMETHING IS WRONG!");
                                    } finally {
                                        response.close();
                                    }

                                }
                            }
                        });
                    }
                }

            }
        });
        Log.d(TAG, "======================Completed populating movies======================");
    }

    private void populateCities() {
        CollectionReference cities = db.collection("cities");

        Map<String, Object> data1 = new HashMap<>();
        data1.put("name", "San Francisco");
        data1.put("state", "CA");
        data1.put("country", "USA");
        data1.put("capital", false);
        data1.put("population", 860000);
        data1.put("regions", Arrays.asList("west_coast", "norcal"));
        cities.document("SF").set(data1);

        Map<String, Object> data2 = new HashMap<>();
        data2.put("name", "Los Angeles");
        data2.put("state", "CA");
        data2.put("country", "USA");
        data2.put("capital", false);
        data2.put("population", 3900000);
        data2.put("regions", Arrays.asList("west_coast", "socal"));
        cities.document("LA").set(data2);

        Map<String, Object> data3 = new HashMap<>();
        data3.put("name", "Washington D.C.");
        data3.put("state", null);
        data3.put("country", "USA");
        data3.put("capital", true);
        data3.put("population", 680000);
        data3.put("regions", Arrays.asList("east_coast"));
        cities.document("DC").set(data3);

        Map<String, Object> data4 = new HashMap<>();
        data4.put("name", "Tokyo");
        data4.put("state", null);
        data4.put("country", "Japan");
        data4.put("capital", true);
        data4.put("population", 9000000);
        data4.put("regions", Arrays.asList("kanto", "honshu"));
        cities.document("TOK").set(data4);

        Map<String, Object> data5 = new HashMap<>();
        data5.put("name", "Beijing");
        data5.put("state", null);
        data5.put("country", "China");
        data5.put("capital", true);
        data5.put("population", 21500000);
        data5.put("regions", Arrays.asList("jingjinji", "hebei"));
        cities.document("BJ").set(data5);

        Log.d(TAG, "======================Completed populating cities======================");
    }

    private void firebaseExample() {
        // Create a new user with a first and last name
        Map<String, Object> user = new HashMap<>();
        user.put("first", "Yichen");
        user.put("last", "Zhang");
        user.put("born", 1999);

        DocumentReference userYichen = db.collection("users").document("Yichen");

        // Add a new document with a generated ID
//        db.collection("users")
//                .add(user)
        userYichen.set(user)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Log.d(TAG, "DocumentSnapshot successfully written!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error adding document", e);
                    }
                });
    }
}

class MovieHolder extends RecyclerView.ViewHolder {

    private final TextView titleView;
    private final TextView yearView;
//    private final ImageView imageView;

    public MovieHolder(View view) {
        super(view);
        titleView = (TextView) view.findViewById(R.id.movie_title);
        yearView = (TextView) view.findViewById(R.id.movie_year);
//        imageView = (ImageView) view.findViewById(R.id.movie_image);
    }

    public TextView getTitleView() { return titleView; }

    public TextView getYearView() { return yearView; }

//    public ImageView getImageView() { return imageView; }
}



