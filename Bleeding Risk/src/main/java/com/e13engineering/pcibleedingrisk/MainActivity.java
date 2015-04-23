package com.e13engineering.pcibleedingrisk;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import java.util.HashMap;
import java.util.Map;


public class MainActivity extends ActionBarActivity {

    private android.support.v7.app.ActionBar actionBar;

    private static final String PREFS = "prefs";
    private static final String PREFS_DISCLAIMER = "false";
    private static final String TRACK_CREATE = "pcibleedingrisk.onCreate",
                                TRACK_RESET = "pcibleedingrisk.viewReset";
    RadioButtonHolder rbHolder;
    TextViewHolder textHolder;
    GoogleAnalytics analytics = null;
    Tracker tracker = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        // Hook up the analytics
        analytics = GoogleAnalytics.getInstance(this);
        tracker = analytics.newTracker(R.xml.analytics);
        tracker.setScreenName(TRACK_CREATE);
        tracker.send(new HitBuilders.AppViewBuilder().build());

        // Set up the action bar
        actionBar = getSupportActionBar();
        actionBar.setElevation(0);

        //Call the disclaimer popup
        displayDisclaimer();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (textHolder == null) {
            textHolder = new TextViewHolder();
        }
        if (rbHolder == null) {
            rbHolder = new RadioButtonHolder();
        }
        update();
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        /*if (id == R.id.action_settings) {
            return true;
        }*/
        if (id == R.id.action_disclaimer) {
            onDisclaimerClicked();
        }
        if (id == R.id.action_reset) {
            rbHolder.reset();
            update();
            if (tracker != null) {
                tracker.setScreenName(TRACK_RESET);
                tracker.send(new HitBuilders.AppViewBuilder().build());
            }
        }
        return super.onOptionsItemSelected(item);
    }


    // Check if the EULA has been accepted
    public void displayDisclaimer() {
        // Access the device's key-value storage
        final SharedPreferences preferences = getSharedPreferences(PREFS, MODE_PRIVATE);

        // Get the disclaimer state and check that it's been accepted
        String disclaimer_state = preferences.getString(PREFS_DISCLAIMER, "");

        if (!disclaimer_state.equals("accepted")) {
            // otherwise, popup the disclaimer
            AlertDialog.Builder alert = new AlertDialog.Builder(this);
            alert.setTitle("EULA");
            alert.setMessage(R.string.liability);


            // Make an "Accept" button to accept the disclaimer
            alert.setPositiveButton("I Understand", new DialogInterface.OnClickListener() {

                public void onClick(DialogInterface dialog, int whichButton) {
                    // Put it into memory (don't forget to commit!)
                    SharedPreferences.Editor e = preferences.edit();
                    e.putString(PREFS_DISCLAIMER, "accepted");
                    e.apply();

                    // Welcome the new user
                    Toast.makeText(getApplicationContext(), "EULA accepted",
                            Toast.LENGTH_SHORT).show();
                }
            });
            alert.show();
        }
    }

    // Request to show the EULA
    public void onDisclaimerClicked() {
        // Access the device's key-value storage

        // Open the disclaimer acceptance state, write unaccepted
        SharedPreferences preferences = getSharedPreferences(PREFS, MODE_PRIVATE);
        SharedPreferences.Editor e = preferences.edit();
        e.putString(PREFS_DISCLAIMER, "unaccepted");
        e.apply();

        //Call the popup dialog
        displayDisclaimer();
    }

    // A radio button was clicked
    public void onRadioButtonClicked(View view) {
        update();
    }

    // Update the scores after the radio button evaluations
    public void update() {
        double score = rbHolder.evaluateScore();
        double percent = rbHolder.evaluatePercent(score);
        textHolder.updateViews(score, percent);
    }

    class RadioButtonHolder {
        RadioButton rbSTEMI1, rbSTEMI2, rbAGE1, rbAGE2, rbAGE3, rbAGE4, rbBMI1, rbBMI2, rbBMI3, rbBMI4, rbPCI1, rbPCI2, rbKID1, rbKID2, rbKID3, rbKID4, rbSHK1, rbSHK2, rbCRD1, rbCRD2, rbGEN1, rbGEN2, rbHB1, rbHB2, rbHB3, rbPCS1, rbPCS2, rbPCS3;
        double[] scores_array = {0,5,10,15,20,25,30,35,40,45,50,55,60,65,70,75,80,85,90,95,100,105,110,115,120,125,130,135,140,145,150,155,160,165,170,175,180,185,190,195,200,205,210};
        double[] percents_array = {0.9,1.1,1.3,1.5,1.7,2.0,2.3,2.7,3.1,3.6,4.2,4.9,5.6,6.5,7.5,8.6,9.9,11.4,13.1,14.9,17.0,19.3,21.8,24.6,27.5,30.7,34.1,37.6,41.3,45.1,49.0,52.8,56.6,60.4,64.0,67.5,70.8,73.9,76.8,79.4,81.8,84.0,86.0};
        Map<Double, Double> scores_percents_map = new HashMap<Double, Double>();

        public RadioButtonHolder() {
            //STEMI
            rbSTEMI1 = (RadioButton) findViewById(R.id.stemi_option1);
            rbSTEMI2 = (RadioButton) findViewById(R.id.stemi_option2);
            //Age
            rbAGE1 = (RadioButton) findViewById(R.id.age_option1);
            rbAGE2 = (RadioButton) findViewById(R.id.age_option2);
            rbAGE3 = (RadioButton) findViewById(R.id.age_option3);
            rbAGE4 = (RadioButton) findViewById(R.id.age_option4);
            //BMI
            rbBMI1 = (RadioButton) findViewById(R.id.bmi_option1);
            rbBMI2 = (RadioButton) findViewById(R.id.bmi_option2);
            rbBMI3 = (RadioButton) findViewById(R.id.bmi_option3);
            rbBMI4 = (RadioButton) findViewById(R.id.bmi_option4);
            //Previous PCI
            rbPCI1 = (RadioButton) findViewById(R.id.previous_pci_option1);
            rbPCI2 = (RadioButton) findViewById(R.id.previous_pci_option2);
            //Chronic Kidney Disease
            rbKID1 = (RadioButton) findViewById(R.id.chronic_kidney_option1);
            rbKID2 = (RadioButton) findViewById(R.id.chronic_kidney_option2);
            rbKID3 = (RadioButton) findViewById(R.id.chronic_kidney_option3);
            rbKID4 = (RadioButton) findViewById(R.id.chronic_kidney_option4);
            //Shock
            rbSHK1 = (RadioButton) findViewById(R.id.shock_option1);
            rbSHK2 = (RadioButton) findViewById(R.id.shock_option2);
            //Cardiac Arrest
            rbCRD1 = (RadioButton) findViewById(R.id.cardiac_option1);
            rbCRD2 = (RadioButton) findViewById(R.id.cardiac_option2);
            //Gender
            rbGEN1 = (RadioButton) findViewById(R.id.gender_option1);
            rbGEN2 = (RadioButton) findViewById(R.id.gender_option2);
            //Hb switch
            rbHB1 = (RadioButton) findViewById(R.id.hb_option1);
            rbHB2 = (RadioButton) findViewById(R.id.hb_option2);
            rbHB3 = (RadioButton) findViewById(R.id.hb_option3);
            //PCI Status
            rbPCS1 = (RadioButton) findViewById(R.id.pci_status_option1);
            rbPCS2 = (RadioButton) findViewById(R.id.pci_status_option2);
            rbPCS3 = (RadioButton) findViewById(R.id.pci_status_option3);

            for (int i=0;i<scores_array.length;i=i+1) {
                scores_percents_map.put(scores_array[i], percents_array[i]);
            }
        }

        public double evaluateScore() {
            int stemi_score = 0;
            int age_score = 0;
            int bmi_score = 0;
            int prev_pci_score = 0;
            int kidney_score = 0;
            int shock_score = 0;
            int cardiac_score = 0;
            int gender_score = 0;
            int hb_score = 0;
            int pci_status_score = 0;

            //STEMI switch
            if (rbSTEMI1.isChecked()) {
                stemi_score = 0;
            }
            if (rbSTEMI2.isChecked()) {
                stemi_score = 15;
            }

            //Age switch
            if (rbAGE1.isChecked()) {
                age_score = 0;
            }
            if (rbAGE2.isChecked()) {
                age_score = 10;
            }
            if (rbAGE3.isChecked()) {
                age_score = 15;
            }
            if (rbAGE4.isChecked()) {
                age_score = 20;
            }

            //BMI switch
            if (rbBMI1.isChecked()) {
                bmi_score = 15;
            }
            if (rbBMI2.isChecked()) {
                bmi_score = 5;
            }
            if (rbBMI3.isChecked()) {
                bmi_score = 0;
            }
            if (rbBMI4.isChecked()) {
                bmi_score = 5;
            }

            //Previous PCI switch
            if (rbPCI1.isChecked()) {
                prev_pci_score = 10;
            }
            if (rbPCI2.isChecked()) {
                prev_pci_score = 0;
            }

            //Chronic Kidney Disease switch
            if (rbKID1.isChecked()) {
                kidney_score = 0;
            }
            if (rbKID2.isChecked()) {
                kidney_score = 10;
            }
            if (rbKID3.isChecked()) {
                kidney_score = 25;
            }
            if (rbKID4.isChecked()) {
                kidney_score = 30;
            }

            //Shock switch
            if (rbSHK1.isChecked()) {
                shock_score = 0;
            }
            if (rbSHK2.isChecked()) {
                shock_score = 35;
            }

            //Cardiac Arrest switch
            if (rbCRD1.isChecked()) {
                cardiac_score = 0;
            }
            if (rbCRD2.isChecked()) {
                cardiac_score = 15;
            }

            //Gender switch
            if (rbGEN1.isChecked()) {
                gender_score = 0;
            }
            if (rbGEN2.isChecked()) {
                gender_score = 20;
            }

            //Hb switch
            if (rbHB1.isChecked()) {
                hb_score = 5;
            }
            if (rbHB2.isChecked()) {
                hb_score = 0;
            }
            if (rbHB3.isChecked()) {
                hb_score = 10;
            }

            //PCI Status switch
            if (rbPCS1.isChecked()) {
                pci_status_score = 0;
            }
            if (rbPCS2.isChecked()) {
                pci_status_score = 20;
            }

            if (rbPCS3.isChecked()) {
                pci_status_score = 40;
            }

            return stemi_score+age_score+bmi_score+prev_pci_score+kidney_score+shock_score+cardiac_score+gender_score+hb_score+pci_status_score;
        }

        public double evaluatePercent(double score) {
            return scores_percents_map.get(score);
        }

        public void reset() {
            rbSTEMI1.setChecked(false);
            rbSTEMI2.setChecked(false);
            rbAGE1.setChecked(false);
            rbAGE2.setChecked(false);
            rbAGE3.setChecked(false);
            rbAGE4.setChecked(false);
            rbBMI1.setChecked(false);
            rbBMI2.setChecked(false);
            rbBMI3.setChecked(false);
            rbBMI4.setChecked(false);
            rbPCI1.setChecked(false);
            rbPCI2.setChecked(false);
            rbKID1.setChecked(false);
            rbKID2.setChecked(false);
            rbKID3.setChecked(false);
            rbKID4.setChecked(false);
            rbSHK1.setChecked(false);
            rbSHK2.setChecked(false);
            rbCRD1.setChecked(false);
            rbCRD2.setChecked(false);
            rbGEN1.setChecked(false);
            rbGEN2.setChecked(false);
            rbHB1.setChecked(false);
            rbHB2.setChecked(false);
            rbHB3.setChecked(false);
            rbPCS1.setChecked(false);
            rbPCS2.setChecked(false);
            rbPCS3.setChecked(false);
        }
    }

    class TextViewHolder {
        TextView score, percent, subtitle;
        RelativeLayout resultsContainer;
        public TextViewHolder() {
            score = (TextView) findViewById(R.id.liveupdating_score);
            percent = (TextView) findViewById(R.id.liveupdating_percent);
            subtitle = (TextView) findViewById(R.id.subtitle);
            resultsContainer = (RelativeLayout) findViewById(R.id.results_container);
        }

        public void updateViews(double score, double percent) {
            this.score.setText("" + score);
            this.percent.setText("" + percent);

            if (percent >= 6.5) {
                resultsContainer.setBackgroundColor(Color.parseColor("#ffe51c23"));
                getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#ffe51c23")));
                subtitle.setBackgroundColor(Color.parseColor("#ffe51c23"));

            } else {
                resultsContainer.setBackgroundColor(Color.parseColor("#ff259b24"));
                getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#ff259b24")));
                subtitle.setBackgroundColor(Color.parseColor("#ff259b24"));
            }
        }
    }
}
