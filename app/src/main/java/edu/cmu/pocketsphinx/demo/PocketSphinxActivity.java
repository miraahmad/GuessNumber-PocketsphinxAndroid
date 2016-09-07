/* ====================================================================
 * Copyright (c) 2014 Alpha Cephei Inc.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY ALPHA CEPHEI INC. ``AS IS'' AND
 * ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL CARNEGIE MELLON UNIVERSITY
 * NOR ITS EMPLOYEES BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * ====================================================================
 */

package edu.cmu.pocketsphinx.demo;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.widget.Button;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Random;

import edu.cmu.pocketsphinx.Assets;
import edu.cmu.pocketsphinx.Hypothesis;
import edu.cmu.pocketsphinx.RecognitionListener;
import edu.cmu.pocketsphinx.SpeechRecognizer;
import edu.cmu.pocketsphinx.SpeechRecognizerSetup;

import static android.widget.Toast.makeText;

public class PocketSphinxActivity extends Activity implements
        RecognitionListener {

    private static final String KEYPHRASE = "guess";
    private SpeechRecognizer recognizer;

    /* Used to handle permission request */
    private static final int PERMISSIONS_REQUEST_RECORD_AUDIO = 1;

    private HashMap<String, Integer> captions;

    Button b_plus, b_minus, b_check;
    TextView tv_number, result_text, caption_text, test_text;

    int current_number, number_to_guess, tries;

    Random r;

    @Override
    public void onCreate(Bundle state) {


        super.onCreate(state);
        setContentView(R.layout.main);

        // Prepare the data for UI
        captions = new HashMap<String, Integer>();
        captions.put(KEYPHRASE, R.string.choose_number);

        b_check = (Button) findViewById(R.id.b_check);
        b_plus = (Button) findViewById(R.id.b_plus);
        b_minus = (Button) findViewById(R.id.b_minus);
        tv_number = (TextView) findViewById(R.id.tv_number);
        result_text = (TextView) findViewById(R.id.result_text);
        caption_text = (TextView) findViewById(R.id.caption_text);
        test_text = (TextView) findViewById(R.id.test_text);

        r = new Random();
        number_to_guess = r.nextInt(51);
        tries = 0;

        //test_text.setText("" + number_to_guess);

        b_check.setEnabled(false);
        b_plus.setEnabled(false);
        b_minus.setEnabled(false);


        caption_text.setText("Preparing the recognizer");

        // Check if user has given permission to record audio
        int permissionCheck = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.RECORD_AUDIO);
        if (permissionCheck == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, PERMISSIONS_REQUEST_RECORD_AUDIO);
            return;
        }
        runRecognizerSetup();
    }

    private void runRecognizerSetup() {
        // Recognizer initialization is a time-consuming and it involves IO,
        // so we execute it in async task
        new AsyncTask<Void, Void, Exception>() {
            @Override
            protected Exception doInBackground(Void... params) {
                try {
                    Assets assets = new Assets(PocketSphinxActivity.this);
                    File assetDir = assets.syncAssets();
                    setupRecognizer(assetDir);
                } catch (IOException e) {
                    return e;
                }
                return null;
            }

            @Override
            protected void onPostExecute(Exception result) {
                if (result != null) {
                    caption_text.setText("Failed to init recognizer " + result);
                } else {
                    caption_text.setText(R.string.choose_number);
                    reset();
                }
            }
        }.execute();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSIONS_REQUEST_RECORD_AUDIO) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                runRecognizerSetup();
            } else {
                finish();
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (recognizer != null) {
            recognizer.cancel();
            recognizer.shutdown();
        }
    }

    /**
     * In partial result we get quick updates about current hypothesis. In
     * keyword spotting mode we can react here, in other modes we need to wait
     * for final result in onResult.
     */
    @Override
    public void onPartialResult(Hypothesis hypothesis) {


    }


    /**
     * This callback is called when we stop the recognizer.
     */
    @Override
    public void onResult(Hypothesis hypothesis) {

        if (hypothesis != null) {
            String text = hypothesis.getHypstr();
            String arr[] = text.split(" ");
            String n1 = "";
            String n2 = "";

            if (text.contains("check")) {

                if (current_number > 50) {
                    result_text.setText("Invalid number");
                } else {
                    //test_text.setText("" + current_number);
                    checkNumber(current_number, number_to_guess);
                }

            } else {

                for (int i = 0; i < arr.length; i++) {
                    n1 = arr[i];
                    n2 = n2 + " " + n1;

                    current_number = Integer.parseInt(NumberData.replaceNumbers(n2));

                    if (current_number > 50) {
                        result_text.setText("Invalid number");
                    } else {
                        result_text.setText("");
                        tv_number.setText("" + current_number);
                    }
                }
            }
        }
    }

    public void checkNumber(int number, int guessNum) { //to check if the guess number equal to given number

        current_number = number;

        if (current_number == guessNum) {
            result_text.setText(getString(R.string.text_congrats) + " " + tries + " " + getString(R.string.text_tries));
            recognizer.stop();
        } else if (current_number > guessNum) {
            result_text.setText("Down!");
            tries++;
        } else if (current_number < guessNum) {
            result_text.setText("Up!");
            tries++;
        }

    }


    @Override
    public void onBeginningOfSpeech() {
    }

    /**
     * We stop recognizer here to get a final result
     */
    @Override
    public void onEndOfSpeech() {

        reset();

    }


    private void setupRecognizer(File assetsDir) throws IOException {
        // The recognizer can be configured to perform multiple searches
        // of different kind and switch between them

        recognizer = SpeechRecognizerSetup.defaultSetup()
                .setAcousticModel(new File(assetsDir, "en-us-ptm"))
                .setDictionary(new File(assetsDir, "cmudict-en-us.dict"))

                //.setRawLogDir(assetsDir) // To disable logging of raw audio comment out this call (takes a lot of space on the device)
                .setKeywordThreshold(1e-10f) // Threshold to tune for keyphrase to balance between false alarms and misses
                .setBoolean("-allphone_ci", true)  // Use context-independent phonetic search, context-dependent is too slow for mobile
                .setFloat("-vad_threshold", 3.0)

                .getRecognizer();
        recognizer.addListener(this);

        /** In your application you might not need to add all those searches.
         * They are added here for demonstration. You can leave just one.
         */


        // Create grammar-based search for digit recognition
        File digitsGrammar = new File(assetsDir, "digits.gram");
        recognizer.addGrammarSearch(KEYPHRASE, digitsGrammar);


    }

    @Override
    public void onError(Exception error) {
        caption_text.setText(error.getMessage());
    }

    @Override
    public void onTimeout() {


    }

    private void reset() {
        recognizer.stop();
        recognizer.startListening(KEYPHRASE);
    }

       /* private void changeNumber(int number){
        r = new Random();
        number_to_guess = r.nextInt(51);

        tries = 0;
        current_number = number;
        tv_number.setText("" + current_number);

        b_check.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (current_number == number_to_guess) {
                    result_text.setText(getString(R.string.text_congrats) + " " + tries + " " + getString(R.string.text_tries));
                    b_check.setEnabled(false);
                    b_plus.setEnabled(false);
                    b_minus.setEnabled(false);
                } else if (current_number > number_to_guess) {
                    result_text.setText("Down!");
                    tries++;
                } else if (current_number < number_to_guess) {
                    result_text.setText("Up!");
                    tries++;
                }
            }
        });

        b_plus.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (current_number < 50) {
                    current_number++;
                }
                tv_number.setText("" + current_number);
            }
        });

        b_minus.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (current_number > 0) {
                    current_number--;
                }
                tv_number.setText("" + current_number);
            }
        });

    }*/
}
