package cab.com.rideversitydriver.Activities;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ContextThemeWrapper;
import android.support.v7.widget.AppCompatCheckBox;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import cab.com.rideversitydriver.Dialogs.CustomDialog;
import cab.com.rideversitydriver.Interfaces.AsyncTaskInterface;
import cab.com.rideversitydriver.Interfaces.DialogDismisser;
import cab.com.rideversitydriver.Models.DriverInfoModel;
import cab.com.rideversitydriver.R;
import cab.com.rideversitydriver.Shapes.PolygonImageView;
import cab.com.rideversitydriver.Utils.ActivityIndicator;
import cab.com.rideversitydriver.Utils.CommonAsynTask;
import cab.com.rideversitydriver.Utils.Constants;
import cab.com.rideversitydriver.Utils.ExifUtil;
import cab.com.rideversitydriver.Utils.SharedPref;
import cab.com.rideversitydriver.Utils.Singleton;
import cab.com.rideversitydriver.Utils.Utilities;

/**
 * Created by KEERTHINI on 8/4/2016.
 */

public class VehicleDoc_Activity extends AppCompatActivity implements View.OnClickListener {

    private Button update;
    private Context context;
    private PolygonImageView imgFront, imgSide, imgRear;
    TextView upload1, upload2, upload3;
    private static final int SELECT_FILE = 1;
    private static final int REQUEST_CAMERA = 2;
    int imageclick = 10;
    //public static String strFrontVehical,strSideVehical,strRearVehical,strSeatAvaliable;
    EditText edtSeatAvaliable;
    Snackbar snackbar;
    LinearLayout layoutMain;
    static SharedPref sharedPref;
    public static String strFrontPicture = "", strSidePicture = "", strRearPicture = "", strDriverImg = "", strSeatAvaliable;
    //public static Bitmap bmFrontPicture=null,bmSidePicture=null,bmRearPicture=null;
    private static final int REQUEST_ID_MULTIPLE_PERMISSIONS = 2;

