package gr.hua.adamantios.assignment1;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.Toast;

import org.apache.http.conn.ConnectTimeoutException;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

class RetrieveFeed extends AsyncTask<Void, Void, Integer> {

    private Context context;
    private URL url;
    private boolean wrongURL = false;
    private boolean slowConnection = false;
    private final AlertDialog.Builder builder;
    private final ProgressDialog ringProgressDialog;
    private ArrayList<String> headlines = new ArrayList<>();     // arraylist for headlines
    private ArrayList<String> descriptions = new ArrayList<>();  // arraylist for descriptions

    RetrieveFeed(MainActivity activity, URL url) {
        this.context = activity;
        this.ringProgressDialog = new ProgressDialog(activity);
        this.builder = new AlertDialog.Builder(activity);
        this.url = url;

    }

    // check if there is network available
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    // dismiss progress dialog
    private void progressDialogDismiss() {
        if (ringProgressDialog.isShowing())
            ringProgressDialog.dismiss();
    }

    // open URL connection and get input stream
    private InputStream getInputStream(URL url) {
        try {
            // open connection
            URLConnection connection = url.openConnection();
            // set connection timeout 5 secs
            connection.setConnectTimeout(5000);
            // set socket timeout 7 secs
            connection.setReadTimeout(7000);
            return connection.getInputStream();

        } catch (ConnectTimeoutException e) {
            e.printStackTrace();
            slowConnection = true;
            publishProgress();
            return null;
        } catch (SocketTimeoutException e) {
            e.printStackTrace();
            slowConnection = true;
            publishProgress();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            wrongURL = true;
            publishProgress();
            return null;
        }
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        // if network is not available show message and cancel async task
        if (!isNetworkAvailable()) {
            builder.setMessage(R.string.dialog_message)
                    .setTitle(R.string.dialog_title);

            builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    // cancel no Internet Connection Dialog
                    dialog.cancel();
                }
            });
            // create no Internet Connection Dialog
            AlertDialog dialog = builder.create();
            // show no Internet Connection Dialog
            dialog.show();

            //Cancel Async Task
            cancel(true);
        }

        // create a progress dialog
        ringProgressDialog.setMessage(context.getString(R.string.retrieving));
        ringProgressDialog.setIndeterminate(false);
        ringProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        ringProgressDialog.setCancelable(true);
    }

    @Override
    protected Integer doInBackground(Void... voids) {
        // program's status variable to return
        int status = 0;

        try {
            publishProgress();

            // create a new XML parser
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(false);
            XmlPullParser xpp = factory.newPullParser();

            //get the XML from an input stream
            InputStream stream = getInputStream(url);
            if (stream != null)
                // set the XML parser with the returned XML
                xpp.setInput(stream, "UTF_8");
            else
                cancel(true);

        /* parse the XML content looking for the "<title>" tag which appears inside the "<item>" tag.
         * However, we should take in consideration that the RSS feed name also is enclosed in a "<title>" tag.
         * As we know, every feed begins with these lines: "<channel><title>Feed_Name</title>...."
         * so we should skip the "<title>" tag which is a child of "<channel>" tag,
         * and take in consideration only "<title>" tag which is a child of "<item>"
         *
         * In order to achieve this, we will make use of a boolean variable.
         */
            boolean insideItem = false;

            // Returns the type of current event: START_TAG, END_TAG, etc..
            int eventType = xpp.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG) {

                    if (xpp.getName().equalsIgnoreCase("item")) {
                        insideItem = true;

                    } else if (xpp.getName().equalsIgnoreCase("title")) {

                        if (insideItem)
                            headlines.add(xpp.nextText()); //extract the headline

                    } else if (xpp.getName().equalsIgnoreCase("description")) {

                        if (insideItem)
                            descriptions.add(xpp.nextText()); //extract the description of the article

                    }

                } else if (eventType == XmlPullParser.END_TAG && xpp.getName().equalsIgnoreCase("item"))
                    insideItem = false;

                eventType = xpp.next(); //move to next element
            }

        } catch (XmlPullParserException e) {
            e.printStackTrace();
            status = 1;
        } catch (IOException e) {
            e.printStackTrace();
            status = 2;
        }

        // return program's status
        return status;
    }

    @Override
    protected void onProgressUpdate(Void... voids) {
        super.onProgressUpdate();
        if (slowConnection) {
            // dismiss progress dialog
            progressDialogDismiss();

            builder.setMessage(R.string.slow_connection)
                    .setTitle(R.string.try_again);

            builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    // cancel slow Internet Connection Dialog
                    dialog.cancel();
                }
            });
            // create slow Internet Connection Dialog
            AlertDialog dialog = builder.create();
            // show slow Internet Connection Dialog
            dialog.show();
        }
        if (wrongURL) {
            // show wrong URL dialog message
            progressDialogDismiss();
            builder.setMessage(R.string.enter_new_url)
                    .setTitle(R.string.wrong_url);

            builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    // cancel wrong URL Dialog
                    dialog.cancel();
                }
            });
            // create wrong URL Dialog
            AlertDialog dialog = builder.create();
            // show wrong URL Dialog
            dialog.show();
        } else
            // show Progress Dialog
            ringProgressDialog.show();
    }

    @Override
    protected void onCancelled() {
        // dismiss progress dialog
        progressDialogDismiss();
        super.onCancelled();
    }

    @Override
    protected void onPostExecute(Integer result) {
        // if doInBackground finished normally
        if (result == 0) {
            // create and initialise intent
            Intent intent = new Intent();
            intent.setAction("gr.hua.android.assignment1.RSSpresentation");

            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            // create a Bundle and pass the arraylists to it
            Bundle bundle = new Bundle();
            bundle.putSerializable("head", headlines);
            bundle.putSerializable("desc", descriptions);

            // pass Bundle to the Intent
            intent.putExtras(bundle);

            //dismiss progress dialog
            progressDialogDismiss();

            //if an activity with proper action exists start the activity
            if (intent.resolveActivity(context.getPackageManager()) != null)
                context.startActivity(intent);

                //else notify the user
            else
                Toast.makeText(context, "No Activity found", Toast.LENGTH_LONG).show();
        } else {
            //dismiss progress dialog
            progressDialogDismiss();

            builder.setMessage(R.string.be_sure_message)
                    .setTitle(R.string.something_went_wrong);

            builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    // cancel no Internet Connection Dialog
                    dialog.cancel();
                }
            });
            // create something went wrong Dialog
            AlertDialog dialog = builder.create();
            // show something went wrong Dialog
            dialog.show();
        }
        super.onPostExecute(result);
    }
}
