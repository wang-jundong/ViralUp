package com.qboxus.musictok.ActivitesFragment.WalletAndWithdraw;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.qboxus.musictok.SimpleClasses.AppCompatLocaleActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.ConsumeParams;
import com.android.billingclient.api.ConsumeResponseListener;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsParams;
import com.android.billingclient.api.SkuDetailsResponseListener;
import com.qboxus.musictok.ApiClasses.ApiLinks;
import com.volley.plus.VPackages.VolleyRequest;
import com.qboxus.musictok.Constants;
import com.qboxus.musictok.Interfaces.AdapterClickListener;
import com.volley.plus.interfaces.Callback;
import com.qboxus.musictok.Models.UserModel;
import com.qboxus.musictok.R;
import com.qboxus.musictok.SimpleClasses.DataParsing;
import com.qboxus.musictok.SimpleClasses.Functions;
import com.qboxus.musictok.SimpleClasses.Variables;

import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

import static com.android.billingclient.api.BillingClient.SkuType.INAPP;

public class MyWallet extends AppCompatLocaleActivity  implements View.OnClickListener, PurchasesUpdatedListener {

    RecyclerView recyclerView;
    MyWalletAdapter adapter ;
    ArrayList<WalletModel> datalist = new ArrayList<>();
    TextView coins_txt;

    WalletModel select_wallet_model;
    List<SkuDetails> storedList=new ArrayList<>();
    BillingClient billingClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Functions.setLocale(Functions.getSharedPreference(MyWallet.this).getString(Variables.APP_LANGUAGE_CODE,Variables.DEFAULT_LANGUAGE_CODE)
                , this, MyWallet.class,false);
        setContentView(R.layout.activity_my_wallet);

        select_wallet_model=new WalletModel();
        coins_txt=findViewById(R.id.coins_txt);

