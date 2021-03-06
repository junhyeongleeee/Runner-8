package com.example.Runner8.ui.F_H.calorieDictionary;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.Runner8.R;
import com.example.Runner8.TRASH.Food;
import com.example.Runner8.TRASH.FoodItem;
import com.example.Runner8.ui.Loading.LoadingDialogue;
import com.example.Runner8.ui.F_H.calorie.Adapter.Model.FoodData;
import com.example.Runner8.ui.F_H.calorie.Activity.FoodSearchActivity;
import com.example.Runner8.ui.F_H.calorie.SingleTon.CalorieSingleTon;
import com.example.Runner8.ui.F_H.calorieDictionary.Adapter.Model.SearchModel;
import com.example.Runner8.ui.F_H.calorieDictionary.Adapter.SearchAdapter;
import com.example.Runner8.ui.F_H.calorieDictionary.Adapter.SearchAdapter2;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import org.jetbrains.annotations.NotNull;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class CalorieDictionaryFragment extends Fragment {

    private CalorieDictionaryViewModel calorieDictionaryViewModel;

    private List<java.lang.String> list;          // ???????????? ?????? ???????????????
    private RecyclerView recyclerView;
    private ListView listView;          // ????????? ????????? ???????????????
    private EditText editSearch;        // ???????????? ????????? Input ???
    private Button button;
    private SearchAdapter adapter;      // ??????????????? ????????? ?????????
    private ArrayList<FoodItem> foodItems = new ArrayList<>();

    private ArrayList<SearchModel> searchModels = new ArrayList<>();
    private ArrayList<SearchModel> search_view_models = new ArrayList<>();
    private ArrayList<SearchModel> search_results = new ArrayList<>();
    private ArrayList<java.lang.String> remove_duplication = new ArrayList<>();

    private NavController navController;

    ProgressBar search_progress;

    FloatingActionButton cart;

    public static CalorieDictionaryFragment newInstance() {
        return new CalorieDictionaryFragment();
    }

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseUser user;

    // View
    AppCompatButton search_button;

    // Class
    SearchModel searchModel;

    // Adapter
    SearchAdapter2 searchAdapter2;

    // Valuable
    java.lang.String search;
    int count = 0;


    public CalorieDictionaryFragment(){}

    @Override
    public void onAttach(@NonNull @NotNull Context context) {
        super.onAttach(context);

        Log.i("FragmentLifeCycle", "CalorieDictionaryFragment onAttach!!");
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Log.i("FragmentLifeCycle", "CalorieDictionaryFragment onDetach!!");
    }

    @Override
    public void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.i("FragmentLifeCycle", "CalorieDictionaryFragment onCreate!!");
    }

    // LayoutInflater : ???????????? XML ????????? ?????? View ????????? ??????????????? ?????????.
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        Log.i("FragmentLifeCycle", "CalorieDictionaryFragment onCreateView!!");

        calorieDictionaryViewModel =
                new ViewModelProvider(this).get(CalorieDictionaryViewModel.class);

        // inflate : xml ??? ????????? ?????? view ??? ????????? ?????? view ????????? ????????? ??????.
        View root = inflater.inflate(R.layout.fragment_calorie_dictionary, container, false);

        editSearch = root.findViewById(R.id.editSearch);
        recyclerView = root.findViewById(R.id.food_search_list);
        search_button = root.findViewById(R.id.search_button);

        navController = Navigation.findNavController(getActivity(), R.id.nav_food_search_fragment);

        user = FirebaseAuth.getInstance().getCurrentUser();
        list = new ArrayList<>();

        // InitialListToFoodDB();

        // ???????????? ????????? ???????????? ????????????.
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        searchAdapter2 = new SearchAdapter2(getActivity(), search_results);

        // ??????????????? ???????????? ????????????.

        recyclerView.setAdapter(searchAdapter2);

        //
        // ????????? ???????????? ????????? ??????
        editSearch.setOnKeyListener((v, keyCode, event) -> {
            if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                    (keyCode == KeyEvent.KEYCODE_ENTER))
                return true;
            else {
                return false;
            }
        });

        search_button.setOnClickListener(v -> {
            search = editSearch.getText().toString();
            search();
        });

        searchAdapter2.setListener((holder, view, position) -> {

            searchModel = search_results.get(position);
            FoodData foodData = new FoodData();

            if(searchModel.getNUTR_CONT4().equals("    ")) foodData.setFat(0.0);
            else foodData.setFat(Double.valueOf(searchModel.getNUTR_CONT4()));

            if(searchModel.getNUTR_CONT2().equals("    ")) foodData.setCar(0.0);
            else foodData.setCar(Double.valueOf(searchModel.getNUTR_CONT2()));

            if(searchModel.getNUTR_CONT3().equals("    ")) foodData.setPro(0.0);
            else foodData.setPro(Double.valueOf(searchModel.getNUTR_CONT3()));

            foodData.setContent(searchModel.getSERVING_SIZE());
            Log.i("content", Double.valueOf(searchModel.getSERVING_SIZE()) + "");

            if(searchModel.getNUTR_CONT1().equals("    ")) foodData.setKcal(0.0);
            else foodData.setKcal(Double.valueOf(searchModel.getNUTR_CONT1()));

            foodData.setName(searchModel.getDESC_KOR());

            CalorieSingleTon.getInstance().setFoodData(foodData);

            navController.navigate(R.id.nav_food_detail);

        });

        return root;
    }

    // ????????? ???????????? ?????????
    public void search() {

        // ?????? ??????????????? ???????????? ????????? ?????? ????????????.
        java.lang.String encodeStr = URLEncoder.encode(search);
        java.lang.String serviceUrl =
                "https://openapi.foodsafetykorea.go.kr/api/a11e1bdb6e4b4f77bc7b/I2790/xml/1/200/DESC_KOR=" + encodeStr;

        DownloadWebpageTask downloadWebpageTask = new DownloadWebpageTask();
        downloadWebpageTask.setSearchModel(searchModel);
        downloadWebpageTask.setSearchModels(searchModels);
        downloadWebpageTask.execute(serviceUrl, search);

    }

    // FoodList Update (FireStore)
    public void InitialListToFoodDB() {

        db.collection("Calorie").document("Food")
                .collection("Dictionary")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        int i = 0;
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Food.getInstance().addFoodData(document.getId());
                            i++;
                        }
                        Log.i("documentSize", java.lang.String.valueOf(task.getResult().size()));
                    } else
                        Log.d("QueryError", "Error getting documents: ", task.getException());
                });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable @org.jetbrains.annotations.Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.i("FragmentLifeCycle", "CalorieDictionaryFragment onActivityResult!!");
    }

    @Override
    public void onResume() {
        super.onResume();

        FragmentActivity activity = getActivity();
        if (activity != null){
            ((FoodSearchActivity) activity).getSupportActionBar().setTitle("????????? ??????");
        }


        Log.i("FragmentLifeCycle", "FoodDetailFragment onResume!!");
    }

    @Override
    public void onPause() {
        super.onPause();

        Log.i("FragmentLifeCycle", "CalorieDictionaryFragment onPause!!");
    }

    @Override
    public void onStop() {
        super.onStop();

        Log.i("FragmentLifeCycle", "CalorieDictionaryFragment onStop!!");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        Log.i("FragmentLifeCycle", "CalorieDictionaryFragment onDestroy!!");
    }

    private class DownloadWebpageTask extends AsyncTask<java.lang.String, Void, java.lang.String> {
        private LoadingDialogue progressDialog = null;

        ArrayList<SearchModel> searchModels;
        SearchModel searchModel;
        java.lang.String searchText;
        boolean equal_check = false;

        public void setSearchModel(SearchModel searchModel) {
            this.searchModel = searchModel;
        }

        public void setSearchModels(ArrayList<SearchModel> searchModels) {
            this.searchModels = searchModels;
        }

        @Override
        protected java.lang.String doInBackground(java.lang.String... params) {
            try {
                searchText = params[1];
                searchModels.clear();
                return (java.lang.String) downloadUrl((java.lang.String) params[0]);
            } catch (Exception e) {
                Log.i("doInBackGround", "????????????  ??????");
                return "???????????? ??????";
            }
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog  = new LoadingDialogue();
            progressDialog.show(getActivity().getSupportFragmentManager(), "loading");

        }

        protected void onProgressUpdate(Integer ... values){

        }

        protected void onPostExecute(java.lang.String result) {
            try {
                XmlPullParserFactory factory = XmlPullParserFactory.newInstance();

                factory.setNamespaceAware(true);
                XmlPullParser xpp = factory.newPullParser();
                xpp.setInput(new StringReader(result));

                java.lang.String NUM;            // ??????
                java.lang.String FOOD_CD;        // ????????????
                java.lang.String DESC_KOR;       // ??????
                java.lang.String SERVING_SIZE;   // ??? ?????????
                java.lang.String NUTR_CONT1;     // ??????(kcal)(1??? ?????????)
                java.lang.String NUTR_CONT2;     // ????????????(g)(1??? ?????????)
                java.lang.String NUTR_CONT3;     // ?????????(g)(1??? ?????????)
                java.lang.String NUTR_CONT4;     // ??????(g)(1??? ?????????)
                java.lang.String NUTR_CONT5;     // ??????(g)(1??? ?????????)
                java.lang.String NUTR_CONT6;     // ?????????(mg)(1??? ?????????)
                java.lang.String NUTR_CONT7;     // ???????????????(mg)(1??? ?????????)
                java.lang.String NUTR_CONT8;     // ???????????????(mg)(1??? ?????????)
                java.lang.String NUTR_CONT9;    // ???????????????(mg)(1??? ?????????)

                boolean bSet_NUM = false;            // ??????
                boolean bSet_FOOD_CD = false;        // ????????????
                boolean bSet_DESC_KOR = false;       // ??????
                boolean bSet_SERVING_SIZE = false;   // ??? ?????????
                boolean bSet_NUTR_CONT1 = false;     // ??????(kcal)(1??? ?????????)
                boolean bSet_NUTR_CONT2 = false;     // ????????????(g)(1??? ?????????)
                boolean bSet_NUTR_CONT3 = false;     // ?????????(g)(1??? ?????????)
                boolean bSet_NUTR_CONT4 = false;     // ??????(g)(1??? ?????????)
                boolean bSet_NUTR_CONT5 = false;     // ??????(g)(1??? ?????????)
                boolean bSet_NUTR_CONT6 = false;     // ?????????(mg)(1??? ?????????)
                boolean bSet_NUTR_CONT7 = false;     // ???????????????(mg)(1??? ?????????)
                boolean bSet_NUTR_CONT8 = false;     // ???????????????(mg)(1??? ?????????)
                boolean bSet_NUTR_CONT9 = false;

                int eventType = xpp.getEventType();


                while (eventType != XmlPullParser.END_DOCUMENT) {

                    if (eventType == XmlPullParser.START_DOCUMENT) {
                        ;
                    }
                    else if (eventType == XmlPullParser.START_TAG) {
                        java.lang.String tagName = xpp.getName();
                        if(tagName.equals("row")){
                            searchModel = new SearchModel();
                        }
                        if (tagName.equals("NUTR_CONT3")) {
                            bSet_NUTR_CONT3 = true;
                        }
                        if (tagName.equals("NUTR_CONT2")) {
                            bSet_NUTR_CONT2 = true;
                        }
                        if (tagName.equals("NUTR_CONT1"))
                            bSet_NUTR_CONT1 = true;
                        if (tagName.equals("SERVING_SIZE"))
                            bSet_SERVING_SIZE = true;
                        if (tagName.equals("NUTR_CONT9"))
                            bSet_NUTR_CONT9 = true;
                        if (tagName.equals("NUTR_CONT8"))
                            bSet_NUTR_CONT8 = true;
                        if (tagName.equals("FOOD_CD"))
                            bSet_FOOD_CD = true;
                        if (tagName.equals("NUTR_CONT7"))
                            bSet_NUTR_CONT7 = true;
                        if (tagName.equals("NUTR_CONT6"))
                            bSet_NUTR_CONT6 = true;
                        if (tagName.equals("NUTR_CONT5"))
                            bSet_NUTR_CONT5 = true;
                        if (tagName.equals("NUTR_CONT4"))
                            bSet_NUTR_CONT4 = true;
                        if (tagName.equals("DESC_KOR"))
                            bSet_DESC_KOR = true;
                        if (tagName.equals("NUM"))
                            bSet_NUM = true;
                    }
                    else if (eventType == XmlPullParser.TEXT) {
                        if (bSet_NUTR_CONT3) {
                            searchModel.setNUTR_CONT3(xpp.getText());
                            bSet_NUTR_CONT3 = false;
                        }
                        if (bSet_NUTR_CONT2) {
                            searchModel.setNUTR_CONT2(xpp.getText());
                            bSet_NUTR_CONT2 = false;
                        }
                        if (bSet_NUTR_CONT1) {
                            searchModel.setNUTR_CONT1(xpp.getText());
                            bSet_NUTR_CONT1 = false;
                        }
                        if (bSet_SERVING_SIZE) {
                            searchModel.setSERVING_SIZE(xpp.getText());
                            bSet_SERVING_SIZE = false;
                        }
                        if (bSet_NUTR_CONT9) {
                            searchModel.setNUTR_CONT9(xpp.getText());
                            bSet_NUTR_CONT9 = false;
                        }
                        if (bSet_NUTR_CONT8) {
                            searchModel.setNUTR_CONT8(xpp.getText());
                            bSet_NUTR_CONT8 = false;
                        }
                        if (bSet_FOOD_CD) {
                            searchModel.setFOOD_CD(xpp.getText());
                            bSet_FOOD_CD = false;
                        }
                        if (bSet_NUTR_CONT7) {
                            searchModel.setNUTR_CONT7(xpp.getText());
                            bSet_NUTR_CONT7 = false;
                        }
                        if (bSet_NUTR_CONT6) {
                            searchModel.setNUTR_CONT6(xpp.getText());
                            bSet_NUTR_CONT6 = false;
                        }
                        if (bSet_NUTR_CONT5) {
                            searchModel.setNUTR_CONT5(xpp.getText());
                            bSet_NUTR_CONT5 = false;
                        }
                        if (bSet_NUTR_CONT4) {
                            searchModel.setNUTR_CONT4(xpp.getText());
                            bSet_NUTR_CONT4 = false;
                        }
                        if (bSet_DESC_KOR) {
                            searchModel.setDESC_KOR(xpp.getText());
                            bSet_DESC_KOR = false;
                        }
                        if (bSet_NUM) {
                            searchModel.setNUM(xpp.getText());
                            bSet_NUM = false;
                        }
                    }
                    else if (eventType == XmlPullParser.END_TAG) {
                        java.lang.String end_tag = xpp.getName();
                        if (end_tag.equals("row")) {
                            searchModels.add(searchModel);
                        }
                    }

                    eventType = xpp.next();
                }
                count = 0;
                search_view_models.clear();
                search_results.clear();
                remove_duplication.clear();

                for(SearchModel sm : searchModels){
                    Log.i("sm", java.lang.String.valueOf(sm.getDESC_KOR().indexOf(searchText)));

                    if ((sm.getDESC_KOR().indexOf(searchText) == 0) && count < 20) {
                        Log.i("sm", sm.getDESC_KOR());
                        search_view_models.add(sm);
                        count++;
                    }
                }

                for(SearchModel data : search_view_models){
                    Log.i("data", data.getDESC_KOR());
                    if(!remove_duplication.contains(data.getDESC_KOR())) {
                        remove_duplication.add(data.getDESC_KOR());
                        search_results.add(data);
                    }
                }

                searchAdapter2.notifyDataSetChanged();
                progressDialog.dismiss();

            } catch (XmlPullParserException e) {
                e.printStackTrace();
            } catch (Exception e) {
                Log.i("ParsingError", e.toString());
            }
        }

        private java.lang.String downloadUrl(java.lang.String myurl) throws IOException {

            HttpURLConnection conn = null;
            try {
                URL url = new URL(myurl);
                conn = (HttpURLConnection) url.openConnection();
                BufferedInputStream buf = new BufferedInputStream(
                        conn.getInputStream());
                BufferedReader bufreader = new BufferedReader(
                        new InputStreamReader(buf, "utf-8"));
                java.lang.String line = null;
                java.lang.String page = "";
                while ((line = bufreader.readLine()) != null) {
                    page += line;
                }
                Log.i("page", page);

                return page;

            } finally {
                conn.disconnect();
            }
        }


    }
}

