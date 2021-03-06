/*
 * Copyright 2016 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.kalei.vision;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.vision.v1.Vision;
import com.google.api.services.vision.v1.VisionRequestInitializer;
import com.google.api.services.vision.v1.model.AnnotateImageRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesResponse;
import com.google.api.services.vision.v1.model.EntityAnnotation;
import com.google.api.services.vision.v1.model.Feature;
import com.google.api.services.vision.v1.model.Image;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.provider.Settings.Secure;
import android.speech.tts.TextToSpeech;
import android.speech.tts.Voice;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class MainActivity extends AppCompatActivity {
    private static final String CLOUD_VISION_API_KEY = "AIzaSyCAUwAH8egdbmm5p8SsZEddWQEGR5_YmZE";
    public static final String FILE_NAME = "temp.jpg";

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int GALLERY_IMAGE_REQUEST = 1;
    public static final int CAMERA_PERMISSIONS_REQUEST = 2;
    public static final int CAMERA_IMAGE_REQUEST = 3;
    private static final float PITCH_VALUE = 1.0f;
    private static final float SPEECH_RATE = 1.0f;
    AsyncTask mTask;
    private TextView mImageDetails;
    private ImageView mMainImage;
    ProgressDialog mProgressLoading;
    private HashMap<String, String> mTranslateTable;
    private TextToSpeech mTTS;
    private LinearLayout mContainer;

    public void loadAds() {

        AdView mAdView = (AdView) findViewById(R.id.adView);
        if (mAdView != null) {
            String android_id = Secure.getString(this.getContentResolver(),
                    Secure.ANDROID_ID);
//            AdRequest adRequest = new AdRequest.Builder().addTestDevice(android_id).build();
            AdRequest adRequest = new AdRequest.Builder().build();
//            mAdView.loadAd(adRequest);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mTTS = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status != TextToSpeech.ERROR) {
                    mTTS.setLanguage(Locale.US);
                }
            }
        });
//        int result = mTTS.setLanguage(Locale.ENGLISH);
//        if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
//            Log.e("TTS", "This Language is not supported");
//            Intent installIntent = new Intent();
//            installIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
//            startActivity(installIntent);
//        }
        loadAds();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        createHashTable();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View v) {
                startCamera();
            }
        });
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
//                builder
//                        .setMessage(R.string.dialog_select_prompt)
//                        .setPositiveButton(R.string.dialog_select_gallery, new DialogInterface.OnClickListener() {
//                            @Override
//                            public void onClick(DialogInterface dialog, int which) {
//                                startGalleryChooser();
//                            }
//                        })
//                        .setNegativeButton(R.string.dialog_select_camera, new DialogInterface.OnClickListener() {
//                            @Override
//                            public void onClick(DialogInterface dialog, int which) {
//                                startCamera();
//                            }
//                        });
//                builder.create().show();
//            }
//        });

        mContainer = (LinearLayout) findViewById(R.id.container);
        mImageDetails = (TextView) findViewById(R.id.image_details);
        mMainImage = (ImageView) findViewById(R.id.main_image);
    }

    public void startGalleryChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select a photo"),
                GALLERY_IMAGE_REQUEST);
    }

    public void startCamera() {
        if (PermissionUtils.requestPermission(
                this,
                CAMERA_PERMISSIONS_REQUEST,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA)) {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(getCameraFile()));
            startActivityForResult(intent, CAMERA_IMAGE_REQUEST);
        }
    }

    public void toggleLoading(boolean showLoading) {
        mProgressLoading = ProgressDialog.show(this, "Uploading image", "Please wait...", true, true);
        if (!showLoading) {
            mProgressLoading.dismiss();
        }
    }

    public File getCameraFile() {
        File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        return new File(dir, FILE_NAME);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        loadAds();
        mContainer.removeAllViews();
        mMainImage.setVisibility(View.VISIBLE);
        if (requestCode == GALLERY_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            uploadImage(data.getData());
        } else if (requestCode == CAMERA_IMAGE_REQUEST && resultCode == RESULT_OK) {
            uploadImage(Uri.fromFile(getCameraFile()));
        }
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (PermissionUtils.permissionGranted(
                requestCode,
                CAMERA_PERMISSIONS_REQUEST,
                grantResults)) {
            startCamera();
        }
    }

    public void uploadImage(Uri uri) {
        if (uri != null) {
            try {
                // scale the image to save on bandwidth
                Bitmap bitmap =
                        scaleBitmapDown(
                                MediaStore.Images.Media.getBitmap(getContentResolver(), uri),
                                1200);

                callCloudVision(bitmap);
                mMainImage.setImageBitmap(bitmap);
            } catch (IOException e) {
                Log.d(TAG, "Image picking failed because " + e.getMessage());
                Toast.makeText(this, R.string.image_picker_error, Toast.LENGTH_LONG).show();
            }
        } else {
            Log.d(TAG, "Image picker gave us a null image.");
            Toast.makeText(this, R.string.image_picker_error, Toast.LENGTH_LONG).show();
        }
    }

    private void callCloudVision(final Bitmap bitmap) throws IOException {
        // Switch text to loading
        mImageDetails.setText("");
        mProgressLoading = ProgressDialog.show(this, "Uploading image", "Please wait...", true, true);
        mProgressLoading.setOnCancelListener(new OnCancelListener() {
            @Override
            public void onCancel(final DialogInterface dialog) {
                if (mTask != null) {
                    mImageDetails.setText(getString(R.string.instruction_text));
                    mMainImage.setVisibility(View.GONE);
                    mTask.cancel(true);
                }
            }
        });

        // Do the real work in an async task, because we need to use the network anyway
        mTask = new AsyncTask<Object, Void, List<String>>() {
            @Override
            protected List<String> doInBackground(Object... params) {
                try {
                    HttpTransport httpTransport = AndroidHttp.newCompatibleTransport();
                    JsonFactory jsonFactory = GsonFactory.getDefaultInstance();

                    Vision.Builder builder = new Vision.Builder(httpTransport, jsonFactory, null);
                    builder.setVisionRequestInitializer(new
                            VisionRequestInitializer(CLOUD_VISION_API_KEY));
                    Vision vision = builder.build();

                    BatchAnnotateImagesRequest batchAnnotateImagesRequest =
                            new BatchAnnotateImagesRequest();
                    batchAnnotateImagesRequest.setRequests(new ArrayList<AnnotateImageRequest>() {{
                        AnnotateImageRequest annotateImageRequest = new AnnotateImageRequest();

                        // Add the image
                        Image base64EncodedImage = new Image();
                        // Convert the bitmap to a JPEG
                        // Just in case it's a format that Android understands but Cloud Vision
                        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, byteArrayOutputStream);
                        byte[] imageBytes = byteArrayOutputStream.toByteArray();

                        // Base64 encode the JPEG
                        base64EncodedImage.encodeContent(imageBytes);
                        annotateImageRequest.setImage(base64EncodedImage);

                        // add the features we want
                        annotateImageRequest.setFeatures(new ArrayList<Feature>() {{
                            Feature labelDetection = new Feature();
                            labelDetection.setType("LABEL_DETECTION");
                            labelDetection.setMaxResults(10);
                            add(labelDetection);
                        }});

                        // Add the list of one thing to the request
                        add(annotateImageRequest);
                    }});

                    Vision.Images.Annotate annotateRequest =
                            vision.images().annotate(batchAnnotateImagesRequest);
                    // Due to a bug: requests to Vision API containing large images fail when GZipped.
                    annotateRequest.setDisableGZipContent(true);
                    Log.d(TAG, "created Cloud Vision request object, sending request");

                    BatchAnnotateImagesResponse response = annotateRequest.execute();
                    return convertResponseToString(response);
                } catch (GoogleJsonResponseException e) {
                    Log.d(TAG, "failed to make API request because " + e.getContent());
                } catch (IOException e) {
                    Log.d(TAG, "failed to make API request because of other IOException " +
                            e.getMessage());
                }
                return null;//"Cloud Vision API request failed. Check logs for details.";
            }

            protected void onPostExecute(List<String> result) {
                mProgressLoading.dismiss();
//                if (label.getScore() * 100 > 75) {
                String s = "";
                for (String r : result) {
                    s += r + "\n";
                    Button row = new Button(MainActivity.this);
                    row.setText(r);
                    row.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT));
                    row.setPadding(50, 50, 50, 50);
                    row.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(final View v) {
                            sayWord(((TextView) v).getText().toString());
                        }
                    });
                    mContainer.addView(row);
                }
//                    stringList.add(label.getDescription());
//                }
                mImageDetails.setText(s);
            }
        };
        mTask.execute();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mTTS != null) {
            mTTS.shutdown();
        }
    }

    public Bitmap scaleBitmapDown(Bitmap bitmap, int maxDimension) {

        int originalWidth = bitmap.getWidth();
        int originalHeight = bitmap.getHeight();
        int resizedWidth = maxDimension;
        int resizedHeight = maxDimension;

        if (originalHeight > originalWidth) {
            resizedHeight = maxDimension;
            resizedWidth = (int) (resizedHeight * (float) originalWidth / (float) originalHeight);
        } else if (originalWidth > originalHeight) {
            resizedWidth = maxDimension;
            resizedHeight = (int) (resizedWidth * (float) originalHeight / (float) originalWidth);
        } else if (originalHeight == originalWidth) {
            resizedHeight = maxDimension;
            resizedWidth = maxDimension;
        }
        return Bitmap.createScaledBitmap(bitmap, resizedWidth, resizedHeight, false);
    }

    private List<String> convertResponseToString(BatchAnnotateImagesResponse response) {
        String message = "I found these things:\n\n";

        List<String> stringList = new ArrayList<>();
        List<EntityAnnotation> labels = response.getResponses().get(0).getLabelAnnotations();
        if (labels != null) {
            for (EntityAnnotation label : labels) {

                message += String.format("%d%% : %s", Math.round(label.getScore() * 100),
                        lookUpHashValue(label.getDescription()) == null ? label.getDescription() : lookUpHashValue(label.getDescription()));
                message += "\n";
                if (label.getScore() * 100 > 75) {
                    stringList.add(label.getDescription());
                }
            }
        } else {
            message += "nothing";
        }

        return stringList;
    }

    private void createHashTable() {
        if (mTranslateTable == null) {
            mTranslateTable = new HashMap<>();
        }

        //for smellia
//        mTranslateTable.put("model", "hag");
//        mTranslateTable.put("girl", "ho bag");
//        mTranslateTable.put("eyebrow", "manly brow");
//        mTranslateTable.put("eye brow", "manly brow");
//        mTranslateTable.put("beauty", "silly ho");
//        mTranslateTable.put("laptop", "stupid piece of metal");
//        mTranslateTable.put("woman", "troll");
//        mTranslateTable.put("child", "stumpy");
//        mTranslateTable.put("hair", "smelly");
//        mTranslateTable.put("toddler", "trilobyte");
    }

    protected void setupVoice() {

        mTTS.setSpeechRate(SPEECH_RATE);
        Set<Voice> voices = mTTS.getVoices();

        if (voices != null) {
            for (Voice v : voices) {
                //could use a british voice here
                //todo: make users download a language pack when they open the app
                if (v.getLocale().equals(Locale.getDefault()) && v.getQuality() == Voice.QUALITY_HIGH) {
                    Log.i("reid", v.toString());
//                mTTS.setVoice(v);
                }
                if (v.getQuality() == Voice.QUALITY_HIGH && v.getLatency() == Voice.LATENCY_LOW && v.getLocale().equals(Locale.getDefault())) {
//                    mTTS.setVoice(v);
                }
            }
        }
        mTTS.setPitch(PITCH_VALUE);
//        mTTS.speak("ka", TextToSpeech.QUEUE_FLUSH, null, "");

    }

    protected void sayWord(String word) {
        setupVoice();

        //todo: handle other languages
        mTTS.setLanguage(Locale.ENGLISH);
        mTTS.speak(word, TextToSpeech.QUEUE_FLUSH, null, "");
    }

    private String lookUpHashValue(String key) {

        return mTranslateTable.get(key);
    }
}
