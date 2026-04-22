package com.ase.submitter;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    // 1. Déclarer les launchers AU DÉBUT de la classe
    private final ActivityResultLauncher<String> galerieLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    ImageView imageProfil = findViewById(R.id.image_profil);
                    imageProfil.setImageURI(uri);
                }
            }
    );

    private final ActivityResultLauncher<Void> cameraLauncher = registerForActivityResult(
            new ActivityResultContracts.TakePicturePreview(),
            bitmap -> {
                if (bitmap != null) {
                    ImageView imageProfil = findViewById(R.id.image_profil);
                    imageProfil.setImageBitmap(bitmap);
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ImageView imageProfil = findViewById(R.id.image_profil);

        // 2. Configurer le clic pour proposer les deux options
        imageProfil.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String[] choix = {"Choisir dans la Galerie", "Prendre une Photo"};

                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Photo de profil");
                builder.setItems(choix, (dialog, which) -> {
                    if (which == 0) {
                        galerieLauncher.launch("image/*");
                    } else {
                        cameraLauncher.launch(null);
                    }
                });
                builder.show();
            }
        });
    }
}