package com.qboxus.musictok.ActivitesFragment.SendGift;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.qboxus.musictok.ActivitesFragment.WalletAndWithdraw.MyWallet;
import com.qboxus.musictok.ApiClasses.ApiLinks;
import com.volley.plus.VPackages.VolleyRequest;
import com.qboxus.musictok.Constants;
import com.volley.plus.interfaces.Callback;
import com.qboxus.musictok.Interfaces.FragmentCallBack;
import com.qboxus.musictok.Models.UserModel;
import com.qboxus.musictok.R;
import com.qboxus.musictok.SimpleClasses.DataParsing;
import com.qboxus.musictok.SimpleClasses.Functions;
import com.qboxus.musictok.SimpleClasses.Variables;
import com.smarteist.autoimageslider.SliderView;

import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

import io.paperdb.Paper;

/**
 * A simple {@link Fragment} subclass.
 */
public class StickerGiftF extends BottomSheetDialogFragment {

    View view;
    Context context;
    TextView coins_txt;
    double total_coins=0;
    Button tab_send_gift;
    SendGiftVHAdapter giftSliderAdapter;
    List<List<StickerModel>> sliderList=new ArrayList<>();
    List<StickerModel> data_list=new ArrayList<>();
    StickerModel selectedModel;
    ProgressBar progressBar;
    SliderView imageSlider;

    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable runnable;

    String id,name,image;
    int giftCounter=0;

    FragmentCallBack callBack;


    public StickerGiftF() {

    }

