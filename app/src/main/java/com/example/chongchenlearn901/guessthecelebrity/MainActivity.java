package com.example.chongchenlearn901.guessthecelebrity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private ArrayList<Celebrity> dataList;
    private Celebrity answer;
    private ArrayList<Button> btnOptions;
    private int optionNum = 4;
    private ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.imageView = findViewById(R.id.imageView);
        this.btnOptions = new ArrayList<>(Arrays.asList(
                findViewById(R.id.btn1),
                findViewById(R.id.btn2),
                findViewById(R.id.btn3),
                findViewById(R.id.btn4)
        ));

        try {
            dataList = (new RequestAsyncTask()).execute("http://www.posh24.se/kandisar").get();
            newGuess();
        } catch (Exception e) {
            Log.e(TAG, "onCreate: ", e);
        }

        for (Button btn : this.btnOptions) {
            btn.setOnClickListener((v) -> {
                Button b = (Button) v;
                if (answer == null) {
                    Toast.makeText(this, "Answer is null", Toast.LENGTH_SHORT).show();
                }
                if (b.getText().toString().equalsIgnoreCase(this.answer.src)) {
                    Toast.makeText(this, "CORRECT!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "WRONG", Toast.LENGTH_SHORT).show();
                }
                newGuess();
            });
        }
    }

    private void newGuess() {
        if (dataList == null) {
            return;
        }
        ArrayList<Celebrity> options = new ArrayList<>();
        int correctNum = (int) (Math.random() * this.optionNum);
        for (int i = 0; i < optionNum; i++) {
            Celebrity celebrity = getRandomCelebrity();

            while (options.contains(celebrity) || celebrity == null) {
                celebrity = getRandomCelebrity();
            }

            if (correctNum == i) {
                Bitmap img = null;
                while (true) {
                    try {
                        img = (new BitmapAsyncTask()).execute(celebrity.src).get();
                    } catch (Exception e) {
                        Log.e(TAG, "newGuess: ", e);
                    }
                    if (img == null) {
                        celebrity = getRandomCelebrity();
                        continue;
                    }
                    break;
                }
                this.answer = celebrity;
                this.imageView.setImageBitmap(img);
            }

            this.btnOptions.get(i).setText(celebrity.alt);
            options.add(celebrity);
        }
    }

    private Celebrity getRandomCelebrity() {
        if (this.dataList == null) {
            return null;
        }
        int rand = (int) (Math.random() * this.dataList.size());
        return this.dataList.get(rand);
    }

    private class Celebrity {
        private String src;
        private String alt;

        Celebrity(String src, String alt) {
            this.src = src;
            this.alt = alt;
        }
    }

    private class RequestAsyncTask extends AsyncTask<String, Void, ArrayList<Celebrity>> {
        private static final String TAG = "RequestAsyncTask";

        @Override
        protected ArrayList<Celebrity> doInBackground(String... strings) {
            if (strings == null || strings.length == 0) {
                return null;
            }
            HttpURLConnection connection = null;
            StringBuilder sb = new StringBuilder();
            try {
                URL url = new URL(strings[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();
                InputStreamReader reader = new InputStreamReader(connection.getInputStream());
                char[] data = new char[1024];
                for (int i = reader.read(data); i != -1; i = reader.read(data)) {
                    sb.append(data, 0, i);
                }
            } catch (Exception e) {
                Log.e(TAG, "doInBackground: ", e);
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }

            return parseDataList(sb.toString());
        }

        private ArrayList<Celebrity> parseDataList(String s) {
            ArrayList<Celebrity> dataList = new ArrayList<>();
            Pattern p = Pattern.compile("<img src=\"(.*)\".*alt=\"(.*)\"");
            Matcher m = p.matcher(s);
            while (m.find()) {
                dataList.add(new Celebrity(m.group(1), m.group(2)));
            }
            return dataList;
        }
    }

    private class BitmapAsyncTask extends AsyncTask<String, Void, Bitmap> {
        private static final String TAG = "BitmapAsyncTask";

        @Override
        protected Bitmap doInBackground(String... strings) {
            if (strings == null || strings.length == 0) {
                return null;
            }
            Bitmap img = null;
            try {
                URL url = new URL(strings[0]);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.connect();
                img = BitmapFactory.decodeStream(connection.getInputStream());
            } catch (Exception e) {
                Log.e(TAG, "doInBackground: ", e);
            }
            return img;
        }
    }
}
