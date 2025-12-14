package com.example.screenshotocr;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        Button btnScan = findViewById(R.id.btnScan);
        Button btnSearch = findViewById(R.id.btnSearch);
        
        btnScan.setOnClickListener(v -> 
            Toast.makeText(this, "Scan feature coming soon!", Toast.LENGTH_SHORT).show()
        );
        
        btnSearch.setOnClickListener(v -> 
            Toast.makeText(this, "Search feature coming soon!", Toast.LENGTH_SHORT).show()
        );
    }
}