    public StickerGiftF(String id, String name, String image, FragmentCallBack callBack) {
        this.id=id;
        this.name=name;
        this.image=image;
        this.callBack = callBack;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view= inflater.inflate(R.layout.fragment_sticker_gift, container, false);
        context=getContext();

        coins_txt=view.findViewById(R.id.coins_txt);
        progressBar=view.findViewById(R.id.progressBar);
        String wallet =""+Functions.getSharedPreference(view.getContext()).getString(Variables.U_WALLET, "0");
        total_coins=Double.parseDouble(wallet);
        coins_txt.setText(wallet);
        tab_send_gift=view.findViewById(R.id.tab_send_gift);
        imageSlider=view.findViewById(R.id.imageSlider);
        SetUpGiftSliderAdapter();

        view.findViewById(R.id.recharge_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                startActivity(new Intent(getActivity(), MyWallet.class));
            }
        });


        tab_send_gift.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                giftCounter=giftCounter+1;

                Bundle bundle=new Bundle();
                bundle.putBoolean("isShow",false);
                bundle.putBoolean("showCount",true);
                bundle.putString("count",""+giftCounter);
                bundle.putSerializable("Data",selectedModel);
                callBack.onResponce(bundle);

                handler.removeCallbacks(runnable);
                runnable=new Runnable() {
                    @Override
                    public void run() {

                        for(StickerModel model:data_list)
                        {
                            if (model.isSelected)
                            {

                                double coin_required=(Double.valueOf(model.coins)*giftCounter);
                                if(total_coins!=0 && coin_required!=0){

                                    if(total_coins>=coin_required){
                                        Call_api_Send_sticker(model,giftCounter);
                                        giftCounter=0;
                                    }
                                    else {
                                        giftCounter=0;
                                        Toast.makeText(context, context.getString(R.string.you_dont_have_sufficent_coins), Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }
                        }


                    }
                };
                handler.postDelayed(runnable, 1000);



            }
        });


        TextView username_txt=view.findViewById(R.id.username_txt);
        username_txt.setText(view.getContext().getString(R.string.send_gift_to)+" "+name);

        hitSHowGiftScreen();
        return view;

    }

    private void hitSHowGiftScreen() {
        {
            if (Paper.book("Gift").contains("giftList"))
            {
                data_list.clear();
                data_list.addAll(Paper.book("Gift").read("giftList"));
                sliderList.clear();
                sliderList.addAll(Functions.createChunksOfList(data_list,6));
                giftSliderAdapter.notifyDataSetChanged();
            }
        }
        if (data_list.size()<1)
        {
            progressBar.setVisibility(View.VISIBLE);
        }
        VolleyRequest.JsonPostRequest(getActivity(), ApiLinks.showGifts,  new JSONObject(),Functions.getHeaders(getActivity()), new Callback() {
            @Override
            public void onResponce(String resp) {
                Functions.checkStatus(getActivity(),resp);
                progressBar.setVisibility(View.GONE);
                if (resp!=null){

                    try {

                        JSONObject jsonObject=new JSONObject(resp);

                        String code=jsonObject.optString("code");
                        if(code!=null && code.equals("200")){
                            JSONArray msgarray = jsonObject.getJSONArray("msg");

                            data_list.clear();
                            for (int i = 0; i<msgarray.length(); i++){
                                JSONObject giftArray=msgarray.getJSONObject(i);
                                JSONObject giftObj=giftArray.getJSONObject("Gift");
                                StickerModel model = new StickerModel();
                                model.id=giftObj.optString("id");
                                model.image =giftObj.optString("image");
                                model.name=giftObj.optString("title");
                                model.coins=giftObj.optString("coin","0");
                                model.isSelected=false;
                                model.count=0;
                                data_list.add(model);
                            }
                            Paper.book("Gift").write("giftList",data_list);
                            {
                                data_list.clear();
                                data_list.addAll(Paper.book("Gift").read("giftList"));
                                sliderList.clear();
                                sliderList.addAll(Functions.createChunksOfList(data_list,6));
                                giftSliderAdapter.notifyDataSetChanged();
                            }

                        }
                        else
                        {
                            Functions.showAlert(view.getContext(), "Server Reponse", jsonObject.optString("msg","Our technical team work on this issue"));

                        }

                    } catch (Exception e) {
                        Log.d(Constants.tag,"Exception : "+e);
                    }

                }

            }
        });
    }

    private void SetUpGiftSliderAdapter() {
        sliderList.clear();
        sliderList.addAll(Functions.createChunksOfList(data_list,6));
        giftSliderAdapter=new SendGiftVHAdapter(sliderList,tab_send_gift, new FragmentCallBack() {
            @Override
            public void onResponce(Bundle bundle) {
                if (bundle.getBoolean("isShow",false))
                {
                    selectedModel= (StickerModel) bundle.getSerializable("Data");
                }
            }
        });
        imageSlider.setSliderAdapter(giftSliderAdapter);
    }



    public void Call_api_Send_sticker(StickerModel model, int giftCount){
        JSONObject params=new JSONObject();
        try {
            params.put("sender_id",Variables.sharedPreferences.getString(Variables.U_ID,""));
            params.put("receiver_id",id);
            params.put("gift_id",model.id);
            params.put("gift_count",giftCount);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Functions.showLoader(context,false,false);
        VolleyRequest.JsonPostRequest(getActivity(), ApiLinks.sendGift, params,Functions.getHeaders(getActivity()), new Callback() {
            @Override
            public void onResponce(String resp) {
                Functions.checkStatus(getActivity(),resp);
                Functions.cancelLoader();

                try {
                    JSONObject jsonObject=new JSONObject(resp);
                    String code=jsonObject.optString("code");
                    if(code!=null && code.equals("200")){
                        JSONObject msgObj=jsonObject.getJSONObject("msg");
                        UserModel userDetailModel= DataParsing.getUserDataModel(msgObj.optJSONObject("User"));
                        SharedPreferences.Editor editor = Functions.getSharedPreference(view.getContext()).edit();
                        editor.putString(Variables.U_WALLET, ""+userDetailModel.getWallet());
                        editor.commit();

                        Bundle bundle=new Bundle();
                        bundle.putBoolean("isShow",true);
                        bundle.putString("count",""+giftCount);
                        bundle.putSerializable("Data",model);
                        callBack.onResponce(bundle);
                        dismiss();
                    }
                    else
                    if(code!=null && code.equals("201"))
                    {
                        Functions.showAlert(view.getContext(),getString(R.string.server_error),""+jsonObject.optString("msg"));
                    }
                    else
                        Toast.makeText(context, ""+jsonObject.optString("msg"), Toast.LENGTH_SHORT).show();

                } catch (Exception e) {
                    e.printStackTrace();
                }


            }
        });

    }



}
