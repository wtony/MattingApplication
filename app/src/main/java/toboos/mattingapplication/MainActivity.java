package toboos.mattingapplication;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
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


import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Arrays;

import Jama.Matrix;

public class MainActivity extends AppCompatActivity {

    private static int RESULT_LOAD_IMAGE = 1;
    private Boolean[] images = new Boolean[4];
    private int counter = 0;


    private ImageView imgView1;
    private ImageView imgView2;
    private ImageView imgView3;
    private ImageView imgView4;

    private Button takeImage;
    private Button buttonLoadImage;
    private Button resetImages;
    private Button calculateMatting;

    double[][] Aformat = {{1.,0.,0.,0.},
            {0.,1.,0.,0.},
            {0.,0.,1.,0.},
            {1.,0.,0.,0.},
            {0.,1.,0.,0.},
            {0.,0.,1.,0.}};

    double[][][] allA;
    double[][][] allB;
    double[][] allX;

    int STATIC_W, STATIC_H;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imgView1 = (ImageView) findViewById(R.id.imgView1);
        imgView2 = (ImageView) findViewById(R.id.imgView2);
        imgView3 = (ImageView) findViewById(R.id.imgView3);
        imgView4 = (ImageView) findViewById(R.id.imgView4);

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
                imgView1.setImageDrawable(null);
                imgView2.setImageDrawable(null);
                imgView3.setImageDrawable(null);
                imgView4.setImageDrawable(null);
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

                for(int i = 0; i < STATIC_H * STATIC_W; i++){
                    Matrix A = new Matrix(allA[i]);
                    Matrix b = new Matrix(allB[i]);
                    double[][] solved = A.solve(b).getArray();
                    System.out.println(Arrays.deepToString(solved));
                    allX[i][0] = solved[0][0];
                    allX[i][1] = solved[1][0];
                    allX[i][2] = solved[2][0];
                    allX[i][3] = solved[3][0];
                }



                imgView3.setVisibility(View.GONE);
                imgView4.setVisibility(View.GONE);

            }
        });




//        double[][] vals = {{1.,2.,3},{4.,5.,6.},{7.,8.,10.},{9,10,11}};
//
//
//
//
//        Matrix b = Matrix.random(4,1);
//        Matrix x = A.solve(b);
//        System.out.println(x.getArray());
//        System.out.println(Arrays.deepToString(x.getArray()));
//
//        Matrix r = A.times(x).minus(b);
//        double rnorm = r.normInf();
//        System.out.println(rnorm);
        
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        ImageView id = null;


        System.out.println(counter);

        switch(counter) {
            case 0:
                id = imgView1;
                break;
            case 1:
                id = imgView2;
                break;
            case 2:
                id = imgView3;
                break;
            case 3:
                id = imgView4;
                break;
        }

        if(counter!=4) {
            counter++;
            updateImage(id, requestCode, resultCode, data);
        }

    }


    public void updateImage(ImageView imageID, int requestCode, int resultCode, Intent data){
        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data) {
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
            int width = 300;
            int height = Math.round(width / aspectRatio);

            bitmap = Bitmap.createScaledBitmap(
                    bitmap, width, height, false);

            imageID.setImageBitmap(bitmap);

            int[] bitmapPixels = new int[width * height];
            bitmap.getPixels(bitmapPixels, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());

            System.out.println(width*height);
            System.out.println(bitmapPixels.length);
            
            if(counter==1){
                STATIC_H = height;
                STATIC_W = width;
                allA = new double[width*height][6][4];
                allB = new double[width*height][6][1];
                allX = new double[width*height][4];

                for(int i = 0; i < width*height; i++){
                    allA[i] = Aformat;
                }
            }

            if(counter ==4){
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
                        case 1:
                            allA[index][0][3] = -1 * R;
                            allA[index][1][3] = -1 * G;
                            allA[index][2][3] = -1 * B;
                                break;
                        case 2:
                            allA[index][3][3] = -1 * R;
                            allA[index][4][3] = -1 * G;
                            allA[index][5][3] = -1 * B;
                                break;
                        case 3:
                            allB[index][0][0] = R;
                            allB[index][1][0] = G;
                            allB[index][2][0] = B;
                                break;
                        case 4:
                            allB[index][3][0] = R;
                            allB[index][4][0] = G;
                            allB[index][5][0] = B;
                                break;
                    }
            }

            System.out.println("done");
        }
    }
}
