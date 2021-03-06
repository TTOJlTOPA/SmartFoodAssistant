package by.solutions.dumb.smartfoodassistant.activities;

import android.app.AlarmManager;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Locale;

import by.solutions.dumb.smartfoodassistant.R;
import by.solutions.dumb.smartfoodassistant.fragments.ProductsFragment;
import by.solutions.dumb.smartfoodassistant.fragments.RecipesFragment;
import by.solutions.dumb.smartfoodassistant.util.UpdatesReceiver;
import by.solutions.dumb.smartfoodassistant.util.filters.ProductsFilter;
import by.solutions.dumb.smartfoodassistant.util.filters.RecipesFilter;
import by.solutions.dumb.smartfoodassistant.util.sql.DatabasesManager;

public class MainActivity extends BaseActivity {

    //region Variables

    private static final String LOG_TAG = "MainActivity";
    private static final int RC_SIGN_IN = 1324;
    private static final String CURRENT_PAGE_BUNDLE_KEY = "CurrentPageKey";

    private FragmentManager fragmentManager;
    private ProductsFragment productsFragment;
    private RecipesFragment recipesFragment;
    private int currentPageId;

    private MenuItem searchItem;
    private MenuItem authItem;
    private ActionBar actionBar;
    private BottomNavigationView bottomNavigationView;

    private ProductsFilter productsFilter;
    private RecipesFilter recipesFilter;

    //endregion


    //region Activity lifecycle

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNavigationView = findViewById(R.id.bottomNavigationBar);

        DatabasesManager.changeLanguageWithVersion(this, Locale.getDefault().getLanguage(), 1);
        fragmentManager = getFragmentManager();
        productsFragment = new ProductsFragment();
        recipesFragment = new RecipesFragment();
        productsFilter = new ProductsFilter();
        recipesFilter = new RecipesFilter();

        currentPageId = R.id.navigation_recipes;
        addFragment(R.id.main_fragment_container, recipesFragment);
        addFragment(R.id.main_fragment_container, productsFragment);
        hideFragment(productsFragment);
        actionBar = getSupportActionBar();
        actionBar.setTitle(R.string.title_recipes);

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.navigation_recipes:
                        if (currentPageId != R.id.navigation_recipes) {
                            Log.d(LOG_TAG, "Recipes bottom button clicked");
                            currentPageId = R.id.navigation_recipes;
                            showFragmentById(currentPageId);
                            Log.d(LOG_TAG, "Recipes fragment showed");
                            searchItem.collapseActionView();
                        }
                        return true;
                    case R.id.navigation_products:
                        if (currentPageId != R.id.navigation_products) {
                            Log.d(LOG_TAG, "Products bottom button clicked");
                            currentPageId = R.id.navigation_products;
                            showFragmentById(currentPageId);
                            Log.d(LOG_TAG, "Products fragment showed");
                            searchItem.collapseActionView();
                        }
                        return true;
                }
                return false;
            }
        });
        startUpdatesChecker();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        SearchView searchView;
        MenuInflater menuInflater = getMenuInflater();

        menuInflater.inflate(R.menu.main_activity_toolbar_actions, menu);
        searchItem = menu.findItem(R.id.action_search);
        searchView = (SearchView) searchItem.getActionView();
        searchView.setMaxWidth(Integer.MAX_VALUE);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (currentPageId == R.id.navigation_recipes) {
                    recipesFilter.name = newText;
                    recipesFragment.filter(recipesFilter);
                } else if (currentPageId == R.id.navigation_products) {
                    productsFilter.name = newText;
                    productsFragment.filter(productsFilter);
                }
                return false;
            }
        });

        authItem = menu.findItem(R.id.action_user);
        updateUser();
        authItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                startActivityForResult(new Intent(MainActivity.this, SignInActivity.class),
                        RC_SIGN_IN);
                return true;
            }
        });

        menuInflater.inflate(R.menu.main_menu, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RC_SIGN_IN) {
            if (resultCode == RESULT_OK) {
                updateUser();
            } else {
                Log.d(LOG_TAG, "SignInActivity incorrect result");
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt(CURRENT_PAGE_BUNDLE_KEY, currentPageId);
        Log.d(LOG_TAG, "Save activity state");
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        currentPageId = savedInstanceState.getInt(CURRENT_PAGE_BUNDLE_KEY, R.id.navigation_recipes);
        showFragmentById(currentPageId);
        Log.d(LOG_TAG, "Restore Activity State");
    }

    //endregion


    //region Private methods

    private void addFragment(int containerId, Fragment fragment) {
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(containerId, fragment);
        fragmentTransaction.commit();
    }

    private void hideFragment(Fragment fragment) {
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.hide(fragment);
        fragmentTransaction.commit();
    }

    private void showFragmentById(int fragmentId) {
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        switch (fragmentId) {
            case R.id.navigation_recipes:
                fragmentTransaction.show(recipesFragment);
                fragmentTransaction.hide(productsFragment);
                productsFragment.resetFilter();
                actionBar.setTitle(R.string.title_recipes);
                break;
            case R.id.navigation_products:
                fragmentTransaction.hide(recipesFragment);
                fragmentTransaction.show(productsFragment);
                recipesFragment.resetFilter();
                actionBar.setTitle(R.string.title_products);
                break;
        }
        fragmentTransaction.commit();
    }

    private void updateUser() {
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        if (firebaseUser != null) {
            authItem.setIcon(R.drawable.ic_user_sign_in_white_24dp);
        } else {
            authItem.setIcon(R.drawable.ic_user_sign_out_white_24dp);
        }
    }

    private void startUpdatesChecker() {
        AlarmManager manager = (AlarmManager) getSystemService(ALARM_SERVICE);
        Intent intent = new Intent(this, UpdatesReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, 0);
        manager.setRepeating(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime(), 60000, pendingIntent);
    }

    //endregion

}
