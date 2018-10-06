package me.timlesallen.awesomelauncher;

import android.app.Activity;
import android.os.Bundle;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

import java.util.Comparator;
import java.util.List;
import java.util.ArrayList;
import android.content.Intent;
import android.widget.*;
import android.view.*;
import android.content.SharedPreferences;
import android.content.Context;



public class AppsListActivity extends Activity {

    SharedPreferences prefs;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_apps_list);
        prefs = this.getPreferences(Context.MODE_PRIVATE);
        loadApps();
        loadListView();
        addClickListener();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadApps(); // in case any new apps have been installed
        loadListView(); // in case any apps have changed rank
    }

    private PackageManager manager;
    private List<AppDetail> apps;

    /**
     * Get list of available apps.
     */
    private void loadApps() {
        manager = getPackageManager();
        apps = new ArrayList<AppDetail>();

        Intent i = new Intent(Intent.ACTION_MAIN, null);
        i.addCategory(Intent.CATEGORY_LAUNCHER);

        List<ResolveInfo> availableActivities = manager.queryIntentActivities(i, 0);
        for(ResolveInfo ri:availableActivities){
            AppDetail app = new AppDetail();
            app.label = ri.loadLabel(manager);
            app.name = ri.activityInfo.packageName;
            app.icon = ri.activityInfo.loadIcon(manager);
            app.launchCount = prefs.getInt(app.name.toString(), 0);
            apps.add(app);
        }
    }

    private ListView list;
    private void loadListView() {
        list = (ListView)findViewById(R.id.apps_list);

        ArrayAdapter<AppDetail> adapter = new ArrayAdapter<AppDetail>(this,
                R.layout.list_item,
                apps) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                if(convertView == null){
                    convertView = getLayoutInflater().inflate(R.layout.list_item, null);
                }

                ImageView appIcon = (ImageView)convertView.findViewById(R.id.item_app_icon);
                appIcon.setImageDrawable(apps.get(position).icon);

                TextView appLabel = (TextView)convertView.findViewById(R.id.item_app_label);
                appLabel.setText(apps.get(position).label);

                return convertView;
            }
        };
        // Sort by launch count
        adapter.sort(new Comparator<AppDetail>() {
            @Override
            public int compare(AppDetail a1, AppDetail a2) {
                return a2.launchCount - a1.launchCount;
            }
        });

        list.setAdapter(adapter);
    }

    private void addClickListener(){
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> av, View v, int pos,
                                    long id) {
                AppDetail app = apps.get(pos);
                String appName = app.name.toString();
                Intent i = manager.getLaunchIntentForPackage(appName);
                prefs.edit().putInt(appName, app.launchCount++).apply();
                AppsListActivity.this.startActivity(i);
            }
        });
    }

}