    public static String strDriverLicense = "", strCarInsurance = "", strVehicaltag = "", strRegistration = "";
    public static byte[] byteFrontPic, byteSidePic, byteRearPic;
    public static String strProfilePic5, strProfilePic6, strProfilePic7;
    AppCompatCheckBox checkbox;
    boolean ckecked = false;
    boolean front = false, side = false, rear = false;
    public static Bitmap bitmapFront, bitmapSide, bitmapRear;
    ActivityIndicator pDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.become_driver_vehicle);
        //context = getApplicationContext();
        context = this;
        sharedPref = new SharedPref(context);
        pDialog = new ActivityIndicator(context);
        update = (Button) findViewById(R.id.button_become_driver);
        imgFront = (PolygonImageView) findViewById(R.id.imageView_front);
        imgSide = (PolygonImageView) findViewById(R.id.imageView_side);
        imgRear = (PolygonImageView) findViewById(R.id.imageView_rear);
        edtSeatAvaliable = (EditText) findViewById(R.id.edittext_seat_avaliable);
        layoutMain = (LinearLayout) findViewById(R.id.layout_vehical_verification);
        checkbox = (AppCompatCheckBox) findViewById(R.id.checkbox_agree);

        if (Singleton.getInstance().mDriverInfoModels.size() > 0) {
            DriverInfoModel model = Singleton.getInstance().mDriverInfoModels.get(0);
            if (!model.getVehicleFrontPicture().equals("")) {
                setImage(imgFront, model.getVehicleFrontPicture());
            }
            if (!model.getVehicleSidePicture().equals("")) {
                setImage(imgSide, model.getVehicleSidePicture());
            }
            if (!model.getVehicleRearPicture().equals("")) {
                setImage(imgRear, model.getVehicleRearPicture());
            }
            edtSeatAvaliable.setText(model.getSeatAvailable());
        }

        if (!isTaskRoot()) {
            final Intent intent = getIntent();
            if (intent.hasCategory(Intent.CATEGORY_LAUNCHER) && Intent.ACTION_MAIN.equals(intent.getAction())) {
                finish();
                return;
            }
        }

        upload1 = (TextView) findViewById(R.id.upload1);
        upload2 = (TextView) findViewById(R.id.upload2);
        upload3 = (TextView) findViewById(R.id.upload3);

        update.setEnabled(true);
        update.setClickable(true);

        upload1.setOnClickListener(this);
        upload2.setOnClickListener(this);
        upload3.setOnClickListener(this);
        update.setOnClickListener(this);
        imgFront.setOnClickListener(this);
        imgSide.setOnClickListener(this);
        imgRear.setOnClickListener(this);

        String device_name = Build.MANUFACTURER;
        //if (device_name.equals("samsung"))
        // {

        if (Constants.PERSONAL_VEHICLE_CAMERA_RECALL == true) {
            if (Constants.bitmapFront != null) {
                imgFront.setImageBitmap(Constants.bitmapFront);
            }
            if (Constants.bitmapSide != null) {
                imgSide.setImageBitmap(Constants.bitmapSide);
            }
            if (Constants.bitmapRear != null) {
                imgRear.setImageBitmap(Constants.bitmapRear);
            }
        }

        checkbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {

                if (isChecked == true) {
                    ckecked = true;
                } else {
                    ckecked = false;
                }
            }

        });


        RelativeLayout backArrow = (RelativeLayout) findViewById(R.id.imageView_backarrow);
        backArrow.setOnClickListener(this);

    }

    private void setImage(PolygonImageView view, String imageUrl) {

        Picasso.with(context)
                .load(imageUrl)
                .placeholder(R.drawable.loading)
                .error(R.drawable.no_image)
                .into(view);


    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.button_become_driver:
                pDialog.show();
                update.setEnabled(false);

                strSeatAvaliable = edtSeatAvaliable.getText().toString();

                Bitmap bitmapEmpty = BitmapFactory.decodeResource(context.getResources(), R.drawable.pic_upload_icon);
                bitmapFront = ((BitmapDrawable) imgFront.getDrawable()).getBitmap();
                bitmapSide = ((BitmapDrawable) imgSide.getDrawable()).getBitmap();
                bitmapRear = ((BitmapDrawable) imgRear.getDrawable()).getBitmap();

                Constants.bitmapFront = ((BitmapDrawable) imgFront.getDrawable()).getBitmap();
                Constants.bitmapSide = ((BitmapDrawable) imgSide.getDrawable()).getBitmap();
                Constants.bitmapRear = ((BitmapDrawable) imgRear.getDrawable()).getBitmap();

                front = Utilities.compareBitmap(bitmapEmpty, bitmapFront);
                side = Utilities.compareBitmap(bitmapEmpty, bitmapSide);
                rear = Utilities.compareBitmap(bitmapEmpty, bitmapRear);

                if (strSeatAvaliable.isEmpty() || front == true || side == true || rear == true) {
                    pDialog.dismiss();
                    snackbar = Snackbar.make(layoutMain, "Please enter seat number and images", Snackbar.LENGTH_LONG);
                    View sbView = snackbar.getView();
                    TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
                    sbView.setBackgroundColor(ContextCompat.getColor(context, R.color.snackbar_background));
                    textView.setTextColor(ContextCompat.getColor(context, R.color.white));
                    snackbar.show();
                    update.setEnabled(true);
                    update.setClickable(true);
                } else {
                    if (ckecked == false) {
                        pDialog.dismiss();
                        snackbar = Snackbar.make(layoutMain, "Please Agree above conditions", Snackbar.LENGTH_LONG);
                        View sbView = snackbar.getView();
                        TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
                        sbView.setBackgroundColor(ContextCompat.getColor(context, R.color.snackbar_background));
                        textView.setTextColor(ContextCompat.getColor(context, R.color.white));
                        snackbar.show();
                        update.setEnabled(true);
                        update.setClickable(true);
                    } else {
                        pDialog.dismiss();
                        becomdDriver();
                    }
                }
                break;
            case R.id.imageView_backarrow:
                Constants.PERSONAL_VEHICLE_CAMERA_RECALL = false;
                finish();
                break;
            case R.id.upload1:
                imageclick = 11;
                Constants.CAMERA_ONE = 11;
                strFrontPicture = "";
                selectImage();
                break;
            case R.id.upload2:
                imageclick = 12;
                Constants.CAMERA_ONE = 12;
                strSidePicture = "";
                selectImage();
                break;
            case R.id.upload3:
                imageclick = 13;
                Constants.CAMERA_ONE = 13;
                strRearPicture = "";
                selectImage();
                break;
            default:
                break;
        }

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Constants.PERSONAL_VEHICLE_CAMERA_RECALL = false;
        finish();
    }

    private void selectImage() {

        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(VehicleDoc_Activity.this, R.style.alertDialog));
        LayoutInflater inflater = getLayoutInflater();
        View alertLayout = inflater.inflate(R.layout.dialog_choose_image, null);
        Button buttonCamera = (Button) alertLayout.findViewById(R.id.camera);
        Button buttonGallery = (Button) alertLayout.findViewById(R.id.gallery);
        Button cancel = (Button) alertLayout.findViewById(R.id.cancel);
        builder.setView(alertLayout);

        Constants.bitmapFront = ((BitmapDrawable) imgFront.getDrawable()).getBitmap();
        Constants.bitmapSide = ((BitmapDrawable) imgSide.getDrawable()).getBitmap();
        Constants.bitmapRear = ((BitmapDrawable) imgRear.getDrawable()).getBitmap();

        final AlertDialog dialog = builder.create();
        dialog.show();
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        buttonCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Constants.PERSONAL_VEHICLE_CAMERA_RECALL = true;
                Intent takePicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(takePicture, REQUEST_CAMERA);
                dialog.dismiss();

            }
        });
        buttonGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Constants.PERSONAL_VEHICLE_CAMERA_RECALL = true;
                Intent pickPhoto = new Intent(Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(pickPhoto, SELECT_FILE);
                dialog.dismiss();

            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Uri selectedImage = data.getData();

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == SELECT_FILE) {
                if (Constants.CAMERA_ONE == 11) {
                    //imgFront.setImageBitmap(onSelectFromGalleryResult(data));

                    String[] filePath = {MediaStore.Images.Media.DATA};
                    Cursor cursor = getContentResolver().query(selectedImage, filePath, null, null, null);
                    cursor.moveToFirst();
                    String imagePath = cursor.getString(cursor.getColumnIndex(filePath[0]));
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                    Bitmap bitmap = BitmapFactory.decodeFile(imagePath, options);
                    cursor.close();
                    Bitmap orientedBitmap = ExifUtil.rotateBitmap(imagePath, bitmap);
                    Constants.bitmapFront = orientedBitmap;
                    imgFront.setImageBitmap(orientedBitmap);

                }
                if (Constants.CAMERA_ONE == 12) {
                    //imgSide.setImageBitmap(onSelectFromGalleryResult(data));

                    String[] filePath = {MediaStore.Images.Media.DATA};
                    Cursor cursor = getContentResolver().query(selectedImage, filePath, null, null, null);
                    cursor.moveToFirst();
                    String imagePath = cursor.getString(cursor.getColumnIndex(filePath[0]));
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                    Bitmap bitmap = BitmapFactory.decodeFile(imagePath, options);
                    cursor.close();
                    Bitmap orientedBitmap = ExifUtil.rotateBitmap(imagePath, bitmap);
                    Constants.bitmapSide = orientedBitmap;
                    imgSide.setImageBitmap(orientedBitmap);
                }
                if (Constants.CAMERA_ONE == 13) {
                    //imgRear.setImageBitmap(onSelectFromGalleryResult(data));

                    String[] filePath = {MediaStore.Images.Media.DATA};
                    Cursor cursor = getContentResolver().query(selectedImage, filePath, null, null, null);
                    cursor.moveToFirst();
                    String imagePath = cursor.getString(cursor.getColumnIndex(filePath[0]));
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                    Bitmap bitmap = BitmapFactory.decodeFile(imagePath, options);
                    cursor.close();
                    Bitmap orientedBitmap = ExifUtil.rotateBitmap(imagePath, bitmap);
                    Constants.bitmapRear = orientedBitmap;
                    imgRear.setImageBitmap(orientedBitmap);
                }

            } else if (requestCode == REQUEST_CAMERA) {
                if (Constants.CAMERA_ONE == 11) {
                    imgFront.setImageBitmap(onCaptureImageResult(data));
                }
                if (Constants.CAMERA_ONE == 12) {
                    imgSide.setImageBitmap(onCaptureImageResult(data));
                }
                if (Constants.CAMERA_ONE == 13) {
                    imgRear.setImageBitmap(onCaptureImageResult(data));
                }
            }
        }
    }

    private Bitmap onSelectFromGalleryResult(Intent data) {
        Bitmap bm = null;
        if (data != null) {
            try {
                bm = MediaStore.Images.Media.getBitmap(context.getContentResolver(), data.getData());

                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                bm.compress(Bitmap.CompressFormat.JPEG, 50, bytes);
                byte[] attachmentBytes = bytes.toByteArray();

                if (Constants.CAMERA_ONE == 11) {
                    //bmFrontPicture=bm;
                    Constants.bitmapFront = bm;
                    byteFrontPic = attachmentBytes;
                } else if (Constants.CAMERA_ONE == 12) {
                    //bmSidePicture=bm;
                    Constants.bitmapSide = bm;
                    byteSidePic = attachmentBytes;
                } else if (Constants.CAMERA_ONE == 13) {
                    //bmRearPicture=bm;
                    Constants.bitmapRear = bm;
                    byteRearPic = attachmentBytes;
                }

            } catch (IOException e) {
                e.printStackTrace();
            }


        }
        return bm;
    }

    private Bitmap onCaptureImageResult(Intent data) {
        Bitmap thumbnail = (Bitmap) data.getExtras().get("data");
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        thumbnail.compress(Bitmap.CompressFormat.JPEG, 50, bytes);

        byte[] attachmentBytes = bytes.toByteArray();
        //imageToBase64String = Base64.encodeToString(attachmentBytes, Base64.DEFAULT);

        if (Constants.CAMERA_ONE == 11) {
            //bmFrontPicture=thumbnail;
            Constants.bitmapFront = thumbnail;
            byteFrontPic = attachmentBytes;
        } else if (Constants.CAMERA_ONE == 12) {
            //bmSidePicture=thumbnail;
            Constants.bitmapSide = thumbnail;
            byteSidePic = attachmentBytes;
        } else if (Constants.CAMERA_ONE == 13) {
            //bmRearPicture=thumbnail;
            Constants.bitmapRear = thumbnail;
            byteRearPic = attachmentBytes;
        }

        File destination = new File(Environment.getExternalStorageDirectory(), System.currentTimeMillis() + ".jpg");
        FileOutputStream fo;
        try {
            destination.createNewFile();
            fo = new FileOutputStream(destination);
            fo.write(bytes.toByteArray());
            fo.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return thumbnail;
    }

    public void becomdDriver() {
        CommonAsynTask becomedriverAsyncTask = new CommonAsynTask(VehicleDoc_Activity.this, Constants.BECOME_DRIVER_URL, getDetails(),
                Constants.REQUEST_TYPE_POST, new AsyncTaskInterface() {
            @Override
            public void onTaskCompleted(JSONObject jsonObject) {


                if (jsonObject != null) {
                    try {
                        if (jsonObject.getString(Constants.RESULT).equals(Constants.SUCCESS)) {
                            CustomDialog customDialog = new CustomDialog();
                            customDialog.setDialogTitle(Constants.REQ_SUBMITTED, new DialogDismisser() {
                                @Override
                                public void dismissDialog(int dismiss) {
                                    finish();
                                    Constants.PERSONAL_VEHICLE_CAMERA_RECALL = false;
                                    Constants.CAMERA_RECALL = false;
                                    Constants.BECOME_DRIVER = true;
                                    Intent intent = new Intent(VehicleDoc_Activity.this, HomeMenu.class);
                                    startActivity(intent);
                                }
                            });
                            customDialog.setDialogDescription(Constants.REQ_SUB_DESCRIPTION);
                            customDialog.show(getSupportFragmentManager(), "Registration");
                            update.setEnabled(true);
                            update.setClickable(true);
                        } else if (jsonObject.getString(Constants.RESULT).equals(Constants.ERROR)) {
                            snackbar = Snackbar.make(layoutMain, "" + jsonObject.getString(Constants.MESSAGE), Snackbar.LENGTH_LONG);
                            View sbView = snackbar.getView();
                            TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
                            sbView.setBackgroundColor(ContextCompat.getColor(context, R.color.snackbar_background));
                            textView.setTextColor(ContextCompat.getColor(context, R.color.white));
                            snackbar.show();
                            update.setEnabled(true);
                            update.setClickable(true);
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    update.setEnabled(true);
                    update.setClickable(true);
                }
            }
        });
        becomedriverAsyncTask.execute();
    }

    private static String getDetails() {
        strDriverLicense = Utilities.encodeToBase64(Constants.thumbnailDriverLicence, Bitmap.CompressFormat.JPEG, 100);
        strCarInsurance = Utilities.encodeToBase64(Constants.bitmapCarproof, Bitmap.CompressFormat.JPEG, 100);
        strVehicaltag = Utilities.encodeToBase64(Constants.bitmapVehicle, Bitmap.CompressFormat.JPEG, 100);
        strRegistration = Utilities.encodeToBase64(Constants.bitmapRegproof, Bitmap.CompressFormat.JPEG, 100);
        strFrontPicture = Utilities.encodeToBase64(Constants.bitmapFront, Bitmap.CompressFormat.JPEG, 100);
        strSidePicture = Utilities.encodeToBase64(Constants.bitmapSide, Bitmap.CompressFormat.JPEG, 100);
        strRearPicture = Utilities.encodeToBase64(Constants.bitmapRear, Bitmap.CompressFormat.JPEG, 100);


        JSONObject objData = new JSONObject();
        try {

            //Log.e("strDriverLicense",""+strDriverLicense);

            objData.putOpt(Constants.DRIVER_ID, sharedPref.getString("userId"));
            objData.putOpt(Constants.DRIVER_LIC_PROOF, strDriverLicense);
            objData.putOpt(Constants.DRIVER_LIC_NUMBER, Constants.strDriverLicenseNo);
            objData.putOpt(Constants.DRIVER_LIC_EXPIRAY_DATE, Constants.strLicenseExpDate);
            objData.putOpt(Constants.CAR_INS_PROOF, strCarInsurance);
            objData.putOpt(Constants.CAR_INS_EXPIRAY_DATE, Constants.strInsuranceExpDate);
            objData.putOpt(Constants.CAR_INS_NUMBER, Constants.strInsuranceNo);
            objData.putOpt(Constants.VEHICAL_TAG_PROOF, strVehicaltag);
            objData.putOpt(Constants.VEHICAL_TAG_NUMBER, Constants.strTagno);
            objData.putOpt(Constants.REGISTER_PROOF, strRegistration);
            objData.putOpt(Constants.VEHICAL_FRONT_PICTURE, strFrontPicture);
            objData.putOpt(Constants.VEHICAL_SIDE_PICTURE, strSidePicture);
            objData.putOpt(Constants.VEHICAL_REAR_PICTURE, strRearPicture);
            objData.putOpt(Constants.SEAT_AVALIABLE, strSeatAvaliable);
            objData.putOpt(Constants.CAR_YEAR, "");
            objData.putOpt("dob", "");
            objData.putOpt(Constants.PERSONAL_EMAIL, "");
            objData.putOpt(Constants.MAILING_ADDRESS, Constants.strMailindaddress);
            objData.putOpt(Constants.CAR_NAME, "");
            objData.putOpt(Constants.CAR_MODEL, "");
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return objData.toString();
    }

    private boolean checkAndRequestPermissions() {
        int permissionCamera = ContextCompat.checkSelfPermission(VehicleDoc_Activity.this, Manifest.permission.CAMERA);

        List<String> listPermissionsNeeded = new ArrayList<>();
        if (permissionCamera != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.CAMERA);
        }

        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(VehicleDoc_Activity.this, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]), REQUEST_ID_MULTIPLE_PERMISSIONS);
            return false;
        }
        return true;
    }

    public static String getEncoded64ImageStringFromBitmap(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 70, stream);
        byte[] byteFormat = stream.toByteArray();
        // get the base 64 string
        String imgString = Base64.encodeToString(byteFormat, Base64.NO_WRAP);

        return imgString;
    }
}