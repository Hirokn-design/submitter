package com.ase.submitter;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import com.ase.submitter.databinding.ActivityMainBinding;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import java.util.ArrayList;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity {
    ListView listViewContacts;
    ArrayAdapter<String> adapter;
    ArrayList<String> contacts;

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listViewContacts = findViewById(R.id.listViewContacts);
        // Initialisation de la liste
        contacts = new ArrayList<>(Arrays.asList(
                "Alice Martin",
                "Bob Dupont",
                "Claire Bernard",
                "David Leroy",
                "Emma Simon"
        ));
        // Création de l'adaptateur
        adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                contacts
        );
        listViewContacts.setAdapter(adapter);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());




        binding.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, FormulaireActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        return  false;
    }
}