package com.none.epm_zte;

import android.content.res.XResources;
import android.content.Context;
import android.content.res.Resources;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.PowerManager;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.crossbowffs.remotepreferences.RemotePreferences;

import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.callbacks.XC_LayoutInflated;

import static android.view.Gravity.CENTER;
import static android.view.Gravity.VERTICAL_GRAVITY_MASK;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

public class epm_zte implements IXposedHookZygoteInit {
    @Override
    public void initZygote(StartupParam startupParam) throws Throwable {
        final XC_LayoutInflated hook = new XC_LayoutInflated() {
            @Override
            public void handleLayoutInflated(LayoutInflatedParam liparam) throws Throwable {
                Resources systemRes = XResources.getSystem();
                final Integer global_dialog_circle1 = systemRes.getIdentifier("circle1", "drawable", "androidzte");
                final Integer global_dialog_power = systemRes.getIdentifier("ic_lock_power", "drawable", "androidzte");
                final Integer global_dialog_reboot = systemRes.getIdentifier("ic_lock_reboot", "drawable", "androidzte");
                Integer newtopMargin = 0;
                String buttons[] = {"recovery", "edl", "bootloader"};
                Context context = liparam.view.getContext();
                RelativeLayout g_d = (RelativeLayout)liparam.view;

                SharedPreferences prefs = new RemotePreferences(context, "com.none.epm_zte", "epm_prefs");
                Boolean addBL = prefs.getBoolean("addBL", false);

                // if bootloader button enabled, move EDL to bottom/end
                if (addBL)
                {
                    buttons[1] = buttons[2];
                    buttons[2] = "edl";
                }

                if (liparam.variant.toString().equals("layout")) // portrait
                {
                    // Move existing buttons up in RL
                    for (int i = 1, j = g_d.getChildCount(); i < j; i++) {
                        RelativeLayout rl_temp = (RelativeLayout) g_d.getChildAt(i);
                        ViewGroup.MarginLayoutParams layoutParams1 = (ViewGroup.MarginLayoutParams)rl_temp.getLayoutParams();

                        layoutParams1.topMargin -= (addBL ? 480 : 320);
                        newtopMargin = layoutParams1.topMargin;
                    }

                    for (int i = 0; i < (addBL ? 3 : 2); i++)
                    {
                    // create new RL
                    RelativeLayout rl_new = new RelativeLayout(context);
//<RelativeLayout android:orientation="vertical" android:layout_width="wrap_content" android:layout_height="wrap_content"
// android:layout_marginTop="348.0dip" android:layout_marginStart="56.0dip" android:layout_alignParentStart="true">
                    rl_new.setGravity(VERTICAL_GRAVITY_MASK);
                    RelativeLayout.LayoutParams rl_newlp = new RelativeLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT);
                    rl_newlp.topMargin = newtopMargin + (i == 2 ? 1080 : 540);
                    if (i == 1)
                        rl_newlp.rightMargin = 296;
                    else
                        rl_newlp.leftMargin = (i == 2 ? 592 : 296);
                    rl_newlp.addRule(i == 1 ? RelativeLayout.ALIGN_PARENT_END : RelativeLayout.ALIGN_PARENT_START, RelativeLayout.TRUE);
                    rl_new.setLayoutParams(rl_newlp);

                    // create LL
                    LinearLayout ll_new = new LinearLayout(context);
//<LinearLayout android:gravity="center_horizontal" android:orientation="vertical" android:id="@id/reset_action"
// android:layout_width="wrap_content" android:layout_height="wrap_content">
                    ll_new.setGravity(1);
                    ll_new.setOrientation(LinearLayout.VERTICAL);
                    ll_new.setId(XResources.getFakeResId(buttons[i] + "_ll"));
                    ll_new.setLayoutParams(new LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT));
                    rl_new.addView(ll_new);

                    // create imagebutton
                    ImageButton imgbtn = new ImageButton(context);
//<ImageButton android:id="@id/reset" android:background="@drawable/circle1" android:clickable="false" android:layout_width="wrap_content"
// android:layout_height="wrap_content" android:src="@drawable/ic_lock_reboot" android:tint="#ffffffff" android:contentDescription="@string/global_action_reset"
// android:alpha="0.9" android:paddingStart="19.0dip" android:paddingEnd="19.0dip" />
                    imgbtn.setId(XResources.getFakeResId(buttons[i]));
                    imgbtn.setBackground(systemRes.getDrawable(global_dialog_circle1));
                    imgbtn.setClickable(false);
                    imgbtn.setLayoutParams(new ViewGroup.LayoutParams(WRAP_CONTENT, WRAP_CONTENT));
                    Drawable drawable = systemRes.getDrawable(buttons[i].length() <= 3 ? global_dialog_power : global_dialog_reboot);
                    imgbtn.setImageDrawable(drawable);
                    imgbtn.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
                    imgbtn.setContentDescription(buttons[i]);
                    imgbtn.setAlpha((float) 0.9);
                    // hook code for button (reason passed via ContentDescription)
                    imgbtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View arg0) {
                            try {
                                //XposedBridge.log("Clicked = " + arg0.getContentDescription());
                                PowerManager pm = (PowerManager) arg0.getContext().getSystemService(Context.POWER_SERVICE);
                                pm.reboot(arg0.getContentDescription().toString());
                            } catch (Throwable ex) {
                            }
                        }
                    });
                    ll_new.addView(imgbtn);

