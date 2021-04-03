package com.aap.scoial;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.SearchView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.List;

public class ViewActivity extends AppCompatActivity {
    RecyclerView recyclerView;
    AdapterPost adapter;
    DatabaseReference dataRef;
    FirebaseStorage mstoarage;
    FirebaseDatabase mdatabse;
    SearchView searchView;
    String last_key;
    Boolean isMixdata = false, isLoading = false;
    int ITEM_LOAD_COUNT;

    private ArrayList<uploadPost> userList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view);
        searchView = findViewById(R.id.searchView);

        getLastKey();

        dataRef =mdatabse.getInstance().getReference().child("Posts");
        dataRef.keepSynced(true);
        mstoarage = FirebaseStorage.getInstance();
        recyclerView = (RecyclerView)findViewById(R.id.recyclerPost);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
//        recyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {
//            @Override
//            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
//                super.onScrollStateChanged(recyclerView, newState);
//            }
//
//            @Override
//            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
//                super.onScrolled(recyclerView, dx, dy);
//            }
//        });



    }

    private void getLastKey() {

        Query query = FirebaseDatabase.getInstance().getReference().child("Posts")
                .orderByKey()
                .limitToLast(1);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot lastKey : dataSnapshot.getChildren()){
                    last_key = lastKey.getKey();

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(ViewActivity.this, "Last Item", Toast.LENGTH_SHORT).show();

            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        userList = new ArrayList<>();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
        if (ref != null) {
            ref.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                        uploadPost model = ds.getValue(uploadPost.class);

                        userList.add(model);

                    }
                    adapter = new AdapterPost(ViewActivity.this, userList);
                    recyclerView.setAdapter(adapter);
//                    getPost();
                    recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                        @Override
                        public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                            super.onScrollStateChanged(recyclerView, newState);
                        }

                        @Override
                        public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                            super.onScrolled(recyclerView, dx, dy);
                        }
                    });

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

        }

        if (searchView != null){
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    return false;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    search(newText);
                    return true;
                }
            });
        }

    }

    private void getPost() {
        if (!isMixdata){
            Query query;
            if (TextUtils.isEmpty(last_key)){
                query = FirebaseDatabase.getInstance().getReference("Posts")
                        .orderByKey()
                        .limitToLast(ITEM_LOAD_COUNT);
            }else {
                query = FirebaseDatabase.getInstance().getReference("Posts")
                        .orderByKey()
                        .startAt(last_key)
            .limitToFirst(ITEM_LOAD_COUNT);
            }

            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.hasChildren()){
                        ArrayList<uploadPost> newUser = new ArrayList<>();
                        for (DataSnapshot userSnapshot : dataSnapshot.getChildren()){
                            newUser.add(userSnapshot.getValue(uploadPost.class));
                        }
                        last_key = String.valueOf(newUser.get((newUser.size()-1)));
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }

    }

    private void search(String str) {
        ArrayList<uploadPost> searchList = new ArrayList<>();

        for (uploadPost obj : userList){
            if (obj.getName().toLowerCase().contains(str.toLowerCase())){
                searchList.add(obj);
            }
        }
        adapter = new AdapterPost(ViewActivity.this, searchList);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));

    }


//    @Override
//    protected void onStop() {
//        super.onStop();
//        adapter.stopListening();
//    }
}