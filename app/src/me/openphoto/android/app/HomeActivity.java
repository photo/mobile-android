
package me.openphoto.android.app;

import java.util.ArrayList;
import java.util.Arrays;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

/**
 * The home activity - screen
 * 
 * @author Patrick Boos
 */
public class HomeActivity extends Activity {
    public static final String TAG = HomeActivity.class.getSimpleName();

    private ListView mainListView;
    private ArrayAdapter<String> listAdapter;

    /**
     * Called when Home Activity is first loaded
     * 
     * @see android.app.Activity#onCreate(android.os.Bundle)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Find the ListView resource.
        mainListView = (ListView) findViewById(R.id.grid_newest_photos);

        // Create and populate a List of planet names.
        String[] planets = new String[] {
                "Mercury", "Venus", "Earth", "Mars",
                "Jupiter", "Saturn", "Uranus", "Neptune"
        };
        ArrayList<String> planetList = new ArrayList<String>();
        planetList.addAll(Arrays.asList(planets));

        // Create ArrayAdapter using the planet list.
        listAdapter = new ArrayAdapter<String>(this, R.layout.activity_home_newest_photos,
                planetList);

        // Add more planets. If you passed a String[] instead of a List<String>
        // into the ArrayAdapter constructor, you must not add more items.
        // Otherwise an exception will occur.
        listAdapter.add("Ceres");
        listAdapter.add("Pluto");
        listAdapter.add("Haumea");
        listAdapter.add("Makemake");
        listAdapter.add("Eris");

        // Set the ArrayAdapter as the ListView's adapter.
        mainListView.setAdapter(listAdapter);
    }
}
