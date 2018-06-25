package gr.hua.adamantios.assignment1;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.net.MalformedURLException;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // set a click listener in order to autocomplete http://
        final EditText mEdit = (EditText) findViewById(R.id.editText);
        mEdit.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // disable the listener so that http:// gets autocompleted only once
                mEdit.setOnClickListener(null);
                mEdit.setText(R.string.suggestion);
            }
        });
    }

    // called when presentRSS button is pressed
    public void presentRSS(View view) {
        EditText mEdit = (EditText) findViewById(R.id.editText);

        try {
            // create URL from the edit text
            URL url = new URL(mEdit.getText().toString());
            // execute AsyncTask
            new RetrieveFeed(MainActivity.this, url).execute();

        } catch (MalformedURLException e) {
            e.printStackTrace();
            // show wrong URL message
            Toast.makeText(MainActivity.this, R.string.wrong_url, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
