package rs.elfak.jajac.geowarfare.activities;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import rs.elfak.jajac.geowarfare.R;
import rs.elfak.jajac.geowarfare.fragments.MapFragment;

public class MainActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(this);
        navigation.setSelectedItemId(R.id.navigation_world);
    }

    /* Try to find the fragment by tag first, if none exist, create
    * a new one using the cls argument. */
    private void replaceFragment(Fragment newFragment) {
        FragmentManager fManager = getSupportFragmentManager();
        fManager.beginTransaction()
                .replace(R.id.main_fragment_container, newFragment)
                .commit();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.navigation_world:
                replaceFragment(new MapFragment());
                return true;
            case R.id.navigation_profile:
                return true;
            case R.id.navigation_rankings:
                return true;
        }
        return false;
    }
}
