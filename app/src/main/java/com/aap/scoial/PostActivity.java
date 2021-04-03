package com.aap.scoial;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PostActivity extends AppCompatActivity implements LocationListener {
    EditText textName, textComplain, textLocation;
    TextView textTime;
    Button btnBrowse, btnUpload,btnLocation;
    ImageView imageTask;
    Uri FilePathUri;
    StorageReference storageReference;
    DatabaseReference databaseReference, refName;
    int Image_Request_Code = 7;
    ProgressDialog progressDialog ;
    FirebaseAuth mAuth;

    private ArrayList<String> permissionsToRequest;
    private ArrayList permissionsRejected = new ArrayList();
    private ArrayList permissions = new ArrayList();
    private final static int ALL_PERMISSIONS_RESULT = 101;
    LocationTrack locationTrack;
    LocationManager locationManager;
    String name, uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);
        textName = findViewById(R.id.txtName);
        textComplain = findViewById(R.id.txtDesc);
        textLocation = findViewById(R.id.txtLocation);
        textTime = findViewById(R.id.textTime);
        btnBrowse = findViewById(R.id.btnbrowse);
        btnLocation = findViewById(R.id.btnLocation);
        btnUpload = findViewById(R.id.btnupload);
        imageTask=  findViewById(R.id.image_view);
        storageReference = FirebaseStorage.getInstance().getReference("Posts");
        databaseReference = FirebaseDatabase.getInstance().getReference("Posts");
        progressDialog = new ProgressDialog(PostActivity.this);

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm a");
        String time = simpleDateFormat.format(new Date());
        textTime.setText(time);
        mAuth = FirebaseAuth.getInstance();

        String userId = mAuth.getCurrentUser().getUid();
        Toast.makeText(PostActivity.this, ""+userId, Toast.LENGTH_SHORT).show();
         refName = FirebaseDatabase.getInstance().getReference("Users");

         refName.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
             @Override
             public void onDataChange(DataSnapshot dataSnapshot) {
                 if (dataSnapshot.hasChild("name")){
                     name = dataSnapshot.child("name").getValue().toString();
                    // Toast.makeText(PostActivity.this, ""+name, Toast.LENGTH_SHORT).show();
                     textName.setText(name);
                 }

             }

             @Override
             public void onCancelled(DatabaseError databaseError) {

             }
         });

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);



        btnBrowse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Image"), Image_Request_Code);
            }
        });
        imageTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Image"), Image_Request_Code);
            }
        });

        btnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                UploadData();
            }
        });
        btnLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                locationTrack = new LocationTrack(PostActivity.this);

                locationEnabled();
                getLocation();
//                if (locationTrack.canGetLocation()) {
//
//
//                    double longitude = locationTrack.getLongitude();
//                    double latitude = locationTrack.getLatitude();
//                    Toast.makeText(locationTrack, ""+longitude + latitude, Toast.LENGTH_SHORT).show();
//                    //       textLocation.setText("Longitude:" + Double.toString(longitude) + "\nLatitude:" + Double.toString(latitude));
//                    getCompleteAddressString(latitude,longitude);
//                } else {
//
//                    locationTrack.showSettingsAlert();
//                }
            }
        });

    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Image_Request_Code && resultCode == RESULT_OK && data != null && data.getData() != null) {

            FilePathUri = data.getData();

            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), FilePathUri);
                imageTask.setImageBitmap(bitmap);
            }
            catch (IOException e) {

                e.printStackTrace();
            }
        }
    }

    public String GetFileExtension(Uri uri) {

        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri)) ;

    }

    private void UploadData() {

        if (FilePathUri != null) {

            progressDialog.setTitle("Your Complaint is Uploading...");
            progressDialog.show();
            StorageReference storageReference2 = storageReference.child(System.currentTimeMillis() + "." + GetFileExtension(FilePathUri));
            storageReference2.putFile(FilePathUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Task<Uri> downloadUrl = taskSnapshot.getStorage().getDownloadUrl().
                                    addOnCompleteListener(new OnCompleteListener<Uri>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Uri> task) {
                                            String eName = textName.getText().toString().trim();
                                            String complain = textComplain.getText().toString().trim();
                                            String time = textTime.getText().toString().trim();

                                            String location = textLocation.getText().toString().trim();
                                            progressDialog.dismiss();
                                            startActivity(new Intent(PostActivity.this,MainActivity.class));
                                            Toast.makeText(getApplicationContext(), "Your Post Uploaded Successfully ", Toast.LENGTH_LONG).show();
                                            uploadPost post = new uploadPost(name,task.getResult().toString(),complain,time,location);
                                            String ImageUploadId = databaseReference.push().getKey();
                                            databaseReference.child(ImageUploadId).setValue(post);
                                        }
                                    });
                        }
                    });
        }else if (FilePathUri == null){
            Toast.makeText(locationTrack, "Please Select Image", Toast.LENGTH_SHORT).show();
        }


    }

    ///////-----------GET LOCATION--------************************//////////---------