        findViewById(R.id.back_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });



        findViewById(R.id.tab_cashout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resultCallback.launch(new Intent(MyWallet.this, WithdrawCoinsA.class));
            }
        });

        initalizeBill();
        init_views();
    }


    ActivityResultLauncher<Intent> resultCallback = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK ) {
                        Intent data = result.getData();
                        if (data.getBooleanExtra("isShow",false))
                        {
                            SetUpScreenData();
                        }

                    }
                }
            });


    public void initalizeBill(){
        billingClient=BillingClient.newBuilder(MyWallet.this).enablePendingPurchases().setListener(this).build();
        Log.d(Constants.tag,"Billing : connection establish ");
        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(@NonNull BillingResult billingResult) {
                Log.d(Constants.tag,"Billing : onBillingSetupFinished ");
                if (billingResult.getResponseCode()==BillingClient.BillingResponseCode.OK)
                {
                    Log.d(Constants.tag,"Billing : Load purchase query "+billingResult.getResponseCode());

                    InitPurchases();
                }
                else
                {
                    Log.d(Constants.tag,"Billing : onBillingSetupFinished "+billingResult.getResponseCode());
                }
            }

            @Override
            public void onBillingServiceDisconnected() {
                Log.d(Constants.tag,"Billing : onBillingServiceDisconnected");
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        SetUpScreenData();
    }

    private void SetUpScreenData() {
        coins_txt.setText(""+Functions.getSharedPreference(MyWallet.this).getString(Variables.U_WALLET, "0"));
    }




    @Override
    public void onPurchasesUpdated(@NonNull BillingResult billingResult, @Nullable List<Purchase> purchases) {
        if (billingResult.getResponseCode()==BillingClient.BillingResponseCode.OK && purchases!=null )
        {
            Log.d(Constants.tag,"Billing : handle purchase okay");
            handlePurchases(purchases);
        }
        else
        if (billingResult.getResponseCode()==BillingClient.BillingResponseCode.USER_CANCELED)
        {
            Log.d(Constants.tag,"Billing : handle purchase cancel");
        }
        else
        {
            Log.d(Constants.tag,"Billing : handle purchase other "+billingResult.getDebugMessage());
        }
    }


    private void handlePurchases(List<Purchase> purchases) {
        for (Purchase skuList:purchases)
        {
            Log.d(Constants.tag,"Billing : purchase "+skuList.getSkus().get(0)+" acknowledged status "+skuList.isAcknowledged()+" purchase status "+(skuList.getPurchaseState()==Purchase.PurchaseState.PURCHASED));

            if (Constants.Product_ID0.equals(skuList.getSkus().get(0)) && skuList.getPurchaseState()==Purchase.PurchaseState.PURCHASED)
            {
// && skuList.getPurchaseState()==Purchase.PurchaseState.PURCHASED
                if (!skuList.isAcknowledged())
                {

                    Log.d(Constants.tag,"Consume check : "+(!skuList.isAcknowledged()));
                    ConsumeParams consumeParams =
                            ConsumeParams.newBuilder()
                                    .setPurchaseToken(skuList.getPurchaseToken())
                                    .build();

                    ConsumeResponseListener listener = new ConsumeResponseListener() {
                        @Override
                        public void onConsumeResponse(BillingResult billingResult, String purchaseToken) {
                            if (billingResult.getResponseCode()==BillingClient.BillingResponseCode.OK )
                            {
                                Log.d(Constants.tag,Constants.Product_ID0+"---"+select_wallet_model.coins);
                                Call_api_update_wallet(select_wallet_model.coins+" coins",select_wallet_model.coins,select_wallet_model.price,skuList.getPurchaseToken());

                            }
                        }
                    };
                    billingClient.consumeAsync(consumeParams, listener);
                }

            }
            else
            if (Constants.Product_ID1.equals(skuList.getSkus().get(0)) && skuList.getPurchaseState()==Purchase.PurchaseState.PURCHASED)
            {

                if (!skuList.isAcknowledged())
                {

                    ConsumeParams consumeParams =
                            ConsumeParams.newBuilder()
                                    .setPurchaseToken(skuList.getPurchaseToken())
                                    .build();

                    ConsumeResponseListener listener = new ConsumeResponseListener() {
                        @Override
                        public void onConsumeResponse(BillingResult billingResult, String purchaseToken) {
                            if (billingResult.getResponseCode()==BillingClient.BillingResponseCode.OK )
                            {
                                Log.d(Constants.tag,Constants.Product_ID1+"---"+select_wallet_model.coins);
                                Call_api_update_wallet(select_wallet_model.coins+" coins",select_wallet_model.coins,select_wallet_model.price,skuList.getPurchaseToken());

                            }
                        }
                    };
                    billingClient.consumeAsync(consumeParams, listener);
                }

            }
            else
            if (Constants.Product_ID2.equals(skuList.getSkus().get(0)) && skuList.getPurchaseState()==Purchase.PurchaseState.PURCHASED)
            {

                if (!skuList.isAcknowledged())
                {


                    ConsumeParams consumeParams =
                            ConsumeParams.newBuilder()
                                    .setPurchaseToken(skuList.getPurchaseToken())
                                    .build();

                    ConsumeResponseListener listener = new ConsumeResponseListener() {
                        @Override
                        public void onConsumeResponse(BillingResult billingResult, String purchaseToken) {
                            if (billingResult.getResponseCode()==BillingClient.BillingResponseCode.OK )
                            {
                                Log.d(Constants.tag,Constants.Product_ID2+"---"+select_wallet_model.coins);
                                Call_api_update_wallet(select_wallet_model.coins+" coins",select_wallet_model.coins,select_wallet_model.price,skuList.getPurchaseToken());

                            }
                        }
                    };
                    billingClient.consumeAsync(consumeParams, listener);

                }

            }
            else
            if (Constants.Product_ID3.equals(skuList.getSkus().get(0)) && skuList.getPurchaseState()==Purchase.PurchaseState.PURCHASED)
            {

                if (!skuList.isAcknowledged())
                {

                    ConsumeParams consumeParams =
                            ConsumeParams.newBuilder()
                                    .setPurchaseToken(skuList.getPurchaseToken())
                                    .build();

                    ConsumeResponseListener listener = new ConsumeResponseListener() {
                        @Override
                        public void onConsumeResponse(BillingResult billingResult, String purchaseToken) {
                            if (billingResult.getResponseCode()==BillingClient.BillingResponseCode.OK )
                            {
                                Log.d(Constants.tag,Constants.Product_ID3+"---"+select_wallet_model.coins);
                                Call_api_update_wallet(select_wallet_model.coins+" coins",select_wallet_model.coins,select_wallet_model.price,skuList.getPurchaseToken());

                            }
                        }
                    };
                    billingClient.consumeAsync(consumeParams, listener);

                }

            }
            else
            if (Constants.Product_ID4.equals(skuList.getSkus().get(0)) && skuList.getPurchaseState()==Purchase.PurchaseState.PURCHASED)
            {

                if (!skuList.isAcknowledged())
                {

                    ConsumeParams consumeParams =
                            ConsumeParams.newBuilder()
                                    .setPurchaseToken(skuList.getPurchaseToken())
                                    .build();

                    ConsumeResponseListener listener = new ConsumeResponseListener() {
                        @Override
                        public void onConsumeResponse(BillingResult billingResult, String purchaseToken) {
                            if (billingResult.getResponseCode()==BillingClient.BillingResponseCode.OK )
                            {
                                Log.d(Constants.tag,Constants.Product_ID4+"---"+select_wallet_model.coins);
                                Call_api_update_wallet(select_wallet_model.coins+" coins",select_wallet_model.coins,select_wallet_model.price,skuList.getPurchaseToken());

                            }
                        }
                    };
                    billingClient.consumeAsync(consumeParams, listener);

                }

            }
        }
    }



    // when we click the continue btn this method will call
    public void Puchase_item(int postion) {
        purchaseSubscription(postion);
    }


    public void purchaseSubscription(int postion){
        if (billingClient.isReady())
        {
            switch (postion)
            {
                case 0:
                {
                    for (SkuDetails detail:storedList)
                    {
                        if (detail.getSku().equalsIgnoreCase(Constants.Product_ID0))
                        {
                            Log.d(Constants.tag,"purchaseSubscription position "+postion+" "+detail.getSku()+" "+detail.getPrice());
                            BillingFlowParams flowParams=BillingFlowParams.newBuilder()
                                    .setSkuDetails(detail)
                                    .build();
                            billingClient.launchBillingFlow(MyWallet.this,flowParams);
                        }
                    }
                }
                break;
                case 1:
                {
                    for (SkuDetails detail:storedList)
                    {
                        if (detail.getSku().equalsIgnoreCase(Constants.Product_ID1))
                        {
                            Log.d(Constants.tag,"purchaseSubscription position "+postion+" "+detail.getSku()+" "+detail.getPrice());
                            BillingFlowParams flowParams=BillingFlowParams.newBuilder()
                                    .setSkuDetails(detail)
                                    .build();
                            billingClient.launchBillingFlow(MyWallet.this,flowParams);
                        }
                    }
                }
                break;
                case 2:
                {
                    for (SkuDetails detail:storedList)
                    {
                        if (detail.getSku().equalsIgnoreCase(Constants.Product_ID2))
                        {
                            Log.d(Constants.tag,"purchaseSubscription position "+postion+" "+detail.getSku()+" "+detail.getPrice());
                            BillingFlowParams flowParams=BillingFlowParams.newBuilder()
                                    .setSkuDetails(detail)
                                    .build();
                            billingClient.launchBillingFlow(MyWallet.this,flowParams);
                        }
                    }
                }
                break;
                case 3:
                {
                    for (SkuDetails detail:storedList)
                    {
                        if (detail.getSku().equalsIgnoreCase(Constants.Product_ID3))
                        {
                            Log.d(Constants.tag,"purchaseSubscription position "+postion+" "+detail.getSku()+" "+detail.getPrice());
                            BillingFlowParams flowParams=BillingFlowParams.newBuilder()
                                    .setSkuDetails(detail)
                                    .build();
                            billingClient.launchBillingFlow(MyWallet.this,flowParams);
                        }
                    }
                }
                break;
                case 4:
                {
                    for (SkuDetails detail:storedList)
                    {
                        if (detail.getSku().equalsIgnoreCase(Constants.Product_ID4))
                        {
                            Log.d(Constants.tag,"purchaseSubscription position "+postion+" "+detail.getSku()+" "+detail.getPrice());
                            BillingFlowParams flowParams=BillingFlowParams.newBuilder()
                                    .setSkuDetails(detail)
                                    .build();
                            billingClient.launchBillingFlow(MyWallet.this,flowParams);
                        }
                    }
                }
                break;
            }

        }
        else
        {
            Log.d(Constants.tag,"Billing : purchaseSubscription ");
        }

    }



    private void InitPurchases() {

        Log.d(Constants.tag,"Billing : InitPurchases ");

        final List<String> skuList=new ArrayList<>();
        skuList.add(Constants.Product_ID0);
        skuList.add(Constants.Product_ID1);
        skuList.add(Constants.Product_ID2);
        skuList.add(Constants.Product_ID3);
        skuList.add(Constants.Product_ID4);
        SkuDetailsParams.Builder params=SkuDetailsParams.newBuilder();
        params.setSkusList(skuList);
        params.setType(INAPP);
        billingClient.querySkuDetailsAsync(params.build()
                , new SkuDetailsResponseListener() {
                    @Override
                    public void onSkuDetailsResponse(@NonNull BillingResult billingResult, @Nullable List<SkuDetails> list) {

                        storedList.clear();
                        storedList.addAll(list);
                        Log.d(Constants.tag,"Billing : onSkuDetailsResponse ");
                    }
                });

    }





    @Override
    public void onClick(View v) {

    }



    private void init_views() {

        recyclerView = findViewById(R.id.recylerview);

        datalist.clear();
        datalist.add(new WalletModel("",Constants.COINS0,Constants.PRICE0));
        datalist.add(new WalletModel("",Constants.COINS1,Constants.PRICE1));
        datalist.add(new WalletModel("",Constants.COINS2,Constants.PRICE2));
        datalist.add(new WalletModel("",Constants.COINS3,Constants.PRICE3));
        datalist.add(new WalletModel("",Constants.COINS4,Constants.PRICE4));

        adapter = new MyWalletAdapter(this, datalist, new AdapterClickListener() {
            @Override
            public void onItemClick(View view, int pos, Object object) {
                select_wallet_model=(WalletModel) object;
                //Call_api_update_wallet(wallet_model.coins+" coins",wallet_model.coins,wallet_model.price,"test_token");
                Puchase_item(pos);
            }
        });
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(adapter);
    }






    public void Call_api_update_wallet(String name,String coins,String price,String Tid){

        JSONObject params=new JSONObject();
        try {
            params.put("user_id",Functions.getSharedPreference(MyWallet.this).getString(Variables.U_ID,""));
            params.put("coin",coins);
            params.put("title",name);
            params.put("price",price.replace("$",""));
            params.put("transaction_id",Tid);
            params.put("device","android");
        } catch (Exception e) {
            Log.d(Constants.tag,"Exception : "+e);
        }

      MyWallet.super.runOnUiThread(new Runnable() {
          @Override
          public void run() {
              Functions.showLoader(MyWallet.this,false,false);
              VolleyRequest.JsonPostRequest(MyWallet.this, ApiLinks.purchaseCoin, params,Functions.getHeaders(MyWallet.this), new Callback() {
                  @Override
                  public void onResponce(String resp) {
                Functions.checkStatus(MyWallet.this,resp);
                      Functions.cancelLoader();
                      Log.d(Constants.tag,"get coins before run \n"+params);
                      try {
                          JSONObject jsonObject=new JSONObject(resp);

                          String code=jsonObject.optString("code");
                          if(code!=null && code.equals("200")){
                              JSONObject msgObj=jsonObject.getJSONObject("msg");
                              UserModel userDetailModel= DataParsing.getUserDataModel(msgObj.optJSONObject("User"));
                              SharedPreferences.Editor editor = Functions.getSharedPreference(MyWallet.this).edit();
                              editor.putString(Variables.U_WALLET, ""+userDetailModel.getWallet());
                              editor.commit();
                              SetUpScreenData();
                          }
                      } catch (Exception e) {
                          Log.d(Constants.tag,"Exception : "+e);
                      }


                  }
              });
          }
      });
    }


    @Override
    protected void onDestroy() {
        billingClient.endConnection();
        super.onDestroy();
    }
}
