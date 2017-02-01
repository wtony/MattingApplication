package toboos.mattingapplication;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;


import java.util.Arrays;

import Jama.Matrix;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        double[][] vals = {{1.,2.,3},{4.,5.,6.},{7.,8.,10.},{9,10,11}};
        Matrix A = new Matrix(vals);
        Matrix b = Matrix.random(4,1);
        Matrix x = A.solve(b);
        System.out.println(x.getArray());
        System.out.println(Arrays.deepToString(x.getArray()));

        Matrix r = A.times(x).minus(b);
        double rnorm = r.normInf();
        System.out.println(rnorm);

        
    }
}
