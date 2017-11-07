package com.marker.menu;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.facebook.login.LoginManager;
import com.facebook.login.widget.ProfilePictureView;
import com.marker.LoginActivity;
import com.marker.R;
import com.marker.SettingsActivity;
import com.marker.SplashActivity;
import com.marker.about.AboutFragment;
import com.marker.app.GestorSesion;
import com.marker.lugar.destino.DestinoManager;
import com.marker.facebook.User;
import com.marker.lugar.history.HistoryActivity;
import com.marker.lugar.history.HistoryManager;
import com.marker.lugar.destino.DestinoActivity;

import butterknife.BindView;
import butterknife.ButterKnife;


public class MenuFragment extends Fragment implements NavigationView.OnNavigationItemSelectedListener {

    @BindView(R.id.fb_logout_button)
    Button mDrawerLogoutButton;
    @BindView(R.id.drawer_user_name)
    TextView mDrawerUserName;
    @BindView(R.id.drawer_user_email)
    TextView mDrawerUserMail;
    @BindView(R.id.drawer_user_picture)
    ProfilePictureView mDrawerUserPicture;


    public MenuFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_menu, container, false);
        ButterKnife.bind(this, view);

        initializeFacebookLogoutButton();
        initializeFacebookUserData(GestorSesion.getInstancia(getContext()).getUsuarioLoggeado());

        return view;
    }

    private void initializeFacebookLogoutButton() {
        mDrawerLogoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LoginManager.getInstance().logOut();

                Intent intent = new Intent(getActivity(), SplashActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        });
    }

    private void initializeFacebookUserData(User me) {
        mDrawerUserName.setText(me.getName());
        mDrawerUserMail.setText(me.getEmail());
        mDrawerUserPicture.setProfileId(me.getId());
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_destinies) {
            OnDestiniesPressed();
        } else if (id == R.id.nav_histories) {
            OnHistoriesPressed();
        } else if (id == R.id.nav_settings) {
            OnSettingsPressed();
        } else if (id == R.id.nav_info) {
            OnAboutPressed();
        } else if (id == R.id.nav_test_notification) {
            OnTestNotificationPressed();
        }
        return false;
    }


    public boolean OnAboutPressed(){
        AboutFragment af = new AboutFragment();
        af.show(getFragmentManager(), getTag());
        return true;
    }

    public void OnSettingsPressed() {
        startActivity(new Intent(getActivity(), SettingsActivity.class));
    }


    public void OnDestiniesPressed() {
        Intent childIntent = new Intent(getActivity(), DestinoActivity.class);
        getActivity().startActivityForResult(childIntent, MenuEnum.PICK_DESTINO_REQUEST);
    }

    public void OnHistoriesPressed() {
        Intent childIntent = new Intent(getActivity(), HistoryActivity.class);
        getActivity().startActivityForResult(childIntent, MenuEnum.PICK_HISTORY_REQUEST);
    }

    public void OnTestNotificationPressed() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        Uri notification = Uri.parse(sharedPreferences.getString("notifications_new_message_ringtone", "DEFAULT_SOUND"));
        Ringtone ringtone = RingtoneManager.getRingtone(getContext(), notification);
        if (ringtone == null){
            notification = RingtoneManager.getActualDefaultRingtoneUri(getContext(), RingtoneManager.TYPE_NOTIFICATION);
            ringtone = RingtoneManager.getRingtone(getContext(), notification);
        }
        ringtone.play();
        // Get instance of Vibrator from current Context
        Vibrator v = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);

        // Start without a delay
        // Each element then alternates between vibrate, sleep, vibrate, sleep...
        long[] pattern = {0, 100, 1000, 300, 200, 100, 500, 200, 100};

        // The '-1' here means to vibrate once, as '-1' is out of bounds in the pattern array
        v.vibrate(pattern, -1);

    }

}
