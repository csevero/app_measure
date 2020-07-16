package com.severocarlos.teste_integracao_firebase;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.severocarlos.teste_integracao_firebase.models.Item;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_LOCATION_PERMISSION = 1;
    private List<Item> items;

    private Button btnFinish;
    private TextView txtCount;
    private ImageView imgInfo;

    private FirebaseDatabase database;
    private DatabaseReference reference;

    private String startLocation;
    private String endLocation;
    private boolean active = true;

    private MainAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnAdd = findViewById(R.id.button_add);
        btnFinish = findViewById(R.id.button_finish);
        txtCount = findViewById(R.id.text_count);
        imgInfo = findViewById(R.id.img_info);

        items = new ArrayList<>();
        txtCount.setText(getString(R.string.main_count, items.size()));

        buildRecycler();

        imgInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogInfo();
            }
        });

        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogFormAdd();
            }
        });

        btnFinish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogFormFinish();
            }
        });
    }

    public void buttonEnabled(boolean enable) {
        if (enable) {
            btnFinish.setEnabled(true);
            btnFinish.setBackgroundResource(R.drawable.bg_button_enabled);
        } else {
            btnFinish.setEnabled(false);
            btnFinish.setBackgroundResource(R.drawable.bg_button_disabled);
        }
    }

    //DIALOG MOSTRANDO COMO USAR
    public void dialogInfo() {
        AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);
        final View customLayout = getLayoutInflater().inflate(R.layout.layout_dialog_info, null);
        alert.setView(customLayout);

        alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        alert.create();
        alert.show();
    }

    //DIALOG PARA ADICIONAR NOVOS DADOS
    public void dialogFormAdd() {
        AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);

        final View customLayout = getLayoutInflater().inflate(R.layout.layout_dialog, null);
        alert.setView(customLayout);

        alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                EditText measure = customLayout.findViewById(R.id.edit_measure);
                String response = measure.getText().toString();

                if (response.isEmpty() || response.startsWith("0")) {
                    Toast.makeText(MainActivity.this, "A medida não pode ser vazia",
                            Toast.LENGTH_SHORT).show();
                } else {
                    items.add(new Item(Float.parseFloat(response)));
                    buttonEnabled(true);

                    mAdapter.notifyItemInserted(items.size());

                    if (active) {
                        verifyPermission();
                    }
                }

            }
        });

        AlertDialog dialog = alert.create();
        dialog.show();
    }

    //DIALOG PARA FINALIZAR
    public void dialogFormFinish() {
        AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);

        final View customLayout = getLayoutInflater().inflate(R.layout.layout_dialog_finish, null);
        alert.setView(customLayout);

        alert.setPositiveButton("Finalizar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                verifyPermission();
            }
        });

        alert.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        AlertDialog dialog = alert.create();
        dialog.show();
    }

    public void verifyPermission() {
        if (ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_CODE_LOCATION_PERMISSION);
        } else {
            getLocation();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_LOCATION_PERMISSION && grantResults.length > 0) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLocation();
            }
        } else {
            Toast.makeText(MainActivity.this, getString(R.string.loc_denied), Toast.LENGTH_SHORT).show();
        }
    }

    public void getLocation() {
        final LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(3000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationServices.getFusedLocationProviderClient(MainActivity.this)
                .requestLocationUpdates(locationRequest, new LocationCallback() {

                    @Override
                    public void onLocationResult(LocationResult locationResult) {
                        super.onLocationResult(locationResult);
                        LocationServices.getFusedLocationProviderClient(MainActivity.this)
                                .removeLocationUpdates(this);
                        if (locationResult != null && locationResult.getLocations().size() > 0) {
                            int latestLocation = locationResult.getLocations().size() - 1;
                            String location = "";
                            double lat =
                                    locationResult.getLocations().get(latestLocation).getLatitude();
                            double lon =
                                    locationResult.getLocations().get(latestLocation).getLongitude();

                            if (active) {
                                startLocation = String.format("Latitute: %s Longitude: %s",
                                        lat, lon);
                                Log.i("startlocation:", startLocation);
                                active = false;
                            } else {
                                endLocation = String.format("Latitute: %s Longitude: %s", lat
                                        , lon);
                                Log.i("endlocation:", endLocation);
                                active = true;
                            }

                            if (startLocation != null && endLocation != null) {
                                Date dataAtual = new Date();
                                String dataFormat =
                                        new SimpleDateFormat("dd_MM_yyyy HH:mm:ss").format(dataAtual);
                                location = String.format("Inicio: %s_Final: %s", startLocation,
                                        endLocation);
                                location = location.replace(".", ",");
                                Log.i("locationfinal", location);

                                database = FirebaseDatabase.getInstance();
                                reference = database.getReference("medida");

                                reference.child(dataFormat).child(location).setValue(items.toString())
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Toast.makeText(MainActivity.this,
                                                        getString(R.string.success),
                                                        Toast.LENGTH_SHORT).show();
                                            }
                                        });

                                items.clear();
                                mAdapter.notifyDataSetChanged();
                                buttonEnabled(false);
                                txtCount.setText(getString(R.string.main_count, items.size()));

                                startLocation = null;
                                endLocation = null;
                            }
                        }
                    }
                }, Looper.getMainLooper());
    }


    //BÁSICO DA RECYCLERVIEW
    public void buildRecycler() {
        mAdapter = new MainAdapter(items);
        RecyclerView mRecyclerView = findViewById(R.id.recycler_main);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);

        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mAdapter);

        mAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onDeleteItem(int position) {
                items.remove(position);
                txtCount.setText(getString(R.string.main_count, items.size()));
                mAdapter.notifyDataSetChanged();

                if (items.size() == 0) {
                    btnFinish.setEnabled(false);
                    btnFinish.setBackgroundResource(R.drawable.bg_button_disabled);
                }
            }
        });
    }

    //ABAIXO SÃO OS ITENS REFERENTE A RECYCLERVIEW (CLIQUES), ETC
    public interface OnItemClickListener {
        void onDeleteItem(int position);
    }

    class MainHolder extends RecyclerView.ViewHolder {

        private TextView txtCount;
        private TextView txtMain;

        MainHolder(@NonNull View itemView, final OnItemClickListener listener) {
            super(itemView);

            txtCount = findViewById(R.id.text_count);
            ImageButton imgButton = itemView.findViewById(R.id.button_del);
            txtMain = itemView.findViewById(R.id.text_main);

            imgButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            listener.onDeleteItem(position);
                        }
                    }
                }
            });
        }
    }

    public class MainAdapter extends RecyclerView.Adapter<MainHolder> {
        private List<Item> items;
        private OnItemClickListener mListener;

        MainAdapter(List<Item> items) {
            this.items = items;
        }

        void setOnItemClickListener(OnItemClickListener mListener) {
            this.mListener = mListener;
        }

        @NonNull
        @Override
        public MainHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = getLayoutInflater().inflate(R.layout.item_list, parent, false);
            return new MainHolder(v, mListener);
        }

        @Override
        public void onBindViewHolder(@NonNull MainHolder holder, int position) {
            Item item = items.get(position);
            holder.txtMain.setText(getString(R.string.item_medida, item.getMedida()));
            holder.txtCount.setText(getString(R.string.main_count, items.size()));
        }

        @Override
        public int getItemCount() {
            return items.size();
        }
    }
}