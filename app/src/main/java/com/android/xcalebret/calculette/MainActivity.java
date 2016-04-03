package com.android.xcalebret.calculette;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.xcalebret.calculette.model.Calculette;
import com.android.xcalebret.calculette.model.stack.Pile2;
import com.android.xcalebret.calculette.model.CalculetteException;

import java.util.Observable;
import java.util.Observer;

public class MainActivity extends AppCompatActivity
{
    private final static String DEBUG = "AndroCalc >>> ";
    private final static int PERMISSIONS_REQUEST_SEND_SMS = 42;
    private PlaceholderFragment mainFrag;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        Toolbar myToolbar = (Toolbar) findViewById(R.id.calc_toolbar);
        setSupportActionBar(myToolbar);

        if (savedInstanceState == null)
        {
            /* bind anonymous generic fragment to the activity */
            mainFrag = new PlaceholderFragment();
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, mainFrag)
                    .commit();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[],
                                           int[] grantResults)
    {
        mainFrag.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment
            implements Observer, View.OnClickListener
    {
        private final static String PREFS = "prefs";

        private String smsPref;
        private String toastPref;
        private String toastOnStr;
        private String toastOffStr;
        private String smsOffStr;
        private String smsActivityStr;
        private String smsServiceStr;
        private int toastDefault;
        private int toastOff;
        private int toastOn;
        private int smsDefault;
        private int smsOff;
        private int smsActivity;
        private int smsService;
        private int calcStackCapacity;

        private View rootView;
        private Context rootContext;
        private Calculette calculette;

        public PlaceholderFragment()
        {
        }

        @Override
        public void onAttach(Context context)
        {
            super.onAttach(context);
            rootContext = context;
            initPreferencies();

            smsPref = getResources().getString(R.string.pref_sms_name);
            toastPref = getResources().getString(R.string.pref_toast_name);
            toastOnStr = getResources().getString(R.string.toast_on_str);
            toastOffStr = getResources().getString(R.string.toast_off_str);
            smsOffStr = getResources().getString(R.string.no_sms_str);
            smsActivityStr = getResources().getString(R.string.activity_sms_str );
            smsServiceStr = getResources().getString(R.string.service_sms_str);

            toastOff = getResources().getInteger(R.integer.toast_off);
            toastOn = getResources().getInteger(R.integer.toast_on);
            smsOff = getResources().getInteger(R.integer.sms_off);
            smsActivity = getResources().getInteger(R.integer.sms_activity);
            smsService = getResources().getInteger(R.integer.sms_service);
            toastDefault = getResources().getInteger(R.integer.toast);
            smsDefault = getResources().getInteger(R.integer.sms);
            calcStackCapacity = getResources().getInteger(R.integer.calc_stack_capacity);
        }

        /**
         * Inflate Fragment with the appropriate Layout.
         * Create and bind calculette model to the fragment.
         */
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState)
        {
            Log.i(DEBUG, "onCreateView : build calculette model");
            setHasOptionsMenu(true);

            calculette = new Calculette(new Pile2<Integer>(calcStackCapacity));
            calculette.addObserver(this);

            rootView = inflater.inflate(R.layout.fragment_main, container, false);
            rootView.findViewById(R.id.empiler).setOnClickListener(this);
            rootView.findViewById(R.id.plus).setOnClickListener(this);
            rootView.findViewById(R.id.moins).setOnClickListener(this);
            rootView.findViewById(R.id.div).setOnClickListener(this);
            rootView.findViewById(R.id.mul).setOnClickListener(this);
            rootView.findViewById(R.id.undo).setOnClickListener(this);
            rootView.findViewById(R.id.clear).setOnClickListener(this);

            return rootView;
        }

        @Override
        public void onClick(View v)
        {
            switch (v.getId())
            {
                case R.id.empiler:
                    empiler();
                    break;
                case R.id.plus:
                    add();
                    break;
                case R.id.moins:
                    sub();
                    break;
                case R.id.mul:
                    mul();
                    break;
                case R.id.div:
                    div();
                    break;
                case R.id.clear:
                    clear();
                    break;
                case R.id.undo:
                    undo();
                    break;
            }
        }

        @Override
        public void onRequestPermissionsResult(int requestCode, String permissions[],
                                               int[] grantResults)
        {
            switch (requestCode)
            {
                case PERMISSIONS_REQUEST_SEND_SMS:
                {
                /* If request is cancelled, the result arrays are empty */
                    if (grantResults.length > 0
                            && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    {
                        Log.i(DEBUG, "SMS_SEND perm granted");
                        enableServiceSms();
                    }
                /* permission denied */
                    else
                    {
                        Log.i(DEBUG, "SMS_SEND perm denied");
                        disableServiceSms();
                    }
                    break;
                }
            }
        }

        /**
         * Update visual elements in the view.
         * Occurs when notifications are sent by the model.
         *
         * @param observable Observable object which causing update (here Calculette)
         * @param data       the data passed to notifyObservers (here int|null)
         */
        @Override
        public void update(Observable observable, Object data)
        {
            EditText printed = (EditText) rootView.findViewById(R.id.printed);
            TextView stack = (TextView) rootView.findViewById(R.id.stack);
            TextView result = (TextView) rootView.findViewById(R.id.result);
            ProgressBar bar = (ProgressBar) rootView.findViewById(R.id.capacity);
            Calculette calc = (Calculette) observable; // we could use this.calculette in this context

            /* display calculette result */
            try
            {
                result.setText(Integer.toString(calc.result()));
            }
            catch (CalculetteException e)
            {
                result.setText(R.string.result_hint);
            }

            printed.getText().clear(); // clear data printed by the user
            stack.setText(calc.toString()); // display a string representation of the stack
            bar.setProgress(calc.size()); // display the current capacity of the stack
        }

        @Override
        public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
        {
            Log.i(DEBUG, "onCreateOptionMenu");
            super.onCreateOptionsMenu(menu, inflater);

            final int sms = getPreferencies(smsPref);
            final int toast = getPreferencies(toastPref);
            int txt = 0;

            if (toast == toastOff) txt = R.string.toast_on;
            else if (toast == toastOn) txt = R.string.toast_off;
            menu.add(menu.NONE, R.integer.toast_menu, menu.NONE, txt);

            final SubMenu smsMenu = menu.addSubMenu(menu.NONE, R.integer.sms_menu, menu.NONE, R.string.sms_options);
            smsMenu.add(menu.NONE, R.integer.sms_off, menu.NONE, R.string.no_sms);
            smsMenu.add(menu.NONE, R.integer.sms_activity, menu.NONE, R.string.activity_sms);
            smsMenu.add(menu.NONE, R.integer.sms_service, menu.NONE, R.string.service_sms);

            /* inflater.inflate(R.menu.menu_main, menu);
            super.onCreateOptionsMenu(menu, inflater);*/
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item)
        {
            Log.i(DEBUG, "Option menu selected");
            final int itemId =  item.getItemId();

            /* Handle Toast menu */
            if (itemId == R.integer.toast_menu)
            {
                final int toast = getPreferencies(toastPref);
                if (toast == toastOff)
                {
                    setPreferencies(toastPref, toastOn);
                    item.setTitle(R.string.toast_off);
                    toastIt(toastOnStr);
                }
                else if (toast == toastOn)
                {
                    setPreferencies(toastPref, toastOff);
                    item.setTitle(R.string.toast_on);
                    toastIt(toastOffStr);
                }
            }

            /* Handle SMS menu */
            else if (itemId == R.integer.sms_off)
            {
                setPreferencies(smsPref, smsOff);
                toastIt(smsOffStr);
            }
            else if (itemId == R.integer.sms_activity)
            {
                setPreferencies(smsPref, smsActivity);
                toastIt(smsActivityStr);
            }
            else if (itemId == R.integer.sms_service)
            {
                askForSmsPermission();
            }

            return super.onOptionsItemSelected(item);
        }

        /**
         * Enable SMS as Service
         */
        private void enableServiceSms()
        {
            setPreferencies(smsPref, smsService);
            toastIt(smsServiceStr);
        }

        /**
         * Disable SMS if permission has been denied
         */
        private void disableServiceSms()
        {
            setPreferencies(smsPref, smsOff);
            toastIt("SMS won't be sent automatically because the permission was denied.", Toast.LENGTH_LONG);
        }

        /**
         * Initialize and store persistant user preferencies.
         */
        private void initPreferencies()
        {
            Log.i(DEBUG, "initPreferencies");
            final SharedPreferences settings = getSharedPreferences();

            final String smsPref = getResources().getString(R.string.pref_sms_name);
            final int sms = settings.getInt(smsPref, getResources().getInteger(R.integer.sms));
            setPreferencies(smsPref, sms);

            final String toastPref = getResources().getString(R.string.pref_toast_name);
            final int toast = settings.getInt(toastPref, getResources().getInteger(R.integer.toast));
            setPreferencies(toastPref, toast);
        }

        /**
         * Get specific shared preference
         *
         * @param pref
         * @return
         */
        private int getPreferencies(String pref)
        {
            final SharedPreferences settings = getSharedPreferences();
            return settings.getInt(pref, getResources().getInteger(R.integer.option_not_defined));
        }

        /**
         * Persistant property storage
         *
         * @param pref
         * @param value
         */
        private void setPreferencies(String pref, Integer value)
        {
            final SharedPreferences settings = getSharedPreferences();
            final SharedPreferences.Editor editor = settings.edit();
            editor.putInt(pref, value);
            editor.commit();
        }

        /**
         * Get app shared preferences
         *
         * @return
         */
        private SharedPreferences getSharedPreferences()
        {
            return rootContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        }

        /**
         * Toast
         */
        private void toastIt(String mess)
        {
            toastIt(mess, Toast.LENGTH_SHORT);
        }

        /**
         * Toast
         *
         * @param mess String: message to be displayed
         * @param duration
         */
        private void toastIt(String mess, int duration)
        {
            Log.i(DEBUG, "Toast printed");
            Toast toast = Toast.makeText(rootContext, mess, duration);
            toast.setGravity(Gravity.BOTTOM | Gravity.CENTER, 0, 50);
            toast.show();
        }

        /**
         * Read the number composed by the user.
         *
         * @return int
         * @throws CalculetteException if the value is not a positive integer
         */
        private int getEntry() throws CalculetteException
        {
            final EditText printed = (EditText) rootView.findViewById(R.id.printed);

            try
            {
                return Integer.valueOf(printed.getText().toString());
            }
            catch (NumberFormatException e)
            {
                throw new CalculetteException("Integer required");
            }
        }

        /**
         * Perform user-defined process when an error occurs.
         * Authorized launch process: toast, sms as hidden service,
         * sms with prompt activity
         *
         * @param e Exception : the exception which has been risen
         * @see com.android.xcalebret.calculette.R.integer for context options
         */
        private void alertUser(Exception e)
        {
            Log.i(DEBUG, "[ERROR] " + e.getMessage());

            /*
             * Toast alert message
             */
            if (getSharedPreferences().getInt(toastPref, toastDefault) == toastOn)
                toastIt(e.getMessage());

            /*
             * SMS activity | SMS service
             */
            final int sms = getSharedPreferences().getInt(smsPref, smsDefault);
            if (sms == smsActivity)
                activitySMS(e.getMessage());
            else if(sms == smsService)
                serviceSMS(e.getMessage());
        }

        /**
         * SMS activity
         *
         * @param mess error message to be displayed
         */
        private void activitySMS(String mess)
        {
            Log.i(DEBUG, "start new SMS intent");
            final String phone = getResources().getString(R.string.recieverUri);
            final Intent sms = new Intent(Intent.ACTION_SENDTO);
            sms.setData(Uri.parse("smsto:" + phone));  // only SMS apps may respond
            sms.putExtra("sms_body", mess);

            if (sms.resolveActivity(rootContext.getPackageManager()) != null)
                startActivity(sms);
            else
                toastIt("No SMS app founded to send the message.");
        }

        /**
         * SMS Service
         *
         * @param mess error message to be displayed
         */
        private void serviceSMS(String mess)
        {
            Log.i(DEBUG, "try to send SMS as background service");
            final String phone = getResources().getString(R.string.recieverUri);

            try
            {
                SmsManager.getDefault().sendTextMessage(phone, null, mess, null, null);
                Log.i(DEBUG, "SMS sent as background service");
            }
            catch (IllegalArgumentException e)
            {
                toastIt("SMS requires phone number & message");
                Log.i(DEBUG, "SMS requires phone number & message");
            }
        }

        /**
         * Ask for SMS_SEND permission.
         * Deals with new API level 23 rules.
         */
        private void askForSmsPermission()
        {
            /* Permission denied or not already granted */
            if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.SEND_SMS)
                    != PackageManager.PERMISSION_GRANTED)
            {
                Log.i(DEBUG, "Permission SEND_SMS currently denied");
                if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                        Manifest.permission.SEND_SMS))
                {
                    Log.i(DEBUG, "Permission SEND_SMS already denied");
                    toastIt("Please, turn on SMS permissions for this app if you want to use the SMS auto profile.", Toast.LENGTH_LONG);
                }
                else
                {
                    // No explanation needed, we can request the permission.
                    Log.i(DEBUG, "SMS_SEND permission requested");
                    ActivityCompat.requestPermissions(getActivity(),
                            new String[]{Manifest.permission.SEND_SMS},
                            PERMISSIONS_REQUEST_SEND_SMS);
                }
            }

            /* Permission already granted */
            else
            {
                enableServiceSms();
            }
        }

        /**
         * Actions performed by the user
         */

        public void empiler()
        {
            try
            {
                calculette.enter(getEntry());
            }
            catch (CalculetteException e)
            {
                alertUser(e);
            }
        }

        public void add()
        {
            try
            {
                calculette.add();
            }
            catch (CalculetteException e)
            {
                alertUser(e);
            }
        }

        public void sub()
        {
            try
            {
                calculette.sub();
            }
            catch (CalculetteException e)
            {
                alertUser(e);
            }
        }

        public void mul()
        {
            try
            {
                calculette.mul();
            }
            catch (CalculetteException e)
            {
                alertUser(e);
            }
        }

        public void div()
        {
            try
            {
                calculette.div();
            }
            catch (CalculetteException e)
            {
                alertUser(e);
            }
        }

        public void clear()
        {
            calculette.clear();
        }

        public void undo()
        {
            try
            {
                calculette.pop();
            }
            catch (CalculetteException e)
            {/* void */}
        }

    }
}
