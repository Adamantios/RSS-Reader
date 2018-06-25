package gr.hua.adamantios.assignment1;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

class RSSTask extends AsyncTask<Void, Void, Integer> {

    private Activity activity;
    private final AlertDialog.Builder builder;
    private ListView listView;
    private final Bundle bundle;
    private final ProgressDialog ringProgressDialog;

    private ArrayList<String> headlines = new ArrayList<>();     // arrayList for headlines
    private ArrayList<String> descriptions = new ArrayList<>();  // arrayList for descriptions

    RSSTask(RSSpresentation activity, ListView listView, Bundle bundle) {
        this.activity = activity;
        // create an Alert Dialog
        builder = new AlertDialog.Builder(activity);
        this.listView = listView;
        this.bundle = bundle;
        this.ringProgressDialog = new ProgressDialog(activity);
    }

    // dismiss progress dialog
    private void progressDialogDismiss() {
        if (ringProgressDialog.isShowing())
            ringProgressDialog.dismiss();
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void onPreExecute() {
        super.onPreExecute();
        // get ArrayLists from Bundle
        // create a progress dialog
        ringProgressDialog.setMessage(activity.getString(R.string.initialising));
        ringProgressDialog.setIndeterminate(false);
        ringProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        ringProgressDialog.setCancelable(true);
        ringProgressDialog.show();
        try {

            headlines = (ArrayList<String>) bundle.getSerializable("head");
            descriptions = (ArrayList<String>) bundle.getSerializable("desc");
        } catch (ClassCastException e) {
            e.printStackTrace();
            builder.setMessage(R.string.wrong_format_message)
                    .setTitle(R.string.wrong_format);

            builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    // cancel wrong format message Dialog
                    dialog.cancel();
                    activity.finish();
                }
            });
            // create wrong format message Dialog
            AlertDialog dialog = builder.create();
            // show wrong format message Dialog
            dialog.show();
        }
    }

    @Override
    protected Integer doInBackground(Void... voids) {
        // if there are headlines
        if (headlines != null && !headlines.isEmpty())
            return 0;
        return 1;
    }

    @Override
    protected void onPostExecute(Integer result) {
        if (result == 0) {
            // binding data
            // ...
            // cancel ring process dialog and create adapter with the headlines
            progressDialogDismiss();
            ArrayAdapter<String> adapter = new ArrayAdapter<>(activity,
                    android.R.layout.simple_list_item_1, headlines);

            // set the listView with the headlines
            listView.setAdapter(adapter);

            // if there are descriptions
            if (descriptions != null && !descriptions.isEmpty()) {
                // create adapter with the descriptions
                AdapterView.OnItemClickListener mMessageClickedHandler = new AdapterView.OnItemClickListener() {
                    public void onItemClick(AdapterView parent, View v, int position, long id) {
                        // if there is a description
                        if (descriptions.get(position) != null)
                            // show the description with a Toast
                            Toast.makeText(activity, descriptions.get(position),
                                    Toast.LENGTH_LONG).show();
                        else
                            // show a no description found Toast
                            Toast.makeText(activity,
                                    R.string.no_description_found,
                                    Toast.LENGTH_SHORT).show();
                    }
                };

                // set items click listener from the listView with the descriptions
                listView.setOnItemClickListener(mMessageClickedHandler);
            } else {
                // show a no descriptions Toast
                Toast.makeText(activity,
                        R.string.no_descriptions,
                        Toast.LENGTH_SHORT).show();
            }

        } else if (result == 1) {
            builder.setMessage(R.string.no_titles_message)
                    .setTitle(R.string.no_titles);

            builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    // cancel no titles Dialog
                    dialog.cancel();
                    activity.finish();
                }
            });
            // create no titles Dialog
            AlertDialog dialog = builder.create();
            // show no titles Dialog
            dialog.show();
        }
    }
}
