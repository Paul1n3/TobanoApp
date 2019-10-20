package net.tobano.quitsmoking.app;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.flurry.android.FlurryAgent;

import net.tobano.quitsmoking.app.util.Theme;

/**
 * Created by calla on 10/05/2017.
 */

public class Personalization extends Fragment {

//    private final int IMAGE_CAMERA = 0;
//    private final int IMAGE_FOLDER = 1;

//    private Uri selectedImage;

    private FloatingActionButton btnGreen;
    private FloatingActionButton btnBlue;
//    private ImageView wallpaper;
//    private FloatingActionButton btnCamera;
//    private FloatingActionButton btnFolder;
//    private Button btnApply;

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = (RelativeLayout) inflater.inflate(R.layout.personalization,
                container, false);

//        wallpaper = (ImageView) v.findViewById(R.id.ivwallaper);
        btnGreen = (FloatingActionButton) v.findViewById(R.id.btnGreen);
        btnBlue = (FloatingActionButton) v.findViewById(R.id.btnBlue);
//        btnCamera = (FloatingActionButton) v.findViewById(R.id.btnCamera);
//        btnFolder = (FloatingActionButton) v.findViewById(R.id.btnFolder);
//        btnApply = (Button) v.findViewById(R.id.btnApply);

        btnGreen.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                changeTheme(Theme.GREEN, "Add_button_personalize_theme_green");
            }
        });

        btnBlue.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                changeTheme(Theme.BLUE, "Add_button_personalize_theme_blue");
            }
        });

//        btnApply.setOnClickListener(new View.OnClickListener() {
//            public void onClick(View v) {
//                ((Start)getActivity()).playSound(Start.strClick);
//                ((Start)getActivity()).saveWallpaper(selectedImage);
//                FlurryAgent.logEvent("Add_button_personalize_apply");
//            }
//        });

//        btnCamera.setOnClickListener(new View.OnClickListener() {
//            public void onClick(View v) {
//                ((Start)getActivity()).playSound(Start.strClick);
//                FlurryAgent.logEvent("image_camera");
//                dispatchTakePictureIntent();
//            }
//        });
//        btnFolder.setOnClickListener(new View.OnClickListener() {
//            public void onClick(View v) {
//                ((Start)getActivity()).playSound(Start.strClick);
//                FlurryAgent.logEvent("image_folder");
//                Intent pickPhoto = new Intent(Intent.ACTION_PICK,
//                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
//                startActivityForResult(pickPhoto , IMAGE_FOLDER);//one can be replaced with any action code
//            }
//        });

        if (container == null) {
            return null;
        }
        return v;
    }

    private void changeTheme(Theme t, String log) {
        ((Start)getActivity()).playSound(Start.strClick);
        ((Start)getActivity()).changeTheme(t);
        FlurryAgent.logEvent(log);
        ((Willpower) ((Start) getActivity()).getFragment(Start.FRAGMENT_WILLPOWER))
                .setTheme();
    }

//    @Override
//    public void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
//        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);
//        switch(requestCode) {
//            case IMAGE_CAMERA:
//                if(resultCode == RESULT_OK){
//                    selectedImage = imageReturnedIntent.getData();
//                    Bundle extras = imageReturnedIntent.getExtras();
//                    Bitmap imageBitmap = (Bitmap) extras.get("data");
//                    wallpaper.setImageBitmap(imageBitmap);
//
////                    selectedImage = imageReturnedIntent.getData();
////                    System.out.println("the image is "+selectedImage);
////                    wallpaper.setImageURI(selectedImage);
//                }
//                break;
//            case IMAGE_FOLDER:
//                if(resultCode == RESULT_OK){
//                    selectedImage = imageReturnedIntent.getData();
//                    wallpaper.setImageURI(selectedImage);
//                }
//                break;
//        }
//    }

//    private void dispatchTakePictureIntent() {
//        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//        // Ensure that there's a camera activity to handle the intent
//        if (takePictureIntent.resolveActivity(getContext().getPackageManager()) != null) {
//            // Create the File where the photo should go
//            File photoFile = null;
//            try {
//                photoFile = createImageFile();
//            } catch (IOException ex) {
//                // Error occurred while creating the File
//            }
//            // Continue only if the File was successfully created
//            if (photoFile != null) {
//                int resourceId = getResources().getIdentifier("wallpaper", "drawable", getContext().getPackageName());
//                Uri photoURI = FileProvider.getUriForFile(getContext(),
//                        getContext().getResources().getResourcePackageName(resourceId),
//                        photoFile);
//                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
//                startActivityForResult(takePictureIntent, IMAGE_CAMERA);
//            }
//        }
//    }

//    private File createImageFile() throws IOException {
//        // Create an image file name
//        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
//        String imageFileName = "JPEG_" + timeStamp + "_";
//        File storageDir = getContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
//        File image = File.createTempFile(
//                imageFileName,  /* prefix */
//                ".jpg",         /* suffix */
//                storageDir      /* directory */
//        );
//        // Save a file: path for use with ACTION_VIEW intents
//        selectedImage = Uri.fromFile(image);
//        return image;
//    }
}
