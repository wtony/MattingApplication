package toboos.mattingapplication;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;


import java.nio.IntBuffer;

import Jama.Matrix;

public class MainActivity extends AppCompatActivity {

    private static int RESULT_LOAD_IMAGE = 1;
    private static int RESULT_LOAD_BACKGROUND = 2;
    private int counter = 0;

    private int numImg = 0;

    private boolean firstimage = false;

    private ImageView imgBackA;
    private ImageView imgBackB;
    private ImageView imgCompA;
    private ImageView imgCompB;

    private Button takeImage;
    private Button buttonLoadImage;
    private Button resetImages;
    private Button calculateMatting;
    private Button calculateComposite;
    private Button buttonLoadBackground;
    private Button hardReset;

    private LinearLayout radioLayout;
    private LinearLayout compositeLayout;
    private LinearLayout threeButtons;

    double[][] Aformat = {{1.,0.,0.,0.},
            {0.,1.,0.,0.},
            {0.,0.,1.,0.},
            {1.,0.,0.,0.},
            {0.,1.,0.,0.},
            {0.,0.,1.,0.}};

    double[][][] allA;
    double[][][] allB;
    double[][] allX;

    double[][][] computeA;
    double[][][] computeX;
    double[][] computeB;

    int STATIC_W, STATIC_H;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imgBackA = (ImageView) findViewById(R.id.imgBackA);
        imgBackB = (ImageView) findViewById(R.id.imgBackB);
        imgCompA = (ImageView) findViewById(R.id.imgCompA);
        imgCompB = (ImageView) findViewById(R.id.imgCompB);

        radioLayout = (LinearLayout)findViewById(R.id.buttongroup);

        compositeLayout = (LinearLayout)findViewById(R.id.compositelayout);

        threeButtons = (LinearLayout)findViewById(R.id.threebuttons);

