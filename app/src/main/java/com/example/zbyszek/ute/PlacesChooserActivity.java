package com.example.zbyszek.ute;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PlacesChooserActivity extends AppCompatActivity {

    private List<String> places;
    private List<String> placeTypes;
    private List<String> googlePlaces;
    private ArrayList<Integer> googlePlacesIndexesAdded;
    private boolean[] placesAdded;
    private Set<Integer> placesToAdd = new HashSet<>();
    private Context context = this;

    private ListView mListView;
    private ArrayAdapter<String> mAdapter;
    private EditText distanceText;
    private AlertDialog placesAdderWindow;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.place_chooser_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_places_chooser);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        placesAdded = new boolean[getResources().getStringArray(R.array.google_places).length];
//        placeTypes = new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.google_place_types)));
        places = new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.places)));
        googlePlaces = new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.google_places)));

        distanceText = (EditText) findViewById(R.id.distEditText);
        mListView = (ListView) findViewById(R.id.placesListView);
        mAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_multiple_choice, places) {
            @NonNull
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                if (position < getResources().getStringArray(R.array.places_keys).length){
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        Drawable background = getDrawable(R.drawable.dane_po_warszawsku);
                        background.setAlpha(100);
                        view.setBackground(background);
                    } else
                        view.setBackgroundColor(Color.LTGRAY);
                }
                return view;
            }
        };
        mListView.setAdapter(mAdapter);
        initGooglePlacesDefault();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                searchOnMap();
            }
        });
    }

    private void initGooglePlacesDefault() {
        googlePlacesIndexesAdded = new ArrayList<>();
        for (int i = getResources().getStringArray(R.array.places_keys).length; i < places.size(); i++) {
            googlePlacesIndexesAdded.add(googlePlaces.indexOf(mAdapter.getItem(i)));
//            String placeName = mAdapter.getItem(i);
//            int num = googlePlaces.indexOf(placeName);
            placesAdded[googlePlaces.indexOf(mAdapter.getItem(i))] = true;
        }
    }

    public void menuItemClick(MenuItem mu) {
        if (placesAdderWindow == null) {
            buildPlacesAdderWindow();
        }
        placesAdderWindow.show();
    }

    private void buildPlacesAdderWindow() {
        placesAdderWindow = new AlertDialog.Builder(this)
                .setTitle("Wybierz dodatkowe miejsca")
                .setMultiChoiceItems(R.array.google_places, placesAdded, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                        if (googlePlacesIndexesAdded.contains(which)) {
                            placesAdded[which] = true;
                        } else if (isChecked) {
                            placesToAdd.add(which);
                        } else if (placesToAdd.contains(which)) {
                            placesToAdd.remove(which);
                        }
                    }
                })
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        addPlaces();
                    }
                })
                .setNegativeButton("Wyjdź", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .create();
        placesAdderWindow.show();
    }

    private void addPlaces() {
        for (int i : placesToAdd) {
            mAdapter.add(getResources().getStringArray(R.array.google_places)[i]);
            mListView.setItemChecked(mListView.getCount() - 1, true);
//            places.add(getResources().getStringArray(R.array.google_places)[i]);
        }
        googlePlacesIndexesAdded.addAll(placesToAdd);
        placesToAdd.clear();
    }

    public void searchOnMap() {
        String distance = distanceText.getText().toString();
        if (distance.isEmpty()) {
            distance = distanceText.getHint().toString();
        } else if (!TextUtils.isDigitsOnly(distance)) {
            Toast.makeText(this,"Proszę wpisać liczbę",Toast.LENGTH_SHORT);
            return;
        }
        Bundle bundle = new Bundle();
        ArrayList<Integer> checkedDefaultPlaces = new ArrayList<>();
        ArrayList<Integer> checkedAddedPlaces = new ArrayList<>();
        int firstSize = getResources().getStringArray(R.array.places_keys).length;
        int lastSize = mListView.getCount();
        for (int i = 0; i < lastSize; i++) {
            if (mListView.isItemChecked(i)) {
                if (i < firstSize) {
                    checkedDefaultPlaces.add(i + 100);
                } else {
                    checkedAddedPlaces.add(googlePlacesIndexesAdded.get(i - firstSize));
                }
            }
        }
        bundle.putIntegerArrayList("UMWwaChecked", checkedDefaultPlaces);
        bundle.putIntegerArrayList("GoogleChecked", checkedAddedPlaces);
        bundle.putString("distance",distance);
        Intent intent = new Intent(PlacesChooserActivity.this, MapsActivity.class);
        intent.putExtras(bundle);
        startActivity(intent);

    }
}