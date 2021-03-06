package com.example.Runner8.ui.map.Guide;


import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.Runner8.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class GuideFragment extends Fragment {


    TextView tv_guideApply, tv_guideCancel, tv_guideClear;
    EditText edit_me,edit_opp,editmeOpp1,editmeOpp2,editoppMe1,editoppMe2;
   // ToggleButton tgbtn_sound;
    FloatingActionButton tgbtn_sound;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.guide_fragment, container, false);

        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        edit_me = view.findViewById(R.id.edit_myLocation);
        edit_opp = view.findViewById(R.id.edit_oppLocation);
        editoppMe2 = view.findViewById(R.id.edit_myPrecedeAfter);
        editoppMe1 = view.findViewById(R.id.edit_myPrecedeBefore);
        editmeOpp1 = view.findViewById(R.id.edit_oppPrecedeBefore);
        editmeOpp2 = view.findViewById(R.id.edit_oppPrecedeAfter);
        tv_guideApply = view.findViewById(R.id.tvBtn_guideApply);
        tv_guideCancel = view.findViewById(R.id.tvBtn_guideCancel);
        tv_guideClear = view.findViewById(R.id.tvBtn_guideClear);
        tgbtn_sound = view.findViewById(R.id.tbtn_sound);

        GuideSoundDialogue dialogue = new GuideSoundDialogue();


        tgbtn_sound.setOnClickListener(v -> dialogue.show(getParentFragmentManager(),"Dialog"));

        tv_guideApply.setOnClickListener(v -> {
            // + ????????????
            Intent intent = new Intent(getActivity(), GuideActivity.class);
            startActivity(intent);
        });

        tv_guideCancel.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), GuideActivity.class);
            startActivity(intent);
        });

        tv_guideClear.setOnClickListener(v -> clearStr());
    }

    public void clearStr(){
        edit_me.setText("?????????????????? ?????? {?????????} ???????????????!");
        edit_opp.setText("????????? ?????????????????? ?????? {????????????} ???????????????!");
        editmeOpp1.setText("????????? ?????? ?????? ????????????!");
        editmeOpp2.setText("????????? ?????? ??????????????????!");
        editoppMe1.setText("????????? ?????? ?????? ????????????!");
        editoppMe2.setText("????????? ??????????????????!");
    }

    public String getEdit_me() {
        return "?????????????????? ?????? {?????????} ???????????????!";
    }

    public String getEdit_opp() {
        return "????????? ?????????????????? ?????? {????????????} ???????????????!";
    }

    public String getEditmeOpp1() {
        return "????????? ?????? ?????? ????????????!";
    }

    public String getEditmeOpp2() { return "????????? ?????? ??????????????????!"; }

    public String getEditoppMe1() {
        return "????????? ?????? ?????? ????????????!";
    }

    public String getEditoppMe2() {
        return "????????? ??????????????????!";
    }
}