package com.example.Runner8.TRASH;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.Runner8.MainActivity;
import com.example.Runner8.R;
import com.example.Runner8.SingleTon.Sub_bundle;
import com.example.Runner8.ui.F_H.calorie.Activity.FoodRepActivity;
import com.example.Runner8.ui.F_H.calorie.Activity.FoodSearchActivity;
import com.example.Runner8.ui.Graph.GraphActivity;
import com.example.Runner8.ui.Graph.Today_Date;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import org.jetbrains.annotations.NotNull;

public class CalorieFragment extends Fragment implements View.OnClickListener{

    private CalorieViewModel calorieViewModel;
    private TextView progress, progress_max;
    private Button search, snackBar, form, japanese, chinese, cafe, zeroKcal, koreaFood, fastFood;
    private ProgressBar progressBar;
    FloatingActionButton floatingActionButton;

    MainActivity activity;
    CalorieFragment CalorieFragment;

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

    // Class
    Today_Date today_date = new Today_Date();

    // newInstance constructor for creating fragment with arguments
    public static CalorieFragment newInstance(int num){
        CalorieFragment fragment = new CalorieFragment();
        Bundle args = new Bundle();
        args.putInt("num",num);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(@NonNull @NotNull Context context) {
        super.onAttach(context);

        activity = (MainActivity)getActivity();

        Log.i("FragmentLifeCycle", "CalorieFragment onAttach!!");
    }

    @Override
    public void onDetach() {
        super.onDetach();

        Log.i("FragmentLifeCycle", "CalorieFragment onDetach!!");

        activity = null;
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        Log.i("FragmentLifeCycle", "CalorieFragment onCreateView!!");

        calorieViewModel =
                new ViewModelProvider(this).get(CalorieViewModel.class);
        View root = inflater.inflate(R.layout.fragment_calorie, container, false);

        return root;
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        snackBar = view.findViewById(R.id.??????);
        form = view.findViewById(R.id.??????);
        japanese = view.findViewById(R.id.??????);
        chinese = view.findViewById(R.id.??????);
        cafe = view.findViewById(R.id.???????????????);
        koreaFood = view.findViewById(R.id.??????);
        fastFood = view.findViewById(R.id.???????????????);
        zeroKcal = view.findViewById(R.id.zeroKcal);
        search = view.findViewById(R.id.search_button);
        progressBar = view.findViewById(R.id.kcal_bar);
        progress = view.findViewById(R.id.progress);
        progress_max = view.findViewById(R.id.progress_max);

        floatingActionButton = view.findViewById(R.id.food_graph);

        // Go fragment_classification
        search.setOnClickListener(v -> {


            Intent intent = new Intent(getContext(), FoodSearchActivity.class);
            intent.putExtra("Fragment", "search");
            startActivity(intent);

            // activity.changeFragment(1);
        });

        floatingActionButton.setOnClickListener(v -> {

            Intent intent = new Intent(getContext(), GraphActivity.class);
            intent.putExtra("kcal_name", "food_kcal");
            intent.putExtra("none_data", "none_food_data");
            startActivity(intent);
        });

        // OnClickListener
        snackBar.setOnClickListener(this);
        form.setOnClickListener(this);
        japanese.setOnClickListener(this);
        chinese.setOnClickListener(this);
        cafe.setOnClickListener(this);
        koreaFood.setOnClickListener(this);
        fastFood.setOnClickListener(this);
        zeroKcal.setOnClickListener(this);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable @org.jetbrains.annotations.Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.i("FragmentLifeCycle", "CalorieFragment onActivityResult!!");
    }

    @Override
    public void onResume() {
        super.onResume();

        /*
        String app_amount = Sub_bundle.getInstance().getApp_amount();
        String total_food_kcal = Sub_bundle.getInstance().getTotal_food_kcal();
        int int_app_amount = (int) Math.round(Double.valueOf(app_amount));

        if(total_food_kcal.equals("")){
            progressBar.setProgress(0);
            progress.setText("0");
        }
        else{
            int total_kcal = (int) Math.round(Double.valueOf(total_food_kcal));
            progressBar.setProgress(total_kcal);
            progress.setText(String.valueOf(total_kcal));
        }

        progress_max.setText(String.valueOf(int_app_amount));
        progressBar.setMax(int_app_amount);

        Log.i("FragmentLifeCycle", "CalorieFragment onResume!!");

         */
    }

    @Override
    public void onPause() {
        super.onPause();

        Log.i("FragmentLifeCycle", "CalorieFragment onPause!!");
    }

    @Override
    public void onStop() {
        super.onStop();

        Log.i("FragmentLifeCycle", "CalorieFragment onStop!!");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i("FragmentLifeCycle", "CalorieFragment onDestroy!!");
    }

    @Override
    public void onClick(View v) {

        Intent intent = new Intent(getContext(), FoodRepActivity.class);

        switch (v.getId()){
            case R.id.??????:
                Sub_bundle.getInstance().setRep("??????");
                startActivity(intent);
//                bundle.putString("Classification", "??????");
//                representativeFoodFragment.setArguments(bundle);
//                transaction.replace(R.id.nav_host_fragment, representativeFoodFragment).commit();
                break;
            case R.id.??????:
                Sub_bundle.getInstance().setRep("??????");
                startActivity(intent);
                break;
            case R.id.??????:
                Sub_bundle.getInstance().setRep("??????");
                startActivity(intent);
                break;
            case R.id.??????:
                Sub_bundle.getInstance().setRep("??????");
                startActivity(intent);
                break;
            case R.id.??????:
                Sub_bundle.getInstance().setRep("??????");
                startActivity(intent);
                break;
            case R.id.???????????????:
                Sub_bundle.getInstance().setRep("???????????????");
                startActivity(intent);
                break;
            case R.id.???????????????:
                Sub_bundle.getInstance().setRep("???????????????");
                startActivity(intent);
                break;
            case R.id.??????:
                Sub_bundle.getInstance().setRep("??????");
                startActivity(intent);
            case R.id.zeroKcal:

                AlertDialog.Builder builder = new AlertDialog.Builder(getContext())
                       .setTitle("??????")
                        .setMessage(" ????????? ???????????? 0 Kcal ?????????. \n ??????????????? ?????? ???????????? ???????????????.")
                        .setNegativeButton("???  ???", (dialog, which) -> {

                            DocumentReference documentReference = db.collection("Users").document(user.getUid());

                            documentReference.get()
                                    .addOnCompleteListener(task -> {
                                        documentReference.update("TotalFoodKcalOfDay", 0);
                                        Sub_bundle.getInstance().setTotal_food_kcal("0");

                                        onResume();
                                    });

                            documentReference.collection("FoodCart")
                                    .get()
                                    .addOnCompleteListener(task -> {
                                        if(task.isSuccessful()){
                                            for(QueryDocumentSnapshot docSnap : task.getResult()){
                                                documentReference.collection("FoodCart").document(docSnap.getId())
                                                        .delete();
                                            }
                                        }
                                    });
                        })
                        .setPositiveButton("??? ???", (dialog, which) -> {
                            dialog.dismiss();
                        });

                AlertDialog dialog = builder.create();
                dialog.show();

                break;
        }
    }
}