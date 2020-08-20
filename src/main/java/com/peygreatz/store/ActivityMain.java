package com.peygreatz.store;

import android.app.Dialog;
import android.content.ClipData;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;

import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.core.widget.NestedScrollView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.appcompat.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.peygreatz.store.data.AppConfig;
import com.peygreatz.store.data.DatabaseHandler;
import com.peygreatz.store.data.SharedPref;
import com.peygreatz.store.fragment.FragmentCategory;
import com.peygreatz.store.fragment.FragmentFeaturedNews;
import com.peygreatz.store.utils.CallbackDialog;
import com.peygreatz.store.utils.DialogUtils;
import com.peygreatz.store.utils.Tools;


import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.firebase.client.annotations.Nullable;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Arrays;
import java.util.List;

import bolts.Task;

public class ActivityMain extends AppCompatActivity {

    private ActionBar actionBar;
    private Toolbar toolbar;
    private FloatingActionButton fab,fab2;
    private CardView search_bar;
    private SwipeRefreshLayout swipe_refresh;
    private View parent_view;
    private NavigationView nav_view;
    private DatabaseHandler db;
    private SharedPref sharedPref;
    private Dialog dialog_failed = null;
    public boolean category_load = false, news_load = false;
    static ActivityMain activityMain;
    private FirebaseAuth auth;
    public static ActivityMain getInstance() {
        return activityMain;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        activityMain = this;
        db = new DatabaseHandler(this);
        sharedPref = new SharedPref(this);

        initToolbar();
        initDrawerMenu();
        initComponent();
        initFragment();
        swipeProgress(true);
        auth = FirebaseAuth.getInstance();

        //get current user
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();





        ImageButton buttonOne = findViewById(R.id.buttonOne);
        buttonOne.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                System.out.println("Button Clicked");

                Intent ArActivityIntent = new Intent(getApplicationContext(), MainChat.class);
                startActivity(ArActivityIntent);
            }
        });

        // launch instruction when first launch
        if (sharedPref.isFirstLaunch()) {
            startActivity(new Intent(this, ActivityInstruction.class));
            sharedPref.setFirstLaunch(false);
        }
    }

    private void initToolbar() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);
        actionBar.setTitle(R.string.app_name);
    }

    private void initDrawerMenu() {
        nav_view = (NavigationView) findViewById(R.id.nav_view);
        final DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close) {
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
            }
        };
        drawer.setDrawerListener(toggle);
        toggle.syncState();
        nav_view.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(final MenuItem item) {
                onItemSelected(item.getItemId());
                //drawer.closeDrawers();
                return true;
            }
        });
        nav_view.setItemIconTintList(getResources().getColorStateList(R.color.nav_state_list));
    }

    private void initFragment() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        // init fragment slider new product
        FragmentFeaturedNews fragmentFeaturedNews = new FragmentFeaturedNews();
        fragmentTransaction.replace(R.id.frame_content_new_product, fragmentFeaturedNews);
        // init fragment category
        FragmentCategory fragmentCategory = new FragmentCategory();
        fragmentTransaction.replace(R.id.frame_content_category, fragmentCategory);

        fragmentTransaction.commit();
    }

    private void initComponent() {
        parent_view = findViewById(R.id.parent_view);
        search_bar = (CardView) findViewById(R.id.search_bar);
        swipe_refresh = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);
        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab2 = (FloatingActionButton) findViewById(R.id.fab2);
        NestedScrollView nested_content = (NestedScrollView) findViewById(R.id.nested_content);
        nested_content.setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() {
            @Override
            public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                if (scrollY < oldScrollY) { // up
                    animateFab(false);
                    animateSearchBar(false);
                }
                if (scrollY > oldScrollY) { // down
                    animateFab(true);
                    animateSearchBar(true);
                }
            }
        });
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(), ActivityShoppingCart.class);
                startActivity(i);
            }
        });
        fab2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(), MainMap.class);
                startActivity(i);
            }
        });

        // on swipe list
        swipe_refresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshFragment();
            }
        });

        ((ImageButton) findViewById(R.id.action_search)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ActivitySearch.navigate(ActivityMain.this);
            }
        });
    }

    private void refreshFragment() {
        category_load = false;
        news_load = false;
        swipeProgress(true);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                initFragment();
            }
        }, 500);
    }

    private void swipeProgress(final boolean show) {
        if (!show) {
            swipe_refresh.setRefreshing(show);
            return;
        }
        swipe_refresh.post(new Runnable() {
            @Override
            public void run() {
                swipe_refresh.setRefreshing(show);
            }
        });
    }

    public boolean onItemSelected(int id) {
        Intent i;
        switch (id) {
            //sub menu
            case R.id.nav_cart:
                i = new Intent(this, ActivityShoppingCart.class);
                startActivity(i);
                break;
            case R.id.nav_wish:
                i = new Intent(this, ActivityWishlist.class);
                startActivity(i);
                break;
            case R.id.nav_history:
                i = new Intent(this, ActivityOrderHistory.class);
                startActivity(i);
                break;


            case R.id.nav_notif:
                i = new Intent(this, ActivityNotification.class);
                startActivity(i);
                break;
            case R.id.nav_setting:
                i = new Intent(this, ActivitySettings.class);
                startActivity(i);
                break;
            case R.id.nav_instruction:
                i = new Intent(this, ActivityInstruction.class);
                startActivity(i);
                break;
            case R.id.nav_rate:
                Tools.rateAction(this);
                break;
            case R.id.nav_about:
                Tools.showDialogAbout(this);
                break;
            case R.id.profile:
                i = new Intent(this, ActivityProfile.class);
                startActivity(i);
                break;
            case R.id.account:
                i = new Intent(this, activity_profile.class);
                startActivity(i);
                break;
            case R.id.sign_out:

                i = new Intent(this, LoginActivity.class);
                startActivity(i);


                        signOut();





        //sign out method






                break;
            default:
                break;
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawers();
        return true;
    }



    boolean isFabHide = false;

    private void animateFab(final boolean hide) {
        if (isFabHide && hide || !isFabHide && !hide) return;
        isFabHide = hide;
        int moveY = hide ? (2 * fab.getHeight()) : 0;
        fab.animate().translationY(moveY).setStartDelay(100).setDuration(300).start();
    }

    boolean isSearchBarHide = false;

    private void animateSearchBar(final boolean hide) {
        if (isSearchBarHide && hide || !isSearchBarHide && !hide) return;
        isSearchBarHide = hide;
        int moveY = hide ? -(2 * search_bar.getHeight()) : 0;
        search_bar.animate().translationY(moveY).setStartDelay(100).setDuration(300).start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateNavCounter(nav_view);
    }

    static boolean active = false;

    @Override
    public void onStart() {
        super.onStart();
        active = true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        active = false;
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (!drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.openDrawer(GravityCompat.START);
        } else {
            doExitApp();
        }
    }

    private long exitTime = 0;
    public void doExitApp() {
        if ((System.currentTimeMillis() - exitTime) > 2000) {
            Toast.makeText(this, R.string.press_again_exit_app, Toast.LENGTH_SHORT).show();
            exitTime = System.currentTimeMillis();
        } else {
            finish();
        }
    }

    public void showDataLoaded() {
        if (category_load && news_load) {
            swipeProgress(false);
            //Snackbar.make(parent_view, R.string.msg_data_loaded, Snackbar.LENGTH_SHORT).show();
        }
    }

    public void showDialogFailed(@StringRes int msg) {
        if (dialog_failed != null && dialog_failed.isShowing()) return;
        swipeProgress(false);
        dialog_failed = new DialogUtils(this).buildDialogWarning(-1, msg, R.string.TRY_AGAIN, R.drawable.img_no_connect, new CallbackDialog() {
            @Override
            public void onPositiveClick(Dialog dialog) {
                dialog.dismiss();
                refreshFragment();
            }

            @Override
            public void onNegativeClick(Dialog dialog) {
            }
        });
        dialog_failed.show();
    }




    /* show ads */


    private void updateNavCounter(NavigationView nav) {
        Menu menu = nav.getMenu();
        // update cart counter
        int cart_count = db.getActiveCartSize();
        ((TextView) menu.findItem(R.id.nav_cart).getActionView().findViewById(R.id.counter)).setText(String.valueOf(cart_count));

        // update wishlist counter
        int wishlist_count = db.getWishlistSize();
        ((TextView) menu.findItem(R.id.nav_wish).getActionView().findViewById(R.id.counter)).setText(String.valueOf(wishlist_count));

        // update notification counter
        int notif_count = db.getUnreadNotificationSize();
        View dot_sign = (View) menu.findItem(R.id.nav_notif).getActionView().findViewById(R.id.dot);
        if (notif_count > 0) {
            dot_sign.setVisibility(View.VISIBLE);
        } else {
            dot_sign.setVisibility(View.GONE);
        }

    }
    public void signOut() {
        auth.signOut();
    }

}
