package com.example.Runner8.TRASH;

import android.os.Bundle;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.example.Runner8.R;
import com.example.Runner8.SingleTon.Sub_bundle;
import com.example.Runner8.ui.F_H.calorie.Adapter.Model.FoodData;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class FoodDetailActivity extends AppCompatActivity {

    TextView fooddataname, kcaldata, proteindata, fatdata, cycdata, foodStyle, day1_citeria;

    LinearLayout high_protein, row_kcal;

    FoodData foodData;

    AppCompatButton btneat_fooddata;

    //
    double TotalFoodKcalOfDay = 0.0;

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

    @Override
    protected void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.foodquickitem_data);

        fooddataname = findViewById(R.id.fooddataname);
        cycdata = findViewById(R.id.cycdata);
        fatdata = findViewById(R.id.fatdata);
        proteindata = findViewById(R.id.proteindata);
        kcaldata = findViewById(R.id.kcaldata);
        foodStyle = findViewById(R.id.foodStyle);
        day1_citeria = findViewById(R.id.day1_citeria);
        btneat_fooddata = findViewById(R.id.btneat_fooddata);

        row_kcal = findViewById(R.id.row_kcal);
        high_protein = findViewById(R.id.high_protein);

        foodData = (FoodData) getIntent().getSerializableExtra("food_data");

        btneat_fooddata.setOnClickListener(v -> {

            Map<java.lang.String,Object> map = new HashMap<>();

            map.put("Eng",foodData.getKcal());
            map.put("Pro", foodData.getPro());
            map.put("Fat", foodData.getFat());
            map.put("name", foodData.getName());
            map.put("Car", foodData.getCar());
            map.put("content", foodData.getContent());

            // ?????? ????????? ?????????
            DocumentReference docRef_foodName = db.collection("Users").document(user.getUid())
                    .collection("FoodCart").document(foodData.getName());

            // Summary ??? ????????? ?????????
            DocumentReference docRef_summary = db.collection("Users").document(user.getUid())
                    .collection("Summary").document(foodData.getName());

            // ???????????? ?????? ??? ?????? Kcal ????????????
            if(Sub_bundle.getInstance().getTotal_food_kcal().equals(""))
                TotalFoodKcalOfDay = 0.0;
            else
                TotalFoodKcalOfDay = Double.valueOf(Sub_bundle.getInstance().getTotal_food_kcal());

            docRef_foodName.get().addOnCompleteListener(task -> {
                DocumentSnapshot document = task.getResult();

                // ??????????????? ????????? ????????? ??????
                if(!document.exists()){
                    Map<java.lang.String,Object> data = new HashMap<>();
                    docRef_foodName.set(data);
                    map.put("Count",1);
                }
                // ????????? ????????? ?????? ??????
                else{
                    int count = Integer.parseInt(document.getData().get("Count").toString());
                    map.put("Count",++count);
                }
                docRef_foodName.update(map);

                // ???????????? ?????? ?????? ????????????
                Map<java.lang.String, Object> totalKcal = new HashMap<>();
                TotalFoodKcalOfDay += Double.valueOf(foodData.getKcal());
                totalKcal.put("TotalFoodKcalOfDay", TotalFoodKcalOfDay);
                db.collection("Users").document(user.getUid())
                        .update(totalKcal);

                // SingleTon
                Sub_bundle.getInstance().setTotal_food_kcal(java.lang.String.valueOf(TotalFoodKcalOfDay));

                Toast.makeText(getApplicationContext(), "??????????????? ?????????????????????.!!", Toast.LENGTH_SHORT).show();
            });
        });

        setView();
    }

    public void setView(){

        Log.i("foodData.getName()", foodData.getName());
        fooddataname.setText(foodData.getName());
        /*
        cycdata.setText(String.valueOf(foodData.getCar()));
        fatdata.setText(String.valueOf(foodData.getFat()));
        proteindata.setText(String.valueOf(foodData.getPro()));
        kcaldata.setText(String.valueOf(foodData.getKcal()));
        cycdata.setText(String.valueOf(foodData.getCar()));

         */

        if(foodData.getF_class() != null) foodStyle.setText(foodData.getF_class());

        // ?????????

        // ?????????

    }

}