//    private String getCompleteAddressString(double LATITUDE, double LONGITUDE) {
//        String strAdd = "";
//        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
//        try {
//            List<Address> addresses = geocoder.getFromLocation(LATITUDE, LONGITUDE, 1);
//            if (addresses != null) {
//                Address returnedAddress = addresses.get(0);
//                StringBuilder strReturnedAddress = new StringBuilder("");
//
//                for (int i = 0; i <= returnedAddress.getMaxAddressLineIndex(); i++) {
//                    strReturnedAddress.append(returnedAddress.getAddressLine(i)).append("\n");
//                }
//                strAdd = strReturnedAddress.toString();
//                Toast.makeText(locationTrack, ""+strAdd, Toast.LENGTH_SHORT).show();
//                textLocation.setText(strAdd);
//                Toast.makeText(this, strReturnedAddress.toString(), Toast.LENGTH_LONG).show();
//                //Log.w("My Current loction address", strReturnedAddress.toString());
//            } else {
//                // Log.w("My Current loction address", "No Address returned!");
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//            Toast.makeText(locationTrack, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
//            //Log.w("My Current loction address", "Canont get Address!");
//        }
//        return strAdd;
//    }
//    private ArrayList findUnAskedPermissions(ArrayList<String> wanted) {
//        ArrayList result = new ArrayList();
//
//        for (String perm : wanted) {
//            if (!hasPermission(perm)) {
//                result.add(perm);
//            }
//        }
//
//        return result;
//    }
//
//    private boolean hasPermission(String permission) {
//        if (canMakeSmores()) {
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                return (checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED);
//            }
//        }
//        return true;
//    }
//
//    private boolean canMakeSmores() {
//        return (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1);
//    }
//
//
//    @TargetApi(Build.VERSION_CODES.M)
//    @Override
//    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
//
//        switch (requestCode) {
//
//            case ALL_PERMISSIONS_RESULT:
//                for (String perms : permissionsToRequest) {
//                    if (!hasPermission(perms)) {
//                        permissionsRejected.add(perms);
//                    }
//                }
//
//                if (permissionsRejected.size() > 0) {
//
//
//                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                        if (shouldShowRequestPermissionRationale((String) permissionsRejected.get(0))) {
//                            showMessageOKCancel("These permissions are mandatory for the application. Please allow access.",
//                                    new DialogInterface.OnClickListener() {
//                                        @Override
//                                        public void onClick(DialogInterface dialog, int which) {
//                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                                                requestPermissions((String[]) permissionsRejected.toArray(new String[permissionsRejected.size()]), ALL_PERMISSIONS_RESULT);
//                                            }
//                                        }
//                                    });
//                            return;
//                        }
//                    }
//
//                }
//
//                break;
//        }
//
//    }
//
//    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
//        new AlertDialog.Builder(PostActivity.this)
//                .setMessage(message)
//                .setPositiveButton("OK", okListener)
//                .setNegativeButton("Cancel", null)
//                .create()
//                .show();
//    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(locationTrack!=null) {
            locationTrack.stopListener();
        }
    }
    private void locationEnabled() {
        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        boolean gps_enabled = false;
        boolean network_enabled = false;
        try {
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (!gps_enabled && !network_enabled) {
            new AlertDialog.Builder(PostActivity.this)
                    .setTitle("Enable GPS Service")
                    .setMessage("We need your GPS location to show Near Places around you.")
                    .setCancelable(false)
                    .setPositiveButton("Enable", new
                            DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                                    startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                                }
                            })
                    .setNegativeButton("Cancel", null)
                    .show();
        }
    }

    void getLocation() {
        try {
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 500, 5, (LocationListener) this);
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }



    @Override
    public void onLocationChanged(@NonNull Location location) {
        try {
            Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);

            textLocation.setText(addresses.get(0).getLocality()+ ", "+addresses.get(0).getCountryName());
//            tvState.setText(addresses.get(0).getAdminArea());
//            tvCountry.setText(addresses.get(0).getCountryName());
//            tvPin.setText(addresses.get(0).getPostalCode());
//            tvLocality.setText(addresses.get(0).getAddressLine(0));

        } catch (Exception e) {
        }

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(@NonNull String provider) {

    }

    @Override
    public void onProviderDisabled(@NonNull String provider) {

    }
}