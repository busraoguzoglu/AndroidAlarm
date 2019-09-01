package madsmurfzz.com.takeyourpills;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.os.Process;

public class AlarmDisplay extends AppCompatActivity {

    TextView medicineView;
    ImageView medicineImage;
    Button iTake;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm_display);

        medicineView = (TextView) findViewById(R.id.medicineView);
        medicineImage = (ImageView) findViewById(R.id.medicineImage);
        iTake = (Button) findViewById(R.id.iTake);

        // call setImageIcon method on medicineImage

    }

    public void onTake( View v ){
        this.finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Process.killProcess(Process.myPid());
    }
}
