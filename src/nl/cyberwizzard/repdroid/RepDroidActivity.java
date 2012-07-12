package nl.cyberwizzard.repdroid;

import nl.cyberwizzard.repdroid.R;
import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.View;

public class RepDroidActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rep_droid);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_rep_droid, menu);
        return true;
    }
    
    public void onClickLoad(View view) {
    	String file = "3mm_Hole_Test.gcode";
    	try {
    		// Open the file
			GCodeParser.openFile(file);
			// Index the file by layer and byte offset
			GCodeParser.indexFile();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }

    
}
