package by.solutions.dumb.smartfoodassistant.activities;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuItem;

import by.solutions.dumb.smartfoodassistant.R;
import by.solutions.dumb.smartfoodassistant.fragments.ProductsFragment;
import by.solutions.dumb.smartfoodassistant.fragments.RecipesFragment;

public class MainActivity extends AppCompatActivity {

    //region Variables

    private FragmentManager fragmentManager;
    private ProductsFragment productsFragment;
    private RecipesFragment recipesFragment;
    private int currentPageId;

    private MenuItem searchItem;
    private ActionBar actionBar;

    //endregion


    //region Activity lifecycle

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        BottomNavigationView bottomNavigationView;

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNavigationView = findViewById(R.id.bottomNavigationBar);

        fragmentManager = getFragmentManager();
        productsFragment = new ProductsFragment();
        recipesFragment = new RecipesFragment();

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
                            currentPageId = R.id.navigation_recipes;
                            changeFragments(recipesFragment, productsFragment);
                            productsFragment.getAdapter().getFilter().filter("");
                            searchItem.collapseActionView();
                            actionBar.setTitle(R.string.title_recipes);
                        }
                        return true;
                    case R.id.navigation_products:
                        if (currentPageId != R.id.navigation_products) {
                            currentPageId = R.id.navigation_products;
                            changeFragments(productsFragment, recipesFragment);
                            recipesFragment.getAdapter().getFilter().filter("");
                            searchItem.collapseActionView();
                            actionBar.setTitle(R.string.title_products);
                        }
                        return true;
                }
                return false;
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        SearchView searchView;

        getMenuInflater().inflate(R.menu.main_activity_toolbar_actions, menu);
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
                    recipesFragment.getAdapter().getFilter().filter(newText);
                } else if (currentPageId == R.id.navigation_products) {
                    productsFragment.getAdapter().getFilter().filter(newText);
                }
                return false;
            }
        });

        return super.onCreateOptionsMenu(menu);
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

    private void changeFragments(Fragment fragment1, Fragment fragment2) {
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.show(fragment1);
        fragmentTransaction.hide(fragment2);
        fragmentTransaction.commit();
    }

    //endregion
}