        hardReset = (Button)findViewById(R.id.hardreset);
        hardReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imgBackA.setImageBitmap(null);
                imgBackB.setImageBitmap(null);
                imgCompA.setImageBitmap(null);
                imgCompB.setImageBitmap(null);
                imgBackA.setVisibility(View.VISIBLE);
                imgBackB.setVisibility(View.VISIBLE);
                imgCompB.setVisibility(View.VISIBLE);
                imgCompA.setVisibility(View.VISIBLE);
                threeButtons.setVisibility(View.VISIBLE);
                radioLayout.setVisibility(View.VISIBLE);
                compositeLayout.setVisibility(View.GONE);
            }
        });

        buttonLoadBackground = (Button)findViewById(R.id.buttonLoadBackground);
        buttonLoadBackground.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(
                        Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

                startActivityForResult(i, RESULT_LOAD_BACKGROUND);
            }
        });

        calculateComposite = (Button) findViewById(R.id.CalculateComposite);
        calculateComposite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for(int i = 0; i < STATIC_H * STATIC_W; i++){
                    Matrix A = new Matrix(computeA[i]);
                    Matrix x = new Matrix(computeX[i]);
                    double[][] solved = A.times(x).getArray();
                    computeB[i][0] = solved[0][0];
                    computeB[i][1] = solved[1][0];
                    computeB[i][2] = solved[2][0];
                }

                int[] vector = new int[STATIC_H * STATIC_W];
                for(int i = 0; i < STATIC_W * STATIC_H; i++){
                    int R = (int) computeB[i][0];
                    int G = (int) computeB[i][1];
                    int B = (int) computeB[i][2];

                    R = (R > 255) ? 255 : R;
                    G = (G > 255) ? 255 : G;
                    B = (B > 255) ? 255 : B;

                    vector[i] = getIntFromColor(R,G,B);
                }
                Bitmap bitmap = Bitmap.createBitmap(STATIC_W, STATIC_H, Bitmap.Config.ARGB_8888);
                // vector is your int[] of ARGB
                bitmap.copyPixelsFromBuffer(IntBuffer.wrap(vector));
                imgBackA.setImageBitmap(bitmap);
            }


        });

        buttonLoadImage = (Button) findViewById(R.id.buttonLoadPicture);
        buttonLoadImage.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {

                Intent i = new Intent(
                        Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

                startActivityForResult(i, RESULT_LOAD_IMAGE);
            }
        });

        resetImages = (Button) findViewById(R.id.resetPictures);
        resetImages.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                counter = 0;
                numImg = 0;
                imgBackA.setImageDrawable(null);
                imgBackB.setImageDrawable(null);
                imgCompA.setImageDrawable(null);
                imgCompB.setImageDrawable(null);
            }
        });

        takeImage = (Button) findViewById(R.id.takePicture);
        takeImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });


        calculateMatting = (Button) findViewById(R.id.CalculateMatting);
        calculateMatting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                computeX = new double[STATIC_H*STATIC_W][4][1];
                computeB = new double[STATIC_H*STATIC_W][3];

                for(int i = 0; i < STATIC_H * STATIC_W; i++){
                    Matrix A = new Matrix(allA[i]);
                    Matrix b = new Matrix(allB[i]);
                    double[][] solved = A.solve(b).getArray();
                    allX[i][0] = solved[0][0];
                    allX[i][1] = solved[1][0];
                    allX[i][2] = solved[2][0];
                    allX[i][3] = solved[3][0];
                }

                int[] vector = new int[STATIC_H * STATIC_W];
                int[] alphavector = new int[STATIC_H * STATIC_W];
                for(int i = 0; i < STATIC_W * STATIC_H; i++){
                    int R = (int) allX[i][0];
                    int G = (int) allX[i][1];
                    int B = (int) allX[i][2];

                    R = (R > 255) ? 255 : R ;
                    G = (G > 255) ? 255 : G;
                    B = (B > 255) ? 255 : B;

                    vector[i] = getIntFromColor(R,G,B);

                    float val = (float) allX[i][3];
                    if(val > 1) val = 1;
                    if(val < 0) val = 0;
                    alphavector[i] = getIntFromColor01(val,val,val);

                    computeX[i][0][0] = R;
                    computeX[i][1][0] = G;
                    computeX[i][2][0] = B;
                    computeX[i][3][0] = val;

                }
                Bitmap bitmap = Bitmap.createBitmap(STATIC_W, STATIC_H, Bitmap.Config.ARGB_8888);
                // vector is your int[] of ARGB
                bitmap.copyPixelsFromBuffer(IntBuffer.wrap(vector));
                imgBackA.setImageBitmap(bitmap);

                Bitmap alphabitmap = Bitmap.createBitmap(STATIC_W,STATIC_H, Bitmap.Config.ARGB_8888);
                alphabitmap.copyPixelsFromBuffer(IntBuffer.wrap(alphavector));
                imgBackB.setImageBitmap(alphabitmap);

                imgCompA.setVisibility(View.GONE);
                imgCompB.setVisibility(View.GONE);

                calculateMatting.setVisibility(View.GONE);
                threeButtons.setVisibility(View.GONE);
                radioLayout.setVisibility(View.GONE);

                compositeLayout.setVisibility(View.VISIBLE);

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        ImageView id = null;

        switch(counter) {
            case 0:
                id = imgBackA;
                break;
            case 1:
                id = imgBackB;
                break;
            case 2:
                id = imgCompA;
                break;
            case 3:
                id = imgCompB;
                break;
        }

            updateImage(id, requestCode, resultCode, data);

    }


    public void updateImage(ImageView imageID, int requestCode, int resultCode, Intent data){
        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data) {
            if(imageID.getDrawable()==null)
                numImg++;

            Uri selectedImage = data.getData();
            String[] filePathColumn = { MediaStore.Images.Media.DATA };

            Cursor cursor = getContentResolver().query(selectedImage,
                    filePathColumn, null, null, null);
            cursor.moveToFirst();

            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String picturePath = cursor.getString(columnIndex);
            cursor.close();

            Bitmap bitmap = BitmapFactory.decodeFile(picturePath);

            float aspectRatio = bitmap.getWidth() /
                    (float) bitmap.getHeight();
            int width = 500;
            int height = Math.round(width / aspectRatio);

            bitmap = Bitmap.createScaledBitmap(
                    bitmap, width, height, false);

            imageID.setImageBitmap(bitmap);

            int[] bitmapPixels = new int[width * height];
            bitmap.getPixels(bitmapPixels, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());

            System.out.println(width*height);
            System.out.println(bitmapPixels.length);

            if(!firstimage){
                STATIC_H = height;
                STATIC_W = width;
                allA = new double[width*height][6][4];
                allB = new double[width*height][6][1];
                allX = new double[width*height][4];
                computeA = new double[width*height][3][4];

                double[][] smallA =  {{1.,0.,0.,0.},{0.,1.,0.,0.},{0.,0.,1.,0.}};
                for(int i = 0; i < width*height; i++){
                    allA[i] = Aformat;
                    computeA[i] = smallA;
                }
                firstimage = true;
            }

            if(numImg ==4){
                System.out.println("In");
                calculateMatting.setVisibility(View.VISIBLE);
                takeImage.setVisibility(View.GONE);
                resetImages.setVisibility(View.GONE);
                buttonLoadImage.setVisibility(View.GONE);
            }

            for(int index = 0; index< bitmapPixels.length; index++){

                    int R = (bitmapPixels[index] >> 16) & 0xff;     //bitwise shifting
                    int G = (bitmapPixels[index] >> 8) & 0xff;
                    int B = bitmapPixels[index] & 0xff;


                    switch(counter){
                        case 0:
                            allA[index][0][3] = -1 * R;
                            allA[index][1][3] = -1 * G;
                            allA[index][2][3] = -1 * B;
                                break;
                        case 1:
                            allA[index][3][3] = -1 * R;
                            allA[index][4][3] = -1 * G;
                            allA[index][5][3] = -1 * B;
                                break;
                        case 2:
                            allB[index][0][0] = R;
                            allB[index][1][0] = G;
                            allB[index][2][0] = B;
                                break;
                        case 3:
                            allB[index][3][0] = R;
                            allB[index][4][0] = G;
                            allB[index][5][0] = B;
                                break;
                    }

            }

            System.out.println("done");
        }else if(requestCode == RESULT_LOAD_BACKGROUND && resultCode == RESULT_OK && null != data){
            Uri selectedImage = data.getData();
            String[] filePathColumn = { MediaStore.Images.Media.DATA };

            Cursor cursor = getContentResolver().query(selectedImage,
                    filePathColumn, null, null, null);
            cursor.moveToFirst();

            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String picturePath = cursor.getString(columnIndex);
            cursor.close();

            Bitmap bitmap = BitmapFactory.decodeFile(picturePath);

            bitmap = Bitmap.createScaledBitmap(
                    bitmap, STATIC_W, STATIC_H, false);

            int[] bitmapPixels = new int[STATIC_W * STATIC_H];
            bitmap.getPixels(bitmapPixels, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());

            imgCompA.setImageBitmap(bitmap);
            imgCompA.setVisibility(View.VISIBLE);

            for(int index = 0; index< bitmapPixels.length; index++) {
                int R = (bitmapPixels[index] >> 16) & 0xff;     //bitwise shifting
                int G = (bitmapPixels[index] >> 8) & 0xff;
                int B = bitmapPixels[index] & 0xff;
                computeA[index][0][3] = -R;
                computeA[index][1][3] = -G;
                computeA[index][2][3] = -B;
            }
        }
    }

    public int getIntFromColor(int Red, int Green, int Blue){
        Red = (Red << 16) & 0x00FF0000; //Shift red 16-bits and mask out other stuff
        Green = (Green << 8) & 0x0000FF00; //Shift Green 8-bits and mask out other stuff
        Blue = Blue & 0x000000FF; //Mask out anything not blue.

        return 0xFF000000 | Red | Green | Blue; //0xFF000000 for 100% Alpha. Bitwise OR everything together.
    }

    public int getIntFromColor01(float Red, float Green, float Blue){
        int R = Math.round(255 * Red);
        int G = Math.round(255 * Green);
        int B = Math.round(255 * Blue);

        R = (R << 16) & 0x00FF0000;
        G = (G << 8) & 0x0000FF00;
        B = B & 0x000000FF;

        return 0xFF000000 | R | G | B;
    }

    public void onRadioButtonClicked(View view) {
        // Is the button now checked?
        boolean checked = ((RadioButton) view).isChecked();

        // Check which radio button was clicked
        switch(view.getId()) {
            case R.id.radioBackA:
                if (checked)
                    counter = 0;
                    break;
            case R.id.radioBackB:
                if (checked)
                    counter = 1;
                    break;
            case R.id.radioCompA:
                if (checked)
                    counter = 2;
                    break;
            case R.id.radioCompB:
                    counter = 3;
                    break;
        }
    }

}
