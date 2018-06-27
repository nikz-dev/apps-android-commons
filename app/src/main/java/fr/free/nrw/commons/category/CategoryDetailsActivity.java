package fr.free.nrw.commons.category;

import android.content.Context;
import android.content.Intent;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;

import butterknife.ButterKnife;
import fr.free.nrw.commons.Media;
import fr.free.nrw.commons.PageTitle;
import fr.free.nrw.commons.R;
import fr.free.nrw.commons.auth.AuthenticatedActivity;
import fr.free.nrw.commons.media.MediaDetailPagerFragment;

import static android.widget.Toast.LENGTH_SHORT;

/**
 * This activity displays pictures of a particular category
 * Its generic and simply takes the name of category name in its start intent to load all images in
 * a particular category. This activity is currently being used to display a list of featured images,
 * which is nothing but another category on wikimedia commons.
 */

public class CategoryDetailsActivity
        extends AuthenticatedActivity
        implements FragmentManager.OnBackStackChangedListener,
                    MediaDetailPagerFragment.MediaDetailProvider,
                    AdapterView.OnItemClickListener{


    private FragmentManager supportFragmentManager;
    private CategoryImagesListFragment categoryImagesListFragment;
    private MediaDetailPagerFragment mediaDetails;
    private String categoryName;

    @Override
    protected void onAuthCookieAcquired(String authCookie) {

    }

    @Override
    protected void onAuthFailure() {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_images);
        ButterKnife.bind(this);

        // Activity can call methods in the fragment by acquiring a
        // reference to the Fragment from FragmentManager, using findFragmentById()
        supportFragmentManager = getSupportFragmentManager();
        setCategoryImagesFragment();
        supportFragmentManager.addOnBackStackChangedListener(this);
        requestAuthToken();
        setPageTitle();
        initDrawer();
        initBackButton();
    }

    /**
     * Gets the categoryName from the intent and initializes the fragment for showing images of that category
     */
    private void setCategoryImagesFragment() {
        categoryImagesListFragment = new CategoryImagesListFragment();
        categoryName = getIntent().getStringExtra("categoryName");
        if (getIntent() != null && categoryName != null) {
            Bundle arguments = new Bundle();
            arguments.putString("categoryName", categoryName);
            categoryImagesListFragment.setArguments(arguments);
            FragmentTransaction transaction = supportFragmentManager.beginTransaction();
            transaction
                    .add(R.id.fragmentContainer, categoryImagesListFragment)
                    .commit();
        }
    }

    /**
     * Gets the passed title from the intents and displays it as the page title
     */
    private void setPageTitle() {
        if (getIntent() != null && getIntent().getStringExtra("title") != null) {
            setTitle(getIntent().getStringExtra("title"));
        }
    }

    @Override
    public void onBackStackChanged() {
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        if (mediaDetails == null || !mediaDetails.isVisible()) {
            // set isFeaturedImage true for featured images, to include author field on media detail
            mediaDetails = new MediaDetailPagerFragment(false, true);
            FragmentManager supportFragmentManager = getSupportFragmentManager();
            supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.fragmentContainer, mediaDetails)
                    .addToBackStack(null)
                    .commit();
            supportFragmentManager.executePendingTransactions();
        }
        mediaDetails.showImage(i);
        initBackButton();
    }

    @Override
    protected void onResume() {
        initBackButton();
        if (supportFragmentManager.getBackStackEntryCount()==1){
            //FIXME: Temporary fix for screen rotation inside media details. If we don't call onBackPressed then fragment stack is increasing every time.
            //FIXME: Similar issue like this https://github.com/commons-app/apps-android-commons/issues/894
            onBackPressed();
        }
        super.onResume();
    }

    /**
     * Consumers should be simply using this method to use this activity.
     * @param context
     * @param title Page title
     * @param categoryName Name of the category for displaying its images
     */
    public static void startYourself(Context context, String title, String categoryName) {
        Intent intent = new Intent(context, CategoryDetailsActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        intent.putExtra("title", title);
        intent.putExtra("categoryName", categoryName);
        context.startActivity(intent);
    }

    @Override
    public Media getMediaAtPosition(int i) {
        if (categoryImagesListFragment.getAdapter() == null) {
            // not yet ready to return data
            return null;
        } else {
            return (Media) categoryImagesListFragment.getAdapter().getItem(i);
        }
    }

    @Override
    public int getTotalMediaCount() {
        if (categoryImagesListFragment.getAdapter() == null) {
            return 0;
        }
        return categoryImagesListFragment.getAdapter().getCount();
    }

    @Override
    public void notifyDatasetChanged() {

    }

    @Override
    public void registerDataSetObserver(DataSetObserver observer) {
    }

    @Override
    public void unregisterDataSetObserver(DataSetObserver observer) {

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.fragment_category_detail, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        // Handle item selection
        switch (item.getItemId()) {
            case R.id.menu_browser_current_category:
                Intent viewIntent = new Intent();
                viewIntent.setAction(Intent.ACTION_VIEW);
                viewIntent.setData(new PageTitle(categoryName).getCanonicalUri());
                //check if web browser available
                if (viewIntent.resolveActivity(this.getPackageManager()) != null) {
                    startActivity(viewIntent);
                } else {
                    Toast toast = Toast.makeText(this, getString(R.string.no_web_browser), LENGTH_SHORT);
                    toast.show();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
