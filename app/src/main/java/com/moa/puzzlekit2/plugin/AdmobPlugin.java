package com.moa.puzzlekit2.plugin;

import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.OnUserEarnedRewardListener;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.gms.ads.rewarded.RewardItem;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;
import com.moa.puzzlekit2.AdType;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;

public class AdmobPlugin extends CordovaPlugin {

    private static final String ACTION_INIT_ADMOB = "initAdmob";
    private static final String ACTION_SHOW_AD = "showAd";
    private static final String ACTION_OVERLAY_BANNER = "overlayBanner";
    private static final String ACTION_HIDE_BANNER = "hideBanner";

    //TEST
    private static final String KEY_INTERSTITIAL = "ca-app-pub-3940256099942544/1033173712";
    private static final String KEY_REWARD = "ca-app-pub-3940256099942544/5224354917";
    private static final String KEY_BANNER = "ca-app-pub-3940256099942544/6300978111";
    //REAL
    //private static  final String KEY_REWARD = "";
    //private static final String KEY_INTERSTITIAL = "ca-app-pub-4345829286689498/2433122416";
    //private static final String KEY_BANNER = "ca-app-pub-4345829286689498/9007512897";

    private static final String LOGTAG = "PUZZLE_LOG";

    private InterstitialAd mInterstitialAd = null;
    private RewardedAd mRewardedAd = null;
    private AdView adView = null;
    private RelativeLayout adViewLayout = null;

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {

        PluginResult result = null;

        Log.d(LOGTAG, "PLUGIN execute!");
        Log.d(LOGTAG, action);

        // 1. 광고 모듈 초기화
        if (ACTION_INIT_ADMOB.equals(action)) {
            initAdmob(callbackContext);
            return true;
        } else if (ACTION_SHOW_AD.equals(action)) {
            String param = args.getString(0);
            showAd(param, callbackContext);
            return true;
        } else if (ACTION_OVERLAY_BANNER.equals(action)) {
            boolean param = args.getBoolean(0);
            showBannerAd(param, callbackContext);
        } else if (ACTION_HIDE_BANNER.equals(action)) {
            boolean param = args.getBoolean(0);
            hideBannerAd(callbackContext);
        }
        return false;
    }

    public PluginResult initAdmob(CallbackContext callbackContext) {
        MobileAds.initialize(cordova.getContext());
        callbackContext.success();
        return null;
    }

    public PluginResult showAd(String adType, CallbackContext callbackContext) {
        Log.d(LOGTAG, adType);
        AdType adTypeCd = AdType.getEnum(adType);

        if (AdType.INTERSTITIAL == adTypeCd) {
            showInterstitialAd(callbackContext);
        } else if (AdType.REWARD == adTypeCd) {
            showRewardAd(callbackContext);
        }
        return null;
    }

