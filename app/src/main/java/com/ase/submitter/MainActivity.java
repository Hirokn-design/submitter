package com.ase.submitter;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;

import com.ase.submitter.databinding.ActivityMainBinding;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    private ContactAdapter adapter;
    private ArrayList<Contact> contacts; // Liste affichée (filtrée)
    private ArrayList<Contact> contactsFull; // Liste source complète

    private static final String PREFS_NAME = "ContactsPrefs";
    private static final String KEY_CONTACTS = "contacts_list";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // 1. Chargement des données sauvegardées
        contactsFull = loadContacts();
        if (contactsFull.isEmpty()) {
            // Contacts par défaut si la liste est vide au premier lancement
            contactsFull.add(new Contact("Alice Martin", "0601020304", "alice@email.com"));
            contactsFull.add(new Contact("Gérard Dupont", "0611223344", "gerard@email.com"));
            saveContacts();
        }
        contacts = new ArrayList<>(contactsFull);
        sortContacts();

        // 2. Configuration Adapter et Vue Vide
        adapter = new ContactAdapter(this, contacts);
        binding.listViewContacts.setAdapter(adapter);
        binding.listViewContacts.setEmptyView(binding.tvEmpty); // Affiche tvEmpty si la liste est vide

        // 3. Barre de recherche (Filtrage)
        binding.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) { return false; }

            @Override
            public boolean onQueryTextChange(String newText) {
                filter(newText);
                return true;
            }
        });

        // 4. Bouton Ajouter (Dialogue)
        binding.btnAjouter.setOnClickListener(v -> showAddDialog());

        // 5. FAB
        binding.fab.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, FormulaireActivity.class);
            startActivity(intent);
        });

        // 6. Clics sur les items
        binding.listViewContacts.setOnItemClickListener((parent, view, position, id) -> {
            Contact selectedContact = contacts.get(position);
            Toast.makeText(MainActivity.this, "Contact : " + selectedContact.getNom(), Toast.LENGTH_SHORT).show();
        });

        binding.listViewContacts.setOnItemLongClickListener((parent, view, position, id) -> {
            Contact selectedContact = contacts.get(position);
            new AlertDialog.Builder(MainActivity.this)
                    .setTitle(selectedContact.getNom())
                    .setMessage("Tél : " + selectedContact.getTelephone() + "\nEmail : " + selectedContact.getEmail())
                    .setPositiveButton("Fermer", null)
                    .show();
            return true;
        });
    }

    private void showAddDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_ajout_contact, null);
        EditText etNom = dialogView.findViewById(R.id.etNom);
        EditText etTel = dialogView.findViewById(R.id.etTelephone);
        EditText etMail = dialogView.findViewById(R.id.etEmail);

        new AlertDialog.Builder(this)
                .setTitle("Nouveau contact")
                .setView(dialogView)
                .setPositiveButton("Ajouter", (dialog, which) -> {
                    String nom = etNom.getText().toString().trim();
                    if (!nom.isEmpty()) {
                        Contact newContact = new Contact(nom, etTel.getText().toString().trim(), etMail.getText().toString().trim());
                        contactsFull.add(newContact);
                        saveContacts();
                        filter(binding.searchView.getQuery().toString()); // Rafraîchir l'affichage
                    }
                })
                .setNegativeButton("Annuler", null)
                .show();
    }

    private void filter(String text) {
        contacts.clear();
        if (text.isEmpty()) {
            contacts.addAll(contactsFull);
        } else {
            String query = text.toLowerCase();
            for (Contact contact : contactsFull) {
                if (contact.getNom().toLowerCase().contains(query)) {
                    contacts.add(contact);
                }
            }
        }
        sortContacts();
    }

    private void sortContacts() {
        Collections.sort(contacts, (c1, c2) -> c1.getNom().compareToIgnoreCase(c2.getNom()));
        Collections.sort(contactsFull, (c1, c2) -> c1.getNom().compareToIgnoreCase(c2.getNom()));
        if (adapter != null) adapter.notifyDataSetChanged();
    }

    // --- Gestion de la sauvegarde (SharedPreferences + JSON) ---
    private void saveContacts() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        JSONArray jsonArray = new JSONArray();
        try {
            for (Contact c : contactsFull) {
                JSONObject obj = new JSONObject();
                obj.put("nom", c.getNom());
                obj.put("tel", c.getTelephone());
                obj.put("mail", c.getEmail());
                jsonArray.put(obj);
            }
            prefs.edit().putString(KEY_CONTACTS, jsonArray.toString()).apply();
        } catch (JSONException e) { e.printStackTrace(); }
    }

    private ArrayList<Contact> loadContacts() {
        ArrayList<Contact> list = new ArrayList<>();
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String json = prefs.getString(KEY_CONTACTS, null);
        if (json == null) return list;
        try {
            JSONArray jsonArray = new JSONArray(json);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject obj = jsonArray.getJSONObject(i);
                list.add(new Contact(obj.getString("nom"), obj.getString("tel"), obj.getString("mail")));
            }
        } catch (JSONException e) { e.printStackTrace(); }
        return list;
    }

    public static class Contact {
        private final String nom, telephone, email;
        public Contact(String nom, String telephone, String email) {
            this.nom = nom; this.telephone = telephone; this.email = email;
        }
        public String getNom() { return nom; }
        public String getTelephone() { return telephone; }
        public String getEmail() { return email; }
    }

    public class ContactAdapter extends ArrayAdapter<Contact> {
        public ContactAdapter(Context context, ArrayList<Contact> list) { super(context, 0, list); }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_contact, parent, false);
            }
            Contact contact = getItem(position);
            TextView tvInitiale = convertView.findViewById(R.id.tvInitiale);
            TextView tvNom = convertView.findViewById(R.id.tvNom);
            TextView tvTel = convertView.findViewById(R.id.tvTelephone);
            TextView tvMail = convertView.findViewById(R.id.tvEmail);
            ImageView imgDelete = convertView.findViewById(R.id.imgDelete);

            tvNom.setText(contact.getNom());
            tvTel.setText(contact.getTelephone());
            tvMail.setText(contact.getEmail());
            char initial = contact.getNom().toUpperCase().charAt(0);
            tvInitiale.setText(String.valueOf(initial));

            // Couleur avatar dynamique
            int color = (initial >= 'A' && initial <= 'F') ? Color.parseColor("#2196F3") :
                        (initial >= 'G' && initial <= 'M') ? Color.parseColor("#4CAF50") : Color.parseColor("#FF9800");
            tvInitiale.getBackground().mutate().setTint(color);

            imgDelete.setOnClickListener(v -> {
                contactsFull.remove(contact);
                contacts.remove(contact);
                saveContacts();
                notifyDataSetChanged();
            });

            return convertView;
        }
    }
}