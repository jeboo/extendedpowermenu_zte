package com.none.epm_zte;

import com.crossbowffs.remotepreferences.RemotePreferenceProvider;

public class MyPreferenceProvider extends RemotePreferenceProvider {
    public MyPreferenceProvider() {
        super("com.none.epm_zte", new String[] {"epm_prefs"});
    }
}