    public void hideBannerAd(CallbackContext callbackContext) {
        Log.d(LOGTAG, "hideBannerAd");
        cordova.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (adView != null) {
                    ViewGroup parentView = (ViewGroup)adView.getParent();
                    if (parentView != null) parentView.removeView(adView);
                    adView.destroy();
                    adView = null;
                }
                callbackContext.success();
            }
        });
    }

    public void showBannerAd(boolean isShow, CallbackContext callbackContext) {
        Log.d(LOGTAG, "showBannerAd");
        cordova.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (adView == null) {
                    adView = new AdView(cordova.getActivity());
                    adView.setAdSize(AdSize.BANNER);
                    adView.setAdUnitId(KEY_BANNER);
                    adView.setAdListener(new AdListener() {
                        @Override
                        public void onAdClicked() {
                            super.onAdClicked();
                        }
                    });
                }
                if (adView.getParent() != null) {
                    ((ViewGroup)adView.getParent()).removeView(adView);
                }
                AdRequest adRequest = new AdRequest.Builder().build();
                adView.loadAd(adRequest);

                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                        RelativeLayout.LayoutParams.MATCH_PARENT,
                        RelativeLayout.LayoutParams.WRAP_CONTENT
                );
                params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);

                if (adViewLayout == null) {
                    adViewLayout = new RelativeLayout(cordova.getActivity());
                    RelativeLayout.LayoutParams initParams = new RelativeLayout.LayoutParams(
                            RelativeLayout.LayoutParams.MATCH_PARENT,
                            RelativeLayout.LayoutParams.MATCH_PARENT
                    );

                    try {
                        ((ViewGroup) (((View)webView.getClass().getMethod("getView").invoke(webView)).getParent())).addView(
                                adViewLayout, initParams
                        );
                    } catch (Exception e) {
                        ((ViewGroup) webView).addView(adViewLayout, initParams);
                    }
                }

                adViewLayout.addView(adView, params);
                adViewLayout.bringToFront();

                adView.setVisibility(View.VISIBLE);
                callbackContext.success();
            }
        });
    }

    public void showInterstitialAd(CallbackContext callbackContext) {
        Log.d(LOGTAG, "showInterstitialAd");
        cordova.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                AdRequest adRequest = new AdRequest.Builder().build();
                InterstitialAd.load(
                        cordova.getContext(),
                        KEY_INTERSTITIAL,
                        adRequest,
                        new InterstitialAdLoadCallback() {
                            @Override
                            public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                                Log.d(LOGTAG, "showInterstitialAd onLoad");
                                AdmobPlugin.this.mInterstitialAd = interstitialAd;
                                AdmobPlugin.this.mInterstitialAd.show(cordova.getActivity());
                                callbackContext.success();

                                AdmobPlugin.this.mInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                                    @Override
                                    public void onAdClicked() {
                                        Log.d(LOGTAG, "Ad was clicked");
                                    }

                                    @Override
                                    public void onAdDismissedFullScreenContent() {
                                        AdmobPlugin.this.mInterstitialAd = null;
                                    }

                                    @Override
                                    public void onAdFailedToShowFullScreenContent(AdError adError) {
                                        AdmobPlugin.this.mInterstitialAd = null;
                                    }

                                    @Override
                                    public void onAdImpression() {
                                        Log.d(LOGTAG, "Ad recorded an impression");
                                    }

                                    public void onAdShowedFullScreenContent() {
                                        Log.d(LOGTAG, "Ad showed fullscreen content");
                                    }
                                });
                            }

                            @Override
                            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                                Log.d(LOGTAG, "showInterstitialAd onError");
                                AdmobPlugin.this.mInterstitialAd = null;
                                Log.e(LOGTAG, loadAdError.toString());
                                callbackContext.error("Fail to load ad : " + loadAdError.getCode() + "-" + loadAdError.getMessage());
                            }
                        }
                );
            }
        });
    }

    public void showRewardAd(CallbackContext callbackContext) {
        cordova.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                AdRequest adRequest = new AdRequest.Builder().build();
                RewardedAd.load(
                        cordova.getContext(),
                        KEY_REWARD,
                        adRequest,
                        new RewardedAdLoadCallback() {
                            @Override
                            public void onAdLoaded(@NonNull RewardedAd rewardedAd) {
                                AdmobPlugin.this.mRewardedAd = rewardedAd;
                                AdmobPlugin.this.mRewardedAd.show(cordova.getActivity(), new OnUserEarnedRewardListener() {
                                    @Override
                                    public void onUserEarnedReward(@NonNull RewardItem rewardItem) {
                                        int rewardAmount = rewardItem.getAmount();
                                        String rewardType = rewardItem.getType();
                                    }
                                });
                            }

                            @Override
                            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                                AdmobPlugin.this.mRewardedAd = null;
                                Log.e(LOGTAG, loadAdError.toString());
                            }
                        }
                );
            }
        });
    }

//    public PluginResult loadInterstitialAd() {
//
//        cordova.getActivity().runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                AdRequest adRequest = new AdRequest.Builder().build();
//                InterstitialAd.load(
//                    cordova.getContext(),
//                    "ca-app-pub-3940256099942544/1033173712",
//                    adRequest,
//                    new InterstitialAdLoadCallback() {
//                        @Override
//                        public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
//                            AdmobPlugin.this.interstitialAd = interstitialAd;
//                            Toast.makeText(cordova.getContext(), "onAdLoaded()", Toast.LENGTH_SHORT).show();
//
//                            interstitialAd.setFullScreenContentCallback(
//                                new FullScreenContentCallback() {
//                                    @Override
//                                    public void onAdDismissedFullScreenContent() {
//                                        AdmobPlugin.this.interstitialAd = null;
//                                    }
//
//                                    @Override
//                                    public void onAdFailedToShowFullScreenContent(AdError adError) {
//                                        AdmobPlugin.this.interstitialAd = null;
//                                    }
//
//                                    @Override
//                                    public void onAdShowedFullScreenContent() {
//
//                                    }
//                                }
//                            );
//                        }
//
//                        @Override
//                        public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
//                            interstitialAd = null;
//                        }
//                    }
//                );
//            }
//        });
//
//        return null;
//    }
}
