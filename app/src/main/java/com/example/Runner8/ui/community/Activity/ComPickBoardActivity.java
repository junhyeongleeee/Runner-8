package com.example.Runner8.ui.community.Activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.example.Runner8.R;
import com.example.Runner8.SingleTon.ScreenChangeDetect;
import com.example.Runner8.ui.Graph.Today_Date;
import com.example.Runner8.ui.community.Adapter.Board.model.BoardModel;
import com.example.Runner8.ui.community.Adapter.Comment.CommentAdapter;
import com.example.Runner8.ui.community.Adapter.Comment.model.CommentModel;
import com.example.Runner8.ui.community.Adapter.Reply.Model.ReplyModel;
import com.example.Runner8.ui.community.Adapter.Reply.ReplyAdapter;
import com.example.Runner8.ui.community.BackPressEditText;
import com.example.Runner8.ui.community.TimeValue;
import com.example.Runner8.ui.community.singleTon.TotalCounts;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ComPickBoardActivity extends AppCompatActivity implements View.OnClickListener {

    // View
    private TextView tv_title, tv_upCount, tv_boardViewCount, tv_boardDate,
            tv_boardContent, tv_boardName, tv_commentInput, tv_totalCommentCount, tv_commentClick;
    private ImageView img_user;
    private ToggleButton img_up;
    private BackPressEditText et_comment;
    private RecyclerView recyclerView;
    private Toolbar toolbar;
    private SwipeRefreshLayout swipeRefreshLayout;

    // Valuable
    private String user_nickName;
    private boolean seeMoreComment = false, reply_check = false, longClickResult = false;
    private int limit = 5, reload_index, currentCommentPosition, currentCommentLongClickPosition, commentSize;

    private ArrayList<CommentModel> dataArrayList = new ArrayList<>();
    private ArrayList<ReplyModel> replyModels = new ArrayList<>();
    private ArrayList<Integer> indexes = new ArrayList<>();

    // Adapter
    private CommentAdapter commentAdapter;
    private ReplyAdapter replyAdapter;

    // Firebase
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

    // Reference
    private DocumentReference dr_user_profile = db.collection("Users").document(user.getUid())
            .collection("Profile").document("diet_profile");

    private DocumentReference dr_user = db.collection("Users").document(user.getUid());

    // Class
    private BoardModel boardModel;
    private CommentModel commentModel, commentModelWhenReplyChecked;
    private Today_Date today_date = new Today_Date();
    private TimeValue timeValue;



    @Override
    protected void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pickboard);

        tv_title = findViewById(R.id.tv_title);
        tv_upCount = findViewById(R.id.tv_upCount);
        tv_boardViewCount = findViewById(R.id.tv_boardViewCount);
        tv_boardDate = findViewById(R.id.tv_boardDate);
        img_up = findViewById(R.id.img_up);
        img_user = findViewById(R.id.img_user);
        tv_boardContent = findViewById(R.id.tv_boardContent);
        tv_boardName = findViewById(R.id.tv_boardName);
        et_comment = findViewById(R.id.et_comment);
        tv_commentInput = findViewById(R.id.tv_commentInput);
        tv_totalCommentCount = findViewById(R.id.tv_totalCommentCount);
        recyclerView = findViewById(R.id.list_commentAll);
        tv_commentClick = findViewById(R.id.tv_commentClick);
        toolbar = findViewById(R.id.pick_board_toolbar);
        swipeRefreshLayout = findViewById(R.id.swipe_refresh);

        //

        // toolbar
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // getIntent().getData...
        getIntentData();

        // onClickListener
        img_up.setOnClickListener(this);
        tv_commentInput.setOnClickListener(this);

        //
        swipeRefreshLayout.setOnRefreshListener(() -> {
            onRefresh();
            swipeRefreshLayout.setRefreshing(false);
        });

        et_comment.setOnBackPressListener(() -> {
            if(reply_check){
                et_comment.setHint(user_nickName + "(???)??? ?????? ?????????");
                reply_check = false;
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        if(boardModel.getUid().equals(user.getUid())) getMenuInflater().inflate(R.menu.board_writer_menu, menu);
        else getMenuInflater().inflate(R.menu.board_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        // board reference
        DocumentReference dr_board = db.collection("Community").document("board");

        DocumentReference dr_board_item = dr_board.collection("item")
                .document(boardModel.getBoard_index());

        switch (item.getItemId()){
            case android.R.id.home:{
                finish();
                return true;
            }

            case R.id.action_edit:
                dr_board_item.get().addOnCompleteListener(task -> {

                    Intent intent = new Intent(this, UploadBoardActivity.class);

                    intent.putExtra("edit", true);
                    intent.putExtra("board_index", boardModel.getBoard_index());

                    startActivity(intent);

                });
                break;

            case R.id.action_remove:

                DocumentReference dr_user_board = db.collection("Users").document(user.getUid())
                        .collection("Comm").document(boardModel.getWriter_index());

                AlertDialog.Builder builder = new AlertDialog.Builder(this)
                        .setTitle("??????")
                        .setMessage(" ????????? ???????????? ??????????????????????????")
                        .setNegativeButton("???", (dialog, which) -> {

                            // total comm
                            dr_board_item.delete();

                            // board_count update
                            dr_board.get().addOnCompleteListener(task -> {
                                DocumentSnapshot document = task.getResult();
                                int board_count = Integer.valueOf(document.get("board_count").toString());
                                //
                                Map<String, Object> map = new HashMap<>();
                                map.put("board_count", --board_count);

                                TotalCounts.getInstance().setBoard_count(String.valueOf(board_count));

                                dr_board.update(map);
                            });

                            //
                            ScreenChangeDetect.getInstance().setRemoveBoard(true);

                            finish();
                        })
                        .setPositiveButton("?????????", (dialog, which) -> { });

                AlertDialog dialog = builder.create();
                dialog.show();

            case R.id.action_declaration:


        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();

        InitializeView();
        onRefresh();

    }

    public void onRefresh(){

        //
        indexes.clear();
        dataArrayList.clear();
        commentAdapter = new CommentAdapter(this, dataArrayList, boardModel.getBoard_index(),
                boardModel.getUp_check());
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(commentAdapter);

        // board reference
        DocumentReference dr_board = db.collection("Community").document("board")
                .collection("item").document(boardModel.getBoard_index());

        if(seeMoreComment){
            // list update
            dr_board.collection("comment")
                    .orderBy("index", Query.Direction.DESCENDING)
                    .get()
                    .addOnCompleteListener(task -> {
                        LoadComment(task.getResult());
                    });
        }
        else{
            if(commentSize < limit+1){
                tv_commentClick.setVisibility(View.INVISIBLE);
            }else {
                tv_commentClick.setVisibility(View.VISIBLE);
                tv_commentClick.setOnClickListener(v -> {

                    Log.i("boardModelupCheck", String.valueOf(boardModel.getUp_check()));
                    seeMoreComment = true;
                    dr_board.collection("comment").whereLessThan("index",
                            indexes.get(indexes.size()-1))
                            .orderBy("index", Query.Direction.DESCENDING)
                            .get()
                            .addOnCompleteListener(task -> {
                                LoadComment(task.getResult());
                                tv_commentClick.setVisibility(TextView.INVISIBLE);
                            });
                });
            }
            // list update
            dr_board.collection("comment")
                    .orderBy("index", Query.Direction.DESCENDING)
                    .limit(limit)
                    .get()
                    .addOnCompleteListener(task -> {
                        LoadComment(task.getResult());
                    });
        }
    }

    public void getIntentData(){
        // getIntentExtra
        boardModel = (BoardModel) getIntent().getSerializableExtra("BoardModel");
        Log.i("boardModel", String.valueOf(boardModel.getUp_check()));
    }

    public void LoadComment(QuerySnapshot document){
        today_date.setNow();
        ArrayList<Map> doc = new ArrayList<>();

        if(document.size() == 0) return;

        for (QueryDocumentSnapshot queryDocumentSnapshot : document)
            doc.add(queryDocumentSnapshot.getData());

        for(Map<String, Object> comment : doc) {

            commentModel = new CommentModel();

            reload_index = Integer.valueOf(comment.get("index").toString());
            indexes.add(reload_index);

            String tmpTimeValue;
            timeValue = new TimeValue(comment.get("date").toString(), comment.get("time").toString());

            tmpTimeValue = timeValue.getTimeValue();

            // timeValue
            commentModel.setTimeValue(tmpTimeValue);
            // ??????
            commentModel.setComment(comment.get("content").toString());
            // uid
            commentModel.setUid(comment.get("uid").toString());
            // ????????? ???
            commentModel.setUpCount(comment.get("upCount").toString());
            // ??????
            commentModel.setDate(comment.get("date").toString());
            // ??????
            commentModel.setTime(comment.get("time").toString());
            // index
            commentModel.setIndex(comment.get("index").toString());
            // ?????? ????????? ?????????
            commentModel.setReply_final_index(comment.get("reply_final_index").toString());
            // ?????? ??????
            commentModel.setReply_count(comment.get("reply_count").toString());

            // ?????? ?????? ???????????? ??????
            commentModel.setReply_view_check(false);

            // ?????? ?????? ??????
            if(comment.get("reply_count").toString().equals("0")) commentModel.setReply_check(false);
            else commentModel.setReply_check(true);

            Log.i("content", "??????" + comment.get("content").toString() + "??????");
            dataArrayList.add(commentModel);
        }

        commentAdapter = new CommentAdapter(this, dataArrayList,
                boardModel.getBoard_index(), boardModel.getUp_check());
        recyclerView.setAdapter(commentAdapter);

        commentAdapter.setOnItemClickListener((holder, view, position) -> {

            commentModel = commentAdapter.getItem(position);

            Log.i("adapter", "setOnItemClickListener");
            currentCommentPosition = position;

            et_comment.requestFocus();

            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);

            et_comment.setText("");
            et_comment.setHint(commentModel.getNickName() + "?????? ??????");

            reply_check = true;

        });

        commentAdapter.setComment_long_click_listener((holder, view, position) -> {

            // ????????? ?????? ???????????? ?????? ?????? ??????
            if(reply_check) return;

            commentModel = commentAdapter.getItem(position);

            Log.i("WhenLongClicked", commentModel.getComment());
            Log.i("getIndex", boardModel.getBoard_index() + commentModel.getIndex());
            Log.i("indexes.", String.valueOf(indexes.get(indexes.size()-1)));

            currentCommentLongClickPosition = position;

            Log.i("dataArrayList",
                    dataArrayList.get(currentCommentLongClickPosition).getComment());

            if(commentModel.getUid().equals(user.getUid())){
                AlertDialog.Builder builder = new AlertDialog.Builder(this)
                        .setTitle("??????")
                        .setMessage(" ????????? ????????? ??????????????????????????")
                        .setNegativeButton("???", (dialog, which) -> {

                            DocumentReference dr_pick_board = db.collection("Community")
                                    .document("board").collection("item")
                                    .document(boardModel.getBoard_index());

                            dr_pick_board.collection("comment")
                                    .document(commentModel.getIndex()).delete();

                            boardModel.setComment_count(
                                    String.valueOf(Integer.valueOf(boardModel.getComment_count()) - 1));

                            Map<String, Object> map = new HashMap<>();
                            map.put("comment_count", boardModel.getComment_count());

                            //
                            tv_totalCommentCount.setText("?????? " + boardModel.getComment_count() + "???");
                            commentModel.setComment_count(boardModel.getComment_count());

                            if(!seeMoreComment) {
                                if(Integer.valueOf(boardModel.getComment_count()) >= 5){
                                    if(Integer.valueOf(boardModel.getComment_count()) == 5){
                                        tv_commentClick.setVisibility(View.INVISIBLE);
                                    }
                                    db.collection("Community").document("board")
                                            .collection("item").document(boardModel.getBoard_index())
                                            .collection("comment")
                                            .whereLessThan("index", indexes.get(indexes.size()-1))
                                            .limit(1)
                                            .get()
                                            .addOnCompleteListener(task1 -> {

                                                for(QueryDocumentSnapshot document1 : task1.getResult()){

                                                    Log.i("(\"content\").toString()", document1.get("content").toString());
                                                    commentModel = new CommentModel();

                                                    reload_index = Integer.valueOf(document1.get("index").toString());
                                                    indexes.add(reload_index);

                                                    today_date.setNow();

                                                    timeValue = new TimeValue(document1.get("date").toString(),
                                                            document1.get("time").toString());
                                                    String tmpTimeValue = timeValue.getTimeValue();
                                                    // timeValue
                                                    commentModel.setTimeValue(tmpTimeValue);
                                                    // ??????
                                                    commentModel.setComment(document1.get("content").toString());
                                                    // uid
                                                    commentModel.setUid(document1.get("uid").toString());
                                                    // ????????? ???
                                                    commentModel.setUpCount(document1.get("upCount").toString());
                                                    // ??????
                                                    commentModel.setDate(document1.get("date").toString());
                                                    // ??????
                                                    commentModel.setTime(document1.get("time").toString());
                                                    // index
                                                    commentModel.setIndex(document1.get("index").toString());
                                                    // ?????? ????????? ?????????
                                                    commentModel.setReply_final_index(
                                                            document1.get("reply_final_index").toString());
                                                    // ?????? ??????
                                                    commentModel.setReply_count(
                                                            document1.get("reply_count").toString());

                                                    // ?????? ?????? ???????????? ??????
                                                    commentModel.setReply_view_check(false);

                                                    // ?????? ?????? ??????
                                                    if(document1.get("reply_count").toString().equals("0")) commentModel.setReply_check(false);
                                                    else commentModel.setReply_check(true);
                                                    dataArrayList.add(commentModel);
                                                    commentAdapter.notifyItemInserted(dataArrayList.size());
                                                }
                                            });
                                }
                            }
                            //
                            dr_pick_board.update(map);

                            //
                            dataArrayList.remove(currentCommentLongClickPosition);
                            commentAdapter.notifyItemRemoved(currentCommentLongClickPosition);
                            commentAdapter.notifyItemRangeChanged(currentCommentLongClickPosition, dataArrayList.size());
                            indexes.remove(currentCommentLongClickPosition);
                        })
                        .setPositiveButton("?????????", (dialog, which) -> { });

                AlertDialog dialog = builder.create();
                dialog.show();
            }
            else{
                AlertDialog.Builder builder = new AlertDialog.Builder(this)
                        .setTitle("??????")
                        .setMessage("????????? ?????? ???????????????!!")
                        .setPositiveButton("??????", (dialog, which) -> { });

                AlertDialog dialog = builder.create();
                dialog.show();
            }

        });

    }

    public void InitializeView(){

        Glide.with(getApplicationContext())
                .load(boardModel.getProfile())
                .into(img_user);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            img_user.setClipToOutline(true);
        }

        tv_boardName.setText(boardModel.getNickName());
        tv_title.setText(boardModel.getTitle());
        tv_boardDate.setText(boardModel.getTimeValue());

        if(boardModel.getUp_check()) {
            img_up.setBackgroundResource(R.drawable.up_check);
            img_up.setChecked(true);
            boardModel.setUp_check(true);
        }else{
            img_up.setBackgroundResource(R.drawable.up);
            img_up.setChecked(false);
            boardModel.setUp_check(false);
        }

        tv_upCount.setText(boardModel.getUp_count());
        tv_boardViewCount.setText("????????? " + boardModel.getViews());

        tv_boardContent.setText(boardModel.getComment_count());
        commentSize = Integer.valueOf(boardModel.getComment_count());

        dr_user_profile.get().addOnCompleteListener(task -> {
            DocumentSnapshot document = task.getResult();
            String nickName = document.get("nickName").toString();
            et_comment.setHint(nickName + "(???)??? ?????? ?????????....");
            user_nickName = nickName;
        });
        tv_totalCommentCount.setText("?????? " + boardModel.getComment_count() + "???");

        //
        if(ScreenChangeDetect.getInstance().isHotBoardToPick()){
        }else if(ScreenChangeDetect.getInstance().isSearchToPick()){
        }else {
            ScreenChangeDetect.getInstance().setPickBoardToComHome(true);
        }
    }
    @Override
    public void onClick(View v) {

        // board reference
        DocumentReference dr_board = db.collection("Community").document("board")
                .collection("item").document(boardModel.getBoard_index());

        switch (v.getId()) {
            case R.id.img_up:

                Log.i("boardModel.getBoard_index()", boardModel.getBoard_index());
                DocumentReference dr_commLike = db.collection("Users").document(user.getUid())
                        .collection("Comm_Like").document(boardModel.getBoard_index());

                Map<String, Object> user_comm = new HashMap<>();
                Map<String, Object> total_comm = new HashMap<>();

                int up_count = Integer.valueOf(tv_upCount.getText().toString());

                // ????????? ??????
                if (!img_up.isChecked()) {
                    // background
                    img_up.setBackground(ContextCompat
                            .getDrawable(getApplicationContext(), R.drawable.up));

                    // user_comm update
                    user_comm.put("up_check", false);
                    //
                    up_count -= 1;
                    img_up.setChecked(false);
                } else {
                    // background
                    img_up.setBackground(ContextCompat
                            .getDrawable(getApplicationContext(), R.drawable.up_check));

                    up_count += 1;
                    // user_comm update
                    user_comm.put("up_check", true);
                    img_up.setChecked(true);
                }
                // up_check
                dr_commLike.set(user_comm);
                // total_comm update
                total_comm.put("up_count", up_count);
                //
                boardModel.setUp_count(String.valueOf(up_count));
                //
                dr_board.update(total_comm);
                // setText
                tv_upCount.setText(String.valueOf(up_count));
                break;

            case R.id.tv_commentInput:

                String comment = et_comment.getText().toString();
                // ?????? ?????? ??????
                if (reply_check) {

                    today_date.setNow();
                    Map<String, Object> addReply = new HashMap<>();
                    Map<String, Object> addComment = new HashMap<>();

                    commentModel.setReply_count(String.valueOf(Integer.valueOf(commentModel.getReply_count()) + 1));
                    commentModel.setReply_final_index(String.valueOf(Integer.valueOf(commentModel.getReply_final_index()) + 1));

                    // ??????, ?????????, ??????, ??????, uid, ????????? ???
                    addReply.put("content", comment);
                    addReply.put("date", today_date.getFormat_date());
                    addReply.put("time", today_date.getFormat_time());
                    addReply.put("upCount", 0);
                    addReply.put("uid", user.getUid());
                    addReply.put("index", commentModel.getReply_final_index());
                    //
                    addComment.put("reply_count", commentModel.getReply_count());
                    addComment.put("reply_final_index", commentModel.getReply_final_index());
                    //
                    dr_board.collection("comment").document(commentModel.getIndex())
                            .collection("reply").document(commentModel.getReply_final_index()).set(addReply);

                    dr_board.collection("comment").document(commentModel.getIndex())
                            .update(addComment);

                    //
                    Toast.makeText(this, "????????? ?????????????????????.!!", Toast.LENGTH_SHORT).show();

                    if (commentModel.isReply_view_check()) {
                        ReplyModel replyModel = new ReplyModel();

                        replyModel.setComment(comment);
                        replyModel.setDate(today_date.getFormat_date());
                        replyModel.setTime(today_date.getFormat_time());
                        replyModel.setUpCount("0");
                        replyModel.setUid(user.getUid());
                        replyModel.setIndex(commentModel.getReply_final_index());

                        replyModel.setDr_comLike(commentModel.getDr_comment());
                        replyModel.setDr_comment(commentAdapter.getCr_comment().document(commentModel.getIndex())
                                .collection("reply").document(commentModel.getReply_final_index()));

                        commentModel.newReplyModel(replyModel);

                        replyModels = commentModel.getReplyModels();
                        replyAdapter = new ReplyAdapter(this, replyModels, commentModel.getUp_check(),
                                commentModel.getIndex());
                        commentModel.getReplyView().setAdapter(replyAdapter);

                    } else {

                        dataArrayList.set(currentCommentPosition, commentModel);
                        commentAdapter.notifyDataSetChanged();
                    }
                }

                // ?????? ??????
                else {
                    Log.i("tv_commentInput", "reply_check false");

                    if (comment.equals("")) {
                        Toast.makeText(this, "????????? ???????????????!!", Toast.LENGTH_SHORT).show();
                    } else {
                        commentModel = new CommentModel();

                        today_date.setNow();
                        Map<String, Object> addComment = new HashMap<>();
                        Map<String, Object> addBoard = new HashMap<>();

                        boardModel.setComment_final_index(
                                String.valueOf(Integer.valueOf(boardModel.getComment_final_index()) + 1));
                        boardModel.setComment_count(
                                String.valueOf(Integer.valueOf(boardModel.getComment_count()) + 1));

                        int comm_final_index = Integer.valueOf(boardModel.getComment_final_index());
                        int com_count = Integer.valueOf(boardModel.getComment_count());
                        Log.i("comm_final_index", String.valueOf(comm_final_index));

                        // ??????, ?????????, ??????, ??????, uid, ????????? ???
                        addComment.put("content", comment);
                        addComment.put("date", today_date.getFormat_date());
                        addComment.put("time", today_date.getFormat_time());
                        addComment.put("upCount", 0);
                        addComment.put("uid", user.getUid());
                        addComment.put("index", comm_final_index);
                        addComment.put("reply_count", 0);
                        addComment.put("reply_final_index", 0);

                        timeValue = new TimeValue(today_date.getFormat_date(),
                                today_date.getFormat_time());
                        String tmpTimeValue = timeValue.getTimeValue();
                        // timeValue
                        commentModel.setTimeValue(tmpTimeValue);
                        // ??????
                        commentModel.setComment(comment);
                        // uid
                        commentModel.setUid(user.getUid());
                        // ????????? ???
                        commentModel.setUpCount("0");
                        // ??????
                        commentModel.setDate(today_date.getFormat_date());
                        // ??????
                        commentModel.setTime(today_date.getFormat_time());
                        // index
                        commentModel.setIndex(String.valueOf(comm_final_index));
                        // ?????? ????????? ?????????
                        commentModel.setReply_final_index("0");
                        // ?????? ??????
                        commentModel.setReply_count("0");

                        // ?????? ?????? ???????????? ??????
                        commentModel.setReply_view_check(false);

                        addBoard.put("comment_final_index", comm_final_index);
                        addBoard.put("comment_count", com_count);

                        // ??????
                        tv_totalCommentCount.setText("?????? " + (com_count) + "???");
                        commentModel.setComment_count(String.valueOf(com_count));

                        dr_board.collection("comment").document(String.valueOf(comm_final_index))
                                .set(addComment);
                        dr_board.update(addBoard);

                        dataArrayList.add(0, commentModel);
                        Log.i("dataArrayList.add", "check!!");

                        if(!seeMoreComment) {
                            if (dataArrayList.size() > limit) {

                                tv_commentClick.setVisibility(View.VISIBLE);
                                tv_commentClick.setOnClickListener(v1 -> {

                                    Log.i("boardModelupCheck", String.valueOf(boardModel.getUp_check()));
                                    seeMoreComment = true;
                                    dr_board.collection("comment").whereLessThan("index",
                                            indexes.get(indexes.size()-1))
                                            .orderBy("index", Query.Direction.DESCENDING)
                                            .get()
                                            .addOnCompleteListener(task -> {
                                                LoadComment(task.getResult());
                                                tv_commentClick.setVisibility(TextView.INVISIBLE);
                                            });
                                });

                                dataArrayList.remove(dataArrayList.size() - 1);
                                commentAdapter.notifyItemRemoved(dataArrayList.size() - 1);
                                commentAdapter.notifyItemRangeChanged(dataArrayList.size() - 1, dataArrayList.size());
                                indexes.remove(indexes.size() - 1);
                                indexes.add(0, comm_final_index);
                            }
                        }

                        commentAdapter.notifyItemInserted(0);
                        Log.i("notifyItemInserted", "check!!");

                        commentAdapter.setOnItemClickListener((holder, view, position) -> {

                            Log.i("adapter", "setOnItemClickListener");
                            commentModelWhenReplyChecked = commentAdapter.getItem(position);

                            currentCommentPosition = position;

                            et_comment.requestFocus();

                            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);

                            et_comment.setText("");
                            et_comment.setHint(commentModelWhenReplyChecked.getNickName() + "?????? ??????");

                            reply_check = true;
                        });

                        commentAdapter.setComment_long_click_listener((holder, view, position) -> {

                            // ????????? ?????? ???????????? ?????? ?????? ??????
                            if(reply_check) return;

                            commentModel = commentAdapter.getItem(position);

                            Log.i("WhenLongClicked", commentModel.getComment());
                            Log.i("getIndex", boardModel.getBoard_index() + commentModel.getIndex());

                            currentCommentLongClickPosition = position;

                            Log.i("dataArrayList",
                                    dataArrayList.get(currentCommentLongClickPosition).getComment());

                            if(commentModel.getUid().equals(user.getUid())){
                                AlertDialog.Builder builder = new AlertDialog.Builder(this)
                                        .setTitle("??????")
                                        .setMessage(" ????????? ????????? ??????????????????????????")
                                        .setNegativeButton("???", (dialog, which) -> {

                                            DocumentReference dr_pick_board = db.collection("Community")
                                                    .document("board").collection("item")
                                                    .document(boardModel.getBoard_index());

                                            dr_pick_board.collection("comment")
                                                    .document(commentModel.getIndex()).delete();

                                            dr_pick_board.get().addOnCompleteListener(task -> {
                                                DocumentSnapshot ds = task.getResult();

                                                int comment_count = Integer.valueOf(ds.get("comment_count").toString());

                                                comment_count -= 1;

                                                Map<String, Object> map = new HashMap<>();
                                                map.put("comment_count", comment_count);

                                                //
                                                tv_totalCommentCount.setText("?????? " + (comment_count) + "???");
                                                commentModel.setComment_count(String.valueOf(comment_count));
                                                boardModel.setComment_count(String.valueOf(comment_count));
                                                commentSize = comment_count;

                                                //
                                                dr_pick_board.update(map);

                                                //
                                                dataArrayList.remove(currentCommentLongClickPosition);
                                                commentAdapter.notifyItemRemoved(currentCommentLongClickPosition);
                                                commentAdapter.notifyItemRangeChanged(currentCommentLongClickPosition, dataArrayList.size());
                                            });
                                        })
                                        .setPositiveButton("?????????", (dialog, which) -> { });

                                AlertDialog dialog = builder.create();
                                dialog.show();
                            }
                            else{
                                AlertDialog.Builder builder = new AlertDialog.Builder(this)
                                        .setTitle("??????")
                                        .setMessage(" ????????? ???????????? ??????????????????????????")
                                        .setPositiveButton("??????", (dialog, which) -> { });

                                AlertDialog dialog = builder.create();
                                dialog.show();
                            }

                        });

                    }
                    Toast.makeText(this, "Add Comment !!", Toast.LENGTH_SHORT).show();
                }

                break;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        ScreenChangeDetect.getInstance().setBoardModel(boardModel);

    }

    @Override
    protected void onStop() {
        super.onStop();

        Log.i("pickBoard onStop", "check!!");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i("pickBoard onDestroy", "check!!");
    }


}