                    // lastly, our text captions
                    TextView txtview = new TextView(context);
// <TextView android:textSize="12.0sp" android:textColor="@color/global_action_text" android:gravity="center" android:id="@id/text_reset"
// android:paddingTop="10.0dip" android:layout_width="110.0dip" android:layout_height="wrap_content" android:text="@string/global_action_reset" android:maxLines="2" />
                    txtview.setTextSize(TypedValue.COMPLEX_UNIT_SP, (float) 12.0);
                    txtview.setTextColor(Color.WHITE);
                    txtview.setGravity(CENTER);
                    txtview.setId(XResources.getFakeResId(buttons[i] + "_tv"));
                    txtview.setPadding(0, 40, 0, 0);
                    txtview.setWidth(110);
                    txtview.setText(buttons[i].length() > 3 ? buttons[i].substring(0, 1).toUpperCase() + buttons[i].substring(1) : buttons[i].toUpperCase());
                    txtview.setMaxLines(2);
                    ll_new.addView(txtview);

                    // finally, add newly constructed RL (containing LL, ImgBtn, TV)
                    g_d.addView((View) rl_new);
                }
                }
                else // landscape, only major difference is encasement in a LL and auto-centering
                {
                    for (int i = 0; i < (addBL ? 3 : 2); i++)
                    {
                        LinearLayout ll_old = (LinearLayout) g_d.getChildAt(i > 0 ? 2 : 1);

                        // create new LL
                        LinearLayout ll_new = new LinearLayout(context);
//<LinearLayout android:gravity="center_horizontal" android:orientation="vertical" android:id="@id/airplanemode"
// android:layout_width="wrap_content" android:layout_height="wrap_content" android:layout_marginEnd="14.0dip">
                        ll_new.setGravity(VERTICAL_GRAVITY_MASK);
                        ll_new.setOrientation(LinearLayout.VERTICAL);
                        ll_new.setId(XResources.getFakeResId(buttons[i] + "_ll"));
                        LinearLayout.LayoutParams ll_newlp = new LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT);
                        ll_newlp.setMarginStart(184);
                        ll_newlp.setMarginEnd(64);
                        ll_new.setLayoutParams(ll_newlp);
                        ll_old.addView(ll_new);

                        // create imagebutton
                        ImageButton imgbtn = new ImageButton(context);
//<ImageButton android:id="@id/airplane" android:background="@drawable/circle1" android:clickable="false" android:layout_width="wrap_content"
// android:layout_height="wrap_content" android:tint="#ffffffff" android:alpha="0.9" android:paddingStart="19.0dip" android:paddingEnd="19.0dip" />
                        imgbtn.setId(XResources.getFakeResId(buttons[i]));
                        imgbtn.setBackground(systemRes.getDrawable(global_dialog_circle1));
                        imgbtn.setClickable(false);
                        imgbtn.setLayoutParams(new ViewGroup.LayoutParams(WRAP_CONTENT, WRAP_CONTENT));
                        Drawable drawable = systemRes.getDrawable(buttons[i].length() <= 3 ? global_dialog_power : global_dialog_reboot);
                        imgbtn.setImageDrawable(drawable);
                        imgbtn.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
                        imgbtn.setContentDescription(buttons[i]);
                        imgbtn.setAlpha((float) 0.9);
                        // hook code for button (reason passed via ContentDescription)
                        imgbtn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View arg0) {
                                try {
                                    //XposedBridge.log("Clicked = " + arg0.getContentDescription());
                                    PowerManager pm = (PowerManager) arg0.getContext().getSystemService(Context.POWER_SERVICE);
                                    pm.reboot(arg0.getContentDescription().toString());
                                } catch (Throwable ex) {
                                }
                            }
                        });
                        ll_new.addView(imgbtn);

                        // lastly, our text captions
                        TextView txtview = new TextView(context);
//<TextView android:textSize="12.0sp" android:textColor="@color/global_action_text" android:gravity="center" android:id="@id/text_airplane"
// android:paddingTop="10.0dip" android:layout_width="110.0dip" android:layout_height="wrap_content" android:maxLines="2" />
                        txtview.setTextSize(TypedValue.COMPLEX_UNIT_SP, (float) 12.0);
                        txtview.setTextColor(Color.WHITE);
                        txtview.setGravity(CENTER);
                        txtview.setId(XResources.getFakeResId(buttons[i] + "_tv"));
                        txtview.setPadding(0, 40, 0, 0);
                        txtview.setWidth(110);
                        txtview.setText(buttons[i].length() > 3 ? buttons[i].substring(0, 1).toUpperCase() + buttons[i].substring(1) : buttons[i].toUpperCase());
                        txtview.setMaxLines(2);
                        ll_new.addView(txtview);
                    }
                }
            }
        };

        XResources.hookSystemWideLayout("androidzte", "layout", "global_dialog", hook);
    }
}

